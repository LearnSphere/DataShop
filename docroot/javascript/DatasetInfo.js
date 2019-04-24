//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2006
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 13159 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2016-04-21 09:07:47 -0400 (Thu, 21 Apr 2016) $
// $KeyWordsOff: $
//

//
// Add an onload listener to initialize everything for this report.
//
onloadObserver.addListener(initDatasetInfo);

//
// Initialize the default navigation and content.
//
function initDatasetInfo() {

    //find out what kind of content we should be looking at.
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.initDatasetInfo",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: "initialize"
        },
        onComplete: determineContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

// The subtabs are retained here from the actual page content.
// The strings step_list and modelInfo are div IDs inside the JSPs
// which are just the content portion of the page and not full pages.  These JSPs are
// 1) step_list.jsp
// 2) kc_models.jsp
// 3) dataset_info_citation.jsp
// 4) dataset_info_overview.jsp
function determineContent(transport) {
    if (transport.responseText.indexOf("step_list") > 0) {
        displayStepList(transport);
    } else if (transport.responseText.indexOf("modelInfo") > 0) {
        displayKCModelContent(transport);
    } else if (transport.responseText.indexOf("customFields") > 0) {
        displayCustomFieldContent(transport);
    } else if (transport.responseText.indexOf("citationInfo") > 0) {
        displayAcknowledgmentAndCitation(transport);
    } else if (transport.responseText.indexOf("termsOfUse") > 0) {
        displayDatasetInfoTermsOfUse(transport);
    } else if (transport.responseText.indexOf("problemList") > 0) {
        displayProblemList(transport);
    } else if (transport.responseText.indexOf("problemContent") > 0) {
        displayProblemContent(transport);
    } else if (transport.responseText.indexOf("samples-page") > 0) {
        displaySamplesContent(transport);
        initSamplesPageContent();
    } else {
        displayOverview(transport);
    }
}

function initSubtab(contentType) {
    var subtabDiv = $("subtab");

    var userLoggedIn = ($('userLoggedIn').value == "false") ? false : true;
    var userAuthorized = ($('userAuthorized').value == "false") ? false : true;
    var hasData = ($('numTransactions').value > 0) ? true : false;
    var isRemote = ($('isRemote').value == "false") ? false : true;

    if (!subtabDiv.firstChild) {
        subtabDiv.insert('<a id="overview_subtab_link">Overview</a>');


        // Users must be logged in and authorized to access these subtabs.
        if (userLoggedIn && userAuthorized && hasData) {
            subtabDiv.insert('<a id="samples_subtab_link">Samples</a>');
            subtabDiv.insert('<a id="models_subtab_link">KC Models</a>');
            subtabDiv.insert('<a id="custom_fields_subtab_link">Custom Fields</a>');
            subtabDiv.insert('<a id="problem_list_subtab_link">Problem List</a>');
            subtabDiv.insert('<a id="step_list_subtab_link">Step List</a>');
            subtabDiv.insert('<a id="citation_subtab_link">Citation</a>');
            $('samples_subtab_link').observe('click', selectSamples);
            $('models_subtab_link').observe('click', selectKCModel);
            $('custom_fields_subtab_link').observe('click', selectCustomFields);
            $('problem_list_subtab_link').observe('click', selectProblemList);
            $('step_list_subtab_link').observe('click', selectStepList);
            $('citation_subtab_link').observe('click', selectCitation);
        } else {
            var titleStr = "";
            if (!userLoggedIn) {
                titleStr = "You must be logged in to use this feature.";
            } else {
                if (isRemote) {
                    titleStr = "This dataset can be found on a remote DataShop instance.";
                } else if (!hasData) {
                    titleStr = "There is no transaction data for this dataset.";
                }
                if (!userAuthorized) {
                    titleStr = "You do not have permission to access this dataset.";
                }
            }
            // Disabled subtab links
            subtabDiv.insert('<span id="samples_subtab_link" class="disabledItem" title=\"'
                    + titleStr + '\">Samples</span>');
            subtabDiv.insert('<span id="models_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">KC Models</span>');
            subtabDiv.insert('<span id="custom_fields_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">Custom Fields</span>');
            subtabDiv.insert('<span id="problem_list_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">Problem List</span>');
            subtabDiv.insert('<span id="step_list_subtab_link" class="disabledItem" title=\"'
                             + titleStr + '\">Step List</span>');

            // Citation based solely on whether or not user is logged in.
            if (userLoggedIn) {
                subtabDiv.insert('<a id="citation_subtab_link">Citation</a>');
                $('citation_subtab_link').observe('click', selectCitation);
            } else {
                subtabDiv.insert('<span id="citation_subtab_link" class="disabledItem" title=\"'
                                 + titleStr + '\">Citation</span>');
            }
        }
        subtabDiv.insert('<a id="terms_subtab_link">Terms of Use</a>');

        // Problem Content subtab: DAs and PAs only.
        var projectAdminFlag = ($('projectAdminFlag').value == "false") ? false : true;
        var adminUserFlag = ($('adminUserFlag').value == "false") ? false : true;
        if (userLoggedIn && (projectAdminFlag || adminUserFlag)) {
            if (isRemote) {
                var titleStr = "This dataset can be found on a remote DataShop instance.";
                subtabDiv.insert('<span id="problem_content_subtab_link" class="disabledItem" '
                                 + 'title=\"' + titleStr + '\">Problem Content</span>');
            } else if (!hasData) {
                var titleStr = "There is no transaction data for this dataset.";
                subtabDiv.insert('<span id="problem_content_subtab_link" class="disabledItem" '
                                 + 'title=\"' + titleStr + '\">Problem Content</span>');
            } else {
                subtabDiv.insert('<a id="problem_content_subtab_link">Problem Content</a>');
                $('problem_content_subtab_link').observe('click', selectProblemContent);
            }
        }

        $('overview_subtab_link').observe('click', selectOverview);
        $('terms_subtab_link').observe('click', selectTerms);
    }
    selectSubtab(contentType);
}

