/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;

/**
 * DatasetLevel Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6308 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-09-15 14:43:21 -0400 (Wed, 15 Sep 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetLevelDao extends AbstractDao<DatasetLevelItem> {

    /**
     * Standard get for a DatasetLevelItem by id.
     * @param id The id of the DatasetLevelItem.
     * @return the matching DatasetLevelItem or null if none found
     */
    DatasetLevelItem get(Integer id);

    /**
     * Standard find for an DatasetLevelItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelItem.
     * @return the matching DatasetLevelItem.
     */
    DatasetLevelItem find(Integer id);

    /**
     * Standard "find all" for DatasetLevelItems.
     * @return a List of objects
     */
    List<DatasetLevelItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns DatasetLevel(s) given a name.
     * @param name name of DatasetLevel
     * @return Collection
     */
    Collection<DatasetLevelItem> find(String name);

    /**
     * Returns a collection of all children for this dataset level. <br />
     * @param parent the dataset level to get children of
     * @return a collection of all children that fall below the parent. <br />
     * <strong>Note:</strong> this collection will not include the parent, but will
     * be recursive so it will bottom out on the lowest most child(ren).
     */
    Collection<DatasetLevelItem> getChildren(DatasetLevelItem parent);

    /**
     * Gets a list of dataset levels in the dataset that match all or a portion of the
     * title parameter.
     * @param toMatch A string to match a title too.
     * @param dataset the dataset item to find levels in.
     * @param matchAny boolean value indicating whether to only look for levels that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching levels items sorted by title.
     */
    List<DatasetLevelItem> findMatchingByTitle(String toMatch, DatasetItem dataset,
            boolean matchAny);

    /**
     * Gets a list of dataset levels in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find levels in.
     * @param matchAny boolean value indicating whether to only look for levels that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching levels items sorted by name.
     */
    List<DatasetLevelItem> findMatchingByName(String toMatch, DatasetItem dataset,
            boolean matchAny);

    /**
     * Gets a list of dataset levels in the dataset that match all of the title and name parameters.
     * @param titleMatch the title string to match
     * @param nameMatch the name string to match
     * @param dataset the dataset item to find levels in
     * @return a list of matching dataset levels
     */
    List<DatasetLevelItem> findMatchingByTitleAndName(String titleMatch, String nameMatch,
            DatasetItem dataset);

    /**
     * Returns the number of dataset_levels for a give dataset id.  Used in export.
     * @param id the dataset id
     * @return an Long representing the count of dataset_levels for a given dataset.
     */
    Long getDatasetLevelCount(Integer id);

    /**
     * Returns an ArrayList containing all level_titles for a particular dataset.
     * @param id the dataset id
     * @return an ArrayList containing all level_titles for a given dataset_id.
     */
    List<String> getDatasetLevelTitles(Integer id);

    /**
     * Find all dataset level Items for a given dataset level.
     * @param dataset the dataset to get all levels for
     * @return a List of matching dataset levels.
     */
    List<DatasetLevelItem> find(DatasetItem dataset);

    /**
     * Map from dataset level hierarchies to dataset level id's.
     * @param dataset fetch the dataset levels for this dataset
     * @return map from dataset level hierarchies to dataset level id's
     */
    SortedMap<String, Integer> getLevelHierarchiesForDataset(DatasetItem dataset);
}
