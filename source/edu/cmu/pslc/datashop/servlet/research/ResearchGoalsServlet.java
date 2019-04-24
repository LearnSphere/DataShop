/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.research;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ResearchGoalDao;
import edu.cmu.pslc.datashop.dao.ResearchGoalDatasetPaperMapDao;
import edu.cmu.pslc.datashop.dao.ResearcherTypeDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet handles all the requests from the External Tools pages.
 *
 * @author alida
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP file name. */
    private static final String JSP_NAME = "/research_goals.jsp";

    /** Title for this page - "External Tools". */
    public static final String SERVLET_TITLE = "Research Goals";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ResearchGoals";

    /** Constant string for the Type ID parameter. */
    private static final String PARAM_TYPE_ID = "typeId";

    /** Constant string for the Goal ID parameter. */
    private static final String PARAM_GOAL_ID = "goalId";

    /** Ajax name for requesting method. */
    private static final String AJAX_REQUEST_ACTION = "researchGoalsAction";

    /**
     * Handles the HTTP get.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));
        PrintWriter out = null;
        try {
            setEncoding(req, resp);
            UserItem userItem = getLoggedInUserItem(req);

            /*
             * This call to hasAgreedToTerms is necessary because users
             * can access this servlet without being logged into DataShop.
             * For those users, the method returns true.
             */
            if (!hasAgreedToTerms(req, false)) {
                forwardTermsAgree(req, resp);
                return;
            }

            if (userItem == null) {
                userItem = DaoFactory.DEFAULT.getUserDao().findOrCreateDefaultUser();
            }

            String actionParam = getParameter(req, AJAX_REQUEST_ACTION);
            if (actionParam == null) {
                showResearchGoals(req, resp, userItem);
            } else if (actionParam.equals("requestDatasetsAndPapers")) {
                getDatasetsAndPapers(req, resp, userItem);
                return;
            } else if (actionParam.equals("requestTypesAndGoals")) {
                getTypesAndGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("requestGoals")) {
                getGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("logHomePageTypeClicked")) {
                logHomePageTypeClicked(req, resp, userItem);
                return;
            } else if (actionParam.equals("logGoalPageGoalClicked")) {
                logGoalPageGoalClicked(req, resp, userItem);
                return;
            }

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            if (out != null) {
                out.close();
            }
            logDebug("doPost end");
        }
    }

    /**
     * Go to the given JSP.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void goToJsp(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String jspName = JSP_NAME;
        logger.info("Going to JSP: " + jspName);
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher(jspName);
        disp.forward(req, resp);
    }

    /**
     * Handle user request to view the complete list of external tools.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void showResearchGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {

        // Set the most recent servlet name for the help page.
        setRecentReport(req.getSession(true), SERVLET_NAME);

        List<ResearcherTypeDto> rtList = getResearcherTypes();
        req.setAttribute(ResearcherTypeDto.ATTRIB_LIST, rtList);

        ResearcherTypeItem typeItem = getTypeParameter(req);
        Integer typeId = null;
        if (typeItem != null) {
            typeId = (Integer)typeItem.getId();
            req.setAttribute(ResearcherTypeDto.ATTRIB_ID, typeId);
            req.setAttribute(ResearcherTypeDto.ATTRIB_LABEL, typeItem.getLabel());
        } else {
            req.setAttribute(ResearcherTypeDto.ATTRIB_LABEL, "&nbsp;");
        }

        List<ResearchGoalDto> goalList = getGoals(typeItem);
        req.setAttribute(ResearchGoalDto.ATTRIB_LIST, goalList);

        String homePageParam = getParameter(req, "homePage");
        if (homePageParam != null) {
            ResearchGoalItem goalItem = getGoalParameter(req);
            logHomePageGoalClicked(userItem, typeItem, goalItem);
        } else {
            logGoalPageTypeClicked(userItem, typeItem);
        }

        goToJsp(req, resp);
    }

    /**
     * Get the full list of researcher types.
     * @return list of researcher types
     */
    private List<ResearcherTypeDto> getResearcherTypes() {
        ResearcherTypeDao rtDao = DaoFactory.DEFAULT.getResearcherTypeDao();
        List<ResearcherTypeItem> itemList = rtDao.findAllInOrder(null);
        List<ResearcherTypeDto> dtoList = new ArrayList<ResearcherTypeDto>();
        for (ResearcherTypeItem item : itemList) {
            Integer itemId = (Integer)item.getId();
            ResearcherTypeDto dto = new ResearcherTypeDto(itemId, item.getLabel(), null, 1);

            List<ResearcherTypeItem> subList = rtDao.findAllInOrder(itemId);
            List<ResearcherTypeDto> subDtoList = new ArrayList<ResearcherTypeDto>();
            if (subList.size() > 0) {
                for (ResearcherTypeItem subItem : subList) {
                    ResearcherTypeDto subDto = new ResearcherTypeDto((Integer)subItem.getId(),
                                                                     subItem.getLabel(),
                                                                     subItem.getParentTypeId(),
                                                                     subItem.getTypeOrder());
                    subDtoList.add(subDto);
                }
            }
            dto.setSubTypeList(subDtoList);

            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Get the research goals for the given researcher type.
     * @param typeItem the researcher type item, can be null
     * @return either a subset of goals or all if given type is null
     */
    private List<ResearchGoalDto> getGoals(ResearcherTypeItem typeItem) {
        List<ResearchGoalDto> dtoList = new ArrayList<ResearchGoalDto>();
        List<ResearchGoalItem> itemList;
        if (typeItem == null) {
            itemList = DaoFactory.DEFAULT.getResearchGoalDao()
                    .findAllInOrder();
        } else {
            itemList = DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao()
                    .findByType(typeItem);
        }

        ResearchGoalDatasetPaperMapDao mapDao =
            DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();

        for (ResearchGoalItem item : itemList) {
            ResearchGoalDto dto = new ResearchGoalDto((Integer)item.getId(),
                    item.getTitle(), item.getDescription(), item.getGoalOrder());
            Integer numberOfPapers = mapDao.getPapersGivenGoal(item).size();
            dto.setNumberOfPapers(numberOfPapers);
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Get the Researcher Type item to get a subset of goal, or null to get all goals.
     * @param req {@link HttpServletRequest}
     * @return the type item if valid id in parameter, null otherwise
     */
    private ResearcherTypeItem getTypeParameter(HttpServletRequest req) {
        ResearcherTypeItem item = null;
        String typeIdParam = getParameter(req, PARAM_TYPE_ID);
        if (typeIdParam != null) {
            if (!typeIdParam.equals("all")) {
                try {
                    Integer typeId = Integer.parseInt(typeIdParam);
                    ResearcherTypeDao dao = DaoFactory.DEFAULT.getResearcherTypeDao();
                    item = dao.get(typeId);
                } catch (NumberFormatException exception) {
                    item = null;
                }
            }
        }
        return item;
    }

    /**
     * Get the Research Goal item from the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the goal item if valid id in parameter, null otherwise
     */
    private ResearchGoalItem getGoalParameter(HttpServletRequest req) {
        ResearchGoalItem item = null;
        String idParam = getParameter(req, PARAM_GOAL_ID);
        if (idParam != null) {
            if (!idParam.equals("all")) {
                try {
                    Integer itemId = Integer.parseInt(idParam);
                    ResearchGoalDao dao = DaoFactory.DEFAULT.getResearchGoalDao();
                    item = dao.get(itemId);
                } catch (NumberFormatException exception) {
                    item = null;
                }
            }
        }
        return item;
    }

    /**
     * Gets a list of datasets and papers for the given research goal.
     * Duplicated in ResearchGoalsEditServlet.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    void getDatasetsAndPapers(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;
        List<RgDatasetDto> datasetList = null;

        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            errorMsg = "Unknown error occurred";
            errorFlag = true;
        } else {
            ResearchGoalDatasetPaperMapDao mapDao =
                    DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();
            datasetList = mapDao.getDatasets(goalItem);
            for (RgDatasetDto datasetDto : datasetList) {
                List<RgPaperDto> paperList = mapDao.getPapers(goalItem, datasetDto.getDatasetId());
                datasetDto.setPaperList(paperList);
                DatasetItem datasetItem = datasetDao.get(datasetDto.getDatasetId());
                Boolean isPublicFlag = authDao.isPublic((Integer)datasetItem.getProject().getId());
                datasetDto.setIsPublicFlag(isPublicFlag);
            }
        }

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "goalId", goalItem.getId(),
                    "datasets", datasetList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /**
     * Gets a list of researcher types and goals given a researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getTypesAndGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        List<ResearcherTypeDto> rtList = getResearcherTypes();
        ResearcherTypeItem typeItem = getTypeParameter(req);
        Integer typeId = (typeItem == null) ? null : (Integer)typeItem.getId();
        List<ResearchGoalDto> goalList = getGoals(typeItem);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "typeId", typeId,
                    "types", rtList,
                    "goals", goalList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /**
     * Gets a list of research goals given a researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        ResearcherTypeItem typeItem = getTypeParameter(req);
        Integer typeId = (typeItem == null) ? null : (Integer)typeItem.getId();
        List<ResearchGoalDto> goalList = getGoals(typeItem);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "typeId", typeId,
                    "goals", goalList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /**
     * Log user's action.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void logHomePageTypeClicked(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        ResearcherTypeItem typeItem = getTypeParameter(req);
        logHomePageTypeClicked(userItem, typeItem);
        writeJSON(resp, json("flag", "success"));
        return;
    }

    /**
     * Log user's action.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void logGoalPageGoalClicked(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        ResearcherTypeItem typeItem = getTypeParameter(req);
        ResearchGoalItem goalItem = getGoalParameter(req);
        logGoalPageGoalClicked(userItem, typeItem, goalItem);
        writeJSON(resp, json("flag", "success"));
        return;
    }

    //
    // Some utility methods to log user's actions.
    //

    /**
     * Log user's actions.
     * @param userItem user item
     * @param typeItem researcher type item
     */
    private void logGoalPageTypeClicked(UserItem userItem,
            ResearcherTypeItem typeItem) {
        String info;
        if (typeItem == null) {
            info = "From goal page: Researcher Type: null";
        } else {
            info = "From goal page: Researcher Type: "
                 + typeItem.getLabel() + " (" + typeItem.getId() + ")";
        }
        UserLogger.log(userItem, UserLogger.VIEW_RESEARCH_GOALS, info, false);
    }

    /**
     * Log user's actions.
     * @param userItem user item
     * @param typeItem researcher type item
     */
    private void logHomePageTypeClicked(UserItem userItem,
            ResearcherTypeItem typeItem) {
        String info = "From home page: Researcher Type: "
                    + typeItem.getLabel() + " (" + typeItem.getId() + ")";
        UserLogger.log(userItem, UserLogger.VIEW_RESEARCH_GOALS, info, false);
    }

    /**
     * Log user's actions.
     * @param userItem user item
     * @param typeItem researcher type item
     * @param goalItem research goal item
     */
    private void logGoalPageGoalClicked(UserItem userItem,
            ResearcherTypeItem typeItem, ResearchGoalItem goalItem) {
        String info;
        if (typeItem == null) {
            info = "From goal page: Research Goal: "
                 + goalItem.getTitle() + " (" + goalItem.getId() + ")";
        } else {
            info = "From goal page: Researcher Type: "
                 + typeItem.getLabel() + " (" + typeItem.getId() + ")"
                 + " Research Goal: "
                 + goalItem.getTitle() + " (" + goalItem.getId() + ")";
        }
        UserLogger.log(userItem, UserLogger.VIEW_RESEARCH_GOALS, info, false);
    }

    /**
     * Log user's actions.
     * @param userItem user item
     * @param typeItem researcher type item
     * @param goalItem research goal item
     */
    private void logHomePageGoalClicked(UserItem userItem,
            ResearcherTypeItem typeItem, ResearchGoalItem goalItem) {
        String info;
        if (typeItem == null) {
            info = "From home page: Research Goal: "
                 + goalItem.getTitle() + " (" + goalItem.getId() + ")";
        } else {
            info = "From home page: Researcher Type: "
                 + typeItem.getLabel() + " (" + typeItem.getId() + ")"
                 + " Research Goal: "
                 + goalItem.getTitle() + " (" + goalItem.getId() + ")";
        }
        UserLogger.log(userItem, UserLogger.VIEW_RESEARCH_GOALS, info, false);
    }
}