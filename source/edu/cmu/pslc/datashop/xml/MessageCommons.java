/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * This class is an attempt to aggregate common functions used in parsing
 * messages (context, tool, and tutor).
 * @author Mike Komisin
 *
 */
public class MessageCommons {

    /**
     * Private constructor.
     */
    private MessageCommons() {

    }

    private final static String INVALID_CHARS = "[\\r\\n\\t]+";
    private final static String INVALID_CHAR_REPLACEMENT_TEXT = " ";

    /**
     * Replace undesirable characters in a string with spaces.
     * @param input the input string
     * @return a string without undesirable characters
     */
    public static String replaceInvalidChars(String input) {
        String output = null;
        if (input != null) {
            output = input.replaceAll(INVALID_CHARS, INVALID_CHAR_REPLACEMENT_TEXT).trim();
        }
        return output;
    }

}
