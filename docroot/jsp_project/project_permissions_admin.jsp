<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 10884 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-04-08 15:31:45 -0400 (Tue, 08 Apr 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPermissionsContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPermissionsDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPermissionsServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.dto.*"%>
<%@page import="edu.cmu.pslc.datashop.util.*"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.*"%>

<!-- project-page-specific header stuff -->
<%@ include file="/jsp_project/project_header.jspf"
%>
<%
int rowsPerPagesOptions[] = {10, 20, 30, 50, 75, 100};
ProjectPermissionsContext permContext = ProjectPermissionsContext.getContext(request);
String currentTab = permContext.getCurrentTab();
String showAdmins = permContext.getShowAdmins() ? "checked" : "";
String rfaSortByColumn = permContext.getRequestsForAccessSortByColumn();
Boolean isRfaAscending = permContext.isRequestsForAccessAscending(rfaSortByColumn);
String cpSearchBy = permContext.getCurrentPermissionsSearchBy();
String cpSortByColumn = permContext.getCurrentPermissionsSortByColumn();
Boolean isCpAscending = permContext.isCurrentPermissionsAscending(cpSortByColumn);
int cpRowsPerPage = permContext.getCurrentPermissionsRowsPerPage();
int cpCurrentPage = permContext.getCurrentPermissionsCurrentPage();
String arSearchBy = permContext.getAccessReportSearchBy();
String arSortByColumn = permContext.getAccessReportSortByColumn();
Boolean isArAscending = permContext.isAccessReportAscending(arSortByColumn);
int arRowsPerPage = permContext.getAccessReportRowsPerPage();
int arCurrentPage = permContext.getAccessReportCurrentPage();

ProjectPermissionsDto permissionsDto =
    (ProjectPermissionsDto)request.getAttribute(ProjectPermissionsServlet.PROJECT_PERMISSIONS_ATTR + projectId);

String message = permissionsDto.getMessage();
String messageLevel = permissionsDto.getMessageLevel();
List<ProjectRequestDTO> requestsForAccess = permissionsDto.getRequestsForAccess();
List<ProjectRequestDTO> accessReport = permissionsDto.getAccessReport();
List<ProjectRequestDTO> currentPermissions = permissionsDto.getCurrentPermissions();

boolean hasTou = permissionsDto.getHasTermsOfUse();

int arNumRecords = permissionsDto.getAccessReportNumRecords();
int arNumRecordsTotal = permissionsDto.getAccessReportNumRecordsTotal();
int arNumPages = permissionsDto.getAccessReportNumPages();
int cpNumRecords = permissionsDto.getCurrentPermissionsNumRecords();
int cpNumRecordsTotal = permissionsDto.getCurrentPermissionsNumRecordsTotal();
int cpNumPages = permissionsDto.getCurrentPermissionsNumPages();

// After form submissions, the info is on the session.
if ((message == null) && (messageLevel == null)) {
   ProjectPermissionsDto tmpDto = (ProjectPermissionsDto)
       session.getAttribute(ProjectPermissionsServlet.PROJECT_PERMISSIONS_ATTR + projectId);
   if (tmpDto != null) {
       message = tmpDto.getMessage();
       messageLevel = tmpDto.getMessageLevel();
   }

   // Reset once read.
   session.setAttribute(ProjectPermissionsServlet.PROJECT_PERMISSIONS_ATTR + projectId, null);
}

%>
     <div id="project-permissions-div">
        <input type="hidden" id="perm_project_id" value="<%=projectId %>" />
        <input type="hidden" id="perm-current-tab" value="<%=currentTab %>" />

        <% if (message != null && messageLevel != null ) {
           if (messageLevel.compareTo(ProjectPermissionsDto.STATUS_MESSAGE_LEVEL_SUCCESS) == 0) { %>
            <script type="text/javascript">successPopup("<%=message%>");</script>
        <% } else if (messageLevel.compareTo(ProjectPermissionsDto.STATUS_MESSAGE_LEVEL_ERROR) == 0) { %>
            <script type="text/javascript">errorPopup("<%=message%>");</script>
        <% } else { %>
            <script type="text/javascript">warningPopup("<%=message%>");</script>
        <% }
        } %>
            <% if (srsMessage.equals(ProjectHelper.SRS_MSG_PUBLIC)) { %>
            <div name="srsMessageDiv" class="information-box srsMessagePublic">
                <p><img src="images/users.gif" id="public-icon-img" alt="(public)" title="This is a public project." />
                <%=srsMessage %></p>
            </div>
            <% } else { %>
            <div name="srsMessageDiv" class="information-box srsMessage">
                <p><%=srsMessage %></p>
                <p>See the <a href="ProjectIRB?id=<%=projectId%>">IRB tab</a>, or contact the 
                <a id="srsMsgRmEmailLink" href="mailto:<%=rmEmail%>">Research Manager</a>
                for more information.</p>
            </div>
            <% } %>

            <div id="perm-project-public-button">
            <%=makeProjectPublicButton %>
            </div>
            
        <div id="perm-requests-for-access-div">
         
            <div class="label">Requests for Access</div>
<%
   if (requestsForAccess.size() > 0) {
%>
              <div id="requests-for-access-table">
              <table id="perm-requests-for-access" class="dataset-box">
              <thead>
                <tr>
                  <th>
                      <a href="javascript:sortRequestsForAccess(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_USER %>')">
                      <%=ProjectRequestDTO.COLUMN_USER %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(rfaSortByColumn,
                           ProjectRequestDTO.COLUMN_USER, isRfaAscending) %>" />
                  </th>
                  <th>
                      <a href="javascript:sortRequestsForAccess(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_INSTITUTION %>')">
                      <%=ProjectRequestDTO.COLUMN_INSTITUTION %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(rfaSortByColumn,
                           ProjectRequestDTO.COLUMN_INSTITUTION, isRfaAscending) %>" />
                  </th>
                  <th>
                      <a href="javascript:sortRequestsForAccess(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LEVEL %>')">
                      <%=ProjectRequestDTO.COLUMN_LEVEL %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(rfaSortByColumn,
                           ProjectRequestDTO.COLUMN_LEVEL, isRfaAscending) %>" />
                  </th>
                  <th>
                      <a href="javascript:sortRequestsForAccess(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_STATUS %>')">
                      <%=ProjectRequestDTO.COLUMN_STATUS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(rfaSortByColumn,
                           ProjectRequestDTO.COLUMN_STATUS, isRfaAscending) %>" />
                  </th>
                  <th>
                      <a href="javascript:sortRequestsForAccess(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LASTACTIVITYDATE %>')">
                      <%=ProjectRequestDTO.COLUMN_LASTACTIVITYDATE %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(rfaSortByColumn,
                           ProjectRequestDTO.COLUMN_LASTACTIVITYDATE, isRfaAscending) %>" />
                  </th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
<%
   } else {
%>
           <span class="emptyPermissionsListSpan">There are no outstanding requests for access to this project.</span>
<%
   } 
%>
<% 
   int rowCount = 0;
   for (ProjectRequestDTO dto : requestsForAccess)  {
%>
            <%
                String userField = "";

                    if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                      userField = "Public";
                    } else if (dto.getUserFullName().trim().equals("")) {
                      userField = dto.getUserId();
                    } else {
                      userField = dto.getUserFullName();
                    }

                    if (!dto.getEmail().equals("")) {
                       userField = "<a href=\"mailto:" + dto.getEmail() + "\">"
                         + userField + "</a>";
                    }
                    if ((!dto.getUserId().equals(UserItem.DEFAULT_USER)) &&
                        (!dto.getUserFullName().trim().equals(""))) {
                       userField += "<br/>" + dto.getUserId();
                    }
            %>

                <tr>
                  <td><%= userField %></td>
                  <td><%= StringUtils.defaultString(dto.getInstitution(), "") %></td>
                  <td><%= AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%></td>
                  <td><%= AccessRequestStatusItem.arStringEquivalent(dto.getStatus(), false)%></td>
                  <td><%= dto.getLastActivityString() %></td>
                  <%
                  if (!dto.isButtonVisible()) {
                  %>
                  <td></td>
                  <%
                  } else {
                    String responseButtonId = "responseId_" + projectId;
                    String rowId = responseButtonId + "_" + rowCount;
                    String buttonTxt = "Respond";
                    if (dto.getUserId().equals(UserItem.DEFAULT_USER)) { buttonTxt = "Vote"; }
                  %>
                  <input type="hidden" id="<%=rowId %>" class="request-access" name="userId" value="<%=dto.getUserId() %>" /></td>
                  <input type="hidden" id="<%=rowId %>" class="request-access" name="projectId" value="<%=projectId %>" /></td>
                  <td>
                      <div id="<%=rowId %>" name="responseButtonDiv" class="responseButton">
                        <p><a href="#" id="<%=rowId %>" name="<%=rowId %>"
                              class="response_link request-access ui-state-default-blue ui-corner-all-blue" >
                        <span class="ui-icon ui-icon-blue ui-icon-newwin"></span><%=buttonTxt %></a></p>
                      </div>
                  </td>
                  <%
                  }
                  %>
            </tr>
<% 
   rowCount++;
   } 
