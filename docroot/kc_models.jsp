<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Benjamin Billings
// Version: $Revision: 13212 $
// Last modified by: $Author: mkomisin $
// Last modified on: $Date: 2016-04-29 11:41:49 -0400 (Fri, 29 Apr 2016) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillModelItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SubgoalAttemptItem"%>

<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.kcmodel.KCModelHelper"%>

<%@page import="org.apache.commons.lang.time.FastDateFormat"%>

<%
    DecimalFormat commaDf = new DecimalFormat("#,###,###");

    DatasetContext httpSessionInfo = (DatasetContext)session.getAttribute("datasetContext_"
        + request.getParameter("datasetId"));

    Boolean editFlag = httpSessionInfo.getEditFlag() || httpSessionInfo.isDataShopAdmin();

    DatasetItem datasetItem = httpSessionInfo.getDataset();

    boolean exportAllowed = true;
    if ((datasetItem.getReleasedFlag() == null)
            || !datasetItem.getReleasedFlag()
            || (datasetItem.getProject() == null)) {
        exportAllowed = false;
    }

    // Datashop Admins can always export.
    exportAllowed |= httpSessionInfo.isDataShopAdmin();

    String kcmSortBy = httpSessionInfo.getKCModelContext().getSortBy();
    Boolean kcmSortAscending = httpSessionInfo.getKCModelContext().getSortByAscendingFlag();
    Boolean kcmGroupByNumObservations = true;

    KCModelHelper modelHelper = HelperFactory.DEFAULT.getKCModelHelper();
    List <SkillModelItem> modelList =
        modelHelper.getModelListSorted(httpSessionInfo.getDataset(),
                                       kcmSortBy,
                                       kcmSortAscending,
                                       kcmGroupByNumObservations);

    // Check for presence of data...
    long numberOfTransactions = httpSessionInfo.getNumTransactions();
    boolean isRemote =
        HelperFactory.DEFAULT.getNavigationHelper().isDatasetRemote(httpSessionInfo.getDataset());

%>
<div id="modelInfo">

    <h1>KC Models</h1>
</div>

<%
if (isRemote) {
    out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/alert_32.gif\" /></div>"
            + "<p>This dataset can be found on a remote DataShop instance.</p></div>");


} else if (numberOfTransactions == 0) {
    out.println("<div class=\"info shortinfo\"><div class=\"imagewrapper\">"
            + "<img src=\"images/alert_32.gif\" /></div>"
            + "<p>There is no transaction data for this dataset.</p></div>");

} else {
%>
<div id="toolbox">
    <% if (exportAllowed) { %>
    <span id="model_toolbox_export" class="control">Export</span>
    <% if (editFlag) { %>
    &nbsp; <span style="color:lightgray">|</span>&nbsp;
    <span id="model_toolbox_import" class="control">Import</span>
    <% } %>
    <% } else { %>
    <span class="control-disabled">Export</span>
    <% if (editFlag) { %>
    &nbsp; <span style="color:lightgray">|</span>&nbsp;
    <span class="control-disabled">Import</span>
    <% } %>
    <% } %>
</div>
<%
} // else
%>

<div id="model_dialogue" style="display:none">
    <% if (editFlag) { %>
    <span id="model_dialogue_export">Export (step 1)</span>
        <span id="model_dialogue_import">Import (step 2)</span>
    <% } else { %>
    <span id="model_dialogue_export">Export</span>
    <% } %>

    <div id="model_export" style="display:none">
        <label>Select existing model(s) to export</label>
        <select multiple="multiple" id="export_model_select">
            <option value="-1">(new only)</option>
<%
                for(SkillModelItem model : modelList) {
                    List <String> skillNames = modelHelper.getSkillNames(model);
%>
                    <option value="<%=model.getId() %>"><%=model.getSkillModelName() %></option>
<%
                }
