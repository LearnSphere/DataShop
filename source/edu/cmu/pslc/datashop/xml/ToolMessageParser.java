/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Interface for the various tool message parsers for each version of the
 * tool message DTD.
 *
 * @author Hui cheng
 * @version $Revision: 2104 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-10 17:20:01 -0500 (Tue, 10 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ToolMessageParser {
    /**
     * Get ToolMessage object from the XML data.
     * @return a ToolMessage object
     */
    ToolMessage getToolMessage();

}


