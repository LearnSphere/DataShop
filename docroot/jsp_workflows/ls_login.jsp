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
<%@page import="edu.cmu.pslc.datashop.servlet.auth.GitHubLoginServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.DataShopInstance"%>

<html>
<head>
    <title>
<%
        out.print("LearnSphere");
%>
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <meta name="google-signin-client_id" content="757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com"/>
    <link rel="icon" href="images/learnsphere/favicon.ico" />
    <!-- GOOGLE FONTS -->
    <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Slabo+27px" rel="stylesheet">
    <!-- LS MAIN Style -->
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/workflows/main.css")%>">
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/jquery-ui-3.3.1.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/workflows/ls_login.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/idpselect.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/print.css")%>" media="print" />
    <!--[if IE 8]>
        <link rel="stylesheet" href="css/ie8fix.css" type="text/css" />
    <![endif]-->
    <!--[if IE 9]>
        <link rel="stylesheet" href="css/ie9fix.css" type="text/css" />
    <![endif]-->

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-3.3.1.min.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-ui-3.3.1.min.js")%>"></script>

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/prototype.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/scriptaculous.js?load=effects,dragdrop,controls")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/hint.js")%>"></script>

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

    LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");

    if (loginInfo != null) {
        accountId = loginInfo.getAccountId();
        invalidAccountIdMsg = loginInfo.getLoginFailedMessage();
    }

    String datashopUrl = DataShopInstance.getDatashopUrl();
    String ghClientId = DataShopInstance.getGithubClientId();
    // If not specified, default to localhost.
    if (ghClientId == null) { ghClientId = "22cdd8a6fc50622cf71c"; }
    StringBuffer githubOAuthUrl = new StringBuffer("https://github.com/login/oauth/authorize?");
    githubOAuthUrl.append("client_id=").append(ghClientId);
    githubOAuthUrl.append("&redirect_uri=").append(datashopUrl).append("/GitHubLogin");
    githubOAuthUrl.append("&scope=").append("user:email");

    // Sign. For the GitHubLogin redirect to work...
    session.setAttribute(GitHubLoginServlet.IS_TIGRIS_ATTR, "true");

    String linkedInClientId = "77sq8ieis6nm9y";
    StringBuffer linkedInOAuthUrl = new StringBuffer("https://www.linkedin.com/oauth/v2/authorization?");
    linkedInOAuthUrl.append("response_type=code");
    linkedInOAuthUrl.append("&client_id=").append(linkedInClientId);
    linkedInOAuthUrl.append("&redirect_uri=").append(datashopUrl).append("/LinkedInLogin");
    linkedInOAuthUrl.append("&state=oiwiwaomwtiwitw2b").append("&scope=r_liteprofile%20r_emailaddress");

    StringBuffer locationStr = new StringBuffer();
    if (DataShopInstance.isSlave()) {
        locationStr.append(DataShopInstance.getSlaveId());
    } else {
        locationStr.append("CMU");
    }
%>

<!--HEADER-->
<div class="header">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
              <span class="sr-only">Toggle navigation</span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </button>
            <h3><a class="navbar-brand" href="http://learnsphere.org/"><span class="logo"></span>LearnSphere@<%=locationStr.toString() %></a></h3>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Tigris</a></li>
                <li><a href="http://learnsphere.org/explore.html">Explore</a></li>
                <li><a href="http://learnsphere.org/leadership.html">Leadership</a></li>
                <li><a href="http://learnsphere.org/about.html">About</a></li>
                <li><a href="http://learnsphere.org/help.html">Help</a></li>
            </ul>
        </div><!--/.nav-collapse -->
    </div>
</div>

<div id="wf-login-div">

<div class="outer-div">

<div id="container-boundingbox" class="wrapper">
  <div id="container" class="wrapper">
      <div class="LoginPage">

        <div class="column11">
          <div class="LoginForm">
            <div class="callout-box">

            <!-- <div class="reg-header">
              <h3>Log in<span class="switch"></span>
              </h3>
            </div> -->

            <div class="signin">

              <% if (datashopUrl != null) { %>
                     <input name="ds-instance-url"
                            id="ds-instance-url" value="<%=datashopUrl %>" type="hidden" />
              <% } %>

              <input name="is-workflows" id="is-workflows" value="true" type="hidden" />

              <!-- Google Sign-in -->
              <div class="g-signin2 loginBox"
                   data-scope="profile"
                   data-clientid="757918974099-bfjl74l0a27t68mfk9v2pbu6rr3fh8qe.apps.googleusercontent.com"
                   data-redirecturi="postmessage"
                   data-accesstype="offline"
                   data-onsuccess="signInCallback"
                   data-longtitle="true"
                   data-width="wide"
                   data-theme="dark"
                   style="padding-top: 21px"></div>

              <!-- GitHub login button. -->
              <div id="githubLogin" class="loginBox" style="padding-top: 20px">
                   <a href="<%=githubOAuthUrl.toString() %>" class="btn btn-lg btn-social btn-github">
                      <i class="fa fa-github"></i> Sign in with GitHub</a>
              </div>

              <!-- LinkedIn login button. -->
              <div id="linkedInLogin" class="loginBox" style="padding-top: 20px">
                   <a href="<%=linkedInOAuthUrl.toString() %>"><img alt="Sign in with LinkedIn" src="images/LinkedIn-sign-in.png"></a>
              </div>

              <!-- InCommon/Shibboleth Sign-in -->
              <div id="idpSelect"></div>

              <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_config.js")%>" type="text/javascript" language="javascript"></script>
              <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_languages.js")%>" type="text/javascript" language="javascript"></script>
              <script src="<%=response.encodeURL("/v/javascript/idp/typeahead.js")%>" type="text/javascript" language="javascript"></script>
              <script src="<%=response.encodeURL("/v/javascript/idp/idpselect.js")%>" type="text/javascript" language="javascript"></script>

              <script src="<%=response.encodeURL("/v/javascript/idp/idpselect_remote.js")%>" type="text/javascript" language="javascript" defer></script>

              </div>

            </div>
          </div>
        </div>

      </div>
  </div>
</div>

</div>   <!-- outer-div -->
</div>    <!-- wf-login-div -->
</html>
