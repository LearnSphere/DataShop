/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import static edu.cmu.pslc.datashop.util.UtilConstants.MAGIC_1000;
import static edu.cmu.pslc.datashop.util.StringUtils.stripChars;

import java.text.DecimalFormat;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This utility class contains members that provide help with formatting things for
 * display.
 *
 * @author Kyle Cunningham
 * @version $Revision: 9702 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-07-19 11:57:43 -0400 (Fri, 19 Jul 2013) $
 * <!-- $KeyWordsOff: $ -->
 */

public final class FormattingUtils {

    /** Private constructor as this is a utility class. */
    private FormattingUtils() { };

    /** Decimal formatter for learning curve values. */
    public static final DecimalFormat LC_DECIMAL_FORMAT = new DecimalFormat("##########.###");

    /** String to represent a null value in the display '.'. */
    public static final String NULL_VALUE_PLACEHOLDER = new String(".");

    /** Format for the exported times. */
    private static final FastDateFormat EXPORT_DATE_FORMAT =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    /**
     * Uses the learning curve decimal format to format a given value.
     * @param value the value to convert to seconds and format.
     * @return a string holding the formatted seconds value, to 3 decimal places.
     */
    public static String formatForSeconds(Long value) {
        if (value == null) { return null; }
        Double doubleValue = new Double(value.doubleValue() / MAGIC_1000);
        return LC_DECIMAL_FORMAT.format(doubleValue);
    }

    /**
     * Uses the learning curve decimal format to format a given value.
     * @param value the value to convert to seconds and format.
     * @return a string holding the formatted seconds value, to 3 decimal places.
     */
    public static String formatForSeconds(Integer value) {
        if (value == null) { return null; }
        Double doubleValue = new Double(value.doubleValue() / MAGIC_1000);
        return LC_DECIMAL_FORMAT.format(doubleValue);
    }

    /**
     * Helper function to properly display NULL items as blanks rather than NULL.
     * @param obj the object to display
     * @return String of the display.
     */
    public static String displayObject(Object obj) {
        return displayObject(obj, false);
    }

    /**
     * Helper function to properly display NULL items.  If replaceNulls is true
     * a null object will be replaced with NULL_VALUE_PLACEHOLDER, otherwise "" will
     * be returned.
     * @param obj the object to process.
     * @param replaceNulls if true, null objects will be replaced, otherwise they will not.
     * @return a string to display.
     */
    public static String displayObject(Object obj, boolean replaceNulls) {
        if (obj == null) {
            return (replaceNulls ? NULL_VALUE_PLACEHOLDER : "");
        } else if (obj instanceof Date) {
            return EXPORT_DATE_FORMAT.format((Date)obj);
        } else {
            return stripChars(obj.toString());
        }
    }
} // end FormattingUtils.java
