<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 11126 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-06-05 16:25:50 -0400 (Thu, 05 Jun 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageProblemContentDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageProblemContentContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageProblemContentServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.PcConversionDto"%>
<%@page import="edu.cmu.pslc.datashop.item.PcConversionItem"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage Problem Content";
    showHelpButton = false;

    cssIncludes.add("Admin.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/Admin.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<%

ManageProblemContentContext mpcContext = ManageProblemContentContext.getContext(request);
String conversionTool = mpcContext.getConversionTool();
String contentVersionSearchBy = mpcContext.getContentVersionSearchBy();
String datasetSearchBy = mpcContext.getDatasetSearchBy();
String mappedStr = mpcContext.getProblemContent();
String sortBy = mpcContext.getSortBy();
Boolean isAscending = mpcContext.isAscending(sortBy);

ManageProblemContentDto problemContentDto = (ManageProblemContentDto)
         request.getAttribute(ManageProblemContentServlet.MANAGE_PROBLEM_CONTENT_ATTR);

List<PcConversionDto> pcConversions = problemContentDto.getPcConversions();

boolean displayConversionToolColumn = problemContentDto.getDisplayConversionToolColumn();

List<String> conversionToolOptions = new ArrayList<String>();
conversionToolOptions.add(0, "");   // add an empty initial entry
conversionToolOptions.add(PcConversionItem.OLI_CONVERTER);
conversionToolOptions.add(PcConversionItem.TUTORSHOP_CONVERTER);

List<String> mappedOptions = new ArrayList<String>();
mappedOptions.add(ManageProblemContentDto.PROBLEM_CONTENT_BOTH);
mappedOptions.add(ManageProblemContentDto.PROBLEM_CONTENT_MAPPED);
mappedOptions.add(ManageProblemContentDto.PROBLEM_CONTENT_UNMAPPED);

String message = problemContentDto.getMessage();
String messageLevel = problemContentDto.getMessageLevel();

// After form submissions, the info is on the session.
if ((message == null) && (messageLevel == null)) {
   ManageProblemContentDto tmpDto = (ManageProblemContentDto)
       session.getAttribute(ManageProblemContentServlet.MANAGE_PROBLEM_CONTENT_ATTR);
   if (tmpDto != null) {
       message = tmpDto.getMessage();
       messageLevel = tmpDto.getMessageLevel();
   }

   // Reset once read.
   session.setAttribute(ManageProblemContentServlet.MANAGE_PROBLEM_CONTENT_ATTR, null);
}

%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

     <div id="manageProblemContent">
     <h1>Manage Problem Content</h1>

     <div id="problem_content_filters_div">
          <fieldset>
          <legend>Filters</legend>
          <div class="filter-div-first">
               <label>Conversion Tool </label><br/>
               <select name="manage_pc_tool_select" id="manage_pc_tool_select">
               <% for (String ctOpt : conversionToolOptions) {
                    String selected = conversionTool.equals(ctOpt) ? "selected=\"selected\"" : "";
               %>
                    <option value="<%=ctOpt %>" <%=selected %>><%=ctOpt %></option>
               <% } %>     
               </select>
          </div>
          <div>
              <label>Content Version </label><br/>
              <% if (contentVersionSearchBy.trim().equals("")) { %>
                 <input type="text" id="manage_pc_cv_search_by" name="manage_pc_cv_search_by"
                        size="50" title="Search by content version " />
                 <img id="manage_pc_cv_search_button"
                      name="manage_pc_cv_search_button" src="images/magnifier.png" />
              <% } else { %>
                 <input type="text" id="manage_pc_cv_search_by" name="manage_pc_cv_search_by"
                        size="50" value="<%=contentVersionSearchBy %>" />
                 <img id="manage_pc_cv_clear_button"
                      name="manage_pc_cv_clear_button" src="images/clear_field.png" />
              <% } %>
          </div>
          <div class="filter-div-first">
               <label>Mapped? </label><br/>
               <select name="manage_pc_mapped_select" id="manage_pc_mapped_select">
               <% for (String mOpt : mappedOptions) {
                    String selected = mappedStr.equals(mOpt) ? "selected=\"selected\"" : "";
               %>
                    <option value="<%=mOpt %>" <%=selected %>><%=mOpt %></option>
               <% } %>     
               </select>
          </div>
          <div>
              <label>Dataset </label><br/>
              <% if (datasetSearchBy.trim().equals("")) { %>
                 <input type="text" id="manage_pc_ds_search_by" name="manage_pc_ds_search_by"
                        size="50" title="Search by dataset " />
                 <img id="manage_pc_ds_search_button"
                      name="manage_pc_ds_search_button" src="images/magnifier.png" />
              <% } else { %>
                 <input type="text" id="manage_pc_ds_search_by" name="manage_pc_ds_search_by"
                        size="50" value="<%=datasetSearchBy %>" />
                 <img id="manage_pc_ds_clear_button"
                      name="manage_pc_ds_clear_button" src="images/clear_field.png" />
              <% } %>
          </div>
          </fieldset>
     </div>

     <% if (pcConversions.size() > 0) { %>
     <div id="manage_pc_conversions">
          <table id="manage_pc_conversions_table">
             <% if (displayConversionToolColumn) { %>
             <colgroup>
                <col style="width:17%"/>
                <col style="width:38%"/>
                <col style="width:15%"/>
                <col style="width:15%"/>
                <col style="width:10%"/>
                <col style="width:5%"/>
             </colgroup>
             <% } else { %>
             <colgroup>
                <col style="width:50%"/>
                <col style="width:15%"/>
                <col style="width:15%"/>
                <col style="width:10%"/>
                <col style="width:10%"/>
             </colgroup>
             <% } %>
             <thead>
                <tr>
                    <% if (displayConversionToolColumn) { %>
                    <th>
                        <a href="javascript:sortConversions('<%=PcConversionDto.COLUMN_CONVERSION_TOOL %>')">
                           <%=PcConversionDto.COLUMN_CONVERSION_TOOL %></a>
                        <img src="<%=PcConversionDto.getSortImage(sortBy,
                                                                  PcConversionDto.COLUMN_CONVERSION_TOOL,
                                                                  isAscending) %>"/>
                    </th>
                    <% } %>
                    <th>
                        <a href="javascript:sortConversions('<%=PcConversionDto.COLUMN_CONTENT_VERSION %>')">
                           <%=PcConversionDto.COLUMN_CONTENT_VERSION %></a>
                        <img src="<%=PcConversionDto.getSortImage(sortBy,
                                                                  PcConversionDto.COLUMN_CONTENT_VERSION,
                                                                  isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortConversions('<%=PcConversionDto.COLUMN_CONVERSION_DATE %>')">
                           <%=PcConversionDto.COLUMN_CONVERSION_DATE %></a>
                        <img src="<%=PcConversionDto.getSortImage(sortBy,
                                                                  PcConversionDto.COLUMN_CONVERSION_DATE,
                                                                  isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortConversions('<%=PcConversionDto.COLUMN_TOOL_VERSION %>')">
                           <%=PcConversionDto.COLUMN_TOOL_VERSION %></a>
                        <img src="<%=PcConversionDto.getSortImage(sortBy,
                                                                  PcConversionDto.COLUMN_TOOL_VERSION,
                                                                  isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortConversions('<%=PcConversionDto.COLUMN_NUM_PROBLEMS %>')">
                           <%=PcConversionDto.COLUMN_NUM_PROBLEMS %></a>
                        <img src="<%=PcConversionDto.getSortImage(sortBy,
                                                                  PcConversionDto.COLUMN_NUM_PROBLEMS,
                                                                  isAscending) %>"/>
                    </th>
                    <th>
                    </th>
                </tr>
             </thead>
             <tbody>
              <%
              for (PcConversionDto dto : pcConversions) {
                   Long contentVersionId = (Long)dto.getPcConversion().getId();
              %>
                <tr>
                   <% if (displayConversionToolColumn) { %>
                      <td><%= dto.getConversionTool() %></td>
                   <% } %>
                   <td>
                     <span class="content_version"><%=dto.getContentVersion() %></span>
                     <br>
                     <span><%=dto.getContentDescription() %></span>
                     <br>
                     <span class="content_date">Content Date: <%= dto.getContentDateStr() %></span>
                     <% if (!dto.getIsDeletable()) { %>
                     <br>
                     <div id="manage_pc_show_datasets_<%=contentVersionId %>">
                          <a href="javascript:showDatasets(<%=contentVersionId %>)">show datasets</a>
                     </div>
                     <div id="manage_pc_dataset_list_<%=contentVersionId %>"
                          class="manage_pc_dataset_list">
                          <div id="dataset_div_<%=contentVersionId %>" class="dataset_div"></div>
                          <a href="javascript:hideDatasets(<%=contentVersionId %>)">hide datasets</a>
                     </div>
                     <% } %>
                   </td>
                   <td><%= dto.getConversionDateStr() %></td>
                   <td><%= dto.getToolVersionStr() %></td>
                   <td><%= dto.getNumProblems() %></td>
                   <% if (dto.getIsDeletable()) { %>
                   <td>
                      <div id="manage_pc_delete_<%=contentVersionId %>">
                           <a href="javascript:showDeleteAreYouSure(<%=contentVersionId %>)">
                           <img src="images/delete.gif"
                                alt="Delete Conversion" title="Delete Conversion">
                           </a>
                      </div>
                      <div id="manage_pc_delete_areYouSure_<%=contentVersionId %>"
                           class="manage_pc_conversion_delete_areYouSure">
                           delete this conversion?&nbsp
                           <a id="manage_pc_delete_no_<%=contentVersionId %>" class="yes_no_links"
                              href="javascript:closeAreYouSure(<%=contentVersionId %>)">no</a>
                           &nbsp/&nbsp
                           <a id="manage_pc_delete_yes_<%=contentVersionId %>" class="yes_no_links"
                              href="javascript:deleteConversion(<%=contentVersionId %>)">yes</a>
                           </a>
                      </div>
                   </td>
                   <% } else {%>
                   <td></td>
                   <% } %>
                </tr>
              <%
              }
              %>
             </tbody>
          </table>
          </div>
     </div>
     <% } %>

     </div> <!-- manageProblemContent -->

</div> <!-- End #contents div -->
</div> <!--  End #main div -->
</td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

</body>
</html>
