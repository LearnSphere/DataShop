<%@ include file="/doctype.jspf"%>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Mike Komisin
// Version: $Revision:  $
// Last modified by: $Author:  $
// Last modified on: $Date: $
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
<%@page import="edu.cmu.pslc.datashop.servlet.accessrequest.AccessRequestHelper"%>

<%@page import="org.apache.commons.collections.comparators.NullComparator"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.sampletodataset.SampleHistoryDto"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.AbstractServlet"%>


<!-- header -->
<%@ include file="/header_variables.jspf"%>
<%
    pageTitle = "Save Sample as Dataset";

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("sample_to_dataset.css");
    cssIncludes.add("inlineEditor.css");
    cssIncludes.add("progress_bar.css");
    // samples.css is already included via dataset_info.jsp

    jsIncludes.add("javascript/sampletodataset/SampleToDataset.js");
    jsIncludes.add("javascript/object/Truncator.js");
    jsIncludes.add("javascript/object/InlineEditor.js");
    jsIncludes.add("javascript/object/ProgressBar.js");

%>

<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>
<!-- code -->
<%
String rowHeader[] = { "Project", "Dataset Name", "Description", "Number of Transactions",
        "Number of Students", "Number of Problems", "Number of Steps", "Number of Unique Steps",
        "Filters" };
ArrayList<String> tableList = (ArrayList<String>) request.getAttribute("tableList");

String hiddenSampleId = (String) request.getAttribute("s2d_sample");


boolean saveAsDatasetAllowed = true;
if ((datasetItem.getReleasedFlag() == null)
        || !datasetItem.getReleasedFlag()
        || (datasetItem.getProject() == null)) {
    saveAsDatasetAllowed = false;
}

    String recentDsPage = (String)session.getAttribute("recent_ds_page"); 
    StringBuffer cancelHref = new StringBuffer("DatasetInfo");
    if ((recentDsPage != null) && (!recentDsPage.equals("null"))) {
       cancelHref = new StringBuffer(recentDsPage);
       // Once read, reset.
       session.setAttribute("recent_ds_page", null);
    }
    cancelHref.append("?datasetId=");
    cancelHref.append(datasetId);
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">

<div id="sample-to-dataset-page">
    <h1>Save Sample as Dataset</h1>
    <!-- Begin #samples_page div -->
    <div id="sample_to_dataset_form">
        <%
        if (tableList != null) {
            %>
            <table id="sample-to-dataset-page-table">
            <%
            int rowCt = 0;
            // The table and the row headers should be the same size/length.
            if (rowHeader.length == tableList.size()) {
                String editToolTip = " title=\"Click to edit\"";
                for (String tableValue : tableList) {
                    if (rowHeader[rowCt].equals("Project")) {
                        // do nothing inside here, but don't get rid of this conditional block
                    } else if (rowHeader[rowCt].equals("Dataset Name")) {
                        %><tr><td class="s2d-header"><p><%=rowHeader[rowCt] %></p></td>
                        <td class="s2d-value">
                        <input type="text" id="dataset-name" name="dataset-name" size="100"
                            value="<%=tableValue == null ? "" : tableValue %>">
                        <br /><p id="nameExists"></p>
                        </td></tr><%
                    } else if (rowHeader[rowCt].equals("Description")) {
                        %><tr><td class="s2d-header"><p><%=rowHeader[rowCt] %></p></td>
                        <td class="s2d-value">
                        <span id="dataset-desc" <%=editToolTip %>><p><%=tableValue == null ? "" : tableValue
                        %><img src="images/pencil.png" title="Click to edit" id="datasetDescription_edit" class="edit_icon"></p></span>
                        </td></tr></table>

                        <table id="sample-to-dataset-stats-table">
                        <tr><th class="s2d-header">Metric name</th><th class="s2d-value">Value</th></tr>
                        <%

                    } else if (rowHeader[rowCt].equals("Filters")) {
                        %></table><table id="sample-to-dataset-submit-table">
                        <tr><td class="s2d-header"><p><%=rowHeader[rowCt] %></p></td>
                        <td class="s2d-value"><p>
                        <%=tableValue == null || tableValue.isEmpty() ? "no filters" : tableValue %>
                        </p></td></tr><%
                    } else {
                        %><tr><td class="s2d-header"><p><%=rowHeader[rowCt] %></p></td>
                        <td class="s2d-value"><p>
                        <%=tableValue == null ? "" : tableValue %>
                        </p></td></tr><%
                    }

                    rowCt++;
                }
                %>
                <tr>
                <td class="s2d-header"><p>Include <br/>user-created KC Models</p></td>
                <td><p id="includeKCMs">
                        <input type="radio" id="s2d-include-kcms" name="includeKCMs" value="true" checked />
                        <label for="s2d-include-kcms">include models</label></p>
                    <p id="excludeKCMs">
                        <input type="radio" id="s2d-exclude-kcms" name="includeKCMs" value="false" />
                        <label for="s2d-exclude-kcms">exclude models</label></p>
                </td>
                </tr>
                <tr>
                <td colspan="2" >
                <div id="saveAsDatasetInfoDiv"><p>This dataset will be created within the same project as the source dataset. It will be assigned the same properties,
                including permissions, as the original dataset except for name and description.</p></div>
                </td>
                </tr>

                <tr><td colspan="2" id="s2d-submit-row">
                <input type="button" id="s2d-submit" value="Save as Dataset" <%
                if (!saveAsDatasetAllowed) {
                    %>disabled<%
                }
                %>/>
                <input type="button" id="s2d-cancel" onClick="location.href='<%=cancelHref.toString() %>'" value="Cancel" />
                </td></tr>
                <%
                if (hiddenSampleId != null) {
                    %><%=hiddenSampleId %> <%
                }
            }
            %>
            </table>
            <%
        }
        %>
    </form>

</div> <!-- End #upload_dataset_div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- footer -->
<%@ include file="/footer.jspf" %>


<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/sample-to-dataset.jspf" %>
</div>

</body>
</html>