//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2012
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 13027 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-03-28 12:32:22 -0400 (Mon, 28 Mar 2016) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        jQuery('#irb_review_search_by').keyup(irbReviewSearchBy);
        jQuery('#irb_review_search_button').click(irbReviewSearchBy);
        jQuery("#irb_review_search_by").hint("auto-hint");
        jQuery('#irb_review_search_clear_button').click(irbReviewSearchByClear);
        
        jQuery('#irb_review_search_by_pi_dp').keyup(irbReviewSearchByPiDp);
        jQuery('#irb_review_search_pi_dp_button').click(irbReviewSearchByPiDp);
        jQuery("#irb_review_search_by_pi_dp").hint("auto-hint");
        jQuery('#irb_review_search_pi_dp_clear_button').click(irbReviewSearchByPiDpClear);

        jQuery('#reviewPublicSelect').change(irbReviewSelect);
        jQuery('#reviewSubjToSelect').change(irbReviewSelect);
        jQuery('#reviewShareabilitySelect').change(irbReviewSelect);
        jQuery('#reviewDataTypeSelect').change(irbReviewSelect);
        jQuery('#reviewDatasetsSelect').change(irbReviewSelect);
        jQuery('#reviewNeedsAttnSelect').change(irbReviewSelect);
        jQuery('#reviewProjectCreatedSelect').change(irbReviewSelect);
        jQuery('#reviewProjectCreatedInput').change(irbReviewSelect);
        jQuery('#reviewDsLastAddedSelect').change(irbReviewSelect);
        jQuery('#reviewDsLastAddedInput').change(irbReviewSelect);
        
        jQuery('#irb_review_pc_clear_button').click(irbReviewClearPc);
        jQuery('#irb_review_dla_clear_button').click(irbReviewClearDla);

        jQuery('#all_irb_search_by').keyup(allIRBSearchBy);
        jQuery('#all_irb_search_button').click(allIRBSearchBy);
        jQuery("#all_irb_search_by").hint("auto-hint");

        jQuery('#all_irb_add_irb').click(openSimpleAddIrbDialog);
        
        jQuery('#reviewProjectCreatedInput').datepicker({ dateFormat: "yy-mm-dd" });
        jQuery('#reviewDsLastAddedInput').datepicker({ dateFormat: "yy-mm-dd" });

        // If the 'Edit IRB' dialog was open before the redirect, reopen it.
        var irbId = getOpenIRBId();
        if (irbId > 0) {
            openEditIRBDialog(irbId);
        }
});

function getOpenIRBId() {
    editIsOpen = jQuery('#editIRBIsOpen');
    if (!editIsOpen) {
        return 0;
    } else {
        return editIsOpen.val();
    }
}
function editIRBIsOpen(irbId) {
    editIsOpen = jQuery('#editIRBIsOpen');
    editIsOpen.val(irbId);
}
function editIRBIsClosed() {
    editIsOpen = jQuery('#editIRBIsOpen');
    editIsOpen.val(0);
}

