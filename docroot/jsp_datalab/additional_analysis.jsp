<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2014
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 12060 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-03-04 16:07:10 -0500 (Wed, 04 Mar 2015) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.datalab.Constants"%>
<%@page import="edu.cmu.datalab.item.AnalysisItem"%>
<%@page import="edu.cmu.datalab.item.CorrelationValueId"%>

<%@page import="edu.cmu.datalab.servlet.AdditionalAnalysisDto"%>
<%@page import="edu.cmu.datalab.servlet.CorrelationValueDto"%>
<%@page import="edu.cmu.datalab.servlet.CronbachsAlphaDto"%>
<%@page import="edu.cmu.datalab.servlet.DatalabContext"%>
<%@page import="edu.cmu.datalab.servlet.DatalabServlet"%>
<%@page import="edu.cmu.datalab.servlet.ItemDto"%>

<%
    String liHomeClassStr = "";
    String li1ClassStr = "";
    String li2ClassStr = "class=\"active\"";

    String totalScoreStr = "Total Score";

    String correlationInfoStr = "These values are a measure of the correlation between each item and ";
    String rpbiInfoStr = "These values are a measure of the Point Biserial Correlation between each item and ";

    String lastRowInfoStr = "";
%>

<!--  header -->
<%@ include file="/jsp_datalab/header.jspf" %>

<%
    DatalabContext context = DatalabContext.getContext(request);
    Boolean cellShading = context.getCellShading();
    Double highCorrelationThreshold = context.getHighCorrelationValue();
    Double lowCorrelationThreshold = context.getLowCorrelationValue();

    String cellShadingStr = "checked";
    if (!cellShading) { cellShadingStr = ""; }

    AdditionalAnalysisDto dto = (AdditionalAnalysisDto)
        request.getAttribute(DatalabServlet.ADDITIONAL_ANALYSIS_ATTR + analysisIdStr);

    AnalysisItem analysis = null;
    Map<CorrelationValueId, CorrelationValueDto> correlationValues = null;
    List<CronbachsAlphaDto> alphas = null;
    List<ItemDto> items = null;
    ItemDto summaryItem = null;

    int numItems = 0;

    Boolean summaryColumnPresent = false;
    String analysisType = "";

    if (dto != null) {
        analysis = dto.getAnalysis();
        correlationValues = dto.getCorrelationValues();
        alphas = dto.getCronbachsAlphas();
        items = dto.getItems();

        summaryColumnPresent = analysis.getSummaryColumnPresent();
        analysisType = analysis.getAnalysisType();

        numItems = analysis.getNumItems();

        // If Summary Column is present remove it from the list as
        // we'll put it in the footer instead.
        if (summaryColumnPresent) {
            int lastIndex = items.size() - 1;
            summaryItem = items.get(lastIndex);
            items.remove(lastIndex);

            correlationInfoStr += summaryItem.getItemName();
            rpbiInfoStr += summaryItem.getItemName();

            // If summary column is present, don't display it's Alpha.
            lastIndex = alphas.size() - 1;
            alphas.remove(lastIndex);
        } else {
            correlationInfoStr += totalScoreStr;
            rpbiInfoStr += totalScoreStr;
        }

        if (analysisType.equals(AnalysisItem.TYPE_GRADEBOOK)) {
            lastRowInfoStr = correlationInfoStr;
        } else if (analysisType.equals(AnalysisItem.TYPE_ITEM)) {
            lastRowInfoStr = rpbiInfoStr;
        }
    }

    // Clear session attr once we've arrived here.
    session.setAttribute(DatalabServlet.GETTING_STARTED_ATTR, null);

%>

<!-- make note of attrs needed by javascript -->
<input type="hidden" id="numItems" value="<%=numItems %>" />

<%
  if (dto == null) {
%>
<div class="outer-div">
<div id="drop-file-div" ondrop="dropFile(event)" ondragover="allowDrop(event)">
     <br/><p class="drop-file-info"><%=dragFileText %></p>
</div>
<div id="upload-new-file-div">
     or <a href="javascript:openUploadDialog()">choose a file on your computer</a>
</div>
</div>   <!-- outer-div -->
<% } %>

<div id="file-loading-div">
     <img id="file-loading-img" src="images/waiting.gif" />
     <br/><p class="drop-file-info"><%=loadingFileText %></p>
</div>

