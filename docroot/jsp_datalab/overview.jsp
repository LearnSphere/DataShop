<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 12075 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-03-12 11:56:12 -0400 (Thu, 12 Mar 2015) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="org.apache.commons.lang.WordUtils"%>
<%@page import="edu.cmu.datalab.Constants"%>
<%@page import="edu.cmu.datalab.item.AnalysisItem"%>
<%@page import="edu.cmu.datalab.item.ValueId"%>

<%@page import="edu.cmu.datalab.servlet.DatalabContext"%>
<%@page import="edu.cmu.datalab.servlet.DatalabServlet"%>
<%@page import="edu.cmu.datalab.servlet.ItemDto"%>
<%@page import="edu.cmu.datalab.servlet.OverviewDto"%>
<%@page import="edu.cmu.datalab.servlet.StudentDto"%>
<%@page import="edu.cmu.datalab.servlet.ValueDto"%>

<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>

<%
    String liHomeClassStr = "";
    String li1ClassStr = "class=\"active\"";
    String li2ClassStr = "";

    String overallScoreStr = OverviewDto.OVERALL_SCORE_SORT_BY;

    String overallScoreInfoStr = overallScoreStr + " is a sum of the scores from the other columns. If this is not right, you can provide your own overall score in the data.";
    String summaryColInfoStr = " as the summary column. If this is not right, DataLab can provide an Overall Score, which is the sum of the scores from the other columns.";

    String difficultySortStr = "Difficulty sort puts the worst students at the top and the hardest items at the left.";
%>

<!--  header -->
<%@ include file="/jsp_datalab/header.jspf" %>

<%
    DatalabContext context = DatalabContext.getContext(request);
    Boolean displayAnonIds = context.getShowAnonStudentIds();
    Integer rowsPerPage = context.getRowsPerPage();
    Integer currentPage = context.getCurrentPage();
    Integer colsPerPage = context.getColsPerPage();
    Integer currentPanel = context.getCurrentPanel();
    String sortBy = context.getSortBy();
    Boolean isAscending = context.isAscending(sortBy);
    Boolean studentShading = context.getStudentShading();
    Integer studentScoreThreshold = context.getStudentScoreThreshold();

    // Row Id sort, ascending, is the same as Original Order sort.
    boolean originalOrderSort = false;
    if ((sortBy.equals(OverviewDto.ORIGINAL_ORDER_SORT_BY)) ||
        (sortBy.equals(OverviewDto.ROW_ID_SORT_BY) && isAscending)) {
        originalOrderSort = true;
    }
    String showAnonStr = "";
    if (displayAnonIds) { showAnonStr = "checked"; }
    String studentShadingStr = "checked";
    if (!studentShading) { studentShadingStr = ""; }

    int rowsPerPageOptions[] = {25, 50, 100};
    int colsPerPageOptions[] = {3, 5, 7, 10};
    
    OverviewDto dto = (OverviewDto)request.getAttribute(DatalabServlet.OVERVIEW_ATTR + analysisIdStr);
    AnalysisItem analysis = null;
    List<StudentDto> students = null;
    List<ItemDto> items = null;
    Map<ValueId, ValueDto> values = null;
    FileItem file = null;
    String fileName = "";
    Boolean summaryColumnPresent = false;
    ItemDto summaryItem = null;
    Map<ValueId, ValueDto> summaryValues = null;

    String warningMsg = (String)request.getAttribute(DatalabServlet.WARNING_MESSAGE_ATTR);
    String gettingStartedOption =
           (String)session.getAttribute(DatalabServlet.GETTING_STARTED_ATTR);

    // Clear session attr once read.
    session.setAttribute(DatalabServlet.GETTING_STARTED_ATTR, null);

    int numStudents = 0;
    int numItems = 0;
    int numPages = 0;
    int numPanels = 0;
    int firstPageIndex = 0;
    int lastPageIndex = 0;
    int firstPanelIndex = 0;
    int lastPanelIndex = 0;

    String message = null;
    String messageLevel = null;
    if (dto != null) {
        analysis = dto.getAnalysis();
        numStudents = analysis.getNumStudents();
        numItems = analysis.getNumItems();

        students = dto.getStudents();
        items = dto.getItems();
        values = dto.getValues();
        file = analysis.getFile();
        if (file != null) {
            fileName = file.getFileName();
        } else if (analysis != null) {
            fileName = analysis.getAnalysisName();
        }

        summaryColumnPresent = analysis.getSummaryColumnPresent();

        if (summaryColumnPresent) {
           summaryItem = dto.getSummaryItem();
           summaryValues = dto.getSummaryValues();

           // Remove the summaryItem from the list of items so it's not printed twice.
           items.remove(summaryItem);
        }
        
        message = dto.getMessage();
        messageLevel = dto.getMessageLevel();

        if (message == null) {
            // Check the session...
            OverviewDto tmpDto =
                (OverviewDto)session.getAttribute(DatalabServlet.OVERVIEW_ATTR + analysisIdStr);
            if (tmpDto != null) {
                message = tmpDto.getMessage();
                messageLevel = tmpDto.getMessageLevel();
            }
            
            // Clear session once read.
            session.setAttribute(DatalabServlet.OVERVIEW_ATTR + analysisIdStr, null);
        }

        // Determine total number of student pages and item 'panels'.
        numPages = (int)Math.ceil((float)numStudents / (float)rowsPerPage);
        numPanels = (int)Math.ceil((float)numItems / (float)colsPerPage);

    } else if (warningMsg != null) {
        message = warningMsg;
        messageLevel = DatalabServlet.STATUS_MESSAGE_LEVEL_WARN;
    }

    Map<String, DatalabServlet.GettingStartedInfo>
        gsInfoMap = DatalabServlet.GETTING_STARTED_INFO_MAP;
