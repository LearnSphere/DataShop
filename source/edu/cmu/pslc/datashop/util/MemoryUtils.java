/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class provides utility methods for watching memory usage.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5455 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2009-05-11 13:17:53 -0400 (Mon, 11 May 2009) $
 * <!-- $KeyWordsOff: $ -->
 *
 */
public final class MemoryUtils {
    /**
     * Private constructor as this is a utility class.
     */
    private MemoryUtils() { }

    /** Constant. */
    private static final int BYTES = 1024;

    /**
     * Log (debug) the memory usage at this point with the 'prefix' string given.
     * @param logger the debug logger
     * @param suffix the suffix to the debug output
     */
    public static void logMemoryUsage(Logger logger, String suffix) {
        logMemoryUsage(Level.INFO, true, logger, suffix);

    }

    /**
     * Log (debug) the memory usage at this point with the 'prefix' string given.
     * @param showMemoryUsageFlag flag indicating whether to log memory usage or not
     * @param logger the debug logger
     * @param suffix the suffix to the debug output
     */
    public static void logMemoryUsage(boolean showMemoryUsageFlag,
            Logger logger, String suffix) {
        logMemoryUsage(Level.INFO, showMemoryUsageFlag, logger, suffix);

    }

    /**
     * Log (debug) the memory usage at this point with the 'prefix' string given.
     * @param loggingLevel  the log4j logging level: TRACE, DEBUG, INFO, WARN, ERROR, FATAL
     * @param showMemoryUsageFlag flag indicating whether to log memory usage or not
     * @param logger the debug logger
     * @param suffix the suffix to the debug output
     */
    public static void logMemoryUsage(Level loggingLevel, boolean showMemoryUsageFlag,
            Logger logger, String suffix) {
        if (showMemoryUsageFlag) {
           Runtime runtime = Runtime.getRuntime();
           long maxMemory = runtime.maxMemory();
           long allocatedMemory = runtime.totalMemory();
           long freeMemory = runtime.freeMemory();
           long usedMemory = allocatedMemory - freeMemory;

           logger.log(loggingLevel,
                  "MEMORY used: " + usedMemory / BYTES
                  + ", free: " + freeMemory / BYTES
                  + ", alloc: " + allocatedMemory / BYTES
                  + ", max: " + maxMemory / BYTES
                  + ", total free: " + (freeMemory + (maxMemory - allocatedMemory)) / BYTES
                  + " - " + suffix);
        }
    }

    /**
     * Get the amount of memory used.
     * @return the amount of memory used in kilobytes
     */
    public static long getMemoryUsed() {
       Runtime runtime = Runtime.getRuntime();
       long allocatedMemory = runtime.totalMemory();
       long freeMemory = runtime.freeMemory();
       return (allocatedMemory - freeMemory) / BYTES;
    }

} // end MemoryUtils class
