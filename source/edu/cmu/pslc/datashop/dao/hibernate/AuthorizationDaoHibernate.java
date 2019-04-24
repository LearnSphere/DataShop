/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the AuthorizationDao.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14263 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-15 15:34:02 -0400 (Fri, 15 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthorizationDaoHibernate extends AbstractDaoHibernate<AuthorizationItem>
implements AuthorizationDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for an authorization item by authorizationId.
     * @param authId the AuthorizationId of the desired authorizationItem.
     * @return the matching AuthorizationItem or null if none found
     */
    public AuthorizationItem get(AuthorizationId authId) {
        return (AuthorizationItem)get(AuthorizationItem.class, authId);
    }

    /**
     * Standard "find all" for authorization items.
     * @return a List of objects which are AuthorizationItems
     */
    public List<AuthorizationItem> findAll() {
        return findAll(AuthorizationItem.class);
    }

    /**
     * Standard find for an authorization item by authorizationId.
     * Find will return a proxy item with just the Id.  Any attempt to access
     * fields of an item retrieved by find will result in a
     * org.hibernate.LazyInitializationException unless a session is reopened.
     * @param authId the AuthorizationId of the desired authorizationItem.
     * @return the matching AuthorizationItem.
     */
    public AuthorizationItem find(AuthorizationId authId) {
        return (AuthorizationItem)find(AuthorizationItem.class, authId);
    }

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
    public String getAuthLevel(UserItem userItem, DatasetItem datasetItem) {
        String level = null;
        if (datasetItem != null && datasetItem.getProject() != null) {
            level = getAuthLevel(
                    (String)userItem.getId(),
                    (Integer)datasetItem.getProject().getId());
        }
        return level;
    }

    /**
     * Get the authorization level string for the given user and project.
     * If the user does not have direct authorization to the project of the given
     * dataset, then check for the default user.
     * @param userId the user id
     * @param projectId the project id
     * @return authorization level for the given user or public user and project
     */
    public String getAuthLevel(String userId, Integer projectId) {
        String level = null;
        if (userId != null && projectId != null) {
            String userLevel = getAuthorization(userId, projectId);
            String publicLevel = getAuthorization(projectId);
            if (userLevel == null) {
                level = publicLevel;
            } else if (publicLevel == null) {
                level = userLevel;
            } else {
                if (userLevel.equals(AuthorizationItem.LEVEL_ADMIN)
                        || publicLevel.equals(AuthorizationItem.LEVEL_ADMIN)) {
                    level = AuthorizationItem.LEVEL_ADMIN;
                } else if (userLevel.equals(AuthorizationItem.LEVEL_EDIT)
                        || publicLevel.equals(AuthorizationItem.LEVEL_EDIT)) {
                    level = AuthorizationItem.LEVEL_EDIT;
                } else {
                    level = AuthorizationItem.LEVEL_VIEW;
                }
            }
        }
        return level;
    }

    /**
     * Returns the authorization level given a user and project, but will
     * return null if not authorized.
     * @param userId the user id
     * @param projectId the project id
     * @return level if authorized, null otherwise
     */
    public String getAuthorization(String userId, Integer projectId) {
        String level = null;
        if (userId != null) {
            AuthorizationId authId = new AuthorizationId(userId, projectId);
            AuthorizationItem item = get(authId);
            if (item != null) {
                level = item.getLevel();
            }
        }
        return level;
    }

    /**
     * Returns the authorization level for any user and a given project, but will
     * return null if not authorized.
     * @param projectId the project id
     * @return level if authorized, null otherwise
     */
    public String getAuthorization(Integer projectId) {
        return getAuthorization(UserItem.DEFAULT_USER, projectId);
    }

    /**
     * Return true if the given user has admin-level access on the given project.
     * @param userId the id of the user
     * @param projectId the id of the project
     * @return true if user has admin, false otherwise
     */
    public boolean isProjectAdmin(String userId, Integer projectId) {
        boolean adminFlag = false;
        String level = getAuthorization(userId, projectId);
        if (AuthorizationItem.LEVEL_ADMIN.equals(level)) {
            adminFlag = true;
        }
        return adminFlag;
    }

    /**
     * Return true if the given user has admin-level access on the given project.
     * @param userItem the user
     * @param projectItem the project
     * @return true if user has admin, false otherwise
     */
    public boolean isProjectAdmin(UserItem userItem, ProjectItem projectItem) {
        if ((userItem == null) || (projectItem == null)) {
            return false;
        }
        return isProjectAdmin((String)userItem.getId(), (Integer)projectItem.getId());
    }

    /**
     * Returns a list of projects that this user has specified access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    public List findMyProjects(String userId) {
        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") starting.");
        }
        StringBuffer query = new StringBuffer();
        query.append("select distinct authorization.project ");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");
        query.append(" join authorization.project p");
        query.append(" where authorization.id.userId = ?");
        query.append(" order by p.projectName");
        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") query: " + query);
        }
        List projects =  getHibernateTemplate().find(query.toString(), userId);
        return projects;
    }


    /**
     * Returns a list of projects that this user has specified access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    public List findMyEditableProjectsForImports(String userId, Boolean isAdminFlag) {
        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") starting.");
        }

        StringBuffer query = new StringBuffer();
        List projects =  null;
        if (isAdminFlag) {
            query.append(" from " + ProjectItem.class.getName() + " p");
            query.append(" order by p.projectName");
            projects =  getHibernateTemplate().find(query.toString());
        } else {
            query.append("select distinct authorization.project ");
            query.append(" from " + AuthorizationItem.class.getName() + " authorization");
            query.append(" join authorization.project p");
            query.append(" where authorization.id.userId = ?");
            query.append(" and (authorization.level = 'edit' or authorization.level = 'admin')");
            query.append(" order by p.projectName");
            projects =  getHibernateTemplate().find(query.toString(), userId);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") query: " + query);
        }

        return projects;
    }

    /**
     * Returns a list of projects that this user has view or greater access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    public List findMyViewableProjectsForImports(String userId, Boolean isAdminFlag) {
        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") starting.");
        }

        StringBuffer query = new StringBuffer();
        List projects =  null;
        if (isAdminFlag) {
            query.append(" from " + ProjectItem.class.getName() + " p");
            query.append(" order by p.projectName");
            projects =  getHibernateTemplate().find(query.toString());
        } else {
            query.append("select distinct authorization.project ");
            query.append(" from " + AuthorizationItem.class.getName() + " authorization");
            query.append(" join authorization.project p");
            query.append(" where authorization.id.userId = ?");
            query.append(" and (authorization.level = 'view' or authorization.level = 'edit' or authorization.level = 'admin')");
            query.append(" order by p.projectName");
            projects =  getHibernateTemplate().find(query.toString(), userId);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("findMyProjects(" + userId + ") query: " + query);
        }

        return projects;
    }

    /**
     * Get a count of the number of accessible samples by user and dataset.
     * @param user the userId to check.
     * @param dataset the datasetId
     * @return a count of the number of accessible samples in the given dataset
     * for the provided user.
     */
    public int findMySamples(UserItem user, DatasetItem dataset) {
        if (user != null && dataset != null) {
            return findMySamples((String)user.getId(), (Integer)dataset.getId());
        } else {
            return -1;
        }
    }

    /**
     * Get a count of the number of accessible samples by user and dataset.
     * @param userId the userId to check.
     * @param datasetId the datasetId
     * @return a count of the number of accessible samples in the given dataset
     * for the provided user.
     */
    public int findMySamples(String userId, Integer datasetId) {
        int result = 0;
        StringBuffer query = new StringBuffer();
        query.append("SELECT COUNT(DISTINCT samp.sample_id) as count");
        query.append(" FROM sample samp");
        query.append(" JOIN ds_dataset ds USING (dataset_id)");
        query.append(" JOIN project USING (project_id)");
        query.append(" JOIN authorization auth USING (project_id)");
        query.append(" WHERE (auth.user_id = :userId OR auth.user_id='%')");
        query.append(" AND (ds.deleted_flag = false OR ds.deleted_flag is NULL)");
        query.append(" AND ds.dataset_id = :datasetId");
        query.append(" AND (samp.global_flag = 1 OR samp.owner = :userId)");
        if (logger.isTraceEnabled()) {
            logger.trace("findMySamples(" + userId + ") query:" + query.toString());
        }
        Session session = getSession();
        SQLQuery sqlQuery = session.createSQLQuery(query.toString());
        sqlQuery.setString("userId", userId);
        sqlQuery.setInteger("datasetId", datasetId);
        sqlQuery.addScalar("count", Hibernate.INTEGER);

        List queryResults = sqlQuery.list();
        if (queryResults.size() >= 0) {
            result = (Integer)queryResults.get(0);
        }
        releaseSession(session);
        return result;
    }

    /**
     * Returns a list of projects that any user has access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    public List findPublicProjects(String userId) {
        if (logger.isTraceEnabled()) {
            logger.trace("findPublicProjects(" + userId + ") starting.");
        }

        List myList = findMyProjects(userId);
        List publicList = findMyProjects(UserItem.DEFAULT_USER);
        if (logger.isTraceEnabled()) {
            logger.trace("findPublicProject: myList has " + myList.size() + " projects"
                    + " and has the following projects: " + myList);
            logger.trace("findPublicProject: publicList has " + publicList.size() + " projects"
                    + " and has the following projects: " + publicList);
        }
        publicList.removeAll(myList);
        Collections.sort(publicList);
        if (logger.isTraceEnabled()) {
            logger.trace("findPublicProject: publicList has " + publicList.size()
                    + " projects after removing my projects and sorting"
                    + " and has the following projects: " + publicList);
        }
        return publicList;
    }

    /**
     * Returns a list of projects that this user does not have specified
     * or public access to.
     * @param userId the user id
     * @return a list of AuthorizationItems
     */
    public List findAvailableProjects(String userId) {
        if (logger.isTraceEnabled()) {
            logger.trace("findAvailableProjects(" + userId + ") starting.");
        }
        List allList = DaoFactory.DEFAULT.getProjectDao().findAll();
        List myList = findMyProjects(userId);
        List publicList = findPublicProjects(userId);
        allList.removeAll(myList);
        allList.removeAll(publicList);
        Collections.sort(allList);
        return allList;
    }

    /**
     * Returns true if the project is public.
     * @param projectId the project id
     * @return true if the project is public, false otherwise
     */
    public boolean isPublic(Integer projectId) {
        StringBuffer query = new StringBuffer();
        query.append("select distinct authorization.project ");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");
        query.append(" join authorization.project p");
        query.append(" where authorization.id.userId = '%'");
        query.append(" and authorization.id.projectId = ?");
        List list = getHibernateTemplate().find(query.toString(), projectId);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Find authorization Id's for users in the authorization table by PI or DP.
     * @param ownerId the user Id of the PI or DP
     * @return a list of authorization Id's
     */
    public List<AuthorizationId> findAuthsByOwner(String ownerId) {
        StringBuffer query = new StringBuffer();
        Object[] params = {ownerId, ownerId};
        query.append("select distinct authorization.id ");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");
        query.append(" left join authorization.project p");
        query.append(" where p.primaryInvestigator.id = ?");
        query.append(" or p.dataProvider.id = ?");

        return getHibernateTemplate().find(query.toString(), params);
    }

    /**
     * Find authorization Id's for all users in the authorization table.
     * @return a list of authorization Id's
     */
    public List<AuthorizationId> findAllAuthIds() {
        StringBuffer query = new StringBuffer();
        query.append("select distinct authorization.id ");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");

        return getHibernateTemplate().find(query.toString());
    }

    /**
     * Return a list of user items which have admin access given a project id.
     * @param projectId the id of the project
     * @return a list of user items, empty list if none found
     */
    public List<UserItem> findProjectAdmins(Integer projectId) {
        StringBuffer query = new StringBuffer();

        Object[] params = {AuthorizationItem.LEVEL_ADMIN, projectId};
        query.append("select distinct user");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");
        query.append(" where authorization.level = ?");
        query.append(" and authorization.id.projectId = ?");

        return getHibernateTemplate().find(query.toString(), params);
    }

    /**
     * Return a list of project items which the given user has admin access for.
     * @param user the user item
     * @return a list of project items, empty list if none found
     */
    public List<ProjectItem> findProjectsByAdmin(UserItem user) {
        StringBuffer query = new StringBuffer();

        Object[] params = {AuthorizationItem.LEVEL_ADMIN, (String)user.getId()};
        query.append("select p");
        query.append(" from " + AuthorizationItem.class.getName() + " authorization");
        query.append(" join authorization.project p");
        query.append(" where authorization.level = ?");
        query.append(" and authorization.id.userId = ?");
        query.append(" order by LOWER(p.projectName)");

        return getHibernateTemplate().find(query.toString(), params);
    }

    private static final String AUTH_ITEMS_BY_PROJECT = 
        "SELECT au FROM AuthorizationItem AS au WHERE au.project = ?";

    /**
     * Return a list of Authorization items for the specified project.
     *
     * @param project the ProjectItem
     * @return list of AuthorizationItem objects
     */
    public List<AuthorizationItem> findAuthsByProject(ProjectItem project) {
        List<AuthorizationItem> result = getHibernateTemplate().
            find(AUTH_ITEMS_BY_PROJECT, project);
        return result;
    }

    private static final String AUTH_ITEMS_BY_USER = 
        "SELECT au FROM AuthorizationItem au WHERE au.user = ?";

    /**
     * Return a list of Authorization items for the specified user.
     *
     * @param user the UserItem
     * @return list of AuthorizationItem objects
     */
    public List<AuthorizationItem> findAuthsByUser(UserItem user) {
        List<AuthorizationItem> result = getHibernateTemplate().
            find(AUTH_ITEMS_BY_USER, user);
        return result;
    }
}