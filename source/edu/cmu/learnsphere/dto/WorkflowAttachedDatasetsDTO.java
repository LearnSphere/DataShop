/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.dto;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.item.DatasetItem;
import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer attached datasets data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "datasets",
                properties = {"datasets" })
public class WorkflowAttachedDatasetsDTO extends DTO {
        
        /** attachedDatasets */
        private List<DatasetDTO> datasets;
    
        /** this WF's datasets. @param datasets this WF's datasets */
        public void setDatasets(List<DatasetDTO> attachedDatasets) { this.datasets = attachedDatasets; }

        /** this WF's datasets. @return the datasets */
        public List<DatasetDTO> getDatasets() {
                datasets = checkNull(datasets);
            return datasets;
        }

        /** Add dataset. @param DatasetDTO */
        public void addAttachedDataset(DatasetDTO dataset) { getDatasets().add(dataset); }
 
}
