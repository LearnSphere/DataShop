/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INSTANCE_NAME_CONFLICT_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_INSTANCE_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REMOTE_REQUESTS_NOT_ALLOWED;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueTooLongException;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.item.DatasetInstanceMapId;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Web service for adding a remote instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 12959 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-02-24 16:32:07 -0500 (Wed, 24 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InstanceSetService extends WebService {
    /** Parameters that are valid for the get method of this service. */
    private static final Set<String> VALID_GET_PARAMS = set(INSTANCE_ID, NAME, URL);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * InstanceSetService constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public InstanceSetService(HttpServletRequest req, HttpServletResponse resp,
                              Map<String, Object> params)
    {
        super(req, resp, params);
    }

    /**
     * Create a new instance based on the provided name and URL.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            // Ensure only valid parameters were provided
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            
            // Validate the name
            String name = (String)this.getParams().get(NAME);
            if (name != null) {
                validateParam(name, NAME, RemoteInstanceItem.INSTANCE_NAME_MAX_LEN);
            }

            // Validate the URL
            String url = (String)this.getParams().get(URL);
            if (url != null) {
                validateParam(url, URL, RemoteInstanceItem.INSTANCE_URL_MAX_LEN);
            }

            // Validate the RemoteInstance ID
            RemoteInstanceItem remoteInstance = validateRemoteInstanceId();

            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                    "This server is not meant to handle requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can create a new instance.
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                    "Insufficient privileges to create a new dataset remotely.");
            }

            // If it is being changed, check for duplicate instance name
            RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
            if ((name != null) && !remoteInstance.getName().equals(name)) {
                if (riDao.findByName(name).size() > 0) {
                    throw new WebServiceException(INSTANCE_NAME_CONFLICT_ERR,
                                                  "An instance with name '"
                                                  + name + "' already exists.");
                }
            }

            // Update the remote instance using the provided name and/or url
            if ((name != null) && (name.length() > 0)) {
                remoteInstance.setName(name);
            }
            if ((url != null) && (url.length() > 0)) {
                remoteInstance.setDatashopUrl(url);
            }
            riDao.saveOrUpdate(remoteInstance);

            writeSuccess("Attributes modified for remote instance " + remoteInstance.getId());
            
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                         + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something went wrong processing web service request.", e);
            writeInternalError();
        }
    }
    
    /**
     * Validate the param.
     * @param param the value to be validated
     * @throws WebServiceException when the value is empty or too long
     */
    private void validateParam(String param, String paramName, int maxLen)
        throws WebServiceException
    {
        if (param.length() > maxLen) {
            throw paramValueTooLongException(paramName, "" + maxLen);
        }
    }

    /**
     * Validate the remote instance ID.
     * @param instanceIdStr the id of the instance to be validated
     * @return RemoteInstanceItem, if found
     * @throws WebServiceException when the instanceId is not valid
     */
    private RemoteInstanceItem validateRemoteInstanceId()
        throws WebServiceException
    {
        String instanceIdStr = (String)this.getParams().get(INSTANCE_ID);
        if (instanceIdStr == null || instanceIdStr.equals("")) {
            throw paramValueMissingException(INSTANCE_ID);
        }
        Long instanceId = longParam(INSTANCE_ID);

        // Find it...
        RemoteInstanceItem riItem = null;
        try {
            RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
            riItem = riDao.get(instanceId);
        } catch (Exception e) {
            throw new WebServiceException(INVALID_INSTANCE_ID_ERR,
                                          "Remote instance " + instanceId + " is not valid.");
        }

        if (riItem == null) {
            throw new WebServiceException(INVALID_INSTANCE_ID_ERR,
                                          "Remote instance " + instanceId + " is not valid.");
        }

        return riItem;
    }
}
