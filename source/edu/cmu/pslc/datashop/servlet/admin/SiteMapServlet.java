/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.util.ServerNameUtils;

/**
 * This servlet is for returning the DataShop Sitemap, sitemap.xml.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SiteMapServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Constant. */
    private static final String SITEMAP_LIST = "/sitemap_list.txt";
    /** Constant. */
    private static final String NAMESPACE_URI = "http://www.sitemaps.org/schemas/sitemap/0.9";
    /** Constant. */
    private static final String PROJECT_URL = "https://pslcdatashop.web.cmu.edu/Project?id=";
    /** Constant. */
    private static final String DATASET_URL =
        "https://pslcdatashop.web.cmu.edu/DatasetInfo?datasetId=";

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));

        PrintWriter out = resp.getWriter();
        InputStream is = null;
        BufferedReader br = null;

        try {
            // sitemap.xml isn't available (relevant) on QA
            if (ServerNameUtils.isQA()) {
                out.append("");
                return;
            }

            Document document = new Document();
            Namespace ns = Namespace.getNamespace(NAMESPACE_URI);
            Element urlset = new Element("urlset", ns);

            // Get static content.
            is = getServletContext().getResourceAsStream(SITEMAP_LIST);
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                Element url = new Element("url");
                url.addContent(new Element("loc").addContent(line));
                urlset.addContent(url);
            }

            // Add projects and released datasets.
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            List<ProjectItem> projectList = projectDao.findAll();
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            List<DatasetItem> datasetList = datasetDao.findDatasetsForSiteMap();

            for (ProjectItem p : projectList) {
                String projectUrl = PROJECT_URL + p.getId();
                Element url = new Element("url");
                url.addContent(new Element("loc").addContent(projectUrl));
                urlset.addContent(url);
            }

            for (DatasetItem d : datasetList) {
                String datasetUrl = DATASET_URL + d.getId();
                Element url = new Element("url");
                url.addContent(new Element("loc").addContent(datasetUrl));
                urlset.addContent(url);
            }

            document.addContent(urlset);

            XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());
            String theXml = outputter.outputString(document);
            out.append(theXml);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) { out.close(); }
            if (is != null) { is.close(); }
            if (br != null) { br.close(); }
            logger.debug("doPost end");
        }
    }
}
