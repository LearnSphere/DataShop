/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.helper;

import java.util.Date;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetSystemLogItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Static class that allows system logs to be written.
 * @author Benjamin Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SystemLogger {
    /* Please keep in alphabetical order. */
    /** Common action "aggregate". */
    public static final String ACTION_AGGREGATE = "aggregate";
    /** Common action "aggregate sample". */
    public static final String ACTION_AGGREGATE_SAMPLE = "aggregate sample";
    /** Common action "aggregate KC model". */
    public static final String ACTION_AGGREGATE_KC_MODEL = "aggregate KC model";
    /** Common action "cached transaction export". */
    public static final String ACTION_CACHED_TX_EXPORT = "cached transaction export";
    /** Common action "cached student step export". */
    public static final String ACTION_CACHED_STEP_EXPORT = "cached student step export";
    /** Common action "cached student problem export". */
    public static final String ACTION_CACHED_PROBLEM_EXPORT = "cached student problem export";
    /** Common action "generated student problem export". */
    public static final String ACTION_GENERATED_PROBLEM_EXPORT = "generated student problem export";
    /** Common action "cached KC model export". */
    public static final String ACTION_CACHED_KCM_EXPORT = "cached KC model step export";

    /** Common action "started cached transaction export". */
    public static final String ACTION_CACHED_TX_EXPORT_START =
        "started cached transaction export";
    /** Common action "started cached transaction export". */
    public static final String ACTION_CACHED_STEP_EXPORT_START =
        "started cached student step export";
    /** Common action "started cached transaction export". */
    public static final String ACTION_CACHED_PROBLEM_EXPORT_START =
        "started cached student problem export";
    /** Common action "started KC model export". */
    public static final String ACTION_CACHED_KCM_EXPORT_START = "started KC model step export";
    /** Common action "create". */
    public static final String ACTION_CREATE_DATASET = "create";
    /** Common action "generate KC models". */
    public static final String ACTION_GENERATE_MODELS = "generate KC models";
    /** Common action "modify". */
    public static final String ACTION_MODIFY_DATASET = "modify";
    /** Common action "pe gen". */
    public static final String ACTION_PE_GEN = "pe gen";
    /** Common action "run LFA". */
    public static final String ACTION_LFA = "run LFA";
    /** Common action "backfill LFA". */
    public static final String ACTION_LFA_BACKFILL = "backfill LFA";

    /** Common action "run cross validation". */
    public static final String ACTION_CV = "run cross validation";

    /** Common action "log conversion done". */
    public static final String ACTION_LOG_CONVERSION_DONE = "log conversion done";
    /** Common action "school auto set". */
    public static final String ACTION_SCHOOL_AUTOSET = "school auto set";
    /** Common action "set tx duration". */
    public static final String ACTION_SET_TX_DURATION = "set tx duration";
    /** Common action "times auto set". */
    public static final String ACTION_TIMES_AUTOSET = "times auto set";
    /** Common action "set step rollup conditions". */
    public static final String ACTION_SET_STEP_ROLLUP_CONDITIONS = "set step rollup conditions";
    /** Common action "updated problem hierarchy". */
    public static final String ACTION_UPDATED_PROBLEM_HIERARCHY = "updated problem hierarchy";
    /** Common action "purge deleted dataset". */
    public static final String PURGE_DELETED_DATASET = "purge deleted dataset";
    /** Create sample action for the dataset system log. */
    public static final String ACTION_CREATE_SAMPLE = "create sample";
    /** Rename sample action for the dataset system log. */
    public static final String ACTION_RENAME_SAMPLE = "rename sample";
    /** Modify filters in sample action for the dataset system log. */
    public static final String ACTION_MODIFY_FILTERS = "modify filters";
    /** Create dataset from sample action for the dataset system log. */
    public static final String ACTION_CREATE_DATASET_FROM_SAMPLE = "create dataset from sample";
    /** Delete sample action for the dataset system log. */
    public static final String ACTION_DELETE_SAMPLE = "delete sample";


    /**
     * Default Constructor - because this class is only applied by static internal classes
     * the any attempts to instantiate will return an UnsupportedOperationException.
     */
    protected SystemLogger() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param skillModel the skillModel associated with the logged action.
     * @param sample The sample information is being logged about.
     * @param action The action being logged.
     * @param info Additional information about the action.
     * @param successFlag boolean indicating whether the action completed successfully
     * @param elapsedTime the elapsed time in milliseconds
     */
    public static void log(DatasetItem dataset, SkillModelItem skillModel,
            SampleItem sample, String action, String info, Boolean successFlag,
            Long elapsedTime) {
        log(dataset, skillModel, sample, action, info, successFlag,
                null, elapsedTime);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param skillModel the skillModel associated with the logged action.
     * @param sample The sample information is being logged about.
     * @param action The action being logged.
     * @param info Additional information about the action.
     * @param successFlag boolean indicating whether the action completed successfully
     * @param value the number of items processed
     * @param elapsedTime the elapsed time in milliseconds
     */
    public static void log(DatasetItem dataset, SkillModelItem skillModel,
            SampleItem sample, String action, String info, Boolean successFlag,
            Integer value, Long elapsedTime) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset cannot be null for system logging");
        }

        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null for system logging");
        }

        DatasetSystemLogItem newLog = new DatasetSystemLogItem();
        newLog.setSkillModel(skillModel);
        newLog.setSample(sample);
        newLog.setAction(action);
        newLog.setDataset(dataset);
        newLog.setInfo(info);
        newLog.setSuccessFlag(successFlag);
        newLog.setTime(new Date());
        newLog.setValue(value);
        newLog.setElapsedTime(elapsedTime);
        DaoFactory.DEFAULT.getDatasetSystemLogDao().saveOrUpdate(newLog);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param action The action being logged.
     */
    public static void log(DatasetItem dataset, String action) {
        log(dataset, null, null, action, null, null, null);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param action The action being logged.
     * @param info Additional information about the action.
     */
    public static void log(DatasetItem dataset, String action, String info) {
        log(dataset, null, null, action, info, null, null);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param action The action being logged.
     * @param successFlag boolean indicating whether the action completed successfully
     * @param elapsedTime the elapsed time in milliseconds
     */
    public static void log(DatasetItem dataset, String action,
            Boolean successFlag, Long elapsedTime) {
        log(dataset, null, null, action, null, successFlag, elapsedTime);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param sample The sample associated with this action.
     * @param action The action being logged.
     * @param successFlag boolean indicating whether the action completed successfully
     */
    public static void log(DatasetItem dataset, SampleItem sample,
            String action, Boolean successFlag) {
        log(dataset, null, sample, action, null, successFlag, null);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param skillModel The skillModel associated with this action.
     * @param action The action being logged.
     * @param info the info string
     * @param successFlag boolean indicating whether the action completed successfully
     * @param elapsedTime the elapsed time in milliseconds
     */
    public static void log(DatasetItem dataset, SkillModelItem skillModel,
            String action, String info,
            Boolean successFlag, Long elapsedTime) {
        log(dataset, skillModel, null, action, info, successFlag, elapsedTime);
    }

    /**
     * Log a message to the database.
     * @param dataset The dataset information is being logged about.
     * @param action The action being logged.
     * @param info Additional information about the action.
     * @param successFlag boolean indicating whether the action completed successfully
     */
    public static void log(DatasetItem dataset, String action, String info, Boolean successFlag) {
        log(dataset, null, null, action, info, successFlag, null);
    }

    /**
     * Check if the CF Modify needs to be logged before logging another one.
     * @param datasetItem the dataset item
     */
    public static void logCfModify(DatasetItem datasetItem) {
        DatasetSystemLogDao logDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
        if (logDao.cfModifyNeedsLogged(datasetItem)) {
            String info = "Dataset '" + datasetItem.getDatasetName() + "'"
                    + " (" + datasetItem.getId() + "): Custom fields modified.";
            log(datasetItem, UserLogger.CF_MODIFY, info);
        }
    }
}
