/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dao.StudentProblemRollupDao;
import edu.cmu.pslc.datashop.dto.StudentProblemRollupOptions;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.DatasetContext;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.NavigationHelper;
import edu.cmu.pslc.datashop.servlet.PageGridHelper;

/**
 * This servlet handles the user options and export requests for
 * the student-problem rollup.
 *
 * @author Mike Komisin
 * @version $Revision: 11729 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-22 21:34:28 -0500 (Sat, 22 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentProblemExportServlet extends AbstractServlet {


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

            UserLogger.log(datasetContext.getDataset(),
                    datasetContext.getUser(), UserLogger.VIEW_PROBLEM_ROLLUP);
            ExportContext exportContext = datasetContext.getExportContext();

            synchronized (exportContext) {
                if (new StudentProblemExportHandler(req, resp, datasetContext, getBaseDir())
                    .processRequest("application/zip; charset=UTF-8", "zip", true)) {
                    setInfo(req, datasetContext);
                    return;
                }
            }

            if (isPageGridRequest(req)) {
                logger.debug("IS PageGrid request");
                StudentProblemRollupOptions options =
                    getStudentProblemRollupOptions(datasetContext, navHelper,
                            true);

                logDebug("IS student-problem export");
                exportContext.setExportPageSubtab("byProblem");

                handlePageGridRequest(req, resp, new StudentProblemPageGridHelper(
                    exportContext, options));
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
     * Set the parameters for building the student problem tables and
     * store them in a DTO.
     * @param datasetContext the current {@link DatasetContext}
     * @param navHelper the current {@link NavigationHelper}
     * @param useCachedFile whether to use the cached file, if available
     * @return {@link StudentProblemRollupOptions} current option settings.
     */
    private StudentProblemRollupOptions getStudentProblemRollupOptions(
        DatasetContext datasetContext,
            NavigationHelper navHelper, Boolean useCachedFile) {

        StudentProblemRollupOptions options = new StudentProblemRollupOptions();
        options.setSamples(datasetContext.getNavContext().getSelectedSamples());
        options.setSkills(navHelper.getSelectedSkills(datasetContext));
        options.setProblems(navHelper.getSelectedProblems(datasetContext));
        options.setStudents(navHelper.getSelectedStudents(datasetContext));
        options.setExportCachedVersion(useCachedFile);
        // Determine whether or not to get the rows from the server or cached file
        // for the current preview; this depends on user selected options
        if ((navHelper.getSelectedProblems(datasetContext).size()
                    != datasetContext.getNavContext().getProblemList().size())
            || (navHelper.getSelectedStudents(datasetContext).size()
                    != datasetContext.getNavContext().getStudentList().size())
            || (datasetContext.getExportContext().getStudentProblemIncludeKCs()
                    && navHelper.getSelectedSkills(datasetContext).size()
                    != datasetContext.getNavContext().getSkillList().size())
        ) {
            options.setHasUserOptions(true);
        }

        Boolean displayStepsWithoutKCs =
            datasetContext.getExportContext().getStudentProblemIncludeUnmappedSteps();
        if (displayStepsWithoutKCs != null) {
            options.setIncludeUnmappedSteps(displayStepsWithoutKCs);
        }

        if (datasetContext.getExportContext().getStudentProblemIncludeKCs()) {
            options.setDisplaySkills(true);
            options.setDisplayAllModels(false);
            options.setModel(navHelper.getSelectedSkillModelItem(datasetContext));
        } else if (datasetContext.getExportContext().getStudentProblemIncludeNoKCs()) {
            options.setDisplaySkills(false);
            options.setDisplayAllModels(false);
            options.setModel(null);
        } else {
            options.setDisplaySkills(false);
            options.setDisplayAllModels(true);
            options.setModel(null);
        }

        return options;
    }


    /**
     * Helper class for handling the student problem tables export calls and responses.
     */
    class StudentProblemExportHandler extends AbstractExportHandler {
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
        public StudentProblemExportHandler(HttpServletRequest req, HttpServletResponse resp,
                DatasetContext datasetContext, String baseDir) {
            super(req, resp, datasetContext, UserLogger.EXPORT_PROBLEM_STUDENT);

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

            StudentProblemExportBean exportBean
                = HelperFactory.DEFAULT.getStudentProblemExportBean();
            exportBean.setAttributes(
                getStudentProblemRollupOptions(datasetContext, navHelper,
                        useCachedFile),
                    userId);
            exportBean.setBaseDir(baseDir);
            exportBean.setDataset(datasetContext.getDataset());
            return exportBean;
        }

        /** {@inheritDoc} */
        public AbstractExportBean getExportBean() {
            return getExportContext().getStudentProblemExportBean();
        }

        /** {@inheritDoc} */
        public void setExportBean(AbstractExportBean bean) {
            getExportContext().setStudentProblemExportBean((StudentProblemExportBean)bean);
        }

        /** The type of export file to be used in the actual file name. */
        private static final String STUD_PROB_TYPE = "student_problem";

        /**
         * Get the string to include for the type of export in the export file name.
         * @param bean the bean
         * @return a string of the type in the file name
         */
        public String getExportFileNameType(AbstractExportBean bean) {
            return STUD_PROB_TYPE;
        }
    }

    /**
     * PageGridExportHelper, implementation of the PageGridHelper for the
     * student problem export.
     */
    class StudentProblemPageGridHelper implements PageGridHelper {
        /** StudentProblemExportHelper. */
        private StudentProblemExportHelper helper;
        /** The student problem export/preview options. */
        private StudentProblemRollupOptions options;
        /** List of all selected samples. */
        private List<SampleItem> sampleList;
        /** ExportContext */
        private ExportContext exportContext;
        /** KC header size. */
        private static final int KC_HEADER_SIZE = 3;
        /** Rows in tables. */
        private long numRows = 0;
        /**
         * Default Constructor.
         * @param exportContext the session information about the export page
         * @param options the StudentProblemRollupOptions
         */
        public StudentProblemPageGridHelper(ExportContext exportContext,
                StudentProblemRollupOptions options) {
            helper = HelperFactory.DEFAULT.getStudentProblemExportHelper();
            this.options = options;
            this.exportContext = exportContext;
            this.sampleList = new ArrayList<SampleItem>();
            if (options.getSamples() != null) {
                sampleList.addAll(options.getSamples());
            }

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

            if (validationMessage() != null) {
                return results;
            }

            int currentRow = 0;
            // walk through all the samples and figure out how many we have to
            // include to fill the page
            StudentProblemRollupDao sprDao = DaoFactory.DEFAULT
                    .getStudentProblemRollupDao();
            List<SampleItem> samplesThatRequireCaching = exportContext
                    .getSamplesThatRequireStudentProblemCaching();

            boolean getCached = !options.hasUserOptions();
            getCached = getCached && (!options.isDisplaySkills()
                || options.isIncludeUnmappedSteps());
            Map<String, Integer> minAndMaxStudentIds = null;
            if (options.hasUserOptions()) {
                logDebug("Getting min and max student ids for based on user options.");
                minAndMaxStudentIds = sprDao.getMinAndMaxStudentIds(options);
            }
            for (SampleItem sample : sampleList) {
                long totalNumRowsThisSample = sprDao.numberOfStudentProblems(
                        sample, options);

                if (currentRow + totalNumRowsThisSample < offset) {
                    currentRow += totalNumRowsThisSample;
                } else if (currentRow >= offset + limit) {
                    currentRow += totalNumRowsThisSample;
                } else {
                    CachedExportFileReader reader = cachedFileReader(sample);
                    // get the rows from this sample that are g.t.e. the offset
                    // or l.t.e. the limit
                    int innerOffset = offset > currentRow ? offset - currentRow
                            : 0;
                    long innerLimit = totalNumRowsThisSample - innerOffset;
                    if (currentRow + totalNumRowsThisSample >= offset + limit) {
                        innerLimit = offset + limit - currentRow - innerOffset;
                    }
                    logTrace("Page grid variables:: "
                            + "Offset: " + offset + ", " + "Limit: " + limit + ", "
                            + "Inner offset: " + innerOffset + ", "
                            + "Inner limit: " + innerLimit + ", "
                            + "Rows seen: " + currentRow + ", "
                            + "Total rows (this sample): " + totalNumRowsThisSample);

                    // read from the cached file if it exists and samples do not
                    // need to be cached.
                    if (reader != null
                            && getCached
                            && (samplesThatRequireCaching == null || !samplesThatRequireCaching
                                    .contains(sample))) {
                        logInfo("Student-Problem Export. Getting Sample: '",
                                sample.getNameAndId(),
                                "' from cached student problem export.");
                        List<List<String>> sampleRows = reader.rows(
                                (int) innerLimit, innerOffset);

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
                                            .matches("KCs[\\s\\t]+\\("
                                                    + model.getSkillModelName()
                                                    + "\\)")) {
                                endIndex = currentIndex + KC_HEADER_SIZE;
                                break;
                            }
                            currentIndex++;
                        }

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
                                    // Show only KC models columns for this
                                    // model
                                    List mainColumns = sampleRow.subList(1,
                                            condIndex + 1);
                                    List kcColumns = sampleRow.subList(
                                            startIndex, endIndex);
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
                    } else {
                        logInfo("Student-Problem Preview. Getting Sample: '",
                                sample.getNameAndId(),
                                "' from server student problem preview.");
                        // The user hasn't selected any options, but the sample
                        // is not cached
                        // So generate a preview that isolates the query to the
                        // min and max
                        // student_ids that will show in the preview (work now
                        // to save time later)
                        if (minAndMaxStudentIds == null) {
                            minAndMaxStudentIds = sprDao
                                    .getMinAndMaxStudentIds(options);
                        }
                        int minStudentId = minAndMaxStudentIds.get("min");
                        int maxStudentId = minAndMaxStudentIds.get("max");

                        Integer offsetByStudentId = minStudentId;
                        int sampleRowsBeforeMinStudentId = 0;
                        if (innerOffset > 0) {
                            // Get the derived offset based on the number
                            // of rows we're skipping by using the student id
                            // range
                            // in the where clause for the preview
                            offsetByStudentId = sprDao.getMinStudentId(
                                    (Integer) sample.getId(), options);

                            sampleRowsBeforeMinStudentId = sprDao
                                    .countToMinStudentId(sample, options,
                                            offsetByStudentId);
                            innerOffset = innerOffset
                                    - sampleRowsBeforeMinStudentId;
                        }
                        List sampleResults = helper.getExportPreviewForSample(
                                sample, options,
                                (int) innerLimit, innerOffset,
                                offsetByStudentId, maxStudentId);
                        logDebug("Student-Problem Rollup: "
                                + sampleResults.size()
                                + " rows returned for sample "
                                + sample.getNameAndId());
                        results.addAll(sampleResults);
                    }
                    currentRow += totalNumRowsThisSample;
                }

            }
            numRows = currentRow;



            return results;
        }

        /**
         * Gets the maximum/total number of student problem rows to page through.
         * @return Long of the total number of student problem rows
         */
        public Long max() {
            return numRows;
        }

        /**
         * Gets the headers for the given sample list.
         * @return List of the headers
         */
        public List headers() {
            if (validationMessage() != null) {
                return emptyList();
            }

            List<String> headers = null;

            CachedExportFileReader reader = cachedFileReader(sampleList.get(0));

            List<SampleItem> samplesThatRequireCaching =
                exportContext.getSamplesThatRequireStudentProblemCaching();

            DatasetSystemLogDao datasetSystemLogDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
            boolean isSampleCached = true;
            if (samplesThatRequireCaching != null) {
                for (SampleItem sample :  sampleList) {
                    if (samplesThatRequireCaching.contains(sample)) {
                        isSampleCached = false;
                        break;
                    }

                    List<SkillModelItem> skillModelsToBeCached =
                        datasetSystemLogDao.getSkillModelsNotCached(sample.getDataset(), sample,
                                SystemLogger.ACTION_CACHED_PROBLEM_EXPORT);
                    if (skillModelsToBeCached != null && skillModelsToBeCached.size() > 0) {
                        isSampleCached = false;
                        break;
                    }
                }
            }
            // Read from the cached file if it exists and the sample does not need caching.
            // The UI options determine whether or not to read from a cached file and
            // whether or not to show all kc model headers from the cached file.
            boolean buildHeaders = (exportContext.getStudentProblemIncludeKCs()
                    && !exportContext.getStudentProblemIncludeUnmappedSteps())
                        || !isSampleCached;

            if (reader == null || buildHeaders) {
                //  Otherwise, generate them from the options
                headers = new ArrayList(helper.getHeaders(options));

            } else {
                // show kc model headers for all models
                headers = reader.headers();
                List<String> newHeaders = null;

                if (exportContext.getStudentProblemIncludeKCs()) {
                    // filter kc model headers for selected model
                    newHeaders = filterKCHeaders(options.getModel(), headers);
                } else if (exportContext.getStudentProblemIncludeNoKCs()) {
                    // show no KC model headers
                    newHeaders = filterKCHeaders(headers);
                }
                if (newHeaders != null) {
                    headers = newHeaders;
                }
            }

            return headers;
        }

        /**
         * Filter KC column headers based on model name.
         * @param model the model
         * @param headers the headers
         * @return the headers
         */
        private List<String> filterKCHeaders(SkillModelItem model, final List<String> headers) {
            List<String> newHeaders = new ArrayList<String>();
            for (final String header : headers) {

                if ((!header.matches("KCs[\\s\\t]+\\(.*\\)")
                        || header.matches("KCs[\\s\\t]+\\(" + model.getSkillModelName() + "\\)"))

                    && (!header.matches("Steps without KCs[\\s\\t]+\\(.*\\)")
                            || header.matches("Steps without KCs[\\s\\t]+\\("
                                    + model.getSkillModelName() + "\\)"))

                    && (!header.matches("KC List[\\s\\t]+\\(.*\\)")
                        || header.matches("KC List[\\s\\t]+\\("
                            + model.getSkillModelName() + "\\)"))) {

                        newHeaders.add(header);
                }
            }
            return newHeaders;
        }

        /**
         * Filters all KC column headers.
         * @param headers the headers
         * @return the headers
         */
        private List<String> filterKCHeaders(final List<String> headers) {
            List<String> newHeaders = new ArrayList<String>();
            String lastHeader = null;
            for (final String header : headers) {

                if (lastHeader != null && lastHeader.matches("Condition")) {
                    break;
                }
                newHeaders.add(header);

                lastHeader = header;
            }

            return newHeaders;
        }

        /**
         * Translates the item into an object array.
         * @param item the row/item to translate.
         * @return Object[] of the columns.
         */
        public Object[] translateItem(Object item) { return ((List)item).toArray(); }

        /** {@inheritDoc} */
        public String validationMessage() {
            StudentProblemRollupDao sprDao = DaoFactory.DEFAULT.getStudentProblemRollupDao();
            Long numSelectedRows = (long) sprDao.numberOfStudentProblems(options);
            boolean problemsWithoutSkillsExist = (numSelectedRows > 0);

            if (sampleList != null && sampleList.size() == 0) {
                return "Select at least one sample to view student-problem export data.";
            }
            if (options.isDisplaySkills()
                    && options.getSkills() != null && options.getSkills().size() == 0
                    && !options.isIncludeUnmappedSteps()
                    && problemsWithoutSkillsExist /* there are problems without skills*/) {
                return "Select at least one KC"
                    + " or Include Steps without KCs to view student-problem export data.";
            } else if (options.isDisplaySkills()
                    && options.getSkills() != null && options.getSkills().size() == 0
                    && !problemsWithoutSkillsExist /*no problems without skills*/) {
                return "Select at least one KC to view student-problem export data.";
            }
            if (options.getStudents() != null && options.getStudents().size() == 0) {
                return "Select at least one student to view student-problem export data.";
            }
            if (options.getProblems() != null && options.getProblems().size() == 0) {
                return "Select at least one problem to view student-problem export data.";
            }

            return null;
        }
    }

}
