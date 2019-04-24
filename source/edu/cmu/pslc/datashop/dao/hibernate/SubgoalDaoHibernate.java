/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Hibernate and Spring implementation of the SubgoalDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6879 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-04-21 10:29:13 -0400 (Thu, 21 Apr 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SubgoalDaoHibernate extends AbstractDaoHibernate implements SubgoalDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a SubgoalItem by id.
     * @param id The id of the user.
     * @return the matching SubgoalItem or null if none found
     */
    public SubgoalItem get(Long id) {
        return (SubgoalItem)get(SubgoalItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SubgoalItem.class);
    }

    /**
     * Standard find for an SubgoalItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SubgoalItem.
     * @return the matching SubgoalItem.
     */
    public SubgoalItem find(Long id) {
        return (SubgoalItem)find(SubgoalItem.class, id);
    }

    /**
     * Get a list of all subgoals in a dataset ordered by problem, dataset, subgoal name.
     * @param dataset the dataset to get the subgoals in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all subgoals in the dataset between the offset and the limit.
     */
    public List find(DatasetItem dataset, Integer limit, Integer offset) {

        String query = "select distinct sub from SubgoalItem sub"
            + " join sub.problem prob"
            + " join prob.datasetLevel lev"
            + " where lev.dataset.id = " + dataset.getId()
            + " order by prob.datasetLevel.levelName, prob.problemName, sub.subgoalName";

        CallbackCreatorHelper helperCreator = new CallbackCreatorHelper(query.toString(),
                offset.intValue(), limit.intValue());

        HibernateCallback callback = helperCreator.getCallback();
        if (logger.isDebugEnabled()) {
            logger.debug("querying with callback :: Limit: "
                    + limit + " Offset: " + offset + " query: "  + query);
        }
        List results = getHibernateTemplate().executeFind(callback);

        return results;
    }

    /**
     * Return a saved item.  If the new item is not in the list, then save it.
     * If it is, then find the existing item and return that.
     * @param collection the collection to search
     * @param newItem the new item
     * @return an existing item
     */
    public Item findOrCreate(Collection collection, Item newItem)  {
        boolean found = false;

        SubgoalItem newSubgoalItem = (SubgoalItem)newItem;

        for (Object existingItem : collection) {
            SubgoalItem existingSubgoalItem = (SubgoalItem)existingItem;

            //check only the fields we care about, but not GUID
            if (Item.objectEquals(existingSubgoalItem.getSubgoalName(),
                    newSubgoalItem.getSubgoalName())
                    && Item.objectEquals(existingSubgoalItem.getInputCellType(),
                            newSubgoalItem.getInputCellType())
                    && Item.objectEqualsFK(existingSubgoalItem.getProblem(),
                            newSubgoalItem.getProblem())) {
                found = true;
                newItem = existingSubgoalItem;
                break;
            }
        }

        if (!found) {
            if (logger.isDebugEnabled()) {
                logger.debug("findOrCreate: creating new item: " + newSubgoalItem);
                logger.debug("findOrCreate: as its not found in collection of size: "
                        + collection.size());
            }
            newSubgoalItem.setGuid(generateGUID(newSubgoalItem));
            saveOrUpdate(newSubgoalItem);
            newItem = newSubgoalItem;
        }
        return newItem;
    }

    /**
     * Gets a list of subgoals in the problem.
     * @param problem the problem to get all subgoals for.
     * @return List of all subgoals.
     */
    public List find(ProblemItem problem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting subgoals for problem : " + problem);
        }

        String query = "select distinct sub from SubgoalItem sub"
            + " join sub.problem prob"
            + " where prob.id = ?";
        return getHibernateTemplate().find(query, problem.getId());
    }

    /** Hibernate query to get a subgoal by dataset and GUID */
    private static final String GET_BY_GUID_QUERY =
        "select distinct sub from SubgoalItem sub"
        + " join sub.problem prob"
        + " join prob.datasetLevel level"
        + " where level.dataset = ?"
        + " and sub.guid = ?";

    /**
     * Gets a subgoal based on a step tag and a dataset.
     * @param dataset the Dataset to get the step tag for.
     * @param guid GUID generated for each step.
     * @return matching SubgoalItem, null if none found.
     */
    public SubgoalItem find(DatasetItem dataset, String guid) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting subgoal by guid: " + guid + " and dataset: " + dataset.getId());
        }

        Object [] params = new Object [2];
        params[0] = dataset;
        params[1] = guid;

        List results = getHibernateTemplate().find(GET_BY_GUID_QUERY, params);
        if (results.size() > 1) {
            logger.warn("More than one subgoal returned for guid: " + guid
                    + " and dataset: " + dataset.getId());
        }

        if (results.size() == 0) { return null; }
        return (SubgoalItem)results.get(0);
    }

    /** Query to get the information necessary for a step GUID. */
    private static final String GET_STEP_DATA_QUERY =
          "SELECT prob.problem_name as probName, "
        + " prob.problem_description as probDesc, "
        + " prob.tutor_flag as probTutorFlag, "
        + " prob.tutor_other as probTutorOther, "
        + " (select group_concat(DISTINCT CONCAT( "
        + "     IF(dl2.level_title IS NOT NULL, CONCAT('(', dl2.level_title, ') '), ''), "
        + "     dl2.level_name) ORDER BY dl2.lft SEPARATOR ', ') "
        + "   FROM dataset_level dl2 "
        + "   WHERE dl2.lft <= dl.lft AND dl2.rgt >= dl.rgt "
        + "   AND dl.dataset_id = dl2.dataset_id "
        + "   GROUP BY dl2.dataset_id) as probHierarchy "
        + "FROM problem prob "
        + "  JOIN dataset_level dl on dl.dataset_level_id = prob.dataset_level_id "
        + "WHERE prob.problem_id = :problemId ";

    /** Step data results index */
    private static final int PROB_NAME_INDEX = 0;
    /** Step data results index */
    private static final int PROB_DESC_INDEX = 1;
    /** Step data results index */
    private static final int PROB_FLAG_INDEX = 2;
    /** Step data results index */
    private static final int PROB_OTHER_INDEX = 3;
    /** Step data results index */
    private static final int PROB_HIER_INDEX = 4;

    /** Collection of allowed HEX chars */
    private static final char[] HEX_CHARS =
        {'0', '1', '2', '3',
         '4', '5', '6', '7',
         '8', '9', 'a', 'b',
         'c', 'd', 'e', 'f', };

    /**
     * Generates a unique identifier for a step in a given dataset.  This function
     * always generates the same GUID for a step given that the step name, problem name,
     * and problem hierarchy string stay consistent.
     * @param subgoal The subgoal to generate a unique identifier for.
     * @return String that is the identifier.
     */
    public String generateGUID(SubgoalItem subgoal) {

        StringBuffer stepGUID = new StringBuffer();
        Session session = null;
        List <Object[]> queryResults = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(GET_STEP_DATA_QUERY);

            //indicate which columns map to which types.
            sqlQuery.addScalar("probHierarchy", Hibernate.STRING);
            sqlQuery.addScalar("probName", Hibernate.STRING);
            sqlQuery.addScalar("probDesc", Hibernate.STRING);
            sqlQuery.addScalar("probTutorFlag", Hibernate.STRING);
            sqlQuery.addScalar("probTutorOther", Hibernate.STRING);

            //set the parameters
            sqlQuery.setLong("problemId", ((Long)subgoal.getProblem().getId()));

            //query the database
            queryResults = sqlQuery.list();
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        ///**
        if (queryResults.size() > 1) {
            logger.warn("More than one problem result returned when generating a step GUID");
        }

        if (queryResults.size() == 0) {
            logger.error("Unable to generate step guid, no problem "
                    + "and hierarchy found in the database.");
            return null;
        }

        Object[] row = queryResults.get(0);
        stepGUID.append((String)row[PROB_HIER_INDEX]);
        stepGUID.append((String)row[PROB_NAME_INDEX]);
        stepGUID.append((String)row[PROB_DESC_INDEX]);
        stepGUID.append((String)row[PROB_FLAG_INDEX]);
        stepGUID.append((String)row[PROB_OTHER_INDEX]);
        stepGUID.append(subgoal.getSubgoalName());

        //run an MD5 encryption on the resulting string and turn it into hex output.
        char[] buf = new char[0];
        final int four = 4;
        final int f = 0xf;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(stepGUID.toString().getBytes("UTF8"));
            byte [] hash  = digest.digest();
            buf = new char[hash.length * 2];
            for (int i = 0, x = 0; i < hash.length; i++) {
                buf[x++] = HEX_CHARS[(hash[i] >>> four) & f];
                buf[x++] = HEX_CHARS[hash[i] & f];
            }
        } catch (NoSuchAlgorithmException noSuchException) {
            logger.error("Unable to find MD5 Algorithm. ", noSuchException);
            return null;
        } catch (UnsupportedEncodingException unsupporetedException) {
            logger.error("Unable to find UTF8 byte encoding for strings. ", unsupporetedException);
            return null;
        }

        return new String(buf);
    }

}
