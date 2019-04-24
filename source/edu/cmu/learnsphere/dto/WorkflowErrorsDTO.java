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
 * Used to transfer errors data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "errors",
                properties = {"errors" })
public class WorkflowErrorsDTO extends DTO {
        /** errors */
        private List<String> errors;
        
        /** this component's errors. @param errors this component's errors */
        public void setErrors(List<String> errors) { this.errors = errors; }

        /** this component's errors. @return the errors */
        public List<String> getErrors() {
                errors = checkNull(errors);
            return errors;
        }

        /** Add error. @param WorkflowErrorDTO */
        public void addError(String error) { getErrors().add(error); }
    

}
