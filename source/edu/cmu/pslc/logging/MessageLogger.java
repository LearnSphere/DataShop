/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.Date;

/**
 * Interface for the 3 types of logger.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6000 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-02-26 15:18:28 -0500 (Fri, 26 Feb 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MessageLogger {

    /**
     * Logs a message.
     * @param message the message to log
     * @return true if log succeeded, false otherwise
     */
    boolean log(Message message);

    /**
     * Logs a message with an authentication token.
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @param message the message to log
     * @return true if log succeeded, false otherwise
     */
    boolean log(String authToken, Message message);

    /**
     * Logs a message with a new time stamp.
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    boolean log(Message message, Date timeStamp);

    /**
     * Logs a message with a new time stamp, and an authentication token.
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    boolean log(String authToken, Message message, Date timeStamp);

    /**
     * Closes the output stream; this is required.
     */
    void close();

}
