/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.dto.StepInfo;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.PageGridHelper;
import edu.cmu.pslc.datashop.servlet.SampleSelectorHelper;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldContext;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldDto;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldHelper;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldServlet;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentContext;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentDto;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentServlet;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListDto;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListHelper;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListServlet;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleToDatasetContext;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SamplesHelper;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportBean;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportHandler;
import edu.cmu.pslc.datashop.servlet.export.StepExportBean;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext;

/**
 * This servlet is for handling the Dataset Info report.
 * There are two major session parameters expected.  One for the type of request
 * and the other for the type of content if content is requested.
 * The Request can be:
 * <ul>
 * <li>null (which implies the base JSP page)</li>
 * <li>content</li>
 * <li>step list (shown in a live grid)</li>
 * <li>result size (for the live grid, where the step list is shown)</li>
 * </ul>
 * The Content can be:
 * <ul>
 * <li>default (which is 'overview')</li>
 * <li>overview</li>
 * <li>files and papers</li>
 * </ul>
 *
 * @author Alida Skogsholm
 * @version $Revision: 13756 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2017-01-19 13:50:45 -0500 (Thu, 19 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoReportServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** The max number of transactions allowed for a sample before the S2D feature disallows it. */
    public static final long S2D_MAX_TXS = 250000;

    /** The Servlet name. */
    public static final String SERVLET = "DatasetInfo";
    /** The JSP name for the base content. */
    public static final String BASE_JSP_NAME = "/dataset_info.jsp";
    /** The JSP name for the overview sub content. */
    private static final String OVERVIEW_JSP_NAME = "/dataset_info_overview.jsp";
    /** The JSP name for the problem list sub content. */
    private static final String PROBLEM_LIST_JSP_NAME = "/problem_list.jsp";
    /** The JSP name for the step list sub content. */
    private static final String STEP_LIST_JSP_NAME = "/step_list.jsp";
    /** The JSP name for the KC models managements sub content. */
    private static final String KC_MODELS_JSP_NAME = "/kc_models.jsp";
    /** The JSP name for the custom fields management sub content. */
    private static final String CUSTOM_FIELDS_JSP_NAME = "/custom_fields.jsp";
    /** The JSP name for the citation sub content. */
    private static final String CITATION_JSP_NAME = "/dataset_info_citation.jsp";
    /** The JSP name for the terms sub content. */
    private static final String TERMS_JSP_NAME = "/dataset_info_terms.jsp";
    /** The JSP name for the problem content sub content. */
    private static final String PROBLEM_CONTENT_JSP_NAME = "/problem_content.jsp";
    /** The JSP name for managing all samples. */
    private static final String SAMPLE_TO_DATASET_JSP_NAME = "/samples.jsp";
    /** The JSP name for the terms no permission. */
    private static final String NO_PERMISSION_NAME = "/dataset_info_no_permission.jsp";

    /** Session Attribute. */
    public static final String DATASET_INFO_REPORT_ATTRIB = "datasetInfoReport";

    /** Session Parameter. */
    private static final String DS_REQUEST_PARAM = "ds_request";
    /** Possible value for session parameter. */
    private static final String DS_REQUEST_CONTENT_VALUE = "content";

    /** Session Parameter. */
    private static final String DS_CONTENT_PARAM = "ds_content";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_INITIALIZE_VALUE = "initialize";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_OVERVIEW_VALUE = "overview";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_PROBLEM_LIST_VALUE = "problemList";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_STEP_LIST_VALUE = "stepList";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_KC_MODELS_VALUE = "models";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_CUSTOM_FIELDS_VALUE = "customFields";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_CITATION_VALUE = "citation";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_TERMS_VALUE = "terms";
    /** Possible value for session parameter. */
    private static final String DS_CONTENT_PROBLEM_CONTENT_VALUE = "problemContent";
    /** The Sort-by context attribute handle. */
    public static final String SORT_BY = "sortBy";
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
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        DatasetContext datasetContext = null;
        try {
            setEncoding(req, resp);

            /*
             * This call to isAuthorizedForDataset is necessary because
             * this servlet is not in the access filter map. Users cannot
             * access this servlet directly.
             */
            boolean isAuthorizedForDataset = isAuthorizedForDataset(req);
            UserItem userItem = getLoggedInUserItem(req);
            String requestingMethod = req.getParameter("requestingMethod");

            DatasetInfoContext infoContext = null;

            // set the most recent servlet name for the help page
            setRecentReport(req.getSession(true), SERVLET);

            // If (and only if) this is a request for the 'Dataset Info' page,
            // set the most recent servlet name for the 'S2D' page.
            if (req.getParameterMap().size() == 1) {
                req.getSession(true).setAttribute("recent_ds_page", SERVLET);
            }

            if (isAuthorizedForDataset) {
                datasetContext = getDatasetContext(req);
                infoContext = datasetContext.getDatasetInfoContext();
                if (datasetContext.getDataset() != null) {
                logger.info(getInfoPrefix(datasetContext) + " dataset "
                        + datasetContext.getDataset().getId());
                }
                // Check first if this a grid request.
                if (isPageGridRequest(req)) {
                    handlePageGridRequest(req, resp, datasetContext);
                    return;
                }

                Boolean requestSampleToDatasetProgress = false;
                if (requestingMethod != null && requestingMethod.matches("ProgressBar.*")) {
                    if (req.getParameter("s2dSelector") != null) {
                        requestSampleToDatasetProgress = true;
                    }
                }
               // Sample to dataset handling for aggregation (if sample not agged)
               // and progress bar for agg on Samples page.
               if (req.getParameter("s2dSelector") != null
                   || (requestingMethod != null && requestingMethod.matches("sampleObject.*"))
                   || (requestingMethod != null && requestingMethod.matches("Samples.*"))
                   || (requestSampleToDatasetProgress)) {
                   // Handle ajax / progress bar requests for aggregating and saving.
                   if (handleSampleToDatasetRequests(datasetContext, userItem,
                       requestingMethod, req, resp)) {
                       return;
                   }
               } else {
                   // If it's an export request, create a new Step export handler.
                   if (new StepExportHandler(req, resp, datasetContext).processRequest("txt")) {
                       setInfo(req, datasetContext);
                       return;
                   }
               }
            }

            //If the request for the KC Models page comes from the learning curve tab,
            //then don't change the content type for the dataset info tab as that
            //behavior is not expected.
            boolean changeContentType = true;
            if (requestingMethod != null
                    && requestingMethod.equals("LearningCurve.requestKCModels")) {
                changeContentType = false;
            }

            logDebug("changeContentType:" + changeContentType);

            // If not a step list request, find out what kind of request it is.
            String dsRequest = req.getParameter(DS_REQUEST_PARAM);
            String subTab = (String)req.getParameter("subtab");

            if (subTab != null && changeContentType && infoContext != null) {
                infoContext.setContentType(subTab);
                logDebug("subtab parameter: setting contentType to: ", subTab);
            }

            // If the request is null, then its a request of the base JSP.
            if (dsRequest == null) {
                // (DS1427) Benchmarking only if the datasetContext is valid
                if (datasetContext != null) {
                    logger.info(getBenchmarkPrefix(datasetContext) + " accessing base JSP.");
                }

                RequestDispatcher disp;
                disp = getServletContext().getRequestDispatcher(BASE_JSP_NAME);
                disp.forward(req, resp);
                return;
            }

            // (DS1427) This is redundant, as it is called by AbstractServlet.getDatasetContext
            //if (datasetContext != null) { setInfo(req, datasetContext); }

            // AJAX request for the content
            if (dsRequest.equals(DS_REQUEST_CONTENT_VALUE)) {

                String contentType;
                if (changeContentType) {
                    contentType = getContentType(req, infoContext);
                } else {
                    contentType = DS_CONTENT_KC_MODELS_VALUE;
                }

                /* (DS1427) Check to allow only the overview and term of use content (AJAX)
                 */
                if (!isAuthorizedForDataset && !(contentType.equals(DS_CONTENT_OVERVIEW_VALUE)
                        || contentType.equals(DS_CONTENT_TERMS_VALUE))) {
                    logger.debug("Ajax request: Forwarding to the no_permission JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            NO_PERMISSION_NAME);
                    disp.forward(req, resp);
                    return;
                }

                if (contentType.equals(DS_CONTENT_OVERVIEW_VALUE)) {
                    // forward to the JSP (view)
                    logger.debug("Ajax request: Forwarding to the overview sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            OVERVIEW_JSP_NAME);

                    DatasetItem datasetItem = null;


                    if (userItem != null) {
                        logger.info(getBenchmarkPrefix(getClass().getSimpleName(),
                                (String)userItem.getId()) + " getting overview.");

                        // Get datasetId from URL...
                        NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                        datasetItem = navHelper.getDataset(
                                getIntegerId(req.getParameter("datasetId")));

                        UserLogger.log(datasetItem, userItem, UserLogger.VIEW_DATASET_INFO);
                    }

                    req.setAttribute("userItem", userItem);
                    req.setAttribute("datasetItem", datasetItem);
                    disp.forward(req, resp);

                } else if (contentType.equals(DS_CONTENT_KC_MODELS_VALUE)) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    KCModelContext kcmContext = datasetContext.getKCModelContext();

                    // Set the default KC Model Sort Options
                    String kcmSortBy = req.getParameter("kcmSortBy");
                    if (kcmSortBy != null && kcmSortBy.compareTo("") != 0) {
                        logger.debug("Setting kcmSortBy in session: " + kcmSortBy);
                        kcmContext.setSortBy(kcmSortBy);
                    }
                    String kcmSortAscendingParam = req.getParameter("kcmSortAscending");
                    if (kcmSortAscendingParam != null && kcmSortAscendingParam.compareTo("") != 0) {
                        Boolean kcmSortAscending = new Boolean(kcmSortAscendingParam);
                        logger.debug("Setting kcmSortAscending in session: " + kcmSortAscending);
                        kcmContext.setSortByAscendingFlag(kcmSortAscending);
                    }

                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    navHelper.sortKcModels(datasetContext,
                                           kcmContext.getSortBy(),
                                           kcmContext.getSortByAscendingFlag());

                    setInfo(req, datasetContext);

                    logger.debug("Ajax request: Forwarding to the kc models sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            KC_MODELS_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_KC_MODEL_INFO);
                    disp.forward(req, resp);
                } else if (contentType.equals(DS_CONTENT_CUSTOM_FIELDS_VALUE)) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    Integer datasetId = getIntegerId(req.getParameter("datasetId"));
                    CustomFieldHelper cfHelper = HelperFactory.DEFAULT.getCustomFieldHelper();
                    List<CustomFieldDto> cfList =
                        cfHelper.getAllCustomFields(datasetId, CustomFieldContext.getContext(req));
                    Boolean displayEdit = cfHelper.getDisplayEditColumn(cfList, userItem);
                    req.setAttribute(CustomFieldServlet.CF_DISPLAY_EDIT_ATTR + datasetId,
                                     displayEdit);
                    req.setAttribute(CustomFieldServlet.CF_LIST_ATTR + datasetId, cfList);

                    logger.debug("Ajax request: Forwarding to the custom fields sub content JSP");
                    RequestDispatcher disp =
                        getServletContext().getRequestDispatcher(CUSTOM_FIELDS_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                   UserLogger.VIEW_CUSTOM_FIELDS);
                    disp.forward(req, resp);
                } else if (contentType.equals(DS_CONTENT_PROBLEM_LIST_VALUE)) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    Integer datasetId = getIntegerId(req.getParameter("datasetId"));
                    ProblemListHelper plHelper = HelperFactory.DEFAULT.getProblemListHelper();
                    ProblemListDto problemListDto =
                        plHelper.getProblemListDto(datasetId,
                                                   datasetContext.getProblemListContext());
                    req.setAttribute(ProblemListServlet.PROBLEM_LIST_ATTR + datasetId,
                                     problemListDto);

                    logger.debug("Ajax request: Forwarding to the problem list sub content JSP");
                    RequestDispatcher disp =
                        getServletContext().getRequestDispatcher(PROBLEM_LIST_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                   UserLogger.VIEW_PROBLEM_LIST);
                    disp.forward(req, resp);

                } else if (contentType.equals(DS_CONTENT_STEP_LIST_VALUE)) {
                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    logger.debug(
                            "Ajax request: Forwarding to the step list sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            STEP_LIST_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_STEP_LIST);
                    disp.forward(req, resp);
                } else if (contentType.equals(DS_CONTENT_CITATION_VALUE)) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    logger.debug("Ajax request: Forwarding to the citation sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            CITATION_JSP_NAME);
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                            UserLogger.VIEW_CITATION);
                    disp.forward(req, resp);
                } else if (contentType.equals(DS_CONTENT_TERMS_VALUE)) {
                    logger.debug("Ajax request: Forwarding to the terms sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                            TERMS_JSP_NAME);

                    if (datasetContext != null) {
                        UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                UserLogger.VIEW_TERMS);
                    }
                    disp.forward(req, resp);
                } else if (contentType.equals(DS_CONTENT_PROBLEM_CONTENT_VALUE)) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    Integer datasetId = getIntegerId(req.getParameter("datasetId"));
                    ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
                    ProblemContentDto problemContentDto =
                        pcHelper.getProblemContentDto(datasetId,
                                                      ProblemContentContext.getContext(req));
                    req.setAttribute(ProblemContentServlet.PROBLEM_CONTENT_ATTR + datasetId,
                                     problemContentDto);

                    logger.debug("Ajax request: Forwarding to the problem content sub content JSP");
                    RequestDispatcher disp =
                        getServletContext().getRequestDispatcher(PROBLEM_CONTENT_JSP_NAME);
                    String info = "Viewed Problem Content mapping tool";
                    UserLogger.log(datasetContext.getDataset(), datasetContext.getUser(),
                                   UserLogger.VIEW_PROBLEM_CONTENT_TOOL, info);
                    disp.forward(req, resp);
                } else if (contentType.equals("samples")
                        && req.getParameter("datasetId") != null
                        && req.getParameter("datasetId").matches("\\d+")) {

                    updateAccessFlag(datasetContext, userItem, "DatasetInfo");

                    SampleToDatasetContext sampleToDatasetContext
                        = datasetContext.getSampleToDatasetContext();
                    // forward to the JSP (view)
                    logger.debug("Ajax request: Forwarding to the samples sub content JSP");
                    RequestDispatcher disp = getServletContext().getRequestDispatcher(
                        SAMPLE_TO_DATASET_JSP_NAME);

                    DatasetItem datasetItem = null;

                    NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
                    datasetItem = navHelper.getDataset(
                        getIntegerId(req.getParameter("datasetId")));

                    if (userItem != null && datasetItem != null) {
                        // Get samples page.
                        logger.info(getBenchmarkPrefix(getClass().getSimpleName(),
                                (String)userItem.getId()) + " getting samples page "
                                + " for dataset (" + datasetItem.getId() + ")");
                        UserLogger.log(datasetItem, userItem, UserLogger.VIEW_SAMPLES_PAGE);
                        Long datasetId = new Long((Integer) datasetItem.getId());

                        // Get SamplesHelper.
                        SamplesHelper samplesHelper = HelperFactory.DEFAULT.getSamplesHelper();

                        // Set the most recent servlet name for the help page.
                        setRecentReport(req.getSession(true), SERVLET);

                        if (req.getParameter("sortBy") != null) {
                            String sortBy = (String) (req.getParameter("sortBy"));
                            sampleToDatasetContext.setSortBy(sortBy);
                            sampleToDatasetContext.toggleSortOrder(sortBy);
                        }

                        // Get the sample row info from the SamplesHelper.
                        List<SampleRowDto> sampleRows = samplesHelper.getSampleRowInfo(
                                userItem, datasetId);

                        req.getSession().setAttribute("sortBy", sampleToDatasetContext.getSortBy());
                        req.getSession().setAttribute("sampleRows", sampleRows);
                        req.getSession().setAttribute("datasetId", (Integer)datasetItem.getId());

                        disp.forward(req, resp);
                    }

                } else {
                    logger.warn("Invalid dataset info content type specified: " + contentType);
                }
            } else {
                logger.warn("Invalid dataset info request type specified: " + dsRequest);
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }

    /**
     * Handle ajax requests or progress bar requests for the Samples page.
     * @param userItem the user item
     * @param requestingMethod the requesting method string
     * @param req the http servlet request
     * @param resp the http servlet response
     * @return whether or not an ajax response has been generated
     * @throws JSONException a json exception
     * @throws IOException an io exception
     */
    private Boolean handleSampleToDatasetRequests(DatasetContext datasetContext,
            UserItem userItem, String requestingMethod,
            HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException {

        UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleMetricDao smDao = DaoFactory.DEFAULT.getSampleMetricDao();
        DatasetItem datasetItem = datasetContext.getDataset();
        SampleItem sampleItem = null;

        Integer sampleId = null;
        Long numTxs = null;

        if (req.getParameter("sampleId") != null
                && req.getParameter("sampleId").matches("\\d+")) {
            sampleId = Integer.parseInt(req.getParameter("sampleId"));
            sampleItem = sampleDao.get(sampleId);
            if (sampleItem != null) {
                numTxs = smDao.getTotalTransactions(sampleItem);
            }
        }

        // Test DS edit role upon request for saving the sample.
        if (requestingMethod.equals("Samples.s2dSaveSample")
                && !userRoleDao.hasDatashopEditRole(userItem)
                && !userItem.getAdminFlag()) {
            writeJSON(resp, json("requestDatashopEditRole", "true"));
            return true;
        // Test DS edit role upon (javascript) request for aggregating the sample.
        } else if (requestingMethod.equals("Samples.s2dSaveSample")
                && (numTxs == null || numTxs <= 0)) {
            writeJSON(resp,
                json("status", "error",
                "message", "This dataset does not contain any transactions."));
            return true;
        // Test DS edit role upon (javascript) request for aggregating the sample.
        } else if (requestingMethod.equals("Samples.s2dSaveSample")
                && (numTxs != null && numTxs >= S2D_MAX_TXS)) {
            UserLogger.log(datasetItem, userItem, UserLogger.SAMPLE_EXCEEDS_MAX_TXS);
            logger.warn("Sample (" + sampleId + ") has " + numTxs
                + " transactions. User cannot save to dataset (" + userItem.getId() + ")");
            writeJSON(resp,
                json("status", "error",
                "message", "The sample size allowed by this feature has been restricted to "
                    + S2D_MAX_TXS + " transactions."));
            return true;
        // Test DS edit role upon (javascript) request for aggregating the sample.
        } else if (requestingMethod.equals("sampleSelector.aggSample")
                && !userRoleDao.hasDatashopEditRole(userItem)
                && !userItem.getAdminFlag()) {
            writeAJAXUpdate(resp, "requestDatashopEditRole");
            return true;

        // If they do have the DS edit role or they are admin, then prompt
        // the user "are you sure you want to ..." , if necessary.
        } else if (requestingMethod.equals("Samples.s2dSaveSample")
                && (userRoleDao.hasDatashopEditRole(userItem)
                    || userItem.getAdminFlag())) {
            // Get a list of datasets created from this sample
            if (sampleId != null) {


                if (datasetItem != null
                        && datasetItem.getReleasedFlag() == null || !datasetItem.getReleasedFlag()) {
                    logDebug("Dataset not released.");
                    writeJSON(resp,
                            json("status", "error",
                                "message", "Cannot save dataset. Please release the dataset to the project."));
                    return true;
                } else if (datasetItem != null && datasetItem.getProject() == null) {
                    logDebug("Cannot save dataset. Dataset does not belong to a project.");
                    writeJSON(resp,
                            json("status", "error",
                                "message", "The dataset has not been released. Please release the dataset to the project."));
                    return true;
                }

                ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
                List<DatasetItem> previouslyCreatedDatasets =
                    iqDao.findDatasetsFromSample(sampleId);
                StringBuffer sBuffer = new StringBuffer();
                for (DatasetItem dsItem : previouslyCreatedDatasets) {
                    sBuffer.append("<a class=\"s2dDatasetLinks\" href=\"DatasetInfo?datasetId="
                            + dsItem.getId() + "\" target=\"_blank\" >"
                            + dsItem.getDatasetName() + "</a><br/>");
                }

                writeJSON(resp,
                    json("sampleId", sampleId,
                        "sampleName", req.getParameter("sampleName"),
                        "previouslyCreatedDatasets", sBuffer.toString()));
            } else {
                writeJSON(resp,
                    json("warn", "Sample Id is null"));
            }
            return true;
        }

        // Made it past the checkpoints so do the sample action
        // (e.g., save, edit, delete, ...)
        String ajaxUpdate = samplesSelectorAjaxUpdate(req);

        setInfo(req, datasetContext);

        if (ajaxUpdate != null) {
            // Return the response in a form the ProgressBar can handle.
            logDebug("writeAJAXUpdate");
            writeAJAXUpdate(resp, ajaxUpdate);
            return true;
        }
        // End of sample to dataset options
        return false;

    }

    /**
     * Check whether the request is an update for the SampleSelector helper.
     * @param req the request
     * @return the AJAX update string, if any, null otherwise
     */
    private String samplesSelectorAjaxUpdate(HttpServletRequest req) {
        DatasetContext datasetContext = getDatasetContext(req);
        SampleSelectorHelper sampleSelectorHelper =
            HelperFactory.DEFAULT.getSampleSelectorHelper();
        String ajaxUpdate = sampleSelectorHelper.update(req, datasetContext, getBaseDir(),
                    getAggSpFilePath());

        return ajaxUpdate;
    }


    /**
     * Handles the page grid request.
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @param datasetContext the dataset context
     * @throws Exception general exception
     */
    private void handlePageGridRequest(HttpServletRequest req,
            HttpServletResponse resp, DatasetContext datasetContext)
            throws Exception {
        logger.debug("Getting Step List information.");

        Long numSteps = null;
        String getMaxParam = req.getParameter("getMax"); //form parameter
        if (Boolean.valueOf(getMaxParam)) {
            SampleItem sample = DaoFactory.DEFAULT.getSampleDao()
                .findOrCreateDefaultSample(datasetContext.getDataset());
            SampleMetricDao sampleMetric = DaoFactory.DEFAULT.getSampleMetricDao();
            numSteps = sampleMetric.getTotalUniqueSteps(sample);
        }

        final DatasetContext sessInfo = datasetContext;
        final Long fNumSteps = numSteps;
        handlePageGridRequest(req, resp, new PageGridHelper() {
            public List pageGridItems(Integer limit, Integer offset) {
                DatasetInfoReportHelper datasetInfoHelper =
                    HelperFactory.DEFAULT.getDatasetInfoReportHelper();
                return datasetInfoHelper.getStepSummary(
                        sessInfo.getDataset(), limit, offset);
            }
            public Long max() { return fNumSteps; }
            public List headers() {
                return asList("Row", "Problem Hierarchy", "Problem Name", "Step Name");
            }
            public Object[] translateItem(Object item) {
                StepInfo stepInfo = (StepInfo)item;

                return new Object[] {
                        stepInfo.getProblemHierarchy(),
                        stepInfo.getProblemName(), stepInfo.getStepName()
                };
            }

            public String validationMessage() { return null; }
        });
        return;
    }

    /**
     * Retrieve the content type for the Dataset Info Report from the session parameter.
     * @param req the HTTP servlet request
     * @param context the current {@link DatasetInfoContext}
     * @return a string representation of the content type requested
     */
    private String getContentType(HttpServletRequest req, DatasetInfoContext context) {
        String contentType = req.getParameter(DS_CONTENT_PARAM);


        if (contentType != null) {
            if (contentType.compareTo(DS_CONTENT_INITIALIZE_VALUE) == 0) {
                /* (DS1427) If context is null, just set to overview,
                 * there is no session to update */
                if (context == null) {
                    contentType = DS_CONTENT_OVERVIEW_VALUE;
                } else {
                    contentType = context.getContentType();
                }
                if (contentType == null) {
                    contentType = DS_CONTENT_OVERVIEW_VALUE;
                    context.setContentType(contentType);
                    logDebug("defaulting contentType to: ", contentType);
                } else {
                    logDebug("contentType: ", contentType);
                }
            } else {
                if (context != null) {
                    context.setContentType(contentType);
                }
                logDebug("setting contentType to: ", contentType);
            }
        }
        return contentType;
    }

    /**
     * Helper class for handling the export calls and responses.
     */
    class StepExportHandler extends AbstractExportHandler {

        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param datasetContext {@link DatasetContext}
         */
        public StepExportHandler(HttpServletRequest req, HttpServletResponse resp,
                DatasetContext datasetContext) {
            super(req, resp, datasetContext, UserLogger.EXPORT_STEP_LIST);
        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {
            DatasetContext datasetContext = getDatasetContext();
            StepExportBean exportBean = HelperFactory.DEFAULT.getStepExportBean();
            exportBean.setAttributes(datasetContext.getDataset());
            return exportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getStepExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setStepExportBean((StepExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String STEP_LIST_TYPE = "step_list";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return STEP_LIST_TYPE;
        }
    }

}
