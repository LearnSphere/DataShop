<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
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
<%@page import="edu.cmu.pslc.datashop.servlet.auth.RegistrationInfo"%>
<%@ include file="/header_variables.jspf" %>

<% pageTitle = "Congratulations"; %>

<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->

<% 
    RegistrationInfo regInfo = (RegistrationInfo) session.getAttribute("registrationInfo");
    String firstName = "";
    String lastName = "";
    if (regInfo != null) {
        firstName = regInfo.getFirstName();
        lastName = regInfo.getLastName();
    }
%>
<tr>
<%@ include file="/main_side_nav.jspf" %>
    <td id="main_td">
       <div id="contents">
          <div id="congratulations">
          <h1>Congratulations, <%=firstName%>!</h1>
          
          <p>You have successfully created a DataShop account. You now have access to all
          <a href="index.jsp?datasets=public">public datasets</a>. To obtain access to a private project, explore the 
          <a href="index.jsp?datasets=other">private datasets</a> page and click the <em>Request Access</em> button next 
          to any project name.</p>
          
          <!-- OTHER STARTING POINTS? -->
          <!-- HELP BUTTON IN WRONG SPOT -->
          
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
