/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Utility methods for the servlets.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10096 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-10-07 07:26:08 -0400 (Mon, 07 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ServletUtil {

    /**
     * Private constructor as this is a utility class.
     */
    private ServletUtil() {
        super();
    }

    /** Max size of the display string */
    private static final int STRING_LENGTH = 20;
    /** Standards enforce this. */
    private static final int THREE = 3;

    /**
     * Truncates a string for display purposes.
     * The default number of decimal places is 2.
     * @param theObject An Object to toString() and then to possibly truncate.
     * @param doTruncate parameter setting that will bypass the truncate if set.
     * @return the truncated string
     */
    public static String truncate(Object theObject, boolean doTruncate) {
        return truncate(theObject, doTruncate, true, 2);
    }

    /**
     * Truncates a string for display purposes or rounds a number for display purposes.
     * Adds a span (mouse over, tool tip thing) with original text.
     * @param theObject An Object to toString() and then to possibly truncate.
     * @param doTruncate parameter setting that will bypass the truncate if set.
     * @param addSpan parameter setting that will add a span to the string if true
     * @param decimalPlaces the number of decimal places to truncate to
     * @return Truncated String.
     */
    public static String truncate(Object theObject,
            boolean doTruncate, boolean addSpan, int decimalPlaces) {
        if (theObject != null) {

            if (!doTruncate) {
                return theObject.toString();
            }

            String theString = null;
            if (theObject instanceof Number) {
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(decimalPlaces);
                nf.setMinimumFractionDigits(decimalPlaces);
                theString = nf.format(theObject);
                if (theString != null) {
                    if (addSpan) {
                        theString = "<span title=\"" + theObject + "\">"
                            + theString
                            + "</span>";
                    }
                }
            } else {
                theString = theObject.toString();
                theString = theString.replace('"', ' ');
                if (theString != null) {
                    if (theString.length() > ServletUtil.STRING_LENGTH) {
                        if (addSpan) {
                            theString = "<span title=\"" + theString + "\">"
                                + theString.substring(0,
                                        ServletUtil.STRING_LENGTH - ServletUtil.THREE) + "..."
                                + "</span>";
                        } else {
                            theString = theString.substring(0,
                                        ServletUtil.STRING_LENGTH - ServletUtil.THREE) + "...";
                        }
                    }
                }
            }
            return theString;
        } else {
            return "";
        }
    }

}
