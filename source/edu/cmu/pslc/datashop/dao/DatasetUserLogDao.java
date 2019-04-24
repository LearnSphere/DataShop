/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserLogItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * DatasetUserLog Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 12081 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:00:09 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface DatasetUserLogDao extends AbstractDao {

    /**
     * Standard get for a DatasetUserLogItem by id.
     * @param id The id of the DatasetUserLogItem.
     * @return the matching DatasetUserLogItem or null if none found
     */
    DatasetUserLogItem get(Integer id);

    /**
     * Standard find for an DatasetUserLogItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired DatasetUserLogItem.
     * @return the matching DatasetUserLogItem.
     */
    DatasetUserLogItem find(Integer id);

    /**
     * Standard "find all" for DatasetUserLogItems.
     * @return a List of objects
     */
    List findAll();

    /**
     * Get a list of all datasets view within the last 30 days by the user.
     * @param user the user to get datasets for.
     * @return a List of dataset items.
     */
    List <DatasetItem> getRecentDatasetsViewed(UserItem user);

    /**
     * Checks if a given action for a given user and dataset has already been properly
     * recorded to avoid recording the same action multiple times (like when refreshing
     * or changing options on a report page).
     * @param dataset The dataset being used by the user.
     * @param user The user performing the action.
     * @param action The action being performed.
     * @return Boolean of true if the action has already been recorded, false otherwise.
     */
    Boolean isAlreadyRecorded(DatasetItem dataset, UserItem user, String action);

    /**
     * Get the number of times the given user has selected any dataset.
     * This is used to determine if the user is a new to DataShop or not.
     * @param userItem the user item
     * @return the number of times the given user has selected any dataset
     */
    long getNumberDatasetSelects(UserItem userItem);

    /**
     * Checks if a given action for a given user has been logged.
     * @param user The user performing the action.
     * @param action the given action
     * @return Boolean of true if the action has been logged, false otherwise.
     */
    boolean hasPerformedAction(UserItem user, String action);

    /**
     * Get the first access date for a given user and project.
     * @param userItem the user item
     * @param projectItem the project item
     * @return the first time the given user has selected any dataset in project
     */
    Date getFirstAccess(UserItem userItem, ProjectItem projectItem);

    /**
     * Get the first access date for a given user and project.
     * @param userItem the user item
     * @param projectItem the project item
     * @return the last time the given user has selected any dataset in project
     */
    Date getLastAccess(UserItem userItem, ProjectItem projectItem);

    /**
     * Gets an object array (UserItem, ProjectId, First access, and Last access)
     * or return null if no users accessed any datasets.
     * @return an object array (UserItem, ProjectId, First access, and Last access)
     */
    Collection getAccessTimes();

    /**
     * Gets an object array (UserItem, First access, and Last access)
     * based on the ProjectItem or return null if no users accessed the dataset.
     * @param projectItem the project item
     * @return an object array (UserItem, First access, and Last access)
     * for a given ProjectItem or null if no users accessed the dataset
     */
    Collection getAccessTimesByProject(ProjectItem projectItem);

    /**
     * Check if the KCM import is already running for the given dataset.
     * @param datasetItem the dataset
     * @return true if there is no 'finished' info after the last started.
     */
    Date areKcmsImporting(DatasetItem datasetItem);

    /**
     * Returns the date of the given user's last login.
     * @param userItem the given user
     * @return the date of the user's last login
     */
    Date getLastLogin(UserItem userItem);

    /**
     * Get a list of N projects for a given user.
     * @param userItem given user
     * @param limit N
     * @return a list of project names
     */
    List<String> getLastProjects(UserItem userItem, Integer limit);

    /**
     * Gets a list of users and their last access times for a given dataset.
     * @return a list of users and their last access times for a given dataset
     */
    List getAccessTimes(DatasetItem datasetItem);
}