<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 14256 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2017-09-08 13:18:08 -0400 (Fri, 08 Sep 2017) $
// $KeyWordsOff: $
%>
<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.Serializable"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dto.StudentWithIntercept"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillItem"%>
<%@page import="edu.cmu.pslc.datashop.item.SkillModelItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ServletUtil"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LfaValuesHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LfaValuesServlet"%>

<%
DecimalFormat commaDf = new DecimalFormat("#,###,###");
String ONE_UNIQUE_STEP = new String("1");

NavigationHelper navigationHelper = HelperFactory.DEFAULT.getNavigationHelper();
DatasetContext sessionInfo = (DatasetContext)session.getAttribute(
        "datasetContext_" + request.getParameter("datasetId"));
LearningCurveContext lcContext = sessionInfo.getLearningCurveContext();
DatasetItem datasetItem = sessionInfo.getDataset();

boolean exportAllowed = true;
if ((datasetItem.getReleasedFlag() == null)
        || !datasetItem.getReleasedFlag()
        || (datasetItem.getProject() == null)) {
    exportAllowed = false;
}

// Datashop Admins can always export.
exportAllowed |= sessionInfo.isDataShopAdmin();

    //get the selected skill model, skills and students.
        SkillModelItem primaryModel = DaoFactory.DEFAULT.getSkillModelDao().get(
            navigationHelper.getSelectedSkillModel(sessionInfo));
        LfaValuesHelper lfaHelper = HelperFactory.DEFAULT.getLfaValuesHelper();
        List<SkillItem> skillList = null;

        // Can only offering sorting and tagging on KCMs that have had LFA run.
        Boolean sortable =
            primaryModel.getLfaStatus().equals(SkillModelItem.LFA_STATUS_COMPLETE) ? true : false;

        if (sortable && lcContext.getSortAndTag()) {
            skillList = lfaHelper.getSortedSkillList(sessionInfo.getDataset(), primaryModel);
        } else {
            skillList = lfaHelper.getSkillList(sessionInfo.getDataset(), primaryModel);
        }

        // Get map of numUniqueSteps-to-skill.
        HashMap<SkillItem, Long> stepsSkillMap = null;
        // No need to get map for 'Unique-Step' model.
        if (!primaryModel.getSkillModelName().equals(SkillModelItem.NAME_UNIQUE_STEP_MODEL)) {
           stepsSkillMap = lfaHelper.getNumberUniqueStepsBySkillMap(sessionInfo.getDataset(), skillList);
        }

        // Get map of numObservations-to-skill.
        HashMap<SkillItem, Long> obsSkillMap =
            lfaHelper.getNumberObservationsBySkillMap(sessionInfo.getDataset(), skillList);

        // Only include the 'KC Category' column if data is present.
        boolean kcCategoryPresent = false;
        for (Iterator iter = skillList.iterator(); iter.hasNext(); ) {
            SkillItem skillItem = (SkillItem)iter.next();
            if (skillItem.getCategory() != null) {
               if (!skillItem.getCategory().equals("")) {
                  kcCategoryPresent = true;
                  break;
               }
            }
        }

        List studentList = lcContext.getStudentInterceptList();

        String numberOfParameters = "";
        if (lcContext.getLfaNumberOfParameters() != null) {
            numberOfParameters += commaDf.format(lcContext.getLfaNumberOfParameters());
        } else {
            numberOfParameters = "&nbsp;";
        }

        String cvNumParameters = "";
        if (primaryModel.getUnstratifiedNumParameters() != null) {
            cvNumParameters += commaDf.format(primaryModel.getUnstratifiedNumParameters());
        }
        String numObservations = "";
        if (primaryModel.getNumObservations() != null) {
            numObservations += commaDf.format(primaryModel.getNumObservations());
        }
        String cvNumObservations = "&nbsp;"; // use non-breaking space so that at least one cell will have a value
        if (primaryModel.getUnstratifiedNumObservations() != null) {
            cvNumObservations = commaDf.format(primaryModel.getUnstratifiedNumObservations()).toString();
        }
        String cvStudentStratifiedRmse = "";
        String cvStudentStratifiedRmseTooltip = "";
        if (primaryModel.getCvStudentStratifiedRmseForDisplay() != null) {
            cvStudentStratifiedRmse = ServletUtil.truncate(primaryModel.getCvStudentStratifiedRmseForDisplay(), true, true, 6);
            cvStudentStratifiedRmseTooltip = "" + primaryModel.getCvStudentStratifiedRmse();
        } else {
            cvStudentStratifiedRmse = "&nbsp;";
            cvStudentStratifiedRmseTooltip = "";
        }
        
        String cvStepStratifiedRmse = "";
        String cvStepStratifiedRmseTooltip = "";
        if (primaryModel.getCvStepStratifiedRmseForDisplay() != null) {
            cvStepStratifiedRmse = ServletUtil.truncate(primaryModel.getCvStepStratifiedRmseForDisplay(), true, true, 6);
            cvStepStratifiedRmseTooltip = "" + primaryModel.getCvStepStratifiedRmse();
        } else {
            cvStepStratifiedRmse = "&nbsp;";
            cvStepStratifiedRmseTooltip = "";
        }
        
        String cvUnstratifiedRmse = "";
        String cvUnstratifiedRmseTooltip = "";
        if (primaryModel.getCvUnstratifiedRmseForDisplay() != null) {
            cvUnstratifiedRmse = ServletUtil.truncate(primaryModel.getCvUnstratifiedRmseForDisplay(), true, true, 6);
            cvUnstratifiedRmseTooltip = "" + primaryModel.getCvUnstratifiedRmse();
        } else {
            cvUnstratifiedRmse = "&nbsp;";
            cvUnstratifiedRmseTooltip = "";
        }
