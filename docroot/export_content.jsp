<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Benjamin K. Billings
// Version: $Revision: 8083 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2012-11-08 14:57:01 -0500 (Thu, 08 Nov 2012) $
// $KeyWordsOff: $
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>

<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem" %>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HttpSessionInfo"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.util.VersionInformation"%>
<%@page import="com.beginmind.login.model.LoginServicePrincipal" %>
<%

NavigationHelper navigationHelper = HelperFactory.DEFAULT.getNavigationHelper();
HttpSessionInfo sessionInfo = (HttpSessionInfo)session.getAttribute("info");

    // Get list of selected samples and create a string
    String samplesList = "";
    List selectedSamples = navigationHelper.getSelectedSamples(sessionInfo);
    DatasetItem dataset= navigationHelper.getDataset(sessionInfo);
%>

<%
if (selectedSamples != null && !selectedSamples.isEmpty()) {
%>
    <input type="button" value="Export Data" name="export" onclick="javascript:startExport()" id="exportButton" />
<%
} else {
%>
    </h1>
<%
}
%>


<div id="exportPreview">
    <table id="exportPreviewTable"><tr><td>Loading...</td></tr></table>
</div>