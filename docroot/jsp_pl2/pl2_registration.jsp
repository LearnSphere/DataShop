<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2019
  All Rights Reserved
-->
<%
// Author: Peter Schaldenbrand
// Version: $Revision: 11648 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-10-07 15:50:09 -0400 (Tue, 07 Oct 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.auth.LoginInfo"%>
<%@page import="edu.cmu.pl2.servlet.DtoHelper"%>
<%
    String institutionPwd = (String)session.getAttribute("institutionPassword");

    // Get a list of advisors at this school for the user to pick from
    String institutionIdStr = request.getParameter("institution_select");
    Long institutionId = null;
    try {
        institutionId = new Long(institutionIdStr);
    } catch(Exception e) {
        institutionId = 1L;
    }
    Map<Long, String> advisorMap = DtoHelper.getAdvisorsAtInstitutionMap(institutionId, institutionPwd);

    LoginInfo loginInfo = (LoginInfo) request.getAttribute("pl2_loginInfo");

    String accountId = "";
    String email = "";
    if (loginInfo != null) {
        //accountId = loginInfo.getAccountId();
        accountId = loginInfo.getLoginId();
        email = loginInfo.getEmail();
    }
%>

<html>
<head>
    <title>
<%
        out.print("PL2 Registration");
%>
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="Content-Type" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/jquery-ui-1.8.18.custom.css")%>" />
    <link rel="stylesheet" type="text/css" href="<%=response.encodeURL("/v/css/pl2/pl2.css")%>" />

    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-1.7.1.min.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/lib/jquery-ui-1.8.17.custom.min.js")%>"></script>
    <script type="text/javascript" src="<%=response.encodeURL("/v/javascript/pl2/pl2Helper.js")%>"></script>

<%@ include file="/google-analytics.jspf" %>
</head>

<div class="header">
    <h3 class="navbar-brand">
        Welcome to the Personalized Learning<span class="exponent">2</span> App
    </h3>
</div>

<div id="pl2-registration">
    <h1>Create an account</h1>

    <form id="registrationForm" name="registrationForm" method="post" action="PL2Login">

        <table id="create-new-account">
            <tr><td colspan="2"><input type="hidden" name="requestingMethod" value="PL2LoginServlet.register" /></td></tr>
            <tr style="display:none">
                <td class="label"><label for="accountID">Username</label></td>
                <td><input type="text" name="newAccountId" id="accountID" size="32" value="<%=accountId%>" title="Please enter a unique username" /></td>
            </tr>
            <tr>
                <td class="label"><label for="advisor">Mentor</label></td>
                <td>
                    <select id="advisor_select" name="advisor_select" form="registrationForm">
                    <%for(Long id : advisorMap.keySet()) {%>
                        <option value="<%=id %>"> <%=advisorMap.get(id) %> </option>
                    <%}%>
                    </select>
                </td>
            </tr>
            <tr style="display:none">
                <td class="label"><label for="email">Email</label></td>
                <td><input type="text" name="email" id="email" size="32" value="<%=email%>" title="Please enter your email address." /></td>
            </tr>

            <tr>
                <td></td>
                <td class="submit">
                    <input name="Submit" value="Create Account" class="ui-button" type="submit" id="submit" />
                </td>
            </tr>
        </table>
    </form>
</div>   <!-- registration -->

<script>

jQuery(document).ready(function() {
    // body has a font-size in terms of vw.  Make sure it has a minimum value
    jQuery(window).resize(function() {
        ensureTextIsLargeEnough();
    });
    ensureTextIsLargeEnough();
});

function ensureTextIsLargeEnough() {
    jQuery('body').css({ "font-size": "1.2vw" })
    if (parseInt(jQuery('body').css("fontSize")) < 16) {
        jQuery('body').css({ "font-size": "16px" });
    }
}


</script>
</html>
