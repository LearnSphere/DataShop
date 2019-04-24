/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An application specific runtime exception which keeps track of whether
 * the exception has been logged or not.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4509 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-02-25 14:59:23 -0500 (Mon, 25 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogException extends RuntimeException {
    /** Flag which keeps track of whether this exception has been logged. */
    private boolean logged = false;

    /**
     * Simple constructor with just the detail message.
     * @param message the detail message used by getMessage()
     */
    public LogException(String message) {
        super(message);
    }

    /**
     * Constructor which logs the exception immediately.
     * @param message exception message
     * @param logger where the output should go
     */
    public LogException(String message, Logger logger) {
        super(message);
        log(logger);
    }

    /**
     * Constructor which takes both the message and the throwable its wrapping.
     * @param message the detail message used by getMessage()
     * @param cause the exception being wrapped
     */
    public LogException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor which logs the exception immediately.
     * @param message the detail message used by getMessage()
     * @param cause the exception being wrapped
     * @param logger where the output should go
     */
    public LogException(String message, Throwable cause, Logger logger) {
        super(message, cause);
        log(logger);
    }

    /**
     * Simple constructor with just the throwable being wrapped.
     * @param cause the exception being wrapped
     */
    public LogException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the logged flag.
     * @return flag indicating whether this exception has been logged
     */
    public boolean isLogged() {
        return logged;
    }

    /**
     * Method to help log the message properly at a SEVERE level.
     * @param logger where the output should go
     */
    public void log(Logger logger) {
        log(logger, Level.ERROR);
    }

    /**
     * Method to help log the message properly at a given level.
     * @param logger where the output should go
     * @param level allows the caller to indicate the level
     */
    public void log(Logger logger, Level level) {
        //Don't log me twice! Let alone a trillion times.
        if (logged) {
            return;
        }
        logged = true;

        Throwable cause = getCause();
        if (cause != null) {
            logger.log(level, getMessage(), cause);
        } else {
            logger.log(level, getMessage(), this);
        }
    } // end log method

    /**
     * Method to print error message in HTML format.
     * @return the exception with HTML tags
     */
    public String toHtmlString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<BR><BR>");
        buffer.append("<STRONG><Font color=red>");
        buffer.append("LogException occurred. " + getMessage());
        buffer.append("</STRONG>");
        buffer.append("<BR><BR>");

        return buffer.toString();

    } // end toHtmlString

} // end class LogException
