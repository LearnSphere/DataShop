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

<%@page import="edu.cmu.pslc.datashop.dto.PaperFile"%>
<%@page import="edu.cmu.pslc.datashop.item.AuthorizationItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem.SortParameter"%>
<%@page import="edu.cmu.pslc.datashop.item.PaperItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper"%>

<%@ include file="/header_variables.jspf" %>

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
    String sortBy = (String) session.getAttribute(FilesInfoServlet.FILES_REQUEST_SORT_BY + "_" + FilesInfoServlet.FILES_CONTENT_PAPERS_VALUE);

    // default sorting...
    if (sortBy == null) sortBy = FilesInfoServlet.PREF_CITATION_COLUMN;

    ProjectItem projectItem = datasetContext.getDataset().getProject();
    Integer projectId = null;
    if (projectItem != null) {
        projectId = (Integer)projectItem.getId();
    }
    
    String userStr = (request.getRemoteUser() == null)
           ? (String)datasetContext.getUser().getId() : request.getRemoteUser();
    boolean isLoggedIn = (userStr == null || userStr.equals("%")) ? false : true;

    Boolean hasProjectAdmin = false;
    Boolean hasProjectEdit = false;
    String authorizationLevel = null;
    if (isLoggedIn) {
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

    String hasEditClass = "";
    if (hasProjectEdit) {
        hasEditClass = "has-edit";
    } 
    %>

    <% if (hasProjectAdmin) { %>
        <div id="public-papers-info" class="infoMessage">
            <img src="images/globe.png" />
            <p><b>This page is public on the web.</b></p>
        </div>
    <% } %>

    <FORM name="embeddedPapersForm" action="Upload?datasetId=<%=datasetContext.getDataset().getId() %>"
    	  method="post" enctype="multipart/form-data" onsubmit="">
    <input type="hidden" name="datasetId" value="<%=datasetContext.getDataset().getId() %>" />
    <input type="hidden" value="<%=sortBy %>" id="papersSortBy" />
    <% if (hasProjectEdit) { %>
      <div id="fileUploadButton">
        <p><a href="#" id="fileUploadLink" class="ui-state-default ui-corner-all" onclick="javascript:uploadPaper(<%=datasetContext.getDataset().getId() %>)">
        <span class=""></span>Upload</a>
        </p>
      </div>
    <% } %>
    <table id="paperInfo" class="dataset-box <%=hasEditClass%>">
        <colgroup>
            <col style="width:5%" />
            <col style="width:30%" />
            <col style="width:20%" />
            <col style="width:10%" />
            <col style="width:10%" />
        <% if (hasProjectEdit) { %>
            <col style="width:5%" />
        <% } %>
        </colgroup>
        <caption>Papers</caption>
        <tr>
            <th><a href="javascript:sortPapers('<%=FilesInfoServlet.PREF_CITATION_COLUMN%>')"><%=FilesInfoServlet.PREF_CITATION_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.PREF_CITATION_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.PREF_CITATION_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortPapers('<%=FilesInfoServlet.CITATION_COLUMN%>')"><%=FilesInfoServlet.CITATION_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.CITATION_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.CITATION_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortPapers('<%=FilesInfoServlet.FILE_NAME_COLUMN%>')"><%=FilesInfoServlet.FILE_NAME_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.FILE_NAME_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.FILE_NAME_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortPapers('<%=FilesInfoServlet.PAPER_UPLOADED_BY_COLUMN%>')"><%=FilesInfoServlet.PAPER_UPLOADED_BY_COLUMN %></a>
            <img src="<%=
	    	FilesInfoServlet.showSortOrder(sortBy,
		FilesInfoServlet.PAPER_UPLOADED_BY_COLUMN,
                filesInfoContext.getSortOrder(FilesInfoServlet.PAPER_UPLOADED_BY_COLUMN) )
	    %>" />
	    </th>
            <th><a href="javascript:sortPapers('<%=FilesInfoServlet.DATE_COLUMN%>')"><%=FilesInfoServlet.DATE_COLUMN %></a>
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

        PaperItem preferredPaper = filesInfoHelper.getPreferredPaper(datasetContext.getDataset());
        List <PaperFile> paperList =
            filesInfoHelper.getPaperList(datasetContext.getDataset());
        int idx = 1;
        if (paperList != null && paperList.size() > 0) {
	   Comparator<PaperFile> pfComparator = PaperFile.getComparator(preferredPaper, sortByParams);
	   Comparator<PaperFile> nullComparator = new NullComparator(pfComparator, false);
	   Collections.sort(paperList, nullComparator);

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
		if (abstractText == null) abstractText = "";
    	        String abstractTextWithNoQuotes = cleanInputTextForEdit(abstractText);
    	        String abstractTextWithBr = cleanInputText(abstractText);
    	        boolean abstractPresent = abstractText.length() > 0 ? true : false;

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
    	%>
                <tr>
                    <td class="<%=td_class_name%> prefCit" id="preferred_citation_<%=paperItem.getId()%>">
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
		      <span name="citation" id="s_citation_<%=paperItem.getId()%>"><%=citationTextWithBr%></span>
                      <input type="hidden" value="<%=citationTextWithNoQuotes%>" />
		      <br/>
		      <% if (abstractPresent) { %>
		      <div id="citationOnlyDiv_<%=paperItem.getId()%>" class="<%=td_class_name%>">
			<a title="s_abstract_<%=paperItem.getId()%>"
			 href="javascript:showAbstract(<%=paperItem.getId()%>)">show abstract
    			</a>
		      </div>
		      <% } %>

		      <div id="abstractDiv_<%=paperItem.getId()%>" class="showAbstractDiv">
			<br/>
			<b>Abstract</b>
			<br/>
                    	<span name="abstract" id="s_abstract_<%=paperItem.getId()%>"><%=abstractTextWithBr%></span>
			<br/>
			<a title="s_abstract_<%=paperItem.getId()%>"
			 href="javascript:hideAbstract(<%=paperItem.getId()%>)">hide abstract
    			</a>
		      </div>
                      <input type="hidden" value="<%=abstractTextWithNoQuotes%>" />
                    </td>

                    <td class="<%=td_class_name%>" id="paper_<%=paperItem.getId()%>">
                    <a title="<%=fileItem.getFileType()%>"
                    href="DownloadPaper?fileName=<%=fileItem.getFileName()%>&fileId=<%=fileItem.getId()%>&datasetId=<%=datasetContext.getDataset().getId()%>">
                        <%=fileItem.getDisplayFileName()%>
    				</a>
                    </td>
                    <td class="<%=td_class_name%> updated" ><%= name %></td>
                    <td class="<%=td_class_name%>" ><%= dateStr %></td>
                    <% if (hasProjectEdit) { %>
                    <td class="<%=td_class_name%>" id="button_<%=paperItem.getId()%>">
                    <% if (((String)paperItem.getOwner().getId()).equals(userStr)
                           || hasProjectAdmin){ %>
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
                      <% } %>
                    </td>
                    <% } %>

                </tr>
                <%
	        idx++;
            } // end for loop
        } else if (paperList == null || paperList.size() == 0) {
	    if (idx % 2 == 0) {
               td_class_name = td_class_name_even;
	    } else {
               td_class_name = td_class_name_odd;
            }
        %>
            <tr>
                <td class="prefCit"></td>
                <td class="<%=td_class_name%>"></td>
                <td class="<%=td_class_name%>" style="font-style:oblique">No papers found.</td>
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

