/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * ClassElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ConditionElement {

    /** Class attribute - required. */
    private String name = null;
    /** Class attribute. */
    private String type = null;
    /** Class attribute. */
    private String desc = null;

    /**
     * Constructor.
     * @param name name
     * @param type type
     * @param desc description
     */
    public ConditionElement(String name, String type, String desc) {
        this.name = name;
        this.type = type;
        this.desc = desc;
    }

    /**
     * Constructor.
     * @param name name
     * @param type type
     */
    public ConditionElement(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Constructor.
     * @param name name
     */
    public ConditionElement(String name) {
        this.name = name;
    }

    /**
     * Gets description.
     * @return description.
     */
    public String getDesc() {
        return desc;
    }
    /**
     * Gets name.
     * @return name.
     */
    public String getName() {
        return name;
    }
    /**
     * Gets type.
     * @return type.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<condition>\n");

        if (name != null && name.length() > 0) {
            buffer.append("\t\t<name>");
            buffer.append(LogFormatUtils.escapeElement(name));
            buffer.append("</name>\n");
        }

        if (type != null && type.length() > 0) {
            buffer.append("\t\t<type>");
            buffer.append(LogFormatUtils.escapeElement(type));
            buffer.append("</type>\n");
        }

        if (desc != null && desc.length() > 0) {
            buffer.append("\t\t<desc>");
            buffer.append(LogFormatUtils.escapeElement(desc));
            buffer.append("</desc>\n");
        }

        buffer.append("\t</condition>\n");

        return buffer.toString();
    }

}
