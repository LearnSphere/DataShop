<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 10435 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearchGoalsContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearcherTypeDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.ResearchGoalDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.RgPaperDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.research.RgPaperWithGoalsDto"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Research Goals";
    showHelpButton = false;
    cssIncludes.add("ResearchGoalsEdit.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ResearchGoalsEdit.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    ResearchGoalsContext toolContext = ResearchGoalsContext.getContext(request);

    List<ResearcherTypeDto> typeList =
            (List<ResearcherTypeDto>)request.getAttribute(ResearcherTypeDto.ATTRIB_LIST);
    List<ResearchGoalDto> goalList =
            (List<ResearchGoalDto>)request.getAttribute(ResearchGoalDto.ATTRIB_LIST);
    List<RgPaperWithGoalsDto> papersWithGoalsList =
            (List<RgPaperWithGoalsDto>)request.getAttribute(RgPaperWithGoalsDto.ATTRIB_LIST);
    List<RgPaperDto> papersWithoutGoalsList =
            (List<RgPaperDto>)request.getAttribute(RgPaperDto.ATTRIB_LIST);
    
    boolean isLoggedIn = remoteUser != null ? true : false;
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="research-goals-edit-page-div">
  
    <div id="researcher-types-edit-div" class="">
        <h1>Researcher Types&nbsp;&nbsp;<a class="rt-button-link" href="javascript:rtOpenTypeDialog();">add</a></h1>
        <%
        if (typeList != null && typeList.size() > 0) { %>
        <table id="researcher-types-table">
        <%
            for (ResearcherTypeDto typeDto : typeList) { 
            Integer typeId = typeDto.getId();
            Integer numGoals = typeDto.getNumberOfGoals();
            %>
            <tr>
            <td class="col1"><%=typeDto.getOrder()%>.</td>
            <td class="col2">
                <span id="rt_view_span_<%=typeId%>">
                    <span id="rt_view_field_<%=typeId%>"><%=typeDto.getLabel()%></span>
                    &nbsp;&nbsp;
                    <span id="rt_view_edit_link_<%=typeId%>">
                        <a class="rt-basic-link"
                            href="javascript:rtEditType(<%=typeId%>)">edit</a>
                    </span>
                </span>
                <span id="rt_edit_span_<%=typeId%>" style="display:none">
                    <input id="rt_field_<%=typeId%>"
                           class="rtLabelField" name="rtLabelField"
                           size="30" value="<%=typeDto.getLabel()%>">
                    <input type="button" id="rt_save_button_<%=typeId%>"
                           value="Save" onclick="javascript:rtSaveEditType(<%=typeId%>)" />
                    <input type="button" id="rt_cancel_button_<%=typeId%>"
                           value="Cancel" onclick="javascript:rtCancelEditType(<%=typeId%>)" />
                    <input type="hidden" id="rt_hidden_field_<%=typeId%>"
                           value="<%=typeDto.getLabel()%>" />
                </span>
            </td>
            <td class="col3">
                <a id="rt_show_goals_link_<%=typeId%>" class="rt-basic-link"
                    href="javascript:rtShowGoals(<%=typeId%>)">show goals (<%=numGoals%>)</a> 
                <a id="rt_hide_goals_link_<%=typeId%>" class="rt-basic-link"
                    href="javascript:rtHideGoals(<%=typeId%>)" style="display:none">hide goals</a> 
                &nbsp;&nbsp;
                <span id="rt_delete_span_<%=typeId%>">
                    <span id="rt_delete_link_span_<%=typeId%>">
                        <a id="rt_delete_link_<%=typeId%>" class="rt-basic-link"
                           href="javascript:rtShowDeleteAreYouSure(<%=typeId%>)">delete</a>
                    </span>
                    <span id="rt_delete_sure_span_<%=typeId%>" style="display:none" class="rt_delete_sure_span" >
                        delete this researcher type?&nbsp;
                        <a id="rt_delete_link_no_<%=typeId%>"
                         href="javascript:rtHideDeleteAreYouSure(<%=typeId%>)">no</a>
                        &nbsp;&nbsp;
                        <a id="rt_delete_link_yes_<%=typeId%>"
                         href="javascript:rtDeleteType(<%=typeId%>)">yes</a>
                    </span>
                </span>
            </td></tr>
            <tr id="rt_goals_row_<%=typeId%>" style="display: none">
                <td></td>
                <td colspan="2" id="rt_goals_row_td_<%=typeId%>">
                    <table>
                    <td>1.</td>
                    <td>This is a 3-cell hidden row for the goals.</td>
                    <td><a href="#">remove</a></td>
                    </table>
                    <p><a href="#">Add goal</a></p>
                </td>
            </tr>
            <% } %>
            </table>
        <% } else { %>
            <p>There are no researcher types.</p>
        <% } %>
        
    </div>
    
    <div id="research-goals-edit-div" class="">
        <h1>Research Goals&nbsp;&nbsp;<a class="rt-button-link" href="javascript:rgAdd()">add</a></h1>
        <%
        if (goalList != null && goalList.size() > 0) { %>
            <table id="research-goals-table">
            <%
            for (ResearchGoalDto goalDto : goalList) { 
                Integer goalId = goalDto.getId();
                Integer numPapers = goalDto.getNumberOfPapers();
            %>
            <tr>
            <td class="col1"><%=goalDto.getOrder()%>.</td>
            <td class="col2">
                <span id="rg_view_span_<%=goalId%>" class="rgEditTitle">
                    <span id="rg_view_field_<%=goalId%>" class="rgEditTitle"><%=goalDto.getTitle()%></span>
                    &nbsp;&nbsp;
                    <span id="rg_view_edit_link_<%=goalId%>">
                        <a class="rt-basic-link" href="javascript:rgEditGoal(<%=goalId%>)">edit</a>
                    </span>
                </span>
                <span id="rg_edit_span_<%=goalId%>" style="display:none">
                    <input id="rg_field_<%=goalId%>"
                           class="rgLabelField" name="rgLabelField"
                           size="50" value="<%=goalDto.getTitle()%>">
                    <input type="button" id="rg_save_button_<%=goalId%>"
                           value="Save" onclick="javascript:rgSaveEditGoal(<%=goalId%>)" />
                    <input type="button" id="rg_cancel_button_<%=goalId%>"
                           value="Cancel" onclick="javascript:rgCancelEditGoal(<%=goalId%>)" />
                    <input type="hidden" id="rg_hidden_field_<%=goalId%>"
                           value="<%=goalDto.getTitle()%>" />
                </span> 
            </td>
            <td class="col3">
                <a id="rg_show_papers_link_<%=goalId%>" class="rt-basic-link"
                   href="javascript:rgShowPapers(<%=goalId%>)">show papers (<%=numPapers%>)</a>
                <a id="rg_hide_papers_link_<%=goalId%>" class="rt-basic-link"
                   href="javascript:rgHidePapers(<%=goalId%>)" style="display:none">hide papers</a>
                &nbsp;
                <a id="rg_show_types_link_<%=goalId%>" class="rt-basic-link" 
                   href="javascript:rgShowTypes(<%=goalId%>)">show types</a>
                <a id="rg_hide_types_link_<%=goalId%>" class="rt-basic-link" 
                   href="javascript:rgHideTypes(<%=goalId%>)" style="display:none">hide types</a>
                &nbsp;
                <span id="rg_delete_span_<%=goalId%>">
                    <span id="rg_delete_link_span_<%=goalId%>">
                        <a id="rg_delete_link_<%=goalId%>" class="rt-basic-link"
                           href="javascript:rgShowDeleteAreYouSure(<%=goalId%>)">delete</a>
                    </span>
                    <span id="rg_delete_sure_span_<%=goalId%>" style="display:none" class="rt_delete_sure_span" >
                        delete this research goal?&nbsp;
                        <a id="rg_delete_link_no_<%=goalId%>"
                         href="javascript:rgHideDeleteAreYouSure(<%=goalId%>)">no</a>
                        &nbsp;&nbsp;
                        <a id="rg_delete_link_yes_<%=goalId%>"
                         href="javascript:rgDeleteGoal(<%=goalId%>)">yes</a>
                    </span>
                </span>
            </td>
            </tr>
            
            <tr><td></td>
            <td class="col4">
                <span id="rg_view_desc_span_<%=goalId%>" class="rgEditDesc"><%=goalDto.getDescription()%></span>
                <span id="rg_edit_desc_span_<%=goalId%>" style="display:none">
                    <textarea rows="10" cols="80"
                              id="rg_desc_field_<%=goalId%>"
                              class="rgLabelField" name="rgLabelField"><%=goalDto.getDescription()%></textarea>
                    <input type="button" id="rg_save_desc_button_<%=goalId%>"
                           value="Save" onclick="javascript:rgSaveEditGoalDesc(<%=goalId%>)" />
                    <input type="button" id="rg_cancel_desc_button_<%=goalId%>"
                           value="Cancel" onclick="javascript:rgCancelEditGoalDesc(<%=goalId%>)" />
                </span> 
            </td>
            <td>
                <a class="rt-basic-link" href="javascript:rgEditGoalDesc(<%=goalId%>)">edit desc</a>
            </td>
            </tr>
            
            <tr id="rg_papers_title_<%=goalId%>" style="display:none"><td></td><td class="rt-basic-title">Papers</td></tr>
            <tr id="rg_papers_row_<%=goalId%>" style="display:none">
            </tr>
            
            <tr id="rg_types_title_<%=goalId%>" style="display:none"><td></td><td class="rt-basic-title">Researcher Types</td></tr>
            <tr id="rg_types_row_<%=goalId%>" style="display:none">
            </td>
            </tr>

            <% } %>
            </table>
        <% } else { %>
            <p>There are no research goals.</p>
        <% } %>
    </div>
    
    <div id="papers-with-goals-div" class="">
        <h1>Papers associated with research goals</h1>
        
        <%
        if (papersWithGoalsList != null && papersWithGoalsList.size() > 0) {
        %>
        <table id="papers-with-goals-table">
        <%
            int count = 1;
            for (RgPaperWithGoalsDto pwgDto : papersWithGoalsList) { 
                RgPaperDto paperDto = pwgDto.getPaperDto();
                Integer dsId = paperDto.getDatasetId();
                String dsName = paperDto.getDatasetName();
                Integer paperId = paperDto.getPaperId();
                Long numG = pwgDto.getGoalCount();
                String downloadPaper = "DownloadPaper?fileName="
                        + paperDto.getFileName()
                        + "&fileId="
                        + paperDto.getFileId()
                        + "&datasetId="
                        + dsId;
                String citation = paperDto.getCitation().trim();
                if (citation == null || citation.length() == 0) {
                    citation = "[blank citation]";
                } 
            %>
            <tr>
              <td class="col1"><%=count%>.</td>
              <td class="col2"><%=citation%>&nbsp;&nbsp;<a href="<%=downloadPaper%>" class="rt-basic-link">download</a></td>
              <td class="col3"><a href="DatasetInfo?datasetId=<%=dsId%>"><%=dsName%></a></td>
              <td class="col4">
              <a id="papers_goals_show_link_<%=paperId%>" class="rt-basic-link"
                 href="javascript:paperGetGoals(<%=dsId%>,<%=paperId%>)">show goals (<%=numG%>)</a>
              <a id="papers_goals_hide_link_<%=paperId%>" class="rt-basic-link"
                 href="javascript:paperHideGoals(<%=dsId%>,<%=paperId%>)" style="display:none">hide goals</a>
              </td>
            </tr>
            <tr style="display: ">
            <tr id="papers_goals_row_<%=paperId%>" style="display:none">
            </tr>
            <% 
                count++; 
            } // for loop %>
           </table>
        <% } else { %>
            <p>There are no papers with research goals.</p>
        <% } %>
    </div>
    
    <div id="papers-without-goals-div" class="">
        <h1>Other papers</h1>
        
        <%
        if (papersWithoutGoalsList != null && papersWithoutGoalsList.size() > 0) {
        %>
        <table id="papers-without-goals-table">
        <%
            int count = 1;
            for (RgPaperDto paperDto : papersWithoutGoalsList) {
                Integer dsId = paperDto.getDatasetId();
                String dsName = paperDto.getDatasetName();
                Integer paperId = paperDto.getPaperId();
                String citation = paperDto.getCitation().trim();
                if (citation == null || citation.length() == 0) {
                    citation = "[blank citation]";
                } 
                String downloadPaper = "DownloadPaper?fileName="
                        + paperDto.getFileName()
                        + "&fileId="
                        + paperDto.getFileId()
                        + "&datasetId="
                        + dsId;
                %>
            <tr>
              <td class="col1"><%=count%>.</td>
              <td class="col2"><%=citation%>&nbsp;&nbsp;<a href="<%=downloadPaper%>" class="rt-basic-link">download</a></td>
              <td class="col3"><a href="DatasetInfo?datasetId=<%=dsId%>"><%=dsName%></a></td>
              <td class="col4"><a class="rt-basic-link" href="javascript:paperAddGoals(<%=dsId%>,<%=paperId%>)">add goals</a></td>
            </tr>
            <% 
            count++;
            } %>
        </table>
        <% } else { %>
            <p>There are no papers without research goals.</p>
        <% } %>
    </div>

</div> <!-- End #research-goals-page-div div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Un-populated div needed for modal windows using jQuery -->
<div id="rtAddTypeDialog" class="rtDialog"></div>
<div id="rtAddGoalDialog" class="rtDialog"></div>
<div id="rgAddGoalDialog" class="rtDialog"></div>
<div id="paperAddGoalsDialog" class="rtDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/external-tool-table.jspf" %>
</div>

</body>
</html>