function selectSubtab(selectedLink) {
    $("overview_subtab_link",
      "samples_subtab_link",
      "models_subtab_link",
      "custom_fields_subtab_link",
      "problem_list_subtab_link",
      "step_list_subtab_link",
      "citation_subtab_link",
      "terms_subtab_link").each(
          function(link) { link.removeClassName("selected"); }
      );

    // Problem Content subtab isn't always present.
    if ($("problem_content_subtab_link")) {
        $("problem_content_subtab_link").removeClassName("selected");
    }

    if (selectedLink == "samples") {
        $("samples_subtab_link").addClassName("selected");
    } else if (selectedLink == "models") {
        $("models_subtab_link").addClassName("selected");
    }  else if (selectedLink == "customFields") {
        $("custom_fields_subtab_link").addClassName("selected");
    }  else if (selectedLink == "problemList") {
        $("problem_list_subtab_link").addClassName("selected");
    } else if (selectedLink == "stepList") {
        $("step_list_subtab_link").addClassName("selected");
    }  else if (selectedLink == "citation") {
        $("citation_subtab_link").addClassName("selected");
    }  else if (selectedLink == "terms") {
        $("terms_subtab_link").addClassName("selected");
    }  else if (selectedLink == "problemContent") {
        $("problem_content_subtab_link").addClassName("selected");
    }  else { // default overview
        $("overview_subtab_link").addClassName("selected");
    }
}

function selectOverview() {
    selectSubtab("overview");
    requestOverview();
}

function selectSamples() {
    selectSubtab("samples");
    requestSamples();
}

function selectKCModel() {
    selectSubtab("models");
    requestKCModels();
}

function selectCustomFields() {
    selectSubtab("customFields");
    requestCustomFields();
}

function selectProblemList() {
    selectSubtab("problemList");
    requestProblemList();
}

function selectStepList() {
    selectSubtab("stepList");
    requestStepList();
}

function selectCitation() {
    selectSubtab("citation");
    requestCitation();
}

function selectTerms() {
    selectSubtab("terms");
    requestTerms();
}

function selectProblemContent() {
    selectSubtab("problemContent");
    requestProblemContent();
}

/** AJAX request to get the overview content from server. */
function requestOverview() {
    var contentType = "overview";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";
    // reset constant to make sure redo/undo button does not show
    // when entering the overview page
    CITATION_UNDO_STATE = false;
    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestOverview",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayOverview,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the overview content. */
function displayOverview(transport) {

    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("overview");
    toggleHypothesis();
    initEditableFields();
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-overview"));
    //Make the request button work
    jQuery('.request_link').click(createRequest);
    //For QA testing
    loadedForQA("true");
}

/**
 * Create truncator objects for fields that should be expandable.  Only call this method
 * if the user does not have edit and admin access (truncator is embedded in the InlineEditor object).
 */
function initTruncatorFields() {
   // hypothesis, description, acknowledgment and additional notes get a Truncator object.
   // set the caller to null so that Event.stop() is not called (only needed when used in
   // conjunction with the InlineEditor)

   options = new Array();
   options.numLines = 7;
   if ($("hypothesis")) {
      new Truncator($('hypothesis'), options);
   }
   if ($("acknowledgment")) {
      new Truncator($('acknowledgment'), options);
   }
   if ($("datasetDescription")) {
      new Truncator($('datasetDescription'), options);
   }
   if ($("notes")) {
      new Truncator($('notes'), options);
   }
   if ($("datasetName")) {
      new Truncator($('datasetName'));
   }
   if ($("project")) {
      new Truncator($('project'));
   }
   if ($("school")) {
      new Truncator($('school'));
   }
}

/**
 * Process a request for LCPI Details.
 * @param measure the measure for details retrieval.
 * @param sortBy how the details should be sorted.
 * @param direction the sort direction.
 * Note: this method can be called without any params (which means we are relying on
 *    values stored in the context or defaults).
 */
function domainLearnlabSetRequest() {

    new Ajax.Request("LCPID", {
            parameters: {
                      requestingMethod: "DatasetInfo.domainLearnlabSetRequest",

                  type: "domain_learnlab_set"

                  },
        requestHeaders: { Accept: 'application/json;charset=UTF-8' },
        onSuccess: function(transport) {
            response = transport.responseText.evalJSON();
            domainLearnlabSetCallback(response);
           },
        onException: function(request, exception) { throw(exception); }
    });
} // end pointInfoDetailsRequest

/**
 * Initialize the editable text field with an inline editor.
 */
function initEditableFields() {
    //check that the "hasEditRights" element exists.

    var isRemote = ($('isRemote').value == "false") ? false : true;

    if (isRemote || (!$("hasEditRights") && !$("hasAdminRights"))) {
        initTruncatorFields();
        return;
    }

    truncatorOptionsArray = new Array();
    truncatorOptionsArray.maxLength = 75;
    truncatorOptionsArray.numLines = 4;

    //init the editable text fields

    var options = new Array();
    options.maxLength = 65000;
    options.truncatorOptions = truncatorOptionsArray;
    if ($("notes")) {
        new InlineEditor($('notes'), 'textarea', 'DatasetInfoEdit', options);
    }

    if ($("datasetDescription")) {
        new InlineEditor($('datasetDescription'), 'textarea', 'DatasetInfoEdit', options);
    }


    if ($("hypothesis")) {
        new InlineEditor($('hypothesis'), 'textarea', 'DatasetInfoEdit', options);
    }

    options = new Array();
    options.maxLength = 255;
    options.truncatorOptions = truncatorOptionsArray;
    if ($("acknowledgment")) {
        new InlineEditor($('acknowledgment'), 'textarea', 'DatasetInfoEdit', options);
    }

    var suggestions = new Array();
    suggestions[0] = { "text":'', "value":'' };
    suggestions[1] = { "text":'on-going', "value":'on-going' };
    suggestions[2] = { "text":'complete', "value":'complete' };
    suggestions[3] = { "text":'files-only', "value":'files-only' };

    var options = new Array();
    options.suggestions = suggestions.toJSON();
    options.maxLength = 20;
    options.allowOther = true;

    //FIXME - ben needs to fix event handling to get this working
    //var toolTipOptions = new Array();
    //toolTipOptions.extraClasses = "inlineToolTip";

    if ($("datasetStatus")) {
            new InlineEditor($('datasetStatus'), null,  'DatasetInfoEdit', options);
    }

    //preferred citation is a little to- custom, so do it w/o the inline editor.
    makeCitationEditable();

    if ($("tutor")) {
            new InlineEditor($('tutor'), null,'DatasetInfoEdit', { maxLength: 50 });
    }

    if ($("domain_learnlab")) {
        var options = new Array();
        options.suggestionParams = {
                requestingMethod: "DatasetInfo.initEditableFields.domain_learnlab",
                datasetId: dataset,
            ajaxRequest: "true",
            getDomainLearnlabList: "true" };
        options.suggestions = suggestions.toJSON();
        options.allowOther = false;
        new InlineEditor($('domain_learnlab'), null, 'DatasetInfoEdit', options);
    }


    if ($("curriculum")) {
        var options = new Array();
        options.suggestionParams = {
                requestingMethod: "DatasetInfo.initEditableFields.curriculum",
                datasetId: dataset,
            ajaxRequest: "true",
            getCurriculumList: "true" };
        options.maxLength = 60;
        options.allowOther = true;
        options.allowOtherTxt = "Create New";
        new InlineEditor($('curriculum'), null, 'DatasetInfoEdit', options);
    }

    suggestions = new Array();
    suggestions[0] = { "text":'Not Specified', "value":'Not Specified' };
    suggestions[1] = { "text":'Yes', "value":'Yes' };
    suggestions[2] = { "text":'No', "value":'No' };

    options = new Array();
    options.suggestions = suggestions.toJSON();
    options.allowOther = false;
    options.onSuccess = toggleHypothesis;
    if ($("studyFlag")) {
            new InlineEditor($('studyFlag'), null, 'DatasetInfoEdit', options);
    }

    options = new Array();
    options.maxLength = 255;
    if ($("school")) {
        new InlineEditor($('school'), null, 'DatasetInfoEdit', options);
    }

    //date is a little to- custom, so do it w/o the inline editor.
    makeDateEditable();

    //ADMIN Only edits from this point on.
    if (!$("hasAdminRights")) { return; }

    //truncatorOptionsArray.numLines = 3;
    if ($("datasetName")) {
        options = {
            truncatorOptions: truncatorOptionsArray,
            onSuccess: handleDatasetInfoOverviewChange,
            maxLength: 100
        };
        new InlineEditor($('datasetName'), null, 'DatasetInfoEdit',  options);
    }

    if ($("project")) {
        var options = new Array();
        options.suggestionParams = {
            requestingMethod: "DatasetInfo.initEditableFields.project",
            datasetId: dataset,
            ajaxRequest: "true",
            getProjectList: "true" };
        options.maxLength = 255;
        options.allowOther = true;
        options.allowOtherTxt = "Create New";
        options.truncatorOptions = truncatorOptionsArray;
        options.onSuccess = handleDatasetInfoOverviewChange;
        new InlineEditor($('project'), null, 'DatasetInfoEdit', options);
    }

}

function handleDatasetInfoOverviewChange() {
    // Reload page to update fields.
    window.location.assign(window.location.href);
}

function toggleHypothesis() {
    var studyFlagCell = $("studyFlag");
    if (!studyFlagCell) { return; }

    var value = "";
    if (studyFlagCell.firstChild) {
            value = studyFlagCell.firstChild.textContent;
            if (!value) {
                value = studyFlagCell.innerText;
            }
            if (!value) { value = ''; }
    }

    // use startsWith because IE6 sees value as 'yesundo' or 'noundo'
    if (value == "Yes" || value.startsWith("Yes"))  {
        $("hypothesisRow").show();
    } else {
        $("hypothesisRow").hide();
    }
}

function makeTruncator(span, name) {
    new Truncator(span, { maxLength: 100, numLines: 1 });
}

function truncateElementsWithName(name) {
    $('main_content_div').select('span[name='+name+']').each(function(span) {
        makeTruncator(span, name);
    });
}

/** AJAX request to get the samples page content. */
function requestSamples() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "samples";
    var contentDiv = $('main_content_div');
    var sortBy = "";
    if (this.id != undefined) {
        sortBy = this.id;
        //alert(sortBy);
    }

    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestSamples",
            datasetId: dataset,
            sortBy: sortBy,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType,
            subtab: "samples"
        },
        onComplete: displaySamplesContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX request to get the kc models content from server. */
