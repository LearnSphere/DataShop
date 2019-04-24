<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 10833 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-03-24 13:40:24 -0400 (Mon, 24 Mar 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>

<%@page import="org.apache.commons.collections.comparators.NullComparator"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem.SortParameter"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper"%>

<%!
    static final Integer MAX_TITLE_LENGTH_FOR_DISPLAY = 100;
    static final Integer MAX_DESC_LENGTH_FOR_DISPLAY = 100;
%>

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
    FilesInfoHelper filesInfoHelper = HelperFactory.DEFAULT.getFilesInfoHelper();
    DatasetContext datasetContext  = (DatasetContext)session.getAttribute("datasetContext_"
    + request.getParameter("datasetId"));
    FilesInfoContext filesInfoContext = datasetContext.getFilesInfoContext();
    SortParameter[] sortByParams = (SortParameter[])filesInfoContext.getSortByParameters();
    String sortBy = (String) session.getAttribute(FilesInfoServlet.FILES_REQUEST_SORT_BY + "_" + FilesInfoServlet.FILES_CONTENT_FILES_VALUE);

    // default sorting...
    if (sortBy == null) sortBy = FilesInfoServlet.TITLE_COLUMN;

    ProjectItem projectItem = datasetContext.getDataset().getProject();
    Integer projectId = null;
    if (projectItem != null) {
        projectId = (Integer)projectItem.getId();
    }

    String userStr = (request.getRemoteUser() == null)
           ? (String)datasetContext.getUser().getId() : request.getRemoteUser();

    Boolean hasProjectAdmin = false;
    Boolean hasProjectEdit = false;
    String authorizationLevel = null;
    if (userStr != null) {
        ProjectPageHelper projPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();
        authorizationLevel = projPageHelper.getAuthLevel(userStr, projectId);
        if (authorizationLevel != null) {
            if (authorizationLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                hasProjectAdmin = true;
                hasProjectEdit = true;
            } else if (authorizationLevel.equals(AuthorizationItem.LEVEL_EDIT)) {
                hasProjectEdit = true;
            }
        }
    }

    boolean isDatashopAdmin =
            HelperFactory.DEFAULT.getProjectHelper().isDataShopAdmin(userStr, datasetContext);
    
    if (isDatashopAdmin) {
        hasProjectEdit = true;
        hasProjectAdmin = true;
    }

    String td_class_name_odd = "cell";
    String td_class_name_even = "cell even";
    String td_class_name = td_class_name_odd;

    FastDateFormat dateFormat = FastDateFormat.getInstance("MMM d, yyyy HH:mm");
%>

    <FORM name="embeddedFilesForm" id="embeddedFilesForm"
    	  action="Upload?datasetId=<%=datasetContext.getDataset().getId() %>" method="post"
	  enctype="multipart/form-data" onsubmit="return validateFileTitle()">
    <input type="hidden" value="<%=datasetContext.getDataset().getId() %>" name="datasetId" />
    <input type="hidden" value="<%=sortBy %>" id="filesSortBy" />
    <% if (hasProjectEdit) { %>
      <div id="fileUploadButton">
        <p><a href="#" id="fileUploadLink" class="ui-state-default ui-corner-all" onclick="javascript:uploadFile(<%=datasetContext.getDataset().getId() %>)">
        <span class=""></span>Upload</a>
        </p>
      </div>
    <% } %>
    <table id="fileInfo" class="dataset-box">
        <colgroup>
            <col style="width:35%" />
            <col style="width:25%" />
            <col style="width:20%" />
            <col style="width:10%" />
        <% if (hasProjectEdit) { %>
            <col style="width:10%" />
        <% } %>
        </colgroup>
        <caption>Files</caption>
        <tr>
            <th><a href="javascript:sortFiles('<%=FilesInfoServlet.TITLE_COLUMN%>')"><%=FilesInfoServlet.TITLE_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.TITLE_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.TITLE_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortFiles('<%=FilesInfoServlet.FILE_NAME_COLUMN%>')"><%=FilesInfoServlet.FILE_NAME_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.FILE_NAME_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.FILE_NAME_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortFiles('<%=FilesInfoServlet.UPLOADED_BY_COLUMN%>')"><%=FilesInfoServlet.UPLOADED_BY_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.UPLOADED_BY_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.UPLOADED_BY_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortFiles('<%=FilesInfoServlet.DATE_COLUMN%>')"><%=FilesInfoServlet.DATE_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.DATE_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.DATE_COLUMN) )
	    %>" />
	    </th>
            <% if (hasProjectEdit) { %>
            <th></th>
            <% } %>
        </tr>
        <%
        List <FileItem> fileList = filesInfoHelper.getFileList(datasetContext.getDataset());
        int idx = 1;
        if (fileList != null && fileList.size() > 0) {
	   Comparator<FileItem> fiComparator = FileItem.getComparator(sortByParams);
	   Comparator<FileItem> nullComparator = new NullComparator(fiComparator, false);
	   Collections.sort(fileList, nullComparator);

            for (Iterator iter = fileList.iterator(); iter.hasNext();) {
                FileItem fileItem = (FileItem)iter.next();
	        if (idx % 2 == 0) {
                    td_class_name = td_class_name_even;
	        } else {
                    td_class_name = td_class_name_odd;
                }
		boolean title_truncated = false;

	        // In the view, should use <br /> to display line breaks in title, description
	        String titleText = fileItem.getTitle();
	        String titleTextWithBr = cleanInputText(titleText);
	        String titleTextWithNoQuotes = cleanInputTextForEdit(titleText);
		String truncatedTitleText = "";
		if (titleText.length() > MAX_TITLE_LENGTH_FOR_DISPLAY) {
		   truncatedTitleText = titleTextWithBr.substring(0, MAX_TITLE_LENGTH_FOR_DISPLAY);
		   truncatedTitleText += "...";
		   title_truncated = true;
		}

		String titleToDisplay = title_truncated ? truncatedTitleText : titleTextWithBr;

		boolean desc_truncated = false;

	        String descText = fileItem.getDescription();
	        String descTextWithBr = cleanInputText(descText);
	        String descTextWithNoQuotes = cleanInputTextForEdit(descText);
		String truncatedDescText = "";
		if (descText != null && descText.length() > MAX_DESC_LENGTH_FOR_DISPLAY) {
		   truncatedDescText = descTextWithBr.substring(0, MAX_DESC_LENGTH_FOR_DISPLAY);
		   truncatedDescText += "...";
		   desc_truncated = true;
		}

		String descToDisplay = desc_truncated ? truncatedDescText : descTextWithBr;

            	UserItem owner = fileItem.getOwner();

            String dateString = dateFormat.format(fileItem.getAddedTime());
            String firstName =
                (owner.getFirstName() != null && owner.getFirstName().length() > 0)
                    ? owner.getFirstName() : "-";
            String lastName =
                    (owner.getLastName() != null && owner.getLastName().length() > 0)
                        ? Character.toUpperCase(owner.getLastName().charAt(0)) + owner.getLastName().substring(1)
                                : "-";

            String nameStr = (firstName.equals("-") && lastName.equals("-")) ?
                    (String)owner.getId() : firstName + " " + lastName;

            String dateStr = "<span class=\"date\">" + dateString + "</span >";

	        %>
            <tr>
                <td class="<%=td_class_name%>" id="title_<%=fileItem.getId()%>">
                  <span name="title" class="bTitle" id="s_title_<%=fileItem.getId()%>"><%=titleToDisplay%></span>
		  <input id="title_input_<%=fileItem.getId()%>" type="hidden" value="<%=titleTextWithNoQuotes%>"/>
		  <%
		  if (title_truncated) {
		  %>
		     <div class="moreTitleDiv" id="moreTitleDiv_<%=fileItem.getId()%>">
		       <a href="javascript:showFullTitle(<%=fileItem.getId()%>)">more</a>
		       <br/>
		     </div>
		     <div class="lessTitleDiv" id="lessTitleDiv_<%=fileItem.getId()%>">
		       <a href="javascript:showTruncatedTitle(<%=fileItem.getId()%>)">less</a>
		       <br/>
		     </div>
		  <%
		  } else {
		  %>
		     <br/>
		  <%
		  }
		  %>
                  <span name="description" id="s_description_<%=fileItem.getId()%>"><%=descToDisplay%></span>
               	  <input id="desc_input_<%=fileItem.getId()%>" type="hidden" value="<%=descTextWithNoQuotes%>"/>
		  <%
		  if (desc_truncated) {
		  %>
		     <div class="moreDescDiv" id="moreDescDiv_<%=fileItem.getId()%>">
		       <a href="javascript:showFullDescription(<%=fileItem.getId()%>)">more</a>
		     </div>
		     <div class="lessDescDiv" id="lessDescDiv_<%=fileItem.getId()%>">
		       <a href="javascript:showTruncatedDesc(<%=fileItem.getId()%>)">less</a>
		     </div>
		  <%
		  }
		  %>

		  <input type="hidden" id="title_full_<%=fileItem.getId()%>" value="<%=titleTextWithNoQuotes%>" />
		  <input type="hidden" id="title_trunc_<%=fileItem.getId()%>" value="<%=truncatedTitleText%>" />
		  <input type="hidden" id="title_truncated_<%=fileItem.getId()%>" value=<%=title_truncated%> />
		  <input type="hidden" id="desc_full_<%=fileItem.getId()%>" value="<%=descTextWithNoQuotes%>" />
		  <input type="hidden" id="desc_trunc_<%=fileItem.getId()%>" value="<%=truncatedDescText%>" />
		  <input type="hidden" id="desc_truncated_<%=fileItem.getId()%>" value=<%=desc_truncated%> />
		</td>
                <td class="<%=td_class_name%>" id="file_<%=fileItem.getId()%>">
                <a title="<%=fileItem.getFileType()%>"
                href="Download?fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>&datasetId=<%=datasetContext.getDataset().getId()%>">
                        <%=fileItem.getDisplayFileName()%>
				</a>
                </td>
                <td class="<%=td_class_name%> updated" ><%= nameStr %></td>
                <td class="<%=td_class_name%>" ><%= dateStr %></td>
                <% if (hasProjectEdit){ %>
                <td class="<%=td_class_name%>" id="button_<%=fileItem.getId()%>">
                <% if (((String)fileItem.getOwner().getId()).equals(userStr) || hasProjectAdmin){ %>
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

                  <div class="descriptionSureDiv" id="descriptionSureDiv_<%=fileItem.getId()%>">
                    File Description is too long.  Truncate to 500 characters?&nbsp
                    <a href="javascript:cancelFileChanges(<%=fileItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="javascript:truncateDescriptionAndSave(<%=fileItem.getId()%>)">yes</a>
                  </div>

                  <div class="fileSaveDiv" id="fileSaveDiv_<%=fileItem.getId()%>">
                    <input type="button" value="Save" onclick="javascript:saveFileChanges(<%=fileItem.getId()%>)" />
                    <input type="button" value="Cancel" onclick="javascript:cancelFileChanges(<%=fileItem.getId()%>)" />
                  </div>
                  <% } %>
                </td>
                <% } %>

            </tr>
            <%
	    idx++;
            } // end for loop
        } else if (fileList == null || fileList.size() == 0) {
	    if (idx % 2 == 0) {
               td_class_name = td_class_name_even;
	    } else {
               td_class_name = td_class_name_odd;
            }
        %>
            <tr>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>" style="font-style:oblique">No files found.</td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>"></td>
                <% if (hasProjectEdit) { %>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <% } %>
            </tr>
        <%
        }
    %>
    </table>
    </FORM>

</table>
