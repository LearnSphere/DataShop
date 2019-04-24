/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2007
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.helper;

import java.util.List;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Helper class for quickly looking up state information about a
 * user from wherever the information is stored (typically the system log).
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15454 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-08-31 13:00:06 -0400 (Fri, 31 Aug 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class UserState {

    /**
     * Default Constructor - because this class is only applied by static internal classes
     * the any attempts to instantiate will return an UnsupportedOperationException.
     */
    protected UserState() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

    /**
     * Get a list of the datasets recently viewed for the given user.
     * @param userId the id of the logged in user
     * @return a list of DatasetItems
     */
    public static List <DatasetItem> getRecentDatasetsViewed(String userId) {
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        return userLogDao.getRecentDatasetsViewed(new UserItem(userId));
    }

    /**
     * Checks if the given user has requested the web services role by looking
     * in the dataset_user_log table.
     * @param userItem the given user
     * @return true if the user has requested the role, false otherwise
     */
    public static boolean hasRequestedWebServicesRole(UserItem userItem) {
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        return userLogDao.hasPerformedAction(userItem, UserLogger.WEB_SERV_REQUEST_ROLE_ACTION);
    }

    /**
     * Checks if the given user has requested the web services role by looking
     * in the dataset_user_log table.
     * @param userItem the given user
     * @return true if the user has requested the role, false otherwise
     */
    public static boolean hasRequestedLoggingActivityRole(UserItem userItem) {
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        return userLogDao.hasPerformedAction(userItem, UserLogger.LOG_ACT_REQUEST_ROLE_ACTION);
    }

    /**
     * Checks if the given user has requested the external tools role by looking
     * in the dataset_user_log table.
     * @param userItem the given user
     * @return true if the user has requested the role, false otherwise
     */
    public static boolean hasRequestedExternalToolsRole(UserItem userItem) {
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        return userLogDao.hasPerformedAction(userItem, UserLogger.REQ_EXTERNAL_TOOL_ROLE);
    }

    /**
     * Checks if the given user has requested the DataShop-Edit role by looking
     * in the dataset_user_log table.
     * @param userItem the given user
     * @return true if the user has requested the role, false otherwise
     */
    public static boolean hasRequestedDatashopEditRole(UserItem userItem) {
        DatasetUserLogDao userLogDao = DaoFactory.DEFAULT.getDatasetUserLogDao();
        return userLogDao.hasPerformedAction(userItem, UserLogger.REQUEST_DS_EDIT_ROLE);
    }
}
