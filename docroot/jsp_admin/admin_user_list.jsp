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
<%@page import="edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersServlet"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<!-- header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage User";
    showHelpButton = false;

    cssIncludes.add("Admin.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/AdminUser.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
//UserItem userItem = (UserItem)request.getAttribute(AccessContext.KEY_USER_ITEM);
String message = (String)request.getAttribute("message");

UserItem criteria = (UserItem)request.getAttribute("criteria");
List<ManageUsersDto> userList = (List<ManageUsersDto>)request.getAttribute("userList");
FastDateFormat formatter = FastDateFormat.getInstance(ManageUsersServlet.DATE_FORMAT);
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="user-management">
    <h1>Manage Users</h1>

        <form id="UserQueryForm" name="UserQueryForm" action="<%= ManageUsersServlet.SERVLET_NAME %>" method="get" >
        <div id="UserFilterOptions">
        <table>
        <tr>
            <td>Username</td>
            <td>Name</td>
            <td>Email</td>
            <td>Institution</td>
            <td>Created After</td>
            <td>Admin Flag</td>
        </tr>
        <tr>
            <td><input type="text" name="idLike" value="<%= StringUtils.defaultString( (String)criteria.getId(), "") %>" /></td>
            <td><input type="text" name="nameLike" value="<%= StringUtils.defaultString(criteria.getFirstName(), "") %>" /></td>
            <td><input type="text" name="emailLike" value="<%= StringUtils.defaultString(criteria.getEmail(), "") %>" /></td>
            <td><input type="text" name="institutionLike" value="<%= StringUtils.defaultString(criteria.getInstitution(), "") %>" /></td>
            <td><input type="text" id="createdFrom" name="createdFrom" value="<%= criteria.getCreationTime() != null ? formatter.format(criteria.getCreationTime()) : "" %>" /></td>
            <td><select name="adminFlag" >
                <option value="all" id="adminFlag_all" >ALL</option>
                <option value="admin" <% out.print(  criteria.getAdminFlag() != null && criteria.getAdminFlag() ? "selected='selected'": ""); %>>Admins only</option>
                <option value="nonadmin" <% out.print( criteria.getAdminFlag() != null && !criteria.getAdminFlag() ? "selected='selected'": ""); %> >Non admins only</option>
            </select></td>
        </tr>
        </table>
        </div>
        <div>
        <input type="submit" value="Search" />
        <input id="btn_clear" type="button" value="Clear" onclick="javascript:clearForm()" />
        <div>
        </form>
        <% if (message != null) {%>
            <div class="message"><%=message%></div>
        <% }%>
        <br />
        <table id="users" class="dataset-box">
            <thead>
            <tr>
                <th>Username</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Email Address</th>
                <th>Institution</th>
                <th>Is Admin</th>
                <th>Created</th>
                <th>Last Login</th>
            </tr>
            </thead>
            <tbody>
<% 
for (ManageUsersDto dto : userList ) {
    UserItem aUser = dto.getUserItem();
%>
            <tr>
                <td><a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=details&id=<%= URLEncoder.encode((String)aUser.getId(), "UTF-8") %>"><%= aUser.getId() %></a></td>
                <td><%= StringUtils.defaultString(aUser.getFirstName(), "") %></td>
                <td><%= StringUtils.defaultString(aUser.getLastName(), "") %></td>
                <td><%= StringUtils.defaultString(aUser.getEmail(), "") %></td>
                <td><%= StringUtils.defaultString(aUser.getInstitution(), "") %></td>
                <td><%= aUser.getAdminFlag() %></td>
                <td><%= dto.getCreatedDate() %></td>
                <td><%= dto.getLastLoginDate() %></td>
            </tr>
<% } %>
            </tbody>
         </table>
            </div>
    
        </div> <!-- End #contents div -->
    </div> <!--  End #main div -->

    </td>
</tr>

<script type="text/javascript">initAdminUserListPage();</script>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

</body>
</html>
