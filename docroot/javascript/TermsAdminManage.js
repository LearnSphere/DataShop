//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2011
// All Rights Reserved
//
// Author: Mike Komisin
// Version: $Revision: 7368 $
// Last modified by: $Author: mkomi $
// Last modified on: $Date: 2011-12-01 11:36:38 -0500 (Thu, 01 Dec 2011) $
// $KeyWordsOff: $
//

Event.observe(window, 'load',
    function(){
        Event.observe($("selectTou"), 'change', addSelectHandler);
        Event.observe($("create_name_text"), 'keyup', addNullTextHandler);
        Event.observe($("delete_terms_button"), 'click', deleteConfirmation);
        Event.observe($("retire_terms_button"), 'click', retireConfirmation);
    }
);

String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
}

function addSelectHandler() {
    var mySelect = $("selectTou");
    var editButton = $("edit_terms_button");
    var deleteButton = $("delete_terms_button");
    var retireButton = $("retire_terms_button");
    
    <!-- Edit button  -->
    if (mySelect.value == '') {
        editButton.disabled = true;
    }
    else {
        editButton.disabled = false;
    }
    for (var i=0; i < mySelect.options.length; i++){
        if (mySelect.options[i].selected == true){

            var str = mySelect.options[i].text;

            var savedString = "saved";
            var lastIndexOfSaved = str.lastIndexOf(savedString);

            var appliedString = "applied";
            var lastIndexOfApplied = str.lastIndexOf(appliedString);

            var archivedString = "archived";
            var lastIndexOfArchived = str.lastIndexOf(archivedString);

            <!--  if the ToU is marked applied -->
            if (lastIndexOfApplied == str.length - appliedString.length) {
                retireButton.disabled = true;
                deleteButton.disabled = true;
            }
            <!--  else if the ToU is marked archived (i.e., no applied versions) -->
            else if (lastIndexOfArchived == str.length - archivedString.length) {
                deleteButton.disabled = true;
                retireButton.disabled = false;
            }
            <!--  else if the ToU is marked saved (i.e., no applied or archived versions) -->
            else if (lastIndexOfSaved == str.length - savedString.length) {
                deleteButton.disabled = false;
                retireButton.disabled = false;
            }
            else {
                deleteButton.disabled = true;
                retireButton.disabled = true;
            }
            <!--  break out of options loop -->
            break;
        }
    }
}

<!-- Function that disables the create new project terms of use button if no name exists in the textbox -->

<!-- Ties to the textbox's onkeyup events -->
function addNullTextHandler(e) {
    myTextarea = $("create_name_text");
    var myButton = $("create_terms_button");
    if (myTextarea.value.trim() == '') {
        myButton.disabled = true;
    }
    else {
        myButton.disabled = false;
    }
}

function deleteConfirmation() {
    var confirmation =
        '<div id="save-and-publish-div" style="font-family: georgia; font-size: 13;">'
        + 'Deleting the terms of use cannot be undone.<br>'
        + 'Are you really sure you want to delete them?<br><br>'
        + '<a href="javascript:deleteTerms()">Yes, do it</a> | <a href="javascript:cancelConfirmation()">No, get me out of here</a>'
        + '</div>';
    $("save-and-publish-div").replace(confirmation);
}

function deleteTerms() {
    var myForm = document.forms['edit_project_terms_form'];

    var newInput = document.createElement('input');
        newInput.name="delete_terms_button";
        newInput.type="hidden";
        myForm.appendChild(newInput);
    document.forms['edit_project_terms_form'].submit();
}

function retireConfirmation() {
    var confirmation =
        '<div id="save-and-publish-div" style="font-family: georgia; font-size: 13;">'
        + 'Retiring the terms of use cannot be undone.<br>'
        + 'Are you really sure you want to retire them?<br><br>'
        + '<a href="javascript:retireTerms()">Yes, do it</a> | <a href="javascript:cancelConfirmation()">No, get me out of here</a>'
        + '</div>';
    $("save-and-publish-div").replace(confirmation);
}

function cancelConfirmation() {
    var confirmation = '<div id="save-and-publish-div">'
        + '</div>';
    $("save-and-publish-div").replace(confirmation);
}

function retireTerms() {
    var myForm = document.forms['edit_project_terms_form'];

    var newInput = document.createElement('input');
        newInput.name="retire_terms_button";
        newInput.type="hidden";
        myForm.appendChild(newInput);
    document.forms['edit_project_terms_form'].submit();
}