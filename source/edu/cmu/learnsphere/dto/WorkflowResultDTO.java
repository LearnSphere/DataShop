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
 * Used to component output data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "result",
                properties = { "elapsedSeconds", "outputFiles" })
public class WorkflowResultDTO extends DTO {
    /** the  elapsedSeconds*/
    private String elapsedSeconds;
    
    /** the errors*/
    private WorkflowOutputFilesDTO outputFiles;
    

    /** the elapsedSeconds. @return theelapsedSeconds */
    public String getElapsedSeconds() { return elapsedSeconds; }

    /** the elapsedSeconds. @param elapsedSeconds */
    public void setElapsedSeconds(String elapsedSeconds) { this.elapsedSeconds = elapsedSeconds; }
    
    
   
    /** this WF's files. @param files this WF's files */
    public void setOutputFiles(WorkflowOutputFilesDTO outputFiles) { this.outputFiles = outputFiles; }

    /** this WF's files. @return the files */
    public WorkflowOutputFilesDTO getOutputFiles() {
        return outputFiles;
    }

    /** Add file. @param WorkflowErrorDTO */
    public void addOutputFile(WorkflowOutputFileDTO file) { outputFiles.addOutputFile(file); }
}
