/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INSTANCE_NAME_CONFLICT_ERR;
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
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InstanceAddService extends WebService {
    /** Parameters that are valid for the get method of this service. */
    private static final Set<String> VALID_GET_PARAMS = set(NAME, URL);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * InstanceAddService constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public InstanceAddService(HttpServletRequest req, HttpServletResponse resp,
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
            validateParam(name, NAME, RemoteInstanceItem.INSTANCE_NAME_MAX_LEN);

            // Validate the URL
            String url = (String)this.getParams().get(URL);
            validateParam(url, URL, RemoteInstanceItem.INSTANCE_URL_MAX_LEN);

            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                    "This server is not meant to handle requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can create a new dataset
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                    "Insufficient privileges to create a new dataset remotely.");
            }

            // Check for duplicate instance name
            RemoteInstanceDao riDao = DaoFactory.DEFAULT.getRemoteInstanceDao();
            if (riDao.findByName(name).size() > 0) {
                throw new WebServiceException(INSTANCE_NAME_CONFLICT_ERR,
                                              "An instance with name " + name + " already exists.");
            }

            // Create a new remote instance using the provided name and url
            RemoteInstanceItem remoteInstance = new RemoteInstanceItem();
            remoteInstance.setName(name);
            remoteInstance.setDatashopUrl(url);
            riDao.saveOrUpdate(remoteInstance);

            // Return the new instance id to the requester
            writeSuccessWithCustomField("", map("instance_id", remoteInstance.getId()));
            
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
        if (param == null || param.equals("")) {
            throw paramValueMissingException(paramName);
        } else if (param.length() > maxLen) {
            throw paramValueTooLongException(paramName, "" + maxLen);
        }
    }
}
