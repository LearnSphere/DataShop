<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 16019 $
// Last modified by: $Author: pls21 $
// Last modified on: $Date: 2019-04-12 15:27:50 -0400 (Fri, 12 Apr 2019) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo"%>
<%
    boolean hasAgreed = false;
    String hasAgreedChecked = "";
    String invalidTouAgreeMessage = "";

    String accountId = "";
    String invalidAccountIdMsg = "";

    String firstName = "";
    String invalidFirstNameMsg = "";

    String lastName = "";
    String invalidLastNameMsg = "";

    String email = "";
    String invalidEmailMsg = "";

    String institution = "";
    String invalidInstitutionMsg = "";

    String invalidPasswordMsg = "";

    boolean isWebIso = false;
    String accountIdType = "text";
    String showAccountId = "";

    LoginInfo loginInfo = (LoginInfo) session.getAttribute("dl_loginInfo");
    RegistrationInfo regInfo = (RegistrationInfo) session.getAttribute("dl_registrationInfo");

    if (loginInfo != null) {
        accountId = loginInfo.getAccountId();

        email = accountId;

        institution = "Carnegie Mellon University";

        isWebIso = loginInfo.isWebIso();
        if (isWebIso) {
            accountIdType = "hidden";
            showAccountId = accountId;
        }
    }

    if (regInfo != null) {

        hasAgreed = regInfo.isTouAgree();
        if (hasAgreed) {
            hasAgreedChecked = "checked";
        }

        invalidTouAgreeMessage = regInfo.getInvalidTouAgreeMessage();

        accountId = regInfo.getAccountId();
        invalidAccountIdMsg = regInfo.getInvalidAccountIdMessage();

        firstName = regInfo.getFirstName();
        invalidFirstNameMsg = regInfo.getInvalidFirstNameMessage();

        lastName = regInfo.getLastName();
        invalidLastNameMsg = regInfo.getInvalidLastNameMessage();

        email = regInfo.getEmail();
        invalidEmailMsg = regInfo.getInvalidEmailMessage();

        institution = regInfo.getInstitution();

        invalidPasswordMsg = regInfo.getInvalidPasswordMessage();
    }
%>

<html>
<head>
    <title>
<%
        out.print("DataLab");
%>
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <link rel="icon" href="images/favicon.ico" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/jquery-ui-1.8.18.custom.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/datalab/datalab.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/login.css")%>" />
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


<%@ include file="/google-analytics.jspf" %>
</head>

<div id="datalab-login-div">
<div id="dl-logo">
  <a href="DataLab"><span>DataLab</span></a>
</div>

<div class="outer-div">

        <div id="dl-registration">
        <!-- Registration Box -->
        <h1>Create an account</h1>

    <form id="registrationForm" name="loginBoxForm" method="post" action="DataLabRegistration">

        <table id="create-new-account">
    <tr><td colspan="2"></td></tr>
            <tr><td class="label"><label for="accountID">Username</label></td>
                   <td>
                   <input type="<%=accountIdType%>" name="newAccountId" id="accountID" size="32" value="<%=accountId%>" title="Please enter a unique username" /><%=showAccountId%>
                   <input type="hidden" name="email" id="email" value="<%=email%>" />
                   <input type="hidden" name="webIsoFlag" id="webIsoFlag" value="<%=isWebIso%>" />
                   </td>
                    <%=invalidAccountIdMsg%>
            </tr>
            <tr><td class="label"><label for="firstName">First Name</label></td>
                   <td><input type="text" name="firstName" id="firstName" size="16" value="<%=firstName%>" title="Please enter your first name." /></td>
                   <%=invalidFirstNameMsg%>
            </tr>
            <tr><td class="label"><label for="lastName">Last Name</label></td>
                   <td><input type="text" name="lastName" id="lastName" size="16" value="<%=lastName%>" title="Please enter your last name." /></td>
                   <%=invalidLastNameMsg%>
            </tr>
            <tr><td class="label"><label for="email">Email</label></td>
                   <td><input type="text" name="email" id="email" size="32" value="<%=email%>" title="Please enter your email address." /></td>
                   <%=invalidEmailMsg%>
            </tr>
            <tr><td class="label"><label for="institution">Institution</label></td>
                   <td><input type="text" name="institution" id="institution" size="32" value="<%=institution%>" title="Please enter your institution." /></td>
            </tr>
            <%  if (!isWebIso) {  %>
            <tr><td class="label"><label for="password1">Password</label></td>
                   <td><input type="password" name="password1" id="password1" size="16" value="" title="Please enter a secure password." /></td></tr>
            <tr><td class="label"><label for="password2">Confirm Password</label></td>
                   <td><input type="password" name="password2" id="password2" size="16" value="" title="Please re-type your password to confirm." /></td>
                   <%=invalidPasswordMsg%>
            </tr>
            <% } %>
            <tr><td></td>
            <td class="submit">
            <input name="Submit" value="Create Account" class="super-emphasize" type="submit" id="submit" />
            </td>
            </tr>
        </table>
    </form>
        </div>   <!-- registration -->

</div>   <!-- outer-div -->

</html>
