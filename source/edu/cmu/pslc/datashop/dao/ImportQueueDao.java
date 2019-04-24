/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.discoursedb.item.DiscourseItem;
import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Import Queue Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12866 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-20 15:52:56 -0500 (Wed, 20 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ImportQueueDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ImportQueueItem get(Integer id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ImportQueueItem find(Integer id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return a list of Import Queue DTO objects given a username
     * for the My Datasets page.
     * @param username the account/user of the current user
     * @return a list of DTOs
     */
    List<ImportQueueDto> getImportQueueByUploader(String username);

    /**
     * Return a list of Import Queue DTO objects given a project id
     * for the Project Datasets page.
     * @param projectId the project id
     * @return a list of Import Queue DTO objects
     */
    List<ImportQueueDto> getImportQueueByProject(Integer projectId);

    /**
     * Return a list of Import Queue DTO objects for the DiscourseDB project.
     * @param projectId the project id
     * @return a list of Import Queue DTO objects
     */
    List<ImportQueueDto> getImportQueueByDiscourseProject(Integer projectId);

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the whole import queue.
     * @return a list of Import Queue DTO objects
     */
    List<ImportQueueDto> getImportQueueForAdmin();

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the recent items not in the queue.
     * @param cutoffDate the cut off date
     * @return a list of Import Queue DTO objects
     */
    List<ImportQueueDto> getRecentItemsForAdmin(Date cutoffDate);

    /**
     * Return a list of Import Queue DTO objects for the
     * administrator's view of the no data items.
     * @param cutoffDate the cut off date
     * @return a list of Import Queue DTO objects
     */
    List<ImportQueueDto> getRecentNoDataItemsForAdmin(Date cutoffDate);

    /**
     * Get the Import Queue DTO object for the specified id.
     * @param importQueueId the id
     * @return an Import Queue DTO object
     */
    ImportQueueDto getImportQueueById(Integer importQueueId);

    /**
     * Return null if nothing in the queue, otherwise return the next queued item.
     * @return the next queued item, null otherwise
     */
    ImportQueueDto getNextQueuedItem();

    /**
     * Get a limited list of the recent dataset names that the
     * given user has used.
     * @param username the current user's id
     * @return a list of dataset names
     */
    List<String> getRecentDatasetNames(String username);

    /**
     * Get a limited list of the recent dataset descriptions that the
     * given user has used.
     * @param username the current user's id
     * @return a list of dataset descriptions
     */
    List<String> getRecentDescriptions(String username);

    /**
     * Returns a Dataset given a name.
     * @param name name of Dataset
     * @return list of dataset items
     */
    List<ImportQueueItem> find(String name);

    /**
     * Get the max queue order for new import queue items.
     * @return the max queue order found in the database
     */
    Integer getMaxQueueOrder();

    /**
     * Find a list of items which are in the queue waiting to be loaded,
     * ie, status in (queued, passed, issues).
     * @return a list of ImportQueueItems
     */
    List<ImportQueueItem> findItemsInQueue();

    /**
     * Find the item with the given queue order.
     * @param queueOrder the given queue order
     * @return an ImportQueueItem if order exists, null otherwise
     */
    ImportQueueItem findByQueueOrder(Integer queueOrder);

    /**
     * Find the item with the given dataset item.
     * @param datasetItem the given dataset item
     * @return an ImportQueueItem if dataset attached, null otherwise
     */
    ImportQueueItem findByDataset(DatasetItem datasetItem);

    /**
     * Find the item with the given discourse item.
     * @param discourseItem the given discourse item
     * @return an ImportQueueItem if discourse attached, null otherwise
     */
    ImportQueueItem findByDiscourse(DiscourseItem discourseItem);

    /**
     * Figure out if the given dataset is already attached to an
     * item in the import queue.
     * @param datasetId dataset id
     * @return true if it is already attached, false otherwise
     */
    boolean isDatasetAlreadyAttached(Integer datasetId);

    /**
     * Find a list of items by project and display_flag.
     * @param projectItem the project
     * @param displayFlag the display_flag
     * @return a list of ImportQueueItems
     */
    List<ImportQueueItem> findByProjectAndDisplayFlag(ProjectItem projectItem,
                                                      Boolean displayFlag);

    /**
     * Get count of items by project and display_flag.
     * @param projectItem the project
     * @param displayFlag the display_flag
     * @return a list of ImportQueueItems
     */
    Long getCountByProjectAndDisplayFlag(ProjectItem projectItem, Boolean displayFlag);

    /**
     * Return a list of pending import queue items which are over one day old.
     * @return list of import queue items
     */
    List<ImportQueueItem> findOldPending();

    /**
     * Return a list of IQ items which are pending for a given project.
     * @param projectItem the project
     * @return list of import queue items
     */
    List<ImportQueueItem> findPendingByProject(ProjectItem projectItem);

    /**
     *  Returns a list of datasets already created from a given sample.
     *  @param sampleId the sample id
     *  @return a list of datasets already created from a given sample
     */
    List<DatasetItem> findDatasetsFromSample(Integer sampleId);
}
