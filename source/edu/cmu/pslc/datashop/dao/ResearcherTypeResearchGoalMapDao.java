/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapId;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;

/**
 * ResearcherType ResearchGoal Map Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResearcherTypeResearchGoalMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ResearcherTypeResearchGoalMapItem get(ResearcherTypeResearchGoalMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ResearcherTypeResearchGoalMapItem find(ResearcherTypeResearchGoalMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ResearchGoalItems.
     *  @param typeItem the given type item
     *  @return a list of items
     */
    List<ResearchGoalItem> findByType(ResearcherTypeItem typeItem);

    /**
     *  Return a list of ResearchGoalItems.
     *  @param typeItem the given Type item
     *  @return a list of items
     */
    List<ResearchGoalItem> findOtherGoals(ResearcherTypeItem typeItem);

    /**
     *  Return a list of ResearcherTypeItems.
     *  @param goalItem the given research goal item
     *  @return a list of items
     */
    List<ResearcherTypeItem> findByGoal(ResearchGoalItem goalItem);
}
