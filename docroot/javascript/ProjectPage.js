//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2011
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 15593 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-10-16 16:49:25 -0400 (Tue, 16 Oct 2018) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        projectId = getProjectId();

        jQuery("#piNameField").hint("auto-hint");
        jQuery("#dpNameField").hint("auto-hint");

        jQuery("#project_tou_select").change(enableApplyToU);
});

//---------------------------
// Appears Anonymous changes
//---------------------------
function pdDisplayEditModeDatasets() {
    jQuery('#datasetsEditDiv').hide();
    jQuery('#datasetsSaveCancelDiv').show();
    
    var viewDivs = jQuery("div[name*='pd_div_view_']");
    var editDivs = jQuery("div[name*='pd_div_edit_']");
    
    for (i = 0; i < viewDivs.length; i++) {
        jQuery(viewDivs[i]).hide();
    }
    for (i = 0; i < editDivs.length; i++) {
        jQuery(editDivs[i]).show();
    }
}

function pdDisplayViewModeDatasets() {
    jQuery('#datasetsEditDiv').show();
    jQuery('#datasetsSaveCancelDiv').hide();
    
    var viewDivs = jQuery("div[name*='pd_div_view_']");
    var editDivs = jQuery("div[name*='pd_div_edit_']");

    for (i = 0; i < viewDivs.length; i++) {
        jQuery(viewDivs[i]).show();
    }
    for (i = 0; i < editDivs.length; i++) {
        jQuery(editDivs[i]).hide();
    }
}

function pdCancDatasetChanges() {

    var appearsAnonDivs = jQuery("div[id*='pd_appears_anon_div_view']");
    var appearsAnonSelects = jQuery("select[id*='pd_appears_anon_select']");
    for (i = 0; i < appearsAnonDivs.length; i++) {
        jQuery(appearsAnonSelects[i]).val(jQuery(appearsAnonDivs[i]).html());
    }
    
    var irbUploadedDivs = jQuery("div[id*='pd_irb_uploaded_div_view']");
    var irbUploadedSelects = jQuery("select[id*='pd_irb_uploaded_select']");
    for (i = 0; i < irbUploadedDivs.length; i++) {
        jQuery(irbUploadedSelects[i]).val(jQuery(irbUploadedDivs[i]).html());
    }
    
    var hasStudyDataDivs = jQuery("div[id*='pd_has_study_data_div_view']");
    var hasStudyDataSelects = jQuery("select[id*='pd_has_study_data_select']");
    for (i = 0; i < hasStudyDataDivs.length; i++) {
        jQuery(hasStudyDataSelects[i]).val(jQuery(hasStudyDataDivs[i]).html());
    }

    pdDisplayViewModeDatasets();
}

