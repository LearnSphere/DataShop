<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
    // Author: Alida Skogsholm
// Version: $Revision: 10435 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.CreateProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.CreateProjectServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectReviewDto"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Create a project";
    cssIncludes.add("UploadDataset.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/UploadDataset.js");
    jsIncludes.add("javascript/object/Truncator.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>


<!-- code -->
<%
    CreateProjectDto dto = (CreateProjectDto)request.getAttribute(
    CreateProjectServlet.REQ_ATTRIB_SETTINGS);

    String dct = dto.getDataCollectionType();
    String checked = "checked";
    String dctNotSpecChecked = "";
    String dctNotHumanChecked = "";
    String dctNotReqChecked = "";
    String dctReqChecked  = "";
    if (dct == null || dct.equals(ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED)) {
        dctNotSpecChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT)) {
        dctNotHumanChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ)) {
        dctNotReqChecked = checked;
    } else if (dct.equals(ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ)) {
        dctReqChecked = checked;
    } else {
        dctNotSpecChecked = checked;
    }
%>


<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="create_project_div">
    <h1>Create a project</h1>
    
    <form id="create_project_form" 
        name="create_project_form" 
      method="post" 
      action="CreateProject">
    <input type="hidden" name="action" value="create" />
    <table id="create_project_table" class="account_profile_table_style">

        <tr><td colspan="2"></td></tr>
        
        <tr>
          <td class="label labelRight">Project Name</td>
          <td><input type="text" name="new_project_name" id="new_project_name"
                    value="<%=dto.getNewProjectName()%>"
                    title="Please enter a unique project name." /></td>
        </tr>
        <% if (dto.getErrorFlag()) { %>
        <tr>
           <td></td>
           <td class="errorMessage">&nbsp;&nbsp;<%=dto.getErrorMessage() %></td>
        </tr>
        <% } %>
        <tr>
        <td class="label labelRight">Data Collection Type</td>
        <td>
        <table id="dctTable">
          <tr>
          <td class="dctTd">
          <input type="radio" name="dataCollectionType" id="dctInput1"
                value="<%=ProjectItem.DATA_COLLECTION_TYPE_NOT_SPECIFIED%>"
                <%=dctNotSpecChecked%>>
          </td><td>
          <label for="dctInput1">
                <%=ProjectReviewDto.DATA_COLLECTION_NOT_SPECIFIED%>
                </label>
                <a id="whyDct" class="recentLink"
                href="help?page=irb">Why specify the data collection type?</a>
          </tr><tr>
          <td class="dctTd">
          <input type="radio" name="dataCollectionType" id="dctInput2"
                value="<%=ProjectItem.DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT%>"
                <%=dctNotHumanChecked%>>
          </td><td>
          <label for="dctInput2">
                <%=ProjectReviewDto.DATA_COLLECTION_NOT_HUMAN_SUBJECT%>
                </label>
          </tr><tr>
          <td class="dctTd">
          <input type="radio" name="dataCollectionType" id="dctInput3"
                value="<%=ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ%>"
                <%=dctNotReqChecked%>>
          </td><td>
          <label for="dctInput3">
                <%=ProjectReviewDto.DATA_COLLECTION_CONSENT_NOT_REQD%>
                </label>
          </tr><tr>
          <td class="dctTd">
          <input type="radio" name="dataCollectionType" id="dctInput4"
                value="<%=ProjectItem.DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ%>"
                <%=dctReqChecked%>>
          </td><td>
          <label for="dctInput4">
                <%=ProjectReviewDto.DATA_COLLECTION_CONSENT_REQUIRED%>
                </label>
          </td></tr>
        </table> <!-- end dctTable -->
        </td>
        </tr>
        
        <tr><td></td>
        <td class="submit">
        <div id="create_button_div" class="createProjectButton">
        <button id="create_button" type="submit">Create</button>
        </div>
        </td>
        </tr>
        
    </table>
    </form>
  
</div> <!-- End #external-tools div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Un-populated div needed for modal windows using jQuery -->
<div id="addToolDialog" class="addToolDialog"></div>
<div id="requestRoleDialog" class="addToolDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/create-project.jspf" %>
</div>

</body>
</html>
