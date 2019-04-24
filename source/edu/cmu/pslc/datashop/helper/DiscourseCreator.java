/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
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

import edu.cmu.pslc.datashop.dao.hibernate.UpdatableIdentityGenerator;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.dto.DiscourseDTO;

import edu.cmu.pslc.datashop.servlet.webservices.WebServiceException;
import edu.cmu.pslc.datashop.servlet.webservices.WebServiceXMLMessage;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.StringUtils;

import edu.cmu.pslc.datashop.webservices.DatashopClient;

/**
 * Singleton for remotely creating and deleting discourses.
 *
 * @version $Revision: 13004 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-24 11:40:58 -0400 (Thu, 24 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public enum DiscourseCreator {
    /** Singleton enum value. */
    INSTANCE;

    /** The instance of the singleton. */
    protected static DiscourseCreator instance;
    /** The discourse DAO. */
    protected DiscourseDao discourseDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Creates and saves a new discourse record based on the provided name and returns the result.
     * If this is a slave DataShop instance, a web service call will be made to reserve an id
     * on a master instance before using that id to create a matching discourse locally.
     * @param discourseName the name of the discourse that will be created
     * @return the newly generated discourse
     * @throws IOException when there is an error creating the new discourse locally or remotely
     */
    public DiscourseItem createNewDiscourse(String discourseName)
        throws IOException
    {
        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
        Long discourseId = null;

        // Call the web service to get a new discourse id from a master instance if necessary
        if (client != null) {
            logger.info("Remotely reserving a new discourse id.");

            Long remoteInstanceId = DataShopInstance.getRemoteInstanceId();

            // If we failed to initialize this remote instance, we can't do anything else.
            if (remoteInstanceId == null) { return null; }

            try {
                String responseXml = client.getDiscourseAdd(discourseName, remoteInstanceId);
                discourseId = parseDiscourseAddResponse(responseXml, discourseName);
            } catch (JDOMException jde) {
                throw new RemoteDataShopConnectionException(jde);
            }
        }

        // Create an empty discourse locally and return the result
        DiscourseItem newDiscourseItem = null;
        if (discourseId == null && isSlave()) {
            throw new NullDiscourseIdForSlave(discourseName);
        } else if (DataShopInstance.isSlave() == null) {
            throw new NullSlaveProperty();
        } else {
            newDiscourseItem = createEmptyDiscourse(discourseId, discourseName);
        }

        return newDiscourseItem;
    }

    /**
     * Creates and saves a new discourse record based on the provided name locally. This is meant
     * specifically for use by the DiscourseAdd web service. Anything else should call
     * createNewDiscourse instead.
     * @param discourseName the name of the discourse that will be created
     * @return the newly generated discourse
     * @throws IOException when there is an error creating the new discourse
     */
    public DiscourseItem createNewDiscourseLocal(String discourseName) throws IOException {
        // Create an empty discourse locally and return the id
        return createEmptyDiscourse(null, discourseName);
    }

    /**
     * Creates an empty discourse based on the provided discourse id and name.
     * If the id is null a new one will be created.
     * When applyRandomName is enabled, the name applied to the stored discourse record is randomly
     * generated, while the real name is only applied to the DiscourseItem object afterward. The
     * real discourse name is then applied to the record the next time it is saved.
     * @param discourseId the id of the discourse
     * @param discourseName the name of the discourse
     * @return the discourse id
     * @throws DuplicateDiscourseNameException when there is an error creating the new discourse
     */
    protected synchronized DiscourseItem createEmptyDiscourse(Long discourseId,
                                                              String discourseName)
        throws IOException {

        // Ensure a discourse with the provided name doesn't already exist
        if (discourseNameExists(discourseName)) {
            throw new DuplicateDiscourseNameException(discourseName, !isSlave());
        }

        // Create a discourse item
        DiscourseItem discourseItem = new DiscourseItem();
        discourseItem.setName(discourseName);

        // Save the discourse record
        UpdatableIdentityGenerator.setNextId(DiscourseItem.class, discourseId);
        try {
            discourseDao.saveOrUpdate(discourseItem);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateDiscourseIdException(discourseId);
        }

        return discourseItem;
    }

    /**
     * Ensure a discourse with the provided name doesn't already exist locally
     * @return
     */
    protected boolean discourseNameExists(String discourseName) {
        return (discourseDao.findByName(discourseName) != null);
    }

    /**
     * Deletes the specified discourse on the master (remote) instance.
     * @param discourseId the id of the discourse that will be deleted
     * @return discourseId the remote discourse id that was delete. Null if failure.
     * @throws IOException when there is an error deleting the discourse remotely
     */
    public Long deleteDiscourse(Long discourseId)
        throws IOException
    {
        if (!isSlave()) {
            logger.info("Not allowed.");
            return null;
        }

        Long deletedId = null;

        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();

        // Call the web service to get a new discourse id from a master instance if necessary
        if (client != null) {
            logger.info("Remotely deleting a discourse.");
            try {
                String responseXml = client.getDiscourseDelete(discourseId);
                deletedId = parseDiscourseDeleteResponse(responseXml, discourseId);
            } catch (JDOMException jde) {
                throw new RemoteDataShopConnectionException(jde);
            }
        }

        return deletedId;
    }

    /**
     * Push the meta-data for the specified discourse to the master (remote) instance.
     * @param dto the DTO of the discourse that will be pushed
     * @throws IOException when there is an error pushing the discourse info
     */
    public void setDiscourse(DiscourseDTO dto)
        throws IOException
    {
        if (!isSlave()) {
            logger.info("Not allowed.");
            return;
        }

        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();

        // Call the web service to get a new discourse id from a master instance if necessary
        if (client != null) {
            logger.info("Pushing discourse info to master.");
            try {
                // Get the XML
                String discourseXml = getLocalDiscourseXml(dto);
                String responseXml = client.getDiscourseSet(dto.getId(), discourseXml);
                parseDiscourseSetResponse(responseXml, dto.getId());
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
     * Helper method to generate XML for specified discourse.
     * @param dto the DTO of the discourse that will be pushed
     * @return String the XML
     * @throws Exception failure to write XML
     */
    private String getLocalDiscourseXml(DiscourseDTO dto)
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
     * Interprets the response from a call to the discourse add web service.
     * @param responseXml the discourse add web service reply
     * @param discourseName the name of the discourse created
     * @return the id of the remotely created discourse
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Long parseDiscourseAddResponse(String responseXml, String discourseName)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);
        String resultMessage = getResultMessage(responseRootNode);
        if (resultCode == null) {
            throw new NullDiscourseIdForSlave(discourseName);
        }

        if (resultCode < 0) {
            if (resultCode == WebServiceException.DATASET_NAME_CONFLICT_ERR) {
                throw new DuplicateDiscourseNameException(discourseName, true);
            } else {
                // Don't lose the error info received from master.
                logger.debug("Failed to retrieve discourseId from master. Error code = "
                             + resultCode + ", error message = " + resultMessage);
                throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                    resultMessage));
            }
        }

        // Retrieve the discourse node from the xml reply
        Element discourseNode = responseRootNode.getChild("discourse");
        if (discourseNode == null) {
            logger.error("The discourse node could not be found in the web service response.");
            throw new NullDiscourseIdForSlave(discourseName);
        }
        
        // Retrieve the new discourse id from the xml reply
        String discourseIdString = discourseNode.getAttributeValue("id");
        if (discourseIdString == null) {
            logger.error("The id attribute of the discourse node could not be found in the "
                         + "web service response.");
            throw new NullDiscourseIdForSlave(discourseName);
        }
        
        // Return the new discourse id
        Long discourseId = null;
        if (discourseIdString != null && discourseIdString.matches("\\d+")) {
            discourseId = Long.valueOf(discourseIdString);
        }
        return discourseId;
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
     * Interprets the response from a call to the discourse delete web service.
     * @param responseXml the discourse delete web service reply
     * @param discourseId the id of the discourse removed
     * @return discourseId the remote discourse id that was delete. Null if failure.
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Long parseDiscourseDeleteResponse(String responseXml, Long discourseId)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);

        if (resultCode == null) { return null; }

        String resultMessage = getResultMessage(responseRootNode);

        if (resultCode < 0) {
            logger.debug("Failed to delete discourse '" + discourseId
                         + "' from master. Error code = "
                         + resultCode + ", error message = " + resultMessage);
            throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                resultMessage));
        }

        return discourseId;
    }

   /**
     * Interprets the response from a call to the discourse set web service.
     * @param responseXml the discourse set web service reply
     * @param discourseId the id of the discourse removed
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private void parseDiscourseSetResponse(String responseXml, Long discourseId)
        throws IOException, JDOMException
    {
        logger.debug("Parsing the web service reply: " + responseXml);
        
        // Retrieve the root node from the xml reply
        Element responseRootNode = getResponseRootNode(responseXml);
        Integer resultCode = getResultCode(responseRootNode);

        if (resultCode == null) { return; }

        String resultMessage = getResultMessage(responseRootNode);

        if (resultCode < 0) {
            logger.debug("Failed to set discourse '" + discourseId
                         + "' info on master. Error code = "
                         + resultCode + ", error message = " + resultMessage);
            throw new RemoteDataShopConnectionException(new WebServiceException(resultCode,
                                                                                resultMessage));
        }
    }
    
    /**
     * An exception thrown when a new discourse cannot be created on the master DataShop server.
     */
    public class RemoteDataShopConnectionException extends IOException {
        public RemoteDataShopConnectionException(Throwable cause) {
            super("An error occurred while contacting the master DataShop server to create a new "
                  + "discourse record. Please try again. Contact DataShop Help if "
                  + "the problem persists.", cause);
        }
    }

    /**
     * An exception thrown when a discourse with a duplicate id already exists.
     */
    public class DuplicateDiscourseIdException extends IOException {
        public DuplicateDiscourseIdException(Long discourseId) {
           super("A discourse with id " + discourseId + " already exists.");
        }
    }

    /**
     * An exception thrown when a discourse with a duplicate name already exists.
     */
    public class DuplicateDiscourseNameException extends IOException {
        public DuplicateDiscourseNameException(String discourseName, Boolean isMaster) {
           super("A discourse with name " + discourseName + " already exists on the "
                 + (isMaster ? "master." : "slave."));
        }
    }

    /**
     * An exception thrown when a null discourse Id is returned for the master.
     */
    public class NullSlaveProperty extends IOException {
        public NullSlaveProperty() {
           super("The instance.properties attribute 'instance.is_slave' must be set to "
               + " true or false. Please set this value before deploying and try again.");
        }
    }

    /**
     * An exception thrown when a null discourse Id is returned for the slave.
     */
    public class NullDiscourseIdForSlave extends IOException {
        public NullDiscourseIdForSlave(String discourseName) {
            super("A null discourse Id was returned by the master DataShop server for "
                  + "discourse name '"
                  + discourseName + "'. Please try again. Contact DataShop Help if the "
                  + "problem persists.");
        }
    }
}
