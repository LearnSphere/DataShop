/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.helper;

import java.util.Date;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Helper class for quickly looking up state information about a
 * dataset from wherever the information is stored (typically the system log).
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12268 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-04-28 11:57:31 -0400 (Tue, 28 Apr 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetState {
    protected static final String PROD_IO_ERROR_MSG = "An error occurred while contacting the "
            + "production DataShop servers to create a new dataset record.";
    
    /**
     * Default Constructor - because this class is only applied by static internal classes
     * the any attempts to instantiate will return an UnsupportedOperationException.
     */
    protected DatasetState() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    /**
     * Convenience method to get the dataset system log DAO.
     * @return the dataset system log DAO
     */
    private static DatasetSystemLogDao datasetSystemLogDao() {
        return DaoFactory.DEFAULT.getDatasetSystemLogDao();
    }

    /**
     * Whether we need to run the CachedExportFileGenerator for this dataset.
     * @param dataset a dataset
     * @return whether we need to run the CachedExportFileGenerator for this dataset
     */
    public static Boolean requiresCachedTxExportFileGeneration(DatasetItem dataset) {
        return datasetSystemLogDao().requiresCachedTxExportFileGeneration(dataset);
    }

    /**
     * Whether we need to run the CachedExportFileGenerator for this dataset.
     * @param dataset a dataset
     * @return whether we need to run the CachedExportFileGenerator for this dataset
     */
    public static Boolean requiresCachedStepExportFileGeneration(DatasetItem dataset) {
        return datasetSystemLogDao().requiresCachedStepExportFileGeneration(dataset);
    }

    /**
     * Returns whether a dataset requires aggregation for de-normalized tables.
     * @param dataset the dataset to check
     * @return boolean of true if aggregation required, false otherwise.
     */
    public static Boolean requiresModelGeneration(DatasetItem dataset) {
        return datasetSystemLogDao().requiresModelGeneration(dataset);
    }

    /**
     * Returns whether the given dataset requires transaction duration calculations.
     * @param dataset the dataset to examine.
     * @return true if tx duration calculation is required, false otherwise.
     */
    public static Boolean requiresTxDurationCalculation(DatasetItem dataset) {
        return datasetSystemLogDao().requiresTxDurationCalculation(dataset);
    }

    /**
     * Returns whether a dataset requires aggregation for de-normalized tables.
     * @param dataset the dataset to check
     * @return boolean of true if aggregation required, false otherwise.
     */
    public static Boolean requiresAggregation(DatasetItem dataset) {
        return datasetSystemLogDao().requiresAggregation(dataset);
    }

    /**
     * Returns whether a sample requires aggregation for de-normalized tables.
     * @param datasetItem the given dataset
     * @param sampleItem the given sample
     * @return boolean of true if aggregation required, false otherwise.
     */
    public static Boolean requiresAggregation(DatasetItem datasetItem, SampleItem sampleItem) {
        return datasetSystemLogDao().requiresAggregation(datasetItem, sampleItem);
    }

    /**
     * Returns whether a dataset requires LFA to be run.
     * @param dataset the dataset to check
     * @param skillModel the skill model to check
     * @return boolean of true if LFA needs to be run, false otherwise.
     */
    public static Boolean requiresLFA(DatasetItem dataset, SkillModelItem skillModel) {
        return datasetSystemLogDao().requiresLFA(dataset, skillModel);
    }

    /**
     * Returns whether a dataset requires CV to be run.
     * @param dataset the dataset to check
     * @param skillModel the skill model to check
     * @return boolean of true if CV needs to be run, false otherwise.
     */
    public static Boolean requiresCV(DatasetItem dataset, SkillModelItem skillModel) {
        return datasetSystemLogDao().requiresCV(dataset, skillModel);
    }

    /**
     * Gets the date and time that this dataset was last modified.
     * @param dataset the dataset to check
     * @return the date the dataset was last modified
     */
    public static Date lastModified(DatasetItem dataset) {
        return datasetSystemLogDao().getLastModified(dataset);
    }
    /**
     * Gets the date and time that this dataset was last created,
     * though in theory it wouldn't be created more than once, but you never know.
     * @param dataset the dataset to check
     * @return the date the dataset was last created, null if not created?
     */
    public static Date lastCreated(DatasetItem dataset) {
        return datasetSystemLogDao().getLastCreated(dataset);
    }

    /**
     * Find out if a dataset has been modified since the last problem hierarchy update.
     * @param dataset the dataset to check.
     * @return whether or not the problem hierarchy update is required.
     */
    public static Boolean requiresProblemHierarchyUpdate(DatasetItem dataset) {
        return datasetSystemLogDao().requiresProblemHierarchyUpdate(dataset);
    }
}
