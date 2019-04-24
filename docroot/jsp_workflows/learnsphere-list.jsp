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
    import="edu.cmu.pslc.datashop.servlet.workflows.LearnSphereServlet"%>
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
    // Workflow Manager functions.
    jsIncludes.add("javascript/workflows/lsInitWorkflowList.js");
    // Workflow Component Menu (Left-hand side tree).
    jsIncludes.add("javascript/workflows/lsWorkflowComponentMenu.js");
    // Workflow editor functions (heavy with jsPlumb).
    jsIncludes.add("javascript/workflows/lsWorkflowEditor.js");
    // Workflow Editor Status Bar (top) used here for linking papers/datasets via workflow list.
    jsIncludes.add("javascript/workflows/lsWorkflowStatusBar.js");
    // Workflow editor AJAX request and response handling functions.
    jsIncludes.add("javascript/workflows/lsWorkflowAuthoring.js");
    jsIncludes.add("javascript/workflows/lsWorkflowComponents.js");
    // Workflow results functions.
    jsIncludes.add("javascript/workflows/lsWorkflowResults.js");
    // Workflow search / display functions.
    jsIncludes.add("javascript/workflows/lsWorkflowSearch.js");
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

    String datasetName = (String) request.getAttribute("datasetName");
    String sortBy = null;
    String searchBy = "";
    Boolean showRequest = true;
    Boolean showTemplates = true;
    Boolean showShared = true;
    Boolean hasPapers = false;
    String searchByAuthor = "";
    String searchByComponent = "";
    String searchByDateLower = "";
    String searchByDateUpper = "";
    String searchByTags = "";
    String loadPanel = "my-workflows-panel";

    //Sort the workflow page rows.
    if (workflowContext != null) {

        // Variable sortBy is used below.
        sortBy = (String) workflowContext.getMySortBy();
        Boolean isAscending = workflowContext.getMySortOrder(sortBy);

        String lastPanel = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_PANEL);

        if (lastPanel != null && !lastPanel.isEmpty()) {
            loadPanel = lastPanel;
            %><script type="text/javascript">
            jQuery('body').data("loadPanel", "<%=loadPanel.toString() %>");
            </script><%
        }

        String myFolders = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_MY_FOLDERS);
        if (myFolders != null && !myFolders.isEmpty()) {
            %><script type="text/javascript">
            jQuery('body').data("<%= WorkflowContext.WF_MY_FOLDERS %>", "<%=myFolders.toString() %>");
            </script><%
        }


        searchBy = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_BY);
        searchByAuthor = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_AUTHOR);
        searchByComponent = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_COMPONENT);
        searchByDateLower = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_DATE_LOWER);
        searchByDateUpper = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_DATE_UPPER);
        searchByTags = (String) workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_TAGS);

        if (!workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_REQUEST).isEmpty()) {
            showRequest = workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_REQUEST).equalsIgnoreCase(WorkflowContext.WF_SEARCH_ACCESS_REQUEST);
        }
        if (!workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_NO_AUTH).isEmpty()) {
            showTemplates = workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_NO_AUTH).equalsIgnoreCase("true");
        }
        if (!workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_SHARED).isEmpty()) {
            showShared = workflowContext.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_SHARED).equalsIgnoreCase("true");
        }
        if (!workflowContext.getSearchAttribute(WorkflowContext.WF_HAS_PAPERS).isEmpty()) {
            hasPapers = workflowContext.getSearchAttribute(WorkflowContext.WF_HAS_PAPERS).equalsIgnoreCase("true");
        }
    }

    String displayNone = "style='display: none'";
    String styleStr = displayNone;
%>




<!-- body -->

