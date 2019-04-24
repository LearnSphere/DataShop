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
 * Used to transfer workflow component data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "component",
                properties = { "id", "idHuman", "name", "type", "state", "connections", "importFile", "options", "result", "errors"})
public class WorkflowComponentDTO extends DTO {
    /** the id */
    private String id;
    /** the workflow id_human */
    private String idHuman;
    /** the workflow name */
    private String name;
    /** the workflow type */
    private String type;
    /** the workflow state in wf_component_instance table */
    private String state;

    /** connections */
    private WorkflowConnectionsDTO connections;
    /** imports */
    private WorkflowImportDTO importFile;
    /** options */
    private WorkflowOptionsDTO options;
    /** result */
    private WorkflowResultDTO result;
    /** the errors*/
    private WorkflowErrorsDTO errors;
    

    /** the id. @return the id */
    public String getId() { return id; }

    /** the id. @param id the id */
    public void setId(String id) { this.id = id; }

    /** the component id_human. @return the component idHuman */
    public String getIdHuman() { return idHuman; }

    /** the component id_human. @param idHuman the component id_human */
    public void setIdHuman(String idHuman) { this.idHuman = idHuman; }
    
    /** the component name. @return the component name */
    public String getName() { return name; }
    
    /** the component name. @param name the component name */
    public void setName(String name) { this.name = name; }

    /** the component type. @param type the component type */
    public void setType(String type) { this.type = type; }
    
    /** the component type. @return the component type */
    public String getType() { return type; }

    /** the component state. @return the component state */
    public String getState() { return state; }

    /** the component state. @param state the component state */
    public void setState(String state) { this.state = state; }

    /** this component's connections. @param connections this component's connections */
    public void setConnections(WorkflowConnectionsDTO connections) { this.connections = connections; }

    /** this component's connections. @return the connections */
    public WorkflowConnectionsDTO getConnections() {
            return connections;
    }

    /** Add connection. @param WorkflowConnectionDTO */
    public void addConnection(WorkflowConnectionDTO connection) { connections.addConnection(connection); }
    
    /** this component's options. @param options this component's options */
    public void setOptions(WorkflowOptionsDTO options) { this.options = options; }

    /** this component's options. @return the options */
    public WorkflowOptionsDTO getOptions() {
        return options;
    }

    /** Add option. @param WorkflowOptionDTO */
    public void addOption(WorkflowOptionDTO option) { options.addOption(option); }
    
    /** outputs */
    /** this component's result. @param outputs this component's outputs */
    public void setResult(WorkflowResultDTO result) { this.result = result; }

    /** this component's result. @return the outputs */
    public WorkflowResultDTO getResult() {
        return result;
    }
    
    /** imports */
    /** this component's import. @param imports this component's imports */
    public void setImportFile(WorkflowImportDTO importFile) { this.importFile = importFile; }

    /** this component's import. @return the importFile */
    public WorkflowImportDTO getImportFile() {
        return importFile;
    }
    
    /** this WF's errors. @param errors this WF's errors */
    public void setErrors(WorkflowErrorsDTO errors) { this.errors = errors; }

    /** this WF's errors. @return the errors */
    public WorkflowErrorsDTO getErrors() {
        return errors;
    }

    /** Add error. @param WorkflowErrorDTO */
    public void addError(String error) { errors.addError(error); }

}
