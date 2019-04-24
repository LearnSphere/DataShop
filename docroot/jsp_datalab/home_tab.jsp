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
<%@page import="edu.cmu.datalab.item.AnalysisItem"%>

<%@page import="edu.cmu.datalab.servlet.DatalabContext"%>
<%@page import="edu.cmu.datalab.servlet.DatalabServlet"%>
<%@page import="edu.cmu.datalab.servlet.HomeDto"%>

<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>

<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%
    String liHomeClassStr = "class=\"active\"";
    String li1ClassStr = "";
    String li2ClassStr = "";

    FastDateFormat dateFormat = FastDateFormat.getInstance("MMM d, yyyy");
%>

<!--  header -->
<%@ include file="/jsp_datalab/header.jspf" %>

<%
    DatalabContext context = DatalabContext.getContext(request);
    String sortBy = context.getHomeSortBy();
    Boolean isAscending = context.isAscending(sortBy);

    HomeDto dto = (HomeDto)request.getAttribute(DatalabServlet.HOME_ATTR + remoteUser);
    UserItem userItem = null;
    List<AnalysisItem> gradebookAnalyses = null;
    List<AnalysisItem> itemAnalyses = null;

    String warningMsg = (String)request.getAttribute(DatalabServlet.WARNING_MESSAGE_ATTR);

    String message = null;
    String messageLevel = null;
    if (dto != null) {
        userItem = dto.getUser();
        gradebookAnalyses = dto.getGradebookAnalyses();
        itemAnalyses = dto.getItemAnalyses();
        
        message = dto.getMessage();
        messageLevel = dto.getMessageLevel();

        if (message == null) {
            // Check the session...
            HomeDto tmpDto =
                (HomeDto)session.getAttribute(DatalabServlet.HOME_ATTR + remoteUser);
            if (tmpDto != null) {
                message = tmpDto.getMessage();
                messageLevel = tmpDto.getMessageLevel();
            }
            
            // Clear session once read.
            session.setAttribute(DatalabServlet.HOME_ATTR + remoteUser, null);
        }

    } else if (warningMsg != null) {
        message = warningMsg;
        messageLevel = DatalabServlet.STATUS_MESSAGE_LEVEL_WARN;
    }
%>

<!-- make note of attrs needed by javascript -->
<input type="hidden" id="message" value="<%=message %>" />
<input type="hidden" id="messageLevel" value="<%=messageLevel %>" />

<div class="outer-div">
<div id="upload-new-file-div">
     <a href="javascript:openUploadDialog()">Upload a new file</a>
</div>
</div>   <!-- outer-div -->

<div id="file-loading-div">
     <img id="file-loading-img" src="images/waiting.gif" />
     <br/><p class="drop-file-info"><%=loadingFileText %></p>
</div>

<%
  if (dto != null) {
%>

<div class="outer-div">

<%
  if (gradebookAnalyses.size() > 0) {
%>
     <span class="dl-label-small">gradebook</span>
     <table id="gradebookTable" class="dl-home-table">
     <colgroup>
        <col style="width:35%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:5%" />
     </colgroup>
     <thead>
     <tr>
         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.ANALYSIS_SORT_BY %>')"><%=HomeDto.ANALYSIS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.ANALYSIS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.UPLOADED_SORT_BY %>')"><%=HomeDto.UPLOADED_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.UPLOADED_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.NUM_STUDENTS_SORT_BY %>')"><%=HomeDto.NUM_STUDENTS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.NUM_STUDENTS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.NUM_ITEMS_SORT_BY %>')"><%=HomeDto.NUM_ITEMS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.NUM_ITEMS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.MAX_SCORE_SORT_BY %>')"><%=HomeDto.MAX_SCORE_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.MAX_SCORE_SORT_BY, isAscending) %>"/>
         </th>

         <!-- empty column for delete icon -->
         <th class="dl-table-header"></th>
     </tr>
     </thead>
     <tbody>
     <%
       String trClassStr = "";
       int count = 0;
       for (AnalysisItem ai : gradebookAnalyses) {
            FileItem file = ai.getFile();
            String fileName = file.getFileName();
            String analysisName = ai.getAnalysisName();
            boolean nameSet = analysisName.equals(fileName) ? false : true;
            String uploadedStr = dateFormat.format(ai.getCreatedTime());
            Double maxScore = ai.getMaxScoreGiven();
            if (!ai.getSummaryColumnPresent()) {
                maxScore = ai.getMaxScoreComputed();
            }

            if ((count % 2) != 0) {
                trClassStr = "class=\"odd\"";
            } else {
                trClassStr = "";
            }
            count++;
     %>
        <tr <%=trClassStr %>>
            <td id="name_td_<%=ai.getId() %>"><span id="name_span_<%=ai.getId() %>">
                <a href="javascript:loadOverview(<%=ai.getId() %>)"><%=analysisName %></a>
                </span>
                <input id="name_input_<%=ai.getId() %>" type="hidden"
                       value="<a href='javascript:loadOverview(<%=ai.getId() %>)'><%=analysisName %></a>"/>
                <a href="javascript:editAnalysisName(<%=ai.getId() %>)" class="analysis-edit-img">
                   <img src="images/pencil.png" alt="Edit Name" title="Edit Name"/></a>
                <div id="nameTextAreaDiv_<%=ai.getId() %>" class="nameChangeDiv">
                     <textarea id="nameInput_<%=ai.getId() %>"
                               rows="1" cols="45"><%=analysisName %></textarea>
                </div>
                <div id="nameSaveDiv_<%=ai.getId() %>" class="nameChangeDiv">
                     <input type="button" value="Save" onclick="javascript:saveNameChange(<%=ai.getId() %>)" />
                     <input type="button" value="Cancel" onclick="javascript:cancelNameChange(<%=ai.getId() %>)" />
                </div>
                <% if (nameSet) { %>
                <br>
                <%=fileName %>
                <% } %>
            </td>
            <td class="center"><%=uploadedStr %></td>
            <td class="center"><%=ai.getNumStudents() %></td>
            <td class="center"><%=ai.getNumItems() %></td>
            <td class="center"><%=df.format(maxScore) %></td>
            <td class="center">
                <div id="deleteAnalysisDiv_<%=ai.getId() %>">
                <a href="javascript:analysisDeleteAreYouSure(<%=ai.getId() %>)">
                <img src="images/delete.gif" alt="Delete" title="Delete"/>
                </a>
                </div>
                <div id="analysisSureDiv_<%=ai.getId() %>" class="deleteSureDiv">
                delete this gradebook?&nbsp
                <a href="javascript:deleteAnalysis(<%=ai.getId() %>)">yes</a>
                <a href="javascript:cancelDeleteAnalysis(<%=ai.getId() %>)">no</a>
                </div>
            </td>

        </tr>
     <% } %>
     </tbody>
     </table>
