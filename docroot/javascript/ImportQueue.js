//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 14324 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-10-05 12:58:37 -0400 (Thu, 05 Oct 2017) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {
    jQuery('#iqCollapseImage').click(collapseImportQueue);
    jQuery('#iqExpandImage').click(expandImportQueue);
    jQuery('#iqExpandImage').hide();
    
    // Assign the open dialog function to the appropriate links
    jQuery('.cancel_import_link').click(cancelImportFromGear);
    
    // Set up gear menu actions
    jQuery(".gearAnchor").click(gearAnchorToggle);
    jQuery(".gearSubmenu").mouseup(function() { return false });
    jQuery(".gearAnchor").mouseup(function() { return false });
    jQuery(document).mouseup(gearAnchorHide);
});

function gearAnchorToggle() {
    anchorId = "#".concat(this.id);
    submenuId = "#".concat(this.id.replace("Anchor", "Submenu"));

    //if we're going to show the menu, hide all other menus and set them to low zIndex values, 
    //set this one to high zIndex values but with anchor on top
    if (jQuery(submenuId).is(':visible') != true) {
        jQuery(".gearSubmenu").hide();
        jQuery(".gearAnchor").zIndex('50');
        jQuery(".gearSubmenu").zIndex('50');
        jQuery(anchorId).zIndex('110');
        jQuery(submenuId).zIndex('100');
    }

    jQuery(submenuId).toggle();
}

function gearAnchorHide() {
    jQuery(".gearAnchor").zIndex('50');
    jQuery(".gearSubmenu").zIndex('50');
    jQuery(".gearSubmenu").hide();
}

//----------------------------------------------------------------------
// General function to get the import queue id from an element name
// which has an underscore before the id
//----------------------------------------------------------------------

function getImportQueueIdFromElementId(elementId) {
    return elementId.substring(elementId.lastIndexOf("_")+1,elementId.length);
}
//---------------------------
// Download file
//---------------------------
function downloadFile(importQueueId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "download_iq_file_form";
    newForm.name = "download_iq_file_form";
    newForm.form = "text/plain";
    newForm.action = "ImportQueue";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = "downloadFile";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "importQueueId";
    newInput.type  = "hidden";
    newInput.value = importQueueId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//----------------------------------------------------
// Collapse/Expand Import Queue on My Datasets Pages
//----------------------------------------------------

function collapseImportQueue() {
    jQuery('#iqCollapseImage').hide();
    jQuery('#iqExpandImage').show();
    jQuery('#importQueueTable_my_datasets_queue').hide();
}
function expandImportQueue() {
    jQuery('#iqCollapseImage').show();
    jQuery('#iqExpandImage').hide();
    jQuery('#importQueueTable_my_datasets_queue').show();
}
//------------------------------------------------------
// Hide/Show the Description of the IQ row
//------------------------------------------------------
function showDescription(importQueueId) {
    jQuery('#datasetDescShow_' + importQueueId).hide();
    jQuery('#datasetDescHide_' + importQueueId).show();
}
function hideDescription(importQueueId) {
    jQuery('#datasetDescShow_' + importQueueId).show();
    jQuery('#datasetDescHide_' + importQueueId).hide();
}

//------------------------------------------------------
// Hide/Show the Details of a row (Verification Results)
//------------------------------------------------------
function showDetails(importQueueId) {
    jQuery('#showDetails_' + importQueueId).hide();
    jQuery('#hideDetails_' + importQueueId).show();
    jQuery('#resultsRow_' + importQueueId).show("slow");
    jQuery('#resultsRow_' + importQueueId).prev("tr").children("td").css('border-bottom', 'none');
}

function hideDetails(importQueueId) {
    jQuery('#showDetails_' + importQueueId).show();
    jQuery('#hideDetails_' + importQueueId).hide();
    jQuery('#resultsRow_' + importQueueId).hide("fast");
    jQuery('#resultsRow_' + importQueueId).prev("tr").children("td").css('border-bottom', '1px solid #E0E0E0');
}

//----------------------------------------------------
// Import Queue - Admin - Edit Status Dialog
//----------------------------------------------------

function openIqEditStatusDialog(importQueueId, format) {
    var dialogDiv = document.getElementById('iqEditStatusDialog');
    dialogDiv.innerHTML = "";
    
    var isDiscourseDbProject = (format == 3) ? true : false;

    // Add the info from the table row
    iqesAddInfo(dialogDiv, importQueueId, isDiscourseDbProject);
    iqesAddStatusSelect(dialogDiv, importQueueId, format);
    iqesAddOtherInputs(dialogDiv, importQueueId, isDiscourseDbProject);
    iqesSetStatus();

    // Add hidden input indicating if IQele is on DiscourseDB project.
    var input = document.createElement('input');
    input.id = "isDiscourseDbProject";
    input.name = "isDiscourseDbProject";
    input.type = "hidden";
    input.value = isDiscourseDbProject;
    dialogDiv.appendChild(input);

    jQuery('#iqesStatusSelect').change(selectStatus);
    jQuery('#datasetInput').keypress(checkDatasetNameAjaxRequestOnCR);

    // Do the dialog thing
    jQuery('#iqEditStatusDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Edit Status",
        width : 450,
        buttons : [ {
            id : "iqes-save-button",
            text : "Save",
            click : iqesSaveButton
        }, {
            id : "iqes-cancel-button",
            text : "Cancel",
            click : iqesCancelButton
        } ]
    });

    jQuery('#iqEditStatusDialog').dialog('open');
}

