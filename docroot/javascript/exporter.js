//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2005
// All Rights Reserved
//
// Author: Benjamin Billings
// Version: $Revision: 10152 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2013-10-15 09:42:21 -0400 (Tue, 15 Oct 2013) $
// $KeyWordsOff: $
//
//
// Add an onload listener to initialize everything for this report.
//
onloadObserver.addListener(initExportInfo);

var subtabs = $A(new Array());
subtabs.push({
        linkId: "transactions_export_subtab_link",
        linkLabel: "By Transaction",
        linkName: "byTransaction",
        onClick: function () {
            selectSubtab("byTransaction");
            contentCheck("byTransaction");
        }
    });
subtabs.push({
        linkId: "student_step_export_subtab_link",
        linkLabel: "By Student-Step",
        linkName: "byStudentStep",
        onClick: function () {
            selectSubtab("byStudentStep");
            contentCheck("byStudentStep");
        }
    });
subtabs.push({
        linkId: "student_problem_export_subtab_link",
        linkLabel: "By Student-Problem",
        linkName: "byProblem",
        onClick: function () {
            selectSubtab("byProblem");
            contentCheck("byProblem");
        }
    });

//
// Initialize the default navigation and content.
//
function initExportInfo() {

    //hide all the menu's at the start except for sampless.
    $('problemExportNav').hide();
    $('stepExportNav').hide();
    $('skills').hide();
    $('skillModels').hide();
    $('students').hide();
    $('problems').hide();

    //init the navigation sections.
    initExportOptionHandlers();
    initDefaultNavigation();
    modelList.hideSecondary();

    createManageKCSetsDialog();
    skillObserver.addListener(updateContent);
    skillObserver.addListener(updateSetModifier);
    studentObserver.addListener(updateContent);
    problemObserver.addListener(updateContent);
    sampleObserver.addListener(contentCheck);
    //initialize the subtabs.
    var subtabDiv = $("subtab");
    subtabs.each(function (subtab) {
        subtabDiv.insert('<a id="' + subtab.linkId + '">' + subtab.linkLabel + '</a>');
        $(subtab.linkId).observe('click', subtab.onClick);
    });

    //find out what kind of content we should be looking at.
    contentCheck();
}

