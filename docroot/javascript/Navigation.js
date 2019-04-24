//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 12290 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-05-01 08:56:53 -0400 (Fri, 01 May 2015) $
// $KeyWordsOff: $
//

/** Observers which will be invoked when the field is updated */
var problemObserver = new Observer("problemUpdate");
var sampleObserver = new Observer("sampleUpdate");
var skillObserver = new Observer("skillUpdate");
var studentObserver = new Observer("studentUpdate");

/** UI Lists */
var problemList = null;
var skillList = null;
var studentList = null;
var modelList = null;
var studentProblemModelList = null;
var stepExportModelList = null;

/** Initialize all necessary Lists and listeners */
function initDefaultNavigation(problemMultiSelect) {

    if (problemMultiSelect !== false) { problemMultiSelect = true; }

    if ($('samples')) {
        new NavigationBox.Base("samples");

        var deselectAll = $("sample_deselect");

        deselectAll.observe('mouseover', function (event) { $(this).addClassName('hover'); } );
        deselectAll.observe('mouseout', function (event) { $(this).removeClassName('hover'); });
    }

    if ($('problems')) {
        var options = {multiSelect:problemMultiSelect, observer:problemObserver, itemInfo:true};
        problemList = new NavigationBox.Component(
                   "problems", "Problems", window.location.pathname, options);
      sampleObserver.addListener(refreshProblemList);
    }

    if ($('skills')) {
        var options = {multiSelect:true, observer:skillObserver};
        skillList = new NavigationBox.Component(
                   "skills", "Knowledge Components", window.location.pathname, options);
      sampleObserver.addListener(refreshSkillList);
        //look at the first H2 which is the title and insert the wrench icon
        $("skills").down('span').insert({before : '<span id="navManageKCsetsButton" title="Add, edit, or load KC sets" class="wrench"></span>'});
    }

    if ($('students')) {
        var options = {multiSelect:true, observer:studentObserver};
        studentList = new NavigationBox.Component(
                   "students", "Students", window.location.pathname, options);
      sampleObserver.addListener(refreshStudentList);
    }

    if ($('skillModels')) {
        modelList = new SkillModelList($("skillModels"), false);
    }

    if ($('problemExportSkillModel')) {
        studentProblemModelList = new SkillModelList($("problemExportSkillModel"), false);
    }

    if ($('stepExportSkillModel')) {
        stepExportModelList = new SkillModelList($("stepExportSkillModel"), false);
    }

}

function refreshProblemList() {
    problemList.refreshList();
}

function refreshSkillList() {
    skillList.refreshList();
}

function refreshStudentList() {
    studentList.refreshList();
}

function refreshModelLists() {
    modelList.refresh();
    stepExportModelList.refresh();
}

/** A global list for the selected/deselected sample IDs. */
var sampleIdList = "";

/**
 * Invokes another method after the timeout has run out.  This allows the user
 * to make multiple sample selection changes and have one submit for all of them.
 * The timeout is in milliseconds and should be set to 1500, for one and a half seconds.
 * @param sampleId the sample id to select or deselect
 */
function selectSample(sampleId) {
    sampleIdList += sampleId + ",";
    var self = this;

    // Go ahead and check (or uncheck) the box so user gets
    // immediate feedback on their selection (Trac #507).
    var theLI = $("li_" + sampleId);
    if (theLI != null) {
        if (theLI.hasClassName('selectedSkill')) {
            theLI.removeClassName('selectedSkill');
        } else {
            theLI.addClassName('selectedSkill');
        }
    }

    setTimeout(function() { self.actuallySelectSample(); }, 1500);
}


