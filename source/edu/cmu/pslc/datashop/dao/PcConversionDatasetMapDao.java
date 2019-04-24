/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapId;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * PcConversion/Dataset Map Data Access Object Interface.
 *
 * @author Cindy Tipper
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PcConversionDatasetMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    PcConversionDatasetMapItem get(PcConversionDatasetMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    PcConversionDatasetMapItem find(PcConversionDatasetMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List<PcConversionDatasetMapItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param pcConversionItem the given PcConversion item
     *  @return a list of items
     */
    List<PcConversionDatasetMapItem> findByPcConversion(PcConversionItem pcConversionItem);

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param datasetItem the given Dataset item
     *  @return a list of items
     */
    List<PcConversionDatasetMapItem> findByDataset(DatasetItem datasetItem);

    /**
     *  Returns whether or not the dataset has problem content mapping.
     *  @param datasetItem the given DATASET item
     *  @return whether or not the dataset has problem content mapping
     */
    boolean isDatasetMapped(DatasetItem datasetItem);

    /**
     *  Returns a list of datasets mapped to the given PcConversionItem.
     *  @param pcConversionItem the given PcConversionItem
     *  @return a list of datasets
     */
    List<DatasetItem> findDatasets(PcConversionItem pcConversionItem);

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param projectItem the given Project item
     *  @return a list of items
     */
    List<PcConversionDatasetMapItem> findByProject(ProjectItem projectItem);

    /**
     *  Determine number of datasets that have Problem Content, in specified project.
     *  @param projectItem the given Project item
     *  @return the count
     */
    Integer getNumDatasetsWithProblemContent(ProjectItem projectItem);
}
