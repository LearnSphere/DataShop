<!--
  Carnegie Mellon University, Human Computer Interaction Institute
  Copyright 2005
  All Rights Reserved
-->
<%
// Author: Alida Skogsholm
// Version: $Revision: 7737 $
// Last modified by: $Author: ctipper $
// Last modified on: $Date: 2012-05-30 14:17:21 -0400 (Wed, 30 May 2012) $
// $KeyWordsOff: $
//
%>
<%@page contentType="text/html"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.util.*"%>
<%@page import="edu.cmu.pslc.datashop.dto.DatasetInfoReport"%>
<%@page import="edu.cmu.pslc.datashop.dto.PaperFile"%>
<%@page import="edu.cmu.pslc.datashop.item.FileItem"%>
<%@page import="edu.cmu.pslc.datashop.item.PaperItem"%>
<%@page import="edu.cmu.pslc.datashop.item.DatasetItem"%>
<%@page import="edu.cmu.pslc.datashop.servlet.DatasetContext"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper"%>
<%@page import="edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet"%>
<%@page import="edu.cmu.pslc.datashop.servlet.HelperFactory"%>
<%@page import="edu.cmu.pslc.datashop.servlet.NavigationHelper"%>

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
    String acknowledgment = "";
    int preferred_paper_id = 0;
    String citation = "";
    String datasetName = "";
    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
    DatasetInfoReportHelper datasetInfoHelper = HelperFactory.DEFAULT.getDatasetInfoReportHelper();
    DatasetContext datasetContext  = (DatasetContext)session.getAttribute("datasetContext_"
            + request.getParameter("datasetId"));
    DatasetInfoReport datasetInfoReport = datasetInfoHelper.getDatasetInfo(datasetContext.getDataset());
    DatasetItem datasetItem = datasetContext.getDataset();

    acknowledgment = (datasetItem.getAcknowledgment() != null)
            ? datasetItem.getAcknowledgment() : "";
    acknowledgment = cleanInputText(acknowledgment);
    if (datasetInfoReport != null){
        citation = (datasetInfoReport.getCitation() != null)
            ? datasetInfoReport.getCitation() : "";
        citation = citation.replaceAll("\\n", "<br>");
    }

    %>
    <div id="citationInfo">
    <ul class="toc">
        <li><a href="#citation">Citation/Acknowledgement</a></li>
        <li><a href="#citing">Citing the DataShop web application and repository</a></li>
    </ul>
    <h3 id="citation">Citation/Acknowledgment</h3>
    <p>If you publish research based on this dataset,
       please include the following

     <% if (!citation.equals(""))  {
         out.println("citation and ");
     }
         out.print("acknowledgment:");
     %></p>

     <% if (!citation.equals("")) {%>
         <h4>Citation</h3>
         <p class="citation"><%=citation %></p>
     <%} %>

     <h4>Acknowledgment</h3>
     <%
     // if neither citation or acknowledgment is specified, display the following:
     datasetName = datasetItem.getDatasetName();

     if (!acknowledgment.equals("")) {%>
         <p class="acknowledgment"><%=acknowledgment %> We used the '<%=datasetName %>' dataset accessed via DataShop (Koedinger et al., 2010).</p>
         <p>or </p>
         <p class="acknowledgment"><%=acknowledgment %> We used the '<%=datasetName %>' dataset accessed via DataShop (pslcdatashop.org).</p>
     <%} else {%>
         <p class="acknowledgment">We used the '<%=datasetName %>' dataset accessed via DataShop (Koedinger et al., 2010).</p>
         <p>or </p>
         <p class="acknowledgment">We used the '<%=datasetName %>' dataset accessed via DataShop (pslcdatashop.org).</p>
     <% } %>

     <h3 id="citing">Citing the DataShop web application and repository</h3>
     <p>To cite the DataShop web application and repository, please include the following reference in your publication:</p>

     <p class="citation">Koedinger, K.R., Baker, R.S.J.d., Cunningham, K., Skogsholm, A., Leber, B., Stamper, J. (2010)
     A Data Repository for the EDM community: The PSLC DataShop. In Romero, C., Ventura, S., Pechenizkiy, M., Baker, R.S.J.d.
     (Eds.) Handbook of Educational Data Mining. Boca Raton, FL: CRC Press.</p>

     <p>You might also cite the DataShop URL in the text of your paper:</p>

     <p class="acknowledgment">For exploratory analysis, I used the PSLC DataShop, available at
     <a href="http://pslcdatashop.org"></a>http://pslcdatashop.org (Koedinger et al., 2010).</p>

     <p>Additional information on citing DataShop is available here:
      <a href="http://www.pslcdatashop.org/help?page=citing">http://www.pslcdatashop.org/help?page=citing</a></p>

    </div>

</table>