function requestKCModels() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "models";
    var contentDiv = $('main_content_div');
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

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestKCModels",
            datasetId: dataset,
            kcmSortBy: kcmSortByValue,
            kcmSortAscending: kcmSortAscendingValue,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayKCModelContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the overview content. */
function displayKCModelContent(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("models");
    initModelControls();
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-kc-models"));
    loadedForQA("true");
}

/** AJAX request to get the custom fields content from server. */
function requestCustomFields() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "customFields";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestCustomFields",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayCustomFieldContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the custom field content. */
function displayCustomFieldContent(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("customFields");
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-custom-fields"));
    loadedForQA("true");
}

/** AJAX response to display the samples page content. */
function displaySamplesContent(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("samples");
    jQuery('.sortByColumn').click(requestSamples);
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-samples"));

    loadedForQA("true");
}

/** AJAX request to get the problem list content from server. */
function requestProblemList() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "problemList";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestProblemList",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayProblemList,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the problem list content. */
function displayProblemList(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    // Need to run 'ProblemContent.js' now that the
    // problem_list.jsp page has been loaded.
    var tag = document.createElement("script");
    tag.src = "javascript/ProblemContent.js";
    document.head.appendChild(tag);

    initSubtab("problemList");
    if ($('problem_list')) {
        var exportAllowed = "true";
        if ($('datasetExportAllowed')) {
            exportAllowed = $('datasetExportAllowed').value;
        }
        options = new Array();
        options['exportCall'] = startExport;
        options['exportAllowed'] = exportAllowed;
    }

    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-problem-list"));
    //For QA testing
    loadedForQA("true");
}

/** AJAX request to get the step list content from server. */
function requestStepList() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "stepList";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestStepList",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayStepList,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the overview content. */
function displayStepList(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    initSubtab("stepList");
    if ($('step_list')) {
        var exportAllowed = "true";
        if ($('datasetExportAllowed')) {
            exportAllowed = $('datasetExportAllowed').value;
        }
        options = new Array();
        options['exportCall'] = startExport;
        options['exportAllowed'] = exportAllowed;
        pageGrid = new PageGrid('step_list', 'Step List', 'DatasetInfo', options);
    }

    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-step-list"));
    //For QA testing
    loadedForQA("true");
}

/** AJAX request to get the citation content from server. */
function requestCitation() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "citation";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestCitation",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayAcknowledgmentAndCitation,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the citation content. */
function displayAcknowledgmentAndCitation(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("citation");
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-citation"));
    loadedForQA("true");
}