function selectStatus() {
    iqesSetStatus();
}

function iqesSaveButton() {
    saveStatusChangesAjaxRequest();
}

function iqesCancelButton() {
    jQuery(this).dialog("close");
}

function iqesAddInfo(dialogDiv, importQueueId, isDiscourse) {
    
    // Get info from the table row
    var datasetName = jQuery('#datasetName_' + importQueueId).val();
    var userName =    jQuery('#userName_' + importQueueId).val();
    var fileName =    jQuery('#fileName_' + importQueueId).val();
    var projectName = jQuery('#projectName_' + importQueueId).val();
    var format =      jQuery('#format_' + importQueueId).val();
    
    // Add UI elements to the dialog
    var input = document.createElement('input');
    input.id = "importQueueId";
    input.name = "importQueueId";
    input.type = "hidden";
    input.value = importQueueId;
    dialogDiv.appendChild(input);
    
    var p = document.createElement('p');
    p.id = "datasetName";
    if (!isDiscourse) {
        p.innerHTML = "<span class='boldface'>Dataset Name:</span> " + datasetName;
    } else {
        p.innerHTML = "<span class='boldface'>Discourse Name:</span> " + datasetName;
    }
    dialogDiv.appendChild(p);
    
    p = document.createElement('p');
    p.id = "userName";
    p.innerHTML = "<span class='boldface'>User:</span> " + userName;
    dialogDiv.appendChild(p);

    p = document.createElement('p');
    p.id = "fileName";
    p.innerHTML = fileName;
    dialogDiv.appendChild(p);

    p = document.createElement('p');
    p.id = "projectName";
    p.innerHTML = "Project: " + projectName;
    dialogDiv.appendChild(p);

    p = document.createElement('p');
    p.id = "format";
    p.innerHTML = format;
    dialogDiv.appendChild(p);
}

