/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.TransactionItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.sampletodataset.SampleRowDto;

/**
 * Sample Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11729 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-11-22 21:34:28 -0500 (Sat, 22 Nov 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SampleDao extends AbstractDao<SampleItem> {

    /**
     * Standard get for a SampleItem by id.
     * @param id The id of the SampleItem.
     * @return the matching SampleItem or null if none found
     */
    SampleItem get(Integer id);

    /**
     * Standard find for an SampleItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SampleItem.
     * @return the matching SampleItem.
     */
    SampleItem find(Integer id);

    /**
     * Standard "find all" for SampleItems.
     * @return a List of objects
     */
    List<SampleItem> findAll();

    /**
     * Get a list of samples for the dataset.
     * @param dataset The datasetItem to get all samples for.
     * @return a List of SampleItems that meet the criteria
     */
    List<SampleItem> find(DatasetItem dataset);

    /**
     * Get a list of samples for the dataset that are either
     * flagged as global or owned by the user.
     * @param dataset The datasetItem to get all samples for.
     * @param user The user to get all samples for.
     * @return a List of SampleItems that meet the criteria
     */
    List<SampleItem> find(DatasetItem dataset, UserItem user);

    /**
     * Get a list of samples for the dataset that are
     * owned by the user and have the given name.
     * @param dataset The datasetItem to get all samples for.
     * @param user The user to get all samples for.
     * @param sampleName the sample name
     * @return a List of SampleItems that meet the criteria
     */
    List<SampleItem> find(DatasetItem dataset, UserItem user, String sampleName);

    /**
     * Get a list of samples for the dataset that are
     * flagged as global and have the given name.
     * @param dataset The datasetItem to get all samples for.
     * @param globalFlag the global flag
     * @param sampleName the sample name
     * @return a List of SampleItems that meet the criteria
     */
    List<SampleItem> find(DatasetItem dataset, Boolean globalFlag, String sampleName);

    /**
     * Creates a default sample which is all transactions for the given dataset.
     * @param dataset to create the default sample for.
     * @return the All Data sample for the given dataset, or null if the dataset
     * has no transactions.
     */
    SampleItem findOrCreateDefaultSample(DatasetItem dataset);

    /**
     * Gets a list of skills associated with this sample.
     * @param sampleItem the sample item
     * @param skillModelItem the skillModel to get skills for.
     * @return a list of SkillItems that fall under this sample/skillModel.
     */
    List<SkillItem> getSkillList(final SampleItem sampleItem, final SkillModelItem skillModelItem);

    /**
     * Gets a list of problems associated with this sample.
     * @param sampleItem the sample item
     * @return a list of ProblemItems that fall under this sample.
     */
    List<ProblemItem> getProblemList(SampleItem sampleItem);

    /**
     * Gets a list of students associated with this sample.
     * @param sampleItem the sample item
     * @return a list of StudentItems that fall under this sample.
     */
    List<StudentItem> getStudentList(SampleItem sampleItem);

    /**
     * Gets a list of students associated with this sample.
     * @param sampleItem the sample item
     * @return a list of StudentItems that fall under this sample.
     */
    List<StudentItem> getStudentListFromAggregate(SampleItem sampleItem);

    /**
     * Returns the number of transactions that fall under this sample.
     * @param sampleItem the sampleItem to get number of transactions for.
     * @return Long of the number of transactions.
     */
    Long getNumTransactions(SampleItem sampleItem);

    /**
     * Returns the number of performed steps that fall under this sample.
     * @param sampleItem the sampleItem to get information for.
     * @return Long of the number of performed steps.
     */
    Long getNumPerformedSteps(SampleItem sampleItem);

    /**
     * Returns the number of unique steps that fall under this sample.
     * @param sampleItem the sampleItem to get information for.
     * @return Long of the number of unique steps.
     */
    Long getNumUniqueSteps(SampleItem sampleItem);

    /**
     * Returns the unique steps that are in this sample.
     * @param sample the sampleItem to get information for.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return list of subgoal items
     */
    List<SubgoalItem> getUniqueSteps(SampleItem sample, Integer limit, Integer offset);

    /**
     * Returns the number of students that fall under this sample.
     * @param sampleItem the sampleItem to get information for.
     * @return Long of the number of students.
     */
    Long getNumStudents(SampleItem sampleItem);

    /**
     * Find the number of sessions for the given sample
     * using the Transaction Sample Map (faster).
     * @param sampleItem the given sample
     * @return the number of sessions for the given sample
     */
    Long getNumSessions(SampleItem sampleItem);

    /**
     * Returns a list of all transactions associated with this sample.
     * @param sampleItem the sampleItem to get transactions for
     * @return list of TransactionsItems
     */
    List<TransactionItem> getTransactions(SampleItem sampleItem);

    /**
     * Returns a list of all transactions associated with this sample and student.
     * @param sampleItem the sample item to get transactions for.
     * @param student StudentItem to get transactions for.
     * @return a List of TransactionItems
     */
    List<TransactionItem> getTransactions(SampleItem sampleItem, StudentItem student);

    /**
     * Returns a list of transactions associated with this sample.
     * @param sampleItem the sampleItem to get transactions for
     * @param limit The number of transactions to return.
     * @param offset The offset for the start position on number returned.
     * @return List of TransactionsItems
     */
    List<TransactionItem> getTransactions(SampleItem sampleItem, Integer limit, Integer offset);

    /**
     * Returns a list of object arrays (Object[]) representing rows and columns in a data preview
     * With columns determined by the ordered set of filters.
     * @param sampleItem the sample to get a preview for.
     * @param offset Integer indicating the offset to begin limiting returned rows.
     * @param limit Integer indicating how many rows to return.
     * @return a List of Lists with objects in rows and columns for the preview.
     */
    List getTransactionsPreview(SampleItem sampleItem, Integer offset, Integer limit);

    /**
     * This function will walk through each filter in turn attempting to determine
     * which one caused the problem.
     * @param sampleItem The sample that has a potential problem
     * @return String of the error or null if no error found. <br>
     * If an error is found it will be of the format...<br>
     * FILTER_ERROR|(filter class)|(filter attribute)|(filter operator)|(filter string)<br>
     * or ERROR|(error string)
     */
    String determineError(SampleItem sampleItem);

    /**
     * Inserts transaction/sample pairs into the transaction_sample_map.
     * <strong>Note: </strong> this function is transactional by student.
     * @param sample the sample to save.
     * @return number of mappings inserted.
     */
    int populateTransactionSampleMap(SampleItem sample);

    /**
     * Inserts transaction/sample pairs into the transaction_sample_map.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param sample the sample to save.
     * @param student the student to save.
     * @return number of mappings inserted.
     */
    int populateTransactionSampleMap(SampleItem sample, StudentItem student);

    /**
     * Remove all step transaction <--> sample mappings for the given sample.
     * @param sampleItem the sample to remove all mappings for for.
     * @return number of items deleted.
     */
    int removeAllTransactionMappings(SampleItem sampleItem);

    /**
     * Return the max number of skills for a single transactions in a given sample and skillModel.
     * Used in export.
     * @param sample the sample to test.
     * @param skillModel the skillModel to count against
     * @return Long (max skill count)
     */
    Long getMaxSkillCount(SampleItem sample, SkillModelItem skillModel);

    /**
     * Return the max number of conditions for single transactions in a given sample.
     * Used in export.
     * @param sample the sample to test.
     * @return Long (max condition count)
     */
    Long getMaxConditionCount(SampleItem sample);

    /**
     * Return the max number of students for single transactions in a given sample.
     * Used in export.
     * @param sample the sample to test.
     * @return Long (max student count)
     */
    Long getMaxStudentCount(SampleItem sample);

    /**
     * Return the max number of attempt selections for a given sample.  Used in export.
     * @param sample the sample to test.
     * @return an Integer (count).
     */
    Long getMaxAttemptSelectionCount(SampleItem sample);

    /**
     * Return the max number of attempt inputs for a given dataset.  Used in export.
     * @param sample the sample to count max inputs for
     * @return an Long (count).
     */
    Long getMaxAttemptInputCount(SampleItem sample);

    /**
     * Return the max number of attempt actions for a given dataset.  Used in export.
     * @param sample the sample to count max inputs for
     * @return an Long (count).
     */
    Long getMaxAttemptActionCount(SampleItem sample);

    /**
     * Need to expose the "getSession" method for use with getSPTransactions.
     * @return a Session
     */
    Session session();

    /**
     * Need to expose the "releaseSession" method for use with getSPTransactions.
     * @param session the session to release
     */
    void release(Session session);

    /**
     * Get the unique steps for the dataset level and sample.
     * @param datasetLevelId the id of the dataset level
     * @param sample the sample
     * @param session pass in the session so it can be retained for the life of the result set
     * @param limit used to set the fetch-ahead limit
     * @return result set for the unique steps for the dataset level and sample
     * @throws SQLException if the database explodes
     */
    ResultSet getStepsForDatasetLevel(Integer datasetLevelId, SampleItem sample,
            Session session, int limit) throws SQLException;

    /**
     * Handles data access for the transaction export.  Maintains a single connection throughout
     * transaction export processing in order to ensure access to temporary tables.
     * @return a transaction export DAO
     */
    TxExportDao getTxExportDao();

    /**
     * Find the sample row info for all samples in a dataset except deleted ones.
     * @param datasetId the dataset id
     * @return a List of objects
     */
    List<SampleRowDto> getDsAdminSampleRowInfo(Long datasetId);

    /**
     * Find the sample row info for global samples and those owned by the user.
     * @param userId the user id
     * @param datasetId the dataset id
     * @return a List of objects
     */
    List<SampleRowDto> getSampleRowInfo(String userId, Long datasetId);

    /**
     * Find by sample name.
     * @param sampleName the sample name
     */
    List<SampleItem> find(String sampleName);

}