function pdSaveDatasetChanges() {
    //format of theData is: "1,yes,TBD,not specified-2,no,N/A,yes";
    var theData = "";

    // get the data and save changes
    var appearsAnonSelects  = jQuery("select[id*='pd_appears_anon_select']");
    var irbUploadedSelects  = jQuery("select[id*='pd_irb_uploaded_select']");
    var hasStudyDataSelects = jQuery("select[id*='pd_has_study_data_select']");
    
    for (i = 0; i < appearsAnonSelects.length; i++) {
        var aaSelect = jQuery(appearsAnonSelects[i]);
        var iuSelect = jQuery(irbUploadedSelects[i]);
        var hsSelect = jQuery(hasStudyDataSelects[i]);
        var idArr = aaSelect.attr("id").split("_");
        var id = idArr[idArr.length-1];
        theData += id + "," + aaSelect.val();
        theData += "," + iuSelect.val();
        theData += "," + hsSelect.val();
        theData += "-";
    }
    
    new Ajax.Request("Project?id=" + projectId, {
        parameters: {
            requestingMethod: "ProjectPage.pdSaveDatasetChanges",
            ajaxAction: "saveDatasetChanges",
            projectId: projectId,
            theData: theData,
            ajaxRequest: "true"
        },
        onComplete: pdHandleSaveDatasetChanges,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function pdHandleSaveDatasetChanges(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.flag == "success") {
        successPopup(json.message);
        
        var appearsAnonDivs = jQuery("div[id*='pd_appears_anon_div_view']");
        var appearsAnonSelects = jQuery("select[id*='pd_appears_anon_select']");
        for (i = 0; i < appearsAnonDivs.length; i++) {
            jQuery(appearsAnonDivs[i]).html(jQuery(appearsAnonSelects[i]).val());
        }
        
        var irbUploadedDivs = jQuery("div[id*='pd_irb_uploaded_div_view']");
        var irbUploadedSelects = jQuery("select[id*='pd_irb_uploaded_select']");
        for (i = 0; i < irbUploadedDivs.length; i++) {
            jQuery(irbUploadedDivs[i]).html(jQuery(irbUploadedSelects[i]).val());
        }
        
        var hasStudyDataDivs = jQuery("div[id*='pd_has_study_data_div_view']");
        var hasStudyDataSelects = jQuery("select[id*='pd_has_study_data_select']");
        for (i = 0; i < hasStudyDataDivs.length; i++) {
            jQuery(hasStudyDataDivs[i]).html(jQuery(hasStudyDataSelects[i]).val());
        }
        
    } else {
        errorPopup(json.message);
    }
    pdDisplayViewModeDatasets();
}

//-----------------
// PI name changes
//-----------------
function showEditPiDiv() {
    $('projectInfoPiDiv').style.display = "none";
    $('projectInfoEditPiDiv').style.display = "inline";
}
function hideEditPiDiv() {
    $('projectInfoPiDiv').style.display = "inline";
    $('projectInfoEditPiDiv').style.display = "none";
}

function verifyPiUserName(projectId) {
    var piUserName = jQuery('#piNameField').val();

    if (piUserName == jQuery('#piNameField').attr('title')) {
        piUserName = "";
    }

    if (piUserName.length > 0) {
        // Verify userName and get full name.
        new Ajax.Request("Project", {
                parameters: {
                    requestingMethod: "ProjectPage.verifyPiUserName",
                        id: projectId,
                        ajaxRequest: "true",
                        verifyUserName: "true",
                        userName: piUserName
                        },
                    onComplete: openMakePiAdminDialog,
                    onException: function (request, exception) {
                    throw(exception);
                }
        });
    } else {
        // No verification needed; removing PI.
        savePiNameChange(projectId, false);
    }
}

function openMakePiAdminDialog(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        var fullUserName = json.fullUserName;

        var dialogDiv = jQuery('.makePiAdminDialog');
        dialogDiv.html("<p>Do you want to give " + fullUserName
                       + " the project admin role as well?</p>");

        // 'Make PI Admin' dialog from project page
        jQuery('#makePiAdminDialog').dialog({
                modal : true,
            autoOpen : false,
	        width : 450,
	        height : 200,
	        title : "Make PI Admin",
	        buttons : [ {
                        id : "make-pi-admin-button",
                        text : "Yes",
                        click : makePiAdmin
                    }, {
                        id : "change-pi-only-button",
                        text : "No",
                        click : changePiOnly
	        } ]
            });

        if (!json.alreadyAdmin) {
            jQuery('#makePiAdminDialog').dialog('open');
        } else {
            savePiNameChange(projectId, false);
        }
    } else {
        // Revert change.
        setUserNameField("piNameField", $("piNameHiddenField").value);
        errorPopup(json.message);
    }
}

function makePiAdmin() {
    jQuery('.makePiAdminDialog').dialog('close');
    savePiNameChange(projectId, true);
}
function changePiOnly() {
    jQuery('.makePiAdminDialog').dialog('close');
    savePiNameChange(projectId, false);
}

function savePiNameChange(projectId, makePiAdminVar) {
    var piUserName = jQuery('#piNameField').val();

    if (piUserName == jQuery('#piNameField').attr('title')) {
        piUserName = "";
    }

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.savePiNameChange",
            id: projectId,
            ajaxRequest: "true",
            editDatasetsPage: "true",
            piName: piUserName,
            makePiAdmin: makePiAdminVar
        },
        onComplete: handleSavePiNameResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSavePiNameResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("projectInfoPiSpan");
        hiddenField = $("piNameHiddenField");
        shownField.innerHTML = json.value;
        var piUserId = $("piNameField").value;
        setUserNameField("piNameField", piUserId);
        hiddenField.value = piUserId;
        updatePiDivs(json.value);
        successPopup(json.message);
    } else {
        // Revert change.
        setUserNameField("piNameField", $("piNameHiddenField").value);
        errorPopup(json.message);
    }
}
function setUserNameField(fieldId, value) {
    if (value == $(fieldId).readAttribute('title')) {
        value = "";
    }
    if (value.length > 0) {
        $(fieldId).value = value;
        $(fieldId).removeAttribute("class");
    } else {
        $(fieldId).title = "Enter username";
        $(fieldId).removeAttribute("value");
        $(fieldId).className = "auto-hint";
    }
}