//---------------------------
// IRB Project page
//   Data Collection Type
//---------------------------
function showEditDCDiv() {
    jQuery('#irbInfoDCDiv').hide();
    jQuery('#irbInfoEditDCDiv').css("display", "inline-block");
}
function hideEditDCDiv() {
    jQuery('#irbInfoDCDiv').show();
    jQuery('#irbInfoEditDCDiv').hide();
}
function saveDCTypeChange(projectId) {
    var theValue = jQuery('input:radio[name=dcTypeField]:checked').val();

    var newForm = document.createElement('FORM');
    newForm.id   = "update_data_type_form";
    newForm.name = "update_data_type_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "editDataCollectionType";
    newInput.id  = "editDataCollectionType";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "value"
    newInput.type  = "hidden";
    newInput.value = theValue;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function cancelDCTypeChange() {
    hideEditDCDiv();

    // Revert any change user might have made to radio buttons.
    var hiddenDCType = jQuery("#dcTypeHiddenField").val();
    if (hiddenDCType == "not_specified") {
        jQuery("#dcTypeField1").attr("checked", true);
    } else if (hiddenDCType == "not_human_subject") {
        jQuery("#dcTypeField2").attr("checked", true);
    } else if (hiddenDCType == "study_data_consent_not_req") {
        jQuery("#dcTypeField3").attr("checked", true);
    } else if (hiddenDCType == "study_data_consent_req") {
        jQuery("#dcTypeField4").attr("checked", true);
    }
}

//---------------------------
// IRB Project page
//   Subject to DataShop IRB
//---------------------------
function showEditSubjToDiv() {
    jQuery('#irbInfoSubjToDSDiv').hide();
    jQuery('#irbInfoEditSubjToDiv').css("display", "inline-block");
}
function hideEditSubjToDiv() {
    jQuery('#irbInfoSubjToDSDiv').show();
    jQuery('#irbInfoEditSubjToDiv').hide();
}
function saveSubjToChange(projectId) {
    var theValue = jQuery('input:radio[name=subjToField]:checked').val();

    var newForm = document.createElement('FORM');
    newForm.id   = "update_subject_to_form";
    newForm.name = "update_subject_to_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "editSubjectTo";
    newInput.id  = "editSubjectTo";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "value"
    newInput.type  = "hidden";
    newInput.value = theValue;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function cancelSubjToChange() {
    hideEditSubjToDiv();

    // Revert any change user might have made to radio buttons.
    var hiddenSubjTo = jQuery("#subjToHiddenField").val();
    if (hiddenSubjTo == "not_specified") {
        jQuery("#subjToField1").attr("checked", true);
    } else if (hiddenSubjTo == "no") {
        jQuery("#subjToField2").attr("checked", true);
    } else if (hiddenSubjTo == "yes") {
        jQuery("#subjToField3").attr("checked", true);
    }
}

//------------------------------
// IRB Project page
//   Shareability Review Status
//------------------------------
function showEditShareStatusDiv() {
    jQuery('#irbInfoShareStatusDiv').hide();
    jQuery('#irbInfoEditShareStatusDiv').css("display", "inline-block");
}
function hideEditShareStatusDiv() {
    jQuery('#irbInfoShareStatusDiv').show();
    jQuery('#irbInfoEditShareStatusDiv').hide();
}
function saveShareStatusChange(projectId) {
    var theValue = jQuery('input:radio[name=shareStatusField]:checked').val();

    var newForm = document.createElement('FORM');
    newForm.id   = "update_shareability_form";
    newForm.name = "update_shareability_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "editShareStatus";
    newInput.id  = "editShareStatus";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "value"
    newInput.type  = "hidden";
    newInput.value = theValue;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function cancelShareStatusChange() {
    hideEditShareStatusDiv();

    // Revert any change user might have made to radio buttons.
    var hiddenShareStatus = jQuery("#shareStatusHiddenField").val();
    if (hiddenShareStatus == "not_submitted") {
        jQuery("#shareStatusField1").attr("checked", true);
    } else if (hiddenShareStatus == "waiting_for_researcher") {
        jQuery("#shareStatusField2").attr("checked", true);
    } else if (hiddenShareStatus == "submitted_for_review") {
        jQuery("#shareStatusField3").attr("checked", true);
    } else if (hiddenShareStatus == "shareable") {
        jQuery("#shareStatusField4").attr("checked", true);
    } else if (hiddenShareStatus == "not_shareable") {
        jQuery("#shareStatusField5").attr("checked", true);
    }
}

function updateShareabilityReviewHistory(tableHtml) {
    jQuery('#irb-shareability-history-table').html(tableHtml);
}

//------------------------------
//IRB Project page
//Needs Attention
//------------------------------
function showEditNeedsAttnDiv() {
    jQuery('#irbInfoNeedsAttnDiv').hide();
    jQuery('#irbInfoEditNeedsAttnDiv').css("display", "inline-block");
}
function hideEditNeedsAttnDiv() {
    jQuery('#irbInfoNeedsAttnDiv').show();
    jQuery('#irbInfoEditNeedsAttnDiv').hide();
}
function saveNeedsAttnChange(projectId) {
    var theValue = jQuery('input:radio[name=needsAttnField]:checked').val();

    var newForm = document.createElement('FORM');
    newForm.id   = "update_shareability_form";
    newForm.name = "update_shareability_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "editNeedsAttn";
    newInput.id  = "editNeedsAttn";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "value"
    newInput.type  = "hidden";
    newInput.value = theValue;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

// Revert any change user might have made to radio buttons.
function cancelNeedsAttnChange() {
    hideEditNeedsAttnDiv();

    var hiddenNeedsAttn = jQuery("#needsAttnHiddenField").val();
    if (hiddenNeedsAttn == "Yes") {
        jQuery("#needsAttnField1").attr("checked", true);
        //jQuery("#needsAttnField2").attr("checked", false);
    } else {
        //jQuery("#needsAttnField1").attr("checked", false);
        jQuery("#needsAttnField2").attr("checked", true);
    }
}

//---------------------------
// IRB Project page
//   Add IRB
//---------------------------
function submitProjectForReview(projectId) {

    var dialogDiv = document.getElementById('submitForReviewDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "submitForReviewForm";
    newForm.name = "submitForReviewForm";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";
    newForm.form   = "text/plain";

    var projectIdInput = document.createElement('input');
    projectIdInput.id   = "id";
    projectIdInput.name = "id";
    projectIdInput.value = projectId;
    projectIdInput.type  = "hidden";
    newForm.appendChild(projectIdInput);

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "submitForReview";
    hiddenInput.name = "submitForReview";
    hiddenInput.value = "true";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    var p = document.createElement('p');
    p.id = "submitForReviewText1";
    p.innerHTML = "Since you would like to share your project outside of your research team, "
        + "your DataShop project and its IRB documentation will need to be reviewed by DataShop staff. "
        + "If you do not intend to share your data through DataShop, you don't need to have it reviewed.";
    newForm.appendChild(p);

    p = document.createElement('p');
    p.id = "submitForReviewText2";
    p.innerHTML = "Before submitting, please be sure you have added any relevant IRB documentation to your project."
        + "The research manager may contact you with any questions.";
    newForm.appendChild(p);

    dialogDiv.appendChild(newForm);

    jQuery('#submitForReviewDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 550,
        height : 320,
        title : "IRB",
        buttons : [ {
            id : "submit-irb-button",
            text : "Submit for Review",
            click : submitForReview
        }, {
            id : "cancel-submit-irb-button",
            text : "Cancel",
            click : closeSubmitDialog
        } ]
    });

    jQuery('#submitForReviewDialog').dialog('open');
}
function submitForReview() {
    jQuery('#submitForReviewForm').submit();
    jQuery('#submitForReviewDialog').dialog('close');
}
function closeSubmitDialog() {
    jQuery(this).dialog("close");
}

//---------------------------
// IRB Project page
//   Add IRB
//---------------------------
function openAddIRBDialog(projectId) {
    var searchByStr = jQuery('input#addIRBSearchBy').val();

    // Start by getting list of existing IRBs for this user.
    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.openAddIRBDialog",
            id: projectId,
            searchBy: searchByStr,
            getIRBList: "true"
        },
        onComplete: setupAddIRBDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function setupAddIRBDialog(transport) {

    // Get list of IRB names...
    var json = transport.responseText.evalJSON(true);
    var irbList = json.irbList;
    var listSize = getListSize(irbList);

    var dialogDiv = document.getElementById('addIRBDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "addIRBForm";
    newForm.name = "addIRBForm";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";
    newForm.form   = "text/plain";

    var projectIdInput = document.createElement('input');
    projectIdInput.id   = "id";
    projectIdInput.name = "id";
    projectIdInput.value = projectId;
    projectIdInput.type  = "hidden";
    newForm.appendChild(projectIdInput);

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "addIRB";
    hiddenInput.name = "addIRB";
    hiddenInput.value = "true";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    // By default, show 'chooseExisting'
    var existingChecked = "";
    var newChecked = "";
    if (listSize > 0) {
        existingChecked = "checked";
    } else {
        newChecked = "checked";
    }

    // Choose existing or add new...
    var chooseInput = document.createElement('p');
    chooseInput.id = "chooseExistingIRB";
    chooseInput.innerHTML = '<input type="radio" id="chooseExisting" name="addIRBType" '
        + 'value="existing"' +  existingChecked + '><label for="chooseExisting">Choose an existing IRB</label>';
    newForm.appendChild(chooseInput);
    var addNewInput = document.createElement('p');
    addNewInput.id = "addNewIRB";
    addNewInput.innerHTML = '<input type="radio" id="addNew" name="addIRBType" '
        + 'value="addNew"' + newChecked + '><label for="addNew">Add a new IRB</label>';
    newForm.appendChild(addNewInput);

    var chooseExistingDiv = jQuery('<div id="chooseExistingIRBDiv">');
    var addNewDiv = jQuery('<div id="addNewIRBDiv">');

    var saveButtonStr = "Select IRB";
    if (listSize > 0) {
        chooseExistingDiv.show();
        addNewDiv.hide();
    } else {
        saveButtonStr = "Save";
        chooseExistingDiv.hide();
        addNewDiv.show();
    }

    var chooseExistingDom = chooseExistingDiv.get(0);
    var addNewDom = addNewDiv.get(0);

    newForm.appendChild(chooseExistingDom);
    newForm.appendChild(addNewDom);

    initializeAddNewDialog(addNewDom);
    initializeChooseExistingDialog(chooseExistingDom, irbList);

    // Finally, attach the form to the div...
    dialogDiv.appendChild(newForm);

    jQuery('#addIRBDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 650,
        height : 670,
        title : "Add an IRB (step 1)",
        buttons : [ {
            id : "save-irb-button",
            text : saveButtonStr,
            click : addIRB
        }, {
            id : "cancel-save-irb-button",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    jQuery('#chooseExisting').click(chooseExistingIRB);
    jQuery('#addNew').click(addNewIRB);

    jQuery('#save-irb-button').button('disable');
    jQuery('#addIRBDialog').dialog('open');

    // Choose existing...
    setupExistingDialog();

    // Add new...
    setupAddNewDialog();
}
function setupExistingDialog() {
    jQuery('#add_irb_search_by').keyup(addIRBSearchBy);
    jQuery('#add_irb_search_button').click(addIRBSearchBy);
    jQuery("#add_irb_search_by").hint("auto-hint");
    jQuery('select#chooseExistingSelect').change(selectExistingIRB);
}

function setupAddNewDialog() {
    jQuery("#irbTitleMaxLen").css('color', 'gray');
    jQuery("#irbPNMaxLen").css('color', 'gray');
    jQuery("#irbGIMaxLen").css('color', 'gray');
    
    jQuery('#approvalDateField').keyup(enableSaveButton);
    jQuery('#approvalDateField').datepicker({ 
        dateFormat: "yy-mm-dd",
        onSelect: function() {
            jQuery('#approvalDateField').val(this.value);
            enableSaveButton();
        }
    });
    
    jQuery('#expirationDateField').keyup(enableSaveButton);
    jQuery('#expirationDateField').datepicker({ 
        dateFormat: "yy-mm-dd",
        onSelect: function() {
            jQuery('#expirationDateField').val(this.value);
            enableSaveButton();
        }
    });
    
    jQuery('#protocolNumberField').keyup(enableSaveButton);
    jQuery('#titleField').keyup(enableSaveButton);
    jQuery('#piField').keyup(enableSaveButton);
    jQuery('#editDialogNaCheckbox').click(enableSaveButton);
    jQuery('#grantingInstitutionField').keyup(enableSaveButton);
}

function openSimpleAddIrbDialog() {

    var dialogDiv = document.getElementById('addIRBDialog');
    dialogDiv.innerHTML = "";

    var newForm = document.createElement('form');
    newForm.id   = "addIRBForm";
    newForm.name = "addIRBForm";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";
    newForm.form   = "text/plain";

    var hiddenInput = document.createElement('input');
    hiddenInput.id   = "addIrbWithoutProject";
    hiddenInput.name = "addIrbWithoutProject";
    hiddenInput.value = "true";
    hiddenInput.type  = "hidden";
    newForm.appendChild(hiddenInput);

    var addNewDiv = jQuery('<div id="addNewIRBDiv">');

    var saveButtonStr = "Save";
    addNewDiv.show();
    var addNewDom = addNewDiv.get(0);
    newForm.appendChild(addNewDom);

    initializeAddNewDialog(addNewDom);

    // Finally, attach the form to the div...
    dialogDiv.appendChild(newForm);

    jQuery('#addIRBDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 650,
        height : 660,
        title : "Add an IRB (step 1)",
        buttons : [ {
            id : "save-irb-button",
            text : saveButtonStr,
            click : addIRB
        }, {
            id : "cancel-save-irb-button",
            text : "Cancel",
            click : closeDialog
        } ]
    });

    jQuery('#addNew').click(addNewIRB);

    jQuery('#save-irb-button').button('disable');
    jQuery('#addIRBDialog').dialog('open');

    // Add new...
    setupAddNewDialog();
}

function getListSize(theList) {
    var count = 0;
    for (var key in theList) {
        if (theList.hasOwnProperty(key)) {
            count++;
        }
    }

    return count;
}
function chooseExistingIRB() {
    saveButtonStr = "Select IRB";
    jQuery('#save-irb-button').html('<span class="ui-button-text">'+ saveButtonStr +'</span>')
    jQuery('#chooseExistingIRBDiv').show();
    jQuery('#addNewIRBDiv').hide();

    // Reset titleField on 'addNew' div to make sure Save button isn't falsely enabled.
    jQuery("textarea#titleField").html("");
    enableSaveButton();
}
function addNewIRB() {
    // Disable save button
    jQuery('#save-irb-button').button('disable');

    saveButtonStr = "Save";
    jQuery('#save-irb-button').html('<span class="ui-button-text">'+ saveButtonStr +'</span>')
    jQuery('#addNewIRBDiv').show();
    jQuery('#chooseExistingIRBDiv').hide();
}

function initializeAddNewDialog(addNewDiv) {
    // Protocol Number
    var pnLabel = document.createElement('p');
    pnLabel.innerHTML = "IRB Protocol Number";
    addNewDiv.appendChild(pnLabel);

    var pnField = document.createElement('input');
    pnField.id = "protocolNumberField";
    pnField.name = "protocolNumberField";
    pnField.className = "protocolNumberField";
    pnField.size = 50;
    addNewDiv.appendChild(pnField);

    var pnMaxLen = document.createElement('div');
    pnMaxLen.id = "irbPNMaxLen";
    pnMaxLen.innerHTML = "Enter no more than 50 characters.";
    addNewDiv.appendChild(pnMaxLen);

    // Title of IRB
    var titleLabel = document.createElement('p');
    titleLabel.innerHTML = "Title";
    addNewDiv.appendChild(titleLabel);

    var titleField = document.createElement('textarea');
    titleField.id = "titleField";
    titleField.name = "titleField"
    titleField.rows = 2;
    titleField.cols = 20;
    addNewDiv.appendChild(titleField);

    var titleMaxLen = document.createElement('div');
    titleMaxLen.id = "irbTitleMaxLen";
    titleMaxLen.innerHTML = "Enter no more than 255 characters.";
    addNewDiv.appendChild(titleMaxLen);

    // PI name
    var piLabel = document.createElement('p');
    piLabel.innerHTML = "IRB PI";
    addNewDiv.appendChild(piLabel);

    var piField = document.createElement('input');
    piField.id = "piNameField";
    piField.name = "piNameField";
    piField.className = "piNameField";
    piField.size = 50;
    addNewDiv.appendChild(piField);

    // Approval Date
    var adLabel = document.createElement('p');
    adLabel.innerHTML = "IRB Approval Date";
    addNewDiv.appendChild(adLabel);

    var adField = document.createElement('input');
    adField.id = "approvalDateField";
    adField.name = "approvalDateField";
    adField.type = "text";
    addNewDiv.appendChild(adField);

    // Expiration Date
    var edLabel = document.createElement('p');
    edLabel.innerHTML = "IRB Expiration Date (if applicable)";
    addNewDiv.appendChild(edLabel);

    var edField = document.createElement('input');
    edField.id = "expirationDateField";
    edField.name = "expirationDateField";
    edField.type = "text";
    addNewDiv.appendChild(edField);
    
    var edNaCheckbox = document.createElement('input');
    edNaCheckbox.id = "editDialogNaCheckbox";
    edNaCheckbox.name = "editDialogNaCheckbox";
    edNaCheckbox.type = "checkbox";
    edNaCheckbox.value = "true";
    var edNaLabel = document.createElement('label');
    edNaLabel.htmlFor = "editDialogNaCheckbox";
    edNaLabel.appendChild(document.createTextNode('N/A'));
    addNewDiv.appendChild(edNaCheckbox);
    addNewDiv.appendChild(edNaLabel);

    // Granting Institution
    var giLabel = document.createElement('p');
    giLabel.innerHTML = "Granting Institution";
    addNewDiv.appendChild(giLabel);

    var giField = document.createElement('input');
    giField.id = "grantingInstitutionField";
    giField.name = "grantingInstitutionField";
    giField.className = "grantingInstitutionField";
    giField.size = 50;
    addNewDiv.appendChild(giField);

    var giMaxLen = document.createElement('div');
    giMaxLen.id = "irbGIMaxLen";
    giMaxLen.innerHTML = "Enter no more than 255 characters.";
    addNewDiv.appendChild(giMaxLen);

    // Note before Save button
    var noteLabel = document.createElement('p');
    noteLabel.className = "addFilesNote";
    noteLabel.innerHTML = "You can upload files (step 2) after adding this IRB.";
    addNewDiv.appendChild(noteLabel);
}

function initializeChooseExistingDialog(chooseExistingDiv, irbList) {

    var irbIdInput = document.createElement('input');
    irbIdInput.id   = "irbId";
    irbIdInput.name = "irbId";
    irbIdInput.type  = "hidden";
    chooseExistingDiv.appendChild(irbIdInput);

    var searchByStr = jQuery('input#addIRBSearchBy').val();

    // Search 
    var searchField = document.createElement('input');
    searchField.id = "add_irb_search_by";
    searchField.name = "add_irb_search_by";
    searchField.type = "text";
    searchField.size = 30;
    if (searchByStr == "") {
        searchField.title = "Search by IRB title";
    } else {
        searchField.value = searchByStr;
    }
    chooseExistingDiv.appendChild(searchField);


    var searchImg = document.createElement('img');
    searchImg.id = "add_irb_search_button";
    searchImg.name = "add_irb_search_button";
    searchImg.src = "images/magnifier.png";
    chooseExistingDiv.appendChild(searchImg);


    // IRB Drop-down
    var irbSelect = createIrbSelect(irbList);
    chooseExistingDiv.appendChild(irbSelect);

    // Title
    var titleDiv = document.createElement('div');
    titleDiv.id = "chooseExistingTitle";

    var titleLabel = document.createElement('span');
    titleLabel.className = "choose-existing-label";
    titleLabel.innerHTML = "Title";
    titleDiv.appendChild(titleLabel);

    var titleField = document.createElement('span');
    titleField.id = "chooseExistingTitleField";
    titleField.className = "choose-existing-title-field";
    titleField.innerHTML = " ";
    titleDiv.appendChild(titleField);

    chooseExistingDiv.appendChild(titleDiv);

    // Protocol Number
    var pnDiv = document.createElement('div');
    pnDiv.id = "chooseExistingPN";

    var pnLabel = document.createElement('span');
    pnLabel.className = "choose-existing-pn-label";
    pnLabel.innerHTML = "IRB Protocol Number";
    pnDiv.appendChild(pnLabel);

    var pnField = document.createElement('span');
    pnField.id = "chooseExistingPNField";
    pnField.className = "choose-existing-field";
    pnField.innerHTML = " ";
    pnDiv.appendChild(pnField);

    chooseExistingDiv.appendChild(pnDiv);

    // PI
    var piDiv = document.createElement('div');
    piDiv.id = "chooseExistingPI";

    var piLabel = document.createElement('span');
    piLabel.className = "choose-existing-label";
    piLabel.innerHTML = "IRB PI";
    piDiv.appendChild(piLabel);

    var piField = document.createElement('span');
    piField.id = "chooseExistingPIField";
    piField.className = "choose-existing-field";
    piField.innerHTML = " ";
    piDiv.appendChild(piField);

    chooseExistingDiv.appendChild(piDiv);

    // Approval Date
    var adDiv = document.createElement('div');
    adDiv.id = "chooseExistingAD";

    var adLabel = document.createElement('span');
    adLabel.className = "choose-existing-label";
    adLabel.innerHTML = "IRB Approval Date";
    adDiv.appendChild(adLabel);

    var adField = document.createElement('span');
    adField.id = "chooseExistingADField";
    adField.className = "choose-existing-field";
    adField.innerHTML = " ";
    adDiv.appendChild(adField);

    chooseExistingDiv.appendChild(adDiv);

    // Expiration Date
    var edDiv = document.createElement('div');
    edDiv.id = "chooseExistingED";

    var edLabel = document.createElement('span');
    edLabel.className = "choose-existing-label";
    edLabel.innerHTML = "IRB Expiration Date";
    edDiv.appendChild(edLabel);

    var edField = document.createElement('span');
    edField.id = "chooseExistingEDField";
    edField.className = "choose-existing-field";
    edField.innerHTML = " ";
    edDiv.appendChild(edField);

    chooseExistingDiv.appendChild(edDiv);

    // Granting Institution
    var giDiv = document.createElement('div');
    giDiv.id = "chooseExistingGI";

    var giLabel = document.createElement('span');
    giLabel.className = "choose-existing-label";
    giLabel.innerHTML = "Granting Institution";
    giDiv.appendChild(giLabel);

    var giField = document.createElement('span');
    giField.id = "chooseExistingGIField";
    giField.className = "choose-existing-field";
    giField.innerHTML = " ";
    giDiv.appendChild(giField);

    chooseExistingDiv.appendChild(giDiv);

    // Files (FileName, Uploaded By, Date)
    var emptyList = [];
    var filesDiv = createFilesTableDiv(emptyList, 0);
    chooseExistingDiv.appendChild(filesDiv.get(0));

    // Notes
    var notesDiv = document.createElement('div');
    notesDiv.id = "chooseExistingNotes";

    var notesLabel = document.createElement('span');
    notesLabel.className = "choose-existing-label";
    notesLabel.innerHTML = "Notes";
    notesDiv.appendChild(notesLabel);

    var notesField = document.createElement('span');
    notesField.id = "chooseExistingNotesField";
    notesField.className = "choose-existing-title-field";
    notesField.innerHTML = " ";
    notesDiv.appendChild(notesField);

    chooseExistingDiv.appendChild(notesDiv);

    // Projects with this IRB
    var projectsDiv = createProjectsDiv(emptyList);
    chooseExistingDiv.appendChild(projectsDiv.get(0));
}

function createIrbSelect(irbList) {
    var irbTitleList = [];
    var irbIdList = [];
    for (var key in irbList) {
        if (irbList.hasOwnProperty(key)) {
            irbTitleList.push(irbList[key]);
            irbIdList.push(key);
        }
    }

    var selectField = document.createElement('select');
    selectField.name = "chooseExistingSelect";
    selectField.className = "chooseExistingSelect";
    selectField.id = "chooseExistingSelect";
    selectField.size = 5;

    for (var i = 0; i < irbTitleList.length; i++) {
        anOption = document.createElement('option');
        anOption.value = irbIdList[i];
        anOption.label = irbTitleList[i];   // IE requires this one
        anOption.text = irbTitleList[i];    // ... while FF this one
        selectField.appendChild(anOption);
    }

    return selectField;
}

function createFilesTableDiv(filesList, irbId) {
    return createFilesTableDiv(filesList, irbId, false);
}
function createFilesTableDiv(filesList, irbId, isEdit) {
    var divIdStr = "chooseExistingFiles";
    if (isEdit) {
        divIdStr = "editDialogFiles";
    }
    var filesDiv = jQuery('<div id = ' + divIdStr + '>');

    filesTableId = divIdStr + "Table";
    var filesTable = jQuery('<table id=' + filesTableId + '>');
    var colgroup = jQuery('<colgroup>');
    if (isEdit) {
        colgroup.html("<col style='width: auto' />" + "<col style='width: 140px' />" +
                      "<col style='width: 160px' />" + "<col style='width: 80px' />");
    } else {
        colgroup.html("<col style='width: 40%' />" +
                      "<col style='width: 35%' />" + "<col style='width: 25%' />");
    }
    filesTable.append(colgroup);
    tr = jQuery('<tr>');
    innerHtmlStr = "<td class='irb-fake-header'>Files</td>";
    if (isEdit) {
        innerHtmlStr = innerHtmlStr + "<td class='irb-fake-header'></td>";
    }
    innerHtmlStr = innerHtmlStr +
        "<td class='irb-fake-header'>Uploaded By</td>" +
        "<td class='irb-fake-header'>Date</td>";

    tr.html(innerHtmlStr);
    filesTable.append(tr);

    for (var i = 0; i < filesList.length; i++) {
        tr = jQuery('<tr>');
        td = jQuery('<td>');
        td.html("<a href='javascript:downloadIRBFile(" + irbId +
                ", " + filesList[i].fileId + ")'>" + filesList[i].fileName + "</a>");
        tr.append(td);
        if (isEdit) {
            td = jQuery('<td>');
            td.html('<a href="javascript:showDeleteFileSureDiv(' + irbId + ', ' + filesList[i].fileId + ')">'
                    + '<img src="images/delete.gif" alt="Delete File" title="Delete File"></a>'
                    + '<div id="deleteFileSure_' + irbId + '_' + filesList[i].fileId
                    + '" class="deleteFileSure"> delete file? '
                    + '<a id="irbDeleteNoLink" href="javascript:showDeleteFileLinkDiv(' + irbId
                    + ', ' + filesList[i].fileId + ')">no</a>' + ' / '
                    + '<a id="irbDeleteYesLink" href="javascript:deleteIrbFile(' + irbId
                    + ', ' + filesList[i].fileId + ')">yes</a>'
                    + '</div>');
                
            tr.append(td);
        }
        td = jQuery('<td>');
        td.html(filesList[i].fileOwner);
        tr.append(td);
        td = jQuery('<td>');
        td.html(filesList[i].addedTime);
        tr.append(td);
        filesTable.append(tr);
    }
    filesDiv.append(filesTable);

    return filesDiv;
}

function createProjectsDiv(projectList) {
    return createProjectsDiv(projectList, false);
}
function createProjectsDiv(projectList, isEdit) {
    var divIdStr = "chooseExistingProjects";
    if (isEdit) {
        divIdStr = "editDialogProjects";
    }

    var projectNameList = [];
    var projectIdList = [];
    for (var key in projectList) {
        if (projectList.hasOwnProperty(key)) {
            projectNameList.push(key);
            projectIdList.push(projectList[key]);
        }
    }

    var projectsDiv = jQuery('<div id = ' + divIdStr + '>');

    projectsTableId = divIdStr + "Table";
    var projectsTable = jQuery('<table id=' + projectsTableId + '>');

    tr = jQuery('<tr>');
    tr.html("<td class='irb-fake-header'>Projects with this IRB</td>");
    projectsTable.append(tr);
    for (var i = 0; i < projectNameList.length; i++) {
        tr = jQuery('<tr>');
        td = jQuery('<td>');
        td.html('<a target="_parent" href="Project?id=' + projectIdList[i] + '">' + projectNameList[i] + '</a>');
        tr.append(td);
        projectsTable.append(tr);
    }
    projectsDiv.append(projectsTable);

    return projectsDiv;
}

function selectExistingIRB() {
    var irbId = jQuery('#chooseExistingSelect option:selected').val();

    // Update hidden input with irbId
    hiddenIrbIdInput = jQuery('#irbId');
    hiddenIrbIdInput.val(irbId);

    new Ajax.Request("ProjectIRB", {
            parameters: {
                requestingMethod: "IRBs.selectExistingIRB",
                    ajaxRequest: "true",
                    id: projectId,
                    getIRB: "true",
                    irbId: irbId
                    },
                onComplete: populateExistingIrbDialog,
                onException: function (request, exception) {
                throw(exception);
            }
    });
}

function populateExistingIrbDialog(transport) {

    var json = transport.responseText.evalJSON(true);
    jQuery('#chooseExistingTitleField').html(json.title);
    jQuery('#chooseExistingPNField').html(json.protocolNumber);
    jQuery('#chooseExistingPIField').html(json.pi);
    jQuery('#chooseExistingADField').html(json.approvalDate);
    jQuery('#chooseExistingEDField').html(json.expirationDate);
    jQuery('#chooseExistingGIField').html(json.grantingInstitution);
    jQuery('#chooseExistingNotesField').html(json.notes);

    var filesDiv = createFilesTableDiv(json.files, json.irbId);
    jQuery('#chooseExistingFiles').html(filesDiv.html());

    var projectsDiv = createProjectsDiv(json.projects);
    jQuery('#chooseExistingProjects').html(projectsDiv.html());

    // Enable save button
    jQuery('#save-irb-button').button('enable');
}

function resetExistingIrbDialog() {
    jQuery('#chooseExistingTitleField').html("");
    jQuery('#chooseExistingPNField').html("");
    jQuery('#chooseExistingPIField').html("");
    jQuery('#chooseExistingADField').html("");
    jQuery('#chooseExistingEDField').html("");
    jQuery('#chooseExistingGIField').html("");
    jQuery('#chooseExistingNotesField').html("");

    var emptyList = [];
    var filesDiv = createFilesTableDiv(emptyList, 0);
    jQuery('#chooseExistingFiles').html(filesDiv.html());
    
    var projectsDiv = createProjectsDiv(emptyList);
    jQuery('#chooseExistingProjects').html(projectsDiv.html());
    
    // Disable save button
    jQuery('#save-irb-button').button('disable');
}

function enableSaveButton() {
    var titleStr = jQuery("textarea#titleField").val();
    var titleFlag = (titleStr.length <= 255) && (titleStr.length > 0);
    var titleGiven = titleStr.length > 0;
    var pnStr = jQuery("input#protocolNumberField").val();
    var pnFlag = (pnStr.length <= 50) && (pnStr.length > 0);
    var pnGiven = pnStr.length > 0;
    var piStr = jQuery("input#piNameField").val();
    var piFlag = (piStr.length <= 255) && (piStr.length > 0);
    var adStr = jQuery("input#approvalDateField").val();
    var adFlag = adStr.length > 0;
    
    var edDateStr = jQuery("input#expirationDateField").val();
    var edDateFlag = edDateStr.length > 0;

    var edNaStr = jQuery("input#editDialogNaCheckbox").is(':checked');
    var edNaFlag = (edNaStr == true);
    if (edNaFlag) {
        //clear the ed date field
        jQuery("input#expirationDateField").val("");
        disableField("input#expirationDateField");
    } else {
        enableField("input#expirationDateField");
    }
     
    var giStr = jQuery("input#grantingInstitutionField").val();
    var giFlag = (giStr.length <= 255) && (giStr.length > 0);
    var giGiven = giStr.length > 0;

    if (titleFlag) {
        jQuery('#irbTitleMaxLen').css('color', 'gray');
    }
    if (pnFlag) {
        jQuery('#irbPNMaxLen').css('color', 'gray');
    }
    if (giFlag) {
        jQuery('#irbGIMaxLen').css('color', 'gray');
    }
    
    if (titleFlag && pnFlag && piFlag && adFlag && giFlag && (edDateFlag || edNaFlag)) {
        jQuery('#save-irb-button').button('enable');
    } else {
        jQuery('#save-irb-button').button('disable');
        if (!titleFlag && titleGiven) {
            jQuery('#irbTitleMaxLen').css('color', 'red');
        }
        if (!pnFlag && pnGiven) {
            jQuery('#irbPNMaxLen').css('color', 'red');
        }
        if (!giFlag && giGiven) {
            jQuery('#irbGIMaxLen').css('color', 'red');
        }
    }
}

// Submit the form that will add the IRB to the repository.
function addIRB() {
    // Should we remove the div not in use... chooseExisting or addNew?
    jQuery('#addIRBForm').submit();
    jQuery('#addIRBDialog').dialog('close');
}

// A simple close modal dialog function.
function closeDialog() {
    jQuery(this).dialog("close");
}

function addIRBSearchBy(event) {
    var key = "";
    if (this.id == 'add_irb_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'add_irb_search_by')
        | this.id == 'add_irb_search_button';
    if (testEvent) {
        resetExistingIrbDialog();
        var searchString = jQuery("#add_irb_search_by").val();
        if (searchString == jQuery("#add_irb_search_by").attr("title")) {
            searchString = "";
        }
        new Ajax.Request("ProjectIRB", {
            parameters: {
                requestingMethod: "IRBs.addIRBSearchBy",
                id: projectId,
                searchBy: searchString,
                getIRBList: "true"
            },
            onComplete: refreshAddIRBDialog,
            onException: function (request, exception) {
                throw(exception);
            }
         });
    }
    return false;
}

function refreshAddIRBDialog(transport) {

    // Get list of IRB names...
    var json = transport.responseText.evalJSON(true);
    var irbList = json.irbList;

    updateIrbSelect(irbList);
    
    // update hidden 'search by' value
    var searchString = jQuery("#add_irb_search_by").val();
    if (searchString == jQuery("#add_irb_search_by").attr("title")) {
        searchString = "";
    }
    jQuery('input#addIRBSearchBy').val(searchString);
}
function updateIrbSelect(irbList) {
    var irbTitleList = [];
    var irbIdList = [];
    for (var key in irbList) {
        if (irbList.hasOwnProperty(key)) {
            irbTitleList.push(irbList[key]);
            irbIdList.push(key);
        }
    }

    var selectField = document.getElementById('chooseExistingSelect');
    clearContents(selectField);

    for (var i = 0; i < irbTitleList.length; i++) {
        anOption = document.createElement('option');
        anOption.value = irbIdList[i];
        anOption.label = irbTitleList[i];  // IE requires this one
        anOption.text = irbTitleList[i];   // ... while FF this one
        selectField.appendChild(anOption);
    }
}

//----------------------------
// IRB Project page
//   Add IRB file
//----------------------------
function openAddFileDialog(irbId) {
    openAddFileDialog(irbId, false);
}
function openAddFileDialog(irbId, isEdit) {
    // Get the div...
    var addFileDiv = document.getElementById('addIRBFileDialog');
    addFileDiv.innerHTML = "";

    var action = "ProjectIRB?id=" + projectId + "&irbId=" + irbId;

    // If invoked from 'Edit IRB' dialog, make note of it.
    if (isEdit) {
        action = action + "&isEdit=true";
    }

    var f = document.createElement('form');
    f.name = "addIRBFileForm";
    f.id = "addIRBFileForm";
    f.action = action;
    f.method = "post";
    f.enctype = "multipart/form-data";
    f.encoding = "multipart/form-data";  // needed for IE7
    f.onsubmit = "";

    var p1 = document.createElement('p');
    p1.innerHTML = "Choose a file to upload";
    f.appendChild(p1);

    var chooseFile = document.createElement('input');
    chooseFile.name = "fileName";
    chooseFile.type = "file";
    chooseFile.id = "file-file-chooser";
    f.appendChild(chooseFile);

    var h6 = document.createElement('h6');
    h6.innerHTML = "File size must not exceed 400MB.";
    h6.id = "fileSizeWarning";
    f.appendChild(h6);

    // Attach the form to the div
    addFileDiv.appendChild(f);

    jQuery('#addIRBFileDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 440,
        height : 300,
        title : "Add IRB File",
        buttons : [ {
            id : "file-button-upload",
            text : "Upload",
            click : addIRBFile
        }, {
            id : "file-button-cancel",
            text : "Cancel",
            click : closeFileDialog
        } ]
    });

    jQuery('#file-button-upload').button('disable');
    jQuery('#file-file-chooser').change(enableFileAddButton);

    jQuery('#addIRBFileDialog').dialog('open');
}

function enableFileAddButton() {
    var fileFlag = ((jQuery('#file-file-chooser').val() != "")
                    && (jQuery('#file-file-chooser').val() != undefined));

    if (fileFlag) {
        jQuery('#file-button-upload').button('enable');
    } else {
        jQuery('#file-button-upload').button('disable');
    }
}

function addIRBFile() {
    jQuery('#addIRBFileForm').submit();
    jQuery('#addIRBFileDialog').dialog('close');
}

function closeFileDialog() {
    jQuery(this).dialog("close");
}

//----------------------------
// IRB Project page
//   Edit IRB
//----------------------------
function openEditIRBDialog(irbId) {
    // Make note of the fact that the 'Edit IRB' dialog is open.
    editIRBIsOpen(irbId);

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.openEditIRBDialog",
            ajaxRequest: "true",
            id: projectId,
            getIRB: "true",
            irbId: irbId
        },
        onComplete: populateEditIRBDialog,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}

function populateEditIRBDialog(transport) {
    var json = transport.responseText.evalJSON(true);
    
    irbId = json.irbId;

    // Get the div...
    var editDiv = document.getElementById('editIRBDialog');
    editDiv.innerHTML = "";

    // Title
    var titleDiv = document.createElement('div');
    titleDiv.id = "editDialogTitle";

    var titleField = document.createElement('span');
    titleField.id = "editDialogTitleField";
    titleField.className = "edit-dialog-title-field";
    titleField.innerHTML = json.title;
    titleDiv.appendChild(titleField);

    editDiv.appendChild(titleDiv);

    // Protocol Number
    var pnDiv = document.createElement('div');
    pnDiv.id = "editDialogPN";

    var pnLabel = document.createElement('span');
    pnLabel.className = "edit-dialog-label";
    pnLabel.innerHTML = "IRB Protocol Number";
    pnDiv.appendChild(pnLabel);

    var pnEditLink = document.createElement('span');
    pnEditLink.className = "irb-edit-irb-link";
    pnEditLink.id = "pnEditLink";
    pnEditLink.innerHTML = "<a href='javascript:showEditPNDiv(" + irbId + ")'>edit</a>";
    pnDiv.appendChild(pnEditLink);

    var pnField = document.createElement('span');
    pnField.id = "editDialogPNField";
    pnField.className = "edit-dialog-field";
    pnField.innerHTML = json.protocolNumber;
    pnDiv.appendChild(pnField);

    var pnEdit = document.createElement('div');
    pnEdit.id = "pnEditDiv";
    pnEdit.innerHTML = '<input id="pnField" class="pnField" name="pnField" size="15" value="' + json.protocolNumber + '">' +
        '<input type="button" id="pnSaveButton" value="Save" onclick="javascript:savePNChange(' + irbId + ')" />' +
        '<input type="button" id="pnCancelButton" value="Cancel" onclick="javascript:cancelPNChange()" />' +
        '<input type="hidden" id="pnHiddenField" value="' + json.protocolNumber + '" />';
    pnDiv.appendChild(pnEdit);

    editDiv.appendChild(pnDiv);

    // PI
    var piDiv = document.createElement('div');
    piDiv.id = "editDialogPI";

    var piLabel = document.createElement('span');
    piLabel.className = "edit-dialog-label";
    piLabel.innerHTML = "IRB PI";
    piDiv.appendChild(piLabel);

    var piEditLink = document.createElement('span');
    piEditLink.className = "irb-edit-irb-link";
    piEditLink.id = "piEditLink";
    piEditLink.innerHTML = "<a href='javascript:showEditPIDiv(" + irbId + ")'>edit</a>";
    piDiv.appendChild(piEditLink);

    var piField = document.createElement('span');
    piField.id = "editDialogPIField";
    piField.className = "edit-dialog-field";
    piField.innerHTML = json.pi;
    piDiv.appendChild(piField);

    var piEdit = document.createElement('div');
    piEdit.id = "piEditDiv";
    piEdit.innerHTML = '<input id="piField" class="piField" name="piField" size="15" value="' + json.pi + '">' +
        '<input type="button" id="piSaveButton" value="Save" onclick="javascript:savePIChange(' + irbId + ')" />' +
        '<input type="button" id="piCancelButton" value="Cancel" onclick="javascript:cancelPIChange()" />' +
        '<input type="hidden" id="piHiddenField" value="' + json.pi + '" />';
    piDiv.appendChild(piEdit);

    editDiv.appendChild(piDiv);

    // Approval Date
    var adDiv = document.createElement('div');
    adDiv.id = "editDialogAD";

    var adLabel = document.createElement('span');
    adLabel.className = "edit-dialog-label";
    adLabel.innerHTML = "IRB Approval Date";
    adDiv.appendChild(adLabel);

    var adEditLink = document.createElement('span');
    adEditLink.className = "irb-edit-irb-link";
    adEditLink.id = "adEditLink";
    adEditLink.innerHTML = "<a href='javascript:showEditADDiv(" + irbId + ")'>edit</a>";
    adDiv.appendChild(adEditLink);

    var adField = document.createElement('span');
    adField.id = "editDialogADField";
    adField.className = "edit-dialog-field";
    adField.innerHTML = json.approvalDate;
    adDiv.appendChild(adField);

    var adEdit = document.createElement('div');
    adEdit.id = "adEditDiv";
    adEdit.innerHTML = '<input id="adField" type="text" name="adField" size="15" value="' + json.approvalDate + '">' +
        '<input type="button" id="adSaveButton" value="Save" onclick="javascript:saveADChange(' + irbId + ')" />' +
        '<input type="button" id="adCancelButton" value="Cancel" onclick="javascript:cancelADChange()" />' +
        '<input type="hidden" id="adHiddenField" value="' + json.approvalDate + '" />';
    adDiv.appendChild(adEdit);
    editDiv.appendChild(adDiv);

    jQuery('#adField').datepicker({ dateFormat: "yy-mm-dd" });

    // Expiration Date
    var edDiv = document.createElement('div');
    edDiv.id = "editDialogED";

    var edLabel = document.createElement('span');
    edLabel.className = "edit-dialog-label";
    edLabel.innerHTML = "IRB Expiration Date";
    edDiv.appendChild(edLabel);

    var edEditLink = document.createElement('span');
    edEditLink.className = "irb-edit-irb-link";
    edEditLink.id = "edEditLink";
    edEditLink.innerHTML = "<a href='javascript:showEditEDDiv(" + irbId + ")'>edit</a>";
    edDiv.appendChild(edEditLink);
    
    var checkedFlag = "";
    var viewExpDate = "";
    var editExpDate = "";
    if (json.expirationDate == "Not applicable") {
        checkedFlag = " checked";
        viewExpDate = json.expirationDate;
        editExpDate = "";
    } else {
        viewExpDate = json.expirationDate;
        editExpDate = json.expirationDate;
    }

    var edField = document.createElement('span');
    edField.id = "editDialogEDField";
    edField.className = "edit-dialog-field";
    edField.innerHTML = viewExpDate;
    edDiv.appendChild(edField);

    var edEdit = document.createElement('div');
    edEdit.id = "edEditDiv";
    edEdit.innerHTML =
        '<input id="edField" type="text" name="edField" size="15" value="' + editExpDate + '" onclick="javascript:checkEdNaBox()">' +
        '<input type="checkbox" id="edNaCheckbox" value="true" onclick="javascript:checkEdNaBox()"' + checkedFlag + '>N/A' +
        '<input type="button" id="edSaveButton" value="Save" onclick="javascript:saveEDChange(' + irbId + ')" />' +
        '<input type="button" id="edCancelButton" value="Cancel" onclick="javascript:cancelEDChange()" />' +
        '<input type="hidden" id="edHiddenField" value="' + editExpDate + '" />';
    edDiv.appendChild(edEdit);
    
    editDiv.appendChild(edDiv);

    jQuery('#edField').datepicker({ 
                dateFormat: "yy-mm-dd",
                onSelect: function() {
                    jQuery('#edField').val(this.value);
                    checkEdDate(this.value);
                }
            });
    
    if (json.expirationDate == "Not applicable") {
        disableField('#edField');
    } else {
        enableField('#edField');
    }

    // Granting Institution
    var giDiv = document.createElement('div');
    giDiv.id = "editDialogGI";

    var giLabel = document.createElement('span');
    giLabel.className = "edit-dialog-label";
    giLabel.innerHTML = "Granting Institution";
    giDiv.appendChild(giLabel);

    var giEditLink = document.createElement('span');
    giEditLink.className = "irb-edit-irb-link";
    giEditLink.id = "giEditLink";
    giEditLink.innerHTML = "<a href='javascript:showEditGIDiv(" + irbId + ")'>edit</a>";
    giDiv.appendChild(giEditLink);

    var giField = document.createElement('span');
    giField.id = "editDialogGIField";
    giField.className = "edit-dialog-field";
    giField.innerHTML = json.grantingInstitution;
    giDiv.appendChild(giField);

    var giEdit = document.createElement('div');
    giEdit.id = "giEditDiv";
    giEdit.innerHTML = '<input id="giField" class="giField" name="giField" size="15" value="' + json.grantingInstitution + '">' +
        '<input type="button" id="giSaveButton" value="Save" onclick="javascript:saveGIChange(' + irbId + ')" />' +
        '<input type="button" id="giCancelButton" value="Cancel" onclick="javascript:cancelGIChange()" />' +
        '<input type="hidden" id="giHiddenField" value="' + json.grantingInstitution + '" />';
    giDiv.appendChild(giEdit);
    editDiv.appendChild(giDiv);

    editDiv.appendChild(giDiv);

    // Notes
    var notesDiv = document.createElement('div');
    notesDiv.id = "editDialogNotes";

    var notesLabel = document.createElement('span');
    notesLabel.className = "edit-dialog-label";
    notesLabel.innerHTML = "Notes";
    notesDiv.appendChild(notesLabel);

    var notesEditLink = document.createElement('span');
    notesEditLink.className = "irb-edit-irb-link";
    notesEditLink.id = "notesEditLink";
    notesEditLink.innerHTML = "<a href='javascript:showEditNotesDiv(" + irbId + ")'>edit</a>";
    notesDiv.appendChild(notesEditLink);

    var notesField = document.createElement('span');
    notesField.id = "editDialogNotesField";
    notesField.className = "edit-dialog-title-field";
    notesField.innerHTML = json.notes;
    notesDiv.appendChild(notesField);

    var notesEdit = document.createElement('div');
    notesEdit.id = "notesEditDiv";
    notesEdit.innerHTML = '<textarea rows="8" cols="50" id="notesField" class="notesField" ' +
        'name="notesField">' + json.notes + '</textarea>' +
        '<input type="button" id="notesSaveButton" value="Save" onclick="javascript:saveNotesChange(' + irbId + ')" />' +
        '<input type="button" id="notesCancelButton" value="Cancel" onclick="javascript:cancelNotesChange()" />' +
        '<input type="hidden" id="notesHiddenField" value="' + json.notes + '" />';
    notesDiv.appendChild(notesEdit);
    editDiv.appendChild(notesDiv);

    editDiv.appendChild(notesDiv);

    // Files (FileName, Uploaded By, Date)
    var filesDiv = createFilesTableDiv(json.files, irbId, true);
    jQuery('#editDialogFiles').html(filesDiv.html());
    editDiv.appendChild(filesDiv.get(0));

    // Add file button...
    var addFileInput = document.createElement('input');
    addFileInput.id = "editDialogAddFile";
    addFileInput.type = "button";
    addFileInput.value = "Add file";
    addFileInput.onclick = function() {openAddFileDialog(irbId, true)};
    editDiv.appendChild(addFileInput);

    // Projects with this IRB
    var projectsDiv = createProjectsDiv(json.projects, true);
    jQuery('#editDialogProjects').html(projectsDiv.html());
    editDiv.appendChild(projectsDiv.get(0));

    var numProjects = json.numProjects;

    // Delete this IRB, only if appropriate
    var isAdmin = jQuery('input#adminUserFlag').val() == "true" ? true : false;
    var currentUser = jQuery('input#currentUser').val();

    var linkEnabled = false;
    var deleteLinkClass = "remove-irb-link-inactive";
    if (numProjects == "0") {
        if (isAdmin || (currentUser == json.addedBy)) {
            deleteLinkClass = "remove-irb-link";
            linkEnabled = true;
        }
    } else if (numProjects == "1") {
        // DS1549.
        if ((projectId != null) && (currentUser == json.addedBy)) {
            deleteLinkClass = "remove-irb-link";
            linkEnabled = true;
        }
    }

    var deleteDiv = document.createElement('div');
    deleteDiv.id = "deleteIRBDiv";
    deleteDiv.name = "deleteIRBDiv";

    var irbDeleteDiv = document.createElement('div');
    irbDeleteDiv.id = "irbDelete_" + irbId;
    if (linkEnabled) {
        irbDeleteDiv.innerHTML = "<a class='" + deleteLinkClass
            + "' href='javascript:showIrbDeleteSureDiv(" + irbId + ")'> Delete this IRB</a>";
    } else {
        irbDeleteDiv.innerHTML = "<span class='" + deleteLinkClass + "'> Delete this IRB</span>";
    }
    deleteDiv.appendChild(irbDeleteDiv);

    var deleteSureDiv = document.createElement('div');
    deleteSureDiv.id = "irbDeleteSure_" + irbId;
    deleteSureDiv.className = "irbDeleteSure";
    deleteSureDiv.innerHTML = "delete this IRB?&nbsp" +
        "<a id='irbDeleteNoLink' href='javascript:showIrbDeleteLinkDiv(" + irbId + ")'>no</a>" +
        "&nbsp/&nbsp" +
        "<a id='irbDeleteYesLink' href='javascript:deleteIRB(" + irbId + ")'>yes</a>";
    deleteDiv.appendChild(deleteSureDiv);

    editDiv.appendChild(deleteDiv);

    jQuery('#editIRBDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 850,
        height : 660,
        title : "Edit IRB",
        buttons : [ {
            id : "save-edit-button",
            text : "Close",
            click : closeEditDialog
        } ]
    });

    jQuery('#editIRBDialog').dialog('open');
}

function closeEditDialog() {
    // Make note of the fact that the 'Edit IRB' dialog is no longer open.
    editIRBIsClosed();

    jQuery(this).dialog("close");

    // Reload page to update fields.
    window.location.assign(window.location.href);
}

//---------------------------
// IRB Edit page
//   Protocol Number
//---------------------------
function showEditPNDiv(irbId) {
    jQuery('#editDialogPNField').hide();
    jQuery('#pnEditLink').hide();
    jQuery('#pnEditDiv').css("display", "inline-block");
}
function hideEditPNDiv() {
    jQuery('#editDialogPNField').show();
    jQuery('#pnEditLink').show();
    jQuery('#pnEditDiv').hide();
}
function savePNChange(irbId) {
    var theValue = jQuery('#pnField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.savePNChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editProtocolNumber: "true",
            value: theValue
        },
        onComplete: handleSavePNResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSavePNResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogPNField");
        hiddenField = jQuery("#pnHiddenField");
        shownField.html(json.value);
        hiddenField.val(json.value);
        hideEditPNDiv();
        successPopup(json.message);
    } else {
        revertPNChange();
        errorPopup(json.message);
    }
}

function cancelPNChange() {
    hideEditPNDiv();
    revertPNChange();
}
function revertPNChange() {
    prevVal = jQuery("#pnHiddenField").val();
    jQuery("#editDialogPNField").html(prevVal);
    jQuery("#pnField").val(prevVal);
}

//---------------------------
// IRB Edit page
//   PI
//---------------------------
function showEditPIDiv(irbId) {
    jQuery('#editDialogPIField').hide();
    jQuery('#piEditLink').hide();
    jQuery('#piEditDiv').css("display", "inline-block");
}
function hideEditPIDiv() {
    jQuery('#editDialogPIField').show();
    jQuery('#piEditLink').show();
    jQuery('#piEditDiv').hide();
}
function savePIChange(irbId) {
    var theValue = jQuery('#piField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.savePIChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editPI: "true",
            value: theValue
        },
        onComplete: handleSavePIResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSavePIResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogPIField");
        hiddenField = jQuery("#piHiddenField");
        shownField.html(json.value);
        hiddenField.val(json.value);
        hideEditPIDiv();
        successPopup(json.message);
    } else {
        revertPIChange();
        errorPopup(json.message);
    }
}

function cancelPIChange() {
    hideEditPIDiv();
    revertPIChange();
}
function revertPIChange() {
    prevVal = jQuery("#piHiddenField").val();
    jQuery("#editDialogPIField").html(prevVal);
    jQuery("#piField").val(prevVal);
}

//---------------------------
// IRB Edit page
//   Approval Date
//---------------------------
function showEditADDiv(irbId) {
    jQuery('#editDialogADField').hide();
    jQuery('#adEditLink').hide();
    jQuery('#adEditDiv').css("display", "inline-block");
}
function hideEditADDiv() {
    jQuery('#editDialogADField').show();
    jQuery('#adEditLink').show();
    jQuery('#adEditDiv').hide();
}
function saveADChange(irbId) {
    var theValue = jQuery('#adField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.saveADChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editApprovalDate: "true",
            value: theValue
        },
        onComplete: handleSaveADResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveADResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    hideEditADDiv();
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogADField");
        hiddenField = jQuery("#adHiddenField");
        shownField.html(json.value);
        hiddenField.val(json.value);
        successPopup(json.message);
    } else {
        revertADChange();
        errorPopup(json.message);
    }
}

function cancelADChange() {
    hideEditADDiv();
    revertADChange();
}
function revertADChange() {
    prevVal = jQuery("#adHiddenField").val();
    jQuery("#editDialogADField").html(prevVal);
    jQuery("#adField").val(prevVal);
}

//---------------------------
// IRB Edit page
//   Expiration Date
//---------------------------
function checkEdDate(edDate) {
    var edDateFlag = edDate.length > 0;
    if (edDateFlag) {
        enableField("#edSaveButton");
    } else {
        disableField("#edSaveButton");
    }
}
function checkEdNaBox() {
    jQuery('#edField').val("");
    
    var edNaStr = jQuery("input#edNaCheckbox").is(':checked');
    var edNaFlag = (edNaStr == true);
    if (edNaFlag) {
        //clear the ed date field
        jQuery("#edField").val("");
        disableExpDateField();
        enableField("#edSaveButton");
    } else {
        enableExpDateField();
        disableField("#edSaveButton");
    }
}
function enableExpDateField() {
    jQuery("#edField").removeAttr("disabled");
}
function disableExpDateField() {
    jQuery("#edField").attr("disabled", "disabled");
}

function showEditEDDiv(irbId) {
    jQuery('#editDialogEDField').hide();
    jQuery('#edEditLink').hide();
    jQuery('#edEditDiv').css("display", "inline-block");
}
function hideEditEDDiv() {
    jQuery('#editDialogEDField').show();
    jQuery('#edEditLink').show();
    jQuery('#edEditDiv').hide();
}
function saveEDChange(irbId) {
    var theValue = jQuery('#edField').val();
    
    var naFlag = "false";
    var edNaStr = jQuery("input#edNaCheckbox").is(':checked');
    if (edNaStr == true) {
        naFlag = "true";
    }

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.saveEDChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editExpirationDate: "true",
            naFlag: naFlag,
            value: theValue
        },
        onComplete: handleSaveEDResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveEDResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    hideEditEDDiv();
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogEDField");
        hiddenField = jQuery("#edHiddenField");
        if (json.value == "Not applicable" || json.value == "") {
            shownField.html("Not applicable");
            hiddenField.val("");
            disableField('#edField');
        } else {
            shownField.html(json.value);
            hiddenField.val(json.value);
            enableField('#edField');
        }
        successPopup(json.message);
    } else {
        revertEDChange();
        errorPopup(json.message);
    }
}

