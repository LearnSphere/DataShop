<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.FeedbackItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem" %>
<%@page import="edu.cmu.pslc.datashop.item.SubgoalAttemptItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportByProblem"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportStep"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportStepAttempt"%>

<%
    // BODY OF ERROR REPORT
    
    SampleSelectorHelper erSampleHelper = HelperFactory.DEFAULT.getSampleSelectorHelper();
    NavigationHelper erNavHelper = HelperFactory.DEFAULT.getNavigationHelper();
    DatasetContext sessionInfo  = (DatasetContext)session.getAttribute(
            "datasetContext_" + request.getParameter("datasetId"));

    ErrorReportByProblem errorReport = null;

    boolean pcAvailableForDataset = false;
    String titleStr = "";
    String classStr = "ui-state-default ui-corner-all request_link";
    boolean buttonEnabled = true;

    if (erNavHelper.getSelectedProblem(sessionInfo) != null) {
        ErrorReportHelper erHelper = HelperFactory.DEFAULT.getErrorReportHelper();
        errorReport = erHelper.getErrorReportByProblem(
                erNavHelper.getSelectedSamples(sessionInfo),
                erNavHelper.getSelectedProblem(sessionInfo),
                erNavHelper.getSelectedSkillModel(sessionInfo));

        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        pcAvailableForDataset = pcHelper.isProblemContentAvailable(sessionInfo.getDataset());

        boolean pcAvailableForProblem =
                pcHelper.isProblemContentAvailable(errorReport.getProblemId());
        if (!pcAvailableForProblem) {
            titleStr = "title=\"Problem Content is not available for this problem.\"";
            classStr = "ui-state-default ui-corner-all dead_link ui-state-disabled";
            buttonEnabled = false;
        }
    }
