<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 11151 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-06-10 14:59:28 -0400 (Tue, 10 Jun 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.ProblemItem"%>

<%

DatasetContext datasetContext = (DatasetContext)session.getAttribute("datasetContext_"
               + request.getParameter("datasetId"));
Integer datasetId = (Integer)datasetContext.getDataset().getId();

int rowsPerPagesOptions[] = {10, 25, 50, 100};
String pcOptions[] = { ProblemListDto.PROBLEM_CONTENT_BOTH,
                       ProblemListDto.PROBLEM_CONTENT_MAPPED,
                       ProblemListDto.PROBLEM_CONTENT_UNMAPPED };
ProblemListContext plContext = datasetContext.getProblemListContext();
String searchBy = plContext.getSearchBy();
Integer rowsPerPage = plContext.getRowsPerPage();
Integer currentPage = plContext.getCurrentPage();
String pcOption = plContext.getProblemContent();

ProblemListDto problemListDto =
    (ProblemListDto)request.getAttribute(ProblemListServlet.PROBLEM_LIST_ATTR + datasetId);

String message = problemListDto.getMessage();
String messageLevel = problemListDto.getMessageLevel();
List<String> hierarchyList = problemListDto.getHierarchyList();
Map<String, List<ProblemItem>> problemMap = problemListDto.getProblemMap();
Integer numHierarchies = problemListDto.getNumProblemHierarchies();
Integer numHierarchiesTotal = problemListDto.getNumProblemHierarchiesTotal();
Long numProblemsTotal = problemListDto.getNumProblemsTotal();
Integer numPages = problemListDto.getNumPages();
Boolean downloadEnabled = problemListDto.getDownloadEnabled();

String downloadButtonClassStr = "ui-state-default ui-corner-all request_link";
if ((downloadEnabled == null) || (!downloadEnabled)) {
    downloadButtonClassStr = "dead_link ui-state-disabled ui-state-default ui-corner-all";
}

// After form submissions, the info is on the session.
if ((message == null) && (messageLevel == null)) {
   ProblemListDto tmpDto = (ProblemListDto)
       session.getAttribute(ProblemListServlet.PROBLEM_LIST_ATTR + datasetId);
   if (tmpDto != null) {
       message = tmpDto.getMessage();
       messageLevel = tmpDto.getMessageLevel();
   }

   // Reset once read.
   session.setAttribute(ProblemListServlet.PROBLEM_LIST_ATTR + datasetId, null);
}

    int firstIndex = 0;
    int lastIndex = 0;

%>
     <div id="problemList">

        <!-- make note of attrs needed by javascript -->
        <input type="hidden" id="plMessage" value="<%=message %>" />
        <input type="hidden" id="plMessageLevel" value="<%=messageLevel %>" />

        <div id="problem_list_header">
             <span class="label">Problem List</span>
             <span id="problem_list_info"> -- <%=numProblemsTotal %> problems, <%=numHierarchiesTotal %> headings</span>

             <div id="problem_list_download_button">
                  <% if (downloadEnabled) { %>
                     <a href="javascript:downloadAll()" id="problemListDownloadLink"
                        class="ui-state-default ui-corner-all request_link">
                        <span class=""></span>Download Problem Content</a>
                  <% } else { %>
                     <span id="problemListDownloadLink"
                        class="dead_link ui-state-disabled ui-state-default ui-corner-all">
                        <span class=""></span>Download Problem Content</span>
                  <% } %>
             </div>  <!-- problem_list_download_button -->
        </div>  <!--  problem_list_header  -->

        <div id="problem_list_filters_div">
            <fieldset>
            <legend>Filters</legend>
            <div>
                <% if (searchBy.trim().equals("")) { %>
                   <input type="text" id="prob_list_search_by" name="prob_list_search_by" size="50"
                          title="Search by problem names and hierarchies " />
                   <img id="prob_list_search_button" name="prob_list_search_button" src="images/magnifier.png" />
                <% } else { %>
                   <input type="text" id="prob_list_search_by" name="prob_list_search_by"
                          size="50" value="<%=searchBy %>" />
                   <img id="prob_list_clear_button" name="prob_list_clear_button" src="images/clear_field.png" />
                <% } %>
            </div>
            <div>
            <span id="prob_list_pc_select_span">Problem Content: </span>
                 <select name="prob_list_pc_select" id="prob_list_pc_select">
                 <% for (String pcOpt : pcOptions) {
                      String selected = pcOption.equals(pcOpt) ? "selected=\"selected\"" : "";
                 %>
                      <option value="<%=pcOpt %>" <%=selected %>><%=pcOpt %></option>
                 <% } %>     
                 </select>
            </div>
            <div class="prob_list_pc_info">
                <img title="For some datasets, additional content that students saw is stored with the dataset. This problem content is typically a text prompt, an image of the interface, or an HTML representation of the problem."
                     alt="problem content" src="images/info.gif" />
            </div>
            </fieldset>
        </div>
<% if (hierarchyList.size() > 0) { %>
        <div id="problem_list_filter_options">
        <table id="problemListFilterOptionsTable">
        <tr>
            <td class="rows-per-page"><span class="headings_per_page">Headings per page: </span>
            <select name="prob_list_rows_per_page" class="prob_list_rows_per_page">
                <%
                for (int rppOpt: rowsPerPagesOptions) {
                    String selected = (rowsPerPage == rppOpt) ? "selected=\"selected\"" : "";
                %>
                    <option value="<%=rppOpt %>" <%=selected %>><%=rppOpt %></option>
                <%
                }
                %>
            </select>
            </td>
            <td>
                <table id="topPageCountTable">
                    <tr>
                    <% if (currentPage > 1 && numPages > 1) { %>
                       <td><a href="javascript:gotoProblemList(1)"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoProblemList(<%=currentPage - 1%>)"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                    <% } else {%>
                       <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                       <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                    <% } %>

                    <% if (currentPage < numPages && numPages > 1) { %>
                       <td><a href="javascript:gotoProblemList(<%=currentPage + 1%>)">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoProblemList(<%=numPages%>)">Last <img src="images/grid/forward_grey.gif" /></a></td>
                    <% } else {%>
                       <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                       <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                    <% } %>
                    </tr>
                </table> <!-- topPageCountTable  -->
            </td>
            <%
                firstIndex = (currentPage - 1) * rowsPerPage;
                lastIndex = numHierarchies;
                if (numHierarchies > firstIndex + rowsPerPage) { lastIndex = firstIndex + rowsPerPage; }
                if (numHierarchies == 0) { firstIndex = -1; }  // special case; empty table
            %>
            <td class="row-range"><%=firstIndex + 1 %>-<%=lastIndex %> of <%=numHierarchies %></td>
        </tr>
        </table>   <!-- problemListFilterOptionsTable  -->
        </div>  <!-- problem_list_filter_options  -->
<% } %>

        <div id="problem_list_div">
<%
        if (hierarchyList.size() > 0) {
%>
            <div id="problem-hierarchy-list">
<%
            for (String hierarchy : hierarchyList) {
%>
                 <h4><%=hierarchy %></h4>
<%
                 List<ProblemItem> problems = problemMap.get(hierarchy);
%>
                 <ul class="per-hierarchy-list">
<%
                 for (ProblemItem p : problems) {
                      if (p.getPcProblem() != null) {
%>
                          <li><a href="javascript:viewProblem(<%=p.getId() %>)"
                                 class="problem-list-view-problem"><%=p.getProblemName() %></a></li>
<%
                      } else {
%>
                          <li><span title="Problem content not available"><%=p.getProblemName() %></span></li>
<%
                      }
                 }
%>
                 </ul>
<%
            }
%>
            </div>   <!-- problem-hierarchy-list -->
<%
        } else {
%>
               <div class="emptyProblemListDiv">No problems or hierarchies match your search criteria.</div>
<%
        }
%>

        <div>   <!-- problem_list_div  -->

<% if (hierarchyList.size() > 0) { %>
        <div id="problem_list_paging_options">
        <table id="problemListPagingOptionsTable">
        <tr>
            <td class="rows-per-page"><span class="headings_per_page">Headings per page: </span>
            <select name="prob_list_rows_per_page" class="prob_list_rows_per_page">
                <%
                for (int rppOpt: rowsPerPagesOptions) {
                    String selected = (rowsPerPage == rppOpt) ? "selected=\"selected\"" : "";
                %>
                    <option value="<%=rppOpt %>" <%=selected %>><%=rppOpt %></option>
                <%
                }
                %>
            </select>
            </td>
            <td>
                <table id="bottomPageCountTable">
                    <tr>
                    <% if (currentPage > 1 && numPages > 1) { %>
                       <td><a href="javascript:gotoProblemList(1)"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoProblemList(<%=currentPage - 1%>)"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                    <% } else {%>
                       <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                       <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                    <% } %>

                    <% if (currentPage < numPages && numPages > 1) { %>
                       <td><a href="javascript:gotoProblemList(<%=currentPage + 1%>)">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoProblemList(<%=numPages%>)">Last <img src="images/grid/forward_grey.gif" /></a></td>
                    <% } else {%>
                       <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                       <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                    <% } %>
                    </tr>
                </table> <!-- bottomPageCountTable  -->
            </td>
            <%
                firstIndex = (currentPage - 1) * rowsPerPage;
                lastIndex = numHierarchies;
                if (numHierarchies > firstIndex + rowsPerPage) { lastIndex = firstIndex + rowsPerPage; }
                if (numHierarchies == 0) { firstIndex = -1; }  // special case; empty table
            %>
            <td class="row-range"><%=firstIndex + 1 %>-<%=lastIndex %> of <%=numHierarchies %></td>
        </tr>
        </table>   <!-- problemListFilterOptionsTable  -->
        </div>  <!-- problem_list_filter_options  -->
<% } %>

     </div> <!-- problem_list -->

