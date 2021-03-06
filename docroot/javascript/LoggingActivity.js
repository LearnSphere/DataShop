//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2009
// All Rights Reserved
//
// Author: Alida Skogsholm
// Version: $Revision: 10541 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-02-11 14:11:28 -0500 (Tue, 11 Feb 2014) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// Maximum character length for the reason textarea
var MaxReasonLen = 500;

//
// Binds an event listenter to the option menu.
//
function initMinuteOptionMenu() {
    var theSelect = document.getElementById("logging_activity_num_minutes");
    if (theSelect != null) {
        theSelect.onchange = minutesUpdated.bindAsEventListener(theSelect);
    }
}

//
// Handles actions from the option menu.
//
function minutesUpdated(theElement) {
    var myForm = document.createElement('FORM');
    myForm.setAttribute('name',   'logging_activity_minute_form');
    myForm.setAttribute('id',     'logging_activity_minute_form');
    myForm.setAttribute('form',   'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var numMinutesInput   = document.createElement('input');
    numMinutesInput.name  = "logging_activity_num_minutes";
    numMinutesInput.type  = "hidden";
    numMinutesInput.value = this.value;

    myForm.appendChild(numMinutesInput);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
}


//
// Binds an event listenter to the request logging activity role button.
//
function initRequestLogggingActivityRoleButton() {
    var theElement = document.getElementById("log_activity_request_role_button");
    if (theElement != null) {
        theElement.onclick = requestLoggingActivityRole.bindAsEventListener(theElement);
    }
}

//
// Handles actions from the request logging activity role button.
//
function requestLoggingActivityRole(theElement) {

    var dialogDiv = document.getElementById('requestLoggingRoleDialog');
    dialogDiv.innerHTML = "";

    var p = document.createElement('p');
    p.innerHTML = "<b>Please tell us about your intended use of DataShop.</b> " +
        "We'd be happy to give you access to the Logging Activity Report, " +
        "but we want to verify that your intended use is research-related.";

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

    jQuery("textarea#requestReason").keyup(reasonModify);

    var dialogTitle = "Access Logging Activity Report";

    jQuery('#requestLoggingRoleDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 575,
        height : 400,
        title : dialogTitle,
        buttons : [ {
            id : "request-logging-role-agree-button",
            text : "Send",
            click : requestRole
        }, {
            id : "request-logging-role-cancel-button",
            text : "Cancel",
            click : closeLoggingRequestRoleDialog
        } ]
    });
    
    // Initially, button is diabled.
    jQuery("#request-logging-role-agree-button").button('disable');

    jQuery('#requestLoggingRoleDialog').dialog('open');
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
        jQuery("#request-logging-role-agree-button").button('enable');
    } else {
        jQuery("#request-logging-role-agree-button").button('disable');
    }
}

// Submit the form that will send the request for the tool role.
function requestRole() {

    closeLoggingRequestRoleDialog();

    var theReason = jQuery("textarea#requestReason").val();

    var myForm = document.createElement('FORM');
    myForm.setAttribute('name',   'logging_activity_request_form');
    myForm.setAttribute('id',     'logging_activity_request_form');
    myForm.setAttribute('form',   'text/plain');
    myForm.setAttribute('action', window.location.href);
    myForm.setAttribute('method', 'POST');

    var input   = document.createElement('input');
    input.name  = "logging_activity_action";
    input.type  = "hidden";
    input.value = "request";
    myForm.appendChild(input);

    var input2   = document.createElement('input');
    input2.name  = "request_reason";
    input2.type  = "hidden";
    input2.value = theReason;
    myForm.appendChild(input2);

    document.getElementsByTagName('body').item(0).appendChild(myForm);
    myForm.submit();
}

function closeLoggingRequestRoleDialog() {
    jQuery('#requestLoggingRoleDialog').dialog('close');
}

onloadObserver.addListener(initMinuteOptionMenu);
onloadObserver.addListener(initRequestLogggingActivityRoleButton);
