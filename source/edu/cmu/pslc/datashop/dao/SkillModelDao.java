/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * SkillModel Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12678 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-13 15:55:53 -0400 (Tue, 13 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SkillModelDao extends AbstractDao {

    /**
     * Standard get for a SkillModelItem by id.
     * @param id The id of the SkillModelItem.
     * @return the matching SkillModelItem or null if none found
     */
    SkillModelItem get(Long id);

    /**
     * Standard find for an SkillModelItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SkillModelItem.
     * @return the matching SkillModelItem.
     */
    SkillModelItem find(Long id);

    /**
     * Standard "find all" for SkillModelItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //
    /**
     * Find all the skill models for undeleted datasets.
     * @return a list of skill model items
     */
     public List findAllUndeleted();

    /**
     * Find all the skill models for the given dataset.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    List find(DatasetItem datasetItem);

    /**
     * Find all the skill models for the given dataset ordered by skillModelName.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    List findOrderByName(DatasetItem datasetItem);
    
    /**
     * Find all the skill models for the given dataset ordered by the number of observations.
     * @param datasetItem the dataset item
     * @return a list of skill model items
     */
    List findOrderByNumObservations(DatasetItem datasetItem);

    /**
     * Find all the skill models for the given dataset and model name.
     * @param datasetItem the dataset item
     * @param modelName the name of the model
     * @return a list of skill model items
     */
    List findByDatasetAndName(DatasetItem datasetItem, String modelName);

    /**
     * Get a skill model item based on dataset and name.
     * @param datasetItem the dataset
     * @param modelName the name of the model
     * @return the resulting SkillModelItem, null if none found.
     */
    SkillModelItem findByName(DatasetItem datasetItem, String modelName);

    /**
     * Creates the single KC model and it's associated skill, populates the
     * transaction_skill_mapping with the skill for each transaction which has a subgoal.
     * @param dataset the dataset to generate the model for.
     */
    void createOrUpdateSingleKCModel(DatasetItem dataset);

    /**
     * Creates the single KC model and it's associated skill, populates the
     * transaction_skill_mapping with the skill for each transaction which has a subgoal.
     * @param dataset the dataset to generate the model for.
     */
    void createOrUpdateUniqueStepModel(DatasetItem dataset);

    /**
     * Updates all models in a given dataset with an updated state.
     * @param dataset the dataset with skill models to update.
     */
    void setAllReady(DatasetItem dataset);

    /**
     * Get count of skill models for the given dataset.
     * @param datasetItem the dataset item
     * @return Long number of skill models
     */
    Long countSkillModels(DatasetItem datasetItem);
}