function updatePiDivs(value) {
    hideEditPiDiv();
    updateDivClass('projectInfoPiDiv', value);
}

function cancelPiNameChange() {
    var piUserId = $("piNameHiddenField").value;
    setUserNameField("piNameField", piUserId);
    hideEditPiDiv();
}

//-----------------
// DP name changes
//-----------------
function showEditDpDiv() {
    $('projectInfoDpDiv').style.display = "none";
    $('projectInfoEditDpDiv').style.display = "inline";
}
function hideEditDpDiv() {
    $('projectInfoDpDiv').style.display = "inline";
    $('projectInfoEditDpDiv').style.display = "none";
}

function saveDpNameChange(projectId) {
    var dpUserName = $("dpNameField").value;

    if (dpUserName == $('dpNameField').readAttribute('title')) {
        dpUserName = "";
    }

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.saveDpNameChange",
            id: projectId,
            ajaxRequest: "true",
            editDatasetsPage: "true",
            dpName: dpUserName
        },
        onComplete: handleSaveDpNameResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveDpNameResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("projectInfoDpSpan");
        hiddenField = $("dpNameHiddenField");
        shownField.innerHTML = json.value;
        var dpUserId = $("dpNameField").value;
        setUserNameField("dpNameField", dpUserId);
        hiddenField.value = dpUserId;
        updateDpDivs(json.value);
        successPopup(json.message);
    } else {
        // Revert change.
        setUserNameField("dpNameField", $("dpNameHiddenField").value);
        errorPopup(json.message);
    }
}
function updateDpDivs(value) {
    hideEditDpDiv();
    updateDivClass('projectInfoDpDiv', value);
}

function cancelDpNameChange() {
    var dpUserId = $("dpNameHiddenField").value;
    setUserNameField("dpNameField", dpUserId);
    hideEditDpDiv();
}

//---------------------
// Description changes
//---------------------
function showEditDescDiv() {
    infoDiv = $('projectInfoDescriptionDiv');
    if (infoDiv) {
        infoDiv.style.display = "none";
    } else {
        $('emptyDescDiv').style.display = "none";
    }
    $('projectInfoEditDescDiv').style.display = "block";
}
function hideEditDescDiv() {
    infoDiv = $('projectInfoDescriptionDiv');
    if (infoDiv) {
        infoDiv.style.display = "block";
    }
    $('projectInfoEditDescDiv').style.display = "none";
}

function saveDescChange(projectId) {
    inputField = $("descriptionField");

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.saveDescChange",
            id: projectId,
            ajaxRequest: "true",
            editDatasetsPage: "true",
            description: inputField.value
        },
        onComplete: handleSaveDescResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveDescResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("projectInfoDescriptionSpan");
        hiddenField = $("descHiddenField");
        shownField.innerHTML = nl2br(json.value);
        hiddenField.value = json.value.replace(/\"/g, "&quot;");
        updateDescDivs(json.value);
        successPopup(json.message);
    } else {
        // Revert change.
        $("descriptionField").value = $("descHiddenField").value;
        errorPopup(json.message);
    }
}
function updateDescDivs(value) {
    hideEditDescDiv();
    updateDivClass('projectInfoDescriptionDiv', value);
}

function cancelDescChange() {
    hideEditDescDiv();
    $("descriptionField").value = $("descHiddenField").value;
}

//--------------
// Tags changes
//--------------
function showEditTagsDiv() {
    $('projectInfoTagsDiv').style.display = "none";
    $('projectInfoEditTagsDiv').style.display = "inline";
}
function hideEditTagsDiv() {
    $('projectInfoTagsDiv').style.display = "inline";
    $('projectInfoEditTagsDiv').style.display = "none";
}

function saveTagsChange(projectId) {
    inputField = $("tagsField");

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.saveTagsChange",
            id: projectId,
            ajaxRequest: "true",
            editDatasetsPage: "true",
            tags: inputField.value
        },
        onComplete: handleSaveTagsResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveTagsResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = $("projectInfoTagsSpan");
        hiddenField = $("tagsHiddenField");
        shownField.innerHTML = json.value;
        hiddenField.value = json.value;
        updateTagDivs(json.value);
        successPopup(json.message);
    } else {
        // Revert change.
        $("tagsField").value = $("tagsHiddenField").value;
        errorPopup(json.message);
    }
}
function updateTagDivs(value) {
    hideEditTagsDiv();
    updateDivClass('projectInfoTagsDiv', value);
}