%>
    <div id="lfaValues">
            <h1 class="lfaStatus"> Logistic regression model status: <%=primaryModel.getLfaStatus()%></h1>
            <h1 class="lfaStatus">Cross Validation status: <%=primaryModel.getCvStatus()%></h1>
            <form action="LfaValues" method="post">
            <input type="hidden" name="datasetId" value="<%=sessionInfo.getDataset().getId() %>" />
            <% if (exportAllowed) { %>
            <input type="submit" value="Export Model Values" class="native-button" name="exportLfaValues" id="exportLfaValues"/>
            <% } else { %>
            <input type="submit" value="Export Model Values" class="native-button" name="exportLfaValues" id="exportLfaValues" disabled/>
            <% } %>
            </form>
        
        <div id="kcModelValuesContainer">
            <table id="kcModelValues" class="dataset-box">
                <caption>
                KC Model Values for <%=primaryModel.getSkillModelName()%> Model
                </caption>
                <thead>
                    <tr>
                        <th>AIC</th>
                        <th>BIC</th>
                        <th>Log Likelihood</th>
                        <th>Number of Parameters</th>
                        <th>Number of Observations</th>
                    </tr>
                </thead>
<%
        if (SkillModelItem.LFA_STATUS_COMPLETE.equals(primaryModel.getLfaStatus())) {
%>
                <tbody>
                    <tr>
                        <td><%=ServletUtil.truncate(primaryModel.getAic(), true, true, 2)%></td>
                        <td><%=ServletUtil.truncate(primaryModel.getBic(), true, true, 2)%></td>
                        <td><%=ServletUtil.truncate(primaryModel.getLogLikelihood(), true, true, 2)%></td>
                        <td><%=numberOfParameters%></td>
                        <td><%=numObservations%></td>
                    </tr>
                </tbody>
<%
	} else  {
%>
            <tbody>
                    <tr>
		            <td colspan=5>
		                <div title="<%=primaryModel.getLfaStatusDescriptionForDisplay()%>"><%= primaryModel.getLfaStatusPartialDescriptionForDisplay() %></div>
		            </td>
		    </tr>
             </tbody>
<%
	}
%> 
            </table>
          </div>  
        
          <div id="stratifiedCrossValidationValuesContainer">
            <table id="stratifiedCrossValidationValues" class="dataset-box">
            <caption>Cross Validation Values (Stratified)</caption>
                <thead>
                    <tr>
                        <th>Cross Validation RMSE (student stratified)</th>
                        <th>Cross Validation RMSE<br />(item stratified)</th>
                    </tr>
                </thead>
