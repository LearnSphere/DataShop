/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseDao;
import edu.cmu.pslc.datashop.dao.TermsOfUseVersionDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserTermsOfUseMapDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectHelper;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;
import edu.cmu.pslc.datashop.servlet.tou.TermsServlet;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Command that check whether the user has agreed to terms of use.
 * This command uses (although not required) the value userItem in the
 * accessContext object which is set by LoadUserItemCommand.
 *
 * @author Young Suk Ahn
 * @version $Revision: 13881 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-02-14 16:41:12 -0500 (Tue, 14 Feb 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CheckAgreedToTermsCommand implements Command {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Terms Agree JSP name. */
    public static final String TERMS_AGREE_JSP_NAME = "/terms_agree.jsp";
    /** Same, for LearnSphere. */
    public static final String LS_TERMS_AGREE_JSP_NAME = "/jsp_workflows/ls_terms_agree.jsp";

    @Override
    public boolean execute(Context ctx) {

        AccessContext accessCtx = (AccessContext)ctx;

        boolean hasAgreedToTerms = false;
        UserItem userItem = accessCtx.getUserItem();

        // Same effect as: if (isLoggedIn(req)) {}
        if (userItem != null) {
            boolean both = false;
            /*
             * If dataset item was retrieved (i.e. LoadDatsetCommand executed),
             * then do TU check for the dataset too.
             */
            if (accessCtx.containsAttribute(AccessContext.KEY_DATASET_ITEM)) {
                both = true;
            }
            hasAgreedToTerms = hasAgreedToTerms(accessCtx, both);
        } else {
            // If user has not logged in return true. see original code AbstractServlet:1267~1270
            hasAgreedToTerms = true;
        }

        if (!hasAgreedToTerms) {
            try {
                CheckAgreedToTermsCommand.forwardTermsAgree(
                        accessCtx.getHttpRequest(), accessCtx.getHttpResponse());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * Returns true if the user is authorized for dataset.
     * This attribute is set by LoadDatasetCommand
     * @param accessCtx the Access Context
     * @return true if authorized, false otherwise.
     */
    public Boolean isAuthorizedForDataset(AccessContext accessCtx) {
        boolean isAuthorized = false;
        if (accessCtx.containsAttribute(AccessContext.KEY_IS_AUTHORIZED_FOR_DS)) {
            isAuthorized = true;
        }
        return isAuthorized;
    }

    /* Codes below was brought from AbstractSevlet because terms of use
     * is a cross cutting concern that many servlet uses but no all.
     * The concrete servlets should be freed from hard-coding TU check,
     * but the TU policy should be configurable from a central point
     * (i.e. Filter).
     */

    /**
     * Forward to the terms of use agree servlet.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException possible exception on a forward
     * @throws IOException possible exception on a forward
     */
    public static void forwardTermsAgree(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher disp;
        String originalUrl = req.getRequestURI();

        String termsAgreeJsp = TERMS_AGREE_JSP_NAME;
        if (originalUrl.indexOf("LearnSphere") > 0)  {
            termsAgreeJsp = LS_TERMS_AGREE_JSP_NAME;
        }

        disp = req.getSession().getServletContext().getRequestDispatcher(termsAgreeJsp);
        disp.forward(req, resp);
    }

    /**
     * Determine if the use r  * Look in the session datasetContext and/or in the servlet request.
     * @param accessCtx the AccessContext sent from AccessFilter
     * @param bothFlag true indicates to check both DataShop and Project ToU, false just DS
     * @return true if the user is authorized to view the selected dataset
     */
    protected boolean hasAgreedToTerms(AccessContext accessCtx, boolean bothFlag) {
        boolean hasAgreedToTerms = false;

        HttpServletRequest req = accessCtx.getHttpRequest();
        String username = (String)accessCtx.getUserItem().getId();
        String prefix = "hasAgreedToTerms, user (" + username + ") ";

        HttpSession httpSession = req.getSession();
        String originalUrl = req.getRequestURI();
        String queryString = req.getQueryString();
        if (queryString != null) {
            originalUrl += "?" + req.getQueryString();
        }
        httpSession.setAttribute(TermsServlet.TERMS_URL_ATTRIB, originalUrl);

        if (hasAgreedToDataShopTerms(req, username)) {
            LogUtils.logDebug(logger, prefix + "has agreed to datashop terms");
            if (bothFlag) {
                //if user is authorized for dataset, then check project terms
                boolean isAuthorizedForDataset = false;
                if (accessCtx.containsAttribute(AccessContext.KEY_IS_AUTHORIZED_FOR_DS)) {
                    isAuthorizedForDataset = accessCtx.isAuthorizedForDataset();
                }
                if (isAuthorizedForDataset) {
                    if (accessCtx.getDatasetItem() != null) {
                        Integer datasetId = (Integer) accessCtx.getDatasetItem().getId();
                        httpSession.setAttribute(TermsServlet.TERMS_DATASET_ID_ATTRIB, datasetId);
                        if (hasAgreedToDatasetTerms(req, username, datasetId)) {
                            hasAgreedToTerms = true;
                            LogUtils.logTrace(logger, prefix + "has agreed to project terms");
                        } else {
                            hasAgreedToTerms = false;
                            getProjectTermsOfUse(req, username, datasetId);
                            LogUtils.logDebug(logger, prefix, "has NOT agreed to project terms");
                        }
                    } else {
                        logger.error(prefix + "datasetContext is unexpectedly null.");
                    }
                } else {
                    hasAgreedToTerms = true;
                }
            } else {
                hasAgreedToTerms = true;
            }
        } else {
            getDataShopTermsOfUse(req, username);
            LogUtils.logDebug(logger, prefix, "has NOT agreed to datashop terms");
        }

        return hasAgreedToTerms;
    }

    /**
     * Determine if the user has agreed to the DataShop terms of use if one exists.
     * @param req the HTTP servlet request
     * @param username the ID of the user, account ID
     * @return true if the user has agreed or none exist, false otherwise
     */
    private boolean hasAgreedToDataShopTerms(
            HttpServletRequest req, String username) {
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        return projHelper.hasAgreedToDataShopTerms(username);
    }

    /**
     * Determine if the user has agreed to the Project terms of use if one exists.
     * @param req the HTTP servlet request
     * @param username the ID of the user, account ID
     * @param datasetId the ID of the dataset
     * @return true if the user has agreed or none exist, false otherwise
     */
    private boolean hasAgreedToDatasetTerms(
            HttpServletRequest req, String username, Integer datasetId) {
        ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);
        ProjectItem projectItem = datasetItem.getProject();
        if (projectItem != null) {
            return projHelper.hasAgreedToDatasetTerms(username, (Integer)projectItem.getId(),
                                                      (Integer)datasetItem.getId());
        } else {
            return true;
        }
    }

    /**
     * Get project-specific current terms of use and put it in the HTTP session.
     * @param req HttpServletRequest
     * @param username the account ID
     * @param datasetId the ID of the dataset
     */
    protected void getProjectTermsOfUse(
            HttpServletRequest req,
            String username,
            Integer datasetId) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = null;
        if (username == null || username.length() == 0) {
            userItem = userDao.findOrCreateDefaultUser();
        } else {
            userItem = userDao.find(username);
        }
        String ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";

        String termsDate = "";
        String termsText = "";

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);
        ProjectItem projectItem = datasetItem.getProject();
        Integer projectId = (Integer)projectItem.getId();
        projectItem = projectDao.get(projectId);
        String projectName = projectItem.getProjectName();

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getProjectTerms(projectId, null);

        if (versionItem != null) {
            termsDate = TermsServlet.DISPLAY_DATE_FORMAT.format(versionItem.getAppliedDate());
            termsText = versionItem.getTerms();

            TermsOfUseDao termsDao = DaoFactory.DEFAULT.getTermsOfUseDao();
            TermsOfUseItem termsItem = termsDao.get((Integer)versionItem.getTermsOfUse().getId());
            String termsName = termsItem.getName();
            UserLogger.log(null, userItem, UserLogger.VIEW_TERMS,
                    "Project terms (" + termsName + ") version: " + versionItem.getVersion()
                    + " (current)" + ipAddress, false);
        }

        TermsOfUseDao termsDao = DaoFactory.DEFAULT.getTermsOfUseDao();
        TermsOfUseItem termsItem = termsDao.get((Integer)versionItem.getTermsOfUse().getId());
        UserTermsOfUseMapDao userTermsOfUseMapDao = DaoFactory.DEFAULT.getUserTermsOfUseMapDao();
        boolean updateFlag = userTermsOfUseMapDao.hasAgreedBefore(username, termsItem);

        HttpSession httpSession = req.getSession();
        httpSession.setAttribute(TermsServlet.TERMS_TYPE_ATTRIB, TermsOfUseItem.PROJECT_TERMS);
        httpSession.setAttribute(TermsServlet.TERMS_TEXT_ATTRIB, termsText);
        httpSession.setAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB, termsDate);
        httpSession.setAttribute(TermsServlet.TERMS_PROJECT_NAME_ATTRIB, projectName);
        httpSession.setAttribute(TermsServlet.TERMS_UPDATE_FLAG_ATTRIB, updateFlag);
    }

    /**
     * Get DataShop's current terms of use and put it in the HTTP session.
     * @param req HttpServletRequest
     * @param username the account ID
     */
    protected void getDataShopTermsOfUse(
            HttpServletRequest req,
            String username) {

        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem userItem = null;
        if (username == null || username.length() == 0) {
            userItem = userDao.findOrCreateDefaultUser();
        } else {
            userItem = userDao.find(username);
        }
        String ipAddress = " [ipaddress:" + req.getRemoteAddr() + "]";

        String termsDate = "";
        String termsText = "";

        TermsOfUseVersionDao termsVersionDao = DaoFactory.DEFAULT.getTermsOfUseVersionDao();
        TermsOfUseVersionItem versionItem = termsVersionDao.getDataShopTerms(null);

        if (versionItem != null) {
            termsDate = TermsServlet.DISPLAY_DATE_FORMAT.format(versionItem.getAppliedDate());
            termsText = versionItem.getTerms();

            UserLogger.log(null, userItem, UserLogger.VIEW_TERMS,
                    "DataShop terms, version: " + versionItem.getVersion()
                    + " (current)" + ipAddress, false);
        }

        HttpSession httpSession = req.getSession();
        httpSession.setAttribute(TermsServlet.TERMS_TYPE_ATTRIB, TermsOfUseItem.DATASHOP_TERMS);
        httpSession.setAttribute(TermsServlet.TERMS_TEXT_ATTRIB, termsText);
        httpSession.setAttribute(TermsServlet.TERMS_EFFECTIVE_DATE_ATTRIB, termsDate);
        httpSession.setAttribute(TermsServlet.TERMS_UPDATE_FLAG_ATTRIB, false);
    }
}
