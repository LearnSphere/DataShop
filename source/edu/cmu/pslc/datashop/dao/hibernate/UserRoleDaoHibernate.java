/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleId;
import edu.cmu.pslc.datashop.item.UserRoleItem;

/**
 * Hibernate and Spring implementation of the UserRoleDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15495 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-09-28 08:33:58 -0400 (Fri, 28 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserRoleDaoHibernate extends AbstractDaoHibernate implements UserRoleDao {

    /** Logger for this class */
    //private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for an User Role item by UserRoleId.
     * @param userRoleId the UserRoleId of the desired UserRoleItem.
     * @return the matching UserRoleItem or null if none found
     */
    public UserRoleItem get(UserRoleId userRoleId) {
        return (UserRoleItem)get(UserRoleItem.class, userRoleId);
    }

    /**
     * Standard "find all" for User Role items.
     * @return a List of objects
     */
    public List<UserRoleItem> findAll() {
        return findAll(UserRoleItem.class);
    }

    /**
     * Standard find for an User Role item by UserRoleId.
     * Only guarantees the id of the item will be filled in.
     * @param userRoleId the UserRoleId of the desired UserRoleItem.
     * @return the matching UserRoleItem.
     */
    public UserRoleItem find(UserRoleId userRoleId) {
        return (UserRoleItem)find(UserRoleItem.class, userRoleId);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * find roles that pertains to a specific user. (DS1430)
     * @param userId the id of the user
     * @return a List of objects
     */
    public List<UserRoleItem> find(String userId) {
        return getHibernateTemplate().find(
                "from UserRoleItem user_role where user.id = ?", userId);
    }

    /**
     * Check if the user is an administrator or has the logging activity role.
     * @param userItem the given user
     * @return true if the given user is authorized to run the logging activity report
     *
     */
    public boolean hasLoggingActivityAuth(UserItem userItem) {
        if (userItem == null) {
            return false;
        } else if (userItem.getAdminFlag()) {
            return true;
        }
        return hasLoggingActivityRole(userItem);
    }

    /**
     * Determine if the user has the logging activity role.
     * @param userItem the given user
     * @return true if the given user has the logging activity role
     */
    private boolean hasLoggingActivityRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'logging_activity'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasLoggingActivityRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

    /**
     * Check if the user is an administrator or has the web services role.
     * @param userItem the given user
     * @return true if the given user is authorized to request an access key for web services
     */
    public boolean hasWebServicesAuth(UserItem userItem) {
        if (userItem == null) {
            return false;
        } else if (userItem.getAdminFlag()) {
            return true;
        }
        return hasWebServicesRole(userItem);
    }

    /**
     * Determine if the user has the logging activity role.
     * @param userItem the given user
     * @return true if the given user has the logging activity role
     */
    private boolean hasWebServicesRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'web_services'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasWebServicesRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

    /**
     * Determine if the user has the terms of user manager role.
     * @param userItem the given user
     * @return true if the given user has the terms of user manager role
     */
    public boolean hasTermsManagerRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'terms_of_use_manager'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasTermsManagerRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

    /**
     * Determine if the user has the research manager role.
     * @param userItem the given user
     * @return true if the given user has the terms of user manager role
     */
    public boolean hasResearchManagerRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'research_manager'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasResearchManagerRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

    /**
     * Determine if the user has the datashop edit role.
     * @param userItem the given user
     * @return true if the given user has the datashop edit role
     */
    public boolean hasDatashopEditRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'datashop_edit'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasDatashopEditRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

    /**
     * Determine if the user has the research goal edit role.
     * @param userItem the given user
     * @return true if the given user has the datashop edit role
     */
    public boolean hasResearchGoalEditRole(UserItem userItem) {
        boolean flag = false;
        if (userItem != null) {
            String query = "SELECT count(*) FROM UserRoleItem user_role_item"
                    + " WHERE role = 'research_goal_edit'"
                    + " AND user.id = ?";

            Long numResults = (Long)getHibernateTemplate().find(query, userItem.getId()).get(0);
            if (numResults != null && numResults > 0) {
                flag = true;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("hasResearchGoalEditRole (" + userItem.getId() + "): " + flag);
        }
        return flag;
    }

}
