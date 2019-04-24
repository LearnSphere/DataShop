<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 13540 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-09-22 14:16:36 -0400 (Thu, 22 Sep 2016) $
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

    String showProjectName;
    if (udDto.getProjectSelection().equals(UploadDatasetDto.PROJ_LATER)) {
        showProjectName = "(choose later)";
    } else {
        showProjectName = udDto.getNewProjectName();
    }

    String domainLearnlabStr = udDto.getDomainName() + "/" + udDto.getLearnlabName();
    String hasStudyDataStr = udDto.getHasStudyData();

    String checked = "checked";
    String disabled = "disabled=\"disabled\"";
    String grayClass = "class=\"grayTxt\"";
    String loadNowYesChecked = "";
    String loadNowNoChecked = "";
    String dataFileDisabled = "";
    String formatDisabled = "";
    String anonCheckDisabled = "";
    String anonLabelDisabled = "";
    String submitButtonText = "Upload";
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
    String irbReqtsMetStr = "I certify that this data file meets DataShop's "
                          + "<a href='help?page=irb' target='_blank'>IRB requirements</a> "
                          + "to upload to the DataShop repository, and I agree to add any relevant "
                          + "IRB documentation later.";
    String irbReqtsMet = "";
    String recallTermsStr = "Please keep in mind the DataShop "
                          + "<a href='Terms' target='_blank'>Terms of Use</a> "
                          + "when uploading your data.";
    String dataFromExistingStr = "This data is from a DataShop export.";

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
    <div id="upload_choose_header">
        <div class="circle1"></div>
        <div class="label1">Describe upload</div>
        <hr>
        <div class="circle2"></div>
        <div class="label2">Choose file</div>
        <hr>
        <div class="circle3"></div>
        <div class="label3">Verify</div> 
    </div>
    
    <h1>Upload a dataset</h1>

    <% if (udDto.getErrorMessage() != null && udDto.getErrorMessage().length() > 0) { %>
        <p id="status_indicator" class="error_style"><%=udDto.getErrorMessage()%></p>
    <% } %>

    <form id="upload_dataset_form"
        name="upload_dataset_form"
      method="post"
      action="UploadDataset" enctype="multipart/form-data">

    <table id="upload_dataset_table" class="account_profile_table_style">

        <tr>
        <td class="label labelRight" id="projectNameLabel">
            <label for="projectName">Project Name</label></td>
        <td id="projectName"><%=showProjectName%></td>
        </tr>

        <tr>
        <td class="label labelRight" id="datasetNameLabel">
            <label for="datasetName">Dataset Name</label></td>
        <td id="datasetName"><%=udDto.getDatasetName()%></td>
        </tr>

        <tr>
        <td class="label labelRight" id="datasetDescLabel">
            <label for="datasetDesc">Description<br /></label></td>
        <td id="datasetDesc"><%=udDto.getDatasetDesccription()%></td>
        </tr>

        <tr>
        <td class="label labelRight" id="domainLearnlabLabel">
            <label for="domainLearnlab">Domain/LearnLab<br /></label></td>
        <td id="domainLearnlab"><%=domainLearnlabStr %></td>
        </tr>
        
        <tr>
        <td class="label labelRight" id="hasStudyDataLabel">
            <label for="hasStudyData">Has Study Data<br /></label></td>
        <td id="hasStudyData"><%=hasStudyDataStr %></td>
        </tr>
        
        <tr>
            <td colspan="2" class="label labelCenter">
            Upload transaction data
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
            <div id="upload_tx_div">
                <table id="upload_tx_table">

                <tr>
                <td class="dataFileCol1">Data file</td>
                <td><input type="file"
                             id="fileName"
                           name="fileName" <%=dataFileDisabled%>/>
                </td>
                </tr>

                <% if (udDto.getDataFileErrorFlag()) { %>
                <tr>
                <td></td>
                <td class="errorMessage">Please select a file.</td>
                </tr>
                <% } %>

                <tr>
                <td>
                </td>
                <td id="dataFromExistingDatasetTd">
                    <input type="checkbox" id="dataFromExistingDataset"
                           name="dataFromExistingDataset">
                    <label for="dataFromExistingDataset" id="dataFromExistingDatasetLabel">
                    <%=dataFromExistingStr %>
                    </label>
                </td>
                </tr>

                <tr>
                <td class="dataFileCol1">Format</td>
                <td>
                <select id="format_select" name="format_select" <%=formatDisabled%>>
                    <option value="<%=ImportQueueItem.FORMAT_TAB%>" <%=tabFormatSelected%>>
                    DataShop Tab-Delimited Format</option>
                    <option value="<%=ImportQueueItem.FORMAT_XML%>" <%=xmlFormatSelected%>>
                    DataShop Tutor Message XML Format</option>
                </select>&nbsp;&nbsp;
                <a href="help?page=import<%=helpLink%>">About accepted formats</a>
                </td>
                </tr>

                <tr>
                <td colspan="2">

                <table id="anonOptionsTable">
                    <tr><td>
                        <input type="radio"
                               id="anonIncludingButton"
                               name="anonOptionsGroup"
                               value="including" <%=anonChecked%> <%=anonCheckDisabled%>
                               onclick="javascript:includingChecked()">
                        </td><td>
                        <label for="anonIncludingButton"
                                id="anonIncludingLabel" <%=anonLabelDisabled%>>
                        <%=includingLabelStr %>
                        </label>
                    </td></tr>

                    <tr><td>
                        <input type="radio"
                               id="anonExceptButton"
                               name="anonOptionsGroup"
                               value="except" <%=exceptChecked%> <%=anonCheckDisabled%>
                               onclick="javascript:exceptChecked()">
                    </td><td>
                        <label for="anonExceptButton"
                                id="anonExceptLabel" <%=anonLabelDisabled%>>
                        <%=exceptLabelStr %>
                        </label>
                    </td></tr>
                    <tr><td></td><td id="anonExceptNote" class="grayNote">
                        <%=includingLabelNote %>
                    </td></tr>

                    <tr><td>
                        <input type="radio"
                               id="anonRevealsButton"
                               name="anonOptionsGroup"
                               value="reveals" <%=anonCheckDisabled%>
                               onclick="javascript:revealsChecked()">
                    </td><td>
                        <label for="anonRevealsButton"
                                id="anonRevealsLabel" <%=anonLabelDisabled%>>
                        The data in this file contains information that either reveals or
                        could possibly be used to reveal the identity of human subjects.
                        </label>
                    </td></tr>
                </table> <!-- end anonOptionsTable -->

                </td></tr>

                <tr><td colspan="2">
                <table id="irbRequirementsMetTable">
                    <tr><td>
                    <input type="checkbox" id="irbRequirementsMet" name="irbRequirementsMet"
                           <%=irbReqtsMet %> onclick="javascript:irbReqtsChecked()">
                    </td>
                    <td>
                    <label for="irbRequirementsMet" id="irbRequirementsLabel">
                    <%=irbReqtsMetStr %>
                    </label>
                    </td>
                    </tr>
                </table>
                </tr>

                </table> <!-- end table upload_tx_table -->

                <div id="recallTermsDiv">
                     <span><%=recallTermsStr %></span>
                </div>

            </div> <!-- end upload_tx_div -->
            </td>
        </tr>

        <tr></tr>

        <tr><td></td>
        <td class="submit">

        <div id="upload_button_div" class="uploadDatasetButton">
            <button id="upload_button" type="submit"><%=submitButtonText%></button>
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
