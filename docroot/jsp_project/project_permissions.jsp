<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 14003 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-03-14 10:39:37 -0400 (Tue, 14 Mar 2017) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>

<!-- project-page-specific header stuff -->
<%@ include file="/jsp_project/project_header.jspf" %>

    <div id="project-permissions-div">
        <h4>You currently have
        <% if (hasProjectAdmin) { %>
        <em>admin</em>
        <% } else if (hasProjectEdit) { %>
        <em>edit</em>
        <% } else if (hasProjectView) { %>
        <em>view</em>
        <% } else { %>
        no
        <% } %>
        <% if (adminUserFlag) { %>
            access to this project, but are a DataShop Administrator.
        <% } else { %>
            access to this project.
        <% } %>
        </h4>
        <p>
        You can:
        </p>
        <ul>
        <li>View existing samples</li>
        <li>Export data, use analysis tools, and download files</li>
        <li>Use data in workflows</li>

        <% if (!hasProjectEdit) { %>
        </ul>
        <h4 id="auth-level-desc-heading">Request edit access if you want to:</h4>
        <ul>
        <% } %>
        
        <li>Create samples</li>
        <li>Add knowledge component models</li>
        <li>Add papers, external analyses, and other types of files</li>
        <li>Add custom fields</li>
        <li>Create KC sets</li>
        </ul>
        <%=makeProjectPublicButton %>
        <%=accessRequestButton %>
    </div> <!-- project-permissions-div -->
</div> <!-- project-page div -->
</td>
</tr>

<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="projectPublicDialog" class="projectPublicDialog"> </div>
<div id="requestDialog" class="requestDialog"> </div>

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
