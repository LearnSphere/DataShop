/**
 * Carnegie Mellon University, Human-Computer Interaction Institute Copyright
 * 2013 All Rights Reserved
 */

/** This file contains jQuery dialogs for Push Button Uploads
 * Dependencies: javascript/lib/jquery-1.7.1.min.js,
 * javascript/lib/jquery-ui-1.8.17.custom.min.js
 */
// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

    // Rename Dataset Dialog
    jQuery('.renameDatasetDialog').dialog({
            modal : true,
            autoOpen : false,
            width : 550,
            height : 320,
            title : "Rename Dataset",
            buttons : [ {
                id : "rename-dataset-button",
                text : "Save",
                click : renameDataset
            }, {
                id : "cancel-rename-dataset-button",
                text : "Cancel",
                click : iqCloseDialog
            } ]
    });

    // Release Dataset Dialog
    jQuery('.releaseDatasetDialog').dialog({
            modal : true,
            autoOpen : false,
            width : 675,
            height : 260,
            title : "Release Dataset to Project",
            buttons : [ {
                id : "release-dataset-to-button",
                text : "Release",
                click : releaseDataset
            }, {
                id : "cancel-release-dataset-to-button",
                text : "Cancel",
                click : iqCloseDialog
            } ]
    });

    // Delete Dataset Dialog
    jQuery('.deleteDatasetDialog').dialog({
            modal : true,
            autoOpen : false,
            width : 675,
            height : 320,
            title : "Delete Dataset",
            buttons : [ {
                id : "delete-dataset-button",
                text : "Delete",
                click : deleteDataset
            }, {
                id : "cancel-delete-dataset-button",
                text : "Cancel",
                click : iqCloseDialog
            } ]
    });

    // Assign the open dialog function to the appropriate links
    jQuery('.release_dataset_link').click(createReleaseDatasetDialog);
    jQuery('.move_dataset_link').click(createMoveDatasetDialog);
    jQuery('.move-dataset-div').click(createMoveDatasetDialog);
    jQuery('.rename_dataset_link').click(openRenameDatasetDialog);
    jQuery('.release-dataset-div').click(createReleaseDatasetDialog);
    jQuery('.delete_dataset_link').click(createDeleteDatasetDialog);
});

/** Maximum character length for the New Dataset Name text box. */
var maxNewDatasetName = 100;

/* Begin create dialogs */

// Assign parameter values and response functions for Ajax request
function createReleaseDatasetDialog() {
    var importQueueId = getImportQueueIdFromElementId(this.id);
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "createReleaseDatasetDialog",
            datasetId : importQueueId
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : createReleaseDatasetAjaxListener,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}

// Assign parameter values and response functions for Ajax request
function createDeleteDatasetDialog() {
    var importQueueId = getImportQueueIdFromElementId(this.id);
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "createDeleteDatasetDialog",
            importQueueId : importQueueId
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : createDeleteDatasetAjaxListener,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}


// Assign parameter values and response functions for Ajax request
function createMoveDatasetDialog() {
    var importQueueId = getImportQueueIdFromElementId(this.id);
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "createMoveDatasetDialog",
            importQueueId : importQueueId
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : createMoveDatasetAjaxListener,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}

/* End create dialogs */

/* Begin Ajax Listeners */

// Create the Release Dataset modal window if Ajax request successful
function createReleaseDatasetAjaxListener(transport) {

    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        if (json.projectId != undefined) {
            openReleaseDatasetDialog(json);
        } else {
            var isReleaseDataset = true;
            openMoveDatasetDialog(json, isReleaseDataset);
        }
    } else if (json.msg == "error") {
        messagePopup(json.cause, "ERROR");
    }
}

// Create the Move Dataset modal window if Ajax request successful
function createMoveDatasetAjaxListener(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        var isReleaseDataset = false;
        openMoveDatasetDialog(json, isReleaseDataset);
    } else if (json.msg == "error") {
        messagePopup(json.cause, "ERROR");
    }
}

// Create the Delete Dataset modal window if Ajax request successful
function createDeleteDatasetAjaxListener(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg = "requestForInfo") {
        openDeleteDatasetDialog(json);
    } else if (json.msg == "success") {
        openDeleteDatasetDialog(json);
    } else if (json.msg == "error") {
        messagePopup(json.cause, "ERROR");
    }
}

