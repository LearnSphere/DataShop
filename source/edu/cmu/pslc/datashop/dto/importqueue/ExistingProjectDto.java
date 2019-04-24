/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto.importqueue;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Data transfer object of items in the Existing Project list.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10527 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-07 10:17:15 -0500 (Fri, 07 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExistingProjectDto {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Integer projectId;
    /** Class attribute. */
    private String projectName;
    /** Class attribute. */
    private String pi;
    /** Class attribute. */
    private String dp;
    /** Class attribute. */
    private String permissions;
    /** Class attribute. */
    private String shareabilityStatus;

    //----- ENUM : permissions -----

    /** Enumerated type list of valid values. */
    private static final List<String> PERM_ENUM = new ArrayList<String>();
    /** Enumerated type constant. */
    public static final String PERM_PRIVATE = "Private project";
    /** Enumerated type constant. */
    public static final String PERM_PUBLIC = "Public project";
    /** Enumerated type constant. */
    public static final String PERM_SOME = "Private project, shared with some";

    static {
        PERM_ENUM.add(PERM_PRIVATE);
        PERM_ENUM.add(PERM_PUBLIC);
        PERM_ENUM.add(PERM_SOME);
    }

    //----- CONSTRUCTOR -----

    /** Default constructor. */
    public ExistingProjectDto() { }

    //----- GETTERS and SETTERS -----

    /**
     * Gets projectId.
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Sets the projectId.
     * @param projectId the projectId to set
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Gets projectName.
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Gets pi.
     * @return the pi
     */
    public String getPi() {
        return pi;
    }

    /**
     * Sets the pi.
     * @param pi the pi to set
     */
    public void setPi(String pi) {
        if (pi == null) {
            this.pi = "";
        } else {
            this.pi = pi;
        }
    }

    /**
     * Gets data provider.
     * @return the data provider
     */
    public String getDp() {
        return dp;
    }

    /**
     * Sets the data provider.
     * @param dp the data provider to set
     */
    public void setDp(String dp) {
        if (dp == null) {
            this.dp = "";
        } else {
            this.dp = dp;
        }
    }

    /**
     * Gets permissions.
     * @return the permissions
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions.
     * @param permissions the permissions to set
     */
    public void setPermissions(String permissions) {
        if (PERM_ENUM.contains(permissions)) {
            this.permissions = permissions;
        } else {
            throw new IllegalArgumentException("Invalid permissions value: " + permissions);
        }
    }

    /**
     * Gets shareabilityReviewStatus.
     * @return the shareabilityReviewStatus
     */
    public String getShareabilityStatus() {
        return shareabilityStatus;
    }

    /**
     * Sets the shareabilityReviewStatus.
     * @param shareabilityReviewStatus the shareabilityReviewStatus to set
     */
    public void setShareabilityStatus(String shareabilityReviewStatus) {
        if (ProjectItem.SHAREABLE_STATUS_ENUM.contains(shareabilityReviewStatus)) {
            this.shareabilityStatus = shareabilityReviewStatus;
        } else {
            throw new IllegalArgumentException(
                    "Invalid shareabilityReviewStatus value: " + shareabilityReviewStatus);
        }
    }

    /**
     * Check if we should show the status on the UI.
     * @return true if shareable or not shareable, false otherwise
     */
    public boolean showShareabilityStatus() {
        boolean flag = false;
        if (shareabilityStatus != null
            &&
           (shareabilityStatus.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE)
         || shareabilityStatus.equals(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
         || shareabilityStatus.equals(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE))) {
            return true;
        }
        return flag;
    }

    /**
     * Get the Shareability Status, formatted for the JSP.
     * @return String shareabilityStatus
     */
    public String getShareabilityStatusString() {
        return ProjectItem.getShareabilityStatusString(shareabilityStatus);
    }
}