%>
        </select>
        <p style="" id="warning_text_short">
            Exporting a KC model with a transaction-to-KC mapping type will convert it to a step-to-KC mapping,
            which may result in a loss of information. (<span id="show_more_control" class="showMore control">more</span>)
        </p>
        <p style="display:none" id="warning_text_long">
            The step-to-KC mapping that DataShop exports may be inconsistent with the original
            transaction-to-KC mapping if the learning software assigned knowledge components
            to steps differently for different students (for example, this may occur if there
            are multiple correct paths through the learning software).
            If these data were generated by such a system, any new model you create will not
            be statistically comparable to the original transaction-to-KC model.
        </p>
        <input type="button" value="Export" id="model_export_btn" class="native-button" />
        <input type="button" value="Close" id="model_close_btn" />
    </div>

    <div id="model_import" style="display:none">
        <form id="model_import_form" method="post" enctype="multipart/form-data"
            action="KcModel?datasetId=<%=httpSessionInfo.getDataset().getId() %>">
            <label>Select a KC model file to import</label>
            <input id="model_import_file" type="file" name="model_import_file" />
            <input type="hidden" value="<%=httpSessionInfo.getDataset().getId() %>" name="datasetId" />
            <p>DataShop will first try to verify the validity of your KC model file.
                Information about this process will be displayed below.
                After verifying, you will be given the opportunity to continue importing or cancel the process.</p>
            <div id="model_import_log"></div>
            <input type="button" value="Verify" id="model_import_btn" class="native-button" disabled="disabled" />
            <input type="button" value="Cancel" id="model_import_cancel_btn" />
        </form>
    </div>
</div>