%>
<%
   if (requestsForAccess.size() > 0) {
%>
              </tbody>
              </table>
              </div>
<%
   }
%>
         </div>
         <div id="perm-add-new-user-div">
              <input type="hidden" id="project-perm-dp" value="<%=dpName %>" />
              <div class="label addNewUser">Add New User</div>
              <div id="add-new-user-input-div">
                   <input type="text" id="add-new-user-field" name="add-new-user-field" size="30"
                          title="Enter username" />
                   <select id="add-new-user-level" name="add-new-user-level">
                           <option value="<%=AuthorizationItem.LEVEL_VIEW%>" selected>
                                   <%=AccessRequestStatusItem.arStringEquivalent(AuthorizationItem.LEVEL_VIEW, false)%>
                           </option>
                           <option value="<%=AuthorizationItem.LEVEL_EDIT%>">
                                   <%=AccessRequestStatusItem.arStringEquivalent(AuthorizationItem.LEVEL_EDIT, false)%>
                           </option>
                           <option value="<%=AuthorizationItem.LEVEL_ADMIN%>">
                                   <%=AccessRequestStatusItem.arStringEquivalent(AuthorizationItem.LEVEL_ADMIN, false)%>
                           </option>
                   </select>
                   <input type="button" id="add-new-user-button"
                          value="Add" onclick="javascript:requestNewUserAccess(<%=projectId%>)" disabled />
                   <a id="view-level-descriptions" href="javascript:viewLevelDescriptions()">view level descriptions table</a>
              </div>
         </div>

        <form id="AccessReportFilterForm" name="AccessReportFilterForm"  method="get" action="?id=<%=projectId%>" >
        <input type="hidden" name="id" value="<%=projectId%>" />
        <div id="permissionTabsDiv">
        <p id="currentPermissionsTab">
           <a id="currentPermissionsLink" href="javascript:showCurrentPermissions(<%=projectId%>)">
              <%=ProjectPermissionsContext.CURRENT_PERMISSIONS_TAB%></a>
        </p>  
        <p id="spacer"> | </p>
        <p id="accessReportTab">
           <a id="accessReportLink" href="javascript:showAccessReport(<%=projectId%>)">
              <%=ProjectPermissionsContext.ACCESS_REPORT_TAB%></a>
        </p>
        </div>

        <div id="accessReportDiv">

        <div id="access-report-filter-options">
        <table id="permFilterOptionsTable">
        <tr>
            <td>
                <% if (arSearchBy.trim().equals("")) { %>
                   <input type="text" id="perm_ar_search_by" name="perm_ar_search_by" size="30"
                          title="Search by user name or id" />
                   <img id="perm_ar_search_button" name="perm_ar_search_button" src="images/magnifier.png" />
                <% } else { %>
                   <input type="text" id="perm_ar_search_by" name="perm_ar_search_by"
                          size="30" value="<%=arSearchBy %>" />
                   <img id="perm_ar_search_button" name="perm_ar_search_button" src="images/magnifier.png" />
                <% } %>
            </td>
            <td class="rows-per-page">Rows per page: 
            <select name="perm_ar_rows_per_page" id="perm_ar_rows_per_page">
                <%
                for (int nppOpt: rowsPerPagesOptions) {
                    String selected = (arRowsPerPage == nppOpt) ? "selected=\"selected\"" : "";
                %>
                    <option value="<%=nppOpt %>" <%=selected %>><%=nppOpt %></option>
                <%
                }
                %>
            </select>
            </td>
            <td>
                <table id="pageCountTable">
                    <tr>
                    <% if (arCurrentPage > 1 && arNumPages > 1) { %>
                       <td><a href="javascript:gotoAccessReport(1)"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoAccessReport(<%=arCurrentPage - 1%>)"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                    <% } else {%>
                       <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                       <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                    <% } %>

                    <% if (arCurrentPage < arNumPages && arNumPages > 1) { %>
                       <td><a href="javascript:gotoAccessReport(<%=arCurrentPage + 1%>)">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoAccessReport(<%=arNumPages%>)">Last <img src="images/grid/forward_grey.gif" /></a></td>
                    <% } else {%>
                       <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                       <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                    <% } %>
                    </tr>
                </table> <!-- pageCountTable  -->
            </td>
            <%
                int firstIndex = (arCurrentPage - 1) * arRowsPerPage;
                int lastIndex = arNumRecords;
                if (arNumRecords > firstIndex + arRowsPerPage) { lastIndex = firstIndex + arRowsPerPage; }
                if (arNumRecords == 0) { firstIndex = -1; }  // special case; empty table
            %>
            <td class="row-range"><%=firstIndex + 1 %>-<%=lastIndex %> of <%=arNumRecords %></td>
        </tr>
        <% if (adminUserFlag) { %>
        <tr>
            <td class="show-admins">
                <input type="checkbox" id="perm_ar_show_admins" name="perm_ar_show_admins" <%=showAdmins %>/>
                <label for="perm_ar_show_admins">Show Admins</label>
            </td>
        </tr>
        <% } %>
        </table>
        </div>

