<%@page import="edu.cmu.pslc.datashop.item.ImportQueueItem"%>
<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2013
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14324 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-10-05 12:58:37 -0400 (Thu, 05 Oct 2017) $
// $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.importqueue.ImportQueueServlet"%>
<%@page import="edu.cmu.pslc.datashop.item.ImportQueueItem"%>
<%@page import="java.util.List"%>

<!--  header -->
<%@ include file="/header_variables.jspf" %>
<%
    pageTitle = "Import Queue";
    cssIncludes.add("ImportQueue.css");
    cssIncludes.add("UploadDataset.css");
    cssIncludes.add("message.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
    jsIncludes.add("javascript/object/Truncator.js");
%>
<%@ include file="/header.jspf" %>
<%@ include file="/access_request_notifications.jspf" %>

<!-- code -->
<%!
    // TD element holds the displayed text. So we convert HTML characters to entities,
    // and add line breaks (br) elements.
    String cleanInputText(String str) {
        String textCleaner = str.replaceAll("&","&amp;").replaceAll(">","&gt;").replaceAll("<","&lt;");
        String textWithBrs = textCleaner.replaceAll("\n","<br />").replaceAll("\"","&quot;");
        return textWithBrs;
    }
%>
<%
    List<ImportQueueDto> queueList = (List<ImportQueueDto>)
            request.getAttribute(ImportQueueServlet.REQ_ATTRIB_IMPORT_QUEUE);
    List<ImportQueueDto> recentList = (List<ImportQueueDto>)
            request.getAttribute(ImportQueueServlet.REQ_ATTRIB_RECENT_ITEMS);
    List<ImportQueueDto> nodataList = (List<ImportQueueDto>)
            request.getAttribute(ImportQueueServlet.REQ_ATTRIB_RECENT_NO_DATA);
    String modeHtml = (String)request.getAttribute(ImportQueueServlet.REQ_ATTRIB_MODE);

    String lastDaysRecent = (String)session.getAttribute(ImportQueueServlet.ATTRIB_LAST_DAYS_RECENT);
    String lastDaysNoData = (String)session.getAttribute(ImportQueueServlet.ATTRIB_LAST_DAYS_NODATA);
    if (lastDaysRecent == null) {
        lastDaysRecent = ImportQueueServlet.VALUE_DEFAULT;
    }
    if (lastDaysNoData == null) {
        lastDaysNoData = ImportQueueServlet.VALUE_DEFAULT;
    }

    boolean showEmailMsg = false;
    String emailSentMsg = (String)session.getAttribute(ImportQueueServlet.EMAIL_SENT_ATTRIB);
    if (emailSentMsg != null) {
        showEmailMsg = true;
        session.setAttribute(ImportQueueServlet.EMAIL_SENT_ATTRIB, null);
    }

    ImportQueueContext iqContext = ImportQueueContext.getContext(request);
    String loadedSortByColumn = iqContext.getLoadedSortByColumn();
    Boolean loadedIsAscending = iqContext.isLoadedAscending(loadedSortByColumn);
    String noDataSortByColumn = iqContext.getNoDataSortByColumn();
    Boolean noDataIsAscending = iqContext.isNoDataAscending(noDataSortByColumn);

    // Default headers, i.e., no sorting.
    String datasetColumnHeader = ImportQueueDto.COLUMN_DATASET;
    String userColumnHeader = ImportQueueDto.COLUMN_USER;
    String statusColumnHeader = ImportQueueDto.COLUMN_STATUS;
    String lastUpdateColumnHeader = ImportQueueDto.COLUMN_LAST_UPDATE;

    // Headers for sortable 'Recently Loaded' table.
    String sortedLoadedDatasetColumnHeader =
           "<a href=\"javascript:sortLoadedImportQueueTable('"
           + ImportQueueDto.COLUMN_DATASET + "')\">"
           + ImportQueueDto.COLUMN_DATASET + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_DATASET, loadedSortByColumn, loadedIsAscending)
           + "\" />";
    String sortedLoadedUserColumnHeader =
           "<a href=\"javascript:sortLoadedImportQueueTable('"
           + ImportQueueDto.COLUMN_USER + "')\">"
           + ImportQueueDto.COLUMN_USER + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_USER, loadedSortByColumn, loadedIsAscending)
           + "\" />";
    String sortedLoadedStatusColumnHeader =
           "<a href=\"javascript:sortLoadedImportQueueTable('"
           + ImportQueueDto.COLUMN_STATUS + "')\">"
           + ImportQueueDto.COLUMN_STATUS + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_STATUS, loadedSortByColumn, loadedIsAscending)
           + "\" />";
    String sortedLoadedLastUpdateColumnHeader =
           "<a href=\"javascript:sortLoadedImportQueueTable('"
           + ImportQueueDto.COLUMN_LAST_UPDATE + "')\">"
           + ImportQueueDto.COLUMN_LAST_UPDATE + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_LAST_UPDATE, loadedSortByColumn, loadedIsAscending)
           + "\" />";

    // More of the same... for the 'Recently Created... No Data' table
    String sortedNoDataDatasetColumnHeader =
           "<a href=\"javascript:sortNoDataImportQueueTable('"
           + ImportQueueDto.COLUMN_DATASET + "')\">"
           + ImportQueueDto.COLUMN_DATASET + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_DATASET, noDataSortByColumn, noDataIsAscending)
           + "\" />";
    String sortedNoDataUserColumnHeader =
           "<a href=\"javascript:sortNoDataImportQueueTable('"
           + ImportQueueDto.COLUMN_USER + "')\">"
           + ImportQueueDto.COLUMN_USER + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_USER, noDataSortByColumn, noDataIsAscending)
           + "\" />";
    String sortedNoDataStatusColumnHeader =
           "<a href=\"javascript:sortNoDataImportQueueTable('"
           + ImportQueueDto.COLUMN_STATUS + "')\">"
           + ImportQueueDto.COLUMN_STATUS + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_STATUS, noDataSortByColumn, noDataIsAscending)
           + "\" />";
    String sortedNoDataLastUpdateColumnHeader =
           "<a href=\"javascript:sortNoDataImportQueueTable('"
           + ImportQueueDto.COLUMN_LAST_UPDATE + "')\">"
           + ImportQueueDto.COLUMN_LAST_UPDATE + "</a>"
           + "<img src=\""
           + ImportQueueDto.getSortImage(ImportQueueDto.COLUMN_LAST_UPDATE, noDataSortByColumn, noDataIsAscending)
           + "\" />";

    // Get the Import Queue transient message information
    Boolean showErrorMsg = false;
    Boolean showSuccessMsg = false;
    String msgText = (String)request.getAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_TEXT);
    String msgLevel = (String)request.getAttribute(ImportQueueServlet.REQ_ATTRIB_IQ_MSG_LEVEL);
    if (msgLevel != null && msgText != null) {
        if (msgLevel.equals(ImportQueueServlet.MSG_LEVEL_ERROR)) {
            showErrorMsg = true;
        } else if (msgLevel.equals(ImportQueueServlet.MSG_LEVEL_SUCCESS)) {
            showSuccessMsg = true;
        }
    }
