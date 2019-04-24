/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserLogItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Static class that allows User logs to be written.
 * @author Benjamin Billings
 * @version $Revision: 15454 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-08-31 13:00:06 -0400 (Fri, 31 Aug 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserLogger {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(UserLogger.class);

    /** Log in. */
    public static final String LOGIN = "Login";
    /** Log out. */
    public static final String LOGOUT = "Logout";
    /** Common action "View Project Datasets". */
    public static final String VIEW_PROJECT_DATASETS = "View Project Datasets";
    /** Common action "View Project Permissions". */
    public static final String VIEW_PROJECT_PERMISSIONS = "View Project Permissions";
    /** Common action "View Project IRB". */
    public static final String VIEW_PROJECT_IRB = "View Project IRB";
    /** Common action "View Project Terms". */
    public static final String VIEW_PROJECT_TERMS = "View Project Terms";
    /** Common action "View IRB Review". */
    public static final String VIEW_IRB_REVIEW = "View IRB Review";
    /** Common action "View All IRBs". */
    public static final String VIEW_ALL_IRBS = "View All IRBs";
    /** Common actions "select dataset". */
    public static final String SELECT_DATASET = "Select Dataset";
    /** Common action "view dataset". */
    public static final String VIEW_DATASET_INFO = "View Dataset Info";
    /** Common action "view papers". */
    public static final String VIEW_PAPERS = "View Papers";
    /** Common action "view files". */
    public static final String VIEW_FILES = "View Files";
    /** Common action "View External Analyses". */
    public static final String VIEW_EXTERNAL_ANALYSES = "View External Analyses";
    /** Common action "view samples page". */
    public static final String VIEW_SAMPLES_PAGE = "View Samples Page";
    /** Common action "view sample to dataset page". */
    public static final String VIEW_SAMPLE_TO_DATASET_PAGE = "View SampleToDataset Page";
    /** Common action "create sample". */
    public static final String SAMPLE_CREATE = "create sample";
    /** Common action "modify sample". */
    public static final String SAMPLE_MODIFY = "modify sample";
    /** Common action "delete sample". */
    public static final String SAMPLE_DELETE = "delete sample";

    /** Common action "transaction export". */
    public static final String EXPORT_TRANSACTIONS = "transaction export";
    /** Common action "step rollup export". */
    public static final String EXPORT_STEP_ROLLUP = "step rollup export";
    /** Common action "step list export". */
    public static final String EXPORT_STEP_LIST = "Export Step List";
    /** Common action "problem-student export". */
    public static final String EXPORT_PROBLEM_STUDENT = "problem-student table export";
    /** Common action "KC model export". */
    public static final String EXPORT_KC_MODEL = "kc model export";

    /** Common action "LFA export". */
    public static final String EXPORT_LFA = "LFA export";
    /** Common action "modify dataset info". */
    public static final String DATASET_INFO_MODIFY = "modify dataset info";
    /** Common action "modify project info". */
    public static final String PROJECT_INFO_MODIFY = "modify project info";

    /** Common action "modify file". */
    public static final String FILE_MODIFY = "modify file";
    /** Common action "add file". */
    public static final String FILE_ADD = "add file";
    /** Common action "delete file". */
    public static final String FILE_DELETE = "delete file";
    /** Common action "download file". */
    public static final String FILE_DOWNLOAD = "download file";

    /** Common action "modify paper". */
    public static final String PAPER_MODIFY = "modify paper";
    /** Common action "add paper". */
    public static final String PAPER_ADD = "add paper";
    /** Common action "delete paper". */
    public static final String PAPER_DELETE = "delete paper";

    /** Common action "edit externalAnalysis". */
    public static final String EXTERNAL_ANALYSIS_MODIFY = "modify externalAnalysis";
    /** Common action "add externalAnalysis". */
    public static final String EXTERNAL_ANALYSIS_ADD = "add externalAnalysis";
    /** Common action "delete externalAnalysis". */
    public static final String EXTERNAL_ANALYSIS_DELETE = "delete externalAnalysis";

    /** Common action "view access requests". */
    public static final String VIEW_MANAGE_ACCESS_REQUESTS = "View Access Requests";
    /** Common action "view error report by KC". */
    public static final String VIEW_ER_BY_KC = "View Error Report By KC";
    /** Common action "view error report by Problem". */
    public static final String VIEW_ER_BY_PROBLEM = "View Error Report By Problem";

    /** Common action "view learning curve by KC". */
    public static final String VIEW_LC_BY_KC = "View Learning Curve by KC";
    /** Common action "view learning curve by student". */
    public static final String VIEW_LC_BY_STUDENT = "View Learning Curve by Student";
    /** Common action "view LFA values". */
    public static final String VIEW_LFA_VALUES = "View LFA values";

    /** Common action "view performance profiler". */
    public static final String VIEW_PERFORMANCE_PROFILER = "View Performance Profiler";

    /** Common action "view step rollup". */
    public static final String VIEW_STEP_ROLLLUP = "View Step Rollup";
    /** Common action "view problem rollup". */
    public static final String VIEW_PROBLEM_ROLLUP = "View Problem Rollup";

    /** Common action "view documentation pages". */
    public static final String VIEW_HELP = "View Documentation Help";
    /** Common action "view report-level help". */
    public static final String VIEW_REPORT_HELP = " Report-Level Help";

    /** Common action "view transaction export". */
    public static final String VIEW_TRANS_EXPORT = "View Transaction Export";

    /** Common action "view KC models tab". */
    public static final String VIEW_KC_MODEL_INFO = "View KCM";
    /** Common action "view custom fields tab". */
    public static final String VIEW_CUSTOM_FIELDS = "View Custom Fields";
    /** Common action "view step list". */
    public static final String VIEW_STEP_LIST = "View Step List";
    /** Common action "view citation". */
    public static final String VIEW_CITATION = "View Citation";

    /** Common action "View Terms". */
    public static final String VIEW_TERMS = "View Terms";
    /** Common action "View Terms". */
    public static final String VIEW_MANAGE_TERMS = "View Manage Terms";
    /** Common action "View Terms". */
    public static final String VIEW_EDIT_TERMS = "View Edit Terms";
    /** Common action "View Terms". */
    public static final String AGREE_TERMS = "Agree Terms";
    /** Common action "View Terms". */
    public static final String SAVE_TERMS = "Save Terms";
    /** Common action "View Terms". */
    public static final String CREATE_TERMS = "Create Terms";
    /** Common action "View Terms". */
    public static final String DELETE_TERMS = "Delete Terms";
    /** Common action "View Terms". */
    public static final String RETIRE_TERMS = "Retire Terms";
    /** Common action "View Terms". */
    public static final String APPLY_TERMS = "Apply Terms";
    /** Common action "View Terms". */
    public static final String CLEAR_TERMS = "Clear Terms";
    /** Common action "Create Account". */
    public static final String CREATE_ACCOUNT = "Create Account";

    // KC Model Actions
    /** Common action "added KC Model". */
    public static final String MODEL_ADD = "Added KCM";
    /** Common action "deleted KC Model". */
    public static final String MODEL_DELETE = "Deleted KCM";
    /** Common action "downloaded KC Model". */
    public static final String MODEL_EXPORT = "Export KCM";
    /** Common action "renamed KC Model". */
    public static final String MODEL_RENAME = "Renamed KCM";
    /** Common action "overwrote KC Model". */
    public static final String MODEL_OVERWRITE = "Overwrote KCM";
    /** Common action "verified KC Model". */
    public static final String MODEL_VERIFY = "Verified KCM";
    /** Common action "Imported KC Model". */
    public static final String MODEL_IMPORT = "Imported KCM";
    /** Common action "LFA model". */
    public static final String MODEL_LFA = "Run LFA on KCM";
    /** Common action "aggregate model". */
    public static final String MODEL_AGGREGATE = "Aggregate KCM";

    /** Action for advanced tab : "View Metrics". */
    public static final String VIEW_METRICS = "View Metrics";

    /** Action for web services: "View Logging Activity Page". */
    public static final String VIEW_LOG_ACT_PAGE = "View Logging Activity Page";
    /** Action for web services: "View Logging Activity Report". */
    public static final String VIEW_LOG_ACT_REPORT = "View Logging Activity Report";
    /** Action for web services: "Request Logging Activity Role". */
    public static final String LOG_ACT_REQUEST_ROLE_ACTION = "Request Logging Activity Role";

    /** Action for web services: "View Web Services Credentials". */
    public static final String VIEW_WEB_SERV_CRED = "View Web Services Credentials";
    /** Action for web services: "Create Web Services Key". */
    public static final String WEB_SERV_CREATE_KEY_ACTION = "Create Web Services Key";
    /** Action for web services: "Request Web Services Role". */
    public static final String WEB_SERV_REQUEST_ROLE_ACTION = "Request Web Services Role";
    /** Action for actual call to web services: "Web Services". */
    public static final String WEB_SERV_ACTION = "Web Services";

    /** Common action "View External Tool List". */
    public static final String VIEW_EXTERNAL_TOOL_TABLE = "View External Tool Table";
    /** Common action "View External Tool". */
    public static final String VIEW_EXTERNAL_TOOL = "View External Tool";
    /** Common action "Request External Tool Role". */
    public static final String REQ_EXTERNAL_TOOL_ROLE = "Request External Tool Role";
    /** Common action "Add External Tool". */
    public static final String ADD_EXTERNAL_TOOL = "Add External Tool";
    /** Common action "Delete External Tool". */
    public static final String DELETE_EXTERNAL_TOOL = "Delete External Tool";
    /** Common action "Add External Tool File". */
    public static final String UPLOAD_EXTERNAL_TOOL_FILE = "Upload External Tool File";
    /** Common action "Delete External Tool File". */
    public static final String DELETE_EXTERNAL_TOOL_FILE = "Delete External Tool File";
    /** Common action "Download External Tool File". */
    public static final String DOWNLOAD_EXTERNAL_TOOL_FILE = "Download External Tool File";
    /** Common action "Download External Tool Files". */
    public static final String DOWNLOAD_EXTERNAL_TOOL_FILES = "Download External Tool Files";
    /** Common action "Edit External Tool". */
    public static final String EDIT_EXTERNAL_TOOL = "Edit External Tool";

    // IRB-related constants
    /** Common action "Modify Project IRB". */
    public static final String MODIFY_PROJECT_IRB = "Modify Project IRB";
    /** Common action "Create IRB". */
    public static final String CREATE_IRB = "Create IRB";
    /** Common action "Edit IRB". */
    public static final String EDIT_IRB = "Edit IRB";
    /** Common action "Add IRB file". */
    public static final String ADD_IRB_FILE = "Add IRB file";
    /** Common action "Remove IRB file". */
    public static final String REMOVE_IRB_FILE = "Remove IRB file";
    /** Common action "Download IRB file". */
    public static final String DOWNLOAD_IRB_FILE = "Download IRB file";
    /** Common action "Delete IRB". */
    public static final String DELETE_IRB = "Delete IRB";
    /** Common action "Submit for Review". */
    public static final String SUBMIT_PROJECT_FOR_REVIEW = "Submit for IRB/shareability review";
    /** Common action "Add New User". */
    public static final String ADD_NEW_USER_ACCESS = "Add New User";

    /** Action for administrator tool: "Save Domain/LearnLab". */
    public static final String SAVE_DOMAIN_LEARNLAB = "Save Domain/LearnLab";
    /** Action for administrator tool: "Save Junk Flag". */
    public static final String SAVE_JUNK_FLAG = "Save Junk Flag";

    //----- Push Button Upload-related constants -----
    /** String constant. */
    public static final String VIEW_UPLOAD_DATASET = "View Upload Dataset";
    /** String constant. */
    public static final String VIEW_CREATE_PROJECT = "View Create Project";
    /** String constant. */
    public static final String VIEW_IMPORT_QUEUE = "View Import Queue";
    /** String constant. */
    public static final String DATASET_CREATE = "Dataset Created";
    /** String constant. */
    public static final String DATASET_UPLOAD = "Dataset Uploaded";
    /** String constant. */
    public static final String DATASET_RENAME = "Dataset Renamed";
    /** String constant. */
    public static final String DATASET_RELEASE = "Dataset Released";
    /** String constant. */
    public static final String DATASET_MOVE = "Dataset Moved";
    /** String constant. */
    public static final String DATASET_DELETE = "Dataset Deleted";
    /** String constant. */
    public static final String PROJECT_CREATE = "Project Created";
    /** String constant. */
    public static final String PROJECT_DELETE = "Project Deleted";
    /** String constant. */
    public static final String PROJECT_RENAME = "Project Renamed";
    /** String constant. */
    public static final String REQUEST_DS_EDIT_ROLE = "Request DataShop-Edit Role";
    /** String constant. */
    public static final String REMOVE_FROM_QUEUE = "Remove from Queue";
    /** String constant. */
    public static final String UPDATE_STATUS_IN_QUEUE = "Update Status in Queue";
    /** String constant. */
    public static final String IMPORT_ANYWAY = "Import Anyway";
    /** String constant. */
    public static final String UNDO_IMPORT_ANYWAY = "Undo Import Anyway";
    /** String constant. */
    public static final String IQ_MOVE_UP = "Move Up";
    /** String constant. */
    public static final String IQ_MOVE_DOWN = "Move Down";
    /** Dataset accessed by someone who is not DataShop admin nor project admin. */
    public static final String ACTION_DATASET_ACCESSED = "Dataset Accessed";
    /** String constant. */
    public static final String CF_MODIFY = "CF Modify";

    /** Common action "View Research Goals". */
    public static final String VIEW_RESEARCH_GOALS = "View Research Goals";
    /** Common action "Edit Research Goals". */
    public static final String EDIT_RESEARCH_GOALS = "Edit Research Goals";

    // Problem Content-related actions
    /** Common action "View Problem Content Info". */
    public static final String VIEW_PROBLEM_CONTENT_INFO = "View Problem Content Info";
    /** Common action "View Problem List". */
    public static final String VIEW_PROBLEM_LIST = "View Problem List";
    /** Common action "Download Problem List". */
    public static final String EXPORT_PROBLEM_LIST = "Export Problem List";
    /** Common action "View Problem Content Tool". */
    public static final String VIEW_PROBLEM_CONTENT_TOOL = "View Problem Content Tool";
    /** Common action "Map Dataset to Problem Content". */
    public static final String MAP_PROBLEM_CONTENT = "Map Dataset to Problem Content";
    /** Common action "Remove Dataset to Problem Content Map". */
    public static final String UNMAP_PROBLEM_CONTENT = "Remove Dataset to Problem Content Map";
    /** Common action "Delete Problem Content". */
    public static final String DELETE_PROBLEM_CONTENT = "Delete Problem Content";

    /** Workflows Log in. */
    public static final String WORKFLOWS_LOGIN = "Workflows Login";
    /** Workflows Log out. */
    public static final String WORKFLOWS_LOGOUT = "Workflows Logout";

    /** Common action "View Workflow List". */
    public static final String VIEW_WORKFLOW_LIST = "View Workflow List";

    /** Common action "Export Discourse". */
    public static final String EXPORT_DISCOURSE = "Export Discourse";

    /** List of view actions for checking against to see if we need to re-record. */
    public static final List <String> VIEW_ACTIONS = new ArrayList <String> ();
    /** Use this action when an user attempts to save too large a sample as a dataset. */
    public static final String SAMPLE_EXCEEDS_MAX_TXS = "Sample to Dataset exceeds max transactions";


    static {
        VIEW_ACTIONS.add(VIEW_PROJECT_DATASETS);
        VIEW_ACTIONS.add(VIEW_PROJECT_PERMISSIONS);
        VIEW_ACTIONS.add(VIEW_PROJECT_IRB);
        VIEW_ACTIONS.add(VIEW_PROJECT_TERMS);
        VIEW_ACTIONS.add(VIEW_ALL_IRBS);
        VIEW_ACTIONS.add(VIEW_IRB_REVIEW);

        VIEW_ACTIONS.add(VIEW_DATASET_INFO);
        VIEW_ACTIONS.add(VIEW_PAPERS);
        VIEW_ACTIONS.add(VIEW_EXTERNAL_ANALYSES);
        VIEW_ACTIONS.add(VIEW_CITATION);
        VIEW_ACTIONS.add(VIEW_FILES);

        VIEW_ACTIONS.add(VIEW_ER_BY_KC);
        VIEW_ACTIONS.add(VIEW_ER_BY_PROBLEM);

        VIEW_ACTIONS.add(VIEW_LC_BY_KC);
        VIEW_ACTIONS.add(VIEW_LC_BY_STUDENT);
        VIEW_ACTIONS.add(VIEW_LFA_VALUES);

        VIEW_ACTIONS.add(VIEW_PERFORMANCE_PROFILER);

        VIEW_ACTIONS.add(VIEW_TRANS_EXPORT);
        VIEW_ACTIONS.add(VIEW_STEP_ROLLLUP);
        VIEW_ACTIONS.add(VIEW_PROBLEM_ROLLUP);

        VIEW_ACTIONS.add(VIEW_HELP);
        VIEW_ACTIONS.add(VIEW_REPORT_HELP);

        VIEW_ACTIONS.add(VIEW_KC_MODEL_INFO);
        VIEW_ACTIONS.add(VIEW_STEP_LIST);

        VIEW_ACTIONS.add(VIEW_METRICS);
        VIEW_ACTIONS.add(VIEW_WEB_SERV_CRED);
        VIEW_ACTIONS.add(VIEW_LOG_ACT_PAGE);
        VIEW_ACTIONS.add(VIEW_LOG_ACT_REPORT);
        VIEW_ACTIONS.add(VIEW_TERMS);
        VIEW_ACTIONS.add(VIEW_MANAGE_TERMS);
        VIEW_ACTIONS.add(VIEW_EDIT_TERMS);
        VIEW_ACTIONS.add(VIEW_MANAGE_ACCESS_REQUESTS);

        VIEW_ACTIONS.add(VIEW_EXTERNAL_TOOL_TABLE);
        VIEW_ACTIONS.add(VIEW_EXTERNAL_TOOL);
        VIEW_ACTIONS.add(VIEW_RESEARCH_GOALS);

        VIEW_ACTIONS.add(VIEW_UPLOAD_DATASET);
        VIEW_ACTIONS.add(VIEW_CREATE_PROJECT);
        VIEW_ACTIONS.add(VIEW_IMPORT_QUEUE);

        VIEW_ACTIONS.add(VIEW_PROBLEM_CONTENT_INFO);
        VIEW_ACTIONS.add(VIEW_PROBLEM_CONTENT_TOOL);
        VIEW_ACTIONS.add(VIEW_PROBLEM_LIST);
    }

    /**
     * Default Constructor - because this class is only applied by static internal classes
     * the any attempts to instantiate will return an UnsupportedOperationException.
     */
    protected UserLogger() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    /**
     * Log a message to the database.
     * @param user The user who performed the action.
     * @param action The action being logged.
     */
    public static void log(UserItem user, String action) {
        log(null, user, action, null);
    }

    /**
     * Log a message to the database.
     * @param user The user who performed the action.
     * @param action The action being logged.
     * @param info Additional information about the action.
     * @param checkIfRecorded boolean indicating whether to check if this was already recorded.
     */
    public static void log(UserItem user, String action, String info,
            boolean checkIfRecorded) {
        log(null, user, action, info, checkIfRecorded);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param user The user who performed the action.
     * @param action The action being logged.
     */
    public static void log(DatasetItem dataset, UserItem user, String action) {
        log(dataset, user, action, null);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param user The user who performed the action.
     * @param action The action being logged.
     * @param info Additional information about the action.
     */
    public static void log(DatasetItem dataset, UserItem user, String action, String info) {
        log(dataset, user, action, info, true);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param user The user who performed the action.
     * @param action The action being logged.
     * @param info Additional information about the action.
     * @param checkIfRecorded boolean indicating whether to check if this was already recorded.
     */
    public static void log(DatasetItem dataset, UserItem user, String action, String info,
            boolean checkIfRecorded) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for User logging");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null for User logging");
        }

        DatasetUserLogDao logDao = DaoFactory.DEFAULT.getDatasetUserLogDao();

        //check if this action has already been properly recorded.
        if (checkIfRecorded) {
            if (logDao.isAlreadyRecorded(dataset, user, action)) { return; }
        }

        DatasetUserLogItem newLog = new DatasetUserLogItem();
        newLog.setAction(action);
        newLog.setDataset(dataset);
        newLog.setUser(user);
        newLog.setInfo(info);
        newLog.setTime(new Date());
        logDao.saveOrUpdate(newLog);
        if (logger.isDebugEnabled()) {
            logger.debug("Logging to dataset user log table, user: "
                    + user.getId() + ", action: " + action + ", info: " + info);
        }
    }

    //---- Custom Field section -----

    /**
     * Log a Custom Field Add action.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param customFieldId the id of the custom field
     * @param customFieldName the name of the custom field
     * @param wsFlag indicates whether user made change through Web Services or UI
     */
    public static void logCfAdd(DatasetItem datasetItem, UserItem userItem,
            Long customFieldId, String customFieldName, Boolean wsFlag) {
        String msg = "Custom field added, " + "'" + customFieldName + "'";
        logCfAction(datasetItem, userItem, msg, customFieldId, wsFlag, true);
    }

    /**
     * Log a Custom Field Modify Name action.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param customFieldId the id of the custom field
     * @param prevName the old name of the custom field
     * @param newName the new name of the custom field
     * @param wsFlag indicates whether user made change through Web Services or UI
     */
    public static void logCfModifyName(DatasetItem datasetItem, UserItem userItem,
            Long customFieldId, String prevName, String newName, Boolean wsFlag) {
        String msg = "Custom field name modified, "
            + "'" + prevName + "' changed to '" + newName + "'";
        logCfAction(datasetItem, userItem, msg, customFieldId, wsFlag, true);
    }

    /**
     * Log a Custom Field Modify Name action.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param customFieldId the id of the custom field
     * @param customFieldName the name of the custom field
     * @param wsFlag indicates whether user made change through Web Services or UI
     */
    public static void logCfModifyDesc(DatasetItem datasetItem, UserItem userItem,
            Long customFieldId, String customFieldName, Boolean wsFlag) {
        String msg = "Custom field desc modified, " + "'" + customFieldName + "'";
        logCfAction(datasetItem, userItem, msg, customFieldId, wsFlag, false);
    }

    /**
     * Log a Custom Field Add action.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param customFieldId the id of the custom field
     * @param customFieldName the name of the custom field
     * @param numTxs number of transactions affected
     * @param wsFlag indicates whether user made change through Web Services or UI
     */
    public static void logCfDelete(DatasetItem datasetItem, UserItem userItem,
            Long customFieldId, String customFieldName, int numTxs, Boolean wsFlag) {
        String msg = "Custom field deleted from "
            + numTxs + " transactions, " + "'" + customFieldName + "'";
        logCfAction(datasetItem, userItem, msg, customFieldId, wsFlag, true);
    }

    /**
     * Log a Custom Field Set Values action.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param customFieldId the id of the custom field
     * @param customFieldName the name of the custom field
     * @param wsFlag indicates whether user made change through Web Services or UI
     */
    public static void logCfSet(DatasetItem datasetItem, UserItem userItem,
            Long customFieldId, String customFieldName, Boolean wsFlag) {
        String msg = "Custom field data set, " + "'" + customFieldName + "'";
        logCfAction(datasetItem, userItem, msg, customFieldId, wsFlag, true);
    }


    /**
     * Log a Custom Field action, used by other logCf methods.
     * @param datasetItem the dataset item
     * @param userItem the user item
     * @param msg part of info field that varies
     * @param customFieldId the id of the custom field
     * @param wsFlag indicates whether user made change through Web Services or UI
     * @param recacheNeeded true if the change should kick off a re-cache of the data
     */
    private static void logCfAction(DatasetItem datasetItem, UserItem userItem,
            String msg, Long customFieldId, Boolean wsFlag, Boolean recacheNeeded) {
        String action = CF_MODIFY;
        String info = "Dataset '" + datasetItem.getDatasetName() + "' ("
                + datasetItem.getId() + "): "
                + msg
                + " (" + customFieldId + "). ";
        if (wsFlag) { info += " WebServices."; }
        log(datasetItem, userItem, action, info);
        if (recacheNeeded) {
            SystemLogger.logCfModify(datasetItem);
        }
    }
}
