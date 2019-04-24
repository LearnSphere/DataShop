<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2006
  All Rights Reserved
-->
<%
// Author: Ben Billings
// Version: $Revision: 13459 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
// $KeyWordsOff: $
//
%>

<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectAnnouncements"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectServlet"%>
<%@page import="edu.cmu.pslc.datashop.util.DataShopInstance"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Home";
    jsIncludes.add("javascript/Project.js");
    // Include access request button scripts
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/AccessRequests.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/ResearchGoalsHomePage.js");
    jsIncludes.add("javascript/GoToDatasetProblemList.js");
    cssIncludes.add("ImportQueue.css");
    cssIncludes.add("UploadDataset.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("access_requests.css");
%>
<%@ include file="/header.jspf" %>

<!-- code -->
<%
    // Get the access request notification counts for the user
    String activityNotificationString = "";
    if (remoteUser != null && user != null) {
        AccessRequestHelper arHelper = HelperFactory.DEFAULT.getAccessRequestHelper();
        Map<String, Long> activityNotifications = arHelper.getActivityCountsMap(user);
        long totalNotifications = activityNotifications.get(AccessRequestStatusItem.STATE_COUNT_TOTAL);
        if (totalNotifications > 0) {
            activityNotificationString = "<img src=\"images/flag.png\" title=\"There is recent access-request activity for you to review.\" />";
            session.setAttribute(AccessRequestStatusItem.STATE_COUNT_TOTAL,
                                 (Long)(totalNotifications));
        }
    }

    Boolean showImportQueue = false;
    String datasetsType = (String)session.getAttribute("datasets");
    if (datasetsType == null || datasetsType.equals(ProjectHelper.DATASETS_MINE)) {
        showImportQueue = true;
    }

    String datasetsPage = (String) session.getAttribute("datasetsPage");

    boolean hasNoDatasets = false;
    if (datasetsPage.indexOf("You have no datasets") > 0) {
        hasNoDatasets = true;
    }

    // Project deleted transient message
    boolean showProjectDeletedMsg = false;
    String projDelMsg = (String)session.getAttribute(ProjectPageServlet.PROJECT_DELETED_ATTRIB);
    if (projDelMsg != null) {
        showProjectDeletedMsg = true;
        session.setAttribute(ProjectPageServlet.PROJECT_DELETED_ATTRIB, null);
    }

    // Upload Dataset transient message
    boolean showIqMsg = false;
    String iqMessage = (String)session.getAttribute(UploadDatasetHelper.ATTRIB_IQ_ADDED);
    if (iqMessage != null) {
        showIqMsg = true;
        session.setAttribute(UploadDatasetHelper.ATTRIB_IQ_ADDED, null);
    }

    // Import Queue transient message
    Boolean showErrorMsg = false;
    Boolean showSuccessMsg = false;
    String msgText = (String)request.getAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_TEXT);
    String msgLevel = (String)request.getAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_LEVEL);
    if (msgLevel != null && msgText != null) {
        if (msgLevel.equals(ImportQueueServlet.MSG_LEVEL_ERROR)) {
            showErrorMsg = true;
        } else if (msgLevel.equals(ImportQueueServlet.MSG_LEVEL_SUCCESS)) {
            showSuccessMsg = true;
        }
    }

    // Determine if 'Request Role' dialog should be opened
    boolean openReqRoleDialog = false;
    String reqRoleStr = (String)session.getAttribute(ProjectServlet.OPEN_ROLE_REQUEST_ATTRIB);
    if (reqRoleStr != null) {
        openReqRoleDialog = true;
        session.setAttribute(ProjectServlet.OPEN_ROLE_REQUEST_ATTRIB, null);
    }

    // If this is a slave, use different URLs for recommended datasets.
    boolean isSlave = DataShopInstance.isSlave();
    String recommendedDatasetHref = "";
    String targetStr = "";
    if (isSlave) {
       recommendedDatasetHref = "https://pslcdatashop.web.cmu.edu/";
       targetStr = "target=\"_blank\"";
    }
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents" class="home_pages">

