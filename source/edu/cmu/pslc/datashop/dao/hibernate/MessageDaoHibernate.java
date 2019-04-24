/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dto.ToolTutorMessageItemPair;
import edu.cmu.pslc.datashop.dto.UserSession;
import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * Hibernate and Spring implementation of the MessageDao.
 *
 * @author Hui Cheng
 * @version $Revision: 7797 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-07-27 08:37:42 -0400 (Fri, 27 Jul 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MessageDaoHibernate extends AbstractDaoHibernate implements MessageDao {

    /**
     * Standard get for a MessageItem by id.
     * @param id The id of the message.
     * @return the matching MessageItem or null if none found
     */
    public MessageItem get(Long id) {
        return (MessageItem)get(MessageItem.class, id);
    }

    /**
     * Standard "find all" for message items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(MessageItem.class);
    }

    /**
     * Standard find for an MessageItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired MessageItem.
     * @return the matching MessageItem.
     */
    public MessageItem find(Long id) {
        return (MessageItem)find(MessageItem.class, id);
    }

    /**
     * Get the distinct user/session pairs from the message table.
     * @return a list of UserSession objects
     */
    public List getDistinctUserSessions() {
        List userSessionList = new ArrayList();
        String query = "select distinct item.userId, item.sessionTag "
                        + "from MessageItem as item "
                        + "where (item.processedFlag is null or item.processedFlag "
                        + "= '" + MessageItem.SUCCESS_QUESTIONABLE_FLAG + "') "
                        + "and item.messageType = 'context' "
                        + "order by item.userId, item.time, item.sessionTag ";
        List sessionList = getHibernateTemplate().find(query);
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            Object[] row = (Object[])iter.next();
            UserSession userSession = new UserSession((String)row[0], (String)row[1]);
            userSessionList.add(userSession);
        }
        return userSessionList;
    }

    /**
     * Get the distinct user/session pairs from the message table where ERRORs have occurred
     * in the past.
     * @return a list of UserSession objects with ERROR in processed flag field
     */
    public List getDistinctUserSessionsWithError() {
        List userSessionList = new ArrayList();
        String query = "select distinct item.userId, item.sessionTag "
                        + "from MessageItem as item "
                        + "where item.processedFlag "
                        + "= '" + MessageItem.ERROR_FLAG + "' "
                        + "and item.messageType = 'context' "
                        + "order by item.userId, item.time, item.sessionTag ";
        List sessionList = getHibernateTemplate().find(query);
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            Object[] row = (Object[])iter.next();
            UserSession userSession = new UserSession((String)row[0], (String)row[1]);
            userSessionList.add(userSession);
        }
        return userSessionList;
    }

    /** String for the query.  Check for new (null) or recheck in the processed flag field. */
    private static final String PROCESSED_FLAG_NEW_RECHECK =
        "and (processedFlag is null or processedFlag = '"
        + MessageItem.SUCCESS_QUESTIONABLE_FLAG + "') ";
    /** String for the query.  Check for error in the processed flag field. */
    private static final String PROCESSED_FLAG_ERROR =
        "and (processedFlag = '" + MessageItem.ERROR_FLAG + "') ";

    /**
     * Get a list of messageItems that are type of context from the message table
     * with the given userId and sessionTag.
     * @param userId the user id
     * @param sessionTag the session id
     * @param getErrorMsgsFlag if true, get context messages with an error to try them again
     * @return a list of MessageItem objects
     */
    public List getContextMessageItems(String userId, String sessionTag, boolean getErrorMsgsFlag) {

        String processedFlagString = PROCESSED_FLAG_NEW_RECHECK;

        if (getErrorMsgsFlag) {
            processedFlagString = PROCESSED_FLAG_ERROR;
        }
        List contextMessageList = new ArrayList();
        Session session = getSession();
        String query = "from MessageItem "
            + " where userId = :userId "
            + " and sessionTag = :sessionTag "
            + " and messageType = :messageType "
            + processedFlagString
            + " order by time ";
        Query q = session.createQuery(query);
        q.setParameter("userId", userId);
        q.setParameter("sessionTag", sessionTag);
        q.setParameter("messageType", "context");
        List sessionList = q.list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            MessageItem mi = (MessageItem)iter.next();
            contextMessageList.add(mi);
        }
        releaseSession(session);
        return contextMessageList;
    }

    /**
     * Get a pair of message items that are tool and tutor messages and
     * are joined on their transaction IDs.
     * Do a native query as its the fastest to code.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    public List getToolTutorMessages(String userId, String sessionTag, String contextMessageId) {
        List pairList = new ArrayList();
        Session session = getSession();
        String query = NativeQueryToolTutorJoin.QUERY;
        List sessionList = session.createSQLQuery(query)
            .addEntity(NativeQueryToolTutorJoin.TOOL_ENTITY, MessageItem.class)
            .addEntity(NativeQueryToolTutorJoin.TUTOR_ENTITY, MessageItem.class)
            .setString(NativeQueryToolTutorJoin.USER_ID_PARAM, userId)
            .setString(NativeQueryToolTutorJoin.SESSION_TAG_PARAM, sessionTag)
            .setString(NativeQueryToolTutorJoin.CXT_MSG_ID_PARAM, contextMessageId)
            .list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            Object[] row = (Object[])iter.next();
            ToolTutorMessageItemPair pair =
                new ToolTutorMessageItemPair((MessageItem)row[0], (MessageItem)row[1]);
            pairList.add(pair);
        }
        releaseSession(session);
        return pairList;
    }

    /**
     * Gets tool messages w/o a matching tutor message.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    public List getToolMessages(String userId, String sessionTag, String contextMessageId) {
        return getMessages(userId, sessionTag, contextMessageId, MessageItem.MSG_TYPE_TOOL);
    }

    /**
     * Gets tutor messages matching the criteria and not processed.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a pair of tool and tutor message items
     */
    public List getTutorMessages(String userId, String sessionTag, String contextMessageId) {
        return getMessages(userId, sessionTag, contextMessageId, MessageItem.MSG_TYPE_TUTOR);
    }

    /**
     * Gets messages matching the criteria.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @param messageType the messageType.
     * @return a list of message items
     */
    public List getMessages(String userId, String sessionTag,
            String contextMessageId, String messageType) {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for messages matching: "
                    + "\n\tuserId: " +  userId
                    + "\n\tsessionTag: " +  sessionTag
                    + "\n\tcontextMessageId: " +  contextMessageId
                    + "\n\tmessageType: " +  messageType);
        }

        DetachedCriteria query = DetachedCriteria.forClass(MessageItem.class);
        query.add(Restrictions.eq("userId", userId));
        query.add(Restrictions.eq("sessionTag", sessionTag));
        query.add(Restrictions.eq("contextMessageId", contextMessageId));
        query.add(Restrictions.eq("messageType", messageType));
        query.add(Restrictions.isNull("processedFlag"));

        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets non-context messages matching the criteria,
     * so that we can mark the messages that go with a given context message.
     * @param userId the user id
     * @param sessionTag the session id
     * @param contextMessageId the context message id
     * @return a list of message items
     */
    public List getNonContextMessages(String userId, String sessionTag,
            String contextMessageId) {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for messages matching: "
                    + "\n\tuserId: " +  userId
                    + "\n\tsessionTag: " +  sessionTag
                    + "\n\tcontextMessageId: " +  contextMessageId);
        }

        DetachedCriteria query = DetachedCriteria.forClass(MessageItem.class);
        query.add(Restrictions.eq("userId", userId));
        query.add(Restrictions.eq("sessionTag", sessionTag));
        query.add(Restrictions.eq("contextMessageId", contextMessageId));
        query.add(Restrictions.ne("messageType", MessageItem.MSG_TYPE_CONTEXT));
        query.add(Restrictions.isNull("processedFlag"));

        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets the minimum time for messages all userId, sessionTag, and context message id.
     * @param userId the user id
     * @param contextMessageId the context message id
     * @return the minimum time or null if none exists
     */
    public Date getMessageMinTime(String userId, String contextMessageId) {
        Object[] params = {userId, contextMessageId};

        String query = "select min(time) as minTime from MessageItem as msg"
                        + " where msg.userId = ? "
                        + " and msg.contextMessageId = ? "
                        + " group by msg.contextMessageId";

        List dates = getHibernateTemplate().find(query, params);
        Date minTime = null;
        if (dates != null && dates.size() == 1) {
            minTime = (Date)dates.get(0);
        }

        return minTime;
    }

    /** Inner class to hold the Native SQL Query strings. */
    private static final class NativeQueryToolTutorJoin {
        /** Tool Message Entity. */
        private static final String TOOL_ENTITY = "tool";
        /** Tutor Message Entity. */
        private static final String TUTOR_ENTITY = "tutor";
        /** User Id Parameter. */
        private static final String USER_ID_PARAM = "userId";
        /** Session Id Parameter. */
        private static final String SESSION_TAG_PARAM = "sessionTag";
        /** Context Message Id Parameter. */
        private static final String CXT_MSG_ID_PARAM = "contextMessageId";

        /** Private constructor, utility class. */
        private NativeQueryToolTutorJoin() { }

        /** Query string. */
        private static final String QUERY =
            "SELECT {" + TOOL_ENTITY + ".*}, {" + TUTOR_ENTITY + ".*}"
                + " FROM message " + TOOL_ENTITY + ""
                + " LEFT JOIN message " + TUTOR_ENTITY
                + " ON (" + TOOL_ENTITY  + ".transaction_id = "
                          + TUTOR_ENTITY + ".transaction_id "
                + " AND " + TOOL_ENTITY  + ".user_id = "
                          + TUTOR_ENTITY + ".user_id"
                + " AND " + TOOL_ENTITY  + ".session_tag = "
                          + TUTOR_ENTITY + ".session_tag"
                + " AND " + TOOL_ENTITY  + ".context_message_id = "
                          + TUTOR_ENTITY + ".context_message_id)"
                + " WHERE " + TOOL_ENTITY  + ".processed_flag IS NULL"
                + " AND "   + TOOL_ENTITY  + ".message_type = 'tool'"
                + " AND "   + TUTOR_ENTITY + ".message_type = 'tutor'"
                + " AND "   + TOOL_ENTITY  + ".user_id = :" + USER_ID_PARAM
                + " AND "   + TOOL_ENTITY  + ".session_tag = :" + SESSION_TAG_PARAM
                + " AND "   + TOOL_ENTITY  + ".context_message_id = :" + CXT_MSG_ID_PARAM
                + " ORDER BY " + TOOL_ENTITY + ".time";
    } // end inner class

}
