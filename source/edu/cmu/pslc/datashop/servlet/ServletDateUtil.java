/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This utility class is useful to parse a date from the user interface and
 * to create a standard date string to be displayed to the user.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ServletDateUtil {

    /**
     * Private constructor as this is a utility class.
     */
    private ServletDateUtil() {
        super();
    }

    /** Format for the exported times. */
    private static final FastDateFormat TIME_FMT =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /** Constant for the format of Approval and Expiration dates. */
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    /** List of alternate date formats. */
    private static final List<SimpleDateFormat> DATE_FMT_LIST =
        Arrays.asList(new SimpleDateFormat("MM-dd-yyyy"),
                      new SimpleDateFormat("MMM-dd-yyyy"),
                      new SimpleDateFormat("MM/dd/yyyy"),
                      new SimpleDateFormat("MMM dd, yyyy"));

    /**
     * Helper method to convert a String to a java.util.Date.
     * @param dateStr the date string in the form 'MM/dd/yyyy'
     * @return Date the formatted date, null if not valid format
     */
    public static Date getDateFromString(String dateStr) {
        synchronized (DATE_FMT) {
            Date result = DATE_FMT.parse(dateStr, new ParsePosition(0));
            if (result == null) {
                // Try some other formats...
                for (SimpleDateFormat sdf : DATE_FMT_LIST) {
                    result = sdf.parse(dateStr, new ParsePosition(0));
                    if (result != null) { break; }
                }
            }
            return result;
        }
    }

    /**
     * Helper method to get Date as String.
     * @param date the Date
     * @return String the date
     */
    public static String getDateString(Date date) {
        if (date == null) {
            return "";
        } else {
            return DATE_FMT.format(date);
        }
    }

    /**
     * Helper method to get Date as String.
     * @param date the Date
     * @return String the date
     */
    public static String getTimeString(Date date) {
        if (date == null) {
            return "";
        } else {
            return TIME_FMT.format(date);
        }
    }
}
