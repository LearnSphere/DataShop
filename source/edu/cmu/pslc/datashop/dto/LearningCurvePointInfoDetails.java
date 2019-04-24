/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO Object for holding detailed information for a specific learning curve point.
 * @author kcunning
 * @version $Revision: 5870 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-11-06 13:54:48 -0500 (Fri, 06 Nov 2009) $
 * <!-- $KeyWordsOff: $ -->
 *
 */

public class LearningCurvePointInfoDetails extends DTO {

    /** The selected measure (student, skill, step, problem). */
    private String selectedMeasure;
    /** The type of curve selected. */
    private String curveType;
    /** How the data is sorted (by measure or curve type value). */
    private String sortBy;
    /** How the sort is directed (ascending or descending). */
    private String sortDirection;
    /** Data structure holding identifiers for measure, the measure name and curve type value. */
    private List<NameValuePair> infoDetailsList;
    /** Data structure holding identifiers for all problems returned. */
    private List<Long> problemIds;

    /**
     * Default Constructor.
     */
    public LearningCurvePointInfoDetails() {
        this.infoDetailsList = new ArrayList();
        this.problemIds = new ArrayList();
    }
    /**
     * Full Constructor.
     * @param curveType the selected curve type.
     * @param infoDetailsList the details.
     */
    public LearningCurvePointInfoDetails(String curveType, List infoDetailsList) {
        this.curveType = curveType;
        this.infoDetailsList = infoDetailsList;
    } // end Constructor.

    /**
     * Return the selected measure.
     * @return the selectedMeasure
     */
    public String getSelectedMeasure() {
        return selectedMeasure;
    }
    /**
     * Set the selected measure.
     * @param selectedMeasure the selectedMeasure to set
     */
    public void setSelectedMeasure(String selectedMeasure) {
        this.selectedMeasure = selectedMeasure;
    }
    /**
     * Get the curve type.
     * @return the curveType
     */
    public String getCurveType() {
        return curveType;
    }

    /**
     * Set the curve type.
     * @param curveType the curveType to set
     */
    public void setCurveType(String curveType) {
        this.curveType = curveType;
    }

    /**
     * Get the sort by.
     * @return the sortBy
     */
    public String getSortBy() {
        return sortBy;
    }
    /**
     * Set the sort by.
     * @param sortBy the sortBy to set
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Get the sort direction.
     * @return the sortDirection
     */
    public String getSortDirection() {
        return sortDirection;
    }

    /**
     * Set the sort direction.
     * @param sortDirection the sortDirection to set
     */
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * Get the info details.
     * @return the infoDetails
     */
    public List<NameValuePair> getInfoDetailsList() {
        return infoDetailsList;
    }

    /**
     * Set the info details.
     * @param infoDetailsList the infoDetails to set
     */
    public void setInfoDetailsList(List<NameValuePair> infoDetailsList) {
        this.infoDetailsList = infoDetailsList;
    }

    /**
     * Add a name/value pair, keyed by a measure id to the LCPID object.
     * @param measureId the identifier of the measure (student_id, skill_id, etc).
     * @param measureName the name of the measure.
     * @param curveTypeValue the value of the curveType for this measure.
     * @param frequency the number of observations for this measure/value pair.
     */
    public void addNameValuePair(Long measureId, String measureName, Double curveTypeValue,
            Long frequency) {
        addNameValuePair(measureId, null, measureName, curveTypeValue, frequency);
    }

    /**
     * Add a name/value pair, keyed by a measure id to the LCPID object.
     * @param measureId the identifier of the measure (student_id, skill_id, etc).
     * @param problemId the identifier of the corresponding problem for this measure.
     * @param measureName  the name of the measure.
     * @param curveTypeValue the value of the curveType for this measure.
     * @param frequency the number of observations for this measure/value pair.
     */
    public void addNameValuePair(Long measureId, Long problemId, String measureName,
            Double curveTypeValue, Long frequency) {
        NameValuePair nvp = new NameValuePair(measureId, measureName, curveTypeValue, frequency);
        if ((Long)problemId != null) {
            nvp.setProblemId(problemId);
        }
        this.infoDetailsList.add(nvp);
    }

    /**
     * * Add a name/value pair, keyed by a measure id to the LCPID object.
     * @param measureId the identifier of the measure (student_id, skill_id, etc).
     * @param nvp the name/value pair to add.
     */
    public void addNameValuePair(Long measureId, NameValuePair nvp) {
        nvp.setId(measureId);
        this.infoDetailsList.add(nvp);
    }

    /**
     * Get the list of problem identifiers.
     * @return the problemIds
     */
    public List<Long> getProblemIds() {
        return problemIds;
    }

    /**
     * Set the list of problem identifiers.
     * @param problemIds the problemIds to set
     */
    public void setProblemIds(List<Long> problemIds) {
        this.problemIds = problemIds;
    }


} // end LearningCurvePointInfoDetails.java
