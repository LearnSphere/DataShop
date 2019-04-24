/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * ActionElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ActionElement {

    /** Class attribute. */
    private String action = null; //required
    /** Class attribute. */
    private String type = null;
    /** Class attribute. */
    private String id = null;

    /**
     * Constructor.
     * @param action action
     * @param type type
     * @param id id
     */
    public ActionElement(String action, String type, String id) {
        this.action = action;
        this.type = type;
        this.id = id;
    }

    /**
     * Constructor.
     * @param action action
     */
    public ActionElement(String action) {
        this.action = action;
    }

    /**
     * Gets selection.
     * @return selection
     */
    public String getSelection() {
        return action;
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
        StringBuffer buffer = new StringBuffer("\t\t<action");

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

        buffer.append(LogFormatUtils.escapeElement(action));

        buffer.append("</action>\n");

        return buffer.toString();
    }
}
