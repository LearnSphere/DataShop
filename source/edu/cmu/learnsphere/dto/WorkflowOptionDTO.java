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
@DTO.Properties(root = "option",
                properties = { "name", "value" })
public class WorkflowOptionDTO extends DTO {
    /** the name */
    private String name;
    /** the value */
    private String value;
    

    /** the option name. @return the option name */
    public String getName() { return name; }

    /** the option name. @param option name */
    public void setName(String name) { this.name = name; }
    
    /** the option value. @return the option value */
    public String getValue() { return value; }

    /** the option value. @param option value */
    public void setValue(String value) { this.value = value; }

}