<%
  }    // gradebookAnalyses.size() > 0
%>

<%
  if (itemAnalyses.size() > 0) {
%>

     <span class="dl-label-small">item</span>
     <table id="itemTable" class="dl-home-table">
     <colgroup>
        <col style="width:35%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:5%" />
     </colgroup>
     <thead>
     <tr>
         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.ANALYSIS_SORT_BY %>')"><%=HomeDto.ANALYSIS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.ANALYSIS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.UPLOADED_SORT_BY %>')"><%=HomeDto.UPLOADED_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.UPLOADED_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.NUM_STUDENTS_SORT_BY %>')"><%=HomeDto.NUM_STUDENTS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.NUM_STUDENTS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.NUM_ITEMS_SORT_BY %>')"><%=HomeDto.NUM_ITEMS_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.NUM_ITEMS_SORT_BY, isAscending) %>"/>
         </th>

         <th class="dl-table-header">
             <a href="javascript:sortHome('<%=HomeDto.MAX_SCORE_SORT_BY %>')"><%=HomeDto.MAX_SCORE_SORT_BY %></a>
             <img src="<%=HomeDto.getSortImage(sortBy, HomeDto.MAX_SCORE_SORT_BY, isAscending) %>"/>
         </th>

         <!-- empty column for delete icon -->
         <th class="dl-table-header"></th>
     </tr>
     </thead>
     <tbody>
     <%
       String trClassStr = "";
       int count = 0;
       for (AnalysisItem ai : itemAnalyses) {
            FileItem file = ai.getFile();
            String fileName = file.getFileName();
            String analysisName = ai.getAnalysisName();
            boolean nameSet = analysisName.equals(fileName) ? false : true;
            String uploadedStr = dateFormat.format(ai.getCreatedTime());
            Double maxScore = ai.getMaxScoreGiven();
            if (!ai.getSummaryColumnPresent()) {
                maxScore = ai.getMaxScoreComputed();
            }

            if ((count % 2) != 0) {
                trClassStr = "class=\"odd\"";
            } else {
                trClassStr = "";
            }
            count++;
     %>
        <tr <%=trClassStr %>>
            <td id="name_td_<%=ai.getId() %>"><span id="name_span_<%=ai.getId() %>">
                <a href="javascript:loadOverview(<%=ai.getId() %>)"><%=analysisName %></a>
                </span>
                <input id="name_input_<%=ai.getId() %>" type="hidden"
                       value="<a href='javascript:loadOverview(<%=ai.getId() %>)'><%=analysisName %></a>"/>
                <a href="javascript:editAnalysisName(<%=ai.getId() %>)" class="analysis-edit-img">
                   <img src="images/pencil.png" alt="Edit Name" title="Edit Name"/></a>
                <div id="nameTextAreaDiv_<%=ai.getId() %>" class="nameChangeDiv">
                     <textarea id="nameInput_<%=ai.getId() %>"
                               rows="1" cols="45"><%=analysisName %></textarea>
                </div>
                <div id="nameSaveDiv_<%=ai.getId() %>" class="nameChangeDiv">
                     <input type="button" value="Save" onclick="javascript:saveNameChange(<%=ai.getId() %>)" />
                     <input type="button" value="Cancel" onclick="javascript:cancelNameChange(<%=ai.getId() %>)" />
                </div>
                <% if (nameSet) { %>
                <br>
                <%=fileName %>
                <% } %>
            </td>
            <td class="center"><%=uploadedStr %></td>
            <td class="center"><%=ai.getNumStudents() %></td>
            <td class="center"><%=ai.getNumItems() %></td>
            <td class="center"><%=df.format(maxScore) %></td>
            <td class="center"><a href="javascript:deleteAnalysis(<%=ai.getId() %>)">
                                  <img src="images/delete.gif" alt="Delete" title="Delete"/></a>
            </td>
        </tr>
     <% } %>
     </tbody>
     </table>
<%
  }    // itemAnalyses.size() > 0
%>

</div>  <!-- outer-div -->
<%
  }    // dto != null
%>

</div>    <!-- datalab-content-wrapper-div -->
</div>    <!-- datalab-content-div -->

<div style="clear:both;"></div>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="addAnalysisDialog" class="addAnalysisDialog"></div>
  
</html>