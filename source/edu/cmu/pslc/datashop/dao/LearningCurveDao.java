/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.dto.LearningCurveOptions;
import edu.cmu.pslc.datashop.dto.LearningCurvePoint;
import edu.cmu.pslc.datashop.dto.LearningCurvePointInfoDetails;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * This DAO gets the information for a learning curve.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6049 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2010-04-23 12:16:53 -0400 (Fri, 23 Apr 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface LearningCurveDao extends AbstractDao<SampleItem> {

    /** LCPI Student Detail Request. */
    String LCPI_STUDENT_DETAILS = "student";
    /** LCPI Skill Detail Request. */
    String LCPI_SKILL_DETAILS = "skill";
    /** LCPI Problem Detail Request. */
    String LCPI_PROBLEM_DETAILS = "problem";
    /** LCPI Step Detail Request. */
    String LCPI_STEP_DETAILS = "step";

    /**
     * Checks that in the learning curve table to see if this sample
     * has been calculated and stored.
     * @param sampleItem The sample to test
     * @return true if the sample has been calculated, false otherwise.
     */
    boolean sampleReady(SampleItem sampleItem);

    /**
     * Get a learning curve for a given skill model and sample.
     * @param reportOptions a helper class containing all the datashop report options.
     * @return a HashMap with the key of an Long index of the type.  The value is a
     * list of learning curve points in order by opportunity.
     */
    Map <Long, List <LearningCurvePoint>> getLearningCurve(LearningCurveOptions reportOptions);

    /**
     * Get the maximum number of opportunities (by skill, student and rollup) for a learning curve.
     * Used when drawing latency curves (correct_step_time and assistance_time),
     * since we may not get points for all opportunities.
     * @param reportOptions a helper class containing the learning curve report options.
     * @return a Map holding the maximum number of opportunities keyed by the typeId.
     */
    Map<Long, Integer> getMaxOpportunityCount(LearningCurveOptions reportOptions);

    /**
     * Get the LC point info details for a selected point and measure.
     * @param oppNum the opportunity number for the selected LC point.
     * @param reportOptions the learning curve report options (selections made by user).
     * @param measure the selected measure for the point info details.
     * @param sortBy how to sort the results - by measureName (problem name, for example)
     *      or by curveTypeValue (error rate value, for example).
     * @param sortByDirection the direction of the sort (ASC or DESC).
     * @return a nice DTO object.
     */
    LearningCurvePointInfoDetails getLCPointInfoDetails(
            int oppNum, LearningCurveOptions reportOptions, String measure,
            String sortBy, String sortByDirection);
}
