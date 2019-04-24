<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 12709 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2015-10-20 11:53:44 -0400 (Tue, 20 Oct 2015) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.CustomFieldItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.customfield.CustomFieldDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper"%>

<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%
    DatasetContext httpSessionInfo = (DatasetContext)session.getAttribute("datasetContext_"
        + request.getParameter("datasetId"));

    Boolean editFlag = httpSessionInfo.getEditFlag() || httpSessionInfo.isDataShopAdmin();

    DatasetItem datasetItem = httpSessionInfo.getDataset();
    ProjectItem projectItem = datasetItem.getProject();
    Integer projectId = null;
    if (projectItem != null) {
        projectId = (Integer)projectItem.getId();
    }

    // Check for presence of data...
    long numberOfTransactions = httpSessionInfo.getNumTransactions();
    boolean isRemote = HelperFactory.DEFAULT.getNavigationHelper().isDatasetRemote(datasetItem);

    UserItem user = (UserItem)session.getAttribute("cmu.edu.pslc.datashop.item.UserItem");
    String remoteUser = (user != null) ? (String)user.getId() : null;

    Boolean adminUserFlag = false;
    if (user != null) { adminUserFlag = user.getAdminFlag(); }

    Boolean hasProjectAdmin = false;
    Boolean hasProjectEdit = false;
    String authorizationLevel = null;
    if (remoteUser != null) {
        ProjectPageHelper projPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();
        authorizationLevel = projPageHelper.getAuthLevel(remoteUser, projectId);
        if (authorizationLevel != null) {
            if (authorizationLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                hasProjectAdmin = true;
                hasProjectEdit = true;
            } else if (authorizationLevel.equals(AuthorizationItem.LEVEL_EDIT)) {
                hasProjectEdit = true;
            }
        }
    }

    CustomFieldContext cfContext = CustomFieldContext.getContext(request);
    String sortByColumn = cfContext.getSortByColumn();
    Boolean isAscending = cfContext.isAscending(sortByColumn);

    Integer datasetId = (Integer)datasetItem.getId();
    List<CustomFieldDto> cfList =
        (List<CustomFieldDto>)request.getAttribute(CustomFieldServlet.CF_LIST_ATTR + datasetId);

    Boolean displayEdit =
        (Boolean)request.getAttribute(CustomFieldServlet.CF_DISPLAY_EDIT_ATTR + datasetId);

    // On redirect, request is empty. Check session.
    if (cfList == null) {
        cfList = (List<CustomFieldDto>)request.getSession().
                getAttribute(CustomFieldServlet.CF_LIST_ATTR + datasetId);
    }

    if (displayEdit == null) {
        displayEdit = (Boolean)request.getSession().
            getAttribute(CustomFieldServlet.CF_DISPLAY_EDIT_ATTR + datasetId);
    }
    if (displayEdit == null) { displayEdit = true; }

    String tdClassOdd = "";
    String tdClassEven = "even";
    String tdClass = tdClassOdd;
