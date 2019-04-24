/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.customfield.CustomFieldContext;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoContext;
import edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportContext;
import edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext;
import edu.cmu.pslc.datashop.servlet.export.ExportContext;
import edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext;
import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext;
import edu.cmu.pslc.datashop.servlet.performanceprofiler.PerformanceProfilerContext;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListContext;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleToDatasetContext;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowContext;

/**
 * This class is for storing information about the current state a user is in
 * when using the DataShop system.  It will write this class to the HTTP session
 * so all stored values must be serializable.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12832 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-12-14 12:12:11 -0500 (Mon, 14 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetContext {

    /** Map key for the dataset info context information. */
    private static final String DATASET_INFO_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoContext";

    /** Map key for the navigation context information. */
    private static final String NAV_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.NavigationContext";

    /** Map key for the learning curve context information. */
    private static final String LEARNING_CURVE_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveContext";

    /** Map key for the performance profiler context information. */
    private static final String PERFORMANCE_PROFILER_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.performanceprofiler.PerformanceProfilerContext";

    /** Map key for the error report context information. */
    private static final String ERROR_REPORT_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.errorreport.ErrorReportContext";

    /** Map key for the export context information. */
    private static final String EXPORT_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.export.ExportContext";

    /** Map key for the knowledge component context information. */
    private static final String KC_MODEL_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.kcmodel.KCModelContext";

    /** Map key for the dataset info context information. */
    private static final String FILES_INFO_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.filesinfo.FilesInfoContext";

    /** Map key for the problem list context information. */
    private static final String PROBLEM_LIST_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListContext";

    /** Map key for the custom field context information. */
    private static final String CUSTOM_FIELD_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.customfield.CustomFieldContext";

    /** Map key for the sample to dataset context information. */
    private static final String SAMPLE_TO_DATASET_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.sampletodataset.SampleToDatasetContext";

    /** Map key for the sample to dataset context information. */
    private static final String WORKFLOW_CONTEXT_KEY =
        "edu.cmu.pslc.datashop.servlet.workflows.WorkflowContext";

    /** Map key for the current dataset. */
    private static final String DATASET =
        "edu.cmu.pslc.datashop.item.DatasetItem";

    /** Map key for the current user. */
    private static final String USER =
        "edu.cmu.pslc.datashop.item.UserItem";

    /** Map key for the list of multi-part file items. */
    private static final String FILE_UPLOAD_ITEMS_KEY =
        "org.apache.commons.fileupload.FileItem";

    /**
     * The most recent report viewed by this dataset, use for redirecting
     * back from the help page to the report that was last viewed.
     */
    private static final String RECENT_REPORT_KEY = "recent_report";

    /** Map key for whether the user has view authorization for this dataset. */
    private static final String AUTH_LEVEL_KEY = "authorization_level";

    /** key for the number of page grid rows to show. */
    private static final String PAGEGRID_ROWS_TO_DISPLAY = "pageGridRows";

    /** Default number of rows to display for the page grid. */
    private static final Integer DEFAULT_NUM_PAGEGRID_ROWS = new Integer(10);

    /** Map key for the number of transactions in the current dataset. */
    private static final String NUM_TRANSACTIONS_KEY = "numTransactions";

    /**
     * Servlet Session context stored in the map, this allows us to synchronize
     * the list to handle multiple thread requests.
     */
    private Map map;

    /**
     * Default Constructor.
     * @param dataset DatasetItem for the associated dataset.
     * @param user UserItem for the associated user.
     * @param authorizationLevel for the given dataset and user
     */
    public DatasetContext(DatasetItem dataset, UserItem user,
            String authorizationLevel) {
        map = Collections.synchronizedMap(new HashMap());
        setDataset(dataset);
        setUser(user);
        setAuthorizationLevel(authorizationLevel);
        setPagegridRows(DEFAULT_NUM_PAGEGRID_ROWS);
    }

    /**
     * Returns the currently selected user id.
     * @return UserItem - the current user as a UserItem.
     */
    public String getUserId() {
        UserItem user = (UserItem)map.get(USER);
        return (user != null) ? (String)user.getId() : null;
    }

    /**
     * Returns the currently selected user.
     * @return UserItem - the current user as a UserItem.
     */
    public UserItem getUser() {
        return (UserItem)map.get(USER);
    }

    /**
     * Set the currently logged in user.
     * @return UserItem - the current user as a UserItem.
     */
    public void setUser(UserItem user) {
        map.put(USER, user);
    }

    /**
     * Returns the dataset associated with this context.
     * @return DatasetItem of the dataset.
     */
    public DatasetItem getDataset() {
        return (DatasetItem)map.get(DATASET);
    }

    /**
     * Sets the dataset associated with this context.
     * @param datasetItem the dataset item to be associated with this context
     */
    public void setDataset(DatasetItem datasetItem) {
        map.put(DATASET, datasetItem);
    }

    /**
     * Gets the navigation items from the session map.
     * @return NavigationContext the currently selected navigation options for the dataset.
     */
    public NavigationContext getNavContext() {
        NavigationContext navContext = (NavigationContext)map.get(NAV_CONTEXT_KEY);
        if (navContext == null) {
            navContext = new NavigationContext();
            setNavContext(navContext);
        }
        return navContext;
    }

    /**
     * Sets the navigation options to save them.
     * @param navContext the current NavigationContext
     */
    public void setNavContext(NavigationContext navContext) {
        map.put(NAV_CONTEXT_KEY, navContext);
    }

    /**
     * Gets the learning options from the session map.
     * @return LearningCurveContext the currently selected learning options for the dataset.
     */
    public LearningCurveContext getLearningCurveContext() {
        LearningCurveContext context =
            (LearningCurveContext)map.get(LEARNING_CURVE_CONTEXT_KEY);
        if (context == null) {
            context = new LearningCurveContext();
            setLearningCurveContext(context);
        }
        return context;
    }

    /**
     * Sets the learning curve session options for a given dataset.
     * @param options the set of LearningCurveContext.
     */
    public void setLearningCurveContext(LearningCurveContext options) {
        map.put(LEARNING_CURVE_CONTEXT_KEY, options);
    }

    /**
     * Gets the learning options from the session map.
     * @return PerformanceProfilerContext the currently selected learning options for the dataset.
     */
    public PerformanceProfilerContext getPerformanceProfilerContext() {
        PerformanceProfilerContext context =
            (PerformanceProfilerContext)map.get(PERFORMANCE_PROFILER_CONTEXT_KEY);
        if (context == null) {
            context = new PerformanceProfilerContext();
            setPerformanceProfilerContext(context);
        }
        return context;
    }

    /**
     * Sets the learning curve session options for a given dataset.
     * @param options the set of PerformanceProfilerContext.
     */
    public void setPerformanceProfilerContext(PerformanceProfilerContext options) {
        map.put(PERFORMANCE_PROFILER_CONTEXT_KEY, options);
    }

    /**
     * Gets the learning options from the session map.
     * @return DatasetInfoContext the currently selected learning options for the dataset.
     */
    public DatasetInfoContext getDatasetInfoContext() {
        DatasetInfoContext context =
            (DatasetInfoContext)map.get(DATASET_INFO_CONTEXT_KEY);
        if (context == null) {
            context = new DatasetInfoContext();
            setDatasetInfoContext(context);
        }
        return context;
    }

    /**
     * Sets the dataset info session options for a given dataset.
     * @param options the set of DatasetInfoContext.
     */
    public void setDatasetInfoContext(DatasetInfoContext options) {
        map.put(DATASET_INFO_CONTEXT_KEY, options);
    }

    /**
     * Gets the export options from the session map.
     * @return ExportContext the currently selected learning options for the dataset.
     */
    public ExportContext getExportContext() {
        ExportContext context =
            (ExportContext)map.get(EXPORT_CONTEXT_KEY);
        if (context == null) {
            context = new ExportContext();
            setExportContext(context);
        }
        return context;
    }

    /**
     * Sets the export options for a given dataset.
     * @param options the set of ExportContext.
     */
    public void setExportContext(ExportContext options) {
        map.put(EXPORT_CONTEXT_KEY, options);
    }

    /**
     * Gets the knowledge component import/export options from the session map.
     * @return KCModelContext the currently knowledge component model context.
     */
    public KCModelContext getKCModelContext() {
        KCModelContext context =
            (KCModelContext)map.get(KC_MODEL_CONTEXT_KEY);
        if (context == null) {
            context = new KCModelContext();
            setKCModelContext(context);
        }
        return context;
    }

    /**
     * Sets the knowledge component model import/export options for a given dataset.
     * @param options the set of KCModelContext options.
     */
    public void setKCModelContext(KCModelContext options) {
        map.put(KC_MODEL_CONTEXT_KEY, options);
    }

    /**
     * Gets the error report options from the session map.
     * @return ErrorReportContext the currently selected error report options for the dataset.
     */
    public ErrorReportContext getErrorReportContext() {
        ErrorReportContext context =
            (ErrorReportContext)map.get(ERROR_REPORT_CONTEXT_KEY);
        if (context == null) {
            context = new ErrorReportContext();
            setErrorReportContext(context);
        }
        return context;
    }

    /**
     * Sets the error report options for a given dataset.
     * @param options the set of ErrorReportContext.
     */
    public void setErrorReportContext(ErrorReportContext options) {
        map.put(ERROR_REPORT_CONTEXT_KEY, options);
    }

    /**
     * Get the the 'files info' options from the session map.
     * @return FilesInfoContext the files info context
     */
    public FilesInfoContext getFilesInfoContext() {
        FilesInfoContext context =
            (FilesInfoContext)map.get(FILES_INFO_CONTEXT_KEY);
        if (context == null) {
            context = new FilesInfoContext();
            setFilesInfoContext(context);
        }
        return context;
    }

    /**
     * Set the 'files info' options for a user.
     * @param options the set of FilesInfoContext options.
     */
    public void setFilesInfoContext(FilesInfoContext options) {
        map.put(FILES_INFO_CONTEXT_KEY, options);
    }

    /**
     * Get the the 'problem list' options from the session map.
     * @return ProblemListContext the problem list context
     */
    public ProblemListContext getProblemListContext() {
        ProblemListContext context =
            (ProblemListContext)map.get(PROBLEM_LIST_CONTEXT_KEY);
        if (context == null) {
            context = new ProblemListContext();
            setProblemListContext(context);
        }
        return context;
    }

    /**
     * Set the 'problem list' options for a user.
     * @param context the ProblemListContext object
     */
    public void setProblemListContext(ProblemListContext context) {
        map.put(PROBLEM_LIST_CONTEXT_KEY, context);
    }

    /**
     * Get the the custom field context from the session map.
     * @return CustomFieldContext the custom field context
     */
    public CustomFieldContext getCustomFieldContext() {
        CustomFieldContext context =
                (CustomFieldContext)map.get(CUSTOM_FIELD_CONTEXT_KEY);
        if (context == null) {
            context = new CustomFieldContext();
            setCustomFieldContext(context);
        }
        return context;
    }

    /**
     * Sets the custom field context for this dataset.
     * @param context {@link CustomFieldContext}
     */
    public void setCustomFieldContext(CustomFieldContext context) {
        map.put(CUSTOM_FIELD_CONTEXT_KEY, context);
    }

    /**
     * Gets the learning options from the session map.
     * @return RecentReport the currently selected learning options for the dataset.
     */
    public String getRecentReport() {
        return (String)map.get(RECENT_REPORT_KEY);
    }

    /**
     * Sets the dataset info session options for a given dataset.
     * @param recentReport name of the most recent report as a String.
     */
    public void setRecentReport(String recentReport) {
        map.put(RECENT_REPORT_KEY, recentReport);
    }

    /**
     * Saves the multi-part file upload {@link FileItem} that resulted from having
     * to possible parse the request object before we were ready to use them.
     * @param fileItems List of {@link FileItem} objects
     */
    public void setUploadItems(List <FileItem> fileItems) {
        map.put(FILE_UPLOAD_ITEMS_KEY, fileItems);
    }

    /**
     * Gets the list of multi-part file items.
     * @return a List of {@link FileItem} objects.
     */
    public List <FileItem> getUploadItems() {
        return (List <FileItem>)map.get(FILE_UPLOAD_ITEMS_KEY);
    }

    /**
     * Set the number of rows to show in the page grid.
     * @param rows the number of rows to display.
     */
    public void setPagegridRows(final Integer rows) {
        map.put(PAGEGRID_ROWS_TO_DISPLAY, rows);
    }

    /**
     * Get the number of rows to display in the page grid.
     * @return the number of rows to display.
     */
    public Integer getPagegridRows() {
        return (Integer)map.get(PAGEGRID_ROWS_TO_DISPLAY);
    }

    /** Gets the authorization level. @return authorization level */
    public String getAuthorizationLevel() {
        return (String)map.get(AUTH_LEVEL_KEY);
    }
    /** Sets the authorization level. @param level level */
    public void setAuthorizationLevel(String level) {
        map.put(AUTH_LEVEL_KEY, level);
    }

    /** Gets the viewFlag. @return true if user has view */
    public Boolean getViewFlag() {
        Boolean viewFlag = false;
        String level = getAuthorizationLevel();
        if (level != null
         && (level.equals(AuthorizationItem.LEVEL_VIEW)
          || level.equals(AuthorizationItem.LEVEL_EDIT)
          || level.equals(AuthorizationItem.LEVEL_ADMIN))) {
            viewFlag = true;
        }
        return viewFlag;
    }

    /** Gets the editFlag. @return true if user has edit */
    public Boolean getEditFlag() {
        Boolean editFlag = false;
        String level = getAuthorizationLevel();
        if (level != null
         && (level.equals(AuthorizationItem.LEVEL_EDIT)
          || level.equals(AuthorizationItem.LEVEL_ADMIN))) {
            editFlag = true;
        }
        return editFlag;
    }

    /** Gets the adminFlag. @return true if user has admin */
    public Boolean getAdminFlag() {
        Boolean adminFlag = false;
        String level = getAuthorizationLevel();
        if (level != null
         && (level.equals(AuthorizationItem.LEVEL_ADMIN))) {
            adminFlag = true;
        }
        return adminFlag;
    }

    /** Gets the user's admin flag. @return true if datashop admin */
    public Boolean isDataShopAdmin() {
        Boolean dataShopAdminFlag = false;
        UserItem userItem = getUser();
        if (userItem != null && userItem.getAdminFlag()) {
            dataShopAdminFlag = true;
        }
        return dataShopAdminFlag;
    }

    public Long getNumTransactions() {
        Long result =
            (map.get(NUM_TRANSACTIONS_KEY) == null) ? 0 : (Long)map.get(NUM_TRANSACTIONS_KEY);
        return result;
    }

    public void setNumTransactions(Long value) {
        map.put(NUM_TRANSACTIONS_KEY, value);
    }

    /**
     * Get the the 'sample to dataset' options from the session map.
     * @return SampleToDatasetContext the sample to dataset context
     */
    public SampleToDatasetContext getSampleToDatasetContext() {
        SampleToDatasetContext context =
            (SampleToDatasetContext)map.get(SAMPLE_TO_DATASET_CONTEXT_KEY);
        if (context == null) {
            context = new SampleToDatasetContext();
            setSampleToDatasetContext(context);
        }
        return context;
    }

    /**
     * Set the 'sample to dataset' options for a user.
     * @param options the set of SampleToDatasetContext options.
     */
    public void setSampleToDatasetContext(SampleToDatasetContext options) {
        map.put(SAMPLE_TO_DATASET_CONTEXT_KEY, options);
    }

    /**
     * Get the the 'workflow' options from the session map.
     * @return WorkflowContext the workflow context
     */
    public WorkflowContext getWorkflowContext() {
        WorkflowContext context =
            (WorkflowContext)map.get(WORKFLOW_CONTEXT_KEY);
        if (context == null) {
            context = new WorkflowContext();
            setWorkflowContext(context);
        }
        return context;
    }

    /**
     * Set the 'workflow' options for a user.
     * @param options the set of WorkflowContext options.
     */
    public void setWorkflowContext(WorkflowContext options) {
        map.put(WORKFLOW_CONTEXT_KEY, options);
    }
}
