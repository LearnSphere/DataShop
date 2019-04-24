/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import org.apache.log4j.Logger;

/**
 * Central location for the current release number and release date.
 *
 * @author Alida Skogsholm
 * @version $Revision: 16038 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-04-21 21:52:36 -0400 (Sun, 21 Apr 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class VersionInformation {

    /** Release number. */
    public static final String RELEASE_NUMBER = "10.4.5";
    /** Release date. */
    public static final String RELEASE_DATE = "April 21, 2019";

    /**
     * Returns the release number.
     * @return the release number
     */
    public static String getReleaseNumber() {
        return RELEASE_NUMBER;
    }

    /**
     * Returns the release date.
     * @return the release date
     */
    public static String getReleaseDate() {
        return RELEASE_DATE;
    }

    /**
     * Returns the release version and the date in one string.
     * @return the release string
     */
    public static String getReleaseString() {
        return "Version " + RELEASE_NUMBER + "  " + RELEASE_DATE;
    }

    /**
     * Default Constructor - because this class is only applied by static internal classes
     * the any attempts to instantiate will return an UnsupportedOperationException.
     */
    protected VersionInformation() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    /**
     * Used just to get the version information.
     * USAGE: java -classpath ... VersionInformation
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("VersionInformation.main");
        try {
            System.out.println(getReleaseString());

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        }
    }
} // end class VersionInformation