/** AJAX request to get the terms of use content from server. */
function requestTerms() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "terms";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestTerms",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayDatasetInfoTermsOfUse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the terms of use content. */
function displayDatasetInfoTermsOfUse(transport) {
        var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";
    initSubtab("terms");
    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-terms"));
    loadedForQA("true");
}

/** AJAX request to get the 'problem content' content from server. */
function requestProblemContent() {
    loadedForQA("false");
    jQuery('div.ui-dialog-content').dialog('close');
    var contentType = "problemContent";
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = "<h1>Loading... </h1>";
    document.body.style.cursor = "wait";

    new Ajax.Request(window.location.href, {
        parameters: {
            requestingMethod: "DatasetInfo.requestProblemContent",
            datasetId: dataset,
            ajaxRequest: "true",
            ds_request: "content",
            ds_content: contentType
        },
        onComplete: displayProblemContent,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

/** AJAX response to display the 'problem content' content. */
function displayProblemContent(transport) {
    var contentDiv = $('main_content_div');
    contentDiv.innerHTML = transport.responseText;
    document.body.style.cursor = "default";

    // Need to run 'ProblemContent.js' now that the
    // problem_content.jsp page has been loaded.
    var tag = document.createElement("script");
    tag.src = "javascript/ProblemContent.js";
    document.head.appendChild(tag);

    initSubtab("problemContent");

    //Update the report-level contextual help content
    theHelpWindow.updateContent($("help-dataset-info-problem-content"));
    //For QA testing
    loadedForQA("true");
}

//
// Export functions
//
var progressBar = false;
function startExport() {
    if (!progressBar) {
        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "DatasetInfo.startExport",
                datasetId: dataset,
                export_start: "true" },
            onComplete: displayProgressBar,
            onException: function (request, exception) { throw(exception); }
        });
    } else {
        if (progressBar != null) {
            progressBar.cancelExport();
            progressBar.closeAll();
        }
        progressBar = false;
        startExport();
    }
}

function displayProgressBar() {
    if (!progressBar) { progressBar = new ProgressBar(getFinalProduct, window.location.href); }
}

function getFinalProduct() {
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'export_form');
    newForm.setAttribute('id', 'export_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', window.location.href);
    newForm.setAttribute('method', 'POST');

    var newRequestType = document.createElement('input');
    newRequestType.name="get_export_file";
    newRequestType.type="hidden";
    newRequestType.value="true";
    newForm.appendChild(newRequestType);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function replaceContents(containerid, elt) {
    if ($(containerid)) {
        $(containerid).update();
        $(containerid).insert(elt);
    }
}

// Change the hidden field value given an element id
function changeHiddenValue(id, input) {
    var hidden = $(id);
    if (hidden != null){
        hidden.value = input;
    }
}

// Create a hidden input element with value
function hiddenWithValue(value) {
    hidden = document.createElement("input");
    hidden.setAttribute("type","hidden");
    hidden.setAttribute("value", value);
    return hidden;
}


/*------------------------------------------------------------------
 * All functions for inline edits of the date fields.
 *----------------------------------------------------------------*/
var dateMouseOverHandler = null;
var dateMouseOutHandler = null;
function makeDateEditable() {
    //find out if this dataset has transaction.
    var date = $("dates");

    // Add edit icon...
    date.insert({'bottom' : '<img src="images/pencil.png" title="Click to edit" '
                + ' id="'+ date.id + '_edit" class="edit_icon" >'});

    dateMouseOverHandler = dateMouseOver.bindAsEventListener(date);
    dateMouseOutHandler = dateMouseOut.bindAsEventListener(date);

    Event.observe(date, 'mouseover', dateMouseOverHandler);
    Event.observe(date, 'mouseout', dateMouseOutHandler);
    Event.observe(date, 'click', openDateForEdit);

    if (DATE_UNDO_STATE) {
        date.insert({'bottom' : '<span title="click to undo last edit" id="date_edit_undo" '
                    + 'class="' + DATE_UNDO_CLASS + '">' + DATE_UNDO_STATE + '</span>'});
    }

    //enable the event handlers for the undo button if it exists.
    if ($("date_edit_undo")) {
        Event.observe("date_edit_undo", 'mouseover', dateUndoMouseoverHandler);
        Event.observe("date_edit_undo", 'mouseout', dateUndoMouseoutHandler);
        Event.observe("date_edit_undo", 'click', dateUndoClickHandler);
    }
}

function dateMouseOver() { this.style.backgroundColor = "#EFEFEF"; }
function dateMouseOut() { this.style.backgroundColor = "white"; }

var START_DATE = false;
var END_DATE = false;
var PREV_START_DATE = false;
var PREV_END_DATE = false;
var DATE_UNDO_STATE = false;
var DATE_UNDO_CLASS = "";

