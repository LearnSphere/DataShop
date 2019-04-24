/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ResearcherTypeResearchGoalMapDao;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapId;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;

/**
 * Hibernate and Spring implementation of the ResearcherTypeResearchGoalMapDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeResearchGoalMapDaoHibernate
        extends AbstractDaoHibernate implements ResearcherTypeResearchGoalMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ResearcherTypeResearchGoalMapItem get(ResearcherTypeResearchGoalMapId id) {
        return (ResearcherTypeResearchGoalMapItem)get(ResearcherTypeResearchGoalMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ResearcherTypeResearchGoalMapItem find(ResearcherTypeResearchGoalMapId id) {
        return (ResearcherTypeResearchGoalMapItem)find(ResearcherTypeResearchGoalMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ResearcherTypeResearchGoalMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByType method. */
    private static final String FIND_BY_TYPE_HQL
        = "select distinct map.researchGoal"
        + " from ResearcherTypeResearchGoalMapItem map"
        + " join map.researcherType rt"
        + " where map.researcherType = ?"
        + " or rt.parentTypeId = ?"
        + " order by map.goalOrder";

    /**
     *  Return a list of ResearchGoalItems.
     *  @param typeItem the given Type item
     *  @return a list of items
     */
    public List<ResearchGoalItem> findByType(ResearcherTypeItem typeItem) {
        Object[] params = {typeItem, (Integer)typeItem.getId()};
        List<ResearchGoalItem> itemList =
                getHibernateTemplate().find(FIND_BY_TYPE_HQL, params);
        return itemList;
    }

    /** HQL query for the findOtherGoals method. */
    private static final String FIND_OTHER_GOALS_HQL
            = "FROM ResearchGoalItem goal"
            + " WHERE goal.id NOT IN"
            + " (select map.researchGoal.id"
            + "  from ResearcherTypeResearchGoalMapItem map"
            + "  where map.researcherType.id = ?)";

    /**
     *  Return a list of ResearchGoalItems.
     *  @param typeItem the given Type item
     *  @return a list of items
     */
    public List<ResearchGoalItem> findOtherGoals(ResearcherTypeItem typeItem) {
        Object[] params = {(Integer)typeItem.getId()};
        List<ResearchGoalItem> itemList =
                getHibernateTemplate().find(FIND_OTHER_GOALS_HQL, params);
        return itemList;
    }

    /** HQL query for the findByGoal method. */
    private static final String FIND_BY_GOAL_HQL
            = "select map.researcherType"
            + " from ResearcherTypeResearchGoalMapItem map"
            + " where map.researchGoal = ?"
            + " order by map.goalOrder";

    /**
     *  Return a list of ResearcherTypeItems.
     *  @param goalItem the given research goal item
     *  @return a list of items
     */
    public List<ResearcherTypeItem> findByGoal(ResearchGoalItem goalItem) {
        Object[] params = {goalItem};
        List<ResearcherTypeItem> itemList =
                getHibernateTemplate().find(FIND_BY_GOAL_HQL, params);
        return itemList;
    }
}
