/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REMOTE_REQUESTS_NOT_ALLOWED;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Web service for deleting a remote dataset.
 *
 * @author Cindy Tipper
 * @version $Revision: 12580 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-24 13:16:28 -0400 (Mon, 24 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DatasetDeleteService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DatasetDeleteService(HttpServletRequest req, HttpServletResponse resp,
                                Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Delete specified Dataset.
     * This is only meant to be used by the 'webservice_request' user to
     * delete the place-holder dataset created on the master when a real
     * dataset is created on a slave.
     *
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            logDebug("datasetId: ", datasetParam());

            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                                              "This server is not meant to handle "
                                              + "requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can create a new dataset
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                              "Insufficient privileges to delete "
                                              + "a dataset remotely.");
            }

            helper().datasetDeleteForId(getAuthenticatedUser(), datasetParam());
            writeSuccess("Remote dataset " + datasetParam() + " successfully deleted.");
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                         + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong with the XML message.", e);
            writeInternalError();
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
