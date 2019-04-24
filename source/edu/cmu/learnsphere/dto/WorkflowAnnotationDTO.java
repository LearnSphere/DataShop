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
@DTO.Properties(root = "annotation",
                properties = {"id", "annotationText", "lastUpdated" })
public class WorkflowAnnotationDTO extends DTO {
        /** the id */
        private Long id;
    /** the annotation text */
    private String annotationText;
    /** the date on which last_updated */
    private Date lastUpdated;
    
    /** the id. @return the id */
    public Long getId() { return id; }

    /** the id. @param id the id */
    public void setId(Long id) { this.id = id; }

    /** the annotation text name. @return the annotation text */
    public String getAnnotationText() { return annotationText; }

    /** the wf annotationText. @param annotationText the wf annotationText */
    public void setAnnotationText(String annotationText) { this.annotationText = annotationText; }
    
    
    /**
     * the date on which last updated.
     * @return the startDate the date on which collection began for this dataset
     */
    public Date getLastUpdated() { return lastUpdated; }

    /**
     * the date on which last updated
     * @param lastUpdated the date on whichlast updated
     */
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

}
