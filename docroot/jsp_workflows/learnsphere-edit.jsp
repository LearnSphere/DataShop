<%@page
    import="edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem"%>
<%@ include file="/doctype.jspf"%>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2015
  All Rights Reserved
-->
<%
    // Author: Mike Komisin
    // Version: $Revision: 15357 $
    // Last modified by: $Author: pls21 $
    // Last modified on: $Date: 2018-07-18 14:53:59 -0400 (Wed, 18 Jul 2018) $
    // $KeyWordsOff: $
    //
    // NEW
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.FieldPosition"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dao.WorkflowComponentDao"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem "%>
<%@page import="edu.cmu.pslc.datashop.workflows.WorkflowComponentItem"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>
<%@page
    import="org.apache.commons.collections.comparators.NullComparator"%>
<%@page import="edu.cmu.pslc.datashop.servlet.workflows.WorkflowRowDto"%>
<%@page
    import="edu.cmu.pslc.datashop.servlet.workflows.WorkflowHistoryDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.workflows.WorkflowContext"%>
<%@page
    import="edu.cmu.pslc.datashop.servlet.workflows.WorkflowEditorServlet"%>
<%@page import="edu.cmu.pslc.datashop.workflows.WorkflowItem"%>
<%@page import="org.apache.commons.lang.WordUtils"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONException"%>
<%@page import="org.json.JSONObject"%>



<!-- header -->
<%@ include file="/header_variables.jspf"%>

<%
    pageTitle = "LearnSphere";

    /* Include common javascript libraries. */
    jsIncludes.add("javascript/lib/jquery-3.3.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-3.3.1.min.js");

    jsIncludes.add("javascript/lib/d3.js");
    jsIncludes.add("javascript/lib/cytoscape.min.js");

    jsIncludes.add("javascript/lib/Autolinker.js");

    /* Include workflow-specific javascript. */
    jsIncludes.add("javascript/workflows/jsPlumb-2.0.4.js");
    // Common workflow functions.
    jsIncludes.add("javascript/workflows/lsWorkflowCommon.js");
    // Workflow Manager functions.
    jsIncludes.add("javascript/workflows/lsWorkflowManager.js");
    // Workflow Tagging functions.
    jsIncludes.add("javascript/workflows/lsWorkflowTags.js");
    // Workflow Editor init.
    jsIncludes.add("javascript/workflows/lsInitWorkflowEditor.js");
    // Workflow Editor operations targetting components, like select component, cut/paste, ...
    jsIncludes.add("javascript/workflows/lsWorkflowEditorOps.js");
    // Workflow Editor Status Bar (top).
    jsIncludes.add("javascript/workflows/lsWorkflowStatusBar.js");
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
    jsIncludes.add("javascript/workflows/lsWorkflowListTree.js");
    // Workflow GLM related functions.
    jsIncludes.add("javascript/workflows/lsGLM.js");
    jsIncludes.add("javascript/workflows/combinations.js");
    // Workflow Custom Option Iterfaces
    jsIncludes.add("javascript/workflows/customOptions/customOptions.js");
    // Workflow annotation feature
    jsIncludes.add("javascript/workflows/workflowAnnotation.js");

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
    jsIncludes.add("javascript/jqwidgets/jqxgrid.columnsreorder.js");
    jsIncludes.add("javascript/jqwidgets/jqxgrid.sort.js");
    jsIncludes.add("javascript/jqwidgets/jqxlistbox.js");
    jsIncludes.add("javascript/jqwidgets/jqxdropdownlist.js");
    jsIncludes.add("javascript/jqwidgets/jqxcheckbox.js");
    jsIncludes.add("javascript/jqwidgets/jqxcalendar.js");
    jsIncludes.add("javascript/jqwidgets/jqxnumberinput.js");
    jsIncludes.add("javascript/jqwidgets/jqxdatetimeinput.js");
    jsIncludes.add("javascript/jQuery_enscroll/enscroll-0.6.2.min.js");
    jsIncludes.add("javascript/jqwidgets/globalization/globalize.js");
    jsIncludes.add("javascript/workflows/importPreview.js");
    jsIncludes.add("javascript/jstree-dist/jstree.js");
    cssIncludes.add("jqwidgets/jqx.base.css");
    cssIncludes.add("jqwidgets/jqx.energyblue.css");

    cssIncludes.add("jquery-ui-3.3.1.css");
    cssIncludes.add("jstree/themes/default/style.css");

    // Override some of the css in style.min.css and jquery
    cssIncludes.add("workflows/learnsphere.css");

    showWfShareIcon = true;
    String lsHeaderType = "ls-header";
%>

<%@ include file="/ls-header.jspf"%>
<%@ include file="/jsp_workflows/workflow-data-control.jspf"%>
<!-- code -->
<%
    FastDateFormat dateFormat = FastDateFormat.getInstance("MMM d, yyyy");
    WorkflowContext workflowContext = (WorkflowContext) request.getSession().getAttribute("workflowContext");
    Boolean canCreateWorkflow = (Boolean) request.getAttribute("canCreateWorkflow");
    if (canCreateWorkflow == null) {
        canCreateWorkflow = false;
    }

    String dsId = (String) request.getAttribute("datasetId");
    String sortBy = null;

    String displayNone = "style='display: none'";
    String styleStr = displayNone;
%>


<%
    if (dsId != null) {
%>
<input type="hidden" id="datasetId" value="<%=dsId%>" />
<%
    }
%>


<!-- body -->

<tr>
    <td id="main_td">

        <div id="main" class="detached">
            <div id="contents" class="container-fluid">

                <!-- The Workflow editor. -->
                <%
                if (request.getAttribute("wfResults") == null) {
                %>

                <div id="workflow-main" style="display: none">
                    <div id="workflow-content">
                        <div id="process-selector-div" class="contextArea">

                            <div id="component-lhs-search-div">
                                <input type="text" id="component-lhs-search-input"
                                    title="Filter by name or description" /> <span
                                    title="Clear text" id="component-lhs-search-clear-button"></span><br />
                                <span title="Uncheck this box to search descriptions and names"
                                    id="component-lhs-search-label"> <input type="checkbox"
                                    id="component-lhs-deep-search-checkbox" /> <label
                                    for="component-lhs-deep-search-checkbox">Search names
                                        only</label>
                                </span>
                            </div>
                            <br /> <span title="Expand or close all component folders"
                                id="expandCloseComponentTree" class="jstreeWidgetLabels">Expand
                                / Collapse</span> <span id="componentsHeader">Components</span>
                            <div id="component-tree-div"></div>
                            <div class="feedback">
                                <a>Feedback</a>
                            </div>

                        </div>
                        <div id="wfStatusBar"></div>
                        <div id="process-div" class="contextArea">
                            <div name="headerObject"
                                class="noSelect component-selection-header"
                                style="position: relative; left: 15px; top: 15px;">
                                <div id="workspaceTitle" class="workspaceTitle">Workspace</div>
                            </div>
                            <div id="wfDiagram"></div>
                        </div>
                        <div id="wfMessageBar">
                            <div id="wfMessageBarText" /></div>
                        </div>

                    </div>
                    <!-- End of workflow-main -->

                </div>
                <!-- End of workflows-page-div -->
                <%
                }
                %>


            </div>
            <!-- End #contents div -->
        </div> <!-- End #main div -->

    </td>
</tr>
<input type="hidden" id="adminUserFlag" value="<%=adminUserFlag%>" />

</table>

</body>
</html>
