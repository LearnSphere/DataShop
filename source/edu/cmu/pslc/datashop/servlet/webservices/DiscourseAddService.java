/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.DATASET_NAME_CONFLICT_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_INSTANCE_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REMOTE_REQUESTS_NOT_ALLOWED;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueTooLongException;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DiscourseInstanceMapDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.dto.DiscourseDTO;

import edu.cmu.pslc.datashop.helper.DiscourseCreator;
import edu.cmu.pslc.datashop.helper.DiscourseCreator.DuplicateDiscourseNameException;

import edu.cmu.pslc.datashop.item.DiscourseInstanceMapId;
import edu.cmu.pslc.datashop.item.DiscourseInstanceMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Web service for adding a new discourse.
 * @author epennin1
 *
 */
public class DiscourseAddService extends WebService {
    /** Parameters that are valid for the post method of this service. */
    private static final Set<String> VALID_POST_PARAMS = set(DISCOURSE_NAME, INSTANCE_ID);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * DiscourseAddService constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DiscourseAddService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Create a new discourse based on the provided discourse name.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            // Ensure only valid parameters were provided
            validateParameters(wsUserLog, VALID_POST_PARAMS);
            
            // Validate the discourse name
            String discourseName = (String)this.getParams().get("name");
            validateDiscourseName(discourseName);

            // Validate the instance id
            String instanceId = (String)this.getParams().get(INSTANCE_ID);
            RemoteInstanceItem remoteInstance = validateRemoteInstanceId(instanceId);
            
            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                    "This server is not meant to handle requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can create a new discourse
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                    "Insufficient privileges to create a new discourse remotely.");
            }
            
            // Create a new discourse using the provided name
            DiscourseItem discourseItem;
            try {
                discourseItem = DiscourseCreator.INSTANCE.createNewDiscourseLocal(discourseName);

                DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
                discourseDao.saveOrUpdate(discourseItem);
            } catch (DuplicateDiscourseNameException ddne) {
                throw new WebServiceException(DATASET_NAME_CONFLICT_ERR, ddne.getMessage());
            }

            // Write entry to discourse_instance_map
            DiscourseInstanceMapDao mapDao = DaoFactory.DEFAULT.getDiscourseInstanceMapDao();
            DiscourseInstanceMapItem mapItem = new DiscourseInstanceMapItem();
            mapItem.setId(new DiscourseInstanceMapId(discourseItem, remoteInstance));
            mapDao.saveOrUpdate(mapItem);

            // Return the new discourse id to the requester
            DiscourseDTO dto = new DiscourseDTO();
            dto.setId((Long)discourseItem.getId());
            dto.setName(discourseItem.getName());
            writeDTOXML(dto);
            
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
     * Validate the discourse name.
     * @param discourseName the name of the discourse to be validated
     * @throws WebServiceException when the discourse name is empty or too long
     */
    private void validateDiscourseName(String discourseName) throws WebServiceException {
        if (discourseName == null || discourseName.equals("")) {
            throw paramValueMissingException(DISCOURSE_NAME);
        }
    }

    /**
     * Validate the remote instance ID.
     * @param instanceIdStr the id of the instance to be validated
     * @return RemoteInstanceItem, if found
     * @throws WebServiceException when the instanceId is not valid
     */
    private RemoteInstanceItem validateRemoteInstanceId(String instanceIdStr)
        throws WebServiceException
    {
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
