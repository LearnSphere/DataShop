<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 10667 $
// Last modified by: $Author: alida $
// Last modified on: $Date: 2014-02-27 14:22:32 -0500 (Thu, 27 Feb 2014) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbFileDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbProjectDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.IrbServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectReviewDto"%>
<%@page import="edu.cmu.pslc.datashop.servlet.irb.ProjectShareabilityHistoryDto"%>
<%@page import="edu.cmu.pslc.datashop.item.IrbItem"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>

<!-- project-page-specific header stuff -->
<%@ include file="/jsp_project/project_header.jspf" %>

<%
        IrbContext irbContext = IrbContext.getContext(request);
        String addIRBSearchBy = irbContext.getAddIRBSearchBy();

        IrbProjectDto irbProjectDto = (IrbProjectDto)request.getAttribute(IrbServlet.IRB_PROJECT_ATTR + projectId);
        ProjectReviewDto projectReviewDto = irbProjectDto.getProjectReviewDto();
        List<IrbDto> irbList = irbProjectDto.getIrbList();
        List<ProjectShareabilityHistoryDto> pshList = irbProjectDto.getProjectShareabilityHistoryList();

        String message = irbProjectDto.getMessage();
        String messageLevel = irbProjectDto.getMessageLevel();

        // Special handling for form submissions, i.e., IRB Add. If the 'message' and 'messageLevel'
        // are NULL on the request, check the session.
        if ((message == null) && (messageLevel == null)) {
           IrbProjectDto tmpIrbProjectDto = 
              (IrbProjectDto)session.getAttribute(IrbServlet.IRB_PROJECT_ATTR + projectId);
           if (tmpIrbProjectDto != null) {
              message = tmpIrbProjectDto.getMessage();
              messageLevel = tmpIrbProjectDto.getMessageLevel();
           }
           // Now that we've read the value, clear it.
           session.setAttribute(IrbServlet.IRB_PROJECT_ATTR + projectId, null);
        }

        String rmNotes = cleanInputText(projectReviewDto.getResearchManagersNotes());

        String rmNotesClass = "";
        if (rmNotes.trim().length() == 0) {
           rmNotes = "No notes have been entered by the research manager.";
           rmNotesClass = "rm-notes-empty";
        }
        String rmNotesWithoutQuotes = projectReviewDto.getResearchManagersNotes().replaceAll("\"","&quot;");

        String removeIrbLinkClass = "remove-irb-link";
        
        boolean needsAttnBool = projectReviewDto.getNeedsAttention();
        String needsAttnStr = "No";
        if (needsAttnBool) {
            needsAttnStr = "Yes";
        }

        String dcType = projectReviewDto.getDataCollectionType();
        String shareStatus = projectReviewDto.getShareabilityStatus();
        boolean hasIRBs = (irbList.size() != 0);
        boolean hasFiles = false;

        for (IrbDto dto : irbList) {
            List<IrbFileDto> fileList = dto.getFileList();
            if (fileList.size() > 0) {
               hasFiles = true;
            }
            if (hasFiles) { break; }
        }

        int displayNotice = 0;  // off
        // Five conditions for displaying notice to Project Admin.
        if (dcType.equals("not_specified")
            && !(shareStatus.equals("shareable") 
                    || shareStatus.equals("shareable_not_public")
                    || shareStatus.equals("not_shareable"))) {
            displayNotice = 1;
        }
        if (dcType.equals("not_human_subject") && shareStatus.equals("not_submitted")) {
            displayNotice = 2;
        }
        if (dcType.equals("study_data_consent_not_req") && shareStatus.equals("not_submitted") && !hasIRBs) {
            displayNotice = 3;
        } else if (dcType.equals("study_data_consent_req") && shareStatus.equals("not_submitted") && !hasIRBs) {
            displayNotice = 4;
        } else if (dcType.equals("study_data_consent_not_req") && shareStatus.equals("not_submitted") && !hasFiles) {
            displayNotice = 5;
        } else if (dcType.equals("study_data_consent_req") && shareStatus.equals("not_submitted") && !hasFiles) {
            displayNotice = 6;
        } else if (dcType.startsWith("study_data") && shareStatus.equals("not_submitted") && hasFiles) {
            displayNotice = 7;
        }

        //study_data_consent_not_req or study_data_consent_req

        // Determine appropriate div width for the irb-info-row divs... content-specific.
        String dcTypeClassStr = "irb-info-width-490";
        String dcTypeClassStr2 = "irb-info-width-480";
        if (dcType.equals("not_specified")) {
           dcTypeClassStr = "irb-info-width-125";
           dcTypeClassStr2 = "irb-info-width-95";
        }
        String subjToClassStr = "irb-info-width-270";
        if (projectReviewDto.getSubjectToDsIrb().equals("not_specified")) {
           subjToClassStr = "irb-info-width-125";
        }
        String shareClassStr = "irb-info-width-125";
        if (shareStatus.equals("waiting_for_researcher")
            || shareStatus.equals("submitted_for_review")) {
           shareClassStr = "irb-info-width-170";
        }

        // Determine if the 'Edit IRB' dialog was open before redirect brought us here...
        Integer irbIdOpenForEdit = (Integer)session.getAttribute(IrbServlet.EDIT_IRB_ID_ATTR);
        if (irbIdOpenForEdit == null) { irbIdOpenForEdit = 0; }

        // Now that we've read the value, clear it.
        session.setAttribute(IrbServlet.EDIT_IRB_ID_ATTR, null);
