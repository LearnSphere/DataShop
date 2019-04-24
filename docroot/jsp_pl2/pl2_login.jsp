<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2016
  All Rights Reserved
-->
<%
// Version: $Revision: 15822 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2019-01-09 16:29:53 -0500 (Wed, 09 Jan 2019) $
// $KeyWordsOff: $
//
%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>


<html>
<head>
    <title>
<%
        out.print("PL2");
%>
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <meta name="google-signin-client_id" content="757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com"/>
    <link rel="icon" href="images/learnsphere/favicon.ico" />
    <!-- GOOGLE FONTS -->
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Slabo+27px" rel="stylesheet">

    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/jquery-ui-3.3.1.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/pl2/pl2.css")%>" />

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-3.3.1.min.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-ui-3.3.1.min.js")%>"></script>

    <!-- for Google sign-in -->
    <script src="https://apis.google.com/js/platform.js?onload=gapiOnload" async defer></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/Authentication.js")%>"></script>

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/pl2/pl2Helper.js")%>"></script>

<%@ include file="/google-analytics.jspf" %>
</head>

<%
    String accountId = "";
    String invalidAccountIdMsg = "";

    LoginInfo loginInfo = (LoginInfo) request.getAttribute("pl2_loginInfo");

    if (loginInfo != null) {
        //accountId = loginInfo.getAccountId();
        //invalidAccountIdMsg = loginInfo.getLoginFailedMessage();
    }


%>
<body>

<!--HEADER-->
<div class="header">
    <h3 class="navbar-brand">
        Welcome to the Personalized Learning<span class="exponent">2</span> App
    </h3>
</div>

<div class="login_container">
    <h3 style="text-align: center;">Please login using your Google account:</h3>
    <div class="login_type">
        <!-- Google Sign-in -->
        <div class="g-signin2"
             data-scope="profile"
             data-clientid="757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com"
             data-redirecturi="postmessage"
             data-accesstype="offline"
             data-onsuccess="signInCallback"
             data-longtitle="true"
             data-width="wide"
             data-theme="dark"
             style="padding-top: 10px;padding-right: 55px;padding-left: 50px">
         </div>

      </div>
</div>



<script>
jQuery(document).ready(function() {
    // body has a font-size in terms of vw.  Make sure it has a minimum value
    jQuery(window).resize(function() {
        ensureTextIsLargeEnough();
    });
    ensureTextIsLargeEnough();
});

function ensureTextIsLargeEnough() {
    jQuery('body').css({ "font-size": "1.2vw" })
    if (parseInt(jQuery('body').css("fontSize")) < 16) {
        jQuery('body').css({ "font-size": "16px" });
    }
}


</script>

</body>
</html>
