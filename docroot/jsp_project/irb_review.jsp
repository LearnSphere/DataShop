<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 10671 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-02-27 21:08:16 -0500 (Thu, 27 Feb 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbReviewFilter"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbReviewServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectReviewDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.UserDto"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>

<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "IRB Review";
    cssIncludes.add("help.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ProjectPage.js");
    jsIncludes.add("javascript/IRBs.js");
    jsIncludes.add("javascript/IrbReview.js");
    jsIncludes.add("javascript/AccessRequests.js");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("access_requests.css");
    cssIncludes.add("project_page.css");
    cssIncludes.add("message.css");
%>
<!--  header -->
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>
<%
        IrbContext irbContext = IrbContext.getContext(request);
        String searchBy = irbContext.getReviewSearchBy();
        String searchByPiDp = irbContext.getReviewSearchByPiDp();
        IrbReviewFilter filter = irbContext.getReviewFilter();
        String sortByColumn = irbContext.getReviewSortByColumn();
        Boolean isAscending = irbContext.isReviewAscending(sortByColumn);

        List<ProjectReviewDto> dtoList = (List<ProjectReviewDto>)request.getAttribute(IrbReviewServlet.IRB_REVIEW_ATTR);

        String selectedStr = "selected";
        
        //Subject to DS 2012 IRB
        String blankSubjToStr = "";
        String noStr = "";
        String yesStr = "";
        //SRS
        String blankShareStr = "";
        String notSubmitStr = "";
        String waitingStr = "";
        String submitStr = "";
        String shareStr = "";
        String shareNotPublicStr = "";
        String notShareStr = "";
        //DCT
        String blankDCStr = "";
        String notSpecStr = "";
        String notHumanStr = "";
        String consentReqStr = "";
        String consentObtStr = "";
        //Unreviewed Datasets
        String blankDSStr = "";
        String noneStr = "";
        String someStr = "";
        String allStr = "";
        //Public/Private
        String publicBlankStr = "";
        String publicPublicStr = "";
        String publicPrivateStr = "";
        //Project Created
        String pcBeforeStr = "";
        String pcOnStr = "";
        String pcAfterStr = "";
        String pcDateStr = "";
        //Dataset Last Added
        String dlaBeforeStr = "";
        String dlaOnStr = "";
        String dlaAfterStr = "";
        String dlaDateStr = "";
        //Needs Attention
        String needsAttnBlankStr = "";
        String needsAttnYesStr = "";
        String needsAttnNoStr = "";

        if (filter != null) {
           String subjTo = filter.getSubjectTo();
           if (subjTo == null) {
              blankSubjToStr = selectedStr;
           } else if (subjTo.equals("yes")) {
              yesStr = selectedStr;
           } else if (subjTo.equals("no")) {
              noStr = selectedStr;
           } else {
              blankSubjToStr = selectedStr;
           }
           String share = filter.getShareability();
           if (share == null) {
              blankShareStr = selectedStr;
           } else if (share.equals("not_submitted")) {
              notSubmitStr = selectedStr;
           } else if (share.equals("waiting_for_researcher")) {
              waitingStr = selectedStr;
           } else if (share.equals("submitted_for_review")) {
              submitStr = selectedStr;
           } else if (share.equals("shareable")) {
              shareStr = selectedStr;
           } else if (share.equals("shareable_not_public")) {
              shareNotPublicStr = selectedStr;
           } else if (share.equals("not_shareable")) {
              notShareStr = selectedStr;
           } else {
              blankShareStr = selectedStr;
           }
           String dataType = filter.getDataCollectionType();
           if (dataType == null) {
              blankDCStr = selectedStr;
           } else if (dataType.equals("not_specified")) {
              notSpecStr = selectedStr;
           } else if (dataType.equals("not_human_subject")) {
              notHumanStr = selectedStr;
           } else if (dataType.equals("study_data_consent_req")) {
              consentReqStr = selectedStr;
           } else if (dataType.equals("study_data_consent_not_req")) {
              consentObtStr = selectedStr;
           } else {
              blankDCStr = selectedStr;
           }
           String datasets = filter.getUnreviewedDatasets();
           if (datasets == null) {
              blankDSStr = selectedStr;
           } else if (datasets.equals("none")) {
              noneStr = selectedStr;
           } else if (datasets.equals("some")) {
              someStr = selectedStr;
           } else if (datasets.equals("all")) {
              allStr = selectedStr;
           } else {
              blankDSStr = selectedStr;
           }
           //Public
           String publicStr = filter.getPublicStr();
           if (publicStr == null) {
               publicBlankStr = selectedStr;
           } else {
               if (publicStr.equalsIgnoreCase("public")) {
                   publicPublicStr = selectedStr;
               } else {
                   publicPrivateStr = selectedStr;
               }
           }
           //Needs Attention
           Boolean needsAttn = filter.getNeedsAttn();
           if (needsAttn == null) {
               needsAttnBlankStr = selectedStr;
           } else {
               if (needsAttn.equals(Boolean.TRUE)) {
                   needsAttnYesStr = selectedStr;
               } else {
                   needsAttnNoStr = selectedStr;
               }
           }
           //Project Created
           String filterPcBefore = filter.getPcBefore();
           if (filterPcBefore == null) {
              pcBeforeStr = selectedStr;
           } else if (filterPcBefore.equals("on")) {
              pcOnStr = selectedStr;
           } else if (filterPcBefore.equals("after")) {
              pcAfterStr = selectedStr;
           } else {
              pcBeforeStr = selectedStr;
           }
           if (filter.getPcDateStr() != null) {
               pcDateStr = filter.getPcDateStr();
           }
           //Dataset Last Added
           String filterDlaBefore = filter.getDlaBefore();
           if (filterDlaBefore == null) {
              dlaBeforeStr = selectedStr;
           } else if (filterDlaBefore.equals("on")) {
              dlaOnStr = selectedStr;
           } else if (filterDlaBefore.equals("after")) {
              dlaAfterStr = selectedStr;
           } else {
              dlaBeforeStr = selectedStr;
           }
           if (filter.getDlaDateStr() != null) {
               dlaDateStr = filter.getDlaDateStr();
           }
        }
%>
<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

        <div id="irb-review-div">

            <div><h2>IRB Review</h2></div>
             
            <div id="irb-review-filters-div">
            <fieldset>
            <legend>Filters</legend>
                <div id="filter-public-div" class="filter-div-first">
                    <label>Public/Private</label><br />
                    <select name="reviewPublicSelect" id="reviewPublicSelect">
                        <option value="blank" <%=publicBlankStr%>> </option>
                        <option value="public" <%=publicPublicStr%>>Public</option>
                        <option value="private" <%=publicPrivateStr%>>Private</option>
                    </select>
                </div>
                <div id="filter-project-name-div">
                    <label>Project Name</label><br />
                    <% if (searchBy.trim().equals("")) { %>
                        <input type="text" id="irb_review_search_by" name="irb_review_search_by" 
                            size="30" title="Search by project name" /> 
                        <img id="irb_review_search_clear_button" class="irb_review_button" name="irb_review_clear_button"
                            src="images/clear_field.png" style="display:none" />
                    <% } else { %>
                        <input type="text" id="irb_review_search_by" name="irb_review_search_by"
                            size="25" value="<%=searchBy %>" />
                        <img id="irb_review_search_clear_button" class="irb_review_button" name="irb_review_clear_button"
                            src="images/clear_field.png" />
                    <% } %>
                </div>
                <div id="filter-pi-dp-div">
                    <label>PI/Data Provider</label><br />
                    <% if (searchByPiDp.trim().equals("")) { %>
                        <input type="text" id="irb_review_search_by_pi_dp" name="irb_review_search_by_pi_dp" 
                            size="30" title="Search by PI or Data Provider" /> 
                        <img id="irb_review_search_pi_dp_clear_button" class="irb_review_button" name="irb_review_pi_dp_clear_button"
                            src="images/clear_field.png" style="display:none"/>
                    <% } else { %>
                        <input type="text" id="irb_review_search_by_pi_dp" name="irb_review_search_by_pi_dp"
                            size="25" value="<%=searchByPiDp %>" />
                        <img id="irb_review_search_pi_dp_clear_button" class="irb_review_button" name="irb_review_pi_dp_clear_button"
                            src="images/clear_field.png" />
                    <% } %>
                </div>
                <div id="filter-srs-div" class="filter-div-first">
                    <label>Shareability Review Status</label><br />
                    <select name="reviewShareabilitySelect" id="reviewShareabilitySelect">
                        <option value="blank" <%=blankShareStr%>> </option>
                        <option value="not_submitted" <%=notSubmitStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_NOT_SUBMITTED%></option>
                        <option value="waiting_for_researcher" <%=waitingStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_WAITING%></option>
                        <option value="submitted_for_review" <%=submitStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_SUBMITTED%></option>
                        <option value="shareable" <%=shareStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_SHAREABLE%></option>
                        <option value="shareable_not_public" <%=shareNotPublicStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_SHAREABLE_NOT_PUBLIC%> </option>
                        <option value="not_shareable" <%=notShareStr%>><%=ProjectItem.SHAREABLE_STATUS_STR_NOT_SHAREABLE%></option>
                    </select>
                </div>
                <div id="filter-dct-div">
                    <label>Data Collection Type</label><br />
                    <select name="reviewDataTypeSelect" id="reviewDataTypeSelect">
                        <option value="blank" <%=blankDCStr%>> </option>
                        <option value="not_specified" <%=notSpecStr%>>Not specified</option>
                        <option value="not_human_subject" <%=notHumanStr%>>Not human subjects data</option>
                        <option value="study_data_consent_req" <%=consentReqStr%>>Study, consent req'd</option>
                        <option value="study_data_consent_not_req" <%=consentObtStr%>>Study, consent not req'd</option>
                    </select>
                </div>
                <div id="filter-subject-div">
                    <label>Subject to DataShop 2012 IRB</label><br />
                    <select name="reviewSubjToSelect" id="reviewSubjToSelect">
                        <option value="blank" <%=blankSubjToStr%>> </option>
                        <option value="no" <%=noStr%>>No</option>
                        <option value="yes" <%=yesStr%>>Yes</option>
                    </select>
                </div>
                <div id="filter-unreviewed-datasets-div">
                    <label>Unreviewed Datasets</label><br />
                    <select name="reviewDatasetsSelect" id="reviewDatasetsSelect">
                        <option value="blank" <%=blankDSStr%>> </option>
                        <option value="none" <%=noneStr%>>None unreviewed</option>
                        <option value="some" <%=someStr%>>Some unreviewed</option>
                        <option value="all" <%=allStr%>>All unreviewed</option>
                    </select>
                </div>
                <div id="filter-project-created-div" class="filter-div-first">
                    <label>Project Created</label><br />
                    <select name="reviewProjectCreatedSelect" id="reviewProjectCreatedSelect">
                        <option value="before" <%=pcBeforeStr%>>Before</option>
                        <option value="on" <%=pcOnStr%>>On</option>
                        <option value="after" <%=pcAfterStr%>>After</option>
                    </select>
                    <input name="reviewProjectCreatedInput" id="reviewProjectCreatedInput" type="text" size="11" value="<%=pcDateStr%>">
                    <% if (pcDateStr.trim().equals("")) { %>
                    <img id="irb_review_pc_clear_button" class="irb_review_button" name="irb_review_pc_button"
                        src="images/clear_field.png" style="display:none"/>
                    <% } else { %>
                    <img id="irb_review_pc_clear_button" class="irb_review_button" name="irb_review_pc_button"
                        src="images/clear_field.png" />
                    <% }  %>
                </div>
                <div id="filter-dataset-last-added-div">
                    <label>Dataset Last Added</label><br />
                    <select name="reviewDsLastAddedSelect" id="reviewDsLastAddedSelect">
                        <option value="before" <%=dlaBeforeStr%>>Before</option>
                        <option value="on" <%=dlaOnStr%>>On</option>
                        <option value="after" <%=dlaAfterStr%>>After</option>
                    </select>
                    <input name="reviewDsLastAddedInput" id="reviewDsLastAddedInput" type="text" size="11" value="<%=dlaDateStr%>">
                    <% if (dlaDateStr.trim().equals("")) { %>
                    <img id="irb_review_dla_clear_button" class="irb_review_button" name="irb_review_dla_button"
                        src="images/clear_field.png" style="display:none"/>
                    <% } else { %>
                    <img id="irb_review_dla_clear_button" class="irb_review_button" name="irb_review_dla_button"
                        src="images/clear_field.png" />
                    <% }  %>
                </div>
                <div id="filter-needs-attention-div">
                    <label>Needs Attention</label><br />
                    <select name="reviewNeedsAttnSelect" id="reviewNeedsAttnSelect">
                        <option value="blank" <%=needsAttnBlankStr%>> </option>
                        <option value="true" <%=needsAttnYesStr%>>Yes</option>
                        <option value="false" <%=needsAttnNoStr%>>No</option>
                    </select>
                </div>
            </fieldset>
            </div>
            
            <% 
            Integer numProjectsFound = 0;
            if (dtoList != null) {
                numProjectsFound = dtoList.size();
            }
            %>
            <div id="numProjectsFoundDiv"><%=numProjectsFound%> projects found.</div>

             <table id="irbReviewTable">
             <colgroup>
                <col style="width:18%" />
                <col style="width:10%" />
                <col style="width:12%" />
                <col style="width:12%" />
                <col style="width:12%" />
                <col style="width:12%" />
                <col style="width:12%" />
                <col style="width:12%" />
             </colgroup>
             <tr>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_NAME%>')">
                    <%=ProjectReviewDto.COLUMN_NAME %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_NAME, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_DS_IRB%>')">
                    <%=ProjectReviewDto.COLUMN_DS_IRB %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_DS_IRB, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_SHAREABILITY%>')">
                    <%=ProjectReviewDto.COLUMN_SHAREABILITY %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_SHAREABILITY, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_DATA_COLLECTION%>')">
                    <%=ProjectReviewDto.COLUMN_DATA_COLLECTION %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_DATA_COLLECTION, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_UNREVIEWED%>')">
                    <%=ProjectReviewDto.COLUMN_UNREVIEWED %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_UNREVIEWED, sortByColumn, isAscending) %>" />
                </th>
                      <!-- 3 new columns -->
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_PROJ_CREATED%>')">
                    <%=ProjectReviewDto.COLUMN_PROJ_CREATED %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_PROJ_CREATED, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_DS_ADDED%>')">
                    <%=ProjectReviewDto.COLUMN_DS_ADDED %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_DS_ADDED, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortIRBReview('<%=ProjectReviewDto.COLUMN_NEEDS_ATTN%>')">
                    <%=ProjectReviewDto.COLUMN_NEEDS_ATTN %></a>
                    <img src="<%=ProjectReviewDto.getSortImage(ProjectReviewDto.COLUMN_NEEDS_ATTN, sortByColumn, isAscending) %>" />
                </th>
             </tr>
             <%
                if (dtoList != null && dtoList.size() > 0) {
                   for (Iterator iter = dtoList.iterator(); iter.hasNext(); ) {
                       ProjectReviewDto dto = (ProjectReviewDto)iter.next();
                       Integer projId = dto.getProjectId();
                       String needsAttention = "No";
                       if (dto.getNeedsAttention()) {
                           needsAttention = "Yes";
                       } %>
                       <% if (dto.getNeedsAttention() && dto.isPublicFlag()) { %>
                       <tr class="highlightRow">
                       <% } else { %>
                       <tr>
                       <% } %>
                           <td class="projectTd_<%=projId %>">
                                <a id="projectName_<%=projId%>" href="ProjectIRB?id=<%=projId%>"><%=dto.getProjectName()%></a>
                                <% if (dto.isPublicFlag()) { %>
                                <img src="images/users.gif" />
                                <% } %>
                                <% for (UserDto userDto : dto.getUserDtoList()) { %>
                                    <br>
                                    <a class="projectUser" href="mailto:<%=userDto.getEmail()%>"><%=userDto.getName()%></a>
                                <% } %>
                                    <br>
                                    <% if (dto.getNumDatasets() > 0) { %>
                                    <a id="showDatasetsLink_<%=projId%>" class="showHideDatasets" href="javascript:irbReviewShowDatasets(<%=projId%>)">show datasets</a>
                                    <a id="hideDatasetsLink_<%=projId%>" class="showHideDatasets" href="javascript:irbReviewHideDatasets(<%=projId%>)" style="display:none">hide datasets</a>
                                    <% } %>
                           </td>
                           <td class="projectTd_<%=projId %>"><span><%=dto.getSubjectToDsIrbStringShort()%></span></td>
                           <td class="projectTd_<%=projId %>"><span><%=dto.getShareabilityStatusString()%></span></td>
                           <td class="projectTd_<%=projId %>"><span><%=dto.getDataCollectionTypeStringShort()%></span></td>
                           <td class="projectTd_<%=projId %>"><span><%=dto.getNumUnreviewedDatasetsString()%></span></td>
                           <td class="projectTd_<%=projId %>"><span title="<%=dto.getProjectCreatedDateTime()%>"><%=dto.getProjectCreatedDate()%></span></td>
                           <td class="projectTd_<%=projId %>"><span title="<%=dto.getDatasetLastAddedDateTime()%>"><%=dto.getDatasetLastAddedDate()%></span></td>
                           <td class="projectTd_<%=projId %>"><span id="projectNeedsAttentionSpan_<%=projId%>"><%=needsAttention%></span></td>
                       </tr>
                       <tr id="datasetsRow_<%=projId%>" class="hidden datasetsRow">
                       <td colspan=8>
                           <div id="datasetsDiv_<%=projId%>" class="datasetsDiv"></div>
                       </td>
                       </tr>
             <%
                   } // end for loop on projects
                }
             %>
             </table>
        </div> <!-- irb-review-div -->
        </div>  <!-- contents div -->
    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

</body>
</html>

