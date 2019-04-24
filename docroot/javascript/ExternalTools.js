//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2012
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 10683 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-02-28 12:27:51 -0500 (Fri, 28 Feb 2014) $
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

        var message = jQuery('#extToolMessage').val();
        var messageLevel = jQuery('#extToolMessageLevel').val();

        if ((message != undefined) && (message != "null")) {
            if (messageLevel == "SUCCESS") {
                successPopup(message);
            } else if (messageLevel == "ERROR") {
                errorPopup(message);
            } else {
                messagePopup(message);
            }
        }
});

// ----- REQUEST ADD TOOL ROLE DIALOG -----

function openRequestExtToolRoleDialog() {
    var dialogDiv = document.getElementById('requestRoleDialog');
    dialogDiv.innerHTML = "";

    newInput = document.createElement('p');
    newInput.innerHTML = "<b>Please tell us about your intended use of DataShop.</b> " +
        "We'd be happy to give you permission to add tools, but we want to verify that " +
        "your intended use is research-related.";
    dialogDiv.appendChild(newInput);

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

    jQuery('#requestRoleDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 575,
        height : 400,
        title : "Add Tool",
        buttons : [ {
            id : "request-tool-role-agree-button",
            text : "I agree. Please grant me access.",
            click : requestRole
        }, {
            id : "request-tool-role-cancel-button",
            text : "Cancel",
            click : closeRequestExtToolRoleDialog
        } ]
    });

    // Initially, button is disabled.
    jQuery("#request-tool-role-agree-button").button('disable');

    jQuery('#requestRoleDialog').dialog('open');
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
        jQuery("#request-tool-role-agree-button").button('enable');
    } else {
        jQuery("#request-tool-role-agree-button").button('disable');
    }
}

// Method called from the bottom of the JSP
function init() {
    initTruncatorFields();
}

// Initialize the fields to be truncated
function initTruncatorFields() {
    options = new Array();
    options.maxLength = 50;
    options.numLines = 2;
    jQuery("#externalToolsTable span[name='tool_name']").each(function () {
        new Truncator(this, options);    
    });
    options.maxLength = 75;
    options.numLines = 3;
    jQuery("#externalToolsTable span[name='tool_desc']").each(function () {
        new Truncator(this, options);    
    });
}

