//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 15419 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-08-06 12:05:45 -0400 (Mon, 06 Aug 2018) $
// $KeyWordsOff: $
//

/*Accordian nav object */
//var lcAccordian = null;
var sriProgressBar = false;
var lfaProgressBar = false;
var areaMouseover;

/** Curve type :: assistance_score. */
var ASSISTANCE_SCORE_TYPE = "assistance_score";
/** Curve type :: error_rate. */
var ERROR_RATE_TYPE = "error_rate";
/** Curve type :: average_incorrects. */
var AVG_INCORRECTS_TYPE = "average_incorrects";
/** Curve Type :: average_hints. */
var AVG_HINTS_TYPE = "average_hints";
/** Curve Type :: step_duration. */
var STEP_DURATION_TYPE = "step_duration";
/** Curve Type :: correct_step_duration. */
var CORRECT_STEP_DURATION_TYPE = "correct_step_duration";
/** Curve Type :: error_step_duration. */
var ERROR_STEP_DURATION_TYPE = "error_step_duration";

/**
 * Highlight the given point on the learning curve image.
 */
function highlightPoint(element) {
  if (element == null) {
    return;
  }
  // split on comma and parse to integers
    var pointCoords = $A(element.coords.split(",")).map(function (n) { return parseInt(n,10) });
    var pointHighlight = $('highlighted_point_div');
    var lcImageCoordinates = $('learning_curve_image').cumulativeOffset();
    var lcImageLeftCoord = lcImageCoordinates[0];
    var lcImageTopCoord = lcImageCoordinates[1];

    // Left offset different by browser.
    var pxOffset = 6;
    if (isThisIE()) {
      pxOffset = 8;
    }

    // the evens are "x" coordinates; we want the smallest one for the left value
    var pointLeftCoord = pointCoords.findAll(function(n,i) { return 0 == i % 2; }).min();
    pointLeftCoord += (lcImageLeftCoord - pxOffset);
    pointLeftCoord += 'px';

    // the odds are "y" coordinates; we want the smallest one for the top value
    var pointTopCoord = pointCoords.findAll(function(n,i) { return 1 == i % 2; }).min();
    pointTopCoord += (lcImageTopCoord - 6);
    pointTopCoord += 'px';

    pointHighlight.setStyle({
        position: 'absolute',
        left: pointLeftCoord,
        top: pointTopCoord
    });
    pointHighlight.observe('click', deselectPoint);
    pointHighlight.show();
    pointHighlight.onmouseover = function(e) {
      overlib(element.getAttribute('label'), 'highlighted_point_div');
    };
    pointHighlight.onmouseout = function(e) { nd() };
}

/**
 * opportunity selector element
 * @return opportunity selector element
 */
function oppSelector() {
    return $('pointInfoOpportunitySelector').firstDescendant();
}

/**
 * sample selector element
 * @return sample selector element
 */
function sampleSelector() {
  return $('pointInfoSampleSelector').firstDescendant();
}


/**
 * get the value contained in elt as an integer
 * @param elt the element
 * @return get the value contained in elt as an integer
 */
function intValue(elt) {
  return parseInt($(elt).value);
}

/**
 * value of the opportunity number selector as an integer
 * @return value of the opportunity number selector as an integer
 */
function oppValue() {
  return intValue(oppSelector());
}

/**
 * the area element from the image map for the series index and opportunity number
 * @param sampleId the series index
 * @param oppNo the opportunity number
 * @return the area element from the image map for the series index and opportunity number
 */
function pointElement(sampleId, oppNo) {
  return $(sampleId + '_' + oppNo + '_area');
}

/**
 * Select the point for this series index and the
 * opportunity number value from the opp. no. selector.
 * @param sampleId the series index
 */
function selectSamplePoint(sampleId) {
  if (isNaN(oppValue()) || oppValue() < 1) {
    oppSelector().value = 1;
  }
  pointInfoRequest({
                requestingMethod: "LearningCurve.selectSamplePoint",
                datasetId: dataset,
    sampleId: sampleId,
    oppNo: oppValue() - 1 });
}

// functions for events that change the opportunity number selection
var oppChanged, oppKey, prevOpp, nextOpp;

/**
 * Update the appropriate fields with data from point info.
 * @param pointInfo contains all of the information for the selected point
 * @param sampleId the series index for the selected point
 * @param oppNo the opportunity number for the selected point
 */
function updatePointInfoDetails(pointInfo, sampleId, oppNo) {
  if (!isNaN(parseInt(oppNo))) {
    oppNo = parseInt(oppNo) + 1;
  }
    oppSelector().value = oppNo;
    oppSelector().stopObserving('blur', oppChanged);
    oppSelector().stopObserving('keydown', oppKey);
    $('prevOppButton').stopObserving('click', prevOpp);
    $('nextOppButton').stopObserving('click', nextOpp);
    oppChanged = function() { selectSamplePoint(sampleId); };
    oppSelector().observe('blur', oppChanged);
    oppKey = function(event) {
      if (event.keyCode == Event.KEY_RETURN) {
        oppChanged();
      }
    }
    oppSelector().observe('keydown', oppKey);
  selectPointForOpportunity = function(oppNo) {
    oppSelector().value = oppNo;
    oppChanged();
  };
  oppMouseOver = function(button) {
        $(button).observe('mouseover', function(event) { this.addClassName('hover'); });
        $(button).observe('mouseout', function(event) { this.removeClassName('hover'); });
  };
    if (oppNo != '-') {
      if ($('pointInfo') && $('pointInfo').style.display == 'none') {
        hideShowInfoFor('hidePointInfoLink');
      }
      nextOpp = function() { selectPointForOpportunity(oppValue() + 1); }
      $('nextOppButton').observe('click', nextOpp);
      prevOpp = function() { selectPointForOpportunity(oppValue() - 1); }
      $('prevOppButton').observe('click', prevOpp);
      $A(['nextOppButton', 'prevOppButton']).each(oppMouseOver);
    } else {
      nextOpp = function() { selectPointForOpportunity(1); }
      $('nextOppButton').observe('click', nextOpp);
      oppMouseOver('nextOppButton');
    }

  $('pointInfoValue').update(pointInfo.value);
  $('pointInfoProblems').update(pointInfo.problemsCount);
  obs = pointInfo.obsString;
  if (pointInfo.dropped > 0) {
    obs += " (" + pointInfo.dropped + ")";
  }
  $('pointInfoObservations').update(obs);

  errBarValue = 0;
  if ($('select_error_bar_type').value == 'std_err') {
      errBarValue = pointInfo.stdError;
  } else {
      errBarValue = pointInfo.stdDeviation;
  }
  if ($('display_error_bars').checked) {
      if ((pointInfo.value != '-') && (errBarValue != '-')) {
    highVal = pointInfo.value + errBarValue;
    lowVal = pointInfo.value - errBarValue;
    $('pointInfoUpperBound').update(highVal.toFixed(2));
    if (lowVal < 0) {
        $('pointInfoLowerBound').update('-');
    } else {
        $('pointInfoLowerBound').update(lowVal.toFixed(2));
    }
      } else {
    $('pointInfoUpperBound').update('-');
    $('pointInfoLowerBound').update('-');
      }
  }
  $('pointInfoSkills').update(pointInfo.skillsCount);
  $('pointInfoSteps').update(pointInfo.stepsCount);
  $('pointInfoStudents').update(pointInfo.studentsCount);
  sampleSelector().update(pointInfo.sampleName);

  $('pointInfoPredictedValue').update(pointInfo.predicted);
  if ($('display_predicted').checked && pointInfo.predicted != "-") {
    $('pointInfoPredictedValue').up().show();
  } else {
    $('pointInfoPredictedValue').up().hide();
  }
}