function cancelTagsChange() {
    hideEditTagsDiv();
    $("tagsField").value = $("tagsHiddenField").value;
}

//--------------
// Link changes
//--------------
function showEditLinkDiv(linkId) {
    $('projectInfoLinkDiv_' + linkId).style.display = "none";
    $('projectInfoEditLinkDiv_' + linkId).style.display = "inline";
}
function hideEditLinkDiv(linkId) {
    $('projectInfoLinkDiv_' + linkId).style.display = "block";
    $('projectInfoEditLinkDiv_' + linkId).style.display = "none";
}

function saveLinkChange(projectId, linkId) {
    titleField = $('linkTitleField_' + linkId);
    urlField = $('linkUrlField_' + linkId);

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.saveLinkChange",
            id: projectId,
            ajaxRequest: "true",
            editDatasetsPage: "true",
            externalLinkId: linkId,
            externalLinkTitle: titleField.value,
            externalLinkUrl: urlField.value
        },
        onComplete: handleSaveLinkResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveLinkResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    linkId = json.linkId;
    if (json.level == "SUCCESS") {
        shownField = $("projectInfoLinkSpan_" + linkId);
        hiddenTitleField = $("linkTitleHiddenField_" + linkId);
        hiddenUrlField = $("linkUrlHiddenField_" + linkId);
        linkUrl = json.valueUrl;
        if ((!linkUrl.startsWith("http://")) && (!linkUrl.startsWith("https://"))) {
            linkUrl = "http://" + linkUrl;
        }
        shownField.innerHTML = "<a href=\"" + linkUrl + "\" target=\'_blank\'>" + json.valueTitle + "</a>";
        hiddenTitleField.value = json.valueTitle;
        hiddenUrlField.value = json.valueUrl;
        hideEditLinkDiv(linkId);
        successPopup(json.message);
    } else {
        // Revert change.
        $("linkTitleField_" + linkId).value = $("linkTitleHiddenField_" + linkId).value;
        $("linkUrlField_" + linkId).value = $("linkUrlHiddenField_" + linkId).value;
        errorPopup(json.message);
    }
}

function cancelLinkChange(linkId) {
    hideEditLinkDiv(linkId);
    $("linkTitleField_" + linkId).value = $("linkTitleHiddenField_" + linkId).value;
    $("linkUrlField_" + linkId).value = $("linkUrlHiddenField_" + linkId).value;
}

function areYouSureExternalLink(linkId) {

    // Show 'Are you sure?' div...
    $('projectAreYouSureDiv_' + linkId).style.display = "inline";

    // ... and hide Edit and Delete divs.
    $('projectEditLinkLink_' + linkId).style.display = "none";
    $('projectDeleteLinkLink_' + linkId).style.display = "none";
}
function deleteExternalLink(linkId) {

    // Create and submit form to delete the external link.
    var theForm = document.createElement('form');
    theForm.id = "deleteExternalLinkForm";
    theForm.name = "deleteExternalLinkForm";
    theForm.action = "Project?id=" + projectId;
    theForm.method = "post";
    theForm.form = "text/plain";

    // Input that will trigger 'editDatasetsPage' in servlet
    var input1 = document.createElement('input');
    input1.id = "editDatasetsPage";
    input1.name = "editDatasetsPage";
    input1.value = "true";
    input1.type = "hidden";
    theForm.appendChild(input1);

    var theInput = document.createElement('input');
    theInput.id = "deleteExternalLink";
    theInput.name = "deleteExternalLink";
    theInput.value = linkId;
    theInput.type = "hidden";
    theForm.appendChild(theInput);

    document.getElementsByTagName('body').item(0).appendChild(theForm);
    theForm.submit();

    updateDeleteDivs(linkId);
}
function updateDeleteDivs(linkId) {
    // Hide 'Are you sure?' div...
    $('projectAreYouSureDiv_' + linkId).style.display = "none";

    // ... and show Edit and Delete divs.
    $('projectEditLinkLink_' + linkId).style.display = "inline";
    $('projectDeleteLinkLink_' + linkId).style.display = "inline";
}

function cancelDeleteExternalLink(linkId) {
    updateDeleteDivs(linkId);
}

