/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Interface for the various tutor message parsers for each version of the
 * tool message DTD.
 *
 * @author Hui cheng
 * @version $Revision: 2152 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-20 13:56:50 -0500 (Fri, 20 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface TutorMessageParser {
    /**
     * Get TutorMessage object from the XML data.
     * @return a TutorMessage object
     */
    TutorMessage getTutorMessage();

}


