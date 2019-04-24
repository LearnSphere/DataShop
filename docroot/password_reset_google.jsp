<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2016
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 13206 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-26 15:40:06 -0400 (Tue, 26 Apr 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>

<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Forgot Password - Google User"; 
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->
<tr>
<td>
<div id="password_reset_webiso_div" class="account_profile_div_style">
        <h1>Google Users</h1>
        <p id="password_reset_webiso" class="error_style">
        Based on your account information, it appears you are using a Google account. You must use the Google sign-in option.
        </p>
        <br/>
        <table id="cmu-login-box">
        <tr><td>
                <span align="center" class="cmulogin"><a href="login">Log in</a></span>
        </td></tr>
        </table>
        
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
