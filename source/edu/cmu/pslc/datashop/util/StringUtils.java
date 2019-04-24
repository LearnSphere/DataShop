/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.util;

import static java.util.Arrays.asList;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Random;

/**
 * Utility methods for processing Strings.
 *
 * @author jimbokun
 * @version $Revision: 12341 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-13 14:05:35 -0400 (Wed, 13 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class StringUtils {
    /** All uppercase characters. */
    public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * Private constructor for utility class.
     */
    private StringUtils() { }

    /**
     * Concatenate all arguments into a new StringBuffer.
     * @param args the objects to concatenate
     * @return Concatenate all arguments into a new StringBuffer.
     */
    public static String concatenate(Object... args) {
        StringBuffer buf = new StringBuffer();

        for (Object arg : args) { buf.append(arg); }

        return buf.toString();
    }

    /**
     * Join string representation of each object in list of objects with delimiter join.
     * @param join delimiter string
     * @param objs items to join
     * @return string representation of each object in list, joined by join
     */
    public static String join(String join, Iterable objs) {
        StringBuffer buf = new StringBuffer();

        if (objs != null) {
            for (Object obj : objs) {
                buf.append((buf.length() == 0 ? "" : join) + (obj == null ? "" : obj));
            }
        }

        return buf.toString();
    }

    /**
     * Join string representation of each object with delimiter join.
     * @param join delimiter string
     * @param objs items to join
     * @return string representation of each object in list, joined by join
     */
    public static String join(String join, Object... objs) { return join(join, asList(objs)); }

    /**
     * Strips out tabs and new line characters to prevent the tab delimited file
     * from breaking.
     * TODO copied from ExportHelper need to put one place
     * @param theString The string to strip the chars out of.
     * @return theString w/o tabs or line breaks.
     */
    public static String stripChars(String theString) {
        if (theString != null) {
            return theString.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
        }
        return null;
    }

    /**
     * Wraps a string in double quotes.
     * @param str the string
     * @return a string wrapped in double quotes
     */
    public static String wrapInDoubleQuotes(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Test if a string is a numeric value
     * @param str the string
     * @return a boolean true if the string is a numeric value
     */
    public static boolean isNumeric(String str) {
            try {
                    double d = Double.parseDouble(str);
                    return true;
            } catch (NumberFormatException nfe) {
                    return false;
            }
    }

    /**
     * Test if a string is a numeric value
     * @param str the string
     * @return a boolean true if the string is a date
     */

    public static boolean isDate(String str) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                    df.parse(str);
                    return true;
            } catch (ParseException e) {
                    return false;
            }
    }

    /**
     * Generate a string with the specified length by randomly choosing characters.
     * @param length length of the string to build
     * @param chars select from these characters
     * @return the generated string
     */
    public static String randomString(int length, String chars) {
        StringBuffer buf = new StringBuffer(length);
        Random rnd = new Random();

        for (int i = 0; i < length; i++) {
            buf.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return buf.toString();
    }
}
