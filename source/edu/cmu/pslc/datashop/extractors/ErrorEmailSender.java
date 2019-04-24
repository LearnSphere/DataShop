/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

/**
 * Interface for sending email.
 * @author Jim Rankin
 * @version $Revision: 6315 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-09-29 09:03:40 -0400 (Wed, 29 Sep 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ErrorEmailSender {

    /**
     * Returns the sendEmailFlag.
     * @return the sendEmailFlag
     */
    boolean isSendEmail();

    /**
     * Returns the emailAddress.
     * @return the emailAddress
     */
    String getEmailAddress();
}