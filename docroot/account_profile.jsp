<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14806 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-02-08 15:28:49 -0500 (Thu, 08 Feb 2018) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.AccountProfileServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%
    String accountId = "";

    String firstName = "";
    String invalidFirstNameMsg = "";

    String lastName = "";
    String invalidLastNameMsg = "";

    String email = "";
    String invalidEmailMsg = "";

    String institution = "";
    String invalidInstitutionMsg = "";

    String userAlias = "";

    boolean isWebIso = false;

    boolean editMode =
        (Boolean)session.getAttribute(AccountProfileServlet.ACCT_PROF_EDIT_MODE);
    String successMsg =
        (String)session.getAttribute(AccountProfileServlet.ACCT_PROF_SUCCESS_MSG);
    String errorMsg = (String)request.getAttribute(AccountProfileServlet.ACCT_PROF_ERROR_MSG);
    RegistrationInfo regInfo =
        (RegistrationInfo)session.getAttribute(AccountProfileServlet.ACCT_PROF_REG_INFO);

    boolean successFlag = false;
    String completeSuccessMessage = "";
    if (successMsg != null) {
       if (successMsg.equals(AccountProfileServlet.PROFILE)) {
           completeSuccessMessage = "You have successfully modified your profile.";
           successFlag = true;
       } else if (successMsg.equals(AccountProfileServlet.PASSWORD)) {
           completeSuccessMessage = "You have successfully changed your password.";
           successFlag = true;
       }
    }

    boolean userIdEqualsEmail = false;
    String addAliasAlertStr =
           "* You can specify an alias if you do not want your email address used "
           + "when the system needs to display your id to other users.";

    if (regInfo != null) {

        accountId = regInfo.getAccountId();

        isWebIso = regInfo.isWebIso();

        firstName = regInfo.getFirstName();
        if (firstName == null) {
            firstName = "";
        }
        invalidFirstNameMsg = regInfo.getInvalidFirstNameMessage();

        lastName = regInfo.getLastName();
        if (lastName == null) {
            lastName = "";
        }
        invalidLastNameMsg = regInfo.getInvalidLastNameMessage();

        email = regInfo.getEmail();
        if (email == null) {
            email = "";
        }
        invalidEmailMsg = regInfo.getInvalidEmailMessage();

        institution = regInfo.getInstitution();
        if (institution == null) {
            institution = "";
        }

        userAlias = regInfo.getUserAlias();
        if (userAlias == null) {
           userAlias = "";
        }

        if (accountId.equalsIgnoreCase(email)) {
           userIdEqualsEmail = true;
        }
    }
%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Account Profile";
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
    cssIncludes.add("styles.css");
    jsIncludes.add("javascript/AccountProfile.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="contents">
<div id="account_profile_div" class="account_profile_div_style">
    <% if (editMode) { %>
        <h1>Edit Profile</h1>
        <form id="edit_profile_form" name="edit_profile_form" method="post" action="AccountProfile">
        <table id="account_profile_table" class="account_profile_table_style">
            <tr><td colspan="3"></td></tr>
            <tr><td class="label"><label for="accountID">Username</label></td>
                   <td>
                   <%=accountId%>
                   <input type="hidden" name="edit_profile_flag" value="true" />
                   </td>
            </tr>
            <tr><td class="label"><label for="firstName">First Name</label></td>
                   <td>
                   <input type="text" name="firstName" id="firstName" size="16"
                          value="<%=firstName%>" title="Please enter your first name." /></td>
                   <%=invalidFirstNameMsg%>
            </tr>
            <tr><td class="label"><label for="lastName">Last Name</label></td>
                   <td>
                   <input type="text" name="lastName" id="lastName" size="16"
                          value="<%=lastName%>" title="Please enter your last name." /></td>
                   <%=invalidLastNameMsg%>
            </tr>
            <tr><td class="label"><label for="email">Email Address</label></td>
                   <td>
                   <input type="text" name="email" id="email" size="32"
                          value="<%=email%>" title="Please enter your email address." /></td>
                   <%=invalidEmailMsg%>
            </tr>
            <tr><td class="label"><label for="institution">Institution <em>(optional)</em></label></td>
                   <td>
                   <input type="text" name="institution" id="institution" size="32"
                          value="<%=institution%>" title="Please enter your institution." /></td>
            </tr>
            <tr><td class="label"><label for="userAlias">Alias* <em>(optional)</em></label></td>
                    <td>
                    <input type="text" name="userAlias" id="userAlias" size="32"
                           value="<%=userAlias %>" title="Please enter a user alias, or handle." /></td>
            </tr>

            <tr><td></td>
            <td class="submit">
            <a href="javascript:clickSaveEditButton();" name="Submit" id="submit">Save</a>
            <script language="javascript" type="text/javascript">
                document.write('<a href="' + location.href + '" id="cancel_edit_profile">Cancel</a>');
            </script>
            </td>
            </tr>

        </table>
        </form>

        <% if (userIdEqualsEmail) { %>
           <div id="addAliasAlertSpan" class="information-box">
                <p><%=addAliasAlertStr %></p>
           </div>
        <% } %>

    <% } else { %>
        <h1>View Profile</h1>
        <% if (successFlag) { %>
        <div id="account_profile_success_div" class="success_style" style="display:block">
        <p>
        <strong>Success!</strong>
        <%=completeSuccessMessage%>
        </p>
        </div>
        <% } else if (errorMsg != null) { %>
            <p class="error_style"><%=errorMsg%></p>
        <% } %>
        <table id="account_profile_table" class="account_profile_table_style">
            <tr><td colspan="3"></td></tr>
            <tr><td class="label"><label for="accountID">Username</label></td>
                   <td class="value"><%=accountId%></td>
                   <td>
                   <a href="javascript:clickEditInfoButton();"
                      name="Submit" id="editInfoButton">Edit Info</a>
                   </td>
            </tr>
            <tr><td class="label"><label for="firstName">First Name</label></td>
                   <td class="value"><%=firstName%></td>
            </tr>
            <tr><td class="label"><label for="lastName">Last Name</label></td>
                   <td class="value"><%=lastName%></td>
            </tr>
            <tr><td class="label"><label for="email">Email Address</label></td>
                   <td class="value"><%=email%></td>
            </tr>
            <tr><td class="label"><label for="institution">Institution <em>(optional)</em></label></td>
                   <td class="value"><%=institution%></td>
            </tr>
            <tr><td class="label"><label for="userAlias">Alias* <em>(optional)</em></label></td>
                <td class="value"><%=userAlias %></td>
            </tr>

            <%  if (!isWebIso) {  %>
            <tr><td class="label"><label for="password">Password</label></td>
                   <td>******</td>
                   <td>
                   <a href="javascript:clickChangePasswordButton();"
                      name="Submit" id="changePasswordButton">Change Password</a>
                   </td>
            </tr>
            <% } %>
        </table>

        <% if (userIdEqualsEmail) { %>
           <div id="addAliasAlertSpan" class="information-box">
                <p><%=addAliasAlertStr %></p>
           </div>
        <% } %>

    <% } %>
</div>

<!-- Update login header if necessary. -->
<%
   boolean flag = userIdEqualsEmail && (userAlias.equals(""));
%>
<script type="text/javascript">
        updateHeader(<%=flag %>);
</script>
</div>
</td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<!-- Hidden Form for Navigation -->
<form name="nav_helper_form" action="LearningCurve" method="post">
    <input type="hidden" name="curriculum_select" />
</form>

</body>
</html>
