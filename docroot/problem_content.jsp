<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 11106 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-05-29 15:34:54 -0400 (Thu, 29 May 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="edu.cmu.pslc.datashop.dao.CurriculumDao"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.MappedContentDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.CurriculumItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem"%>
<%@page import="edu.cmu.pslc.datashop.item.PcConversionItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProblemItem"%>

<%

DatasetContext datasetContext = (DatasetContext)session.getAttribute("datasetContext_"
               + request.getParameter("datasetId"));
DatasetItem dataset = datasetContext.getDataset();
Integer datasetId = null;
if (dataset != null) {
   datasetId = (Integer)dataset.getId();
   dataset = DaoFactory.DEFAULT.getDatasetDao().get(datasetId);
}
CurriculumDao dao = DaoFactory.DEFAULT.getCurriculumDao();
CurriculumItem curriculum = dataset.getCurriculum();
if (curriculum != null) {
   curriculum = dao.get((Integer)curriculum.getId());
}

ProblemContentContext pcContext = ProblemContentContext.getContext(request);
String conversionTool = pcContext.getConversionTool();
String searchBy = pcContext.getSearchBy();
String sortBy = pcContext.getSortBy();
Boolean isAscending = pcContext.isAscending(sortBy);

ProblemContentDto problemContentDto =
    (ProblemContentDto)request.getAttribute(ProblemContentServlet.PROBLEM_CONTENT_ATTR + datasetId);

List<PcConversionItem> contentVersions = problemContentDto.getContentVersions();
List<MappedContentDto> mappedContent = problemContentDto.getMappedContent();

Long numProblems = problemContentDto.getNumProblems();
Long numProblemsMapped = problemContentDto.getNumProblemsMapped();

List<String> conversionToolOptions = new ArrayList<String>();
conversionToolOptions.add(0, "");   // add an empty initial entry
conversionToolOptions.add(PcConversionItem.OLI_CONVERTER);
conversionToolOptions.add(PcConversionItem.TUTORSHOP_CONVERTER);

String message = problemContentDto.getMessage();
String messageLevel = problemContentDto.getMessageLevel();

// After form submissions, the info is on the session.
if ((message == null) && (messageLevel == null)) {
   ProblemContentDto tmpDto = (ProblemContentDto)
       session.getAttribute(ProblemContentServlet.PROBLEM_CONTENT_ATTR + datasetId);
   if (tmpDto != null) {
       message = tmpDto.getMessage();
       messageLevel = tmpDto.getMessageLevel();
   }

   // Reset once read.
   session.setAttribute(ProblemContentServlet.PROBLEM_CONTENT_ATTR + datasetId, null);
}

%>
     <div id="problemContent">

     <% if (curriculum != null) { %>
         <div id="pc_curriculum">
              <span class="label">Curriculum: <%=curriculum.getCurriculumName() %></span>
         </div>
     <% } %>

     <div id="pc_conversion_tool">
          <div class="label">Conversion Tool</div>
          <select name="pc_conversion_tool_select" id="pc_conversion_tool_select">
              <%
              for (String tutor : conversionToolOptions) {
                  String selected = (conversionTool.equals(tutor)) ? "selected=\"selected\"" : "";
              %>
                  <option value="<%=tutor %>" <%=selected %>><%=tutor %></option>
              <%
              }
              %>
          </select>
     </div>

     <div id="pc_content_version">
          <div class="label">Content Version</div>
          <div id="pc_content_version_search">
          <% if (searchBy.trim().equals("")) { %>
                <input type="text" id="pc_search_by" name="pc_search_by" size="50"
                       title="Search by content version " />
                <img id="pc_search_button" name="pc_search_button" src="images/magnifier.png" />
          <% } else { %>
                <input type="text" id="pc_search_by" name="pc_search_by"
                       size="50" value="<%=searchBy %>" />
                <img id="pc_clear_button" name="pc_clear_button" src="images/clear_field.png" />
          <% } %>
          </div>
          <select name="pc_content_version_select" id="pc_content_version_select" size="5">
              <%
              for (PcConversionItem pci : contentVersions) {
                  String nameStr = pci.getContentVersion();
                  String dateStr = "";
                  // Add date info to name if TutorShop (MathTutor)
                  if ((!pci.getContentDateStr().equals(""))
                      && (pci.getConversionTool().contains("TutorShop"))) {
                      dateStr = "(" + pci.getContentDateStr() + ")";
                  }
              %>
                  <option value="<%=pci.getId() %>"><%=nameStr %>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=dateStr %></option>
              <%
              }
              %>
          </select>
          <div id="pc_content_version_box">
          <%
          for (PcConversionItem pci : contentVersions) {
          %>
               <div id="pc_content_version_<%=pci.getId() %>" class="content_version_data">
                    <span class="content_date">Content Date: <%=pci.getContentDateStr() %>
                    </span>
                    <br>
                    <span class="conversion_date">Conversion Date: <%=pci.getConversionDateStr() %>
                    </span>
                    <br>
                    <br>
                    <span class="content_description"><%=pci.getContentDescription() %></span>
               </div>
          <%
          }
          %>
          </div>
          <div id="pc_content_version_add">
               <input type="button" id="add_content_version"
                      value="Add" onclick="javascript:addContentVersion()" disabled />
          </div>
     </div>

     <% if (mappedContent.size() > 0) { %>
     <div id="pc_mapped_content">
          <div id="pc_mapped_content_label">
          <div class="label">Mapped Content 
               <span id="problem_list_info">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                     <%=numProblemsMapped %> out of <%=numProblems %> problems mapped</span>
          </div>
          <table id="pc_mapped_content_table" class="dataset-box">
             <colgroup>
                <col style="width:45%"/>
                <col style="width:15%"/>
                <col style="width:15%"/>
                <col style="width:30%"/>
                <col style="width:15%"/>
             </colgroup>
             <thead>
                <tr>
                    <th>
                        <a href="javascript:sortMappedContent('<%=MappedContentDto.COLUMN_CONTENT_VERSION %>')">
                           <%=MappedContentDto.COLUMN_CONTENT_VERSION %></a>
                        <img src="<%=MappedContentDto.getSortImage(sortBy,
                                                                     MappedContentDto.COLUMN_CONTENT_VERSION,
                                                                     isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortMappedContent('<%=MappedContentDto.COLUMN_CONTENT_DATE %>')">
                           <%=MappedContentDto.COLUMN_CONTENT_DATE %></a>
                        <img src="<%=MappedContentDto.getSortImage(sortBy,
                                                                     MappedContentDto.COLUMN_CONTENT_DATE,
                                                                     isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortMappedContent('<%=MappedContentDto.COLUMN_CONVERSION_DATE %>')">
                           <%=MappedContentDto.COLUMN_CONVERSION_DATE %></a>
                        <img src="<%=MappedContentDto.getSortImage(sortBy,
                                                                     MappedContentDto.COLUMN_CONVERSION_DATE,
                                                                     isAscending) %>"/>
                    </th>
                    <th>
                        <a href="javascript:sortMappedContent('<%=MappedContentDto.COLUMN_STATUS %>')">
                           <%=MappedContentDto.COLUMN_STATUS %></a>
                        <img src="<%=MappedContentDto.getSortImage(sortBy,
                                                                     MappedContentDto.COLUMN_STATUS,
                                                                     isAscending) %>"/>
                    </th>
                    <th>
                    </th>
                </tr>
             </thead>
             <tbody>
              <%
              String statusStr = "";
              long numMapped = 0;
              String classStr;
              for (MappedContentDto dto : mappedContent) {
                   classStr = "num_problems_mapped";
                   numMapped = dto.getNumProblemsMapped();
                   if (numMapped == 0) {
                       classStr += " none_mapped";
                   }

                   statusStr = dto.getStatus();
                   Long contentVersionId = (Long)dto.getPcConversion().getId();
                   if (statusStr.equalsIgnoreCase(PcConversionDatasetMapItem.STATUS_ERROR)) {
                       statusStr = "<a href=\"javascript:displayMappingError()\">Error</a>";
                   }
              %>
                <tr>
                   <td>
                     <span class="content_version"><%=dto.getContentVersion() %></span>
                     <br>
                     <span><%=dto.getContentDescription() %></span>
                   </td>
                   <td><%= dto.getContentDateStr() %></td>
                   <td><%= dto.getConversionDateStr() %></td>
                   <td>
                     <span><%=statusStr %></span>
                     <% if (!statusStr.
                            equalsIgnoreCase(PcConversionDatasetMapItem.STATUS_PENDING)) { %>
                     <br>
                     <span class="<%=classStr %>"><%=numMapped %> problems mapped
                     </span>
                     <% } %>
                   </td>
                   <td>
                      <div id="pc_delete_map_<%=contentVersionId %>"
                           class="pc_content_version_map_delete">
                           <a href="javascript:showDeleteAreYouSure(<%=contentVersionId %>)">
                           <img src="images/delete.gif" alt="Delete Mapping" title="Delete Mapping">
                           </a>
                      </div>
                      <div id="pc_delete_map_areYouSure_<%=contentVersionId %>"
                           class="pc_content_version_map_delete_areYouSure">
                           delete this mapping?&nbsp
                           <a id="pc_delete_map_no_<%=contentVersionId %>" class="yes_no_links"
                              href="javascript:closeAreYouSure(<%=contentVersionId %>)">no</a>
                           &nbsp/&nbsp
                           <a id="pc_delete_map_yes_<%=contentVersionId %>" class="yes_no_links"
                              href="javascript:deleteContentVersion(<%=contentVersionId %>)">yes</a>
                           </a>
                      </div>
                   </td>
                </tr>
              <%
              }
              %>
             </tbody>
          </table>
          </div>
     </div>
     <% } %>

     </div> <!-- problemContent -->

