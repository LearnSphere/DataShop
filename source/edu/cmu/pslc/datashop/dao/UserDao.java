/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.UserItem;

/**
 * User Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14923 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-13 12:38:51 -0400 (Tue, 13 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface UserDao extends AbstractDao<UserItem> {

    /**
     * Standard get for a UserItem by id.
     * @param id The id of the UserItem.
     * @return the matching UserItem or null if none found
     */
    UserItem get(String id);

    /**
     * Standard find for an UserItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired UserItem.
     * @return the matching UserItem.
     */
    UserItem find(String id);

    /**
     * Standard "find all" for UserItems.
     * @return a List of objects
     */
    List<UserItem> findAll();

    /**
     * Find or Create method for the default user.
     * @return the default user.
     */
    UserItem findOrCreateDefaultUser();

    /**
     * Find or Create method for the system user.
     * @return the system user.
     */
    UserItem findOrCreateSystemUser();

    /**
     * Find the user corresponding to the web services API token.
     * @param apiToken a web services API token
     * @return the user corresponding to the web services API token.
     */
    UserItem findUserWithApiToken(String apiToken);

    /**
     * Find the user with the given last name and email address.
     * @param lastName the user-entered last name
     * @param email the user-entered email address
     * @return the user item if found, null otherwise
     */
    List<UserItem> findByLastNameAndEmail(String lastName, String email);

    /**
      * Find the user with the given id and login type.
     * @param id the user id
     * @param type the login type
     * @return the user item if found, null otherwise
     */
    List<UserItem> findByIdAndType(String id, String type);

    /**
      * Find the user with the given login id and login type.
     * @param id the user login id (Google, GitHub or LinkedIn-assigned)
     * @param type the login type
     * @return the user item if found, null otherwise
     */
    List<UserItem> findByLoginIdAndType(String id, String type);


    /**
     * Retrieves users by taking the non-null fields in the criteria and applying 'LIKE' operation.
     * The compared fields are: id, name (for first, last), email, and institution.
     *
     * @param criteria the user item
     * @param offset starting record
     * @param max maximum records to retrieve
     * @return a list of user items
     */
    List<UserItem> findBy(UserItem criteria, int offset, int max);
}
