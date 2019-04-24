/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_AUTHORIZATION_REQUEST_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_INSTANCE_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_PROJECT_NAME_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_USER_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.invalidParamValueException;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Web service for setting Authorization values: grant, deny, or modify access level.
 *
 * @author Cindy Tipper
 * @version $Revision: 14263 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-15 15:34:02 -0400 (Fri, 15 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthorizationSetService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for this service */
    private static final Set<String> VALID_PARAMS = 
        set(USER_ID, PROJECT_NAME, LEVEL, ACTION);
    /** valid values of the LEVEL param */
    private static final Set<String> VALID_LEVELS = set(AuthorizationItem.LEVEL_ADMIN,
                                                        AuthorizationItem.LEVEL_EDIT,
                                                        AuthorizationItem.LEVEL_VIEW);
    /** GRANT action */
    private static final String GRANT = "grant";
    /** DENY action */
    private static final String DENY = "deny";
    /** MODIFY action */
    private static final String MODIFY = "modify";
    /** valid values of the ACTION param */
    private static final Set<String> VALID_ACTIONS = set(GRANT, DENY, MODIFY);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public AuthorizationSetService(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Set authorization.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_PARAMS);

            UserItem user = validateUserParam();
            ProjectItem project = validateProjectParam();

            String level = (String)this.getParams().get(LEVEL);
            validateParam(level, LEVEL, VALID_LEVELS);

            String action = (String)this.getParams().get(ACTION);
            validateParam(action, ACTION, VALID_ACTIONS);

            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

            // Ensure that the authenticated user has admin permissions
            // on the specified project.
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!hasAdminAccess(authenticatedUser, project)) {
                throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                              "Insufficient access to modify project '"
                                              + project.getProjectName() + "'.");
            }

            AuthorizationItem authItem = authDao.get(new AuthorizationId(user, project));
            if ((authItem != null) && (action.equals(DENY))) {
                authDao.delete(authItem);
            } else if ((authItem != null) && (action.equals(MODIFY))) {
                authItem.setLevel(level);
                authDao.saveOrUpdate(authItem);
            } else if ((authItem == null) && (action.equals(GRANT))) {
                authItem = new AuthorizationItem(user, project);
                authItem.setLevel(level);
                authDao.saveOrUpdate(authItem);
            } else {
                throw new WebServiceException(INVALID_AUTHORIZATION_REQUEST_ERR,
                                              "Invalid request (" + action
                                              + ") for user '" + user.getId()
                                              + "' on project '"
                                              + project.getProjectName()
                                              + "'.");
            }

            if (action.equals(DENY)) {
                writeSuccess("Denied access to user '" + user.getId()
                             + "', for project '" + project.getProjectName()
                             + "'.");
            } else if (action.equals(GRANT)) {
                writeSuccess("Granted access to user '" + user.getId()
                             + "', for project '" + project.getProjectName()
                             + "' at level '" + level + "'.");
            } else {
                writeSuccess("Updated access for user '" + user.getName()
                             + "', on project '" + project.getProjectName()
                             + "', to '" + level + "'.");
            }

        } catch (WebServiceException wse) {
            logger.error("Caught known web service error " + wse.getErrorCode()
                         + ": '" + wse.getErrorMessage() + "'");
            writeError(wse);
        } catch (Exception e) {
            logger.error("Something unexpected went wrong processing web service request.", e);
            writeInternalError();
        }
    }

    /**
     * Validate the specified user id.
     * @return UserItem, if found
     * @throws WebServiceException when the id is not valid
     */
    private UserItem validateUserParam()
        throws WebServiceException
    {
        String userId = (String)this.getParams().get(USER_ID);
        if (userId == null || userId.equals("")) {
            throw paramValueMissingException(USER_ID);
        }

        // Find it...
        UserItem user = null;
        try {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            user = userDao.get(userId);
        } catch (Exception e) {
            throw new WebServiceException(UNKNOWN_ERR,
                                          "Unknown error retrieving user for id '"
                                          + userId + "'.");
        }

        if (user == null) {
            throw new WebServiceException(INVALID_USER_ID_ERR,
                                          "User id '" + userId + "' is not valid.");
        }

        return user;
    }

    /**
     * Validate the specified project id.
     * @return ProjectItem, if found
     * @throws WebServiceException when the id is not valid
     */
    private ProjectItem validateProjectParam()
        throws WebServiceException
    {
        String projectName = (String)this.getParams().get(PROJECT_NAME);
        if (projectName == null || projectName.equals("")) {
            throw paramValueMissingException(PROJECT_NAME);
        }

        // Find it...
        ProjectItem project = null;
        try {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            List<ProjectItem> pList = (List<ProjectItem>)projectDao.find(projectName);
            if ((pList != null) && (pList.size() > 0)) {
                project = (ProjectItem)pList.get(0);
            }
            if (project != null) {
                project = projectDao.get((Integer)project.getId());
            }
        } catch (Exception e) {
            throw new WebServiceException(UNKNOWN_ERR,
                                          "Unknown error retrieving project '"
                                          + projectName + "'.");
        }

        if (project == null) {
            throw new WebServiceException(INVALID_PROJECT_NAME_ERR,
                                          "Project '" + projectName + "' is not valid.");
        }

        return project;
    }

    /**
     * Validate the param.
     * @param paramValue the value to be validated
     * @param paramName the name of the param
     * @param validValues set of valid values for specified param
     * @throws WebServiceException when the value is not valid
     */
    private void validateParam(String paramValue, String paramName, Set<String> validValues)
        throws WebServiceException
    {
        if (paramValue == null || paramValue.equals("")) {
            throw paramValueMissingException(paramName);
        }

        if (validValues != null && !validValues.contains(paramValue)) {
            throw invalidParamValueException(paramName, paramValue);
        }
    }

    /**
     * Helper method to determine if user has admin access to the specified project.
     * DataShop Admin users have access.
     *
     * @param user the authenticated user item
     * @param project the project of interest
     * @return boolean indicating if user has admin access
     */
    private Boolean hasAdminAccess(UserItem user, ProjectItem project) {

        if (user.getAdminFlag()) { return true; }

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        AuthorizationItem authItem = authDao.get(new AuthorizationId(user, project));
        
        if (authItem == null) { return false; }

        String level = authItem.getLevel();
        if (level.equals(AuthorizationItem.LEVEL_ADMIN)) { return true; }

        return false;
    }
}
