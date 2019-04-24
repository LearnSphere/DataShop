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
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem" %>
<%@page import="edu.cmu.pslc.datashop.item.SkillModelItem" %>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.export.ExportContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldHelper"%>
<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Export";
    jsIncludes.add("javascript/sampleSelector.js");
    jsIncludes.add("javascript/sampleObject.js");
    jsIncludes.add("javascript/filterObject.js");
    jsIncludes.add("javascript/object/ProgressBar.js");
    jsIncludes.add("javascript/object/PageGrid.js");
    jsIncludes.add("javascript/object/SkillModelList.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/ProblemContent.js");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    cssIncludes.add("export.css");
    cssIncludes.add("progress_bar.css");
    cssIncludes.add("DataGrid.css");
    cssIncludes.add("ProblemContent.css");
    jsIncludes.add("javascript/exporter.js");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    jsIncludes.add("javascript/ErrorReportCommon.js");   // for the tooltip 'Error Report' button
%>




<!--  header -->
<%@ include file="/header.jspf" %>
<%
  boolean authorized = false;

    if (navHelper == null) {
        navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    }

    ExportContext exportContext = info.getExportContext();
    if (datasetItem == null) {
        datasetItem = navHelper.getDataset(info);
    }

    if (datasetItem != null && datasetItem.getId() != null) {
        authorized = projHelper.isAuthorized(remoteUser, datasetItem.getId());
    }

    if (!authorized) {
        String redirectURL = "index.jsp";
        response.sendRedirect(redirectURL);
    // If authorized user, then load custom field data for this dataset's tooltips
    } else if (datasetItem != null && datasetItem.getId() != null) {
        CustomFieldContext cfContext = CustomFieldContext.getContext(request);
        CustomFieldHelper cfHelper = HelperFactory.DEFAULT.getCustomFieldHelper();
        List<CustomFieldDto> cfList =
            cfHelper.getAllCustomFields((Integer)datasetItem.getId(),
                CustomFieldContext.getContext(request));

        if (cfList == null) {
            cfList = (List<CustomFieldDto>)request.getSession().
                    getAttribute(CustomFieldServlet.CF_LIST_ATTR + datasetId);
        }

        if (cfList != null && cfList.size() > 0) {
            for (Iterator iter = cfList.iterator(); iter.hasNext(); ) {
                 CustomFieldDto dto = (CustomFieldDto)iter.next();
                 //Map<String, Object> map = (Map<String, Object>)(dto.);
                 if (dto.getName() != null && !dto.getName().isEmpty()) {
                   String cfDomId = "cf_hash_" + ((String)dto.getName()).hashCode();
                     String cfToolTipText = "<p><strong>Name:</strong> " + dto.getName() + "</p>"
                         + (dto.getDescriptionToDisplay() != null ?
                             "<p><strong>Description:</strong> " + dto.getDescriptionToDisplay() + "</p>" : "")
                         + "<p><strong>Created by:</strong> " + dto.getOwnerString()
                         + " " + dto.getDateCreatedString() + "</p>"
                         + "<p><strong>Type:</strong> " + dto.getTypeString() + "</p>"
                         + (!dto.getUpdatedByString().isEmpty() ?
                             "<p><strong>Updated by:</strong> " + dto.getUpdatedByString() : "")
                         + " " + dto.getLastUpdatedString() + "</p>"
                         + "<p><strong>Rows with values:</strong> " + dto.getRowsWithValues() + "%</p>"
                         + "<p><strong>Level:</strong> " + dto.getLevel() + "</p>";
                 %>
                 <script type="text/javascript">
                     var customFieldInfoDiv = document.createElement('div');
                     customFieldInfoDiv.id   = "cf_tooltip_" + "<%=cfDomId %>";
                     customFieldInfoDiv.name = "cf_tooltip_" + "<%=cfDomId %>";
                     customFieldInfoDiv.setAttribute("style", "visibility: hidden;");
                     customFieldInfoDiv.innerHTML = "<%=cfToolTipText %>";
                     jQuery('body').append(customFieldInfoDiv);
                 </script>
                 <%
                 }
            }
        }
    }

    boolean userLoggedIn = (remoteUser == null) ? false : true;

    Boolean exportSPIncludeAllKCs = exportContext.getStudentProblemIncludeAllKCs() != null
        ? exportContext.getStudentProblemIncludeAllKCs() : false;
    Boolean exportSPIncludeKCs = exportContext.getStudentProblemIncludeKCs() != null
            ? exportContext.getStudentProblemIncludeKCs() : false;
    Boolean exportSPIncludeStepsWithoutKCs = exportContext.getStudentProblemIncludeUnmappedSteps() != null
            ? exportContext.getStudentProblemIncludeUnmappedSteps() : true;
    Boolean exportSPIncludeNoKCs = exportContext.getStudentProblemIncludeNoKCs() != null
            ? exportContext.getStudentProblemIncludeNoKCs() : false;
    Boolean exportCachedVersion = exportContext.getUseCachedVersion() != null
            ? exportContext.getUseCachedVersion() : false;

    Boolean exportSSIncludeAllKCs = exportContext.getStudentStepIncludeAllKCs() != null
            ? exportContext.getStudentStepIncludeAllKCs() : false;
    Boolean exportSSIncludeKCs = exportContext.getStudentStepIncludeKCs() != null
            ? exportContext.getStudentStepIncludeKCs() : false;
    Boolean exportSSIncludeNoKCs = exportContext.getStudentStepIncludeNoKCs() != null
            ? exportContext.getStudentStepIncludeNoKCs() : false;

    Boolean sampleSelected = navHelper.getSelectedSamples(info).size() == 0 ? false : true;

