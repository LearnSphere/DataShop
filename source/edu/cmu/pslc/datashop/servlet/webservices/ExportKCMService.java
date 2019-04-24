/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;

import edu.cmu.pslc.datashop.helper.UserLogger;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.servlet.export.AbstractExportBean;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportHandler;

import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelExportBean;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_SKILL_MODEL;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import static java.lang.String.format;

/**
 * Web service for export KC models.
 *
 * @author Cindy Tipper
 * @version $Revision: 15775 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-18 12:06:11 -0500 (Tue, 18 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExportKCMService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for this service */
    private static final Set<String> VALID_PARAMS = set(DATASET_ID, KC_MODEL);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public ExportKCMService(HttpServletRequest req,
                            HttpServletResponse resp,
                            Map<String, Object> params) {
        super(req, resp, params);
    }

    /** Sleep for 3 seconds before retrying.
     *  1000 milliseconds in a second. */
    private static final int MILLISECONDS_TO_SLEEP = 3000;

    /**
     * Export KC model(s).
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_PARAMS);

            SkillModelItem kcModel = validateKCModelParam();

            UserItem userItem = getAuthenticatedUser();
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem datasetItem = datasetDao.get(datasetParam());

            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            String authLevel = authDao.getAuthLevel(userItem, datasetItem);

            DatasetContext datasetContext = new DatasetContext(datasetItem, userItem, authLevel);

            KCModelExportHandler handler = 
                new KCModelExportHandler(getReq(), getResp(), datasetContext);

            // Start the export...
            handler.startExport();

            // Monitor progress...
            Integer percent = handler.checkStatus();
            if (percent != null) {
                while (percent < 100) {
                    Thread.sleep(MILLISECONDS_TO_SLEEP);
                    percent = handler.checkStatus();
                }
            } else {
                throw new WebServiceException(UNKNOWN_ERR, "KC Model export bean unexpectedly null.");
            }

            // Get export file...
            handler.writeExportFile();

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
     * Validate the specified KC model name, return null if not found.
     * @return SkillModelItem, if found
     * @throws WebServiceException when the name is not valid
     */
    private SkillModelItem validateKCModelParam()
        throws WebServiceException
    {
        SkillModelItem result = null;

        UserItem user = getAuthenticatedUser();
        if (stringParam(KC_MODEL) != null) {
            result = helper().kcModelForName(user, datasetParam(), stringParam(KC_MODEL));
        }

        // If KCM still null, named skill model doesn't exist.
        if (result == null) {
            throw new WebServiceException(INVALID_SKILL_MODEL,
                                          "Skill model '" + stringParam(KC_MODEL) + "' is not valid.");
        }

        return result;
    }

    /**
     * Helper class for handling the KCM export calls and responses.
     */
    class KCModelExportHandler extends AbstractExportHandler {

        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param datasetContext {@link DatasetContext}
         */
        public KCModelExportHandler(HttpServletRequest req, HttpServletResponse resp,
                                    DatasetContext datasetContext) {
            super(req, resp, datasetContext, UserLogger.MODEL_EXPORT);
        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {
            DatasetContext datasetContext = getDatasetContext();

            String modelName = stringParam(KC_MODEL);

            SkillModelItem kcModel = null;
            try {
                kcModel = helper().kcModelForName(getAuthenticatedUser(),
                                                  datasetParam(), modelName);
            } catch (WebServiceException wse) {
                logger.error("Caught known web service error " + wse.getErrorCode()
                             + ": '" + wse.getErrorMessage() + "'");
                writeError(wse);
            } catch (Exception e) {
                logger.error("Something unexpected went wrong processing web service request.", e);
                writeInternalError();
            }

            List <SkillModelItem> skillModelList = new ArrayList<SkillModelItem>();
            if (kcModel != null) {
                skillModelList.add(kcModel);
            } else {
                //skillModelList.add(datasetContext.getDataset().getSkillModelItems());
            }

            KCModelExportBean exportBean = HelperFactory.DEFAULT.getKCModelExportBean();
            exportBean.setAttributes(datasetContext.getDataset(), skillModelList,
                                     datasetContext.getUser());

            return exportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getKCModelExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setKCModelExportBean((KCModelExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String KCM_TYPE = "kcm";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return KCM_TYPE;
        }

        public static final String CONTENT_TYPE = "text/cvs; charset=UTF-8";

        /**
         * Process the export request.
         * @return boolean indication the request was processed and results returned, false if not
         * processed or no response sent.
         */
        public Boolean startExport() {

            String paramsString = "";
            for (String param :  (Set<String>)getReq().getParameterMap().keySet()) {
                String p = getReq().getParameter(param);
                // Don't print really long params..
                if (p.length() < 100) {
                    paramsString += param + "(" + p + "), ";
                } else {
                    paramsString += param + "(...)";
                }
            }
            logger.debug(paramsString);

            logDebug("Processing export request.");
            Boolean isBeanRunning = false;

            // If the bean is currently in the session, get it.
            AbstractExportBean bean = getExportBean();
            
            // This handles the case where the export bean
            // in the session is not up to date with the system.
            if (bean != null && !bean.isRunning()) {
                bean.stop();
                bean = null;
            }
            
            // If the bean is null, then create it
            if (bean == null) {
                bean = createExportBean(); //create a new bean.
            }
            // If the bean is not running then start it
            if (bean != null && !bean.isRunning()) {
                new Thread(bean).start();
                logInfo("Started new export thread.");
            } else {
                // The bean was already started by another process.
                // Piggy back it.
                isBeanRunning = true;
            }
            
            setExportBean(bean);
            
            return true;
        }

        public Integer checkStatus() {

            logInfo("Export check.  Export type :: ", UserLogger.MODEL_EXPORT);

            AbstractExportBean bean = getExportBean();
            if (bean == null) {
                logger.warn("Export bean was null when checking status.");
                return null;
            } else {
                String response = "Building KC Model export ...";
                
                int percent = bean.getPercent();
                if (percent == AbstractExportBean.ZERO_ROWS_ERROR_CODE) {
                    response = "There are zero rows in this export.";
                } else if (percent == AbstractExportBean.UNKNOWN_ERROR_CODE) {
                    response = "An unknown error occurred.";
                }
                
                logDebug("processRequest, Percent: ", percent, ", Response: ", response);
                return percent;
            }
        }

        public Boolean cancelExport() {

            logInfo("Export canceled.");
            AbstractExportBean bean = getExportBean();
            if (bean == null) {
                logger.warn("Export Bean was null when canceling.");
            } else {
                bean.stop();
                try {
                    logInfo("Calling thread.join() to wait for thread to stop.");
                    new Thread(bean).join();
                } catch (InterruptedException e) {
                    logger.warn("AbstractExportBean thread was interrupted "
                                + "when attempting to stop.");
                }
            }
            removeExportBean();
            return true;
        }

        public Boolean writeExportFile() {

            logInfo("Getting export file.");
            File exportFile = null;
            AbstractExportBean bean = getExportBean();

            if (bean == null) {
                logger.warn("Export Bean was null when getting file.");
            } else {

                exportFile = bean.getResultsFile();
                if (exportFile != null) {
                    logDebug("Export File's absolute path is: ",
                             exportFile.getAbsolutePath());
                }
            }

            if (exportFile != null) {
                String fileNameType = getExportFileNameType(bean);
                String cleanedFileName =
                    AbstractServlet.getExportFileName(getDatasetContext().getDataset(), fileNameType);
                String fileName = cleanedFileName + ".txt";
                getResp().setContentType(CONTENT_TYPE);
                getResp().addHeader("Content-Disposition", "attachment; filename=" + fileName);
                
                ServletOutputStream outStream = null;
                BufferedInputStream buf = null;
                FileInputStream input = null;
                try {
                    outStream = getResp().getOutputStream();
                    getResp().setContentLength((int)exportFile.length());
                    input = new FileInputStream(exportFile);
                    
                    if (input != null) {
                        buf = new BufferedInputStream(input);
                        int readBytes = 0;
                        while ((readBytes = buf.read()) != -1) {
                            outStream.write(readBytes);
                        }
                        outStream.flush();
                    }
                } catch (Exception e) {
                    logger.error("An exception occurred while writing export file to client.", e);
                } finally {
                    try {
                        if (outStream != null) { outStream.close(); }
                        if (buf != null) { buf.close(); }
                        if (input != null) { input.close(); }
                    } catch (IOException ioe) {
                        logger.warn("Exception cleaning up after export.", ioe);
                    }
                }
            }

            bean.deleteTempFile();
            bean.stop();
            removeExportBean();
            return true;
        }
    }
}
