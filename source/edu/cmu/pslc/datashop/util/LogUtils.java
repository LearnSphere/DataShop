/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import static edu.cmu.pslc.datashop.util.StringUtils.concatenate;

/**
 * Utility methods for logging functionality.
 * @author jimbokun
 * @version $Revision: 5560 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-07-01 16:40:35 -0400 (Wed, 01 Jul 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class LogUtils {
    /** Private constructor as this is a utility class. */
    private LogUtils() { }

    /**
     * Only log if debugging is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logDebug(Log logger, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(concatenate(args));
        }
    }

    /**
     * Only log if trace is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logTrace(Log logger, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(concatenate(args));
        }
    }

    /**
     * Only log if info is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logInfo(Log logger, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(concatenate(args));
        }
    }

    /**
     * Only log if debugging is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logDebug(Logger logger, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(concatenate(args));
        }
    }

    /**
     * Only log if trace is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logTrace(Logger logger, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(concatenate(args));
        }
    }

    /**
     * Only log if info is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logInfo(Logger logger, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(concatenate(args));
        }
    }

    /**
     * Only log if info is enabled.
     * @param logger the logger
     * @param args concatenate all arguments into the string to be logged
     */
    public static void logErr(Logger logger, Object... args) {
        logger.error(concatenate(args));
    }

    /**
     * Log warning messages.
     * @param logger the logger.
     * @param args what we wish to log.
     */
    public static void logWarn(Logger logger, Object... args) {
        logger.warn(concatenate(args));
    }
}