<%
   if (accessReport.size() > 0) {
        %>

        <table id="perm-access-report" class="dataset-box">
            <thead>
            <tr>
                <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_USER %>')">
                      <%=ProjectRequestDTO.COLUMN_USER %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_USER, isArAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_INSTITUTION %>')">
                      <%=ProjectRequestDTO.COLUMN_INSTITUTION %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_INSTITUTION, isArAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LEVEL %>')">
                      <%=ProjectRequestDTO.COLUMN_LEVEL %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_LEVEL, isArAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_FIRSTACCESS %>')">
                      <%=ProjectRequestDTO.COLUMN_FIRSTACCESS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_FIRSTACCESS, isArAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LASTACCESS %>')">
                      <%=ProjectRequestDTO.COLUMN_LASTACCESS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_LASTACCESS, isArAscending) %>" />
                </th>
                <% if (hasTou) { %>
                   <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMS %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMS, isArAscending) %>" />
                   </th>
                   <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMSVERSION %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMSVERSION %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMSVERSION, isArAscending) %>" />
                   </th>
                   <th>
                      <a href="javascript:sortAccessReport(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMSDATE %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMSDATE %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(arSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMSDATE, isArAscending) %>" />
                   </th>
                <% } %>
            </tr>
            </thead>
            <tbody>
