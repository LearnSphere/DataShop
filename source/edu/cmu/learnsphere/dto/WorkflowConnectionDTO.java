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
 * Used to transfer connection data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "connection",
                properties = { "to", "from", "index", "frindex", "tindex" })
public class WorkflowConnectionDTO extends DTO {
    /** the to */
    private String to;
    /** the from */
    private String from;
    /** the index */
    private String index;
    /** the tindex */
    private String tindex;
    /** the findex */
    private String frindex;

    /** the to. @return to */
    public String getTo() { return to; }

    /** the to. @param to */
    public void setTo(String to) { this.to = to; }
    
    /** the from. @return from */
    public String getFrom() { return from; }

    /** the from. @param from */
    public void setFrom(String from) { this.from = from; }
    
    /** the index. @return index */
    public String getIndex() { return index; }

    /** the index. @param index */
    public void setIndex(String index) { this.index = index; }
    
    /** the tindex. @return tindex */
    public String getTindex() { return tindex; }

    /** the tindex. @param tindex */
    public void setTindex(String tindex) { this.tindex = tindex; }
    
    /** the frindex. @return frindex */
    public String getFrindex() { return frindex; }

    /** the frindex. @param frindex */
    public void setFrindex(String frindex) { this.frindex = frindex; }
    
}