<%
        	if (SkillModelItem.CV_STATUS_COMPLETE.equals(primaryModel.getCvStatus())) {
%>
                <tbody>
                    <tr>
                        <td><div title="<%=cvStudentStratifiedRmseTooltip%>"><%=cvStudentStratifiedRmse%></div></td>
                        <td><div title="<%=cvStepStratifiedRmseTooltip%>"><%=cvStepStratifiedRmse%></div></td>
                    </tr>
                </tbody>
<%
        	} else if (SkillModelItem.CV_STATUS_IMCOMPLETE.equals(primaryModel.getCvStatus())) {
%>
                <tbody>
                    <tr>
<%
			if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(primaryModel.getCVIncompleteOrCvStudentStratifiedRmseForDisplay())){   
%>
                        	<td><div title="<%=primaryModel.getCvStatusDescriptionForDisplay()%>"><%= primaryModel.getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() %></div></td>
<%
			} else {
%>
				<td><div title="<%=cvStudentStratifiedRmseTooltip%>"><%=cvStudentStratifiedRmse%></div></td>
<%
			}
%>
<%
			if (SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(primaryModel.getCVIncompleteOrCvStepStratifiedRmseForDisplay())){   
%>
                        	<td><div title="<%=primaryModel.getCvStatusDescriptionForDisplay()%>"><%= primaryModel.getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() %></div></td>
<%
			} else {
%>
				<td><div title="<%=cvStepStratifiedRmseTooltip%>"><%=cvStepStratifiedRmse%></div></td>
<%
			}
%>
                    </tr>
                </tbody>
<%
        	} else {
%> 
		<tbody>
                    <tr>
			<td colspan=2>
	                	<div title="<%=primaryModel.getCvStatusDescriptionForDisplay()%>"><%= primaryModel.getCvStatusPartialDescriptionForDisplay() %></div>
	            	</td>
	            </tr>
	        </tbody>
<%
        	}
%>                
                
            </table>
        </div>  
        
        <div id="unstratifiedCrossValidationValuesContainer">
            <table id="unstratifiedCrossValidationValues" class="dataset-box">
            <caption>Cross Validation Values (Unstratified)</caption>
                <thead>
                    <tr>
                        <th>Cross Validation RMSE</th>
                        <th>Number of Parameters*</th>
                        <th>Number of Observations*</th>
                    </tr>
                </thead>
<%
		if (!SkillModelItem.CV_STATUS_UNABLE_TO_RUN.equals(primaryModel.getCVIncompleteOrCvUnstratifiedRmseForDisplay())){   
%>	
                <tbody>
                    <tr>
                        <td><div title="<%=cvUnstratifiedRmseTooltip%>"><%=cvUnstratifiedRmse%></div></td>
                        <td><%=cvNumParameters%></td>
                        <td><%=cvNumObservations%></td>
                    </tr>
                </tbody>
<%
		} else if (SkillModelItem.CV_STATUS_IMCOMPLETE.equals(primaryModel.getCvStatus())) {   
%>
		<tbody>
                    <tr>
                    	<td colspan=3>
                	<div title="<%=primaryModel.getCvStatusDescriptionForDisplay()%>"><%= primaryModel.getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() %></div></span>
            		</td>
                    </tr>
                </tbody>
<%
		} else {
%>
		<tbody>
                    <tr>
                    	<td colspan=3>
                	<div title="<%=primaryModel.getCvStatusDescriptionForDisplay()%>"><%= primaryModel.getCvStatusPartialDescriptionForDisplay() %></div></span>
            		</td>
                    </tr>
                </tbody>
<%
		}
