<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->

<%
// Author: Alida Skogsholm
// Version: $Revision: 13099 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
// $KeyWordsOff: $
//
%>

<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DecimalFormat"%>

<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.dto.PaperFile"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DomainItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.LearnlabItem"%>
<%@page import="edu.cmu.pslc.datashop.item.PaperItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SubgoalAttemptItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>

<%
    String projectName = "";
    String piName = "";
    String dpName = "";
    String datasetName = "";
    String curriculumName = "";
    String dates = "";
    String domainName = "";
    String learnlabName = "";
    String tutor = "";
    String description = "";
    String hypothesis = "";
    String status = "";
    String studyFlag = "";
    String school = "";
    String acknowledgment = "";
    String notes = "";
    String preferredPaperFileName = "";
    String preferredPaperCitation = "";
    int preferredPaperFileId = 0;
    long preferredPaperFileSize = 0;
    long numberOfStudents = 0;
    String strFormattedNumberOfStudents = "0";
    long numberOfTransactions = 0;
    String strFormattedNumberOfTransactions = "0";
    long numberOfSteps = 0;
    String strFormattedNumberOfSteps = "0";
    long totalNumberOfSteps = 0;
    String strFormattedTotalNumberOfSteps = "0";
    HashMap skillModels = new HashMap();
    long numberOfSkillModels = 0;
    String strFormattedTotalStudentHours = "0";
    String userAgent = (String)request.getHeader("user-Agent");
    String browserType = "";
    List<PaperFile> paperFileList = new ArrayList();
    PaperItem preferredPaper = new PaperItem();
    Long numPapers = 0l;

    if (userAgent.indexOf("MSIE") > -1){
        browserType = "IE";
    }

    UserItem user = (UserItem)session.getAttribute("cmu.edu.pslc.datashop.item.UserItem");
    String remoteUser = (user != null) ? (String)user.getId() : null;
    
    DatasetContext datasetContext  = (DatasetContext)
            session.getAttribute("datasetContext_" + request.getParameter("datasetId"));

    DatasetItem datasetItem = (DatasetItem)request.getAttribute("datasetItem");
    if (datasetItem == null) {
        datasetItem =  DaoFactory.DEFAULT.getDatasetDao().get(
                Integer.valueOf(request.getParameter("datasetId")));
    }
    Integer dsId = (Integer)datasetItem.getId();
    
    ProjectHelper projectHelper = HelperFactory.DEFAULT.getProjectHelper();
    boolean isPublic = projectHelper.isPublic(dsId);
    boolean isAuthorized = projectHelper.isAuthorized(remoteUser, dsId);
    
    boolean hasProjectAdminAuthorization = false;
    boolean hasEditAuthorization = false;
    boolean isDataShopAdmin = false;

    DatasetInfoReportHelper datasetInfoHelper = HelperFactory.DEFAULT.getDatasetInfoReportHelper();
    DatasetInfoReport datasetInfoReport = datasetInfoHelper.getDatasetInfo(datasetItem);

    boolean isRemote = false;
    boolean problemContentAvailable = false;

    if (datasetInfoReport != null) {

        datasetItem = datasetInfoReport.getDatasetItem();
        projectName = (datasetInfoReport.getProjectName() != null)
                            ? datasetInfoReport.getProjectName() : "";
        piName = datasetInfoReport.getPiName();
        dpName = datasetInfoReport.getDpName();

        domainName = (datasetInfoReport.getDomainName() != null)
                            ? datasetInfoReport.getDomainName() : "";
        learnlabName = (datasetInfoReport.getLearnlabName() != null)
                            ? datasetInfoReport.getLearnlabName() : "";
        datasetName = datasetInfoReport.getDatasetItem().getDatasetName();
        dates = datasetInfoReport.getDates();
        if (datasetInfoReport.getCurriculumName() != null) {
            curriculumName = datasetInfoReport.getCurriculumName();
        }

        //datasetItem.get
        if (datasetItem.getTutor() != null) { tutor = datasetItem.getTutor(); }

        if (datasetItem.getDescription() != null) { description = datasetItem.getDescription(); }

        if (datasetItem.getHypothesis() != null) { hypothesis = datasetItem.getHypothesis(); }

        if (datasetItem.getStatus() != null) { status = datasetItem.getStatus(); }

        if (datasetItem.getNotes() != null) { notes = datasetItem.getNotes(); }

        if (datasetItem.getSchool() != null) { school = datasetItem.getSchool(); }

        if (datasetItem.getAcknowledgment() != null) { acknowledgment = datasetItem.getAcknowledgment(); }

        if (datasetItem.getPreferredPaper() != null) {
            preferredPaper = datasetItem.getPreferredPaper();
            preferredPaperFileId =  datasetInfoReport.getFileId();
            preferredPaperFileName = (String)datasetInfoReport.getFileName();
            preferredPaperFileSize = datasetInfoReport.getFileSize();
            preferredPaperCitation = (String)datasetItem.getPreferredPaper().getCitation();
        }

        if (datasetItem.getStudyFlag() != null) { studyFlag = datasetItem.getStudyFlag(); }
        
        numPapers = datasetInfoReport.getNumberOfPapers();

        numberOfStudents = datasetInfoReport.getNumberOfStudents();
        numberOfTransactions = datasetInfoReport.getNumberOfTransactions();
        numberOfSteps = datasetInfoReport.getNumberOfSteps();
        totalNumberOfSteps = datasetInfoReport.getTotalNumberOfSteps();
        skillModels = datasetInfoReport.getSkillModels();
        numberOfSkillModels = skillModels.size();

        DecimalFormat df = new DecimalFormat("#,##0.00");
        strFormattedTotalStudentHours = df.format(datasetInfoReport.getTotalStudentHours());

        DecimalFormat commaDf = new DecimalFormat("#,###,###");
        strFormattedNumberOfStudents = commaDf.format(numberOfStudents);
        strFormattedNumberOfTransactions = commaDf.format(numberOfTransactions);
        strFormattedNumberOfSteps = commaDf.format(numberOfSteps);
        strFormattedTotalNumberOfSteps = commaDf.format(totalNumberOfSteps);

        if (datasetContext != null && projectHelper != null) {
            
            hasProjectAdminAuthorization = datasetContext.getAdminFlag();

            if (hasProjectAdminAuthorization) {
                hasEditAuthorization = true;
            } else {
                hasEditAuthorization = datasetContext.getEditFlag();
            }

            isDataShopAdmin = projectHelper.isDataShopAdmin(
                    request.getRemoteUser(), datasetContext);
        }

        FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
        paperFileList = filesInfoHelper.getPaperList(datasetItem);

        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
        if (navHelper != null) {
            isRemote = navHelper.isDatasetRemote(datasetItem);
            problemContentAvailable = navHelper.isProblemContentAvailable(datasetItem);
        }

        if (isRemote) {
            preferredPaperCitation =
                (datasetInfoReport.getCitation() == null) ? "" : datasetInfoReport.getCitation();
        }
    }

    String editToolTip = "";
    if (hasProjectAdminAuthorization) {
        editToolTip = " title=\"Click to edit\"";
    }

    String editRequiredClass = "";
    if (hasProjectAdminAuthorization && (studyFlag.equals(DatasetItem.STUDY_FLAG_NOT_SPEC))) {
        studyFlag += " -- please set this field";
        editRequiredClass = " class=\"editRequired\"";
    }

    %>

    <div id="datasetInfo">
    <div id="datasetInfoTables">
    <table id="datasetInfoOverview" class="dataset-box">
        <caption>Overview</caption>

        <% if ( isDataShopAdmin ) {  %>
        <tr><th>Dataset Name</th><td id="datasetName"><%=datasetName%></td></tr>
        <tr><th>Project</th><td id="project"><%=projectName%></td></tr>
        <% } %>
        
        <% if (dpName == null || dpName.equals("")) { %>
            <tr><th id="primaryInvestigatorLabel">Principal Investigator</th>
                <td id="primaryInvestigator"><%=piName%></td></tr>
        <% } else {%>
            <tr><th id="primaryInvestigatorLabel">Principal Investigator</th>
                <td id="primaryInvestigator"><%=piName%></td></tr>
            <tr><th id="dataProviderLabel">Data Provider</th>
                <td id="dataProvider"><%=dpName%></td></tr>
        <% } %>
        <tr><th>Curriculum</th><td id="curriculum" <%=editToolTip%>><%=curriculumName%></td></tr>
        <tr>
            <th>Dates</th>
            <td id="dates" <%=editToolTip%>><%
                String startDateString = "";
                if (datasetItem.getStartTime() != null) { startDateString = datasetItem.getStartTime().toString(); }
                String endDateString = "";
                if (datasetItem.getEndTime() != null) { endDateString = datasetItem.getEndTime().toString(); }
                out.print("<span id=\"date_span\">" + dates + "</span>");
                out.print("<input type=\"text\" id=\"start_date\" name=\"start_date\" "
                            + "value=\"" + startDateString + "\" readonly=\"true\" style=\"display: none;\" />");
                out.print("<input type=\"text\" id=\"end_date\" name=\"start_date\" "
                        + "value=\"" + endDateString + "\" readonly=\"true\" style=\"display: none;\" />");
            %></td>
        </tr>
        <tr><th>Domain/LearnLab</th><td id="domain_learnlab" <%=editToolTip%>><%=domainName%><%
            if ((!learnlabName.equals(""))&&(!domainName.equals("Other"))) {
                out.print( "/" + learnlabName);
            } %></td></tr>
        <tr><th>Tutor</th><td id="tutor" <%=editToolTip%>><%=tutor%></td></tr>
        <tr><th>Description</th><td id="datasetDescription" <%=editToolTip%>><pre><%=description %></pre></td></tr>
        <tr><th>Has Study Data</th>
            <td id="studyFlag" <%=editRequiredClass %> <%=editToolTip%>><%=studyFlag %></td>
        </tr>
        <tr id="hypothesisRow"><th>Hypothesis</th><td id="hypothesis" <%=editToolTip%>><%=hypothesis%></td></tr>
        <tr><th>Status</th><td id="datasetStatus" <%=editToolTip%>><%=status%></td></tr>

        <% // (DS1427) Show only if authorized
        if ( user != null ) { %>
        <tr><th>School(s)</th><td id="school" <%=editToolTip%>><%=school%></td></tr>
        <% } %>

        <tr><th>Acknowledgment for <br />Secondary Analysis</th><td id="acknowledgment" <%=editToolTip%>><pre><%=acknowledgment%></pre></td></tr>
        <% if (!isRemote) { %>
        <tr><th>Preferred Citation for <br />Secondary Analysis</th>
            <td id="preferredPaperCitation" <%=editToolTip%>>
             <div id="paper_citation_div" name="paper_citation_div" style="display: block;"><%=preferredPaperCitation.replaceAll("\n", "<BR>").replaceAll("\r", "<BR>")%></div>
             <select id="file_name_size_select" name="file_name_size_select" style="display: none;" onchange="showCitation(this.value);">
                    <option value="0"><img src="images/trans_spacer.gif" width="500" height="10"></option>
                    <%
                    FileItem fileItem = new FileItem();
                    for(PaperFile paperFile : paperFileList ){
                        if (paperFile != null) {
                            fileItem = paperFile.getFileItem();
                            out.println(" <option value=" + fileItem.getId());
                            if (paperFile.getPaperItem().equals(preferredPaper)) {
                                out.println(" selected ");
                            }
                            out.println(">");
                            out.println(fileItem.getFileName() + " (" + (float)fileItem.getFileSize()/(float)1000 + " kb)");
                            out.println(" </option>");
                        }
                    }
                    %>
                </select>
                <input type="hidden" id="preferred_citation_text" value="<%=preferredPaperCitation%>" />
                <input type="hidden" id="preferred_file_id" value="<%=preferredPaperFileId%>" />
               </td>
            </tr>
        <% } else { // isRemote %>
        <tr><th>Preferred Citation for <br />Secondary Analysis</th>
            <td id="preferredPaperCitation" <%=editToolTip%>>
             <div id="paper_citation_div" name="paper_citation_div" style="display: block;"><%=preferredPaperCitation.replaceAll("\n", "<BR>").replaceAll("\r", "<BR>")%></div>
            </td>
        </tr>           
        <% } %>
        <% // (DS1427) Show only if authorized
        if ( user != null ) { %>
        <tr><th>Additional Notes</th><td id="notes" <%=editToolTip%>><pre><%=notes%></pre></td></tr>
        <% } %>

    </table>

    <table id="datasetInfoStats" class="dataset-box">
        <caption>Statistics</caption>
        <tr><th>Number of Students</th><td><%=strFormattedNumberOfStudents%></td></tr>
        <tr><th>Number of Unique Steps</th>
            <% if (numberOfSteps == -1) { %>
            <td></td>
            <% } else { %>
            <td><%=strFormattedNumberOfSteps%></td>
            <% } %>
        </tr>
        <tr><th>Total Number of Steps</th>
            <% if (totalNumberOfSteps == -1) { %>
            <td></td>
            <% } else { %>
            <td><%=strFormattedTotalNumberOfSteps%></td>
            <% } %>
        </tr>
        <tr><th>Total Number of Transactions</th><td><%=strFormattedNumberOfTransactions%></td></tr>
        <tr><th>Total Student Hours</th><td><%=strFormattedTotalStudentHours%></td></tr>
        <% if (numberOfSkillModels != 0 ) {
               // get the set of keys (skill model names) and retrieve their skill counts
               TreeSet skillModelNames = new TreeSet();
               skillModelNames.addAll(skillModels.keySet());
               int kcmCount = 0;
               for (Iterator it = skillModelNames.iterator(); it.hasNext();) {
                    String skillModelName = (String) it.next();
                    int skillCount = 0;
                    Integer skillCountInt = (Integer) skillModels.get(skillModelName);
                    if (skillCountInt != null) {
                        skillCount = skillCountInt.intValue();
                    }
                    %>
                    <tr>
                        <%
                        if (kcmCount == 0) {
                            out.print("<th>Knowledge Component Model(s)</th>");
                        } else {
                            out.print("<th>&nbsp;</th>");
                        }
                        out.print("<td>");
                        out.print(skillModelName + "  (" + skillCount + " ");

                        if (skillCount == 0 || skillCount == 1) {
                            out.print("knowledge component)");
                        } else {
                            out.print("knowledge components)");
                        }
                        out.print("</td>");
                        %>
                    </tr>
              <%
                  kcmCount++;
              }
           } else {
               out.print("<tr><th>Knowledge Component Model(s)</th>");
               out.print("<td>None</td></tr>");
           }
           %>
    </table>
    </div> <!-- end #datasetInfoTables -->

    <% if ((isDataShopAdmin || hasProjectAdminAuthorization) && !isRemote) {  %>
        <div class="infoMessage">
            <img src="images/globe.png" />
            <p ><b>This page is public on the web.</b> <br />
            The "School(s)" and "Additional Notes" fields are private.</p>
        </div>
    <% } %>

    <%
    if (isDataShopAdmin) {
    %>
        <div id="intro">
        <!-- This element tells any javascript that it's OK to allow edits -->
        <p id=hasAdminRights>You are logged in as an administrator.</p>
        </div>
    <%
    } else if (hasProjectAdminAuthorization) {
    %>
        <div id="intro">
        <!-- This element tells any javascript that it's OK to allow edits -->
        <p id=hasEditRights>You have permission to edit some of the rows in the
        dataset overview table.<img src="images/pencil.png">
        <b>Click a field to edit its contents.</b>
        </p>
        </div>
    <%
    }
    %>

    <%
    String remoteAccessButton = projectHelper.getRemoteAccessButton(datasetItem);
    %>
    <div id="remoteAccessButtonDiv"><%=remoteAccessButton %></div>

    <% 
    String accessRequestButton = "";
    if (!isRemote && ((!isAuthorized) && (datasetItem.getProject() != null))) {
        Integer projectId = (Integer)datasetItem.getProject().getId();
        accessRequestButton = projectHelper.getAccessRequestButton(remoteUser, projectId, false);

        if (user != null) {
    %>
            <div id="accessRequestButtonDiv">
                 <%=accessRequestButton%>
            </div>
    <%
        }
    }
    %>
    
    <% if (numPapers > 0) { 
        String title;
        String msg;
        if (numPapers == 1) {
            title = "There is 1 paper attached to this dataset.";
            msg = numPapers + " <a href=\"Files?datasetId=" + dsId
                  + "\" onclick=\"javascript:resetForPapers(" + dsId
                  + ")\">paper</a> is attached to this dataset.";
        } else {
            title = "There are " + numPapers + " papers attached to this dataset.";
            msg = numPapers + " <a href=\"Files?datasetId=" + dsId
                  + "\" onclick=\"javascript:resetForPapers(" + dsId
                  + ")\">papers</a> are attached to this dataset.";
        }
    %>

    <div id="papers_msg" class="infoMessage">
        <img title="<%=title%>" alt="(gold star)" src="images/star.png">
        <p><%=msg%></p>
    </div>
    <% } %>    

<%
        if (problemContentAvailable) {
            String pcTitle = "This dataset contains the problem content that students saw.";
            String pcMsg = "This dataset contains the ";
            if (user != null) {
                pcMsg += "<a href=\"javascript:selectProblemList()\">problem content</a>";
            } else {
                pcMsg += "problem content";
            }
            pcMsg += " that students saw.";
%>

            <div id="pc_msg" class="infoMessage">
                 <img title=<%=pcTitle %> alt="(brick)" src="images/brick.png">
                 <p><%=pcMsg %></p>
            </div>
<%
        }
%>
    
    </div> <!-- end #datasetInfo -->
    <div style="clear:both"></div>

</table>