/* End Ajax Listeners */

/* Begin Dialog definitions */

// Opens a modal window populated with the dataset name
function openRenameDatasetDialog() {
  var importQueueId = getImportQueueIdFromElementId(this.id);
  var currentDatasetName = jQuery('#datasetName_' + importQueueId).val();
  var isDiscourse = getIsDiscourse(importQueueId);
  var itemStr = getItemStr(importQueueId);

  if (importQueueId == null || importQueueId == undefined) {
        messagePopup("An error occurred while trying to delete this " + itemStr + ".", "ERROR");
  } else {

        // Get the renameDataset dialog div from the page
        var renameDatasetDiv = document.getElementById('renameDatasetDialog');
        renameDatasetDiv.innerHTML = "";

        var importQueueIdInput = document.createElement('input');
        importQueueIdInput.id = "importQueueId";
        importQueueIdInput.name = "importQueueId";
        importQueueIdInput.type = "hidden";
        importQueueIdInput.value = importQueueId;
        renameDatasetDiv.appendChild(importQueueIdInput);

        var datasetName = document.createElement('input');
        datasetName.id = "currentDatasetName";
        datasetName.name = "currentDatasetName";
        datasetName.type = "hidden";
        datasetName.value = currentDatasetName;
        renameDatasetDiv.appendChild(datasetName);

        // Add hidden input indicating if IQele is on DiscourseDB project.
        var discourseInput = document.createElement('input');
        discourseInput.id = "isDiscourse";
        discourseInput.name = "isDiscourse";
        discourseInput.type = "hidden";
        discourseInput.value = isDiscourse;
        renameDatasetDiv.appendChild(discourseInput);

        // Create form elements to add to the div
        var p = document.createElement('p');
        p.id = "currentDatasetLabel";
        p.innerHTML = "<span class='boldface'>Current " + itemStr + " name:</span> "
                + currentDatasetName;
        renameDatasetDiv.appendChild(p);

        var p = document.createElement('p');
        p.id = "newDatasetLabel";
        p.innerHTML = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<span class='boldface'>New " + itemStr + " name:&nbsp;</span>"
                + '<input type="text" maxlength="' + maxNewDatasetName
                + '" id="newDatasetName" size="50" ' + 'value="'
                + currentDatasetName + '" />';

        renameDatasetDiv.appendChild(p);

        var hiddenDiv = document.createElement('div');
        hiddenDiv.id = "hiddenErrorDiv";
        hiddenDiv.name = "hiddenErrorDiv";
        hiddenDiv.className = "errorMessage";
        renameDatasetDiv.appendChild(hiddenDiv);

        jQuery('#hiddenErrorDiv').hide();

        if (isDiscourse) {
            jQuery('#renameDatasetDialog').dialog('option', 'title', 'Rename Discourse');
        }
        jQuery('#renameDatasetDialog').dialog('open');
    }
}

// Submits an Ajax request to the servlet for the Rename Dataset dialog.
function renameDataset() {
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "renameDataset",
            importQueueId : jQuery('input[id=importQueueId]').val(),
            newDatasetName : jQuery('input#newDatasetName').val()
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : getRenameDatasetStatus,
        timeout : 50,
        onException : function(request, exception) {
            throw (exception);
        }
    });

    return false;
}

// Opens a modal window for releasing dataset
function openReleaseDatasetDialog(json) {

    // Get the releaseDataset dialog div from the page
    var releaseDatasetDiv = document.getElementById('releaseDatasetDialog');
    releaseDatasetDiv.innerHTML = "";

    var datasetId = document.createElement('input');
    datasetId.id = "datasetId";
    datasetId.name = "datasetId";
    datasetId.type = "hidden";
    datasetId.value = json.datasetId;
    releaseDatasetDiv.appendChild(datasetId);

    var projectId = document.createElement('input');
    projectId.id = "projectId";
    projectId.name = "projectId";
    projectId.type = "hidden";
    projectId.value = json.projectId;
    releaseDatasetDiv.appendChild(projectId);

    // Create form elements to add to the div
    var p = document.createElement('p');
    p.id = "releaseLabel";
    p.innerHTML = "Are you sure you want to release the dataset to the project '"
            + json.projectName
            + "'?"
            + " It will become published to the project and inherit the project's permissions.";
    releaseDatasetDiv.appendChild(p);

    jQuery('#releaseDatasetDialog').dialog('open');

}

