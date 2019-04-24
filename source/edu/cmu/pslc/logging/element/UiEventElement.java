/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class UiEventElement implements EventElement {

    private String id = null;
    private String name = null;

    public UiEventElement(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("<ui_event");

        if (id != null) {
            buffer.append(" id=\"");
            buffer.append(LogFormatUtils.escapeAttribute(id));
            buffer.append("\"");
        }

        if (name != null) {
            buffer.append(" name=\"");
            buffer.append(LogFormatUtils.escapeAttribute(name));
            buffer.append("\"");
        }

        buffer.append("/>\n");

        return buffer.toString();
    }
}
