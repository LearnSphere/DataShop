/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetSystemLogItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * DatasetSystemLog Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetSystemLogDao extends AbstractDao<DatasetSystemLogItem> {

    /** "OK" status. */
    String STATUS_OK = "OK";
    /** "WAIT" status. */
    String STATUS_WAIT = "WAIT";
    /** "ERROR" status. */
    String STATUS_ERROR = "ERROR";

    /**
     * Standard get for a DatasetSystemLogItem by id.
     * @param id The id of the DatasetSystemLogItem.
     * @return the matching DatasetSystemLogItem or null if none found
     */
    DatasetSystemLogItem get(Integer id);

    /**
     * Standard find for an DatasetSystemLogItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetSystemLogItem.
     * @return the matching DatasetSystemLogItem.
     */
    DatasetSystemLogItem find(Integer id);

    /**
     * Standard "find all" for DatasetSystemLogItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Determine if there are any samples that require transaction export caching.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    List<SampleItem> getSamplesToCacheTx(DatasetItem dataset);

    /**
     * Determine if there are any samples that require student-step export caching.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    List<SampleItem> getSamplesToCacheStep(DatasetItem dataset);

    /**
     * Determine if there are any samples that require student problem export caching.
     * First look for samples whose ACTION_AGGREGATE_SAMPLE timestamp is greater than
     * that sample's ACTION_CACHED_PROBLEM_EXPORT timestamp.  Next, look for samples whose
     * ACTION_CACHED_PROBLEM_EXPORT timestamp is less than a ACTION_AGGREGATE_KC_MODEL
     * timestamp.  Union these two queries and return a list of SampleItems returned
     * from the queries.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    List<SampleItem> getSamplesToCacheProblem(DatasetItem dataset);

    /**
     * Return only those samples whose 'aggregate sample' occurred after the
     * latest 'cached transaction export' action for the given dataset.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    List<SampleItem> getSamplesToCacheAggSampleOnly(DatasetItem dataset);

    /**
     * Return only those samples whose 'aggregate sample' occurred after the
     * latest 'cached transaction export' action for the given dataset or whose
     * dataset has been modified after the cached transaction export.
     * @param dataset the dataset to check.
     * @return a list of SampleItems that require caching.
     */
    List<SampleItem> getSamplesToCacheAggAndModifyActionsOnly(DatasetItem dataset);

    /**
     * Get all the sample IDs and cached transaction export time given a dataset.
     * @param dataset the dataset to check
     * @param cacheAction the string which describes the export type
     * @return sampleList a map that includes the sample id and its cached transaction export time
     * */
    Map<Integer, Date> getSamplesNeedToBeCached(DatasetItem dataset, String cacheAction);

    /**
     * Find out if a dataset has skill models that have been added but not yet cached.
     * @param dataset the dataset to check.
     * @param sample the sample item
     * @param cacheAction the string which describes the export type
     * @return a list of SkillModelItems that are not yet cached (if none then the list is empty).
     */
    List<SkillModelItem> getSkillModelsNotCached(DatasetItem dataset, SampleItem sample,
        String cacheAction);

    /**
     * Determine if a dataset requires transaction duration calculation.
     * @param dataset the dataset to examine.
     * @return true if the dataset requires tx duration calculation, false otherwise.
     */
    Boolean requiresTxDurationCalculation(DatasetItem dataset);

    /**
     * Find out if a dataset has been modified since the last step export file
     * generation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    Boolean requiresCachedStepExportFileGeneration(DatasetItem dataset);

    /**
     * Find out if a dataset has been modified since the last aggregation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    Boolean requiresAggregation(DatasetItem dataset);

    /**
     * Find out if a sample needs to be aggregated because
     * the dataset has been modified since the last aggregation of the sample.
     * @param datasetItem the given dataset
     * @param sampleItem the given sample
     * @return true if aggregation is required, false otherwise
     */
    Boolean requiresAggregation(DatasetItem datasetItem, SampleItem sampleItem);

    /**
     * Find out if a dataset has been modified since the last cached export file
     * generation.
     * @param dataset the dataset to check.
     * @return Boolean of true if aggregation required, false otherwise.
     */
    Boolean requiresCachedTxExportFileGeneration(DatasetItem dataset);

    /**
     * Find out if a dataset has been modified since the last LFA run.
     * @param dataset the dataset to check.
     * @param skillModel the skill model to check.
     * @return Boolean of true if LFA required, false otherwise.
     */
    Boolean requiresLFA(DatasetItem dataset, SkillModelItem skillModel);

    /**
     * Find out if a dataset has been modified since the last CV run.
     * @param dataset the dataset to check.
     * @param skillModel the skill model to check.
     * @return Boolean of true if LFA required, false otherwise.
     */
    Boolean requiresCV(DatasetItem dataset, SkillModelItem skillModel);

    /**
     * Find out if a dataset has been modified since the last KC model generation.
     * @param dataset the dataset to check
     * @return Boolean of true if model generation required, false otherwise.
     */
    Boolean requiresModelGeneration(DatasetItem dataset);

    /**
     * Gets the last date and time that the given dataset had KC models generated.
     * @param dataset the given dataset
     * @return the last date models were generated, null if models not generated or not logged
     */
    Date getLastModelGenerated(DatasetItem dataset);

    /**
     * Gets the last Date and time that the given dataset was last modified.
     * @param dataset the dataset you are getting info on.
     * @return Date of the last date modified, null if never modified or not logged.
     */
    Date getLastModified(DatasetItem dataset);

    /**
     * Gets the last date and time that the given dataset had Problem Events generated.
     * @param dataset the dataset you are getting info on.
     * @return the last date of given action, null if it never occurred or not logged.
     */
    Date getLastPeGen(DatasetItem dataset);

    /**
     * Gets the last Date and time that the given dataset was last created,
     * though in theory it wouldn't be created more than once, but you never know.
     * @param dataset the dataset you are getting info on.
     * @return Date of the last date created, null if never created or creation not logged.
     */
    Date getLastCreated(DatasetItem dataset);

    /**
     * Gets the last Date and time that the given sample was cached for the given exportBy
     * value (byTransaction or byStudentStep).
     * @param sample the sample
     * @param selection the export type string (byStudentStep, byTransaction, or byProblem)
     * @return Date of the last cached time or null if never cached.
     */
    Date getLastCachedTime(SampleItem sample, String selection);

    /**
     * Check for a matching message in the dataset system log.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action to check
     * @return whether an export for this dataset and sample was already started
     */
    boolean messageCheck(DatasetItem dataset, SampleItem sample, String action);

    /**
     * Remove matching messages.  Current use case is removing messages
     * indicating that an export for this dataset and sample was already started.
     * Only call this after the export has successfully completed or failed.
     * @param dataset the dataset
     * @param sample the sample
     * @param action the action message to remove, generally transaction or step export started
     * message.
     */
    void removeMessage(DatasetItem dataset, SampleItem sample, String action);

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was already started.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was already started
     */
    boolean isTransactionExportStarted(DatasetItem dataset, SampleItem sample);

    /**
     * Check for a message in the dataset system log indicating that an export for this dataset
     * and sample was completed.
     * @param dataset the dataset
     * @param sample the sample
     * @return whether an export for this dataset and sample was completed
     */
    boolean isTransactionExportCompleted(DatasetItem dataset, SampleItem sample);

    /**
     * Remove messages indicating that an export for this dataset and sample was already started.
     * Only call this after the export has successfully completed or failed.
     * @param dataset the dataset
     * @param sample the sample
     */
    void removeTransactionExportStartedMessage(DatasetItem dataset, SampleItem sample);

    /**
     * Determine if aggregation for the given sample is complete.
     * @param sample the SampleItem whose aggregation status we are inquiring about.
     * @return OK if aggregation is complete and successful, ERROR if aggregation is complete
     * but not successful, WAIT if no record in the dataset_system_log table exists yet.
     */
    String sampleAggComplete(SampleItem sample);

    /**
     * Check for a "log conversion done" message in the dataset_system_log table for the
     * current date.  If present, return true, otherwise return false.
     * @return true if log conversion has finished for the current date, false otherwise.
     */
    boolean isLogConversionDone();

    /**
     * Check if a CF Modify has been logged since the last run of CFG-transaction.
     * @param datasetItem the dataset item
     * @return true if the CF Modify needs to be logged
     */
    boolean cfModifyNeedsLogged(DatasetItem datasetItem);

    /**
     * Find out if a dataset has been modified since the last problem hierarchy update.
     * @param dataset the dataset to check.
     * @return whether or not the problem hierarchy update is required.
     */
    Boolean requiresProblemHierarchyUpdate(DatasetItem dataset);

    /**
     * Find out if a dataset has been modified since the last KC model step export.
     * @param dataset the dataset to check.
     * @return Boolean of true if export/cache required, false otherwise.
     */
    Boolean requiresCachedKcmStepExportGeneration(DatasetItem dataset);
}