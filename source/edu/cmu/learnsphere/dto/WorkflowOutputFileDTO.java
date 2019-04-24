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
@DTO.Properties(root = "file",
                properties = {"type", "path", "name", "index", "label" })
public class WorkflowOutputFileDTO extends DTO {
        /** type */
        private String type;
        /** type */
        private String path;
        /** type */
        private String name;
        /** type */
        private String index;
        /** type */
        private String label;
    
        /** the  type. @return the type */
        public String getType() { return type; }

        /** the type. @param type the type */
        public void setType(String type) { this.type = type; }
        
        /** the path name. @return the path */
        public String getPath() { return path; }

        /** the path. @param  the path */
        public void setPath(String path) { this.path = path; }
        
        /** the  name. @return the name */
        public String getName() { return name; }

        /** the name. @param name the name */
        public void setName(String name) { this.name = name; }
        
        /** the  index. @return the index */
        public String getIndex() { return index; }

        /** the index. @param index the index */
        public void setIndex(String index) { this.index = index; }
        
        /** the  label. @return the label */
        public String getLabel() { return label; }

        /** the label. @param label the label */
        public void setLabel(String label) { this.label = label; }
}
