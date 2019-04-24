<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 11049 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-05-19 11:27:01 -0400 (Mon, 19 May 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.TermsServlet"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<% 
    pageTitle = "Terms of Use"; 
    showHelpButton = false;
    cssIncludes.add("help.css");
    jsIncludes.add("javascript/TermsAgree.js");
%>
<%@ include file="/header.jspf" %>

<!-- code -->
<%
    // get the terms text, date and type
    String termsType = (String)session.getAttribute(TermsServlet.TERMS_TYPE_ATTRIB);
    String termsText = (String)session.getAttribute(TermsServlet.TERMS_TEXT_ATTRIB);
    String termsDate = (String)session.getAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB);
    String termsProjectName = (String)session.getAttribute(TermsServlet.TERMS_PROJECT_NAME_ATTRIB);
    Boolean termsUpdateFlag = (Boolean)session.getAttribute(TermsServlet.TERMS_UPDATE_FLAG_ATTRIB);
    
    Boolean showProjectTerms = false;
    String introText = "The DataShop terms of use have been updated.";
    if (termsType != null && termsType.equalsIgnoreCase(TermsOfUseItem.PROJECT_TERMS)) {
        showProjectTerms = true;
        
        if (termsUpdateFlag) {
            introText = "The terms of use for this project have been updated.";
        } else {
            introText = "The provider of this data has requested you agree to their terms to access datasets in this project.";
        }
    }
%>

<!-- body -->
<tr>
<td>

    <div id="terms-agree">
        <h1><%=termsType%> Terms of Use</h1>
        <% if (showProjectTerms) { %>
            <h2><%=termsProjectName%></h2>
        <% } %>

        <p class="intro-text">
        <%=introText%>
        Please read them and click "Yes, I agree to these terms" to continue.</p>

    <form id="termsAgreeForm" name="termsAgreeForm" method="post" action="TermsAgree">
        <input type="hidden" name="termsAgreePage" />
        <div id="terms">
            <p><em>Effective <%=termsDate%></em></p>
            <%=termsText%>
        </div>
        <div id="agree_checkbox">
            <input type="checkbox" 
                   name="agreeCheckbox" id="agreeCheckbox" 
                   value="agree" /> 
            <label for="agreeCheckbox">Yes, I agree to these terms (required)</label>
        </div>
        <div id="continue_button_div">
            <button type="submit" id="continueButton" disabled="disabled">Continue</button>
        </div>
        </div>
    </form>
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

</body>
</html>
