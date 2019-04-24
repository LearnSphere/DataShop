/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.ProblemEventItem;
import edu.cmu.pslc.datashop.xml.ContextMessage;

/**
 * ProblemEvent Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 9891 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-09-05 14:40:47 -0400 (Thu, 05 Sep 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProblemEventDao extends AbstractDao {

    /**
     * Standard get for a ProblemEventItem by id.
     * @param id The id of the ProblemEventItem.
     * @return the matching ProblemEventItem or null if none found
     */
    ProblemEventItem get(Long id);

    /**
     * Standard find for an ProblemEventItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ProblemEventItem.
     * @return the matching ProblemEventItem.
     */
    ProblemEventItem find(Long id);

    /**
     * Standard "find all" for ProblemEventItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Determines if a context message already exists for the
     * same student, problem, and time.
     * @param contextMessage the ContextMessageItem
     * @param messageItem the MessageItem
     * @return true if a context message already exists for the
     *  same student, problem, and time; false, otherwise.
     */
    boolean isEventRepresented(ContextMessage contextMessage, MessageItem messageItem);

}