/**
 * Show a popup menu to change the sample selection.
 * @param samples2Series maps sample names to the corresponding series index
 */
function popupSamples(samples2Series, orderedSampleNames) {
  new PopupMenu("Select a sample", "SampleSelector", orderedSampleNames, mouse_x, mouse_y,
      function(selected) { selectSamplePoint(samples2Series[selected]); },
      sampleSelector().firstChild.nodeValue);
}

/**
 * Replace point info field values with "-" when no point selected.
 */
function resetPointInfoDetails() {
    if (!$('pointInfo')) {
      return;
    }
  updatePointInfoDetails({ sampleName: "-", value: "-", skillsCount: "-", problemsCount: "-",
    stepsCount: "-", studentsCount: "-", observations: "-", predicted: "-" }, "-", "-");
}

/**
 * Request point info data from the server for the given parameters,
 * which either indicate the series index and opportunity number, or
 * a flag indicating fetch data for the previously selected point.
 * @param params indicate the series index and opportunity number, or
 * a flag indicating fetch data for the previously selected point
 */
function pointInfoRequest(params) {
    // send an AJAX call to the servlet to gather graph point information
    new Ajax.Request("PointInfo", {
        parameters: params,
        requestHeaders: { Accept: 'application/json;charset=UTF-8' },
        onSuccess: function(transport) { pointInfoRequestCallback(transport);},
        onException: function(request, exception) { throw(exception); }
    });
}

/**
 * Callback method for a pointInfoRequest.  If the response is "-", deselect the point.
 * Otherwise, check to see if the pointInfo area is on the screen (if it isn't then we
 * can't display anything).
 * @param transport the AJAX transport object.
 */
function pointInfoRequestCallback(transport) {
    response = transport.responseText.evalJSON();
         if (response.statusMessage) {
           deselectPoint();
         } else {
           if (response.pointInfo.value == '-') {
         deselectPoint();
           } else {
         if (!$('pointInfo')) {
             return;
         }
         updatePointInfoDetails(response.pointInfo, response.sampleId, response.oppNo);
         highlightPoint(pointElement(response.sampleId, response.oppNo));
         registerLCPIDObservers();
         if ($('pointInfoCol3').visible()) {
             pointInfoDetailsRequest();
         }
           }
         }
} // end pointInfoRequestCallback()

/**
 * Request point info data from the server for the given point info element.
 */
function selectPoint(element) {
    var elementInfo = $A(element.id.split("_"));
    pointInfoRequest({
        requestingMethod: "LearningCurve.selectPoint",
        datasetId: dataset,
      sampleId: elementInfo[0],
      oppNo: elementInfo[1] });
}

/**
 * Request point info data from the server for the previously selected point.
 */
function getSelectedPointInfo() {
    pointInfoRequest({
        requestingMethod: "LearningCurve.getSelectedPointInfo",
        datasetId: dataset,
      selected: true });
}

/**
 * Deselect/de-highlight a currently selected point on the lc graph.
 */
function deselectPoint() {
    if (!$('pointInfo')) {
      return;
    }
  resetPointInfoDetails();
    var pointHighlight = $('highlighted_point_div');
    pointHighlight.stopObserving('click', selectPoint);
    pointHighlight.hide();
    deregisterLCPIDObservers();
    new Ajax.Request("PointInfo", {
        parameters: {
                requestingMethod: "LearningCurve.deselectPoint",
                datasetId: dataset,
          deselect: true },
        requestHeaders: { Accept: 'application/json;charset=UTF-8' },
        onSuccess: function(transport) { clearLCPID(); },
        onException: function(request, exception) { throw(exception); }
    });
} // end deselectPoint()


/**
 * Validate min and maxOpportunity form elements.  Check to make sure the values
 * are a number and also an integer.
 */
function validateOpps() {
    var input = $('maxOpportunities');
    var s = input.value;
    var number = new Number(s);

    if (isNaN(number) || s.indexOf('.') != -1) {
      alert("Please enter an integer for maximum number of opportunities.");
      input.value = "";
      return false;
    } else {
      input.value = number;
    }

    input = $('minOpportunities');
    s = input.value;
    number = new Number(s);
    if (isNaN(number) || s.indexOf('.') != -1) {
      alert("Please enter an integer for minimum number of opportunities.");
      input.value = "";
      return false;
    } else {
      input.value = number;
    }
    if (!validateStdDevCutoff()) {
      return false;
    }

    return true;
}

function clearMinOpps() {
    $('minOpportunities').value = "";
    return true;
}

function clearMaxOpps() {
    $('maxOpportunities').value = "";
    return true;
}

function validateStdDevCutoff() {
    var input = $('stdDevCutoff');
    var s = input.value;
    var number = new Number(s);

    if (isNaN(number)) {
        alert("Please enter a valid number of std deviation cutoff");
        input.value = "";
        return false;
    } else {
        input.value = number;
    }
    return true;
}

function clearStdDevCutoff() {
    var stdDevCutoff = document.getElementById('stdDevCutoff');
    stdDevCutoff.value = "";
    return true;
}

/**
 * send an AJAX call to the servlet for graph generation
 */
function requestLearningCurveContent(params) {
  new Ajax.Request("LearningCurveContent", {
    parameters: $H({
                        requestingMethod: "LearningCurve.requestLearningCurveContent",
                        datasetId: dataset,
      ajaxRequest: "true",
      action: "post"
    }).merge(params),
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: displayLearningCurve,
        onException: function (request, exception) {
           throw(exception);
        }
    });
}

function selectType(input){
    var lcContentDiv = $('learningCurves');
    lcContentDiv.innerHTML="<h1>Loading... </h1>";
    document.body.style.cursor="wait";

    var predictedInput = $('display_predicted');
    var predictedLabel = predictedInput.next();
    var curveSelection = input;
    var stdDevCutoff = $('stdDevCutoff');
    var stdDevClearButton = $('stdDevClear');

    $('lc_selected_type').value = curveSelection;
    isErrorRateCurve(curveSelection);
    isLatencyCurve(curveSelection);
    requestLearningCurveContent({ learning_curve_type: curveSelection });
}

/**
 * isErrorRateCurve()
 * Checks if the supplied curve selection is an error_rate curve.<b>
 * If it is, then enable the view predicted option.
 */
function isErrorRateCurve(curveSelection) {
    var predictedInput = $('display_predicted');
    var predictedLabel = predictedInput.next();
    var includeHSInput = $('include_high_stakes');
    var includeHSLabel = $('include_high_stakes_label');
    var classifyLCs = $('classify_lcs_div');
    var viewClassified = $('viewClassifiedDiv').value;

    if (curveSelection == 'error_rate') {
        predictedInput.disabled=false;
        predictedLabel.removeClassName('disabled');
        if (predictedInput.checked) {
            modelList.displaySecondary();
        }

        includeHSInput.disabled = false;
        includeHSLabel.removeClassName('disabled');

        // Need to determine Navigation box has been collapsed.
        var navBoxHeader = $('lcNavBoxHeader');
        if ((viewClassified == "true") && (navBoxHeader.down('span').hasClassName('collapse'))) {
            classifyLCs.style.display = "block";
        }
        return true;
    } else {
        predictedInput.disabled=true;
        predictedLabel.addClassName('disabled');
        includeHSInput.disabled=true;
        includeHSLabel.addClassName('disabled');
        modelList.hideSecondary();
        classifyLCs.style.display = "none";
        return false;
    }
}