function openDateForEdit(event) {
    Event.stop(event);
    if ($("date_edit_undo")) { $("date_edit_undo").remove(); }
    if ($("dates_edit")) { $("dates_edit").remove(); }

    var dateCell = $("dates")
    Event.stopObserving(dateCell, 'mouseover', dateMouseOverHandler);
    Event.stopObserving(dateCell, 'mouseout', dateMouseOutHandler);
    Event.stopObserving(dateCell, 'click', openDateForEdit);
    dateCell.style.backgroundColor = "#EFEFEF";

    var dateSpan = $("date_span");
    dateSpan.hide();

    var startDate = $("start_date");
    if (!START_DATE) { START_DATE = startDate.value; }
    startDate.show();

    var endDate = $("end_date");
    if (!END_DATE) { END_DATE = endDate.value; }
    endDate.show();

    new Insertion.Before(startDate, "<label id=\"start_date_label\">Start Date</label>");

    var calendarImage = "<img id=\"start_date_calendar_trigger\" />";
    var clearImage = "<img id=\"start_date_clear_trigger\" />";

    new Insertion.After(startDate, clearImage);
    new Insertion.After(startDate, calendarImage);

    $('start_date_calendar_trigger').src = "images/website.gif";
    $('start_date_calendar_trigger').alt = "Choose Start Date";
    $('start_date_calendar_trigger').title = "Choose Start Date";
    $('start_date_calendar_trigger').onmouseover = function () {
                                        this.src = "images/website_blue.gif";
                                        this.style.cursor = "pointer";
                                    }
    $('start_date_calendar_trigger').onmouseout = function () {
                                        this.src = "images/website.gif";
                                        this.style.cursor = "default";
                                    }

    $('start_date_clear_trigger').src = "images/delete_file_grey.gif";
    $('start_date_clear_trigger').alt = "Clear Start Date";
    $('start_date_clear_trigger').title = "Clear Start Date";
    $('start_date_clear_trigger').onclick = function () { $("start_date").value = ""; }
    $('start_date_clear_trigger').onmouseover = function () {
                                    this.src = "images/delete_file.gif";
                                    this.style.cursor = "pointer";
                                }
    $('start_date_clear_trigger').onmouseout = function () {
                                    this.src = "images/delete_file_grey.gif";
                                    this.style.cursor = "default";
                                }

    //create a break and do the end date the same way now.
    dateCell.insertBefore(document.createElement("br"),endDate);

    calendarImage = "<img id=\"end_date_calendar_trigger\" />";
    clearImage = "<img id=\"end_date_clear_trigger\" />";

    new Insertion.After(endDate, clearImage);
    new Insertion.After(endDate, calendarImage);

    $('end_date_calendar_trigger').src = "images/website.gif";
    $('end_date_calendar_trigger').alt = "Choose End Date";
    $('end_date_calendar_trigger').title = "Choose End Date";
    $('end_date_calendar_trigger').onmouseover = function () {
                                        this.src = "images/website_blue.gif";
                                        this.style.cursor = "pointer";
                                    }
    $('end_date_calendar_trigger').onmouseout = function () {
                                        this.src = "images/website.gif";
                                        this.style.cursor = "default"
                                    }

    $('end_date_clear_trigger').src = "images/delete_file_grey.gif";
    $('end_date_clear_trigger').alt = "Clear End Date";
    $('end_date_clear_trigger').title = "Clear End Date";
    $('end_date_clear_trigger').onclick = function () { $("end_date").value = ""; }
    $('end_date_clear_trigger').onmouseover = function () {
                                    this.src = "images/delete_file.gif";
                                    this.style.cursor = "pointer";
                                }
    $('end_date_clear_trigger').onmouseout = function () {
                                    this.src = "images/delete_file_grey.gif";
                                    this.style.cursor = "default"
                                }

    new Insertion.Before(endDate, "<label id=\"end_date_label\">End Date</label>");

    //init the calendar objects for both fields.
    new Calendar.setup({
        inputField     : "start_date",
        button         : "start_date_calendar_trigger",
        firstDay       : 0,
        showsTime      : true,
        step           : 1,
        electric       : true,
        align          : "TR",
        singleClick    : false,
        ifFormat       : "%Y-%m-%d %H:%M:%S",
        daFormat       : "%Y/%m/%d"
    });

    new Calendar.setup({
        inputField     : "end_date",
        button         : "end_date_calendar_trigger",
        firstDay       : 0,
        showsTime      : true,
        step           : 1,
        electric       : true,
        align          : "TR",
        singleClick    : false,
        ifFormat       : "%Y-%m-%d %H:%M:%S",
        daFormat       : "%Y/%m/%d"
    });

    //one more break and our buttons!
    new Insertion.Bottom(dateCell, "<br />");
    new Insertion.Bottom(dateCell,
        "<input id='dateAutoSetButton' type='button' value='auto-set' "
        + "title='Click here to have the dates automatically set from the study data.' />");
    new Insertion.Bottom(dateCell,
        "<input id='dateSaveButton' type='button' value='save' name=\"saveButton\" />");
    new Insertion.Bottom(dateCell,
        "<input id='dateCancelButton' type='button' value='cancel' name=\"cancelButton\" />");

    $("dateSaveButton").onclick = function () { return false; }
    $("dateCancelButton").onclick = function () { return false; }
    $("dateAutoSetButton").onclick = function () { return false; }
    Event.observe("dateSaveButton", 'click', saveDateEdit);
    Event.observe("dateCancelButton", 'click', cancelDateEdit);
    Event.observe("dateAutoSetButton", 'click', autosetDates);
}

function saveDateEdit() {
    DATE_UNDO_STATE = false;
    new Ajax.Request("DatasetInfoEdit", {
        parameters: {
            requestingMethod: "DatasetInfo.saveDateEdit",
            datasetId: dataset,
            startDate: $F("start_date"),
            endDate: $F("end_date")
        },
        onComplete: saveDateEditListener,
        onException: function (request, exception) { throw(exception); }
    });
}

function autosetDates() {
    messagePopup('Getting Dates from transaction data.  This may take a few minutes'
                       + ' depending on the size of the dataset. A popup message will appear'
                       + ' when it completes.', "MESSAGE");

    DATE_UNDO_STATE = false;
    new Ajax.Request("DatasetInfoEdit", {
        parameters: {
            requestingMethod: "DatasetInfo.autosetDates",
            datasetId: dataset,
            autosetDates: "true" },
        onComplete: saveDateEditListener,
        onException: function (request, exception) { throw(exception); }
    });
}

/**
 * This funciton catches the results of the ajax save call, and redirects to the proper results.
 */
function saveDateEditListener(transport) {

    var response = transport.responseText;
    var data = response.evalJSON();

    var messageType = data.messageType;
    var message = data.message;
    var value = data.value;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
        errorPopup(message);
    } else if (messageType == 'SUCCESS') {
        // toggle undo/redo
        if (DATE_UNDO_STATE == "undo") {
            DATE_UNDO_STATE = "redo";
            DATE_UNDO_CLASS = "redo_button";
        } else {
            DATE_UNDO_STATE = "undo";
            DATE_UNDO_CLASS = "undo_button";
        }
        $("date_span").remove();
        if (data.value == null || data.value == '' ){
            value = '&nbsp;';
        }
        new Insertion.Top("dates", "<span id=\"date_span\">" + value + "</span>");
        $("end_date").value = data.endDate;
        $("start_date").value = data.startDate;

        PREV_START_DATE = START_DATE;
        PREV_END_DATE = END_DATE;
        START_DATE = data.startDate;
        END_DATE = data.endDate

        closeDateEdit();
        successPopup(message);
    } else {
        errorPopup("Unexpected error from the server. "
        + "Please try again and contact the DataShop team if the errors persists.");
    }
}

function cancelDateEdit(event) {
    var startDate = $("start_date");
    var endDate = $("end_date");
    startDate.value = START_DATE;
    endDate.value = END_DATE;
    Event.stop(event);
    closeDateEdit();
}

