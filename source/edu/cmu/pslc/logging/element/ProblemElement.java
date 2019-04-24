/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * ProblemElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemElement {

    //
    // NOTE that the tutor flags are also in the ProblemItem class.
    //

    /** Tutor Flag enumeration field value - "tutor". */
    public static final String TUTOR_FLAG_TUTOR = "tutor";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_TEST = "test";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_PRE_TEST = "pre-test";
    /** Tutor Flag enumeration field value - "test". */
    public static final String TUTOR_FLAG_POST_TEST = "post-test";
    /** Tutor Flag enumeration field value - "other". */
    public static final String TUTOR_FLAG_OTHER = "other";

    /** Class attribute. */
    private String tutorFlag = null;
    /** Class attribute. */
    private String other = null;
    /** Class attribute - required. */
    private String name = null;
    /** Class attribute. */
    private String context = null;

    /**
     * Constructor.
     * @param tutorFlag tutorFlag
     * @param other other
     * @param name name
     * @param context context
     */
    public ProblemElement(String tutorFlag, String other, String name, String context) {
        this.tutorFlag = tutorFlag;
        this.other = other;
        this.name = name;
        this.context = context;
    }
    /**
     * Constructor.
     * @param name name
     * @param context context
     */
    public ProblemElement(String name, String context) {
        this.name = name;
        this.context = context;
    }
    /**
     * Constructor.
     * @param name name
     */
    public ProblemElement(String name) {
        this.name = name;
    }

    /**
     * Gets context.
     * @return context
     */
    public String getContext() {
        return context;
    }
    /**
     * Gets name.
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Gets other.
     * @return other
     */
    public String getOther() {
        return other;
    }
    /**
     * Gets context.
     * @return tutorFlag
     */
    public String getTutorFlag() {
        return tutorFlag;
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
        if (tutorFlag != null) {
            buffer.append("<problem");
            buffer.append(" tutorFlag=\"");
            buffer.append(LogFormatUtils.escapeAttribute(tutorFlag));
            buffer.append("\"");
            if (other != null) {
                buffer.append(" other=\"");
                buffer.append(LogFormatUtils.escapeAttribute(other));
                buffer.append("\"");
            }
            buffer.append(">\n");
        } else {
            buffer.append("<problem>\n");
        }

        if (name != null && name.length() > 0) {
            buffer.append(tabs);
            buffer.append("\t<name>");
            buffer.append(LogFormatUtils.escapeElement(name));
            buffer.append("</name>\n");
        }
        if (context != null && context.length() > 0) {
            buffer.append(tabs);
            buffer.append("\t<context>");
            buffer.append(LogFormatUtils.escapeElement(context));
            buffer.append("</context>\n");
        }

        buffer.append(tabs);
        buffer.append("</problem>");
        buffer.append("\n");

        return buffer.toString();
    }

}
