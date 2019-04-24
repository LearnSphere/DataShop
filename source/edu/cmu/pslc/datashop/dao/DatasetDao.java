/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DomainItem;
import edu.cmu.pslc.datashop.item.LearnlabItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Dataset Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12341 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-13 14:05:35 -0400 (Wed, 13 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetDao extends AbstractDao {

    /**
     * Standard get for a Dataset item by id.
     * @param id the id of the desired Dataset item
     * @return the matching DatasetItem or null if none found
     */
    DatasetItem get(Integer id);

    /**
     * Standard find for a Dataset item by id.
     * @param id id of the object to find
     * @return DatasetItem
     */
    DatasetItem find(Integer id);

    /**
     * Standard "find all" for Dataset items.
     * @return a List of objects
     */
    List<DatasetItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of datasets marked as deleted.
     * @return List of all datasets marked as deleted
     */
    List<DatasetItem> findDeletedDatasets();

    /**
     * Returns a list of datasets not marked as deleted.
     * @return List of all datasets not marked as deleted
     */
    List<DatasetItem> findUndeletedDatasets();

    /**
     * Returns a Dataset given a name.
     * @param name name of Dataset
     * @return list of dataset items
     */
    List<DatasetItem> find(String name);

    /**
     * Returns a collection of datasets with given project, which can be null.
     * @param projectItem the project item
     * @return collection of datasets
     */
    Collection<DatasetItem> findByProject(ProjectItem projectItem,
            boolean isProjectOrDatashopAdminFlag);

    /**
     * Automatically sets the start and end times based on transaction data.
     * @param datasetItem the dataset to auto set the times for.
     * @return The dataset item with the updated times, null if a failure occurred.
     */
     DatasetItem autoSetDates(DatasetItem datasetItem);

     /**
      * Find the dataset with the lowest dataset_id and return it.
      * @return a DatasetItem with the lowest database identifier.
      */
     DatasetItem findDatasetWithMinId();

     /**
      * Find the list of datasets where learnlab is null and not marked as junk.
      * @return a list of DatasetItem objects where learnlab is null
      */
     List<DatasetItem> findDatasetsWhereLearnLabIsNull();

     /**
      * Returns DatasetItem given a learnlab.
      * @param learnlab LearnlabItem
      * @return a collection of DatasetItem
      */
     Collection<DatasetItem> findByLearnlab(LearnlabItem learnlab);

     /**
      * Returns DatasetItem given a domain.
      * @param domain DomainItem
      * @return a collection of DatasetItem
      */
     Collection<DatasetItem> findByDomain(DomainItem domain);

     /**
      * Gets the import queue Id of the item associated with the provided dataset.
      * @param datasetItem the dataset item
      * @return the import queue Id of the item associated with the provided dataset
      */
     Integer getImportQueueId(DatasetItem datasetItem);

    /**
     * Returns a list of datasets for the sitemap.xml: released, not junk
     * and not deleted.
     * @return List of all non-junk datasets marked as released and not deleted
     */
    List<DatasetItem> findDatasetsForSiteMap();

    /**
     * Count the number of papers for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of papers attached to the given dataset
     */
    Long countPapers(DatasetItem datasetItem);

    /**
     * Count the number of external analyses for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of external analyses attached to the given dataset
     */
    Long countExternalAnalyses(DatasetItem datasetItem);

    /**
     * Count the number of attached files for the given dataset.
     * @param datasetItem the given dataset
     * @return the number of attached files for the given dataset
     */
    Long countFiles(DatasetItem datasetItem);
}
