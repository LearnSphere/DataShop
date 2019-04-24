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
 * Used to transfer outputFile data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "outputFiles",
                properties = {"outputFiles" })
public class WorkflowOutputFilesDTO extends DTO {
        /** outputs */
        private List<WorkflowOutputFileDTO> outputFiles;
    
    
        /** outputs */
        /** this component's outputs. @param outputs this component's outputs */
        public void setOutputFiles(List<WorkflowOutputFileDTO> outputFiles) { this.outputFiles = outputFiles; }

        /** this component's outputs. @return the outputs */
        public List<WorkflowOutputFileDTO> getOutputFiles() {
                outputFiles = checkNull(outputFiles);
            return outputFiles;
        }

        /** Add output. @param WorkflowOutputFileDTO */
        public void addOutputFile(WorkflowOutputFileDTO outputFile) { getOutputFiles().add(outputFile); }
}
