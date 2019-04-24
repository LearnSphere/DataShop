/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

/**
 * Convenient class for Checking Dataset Authorization (the default constructor
 * sets AUTH_LEVEL_AUTHORIZED_FOR_DS flag).
 * @author Young Suk Ahn
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CheckDsAuthCommand extends CheckAuthCommand {
    /**
     * Check dataset authorization command.
     */
    public CheckDsAuthCommand() {
        super(CheckAuthCommand.AUTH_LEVEL_AUTHORIZED_FOR_DS);
    }
}