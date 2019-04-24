<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2015
  All Rights Reserved
-->

<%
// Author: Cindy Tipper
// Version: $Revision: 13128 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-19 12:46:09 -0400 (Tue, 19 Apr 2016) $
// $KeyWordsOff: $
//
%>

<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DecimalFormat"%>

<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>

<%@page import="edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDbHelper"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDbServlet"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDto"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Discourse Export";
    cssIncludes.add("discourse_db.css");
    cssIncludes.add("datasetInfo.css");
    jsIncludes.add("javascript/DiscourseDB.js");
%>

<%@ include file="/header.jspf" %>

<!-- Initialize discourseId attr for Help. -->
<script type="text/javascript">initializeDiscourseId();</script>

<%
    DiscourseDto dto = (DiscourseDto)
        request.getAttribute(DiscourseDbServlet.DISCOURSE_OVERVIEW_ATTR + discourseIdStr);

    DiscourseItem discourse = null;
    if (dto != null) {
        discourse = dto.getDiscourse();
    }

    Long discourseId = 0L;
    String discourseName = "";
    Long numDiscourseParts = 0L;
    Long numContributions = 0L;
    Long numRelations = 0L;
    Long numDataSources = 0L;
    Long numAnnotations = 0L;
    Long numUsers = 0L;
    String dateRange = "";
    String tabHeaderStr = "";

    // Eventually we might have discourses that didn't come in as Upload.
    // If so, might not have the file to export.
    Boolean hasExport = true;

    if (discourse != null) {
        discourseId = (Long)discourse.getId();

        discourseName = discourse.getName();
        numDiscourseParts = dto.getNumDiscourseParts();
        numContributions = dto.getNumContributions();
        numRelations = dto.getNumRelations();
        numDataSources = dto.getNumDataSources();
        numAnnotations = dto.getNumAnnotations();
        numUsers = dto.getNumUsers();
        dateRange = dto.getDateRange();

        boolean isAuthorized = true;   // TBD
        boolean userLoggedIn = (remoteUser == null) ? false : true;

        DiscourseDbHelper discourseDbHelper = HelperFactory.DEFAULT.getDiscourseDbHelper();
        tabHeaderStr =
            discourseDbHelper.displayTabs("Export", discourseId, isAuthorized, userLoggedIn);
    }

    String message = dto.getMessage();
    String messageLevel = dto.getMessageLevel();
%>

<!-- body -->

<tr id="body">

<td id="content">

<div id="tabheader"><%=tabHeaderStr %></div>

<div id="main">
    <div id="contents">
    <%=datasetNameString%>
    <div id="outer-div">

    <%
        if (message != null && messageLevel != null ) {
           if (messageLevel.compareTo(DiscourseDbServlet.STATUS_MESSAGE_LEVEL_SUCCESS) == 0) {
    %>
            <script type="text/javascript">successPopup("<%=message%>");</script>
    <%
           } else if (messageLevel.compareTo(DiscourseDbServlet.STATUS_MESSAGE_LEVEL_ERROR) == 0) {
    %>
            <script type="text/javascript">errorPopup("<%=message%>");</script>
    <%
           } else {
    %>
            <script type="text/javascript">messagePopup("<%=message%>", "WARNING");</script>
    <%    }
        }
    %>

    <% if (discourse != null) { %>

       <div id="exportDiscourseDiv">
       <p>
           <a href="#" id="exportDiscourseLink"
              <% if (hasExport) { %>
                  class="ui-state-default ui-corner-all"
                  onclick="javascript:exportDiscourse(<%=discourseId %>)"
              <% } else { %>
                  class="ui-state-default ui-corner-all dead_link ui-state-disabled"
                  title="File not available to download"
              <% } %>
              ><span class=""></span>Export Discourse</a>
       </p>
       </div>

       <!-- <div id="discourse-info-div"> -->
       <div id="datasetInfo">
            <div id="datasetInfoTables">
            <table id="discourse-info-table" class="dataset-box">
                   <caption>Overview</caption>
                   <tr>
                        <th class="row-header">Discourse Name</td>
                        <td class="discourse-name"><%=discourse.getName() %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Dates</td>
                        <td class="discourse-dates"><%=dateRange %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Users</td>
                        <td class="discourse-name"><%=numUsers %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Discourse Parts</td>
                        <td class="discourse-name"><%=numDiscourseParts %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Contributions</td>
                        <td class="discourse-name"><%=numContributions %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Data Sources</td>
                        <td class="discourse-name"><%=numDataSources %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Annotations</td>
                        <td class="discourse-name"><%=numAnnotations %></td>
                   </tr>
                   <tr>
                        <th class="row-header">Number of Relations</td>
                        <td class="discourse-name"><%=numRelations %></td>
                   </tr>

            </table>
            </div>   <!-- datasetInfoTables -->
       </div>  <!-- datasetInfo -->
    <% } %>

    </div>   <!-- outer-div -->

    <div style="clear:both"></div>

</div>  <!-- main -->
</div>  <!-- contents -->
</td>   <!-- content -->
</tr>   <!-- body -->

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>   <!-- opened in header.jspf -->

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/discourse-export.jspf" %>
</div>