// Opens a modal window for moving dataset to project
function openMoveDatasetDialog(json, isReleaseDataset) {
    var releaseMessage = "If you release this dataset to this project,"
        + " it will become published to this project and inherit the project's permissions.";
    var moveOrRelease = document.createElement('p');
    moveOrRelease.id = "moveLabel";
    if (isReleaseDataset == false) {
        releaseMessage = "";
        moveOrRelease.innerHTML = "Move this dataset to...";

        jQuery('.moveDatasetDialog').dialog({
            modal : true,
            autoOpen : false,
            width : 675,
            height : 280,
            title : "Move Dataset",
            buttons : [ {
                id : "move-dataset-button",
                text : "Move",
                click : moveDataset
            }, {
                id : "cancel-move-dataset-button",
                text : "Cancel",
                click : iqCloseDialog
            } ]
        });
    } else {
        moveOrRelease.innerHTML = "Release this dataset to...";
        jQuery('.moveDatasetDialog').dialog({
            modal : true,
            autoOpen : false,
            width : 675,
            height : 280,
            title : "Release Dataset",
            buttons : [ {
                id : "release-dataset-button",
                text : "Release",
                click : moveDataset
            }, {
                id : "cancel-release-dataset-button",
                text : "Cancel",
                click : iqCloseDialog
            } ]
        });
    }
    // Move Dataset Dialog

    // Get the moveDataset dialog div from the page
    var moveDatasetDiv = document.getElementById('moveDatasetDialog');
    moveDatasetDiv.innerHTML = "";
    // Hidden input: datasetId
    var datasetId = document.createElement('input');
    datasetId.id = "datasetId";
    datasetId.name = "datasetId";
    datasetId.type = "hidden";
    datasetId.value = json.datasetId;
    moveDatasetDiv.appendChild(datasetId);
    // Hidden input: importQueueId
    var importQueueId = document.createElement('input');
    importQueueId.id = "importQueueId";
    importQueueId.name = "importQueueId";
    importQueueId.type = "hidden";
    importQueueId.value = json.importQueueId;
    moveDatasetDiv.appendChild(importQueueId);
    // Hidden input: releasedFlag
    var releasedFlag = document.createElement('input');
    releasedFlag.id = "releasedFlag";
    releasedFlag.name = "releasedFlag";
    releasedFlag.type = "hidden";
    releasedFlag.value = isReleaseDataset.toString();
    moveDatasetDiv.appendChild(releasedFlag);
    // Move or Release differences
    moveDatasetDiv.appendChild(moveOrRelease);
    // Radio buttons: New or existing project
    var newCheckedStr = "checked";
    var existingCheckedStr = "";
    var defaultExisting = false;
    if (json.existingProjectComboBox != null
        && json.existingProjectComboBox != undefined) {
        defaultExisting = true;
        newCheckedStr = "";
        existingCheckedStr = "checked";
    }
    var p = document.createElement('p');
    p.id = "moveRadio";
    p.innerHTML = "<input type=\"radio\" id=\"newProjectRadioButton\" name=\"newOrExisting\" value=\"NEW\" "
        + newCheckedStr + " />"
        + "<label for=\"newProjectRadioButton\">new project</label>"
        + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    if (defaultExisting) {
        p.innerHTML = p.innerHTML
            + "<input type=\"radio\" id=\"existingProjectRadioButton\" name=\"newOrExisting\" value=\"EXISTING\" "
            + existingCheckedStr + " />"
            + "<label for=\"existingProjectRadioButton\">existing project</label>";
    } else {
        p.innerHTML = p.innerHTML
                + "<input type=\"radio\" id=\"existingProjectRadioButton\" name=\"newOrExisting\" value=\"EXISTING\" DISABLED />"
                + "<label for=\"existingProjectRadioButton\"><font color=\"#909090\">existing project</font></label>";
    }

    moveDatasetDiv.appendChild(p);
    // Project Input Text
    var p = document.createElement('p');
    p.id = "newProjectInputText";
    p.innerHTML = "Project Name<br><br>&nbsp;&nbsp;&nbsp;"
            + "<input type=\"text\" id=\"newProjectName\""
            + " name=\"newProjectName\" maxlength=\"100\" size=\"90\" />";
    moveDatasetDiv.appendChild(p);
    var pnote = document.createElement('p');
    pnote.id = "newProjectNote";
    pnote.innerHTML = "Don't see a list of existing projects? Check with the project admin to make sure "
        + "you are listed as having \"admin\" permission."
    moveDatasetDiv.appendChild(pnote);
    if (defaultExisting) {
        jQuery('#newProjectNote').hide();
    }

    // Project Combo Box
    var d = document.createElement('div');
    d.id = "existingProjectComboBox";
    d.innerHTML = "<p>Project Name</p>&nbsp;&nbsp;&nbsp;"
            + json.existingProjectComboBox;
    moveDatasetDiv.appendChild(d);
    // Move dataset notes
    var d = document.createElement('div');
    d.id = "moveDatasetNotes";
    d.innerHTML = "";
    moveDatasetDiv.appendChild(d);
    // Project Meta Data
    var d = document.createElement('div');
    d.id = "projectMetaDataDiv";
    d.innerHTML = "";
    moveDatasetDiv.appendChild(d);

    // Release message
    var d = document.createElement('div');
    d.id = "releaseMessageDiv";
    d.innerHTML = releaseMessage;
    moveDatasetDiv.appendChild(d);

    // Hidden 'Project exists' div.
    var hiddenDiv = document.createElement('div');
    hiddenDiv.id = "hiddenErrorDiv";
    hiddenDiv.name = "hiddenErrorDiv";
    hiddenDiv.className = "errorMessage";
    moveDatasetDiv.appendChild(hiddenDiv);
    jQuery('#hiddenErrorDiv').hide();

    // On radio button change, update what to show in Move Dataset Dialog
    jQuery('input[name=newOrExisting]:radio').change(updateMoveDatasetDialog);
    // On combo box change, update to show project meta data
    jQuery("#existingProjectComboBox").change(getProjectMetaData);

    jQuery('#moveDatasetDialog').dialog('open');

    // Figure out what initial dialog should be...
    updateMoveDatasetDialog();
}

