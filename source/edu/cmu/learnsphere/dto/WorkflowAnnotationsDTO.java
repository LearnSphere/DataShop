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
 * Used to transfer annotation data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "annotations",
                properties = {"annotations" })
public class WorkflowAnnotationsDTO extends DTO {
        /** annotations */
        private List<WorkflowAnnotationDTO> annotations;
    
    
    /** this WF's annotations. @param annotations this WF's annotations */
    public void setAnnotations(List<WorkflowAnnotationDTO> annotations) { this.annotations = annotations; }

    /** this WF's annotations. @return the annotations */
    public List<WorkflowAnnotationDTO> getAnnotations() {
        annotations = checkNull(annotations);
        return annotations;
    }

    /** Add annotation. @param WorkflowAnnotationDTO */
    public void addAnnotation(WorkflowAnnotationDTO annotation) { getAnnotations().add(annotation); }
    

}
