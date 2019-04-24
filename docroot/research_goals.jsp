<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 13459 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearchGoalsContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearcherTypeDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearchGoalDto"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Research Goals";
    cssIncludes.add("ResearchGoals.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ResearchGoals.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    ResearchGoalsContext toolContext = ResearchGoalsContext.getContext(request);

    Integer typeId = 
            (Integer)request.getAttribute(ResearcherTypeDto.ATTRIB_ID);
    String researcherTypeLabel = 
            (String)request.getAttribute(ResearcherTypeDto.ATTRIB_LABEL);

    List<ResearcherTypeDto> typeList =
            (List<ResearcherTypeDto>)request.getAttribute(ResearcherTypeDto.ATTRIB_LIST);
    List<ResearchGoalDto> goalList =
            (List<ResearchGoalDto>)request.getAttribute(ResearchGoalDto.ATTRIB_LIST);
    
    boolean isLoggedIn = remoteUser != null ? true : false;

    String masterDataShopUrl = "https://pslcdatashop.web.cmu.edu";
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="research-goals-page-div">
    <h1>What can I do with DataShop?</h1>
  
    <div id="researcher-types-div" class="">
    
    <h3 id="txt-1">I'm a</h3>
    <input type="hidden" id="rt_selection" value="<%=typeId%>"/>
    <div id="dd" class="wrapper-dropdown" tabindex="1">
      <span><%=researcherTypeLabel%>&nbsp;</span>
    <%
    if (typeList != null && typeList.size() > 0) {
        %><ul id="rt_dropdown" class="dropdown">
          <li id="rt_li_show_all"><em>Show all topics</em></li>
        <% for (ResearcherTypeDto typeDto : typeList) { %>
            <li id="rt_li_<%=typeDto.getId()%>"><%=typeDto.getLabel()%></li>
        <%
        } // end for loop
        %></ul><%
    } else {
    %>
        <p>There are no researcher types.</p>
    <% } %>
      </div>
    <h3 id="txt-2">and I want to ...</h3>
    </div>
    
    <div id="research-goals-div" class="">

    <%
    if (goalList != null && goalList.size() > 0) {
        %><ul><%
        for (ResearchGoalDto goalDto : goalList) { %>
            <li><a href="javascript:selectResearchGoal(<%=goalDto.getId()%>)"><%=goalDto.getTitle()%></a></li>
        <%
        } // end for loop
        %></ul><%
    } else {
    %>
        <p>There are no research goals.</p>
    <% } %>
    </div>
    <% for (ResearchGoalDto goalDto : goalList) {
        Integer numPapers = goalDto.getNumberOfPapers();
        Integer goalId = goalDto.getId(); %>
        <h2 id="rg_<%=goalId%>"><%=goalDto.getTitle()%></h2>
        <p class="description"><%=goalDto.getDescription()%></p>
        <% if (DataShopInstance.isSlave() && (numPapers == 0)) { %>
           <a id="rg_show_remote_datasets_<%=goalId%>" class="rg_link"
              href="<%=masterDataShopUrl %>/ResearchGoals#rg_<%=goalId%>">Show related datasets and papers on the master instance, DataShop@CMU</a>
           <br>
           <br>
        <% } %>
        <a id="rg_show_datasets_<%=goalId%>" class="rg_link"
           href="javascript:showRelatedDatasetsAndPapers(<%=goalId%>);">Show related datasets and papers</a>
        <a id="rg_hide_datasets_<%=goalId%>" class="rg_link" style="display:none"
           href="javascript:hideRelatedDatasetsAndPapers(<%=goalId%>);">Hide related datasets and papers</a>
        <% if (DataShopInstance.isSlave() && (numPapers > 0)) { %>
           <br>
        <% } %>
        <div id="rg_datasets_div_<%=goalId%>"></div>
        <% if (DataShopInstance.isSlave() && (numPapers > 0)) { %>
           <br>
           <a id="rg_show_remote_datasets_<%=goalId%>" class="rg_link"
              href="<%=masterDataShopUrl %>/ResearchGoals#rg_<%=goalId%>">Show related datasets and papers on the master instance, DataShop@CMU</a>
        <% } %>
    <% } %>

</div> <!-- End #research-goals-page-div div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/research-goals.jspf" %>
</div>

</body>
</html>
