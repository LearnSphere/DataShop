/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.AlphaScoreDao;
import edu.cmu.pslc.datashop.dto.StudentWithIntercept;
import edu.cmu.pslc.datashop.item.AlphaScoreId;
import edu.cmu.pslc.datashop.item.AlphaScoreItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * Hibernate and Spring implementation of the AlphaScoreDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 5237 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-12-04 11:46:56 -0500 (Thu, 04 Dec 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AlphaScoreDaoHibernate extends AbstractDaoHibernate implements AlphaScoreDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a AlphaScoreItem by id.
     * @param id The id of the user.
     * @return the matching AlphaScoreItem or null if none found
     */
    public AlphaScoreItem get(AlphaScoreId id) {
        return (AlphaScoreItem)get(AlphaScoreItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(AlphaScoreItem.class);
    }

    /**
     * Standard find for an AlphaScoreItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired AlphaScoreItem.
     * @return the matching AlphaScoreItem.
     */
    public AlphaScoreItem find(AlphaScoreId id) {
        return (AlphaScoreItem)find(AlphaScoreItem.class, id);
    }

    /**
     * Given a skill model and a list of students, return a list of alpha score items.
     * @param skillModelItem the selected skill model
     * @param studentList the selected students
     * @return a list of StudentWithIntercept objects
     */
    public List find(SkillModelItem skillModelItem, List studentList) {
        //build query
        String query =  "from StudentItem as stud"
            + " left join stud.alphaScores alpha"
            + " with alpha.skillModel.id = ?"
            + " where stud.id in (";
        for (Iterator iter = studentList.iterator(); iter.hasNext();) {
            StudentItem studentItem = (StudentItem)iter.next();
            query += studentItem.getId().toString() + ", ";
        }
        query = query.substring(0, query.length() - 2);
        query += ")";
        query += " order by stud.anonymousUserId";

        //log query
        if (logger.isDebugEnabled()) {
            logger.debug("find by model and student list, query: " + query);
        }

        //run query
        List alphaStudentList = getHibernateTemplate().find(query, skillModelItem.getId());

        //pull out the results
        Long skillModelIdLong = (Long)skillModelItem.getId();
        long skillModelId = skillModelIdLong.longValue();
        List returnList = new ArrayList();
        for (Iterator iter = alphaStudentList.iterator(); iter.hasNext();) {
            Object[] resultRow = (Object[])iter.next();
            StudentItem studentItem = (StudentItem)resultRow[0];
            AlphaScoreItem alphaScoreItem = (AlphaScoreItem)resultRow[1];
            Double alphaValue = null;
            if (alphaScoreItem != null) {
                alphaValue = alphaScoreItem.getAlpha();
            }
            returnList.add(new StudentWithIntercept(studentItem.getAnonymousUserId(),
                           alphaValue,
                           skillModelId));
        }
        // return results
        return returnList;
    }

    /**
     * Given a skill model, return a list of alpha score items.
     * @param skillModelItem the selected skill model
     * @return a list of StudentWithIntercept objects, or an empty list if the skill model is null
     */
    public List find(SkillModelItem skillModelItem) {
        if (skillModelItem == null) { return new ArrayList(); }

        //build query
        String query = "from AlphaScoreItem as alpha"
            + " join alpha.student stud"
            + " where skill_model_id = ?"
            + " order by stud.anonymousUserId";

        //log query
        if (logger.isDebugEnabled()) {
            logger.debug("find by model, query: " + query);
        }

        //run query
        List alphaStudentList = getHibernateTemplate().find(query, skillModelItem.getId());

        //pull out the results
        Long skillModelIdLong = (Long)skillModelItem.getId();
        long skillModelId = skillModelIdLong.longValue();
        List returnList = new ArrayList();
        for (Iterator iter = alphaStudentList.iterator(); iter.hasNext();) {
            Object[] resultRow = (Object[])iter.next();
            AlphaScoreItem alphaScoreItem = (AlphaScoreItem)resultRow[0];
            StudentItem studentItem = (StudentItem)resultRow[1];
            returnList.add(new StudentWithIntercept(studentItem.getAnonymousUserId(),
                    alphaScoreItem.getAlpha(),
                    skillModelId));
        }

        // return results
        return returnList;
    }

    /**
     * Clear the alpha scores for a given skill model.
     * This is only needed if the LFA code cannot produce valid results.
     * See DS816:  (LFA values are not cleared if second run produces no results).
     * @param skillModelItem the given skill model
     * @return the number of rows deleted
     */
    public int clear(SkillModelItem skillModelItem) {
        if (skillModelItem == null) {
            throw new IllegalArgumentException("Skill Model cannot be null.");
        }
        int rowCount = 0;
        String query = "delete from alpha_score where skill_model_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Long)skillModelItem.getId()).longValue());
            rowCount = ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("clear (SkillModel " + skillModelItem.getId()
                        + ") Deleted " + rowCount + " rows.");
            }
        } catch (SQLException exception) {
            logger.error("clear (SkillModel " + skillModelItem.getId()
                    + ") SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
        return rowCount;
    }
}
