<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2015
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 15738 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
// $KeyWordsOff: $
//
%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageInstanceDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.admin.ManageInstanceServlet"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Manage Instance";
    showHelpButton = false;
    cssIncludes.add("Admin.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/Admin.js");
%>

<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%
    ManageInstanceDto dto = (ManageInstanceDto) request.getAttribute("manageInstanceDto");

    String disabledStr = !dto.getIsSlave() ? "disabled=\"disabled\"" : "";

    Boolean useSsl = dto.getUseSslSmtp();
    String disabledSslStr = !useSsl ? "disabled=\"disabled\"" : "";
    String hiddenStr = !useSsl ? "style=\"display:none\"" : "";

    Boolean showErrorMsg = false;
    Boolean showWarningMsg = false;
    Boolean showSuccessMsg = false;
    String msgText = (String)session.getAttribute(ManageInstanceServlet.MESSAGE);
    String msgLevel = (String)session.getAttribute(ManageInstanceServlet.MESSAGE_LEVEL);
    if (msgLevel != null && msgText != null) {
        if (msgLevel.equals(ManageInstanceServlet.ERROR)) {
            showErrorMsg = true;
        } else if (msgLevel.equals(ManageInstanceServlet.WARNING)) {
            showWarningMsg = true;
        } else if (msgLevel.equals(ManageInstanceServlet.SUCCESS)) {
            showSuccessMsg = true;
        }

        // Reset once read.
        session.setAttribute(ManageInstanceServlet.MESSAGE, null);
        session.setAttribute(ManageInstanceServlet.MESSAGE_LEVEL, null);
    }

