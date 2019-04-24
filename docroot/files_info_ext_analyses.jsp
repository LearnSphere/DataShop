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
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>

<%@page import="org.apache.commons.collections.comparators.NullComparator"%>
<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%@page import="edu.cmu.pslc.datashop.dto.ExternalAnalysisFile"%>
<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ExternalAnalysisItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem.SortParameter"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillModelItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.kcmodel.KCModelHelper"%>
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
    String sortBy = (String) session.getAttribute(FilesInfoServlet.FILES_REQUEST_SORT_BY + "_" + FilesInfoServlet.FILES_CONTENT_EXT_ANALYSES_VALUE);

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
    String td_class_name_even = "even";
    String td_class_name = td_class_name_odd;

    FastDateFormat dateFormat = FastDateFormat.getInstance("MMM d, yyyy HH:mm");
%>

    <%
        KCModelHelper kcModelHelper = HelperFactory.DEFAULT.getKCModelHelper();

	List<SkillModelItem> kcModels = 
	           kcModelHelper.getModelList(datasetContext.getDataset(), datasetContext.getUser());
    %>

    <FORM name="embeddedFilesForm" id="embeddedFilesForm"
    	  action="Upload?datasetId=<%=datasetContext.getDataset().getId() %>" method="post"
	  encrypte="multipart/form-data" onsubmit="return validateFileTitle()">
    <input type="hidden" value="<%=datasetContext.getDataset().getId() %>" name="datasetId" />
    <input type="hidden" value="<%=sortBy %>" id="extAnalysesSortBy" />
    <% if (hasProjectEdit) { %>
      <div id="fileUploadButton">
        <p><a href="#" id="fileUploadLink" class="ui-state-default ui-corner-all" onclick="javascript:uploadExternalAnalysis(<%=datasetContext.getDataset().getId() %>)">
        <span class=""></span>Upload</a>
        </p>
      </div>
    <% } %>
    <table id="externalAnalysesInfo" class="dataset-box">
        <colgroup>
            <col style="width:25%" />
            <col style="width:20%" />
            <col style="width:20%" />
            <col style="width:10%" />
            <col style="width:10%" />
            <col style="width:10%" />
        <% if (hasProjectEdit) { %>
            <col style="width:5%" />
        <% } %>
        </colgroup>
        <caption>External Analyses</caption>
        <tr>
            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.TITLE_COLUMN%>')"><%=FilesInfoServlet.TITLE_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.TITLE_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.TITLE_COLUMN) )
	    %>" />
	    </th>

            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.FILE_NAME_COLUMN%>')"><%=FilesInfoServlet.FILE_NAME_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.FILE_NAME_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.FILE_NAME_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.KC_MODEL_COLUMN%>')"><%=FilesInfoServlet.KC_MODEL_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.KC_MODEL_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.KC_MODEL_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.STATISTICAL_MODEL_COLUMN%>')"><%=FilesInfoServlet.STATISTICAL_MODEL_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.STATISTICAL_MODEL_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.STATISTICAL_MODEL_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.UPLOADED_BY_COLUMN%>')"><%=FilesInfoServlet.UPLOADED_BY_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.UPLOADED_BY_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.UPLOADED_BY_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortExternalAnalyses('<%=FilesInfoServlet.DATE_COLUMN%>')"><%=FilesInfoServlet.DATE_COLUMN %></a>
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
        List<ExternalAnalysisFile> eaList = filesInfoHelper.getExternalAnalysisList(datasetContext.getDataset());
        int idx = 1;
        if (eaList != null && eaList.size() > 0) {
	   Comparator<ExternalAnalysisFile> eafComparator = ExternalAnalysisFile.getComparator(sortByParams);
	   Comparator<ExternalAnalysisFile> nullComparator = new NullComparator(eafComparator, false);
	   Collections.sort(eaList, nullComparator);

            for (Iterator iter = eaList.iterator(); iter.hasNext();) {
                ExternalAnalysisFile eaFile = (ExternalAnalysisFile)iter.next();
		ExternalAnalysisItem eaItem = eaFile.getExternalAnalysisItem();
		FileItem fileItem = eaFile.getFileItem();
		UserItem owner = eaFile.getOwner();
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

	    String dateString = dateFormat.format(fileItem.getAddedTime());
            String firstName =
                (owner.getFirstName() != null && owner.getFirstName().length() > 0)
                    ? owner.getFirstName() : "-";
            String lastName =
                    (owner.getLastName() != null && owner.getLastName().length() > 0)
                        ? Character.toUpperCase(owner.getLastName().charAt(0)) + owner.getLastName().substring(1)
                                : "-";

            String name = (firstName.equals("-") && lastName.equals("-")) ?
                    (String)owner.getId() : firstName + " " + lastName;

            String dateStr = "<span class=\"date\">" + dateString + "</span >";

	    String skillModelStr = eaItem.getSkillModelName();
	    if (skillModelStr == null)
	    	skillModelStr = "";
	    String statModelStr = eaItem.getStatisticalModel();
	    if (statModelStr == null)
	    	statModelStr = "";

	    boolean fileIsViewable = false;
	    String fileType = fileItem.getFileType();
	    if (fileType.startsWith("text")) fileIsViewable = true;

	        %>
            <tr>
                <td class="<%=td_class_name%>" id="title_<%=eaItem.getId()%>">
                <% if (fileIsViewable) { %>
		  <a href="javascript:displayExternalAnalysis(<%=eaItem.getId()%>)">
                    <span name="title" class="bTitle" id="s_title_<%=eaItem.getId()%>"><%=titleToDisplay%></span>
                  </a>
                <% } else {%>
                    <span name="title" class="bTitle" id="s_title_<%=eaItem.getId()%>"><%=titleToDisplay%></span>
                <% } %>
		  <input type="hidden" value="<%=titleTextWithNoQuotes%>" id="title_input_<%=eaItem.getId()%>" />
		  <%
		  if (title_truncated) {
		  %>
		     <div class="moreTitleDiv" id="moreTitleDiv_<%=eaItem.getId()%>">
		       <br/>
		       <a href="javascript:showFullTitle(<%=eaItem.getId()%>)">more</a>
		       <br/>
		     </div>
		     <div class="lessTitleDiv" id="lessTitleDiv_<%=eaItem.getId()%>">
		       <br/>
		       <a href="javascript:showTruncatedTitle(<%=eaItem.getId()%>)">less</a>
		       <br/>
		     </div>
		  <%
		  } else {
		  %>
		     <br/>
		  <%
		  }
		  %>
                  <span name="description" id="s_description_<%=eaItem.getId()%>"><%=descToDisplay%></span>
                  <input type="hidden" value="<%=descTextWithNoQuotes%>" id="desc_input_<%=eaItem.getId()%>" />
		  <input type="hidden" id="fileIsViewable_<%=eaItem.getId()%>" value="<%=fileIsViewable%>" />

		  <%
		  if (desc_truncated) {
		  %>
		     <div class="moreDescDiv" id="moreDescDiv_<%=eaItem.getId()%>">
		       <a href="javascript:showFullDescription(<%=eaItem.getId()%>)">more</a>
		     </div>
		     <div class="lessDescDiv" id="lessDescDiv_<%=eaItem.getId()%>">
		       <a href="javascript:showTruncatedDesc(<%=eaItem.getId()%>)">less</a>
		     </div>
		  <%
		  }
		  %>

		  <input type="hidden" id="title_full_<%=eaItem.getId()%>" value="<%=titleTextWithNoQuotes%>" />
		  <input type="hidden" id="title_trunc_<%=eaItem.getId()%>" value="<%=truncatedTitleText%>" />
		  <input type="hidden" id="title_truncated_<%=eaItem.getId()%>" value=<%=title_truncated%> />
		  <input type="hidden" id="desc_full_<%=eaItem.getId()%>" value="<%=descTextWithNoQuotes%>" />
		  <input type="hidden" id="desc_trunc_<%=eaItem.getId()%>" value="<%=truncatedDescText%>" />
		  <input type="hidden" id="desc_truncated_<%=eaItem.getId()%>" value=<%=desc_truncated%> />
		</td>
                <td class="<%=td_class_name%>" id="file_<%=eaItem.getId()%>">
                <a title="<%=fileItem.getFileType()%>"
                href="Download?fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>&datasetId=<%=datasetContext.getDataset().getId()%>"><%=fileItem.getDisplayFileName()%>
		</a>
                </td>
                <td class="<%=td_class_name%>" ><%= skillModelStr %></td>
                <td class="<%=td_class_name%>" ><%= statModelStr %></td>
                <td class="<%=td_class_name%> updated" ><%= name %></td>
                <td class="<%=td_class_name%>" ><%= dateStr %></td>
                <% if (hasProjectEdit) { %>
                <td class="<%=td_class_name%>" id="button_<%=eaItem.getId()%>">
                <% if (((String)eaItem.getOwner().getId()).equals(userStr) || hasProjectAdmin){ %>
                  <div id="extAnalysesEditDiv_<%=eaItem.getId()%>" class="editButtons">
                    <a href="javascript:makeExtAnalysesRowEditable(<%=eaItem.getId()%>)">
                    <img src="images/edit.gif" alt="Edit Analysis" title="Edit Analysis">
                    </a>

                    <a href="javascript:extAnalysesDeleteAreYouSure(<%=eaItem.getId()%>)">
                    <img src="images/delete.gif" alt="Delete Analysis" title="Delete Analysis">
                    </a>
                  </div>

                  <div class="extAnalysesSureDiv" id="extAnalysesSureDiv_<%=eaItem.getId()%>">
                    delete this file?&nbsp
                    <a href="javascript:extAnalysesDeleteAreYouSureDone(<%=eaItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="Delete?datasetId=<%=datasetContext.getDataset().getId() %>&fileName=<%=fileItem.getFileName()%>&externalAnalysisId=<%=eaItem.getId()%>">yes</a>
                  </div>

                  <div class="titleSureDiv" id="titleSureDiv_<%=eaItem.getId()%>">
                    External Analysis Title is too long.  Truncate to 255 characters?&nbsp
                    <a href="javascript:cancelExtAnalysesChanges(<%=eaItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="javascript:truncateEATitleAndSave(<%=eaItem.getId()%>)">yes</a>
                  </div>

                  <div class="descriptionSureDiv" id="descriptionSureDiv_<%=eaItem.getId()%>">
                    External Analysis Description is too long.  Truncate to 500 characters?&nbsp
                    <a href="javascript:cancelExtAnalysesChanges(<%=eaItem.getId()%>)">no</a>
                    &nbsp/&nbsp
                    <a href="javascript:truncateEADescAndSave(<%=eaItem.getId()%>)">yes</a>
                  </div>

                  <div class="extAnalysesSaveDiv" id="extAnalysesSaveDiv_<%=eaItem.getId()%>">
                    <input type="button" value="Save" onclick="javascript:saveExtAnalysesChanges(<%=eaItem.getId()%>)" />
                    <input type="button" value="Cancel" onclick="javascript:cancelExtAnalysesChanges(<%=eaItem.getId()%>)" />
                  </div>
			<% } %>
			</td>
                <% } %>

            </tr>
            <%
	    idx++;
            } // end for loop
        } else if (eaList == null || eaList.size() == 0) {
	    if (idx % 2 == 0) {
               td_class_name = td_class_name_even;
	    } else {
               td_class_name = td_class_name_odd;
            }
        %>
            <tr>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>" style="font-style:oblique">No files found.</td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <% if (hasProjectEdit) { %>
                <td class="<%=td_class_name%>">&nbsp;</td>
                <% } %>
            </tr>
        <%
        }
    %>
    </table>

    <div id="externalAnalysisOutput" class="externalAnalysisOutput">
        <table id=eaFileTitleTable class="eaFileTitleTable">
          <tr>
            <td class="eaBackLink">
              <a href="Files?datasetId=<%=datasetContext.getDataset().getId() %>">Back to External Analyses</a>
            </td>
          </tr>
          <tr>
            <td><span id="eaTitleSpan">"put the file title here"</span></td>
            <td class="eaDownload">
              <a id="eaDownloadLink" href="Download URL">Download</a>
            </td>
            <td></td>
          </tr>
        </table>
        <p id="eaText" class="eaText">"put the file content here"</p>
    </div>
    <div id="kcModelDivHidden_<%=datasetContext.getDataset().getId()%>" class="kcModelDiv">
    	 <select name="externalAnalysisSkillModel">
	    <option value="" selected="selected"></option>
	    <%
	    for (Iterator iter = kcModels.iterator(); iter.hasNext(); ) {
	    	SkillModelItem smItem = (SkillModelItem)iter.next();
		%>
		<option value=<%=smItem.getId()%>><%=smItem.getSkillModelName()%></option>
	    <%
	    }
	    %>
	 </select>
    </div>
    </FORM>

</table>
