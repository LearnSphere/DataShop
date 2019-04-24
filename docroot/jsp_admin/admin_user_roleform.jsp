<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2010
  All Rights Reserved
-->
<%
// Author: Young Suk Ahn
// Version: $Revision: 15625 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-10-23 09:57:24 -0400 (Tue, 23 Oct 2018) $
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
    Set<String> userRoleSet = (Set<String>)request.getAttribute("userRoleSet");
    Map<String, String> roleMap = (Map<String, String>)request.getAttribute("roleMap");
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="user-management">
         <h1><a href="<%= ManageUsersServlet.SERVLET_NAME %>">Manage Users</a> &gt; <a href="<%= ManageUsersServlet.SERVLET_NAME %>?action=details&id=<%= URLEncoder.encode((String)aUser.getId(), "UTF-8") %>"><%= aUser.getName() %></a> &gt; Roles</h1>

         <form id="UserForm" name="UserForm" action="<%= ManageUsersServlet.SERVLET_NAME %>?action=roleedit" method="post" >
            <input type="hidden" name="id" value="<%= aUser.getId() %>" />
            <table id="user-details" class="dataset-box">
            <tbody>
<% for(Map.Entry<String, String> entry: roleMap.entrySet()) { 
       String theValue = entry.getValue();
       String theKey = entry.getKey();
       if ((theKey.equals(UserRoleItem.ROLE_LOGGING_ACTIVITY)) ||
           (theKey.equals(UserRoleItem.ROLE_WEB_SERVICES)) ||
           (theKey.equals(UserRoleItem.ROLE_EXTERNAL_TOOLS)) ||
           (theKey.equals(UserRoleItem.ROLE_DATASHOP_EDIT))) {
           theValue += " *";
       }
%>
            <tr>
                <th><%= theValue %></th>
                <td><input type="checkbox" name="<%=entry.getKey() %>" value="<%= ManageUsersServlet.CHECKED_VAL %>" <% out.print(userRoleSet.contains(entry.getKey()) ? "checked='checked'": ""); %>/></td>
            </tr>
<% } %>
            <tr>
                <th>&nbsp;</th><td><input type="submit" value="Save" /> <input type="reset" value="Reset" /></td>
            </tr>
            </tbody>
            </table>
         </form>

         <div id="user-management-note" class="information-box">
              <p>* Email will be sent to the user when these roles are granted.</p>
         </div>
    </div>  <!-- End of #user-management div -->
                
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
