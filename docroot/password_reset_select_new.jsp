<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 13267 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-05-26 14:29:02 -0400 (Thu, 26 May 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.PasswordResetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.ChangePasswordServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%
    String guid =
        (String)session.getAttribute(PasswordResetServlet.GUID);
        
    String invalidNewPasswordMsg = "";
    
    String errorMsg = (String)session.getAttribute(ChangePasswordServlet.NEW_PASSWORD_MSG);
    if (errorMsg != null) {
        invalidNewPasswordMsg = "<tr><td></td><td class=\"errorMessage\">" +  errorMsg + "</td></tr>";
    }
    
    String passwordResetErrorMsg = (String)request.getAttribute(PasswordResetServlet.ERROR_MSG);
%>
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Select a New Password"; 
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->
<tr>
<td>
<div id="password_select_new_div" class="account_profile_div_style">
        <h1>Select a New Password</h1>
        <% if (passwordResetErrorMsg != null) { %>
            <p class="error_style"><%=passwordResetErrorMsg%></p>
        <% } %>
        <form id="password_select_new_form" name="password_select_new_form" 
              method="post" action="PasswordReset?id=<%=guid%>">
        <table id="password_select_new_table" class="account_profile_table_style">
            
            <tr><td class="label"><label for="accountID">Username</label></td>
                <td>
                <input name="accountID" id="accountID" size="16" value="" 
                       title="Please enter your username." />
                </td>
            </tr>
            <tr><td class="label"><label for="password1">New Password</label></td>
                <td>
                <input type="password" name="password1" id="password1" size="16" value="" 
                       title="Please enter a secure password." />
                <input type="hidden" name="change_password_flag" value="true" />
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
            <input name="Submit" value="Save" class="super-emphasize" type="submit" id="submit" />
            <a href="index.jsp" id="cancel_edit_profile">cancel</a>
            </td>
            </tr>
        </table>
        </form>
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
