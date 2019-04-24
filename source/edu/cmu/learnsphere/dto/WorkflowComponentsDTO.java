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
 * Used to transfer component data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "components",
                properties = {"components" })
public class WorkflowComponentsDTO extends DTO {
        /** components */
        private List<WorkflowComponentDTO> components;
        
        
        /** components */
        /** this WF's components. @param components this WF's components */
        public void setComponents(List<WorkflowComponentDTO> components) { this.components = components; }

        /** this WF's components. @return the components */
        public List<WorkflowComponentDTO> getComponents() {
                components = checkNull(components);
            return components;
        }

        /** Add component. @param WorkflowComponentDTO */
        public void addComponent(WorkflowComponentDTO component) { getComponents().add(component); }
    
}