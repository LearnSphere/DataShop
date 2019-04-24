<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 15845 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2019-02-05 15:22:39 -0500 (Tue, 05 Feb 2019) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.dto.LearningCurveOptions"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillItem"%>
<%@page import="edu.cmu.pslc.datashop.item.StudentItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.export.ExportContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveServlet" %>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%@page buffer="100kb" %>

<!--  header section -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Learning Curve";
    jsIncludes.add("javascript/LCPID.js");
    jsIncludes.add("javascript/object/InlineEditor.js");
    jsIncludes.add("javascript/object/FileUploader.js");
    jsIncludes.add("javascript/LearningCurve.js");
    jsIncludes.add("javascript/KcModel.js");
    jsIncludes.add("javascript/sampleSelector.js");
    jsIncludes.add("javascript/sampleObject.js");
    jsIncludes.add("javascript/filterObject.js");
    jsIncludes.add("javascript/StepRollupPreview.js");
    jsIncludes.add("javascript/object/ProgressBar.js");
    jsIncludes.add("javascript/object/SkillModelList.js");
    jsIncludes.add("javascript/object/PopupMenu.js");
    jsIncludes.add("javascript/object/PageGrid.js");
    jsIncludes.add("javascript/object/SortHelper.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/ProblemContent.js");
    cssIncludes.add("learning_curve.css");
    cssIncludes.add("progress_bar.css");
    cssIncludes.add("styles.css");
    cssIncludes.add("lfaValues.css");
    cssIncludes.add("DataGrid.css");
    cssIncludes.add("kc_models.css");
    cssIncludes.add("inlineEditor.css");
    cssIncludes.add("ProblemContent.css");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    jsIncludes.add("javascript/ErrorReportCommon.js");   // for the tooltip 'Error Report' button
%>
<%@ include file="/header.jspf" %>

<!--  java section -->
<%
    if (navHelper == null) {
      navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    }
    boolean even = false;

    List skillList = new ArrayList();
    LearningCurveContext lcContext = info.getLearningCurveContext();
    ExportContext exportContext = info.getExportContext();

    String errorBarType = lcContext.getErrorBarType();

    String learningCurveType =
        (lcContext.getGraphType() == null)
            ? LearningCurveOptions.ERROR_RATE_TYPE
            : lcContext.getGraphType();

    String learningCurveView = lcContext.isViewBySkill()
             ? LearningCurveServlet.VIEW_BY_KC
             : LearningCurveServlet.VIEW_BY_STUDENT;

    int numSamples = navHelper.getSelectedSamples(info).size();

    Boolean viewErrorBars = lcContext.getDisplayErrorBars();
    Boolean viewPredicted = lcContext.getDisplayPredicted();
    Boolean includeHighStakes = lcContext.getIncludeHighStakes();
    Boolean showSkillModelValues = lcContext.getDisplayLFAModelValues();
    Boolean showSkillValues = lcContext.getDisplayLFASkillValues();
    Boolean showStudentValues = lcContext.getDisplayLFAStudentValues();
    Boolean classifyLCs = lcContext.getClassifyThumbnails();
    Integer studentThreshold = lcContext.getStudentThreshold();
    Integer opportunityThreshold = lcContext.getOpportunityThreshold();
    Double lowErrorThreshold = lcContext.getLowErrorThreshold();
    Double highErrorThreshold = lcContext.getHighErrorThreshold();
    Double afmSlopeThreshold = lcContext.getAfmSlopeThreshold();
    String disabledInputStr = "disabled";
    if (classifyLCs) { disabledInputStr = ""; }
    Boolean viewClassifiedDiv = true;
    String classifiedDisplayStr = "display:block";
    String includeHSDisplayStr = "display:block";
    if (!lcContext.isViewBySkill() || (numSamples > 1)) {
         classifiedDisplayStr = "display:none";
         viewClassifiedDiv = false;
    }

    Boolean exportSSIncludeAllKCs = exportContext.getStudentStepIncludeAllKCs() != null
            ? exportContext.getStudentStepIncludeAllKCs() : false;
    Boolean exportSSIncludeKCs = exportContext.getStudentStepIncludeKCs() != null
            ? exportContext.getStudentStepIncludeKCs() : false;
    Boolean exportSSIncludeNoKCs = exportContext.getStudentStepIncludeNoKCs() != null
            ? exportContext.getStudentStepIncludeNoKCs() : false;
    Boolean exportCachedVersion = exportContext.getUseCachedVersion() != null
            ? exportContext.getUseCachedVersion() : false;
    Boolean sampleSelected = navHelper.getSelectedSamples(info).size() == 0 ? false : true;

    // This page isn't reloaded by curveType changes so keep that check separate.
    // It will be checked in Javascript when curveType has changed.
    if (!learningCurveType.equals(LearningCurveOptions.ERROR_RATE_TYPE)) {
         classifiedDisplayStr = "display:none";
         includeHSDisplayStr = "display:none";
    }

    double stdDeviationCutoffValue =
        (lcContext.getStdDeviationCutoff() == null)
            ? 2.5
            : lcContext.getStdDeviationCutoff().doubleValue();

    //check if user is authorized to view selected dataset
    boolean authorized = false;

    Boolean highStakesDataPresent = false;

    skillList = navHelper.getSkills(info);
    if (datasetItem == null) {
        datasetItem = navHelper.getDataset(info);
    }

    if (datasetItem != null && datasetItem.getId() != null) {
        authorized = projHelper.isAuthorized(remoteUser, datasetItem.getId());
        highStakesDataPresent = navHelper.getHighStakesDataPresent(datasetItem);
    }

    // Regardless of curve-type, if high-stakes error rate data isn't present
    // in the dataset, don't display the checkbox offering to include...
    if (!highStakesDataPresent) {
         includeHSDisplayStr = "display:none";
    }

    //if not authorized go back to home page
    if (!authorized) {
        String redirectURL = "index.jsp";
        response.sendRedirect(redirectURL);
    }

    boolean userLoggedIn = (remoteUser == null) ? false : true;
