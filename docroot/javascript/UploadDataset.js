//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2013
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 13474 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-09-08 10:23:28 -0400 (Thu, 08 Sep 2016) $
// $KeyWordsOff: $
//

// Maximum character length for the reason textarea
var MaxReasonLen = 500;

// Prototype-style trim function (copied from FileUpload.js from TermsAdminManage.js)
String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

// (copied from FileUpload.js)
jQuery.noConflict();

// jQuery's Onload function (copied from FileUpload.js)
jQuery(document).ready(function() {
    // initialize buttons
    jQuery('#upload_button').button();
    jQuery('#create_button').button();
    jQuery('#choose_button').button();
    jQuery('#continue_button').button();
    jQuery('#back_button').button();

    jQuery('#format_select').change(fileFormatChanged);

    // By default, the Upload/Create/Choose buttons are disabled
    jQuery('#upload_button').button('disable');
    jQuery('#create_button').button('disable');
    jQuery('#choose_button').button('disable');

    // 'Create' (for project) enabled once name is given
    jQuery('#new_project_name').keyup(enableCreateButton);

    // Several variables go into enable of 'Upload' (of dataset) button
    jQuery('#datasetName').keyup(checkContinueButton);
    jQuery('#domainLearnlab').change(checkContinueButton);
    jQuery('#hasStudyData').change(checkContinueButton);
    jQuery('#new_project_name').keyup(checkContinueButton);
    jQuery('#existing_project_select').change(checkContinueButton);
    jQuery('#newRadio').click(checkContinueButton);
    jQuery('#existingRadio').click(checkContinueButton);
    jQuery('#laterRadio').click(checkContinueButton);
    jQuery('#txToUploadGroupNo').click(checkContinueButton);
    jQuery('#txToUploadGroupYes').click(checkContinueButton);
    jQuery('#fileName').change(checkUploadButton);
});

// ----- Project Group -----

function showNewProjectDiv() {
    $('new_project_div').style.display = "inline";
    $('existing_project_list_div').style.display = "none";
}
function showExistingProjectDiv() {
    $('new_project_div').style.display = "none";
    $('existing_project_list_div').style.display = "inline";
}
function showChooseLaterProjectDiv() {
    $('new_project_div').style.display = "none";
    $('existing_project_list_div').style.display = "none";
}

function showExistingProject(selectElement) {
    //loop through and hide all the existing project DIVs
    for (var i = 0; i < selectElement.length; i++){
        var val = selectElement.options[i].value;
        var hideDiv = $("existing_project_div_" + val);
        if (hideDiv) {
            hideDiv.style.display = "none";
        }
    }

    //show just the one selected
    var projectId = selectElement.value;
    var newDiv = $("existing_project_div_" + projectId);
    if (newDiv) {
        newDiv.style.display = "inline";
    }
    if (projectId != 0) {
        var errorMsg = $("existingProjectErrorMsg");
        if (errorMsg) {
            errorMsg.style.display = "none";
        }
    }
}

function disableYesTxOptions() {
    $('fileName').disabled = true;
    $('format_select').disabled = true;

    $('anonIncludingButton').disabled = true;
    $('anonIncludingLabel').addClassName("grayTxt");
    $('anonExceptButton').disabled = true;
    $('anonExceptLabel').addClassName("grayTxt");
    $('anonRevealsButton').disabled = true;
    $('anonRevealsLabel').addClassName("grayTxt");

    jQuery( "button" ).button( "option", "label", "Create" );
}

function enableYesTxOptions() {
    $('fileName').disabled = false;
    $('format_select').disabled = false;

    $('anonIncludingButton').disabled = false;
    $('anonIncludingLabel').removeClassName("grayTxt");
    $('anonExceptButton').disabled = false;
    $('anonExceptLabel').removeClassName("grayTxt");
    $('anonRevealsButton').disabled = false;
    $('anonRevealsLabel').removeClassName("grayTxt");

    jQuery( "button" ).button( "option", "label", "Upload" );
}

function includingChecked() {
    checkUploadButton();
}

function exceptChecked() {
    checkUploadButton();
}

function revealsChecked() {
    disableUploadButton();
}

function irbReqtsChecked() {
    checkUploadButton();
}

