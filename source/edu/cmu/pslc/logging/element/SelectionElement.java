/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class SelectionElement {

    private String selection = null; //required
    private String type = null;
    private String id = null;

    public SelectionElement(String selection, String type, String id) {
        this.selection = selection;
        this.type = type;
        this.id = id;
    }
    public SelectionElement(String selection) {
        this.selection = selection;
    }

    public String getSelection() {
        return selection;
    }
    public String getType() {
        return type;
    }
    public String getId() {
        return id;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t\t<selection");

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

        buffer.append(LogFormatUtils.escapeElement(selection));

        buffer.append("</selection>\n");

        return buffer.toString();
    }
}
