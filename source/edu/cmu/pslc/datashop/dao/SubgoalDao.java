/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;

/**
 * Subgoal Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4592 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-03-28 15:58:16 -0400 (Fri, 28 Mar 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface SubgoalDao extends AbstractDao {

    /**
     * Standard get for a SubgoalItem by id.
     * @param id The id of the SubgoalItem.
     * @return the matching SubgoalItem or null if none found
     */
    SubgoalItem get(Long id);

    /**
     * Standard find for an SubgoalItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired SubgoalItem.
     * @return the matching SubgoalItem.
     */
    SubgoalItem find(Long id);

    /**
     * Standard "find all" for SubgoalItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Get a list of all subgoals in a dataset ordered by problem.
     * @param dataset the dataset to get the subgoals in.
     * @param limit the limit of a number to return.
     * @param offset the offset for the first to return.
     * @return List of all subgoals in the dataset between the offset and the limit.
     */
    List find(DatasetItem dataset, Integer limit, Integer offset);

    /**
     * Gets a list of subgoals in the problem.
     * @param problem the problem to get all subgoals for.
     * @return List of all subgoals.
     */
    List find(ProblemItem problem);

    /**
     * Gets a subgoal based on a step tag and a dataset.
     * @param dataset the Dataset to get the step tag for.
     * @param guid GUID generated for each step.
     * @return matching SubgoalItem, null if none found.
     */
    SubgoalItem find(DatasetItem dataset, String guid);

    /**
     * Generates a unique identifier for a step in a given dataset.  This function
     * always generates the same GUID for a step given that the step name, problem name,
     * and problem hierarchy string stay consistent.
     * @param subgoal The subgoal to generate a unique identifier for.
     * @return String that is the identifier.
     */
    String generateGUID(SubgoalItem subgoal);

}
