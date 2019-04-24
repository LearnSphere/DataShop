/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;

import java.util.Date;

import edu.cmu.pslc.datashop.servlet.webservices.WebServiceUserLog;
import edu.cmu.pslc.datashop.workflows.WorkflowUserLogItem;

/**
 * Returned from the handle method. Helps us decide what to log.
 *
 * @author hui cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebServiceUserLog {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Boolean handled = false;
    /** Class attribute. */
    private WorkflowUserLogItem userLogItem = new WorkflowUserLogItem();

    //----- CONSTRUCTOR -----

    /** Constructor. */
    LearnSphereWebServiceUserLog() { }

    //----- CONVENIENCE SETTERS -----

    /**
     * Gets the user on the user log item.
     * @return String 
     */
    public String getUser() {
        return userLogItem.getUser();
    }
    /**
     * Sets the user on the user log item.
     * @param String the user id
     */
    public void setUser(String user) {
        userLogItem.setUser(user);
    }
    /**
     * Sets the action on the user log item.
     * @param action the action
     */
    public void setAction(String action) {
        userLogItem.setAction(action);
    }
    /**
     * Sets the info on the user log item.
     * @param info the info
     */
    public void setInfo(String info) {
        userLogItem.setInfo(info);
    }
    /**
     * Sets the time on the user log item.
     * @param Date the info
     */
    public void setTime(Date time) {
        userLogItem.setTime(time);
    }
    /**
     * Sets the workflow on the user log item.
     * @param Date the info
     */
    public void setWorkflow(long workflow) {
        userLogItem.setWorkflow(workflow);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Gets handled.
     * @return the handled
     */
    public Boolean getHandled() {
        return handled;
    }
    /**
     * Sets the handled.
     * @param handled the handled to set
     */
    public void setHandled(Boolean handled) {
        this.handled = handled;
    }
    /**
     * Gets userLogItem.
     * @return the userLogItem
     */
    public WorkflowUserLogItem getUserLogItem() {
        return userLogItem;
    }
    /**
     * Sets the userLogItem.
     * @param userLogItem the userLogItem to set
     */
    public void setUserLogItem(WorkflowUserLogItem userLogItem) {
        this.userLogItem = userLogItem;
    }
}
