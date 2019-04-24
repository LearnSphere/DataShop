<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2009
  All Rights Reserved
-->
<%
    // Author: Alida Skogsholm
    // Version: $Revision: 10541 $
    // Last modified by: $Author: ctipper $
    // Last modified on: $Date: 2014-02-11 14:11:28 -0500 (Tue, 11 Feb 2014) $
    // $KeyWordsOff: $
    //
%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="edu.cmu.pslc.datashop.dto.LoggingActivityOverviewReport"%>
<%@page import="edu.cmu.pslc.datashop.dto.LoggingActivitySession"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Logging Activity";
    jsIncludes.add("javascript/LoggingActivity.js");
    cssIncludes.add("AdvancedPages.css");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="logging-activity">
    <h1>Logging Activity</h1>
        <div id="usage-guidelines" class="information-box">
        <h3>Usage Guidelines</h3>
        <ul>
            <li>Please be patient: this report may take a while to run. If the page hasn't finished loading,
            do not try to reload it.</li>
            <li>Try to limit how often you (re)load this page. For example, if the page takes a minute to load,
            do not reload it for a couple of minutes.</li>
            <li>If the page takes an inordinate amount of time to load, please <a href="help?page=contact">let us know</a>.</li>
        </ul>
        </div>
    <%
        if (hasLoggingActivityRole) {
            // Get the report from the HTTP session from the Servlet.
                LoggingActivityOverviewReport report = (LoggingActivityOverviewReport) session
                        .getAttribute("logging_activity_overview_report");
                int numDatasets = report.numDatasets();
                String selectedValue = "";
                int numMinutes = (Integer) session
                        .getAttribute("logging_activity_num_minutes");
    %>

    <p>Show logging activity for the last <select id="logging_activity_num_minutes">
    <%
        selectedValue = (numMinutes == 5) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="5">5 minutes</option>
    <%
        selectedValue = (numMinutes == 15) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="15">15 minutes</option>
    <%
        selectedValue = (numMinutes == 30) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="30">30 minutes</option>
    <%
        selectedValue = (numMinutes == 60) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="60">1 hour</option>
    <%
        selectedValue = (numMinutes == 240) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="240">4 hours</option>
    <%
        selectedValue = (numMinutes == 480) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="480">8 hours</option>
    <%
        selectedValue = (numMinutes == 960) ? "selected=\"selected\""
                    : "";
    %>
    <option value="960" <%=selectedValue%>>12 hours</option>
    <%
        selectedValue = (numMinutes == 1440) ? "selected=\"selected\""
                    : "";
    %>
    <option <%=selectedValue%> value="1440">24 hours</option>
    </select>.
    <p class="description">From <%=report.getFormattedServerReceiptTimeCutoff()%>
    to <%=report.getFormattedStartTime()%>, <%=numDatasets%> datasets received log messages.</p>

    <%
        Set<String> reportDatasetNameList = report.getDatasetNames();
            for (Iterator dsIter = reportDatasetNameList.iterator(); dsIter
                    .hasNext();) {
                String currentDatasetName = (String) dsIter.next();
                List<LoggingActivitySession> sessList = report
                        .getSessions(currentDatasetName);
                int numSessions = sessList.size();
    %>
    <%
    if (currentDatasetName == null) {
    %>
    <h2>Dataset name is unknown &mdash; <%=numSessions%> sessions</h2>
    <p class='note'>The context message is outside the given time frame. Try increasing the time frame to get the dataset name for these sessions.</p>
    <%
    } else {
    %>
    <h2>Dataset: <%=currentDatasetName%> &mdash; <%=numSessions%> sessions</h2>
    <%
    }
    %>
    <table>
        <tr>
        <th>Session ID</th>
        <th>First Message Receipt Time</th>
        <th>Last Message Receipt Time</th>
        <th>Total</th>
        <th>Context</th>
        <th>Tool</th>
        <th>Tutor</th>
        <th>Plain</th>
        </tr>
        <%
            if (numSessions > 0) {
                for (Iterator iter = sessList.iterator(); iter.hasNext();) {
                    LoggingActivitySession sessionData =
                        (LoggingActivitySession) iter.next();
        %>
        <tr>
            <td><%=sessionData.getSessionId()%></td>
            <td><%=sessionData.getMinServerReceiptTime()%></td>
            <td><%=sessionData.getMaxServerReceiptTime()%></td>
            <td><%=sessionData.getNumTotalMessages()%></td>
            <td><%=sessionData.getNumContextMessages()%></td>
            <td><%=sessionData.getNumToolMessages()%></td>
            <td><%=sessionData.getNumTutorMessages()%></td>
            <td><%=sessionData.getNumPlainMessages()%></td>
        </tr>
        <%
                } // end for loop
            } // end if on numSession
        %>
    </table>
    <%
        } // end for loop on dataset names
    %>

    <%
            } else {
                Boolean logActRoleReqFlag = (Boolean) session
                        .getAttribute("logging_activity_requested_flag");
                if (!logActRoleReqFlag) {
        %>
        <p>When activated, this report provides a logging diagnostic by displaying counts of all
        recent log messages received by the <a href="help?page=logging#logging-urls">logging server</a>,
        organized by dataset and student session. It is intended for use by technical researchers or staff
        who are in the process of verifying logging activity from a study site.</p>

        <p><strong>Since this report is generated by querying the live logging
        database, and using it has the potential to affect that database, we are requesting that you
        agree to follow the guidelines above before using the page.</strong></p>

        <button type="button" id="log_activity_request_role_button">I agree. Please grant me access to use this report.</button>
        <%
            } else {
        %>
    <p><strong>Thank you for requesting access to this report. We will review
    your request and grant you access shortly. Until then, please be patient, and kindly <a
        href="help?page=contact">remind us</a> in a couple of days (or sooner, if it's urgent) if
    you don't hear from us.</strong></p>
    <%
        }
    %>
    <%
        } // end else
    %>

    </div> <!-- End #contents div -->
    </div> <!-- End #logging-activity div -->
    </div> <!--  End #main div -->

    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/logging-activity.jspf" %>
</div>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="requestLoggingRoleDialog" class="requestLoggingRoleDialog"></div>

</body>
</html>
