<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2017
  All Rights Reserved
-->
<%
// Version: $Revision: 14925 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-03-14 11:46:43 -0400 (Wed, 14 Mar 2018) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.TermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
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

    boolean isWebIso = false;
    String accountIdType = "text";
    String showAccountId = "";

    LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
    RegistrationInfo regInfo = (RegistrationInfo) session.getAttribute("registrationInfo");
    
    if (loginInfo != null) {
        accountId = loginInfo.getAccountId();
        if (loginInfo.getFirstName() != null) {
            firstName = loginInfo.getFirstName();
        }
        if (loginInfo.getLastName() != null) {
            lastName = loginInfo.getLastName();
        }
        
        if (loginInfo.getEmail() != null) {
            email = loginInfo.getEmail();
        }

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
        // If accountId is invalid, allow user to change it.
        if (!invalidAccountIdMsg.equals("")) {
            accountIdType = "text";
            showAccountId = "";
        }

        firstName = regInfo.getFirstName();
        invalidFirstNameMsg = regInfo.getInvalidFirstNameMessage();

        lastName = regInfo.getLastName();
        invalidLastNameMsg = regInfo.getInvalidLastNameMessage();

        email = regInfo.getEmail();
        invalidEmailMsg = regInfo.getInvalidEmailMessage();

        institution = regInfo.getInstitution();
    }
    
    // get the terms text
    String termsDate = (String)session.getAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB);
    String termsText = (String)session.getAttribute(TermsServlet.TERMS_TEXT_ATTRIB);
%>
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Registration"; 

    String lsHeaderType = "ls-header-short";
%>
<link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/workflows/ls_login.css")%>">
<!--  header -->
<%@ include file="/ls-header.jspf" %>

<!-- body -->

<tr>
    <td>
        <div id="registration" class="ls-registration">
        <!-- Registration Box -->
        <h1>Create a LearnSphere account</h1>
        
	<form id="registrationForm" name="loginBoxForm" method="post" action="ls_registration">
        <% if (termsText != null && termsText != "") { %>
        <h2>Terms of Use</h2>
        <div id="terms">
            <p><em>Effective <%=termsDate%></em></p>
            <%=termsText%>
        </div>
        <div id="agree_checkbox">
            <input type="checkbox" name="agreeCheckbox"  id="agreeCheckbox" 
                   value="agree"  <%=hasAgreedChecked%>/> 
            <label for="agreeCheckbox">Yes, I agree to these terms (required)</label>
            <% if (invalidTouAgreeMessage != "") { %>
            <p id="regstrationErrorMessage" class="errorMessage">
            <%=invalidTouAgreeMessage%>
            </p>
            <% } %>
        </div>
        <% } %>
        
        <table id="ls-create-new-account">
	<tr><td colspan="2"></td></tr>
            <tr><td ><label for="accountID">Username</label></td>
                   <td>
                   <input type="<%=accountIdType%>" name="newAccountId" id="accountID" size="32" value="<%=accountId%>" title="Please enter a unique username" /><%=showAccountId%>
                   <input type="hidden" name="webIsoFlag" id="webIsoFlag" value="<%=isWebIso%>" />
                   </td>
                    <%=invalidAccountIdMsg%>
            </tr>
            <tr><td ><label for="firstName">First Name</label></td>
                   <td><input type="text" name="firstName" id="firstName" size="16" value="<%=firstName%>" title="Please enter your first name." /></td>
                   <%=invalidFirstNameMsg%>
            </tr>
            <tr><td ><label for="lastName">Last Name</label></td>
                   <td><input type="text" name="lastName" id="lastName" size="16" value="<%=lastName%>" title="Please enter your last name." /></td>
                   <%=invalidLastNameMsg%>
            </tr>
            <tr><td ><label for="email">Email Address</label></td>
                   <td><input type="text" name="email" id="email" size="32" value="<%=email%>" title="Please enter your email address." /></td>
                   <%=invalidEmailMsg%>
            </tr>
            <tr><td ><label for="institution">Institution <em>(optional)</em></label></td>
                   <td><input type="text" name="institution" id="institution" size="32" value="<%=institution%>" title="Please enter your institution." /></td>
            </tr>
            <tr><td></td>
            <td class="submit">
            <input name="Submit" value="Create Account" class="super-emphasize" type="submit" id="submit" />
            </td>
            </tr>
        </table>
	</form>
        </div>
    </td>
</tr> 
    
    </td>
</tr>
</table>

</body>
</html>
