<%@page contentType="text/html"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProfilerOptions"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ListIterator"%>

<div class="navigationBoxHeader"><h2 class="nav_header">Performance Profiler</h2></div>
<%
    String ppViewByCategory = info.getPerformanceProfilerContext().getViewByCategory();
    String ppViewByType = info.getPerformanceProfilerContext().getViewByType();
    String ppSortBy = info.getPerformanceProfilerContext().getSortBy();
    Boolean ppDisplayPredicted = info.getPerformanceProfilerContext().getDisplayPredicted();
    Boolean ppSortAscending = info.getPerformanceProfilerContext().getSortByAscendingDirection();
    Integer ppTopLimit = info.getPerformanceProfilerContext().getTopLimit();
    Integer ppBottomLimit = info.getPerformanceProfilerContext().getBottomLimit();
    Boolean ppDisplayUnmapped = info.getPerformanceProfilerContext().getDisplayUnmapped();

    if (ppViewByCategory == null || ppViewByCategory.compareTo("") == 0) {
        ppViewByCategory = ProfilerOptions.TYPE_PROBLEM;
    }
    if (ppViewByType == null || ppViewByType.compareTo("") == 0) {
        ppViewByType = ProfilerOptions.VIEW_ERROR_RATE;
    }
    if (ppSortBy == null || ppSortBy.compareTo("") == 0) {
        ppSortBy = ProfilerOptions.SORT_BY_ERROR_RATE;
    }
    if (ppDisplayPredicted == null) {
        ppDisplayPredicted = Boolean.FALSE;
    }
    if (ppSortAscending == null) {
        ppSortAscending = Boolean.TRUE;
    }
    if (ppDisplayUnmapped == null) {
        ppDisplayUnmapped = Boolean.TRUE;
    }
%>
    <div class="wrapper">
        
    <h3>Performance Metric</h3>
    <select id="ppPerformanceMetric" name="ppPerformanceMetric">
    <%
    for(Iterator it = ProfilerOptions.VIEW_OPTIONS.listIterator(); it.hasNext();) {
        String sortOption = (String)it.next();
        out.print("<option value=\"" + sortOption + "\" ");
        if (sortOption.compareTo(ppViewByType) == 0) {
            out.print("selected");
        }
        out.print(" >" + sortOption + "</option>");
    }
    %>
    </select>

    <h3>View by</h3>
    <select id="ppViewBy" name="ppViewBy">
    <%
        for(Iterator it = ProfilerOptions.TYPE_OPTIONS.listIterator(); it.hasNext();) {
            String sortOption = (String)it.next();
            out.print("<option value=\"" + sortOption + "\" ");
            if (sortOption.compareTo(ppViewByCategory) == 0) {
                out.print("selected");
            }
            out.print(" >" + sortOption + "</option>");
        }
        
        //also include a list of all dataset level titles as options.
        List levelTitles = DaoFactory.DEFAULT.getDatasetLevelDao()
            .getDatasetLevelTitles((Integer)datasetItem.getId());
        for (Iterator it = levelTitles.iterator(); it.hasNext();) {
            String typeString = (String)it.next();
            if (typeString != null) {
                typeString = typeString.trim();
                if (typeString.length() > 0) {
                    out.print("<option value=\"" + typeString + "\" ");
                    if (typeString.compareTo(ppViewByCategory) == 0) {
                        out.print("selected");
                    }
                }
                out.print(" >" + typeString + "</option>");
            }
        }
    %>
    </select>
    
    <h3>Sort by <img id="ppSortAscending" src="images/grid/up.gif" title="Ascending"> </h3>
    <select id="ppSortBy" name="ppSortBy">
    <%
    for(Iterator it = ProfilerOptions.SORT_OPTIONS.listIterator(); it.hasNext();) {
        String sortOption = (String)it.next();
        out.print("<option value=\"" + sortOption + "\" ");
        if (sortOption.compareTo(ppSortBy) == 0) {
            out.print("selected");
        }
        out.print(" >" + sortOption + "</option>");
    }
    %>
    </select>
    
    <!--  
    <h3>Order</h3>
    <select id="ppSortAscending" name="ppSortAscending">
        <option value="true"
            <% if (ppSortAscending.booleanValue()) { out.print("selected"); } %>
        >Ascending</option>
        <option value="false"
            <% if (!ppSortAscending.booleanValue()) { out.print("selected"); } %>
        >Descending</option>
    </select>
    -->

    <h3>Show</h3>
    <input type="checkbox" name="ppDisplayPredicted" id="ppDisplayPredicted"
    <% if (ppDisplayPredicted.booleanValue()) { out.print("checked"); } %> />
    <label for="ppDisplayPredicted">Predicted Error Rate</label><br />
        
    <input id="ppDisplayUnmapped" name="ppDisplayUnmapped" type="checkbox"
    <% if (ppDisplayUnmapped.booleanValue()) { out.print("checked"); } %> />
    <label for="ppDisplayUnmapped">Steps without a KC</label><br />

    <%  
    Integer minStuds = info.getPerformanceProfilerContext().getMinStudents();
    Integer minProbs = info.getPerformanceProfilerContext().getMinProblems();
    Integer minSteps = info.getPerformanceProfilerContext().getMinSteps();
    Integer minSkils = info.getPerformanceProfilerContext().getMinSkills();
    %>
    <h3 id="ppOnlyShowRowsLabel">Only show rows with at least ...</h3>
    
    <form method="POST" name="cutoffMinsForm" action="" id="cutoffMins">
        <input type="text" name="minStudents" id="minStudents" class="valueInput"
            value="<%= (minStuds != null && minStuds > 0) ? minStuds : "" %>" size="2" />
        <span class="oppLabel" >Students</span>
        <input class="clearButton" type="button" name="minStudentsClear" id="minStudentsClear" value="Clear" /><br />
        
        <input type="text" name="minProblems" id="minProblems" class="valueInput"
            value="<%= (minProbs != null && minProbs > 0) ? minProbs : "" %>" size="2" />
        <span class="oppLabel" >Problems</span>
        <input class="clearButton" type="button" name="minProblemsClear" id="minProblemsClear" value="Clear" /><br />
        
        <input type="text" name="minSteps" id="minSteps" class="valueInput"
            value="<%= (minSteps != null && minSteps > 0) ? minSteps : "" %>" size="2" />
        <span class="oppLabel" >Steps</span>
        <input class="clearButton" type="button" name="minStepsClear" id="minStepsClear" value="Clear" /><br />

        <input type="text" name="minSkills" id="minSkills" class="valueInput"
            value="<%= (minSkils != null && minSkils > 0) ? minSkils : "" %>" size="2" />
        <span class="oppLabel" >KCs</span>
        <input class="clearButton" type="button" name="minSkillsClear" id="minSkillsClear" value="Clear" /><br />

        <input type="submit" name="cutoffMinsSet" id="cutoffMinsSet" value="Refresh Graph" class="native-button" />
    </form>
    
    <script type="text/javascript">initPPChangeHandlers();</script>
</div>
