/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;

import edu.cmu.pslc.datashop.servlet.problemcontent.MappedContentDto;


/**
 * PcConversion Data Access Object Interface.
 *
 * @author
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PcConversionDao extends AbstractDao {

    /**
     * Standard get for a PcConversionItem by id.
     * @param id The id of the PcConversionItem.
     * @return the matching PcConversionItem or null if none found
     */
    PcConversionItem get(Long id);

    /**
     * Standard find for an PcConversionItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired PcConversionItem.
     * @return the matching PcConversionItem.
     */
    PcConversionItem find(Long id);

    /**
     * Standard "find all" for PcConversionItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Get PcConversionItem matching specified conversion tool, conversion date
     * and content version. Null returned if match not found.
     * @param conversionTool the conversion tool
     * @param contentDate the content date
     * @param contentVersion the content version
     * @return the PCConversionItem
     */
    PcConversionItem getByToolDateAndVersion(String conversionTool, Date contentDate,
            String contentVersion);

    /**
     * Get a list of content versions by conversion tool, filtered. Do not
     * include conversions which are already in the pc_conversion_dataset_map
     * for the specified dataset.
     * @param conversionTool the name of the conversion tool
     * @param dataset the specified dataset
     * @param searchBy the string to filter by
     * @return list of PcConversionItems
     */
    List<PcConversionItem> getContentVersionsByTool(String conversionTool,
                                                    DatasetItem dataset,
                                                    String searchBy);

    /**
     * Get a list of mapped content versions by dataset.
     * @param dataset the dataset to match
     * @return list of MappedContentDto objects
     */
    List<MappedContentDto> getMappedContent(DatasetItem dataset);

    /**
     * Gets the mapped items by dataset.
     * @param dataset the dataset
     * @return the mapped PcConversionItems
     */
    List<PcConversionItem> getMappedByDataset(DatasetItem dataset);

    /**
     * Get a list of content versions, narrowed by one or more of the following:
     * conversion tool, content version, dataset name and whether or not
     * the content is mapped.
     * @param conversionTool the name of the conversion tool
     * @param contentVersionSearchBy the string to filter by
     * @param datasetSearchBy the string to filter by
     * @param mapped if content is mapped to one or more datasets
     * @return list of PcConversionItems
     */
    List<PcConversionItem> getContentVersionsFiltered(String conversionTool,
                                                      String contentVersionSearchBy,
                                                      String datasetSearchBy,
                                                      Boolean mapped);
}
