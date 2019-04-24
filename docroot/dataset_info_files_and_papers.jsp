<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14061 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2017-04-27 06:44:18 -0400 (Thu, 27 Apr 2017) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.dto.PaperFile"%>

<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.PaperItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>

<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%!
    // TD element holds the displayed text. So we convert HTML characters to entities, and add line breaks (br) elements.
    String cleanInputText(String str) {
    	String textCleaner = (str == null) ? ""
                : str.replaceAll("&","&amp;").replaceAll(">","&gt;").replaceAll("<","&lt;");
    	String textWithBrs = textCleaner.replaceAll("\n","<br />");
    	return textWithBrs;
    }
    // Input element holds the value for editing. So we escape single quotes by converting to entities.
    String cleanInputTextForEdit(String str) {
    	String textCleaner = (str == null) ? "" : str.replaceAll("\"","&quot;");
    	return textCleaner;
    }
%>

<%
    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
    DatasetContext datasetContext  = (DatasetContext)session.getAttribute("datasetContext_"
    + request.getParameter("datasetId"));

    boolean hasEditAuth = HelperFactory.DEFAULT.getProjectHelper().isDataShopAdmin(
    request.getRemoteUser(),
    datasetContext);

    if (!hasEditAuth) {
        hasEditAuth = HelperFactory.DEFAULT.getProjectHelper().isDataShopAdmin(
        request.getRemoteUser(),
        datasetContext);
    }

    String td_class_name_odd = "cell";
    String td_class_name_even = "cell even";
    String td_class_name = td_class_name_odd;

    FastDateFormat dateFormat = FastDateFormat.getInstance("MMM d, yyyy HH:mm");

	String messageType = (String)session.getAttribute("file_servlet_message_type");
	String message = (String)session.getAttribute("file_servlet_message");
	if (messageType != null && message != null ) {
    if (messageType.compareTo("SUCCESS") == 0) {
				out.print("<p class=\"success\">" + message + "</p>");
    } else if (messageType.compareTo("ERROR") == 0) {
    	out.print("<p class=\"error\">" + message + "</p>");
    } else {
    	out.print("<p class=\"warn\">" + message + "</p>");
    }
    session.setAttribute("file_servlet_message_type", null);
    session.setAttribute("file_servlet_message", null);
	}
