<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="edu.cmu.pslc.datashop.item.SampleItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SubgoalAttemptItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.SampleSelectorHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportBySkillList"%>
<%@page import="edu.cmu.pslc.datashop.dto.ErrorReportBySkill"%>

<%@page import="edu.cmu.pslc.datashop.item.SkillItem"%>

<%
    // BODY OF ERROR REPORT
    
    SampleSelectorHelper erSampleHelper = HelperFactory.DEFAULT.getSampleSelectorHelper();
    NavigationHelper erNavHelper = HelperFactory.DEFAULT.getNavigationHelper();
    DatasetContext sessionInfo  = (DatasetContext)session.getAttribute(
            "datasetContext_" + request.getParameter("datasetId"));

    ErrorReportBySkillList errorReportBySkillList = null;

    List skillList = erNavHelper.getSelectedSkills(sessionInfo);

    if (skillList.size() > 0) {
        ErrorReportHelper erHelper = HelperFactory.DEFAULT.getErrorReportHelper();
        errorReportBySkillList = erHelper.getErrorReportBySkillList(
                erNavHelper.getSelectedSamples(sessionInfo),
                erNavHelper.getSelectedSkillModel(sessionInfo),
                skillList);
    }

    ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
    boolean pcAvailableForDataset = pcHelper.isProblemContentAvailable(sessionInfo.getDataset());

    if (erNavHelper.getSelectedSamples(sessionInfo).isEmpty()) {
    	out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
                + "<img src=\"images/info_32.gif\" /></div>"
                + "<p>Select at least one sample to view an error report.</p></div>");
    } else if (errorReportBySkillList != null && erNavHelper.getSelectedSkills(sessionInfo) != null) {
    
%>
        <p name="problem" id="problemname">All Selected Knowledge Components</p>

<table name="errorReport" id="errorReport">
<tr><th>Knowledge Component</th><th>Aggregate Values</th></tr>
<%
Collection erSkillList = errorReportBySkillList.getSkillList();
int stepIdx = 0;
if (erSkillList != null) {
    String prevSkillName = "";
    int oddEven = 0;
    for (Iterator iter = erSkillList.iterator(); iter.hasNext();) {
        stepIdx++;
        ErrorReportBySkill erBySkill = (ErrorReportBySkill)iter.next();
        String skillName = erBySkill.getSkillName();
        
        if (!prevSkillName.equals(skillName)) {
            oddEven++;
            prevSkillName = skillName;
        }
        if (skillName != null) {
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
            <tr><td name="step<%=stepIdx%>" class="step" ><%=skillName%></td></tr>
            <tr><td name="total<%=stepIdx%>" class="total" title="Total number of observations for this knowledge component.">Number of Observations: <%=NumberFormat.getNumberInstance(Locale.US).format(erBySkill.getNumObsTotal())%></td></tr>
            <tr><td class="kcs">Problem(s):</td></tr>
<%
            String problemNameList = null;
            if (pcAvailableForDataset) {
                problemNameList = pcHelper.getProblemNameListForDisplay(erBySkill);
            } else {
                problemNameList = erBySkill.getProblemNameList();
            }
            if (problemNameList != null) {
                %>
                <tr><td name="skill<%=stepIdx%>_problem" class="skill"><%=problemNameList%></td></tr>
                <%
            } // end if on problem names not null         
%>
   
            <tr><td name="skill<%=stepIdx%>_sample" class="sample">Sample: <%=erBySkill.getSampleName()%></td></tr> 
            </table></td><!-- end stepTable -->

            <td class="attemptcell">
            <table id="attemptTable<%=stepIdx%>" name="attemptTable" class="attempts">
            <tr><th>Evaluation</th><th>Number of Observations</th></tr>
<%
%>

<%
    if (erBySkill.getNumObsCorrect() > 0) { 
%>
            <tr class="er-correct">
            <td name="attempt<%=stepIdx%>_correct_label"><%=SubgoalAttemptItem.CORRECT_FLAG_CORRECT%></td>
            <td name="attempt<%=stepIdx%>_correct_number"><%=erBySkill.getNumObsCorrect()%>&nbsp;(<%=erBySkill.getCorrectPercentage()%>)</td>
            </tr>
<%
    }
    if (erBySkill.getNumObsIncorrect() > 0) { 
%>
            <tr class="er-incorrect">
            <td name="attempt<%=stepIdx%>_incorrect_label"><%=SubgoalAttemptItem.CORRECT_FLAG_INCORRECT%></td>
            <td name="attempt<%=stepIdx%>_incorrect_number"><%=erBySkill.getNumObsIncorrect()%>&nbsp;(<%=erBySkill.getIncorrectPercentage()%>)</td>
            </tr>
<%
    }
    if (erBySkill.getNumObsHint() > 0) { 
%>
            <tr class="er-hint">
            <td name="attempt<%=stepIdx%>_hint_label"><%=SubgoalAttemptItem.CORRECT_FLAG_HINT%></td>
            <td name="attempt<%=stepIdx%>_hint_number"><%=erBySkill.getNumObsHint()%>&nbsp;(<%=erBySkill.getHintPercentage()%>)</td>
            </tr>
<%
    }
    if (erBySkill.getNumObsUnknown() > 0) { 
%>
            <tr class="er-unknown">
            <td name="attempt<%=stepIdx%>_unknown_label"><%=SubgoalAttemptItem.CORRECT_FLAG_UNKNOWN%></td>
            <td name="attempt<%=stepIdx%>_unknown_number"><%=erBySkill.getNumObsUnknown()%>&nbsp;(<%=erBySkill.getUnknownPercentage()%>)</td>
            </tr>
<%
    }
%>

<%

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
    } else if (erNavHelper.getSelectedSkills(sessionInfo) == null || erNavHelper.getSelectedSkills(sessionInfo).size() == 0) {
    	out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
                + "<img src=\"images/info_32.gif\" /></div>"
                + "<p>Select at least one knowledge component to view an error report.</p></div>");
    } else {
        out.print("<p class=\"error\">Navigation error</p>");
    }
%>