%>
<div id="customFields">

    <h1>Custom Fields</h1>

    <%
    if (isRemote) {
        out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/alert_32.gif\" /></div>"
            + "<p>This dataset can be found on a remote DataShop instance.</p></div>");

    } else if (numberOfTransactions == 0) {
        out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/alert_32.gif\" /></div>"
            + "<p>There is no transaction data for this dataset.</p></div>");

    } else {
    %>

    <% if (adminUserFlag || hasProjectEdit || hasProjectAdmin) { %>
        <input type="button" id="add-custom-field-button" class="ui-state-default ui-corner-all"
               value="Add Custom Field"
               onclick="javascript:openAddCustomFieldDialog(<%=datasetId %>)">
    <% } %>

    <table id="customFieldsTable" class="dataset-box">
    <colgroup>
        <col style="width:30%"/>
        <col style="width:15%"/>
        <col style="width:10%"/>
        <col style="width:15%"/>
        <col style="width:10%"/>
        <col style="width:10%"/>
        <% if (displayEdit) { %>
        <col style="width:10%"/>
        <% } %>
    </colgroup>
    <tr>
        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_NAME %>')">
               <%=CustomFieldDto.COLUMN_NAME %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_NAME,
                                                     sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_OWNER %>')">
               <%=CustomFieldDto.COLUMN_OWNER %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_OWNER,
                                                     sortByColumn, isAscending) %>" />
        </th>

        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_TYPE %>')">
               <%=CustomFieldDto.COLUMN_TYPE %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_TYPE,
                                                     sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_UPDATED_BY %>')">
               <%=CustomFieldDto.COLUMN_UPDATED_BY %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_UPDATED_BY,
                                                     sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_ROWS_WITH %>')">
               <%=CustomFieldDto.COLUMN_ROWS_WITH %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_ROWS_WITH,
                                                     sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortCustomFields('<%=CustomFieldDto.COLUMN_LEVEL %>')">
               <%=CustomFieldDto.COLUMN_LEVEL %></a>
            <img src="<%=CustomFieldDto.getSortImage(CustomFieldDto.COLUMN_LEVEL,
                                                     sortByColumn, isAscending) %>" />
        </th>
        <% if (displayEdit) { %>
        <th>
            <span></span>
        </th>
        <% } %>
    </tr>
    <%
        if (cfList != null && cfList.size() > 0) {
            int index = 1;
            for (Iterator iter = cfList.iterator(); iter.hasNext(); ) {
                 CustomFieldDto dto = (CustomFieldDto)iter.next();
                 String fullDescription =
                    (dto.getDescription() == null) ? "" : dto.getDescription();
                 String truncatedDescription =
                    (dto.getDescriptionTruncated() == null) ? "" : dto.getDescriptionTruncated();
                 String descriptionToDisplay =
                    (dto.getDescriptionToDisplay() == null) ? "" : dto.getDescriptionToDisplay();
                 Boolean isOwner = (user.equals(dto.getOwner()));
                 Boolean isUserCF = !(dto.getOwner().getName().equals(UserItem.SYSTEM_USER));
                 if (index % 2 == 0) {
                     tdClass = tdClassEven;
                 } else {
                     tdClass = tdClassOdd;
                 }
    %>
                 <tr>
                     <td class=<%=tdClass %>>
                         <div class="cfName cfWrappedText"><%=dto.getName() %></div>
                         <div id="cfDescription_<%=dto.getId()%>" class="cfWrappedText">
                         <span id="cfDescSpan_<%=dto.getId()%>"><%=descriptionToDisplay %></span>
                         </div>
                         <% if (dto.getDescriptionTruncatedFlag()) { %>
                            <div class="cfDescMoreDiv" id="cfDescMoreDiv_<%=dto.getId()%>">
                               <a href="javascript:showFullCFDescription(<%=dto.getId()%>)">more</a>
                            </div>
                            <div class="cfDescLessDiv" id="cfDescLessDiv_<%=dto.getId()%>">
                               <a href="javascript:hideFullCFDescription(<%=dto.getId()%>)">less</a>
                            </div>
                         <% } %>

                         <!-- HIDDEN FIELDS FOR DESCRIPTION -->
                         <input type="hidden" id="cfDescFull_<%=dto.getId()%>"
                                value="<%=fullDescription %>" />
                         <input type="hidden" id="cfDescTrunc_<%=dto.getId()%>"
                                value="<%=truncatedDescription %>" />
                         <input type="hidden" id="cfDescTruncFlag_<%=dto.getId()%>"
                                value=<%=dto.getDescriptionTruncatedFlag()%> />
                     </td>
                     <td class=<%=tdClass %>>
                         <%=dto.getOwnerString() %> <br/> <%=dto.getDateCreatedString() %></td>
                     <td class=<%=tdClass %>><%=dto.getTypeString() %></td>
                     <td class=<%=tdClass %>>
                         <%=dto.getUpdatedByString() %> <br/> <%=dto.getLastUpdatedString() %></td>
                     <td class=<%=tdClass %>><%=dto.getRowsWithValues() %>%</td>
                     <td class=<%=tdClass %>><%=dto.getLevel() %></td>
                     <% if (displayEdit) { %>
                     <td class=<%=tdClass %>>
                        <% if (adminUserFlag
                               || (isUserCF && hasProjectAdmin)
                               || (isOwner && hasProjectEdit)) { %>
                        <div id="cfEditDiv_<%=dto.getId()%>" class="editButtons">
                             <a href="javascript:openEditCustomFieldDialog(<%=dto.getId()%>)">
                             <img src="images/edit.gif" alt="Edit Custom Field"
                                  title="Edit Custom Field">
                             </a>
                             <div id="cfDeleteLinkDiv_<%=dto.getId()%>" class="cfDeleteLinkDiv" >
                                  <a href="javascript:showCFDeleteSureDiv(<%=dto.getId()%>)">
                                  <img src="images/delete.gif" alt="Delete Custom Field"
                                       title="Delete Custom Field">
                                  </a>
                             </div>
                             <div id="cfDeleteSureDiv_<%=dto.getId()%>" class="cfDeleteSureDiv" >
                                  delete this custom field?&nbsp
                                  <a id="cfDeleteNoLink_<%=dto.getId()%>"
                                  class="yes_no_links"
                                     href="javascript:showCFDeleteLinkDiv(<%=dto.getId() %>)">no</a>
                                  &nbsp/&nbsp
                                  <a id="cfDeleteYesLink_<%=dto.getId()%>"
                                  class="yes_no_links"
                                     href="javascript:deleteCustomField(<%=dto.getId() %>)">yes</a>
                             </div>

                        </div>
                        <% } %>
                     </td>
                     <% } %>
                 </tr>
    <%
            index++;
            }
        }
    %>


    </table>
</div>

<p class="cf-export-note">To export any of these custom fields, visit the corresponding <a id="cf-export-link" href="Export?datasetId=<%=datasetId%>">Export</a> page (e.g., By Transaction).</p>

<%
} // else
%>

<div style="clear:both;"></div>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="customFieldDialog" class="customFieldDialog"></div>



