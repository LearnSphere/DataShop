/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * LevelElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LevelElement {

    /** Class attribute. */
    private String type = null;
    /** Class attribute - required. */
    private String name = null;
    /** Class attribute. */
    private LevelElement levelElement = null;
    /** Class attribute. */
    private ProblemElement problemElement = null;

    /**
     * Constructor.
     * @param type type
     * @param name name
     * @param problemElement problemElement
     */
    public LevelElement(String type, String name, ProblemElement problemElement) {
        this.type = type;
        this.name = name;
        this.problemElement = problemElement;
    }

    /**
     * Constructor.
     * @param type type
     * @param name name
     * @param levelElement levelElement
     */
    public LevelElement(String type, String name, LevelElement levelElement) {
        this.type = type;
        this.name = name;
        this.levelElement = levelElement;
    }

    /**
     * Constructor.
     * @param name name
     * @param problemElement problemElement
     */
    public LevelElement(String name, ProblemElement problemElement) {
        this.name = name;
        this.problemElement = problemElement;
    }

    /**
     * Constructor.
     * @param name name
     * @param levelElement levelElement
     */
    public LevelElement(String name, LevelElement levelElement) {
        this.name = name;
        this.levelElement = levelElement;
    }

    /**
     * Gets type.
     * @return type
     */
    public String getType() {
        return type;
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
     * Gets problemElement.
     * @return problemElement
     */
    public ProblemElement getProblemElement() {
        return problemElement;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        return toString("\t\t");
    }
    /**
     * Returns the xml version of the object.
     * @param tabs tabs
     * @return xml version of the object
     */
    public String toString(String tabs) {
        StringBuffer buffer = new StringBuffer(tabs);
        if (type != null) {
            buffer.append("<level");
            buffer.append(" type=\"");
            buffer.append(LogFormatUtils.escapeAttribute(type));
            buffer.append("\">\n");
        } else {
            buffer.append("<level>\n");
        }

        buffer.append(tabs);
        buffer.append("\t<name>");
        buffer.append(LogFormatUtils.escapeElement(name));
        buffer.append("</name>\n");

        if (levelElement != null) {
            buffer.append(levelElement.toString(tabs + "\t"));
        } else if (problemElement != null) {
            buffer.append(problemElement.toString(tabs + "\t"));
        }

        buffer.append(tabs);
        buffer.append("</level>");
        buffer.append("\n");

        return buffer.toString();
    }

}
