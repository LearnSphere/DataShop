/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Skill Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6048 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-04-23 11:49:14 -0400 (Fri, 23 Apr 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SkillDao extends AbstractDao {

    /**
     * Standard get for a SkillItem by id.
     * @param id The id of the SkillItem.
     * @return the matching SkillItem or null if none found
     */
    SkillItem get(Long id);

    /**
     * Standard find for an SkillItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SkillItem.
     * @return the matching SkillItem.
     */
    SkillItem find(Long id);

    /**
     * Standard "find all" for SkillItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Return the number of skills in a given skill model.
     * @param skillModelItem the given skill model
     * @return number of skills
     */
    int getNumSkills(SkillModelItem skillModelItem);

    /**
     * Find all the skills for the given skill model.
     * @param skillModelItem the skill model item
     * @return a list of skill items
     */
    List find(SkillModelItem skillModelItem);

    /**
     * Find all the skills for the given cognitive step.
     * @param cogStepItem the CognitiveStepItem.
     * @return a list of skill items
     */
    List find(CognitiveStepItem cogStepItem);

    /**
     * Find all the skills that match the model and name.
     * @param skillModelItem the skill model item
     * @param skillName the name of the skill to find.
     * @return a list of skill items
     */
    List find(SkillModelItem skillModelItem, String skillName);

    /**
     * Inserts transaction/skill pairs into the transaction_skill_map via bulk inserts.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param subgoal the subgoal get transaction for.
     * @param skill the skill to insert mappings for.
     * @param skillAndModel the skill and model in a string for debugging
     * @return number of mappings inserted.
     */
    int populateTransactionSkillMap(SubgoalItem subgoal, SkillItem skill,
            String skillAndModel);

    /**
     * Inserts transaction/skill pairs into the transaction_skill_map via bulk inserts.
     * <strong>Note: </strong> this function is not transaction based.  It is intended to be
     * used inside of a transaction.
     * @param dataset the dataset get transaction for.
     * @param skill the skill to insert mappings for.
     * @param skillAndModel the skill and model in a string for debugging
     * @return number of mappings inserted.
     */
    int populateTransactionSkillMap(DatasetItem dataset, SkillItem skill,
            String skillAndModel);

    /**
     * Get the maximum number of skills on a single transaction for a given skill model.
     * @param model the SkillModelItem to get a max count for.
     * @return Integer of the maximum count.
     */
    Integer getMaxSkillCount(SkillModelItem model);
}
