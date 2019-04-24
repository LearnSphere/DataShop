<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 15493 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2018-09-27 10:05:44 -0400 (Thu, 27 Sep 2018) $
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
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldServlet"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Dataset Info";
    cssIncludes.add("datasetInfo.css");
    cssIncludes.add("inlineEditor.css");
    cssIncludes.add("progress_bar.css");
    cssIncludes.add("DataGrid.css");
    cssIncludes.add("kc_models.css");
    cssIncludes.add("custom_fields.css");
    cssIncludes.add("../jscalendar/calendar-blue.css");
    cssIncludes.add("ImportQueue.css");
    cssIncludes.add("access_requests.css");
    cssIncludes.add("samples.css");
    cssIncludes.add("ProblemContent.css");


    /* Include common javascript libraries. */

    jsIncludes.add("javascript/lib/jquery-1.7.1.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/lib/prototype.js");
    jsIncludes.add("javascript/lib/d3.js");

    /* Include workflow-specific javascript. */
    jsIncludes.add("javascript/workflows/jsPlumb-2.0.4.js");
    // Common workflow functions.
    jsIncludes.add("javascript/workflows/lsWorkflowCommon.js");
    // Workflow Manager functions.
    jsIncludes.add("javascript/workflows/lsWorkflowManager.js");
    // Workflow Component Menu (Left-hand side tree).
    jsIncludes.add("javascript/workflows/lsWorkflowComponentMenu.js");
    // Workflow editor functions (heavy with jsPlumb).
    jsIncludes.add("javascript/workflows/lsWorkflowEditor.js");
    // Workflow editor AJAX request and response handling functions.
    jsIncludes.add("javascript/workflows/lsWorkflowAuthoring.js");
    jsIncludes.add("javascript/workflows/lsWorkflowComponents.js");
    // Workflow results functions.
    jsIncludes.add("javascript/workflows/lsWorkflowResults.js");
     // Workflow search / display functions.
    jsIncludes.add("javascript/workflows/lsWorkflowList.js");

    // This is a placeholder for the jqwidgets until we implement
       // advanced import components capable of guessing/setting column types in tables.

    jsIncludes.add("javascript/jqwidgets/jqxcore.js");
    jsIncludes.add("javascript/jqwidgets/jqxdata.js");
    jsIncludes.add("javascript/jqwidgets/jqxbuttons.js");
    jsIncludes.add("javascript/jqwidgets/jqxscrollbar.js");
    jsIncludes.add("javascript/jqwidgets/jqxmenu.js");
    jsIncludes.add("javascript/jqwidgets/jqxgrid.js");
    jsIncludes.add("javascript/jqwidgets/jqxgrid.edit.js");
    jsIncludes.add("javascript/jqwidgets/jqxgrid.selection.js");
    jsIncludes.add("javascript/jqwidgets/jqxgrid.columnsresize.js");
    jsIncludes.add("javascript/jqwidgets/jqxlistbox.js");
    jsIncludes.add("javascript/jqwidgets/jqxdropdownlist.js");
    jsIncludes.add("javascript/jqwidgets/jqxcheckbox.js");
    jsIncludes.add("javascript/jqwidgets/jqxcalendar.js");
    jsIncludes.add("javascript/jqwidgets/jqxnumberinput.js");
    jsIncludes.add("javascript/jqwidgets/jqxdatetimeinput.js");
    jsIncludes.add("javascript/jqwidgets/globalization/globalize.js");
    jsIncludes.add("javascript/workflows/importPreview.js");
    cssIncludes.add("jqwidgets/jqx.base.css");
    cssIncludes.add("jqwidgets/jqx.energyblue.css");


    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("access_requests.css");
    jsIncludes.add("javascript/sampleSelector.js");
    jsIncludes.add("javascript/sampleObject.js");
    jsIncludes.add("javascript/filterObject.js");
    jsIncludes.add("javascript/object/Truncator.js");
    jsIncludes.add("javascript/object/InlineEditor.js");
    jsIncludes.add("javascript/object/FileUploader.js");
    jsIncludes.add("javascript/DatasetInfo.js");
    jsIncludes.add("javascript/KcModel.js");
    jsIncludes.add("javascript/CustomFields.js");
    jsIncludes.add("javascript/ProblemContent.js");
    jsIncludes.add("javascript/object/ProgressBar.js");
    jsIncludes.add("javascript/object/PageGrid.js");
    jsIncludes.add("jscalendar/calendar_stripped.js");
    jsIncludes.add("jscalendar/calendar-setup_stripped.js");
    jsIncludes.add("jscalendar/calendar-en.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/AccessRequests.js");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- get the nav helper and check authorization -->
