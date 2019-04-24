<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Benjamin Billings
// Version: $Revision: 13157 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-20 16:02:18 -0400 (Wed, 20 Apr 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.performanceprofiler.PerformanceProfilerProducer"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%@page buffer="100kb" %>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Performance Profiler";
    jsIncludes.add("javascript/sampleSelector.js");
    jsIncludes.add("javascript/sampleObject.js");
    jsIncludes.add("javascript/filterObject.js");
    jsIncludes.add("javascript/PerformanceProfiler.js");
    jsIncludes.add("javascript/object/SkillModelList.js");
    jsIncludes.add("javascript/object/ProgressBar.js");
    jsIncludes.add("javascript/object/PopupMenu.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/ProblemContent.js");
    cssIncludes.add("progress_bar.css");
    cssIncludes.add("ProblemContent.css");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    jsIncludes.add("javascript/ErrorReportCommon.js");   // for the tooltip 'Error Report' button
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<%
    //check if user is authorized to view selected dataset
    boolean authorized = false;
    if (datasetItem == null) {
        datasetItem = navHelper.getDataset(info);
    }
    if (datasetItem != null && datasetItem.getId() != null) {
        authorized = projHelper.isAuthorized(remoteUser, datasetItem.getId());
    }

    //if not authorized go back to home page
    if (!authorized) {
        String redirectURL = "index.jsp";
        response.sendRedirect(redirectURL);
    }

    boolean userLoggedIn = (remoteUser == null) ? false : true;
%>

<tr id="body">
<td id="nav">
<div id="navdiv">
    <div id="innerNavDiv">
    <%
        out.print(navHelper.displaySampleNav(true, info));
    %>
    <div id="pp_nav">
        <%@ include file="/performance_profiler_nav.jsp" %>
    </div>
    <div id="skillModels"></div>
    <div id="skills"></div>
    <div id="students"></div>
    <div id="problems"></div>

    </div> <!-- end innerNavDiv -->
</div>
</td>

<td id="content">
<div id="tabheader">
    <%=navHelper.displayTabs("Performance_Profiler", info, authorized, user)%>
</div>
<div id="main">
    <div id="contents">
    <%=contentHeader%>
        <div id="performanceProfiler">
            <h1>&nbsp;</h1>
            <br/><br/>
        </div>
    </div>
</div>
</td></tr>
<!-- footer -->
<%@ include file="/footer.jspf" %>
</td></tr>
</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/profiler.jspf" %>
</div>
<div style="display:none">
<%@ include file="/help/report-level/kc-sets.jspf" %>
</div>
</body>
</html>
