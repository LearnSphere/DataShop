/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.research;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ResearchGoalDao;
import edu.cmu.pslc.datashop.dao.ResearchGoalDatasetPaperMapDao;
import edu.cmu.pslc.datashop.dao.ResearcherTypeDao;
import edu.cmu.pslc.datashop.dao.ResearcherTypeResearchGoalMapDao;
import edu.cmu.pslc.datashop.dao.UserRoleDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapId;
import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapItem;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapId;
import edu.cmu.pslc.datashop.item.ResearcherTypeResearchGoalMapItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;

/**
 * This servlet handles all the requests from the External Tools pages.
 *
 * @author alida
 * @version $Revision: 12463 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-07-02 13:28:54 -0400 (Thu, 02 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalsEditServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP file name. */
    private static final String JSP_NAME = "/research_goals_edit.jsp";

    /** Title for this page - "External Tools". */
    public static final String SERVLET_TITLE = "Edit Research Goals";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ResearchGoalsEdit";

    /** Constant string for the Type ID parameter. */
    private static final String PARAM_TYPE_ID = "typeId";
    /** Constant string for the Goal ID parameter. */
    private static final String PARAM_GOAL_ID = "goalId";
    /** Constant string for the Dataset ID parameter. */
    private static final String PARAM_DATASET_ID = "datasetId";
    /** Constant string for the Paper ID parameter. */
    private static final String PARAM_PAPER_ID = "paperId";

    /** Ajax name for requesting method. */
    private static final String AJAX_REQUEST_ACTION = "ajaxAction";

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

            // User must be logged in
            if (userItem == null) {
                redirectHome(req, resp);
                return; //must return after redirect home
            }

            // User must be DataShop Admin or have the Research Goal Edit Role
            UserRoleDao userRoleDao = DaoFactory.DEFAULT.getUserRoleDao();
            boolean hasResearchGoalEditRole = userRoleDao.hasResearchGoalEditRole(userItem);
            if (!userItem.getAdminFlag() && !hasResearchGoalEditRole) {
                redirectHome(req, resp);
                return; //must return after redirect home
            }

            String actionParam = getParameter(req, AJAX_REQUEST_ACTION);
            if (actionParam == null) {
                showResearchGoals(req, resp, userItem);
            } else if (actionParam.equals("rtSaveEditType")) {
                rtSaveEditType(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtDeleteType")) {
                rtDeleteType(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtAddType")) {
                rtAddType(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtShowGoals")) {
                showGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtGetOtherGoals")) {
                rtGetOtherGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtAddGoal")) {
                rtAddGoal(req, resp, userItem);
                return;
            } else if (actionParam.equals("rtRemoveGoal")) {
                rtRemoveGoal(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgSaveEditGoal")) {
                rgSaveEditGoal(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgSaveEditGoalDesc")) {
                rgSaveEditGoalDesc(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgRequestDesc")) {
                rgRequestDesc(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgDeleteGoal")) {
                rgDeleteGoal(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgAddGoal")) {
                rgAddGoal(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgGetPapers")) {
                rgGetPapers(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgRemovePaper")) {
                rgRemovePaper(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgGetTypes")) {
                rgGetTypes(req, resp, userItem);
                return;
            } else if (actionParam.equals("rgRemoveType")) {
                rgRemoveType(req, resp, userItem);
                return;
            } else if (actionParam.equals("paperGetGoals")) {
                paperGetGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("paperGetOtherGoals")) {
                paperGetOtherGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("paperAddGoals")) {
                paperAddGoals(req, resp, userItem);
                return;
            } else if (actionParam.equals("paperRemoveGoal")) {
                paperRemoveGoal(req, resp, userItem);
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

        List<ResearchGoalDto> goalList = getGoals(null);
        req.setAttribute(ResearchGoalDto.ATTRIB_LIST, goalList);

        List<RgPaperWithGoalsDto> paperList = getPapersWithGoals();
        req.setAttribute(RgPaperWithGoalsDto.ATTRIB_LIST, paperList);

        List<RgPaperDto> papersWithoutGoalsList = getPapersWithoutGoals();
        req.setAttribute(RgPaperDto.ATTRIB_LIST, papersWithoutGoalsList);

        goToJsp(req, resp);
    }

    /**
     * Get the full list of researcher types.
     * @return list of researcher types
     */
    private List<ResearcherTypeDto> getResearcherTypes() {
        ResearcherTypeDao rtDao = DaoFactory.DEFAULT.getResearcherTypeDao();
        ResearcherTypeResearchGoalMapDao rtrgMapDao =
                DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao();
        List<ResearcherTypeItem> itemList = rtDao.findAllInOrder(null);
        List<ResearcherTypeDto> dtoList = new ArrayList<ResearcherTypeDto>();
        for (ResearcherTypeItem item : itemList) {
            Integer numberOfGoals = 0;
            Integer itemId = (Integer)item.getId();
            ResearcherTypeDto dto = new ResearcherTypeDto(itemId, item.getLabel(),
                                                          null, item.getTypeOrder());

            List<ResearcherTypeItem> subList = rtDao.findAllInOrder(itemId);
            List<ResearcherTypeDto> subDtoList = new ArrayList<ResearcherTypeDto>();
            if (subList.size() > 0) {
                for (ResearcherTypeItem subItem : subList) {
                    ResearcherTypeDto subDto = new ResearcherTypeDto((Integer)subItem.getId(),
                                                                     subItem.getLabel(),
                                                                     subItem.getParentTypeId(),
                                                                     subItem.getTypeOrder());
                    List<ResearchGoalItem> subGoalList = rtrgMapDao.findByType(subItem);
                    numberOfGoals = subGoalList.size();
                    subDto.setNumberOfGoals(numberOfGoals);
                    subDtoList.add(subDto);
                }
            }
            dto.setSubTypeList(subDtoList);

            List<ResearchGoalItem> goalList = rtrgMapDao.findByType(item);
            numberOfGoals = goalList.size();
            dto.setNumberOfGoals(numberOfGoals);
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
        ResearchGoalDatasetPaperMapDao rgdpMapDao =
                DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
        for (ResearchGoalItem item : itemList) {
            ResearchGoalDto dto = new ResearchGoalDto((Integer)item.getId(),
                    item.getTitle(), item.getDescription(), item.getGoalOrder());
            List<RgPaperWithGoalsDto> paperList = rgdpMapDao.getPapersGivenGoal(item);
            Integer numberOfPapers = paperList.size();
            dto.setNumberOfPapers(numberOfPapers);
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Get a list of papers WITH Research Goals.
     * @return list of papers with goals
     */
    private List<RgPaperWithGoalsDto> getPapersWithGoals() {
        List<RgPaperWithGoalsDto> dtoList =
             DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                    .getPapersWithGoals();
        return dtoList;
    }

    /**
     * Get a list of papers WITHOUT Research Goals.
     * @return list of papers with goals
     */
    private List<RgPaperDto> getPapersWithoutGoals() {
        List<RgPaperDto> dtoList =
             DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                    .getPapersWithoutGoals();
        return dtoList;
    }

    /**
     * Save changes to the researcher type label.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtSaveEditType(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearcherTypeItem typeItem = getTypeParameter(req);
        if (typeItem == null) {
            message = "Researcher type not found.";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            Integer typeId = (Integer)typeItem.getId();
            String newLabel = getParameter(req, "newLabel");
            if (newLabel != null && newLabel.length() > 0) {
                newLabel = stripHtml(newLabel).trim();
                typeItem.setLabel(newLabel);
                DaoFactory.DEFAULT.getResearcherTypeDao().saveOrUpdate(typeItem);

                reOrderTypesAndGoals();

                message = "Successfully updated researcher type.";
                String info = "Updated researcher type."
                        + " Type: " + typeItem.getLabel() + " (" + typeItem.getId() + ").";
                UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            } else {
                newLabel = typeItem.getLabel();
                message = "Invalid label.";
            }
            writeJSON(resp, json(
                    "flag", "success",
                    "typeId", typeId,
                    "newLabel", newLabel,
                    "message", message));
        }
        return;
    }

    /**
     * Delete a given researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtDeleteType(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearcherTypeItem typeItem = getTypeParameter(req);
        if (typeItem == null) {
            message = "Researcher type not found.";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            DaoFactory.DEFAULT.getResearcherTypeDao().delete(typeItem);
            message = "Successfully deleted researcher type.";
            String info = "Deleted researcher type."
                    + " Type: " + typeItem.getLabel() + " (" + typeItem.getId() + ").";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message));
        }
        return;
    }

    /**
     * Add a new researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtAddType(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        String rtLabel = getParameter(req, "typeLabel");
        if (rtLabel == null || rtLabel.length() == 0) {
            message = "Invalid type label";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            Integer newOrder = DaoFactory.DEFAULT.getResearcherTypeDao()
                    .getNextOrderValue();
            ResearcherTypeItem typeItem = new ResearcherTypeItem();
            typeItem.setLabel(stripHtml(rtLabel).trim());
            typeItem.setTypeOrder(newOrder);
            DaoFactory.DEFAULT.getResearcherTypeDao().saveOrUpdate(typeItem);

            reOrderTypesAndGoals();

            message = "Successfully added researcher type.";
            String info = "Added researcher type."
                    + " Type: " + typeItem.getLabel();
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message));
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
    private void showGoals(HttpServletRequest req, HttpServletResponse resp,
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
     * Gets a list of researcher types and goals given a researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtGetOtherGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        ResearcherTypeItem typeItem = getTypeParameter(req);
        Integer typeId = (typeItem == null) ? null : (Integer)typeItem.getId();
        List<ResearchGoalDto> otherGoals = getOtherGoals(typeItem);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "typeId", typeId,
                    "otherGoals", otherGoals));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /**
     * Get the research goals for the given researcher type.
     * @param typeItem the researcher type item, can be null
     * @return either a subset of goals or all if given type is null
     */
    private List<ResearchGoalDto> getOtherGoals(ResearcherTypeItem typeItem) {
        List<ResearchGoalDto> dtoList = new ArrayList<ResearchGoalDto>();
        List<ResearchGoalItem> itemList;
        itemList = DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao()
                .findOtherGoals(typeItem);
        for (ResearchGoalItem item : itemList) {
            ResearchGoalDto dto = new ResearchGoalDto((Integer)item.getId(),
                    item.getTitle(), item.getDescription(), item.getGoalOrder());
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Add the list of research goals to the given researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtAddGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        ResearcherTypeItem typeItem = getTypeParameter(req);
        String goalIds = getParameter(req, "goalList");
        String[] goalList = goalIds.trim().split(",");

        if (typeItem != null) {
            ResearcherTypeResearchGoalMapDao mapDao =
                    DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao();
            boolean success = false;
            for (int idx = 0; idx < goalList.length; idx++) {
                String goalId = goalList[idx];
                if (goalId != null && goalId.length() > 0) {
                    ResearchGoalItem goalItem = getGoalItem(goalId);
                    if (goalItem != null) {
                        ResearcherTypeResearchGoalMapId mapId =
                                new ResearcherTypeResearchGoalMapId(typeItem, goalItem);
                        ResearcherTypeResearchGoalMapItem mapItem =
                                new ResearcherTypeResearchGoalMapItem();
                        mapItem.setId(mapId);
                        mapItem.setGoalOrder(1); //Not used
                        mapDao.saveOrUpdate(mapItem);
                        success = true;
                    } else {
                        success = false;
                    }
                }
            } // end for loop
            if (success) {
                errorFlag = false;
                message = "Successfully added type to research goal(s).";
                String info = "Added type to research goals."
                        + " Type: " + typeItem.getLabel() + " (" + typeItem.getId() + ")"
                        + " Goal Ids: " + goalIds;
                UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            } else {
                errorFlag = true;
                message = "Failed to add research goal(s).";
            }
            reOrderTypesAndGoals();
        } else {
            errorFlag = true;
            message = "Failed to add goal to type.";
        }
        getGoals(resp, errorFlag, message, typeItem);
    }

    /**
     * Remove the given research goal to the given researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rtRemoveGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        ResearcherTypeItem typeItem = getTypeParameter(req);
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (typeItem != null && goalItem != null) {
            ResearcherTypeResearchGoalMapDao mapDao =
                    DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao();
            ResearcherTypeResearchGoalMapId mapId =
                    new ResearcherTypeResearchGoalMapId(typeItem, goalItem);
            ResearcherTypeResearchGoalMapItem mapItem = new ResearcherTypeResearchGoalMapItem();
            mapItem.setId(mapId);
            mapDao.delete(mapItem);
            message = "Successfully removed the research goal from type.";
            String info = "Removed research goal from type."
                    + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + "),"
                    + " Type: " + typeItem.getLabel() + " (" + typeItem.getId() + ").";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            reOrderTypesAndGoals();
        } else {
            errorFlag = true;
            message = "Failed to remove goal from type.";
        }
        getGoals(resp, errorFlag, message, typeItem);
    }

    /**
     * Gets a list of research goals given a researcher type.
     * @param resp {@link HttpServletResponse}
     * @param errorFlag indicates whether an error occurred
     * @param message the message for the user
     * @param typeItem the researcher type item
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getGoals(HttpServletResponse resp,
            Boolean errorFlag, String message,
            ResearcherTypeItem typeItem) throws IOException, JSONException {
        Integer typeId = (typeItem == null) ? null : (Integer)typeItem.getId();
        List<ResearchGoalDto> goalList = getGoals(typeItem);
        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message,
                    "typeId", typeId,
                    "goals", goalList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        }
        return;
    }

    //--------------------------- REORDER SECTION ------------------------------

    /**
     * Reorder the researcher types and research goals
     * when goals are added to papers.
     */
    private void reOrderTypesAndGoals() {
        //Reorder the Research Goals
        ResearchGoalDao goalDao = DaoFactory.DEFAULT.getResearchGoalDao();
        List<ResearchGoalItem> goals =
             DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                    .getGoalsInOrder();
        int goalOrder = 1;
        for (ResearchGoalItem item : goals) {
            item.setGoalOrder(goalOrder);
            goalDao.saveOrUpdate(item);
            goalOrder++;
        }

        //Reorder the Researcher Types
        ResearcherTypeDao typeDao = DaoFactory.DEFAULT.getResearcherTypeDao();
        List<ResearcherTypeItem> types =
                DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                .getTypesInOrder();
        int typeOrder = 1;
        for (ResearcherTypeItem item : types) {
            item.setTypeOrder(typeOrder);
            typeDao.saveOrUpdate(item);
            typeOrder++;
        }
    }
    //------------------------------ RG SECTION ------------------------------

    /**
     * Save changes to the research goal title.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgSaveEditGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            message = "Research goal not found.";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            Integer goalId = (Integer)goalItem.getId();
            String newTitle = getParameter(req, "newTitle");
            if (newTitle != null && newTitle.length() > 0) {
                newTitle = stripHtml(newTitle).trim();
                goalItem.setTitle(newTitle);
                DaoFactory.DEFAULT.getResearchGoalDao().saveOrUpdate(goalItem);

                reOrderTypesAndGoals();

                message = "Successfully updated research goal.";
                String info = "Updated research goal."
                        + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + ").";
                UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            } else {
                newTitle = goalItem.getTitle();
                message = "Invalid title.";
            }
            writeJSON(resp, json(
                    "flag", "success",
                    "goalId", goalId,
                    "newTitle", newTitle,
                    "message", message));
        }
        return;
    }

    /**
     * Save changes to the research goal description.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgSaveEditGoalDesc(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", "Research goal not found."));
        } else {
            Integer goalId = (Integer)goalItem.getId();
            String newDesc = getParameter(req, "newDesc");
            if (newDesc != null && newDesc.length() > 0) {
                Boolean isValid = isHtmlValid(newDesc);
                if (isValid) {
                    if (areHrefsValid(newDesc)) {
                        goalItem.setDescription(newDesc);
                        DaoFactory.DEFAULT.getResearchGoalDao().saveOrUpdate(goalItem);
                        message = "Successfully updated research goal description.";
                        String info = "Updated research goal description."
                                + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + ").";
                        UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
                        writeJSON(resp, json(
                                "flag", "success",
                                "goalId", goalId,
                                "newDesc", newDesc,
                                "message", message));
                    } else {
                        writeJSON(resp, json(
                                "flag", "error",
                                "message", "Double quotes in HREF do not match."));
                    }
                } else {
                    writeJSON(resp, json(
                            "flag", "error",
                            "message", "Invalid HTML found."));
                }
            } else {
                writeJSON(resp, json(
                        "flag", "success",
                        "goalId", goalId,
                        "newDesc", newDesc,
                        "message", "Description cleared."));
            }
        }
        return;
    }

    /**
     * Check if the HTML is valid using some well known, free HTML parser.
     * @param text the given string
     * @return true if valid, false otherwise
     */
    private boolean isHtmlValid(String text) {
        boolean isValid = true;
        //TBD need to find a way to pass in the base uri as most
        //link will be relative and they do not pass this test.
        //isValid = Jsoup.isValid(text, Whitelist.relaxed());
        //logDebug("isHtmlValid: ", isValid);
        return isValid;

    }

    /**
     * Checks for an HREF and if it exists checks that double quotes are closed.
     * @param text the text string to check
     * @return true if all okay, false otherwise
     */
    private boolean hrefQuotesMatch(String text) {
        boolean match = true;
        int numHrefs = StringUtils.countMatches(text.toLowerCase(), " href");
        if (numHrefs > 0) {
            Document doc = Jsoup.parseBodyFragment(text);
            Elements anchorElements = doc.select("a");
            if (anchorElements.size() != numHrefs) {
                match = false;
            }
        }
        logDebug("hrefQuotesMatch: ", match);
        return match;
    }

    /**
     * Checks for an HREF and if it exists checks that double quotes are closed.
     * @param text the text string to check
     * @return true if all okay, false otherwise
     */
    private boolean areHrefsValid(String text) {
        boolean passes = true;
        String inputHTML = text;
        Parser parser = new Parser();
        try {
            parser.setInputHTML(inputHTML);

            parser.setEncoding("UTF-8");
            NodeList nl = parser.parse(null);
            NodeList hrefs = nl.extractAllNodesThatMatch(new TagNameFilter("a"), true);
            for (int nodeCount = 0; nodeCount < hrefs.size(); nodeCount++) {
                logDebug("A element found: ", hrefs.elementAt(nodeCount).toHtml());
                String[] splitAttributes = hrefs.elementAt(nodeCount).toString().split(";");
                if (splitAttributes.length > 0) {
                    String url = splitAttributes[0].replaceAll("Link to : ", "");

                    if (!url.contains(".*://")) {
                        url = "http://" + url;
                    }
                    if (!isValidURL(url)) {
                        logDebug("Invalid URL prevented in ResearchGoalsEditServlet: ", url);
                        passes = false;

                    }
                }
            }
        } catch (ParserException e) {
            return false;
        }
        return passes;
    }

    /**
     * Determine if a URL is valid.
     * @param url the URL
     * @return whether or not the URL is valid
     */
    public boolean isValidURL(String url) {
        URL u = null;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    /**
     * On cancel edit, need to go get the goal description again to avoid
     * the trouble of having to save HTML text in an HTML element.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgRequestDesc(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", "Research goal not found."));
        } else {
            Integer goalId = (Integer)goalItem.getId();
            String newDesc = goalItem.getDescription();
            writeJSON(resp, json(
                    "flag", "success",
                    "goalId", goalId,
                    "newDesc", newDesc,
                    "message", message));
        }
        return;
    }

    /**
     * Delete a given research goal.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgDeleteGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            message = "Research goal not found.";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            DaoFactory.DEFAULT.getResearchGoalDao().delete(goalItem);
            message = "Successfully deleted research goal.";
            String info = "Deleted research goal."
                    + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + ").";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message));
        }
        return;
    }

    /**
     * Add a new research goal.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgAddGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message = null;
        String rgTitle = getParameter(req, "goalTitle");
        if (rgTitle == null || rgTitle.length() == 0) {
            message = "Invalid goal title";
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        } else {
            Integer newOrder = DaoFactory.DEFAULT.getResearchGoalDao()
                    .getNextOrderValue();
            ResearchGoalItem goalItem = new ResearchGoalItem();
            goalItem.setTitle(stripHtml(rgTitle).trim());
            goalItem.setDescription("");
            goalItem.setGoalOrder(newOrder);
            DaoFactory.DEFAULT.getResearchGoalDao().saveOrUpdate(goalItem);

            reOrderTypesAndGoals();

            message = "Successfully added research goal.";
            String info = "Added research goal."
                    + " Goal: " + goalItem.getTitle();
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message));
        }
        return;
    }

    /**
     * Get a list of papers for a given research goal.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgGetPapers(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            errorMsg = "Invalid goal.";
            errorFlag = true;
        }

        getPapers(resp, errorFlag, errorMsg, goalItem);
        return;
    }

    /**
     * Remove the given paper from the given research goal.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgRemovePaper(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        ResearchGoalItem goalItem = getGoalParameter(req);
        DatasetItem datasetItem = getDatasetParameter(req);
        PaperItem paperItem = getPaperParameter(req);
        if (goalItem != null && datasetItem != null && paperItem != null) {
            ResearchGoalDatasetPaperMapDao mapDao =
                DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
            ResearchGoalDatasetPaperMapId mapId =
                    new ResearchGoalDatasetPaperMapId(goalItem, datasetItem, paperItem);
            ResearchGoalDatasetPaperMapItem mapItem = new ResearchGoalDatasetPaperMapItem();
            mapItem.setId(mapId);
            mapDao.delete(mapItem);
            message = "Successfully removed research goal from paper.";
            String info = "Removed research goal from paper."
                    + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + "),"
                    + " Paper(" + paperItem.getId() + ")";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            reOrderTypesAndGoals();
        } else {
            errorFlag = true;
            message = "Failed to remove paper from goal.";
            logger.error(message);
        }
        getPapers(resp, errorFlag, message, goalItem);
    }

    /**
     * Gets a list of papers for a given research goal.
     * @param resp {@link HttpServletResponse}
     * @param errorFlag indicates whether an error occurred
     * @param message the message for the user
     * @param goalItem the research goal item
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getPapers(HttpServletResponse resp,
            Boolean errorFlag, String message,
            ResearchGoalItem goalItem) throws IOException, JSONException {
        Integer goalId = (goalItem == null) ? null : (Integer)goalItem.getId();
        List<RgPaperWithGoalsDto> papers = null;

        ResearchGoalDatasetPaperMapDao mapDao =
                DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
        papers = mapDao.getPapersGivenGoal(goalItem);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message,
                    "goalId", goalId,
                    "papers", papers));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        }
    }

    /**
     * Get a list of researcher types for a given research goal.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgGetTypes(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        ResearchGoalItem goalItem = getGoalParameter(req);
        if (goalItem == null) {
            errorMsg = "Unknown error occurred";
            errorFlag = true;
        }

        getTypes(resp, errorFlag, errorMsg, goalItem);
    }

    /**
     * Remove the given research goal from the given researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void rgRemoveType(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        ResearcherTypeItem typeItem = getTypeParameter(req);
        ResearchGoalItem goalItem = getGoalParameter(req);
        if (typeItem != null && goalItem != null) {
            ResearcherTypeResearchGoalMapDao mapDao =
                    DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao();
            ResearcherTypeResearchGoalMapId mapId =
                    new ResearcherTypeResearchGoalMapId(typeItem, goalItem);
            ResearcherTypeResearchGoalMapItem mapItem = new ResearcherTypeResearchGoalMapItem();
            mapItem.setId(mapId);
            mapDao.delete(mapItem);
            message = "Successfully removed the research goal from type.";
            String info = "Removed research goal from type."
                    + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + "),"
                    + " Type: " + typeItem.getLabel() + " (" + typeItem.getId() + ").";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
        } else {
            errorFlag = true;
            message = "Failed to remove type from goal.";
        }
        getTypes(resp, errorFlag, message, goalItem);
    }

    /**
     * Gets a list of research types given a research goal.
     * @param resp {@link HttpServletResponse}
     * @param errorFlag indicates whether an error occurred
     * @param message the message for the user
     * @param goalItem the research goal item
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void getTypes(HttpServletResponse resp,
            Boolean errorFlag, String message,
            ResearchGoalItem goalItem) throws IOException, JSONException {
        Integer goalId = (goalItem == null) ? null : (Integer)goalItem.getId();
        List<ResearcherTypeDto> typeList = new ArrayList<ResearcherTypeDto>();
        ResearcherTypeResearchGoalMapDao mapDao =
                DaoFactory.DEFAULT.getResearcherTypeResearchGoalMapDao();
        List<ResearcherTypeItem> list = mapDao.findByGoal(goalItem);
        for (ResearcherTypeItem item : list) {
            ResearcherTypeDto dto = new ResearcherTypeDto((Integer)item.getId(), item.getLabel(),
                                                          null, item.getTypeOrder());
            typeList.add(dto);
        }

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "message", message,
                    "goalId", goalId,
                    "typeList", typeList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        }
        return;
    }

    /**
     * Gets a list research goals given a paper id.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void paperGetGoals(HttpServletRequest req, HttpServletResponse resp,
                    UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String errorMsg = "Invalid paper";
        DatasetItem datasetItem = getDatasetParameter(req);
        PaperItem paperItem = getPaperParameter(req);
        if (datasetItem == null || paperItem == null) {
            errorFlag = true;
        }
        paperGetGoals(resp, errorFlag, errorMsg, datasetItem, paperItem);
    }

    /**
     * Gets the goals s for a given dataset/paper..
     * @param resp {@link HttpServletResponse}
     * @param errorFlag error flag
     * @param message user message
     * @param datasetItem dataset item
     * @param paperItem paper item
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void paperGetGoals(HttpServletResponse resp,
            Boolean errorFlag, String message,
            DatasetItem datasetItem, PaperItem paperItem)
            throws IOException, JSONException {

        Integer datasetId = (datasetItem == null) ? null : (Integer)datasetItem.getId();
        Integer paperId = (paperItem == null) ? null : (Integer)paperItem.getId();

        List<ResearchGoalDto> goals =
             DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                    .getGoalsGivenPaper(paperId);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "datasetId", datasetId,
                    "paperId", paperId,
                    "goals", goals));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", message));
        }
        return;
    }

    /**
     * Gets a list the goals not attached to the given paper.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void paperGetOtherGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;

        DatasetItem datasetItem = getDatasetParameter(req);
        Integer datasetId = (datasetItem == null) ? null : (Integer)datasetItem.getId();
        PaperItem paperItem = getPaperParameter(req);
        Integer paperId = (paperItem == null) ? null : (Integer)paperItem.getId();
        List<ResearchGoalDto> otherGoals = paperGetOtherGoals(paperItem);

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "datasetId", datasetId,
                    "paperId", paperId,
                    "otherGoals", otherGoals));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /**
     * Get the research goals for the given researcher type.
     * @param paperItem the paper item, cannot be null
     * @return either a subset of goals or all if given type is null
     */
    private List<ResearchGoalDto> paperGetOtherGoals(PaperItem paperItem) {
        List<ResearchGoalDto> dtoList =
                DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao()
                .paperGetOtherGoals((Integer)paperItem.getId());
        return dtoList;
    }

    /**
     * Add the list of research goals to the given researcher type.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void paperAddGoals(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        DatasetItem datasetItem = getDatasetParameter(req);
        PaperItem paperItem = getPaperParameter(req);
        String goalIds = getParameter(req, "goalList");
        String[] goalList = goalIds.trim().split(",");

        if (datasetItem != null && paperItem != null) {
            ResearchGoalDatasetPaperMapDao mapDao =
                    DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
            boolean success = false;
            for (int idx = 0; idx < goalList.length; idx++) {
                String goalId = goalList[idx];
                if (goalId != null && goalId.length() > 0) {
                    ResearchGoalItem goalItem = getGoalItem(goalId);
                    if (goalItem != null) {
                        ResearchGoalDatasetPaperMapId mapId =
                                new ResearchGoalDatasetPaperMapId(goalItem, datasetItem, paperItem);
                        ResearchGoalDatasetPaperMapItem mapItem =
                                new ResearchGoalDatasetPaperMapItem();
                        mapItem.setId(mapId);
                        mapItem.setPaperOrder(1); //Not used
                        mapDao.saveOrUpdate(mapItem);
                        success = true;
                    } else {
                        logger.error("Research goal NOT found: " + goalId);
                        success = false;
                    }
                }
            } // end for loop
            if (success) {
                errorFlag = false;
                message = "Successfully added paper to research goal(s).";
                String info = "Added paper to research goals."
                        + " Paper (" + paperItem.getId() + "),"
                        + " Goal Ids: " + goalIds;
                UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            } else {
                errorFlag = true;
                message = "Failed to add paper to research goal(s).";
            }
            reOrderTypesAndGoals();
        } else {
            if (datasetItem == null) {
                logger.error("paperAddGoals: Dataset item is null");
            }
            if (paperItem == null) {
                logger.error("paperAddGoals: Paper item is null");
            }
            errorFlag = true;
            message = "Failed to add goal to type.";
        }
        paperGetGoals(resp, errorFlag, message, datasetItem, paperItem);
    }

    /**
     * Remove the given goal from the dataset/paper pair.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the current user
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    private void paperRemoveGoal(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        boolean errorFlag = false;
        String message;
        ResearchGoalItem goalItem = getGoalParameter(req);
        DatasetItem datasetItem = getDatasetParameter(req);
        PaperItem paperItem = getPaperParameter(req);

        if (goalItem != null && datasetItem != null && paperItem != null) {
            ResearchGoalDatasetPaperMapDao mapDao =
                    DaoFactory.DEFAULT.getResearchGoalDatasetPaperMapDao();
            ResearchGoalDatasetPaperMapId mapId =
                    new ResearchGoalDatasetPaperMapId(goalItem, datasetItem, paperItem);
            ResearchGoalDatasetPaperMapItem mapItem = mapDao.get(mapId);
            mapDao.delete(mapItem);
            message = "Successfully removed research goal from paper.";
            String info = "Removed research goal from paper."
                    + " Goal: " + goalItem.getTitle() + " (" + goalItem.getId() + "),"
                    + " Paper (" + paperItem.getId() + ")";
            UserLogger.log(userItem, UserLogger.EDIT_RESEARCH_GOALS, info, false);
            reOrderTypesAndGoals();
        } else {
            if (goalItem == null) {
                logger.error("paperAddGoals: Goal item is null");
            }
            if (datasetItem == null) {
                logger.error("paperAddGoals: Dataset item is null");
            }
            if (paperItem == null) {
                logger.error("paperAddGoals: Paper item is null");
            }
            errorFlag = true;
            message = "Failed to add goal to type.";
        }
        paperGetGoals(resp, errorFlag, message, datasetItem, paperItem);
    }

    //------------------------------ UTILITIES ------------------------------

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
        return getGoalItem(getParameter(req, PARAM_GOAL_ID));
    }

    /**
     * Get the goal item given an id as a string.
     * @param idParam the string of the id
     * @return the item if found, null otherwise
     */
    private ResearchGoalItem getGoalItem(String idParam) {
        ResearchGoalItem item = null;
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
     * Get the Research Goal item from the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the goal item if valid id in parameter, null otherwise
     */
    private DatasetItem getDatasetParameter(HttpServletRequest req) {
        return getDatasetItem(getParameter(req, PARAM_DATASET_ID));
    }

    /**
     * Get the goal item given an id as a string.
     * @param idParam the string of the id
     * @return the item if found, null otherwise
     */
    private DatasetItem getDatasetItem(String idParam) {
        DatasetItem item = null;
        if (idParam != null) {
            try {
                Integer itemId = Integer.parseInt(idParam);
                item = DaoFactory.DEFAULT.getDatasetDao().get(itemId);
            } catch (NumberFormatException exception) {
                item = null;
            }
        }
        return item;
    }

    /**
     * Get the Research Goal item from the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the goal item if valid id in parameter, null otherwise
     */
    private PaperItem getPaperParameter(HttpServletRequest req) {
        return getPaperItem(getParameter(req, PARAM_PAPER_ID));
    }

    /**
     * Get the goal item given an id as a string.
     * @param idParam the string of the id
     * @return the item if found, null otherwise
     */
    private PaperItem getPaperItem(String idParam) {
        PaperItem item = null;
        if (idParam != null) {
            try {
                Integer itemId = Integer.parseInt(idParam);
                item = DaoFactory.DEFAULT.getPaperDao().get(itemId);
            } catch (NumberFormatException exception) {
                item = null;
            }
        }
        return item;
    }

}