/* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.mapping.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.hibernate.ItemDaoHibernate;

import edu.cmu.pslc.datashop.mapping.dao.StudentDao;
import edu.cmu.pslc.datashop.mapping.item.StudentItem;

/**
 * Hibernate and Spring implementation of the StudentDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14382 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-17 12:46:02 -0400 (Tue, 17 Oct 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StudentDaoHibernate extends ItemDaoHibernate<StudentItem> implements StudentDao {
    /**
     * The class to use in find and get methods.
     * @return StudentItem.class
     */
    protected Class<StudentItem> getItemClass() {
        return StudentItem.class;
    }

    /**
     * Returns StudentItem given a name.
     * @param name name of the StudentItem
     * @return a collection of StudentItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from StudentItem student where student.actualUserId = ?", name);
    }

    /**
     * Returns StudentItem given a name, case insensitive.
     * @param name name of the StudentItem
     * @return a collection of StudentItems
     */
    public Collection findIgnoreCase(String name) {
        return getHibernateTemplate().find("from StudentItem student where lower(student.actualUserId) = ?",
                                           name.toLowerCase());
    }

    /**
     * Standard find for a mapped StudentItem by original id.
     * @param id the id of the original StudentItem.
     * @return the matching StudentItem.
     */
    public StudentItem findByOriginalId(Long id) {
        List<StudentItem> result = 
            getHibernateTemplate().find(
                "from StudentItem student where student.originalId = ?", id);
        if ((result == null) || (result.size() == 0)) { return null; }

        return result.get(0);
    }
}