<%@ include file="/login_area.jspf" %>

    <% if (showProjectDeletedMsg) { %>
    <script type="text/javascript">successPopup("<%=projDelMsg %>");</script>
    <% } %>

    <% if (showIqMsg) { %>
    <script type="text/javascript">successPopup("<%=iqMessage%>");</script>
    <% } %>

    <% if (showErrorMsg) { //Import Queue transient message %>
    <script type="text/javascript">errorPopup("<%=msgText %>");</script>
    <% } else if (showSuccessMsg) { %>
    <script type="text/javascript">successPopup("<%=msgText %>");</script>
    <% } %>

    <!-- TRY ME OUT DATASETS
         Show the Production datasets list regardless of the server, even
         though the dataset id may not exist and the dataset name will not match. -->
    <div id="info-boxes-wrapper" class="clearfix">
        <%
        if (remoteUser != null) {
            String tryMeOutDatasetsDisplay = "none";
            String showTryMeOutDatasetsDisplay = "block";
            if (newUserFlag) {
                tryMeOutDatasetsDisplay = "block";
                showTryMeOutDatasetsDisplay = "none";
            }
        %>
            <div id="try-me-out-datasets" class="information-box" style="display:<%=tryMeOutDatasetsDisplay%>">
            <a href="javascript:showTryMeOutDatasets()" class="close-link">x</a>
            <p>
            <% if (newUserFlag) { %>
                <strong>New to DataShop?</strong>
            <% } %>
            Try one of these recommended datasets.
            (They exercise most of DataShop's features and are a good size for exploring the application.)</p>
            <ul>
                <li><a href="<%=recommendedDatasetHref %>DatasetInfo?datasetId=445"
                       <%=targetStr %>>Digital Games for Improving Number Sense - Study 1</a></li>
                <li><a href="<%=recommendedDatasetHref %>DatasetInfo?datasetId=76"
                       <%=targetStr %>>Geometry Area (1996-1997)</a></li>
                <li><a href="<%=recommendedDatasetHref %>DatasetInfo?datasetId=313"
                       <%=targetStr %>>Intelligent Writing Tutor (IWT) Self-Explanation Study 1 (Spring 2009)</a></li>
            </ul>
            <% if (isSlave) { %>
               <p><em>Note: these datasets are on the master DataShop instance at CMU.</em></p>
            <% } %>
            </div>
            <div id="show-try-me-out-datasets" style="display:<%=showTryMeOutDatasetsDisplay%>">
                <a href="javascript:hideTryMeOutDatasets()">Show recommended datasets</a>
            </div>
        <% } // end if (remoteUser != null) %>

        <!-- PROJECT ANNOUNCEMENTS -->
        <%
            String announcementsDisplay = "none";
            String showAnnouncementsDisplay = "block";
            String announcementHtmlList = ProjectAnnouncements.getHtmlList();
            String loggedInClass = "";
            if (remoteUser == null) {
                loggedInClass = "not_logged_in";
            }
        %>
        <div id="project-announcements" class="information-box" style="display:<%=announcementsDisplay%>">
            <a href="javascript:showProjectAnnouncements()" class="close-link">x</a>
            <p><strong>Announcements</strong></p>
            <%=announcementHtmlList%>
        </div>

        <div id="show-project-announcements" class="<%=loggedInClass%>" style="display:<%=showAnnouncementsDisplay%>">
            <a href="javascript:hideProjectAnnouncements()">Show announcements</a>
        </div>

    </div> <!-- end info-boxes-wrapper div -->

    <% if (remoteUser != null) { %>
    <input type="hidden" id="new_user_flag" value="<%=newUserFlag %>" />
    <input type="hidden" id="remote_user" value="<%=remoteUser %>" />

    <div id="show_what_can_ds_do_div">
         <a href="javascript:showWhatCanDsDo()">Show 'What can I do with DataShop?'</a>
    </div>
    <div id="what_can_ds_do_div">
         <input type="hidden" id="rg_heading_display" value="block" />
         <a href="javascript:clearWhatCanDsDo()">
            <img title="Close" alt="Close" src="images/cross.png" />
         </a>
         <h2 id="research_goals_heading" style="display:none">What can I do with DataShop?</h2>
         <div id="research_goals_home_page" style="display:none">
              <div id="researcher_types">
              </div>
              <div id="research_goals">
              </div>
         </div>
    </div>
    <% } %>    <!-- end if (remoteUser != null) -->

    <% if (showImportQueue) { %>
    <div id="import-queue-div">
        <%
        ImportQueueHelper iqHelper = HelperFactory.DEFAULT.getImportQueueHelper();
        List<ImportQueueDto> importQueueList =
                    iqHelper.getImportQueueByUploader(ImportQueueContext.getContext(request),
                                                      remoteUser);
        String iqTableViewType = "my_datasets_queue";

        String datasetColumnHeader = ImportQueueDto.COLUMN_DATASET;
        String userColumnHeader = ImportQueueDto.COLUMN_USER;
        String statusColumnHeader = ImportQueueDto.COLUMN_STATUS;
        String lastUpdateColumnHeader = ImportQueueDto.COLUMN_LAST_UPDATE;

        if (importQueueList.size() > 0) { %>
            <h1>
            <img  id="iqCollapseImage"
                 src="images/contract.png"
               title="Collapse the import queue">
            <img  id="iqExpandImage"
                 src="images/expand.png"
               title="Expand the import queue">
               Import Queue</h1>
            <%@ include file="/jsp_dataset/import_queue_table.jspf" %>
        <% } %>
    </div> <!--  end import-queue-div -->
    <% } %>

    <!-- Here are the datasets from the Project Servlet -->
    <%=datasetsPage %>

    <%
    if (hasNoDatasets) {
    %>
    <div id="uploadDatasetButtonDiv" class="fatButtonDiv">
        <p><a href="<%=uploadDatasetHref %>"
                id="uploadDatasetButtonLink"
             class="ui-state-default ui-corner-all">
            <span class=""></span>Upload a dataset</a></p>
    </div>

    <div id="createProjectButtonDiv" class="fatButtonDiv">
        <p><a href="<%=createProjectHref %>"
                id="createProjectButtonLink"
             class="ui-state-default ui-corner-all">
            <span class=""></span>Create a project</a></p>
    </div>
    <% } %>

</div>
</div>
</td>
</tr>

<%
if (openReqRoleDialog) {
    if (reqRoleStr.equals(ProjectServlet.UPLOAD_DATASET_REDIRECT)) {
        if (hasRequestedDatashopEditRole) {
%>
            <script type="text/javascript">roleRequestPending(true);</script>
<%
        } else {
%>
            <script type="text/javascript">requestDatashopEditRole(true);</script>
<%
        }
    } else if (reqRoleStr.equals(ProjectServlet.CREATE_PROJECT_REDIRECT)) {
        if (hasRequestedDatashopEditRole) {
%>
            <script type="text/javascript">roleRequestPending(false);</script>
<%
        } else {
%>
            <script type="text/javascript">requestDatashopEditRole(false);</script>
<%
        }
    }
}
%>

<!--  we're logged in, but don't show public datasets initially. -->
                <% if (remoteUser != null) { %>
                <script type="text/javascript">
                //document.getElementById('public-data-sets').style.display = "none";
                </script>
                <% } %>
<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="iqCancelImportDialog" class="iqDialog"></div>
<div id="requestDialog" class="requestDialog"></div>
<div id="responseDialog" class="responseDialog"></div>
<div id="renameDatasetDialog" class="renameDatasetDialog"></div>
<div id="deleteDatasetDialog" class="deleteDatasetDialog"> </div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
                <% if (remoteUser != null) { %>
<%@ include file="/help/report-level/home-logged-in.jspf" %>
                <% } else {%>
<%@ include file="/help/report-level/home-not-logged-in.jspf" %>
                <% } %>
</div>

<script type="text/javascript">
    onloadObserver.addListener(initTryMeOutDatasetsState);
    onloadObserver.addListener(initProjectAnnouncementsState);
</script>

</body>
</html>
