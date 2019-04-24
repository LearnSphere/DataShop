<html>

<%@page import="edu.cmu.pl2.dao.AdvisorDao"%>
<%@page import="edu.cmu.pl2.dao.DaoFactory"%>
<%@page import="edu.cmu.pl2.item.AdvisorItem"%>
<%@page import="edu.cmu.pl2.servlet.DtoHelper"%>
<%@page import="edu.cmu.pl2.servlet.PL2Servlet"%>
<%@page import="edu.cmu.pl2.servlet.KickoffInterviewDto"%>
<%@page import="java.util.ArrayList"%>

<!-- header -->
<%
//Long advisorId = (Long)request.getAttribute("advisor_id");
//AdvisorItem advisor = (AdvisorItem)request.getAttribute("advisor");

Long studentId = new Long(request.getParameter("studentId"));
Long advisorId = new Long(request.getParameter("advisorId"));

String institutionPwd = (String)session.getAttribute("institutionPassword");

KickoffInterviewDto kickoffDto = DtoHelper.getKickoffInterviewDto(advisorId, studentId, institutionPwd);

String advisorFirstName = kickoffDto.getAdvisorFirstName();
String studentFirstName = kickoffDto.getStudentFirstName();
String studentLastName = kickoffDto.getStudentLastName();

%>
<%@ include file="../jsp_pl2/header.jspf"%>


<!--  Set some JS global vars -->
<script type="text/javascript">
jQuery('body').data("advisorId", <%=advisorId %>);
jQuery('body').data("studentId", <%=studentId %>);
</script>

<!-- Functions for creating the questions -->
<%@ include file="../jsp_pl2/interview_question_functions.jspf"%>

<!-- This will get the questions as a JavaScript object -->
<%@ include file="../jsp_pl2/kickoff_interview_questions.jspf"%>


<body>
<div class="kickoff_interview_title">Kickoff Conversation</div>

<div id="questionsContainer"></div>


<script>
var questionContainer = jQuery('#questionsContainer');



questions.forEach(function(question) {
    questionToHtml(questionContainer, question);
});

jQuery('.openEnded').change(toggleOpenEndedInput);

</script>


<script>
var questionContainer = jQuery('#questionsContainer');
var stuFirstName = "<%=studentFirstName%>";

let goalSettingDiv = updateGoalHtml(stuFirstName);


questionContainer.append(goalSettingDiv);

// Instantiate date picker
jQuery('#goal_completion_date').datepicker(
        { defaultDate: 60 }); // TODO: set options for the picker
jQuery('#goal_completion_date').datepicker('setDate', 60);

// Display the custom objective stuff when the user wants it
jQuery('input[name="acceptRec"]').change(function() {
    let val = jQuery(this).val();
    if (val === 'accept') {
        jQuery('#customObjective').hide();
    } else {
        jQuery('#customObjective').show();
    }
});






// Add some buttons for submitting
questionContainer.append(jQuery('<button />')
        .text('submit')
        .attr('id', 'submitInterviewQuestions')
        .attr('class', 'ui-button'));
//questionContainer.append(jQuery('<button />').text('cancel')
//        .attr('class', 'ui-button'));

// Submit the interview question answers as a big form
jQuery('#submitInterviewQuestions').click(submitInterviewQuestions);


// When clicking a checkbox, make it look selected
jQuery('.checkboxContainer input[type="checkbox"]').change(function() {
    let el = jQuery(this);
    if (el.is(':checked')) {
        jQuery("label[for='" + el.attr('id') + "']").addClass('selectedResource');
    } else {
        jQuery("label[for='" + el.attr('id') + "']").removeClass('selectedResource');
    }
});

</script>
</body>

</html>
