<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
    // Author: Benjamin K. Billings
    // Version: $Revision: 10025 $
    // Last modified by: $Author: ctipper $
    // Last modified on: $Date: 2013-09-24 13:58:48 -0400 (Tue, 24 Sep 2013) $
    // $KeyWordsOff: $
%>
<%@page contentType="text/html"%>
<%@page import="java.io.PrintWriter"%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
 
<%@page import="edu.cmu.pslc.datashop.item.Item"%>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveImage" %>
<%@page import="edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveJspAssistant" %>

<%  LearningCurveJspAssistant helper = new LearningCurveJspAssistant(session, request, new PrintWriter(out));
    if (helper.primaryModel() == null) { %>
        <div class="info shortinfo">
           <div class="imagewrapper"><img src="images/alert_32.gif"/></div>
           <p>DataShop can't generate any learning curves because there are no KC models for this dataset. 
           To create a KC model, you'll need to associate KCs with steps in the data. See the 
           <a href="help?page=kcm">KC Models help page</a> for more information.</p>
        </div>
<%  } else if (helper.checkEmpty() != null) { %>
        <div class="info shortinfo">
           <div class="imagewrapper"><img src="images/info_32.gif"/></div>
           <p>Select at least one <%=helper.checkEmpty()%> to generate a learning curve.</p>
        </div>
<%  } else {
        helper.init();
        helper.produceRollup(); %>
		<img id="learning_curve_image" src="<%=helper.graphURL()%>"
		     width=<%=LearningCurveJspAssistant.CHART_WIDTH%> height=<%=LearningCurveJspAssistant.CHART_HEIGHT%>
		     border=0 usemap="#<%=helper.filename()%>"/>
		<br/>
                <div id="hideGraphInfo"><span id="hideGraphInfoLink">hide graph info</span></div>
                <div id="graphInfo" class="clearfix">
                    <div id="graphInfoCol1">
<%      if (helper.shouldPrintStdDevCutoff()) { %>
                        <span class="graphInfoHeader">Standard deviation cutoff:</span>
                        <span id="stdDevCutoffVal" class="clearfix"><%=helper.getStdDeviationCutOff()%></span>
<%      } %>
<span class="graphInfoHeader">Min and max opportunity cutoffs:</span>
<%      helper.printMinMaxCutoff(); %>
<% if (helper.getClassifyThumbnails()) { %>
<span class="graphInfoHeader">Last opportunity categorized:</span><span id="lastValidPointStr"><%=helper.getLastValidOpportunityStr() %></span>
<% } %>
<% if (helper.getDisplayErrorBars()) { %>
<span class="graphInfoHeader">Error Bars:</span><span id="errorBarTypeStr"><%=helper.getErrorBarTypeStr() %></span>
<% } %>
                    </div>
                    <div id="graphInfoCol2">
<%      helper.printObsCount(); %>
                    </div>
<% if (helper.getDisplayErrorBars()) { %>
		    <div id="graphInfoNote">
		       <span>Note: Error bars are calculated without the observations dropped by the standard deviation cutoff.</span>
		    </div>
<% } %>
                </div>
        	<div style="clear:both"></div>

                <div id="hidePointInfo"><span id="hidePointInfoLink">hide point info</span></div>
                <div id="pointInfo" class="clearfix">
                    <table id="pointInfoSample">
                        <tr>
                            <td id="pointInfoSampleLabel" class="pointInfoHeader">Sample: </td>
                            <td id="pointInfoSampleSelector"
                                onclick='popupSamples(<%=helper.jsonSampleNameToSeries().toString()%>, <%=helper.jsonOrderedSampleNames().toString()%>)'
                                onmouseover='this.addClassName("hover");'
                                onmouseout='this.removeClassName("hover");'>
                                <span>All Data</span>
                                <span id="pointInfoSampleSelectorImage"></span>
                            </td>
                        </tr>
                    </table>
                    
                    <table id="pointInfoCol1">
                        <tr>
                            <td id="pointInfoOpportunityLabel" class="pointInfoHeader">Opportunity: </td>
                            <td id="pointInfoOpportunitySelector">
                                <input id="pointInfoOpportunityInput" type="text" value="2" size="3" /> 
                                <span class="OppButtons">
                                    <span id="prevOppButtonWrapper"><span id="prevOppButton" title="Previous Opportunity"></span></span>
                                    <span id="nextOppButtonWrapper"><span id="nextOppButton" title="Next Opportunity"></span></span>
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td id="pointInfoValueLabel" class="pointInfoHeader">Value: </td>
                            <td id="pointInfoValue">-</td>
                        </tr>
                        <tr>
                            <td id="pointInfoPredictedLabel" class="pointInfoHeader">Predicted: </td>
                            <td id="pointInfoPredictedValue">-</td>
                        </tr>
                        <tr>
                            <td id="pointInfoObservationsLabel" class="pointInfoHeader">Observations: </td>
                            <td id="pointInfoObservations">-</td>
                        </tr>
<% if (helper.getDisplayErrorBars()) { %>
                        <tr>
                            <td id="pointInfoUpperBoundLabel" class="pointInfoHeader"><%=helper.getUpperBoundLabelStr() %></td>
                            <td id="pointInfoUpperBound">-</td>
                        </tr>
                        <tr>
                            <td id="pointInfoLowerBoundLabel" class="pointInfoHeader"><%=helper.getLowerBoundLabelStr() %></td>
                            <td id="pointInfoLowerBound">-</td>
                        </tr>
<% } %>
                    </table>
                    
                    <table id="pointInfoCol2" class="clearfix">
                        <tr id="pointInfoSkillsRow" class="infoRow">
                            <td id="pointInfoSkillsLabel" class="pointInfoHeader">KCs: </td>
                            <td id="pointInfoSkills">-</td>
                        </tr>
                        <tr id="pointInfoProblemsRow" class="infoRow">
                            <td id="pointInfoProblemsLabel" class="pointInfoHeader">Problems: </td>
                            <td id="pointInfoProblems">-</td>
                        </tr>
                        <tr id="pointInfoStepsRow" class="infoRow">
                            <td id="pointInfoStepsLabel" class="pointInfoHeader">Steps: </td>
                            <td id="pointInfoSteps">-</td>
                        </tr>
                        <tr id="pointInfoStudentsRow" class="infoRow">
                            <td id="pointInfoStudentsLabel" class="pointInfoHeader">Students: </td>
                            <td id="pointInfoStudents">-</td>      
                        </tr>
                    </table>
                    
                    <div id="pointInfoCol3" class=""> 
                        <div id="pointInfoDetailsClose">x</div>
                        <div id="pointInfoDetailsCol1Header" class="nameColumn">
                            <span id="nameColumn" class="nameColumn">Name</span>                            
                        </div>
                        <div id="pointInfoDetailsCol2Header" class="valueColumn">
                            <span id="valueColumn" class="valueColumn">Value</span>
                        </div>
                        <div id="pointInfoDetailsCol3Header" class="freqColumn">
                            <span id="freqColumn" class="freqColumn">Obs</span>
                        </div>
                        <div id="pointInfoDetailsValues">
                            <table id="pointInfoDetailsValuesTable"></table>
                        </div>
                        <div id="pointInfoDetailsMessage">Only showing the first 50 rows.</div>
                        <div id="pointInfoDetailsActionLinks">
                            <p id="lcpidActionSelect" class="actionSelectLink"><span id="lcpidActionSelectLink" class="actionSelectLink"></span></p>
                            <p id="lcpidActionRemove" class="actionRemoveLink"><span id="lcpidActionRemoveLink" class="actionRemoveLink"></span></p>
                        </div>
                    </div>
                    
                </div>
                <div id="hideObservationTable"><span id="hideObservationTableLink">hide observation table</span></div>
                <div id="observationTables">
<%      helper.printObsTables(); %>
                </div>

   <div id="thumbs">
<%      helper.produceRollupThumb(); %>
        <div id="allSkillThumb">
		<a class="<%=helper.thumbClass(helper.topItem() == null)%>" 
            	   href="LearningCurve?<%=helper.topIdLabel()%>=null&datasetId=<%=helper.datasetId()%>"
		   name="thumb_all_selected_<%=helper.allSelectedLabel()%>">
		    <img src="<%=helper.graphURL()%>" width=175 height=95 border=0 />
		</a>
        </div>
        <div style="clear:left">
           <hr>
        </div>

<%
     int numberOfThumbs = helper.numThumbs();
     Integer lcDatasetId = (Integer)helper.datasetId();
     Item topItem = helper.topItem();
     boolean hasAfmRun = helper.hasAfmRun(topItem);

     Map<String, List<LearningCurveImage>> lcImages = helper.generateImageLists();

     String categoryName = LearningCurveImage.NOT_CLASSIFIED;
     int lcRange = helper.lcRange(categoryName);

     String pageOrCategoryStr = "category";

     List<LearningCurveImage> lciList = lcImages.get(LearningCurveImage.NOT_CLASSIFIED);
     if ((lciList != null) && (lciList.size() > 0)) {
        categoryName = "";   // no title displayed when not classifying
        pageOrCategoryStr = "page";
%>
        <%@ include file="learning_curve_thumbs.jspf" %>
<%
     } else {
        lciList = lcImages.get(LearningCurveImage.CLASSIFIED_LOW_AND_FLAT);
        categoryName = LearningCurveImage.CLASSIFIED_LOW_AND_FLAT;
        lcRange = helper.lcRange(categoryName);
%>

        <%@ include file="learning_curve_thumbs.jspf" %>

<%
        lciList = lcImages.get(LearningCurveImage.CLASSIFIED_NO_LEARNING);
        categoryName = LearningCurveImage.CLASSIFIED_NO_LEARNING;
        lcRange = helper.lcRange(categoryName);
%>

        <%@ include file="learning_curve_thumbs.jspf" %>

<%
        lciList = lcImages.get(LearningCurveImage.CLASSIFIED_STILL_HIGH);
        categoryName = LearningCurveImage.CLASSIFIED_STILL_HIGH;
        lcRange = helper.lcRange(categoryName);
%>

        <%@ include file="learning_curve_thumbs.jspf" %>

<%
        lciList = lcImages.get(LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA);
        categoryName = LearningCurveImage.CLASSIFIED_TOO_LITTLE_DATA;
        lcRange = helper.lcRange(categoryName);
%>

        <%@ include file="learning_curve_thumbs.jspf" %>

<%
        lciList = lcImages.get(LearningCurveImage.CLASSIFIED_OTHER);
        categoryName = LearningCurveImage.CLASSIFIED_OTHER;
        lcRange = helper.lcRange(categoryName);
%>

        <%@ include file="learning_curve_thumbs.jspf" %>

<%
     }
%>
   </div>  <!-- id=thumbs -->

<div style="clear:both">&nbsp;</div>

<p class="showCount">&raquo; Showing
<%  int[] choices = LearningCurveJspAssistant.THUMBNAIL_CHOICES;
    for (int numThumb : choices) {
        if (numberOfThumbs == numThumb) {
%><strong><%=numThumb%></strong><%
        } else {
%><a href="LearningCurve?numberThumbs=<%=numThumb%>&datasetId=<%=helper.datasetId()%>">
<%=numThumb%></a><%
        }
        if (numThumb != choices[choices.length - 1]) {
%>, <%
        }
    } %>
thumbnails per <%=pageOrCategoryStr %>.</p>
<div id="highlighted_point_div" style="display:none; width:30px; cursor:pointer;">
    <img src="images/circle.gif">
</div>
<%
} %>