function iqesSetStatus() {
    var newStatusEnum = jQuery('#iqesStatusSelect').val();
    if (newStatusEnum == "queued"
     || newStatusEnum == "passed") {
        jQuery('#estImportDateDiv').show();
        jQuery('#errorsDiv').hide();
        jQuery('#issuesDiv').hide();
        jQuery('#resultsDiv').hide();
        jQuery('#datasetDiv').hide();
    } else if (newStatusEnum == "errors") {
        jQuery('#estImportDateDiv').hide();
        jQuery('#errorsDiv').show();
        jQuery('#issuesDiv').show();
        jQuery('#resultsDiv').show();
        jQuery('#datasetDiv').hide();
    } else if (newStatusEnum == "issues") {
        jQuery('#estImportDateDiv').show();
        jQuery('#errorsDiv').hide();
        jQuery('#issuesDiv').show();
        jQuery('#resultsDiv').show();
        jQuery('#datasetDiv').hide();
    } else if (newStatusEnum == "loaded") {
        jQuery('#estImportDateDiv').hide();
        jQuery('#errorsDiv').hide();
        jQuery('#issuesDiv').hide();
        jQuery('#resultsDiv').hide();
        jQuery('#datasetDiv').show();
    } else if (newStatusEnum == "canceled") {
        jQuery('#estImportDateDiv').hide();
        jQuery('#errorsDiv').hide();
        jQuery('#issuesDiv').hide();
        jQuery('#resultsDiv').hide();
        jQuery('#datasetDiv').hide();
    }
    //hide all errors -- could be a loop over all .errorMessage in div
    jQuery('#estImportDateDiv .errorMessage').hide();
    jQuery('#errorsDiv .errorMessage').hide();
    jQuery('#issuesDiv .errorMessage').hide();
    jQuery('#resultsDiv .errorMessage').hide();
    jQuery('#datasetDiv .errorMessage').hide();
    jQuery('#generalError.errorMessage').hide();
    jQuery('#actualDatasetName').hide();
}

function iqesAddStatusSelect(dialogDiv, importQueueId, format) {
    var currStatus = jQuery('#status_' + importQueueId).val();
    
    var ele = document.createElement('label');
    ele.id  = "statusLabel";
    ele.name  = "statusLabel";
    ele.htmlFor = "iqesStatusSelect";
    ele.innerHTML = "Status";
    dialogDiv.appendChild(ele);
    
    var newSelect = document.createElement('select');
    newSelect.id   = "iqesStatusSelect";
    newSelect.name = "iqesStatusSelect";
    
    //these are hidden elements created in the jsp
    var hiddenSelect = document.getElementById('statusSelectTab');
    if (format == 1) {
        hiddenSelect = document.getElementById('statusSelectXml');
    } else if (format == 3) {
        hiddenSelect = document.getElementById('statusSelectDiscourse');
    }

    for (var i = 0; i < hiddenSelect.length; i++){
        var option = document.createElement('option');
        option.value = hiddenSelect.options[i].value;
        var text = hiddenSelect.options[i].text;
        if (option.value == currStatus) {
            option.selected = true;
        }
        option.appendChild(document.createTextNode(text));
        newSelect.appendChild(option);
    }
    
    dialogDiv.appendChild(newSelect);
}

