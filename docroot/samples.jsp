<%@ include file="/doctype.jspf"%>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Mike Komisin
// Version: $Revision: 11128 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2014-06-06 09:24:47 -0400 (Fri, 06 Jun 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="org.apache.commons.collections.comparators.NullComparator"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SampleHistoryDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SampleToDatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.AbstractServlet"%>



<!-- header -->
<%@ include file="/header_variables.jspf"%>
<%
    pageTitle = "Samples";

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/sampletodataset/Samples.js");
    jsIncludes.add("UploadDataset.js");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    // samples.css included via dataset_info.jsp
%>


<!-- code -->





<%
DatasetContext httpSessionInfo = (DatasetContext)session.getAttribute("datasetContext_"
        + request.getParameter("datasetId"));
SampleToDatasetContext sampleToDatasetContext = httpSessionInfo.getSampleToDatasetContext();


List<SampleRowDto> sampleRows = (List<SampleRowDto>) request.getSession().getAttribute("sampleRows");
//Sort the sample page rows.
Comparator<SampleRowDto> comparator = SampleRowDto.getComparator(
        SamplesServlet.selectSortParameters(sampleToDatasetContext));
NullComparator nullComparator = new NullComparator(comparator, false);
Collections.sort(sampleRows, nullComparator);

String sortBy = (String) sampleToDatasetContext.getSortBy();
Integer datasetId = (Integer) request.getSession().getAttribute("datasetId");

%>


