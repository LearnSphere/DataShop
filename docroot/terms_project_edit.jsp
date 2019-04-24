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
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao"%>
<%@page import="edu.cmu.pslc.datashop.dao.ProjectTermsOfUseHistoryDao"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectTermsOfUseHistoryItem"%>
<%@page import="edu.cmu.pslc.datashop.dao.ProjectTermsOfUseMapDao"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseItem"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseVersionItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.ManageTermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Manage Terms";
    cssIncludes.add("Admin.css");
    jsIncludes.add("javascript/TermsProjectEdit.js");
%>
<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<%
    FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
    // User interface options
    String pageAction = "";
    String isReadOnly = "";
    String isDisabled = "";
    String isApplyDisabled = "";

    if (hasTermsManagerRole) {
        pageTitle = "Manage Terms";
        pageAction = ""; // Managing
    }
    else if (adminUserFlag) {
        pageTitle = "Edit Terms";
        pageAction = "Editing";
    }

    String selectTou = (String)ManageTermsServlet.SELECTED_TOU_PARAM;
    String editProjectTerms = (String)ManageTermsServlet.EDIT_PROJECT_TERMS_PARAM;
    String applyTerms = (String)ManageTermsServlet.APPLY_TERMS_PARAM;
    String clearTerms = (String)ManageTermsServlet.CLEAR_TERMS_PARAM;
    String termsText = (String)ManageTermsServlet.TERMS_TEXT_PARAM;
    String publishTerms = (String)ManageTermsServlet.PUBLISH_TERMS_PARAM;
    String filterProjects =  (String)ManageTermsServlet.FILTER_PROJECTS_PARAM;
    String saveTerms = (String)ManageTermsServlet.SAVE_TERMS_ATTRIB;
    String selectTerms = (String)ManageTermsServlet.SELECTED_VERSION_PARAM;

    // Get a list of projects
    List<ProjectItem> projects = (List<ProjectItem>) request.getAttribute((String)ManageTermsServlet.PROJECTS_ATTRIB);

    // Get the ToU and ToU Id associated with this project
    TermsOfUseItem touItem = (TermsOfUseItem)(request.getAttribute((String)ManageTermsServlet.TOU_ITEM_ATTRIB));
    String touId = Integer.toString((Integer)(touItem.getId()));

    // Get the current version to display
    TermsOfUseVersionItem touCurrentVersion = (TermsOfUseVersionItem)
        (request.getAttribute((String)ManageTermsServlet.TOU_CURRENT_VERSION_ATTRIB));
    // Get a list of all versions to display
    List<TermsOfUseVersionItem> touList = (List<TermsOfUseVersionItem>)
        (request.getAttribute((String)ManageTermsServlet.TOU_LIST_ATTRIB));

    // User-specified ToU version to display (if one exists)
    String selectedVersionId = (String) request.getAttribute((String)ManageTermsServlet.VERSION_ID_PARAM);

    // Get all available data providers from the project table
    List<UserItem> dataProviders = (List<UserItem>) request.getAttribute((String)ManageTermsServlet.PROVIDERS_ATTRIB);
    // Get the user selected data provider (optional)
    String selectedDataProvider = (String) session.getAttribute((String)ManageTermsServlet.SELECTED_PROVIDER_ATTRIB);

    String notHeadVersion = (String) request.getAttribute((String)ManageTermsServlet.NOT_HEAD_ATTRIB);

    if (hasTermsManagerRole && !adminUserFlag) {
        isReadOnly = "readonly=\"readonly\"";
    }

    if (notHeadVersion != null) {
        isReadOnly = "readonly=\"readonly\"";
        isDisabled = "disabled";
        out.println("<script type=\"text/javascript\">var notHeadVersion = true;</script>");
    }
    else {
        out.println("<script type=\"text/javascript\">var notHeadVersion = false;</script>");
    }

    if (touCurrentVersion == null) {
        isApplyDisabled = "disabled";
    }
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <!-- Edit Terms of Use Header -->
    <div id="edit-terms-div">
        <h1><a href="ManageTerms">Manage Terms</a> &gt; <%=pageAction %> "<%
            // Print the name of the Project Terms of Use
            if (touItem != null) {
                out.print(touItem.getName());
            }
        %>"</h1>

        <!-- Begin Publish and Save Form -->
        <form name="edit_project_terms_form" id="edit_project_terms_form" action="ManageTerms" method="post">

        <!-- Edit Terms TextArea -->
        <div id="text-area-div">
        <textarea cols="60" rows="20" name="<%=termsText %>" id="<%=termsText %>" <%=isReadOnly %>><%
            // Print the actual terms into the text area
            if (touCurrentVersion != null) {
                out.print(touCurrentVersion.getTerms());
            }
        %></textarea></div>
        <div id="versions-div">

        <!-- Begin #versions-div -->
        <h2>Versions:</h2>
        <table>

        <%
        // Get selected version Id
        if (selectedVersionId == null && touCurrentVersion != null) {
            selectedVersionId = Integer.toString((Integer)(touCurrentVersion.getId()));;
        }

        // List versions in reverse order
        if (touList != null) {
            Collections.reverse(touList);
        }
        // Print each version for this terms of use as a link and
        // load the currently selected Terms into the TextArea when clicked
        if (touList != null && !touList.isEmpty() ) {
            for (TermsOfUseVersionItem touVersionItem : touList) {

                // Is this version currently displayed
                Boolean isSelected = false;

                // Date formatting variables
                FieldPosition fieldPos = new FieldPosition(0);
                StringBuffer empty = new StringBuffer();
                out.print("<tr><td>");

                String touVersionId = Integer.toString((Integer)(touVersionItem.getId()));

                // Whether or not this version is currently displayed
                if (selectedVersionId != null) {
                    isSelected = selectedVersionId.equals(touVersionId);
                }
                else {
                    isSelected = false;
                }

                out.print( touVersionItem.getVersion() + "</td><td>" );

                // If a version is being displayed currently, then turn its link into plain text
                // Note: Uses javascript to change the attributes of the 'a' element
                // when the text area is modified (changes text to link or vice versa)
                out.print("<span class=\"terms_side_menu\">");

                if ( !isSelected ) {
                    out.print("<a href=\"javascript:loadSelectedTerms('" + touVersionId + "', '" + touId + "');\" >");
                }
                else {
                    out.print("<a href=\"javascript:loadSelectedTerms('" + touVersionId + "', '" + touId + "');\" id=\"curlink\" >");
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
        </div><!-- End #versions-div -->

        <input type="hidden" name="<%=selectTou %>" value="<%=touId %>" />
        <input type="hidden" name="<%=publishTerms %>" value="" />
        <input type="hidden" name="<%=editProjectTerms %>" value="" />


        <!-- This div gets replaced by a new div (a confirmation box) when the submit button is clicked -->
        <div id="save-and-publish-div">
        <%
        // If the user is a terms manager (i.e. data provider), then don't display the save button
        if (adminUserFlag) {
        %>
            <button type="button" id="<%=saveTerms %>" disabled>Save</button>
        <%
        }
        %>
        </div>
        </form>
    <!-- End Publish and Save Form -->

    <!-- Begin Filter by Data Provider Form -->
    <div id="assign-terms-div">
    <%
    if (adminUserFlag) {
    %>
    <form name="select_data_provider_form" action="ManageTerms" method="post">
    <p>Filter by Data Provider: <select id="<%=filterProjects %>" name="<%=filterProjects %>" onchange="javascript:select_data_provider_form.submit();">
        <option value="">--</option>

        <%
        // Populated a combo box with data providers
        if (dataProviders != null && !dataProviders.isEmpty() ) {
            for (UserItem userItem : dataProviders) {

                // Keep previously selected data provider (if exists)
                String isSelected = "";
                if (selectedDataProvider != null && selectedDataProvider.equals(userItem.getId())) {
                    isSelected = "selected";
                }
                out.print( "<option value=\""
                        + userItem.getId()
                        + "\" " + isSelected + ">" );

                out.print( userItem.getName() );

                out.print( "</option>" );
            }
        }

    %></select>

    <input type="hidden" name="<%=selectTou %>" value="<%=touId %>" />
    <input type="hidden" name="<%=editProjectTerms %>" value="" />
    </form>
    <!-- End of Filter by Data ProviderForm -->
    </p>

    <!-- Begin Select Projects Table Form -->
    <%
    }

    if (projects != null && !projects.isEmpty()) {
    %>
    <form name="edit_project_terms_map_form" action="ManageTerms" method="post">
    <table id="project_table" class="dataset-box">
    <thead>
        <th class="col1"><input type="checkbox" id="select_all" name="select_all" value="" /></th>
        <th>Project</th>
        <th>Data Provider</th>
        <th>Terms in Effect</th>
        <th>Version</th>
        <th>Effective Date</th>
    </thead>
    <tbody>
    <%
            TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
            ProjectTermsOfUseMapDao mapDao = DaoFactory.DEFAULT.getProjectTermsOfUseMapDao();
            // Populated a projects table with projects

            if (projects != null && !projects.isEmpty() ) {
                for (ProjectItem project : projects) {

                    // Date formatting variables
                    FieldPosition fieldPos = new FieldPosition(0);
                    StringBuffer empty = new StringBuffer();

                    String dataProviderName = "";
                    String termsName = "";
                    String termsEffective = "";
                    String appliedVersionNumber = "";

                    if ( project.getDataProvider() != null ) {
                        dataProviderName = project.getDataProvider().getName();
                    }

                    // Populate the project-terms table
                    TermsOfUseItem touTemp = (TermsOfUseItem) mapDao.getTermsOfUseForProject(project);
                    TermsOfUseVersionItem touVersionTemp = null;
                    String isChecked = "";

                    if ( touTemp != null ) {
                        termsName = touTemp.getName();

                        touVersionTemp = (TermsOfUseVersionItem) touVersionDao.findAppliedVersion(termsName);
                        if (touVersionTemp != null) {
                            appliedVersionNumber = touVersionTemp.getVersion().toString();
                            termsEffective =  dateFormat.format(touVersionTemp.getAppliedDate(), empty, fieldPos).toString();

                            if ( touVersionTemp.equals(touCurrentVersion) ) {
                                isChecked = "checked=yes";
                            }
                        }
                        else {
                            termsName = "";
                        }
                    }

                    out.print( "<tr><td class=\"col1\"><input type=\"checkbox\" class=\"boxes\" name=\"selectedProjectIds\" value=\""
                            + project.getId() + "\" "
                            + isChecked + "/></td>"
                            + "<td>" + project.getProjectName() + "</td>"
                            + "<td>" + dataProviderName + "</td>"
                            + "<td>" + termsName  + "</td>"
                            + "<td>" + appliedVersionNumber + "</td>"
                            + "<td>" + termsEffective + "</td></tr>"
                    );
            }
        }
    %>
    </tbody>

    <input type="hidden" name="<%=selectTou %>" value="<%=touId %>" />
    <input type="hidden" name="<%=applyTerms %>" value="" />
    <input type="hidden" name="<%=editProjectTerms %>" value="" />

    </table>
    </div>
    <div id="apply-clear-terms-div">
        <div>
        <button type="submit" id="apply_terms_button" <%=isDisabled %> <%=isApplyDisabled %>>Apply Terms</button>
        <button type="submit" id="clear_terms_button" name="<%=clearTerms %>" <%=isDisabled %>>Clear Terms</button>
        </div>
    </div>

    </form>
    <!-- End Select Projects Table Form -->
    <%
    } else {
        %>
        <div id="warning-div">
        You are not listed as the Data Provider for any projects.
        </div>
        <%
    }
    %>
    </div>

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
<%@ include file="/help/report-level/registration.jspf" %>
</div>

</body>
</html>
