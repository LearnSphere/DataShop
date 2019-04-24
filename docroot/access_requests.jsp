<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Mike Komisin
// Version: $Revision: 11717 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-11-18 15:51:35 -0500 (Tue, 18 Nov 2014) $
// $KeyWordsOff: $
//
%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectRequestDTO.SortParameter"%>
<%@page import="org.apache.commons.collections.comparators.NullComparator"%>

<%@page import="edu.cmu.pslc.datashop.dto.ProjectRequestDTO"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectRequestHistoryDTO"%>
<%@page import="edu.cmu.pslc.datashop.dto.UserRequestDTO"%>

<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestHistoryItem"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>

<%@page import="edu.cmu.pslc.datashop.servlet.accessrequest.AccessReportDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.AbstractServlet"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Access Requests";

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/AccessRequests.js");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("access_requests.css");
%>
<%@ include file="/header.jspf" %>

<!-- code -->
<%
  long notReviewedCount = session.getAttribute(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED) == null ?
        0 : (Long)(session.getAttribute(AccessRequestStatusItem.STATE_COUNT_NOT_REVIEWED));
  long recentActivityCount = session.getAttribute(AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY) == null ?
        0 : (Long)(session.getAttribute(AccessRequestStatusItem.STATE_COUNT_RECENT_ACTIVITY));
  long myRequestsCount = session.getAttribute(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS) == null ?
          0 : (Long)(session.getAttribute(AccessRequestStatusItem.STATE_COUNT_MY_REQUESTS));
  long totalNotifications = session.getAttribute(AccessRequestStatusItem.STATE_COUNT_TOTAL) == null ?
          0 : (Long)(session.getAttribute(AccessRequestStatusItem.STATE_COUNT_TOTAL));
  String notReviewed_highlight = "", recentActivity_highlight = "", accessReport_highlight = "";

  // Get the context, current tab, older user requests, and rowsPerPage session values
  AccessRequestContext arContext = (AccessRequestContext)session.getAttribute(AccessRequestServlet.ACCESS_REQUEST_CONTEXT_ATTRIB);
  String currentTab = (String) arContext.getCurrentTab();
  Boolean showOlderUserRequests = (Boolean) arContext.getShowOlderUserRequests();
  Boolean showOlderProjectRequests = (Boolean) arContext.getShowOlderProjectRequests();
  Integer rowsPerPage = (Integer) arContext.getRowsPerPage();
  Integer currentPage = (Integer) arContext.getCurrentPage();

  String activityNotificationString = "";
  if (totalNotifications > 0) {
      activityNotificationString = "<img src=\"images/flag.png\" title=\"There is recent access-request activity for you to review.\" />";
     }

  // Show/Hide project requests for Recent Activity / Not Reviewed
  Boolean showOlderProjectLink = false;
  // Show/Hide terms columns for access report
  Boolean isTermsColumnGroupVisible = false;

  // Get the sort, filter, and search parameters
  String sortBy = arContext.getSortBy();
  String filterBy = (String) arContext.getFilterBy();
  String searchString = (String) arContext.getSearchString();
  Boolean wasEverOwner = (Boolean) arContext.getWasEverOwner();

  // Setup local variables
  int projectRequestColspan = 0;
  int projectTrunc = 100;

  // Determine which tab we are on and change column span class accordingly
  int userRequestColspan = 7;
  if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
    recentActivity_highlight = "checked=\"checked\"";
    projectRequestColspan = 9;
  } else if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
    accessReport_highlight = "checked=\"checked\"";
    projectRequestColspan = 8;
  } else {
    currentTab = AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED;
    notReviewed_highlight = "checked=\"checked\"";
    projectRequestColspan = 9;
  }
