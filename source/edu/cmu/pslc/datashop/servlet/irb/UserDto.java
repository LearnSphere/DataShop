/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import edu.cmu.pslc.datashop.item.UserItem;

/**
 * DTO for the PI/DP/PAs of a project.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10513 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-03 12:56:21 -0500 (Mon, 03 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserDto {

    //----- CONSTANTS -----

    //----- ATTRIBUTES -----

    /** Attribute. */
    private String name;
    /** Attribute. */
    private String email;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param userItem the user item
     * @param role the user's role for this project
     */
    public UserDto(UserItem userItem, String role) {
        setName(userItem.getName() + " (" + role + ")");
        setEmail(userItem.getEmail());
    }

    //----- GETTERs and SETTERs -----

    /**
     * Returns the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the email.
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

}