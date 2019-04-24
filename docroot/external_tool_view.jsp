<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human-Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
  // Author: alida
  // Version: $Revision: 10683 $
  // Last modified by: $Author: ctipper $
  // Last modified on: $Date: 2014-02-28 12:27:51 -0500 (Fri, 28 Feb 2014) $
  // $KeyWordsOff: $
%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolsContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolFileDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolTableDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.exttools.ExternalToolPageDto"%>
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
<%
    ExternalToolPageDto pageDto =
        (ExternalToolPageDto)request.getAttribute(ExternalToolPageDto.ATTRIB_NAME);
    String message = pageDto.getMessage();
    String messageLevel = pageDto.getMessageLevel();

    // Special handling for file uploads. If the 'message' and 'messageLevel'
    // are NULL on the request, check the session.
    if ((message == null) && (messageLevel == null)) {
        ExternalToolPageDto tmpPageDto =
            (ExternalToolPageDto)session.getAttribute(ExternalToolPageDto.ATTRIB_NAME);
        if (tmpPageDto != null) {
            message = tmpPageDto.getMessage();
            messageLevel = tmpPageDto.getMessageLevel();
        }
    }
    // Now that we've read the data, clear it from the session.
    session.setAttribute(ExternalToolPageDto.ATTRIB_NAME, null);

    ExternalToolDto toolDto = pageDto.getToolDto();
    Integer toolId = toolDto.getId();
    List<ExternalToolFileDto> fileList = pageDto.getFileList();
    boolean hasFiles = fileList.size() > 0 ? true : false;

    ExternalToolsContext context = ExternalToolsContext.getContext(request);
    String sortByColumn = context.getFileSortByColumn();
    Boolean isAscending = context.isFileAscending(sortByColumn);

    boolean hasEditAuth = pageDto.hasEditAuth();
    boolean hasAdminAuth = pageDto.hasAdminAuth();
    boolean isLoggedIn = remoteUser != null ? true : false;

    String td_class_name_odd = "cell";
    String td_class_name_even = "cell even";
    String td_class_name = td_class_name_odd;

    String descriptionWithoutQuotes = toolDto.getDescription().replaceAll("\"","&quot;");
    //add the html if a url is found in the description
    String descriptionToDisplay = toolDto.fixUrls(toolDto.getDescription());    
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

    <div id="toolPageIndexDiv">
        <a href="ExternalTools" id="indexLink">External Tools</a> /
        <div id="toolPageViewNameDiv">
            <span id="toolPageViewNameSpan"><%=toolDto.getName()%></span>
            <% if (hasEditAuth || hasAdminAuth ) { %>
            <a id="toolPageEditNameLink" href="javascript:showEditToolNameField(<%=toolId%>)">edit</a>
            <% } %>
        </div>
        <% if (hasEditAuth || hasAdminAuth ) { %>
        <div id="toolPageEditNameDiv">
            <input id="toolNameField" class="toolNameField" name="toolNameField" size="30"
                   value="<%=toolDto.getName()%>">
            <input type="button" id="toolPageNameSaveButton"
                   value="Save" onclick="javascript:saveToolNameChanges(<%=toolId%>)" />
            <input type="button" id="toolPageNameCancelButton"
                   value="Cancel" onclick="javascript:cancelToolNameChanges()" />
            <input type="hidden" id="toolPageNameHiddenField"
                   value="<%=toolDto.getName()%>" />
        </div>
        <% } %>
    </div>
    
    <div id="toolPageLeftDiv">
        <div class="toolPageLeftSubDivs"><div class="label">Description:</div>
            <div id="toolPageViewDescriptionDiv">
                <span id="toolPageViewDescriptionSpan" class="externalToolsDescSpan"><%=descriptionToDisplay%></span>
                <% if (hasEditAuth || hasAdminAuth ) { %>
                <a id="toolPageEditDescriptionLink"
                   href="javascript:showEditToolDescriptionField(<%=toolId%>)">edit</a>
                <% } %>
            </div>
            <% if (hasEditAuth || hasAdminAuth ) { %>
            <div id="toolPageEditDescriptionDiv">
                <textarea rows="8" cols="50" id="toolDescriptionField"
                          class="toolDescriptionField"
                          name="toolDescriptionField"><%=toolDto.getDescription()%></textarea>
                <br />
                <div id="toolPageDescriptionMaxLen">Enter no more than 500 characters.</div>
                <input type="button" id="toolPageDescriptionSaveButton"
                       value="Save" onclick="javascript:saveToolDescriptionChanges(<%=toolId%>)" />
                <input type="button" id="toolPageDescriptionCancelButton"
                       value="Cancel" onclick="javascript:cancelToolDescriptionChanges()" />
                <input type="hidden" id="toolPageDescriptionHiddenField"
                       value="<%=descriptionWithoutQuotes%>" />
            </div>
            <% } %>
        </div>

        <div class="toolPageLeftSubDivs clear"><div class="label">Language:</div>
            <div id="toolPageViewLanguageDiv">
                <span id="toolPageViewLanguageSpan"><%=toolDto.getLanguage()%></span>
                <% if (hasEditAuth || hasAdminAuth ) { %>
                <a id="toolPageEditLanguageLink" href="javascript:showEditToolLanguageField(<%=toolId%>)">edit</a>
                <% } %>
            </div>
            <% if (hasEditAuth || hasAdminAuth ) { %>
            <div id="toolPageEditLanguageDiv">
                <input id="toolLanguageField" class="toolLanguageField" name="toolLanguageField"
                       size="30" value="<%=toolDto.getLanguage()%>">
                <input type="button" id="toolPageLanguageSaveButton"
                       value="Save" onclick="javascript:saveToolLanguageChanges(<%=toolId%>)" />
                <input type="button" id="toolPageLanguageCancelButton"
                       value="Cancel" onclick="javascript:cancelToolLanguageChanges()" />
                <input type="hidden" id="toolPageLanguageHiddenField"
                       value="<%=toolDto.getLanguage()%>" />
            </div>
            <% } %>
        </div>

        <%
            String webPage = toolDto.getWebPage();
            String webPageToLink = webPage;
            if (!webPage.startsWith("http://") && !webPage.startsWith("https://")) {
                webPageToLink = "http://" + webPage;
            }
        %>

        <div class="toolPageLeftSubDivs clear"><div class="label">Home page / External resource:</div>
            <div id="toolPageViewHomePageDiv">
                <span id="toolPageViewHomePageSpan"><a href="<%=webPageToLink%>"
                      onclick="javascript:countHomePageClicks(<%=toolId %>)"
                      target="_blank"><%=webPage%></a></span>
                <% if (hasEditAuth || hasAdminAuth ) { %>
                <a id="toolPageEditHomePageLink" href="javascript:showEditToolHomePageField(<%=toolId%>)">edit</a>
                <% } %>
            </div>
            <% if (hasEditAuth || hasAdminAuth ) { %>
            <div id="toolPageEditHomePageDiv">
                <input id="toolHomePageField" class="toolHomePageField" name="toolHomePageField"
                       size="30" value="<%=toolDto.getWebPage()%>">
                <input type="button" id="toolPageHomePageSaveButton"
                       value="Save" onclick="javascript:saveToolHomePageChanges(<%=toolId%>)" />
                <input type="button" id="toolPageHomePageCancelButton"
                       value="Cancel" onclick="javascript:cancelToolHomePageChanges()" />
                <input type="hidden" id="toolPageHomePageHiddenField"
                       value="<%=toolDto.getWebPage()%>" />
            </div>
            <% } %>
        </div>

    </div>
    <div id="toolPageRightDiv">
        <div class="rightRow"><div>Contributor:</div> <%=toolDto.getContributor()%></div>
        <div class="rightRow clear"><div>Added:</div> <%=toolDto.getAddedTimeString()%></div>
        <div class="rightRow clear"><div>Updated:</div> <%=toolDto.getUpdatedTimeString()%></div>
        <div class="rightRow clear"><div>Downloads:</div>
             <span id="downloadCount_<%=toolId %>"> <%=toolDto.getDownloads()%></span>
        </div>
    </div>

    <% if (hasEditAuth || hasAdminAuth ) { %>
        <div id="deleteThisToolDiv">
            <div id="toolDeleteLinkDiv" class="toolDeleteLinkDiv">
                <a id="deleteToolLink"
                   href="javascript:showToolDeleteSureDiv(<%=toolId%>)">
                delete this tool
                </a>
            </div>

            <div id="toolDeleteSureDiv" class="toolDeleteSureDiv" >
                delete this tool?&nbsp
                <a id="toolDeleteNoLink" href="javascript:showToolDeleteLinkDiv(<%=toolId%>)">no</a>
                &nbsp/&nbsp
                <a id="toolDeleteYesLink" href="javascript:deleteTool(<%=toolId%>)">yes</a>
            </div>
        </div>
    <% } %>

    <div id="toolPageBottomHalfDiv">

    <h2>Files</h2>

        <div id="downloadAllFilesButton">
        <p>
            <a href="#" id="downloadAllFilesLink"
               <% if (hasFiles) { %>
                   class="ui-state-default ui-corner-all request_link"
                   onclick="javascript:downloadAllFiles(<%=toolId%>)"
               <% } else { %>
                   class="ui-state-default ui-corner-all dead_link ui-state-disabled"
                   title="No files to download"
               <% } %>
               ><span class=""></span>Download All</a>
        </p>
        </div>

    <% if (hasEditAuth || hasAdminAuth ) { %>
        <div id="uploadFileButton">
            <p>
            <a href="#" id="uploadToolFileLink"
               class="ui-state-default ui-corner-all"
               onclick="javascript:openUploadToolFileDialog(<%=toolId%>)">
            <span class=""></span>Upload File</a>
            </p>
        </div>
    <% } %>

    <table id="externalToolsFilesTable" class="dataset-box">
    <colgroup>
        <col style="width:35%" />
        <col style="width:15%" />
        <col style="width:20%" />
        <col style="width:25%" />
        <% if (hasEditAuth || hasAdminAuth ) { %>
        <col style="width:5%" />
        <% } %>
    </colgroup>
        <tr>
        <th>
            <a href="javascript:sortFiles(<%=toolId%>, '<%=ExternalToolFileDto.COLUMN_NAME%>')">
            <%=ExternalToolFileDto.COLUMN_NAME %></a>
            <img src="<%=ExternalToolFileDto.getSortImage(ExternalToolFileDto.COLUMN_NAME, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortFiles(<%=toolId%>, '<%=ExternalToolFileDto.COLUMN_SIZE%>')">
            <%=ExternalToolFileDto.COLUMN_SIZE %></a>
            <img src="<%=ExternalToolFileDto.getSortImage(ExternalToolFileDto.COLUMN_SIZE, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortFiles(<%=toolId%>, '<%=ExternalToolFileDto.COLUMN_DOWNLOADS%>')">
            <%=ExternalToolFileDto.COLUMN_DOWNLOADS %></a>
            <img src="<%=ExternalToolFileDto.getSortImage(ExternalToolFileDto.COLUMN_DOWNLOADS, sortByColumn, isAscending) %>" />
        </th>
        <th>
            <a href="javascript:sortFiles(<%=toolId%>, '<%=ExternalToolFileDto.COLUMN_UPDATED%>')">
            <%=ExternalToolFileDto.COLUMN_UPDATED %></a>
            <img src="<%=ExternalToolFileDto.getSortImage(ExternalToolFileDto.COLUMN_UPDATED, sortByColumn, isAscending) %>" />
        </th>
        <% if (hasEditAuth || hasAdminAuth ) { %>
        <th> </th>
        <% } %>
        </tr>
    <%
    int idx = 1;
    if (fileList != null && fileList.size() > 0) {
        for (Iterator iter = fileList.iterator(); iter.hasNext();) {
            ExternalToolFileDto fileDto = (ExternalToolFileDto)iter.next();
            Integer fileId = fileDto.getFileId();

            if (idx % 2 == 0) {
                td_class_name = td_class_name_even;
            } else {
                td_class_name = td_class_name_odd;
            }
            %>

            <tr>
            <td class="<%=td_class_name%>">
                <a id="file_"<%=fileId%>
                   name="fileLink"
                   href="javascript:downloadFile(<%=toolId%>, <%=fileId%>)">
                    <%= fileDto.getFileName() %>
                </a>
            </td>
            <td class="<%=td_class_name%>"><%= fileDto.getFileSizeString() %></td>
            <td class="<%=td_class_name%>"><%= fileDto.getDownloadsString() %></td>
            <td class="<%=td_class_name%>">
                 <span class="date"><%= fileDto.getUpdatedTimeString() %></span>
            </td>

            <% if (hasEditAuth || hasAdminAuth ) { %>
            <td class="<%=td_class_name%>" id="button_<%=fileId%>">

                <div id="fileDeleteImageDiv_<%=fileId%>" class="editButtons">
                    <a href="javascript:showFileDeleteSureDiv(<%=fileId%>)">
                        <img src="images/delete.gif" alt="Delete File" title="Delete File">
                    </a>
                </div>

                <div id="fileDeleteSureDiv_<%=fileId%>" class="fileDeleteSureDiv" >
                    delete this file?&nbsp
                    <a href="javascript:showFileDeleteImageDiv(<%=fileId%>)">no</a>
                    &nbsp/&nbsp
                    <a href="javascript:deleteFile(<%=toolId%>, <%=fileId%>)">yes</a>
                </div>

            </td>
            <% } %>
            </tr>
            <%
            idx++;
        } // end for loop
    } else { %>
        <tr>
        <td class="<%=td_class_name%>" style="font-style:oblique">No files found.</td>
        <td class="<%=td_class_name%>"></td>
        <td class="<%=td_class_name%>"></td>
        <td class="<%=td_class_name%>"></td>
        <% if (hasEditAuth || hasAdminAuth ) { %>
        <td class="<%=td_class_name%>"></td>
        <% } %>
        </tr>
    <% } %>
    </table>

    <% if (hasEditAuth) { %>
        <div id="toolPageReadmeTipDiv">
            <strong>Tip:</strong> We recommend you include a README file that explains how to use your
            tool or points to a webpage that does the same.
        </div>
    <% } %>
    </div> <!-- End toolPageBottomHalf div -->

</div> <!-- End #external-tools div -->
</div> <!-- End #contents div -->
</div> <!-- End #main div -->

</td>
</tr>

<!-- Un-populated div needed for modal windows using jQuery -->
<div id="uploadToolFileDialog" class="uploadToolFileDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>

</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/external-tool-view.jspf" %>
</div>

</body>
</html>