%>

<!-- body -->
<tr>
<%@ include file="/main_side_nav.jspf" %>
<td id="main_td">
<div id="main">
<div id="contents">
  <% if (showEmailMsg) { //Email set transient message %>
  <script type="text/javascript">successPopup("<%=emailSentMsg %>");</script>
  <% } %>
  <% if (showErrorMsg) { //Import Queue transient message %>
  <script type="text/javascript">errorPopup("<%=msgText %>");</script>
  <% } else if (showSuccessMsg) { %>
  <script type="text/javascript">successPopup("<%=msgText %>");</script>
  <% } %>

  <div id="import-queue-admin-div">
    <div id="import-queue-div">
    <h1>Import Queue</h1>
    <div id="queueModeDiv">
        <%=modeHtml%>
    </div>
        <%
        List<ImportQueueDto> importQueueList = queueList;
        String iqTableViewType = "admin_queue";
        // using default columnHeader variables
        %>
        <%@ include file="/jsp_dataset/import_queue_table.jspf" %>

    <h1>Recently Loaded, Canceled or Failed Items</h1>
        <form id="lastDaysRecentForm" name="lastDaysRecentForm"
          action="<%=ImportQueueServlet.SERVLET_NAME%>" method="post" >
            <p class="show-items">Show items from the last
                <select name="<%=ImportQueueServlet.PARAM_LAST_DAYS_RECENT%>"
                    id="<%=ImportQueueServlet.PARAM_LAST_DAYS_RECENT%>"
                    onchange="javascript:updateRecentItems();">
                <%  for (String key : ImportQueueServlet.LD_VALUE_TXT_MAP.keySet()) {
                    String value = ImportQueueServlet.LD_VALUE_TXT_MAP.get(key);
                    String selected = "";
                    if (lastDaysRecent.equals(key)) {
                        selected = "selected";
                    }
                    %>
                    <option value="<%=key%>" <%=selected%>><%=value%></option>
                <% } // end for loop %>
                </select>
            </p>
        </form>
        <%
        importQueueList = recentList;
        iqTableViewType = "admin_recent";
        // use sortable columnHeader variables, modified for 'Recently Loaded'
        datasetColumnHeader = sortedLoadedDatasetColumnHeader;
        userColumnHeader = sortedLoadedUserColumnHeader;
        statusColumnHeader = sortedLoadedStatusColumnHeader;
        lastUpdateColumnHeader = sortedLoadedLastUpdateColumnHeader;
        %>
        <%@ include file="/jsp_dataset/import_queue_table.jspf" %>

    <h1>Recently Created Datasets with No Data</h1>
        <form id="lastDaysNoDataForm" name="lastDaysNoDataForm"
              action="<%=ImportQueueServlet.SERVLET_NAME%>" method="post" >
            <p class="show-items">Show items from the last
                <select name="<%=ImportQueueServlet.PARAM_LAST_DAYS_NODATA%>"
                    id="<%=ImportQueueServlet.PARAM_LAST_DAYS_NODATA%>"
                    onchange="javascript:updateNoDataItems();">
                <%  for (String key : ImportQueueServlet.LD_VALUE_TXT_MAP.keySet()) {
                    String value = ImportQueueServlet.LD_VALUE_TXT_MAP.get(key);
                    String selected = "";
                    if (lastDaysNoData.equals(key)) {
                        selected = "selected";
                    }
                    %>
                    <option value="<%=key%>" <%=selected%>><%=value%></option>
                <% } // end for loop %>
                </select>
            </p>
        </form>
        <%
        importQueueList = nodataList;
        iqTableViewType = "admin_nodata";
        // use sortable columnHeader variables, modified for 'No Data'
        datasetColumnHeader = sortedNoDataDatasetColumnHeader;
        userColumnHeader = sortedNoDataUserColumnHeader;
        statusColumnHeader = sortedNoDataStatusColumnHeader;
        lastUpdateColumnHeader = sortedNoDataLastUpdateColumnHeader;
        %>
        <%@ include file="/jsp_dataset/import_queue_table.jspf" %>

    </div> <!-- End #import-queue-div -->
  </div> <!-- End #import-queue-admin-div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Populated but hidden select boxes for edit status dialog -->