<% 
   rowCount = 0;
   for (ProjectRequestDTO dto : accessReport)  {%>
            <%
                String userField = "";
                // Don't display edit button if entry corresponds to current user
                boolean isButtonVisible = dto.isButtonVisible();
                
                    if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                      userField = "Public";
                    } else if (dto.getUserFullName().trim().equals("")) {
                      userField = dto.getUserId();
                    } else {
                      userField = dto.getUserFullName();
                    }

                    if (!dto.getEmail().equals("")) {
                       userField = "<a href=\"mailto:" + dto.getEmail() + "\">"
                         + userField + "</a>";
                    }
                    if ((!dto.getUserId().equals(UserItem.DEFAULT_USER)) &&
                        (!dto.getUserFullName().trim().equals(""))) {
                       userField += "<br/>" + dto.getUserId();
                    }
            %>

            <tr>
                <td><%= userField %></td>
                <td><%= StringUtils.defaultString(dto.getInstitution(), "") %></td>
                <td>
                <% if (isButtonVisible) {
                    String responseButtonId = "responseId_" + projectId;
                    String rowId = responseButtonId + "_" + rowCount;
                %>
                   <input type="hidden" id="<%=rowId %>" class="<%=ProjectPermissionsContext.ACCESS_REPORT_TAB %>"
                          name="userId" value="<%=dto.getUserId() %>" />
                   <input type="hidden" id="<%=rowId %>" class="<%=ProjectPermissionsContext.ACCESS_REPORT_TAB %>"
                          name="projectId" value="<%=projectId %>" />
                   <div id="<%=rowId %>" name="modifyAccessDiv" class="modifyAccessDiv">
                        <p><a href="#" id="<%=rowId %>"
                              class="response_link" name="<%=rowId %>" ><%= 
                              AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%>
                              <img src="images/edit.gif"></a>
                        </p>
                   </div>
                <% } else { %>
                   <%= AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%>
                <% } %>
                </td>
                <td><%= dto.getFirstAccessString() %></td>
                <td><%= dto.getLastAccessString() %></td>
                <% if (hasTou) { %>
                   <td><%= dto.getTouName() %></td>
                   <td><%= dto.getTouVersionAgreedString() %></td>
                   <td><%= dto.getTouDateAgreedString() %></td>
                <% } %>
            </tr>
