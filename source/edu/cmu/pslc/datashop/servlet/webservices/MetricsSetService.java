/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DomainDao;
import edu.cmu.pslc.datashop.dao.LearnlabDao;
import edu.cmu.pslc.datashop.dao.MetricByDomainReportDao;
import edu.cmu.pslc.datashop.dao.MetricByLearnlabReportDao;
import edu.cmu.pslc.datashop.dao.MetricReportDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;

import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.MetricByDomainReportItem;
import edu.cmu.pslc.datashop.item.MetricByLearnlabReportItem;
import edu.cmu.pslc.datashop.item.MetricReportItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_INSTANCE_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.REMOTE_REQUESTS_NOT_ALLOWED;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueTooLongException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidDataException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.unknownErrorException;
import static edu.cmu.pslc.datashop.util.CollectionUtils.map;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

/**
 * Web service for setting Metrics Report values.
 *
 * @author Cindy Tipper
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricsSetService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for the get method of this service */
    private static final Set<String> VALID_GET_PARAMS = set(INSTANCE_ID, POST_DATA);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public MetricsSetService(HttpServletRequest req,
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

            RemoteInstanceItem remoteInstance = validateRemoteInstanceId();

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

            // Create the metric_report row.
            MetricReportDao mrDao = DaoFactory.DEFAULT.getMetricReportDao();
            MetricReportItem reportItem = new MetricReportItem();
            reportItem.setTime(new Date());
            reportItem.setRemoteInstance(remoteInstance);
            mrDao.saveOrUpdate(reportItem);

            // Parse file and create metric_by_domain_report and
            // metric_by_learnlab_report rows.
            parsePostData(reportItem);

            writeSuccess("Metrics data added for remote instance " + remoteInstance.getId());
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
     * Parse postData, creating rows in the metric_by_*_report tables.
     * @param reportItem the metric report
     * @throws WebServiceException when postData is not valid
     */
    private void parsePostData(MetricReportItem reportItem) throws WebServiceException {

        BufferedReader br = new BufferedReader(new StringReader(stringParam(POST_DATA)));
        String strLine;
        int cnt = 0;

        //Read post data Line By Line
        try {
            while ((strLine = br.readLine()) != null) {
                if (strLine.lastIndexOf("\r") >= 0) {
                    strLine = strLine.substring(0, strLine.length() - 1);
                }
                String[] cols = strLine.split("\t");
                //check headers and number of columns
                if (cols.length != MetricReportItem.HEADERS.size()) {
                    String errorStr = "Wrong number of columns in data; "
                        + "expected " + MetricReportItem.HEADERS.size() + " but there were " + cols.length + ".";
                    logger.info(errorStr);
                    throw invalidDataException(errorStr);
                }
                if (cnt == 0) {
                    for (int i = 0; i < MetricReportItem.HEADERS.size(); i++) {
                        if (!cols[i].equals(MetricReportItem.HEADERS.get(i))) {
                            String errorStr = "Column header \"" + cols[i] + "\" is not valid.";
                            logger.info(errorStr);
                            throw invalidDataException(errorStr);
                        }
                    }
                } else {
                    String category = cols[0];
                    if (category.startsWith(MetricReportItem.DOMAIN)) {
                        parseDomainMetric(cols, reportItem);
                    } else if (category.startsWith(MetricReportItem.LEARNLAB)) {
                        parseLearnLabMetric(cols, reportItem);
                    } else {
                        String errorStr = "Category does not start with expected 'Domain:' "
                            + "or 'LearnLab:' prefix: " + category;
                        logDebug(errorStr);
                        throw invalidDataException(errorStr);
                    }
                }
                cnt++;
            }
        } catch (IOException ex) {
            throw unknownErrorException();
        }
    }

    /**
     * Helper method to parse metric report row.
     * @param cols String array of single row of metric report
     * @param reportItem the metric_report item
     * @throws WebServiceException when postData is not valid
     */
    private void parseDomainMetric(String[] cols, MetricReportItem reportItem)
        throws WebServiceException
    {
        int index = cols[0].indexOf(MetricReportItem.SEP);
        if (index < 0) {
            String errorStr =
                "Expected category separator '" + MetricReportItem.SEP + "' not found.";
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }

        MetricByDomainReportItem item = new MetricByDomainReportItem();
        item.setMetricReport(reportItem);

        String domain = cols[0].substring(index+1);
        DomainDao domainDao = DaoFactory.DEFAULT.getDomainDao();
        DomainItem domainItem = domainDao.findByName(domain);
        if (domainItem == null) {
            String errorStr = "Invalid Domain: " + domain;
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }
        item.setDomain(domainItem);

        String invalidCol = MetricReportItem.HEADERS.get(1);
        String value = cols[1];
        try {
            // Files
            item.setFiles(parseInt(value));

            // Papers
            invalidCol = MetricReportItem.HEADERS.get(2);
            value = cols[2];
            item.setPapers(parseInt(value));

            // Datasets
            invalidCol = MetricReportItem.HEADERS.get(3);
            value = cols[3];
            item.setDatasets(parseInt(value));

            // Actions
            invalidCol = MetricReportItem.HEADERS.get(4);
            value = cols[4];
            item.setActions(parseInt(value));

            // Students
            invalidCol = MetricReportItem.HEADERS.get(5);
            value = cols[5];
            item.setStudents(parseInt(value));

            // Hours
            invalidCol = MetricReportItem.HEADERS.get(6);
            value = cols[6];
            item.setHours(parseDouble(value));

        } catch (NumberFormatException nfe) {
            String errorStr = "Invalid value for column '" + invalidCol + "': " + value + ".";
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }

        MetricByDomainReportDao dao = DaoFactory.DEFAULT.getMetricByDomainReportDao();
        dao.saveOrUpdate(item);
    }

    /**
     * Helper method to parse metric report row.
     * @param cols String array of single row of metric report
     * @param reportItem the metric_report item
     * @throws WebServiceException when postData is not valid
     */
    private void parseLearnLabMetric(String[] cols, MetricReportItem reportItem)
        throws WebServiceException
    {
        int index = cols[0].indexOf(MetricReportItem.SEP);
        if (index < 0) {
            String errorStr =
                "Expected category separator '" + MetricReportItem.SEP + "' not found.";
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }

        MetricByLearnlabReportItem item = new MetricByLearnlabReportItem();
        item.setMetricReport(reportItem);

        String learnlab = cols[0].substring(index+1);
        LearnlabDao learnlabDao = DaoFactory.DEFAULT.getLearnlabDao();
        LearnlabItem learnlabItem = learnlabDao.findByName(learnlab);
        if (learnlabItem == null) {
            String errorStr = "Invalid LearnLab: " + learnlab;
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }
        item.setLearnlab(learnlabItem);

        String invalidCol = MetricReportItem.HEADERS.get(1);
        String value = cols[1];
        try {
            // Files
            item.setFiles(parseInt(value));

            // Papers
            invalidCol = MetricReportItem.HEADERS.get(2);
            value = cols[2];
            item.setPapers(parseInt(value));

            // Datasets
            invalidCol = MetricReportItem.HEADERS.get(3);
            value = cols[3];
            item.setDatasets(parseInt(value));

            // Actions
            invalidCol = MetricReportItem.HEADERS.get(4);
            value = cols[4];
            item.setActions(parseInt(value));

            // Students
            invalidCol = MetricReportItem.HEADERS.get(5);
            value = cols[5];
            item.setStudents(parseInt(value));

            // Hours
            invalidCol = MetricReportItem.HEADERS.get(6);
            value = cols[6];
            item.setHours(parseDouble(value));

        } catch (NumberFormatException nfe) {
            String errorStr = "Invalid value for column '" + invalidCol + "': " + value + ".";
            logger.info(errorStr);
            throw invalidDataException(errorStr);
        }

        MetricByLearnlabReportDao dao = DaoFactory.DEFAULT.getMetricByLearnlabReportDao();
        dao.saveOrUpdate(item);
    }

    /**
     * Only log if debugging is enabled.
     *
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