%>

        <%
        if (message != null && messageLevel != null ) {
           if (messageLevel.compareTo(IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS) == 0) {
        %>
            <script type="text/javascript">successPopup("<%=message%>");</script>
        <%
           } else if (messageLevel.compareTo(IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR) == 0) {
        %>
            <script type="text/javascript">errorPopup("<%=message%>");</script>
        <%
           } else {
        %>
            <script type="text/javascript">messagePopup("<%=message%>", "WARNING");</script>
        <% }
        }
        %>

        <div id="irb-info-div">
             <div class="irb-info-row">
                  <div class="label">Data Collection Type</div>
                  <div id="irbInfoDCDiv" class="<%=dcTypeClassStr%>">
                       <div id="irbInfoDCSpan" class="<%=dcTypeClassStr2%>"><%=projectReviewDto.getDataCollectionTypeString()%></div>
                  <% if (adminUserFlag || hasRmRole || hasProjectAdmin) { %>
                     <div class="irb-edit-link"><a id="irbEditDCLink" href="javascript:showEditDCDiv(<%=projectId%>)">edit</a></div>
                  <% } %>
                  </div>
                  <div id="irbInfoEditDCDiv">
                       <%
                           String notSpecifiedStr = "";
                           String notHumanStr = "";
                           String consentObtStr = "";
                           String consentReqStr = "";
                           if (projectReviewDto.getDataCollectionType().equals("not_specified")) {
                              notSpecifiedStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getDataCollectionType().equals("not_human_subject")) {
                              notHumanStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getDataCollectionType().equals("study_data_consent_not_req")) {
                              consentObtStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getDataCollectionType().equals("study_data_consent_req")) {
                              consentReqStr = "checked=\"checked\"";
                           }
                       %>
                       <table id="irbInfoEditDCTable">
                       <tr>
                       <td class="irb-info-edit-dc-td">
                           <input type="radio" id="dcTypeField1" name="dcTypeField" value="not_specified"
                           <%=notSpecifiedStr%>>
                       </td>
                       <td>
                           <label for="dcTypeField1"><%=ProjectReviewDto.DATA_COLLECTION_NOT_SPECIFIED%>
                           </label><br>
                       </td>
                       </tr>
                       <tr>
                       <td class="irb-info-edit-dc-td">
                           <input type="radio" id="dcTypeField2" name="dcTypeField" value="not_human_subject" <%=notHumanStr%>>
                       </td>
                       <td>
                           <label for="dcTypeField2"><%=ProjectReviewDto.DATA_COLLECTION_NOT_HUMAN_SUBJECT%>
                           </label><br>
                       </td>
                       </tr>
                       <tr>
                       <td class="irb-info-edit-dc-td">
                           <input type="radio" id="dcTypeField3" name="dcTypeField" value="study_data_consent_not_req"
                           <%=consentObtStr%>>
                       </td>
                       <td>
                           <label for="dcTypeField3"><%=ProjectReviewDto.DATA_COLLECTION_CONSENT_NOT_REQD%>
                           </label><br>
                       </td>
                       </tr>
                       <tr>
                       <td class="irb-info-edit-dc-td">
                           <input type="radio" id="dcTypeField4" name="dcTypeField" value="study_data_consent_req"
                           <%=consentReqStr%>>
                       </td>
                       <td>
                           <label for="dcTypeField4"><%=ProjectReviewDto.DATA_COLLECTION_CONSENT_REQUIRED%>
                           </label><br>
                       </td>
                       </tr>
                       </table>
                       <input type="button" id="dcTypeSaveButton" value="Save" onclick="javascript:saveDCTypeChange(<%=projectId%>)" />
                       <input type="button" id="dcTypeCancelButton" value="Cancel" onclick="javascript:cancelDCTypeChange()" />
                       <input type="hidden" id="dcTypeHiddenField" value="<%=projectReviewDto.getDataCollectionType()%>" />
                  </div>

             </div> <!-- irb-info-row -->
             <div class="irb-info-row clear">
                  <div class="label">Subject to 2012 DataShop IRB</div>
                  <div id="irbInfoSubjToDSDiv" class="<%=subjToClassStr%>">
                       <span id="irbInfoSubjToSpan"><%=projectReviewDto.getSubjectToDsIrbString()%></span>
                  <% if (adminUserFlag || hasRmRole) { %>
                     <div class="irb-edit-link"><a id="irbEditSubjToDSLink" href="javascript:showEditSubjToDiv()">edit</a></div>
                  <% } %>
                  </div>
                  <div id="irbInfoEditSubjToDiv">
                       <%
                           notSpecifiedStr = "";
                           String noStr = "";
                           String yesStr = "";
                           if (projectReviewDto.getSubjectToDsIrb().equals("not_specified")) {
                              notSpecifiedStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getSubjectToDsIrb().equals("no")) {
                              noStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getSubjectToDsIrb().equals("yes")) {
                              yesStr = "checked=\"checked\"";
                           }
                       %>
                       <input type="radio" id="subjToField1" name="subjToField" value="not_specified"
                              <%=notSpecifiedStr%>><label for="subjToField1"><%=ProjectReviewDto.SUBJECT_TO_DS_IRB_NOT_SPECIFIED%></label><br>
                       <input type="radio" id="subjToField2" name="subjToField" value="no"
                              <%=noStr%>><label for="subjToField2"><%=ProjectReviewDto.SUBJECT_TO_DS_IRB_NO%></label><br>
                       <input type="radio" id="subjToField3" name="subjToField" value="yes"
                              <%=yesStr%>><label for="subjToField3"><%=ProjectReviewDto.SUBJECT_TO_DS_IRB_YES%></label><br>
                       <input type="button" id="irbSubjToSaveButton" name="irbSubjToSaveButton" value="Save"
                              onclick="javascript:saveSubjToChange(<%=projectId%>)" enabled/>
                       <input type="button" id="irbSubjToCancelButton" value="Cancel" onclick="javascript:cancelSubjToChange()" enabled/>
                       <input type="hidden" id="subjToHiddenField" value="<%=projectReviewDto.getSubjectToDsIrb()%>" />
                  </div>
             </div> <!-- irb-info-row -->

             <div class="irb-info-row clear">
                  <div class="label">Shareability Review Status</div>
                  <div id="irbInfoShareStatusDiv" class="<%=shareClassStr%>">
                       <span id="irbInfoShareStatusSpan"><%=projectReviewDto.getShareabilityStatusString()%></span>
                  <% if (adminUserFlag || hasRmRole) { %>
                     <div class="irb-edit-link"><a id="irbEditShareStatusLink" href="javascript:showEditShareStatusDiv()">edit</a></div>
                  <% } %>
                  </div>
                  <div id="irbInfoEditShareStatusDiv">
                       <%
                           String disabledStr = "";
                           String notSubmittedStr = "";
                           String waitingStr = "";
                           String submittedStr = "";
                           String shareableStr = "";
                           String shareableNotPublicStr = "";
                           String notShareableStr = "";
                           if (projectReviewDto.getShareabilityStatus().equals("not_submitted")) {
                              notSubmittedStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getShareabilityStatus().equals("waiting_for_researcher")) {
                              waitingStr = "checked=\"checked\"";
                           } else if (projectReviewDto.getShareabilityStatus().equals("submitted_for_review")) {
                              submittedStr = "checked=\"checked\"";
                              disabledStr = "disabled";
                           } else if (projectReviewDto.getShareabilityStatus().equals("shareable")) {
                              shareableStr = "checked=\"checked\"";
                              disabledStr = "disabled";
                           } else if (projectReviewDto.getShareabilityStatus().equals("shareable_not_public")) {
                               shareableNotPublicStr = "checked=\"checked\"";
                              disabledStr = "disabled";
                           } else if (projectReviewDto.getShareabilityStatus().equals("not_shareable")) {
                              notShareableStr = "checked=\"checked\"";
                           }
                       %>
                       <input type="radio" id="shareStatusField1" name="shareStatusField" value="not_submitted"
                              <%=notSubmittedStr%>><label for="shareStatusField1"><%=ProjectItem.SHAREABLE_STATUS_STR_NOT_SUBMITTED%></label><br>
                       <input type="radio" id="shareStatusField2" name="shareStatusField" value="waiting_for_researcher"
                              <%=waitingStr%>><label for="shareStatusField2"><%=ProjectItem.SHAREABLE_STATUS_STR_WAITING%></label><br>
                       <input type="radio" id="shareStatusField3" name="shareStatusField" value="submitted_for_review"
                              <%=submittedStr%>><label for="shareStatusField3"><%=ProjectItem.SHAREABLE_STATUS_STR_SUBMITTED%></label><br>
                       <input type="radio" id="shareStatusField4" name="shareStatusField" value="shareable"
                              <%=shareableStr%>><label for="shareStatusField4"><%=ProjectItem.SHAREABLE_STATUS_STR_SHAREABLE%></label><br>
                       <input type="radio" id="shareStatusField5" name="shareStatusField" value="shareable_not_public"
                              <%=shareableNotPublicStr%>><label for="shareStatusField5"><%=ProjectItem.SHAREABLE_STATUS_STR_SHAREABLE_NOT_PUBLIC%> (e.g. OLI projects)</label><br>
                       <input type="radio" id="shareStatusField6" name="shareStatusField" value="not_shareable"
                              <%=notShareableStr%>><label for="shareStatusField6"><%=ProjectItem.SHAREABLE_STATUS_STR_NOT_SHAREABLE%></label><br>
                       <input type="button" id="shareStatusSaveButton" value="Save"
                              onclick="javascript:saveShareStatusChange(<%=projectId%>)" />
                       <input type="button" id="shareStatusCancelButton" value="Cancel" onclick="javascript:cancelShareStatusChange()" />
                       <input type="hidden" id="shareStatusHiddenField" value="<%=projectReviewDto.getShareabilityStatus()%>" />
                  </div>
             </div> <!-- irb-info-row -->

             <!-- Needs Attention -->
             <% if (adminUserFlag || hasRmRole) { %>
             <div class="irb-info-row clear">
                  <div class="label">Needs Attention</div>
                  <div id="irbInfoNeedsAttnDiv" class="<%=shareClassStr%>">
                       <span id="irbInfoNeedsAttnSpan"><%=needsAttnStr%></span>
                     <div class="irb-edit-link"><a id="irbEditNeedsAttnLink" href="javascript:showEditNeedsAttnDiv()">edit</a></div>
                  </div>
                  <div id="irbInfoEditNeedsAttnDiv">
                       <%
                           String yesCheckedStr = "";
                           String noCheckedStr = "";
                           if (needsAttnBool) {
                               yesCheckedStr = "checked=\"checked\"";
                           } else {
                               noCheckedStr = "checked=\"checked\"";
                           }
                       %>
                       <input type="radio" id="needsAttnField1" name="needsAttnField" value="Yes"
                              <%=yesCheckedStr%>><label for="needsAttnField1">Yes</label><br>
                       <input type="radio" id="needsAttnField2" name="needsAttnField" value="No"
                              <%=noCheckedStr%>><label for="needsAttnField2">No</label><br>
                       <input type="button" id="needsAttnSaveButton" value="Save"
                              onclick="javascript:saveNeedsAttnChange(<%=projectId%>)" />
                       <input type="button" id="needsAttnCancelButton" value="Cancel" onclick="javascript:cancelNeedsAttnChange()" />
                       <input type="hidden" id="needsAttnHiddenField" value="<%=needsAttnStr%>" />
                  </div>
             </div> <!-- irb-info-row -->
             <% } %>

             <% if (hasProjectAdmin) { %>
                <div id="submit-and-email-div">
                     <div id="submitProjectForReview">
                          <input type="button" id="submitForReviewButton" value="Submit project for review"
                                 onclick="javascript:submitProjectForReview(<%=projectId%>)" <%=disabledStr%>/>
                     </div> <!-- submitProjectForReview -->
                     <div id="emailRM">
                          <a id="emailRMLink" href="mailto:<%=rmEmail%>">Email the research manager</a>
                     </div> <!-- emailRM -->
                </div> <!-- submit-and-email-div -->
             <% } %>

             <% if (hasProjectAdmin && (displayNotice > 0)) { %>
                <div id="projectAdminNoticeDiv" class="information-box">
                     <% if (displayNotice == 1) { %>
                     <p id="projectAdminNotice">Please specify a data collection type. These categories are 
                     described in more detail on <a href="help?page=irb">our help page</a>.
                     </p>
                     <% } %>
                     <% if (displayNotice == 2) { %>
                     <p id="projectAdminNotice">You've identified the data in this project as "not human subjects data".
                     If you wish to be able to share your project outside your immediate research team, click "Submit project 
                     for review" to start our review process. See <a href="help?page=irb">our help page</a> for more information 
                     about this process. If you do not intend to share your data through DataShop, you don't need to have it reviewed.
                     </p>
                     <% } %>
                     <% if (displayNotice == 3) { %>
                     <p id="projectAdminNotice">As you've identified the data in this project as containing "study data", you will need 
                     to upload the associated IRB approval letter. In addition, if you wish to be able to share this project outside 
                     of your immediate research team, you will need to click "Submit project for review". See <a href="help?page=irb">our 
                     help page</a> for more information about this process.
                     </p>
                     <% } %>
                     <% if (displayNotice == 4) { %>
                     <p id="projectAdminNotice">As you've identified the data in this project as containing "study data", you will need 
                     to upload the associated IRB approval letter. In addition, if you wish to be able to share this project outside 
                     of your immediate research team, you will need to upload your study consent form and then submit your project for 
                     review. See <a href="help?page=irb">our help page</a> for more information about this process.
                     </p>
                     <% } %>
                     <% if (displayNotice == 5) { %>
                     <p id="projectAdminNotice">You've identified the data in this project as containing "study data" and specified 
                     the associated IRB, but you haven't uploaded your IRB file(s) yet. Please upload the associated IRB approval 
                     letter next. In addition, if you wish to be able to share this project outside of your immediate research team, 
                     you will need to click "Submit project for review". See <a href="help?page=irb">our help page</a> for more information 
                     about this process.
                     </p>
                     <% } %>
                     <% if (displayNotice == 6) { %>
                     <p id="projectAdminNotice">You've identified the data in this project as containing "study data" and specified 
                     the associated IRB, but you haven't uploaded your IRB file(s) yet. Please upload the associated IRB approval 
                     letter next. In addition, if you wish to be able to share this project outside of your immediate research team, 
                     you will also need to upload your study consent form and then click "Submit project for review". 
                     See <a href="help?page=irb">our help page</a> for more information about this process.
                     </p>
                     <% } %>
                     <% if (displayNotice == 7) { %>
                     <p id="projectAdminNotice">It looks like you've specified an IRB for this project and uploaded one or more IRB files. 
                     If you'd like to be able to share this project outside your immediate research team, the last step is for you to click 
                     "Submit project for review". See <a href="help?page=irb">our help page</a> for more information about this process.
                     </p>
                     <% } %>
                </div> <!-- submit-and-email-div -->
             <% } %>

             <div id="irb-docs">
                  <div class="left-label" style="width:120px"><span>IRB Documents</span></div>
                  <% if (adminUserFlag || hasRmRole || hasProjectAdmin) { %>
                     <div class="irb-add-link"><a id="irbAddIRBLink" href="javascript:openAddIRBDialog(<%=projectId%>)">Add an IRB (step 1)</a></div>
                     <div id="projectIrbUploadNote" class="irb-note">You can upload files (step 2) after adding an IRB.</div>
                  <% } %>
                  <% for (IrbDto irb : irbList) {
                        String irbExpDateDisplay = IrbItem.NOT_APPLICABLE;
                        if (!irb.getExpirationDateNaFlag()) {
                            irbExpDateDisplay = irb.getExpirationDateString();
                        }
                     %>
                     <div class="irb-doc" id="irbDocDiv_<%=irb.getId()%>">

                        <div id="irbDocTitle"><span><%=irb.getTitle()%></span></div>
                        <div id="irbEditRemoveDiv">
                             <div id="irbDocEdit"><a id="editIRBLink"
                                  href="javascript:openEditIRBDialog(<%=irb.getId()%>)">Edit this IRB</a></div>
                             <%
                             if (projectReviewDto.getShareabilityStatus().equals("shareable")) {
                                removeIrbLinkClass = "remove-irb-link-inactive";
                             }
                             %>
                             <div id="removeThisIrbDiv">
                             <%
                             if (projectReviewDto.getShareabilityStatus().equals("shareable")) {
                             %>
                                <div id="irbDocRemove_<%=irb.getId()%>">
                                     <span class="<%=removeIrbLinkClass%>">Remove this IRB
                                     </span>
                                </div>
                             <%
                             } else {
                             %>
                                <div id="irbDocRemove_<%=irb.getId()%>"><a class="<%=removeIrbLinkClass%>"
                                     href="javascript:showIrbRemoveSureDiv(<%=irb.getId()%>)">Remove this IRB</a>
                                </div>
                             <%
                             }
                             %>
                             <div id="irbRemoveSure_<%=irb.getId()%>" class="irbRemoveSure">
                                  remove this IRB?&nbsp
                                  <a id="irbDeleteNoLink" href="javascript:showIrbRemoveLinkDiv(<%=irb.getId()%>)">no</a>
                                  &nbsp/&nbsp
                                  <a id="irbDeleteYesLink" href="javascript:removeIRB(<%=irb.getId()%>, <%=projectId%>)">yes</a>
                             </div>
                             </div>
                        </div>
                        <div id="irbDocPN">
                        <span class="irb-doc-label">IRB Protocol Number</span>
                        <span id="pnField_<%=irb.getId()%>" class="irb-doc-field"><%=irb.getProtocolNumber()%></span>
                        </div>
                        <div id="irbDocPI">
                        <span class="irb-doc-label">IRB PI</span>
                        <span id="piField_<%=irb.getId()%>" class="irb-doc-field"><%=irb.getPiName()%></span>
                        </div>
                        <div id="irbDocAD">
                        <span class="irb-doc-label">IRB Approval Date</span>
                        <span id="adField_<%=irb.getId()%>" class="irb-doc-field"><%=irb.getApprovalDateString()%></span>
                        </div>
                        <div id="irbDocED">
                        <span class="irb-doc-label">IRB Expiration Date</span>
                        <span id="edField_<%=irb.getId()%>" class="irb-doc-field"><%=irbExpDateDisplay%></span>
                        </div>
                        <div id="irbDocGI">
                        <span class="irb-doc-label">Granting Institution</span>
                        <span id="giField_<%=irb.getId()%>" class="irb-doc-field"><%=irb.getGrantingInstitutionString()%></span>
                        </div>
                        <div id="irbDocNotes">
                        <span class="irb-doc-label">Notes</span>
                        <span id="notesField_<%=irb.getId()%>" class="irb-doc-field"><%=irb.getNotes()%></span>
                        </div>
                        <div id="irbDocFiles">
                        <table class="irb-doc-files-table">
                        <colgroup>
                            <col style="width: 45%" />
                            <col style="width: 35%" />
                            <col style="width: 20%" />
                        </colgroup>
                        <tr>
                        <td class="irb-fake-header">Files</td>
                        <td class="irb-fake-header">Uploaded By</td>
                        <td class="irb-fake-header">Date</td>
                        </tr>
                        <%
                           List<IrbFileDto> files = irb.getFileList();
                           for (IrbFileDto file : files) {
                        %>
                           <tr>
                           <td>
                           <a href="javascript:downloadIRBFile(<%=irb.getId()%>, <%=file.getFileId()%>)">
                                <%=file.getFileName()%>
                           </a>
                           </td>
                           <td><%=file.getFileOwnerString()%></td>
                           <td><%=file.getAddedTimeString()%></td>
                           </tr>
                        <% } %>
                        </table>
                        </div>

                        <div class="irb-file-add">
                             <a id="irbAddFileLink" href="javascript:openAddFileDialog(<%=irb.getId()%>)">Upload a file (step 2)</a>
                        </div>

                     </div>  <!-- irb-doc -->
                  <% } %>
                  <% if (irbList.size() == 0) { %>
                     <span id="emptyIRBListSpan">No IRBs uploaded.</span>
                  <% } %>
             </div> <!-- irb-docs -->
             <div id="irb-shareability-history">
                  <div class="left-label"><span>Shareability Review History</span></div>
                  <table id="irb-shareability-history-table" class="irb-shareability-history">
                  <%
                  for (ProjectShareabilityHistoryDto psh : pshList) {
                  %>
                     <tr>
                     <td><%=psh.getUpdatedTimeString()%></td>
                     <td><%=psh.getUpdatedByLink()%></td>
                     <td><%=psh.getShareableStatusString()%></td>
                     </tr>
                  <%
                  }
                  %>
                  <% if (pshList.size() == 0) { %>
                     <span id="emptyShareabilityListSpan">No shareability review history.</span>
                  <% } %>
                  </table>
             </div> <!-- irb-shareability-history -->
             <div id="irb-rm-notes">
                  <div class="left-label"><span>Research Manager's Notes</span></div>
                  <% if (adminUserFlag || hasRmRole) { %>
                     <div class="irb-edit-link" style="position:static">
                          <a id="irbEditRMNotesLink" href="javascript:showEditRMNotesDiv()">edit</a>
                     </div>
                  <% } %>
                  <div id="irbInfoRMNotesDiv">
                       <span id="irbInfoRMNotesSpan" class="<%=rmNotesClass%>"><%=rmNotes%></span>
                  </div>
                  <div id="irbInfoEditRMNotesDiv">
                       <textarea rows="8" cols="50" id="rmNotesField" class="rm-notes-field"
                                 name="rmNotesField"><%=projectReviewDto.getResearchManagersNotes().trim()%></textarea>
                       <br />
                       <input type="button" id="rmNotesSaveButton" value="Save" onclick="javascript:saveRMNotesChange(<%=projectId%>)" />
                       <input type="button" id="rmNotesCancelButton" value="Cancel" onclick="javascript:cancelRMNotesChange()" />
                       <input type="hidden" id="rmNotesHiddenField" value="<%=rmNotesWithoutQuotes%>" />
                  </div>
             </div> <!-- irb-info-row -->
        </div> <!-- irb-info-div -->
        </div> <!-- irb_content_div -->

        <!-- make note of attrs needed by javascript -->
        <input type="hidden" id="addIRBSearchBy" value="<%=addIRBSearchBy%>" />
        <input type="hidden" id="currentUser" value="<%=remoteUser%>" />
        <input type="hidden" id="editIRBIsOpen" value="<%=irbIdOpenForEdit%>" />

        </div>  <!-- project-page div -->
    </td>
</tr>

<!-- Un-populated divs needed for modal windows using jQuery -->
<div id="addIRBDialog" class="addIRBDialog"></div>
<div id="addIRBFileDialog" class="addIRBFileDialog"></div>
<div id="editIRBDialog" class="editIRBDialog"></div>
<div id="submitForReviewDialog" class="submitForReviewDialog"></div>

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/project_page_irb.jspf" %>
</div>

</body>
</html>

