package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Superclass for servlets that produce AJAX responses.
 * @author jimbokun
 * @version $Revision: 7664 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-04-23 10:21:51 -0400 (Mon, 23 Apr 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AJAXResponseServlet extends AbstractServlet {

    /**
     * Handles the HTTP get.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     * @see javax.servlet.http.HttpServlet
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
     * @see javax.servlet.http.HttpServlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        try {
            if (!isAuthorizedForDataset(req)) {
                writeStatusMessage(resp, "not authorized");
                return;
            }
            doAJAX(req, resp);
        } catch (Exception e) {
            forwardError(req, resp, logger(), e);
        } finally {
            logDebug("doPost end");
        }
    }

    /**
     * Handles the AJAX request.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws Exception a servlet exception
     * @see javax.servlet.http.HttpServlet
     */
    protected abstract void doAJAX(HttpServletRequest req, HttpServletResponse resp)
    throws Exception;
}