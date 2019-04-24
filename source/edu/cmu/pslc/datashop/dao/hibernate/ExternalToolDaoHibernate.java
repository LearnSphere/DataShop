/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ExternalToolDao;
import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the ExternalToolDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7880 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-08-22 11:00:53 -0400 (Wed, 22 Aug 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolDaoHibernate extends AbstractDaoHibernate implements ExternalToolDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ExternalToolItem get(Integer id) {
        return (ExternalToolItem)get(ExternalToolItem.class, id);
    }
    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ExternalToolItem find(Integer id) {
        return (ExternalToolItem)find(ExternalToolItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ExternalToolItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant HQL. */
    private static final String FIND_BY_NAME_CONTRIB_HQL =
                "from ExternalToolItem tool "
                + " where tool.name = ?"
                + " and tool.contributor = ?";

    /**
     * Find an external tool item by name and contributor.
     * @param name the name of the external tool
     * @param userItem the user item for the contributor
     * @return an item if found, null otherwise
     */
    public ExternalToolItem findByNameAndContributor(String name, UserItem userItem) {
        ExternalToolItem toolItem = null;
        Object[] params = {name, userItem};
        List<ExternalToolItem> list = getHibernateTemplate().find(
                FIND_BY_NAME_CONTRIB_HQL, params);
        if (list.size() > 0) {
            toolItem = list.get(0);
        }
        return toolItem;
    }

    /** Constant HQL. */
    private static final String FIND_BY_CONTRIB_HQL =
            "from ExternalToolItem tool " + " where tool.contributor = ?";

    /**
     * Find a list of external tool items by contributor.
     * @param userItem the user item for the contributor
     * @return a List of item objects
     */
    public List<ExternalToolItem> findByContributor(UserItem userItem) {
        Object[] params = {userItem};
        List<ExternalToolItem> list = getHibernateTemplate().find(
                FIND_BY_CONTRIB_HQL, params);
        return list;
    }
}