function showAddLinkDiv() {
    // Hide the 'Add' link...
    $('projectAddLinkLink').style.display = "none";

    // ... and show the Add div.
    $('projectInfoAddLinkDiv').style.display = "inline";
}

function updateAddDivs() {
    // Show the 'Add' link...
    $('projectAddLinkLink').style.display = "inline";

    // ... and hide the Add div.
    $('projectInfoAddLinkDiv').style.display = "none";
}

function cancelLinkAdd() {
    updateAddDivs();
    $("linkTitleField").value = "";
    $("linkUrlField").value = "";
}

function addExternalLink(projectId) {

    // Create and submit form to delete the external link.
    var theForm = document.createElement('form');
    theForm.id = "addExternalLinkForm";
    theForm.name = "addExternalLinkForm";
    theForm.action = "Project?id=" + projectId;
    theForm.method = "post";
    theForm.form = "text/plain";

    // Input that will trigger 'editDatasetsPage' in servlet
    var input1 = document.createElement('input');
    input1.id = "editDatasetsPage";
    input1.name = "editDatasetsPage";
    input1.value = "true";
    input1.type = "hidden";
    theForm.appendChild(input1);

    var theTitleInput = document.createElement('input');
    theTitleInput.id = "externalLinkTitle";
    theTitleInput.name = "externalLinkTitle";
    theTitleInput.value = $('linkTitleField').value;
    theTitleInput.type = "hidden";
    theForm.appendChild(theTitleInput);

    var theUrlInput = document.createElement('input');
    theUrlInput.id = "externalLinkUrl";
    theUrlInput.name = "externalLinkUrl";
    theUrlInput.value = $('linkUrlField').value;
    theUrlInput.type = "hidden";
    theForm.appendChild(theUrlInput);

    document.getElementsByTagName('body').item(0).appendChild(theForm);
    theForm.submit();

    updateAddDivs();
}

