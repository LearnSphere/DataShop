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
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>

<%@page import="edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.ContributionTypeDto"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDbHelper"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDbServlet"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscourseDto"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.DiscoursePartTypeDto"%>
<%@page import="edu.cmu.pslc.datashop.discoursedb.servlet.RelationTypeDto"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Discourse Info";
    cssIncludes.add("discourse_db.css");
    cssIncludes.add("datasetInfo.css");
    jsIncludes.add("javascript/DiscourseDB.js");
%>

<%@ include file="/header.jspf" %>

<!-- Initialize discourseId attr for Help. -->
<script type="text/javascript">initializeDiscourseId();</script>

<%
    ProjectHelper projectHelper = HelperFactory.DEFAULT.getProjectHelper();

    DiscourseDto dto = (DiscourseDto)
        request.getAttribute(DiscourseDbServlet.DISCOURSE_OVERVIEW_ATTR + discourseIdStr);

    DiscourseItem discourse = null;
    if (dto != null) {
        discourse = dto.getDiscourse();
        isRemote = dto.getIsRemote();
    }

    Long discourseId = 0L;
    String discourseName = "";
    int numDiscoursePartTypes = 0;
    int numContributionTypes = 0;
    int numRelationTypes = 0;
    Long numDiscourseParts = 0L;
    Long numContributions = 0L;
    Long numRelations = 0L;
    Long numDataSources = 0L;
    Long numAnnotations = 0L;
    Long numUsers = 0L;
    String dateRange = "";
    List<DiscoursePartTypeDto> parts = null;
    List<ContributionTypeDto> contributions = null;
    List<RelationTypeDto> relations = null;
    String tabHeaderStr = "";

    if (discourse != null) {
        discourseId = (Long)discourse.getId();

        discourseName = discourse.getName();
        if (!isRemote) {
           parts = dto.getDiscoursePartTypes();
           numDiscoursePartTypes = parts.size();
           contributions = dto.getContributionTypes();
           numContributionTypes = contributions.size();
           relations = dto.getRelationTypes();
           numRelationTypes = relations.size();
        } else {
           numDiscourseParts = dto.getNumDiscourseParts();
           numContributions = dto.getNumContributions();
           numRelations = dto.getNumRelations();
        }

        numDataSources = dto.getNumDataSources();
        numAnnotations = dto.getNumAnnotations();
        numUsers = dto.getNumUsers();
        dateRange = dto.getDateRange();

        boolean isAuthorized = projectHelper.isAuthorizedForDiscourseDb(remoteUser, discourseId);
        boolean userLoggedIn = (remoteUser == null) ? false : true;

        DiscourseDbHelper discourseDbHelper = HelperFactory.DEFAULT.getDiscourseDbHelper();
        tabHeaderStr =
            discourseDbHelper.displayTabs("DiscourseInfo", discourseId, isAuthorized, userLoggedIn);
    }
%>

<!-- body -->

<tr id="body">

<td id="content">

<div id="tabheader"><%=tabHeaderStr %></div>

<div id="main">
    <div id="contents">
    <%=datasetNameString%>
    <div id="outer-div">

    <% if (discourse != null) { %>
       <!-- <div id="discourse-info-div"> -->

       <%
       String remoteAccessButton = projectHelper.getRemoteAccessButton(discourse);
       %>
       <div id="remoteAccessButtonDiv"><%=remoteAccessButton %></div>

       <div id="datasetInfo">
            <div id="datasetInfoTables">
            <% if (!isRemote) { %>
            <table id="discourse-info-table" class="dataset-box">
                   <caption>Discourse</caption>
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
                        <th class="row-header">Discourse Parts</td>
                        <td id="discourse-parts">
                            <% if ((parts != null) && (parts.size() > 0)) { %>
                            <table id="discourse-parts-table">
                                   <tr><td><b><%=numDiscoursePartTypes %> type(s):</td></b></tr>
                                   <% for (DiscoursePartTypeDto dp : parts) { %>
                                      <tr><td><%=dp.getType() %></td><td><%=dp.getCount() %></td></tr>
                                   <% } %>
                            </table>
                            <% } %>
                        </td>
                   </tr>
                   <tr>
                        <th class="row-header">Contributions</td>
                        <td id="contributions">
                            <% if ((contributions != null) && (contributions.size() > 0)) { %>
                            <table id="contributions-table">
                                   <tr><td><b><%=numContributionTypes %> type(s):</td></b></tr>
                                   <% for (ContributionTypeDto c : contributions) { %>
                                      <tr><td><%=c.getType() %></td><td><%=c.getCount() %></td></tr>
                                   <% } %>
                            </table>
                            <% } %>
                        </td>
                   </tr>
                   <tr>
                        <th class="row-header">Data Sources</td>
                        <td id="data-sources">
                            <table id="data-sources-table">
                                   <tr><td><b><%=numDataSources %> total:</b></td></tr>
                                   <tr><td>Types: </td><td><%=dto.getDataSourceTypes() %></td></tr>
                                   <tr><td>Datasets: </td><td><%=dto.getDataSourceDatasets() %></td></tr>
                            </table>
                        </td>
                   </tr>
                   <tr>
                        <th class="row-header">Annotations</td>
                        <td id="annotations">
                            <table id="annotations-table">
                                   <tr><td><b><%=numAnnotations %> total:</b></td></tr>
                                   <% if (numAnnotations > 0) { %>
                                   <tr><td>Types: </td><td><%=dto.getAnnotationTypes() %></td></tr>
                                   <% } else { %>
                                   <tr><td>Types: </td><td></td></tr>
                                   <% } %>
                            </table>
                        </td>
                   </tr>
                   <tr>
                        <th class="row-header">Discourse Relations</td>
                        <td id="relations">
                            <% if ((relations != null) && (relations.size() > 0)) { %>
                            <table id="relations-table">
                                   <tr><td><b><%=numRelationTypes %> type(s):</b></td></tr>
                                   <% for (RelationTypeDto r : relations) { %>
                                      <tr><td><%=r.getType() %></td><td><%=r.getCount() %></td></tr>
                                   <% } %>
                            </table>
                            <% } %>
                        </td>
                   </tr>
            </table>
            <% } else { %>

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
                        <th class="row-header">Number of Relations</td>
                        <td class="discourse-name"><%=numRelations %></td>
                   </tr>

            </table>
            <% } // !isRemote %>
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
<%@ include file="/help/report-level/discourse-overview.jspf" %>
</div>