function checkContinueButton() {
    var enableFlag = false;

    // Dataset Name must be specified
    var dsName = jQuery('#datasetName').val();
    if (dsName.length > 0) {
        enableFlag = true;
    }

    // Must choose Domain/Learnlab...
    var dl = jQuery('#domainLearnlab').val();
    if (dl != "dummy") {
        enableFlag = enableFlag && true;
    } else {
        enableFlag = false;
    }

    // Must specify 'Has study data?'
    var hsd = jQuery('#hasStudyData').val();
    if (hsd != "dummy") {
        enableFlag = enableFlag && true;
    } else {
        enableFlag = false;
    }

    // If new, project name must be specified
    var newRadioFlag = jQuery('#newRadio').attr('checked');
    if (newRadioFlag) {
        jQuery('#no_data_no_project_warning').hide();
        if (jQuery('#new_project_name').val().length > 0) {
            enableFlag = enableFlag && true;
        } else {
            enableFlag = false;
        }
    }

    // If existing, must select from list
    var existingRadioFlag = jQuery('#existingRadio').attr('checked');
    if (existingRadioFlag) {
        jQuery('#no_data_no_project_warning').hide();
        var projectId = jQuery('#existing_project_select option:selected').val();
        if (projectId > 0) {
            enableFlag = enableFlag && true;
        } else {
            enableFlag = false;
        }
    }

    // If choose later, no_data isn't an option
    var laterRadioFlag = jQuery('#laterRadio').attr('checked');
    if (laterRadioFlag) {
        var noData =  jQuery('#txToUploadGroupNo').attr('checked');
        if (noData) {
            enableFlag = false;
            jQuery('#no_data_no_project_warning').show();
        } else {
            enableFlag = enableFlag && true;
        }
    }

    if (enableFlag) {
        enableContinueButton();
    } else {
        disableContinueButton();
    }
}

function checkUploadButton() {
    var enableFlag = !jQuery('#anonRevealsButton').attr('checked');

    // File must be specified...
    if ((jQuery('#fileName').val() != undefined)
        && (jQuery('#fileName').val() != "")) {
        enableFlag = enableFlag && true;
    } else {
        enableFlag = enableFlag && false;
    }

    // "IRB Requirements met" must be checked
    var irbChecked = jQuery('#irbRequirementsMet').attr('checked');
    enableFlag = enableFlag && irbChecked;

    if (enableFlag) {
        enableUploadButton();
    } else {
        disableUploadButton();
    }
}

function enableUploadButton() {
    jQuery( "button" ).button( "option", "disabled", false );
}
function disableUploadButton() {
    jQuery( "button" ).button( "option", "disabled", true );
}

function enableContinueButton() {
    jQuery( "button" ).button( "option", "disabled", false );
}
function disableContinueButton() {
    jQuery( "button" ).button( "option", "disabled", true );
}

function backButton() {
    var myForm = document.forms['verify_upload_dataset_form'];
    var newInput = document.createElement('input');
    newInput.name="upload_dataset_action";
    newInput.type="hidden";
    newInput.value="back";
    myForm.appendChild(newInput);
    document.forms['verify_upload_dataset_form'].submit();
}

function continueButton() {
    var myForm = document.forms['verify_upload_dataset_form'];
    var newInput = document.createElement('input');
    newInput.name="upload_dataset_action";
    newInput.type="hidden";
    newInput.value="verify";
    myForm.appendChild(newInput);
    document.forms['verify_upload_dataset_form'].submit();
}

//----- Recent Dataset Names Dialog -----

function rdnOpenDialog() {
    var dialogDiv = document.getElementById('recentDatasetNamesDialog');
    dialogDiv.innerHTML = "";

    var newSelect = document.createElement('select');
    newSelect.id   = "rdnSelect";
    newSelect.name = "rdnSelect";
    newSelect.size = 10;

    var hiddenSelect = document.getElementById('recentDatasetNames');
    for (var i = 0; i < hiddenSelect.length; i++){
        var option = document.createElement('option');
        option.value = hiddenSelect.options[i].value;
        option.appendChild(document.createTextNode(hiddenSelect.options[i].text));
        newSelect.appendChild(option);
    }

    dialogDiv.appendChild(newSelect);

    jQuery('#rdnSelect').change(rdnSelectChanged);

    jQuery('#recentDatasetNamesDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Recent Dataset Names",
        buttons : [ {
            id : "rdn-okay-button",
            text : "Okay",
            click : rdnOkayButton
        }, {
            id : "rdn-cancel-button",
            text : "Cancel",
            click : rdnCancelButton
        } ]
    });

    jQuery('#rdn-okay-button').button('disable');
    jQuery('#recentDatasetNamesDialog').dialog('open');
}

