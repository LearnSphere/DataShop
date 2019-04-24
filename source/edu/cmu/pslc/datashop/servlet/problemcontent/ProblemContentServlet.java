/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapId;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportServlet;

/**
 * This servlet is for displaying a Problem Content.
 *
 * @author Cindy Tipper
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemContentServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for the 'Problem Content' info jsp. */
    private static final String PC_INFO_JSP_NAME = "/problem_content_info.jsp";
    /** The JSP name for the 'Problem Content' mapping tool jsp. */
    private static final String PC_JSP_NAME = "/problem_content.jsp";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProblemContent";

    /** Constant. */
    public static final String PC_PROBLEM_ID = "pc_problem_id";
    /** Constant. */
    public static final String PC_DATASET_ID = "pc_dataset_id";
    /** Constant. */
    public static final String PC_BASE_DIR = "pc_base_dir";

    /** Constant for the request/session attribute. */
    public static final String PROBLEM_CONTENT_ATTR = "problemContent_";

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

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

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            UserItem userItem = getLoggedInUserItem(req);
            if (userItem == null) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            Long pcId = getLongId((String)req.getParameter("pcId"));
            if (pcId != null) {
                httpSession.setAttribute(PC_PROBLEM_ID, pcId);
                httpSession.setAttribute(PC_BASE_DIR, getBaseDir());
                RequestDispatcher disp = getServletContext().getRequestDispatcher(PC_INFO_JSP_NAME);
                disp.forward(req, resp);
            } else if (req.getParameter("viewProblem") != null) {
                viewProblem(req, resp, userItem);
            } else if (req.getParameter("datasetId") != null) {
                Integer datasetId = getIntegerId((String)req.getParameter("datasetId"));

                if (getParameter(req, "conversionTool") != null) {
                    setConversionToolParam(req, getParameter(req, "conversionTool"));
                }
                if (getParameter(req, "searchBy") != null) {
                    setSearchParam(req, getParameter(req, "searchBy"));
                }
                if (getParameter(req, "sortBy") != null) {
                    setSortParam(req, getParameter(req, "sortBy"));
                }
                if (getParameter(req, "addContentVersion") != null) {
                    addContentVersion(req, datasetId, userItem);
                }
                if (getParameter(req, "deleteContentVersion") != null) {
                    deleteContentVersion(req, datasetId, userItem);
                }

                redirectToDatasetInfoPage(datasetId, req, resp);
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

    /**
     * Helper method for determining Problem Content information.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the logged in user
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void viewProblem(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws IOException, JSONException {

        String message = "";
        String status = STATUS_MESSAGE_LEVEL_SUCCESS;

        Long problemId = getLongId((String)req.getParameter("problemId"));

        ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
        PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();

        ProblemItem problem = problemDao.get(problemId);
        String problemName = problem.getProblemName();

        PcProblemItem pcProblem = problem.getPcProblem();
        if (pcProblem != null) {
            pcProblem = pcProblemDao.get((Long)pcProblem.getId());

            String logInfoStr = "Viewed problem content for problem: " + problemName
                + " (" + problemId + ")";
            UserLogger.log(userItem, UserLogger.VIEW_PROBLEM_CONTENT_INFO, logInfoStr, false);

            writeJSON(resp, json("message", message, "status", status,
                                 "pcId", pcProblem.getId(),
                                 "problemName", pcProblem.getProblemName()));
        } else {
            message = "Unable to locate Problem Content for problem '"
                + problem.getProblemName() + "'.";
            status = STATUS_MESSAGE_LEVEL_ERROR;
            writeJSON(resp, json("message", message, "status", status));
        }
    }

    /**
     * Helper method for redirecting to the DatasetInfo page.
     * @param datasetId the dataset id
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        redirectToDatasetInfoPage(datasetId, null, null, req, resp);
    }

    /**
     * Helper method for redirecting to the DatasetInfo page with message info.
     * @param datasetId the dataset id
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToDatasetInfoPage(Integer datasetId, String message, String messageLevel,
                                           HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        ProblemContentDto problemContentDto =
            pcHelper.getProblemContentDto(datasetId, ProblemContentContext.getContext(req));
        problemContentDto.setMessage(message);
        problemContentDto.setMessageLevel(messageLevel);

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(PROBLEM_CONTENT_ATTR + datasetId, problemContentDto);

        resp.sendRedirect(DatasetInfoReportServlet.SERVLET + "?datasetId=" + datasetId);
    }

    /**
     * Sets the conversion tool parameter in the ProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param conversionTool the tool name
     */
    private void setConversionToolParam(HttpServletRequest req, String conversionTool) {
        ProblemContentContext context = ProblemContentContext.getContext(req);
        context.setConversionTool(conversionTool);
        ProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the search parameter in the ProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private void setSearchParam(HttpServletRequest req, String searchBy) {
        ProblemContentContext context = ProblemContentContext.getContext(req);
        context.setSearchBy(searchBy);
        ProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the sort parameter in the ProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param sortBy the string to sort by
     */
    private void setSortParam(HttpServletRequest req, String sortBy) {
        ProblemContentContext context = ProblemContentContext.getContext(req);
        context.setSortBy(sortBy, true);
        ProblemContentContext.setContext(req, context);
    }

    /**
     * Add a mapping for the specified content version.
     * @param req {@link HttpServletRequest}
     * @param datasetId the dataset id
     * @param userItem the logged in user
     */
    private void addContentVersion(HttpServletRequest req, Integer datasetId, UserItem userItem) {
        Long versionId = getLongId((String)req.getParameter("contentVersionId"));

        PcConversionDao dao = DaoFactory.DEFAULT.getPcConversionDao();
        PcConversionItem pci = dao.get(versionId);
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);

        PcConversionDatasetMapDao mapDao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
        PcConversionDatasetMapItem mapItem = new PcConversionDatasetMapItem();
        mapItem.setId(new PcConversionDatasetMapId(pci, dataset));
        mapItem.setNumProblemsMapped(0L);
        mapItem.setMappedBy(userItem);
        mapItem.setMappedTime(new Date());
        mapDao.saveOrUpdate(mapItem);

        ProblemContentMappingBean pcmBean = HelperFactory.DEFAULT.getProblemContentMappingBean();
        pcmBean.setAttributes(dataset, pci, mapItem);
        new Thread(pcmBean).start();
        logger.info("ProblemContentMappingBean started");

        String logInfoStr = "Mapped dataset to problem content version: " + pci.getContentVersion()
            + " (" + versionId + ")";
        UserLogger.log(dataset, userItem, UserLogger.MAP_PROBLEM_CONTENT, logInfoStr, false);
    }

    /**
     * Delete a mapping for the specified content version.
     * @param req {@link HttpServletRequest}
     * @param datasetId the dataset id
     * @param userItem the logged in user
     */
    private void deleteContentVersion(HttpServletRequest req, Integer datasetId,
                                      UserItem userItem) {
        Long versionId = getLongId((String)req.getParameter("contentVersionId"));

        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        pcHelper.deleteContentVersionMapping(versionId, datasetId, userItem);
    }
}
