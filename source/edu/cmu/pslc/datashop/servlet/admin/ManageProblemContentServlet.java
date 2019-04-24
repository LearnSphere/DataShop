/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.comparators.NullComparator;
import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.PcConversionDao;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemContentHelper;

/**
 * This servlet is for managing Problem Content.
 *
 * @author Cindy Tipper
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageProblemContentServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for the 'Manage Problem Content' jsp. */
    private static final String MANAGE_PC_JSP_NAME = "/jsp_admin/admin_problem_content.jsp";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ManageProblemContent";

    /** Constant for the request/session attribute. */
    public static final String MANAGE_PROBLEM_CONTENT_ATTR = "ManageProblemContent";

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

            if ((getParameter(req, "action") != null) &&
                (getParameter(req, "action").equals("requestDatasets"))) {
                requestDatasets(req, resp);
            } else if (getParameter(req, "formRequest") != null) {
                if (getParameter(req, "conversionTool") != null) {
                    setConversionToolParam(req, getParameter(req, "conversionTool"));
                }
                if (getParameter(req, "contentVersionSearchBy") != null) {
                    setContentVersionSearchParam(req, getParameter(req, "contentVersionSearchBy"));
                }
                if (getParameter(req, "datasetSearchBy") != null) {
                    setDatasetSearchParam(req, getParameter(req, "datasetSearchBy"));
                }
                if (getParameter(req, "problemContent") != null) {
                    setProblemContentParam(req, getParameter(req, "problemContent"));
                }
                if (getParameter(req, "sortBy") != null) {
                    setSortParam(req, getParameter(req, "sortBy"));
                }
                if (getParameter(req, "deletePcConversion") != null) {
                    deletePcConversion(req, userItem);
                }

                redirectToManagePCPage(req, resp);

            } else {
                // JSP was loaded...
                req.setAttribute(MANAGE_PROBLEM_CONTENT_ATTR, getDto(req));
                logger.debug("Ajax request: Forwarding to the problem content sub content JSP");
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(MANAGE_PC_JSP_NAME);
                disp.forward(req, resp);
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
     * Helper method for redirecting to the DatasetInfo page.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToManagePCPage(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        redirectToManagePCPage(null, null, req, resp);
    }

    /**
     * Helper method for redirecting to the Manage Problem Content page with message info.
     * @param message the message to display to the user
     * @param messageLevel the level of the message
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToManagePCPage(String message, String messageLevel,
                                        HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        ManageProblemContentDto dto = getDto(req);
        dto.setMessage(message);
        dto.setMessageLevel(messageLevel);

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(MANAGE_PROBLEM_CONTENT_ATTR, dto);
        resp.sendRedirect(SERVLET_NAME);
    }

    /**
     * Helper method to generate DTO needed to display 'Manage Problem Content'
     * page.
     * @param req {@link HttpServletRequest}
     * @return ManageProblemContentDto the dto
     */
    private ManageProblemContentDto getDto(HttpServletRequest req) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);

        List<PcConversionDto> pcConversionList = getPcConversionList(context);

        ManageProblemContentDto dto = new ManageProblemContentDto();
        dto.setPcConversions(pcConversionList);

        String conversionTool = context.getConversionTool();
        dto.setDisplayConversionToolColumn(conversionTool.equals("") ? true : false);

        return dto;
    }

    /**
     * Helper method to generate list of DTOs of managed Problem Content.
     * @param context the ManageProblemContentContext
     * @return List<PcConversionDto> objects
     */
    private List<PcConversionDto> getPcConversionList(ManageProblemContentContext context) {

        String conversionTool = context.getConversionTool();
        String contentVersionSearchBy = context.getContentVersionSearchBy();
        String datasetSearchBy = context.getDatasetSearchBy();
        String mappedStr = context.getProblemContent();
        Boolean mapped = null;
        if (mappedStr.equals(ManageProblemContentDto.PROBLEM_CONTENT_MAPPED)) {
            mapped = true;
        } else if (mappedStr.equals(ManageProblemContentDto.PROBLEM_CONTENT_UNMAPPED)) {
            mapped = false;
        }

        PcConversionDao dao = DaoFactory.DEFAULT.getPcConversionDao();
        List<PcConversionItem> contentVersions =
            dao.getContentVersionsFiltered(conversionTool,
                                           contentVersionSearchBy, datasetSearchBy, mapped);

        PcProblemDao pcpDao = DaoFactory.DEFAULT.getPcProblemDao();
        PcConversionDatasetMapDao mapDao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();

        List<PcConversionDto> pcConversionList = new ArrayList(contentVersions.size());
        for (PcConversionItem pci : contentVersions) {
            PcConversionDto dto = new PcConversionDto();
            dto.setPcConversion(pci);

            List<PcProblemItem> pcProblems = pcpDao.findProblemsByConversion(pci);
            dto.setNumProblems(new Long(pcProblems.size()));

            List<PcConversionDatasetMapItem> mapItems = mapDao.findByPcConversion(pci);
            dto.setIsDeletable(mapItems.size() > 0 ? false : true);

            pcConversionList.add(dto);
        }

        String sortBy = context.getSortBy();
        Boolean isAscending = context.isAscending(sortBy);

        Comparator<PcConversionDto> comparator =
            PcConversionDto.getComparator(PcConversionDto.getSortByParameters(sortBy, isAscending));
        Comparator<PcConversionDto> nullComparator = new NullComparator(comparator, false);
        Collections.sort(pcConversionList, nullComparator);

        return pcConversionList;
    }

    /**
     * Gets a list of datasets for the given PC conversion.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void requestDatasets(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, JSONException {

        String errorMsg = null;
        boolean errorFlag = false;
        JSONArray pcDatasetList = new JSONArray();

        Long conversionId = getLongId((String)req.getParameter("pcConversionId"));
        PcConversionDao pcDao = DaoFactory.DEFAULT.getPcConversionDao();
        PcConversionItem pci = pcDao.get(conversionId);

        if (pci == null) {
            errorMsg = "Unknown error occurred";
            errorFlag = true;
        } else {
            PcConversionDatasetMapDao mapDao = DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
            List<PcConversionDatasetMapItem> mapItems = mapDao.findByPcConversion(pci);

            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            for (PcConversionDatasetMapItem mi : mapItems) {
                DatasetItem dataset = datasetDao.get((Integer)mi.getDataset().getId());
                JSONObject pcDatasetInfo = new JSONObject();
                pcDatasetInfo.put("datasetName", dataset.getDatasetName());
                pcDatasetInfo.put("datasetId", dataset.getId());
                pcDatasetInfo.put("numProblemsMapped", mi.getNumProblemsMapped());
                pcDatasetInfo.put("status", mi.getStatus());
                pcDatasetList.put(pcDatasetInfo);
            }
        }

        if (!errorFlag) {
            writeJSON(resp, json("flag", "success",
                                 "versionId", conversionId,
                                 "datasets", pcDatasetList));
        } else {
            writeJSON(resp, json("flag", "error", "message", errorMsg));
        }

        return;
    }

    /**
     * Sets the conversion tool parameter in the ManageProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param conversionTool the tool name
     */
    private void setConversionToolParam(HttpServletRequest req, String conversionTool) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);
        context.setConversionTool(conversionTool);
        ManageProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the content version search parameter in the ManageProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private void setContentVersionSearchParam(HttpServletRequest req, String searchBy) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);
        context.setContentVersionSearchBy(searchBy);
        ManageProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the dataset search parameter in the ManageProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private void setDatasetSearchParam(HttpServletRequest req, String searchBy) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);
        context.setDatasetSearchBy(searchBy);
        ManageProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the 'problem content' parameter in the ProblemListContext.
     * @param req {@link HttpServletRequest}
     * @param problemContentParam the types of problems to be displayed
     */
    private void setProblemContentParam(HttpServletRequest req, String problemContentParam) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);
        context.setProblemContent(problemContentParam);
        ManageProblemContentContext.setContext(req, context);
    }

    /**
     * Sets the sort parameter in the ManageProblemContentContext.
     * @param req {@link HttpServletRequest}
     * @param sortBy the string to sort by
     */
    private void setSortParam(HttpServletRequest req, String sortBy) {
        ManageProblemContentContext context = ManageProblemContentContext.getContext(req);
        context.setSortBy(sortBy, true);
        ManageProblemContentContext.setContext(req, context);
    }

    /**
     * Delete a mapping for the specified PC conversion.
     * @param req {@link HttpServletRequest}
     * @param userItem the logged in user
     */
    private void deletePcConversion(HttpServletRequest req, UserItem userItem) {

        Long conversionId = getLongId((String)req.getParameter("pcConversionId"));

        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        pcHelper.deletePcConversion(conversionId, userItem);

    }
}