function iqesAddOtherInputs(dialogDiv, importQueueId, isDiscourse) {
    var est = jQuery('#est_' + importQueueId).val();
    var errors = jQuery('#errors_' + importQueueId).val();
    var issues = jQuery('#issues_' + importQueueId).val();
    var resultsText = jQuery('#resultsText_' + importQueueId).html();
    if (resultsText == null) { resultsText = ""; }
    
    //Estimated Import Date
    var div = document.createElement('div');
    div.id = "estImportDateDiv";
    div.name = "estImportDateDiv";
    dialogDiv.appendChild(div);
    var ele = document.createElement('label');
    ele.id  = "estImportDateLabel";
    ele.name  = "estImportDateLabel";
    ele.htmlFor = "estImportDateInput";
    ele.innerHTML = "Est Import Date";
    div.appendChild(ele);
    
    ele = document.createElement('input');
    ele.id  = "estImportDateInput";
    ele.name  = "estImportDateInput";
    ele.type = "text";
    ele.value = est;
    div.appendChild(ele);
    
    ele = document.createElement('p');
    ele.id  = "estImportDateError";
    ele.className = "errorMessage";
    div.appendChild(ele);
    
    jQuery('#estImportDateInput').datepicker({ dateFormat: "MM d, yy" });

    //Number of errors
    div = document.createElement('div');
    div.id = "errorsDiv";
    div.name = "errorsDiv";
    dialogDiv.appendChild(div);
    ele = document.createElement('label');
    ele.id  = "errorsLabel";
    ele.name  = "errorsLabel";
    ele.htmlFor = "errorsInput";
    ele.innerHTML = "Errors";
    div.appendChild(ele);
    ele = document.createElement('input');
    ele.id  = "errorsInput";
    ele.name  = "errorsInput";
    ele.value = errors;
    div.appendChild(ele);
    ele = document.createElement('p');
    ele.id  = "errorsError";
    ele.className = "errorMessage";
    div.appendChild(ele);

    //Number of issues
    div = document.createElement('div');
    div.id = "issuesDiv";
    div.name = "issuesDiv";
    dialogDiv.appendChild(div);
    ele = document.createElement('label');
    ele.id  = "issuesLabel";
    ele.name  = "issuesLabel";
    ele.htmlFor = "issuesInput";
    ele.innerHTML = "Potential issues";
    div.appendChild(ele);
    ele = document.createElement('input');
    ele.id  = "issuesInput";
    ele.name  = "issuesInput";
    ele.value = issues;
    div.appendChild(ele);
    ele = document.createElement('p');
    ele.id = "issuesError";
    ele.className = "errorMessage";
    div.appendChild(ele);
    
    //Verification results
    div = document.createElement('div');
    div.id = "resultsDiv";
    div.name = "resultsDiv";
    dialogDiv.appendChild(div);
    ele = document.createElement('label');
    ele.id  = "resultsLabel";
    ele.name  = "resultsLabel";
    ele.htmlFor = "resultsInput";
    ele.innerHTML = "Verification results";
    div.appendChild(ele);
    ele = document.createElement('textarea');
    ele.id  = "resultsInput";
    ele.name  = "resultsInput";
    ele.value = resultsText;
    ele.rows = 4;
    ele.cols = 30;
    div.appendChild(ele);
    ele = document.createElement('p');
    ele.id = "resultsError";
    ele.className = "errorMessage";
    div.appendChild(ele);
    
    //Dataset Id
    div = document.createElement('div');
    div.id = "datasetDiv";
    div.name = "datasetDiv";
    dialogDiv.appendChild(div);
    ele = document.createElement('label');
    ele.id  = "datasetLabel";
    ele.name  = "datasetLabel";
    ele.htmlFor = "datasetInput";
    ele.innerHTML = !isDiscourse ? "New Dataset Id" : "New Discourse Id";
    div.appendChild(ele);
    ele = document.createElement('input');
    ele.id  = "datasetInput";
    ele.name  = "datasetInput";
    ele.value = "";
    div.appendChild(ele);
    ele = document.createElement('a');
    ele.id = "datasetNameCheckLink";
    ele.innerHTML = "check";
    div.appendChild(ele);
    jQuery('#datasetNameCheckLink').click(checkDatasetNameAjaxRequest);
    ele = document.createElement('p');
    ele.id = "datasetError";
    ele.className = "errorMessage";
    div.appendChild(ele);
    ele = document.createElement('p');
    ele.id = "actualDatasetName";
    div.appendChild(ele);
    
    //General error message
    ele = document.createElement('p');
    ele.id = "generalError";
    ele.name = "generalError";
    ele.className = "errorMessage";
    dialogDiv.appendChild(ele);
}

