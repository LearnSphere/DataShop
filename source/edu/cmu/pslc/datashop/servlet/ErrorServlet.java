/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Servlet to handle exceptions from JSPs, Servlets, 404 and 500s.
 * See error page configuration in web.xml.
 * TechTip: http://java.sun.com/developer/EJTechTips/2003/tt0114.html
 *
 * @author Alida Skogsholm
 * @version $Revision: 9456 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-06-20 13:34:51 -0400 (Thu, 20 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ErrorServlet extends AbstractServlet {

    /** logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** JSP file name. */
    public static final String ERROR_PAGE_JSP_NAME = "/error_page.jsp";

    /** JSP file name. */
    private static final String PAGE_NOT_FOUND_JSP_NAME = "/page_not_found.jsp";
    /** Constant. */
    private static final Integer PAGE_NOT_FOUND_ERROR_CODE = new Integer(404);
    /** Constant. */
    private static final String  ERROR_CODE_VAR = "javax.servlet.error.status_code";

    /** As defined by the Servlet 2.3 specification. */
    private static final String[] VARS = {
        "javax.servlet.error.status_code",
        "javax.servlet.error.exception_type",
        "javax.servlet.error.message",
        "javax.servlet.error.exception",
        "javax.servlet.error.request_uri"
    };

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
        //no difference, so just forward the request and response to the post.
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
        logger.debug("doPost begin");
        PrintWriter out = null;
        String userId = "unknown";
        UserItem userItem = getUser(req);
        if (userItem != null) {
            userId = (String)userItem.getId();
        }
        try {
            setEncoding(req, resp);

            // Go to the oops page by default.
            String jspName = ERROR_PAGE_JSP_NAME;

            // Create message to log variables.
            String message = "";
            for (int i = 0; i < VARS.length; i++) {
                message += VARS[i] + " : " + req.getAttribute(VARS[i]) + ", ";
            }

            // Check if its a page not found error and go to the page not found page if it is.
            Integer errorCode = (Integer)req.getAttribute(ERROR_CODE_VAR);
            if (errorCode != null && errorCode.equals(PAGE_NOT_FOUND_ERROR_CODE)) {
                jspName = PAGE_NOT_FOUND_JSP_NAME;
            }
            logger.warn("User: " + userId
                    + ", ErrorCode:" + errorCode + ", redirect to " + jspName
                    + ":: " + message);

            // Go to the JSP specified.
            RequestDispatcher disp;
            disp = getServletContext().getRequestDispatcher(jspName);
            disp.forward(req, resp);
            return;

        } catch (Exception exception) {
            //This is the only servlet which handles exceptions
            //this way.  All others should call forwardError.
            logger.error("ErrorServlet - Exception for user " + userId);

            resp.setContentType("text/html");
            out = resp.getWriter();
            out.write(handleException(logger, exception));
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    } // end doPost

    /**
     * Pretty pink exception page that isn't that pretty.
     * We need to keep this here in case we get an exception
     * in the servlet that is catching the exceptions.
     * @param logger the logger that logs to a file
     * @param exception the exception that occurred
     * @return html to show in the browser
     */
    private String handleException(Logger logger, Exception exception) {
        String message = getClass().getName() + " : "
            + "doPost : "
            + exception.getMessage();
        logger.warn(message, exception);

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));

        String html = "<P>" + getClass().getName();
        html += "<FONT size+=1 COLOR='#FF3399'><STRONG>";
        html += "<P>Exception";
        html += "</FONT></STRONG>";
        html += "<P><CODE>" + sw.toString() + "</CODE>";

        return html;
    }
}
