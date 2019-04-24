/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.WorkflowTagDao;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;

/**
 * WorkflowTag Data Access Object Implementation.
 *
 * @author Cindy Tipper
 * @version $Revision: 15760 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-12-14 13:23:22 -0500 (Fri, 14 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowTagDaoHibernate
    extends AbstractDaoHibernate<WorkflowTagItem> implements WorkflowTagDao {

    /**
     * Standard get for a WorkflowTagItem by id.
     * @param id The id of the WorkflowTagItem
     * @return the matching WorkflowTagItem or null if none found
     */
    public WorkflowTagItem get(Long id) {
        return (WorkflowTagItem)get(WorkflowTagItem.class, id);
    }

    /**
     * Standard find for a WorkflowTagItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired WorkflowTagItem.
     * @return the matching WorkflowTagItem.
     */
    public WorkflowTagItem find(Long id) {
        return (WorkflowTagItem)find(WorkflowTagItem.class, id);
    }

    /**
     * Standard "find all" for WorkflowTagItems.
     * @return a List of WorkflowTagItems
     */
    public List findAll() {
        return findAll(WorkflowTagItem.class);
    }

    //
    // non-standard methods.
    //

    /**
     * Returns a list of WorkflowTagItems for a given workflow.
     * @param workflowId
     * @return list of WorkflowTagItem
     */
    public List<WorkflowTagItem> findByWorkflow(WorkflowItem workflow) {
        Object[] params = { workflow };
        return getHibernateTemplate().find("from WorkflowTagItem wt "
                                           + "where workflow = ?",
                                           params);
    }

    /**
     * Returns a JSONArray of tags sorted by popularity.
     * @param limit - max number of workflows to return in the list
     *      if limit <= 0, return all tags
     * @return JSONArray of tags as strings
     */
    public JSONArray getTagsByPopularity(int limit) {
        List<String> workflowNames = new ArrayList<String>();
        JSONArray tagNamesCounts = new JSONArray();

        String query = "SELECT COUNT(*) AS ct, group_concat(tag separator \",\") AS tagNames "
                    + "FROM workflow_tag wt "
                    + "GROUP BY wt.tag "
                    + "ORDER BY ct desc ";

        if (limit > 0) {
            query += " LIMIT " + limit;
        }

        Session session = getSession();

        try {
            SQLQuery sqlQuery = session.createSQLQuery(query);

            sqlQuery.addScalar("ct", Hibernate.LONG);
            sqlQuery.addScalar("tagNames", Hibernate.STRING);

            int i = 0;
            if (sqlQuery != null) {
                List<Object[]> results = sqlQuery.list();
                for (Object[] row: results) {
                    Long count = (Long)row[0];
                    // Tags can have different capitalizations but are grouped together for counts
                    // Return all versions of capitalization in the DB
                    String tagNameStr = (String)row[1];
                    JSONArray tagNames = new JSONArray();
                    int j = 0;
                    HashSet<String> uniqueTags = new HashSet<String>();
                    for (String tagSpelling : tagNameStr.split(",")) {
                        if (!uniqueTags.contains(tagSpelling)) {
                            tagNames.put(j++, tagSpelling);
                            uniqueTags.add(tagSpelling);
                        }
                    }

                    JSONObject countTagObj = new JSONObject();
                    countTagObj.put("count", count);
                    countTagObj.put("tagNames", tagNames);

                    tagNamesCounts.put(i++, countTagObj);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to get most popular tags: " + e.toString());
        } finally {
            releaseSession(session);
        }
        return tagNamesCounts;
    }
}
