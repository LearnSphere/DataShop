/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ProblemEventDao;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.ProblemEventItem;
import edu.cmu.pslc.datashop.xml.ContextMessage;

/**
 * Hibernate and Spring implementation of the ProblemEventDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10093 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-10-04 11:41:16 -0400 (Fri, 04 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemEventDaoHibernate extends AbstractDaoHibernate implements ProblemEventDao {

    /**
     * Standard get for a ProblemEventItem by id.
     * @param id The id of the user.
     * @return the matching ProblemEventItem or null if none found
     */
    public ProblemEventItem get(Long id) {
        return (ProblemEventItem)get(ProblemEventItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ProblemEventItem.class);
    }

    /**
     * Standard find for an ProblemEventItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ProblemEventItem.
     * @return the matching ProblemEventItem.
     */
    public ProblemEventItem find(Long id) {
        return (ProblemEventItem)find(ProblemEventItem.class, id);
    }

    /** Query used in existsForOtherSession. */
    private static final String PE_QUERY = "from ProblemEventItem pe"
            + " join pe.session sess"
            + " where sess.student = ?"
            + " and pe.problem = ?"
            + " and pe.startTime = ?"
            + " and pe.eventFlag = 0";
    /**
     * Determines if a context message already exists for the
     * same student, problem, and time.
     * @param contextMessage the ContextMessageItem
     * @param messageItem the MessageItem
     * @return true if a context message already exists for the
     *  same student, problem, and time; false, otherwise.
     */
    public boolean isEventRepresented(ContextMessage contextMessage, MessageItem messageItem) {

        boolean duplicatesExist = false;
        Object[] params = {
            contextMessage.getStudentItem(),
            contextMessage.getProblemItem(),
            messageItem.getTime()
        };

        // A list of problem event (event_flag = 0) with the same student, problem, and start time
        List<ProblemEventItem> problemEvents = getHibernateTemplate().find(PE_QUERY, params);

        if (problemEvents != null && !problemEvents.isEmpty()) {
            duplicatesExist = true;
        }
        return duplicatesExist;
    }

}
