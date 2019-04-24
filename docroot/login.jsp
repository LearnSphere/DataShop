<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
    // Author: Ben Billings
// Version: $Revision: 15822 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2019-01-09 16:29:53 -0500 (Wed, 09 Jan 2019) $
// $KeyWordsOff: $
//
%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.util.DataShopInstance"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>

<%@ include file="/header_variables.jspf" %>

<% 
    pageTitle = "Login"; 
    cssIncludes.add("login.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- GitHub login button styling. -->
<link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/github/social-buttons.css")%>" />

<!-- body -->
<tr>
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

    String linkedInClientId = "77sq8ieis6nm9y";
    StringBuffer linkedInOAuthUrl = new StringBuffer("https://www.linkedin.com/oauth/v2/authorization?");
    linkedInOAuthUrl.append("response_type=code");
    linkedInOAuthUrl.append("&client_id=").append(linkedInClientId);
    linkedInOAuthUrl.append("&redirect_uri=").append(datashopUrl).append("/LinkedInLogin");
    linkedInOAuthUrl.append("&state=oiwiwaomwtiwitw2b").append("&scope=r_liteprofile%20r_emailaddress");

 //   if (remoteUser == null) {
%>
    <td id="login-page">
<!-- Begin pasted content -->    
<div id="container-boundingbox" class="wrapper">
      <div id="container" class="wrapper">
        <div class="LoginPage">
            <div class="page-header">
                <h1>Welcome!</h1>
            </div>

<div class="column11 first">
    <div class="page-intro">DataShop is a free service for the learning science community.</div>
      <div class="Tour"><div class="datashop-tour">
    <div class="tour-container">
      <div class="tour1">
          <h4>Upload your own data</h4>
          <p>Log data from an intelligent tutor or upload it for import into DataShop.</p>
      </div>
    </div>
    <div class="tour-container">
      <div class="tour2">
        <h4>Analyze existing data</h4>
        <p>Analysis tools help you explore data visually, while statistical tools help you model knowledge.</p>
      </div>
    </div>
    <div class="tour-container last">
      <div class="tour3">
        <h4>Export easily</h4>
        <p>Export to a tab-delimited format that can be viewed in Excel, R, SPSS, or another analysis program.</p>
      </div>
    </div>
  </div>
</div>
</div>

  <div class="column11">
    <span id="login-or-create-acct"><span> Log in </span> or <span title="Use one of the sign-in options to authenticate and you will be redirected back to DataShop to create an account."> Create an Account </span> with one of these options.</span>
    <div class="LoginForm">

    <div class="callout-box">

    <div class="signin">

      <% if (datashopUrl != null) { %>
             <input name="ds-instance-url"
                    id="ds-instance-url" value="<%=datashopUrl %>" type="hidden" />
      <% } %>

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
           style="padding-top: 10px;padding-right: 55px;padding-left: 50px"></div>

      <!-- GitHub login button. -->
      <div id="githubLogin" style="padding-top: 20px;padding-left: 50px">
           <a href="<%=githubOAuthUrl.toString() %>" class="btn btn-lg btn-social btn-github">
              <i class="fa fa-github"></i> Sign in with GitHub</a>
      </div>

      <!-- LinkedIn login button. -->
      <div id="linkedInLogin" style="padding-top: 20px;padding-left: 50px">
           <a href="<%=linkedInOAuthUrl.toString() %>"><img alt="Sign in with LinkedIn" src="images/LinkedIn-sign-in.png"></a>
      </div>

      <!-- InCommon/Shibboleth Sign-in -->
      <div id="idpSelect" style="padding-top: 20px"></div>

      </div>  <!-- signin -->
      </div>  <!-- callout-box -->

     <!-- If this is a remote (slave) instance, don't offer the local option. -->
     <% if (!DataShopInstance.isSlave()) { %>

      <div style="margin-top: 20px">
<!--      <span id="local-login-option">This local log in option will be removed following the next release.</span> -->
      <div class="callout-box">

        <form id="loginBoxForm" name="loginBoxForm" method="post" action="login">
        <input name="redirect" value="<% if(request.getHeader("Referer") != null) { 
            out.print(request.getHeader("Referer")); } else { out.print("/datashop"); }%>" type="hidden"/>
        <input name="source" value="project-page-external" type="hidden"/>    
        <label for="account">Username</label>
        <input id="account" maxlength="64" name="account" value="<%=accountId%>" class="text" type="text" />
        <label for="password">Password</label>
        <input id="password" maxlength="64" name="password" class="text" type="password" />
        <%=invalidAccountIdMsg%>

        <input name="login" value="Log in" class="super-emphasize" type="button" id="login-submit" /><div style="display: none;"></div>
        </form>

        <a href="ForgotPassword" class="password">Forgot your password?</a>

      </div>  <!-- callout-box -->
      </div>

     <% } %>

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

    <% if (!DataShopInstance.isSlave()) { %>
       <script type="text/javascript" language="JavaScript">
       <!--
       var focusControl = document.forms["loginBoxForm"].elements["account"];
    
        if (focusControl.type != "hidden" && !focusControl.disabled) {
           focusControl.focus();
        }
        // -->
        </script>
    <% } %>

    </td>
</tr>
<%
 //   }
%>

<!-- footer -->
<%@ include file="/footer.jspf" %>
</table>
<!-- Hidden Form for Navigation -->
<form name="nav_helper_form" action="LearningCurve" method="post">
    <input type="hidden" name="curriculum_select" />
</form>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/login.jspf" %>
</div>

<!-- unpopulated div for modal dialog -->
<div id="notifyLocalLoginDialog" />

</body>
</html>
