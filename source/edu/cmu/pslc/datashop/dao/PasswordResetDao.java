/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;

import edu.cmu.pslc.datashop.item.PasswordResetItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Password Reset Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7571 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-04-02 15:28:19 -0400 (Mon, 02 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PasswordResetDao extends AbstractDao {

    /**
     * Standard get for a PaperItem by id.
     * @param id The id of the PaperItem.
     * @return the matching PaperItem or null if none found
     */
    PasswordResetItem get(String id);

    /**
     * Standard find for an PaperItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired PaperItem.
     * @return the matching PaperItem.
     */
    PasswordResetItem find(String id);

    /**
     * Standard "find all" for PasswordResetItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns a collection of items given a user.
     * @param user the user who made the request
     * @return a collection of items
     */
    Collection find(UserItem user);
}
