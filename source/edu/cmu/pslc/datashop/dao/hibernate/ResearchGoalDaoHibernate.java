/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ResearchGoalDao;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;

/**
 * Hibernate and Spring implementation of the ResearchGoalDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalDaoHibernate extends AbstractDaoHibernate
        implements ResearchGoalDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ResearchGoalItem get(Integer id) {
        return (ResearchGoalItem)get(ResearchGoalItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ResearchGoalItem find(Integer id) {
        return (ResearchGoalItem)find(ResearchGoalItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ResearchGoalItem.class);
    }

    //
    // Non-standard methods begin.
    //
    /** HQL query for the getNextOrderValue method. */
    private static final String FIND_ALL_IN_ORDER
            = "FROM ResearchGoalItem ORDER BY goalOrder";

    /**
     * Find all research goals ordered by the goal order field.
     * @return list of research goal items
     */
    public List<ResearchGoalItem> findAllInOrder() {
        return getHibernateTemplate().find(FIND_ALL_IN_ORDER);
    }

    /** HQL query for the getNextOrderValue method. */
    private static final String GET_NEXT_ORDER_VALUE
            = "SELECT max(goalOrder) FROM ResearchGoalItem";

    /**
     * Get the next order value for research goals.
     * @return 1 if no goals found, the next order value of the max order found
     */
    public Integer getNextOrderValue() {
        Integer value = 0;
        List<Integer> list =
                getHibernateTemplate().find(GET_NEXT_ORDER_VALUE);
        if (list != null && list.size() > 0) {
            if (list.get(0) != null) {
                value = (Integer)list.get(0);
            }
        }
        value++;
        return value;
    }

}
