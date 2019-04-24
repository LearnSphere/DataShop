/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.example;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * This class demonstrates how to create a DAO
 * to write custom queries.  It demonstrates how to write a
 * query in HQL with named parameters.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExampleDao extends AbstractDaoHibernate {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The HQL for the findProblem method.
     */
    private static final String FIND_PROBLEM_QUERY =
        "select problem from ProblemItem problem"
        + " join problem.datasetLevel section"
        + " join section.parent unit"
        + " join unit.dataset dataset"
        + " where section.levelTitle = 'Section'"
        + " and unit.levelTitle = 'Unit'"
        + " and problem.problemName = :problemName"
        + " and section.levelName = :sectionName"
        + " and unit.levelName = :unitName"
        + " and dataset.id = :datasetId";

    /**
     * Returns a problem item given the dataset id, unit name, section name and problem name.
     * @param datasetId the dataset id
     * @param unitName the name of the unit
     * @param sectionName the name of the section
     * @param problemName the name of the problem
     * @return a problem item
     */
    public ProblemItem findProblem(Integer datasetId,
            String unitName, String sectionName, String problemName) {
        ProblemItem problemItem = null;

        String[] params = {"problemName", "sectionName", "unitName", "datasetId"};
        Object[] values = {problemName, sectionName, unitName, datasetId};

        List problemList =
            getHibernateTemplate().findByNamedParam(FIND_PROBLEM_QUERY, params, values);

        if (problemList.size() == 1) {
            problemItem = (ProblemItem)problemList.get(0);
        } else if (problemList.size() > 1) {
            logger.error("More than on problem found for"
                    + " dataset " + datasetId
                    + " unit " + unitName
                    + " section " + sectionName
                    + " problem " + problemName);
            problemItem = (ProblemItem)problemList.get(0);
        } else {
            logger.info("No problem found for"
                    + " dataset " + datasetId
                    + " unit " + unitName
                    + " section " + sectionName
                    + " problem " + problemName);
        }

        return problemItem;
    }
}