<tr>
    <td id="main_td">

        <div id="main" class="detached">
            <div id="contents" class="container-fluid">

                <%
                    if (request.getAttribute("wfResults") == null) {
                %>
                <div class="wfAdvancedSearchDiv">
                    <%
                        if (adminUserFlag) {
                    %>
                    <div class="wfSearchElement wfSearchElementSpacing">
                        <a href="#" class="refreshWorkflowComponentDefinitions">Refresh
                            Component Definitions</a>
                    </div>
                    <%
                        }
                    %>

                    <div class="wfSearchElement wfSearchElementSpacing">
                        <%
                            if (canCreateWorkflow) {
                        %>
                        <div id="createNewWorkflowButton"></div>
                        <div id="spacerDiv">
                            <img src="images/trans_spacer.gif"
                                onLoad="initCreateButton(true)" />
                        </div>
                        <%
                            } else if (!canCreateWorkflow) {
                        %>
                        <div id="createNewWorkflowButton"></div>
                        <div id="spacerDiv">
                            <img src="images/trans_spacer.gif"
                                onLoad="initCreateButton(false)" />
                        </div>
                        <%
                            }
                        %>
                    </div>

                    <div id="wf-search" class="wfSearchElement ">
                        <div class="searchInputDiv">
                            <%
                                if (searchBy == null || searchBy.trim().equals("")) {
                            %>
                            <input type="text" id="wf_search_by" name="wf_search_by"
                                title="Quick Search..." />
                            <%
                                } else {
                            %>
                            <input type="text" id="wf_search_by" name="wf_search_by"
                                value="<%=searchBy%>" />
                            <%
                                }
                            %>
                        </div>
                    </div>

                    <div id="wf-advanced-search-header" class="wfSearchElement">
                        <div class="wfSearchHeader">Advanced Search</div>
                        <div class="table-break"></div>
                    </div>

                    <!-- <div id="wfSearchAccess" class="wfSearchElement">

                            /*String requestCheckedStr = showRequest ? "checked=\"checked\"" : "";
                            String templatesCheckedStr = showTemplates ? "checked=\"checked\"" : "";
                            String sharedCheckedStr = showShared ? "checked=\"checked\"" : "";*/


                        <div>By Data Access:</div>

                        <div id="searchFilters">
                            <div class="searchCheckbox"
                                title="These workflows contain data that you already have access to">
                                <input type="checkbox"
                                    id="wf_search_access_shared" sharedCheckedStr /> <label
                                    id="searchSharedLabel" for="wf_search_access_shared">Shared</label>
                            </div>
                            <div class="searchCheckbox"
                                title="These are workflows that contain private data but can be used as templates with your own data.">
                                <input type="checkbox"
                                    id="wf_search_access_no_auth" templatesCheckedStr /> <label
                                    id="searchTemplateLabel" for="wf_search_access_no_auth">Templates</label>
                            </div>
                        </div>
                    </div> -->
                    <%
                        String hasPapersChecked = hasPapers ? "checked=\"checked\"" : "";
                    %>
                    <div id="wfSearchPapers" class="wfSearchPaperDiv">
                        <div id="searchPapers">
                            <div class="searchPaperCheckbox"
                                title="These workflows are linked to academic papers">
                                <input type="checkbox"
                                    id="wf_has_papers" <%=hasPapersChecked %> /> <label
                                    id="hasPapers" for="wf_has_papers">Papers attached</label>
                            </div>
                        </div>

                    </div>
                    <div id="wfSearchByAuthor" class="wfSearchElement">
                        <div class="searchTextfield wfSearchElementSpacing">
                            <span>By Author:</span>
                            <div class="searchInputDiv">
                                <input type="text" id="wf_search_author"
                                    value="<%=searchByAuthor%>" disabled="true" />
                            </div>
                        </div>
                        <div class="searchTextfield wfSearchElementSpacing">
                            <span>By Component:</span>
                            <div class="searchInputDiv">
                                <input type="text" id="wf_search_component"
                                    value="<%=searchByComponent%>" />
                            </div>
                        </div>
                        <div class="searchTextfield wfSearchElementSpacing">
                            <span>By Date:</span>
                            <div class="searchInputDiv dateRangeInputDiv">
                                <input type="text" id="wf_search_date_lower"
                                    value="<%=searchByDateLower%>" /> -
                                <input type="text" id="wf_search_date_upper"
                                    value="<%=searchByDateUpper%>" />
                            </div>
                        </div>
                        <div class="searchTextfield wfSearchElementSpacing">
                            <span>By Tag:</span>
                            <div class="searchInputDiv searchTagDiv">
                            </div>
                        </div>
                        <script>
                            var workflowContextTagsString = "<%=searchByTags%>";
                        </script>
                    </div>

                    <div id="wf-advanced-tags-header" class="wfPaddedLeft">
                        <div class="searchInputDiv">
                                <div id="wfClearSearch"></div>
                                <div id="wfSearch"></div>
                        </div>
                    </div>
<!-- TAGS PLACEHOLDER
                    <div id="wf-advanced-tags-header" class="wfSearchElement">
                        <div class="wfSearchHeader">Filter Tags</div>
                        <div class="table-break"></div>
                        <div id="wf-tag-div">
                            <div>#Buck Rogers</div>
                            <div>#Flash Gordon</div>
                            <div>#Robin Williams</div>
                        </div>
                    </div>
 -->
                </div>
                <%
                }
                %>


                <!-- The Workflow editor. -->
                <%
                if (request.getAttribute("wfResults") == null) {
                %>

                <div id="workflow-main" style="display: none">
                    <div id="workflow-content">


                    </div>
                    <!-- End of workflow-main -->

                </div>
                <!-- End of workflows-page-div -->
                <%
                }
                %>


                <!-- The Workflow list. -->
                <%
                if (request.getAttribute("wfResults") == null
                    && request.getAttribute("wfState") == null) {
                %>
                <% if (datasetName != null) { %>
                <div id="selectedDatasetDiv">Filter by dataset: <%=datasetName %>
                  <span id="dsFilterSpan" class="removeSearchFilter" name="wf_search_dataset">
                  <img src="css/images/close.svg" width="22px" height="22px" />
                  </span></div>
                <% } %>

                <div class="workflowListTabs">
                    <div id="wfTabDiv">
                        <ul>
                            <li><a href="#my-workflows-panel">MY WORKFLOWS
                              <span class="wfCountSpan" id="my-workflows-count" ></span>
                            </a></li>
                            <li><a href="#recommended-workflows-panel">RECOMMENDED
                              <span class="wfCountSpan" id="recommended-workflows-count" ></span>
                            </a></li>
                            <li><a href="#shared-workflows-panel">PUBLIC
                              <span class="wfCountSpan" id="shared-workflows-count" ></span>
                            </a></li>
                        </ul>
                        <div class="pageDiv"></div>
                        <div class="wfCreateFolderDiv"></div>
                        <div class="wfLegend"><span>* = workflow is public</span></div>

                        <div id="my-workflows-panel" class="workflowTabDiv">

                        </div>
                        <div id="shared-workflows-panel" class="workflowTabDiv">

                        </div>
                        <div id="recommended-workflows-panel" class="workflowTabDiv">

                        </div>
                        <div class="pageDiv"></div>
                    </div>
                </div>

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