//Ajax call to save changes on Edit Status dialog
function saveStatusChangesAjaxRequest() {
    var importQueueId = jQuery('#importQueueId').val();
    var status = jQuery('#iqesStatusSelect').val();
    var estImportDate = jQuery('#estImportDateInput').val();
    var errors = jQuery('#errorsInput').val();
    var issues = jQuery('#issuesInput').val();
    var results = jQuery('#resultsInput').val();
    var datasetId = jQuery('#datasetInput').val();
    var isDiscourse = jQuery('#isDiscourseDbProject').val();
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "saveStatusChangesAjaxRequest",
            importQueueId : importQueueId,
            status : status,
            estImportDate : estImportDate,
            errors : errors,
            issues : issues,
            results : results,
            datasetId : datasetId,
            isDiscourse : isDiscourse
        },
        requestHeaders : {Accept : 'application/json;charset=UTF-8'},
        beforeSend : showStatusIndicator("Please wait..."),
        onSuccess : hideStatusIndicator,
        onComplete : saveStatusChangesAjaxListener,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}

function saveStatusChangesAjaxListener(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        //close dialog
        jQuery('#iqEditStatusDialog').dialog('close');
        //update fields in table by reloading the page
        reloadPage();
    } else if (json.msg == "error") {
        //display error messages if they exist
        if (json.generalErrorMsg.length > 0) {
            jQuery('#iqEditStatusDialog .errorMessage').html(json.generalErrorMsg);
            jQuery('#iqEditStatusDialog .errorMessage').show();
        } else {
            jQuery('#iqEditStatusDialog .errorMessage').hide();
        }
        if (json.estImportDateErrorMsg.length > 0) {
            jQuery('#estImportDateDiv .errorMessage').html(json.estImportDateErrorMsg);
            jQuery('#estImportDateDiv .errorMessage').show();
        } else {
            jQuery('#estImportDateDiv .errorMessage').hide();
        }
        if (json.errorsErrorMsg.length > 0) {
            jQuery('#errorsDiv .errorMessage').html(json.errorsErrorMsg);
            jQuery('#errorsDiv .errorMessage').show();
        } else {
            jQuery('#errorsDiv .errorMessage').hide();
        }
        if (json.issuesErrorMsg.length > 0) {
            jQuery('#issuesDiv .errorMessage').html(json.issuesErrorMsg);
            jQuery('#issuesDiv .errorMessage').show();
        } else {
            jQuery('#issuesDiv .errorMessage').hide();
        }
        if (json.resultsErrorMsg.length > 0) {
            jQuery('#resultsDiv .errorMessage').html(json.resultsErrorMsg);
            jQuery('#resultsDiv .errorMessage').show();
        } else {
            jQuery('#resultsDiv .errorMessage').hide();
        }
        if (json.datasetIdErrorMsg.length > 0) {
            jQuery('#datasetDiv .errorMessage').html(json.datasetIdErrorMsg);
            jQuery('#datasetDiv .errorMessage').show();
            jQuery('#actualDatasetName').hide();
        } else {
            jQuery('#datasetDiv .errorMessage').hide();
        }
    }
}

function reloadPage() {
    var newForm = document.createElement('FORM');
    newForm.id   = "reload_import_queue_form";
    newForm.name = "reload_import_queue_form";
    newForm.form = "text/plain";
    newForm.action = "ImportQueue";
    newForm.method = "post";

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//-------------------------------------------------------
// Get the dataset name
//-------------------------------------------------------

function checkDatasetNameAjaxRequestOnCR(e) {
    if (e.which != 13) {
        return;
    }
    checkDatasetNameAjaxRequest();
}
function checkDatasetNameAjaxRequest() {
    var datasetId = jQuery('#datasetInput').val();
    var isDiscourse = jQuery('#isDiscourseDbProject').val();

    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "checkDatasetNameAjaxRequest",
            datasetId : datasetId,
            isDiscourse : isDiscourse
        },
        requestHeaders : {Accept : 'application/json;charset=UTF-8'},
        beforeSend : showStatusIndicator("Please wait..."),
        onSuccess : hideStatusIndicator,
        onComplete : checkDatasetNameAjaxListener,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}