function studentProblemSkillModelInit(selectedModels) {
    if ($F('exportIncludeKCs')) {
        $('skill_model_select').removeAttribute('disabled');
        $('exportIncludeStepsWithoutKCs').removeAttribute('disabled');
        $('selectedKCM').innerHTML = selectedModels;
    } else {
        $('skill_model_select').setAttribute('disabled', 'disabled');
        $('exportIncludeStepsWithoutKCs').setAttribute('disabled', 'disabled');
        if ($F('exportIncludeAllKCs')) {
            $('selectedKCM').innerHTML = 'All Models';
        } else {
            $('selectedKCM').innerHTML = 'No Models';
        }
    }
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
        new Ajax.Request(window.location.href, {
            parameters: {
                requestingMethod: "exporter.initExportOptionHandlers",
                datasetId: dataset,
                determine_content: "true",
                exportIncludeAllKCs: $F('exportIncludeAllKCs') ? true : false,
                exportIncludeKCs: $F('exportIncludeKCs') ? true : false,
                exportIncludeNoKCs: $F('exportIncludeNoKCs') ? true : false,
                exportIncludeStepsWithoutKCs: $F('exportIncludeStepsWithoutKCs') ? true : false,
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
    $("exportIncludeAllKCs").observe('click', update);
    $("exportIncludeKCs").observe('click', update);
    $("exportIncludeNoKCs").observe('click', update);
    $("exportIncludeStepsWithoutKCs").observe('click', update);
    $("exportStepIncludeAllKCs").observe('click', update);
    $("exportStepIncludeKCs").observe('click', update);
    $("exportStepIncludeNoKCs").observe('click', update);
}

/**
 * Gets the current content to be displayed along with a check
 * that at least one sample is selected.
 * @param subtab if content type is known, pre-select the subtab.
 */
function contentCheck(subtab) {
    if (subtab) {
        var params = {
                requestingMethod: "exporter.contentCheck",
                datasetId: dataset,
                set_selected_subtab: subtab};
    } else {
        var params = {
                requestingMethod: "exporter.contentCheck",
                datasetId: dataset,
                determine_content: "true"};
    }

    new Ajax.Request(window.location.href, {
        parameters: params,
        onComplete: updateContent,
        onException: function (request, exception) {
          throw(exception);
        }
    });
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
        selected = "byTransaction"
        selectSubtab("byTransaction");
    }

    return selected;
}

/**
 * This function updates the content based on the currently selected subtab.
 * It can take either a transport object with the noSamplea and subtab set, or
 * it just checks the page.  If you need to verify a sample has been selected
 * call "contentCheck" instead.
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

    $('skillModels').hide();

    if (subtab == "byTransaction") {
        theHelpWindow.updateContent($("help-export-tx"));
        $('cached_export_selection_div').hide();
        $('txExportNav').show();
        $('problemExportNav').hide();
        $('stepExportNav').hide();
        $('skills').hide();
        $('students').hide();
        $('problems').hide();
        $('contentSetName').hide();
        $('contentSetNameModified').hide();

        closeManageKCSetsDialog();
        updateKCMHeader();
        requestByTransaction();
        showExportFileStatus();
        updateExportFileStatus();
    } else if (subtab == "byStudentStep") {
        theHelpWindow.updateContent($("help-export-step"));
        $('cached_export_selection_div').show();
        $('txExportNav').hide();
        $('problemExportNav').hide();
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

        modelList.hideSecondary();
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

    } else {
        theHelpWindow.updateContent($("help-export-problem"));
        $('cached_export_selection_div').show();
        $('txExportNav').hide();
        $('problemExportNav').show();
        $('stepExportNav').hide();
        if ($F('exportIncludeNoKCs') || $F('exportIncludeAllKCs')) {
          $('skills').hide();
        } else {
          $('skills').show();
        }
        $('kcModelNavHeader').hide();
        $('primary_kc_model_label').hide();

        studentProblemSkillModelInit(selectedSkillModelList);
        $('students').show();
        $('problems').show();

        modelList.hideSecondary();
        resetKCMHeader();
        if ($('contentSetName')) {
            $('contentSetName').show();
        }
        if ($('contentSetNameModified')) {
            $('contentSetNameModified').show();
        }
        requestByProblem();
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

function selectSubtab(selectedLink) {
    subtabs.each(function (subtab) {
        if (subtab.linkName==selectedLink)
            $(subtab.linkId).addClassName("selected");
        else
         $(subtab.linkId).removeClassName("selected");
    });
}

function updateKCMHeader() {
    var selectedKCMArea = $('selectedKCM');
    if (selectedKCMArea) {
        selectedKCMArea.hide();
    }
    var allSelectedKCMArea = $('allSelectedKCM');
    if (allSelectedKCMArea) {
        allSelectedKCMArea.show();
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


/** AJAX request to get the overview content from server. */
function requestByTransaction() {
    $('main_content_div').innerHTML = "<div id='pgrid_tx'/>";

    var exportAllowed = "true";
    if ($('datasetExportAllowed')) {
        exportAllowed = $('datasetExportAllowed').value;
    }

    var options = new Array();
    options['exportCall'] = startExportTx;
    options['exportAllowed'] = exportAllowed;
    options['extraParams'] = {datasetId: dataset};
    new PageGrid('pgrid_tx', 'Transactions', "Export", options);
}


/** AJAX request to get the overview content from server. */
function requestByStudentStep() {
    $('main_content_div').innerHTML = "<div id='stepRollupPreview'/>";

    var exportAllowed = "true";
    if ($('datasetExportAllowed')) {
        exportAllowed = $('datasetExportAllowed').value;
    }

    var options = {
        exportCall: function () {
            if (progressBar != null && progressBar) {
                //DS721 not sure if this will break something else. [8/17/2009 ads]
                //progressBar.cancelExport();
                progressBar.closeAll();
            }
            progressBar = false;

            var postParams = {}
            postParams['requestingMethod'] = "exporter.requestByStudentStep";
            postParams['datasetId'] = dataset;
            postParams['export_start'] = "true"
            postParams['use_cached_version'] = $('use_cached_version').checked;
            new Ajax.Request("StepRollupExport", {
                parameters: postParams,
                onComplete: function () {
                    progressBar = new ProgressBar(
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


/** AJAX request to get the overview content from server. */
function requestByProblem() {
    $('main_content_div').innerHTML = "<div id='studentProblemPreview'/>";

    var exportAllowed = "true";
    if ($('datasetExportAllowed')) {
        exportAllowed = $('datasetExportAllowed').value;
    }

    var options = {
        exportCall: function () {
            if (progressBar != null && progressBar) {
                progressBar.closeAll();
            }
            progressBar = false;

            var postParams = {}
            postParams['requestingMethod'] = "exporter.requestByProblem";
            postParams['datasetId'] = dataset;
            postParams['export_start'] = "true";
            postParams['use_cached_version'] = $('use_cached_version').checked;

            new Ajax.Request("StudentProblemExport", {
                parameters: postParams,
                onComplete: function () {
                    progressBar = new ProgressBar(
                        function () {
                            var newForm = document.createElement('FORM');
                            newForm.setAttribute('name', 'student_problem_export_form');
                            newForm.setAttribute('id', 'student_problem_export_form');
                            newForm.setAttribute('form', 'text/plain');
                            newForm.setAttribute('action', "StudentProblemExport?datasetId=" + dataset );
                            newForm.setAttribute('method', 'POST');

                            var newInput = document.createElement('input');
                            newInput.name="get_export_file";
                            newInput.type="hidden";
                            newInput.value="true";
                            newForm.appendChild(newInput);

                            document.getElementsByTagName('body').item(0).appendChild(newForm);
                            newForm.submit();
                        },
                        'StudentProblemExport');
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
    new PageGrid('studentProblemPreview', "Student-Problem Rollup", 'StudentProblemExport', options);
}

var progressBar = false;
var currentExportType = false;
function startExportTx() {
    currentExportType = "tx_export";
    startExport("tx_export");
}
function startExportProb() {
    currentExportType = "student_problem_export";
    startExport("student_problem_export");

}

function startExport(txProb) {
    if (!progressBar) {
        var postParams = {};
        postParams['export_start'] = "true";
        postParams[txProb] = "true";
        new Ajax.Request(window.location.href, {
            parameters: postParams,
            onComplete: displayProgressBar,
            onException: function (request, exception) {
              throw(exception);
            }
        });
    } else {
        if (progressBar != null) {
            //DS721 not sure if this will break something else. [8/17/2009 ads]
            //progressBar.cancelExport();
            progressBar.closeAll();
        }
        progressBar = false;
        startExport(currentExportType);
    }
}

function displayProgressBar(transport) {
    if (!progressBar) {
        var extraParamsArray = {};
        extraParamsArray[currentExportType] = "true";

        var options = { extraParams: extraParamsArray };
        progressBar = new ProgressBar(getFinalProduct, window.location.href, options);
    }
}

function getFinalProduct() {
    var newForm = document.createElement('FORM');
    newForm.setAttribute('name', 'export_form');
    newForm.setAttribute('id', 'export_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', window.location.href);
    newForm.setAttribute('method', 'POST');

    var newInput = document.createElement('input');
    newInput.name="get_export_file";
    newInput.type="hidden";
    newInput.value="true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name=currentExportType;
    newInput.type="hidden";
    newInput.value="true";
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
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
            requestingMethod: "exporter.selectExportOptions",
            datasetId: dataset,
            ajaxRequest: "true",
            displaySkills: $F('sri_displaySkills') ? true : false,
            displayPredicted: $F('sri_displayLFAScore') ? true : false
       },
       onComplete: function () {contentCheck("byStudentStep"); },
       onException: function (request, exception) {
           throw(exception);
       }
   });
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

/*
 * Removes square brackets from the text.
 * */
function removeBrackets(textToClean){
    return textToClean.replace('[', '').replace(']', '');
}

