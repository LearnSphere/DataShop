/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.dto.RemoteInstanceDTO;

import edu.cmu.pslc.datashop.item.RemoteInstanceItem;

import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_INSTANCE_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;

/**
 * Web service for fetching a single remote instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 12672 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:45:25 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InstanceService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());


    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(INSTANCE_ID);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public InstanceService(HttpServletRequest req, HttpServletResponse resp,
                           Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Return the requested dataset as XML.
     * @param wsUserLog web service user lgo
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);

            // Validate the RemoteInstance ID
            RemoteInstanceItem remoteInstance = validateRemoteInstanceId();

            RemoteInstanceDTO dto = new RemoteInstanceDTO();
            dto.setId((Long)remoteInstance.getId());
            dto.setName(remoteInstance.getName());
            dto.setDatashopUrl(remoteInstance.getDatashopUrl());
            writeDTOXML(dto);
        } catch (WebServiceException wse) {
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something unexpected went wrong with the web service request.", e);
            writeInternalError();
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

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
