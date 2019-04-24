/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2016
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.workflows.auth;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.workflows.LearnSphereServlet;

import edu.cmu.pslc.datashop.auth.OliUserServices;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * Handles the basic log out functionality for Workflows (via learnsphere.org)
 * Copied from the DataLab servlet by the same name.
 *
 * @version $Revision: 13735 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-09 15:55:57 -0500 (Mon, 09 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereLogoutServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP post.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            HttpSession httpSession = req.getSession(true);

            // log that the user logged in in the dataset_user_log table
            UserItem userItem = getLoggedInUserItem(req);
            if (userItem != null) {
                UserLogger.log(userItem, UserLogger.WORKFLOWS_LOGOUT);
            } else {
                logger.info("userItem is null when logging out");
            }

            if (OliUserServices.isOliEnabled()) {
                boolean logoutSuccessful = OliUserServices.logout(req);
                if (logoutSuccessful) {
                    logger.info("OliUserServices.logout is successful.");

                    setUserAndCleanSession(req, null);

                } else {
                    logger.warn("OliUserServices.logout was NOT successful.");
                }

            } else {
                logger.info("OLI services not available, logging user out.");

                setUserAndCleanSession(req, null);
            }

            resp.sendRedirect(LearnSphereServlet.SERVLET_NAME);

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logger.debug("doPost end");
        }
    } //end doPost()

} // end class LearnSphereLogoutServlet