function cancelEDChange() {
    hideEditEDDiv();
    revertEDChange();
}
function revertEDChange() {
    var viewVal = "";
    var editVal = jQuery("#edHiddenField").val();
    if (editVal == "") {
        viewVal = "Not applicable";
        disableField('#edField');
        jQuery('#edNaCheckbox').prop('checked', true);
    } else {
        viewVal = editVal;
        enableField('#edField');
        jQuery('#edNaCheckbox').prop('checked', false);
    }
    jQuery("#editDialogEDField").html(viewVal);
    jQuery("#edField").val(editVal);
}

function enableField(fieldName) {
    jQuery(fieldName).removeAttr("disabled");
}
function disableField(fieldName) {
    jQuery(fieldName).attr("disabled", "disabled");
}

//---------------------------
// IRB Edit page
//   Granting Institution
//---------------------------
function showEditGIDiv(irbId) {
    jQuery('#editDialogGIField').hide();
    jQuery('#giEditLink').hide();
    jQuery('#giEditDiv').css("display", "inline-block");
}
function hideEditGIDiv() {
    jQuery('#editDialogGIField').show();
    jQuery('#giEditLink').show();
    jQuery('#giEditDiv').hide();
}
function saveGIChange(irbId) {
    var theValue = jQuery('#giField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.saveGIChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editGrantingInstitution: "true",
            value: theValue
        },
        onComplete: handleSaveGIResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveGIResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogGIField");
        hiddenField = jQuery("#giHiddenField");
        shownField.html(json.value);
        hiddenField.val(json.value);
        hideEditGIDiv();
        successPopup(json.message);
    } else {
        revertGIChange();
        errorPopup(json.message);
    }
}