%>

<!-- make note of attrs needed by javascript -->
<input type="hidden" id="message" value="<%=message %>" />
<input type="hidden" id="messageLevel" value="<%=messageLevel %>" />

<% if (dto == null) { %>
<div class="outer-div">
<div id="drop-file-div" ondrop="dropFile(event)" ondragover="allowDrop(event)">
     <p class="drop-file-info"><%=dragFileText %></p>
</div>
<div id="upload-new-file-div">
     or <a href="javascript:openUploadDialog()">choose a file on your computer</a>
</div>

<div id="do-you-want-to-div">
     <div id="do-you-want-to-list">
     <p>Do you want to...</p>
     <ul>
     <li id="<%=DatalabServlet.GS_OPTION_CONTACT_STUDENTS %>"><a class="doYouWantToOption"><%= gsInfoMap.get(DatalabServlet.GS_OPTION_CONTACT_STUDENTS).getDoYouWantToStr() %></a></li>
     <li id="<%=DatalabServlet.GS_OPTION_IDENTIFY_ITEM %>"><a class="doYouWantToOption"><%= gsInfoMap.get(DatalabServlet.GS_OPTION_IDENTIFY_ITEM).getDoYouWantToStr() %></a></li>
     <li id="<%=DatalabServlet.GS_OPTION_ADDITIONAL_ANALYSIS %>"><a class="doYouWantToOption"><%= gsInfoMap.get(DatalabServlet.GS_OPTION_ADDITIONAL_ANALYSIS).getDoYouWantToStr() %></a></li>
     <li id="<%=DatalabServlet.GS_OPTION_COMPUTE_OVERALL_SCORE %>"><a class="doYouWantToOption"><%= gsInfoMap.get(DatalabServlet.GS_OPTION_COMPUTE_OVERALL_SCORE).getDoYouWantToStr() %></a></li>
     </ul>
     </div>

     <!-- initially hidden -->
     <div id="three-options-div">
          <ul>
          <li><a id="option-1" href="">Analyze my gradebook</a></li>
          <li><a id="option-2" href="">Analyze a sample gradebook</a></li>
          <li><a id="option-3" target="_blank" href="">See an example analysis video</a></li>
          </ul>
     </div>
</div>

</div>   <!-- outer-div -->
<% } %>

<div id="file-loading-div">
     <img id="file-loading-img" src="images/waiting.gif" />
     <br/><p class="drop-file-info"><%=loadingFileText %></p>
</div>

