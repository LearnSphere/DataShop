/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.moocdb.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.learnsphere.analysis.moocdb.item.FeatureExtractionItem;
import edu.cmu.pslc.learnsphere.analysis.moocdb.item.MOOCdbItem;

/**
 * MOOCdb Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 14073 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2017-05-15 14:48:12 -0400 (Mon, 15 May 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface FeatureExtractionDao extends edu.cmu.pslc.datashop.dao.AbstractDao {

    /**
     * Standard get for a FeatureExtractionItem by id.
     * @param id the id of the desired FeatureExtractionItem
     * @return the matching FeatureExtractionItem or null if none found
     */
     FeatureExtractionItem get(Long id);

    /**
     * Standard find for a FeatureExtractionItem by id.
     * @param id id of the object to find
     * @return FeatureExtractionItem
     */
     FeatureExtractionItem find(Long id);

    /**
     * Standard "find all" for FeatureExtractionItem.
     * @return a List of objects
     */
    List<FeatureExtractionItem> findAll();

    //
    // Non-standard methods begin.
    //
    Map<Integer, String> getAllFeatures(String MOOCdbName);
    FeatureExtractionItem findAFeatureExtraction(String MOOCdbName, Date startDate, int numberWeeks, String featuresToExtract);
    //can't use default save and update because database changes
    void saveOrUpdateFeatureExtractionItem(String MOOCdbName, FeatureExtractionItem featureExtractionItem) throws Exception;
}