<select id="statusSelectTab" name="statusSelectTab" class="hidden">
    <%  for (String key : ImportQueueItem.STATUS_TXT_MAP_TAB.keySet()) {
            String value = ImportQueueItem.STATUS_TXT_MAP_TAB.get(key);
            %>
            <option value="<%=key%>"><%=value%></option>
    <% } %>
</select>
<!-- Populated but hidden select boxes for edit status dialog -->
<select id="statusSelectXml" name="statusSelectXml" class="hidden">
    <%  for (String key : ImportQueueItem.STATUS_TXT_MAP_XML.keySet()) {
            String value = ImportQueueItem.STATUS_TXT_MAP_XML.get(key);
            %>
            <option value="<%=key%>"><%=value%></option>
    <% } %>
</select>
<!-- Populated but hidden select boxes for edit status dialog -->
<select id="statusSelectDiscourse" name="statusSelectDiscourse" class="hidden">
    <%  for (String key : ImportQueueItem.STATUS_TXT_MAP_DISCOURSE.keySet()) {
            String value = ImportQueueItem.STATUS_TXT_MAP_DISCOURSE.get(key);
            %>
            <option value="<%=key%>"><%=value%></option>
    <% } %>
</select>

<!-- Un-populated div needed for modal windows using jQuery -->
<div id="iqEditStatusDialog" class="iqDialog"></div>
<div id="iqCancelImportDialog" class="iqDialog"></div>
<div id="renameDatasetDialog" class="renameDatasetDialog"> </div>
<div id="deleteDatasetDialog" class="deleteDatasetDialog"> </div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/import-queue.jspf" %>
</div>

</body>
</html>
