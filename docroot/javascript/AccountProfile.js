//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2012
// All Rights Reserved
//

function clickEditInfoButton() {
    var newForm = document.createElement('form');
    newForm.setAttribute('name', 'edit_account_profile_form');
    newForm.setAttribute('id', 'edit_account_profile_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', 'AccountProfile');
    newForm.setAttribute('method', 'POST');
    
    var newInput = document.createElement('input');
    newInput.name="edit_mode";
    newInput.type="hidden";
    newInput.value="true";
    newForm.appendChild(newInput);
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function clickChangePasswordButton() {
    var newForm = document.createElement('form');
    newForm.setAttribute('name', 'go_to_change_password_form');
    newForm.setAttribute('id', 'go_to_change_password_form');
    newForm.setAttribute('form', 'text/plain');
    newForm.setAttribute('action', 'ChangePassword');
    newForm.setAttribute('method', 'POST');
    
    document.getElementsByTagName('body').item(0).appendChild(newForm);
    newForm.submit();
}

function clickSaveEditButton() {
    var theForm = $('edit_profile_form');
    theForm.submit();
}

function clickSavePasswordButton() {
    var theForm = $('change_password_form');
    theForm.submit();
}

function updateHeader(updateAliasAlert) {

    var theSpan = $('aliasAlert');
    var img = $('aliasAlertImg');
    if (updateAliasAlert && (img == null)) {
        img = document.createElement('img');
        img.id = "aliasAlertImg";
        img.src = "images/exclamation.png";
        img.title = "Please update your profile.";
        theSpan.appendChild(img);
    } else if (!updateAliasAlert && (img != null)) {
        theSpan.removeChild(img);
    }
}
