//
// Carnegie Mellon University, Human Computer Interaction Institute
// Copyright 2015
// All Rights Reserved
//
// Author: Cindy Tipper
// Version: $Revision: 15996 $
// Last modified by: $Author: pls21 $
// Last modified on: $Date: 2019-04-04 11:53:00 -0400 (Thu, 04 Apr 2019) $
// $KeyWordsOff: $
//

// Tell jQuery to yield to prototype;
// i.e., cannot use jQuery's short notation, $(..), since prototype is using it
jQuery.noConflict();

// jQuery's Onload function
jQuery(document).ready(function() {

        jQuery('input#login-submit').click(notifyLocalLogins);
        jQuery('a#datashopLogoLink').focus();
        jQuery('a#datashopLogoLink').blur();
});

function gapiOnload() {

    if (gapi.auth2 == undefined) {
        gapi.load('auth2', function() {
            gapi.auth2.init({
        fetch_basic_profile: false,
        client_id: '757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com',
        scope: 'profile'}).then(function () {
            auth2 = gapi.auth2.getAuthInstance();
        });
        });
    }
}

function signInCallback(authResult) {
    var url = window.location.href;

    var ssoServlet = "login";
    if (url.indexOf('DataLab') > 0) {
        ssoServlet = "DataLabLogin";
    } else if (url.indexOf('LearnSphere') > 0) {
        ssoServlet = "LearnSphereLogin";
    } else if (url.indexOf('PL2') > 0 || url.indexOf('PL2Login') > 0) {
        ssoServlet = "PL2Login";
    }

    if (authResult['code']) {

        // Hide the sign-in button now that the user is authorized, for example:
        jQuery('button#signinButton').attr('style', 'display: none');

        jQuery(
               '<form id="googleSignInForm" method="post" action="' + ssoServlet + '">'
               + '<input id="googleAuthCode" name="googleAuthCode" type="hidden" '
               + 'value="' + authResult['code']
               + '"/></form>').appendTo('body').submit();
    }
}

function logout() {
    var url = window.location.href;

    var logoutServlet = "logout";
    if (url.indexOf('DataLab') > 0) {
        logoutServlet = "DataLabLogout";
    } else if (url.indexOf('LearnSphere') > 0) {
        logoutServlet = "LearnSphereLogout";
    }

    gapi.load('auth2', function() {
        gapi.auth2.init({
        fetch_basic_profile: false,
        client_id: '757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com',
        scope: 'profile'}).then(function () {
        auth2 = gapi.auth2.getAuthInstance();
        });
    });

    // If user is logged into Google, sign them out.
    if (gapi.auth2) {
        var auth2 = gapi.auth2.getAuthInstance();
        if (auth2.isSignedIn.get()) {
            auth2.signOut();
        }
    }

    jQuery('<form id="logoutForm" method="post" action="' + logoutServlet + '"></form>').appendTo('body').submit();
}

//----------------------------------------
//  Tap into local login attempts...
//----------------------------------------
function notifyLocalLogins() {

    var dialogDiv = jQuery('#notifyLocalLoginDialog');
    dialogDiv.html("");

    var dsHelpLink = "<a href=\"mailto:datashop-help@lists.andrew.cmu.edu\">DataShop Help</a>";

    jQuery('<p>The local DataShop login option is being phased out.</p>').appendTo(dialogDiv);;
    jQuery('<br>').appendTo(dialogDiv);

    var notifyTxt = "Please use the Google or InCommon login option to create a new "
        + "account. Once you've done this, send an email to "
        + dsHelpLink + ", and "
        + "we will convert all existing projects, datasets and permissions "
        + "from your current account to your new account.";

    var p = jQuery('<p>' + notifyTxt + '</p>').appendTo(dialogDiv);;

    var dialogTitle = "Notice";

    jQuery('#notifyLocalLoginDialog').dialog({
        modal : true,
        autoOpen : false,
        resizable : false,
        width : 500,
        height : 300,
        title : dialogTitle,
        buttons : [ {
            id : "notify-local-login-ok-button",
            text : "OK",
            click : closeLocalLoginDialog
        } ]
    });

    jQuery('#notifyLocalLoginDialog').dialog('open');

}

function closeLocalLoginDialog() {
    jQuery('#notifyLocalLoginDialog').dialog('close');

    jQuery('#loginBoxForm').submit();
}