function closeDateEdit() {
    var dateCell = $("dates")
    $(dateCell).immediateDescendants().each(function (item) {
        if (item.id != "date_span"
         && item.id != "start_date"
         && item.id != "end_date")
        item.parentNode.removeChild(item);
    });

    $("date_span").show();
    $("start_date").hide();
    $("end_date").hide();

    makeDateEditable();
    dateCell.style.backgroundColor = "white";
}

function undoLastDateEdit() {
    if (PREV_END_DATE !== false && PREV_START_DATE !== false) {
        new Ajax.Request("DatasetInfoEdit", {
            parameters: {
                requestingMethod: "DatasetInfo.undoLastDateEdit",
                datasetId: dataset,
                startDate: PREV_START_DATE,
                endDate: PREV_END_DATE
            },
            onComplete: saveDateEditListener,
            onException: function (request, exception) { throw(exception); }
        });
    } else {
       errorPopup("Last edit unavailible, unable to undo.");
       if ($("date_edit_undo")) { $("date_edit_undo").remove(); }
    }
}

function dateUndoMouseoverHandler (event) {
    $(this).addClassName('hover');
    Event.stop(event);
}

function dateUndoMouseoutHandler (event) {
    $(this).removeClassName('hover');
    Event.stop(event);
}

function dateUndoClickHandler (event) {
    undoLastDateEdit();
    Event.stop(event);
}

/*------------------------------------------------------------------
* All functions for inline edits of the preferred paper citation field.
*----------------------------------------------------------------*/
var FILE_ID = false;
var CITATION = false;
var PREV_FILE_ID = false;
var PREV_CITATION = false;
var CITATION_UNDO_STATE = false;
var CITATION_UNDO_CLASS = "";

function openPreferredCitationForEdit(event) {
    CITATION_UNDO_STATE = false;

    Event.stop(event);

    if ($("paper_edit_undo")) { $("paper_edit_undo").remove(); }
        if ($("preferredPaperCitation_edit")) { $("preferredPaperCitation_edit").remove(); }

    var preferredCitationCell = $("preferredPaperCitation");
    if (preferredCitationCell != null){
        Event.stopObserving(preferredCitationCell, 'mouseover', citationMouseOverHandler);
        Event.stopObserving(preferredCitationCell, 'mouseout', citationMouseOutHandler);
        Event.stopObserving(preferredCitationCell, 'click', openDateForEdit);
        preferredCitationCell.style.backgroundColor = "#EFEFEF";


        var fileSelect = $('file_name_size_select');

        if (fileSelect != null){
            //display sentence
            if (!$('paper_select_div')){
                 new Insertion.Top(preferredCitationCell,
                         "<div id=\"paper_select_div\">Select a paper attached to this dataset:</div>");
            } else {
                $('paper_select_div').show();
            }

            //display file drop down menu
            if (!FILE_ID) { FILE_ID = fileSelect.value; }
            fileSelect.style.display = "block";

            //display link to papers and files page
            if (!$('attach_a_paper')){
                new Insertion.After(fileSelect,
                        "<div id=\"attach_a_paper\">" +
                    "<a onClick=\"javascript:goToPapersPage(" + dataset + ");" +
                    "\">Add a new paper</a></div>");
            }

            //remove the edit link if exists
            var editCitationLink = $('edit_citation_link');
            if (editCitationLink != null){
                editCitationLink.parentNode.removeChild(editCitationLink);
            }

            var citationSpan = $('paper_citation_div');
            var citation = '';
            if (citationSpan){
                citation = citationSpan.innerHTML;
            }
            if (!CITATION)  {
                CITATION = citationSpan.innerHTML;
            }

            var citationTextArea = $('paper_citation');

            // if text area doesn't exist and user selects a file,
            // then remove the span with old value
            // and insert a new span with updated value
            if (citationTextArea == null && fileSelect.value != 0) {
                citationSpan.parentNode.removeChild(citationSpan);
                // if citation exists for the paper, show it with the edit link, otherwise show the blank span.
                if ($('attach_a_paper')){
                    new Insertion.After($('attach_a_paper'),  "<div id=\"paper_citation_div\">"
                        + nl2br(CITATION) + "</div>");
                }
                if ((citation != '') && ($('paper_citation_div'))) {
                    new Insertion.After($('paper_citation_div'),
                            "<div id=\"edit_citation_link\"><a onClick=\"javascript:makeCitationEditable();\">edit</a></div>");

                } else if ($('no_citation_span')){
                    new Insertion.After($('no_citation_span'),
                            "<div id=\"edit_citation_link\"><a onclick=\"makeCitationEditable();\">Add one here.</a></div>");

                }
            }

            var citationUndoLink = $('citation_edit_undo');
            if (citationUndoLink != null){
                citationUndoLink.parentNode.removeChild(citationUndoLink);
            }

            if (($('citationCancelButton') == null) && ($('citationSaveButton') == null)){
                new Insertion.Bottom(preferredCitationCell,
                    "<input id='citationSaveButton' type='button' value='save' name=\"saveButton\"/>");
                new Insertion.Bottom(preferredCitationCell,
                    "<input id='citationCancelButton' type='button' value='cancel' name=\"cancelButton\" />");

                $("citationSaveButton").onclick = function () { return false; }
                $("citationCancelButton").onclick = function () { return false; }
                Event.observe("citationSaveButton", 'click', saveCitationEdit);
                Event.observe("citationCancelButton", 'click', cancelCitationEdit);
            }
        }
    }
}

