/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.util.ArrayList;
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

import edu.cmu.pslc.datashop.dto.AuthorizationDTO;

import edu.cmu.pslc.datashop.item.AuthorizationId;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_PROJECT_NAME_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.INVALID_USER_ID_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNAUTHORIZED_USER_ERR;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.UNKNOWN_ERR;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

import static java.lang.String.format;

/**
 * Web service for querying Authorization values by user and project.
 *
 * @author Cindy Tipper
 * @version $Revision: 14263 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-15 15:34:02 -0400 (Fri, 15 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AuthorizationService extends WebService {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** the parameters that are valid for this service */
    private static final Set<String> VALID_PARAMS = set(USER_ID, PROJECT_NAME);

    /**
     * Constructor.
     * @param req the web service request
     * @param resp the web service response
     * @param params all parameters for this request, including path parameters
     */
    public AuthorizationService(HttpServletRequest req,
                                HttpServletResponse resp,
                                Map<String, Object> params) {
        super(req, resp, params);
    }

    /**
     * Query authorization.
     * @param wsUserLog web service user log
     */
    public void get(WebServiceUserLog wsUserLog) {
        try {
            validateParameters(wsUserLog, VALID_PARAMS);

            UserItem user = validateUserParam();
            ProjectItem project = validateProjectParam();

            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

            // Ensure that the authenticated user has permissions on specified project.
            UserItem authenticatedUser = getAuthenticatedUser();
            if (!hasAdminAccess(authenticatedUser, project)) {
                if ((user == null) && (project == null)) {
                    // User has asked for all Authorization info.
                    throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                                  "Insufficient access to query "
                                                  + "authorization table.");
                } else if (user == null) {
                    // User has asked for Auth info for a specific project.
                    throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                                  "Insufficient access to query project '"
                                                  + project.getProjectName() + "'.");
                } else if (project == null) {
                    // User has asked for Auth info for a specific user.
                    throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                                  "Insufficient access to query user '"
                                                  + user.getUserName() + "'.");
                } else {
                    throw new WebServiceException(UNAUTHORIZED_USER_ERR,
                                                  "Insufficient access to query user/project '"
                                                  + user.getId() + "/"
                                                  + project.getProjectName() + "'.");
                }
            }

            List<AuthorizationItem> authItems = new ArrayList<AuthorizationItem>();
            if ((user == null) && (project == null)) {
                // User has asked for all Authorization info.
                authItems = authDao.findAll();
            } else if (user == null) {
                // User has asked for Auth info for a specific project.
                authItems = authDao.findAuthsByProject(project);
            } else if (project == null) {
                // User has asked for Auth info for a specific user.
                authItems = authDao.findAuthsByUser(user);
            } else {
                // User wants info for a specific user/project pair.
                AuthorizationId authId = new AuthorizationId(user, project);
                authItems.add(authDao.find(authId));
            }

            // Loop through authItems list, creating DTOs
            List<AuthorizationDTO> dtos = helper().authorizationDTOs(authItems);
            writeDTOXML(dtos, format("Success. %d auth item(s) found.", dtos.size()));

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
     * Validate the specified user id, return null if not found.
     * @return UserItem, if found
     * @throws WebServiceException when the id is not valid
     */
    private UserItem validateUserParam()
        throws WebServiceException
    {
        String userId = (String)this.getParams().get(USER_ID);
        if (userId == null || userId.equals("")) {
            return null;
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

        // If user still null, named user doesn't exist.
        if (user == null) {
            throw new WebServiceException(INVALID_USER_ID_ERR,
                                          "User id '" + userId + "' is not valid.");
        }

        return user;
    }

    /**
     * Validate the specified project id, return null if not found.
     * @return ProjectItem, if found
     * @throws WebServiceException when the id is not valid
     */
    private ProjectItem validateProjectParam()
        throws WebServiceException
    {
        String projectName = (String)this.getParams().get(PROJECT_NAME);
        if (projectName == null || projectName.equals("")) {
            return null;
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

        // If project still null, named project doesn't exist.
        if (project == null) {
            throw new WebServiceException(INVALID_PROJECT_NAME_ERR,
                                          "Project '" + projectName + "' is not valid.");
        }

        return project;
    }

    /**
     * Helper method to determine if user has admin access to the specified project.
     * If the project is null, only DataShop Admin users have access.
     *
     * @param user the authenticated user item
     * @param project the project of interest
     * @return boolean indicating if user has admin access
     */
    private Boolean hasAdminAccess(UserItem user, ProjectItem project) {

        if (user.getAdminFlag()) { return true; }

        // If project isn't specified, user isn't DS Admin so the answer is no.
        if (project == null) { return false; }

        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
        AuthorizationItem authItem = authDao.get(new AuthorizationId(user, project));

        // Confirm that authorized user has admin access to project.
        if (authItem == null) { return false; }

        String level = authItem.getLevel();
        if (level.equals(AuthorizationItem.LEVEL_ADMIN)) { return true; }

        return false;
    }
}
