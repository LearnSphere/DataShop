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

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>
<%
    pageTitle = "Web Services Credentials";
    jsIncludes.add("javascript/WebServices.js");
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

    <div id="web-services">
    <h1>Web Services Credentials</h1>

    <p>You can use DataShop web services to enable your program or web site to retrieve
    DataShop data and (eventually) insert data back to the central repository, all through
    a simple application programming interface (API).  Our service follows the
    <a href="http://en.wikipedia.org/wiki/Representational_State_Transfer">REST
    guidelines</a>, which means that requests to web services are done over HTTP using URLs
    that represent resources.</p>
    <%
    if (hasWebServicesRole) {
        boolean hasAccessKey = user.getApiToken() != null ? true : false;
        String accessKeyID = "You do not have a key yet";
        String secretAccessKey = "";
        if (hasAccessKey) {
            accessKeyID = user.getApiToken();
            secretAccessKey = user.getSecret();
        }
    %>
        <h2>Your Access Key</h2>
        <table id="access-key-table">
        <tr>
            <th>Access Key ID</th>
            <th>Secret Access Key</th>
        </tr>
        <tr>
            <td><%=accessKeyID%></td>
            <td><%=secretAccessKey%></td>
        </tr>
        </table>
        <button type="button" id="create_access_key_button">Generate a new access key</button>
    <%
    } else {
        Boolean webServRoleReqFlag = (Boolean)session.getAttribute("web_services_requested_flag");
    %>

        <%
        if (!webServRoleReqFlag) {
        %>
        <h2>Get started with DataShop Web Services</h2>

        <p>To use DataShop web services, you must first request access.  <strong>By using web services,
        you are accessing and potentially modifying a shared resource with limited bandwidth
        and processing power.</strong>  Please read the following Terms of Services and click
        "Agree and Request Access" to request access.
        </p>
        <div id="web-services-tos">
        <%@ include file="help/web_services_agreement.jspf" %>

        </div>
        <div id="request-button-div">
        <button type="button" id="web_services_request_role_button">I agree and would like to request
        access to use DataShop Web Services</button>
        </div>
        <%
        } else {
        %>
        <div id="request-thank-you" class="information-box">
        <strong>Thank you for requesting access to DataShop Web Services.</strong> We will review your request and grant you
        access shortly. When we've given you access, you'll see a section at the top of this page for
        creating public/private access keys. (We'll also email you.) Until then, please be patient, and kindly
        <a href="help?page=contact">remind us</a> in a couple of days if you don't hear from us.
        </div>
        <%
        }
        %>

    <%
    } // end else
    %>


    <h2>Access Credentials</h2>
    <p>To use DataShop web services, you need to identify yourself as the sender of each request.
    This is accomplished by sending a digital signature that is derived from a pair of
    public/private access keys.</p>

    <h2>Access Key ID</h2>
    <p>Your Access Key ID identifies you as the party responsible for service requests.
    Include it with each request you send to us.</p>

    <h2>Secret Access Key</h2>
    <p>Your Access Key ID has a Secret Access Key associated with it. Use your Secret Access Key
    to calculate a signature to include in requests to DataShop web services. Your Secret Access Key
    is a secret, and should be known only by you and DataShop. You should never include your Secret
    Access Key in your requests to DataShop web services. You should never email your Secret Access
    Key to anyone. It is important to keep your Secret Access Key confidential to protect your
    account.</p>

    <h2>Web Services User Agreement</h2>
    <p>Our user agreement can be viewed <a href="help?page=webServicesUserAgreement">here</a>.</p>
    </div> <!-- End #contents div -->
    </div> <!-- End #logging-activity div -->

    </td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/web-services.jspf" %>
</div>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="requestWebSvcRoleDialog" class="requestWebSvcRoleDialog"></div>

</body>
</html>