function cancelGIChange() {
    hideEditGIDiv();
    revertGIChange();
}
function revertGIChange() {
    prevVal = jQuery("#giHiddenField").val();
    jQuery("#editDialogGIField").html(prevVal);
    jQuery("#giField").val(prevVal);
}

//---------------------------
// IRB Edit page
//   Files
//---------------------------
// Hide link div, show 'Are you sure' div
function showDeleteFileSureDiv(irbId, fileId) {
    irbSureDiv = $("deleteFileSure_" + irbId + "_" + fileId);
    irbSureDiv.style.display = "inline";
}

// Show link div, hide 'Are you sure' div
function showDeleteFileLinkDiv(irbId, fileId) {
    irbSureDiv = $("deleteFileSure_" + irbId + "_" + fileId);
    irbSureDiv.style.display = "none";
}
function deleteIrbFile(irbId, fileId) {

    // If file is deleted via 'All IRBs' page,  projectId isn't set.
    var action = "ProjectIRB";
    if (projectId != null) {
        action = action + "?id=" + projectId;
    }

    var newForm = document.createElement('FORM');
    newForm.id   = "delete_irb_file_form";
    newForm.name = "delete_irb_file_form";
    newForm.form = "text/plain";
    newForm.action = action;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "deleteFile";
    newInput.id  = "deleteFile";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "irbId";
    newInput.type  = "hidden";
    newInput.value = irbId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "fileId";
    newInput.type  = "hidden";
    newInput.value = fileId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//---------------------------
// IRB Edit page
//   Notes
//---------------------------
function showEditNotesDiv(irbId) {
    jQuery('#editDialogNotesField').hide();
    jQuery('#notesEditLink').hide();
    jQuery('#notesEditDiv').css("display", "inline-block");
}
function hideEditNotesDiv() {
    jQuery('#editDialogNotesField').show();
    jQuery('#notesEditLink').show();
    jQuery('#notesEditDiv').hide();
}
function saveNotesChange(irbId) {
    var theValue = jQuery('#notesField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.saveNotesChange",
            id: projectId,
            irbId: irbId,
            ajaxRequest: "true",
            editNotes: "true",
            value: theValue
        },
        onComplete: handleSaveNotesResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveNotesResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = jQuery("#editDialogNotesField");
        hiddenField = jQuery("#notesHiddenField");
        shownField.html(json.value);
        hiddenField.val(json.value);
        hideEditNotesDiv();
        successPopup(json.message);
    } else {
        revertNotesChange();
        errorPopup(json.message);
    }
}