<% 
   rowCount++;
   }
%>
            </tbody>
         </table>

<%
   } else if (arNumRecordsTotal == 0) {
%>
           <span class="emptyPermissionsListSpan">No users have accessed this project.</span>
<%
   } else {
%>
           <span class="emptyPermissionsListSpan">No users match your search criteria.</span>
<%
   }
%>

        </div> <!-- accessReportDiv -->
        <div id="currentPermissionsDiv">

        <div id="access-report-filter-options">
        <table id="permFilterOptionsTable">
        <tr>
            <td>
                <% if (cpSearchBy.trim().equals("")) { %>
                   <input type="text" id="perm_cp_search_by" name="perm_cp_search_by" size="30"
                          title="Search by user name or id" />
                   <img id="perm_cp_search_button" name="perm_cp_search_button" src="images/magnifier.png" />
                <% } else { %>
                   <input type="text" id="perm_cp_search_by" name="perm_cp_search_by"
                          size="30" value="<%=cpSearchBy %>" />
                   <img id="perm_cp_search_button" name="perm_cp_search_button" src="images/magnifier.png" />
                <% } %>
            </td>
            <td class="rows-per-page">Rows per page: 
            <select name="perm_cp_rows_per_page" id="perm_cp_rows_per_page">
                <%
                for (int nppOpt: rowsPerPagesOptions) {
                    String selected = (cpRowsPerPage == nppOpt) ? "selected=\"selected\"" : "";
                %>
                    <option value="<%=nppOpt %>" <%=selected %>><%=nppOpt %></option>
                <%
                }
                %>
            </select>
            </td>
            <td>
                <table id="pageCountTable">
                    <tr>
                    <% if (cpCurrentPage > 1 && cpNumPages > 1) { %>
                       <td><a href="javascript:gotoCurrentPermissions(1)"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoCurrentPermissions(<%=cpCurrentPage - 1%>)"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                    <% } else {%>
                       <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                       <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                    <% } %>

                    <% if (cpCurrentPage < cpNumPages && cpNumPages > 1) { %>
                       <td><a href="javascript:gotoCurrentPermissions(<%=cpCurrentPage + 1%>)">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                       <td><a href="javascript:gotoCurrentPermissions(<%=cpNumPages%>)">Last <img src="images/grid/forward_grey.gif" /></a></td>
                    <% } else {%>
                       <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                       <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                    <% } %>
                    </tr>
                </table> <!-- pageCountTable  -->
            </td>
            <%
                firstIndex = (cpCurrentPage - 1) * cpRowsPerPage;
                lastIndex = cpNumRecords;
                if (cpNumRecords > firstIndex + cpRowsPerPage) { lastIndex = firstIndex + cpRowsPerPage; }
                if (cpNumRecords == 0) { firstIndex = -1; }  // special case; empty table
            %>
            <td class="row-range"><%=firstIndex + 1 %>-<%=lastIndex %> of <%=cpNumRecords %></td>
        </tr>
        <% if (adminUserFlag) { %>
        <tr>
            <td class="show-admins">
                <input type="checkbox" id="perm_cp_show_admins" name="perm_cp_show_admins" <%=showAdmins %>/>
                <label for="perm_cp_show_admins">Show Admins</label>
            </td>
        </tr>
        <% } %>
        </table>
        </div>

