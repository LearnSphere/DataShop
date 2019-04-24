<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 8659 $
// Last modified by: $Author: bleber $
// Last modified on: $Date: 2013-02-12 10:16:42 -0500 (Tue, 12 Feb 2013) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.ForgotPasswordServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%
    String errorMsg = (String)session.getAttribute(ForgotPasswordServlet.ERROR_MSG);
%>
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Forgot Password";
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->
<tr>
<td>
<div id="forgot_password_div" class="account_profile_div_style">
        <h1>Forgot your password?</h1>
        <% if (errorMsg != null) { %>
            <p id="forgot_password_error_message" class="error_style"><%=errorMsg%></p>
        <% } %>
        <form id="forgot_password_form" name="forgot_password_form" 
              method="post" action="ForgotPassword">
        <table id="forgot_password_table" class="account_profile_table_style">
            <tr><td colspan="2" class="amazon-orange">If you know your username, enter it below:</td></tr>
            <tr><td class="label"><label for="username">Username</label></td>
                <td>
                <input type="text" name="username" id="username" size="32" value="" 
                       title="Please enter your username." />
                </td>
            </tr>
            <tr>
                <td></td>
                <td class="small_label">Note: Username is case-sensitive.</td>
            </tr>
            <tr><td colspan="2" class="amazon-orange">-OR- search by your name and email address:</td></tr>
            <tr><td class="label"><label for="lastName">Last Name</label></td>
                <td>
                <input type="text" name="lastName" id="lastName" size="16" value="" 
                       title="Please enter your last name." />
                </td>
            </tr>
            <tr><td class="label"><label for="email">Email Address</label></td>
                <td>
                <input type="text" name="email" id="email" size="32" value="" 
                       title="Please enter your email address." />
                </td>
            </tr>
            
            <tr><td></td>
            <td class="submit">
            <input name="Submit" value="Request Password" class="super-emphasize" type="submit" id="submit" />
            <a href="index.jsp" id="cancel_forgot_password">cancel</a>
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