<%
  if (dto != null) {
%>
<div class="outer-div">
<div id="cell-shading-div">
     <input type="checkbox" name="cellShadingInput" id="cellShadingInput" <%=cellShadingStr %>/>
     <label for="cellShadingInput">Shade cells based on correlation thresholds</label>

     <table id="correlationThresholdsTable">
     <tbody>
        <tr>
            <td class="threshold-label">High correlation is greater than</td>
            <td class="threshold-value">
                <input type="textarea" id="high-threshold-value" class="threshold-input"
                       value="<%=highCorrelationThreshold %>">
                </input>
            </td>
        </tr>
        <tr>
            <td class="threshold-label">Low correlation is less than</td>
            <td class="threshold-value">
                <input type="textarea" id="low-threshold-value" class="threshold-input"
                       value="<%=lowCorrelationThreshold %>">
                </input>
            </td>
        </tr>
     </tbody>
     </table>
</div>

<div id="additional-analysis-tables-div">
<%
  if ((items.size() > 0) && (correlationValues != null)) {
%>
<div id="correlation-table-div">

     <div id="correlation-label">
     <span class="dl-label">Correlation Matrix</span>
     <img title="Correlation Matrix" alt="Correlation Matrix" src="images/info.gif" />
     </div>
     <table id="correlationTable" class="cell-border compact">
     <thead>
     <tr>
         <th class="fixed-col-left"></th>

         <%
           for (ItemDto i : items) {
         %>
         <th class="dl-table-header"><%=i.getItemName() %></th>
         <%
           }
         %>
     </tr>
     </thead>
     <tbody>
     <%
       for (ItemDto i : items) {
           Long item1Id = i.getId();
     %>
     <tr>
         <td style="border-right-color:black"><%=i.getItemName() %></td>
         <%
           for (ItemDto i2 : items) {
               CorrelationValueId cvid = new CorrelationValueId(i2.getItem(), i.getItem());
               CorrelationValueDto cv = correlationValues.get(cvid);
               if (cv == null) {
         %>
               <td></td>
               <%
                } else { 
               %>
               <td class="<%=cv.getShading() %>"><%=cv.getValueToDisplay() %></td>
               <% } %>
         <%
           }
         %>
       <%
       }
       %>
     </tr>
     </tbody>
     <tfoot>
     <% if (!summaryColumnPresent) { %>
     <tr>
        <td class="fixed-col-left"><%=totalScoreStr %>
            <img title="<%=lastRowInfoStr %>" alt="<%=totalScoreStr %>"
                 src="images/info.gif" />
        </td>
        <%
        for (ItemDto i : items) {
        %> 
            <td class="<%=i.getCorrelationShading() %>">
                <%=i.getCorrelationValue() %>
            </td>       
        <%
        }
        %>
     </tr>
     <% } else { %>
     <tr>
        <td class="dl-table-header fixed-col-left"><%=summaryItem.getItemName() %>
            <img title="<%=lastRowInfoStr %>" alt="<%=totalScoreStr %>"
                 src="images/info.gif" />
        </td>
         <%
           if (analysisType.equals(AnalysisItem.TYPE_GRADEBOOK)) {
               for (ItemDto i : items) {
                    CorrelationValueId cvid =
                                       new CorrelationValueId(i.getItem(), summaryItem.getItem());
                    CorrelationValueDto cv = correlationValues.get(cvid);
                    if (cv == null) {
         %>
                    <td></td>

                    <% } else { %>
                    <td class="<%=cv.getShading() %>"><%=cv.getValueToDisplay() %></td>
                    <% }
               }
           } else if (analysisType.equals(AnalysisItem.TYPE_ITEM)) {
               for (ItemDto i : items) {
         %> 
                    <td class="<%=i.getCorrelationShading() %>"><%=i.getCorrelationValue() %></td>
         <%
               }
           }
         %>
     </tr>
     <% } %>
     </tfoot>
     </table>
</div>
<%
  }
%>

<%
  if (alphas.size() > 0) {
     CronbachsAlphaDto allItemsAlpha = alphas.get(0);
     alphas.remove(0);
%>
<div id="cronbachs-alpha-table-div">

     <div id="reliability-label">
     <span class="dl-label">Reliability</span>
     <img title="Reliability is based on Cronbach's Alpha.
Cells are highlighted for an improved alpha. An 
improved alpha suggests a problem with this item." alt="Reliability" src="images/info.gif" />
     </div>
     <table id="cronbachsAlphaTable" class="cell-border compact">
     <thead>
     <tr>
         <th class="dl-table-header">Items</th>
         <th class="dl-table-header">Alpha</th>
     </tr>
     <tr>
         <th class="dl-table-header"><%=allItemsAlpha.getLabel() %></th>
         <th class="dl-table-header"><%=allItemsAlpha.getValue() %></th>
     </tr>
     </thead>
     <tbody>
     <%
       for (CronbachsAlphaDto a : alphas) {
     %>
     <tr>
         <td><%=a.getLabel() %></td>
         <td class="<%=a.getShading() %>"><%=a.getValue() %></td>
     </tr>
     <%
       }
     %>

     </tbody>
     </table>
</div>
<%
  }
%>
</div>

</div>

<%
  }    // dto != null
%>
</div>    <!-- datalab-content-wrapper-div -->
</div>    <!-- datalab-content-div -->

<div style="clear:both;"></div>
  
</html>