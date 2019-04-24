<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 14135 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-06-16 13:54:35 -0400 (Fri, 16 Jun 2017) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>

<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext"%>

<%@ include file="/header_variables.jspf" %>

<%
    pageTitle = "Files";
    cssIncludes.add("datasetInfo.css");
    cssIncludes.add("filesInfo.css");
    cssIncludes.add("jquery-ui-1.8.18.custom.css");
    cssIncludes.add("ImportQueue.css");

    jsIncludes.add("javascript/lib/jquery-1.7.1.min.js");
    jsIncludes.add("javascript/lib/jquery-ui-1.8.17.custom.min.js");
    jsIncludes.add("javascript/FilesInfo.js");
    jsIncludes.add("javascript/FileUpload.js");
    jsIncludes.add("javascript/object/Truncator.js");
    jsIncludes.add("javascript/ImportQueue.js");
    jsIncludes.add("javascript/ImportQueue/ImportQueueDialogs.js");
%>

<!--  header -->
<%@ include file="/header.jspf" %>

<!-- get the nav helper and check authorization -->
<%
    boolean authorized = false;
    info = (DatasetContext)session.getAttribute("datasetContext_" + datasetId);
    navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    projHelper = HelperFactory.DEFAULT.getProjectHelper();

    if ((datasetItem == null) && (info != null)) {
        datasetItem = info.getDataset();
    }
    if (navHelper != null && info != null) {
        if (datasetItem != null && projHelper != null) {
            authorized = projHelper.isAuthorized(remoteUser, (Integer)datasetItem.getId());
        }
    }

    boolean userLoggedIn = (remoteUser == null) ? false : true;

    String messageType = null;
    String message = null;
    if (info != null) {
       FilesInfoContext filesInfoContext = info.getFilesInfoContext();    
       messageType = filesInfoContext.getFileMessageType();
       message = filesInfoContext.getFileMessage();

       // Now reset the message.
       filesInfoContext.setFileMessage(null);
       filesInfoContext.setFileMessageType(null);
    }

    String tabHeaderStr = "";
    if (info != null) {
        tabHeaderStr = navHelper.displayTabs("FilesInfo", info, authorized, user);
    } else {
        tabHeaderStr = navHelper.displayTabs("FilesInfo", (Integer)datasetItem.getId(),
                                             authorized, user);
    }
    
    Long fiNumPapers = (Long)request.getAttribute("numPapers");
    Long fiNumExternalAnalyses = (Long)request.getAttribute("numExternalAnalyses");
    Long fiNumFiles = (Long)request.getAttribute("numFiles");
%>

<!-- body -->

<tr id="body"><td id="nav">

</td>

<td id="content" colspan="2">

<div id="tabheader"><%=tabHeaderStr %></div>

<div id="main">
    <input type="hidden" id="userLoggedIn" value="<%=userLoggedIn %>" />
    <input type="hidden" id="userAuthorized" value="<%=authorized %>" />

    <input type="hidden" id="numPapers" value="<%=fiNumPapers%>" />
    <input type="hidden" id="numExternalAnalyses" value="<%=fiNumExternalAnalyses%>" />
    <input type="hidden" id="numFiles" value="<%=fiNumFiles%>" />

    <div id="subtab" class="filesSubtab"></div>
    <div id="contents">
        <%=datasetNameString%>
        <%@ include file="/dataset_info_extra.jspf" %>
        <div id="main_content_div">

        <!-- make note of attrs needed by javascript -->
        <input type="hidden" id="filesMessage" value="<%=message %>" />
        <input type="hidden" id="filesMessageLevel" value="<%=messageType %>" />

        </div>
    </div>
</div></td></tr>

<!-- Unpopulated div's needed for modal windows using jQuery -->
<div id="uploadPaperDialog" class="uploadPaperDialog"></div>
<div id="uploadFileDialog" class="uploadFileDialog"></div>
<div id="uploadExternalAnalysisDialog" class="uploadExternalAnalysisDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none"></div>
<div style="display:none">
<%@ include file="/help/report-level/files.jspf" %>
</div>

<script type="text/javascript">
    onloadObserver.addListener(updateSubTabsWithValues);
</script>

</body>
</html>