var citationMouseOverHandler = null;
var citationMouseOutHandler = null;
/* Function for editing the citation. */
function makeCitationEditable() {
    // add event listener to the cell
    var preferredCitationCell = $("preferredPaperCitation");

        // Add edit icon...
        preferredCitationCell.insert({'bottom' :
                    '<img src="images/pencil.png" title="Click to edit" '
                    + ' id="'+ preferredCitationCell.id
                    + '_edit" class="edit_icon" >'});

    citationMouseOverHandler = citationMouseOver.bindAsEventListener(preferredCitationCell);
    citationMouseOutHandler = citationMouseOut.bindAsEventListener(preferredCitationCell);

    Event.observe(preferredCitationCell, 'mouseover', citationMouseOverHandler);
    Event.observe(preferredCitationCell, 'mouseout', citationMouseOutHandler);
    Event.observe(preferredCitationCell, 'click', openPreferredCitationForEdit);

    // remove the span if exists
    var noCitationSpan = $('no_citation_span')
    if (noCitationSpan){
        noCitationSpan.parentNode.removeChild(noCitationSpan);
    }

    // change citation span to text area for edit
    var citationSpan = $('paper_citation_div');
    var citationTextArea = $('paper_citation');
    if ($('edit_citation_link')) {
        spanToTextArea(citationSpan, 'paper_citation', citationSpan.innerHTML, $('citationSaveButton'));
        $('edit_citation_link').hide();
        citationSpan.hide();
    }

    if (CITATION_UNDO_STATE) {
        preferredCitationCell.insert({'bottom' :
                    '<span title="click to undo last edit" id="citation_edit_undo" '
                    + 'class="' + CITATION_UNDO_CLASS + '">' + CITATION_UNDO_STATE + '</span>'});
    }

    var fileSelect =  $('file_name_size_select');
    if (!fileSelect){
        FILE_ID = fileSelect.value;
    }

    if ((!PREV_FILE_ID) || ((PREV_FILE_ID == 0) && (PREV_FILE_ID == FILE_ID))){
        PREV_FILE_ID = FILE_ID;
    }
    if ($("citation_edit_undo")) {
       Event.observe("citation_edit_undo", 'mouseover', citationUndoMouseoverHandler);
       Event.observe("citation_edit_undo", 'mouseout', citationUndoMouseoutHandler);
       Event.observe("citation_edit_undo", 'click', citationUndoClickHandler);
    }
}

function saveCitationEdit(){
    CITATION_UNDO_STATE = false;
    var citation = null;
    if ($('paper_citation') != null) {
        citation = $F("paper_citation");
    } else if ($('paper_citation_div') != null) {
        citation = $("paper_citation_div").innerHTML;

    }

    hiddenFileId = $('preferred_file_id');
    hiddenFileId.value = $F("file_name_size_select");

    hiddenText = $('preferred_citation_text');
    hiddenText.value = citation;

    new Ajax.Request("DatasetInfoEdit", {
        parameters: {
            requestingMethod: "DatasetInfo.saveCitationEdit",
            datasetId: dataset,
            fileId: $F("file_name_size_select"),
            citation: br2nl(citation)
        },
        onComplete: saveCitationEditListener,
        onException: function (request, exception) { throw(exception); }
    });
}

/**
 * This function catches the results of the ajax save call, and redirects to the proper results.
 */
saveCitationEditListener = function(transport) {
    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;
    var value = data.value;

    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
        PREV_FILE_ID = FILE_ID;
        PREV_CITATION = CITATION;
        CITATION = false;
        var citationSpan = $("paper_citation_div");
        if (citationSpan != null){
            citationSpan.parentNode.removeChild(citationSpan);
        }
        new Insertion.Top($('preferredPaperCitation'),
                          "<div id=\"paper_citation_div\">" +  nl2br(data.citation) + "</div>");
        errorPopup(message);
    } else if (messageType == 'SUCCESS') {
        // toggle undo/redo
        if (CITATION_UNDO_STATE == "undo") {
            CITATION_UNDO_STATE = "redo";
            CITATION_UNDO_CLASS = "redo_button";
        } else {
            CITATION_UNDO_STATE = "undo";
            CITATION_UNDO_CLASS = "undo_button";
        }

        var citationSpan = $("paper_citation_div");

        if (citationSpan != null){
            citationSpan.parentNode.removeChild(citationSpan);
        }

        if (data.value == null || data.value == '' ){
            value = '&nbsp;';
        }

        new Insertion.Top($('preferredPaperCitation'),
                          "<div id=\"paper_citation_div\">" +  nl2br(data.citation) + "</div>");

        PREV_FILE_ID = FILE_ID;
        FILE_ID = data.fileId;

        PREV_CITATION = CITATION;
        CITATION = data.citation;
        closeCitationEdit();
        successPopup(message);
    } else {
        errorPopup("Unexpected error from the server. "
        + "Please try again and contact the DataShop team if the errors persists.");
    }

}

function closeCitationEdit() {
    var citationCell = $("preferredPaperCitation")
    $(citationCell).immediateDescendants().each(function (item) {
        if (item.id != "paper_citation_div"
         && item.id != "file_name_size_select"
         && item.id != "paper_select_div"
         && item.id != "preferred_citation_text"
         && item.id != "preferred_file_id") {
         item.parentNode.removeChild(item);
        }
    });

    $("paper_citation_div").show();
    $("file_name_size_select").style.display = "none";
    $("paper_select_div").hide();

    makeCitationEditable();
    citationCell.style.backgroundColor = "white";
}

function cancelCitationEdit(event){
    fileId = $('file_name_size_select');
    citation = $('paper_citation_div');
    PREV_FILE_ID = $('preferred_file_id').value;
    PREV_CITATION = $('preferred_citation_text').value;
    CITATION = PREV_CITATION;
    if (PREV_CITATION != false){

         fileId.value = PREV_FILE_ID;

         citation.parentNode.removeChild(citation);
         new Insertion.After($('attach_a_paper'),  "<div id=\"paper_citation_div\">"
                    + nl2br(PREV_CITATION) + "</div>");

         citation = $('paper_citation_div');
    } else {
        citation.innerHTML = PREV_CITATION;
    }
    citation.show();
    Event.stop(event);
    closeCitationEdit();
}

function spanToTextArea(td, name, input, positionBeforeElement){
    var id = td.id.slice(0, td.id.length - '_div'.length);
    var txtArea = "<textarea name="+name+" id=" +id+ ">" + input.replace(/<BR>/g, "\r\n").replace(/<br>/g, "\r\n")  + "</textarea>"
    new Insertion.Before(positionBeforeElement, txtArea);
}

function citationMouseOver() { this.style.backgroundColor = "#EFEFEF"; }
function citationMouseOut() { this.style.backgroundColor = "white"; }

function citationUndoMouseoverHandler (event) {
    $(this).addClassName('hover');
    Event.stop(event);
}

function citationUndoMouseoutHandler (event) {
    $(this).removeClassName('hover');
    Event.stop(event);
}

function citationUndoClickHandler (event) {
    undoLastCitationEdit();
    Event.stop(event);
}

