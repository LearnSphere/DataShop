/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Date;

/**
 * Used to transfer User Access Request data.
 *
 * @author Mike Komisin
 * @version $Revision: 7529 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2012-03-20 09:51:25 -0400 (Tue, 20 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserRequestDTO implements java.io.Serializable {

    /** Project Id. */
    private int projectId;
    /** Project name. */
    private String projectName;
    /** Principal Investigator name. */
    private String piName;
    /** Data Provider name. */
    private String dpName;
    /** Access Request level. */
    private String level;
    /** Access Request status. */
    private String status;
    /** Reason provided upon request. */
    private String reason;
    /** Last request date. */
    private Date lastRequest;
    /** Request button title. */
    private String buttonTitle;
    /** Request button enabled flag. */
    private boolean isButtonEnabled;
    /** Access Request recent flag. */
    private boolean isRecent;

    /** Default constructor. */
    public UserRequestDTO() {
    }

    /**
     * Returns the project Id.
     * @return the project Id
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * Sets the project Id.
     * @param projectId the project Id
     */
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    /**
     * Returns the project name.
     * @return the project name
     */
    public String getProjectName() {
        if (projectName == null) {
            return "";
        } else {
            return projectName;
        }
    }

    /**
     * Returns the project name, truncated to n characters.
     * @param n the number of characters to keep
     * @return the truncated project name
     */
    public String getProjectNameTrunc(int n) {
        String dots = "";
        if (projectName == null || projectName.equals("")) {
            return "";
        } else if (n > projectName.length()) {
            n = projectName.length();
        } else {
            dots = "...";
        }
        return projectName.substring(0, n) + dots;
    }

    /**
     * Sets the project name.
     * @param projectName the project name
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the reason.
     * @return the reason
     */
    public String getReason() {
        if (reason != null) {
            return reason.replaceAll("[\t\r\n]+", " ");
        } else {
            return "";
        }
    }

    /**
     * Returns the reason preserving line breaks.
     * @return the reason
     */
    public String getReasonWithLinebreaks() {
        if (reason != null) {
            return reason;
        } else {
            return "";
        }
    }

    /**
     * Sets the reason.
     * @param reason the reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Return the principal investigator name.
     * @return the principal investigator name
     */
    public String getPiName() {
        if (piName == null) {
            return "";
        }
        return piName;
    }

    /**
     * Sets the principal investigator name.
     * @param piName the principal investigator name
     */
    public void setPiName(String piName) {
        this.piName = piName;
    }

    /**
     * Returns the data provider name.
     * @return the data provider name
     */
    public String getDpName() {
        if (dpName == null) {
            return "";
        }
        return dpName;
    }

    /**
     * Sets the data provider name.
     * @param dpName the data provider name
     */
    public void setDpName(String dpName) {
        this.dpName = dpName;
    }

    /**
     * Returns the access request level.
     * @return the access request level
     */
    public String getLevel() {
        if (level == null) {
            return "";
        }
        return level;
    }

    /**
     * Sets the access request level.
     * @param level the access request level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * Returns the access request status.
     * @return the access request status
     */
    public String getStatus() {
        if (status == null) {
            return "";
        }
        return status;
    }

    /**
     * Sets the access request status.
     * @param status the access request status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the last request date.
     * @return the last request date
     */
    public Date getLastRequest() {
        return lastRequest;
    }

    /**
     * Sets the last request date.
     * @param lastRequest the last request date
     */
    public void setLastRequest(Date lastRequest) {
        this.lastRequest = lastRequest;
    }

    /**
     * Returns the request button title.
     * @return the request button title
     */
    public String getButtonTitle() {
        if (buttonTitle == null) {
            return "";
        }
        return buttonTitle;
    }

    /**
     * Sets the request button title.
     * @param buttonTitle the request button title
     */
    public void setButtonTitle(String buttonTitle) {
        this.buttonTitle = buttonTitle;
    }

    /**
     * Returns the request button enabled flag.
     * @return the request button enabled flag
     */
    public boolean isButtonEnabled() {
        return isButtonEnabled;
    }

    /**
     * Sets the request button enabled flag.
     * @param isButtonEnabled the button enabled flag
     */
    public void setButtonEnabled(boolean isButtonEnabled) {
        this.isButtonEnabled = isButtonEnabled;
    }

    /**
     * Returns the request recent flag.
     * @return the request recent flag
     */
    public boolean isRecent() {
        return isRecent;
    }

    /**
     * Sets the request recent flag.
     * @param isRecent the request recent flag
     */
    public void setRecent(boolean isRecent) {
        this.isRecent = isRecent;
    }
}
