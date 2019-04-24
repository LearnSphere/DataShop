/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.SetDao;
import edu.cmu.pslc.datashop.item.SetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Hibernate and Spring implementation of the SetDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4584 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-03-25 14:25:54 -0400 (Tue, 25 Mar 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SetDaoHibernate extends AbstractDaoHibernate implements SetDao {

    /**
     * Standard get for a SetItem by id.
     * @param id The id of the user.
     * @return the matching SetItem or null if none found
     */
    public SetItem get(Integer id) {
        return (SetItem)get(SetItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SetItem.class);
    }

    /**
     * Standard find for an SetItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SetItem.
     * @return the matching SetItem.
     */
    public SetItem find(Integer id) {
        return (SetItem)find(SetItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant. */
    private static final int FIND_NUM_PARAMS = 3;

    /**
     * Return a list of SetItems for the given skill model and set name, though
     * there should not be more than one.
     * @param skillModelItem the given skill model
     * @param setName the given set name
     * @return a SetItem if it is found, null otherwise
     */
    public List find(SkillModelItem skillModelItem, String setName) {
        String query = "select distinct si"
            + " from SetItem si"
            + " join si.skills sk"
            + " where si.type = ?"
            + " and si.name = ?"
            + " and sk.skillModel.id = ?";

        Object [] params = new Object[FIND_NUM_PARAMS];
        params[0] = SetItem.SET_TYPE_SKILL;
        params[1] = setName;
        params[2] = skillModelItem.getId();

        return getHibernateTemplate().find(query, params);
    }

    /**
     * Finds a list of skill sets for the given skill model.
     * @param skillModelItem the given skill model
     * @return a list of SetItems
     */
    public List findSkillSets(SkillModelItem skillModelItem) {
        String query = "select distinct si"
                     + " from SetItem si"
                     + " join si.skills sk"
                     + " where si.type = ?"
                     + " and sk.skillModel.id = ?"
                     + " order by si.name";

        Object [] params = new Object[2];
        params[0] = SetItem.SET_TYPE_SKILL;
        params[1] = skillModelItem.getId();

        return getHibernateTemplate().find(query, params);
    }

}
