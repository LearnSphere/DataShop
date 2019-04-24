<html>

<%@page import="edu.cmu.pl2.dao.AdvisorDao"%>
<%@page import="edu.cmu.pl2.dao.DaoFactory"%>
<%@page import="edu.cmu.pl2.item.AdvisorItem"%>
<%@page import="edu.cmu.pl2.servlet.DtoHelper"%>
<%@page import="edu.cmu.pl2.servlet.ResourceLibraryDto"%>
<%@page import="java.util.ArrayList"%>

<!-- header -->
<%
Long advisorId = new Long(request.getParameter("advisorId"));
Long studentId = new Long(request.getParameter("studentId"));
Long strategyOptionId = new Long(request.getParameter("strategyOptionId"));
Long planId = new Long(request.getParameter("planId"));
Long sessionId = new Long(request.getParameter("sessionId"));

if (request.getAttribute("planId") != null) {
    // A new plan was made for this resource that was just added.  Here's the new Id
    planId = (Long)request.getAttribute("planId");
}

String institutionPwd = (String)session.getAttribute("institutionPassword");

ResourceLibraryDto resourceLibraryDto = DtoHelper.getResourceLibraryDto(advisorId, studentId, strategyOptionId, planId, institutionPwd);

Map<Long, String> selectedSystemResources = resourceLibraryDto.getSelectedSystemResources();
Map<Long, String> selectedAdvisorResources = resourceLibraryDto.getSelectedAdvisorResources();


String advisorFirstName = resourceLibraryDto.getAdvisorFirstName();
%>
<%@ include file="../jsp_pl2/header.jspf"%>

<script>
jQuery('body').data("planId", <%=planId %>);
jQuery('body').data("advisorId", <%=advisorId %>);
jQuery('body').data("studentId", <%=studentId %>);
jQuery('body').data("strategyOptionId", <%=strategyOptionId %>);
jQuery('body').data("sessionId", <%=sessionId %>);
</script>

<body>
<div class="resourceLibrary">
    <div id="strategyOptionTitle">"<%=resourceLibraryDto.getStrategyOption() %>" Resource Library</div>
    <form id="backToStudentButton" class="ui-button" method="post" action="PL2">
        back to student
        <input type="hidden" name="requestingMethod" value="PL2Servlet.studentDashboard" />
        <input type="hidden" name="studentId" value="<%=studentId %>" />
        <input type="hidden" name="advisorId" value="<%=advisorId %>" />
    </form><br/>
    <%
    Map<Long, String> systemResources = resourceLibraryDto.getSystemResources();
    if (systemResources != null) {
        Iterator it = systemResources.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            String selectedClass = "";
            if (selectedSystemResources.containsKey(pair.getKey())) {
                selectedClass = " selectedResource ";
            }
            %><div resourceType="systemResource" class="greenText resource <%=selectedClass %>" value="<%=pair.getKey() %>">+ <%=pair.getValue() %></div> <br/> <%
        }
    }
    Map<Long, String> advisorResources = resourceLibraryDto.getAdvisorResources();
    if (advisorResources != null) {
        Iterator it = advisorResources.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            String selectedClass = "";
            if (selectedAdvisorResources.containsKey(pair.getKey())) {
                selectedClass = " selectedResource ";
            }
            %><div resourceType="advisorResource" class="greenText resource <%=selectedClass %>" value="<%=pair.getKey() %>">+ <%=pair.getValue() %></div> <br/> <%
        }
    }
    %>

    <div id="customResource" class="greenText resource" >+ Custom resource</div>
</div>
</body>

<script>
jQuery(document).ready(function() {
    // Grey out class button since we're on the individual page now
    jQuery('#classButton').css('opacity', '0.7');

    // Handler for back to student button
    jQuery('#backToStudentButton').click(function() {
        jQuery(this).submit();
    });

    // Handle adding a custom resource
    jQuery('#customResource').off('click');
    jQuery('#customResource').click(function() {
        //alert('Ability to add your own resources is coming soon.');
        createResourceDialog();
    });
});
</script>

</html>
