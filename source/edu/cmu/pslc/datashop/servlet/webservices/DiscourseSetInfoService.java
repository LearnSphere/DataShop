/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDao;
import edu.cmu.pslc.datashop.discoursedb.dao.DiscourseDbDaoFactory;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.RemoteDiscourseInfoDao;

import edu.cmu.pslc.datashop.item.RemoteDiscourseInfoItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.ServletDateUtil;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;

import edu.cmu.pslc.datashop.xml.XMLConstants;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_XML_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_VALUE_FOR_ELEMENT_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REMOTE_REQUESTS_NOT_ALLOWED;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidParamValueException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.unknownErrorException;

import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for setting discourse meta-data values.
 *
 * @author Cindy Tipper
 * @version $Revision: 12983 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-14 15:13:11 -0400 (Mon, 14 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiscourseSetInfoService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DISCOURSE_ID, POST_DATA);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DiscourseSetInfoService(HttpServletRequest req,
                                   HttpServletResponse resp, Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Set custom field values.
     * @param wsUserLog web service user log
     */
    public void post(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_GET_PARAMS);
            validatePostData();

            DiscourseDao dsDao = DiscourseDbDaoFactory.DEFAULT.getDiscourseDao();
            DiscourseItem discourse = dsDao.get(longParam(DISCOURSE_ID));

            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                    "This server is not meant to handle requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can modify the remote discourse
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                    "Insufficient privileges to create a new discourse remotely.");
            }

            // Parse the XML.
            RemoteDiscourseInfoItem item = parsePostData(discourse);
            RemoteDiscourseInfoDao dao = DaoFactory.DEFAULT.getRemoteDiscourseInfoDao();
            dao.saveOrUpdate(item);

            // Persist any discourse changes too... name might have changed.
            dsDao.saveOrUpdate(discourse);

            writeSuccess("Meta-data added for remote discourse " + discourse.getId());
        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                         + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something unexpected went wrong processing web service request.", e);
            writeInternalError();
        }
    }

    /**
     * Validate postData parameter.
     *
     * @throws WebServiceException when postData is empty or null
     */
    private void validatePostData() throws WebServiceException {
        String postData = stringParam(POST_DATA);
        if (postData == null || postData.equals("")) {
            throw invalidDataException(POST_DATA + " is empty or null.");
        }
    }

    /**
     * Parse postData, reading the remote discourse meta-data from XML.
     * @param discourse the DiscourseItem
     * @return RemoteDiscourseInfoItem discourse meta-data, read from XML
     * @throws WebServiceException when postData is not valid
     */
    private RemoteDiscourseInfoItem parsePostData(DiscourseItem discourse)
        throws WebServiceException
    {
        RemoteDiscourseInfoItem result = createRemoteDiscourseInfo(discourse);

        Element messageElement = null;
        try {
            StringReader reader = new StringReader(stringParam(POST_DATA));
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();
            Element dsElement = root.getChild(XMLConstants.DISCOURSE_ELEMENT);
            if (dsElement == null) {
                logger.error("Invalid XML format: missing root or </"
                             + XMLConstants.DISCOURSE_ELEMENT + "> element.");
                throw new WebServiceException(INVALID_XML_ERR, "Invalid XML format.");
            }
            String idAttr = dsElement.getAttributeValue("id");

            // get each element from XML
            Iterator messageIter = dsElement.getChildren().iterator();
            while (messageIter.hasNext()) {
                messageElement = (Element)messageIter.next();
                String elementName = messageElement.getName();
                if (elementName.equals("name")) {
                    discourse.setName(messageElement.getTextTrim());
                } else if (elementName.equals("date_range")) {
                    result.setDateRange(messageElement.getTextTrim());
                } else if (elementName.equals("number_of_users")) {
                    result.setNumUsers(parseLong("number_of_users", messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_discourse_parts")) {
                    result.setNumDiscourseParts(parseLong("number_of_discourse_parts",
                                                          messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_contributions")) {
                    result.setNumContributions(parseLong("number_of_contributions",
                                                         messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_data_sources")) {
                    result.setNumDataSources(parseLong("number_of_data_sources",
                                                       messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_relations")) {
                    result.setNumRelations(parseLong("number_of_relation",
                                                     messageElement.getTextTrim()));
                }
            }

        } catch (JDOMException exception) {
            logger.warn("Invalid XML format: " + exception.getMessage());
            throw new WebServiceException(INVALID_XML_ERR, "Invalid XML format.");
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
            throw unknownErrorException();
        }
        return result;
    }

    /**
     * Helper method for creating a RemoteDiscourseInfoItem for a given DiscourseItem.
     * If a row already exists in the db for this discourse, it will be deleted.
     * @param discourse the DiscourseItem
     * @return RemoteDiscourseInfoItem the item
     */
    private RemoteDiscourseInfoItem createRemoteDiscourseInfo(DiscourseItem discourse) {

        RemoteDiscourseInfoDao dao = DaoFactory.DEFAULT.getRemoteDiscourseInfoDao();

        List<RemoteDiscourseInfoItem> list = dao.findByDiscourse(discourse);
        for (RemoteDiscourseInfoItem item : list) {
            dao.delete(item);
        }

        RemoteDiscourseInfoItem result = new RemoteDiscourseInfoItem();
        result.setDiscourse(discourse);

        return result;
    }

    /**
     * Helper method for parsing Long values from XML string.
     * @param eleName the XML element name
     * @param eleVal the XML element value
     * @return Long value
     * @throws WebServiceException when XML is not valid Long
     */
    private Long parseLong(String eleName, String eleVal)
        throws WebServiceException
    {
        try {
            return Long.parseLong(eleVal);
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(eleName, eleVal);
        }
    }

    /**
     * Helper method for parsing Integer values from XML string.
     * @param eleName the XML element name
     * @param eleVal the XML element value
     * @return Integer value
     * @throws WebServiceException when XML is not valid Integer
     */
    private Integer parseInteger(String eleName, String eleVal)
        throws WebServiceException
    {
        try {
            return Integer.parseInt(eleVal);
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(eleName, eleVal);
        }
    }

    /**
     * Helper method for parsing Double values from XML string.
     * @param eleName the XML element name
     * @param eleVal the XML element value
     * @return Double value
     * @throws WebServiceException when XML is not valid Double
     */
    private Double parseDouble(String eleName, String eleVal)
        throws WebServiceException
    {
        try {
            return Double.parseDouble(eleVal);
        } catch (NumberFormatException nfe) {
            throw invalidParamValueException(eleName, eleVal);
        }
    }

    /**
     * Only log if debugging is enabled.
     *
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
