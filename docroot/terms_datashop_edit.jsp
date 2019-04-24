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
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseItem"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseVersionItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.ManageTermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Edit Terms";
    cssIncludes.add("Admin.css");
    jsIncludes.add("javascript/TermsDatashopEdit.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<%  // Get the attributes from the HTTP request
    String editDsTerms = (String)ManageTermsServlet.EDIT_DS_TERMS_PARAM;
    String termsText = (String)ManageTermsServlet.TERMS_TEXT_PARAM;
    String publishTerms = (String)ManageTermsServlet.PUBLISH_TERMS_PARAM;
    String saveTerms = (String)ManageTermsServlet.SAVE_TERMS_ATTRIB;
    String selectTerms = (String)ManageTermsServlet.SELECTED_VERSION_PARAM;

    // Get the ToU and ToU Id associated with this project
    TermsOfUseItem touItem = (TermsOfUseItem)(request.getAttribute((String)ManageTermsServlet.TOU_ITEM_ATTRIB));

    // Get the current version to display
    TermsOfUseVersionItem touCurrent = (TermsOfUseVersionItem)(request.getAttribute((String)ManageTermsServlet.TOU_CURRENT_VERSION_ATTRIB));

    // Get a list of all versions to display
    List<TermsOfUseVersionItem> touList = (List<TermsOfUseVersionItem>)(request.getAttribute((String)ManageTermsServlet.TOU_LIST_ATTRIB));

    // User-selected version for this ToU
    String selectedVersionId = (String) request.getAttribute((String)ManageTermsServlet.VERSION_ID_PARAM);

    String notHeadVersion = (String) request.getAttribute((String)ManageTermsServlet.NOT_HEAD_ATTRIB);
    String isDisabled = "";

    if (notHeadVersion != null) {
        isDisabled = "readonly=\"readonly\"";
    }
    cssIncludes.add("Admin.css");
%>


<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">

    <div id="main">

    <div id="contents">
    <!-- Edit Terms of Use Header -->
    <div id="edit-terms-div">
        <h1><a href="ManageTerms">Manage Terms</a> &gt; Editing "<%
            // Print the name of the Datashop Terms of Use
            if (touItem != null) {
                out.print(touItem.getName());
            }
        %> Terms of Use"</h1>
        <form name="edit_ds_terms_form" id="edit_ds_terms_form" action="ManageTerms" method="post">

        <!-- Edit Terms TextArea -->
        <div id="text-area-div"><textarea cols="60" rows="20" name="<%=termsText %>" id="<%=termsText %>" <%=isDisabled %>><%
            // Print the actual terms into the text area
            if (touCurrent != null) {
                out.print(touCurrent.getTerms());
            }
        %></textarea></div>
        <div id="versions-div">

        <!-- Edit Terms of Use Versions Menu -->
        <h2>Versions:</h2>
        <table>

        <%
        FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");

        // Get the selected version to display (if one exists)

        if (selectedVersionId == null && touCurrent != null) {
            selectedVersionId = Integer.toString((Integer)(touCurrent.getId()));;
        }

        // Print each version for this terms of use as a link and
        // load the currently selected Terms into the TextArea when clicked
        if (touList != null && !touList.isEmpty() ) {
            for (TermsOfUseVersionItem touVersionItem : touList) {

                Boolean isSelected = false;
                FieldPosition fieldPos = new FieldPosition(0);
                StringBuffer empty = new StringBuffer();
                out.print("<tr><td>");

                String touVersionId = Integer.toString((Integer)(touVersionItem.getId()));
                if (selectedVersionId != null) {
                    isSelected = selectedVersionId.equals(touVersionId);
                }
                else {
                    isSelected = false;
                }

                out.print( touVersionItem.getVersion() + "</td><td>" );

                // If a version is being displayed currently, then turn its link into plain text
                out.print("<span class=\"terms_side_menu\">");

                if ( !isSelected ) {
                    out.print("<a href=\"javascript:loadSelectedTerms('" + touVersionId + "');\">");
                }
                else {
                    out.print("<a href=\"javascript:loadSelectedTerms('" + touVersionId + "');\" id=\"curlink\">");
                }

                if (touVersionItem.getSavedDate() != null) {
                    out.print(dateFormat.format(touVersionItem.getSavedDate(), empty, fieldPos));
                }
                out.print( "</a></span>");

                out.print( "</td></tr>");
            }
        }
        %>
        </table>
        </div> <!-- End #versions-div -->
        <input type="hidden" name="<%=publishTerms %>" />
        <input type="hidden" name="<%=editDsTerms %>" />

        <!-- This div gets replaced by a new div (a confirmation box) when the submit button is clicked -->
        <div id="save-and-publish-div">
            <button type="button" id="<%=saveTerms %>" disabled>Save and Publish</button>
        </div>
        </form>

    </div> <!-- End #edit-terms-div -->
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
