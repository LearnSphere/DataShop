/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.dao.OliDiscussionDao;
import edu.cmu.pslc.datashop.item.OliDiscussionItem;

/**
 * Authorization Dao Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliDiscussionDaoHibernate extends AbstractDaoHibernate
     implements OliDiscussionDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Hibernate/Spring get for a selection item by id.
     * @param id the id of the desired project item
     * @return the matching ProjectItem or null if none found
     */
    public OliDiscussionItem get(Long id) {
        return (OliDiscussionItem)get(OliDiscussionItem.class, id);
    }
    /**
     * Hibernate/Spring find for a selection item by id.
     * @param id id of the object to find
     * @return ProjectItem
     */
    public OliDiscussionItem find(Long id) {
        return (OliDiscussionItem)find(OliDiscussionItem.class, id);
    }

    /**
     * Hibernate/Spring "find all" for selection items.
     * @return a List of objects which are ProjectItems
     */
    public List findAll() {
        return findAll(OliDiscussionItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Use a native SQL query to get OLI discussion data from the OLI log database.
     * @return a list of OliDiscussionItem objects
     */
    public List getDiscussionData() {
        List discussionList = new ArrayList();
        Session session = getSession();
        List sessionList = session.createSQLQuery(NativeQueryGetData.QUERY)
                .addScalar("user_id", Hibernate.STRING)
                .addScalar("session_tag", Hibernate.STRING)
                .addScalar("discussion_time", Hibernate.STRING)
                .addScalar("time_zone", Hibernate.STRING)
                .addScalar("action_guid", Hibernate.STRING)
                .addScalar("info", Hibernate.TEXT)
                .list();
        for (Iterator iter = sessionList.iterator(); iter.hasNext();) {
            Object[] row = (Object[])iter.next();

            int x = 0;
            String userId = (String)row[x]; x++;
            String sessionTag = (String)row[x]; x++;
            String timeString = (String)row[x]; x++;
            Date discussionTime = getDateStd(timeString);
            String timeZone = (String)row[x]; x++;
            String actionGuid = (String)row[x]; x++;
            String info = (String)row[x]; x++;


            OliDiscussionItem item = new OliDiscussionItem();
            item.setUserId(userId);
            item.setSessionTag(sessionTag);
            item.setDiscussionTime(discussionTime);
            item.setTimeZone(timeZone);
            item.setActionGuid(actionGuid);

            item = parseInfo(item, info);
            if (item != null) {
                discussionList.add(item);
            }
        }
        releaseSession(session);
        return discussionList;
    }

    /** Inner class to hold the Native SQL Query strings. */
    private static final class NativeQueryGetData {

        /** Private constructor. */
        private NativeQueryGetData() { }

        /** Log Database. */
        private static final String LOG_DB =
            HibernateDaoFactory.DEFAULT.getLogDatabaseName();

        /** Query string. */
        private static final String QUERY =
            "SELECT session.user_id as user_id, "
            + " session.user_sess as session_tag, "
            + " la.time as discussion_time, "
            + " la.timezone as time_zone, "
            + " la.guid as action_guid, "
            + " la.info as info"
            + " FROM " + LOG_DB + ".log_act la, " + LOG_DB + ".log_sess session"
            + " LEFT JOIN oli_discussion d"
            + " ON la.guid = d.action_guid"
            + " WHERE d.action_guid IS NULL"
            + " AND la.sess_ref = session.user_sess"
            + " AND la.source = 'DISCUSSION_ACTIVITY'"
            + " AND la.action = 'POST_MESSAGE'"
            + " ORDER by session.user_id, la.sess_ref, la.info";
    }

    /**
     * Parse the info field of a discussion post message as its in an XML format.
     * @param item the item so far
     * @param info the info field
     * @return the item with the rest of the fields filled in
     */
    private OliDiscussionItem parseInfo(OliDiscussionItem item, String info) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(new StringReader(info));
            Element root = doc.getRootElement();

            if (root.getName().equals("post")) {
                String postGuid = root.getAttributeValue("guid");
                String threadGuid = root.getAttributeValue("thread_guid");

                String starsValue = root.getAttributeValue("stars");
                Integer stars = new Integer(starsValue);

                String hiddenFlagValue = root.getAttributeValue("hidden");
                Boolean hiddenFlag = new Boolean(hiddenFlagValue);

                //TODO is the date created value useful? or redundant with time of post?
                //String dateCreatedValue = root.getAttributeValue("date_created");

                String dateAcceptedValue = root.getAttributeValue("date_accepted");
                Date acceptedDate = getDateXml(dateAcceptedValue);

                String subject = root.getChildTextTrim("subject");

                String body = root.getChildTextTrim("body");

                item.setPostGuid(postGuid);
                item.setThreadGuid(threadGuid);
                item.setStars(stars);
                item.setHiddenFlag(hiddenFlag);
                item.setAcceptedDate(acceptedDate);
                item.setSubject(subject);
                item.setBody(body);

            } else {
                logger.warn("Discussion info did not contain the proper root element."
                        + " For guid " + item.getActionGuid() + " Received " + info);
            }

        } catch (JDOMException exception) {
            logger.error("BAD GUID is " + item.getActionGuid()
                    + " JDOMException occurred. " + exception.getMessage()
                    + " Discussion info did not contain the post root element"
                    + " for guid " + item.getActionGuid() + " Received " + info);
            item = null;
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage(), exception);
            item = null;
        }
        return item;
    }

    /** Formatter to interpret time field in the format "MM/dd/yyyy HH:mm a". */
    private static final DateFormat DATE_FMT_XML = new SimpleDateFormat("MM/dd/yyyy HH:mm a");

    /**
     * Quick, little date utility method to convert strings from XML.
     * @param dateString the string as a date
     * @return a java.util.Date
     */
    private Date getDateXml(String dateString) {
        if (dateString == null) { return null; }
        synchronized (DATE_FMT_XML) {
            Date timeStamp = DATE_FMT_XML.parse(dateString, new ParsePosition(0));
            return timeStamp;
        }
    }

}
