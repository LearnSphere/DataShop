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
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>
<%

    FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("EEE MMM dd, yyyy HH:mm a");

    Date expirationDate = (Date)session.getAttribute(ForgotPasswordServlet.EXPIRATION_DATE);
    String expirationString = " in 24 hours";
    if (expirationDate != null) {
        expirationString = DATE_FORMAT.format(expirationDate);
    }

%>
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Forgot Password - Email sent";
    showHelpButton = false;
    cssIncludes.add("AccountProfile.css");
    cssIncludes.add("message.css");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->
<tr>
<td>
<div id="password_reset_email_sent_div" class="account_profile_div_style">
        <h1>Reset Password</h1>
        <p id="email_sent_msg" class="success_style">
        Instructions on how to set your password have been sent to your email address.
        </p>
        <p>
        To protect your privacy, these instructions will expire on <%=expirationString%>.
        </p>
        <p>
        If you don't receive an email from us or need to change the email address
        associated with your account, please <a href="help?page=contact">contact us</a>.
        </p>
        <p>
        <a href="index.jsp" id="return_home_page">Return to home page</a>
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
