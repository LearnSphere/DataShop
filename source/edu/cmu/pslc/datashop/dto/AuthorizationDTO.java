/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Used to transfer authorization info as XML, JSON, etc.
 *
 * @author Cindy Tipper
 * @version $Revision: 14265 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-18 13:31:22 -0400 (Mon, 18 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties (root = "authorization", properties = { "user", "userName", "project", "level" })
public class AuthorizationDTO extends DTO {
    /** user id */
    private String user;
    /** user name */
    private String userName;
    /** project name */
    private String project;
    /** access level */
    private String level;

    /** user. @return user id */
    public String getUser() { return user; }

    /** user. @param user the user id */
    public void setUser(String user) { this.user = user; }

    /** userName. @return user name */
    public String getUserName() { return userName; }

    /** userName. @param userName the user name */
    public void setUserName(String userName) { this.userName = userName; }

    /** project. @return project name */
    public String getProject() { return project; }

    /** project. @param project the project name */
    public void setProject(String project) { this.project = project; }

    /** level. @return access level */
    public String getLevel() { return level; }

    /** level. @param level the access level */
    public void setLevel(String level) { this.level = level; }
}