function updateMoveDatasetDialog() {
    var newOrExistingRadioButton = jQuery("input[name=newOrExisting]:checked")
            .val();
    if (newOrExistingRadioButton == "EXISTING") {
        jQuery('.moveDatasetDialog').css("height", "450")
        jQuery('.ui-dialog').animate({
            'marginTop' : "-=50px"
        });
        jQuery('#releaseMessageDiv').show();
        jQuery('#existingProjectComboBox').show();
        jQuery('#newProjectInputText').hide();
    } else {
        jQuery('.moveDatasetDialog').css("height", "205")
        jQuery('.ui-dialog').animate({
            'marginTop' : "+=50px"
        });
        jQuery('#releaseMessageDiv').hide();
        jQuery('#existingProjectComboBox').hide();
        jQuery('#newProjectInputText').show();
    }
    getProjectMetaData();
}

function getProjectMetaData() {
    var projectId = jQuery('#projectComboBox').val();
    if (projectId == null) {
        projectId = jQuery('.projectOption:first').val();
        jQuery('.projectOption:first').attr('selected', 'selected')
    }
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "getProjectMetaData",
            projectId : projectId
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        onComplete : updateProjectMetaData,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    return false;
}

function updateProjectMetaData(transport) {
    var json2 = transport.responseText.evalJSON(true);
    if (json2.msg == "success") {
        var newOrExistingRadioButton = jQuery(
                "input[name=newOrExisting]:checked").val();

        // 'existing project' radio button selected
        if (newOrExistingRadioButton == "EXISTING") {

            jQuery("#moveDatasetNotes")
                    .html(
                            "Don't see your project here? Check with the project admin to make sure"
                                    + " you are listed as having \"admin\" permission.");

            if (json2.projectId != undefined) {
                var projectHtml =
                    "<table id=\"projectMetaDataTable\">"
                    + "<tr><th>" + "Project Name" + "</th><td>"
                    + json2.projectName + "</td></tr>"
                    + "<tr><th>" + "PI" + "</th><td>" + json2.piName
                    + "</td></tr>";
                if (json2.dpName != "") {
                    projectHtml += "<tr><th>" + "Data Provider"
                    + "</th><td>" + json2.dpName + "</td></tr>";
                }
                projectHtml += "<tr><th>" + "Permissions" + "</th><td>"
                    + json2.permissions + "</td>" + "</tr></table>";
                jQuery('#projectMetaDataDiv').html(projectHtml);
            }
            // 'new project' radio button selected
        } else {
            jQuery("#moveDatasetNotes").html('');
            jQuery('#projectMetaDataDiv').html('');
        }
    }
}

