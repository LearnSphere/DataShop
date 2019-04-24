/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseMapId;
import edu.cmu.pslc.datashop.item.TermsOfUseItem;

/**
 * Transaction Skill Event Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectTermsOfUseMapDao extends AbstractDao {

    /**
     * Standard get for a ProjectTermsOfUseMapItem.
     * @param id the ProjectTermsOfUseMapId for this item.
     * @return the matching ProjectTermsOfUseMapItem or null if none found
     */
    ProjectTermsOfUseMapItem get(ProjectTermsOfUseMapId id);

    /**
     * Standard find for an ProjectTermsOfUseMapItem.
     * Only the id of the item will be filled in.
     * @param id the ProjectTermsOfUseMapId for this item.
     * @return the matching ProjectTermsOfUseMapItem.
     */
    ProjectTermsOfUseMapItem find(ProjectTermsOfUseMapId id);

    /**
     * Standard "find all" for ProjectTermsOfUseMapItem items.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a list of termsOfUse with which a project is associated.
     * @param projectId the project id
     * @return a list of ProjectTermsOfUseMapItems
     */
    Collection findDistinctTermsOfUse(Integer projectId);

    /**
     * Returns a list of projects with which a terms of use is associated.
     * @param termsOfUseId the termsOfUse id
     * @return  a list of Terms of Use items
     */
    Collection findDistinctProjects(Integer termsOfUseId);

    /**
     * Adds a new composite key to the project terms of use map.
     *
     * @param projectItem the project
     * @param touItem the terms of use
     */
    void add(ProjectItem projectItem, TermsOfUseItem touItem);

    /**
     * Removes a composite key from the project terms of use map.
     *
     * @param projectItem the project
     * @param touItem the terms of use
     * @return null if no item was found else return the deleted item
     */
    ProjectTermsOfUseMapItem remove(ProjectItem projectItem, TermsOfUseItem touItem);

    /**
     * Find projects given a terms of use.
     *
     * @param termsOfUseItem the terms of use
     * @return a collection of Projects
     */
    Collection getProjectsForTermsOfUse(TermsOfUseItem termsOfUseItem);

    /**
     * Find the terms of use associated with a project.
     *
     * @param projectItem the project
     * @return the TermsOfUseItem
     */
    TermsOfUseItem getTermsOfUseForProject(ProjectItem projectItem);

} // end ProjectTermsOfUseMapDao.java