function cancelNotesChange() {
    hideEditNotesDiv();
    revertNotesChange();
}
function revertNotesChange() {
    prevVal = jQuery("#notesHiddenField").val();
    jQuery("#editDialogNotesField").html(prevVal);
    jQuery("#notesField").val(prevVal);
}

//---------------------------
// IRB Edit page
//   Delete IRB
//---------------------------
// Hide link div, show 'Are you sure' div
function showIrbDeleteSureDiv(irbId) {
    irbSureDiv = $("irbDeleteSure_" + irbId);
    irbSureDiv.style.display = "inline";

    irbLinkDiv = $("irbDelete_" + irbId);
    irbLinkDiv.style.display = "none";
}

// Show link div, hide 'Are you sure' div
function showIrbDeleteLinkDiv(irbId) {
    irbSureDiv = $("irbDeleteSure_" + irbId);
    irbSureDiv.style.display = "none";

    irbLinkDiv = $("irbDelete_" + irbId);
    irbLinkDiv.style.display = "inline";
}

function deleteIRB(irbId) {
    showIrbDeleteLinkDiv(irbId);

    var newForm = document.createElement('FORM');
    newForm.id   = "delete_irb_form";
    newForm.name = "delete_irb_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?irbId=" + irbId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "deleteIRB";
    newInput.id  = "deleteIRB";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    if (projectId != null) {
        var projectIdInput = document.createElement('input');
        projectIdInput.id   = "id";
        projectIdInput.name = "id";
        projectIdInput.value = projectId;
        projectIdInput.type  = "hidden";
        newForm.appendChild(projectIdInput);
    }

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//----------------------------
// IRB Project page
//   Remove IRB
//----------------------------
// Hide link div, show 'Are you sure' div
function showIrbRemoveSureDiv(irbId) {
    irbSureDiv = $("irbRemoveSure_" + irbId);
    irbSureDiv.style.display = "inline";

    irbLinkDiv = $("irbDocRemove_" + irbId);
    irbLinkDiv.style.display = "none";
}

// Show link div, hide 'Are you sure' div
function showIrbRemoveLinkDiv(irbId) {
    irbSureDiv = $("irbRemoveSure_" + irbId);
    irbSureDiv.style.display = "none";

    irbLinkDiv = $("irbDocRemove_" + irbId);
    irbLinkDiv.style.display = "inline";
}

function removeIRB(irbId, projectId) {
    showIrbRemoveLinkDiv(irbId);

    var newForm = document.createElement('FORM');
    newForm.id   = "remove_irb_form";
    newForm.name = "remove_irb_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "removeIRB";
    newInput.id  = "removeIRB";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "irbId";
    newInput.type  = "hidden";
    newInput.value = irbId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//----------------------------
// IRB Project page
//   Research Manager's Notes
//----------------------------
function showEditRMNotesDiv() {
    jQuery('#irbInfoRMNotesDiv').hide();
    jQuery('#irbEditRMNotesLink').hide();
    jQuery('#irbInfoEditRMNotesDiv').show();
}
function hideEditRMNotesDiv() {
    jQuery('#irbInfoRMNotesDiv').show();
    jQuery('#irbEditRMNotesLink').show();
    jQuery('#irbInfoEditRMNotesDiv').hide();
}
function saveRMNotesChange(projectId) {
    var theValue = jQuery('#rmNotesField').val();

    new Ajax.Request("ProjectIRB", {
        parameters: {
            requestingMethod: "IRBs.saveRMNotesChange",
            id: projectId,
            ajaxRequest: "true",
            editRMNotes: "true",
            value: theValue
        },
        onComplete: handleSaveRMNotesResponse,
        onException: function (request, exception) {
            throw(exception);
        }
    });
}
function handleSaveRMNotesResponse(transport) {
    var json = transport.responseText.evalJSON(true);
    if (json.level == "SUCCESS") {
        shownField = jQuery("#irbInfoRMNotesSpan");
        hiddenField = jQuery("#rmNotesHiddenField");
        if (json.value.length == 0) {
            shownField.html("No notes have been entered by the research manager.");
            shownField.addClass("rm-notes-empty");
        } else {
            shownField.html(nl2br(json.value));
            if (shownField.hasClass("rm-notes-empty")) {
                shownField.removeClass("rm-notes-empty");
            }
        }
        hiddenField.val(json.value.replace(/\"/g, "&quot;"));
        hideEditRMNotesDiv();
        successPopup(json.message);
    } else {
        revertRMNotesChange();
        errorPopup(json.message);
    }
}
function cancelRMNotesChange() {
    hideEditRMNotesDiv();
    revertRMNotesChange();
}
function revertRMNotesChange() {
    prevVal = jQuery("#rmNotesHiddenField").val();
    if (prevVal.length == 0) {
        jQuery("#irbInfoRMNotesSpan").html("No notes have been entered by the research manager.");
        jQuery("#rmNotesField").val("");
    } else {
        jQuery("#irbInfoRMNotesSpan").html(prevVal);
        jQuery("#rmNotesField").val(prevVal);
    }
}

//---------------------------
// IRB File Download
//---------------------------
function downloadIRBFile(irbId, fileId) {

    var newForm = document.createElement('FORM');
    newForm.id   = "download_irb_file_form";
    newForm.name = "download_irb_file_form";
    newForm.form = "text/plain";
    newForm.action = "ProjectIRB?id=" + projectId;
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "projectId";
    newInput.type  = "hidden";
    newInput.value = "projectId";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "downloadFile";
    newInput.type  = "hidden";
    newInput.value = "true";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "irbId";
    newInput.type  = "hidden";
    newInput.value = irbId;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "fileId";
    newInput.type  = "hidden";
    newInput.value = fileId;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

//---------------------------
// IRB Review page
//---------------------------
function irbReviewSelect(event) {
    var subjToStr = jQuery('#reviewSubjToSelect').val();
    var shareabilityStr = jQuery("#reviewShareabilitySelect").val();
    var dataTypeStr = jQuery("#reviewDataTypeSelect").val();
    var datasetsStr = jQuery("#reviewDatasetsSelect").val();
    var publicStr = jQuery("#reviewPublicSelect").val();
    var needsAttnStr = jQuery("#reviewNeedsAttnSelect").val();
    var pcBeforeStr = jQuery("#reviewProjectCreatedSelect").val();
    var pcDateStr = jQuery("#reviewProjectCreatedInput").val();
    var dlaBeforeStr = jQuery("#reviewDsLastAddedSelect").val();
    var dlaDateStr = jQuery("#reviewDsLastAddedInput").val();

    var newForm = document.createElement('FORM');
    newForm.id = "filter_irb_review_form";
    newForm.name = "filter_irb_review_form";
    newForm.form = "text/plain";
    newForm.action = "IRBReview";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "irbReviewAction";
    newInput.type  = "hidden";
    newInput.value = "filter";
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterSubjectTo";
    newInput.type  = "hidden";
    newInput.value = subjToStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterShareability";
    newInput.type  = "hidden";
    newInput.value = shareabilityStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterDataType";
    newInput.type  = "hidden";
    newInput.value = dataTypeStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterDatasets";
    newInput.type  = "hidden";
    newInput.value = datasetsStr;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name  = "filterPublic";
    newInput.type  = "hidden";
    newInput.value = publicStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterNeedsAttn";
    newInput.type  = "hidden";
    newInput.value = needsAttnStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterPcBefore";
    newInput.type  = "hidden";
    newInput.value = pcBeforeStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterPcDate";
    newInput.type  = "hidden";
    newInput.value = pcDateStr;
    newForm.appendChild(newInput);
    
    newInput = document.createElement('input');
    newInput.name  = "filterDlaBefore";
    newInput.type  = "hidden";
    newInput.value = dlaBeforeStr;
    newForm.appendChild(newInput);

    newInput = document.createElement('input');
    newInput.name  = "filterDlaDate";
    newInput.type  = "hidden";
    newInput.value = dlaDateStr;
    newForm.appendChild(newInput);

    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function irbReviewClearPc(event) {
    jQuery("#reviewProjectCreatedInput").val("");
    irbReviewSelect(event);
}
function irbReviewClearDla(event) {
    jQuery("#reviewDsLastAddedInput").val("");
    irbReviewSelect(event);
}

function irbReviewSearchBy(event) {
    var key = "";
    if (this.id == 'irb_review_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'irb_review_search_by')
        | this.id == 'irb_review_search_button';
    if (testEvent) {
        var searchString = jQuery("#irb_review_search_by").val();
        if (searchString == jQuery("#irb_review_search_by").attr("title")) {
            searchString = "";
        }
        jQuery(
               '<form id="searchStringForm" method="post" action="IRBReview">'
               + '<input id="irbReviewAction" name="irbReviewAction"'
               + ' type="hidden" value="search"/> '
               + '<input id="searchBy" name="searchBy"'
               + ' type="hidden" value="' + searchString
               + '"/></form>').appendTo('body').submit();
    }
    return false;
}
function irbReviewSearchByClear(event) {
    jQuery("#irb_review_search_by").val("");
    jQuery(
            '<form id="searchStringForm" method="post" action="IRBReview">'
            + '<input id="irbReviewAction" name="irbReviewAction"'
            + ' type="hidden" value="search"/> '
            + '<input id="searchBy" name="searchBy"'
            + ' type="hidden" value="'
            + '"/></form>').appendTo('body').submit();
}
function irbReviewSearchByPiDp(event) {
    var key = "";
    if (this.id == 'irb_review_search_by_pi_dp') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'irb_review_search_by_pi_dp')
        | this.id == 'irb_review_search_pi_dp_button';
    if (testEvent) {
        var searchString = jQuery("#irb_review_search_by_pi_dp").val();
        if (searchString == jQuery("#irb_review_search_by_pi_dp").attr("title")) {
            searchString = "";
        }
        jQuery(
               '<form id="searchStringForm" method="post" action="IRBReview">'
               + '<input id="irbReviewAction" name="irbReviewAction"'
               + ' type="hidden" value="searchByPiDp"/> '
               + '<input id="searchByPiDp" name="searchByPiDp"'
               + ' type="hidden" value="' + searchString
               + '"/></form>').appendTo('body').submit();
    }
    return false;
}
function irbReviewSearchByPiDpClear(event) {
    jQuery("#irb_review_search_by_pi_dp").val("");
    jQuery(
            '<form id="searchStringForm" method="post" action="IRBReview">'
            + '<input id="irbReviewAction" name="irbReviewAction"'
            + ' type="hidden" value="searchByPiDp"/> '
            + '<input id="searchByPiDp" name="searchByPiDp"'
            + ' type="hidden" value="'
            + '"/></form>').appendTo('body').submit();
}

function sortIRBReview(sortByKey) {

    var newForm = document.createElement('FORM');
    newForm.id = "sort_irb_review_form";
    newForm.name = "sort_irb_review_form";
    newForm.form = "text/plain";
    newForm.action = "IRBReview";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "irbReviewAction";
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

// see IrbReview.js

//---------------------------
// All IRBs page
//---------------------------
function allIRBSearchBy(event) {
    var key = "";
    if (this.id == 'all_irb_search_by') {
        key = event.keyCode || event.which;
    }
    var testEvent = (key === 13 & this.id == 'all_irb_search_by')
        | this.id == 'all_irb_search_button';
    if (testEvent) {
        var searchString = jQuery("#all_irb_search_by").val();
        if (searchString == jQuery("#all_irb_search_by").attr("title")) {
            searchString = "";
        }
        jQuery(
               '<form id="searchStringForm" method="post" action="IRBReview?all">'
               + '<input id="irbReviewAction" name="irbReviewAction"'
               + ' type="hidden" value="search"/> '
               + '<input id="searchBy" name="searchBy"'
               + ' type="hidden" value="' + searchString
               + '"/></form>').appendTo('body').submit();
    }
    return false;
}

function sortAllIRBs(sortByKey) {
    var newForm = document.createElement('FORM');
    newForm.id = "sort_all_irbs_form";
    newForm.name = "sort_all_irbs_form";
    newForm.form = "text/plain";
    newForm.action = "IRBReview?all";
    newForm.method = "post";

    var newInput = document.createElement('input');
    newInput.name  = "irbReviewAction";
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
