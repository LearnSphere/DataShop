/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.irb;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * DTO for a single ProjectShareabilityHistory item on a project IRB page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10527 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-02-07 10:17:15 -0500 (Fri, 07 Feb 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectShareabilityHistoryDto {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Project id . */
    private Integer projectId;
    /** UpdatedBy user. */
    private UserItem updatedBy;
    /** UpdatedTime. */
    private Date updatedTime;
    /** ShareableStatus. */
    private String shareableStatus;

    /** Constant for formatting of dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id
     * @param projectId the id of the project
     * @param updatedBy the user that last updated this project
     * @param updatedTime the last time this project was updated
     * @param shareableStatus status of this project's shareability
     */
    public ProjectShareabilityHistoryDto(Integer id, Integer projectId,
                                         UserItem updatedBy, Date updatedTime,
                                         String shareableStatus) {
        this.id = id;
        this.projectId = projectId;
        this.updatedBy = updatedBy;
        this.updatedTime = updatedTime;
        this.shareableStatus = shareableStatus;
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the id of this project.
     * @return Integer project id
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Set the id of this project.
     * @param projectId the project id
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the user that updated this project.
     * @return UserItem the user item
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user that updated this project.
     * @param userItem the user item
     */
    public void setUpdatedBy(UserItem userItem) {
        this.updatedBy = userItem;
    }

    /**
     * Get the name of the user that updated this project.
     * @return String user name (first, last)
     */
    public String getUpdatedByString() {
        return updatedBy.getName();
    }

    /**
     * Get the name of the user that updated this project.
     * @return String HTML link with user name and email
     */
    public String getUpdatedByLink() {
        if (updatedBy.getEmail() == null) {
            return updatedBy.getName();
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("<a href=\"mailto:");
            sb.append(updatedBy.getEmail());
            sb.append("\">");
            sb.append(updatedBy.getName());
            sb.append("</a>");
            return sb.toString();
        }
    }

    /**
     * Get the updated time.
     * @return the updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set the updated time.
     * @param updatedTime the updatedTime to set
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get the updated time as a string.
     * @return the updatedTime as a string
     */
    public String getUpdatedTimeString() {
        if (updatedTime == null) {
            return "";
        } else {
            return DATE_FMT.format(updatedTime);
        }
    }

    /**
     * Get the shareability status.
     * @return String shareableStatus
     */
    public String getShareableStatus() {
        return shareableStatus;
    }

    /**
     * Set the shareability status.
     * @param shareableStatus string
     */
    public void setShareableStatus(String shareableStatus) {
        this.shareableStatus = shareableStatus;
    }

    /**
     * Get the Shareability Status, formatted for the JSP.
     * @return String shareabilityStatus
     */
    public String getShareableStatusString() {
        return ProjectItem.getShareabilityStatusString(shareableStatus);
    }
}