/* Makes a call to select the sample as a full page refresh */
function actuallySelectSample() {
  if (sampleIdList != null && sampleIdList.length > 0) {
    var sampleSelectForm = document.createElement('FORM');
    sampleSelectForm.setAttribute('name', 'sample_select_form');
    sampleSelectForm.setAttribute('id', 'sample_select_form');
    sampleSelectForm.setAttribute('form', 'text/plain');
    sampleSelectForm.setAttribute('action', window.location.href);
    sampleSelectForm.setAttribute('method', 'POST');

    var sampleIdInput   = document.createElement('input');
    sampleIdInput.name  = "sampleId";
    sampleIdInput.type  = "hidden";
    sampleIdInput.value = sampleIdList;
    sampleSelectForm.appendChild(sampleIdInput);

    document.getElementsByTagName('body').item(0).appendChild(sampleSelectForm);
    sampleSelectForm.submit();
                sampleIdList = "";
  }
  // full page refresh for select sample
}

function refreshSampleList(sampleId) {
  //build the parameters string
  /* Want a full page refresh and not just an ajax request.
  var postParams = {
                requestingMethod: "Navigation.refreshSampleList",
                datasetId: dataset,
    sampleListRefresh: "true"
    }
  if (sampleId != null) {
    postParams['updatedSample'] = sampleId;
  }

  new Ajax.Request(window.location.href, {
    parameters: postParams,
    onComplete: processSampleRefresh,
    onException: function (request, exception) {
      throw(exception);
    }
  });
  */

  var sampleSelectForm = document.createElement('FORM');
  sampleSelectForm.setAttribute('name', 'sample_select_form');
  sampleSelectForm.setAttribute('id', 'sample_select_form');
  sampleSelectForm.setAttribute('form', 'text/plain');
  sampleSelectForm.setAttribute('action', window.location.href);
  sampleSelectForm.setAttribute('method', 'POST');

  var datasetIdInput = document.createElement('input');
  datasetIdInput.name="datasetId";
  datasetIdInput.type="hidden";
  datasetIdInput.value=dataset;
  sampleSelectForm.appendChild(datasetIdInput);

  var sampleListRefreshInput = document.createElement('input');
  sampleListRefreshInput.name="sampleListRefresh";
  sampleListRefreshInput.type="hidden";
  sampleListRefreshInput.value="true";
  sampleSelectForm.appendChild(sampleListRefreshInput);

  if (sampleId != null) {
      var updatedSampleInput = document.createElement('input');
      updatedSampleInput.name="updatedSample";
      updatedSampleInput.type="hidden";
      updatedSampleInput.value=sampleId;
      sampleSelectForm.appendChild(updatedSampleInput);
  }

  document.getElementsByTagName('body').item(0).appendChild(sampleSelectForm);
  sampleSelectForm.submit();
}

function processSampleRefresh(transport) {
  var sampleDiv = document.getElementById('samples');
  response = transport.responseText.evalJSON();
  sampleDiv.innerHTML = response.sampleNavDiv;
  var contentSampleNameElement = $("contentSampleName");
  if (contentSampleNameElement) {
      contentSampleNameElement.innerHTML = "Sample(s): " + response.sampleNames;
  } else {
      var contentBoxElement = $("contentHeaderCol1");
      if (contentBoxElement) {
          var newH1 = "<h1 id=\"contentSampleName\" class=\"samplename\">Sample(s): "
                  + response.sampleNames + "</h1>";
          contentBoxElement.insert(newH1);
      }
  }
  sampleObserver.invoke();
}

function selectSkillModel() {
  var modelSelector = document.getElementById("skill_model_select");
  var modelId = modelSelector.options[modelSelector.selectedIndex].value;

  if (modelId != null) {
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'skill_model_select_form');
    newForm.setAttribute('id', 'skill_model_select_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', window.location.pathname);
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="skillModelId";
    newInput.type="hidden";
    newInput.value=modelId;

    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
  }
}

function selectSecondarySkillModel() {
    var modelSelector = document.getElementById("secondary_skill_model_select");
  var modelId = modelSelector.options[modelSelector.selectedIndex].value;

  if (modelId != null) {
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'secondary_skill_model_select_form');
    newForm.setAttribute('id', 'secondary_skill_model_select_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', window.location.pathname);
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="secondarySkillModelId";
    newInput.type="hidden";
    newInput.value=modelId;

    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
  }
}
