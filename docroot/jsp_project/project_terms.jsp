<%@ include file="/doctype.jspf" %>
<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2012
  All Rights Reserved
-->
<%
// Author: Cindy Tipper
// Version: $Revision: 15660 $
// Last modified by: $Author: pls21 $
// Last modified on: $Date: 2018-10-29 10:26:17 -0400 (Mon, 29 Oct 2018) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.lang.StringBuffer"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.tou.ManageTermsServlet"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dao.TermsOfUseDao"%>
<%@page import="edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.item.ProjectItem"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseItem"%>
<%@page import="edu.cmu.pslc.datashop.item.TermsOfUseVersionItem"%>
<%@page import="edu.cmu.pslc.datashop.item.UserItem"%>

<!-- project-page-specific header stuff -->
<%@ include file="/jsp_project/project_header.jspf" %>

<%
    TermsOfUseDao touDao = DaoFactory.DEFAULT.getTermsOfUseDao();
    TermsOfUseVersionDao touVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
    List<TermsOfUseItem> projectTouList = (List<TermsOfUseItem>)touDao.findAllProjectTermsOfUse();

    boolean allowApplyAndClear = false;
    if (adminUserFlag) {
        allowApplyAndClear = true;
    }
    if (isDataProvider && hasTermsManagerRole) {
        allowApplyAndClear = true;
    }
%>

        <div id="project_content_div">
        <% if ((projectTerms != null) && allowApplyAndClear) { %>
            <h3 style="display:inline">Project Terms of Use</h3>
            <div id="clear_terms_div">
                <input type="button" id="clear_terms_button" value="Clear Terms"
                    onclick="javascript:clearTermsAreYouSure()"/>
            </div>
        <% } else { %>
            <h3>Project Terms of Use</h3>
        <% } %>

        <%if (projectTerms != null){ %>
             <p><em>Effective <%=projInfoReport.getProjectTermsEffectiveDate() %></em></p>
             <div id="project-terms-note">
             <%=projectTerms %>
             </div>
             <% if (datashopTerms != null) { %>
                <h3>DataShop Terms of Use</h3>
                <p id="datashop-terms-note">The DataShop <a href="Terms">terms of use</a> also apply.</p>
             <% } %>
        <%} else {%>
            <p id="project-terms-note">There are no additional terms associated with this project.</p>

            <% if (allowApplyAndClear) { %>
            <div id="project_terms_select_div">
                 <div id="select_terms_div">
                      <select id="project_tou_select" name="project_tou_select">
                              <option value="-1" selected>Select Terms</option>
                <%
                if (!projectTouList.isEmpty()) {

                    // Display each Project Terms Of Use in the select combo box
                    for (TermsOfUseItem projectTouItem : projectTouList) {
                        String touName = projectTouItem.getName();
                        Integer touId = (Integer)projectTouItem.getId();

                        // Only include those ToUs that are 'applied' or 'saved'.
                        if (touVersionDao.hasStatus(projectTouItem,
                            TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_APPLIED)) {
                            %>
                              <option value="<%=touId %>"><%=touName %> - applied</option>
                            <%
                         }
                         else if (touVersionDao.hasStatus(projectTouItem,
                                  TermsOfUseVersionItem.TERMS_OF_USE_VERSION_STATUS_SAVED)) {
                            %>
                              <option value="<%=touId %>"><%=touName %> - saved</option>
                            <%
                         }
                    }
                }
                %>
                      </select>
                      <input type="button" id="apply_terms_button" value="Apply Terms"
                             onclick="javascript:applyTermsToProject(<%=projectId %>)" disabled />
                 </div>   <!-- select_terms_div -->
            </div>   <!-- project_terms_select_div -->
            <% } %>

            <% if (datashopTerms != null) { %>
               <h3>DataShop Terms of Use</h3>
               <p id="datashop-terms-note">The DataShop <a href="Terms">terms of use</a> apply.</p>
            <% } %>
        <%} %>
        </div> <!-- project_content_div -->
        </div>  <!-- project-page div -->
    </td>
</tr>

<!-- Un-populated div's needed for modal windows using jQuery -->
<div id="areYouSureDialog" style="display:none">

<!-- footer -->
<%@ include file="/footer.jspf" %>
    </td>
</tr>
</table>

<div id="hiddenHelpContent" style="display:none">
<%@ include file="/help/report-level/project_page_terms.jspf" %>
</div>

</body>
</html>

