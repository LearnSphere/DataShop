/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;

/**
 * This servlet is for displaying Problem Content resources.
 *
 * @author Cindy Tipper
 * @version $Revision: 11048 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-19 11:15:12 -0400 (Mon, 19 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DisplayResourcesServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Handles the HTTP get.
     * @see javax.servlet.http.HttpServlet
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
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        HttpSession httpSession = req.getSession(true);

        try {

            UserItem userItem = getLoggedInUserItem(req);
            if (userItem == null) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            String fileName = (String)req.getParameter("fileName");
            if (fileName == null) {
                logger.error("fileName not specified.");
                return;
            } else {
                StringBuffer sb = new StringBuffer(getBaseDir());
                if (!getBaseDir().endsWith("/")) { sb.append("/"); }
                sb.append(fileName);
                fileName = sb.toString();
            }

            File theFile = new File(fileName);

            if (!theFile.exists()) {
                logger.error("File does not exist: " + fileName);
                return;
            }

            String mimeType = null;
            if ((fileName.endsWith("jpeg")) || (fileName.endsWith("jpg"))) {
                mimeType = "image/jpeg";
            } else if (fileName.endsWith("png")) {
                mimeType = "image/png";
            }

            if (mimeType != null) {
                resp.setHeader("Content-Type", mimeType);
            }
            resp.setHeader("Content-Length", String.valueOf(theFile.length()));
            resp.setHeader("Content-Disposition",
                           "attachment; filename=\"" + fileName + "\"");

            BufferedInputStream inStream = null;
            BufferedOutputStream outStream = null;
            try {
                inStream = new BufferedInputStream(new FileInputStream(theFile));
                outStream = new BufferedOutputStream(resp.getOutputStream());

                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
                outStream.flush();
            } catch (Exception e) {
                logger.error("Failed to write contents of file: " + fileName + e);
            } finally {
                if (inStream != null) { inStream.close(); }
                if (outStream != null) { outStream.close(); }
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logger.debug("doPost end");
        }
    }
}