function checkDatasetNameAjaxListener(transport) {
    var isDiscourse = jQuery('#isDiscourseDbProject').val();
    var label = (isDiscourse == "true") ? "Discourse:" : "Dataset:";

    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        //display dataset name and creation date
        var html = "<span class=\"boldface\">Project:</span> " + json.projectName + "<br>";
        html += "<span class=\"boldface\">" + label + "</span> " + json.datasetName + "<br>";
        html += "<span class=\"boldface\">Created:</span> " + json.creationDate;
        jQuery('#actualDatasetName').html(html);
        jQuery('#actualDatasetName').show();
        jQuery('#datasetDiv .errorMessage').hide();
    } else if (json.msg == "error") {
        //display error message
        jQuery('#actualDatasetName').hide();
        if (json.datasetIdErrorMsg.length > 0) {
            jQuery('#datasetDiv .errorMessage').html(json.datasetIdErrorMsg);
            jQuery('#datasetDiv .errorMessage').show();
        } else {
            jQuery('#datasetDiv .errorMessage').hide();
        }
    }
}

//-------------------------------------------------------
// Move items up/down in the queue
//-------------------------------------------------------

function moveItemUp(importQueueId) {
    submitForm("moveItemUp", importQueueId);
}
function moveItemDown(importQueueId) {
    submitForm("moveItemDown", importQueueId);
}

//----------------------------------------------------
// Cancel Import Dialog
//----------------------------------------------------

function undoCancel(importQueueId) {
    submitForm("undoCancel", importQueueId);
}

function openIqCancelImportDialog(importQueueId) {
    var dialogDiv = document.getElementById('iqCancelImportDialog');
    dialogDiv.innerHTML = "";

    var datasetName = jQuery('#datasetName_' + importQueueId).val();

    var itemStr = getItemStr(importQueueId);
    
    var newInput = document.createElement('input');
    newInput.id  = "importQueueId";
    newInput.type  = "hidden";
    newInput.value = importQueueId;
    dialogDiv.appendChild(newInput);

    p = document.createElement('p');
    p.name = "question";
    p.innerHTML = "Are you sure you want to cancel importing the " + itemStr + " '" + datasetName + "'?";
    dialogDiv.appendChild(p);
 
    // Do the dialog thing
    jQuery('#iqCancelImportDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Edit Status",
        width : 450,
        buttons : [ {
            id : "iqci-yes-button",
            text : "Yes",
            click : iqciYesButton
        }, {
            id : "iqci-no-button",
            text : "No",
            click : iqciNoButton
        } ]
    });

    jQuery('#iqCancelImportDialog').dialog('open');
}
function iqciYesButton() {
    var importQueueId = jQuery('#importQueueId').val();
    cancelImport(importQueueId);
    jQuery('#iqCancelImportDialog').dialog('close');
}
function iqciNoButton() {
    jQuery('#iqCancelImportDialog').dialog('close');
}
function cancelImport(importQueueId) {
    submitForm("cancelImport", importQueueId);
}

function cancelImportFromGear() {
    var importQueueId = getImportQueueIdFromElementId(this.id);
    openIqCancelImportDialog(importQueueId);
}

//----------------------------------------------------
// Hide Import Queue Item
//----------------------------------------------------