<%
    info = (DatasetContext)session.getAttribute("datasetContext_" + datasetId);

    boolean projectAdminFlag = false;
    if (info != null) {
        projectAdminFlag = info.getAdminFlag();
    }

    boolean authorized = false;
    if (navHelper != null && info != null) {
        if (datasetItem != null && projHelper != null) {
            authorized = projHelper.isAuthorized(remoteUser, (Integer)datasetItem.getId());
        }
    }

    // Read attrs specific to 'Custom Fields' subtab
    String message =
        (String)session.getAttribute(CustomFieldServlet.CF_MESSAGE_ATTR + datasetId);
    String messageLevel =
        (String)session.getAttribute(CustomFieldServlet.CF_MESSAGE_LEVEL_ATTR + datasetId);

    // Null once read...
    request.getSession().setAttribute(CustomFieldServlet.CF_MESSAGE_ATTR + datasetId, null);
    request.getSession().setAttribute(CustomFieldServlet.CF_MESSAGE_LEVEL_ATTR + datasetId, null);

    boolean userLoggedIn = (remoteUser == null) ? false : true;

    String tabHeaderStr = "";
    if (info != null) {
        tabHeaderStr = navHelper.displayTabs("DatasetInfoReport", info, authorized, user);
    } else {
        tabHeaderStr = navHelper.displayTabs("DatasetInfoReport", (Integer)datasetItem.getId(),
                                             authorized, user);
    }
%>

<!-- body -->

<tr id="body"><td id="nav">

</td>

<td id="content" colspan="2">

<div id="tabheader"><%=tabHeaderStr %></div>

<div id="main">
    <!-- make note of attrs needed by javascript -->
    <input type="hidden" id="cfMessage" value="<%=message %>" />
    <input type="hidden" id="cfMessageLevel" value="<%=messageLevel %>" />
    <input type="hidden" id="userLoggedIn" value="<%=userLoggedIn %>" />
    <input type="hidden" id="userAuthorized" value="<%=authorized %>" />
    <input type="hidden" id="numTransactions" value="<%=numberOfTransactions %>" />
    <input type="hidden" id="isRemote" value="<%=isRemote %>" />
    <input type="hidden" id="projectAdminFlag" value="<%=projectAdminFlag %>" />
    <input type="hidden" id="adminUserFlag" value="<%=adminUserFlag %>" />

    <div id="subtab"></div>
    <div id="contents">
        <%=datasetNameString%>
        <%@ include file="/dataset_info_extra.jspf" %>
        <div id="main_content_div"></div>
    </div>
</div></td></tr>
<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="requestDialog" class="requestDialog"></div>

<div id="hiddenHelpContent" style="display:none"></div>
<div style="display:none">
<%@ include file="/help/report-level/dataset-info-overview.jspf" %>
<%@ include file="/help/report-level/dataset-info-samples.jspf" %>
<%@ include file="/help/report-level/dataset-info-kc-models.jspf" %>
<%@ include file="/help/report-level/dataset-info-custom-fields.jspf" %>
<%@ include file="/help/report-level/dataset-info-problem-list.jspf" %>
<%@ include file="/help/report-level/dataset-info-step-list.jspf" %>
<%@ include file="/help/report-level/dataset-info-citation.jspf" %>
<%@ include file="/help/report-level/dataset-info-terms.jspf" %>
<%@ include file="/help/report-level/dataset-info-problem-content.jspf" %>
</div>

</body>
</html>
