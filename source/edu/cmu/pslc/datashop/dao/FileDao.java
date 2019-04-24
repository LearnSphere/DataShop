/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;

/**
 * File Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface FileDao extends AbstractDao {

    /**
     * Standard get for a FileItem by id.
     * @param id The id of the FileItem.
     * @return the matching FileItem or null if none found
     */
    FileItem get(Integer id);

    /**
     * Standard find for an FileItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired FileItem.
     * @return the matching FileItem.
     */
    FileItem find(Integer id);

    /**
     * Standard "find all" for FileItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get a list of file items with the given file name and path.
     * @param path the path to the file
     * @param name the name of the file
     * @return a List of matching FileItems, though only expect one item in list
     */
    List find(String path, String name);

    /**
     * Returns a list of distinct file paths given a dataset.
     * @param datasetItem the datasetItem
     * @return a list of distinct file paths
     */
    List<String> getDistinctFilePaths(final DatasetItem datasetItem);

    /**
     * Get a list of file ids given the dataset item.
     * @param datasetItem the dataset item
     * @return the file ids
     */
    List<Integer> find(DatasetItem datasetItem);

    /**
     * Get a dataset id associated with given file item.
     * @param fileItem the file item
     * @return the dataset id associated with given file item
     */
	Integer findDatasetId(FileItem dsFileItem);

}
