/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.helper;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.springframework.dao.DataIntegrityViolationException;

import edu.cmu.pslc.datashop.dao.AlphaScoreDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.hibernate.UpdatableIdentityGenerator;

import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.KcModelDTO;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceXMLMessage;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.StringUtils;

import edu.cmu.pslc.datashop.webservices.DatashopClient;

/**
 * Singleton for remotely creating and deleting datasets.
 *
 * @version $Revision: $
 * <BR>Last modified by: $Author: $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public enum DatasetCreator {
    /** Singleton enum value. */
    INSTANCE;

    /** The instance of the singleton. */
    protected static DatasetCreator instance;
    /** The dataset DAO. */
    protected DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Creates and saves a new dataset record based on the provided name and returns the result.
     * If this is a slave DataShop instance, a web service call will be made to reserve an id
     * on a master instance before using that id to create a matching dataset locally.
     * @param datasetName the name of the dataset that will be created
     * @return the newly generated dataset
     * @throws IOException when there is an error creating the new dataset locally or remotely
     */
    public DatasetItem createNewDataset(String datasetName)
        throws IOException
    {
        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
        Integer datasetId = null;

        // Call the web service to get a new dataset id from a master instance if necessary
        if (client != null) {
            logger.info("Remotely reserving a new dataset id.");

            Long remoteInstanceId = DataShopInstance.getRemoteInstanceId();

            // If we failed to initialize this remote instance, we can't do anything else.
            if (remoteInstanceId == null) { return null; }

            try {
                String responseXml = client.getDatasetAdd(datasetName, remoteInstanceId);
                datasetId = parseDatasetAddResponse(responseXml, datasetName);
            } catch (JDOMException jde) {
                throw new RemoteDataShopConnectionException(jde);
            }
        }

        // Create an empty dataset locally and return the result
        DatasetItem newDatasetItem = null;
        if (datasetId == null && isSlave()) {
            throw new NullDatasetIdForSlave(datasetName);
        } else if (DataShopInstance.isSlave() == null) {
            throw new NullSlaveProperty();
        } else {
            newDatasetItem = createEmptyDataset(datasetId, datasetName);
        }

        return newDatasetItem;
    }

    /**
     * Creates and saves a new dataset record based on the provided name locally. This is meant
     * specifically for use by the DatasetAdd web service. Anything else should call
     * createNewDataset instead.
     * @param datasetName the name of the dataset that will be created
     * @return the newly generated dataset
     * @throws IOException when there is an error creating the new dataset
     */
    public DatasetItem createNewDatasetLocal(String datasetName) throws IOException {
        // Create an empty dataset locally and return the id
        return createEmptyDataset(null, datasetName);
    }

    /**
     * Creates an empty dataset based on the provided dataset id and name.
     * If the id is null a new one will be created.
     * When applyRandomName is enabled, the name applied to the stored dataset record is randomly
     * generated, while the real name is only applied to the DatasetItem object afterward. The
     * real dataset name is then applied to the record the next time it is saved.
     * @param datasetId the id of the dataset
     * @param datasetName the name of the dataset
     * @return the dataset id
     * @throws DuplicateDatasetNameException when there is an error creating the new dataset
     */
    protected synchronized DatasetItem createEmptyDataset(Integer datasetId, String datasetName)
        throws IOException {

        // Ensure a dataset with the provided name doesn't already exist
        if (datasetNameExists(datasetName)) {
            throw new DuplicateDatasetNameException(datasetName, !isSlave());
        }

        // Create a dataset item
        DatasetItem datasetItem = new DatasetItem();
        datasetItem.setDatasetName(datasetName);

        // Save the dataset record
        UpdatableIdentityGenerator.setNextId(DatasetItem.class, datasetId);
        try {
            datasetDao.saveOrUpdate(datasetItem);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDatasetIdException(datasetId);
        }

        return datasetItem;
    }

    /**
     * Ensure a dataset with the provided name doesn't already exist locally
     * @return
     */
    protected boolean datasetNameExists(String datasetName) {
        return datasetDao.find(datasetName).size() > 0;
    }

    /**
     * Deletes the specified dataset on the master (remote) instance.
     * @param datasetId the id of the dataset that will be deleted
     * @return datasetId the remote dataset id that was delete. Null if failure.
     * @throws IOException when there is an error deleting the dataset remotely
     */
    public Integer deleteDataset(Integer datasetId)
        throws IOException
    {
        if (!isSlave()) {
            logger.info("Not allowed.");
            return null;
        }

        Integer deletedId = null;

        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();

        // Call the web service to get a new dataset id from a master instance if necessary
        if (client != null) {
            logger.info("Remotely deleting a dataset.");
            try {
                String responseXml = client.getDatasetDelete(datasetId);
                deletedId = parseDatasetDeleteResponse(responseXml, datasetId);
            } catch (JDOMException jde) {
                throw new RemoteDataShopConnectionException(jde);
            }
        }

        return deletedId;
    }

    /**
     * Push the meta-data for the specified dataset to the master (remote) instance.
     * @param dto the DTO of the dataset that will be pushed
     * @throws IOException when there is an error pushing the dataset info
     */
    public void setDataset(DatasetDTO dto)
        throws IOException
    {
        if (!isSlave()) {
            logger.info("Not allowed.");
            return;
        }

        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();

        // Call the web service to get a new dataset id from a master instance if necessary
        if (client != null) {
            logger.info("Pushing dataset info to master.");
            try {
                // Get the XML
                String datasetXml = getLocalDatasetXml(dto);
                String responseXml = client.getDatasetSet(dto.getId(), datasetXml);
                parseDatasetSetResponse(responseXml, dto.getId());
            } catch (JDOMException jde) {
                throw new RemoteDataShopConnectionException(jde);
            } catch (Exception e) {
                throw new RemoteDataShopConnectionException(e);
            }
        } else {
            logger.info("DatashopClient is null");
        }
    }

    /**
     * Helper method to generate XML for specified dataset.
     * @param dto the DTO of the dataset that will be pushed
     * @return String the XML
     * @throws Exception failure to write XML
     */
    private String getLocalDatasetXml(DatasetDTO dto)
        throws Exception
    {
        // Using an empty string environment arg, means the DataShopInstance
        // master schema will be used.
        WebServiceXMLMessage xmlMessage = new WebServiceXMLMessage(dto, "");
        return xmlMessage.xmlString();
    }

    /**
     * Determine if this is running on a slave.
     */
    private Boolean isSlave() {
        if (DataShopInstance.isSlave() != null && DataShopInstance.isSlave()) {
            return true;
        }

        return false;
    }

   /**
     * Interprets the response from a call to the dataset add web service.
     * @param responseXml the dataset add web service reply
     * @param datasetName the name of the dataset created
     * @return the id of the remotely created dataset
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Integer parseDatasetAddResponse(String responseXml, String datasetName)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);
        String resultMessage = getResultMessage(responseRootNode);
        if (resultCode == null) {
            throw new NullDatasetIdForSlave(datasetName);
        }

        if (resultCode < 0) {
            if (resultCode == WebServiceException.DATASET_NAME_CONFLICT_ERR) {
                throw new DuplicateDatasetNameException(datasetName, true);
            } else {
                // Don't lose the error info received from master.
                logger.debug("Failed to retrieve datasetId from master. Error code = "
                             + resultCode + ", error message = " + resultMessage);
                throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                    resultMessage));
            }
        }

        // Retrieve the dataset node from the xml reply
        Element datasetNode = responseRootNode.getChild("dataset");
        if (datasetNode == null) {
            logger.error("The dataset node could not be found in the web service response.");
            throw new NullDatasetIdForSlave(datasetName);
        }
        
        // Retrieve the new dataset id from the xml reply
        String datasetIdString = datasetNode.getAttributeValue("id");
        if (datasetIdString == null) {
            logger.error("The id attribute of the dataset node could not be found in the "
                         + "web service response.");
            throw new NullDatasetIdForSlave(datasetName);
        }
        
        // Return the new dataset id
        Integer datasetId = null;
        if (datasetIdString != null && datasetIdString.matches("\\d+")) {
            datasetId = Integer.valueOf(datasetIdString);
        }
        return datasetId;
    }
    
    /**
     * Parses the xml response from a web service call and returns its root node.
     * @param responseXml the response from a web service call
     * @return the root node
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Element getResponseRootNode(String responseXml) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();
        StringReader reader = null;
        Element rootElement;
        
        try {
            reader = new StringReader(responseXml);
            rootElement = builder.build(reader).getRootElement();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        
        return rootElement;
    }
    
    /**
     * Retrieves the status code from a web service response.
     * @param response the root node of a web service response
     * @return the result_code
     */
    private Integer getResultCode(Element response) {
        String resultCodeString = response.getAttributeValue("result_code");
        if (resultCodeString == null) {
            logger.error("No response code found in the web service response.");
            return null;
        }
        Integer resultCode = Integer.valueOf(resultCodeString);
        
        return resultCode;
    }

    /**
     * Retrieves the status message from a web service response.
     * @param response the root node of a web service response
     * @return the result_message
     */
    private String getResultMessage(Element response) {
        Integer resultCode = getResultCode(response);
        String resultMessage = response.getAttributeValue("result_message");
        if (resultMessage != null) {
            if (resultCode < 0) {
                logger.error("Error response from the web service: " + resultMessage);
            } else {
                logger.info("Response from the web service: " + resultMessage);
            }
        }
        
        return resultMessage;
    }

   /**
     * Interprets the response from a call to the dataset delete web service.
     * @param responseXml the dataset delete web service reply
     * @param datasetId the id of the dataset removed
     * @return datasetId the remote dataset id that was delete. Null if failure.
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Integer parseDatasetDeleteResponse(String responseXml, Integer datasetId)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);

        if (resultCode == null) { return null; }

        String resultMessage = getResultMessage(responseRootNode);

        if (resultCode < 0) {
            logger.debug("Failed to delete dataset '" + datasetId
                         + "' from master. Error code = "
                         + resultCode + ", error message = " + resultMessage);
            throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                resultMessage));
        }

        return datasetId;
    }

   /**
     * Interprets the response from a call to the dataset set web service.
     * @param responseXml the dataset set web service reply
     * @param datasetId the id of the dataset removed
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private void parseDatasetSetResponse(String responseXml, Integer datasetId)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);

        if (resultCode == null) { return; }

        String resultMessage = getResultMessage(responseRootNode);

        if (resultCode < 0) {
            logger.debug("Failed to set dataset '" + datasetId
                         + "' info on master. Error code = "
                         + resultCode + ", error message = " + resultMessage);
            throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                resultMessage));
        }
    }
    
    /**
     * An exception thrown when a new dataset cannot be created on the master DataShop server.
     */
    public class RemoteDataShopConnectionException extends IOException {
        public RemoteDataShopConnectionException(Throwable cause) {
            super("An error occurred while contacting the master DataShop server to create a new "
                  + "dataset record. Please try again. Contact DataShop Help if "
                  + "the problem persists.", cause);
        }
    }

    /**
     * An exception thrown when a dataset with a duplicate id already exists.
     */
    public class DuplicateDatasetIdException extends IOException {
        public DuplicateDatasetIdException(Integer datasetId) {
           super("A dataset with id " + datasetId + " already exists.");
        }
    }

    /**
     * An exception thrown when a dataset with a duplicate name already exists.
     */
    public class DuplicateDatasetNameException extends IOException {
        public DuplicateDatasetNameException(String datasetName, Boolean isMaster) {
           super("A dataset with name " + datasetName + " already exists on the "
                 + (isMaster ? "master." : "slave."));
        }
    }

    /**
     * An exception thrown when a null dataset Id is returned for the master.
     */
    public class NullSlaveProperty extends IOException {
        public NullSlaveProperty() {
           super("The instance.properties attribute 'instance.is_slave' must be set to "
               + " true or false. Please set this value before deploying and try again.");
        }
    }

    /**
     * An exception thrown when a null dataset Id is returned for the slave.
     */
    public class NullDatasetIdForSlave extends IOException {
        public NullDatasetIdForSlave(String datasetName) {
            super("A null dataset Id was returned by the master DataShop server for dataset name '"
                  + datasetName + "'. Please try again. Contact DataShop Help if the "
                  + "problem persists.");
        }
    }
}
