/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.auth.AccessFilter;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 * Check for other authentication (logged in) and authorization depending on the authLevel flag.
 * @author Young Suk Ahn
 * @version $Revision: 9803 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-08-19 14:06:32 -0400 (Mon, 19 Aug 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CheckAuthCommand implements Command {

    /** Authorization Level: no authorization required. **/
    public static final int AUTH_LEVEL_NONE = 0;

    /** Checks whether the user is logged in. Redirect to login page if not.
     * Requires: LoadUserItemCommand */
    public static final int AUTH_LEVEL_LOGGED_IN = 1;

    /** Checks whether the user has authorization for the Dataset.
     * Redirect to Dataset Overview page if not.
     * Requires: LoadUserItemCommand & LoadDatsetCommand */
    public static final int AUTH_LEVEL_AUTHORIZED_FOR_DS = 2;

    /** Check if the user is admin.
     * Requires: LoadUserItemCommand
     */
    public static final int AUTH_LEVEL_ADMIN = 3;

    /** The required authorization level **/
    private int authLevel; // the level of authentication required for the page
    /** Log4j logger. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The default constructor with login required. **/
    public CheckAuthCommand() {
        this.authLevel = AUTH_LEVEL_LOGGED_IN;
    }
    /**
     * Check authorization command.
     * @param authLevel the authorization level
     */
    public CheckAuthCommand(int authLevel) {
        this.authLevel = authLevel;
    }

    /**
     * Authorization level getter.
     * @return the authorization level
     */
    public int getAuthLevel() {
        return authLevel;
    }
    /**
     * Set the authorization level.
     * @param authLevel the authorization level
     */
    public void setAuthLevel(int authLevel) {
        this.authLevel = authLevel;
    }

    /**
     * Checks for authentication (login) and/or authorization based on the authLevel.
     * @param ctx the Context
     * @return true means 'stop' (see http://commons.apache.org/chain/api-release/index.html)
     */
    @Override
    public boolean execute(Context ctx) {
        AccessContext accessCtx = (AccessContext)ctx;
        HttpServletRequest req = accessCtx.getHttpRequest();
        HttpServletResponse resp = accessCtx.getHttpResponse();
        String contextPath = req.getContextPath();
        LogUtils.logDebug(logger, "getContextPath: ", contextPath);

        if (this.authLevel >= AUTH_LEVEL_LOGGED_IN) {
            if (!accessCtx.containsAttribute(AccessContext.KEY_USER_ITEM)) {
                throw new IllegalStateException(
                        "User Item was not found, perhaps LoadUserItemCommand was not executed");
            }
        }
        // From this point accessCtx.getUserItem is a valid entry.

        if (this.authLevel >= AUTH_LEVEL_LOGGED_IN) {
            if (accessCtx.getUserItem() == null) {
                try {
                    if (accessCtx.getDatasetItem() == null) {
                        // If datasetItem not exists, redirect to login page.
                        String redirUrl = contextPath + AccessFilter.REDIRECT_LOGIN;
                        LogUtils.logDebug(logger,
                                "Not logged in, redirecting to ", redirUrl);
                        AccessFilter.redirect(req, resp, redirUrl);
                    } else {
                        // If datasetItem exists, then redirect to Dataset Overview page.
                        String redirUrl = contextPath + AccessFilter.REDIRECT_DATASET_INFO
                                + "?datasetId=" + accessCtx.getDatasetId()
                                + "&access_redir=1";
                        LogUtils.logDebug(logger,
                                "(1)Not authorized for this dataset, redirecting to ", redirUrl);
                        AccessFilter.redirect(req, resp, redirUrl);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error while redirecting.", e);
                }
                return true;
            }
        }

        // from this point accessCtx.getUserItem is a valid object.

        if (this.authLevel == AUTH_LEVEL_AUTHORIZED_FOR_DS) {
            if (!accessCtx.containsAttribute(AccessContext.KEY_IS_AUTHORIZED_FOR_DS)) {
                throw new IllegalStateException(
                        "IsAuthorizedForDataset was not set. "
                        + "Perhaps LoadDatsetCommand was not executed.");
            }

            if (!accessCtx.isAuthorizedForDataset()) {
                try {
                    String redirUrl = contextPath + AccessFilter.REDIRECT_DATASET_INFO
                            + "?datasetId=" + accessCtx.getDatasetId()
                            + "&access_redir=1";
                    LogUtils.logDebug(logger,
                            "(2)Not authorized for this dataset, redirecting to ", redirUrl);
                    AccessFilter.redirect(req, resp, redirUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Error while redirecting.", e);
                }
                return true;
            }
        }

        if (this.authLevel == AUTH_LEVEL_ADMIN) {
            if (!accessCtx.getUserItem().getAdminFlag()) {
                try {
                    String redirUrl = AccessFilter.REDIRECT_INDEX_JSP;
                    LogUtils.logDebug(logger,
                            "Not admin, redirecting to ", redirUrl);
                    AccessFilter.redirect(req, resp, redirUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Error while redirecting.", e);
                }
                return true;
            }
        }
        return false;
    }
}