%>

<!-- body section -->
<tr id="body">
<td id="nav">
<div id="navdiv">
    <div id="innerNavDiv">
    <%
        out.print(navHelper.displaySampleNav(true, info));
    %>

        <!-- for use by KC Models subtab -->
        <div id="kcModelsSamplesDiv">
             <div class="navigationBoxHeader">
             <h2>Samples</h2>
             </div>
             <span><p>KC model values are computed for all steps in this dataset with a KC label.</p></span>
             <br/>
        </div>

        <div id="learningCurveNav">
            <input type="hidden" id="viewClassifiedDiv" value="<%=viewClassifiedDiv %>" />
            <div class="navigationBoxHeader" id="lcNavBoxHeader"><h2>Learning Curve</h2></div>

            <h3>View By</h3>
            <form method=POST name="lc_view">
                <input type="radio" name="learning_curve_view" id="lc_view_kc"
                    value="view_by_kc" onClick="submit(); return true"
                    <%if (learningCurveView.compareTo("view_by_kc") == 0) { %><%=" checked"%><% }%> />
                <label for="lc_view_kc">Knowledge Component</label><br />

                <input type="radio" name="learning_curve_view" id="lc_view_student"
                    value="view_by_student" onClick="submit(); return true"
                    <% if (learningCurveView.compareTo("view_by_student") == 0) { %><%=" checked"%><% }%> />
                <label for="lc_view_student">Student</label>
              <input type="hidden" name="lc_selected_type" id="lc_selected_type" value="<%=learningCurveType%>"/>
            </form>

            <h3>Show</h3>
            <form method=POST name="view_predicted">
        <input type="checkbox" name="display_error_bars"
                      id="display_error_bars" value="display_error_bars" onClick="return viewErrorBars(this)"
                    <% if (viewErrorBars) { %><%=" checked"%><% }%> />
                <label id="display_error_bars_label" for="display_error_bars" >Error bars</label><br />
    <select id="select_error_bar_type" name="selectErrorBarType" title="Error bar type" onChange="return setErrorBarType(this)">
       <option value="<%= LearningCurveOptions.ERROR_BAR_TYPE_SE%>" title="Std Error" <% if (errorBarType.compareTo(LearningCurveOptions.ERROR_BAR_TYPE_SE) == 0) { %><%="selected"%><% }%>>Standard Error</option>
       <option value="<%= LearningCurveOptions.ERROR_BAR_TYPE_SD%>" title="Std Dev" <% if (errorBarType.compareTo(LearningCurveOptions.ERROR_BAR_TYPE_SD) == 0) { %><%="selected"%><% }%>>Standard Deviation</option>
    </select><br />

        <input type="checkbox" name="display_predicted" id="display_predicted" value="display_predicted" onClick="return viewPredicted(this)"
                    <% if (learningCurveType.compareTo("error_rate") != 0) { %><%=" disabled"%><% }%>
                    <% if (viewPredicted) { %><%=" checked"%><% }%> />
                <label id="display_predicted_label" for="display_predicted" class="<% if (learningCurveType.compareTo("error_rate") != 0) { %><%="disabled"%><% }%>" >Predicted learning curve</label><br />

        <div name="include_high_stakes" id="include_high_stakes_div" style="<%= includeHSDisplayStr %>">
             <table><tr>
             <td>
        <input type="checkbox" name="include_high_stakes" id="include_high_stakes" value="include_high_stakes" onClick="return includeHighStakes(this)"
                    <% if (learningCurveType.compareTo("error_rate") != 0) { %><%=" disabled"%><% }%>
                    <% if (includeHighStakes) { %><%=" checked"%><% }%> />
             </td>
             <td>
                <label id="include_high_stakes_label" for="include_high_stakes" class="<% if (learningCurveType.compareTo("error_rate") != 0) { %><%="disabled"%><% }%>" ><span>Opportunities include high stakes</span></label><br />
             </td>
             </tr></table>
        </div>

            </form>

            <form method="POST" name="maxOppsForm" id="maxOpp">
                <label>Opportunity Cutoff</label>
                <span><img src="images/information.png" alt="info" id="opp_cutoff_info"/></span>
                <div style="display: none" id="opp_cutoff_tooltip_content">A minimum and/or maximum number of opportunities a student must have
                had with a knowledge component for the student-knowledge component pair to be included in the data.</div>
                <br />
                <span class="oppLabel" >Min </span>
                <input type="text" name="minOpportunities" id="minOpportunities"
                value="<%if (lcContext.getMinOpportunityNumber() != null && lcContext.getMinOpportunityNumber().intValue() > 0) { out.print(lcContext.getMinOpportunityNumber());}%>" size="2" />
                <input type="button" name="minOppsClear" id="minOppsClear" value="Clear" onclick="return clearMinOpps()"   /><br />
                <span class="oppLabel" >Max </span>
                <input type="text" name="maxOpportunities" id="maxOpportunities"
                value="<%if (lcContext.getMaxOpportunityNumber() != null && lcContext.getMaxOpportunityNumber().intValue() > 0) { out.print(lcContext.getMaxOpportunityNumber());}%>" size="2" />
                <input type="button" name="maxOppsClear" id="maxOppsClear" value="Clear" onclick="return clearMaxOpps()"   /><br />

              <label>Std. Deviation Cutoff</label>
                <span><img src="images/information.png" alt="info" id="stdev_cutoff_info" /></span>
                <div style="display: none" id="stdev_cutoff_tooltip_content">The number of standard deviations above and below the mean for which to include data points.
                Data points (observations) falling outside the specified number of standard deviations are dropped from the graph.</div>
                <br />
                <input type="text" name="stdDevCutoff" id="stdDevCutoff" value="<%=stdDeviationCutoffValue%>" size="3" />
                <input type="button" name="stdDevClear" id="stdDevClear" value="Clear" onclick="return clearStdDevCutoff()"   /><br />
                <input type="submit" name="minOppsSet" id="minOppsSet" value="Refresh Graph" onclick="return validateOpps()" class="native-button" />
            </form>

            <div name="view_classified" id="classify_lcs_div" style="<%=classifiedDisplayStr %>">
                <table>
                <tr>
                <td>
                <input type="checkbox" name="classify_lcs"
           id="classify_lcs" value="classify_lcs" onClick="return toggleClassify(this)"
                <% if (classifyLCs) { %><%=" checked"%><% }%> />
                </td>
                <td style="vertical-align:middle">
                <label id="classify_lcs_label"
                       for="classify_lcs" >Categorize curves</label>
                       <img src="images/information.png" id="classify_lcs_info" />
                       <div style="display: none" id="classify_lcs_tooltip_content">
                       <strong>Learning Curve Categorization</strong><br/>
                       <p>Categorizes learning curves (KCs) into one of four categories.</p>
                       <p>The algorithm first discards points in each curve based on the <strong>student threshold</strong>.
                       If a point has fewer than that number of students, it is ignored. Within the points remaining:</li>
                       <ul>
                           <li>If the number of points is below the <strong>opportunity threshold</strong>,
                           then that curve has <strong>too little data</strong>.</li>
                           <li>If all points on the curve are beneath the <strong>low error threshold</strong>,
                           then the curve is <strong>low and flat</strong>.</li>
                           <li>If the slope of the predicted learning curve (as determined by the AFM algorithm) is below
                           the <strong>AFM slope threshold</strong>, then the curve shows <strong>no learning</strong>.</li>
                           <li>If the last point of the curve is above the <strong>high error threshold</strong>,
                           then the curve is <strong>still high</strong>.</li>
                       </ul>
                       </div>
                       <br />
                </td>
                </table>
                <div id="classify_thresholds_div">
                <table id="classify_thresholds">
                <tr>
                  <td class="col1">
                     <input type="text" name="student_threshold" id="student_threshold"
                            value="<%=studentThreshold %>" size="3" <%=disabledInputStr %> />
                 </td>
                  <td class="col2">
                     <label id="student_threshold_label"
                            for="student_threshold" >Student threshold</label>
                  </td>
                </tr>
                <tr>
                  <td class="col1">
                     <input type="text" name="opportunity_threshold" id="opportunity_threshold"
                            value="<%=opportunityThreshold %>" size="3" <%=disabledInputStr %> />
                  </td>
                  <td class="col2">
                     <label id="opportunity_threshold_label"
                            for="opportunity_threshold" >Opportunity threshold</label>
                  </td>
                </tr>
                <tr>
                  <td class="col1">
                     <input type="text" name="low_error_threshold" id="low_error_threshold"
                            value="<%=lowErrorThreshold %>" size="3" <%=disabledInputStr %> />
                  </td>
                  <td class="col2">
                     <label id="low_error_threshold_label"
                            for="low_error_threshold" >Low error threshold</label>
                  </td>
                </tr>
                <tr>
                  <td class="col1">
                     <input type="text" name="high_error_threshold" id="high_error_threshold"
                            value="<%=highErrorThreshold %>" size="3" <%=disabledInputStr %> />
                  </td>
                  <td class="col2">
                     <label id="high_error_threshold_label"
                            for="high_error_threshold" >High error threshold</label>
                  </td>
                </tr>
                <tr>
                  <td class="col1">
                     <input type="text" name="afm_slope_threshold" id="afm_slope_threshold"
                            value="<%=afmSlopeThreshold %>" size="3" <%=disabledInputStr %> />
                  </td>
                  <td class="col2">
                     <label id="afm_slope_threshold_label"
                            for="afm_slope_threshold" >AFM slope threshold</label>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                     <input type="submit" name="updateClassify" id="updateClassify"
                            value="Categorize" onclick="return classifyLCs()"
                            class="native-button" <%=disabledInputStr %> />
                  </td>
                </tr>
                </table>
                </div>
            </div>
        </div>

        <%
        if (!exportSSIncludeAllKCs && !exportSSIncludeKCs
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

        <div id="lfaValuesNav">
            <div class="navigationBoxHeader"><h2>Model Values</h2></div>

            <div id="modelValuesSamplesText">
                 <span><p>Model values are computed for all steps in this dataset with a KC label.</p></span>
                 <br/>
            </div>
        </div>

        <div id="skillModels"></div>
        <div id="skills"></div>
        <div id="students"></div>
        <div id="problems"></div> <!--to make this work, update java script:initLearningCurve -->
    </div> <!-- end innerNavDiv -->
</div>
</td>

<td id="content">
<div id="tabheader">
    <%=navHelper.displayTabs("Learning_Curve", info, authorized, user)%>
</div>
<div id="main">
    <div id="subtab"></div>
    <div id="contents">
          <%=contentHeader%>
          <div id="main_content_div"></div>

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


        <div id="learningCurves">
            <h1>&nbsp;</h1>
        </div>


    </div> <!--  end of contents div -->
</div>
</td></tr>

        <div id="lowAndFlatTooltipContent">Students likely received too much practice for these KCs.
        The low error rate shows that students mastered the KCs but continued to receive tasks for them.
        Consider reducing the required number of tasks or change your system's knowledge-tracing parameters
        (if it uses knowledge-tracing) so that students get fewer opportunities with these KCs.
        <br><br><strong>How it was calculated:</strong><br>
        After discarding points beneath the <strong>student threshold (<span class="value"><%=studentThreshold %></span>)</strong>, the number
        of remaining points is above the <strong>opportunity threshold (<span class="value"><%=opportunityThreshold %></span>)</strong> and all of those
        points are beneath the <strong>low error threshold (<span class="value"><%=lowErrorThreshold %></span>)</strong>.
        </div>
        <div id="noLearningTooltipContent">The slope of the predicted learning curve shows no apparent
        learning for these KCs. Explore whether the KC can be split into multiple KCs (via the KC Model
        export/import) that better account for variation in difficulty and transfer of learning that may
        be occurring across the tasks (problem steps) currently labeled by this KC.
        <br><br><strong>How it was calculated:</strong><br>
        After discarding points beneath the <strong>student threshold (<span class="value"><%=studentThreshold %></span>)</strong>, the remaining points
        did not cause the curve to fall into the categories of <em>too little data</em> or <em>low and flat</em>; however, the slope of the predicted
        learning curve (as determined by the AFM algorithm) is below the <strong>AFM slope threshold (<span class="value"><%=afmSlopeThreshold %></span>)</strong>
        </div>
        <div id="stillHighTooltipContent">Students continued to have difficulty with these KCs.
        Consider increasing opportunities for practice.
        <br><br><strong>How it was calculated:</strong><br>
        After discarding points beneath the <strong>student threshold (<span class="value"><%=studentThreshold %></span>)</strong>, the
        remaining points did not cause the curve to fall into the categories of <em>too little data</em>, <em>low and flat</em> or <em>no learning</em> and the last
        point in the resulting curve is above the <strong>high error threshold (<span class="value"><%=highErrorThreshold %></span>)</strong>.
        </div>
        <div id="tooLittleDataTooltipContent">Students didn't practice these KCs enough for the data to
        be interpretable. You might consider adding more tasks for these KCs or merging KCs (via the
        KC Model export/import).
        <br><br><strong>How it was calculated:</strong><br>
        After discarding points beneath the <strong>student threshold (<span class="value"><%=studentThreshold %></span>)</strong>, the
        number of remaining points is below the <strong>opportunity threshold (<span class="value"><%=opportunityThreshold %></span>)</strong>.
        </div>
        <div id="otherTooltipContent">These KCs did not fall into any of the above "bad" or "at risk" categories.
        Thus, these are "good" learning curves in the sense that they appear to indicate substantial student learning.</div>


<!-- footer section -->
<%@ include file="/footer.jspf" %>
</td></tr>
</table>

<div id="hiddenHelpContent" style="display:none"></div>
<div style="display:none">
<%@ include file="/help/report-level/learning-curve-line-graph.jspf" %>
<%@ include file="/help/report-level/student-step-rollup.jspf" %>
<%@ include file="/help/report-level/model-values.jspf" %>
<%@ include file="/help/report-level/kc-sets.jspf" %>
<%@ include file="/help/report-level/dataset-info-kc-models.jspf" %>
</div>

</body>
</html>

