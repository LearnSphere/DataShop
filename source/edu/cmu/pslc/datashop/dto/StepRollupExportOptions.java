/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.ArrayList;
import java.util.List;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Contains all the options for creating the student step rollup.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10169 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-10-16 09:13:10 -0400 (Wed, 16 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepRollupExportOptions {

    /** List of samples to get items for */
    private List <SampleItem> samples;
    /** List of students to get items for */
    private List <StudentItem> students;
    /** List of problems to get items for */
    private List <ProblemItem> problems;
    /** List of skills to get items for */
    private List <SkillItem> skills;
    /** The currently selected skill model. */
    private SkillModelItem model;
    /** The limit. */
    private Integer limit;
    /** The offset. */
    private Integer offset;
    /** Whether the UI has user options set. */
    private boolean hasUserOptions;
    /** Boolean indicating whether or not to displaySkills. */
    private boolean displaySkills;
    /** Boolean indicating whether or not to display skills for all skill models. */
    private boolean displayAllModels;
    /** Boolean indicating whether or not to export the cached version. */
    private boolean exportCachedVersion;
    /** The number of rows in problem rollup export. */
    private int numProblems = 0;
    /** Whether we're export the cached or generated export. */
    private boolean isCached = false;
    /** Default Constructor. */
    public StepRollupExportOptions() {
        samples = new ArrayList <SampleItem> ();
        students = new ArrayList <StudentItem> ();
        problems = new ArrayList <ProblemItem> ();
        skills = new ArrayList <SkillItem> ();
        model = null;
        hasUserOptions = false;
        displaySkills = false;
        displayAllModels = false;
        exportCachedVersion = false;
    }

    /**
     * Constructor that takes a sample item.
     * @param sampleItem the sample item
     */
    public StepRollupExportOptions(SampleItem sampleItem) {
        samples = new ArrayList <SampleItem> ();
        if (sampleItem != null) {
            samples.add(sampleItem);
        }

        students = new ArrayList <StudentItem> ();
        problems = new ArrayList <ProblemItem> ();
        skills = new ArrayList <SkillItem> ();
        model = null;
        hasUserOptions = false;
        displaySkills = false;
        displayAllModels = false;
        exportCachedVersion = false;
    }


    /** Number of problems for the sample. @return number of problems for the sample */
    public Integer getNumProblems() {
        return numProblems;
    }

    /** Returns samples. @return Returns the samples. */
    public List <SampleItem> getSamples() {
        return samples;
    }

    /** Returns students. @return Returns the students. */
    public List <StudentItem> getSelectedStudents() {
        return students;
    }

    /** Returns problems. @return Returns the problems. */
    public List <ProblemItem> getSelectedProblems() {
        return problems;
    }

    /** Returns skills. @return Returns the skills. */
    public List <SkillItem> getSelectedSkills() {
        return skills;
    }

    /** Returns model. @return Returns the model. */
    public SkillModelItem getModel() {
        return model;
    }

    /** Set samples. @param samples The samples to set. */
    public void setSamples(List <SampleItem> samples) {
        this.samples = samples;
    }

    /** Set students. @param students The students to set. */
    public void setSelectedStudents(List <StudentItem> students) {
        this.students = students;
    }

    /** Set problems. @param problems The problems to set. */
    public void setSelectedProblems(List <ProblemItem> problems) {
        this.problems = problems;
    }

    /** Set skills. @param skills The skills to set. */
    public void setSelectedSkills(List <SkillItem> skills) {
        this.skills = skills;
    }

    /** Set model. @param model The model to set. */
    public void setModel(SkillModelItem model) {
        this.model = model;
    }

    /** Returns displaySkills. @return Returns the displaySkills. */
    public boolean isDisplaySkills() {
        return displaySkills;
    }

    /** Returns displayAllModels. @return Returns the displayAllModels. */
    public boolean isDisplayAllModels() {
        return displayAllModels;
    }

    /** Returns exportCachedVersion. @return exportCachedVersion. */
    public boolean getExportCachedVersion() {
        return exportCachedVersion;
    }

    /** Set displaySkills. @param displaySkills The displaySkills to set. */
    public void setDisplaySkills(boolean displaySkills) {
        this.displaySkills = displaySkills;
    }

    /** Set displayAllModels. @param displayAllModels The displayAllModels to set. */
    public void setDisplayAllModels(boolean displayAllModels) {
        this.displayAllModels = displayAllModels;
    }

     /** Set exportCachedVersion. @param exportCachedVersion The exportCachedVersion to set. */
    public void setExportCachedVersion(boolean exportCachedVersion) {
        this.exportCachedVersion = exportCachedVersion;
    }

    /**
     * Message indicating that the user made invalid selections, or null otherwise.
     * @return Message indicating that the user made invalid selections, or null otherwise.
     */
    public String validationMessage() {
        if (getSamples() == null || getSamples().size() == 0) {
            return "Select at least one sample to view student-step export data.";
        }
        if (getSelectedStudents() == null || getSelectedStudents().size() == 0) {
            return "Select at least one student to view student-step export data.";
        }
        if (getSelectedProblems() == null || getSelectedProblems().size() == 0) {
            return "Select at least one problem to view student-step export data.";
        }
        return null;
    }

    /**
     * false if the user made invalid selections, true otherwise.
     * @return false if the user made invalid selections, true otherwise.
     */
    public boolean isValid() {
        return validationMessage() == null;
    }

    /** Return the limit.
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }

    /** Return the offset.
     * @return the offset
     */
    public Integer getOffset() {
        return offset;
    }

    /** Set the limit.
     * @param limit the limit to set
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /** Set the offset.
     * @param offset the offset to set
     */
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    /** Set whether the UI has user options selected.
     * @param hasUserOptions whether the UI has user options selected
     */
    public void setHasUserOptions(Boolean hasUserOptions) {
        this.hasUserOptions = hasUserOptions;
    }

    /** Whether the UI has user options selected.
     * @return whether the UI has user options selected
     */
    public boolean hasUserOptions() {
        return hasUserOptions;
    }
}
