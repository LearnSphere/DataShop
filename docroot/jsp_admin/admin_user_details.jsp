<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersDto"%>
<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Young Suk Ahn
// Version: $Revision: 9702 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-07-19 11:57:43 -0400 (Fri, 19 Jul 2013) $
// $KeyWordsOff: $
//
%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserRoleItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersServlet"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage User";
    showHelpButton = false;
    cssIncludes.add("Admin.css");
    cssIncludes.add("message.css");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    UserItem userItem = (UserItem) request.getAttribute(AccessContext.KEY_USER_ITEM);

    String errorMessage = (String) request.getAttribute("errorMessage");
    UserItem theUser = (UserItem) request.getAttribute("user");
    List<UserRoleItem> theUserRoles = (List<UserRoleItem>) request.getAttribute("userRoles");
    Map<String, String> roleMap = (Map<String, String>) request.getAttribute("roleMap");
    ManageUsersDto dto = (ManageUsersDto) request.getAttribute("manageUsersDto");

    Boolean isUserEnabled = (Boolean) request.getAttribute("isUserEnabled");

    String suspResumeAction = null;
    String suspResumeActionTitle = null;

    if (isUserEnabled != null) {
        suspResumeAction = isUserEnabled ? ManageUsersServlet.ACTION_SUSPEND
                : ManageUsersServlet.ACTION_RESTORE;
        suspResumeActionTitle = isUserEnabled ? "Suspend" : "Restore";
    }
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="user-management">
    <h1><a href="<%= ManageUsersServlet.SERVLET_NAME %>">Manage Users</a> &gt; <%= theUser.getName() %></h1>

    <% if (errorMessage != null) {%>
       <div class="error_message_div"> 
       <p class="error_style"><%=errorMessage%></p>
       </div>
    <% }%>

       <div id="operations" style="float:left">
           <table id="manage-menu" class="dataset-box">
           <tbody>
           <tr>
                <th>Account Settings</th>
           </tr>
           <tr>
               <td>
               <a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=edit&id=<%= URLEncoder.encode((String)theUser.getId(), "UTF-8") %>">Update Account Profile</a><br />
               <a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=roleedit&id=<%= URLEncoder.encode((String)theUser.getId(), "UTF-8") %>">Manage User Roles</a><br /></br>
               <div id="passwdReset"><a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=pwdreset&id=<%= URLEncoder.encode((String)theUser.getId(), "UTF-8") %>">Send password reset</a></div>
            <%if (isUserEnabled != null) { %>
               <div id="susResAccount"><a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=<%= suspResumeAction %>&id=<%= URLEncoder.encode((String)theUser.getId(), "UTF-8") %>" class="button-like <%= suspResumeAction %>"><%= suspResumeActionTitle %> account</a></div>
            <% } %>
               </td>
           </tr>
           </tbody>
           </table>
       </div>

        <table id="user-details" class="dataset-box">
        <tbody>
        <tr>
            <th>Username</th><td><%= theUser.getId() %></td>
        </tr>
        <tr>
            <th>First Name</th><td><%= StringUtils.defaultString(theUser.getFirstName(), "") %></td>
        </tr>
        <tr>
            <th>Last Name</th><td><%= StringUtils.defaultString(theUser.getLastName(), "") %></td>
        </tr>
        <tr>
            <th>Email</th><td><%= StringUtils.defaultString(theUser.getEmail(), "") %></td>
        </tr>
        <tr>
            <th>Institution</th><td><%= StringUtils.defaultString(theUser.getInstitution()) %></td>
        </tr>
        <tr>
            <th>Is Admin</th><td><%= theUser.getAdminFlag() %></td>
        <tr>
            <th>Created</th><td><%= dto.getCreatedDate() %></td>
        </tr>
        <tr>
            <th>Last Login</th><td><%= dto.getLastLoginDate() %></td>
        </tr>
        <tr>
            <th>Recent Projects</th><td><%= dto.getLastFiveProjects().replace(", ",",<br>")%></td>
        </tr>
        <tr>
            <th>Role(s)</th>
            <td>
            <% if (theUserRoles != null) { 
                   for (UserRoleItem roleItem: theUserRoles) {
                       out.print( roleMap.get(roleItem.getRole()) + "<br />");
                   }
            } %>
            </td>
        </tr>
        </tbody>
        </table>
         
        </div>
</div> <!-- End #contents div -->
</div> <!-- End #main div -->
</td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

</body>
</html>
