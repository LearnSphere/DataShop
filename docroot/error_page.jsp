<%@page isErrorPage="true" %>
<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2008
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 9456 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2013-06-20 13:34:51 -0400 (Thu, 20 Jun 2013) $
// $KeyWordsOff: $
%>

<%@ include file="/header_variables.jspf" %>

<% pageTitle = "Oops"; %>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- body -->

<tr><td>
    <table id="oops">
    <%
    String oopsStr = "Oops!";
    String msg = (String) request.getAttribute("MSG");
    if ( msg == null || msg.isEmpty()) {
        msg = "You found a bug. We're terribly sorry about that.";
    }
    // Don't include 'Oops!' if error is dataset not found...
    if (msg.startsWith("Dataset not found")) {
       oopsStr = "";
    }
    %>
    <th><%=oopsStr %>
    <p><%=msg %><br />
    If you'd like to report the problem, contact us by
    <a href="help?page=contact">sending us an email</a>.
    </p>
    <p>&mdash; The DataShop Team</p>
    <p><a href="index.jsp">Home</a></p>
    </th>
    </table>
</td></tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<%
if (exception == null) {
%>
    <!--  No exception here. -->
<%
} else {
%>
    <!--
    The exception that occurred is:
    <%= exception.getClass().getName() %>
    The message is:
    <%= exception.getMessage() %>
     -->
<%
}
%>

</body>
</html>
