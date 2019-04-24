/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
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

import edu.cmu.pslc.datashop.dao.CurriculumDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.RemoteDatasetInfoDao;
import edu.cmu.pslc.datashop.dao.RemoteSkillModelDao;

import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.dto.DTO;

import edu.cmu.pslc.datashop.item.CurriculumItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.RemoteDatasetInfoItem;
import edu.cmu.pslc.datashop.item.RemoteSkillModelItem;
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
 * Web service for setting dataset meta-data values.
 *
 * @author Cindy Tipper
 * @version $Revision: 13164 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-21 10:50:17 -0400 (Thu, 21 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetSetInfoService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(DATASET_ID, POST_DATA);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public DatasetSetInfoService(HttpServletRequest req,
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

            DatasetItem dataset = wsUserLog.getDataset();

            // Ensure that this is a DataShop master server with a request handling user defined
            UserItem remoteRequestsUser = DataShopInstance.getMasterUser();
            if (DataShopInstance.isSlave() || remoteRequestsUser == null) {
                throw new WebServiceException(REMOTE_REQUESTS_NOT_ALLOWED,
                    "This server is not meant to handle requests from remote DataShop instances.");
            }
            
            // Ensure that only the appointed user can modify the remote dataset
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!authenticatedUser.getId().equals(remoteRequestsUser.getId())) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                    "Insufficient privileges to create a new dataset remotely.");
            }

            // Parse the XML.
            RemoteDatasetInfoItem item = parsePostData(dataset);
            RemoteDatasetInfoDao dao = DaoFactory.DEFAULT.getRemoteDatasetInfoDao();
            dao.saveOrUpdate(item);

            // Persist KCs...
            RemoteSkillModelDao skillDao = DaoFactory.DEFAULT.getRemoteSkillModelDao();
            List<RemoteSkillModelItem> skillModels = item.getSkillModelsExternal();
            for (RemoteSkillModelItem rsmi : skillModels) {
                skillDao.saveOrUpdate(rsmi);
            }

            // Persist any dataset changes too.
            DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
            dsDao.saveOrUpdate(dataset);

            writeSuccess("Meta-data added for remote dataset " + dataset.getId());
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
     * Parse postData, reading the remote dataset meta-data from XML.
     * @param dataset the DatasetItem
     * @return RemoteDatasetInfoItem dataset meta-data, read from XML
     * @throws WebServiceException when postData is not valid
     */
    private RemoteDatasetInfoItem parsePostData(DatasetItem dataset)
        throws WebServiceException
    {
        RemoteDatasetInfoItem result = createRemoteDatasetInfo(dataset);

        Element messageElement = null;
        try {
            StringReader reader = new StringReader(stringParam(POST_DATA));
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();
            Element dsElement = root.getChild(XMLConstants.DATASET_ELEMENT);
            if (dsElement == null) {
                logger.error("Invalid XML format: missing root or </"
                             + XMLConstants.DATASET_ELEMENT + "> element.");
                throw new WebServiceException(INVALID_XML_ERR, "Invalid XML format.");
            }
            String idAttr = dsElement.getAttributeValue("id");

            // get each element from XML
            Iterator messageIter = dsElement.getChildren().iterator();
            while (messageIter.hasNext()) {
                messageElement = (Element)messageIter.next();
                String elementName = messageElement.getName();
                if (elementName.equals("name")) {
                    dataset.setDatasetName(messageElement.getTextTrim());
                } else if (elementName.equals("project")) {
                    result.setProjectName(messageElement.getTextTrim());
                } else if (elementName.equals("pi_name")) {
                    result.setPiName(messageElement.getTextTrim());
                } else if (elementName.equals("data_provider_name")) {
                    result.setDpName(messageElement.getTextTrim());
                } else if (elementName.equals("domain")) {
                    dataset.setDomain(getDomain(messageElement.getTextTrim()));
                } else if (elementName.equals("learnlab")) {
                    dataset.setLearnlab(getLearnlab(messageElement.getTextTrim()));
                } else if (elementName.equals("curriculum")) {
                    dataset.setCurriculum(getCurriculum(messageElement.getTextTrim()));
                } else if (elementName.equals("tutor")) {
                    dataset.setTutor(messageElement.getTextTrim());
                } else if (elementName.equals("start_date")) {
                    dataset.setStartTime(ServletDateUtil.
                                         getDateFromString(messageElement.getTextTrim()));
                } else if (elementName.equals("end_date")) {
                    dataset.setEndTime(ServletDateUtil.
                                       getDateFromString(messageElement.getTextTrim()));
                } else if (elementName.equals("status")) {
                    dataset.setStatus(messageElement.getTextTrim());
                } else if (elementName.equals("description")) {
                    dataset.setDescription(messageElement.getTextTrim());
                } else if (elementName.equals("hypothesis")) {
                    dataset.setHypothesis(messageElement.getTextTrim());
                } else if (elementName.equals("acknowledgment")) {
                    dataset.setAcknowledgment(messageElement.getTextTrim());
                } else if (elementName.equals("citation")) {
                    result.setCitation(messageElement.getTextTrim());
                } else if (elementName.equals("school")) {
                    dataset.setSchool(messageElement.getTextTrim());
                } else if (elementName.equals("additional_notes")) {
                    dataset.setNotes(messageElement.getTextTrim());
                } else if (elementName.equals("access")) {
                    result.setAccessLevel(messageElement.getTextTrim());
                } else if (elementName.equals("public")) {
                    String publicStr = messageElement.getTextTrim();
                    result.setIsPublic(publicStr.equals("yes") ? true : false);
                } else if (elementName.equals("released")) {
                    String releasedStr = messageElement.getTextTrim();
                    dataset.setReleasedFlag(releasedStr.equals("yes") ? true : false);
                } else if (elementName.equals("has_study_data")) {
                    String studyStr = messageElement.getTextTrim();
                    dataset.setStudyFlag(studyStr.equalsIgnoreCase("yes") ?
                                         DatasetItem.STUDY_FLAG_YES :
                                         (studyStr.equalsIgnoreCase("no") ?
                                          DatasetItem.STUDY_FLAG_NO :
                                          DatasetItem.STUDY_FLAG_NOT_SPEC));
                } else if (elementName.equals("number_of_students")) {
                    result.setNumStudents(parseLong("number_of_students",
                                                    messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_student_hours")) {
                    result.setNumStudentHours(parseDouble("number_of_student_hours",
                                                          messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_unique_steps")) {
                    result.setNumUniqueSteps(parseLong("number_of_unique_steps",
                                                       messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_steps")) {
                    result.setNumSteps(parseLong("number_of_steps",
                                                 messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_transactions")) {
                    result.setNumTransactions(parseLong("number_of_transactions",
                                                        messageElement.getTextTrim()));
                } else if (elementName.equals("number_of_samples")) {
                    result.setNumSamples(parseLong("number_of_samples",
                                                   messageElement.getTextTrim()));
                } else if (elementName.equals("kc_model")) {
                    // The only attr of the kc_model is id; we don't need it.
                    RemoteSkillModelItem skillModel = createRemoteSkillModelItem(messageElement);
                    if (skillModel != null) { result.addSkillModel(skillModel); }
                }
            }

            // Now, look for missing elements. If the attr was unset/cleared on
            // the remote instance it won't be in the list.
            unsetMissingElements(dataset, result, dsElement.getChildren());

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
     * Helper method to unset or clear elements missing from the <dataset>.
     *
     * @param dataset the DatasetItem to be updated
     * @param remote the RemoteDatasetItem to be updated
     * @param children the list of child elements
     */
    private void unsetMissingElements(DatasetItem dataset, RemoteDatasetInfoItem remote,
                                      List<Element> children) {

        DTO.Properties props = DatasetDTO.class.getAnnotation(DTO.Properties.class);
        String[] datasetProps = props.properties();
        List<String> propList = new LinkedList<String>(Arrays.asList(datasetProps));

        Iterator<Element> iter = children.iterator();
        while (iter.hasNext()) {
            String eleName = WebServiceXMLMessage.camelizeTag(iter.next().getName());
            propList.remove(eleName);
        }

        // Now process what's left...
        Iterator<String> pIter = propList.iterator();
        while (pIter.hasNext()) {
            String elementName = pIter.next();

            // Only certain properties can be reset, or cleared.
            if (elementName.equals("project")) {
                remote.setProjectName(null);
            } else if (elementName.equals("piName")) {
                remote.setPiName(null);
            } else if (elementName.equals("dataProviderName")) {
                remote.setDpName(null);
            } else if (elementName.equals("domain")) {
                dataset.setDomain(null);
            } else if (elementName.equals("learnlab")) {
                dataset.setLearnlab(null);
            } else if (elementName.equals("curriculum")) {
                dataset.setCurriculum(null);
            } else if (elementName.equals("tutor")) {
                dataset.setTutor(null);
            } else if (elementName.equals("startDate")) {
                dataset.setStartTime(null);
            } else if (elementName.equals("endDate")) {
                dataset.setEndTime(null);
            } else if (elementName.equals("status")) {
                dataset.setStatus(null);
            } else if (elementName.equals("description")) {
                dataset.setDescription(null);
            } else if (elementName.equals("hypothesis")) {
                dataset.setHypothesis(null);
            } else if (elementName.equals("acknowledgment")) {
                dataset.setAcknowledgment(null);
            } else if (elementName.equals("citation")) {
                remote.setCitation(null);
            } else if (elementName.equals("school")) {
                dataset.setSchool(null);
            } else if (elementName.equals("additionalNotes")) {
                dataset.setNotes(null);
            }
        }
    }

    /**
     * Helper method for creating a RemoteDatasetInfoItem for a given DatasetItem.
     * If a row already exists in the db for this dataset, it will be deleted.
     * @param dataset the DatasetItem
     * @return RemoteDatasetInfoItem the item
     */
    private RemoteDatasetInfoItem createRemoteDatasetInfo(DatasetItem dataset) {

        RemoteDatasetInfoDao dao = DaoFactory.DEFAULT.getRemoteDatasetInfoDao();

        List<RemoteDatasetInfoItem> list = dao.findByDataset(dataset);
        for (RemoteDatasetInfoItem item : list) {
            dao.delete(item);
        }

        RemoteDatasetInfoItem result = new RemoteDatasetInfoItem();
        result.setDataset(dataset);

        return result;
    }

    /**
     * Helper method for parsing <kc_model> XML and generating an item.
     * @param kcModelEle the <kc_model> Element
     * @return RemoteSkillModelItem the item
     * @throws WebServiceException when postData is not valid
     */
    private RemoteSkillModelItem createRemoteSkillModelItem(Element kcModelEle)
        throws WebServiceException
    {
        List<Element> children = kcModelEle.getChildren();
        if (children == null) { return null; }

        RemoteSkillModelItem result = new RemoteSkillModelItem();

        for (Element c : children) {
            String childName = c.getName();
            if (childName.equals("name")) {
                result.setSkillModelName(c.getTextTrim());
            } else if (childName.equals("aic")) {
                result.setAic(parseDouble("aic", c.getTextTrim()));
            } else if (childName.equals("bic")) {
                result.setBic(parseDouble("bic", c.getTextTrim()));
            } else if (childName.equals("log_likelihood")) {
                result.setLogLikelihood(parseDouble("log_likelihood", c.getTextTrim()));
            } else if (childName.equals("logistic_regression_model_status")) {
                result.setLfaStatus(c.getTextTrim());
            } else if (childName.equals("logistic_regression_model_status_description")) {
                result.setLfaStatusDescription(c.getTextTrim());
            } else if (childName.equals("observations_with_kcs")) {
                result.setNumObservations(parseInteger("observations_with_kcs",
                                                       c.getTextTrim()));
            } else if (childName.equals("student_stratified_cross_validation_rmse")) {
                result.setCvStudentStratifiedRmse(
                         parseDouble("student_stratified_cross_validation_rmse", c.getTextTrim()));
            } else if (childName.equals("step_stratified_cross_validation_rmse")) {
                result.setCvStepStratifiedRmse(
                         parseDouble("step_stratified_cross_validation_rmse", c.getTextTrim()));
            } else if (childName.equals("unstratified_cross_validation_rmse")) {
                result.setCvUnstratifiedRmse(parseDouble("unstratified_cross_validation_rmse",
                                                         c.getTextTrim()));
            } else if (childName.equals("unstratified_number_of_observations")) {
                result.setUnstratifiedNumObservations(
                         parseInteger("unstratified_number_of_observations", c.getTextTrim()));
            } else if (childName.equals("unstratified_number_of_parameters")) {
                result.setUnstratifiedNumParameters(
                         parseInteger("unstratified_number_of_parameters", c.getTextTrim()));
            } else if (childName.equals("cross_validation_status")) {
                result.setCvStatus(c.getTextTrim());
            } else if (childName.equals("cross_validation_status_description")) {
                result.setCvStatusDescription(c.getTextTrim());
            } else if (childName.equals("number_of_kcs")) {
                result.setNumSkills(parseInteger("number_of_kcs", c.getTextTrim()));
            }
        }

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
     * Helper method to get DomainItem given domain name.
     * @param domainName
     * @return DomainItem null if not found
     */
    private DomainItem getDomain(String domainName) {
        return DaoFactory.DEFAULT.getDomainDao().findByName(domainName);
    }

    /**
     * Helper method to get LearnlabItem given learnlab name.
     * @param learnlabName
     * @return LearnlabItem null if not found
     */
    private LearnlabItem getLearnlab(String learnlabName) {
        return DaoFactory.DEFAULT.getLearnlabDao().findByName(learnlabName);
    }

    /**
     * Helper method to get CurriculumItem given the name.
     * @param curriculumName
     * @return CurriculumItem null if not found
     */
    private CurriculumItem getCurriculum(String curriculumName) {
        CurriculumItem result = new CurriculumItem();
        result.setCurriculumName(curriculumName);

        CurriculumDao curriculumDao = DaoFactory.DEFAULT.getCurriculumDao();
        return (CurriculumItem)curriculumDao.findOrCreate(curriculumDao.findAll(), result);
    }

    /**
     * Only log if debugging is enabled.
     *
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