%>
<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="access-requests">

    <h1>Access Requests</h1>
    <div id="user-requests">

    <%
    // List of user requests
    List<UserRequestDTO> userRequests = (List<UserRequestDTO>)
         session.getAttribute(AccessRequestServlet.USER_REQUEST_DTO_ATTRIB);
    // List of shown user and shown project requests
    List<UserRequestDTO> shownUserRequests = new ArrayList();
    List<ProjectRequestDTO> shownProjectRequests = new ArrayList();

    // Check to see if any of the projects have a DP
    Boolean displayDp = false;
    Boolean showOlderUserLink = false;
    // Only add recent (last 90 days) user requests
    for (Iterator iter = userRequests.iterator(); iter.hasNext();) {

        UserRequestDTO dto = (UserRequestDTO) iter.next();
        // Show only last 90 days unless otherwise specified by the user
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -AccessRequestContext.DEFAULT_OLDER_THAN);
        Date olderThanDate = (Date) cal.getTime();

        if (dto.getLastRequest() == null
                || dto.getLastRequest().after(olderThanDate) || showOlderUserRequests) {
            shownUserRequests.add(dto);
        }
        // Do we show the "Show Older User Requests" link
        if (dto.getLastRequest() == null
                || !dto.getLastRequest().after(olderThanDate)) {
            showOlderUserLink = true;
        }
        // Do we show column header as PI/DP
        if (!dto.getDpName().equals("") && !dto.getPiName().equals(dto.getDpName())) {
            displayDp = true;
        }
    }
    %>
    <h2>My Requests for Access<span id="MyRequestsCount"><%
    if (myRequestsCount > 0) {
        %> (<%=myRequestsCount %>)<%
    }
    %></span></h2>

    <%
    // if there are any requests to display, then display them in a table
    if (userRequests != null && userRequests.size() > 0) {
                %>
                <form id="user_requests_form">
                  <table id="user-requests-table">
                  <tr>
                        <th class="arFirstColumn"></th>
                        <th>Project</th>
                        <%
                        if (displayDp == true) { %>
                          <th>PI / Data Provider</th>
                        <% } else { %>
                          <th>PI</th>
                        <% } %>
                        <th>Level</th>
                        <th>Status</th>
                        <th>Last Request</th>
                        <th></th>
                  </tr>
                <%
                // Show My Requests for Access (user request list)
                int rowCount = 0;

                for (Iterator<UserRequestDTO> iter = shownUserRequests.iterator(); iter.hasNext();) {
                      String evenOrOdd = rowCount % 2 == 0 ? "even" : "";
                      UserRequestDTO dto = (UserRequestDTO) iter.next();
                      String rowId = "requestId_" + dto.getProjectId();

                      %>
                      <tr class="<%=evenOrOdd %>" id="<%=rowId %>" name="<%=rowId %>">
                       <td class="arFirstColumn">
                       <%
                       if (dto.isRecent()) {
                           %><img src="images/sphere.png" id="<%=rowId %>" class="notify-img" title="Recently changed" /><%
                       }
                       %>
                       </td>
                       <td><a href="Project?id=<%=dto.getProjectId()%>" title="<%=dto.getProjectName()
                        %>"><%=dto.getProjectNameTrunc(projectTrunc)%></a></td>
                       <td>
                         <%
                         String dpTag = "";
                         if (!dto.getPiName().equals("")) {
                         %>
                         <%=dto.getPiName() %>
                         <%
                         } else {
                             dpTag = "(DP)";
                         }
                         if (displayDp && !dto.getPiName().equals("") && !dto.getDpName().equals("")) {
                                 %>, <%=dto.getDpName()%> <%=dpTag %><%
                         } else if (displayDp && !dto.getDpName().equals("")) {
                             %><%=dto.getDpName()%> <%=dpTag %><%
                         }
                       %></td>
                       <td><div id="<%=rowId %>" name="levelDiv"><%=AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false) %>
                       </div></td>

                       <td><div id="<%=rowId %>" name="statusDiv"><%=dto.getStatus() %>
                       </div></td>
                       <td><div id="<%=rowId %>" name="lastRequestDiv">
                       <%=AccessRequestServlet.quickDateFormat(dto.getLastRequest()) %>
                       </div></td>
                       <%
                       String requestButtonClass = "";
                       if (dto.isButtonEnabled()) {
                         requestButtonClass = "request_link";
                       } else {
                         requestButtonClass = "dead_link ui-state-disabled";
                       }

                       if (!dto.getStatus().equalsIgnoreCase(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED)) {
                       %>
                       <td><div id="<%=rowId %>" name="requestButtonDiv" class="accessRequestButton">
                         <p><a href="#" id="<%=rowId %>" class="<%=requestButtonClass
                         %> ui-state-default ui-corner-all" name="<%=rowId %>" >
                         <span class="ui-icon ui-icon-newwin"></span><%=dto.getButtonTitle() %></a></p>
                       </div></td>
                       </tr>
                       <%
                       } else {
                           %>
                           <td><div id="<%=rowId %>" name="requestButtonDiv" class="accessRequestButton">
                             <p><span class="ui-icon ui-icon-check"></span></p>
                           </div></td>
                       </tr>
                           <%
                       }
                      rowCount++;
                }

                // Show "Hide older user requests" link
                if (showOlderUserRequests && showOlderUserLink) {
                        %>
                        <tr><td colspan="<%=userRequestColspan %>"><div id="showHideUserRequestsDiv">
                            <span class="hideOlderUserRequests">
                            Hide requests older than <%=AccessRequestContext.DEFAULT_OLDER_THAN %> days</span>
                            </div><td></tr>
                        <%
                // Show "Show older user requests" link
                } else if (!showOlderUserRequests && showOlderUserLink) {       // end if showOlderUserRequests
                %>
                    <tr><td colspan="<%=userRequestColspan %>"><div id="showHideUserRequestsDiv">
                        <span class="showOlderUserRequests">
                        Show requests older than <%=AccessRequestContext.DEFAULT_OLDER_THAN %> days</span>
                    </div><td></tr>
                <%
                }
        %>
        </table>  <!-- End #user-requests table -->
        </form>
        <%
    } else {  // no user requests for the logged in user have been found
    %>
      <div id="no-user-requests">
        You have not yet requested access to any private projects. You can do so from the
        <a href="index.jsp?datasets=other">private datasets</a> page.
      </div> <!-- End #no-user-requests div -->
    <%
    }
    %>
    </div> <!-- End #user-requests div -->

    <h2>Requests for Access to My Projects</h2>
    <%
    String notReviewedLabel = "Not Reviewed";
    if (adminUserFlag) {
        notReviewedLabel = "Pending";
    }
    %>

    <!-- Sub-tab links (Not Reviewed/Pending, Recent Activity, Access Report) -->
        <input type="hidden" id="ar-current-tab" value="<%=currentTab %>" />
        <div id="radioLinks" style="font-size: .9em;">
            <input type="radio" id="Not Reviewed" name="radioLink" <%=notReviewed_highlight %> /><label id="notreviewed_label" for="Not Reviewed"><%=notReviewedLabel %><%
            if (notReviewedCount > 0) {
            out.print(" (" + notReviewedCount + ") ");
        }
            %></label>
            <input type="radio" id="Recent Activity" name="radioLink" <%=recentActivity_highlight %> /><label id="recent_label" for="Recent Activity">Recent Activity<%
            if (recentActivityCount > 0) {
             out.print(" (" + recentActivityCount + ") ");
          }
            %></label>
            <input type="radio" id="Access Report" name="radioLink" <%=accessReport_highlight %> /><label id="report_label" for="Access Report">Access Report</label>
        </div>

    <%
        // Get the list of project requests for this user (pi, dp, or admin)
        List<ProjectRequestDTO> projectRequests = (List<ProjectRequestDTO>)
             session.getAttribute(AccessRequestServlet.PROJECT_REQUEST_DTO_ATTRIB);

        int arNumPages = 0;
        int arNumRecords = 0;

        if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
            AccessReportDto accessReportDto = (AccessReportDto)session.getAttribute(AccessRequestServlet.ACCESS_REPORT_DTO_ATTRIB);
            projectRequests = accessReportDto.getAccessReport();
            arNumPages = accessReportDto.getNumPages();
            arNumRecords = accessReportDto.getNumRecords();
            isTermsColumnGroupVisible = accessReportDto.getHasTermsOfUse();
        }

    int rowCount = 0;

    // Create a table for the controls (search, rows per page, new access row, export, etc)
    String tableClass = "";
    if (adminUserFlag && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
        tableClass = "adminTable";
    }
    %>
    <table id="project-request-controls" class="<%=tableClass%>">
          <tr>
            <td class="search-controls">
            <%
            // Search by user or project

              if (searchString.trim().equals("")) {
              %>
                <input type="text" id="<%=AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING %>"
                    name="<%=AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING %>"
                    size="30" title="Search by project or user" /> <img id="ar_search_button"
                    name="ar_search_button" src="images/magnifier.png" />
              <%
              } else {
              %>
                <input type="text" id="<%=AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING %>"
                    name="<%=AccessRequestContext.ACCESS_REQUEST_SEARCH_STRING %>"
                    size="30" value="<%=searchString %>" /> <img id="ar_search_button"
                    name="ar_search_button" src="images/magnifier.png" />
              <%
              }


            %>
            </td>
        <%
        // Rows per page control for Access Report
        if (projectRequests != null && !projectRequests.isEmpty() && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
            %>
            <td class="rows-per-page">
              Rows per page: <select id="<%=AccessRequestContext.ACCESS_REQUEST_ROWS_PER_PAGE %>">
              <%
              int rowCounts[] = { 10, 20, 30, 50, 75, 100 };
              for (int rowsPerPageCount : rowCounts) {
                  String selected = "";
                  if (rowsPerPage == rowsPerPageCount) {
                      selected = "selected";
                  }
                  %>
                      <option value="<%=rowsPerPageCount %>" <%=selected %>><%=rowsPerPageCount %></option>
                  <%
              }
              %>
              </select>
            </td>
            <%
        }
        // Determine start and end indexes for displaying the rows,
        // determine which options are set for display, and format the request list
        if (projectRequests != null && !projectRequests.isEmpty()) {
            // Make sure request list is sorted according to user-defined preference
            // Not necessary for 'Access Report' as db has already sorted results.
            if (!currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
                Comparator<ProjectRequestDTO> comparator = ProjectRequestDTO.getComparator(
                        AccessRequestServlet.selectSortParameters(arContext));
                Comparator<ProjectRequestDTO> nullComparator = new NullComparator(comparator, false);
                Collections.sort(projectRequests, nullComparator);
            }

            // Make a simple page bar so the user can iterator through (int)rowsPerPage rows at a time
            if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
              %>
              <td class="nav-buttons"><table id="pageCountTable"><tr>
              <%
              // Display Prev page link
              if (currentPage > 1 && arNumPages > 1) {
                  %>
                  <td><a href="AccessRequests?ar_current_page=1"><img src="images/grid/backwards_grey.gif" /> First</a></td><td><b>|</b></td>
                  <td><a href="AccessRequests?ar_current_page=<%=(currentPage-1) %>"><img src="images/grid/back_grey.gif" /> Back</a></td><td><b>|</b></td>
                  <%
              } else {
                  %>
                  <td class="disabled"><img src="images/grid/backwards_light_grey.gif" /> First</td><td><b>|</b></td>
                  <td class="disabled"><img src="images/grid/back_light_grey.gif" /> Back</td><td><b>|</b></td>
                  <%
              }
              // Display Next next link
              if (currentPage < arNumPages && arNumPages > 1) {
                  %>
                  <td><a href="AccessRequests?ar_current_page=<%=(currentPage+1) %>">Next <img src="images/grid/next_grey.gif" /></a></td><td><b>|</b></td>
                  <td><a href="AccessRequests?ar_current_page=<%=arNumPages %>">Last <img src="images/grid/forward_grey.gif" /></a></td>
                  <%
              } else {
                  %>
                  <td class="disabled">Next <img src="images/grid/next_light_grey.gif" /></td><td><b>|</b></td>
                  <td class="disabled">Last <img src="images/grid/forward_light_grey.gif" />
                  <%
              }
            }

            if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
                // Determine at which index to start displaying records
                int firstIndex = (currentPage - 1) * rowsPerPage;
                int lastIndex = arNumRecords;
                if (arNumRecords > firstIndex + rowsPerPage) { lastIndex = firstIndex + rowsPerPage; }
            %>
             </tr>
             </table>  <!-- End #pageCountTable table -->
             </td>

            <td class="row-range"><%=firstIndex + 1 %>-<%=lastIndex %> of <%=arNumRecords %></td>
            <%
            }

            // Partition off older and recent project requests for Not reviewed and Recent sub-tabs
            Integer rowsAdded = 0;
            // Add DTO only for currently viewed page number
            Iterator<ProjectRequestDTO> pageIter = projectRequests.iterator();
            for (; pageIter.hasNext()
                    && (rowsAdded < rowsPerPage || !currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)); ) {
                ProjectRequestDTO dto = (ProjectRequestDTO) pageIter.next();

                // Get the date object for '90 days ago'
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -AccessRequestContext.DEFAULT_OLDER_THAN);
                Date olderThanDate = (Date) cal.getTime();

                // If the last activity date is less than 90 days ago, then display the request
                if (dto.getLastActivityDate() != null
                  && (showOlderProjectRequests || dto.getLastActivityDate().after(olderThanDate) )) {
                        shownProjectRequests.add(dto);
                        rowsAdded++;
                 // Display the request if the Show Older Requests is selected or if we're on the report
                } else if ((dto.getLastActivityDate() == null && showOlderProjectRequests)
                        || currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
                        shownProjectRequests.add(dto);
                        rowsAdded++;
                }
                if (!currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
                  if (dto.getLastActivityDate() == null || !dto.getLastActivityDate().after(olderThanDate)) {
                    showOlderProjectLink = true;
                  }
                }
            }

            if (isTermsColumnGroupVisible && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
              projectRequestColspan += 3;
            }
        }

        // Administrator control: New Access Row Button
        if (adminUserFlag) {
           %>
           <td><div id="newAccessRowDiv" name="newAccessRowDiv">
             <p><a href="#" id="newAccessRow" class="newaccess_link ui-state-default ui-corner-all" name="newAccessRow" >
             <span class="ui-icon ui-icon-newwin"></span>New access row</a></p>
           </div></td>
           <%
        }

        %>
        </tr>
        <tr>
        <%

        // Administrator's control: filter-by control for Pending/Not Reviewed
        if (((projectRequests != null && !projectRequests.isEmpty()) || !filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY))
                && adminUserFlag && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)) {
            %>

            <td class="filter-by">
              Filter by: <select id="<%=AccessRequestContext.ACCESS_REQUEST_FILTER_BY %>">
              <%
              String filters[] = { AccessRequestContext.DEFAULT_FILTER_BY,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED };
              for (String filter : filters) {
                  String selected = "";
                  if (filterBy.equals(filter)) {
                      selected = "selected";
                  }
                  %>
                      <option value="<%=filter %>" <%=selected %>><%=
                         AccessRequestStatusItem.arStringEquivalent(filter, true) %></option>
                  <%
              }
              %>
              </select>
            </td></tr>
            <%
        // Administrator's control: filter-by control for Recent Activity
        } else if (((projectRequests != null && !projectRequests.isEmpty()) || !filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY))
                && adminUserFlag && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
            %>
            <td class="filter-by">
              Filter by: <select id="<%=AccessRequestContext.ACCESS_REQUEST_FILTER_BY %>">
              <%
              String filters[] = { AccessRequestContext.DEFAULT_FILTER_BY,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_NOT_REVIEWED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_APPROVED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_APPROVED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_APPROVED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED,
                      AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED };
              for (String filter : filters) {
                  String selected = "";
                  if (filterBy.equals(filter)) {
                      selected = "selected";
                  }
                  if (filter.equals(AccessRequestContext.DEFAULT_FILTER_BY)) {
                  %>
                      <option value="<%=filter %>" <%=selected %>>All Recent</option>
                  <%
                  } else {
                  %>
                      <option value="<%=filter %>" <%=selected %>><%=
                        AccessRequestStatusItem.arStringEquivalent(filter, true) %></option>
                  <%
                  }
              }
              %>
              </select>
            </td></tr>
            <%
        } else if ((projectRequests != null && !projectRequests.isEmpty()) 
                   && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {

            String showAdminsChecked = arContext.getShowAdmins() ? "checked" : "";

            if (adminUserFlag) { 
            %>
            <td class="show-admins">
              <input type="checkbox" name="<%=AccessRequestContext.ACCESS_REQUEST_SHOW_ADMINS %>"
                id="<%=AccessRequestContext.ACCESS_REQUEST_SHOW_ADMINS %>" <%=showAdminsChecked %>/><label
                for="<%=AccessRequestContext.ACCESS_REQUEST_SHOW_ADMINS %>">Show Admins</label>
            </td>
            </tr>
            <% } %>

            <!-- Export-related -->
            <td class="export-button">
            <div id="exportDiv" name="exportDiv">

                 <p><a href="#" id="accessRequestExportReport"
                       class="export_link ui-state-default ui-corner-all"
                       name="accessRequestExportReport" >
                       <span class="ui-icon ui-icon-newwin"></span>Export Access Report</a>
                 </p>
            </div>
            </td>
            <td class="show-admins">
              <div id="exportCpDiv">
                   <input type="checkbox" name="export_cp_only" id="export_cp_only" />
                   <label for="export_cp_only">Export Current Permissions Only</label>
              </div>
            </td></tr>

        <% } %>
      </table>  <!-- End #project-request-controls table -->

    <div id="project-requests"> <!-- Begin #project-requests div -->
    <form id="project_requests_form">
    <table id="project-requests-table">
    <%
    if (projectRequests != null && !projectRequests.isEmpty()) {
      if (!shownProjectRequests.isEmpty()) {
      %>

      <tr>
        <th class="arFirstColumn"></th>
        <th class="arSecondColumn"></th>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_PROJECT %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_PROJECT %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_PROJECT,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_PROJECT) )
        %>" /></th>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_USER %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_USER %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_USER,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_USER) )
        %>" /></th>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_INSTITUTION %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_INSTITUTION %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_INSTITUTION,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_INSTITUTION) )
        %>" /></th>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_LEVEL %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_LEVEL %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_LEVEL,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_LEVEL) )
        %>" /></th>

        <%
        if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {
        %>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_FIRSTACCESS %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_FIRSTACCESS %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_FIRSTACCESS,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_FIRSTACCESS) )
        %>" /></th>
        <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_LASTACCESS %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_LASTACCESS %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_LASTACCESS,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_LASTACCESS) )
        %>" /></th>
          <%
          if (isTermsColumnGroupVisible) {
          %>
          <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_TERMS %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_TERMS %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_TERMS,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_TERMS) )
        %>" /></th>
          <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_TERMSVERSION %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_TERMSVERSION %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_TERMSVERSION,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_TERMSVERSION) )
        %>" /></th>
          <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_TERMSDATE %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_TERMSDATE %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_TERMSDATE,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_TERMSDATE) )
        %>" /></th>

          <%
          }
        } else {
        %>
          <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_STATUS %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_STATUS %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_STATUS,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_STATUS) )
        %>" /></th>
          <th><a href="#" id="<%=ProjectRequestDTO.COLUMN_LASTACTIVITYDATE %>" class="sortByColumn"><%=ProjectRequestDTO.COLUMN_LASTACTIVITYDATE %></a>
        <img src="<%=
            ProjectRequestDTO.showSortOrder(sortBy,
                ProjectRequestDTO.COLUMN_LASTACTIVITYDATE,
                arContext.getSortOrder(ProjectRequestDTO.COLUMN_LASTACTIVITYDATE) )
        %>" /></th>

          <th></th>
        <%
        }
        %>
      </tr>

      <%
      }

        for (ProjectRequestDTO dto : shownProjectRequests) {    // begin shownProjectRequests
            // Even rows are gray
            String evenOrOdd = rowCount % 2 == 0 ? "even" : "";

            String responseButtonId = "responseId_" + dto.getProjectId();
            String rowId = responseButtonId + "_" + rowCount;
            List<ProjectRequestHistoryDTO> historyList = (List<ProjectRequestHistoryDTO>) dto.getProjectRequestHistory();
            %>
            <tr class="<%=evenOrOdd %>" id="<%=rowId %>" name="<%=rowId %>">
            <td class="arFirstColumn">
            <%
            if (dto.isRecent()
                    && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
                %><img src="images/sphere.png" id="<%=rowId %>" class="notify-img"><%
            }
            %>
            </td>
            <td class="arSecondColumn">
              <% if (historyList != null && !historyList.isEmpty()) { %>
                <p><img id="<%=rowId %>" class="collapsed" name="<%=rowId %>" src="images/expand.png"></p>
              <% } else { %>
                  <p></p>
              <%
                 }
              %>
            </td>
            <td><%
            if (!dto.getPiName().equals("")) {
                if (!dto.getDpName().equals("")) {
                    %><a href="Project?id=<%=dto.getProjectId()%>" title="PI: <%=dto.getPiName() %>, DP: <%=dto.getDpName() %>"><%=
                        dto.getProjectNameTrunc(projectTrunc)%></a><%
                }
                else if (dto.getDpName().equals("")) {
                    %><a href="Project?id=<%=dto.getProjectId()%>" title="PI: <%=dto.getPiName() %>"><%=
                        dto.getProjectNameTrunc(projectTrunc)%></a><%
                }
            } else if (!dto.getDpName().equals("")) {
                %><a href="Project?id=<%=dto.getProjectId()%>" title="DP: <%=dto.getDpName() %>"><%=
                    dto.getProjectNameTrunc(projectTrunc)%></a><%
            }
            else if (!dto.getProjectName().equals("-")) {
                %><a href="Project?id=<%=dto.getProjectId()%>" title="<%=dto.getProjectName() %>"><%=
                        dto.getProjectNameTrunc(projectTrunc)%></a><%
            } else {
                %>-<%
            }

            %></td>
            <%
                String userField = "";

                    if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                      userField = "Public";
                    } else if (!dto.getEmail().equals("")) {
                      userField = "<a href=\"mailto:" + dto.getEmail() + "\">"
                        + dto.getUserFullName() + "</a>";
                    } else {
                      userField = dto.getUserFullName();
                    }
            %>

            <td class="hidden_td"><input type="hidden" id="<%=rowId %>" class="<%=currentTab %>"
                name="userId" value="<%=dto.getUserId() %>" /></td>
            <td class="hidden_td"><input type="hidden" id="<%=rowId %>" class="<%=currentTab %>"
                name="projectId" value="<%=dto.getProjectId() %>" /></td>
            <td><%=userField %></td>
            <td><%=dto.getInstitution()%></td>
            <td><div id="<%=rowId %>" name="modifyAccessDiv" class="modifyAccessDiv">
            <%
            if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                if (dto.getLevel().equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)) {
                %>
                  <p class="dead_link"><%=ProjectRequestDTO.PRIVATE_LEVEL %></p>
                <%
                } else {
                    String level = dto.getLevel();
                    String capitalized = level;
                    if (level != null && level.length() > 0) {
                        capitalized = level.substring(0, 1).toUpperCase()
                                + level.substring(1, level.length()).toLowerCase();
                    }
                %>
                  <p class="dead_link"><%=capitalized%></p>
                <%
                }

            } else if (dto.isButtonVisible() && !adminUserFlag) {
                  %>
                  <p class="dead_link"><%=AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false)%></p>
                <%
            } else {
                if (!dto.getLevel().equals("-")) {
                    %><a href="#" id="<%=rowId %>" class="response_link" name="<%=rowId %>" ><%=
                AccessRequestStatusItem.arStringEquivalent(dto.getLevel(), false) %>
                <img src="images/edit.gif"></a><%
                } else {
                    %>-<%
                }
              %>


              <%
            }
            %>
            </div></td>

            <%
            if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) {

              if (dto.getFirstAccess() != null) {
              %>
                <td><%=AccessRequestServlet.quickDateFormat(dto.getFirstAccess()) %></td>
              <%
              } else if (dto.getUserId().equals(UserItem.DEFAULT_USER)) { %>
                <td></td>
              <%
              } else { %>
                <td>-</td>
              <%
              }
              if (dto.getLastAccess() != null) {
              %>
                <td><%=AccessRequestServlet.quickDateFormat(dto.getLastAccess()) %></td>
              <%
              } else if (dto.getUserId().equals(UserItem.DEFAULT_USER)) { %>
                <td></td>
              <%
              } else { %>
                <td>-</td>
              <%
              }

              // Only show terms columns if one of the project requests has associated terms
               if (isTermsColumnGroupVisible) {
               %>
                 <td><%=dto.getTouName()%></td>
                 <td><%=dto.getTouVersionAgreedString() %></td>
                 <td><%=dto.getTouDateAgreedString() %></td>
               <%
              }
            } // end if(AccessReport)
            else if (!currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)) { // begin if(Not Reviewed)
            %>
              <td><div id="<%=rowId %>" name="statusDiv">
                <%=AccessRequestStatusItem.arStringEquivalent(dto.getStatus(), false) %>
                </div></td>
              <td><div id="<%=rowId %>" name="lastActivityDiv">
                <%=AccessRequestServlet.quickDateFormat(dto.getLastActivityDate()) %>
                </div></td>
              <td>
              <%
              if (dto.isButtonVisible() && !adminUserFlag) {
                  if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                      %>
                      <div id="<%=rowId %>" name="responseButtonDiv"  class="responseButton">
                        <p><a href="#" id="<%=rowId %>" class="response_link ui-state-default-blue ui-corner-all-blue" name="<%=rowId %>" >
                        <span class="ui-icon ui-icon-blue ui-icon-newwin"></span>Vote</a></p>
                      </div>
                      <%
                  } else {
                      %>
                      <div id="<%=rowId %>" name="responseButtonDiv" class="responseButton">
                        <p><a href="#" id="<%=rowId %>" class="response_link ui-state-default-blue ui-corner-all-blue" name="<%=rowId %>" >
                        <span class="ui-icon ui-icon-blue ui-icon-newwin"></span>Respond</a></p>
                      </div>
                      <%
                  }
              }
              %>
              </td>
              <%
            }
            %>

            </tr>
            <tr class="history_row <%=evenOrOdd %>"><td colspan="<%=projectRequestColspan %>">
              <table id="<%=rowId %>" name="project-request-history" class="project-request-history">

              <tr id="<%=rowId %>" name="<%=rowId %>" class="historyList">
              <td class="arFirstColumn"></td>
              <td class="arSecondColumn"></td>
              <td><b>History</b></td>
              </tr>
              <tr id="<%=rowId %>" name="historyPlaceHolder"></tr>

            <%
            if (historyList != null && !historyList.isEmpty()) {
              int historyRowCount = 0;
              for (ProjectRequestHistoryDTO historyDto : historyList) {
                 //String evenOrOddHistory = historyRowCount % 2 == 0 ? "evenHistory" : "";
                 %>

                 <tr id="<%=rowId %>" name="<%=rowId %>" class="historyList">
                 <td class="arFirstColumn"></td>
                 <td class="arSecondColumn"></td>

                 <td><%=AccessRequestServlet.quickDateFormat(historyDto.getDate())%></td>
                 <%
                 if (!historyDto.getEmail().equals("")){
                 %>
                 <td><a href="mailto:<%=historyDto.getEmail()%>">
                     <%=historyDto.getUserFullName()%></a> (<%=
                        AccessRequestStatusItem.arStringEquivalent(historyDto.getRole(), false) %>)</td>
                 <%
                     } else {
                 %>
                     <td><%=historyDto.getUserFullName()%> (<%=
                        AccessRequestStatusItem.arStringEquivalent(historyDto.getRole(), false) %>)</td>
                 <%
                 }
                 %>
                 <td><%=AccessRequestStatusItem.arStringEquivalent(historyDto.getAction(), true)%></td>
                 <%
                 if (historyDto.getAction().equals(AccessRequestHistoryItem.ACCESS_REQUEST_ACTION_DENY)
                     && !dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                   %>
                   <td></td>
                   <%
                 } else if (dto.getUserId().equals(UserItem.DEFAULT_USER)) {
                     %>
                     <td>Public Access</td>
                     <%
                 } else {
                   %>
                   <td><%=AccessRequestStatusItem.arStringEquivalent(historyDto.getLevel(), true)%></td>
                   <%
                 }
                 %>

                 <td class="reason"><%=historyDto.getReason()
                    .replaceAll("&", "&amp;")
                        .replaceAll(">", "&gt;")
                        .replaceAll("<", "&lt;")%></td>
                 <td></td>
                 <td></td>
                 <td></td>
                 <td></td>
                 </tr>

              <%
                historyRowCount++;
              } // end for loop for all project history

            }  // end if historyList != null
            %>
            </table></td></tr>
            <%
            rowCount++;
        } // end shownProjectRequests loop

        if (shownProjectRequests.isEmpty() && showOlderProjectLink) {
            %>
            <tr><td colspan="<%=projectRequestColspan %>"><div id="no-project-requests" class="infoMessage">
                    There has been no access request activity for your projects within the last 90 days.
                </div><td></tr>
            <%
        }
        if (showOlderProjectRequests && showOlderProjectLink) {
                %>
                <tr><td colspan="<%=projectRequestColspan %>"><div id="showHideProjectRequestsDiv">
                            <span class="hideOlderProjectRequests">
                            Hide requests older than <%=AccessRequestContext.DEFAULT_OLDER_THAN %> days</span>
                        </div><td></tr>
                <%
        } else if (!showOlderProjectRequests && showOlderProjectLink) {
                %>
                <tr><td colspan="<%=projectRequestColspan %>"><div id="showHideProjectRequestsDiv">
                        <span class="showOlderProjectRequests">
                        Show requests older than <%=AccessRequestContext.DEFAULT_OLDER_THAN %> days</span>
                    </div><td></tr>
                <%
        }

        %>
        </table>
        </form>
        </div>
        <%
    } else if (currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_REPORT)
            && wasEverOwner) {
        String displayStr = "There is no history of anyone having access to your project(s).";
        if (!searchString.trim().equals("")) {
             displayStr = "No access requests match your search criteria.";
        }
    %>
      <div id="no-project-requests" class="infoMessage">
        <%=displayStr %>
      </div>
    <%
    } else if (adminUserFlag && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_NOT_REVIEWED)) {
        String displayStr = "There are no pending requests";
        if (!filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY)) {
            displayStr = "No pending requests match that filter criteria.";
        }
        if (!searchString.trim().equals("")) {
            displayStr = "No pending requests match your search criteria.";
        }
        %>
        <div id="no-project-requests" class="infoMessage">
        <%=displayStr %>
        </div>
      <%
    } else if (adminUserFlag && currentTab.equals(AccessRequestServlet.ACCESS_REQUEST_SUBTAB_RECENT)) {
        String displayStr = "There are no recent requests";
        if (!filterBy.equals(AccessRequestContext.DEFAULT_FILTER_BY)) {
            displayStr = "No recent requests match that filter criteria.";
        }
        if (!searchString.trim().equals("")) {
            displayStr = "No recent requests match your search criteria.";
        }
        %>
        <div id="no-project-requests" class="infoMessage">
        <%=displayStr %>
        </div>
      <%
    } else if (wasEverOwner) {
       String displayStr = "There are no access requests for you to review.";
       if (!searchString.trim().equals("")) {
            displayStr = "No access requests match your search criteria.";
        }
    %>
      <div id="no-project-requests" class="infoMessage">
        <%=displayStr %>
      </div> <!-- End #no-project-requests div -->
    <%
    } else {
        %>
      <div id="no-project-requests" class="infoMessage">
      You are not the PI or Data Provider for any projects.
      </div> <!-- End #no-project-requests div -->
        <%
    }
    %>


    </div> <!-- End #project-requests div -->
    </div> <!-- End #access-requests div -->
    </div> <!-- End #contents div -->
    </div> <!--  End #main div -->

    </td>
</tr></table>

<!-- Unpopulated div's needed for modal windows using jQuery -->
<div id="requestDialog" class="requestDialog"> </div>
<div id="responseDialog" class="responseDialog"> </div>
<div id="newAccessRowDialog" class="newAccessRowDialog"> </div>
<div id="voteProjectPublicDialog" class="voteProjectPublicDialog"> </div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/access-requests.jspf" %>
</div>

</body>
</html>
