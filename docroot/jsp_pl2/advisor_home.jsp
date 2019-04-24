<html>

<%@page import="edu.cmu.pl2.dao.AdvisorDao"%>
<%@page import="edu.cmu.pl2.dao.DaoFactory"%>
<%@page import="edu.cmu.pl2.item.AdvisorItem"%>
<%@page import="edu.cmu.pl2.servlet.PL2Servlet"%>
<%@page import="edu.cmu.pl2.servlet.DtoHelper"%>
<%@page import="edu.cmu.pl2.servlet.AdvisorHomeDto"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.text.SimpleDateFormat"%>

<!-- header -->
<%
Date now = PL2Servlet.getCurrentDateInDemo();

String pattern = "EEEEE dd MMMMM yyyy HH:mm:ss";
SimpleDateFormat simpleDateFormat =
        new SimpleDateFormat(pattern);

String nowStr = simpleDateFormat.format(now);

Long advisorId = (Long)request.getAttribute("advisor_id");
AdvisorItem advisor = (AdvisorItem)request.getAttribute("advisor");
if (advisor == null && advisorId != null) {
    AdvisorDao advisorDao = DaoFactory.DEFAULT.getAdvisorDao();
    advisor = advisorDao.get(advisorId);
}

String schoolPwd = (String)session.getAttribute("institutionPassword");
AdvisorHomeDto advisorHomeDto = DtoHelper.getAdvisorHomePageInfo(advisor, schoolPwd);

List<Long> advisorsStudentsIds = advisorHomeDto.getAdvisorsStudents();
Map<Long, String> studentFirstNameMap = advisorHomeDto.getStudentFirstNameMap();
Map<Long, String> studentLastNameMap = advisorHomeDto.getStudentLastNameMap();
Map<Long, String> advisorsStudentsStatuses = advisorHomeDto.getStudentStatusMap();
Map<Long, Boolean> studentHasPlanMap = advisorHomeDto.getStudentHasPlanMap();

ArrayList<Long> studentsWhoDontNeedAttention = new ArrayList<Long>();

String advisorFirstName = advisorHomeDto.getAdvisorFirstName();
%>

<%@ include file="../jsp_pl2/header.jspf"%>

<body>

<div class="demoDate">Today's date in this demo is: <%=nowStr %></div>

<form class="refreshDb" method="post" action="PL2" >
    <input type="hidden" name="requestingMethod" value="PL2Servlet.restoreDummyData" />
    <button type="submit"> Reset Demo</button>
</form>

<form class="nextWeekForm" method="post" action="PL2" >
    <input type="hidden" name="requestingMethod" value="PL2Servlet.nextWeekInDemo" />
    <button type="submit">Next Week</button>
</form>

<div id="studentViewBig">
    <!-- Add students who need attention (Missed you || Ramp it up) -->
    <div id="studentsNeedAttention">
        <%
        for (Long stuId : advisorsStudentsIds) {
            String firstName = studentFirstNameMap.get(stuId);
            String lastName = studentLastNameMap.get(stuId);
            String status = advisorsStudentsStatuses.get(stuId);
            Boolean hasPlan = studentHasPlanMap.get(stuId);
            String planClass = "";
            if (hasPlan) {
                planClass = " hasPlan ";
            }
            if (status != null && !PL2Servlet.NO_ATTENTION_NEEDED_STATUSES.contains(status)) {
                %>
                    <form class="studentBox" value="<%=stuId%>" method="post" action="PL2" title="View <%=firstName%>'s Information">
                        <input type="hidden" name="requestingMethod" value="PL2Servlet.studentDashboard" />
                        <input type="hidden" name="studentId" value="<%= stuId %>" />
                        <input type="hidden" name="advisorId" value="<%= advisorId %>" />
                        <div class="studentImage  <%=planClass%>">
                            <img src="images/pl2/student_profile_needs_attention.svg">
                            <%
                            if (hasPlan) {
                                %><div class="check"><img src="images/pl2/checkmark.svg"></div><%
                            }
                            %>
                        </div>
                        <div class="studentInfo">
                            <div class="studentName"><%=firstName %> <%=lastName %></div>
                            <div class="studentStatus"><%=status %></div>
                        </div>
                    </form>
                <%
            } else {
                studentsWhoDontNeedAttention.add(stuId);
            }
        }
        %>

    </div>

    <div class="horizontalDivider"></div>

    <div id="studentsWhoNeedAttention">
        <%
        for (Long stuId : studentsWhoDontNeedAttention) {
            String firstName = studentFirstNameMap.get(stuId);
            String lastName = studentLastNameMap.get(stuId);
            String status = advisorsStudentsStatuses.get(stuId);
            Boolean hasPlan = studentHasPlanMap.get(stuId);
            String planClass = "";
            if (hasPlan) {
                planClass = " hasPlan ";
            }
            %>
            <form class="studentBox" value="<%=stuId%>" method="post" action="PL2" title="View <%=firstName%>'s Information">
                <input type="hidden" name="requestingMethod" value="PL2Servlet.studentDashboard" />
                <input type="hidden" name="studentId" value="<%=stuId %>" />
                <input type="hidden" name="advisorId" value="<%= advisorId %>" />
                <div class="studentImage <%=planClass%>">
                    <img src="images/pl2/student_profile_no_attention.svg">
                    <%if (hasPlan) {
                        %><div class="check"><img src="images/pl2/checkmark.svg"></div> <%
                    }
                    %>
                </div>
                <div class="studentInfo">
                    <div class="studentName"><%=firstName %> <%=lastName %></div>
                    <div class="studentStatus"><%=status %></div>
                </div>
            </form>
        <%
        }
        %>
    </div>
</div>

<script>

jQuery(document).ready(function() {
    // Add Event handler for clicking on a student to go to their dashboard
    jQuery('.studentBox').click(function() {
        jQuery(this).submit();
    });

    // Grey out the individual button since we're on the group page
    jQuery('#individualButton').css('opacity', '0.7');

    // Ensure that the hash secret is set
    //ensureHashSecretIsSet();
});
</script>

</body>

</html>
