<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved
-->
<%
// Author: Mike Komisin
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
<%@page import="java.text.FieldPosition"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.TermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseItem"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseVersionItem"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dao.TermsOfUseDao"%>
<%@page import="edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.ManageTermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Manage Terms";
    cssIncludes.add("Admin.css");
    jsIncludes.add("javascript/TermsDPManage.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<script src="/javascript/TermsDPManage.js"></script>

<%
String selectTou = (String)ManageTermsServlet.SELECTED_TOU_PARAM;
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">

    <div id="main">
    <div id="contents">
    <div id="manage-terms-div">

        <h1>Project Terms of Use</h1>
        <form name="edit_project_terms_form" action="ManageTerms" method="post">
        <table>
            <tr>
                <td><label>Existing terms</label></td>
                <td><select id="<%=selectTou %>" name="<%=selectTou %>">
                <option value="">- Select Terms -</option>
                <%
                TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
                TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
                Collection projectTouList = touDao.findAllProjectTermsOfUse();

                if (!projectTouList.isEmpty()) {

                    // Display each Project Terms Of Use in the select combo box
                    for ( Object projectTouItem : projectTouList ) {

                        // Search through to test if a terms of use version exists which has been applied
                        // the string is the ToU name
                        String touName = ((TermsOfUseItem)projectTouItem).getName();
                        Integer touId = (Integer)(((TermsOfUseItem)projectTouItem).getId());

                        // if some version is applied
                        if (touVersionDao.hasStatus((TermsOfUseItem)projectTouItem,
                            TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED)) {
                                out.print("<option value=\"" + touId + "\">"
                                    + touName
                                    + " - applied"
                                    + "</option>");
                         }
                         else if (touVersionDao.hasStatus((TermsOfUseItem)projectTouItem,
                            TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_ARCHIVED)) {
                             out.print("<option value=\"" + touId + "\">"
                                + touName
                                + " - archived"
                                + "</option>");
                         }
                         else {
                             out.print("<option value=\"" + touId + "\">"
                                + touName
                                + " - saved"  // all versions are saved or archived
                                + "</option>");
                         }
                    }
                }
                %>

                </select></td>
                <input type="hidden" name="edit_project_terms" />
                <td><button type="submit" id="manage_terms_button" name="manage_terms_button" disabled>Manage</button></td>
            </tr>
            </form>

        <tr><td></td><td colspan="2">
        </td></tr>
        </table>

    </div> <!-- End #manage-terms-div -->
    </div> <!-- End #contents div -->
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
<%@ include file="/help/report-level/manage-terms.jspf" %>
</div>

</body>
</html>