// Submit the form that will send the request for the tool role.
function requestRole() {
    jQuery('#requestRoleDialog').dialog('close');

    var theReason = jQuery("textarea#requestReason").val();

    var newForm = document.createElement('form');
    newForm.id   = "requestToolRoleForm";
    newForm.name = "requestToolRoleForm";
    newForm.action = "ExternalTools";
    newForm.method = "post";
    newForm.form   = "text/plain";

    var newInput = document.createElement('input');
    newInput.id   = "externalToolsAction";
    newInput.name = "externalToolsAction";
    newInput.value = "RequestRole";
    newInput.type  = "hidden";
    newForm.appendChild(newInput);

    var input2   = document.createElement('input');
    input2.id    = "requestReason";
    input2.name  = "requestReason";
    input2.type  = "hidden";
    input2.value = theReason;
    newForm.appendChild(input2);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// A simple close modal dialog function.
function closeRequestExtToolRoleDialog() {
    jQuery(this).dialog("close");
}

// ----- ADD TOOL DIALOG -----

// Open the Add Tool dialog
function openAddToolDialog() {
    // Start by getting list of existing tools for this user.
    new Ajax.Request("ExternalTools", {
        parameters: {
            requestingMethod: "ExternalTools.getToolListByOwner",
            externalToolsAction: "getToolListByOwner"
        },
        onComplete: setupAddToolDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function setupAddToolDialog(transport) {

    // Build array of tool names...
    var json = transport.responseText.evalJSON(true);
    var toolList = json.toolList;

    toolNames = [];
    for (var key in toolList) {
        if (toolList.hasOwnProperty(key)) {
            toolNames.push(key);
        }
    }

    var dialogDiv = document.getElementById('addToolDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "addToolForm";
    newForm.name = "addToolForm";
    newForm.action = "ExternalTools";
    newForm.method = "post";
    newForm.form   = "text/plain";

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "externalToolsAction";
    hiddenInput.name = "externalToolsAction";
    hiddenInput.value = "AddTool";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    // Name of tool
    var nameLabel = document.createElement('p');
    nameLabel.innerHTML = "Name";
    newForm.appendChild(nameLabel);

    var nameField = document.createElement('input');
    nameField.id = "toolNameField";
    nameField.name = "toolNameField"
    nameField.className = "toolNameField";
    nameField.size = 50;
    newForm.appendChild(nameField);

    var nameMaxLen = document.createElement('div');
    nameMaxLen.id = "toolNameMaxLen";
    nameMaxLen.innerHTML = "Enter a unique name that is no more than 255 characters.";
    newForm.appendChild(nameMaxLen);

    // Description of tool
    var descLabel = document.createElement('p');
    descLabel.innerHTML = "Description";
    newForm.appendChild(descLabel);

    var descField = document.createElement('textarea');
    descField.id   = "toolDescField";
    descField.name = "toolDescField";
    descField.rows = 4;
    descField.cols = 20;
    newForm.appendChild(descField);

    var descMaxLen = document.createElement('div');
    descMaxLen.id = "toolDescMaxLen";
    descMaxLen.innerHTML = "Enter no more than 500 characters.";
    newForm.appendChild(descMaxLen);

    // Language of tool
    var langLabel = document.createElement('p');
    langLabel.innerHTML = "Language";
    newForm.appendChild(langLabel);

    var langField = document.createElement('input');
    langField.id = "toolLangField";
    langField.name = "toolLangField";
    langField.size = 30;
    newForm.appendChild(langField);

    // Project homepage of tool
    var pageLabel = document.createElement('p');
    pageLabel.innerHTML = "Home page";
    newForm.appendChild(pageLabel);

    var pageField = document.createElement('input');
    pageField.id = "toolPageField";
    pageField.name = "toolPageField";
    pageField.size = 50;
    newForm.appendChild(pageField);

    // Note before create button
    var noteLabel = document.createElement('p');
    noteLabel.className = "addFilesNote";
    noteLabel.innerHTML = "You can add files after you've created a page for the tool.";
    newForm.appendChild(noteLabel);

    // Finally, attach the form to the div...
    dialogDiv.appendChild(newForm);

    jQuery('#addToolDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 540,
        title : "Add Tool",
        buttons : [ {
            id : "create-tool-button",
            text : "Create",
            click : addTool
        }, {
            id : "cancel-create-tool-button",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    jQuery('#create-tool-button').button('disable');
    jQuery('#toolNameField').keyup(enableCreateButton);
    jQuery('#toolDescField').keyup(enableCreateButton);
    jQuery('#toolLangField').keyup(enableCreateButton);
    jQuery('#toolPageField').keyup(enableCreateButton);
    jQuery('#addToolDialog').dialog('open');
}

function enableCreateButton() {
    var nameStr = jQuery("input#toolNameField").val();
    var nameFlag = nameStr.length <= 255;
    var nameGiven = nameStr.length > 0;
    var descFlag = jQuery("textarea#toolDescField").val().length <= 500;
    var langFlag = jQuery("input#toolLangField").val().length <= 255;
    var pageFlag = jQuery("input#toolPageField").val().length <= 255;

    if ($A(toolNames).indexOf(nameStr) >= 0) {
        // Tool by that name already exists.
        nameFlag = false;
    }

    if (nameFlag) {
        jQuery("#toolNameMaxLen").css('color', '#333');
    }
    if (descFlag) {
        jQuery("#toolDescMaxLen").css('color', '#333');
    }
    if (nameFlag && descFlag && langFlag && pageFlag && nameGiven) {
        jQuery('#create-tool-button').button('enable');
    } else {
        jQuery('#create-tool-button').button('disable');
        if (!nameFlag) {
            jQuery("#toolNameMaxLen").css('color', '#E00000');
        }
        if (!descFlag) {
            jQuery("#toolDescMaxLen").css('color', '#E00000');
        }
    }
}

// Submit the form that will add the tool to the repository.
function addTool() {
    jQuery('#addToolForm').submit();
    jQuery('#addToolDialog').dialog('close');
}

// A simple close modal dialog function.
function closeDialog() {
    jQuery(this).dialog("close");
}

// ----- SORT COLUMNS in TOOL TABLE -----

function sortTools(sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id = "sort_tools_form";
    newForm.name = "sort_tools_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "sort";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- SORT COLUMNS in FILES TABLE ON SINGLE-TOOL VIEW PAGE -----

function sortFiles(toolId, sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id = "sort_files_form";
    newForm.name = "sort_files_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "sort";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "toolId";
    newInput.type  = "hidden";
    newInput.value = toolId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- UPLOAD FILE to a tool DIALOG -----

// Open the Upload File dialog for tools
function openUploadToolFileDialog(toolId) {
    var dialogDiv = document.getElementById('uploadToolFileDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "uploadToolFileForm";
    newForm.name = "uploadToolFileForm";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";
    newForm.enctype  = "multipart/form-data";
    newForm.encoding = "multipart/form-data";  // IE7
    newForm.onsubmit = "";

    var newInput = document.createElement('input');
    newInput.id = "externalToolsAction";
    newInput.name = "externalToolsAction";
    newInput.value = "UploadFile";
    newInput.type = "hidden";
    newForm.appendChild(newInput);

    newInput = document.createElement('p');
    newInput.innerHTML = "Choose a file to upload *";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name = "fileName";
    newInput.type = "file";
    newInput.id = "tool-file-chooser";
    newForm.appendChild(newInput);

    // Attach the form to the div
    dialogDiv.appendChild(newForm);

    jQuery('#uploadToolFileDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 200,
        title : "Upload File",
        buttons : [ {
            id : "create-tool-file-button",
            text : "Upload",
            click : uploadToolFile
        }, {
            id : "cancel-create-tool-file-button",
            text : "Cancel",
            click : closeUploadToolFileDialog
        } ]
    });

    jQuery('#uploadToolFileDialog').dialog('open');
}

// Submit the form that will add a file to a tool
function uploadToolFile() {
    jQuery('#uploadToolFileForm').submit();
    jQuery('#uploadToolFileDialog').dialog('close');
}

// Close the Upload File dialog for tools
function closeUploadToolFileDialog() {
    jQuery(this).dialog("close");
}

// ----- DOWNLOAD SINGLE FILE -----

// Submit form to download the given file for the given tool
function downloadFile(toolId, fileId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "download_tool_file_form";
    newForm.name = "download_tool_file_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "DownloadFile";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "toolId";
    newInput.type  = "hidden";
    newInput.value = toolId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "fileId";
    newInput.type  = "hidden";
    newInput.value = fileId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- DOWNLOAD ALL FILES -----

// Submit form to download all the files for the given tool
function downloadAllFiles(toolId, fileId) {
    var newForm = document.createElement('FORM');
    newForm.id   = "download_tool_file_form";
    newForm.name = "download_tool_file_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "DownloadAllFiles";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "toolId";
    newInput.type  = "hidden";
    newInput.value = toolId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "fileId";
    newInput.type  = "hidden";
    newInput.value = fileId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- DELETE TOOL METHODS -----

// Hide link div, show 'Are you sure' div
function showToolDeleteSureDiv() {
    toolSureDiv = $("toolDeleteSureDiv");
    toolSureDiv.style.display = "inline";

    toolLinkDiv = $("toolDeleteLinkDiv");
    toolLinkDiv.style.display = "none";
}

// Show link div, hide 'Are you sure' div
function showToolDeleteLinkDiv() {
    toolSureDiv = $("toolDeleteSureDiv");
    toolSureDiv.style.display = "none";

    toolLinkDiv = $("toolDeleteLinkDiv");
    toolLinkDiv.style.display = "inline";
}

// Submit form to delete the given tool
function deleteTool(toolId) {
    showToolDeleteLinkDiv(toolId);

    var newForm = document.createElement('FORM');
    newForm.id   = "delete_tool_form";
    newForm.name = "delete_tool_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "DeleteTool";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "toolId";
    newInput.type  = "hidden";
    newInput.value = toolId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- DELETE FILE METHODS -----

// Hide/Show File Sure/Image Divs
function showFileDeleteSureDiv(fileId) {
    fileSureDiv = $("fileDeleteSureDiv_" + fileId);
    fileSureDiv.style.display = "inline";

    fileImageDiv = $("fileDeleteImageDiv_" + fileId);
    fileImageDiv.style.display = "none";
}

// Hide/Show File Sure/Image Divs
function showFileDeleteImageDiv(fileId) {
    fileSureDiv = $("fileDeleteSureDiv_" + fileId);
    fileSureDiv.style.display = "none";

    fileImageDiv = $("fileDeleteImageDiv_" + fileId);
    fileImageDiv.style.display = "inline";
}

// Submit form to delete the given file from the given tool
function deleteFile(toolId, fileId) {
    showFileDeleteImageDiv(fileId);

    var newForm = document.createElement('FORM');
    newForm.id   = "delete_tool_file_form";
    newForm.name = "delete_tool_file_form";
    newForm.form = "text/plain";
    newForm.action = "ExternalTools?toolId=" + toolId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "externalToolsAction";
    newInput.type  = "hidden";
    newInput.value = "DeleteToolFile";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "toolId";
    newInput.type  = "hidden";
    newInput.value = toolId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "fileId";
    newInput.type  = "hidden";
    newInput.value = fileId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// ----- EDIT NAME FIELD on TOOL PAGE METHODS -----

function removeStatusIndicator() {
    // Get rid of any status indicator on this page...
    var statusIndicator = $('status_indicator');
    if (statusIndicator != null) statusIndicator.style.display = "none";
}

function showEditToolNameField() {
    removeStatusIndicator();

    editDiv = $("toolPageEditNameDiv");
    editDiv.style.display = "inline";

    viewDiv = $("toolPageViewNameDiv");
    viewDiv.style.display = "none";
}

function hideEditToolNameField() {
    editDiv = $("toolPageEditNameDiv");
    editDiv.style.display = "none";

    viewDiv = $("toolPageViewNameDiv");
    viewDiv.style.display = "inline";
}

function cancelToolNameChanges() {
    hiddenField = $("toolPageNameHiddenField");
    inputField = $("toolNameField");
    inputField.value = hiddenField.value;

    hideEditToolNameField();
}

function saveToolNameChanges(toolId) {
    inputField = $("toolNameField");

    new Ajax.Request("ExternalTools?toolId="+toolId, {
        parameters: {
            requestingMethod: "ExternalTools.saveToolNameChanges",
            externalToolsAction: "editName",
            toolId: toolId,
            ajaxRequest: "true",
            toolField: "name",
            toolValue: inputField.value
        },
        onComplete: handleSaveToolNameResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveToolNameResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("toolPageViewNameSpan");
        hiddenField = $("toolPageNameHiddenField");
        shownField.innerHTML = json.value;
        hiddenField.value = json.value;
        hideEditToolNameField();
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

// ----- EDIT DESCRIPTION FIELD on TOOL PAGE METHODS -----

function showEditToolDescriptionField() {
    removeStatusIndicator();

    editDiv = $("toolPageEditDescriptionDiv");
    editDiv.style.display = "inline";

    viewDiv = $("toolPageViewDescriptionDiv");
    viewDiv.style.display = "none";

    // Track changes to description textarea...
    jQuery('#toolDescriptionField').keyup(enableDescriptionSaveButton);
}
function enableDescriptionSaveButton() {
    var descLen = jQuery("#toolDescriptionField").val().length;

    if (descLen > 500) {
    jQuery('#toolPageDescriptionMaxLen').css('color', '#E00000');
    jQuery('#toolPageDescriptionSaveButton').prop('disabled', true);
    } else {
    jQuery('#toolPageDescriptionMaxLen').css('color', '#333');
    jQuery('#toolPageDescriptionSaveButton').prop('disabled', false);
    }
}
function hideEditToolDescriptionField() {
    editDiv = $("toolPageEditDescriptionDiv");
    editDiv.style.display = "none";

    viewDiv = $("toolPageViewDescriptionDiv");
    viewDiv.style.display = "inline";
}
function cancelToolDescriptionChanges() {
    // Reset text color and button
    jQuery('#toolPageDescriptionMaxLen').css('color', '#333');
    jQuery('#toolPageDescriptionSaveButton').prop('disabled', false);

    hiddenField = $("toolPageDescriptionHiddenField");
    inputField = $("toolDescriptionField");
    inputField.value = hiddenField.value;

    hideEditToolDescriptionField();
}
function saveToolDescriptionChanges(toolId) {
    // Reset text color and button
    jQuery('#toolPageDescriptionMaxLen').css('color', '#333');
    jQuery('#toolPageDescriptionSaveButton').prop('disabled', false);

    inputField = $("toolDescriptionField");

    new Ajax.Request("ExternalTools?toolId="+toolId, {
        parameters: {
            requestingMethod: "ExternalTools.saveToolDescriptionChanges",
            externalToolsAction: "editDescription",
            toolId: toolId,
            ajaxRequest: "true",
            toolField: "name",
            toolValue: inputField.value
        },
        onComplete: handleSaveToolDescriptionResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveToolDescriptionResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    shownField = $("toolPageViewDescriptionSpan");
    hiddenField = $("toolPageDescriptionHiddenField");
    shownField.innerHTML = nl2br(fixUrls(json.value));
    hiddenField.value = json.value.replace(/\"/g, "&quot;");;
    hideEditToolDescriptionField();
    if (json.level == "SUCCESS") {
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

// ----- EDIT LANGUAGE FIELD on TOOL PAGE METHODS -----

function showEditToolLanguageField() {
    removeStatusIndicator();

    editDiv = $("toolPageEditLanguageDiv");
    editDiv.style.display = "inline";

    viewDiv = $("toolPageViewLanguageDiv");
    viewDiv.style.display = "none";
}
function hideEditToolLanguageField() {
    editDiv = $("toolPageEditLanguageDiv");
    editDiv.style.display = "none";

    viewDiv = $("toolPageViewLanguageDiv");
    viewDiv.style.display = "inline";
}
function cancelToolLanguageChanges() {
    hiddenField = $("toolPageLanguageHiddenField");
    inputField = $("toolLanguageField");
    inputField.value = hiddenField.value;

    hideEditToolLanguageField();
}
function saveToolLanguageChanges(toolId) {
    inputField = $("toolLanguageField");

    new Ajax.Request("ExternalTools?toolId="+toolId, {
        parameters: {
            requestingMethod: "ExternalTools.saveToolLanguageChanges",
            externalToolsAction: "editLanguage",
            toolId: toolId,
            ajaxRequest: "true",
            toolField: "name",
            toolValue: inputField.value
        },
        onComplete: handleSaveToolLanguageResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveToolLanguageResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("toolPageViewLanguageSpan");
        hiddenField = $("toolPageLanguageHiddenField");
        shownField.innerHTML = json.value;
        hiddenField.value = json.value;
        hideEditToolLanguageField();
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

// ----- EDIT HOME PAGE FIELD on TOOL PAGE METHODS -----

function showEditToolHomePageField() {
    removeStatusIndicator();

    editDiv = $("toolPageEditHomePageDiv");
    editDiv.style.display = "inline";

    viewDiv = $("toolPageViewHomePageDiv");
    viewDiv.style.display = "none";
}

function hideEditToolHomePageField() {
    editDiv = $("toolPageEditHomePageDiv");
    editDiv.style.display = "none";

    viewDiv = $("toolPageViewHomePageDiv");
    viewDiv.style.display = "inline";
}

function cancelToolHomePageChanges() {
    hiddenField = $("toolPageHomePageHiddenField");
    inputField = $("toolHomePageField");
    inputField.value = hiddenField.value;

    hideEditToolHomePageField();
}

function saveToolHomePageChanges(toolId) {
    inputField = $("toolHomePageField");

    new Ajax.Request("ExternalTools?toolId="+toolId, {
        parameters: {
            requestingMethod: "ExternalTools.saveToolHomePageChanges",
            externalToolsAction: "editHomePage",
            toolId: toolId,
            ajaxRequest: "true",
            toolField: "name",
            toolValue: inputField.value
        },
        onComplete: handleSaveToolHomePageResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveToolHomePageResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownFieldLink = $("toolPageViewHomePageSpan").firstDescendant();
        hiddenField = $("toolPageHomePageHiddenField");
        var webPage = json.value;
        var webPageToLink = webPage;
        if (!webPage.startsWith("http://") && !webPage.startsWith("https://")) {
            webPageToLink = "http://" + webPage;
        }
        shownFieldLink.innerHTML = webPage;
        shownFieldLink.href = webPageToLink;
        hiddenField.value = webPage;
        hideEditToolHomePageField();
        successPopup(json.message);
    } else {
        errorPopup(json.message);
    }
}

// Count clicks on the 'Home page' link as downloads for the tool.
function countHomePageClicks(toolId) {
    new Ajax.Request("ExternalTools?toolId="+toolId, {
        parameters: {
            requestingMethod: "ExternalTools.countHomePageClicks",
            externalToolsAction: "incrementDownloadsCounter",
            toolId: toolId,
            ajaxRequest: "true"
        },
        onComplete: handleHomePageClickResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleHomePageClickResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        downloadCountSpan = $('downloadCount_' + json.toolId);
        downloadCountSpan.innerHTML = json.value;
    } else {
        errorPopup(json.message);
    }
}