// Opens a modal window for deleting datasets
function openDeleteDatasetDialog(json) {

    var isDiscourse = getIsDiscourse(json.importQueueId);
    var itemStr = getItemStr(json.importQueueId);

    // Get the deleteDataset dialog div from the page
    var deleteDatasetDiv = document.getElementById('deleteDatasetDialog');
    deleteDatasetDiv.innerHTML = "";

    var importQueueId = document.createElement('input');
    importQueueId.id = "importQueueId";
    importQueueId.name = "importQueueId";
    importQueueId.type = "hidden";
    importQueueId.value = json.importQueueId;
    deleteDatasetDiv.appendChild(importQueueId);

    // Create form elements to add to the div
    var p = document.createElement('p');
    p.id = "deleteLabel";
    p.innerHTML = "Are you sure you want to delete the " + itemStr + " '"
            + json.datasetName + "'? This cannot be undone.";
    deleteDatasetDiv.appendChild(p);

    if (json.msg == "requestForInfo") {
        if (json.additionalInfo != undefined && json.additionalInfo != "") {
            var additionalInfo = document.createElement('p');
            additionalInfo.id = "additionalInfoId";
            additionalInfo.innerHTML = json.additionalInfo;
            deleteDatasetDiv.appendChild(additionalInfo);
        }

        var pAccess = document.createElement('p');
        pAccess.id = "accessListPrompt";
        pAccess.innerHTML = "Other users have accessed this dataset.";
        var accessListDiv = document.createElement('div');
        accessListDiv.id = "accessList";
        if (json.accessListTable != undefined && json.accessListTable != "") {
            accessListDiv.innerHTML = json.accessListTable;
            deleteDatasetDiv.appendChild(pAccess);
            deleteDatasetDiv.appendChild(accessListDiv);
        }


    }

    if (isDiscourse) {
        jQuery('#deleteDatasetDialog').dialog('option', 'title', 'Delete Discourse');
    }

    jQuery('#deleteDatasetDialog').dialog('open');

}

// Submits an Ajax request to the servlet for the Release Dataset dialog.
function releaseDataset() {
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "releaseDataset",
            datasetId : jQuery('input[id=datasetId]').val()
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : getReleaseDatasetStatus,
        timeout : 50,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    jQuery(this).dialog("close");
    return false;
}

//Submits an Ajax request to the servlet for the Release Dataset dialog.
function moveDataset() {
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "moveDataset",
            datasetId : jQuery('input[id=datasetId]').val(),
            importQueueId : jQuery('input[id=importQueueId]').val(),
            projectId : jQuery('#projectComboBox').val(),
            newOrExisting : jQuery("input[name=newOrExisting]:checked").val(),
            newProjectName : jQuery('input[id=newProjectName]').val(),
            releasedFlag : jQuery('input[id=releasedFlag]').val()
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : getMoveDatasetStatus,
        timeout : 50,
        onException : function(request, exception) {
            throw (exception);
        }
    });

    return false;
}

// Submits an Ajax request to the servlet for the Delete Dataset dialog.
function deleteDataset() {
    new Ajax.Request("ImportQueue", {
        parameters : {
            requestingMethod : "deleteDataset",
            importQueueId : jQuery('input[id=importQueueId]').val()
        },
        requestHeaders : {
            Accept : 'application/json;charset=UTF-8'
        },
        beforeSend : iqShowStatusIndicator("Please wait..."),
        onSuccess : iqHideStatusIndicator,
        onComplete : getDeleteDatasetStatus,
        timeout : 50,
        onException : function(request, exception) {
            throw (exception);
        }
    });
    jQuery(this).dialog("close");
    return false;
}

