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

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.ErrorServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.ProjectHelper;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * This command Loads datasetItem based on the HttpRequest's 'datasetId' parameter.
 * It first checks for DatasetContext in the session, if not found, retrieves
 * the dataset from database through DAO.
 * Once retrieved, sets in the HttpRequest's attribute 'datasetItem' for
 * subsequent use.
 *
 * @author Young Suk Ahn
 * @version $Revision: 13946 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-02-23 12:03:58 -0500 (Thu, 23 Feb 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoadDatsetCommand implements Command {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(LoadDatsetCommand.class);

    @Override
    public boolean execute(Context ctx) {

        AccessContext accessCtx = (AccessContext)ctx;

        String datasetId = accessCtx.getHttpRequest().getParameter("datasetId");
        accessCtx.setDatasetId(datasetId);

        UserItem userItem = accessCtx.getUserItem();

        DatasetContext datasetContext = null;
        if (userItem != null) {
                datasetContext  = getDatasetContext(userItem,
                        accessCtx.getHttpRequest(), accessCtx.getHttpResponse());
        }

        DatasetItem datasetItem = null;
        if (datasetContext != null) {
            datasetItem = datasetContext.getDataset();
        } else {
            if (datasetId != null && datasetId.matches("\\d+")) {
                datasetItem =  DaoFactory.DEFAULT.getDatasetDao().get(
                        Integer.valueOf(datasetId));
            }
        }

        // Deleted datasets
        if (datasetItem == null
                || (datasetItem.getDeletedFlag() != null
                    && datasetItem.getDeletedFlag())) {

            logger.info("LoadDatsetCommand attempted access on deleted dataset.");
            try {

                logger.info("LoadDatsetCommand forwarding to " + ErrorServlet.ERROR_PAGE_JSP_NAME);
                LoadDatsetCommand.forwardDatasetNotFound(
                accessCtx.getHttpRequest(), accessCtx.getHttpResponse());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        boolean dsAdmin = (userItem != null) ? userItem.getAdminFlag() : false;

        // Datasets in the 'Remote Datasets' project only available to DS admin.
        boolean isRemoteDataset = getIsRemoteDataset(datasetItem);
        if (isRemoteDataset && !dsAdmin) {
            logger.info("LoadDatsetCommand attempted access on remote dataset.");
            try {

                logger.info("LoadDatsetCommand forwarding to " + ErrorServlet.ERROR_PAGE_JSP_NAME);
                LoadDatsetCommand.forwardDatasetNotFound(accessCtx.getHttpRequest(),
                                                         accessCtx.getHttpResponse());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Unreleased datasets. Only available to DS admin, project admin and uploader.
        boolean released = (datasetItem.getReleasedFlag() != null)
            ? datasetItem.getReleasedFlag() : false;
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        boolean projectAdmin = authDao.isProjectAdmin(userItem, datasetItem.getProject());
        boolean isUploader = getIsUploader(datasetItem, userItem);
        if (!released && !(dsAdmin || projectAdmin || isUploader)) {
            logger.info("LoadDatsetCommand attempted access on unreleased dataset.");
            try {

                logger.info("LoadDatsetCommand forwarding to " + ErrorServlet.ERROR_PAGE_JSP_NAME);
                LoadDatsetCommand.forwardDatasetNotFound(accessCtx.getHttpRequest(),
                                                         accessCtx.getHttpResponse());
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        accessCtx.setDatasetItem(datasetItem);

        boolean isAuthorizedForDataset = false;
        if (accessCtx.getDatasetItem() != null) {
            if (userItem != null) {
                String remoteUser = (userItem != null) ? (String)userItem.getId() : null;

                ProjectHelper projHelper = HelperFactory.DEFAULT.getProjectHelper();
                isAuthorizedForDataset = projHelper.isAuthorized(remoteUser,
                        (Integer)datasetItem.getId());
                logger.debug("isAuthorizedForDataset = " + isAuthorizedForDataset);
            }
        }
        accessCtx.setAuthorizedForDataset(isAuthorizedForDataset);

        return false;
    }

    /**
     * Helper method to determine if a dataset is in the 'Remote Datasets'
     * project.
     * @param datasetItem the dataset
     * @return flag indicating if the dataset is in the project
     */
    private boolean getIsRemoteDataset(DatasetItem datasetItem) {
        ProjectItem projectItem = datasetItem.getProject();
        if (projectItem == null) { return false; }
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        projectItem = projectDao.get((Integer)projectItem.getId());
        return (projectItem.getProjectName().equals(ProjectItem.REMOTE_DATASETS));
    }

    /**
     * Helper method to determine if a dataset was uploaded by the
     * specified user.
     * @param datasetItem the dataset
     * @param userItem the user
     * @return flag indicating if a dataset was uploaded by the user
     */
    private boolean getIsUploader(DatasetItem datasetItem, UserItem userItem) {
        if (userItem == null) { return false; }

        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        ImportQueueItem iqItem = iqDao.findByDataset(datasetItem);
        if (iqItem == null) { return false; }

        iqItem = iqDao.get((Integer)iqItem.getId());
        UserItem uploader = iqItem.getUploadedBy();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        uploader = userDao.get((String)uploader.getId());
        if (uploader == null) { return false; }

        return uploader.equals(userItem);
    }

    /**
     * Overload the getDatasetContext method to first get the session from the request.
     * This method will return null if unsuccessful in determining dataset id.
     * @param userItem the user item
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @return the DatasetContext object
     */
    protected DatasetContext getDatasetContext(UserItem userItem,
            HttpServletRequest req, HttpServletResponse resp) {
        return getDatasetContextStatic(userItem, req, resp);
    }
    public static DatasetContext getDatasetContextStatic(UserItem userItem,
            HttpServletRequest req, HttpServletResponse resp) {
        HttpSession httpSession = req.getSession(true);
        String datasetIdString = req.getParameter("datasetId");

        DatasetContext datasetContext = null;
        //see if we already saved it as an attribute.
        if (datasetIdString == null) {
            datasetIdString = (String)req.getAttribute("datasetId");
        }

        if (datasetIdString == null
                || datasetIdString.equals("") || datasetIdString.equals("null")) {
            logger.debug("Dataset ID parameter was blank, returning null context.");
            return null;
        } else if (datasetIdString.matches("\\d+")) {
            // The dataset id is an integer so get the dataset item, if it exists
            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            DatasetItem datasetItem = navHelper.getDataset(Integer.valueOf(datasetIdString));
            if (datasetItem == null
                || (datasetItem.getDeletedFlag() != null
                    && datasetItem.getDeletedFlag())) {
                // If one doesn't return, then set the context to null
                // as the dataset may have just been deleted and still be in the context
                httpSession.setAttribute("datasetContext_" + datasetIdString, null);
                logger.warn("DatasetId from deleted dataset: " + datasetIdString);
                return null;
            } else {
                req.setAttribute("datasetId", datasetIdString);
                datasetContext =
                    (DatasetContext)httpSession.getAttribute("datasetContext_" + datasetIdString);
                // create a new context if one doesn't exist
                if (datasetContext == null) {
                    LogUtils.logDebug(logger, "DatasetContext null, creating new for dataset "
                            , datasetIdString);
                    // the dataset is accessible and loaded into the context
                    // so set the info for the dataset and log the user action
                    AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
                    String authorizationLevel = authDao.getAuthLevel(userItem, datasetItem);
                    datasetContext = new DatasetContext(
                        datasetItem, userItem, authorizationLevel);
                    navHelper.initializeAll(datasetContext);
                    setInfo(req, datasetContext);
                    // Trac #71. Only log if user has access to dataset.
                    // Trac #308. Also log if user is DataShop Admin.
                    if ((authorizationLevel != null) || userItem.getAdminFlag()) {
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                       UserLogger.SELECT_DATASET);
                    }
                } else {
                    // Make sure user within DatasetContext is correct. Trac #298.
                    UserItem dcUser = datasetContext.getUser();
                    if ((dcUser == null) || !dcUser.equals(userItem)) {
                        logger.debug("Updating logged in user: from "
                                     + dcUser + " to " + userItem);
                        datasetContext.setUser(userItem);
                        setInfo(req, datasetContext);
                    }
                }

            }
        } else {
            logger.warn("DatasetId is not an integer: " + datasetIdString);
            return null;
        }

        return datasetContext;
    }

    /**
     * Save the session datasetContext back to the session.  Only do this if the
     * user or dataset have changed by checking the dirty flag;
     * @param req {@link HttpServletRequest}
     * @param datasetContext the DatasetContext object
     */
    static void setInfo(HttpServletRequest req, DatasetContext datasetContext) {
        setNumTransactions(datasetContext);
        req.getSession().setAttribute(
                "datasetContext_" + datasetContext.getDataset().getId(),
                datasetContext);
    }

    /**
     * Helper method to set the number of transactions for a given
     * DatasetContext.
     * @param datasetContext the DatasetContext
     */
    private static void setNumTransactions(DatasetContext datasetContext) {
        SampleMetricDao smDao = DaoFactory.DEFAULT.getSampleMetricDao();
        datasetContext.setNumTransactions(smDao.getTotalTransactions(datasetContext.getDataset()));
    }

    /** Dataset Not Found message. */
    private static final String ERROR_PAGE_MSG = "Dataset not found.";
    /**
     * Forward to the dataset not found page.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException possible exception on a forward
     * @throws IOException possible exception on a forward
     */
    public static void forwardDatasetNotFound(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("MSG", ERROR_PAGE_MSG);
        RequestDispatcher disp;
        disp = req.getSession().
            getServletContext().getRequestDispatcher(ErrorServlet.ERROR_PAGE_JSP_NAME);
        disp.forward(req, resp);
    }

}