<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
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
<%@page import="edu.cmu.pslc.datashop.servlet.tou.TermsServlet"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<% 
    //This has to be both before header.jspf and after.
    jsIncludes.add("javascript/HideHelpButtonAndWindow.js");

    pageTitle = "Terms of Use"; 
    showHelpButton = false;
    cssIncludes.add("help.css");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>
<%
    //This has to be both before header.jspf and after.
    jsIncludes.add("javascript/HideHelpButtonAndWindow.js");
%>

<!-- code -->
<%
    // get the terms text
    String termsEffectiveDate = (String)session.getAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB);
    String termsArchivedDate = (String)session.getAttribute(TermsServlet.TERMS_ARCHIVED_DATE_ATTRIB);
    String termsText = (String)session.getAttribute(TermsServlet.TERMS_TEXT_ATTRIB);
    String termsStatus = (String)session.getAttribute(TermsServlet.TERMS_STATUS_ATTRIB);
    Integer termsVersion = (Integer)session.getAttribute(TermsServlet.TERMS_VERSION_ATTRIB);
    ArrayList<String> termsArchived =
        (ArrayList<String>)session.getAttribute(TermsServlet.TERMS_ARCHIVED_ATTRIB);
        
    String show = (String)session.getAttribute(TermsServlet.TERMS_SHOW_ATTRIB);
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

        <div id="datashop-terms">
        <% if (show.equals("no_terms")) { %>
            <h1>DataShop Terms of Use</h1>
            <p>There are no terms associated with DataShop at this time.</p>
        <% } else if (show.equals("invalid_version")) { %>
            <h1>Invalid Version</h1>
            <p>There are no terms associated with the specified version.</p>
        <% } else if (show.equals("list")) { %>
            <h1>Previous Terms of Use</h1>
            <h3>Please Note: These terms are NOT current.
            Please read the <a href="Terms">current terms</a>.</h3>
            <ul>
            <%
            for (int i = 0; i < termsArchived.size(); i++){
                out.println(termsArchived.get(i).toString());
            }
            %>
            </ul>
        <% } else {
               if (show.equals("current")) { %>
                   <h1>DataShop Terms of Use</h1>
	           <%if (termsEffectiveDate != null) { %>
                       <p><em>Effective <%=termsEffectiveDate%></em>
                       <p><%=termsText%></p>
                   <%}
               } else {  %>
                   <h1>Previous Terms of Use</h1>
                   <h3>Please Note: These terms are NOT current.
                   Please read the <a href="Terms">current terms</a>.</h3>
                   <p><em>Version <%=termsVersion%></em>
                   <p><em>Effective <%=termsEffectiveDate%> through <%=termsArchivedDate%></em>
                   <p><%=termsText%></p>
                   <%
               } 
               if ((termsArchived != null) && (termsArchived.size()) > 0) { %>
                   <p><a href="Terms?action=viewPrevList">Archive of previous terms</a></p>
                   <%  
               }      
           } %>
        </div>
    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

<!-- Hidden Form for Navigation -->
<form name="nav_helper_form" action="LearningCurve" method="post">
    <input type="hidden" name="curriculum_select" />
</form>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/registration.jspf" %>
</div>

</body>
</html>