function openIqHideRowDialog(importQueueId) {
    var dialogDivId = "iqHideRowDialog";
    var dialogDiv = document.createElement('div');
    dialogDiv.id  = dialogDivId;
    document.body.appendChild(dialogDiv);    
    
    var newInput = document.createElement('input');
    newInput.id  = "importQueueId";
    newInput.type  = "hidden";
    newInput.value = importQueueId;
    dialogDiv.appendChild(newInput);

    p = document.createElement('p');
    p.name = "question";
    p.innerHTML = "Are you sure you want to remove this file from the import queue? This cannot be undone.";
    dialogDiv.appendChild(p);
    
    dialogDivId = "#".concat(dialogDivId);
 
    // Do the dialog thing
    jQuery(dialogDivId).dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Remove file",
        width : 450,
        buttons : [ {
            id : "iqhr-yes-button",
            text : "Remove",
            click : iqhrYesButton
        }, {
            id : "iqhr-no-button",
            text : "Cancel",
            click : iqhrNoButton
        } ]
    });

    jQuery(dialogDivId).dialog('open');
}

function hideRow(importQueueId) {
    openIqHideRowDialog(importQueueId);
}
function iqhrYesButton() {
    var importQueueId = jQuery('#importQueueId').val();
    submitForm("hideRow", importQueueId);
    jQuery(this).dialog("close");
}
function iqhrNoButton() {
    jQuery(this).dialog("close");
}

//-------------------------------------------------------
// Generic method to submit a form with
// an action (which is the name of the requesting method)
// and the import queue id.
//-------------------------------------------------------

function submitForm(requestingMethod, importQueueId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "submit_form";
    newForm.name = "submit_form";
    newForm.form = "text/plain";
    newForm.action = window.location.href;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = requestingMethod;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "importQueueId";
    newInput.type  = "hidden";
    newInput.value = importQueueId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//----------------------------------------------------
// Pause/Start Queue
//----------------------------------------------------

function pauseQueue() {
    submitCommand("pauseQueue");
}

function startQueue() {
    submitCommand("startQueue");
}

//-------------------------------------------------------
//Generic method to submit a form with nothing but
//an action (which is the name of the requesting method).
//-------------------------------------------------------

function submitCommand(requestingMethod) {
    var newForm = document.createElement('FORM');
    newForm.id   = "submit_form";
    newForm.name = "submit_form";
    newForm.form = "text/plain";
    newForm.action = window.location.href;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = requestingMethod;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//-------------------------------------------------------
// Show a status indicator while waiting on an AJAX call
// FIXME THIS SHOULD BE SOMEWHERE ELSE -- MORE GENERIC -- COPIED FROM AccessRequests.js
//-------------------------------------------------------

function showStatusIndicator(titleString) {
    var div = document.createElement('div');
    div.id = "statusIndicator";
    div.className = "statusIndicator";

    jQuery('body').append(div);

    // Status Indicator Popup
    jQuery('.statusIndicator').dialog({
        modal : true,
        autoOpen : false,
        title : titleString,
        width : 120,
        height : 120
    });

    // Create form elements to add to the status indicator
    jQuery('#statusIndicator')
        .html('<center><img id="waitingIcon" src="images/waiting.gif" /></center>');
    jQuery('#statusIndicator').dialog('open');
    jQuery('#statusIndicator').dialog("option", "stack", true);
    jQuery('#statusIndicator').dialog("option", "resizable", false);
}

// Hides that status indicator
function hideStatusIndicator() {
    jQuery('#statusIndicator').dialog('close');
}

//-------------------------------------------------------
// Sorting methods
//-------------------------------------------------------

function sortLoadedImportQueueTable(sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id   = "sort_loaded_iq_table_form";
    newForm.name = "sort_loaded_iq_table_form";
    newForm.form = "text/plain";
    newForm.action = "ImportQueue";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = "sortRecentlyLoaded";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function sortNoDataImportQueueTable(sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id   = "sort_no_data_iq_table_form";
    newForm.name = "sort_no_data_iq_table_form";
    newForm.form = "text/plain";
    newForm.action = "ImportQueue";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "requestingMethod";
    newInput.type  = "hidden";
    newInput.value = "sortNoData";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function updateRecentItems() {
    jQuery('#lastDaysRecentForm').appendTo('body').submit();
}

function updateNoDataItems() {
    jQuery('#lastDaysNoDataForm').appendTo('body').submit();
}