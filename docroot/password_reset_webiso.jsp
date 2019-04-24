<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 13267 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-05-26 14:29:02 -0400 (Thu, 26 May 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>

<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Forgot Password - WebISO User"; 
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
        <h1>Carnegie Mellon Users</h1>
        <p id="password_reset_webiso" class="error_style">
        Based on your account information, it appears you may be affiliated with an <a id=\"inCommonLink\" target=\"_blank\" href=\"http://incommon.org\">InCommon</a> organization. You must use the InCommon sign-in option.
        </p>
        <br/>
        <!-- WebISO login box -->
        <table id="cmu-login-box">
        <tr><td>
                <span align="center" class="cmulogin"><a href="<%=webIsoHref%>">Log in with your organization</a></span>
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
