<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Be Jamin Billings
// Version: $Revision: 13157 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-20 16:02:18 -0400 (Wed, 20 Apr 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SubgoalAttemptItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportServlet"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportByProblem"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportStep"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportStepAttempt"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Error Report";
    jsIncludes.add("javascript/ErrorReport.js");
    jsIncludes.add("javascript/sampleSelector.js");
    jsIncludes.add("javascript/sampleObject.js");
    jsIncludes.add("javascript/filterObject.js");
    jsIncludes.add("javascript/object/ProgressBar.js");
    jsIncludes.add("javascript/object/SkillModelList.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/ProblemContent.js");
    cssIncludes.add("progress_bar.css");
    cssIncludes.add("ProblemContent.css");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    jsIncludes.add("javascript/ErrorReportCommon.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- get the navigation helper and check authorization -->
<%
String errorReportViewBy = info.getErrorReportContext().getViewBy();
boolean viewByProblemFlag = true;
if (errorReportViewBy == null) {
    errorReportViewBy = ErrorReportServlet.VIEW_BY_PROBLEM;
    viewByProblemFlag = true;
} else if (errorReportViewBy.compareTo(ErrorReportServlet.VIEW_BY_PROBLEM) == 0) {
    viewByProblemFlag = true;
} else  {
    viewByProblemFlag = false;
}

    boolean authorized = false;

    Integer selectedDatasetId = new Integer(0);
    info = (DatasetContext)session.getAttribute("datasetContext_" + datasetId);

    if (navHelper != null) {

        if (datasetItem == null) {
            datasetItem = navHelper.getDataset(info);
        }
        if (datasetItem != null) {
            selectedDatasetId = (Integer)datasetItem.getId();

            if (projHelper != null) {
        authorized = projHelper.isAuthorized(remoteUser, selectedDatasetId);
            }
        }
    }

    if (!authorized) {
        String redirectURL = "index.jsp";
        response.sendRedirect(redirectURL);
    }

    boolean userLoggedIn = (remoteUser == null) ? false : true;
%>

<!-- body -->
<tr id="body"><td id="nav">

<div id="navdiv">
    <div id="innerNavDiv">
    <!-- display sample box -->
    <%=navHelper.displaySampleNav(true, info)%>

    <!-- display error report options box -->
    <%
    String viewByProblemChecked = "";
    String viewByKCChecked = "";

    if (viewByProblemFlag) {
        viewByProblemChecked = " checked";
    } else {
        viewByKCChecked = " checked";
    }
    %>
    <div id="er_nav">
    <div class="navigationBoxHeader"><h2 class="nav_header">Error Report</h2></div>
    <p>First Attempt Only</p>
    <h3>View by</h3>
        <form method=POST name="er_viewby_form" action="ErrorReport">
            <input type="radio" name="error_report_view_by" id="er_problem" value="problem"
                   onClick="viewByProblem(this)" <%=viewByProblemChecked%> />
            <label for="er_problem">Problem</label><br />

            <input type="radio" name="error_report_view_by" id="er_skill" value="skill"
                   onClick="viewByProblem(this)" <%=viewByKCChecked%> />
            <label for="er_skill">Knowledge Component</label>
        </form>
    </div>

    <!-- display skill model box -->
    <%
    String action = "ErrorReport";

    %>
    <div id="skillModels"></div>
    <%
        if (viewByProblemFlag) {
    %>
        <!-- display problem list selection box -->
        <div id="problems"></div> <!-- Blank div to get filled in via javascript -->
    <%
    } else {
    %>
        <!-- display kc list selection box -->
        <div id="skills"></div>
    <%
    }
    %>

    </div> <!-- end innerNavDiv -->
</div>
</td>

<td id="content">

<div id="tabheader">
<%=navHelper.displayTabs("Error_Report", info, authorized, user)%>
</div>

<div id="main">
    <div id="contents">
        <%=contentHeader%>
        <div id="errorReportDiv">
    <%
        if (viewByProblemFlag) {
    %>
            <jsp:include page="/error_report_content.jsp" />
    <%
    } else {
    %>
            <jsp:include page="/error_report_by_skill.jsp" />
    <%
    }
    %>
        </div>
    </div>
</div></td></tr>
<!-- footer -->
<%@ include file="/footer.jspf" %>
</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/error-report.jspf" %>
</div>
<div style="display:none">
<%@ include file="/help/report-level/kc-sets.jspf" %>
</div>
</body>
</html>
