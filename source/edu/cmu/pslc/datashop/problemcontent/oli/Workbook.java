/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.problemcontent.oli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Contains information relevant to workbook pages and their
 * activities, inline assessments, and learning objectives.
 * @author mkomisin
 *
 */
public class Workbook extends edu.cmu.pslc.datashop.dto.DTO {
    /** Workbook ID. */
    private String id = null;
    /** Workbook ID. */
    private String title = null;
    /** Workbook file path. */
    private String workbookFilePath = null;
    /** Activities. */
    private List<Element> activities = new ArrayList<Element>();
    /** Inline assessments. */
    private List<Element> inlines = new ArrayList<Element>();
    /** Learning objectives. */
    private List<Element> learningObjectives = new ArrayList<Element>();

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Default constructor.
     */
    public Workbook() {

    }

    /**
     * Constructor that takes the workbook page's
     * root element and extracts the desired inner elements.
     * @param root the workbook page's root element
     * @param file the workbook file
     */
    public Workbook(Element root, File file) {
        id = root.getAttributeValue("id");
        workbookFilePath = file.getAbsolutePath();

        Element head = root.getChild("head");
        if (head != null) {
            Element titleElement = head.getChild("title");
            if (titleElement != null) {
                title = titleElement.getText().trim();
            }
        }
        // If no title is found, make it an empty string
        if (title == null) {
            title = new String("");
        }

        // Other workbook pages or activities
        addActivities(CommonXml.getElementsByTagName(root, "activity"));
        addActivities(CommonXml.getElementsByTagName(root, "activity_link"));
        // Learning objective references
        addLearningObjectives(CommonXml.getElementsByTagName(root, "objref"));
        // Learning objectives
        addLearningObjectives(CommonXml.getElementsByTagName(root, "objective"));
        // Inline assessments
        addInlines(CommonXml.getElementsByTagName(root, "inline"));

    }

    /**
     * Get the workbook ID.
     * @return the workbook ID
     */
    public String getWorkbookId() {
        return id;
    }

    /**
     * Set the workbook ID.
     * @param workbookId the workbook ID
     */
    public void setWorkbookId(String workbookId) {
        this.id = workbookId;
    }

    /**
     * Add activity elements to this workbook.
     * @param activities the list of activity elements
     */
    public void addActivities(List<Element> activities) {
        this.activities.addAll(activities);
    }

    /**
     * Add inline elements to this workbook.
     * @param inlines the list of inline elements
     */
    public void addInlines(List<Element> inlines) {
        this.inlines.addAll(inlines);
    }

    /**
     * Add learning objective elements to this workbook.
     * @param learningObjectives the list of learning objective elements
     */
    public void addLearningObjectives(List<Element> learningObjectives) {
        this.learningObjectives.addAll(learningObjectives);
    }

    /**
     * Get the activities in this workbook.
     * @return the activities in this workbook
     */
    public List<Element> getActivities() {
        return activities;
    }

    /**
     * Get the inline assessments in this workbook.
     * @return the inline assessments in this workbook
     */
    public List<Element> getInlines() {
        return inlines;
    }

    /**
     * Get the actual learning objectives in this workbook.
     * @return the actual learning objectives in this workbook
     */
    public List<Element> getLearningObjectives() {
        return learningObjectives;
    }

    /**
     * Gets the title of the workbook.
     * @return the title of the workbook
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the workbook.
     * @param title the workbook title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the file path of the workbook.
     * @return the file path of the workbook
     */
    public String getFilePath() {
        return workbookFilePath;
    }
}