%>

<!-- body -->
<tr id="body"><td id="nav">

<div id="navdiv">
        <div id="innerNavDiv">
            <%=navHelper.displaySampleNav(true, info)%>
        <div id="txExportNav">
            <div class="navigationBoxHeader"><h2 class="nav_header">By Transaction</h2></div>
                <div id="exportOptionsWrapper">
                    <p>Transaction export contains information for all KC models.</p>
                </div>
        </div>

    <% if (!exportSPIncludeAllKCs && !exportSPIncludeKCs
               && !exportSPIncludeNoKCs) {
           exportSPIncludeAllKCs = true;
    }
    %>

        <div id="problemExportNav">
            <div class="navigationBoxHeader"><h2 class="nav_header">KC Models</h2>
            <span id="kcModelsLinkLink" title="Go to the KC Models page" class="wrench">
            <a href="javascript:goToKcModelsPage()">details</a></span></div>

            <div id="exportOptionsWrapper">
                <!--  Include all KCs. -->
                <input type="radio" name="problemExportNavRadio" id="exportIncludeAllKCs"
                    value="exportIncludeAllKCs"
                <% if (exportSPIncludeAllKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportIncludeAllKCs">All KC Models</label><br />

                <!--  Include selected KC(s). -->

                <input type="radio" name="problemExportNavRadio" id="exportIncludeKCs"
                <% if (exportSPIncludeKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportIncludeKCs">Primary</label>
                <div style="position: relative; top: 0px; left: 12px;">
                <div id="problemExportSkillNav">

                    <div id="problemExportSkillModel"></div>

                </div>
                <div id="includeStepsWithoutKCsCheckbox">
                    <!--  Include steps without KCs if a KC is selected. -->
                    <input type="checkbox" id="exportIncludeStepsWithoutKCs"
                    <% if (exportSPIncludeStepsWithoutKCs.booleanValue()) { out.print("checked"); } %> />
                    <label for="exportIncludeStepsWithoutKCs">Include Steps without KCs</label>

                </div>
                </div>

                <!--  Include all KCs. -->
                <input type="radio" name="problemExportNavRadio" id="exportIncludeNoKCs"
                    value="exportIncludeNoKCs"
                <% if (exportSPIncludeNoKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportIncludeNoKCs">No KC Models</label><br />

            </div>
        </div>
        <% if (!exportSSIncludeAllKCs && !exportSSIncludeKCs
                   && !exportSSIncludeNoKCs) {
               exportSSIncludeAllKCs = true;
        }
        %>
        <div id="stepExportNav">
            <div class="navigationBoxHeader"><h2 class="nav_header">KC Models</h2>
            <span id="kcModelsLinkLink" title="Go to the KC Models page" class="wrench">
            <a href="javascript:goToKcModelsPage()">details</a></span></div>

            <div id="exportOptionsWrapper">
                <!--  Include all KCs. -->
                <input type="radio" name="stepExportNavRadio" id="exportStepIncludeAllKCs"
                    value="exportStepIncludeAllKCs"
                <% if (exportSSIncludeAllKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportStepIncludeAllKCs">All KC Models</label><br />

                <!--  Include selected KC(s). -->

                <input type="radio" name="stepExportNavRadio" id="exportStepIncludeKCs"
                <% if (exportSSIncludeKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportStepIncludeKCs">Primary</label>
                <div style="position: relative; top: 0px; left: 12px;">
                <div id="stepExportSkillNav">
                    <div id="stepExportSkillModel"></div>
                </div>
                </div>

                <!--  Include all KCs. -->
                <input type="radio" name="stepExportNavRadio" id="exportStepIncludeNoKCs"
                    value="exportStepIncludeNoKCs"
                <% if (exportSSIncludeNoKCs.booleanValue()) { out.print("checked"); } %> />
                <label for="exportStepIncludeNoKCs">No KC Models</label><br />

            </div>
        </div>
        <div id="skillModels"></div>
        <div id="skills"></div>
        <div id="students"></div>
        <div id="problems"></div>
        </div>
</div>
</td>

<td id="content">
<div id="tabheader">
        <%=navHelper.displayTabs("Exporter", info, authorized, user)%>
</div>
<div id="main">
    <div id="subtab"></div>
    <div id="contents">
        <%=contentHeader%>

        <%if(sampleSelected) {%>
         <div id="sample_cached_file_info_div">
            <table id="sample_cached_file_info_table">
                <colgroup>
                    <col class="col1" />
                    <col class="col2" />
                    <col class="col3" />
                </colgroup>
                <thead>
                <tr id="sample_cached_file_info_table_header_row">
                    <th>Sample</th>
                    <th id="export_file_status_header">Export File Status</th>
                    <th>File Creation Date</th>
                </tr>
                </thead>
            </table>
        </div>
        <%}%>
        <div id="cached_export_selection_div">
            <h4>When exporting ...</h4>
            <input type="radio" name="exportCachedRadio" id="use_cached_version"
                value="true"
                <% if (exportCachedVersion.booleanValue()) { out.print("checked"); } %> />
                    <label for="use_cached_version">
                    Use cached version of selected samples (faster)
                    </label>
        <br/>
            <input type="radio" name="exportCachedRadio" id="use_uncached_version"
                value="false"
                <% if (!exportCachedVersion.booleanValue()) { out.print("checked"); } %> />
                    <label for="use_uncached_version">
                    Use selected options (possibly slower)
                    </label>
        </div>
        <div id="main_content_div">
            <h1>&nbsp;</h1>
        </div>
    </div>
</div>

</td></tr>
<!-- footer -->
<%@ include file="/footer.jspf" %>
</td></tr>
</table>

<div id="hiddenHelpContent" style="display:none"></div>
<div style="display:none">
<%@ include file="/help/report-level/export-tx.jspf" %>
<%@ include file="/help/report-level/export-problem.jspf" %>
<%@ include file="/help/report-level/export-step.jspf" %>
<%@ include file="/help/report-level/kc-sets.jspf" %>
</div>

<!-- Hidden Form for Navigation -->
<form name="nav_helper_form" action="ErrorReport" method="post">
        <input type="hidden" name="unit_select" />
        <input type="hidden" name="section_select" />
        <input type="hidden" name="skill_select" />
        <input type="hidden" name="problem_select" />
</form>
</body>
</html>
