/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FilterDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleHistoryDao;
import edu.cmu.pslc.datashop.dao.hibernate.SampleMapper;
import edu.cmu.pslc.datashop.dto.CachedFileInfo;
import edu.cmu.pslc.datashop.helper.DatasetState;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.SampleHistoryItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.export.AggregatorBean;
import edu.cmu.pslc.datashop.servlet.export.CachedExportFileReader;
import edu.cmu.pslc.datashop.servlet.export.StepRollupExportHelper;
import edu.cmu.pslc.datashop.servlet.export.StudentProblemExportHelper;
import edu.cmu.pslc.datashop.servlet.export.TransactionExportHelper;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;
import static edu.cmu.pslc.datashop.util.FormattingUtils.displayObject;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 * This class contains the business tier logic for manipulating and creating Samples
 * on a dataset.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15837 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleSelectorHelper {

    /** Number format for to include commas but no decimals. */
    private static final DecimalFormat COMMA_DF = new DecimalFormat("#,###,##0");

    /** Form parameter */
    private static final String SAMPLE_SAVE_PARAM = "sampleSave";
    /** Form parameter */
    private static final String SAMPLE_ID_PARAM = "sampleSelectorId";
    /** Form parameter */
    private static final String SAMPLE_NAME_PARAM = "sampleName";
    /** Form parameter */
    private static final String SAMPLE_DESCRIPTION_PARAM = "sampleDescription";
    /** Form parameter */
    private static final String IS_GLOBAL_PARAM = "isGlobal";
    /** Form parameter.  Filter parameters are followed by a _ then the index of the filter. */
    private static final String FILTER_CLASS_PARAM = "filterClass_";
    /** Form parameter */
    private static final String FILTER_ATTRIB_PARAM = "filterAttribute_";
    /** Form parameter */
    private static final String FILTER_STRING_PARAM = "filterString_";
    /** Form parameter */
    private static final String FILTER_OPERATOR_PARAM = "filterOperator_";
    /** Form parameter */
    private static final String FILTER_POSITION_PARAM = "filterPosition_";
    /** Form parameter */
    private static final String UPDATE_PREVIEW_PARAM = "samplePreviewUpdate";
    /** Form parameter */
    private static final String LOAD_SAMPLE_PARAM = "loadSample";
    /** Form parameter */
    private static final String DELETE_SAMPLE_PARAM = "deleteSample";

    /** Preview Limit Default */
    private static final Integer PREVIEW_LIMIT_DEFAULT = new Integer(10);
    /** Preview Offset Default */
    private static final Integer PREVIEW_OFFSET_DEFAULT = new Integer(0);
    /** Magic One hundred */
    private static final int ONE_HUNDRED = 100;

    /** Maximum length of sample name. */
    private static final int MAX_LENGTH_SAMPLE_NAME = 100;
    /** Maximum length of sample description. */
    private static final int MAX_LENGTH_SAMPLE_DESC = 255;

    /** Debug logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** TransactionExportHelper for removing cached transaction exports. */
    private TransactionExportHelper helper;

    /** Default Constructor. */
    public SampleSelectorHelper() {
        logDebug("SampleSelectorHelper.constructor");
    }

    /** Constant. */
    private static final int MAX_NUM_CHARS_TO_LOG_IN_RESPONSE = 75;

    /**
     * Process the HttpServletRequest for updates to the sample selector.
     * Any update will be returned as a string for an AJAX update.
     * @param req the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @param baseDir the base directory where cached export files are stored in the system.
     * @param aggSpFilePath the file path to the aggregator stored procedure file.
     * @return a String of the response, null if there is no response.
     */
    public String update(HttpServletRequest req, DatasetContext datasetContext,
            String baseDir, String aggSpFilePath) {
        final String user = (String)datasetContext.getUser().getId();
        logger.info("SampleSelectorHelper.update : starting : user " + user);

        //check if a save was called.
        String updateResponse = saveSample(datasetContext.getUser(), req, datasetContext, aggSpFilePath, baseDir);
        //check if a load was called
        if (updateResponse == null) { updateResponse = loadSample(req, datasetContext); }
        //check if a update preview was called.
        if (updateResponse == null) { updateResponse = updateSamplePreview(req, datasetContext); }
        //check if a delete was called.
        if (updateResponse == null) { updateResponse = deleteSample(req, datasetContext, baseDir); }
        // check if agg sample was called.
        if (updateResponse == null) { updateResponse = aggSample(req, datasetContext, aggSpFilePath, baseDir); }
        //check if a sample aggregation check was made
        if (updateResponse == null) { updateResponse = aggregateCheck(req, datasetContext); }
        //check if a cancel aggregation was made
        if (updateResponse == null) { updateResponse = aggregateCancel(req, datasetContext); }

        String cutOffResponse = updateResponse;
        if (updateResponse != null) {
            int maxLen = updateResponse.length() > MAX_NUM_CHARS_TO_LOG_IN_RESPONSE
                ? MAX_NUM_CHARS_TO_LOG_IN_RESPONSE : updateResponse.length();
            cutOffResponse = updateResponse.substring(0, maxLen);
        }
        logger.info("SampleSelectorHelper.update : finished : user "
                + user + " with response " + cutOffResponse);
        return updateResponse;
    }

    /**
     * Checks the status of an aggregation for a sample.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @return a string of response for the updated sample.
     */
    private String aggregateCheck(HttpServletRequest request, DatasetContext datasetContext) {
        String exportCheckParam = request.getParameter("aggregator_check");
        String s2dReqParam = request.getParameter("s2dSelector");
        if (exportCheckParam != null) {
            JSONObject beanResponseJSON = new JSONObject();
            HttpSession httpSession = request.getSession();
            AggregatorBean ab;
            synchronized (httpSession) {
                ab = (AggregatorBean) httpSession.getAttribute("aggregatorBean");
            }
            try {
                if (ab == null) {
                    if (s2dReqParam != null) {
                        logger.warn("Aggregator Bean is complete.");
                        beanResponseJSON.put("status", "100");
                        beanResponseJSON.put("message", "Aggregation of sample has completed.");
                    } else {
                        logger.warn("Aggregator Bean was null when checking percent.");
                        beanResponseJSON.put("status", "-1");
                        beanResponseJSON.put("message", "Aggregation of sample has failed.");
                    }
                    return beanResponseJSON.toString();
                } else {
                    int percent = ab.getPercent();
                    if (percent < 0 || percent == ONE_HUNDRED) {
                        httpSession.removeAttribute("aggregatorBean");
                    }
                    beanResponseJSON.put("status", percent);
                    beanResponseJSON.put("message", "Aggregating sample data ... ");
                    return beanResponseJSON.toString();
                }
            } catch (JSONException jsonException) {
                logger.error("JSONException thrown in the AbstractExportBean.  That's bad."
                        + jsonException.getMessage());
                return "-1";
            }
        }
        return null;
    }

    /**
     * Cancels the aggregation of a sample and deletes the sample.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @return a string of response for the updated sample.
     */
    private String aggregateCancel(HttpServletRequest request, DatasetContext datasetContext) {
        String aggCancelParam = request.getParameter("aggregator_cancel");
        if (aggCancelParam != null) {
            HttpSession httpSession = request.getSession();
            AggregatorBean ab;
            synchronized (httpSession) {
                ab = (AggregatorBean) httpSession.getAttribute("aggregatorBean");
            }
            if (ab == null) {
                logger.warn("Aggregator Bean was null when attempting to cancel.");
                return "FAILED";
            } else {
                ab.cancel();
                httpSession.removeAttribute("aggregatorBean");
                return "SUCCESS";
            }
        }
        return null;
    }

    /**
     * Checks the request to see if they sample is being updated.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @param aggSpFilePath the file path to the aggregator stored procedure file.
     * @param baseDir the base directory where cached export files are stored in the system.
     * @return a string of response for the updated sample.
     */
    private String aggSample(HttpServletRequest request,
            DatasetContext datasetContext, String aggSpFilePath, String baseDir) {
        String aggSample = request.getParameter("aggSample"); //form parameter
        String response = null;

        if (aggSample != null) {

            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            logDebug("Agg Sample Parameter Found - Processing Sample");
            String sampleIdString = request.getParameter(SAMPLE_ID_PARAM);

            //check if we are updating a pre-existing or creating a new.
            if (sampleIdString != null && sampleIdString.compareTo("") != 0) {
                logDebug("Loading Existing sample.");
                SampleItem existingSample = sampleDao.get(new Integer(sampleIdString));

                Boolean requiresAgg = null;

                if (existingSample != null) {

                    DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
                    DatasetItem datasetItem = dsDao.find((Integer) existingSample.getDataset().getId());
                    requiresAgg = DatasetState.requiresAggregation(datasetItem, existingSample);
                    response = "SUCCESS|Sample has been aggregated successfully|" + existingSample.getId();
                    if (requiresAgg) {
                        //delete all existing metrics
                        DaoFactory.DEFAULT.getSampleMetricDao().deleteAll(existingSample);
                        //now update the sample
                        aggregateSample(existingSample, existingSample.getGlobalFlag(), request, aggSpFilePath);
                        response += "|AGGREGATING";
                    }
                }
                return response;
            }
        }
        return response;
    }

    /**
     * Checks the request to see if they sample is being updated.
     * @param userItem the user saving the sample
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @param aggSpFilePath the file path to the aggregator stored procedure file.
     * @param baseDir the base directory where cached export files are stored in the system.
     * @return a string of response for the updated sample.
     */
    private String saveSample(UserItem userItem, HttpServletRequest request,
            DatasetContext datasetContext, String aggSpFilePath, String baseDir) {
        String saveSample = request.getParameter(SAMPLE_SAVE_PARAM); //form parameter
        String response = null;

        if (saveSample != null) {
            boolean aggregateRequired = true;
            SampleItem sample = new SampleItem();
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            FilterDao filterDao = DaoFactory.DEFAULT.getFilterDao();
            logDebug("Save Sample Parameter Found - Processing Sample");
            String sampleIdString = request.getParameter(SAMPLE_ID_PARAM);
            try {
                sample = processFilters(request, datasetContext);
            } catch (FilterStringMaxLengthExceededException exception) {
                String msg = exception.getMessage();
                logger.info("FilterStringMaxLengthExceededException occurred: " + msg);
                response = "ERROR|Error: " + msg;
                return response;
            }

            sample.setDataset(datasetContext.getDataset());
            if (sampleDao.getTransactionsPreview(sample, 0, 1).size() < 1) {
                logDebug("No transactions for this sample.");
                response = "ERROR|Error: The sample must contain at least one transaction.";
                return response;
            }

            if (sampleIdString != null) {
                try {
                    sample.setId(new Integer(sampleIdString));
                } catch (NumberFormatException nfe) {
                    logger.warn("Caught NumberFormatException", nfe);
                    response = "ERROR|Error: There was an unexpected error saving your sample.";
                    return response;
                }
            }

            sample.setOwner(datasetContext.getUser());
            String datasetName = datasetContext.getDataset().getDatasetName();
            sample.setFilePath(FileUtils.cleanForFileSystem(datasetName));
            String sampleName = request.getParameter(SAMPLE_NAME_PARAM); //form parameter
            if (sampleName != null && !sampleName.equals("")) {
                if (sampleName.length() > MAX_LENGTH_SAMPLE_NAME) {
                    sampleName = sampleName.substring(0, MAX_LENGTH_SAMPLE_NAME);
                    logger.warn("Truncating sampleName to " + MAX_LENGTH_SAMPLE_NAME
                            + " characters to " + sampleName);
                }
                sample.setSampleName(sampleName);

                logDebug("Set name for sample:", sample);
            } else {
                logDebug("Sample Name is null");
                response = "ERROR|Error: You must enter a sample name.";
                return response;
            }

            // form parameter
            String sampleDescription = request.getParameter(SAMPLE_DESCRIPTION_PARAM);
            if (sampleDescription != null && !sampleDescription.equals("")) {
                if (sampleDescription.length() > MAX_LENGTH_SAMPLE_DESC) {
                    sampleDescription = sampleDescription.substring(0, MAX_LENGTH_SAMPLE_DESC);
                    logger.warn("Truncating sampleDescription to " + MAX_LENGTH_SAMPLE_DESC
                            + " characters to " + sampleDescription);
                }
                sample.setDescription(sampleDescription);
                logDebug("Set description:", sample);
            } else { logDebug("Sample Description is null"); }

            //get and set the global flag from the form.
            String isGlobal = request.getParameter(IS_GLOBAL_PARAM); //form parameter
            Boolean isGlobalBool = false;
            if (isGlobal != null) {
                if (isGlobal.equals("false")) {
                    sample.setGlobalFlag(false);
                    logDebug("Set isGlobal false for sample:", sample);
                } else {
                    // Defer making sample global until aggregation has finished
                    isGlobalBool = true;
                }
            } else {
                logDebug("Global flag was incorrectly null.");
                response = "ERROR|Error: Global flag was incorrectly null.";
                return response;
            }

            response = checkForErrors(sample, datasetContext);
            if (response != null) { return response; }

            //check if we are updating a pre-existing or creating a new.
            if (sampleIdString != null && sampleIdString.compareTo("") != 0) {
                logDebug("Loading Existing sample.");
                SampleItem existingSample = sampleDao.get(new Integer(sampleIdString));
                if (existingSample != null
                        && datasetContext.getUser().equals(existingSample.getOwner())
                        && !existingSample.getSampleName().equals(
                        SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME)) {

                    // Used for the samples page (part of the sample to dataset feature)
                    SamplesHelper samplesHelper = HelperFactory.DEFAULT.getSamplesHelper();

                    logDebug("Deleting Filters for Sample: ", existingSample.getId());

                    //test if the filter lists are different so we know if we need to
                    //aggregate the sample again.
                    Boolean differentFilters = differentFilters(existingSample, sample);
                    if (!differentFilters) { aggregateRequired = false; }
                    for (Iterator it = existingSample.getFiltersExternal().iterator();
                            it.hasNext();) {
                        FilterItem filterItem = (FilterItem)it.next();
                        filterItem = filterDao.get((Integer)filterItem.getId());
                        filterDao.delete(filterItem);
                    }
                    existingSample.clearFilters();
                    boolean sampleNameModified =
                        !existingSample.getSampleName().equals(sample.getSampleName());
                    SampleItem copyOfExistingSample = null;

                    if (sampleNameModified) {
                        // make a copy of the existing sample to use when updating the cached file.
                        copyOfExistingSample = createCopy(existingSample);
                        // Create sample history item
                        String infoString = "Renamed '" + existingSample.getSampleName()
                            + "' to '"
                            + sample.getSampleName() + "'";
                        String infoStringDSL = "Renamed '" + existingSample.getSampleName()
                                + "' [" +  existingSample.getId() + "] to '"
                                + sample.getSampleName() + "' [" + existingSample.getId() + "]";
                        samplesHelper.saveSampleHistory(userItem,
                            existingSample, SampleHistoryItem.ACTION_RENAME_SAMPLE,
                                infoString, null);
                        // Log sample rename action to the dataset system log
                        SystemLogger.log(existingSample.getDataset(), null, existingSample,
                                SystemLogger.ACTION_RENAME_SAMPLE,
                                infoStringDSL,
                                Boolean.TRUE,
                                null);

                    }

                    existingSample.setSampleName(sample.getSampleName());
                    existingSample.setDescription(sample.getDescription());
                    // Existing sample doesn't need to wait for aggregation to update global flag
                    existingSample.setGlobalFlag(isGlobalBool);

                    sampleDao.saveOrUpdate(existingSample);
                    logDebug("Existing Sample saved : ", existingSample);
                    // lets do this after the sample has been successfully updated.
                    if (sampleNameModified) {
                        updateCachedFile(datasetContext.getDataset(), copyOfExistingSample,
                                sample, baseDir);
                    }

                    //add each new filter to the existing sample..
                    for (FilterItem filterItem : sample.getFiltersExternal()) {
                        existingSample.addFilter(filterItem);
                    }

                    //save each filter of the new/updated sample.
                    for (FilterItem filter : existingSample.getFiltersExternal()) {
                        filterDao.saveOrUpdate(filter);
                        logDebug("Saving Filter: ", filter);
                    }



                    if (differentFilters) {
                        // Create sample history item
                        String infoString = "Modified filters for sample '" + existingSample.getSampleName()
                            + "'";
                        String infoStringDSL = "Modified filters for sample '" + existingSample.getSampleName()
                                + "' [" + existingSample.getId() + "]";
                        samplesHelper.saveSampleHistory(userItem,
                            existingSample, SampleHistoryItem.ACTION_MODIFY_FILTERS,
                                infoString, null);
                        // Log modify filters action to dataset system log
                        SystemLogger.log(existingSample.getDataset(), null, existingSample,
                                SystemLogger.ACTION_MODIFY_FILTERS,
                                infoStringDSL,
                                Boolean.TRUE,
                                null);

                    }

                } else {
                    if (existingSample == null) {
                        logDebug("Existing sample came back as null");
                    } else {
                        logDebug("Owner and user do not match, cannot edit");
                    }
                }
                response = "SUCCESS|Sample has been updated successfully|" + existingSample.getId();
                UserLogger.log(sample.getDataset(), sample.getOwner(),
                        UserLogger.SAMPLE_MODIFY,
                        "Modified sample '" + sample.getSampleName()
                            + "' (" + sample.getId() + ")");


                if (aggregateRequired) {
                    //delete all existing metrics
                    DaoFactory.DEFAULT.getSampleMetricDao().deleteAll(existingSample);
                    //now update the sample
                    aggregateSample(existingSample, isGlobalBool, request, aggSpFilePath);
                    response += "|AGGREGATING";
                }
                return response;
            } else {

                sampleDao.saveOrUpdate(sample);
                logDebug("New Sample saved : ", sample);

                //save each filter of the new/updated sample.
                for (FilterItem filter : sample.getFiltersExternal()) {
                    filterDao.saveOrUpdate(filter);
                }

                response = "SUCCESS|Sample has been saved successfully|" + sample.getId();
                sample = sampleDao.get((Integer)sample.getId());
                // Log create sample action to the user log
                UserLogger.log(sample.getDataset(), sample.getOwner(),
                        UserLogger.SAMPLE_CREATE,
                        "New sample '" + sample.getSampleName() + "' (" + sample.getId() + ")");
                // Create sample history item
                String infoString = "Created sample '" + sample.getSampleName()
                    + "'";
                String infoStringDSL = "Created sample '" + sample.getSampleName()
                        + "' [" + sample.getId() + "]";
                SamplesHelper samplesHelper = HelperFactory.DEFAULT.getSamplesHelper();
                samplesHelper.saveSampleHistory(userItem, sample,
                    SampleHistoryItem.ACTION_CREATE_SAMPLE, infoString, null);
                // Log create sample action to dataset system log
                SystemLogger.log(sample.getDataset(), null, sample,
                        SystemLogger.ACTION_CREATE_SAMPLE,
                        infoStringDSL,
                        Boolean.TRUE,
                        null);

                if (aggregateRequired) {
                    aggregateSample(sample, isGlobalBool, request, aggSpFilePath);
                    response += "|AGGREGATING";
                }
            }
        }
        return response;
    }

    /**
     * Aggregate data for the given sample.
     * @param sample the sample to aggregate for.
     * @param isGlobal may other users view this sample
     * @param req the HttpServletRequest object.
     * @param aggSpFilePath the file path to the aggregator stored procedure file.
     */
    private void aggregateSample(SampleItem sample, Boolean isGlobal, HttpServletRequest req,
            String aggSpFilePath) {
        logDebug("Aggregating data for sample : " + sample);
        logDebug("Removing old step roll-ups");
        int removed = DaoFactory.DEFAULT.getStepRollupDao().removeAll(sample);
        logDebug("Removed ", removed, "step roll-ups");
        logDebug("Removing old sample transaction mappings");
        removed = DaoFactory.DEFAULT.getSampleDao().removeAllTransactionMappings(sample);
        logDebug("Removed ", removed, " mappings");

        HttpSession httpSession = req.getSession();
        httpSession.removeAttribute("aggregatorBean");
        AggregatorBean aggregatorBean;
        synchronized (httpSession) {
            aggregatorBean =
                (AggregatorBean)httpSession.getAttribute("aggregatorBean");
            if (aggregatorBean == null) {
                aggregatorBean = HelperFactory.DEFAULT.getAggregatorBean();
                httpSession.setAttribute("aggregatorBean", aggregatorBean);
            }
        }

        logger.info("starting new thread");
        aggregatorBean.setAttributes(sample, isGlobal, aggSpFilePath);
        new Thread(aggregatorBean).start();
        logger.info("started new thread");
    }

    /**
     * Checks a sample for errors.  Returns a string of the error if there is one,
     * otherwise null.
     * @param sample the sample to test
     * @param datasetContext the DatasetContext for this user.
     * @return A string of the error, or null if no errors were found
     */
    private String checkForErrors(SampleItem sample, DatasetContext datasetContext) {
        String errorString = null;
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();

        if (sample.isAllData()) {
            errorString = "ERROR|You cannot name a sample '"
                + SampleItem.ALL_TRANSACTIONS_SAMPLE_NAME + "'."
                + " Please choose a different name.";
            return errorString;
        }

        //check that the user doesn't already have a sample with this name
        //if it is not an update.
        List userSamples = sampleDao.find(datasetContext.getDataset(),
                datasetContext.getUser(), sample.getSampleName());
        for (Iterator iter = userSamples.iterator(); iter.hasNext();) {
            SampleItem existingSample = (SampleItem)iter.next();
            if (!existingSample.getId().equals(sample.getId())) {
                errorString = "ERROR|You already have a sample with that name."
                    + " Please choose a different name.";
                 return errorString;
            }
        }

        //check that no one else has a global sample with the same name for this dataset
        if (sample.getGlobalFlag().booleanValue()) {
            List otherSamples = sampleDao.find(datasetContext.getDataset(),
                    sample.getGlobalFlag(), sample.getSampleName());
            for (Iterator iter = otherSamples.iterator(); iter.hasNext();) {
                SampleItem existingSample = (SampleItem)iter.next();
                if (!sample.getOwner().equals(existingSample.getOwner())) {
                    errorString = "ERROR|A shared sample with that name already exists. "
                         + "Please choose a different name.";
                     return errorString;
                }
            }
        }
        return errorString;
    }

    /**
     * Retrieves sample information given an ID.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     *
     * @return String of the response, null if no response.<br>
     * response should be returned as a tab delineated string with...<br>
     * Sample Id[tab]Sample Name[tab]Sample Description[tab]Is Global[newline]<br>
     * followed by a filter list...<br>
     * FilterClass[tab]FilterAttribute[tab]FilterString[tab]FilterOperator[newline]<br>
     */
    private String loadSample(HttpServletRequest request, DatasetContext datasetContext) {
        String loadSample = request.getParameter(LOAD_SAMPLE_PARAM);
        StringBuffer response = null;
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem sample = null;

        if (loadSample != null) {
            response = new StringBuffer();
            String sampleId = request.getParameter(SAMPLE_ID_PARAM);
            logDebug("Loading Sample with ID of : ", sampleId);
            sample = sampleDao.get(new Integer(sampleId));

            if (sample == null) {
                response.append("ERROR\tNo sample found");
                return response.toString();
            } else {
                datasetContext.getNavContext().setWorkingSample(sample);
            }

            response.append(sample.getId() + "\t");
            if (sample.getOwner().equals(datasetContext.getUser())
              && !sample.isAllData()) {
                response.append("true\t");
            } else {
                response.append("false\t");
            }
            response.append(sample.getSampleName() + "\t");
            if (sample.getDescription() != null) {
                response.append(sample.getDescription());
            }
            response.append("\t");
            response.append(sample.getGlobalFlag());

            //walk through list adding each filter to the response string.
            for (Iterator it = sample.getFiltersExternal().iterator(); it.hasNext();) {
                FilterItem filterItem = (FilterItem) it.next();
                response.append("\n" + filterItem.getClazz() + "\t");
                response.append(filterItem.getAttribute() + "\t");
                response.append(filterItem.getFilterString() + "\t");
                response.append(filterItem.getOperator());
            }

            return response.toString();
        }
        return null;
    }

    /**
     * Deletes a sample given an ID.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @param baseDir the base directory where cached export files are stored in the system.
     * @return String of the response, null if no response.
     */
    private String deleteSample(HttpServletRequest request, DatasetContext datasetContext,
            String baseDir) {
        String deleteSample = request.getParameter(DELETE_SAMPLE_PARAM);
        StringBuffer response = null;
        if (deleteSample != null) {
            helper = HelperFactory.DEFAULT.getTransactionExportHelper();
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            SampleItem sample = null;
            response = new StringBuffer();

            String sampleId = request.getParameter(SAMPLE_ID_PARAM);
            logDebug("Deleting Sample with ID of : ", sampleId);
            sample = sampleDao.get(new Integer(sampleId));
            if (sample == null) {
                response.append("ERROR\tNo sample matching selection found.");
                return response.toString();
            }

            UserItem thisUser = datasetContext.getUser();
            // Allow the sample owner or DS Admin to delete the sample
            if (sample.getOwner().equals(datasetContext.getUser())
                || (thisUser != null && thisUser.getAdminFlag())) {
                // Prevent deletion of All Data sample
                if (!sample.getSampleName().equalsIgnoreCase("all data")) {
                    SampleHistoryDao sampleHistoryDao = DaoFactory.DEFAULT.getSampleHistoryDao();
                    List<SampleHistoryItem> sampleHistory = sampleHistoryDao.find(sample);
                    for (SampleHistoryItem historyItem : sampleHistory) {
                        sampleHistoryDao.delete(historyItem);
                    }
                    SystemLogger.log(sample.getDataset(), null, sample,
                        SystemLogger.ACTION_DELETE_SAMPLE,
                        "Deleted sample '" + sample.getSampleName() + "' [" + sample.getId() + "]",
                        null,
                        null);
                    UserLogger.log(datasetContext.getDataset(),
                            datasetContext.getUser(), UserLogger.SAMPLE_DELETE,
                            "Deleted sample '" + sample.getSampleName() + "' (" + sample.getId() + ")");
                    sampleDao.delete(sample);

                }
            } else {
                response.append("ERROR\tDelete failed. You are not the owner of this sample!!!");
                return response.toString();
            }

            // if a cached transaction export exists, delete it.
            String cachedFileName = helper.getCachedFileName(sample, baseDir);
            if (cachedFileName != null) {
                if (helper.deleteFile(cachedFileName)) {
                       logger.info("Successfully deleted cached transaction export file '"
                        + cachedFileName + "'.");
                } else {
                    logger.error("Error occurred while attempting to remove"
                            + " cached transaction export "
                            + " for " + sample.getSampleName() + "(" + sample.getId() + ").");
                }
            }

            // if a cached step export exists, delete it.
            StepRollupExportHelper stepExportHelper =
                    HelperFactory.DEFAULT.getStepRollupExportHelper();
            String cachedStepFileName = stepExportHelper.getCachedFileName(sample, baseDir);
            if (cachedStepFileName != null) {
                if (stepExportHelper.deleteFile(cachedStepFileName)) {
                       logger.info("Successfully deleted cached step export file '"
                        + cachedStepFileName + "'.");
                } else {
                    logger.error("Error occurred while attempting to remove"
                            + " cached step export "
                            + " for " + sample.getSampleName() + "(" + sample.getId() + ").");
                }
            }

            // if a cached student-problem export exists, delete it.
            StudentProblemExportHelper spExportHelper =
                HelperFactory.DEFAULT.getStudentProblemExportHelper();
            String cachedStudentProblemFileName = spExportHelper.getCachedFileName(sample, baseDir);
            if (cachedStudentProblemFileName != null) {
                if (spExportHelper.deleteFile(cachedStudentProblemFileName)) {
                       logger.info("Successfully deleted cached student-problem export file '"
                        + cachedStudentProblemFileName + "'.");
                } else {
                    logger.error("Error occurred while attempting to remove"
                            + " cached student-problem export ," + cachedStudentProblemFileName
                            + ", for " + sample.getSampleName() + "(" + sample.getId() + ").");
                }
            }


            response.append("SUCCESS\tSample Deleted");
            return response.toString();
        }
        logDebug("deleteSample :: No action taken.");
        return null;
    }

    /**
     * Updates the preview code.
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @return String of HTML containing the internals of the data preview table.
     */
    private String updateSamplePreview(HttpServletRequest request, DatasetContext datasetContext) {
        String previewUpdate = request.getParameter(UPDATE_PREVIEW_PARAM);
        if (previewUpdate != null) {
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            logDebug("Performing Sample Preview Update : ", previewUpdate);

            SampleItem sample = null;
            try {
                sample = processFilters(request, datasetContext);
            } catch (FilterStringMaxLengthExceededException exception) {
                String msg = exception.getMessage();
                logger.info("FilterStringMaxLengthExceededException occurred: " + msg);
                return "MAX_LENGTH_ERROR|" + msg;
            }
            sample.setDataset(datasetContext.getDataset());

            StringBuffer htmlBuffer = new StringBuffer();
            List<FilterItem> filterList = sample.getFiltersExternal();

            //construct the header.
            Long numTrans = sampleDao.getNumTransactions(sample);
            if (numTrans == null) {
                return sampleDao.determineError(sample);
            }

            htmlBuffer.append("<table  id=\"dataPreviewTable\"><caption>");
            htmlBuffer.append(COMMA_DF.format(numTrans));
            htmlBuffer.append(" transactions in this sample; displaying the first 10</caption>");
            htmlBuffer.append("<thead><tr><th>Transaction Time</th>");

            HashSet columns = new HashSet();

            for (FilterItem filterItem : filterList) {
                columns.add(filterItem.getAttribute());
                if (filterItem.getAttribute().equals(
                        SampleMapper.CONDITION_NAME.get(SampleMapper.DB))) {
                    htmlBuffer.append("<th>" + SampleMapper.CONDITION.get(SampleMapper.UI) + " "
                        + SampleMapper.CONDITION_NAME.get(SampleMapper.UI) + "</th>");
                }
                if (filterItem.getAttribute().equals(
                        SampleMapper.CONDITION_TYPE.get(SampleMapper.DB))) {
                    htmlBuffer.append("<th>" + SampleMapper.CONDITION.get(SampleMapper.UI) + " "
                            + SampleMapper.CONDITION_TYPE.get(SampleMapper.UI) + "</th>");
                }
            }

            if (columns.contains(SampleMapper.DATASET_LEVEL_TITLE.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.DATASET_LEVEL.get(SampleMapper.UI) + " "
                        + SampleMapper.DATASET_LEVEL_TITLE.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.DATASET_LEVEL_NAME.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.DATASET_LEVEL.get(SampleMapper.UI) + " "
                        + SampleMapper.DATASET_LEVEL_NAME.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.PROBLEM_NAME.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.PROBLEM.get(SampleMapper.UI) + " "
                        + SampleMapper.PROBLEM_NAME.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.PROBLEM_DESCRIPTION.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.PROBLEM.get(SampleMapper.UI) + " "
                        + SampleMapper.PROBLEM_DESCRIPTION.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.SCHOOL_NAME.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.SCHOOL.get(SampleMapper.UI) + " "
                        + SampleMapper.SCHOOL_NAME.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.STUDENT_NAME.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.STUDENT_NAME.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(
                    SampleMapper.TRANSACTION_SUBGOAL_ATTEMPT.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.TRANSACTION_SUBGOAL_ATTEMPT.get(SampleMapper.UI) + "</th>");

            }

            if (columns.contains(SampleMapper.CUSTOM_FIELD_NAME.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.CUSTOM_FIELD.get(SampleMapper.UI) + " "
                        + SampleMapper.CUSTOM_FIELD_NAME.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains(SampleMapper.CUSTOM_FIELD_VALUE.get(SampleMapper.DB))) {
                htmlBuffer.append("<th>"
                        + SampleMapper.CUSTOM_FIELD.get(SampleMapper.UI) + " "
                        + SampleMapper.CUSTOM_FIELD_VALUE.get(SampleMapper.UI) + "</th>");
            }

            if (columns.contains("transactionTypeTutor")) {
                htmlBuffer.append("<th>Tutor Response Type</th>");
            }
            if (columns.contains("transactionTypeTool")) {
                htmlBuffer.append("<th>Student Response Type</th>");
            }
            if (columns.contains("transactionSubtypeTutor")) {
                htmlBuffer.append("<th>Tutor Response Subtype</th>");
            }
            if (columns.contains("transactionSubtypeTool")) {
                htmlBuffer.append("<th>Student Response Subtype</th>");
            }

            htmlBuffer.append("</tr></thead>");
            htmlBuffer.append("<tbody>");

            List<Object[]> previewList = null;
            try {
                //TODO set the limit from either a default or the form.
                previewList = sampleDao.getTransactionsPreview(sample, PREVIEW_OFFSET_DEFAULT,
                        PREVIEW_LIMIT_DEFAULT);
                if (numTrans == null) { return sampleDao.determineError(sample); }
            } catch (Exception exception) {
                logger.warn("Sample Selector Helper caught the following exception :: "
                        + exception.getMessage(), exception);
                return ("ERROR|Unknown error getting data preview.");
            }

            if (previewList != null && !previewList.isEmpty()) {
                //walk through the preview list displaying all numbers
                int count = 1;
                for (Object[] objArray : previewList) {
                    if (objArray != null) {
                        htmlBuffer.append("<tr>");
                        //start at the 2nd entity.. the first is just the transaction id
                        //which we don't want.
                        for (int i = 1; i < objArray.length; i++) {
                            Object obj = objArray[i];
                            htmlBuffer.append("<td");
                            if (count % 2 == 0) {
                                htmlBuffer.append(" class=\"even\"");
                            }
                            htmlBuffer.append(">");
                            if (obj instanceof Date) {
                                htmlBuffer.append(displayObject(obj));
                            } else {
                                htmlBuffer.append(obj);
                            }
                            htmlBuffer.append("</td>");
                        }
                        htmlBuffer.append("</tr>");
                        count++;
                    } else {
                        logger.warn("Object array is unexpectedly null");
                    }
                }
            } else {
                htmlBuffer.append("<tr><td class=\"empty\">zero rows returned</td></tr>");
            }
            htmlBuffer.append("</tbody>");
            htmlBuffer.append("</table>");
            logDebug("Finished HTML string construction :", htmlBuffer);
            return htmlBuffer.toString();
        } else {
            return null;
        }
    }

    /**
     * Process the HTTP request to update the filter information in this sample
     * @param request the HttpServletRequest to process.
     * @param datasetContext information stored in the HTTP session via DatasetContext class.
     * @return a blank/new SampleItem with the filters attached.
     */
    private SampleItem processFilters(HttpServletRequest request, DatasetContext datasetContext) {
        logDebug("processFilters: Begin");

        //create a new sample since we don't want to actually overwrite
        //or save anything until the user OKs it.
        SampleItem sample = new SampleItem();

        int i = 0;
        String classString = request.getParameter(FILTER_CLASS_PARAM + i);
        String attribString;
        String stringString;
        String operatorString;
        String positionString;
        while (classString != null) {
            classString = request.getParameter(FILTER_CLASS_PARAM + i);
            attribString = request.getParameter(FILTER_ATTRIB_PARAM + i);
            stringString = request.getParameter(FILTER_STRING_PARAM + i);
            operatorString = request.getParameter(FILTER_OPERATOR_PARAM + i);
            positionString = request.getParameter(FILTER_POSITION_PARAM + i);

            if (classString != null && attribString != null) {
                logDebug("Filter found with following properties :: ", "class=", classString,
                        " attribute=", attribString, " filterString=", stringString,
                        " operator=", operatorString, " position=", positionString);

                FilterItem filter = new FilterItem();
                filter.setClazz(classString);
                filter.setAttribute(attribString);

                if (stringString != null) {
                    if (stringString.length() > FilterItem.FILTER_STRING_MAX_LENGTH) {
                        String msg = "Filter for " + classString + "." + attribString
                            + " is too long. Please remove one or more items and try saving again.";
                        throw new FilterStringMaxLengthExceededException(msg);
                    }
                    filter.setFilterString(stringString);
                }
                if (operatorString != null) {
                    filter.setOperator(operatorString);
                }
                if (positionString != null) {
                    filter.setPosition(new Integer(positionString));
                }
                sample.addFilter(filter);

                logDebug("FilterItem created is ", filter);
            }
            i++;
        }
        logDebug("processFilters: End");

        return sample;
    } // end processFilters()

    /**
     * Helper function to determine if the two lists of filters items are different
     * @param existingSample the existing sample
     * @param newSample the new sample
     * @return a boolean of true if the filter lists are different, false otherwise.
     */
    private boolean differentFilters(SampleItem existingSample, SampleItem newSample) {
        List newFilters = newSample.getFiltersExternal();
        List existingFilters = existingSample.getFiltersExternal();

        if (newFilters.size() != existingFilters.size()) { return true; }
        if (!newFilters.containsAll(existingFilters)
                || !existingFilters.containsAll(newFilters)) {
            return true;
        }

        return false;
    }

    /**
     * Helper method to create a quick copy of a sample.  This method will set the sampleName,
     * sampleID and filePath attributes in the copied object.
     * @param sampleToCopy the sample to copy
     * @return a new sample object
     */
    private SampleItem createCopy(SampleItem sampleToCopy) {
        SampleItem sample = new SampleItem();
        sample.setSampleName(sampleToCopy.getSampleName());
        sample.setId((Integer)sampleToCopy.getId());
        sample.setFilePath(sampleToCopy.getFilePath());
        return sample;
    }

    /**
     * When a sample is renamed we need to rename the corresponding cached transaction export file.
     * This involves finding the existing cached file, opening it up, copying and renaming
     * the .txt zip entry and creating a new cached file.  As of Java 6, we cannot delete zip
     * entries, so this is the workaround.
     * @param dataset the dataset for this sample.
     * @param existingSample the sample prior to rename.
     * @param newSample the renamed sample.
     * @param baseDir the base directory where cached exports are stored.
     */
    private void updateCachedFile(DatasetItem dataset, SampleItem existingSample,
            SampleItem newSample, String baseDir) {
        helper = HelperFactory.DEFAULT.getTransactionExportHelper();
        String cachedFileName = helper.getCachedFileName(existingSample, baseDir);
        if (cachedFileName != null) {
            try {
                CachedFileInfo info = new CachedFileInfo(dataset, newSample, baseDir);
                ZipFile zipFile = new ZipFile(cachedFileName);
                String justFileName = cachedFileName.substring(
                        cachedFileName.lastIndexOf(File.separator) + 1);
                String exportFileName = justFileName.replace("zip", "txt");
                ZipEntry zipFileEntry = zipFile.getEntry(exportFileName);
                if (zipFileEntry == null) {
                    logError(logger, "Was unable to find an existing zip file entry for "
                            + helper.formatForLogging(existingSample));
                    zipFile.close();
                    return;
                } else {
                    logDebug("Found existing zip file entry for ",
                            helper.formatForLogging(existingSample));
                    logInfo(logger, "Attempting to copy cached transaction export.");
                    boolean result = false;
                    String wholePath = helper.getDirectoryPath(info.getSample(), info.getBaseDir());
                    File newDirectory = new File(wholePath);
                    // Ensure the directory can be created before processing transactions.
                    if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                        FileUtils.makeWorldReadable(newDirectory);
                        // Set the permissions on the entire directory so we can
                        // access the file.
                        boolean ownerOnlyFlag = false;
                        newDirectory.setReadable(true, ownerOnlyFlag);
                        newDirectory.setWritable(true, ownerOnlyFlag);

                        File tempFile = info.getTempFile();

                        // Ensure the file can be created before processing
                        // transactions.
                        if (tempFile != null) {

                            PrintWriter tmp = new PrintWriter(tempFile);
                            CachedExportFileReader rdr = new CachedExportFileReader(cachedFileName);
                            List<String> row;
                            String newSampleName = newSample.getSampleName();

                            // 1. write headers
                            logDebug("printing headers ",
                                    join("\t", rdr.headers()));
                            tmp.println(join("\t", rdr.headers()));
                            // 2. for each row, replace second col with new
                            // sample name, first col is row #
                            while (!(row = rdr.nextRow()).isEmpty()) {
                                row.set(1, newSampleName);
                                tmp.println(join("\t", row));
                            }
                            zipFile.close();
                            tmp.close();

                            // let the transaction helper do the rest of the work for us.

                            helper.createCachedFile(info, tempFile);
                            File deleteMe = new File(zipFile.getName());
                            result = deleteMe.delete();

                        }
                    }


                    if (result) {
                        logInfo(logger, "Successfully removed the old cached export file for "
                                + helper.formatForLogging(existingSample));
                    } else {
                        logWarn(logger, "Unable to remove the old cached export file for "
                                + helper.formatForLogging(existingSample));
                    }
                    return;
                }
            } catch (IOException ioe) {
                logError("Unable to update existing cached file for "
                        + helper.formatForLogging(existingSample) + ioe.getMessage());
            }
        } else {
            logger.info("No cached export file to update.");
        }

    } // end updateCachedFileName()

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Helper method for INFO logging.
     * @param args what we wish to log.
     */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /**
     * Helper method for ERROR logging.
     * @param args what we wish to log.
     */
    private void logError(Object... args) {
        LogUtils.logErr(logger, args);
    }

    /**
     * Helper method for WARN logging.
     * @param args what we wish to log.
     */
    private void logWarn(Object... args) {
        LogUtils.logWarn(logger, args);
    }

    /**
     * Thrown from the processFilters method only.
     */
    private class FilterStringMaxLengthExceededException extends RuntimeException {
        /**
         * Constructor.
         * @param message the message to the user
         */
        public FilterStringMaxLengthExceededException(String message) {
            super(message);
        }
    }

} // end SampleSelectorHelper.java