%>

    <FORM name="embeddedPapersForm" action="Upload" method="post" enctype="multipart/form-data" onsubmit="">
    <input type="hidden" name="datasetId" value="<%=datasetContext.getDataset().getId() %>" />
    <table id="paperInfo" class="dataset-box">
        <caption>Papers</caption>
        <tr>
            <th class="num">#</th>
            <th>Preferred Citation</th>
            <th>Citation</th>
            <th>Abstract</th>
            <th>Paper</th>
            <th>Uploaded By</th>
            <% if (hasEditAuth) { %>
            <th></th>
            <% } %>
        </tr>
        <%

        PaperItem preferredPaper = filesInfoHelper.getPreferredPaper(datasetContext.getDataset());
        List <PaperFile> paperList =
            filesInfoHelper.getPaperList(datasetContext.getDataset());
        int idx = 1;
        if (paperList != null && paperList.size() > 0) {
            for (Iterator iter = paperList.iterator(); iter.hasNext();) {
                PaperFile paperFile = (PaperFile)iter.next();
                PaperItem paperItem = paperFile.getPaperItem();
                FileItem fileItem = paperFile.getFileItem();
                UserItem owner = paperFile.getOwner();
    	        if (idx % 2 == 0) {
                        td_class_name = td_class_name_even;
    	        } else {
                        td_class_name = td_class_name_odd;
                    }
    	        // In the view, should use <br /> to display line breaks in citation, abstract
    	        String citationText = paperItem.getCitation();
    	        String citationTextWithBr = cleanInputText(citationText);
    	        String citationTextWithNoQuotes = cleanInputTextForEdit(citationText);

    	        String abstractText = paperItem.getPaperAbstract();
    	        String abstractTextWithNoQuotes = cleanInputTextForEdit(abstractText);
    	        String abstractTextWithBr = cleanInputText(abstractText);
    	        String truncatedAbstractText = "";

                String dateString = dateFormat.format(fileItem.getAddedTime());
                String firstInitial =
                    (owner.getFirstName() != null && owner.getFirstName().length() > 0)
                        ? owner.getFirstName().substring(0, 1).toUpperCase() + "." : "-";
                String lastName =
                        (owner.getLastName() != null && owner.getLastName().length() > 0)
                            ? Character.toUpperCase(owner.getLastName().charAt(0)) + owner.getLastName().substring(1)
                                    : "-";
                String name = (firstInitial.equals("-") && lastName.equals("-")) ?
                            (String)owner.getId() : lastName + ", " + firstInitial;

                String updatedBy = name + "<br /><span class=\"date\">" + dateString + "</span >";

    	        boolean truncated = false;
    	        // trucate the abstract text if it's greater than 300 chars.
    	        if (abstractText.length() > 300 ) {
    	        	truncatedAbstractText = abstractTextWithBr.substring(0, 300);
    	        	truncatedAbstractText += " ... ";
    	        	truncated = true;
    	        }

    	%>
                <tr>
                    <td class="<%=td_class_name%> num"><%=idx++%></td>
                    <td class="<%=td_class_name%>" id="preferred_citation_<%=paperItem.getId()%>">
                        <%

                        if ((preferredPaper != null) && (preferredPaper.getId().equals(paperItem.getId()))){
                            %>
                            <img src="images/tick.png" id="preferred_citation_image" alt="Preferred paper for citation" title="Preferred paper for citation">
                            <input type="hidden" id="preferred_citation_hidden" value=true />
                        <%
                        } else {%>
                            <input type="hidden" id="preferred_citation_hidden" value=false />
                        <%} %>
                    </td>
                    <td class="<%=td_class_name%>" id="citation_<%=paperItem.getId()%>">
                     <span name="citation" id="s_citation_<%=paperItem.getId()%>"><%=citationText%></span>
                    	<input type="hidden" value="<%=citationTextWithNoQuotes%>" /></td>
                    	<td class="<%=td_class_name%>" id="abstract_<%=paperItem.getId()%>">
                    		<span name="abstract" id="s_abstract_<%=paperItem.getId()%>"><%=abstractText%></span>
                    		<br>
                    		<input type="hidden" value="<%=abstractTextWithNoQuotes%>" />
                    		<input type="hidden" id="abstract_<%=paperItem.getId()%>_full" value="<%=cleanInputTextForEdit(abstractTextWithBr)%>" />
                    		<input type="hidden" id="abstract_<%=paperItem.getId()%>_truncated" value="<%=cleanInputTextForEdit(truncatedAbstractText)%>" />
                    		</td>
                    <td class="<%=td_class_name%>" id="paper_<%=paperItem.getId()%>">
                    <a title="<%=fileItem.getFileType()%>"
                    href="DownloadPaper?fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>&datasetId=<%=datasetContext.getDataset().getId()%>">
                        <%=fileItem.getDisplayFileName()%>
    				</a>
                    </td>
                    <td class="<%=td_class_name%> updated" ><%= updatedBy %></td>
                    <% if (hasEditAuth) { %>
                    <td class="<%=td_class_name%>" id="button_<%=paperItem.getId()%>">
                      <div id="paperEditDiv_<%=paperItem.getId()%>" class="editButtons">
                        <a href="javascript:makePaperRowEditable(<%=paperItem.getId()%>)">
                        <img src="images/edit.gif" alt="Edit Paper" title="Edit Paper">
                        </a>

                        <a href="javascript:paperDeleteAreYouSure(<%=paperItem.getId()%>)">
                        <img src="images/delete.gif" alt="Delete Paper" title="Delete Paper">
                        </a>
                      </div>

                      <div class="paperSureDiv" id="paperSureDiv_<%=paperItem.getId()%>">
                        delete this paper?&nbsp
                        <a href="javascript:paperDeleteAreYouSureDone(<%=paperItem.getId()%>)">no</a>
                        &nbsp/&nbsp
                        <a href="Delete?datasetId=<%=datasetContext.getDataset().getId() %>&paperId=<%=paperItem.getId()%>">yes</a>
                      </div>

                      <div class="paperSaveDiv" id="paperSaveDiv_<%=paperItem.getId()%>">
                        <input type="button" value="Save" onclick="javascript:savePaperChanges(<%=paperItem.getId()%>)" />
                        <input type="button" value="Cancel" onclick="javascript:cancelPaperChanges(<%=paperItem.getId()%>)" />
                      </div>
                    </td>
                    <% } %>

                </tr>
                <%
            } // end for loop
        } // else there are some papers
        if (idx % 2 == 0) {
            td_class_name = td_class_name_even;
        } else {
            td_class_name = td_class_name_odd;
        }
        if (hasEditAuth) {
        %>
            <tr>
                <td class="<%=td_class_name%> num"><%=idx++%></td>
                <td class="<%=td_class_name%>"><input type="checkbox" name="preferredCitation" id="preferredCitation" onClick="javascript:singleCheck('preferredCitation', this);"/></td>
                <td class="<%=td_class_name%>"><textarea name="paperCitation" id="paperCitation" class="citation" rows="4" cols="20"></textarea></td>
                <td class="<%=td_class_name%>"><textarea name="paperAbstract" rows="4" cols="20"></textarea></td>
                <td class="<%=td_class_name%>"><input name="fileName" type="file"></td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>" id="addPaperTd">
                    <a href="javascript:checkPaperCitation();" id="paperAddButton" title="Add this paper"><img src="images/add_file.gif" /></a></td>
            </tr>
        <%
        } else if (paperList == null || paperList.size() == 0) {
        %>
            <tr>
                <td class="num"></td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>">No papers found.</td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>"></td>
            </tr>
        <%
        }
    %>
    </table>
    </FORM>

    <FORM name="embeddedFilesForm" id="embeddedFilesForm" action="Upload" method="post" enctype="multipart/form-data" onsubmit="return validateFileTitle()">
    <input type="hidden" value="<%=datasetContext.getDataset().getId() %>" name="datasetId" />
    <table id="fileInfo" class="dataset-box">
        <caption>Files</caption>
        <tr>
            <th class="num">#</th>
            <th>Title</th>
            <th>Description</th>
            <th>File</th>
            <th>Uploaded By</th>
            <% if (hasEditAuth) { %>
            <th></th>
            <% } %>
        </tr>
        <%
        List <FileItem> fileList =
            filesInfoHelper.getFileList(datasetContext.getDataset());
        idx = 1;
        if (fileList != null && fileList.size() > 0) {
            for (Iterator iter = fileList.iterator(); iter.hasNext();) {
                FileItem fileItem = (FileItem)iter.next();
	        if (idx % 2 == 0) {
                    td_class_name = td_class_name_even;
	        } else {
                    td_class_name = td_class_name_odd;
                }
	        // In the view, should use <br /> to display line breaks in title, description
	        String titleText = fileItem.getTitle();
	        String titleTextWithBr = cleanInputText(titleText);
	        String titleTextWithNoQuotes = cleanInputTextForEdit(titleText);
            	UserItem owner = fileItem.getOwner();

	        String descText = fileItem.getDescription();
	        String descTextWithBr = cleanInputText(descText);
	        String descTextWithNoQuotes = cleanInputTextForEdit(descText);

            String dateString = dateFormat.format(fileItem.getAddedTime());
            String firstInitial =
                (owner.getFirstName() != null && owner.getFirstName().length() > 0)
                    ? owner.getFirstName().substring(0, 1).toUpperCase() + "." : "-";
            String lastName =
                    (owner.getLastName() != null && owner.getLastName().length() > 0)
                        ? Character.toUpperCase(owner.getLastName().charAt(0)) + owner.getLastName().substring(1)
                                : "-";

            String name = (firstInitial.equals("-") && lastName.equals("-")) ?
                    (String)owner.getId() : lastName + ", " + firstInitial;

            String updatedBy = name + "<br /><span class=\"date\">" + dateString + "</span >";

	        %>
            <tr>
                <td class="<%=td_class_name%> num"><%=idx++%></td>
                <td class="<%=td_class_name%>" id="title_<%=fileItem.getId()%>">
                  <span name="title" id="s_title_<%=fileItem.getId()%>"><%=titleTextWithBr%></span>
			               <input type="hidden" value="<%=titleTextWithNoQuotes%>" /></td>
                <td class="<%=td_class_name%>" id="desc_<%=fileItem.getId()%>">
                  <span name="description" id="s_description_<%=fileItem.getId()%>"><%=descTextWithBr%></span>
                	 <input type="hidden" value="<%=descTextWithNoQuotes%>" /></td>
                <td class="<%=td_class_name%>" id="file_<%=fileItem.getId()%>">
                <a title="<%=fileItem.getFileType()%>"
                href="Download?fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>&datasetId=<%=datasetContext.getDataset().getId()%>">
                        <%=fileItem.getDisplayFileName()%>
				</a>
                </td>
                <td class="<%=td_class_name%> updated" ><%= updatedBy %></td>
                <% if (hasEditAuth) { %>
                <td class="<%=td_class_name%>" id="button_<%=fileItem.getId()%>">
                  <div id="fileEditDiv_<%=fileItem.getId()%>" class="editButtons">
                    <a href="javascript:makeFileRowEditable(<%=fileItem.getId()%>)">
                    <img src="images/edit.gif" alt="Edit File" title="Edit File">
                    </a>

                    <a href="javascript:fileDeleteAreYouSure(<%=fileItem.getId()%>)">
                    <img src="images/delete.gif" alt="Delete File" title="Delete File">
                    </a>
                  </div>

                  <div class="fileSureDiv" id="fileSureDiv_<%=fileItem.getId()%>">
                    delete this file?&nbsp
                    <a href="javascript:fileDeleteAreYouSureDone(<%=fileItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="Delete?datasetId=<%=datasetContext.getDataset().getId() %>&fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>">yes</a>
                  </div>

                  <div class="titleSureDiv" id="titleSureDiv_<%=fileItem.getId()%>">
                    File Title is too long.  Truncate to 255 characters?&nbsp
                    <a href="javascript:javascript:cancelFileChanges(<%=fileItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="javascript:truncateTitleAndSave(<%=fileItem.getId()%>)">yes</a>
                  </div>

                  <div class="fileSaveDiv" id="fileSaveDiv_<%=fileItem.getId()%>">
                    <input type="button" value="Save" onclick="javascript:saveFileChanges(<%=fileItem.getId()%>)" />
                    <input type="button" value="Cancel" onclick="javascript:cancelFileChanges(<%=fileItem.getId()%>)" />
                  </div>
                </td>
                <% } %>

            </tr>
            <%
            } // end for loop
        } // else there are some files
        if (idx % 2 == 0) {
            td_class_name = td_class_name_even;
        } else {
            td_class_name = td_class_name_odd;
        }
        if (hasEditAuth) {
        %>
            <tr>
                <td class="<%=td_class_name%> num"><%=idx++%></td>
                <td class="<%=td_class_name%>"><textarea name="fileTitle" id="fileTitle" class="title" rows="4" cols="20"></textarea></td>
                <td class="<%=td_class_name%>"><textarea name="fileDescription" rows="4" cols="20"></textarea></td>
                <td class="<%=td_class_name%>"><input name="fileName" type="file"></td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>" id="addFileTd">
                </td>
                <!-- <input type="submit" id="fileAddButton" name="Submit" value="Add"/></td> -->
            </tr>
        <%
        } else if (fileList == null || fileList.size() == 0) {
        %>
            <tr>
                <td class="num"></td>
                <td class="<%=td_class_name%>">No files found.</td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>"></td>
            </tr>
        <%
        }
    %>
    </table>
    </FORM>

</table>
