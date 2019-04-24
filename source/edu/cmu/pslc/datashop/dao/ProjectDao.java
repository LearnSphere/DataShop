/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto;
import edu.cmu.pslc.datashop.item.IrbItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.irb.IrbReviewFilter;

/**
 * Project Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectDao extends AbstractDao {

    /**
     * Standard get for a project item by id.
     * @param id the id of the desired project item
     * @return the matching ProjectItem or null if none found
     */
    ProjectItem get(Integer id);

    /**
     * Standard find for a project item by id.
     * @param id id of the object to find
     * @return ProjectItem
     */
    ProjectItem find(Integer id);

    /**
     * Standard "find all" for project items.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Nonstandard "find all" for project items sorted by projectName.
     * @return a List of objects which are ProjectItems sorted by projectName
     */
    List findAllSortByName();

    /**
     * Returns ProjectItem given a name.
     * @param name name of the ProjectItem
     * @return a collection of ProjectItems
     */
    Collection find(String name);

    /**
     * Count the number of papers for the datasets of this project.
     *
     * @param projectItem the dataset
     * @return the number of papers attached to this project
     */
    Long countPapers(ProjectItem projectItem);

    /**
     * Find projects given a data provider.
     * @param userItem the data provider
     * @return a collection of ProjectItems
     */
    Collection findByDataProvider(UserItem userItem);

    /**
     * Find projects given a data provider or primary investigator.
     * @param ownerItem the data provider or PI
     * @return a collection of ProjectItems
     */
    Collection findByOwner(UserItem ownerItem);

    /**
     * Find all data providers.
     *
     * @return a collection of UserItems
     */
    Collection findAllDataProviders();

    /**
     * Nonstandard "find all" for project items, filtered by IRB attributes.
     * @param filter IrbReviewFilter specifying how to filter list of projects
     * @return a List of objects which are ProjectItems sorted by projectName
     */
    List findAllFiltered(IrbReviewFilter filter);

    /**
     * Return a list of ExistingProjectDto objects for the given user.
     * @param userItem the user item for the current user
     * @return list of ExistingProjectDto objects
     */
    List<ExistingProjectDto> getExistingProjects(UserItem userItem);

    /**
     * Find the special 'Remote Datasets' project that holds datasets
     * created for SLAVE instances.
     * 
     * @return ProjectItem
     */
    ProjectItem findRemoteDatasetsProject();

    /**
     * Return a list of the DiscourseDB projects.
     * 
     * @return list of ProjectItem objects
     */
    List<ProjectItem> findDiscourseDbProjects();
}
