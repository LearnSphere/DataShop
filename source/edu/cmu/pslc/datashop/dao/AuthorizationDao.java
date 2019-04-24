/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Authorization Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14263 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-15 15:34:02 -0400 (Fri, 15 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface AuthorizationDao extends AbstractDao<AuthorizationItem> {

    /**
     * Standard get for an authorization item by authorizationId.
     * @param authId the AuthorizationId of the desired authorizationItem.
     * @return the matching AuthorizationItem or null if none found
     */
    AuthorizationItem get(AuthorizationId authId);

    /**
     * Standard "find all" for authorization items.
     * @return a List of objects
     */
    List findAll();

    /**
     * Standard find for an authorization item by authorizationId.
     * Only guarantees the id of the item will be filled in.
     * @param authId the AuthorizationId of the desired authorizationItem.
     * @return the matching AuthorizationItem.
     */
    AuthorizationItem find(AuthorizationId authId);

    //
    // Non-standard methods begin.
    //

    /**
     * Get the authorization level string for the given user and dataset.
     * If the user does not have direct authorization to the project of the given
     * dataset, then check for the default user.
     * @param userItem the user item
     * @param datasetItem the dataset item
     * @return authorization level for the given user or public user and dataset
     */
    String getAuthLevel(UserItem userItem, DatasetItem datasetItem);

    /**
     * Get the authorization level string for the given user and project.
     * If the user does not have direct authorization to the project of the given
     * dataset, then check for the default user.
     * @param userId the user id
     * @param projectId the project id
     * @return authorization level for the given user or public user and project
     */
    String getAuthLevel(String userId, Integer projectId);

    /**
     * Returns the authorization level given a user and project, but will
     * return null if not authorized.
     * @param userId the user id
     * @param projectId the project id
     * @return level if authorized, null otherwise
     */
    String getAuthorization(String userId, Integer projectId);

    /**
     * Returns the authorization level for any user and a given project, but will
     * return null if not authorized.
     * @param projectId the project id
     * @return level if authorized, null otherwise
     */
    String getAuthorization(Integer projectId);

    /**
     * Return true if the given user has admin-level access on the given project.
     * @param userId the id of the user
     * @param projectId the id of the project
     * @return true if user has admin, false otherwise
     */
    boolean isProjectAdmin(String userId, Integer projectId);

    /**
     * Return true if the given user has admin-level access on the given project.
     * @param userItem the user
     * @param projectItem the project
     * @return true if user has admin, false otherwise
     */
    boolean isProjectAdmin(UserItem userItem, ProjectItem projectItem);

    /**
     * Returns a list of projects that this user has specified access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    List<ProjectItem> findMyProjects(String userId);

    /**
     * Returns a list of projects that this user has specified access to
     * or all projects if they are admin.
     * @param userId the user id
     * @param adminFlag the adminFlag
     * @return a list of AuthorizationItems
     */
    List<ProjectItem> findMyEditableProjectsForImports(String userId, Boolean isDatashopOrProjectAdmin);


    /**
     * Returns a list of projects that this user has view or greater access to
     * or all projects if they are admin.
     * @param userId the user id
     * @param adminFlag the adminFlag
     * @return a list of AuthorizationItems
     */
    List<ProjectItem> findMyViewableProjectsForImports(String userId, Boolean isDatashopOrProjectAdmin);
    /**
     * Get a count of the number of accessible samples by user and dataset.
     * @param userId the userId to check.
     * @param datasetId the datasetId
     * @return a count of the number of accessible samples in the given dataset
     * for the provided user.
     */
    int findMySamples(String userId, Integer datasetId);

    /**
     * Get a count of the number of accessible samples by user and dataset.
     * @param user the userId to check.
     * @param dataset the datasetId
     * @return a count of the number of accessible samples in the given dataset
     * for the provided user.
     */
    int findMySamples(UserItem user, DatasetItem dataset);

    /**
     * Returns a list of projects that any user has access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    List<ProjectItem> findPublicProjects(String userId);

    /**
     * Returns a list of projects that this user does not have specified
     * or public access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    List findAvailableProjects(String userId);

    /**
     * Returns true if the project is public.
     * @param projectId the project id
     * @return true if the project is public, false otherwise
     */
    boolean isPublic(Integer projectId);

    /**
     * Find authorization Id's for users in the authorization table by PI or DP.
     * @param ownerId the user Id of the PI or DP
     * @return a list of authorization Id's
     */
    Collection findAuthsByOwner(String ownerId);

    /**
     * Find authorization Id's for all users in the authorization table.
     * @return a list of authorization Id's
     */
    Collection findAllAuthIds();

    /**
     * Return a list of user items which have admin access given a project id.
     * @param projectId the id of the project
     * @return a list of user items, empty list if none found
     */
    List<UserItem> findProjectAdmins(Integer projectId);

    /**
     * Return a list of project items which the given user has admin access for.
     * @param user the user item
     * @return a list of project items, empty list if none found
     */
    List<ProjectItem> findProjectsByAdmin(UserItem user);

    /**
     * Return a list of Authorization items for the specified project.
     *
     * @param project the ProjectItem
     * @return list of AuthorizationItem objects
     */
    List<AuthorizationItem> findAuthsByProject(ProjectItem project);

    /**
     * Return a list of Authorization items for the specified user.
     *
     * @param user the UserItem
     * @return list of AuthorizationItem objects
     */
    List<AuthorizationItem> findAuthsByUser(UserItem user);

} // end interface
