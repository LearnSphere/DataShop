/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.workflows.WorkflowErrorTranslationItem;
/**
 * WorkflowErrorTranslation Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public interface WorkflowErrorTranslationDao extends AbstractDao {

    /**
     * Standard get for a WorkflowErrorTranslationItem by id.
     * @param id The id of the WorkflowErrorTranslationItem.
     * @return the matching WorkflowErrorTranslationItem or null if none found
     */
    WorkflowErrorTranslationItem get(Integer id);

    /**
     * Standard find for an WorkflowErrorTranslationItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired WorkflowErrorTranslationItem.
     * @return the matching WorkflowErrorTranslationItem.
     */
    WorkflowErrorTranslationItem find(Integer id);

    /**
     * Standard "find all" for WorkflowErrorTranslationItems.
     * @return a List of objects
     */
    List findAll();

    //
    // non-standard methods.
    //

    /**
     * Returns the WorkflowErrorTranslationItem given the message.
     * @param message the message
     * @return the WorkflowErrorTranslationItem list
     */
    List<WorkflowErrorTranslationItem> findByMessage(String message);


}
