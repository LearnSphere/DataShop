/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Hibernate and Spring implementation of the SkillModelDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12678 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-13 15:55:53 -0400 (Tue, 13 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SkillModelDaoHibernate extends AbstractDaoHibernate implements SkillModelDao {

    /**
     * Standard get for a SkillModelItem by id.
     * @param id The id of the user.
     * @return the matching SkillModelItem or null if none found
     */
    public SkillModelItem get(Long id) {
        if (id == null) { return null; }
        return (SkillModelItem)get(SkillModelItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(SkillModelItem.class);
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

        SkillModelItem newSkilLModelItem = (SkillModelItem)newItem;

        for (Object existingItem : collection) {
            SkillModelItem existingSkillModelItem = (SkillModelItem)existingItem;
            if (existingSkillModelItem.getSkillModelName().equals(
                    newSkilLModelItem.getSkillModelName())) {

                existingSkillModelItem.setModifiedTime(new Date());
                existingSkillModelItem.setStatus(SkillModelItem.STATUS_NOT_READY);
                if (existingSkillModelItem.getAllowLFAFlag()) {
                    existingSkillModelItem.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);
                    existingSkillModelItem.setCvStatus(SkillModelItem.LFA_STATUS_QUEUED);
                }
                newItem = (Item)existingSkillModelItem;

                found = true;
                break;
            }
        }

        if (!found) {
            if (logger.isDebugEnabled()) {
                logger.debug("findOrCreate: creating new item: " + newItem);
                logger.debug("findOrCreate: as its not found in collection of size: "
                        + collection.size());
            }
            saveOrUpdate(newItem);
            //list.add(newItem);
        }
        return newItem;
    }

    /**
     * Standard find for an SkillModelItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired SkillModelItem.
     * @return the matching SkillModelItem.
     */
    public SkillModelItem find(Long id) {
        return (SkillModelItem)find(SkillModelItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the skill models for undeleted datasets.
     * @return a list of skill model items
     */
     public List findAllUndeleted() {
        String query = "select distinct sm from SkillModelItem sm"
            + " where sm.dataset.deletedFlag is NULL OR sm.dataset.deletedFlag = false";
        return getHibernateTemplate().find(query);
     }

    /**
     * Find all the skill models for the given dataset.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    public List find(DatasetItem datasetItem) {
        String query = "select distinct sm from SkillModelItem sm"
            + " where sm.dataset = ?";
        return getHibernateTemplate().find(query, datasetItem);
    }

    /**
     * Find all the skill models for the given dataset ordered by skillModelName.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    public List findOrderByName(DatasetItem datasetItem) {
        if (datasetItem == null) {
            return null;
        }
        String query = "select distinct sm from SkillModelItem sm"
            + " where sm.dataset = ? order by skillModelName";
        return getHibernateTemplate().find(query, datasetItem);
    }
    
    /**
     * Find all the skill models for the given dataset ordered by the number of observations.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    public List findOrderByNumObservations(DatasetItem datasetItem) {
        if (datasetItem == null) {
            return null;
        }
        String query = "select distinct sm from SkillModelItem sm"
                + " where sm.dataset = ? order by numObservations";
        return getHibernateTemplate().find(query, datasetItem);
    }

    /**
     * Get a skill model item based on dataset and name.
     * @param datasetItem the dataset
     * @param modelName the name of the model
     * @return the resulting SkillModelItem, null if none found.
     */
    public SkillModelItem findByName(DatasetItem datasetItem, String modelName) {
        String query = "select distinct sm from SkillModelItem sm"
            + " where sm.dataset = ?"
            + " and sm.skillModelName = ?";

        Object[] params = new Object[2];
        params[0] = datasetItem;
        params[1] = modelName;

        List results = getHibernateTemplate().find(query, params);
        if (results.size() > 1) {
            logger.warn("More than one model found for dataset " + datasetItem.getId()
                    + " and model name " + modelName);
        }

        return (results.size() > 0) ? (SkillModelItem)results.get(0) : null;
    }

    /** HQL Query to get the number of skills in a given skill model. */
    public static final String GET_SKILL_MODELSS_BY_NAME
        = "select distinct(skillModel) from SkillModelItem skillModel "
            + "where skillModel.dataset.id = ? "
            + "and skillModel.skillModelName = ? ";

    /**
     * Get a skill model item based on dataset and name.
     * @param datasetItem the dataset
     * @param modelName the name of the model
     * @return the resulting SkillModelItem, null if none found.
     */
    public List<SkillModelItem> findByDatasetAndName(DatasetItem datasetItem, String modelName) {
        Object[] statementObjects = new Object[2];
        statementObjects[0] = datasetItem.getId();
        statementObjects[1] = modelName;
        return getHibernateTemplate().find(GET_SKILL_MODELSS_BY_NAME, statementObjects);
    }


    /**
     * Creates the single KC model and it's associated skill, populates the
     * transaction_skill_mapping with the skill for each transaction which has a subgoal.
     * @param dataset the dataset to generate the model for.
     */
    public void createOrUpdateSingleKCModel(DatasetItem dataset) {

        SkillModelItem existing = findByName(dataset, SkillModelItem.NAME_SINGLE_KC_MODEL);
        if (existing != null) { delete(existing); } //just delete it to make life easy.

        SkillModelItem newModel = new SkillModelItem();
        newModel.setSkillModelName(SkillModelItem.NAME_SINGLE_KC_MODEL);
        newModel.setSource(SkillModelItem.SOURCE_AUTO_GEN);
        newModel.setMappingType(SkillModelItem.MAPPING_STEP);
        newModel.setDataset(dataset);
        newModel.setStatus(SkillModelItem.STATUS_NOT_READY);
        newModel.setGlobalFlag(true);
        saveOrUpdate(newModel);

        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();

        SkillItem singleKC = new SkillItem();
        singleKC.setSkillName(SkillItem.SINGLE_KC_MODEL_SKILL_NAME);
        singleKC.setSkillModel(newModel);
        skillDao.saveOrUpdate(singleKC);

        String skillAndModel =  SkillItem.SINGLE_KC_MODEL_SKILL_NAME
                + " :: " + SkillModelItem.NAME_SINGLE_KC_MODEL;

        skillDao.populateTransactionSkillMap(dataset, singleKC, skillAndModel);

        // Skill was added to the model, update the count in the model.
        newModel = get((Long)newModel.getId());
        newModel.setNumSkills(1);
        saveOrUpdate(newModel);
    }

    /** Constant for cutoff of percentage of students in a subgoal. */
    private static final Double PCT_STUDENTS_CUTOFF = 0.1;

    /** Constant for cutoff of number of students in a subgoal. */
    private static final Integer NUM_STUDENTS_CUTOFF = 10;

    /**
     * Creates the single KC model and it's associated skill, populates the
     * transaction_skill_mapping with the skill for each transaction which has a subgoal.
     * @param dataset the dataset to generate the model for.
     */
    public void createOrUpdateUniqueStepModel(DatasetItem dataset) {

        SkillModelItem existing = findByName(dataset, SkillModelItem.NAME_UNIQUE_STEP_MODEL);
        if (existing != null) { delete(existing); } //just delete it to make life easy.

        SkillModelItem newModel = new SkillModelItem();
        newModel.setSkillModelName(SkillModelItem.NAME_UNIQUE_STEP_MODEL);
        newModel.setSource(SkillModelItem.SOURCE_AUTO_GEN);
        newModel.setMappingType(SkillModelItem.MAPPING_STEP);
        newModel.setDataset(dataset);
        newModel.setStatus(SkillModelItem.STATUS_NOT_READY);
        newModel.setGlobalFlag(true);
        saveOrUpdate(newModel);

        Long numStudents = DaoFactory.DEFAULT.getSampleMetricDao().getTotalStudents(dataset);
        int cutoff = new Double(numStudents * PCT_STUDENTS_CUTOFF).intValue();

        // Trac #565. If necessary, include subgoals for which there are at least 10 students.
        String havingClause = "(count(distinct stud.id) > " + cutoff
            + " or count(distinct stud.id) >= " + NUM_STUDENTS_CUTOFF + ")";

        Session session = getSession();
        String query = "select distinct sub from "
            + " TransactionItem trans "
            + " join trans.subgoal sub"
            + " join trans.session.student stud"
            + " where trans.dataset.id = " + dataset.getId()
            + " group by sub.id having " + havingClause
            + " order by sub.guid";

        ScrollableResults results = session
            .createQuery(query)
            .setCacheMode(CacheMode.IGNORE)
            .scroll();

        //walk through each subgoal, create a new skill and populate the mapping.
        int counter = 0;
        SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
        while (results.next()) {
            counter++;
            SubgoalItem subgoal = (SubgoalItem)results.get(0);
            SkillItem newSkill = new SkillItem();
            newSkill.setSkillName("KC" + counter);
            newSkill.setSkillModel(newModel);
            skillDao.saveOrUpdate(newSkill);

            String skillAndModel = newSkill.getSkillName()
                    + " :: " + newModel.getSkillModelName();

            skillDao.populateTransactionSkillMap(subgoal, newSkill, skillAndModel);
        }

        if (counter == 0) {
            logger.warn("No steps found in this dataset for unique-step model!");
        } else {
            // If skills were added to the model, update the count in the model.
            newModel = get((Long)newModel.getId());
            newModel.setNumSkills(counter);
            saveOrUpdate(newModel);
        }

        session.flush();
        session.clear();
        releaseSession(session);
    }

    /**
     * Updates all models in a given dataset with an updated state.
     * @param dataset the dataset with skill models to update.
     */
    public void setAllReady(DatasetItem dataset) {
        List <SkillModelItem> modelList = find(dataset);
        for (SkillModelItem model : modelList) {
            model.setStatus(SkillModelItem.STATUS_READY);
            saveOrUpdate(model);
        }
    }

    /**
     * Get count of skill models for the given dataset.
     * @param datasetItem the dataset item
     * @return Long number of skill models
     */
    public Long countSkillModels(DatasetItem datasetItem) {
        String query = "select count(distinct sm) from SkillModelItem sm"
            + " where sm.dataset = ?";
        return (Long)getHibernateTemplate().find(query, datasetItem).get(0);
    }
}