<%
   if (currentPermissions.size() > 0) {
        %>

        <table id="perm-current-permissions" class="dataset-box">
            <thead>
            <tr>
                <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_USER %>')">
                      <%=ProjectRequestDTO.COLUMN_USER %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_USER, isCpAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_INSTITUTION %>')">
                      <%=ProjectRequestDTO.COLUMN_INSTITUTION %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_INSTITUTION, isCpAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LEVEL %>')">
                      <%=ProjectRequestDTO.COLUMN_LEVEL %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_LEVEL, isCpAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_FIRSTACCESS %>')">
                      <%=ProjectRequestDTO.COLUMN_FIRSTACCESS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_FIRSTACCESS, isCpAscending) %>" />
                </th>
                <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_LASTACCESS %>')">
                      <%=ProjectRequestDTO.COLUMN_LASTACCESS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_LASTACCESS, isCpAscending) %>" />
                </th>
                <% if (hasTou) { %>
                   <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMS %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMS %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMS, isCpAscending) %>" />
                   </th>
                   <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMSVERSION %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMSVERSION %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMS, isCpAscending) %>" />
                   </th>
                   <th>
                      <a href="javascript:sortCurrentPermissions(<%=projectId %>, '<%=ProjectRequestDTO.COLUMN_TERMSDATE %>')">
                      <%=ProjectRequestDTO.COLUMN_TERMSDATE %></a>
                      <img src="<%=ProjectRequestDTO.showSortOrder(cpSortByColumn,
                           ProjectRequestDTO.COLUMN_TERMS, isCpAscending) %>" />
                   </th>
                <% } %>
            </tr>
            </thead>
            <tbody>
<% 
   rowCount = 0;
   for (ProjectRequestDTO dto : currentPermissions)  {%>
            <%
                String userField = "";
                // Don't display edit button if entry corresponds to current user
                boolean isButtonVisible = dto.isButtonVisible();

                    if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                      userField = "Public";
                    } else if (dto.getUserFullName().trim().equals("")) {
                      userField = dto.getUserId();
                    } else {
                      userField = dto.getUserFullName();
                    }

                    if (!dto.getEmail().equals("")) {
                       userField = "<a href=\"mailto:" + dto.getEmail() + "\">"
                         + userField + "</a>";
                    }
                    if ((!dto.getUserId().equals(UserItem.DEFAULT_USER)) &&
                        (!dto.getUserFullName().trim().equals(""))) {
                       userField += "<br/>" + dto.getUserId();
                    }
            %>

            <tr>
                <td><%= userField %></td>
                <td><%= StringUtils.defaultString(dto.getInstitution(), "") %></td>
                <td>
                <% if (isButtonVisible) {
                    String responseButtonId = "responseId_" + projectId;
                    String rowId = responseButtonId + "_" + rowCount;
                %>
                   <input type="hidden" id="<%=rowId %>" class="<%=ProjectPermissionsContext.CURRENT_PERMISSIONS_TAB %>"
                          name="userId" value="<%=dto.getUserId() %>" />
                   <input type="hidden" id="<%=rowId %>" class="<%=ProjectPermissionsContext.CURRENT_PERMISSIONS_TAB %>"
                          name="projectId" value="<%=projectId %>" />
                   <div id="<%=rowId %>" name="modifyAccessDiv" class="modifyAccessDiv">
                        <p><a href="#" id="<%=rowId %>"
                              class="response_link" name="<%=rowId %>" ><%= 
                              AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%>
                              <img src="images/edit.gif"></a>
                        </p>
                   </div>
                <% } else { %>
                   <%= AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%>
                <% } %>
                </td>
                <td><%= dto.getFirstAccessString() %></td>
                <td><%= dto.getLastAccessString() %></td>
                <% if (hasTou) { %>
                   <td><%= dto.getTouName() %></td>
                   <td><%= dto.getTouVersionAgreedString() %></td>
                   <td><%= dto.getTouDateAgreedString() %></td>
                <% } %>
            </tr>
<%
   rowCount++;
   }
%>
            </tbody>
         </table>

<%
   } else if (cpNumRecordsTotal == 0) {
%>
           <span class="emptyPermissionsListSpan">There are currently no users with permission to access to this project.</span>
<%
   } else {
%>
           <span class="emptyPermissionsListSpan">No users match your search criteria.</span>
<%
   }
%>

        </div> <!-- currentPermissionsDiv -->

    <% if (currentTab.equals(ProjectPermissionsContext.ACCESS_REPORT_TAB)) { %>
           <script type="text/javascript">showAccessReport();</script>
    <% } %>

    </div> <!-- project-permissions-div -->

</div> <!-- project-page div -->
</td>
</tr>

<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="levelDescriptionsDialog" class="levelDescriptionsDialog"> </div>
<div id="projectPublicDialog" class="projectPublicDialog"> </div>
<div id="requestDialog" class="requestDialog"> </div>
<div id="responseDialog" class="responseDialog"> </div>
<div id="voteProjectPublicDialog" class="voteProjectPublicDialog"> </div>

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/project_page_permissions.jspf" %>
</div>

</body>
</html>
