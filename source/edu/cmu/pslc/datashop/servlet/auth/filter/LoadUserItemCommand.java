/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.auth.AuthInfo;
import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserState;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Commands that loads userItem and sets in the Request's attribute.

 * @author Young Suk Ahn
 * @version $Revision: 13206 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-26 15:40:06 -0400 (Tue, 26 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LoadUserItemCommand implements Command {

    /** Session Key for the UserItem */
    protected static final String USER_SESSION_KEY = "cmu.edu.pslc.datashop.item.UserItem";
    /** Log4j logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public boolean execute(Context ctx) {

        AccessContext accessCtx = (AccessContext)ctx;

        // Retrieve user:
        // NOTE: AbstracServlet.getLoggedInUserItem is not static.
        UserItem userItem = getLoggedInUserItem(accessCtx.getHttpRequest());
        accessCtx.setUserItem(userItem);

        return false;
    }

    /* Codes below was brought from AbstractSevlet because getting user
     * is a cross cutting concern that many servlet uses but no all.
     * The concrete servlets should be freed from hard-coding user loading,
     * as it may produce inconsistencies in getting the user object and may
     * produce duplicate code.
     * Subsequent use of user can be done through Request's attribute "userItem"
     */

    /**
     * Check if the user is logged in.  If so, then make sure the Data Shop user
     * is created properly.  If on OLI-less system (i.e. development) then the
     * user is always logged in.
     * @param req the {@link HttpServletRequest}
     * @return the account id if logged in, null otherwise
     */
    protected UserItem getLoggedInUserItem(HttpServletRequest req) {
        boolean isLoggedIn = false;
        String accountId = req.getRemoteUser();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem currentUser = AbstractServlet.getUser(req);
        UserItem userItem = null;
        HttpSession httpSession = req.getSession();

        if (accountId == null) {
            UserItem sessionUser = AbstractServlet.getUser(req);
            if (sessionUser != null) {
                accountId = (String)sessionUser.getId();
            }
        }
        
        if (isGoogleUser(accountId)) {
            LogUtils.logDebug(logger, "isLoggedIn: Google user");
            
            if (accountId != null) {
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userDao.saveOrUpdate(userItem);
                }
                isLoggedIn = true;
            }
        } else if (OliUserServices.isOliEnabled()) {
            LogUtils.logDebug(logger, "isLoggedIn: OLI services enabled");
            AuthInfo authInfo = OliUserServices.isLoggedIn(req);
            if (authInfo != null) {
                accountId = authInfo.getUserId();
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userItem.setFirstName(authInfo.getFirstName());
                    userItem.setLastName(authInfo.getLastName());
                    userItem.setEmail(authInfo.getEmailAddress());
                    userItem.setInstitution(authInfo.getInstitution());
                    userDao.saveOrUpdate(userItem);
                    LogUtils.logInfo(logger, "User " + accountId + " saved in analysis database.");
                }
                isLoggedIn = true;
            }
        } else {
            LogUtils.logDebug(logger, "isLoggedIn: OLI services NOT enabled");

            if (accountId != null) {
                userItem = userDao.get(accountId);
                if (userItem == null) {
                    userItem = new UserItem();
                    userItem.setId(accountId);
                    userDao.saveOrUpdate(userItem);
                }
                isLoggedIn = true;
            }
        }

        if (!isLoggedIn) {
            httpSession.removeAttribute(USER_SESSION_KEY);

        } else {
            if ((currentUser == null) || (!userItem.equals(currentUser))) {
                httpSession.setAttribute(USER_SESSION_KEY, userItem);
            }
            setRecentDatasets(req.getSession(true), (String)userItem.getId());
        }

        LogUtils.logDebug(logger, "User ", accountId, " logged in: ", isLoggedIn);
        return userItem;
    }

    /** Constant. */
    private static final String GMAIL_DOMAIN = "gmail.com";

    /**
     * Helper function to determine if current user is a Google user.
     * @param accountId the logged in user
     * @return flag indicating if user is a Google user
     */
    private Boolean isGoogleUser(String accountId) {
        if (accountId == null) { return false; }

        if (accountId.indexOf(GMAIL_DOMAIN) > 0) {
            return true;
        }

        return false;
    }

    /**
     * Set the user's recent dataset list in the HTTP session.
     * @param httpSession the HTTP session
     * @param accountId the account id
     */
    protected void setRecentDatasets(HttpSession httpSession, String accountId) {
        List datasetList = UserState.getRecentDatasetsViewed(accountId);
        LogUtils.logDebug(logger, "Number of recent datasets is: ", datasetList.size());
        httpSession.setAttribute("recent_datasets", datasetList);
    }
}
