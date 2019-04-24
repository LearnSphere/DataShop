/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import edu.cmu.pslc.datashop.dto.ProjectInfoReport;
import edu.cmu.pslc.datashop.item.DatasetItem;

/**
 * This DAO gets the information for the project info report.
 *
 * @author Shanwen Yu
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectInfoReportDao {

    /**
     * Returns the info needed for the project info report.
     * @param datasetItem the selected dataset item
     * @return an object that holds all we need to display project info
     */
    ProjectInfoReport getProjectInfoReport(DatasetItem datasetItem);

    /**
     * Returns the info needed for the project info report.
     * @param projectId the selected project id
     * @return an object that holds all we need to display project info
     */
    ProjectInfoReport getProjectInfoReport(Integer projectId);
}
