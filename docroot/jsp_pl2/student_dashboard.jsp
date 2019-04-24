<html>

<%@page import="edu.cmu.pl2.dao.AdvisorDao"%>
<%@page import="edu.cmu.pl2.dao.DaoFactory"%>
<%@page import="edu.cmu.pl2.item.AdvisorItem"%>
<%@page import="edu.cmu.pl2.servlet.DtoHelper"%>
<%@page import="edu.cmu.pl2.servlet.PL2Servlet"%>
<%@page import="edu.cmu.pl2.servlet.StudentDashboardDto"%>
<%@page import="edu.cmu.pl2.servlet.SessionInfoDto"%>
<%@page import="java.util.ArrayList"%>

<!-- header -->
<%
Long advisorId = (Long)request.getAttribute("advisor_id");
AdvisorItem advisor = (AdvisorItem)request.getAttribute("advisor");

Long studentId = new Long(request.getParameter("studentId"));

String institutionPwd = (String)session.getAttribute("institutionPassword");

StudentDashboardDto studentDashDto = DtoHelper.getStudentDashboardDto(advisorId, studentId, institutionPwd);

String studentFirstName = studentDashDto.getStudentFirstName();
String studentLastName = studentDashDto.getStudentLastName();
String studentStatus = studentDashDto.getStudentStatus();

String semesterGoal = studentDashDto.getSemesterGoal();

String weeklyObjective = studentDashDto.getWeeklyObjective();

List<String> background = studentDashDto.getBackground();

List<SessionInfoDto> sessInfoDtos = studentDashDto.getSessionInfoDtos();

Long mostRecentPlanId = null;
if (sessInfoDtos.size() > 0) {
    mostRecentPlanId = new Long(sessInfoDtos.get(0).getPlanId());
}


// Determine whether to put the "start new session" button or end session button
String START_SESSION_BUTTON = "<button id=\"create_new_session\" class=\"ui-button sessionStartEndButton\" onclick=\"javascript:newSessionDialog();\">Start new session</button>";
String END_SESSION_BUTTON = "<button id=\"end_session\" class=\"ui-button sessionStartEndButton\" onclick=\"javascript:endSessionInterviewDialog();\">End Current Session</button>";

String startEndSessionButton = START_SESSION_BUTTON;
HttpSession httpSession = request.getSession();
if (httpSession != null) {
    HashMap<Long, Boolean> activeSessions = (HashMap<Long, Boolean>)httpSession.getAttribute("active_student_sessions");
    if (activeSessions != null && activeSessions.containsKey(studentId)) {
        if (activeSessions.get(studentId) == true) {
            // The session is active, allow the user to end it
            startEndSessionButton = END_SESSION_BUTTON;
        }
    }
}


String advisorFirstName = studentDashDto.getAdvisorFirstName();
%>
<%@ include file="../jsp_pl2/header.jspf"%>


<!--  Set some JS global vars -->
<script type="text/javascript">
jQuery('body').data("advisorId", <%=advisorId %>);
jQuery('body').data("studentId", <%=studentId %>);
jQuery('body').data("planId", <%=mostRecentPlanId %>);
jQuery('body').data("studentFirstName", "<%=studentFirstName %>");
</script>


<body>
<div id="studentInfo">
    <div id="studentMainInfo">
        <div class="studentImage">
            <img src="images/pl2/student_profile_needs_attention.svg">
        </div>
        <div id="studentMainTextInfo">
            <div id="studentNameAndStatus">
                <div id="studentNameDashboard"> <%=studentFirstName %> <%=studentLastName %></div>
                <div id="nameStatusDivider">|</div>
                <div id="studentStatusDashboard" class="greenText"><%=studentStatus %>!</div>
            </div>
            <div id="goalAndObjective">
                <span class="lightBlueText">Semester Goal: </span>
                    <%=semesterGoal %><br/>
                <span class="lightBlueText"><%=studentFirstName %>'s Weekly Objective: </span>
                    <%=weeklyObjective %>
                <div class="updateGoal greenText" >[update]</div>
            </div>
            <div id="backgroundInfo">
                <span class="lightBlueText">Background Info: </span>
                <%=PL2Servlet.listToCommaSeparatedString(background) %>
                <div class="addBackground greenText" >[add]</div>
            </div>
        </div>
        <%=startEndSessionButton %>
    </div>


    <div id="allSessions">
        <%
        int sessCount = 0;
        int numSess = sessInfoDtos.size();
        for (SessionInfoDto sessInfoDto : sessInfoDtos) {
            %> <%@ include file="../jsp_pl2/session.jspf"%>  <%
            sessCount++;
        }%>
    </div>

</div>

<div id="start_session_questions" style="display:none"></div>
<div id="end_session_questions" style="display:none"></div>

<%@ include file="../jsp_pl2/interview_question_functions.jspf"%>
<%@ include file="../jsp_pl2/start_session_questions.jspf"%>
<%@ include file="../jsp_pl2/end_session_questions.jspf"%>

<script>
jQuery(document).ready(function() {
    var questionContainer = jQuery('#start_session_questions');

    start_questions.forEach(function(question) {
        questionToHtml(questionContainer, question);
    });

    questionContainer = jQuery('#end_session_questions');

    end_questions.forEach(function(question) {
        questionToHtml(questionContainer, question);
    });
});

</script>

</body>

<script>
jQuery(document).ready(function() {
    // Grey out class button since we're on the individual page now
    jQuery('#classButton').css('opacity', '0.7');

    // Add event handler to edit buttons
    jQuery('.editObjective').click(editObjective);
    jQuery('.addReflection').click(addReflectionDialog);
    jQuery('.addBackground').click(addBackgroundDialog);
    jQuery('.updateGoal').click(updateGoalDialog);

    // Handler for adding a strategy option
    jQuery('.strategyOptionAdd').click(function() {
        jQuery(this).submit();
    });

    // Handler for adding a custom strategy option
    jQuery('#customStrategy').click(function() {
        alert('Ability to add a new Strategy Option is coming soon.');
    });
});

/**
 * Edit the objective
 */
function editObjective() {
    //alert('Coming soon.');
    editObjectiveDialog();
}

</script>

</html>
