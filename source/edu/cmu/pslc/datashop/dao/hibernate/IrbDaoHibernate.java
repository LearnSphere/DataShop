/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.IrbDao;
import edu.cmu.pslc.datashop.item.IrbItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Data access object to retrieve the data from the IRB database table
 * via Hibernate.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbDaoHibernate extends AbstractDaoHibernate
    implements IrbDao {

    /**
     * Standard get for an IRB item by id.
     * @param id the id of the desired IRB item
     * @return the matching IrbItem or null if none found
     */
    public IrbItem get(Integer id) {
        return (IrbItem)get(IrbItem.class, id);
    }
    /**
     * Standard find for an IRB item by id.
     * @param id id of the object to find
     * @return IrbItem
     */
    public IrbItem find(Integer id) {
        return (IrbItem)find(IrbItem.class, id);
    }

    /**
     * Standard "find all" for IRB items.
     * @return a List of objects which are IrbItems
     */
    public List<IrbItem> findAll() {
        return getHibernateTemplate().find("from " + IrbItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Nonstandard "find all" for IRB items sorted by title.
     * @return a List of objects which are IrbItems sorted by title
     */
    public List<IrbItem> findAllSortByTitle() {
        return getHibernateTemplate().find("from " + IrbItem.class.getName()
                + " order by title");
    }

    /**
     * Find IRBs given a project.
     * @param projectItem the project
     * @return a collection of IrbItems
     */
    public List<IrbItem> findByProject(ProjectItem projectItem) {
        Object[] params = {projectItem};
        return getHibernateTemplate().find(
                "from ProjectItem project "
                + " where project.project = ?", params);
    }

    /**
     * Find all IRBs matching given search string.
     * By default, this will attempt to match on title or PI.
     * @param searchBy search string
     * @param titleOnly do not include PI in search
     * @return a collection of IrbItems
     */
    public List<IrbItem> findAllMatching(String searchBy, boolean titleOnly) {
        DetachedCriteria query = DetachedCriteria.forClass(IrbItem.class);
        if (titleOnly) {
            query.add(Restrictions.ilike("title", searchBy, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.or(Restrictions.ilike("title", searchBy, MatchMode.ANYWHERE),
                                      Restrictions.ilike("pi", searchBy, MatchMode.ANYWHERE)));
        }
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Find all IRBs matching given search string for the specified user.
     * By default, this will attempt to match on title or PI.
     * @param user the user
     * @param searchBy search string
     * @param titleOnly do not include PI in search
     * @return a collection of IrbItems
     */
    public List<IrbItem> findAllMatchingByUser(UserItem user, String searchBy, boolean titleOnly) {
        DetachedCriteria query = DetachedCriteria.forClass(IrbItem.class);
        Conjunction conjunction = Restrictions.conjunction();
        conjunction.add(Restrictions.eq("addedBy", user));
        if (titleOnly) {
            conjunction.add(Restrictions.ilike("title", searchBy, MatchMode.ANYWHERE));
        } else {
            conjunction.add(Restrictions.or(
                    Restrictions.ilike("title", searchBy, MatchMode.ANYWHERE),
                             Restrictions.ilike("pi", searchBy, MatchMode.ANYWHERE)));
        }
        query.add(conjunction);
        return getHibernateTemplate().findByCriteria(query);
    }
}
