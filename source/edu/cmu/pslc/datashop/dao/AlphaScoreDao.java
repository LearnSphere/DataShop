/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.AlphaScoreId;
import edu.cmu.pslc.datashop.item.AlphaScoreItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * AlphaScore Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 8627 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:14:35 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AlphaScoreDao extends AbstractDao {

    /**
     * Standard get for a AlphaScoreItem by id.
     * @param id The id of the AlphaScoreItem.
     * @return the matching AlphaScoreItem or null if none found
     */
    AlphaScoreItem get(AlphaScoreId id);

    /**
     * Standard find for an AlphaScoreItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired AlphaScoreItem.
     * @return the matching AlphaScoreItem.
     */
    AlphaScoreItem find(AlphaScoreId id);

    /**
     * Standard "find all" for AlphaScoreItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Given a skill model and a list of students, return a list of alpha score items.
     * @param skillModelItem the selected skill model
     * @param studentList the selected students
     * @return a list of AlphaScoreItems
     */
    List find(SkillModelItem skillModelItem, List studentList);

    /**
     * Given a skill model, return a list of alpha score items.
     * @param skillModelItem the selected skill model
     * @return a list of StudentWithIntercept objects
     */
    List find(SkillModelItem skillModelItem);

    /**
     * Clear the alpha scores for a given skill model.
     * This is only needed if the LFA code cannot produce valid results.
     * See DS816:  (LFA values are not cleared if second run produces no results).
     * @param skillModelItem the given skill model
     * @return the number of rows deleted
     */
    int clear(SkillModelItem skillModelItem);
}
