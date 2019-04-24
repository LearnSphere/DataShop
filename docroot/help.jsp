<%@ include file="/doctype.jspf"%>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%// Author: Brett Leber
  // Version: $Revision: 14976 $
  // Last modified by: $Author: ctipper $
  // Last modified on: $Date: 2018-03-22 15:32:06 -0400 (Thu, 22 Mar 2018) $
  // $KeyWordsOff: $
%>

<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.util.DataShopInstance"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal"%>

<%@ include file="/header_variables.jspf"%>

<%
    cssIncludes.add("help.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    //This has to be both before header.jspf and after.
    jsIncludes.add("javascript/HideHelpButtonAndWindow.js");

    String pageFragment = (String)session.getAttribute("page_filename");
    String pageDisplayTitle = (String)session.getAttribute("page_title");
    if (pageDisplayTitle != null && !pageDisplayTitle.equals("")) {
        pageTitle = "Help > " + pageDisplayTitle;
    } else {
        pageTitle = "Help";
    }

%>
<!--  header -->
<%@ include file="/header.jspf"%>
<%
    //This has to be both before header.jspf and after.
    jsIncludes.add("javascript/HideHelpButtonAndWindow.js");

    // Some "Back to..." URLs only make sense if datasetId available.
    boolean requiresId = false;
    boolean requiresDiscourseId = false;

    String recentReportServlet = (String)session.getAttribute("recent_report");
    String recentReportName = "DataShop";
    if (recentReportServlet != null) {
        if (recentReportServlet.equals("LearningCurve")) {
            recentReportName = "Learning Curve";
            requiresId = true;
        } else if (recentReportServlet.equals("ErrorReport")) {
            recentReportName = "Error Report";
            requiresId = true;
        } else if (recentReportServlet.equals("Export")) {
            recentReportName = "Export";
            requiresId = true;
        } else if (recentReportServlet.equals("DatasetInfo")) {
            recentReportName = "Dataset Info";
            requiresId = true;
        } else if (recentReportServlet.equals("PerformanceProfiler")) {
            recentReportName = "Performance Profiler";
            requiresId = true;
        } else if (recentReportServlet.equals("registration")) {
            recentReportName = "Create a New DataShop Account";
        } else if (recentReportServlet.equals("Files")) {
            recentReportName = "Files";
            requiresId = true;
        } else if (recentReportServlet.equals("Advanced")) {
            recentReportName = "Advanced";
        } else if (recentReportServlet.equals("AccessRequests")) {
            recentReportName = "Access Requests";
        } else if ((recentReportServlet.equals("MetricsReport")) ||
                   (recentReportServlet.equals("WebServicesCredentials")) ||
                   (recentReportServlet.equals("LoggingActivity")) ||
                   (recentReportServlet.equals("ManageTerms"))) {
            recentReportName = "Advanced";
        } else if (recentReportServlet.equals("AdminDomainLearnLab")) {
            recentReportName = "Admin";
        } else if (recentReportServlet.equals("ExternalTools")) {
            recentReportName = "External Tools";
        } else if (recentReportServlet.equals("IRBReview")) {
            recentReportName = "IRB Review";
        } else if (recentReportServlet.equals("AccountProfile")) {
            recentReportName = "Account Profile";
        } else if (recentReportServlet.equals("ManageUsers")) {
            recentReportName = "Manage Users";
        } else if (recentReportServlet.equals("CreateProject")) {
            recentReportName = "Create Project";
        } else if (recentReportServlet.equals("UploadDataset")) {
            recentReportName = "Upload Dataset";
        } else if (recentReportServlet.equals("ImportQueue")) {
            recentReportName = "Import Queue";
        } else if (recentReportServlet.equals("Project") ||
                   recentReportServlet.equals("ProjectPermissions") ||
                   recentReportServlet.equals("ProjectIRB") ||
                   recentReportServlet.equals("ProjectTerms")) {
            recentReportName = "Project";
        } else if (recentReportServlet.equals("ManageProblemContent")) {
            recentReportName = "Manage Problem Content";
        } else if (recentReportServlet.equals("DiscourseInfo")) {
            recentReportName = "Discourse Info";
            requiresDiscourseId = true;
        } else if (recentReportServlet.equals("DiscourseExport")) {
            recentReportName = "Discourse Export";
            requiresDiscourseId = true;
        } else if (recentReportServlet.equals("ManageWorkflows")) {
            recentReportName = "Manage Workflows";
        } else if (recentReportServlet.equals("DatasetWorkflows")) {
            recentReportName = "Dataset Workflows";
        }
    } else {
        recentReportServlet = "index.jsp";
    }

    StringBuffer backToAppUrl = new StringBuffer(recentReportServlet);
    if (datasetId != null) {
       backToAppUrl.append("?datasetId=");
       backToAppUrl.append(datasetId);

       // Save it for pages 'downstream'...
       session.setAttribute("help_datasetId", datasetId);
    } else if (projectIdStr != null) {
       backToAppUrl.append("?id=");
       backToAppUrl.append(projectIdStr);
    } else if (discourseIdStr != null) {
       backToAppUrl.append("?discourseId=");
       backToAppUrl.append(discourseIdStr);

       // Save it for pages 'downstream'...
       session.setAttribute("help_discourseId", discourseIdStr);
    }

    // If datasetId is required but not available, check session...
    if (requiresId && (datasetId == null)) {
       datasetId = (String)session.getAttribute("help_datasetId");
       if (datasetId != null) {
          backToAppUrl.append("?datasetId=");
          backToAppUrl.append(datasetId);
       }
    }

    // If discourseId is required but not available, check session...
    if (requiresDiscourseId && (discourseIdStr == null)) {
       discourseIdStr = (String)session.getAttribute("help_discourseId");
       if (discourseIdStr != null) {
          backToAppUrl.append("?discourseId=");
          backToAppUrl.append(discourseIdStr);
       }
    }

    String datashopHelpEmail = DataShopInstance.getEncodedDatashopHelpEmail();
%>
<!-- body -->
<tr id="body"><td id="nav"></td>

<td id="content">
<div id="main">
<div id="contents">
    <p id="backToApp"><a href="<%=backToAppUrl.toString() %>">
    &#171 Back to <%=recentReportName%></a></p>
<div id="help">
<div id="toc"><%@ include file="help/help_toc.jspf"%></div>
<div id="helpcontents">

<script>
  (function() {
    var regex = new RegExp(".*/help.*","gi");
    if (window.location.href.match(regex) !== null) {
      cx = '003403572499718465305:ikvddmxy6io';
      gcse = document.createElement('script');
      gcse.type = 'text/javascript';
      gcse.async = true;
      gcse.src = 'https://cse.google.com/cse.js?cx=' + cx;
      s = document.getElementsByTagName('script')[0];
      s.parentNode.insertBefore(gcse, s);
    }
  })();
</script>


<div id="helpSearch" style="width: 290px; float: right; ">
          <gcse:search></gcse:search>
        </div>

<!-- Encoded ds-help email -->
<input type="hidden" id="ds-help-email" value="<%=datashopHelpEmail %>" />

<%
  if (pageFragment != null && !pageFragment.equals("")) {
%>
      <jsp:include page="<%= pageFragment %>" />
<%
  } else if (pageDisplayTitle != null && pageDisplayTitle.equals("Page not found")){
%>
    <h3>The requested help page was not found. <br />Have you tried browsing
    the table of contents to the left?</h3>
    <p class="clearFloat"></p>
<%
  } else {
%>
    <h2>What is DataShop?</h2>

    <p>The LearnLab DataShop is a data repository and web
    application for learning science researchers. It provides secure data storage as well as an
    array of analysis and visualization tools available through a web-based interface.</p>

    <p>DataShop is currently funded in part by a National Science Foundation grant (OAC-1443068). Previously
    it was funded by National Science Foundation grants (SBE-0836012, SBE-0354420) to LearnLab.</p>

    <h3>Starting Points</h3>

    <ul class="concise">
        <li>If you're not familiar with the project or web application, see
        <a href="http://pslcdatashop.org/about/faq.html">our FAQ</a>.</li>
        <li>For the latest project news, visit <a href="http://pslcdatashop.org/about">our home
        page</a>.</li>
        <li>Browse our documentation using the table of contents to the left.</li>
        <li>Watch our <a href="http://www.youtube.com/user/datashoptutorials">tutorial videos on YouTube</a>.
        <li>Download the <a href="http://pslcdatashop.org/downloads/DataShopCheatSheet.pdf">DataShop Cheat Sheet (PDF)</a>.</li>
    </ul>

    <div id="format_buttons_div" class="clearfix">
        <div id="td_format_button_div" class="formatButton">
            <a id="td_format_button" type="submit" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
            role="button" aria-disabled="false" href="help?page=importFormatTd"><span class="ui-button-text">Import file format (tab-delimited)</span></a>
        </div>

        <div id="export_format_button_div" class="formatButton">
            <a id="export_format_button" type="submit" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only"
            role="button" aria-disabled="false" href="help?page=export#descriptions"><span class="ui-button-text">Export formats</span></a>
        </div>
    </div>

    <h3>An introduction to using DataShop for exploratory analysis</h3>
    <p style="margin-left: 22px"><iframe width="480" height="360" src="https://www.youtube.com/embed/qCR2mxcIb6M?rel=0" frameborder="0" allowfullscreen></iframe></p>
    <p>View more DataShop tutorials on <a href="http://www.youtube.com/user/datashoptutorials">YouTube</a></p>
    <!-- <p>The amount of data in DataShop is constantly growing. The majority of datasets contain
    tutor interaction data, while others are files-only datasets, used solely for central and secure
    file storage.</p> -->

    <p class="clearFloat"></p>
<%
  }
%>
    </div>
    </div></div></div>
    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf"%>

</table>

<!-- Hidden Form for Navigation -->
<form name="nav_helper_form" action="ErrorReport" method="post"><input
    type="hidden" name="unit_select" /> <input type="hidden"
    name="section_select" /> <input type="hidden" name="skill_select" />
<input type="hidden" name="problem_select" /></form>

</body>
</html>

