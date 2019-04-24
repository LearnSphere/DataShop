/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.tou;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;

import edu.cmu.pslc.datashop.helper.UserLogger;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserTermsOfUseMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserTermsOfUseMapItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;

/**
 * Get the user's agreement and save to the database.
 * Then forward on the the original servlet (url).
 *
 * @author Alida Skogsholm
 * @version $Revision: 9597 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-07-16 10:55:07 -0400 (Tue, 16 Jul 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TermsAgreeServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

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
        logger.debug("doPost begin :: " + getDebugParamsString(req));
        PrintWriter out = null;
        HttpSession httpSession = req.getSession(true);
        try {
            setEncoding(req, resp);

            //If logged in, then check that the user agreed, save the agreement,
            //and go to the original URL
            if (isLoggedIn(req)) {
                UserItem userItem = getUser(req);

                String termsType = (String)httpSession.getAttribute(
                        TermsServlet.TERMS_TYPE_ATTRIB);
                String servletName = (String)httpSession.getAttribute(
                        TermsServlet.TERMS_URL_ATTRIB);
                Integer datasetId = (Integer)httpSession.getAttribute(
                        TermsServlet.TERMS_DATASET_ID_ATTRIB);

                if (termsType.equals(TermsOfUseItem.DATASHOP_TERMS)) {
                    agreeToDataShopTerms(userItem);
                } else {
                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    DatasetItem datasetItem = navHelper.getDataset(datasetId);
                    ProjectItem projectItem = datasetItem.getProject();

                    agreeToProjectTerms(projectItem, userItem, datasetItem);
                }
                resp.sendRedirect(servletName);

            //If not logged in, then go to the home page
            } else {
                redirectHome(req, resp);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Save that the user agreed to the current Terms of Use for a given project,
     * when accessing the given dataset.
     * @param projectItem the project item
     * @param userItem the user item
     * @param datasetItem the dataset item
     */
    private void agreeToProjectTerms(ProjectItem projectItem,
                                     UserItem userItem, DatasetItem datasetItem) {
        Integer projectId = (Integer)projectItem.getId();
        Integer datasetId = (Integer)datasetItem.getId();
        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        DatasetUserTermsOfUseMapDao dsUserTouMapDao =
            DaoFactory.DEFAULT.getDatasetUserTermsOfUseMapDao();
        UserTermsOfUseMapDao userTouMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();

        TermsOfUseVersionItem versionItem = termsVersionDao.getProjectTerms(projectId, null);
        if (versionItem != null) {
            UserTermsOfUseMapItem userTouMapItem = new UserTermsOfUseMapItem();
            userTouMapItem.setTermsOfUseExternal(versionItem.getTermsOfUse());
            userTouMapItem.setUserExternal(userItem);
            userTouMapItem.setTermsOfUseVersion(versionItem);
            userTouMapItem.setDate(new Date());
            userTouMapDao.saveOrUpdate(userTouMapItem);

            DatasetUserTermsOfUseMapItem dsUserTouMapItem = new DatasetUserTermsOfUseMapItem();
            dsUserTouMapItem.setTermsOfUseExternal(versionItem.getTermsOfUse());
            dsUserTouMapItem.setUserExternal(userItem);
            dsUserTouMapItem.setDatasetExternal(datasetItem);
            dsUserTouMapItem.setTermsOfUseVersion(versionItem);
            dsUserTouMapItem.setDate(new Date());
            dsUserTouMapDao.saveOrUpdate(dsUserTouMapItem);

            TermsOfUseDao termsDao = DaoFactory.DEFAULT.getTermsOfUseDao();
            TermsOfUseItem termsItem = termsDao.get((Integer)versionItem.getTermsOfUse().getId());
            String termsName = termsItem.getName();
            Integer version = versionItem.getVersion();
            UserLogger.log(null, userItem, UserLogger.AGREE_TERMS,
                           "Project terms (" + termsName + ") version: " + version);
            logger.info("Saved user (" + userItem.getId()
                        + ") agreement to Project terms version " + version
                        + " for dataset " + (Integer)datasetItem.getId());
        }
    }

}
