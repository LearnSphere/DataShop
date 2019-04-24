/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dto.StepRollupExportOptions;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.PageGridHelper;

/**
 * Helper class for exporting step rollup tables.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11729 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-22 21:34:28 -0500 (Sat, 22 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupExportServlet extends AbstractServlet {


    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        //no difference, so just forward the request and response to the post.
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);

            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            DatasetContext datasetContext = getDatasetContext(req);
            ExportContext exportContext = datasetContext.getExportContext();

            UserLogger.log(datasetContext.getDataset(),
                    datasetContext.getUser(), UserLogger.VIEW_STEP_ROLLLUP);

            String displaySkillsParam = req.getParameter("displaySkills");
            if (displaySkillsParam != null) {
                exportContext.setSRIDisplayKCs(Boolean.valueOf(displaySkillsParam));
            }

            synchronized (datasetContext.getExportContext()) {
                if (new StudentStepExportHandler(req, resp, datasetContext, getBaseDir())
                .processRequest("application/zip; charset=UTF-8", "zip", true)) {
                    return;
                }
            }

            if (isPageGridRequest(req)) {
                logger.debug("IS PageGrid request");
                StepRollupExportOptions options =
                    getStepRollupExportOptions(datasetContext, navHelper, true);

                handlePageGridRequest(req, resp, new StepRollupPageGridHelper(
                    datasetContext.getExportContext(), options));
                return;
            } else {
                logger.debug("IS NOT PageGrid request");
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
     * Gather up all the parameters and session setting that are required for a
     * Step Rollup-up export into one nice easy to use DTO.
     * @param datasetContext the current {@link DatasetContext}
     * @param navHelper the current {@link NavigationHelper}
     * @param useCachedFile whether to use the cached file, if available
     * @return {@link StepRollupExportOptions} current option settings.
     */
    private StepRollupExportOptions getStepRollupExportOptions(DatasetContext datasetContext,
            NavigationHelper navHelper, Boolean useCachedFile) {
        StepRollupExportOptions options = new StepRollupExportOptions();
        options.setSamples(navHelper.getSelectedSamples(datasetContext));
        options.setModel(DaoFactory.DEFAULT.getSkillModelDao().get(
                                    navHelper.getSelectedSkillModel(datasetContext)));

        Boolean displaySkills =
            datasetContext.getExportContext().getStudentStepDisplayKCs();
        if (displaySkills != null) { options.setDisplaySkills(displaySkills); }


        options.setSelectedSkills(navHelper.getSelectedSkills(datasetContext));
        options.setSelectedProblems(navHelper.getSelectedProblems(datasetContext));
        options.setSelectedStudents(navHelper.getSelectedStudents(datasetContext));
        options.setExportCachedVersion(useCachedFile);
        // Determine whether or not to get the rows from the server or cached file
        // for the current preview; this depends on user selected options
        if ((navHelper.getSelectedProblems(datasetContext).size()
                    != datasetContext.getNavContext().getProblemList().size())
            || (navHelper.getSelectedStudents(datasetContext).size()
                    != datasetContext.getNavContext().getStudentList().size())
            || (datasetContext.getExportContext().getStudentStepIncludeKCs()
                    && navHelper.getSelectedSkills(datasetContext).size()
                    != datasetContext.getNavContext().getSkillList().size())
        ) {
            options.setHasUserOptions(true);
        }

        if (datasetContext.getExportContext().getStudentStepIncludeKCs()) {
            options.setDisplaySkills(true);
            options.setDisplayAllModels(false);
            options.setModel(navHelper.getSelectedSkillModelItem(datasetContext));
        } else if (datasetContext.getExportContext().getStudentStepIncludeNoKCs()) {
            options.setDisplaySkills(false);
            options.setModel(null);
        } else {
            options.setDisplaySkills(false);
            options.setDisplayAllModels(true);
            options.setModel(null);
        }

        return options;
    }

    /**
     * Helper class to handle the page grid requests.
     */
    private class StepRollupPageGridHelper implements PageGridHelper {
        /** All the options for a given export. */
        private StepRollupExportOptions options;
        /** The StepRollupExportHelper. */
        private StepRollupExportHelper helper;
        /** ExportContext */
        private ExportContext exportContext;
        /** KC header size. */
        private static final int KC_HEADER_SIZE = 3;
        /** Rows in tables. */
        private long numRows = 0;
        /**
         * Default Constructor.
         * @param options the collection of StepRollupExportOptions
         * @param exportContext the export context
         */
        public StepRollupPageGridHelper(ExportContext exportContext,
                StepRollupExportOptions options) {
            this.helper = HelperFactory.DEFAULT.getStepRollupExportHelper();
            this.options = options;
            this.exportContext = exportContext;
        }

        /**
         * True if the user has selected at least one sample, false otherwise.
         * @return True if the user has selected at least one sample, false otherwise.
         */
        private boolean isValid() {
            return (options.getSamples() != null && options.getSamples().size() > 0
                && options.getSelectedStudents() != null && options.getSelectedStudents().size() > 0
                && options.getSelectedProblems() != null && options.getSelectedProblems().size() > 0
                );
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
         * Gets the set of student problem rows based on the current sample
         * along with the Offset and Limit
         * @param limit the number of rows to return.
         * @param offset the offset for the current Sample.
         * @return a List of student problem rows
         */
        public List pageGridItems(Integer limit, Integer offset) {
            options.setLimit(limit);
            options.setOffset(offset);
            List results = new ArrayList();
            if (!isValid()) {
                return results;
            }
            // walk through all the samples and figure out how many we have to
            // include to fill the page
            List<SampleItem> samplesThatRequireCaching = exportContext
                    .getSamplesThatRequireStudentStepCaching();

            boolean getCached = !options.hasUserOptions();
            Integer currentLimit = options.getLimit();
            Integer currentOffset = options.getOffset();

            for (SampleItem sample : options.getSamples()) {

                int sampleSize = helper.getExportResultsSize(sample, options);

                CachedExportFileReader reader = cachedFileReader(sample);
                // read from the cached file if it exists and samples do not
                // need to be cached.
                if (currentOffset < sampleSize) {
                    if (reader != null
                            && getCached
                            && (samplesThatRequireCaching == null || !samplesThatRequireCaching
                                    .contains(sample))) {
                        logInfo("Student-Step Export. Getting Sample: '",
                                sample.getNameAndId(),
                                "' from cached student step export.");
                        List<List<String>> sampleRows = reader.rows(
                                (int) currentLimit, currentOffset);

                        List<String> headers = reader.headers();
                        boolean hasRowCol = !headers.isEmpty()
                                && "Row".equals((String) headers.get(0));

                        // Get the columns based on UI options
                        SkillModelItem model = options.isDisplaySkills() ? options
                                .getModel() : null;

                        int currentIndex = 0, endIndex = 0, condIndex = 0;

                        for (String header : headers) {
                            if (header.matches("Condition")) {
                                condIndex = currentIndex;
                            } else if (model != null
                                    && header
                                            .matches("KC[\\s\\t]+\\("
                                                    + model.getSkillModelName()
                                                    + "\\)")) {
                                endIndex = currentIndex + KC_HEADER_SIZE;
                                break;
                            }
                            currentIndex++;
                        }

                        if (sampleRows == null || model != null && endIndex == 0) {
                            logger.info("Uncached model found. Not reading from cached export.");
                        } else if (sampleRows != null) {
                            int startIndex = currentIndex;
                            if (hasRowCol) {
                                for (int i = 0; i < sampleRows.size(); i++) {
                                    List<String> sampleRow = sampleRows.get(i);
                                    if (options.isDisplayAllModels()) {
                                        // Show all KC models columns
                                        sampleRows.set(
                                                i,
                                                sampleRow.subList(1,
                                                        sampleRow.size()));
                                    } else if (endIndex > 0) {
                                        // Show only KC models columns for a single model
                                        List mainColumns = sampleRow.subList(1,
                                                condIndex + 1);
                                        List kcColumns = sampleRow.subList(
                                            startIndex,
                                            // Predicted error rate can sometimes be an empty column
                                            // in which case, the index will be reduced by 1
                                            (endIndex > sampleRow.size()
                                                ? sampleRow.size() : endIndex)
                                        );
                                        List newRow = new ArrayList<String>();
                                        newRow.addAll(mainColumns);
                                        newRow.addAll(kcColumns);
                                        sampleRows.set(i, newRow);
                                    } else {
                                        // Show no KC models columns
                                        sampleRows
                                                .set(i, sampleRow.subList(1,
                                                        condIndex + 1));
                                    }
                                }
                            }
                            results.addAll(sampleRows);
                        }
                    } else {
                    // If the results are empty, then the cached file did not exist
                    // or it needs to be re-cached because of a new skill model.
                        logInfo("Student-Step Preview. Getting Sample: '",
                                sample.getNameAndId(),
                                "' from server student-step preview.");

                        List sampleResults = helper.getExportPreviewForSample(
                                sample, options, currentLimit, currentOffset);
                        logDebug("Student-Step Rollup: "
                                + sampleResults.size()
                                + " rows returned for sample "
                                + sample.getNameAndId());
                        results.addAll(sampleResults);
                     }
                    currentOffset = 0;
                } else {
                    currentOffset -= sampleSize;
                    currentOffset = (currentOffset < 0) ? 0 : currentOffset;
                }

                if (results.size() >= options.getLimit()) {
                    break;
                } else {
                    currentLimit = options.getLimit() - results.size();
                }
            }
            return results;
        }

        /**
         * Get the max
         * @return Long of the max results.
         */
        public Long max() {
            return new Long(helper.getExportResultsSize(options));
        }

        /**
         * Get the headers for the preview.
         * @return List of the headers.
         */
        public List headers() {
            return helper.getHeaders(options);
        }

        /**
         * Translates the item into an Object[] that can be used to create the JSON objects.
         * @param item The item to convert
         * @return Object[] of the items to display.
         */
        public Object[] translateItem(Object item) {
                List stepRollupInfo = (List)item;
                return stepRollupInfo.toArray();
        }

        /** {@inheritDoc} */
        public String validationMessage() { return options.validationMessage(); }
    }

    /**
     * Helper class for handling the student-step tables export calls and responses.
     */
    class StudentStepExportHandler extends AbstractExportHandler {
        /** Base directory where cached transaction export files belong. */
        private String baseDir;
        /** Whether to use the cached file, if available. */
        private Boolean useCachedFile = true;
        /**
         * Default Constructor.
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @param datasetContext {@link DatasetContext}
         * @param baseDir the files directory
         */
        public StudentStepExportHandler(HttpServletRequest req, HttpServletResponse resp,
                DatasetContext datasetContext, String baseDir) {
            super(req, resp, datasetContext, UserLogger.EXPORT_STEP_ROLLUP);

            ExportContext exportContext = datasetContext.getExportContext();
            useCachedFile = req.getParameter("use_cached_version") == null
                    ? true : Boolean.valueOf(req.getParameter("use_cached_version"));
            exportContext.setUseCachedVersion(useCachedFile);
            this.baseDir = baseDir;
        }

        /** {@inheritDoc} */
        public AbstractExportBean createExportBean() {
            DatasetContext datasetContext = getDatasetContext();
            NavigationHelper navHelper = HelperFactory.DEFAULT.getNavigationHelper();
            String userId = (String) navHelper.getUser(datasetContext).getId();

            StepRollupExportBean exportBean
                = HelperFactory.DEFAULT.getStepRollupExportBean();
            exportBean.setAttributes(
                    getStepRollupExportOptions(datasetContext, navHelper,
                        useCachedFile),
                    userId);
            exportBean.setBaseDir(baseDir);
            exportBean.setDataset(datasetContext.getDataset());
            return exportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getStepRollupExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setStepRollupExportBean((StepRollupExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String STUD_STEP_TYPE = "student_step";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return STUD_STEP_TYPE;
        }
    }
}