%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <% if (showErrorMsg) { %>
       <script type="text/javascript">errorPopup("<%=msgText %>");</script>
    <% } else if (showWarningMsg) { %>
       <script type="text/javascript">warningPopup("<%=msgText %>");</script>
    <% } else if (showSuccessMsg) { %>
       <script type="text/javascript">successPopup("<%=msgText %>");</script>
    <% } %>

    <div id="instance-management">
         <h2>Manage Instance</h2>

         <form id="InstanceForm" name="InstanceForm" action="<%= ManageInstanceServlet.SERVLET_NAME %>?action=edit" method="post" >
               <table id="instance-config-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>Configured by</th><td><%=dto.getConfiguredBy() %></td>
                      </tr>
                      <tr>
                        <th>Configured date</th><td><%=dto.getConfiguredTime() %></td>
                      </tr>
                      </tbody>
               </table>
               <table id="instance-url-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>DataShop URL</th><td><input id="ds-url-input" type="text" name="datashopUrl" value="<%= StringUtils.defaultString(dto.getDatashopUrl(), "") %>" /></td>
                      </tr>
                      </tbody>
               </table>
               <table id="instance-email-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>Is Sendmail Active ?</th>
                        <td><select id="sendmail-input" name="isSendmailActive" >
                                    <option value="false" <% out.print( !dto.getIsSendmailActive() ? "selected=\"selected\"" : "" ); %> >false</option>
                                    <option value="true" <% out.print( dto.getIsSendmailActive() ? "selected=\"selected\"" : "" ); %> >true</option>
                            </select></td>
                      </tr>
                      <tr>
                        <th>DataShop Help Email</th><td><input id="help-email-input" type="text" name="datashopHelpEmail" value="<%= StringUtils.defaultString(dto.getDatashopHelpEmail(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>DataShop RM Email</th><td><input id="rm-email-input" type="text" name="datashopRmEmail" value="<%= StringUtils.defaultString(dto.getDatashopRmEmail(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>DataShop Bucket Email</th><td><input id="bucket-email-input" type="text" name="datashopBucketEmail" value="<%= StringUtils.defaultString(dto.getDatashopBucketEmail(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>DataShop SMTP Host</th><td><input id="smtp-host-input" type="text" name="datashopSmtpHost" value="<%= StringUtils.defaultString(dto.getDatashopSmtpHost(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>DataShop SMTP Port</th><td><input id="smtp-port-input" type="text" name="datashopSmtpPort" value="<%= StringUtils.defaultString(dto.getDatashopSmtpPort(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>Use secure SMTP (SSL)?</th>
                        <td><input id="use-ssl-smtp-input" type="checkbox" name="useSslSmtp"
                                   <% out.print( dto.getUseSslSmtp() ? "checked=\"checked\"" : "" ); %>>
                        </td>
                      </tr>
                      <tr id="smtp-user-row" <%=hiddenStr %>>
                        <th>DataShop SMTP User</th><td><input id="smtp-user-input" type="text" name="datashopSmtpUser" value="<%= StringUtils.defaultString(dto.getDatashopSmtpUser(), "") %>" <%=disabledSslStr %> />
                        <label for="smtp-user-input" id="smtp-user-label">required if using SSL</label></td>
                      </tr>
                      <tr id="smtp-password-row" <%=hiddenStr %>>
                        <th>DataShop SMTP Password</th><td><input id="smtp-password-input" type="text" name="datashopSmtpPassword" value="<%= StringUtils.defaultString(dto.getDatashopSmtpPassword(), "") %>" <%=disabledSslStr %> />
                        <label for="smtp-password-input" id="smtp-password-label">required if using SSL</label></td>
                      </tr>
                      </tbody>
               </table>
               <table id="instance-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>Is this instance a slave?</th>
                        <td><input id="is-slave-input" type="checkbox" name="isSlave"
                                   <% out.print( dto.getIsSlave() ? "checked=\"checked\"" : "" ); %>>
                        </td>
                      </tr>
                      <tr>
                        <th>Slave ID</th><td><input id="slave-id-input" type="text" name="slaveId" value="<%= StringUtils.defaultString(dto.getSlaveId(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      <tr>
                        <th>Master User *</th><td><input id="master-user-input" type="text" name="masterUser" value="<%= StringUtils.defaultString(dto.getMasterUser(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      <tr>
                        <th>Master URL <b>*</b></th><td><input id="master-url-input" type="text" name="masterUrl" value="<%= StringUtils.defaultString(dto.getMasterUrl(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      <tr>
                        <th>Master Schema <b>*</b></th><td><input id="master-schema-input" type="text" name="masterSchema" value="<%= StringUtils.defaultString(dto.getMasterSchema(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      <tr>
                        <th>Slave API Token<b>*</b></th><td><input id="slave-apitoken-input" type="text" name="slaveApiToken" value="<%= StringUtils.defaultString(dto.getSlaveApiToken(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      <tr>
                        <th>Slave Secret<b>*</b></th><td><input id="slave-secret-input" type="text" name="slaveSecret" value="<%= StringUtils.defaultString(dto.getSlaveSecret(), "") %>" <%=disabledStr %> /></td>
                      </tr>
                      </tbody>
               </table>
               <table id="instance-github-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>GitHub Client ID *</th><td><input id="gh-client-id-input" type="text" name="ghClientId" value="<%= StringUtils.defaultString(dto.getGithubClientId(), "") %>" /></td>
                      </tr>
                      <tr>
                        <th>GitHub Client Secret *</th><td><input id="gh-client-secret-input" type="text" name="ghClientSecret" value="<%= StringUtils.defaultString(dto.getGithubClientSecret(), "") %>" /></td>
                      </tr>
                      </tbody>
               </table>
               <table id="instance-wf-details" class="dataset-box">
                      <tbody>
                      <tr>
                        <th>Component heap size *</th><td><input id="wfc-heap-size" type="text" name="wfcHeapSize" value="<%= dto.getWfcHeapSize() != null ? dto.getWfcHeapSize() : "" %>" />
                        <label for="wfc-heap-size">e.g., 1024</label></td>
                      </tr>
                      <tr>
                        <th>Workflow Component Dir *</th><td><input id="wfc-dir-input" type="text" name="wfcDir" value="<%= StringUtils.defaultString(dto.getWfcDir(), "") %>" />
                        <label for="wfc-dir-input">e.g., C:/git-repos/WorkflowComponents (windows) or /datashop/workflow_components (linux)</label></td>
                      </tr>
                      <tr>
                        <th>Workflow Slave (optional)</th><td><input id="wfc-slave-instance" type="text" name="wfcRemote" value="<%= StringUtils.defaultString(dto.getWfcRemote(), "") %>" />
                        <label for="wfc-slave-instance">e.g., http://localhost:9000</label></td>
                      </tr>
                      </tbody>
               </table>
               <div id="instance-warning-div"><p><b>*</b> We strongly suggest you not change these
                    values. They have been configured specifically to allow remote (slave)
                    instances to work with the production (CMU) DataShop server.</p>
               </div>
               <div id="submit-reset-div">
                    <input id="admin-submit" type="submit" value="Save" disabled />
                    <input id="admin-reset" type="reset" value="Reset" disabled />
               </div>
         </form>

    </div>  <!-- End of #instance-management div -->

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
