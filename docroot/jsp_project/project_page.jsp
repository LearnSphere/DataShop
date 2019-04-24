<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetDto"%>
<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14293 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ExternalLinkDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectDatasetDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectDiscourseDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.TermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="org.apache.commons.lang.StringUtils"%>

<%
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
%>

<!-- project-page-specific header stuff -->
<%@ include file="/jsp_project/project_header.jspf" %>

    <% if (showIqMsg) { %>
    <script type="text/javascript">successPopup("<%=iqMessage%>");</script>
    <% } %>

    <% if (showErrorMsg) { //Import Queue transient message %>
    <script type="text/javascript">errorPopup("<%=msgText %>");</script>
    <% } else if (showSuccessMsg) { %>
    <script type="text/javascript">successPopup("<%=msgText %>");</script>
    <% } %>

    <%
        String message = projectDto.getMessage();
        String messageLevel = projectDto.getMessageLevel();

        // Special handling for form submissions, i.e., Project Delete.
        // If the 'message' and 'messageLevel' are NULL on the request, check the session.
        if ((message == null) && (messageLevel == null)) {
           ProjectDto tmpProjectDto =
              (ProjectDto)session.getAttribute(ProjectPageServlet.PROJECT_DTO_ATTRIB + projectId);
           if (tmpProjectDto != null) {
              message = tmpProjectDto.getMessage();
              messageLevel = tmpProjectDto.getMessageLevel();
           }
           // Now that we've read the value, clear it.
           session.setAttribute(ProjectPageServlet.PROJECT_DTO_ATTRIB + projectId, null);
        }

        List<ProjectDatasetDto> datasets = projectDto.getDatasets();
        List<ImportQueueDto> importQueueList = projectDto.getImportQueueList();
        List<ProjectDiscourseDto> discourses = projectDto.getDiscourses();
    %>

    <%
        String disabledDeleteTooltip =
            "Only a project that has no datasets can be deleted. To delete this project, delete or move its datasets first.";
        String disabledRenameTooltip = "";

         boolean isDiscourseProject = ((isDiscourse != null) && isDiscourse) ? true : false;
         if (isDiscourseProject) {
            disabledDeleteTooltip =
                 "Only a project that has no discourses can be deleted. To delete this project, delete its discourses first.";
            disabledRenameTooltip = "The DiscourseDB project cannot be renamed.";
         }
    %>

    <%
    if (adminUserFlag || hasDatashopEditRole) {
        uploadDatasetHref = "UploadDataset?id=" + projectId;
    }

    itemStr = "dataset";
    if (isDiscourseProject) {
        itemStr = "discourse";
    }
    %>

    <div id="project_content_div">

        <%
        if (message != null && messageLevel != null ) {
           if (messageLevel.compareTo(ProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS) == 0) {
        %>
            <script type="text/javascript">successPopup("<%=message%>");</script>
        <%
           } else if (messageLevel.compareTo(ProjectDto.STATUS_MESSAGE_LEVEL_ERROR) == 0) {
        %>
            <script type="text/javascript">errorPopup("<%=message%>");</script>
        <%
           } else {
        %>
            <script type="text/javascript">messagePopup("<%=message%>", "WARNING");</script>
        <% }
        }
        %>

        <% if (adminUserFlag || hasProjectAdmin) { %>
           <div id="project-actions">
                <input type="hidden" id="projectNameHiddenField" value="<%=projectName %>" />
                <span>Project Actions:</span>
                <ul class="concise">
                <% if (!isDiscourseProject) { %>
                   <li><a id="rename-project-link" href="javascript:renameProject(<%=projectId %>)">Rename</a></li>
                <% } else { %>
                   <li><span class="disabled" title="<%=disabledRenameTooltip %>">Rename</span></li>
                <% } %>

                <li><a id="upload-dataset-link" href="<%=uploadDatasetHref %>">Upload a <%=itemStr %></a></li>
                <% if (projectDto.isDeleteAllowed()) { %>
                   <li><a id="delete-project-link" href="javascript:deleteProject(<%=projectId %>)">Delete</a></li>
                <% } else {%>
                   <li><span class="disabled" title="<%=disabledDeleteTooltip %>">Delete</span></li>
                <% } %>
                </ul>
           </div>
        <% } %>

    <div id="project-info-div">
        <div class="project-info-row">
        <% if (authorizationLevel == null) { %>
            <%=accessRequestButton %>
        <% }%>
        <div class="label">PI</div>
        <%
        String classStr = "";
        if (piName.equals("")) {
           classStr = "emptyDiv";
        }
        %>
        <div id="projectInfoPiDiv" class="<%=classStr%>">
        <span id="projectInfoPiSpan"><%=piName%></span>
        <% if (adminUserFlag || hasProjectAdmin) { %>
            <div class="project-edit-link"><a id="projectEditPiLink" href="javascript:showEditPiDiv()">edit</a></div>
        <% } %>
        </div>
        <div id="projectInfoEditPiDiv">
            <% if (piUserId.trim().equals("")) { %>
               <input type="text" id="piNameField" name="piNameField" size="30" title="Enter username">
            <% } else { %>
               <input type="text" id="piNameField" name="piNameField" size="30" title="Enter username"
                      value="<%=piUserId%>">
            <% } %>
            <input type="button" id="piNameSaveButton" value="Save" onclick="javascript:verifyPiUserName(<%=projectId%>)" />
            <input type="button" id="piNameCancelButton" value="Cancel" onclick="javascript:cancelPiNameChange()" />
            <input type="hidden" id="piNameHiddenField" value="<%=piUserId%>" />
        </div>
        </div>
        <%
        // Don't display DP unless an adminUser or dpName != piName.
        if (adminUserFlag || (!dpName.equals(piName))) {
        %>
        <div class="project-info-row clear">
        <div class="label">Data Provider</div>
        <%
        classStr = "";
        if (dpName.equals("")) {
           classStr = "emptyDiv";
        }
        %>
        <div id="projectInfoDpDiv" class="<%=classStr%>">
        <span id="projectInfoDpSpan"><%=dpName%></span>
        <% if (adminUserFlag) { %>
            <div class="project-edit-link"><a id="projectEditDpLink" href="javascript:showEditDpDiv()">edit</a></div>
        <% } %>
        </div>
        <div id="projectInfoEditDpDiv">
            <% if (dpUserId.trim().equals("")) { %>
               <input type="text" id="dpNameField" name="dpNameField" size="30" title="Enter username">
            <% } else { %>
               <input type="text" id="dpNameField" name="dpNameField" size="30" value="<%=dpUserId%>">
            <% } %>
            <input type="button" id="dpNameSaveButton" value="Save" onclick="javascript:saveDpNameChange(<%=projectId%>)" />
            <input type="button" id="dpNameCancelButton" value="Cancel" onclick="javascript:cancelDpNameChange()" />
            <input type="hidden" id="dpNameHiddenField" value="<%=dpUserId%>" />
        </div>
        </div>
        <% } %>
        <div class="project-info-row clear">
        <div class="label">Description</div>
        <%
        classStr = "";
        if (description.equals("")) {
           classStr = "emptyDiv";
        }
        %>
        <div id="projectInfoDescriptionDiv" class="<%=classStr%>">
             <span id="projectInfoDescriptionSpan"><%=descriptionToDisplay%></span>
         <% if (adminUserFlag || hasProjectAdmin) { %>
            <div class="project-edit-link"><a id="projectEditDescLink" href="javascript:showEditDescDiv()">edit</a></div>
         <% } %>
         </div>
        <div id="projectInfoEditDescDiv">
                 <textarea rows="8" cols="50" id="descriptionField"
                          class="descriptionField"
                          name="descriptionField"><%=description%></textarea>
                 <br />
             <input type="button" id="descSaveButton" value="Save" onclick="javascript:saveDescChange(<%=projectId%>)" />
             <input type="button" id="descCancelButton" value="Cancel" onclick="javascript:cancelDescChange()" />
             <input type="hidden" id="descHiddenField" value="<%=descriptionWithoutQuotes%>" />
        </div>
        </div>
        <div class="project-info-row clear">
        <div class="label">Tags</div>
        <%
        classStr = "";
        if (tags.equals("")) {
           classStr = "emptyDiv";
        }
        %>
        <div id="projectInfoTagsDiv" class="<%=classStr%>">
        <span id="projectInfoTagsSpan"><%=tags%></span>
        <% if (adminUserFlag || hasProjectAdmin) { %>
           <div class="project-edit-link"><a id="projectEditTagsLink" href="javascript:showEditTagsDiv()">edit</a></div>
        <% } %>
        </div>
        <div id="projectInfoEditTagsDiv">
             <input id="tagsField" class="tagsField" name="tagsField" size="50" value="<%=tags%>">
             <input type="button" id="tagsSaveButton" value="Save" onclick="javascript:saveTagsChange(<%=projectId%>)" />
             <input type="button" id="tagsCancelButton" value="Cancel" onclick="javascript:cancelTagsChange()" />
                 <br />
                 <div id="projectTagsHowTo">Separate tags with commas.</div>
             <input type="hidden" id="tagsHiddenField" value="<%=tags%>" />
        </div>
        </div>
        <div class="project-info-row clear">
        <div class="label clear">External Links</div>
        <div id="projectInfoLinksDiv">
        <%
        for (ExternalLinkDto link : links) {
            String linkUrl = link.getUrl();
            if ((!linkUrl.startsWith("http://")) && (!linkUrl.startsWith("https://"))) {
                linkUrl = "http://" + linkUrl;
            }
        %>
               <div id="projectInfoLinkDiv_<%=link.getId()%>" class="projectInfoLinkDiv">
                   <span id="projectInfoLinkSpan_<%=link.getId()%>">
                         <a href="<%=linkUrl%>" target="_blank"><%=link.getTitle()%></a>
                   </span>
                       <% if (adminUserFlag || hasProjectAdmin) { %>
                      <div class="project-edit-link">
                   <a id="projectEditLinkLink_<%=link.getId()%>" href="javascript:showEditLinkDiv(<%=link.getId()%>)">edit</a>
                <a id="projectDeleteLinkLink_<%=link.getId()%>" href="javascript:areYouSureExternalLink(<%=link.getId()%>)">
                   <img src="images/delete.gif" class="trash-img">
                </a>
               </div>
                       <% } %>
               </div>
           <div id="projectInfoEditLinkDiv_<%=link.getId()%>" class="projectInfoEditLinkDiv">
               <span id="linkTitleTitle">Title</span><span id="linkUrlTitle">URL</span>
               <br />
               <input id="linkTitleField_<%=link.getId()%>" name="linkTitleField_<%=link.getId()%>" size="20" value="<%=link.getTitle()%>">
               <input id="linkUrlField_<%=link.getId()%>" name="linkUrlField_<%=link.getId()%>" size="20" value="<%=link.getUrl()%>">
               <input type="button" id="linkSaveButton_<%=link.getId()%>" value="Save" onclick="javascript:saveLinkChange(<%=projectId%>, <%=link.getId()%>)" />
               <input type="button" id="linkCancelButton_<%=link.getId()%>" value="Cancel" onclick="javascript:cancelLinkChange(<%=link.getId()%>)" />
               <input type="hidden" id="linkTitleHiddenField_<%=link.getId()%>" value="<%=link.getTitle()%>" />
               <input type="hidden" id="linkUrlHiddenField_<%=link.getId()%>" value="<%=link.getUrl()%>" />
           </div>
           <div class="projectLinkSureDiv" id="projectAreYouSureDiv_<%=link.getId()%>">
               delete this link?&nbsp
                       <a href="javascript:cancelDeleteExternalLink(<%=link.getId()%>)">no</a>
                       &nbsp/&nbsp
                       <a href="javascript:deleteExternalLink(<%=link.getId()%>)">yes</a>
           </div>
        <%
        }
        %>
                  <% if (adminUserFlag || hasProjectAdmin) { %>
               <div class="project-add-link">
               <a id="projectAddLinkLink" href="javascript:showAddLinkDiv()">add</a>
           </div>
           <div id="projectInfoAddLinkDiv" class="projectInfoAddLinkDiv">
               <span id="linkTitleTitle">Title</span><span id="linkUrlTitle">URL</span>
               <br />
               <input id="linkTitleField" name="linkTitleField" size="20" value="">
               <input id="linkUrlField" name="linkUrlField" size="20" value="">
               <input type="button" id="linkSaveButton" value="Save" onclick="javascript:addExternalLink(<%=projectId%>)" />
               <input type="button" id="linkCancelButton" value="Cancel" onclick="javascript:cancelLinkAdd()" />
           </div>
           <% } %>
        </div>
        </div>
    </div>

    <% if (adminUserFlag || hasProjectAdmin) { %>
    <!-- Import Queue Section -->
    <div id="import-queue-div">
        <%
        String datasetColumnHeader = isDiscourseProject ? "Discourse" : ImportQueueDto.COLUMN_DATASET;
        String userColumnHeader = ImportQueueDto.COLUMN_USER;
        String statusColumnHeader = ImportQueueDto.COLUMN_STATUS;
        String lastUpdateColumnHeader = ImportQueueDto.COLUMN_LAST_UPDATE;
        String iqTableViewType = "project_queue";
        if (importQueueList.size() > 0) { %>
            <h1>Import Queue</h1>
            <%@ include file="/jsp_dataset/import_queue_table.jspf" %>
        <% } %>
    </div> <!--  end import-queue-div -->
    <% } %>

    <!-- Datasets Section -->
    <div><br/></div>
    <div id="project-datasets-div">
    <% if (datasets.size() > 0) { %>
    <div id="datasetsLabelDiv" class="label"><span>Datasets</span></div>
    <% if (adminUserFlag || hasRmRole) { %>
    <div id="datasetsEditDiv">
        <a href="javascript:pdDisplayEditModeDatasets()">edit</a>
        </div>
        <div id="datasetsSaveCancelDiv" style="display:none">
        <input id="datasetsSaveButton" type="button"
            onclick="javascript:pdSaveDatasetChanges()"
            value="Save" name="datasetsButton">
        <input id="datasetsCancButton" type="button"
            onclick="javascript:pdCancDatasetChanges()"
            value="Cancel" name="datasetsButton">
    </div>
    <% } %>
    <% if (adminUserFlag || hasRmRole) { %>
    <table id="datasetsTable" class="dataset-box adminColsPresent">
    <% } else { %>
    <table id="datasetsTable" class="dataset-box noAdminColsPresent">
    <% } %>
        <thead>
        <tr>
        <% if (adminUserFlag || hasRmRole) { %>
        <th class="appearsAnon">Appears anonymous?</th>
        <th class="irbUploaded">IRB Uploaded</th>
        <th class="hasStudyData">Has Study Data</th>
        <% } %>
        <th class="name">Dataset</th>
        <th class="domain_learnlab">Domain/ LearnLab</th>
        <th class="dates">Dates</th>
        <th class="dataLastModified">Data Last Modified</th>
        <th class="transactions">Transactions</th>
        <th class="skillModels">KC Models</th>
        <th class="status">Status</th>
        <% if (numPapers > 0) { %>
        <th class="papers">Papers</th>
        <% } %>
        <% if (numDatasetsWithProblemContent > 0) { %>
        <th class="problemContent"></th>
        <% } %>
        <% if (adminUserFlag || hasProjectAdmin) { %>
        <th></th>
        <% } %>
        </tr>
        </thead>
        <tbody>
        <%
        int index = 1;
        String tr_class_name = "";
        List<Integer> datasetIdList = new ArrayList<Integer>();
        for (ProjectDatasetDto dataset : datasets) {
            String suffix = "ds_" + dataset.getId();
            Integer iqId = dataset.getImportQueueId();
            if (index % 2 == 0) {
                tr_class_name = "odd";  // style is a misnomer!
            } else {
                tr_class_name = "";
            }
            datasetIdList.add(dataset.getId());
            String appearsAnonStr = dataset.getAppearsAnonymous();
            String irbUploadedStr = dataset.getIrbUploaded();
            String hasStudyDataStr = dataset.getHasStudyData();
            %>

            <% if (dataset != null && dataset.getImportQueueId() != null) { %>
                <tr class="<%=tr_class_name%>" id="iqrow_<%=dataset.getImportQueueId()%>" name="iqrow">
            <% } else { %>
                <tr class="<%=tr_class_name%>">
            <% }

            if (adminUserFlag || hasRmRole) { %>
                <td class="appearsAnon">
                <div name="pd_div_view_<%=suffix%>" id="pd_appears_anon_div_view_<%=suffix%>"><%=appearsAnonStr%></div>
                <div name="pd_div_edit_<%=suffix%>" id="pd_appears_anon_div_edit_<%=suffix%>" style="display:none">
                <%=projPageHelper.addAppearsAnonSelect("pd_appears_anon_select_" + suffix, appearsAnonStr)%>
                </div></td>

                <td class="irbUploaded">
                <div name="pd_div_view_<%=suffix%>" id="pd_irb_uploaded_div_view_<%=suffix%>"><%=irbUploadedStr%></div>
                <div name="pd_div_edit_<%=suffix%>" id="pd_irb_uploaded_div_edit_<%=suffix%>" style="display:none">
                <%=projPageHelper.addIrbUploadedSelect("pd_irb_uploaded_select_" + suffix, irbUploadedStr)%>
                </div></td>

                <td class="hasStudyData">
                <div name="pd_div_view_<%=suffix%>" id="pd_has_study_data_div_view_<%=suffix%>"><%=hasStudyDataStr%></div>
                <div name="pd_div_edit_<%=suffix%>" id="pd_has_study_data_div_edit_<%=suffix%>" style="display:none">
                <%=projPageHelper.addHasStudyDataSelect("pd_has_study_data_select_" + suffix, hasStudyDataStr)%>
                </div></td>
            <% } %>

            <td class="name">
            <div name="pd_div_view_<%=suffix%>" id="pd_dataset_name_div_view_<%=suffix%>">
                <% if (iqId == null) { %>
                    <a href="DatasetInfo?datasetId=<%=dataset.getId()%>"><%=dataset.getName()%></a>
                <% } else { %>
                    <span id="datasetNameSpan_<%=iqId%>" class="dataset-name">
                    <a href="DatasetInfo?datasetId=<%=dataset.getId()%>"><%=dataset.getName()%></a>
                    </span>
                    <input type="hidden" id="datasetName_<%=iqId%>" value="<%=dataset.getName()%>">
                <% } %>
            </div>
            <div name="pd_div_edit_<%=suffix%>" id="pd_dataset_name_div_view_<%=suffix%>" style="display:none"><%=dataset.getName()%></div>
            </td>

            <td class="domain_learnlab"><%=dataset.getDomainDisplayString().replace("/", "/ ")%></td>
            <td class="dates"><%=dataset.getDateRange()%></td>
            <td class="dataLastModified" title="<%=dataset.getDataLastModifiedTime()%>"><%=dataset.getDataLastModifiedDate()%></td>
            <td class="transactions"><%=dataset.getNumTransactionsFormatted()%></td>
            <td class="skillModels"><%=dataset.getNumSkillModelsFormatted()%></td>
            <td class="status"><%=dataset.getStatus()%></td>
            <% if (numPapers > 0) { %>
            <td class="papers"><%=dataset.getNumPapers()%></td>
            <% } %>
            <% if (numDatasetsWithProblemContent > 0) {
                   if (projHelper.getIsProblemContentAvailable(dataset.getId())) { %>
                       <td class="problemContent">
                           <a href="javascript:goToDatasetProblemList(<%=dataset.getId()%>)">
                           <img src="images/brick.png" alt="(problem content)"
                           title="This dataset contains the problem content that students saw." />
                           </a>
                       </td>
                <% } else {%>
                       <td></td>
                <% } %>
            <% } %>
            <% if (adminUserFlag || hasProjectAdmin) { %>
                <% if (iqId == null) { %>
                <td class="gear">
                    <span class="disabledGear">
                    <img id="gearImage" alt="Gear"
                        src="images/gear-arrow-down-disabled.png"
                      title="Only datasets created through the web application can be moved, renamed or deleted.">
                    </span>
                </td>
                <% } else {
                String disabledString = "_disabled ui-state-disabled";
                String isDisabledString = "";
                String disabledDeleteToolTip = "";
                if (!remoteUser.equals(dataset.getUploaderName()) && !adminUserFlag) {
                    isDisabledString = disabledString;
                    disabledDeleteToolTip = "Only the uploader of a dataset can delete it.";
                }
                %>
                <td class="gear">
                    <div id="gearDropdown_<%=iqId%>" class="gearDropdown">
                        <a id="gearAnchor_<%=iqId%>" class="gearAnchor">
                            <img id="gearImage" alt="Gear" src="images/gear-arrow-down.png">
                        </a>
                        <div id="gearSubmenu_<%=iqId%>" class="gearSubmenu">
                        <ul class="gearUl">
                            <li><a id="moveDatasetLink_<%=iqId%>"
                                class="move_dataset_link">Move to another project</a></li>
                            <li><a id="renameDatasetLink_<%=iqId%>"
                                class="rename_dataset_link">Rename</a></li>
                            <li><a id="deleteDatasetLink_<%=iqId%>"
                                class="delete_dataset_link<%=isDisabledString%>"
                                title="<%=disabledDeleteToolTip%>">Delete</a></li>
                        </ul>
                        </div>
                    </div>
                </td>
                <% } %>
            <% } %>
        </tr>
            <%
            index++;
        } // end for loop
        %>
        </tbody>
    </table>

        <% if (adminUserFlag || hasRmRole) { %>
        <div id="projectDatasetsExplanatoryText">
            <h4 class="grayNote">Appears Anonymous?</h4>
            <p class="grayNote">
            N/A - Student user IDs were de-identified by system<br>
            Yes - Data appears to be anonymous (students not identifiable)<br>
            No - Data reveals student identities<br>
            Not reviewed - Have not reviewed data for anonymity<br>
            More info needed - Unclear whether data is anonymous<br>
            </p>

            <h4 class="grayNote">IRB Uploaded</h4>
            <p class="grayNote">
            TBD - not set by Research Manager yet<br>
            Yes - IRB docs are uploaded and checked by Research Manager<br>
            No - IRB docs still need to be uploaded for this dataset<br>
            N/A - No IRB docs are required for this dataset
            </p>

            <h4 class="grayNote">Has Study Data</h4>
            <p class="grayNote">
            Not Specified - this field has not been set yet<br>
            Yes - Study data is included in this dataset<br>
            No - No study data is included in this dataset
            </p>
        </div>
        <% } %>
    <% } else if (discourses.size() > 0) { %>
         <div id="datasetsLabelDiv" class="label"><span>Discourses</span></div>
         <% if (adminUserFlag || hasRmRole) { %>
         <table id="discoursesTable" class="dataset-box adminColsPresent">
         <% } else { %>
         <table id="discoursesTable" class="dataset-box noAdminColsPresent">
         <% } %>
         <thead>
         <tr>
         <th class="discourse_name">Discourse</th>
         <th class="discourse_dates">Dates</th>
         <th class="users">Users</th>
         <th class="contributions">Contributions</th>
         <th class="data_sources">Data Sources</th>
         <% if (adminUserFlag || hasProjectAdmin) { %>
            <th></th>
         <% } %>
         </tr>
         </thead>
         <tbody>
         <%
         int index = 1;
         String tr_class_name = "";
         for (ProjectDiscourseDto discourse : discourses) {
            String suffix = "ds_" + discourse.getId();
            Integer iqId = discourse.getImportQueueId();
            if (index % 2 == 0) {
                tr_class_name = "odd";  // style is a misnomer!
            } else {
                tr_class_name = "";
            }
         %>

         <tr class="<%=tr_class_name%>">
            <td class="discourse_name">
            <div name="pd_div_view_<%=suffix%>" id="pd_discourse_name_div_view_<%=suffix%>">
            <% if (iqId != null) { %>
                  <span id="datasetNameSpan_<%=iqId%>" class="dataset-name">
                        <a href="DiscourseInfo?discourseId=<%=discourse.getId() %>"><%=discourse.getName()%></a>
                  </span>
                  <input type="hidden" id="datasetName_<%=iqId%>" value="<%=discourse.getName()%>">
                  <!-- Fake it... for the dialogs to have correct labels. -->
                  <input type="hidden" id="format_<%=iqId%>" value="discourse db format">
            <% } else {%>
                  <a href="DiscourseInfo?discourseId=<%=discourse.getId() %>"><%=discourse.getName()%></a>
            <% } %>
            </div>
            </td>

            <td class="discourse_dates"><%=discourse.getDateRange()%></td>
            <td class="users"><%=discourse.getNumUsers()%></td>
            <td class="contributions"><%=discourse.getNumContributionsFormatted()%></td>
            <td class="dataSources"><%=discourse.getNumDataSources()%></td>
            <% if (adminUserFlag || hasProjectAdmin) { %>
                <% if (iqId == null) { %>
                <td class="gear">
                    <span class="disabledGear">
                    <img id="gearImage" alt="Gear"
                        src="images/gear-arrow-down-disabled.png"
                      title="Only discourses created through the web application can be moved, renamed or deleted.">
                    </span>
                </td>
                <% } else {
                String disabledString = "_disabled ui-state-disabled";
                String isDisabledString = "";
                String disabledDeleteToolTip = "";
                if (!remoteUser.equals(discourse.getUploaderName()) && !adminUserFlag) {
                    isDisabledString = disabledString;
                    disabledDeleteToolTip = "Only the uploader of a discourse can delete it.";
                }
                %>
                <td class="gear">
                    <div id="gearDropdown_<%=iqId%>" class="gearDropdown">
                        <a id="gearAnchor_<%=iqId%>" class="gearAnchor">
                            <img id="gearImage" alt="Gear" src="images/gear-arrow-down.png">
                        </a>
                        <div id="gearSubmenu_<%=iqId%>" class="gearSubmenu">
                        <ul class="gearUl">
                            <li><a id="renameDatasetLink_<%=iqId%>"
                                class="rename_dataset_link">Rename</a></li>
                            <li><a id="deleteDatasetLink_<%=iqId%>"
                                class="delete_dataset_link<%=isDisabledString%>"
                                title="<%=disabledDeleteToolTip%>">Delete</a></li>
                        </ul>
                        </div>
                    </div>
                </td>
                <% } %>
            <% } %>
         </tr>
         <%
            index++;
         } // end for loop
         %>

         </tbody>
         </table>
    <% } else { %>
         <div id="datasetsLabelDiv" class="label"><span><%=StringUtils.capitalize(itemStr) %>s</span></div>
         <% if (adminUserFlag || hasProjectAdmin) { %>
         <div id="uploadDatasetButtonDiv" class="fatButtonDiv">
              <p><a href="<%=uploadDatasetHref %>"
                    id="uploadDatasetButtonLink"
                    class="ui-state-default ui-corner-all">
                    <span class=""></span>Upload a <%=itemStr %></a></p>
         </div>
         <% } else { %>
            <span class="emptyPermissionsListSpan">This project contains no <%=itemStr %>s.</span>
         <% } %>
    <%
       } // end if datasets > 0
    %>
    </div> <!-- project-info-div -->
    </div> <!-- project_content_div -->
    </div> <!-- project-page div -->
    </td>
</tr>

<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="iqCancelImportDialog" class="iqDialog"></div>
<div id="renameProjectDialog" class="renameProjectDialog"> </div>
<div id="deleteProjectDialog" class="deleteProjectDialog"> </div>
<div id="projectPublicDialog" class="projectPublicDialog"> </div>
<div id="renameDatasetDialog" class="renameDatasetDialog"> </div>
<div id="deleteDatasetDialog" class="deleteDatasetDialog"> </div>
<div id="requestDialog" class="requestDialog"> </div>
<div id="makePiAdminDialog" class="makePiAdminDialog"> </div>

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
<%@ include file="/help/report-level/project_page.jspf" %>
</div>

</body>
</html>