/* End Ajax Submissions */

/* Begin Ajax Submission supporting functions */

// Reload the page after Ajax success
function reloadPageonComplete(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        window.location.assign(window.location.href);
    }
}

// Shows a status indicator while waiting
function iqShowStatusIndicator(titleString) {
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
            .html(
                    '<center><img id="waitingIcon" src="images/waiting.gif" /></center>');
    jQuery('#statusIndicator').dialog('open');
    jQuery('#statusIndicator').dialog("option", "stack", true);
    jQuery('#statusIndicator').dialog("option", "resizable", false);
}

// Hides that status indicator
function iqHideStatusIndicator() {
    jQuery('#statusIndicator').dialog('close');

}

// Ajax response listener for Rename Dataset
function getRenameDatasetStatus(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        var response = json.response;
        var titleSpan = 'span[class=dataset-name][id=datasetNameSpan_' + json.importQueueId + ']';
        if (json.isLoaded == true) {
            var isDiscourse = jQuery('#isDiscourse').val();
            if (!isDiscourse) {
                jQuery(titleSpan).html(
                                       "<a href=\"DatasetInfo?datasetId=" + json.datasetId + "\">"
                                       + json.datasetName + "</a>");
            } else {
                jQuery(titleSpan).html(
                                       "<a href=\"DiscourseInfo?discourseId=" + json.datasetId + "\">"
                                       + json.datasetName + "</a>");
            }
        } else {
            jQuery(titleSpan).html(json.datasetName);
        }

        if (json.status != "EXISTS") {
            closeRenameDatasetDialog();
        }

        if (json.status == "RENAMED") {
            var lastUpdateSpan = '#lastUpdateSpan_' + json.importQueueId;
            jQuery(lastUpdateSpan).html(json.lastUpdatedString);
            var datasetNameHidden = '#datasetName_' + json.importQueueId;
            jQuery(datasetNameHidden).val(json.datasetName);
            messagePopup(response, "SUCCESS");
        } else if (json.status == "EXISTS") {
            jQuery('#hiddenErrorDiv').html(json.response);
            jQuery('#hiddenErrorDiv').show();
        } else {
            messagePopup(response, "ERROR");
        }

    } else if (json.msg == "error") {
        closeRenameDatasetDialog();
        messagePopup(json.cause, "ERROR");
    } else if (json.msg == "timeout") {
        alert('timeout');
    }
}

function closeRenameDatasetDialog() {
    jQuery('#renameDatasetDialog').dialog('close');
}

// Ajax response listener for Release Dataset
function getReleaseDatasetStatus(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        var response = json.response;

        if (json.status == "RELEASED") {
            if (json.datasetId != undefined) {
                reloadPageonComplete(transport);
            }
            if (json.projectName != undefined) {
                jQuery("div[id=project-name-div]").html("&nbsp;&nbsp;&nbsp;&nbsp;Project: <a href=\"Project?id="
                         + json.projectId + "\">"
                         + json.projectName + "</a><br>");
            }
            messagePopup(response, "SUCCESS");
            // Reload page if invoked from an import queue
            if (jQuery('#import-queue-div').length != 0) {
                window.location.assign(window.location.href);
            }
        } else {
            messagePopup(response, "ERROR");
        }
    } else if (json.msg == "error") {
        messagePopup(json.cause, "ERROR");
    } else if (json.msg == "timeout") {
        alert('timeout');
    }
}

