/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserLogItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Returned from the handle method. Helps us decide what to log.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServiceUserLog {

    //----- ATTRIBUTES -----

    /** Class attribute. */
    private Boolean handled = false;
    /** Class attribute. */
    private DatasetUserLogItem userLogItem = new DatasetUserLogItem();

    //----- CONSTRUCTOR -----

    /** Constructor. */
    WebServiceUserLog() { }

    //----- CONVENIENCE SETTERS -----

    /**
     * Gets the dataset on the user log item.
     * @return datasetItem
     */
    public DatasetItem getDataset() {
        return userLogItem.getDataset();
    }
    /**
     * Sets the dataset on the user log item.
     * @param datasetItem the dataset item
     */
    public void setDataset(DatasetItem datasetItem) {
        userLogItem.setDataset(datasetItem);
    }
    /**
     * Gets the dataset on the user log item.
     * @return datasetItem
     */
    public UserItem getUser() {
        return userLogItem.getUser();
    }
    /**
     * Sets the user on the user log item.
     * @param userItem the user item
     */
    public void setUser(UserItem userItem) {
        userLogItem.setUser(userItem);
    }
    /**
     * Sets the user on the user log item.
     * @param action the action
     */
    public void setAction(String action) {
        userLogItem.setAction(action);
    }
    /**
     * Sets the user on the user log item.
     * @param info the info
     */
    public void setInfo(String info) {
        userLogItem.setInfo(info);
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
    public DatasetUserLogItem getUserLogItem() {
        return userLogItem;
    }
    /**
     * Sets the userLogItem.
     * @param userLogItem the userLogItem to set
     */
    public void setUserLogItem(DatasetUserLogItem userLogItem) {
        this.userLogItem = userLogItem;
    }
}