<%
if (numberOfTransactions > 0) {
        int modelOrder = 0;

%>

<div id="modelValuesTableContainer">
    <div class="kcm_sort_note">
        <span class="kcm_sort_note_label">Sort by</span>
        <span class="kcm_sort_note_value">
            <select id="kcmSortBy" name="kcmSortBy">
            <%
                for(Iterator it = KCModelContext.SORT_OPTIONS.listIterator(); it.hasNext();) {
                    String sortOption = (String)it.next();
                    out.print("<option value=\"" + sortOption + "\" ");
                    if (sortOption.compareTo(kcmSortBy) == 0) {
                        out.print("selected");
                    }
                    out.print(" >" + sortOption + "</option>");
                }
            %>
            </select>

            <select id="kcmSortAscending" name="kcmSortAscending">
               <option value="true"
               <% if (kcmSortAscending.booleanValue()) {
                     out.print("selected"); } %>
                >ascending</option>
                <option value="false"
                <% if (!kcmSortAscending.booleanValue()) {
                      out.print("selected"); } %>
                >descending</option>
            </select>
        </span>
        <br />

        <span class="kcm_sort_note_label">Grouped by </span>
        <span class="kcm_sort_note_value">Observations with KCs (ascending)</span>
    </div>

<%
        // Can the user delete any models? If so, we need the final column.
        boolean isModifyAuthorizedForAny = false;
        for(SkillModelItem model : modelList) {
            Long modelId = (Long)model.getId();
            if (modelHelper.isModifyAuthorized(httpSessionInfo.getUser(), modelId)) {
                isModifyAuthorizedForAny = true;
                break;
            }
        }
%>
<table id="modelValuesTable" class="dataset-box">
     <tr>
         <th colspan=6 style="background-color:white;border-left:1px solid white;"></th>
         <th colspan=4 id="cross_validation_header">Cross Validation*</th>
<%
        if (isModifyAuthorizedForAny) {
%>
         <th style="border-top:none;border-right:1px solid white;background-color:white;"></th>
<%
        }
%>
     </tr>
     <tr>
         <th class=firstcell" style="border-left:1px solid #b3b3b3"></th>
         <th id="model_name_header">Model Name</th>
         <th class="num_kcs">KCs</th>
         <th class="obs_with_kcs">Observations with KCs</th>
         <th>AIC</th>
         <th>BIC</th>
         <th>RMSE (student stratified)</th>
         <th>RMSE (item stratified)</th>
         <th>RMSE (unstratified)</th>
         <th>Observations (unstratified)</th>
<%
        if (isModifyAuthorizedForAny) {
%>
         <th></th>
<%
        }
%>
     </tr>
<%
if (modelList.size() == 0) {
%>
        <tr>
                <td class="firstcell"> </td>
                <td> <span class="modelNameFake"> No KC Models defined.</span></td>
                <td> </td>
                <td> </td>
                <td> </td>
                <td> </td>
                <td> </td>
                <td> </td>
                <td> </td>
                <td> </td>
        </tr>
<%
} // if (modelList.size() == 0)
%>
<%
       SkillModelItem prevModel = null;

       for(SkillModelItem model : modelList) {
            Integer numObs = model.getNumObservations();
            Integer prevNumObs = prevModel == null ? null : prevModel.getNumObservations();

            boolean numObsEqual = numObs == null && prevNumObs == null
                    || numObs != null && numObs.equals(prevNumObs);
            String numObsClass = numObsEqual ? "" : "numObsChanged";

            modelOrder++;
            String isEvenClass = "";
                if (modelOrder % 2 == 0) {
                    isEvenClass = "even";
                } else {
                    isEvenClass = "";
                }
        List <String> skillNames = modelHelper.getSkillNames(model);
        String skillModelName = model.getSkillModelName().toLowerCase().replace(' ', '_');
        Long modelId = (Long) model.getId();
        boolean isModifyAuthorized = modelHelper.isModifyAuthorized(httpSessionInfo.getUser(), modelId);

        String modelIdString = (isModifyAuthorized) ? "model_id=" + model.getId() : "";

    String source = null;
        if (model.getSource().equals(SkillModelItem.SOURCE_LOGGED)) {
        source = "logged with dataset";
    } else if (model.getSource().equals(SkillModelItem.SOURCE_IMPORTED)) {
                source = "created by: " + modelHelper.getOwnerDisplayName(model);
    } else {
                source = "auto-generated model";
    }

    String createdOn = null;
    if(model.getCreationTime() != null) {
        FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
        createdOn = dateFormat.format(model.getCreationTime());
    }

    String mappingType = null;
    if (model.getMappingType() != null && !model.getMappingType().equals("")) {
        mappingType = model.getMappingType();
    }

    String status = null;
    if (model.getStatus() != null && !model.getStatus().equals("")) {
        status = model.getStatus();
    }
%>

    <tr class="model <%=numObsClass%>" id="kcm_id_model_<%=model.getId() %>">
        <td class="firstcell <%=isEvenClass%>">
            <div id="<%=skillModelName %>_details_link" class="detailsLink control" <%=modelIdString %> model_order="<%=modelOrder%>">
                <img src="images/expand.png">
                          </div>
        </td>

        <td class="<%=isEvenClass%>">

            <div id="tooltip_content_<%=modelOrder%>" class="xstooltip" style="display:none">
<%
            if(source != null) {
%>
                <strong>Source:</strong> <%=source%><br/>
<%
            }

            if(createdOn != null) {
%>
                <strong>Created on:</strong> <%=createdOn%><br/>
<%
            }

            if(mappingType != null) {
%>
                <strong>Mapping type:</strong> <%=mappingType%><br/>
<%
            }

            if(status != null) {
%>
                <strong>Status:</strong> <%=status%><br/>
<%
            }

            if(isModifyAuthorized) {
%>
                <em>Click to rename this KC model.</em>
<%
            }
%>
            </div>

            <span class="modelName" id="kcm_id_name_<%=modelOrder%>" <%=modelIdString %>
                name="model_order_<%=modelOrder%>" model_order="<%=modelOrder%>"><%=model.getSkillModelName() %></span>
        </td>

        <td class="num_kcs <%=isEvenClass%>">
<%
        Integer numKcs = model.getNumSkills();
        if (numKcs != null && numKcs > 0) {
%>
             <span class="value"><%=commaDf.format(numKcs)%></span>
<%
        }
%>
        </td>

        <td class="obs_with_kcs <%=isEvenClass%>">
<%
        if (numObs != null && numObs > 0) {
%>
            <span class="value"><%=commaDf.format(numObs)%></span>
<%
        }
%>
        </td>

<%
        if (SkillModelItem.LFA_STATUS_COMPLETE.equals(model.getLfaStatus())) {
%>
            <td class="<%=isEvenClass%>">
                <span class="value"><%= model.getLFAStatusOrAICValueForDisplay() %></span>
            </td>
            <td class="<%=isEvenClass%>">
                <span class="value"><%= model.getEmptyStringOrBICValueForDisplay() %></span>
            </td>

<%
            if (SkillModelItem.CV_STATUS_COMPLETE.equals(model.getCvStatus())) {
%>
            <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getCVStatusOrCvStudentStratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getEmptyStringOrCvStepStratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getEmptyStringOrCvUnstratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
<%
            Integer cvNumObservations = model.getUnstratifiedNumObservations();
            if (cvNumObservations != null && cvNumObservations > 0) {
%>
                            <span class="value"><%=commaDf.format(cvNumObservations)%></span>
<%
            } else {
%>
                <span class="value"></span>
<%
            }
%>
            </td>
<%
            } else if (SkillModelItem.CV_STATUS_IMCOMPLETE.equals(model.getCvStatus())) {
%>
<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvStudentStratifiedRmseForDisplay())){
%>
                <td class="<%=isEvenClass%>">
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() %></span>
                        </td>
<%
            }
%>

<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvStepStratifiedRmseForDisplay())){
%>
                <td class="<%=isEvenClass%>">
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() %></span>
                        </td>
<%
            }