function rdnSelectChanged() {
    jQuery('#rdn-okay-button').button('enable');
}

function rdnOkayButton() {
    jQuery(this).dialog("close");
    var e = $('rdnSelect');
    $('datasetName').value = e.options[e.selectedIndex].text;
}

function rdnCancelButton() {
    jQuery(this).dialog("close");
}

//----- Recent Descriptions Dialog -----

function rddOpenDialog() {
    var dialogDiv = document.getElementById('recentDescriptionsDialog');
    dialogDiv.innerHTML = "";

    var newSelect = document.createElement('select');
    newSelect.id   = "rddSelect";
    newSelect.name = "rddSelect";
    newSelect.size = 10;

    var hiddenSelect = document.getElementById('recentDescriptions');
    for (var i = 0; i < hiddenSelect.length; i++){
        var option = document.createElement('option');
        option.value = hiddenSelect.options[i].value;
        option.appendChild(document.createTextNode(hiddenSelect.options[i].text));
        newSelect.appendChild(option);
    }

    dialogDiv.appendChild(newSelect);

    jQuery('#rddSelect').change(rddSelectChanged);

    jQuery('#recentDescriptionsDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        title : "Recent Dataset Names",
        buttons : [ {
            id : "rdd-okay-button",
            text : "Okay",
            click : rddOkayButton
        }, {
            id : "rdd-cancel-button",
            text : "Cancel",
            click : rddCancelButton
        } ]
    });

    jQuery('#rdd-okay-button').button('disable');
    jQuery('#recentDescriptionsDialog').dialog('open');
}

function rddSelectChanged() {
    jQuery('#rdd-okay-button').button('enable');
}

function rddOkayButton() {
    jQuery(this).dialog("close");
    var e = $('rddSelect');
    $('datasetDesc').value = e.options[e.selectedIndex].value;
}

function rddCancelButton() {
    jQuery(this).dialog("close");
}

//------------------------------
//  DataShop-Edit role requests
//------------------------------
function requestDatashopEditRole(uploadDataset) {
    var dialogDiv = document.getElementById('requestEditRoleDialog');
    dialogDiv.innerHTML = "";

    var p = document.createElement('p');
    p.innerHTML = "<b>Please tell us about your intended use of DataShop.</b> " +
        "We'd be happy to give you permission to create projects, upload datasets, " +
        "and create new datasets from samples, but we want to verify that your intended use "
        + "is research-related.";

    dialogDiv.appendChild(p);

    var reason = document.createElement('textarea');
    reason.id = "requestReason";
    reason.name = "requestReason";
    reason.rows = 4;
    reason.cols = 60;
    dialogDiv.appendChild(reason);

    var reasonMaxLen = document.createElement('div');
    reasonMaxLen.id = "reasonMaxLen";
    var charsLeft = MaxReasonLen - reason.value.length;
    reasonMaxLen.innerHTML = charsLeft + " characters remaining";
    dialogDiv.appendChild(reasonMaxLen);

    var br = document.createElement('br');
    dialogDiv.appendChild(br);

    var p2 = document.createElement('p');
    p2.innerHTML = "By clicking the button below, you agree not to post any " +
        "data that is in violation of the DataShop " +
        "<a href='Terms' target='_blank'>Terms of Use</a>.";

    dialogDiv.appendChild(p2);

    jQuery("textarea#requestReason").keyup(reasonModify);

    var dialogTitle = "Request Permissions";
    if (!uploadDataset) {
        dialogTitle = "Create Project";
    }

    jQuery('#requestEditRoleDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 575,
        height : 400,
        title : dialogTitle,
        buttons : [ {
            id : "request-edit-role-agree-button",
            text : "I agree. Please grant me access.",
            click : requestRole
        }, {
            id : "request-edit-role-cancel-button",
            text : "Cancel",
            click : closeRequestRoleDialog
        } ]
    });

    // Initially, button is disabled.
    jQuery("#request-edit-role-agree-button").button('disable');

    jQuery('#requestEditRoleDialog').dialog('open');
}

