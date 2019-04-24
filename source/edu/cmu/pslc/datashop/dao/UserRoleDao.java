/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.item.UserRoleId;
import edu.cmu.pslc.datashop.item.UserRoleItem;

/**
 * User Role Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15495 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-09-28 08:33:58 -0400 (Fri, 28 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface UserRoleDao extends AbstractDao {

    /**
     * Standard get for an User Role item by UserRoleId.
     * @param userRoleId the UserRoleId of the desired UserRoleItem.
     * @return the matching UserRoleItem or null if none found
     */
    UserRoleItem get(UserRoleId userRoleId);

    /**
     * Standard "find all" for User Role items.
     * @return a List of objects
     */
    List findAll();

    /**
     * Standard find for an User Role item by UserRoleId.
     * Only guarantees the id of the item will be filled in.
     * @param userRoleId the UserRoleId of the desired UserRoleItem.
     * @return the matching UserRoleItem.
     */
    UserRoleItem find(UserRoleId userRoleId);

    //
    // Non-standard methods begin.
    //

    /**
     * find roles that pertains to a specific user.
     * @param userId the id of the user
     * @return a List of objects
     */
    List<UserRoleItem> find(String userId);

    /**
     * Check if the user is an administrator or has the logging activity role.
     * @param userItem the given user
     * @return true if the given user is authorized to run the logging activity report
     */
    boolean hasLoggingActivityAuth(UserItem userItem);

    /**
     * Check if the user is an administrator or has the web services role.
     * @param userItem the given user
     * @return true if the given user is authorized to request an access key for web services
     */
    boolean hasWebServicesAuth(UserItem userItem);

    /**
     * Determine if the user has the terms of user manager role.
     * @param userItem the given user
     * @return true if the given user has the terms of user manager role
     */
    boolean hasTermsManagerRole(UserItem userItem);

    /**
     * Determine if the user has the research manager role.
     * @param userItem the given user
     * @return true if the given user has the research manager role
     */
    boolean hasResearchManagerRole(UserItem userItem);

    /**
     * Determine if the user has the datashop edit role.
     * @param userItem the given user
     * @return true if the given user has the datashop edit role
     */
    boolean hasDatashopEditRole(UserItem userItem);

    /**
     * Determine if the user has the research goal edit role.
     * @param userItem the given user
     * @return true if the given user has the datashop edit role
     */
    boolean hasResearchGoalEditRole(UserItem userItem);

} // end interface
