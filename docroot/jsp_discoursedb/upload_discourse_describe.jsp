<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 12866 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsContext"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.item.ImportQueueItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.UploadDatasetServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectReviewDto"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Upload a discourse";
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

    List<String> hasStudyDataList =
            (List<String>)request.getAttribute(
                    UploadDatasetServlet.REQ_ATTRIB_HAS_STUDY_DATA_LIST);

    String hasStudyData = udDto.getHasStudyData();

    String submitButtonText = "Continue";
        
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
    <h1>Upload a discourse</h1>

    <% if (udDto.getErrorMessage() != null && udDto.getErrorMessage().length() > 0) { %>
        <p id="status_indicator" class="error_style"><%=udDto.getErrorMessage()%></p>
    <% } %>

    <form id="upload_dataset_form"
        name="upload_dataset_form"
      method="post"
      action="UploadDataset">
    <table id="upload_dataset_table" class="account_profile_table_style">
        <input type="hidden" name="upload_dataset_action" value="describe" />

        <tr><td class="label labelRight"><label>Project Name</label></td>
            <td><%=udDto.getNewProjectName() %>
            <input type="hidden" name="projectId" value="<%=udDto.getExistingProjectId()%>" />
            <input type="hidden" name="projectName" value="<%=udDto.getNewProjectName()%>" />
            </td>
        </tr>

        <tr><td class="label labelRight" id="datasetNameLabel"><label for="datasetName">Discourse Name</label></td>
            <td>
            <input type="text" name="datasetName" id="datasetName"
                  value="<%=udDto.getDatasetName()%>"
                  title="Please enter a unique dataset name." />
            </td>
        </tr>


        <% if (udDto.getDatasetNameErrorFlag()) { 
            String datasetNameErrorMsg = "This discourse name is already taken";
            if (udDto.getDatasetName().length() > DatasetItem.DATASET_NAME_MAX_LEN) {
                datasetNameErrorMsg = "Discourse name is too long. Must be less than 100 characters.";
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
            </td>
        </tr>

        <!-- Fake it so the javascript thinks user has selected Domain/Learnlab -->
        <input type="hidden" id="domainLearnlab" value="not_dummy"/>        
        
        <tr><td class="label labelRight" id="hasStudyDataLabel"><label for="hasStudyData">Has Study Data</label></td>
            <td>
            <select id="hasStudyData" name="hasStudyData">
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

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/upload-dataset.jspf" %>
</div>

</body>
</html>
