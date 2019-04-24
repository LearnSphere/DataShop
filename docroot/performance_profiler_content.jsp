<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Benjamin Billings
// Version: $Revision: 13101 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2016-04-14 15:12:04 -0400 (Thu, 14 Apr 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.Serializable"%>

<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.Serializable"%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>

<%@page import="edu.cmu.pslc.datashop.dto.ProfilerOptions"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillModelItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.performanceprofiler.PerformanceProfilerProducer"%>
<%@page import="edu.cmu.pslc.datashop.servlet.performanceprofiler.PerformanceProfilerContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>

<%

NavigationHelper navigationHelper = HelperFactory.DEFAULT.getNavigationHelper();
DatasetContext sessionInfo = (DatasetContext)session.getAttribute("datasetContext_"
        + request.getParameter("datasetId"));
PerformanceProfilerContext ppContext = sessionInfo.getPerformanceProfilerContext();

String ppViewByCategory = ppContext.getViewByCategory();
String ppViewByType = ppContext.getViewByType();
String ppSortBy = ppContext.getSortBy();
Boolean ppShowPredicted = ppContext.getDisplayPredicted();
Boolean ppSortAscending = ppContext.getSortByAscendingDirection();
Integer ppTopLimit = ppContext.getTopLimit();
Integer ppBottomLimit = ppContext.getBottomLimit();
Boolean ppDisplayUnmapped = ppContext.getDisplayUnmapped();

String samplesList = "";
String datasetName = "";

if (ppTopLimit == null) {
    ppTopLimit = new Integer(6);
} else if (ppTopLimit.intValue() == -1) {
    ppTopLimit = null;
}

if (ppBottomLimit == null) {
    ppBottomLimit = new Integer(6);
} else if (ppBottomLimit.intValue() == -1) {
    ppBottomLimit = null;
}

if (ppDisplayUnmapped == null) {
    ppDisplayUnmapped = Boolean.TRUE;
}

if (ppViewByCategory == null || ppViewByCategory.compareTo("") == 0) {
    ppViewByCategory = ProfilerOptions.TYPE_PROBLEM;
}
if (ppViewByType == null || ppViewByType.compareTo("") == 0) {
    ppViewByType = ProfilerOptions.VIEW_ERROR_RATE;
}
if (ppSortBy == null || ppSortBy.compareTo("") == 0) {
    ppSortBy = ProfilerOptions.SORT_BY_ERROR_RATE;
}
if (ppShowPredicted == null) {
    ppShowPredicted = Boolean.FALSE;
}

if (ppSortAscending == null) {
    ppSortAscending = Boolean.TRUE;
}

if (navigationHelper.getSelectedSamples(sessionInfo).isEmpty()) {
    out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/info_32.gif\" /></div>"
            + "<p>Select at least one sample to view data.</p></div>");
} else if (navigationHelper.getSelectedStudents(sessionInfo).isEmpty()) {
    out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/info_32.gif\" /></div>"
            + "<p>Select at least one student to view data.</p></div>");
} else if (navigationHelper.getSelectedProblems(sessionInfo).isEmpty()) {
    out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/info_32.gif\" /></div>"
            + "<p>Select at least one problem to view data.</p></div>");
} else {
        List sampleList = new ArrayList();
        List skillList = new ArrayList();
        List problemList = new ArrayList();
        List studentList = new ArrayList();
        
        sampleList = navigationHelper.getSelectedSamples(sessionInfo);
        
        SkillModelItem primaryModel = navigationHelper.getSelectedSkillModelItem(sessionInfo);
        Long primaryModelId = navigationHelper.getSelectedSkillModel(sessionInfo);
        SkillModelItem secondaryModel = null;
        Long secondaryModelId = navigationHelper.getSecondarySelectedSkillModel(sessionInfo);
        if (primaryModel != null && !primaryModelId.equals(secondaryModelId)) {
            secondaryModel = navigationHelper.getSecondarySelectedSkillModelItem(sessionInfo);
        }

        out.print("<table><tr>");
        for (Iterator it = sampleList.iterator(); it.hasNext();) {
            SampleItem sample = (SampleItem)it.next();
            
            String sampleStr = sample.getSampleName();
            out.print("<td style=\"text-align:center\" >");
            out.print("<span class=\"ppSampleTitle\">Sample: " +  sampleStr + "</span>");
            
            ProfilerOptions options = new ProfilerOptions(
                sample,
                primaryModel,
                ppViewByCategory,
                ppViewByType,
                ppSortBy,
                ppSortAscending.booleanValue(),
                secondaryModel);
    
            skillList = navigationHelper.getSelectedSkills(sessionInfo);
            studentList = navigationHelper.getSelectedStudents(sessionInfo);
            problemList = navigationHelper.getSelectedProblems(sessionInfo);
        
            options.setStudentList(studentList);
            options.setSkillList(skillList);
            options.setProblemList(problemList);
            options.setViewPredicted(ppShowPredicted.booleanValue());
            options.setUpperLimit(ppTopLimit);
            options.setLowerLimit(ppBottomLimit);
            options.setDisplayUnmappedFlag(ppDisplayUnmapped.booleanValue());
            options.setNumberOfGraphs(sampleList.size());
            options.setMinProblems(sessionInfo.getPerformanceProfilerContext().getMinProblems());
            options.setMinStudents(sessionInfo.getPerformanceProfilerContext().getMinStudents());
            options.setMinSteps(sessionInfo.getPerformanceProfilerContext().getMinSteps());
            options.setMinSkills(sessionInfo.getPerformanceProfilerContext().getMinSkills());
    
            PerformanceProfilerProducer producer = new PerformanceProfilerProducer(options);
            String filename = producer.generateGraph(new PrintWriter(out), sessionInfo);
            String graphURL = request.getContextPath() + filename;
            out.print("<img src=\"" + graphURL
                + "\" border=0 usemap=\"#" + filename
                + "\" /><br /><br/>");
            out.print("</td>");
        }
        out.print("</tr></table>");    

            
        out.print("<p class=\"imageInfo\">Sorted by " + ppSortBy + " in ");
        if (ppSortAscending.booleanValue()) {
            out.print("Ascending order.");
        } else {
            out.print("Descending order");
        }
        out.print("<br />");
                out.print("Note: A maximum of 500 rows can be shown in one graph.</p>");

%>
        <table id="ppControls" class="ppControls">
        <tbody>
            <tr>
                <td><label>Show top</label></td>
                <td><input type="text"
            value="<% if (ppTopLimit != null) { out.print(ppTopLimit); } %>"
            id="ppTopLimit" size="3" /> <%=ppViewByCategory %>s</td>
            <td><input type="button" value="Clear" onclick="javascript:clearPpTopLimit()" /></td>
            </tr>
            <tr>
                <td><label>Show bottom</label></td>
                <td><input type="text" 
            value="<% if (ppBottomLimit != null) { out.print(ppBottomLimit); } %>"
            id="ppBottomLimit" size="3" />    <%=ppViewByCategory %>s</td>
            <td><input type="button" value="Clear" onclick="javascript:clearPpBottomLimit()" /></td>
            </tr>
            <tr><td class="refreshCell" colspan="2">
                <input type="button" class="native-button" value="Refresh Graph" onclick="javascript:updateContent()" /></td></tr>
          
        </tbody>
        </table>
<%    
}
%>
