<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 10513 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsContext"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.VerificationResults"%>
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
    
    VerificationResults results = udDto.getResults();
    VerificationResults.Messages genMsgs = results.getGeneralMessages();
    ArrayList<VerificationResults.Messages> fileMsgsArrayList = results.getFileMessages();
    
    Integer totalErrors = results.getTotalErrors();
    Integer totalIssues = results.getTotalIssues();
    
    boolean passedFlag = false;
    boolean showBackButton = false;
    boolean showContinueButton = false;

    if (results.getStatus().equals(ImportQueueItem.STATUS_PASSED)) {
        passedFlag = true;
        showContinueButton = true;
    } else if (results.getStatus().equals(ImportQueueItem.STATUS_ISSUES)) {
        showBackButton = true;
        showContinueButton = true;
    } else {
        showBackButton = true;
    }
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="upload_dataset_div">
    <div id="upload_verify_header">
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

    <form id="verify_upload_dataset_form"
        name="verify_upload_dataset_form"
      method="post"
      action="UploadDataset">

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
        
        <td class="label labelRight" id="hasStudyDataLabel">
            <label for="hasStudyData">Has Study Data<br /></label></td>
        <td id="hasStudyData"><%=hasStudyDataStr %></td>
        </tr>
        
        <tr>
        <td class="label labelRight" id="dataFileLabel">
            <label for="dataFile">Data file<br /></label></td>
        <td id="dataFile"><%=udDto.getDataFile()%></td>
        </tr>
    </table>

    <p class="verifyResults">Verification Results</p>
    
    <% if (passedFlag) { %>
        <div class="success"><%=results.getSuccessMessage()%></div>
    <% } else { %>
        <% if (totalErrors != null && totalErrors > 0 && totalIssues != null && totalIssues >0) { %>
        <p id="summary" class="summary">
        <% if (totalErrors != null && totalErrors > 0) { %>
        <%=totalErrors%> error<% if (totalErrors > 1) { %>s<% } %>
        and
        <% if (totalIssues != null && totalIssues > 0) { %>
        <%=totalIssues%> potential issue<% if (totalIssues > 1) { %>s<% } %>
        <% } %>
        found.</p>
        <% } %>
        <% } %>
        
        <% if (totalErrors != null && totalErrors > 0) { %>
        <p id="errorHeading" class="errorHeading"><%=totalErrors%> error<% if (totalErrors > 1) { %>s<% } %></p>
        <div>
            <div> 
            <% 
            int geidx = 0;
            for (String msg : genMsgs.getErrors()) { %>
                <div id="genErrorMsgs_<%=geidx%>" class="uploadVerifyResults"><%=msg%></div>
            <% } %>
            </div>
        </div>
        <% } %>
    
        <% 
        int feidx = 0;
        for (VerificationResults.Messages vm : fileMsgsArrayList) { 
            ArrayList<String> errors = vm.getErrors();
            if (errors.size() > 0) { %>
                <% if (vm.getFileName() != null) { %>
                    <p class="fileHeader">In file <%=vm.getFileName()%>:</p>
                <% } %>
                <div> 
                <% 
                for (String msg : errors) { %>
                    <div id="fileErrorMsgs_<%=feidx%>" class="uploadVerifyResults"><%=msg%></div>
                <% } %>
                </div>
            <% } %>
        <% } %>

        <% if (totalIssues != null && totalIssues > 0) { %>
        <p id="issuesHeading" class="issuesHeading"><%=totalIssues%> potential issue<% if (totalIssues > 1) { %>s<% } %></p>
        <div>
            <div> 
            <% 
            int giidx = 0;
            for (String msg : genMsgs.getIssues()) { %>
                <div id="genIssues_<%=giidx%>" class="uploadVerifyResults"><%=msg%></div>
            <% } %>
            </div>
        </div>
        <% } %>
    
        <% 
        int fiidx = 0;
        for (VerificationResults.Messages vm : fileMsgsArrayList) { 
            ArrayList<String> issues = vm.getIssues();
            if (issues.size() > 0) { %>
                <% if (vm.getFileName() != null) { %>
                    <p class="fileHeader">In file <%=vm.getFileName()%>:</p>
                <% } %>
                <div> 
                <% for (String msg : issues) { %>
                    <div id="fileIssues_<%=fiidx%>" class="uploadVerifyResults"><%=msg%></div>
                <% } %>
                </div>
            <% } %>
        <% } %>
    <% } %>
    
    <% if (showBackButton && showContinueButton) {%>
    <div id="back_and_continue_div">
    <% } %>
    <% if (showBackButton) { %>
    <div id="back_button_div" class="uploadDatasetButton">
        <input id="back_button" type="button" value="Back"
          onclick="javascript:backButton()" />
        
    </div>
    <% } %>
    <% if (showBackButton && showContinueButton) {%>
    <div id="or_div">or</div>
    <% } %>
    <% if (showContinueButton) { %>
    <div id="upload_button_div" class="uploadDatasetButton">
        <input id="continue_button" type="button" value="Continue"
          onclick="javascript:continueButton()" />
    </div>
    <% } %>
    <% if (showBackButton && showContinueButton) {%>
    </div>
    <% } %>
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
