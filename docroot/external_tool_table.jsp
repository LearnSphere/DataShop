<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 9846 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2013-08-23 14:07:48 -0400 (Fri, 23 Aug 2013) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolTableDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolDto"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "External Tools";
    cssIncludes.add("ExternalTools.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ExternalTools.js");
    jsIncludes.add("javascript/object/Truncator.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>


<!-- code -->
<%!
    // TD element holds the displayed text. So we convert HTML characters to entities,
    // and add line breaks (br) elements.
    String cleanInputText(String str) {
        String textCleaner = str.replaceAll("&","&amp;").
            replaceAll(">","&gt;").replaceAll("<","&lt;");
        String textWithBrs = textCleaner.replaceAll("\n","<br />").replaceAll("\"","&quot;");
        return textWithBrs;
    }
%>
<%
    ExternalToolTableDto tableDto =
            (ExternalToolTableDto)request.getAttribute(ExternalToolTableDto.ATTRIB_NAME);
    List<ExternalToolDto> toolList = tableDto.getToolList();
    String message = tableDto.getMessage();
    String messageLevel = tableDto.getMessageLevel();

    ExternalToolsContext toolContext = ExternalToolsContext.getContext(request);
    String sortByColumn = toolContext.getToolSortByColumn();
    Boolean isAscending = toolContext.isToolAscending(sortByColumn);
    
    boolean isLoggedIn = remoteUser != null ? true : false;
    boolean hasRequestedRole = tableDto.hasRequestedExtToolsRoleFlag();
    boolean hasRole = tableDto.hasExtToolsRole();
    boolean hasAdminAuth = tableDto.hasAdminAuth();

    String td_class_name_odd = "cell";
    String td_class_name_even = "cell even";
    String td_class_name = td_class_name_odd;
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

    <!-- make note of attrs needed by javascript -->
    <input type="hidden" id="extToolMessage" value="<%=message %>" />
    <input type="hidden" id="extToolMessageLevel" value="<%=messageLevel %>" />
        
    <div id="external-tools-div">
    <h1>External Tools</h1>
  
    <p id="freeToolsTxt">
    Free tools submitted by developers in the educational data mining and intelligent 
    tutoring systems communities.
    Please be aware that these files have been provided by users of the site; 
    we cannot vouch for their accuracy or authenticity. 
    <% if (!isLoggedIn) { %>
    To add your own tools, please log in.
    <% } %>
    </p>    
    <% if (isLoggedIn && (hasRole || hasAdminAuth)) { %>
        <div id="addToolButton">
            <p>
            <a href="#" id="addToolLink" 
               class="ui-state-default ui-corner-all" 
               onclick="javascript:openAddToolDialog()">
            <span class=""></span>Add Tool</a>
            </p>
        </div>
    <% } else if (isLoggedIn && !hasRole && hasRequestedRole) { %>
        
    <% } else if (isLoggedIn && !hasRole) { %>
        <div id="addToolButton">
            <p>
            <a href="#" id="addToolLink" 
               class="ui-state-default ui-corner-all" 
               onclick="javascript:openRequestExtToolRoleDialog()">
            <span class=""></span>Add Tool</a>
            </p>
        </div>
    <% } %>
    
    <table id="externalToolsTable" class="">
    <colgroup>
        <col style="width:40%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
        <col style="width:15%" />
    </colgroup>
    <tr>
        <th>
            <a href="javascript:sortTools('<%=ExternalToolDto.COLUMN_NAME%>')">
            <%=ExternalToolDto.COLUMN_NAME %></a>
            <img src="<%=ExternalToolDto.getSortImage(ExternalToolDto.COLUMN_NAME, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortTools('<%=ExternalToolDto.COLUMN_LANGUAGE%>')">
            <%=ExternalToolDto.COLUMN_LANGUAGE %></a>
            <img src="<%=ExternalToolDto.getSortImage(ExternalToolDto.COLUMN_LANGUAGE, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortTools('<%=ExternalToolDto.COLUMN_CONTRIBUTOR%>')">
            <%=ExternalToolDto.COLUMN_CONTRIBUTOR %></a>
            <img src="<%=ExternalToolDto.getSortImage(ExternalToolDto.COLUMN_CONTRIBUTOR, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortTools('<%=ExternalToolDto.COLUMN_DOWNLOADS%>')">
            <%=ExternalToolDto.COLUMN_DOWNLOADS %></a>
            <img src="<%=ExternalToolDto.getSortImage(ExternalToolDto.COLUMN_DOWNLOADS, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortTools('<%=ExternalToolDto.COLUMN_UPDATED%>')">
            <%=ExternalToolDto.COLUMN_UPDATED %></a>
            <img src="<%=ExternalToolDto.getSortImage(ExternalToolDto.COLUMN_UPDATED, sortByColumn, isAscending) %>" />
        </th>
    </tr>

    <%
    int idx = 1;
    if (toolList != null && toolList.size() > 0) {
        for (Iterator iter = toolList.iterator(); iter.hasNext();) {
            ExternalToolDto toolDto = (ExternalToolDto)iter.next();
            String fullDescription = toolDto.getDescription();
            if (idx % 2 == 0) {
                td_class_name = td_class_name_even;
            } else {
                td_class_name = td_class_name_odd;
            }
            %>
            <tr>
            <td class="<%=td_class_name%>" id="fullName_<%=toolDto.getId()%>">
            
                  <a href="ExternalTools?toolId=<%=toolDto.getId()%>">
                  <span name="tool_name" class="bTitle"
                      id="name_span_<%=toolDto.getId()%>"><%=toolDto.getName()%></span>
                  </a>

                  <span name="tool_desc" id="desc_span_<%=toolDto.getId()%>"
                        class="externalToolsDescSpan"><%=fullDescription%></span>

            </td>
            <td class="<%=td_class_name%>"><%= toolDto.getLanguage() %></td>
            <td class="<%=td_class_name%>"><%= toolDto.getContributor() %></td>
            <td class="<%=td_class_name%>"><%= toolDto.getDownloadsString() %></td>
            <td class="<%=td_class_name%>">
                 <span class="date"><%= toolDto.getUpdatedTimeString() %></span>
            </td>
            </tr>
            <%
            idx++;
        } // end for loop
            
    } else {
        %>
            <tr>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>" style="font-style:oblique">No tools found.</td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>"></td>
            </tr>
        <%
    }
    %>
    </table>

</div> <!-- End #external-tools div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Un-populated div needed for modal windows using jQuery -->
<div id="addToolDialog" class="addToolDialog"></div>
<div id="requestRoleDialog" class="addToolDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/external-tool-table.jspf" %>
</div>

<script type="text/javascript">
    onloadObserver.addListener(initTruncatorFields);
</script>


</body>
</html>
