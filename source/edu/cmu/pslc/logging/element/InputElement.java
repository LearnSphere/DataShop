/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * InputElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InputElement {

    /** Class attribute - required. */
    private String input = null;
    /** Class attribute. */
    private String type = null;
    /** Class attribute. */
    private String id = null;

    /**
     * Constructor.
     * @param input input
     * @param type type
     * @param id id
     */
    public InputElement(String input, String type, String id) {
        this.input = input;
        this.type = type;
        this.id = id;
    }
    /**
     * Constructor.
     * @param selection selection
     */
    public InputElement(String selection) {
        this.input = selection;
    }

    /**
     * Gets selection.
     * @return selection
     */
    public String getSelection() {
        return input;
    }
    /**
     * Gets type.
     * @return type
     */
    public String getType() {
        return type;
    }
    /**
     * Gets id.
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t\t<input");

        if (type != null) {
            buffer.append(" type=\"");
            buffer.append(LogFormatUtils.escapeAttribute(type));
            buffer.append("\"");
        }

        if (id != null) {
            buffer.append(" id=\"");
            buffer.append(LogFormatUtils.escapeAttribute(id));
            buffer.append("\"");
        }

        buffer.append(">");

        buffer.append(LogFormatUtils.escapeElement(input));

        buffer.append("</input>\n");

        return buffer.toString();
    }
}
