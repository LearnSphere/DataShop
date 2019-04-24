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
 * Used to transfer options data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "options",
                properties = {"options" })
public class WorkflowOptionsDTO extends DTO {
        /** options */
        private List<WorkflowOptionDTO> options;
    
    
        /** this component's options. @param options this component's options */
        public void setOptions(List<WorkflowOptionDTO> options) { this.options = options; }

        /** this component's options. @return the options */
        public List<WorkflowOptionDTO> getOptions() {
                options = checkNull(options);
            return options;
        }

        /** Add option. @param WorkflowOptionDTO */
        public void addOption(WorkflowOptionDTO option) { getOptions().add(option); }
}
