<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentServlet"%>

<%
    // Problem Content
    
    ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();

    Long pcProblemId = (Long)session.getAttribute(ProblemContentServlet.PC_PROBLEM_ID);
    String baseDir = (String)session.getAttribute(ProblemContentServlet.PC_BASE_DIR);
    String html = pcHelper.getHtml(pcProblemId, baseDir);
%>
    <%=html %>
