/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * CustomFieldElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12867 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-01-21 09:54:56 -0500 (Thu, 21 Jan 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldElement {

    /** Class attribute - required. */
    private String name = null;
    /** Class attribute - required. */
    private String value = null;

    /**
     * Constructor.
     * @param name name
     * @param value value
     */
    public CustomFieldElement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets name.
     * @return name.
     */
    public String getName() {
        return name;
    }
    /**
     * Gets value.
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<custom_field>\n");

        if (name != null && name.length() > 0) {
            buffer.append("\t\t<name>");
            buffer.append(LogFormatUtils.escapeElement(name));
            buffer.append("</name>\n");
        }

        if (value != null) {
            buffer.append("\t\t<value>");
            buffer.append(LogFormatUtils.escapeElement(value));
            buffer.append("</value>\n");
        }

        buffer.append("\t</custom_field>\n");

        return buffer.toString();
    }

}
