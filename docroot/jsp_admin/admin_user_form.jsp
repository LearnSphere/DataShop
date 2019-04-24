<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Young Suk Ahn
// Version: $Revision: 8659 $
// Last modified by: $Author: bleber $
// Last modified on: $Date: 2013-02-12 10:16:42 -0500 (Tue, 12 Feb 2013) $
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
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageUsersServlet"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage User";
    showHelpButton = false;
    cssIncludes.add("Admin.css");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    UserItem userItem = (UserItem)request.getAttribute(AccessContext.KEY_USER_ITEM);
    UserItem aUser = (UserItem)request.getAttribute("user");
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="user-management">
    <h1><a href="<%= ManageUsersServlet.SERVLET_NAME %>">Manage Users</a> &gt; <a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=details&id=<%= URLEncoder.encode((String)aUser.getId(), "UTF-8") %>"><%= aUser.getName() %></a> &gt; Profile</h1>

   <form id="UserForm" name="UserForm" action="<%= ManageUsersServlet.SERVLET_NAME %>?action=edit" method="post" >
       <input type="hidden" name="id" value="<%= aUser.getId() %>" />
       <table id="user-details" class="dataset-box">
       <tbody>
       <tr>
                <th>Username</th><td><%= aUser.getId() %></td>
       </tr>
            <tr>
                <th>First Name</th><td><input type="text" name="firstName" value="<%= StringUtils.defaultString(aUser.getFirstName(), "") %>" /></td>
            </tr>
            <tr>
                <th>Last Name</th><td><input type="text" name="lastName" value="<%= StringUtils.defaultString(aUser.getLastName(), "") %>" /></td>
            </tr>
            <tr>
                <th>Email</th><td><input type="text" name="email" value="<%= StringUtils.defaultString(aUser.getEmail(), "") %>" /></td>
            </tr>
            <tr>
                <th>Institution (optional)</th><td><input type="text" name="institution" value="<%= StringUtils.defaultString(aUser.getInstitution(), "") %>" /></td>
            </tr>
            <tr>
                <th>Is Admin</th><td><select name="adminFlag" >
                        <option value="false" <% out.print( !aUser.getAdminFlag() ? "selected=\"selected\"" : "" ); %> >false</option>
                        <option value="true" <% out.print( aUser.getAdminFlag() ? "selected=\"selected\"" : "" ); %> >true</option>
                    </select>
                </td>
            <tr>
                <th>Created</th><td><%= aUser.getCreationTime() %></td>
            </tr>
            <tr>
                <th>&nbsp;</th><td> <input type="submit" value="Save" /> <input type="reset" value="Reset" /> </td>
            </tr>
            </tbody>
         </table>
         </form>
     </div>
    
        </div> <!-- End #contents div -->
    </div> <!--  End #main div -->

    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/metrics-report.jspf" %>
</div>

</body>
</html>
