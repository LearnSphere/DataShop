/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
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

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DatasetInstanceMapDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.dto.DatasetDTO;

import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.DatasetCreator.DuplicateDatasetNameException;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapId;
import edu.cmu.pslc.datashop.item.DatasetInstanceMapItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;

/**
 * Web service for adding a new dataset.
 * @author epennin1
 *
 */
public class DatasetAddService extends WebService {
    /** Parameters that are valid for the post method of this service. */
    private static final Set<String> VALID_POST_PARAMS = set(DATASET_NAME, INSTANCE_ID);
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * DatasetAddService constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DatasetAddService(HttpServletRequest req,
            HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Create a new dataset based on the provided dataset name.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            // Ensure only valid parameters were provided
            validateParameters(wsUserLog, VALID_POST_PARAMS);
            
            // Validate the dataset name
            String datasetName = (String)this.getParams().get("name");
            validateDatasetName(datasetName);

            // Validate the instance id
            String instanceId = (String)this.getParams().get(INSTANCE_ID);
            RemoteInstanceItem remoteInstance = validateRemoteInstanceId(instanceId);
            
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
            
            // Create a new dataset using the provided name
            DatasetItem datasetItem;
            try {
                datasetItem = DatasetCreator.INSTANCE.createNewDatasetLocal(datasetName);

                // Put this remote dataset into the appropriate project.
                ProjectItem remoteDatasetsProject = getRemoteDatasetsProject();
                datasetItem.setProject(remoteDatasetsProject);
                DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
                datasetDao.saveOrUpdate(datasetItem);
            } catch (DuplicateDatasetNameException ddne) {
                throw new WebServiceException(DATASET_NAME_CONFLICT_ERR, ddne.getMessage());
            }

            // Write entry to dataset_instance_map
            DatasetInstanceMapDao mapDao = DaoFactory.DEFAULT.getDatasetInstanceMapDao();
            DatasetInstanceMapItem mapItem = new DatasetInstanceMapItem();
            mapItem.setId(new DatasetInstanceMapId(datasetItem, remoteInstance));
            mapDao.saveOrUpdate(mapItem);

            // Return the new dataset id to the requester
            DatasetDTO dto = new DatasetDTO();
            dto.setId((Integer)datasetItem.getId());
            dto.setName(datasetItem.getDatasetName());
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
     * Validate the dataset name.
     * @param datasetName the name of the dataset to be validated
     * @throws WebServiceException when the dataset name is empty or too long
     */
    private void validateDatasetName(String datasetName) throws WebServiceException {
        if (datasetName == null || datasetName.equals("")) {
            throw paramValueMissingException(DATASET_NAME);
        } else if (datasetName.length() > DatasetItem.DATASET_NAME_MAX_LEN) {
            throw paramValueTooLongException(DATASET_NAME, "" + DatasetItem.DATASET_NAME_MAX_LEN);
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

    /**
     * Helper method to get the 'Remote Datasets' project item.
     */
    private ProjectItem getRemoteDatasetsProject() {

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.findRemoteDatasetsProject();

        if (projectItem == null) {
            projectItem = new ProjectItem();
            projectItem.setProjectName(ProjectItem.REMOTE_DATASETS);
            projectItem.setDescription("Project to hold remote datasets.");
            projectItem.setShareableStatus(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE);
            projectItem.setSubjectToDsIrb(ProjectItem.SUBJECT_TO_DS_IRB_NO);
            projectDao.saveOrUpdate(projectItem);
        }

        return projectItem;
    }
}
