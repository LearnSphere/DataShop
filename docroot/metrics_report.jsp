<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2010
  All Rights Reserved
-->
<%
    // Author: Shanwen Yu
    // Version: $Revision: 13459 $
    // Last modified by: $Author: ctipper $
    // Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
    // $KeyWordsOff: $
    //
%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Set"%>
<%@page import="edu.cmu.pslc.datashop.dto.LearnlabDomainMetricsReport"%>
<%@page import="edu.cmu.pslc.datashop.item.AccessRequestStatusItem"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Metrics";
    cssIncludes.add("AdvancedPages.css");
    jsIncludes.add("javascript/MetricsReport.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <div id="metrics-report">
    <h1>Metrics</h1>

        <%
        String td_class_name_odd = "cell";
        String td_class_name_even = "cell even";
        String td_class_name = td_class_name_odd;

        LearnlabDomainMetricsReport report = (LearnlabDomainMetricsReport)session.getAttribute("metrics_report");
        out.println("<h3><i>As of " + report.getFormattedTime() + "</i></h3>");

        Map map = report.getMap();
        int mapSize = map.size();

        String domainTableId = "metric-by-domain";
        String learnlabTableId = "metric-by-learnlab";
        Iterator mapPairs = map.entrySet().iterator();
        for (int i = 0; i < mapSize; i ++){
            Map.Entry entry = (Map.Entry) mapPairs.next();

            if (entry.getKey().equals("Domain")){
                out.println("<h2>By Domain</h2>");
                out.println("<table id=\"" + domainTableId + "\">");
                out.println("<th class=\"col1\">Domain</th><th>Files</th><th>Papers</th><th>Datasets</th>");
                out.println("<th>Student Actions</th><th>Students</th><th>Student Hours</th>");
                int idx = 1;
                List domainList = (List)entry.getValue();
                for (Iterator it = domainList.iterator(); it.hasNext();) {
                	List metricByDomainValues = (List)it.next();
                	 if (idx % 2 == 0) {
                         	td_class_name = td_class_name_even;
	     	         } else {
	                        td_class_name = td_class_name_odd;
	                 }
                	out.println("<tr>");
                	for (ListIterator it2 = metricByDomainValues.listIterator(); it2.hasNext();) {
                		if (it2.nextIndex()==0) {
                		    out.println("<td class=\""+td_class_name+" col1"+"\">");
                                } else {
                                    out.println("<td class=\""+td_class_name+"\">");
                                }
                		out.println(it2.next().toString());
                		out.println("</td>");
                	 }
                	out.println("</tr>");
                	idx ++;
                }
                out.println("<tr class=\"total\"><td class=\"col1\">Total</td><td>"+ report.getTotalDomainFiles());
                out.println("</td><td>" + report.getTotalDomainPapers());
                out.println("</td><td>" + report.getTotalDomainDatasets());
                out.println("</td><td>" + report.getTotalDomainActions());
                out.println("</td><td>" + report.getTotalDomainStudents());
                out.println("</td><td>" + report.getTotalDomainHours());
                out.println("</td></tr>");
                out.println("</table>");
            }else if (entry.getKey().equals("Learnlab")){
                out.println("<h2>By LearnLab</h2>");
                out.println("<table id=\"" + learnlabTableId + "\">");
                out.println("<th class=\"col1\">LearnLab</th><th>Files</th><th>Papers</th><th>Datasets</th>");
                out.println("<th>Student Actions</th><th>Students</th><th>Student Hours</th>");
                List learnlabList = (List)entry.getValue();
                int idx = 1;
                for (Iterator it = learnlabList.iterator(); it.hasNext();) {
                	List metricByLearnlabValues = (List)it.next();
                	if (idx % 2 == 0) {
                      	td_class_name = td_class_name_even;
	     	         } else {
	                        td_class_name = td_class_name_odd;
	                 }
                	out.println("<tr>");
                	for (ListIterator it2 = metricByLearnlabValues.listIterator(); it2.hasNext();) {
                		if (it2.nextIndex()==0) {
                		    out.println("<td class=\""+td_class_name+" col1"+"\">");
                                } else {
                                    out.println("<td class=\""+td_class_name+"\">");
                                }
                		out.println(it2.next().toString());
                		out.println("</td>");
                	 }
                	out.println("</tr>");
                        idx ++;
                }
                out.println("<tr class=\"total\"><td class=\"col1\">Total</td><td>"+ report.getTotalLearnlabFiles());
                out.println("</td><td>" + report.getTotalLearnlabPapers());
                out.println("</td><td>" + report.getTotalLearnlabDatasets());
                out.println("</td><td>" + report.getTotalLearnlabActions());
                out.println("</td><td>" + report.getTotalLearnlabStudents());
                out.println("</td><td>" + report.getTotalLearnlabHours());
                out.println("</td></tr>");
                out.println("</table>");
            }else{
                out.println("found neither Domain or Learnlab");
            }

        }


        %>

        <%
        Map<String, LearnlabDomainMetricsReport> remoteInstancesMap = report.getRemoteInstanceReports();
        if (!remoteInstancesMap.isEmpty()) {
        %>
            <hr/>
            <h1>DataShop Instances</h1>
        <%
        }

        domainTableId = "metric-by-domain-remote";
        learnlabTableId = "metric-by-learnlab-remote";
        int riIndex = 0;
        for (String s : remoteInstancesMap.keySet()) {
            LearnlabDomainMetricsReport remoteReport = remoteInstancesMap.get(s);

            out.println("<h2>" + s + "</h2>");

            Map remoteMap = remoteReport.getMap();
            mapSize = remoteMap.size();

            mapPairs = remoteMap.entrySet().iterator();
            for (int i = 0; i < mapSize; i ++){
                Map.Entry entry = (Map.Entry) mapPairs.next();

                if (entry.getKey().equals("Domain")){
                   out.println("<h3>By Domain</h3>");
                   out.println("<table id=\"" + domainTableId + "_" + riIndex + "\">");
                   out.println("<th class=\"col1\">Domain</th><th>Files</th><th>Papers</th><th>Datasets</th>");
                   out.println("<th>Student Actions</th><th>Students</th><th>Student Hours</th>");
                   int idx = 1;
                   List domainList = (List)entry.getValue();
                   for (Iterator it = domainList.iterator(); it.hasNext();) {
                       List metricByDomainValues = (List)it.next();
                       if (idx % 2 == 0) {
                          td_class_name = td_class_name_even;
	     	       } else {
	                  td_class_name = td_class_name_odd;
	               }
                       out.println("<tr>");
                       for (ListIterator it2 = metricByDomainValues.listIterator(); it2.hasNext();) {
                           if (it2.nextIndex()==0) {
                              out.println("<td class=\""+td_class_name+" col1"+"\">");
                           } else {
                              out.println("<td class=\""+td_class_name+"\">");
                           }
                           out.println(it2.next().toString());
                           out.println("</td>");
                       }
                       out.println("</tr>");
                       idx ++;
                   }
                   out.println("<tr class=\"total\"><td class=\"col1\">Total</td><td>" +
                               remoteReport.getTotalDomainFiles());
                   out.println("</td><td>" + remoteReport.getTotalDomainPapers());
                   out.println("</td><td>" + remoteReport.getTotalDomainDatasets());
                   out.println("</td><td>" + remoteReport.getTotalDomainActions());
                   out.println("</td><td>" + remoteReport.getTotalDomainStudents());
                   out.println("</td><td>" + remoteReport.getTotalDomainHours());
                   out.println("</td></tr>");
                   out.println("</table>");
                } else if (entry.getKey().equals("Learnlab")){
                   out.println("<h3>By LearnLab</h3>");
                   out.println("<table id=\"" + learnlabTableId + "_" + riIndex + "\">");
                   out.println("<th class=\"col1\">LearnLab</th><th>Files</th><th>Papers</th><th>Datasets</th>");
                   out.println("<th>Student Actions</th><th>Students</th><th>Student Hours</th>");
                   List learnlabList = (List)entry.getValue();
                   int idx = 1;
                   for (Iterator it = learnlabList.iterator(); it.hasNext();) {
                       List metricByLearnlabValues = (List)it.next();
                       if (idx % 2 == 0) {
                          td_class_name = td_class_name_even;
	     	       } else {
	                  td_class_name = td_class_name_odd;
	               }
                       out.println("<tr>");
                       for (ListIterator it2 = metricByLearnlabValues.listIterator(); it2.hasNext();) {
              	           if (it2.nextIndex()==0) {
               		      out.println("<td class=\""+td_class_name+" col1"+"\">");
                           } else {
                              out.println("<td class=\""+td_class_name+"\">");
                           }
              		   out.println(it2.next().toString());
               		   out.println("</td>");
               	       }
                       out.println("</tr>");
                       idx ++;
                   }
                   out.println("<tr class=\"total\"><td class=\"col1\">Total</td><td>" +
                               remoteReport.getTotalLearnlabFiles());
                   out.println("</td><td>" + remoteReport.getTotalLearnlabPapers());
                   out.println("</td><td>" + remoteReport.getTotalLearnlabDatasets());
                   out.println("</td><td>" + remoteReport.getTotalLearnlabActions());
                   out.println("</td><td>" + remoteReport.getTotalLearnlabStudents());
                   out.println("</td><td>" + remoteReport.getTotalLearnlabHours());
                   out.println("</td></tr>");
                   out.println("</table>");
                } else{
                   out.println("found neither Domain or Learnlab");
                }
            }
            riIndex++;
        }
        %>
        </div>

        <div style="display:none" id="learnlab_unspecified_tooltip_content"><b>Unspecified</b> indicates that the dataset does not have a LearnLab designation.</div>
        <div style="display:none" id="learnlab_other_tooltip_content"><b>Other</b> indicates that the dataset has a LearnLab different than one of those specifically defined by DataShop.</div>
        <div style="display:none" id="domain_unspecified_tooltip_content"><b>Unspecified</b> indicates that the dataset does not have a Domain designation.</div>
        <div style="display:none" id="domain_other_tooltip_content"><b>Other</b> indicates that the dataset has a Domain different than one of those specifically defined by DataShop.</div>

    </div> <!-- End #metrics-report div -->
    </div> <!-- End #contents div -->
    </div> <!-- End #main div -->

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
