<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 13474 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-09-08 10:23:28 -0400 (Thu, 08 Sep 2016) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsContext"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.item.ImportQueueItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectReviewDto"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Upload a dataset";
    cssIncludes.add("UploadDataset.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/UploadDataset.js");
    jsIncludes.add("javascript/object/Truncator.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- clean input text method -->
<%!
    // TD element holds the displayed text. So we convert HTML characters to entities,
    // and add line breaks (br) elements.
    String cleanInputText(String str) {
        String textCleaner = str.replaceAll("&","&amp;").
            replaceAll(">","&gt;").replaceAll("<","&lt;");
        String textWithBrs = textCleaner.replaceAll("\n","<br />").replaceAll("\"","&quot;");
        return textWithBrs;
    }
%>
<!-- code -->
<%
    UploadDatasetDto udDto = (UploadDatasetDto)session.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_SETTINGS);

    List<ExistingProjectDto> existingProjectList =
            (List<ExistingProjectDto>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_PROJECTS);

    List<String> recentDatasetNames =
            (List<String>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_RECENT_NAMES);

    List<String> recentDescriptions =
            (List<String>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_RECENT_DESCS);

    List<String> domainLearnlabList =
            (List<String>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_DOMAIN_LEARNLAB_LIST);
    
    List<String> hasStudyDataList =
            (List<String>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_HAS_STUDY_DATA_LIST);

    String domain = udDto.getDomainName();
    String learnlab = udDto.getLearnlabName();
    String hasStudyData = udDto.getHasStudyData();

    String checked = "checked";
    String newChecked = "";
    String existChecked = "";
    String laterChecked = "";
    String hidden = "class=\"hidden\"";
    String newDivClass = hidden;
    String existDivClass = hidden;
    
    if (udDto.getProjectSelection().equals(UploadDatasetDto.PROJ_NEW)) {
        newChecked = checked;
        newDivClass = "";
        existDivClass = hidden;
    } else if (udDto.getProjectSelection().equals(UploadDatasetDto.PROJ_EXIST)) {
        existChecked = checked;
        newDivClass = hidden;
        existDivClass = "";
    } else {
        laterChecked = checked;
    }

    String dct = udDto.getDataCollectionType();
    String dctNotSpecChecked = "";
    String dctNotHumanChecked = "";
    String dctNotReqChecked = "";
    String dctReqChecked  = "";
    if (dct == null || dct.equals(ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED)) {
        dctNotSpecChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT)) {
        dctNotHumanChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ)) {
        dctNotReqChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ)) {
        dctReqChecked = checked;
    } else {
        dctNotSpecChecked = checked;
    }

    String disabled = "disabled=\"disabled\"";
    String grayClass = "class=\"grayTxt\"";
    String loadNowYesChecked = "";
    String loadNowNoChecked = "";
    String dataFileDisabled = "";
    String formatDisabled = "";
    String anonCheckDisabled = "";
    String anonLabelDisabled = "";
    String submitButtonText = "Continue";
    if (udDto.getLoadDataNowFlag()) {
        loadNowYesChecked = checked;
    } else {
        loadNowNoChecked = checked;
        dataFileDisabled = disabled;
        formatDisabled = disabled;
        anonCheckDisabled = disabled;
        anonLabelDisabled = grayClass;
        submitButtonText = "Create";
    }

    String includingLabelStr;
    String includingLabelNote;
    String exceptLabelStr;
    String tabFormatSelected = "";
    String xmlFormatSelected = "";
    if (udDto.getFormat().equals(ImportQueueItem.FORMAT_TAB)) {
        tabFormatSelected = "selected";
        includingLabelStr = "I certify that all data in this file <strong>including</strong> "
                          + "the content of the \"Anon Student Id\" column is de-identified.";
        includingLabelNote = "DataShop will de-identify the \"Anon Student Id\" column.";
        exceptLabelStr = "I certify that all data in this file <strong>except</strong> "
                       + "the content of the \"Anon Student Id\" column is de-identified.";
    } else {
        xmlFormatSelected = "selected";
        includingLabelStr = "I certify that all data in this file <strong>including</strong> "
                          + "the content of the \"user_id\" element is de-identified.";

        includingLabelNote = "DataShop will de-identify the content of the \"user_id\" element.";
        exceptLabelStr = "I certify that all data in this file <strong>except</strong> "
                       + "the content of the \"user_id\" element is de-identified.";
    }

    String anonChecked = "";
    String exceptChecked = "";
    if (udDto.getAnonymizedFlag()) {
        anonChecked = checked;
    } else {
        exceptChecked = checked;
    }
    //so that the help link comes back with the project id in the URL
    String helpLink = "";
    if (udDto.getProjectSelection().equals(UploadDatasetDto.PROJ_CURRENT)) {
        helpLink = "&id=" + udDto.getExistingProjectId();
    }

    // Warning message
    String warningMsg = "To create a new dataset without transaction data, you "
           + "need to select an existing project or specify a new one.";
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="upload_dataset_div">
    <h1>Upload a dataset</h1>

    <% if (udDto.getErrorMessage() != null && udDto.getErrorMessage().length() > 0) { %>
        <p id="status_indicator" class="error_style"><%=udDto.getErrorMessage()%></p>
    <% } %>

    <form id="upload_dataset_form"
        name="upload_dataset_form"
      method="post"
      action="UploadDataset">
    <table id="upload_dataset_table" class="account_profile_table_style">
        <input type="hidden" name="upload_dataset_action" value="describe" />

