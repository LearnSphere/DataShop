/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2007
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for Logging.  Handles string escaping.
 *
 * @author Benjamin Billings
 * @version $Revision: 6719 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-03-17 11:13:06 -0400 (Thu, 17 Mar 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class LogFormatUtils {

    /**
     * Predefined XML entities. From http://www.w3schools.com/xml/xml_cdata.asp.
     * Key elements are the characters, value elements are the escape sequences.
     * E.g. "&lt;" "&apos", etc. Looks at everything except ampersands which
     * are handled seperately.
     */
    private static final HashMap <String, String> ESCAPABLE_ENTITIES = new HashMap(5);
    static {
        ESCAPABLE_ENTITIES.put("<", "&lt;");    //    <   less than
        ESCAPABLE_ENTITIES.put(">", "&gt;");    //    >   greater than
        ESCAPABLE_ENTITIES.put("'", "&apos;");  //    '   apostrophe
        ESCAPABLE_ENTITIES.put("\"", "&quot;"); //    "   quotation mark
    }

    /**
     * String containing all predefined XML entities minus the ampersand.
     */
    private static final String NON_AMPERSAND_ENTITIES;
    static {
        Collection <String> stringsToEscape = ESCAPABLE_ENTITIES.keySet();
        StringBuffer toEscape = new StringBuffer();
        for (Iterator <String> it = stringsToEscape.iterator(); it.hasNext();) {
            String theString = it.next();
            if (!("&".equals(theString))) { toEscape.append(theString); }
        }
        NON_AMPERSAND_ENTITIES = toEscape.toString();
    }


    /** Regular expression for matching XML decimal entity expressions. */
    private static final Pattern NUMBERIC_ENTITY_PATTERN =
        Pattern.compile("&#([0-9][0-9]*);");

    /** Regular expression for matching XML hexadecimal entity expressions. */
    private static final Pattern HEX_NUM_ENTITY_PATTERN =
        Pattern.compile("&#[xX]([0-9A-Fa-f][0-9A-Fa-f]*);");

    /** Private constructor to prevent instantiation. */
    private LogFormatUtils() { }

    /**
     * Returns an escaped version of the string suitable for inclusion in an XML
     * attribute.  An already escaped string will not be re-escaped.
     * @param attributeString The string to escape.
     * @return an escaped version of the string.
     */
    public static String escapeAttribute(String attributeString) {
        String escapedString = escape(attributeString);
        return escapedString.replaceAll("\t", "");
    }

    /**
     * Check that the string does not contain any invalid XML characters, if no
     * invalid characters, returns the string, otherwise checks that the characters
     * are escaped properly.  If not escaped properly wraps the string in
     * CDATA[ ... ]]
     * @param theString The string to escape.
     * @return the string wrapped with CDATA[ ... ]]
     */
    public static String escapeElement(String theString) {
        if (theString.contains("<![CDATA[")) {
            return theString;
        }
        return hasUnescapedString(theString) ? "<![CDATA[" + theString + "]]>" : theString;
    }

    /**
     * Escape characters that are illegal in XML attribute values.
     * @param theString String to edit
     * @return edited String
     */
    private static String escape(String theString) {
        if (theString == null || theString.length() < 1) { return theString; }

        theString = fixAmpersands(theString);  // deal w/ the & chars first

        // now for the chars other than &
        StringTokenizer tokenizer = new StringTokenizer(theString, NON_AMPERSAND_ENTITIES, true);
        StringBuffer returnBuffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String currentToken = tokenizer.nextToken();

            //check that the token is a single character.
            if (currentToken.length() == 1) {
                String escapedString = ESCAPABLE_ENTITIES.get(currentToken);
                currentToken = (escapedString != null) ? escapedString : currentToken;
            }
            returnBuffer.append(currentToken);
        }
        return returnBuffer.toString();
    }

    /**
     * Checks each ampersand to make sure that it is part of an escape sequence.
     * If it's not assumes that it needs to be escaped.
     * @param theString String to check.
     * @return String with any & not involved in a escape sequence replaced with &amp;
     */
    private static String fixAmpersands(String theString) {
        int currentOccurrenceIndex = theString.indexOf('&');

        // if none found, return the string.
        if (currentOccurrenceIndex < 0) {
            return theString;
        }

        StringBuffer returnBuffer = new StringBuffer(theString.substring(0,
                currentOccurrenceIndex));

        while (currentOccurrenceIndex >= 0) {
            // get the next occurrence of the ampersand.
            int nextOccurrenceIndex = theString.indexOf('&', currentOccurrenceIndex + 1);

            String seq = null;
            if (nextOccurrenceIndex < 0) {
                seq = theString.substring(currentOccurrenceIndex);
            } else {
                seq = theString.substring(currentOccurrenceIndex, nextOccurrenceIndex);
            }

            if (!hasStartingEscapeSequence(seq)) {
                returnBuffer.append("&amp;").append(seq.substring(1));
            } else {
                returnBuffer.append(seq);
            }

            currentOccurrenceIndex = nextOccurrenceIndex;
        }

        return returnBuffer.toString();
    }

    /**
     * If the given string starts with an XML escape sequence, then return
     * that sequence.
     * @param theString string to check if it starts with an escape sequence.
     * @return the leading escape sequence, including the '&' and ';';
     *         null if theString does not begin with an escape sequence
     */
    private static Boolean hasStartingEscapeSequence(String theString) {
        if (theString == null || theString.length() < 2) { return false; }

        int semicolonPos = theString.indexOf(';');
        if (theString.charAt(0) != '&' || semicolonPos < 1) { return false; }
        String escSeq = theString.substring(0, semicolonPos + 1);

        Matcher regXMatcher = NUMBERIC_ENTITY_PATTERN.matcher(escSeq);
        if (regXMatcher.matches()) { return true; }

        regXMatcher = HEX_NUM_ENTITY_PATTERN.matcher(escSeq);
        if (regXMatcher.matches()) { return true; }

        if (escSeq.equals("&amp;")) { return true; }

        return ESCAPABLE_ENTITIES.containsValue(escSeq);
    }

    /**
     * This function checks a string to make sure that all characters listed in
     * Escapable entities are escaped, and that the ampersands are part of an escape
     * sequence of some kind.
     * @param theString the string to check for unescaped characters
     * @return true if has unescaped characters, false otherwise.
     */
    public static Boolean hasUnescapedString(String theString) {

        //see if any illegal chars appear (does not include ampersand).
        for (Iterator <String> it = ESCAPABLE_ENTITIES.keySet().iterator(); it.hasNext();) {
            if (theString.indexOf(it.next()) >= 0) { return true; }
        }

        int currentOccurrenceIndex = theString.indexOf('&');
        // if no ampersands found none to worry about.
        if (currentOccurrenceIndex < 0) { return false; }

        //else, check that they are escape sequences.
        while (currentOccurrenceIndex >= 0) {
            // get the next occurrence of the ampersand.
            int nextOccurrenceIndex = theString.indexOf('&', currentOccurrenceIndex + 1);

            String seq = null;
            if (nextOccurrenceIndex < 0) {
                seq = theString.substring(currentOccurrenceIndex);
            } else {
                seq = theString.substring(currentOccurrenceIndex, nextOccurrenceIndex);
            }

            if (!hasStartingEscapeSequence(seq)) { return true; }

            currentOccurrenceIndex = nextOccurrenceIndex;
        }
        return false;
    }
}