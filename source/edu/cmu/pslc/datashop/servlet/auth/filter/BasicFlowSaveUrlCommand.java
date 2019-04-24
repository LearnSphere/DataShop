/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

/**
 * Convenient class for basic flow that saves URL (the default constructor
 * sets saveVisitUrl flag to true).
 *
 * @author Young Suk Ahn
 * @version $Revision: 7927 $
 * <BR>Last modified by: $Author: ysahn $
 * <BR>Last modified on: $Date: 2012-09-05 12:32:06 -0400 (Wed, 05 Sep 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class BasicFlowSaveUrlCommand extends BasicFlowCommand {

    /**
     * Default constructor.
     */
    public BasicFlowSaveUrlCommand() {
        super(true);
    }
}
