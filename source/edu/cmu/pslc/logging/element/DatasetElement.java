/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * DatasetElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetElement {

    /** Class attribute - required. */
    private String name = null;
    /** Class attribute - required. */
    private LevelElement levelElement = null;

    /**
     * Constructor.
     * @param name name
     * @param levelElement levelElement
     */
    public DatasetElement(String name, LevelElement levelElement) {
        this.name = name;
        this.levelElement = levelElement;
    }

    /**
     * Gets name.
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Gets levelElement.
     * @return levelElement
     */
    public LevelElement getLevelElement() {
        return levelElement;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<dataset>\n");

        buffer.append("\t\t<name>");
        buffer.append(LogFormatUtils.escapeElement(name));
        buffer.append("</name>\n");

        buffer.append(levelElement.toString());

        buffer.append("\t</dataset>");
        buffer.append("\n");

        return buffer.toString();
    }

}