function undoLastCitationEdit() {
    if (PREV_FILE_ID !== false) {
        var citationDiv = $('paper_citation_div');
        citationDiv.parentNode.removeChild(citationDiv);
           new Insertion.Before($('paper_select_div'),  "<div id=\"paper_citation_div\">"
                   + nl2br(PREV_CITATION) + "</div>");
           if (PREV_FILE_ID == 0){
               if (PREV_CITATION == ""){
                   new Ajax.Request("DatasetInfoEdit", {
                    parameters: {
                        requestingMethod: "DatasetInfo.undoLastCitationEdit",
                        datasetId: dataset,
                        fileId: FILE_ID,
                        prevFileId: PREV_FILE_ID,
                        citation: br2nl(CITATION)
                    },
                    onComplete: saveCitationEditListener,
                    onException: function (request, exception) { throw(exception); }
                });
               }else {
                new Ajax.Request("DatasetInfoEdit", {
                    parameters: {
                        requestingMethod: "DatasetInfo.undoLastCitationEdit",
                        datasetId: dataset,
                        fileId: FILE_ID,
                        prevFileId: PREV_FILE_ID,
                        citation: br2nl(PREV_CITATION)
                    },
                    onComplete: saveCitationEditListener,
                    onException: function (request, exception) { throw(exception); }
                });
               }
           } else if (PREV_FILE_ID > 0){
               new Ajax.Request("DatasetInfoEdit", {
                parameters: {
                    requestingMethod: "DatasetInfo.undoLastCitationEdit",
                    datasetId: dataset,
                    fileId: PREV_FILE_ID,
                    prevFileId: PREV_FILE_ID,
                    citation: br2nl(PREV_CITATION)
                },
                onComplete: saveCitationEditListener,
                onException: function (request, exception) { throw(exception); }
            });
           }
    } else {
       errorPopup("Last edit unavailible, unable to undo.");
       if ($("citation_edit_undo")) { $("citation_edit_undo").remove(); }
    }
}
/** AJAX call to display the preferred citation content. */
function showCitation(file){
    var citationTextArea = $('paper_citation');
    if (citationTextArea != null && file == 0) {
        citationTextArea.parentNode.removeChild(citationTextArea);
    }

    var citationSpan = $("paper_citation_div");
    var suggestions = new Array();
    new Ajax.Request("DatasetInfoEdit", {
         parameters: {
            requestingMethod: "DatasetInfo.showCitation.citation",
            datasetId: dataset,
            fileId: file,
            ajaxRequest: "true",
            getCitation: "true"
        },
        onComplete: displayCitation,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
/** AJAX response to display the preferred citation content. */
function displayCitation(transport){
    var citationSpan = $('paper_citation_div');
    var response = transport.responseText;
    var data = response.evalJSON();
    var messageType = data.messageType;
    var message = data.message;
    var value = data.value;

    var noCitationSpan = $("no_citation_span");
    if (noCitationSpan){
        noCitationSpan.parentNode.removeChild(noCitationSpan);
    }

    var editCitationLink = $("edit_citation_link");
    if (editCitationLink){
        editCitationLink.parentNode.removeChild(editCitationLink);
    }

    var citationTextArea = $("paper_citation");
    if (messageType == "ERROR" || messageType == 'UNAUTHORIZED') {
        errorPopup(message);
    } else if (messageType == 'SUCCESS') {
        if (citationTextArea != null && citationTextArea.visible() == true){
            citationTextArea.value = value;
        } else if (citationSpan){
             citationSpan.parentNode.removeChild(citationSpan);
             new Insertion.After($('attach_a_paper'),  "<div id=\"paper_citation_div\">"
                        + nl2br(value) + "</div>");
             var citationSpan = $('paper_citation_div');
             if (!$("edit_citation_link") && (value != "") ) {
                     new Insertion.After(citationSpan,
                             "<div id=\"edit_citation_link\"><a onclick=\"makeCitationEditable();\">edit</a></div>");
             } else if (!$("edit_citation_link")
                     && (value == "")
                     && ($('file_name_size_select').value > 0)){
                 new Insertion.After(citationSpan,
                         "    <div id=\"no_citation_span\">This paper has no citation.</div> "
                         + "<div id=\"edit_citation_link\"><a onclick=\"makeCitationEditable();\">Add one here.</a></div>");
             }
         }
    } else {
        errorPopup("Unexpected error from the server. "
        + "Please try again and contact the DataShop team if the errors persists.");
    }
}

function goToPapersPage(datasetId) {
    var url = "Files?datasetId=" + dataset;
    window.location = url;
}

/**
 * Replaces newline characters with break tags.  The Preceding space is an IE7 fix, which
 * doesn't recognize consecutive break statements without a character in between.
 */
 function nl2br(text){
        var text = escape(text);
        if(text.indexOf('%0D%0A') > -1){
            var re_nlchar = /%0D%0A/g;
            text = text.replace(re_nlchar,'&nbsp;<br />')
        } else if(text.indexOf('%0A') > -1){
            var re_nlchar = /%0A/g;
            text = text.replace(re_nlchar,'&nbsp;<br />')
        } else if(text.indexOf('%0D') > -1){
            var re_nlchar = /%0D/g;
            text = text.replace(re_nlchar,'&nbsp;<br />')
        }
        return unescape(text);
}

 /**
  * Replaces break tags with newline characters.  The Preceding space is an IE7 fix, which
  * doesn't recognize consecutive break statements without a character in between.
  */
  function br2nl(text){
         if(text.indexOf('&nbsp;<br>') > -1){
             var re_brchar = /&nbsp;<br>/g;
             text = text.replace(re_brchar,'%0D%0A');
         } else if(text.indexOf('%0A') > -1){
             var re_nlchar = /&nbsp;/g;
             text = text.replace(re_brchar,'%0D%0A');
         } else if(text.indexOf('%0D') > -1){
             var re_nlchar = /<br>/g;
             text = text.replace(re_brchar,'%0D%0A');
         }
         return unescape(text);
 }

// Reset contentType to 'papers'
function resetForPapers(datasetId) {
    var contentType = "papers";
    new Ajax.Request("Files", {
        parameters: {
            requestingMethod: "DatasetInfo.resetForPapers",
            datasetId: datasetId,
            ajaxRequest: "true",
            files_request: "content",
            files_content: contentType
        },
        onException: function (request, exception) {
            throw(exception);
        }
    });
}



function initSamplesPageContent() {
    jQuery('.historyList').hide();
}
