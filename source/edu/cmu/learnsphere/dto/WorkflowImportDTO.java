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
 * Used to transfer option data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "import",
                properties = { "id", "name", "title", "type", "fileSelectionType", "datasetLink", "datasetName"})
public class WorkflowImportDTO extends DTO {
    /** the  fileId*/
    private String id;
    /** the fileName */
    private String name;
    /** the title */
    private String title;
    /** the fileType */
    private String type;
    /** the fileTypeSelection */
    private String fileSelectionType;
    /** the datasetLink */
    private String datasetLink;
    /** the datasetName */
    private String datasetName;

    /** the fileId. @return the fileId */
    public String getId() { return id; }

    /** the fileId. @param  fileId*/
    public void setId(String fileId) { this.id = fileId; }
    
    /** the fileName. @return the fileName */
    public String getName() { return name; }

    /** the fileName. @param fileName */
    public void setName(String name) { this.name = name; }
    
    /** the title. @return the title */
    public String getTitle() { return title; }

    /** the . @param  */
    public void setTitle(String title) { this.title = title; }
    
    /** the fileType. @return the fileType */
    public String getType() { return type; }

    /** the fileType. @param fileType */
    public void setType(String type) { this.type = type; }
    
    
    /** the fileSelectionType. @return the fileSelectionType */
    public String getFileSelectionType() { return fileSelectionType; }

    /** the fileSelectionType. @param fileSelectionType */
    public void setFileSelectionType(String fileSelectionType) { this.fileSelectionType = fileSelectionType; }
    
    /** the datasetLink. @return the datasetLink */
    public String getDatasetLink() { return datasetLink; }

    /** the datasetLink. @param  datasetLink*/
    public void setDatasetLink(String datasetLink) { this.datasetLink = datasetLink; }
    
    /** the datasetName. @return the datasetName */
    public String getdatasetName() { return datasetName; }

    /** the datasetName. @param datasetName */
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
    
   

}
