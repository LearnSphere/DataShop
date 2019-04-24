<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2018
  All Rights Reserved
-->
<%
// Author:
// Version: $Revision:  $
// Last modified by: $Author:  $
// Last modified on: $Date:  $
// $KeyWordsOff: $
//
%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="edu.cmu.pslc.datashop.workflows.WorkflowComponentDTO"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageComponentsServlet"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Tigris Components";
    showHelpButton = false;
    cssIncludes.add("workflows/learnsphere.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/workflows/admin_wf_components.js");
    jsIncludes.add("javascript/workflows/lsWorkflowAuthoring.js");

%>

<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    List<WorkflowComponentDTO> wfcDtoList = null;
    if (request.getAttribute("wfcDtoList") != null) {
        wfcDtoList = (List<WorkflowComponentDTO>) request.getAttribute("wfcDtoList");
    }


%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="component-management">
         <h2>Manage Tigris Components</h2>
Force a Reload: Hold down the Shift key and left-click the Reload button<br/>
Press "Ctrl + F5" or press "Ctrl + Shift + R" (Windows, Linux) <br/>
Press "Command + Shift + R" (Mac) <br/>
    <%
    if (wfcDtoList != null) {

        Boolean enabledFlag = true;

        List<String> unorderedComponentTypes = new ArrayList<String>();
        for (WorkflowComponentDTO wfcDto : wfcDtoList) {
            if (!unorderedComponentTypes.contains(wfcDto.getComponentType())) {
                unorderedComponentTypes.add(wfcDto.getComponentType());
            }
        }

        List<String> orderedComponentTypes = new ArrayList<String>();
        orderedComponentTypes.add("Import");
        orderedComponentTypes.add("Transform");
        orderedComponentTypes.add("Analysis");
        orderedComponentTypes.add("Visualization");
        orderedComponentTypes.add("Export");
        unorderedComponentTypes.removeAll(orderedComponentTypes);
        orderedComponentTypes.addAll(unorderedComponentTypes);
        %><form id="componentForm" name="componentForm" method="post" action="ManageComponents"><%

        for (String componentType : orderedComponentTypes) {
            %><table class="wfFormTable">
            <tr><td colspan="10" style="border: none;"><h3><%=componentType %></h3></td></tr>
            <tr>
            <th>Name</th>
            <th>Enabled</th>
            <th>Remote Exec Enabled</th>
            <th>Schema</th>
            <th>Tool Dir</th>
            <th>Tool Path</th>
            <th>Author</th>
            <th>Citation</th>
            <th>Version</th>
            <th>Wrapper</th></tr><%
            Boolean hasChildren = false;

            for (WorkflowComponentDTO wfcDto : wfcDtoList) {
                if (componentType.equalsIgnoreCase(wfcDto.getComponentType())) {
                    hasChildren = true;
                    Boolean isEnabled = false;
                    if (wfcDto.getEnabled()) {
                        isEnabled = true;
                    }
                    Boolean isRemoteExecEnabled = false;
                    if (wfcDto.getRemoteExecEnabled()) {
                        isRemoteExecEnabled = true;
                    }
                %>
                <tr><td style="padding-left: 20px;"><%=wfcDto.getComponentName() %></td>

                    <%
                    String enabledTrue = "";
                    String enabledFalse = "";
                    if (isEnabled) {
                        enabledTrue = " selected=\"selected\"";
                    } else {
                        enabledFalse = " selected=\"selected\"";
                    }
                    String remoteExecEnabledTrue = "";
                    String remoteExecEnabledFalse = "";
                    if (isRemoteExecEnabled) {
                        remoteExecEnabledTrue = " selected=\"selected\"";
                    } else {
                        remoteExecEnabledFalse = " selected=\"selected\"";
                    }
                    %>

                 <td><div id="enabled_option_<%=wfcDto.getComponentId() %>">
                    <select class="formItem" name="enabled" id="enabled_<%=wfcDto.getComponentId() %>">
                           <option value="true" <%=enabledTrue %>>Enabled</option>
                           <option value="false" <%=enabledFalse %>>Disabled</option>
                    </select>
                </div></td>

                 <td><div id="remote_exec_enabled_option_<%=wfcDto.getComponentId() %>>">
                    <select class="formItem" name="remoteExecEnabled" id="remoteExecEnabled_<%=wfcDto.getComponentId() %>">
                           <option value="true" <%=remoteExecEnabledTrue %>>Enabled</option>
                           <option value="false" <%=remoteExecEnabledFalse %>>Disabled</option>
                    </select>
                </div></td>
                 <td><input type="text" class="formItemText" name="schemaPath" id="schemaPath_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getSchemaPath() %>" /></td>
                 <td><input type="text" class="formItemText" name="toolDir" id="toolDir_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getToolDir() %>" /></td>
                 <td><input type="text" class="formItemText" name="toolPath" id="toolPath_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getToolPath() %>" /></td>
                 <td><input type="text" class="formItemText" name="author" id="author_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getAuthor() %>" /></td>
                 <td><input type="text" class="formItemText" name="citation" id="citation_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getCitation() %>" /></td>
                 <td><input type="text" class="formItemText" name="version" id="version_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getVersion() %>" /></td>
                 <td><input type="text" class="formItemText" name="interpreterPath" id="interpreterPath_<%=wfcDto.getComponentId() %>" value="<%=wfcDto.getInterpreterPath() %>" /></td>
                </tr>


                <%
                }
            }
            if (!hasChildren) {
                %><td colspan="9">none available</td><%
            }
            %></table><%
        }
        %>
        <input type="hidden" name="requestingMethod" value="updateComponent">
        </form><%
    }
    %>

    </div>  <!-- End of #component-management div -->

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