function reasonModify() {

    var enableFlag = false;

    var reasonFieldLen = jQuery("textarea#requestReason").val().length;

    // Reason field must be specified...
    if (reasonFieldLen > 0) {
        enableFlag = true;
    }

    // ... and less than MaxReasonLen characters.
    if (reasonFieldLen > MaxReasonLen) {
        jQuery("div#reasonMaxLen").css("color", "red");
        enableFlag = false;
    } else {
        jQuery("div#reasonMaxLen").css("color", "black");
        enableFlag = enableFlag && true;
    }

    var charsLeft = MaxReasonLen - jQuery("textarea#requestReason").val().length;
    // Update the chars remaining
    jQuery("div#reasonMaxLen").html(charsLeft + " characters remaining");

    if (enableFlag) {
        jQuery("#request-edit-role-agree-button").button('enable');
    } else {
        jQuery("#request-edit-role-agree-button").button('disable');
    }
}

// Submit the form that will send the request for the tool role.
function requestRole() {

    closeRequestRoleDialog();

    var theReason = jQuery("textarea#requestReason").val();

    new Ajax.Request("UploadDataset", {
        parameters: {
            requestingMethod: "UploadDataset.requestRole",
            requestRole: "true",
            requestReason: theReason
        },
        onComplete: handleRequestResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleRequestResponse(transport) {
    var json = transport.responseText.evalJSON(true);

    // Update the appropriate links...
    var a = jQuery('#upload-dataset-navlink');
    if (a) {
        a.attr("href", "javascript:roleRequestPending(true)");
    }
    a = jQuery('#create-project-navlink');
    if (a) {
        a.attr("href", "javascript:roleRequestPending(false)");
    }
    a = jQuery('#uploadDatasetButtonLink');
    if (a) {
        a.attr("href", "javascript:roleRequestPending(true)");
    }
    a = jQuery('#createProjectButtonLink');
    if (a) {
        a.attr("href", "javascript:roleRequestPending(false)");
    }
    a = jQuery('#upload-dataset-link');
    if (a) {
        a.attr("href", "javascript:roleRequestPending(true)");
    }

    successPopup(json.message);
}

function closeRequestRoleDialog() {
    jQuery('#requestEditRoleDialog').dialog('close');
}

function roleRequestPending(uploadDataset) {
    var dialogDiv = document.getElementById('requestPendingDialog');
    dialogDiv.innerHTML = "";

    var p = document.createElement('p');
    p.innerHTML = "Access to add datasets and projects is pending.";

    dialogDiv.appendChild(p);

    var dialogTitle = "Request Permissions";
    if (!uploadDataset) {
        dialogTitle = "Create Project";
    }

    jQuery('#requestPendingDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 510,
        height : 260,
        title : dialogTitle,
        buttons : [ {
            id : "pending-ok-button",
            text : "OK",
            click : closeRequestPendingDialog
        } ]
    });

    jQuery('#requestPendingDialog').dialog('open');
}

function closeRequestPendingDialog() {
    jQuery('#requestPendingDialog').dialog('close');
}

function fileFormatChanged() {
    // User changed file format (XML v. tab-delimited)
    var selectedFormat = jQuery('#format_select').val();
    var label = jQuery('#anonExceptLabel');
    if (selectedFormat == "tab_delimited") {
        jQuery('#anonExceptLabel').html("I certify that all data in this file <strong>except</strong> "
                                        + "the content of the \"Anon Student Id\" column is anonymized.");
        jQuery('#anonExceptNote').html("DataShop will de-identify the \"Anon Student Id\" column.");
        jQuery('#anonIncludingLabel').html("I certify that all data in this file <strong>including</strong> "
                                        + "the content of the \"Anon Student Id\" column is anonymized.");
    } else if (selectedFormat == "xml") {
        jQuery('#anonExceptLabel').html("I certify that all data in this file <strong>except</strong> "
                                        + "the content of the \"user_id\" element is anonymized.");
        jQuery('#anonExceptNote').html("DataShop will de-identify the content of the \"user_id\" element.");
        jQuery('#anonIncludingLabel').html("I certify that all data in this file <strong>including</strong> "
                                        + "the content of the \"user_id\" element is anonymized.");
    }
}

//------------------------------
//  'Create Project' page
//------------------------------
function enableCreateButton() {
    var projectName = jQuery('#new_project_name').val();
    if (projectName.length > 0) {
        jQuery('#create_button').button('enable');
    } else {
        jQuery('#create_button').button('disable');
    }
}
