/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

/**
 * Convenient class for Checking Admin Authorization (the default constructor
 * sets AUTH_LEVEL_ADMIN flag).
 * @author Young Suk Ahn
 * @version $Revision: 7927 $
 * <BR>Last modified by: $Author: ysahn $
 * <BR>Last modified on: $Date: 2012-09-05 12:32:06 -0400 (Wed, 05 Sep 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CheckAdminCommand extends CheckAuthCommand {

    /**
     * Default constructor.
     */
    public CheckAdminCommand() {
        super(CheckAuthCommand.AUTH_LEVEL_ADMIN);
    }
}