%>
            </table>
        <p id="unstratifiedCrossValidationNote">* In the case where a student or step does not appear in at least two observations, all data points 
        for that student or step are excluded from cross validation, resulting in fewer parameters and observations.</p>
        </div>  
        
        <% int rowCount = 0;
           Double slopeThreshold = lcContext.getAfmSlopeThreshold();
           String checkedStr = "checked=\"checked\"";
           if (!lcContext.getSortAndTag()) { checkedStr = ""; }
         %>

            <table id="kcValues" class="dataset-box">
                <caption>
                KC Values for <%=primaryModel.getSkillModelName()%> model
                <% if (sortable) { %>
                <div id="slope-threshold">
                 <form id="slope-threshold-form" action="LearningCurve">
                       <label for="sort_kcs"><span>Sort & Tag KC Values:</span></label>
                       <input type="checkbox" id="sort_kcs" name="sort_kcs" <%=checkedStr %> />
                       <label for="learning_rate_threshold"><span>Learning Rate Threshold:&nbsp;</span></label>
                       <input type="hidden"
                              name="datasetId" value="<%=sessionInfo.getDataset().getId() %>" />
                       <input id="learning_rate_threshold" name="learning_rate_threshold"
                              type="text" value="<%=slopeThreshold %>" />
                       <input id="slope-threshold-submit" type="submit" value="Update" disabled/>
                 </form>
                </div>
                <% } %>
                </caption>
                <tr>
                <th>KC Name</th>
                <% if (kcCategoryPresent) { %>
                      <th>KC Category</th>
                <% } %>
                <th>Number of<br/>Unique Steps</th>
                <th>Number of<br/>Observations</th>
                <th>Intercept (logit)</th>
                <th>Intercept (probability)</th>
                <th>Slope</th>
                </tr>
            <%
            //walk through the list of skills 
            rowCount = 1;
            for (Iterator iter = skillList.iterator(); iter.hasNext(); ) {
                SkillItem skillItem = (SkillItem)iter.next();
                StringBuffer classStr = new StringBuffer("class='afmNum");

                // If sorted, no grey shading, just lines between rows.
                if (sortable && lcContext.getSortAndTag()) {
                    classStr.append(" sorted");
                }

                // If not sorted, shade the even rows.
                if ((!sortable || !lcContext.getSortAndTag()) && (rowCount % 2 == 0)) {
                    classStr.append(" even");
                }

                if (sortable &&
                    lcContext.getSortAndTag() && (skillItem.getGamma() < slopeThreshold)) {
                    classStr.append(" lowSlope");
                }
                classStr.append("'");

                String numUniqueStepsStr = "";
                if (stepsSkillMap != null) {
                   Long numSteps = (Long)stepsSkillMap.get(skillItem);
                   if (numSteps != null) {
                      numUniqueStepsStr = numSteps.toString();
                   }
                } else {
                  // If the map is null it means this is the 'Unique-Step' model.
                  numUniqueStepsStr = ONE_UNIQUE_STEP;
                }

                String numObsStr = "";
                Long numObs = (Long)obsSkillMap.get(skillItem);
                if (numObs != null) {
                   numObsStr = numObs.toString();
                }
            %>
                <tr>
                <td <%=classStr%>><%=skillItem.getSkillName()%></td>
                <% 
                if (kcCategoryPresent) {    
                   if (skillItem.getCategory() != null) {
                %>
                        <td <%=classStr%>><%=skillItem.getCategory()%></td>
                <%
                   } else {
                %>
                        <td <%=classStr%>></td>
                <%
                   }
                }
                %>
                <td <%=classStr%>><%=numUniqueStepsStr %></td>
                <td <%=classStr%>><%=numObsStr %></td>
                <td <%=classStr%>><%=ServletUtil.truncate(skillItem.getBeta(), true, true, 2)%></td>
                <td <%=classStr%>><%=ServletUtil.truncate(skillItem.getSkillInterceptAsProbability(), true, true, 2)%></td>
                <td <%=classStr%>><%=ServletUtil.truncate(skillItem.getGamma(), true, true, 3)%></td>
               </tr>
            <% 
                rowCount++;
            }
            %>
            </table>

        <% rowCount = 1; %>

            <table id="studentValues" class="dataset-box">
                <caption>
                Student Values for <%=primaryModel.getSkillModelName()%> model
                <p class="lfaValuesNote"><%=LfaValuesServlet.STUDENT_INTERCEPT_CAPTION%></p>
                </caption>
                <tr>
                    <th>Anon Student Id</th>
                    <th>Intercept</th>
                </tr>
            <%
            //walk through the list of students 
            for (Iterator iter = studentList.iterator(); iter.hasNext(); ) {
                StudentWithIntercept studentWithIntercept = (StudentWithIntercept)iter.next();

                StringBuffer classStr = new StringBuffer("class='afmNum");                

                // For consistency, if table above not sorted, shade the even rows.
                if ((!sortable || !lcContext.getSortAndTag()) && (rowCount % 2 == 0)) {
                    classStr.append(" even");
                }
                // If sorted, no grey shading, just lines between rows.
                if (sortable && lcContext.getSortAndTag()) {
                    classStr.append(" sorted");
                }
                classStr.append("'");            
             %>
                <tr>
                    <td <%=classStr%>><%=studentWithIntercept.getAnonymousUserId()%></td>
                    <%
                    String lfaIntercept = ServletUtil.truncate(studentWithIntercept.getLfaIntercept(), true, true, 2);
                    if (lfaIntercept.equals("")) {
                        lfaIntercept = "<span title='No data in this KC Model for this student.'>N/A</span>";
                    }
                    %>
                    <td <%=classStr%>><%=lfaIntercept%></td>
                </tr>
            <%
                rowCount++;
            }
            %>
            </table>
        </div>
