/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class PropertyElement {

    private String name = null;
    private List entryList = null;
    private String contents = null;

    public PropertyElement(String name, String contents) {
        this.name = name;
        this.contents = contents;
    }
    /**
     * Create a list-valued property. Will create entry element for each list
     * element.
     * @param name property name
     * @param entryList list of values, each added by {@link #addEntry(Object)}
     */
    public PropertyElement(String name, List entryList) {
        this.name = name;
        if (entryList == null) { return; }
        for (Iterator iter = entryList.iterator(); iter.hasNext();) {
            addEntry(iter.next());
        }
    }
    public PropertyElement(String name) {
        this.name = name;
    }

    public List getEntryList() {
        return entryList;
    }
    /**
     * Add a single entry to a list-valued property. Value stored will be
     * result of argument's {@link Object#toString()} method.
     * Stores "null" if argument is null.
     * @param entry object to add
     */
    public void addEntry(Object entry) {
        if (entryList == null) { entryList = new ArrayList(); }

        if (entry == null) {
            entryList.add("null");
        } else {
            entryList.add(entry.toString());
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<property");
        if (name != null) {
            buffer.append(" name=\"");
            buffer.append(LogFormatUtils.escapeAttribute(name));
            buffer.append("\"");
        }

        buffer.append(">\n");

        if (entryList != null && entryList.size() > 0) {
            for (Iterator iter = entryList.iterator(); iter.hasNext();) {
                buffer.append("\t\t<entry>");
                buffer.append(LogFormatUtils.escapeElement((String)iter.next()));
                buffer.append("</entry>\n");
            }
        }
        if (contents != null && contents.length() > 0) {
            buffer.append("\t\t");
            buffer.append(LogFormatUtils.escapeElement(contents));
            buffer.append("\n");
        }

        buffer.append("\t</property>\n");

        return buffer.toString();
    }
}
