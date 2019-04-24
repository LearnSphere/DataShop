/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.InputDao;
import edu.cmu.pslc.datashop.item.InputItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Hibernate and Spring implementation of the InputDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InputDaoHibernate extends AbstractDaoHibernate implements InputDao {

    /**
     * Standard get for a InputItem by id.
     * @param id The id of the user.
     * @return the matching InputItem or null if none found
     */
    public InputItem get(Long id) {
        return (InputItem)get(InputItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(InputItem.class);
    }

    /**
     * Standard find for an InputItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired InputItem.
     * @return the matching InputItem.
     */
    public InputItem find(Long id) {
        return (InputItem)find(InputItem.class, id);
    }

    //
    // non-standard methods.
    //

    /**
     * Find all inputs for a given subgoal and input name.
     * @param subgoalItem the SubgoalItem this action belongs to.
     * @param input input name
     * @return list of inputs.
     */
    public List findBySubgoalAndName(SubgoalItem subgoalItem, String input) {
        String query = "select distinct ipt from InputItem ipt"
            + " join ipt.subgoal sub"
            + " where sub.id = ? and input = ?";
        return getHibernateTemplate().find(
                query,  new Object[] {subgoalItem.getId(), input});
    }
}
