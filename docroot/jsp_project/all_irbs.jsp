<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 10663 $
// Last modified by: $Author: bleber $
// Last modified on: $Date: 2014-02-27 11:41:22 -0500 (Thu, 27 Feb 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbReviewServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbProjectDto"%>

<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "All IRBs";
    cssIncludes.add("help.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ProjectPage.js");
    jsIncludes.add("javascript/IRBs.js");
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
        String searchBy = irbContext.getAllIRBsSearchBy();
        String sortByColumn = irbContext.getAllIRBsSortByColumn();
        Boolean isAscending = irbContext.isAllIRBsAscending(sortByColumn);

        List<IrbDto> dtoList = (List<IrbDto>)request.getAttribute(IrbReviewServlet.ALL_IRBS_ATTR);

        // Determine if the 'Edit IRB' dialog was open before redirect brought us here...
        Integer irbIdOpenForEdit = (Integer)session.getAttribute(IrbServlet.EDIT_IRB_ID_ATTR);
        if (irbIdOpenForEdit == null) { irbIdOpenForEdit = 0; }

        // Now that we've read the value, clear it.
        session.setAttribute(IrbServlet.EDIT_IRB_ID_ATTR, null);

        // For this jsp, the 'message' and 'messageLevel' are on the session.
        String message = (String)session.getAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_ATTR);
        String messageLevel = (String)session.getAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_LEVEL_ATTR);

        // Now that we've read the values, clear them.
        session.setAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_ATTR, null);
        session.setAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_LEVEL_ATTR, null);

%>
<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">
     
        <div id="all-irbs-div">
             
             <div>
                 <h2>All IRBs</h2>
             </div>
             
             <%
             if (message != null && messageLevel != null ) {
                if (messageLevel.compareTo(IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS) == 0) {
             %>
                <script type="text/javascript">successPopup("<%=message%>");</script>
             <%
                } else if (messageLevel.compareTo(IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR) == 0) {
             %>
                <script type="text/javascript">errorPopup("<%=message%>");</script>
             <%
                } else {
             %>
                <script type="text/javascript">messagePopup("<%=message%>", "WARNING");</script>
             <% }
             }
             %>

             <div id="allIrbSearchDiv">
             <%
             if (searchBy.trim().equals("")) {
             %>
                <input type="text" id="all_irb_search_by" name="all_irb_search_by" size="30"
                    title="Search by IRB title or PI name" />
                    <img id="all_irb_search_button" name="all_irb_search_button" src="images/magnifier.png" />
             <%
             } else {
             %>
                <input type="text" id="all_irb_search_by" name="all_irb_search_by"
                    size="30" value="<%=searchBy %>" /> <img id="all_irb_search_button"
                    name="all_irb_search_button" src="images/magnifier.png" />
             <%
             }
             %>
             </div>
             
             <div id="addIrbButton" name="addIrbButton">
             <p><a href="#" id="all_irb_add_irb" class="ui-state-default ui-corner-all" name="all_irb_add_irb">
             <span class="ui-icon ui-icon-newwin"></span>Add an IRB</a></p>
             </div>

             <table id="allIRBsTable">
             <colgroup>
                <col style="width:10%" />
                <col style="width:30%" />
                <col style="width:14%" />
                <col style="width:10%" />
                <col style="width:10%" />
                <col style="width:10%" />
                <col style="width:8%" />
                <col style="width:8%" />
                <col style="width:8%" />
                <col style="width:8%" />
             </colgroup>
             <tr>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_NUMBER%>')">
                    <%=IrbDto.COLUMN_NUMBER %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_NUMBER, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_TITLE%>')">
                    <%=IrbDto.COLUMN_TITLE %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_TITLE, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_PI%>')">
                    <%=IrbDto.COLUMN_PI %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_PI, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_APPROVAL%>')">
                    <%=IrbDto.COLUMN_APPROVAL %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_APPROVAL, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_EXPIRATION%>')">
                    <%=IrbDto.COLUMN_EXPIRATION %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_EXPIRATION, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_INSTITUTION%>')">
                    <%=IrbDto.COLUMN_INSTITUTION %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_INSTITUTION, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_PROJECTS%>')">
                    <%=IrbDto.COLUMN_PROJECTS %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_PROJECTS, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_FILES%>')">
                    <%=IrbDto.COLUMN_FILES %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_FILES, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_UPDATED_BY%>')">
                    <%=IrbDto.COLUMN_UPDATED_BY %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_UPDATED_BY, sortByColumn, isAscending) %>" />
                </th>
                <th>
                    <a href="javascript:sortAllIRBs('<%=IrbDto.COLUMN_UPDATED_DATE%>')">
                    <%=IrbDto.COLUMN_UPDATED_DATE %></a>
                    <img src="<%=IrbDto.getSortImage(IrbDto.COLUMN_UPDATED_DATE, sortByColumn, isAscending) %>" />
                </th>
             </tr>
             <%
                if (dtoList != null && dtoList.size() > 0) {
                   for (Iterator iter = dtoList.iterator(); iter.hasNext(); ) {
                       IrbDto dto = (IrbDto)iter.next();
                       int numFiles = dto.getFileList().size();
                       String udDateStr = dto.getUpdatedDateString();
                       String udTimeStr = dto.getUpdatedTimeString();
             %>
                       <tr>
                           <td><span id="pnField_<%=dto.getId()%>"><%=dto.getProtocolNumber()%></span></td>
                           <td><span>
                                <a href="javascript:openEditIRBDialog(<%=dto.getId()%>)"><%=dto.getTitle()%></a>
                           </span></td>
                           <td><span id="piField_<%=dto.getId()%>"><%=dto.getPiName()%></span></td>
                           <td><span id="adField_<%=dto.getId()%>"><%=dto.getApprovalDateString()%></span></td>
                           <td><span id="edField_<%=dto.getId()%>"><%=dto.getExpirationDateString()%></span></td>
                           <td><span id="giField_<%=dto.getId()%>"><%=dto.getGrantingInstitutionString()%></span></td>
                           <td><span><%=dto.getNumProjects()%></span></td>
                           <td><span id="numFilesField_<%=dto.getId()%>"><%=numFiles%></span></td>
                           <td><span id="ubField_<%=dto.getId()%>"><%=dto.getUpdatedByString()%></span></td>
                           <td><span id="udField_<%=dto.getId()%>" title="<%=udTimeStr%>"><%=udDateStr%></span></td>
                       </tr>
             <%
                   }
                }
             %>
             </table>

             <!-- make note of attrs needed by javascript -->
             <input type="hidden" id="adminUserFlag" value="<%=adminUserFlag%>" />
             <input type="hidden" id="currentUser" value="<%=remoteUser%>" />
             <input type="hidden" id="editIRBIsOpen" value="<%=irbIdOpenForEdit%>" />

        </div> <!-- all-irbs-div -->
        </div>  <!-- contents div -->
    </td>
</tr>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="editIRBDialog" class="editIRBDialog"></div>
<div id="addIRBFileDialog" class="addIRBFileDialog"></div>
<div id="addIRBDialog" class="addIRBDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

</body>
</html>

