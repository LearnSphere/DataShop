//
// Carnegie Mellon University
// Copyright 2013
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 11984 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2015-02-05 15:22:17 -0500 (Thu, 05 Feb 2015) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(
        function() {

            var message = jQuery('#cfMessage').val();
            var messageLevel = jQuery('#cfMessageLevel').val();

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

function openAddCustomFieldDialog(datasetId) {
    var newContents = document.createElement('div');
    newContents.id   = "addCustomFieldDiv";
    newContents.name = "addCustomFieldDiv";

    var hiddenInput = document.createElement('input');
    hiddenInput.id  = "datasetId";
    hiddenInput.name  = "datasetId";
    hiddenInput.type  = "hidden";
    hiddenInput.value = dataset;
    newContents.appendChild(hiddenInput);

    var dialogDiv = populateDialogDiv(newContents, 0, null, null, null, null, false);
}

function populateDialogDiv(theContents, cfId, cfName, cfDesc, cfLevel, hasData) {

    var dialogDiv = document.getElementById('customFieldDialog');
    dialogDiv.innerHTML = "";

    var hiddenInput = document.createElement('input');
    hiddenInput.id  = "customFieldId";
    hiddenInput.name  = "customFieldId";
    hiddenInput.type  = "hidden";
    hiddenInput.value = cfId;
    theContents.appendChild(hiddenInput);

    var isEdit = (cfId != 0);

    // Name
    var nameLabel = document.createElement('p');
    nameLabel.innerHTML = "Name";
    theContents.appendChild(nameLabel);

    var nameField = document.createElement('input');
    nameField.id = "cfNameField";
    nameField.name = "cfNameField"
    nameField.className = "cfNameField";
    if (isEdit) { nameField.value = cfName; }
    nameField.size = 50;
    theContents.appendChild(nameField);

    var nameInUse = document.createElement('div');
    nameInUse.id = "cfNameInUse";
    nameInUse.name = "cfNameInUse";
    theContents.appendChild(nameInUse);
    jQuery('#cfNameInUse').hide();

    var nameMaxLen = document.createElement('div');
    nameMaxLen.id = "cfNameMaxLen";
    nameMaxLen.innerHTML = "Enter a unique name that is no more than 255 characters.";
    theContents.appendChild(nameMaxLen);

    // Description
    var descLabel = document.createElement('p');
    descLabel.innerHTML = "Description";
    theContents.appendChild(descLabel);

    var descField = document.createElement('textarea');
    descField.id   = "cfDescField";
    descField.name = "cfDescField";
    descField.rows = 4;
    descField.cols = 20;
    if (isEdit && (cfDesc != null)) { descField.value = cfDesc; }
    theContents.appendChild(descField);

    var descMaxLen = document.createElement('div');
    descMaxLen.id = "cfDescMaxLen";
    descMaxLen.innerHTML = "Enter no more than 500 characters.";
    theContents.appendChild(descMaxLen);


    // Level
    var levelLabel = document.createElement('p');
    levelLabel.innerHTML = "Level";
    theContents.appendChild(levelLabel);

    selectField = document.createElement('select');
    selectField.name = "cfLevelSelect";
    selectField.className = "cfLevelSelect";
    selectField.id = "cfLevelSelect";
    selectField.size = 1;

    var cfLevelList = ['transaction'];

    for (var i = 0; i < cfLevelList.length; i++) {
        anOption = document.createElement('option');
        anOption.value = cfLevelList[i];
        anOption.label = cfLevelList[i];   // IE requires this one
        anOption.text = cfLevelList[i];    // ... while FF this one

        if (isEdit) {
            if (cfLevelList[i] == cfLevel) {
                anOption.selected = true;
            }
        }

        selectField.appendChild(anOption);
    }
    // Changes to 'level' not supported in this release.
    selectField.setAttribute('disabled', 'disabled');
    selectField.title = 'Transaction-level custom fields are ' +
        'the only supported custom fields at this time.';
    theContents.appendChild(selectField);

    if (!isEdit) {
        // Since the level select is disabled, the value won't appear in the form.
        // Fake it with a hidden attribute until we support more than one level.
        hiddenInput = document.createElement('input');
        hiddenInput.name  = "cfLevelSelect";
        hiddenInput.type  = "hidden";
        hiddenInput.value = "transaction";
        theContents.appendChild(hiddenInput);
    }

    // Note before Add button
    var noteLabel = document.createElement('p');
    noteLabel.className = "addCustomFieldDataNote";
    var noteText = "You can add data to this custom field using web services.";
    if (hasData) {
        noteText = "You can add or modify data to this custom field using web services.";
    }
    noteLabel.innerHTML = noteText;
    theContents.appendChild(noteLabel);

    // Finally, attach the form to the div...
    dialogDiv.appendChild(theContents);

    var titleStr = "Add Custom Field";
    if (isEdit) { titleStr = "Edit Custom Field"; }
    var buttonStr = "Add";
    if (isEdit) { buttonStr = "Save"; }

    jQuery('#customFieldDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 540,
        title : titleStr,
        buttons : [ {
            id : "save-cf-button",
            text : buttonStr,
            click : saveCustomField
        }, {
            id : "cancel-add-cf-button",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    // For 'add', disable button until fields populated correctly.
    if (!isEdit) {
        jQuery('#save-cf-button').button('disable');
    }
    jQuery('#cfNameField').keyup(enableSaveButton);
    jQuery('#cfDescField').keyup(enableSaveButton);
    jQuery('#customFieldDialog').dialog('open');

    return dialogDiv;
}

function enableSaveButton() {
    jQuery('#cfNameInUse').hide();

    var nameStr = jQuery("input#cfNameField").val();
    var nameFlag = nameStr.length <= 255;
    var nameGiven = nameStr.length > 0;
    var descFlag = jQuery("textarea#cfDescField").val().length <= 500;

    if (nameFlag) {
        jQuery("#cfNameMaxLen").css('color', '#333');
    }
    if (descFlag) {
        jQuery("#cfDescMaxLen").css('color', '#333');
    }
    if (nameFlag && descFlag && nameGiven) {
        jQuery('#save-cf-button').button('enable');
    } else {
        jQuery('#save-cf-button').button('disable');
        if (!nameFlag) {
            jQuery("#cfNameMaxLen").css('color', '#E00000');
        }
        if (!descFlag) {
            jQuery("#cfDescMaxLen").css('color', '#E00000');
        }
    }
}

// Submit the form that will add or edit the custom field
function saveCustomField() {
    var theContents = jQuery('#addCustomFieldDiv');
    var cfAction = "addCustomField";
    if (!theContents.length) {
        theContents = jQuery('#editCustomFieldDiv');
        cfAction = "editCustomField";
    }

    jQuery('div#cfNameInUse').hide();

    new Ajax.Request("CustomField", {
        parameters: {
            requestingMethod: "CustomFields.saveCustomField",
            datasetId: dataset,
            customFieldId: jQuery('#customFieldId').val(),
            cfNameField: jQuery('#cfNameField').val(),
            cfDescField: jQuery('#cfDescField').val(),
            cfLevelSelect: jQuery('#cfLevelSelect').val(),
            customFieldAction: cfAction
        },
        onComplete: handleSaveResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleSaveResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.messageLevel == "SUCCESS") {
        closeDialog();

        // Reload page to update fields.
        window.location.assign(window.location.href);

    } else {
        // Update dialog? or close it?
        if (json.message == "This name is already taken.") {
            jQuery('#cfNameInUse').html(json.message);
            jQuery('#cfNameInUse').show();
        } else {
            closeDialog();
            errorPopup(json.message);
        }
    }
}

// A simple close modal dialog function.
function closeDialog() {
    jQuery(this).dialog("close");
}

function sortCustomFields(sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id = "sortCustomFieldsForm";
    newForm.name = "sortCustomFieldsForm";
    newForm.form = "text/plain";
    newForm.action = "CustomField";
    newForm.method = "post";

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "customFieldAction";
    hiddenInput.name = "customFieldAction";
    hiddenInput.value = "sortCustomFields";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    var newInput = document.createElement('input');
    newInput.name  = "datasetId";
    newInput.type  = "hidden";
    newInput.value = dataset;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "sortBy";
    newInput.type  = "hidden";
    newInput.value = sortByKey;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function showCFDeleteSureDiv(cfId) {
    cfSureDiv = $("cfDeleteSureDiv_" + cfId);
    cfSureDiv.style.display = "inline";

    cfLinkDiv = $("cfDeleteLinkDiv_" + cfId);
    cfLinkDiv.style.display = "none";
}

function showCFDeleteLinkDiv(cfId) {
    cfSureDiv = $("cfDeleteSureDiv_" + cfId);
    cfSureDiv.style.display = "none";

    cfLinkDiv = $("cfDeleteLinkDiv_" + cfId);
    cfLinkDiv.style.display = "inline";
}

function deleteCustomField(cfId) {
    showCFDeleteLinkDiv(cfId);

    new Ajax.Request("CustomField", {
        parameters: {
            requestingMethod: "CustomFields.deleteCustomField",
            datasetId: dataset,
            customFieldId: cfId,
            customFieldAction: "deleteCustomField"
        },
        onComplete: handleDeleteResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function handleDeleteResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.messageLevel == "SUCCESS") {
        // Reload page to update fields.
        window.location.assign(window.location.href);
    } else {
        errorPopup(json.message);
    }
}

function openEditCustomFieldDialog(cfId) {
    new Ajax.Request("CustomField", {
        parameters: {
            requestingMethod: "CustomFields.openEditCustomFieldDialog",
            datasetId: dataset,
            customFieldId: cfId,
            customFieldAction: "getCustomField"
        },
        onComplete: setupEditDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function setupEditDialog(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.messageLevel == "SUCCESS") {
        openEditDialog(json);
    } else {
        errorPopup(json.message);
    }
}

function openEditDialog(json) {

    var datasetId = json.datasetId;
    var cfId = json.id;
    var cfName = json.name;
    var cfDesc = null;
    // This can be null in db...
    if (json.description != undefined) {
        cfDesc = json.description;
    }
    var cfLevel = json.level;
    var hasData = json.hasData;

    var newContents = document.createElement('div');
    newContents.id   = "editCustomFieldDiv";
    newContents.name = "editCustomFieldDiv";
    newContents.form = "text/plain";
    newContents.action = "CustomField";
    newContents.method = "post";

    hiddenInput = document.createElement('input');
    hiddenInput.name  = "datasetId";
    hiddenInput.type  = "hidden";
    hiddenInput.value = datasetId;
    newContents.appendChild(hiddenInput);

    var dialogDiv = populateDialogDiv(newContents, cfId, cfName, cfDesc, cfLevel, hasData);
}

function showFullCFDescription(cfId) {
    var fullDescription = $('cfDescFull_' + cfId).value;

    var descSpan = $('cfDescSpan_' + cfId);
    descSpan.innerHTML = fullDescription;

    var descMoreDiv = $('cfDescMoreDiv_' + cfId);
    descMoreDiv.style.display = "none";
    var descLessDiv = $('cfDescLessDiv_' + cfId);
    descLessDiv.style.display = "block";
}

function hideFullCFDescription(cfId) {
    var truncDescription = $('cfDescTrunc_' + cfId).value;

    var descSpan = $('cfDescSpan_' + cfId);
    descSpan.innerHTML = truncDescription;

    var descLessDiv = $('cfDescLessDiv_' + cfId);
    descLessDiv.style.display = "none";
    var descMoreDiv = $('cfDescMoreDiv_' + cfId);
    descMoreDiv.style.display = "block";
}
