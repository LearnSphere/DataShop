/* Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.StudentDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.StudentItem;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Hibernate and Spring implementation of the StudentDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14350 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-10-10 09:10:23 -0400 (Tue, 10 Oct 2017) $
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
     * Find all students in a dataset.
     * @param dataset the DatasetItem to get students for.
     * @return a Collection of all students in the dataset.
     */
    public List find(DatasetItem dataset) {
        return getHibernateTemplate().find(
                "select distinct stud "
              + "from StudentItem stud "
              + "join stud.sessions sess "
              + "where sess.dataset = ?", dataset);
    }

    /**
     * Standard delete, extended to delete mapped student.
     * @param obj the object to delete
     */
    @Override
    public void delete(Item obj) {
        // Get id of item to be deleted and use it to find mapped student.
        Long studentId = (Long)obj.getId();

        logDebug("Deleting student: " + studentId);

        edu.cmu.pslc.datashop.mapping.dao.StudentDao mappedDao =
            edu.cmu.pslc.datashop.mapping.dao.DaoFactory.HIBERNATE.getStudentDao();

        edu.cmu.pslc.datashop.mapping.item.StudentItem mappedStudent =
            mappedDao.findByOriginalId(studentId);
        if (mappedStudent != null) {
            logDebug("Deleting mapped student: " + mappedStudent);
            delete(mappedStudent);
        }

        super.delete(obj);
    }

    /**
     * Find student with specified anon_user_id.
     *
     * @param anonUserId the string to match
     * @return the matching student item
     */
    public List findByAnonId(String anonUserId) {
        return getHibernateTemplate().find(
                "from StudentItem student where student.anonymousUserId = ?", anonUserId);
    }

    /**
     * Gets a list of students in the dataset that match all or a portion of the
     * AnonId parameter.
     * @param toMatch A string to match the Anon Id too.
     * @param dataset the dataset item to find students in.
     * @param matchAny boolean value indicating whether to only look for students that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching student items sorted by Anon Id.
     */
    public List findMatchingByAnonId(String toMatch, DatasetItem dataset, boolean matchAny) {

        DetachedCriteria query = DetachedCriteria.forClass(StudentItem.class);

        if (matchAny) {
            query.add(Restrictions.ilike("anonymousUserId", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("anonymousUserId", toMatch, MatchMode.START));
        }
        query.createCriteria("sessions")
             .add(Restrictions.eq("dataset", dataset));
        query.addOrder(Property.forName("anonymousUserId").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Returns a bunch of students where the anonymous user id is blank, which are
     * created by the FFI when anonFlag is true.
     * @return a collection of StudentItems
     */
    public List<StudentItem> findByEmptyAnonUserId() {
        return getHibernateTemplate().find(
                "from StudentItem student where student.anonymousUserId = ''");
    }

    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }
}
