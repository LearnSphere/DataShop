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
<%@page import="edu.cmu.pslc.datashop.servlet.auth.PasswordResetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.ChangePasswordServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%
%>
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Password Changed"; 
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->
<tr>
<td>
<div id="password_changed_div" class="account_profile_div_style">
        <h1>Password Changed</h1>
        <p id="password_changed" class="success_style">
        Your account password has been changed.
        </p>
        <p>
        <a href="login" id="login">Proceed to Log In</a>
        </p>
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
