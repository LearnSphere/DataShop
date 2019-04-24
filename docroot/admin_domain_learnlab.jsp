<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2009
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 8659 $
// Last modified by: $Author: bleber $
// Last modified on: $Date: 2013-02-12 10:16:42 -0500 (Tue, 12 Feb 2013) $
// $KeyWordsOff: $
//
%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="edu.cmu.pslc.datashop.dto.AdminDomainLearnLabDTO"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.AdminDomainLearnLabServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Admin - Set Domain/LearnLab";
    showHelpButton = false;
    jsIncludes.add("javascript/Admin.js");
    cssIncludes.add("Admin.css");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">
    
    <div id="admin-domain-learnlab-div">
    <h1>Set Domain/LearnLab</h1>

    <%
    List<AdminDomainLearnLabDTO> data = (List<AdminDomainLearnLabDTO>)
        session.getAttribute(AdminDomainLearnLabServlet.ADMIN_DOMAIN_LEARNLAB_DATA);
    String whichDatasetsAttribute = (String)
        session.getAttribute(AdminDomainLearnLabServlet.ADMIN_DOMAIN_LEARNLAB_EXCLUDED_FLAG);
    String excludedChecked = " checked";
    String allChecked = " ";
    if (whichDatasetsAttribute.equals("false")) {
        excludedChecked = " ";
        allChecked = " checked";
    }
    %>
    <form id="which_datasets_form">

    <input type="radio" name="which_datasets_rb" id="excluded_datasets" value="excluded"
           onClick="getAdminDatasets()" <%=excludedChecked%>/>
    <label for="excluded_datasets">Excluded Datasets</label><br />
    <input type="radio" name="which_datasets_rb" id="all_datasets" value="all"
           onClick="getAdminDatasets()" <%=allChecked%>/>
    <label for="all_datasets">All Datasets</label>
    </form>

    <table id="admin-domain-learnlab-table">
    <tr>
        <th class="col-dataset-id">Id</th>
        <th class="col-dataset-name">Name</th>
        <th class="col-pi">PI</th>
        <th class="col-hours">Student Hours</th>
        <th class="col-domain">Domain/LearnLab</th>
        <th class="col-junk">Junk Flag</th>
    </tr>
    <tr>
    <%
    if (data.size() > 0) {
        for (Iterator iter = data.iterator(); iter.hasNext();) {
            AdminDomainLearnLabDTO dto = (AdminDomainLearnLabDTO) iter.next();
    %>
        <td class="col-dataset-id"><%=dto.getDatasetId()%></td>
        <td class="col-dataset-name"><a href="DatasetInfo?datasetId=<%=dto.getDatasetId()%>"><%=dto.getDatasetName()%></td></a>
        <td class="col-pi"><%=dto.getPrimaryInvestigator()%></td>
        <td class="col-hours"><%=dto.getStudentHours()%></td>
        <td class="col-domain"><%=dto.getDomainLearnLabOptionMenu()%></td>
        <td class="col-junk"><%=dto.getJunkFlagOptionMenu()%></td>
    </tr>
    <%
        } // end for loop
    } else {
    %>
        <td></td>
        <td class="col-no-datasets-found">No datasets found.</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    <%
    }
    %>
    </table>

    </div> <!-- End #admin-domain-learnlab div -->
    </div> <!-- End #contents div -->

    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

</body>
</html>
