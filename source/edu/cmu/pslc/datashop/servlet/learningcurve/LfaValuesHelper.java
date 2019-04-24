/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.learningcurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;


/**
 * Helper to get data for the Learning Curve -> Model Values subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 14256 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-08 13:18:08 -0400 (Fri, 08 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LfaValuesHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public LfaValuesHelper() { }

    /**
     * Helper method to get list of SkillItems for a specific Dataset and SkillModel.
     * @param dataset the DatasetItem
     * @param skillModel the SkillModelItem
     * @return List of SkillItem objects
     */
    public List<SkillItem> getSkillList(DatasetItem dataset, SkillModelItem skillModel) {

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem sample = sampleDao.findOrCreateDefaultSample(dataset);
        List<SkillItem> result = sampleDao.getSkillList(sample, skillModel);

        Collections.sort(result);
        return result;
    }

    /**
     * Helper method to get sorted list of SkillItems for a specific Dataset and SkillModel.
     * Skills will be sorted by Intercept, ascending.
     * @param dataset the DatasetItem
     * @param skillModel the SkillModelItem
     * @return List of SkillItem objects
     */
    public List<SkillItem> getSortedSkillList(DatasetItem dataset, SkillModelItem skillModel) {

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem sample = sampleDao.findOrCreateDefaultSample(dataset);
        List<SkillItem> result = sampleDao.getSkillList(sample, skillModel);

        Comparator<SkillItem> comparator = new SkillItemComparator();

        Comparator<SkillItem> nullComparator = new NullComparator(comparator, false);

        Collections.sort(result, nullComparator);
        return result;
    }

    /**
     * Helper method to get list of StudentItems for a specific Dataset.
     * @param dataset the DatasetItem
     * @return List of StudentItem objects
     */
    public List<StudentItem> getStudentList(DatasetItem dataset) {

        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        SampleItem sample = sampleDao.findOrCreateDefaultSample(dataset);
        List<StudentItem> result = sampleDao.getStudentListFromAggregate(sample);

        return result;
    }

    /**
     * Helper method to get map of skills to number of unique steps for
     * a given dataset and list of skills.
     * @param dataset the DatasetItem
     * @param skillList list of skills of interest
     * @return Map of skill ids and number of unique steps
     */
    public HashMap<SkillItem, Long>
        getNumberUniqueStepsBySkillMap(DatasetItem dataset,
                                       List<SkillItem> skillList)
    {
        HashMap<SkillItem, Long> result = new HashMap<SkillItem, Long>();

        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();

        for (Iterator i = skillList.iterator(); i.hasNext();) {
            SkillItem skill = (SkillItem)i.next();
            Long numUniqSteps = stepRollupDao.getNumberUniqueSteps(dataset, skill);
            result.put(skill, numUniqSteps);
        }

        return result;
    }

    /**
     * Helper method to get map of skills to number of observations for
     * a given dataset and list of skills.
     * @param dataset the DatasetItem
     * @param skillList list of skills of interest
     * @return Map of skill ids and number of observations
     */
    public HashMap<SkillItem, Long>
        getNumberObservationsBySkillMap(DatasetItem dataset,
                                        List<SkillItem> skillList)
    {
        HashMap<SkillItem, Long> result = new HashMap<SkillItem, Long>();

        StepRollupDao stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();

        for (Iterator i = skillList.iterator(); i.hasNext();) {
            SkillItem skill = (SkillItem)i.next();
            Long numObs = stepRollupDao.getNumberObservations(dataset, skill);
            result.put(skill, numObs);
        }

        return result;
    }
                                                             
    /**
     * Comparator for SkillItem objects.
     */
    private static final class SkillItemComparator implements Comparator<SkillItem> {

        private SkillItemComparator() {}

        /**
         * Comparator.
         * @param o1 the first SkillItem object
         * @param o2 the second SkillItem object
         * @return comparator value
         */
        public int compare(SkillItem o1, SkillItem o2) {
            return o1.getBeta().compareTo(o2.getBeta());
        }
    }
}
