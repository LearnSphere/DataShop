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
 * Used to transfer workflow data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "workflow",
                properties = { "id", "name", "owner", "global", "dataAccess", "description", "lastUpdated",
                               "state", "attachedDatasets", "annotations", "components" })
public class WorkflowDTO extends DTO {
    /** the id */
    private Long id;
    /** the workflow name */
    private String name;
    /** the workflow owner */
    private String owner;
    /** the workflow data_access */
    private String dataAccess;
    /** the description */
    private String description;
    /** the state */
    private String state;
    

    /** the date on which last_updated */
    private Date lastUpdated;

    /** whether the workflow is global */
    private String global;

    /** datasets */
    private WorkflowAttachedDatasetsDTO attachedDatasets;
    /** annotations */
    private WorkflowAnnotationsDTO annotations;
    /** components */
    private WorkflowComponentsDTO components;

    /** the id. @return the id */
    public Long getId() { return id; }

    /** the id. @param id the id */
    public void setId(Long id) { this.id = id; }

    /** the wf name. @return the wf name */
    public String getName() { return name; }

    /** the wf name. @param name the wf name */
    public void setName(String name) { this.name = name; }
    
    /** the wf owner. @return the wf owner */
    public String getOwner() { return owner; }
    
    /** the wf owner. @param owner the wf owner */
    public void setOwner(String owner) { this.owner = owner; }

    /** the wf data access. @param dataAccess the wf dataAccess */
    public void setDataAccess(String dataAccess) { this.dataAccess = dataAccess; }
    
    /** the wf data access. @return the wf data access */
    public String getDataAccess() { return dataAccess; }

    /** the description. @return the description */
    public String getDescription() { return description; }

    /** the description. @param description the description */
    public void setDescription(String description) { this.description = description; }

    /** the state. @return the state */
    public String getState() { return state; }

    /** the state. @param state the state */
    public void setState(String state) { this.state = state; }
    
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

    /** whether the wf is global. @return whether the wf is global */
    public String isGlobal() { return global; }

    /** whether the wf is global. @param isGlobal whether the wf is global */
    public void setGlobal(boolean isGlobal) { 
            if (isGlobal)
                    this.global = "true"; 
            else
                    this.global = "false";
            }

    
    /** this WF's annotations. @param annotations this WF's annotations */
    public void setAnnotations(WorkflowAnnotationsDTO annotations) { this.annotations = annotations; }

    /** this WF's annotations. @return the annotations */
    public WorkflowAnnotationsDTO getAnnotations() {
        return annotations;
    }

    /** Add annotation. @param WorkflowAnnotationDTO */
    public void addAnnotation(WorkflowAnnotationDTO annotation) { annotations.addAnnotation(annotation); }
   
    /** this WF's attachedDatasets. @param attachedDatasets this WF's attachedDatasets */
    public void setAttachedDatasets(WorkflowAttachedDatasetsDTO attachedDatasets) { this.attachedDatasets = attachedDatasets; }

    /** this WF's attachedDatasets. @return the attachedDatasets */
    public WorkflowAttachedDatasetsDTO getAttachedDatasets() {
        return attachedDatasets;
    }

    /** Add attachedDataset. @param WorkflowAnnotationDTO */
    public void addAttachedDataset(DatasetDTO dataset) { attachedDatasets.addAttachedDataset(dataset); }
   
    
    /** components */
    /** this WF's components. @param components this WF's components */
    public void setComponents(WorkflowComponentsDTO components) { this.components = components; }

    /** this WF's components. @return the components */
    public WorkflowComponentsDTO getComponents() {
            return components;
    }

    /** Add component. @param WorkflowComponentDTO */
    public void addComponent(WorkflowComponentDTO component) { components.addComponent(component); }
}