//-----------------
// Project Actions
//-----------------
function renameProject(projectId) {

    var dialogDiv = jQuery('#renameProjectDialog');
    dialogDiv.html("");

    var projectName = jQuery('#projectNameHiddenField').val();

    var p = document.createElement('p');
    p.innerHTML = "Current project name: " + projectName;
    dialogDiv.append(p);

    p = document.createElement('span');
    p.id = "newProjectNameLabel";
    p.innerHTML = "New project name: ";
    dialogDiv.append(p);

    var newNameField = document.createElement('input');
    newNameField.id = "newProjectName";
    newNameField.name = "newProjectName";
    newNameField.className = "newProjectName";
    newNameField.value = projectName;
    newNameField.size = 30;
    dialogDiv.append(newNameField);

    var hiddenDiv = document.createElement('div');
    hiddenDiv.id = "hiddenErrorDiv";
    hiddenDiv.name = "hiddenErrorDiv";
    hiddenDiv.className = "errorMessage";
    dialogDiv.append(hiddenDiv);

    jQuery('#hiddenErrorDiv').hide();

    // 'Rename Project' dialog from project page
    jQuery('#renameProjectDialog').dialog({
            modal : true,
	    autoOpen : false,
	    width : 400,
	    height : 200,
	    title : "Rename Project",
            buttons : [ {
                id : "rename-project-button",
                text : "Save",
                click : handleRenameProject
            }, {
                id : "cancel-rename-project-button",
                text : "Cancel",
                click : closeRenameDialog
            } ]
        });

    jQuery('#newProjectName').keyup(enableRenameButton);
    jQuery('#rename-project-button').button('disable');
    jQuery('#renameProjectDialog').dialog('open');

}
function enableRenameButton() {
    var projectName = jQuery('#projectNameHiddenField').val();
    var newNameStr = jQuery('input#newProjectName').val();
    if ((newNameStr.length > 0) && (newNameStr != projectName)) {
        jQuery('#rename-project-button').button('enable');
    } else {
        jQuery('#rename-project-button').button('disable');
    }
}
function handleRenameProject() {
    var newProjectName = jQuery('#newProjectName').val();;

    new Ajax.Request("Project", {
        parameters: {
            requestingMethod: "ProjectPage.renameProject",
            id: projectId,
            ajaxRequest: "true",
            renameProject: "true",
            newProjectName: newProjectName
        },
        onComplete: handleRenameResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleRenameResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        closeRenameDialog();

        var newProjectName = json.value;

        // update project name divs...
        jQuery('#projectNameHeader').html(newProjectName);
        jQuery('#projectNameHiddenField').val(newProjectName);
        successPopup(json.message);
    } else {
        if (json.value == 'duplicate') {
            jQuery('#hiddenErrorDiv').html(json.message);
            jQuery('#hiddenErrorDiv').show();
        } else {
            closeRenameDialog();
            errorPopup(json.message);
        }
    }
}

function closeRenameDialog() {
    jQuery('#renameProjectDialog').dialog('close');
}

function deleteProject(projectId) {

    var dialogDiv = jQuery('#deleteProjectDialog');
    dialogDiv.html("");

    var newForm = document.createElement('form');
    newForm.id   = "deleteProjectForm";
    newForm.name = "deleteProjectForm";
    newForm.action = "Project?id=" + projectId;
    newForm.method = "post";
    newForm.form   = "text/plain";

    var projectIdInput = document.createElement('input');
    projectIdInput.id   = "id";
    projectIdInput.name = "id";
    projectIdInput.value = projectId;
    projectIdInput.type  = "hidden";
    newForm.appendChild(projectIdInput);

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "deleteProject";
    hiddenInput.name = "deleteProject";
    hiddenInput.value = "true";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    var projectName = jQuery('#projectNameHiddenField').val();

    var p = document.createElement('p');
    p.innerHTML = "Are you sure you want to delete the project '" + projectName + "'? This cannot be undone.";
    newForm.appendChild(p);

    dialogDiv.append(newForm);

    // 'Delete Project' dialog from project page
    jQuery('#deleteProjectDialog').dialog({
            modal : true,
	    autoOpen : false,
	    width : 450,
	    height : 200,
	    title : "Delete Project",
	    buttons : [ {
                id : "delete-project-button",
                text : "Delete",
                click : handleDeleteProject
            }, {
                id : "cancel-delete-project-button",
                text : "Cancel",
                click : closeDeleteDialog
            } ]
        });

    jQuery('#deleteProjectDialog').dialog('open');

}
function handleDeleteProject() {
    jQuery('#deleteProjectForm').submit();
    jQuery('#deleteProjectDialog').dialog('close');
}
function closeDeleteDialog() {
    jQuery(this).dialog('close');
}

//-----------------
// General purpose
//-----------------
function updateDivClass(divId, value) {
    // Goofiness to handle variable width div
    if (value == "") {
        $(divId).className = "emptyDiv";
    } else {
        $(divId).className = "";
    }
}

function getProjectId() {
    var regexS = "[\\?&]id=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.href );
    if( results == null ) {
        return null;
    } else {
        return results[1];
    }
}

//----------------------
// Project Terms of Use
//----------------------
function enableApplyToU() {
    var tou = jQuery('#project_tou_select').val();
    if (tou != "-1") {
        // User has selected a Tou... enable 'Apply' button
        jQuery('#apply_terms_button').removeAttr('disabled');
    } else {
        jQuery('#apply_terms_button').attr('disabled', 'true');
    }
}

function applyTermsToProject(projectId) {

    var newForm = document.createElement('FORM');
    newForm.id   = "add_project_terms_form";
    newForm.name = "add_project_terms_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectTerms?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "applyTerms";
    newInput.id  = "applyTerms";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    var tou = jQuery('#project_tou_select').val();

    newInput = document.createElement('input');
    newInput.name  = "tou";
    newInput.type  = "hidden";
    newInput.value = tou;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function clearTermsAreYouSure() {

    var areYouSure = document.getElementById('areYouSureDialog');
    areYouSure.innerHTML = "";

    var p = document.createElement('p');
    p.innerHTML = "Are you sure you want to remove the terms of use from this project?";
    areYouSure.appendChild(p);

    jQuery('#areYouSureDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 400,
        height : 200,
        title : "Clear Terms",
        buttons : [ {
            id : "clear-terms-agree-button",
            text : "Clear",
            click : clearTermsFromProject
        }, {
            id : "clear-terms-cancel-button",
            text : "Cancel",
            click : closeClearTermsDialog
        } ]
    });

    jQuery('#areYouSureDialog').dialog('open');                
}

function clearTermsFromProject() {

    closeClearTermsDialog();

    var newForm = document.createElement('FORM');
    newForm.id   = "clear_project_terms_form";
    newForm.name = "clear_project_terms_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectTerms?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "clearTerms";
    newInput.id  = "clearTerms";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function closeClearTermsDialog() {
    jQuery('#areYouSureDialog').dialog('close');
}
