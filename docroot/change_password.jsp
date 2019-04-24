<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14794 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-02-07 20:20:23 -0500 (Wed, 07 Feb 2018) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.ChangePasswordServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%

    String invalidNewPasswordMsg = "";
    String invalidCurrentPasswordMsg = "";
    
    String errorMsg = (String)session.getAttribute(ChangePasswordServlet.NEW_PASSWORD_MSG);
    if (errorMsg != null) {
        invalidNewPasswordMsg = "<tr><td></td><td class=\"errorMessage\">" +  errorMsg + "</td></tr>";
    }
      
    errorMsg = (String)session.getAttribute(ChangePasswordServlet.CURRENT_PASSWORD_MSG);
    if (errorMsg != null) {
        invalidCurrentPasswordMsg = "<tr><td></td><td class=\"errorMessage\">" +  errorMsg + "</td></tr>";
    }

%>
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Change Password"; 
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
    jsIncludes.add("javascript/AccountProfile.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="contents">
<div id="change_password_div" class="account_profile_div_style">
        <h1>Change Password</h1>
        <form id="change_password_form" name="change_password_form" 
              method="post" action="ChangePassword">
        <table id="change_password_table" class="account_profile_table_style">
            <tr><td colspan="2" class="amazon-orange">What is your current password?</td></tr>
            <tr><td class="label"><label for="password0">Current Password</label></td>
                <td>
                <input type="password" name="password0" id="password0" size="16" value="" 
                       title="Please enter your current password." />
                <input type="hidden" name="change_password_flag" value="true" />
                </td>
                <%=invalidCurrentPasswordMsg%>
            </tr>
            
            <tr><td colspan="2" class="amazon-orange">What is your new password?</td></tr>
            <tr><td class="label"><label for="password1">New Password</label></td>
                <td>
                <input type="password" name="password1" id="password1" size="16" value="" 
                       title="Please enter a secure password." />
                </td>
            </tr>
            <tr><td class="label"><label for="password2">Confirm Password</label></td>
                <td>
                <input type="password" name="password2" id="password2" size="16" value="" 
                       title="Please re-type your password to confirm." />
                </td>
                <%=invalidNewPasswordMsg%>           
            </tr>
            
            <tr><td></td>
            <td class="submit">
            <a href="javascript:clickSavePasswordButton();" name="Submit" id="submit">Save</a>
            <a href="AccountProfile" id="cancel_edit_profile">Cancel</a>
            </td>
            </tr>
        </table>
        </form>
</div>
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

</BODY>
</HTML>
