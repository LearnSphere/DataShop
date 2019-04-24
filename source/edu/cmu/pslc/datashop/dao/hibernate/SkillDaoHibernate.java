/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.util.LogException;

/**
 * Hibernate and Spring implementation of the SkillDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6048 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-04-23 11:49:14 -0400 (Fri, 23 Apr 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SkillDaoHibernate extends AbstractDaoHibernate implements SkillDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a SkillItem by id.
     * @param id The id of the user.
     * @return the matching SkillItem or null if none found
     */
    public SkillItem get(Long id) {
        return (SkillItem)get(SkillItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SkillItem.class);
    }

    /**
     * Standard find for an SkillItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SkillItem.
     * @return the matching SkillItem.
     */
    public SkillItem find(Long id) {
        return (SkillItem)find(SkillItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL Query to get the number of skills in a given skill model. */
    public static final String GET_NUM_SKILLS_QUERY
        = "select count(*) from SkillItem where skill_model_id = ";

    /**
     * Return the number of skills in a given skill model.
     * @param skillModelItem the given skill model
     * @return number of skills
     */
    public int getNumSkills(SkillModelItem skillModelItem) {
        int numSkills = 0;
        if (skillModelItem == null || skillModelItem.getId() == null) {
            return numSkills;
        }

        String query = GET_NUM_SKILLS_QUERY + skillModelItem.getId();
        if (logger.isDebugEnabled()) {
            logger.debug("getNumSkills: query: " + query);
        }

        //This didn't work.
        //Integer numResults = (Integer) getHibernateTemplate().iterate(query).next();
        //return numResults.intValue();

        Collection resultsList = getHibernateTemplate().find(query);
        for (Iterator iter = resultsList.iterator(); iter.hasNext();) {
            Long numResults = (Long)iter.next();
            numSkills = numResults.intValue();
        }

        return numSkills;
    }

    /** HQL Query to get the number of skills in a given skill model. */
    public static final String GET_SKILLS_IN_MODEL_QUERY
        = "select distinct(skill) from SkillItem skill where skill.skillModel.id = ? ";

    /**
     * Find all the skills for the given skill model.
     * @param skillModelItem the skill model item
     * @return a list of skill items
     */
    public List find(SkillModelItem skillModelItem) {
        //DetachedCriteria query = DetachedCriteria.forClass(SkillItem.class);
        //query.add(Restrictions.eq("skillModel", skillModelItem));
        //return getHibernateTemplate().findByCriteria(query);
        return getHibernateTemplate().find(GET_SKILLS_IN_MODEL_QUERY, skillModelItem.getId());
    }

    /** HQL Query to get the number of skills in a given skill model. */
    public static final String GET_SKILLS_FOR_COG_STEP_QUERY
        = " select distinct(skill) from CognitiveStepItem csi"
        + " join csi.skills skill "
        + " where csi.id = ? ";

    /**
     * Find all the skills for the given cognitive step.
     * @param cogStepItem the CognitiveStepItem.
     * @return a list of skill items
     */
    public List find(CognitiveStepItem cogStepItem) {
        return getHibernateTemplate().find(GET_SKILLS_FOR_COG_STEP_QUERY, cogStepItem.getId());
    }

    /** HQL Query to get the number of skills in a given skill model. */
    public static final String GET_SKILLS_BY_NAME_MODEL
        = "select distinct(skill) from SkillItem skill "
            + "where skill.skillModel.id = ? "
            + "and skill.skillName = ? ";

    /**
     * Find all the skills that match the model and name.
     * @param skillModelItem the skill model item
     * @param skillName the name of the skill as a string.
     * @return a list of skill items
     */
    public List find(SkillModelItem skillModelItem, String skillName) {
        Object[] statementObjects = new Object[2];
        statementObjects[0] = skillModelItem.getId();
        statementObjects[1] = skillName;
        return getHibernateTemplate().find(GET_SKILLS_BY_NAME_MODEL, statementObjects);
    }

    /** Native SQL insert query */
    private static final String SKILL_TRANS_MAP_UPDATE =
          "insert ignore into transaction_skill_map"
        + " (transaction_id, skill_id) values ";

    /**
     * Inserts transaction/skill pairs into the transaction_skill_map via bulk inserts.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param subgoal the subgoal get transaction for.
     * @param skill the skill to insert mappings for.
     * @param skillAndModel the skill and model in a string for debugging
     * @return number of mappings inserted.
     */
    public int populateTransactionSkillMap(SubgoalItem subgoal, SkillItem skill,
            String skillAndModel) {
        if (subgoal == null) { throw new IllegalArgumentException("Subgoal cannot be null."); }
        if (skill == null) { throw new IllegalArgumentException("Skill cannot be null."); }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding skill/transaction mappings for"
                   + " subgoal " + subgoal.getId()
                   + ", skill and model " + skillAndModel);
        }

        Session session = getSession();

        //first get a list of transactions as a ScrollableResults from the subgoal.
        String query = "select distinct trans.id from "
            + " TransactionItem trans "
            + " where trans.subgoal.id = " + subgoal.getId();

        ScrollableResults results = session
            .createQuery(query)
            .setCacheMode(CacheMode.IGNORE)
            .scroll();

        int count = insertMappings(skill, results, session, skillAndModel);

        //final flush and clear to make sure any transaction properly picks up the changes.
        session.flush();
        session.clear();
        releaseSession(session);
        return count;
    }

    /**
     * Inserts transaction/skill pairs into the transaction_skill_map via bulk inserts.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param dataset the dataset get transaction for.
     * @param skill the skill to insert mappings for.
     * @param skillAndModel the skill and model in a string for debugging
     * @return number of mappings inserted.
     */
    public int populateTransactionSkillMap(DatasetItem dataset, SkillItem skill,
            String skillAndModel) {
        if (dataset == null) { throw new IllegalArgumentException("Dataset cannot be null."); }
        if (skill == null) { throw new IllegalArgumentException("Skill cannot be null."); }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding skill/transaction mappings for skill " + skill.getId()
                   + " and dataset " + dataset.getId());
        }

        Session session = getSession();

        //first get a list of transactions as a ScrollableResults from the subgoal.
        String query = "select distinct trans.id from "
            + " TransactionItem trans "
            + " where trans.dataset.id = " + dataset.getId()
            + " and trans.subgoal IS NOT NULL";

        ScrollableResults results = session
            .createQuery(query)
            .setCacheMode(CacheMode.IGNORE)
            .scroll();

        int count = insertMappings(skill, results, session, skillAndModel);

        //final flush and clear to make sure any transaction properly picks up the changes.
        session.flush();
        session.clear();
        releaseSession(session);
        return count;
    }

    /**
     * Places transaction_skill_map mappings which map the given skill to the results set
     * which is expected to be a ScrollableResults set of transaction IDs.
     * @param skill The skill to map.
     * @param results ScrollableResults set of transaction IDs.
     * @param session The hibernate session.
     * @param skillAndModel the skill and model in a string for debugging
     * @return the number of mappings created.
     */
    private int insertMappings(SkillItem skill, ScrollableResults results,
            Session session, String skillAndModel) {
        StringBuffer insertQuery = new StringBuffer(SKILL_TRANS_MAP_UPDATE);
        int count = 0;
        String skillId = skill.getId().toString();
        //walk through each transaction result and build up an insert query string
        //up to BATCH_SIZE, at which point run the query and repeat
        boolean inserted = true;
        while (results.next()) {
            if (!inserted) { insertQuery.append(", "); }
            inserted = false;
            Long transId = (Long)results.get(0);
            insertQuery.append("(" + transId + ", " + skillId + ")");
            if (++count % BATCH_SIZE == 0) {
                try {
                    session.connection().createStatement().execute(insertQuery.toString());
                } catch (SQLException sqlException) {
                    logger.error("Exception occurred adding transaction_skill_map items",
                            sqlException);
                    throw new LogException(
                            "Exception occurred adding transaction_skill_map items", sqlException);
                }
                insertQuery = new StringBuffer(SKILL_TRANS_MAP_UPDATE);
                inserted = true;
            }
        }

        if (!inserted) {
            try {
                session.connection().createStatement().execute(insertQuery.toString());
            } catch (SQLException sqlException) {
                String msg = "Exception occurred adding transaction_skill_map items for "
                        + skillAndModel;
                logger.error(msg, sqlException);
                throw new LogException(msg, sqlException);
            }
        }

        return count;
    }

    /** {@inheritDoc} */
    public Integer getMaxSkillCount(SkillModelItem model) {
        Session session = getSession();

        //first get a list of transactions as a ScrollableResults from the subgoal.
        String query = "select count(sk.skill_id) c"
            + " from skill sk"
            + " join transaction_skill_map tsm on sk.skill_id = tsm.skill_id"
            + " where sk.skill_model_id = :modelId"
            + " group by tsm.transaction_id"
            + " order by c desc"
            + " limit 1";

        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setLong("modelId", (Long)model.getId());

        sqlQuery.addScalar("c", Hibernate.INTEGER);
        Integer result = (Integer)sqlQuery.uniqueResult();
        releaseSession(session);

        return (result == null) ? 0 : result;
    }
}
