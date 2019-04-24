<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
    // Author: Ben Billings
// Version: $Revision: 15073 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-04-19 14:47:30 -0400 (Thu, 19 Apr 2018) $
// $KeyWordsOff: $
//
%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.util.DataShopInstance"%>

<html>
<head>
    <title>
<%
        out.print("DataLab");
%>
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <meta name="google-signin-client_id" content="757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com"/>
    <link rel="icon" href="images/favicon.ico" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/jquery-ui-1.8.18.custom.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/datalab/datalab.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/login.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/idpselect.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/print.css")%>" media="print" />
    <!--[if IE 8]>
        <link rel="stylesheet" href="css/ie8fix.css" type="text/css" />
    <![endif]-->
    <!--[if IE 9]>
        <link rel="stylesheet" href="css/ie9fix.css" type="text/css" />
    <![endif]-->

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-1.7.1.min.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-ui-1.8.17.custom.min.js")%>"></script>

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/prototype.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/scriptaculous.js?load=effects,dragdrop,controls")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/hint.js")%>"></script>

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/datalab/DataLab.js")%>"></script>

    <!-- for Google sign-in -->
    <script src="https://apis.google.com/js/platform.js?onload=gapiOnload" async defer></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/Authentication.js")%>"></script>

    <!-- GitHub login button styling. -->
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/github/social-buttons.css")%>" />

<%@ include file="/google-analytics.jspf" %>
</head>

<%
    String accountId = "";
    String invalidAccountIdMsg = "";

    LoginInfo loginInfo = (LoginInfo) session.getAttribute("dl_loginInfo");

    if (loginInfo != null) {
        accountId = loginInfo.getAccountId();
        invalidAccountIdMsg = loginInfo.getLoginFailedMessage();
    }

    String datashopUrl = DataShopInstance.getDatashopUrl();
    StringBuffer githubOAuthUrl = new StringBuffer("https://github.com/login/oauth/authorize?");
    githubOAuthUrl.append("client_id=22cdd8a6fc50622cf71c");
    githubOAuthUrl.append("&redirect_uri=").append(datashopUrl).append("/login");
    githubOAuthUrl.append("&scope=").append("user:email");
%>

<div id="datalab-login-div">
<div id="dl-logo">
  <a href="DataLab"><span>DataLab</span></a>
</div>

<div class="outer-div">

<div id="container-boundingbox" class="wrapper">
      <div id="container" class="wrapper">
        <div class="LoginPage">
            <div class="page-header">
                <h1>Welcome Back</h1>
            </div>

  <div class="column11">
    <div class="LoginForm"><div class="callout-box">
    <div class="reg-header">
      <h3>Log in<span class="switch"></span>
      </h3>
    </div>
    <div class="signin">
    <form id="loginBoxForm" name="loginBoxForm" method="post" action="DataLabLogin">
    <input name="redirect" value="<% if(request.getHeader("Referer") != null) {
            out.print(request.getHeader("Referer")); } else { out.print("/datashop"); }%>" type="hidden"/>
    <input name="source" value="project-page-external" type="hidden"/>
    <label for="account">Username</label>
    <input id="account" maxlength="64" name="account" value="<%=accountId%>" class="text" type="text" />
    <label for="password">Password</label>
    <input id="password" maxlength="64" name="password" class="text" type="password" />
    <%=invalidAccountIdMsg%>
  <!--
    <div class="checkbox-container">
    <input id="rememberMe" name="rememberMe" value="true" class="checkbox"
           type="checkbox" checked="checked" /><label class="checkbox-text">Remember me</label>
  </div>
  -->
  <input name="login" value="Log in" class="super-emphasize" type="submit" id="submit" /><div style="display: none;"></div>
  </form>
  <a href="ForgotPassword" class="password">Forgot your password?</a>

      <% if (datashopUrl != null) { %>
             <input name="ds-instance-url"
                    id="ds-instance-url" value="<%=datashopUrl %>" type="hidden" />
      <% } %>

      <input name="is-datalab" id="is-datalab" value="true" type="hidden" />

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
           style="padding-top: 10px"></div>

      <!-- GitHub login button. -->
<!-- Issues to work out with this. Don't include for now.
      <div id="githubLogin" style="padding-top: 20px">
           <a href="<%=githubOAuthUrl.toString() %>" class="btn btn-lg btn-social btn-github">
              <i class="fa fa-github"></i> Sign in with GitHub</a>
      </div>
-->

      <!-- InCommon/Shibboleth Sign-in -->
      <div id="idpSelect" style="padding-top: 10px"></div>

      <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_config.js")%>" type="text/javascript" language="javascript"></script>
      <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_languages.js")%>" type="text/javascript" language="javascript"></script>
      <script src="<%=response.encodeURL("/v/javascript/idp/typeahead.js")%>" type="text/javascript" language="javascript"></script>
      <script src="<%=response.encodeURL("/v/javascript/idp/idpselect.js")%>" type="text/javascript" language="javascript"></script>

      <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_remote.js")%>" type="text/javascript" language="javascript" defer></script>

      </div>
    </div>
</div></div>

</div>
</div>
    </div>

    <div class="shadow wrapper"></div>
<!-- End pasted content -->

    <script type="text/javascript" language="JavaScript">
    <!--
    var focusControl = document.forms["loginBoxForm"].elements["account"];

    if (focusControl.type != "hidden" && !focusControl.disabled) {
       focusControl.focus();
    }
    // -->
    </script>

</div>   <!-- outer-div -->
</div>    <!-- datalab-login-div -->
</html>