<%
  if (dto != null) {

      Long analysisId = 0L;
      try {
         analysisId = Long.valueOf(analysisIdStr);
      } catch (Exception nfe) {
         // ignore
      }

      // If user isn't logged in, offer the 'Save' button.
      if ((remoteUser == null) && (analysisId > DatalabServlet.MAX_SAMPLE_ANALYSIS_ID)) {
%>

<div id="save-analysis-div">
     <form action="DataLabLogin" method="post">
           <input name="Submit" value="Save" class="btn btn-sm btn-default"
                  type="submit" id="save-analysis-button">
     </form>
</div>

<%
      }   // if (remoteUser == null)
%>

<div class="outer-div">
<div id="upload-new-file-div">
     <a href="javascript:openUploadDialog()">Upload a new file</a>
</div>
<div id="file-name-div">
     <span class="dl-label"><%=fileName %></span>
</div>

<%
  if (students.size() > 0) {
%>

<input type="hidden" id="maxSampleAnalysisId" value="<%=DatalabServlet.MAX_SAMPLE_ANALYSIS_ID %>" />

<div id="table-div">
<div id="overview-table-div">

     <span class="dl-label">Overview</span>
     <div id="paging-options-table">
        <table id="pagingOptionsTable">
        <tr>
            <td class="display-anon-ids">
                <input type="checkbox" id="displayAnonIds" name="displayAnonIds" <%=showAnonStr %>/>
                <label for="displayAnonIds">Display Anonymous IDs only</label>
            </td>
        </tr>
        <tr>
            <td class="rows-per-page"><span class="rows_per_page">Students per page: </span>
            <select name="students_per_page" class="students_per_page">
                <%
                for (int rppOpt : rowsPerPageOptions) {
                    String selected = (rowsPerPage == rppOpt) ? "selected=\"selected\"" : "";
                %>
                    <option value="<%=rppOpt %>" <%=selected %>><%=rppOpt %></option>
                <%
                }
                %>
            </select>
            </td>
            <td>
                <table id="pageCountTable">
                    <tr>
                    <% if (currentPage > 1 && numPages > 1) { %>
                       <td><a href="javascript:gotoStudent(1)"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoStudent(<%=currentPage - 1%>)"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                    <% } else {%>
                       <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                       <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                    <% } %>

                    <% if (currentPage < numPages && numPages > 1) { %>
                       <td><a href="javascript:gotoStudent(<%=currentPage + 1%>)">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoStudent(<%=numPages%>)">Last <img src="images/grid/forward_grey.gif" /></a></td>
                    <% } else {%>
                       <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                       <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                    <% } %>
                    </tr>
                </table> <!-- pageCountTable  -->
            </td>
            <%
                firstPageIndex = (currentPage - 1) * rowsPerPage;
                lastPageIndex = numStudents;
                if (numStudents > firstPageIndex + rowsPerPage) { lastPageIndex = firstPageIndex + rowsPerPage; }
                if (numStudents == 0) { firstPageIndex = -1; }  // special case; empty table
            %>
            <td class="row-range"><%=firstPageIndex + 1 %>-<%=lastPageIndex %> of <%=numStudents %></td>
        </tr>
        </table>   <!-- pagingOptionsTable  -->
     </div>

     <div id="sortDropdown_<%=analysisIdStr %>" class="sortDropdown">
          <a id="sortAnchor_<%=analysisIdStr %>" class="sortAnchor">
             <span>Sort</span>
          </a>
          <div id="sortSubmenu_<%=analysisIdStr %>" class="sortSubmenu">
             <ul class="sortUl">
                 <li><input type="radio" name="original_sort" id="original_sort_link"
                            value="<%=OverviewDto.ORIGINAL_ORDER_SORT_BY %>"
                            <% if (originalOrderSort) {
                                   out.print("checked");
                               }
                            %>/>
                     <label for="original_sort_link">Original order</label>
                 </li>

                 <li><input type="radio" name="difficulty_sort" id="difficulty_sort_link"
                            value="<%=OverviewDto.DIFFICULTY_SORT_BY %>"
                            <% if (sortBy.equals(OverviewDto.DIFFICULTY_SORT_BY)) {
                                   out.print("checked");
                               }
                            %>/>
                     <label for="difficulty_sort_link">Difficulty sort
                     <img title="<%=difficultySortStr %>"
                          alt="Difficulty sort" src="images/info.gif" />
                     </label>
                 </li>

                 <!-- User can't actually choose 'custom sort'. It is just an
                      indication of whether or not the table is sorted by a column. -->
                 <li><input type="radio" name="custom_sort" id="custom_sort_link"
                            value="<%=sortBy %>" disabled  
                            <% if (!sortBy.equals(OverviewDto.ORIGINAL_ORDER_SORT_BY) &&
                                  (!sortBy.equals(OverviewDto.DIFFICULTY_SORT_BY)) &&
                                  (!originalOrderSort)) {
                                   out.print("checked");
                               }
                            %>/>
                     <label for="custom_sort_link">Custom sort</label>
                 </li>
             </ul>
          </div>
     </div>  <!-- sortDropdown -->

     <%
        // Row Id sort, ascending, is the same as Original Order sort.
        // Make sure we show arrow in 'Row' column if using Original Order sort.
        String rowColSortImg = OverviewDto.getSortImage(sortBy,
                                                        OverviewDto.ROW_ID_SORT_BY, isAscending);
        if (originalOrderSort) {
            rowColSortImg = "images/grid/up.gif";
        }
     %>
     <table id="overviewTable" class="cell-border compact">
     <thead>
     <tr>
         <th class="dl-table-header">
             <a href="javascript:sortOverview('<%=OverviewDto.ROW_ID_SORT_BY %>')">Row</a>
             <img src="<%=rowColSortImg %>"/>
         </th>
         <th class="dl-table-header fixed-col-left">
             <a href="javascript:sortOverview('<%=OverviewDto.STUDENT_SORT_BY %>')"><%=OverviewDto.STUDENT_SORT_BY %></a>
             <img src="<%=OverviewDto.getSortImage(sortBy,
                                                   OverviewDto.STUDENT_SORT_BY, isAscending) %>"/>
         </th>

         <%
           for (ItemDto i : items) {
               String itemName = i.getItemName();
         %>
         <th class="dl-table-header">
             <a href="javascript:sortOverview('<%=itemName %>')"><%=itemName %></a>
             <img src="<%=OverviewDto.getSortImage(sortBy, itemName, isAscending) %>"/>
         </th>
         <%
           }
         %>
         <% if (!summaryColumnPresent) { %>
         <th class="dl-table-header fixed-col-right">
             <a href="javascript:sortOverview('<%=OverviewDto.OVERALL_SCORE_SORT_BY %>')"><%=OverviewDto.OVERALL_SCORE_SORT_BY %></a>
             <img src="<%=OverviewDto.getSortImage(sortBy,
                                                   OverviewDto.OVERALL_SCORE_SORT_BY,
                                                   isAscending) %>"/>
             <img title="<%=overallScoreInfoStr %>" alt="<%=overallScoreStr %>"
                  src="images/info.gif" />
         </th>
         <% } else {
            String summaryItemName = summaryItem.getItemName();
         %>
         <th class="dl-table-header fixed-col-right">
             <a href="javascript:sortOverview('<%=summaryItemName %>')"><%=summaryItemName %></a>
             <img src="<%=OverviewDto.getSortImage(sortBy, summaryItemName, isAscending) %>"/>
             <img title="Using <%=summaryItemName %><%=summaryColInfoStr %>" alt="<%=summaryItemName %>" src="images/info.gif" />
         </th>
         <% } %>
     </tr>
     </thead>
     <tbody>
     <%
       for (StudentDto s : students) {
           Long studentId = (Long)s.getStudent().getId();
           String studentStr = displayAnonIds ? s.getAnonStudentId() : s.getStudentName();
     %>
     <tr>
         <td><%=s.getStudentIndex() %></td>
         <td id="td_student_display_<%=studentId %>" class="<%=s.getShading() %>"
             style="border-right-color:black"><%=studentStr %></td>
         <%
           for (ItemDto i : items) {
               ValueId vid = new ValueId(s.getStudent(), i.getItem());
               ValueDto v = values.get(vid);
               if (v == null) { 
         %>
                   <td class="shading-grey"></td>
             <% } else { %>
                   <td class="<%=v.getShading() %>"><%=v.getValueStr() %></td>
             <% } %>
         <% } %>

         <% if (!summaryColumnPresent) { %>
                <td style="border-left-color:black"><%=s.getComputedOverallScore() %></td>
         <%
            } else {
                ValueId vid = new ValueId(s.getStudent(), summaryItem.getItem());
                ValueDto v = summaryValues.get(vid);
                if (v == null) {
         %>
                   <td class="shading-grey" style="border-left-color:black"></td>
             <% } else { %>
                   <td class="<%=v.getShading() %>" style="border-left-color:black">
                       <%=v.getValueStr() %>
                   </td>
             <% } %>
         <% } %>
         </tr>
       <%
       }
       %>
     </tbody>
     <tfoot>
     <tr>
        <td class="empty-col"></td>
        <td class="fixed-col-left">Column Average</td>
        <%
           for (ItemDto i : items) {
        %>
               <td><%=i.getAverage() %></td>
        <%
           }
        %>
        <td class="fixed-col-right no-bottom-cell"></td>
     </tr>
     <tr>
        <td class="empty-col"></td>
        <td class="fixed-col-left">Standard Deviation</td>
        <%
           for (ItemDto i : items) {
        %>
               <td><%=i.getStdDeviation() %></td>
        <%
           }
        %>
        <td class="fixed-col-right"></td>
     </tr>
     </tfoot>
     </table>

