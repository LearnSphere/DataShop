/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.servlet.webservices;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.webservices.WebServicesServlet;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowUserLogDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.logging.util.DateTools;
import static java.util.Arrays.asList;
import static edu.cmu.pslc.datashop.util.CollectionUtils.iter;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.NO_MATCHING_URL_ERR;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Entry point for all web services requests.
 *
 * @author Hui Cheng
 * @version $Revision: 15894 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $ $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnSphereWebServicesServlet extends WebServicesServlet {
    /** handles requests for multiple workflows */
    private static final LearnSphereWebServiceHandler WORKFLOWS_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/?", WorkflowsService.class);
    /** handles requests for a single workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/?", WorkflowService.class, "workflow_id");
    /** handles requests for files of a workflow, deprecated */
    private static final LearnSphereWebServiceHandler WORKFLOW_FILES_HANDLER =
        new LearnSphereWebServiceHandler("/workflow_files/(\\d+)/?", WorkflowFilesService.class, "workflow_id");
    /** handles requests for files of a workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_FILES_HANDLER_V2 =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/files?", WorkflowFilesService.class, "workflow_id");
    /** handles requests for run of a workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_RUN_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/run?", WorkflowRunService.class, "workflow_id");
    /** handles requests for delete of a workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_DELETE_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/delete?", WorkflowDeleteService.class, "workflow_id");
    /** handles requests for saveAsNew of a workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_SAVE_AS_NEW_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/save_as_new?", WorkflowSaveAsNewService.class, "workflow_id");
    /** handles requests for modify of a workflow */
    private static final LearnSphereWebServiceHandler WORKFLOW_MODIFY_HANDLER =
        new LearnSphereWebServiceHandler("/workflows/(\\d+)/modify?", WorkflowModifyService.class, "workflow_id");

    /** handlers for routing incoming requests to the correct service */
    private static List<LearnSphereWebServiceHandler> ls_handlers =
        asList(WORKFLOWS_HANDLER, WORKFLOW_HANDLER, WORKFLOW_FILES_HANDLER, WORKFLOW_FILES_HANDLER_V2,
                        WORKFLOW_RUN_HANDLER, WORKFLOW_DELETE_HANDLER, WORKFLOW_SAVE_AS_NEW_HANDLER, 
                        WORKFLOW_MODIFY_HANDLER);
    
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * Find a handler matching the request, then delegate to the corresponding web service.
     * @param req the web service request
     * @param resp the web service response
     * @param method the HTTP method
     */
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp,
                               HttpMethod method) {
        try {
            // dump the headers for debugging
            @SuppressWarnings("unchecked")
            Enumeration<String> hdrNames = req.getHeaderNames();
            for (String hdr : iter(hdrNames)) {
                logDebug("HEADER ", hdr, " : ", req.getHeader(hdr));
            }

            if (!authenticate(req)) {
                logDebug("authentication failed!");
                writeError(resp, SC_UNAUTHORIZED, "Authorization failed.  Check your credentials.");
                return;
            }
            logDebug("authentication succeeded!");
            boolean handled = false;
            
            UserItem currentUser = (UserItem)req.getAttribute("authenticatedUser");
            String action = UserLogger.WEB_SERV_ACTION;
            String info = "Path: " + req.getPathInfo() + " :: "
                + "Params: " + getDebugParamsString(req) + " :: "
                + "Header Date: " + req.getHeader("date") + " :: ";
            
            LearnSphereWebServiceUserLog wsUserLog = new LearnSphereWebServiceUserLog();
            wsUserLog.setUser((String)currentUser.getId());
            wsUserLog.setAction(action);
            wsUserLog.setInfo(info);
            
            setEnvironmentByHTTPRequest(req);
            for (LearnSphereWebServiceHandler handler : ls_handlers) {
                long startTime = System.currentTimeMillis();
                
                //set baseDir in req for later use
                req.setAttribute("baseDir", this.getBaseDir());
                wsUserLog = handler.handle(wsUserLog, req.getPathInfo(), method, req, resp);
                handled = wsUserLog.getHandled();
                if (handled) {
                        //handled: includes successfull handled and unsuccessfull
                        //either case will be recorded in log table
                        //in case of failure, error message is set in individual service
                        wsUserLog.setTime(DateTools.getDate("" + startTime));
                        //save this action to workflow_user_log
                        WorkflowUserLogDao wfUserLogDao = DaoFactory.DEFAULT.getWorkflowUserLogDao();
                        wfUserLogDao.saveOrUpdate(wsUserLog.getUserLogItem());
                    break;
                }
            }
            if (!handled) {
                writeError(resp, NO_MATCHING_URL_ERR, SC_NOT_FOUND, NO_MATCHING_URL_MSG);
            }
        } catch (Exception e) {
            logger.error("Failed to handle request: ", e);
            writeError(resp, SC_INTERNAL_SERVER_ERROR,
                       "Unknown error while handling request: " + e.getMessage());
        }
    }

}