%>
<%
    if (erNavHelper.getSelectedSamples(sessionInfo).isEmpty()) {
    	out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
                + "<img src=\"images/info_32.gif\" /></div>"
                + "<p>Select at least one sample to view an error report.</p></div>");
    } else if (errorReport != null && erNavHelper.getSelectedProblem(sessionInfo) != null) { %>
        <p name="problem" id="problemname">Problem: <%=errorReport.getProblemName()%>
        <% if (pcAvailableForDataset) { %>
            <span id="error-report-view-problem-button">
            <% if (buttonEnabled) { %>
                 <a id="errorReportViewProblemLink"
                    href="javascript:viewProblem(<%=errorReport.getProblemId() %>)"
                    class="<%=classStr %>">
                    <span class=""></span>View Problem
                 </a>
            <% } else { %>
                 <span id="errorReportViewProblemLink"
                    class="<%=classStr %>"
                    <%=titleStr %>>View Problem
                 </span>
            <% } %>
            </span>
        <% } %>
        </p>
<%
if (errorReport.getProblemDesc() != null) {
%>
<p id="problemdescription">Description: <%=errorReport.getProblemDesc()%></p>
<%
}

Collection stepList = errorReport.getStepList();
int numSteps = 0;
if (stepList != null) {
    numSteps = stepList.size();
}
%>
<p id="numberofsteps">Number of Steps: <%=numSteps%></p>
<table name="errorReport" id="errorReport">
<tr><th>Step</th><th>Attempts</th></tr>
<%
int stepIdx = 0;
if (stepList != null) {
    String prevStepName = "";
    int oddEven = 0;
    for (Iterator iter = stepList.iterator(); iter.hasNext();) {
        stepIdx++;
        ErrorReportStep step = (ErrorReportStep)iter.next();
        if (!prevStepName.equals(step.getStepName())) {
            oddEven++;
            prevStepName = step.getStepName();
        }
        if (step.getStepName() != null) {
            if (oddEven%2!=0) {
%>
                <tr class="odd">
<%
            } else {    
%>
                <tr>
<%
            } 
%>
            <td class="stepcell">
            <table id="stepTable<%=stepIdx%>"name="stepTable" class="stepTable">
            <tr><td name="step<%=stepIdx%>" class="step" title="<%=step.getHoverSAI()%>"><%=step.getDisplaySAI()%></td></tr>
            <tr><td name="total<%=stepIdx%>" class="total" title="Total number of students who attempted this step.">Number of Students: <%=step.getNumStudents()%></td></tr>
            <tr><td name="total<%=stepIdx%>" class="total" title="Total number of observations for this step.">Number of Observations: <%=step.getNumObservations()%></td></tr>
            <tr><td title="Another name for skills." class="kcs">Knowledge Component(s):</td></tr>
<%
            if (step.getKcList() != null) {
%>
                <tr><td name="step<%=stepIdx%>_skill" class="skill"><%=step.getKcList()%></td></tr>
<%
            } // end if on skill names         
%>
   
            <tr><td name="step<%=stepIdx%>_sample" class="sample">Sample: <%=step.getSampleName()%></td></tr> 
            </table></td><!-- end stepTable -->

            <td class="attemptcell">
            <table id="attemptTable<%=stepIdx%>" name="attemptTable" class="attempts">
            <tr><th>Evaluation</th><th>Number of Observations</th><th>Answer</th><th>Feedback/Classification</th></tr>
<%
            int attemptIdx = 0;

            if (step.getSortedAttemptList() != null) {
                String cssClass = "er-hint";
                for (Iterator attemptIter = step.getSortedAttemptList().iterator(); attemptIter.hasNext();) {
                    attemptIdx++;
                    ErrorReportStepAttempt attempt = (ErrorReportStepAttempt)attemptIter.next();
                    if (attempt.getCorrectFlag().equals(SubgoalAttemptItem.CORRECT_FLAG_CORRECT)) {
                         cssClass = "er-correct";
                    } else if (attempt.getCorrectFlag().equals(SubgoalAttemptItem.CORRECT_FLAG_INCORRECT)) {
                         cssClass = "er-incorrect";
                    } else {
                         cssClass = "er-hint";
                    }
%>
                    <tr class="<%=cssClass%>">
                    <td name="attempt<%=attemptIdx%>_correct"><%=attempt.getCorrectFlag()%></td>
                    <!-- 
                    <td name="attempt<%=attemptIdx%>_number"><%=attempt.getNumStudents()%>&nbsp;(<%=attempt.getPercentage()%>)</td>
                     -->
                    <td name="attempt<%=attemptIdx%>_number"><%=attempt.getNumObservations()%>&nbsp;(<%=attempt.getPercentage()%>)</td>
                    <td name="attempt<%=attemptIdx%>_answer" title="<%=attempt.getHoverSAI()%>"><%=attempt.getDisplaySAI()%></td>
                    <td><table class="er-feedback">
                    <%
                    int feedIdx = 1;
                    for (Iterator feedIter = attempt.getFeedbackItemList().iterator(); feedIter.hasNext();) {
                        FeedbackItem feedbackItem = (FeedbackItem)feedIter.next();
                    %>
                        <tr>
                        <td name="attempt<%=attemptIdx%>_<%=feedIdx%>_feedback"><%=feedbackItem.getFeedbackText()%> </td>
                        <%
                        if (feedbackItem.getClassification() != null) {
                        %>
                        <td name="attempt<%=attemptIdx%>_<%=feedIdx%>_classification"><%=feedbackItem.getClassification()%></td>
                        <%
                        }
                        %>
                        </tr>
                    <%
                        feedIdx++;
                    }
                    %>
                    </table></td>
                    </tr>
<%
                } // end loop on incorrect attempt list
            } // end if on incorrect attempt list is not null 

        } // end if on step name not null
%>
        </table></td></tr><!-- end atttemptTable -->
<%
    } // end for loop on steps
} else {
%>
<tr><td>no steps</td><td></td></tr>
<%
}
%>
</table><!-- end errorReport table -->
<%
    } else if (erNavHelper.getSelectedProblem(sessionInfo) == null) {
        out.print("<p>No problem selected</p>");
    } else {
        out.print("<p class=\"error\">Navigation error</p>");
    }
%>