</div>
</div>
<%
  }
%>

<div id="overview-info-div">
     <span>Found <%=analysis.getNumStudents() %> students and <%=analysis.getNumItems() %> items.
     </span>
     <a href="javascript:clearOverviewInfo()">
        <img title="Close" alt="Close" src="images/cross.png" />
     </a>
     <br />
     <span>Missing entries are ignored for Column Average.</span>
</div>

<%
        String otherAnalysisType = AnalysisItem.TYPE_ITEM;
        if (analysis.getAnalysisType().equals(AnalysisItem.TYPE_ITEM)) {
            otherAnalysisType = AnalysisItem.TYPE_GRADEBOOK;
        }
        otherAnalysisType = WordUtils.capitalize(otherAnalysisType);

        String changeTooltipStr = "Change to analyze as " + otherAnalysisType + " data.\n"
               + "Item data is assumed to have values of either 0 or 1\n"
               + "and indicates the result of a single question. A different\n"
               + "correlation analysis is performed for Item data.";
%>
<div id="overview-display-options">
     <div id="analysis-type-div">
          <span title="<%=changeTooltipStr %>">Analyzing as <%=WordUtils.capitalize(analysis.getAnalysisType()) %> data (<a href="javascript:changeAnalysisType()">change</a>)</span>
     </div>
     <div id="shading-and-summary-info-div">
     <table id="shading-and-summary-info-table">
     <tr>
     <td class="cell-one">
     <div id="student-shading-div">
          <input type="checkbox" name="studentShadingInput"
                 id="studentShadingInput" <%=studentShadingStr %>/>
          <% if (!summaryColumnPresent) { %>
          <label for="studentShadingInput">Shade students based on <%=overallScoreStr %></label>
          <br/>
          <span><%=overallScoreStr %> below </span>
          <input type="textarea" id="student-score-threshold-input"
                 value="<%=studentScoreThreshold %>">
          </input>
          <span>% where <%=analysis.getOverallMax() %> is the Overall Max</span>
          <% } else { %>
          <label for="studentShadingInput">Shade students based on <%=summaryItem.getItemName() %></label>
          <br/>
          <span><%=summaryItem.getItemName() %> below </span>
          <input type="textarea" id="student-score-threshold-input"
                 value="<%=studentScoreThreshold %>">
          </input>
          <!-- summary column is present so use it's max -->
          <span>% where <%=analysis.getMaxScoreGiven() %> is the max possible</span>
          <% } %>

          <% if (studentShading) { %>
             <br/>
             <span>There are <%=dto.getNumStrugglingStudents() %> struggling students.</span>
             <br/>
             <a href="javascript:openEmailDialog()">Email struggling students</a>
          <% } %>
     </div>
     </td>
     <td class="cell-two">
     <div id="summary-column-info-div">
          <p>Summary Column</p>
          <table id="summary-column-table">
              <tr><td>
              <input type="radio" name="summary_column" id="summary_computed_link"
                     value="<%=DatalabServlet.SUMMARY_COMPUTED %>"
              <% if (!summaryColumnPresent) { out.print("checked"); } %>
              />
              <label for="summary_computed_link">Compute an <b><%=overallScoreStr %></b> for me</label>
              </td></tr>
              <tr><td>
              <input type="radio" name="summary_column" id="summary_provided_link"
                     value="<%=DatalabServlet.SUMMARY_PROVIDED %>"
              <% if (summaryColumnPresent) { out.print("checked"); } %>
              />
              <label for="summary_provided_link">Use the last column I've provided</label>
              </td></tr>
          </table>
          <% if (!summaryColumnPresent) { %>
             <span><%=overallScoreInfoStr %></span>
          <% } else { %>
             <span>Using <b><%=summaryItem.getItemName() %></b><%=summaryColInfoStr %></span>
          <% } %>
     </div>
     </td>
     <td class="empty-cell"></td>
     </tr>
     </table>
     </div>
</div>

</div>  <!-- outer-div -->
<%
  }    // dto != null
%>

<%
    // Look at gettingStartedOption... if 2, need to open email dialog.
    // Other options are handled in the servlet.
    if ((gettingStartedOption != null) &&
        (gettingStartedOption.equals(DatalabServlet.GS_OPTION_CONTACT_STUDENTS))) {
%>
        <script type="text/javascript">openEmailDialog();</script>
<%      
    }
%>

</div>    <!-- datalab-content-wrapper-div -->
</div>    <!-- datalab-content-div -->

<div style="clear:both;"></div>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="addAnalysisDialog" class="addAnalysisDialog"></div>
<div id="editItemDialog" class="editItemDialog"></div>
<div id="emailStudentsDialog" class="emailStudentsDialog"></div>
  
</html>