<div id="samples-page">
    <!-- Begin #samples_page div -->
    <h2>Samples</h2>
    <form id="samples_page_form">
        <table id="samples-page-table">
            <%
    if (sampleRows != null && !sampleRows.isEmpty()) {
      %>

            <tr>
                <th class="arFirstColumn"></th>
                <th class="arSecondColumn"></th>
                <th><a href="#" id="<%=SampleRowDto.COLUMN_SAMPLE_NAME %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_SAMPLE_NAME %></a> <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_SAMPLE_NAME,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_SAMPLE_NAME))
        %>" /></th>
                <th><a href="#" id="<%=SampleRowDto.COLUMN_OWNER_ID %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_OWNER_ID %></a> <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_OWNER_ID,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_OWNER_ID))
        %>" /></th>
                <th><a href="#" id="<%=SampleRowDto.COLUMN_NUM_TRANSACTIONS %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_NUM_TRANSACTIONS %></a>
                    <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_NUM_TRANSACTIONS,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_NUM_TRANSACTIONS))
        %>" /></th>

                <th><a href="#" id="<%=SampleRowDto.COLUMN_NUM_STUDENTS %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_NUM_STUDENTS %></a> <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_NUM_STUDENTS,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_NUM_STUDENTS))
        %>" /></th>

                <th><a href="#" id="<%=SampleRowDto.COLUMN_NUM_PROBLEMS %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_NUM_PROBLEMS %></a> <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_NUM_PROBLEMS,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_NUM_PROBLEMS))
        %>" /></th>

                <th><a href="#" id="<%=SampleRowDto.COLUMN_NUM_STEPS %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_NUM_STEPS %></a> <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_NUM_STEPS,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_NUM_STEPS))
        %>" /></th>

                <th><a href="#" id="<%=SampleRowDto.COLUMN_NUM_UNIQUE_STEPS %>"
                    class="sortByColumn"><%=SampleRowDto.COLUMN_NUM_UNIQUE_STEPS %></a>
                    <img
                    src="<%=
            SampleRowDto.showSortOrder(sortBy,
                SampleRowDto.COLUMN_NUM_UNIQUE_STEPS,
                sampleToDatasetContext.getSortOrder(SampleRowDto.COLUMN_NUM_UNIQUE_STEPS))
        %>" /></th>


                <th class="actionableIconsColumn"></th>
            </tr>

            <%

        int rowCount = 0;
        for (SampleRowDto sampleRow : sampleRows) {    // begin sampleRow table
            // Even rows are gray
            String evenOrOdd = rowCount % 2 == 0 ? "even" : "";

            Boolean hasSampleHistory = sampleRow.hasSampleHistory();
            Integer sampleId = sampleRow.getSampleId();
            StringBuffer description = new StringBuffer(sampleRow.getSampleName());
            if (sampleRow.getDescription() != null) {
                description.append(" : " + sampleRow.getDescription());
            }
            %>
            <tr class="<%=evenOrOdd %>" id="samples_<%=sampleId %>"
                name="samples_<%=sampleId %>">

                <td class="arFirstColumn">
                    <% if (hasSampleHistory) { %>
                    <p>
                        <img id="sample_history_<%=sampleId %>" class="s2dCollapsed"
                            name="sample_history_<%=sampleId %>" src="images/expand.png"
                            title="Show/hide sample history">
                        <table><tr id="historyList_<%=sampleId %>"
                            name="historyList_<%=sampleId %>" class="historyList">
                        </tr></table>
                    </p> <% } else { %>
                    <p></p> <%
                 }
              %>
                </td>

                <!-- Is sample shared -->
                <td class="arSecondColumn">
                <p>
                    <% if (sampleRow.getIsGlobal() == 1) { %>

                        <img id="sample_public_<%=sampleId %>"
                            name="sample_public_<%=sampleId %>" title="Shared sample"
                                alt="Shared sample" src="images/users.gif">
                         <%
                    }

                %>
                </p>
                </td>

                <!-- Sample metric columns -->
                <td title="<%=description.toString() %>">
                    <input type="hidden" id="sampleName_<%=sampleRow.getSampleId() %>"
                         value="<%=sampleRow.getSampleName() %>" />
                    <%=sampleRow.getSampleName() %> <%=sampleRow.getPencilIcon() == null
                        ? "" : sampleRow.getPencilIcon() %></td>

                <% String ownerString =
                    sampleRow.getOwnerId().equals("%") ? "-" : sampleRow.getOwnerId();
                %>
                <td>
                    <% if (sampleRow.getOwnerEmail() != null) { %>
                    <a href="mailto:<%= sampleRow.getOwnerEmail() %>"><%=ownerString %>
                    </a>
                    <% } else { %>
                    <%=ownerString %>
                    <% } %>
                </td>
                <%
                Long numSteps = sampleRow.getNumSteps();
                Long numUniqueSteps = sampleRow.getNumUniqueSteps();
                String numStepsString = numSteps == null || numSteps < 0
                        ? "" : numSteps.toString();
                String numUniqueStepsString = numUniqueSteps == null || numUniqueSteps < 0
                        ? "" : numUniqueSteps.toString();

                %>
                <td><%=sampleRow.getNumTransactions() %></td>
                <td><%=sampleRow.getNumStudents() %></td>
                <td><%=sampleRow.getNumProblems() %></td>
                <td><%=numStepsString %></td>
                <td><%=numUniqueStepsString %></td>




                <!-- Actionable icon column -->
                <td class="actionableIconColumn"><%=sampleRow.getActionableIcons() == null
                        ? "" : sampleRow.getActionableIcons() %></td>

            </tr>
            <tr class="history_row <%=evenOrOdd %>">
                <td colspan="12">
                    <table id="sample_history_<%=sampleId %>" name="sample-history"
                        class="sample-history">

                        <%

            %>
                    </table>
                </td>
            </tr>
            <%
            rowCount++;
        } // end sampleRow table

        if (datasetId != null) {
            %>
            <input type="hidden" id="datasetId" name="datasetId" value="<%=datasetId %>" />
            <%
        }


        %>
        </table>
    </form>
    <div id="requestEditRoleDialog" class="requestEditRoleDialog"></div>
    <div id="s2dConfirmationDialog"></div>
</div>
<%
    }
%>