<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2011
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 8309 $
// Last modified by: $Author: bleber $
// Last modified on: $Date: 2012-12-05 12:06:26 -0500 (Wed, 05 Dec 2012) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.dao.DaoFactory"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.dto.ProjectInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.ProjectHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>

<%
    String projectName = "";
    String dataShopTerms = "";
    String projectTerms = "";
    String dataShopTermsEffectiveDate = "";
    String projectTermsEffectiveDate = "";

    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();

    DatasetContext datasetContext  = (DatasetContext)session.getAttribute("datasetContext_"
            + request.getParameter("datasetId"));

    /* (DS1427) Retrieving dataItem directly from DAO if necessary (i.e. not loggined-in).*/
    DatasetItem datasetItem = null;
    if (datasetContext != null) {
        datasetItem = datasetContext.getDataset();
    } else {
    	datasetItem =  DaoFactory.DEFAULT.getDatasetDao().get( Integer.valueOf(request.getParameter("datasetId")));
    }

    ProjectInfoReport projectInfoReport = projHelper.getProjectInfoReport(datasetItem);
    if (projectInfoReport != null){
        projectName = (projectInfoReport.getName() != null)
        ? projectInfoReport.getName() : "";

        dataShopTerms = (projectInfoReport.getDataShopTerms() != null)
        ? projectInfoReport.getDataShopTerms() : "";

        projectTerms = (projectInfoReport.getProjectTerms() != null)
        ? projectInfoReport.getProjectTerms() : "";

        dataShopTermsEffectiveDate = (projectInfoReport.getDataShopTermsEffectiveDate() != null)
        ? projectInfoReport.getDataShopTermsEffectiveDate() : "";

        projectTermsEffectiveDate = (projectInfoReport.getProjectTermsEffectiveDate() != null)
        ? projectInfoReport.getProjectTermsEffectiveDate() : "";
    }


    %>
    <div id="termsOfUse">

<%
    if (projectTerms == "" && dataShopTerms == "") {
%>
    <h3 id="project">Terms of Use</h3>
    <p>There are no terms associated with DataShop or this project at this time.</p>
<%
    } else {%>
        <h3 id="project">Project Terms of Use</h3>
<%
        if (projectTerms != "") {%>
            <p>The following terms apply for this dataset and the other datasets in the project <%=projectName %>.</p>
            <p class="effectivedate">Effective <%=projectTermsEffectiveDate %></p>
            <div id="project-terms-text">
            <%=projectTerms %>
            </div>
            <%
            if (dataShopTerms != "") {
            %> 
            <h3 id="datashop">DataShop Terms of Use</h3>
            <p id="datashop-terms-note">The DataShop <a href="Terms">terms of use</a> also apply.</p>
<%          }
        } else { %>
        <p>There are no additional terms associated with this project.</p>
<%
            if (dataShopTerms != "") {%>
            <h3 id="datashop">DataShop Terms of Use</h3>
            <p id="datashop-terms-note">The DataShop <a href="Terms">terms of use</a> apply.</p>
            </div>
            <%
            }
        }
    }
%>

    </div>
</table>
