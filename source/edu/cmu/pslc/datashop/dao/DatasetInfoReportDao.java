/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;


import edu.cmu.pslc.datashop.dto.DatasetInfoReport;
import edu.cmu.pslc.datashop.item.DatasetItem;


/**
 * This DAO gets the information for the dataset info report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7541 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-23 11:52:32 -0400 (Fri, 23 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetInfoReportDao {

    /**
     * Returns the info needed for the dataset info report.
     * @param datasetItem the selected dataset item
     * @return an object that holds all we need to display dataset info
     */
    DatasetInfoReport getDatasetInfoReport(DatasetItem datasetItem);
}
