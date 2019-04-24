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
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage Terms";
    cssIncludes.add("Admin.css");
    jsIncludes.add("javascript/TermsAdminManage.js");
%>
<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<%
    String editProjectTerms = (String)ManageTermsServlet.EDIT_PROJECT_TERMS_PARAM;
    String editDsTerms = (String)ManageTermsServlet.EDIT_DS_TERMS_PARAM;
    String selectTou = (String)ManageTermsServlet.SELECTED_TOU_PARAM;
    String deleteTermsButton = (String)ManageTermsServlet.DELETE_TERMS_BUTTON_PARAM;
    String retireTermsButton = (String)ManageTermsServlet.RETIRE_TERMS_BUTTON_PARAM;
    String createTermsButton = (String)ManageTermsServlet.CREATE_TERMS_BUTTON_PARAM;
    String editDsTermsButton = (String)ManageTermsServlet.EDIT_DS_TERMS_BUTTON_PARAM;
    String createNameText = (String)ManageTermsServlet.CREATE_NAME_TEXT_PARAM;

    String termsExistParam = (String)request.getAttribute((String)ManageTermsServlet.TOU_EXISTS_ATTRIB);
    TermsOfUseVersionItem touVersionItem = (TermsOfUseVersionItem)
        (request.getAttribute((String)ManageTermsServlet.TOU_CURRENT_VERSION_ATTRIB));
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="manage-terms-div">

        <h1>DataShop Terms of Use</h1>
        <form name="edit_ds_terms_form" action="ManageTerms" method="post">
        <%
            // Display Datashop ToU version
            FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
            FieldPosition fieldPos = new FieldPosition(0);
            StringBuffer empty = new StringBuffer();

            if (touVersionItem != null) {
                out.print("<p>Current version: "
                    + touVersionItem.getVersion() + " ("
                    + dateFormat.format(touVersionItem.getAppliedDate(), empty, fieldPos) + ")"
                    + "</p>");
            }
        %>
         <!-- Provide a form to edit, delete, or retire the DataShop Terms Of Use -->
        <input type="hidden" name="<%=editDsTerms %>" />
        <button type="submit" id="<%=editDsTermsButton %>" name="<%=editDsTermsButton %>">Edit</button>
        </form>

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

                <td>
                <input type="hidden" name="<%=editProjectTerms %>" />
                <button type="submit" id="edit_terms_button" disabled>Edit</button>
                <button type="button" id="<%=deleteTermsButton %>" name="<%=deleteTermsButton %>" disabled>Delete</button>
                <button type="button" id="<%=retireTermsButton %>" name="<%=retireTermsButton %>" disabled>Retire</button></td>
            </tr>
            </form>

            <!-- Create project terms form -->
            <form name="create_project_terms_form" action="ManageTerms" method="post">
            <input type="hidden" name="<%=editProjectTerms %>" style="display:none"/>
            <tr><td><label>New terms</label></td><td><input type="text" name="<%=createNameText %>" id="<%=createNameText %>" /></td>
            <td><button type="submit" id="<%=createTermsButton %>" name="<%=createTermsButton %>" disabled>Create</button>
            </td></tr>
            </form>

        <tr><td></td><td colspan="2">
        </td></tr>
        </table>

        <div id="save-and-publish-div"></div>
        <%
        // Check if last project ToU creation was successful
        if ( termsExistParam != null ) {
            String alertTermsCreationStatus = "<br><div id=\"save-and-publish-div\" >"
                + "Cannot create the Terms since a Terms of Use with that name already exists.<br>"
                + "<a href=\"ManageTerms\">I understand</a>"
                + "</div>";
            out.print(alertTermsCreationStatus);
        }

    %>

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
