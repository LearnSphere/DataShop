/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * Message Data Access Object Interface.
 *
 * @author Hui Cheng
 * @version $Revision: 7797 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-07-27 08:37:42 -0400 (Fri, 27 Jul 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface MessageDao extends AbstractDao {

    /**
     * Standard get for a MessageItem by id.
     * @param id The id of the MessageItem.
     * @return the matching MessageItem or null if none found
     */
    MessageItem get(Long id);

    /**
     * Standard find for an MessageItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired MessageItem.
     * @return the matching MessageItem.
     */
    MessageItem find(Long id);

    /**
     * Standard "find all" for MessageItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Get the distinct user/session pairs from the message table.
     * @return a list of UserSession objects
     */
    List getDistinctUserSessions();

    /**
     * Get the distinct user/session pairs from the message table where ERRORs have occurred
     * in the past.
     * @return a list of UserSession objects with ERROR in processed flag field
     */
    List getDistinctUserSessionsWithError();

    /**
     * Get a list of messageItems that are type of context from the message table
     * with the given userId and sessionTag.
     * @param userId the user id
     * @param sessionTag the session id
     * @param getErrorMsgsFlag if true, get context messages with an error to try them again
     * @return a list of MessageItem objects
     */
    List getContextMessageItems(String userId, String sessionTag, boolean getErrorMsgsFlag);

    /**
     * Get a pair of message items that are tool and tutor messages and
     * are joined on their transaction IDs.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    List getToolTutorMessages(String userId, String sessionTag, String contextMessageId);

    /**
     * Gets tool messages w/o a matching tutor message.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    List getToolMessages(String userId, String sessionTag, String contextMessageId);

    /**
     * Gets tutor messages w/o a matching tool message.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    List getTutorMessages(String userId, String sessionTag, String contextMessageId);

    /**
     * Gets tutor messages matching the criteria.  Only non processed messages
     * whose serverReceiptTime is null or whose difference with the current time is
     * greater than the CONFIDENCE_TIME.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @param messageType the messageType.
     * @return a pair of tool and tutor message items
     */
    List getMessages(String userId, String sessionTag, String contextMessageId, String messageType);

    /**
     * Gets non-context messages matching the criteria,
     * so that we can mark the messages that go with a given context message.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a list of message items
     */
    List getNonContextMessages(String userId, String sessionTag, String contextMessageId);

    /**
     * Get the minimum time for messages all given userId, sessionTag, and context message id.
     * @param userId the user id
     * @param contextMessageId the context message id
     * @return the minimum time or null if none exists
     */
    Date getMessageMinTime(String userId, String contextMessageId);
}
