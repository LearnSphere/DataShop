/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.auth.filter;

import java.io.UnsupportedEncodingException;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import edu.cmu.pslc.datashop.servlet.auth.AccessFilter;
import edu.cmu.pslc.datashop.servlet.auth.AccessFilter.AccessContext;


/**
 * The command for all requests.
 * This is a basic command that sets the encoding to UTF-8 and saves
 * the last visit page (url) if saveVisitUrl flag is set to true.
 *
 * @author Young Suk Ahn
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class BasicFlowCommand implements Command {

    /** Flag indicating whether to save the visited url or not. **/
    private boolean saveVisitUrl = false;

    /**
     * Nothing to do in the default constructor.
     */
    public BasicFlowCommand() {
    }

    /**
     * Constructor with the initial value for the saveVisitUrl.
     * @param saveVisitUrl initial value for the saveVisitUrl
     */
    public BasicFlowCommand(boolean saveVisitUrl) {
        this.saveVisitUrl = saveVisitUrl;
    }

    /**
     * Returns whether to save the URL.
     * @return whether to save the URL
     */
    public boolean isSaveVisitUrl() {
        return saveVisitUrl;
    }

    /**
     * Set whether to save the URL.
     * @param saveVisitUrl the URL to save
     */
    public void setSaveVisitUrl(boolean saveVisitUrl) {
        this.saveVisitUrl = saveVisitUrl;
    }

    @Override
    public boolean execute(Context ctx) {

        AccessContext accessCtx = (AccessContext)ctx;

        try {
            accessCtx.getHttpRequest().setCharacterEncoding("UTF-8");
            accessCtx.getHttpResponse().setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException (e);
        }

        if (this.saveVisitUrl && !accessCtx.isAjax()) {
            String accessRedir = accessCtx.getHttpRequest().getParameter("access_redir");
            /* Save the last visit page into the session only if it is not coming
             * from a redirection due to access control.
             * That is, if you were redirected, you want the last page to be
             * the page before the redirection.
             */
            if (accessRedir == null) {
                accessCtx.getHttpRequest().getSession().setAttribute(
                        AccessFilter.LAST_VISIT_URL_KEY, AccessFilter.lastVisitUrl(
                                accessCtx.getHttpRequest()));
            }
        }

        return false;
    }

}
