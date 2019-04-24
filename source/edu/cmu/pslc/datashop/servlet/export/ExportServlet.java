/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dto.ExportCache;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.PageGridHelper;

/**
 * This servlet is for handling the creation of a export for exporting
 * data from the database.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11888 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-12-12 14:58:11 -0500 (Fri, 12 Dec 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExportServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The Servlet name. */
    public static final String SERVLET_NAME = "Export";

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/exporter.jsp";

    /** The JSP name for the content of this servlet. */
    public static final String JSP_CONTENT_NAME = "/export_content.jsp";

    /** HTML parameter. */
    public static final String VIEW_BY_TRANSACTION = "byTransaction";
    /** HTML parameter. */
    public static final String VIEW_BY_STUDENT_STEP = "byStudentStep";
    /** HTML parameter. */
    public static final String VIEW_BY_STUDENT_PROBLEM = "byProblem";
    /** HTML parameter. */
    public static final String TRANSACTION_EXPORT_REQUEST_PARAM = "tx_export";

    /** HTML parameter. */
    public static final String EXPORT_PREVIEW_PARAM = "exportPreviewUpdate";

    /** HTML parameter. */
    public static final String EXPORT_CONTEXT_AJAX_UPADTE = "exportPageUpdate";

    /** Constant. */
    public static final int SAMPLE_INFO_NUMBER = 4;

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String debugParams = getDebugParamsString(req);
        logDebug("doPost begin :: ", debugParams);
        try {
            setEncoding(req, resp);

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET_NAME);

            // set the most recent servlet name for the 'S2D' page
            req.getSession(true).setAttribute("recent_ds_page", SERVLET_NAME);

            final NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            DatasetContext datasetContext = getDatasetContext(req);

            UserItem userItem = getLoggedInUserItem(req);
            updateAccessFlag(datasetContext, userItem, "Export");

            ExportContext exportContext = datasetContext.getExportContext();
            String infoPrefix = getInfoPrefix(datasetContext);
            String benchmarkPrefix = getBenchmarkPrefix(datasetContext);
            logger.info(infoPrefix + " dataset " + datasetContext.getDataset().getId());

            // Set which KC model columns should be displayed via the export context
            setStudentProblemKCOptions(req, exportContext);
            setStudentStepKCOptions(req, exportContext);

            String subtab = req.getParameter("set_selected_subtab");
            String selection = new String();
            if (subtab != null) {
                selection = subtab;
                exportContext.setExportPageSubtab(subtab);
            } else {
                selection = exportContext.getExportPageSubtab();
                if (selection == null) {
                    selection = VIEW_BY_TRANSACTION;
                    exportContext.setExportPageSubtab(selection);
                }
            }

            String learningCurveRequest = req.getParameter("learning_curve_request");
            if (learningCurveRequest != null) {
                selection = learningCurveRequest;
            }


            if (req.getParameter("determine_content") != null || subtab != null) {
                if (exportContext != null) {
                    if (selection.equals(VIEW_BY_TRANSACTION)) {
                        exportContext.setSamplesThatRequireCaching(
                            getSamplesRequiresCaching(datasetContext,
                                SystemLogger.ACTION_CACHED_TX_EXPORT));
                    } else if (selection.equals(VIEW_BY_STUDENT_PROBLEM)) {
                        exportContext.setSamplesThatRequireStudentProblemCaching(
                            getSamplesRequiresCaching(datasetContext,
                                SystemLogger.ACTION_CACHED_PROBLEM_EXPORT));
                    } else if (selection.equals(VIEW_BY_STUDENT_STEP)) {
                        exportContext.setSamplesThatRequireStudentStepCaching(
                            getSamplesRequiresCaching(datasetContext,
                                SystemLogger.ACTION_CACHED_STEP_EXPORT));
                    }
                }
                logDebug("checking content and sample selection");
                JSONObject json = new JSONObject();
                List<SampleItem> sampleList = navHelper.getSelectedSamples(datasetContext);
                Map<SampleItem, String> cachedFileStatus =
                    navHelper.getCachedFileStatus(datasetContext, selection);

                json.put("noSamples", sampleList.isEmpty());

                if (selection != null && !selection.isEmpty()) {
                    json.put("subtab", selection);
                    // log which export the user chose to view and
                    // do some other stuff in 'VIEW_BY_TRANSACTION'.
                    if (selection.equals(VIEW_BY_TRANSACTION)
                            || selection.equals(VIEW_BY_STUDENT_PROBLEM)
                            || selection.equals(VIEW_BY_STUDENT_STEP)) {
                        // add values to JSON object, retrieve these values from exporter JS
                        ArrayList sampleInfoId = new ArrayList();
                        ArrayList sampleInfoName = new ArrayList();
                        ArrayList sampleInfoSkillModelsNotCached = new ArrayList();
                        ArrayList sampleInfoCachedFileStatus = new ArrayList();

                        String cachedExportAction = new String(
                            SystemLogger.ACTION_CACHED_TX_EXPORT);
                        if (selection.equals(VIEW_BY_STUDENT_STEP)) {
                            cachedExportAction = SystemLogger.ACTION_CACHED_STEP_EXPORT;
                        } else if (selection.equals(VIEW_BY_STUDENT_PROBLEM)) {
                            cachedExportAction = SystemLogger.ACTION_CACHED_PROBLEM_EXPORT;
                        }

                        for (SampleItem sample : sampleList) {
                            sampleInfoId.add(sample.getId());
                            sampleInfoName.add(sample.getSampleName());
                            sampleInfoSkillModelsNotCached.add(
                                getSkillModelsNotCached(datasetContext, sample,
                                    cachedExportAction));
                            sampleInfoCachedFileStatus.add(cachedFileStatus.get(sample));
                        }
                        // Get selected skill models to build a list
                        SkillModelItem selectedSkillModel = navHelper
                            .getSelectedSkillModelItem(datasetContext);

                        json.put("lstSampleId", sampleInfoId);
                        json.put("lstSampleName", sampleInfoName);
                        json.put("lstSkillModelsNotCached", sampleInfoSkillModelsNotCached);
                        json.put("lstCachedFileStatus", sampleInfoCachedFileStatus);
                        json.put("lstSelectedSkillModels", selectedSkillModel.getSkillModelName());
                    }

                    if (selection.equals(VIEW_BY_TRANSACTION)) {
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_TRANS_EXPORT);
                        // Get the samples that require caching for transaction export
                        json.put("lstSamplesThatRequireCaching",
                            getSampleIDs(exportContext.getSamplesThatRequireCaching()));

                    } else if (selection.equals(VIEW_BY_STUDENT_STEP)) {
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_STEP_ROLLLUP);
                        // Get the samples that require caching for student-step export
                        json.put("lstSamplesThatRequireCaching",
                            getSampleIDs(exportContext.getSamplesThatRequireStudentStepCaching()));
                    } else {
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_PROBLEM_ROLLUP);
                        // Get the samples that require caching for student-problem export
                        json.put("lstSamplesThatRequireCaching",
                            getSampleIDs(exportContext
                                .getSamplesThatRequireStudentProblemCaching()));
                    }
                }
                setInfo(req, datasetContext);
                writeJSON(resp, datasetContext, json);
                return;
            }

            if (isPageGridRequest(req)) {
                logDebug("IS PageGrid request");
                PageGridHelper pgHelper;

                logDebug("IS transaction export");
                exportContext.setExportPageSubtab("byTransaction");

                pgHelper = new TxnPageGridHelper(exportContext,
                        datasetContext.getNavContext().getSelectedSamples());
                UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                        UserLogger.VIEW_TRANS_EXPORT);

                handlePageGridRequest(req, resp, pgHelper);
                return;
            } else {
                logDebug("IS NOT PageGrid request");
            }

            if (handleExport(req, resp, navHelper, datasetContext)) { return; }

            if (!updateNavigationOptions(req, resp)) {
                // forward to the JSP (view)
                RequestDispatcher disp;
                String exportUpdateParam = req.getParameter(EXPORT_CONTEXT_AJAX_UPADTE);
                if (exportUpdateParam != null && exportUpdateParam.equals("true")) {
                    logger.info(infoPrefix + " accessing content JSP.");
                    disp = getServletContext().getRequestDispatcher(JSP_CONTENT_NAME);
                } else {
                    logger.info(benchmarkPrefix + " accessing base JSP.");
                    disp = getServletContext().getRequestDispatcher(JSP_NAME);
                }
                disp.forward(req, resp);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
           logDebug("doPost end :: ", debugParams);
        }
    }

    /**
     * Sets the KC options for the student-problem rollup.
     * @param req the HttpServletRequest
     * @param exportContext the ExportContext
     */
    private void setStudentProblemKCOptions(HttpServletRequest req, ExportContext exportContext) {
        String exportIncludeAllKCs = req.getParameter("exportIncludeAllKCs");
        if (exportIncludeAllKCs != null) {
            exportContext.setStudentProblemIncludeAllKCs(Boolean.valueOf(exportIncludeAllKCs));
        }
        String exportIncludeKCs = req.getParameter("exportIncludeKCs");
        if (exportIncludeKCs != null) {
            exportContext.setStudentProblemIncludeKCs(Boolean.valueOf(exportIncludeKCs));
        }
        String exportIncludeNoKCs = req.getParameter("exportIncludeNoKCs");
        if (exportIncludeNoKCs != null) {
            exportContext.setStudentProblemIncludeNoKCs(Boolean.valueOf(exportIncludeNoKCs));
        }
        if (exportIncludeAllKCs == null && exportIncludeKCs == null
                && exportIncludeNoKCs == null) {
            exportContext.setStudentProblemIncludeAllKCs(true);
        }
        String exportIncludeStepsWithoutKCs = req.getParameter("exportIncludeStepsWithoutKCs");
        if (exportIncludeStepsWithoutKCs != null) {
            exportContext.setStudentProblemIncludeUnmappedSteps(
                Boolean.valueOf(exportIncludeStepsWithoutKCs));
        }
    }

    /**
     * Sets the KC options for the student-step rollup.
     * @param req the HttpServletRequest
     * @param exportContext the ExportContext
     */
    private void setStudentStepKCOptions(HttpServletRequest req, ExportContext exportContext) {
        String exportStepIncludeAllKCs = req.getParameter("exportStepIncludeAllKCs");
        if (exportStepIncludeAllKCs != null) {
            exportContext.setStudentStepIncludeAllKCs(Boolean.valueOf(exportStepIncludeAllKCs));
        }
        String exportStepIncludeKCs = req.getParameter("exportStepIncludeKCs");
        if (exportStepIncludeKCs != null) {
            exportContext.setStudentStepIncludeKCs(Boolean.valueOf(exportStepIncludeKCs));
        }
        String exportStepIncludeNoKCs = req.getParameter("exportStepIncludeNoKCs");
        if (exportStepIncludeNoKCs != null) {
            exportContext.setStudentStepIncludeNoKCs(Boolean.valueOf(exportStepIncludeNoKCs));
        }
        if (exportStepIncludeAllKCs == null && exportStepIncludeKCs == null
                && exportStepIncludeNoKCs == null) {
            exportContext.setStudentStepIncludeAllKCs(true);
        }
    }

    /**
     * Helper function that handles all the export parameters/requests.
     * @param req the HttpServletRequest.
     * @param resp the HttpServletResponse.
     * @param navHelper the NavigationHelper instance.
     * @param datasetContext The DatasetContext
     * @return boolean indicating this has handled an export parameter of some kind.
     * @throws IOException thrown from writing to the response out stream.
     */
    private boolean handleExport(HttpServletRequest req, HttpServletResponse resp,
            NavigationHelper navHelper, DatasetContext datasetContext) throws IOException {

        boolean responseSent = false;

        //Synchronize the export context to make sure that the a new bean is finished
        //being created and saved to the session before the "check" comes in that
        //would otherwise fail.
        synchronized (datasetContext.getExportContext()) {
            if (req.getParameter(TRANSACTION_EXPORT_REQUEST_PARAM) != null) {
                logDebug("Processing transaction export request.");
                responseSent = new TransactionExportHandler(req, resp, datasetContext,
                        getBaseDir()).processRequest("application/zip; charset=UTF-8", "zip", true);
            }
            setInfo(req, datasetContext);
        }
        return responseSent;
    }

    /**
     * Returns a list of skillModels that have been added to the dataset but are not yet
     * cached.  This means they will show up in the tx_export preview but not in the actual
     * export itself.
     * @param context the dataset context
     * @param sample the sample item
     * @param selection the export type string (byStudentStep, byTransaction, or byProblem)
     * @return a list of SkillModels that have not been cached, or an empty list.
     */
    private ArrayList<String> getSkillModelsNotCached(
            DatasetContext context, SampleItem sample, String selection) {
        ArrayList<String> skillModelNameList = new ArrayList<String>();
        DatasetItem dataset = context.getDataset();
        List<SkillModelItem> itemList =
            DaoFactory.DEFAULT.getDatasetSystemLogDao()
                .getSkillModelsNotCached(dataset, sample, selection);

        for (SkillModelItem skillModel : itemList) {
            skillModelNameList.add(skillModel.getSkillModelName());
        }
        return skillModelNameList;
    }

    /**
     * Get a list of the IDs for a sample list.
     * @param sampleList the list of sample items
     * @return an ArrayList of Integers
     */
    private ArrayList<Integer> getSampleIDs(List<SampleItem> sampleList) {
        ArrayList<Integer> sampleIdList = new ArrayList<Integer>();
        for (SampleItem sample :  sampleList) {
            sampleIdList.add((Integer) sample.getId());
        }
        return sampleIdList;
    }

    /**
     * Get a list of the samples that require caching.
     * @param context the dataset context.
     * @param cachedExportAction the string which describes the export type
     * @return the sample list
     */
    private List<SampleItem> getSamplesRequiresCaching(DatasetContext context,
            String cachedExportAction) {
        DatasetItem dataset = context.getDataset();
        List<SampleItem> sampleList = null;
        if (cachedExportAction.equals(SystemLogger.ACTION_CACHED_STEP_EXPORT)) {
            sampleList = DaoFactory.DEFAULT.getDatasetSystemLogDao().getSamplesToCacheStep(dataset);
        } else if (cachedExportAction.equals(SystemLogger.ACTION_CACHED_PROBLEM_EXPORT)) {
            sampleList = DaoFactory.DEFAULT.getDatasetSystemLogDao()
                .getSamplesToCacheProblem(dataset);
        } else {
            sampleList = DaoFactory.DEFAULT.getDatasetSystemLogDao().getSamplesToCacheTx(dataset);
        }
        return sampleList;
    }

    /**
     * PageGridExportHelper, implementation of the PageGridHelper for the
     * transaction export.
     */
    class TxnPageGridHelper implements PageGridHelper {
        /** TransactionExportHelper. */
        private TransactionExportHelper helper;
        /** List of all selected samples. */
        private List<SampleItem> sampleList;
        /** ExportCache */
        private ExportCache cache;
        /** ExportContext */
        private ExportContext exportContext;

        /**
         * Default Constructor.
         * @param exportContext the session information about the export page
         * @param sampleList the list of all samples to work through.
         */
        public TxnPageGridHelper(ExportContext exportContext, List sampleList) {
            helper = HelperFactory.DEFAULT.getTransactionExportHelper();
            this.sampleList = sampleList;
            cache = new ExportCache();
            this.exportContext = exportContext;
        }

        /**
         * True if the user has selected at least one sample, false otherwise.
         * @return True if the user has selected at least one sample, false otherwise.
         */
        private boolean isValid() {
            return sampleList != null && sampleList.size() > 0;
        }

        /**
         * Create a CachedExportFileReader for the sample.
         * @param sample the sample
         * @return a CachedExportFileReader for the sample
         */
        private CachedExportFileReader cachedFileReader(SampleItem sample) {
            return helper.cachedFileReader(sample, getBaseDir());
        }

        /**
         * Gets the set of transactions based on the current sample along with the
         * Offset and Limit
         * @param limit the number of rows to return.
         * @param offset the offset for the currentSample.
         * @return a List of transactions
         */
        public List pageGridItems(Integer limit, Integer offset) {
            List<List<String>> results = new ArrayList();

            if (!isValid()) { return results; }

            SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
            int totalNum = 0;

            //walk through all the samples and figure out how many we have to
            //include in order to hit the limit
            int sampleOffset = offset;
            int tmpLimit = limit;

            cache = helper.initExportCache(sampleList.get(0), cache);

            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            boolean mergeHeaders = (sampleList.size() > 1
                    && !sampleList.contains(
                            sampleDao.findOrCreateDefaultSample(sampleList.get(0).getDataset())));
            List<SkillModelWithMaxSkills> maxSkillValues = null;

            if (mergeHeaders) {
                maxSkillValues = DaoFactory.DEFAULT.getSampleMetricDao()
                    .getMaxModelsWithSkills(sampleList, cache.getSkillModelNames());
            }

            List<SampleItem> samplesThatRequireCaching =
                exportContext.getSamplesThatRequireCaching();
            for (SampleItem sample : sampleList) {
                totalNum += metricDao.getTotalTransactions(sample);
                if (totalNum >= offset) {
                    CachedExportFileReader reader = cachedFileReader(sample);
                    // read from the cached file if it exists and samples do not need to be cached.
                    if (reader != null && !mergeHeaders
                            && samplesThatRequireCaching != null
                            && !samplesThatRequireCaching.contains(sample)) {
                        logDebug("Get sample ", sample.getId(), " from cached file");
                        List<List<String>> sampleRows = reader.rows(tmpLimit, sampleOffset);

                        //DS1027:  (Missing Row Column in TX and Step List export files)
                        List<String> headers = reader.headers();
                        boolean hasRowCol = !headers.isEmpty() && "Row".equals(headers.get(0));
                        logDebug("hasRowCol ", hasRowCol);

                        if (hasRowCol) {
                            for (int i = 0; i < sampleRows.size(); i++) {
                                List<String> sampleRow = sampleRows.get(i);
                                sampleRows.set(i, sampleRow.subList(1, sampleRow.size()));
                            }
                        }

                        results.addAll(sampleRows);
                        tmpLimit -= sampleRows.size();
                    } else {
                        logDebug("Get sample ", sample.getId(), " from server");
                     //   helper.getHeaderColumns(cache, sampleList, maxSkillValues);
                        List<TransactionItem> sampleResults =
                            helper.getBatch(sample, sampleOffset, tmpLimit);
                        //process all the samples right now while we have the sample.
                        for (TransactionItem txnObj : sampleResults) {
                            results.add(helper.getTransactionData(sample, txnObj, cache, false));
                            tmpLimit--;
                        }
                        sampleOffset = 0;
                    }
                } else {
                    sampleOffset = offset - totalNum;
                }
                if (totalNum > offset + limit) { break; }
            }

            return results;
        }

        /**
         * Gets the maximum/total number of transactions to page through.
         * @return Long of the total number of transactions
         */
        public Long max() {
            long totalTransaction = 0;
            SampleMetricDao metricDao = DaoFactory.DEFAULT.getSampleMetricDao();
            for (SampleItem sample : sampleList) {
                totalTransaction += metricDao.getTotalTransactions(sample);
            }
            return totalTransaction;
        }

        /**
         * Gets the headers for the given sample list.
         * @return List of the headers
         */
        public List headers() {
            if (!isValid()) { return emptyList(); }

            List<String> headers = null;

            boolean mergeHeaders = false;

            // We'll need to find the right subset of skill model headers if we have more
            // than one sample and the samples don't share the same skill models.  If one
            // of the selected samples is All Data we don't need to merge because we'll
            // include all headers by default.
            SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
            mergeHeaders = (sampleList.size() > 1
                            && !sampleList.contains(
                                    sampleDao.findOrCreateDefaultSample(
                                            sampleList.get(0).getDataset())));

            List<SkillModelWithMaxSkills> maxSkillValues = null;

            if (mergeHeaders) {
                maxSkillValues = DaoFactory.DEFAULT.getSampleMetricDao()
                    .getMaxModelsWithSkills(sampleList, cache.getSkillModelNames());
            }

            CachedExportFileReader reader = cachedFileReader(sampleList.get(0));

            List<SampleItem> samplesThatRequireCaching =
                exportContext.getSamplesThatRequireCaching();

            DatasetSystemLogDao datasetSystemLogDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();

            Boolean hasSampleOrSKMToCache = false;
            if (samplesThatRequireCaching != null) {
                for (SampleItem sample :  sampleList) {
                    if (samplesThatRequireCaching.contains(sample)) {
                        hasSampleOrSKMToCache = true;
                        break;
                    }

                    List<SkillModelItem> skillModelsToBeCached =
                        datasetSystemLogDao.getSkillModelsNotCached(sample.getDataset(), sample,
                            SystemLogger.ACTION_CACHED_TX_EXPORT);
                    if (skillModelsToBeCached != null && skillModelsToBeCached.size() > 0) {
                        hasSampleOrSKMToCache = true;
                        break;
                    }
                }
            }
            // read from the cached file if it exists and (DS1195) the sample does not need caching.
            if (reader != null  && !mergeHeaders && !hasSampleOrSKMToCache) {
                headers = reader.headers();
            } else {
                cache.clearTheCache();
                headers = helper.getHeaderColumns(cache, sampleList, maxSkillValues);
            }
            //DS1027:  (Add Row header if missing)
            if (!headers.isEmpty() && !headers.get(0).equals("Row")) { headers.add(0, "Row"); }
            return headers;
        }

        /**
         * Translates the item into an object array.
         * @param item the row/item to translate.
         * @return Object[] of the columns.
         */
        public Object[] translateItem(Object item) { return ((List)item).toArray(); }

        /** {@inheritDoc} */
        public String validationMessage() {
            if (sampleList != null && sampleList.size() == 0) {
                return "Select at least one sample to view transaction export data.";
            }
            return null;
        }
    }

    /**
     * Helper class for handling the transaction export calls and responses.
     */
    class TransactionExportHandler extends AbstractExportHandler {

        /** Base directory where cached transaction export files belong. */
        private String baseDir;

        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param datasetContext {@link DatasetContext}
         * @param baseDir base directory for file storage.
         */
        public TransactionExportHandler(HttpServletRequest req, HttpServletResponse resp,
                DatasetContext datasetContext, String baseDir) {
            super(req, resp, datasetContext, UserLogger.EXPORT_TRANSACTIONS);
            this.baseDir = baseDir;
        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {
            DatasetContext datasetContext = getDatasetContext();
            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            TxExportBean txExportBean = HelperFactory.DEFAULT.getTxExportBean();
            txExportBean.setAttributes(navHelper.getSelectedSamples(datasetContext),
                    datasetContext.getDataset(), baseDir, datasetContext.getUserId(),
                    getTxExportSpFilePath());
            txExportBean.setSendEmail(isSendmailActive());
            txExportBean.setEmailAddress(getEmailAddressDatashopHelp());
            return txExportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getTxnExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setTxnExportBean((TxExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String TX_TYPE = "tx";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return TX_TYPE;
        }
    }

}