/**
 * isLatencyCurve()
 * Checks if the supplied curve selection is latency curve.
 * If it is, then enabled the stdDevCutoff and button.
 */
function isLatencyCurve(curveSelection) {
    var stdDevCutoff = $('stdDevCutoff');
    var stdDevClearButton = $('stdDevClear');

    if (curveSelection == STEP_DURATION_TYPE || curveSelection == CORRECT_STEP_DURATION_TYPE
        || curveSelection == ERROR_STEP_DURATION_TYPE) {
        stdDevCutoff.disabled=false;
      stdDevClearButton.disabled=false;
      return true;
    } else {
        stdDevCutoff.disabled=true;
      stdDevClearButton.disabled=true;
      return false;
    }
}

function viewPredicted(input){
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "LearningCurve.viewPredicted",
            datasetId: dataset,
            ajaxRequest: "true",
            view_predicted: input.checked,
            action: "post"
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: this.updateLearningCurve({}),
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function viewErrorBars(input){
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "LearningCurve.viewErrorBars",
            datasetId: dataset,
            ajaxRequest: "true",
            view_error_bars: input.checked,
            action: "post"
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: this.updateLearningCurve({}),
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function setErrorBarType(input){
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "LearningCurve.setErrorBarType",
            datasetId: dataset,
            ajaxRequest: "true",
            error_bar_type: input.value,
            action: "post"
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: this.updateLearningCurve({}),
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function includeHighStakes(input){
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "LearningCurve.includeHighStakes",
            datasetId: dataset,
            ajaxRequest: "true",
            include_high_stakes: input.checked,
            action: "post"
        },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: this.updateLearningCurve({ include_high_stakes: input.checked }),
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function toggleClassify(input){
    // If not classifying, disable the threshold inputs and 'Reclassify' button
    if (!input.checked) {
        $('student_threshold').disabled = true;
        $('opportunity_threshold').disabled = true;
        $('low_error_threshold').disabled = true;
        $('high_error_threshold').disabled = true;
        $('afm_slope_threshold').disabled = true;
        $('updateClassify').disabled = true;
    } else {
        $('student_threshold').disabled = false;
        $('opportunity_threshold').disabled = false;
        $('low_error_threshold').disabled = false;
        $('high_error_threshold').disabled = false;
        $('afm_slope_threshold').disabled = false;
        $('updateClassify').disabled = false;
    }

    var newForm = document.createElement('FORM');
    newForm.id   = "toggle_classify_form";
    newForm.name = "toggle_classify_form";
    newForm.form = "text/plain";
    newForm.action = window.location.href;;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name = "datasetId";
    newInput.type = "hidden";
    newInput.value = dataset;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name = "classify_lcs";
    newInput.type = "hidden";
    newInput.value = input.checked;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function classifyLCs(){
    var studentThreshold = $('student_threshold');
    var opportunityThreshold = $('opportunity_threshold');
    var lowErrorThreshold = $('low_error_threshold');
    var highErrorThreshold = $('high_error_threshold');
    var afmSlopeThreshold = $('afm_slope_threshold');

    if (validateThresholds(studentThreshold, opportunityThreshold, lowErrorThreshold,
                           highErrorThreshold, afmSlopeThreshold)) {

        var newForm = document.createElement('FORM');
        newForm.id   = "classify_lcs_form";
        newForm.name = "classify_lcs_form";
        newForm.form = "text/plain";
        newForm.action = window.location.href;;
        newForm.method = "post";

        var newInput = document.createElement('input');
        newInput.name = "datasetId";
        newInput.type = "hidden";
        newInput.value = dataset;
        newForm.appendChild(newInput);

        newInput = document.createElement('input');
        newInput.name = "student_threshold";
        newInput.type = "hidden";
        newInput.value = studentThreshold.value;
        newForm.appendChild(newInput);

        newInput = document.createElement('input');
        newInput.name = "opportunity_threshold";
        newInput.type = "hidden";
        newInput.value = opportunityThreshold.value;
        newForm.appendChild(newInput);

        newInput = document.createElement('input');
        newInput.name = "low_error_threshold";
        newInput.type = "hidden";
        newInput.value = lowErrorThreshold.value;
        newForm.appendChild(newInput);

        newInput = document.createElement('input');
        newInput.name = "high_error_threshold";
        newInput.type = "hidden";
        newInput.value = highErrorThreshold.value;
        newForm.appendChild(newInput);

        newInput = document.createElement('input');
        newInput.name = "afm_slope_threshold";
        newInput.type = "hidden";
        newInput.value = afmSlopeThreshold.value;
        newForm.appendChild(newInput);

        document.getElementsByTagName('body').item(0).appendChild(newForm);
        newForm.submit();
    }
}

function validateThresholds(student, opportunity, lowError, highError, afmSlope) {

    var value = student.value;
    var num = new Number(value);
    if (isNaN(num) || (value.indexOf('.') != -1) || (num < 1)) {
        alert("Please enter a positive integer for the student threshold.");
        return false;
    }

    value = opportunity.value;
    var num = new Number(value);
    if (isNaN(num) || (value.indexOf('.') != -1) || (num < 1)) {
        alert("Please enter a positive integer for the opportunity threshold.");
        return false;
    }

    value = lowError.value;
    var num = new Number(value);
    if (isNaN(num) || (num < 0) || (num > 100)) {
        alert("Please enter a valid percentage for the low error threshold.");
        return false;
    }

    value = highError.value;
    var num = new Number(value);
    if (isNaN(num) || (num < 0) || (num > 100)) {
        alert("Please enter a valid percentage for the high error threshold.");
        return false;
    }

    value = afmSlope.value;
    var num = new Number(value);
    if (isNaN(num) || (num < 0)) {
        alert("Please enter a value greater than 0 for the AFM slope threshold.");
        return false;
    }

    return true;
}

function selectExportOptions(input) {

    if (!$F('sri_displaySkills') && $(input) == $('sri_displaySkills')) {
        $('sri_displayLFAScore').checked = false;
    } else if ($F('sri_displayLFAScore') && $(input) == $('sri_displayLFAScore')) {
        $('sri_displaySkills').checked = true;
    }

    displaySkillsInput = document.getElementById("sri_displaySkills");
    //select the skills if they aren't already selected.
    var myAjax = new Ajax.Request('StepRollupExport', {
        parameters: {
            requestingMethod: "LearningCurve.selectExportOptions",
            datasetId: dataset,
            ajaxRequest: "true",
            learning_curve_request: "byStudentStep",
            displaySkills: $F('sri_displaySkills') ? true : false,
            displayPredicted: $F('sri_displayLFAScore') ? true : false
       },
       onSuccess: chooseContent,
       onException: function (request, exception) {
           throw(exception);
       }
   });
}

/**
 * Used as a dummy ajax target that just does nothing
 */
function doNothing() {
    //alert("LearningCurve.js: doNothing function");
}

/** AJAX request to update the learning curve report content. */
function updateLearningCurve(params) {
    $('learningCurves').innerHTML="<h1>Loading... </h1>";
    document.body.style.cursor="wait";
    requestLearningCurveContent(params);
}

/** AJAX response to display the learning curve. */
function displayLearningCurve (transport) {
    type = $('lc_selected_type');
    if (isErrorRateCurve(type.value)) {
        modelList.displaySecondary();
    }

    isLatencyCurve(type.value);
    $('learningCurves').innerHTML=transport.responseText;
    document.body.style.cursor="default";
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-learning-curve-line-graph"));

    //For QA testing.
    loadedForQA("true");
    resetPointInfoDetails();
    getSelectedPointInfo();

    // Can't do this if no sample is loaded and therefore hide/show links don't exist
    initHideShowInfo();

    initializeCategoryTooltips();
}

/** AJAX request to get the LFA values. */
function requestLfaValues() {
    modelList.hideSecondary();
    var lcContentDiv = $('learningCurves');
    lcContentDiv.innerHTML="<h1>Loading... </h1>";
    document.body.style.cursor="wait";
    new Ajax.Request("LfaValues", {
        parameters: {
                requestingMethod: "LearningCurve.requestLfaValues",
                datasetId: dataset,
          requestLfaValues: "true" },
        requestHeaders: {Accept: 'application/json;charset=UTF-8'},
        onSuccess: displayLfaValues,
        onException: function (request, exception) {
           throw(exception);
        }
    });
}

/** AJAX response to display the LFA values. */
function displayLfaValues(transport) {
    var lcContentDiv = $('learningCurves');
    lcContentDiv.innerHTML=transport.responseText;
    document.body.style.cursor="default";
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-model-values"));
    //For QA testing.
    loadedForQA("true");

    // Watch for changes to 'Sort & Tag' checkbox.
    var sortKcs = $('sort_kcs');
    if (sortKcs) {
        sortKcs.observe('click', enableThresholdButton);
    }
    var rateThreshold = $('learning_rate_threshold');
    if (rateThreshold) {
        rateThreshold.observe('keyup', enableThresholdButton);
    }
}

//----
/** AJAX request to get the kc models content from server. */
function requestKCModels() {
    loadedForQA("false");
    var contentType = "models";
    var contentDiv = $('learningCurves');
    var kcmSortBy = $("kcmSortBy");
    var kcmSortByValue = null;
    var kcmSortAscending = $("kcmSortAscending");
    var kcmSortAscendingValue = null;
    if (kcmSortBy) {
        kcmSortByValue =  kcmSortBy.options[kcmSortBy.selectedIndex].value;
    }
    if (kcmSortAscending) {
        kcmSortAscendingValue =  kcmSortAscending.options[kcmSortAscending.selectedIndex].value;
    }
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    //new Ajax.Request(window.location.href, {
    new Ajax.Request("DatasetInfo", {
        parameters: {
            requestingMethod: "LearningCurve.requestKCModels",
            datasetId: dataset,
            kcmSortBy: kcmSortByValue,
            kcmSortAscending: kcmSortAscendingValue,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayKCModels,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the overview content. */
function displayKCModels(transport) {
    var contentDiv = $('learningCurves');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    selectSubtab("kcModels");
    initModelControls();
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-kc-models"));
    //For QA testing.
    loadedForQA("true");
}
//----

/** AJAX request to get the overview content from server. */
function requestByStudentStep() {
    $('learningCurves').innerHTML = "<div id='stepRollupPreview'/>";

    var exportAllowed = "true";
    if ($('datasetExportAllowed')) {
        exportAllowed = $('datasetExportAllowed').value;
    }

    var options = {
        exportCall: function () {
            if (sriProgressBar != null && sriProgressBar) {
                //DS721 not sure if this will break something else. [8/17/2009 ads]
                //sriProgressBar.cancelExport();
                sriProgressBar.closeAll();
            }
            sriProgressBar = false;

            var postParams = {}
            postParams['requestingMethod'] = "exporter.requestByStudentStep";
            postParams['datasetId'] = dataset;
            postParams['export_start'] = "true";
            postParams['use_cached_version'] = $('use_cached_version').checked;
            new Ajax.Request("StepRollupExport", {
                parameters: postParams,
                onComplete: function () {
                    sriProgressBar = new ProgressBar(
                        function () {
                            var newForm = document.createElement('FORM');
                            newForm.setAttribute('name', 'export_form');
                            newForm.setAttribute('id', 'export_form');
                            newForm.setAttribute('form', 'text/plain');
                            newForm.setAttribute('action', "StepRollupExport?datasetId=" + dataset );
                            newForm.setAttribute('method', 'POST');

                            var newInput = document.createElement('input');
                            newInput.name="get_export_file";
                            newInput.type="hidden";
                            newInput.value="true";
                            newForm.appendChild(newInput);

                            document.getElementsByTagName('body').item(0).appendChild(newForm);
                            newForm.submit();
                        },
                        'StepRollupExport');
                },
                onException: function (request, exception) {
                  throw(exception);
                }
            });
        },
        exportAllowed: exportAllowed,
        truncate: 50,
        extraParams: {datasetId: dataset}
    }
    new PageGrid('stepRollupPreview', "Student-Step Rollup", 'StepRollupExport', options);
}


var contentTiming = false;
function chooseContent() {
    if (contentTiming) {
        clearTimeout(contentTiming);
        contentTiming = false;
    }
    contentTiming = setTimeout(initContent, 1000);
}

var subtabs = $A(new Array());
subtabs.push({
        linkId: "lc_subtab_link",
        linkLabel: "Line Graph",
        linkName: "learningCurve",
        onClick: function () {
          selectSubtab("learningCurve");
            chooseContentInvoker("learningCurve");
        }
    });
subtabs.push({
        linkId: "sr_subtab_link",
        linkLabel: "Student-Step Rollup",
        linkName: "byStudentStep",
        onClick: function () {
          selectSubtab("byStudentStep");
            chooseContentInvoker("byStudentStep");
        }
    });
subtabs.push({
        linkId: "lfa_subtab_link",
        linkLabel: "Model Values",
        linkName: "lfaValues",
        onClick: function () {
            selectSubtab("lfaValues");
            chooseContentInvoker("lfaValues");
        }
    });
subtabs.push({
    linkId: "kcm_subtab_link",
    linkLabel: "KC Models",
    linkName: "kcModels",
    onClick: function () {
      selectSubtab("kcModels");
        chooseContentInvoker("kcModels");
    }
});
function chooseContentInvoker(contentType) {
    if (contentType.responseText) {
        contentType=contentType.responseText;
    }
    lcNavDiv = document.getElementById("learningCurveNav");
    lfaValuesDiv = document.getElementById("lfaValuesNav");
    probNavDiv = document.getElementById("problems");
    skillModelsNavDiv = document.getElementById("skillModels");
    skillsNavDiv = document.getElementById("skills");
    studentsNavDiv = document.getElementById("students");

    showSamplesDiv(contentType);
    if (contentType == "learningCurve") {
        lcNavDiv.style.display="block";
        lfaValuesDiv.style.display="none";
        probNavDiv.style.display="none";
        skillModelsNavDiv.style.display="block";
        skillsNavDiv.style.display="block";
        studentsNavDiv.style.display="block";
        hideStepRollup();
    } else if (contentType == "byStudentStep") {
        lcNavDiv.style.display="none";
        lfaValuesDiv.style.display="none";
        probNavDiv.style.display="block";
        skillModelsNavDiv.style.display="block";
        skillsNavDiv.style.display="block";
        studentsNavDiv.style.display="block";
        showStepRollup();
        //Update the report-level contextual help content
        theHelpWindow.updateContent($("help-student-step-rollup"));
    } else if (contentType == "kcModels") {
        // hide the nav boxes for the other subtabs
        lcNavDiv.style.display="none";
        lfaValuesDiv.style.display="none";
        probNavDiv.style.display="none";
        // also hide the nav boxes which have no effect on this page
        skillModelsNavDiv.style.display="none";
        skillsNavDiv.style.display="none";
        studentsNavDiv.style.display="none";
        hideStepRollup();
    } else { // Model Values
        lcNavDiv.style.display="none";
        lfaValuesDiv.style.display="block";
        probNavDiv.style.display="none";
        skillModelsNavDiv.style.display="block";
        skillsNavDiv.style.display="none";
        studentsNavDiv.style.display="none";
        hideStepRollup();
    }

    // If necessary, modify the header, e.g., sample and kcm names
    modifyHeaderContent(contentType);

    new Ajax.Request(window.location.href, {
        parameters: {
                requestingMethod: "LearningCurve.chooseContentInvoker",
                datasetId: dataset,
                content_type: contentType },
        onSuccess: showContent,
        onException: function (request, exception) {
           throw(exception);
        }
    });
    selectSubtab(contentType);
}

function hideStepRollup() {

    var kcmNavHdr = $('kcModelNavHeader');
    if (kcmNavHdr) {
        kcmNavHdr.show();
    }
    jQuery('.skill_model_select').removeAttr('disabled');
    var stepExNav = $('stepExportNav');
    if (stepExNav) {
        stepExNav.hide();
    }
    var primaryLabel = $('primary_kc_model_label');
    if (primaryLabel) {
        primaryLabel.show();
    }
    var cachedExpSelection = $('cached_export_selection_div');
    if (cachedExpSelection) {
        cachedExpSelection.hide();
    }
    modelList.displaySecondary();
    hideExportFileStatus();
    resetKCMHeader();


}
function showStepRollup() {

    initExportOptionHandlers();
    $('kcModelNavHeader').hide();

    jQuery('.skill_model_select').attr('disabled', 'disabled');
    $('stepExportNav').show();
    $('skillModels').hide();
    if ($('primary_kc_model_label')) { $('primary_kc_model_label').hide(); }
    if ($F('exportStepIncludeNoKCs') || $F('exportStepIncludeAllKCs')) {
        $('skills').hide();
    } else {
        $('skills').show();
    }

    $('students').show();
    $('problems').show();

    modelList.hideSecondary();

    resetKCMHeader();

    $('cached_export_selection_div').show();

    showExportFileStatus();
    updateExportFileStatus();
}

function showSamplesDiv(contentType) {

    samplesDiv = document.getElementById("samples");
    kcModelsSamplesDiv = document.getElementById("kcModelsSamplesDiv");

    if (contentType == "learningCurve") {
        samplesDiv.style.display="block";
        kcModelsSamplesDiv.style.display="none";
    } else if (contentType == "byStudentStep") {
        samplesDiv.style.display="block";
        kcModelsSamplesDiv.style.display="none";
    } else if (contentType == "kcModels") {
        samplesDiv.style.display="none";
        kcModelsSamplesDiv.style.display="block";
    } else { // Model Values
        samplesDiv.style.display="none";
        kcModelsSamplesDiv.style.display="none";
    }
}

function modifyHeaderContent(contentType) {

    var sampleNameDiv = $('contentSampleName');
    var kcModelNameDiv = $('contentKcModelName');

    if (contentType == "learningCurve") {
        if (sampleNameDiv) {
            sampleNameDiv.show();
        }
        if (kcModelNameDiv) {
            kcModelNameDiv.show();
        }
    } else if (contentType == "byStudentStep") {
        if (sampleNameDiv) {
            sampleNameDiv.show();
        }
        if (kcModelNameDiv) {
            kcModelNameDiv.show();
        }
    } else if (contentType == "kcModels") {
        if (sampleNameDiv) {
            sampleNameDiv.hide();
        }
        if (kcModelNameDiv) {
            kcModelNameDiv.hide();
        }
    } else { // Model Values
        if (sampleNameDiv) {
            sampleNameDiv.hide();
        }
        if (kcModelNameDiv) {
            kcModelNameDiv.show();
        }
    }
}

function resetKCMHeader() {
    var selectedKCMArea = $('selectedKCM');
    if (selectedKCMArea) {
        selectedKCMArea.show();
    }
    var allSelectedKCMArea = $('allSelectedKCM');
    if (allSelectedKCMArea) {
        allSelectedKCMArea.hide();
    }
}

function initHideShowInfo() {
    if ($('hideGraphInfoLink')) {
        $('hideGraphInfoLink').onclick = this.hideShowInfo.bindAsEventListener("hideGraphInfoLink");
        $('hideObservationTableLink').onclick = this.hideShowInfo.bindAsEventListener("hideObservationTableLink");
        $('hidePointInfoLink').onclick = this.hideShowInfo.bindAsEventListener("hidePointInfoLink");
        $('pointInfoDetailsClose').onclick = this.hideShowInfoDetails.bindAsEventListener("pointInfoDetailsClose");
    }
    hideShowRequest({
        requestingMethod: "LearningCurve.initHideShowInfo",
        datasetId: dataset
    });
}

function hideShowInfo() {
  hideShowInfoFor(this.valueOf());
}

/**
 * InfoDetails gets its own method because of the HTML layout.
 */
function hideShowInfoDetails() {
    linkId = this.valueOf();
    highlightSelectedMeasure(null);
    hideShowRequest({
        requestingMethod: "LearningCurve.hideShowInfoDetails",
        datasetId: dataset,
        linkId: linkId,
        hideShow: $(linkId).up().visible() ? "hide" : "show"
    });
}

function hideShowInfoFor(linkId) {
    //doing this relatively, but not sure how else to do it
    hideShowRequest({
        requestingMethod: "LearningCurve.hideShowInfoFor",
        datasetId: dataset,
        linkId: linkId,
      hideShow: $(linkId).up().next().visible() ? "hide" : "show"
    });
}

/**
 * Send a request to the server to get or set hide/show information, then hide or show
 * divs appropriately.
 * @param params the link id and whether to hide or show, or empty to request existing settings
 * @return asynchronously hide or show divs according to server response
 */
function hideShowRequest(params) {
  new Ajax.Request("HideShow", {
    parameters: params,
    requestHeaders: { Accept: 'application/json;charset=UTF-8' },
    onSuccess: function(transport) {
      hideShowCallback(transport);
    },
    onException: function(request, exception) { throw(exception); }
  });
}

/**
 * Callback method for the AJAX call to the HideShow servlet.
 * Loop through the hash of hide/show information and set visbility accordingly.
 * @param transport the AJAX transport object.
 */
function hideShowCallback(transport) {
    $H(transport.responseText.evalJSON()).each(function (linkHideShow) {
      link = $(linkHideShow.key);
      if (link) {
        // pointInfoDetails close is unique because the area to hide/show is only .up()
        if (link == $('pointInfoDetailsClose')) {
            elementToHideShow = link.up(); //doing this relatively, but not sure how else to do it
        if (linkHideShow.value == 'hide') {
          elementToHideShow.hide();
        } else {
          elementToHideShow.show();
        }
        } else {
       currentLinkText = link.innerHTML;
       elementToHideShow = link.up().next(); //doing this relatively, but not sure how else to do it
        if (linkHideShow.value == 'hide') {
          elementToHideShow.hide();
          link.innerHTML = currentLinkText.replace("hide","show");
        } else {
          elementToHideShow.show();
          link.innerHTML = currentLinkText.replace("show","hide");
        }
        }

      }
    });
} // end hideShowCallback()

function initContent() {
    new Ajax.Request(window.location.href, {
        parameters: {
                requestingMethod: "LearningCurve.initContent",
                datasetId: dataset,
          content_type: "get" },
        onSuccess: chooseContentInvoker,
        onException: function (request, exception) { throw(exception); }
    });
}

function initSubtabs() {
  //initialize the subtabs.
    var subtabDiv = $("subtab");
    subtabs.each(function (subtab) {
        subtabDiv.insert('<a id="' + subtab.linkId + '">' + subtab.linkLabel + '</a>');
        $(subtab.linkId).observe('click', subtab.onClick);
    });
}


function studentStepSkillModelInit(selectedModels) {
    if ($F('exportStepIncludeKCs')) {
        jQuery('.skill_model_select').removeAttr('disabled');
        $('selectedKCM').innerHTML = selectedModels;
    } else {
        jQuery('.skill_model_select').attr('disabled', 'disabled');
        if ($F('exportStepIncludeAllKCs')) {
            $('selectedKCM').innerHTML = 'All Models';
        } else {
            $('selectedKCM').innerHTML = 'No Models';
        }
    }
}

/**
 * Add listeners to the export option handlers.
 */
function initExportOptionHandlers() {
    var update = function() {
        new Ajax.Request('Export', {
            parameters: {
                requestingMethod: "exporter.initExportOptionHandlers",
                datasetId: dataset,
                determine_content: "true",
                learning_curve_request: "byStudentStep",
                exportStepIncludeAllKCs: $F('exportStepIncludeAllKCs') ? true : false,
                exportStepIncludeKCs: $F('exportStepIncludeKCs') ? true : false,
                exportStepIncludeNoKCs: $F('exportStepIncludeNoKCs') ? true : false
            },
            onComplete: updateContent,
            onException: function (request, exception) {
              throw(exception);
            }
        });
    }

    $("exportStepIncludeAllKCs").observe('click', updateStepRollup);
    $("exportStepIncludeKCs").observe('click', updateStepRollup);
    $("exportStepIncludeNoKCs").observe('click', updateStepRollup);
}


/**
 * Gets the current content to be displayed along with a check
 * that at least one sample is selected.
 * @param subtab if content type is known, pre-select the subtab.
 */
function updateStepRollup() {
    var params = {
            requestingMethod: "LearningCurve.updateStepRollup",
            datasetId: dataset,
            learning_curve_request: "byStudentStep",
            determine_content: "true",
            exportStepIncludeAllKCs: $F('exportStepIncludeAllKCs') ? true : false,
            exportStepIncludeKCs: $F('exportStepIncludeKCs') ? true : false,
            exportStepIncludeNoKCs: $F('exportStepIncludeNoKCs') ? true : false};


    new Ajax.Request('Export', {
        parameters: params,
        onComplete: updateContent,
        onException: function (request, exception) {
          throw(exception);
        }
    });

}

function selectSubtab(selectedLink) {
    lcLink = document.getElementById("lc_subtab_link");
    srLink = document.getElementById("sr_subtab_link");
    lfaLink = document.getElementById("lfa_subtab_link");
    kcmLink = document.getElementById("kcm_subtab_link");
    if (selectedLink == "learningCurve") {
        lcLink.className="selected";
        srLink.className="";
        lfaLink.className="";
        kcmLink.className="";
    } else if (selectedLink == "byStudentStep") {
        lcLink.className="";
        srLink.className="selected";
        lfaLink.className="";
        kcmLink.className="";
    } else if (selectedLink == "kcModels") {
        lcLink.className="";
        srLink.className="";
        lfaLink.className="";
        kcmLink.className="selected";
    } else {
        lcLink.className="";
        srLink.className="";
        lfaLink.className="selected";
        kcmLink.className="";
    }

    // Sorting changes on the "KC Models" subtab may affect the display on other subtabs...
    refreshModelLists();
    updateKCModelHeader(selectedLink);
}

function updateKCModelHeader(subtab) {
    var modelSelector = document.getElementById("skill_model_select");
    var primaryModel = modelSelector.options[modelSelector.selectedIndex].label;

    if (subtab == "byStudentStep") {
        studentStepSkillModelInit(primaryModel);
    } else if (subtab != "kcModels") {
        $('selectedKCM').innerHTML = primaryModel;
    }
}

function showContent(transport) {
    var contentType=transport.responseText;
    if (contentType == "byStudentStep") {
      requestByStudentStep();
      updateStepRollup();
    } else if (contentType == "lfaValues") {
        requestLfaValues();
    } else if (contentType == "kcModels") {
        requestKCModels();
    } else {
      // default to learning curve in the case of an invalid contentType
      // see DS677
        updateLearningCurve();
    }
}


/**
 * Walks through all the subtabs and figures out which one is currently selected.
 */
function getSelectedSubtab() {

    var selected = false;

    subtabs.each(function (subtab) {
        if ($(subtab.linkId).hasClassName("selected")) { selected = subtab.linkName; }
    });

    //didn't find a selected subtab, so select by transaction.
    if (!selected) {
        selected = "learningCurve"
        selectSubtab("learningCurve");
    }

    return selected;
}


/*
 * Display the Sample Cached File Information.
 */
function displaySampleCachedFileInfo(sampleIdList,
                                     sampleNameList,
                                     skillModelsNotCachedList,
                                     cachedFileStatusList,
                                     samplesThatRequireCaching){

    var mySampleName = null, mySampleSkillModelNotCached = null, mySampleCachedFileStatus = null;
    var cellId = '';
    var isSampleRequireCaching = false, hasSkillModelsToCache = false;
    var tableCell = $('sample_cached_file_info_table');
    if (tableCell) {
        if (sampleIdList != null){
            var firstTbody = tableCell.getElementsByTagName("tbody")[0];
            if (firstTbody) {
                tableCell.removeChild(firstTbody);
            }
            var tBody = document.createElement('tbody');
            // loop through the sample list
            for (var i = 0; i < sampleIdList.length; i++) {
                // reset variables
                isSampleRequireCaching = false;
                hasSkillModelsToCache = false;

                // create a TR element
                var newTR = document.createElement('tr');
                tBody.appendChild(newTR);

                // construct cell id
                mySampleId = sampleIdList[i];
                mySampleName = sampleNameList[i];
                cellId = mySampleName + '_' + mySampleId;

                // create TD for name
                var newNameTD = document.createElement('td');
                newNameTD.id = cellId + '_name';
                newNameTD.innerHTML = mySampleName;
                newTR.appendChild(newNameTD);

                // get values and pass in to the status field to get the correct image displayed
                if (skillModelsNotCachedList[i] != null && skillModelsNotCachedList[i] != undefined) {
                    mySampleSkillModelNotCached = removeBrackets(skillModelsNotCachedList[i]);
                }

                if (mySampleSkillModelNotCached != null && mySampleSkillModelNotCached != ''){
                    hasSkillModelsToCache = true;
                }

                mySampleCachedFileStatus = cachedFileStatusList[i];
                if (mySampleCachedFileStatus == null) {
                    mySampleCachedFileStatus = '-';
                }

                // create TD for status
                var newSkmTd = document.createElement('td');
                newSkmTd.id = cellId + '_status';
                if (samplesThatRequireCaching != null) {

                    for (var c = 0; c < samplesThatRequireCaching.length; c ++ ){
                        if (mySampleId == samplesThatRequireCaching[c]){
                            isSampleRequireCaching = true;
                            break;
                        }
                    }
                }
                var imageTD = getSampleImage(cellId, hasSkillModelsToCache,
                                             mySampleCachedFileStatus, isSampleRequireCaching);
                newSkmTd.appendChild(imageTD);
                newTR.appendChild(newSkmTd);

                // create TD for date
                var newCfsTD = document.createElement('td');
                newCfsTD.id = cellId + '_date';
                newCfsTD.innerHTML = mySampleCachedFileStatus;
                newTR.appendChild(newCfsTD);

                // add tool tips
                var newToolTipTR = getToolTip(cellId, mySampleSkillModelNotCached,
                                              mySampleCachedFileStatus, isSampleRequireCaching);
                tBody.appendChild(newToolTipTR);
           }// end for loop
           tableCell.appendChild(tBody);
       } // end if (sampleIdList != null)

    } // end if (tableCell)
}

/*
 * Removes square brackets from the text.
 * */
function removeBrackets(textToClean){
    return textToClean.replace('[', '').replace(']', '');
}


/*
 * Constructs an image element to be used in the Sample Cached File Info cell.
 */
function getSampleImage(cellId, hasSkillModelsToCache, cachedFileStatus, isSampleRequireCaching){
    var imageTD = document.createElement('img');
    if (cachedFileStatus == '-'){
        imageSource = 'images/hourglass.png';
    } else if (isSampleRequireCaching || hasSkillModelsToCache){
        imageSource = 'images/alert.gif';
    } else if (!isSampleRequireCaching && !hasSkillModelsToCache){
        imageSource = 'images/tick.png';
    }

    imageTD.id = cellId + '_status_img';
    imageTD.setAttribute('class', 'export_file_status');
    imageTD.src = imageSource;
    return imageTD;
}

/**
 * This function updates the content based on the currently selected subtab.
 * It can take either a transport object with the noSamplea and subtab set, or
 * it just checks the page.
 */
function updateContent(transport) {
    var subtab = false;
    var sampleIdList = null;
    var sampleNameList = null;
    var skillModelsNotCachedList = null;
    var cachedFileStatusList = null;
    var samplesThatRequireCachingList = null;

    if (transport) {

        var json = transport.responseText.evalJSON(true);
        subtab = json.subtab;
        sampleIdList = json.lstSampleId;
        sampleNameList = json.lstSampleName;
        skillModelsNotCachedList = json.lstSkillModelsNotCached;
        cachedFileStatusList = json.lstCachedFileStatus;
        samplesThatRequireCachingList = json.lstSamplesThatRequireCaching;
        selectedSkillModelList = json.lstSelectedSkillModels;
        displaySampleCachedFileInfo(sampleIdList,
                                    sampleNameList,
                                    skillModelsNotCachedList,
                                    cachedFileStatusList,
                                    samplesThatRequireCachingList);
    }

    if (!subtab) {
        subtab = getSelectedSubtab();
    } else {
        selectSubtab(subtab);
    }
    if (subtab == "byStudentStep") {
        //theHelpWindow.updateContent($("help-export-step"));
        $('cached_export_selection_div').show();
        $('stepExportNav').show();
        //Nudge here prevents all nav box margins from collapsing in IE8
        $('samples').setStyle({
            marginBottom: '0.5em'
        });
        if ($F('exportStepIncludeNoKCs') || $F('exportStepIncludeAllKCs')) {
            $('skills').hide();
        } else {
            $('skills').show();
        }

        $('kcModelNavHeader').hide();
        $('primary_kc_model_label').hide();

        studentStepSkillModelInit(selectedSkillModelList);
        $('students').show();
        $('problems').show();

        resetKCMHeader();
        if ($('contentSetName')) {
            $('contentSetName').show();
        }
        if ($('contentSetNameModified')) {
            $('contentSetNameModified').show();
        }

        requestByStudentStep();
        showExportFileStatus();
        updateExportFileStatus();

    }
}

function showExportFileStatus() {
    var exportFileStatus = $('sample_cached_file_info_div');
    if (exportFileStatus != null) {
        $('sample_cached_file_info_div').show();
    }
}

function hideExportFileStatus() {
    var exportFileStatus = $('sample_cached_file_info_div');
    if (exportFileStatus != null) {
        $('sample_cached_file_info_div').hide();
    }
}

/**
 * Generate tooltips for the cached file info section.
 */
function updateExportFileStatus() {
    var options = new Array();
    options['delay'] = '100';
    options['timeout'] = '30000';
    options['extraClasses'] = 'export_file_status';

    $$('.export_file_status').each(function(item) {
        var id = item.id.split("_status_img")[0];
        var tooltipTextId = id + "_tooltip_text";
        new ToolTip(item.id, $(tooltipTextId).innerHTML, options);

    });
}

/*
 * Constructs a tool tip section to be used in the Sample Cached File Info.
 */
function getToolTip(cellId, skillModelsNotCachedList, cachedFileStatus, isSampleRequireCaching){

    var infoMsg = '';
    var hasSkillModelsToCache = false;

    if (skillModelsNotCachedList != '' && skillModelsNotCachedList.length > 0) {
        hasSkillModelsToCache = true;
    }

    // create the P element
    var infoMsgParagraph = document.createElement('p');

    // create a TR element
    var toolTipTR = document.createElement('tr');
    toolTipTR.setAttribute("style", "visibility:hidden; display:none");

    // create a TD element
    var toolTipTD = document.createElement('td');
    toolTipTD.setAttribute("colspan", "3");
    toolTipTD.id = cellId + '_tooltip_text';

    // if the file has not been cached
    if (cachedFileStatus == '-'){
        infoMsg = 'Depending on the size of this sample, exporting may take a while. ';
        infoMsg += 'For a speedier export, only export the \"All Data\" sample (or others that are up-to-date.)';
        text = document.createTextNode(infoMsg);
        infoMsgParagraph.appendChild(text);
        toolTipTD.appendChild(infoMsgParagraph);
    } else if (isSampleRequireCaching || hasSkillModelsToCache) {

        // if the sample requres caching
        if (isSampleRequireCaching) {
            infoMsg = 'The export file and preview for this sample does not include the latest data.';
            text = document.createTextNode(infoMsg);
            infoMsgParagraph.appendChild(text);
            toolTipTD.appendChild(infoMsgParagraph);
        }
        // if the skill models require caching
        if (hasSkillModelsToCache) {
            infoMsgParagraph = document.createElement('p');
            infoMsg = 'The following KC models are not yet included in this file: ';
            text = document.createTextNode(infoMsg);
            infoMsgParagraph.appendChild(text);
            var skmUL = document.createElement('ul');
            skillModelsNotCachedList = skillModelsNotCachedList.split(',');

            for (var i = 0; i < skillModelsNotCachedList.length; i++) {
                var skillModelName = skillModelsNotCachedList[i];
                var skmLI = document.createElement('li');
                text = document.createTextNode(skillModelName);
                skmLI.appendChild(text);
                skmUL.appendChild(skmLI);
            }
            infoMsgParagraph.appendChild(skmUL);
            toolTipTD.appendChild(infoMsgParagraph);
        }

        infoMsgParagraph = document.createElement('p');
        infoMsg = 'The latest data for this sample will be included in the export file by tomorrow morning. ';
        infoMsg += 'If you need these data before then, please contact us.';
        text = document.createTextNode(infoMsg);
        infoMsgParagraph.appendChild(text);
        toolTipTD.appendChild(infoMsgParagraph);
    } else if (!isSampleRequireCaching && !hasSkillModelsToCache) {
        infoMsgParagraph = document.createElement('p');
        infoMsg = 'Export file is up-to-date.';
        text = document.createTextNode(infoMsg);
        infoMsgParagraph.appendChild(text);
        toolTipTD.appendChild(infoMsgParagraph);
    }// end else

     toolTipTR.appendChild(toolTipTD);

     return toolTipTR;
}


function getStepRollupExportFile() {
  var newForm = document.createElement('FORM');
  newForm.setAttribute('name', 'export_form');
  newForm.setAttribute('id', 'export_form');
  newForm.setAttribute('form', 'text/plain');
  newForm.setAttribute('action', "StepRollupExport?datasetId=" + dataset );
  newForm.setAttribute('method', 'POST');

  var newInput = document.createElement('input');
  newInput.name="get_export_file";
  newInput.type="hidden";
  newInput.value="true";
  newForm.appendChild(newInput);

  document.getElementsByTagName('body').item(0).appendChild(newForm);
  newForm.submit();

}

/** Initialize all javascript items necessary for the Learning Curve Report */
function initLearningCurve() {
    initDefaultNavigation();

    //check the type, if it is 'error_rate' check if the predicted is selected for the model list.
    type = $('lc_selected_type');
    if (type.value == 'error_rate') {
        modelList.displaySecondary();
    }

    // Initialize navigation tooltips
    var options = new Array();
    options['extraClasses'] = 'infoDiv';
    options['timeout'] = '100000';
    options['delay'] = '100';
    options['extraClasses'] = 'lcNavToolTip';

    oppCutoffTooltipContent = $('opp_cutoff_tooltip_content').innerHTML;
    stdevTooltipContent = $('stdev_cutoff_tooltip_content').innerHTML;

    classifyTooltip =  $('classify_lcs_tooltip_content');
    if (classifyTooltip) {
      classifyTooltipContent = classifyTooltip.innerHTML;
      new ToolTip("classify_lcs_info", classifyTooltipContent, options);
    }

    new ToolTip("opp_cutoff_info", oppCutoffTooltipContent, options);
    new ToolTip("stdev_cutoff_info", stdevTooltipContent, options);

    isLatencyCurve(type.value);

    new NavigationBox.Base("learningCurveNav");
    createManageKCSetsDialog();

    //listen for changes in skills to update the learning curves.
    skillObserver.addListener(chooseContent);
    skillObserver.addListener(updateSetModifier);

    //listen for changes in the students to update the learning curves.
    studentObserver.addListener(chooseContent);

    //listen for changes in the problem nav box to update the LCs (DS1211)
    problemObserver.addListener(chooseContent);
    initSubtabs();
    initContent();
}

function enableThresholdButton() {
    $('slope-threshold-submit').disabled = false;
}

/**
 * Takes an array that consists of
 * TYPE (type of menu to display),
 * Selected (the item selected),
 * The menu options.
 * Parses this list and opens a new pop up menu.
 */
function lcDisplayTypeMenu(itemArray) {
    actualArray = new Array();
    //the list does not include the first two items.
    for (i = 2; i < itemArray.length; i++) {
        actualArray[i-2] = itemArray[i];
    }
    new PopupMenu("Learning Curve Types", "LearningCurveTypes", actualArray, mouse_x, mouse_y,
        parseSelectedType, itemArray[1]);
}

/**
 * Takes the learning curve type selected and passes the correct
 * value into selectType() to draw the learning curve.
 */
function parseSelectedType(optionSelect) {
    // read the selected option and format to pass into selectType()
    var ERROR_RATE = "Error Rate";
    var ASSISTANCE_SCORE = "Assistance Score";
    var NUM_INCORRECTS = "Number of Incorrects";
    var NUM_HINTS = "Number of Hints";
    var STEP_DURATION = "Step Duration";
    var CORRECT_STEP_DURATION = "Correct Step Duration";
    var ERROR_STEP_DURATION = "Error Step Duration";

    if (optionSelect == ASSISTANCE_SCORE) { selectType(ASSISTANCE_SCORE_TYPE); }
    else if (optionSelect == ERROR_RATE) { selectType(ERROR_RATE_TYPE); }
    else if (optionSelect == NUM_INCORRECTS) {selectType(AVG_INCORRECTS_TYPE); }
    else if (optionSelect == NUM_HINTS) { selectType(AVG_HINTS_TYPE); }
    else if (optionSelect == STEP_DURATION) { selectType(STEP_DURATION_TYPE); }
    else if (optionSelect == CORRECT_STEP_DURATION) { selectType(CORRECT_STEP_DURATION_TYPE); }
    else if (optionSelect == ERROR_STEP_DURATION) { selectType(ERROR_STEP_DURATION_TYPE); }
}

function initializeCategoryTooltips() {
    // Initialize category heading tooltips
    var options1 = new Array();
    options1['extraClasses'] = 'lcCategoryTooltips';
    options1['timeout'] = '100000';
    options1['delay'] = '100';

    lowAndFlatTooltip = $('perCategoryThumbsTag_Low_and_flat');
    if (lowAndFlatTooltip) {
        lowAndFlatTooltipContent = $('lowAndFlatTooltipContent').innerHTML;
        new ToolTip("perCategoryThumbsImg_Low_and_flat", lowAndFlatTooltipContent, options1);
    }
    noLearningTooltip = $('perCategoryThumbsTag_No_learning');
    if (noLearningTooltip) {
      noLearningTooltipContent = $('noLearningTooltipContent').innerHTML;
        new ToolTip("perCategoryThumbsImg_No_learning", noLearningTooltipContent, options1);
    }

    stillHighTooltip = $('perCategoryThumbsTag_Still_high');
    if (stillHighTooltip) {
      stillHighTooltipContent = $('stillHighTooltipContent').innerHTML;
        new ToolTip("perCategoryThumbsImg_Still_high", stillHighTooltipContent, options1);
    }

    tooLittleDataTooltip = $('perCategoryThumbsTag_Too_little_data');
    if (tooLittleDataTooltip) {
      tooLittleDataTooltipContent = $('tooLittleDataTooltipContent').innerHTML;
        new ToolTip("perCategoryThumbsImg_Too_little_data", tooLittleDataTooltipContent, options1);
    }

    otherTooltip = $('perCategoryThumbsTag_Other');
    if (otherTooltip) {
      otherTooltipContent = $('otherTooltipContent').innerHTML;
        new ToolTip("perCategoryThumbsImg_Other", otherTooltipContent, options1);
    }
}

onloadObserver.addListener(initLearningCurve);
