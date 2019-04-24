/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ResearcherTypeDao;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;
import edu.cmu.pslc.datashop.item.SampleMetricItem;

/**
 * Hibernate and Spring implementation of the ResearcherTypeDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearcherTypeDaoHibernate extends AbstractDaoHibernate
        implements ResearcherTypeDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ResearcherTypeItem get(Integer id) {
        return (ResearcherTypeItem)get(ResearcherTypeItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ResearcherTypeItem find(Integer id) {
        return (ResearcherTypeItem)find(ResearcherTypeItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ResearcherTypeItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findAllInOrder method. */
    private static final String FIND_ALL_IN_ORDER
        = " from ResearcherTypeItem"
        + " where parentTypeId is null"
        + " order by typeOrder";

    /** HQL query for the findAllInOrder method, by parent. */
    private static final String FIND_ALL_IN_ORDER_BY_PARENT
        = " from ResearcherTypeItem"
        + " where parentTypeId = ?"
        + " order by typeOrder";

    /**
     *  Return all the researcher types by type order.
     *  @param parentId the parent id, if not null
     *  @return a list of items
     */
    public List<ResearcherTypeItem> findAllInOrder(Integer parentId) {
        Object[] params = {parentId};
        List<ResearcherTypeItem> itemList = null;
        if (parentId != null) {
            itemList = getHibernateTemplate().find(FIND_ALL_IN_ORDER_BY_PARENT, params);
        } else {
            itemList = getHibernateTemplate().find(FIND_ALL_IN_ORDER);
        }
        return itemList;
    }

    /** HQL query for the getNextOrderValue method. */
    private static final String GET_NEXT_ORDER_VALUE
            = "SELECT max(typeOrder) FROM ResearcherTypeItem";

    /**
     * Get the next order value for researcher types.
     * @return 1 if no types found, the next order value of the max order found
     */
    public Integer getNextOrderValue() {
        int value = 0;
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
