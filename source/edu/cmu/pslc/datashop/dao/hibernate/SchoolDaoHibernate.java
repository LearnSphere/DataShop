/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dao.SchoolDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SchoolItem;

/**
 * Hibernate and Spring implementation of the SchoolDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SchoolDaoHibernate extends AbstractDaoHibernate implements SchoolDao {

    /**
     * Standard get for a SchoolItem by id.
     * @param id The id of the user.
     * @return the matching SchoolItem or null if none found
     */
    public SchoolItem get(Integer id) {
        return (SchoolItem)get(SchoolItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SchoolItem.class);
    }

    /**
     * Standard find for an SchoolItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SchoolItem.
     * @return the matching SchoolItem.
     */
    public SchoolItem find(Integer id) {
        return (SchoolItem)find(SchoolItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns SchoolItem given a name.
     * @param name name of the SchoolItem
     * @return a collection of SchoolItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from SchoolItem school where school.schoolName = ?", name);
    }

    /** HQL Query string for the findMatchingByName method. */
    private static final String FIND_SCHOOL_BY_MATCHING_NAME_QUERY
        = "select distinct school"
         + " from TransactionItem as transaction"
         + " join transaction.school as school"
         + " join transaction.dataset as dataset"
         + " where dataset = ?"
         + " and school.schoolName like ?"
         + " order by school.schoolName";

    /**
     * Gets a list of schools in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match the Anon Id too.
     * @param datasetItem the dataset item to find schools in.
     * @param matchAny boolean value indicating whether to only look for schools that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching school items sorted by name.
     */
    public List findMatchingByName(String toMatch, DatasetItem datasetItem, boolean matchAny) {
        String schoolName = toMatch + "%";
        if (matchAny) {
            schoolName = "%" + schoolName;
        }

        Object[] params = new Object[2];
        params[0] = datasetItem;
        params[1] = schoolName;

        return getHibernateTemplate().find(FIND_SCHOOL_BY_MATCHING_NAME_QUERY, params);
    }
}