//Ajax response listener for Release Dataset
function getMoveDatasetStatus(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        var response = json.response;

        if (json.status != "EXISTS") {
            closeMoveDatasetDialog();
        }

        if (json.status == "MOVED") {
            if (json.datasetId != undefined) {
                var releaseButtonDiv = 'div[id=unreleased_dataset_div]';
                jQuery(releaseButtonDiv).hide();
            }
            if (json.projectName != undefined) {
                var lastUpdateSpan = '#lastUpdateSpan_' + json.importQueueId;
                jQuery(lastUpdateSpan).html(json.lastUpdatedString);
                var divRef = "tr[id=iqrow_" + json.importQueueId + "]";
                if (jQuery("div[id=project_content_div]").length
                    || jQuery("div[id=my-data-sets]").length
                    || jQuery("table[id=my-data-sets]").length
                    || jQuery("div[id=public-data-sets]").length
                    || jQuery("table[id=public-data-sets]").length
                    || jQuery("div[id=available-data-sets]").length
                    || jQuery("table[id=available-data-sets]").length
                    || jQuery("div[id=contentHeader]").length) {
                        messagePopup(response, "SUCCESS");
                        reloadPageonComplete(transport);
                } else {
                    var divRef = "div[id=project-name-div][name=" + json.importQueueId + "]";
                    jQuery(divRef).html("&nbsp;&nbsp;&nbsp;&nbsp;Project: <a href=\"Project?id="
                         + json.projectId + "\">"
                         + json.projectName + "</a><br>");
                    messagePopup(response, "SUCCESS");
                }
            }
        } else if (json.status == "EXISTS") {
            jQuery('#hiddenErrorDiv').html(json.response);
            jQuery('#hiddenErrorDiv').show();
        } else {
            messagePopup(response, "ERROR");
        }
    } else if (json.msg == "error") {
        closeMoveDatasetDialog();
        messagePopup(json.cause, "ERROR");
    } else if (json.msg == "timeout") {
        alert('timeout');
    }
}

function closeMoveDatasetDialog() {
    jQuery('#moveDatasetDialog').dialog('close');
}

// Ajax response listener for Delete Dataset
function getDeleteDatasetStatus(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.msg == "success") {
        var response = json.response;
        var importQueueId = json.importQueueId;
        if (json.status == "DELETED") {
            messagePopup(response, "SUCCESS");
            reloadPageonComplete(transport);
        } else {
            messagePopup(response, "ERROR");
        }

    } else if (json.msg == "error") {
        messagePopup(json.cause, "ERROR");
    } else if (json.msg == "timeout") {
        alert('timeout');
    }
}

// A simple close modal dialog function to make reading easier elsewhere.
function iqCloseDialog() {
    jQuery(this).dialog("close");

}

function iqCloseInformationDialog() {
    jQuery(this).dialog("close");
    jQuery(this).empty().remove();
}

/* End Ajax Submission supporting functions */

/* Begin Ajax Response supporting functions */

/** Used to show info from Ajax responses
 *
 */
function iqShowConfirmationDialog(info) {
    // Create form elements to add to the div
    var div = document.createElement('div');
    div.id = "confirmationDialog";
    div.className = "informationalDialog";
    div.innerHTML = info;
    jQuery('body').append(div);

    jQuery('div[id=confirmationDialog]').dialog({
        modal : true,
        autoOpen : false,
        width : 620,
        height : 325,
        title : "Success",
        buttons : [ {
            id : "success-okay-button",
            text : "Okay",
            click : iqCloseInformationDialog
        } ]
    });

    jQuery('div[id=confirmationDialog]').dialog('open');
}

/** Used to show errors with Ajax responses
 *
 */
function iqShowErrorDialog(cause) {
    // Create form elements to add to the div
    var div = document.createElement('div');
    div.id = "iqShowErrorDialog";
    div.className = "informationalDialog";
    div.innerHTML = cause;
    jQuery('body').append(div);
    // Used to show errors with the Ajax requests
    jQuery('#iqShowErrorDialog').dialog({
        modal : true,
        autoOpen : false,
        width : 340,
        height : 240,
        title : "Oops",
        buttons : [ {
            id : "success-oops-button",
            text : "Okay",
            click : iqCloseInformationDialog
        } ]
    });

    jQuery('#iqShowErrorDialog').dialog('open');
}

//-------------------------------------------------------
// Helper function to determine, based on 'format', if
// an ImportQueue row is for a Discourse.
//-------------------------------------------------------
function getIsDiscourse(importQueueId) {
  var fileFormat = jQuery('#format_' + importQueueId).val();
  var isDiscourse = false;
  if (fileFormat != undefined) {
      isDiscourse = fileFormat.startsWith("discourse") ? true : false;
  }
  return isDiscourse;
}

//-------------------------------------------------------
// Helper function to get type of item, based on 'format',
// in an ImportQueue row -- dataset or discourse.
//-------------------------------------------------------
function getItemStr(importQueueId) {
  var itemStr = getIsDiscourse(importQueueId) ? "discourse" : "dataset";
  return itemStr;
}