%>

<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvUnstratifiedRmseForDisplay())){
%>
                        <td class="<%=isEvenClass%>" colspan=2>
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                        <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() %></span>
                        </td>

                        <td class="<%=isEvenClass%>">
<%
                Integer cvNumObservations = model.getUnstratifiedNumObservations();
                if (cvNumObservations != null && cvNumObservations > 0) {
%>
                                <span class="value"><%=commaDf.format(cvNumObservations)%></span>
<%
                } else {
%>
                    <span class="value"></span>
<%
                }
%>
                </td>
<%
            }
%>
<%
        } else {
%>
                <td class="<%=isEvenClass%>" colspan=4>
                        <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCvStatusPartialDescriptionForDisplay() %></div></span>
                    </td>

<%
        }
%>

<%
    } else  {
%>
            <td class="<%=isEvenClass%>" colspan=2>
                <span class="value colspanning"><div title="<%=model.getLfaStatusDescriptionForDisplay()%>"><%= model.getLfaStatusPartialDescriptionForDisplay() %></div></span>
            </td>





<%
            if (SkillModelItem.CV_STATUS_COMPLETE.equals(model.getCvStatus())) {
%>
            <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getCVStatusOrCvStudentStratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getEmptyStringOrCvStepStratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
                        <span class="value"><%= model.getEmptyStringOrCvUnstratifiedRmseForDisplay() %></span>
                    </td>

                    <td class="<%=isEvenClass%>">
<%
            Integer cvNumObservations = model.getUnstratifiedNumObservations();
            if (cvNumObservations != null && cvNumObservations > 0) {
%>
                            <span class="value"><%=commaDf.format(cvNumObservations)%></span>
<%
            } else {
%>
                <span class="value"></span>
<%
            }
%>
            </td>
<%
            } else if (SkillModelItem.CV_STATUS_IMCOMPLETE.equals(model.getCvStatus())) {
%>
<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvStudentStratifiedRmseForDisplay())){
%>
                <td class="<%=isEvenClass%>">
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() %></span>
                        </td>
<%
            }
%>

<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvStepStratifiedRmseForDisplay())){
%>
                <td class="<%=isEvenClass%>">
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() %></span>
                        </td>
<%
            }
%>

<%
            if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(model.getCVIncompleteOrCvUnstratifiedRmseForDisplay())){
%>
                        <td class="<%=isEvenClass%>" colspan=2>
                            <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() %></div></span>
                        </td>
<%
            } else {
%>
                        <td class="<%=isEvenClass%>">
                            <span class="value"><%= model.getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() %></span>
                        </td>

                        <td class="<%=isEvenClass%>">
<%
                Integer cvNumObservations = model.getUnstratifiedNumObservations();
                if (cvNumObservations != null && cvNumObservations > 0) {
%>
                                <span class="value"><%=commaDf.format(cvNumObservations)%></span>
<%
                } else {
%>
                    <span class="value"></span>
<%
                }
%>
                </td>
<%
            }
%>
<%
        } else {
%>
                <td class="<%=isEvenClass%>" colspan=4>
                        <span class="value colspanning"><div title="<%=model.getCvStatusDescriptionForDisplay()%>"><%= model.getCvStatusPartialDescriptionForDisplay() %></div></span>
                    </td>

<%
        }
%>



<%
    }
    if (isModifyAuthorizedForAny) {
%>
                    <td class="deletecell <%=isEvenClass%>">
<%
        if (isModifyAuthorized) {
%>
                    <span class="deleteModel control" model_id="<%=model.getId() %>"><img src="images/delete.gif"
                            title="Delete Model" alt="Delete Model" /></span>
<%
        }
%>
                    </td>
<%
                }
%>
    </tr>

    <tr class="moreInfo" style="display: none" id="<%=modelOrder %>_details">
        <td class="<%=isEvenClass%>" colspan=11>
                    <h3>KCs in this model</h3>
                    <ul class="skills">
<%
                        for(String skillName : skillNames) {
                            out.print("\n\t\t\t<li>" + skillName + "</li>");
                        }
%>
                    </ul>
        </td>
    </tr>

<%
            prevModel = model;
        } // end for loop on skill models
%>
</table>
</div>

<p class="cross-validation-note">* Cross validation RMSE values will be slightly different between runs.
The number displayed is based on a single run.</p>
<%
} // end if (numberOfTransactions > 0)
%>
<div style="clear:both;"></div>

