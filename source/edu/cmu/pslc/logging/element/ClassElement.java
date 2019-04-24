/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.util.LogFormatUtils;

/**
 * Class element.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ClassElement {

    /** Class attribute. */
    private String name = null;
    /** Class attribute. */
    private String school = null;
    /** Class attribute. */
    private String period = null;
    /** Class attribute. */
    private String description = null;
    /** Class attribute. */
    private List instructorList = new ArrayList();

    /**
     * Constructor.
     */
    public ClassElement() { }
    /**
     * Constructor.
     * @param name name
     * @param school school
     * @param period period
     * @param description description
     */
    public ClassElement(String name, String school, String period, String description) {
        this.name = name;
        this.school = school;
        this.period = period;
        this.description = description;
    }

    /**
     * Gets name.
     * @return name
     */
    public String getName() {
        return name;
    }
    /**
     * Sets name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Gets description.
     * @return description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * Sets description.
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * Gets instructorList.
     * @return instructorList
     */
    public List getInstructorList() {
        return instructorList;
    }
    /**
     * Adds instructor.
     * @param instructor instructor
     */
    public void addInstructor(String instructor) {
        if (instructor != null && instructor.length() > 0) {
            this.instructorList.add(instructor);
        }
    }
    /**
     * Gets period.
     * @return period
     */
    public String getPeriod() {
        return period;
    }
    /**
     * Sets period.
     * @param period period
     */
    public void setPeriod(String period) {
        this.period = period;
    }
    /**
     * Gets school.
     * @return school
     */
    public String getSchool() {
        return school;
    }
    /**
     * Sets school.
     * @param school school
     */
    public void setSchool(String school) {
        this.school = school;
    }

    /**
     * toString.
     * @return string version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<class>\n");

        if (name != null && name.length() > 0) {
            buffer.append("\t\t<name>");
            buffer.append(LogFormatUtils.escapeElement(name));
            buffer.append("</name>\n");
        }
        if (school != null && school.length() > 0) {
            buffer.append("\t\t<school>");
            buffer.append(LogFormatUtils.escapeElement(school));
            buffer.append("</school>\n");
        }
        if (period != null && period.length() > 0) {
            buffer.append("\t\t<period>");
            buffer.append(LogFormatUtils.escapeElement(period));
            buffer.append("</period>\n");
        }
        if (description != null && description.length() > 0) {
            buffer.append("\t\t<description>");
            buffer.append(LogFormatUtils.escapeElement(description));
            buffer.append("</description>\n");
        }

        for (Iterator iter = instructorList.iterator(); iter.hasNext();) {
            String instructor = (String)iter.next();
            if (instructor != null && instructor.length() > 0) {
                buffer.append("\t\t<instructor>");
                buffer.append(LogFormatUtils.escapeElement(instructor));
                buffer.append("</instructor>\n");
            }
        }

        buffer.append("\t</class>");
        buffer.append("\n");

        return buffer.toString();
    }
}