<% if (udDto.getProjectSelection().equals(UploadDatasetDto.PROJ_CURRENT)) { %>
        <tr><td class="label labelRight"><label>Project Name</label></td>
            <td><%=udDto.getNewProjectName() %>
            <input type="hidden" name="projectId" value="<%=udDto.getExistingProjectId()%>" />
            <input type="hidden" name="projectName" value="<%=udDto.getNewProjectName()%>" />
            </td>
        </tr>
<% } else { %>
        <tr><td class="label labelRight"><label>Project</label></td>
            <td>Add this dataset to ...</td>
        </tr>

        <tr><td></td>
            <td>
            <div id="projectGroupDiv">
            <input type="hidden" name="upload" value="upload" />
            <input type="radio" name="project_group" id="newRadio"
               onclick="javascript:showNewProjectDiv()"
                  value="<%=UploadDatasetDto.PROJ_NEW%>" <%=newChecked%>>
                  <label for="newRadio">new project</label>
            <input type="radio" name="project_group" id="existingRadio"
               onclick="javascript:showExistingProjectDiv()"
                  value="<%=UploadDatasetDto.PROJ_EXIST%>" <%=existChecked%>>
                  <label for="existingRadio">existing project</label>
            <input type="radio" name="project_group" id="laterRadio"
               onclick="javascript:showChooseLaterProjectDiv()"
                  value="<%=UploadDatasetDto.PROJ_LATER%>" <%=laterChecked%>>
                  <label for="laterRadio">choose later</label>
            <div id="no_data_no_project_warning">
            <span><%=warningMsg %></span>
            </div>
            </div>
            <div id="new_project_div" <%=newDivClass%>>
                <table id ="new_project_table">
                <tr>
                  <td class="labelSmaller">Project Name</td>
                  <td><input type="text" name="new_project_name" id="new_project_name"
                            value="<%=udDto.getNewProjectName()%>"
                            title="Please enter a unique project name." /></td>
                </tr>
                <% if (udDto.getNewProjectNameErrorFlag()) { %>
                <tr>
                   <td></td>
                   <td class="errorMessage">This project name is already taken.</td>
                </tr>
                <% } %>
                <tr>
                <td class="labelSmaller">Data Collection Type</td>
                <td>
                <table id="dctTable">
                  <tr>
                  <td class="dctTd">
                  <input type="radio" name="dataCollectionType" id="dctInput1"
                        value="<%=ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED%>"
                        <%=dctNotSpecChecked%>>
                  </td><td>
                  <label for="dctInput1">
                        <%=ProjectReviewDto.DATA_COLLECTION_NOT_SPECIFIED%>
                        </label>
                  </tr><tr>
                  <td class="dctTd">
                  <input type="radio" name="dataCollectionType" id="dctInput2"
                        value="<%=ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT%>"
                        <%=dctNotHumanChecked%>>
                  </td><td>
                  <label for="dctInput2">
                        <%=ProjectReviewDto.DATA_COLLECTION_NOT_HUMAN_SUBJECT%>
                        </label>
                  </tr><tr>
                  <td class="dctTd">
                  <input type="radio" name="dataCollectionType" id="dctInput3"
                        value="<%=ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ%>"
                        <%=dctNotReqChecked%>>
                  </td><td>
                  <label for="dctInput3">
                        <%=ProjectReviewDto.DATA_COLLECTION_CONSENT_NOT_REQD%>
                        </label>
                  </tr><tr>
                  <td class="dctTd">
                  <input type="radio" name="dataCollectionType" id="dctInput4"
                        value="<%=ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ%>"
                        <%=dctReqChecked%>>
                  </td><td>
                  <label for="dctInput4">
                        <%=ProjectReviewDto.DATA_COLLECTION_CONSENT_REQUIRED%>
                        </label>
                  </td></tr>
                </table> <!-- end dctTable -->
                </td>
                </tr>
                </table>
            </div>
            <div id="existing_project_list_div" <%=existDivClass%>>
                <select id="existing_project_select"
                      name="existing_project_select"
                      size="5"
                  onchange="javascript:showExistingProject(this);" >
                    <option value="0"></option>
                <%  Integer selExProjectId = udDto.getExistingProjectId();
                    for (ExistingProjectDto dto : existingProjectList) {
                        String projSel = "";
                        if (selExProjectId != null && selExProjectId.equals(dto.getProjectId())) {
                            projSel = " selected";
                        } %>
                    <option value="<%=dto.getProjectId()%>" <%=projSel%>>
                        <%=dto.getProjectName()%></option>
                <% } %>
                </select>
                <% if (udDto.getExistingProjectIdErrorFlag()) { %>
                   <p class="errorMessage" id="existingProjectErrorMsg">Please select a project.</p>
                <% } %>
                <p class="grayNote">
                Don't see your project here? Check with the project admin to make
                sure you are listed as having "admin" permission.
                </p>
                <%  for (ExistingProjectDto dto : existingProjectList) {
                        String projHidden = hidden;
                        if (selExProjectId != null && selExProjectId.equals(dto.getProjectId())) {
                            projHidden = "";
                        }
                    %>
                    <div id="existing_project_div_<%=dto.getProjectId()%>" <%=projHidden%>>
                      <table id ="existing_project_table_<%=dto.getProjectId()%>">
                      <tr><td class="labelSmaller">Project Name</td>
                          <td><%=dto.getProjectName()%></td></tr>
                      <tr><td class="labelSmaller">PI</td>
                          <td><%=dto.getPi()%></td></tr>
                      <% if (dto.getDp() != null && dto.getDp().length() > 0) { %>
                      <tr><td class="labelSmaller">Data Provider</td>
                          <td><%=dto.getDp()%></td></tr>
                      <% } %>
                      <tr><td class="labelSmaller">Permissions</td>
                          <td><%=dto.getPermissions()%></td></tr>
                      <% if (dto.showShareabilityStatus()) { %>
                      <tr><td class="labelSmaller">Shareability Review Status</td>
                          <td><%=dto.getShareabilityStatusString()%></td></tr>
                      <% } %>
                    </table>
                    </div>
                <% } // end for loop%>
            </div> <!-- end existing_project_list_div -->
            </td>
        </tr>
<% } %>

        <tr><td class="label labelRight" id="datasetNameLabel"><label for="datasetName">Dataset Name</label></td>
            <td>
            <input type="text" name="datasetName" id="datasetName"
                  value="<%=udDto.getDatasetName()%>"
                  title="Please enter a unique dataset name." />
            <% if (recentDatasetNames != null && !recentDatasetNames.isEmpty()) { %>
            <a id="recentDatasetNamesLink" class="recentLink" href="javascript:rdnOpenDialog()">Recent dataset names</a>
            <% } else { %>
            <span id="recentDatasetNamesLinkDisabled">Recent dataset names</span>
            <% } %>
            </td>
        </tr>


        <% if (udDto.getDatasetNameErrorFlag()) { 
            String datasetNameErrorMsg = "This dataset name is already taken";
            if (udDto.getDatasetName().length() > DatasetItem.DATASET_NAME_MAX_LEN) {
                datasetNameErrorMsg = "Dataset name is too long. Must be less than 100 characters.";
            }
        %>
        <tr>
        <td></td>
        <td class="errorMessage"><%=datasetNameErrorMsg%></td>
        </tr>
        <% } %>

        <tr><td class="label labelRight" id="datasetDescLabel"><label for="datasetDesc">Description<br /><span>(optional)</span></label></td>
            <td>
            <textarea name="datasetDesc" id="datasetDesc" class="datasetDesc"
                      rows="4" cols="50"><%=udDto.getDatasetDesccription()%></textarea>
            <% if (recentDescriptions != null && !recentDescriptions.isEmpty()) { %>
            <a id="recentDescLink" class="recentLink"
             href="javascript:rddOpenDialog()">Recent descriptions</a>
             <% } else { %>
             <span id="recentDescLinkDisabled">Recent descriptions</span>
             <% } %>
            </td>
        </tr>
        
        <tr><td class="label labelRight" id="domainLearnlabLabel"><label for="domainLearnlab">Domain/LearnLab</label></td>
            <td>
            <select id="domainLearnlab" name="domainLearnlab">
                    <option value="dummy"> -- </option>
                    <% 
                    String dlSelected = "";
                    for (String dl : domainLearnlabList) {
                        if (domain != null && learnlab != null 
                                && dl.startsWith(domain) && dl.endsWith(learnlab)) {
                            dlSelected = "selected=\"selected\"";
                        } else {
                            dlSelected = "";
                        }
                    %>
                       <option value="<%=dl %>" <%=dlSelected%>><%=dl%></option>
                    <% } %>
            </select>
            </td>
        </tr>
        
        <tr><td class="label labelRight" id="hasStudyDataLabel"><label for="hasStudyData">Has Study Data</label></td>
            <td>
            <select id="hasStudyData" name="hasStudyData">
                    <option value="dummy"> -- </option>
                    <% 
                    String hsdSelected = "";
                    for (String hsd : hasStudyDataList) {
                        if (hasStudyData != null && hsd.startsWith(hasStudyData)) {
                            hsdSelected = "selected=\"selected\"";
                        } else {
                            hsdSelected = "";
                        }
                    %>
                       <option value="<%=hsd %>" <%=hsdSelected%>><%=hsd%></option>
                    <% } %>
            </select>
            </td>
        </tr>
        
        <tr>
            <td colspan="2" class="label labelCenter">
            Do you have transaction data to upload now?
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
            <table id="txToUploadTable">
                <tr><td>
                    <input type="radio"
                           id="txToUploadGroupNo"
                           name="txToUploadGroup"
                           value="no" <%=loadNowNoChecked%>
                           onclick="javascript:disableYesTxOptions()">
                </td><td>
                    <label for="txToUploadGroupNo">
                    No transaction data now
                    </label>
                </td></tr>
                <tr><td></td><td class="grayNote transactionDataTd">
                    Dataset will either receive transaction data
                    later or it will be a files-only dataset.
                </td></tr> <tr><td>
                    <input type="radio"
                           id="txToUploadGroupYes"
                           name="txToUploadGroup"
                           value="yes" <%=loadNowYesChecked%>
                           onclick="javascript:enableYesTxOptions()">
                </td><td>
                    <label for="txToUploadGroupYes">
                    Yes, I want to upload transaction data
                    </label>
                </td></tr>
                <tr><td></td><td class="grayNote transactionDataTd">
                    Can be a single TXT or XML file, or a zip
                    file (.zip, .gz or .bz) containing multiple files.
                </td>
                </tr>
            </table>

            </td>
        </tr>

        <tr></tr>

        <tr><td></td>
        <td class="submit">

        <div id="upload_button_div" class="uploadDatasetButton">
            <button id="choose_button" type="submit"><%=submitButtonText%></button>
        </div>

        </td>
        </tr>

    </table>
    </form>

</div> <!-- End #upload_dataset_div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Populated but hidden select box for recent dataset name dialog -->
<select id="recentDatasetNames" name="recentDatasetNames" <%=hidden%>>
    <%  for (String item : recentDatasetNames) { %>
    <option><%=item%></option>
    <% } %>
</select>

<select id="recentDescriptions" name="recentDescriptions" <%=hidden%>>
    <%  for (String item : recentDescriptions) { %>
    <option value="<%=item %>"><%=item%></option>
    <% } %>
</select>


<!-- Un-populated div needed for modal windows using jQuery -->
<div id="recentDatasetNamesDialog" class="iqDialog"></div>
<div id="recentDescriptionsDialog" class="iqDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/upload-dataset.jspf" %>
</div>

</body>
</html>
