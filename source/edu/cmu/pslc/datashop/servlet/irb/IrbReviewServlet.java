/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper;

/**
 * This servlet is for displaying IRB Review and All IRBs pages.
 *
 * @author Cindy Tipper
 * @version $Revision: 12069 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-03-10 16:52:49 -0400 (Tue, 10 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbReviewServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String IRB_REVIEW_JSP_NAME = "/jsp_project/irb_review.jsp";
    /** The JSP name for this servlet. */
    public static final String ALL_IRBS_JSP_NAME = "/jsp_project/all_irbs.jsp";

    /** Constant for the request attribute. */
    public static final String IRB_REVIEW_ATTR = "irbReviewDto";
    /** Constant for the request attribute. */
    public static final String ALL_IRBS_ATTR = "allIRBsDto";
    /** Constant for the request attribute. */
    public static final String IRB_REVIEW_MESSAGE_ATTR = "allIRBsDto_message";
    /** Constant for the request attribute. */
    public static final String IRB_REVIEW_MESSAGE_LEVEL_ATTR = "allIRBsDto_messageLevel";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "IRBReview";

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

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {
            String jspName = IRB_REVIEW_JSP_NAME;

            UserItem userItem = getLoggedInUserItem(req);

            // If user not logged in, go to main project page.
            if ((userItem == null) || !isUserAuthorized(req)) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(ProjectServlet.SERVLET_NAME);
                disp.forward(req, resp);
                return;
            }

            // If 'all' is not specified, display the 'IRB Review' page, otherwise 'All IRBs'
            String allIRBs = (String)req.getParameter("all");

            // The "IRB Review" page...
            if (allIRBs == null) {
                // Check if the sort by column has been changed
                String actionParam = getParameter(req, "irbReviewAction");

                //Check for Ajax Requests
                if (actionParam != null && actionParam.equals("requestDatasets")) {
                    requestDatasets(req, resp);
                    return;
                }
                if (actionParam != null && actionParam.equals("saveChangesDatasets")) {
                    saveChangesDatasets(req, resp, userItem);
                    return;
                }

                //Check for Non-Ajax Requests
                if (actionParam != null && actionParam.equals("sort")) {
                    String sortByParam = getParameter(req, "sortBy");
                    if (sortByParam != null) {
                        setSortByColumnForReview(req, sortByParam);
                    } else {
                        logger.warn("All IRBs: The sortBy parameter is unexpectedly null.");
                    }
                }

                // Check if the searchBy has been specified
                if (actionParam != null && actionParam.equals("search")) {
                    String searchByParam = getParameter(req, "searchBy");
                    if (searchByParam != null) {
                        setSearchByForReview(req, searchByParam);
                        logger.info("Search IRB Review by: " + searchByParam);
                    } else {
                        logger.warn("All IRBs: The searchBy parameter is unexpectedly null.");
                    }
                }

                // Check if the searchByPiDp has been specified
                if (actionParam != null && actionParam.equals("searchByPiDp")) {
                    String searchByPiDpParam = getParameter(req, "searchByPiDp");
                    if (searchByPiDpParam != null) {
                        setSearchByPiDpForReview(req, searchByPiDpParam);
                        logger.info("SearchByPiDp: " + searchByPiDpParam);
                    } else {
                        logger.warn("All IRBs: The searchBy parameter is unexpectedly null.");
                    }
                }

                // Check if the filtering has been specified
                if (actionParam != null && actionParam.equals("filter")) {
                    setFilterForReview(req);
                }

                jspName = IRB_REVIEW_JSP_NAME;

                IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                List<ProjectReviewDto> projectReviewList =
                    irbHelper.getAllProjectReviewDtos(IrbContext.getContext(req), userItem);
                req.setAttribute(IRB_REVIEW_ATTR, projectReviewList);

                UserLogger.log(userItem, UserLogger.VIEW_IRB_REVIEW);

            } else {
                // Check if the sort by column has been changed
                String actionParam = getParameter(req, "irbReviewAction");
                if (actionParam != null && actionParam.equals("sort")) {
                    String sortByParam = getParameter(req, "sortBy");
                    if (sortByParam != null) {
                        setSortByColumnForAllIRBs(req, sortByParam);
                    } else {
                        logger.warn("IRB Review: The sortBy parameter is unexpectedly null.");
                    }
                }

                // Check if the searchBy has been specified
                if (actionParam != null && actionParam.equals("search")) {
                    String searchByParam = getParameter(req, "searchBy");
                    if (searchByParam != null) {
                        setSearchByForAllIRBs(req, searchByParam);
                        logger.info("Search All IRBs by: " + searchByParam);
                    } else {
                        logger.warn("IRB Review: The searchBy parameter is unexpectedly null.");
                    }
                }

                jspName = ALL_IRBS_JSP_NAME;

                IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                List<IrbDto> irbList = irbHelper.getAllIRBs(IrbContext.getContext(req));
                req.setAttribute(ALL_IRBS_ATTR, irbList);

                UserLogger.log(userItem, UserLogger.VIEW_ALL_IRBS);
            }

            logger.info("Going to JSP: " + jspName);
            RequestDispatcher disp = getServletContext().getRequestDispatcher(jspName);
            disp.forward(req, resp);

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
     * Helper method to determine if logged in user authorized to view IRBReview JSPs.
     * @param req {@link HttpServletRequest}
     * @return boolean flag
     */
    private boolean isUserAuthorized(HttpServletRequest req) {
        UserItem user = getUser(req);

        if (user.getAdminFlag()) {
            return true;
        }

        ProjectPageHelper projectPageHelper = HelperFactory.DEFAULT.getProjectPageHelper();
        if (projectPageHelper.hasResearchManagerRole(user)) {
            return true;
        }

        return false;
    }

    /**
     * Sets the sort parameters in the IRB Review context given a column name
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private static void setSortByColumnForReview(HttpServletRequest req, String sortByColumn) {
        IrbContext context = IrbContext.getContext(req);
        context.setReviewSortByColumn(sortByColumn, true);
        IrbContext.setContext(req, context);
    }

    /**
     * Sets the sort parameters in the All IRBs context given a column name
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private static void setSortByColumnForAllIRBs(HttpServletRequest req, String sortByColumn) {
        IrbContext context = IrbContext.getContext(req);
        context.setAllIRBsSortByColumn(sortByColumn, true);
        IrbContext.setContext(req, context);
    }

    /**
     * Sets the searchBy parameters in the IRB Review context.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private static void setSearchByForReview(HttpServletRequest req, String searchBy) {
        IrbContext context = IrbContext.getContext(req);
        context.setReviewSearchBy(searchBy);
        IrbContext.setContext(req, context);
    }

    /**
     * Sets the searchByPiDp parameter in the IRB Review context.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private static void setSearchByPiDpForReview(HttpServletRequest req, String searchBy) {
        IrbContext context = IrbContext.getContext(req);
        context.setReviewSearchByPiDp(searchBy);
        IrbContext.setContext(req, context);
    }

    /**
     * Sets the searchBy parameters in the All IRBs context.
     * @param req {@link HttpServletRequest}
     * @param searchBy the string to search by
     */
    private static void setSearchByForAllIRBs(HttpServletRequest req, String searchBy) {
        IrbContext context = IrbContext.getContext(req);
        context.setAllIRBsSearchBy(searchBy);
        IrbContext.setContext(req, context);
    }

    /**
     * Sets the filter parameters in the IRB Review context.
     * @param req {@link HttpServletRequest}
     */
    private void setFilterForReview(HttpServletRequest req) {
        IrbContext context = IrbContext.getContext(req);
        IrbReviewFilter filter = new IrbReviewFilter(getParameter(req, "filterSubjectTo"),
                                                     getParameter(req, "filterShareability"),
                                                     getParameter(req, "filterDataType"),
                                                     getParameter(req, "filterDatasets"),
                                                     getParameter(req, "filterPublic"),
                                                     getParameter(req, "filterNeedsAttn"));
        filter.setPcBefore(getParameter(req, "filterPcBefore"));
        filter.setPcDate(getParameter(req, "filterPcDate"));
        filter.setDlaBefore(getParameter(req, "filterDlaBefore"));
        filter.setDlaDate(getParameter(req, "filterDlaDate"));
        context.setReviewFilter(filter);
        IrbContext.setContext(req, context);
    }

    /** Constant string for the Project ID parameter. */
    private static final String PARAM_PROJECT_ID = "projectId";

    /**
     * Get the project item from the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the project item if valid id in parameter, null otherwise
     */
    private ProjectItem getProjectParameter(HttpServletRequest req) {
        return getProjectItem(getParameter(req, PARAM_PROJECT_ID));
    }

    /**
     * Get the project item given an id as a string.
     * @param idParam the string of the id
     * @return the item if found, null otherwise
     */
    private ProjectItem getProjectItem(String idParam) {
        ProjectItem item = null;
        if (idParam != null) {
            try {
                Integer itemId = Integer.parseInt(idParam);
                item = DaoFactory.DEFAULT.getProjectDao().get(itemId);
            } catch (NumberFormatException exception) {
                item = null;
            }
        }
        return item;
    }

    /**
     * Get the dataset item given an id as a string.
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
     * Gets a list of datasets for the given project.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    void requestDatasets(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, JSONException {
        String errorMsg = null;
        boolean errorFlag = false;
        List<IrbDatasetDto> dtoList = new ArrayList();

        boolean isProjectOrDatashopAdminFlag = true; //FIXME get the real value

        ProjectItem projectItem = getProjectParameter(req);
        if (projectItem == null) {
            errorMsg = "Unknown error occurred";
            errorFlag = true;
        } else {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            List<DatasetItem> itemList = (List<DatasetItem>)datasetDao.
                    findByProject(projectItem, isProjectOrDatashopAdminFlag);
            for (DatasetItem item : itemList) {
                IrbDatasetDto dto = new IrbDatasetDto(item);
                dtoList.add(dto);
            }
        }

        if (!errorFlag) {
            writeJSON(resp, json(
                    "flag", "success",
                    "projectId", projectItem.getId(),
                    "datasets", dtoList));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", errorMsg));
        }
        return;
    }

    /** Constant for the index into the string returned from UI: Dataset. */
    private static final int DS_IDX = 0;
    /** Constant for the index into the string returned from UI: Appears Anonymous. */
    private static final int AA_IDX = 1;
    /** Constant for the index into the string returned from UI: IRB Uploaded. */
    private static final int IU_IDX = 2;
    /** Constant for the index into the string returned from UI: Study Flag. */
    private static final int SF_IDX = 3;

    /**
     * Gets a list of datasets for the given project.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws JSONException JSON exception
     * @throws IOException IO exception
     */
    void saveChangesDatasets(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws IOException, JSONException {
        boolean errorFlag = false;
        boolean dataChanged = false;

        ProjectItem projectItem = getProjectParameter(req);
        if (projectItem == null) {
            errorFlag = true;
        } else {
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            boolean irbUploadedChanged = false;
            boolean appearsAnonChanged = false;

            String theData = req.getParameter("theData");

            String[] arr = theData.split("-");
            for (int idx = 0; idx < arr.length; idx++) {
                boolean datasetChanged = false;
                String[] values = arr[idx].split(",");
                DatasetItem datasetItem = getDatasetItem(values[DS_IDX]);

                String oldAppearsAnon = datasetItem.getAppearsAnonymous();
                String newAppearsAnon = DatasetItem.getAppearsAnonymousEnum(values[AA_IDX]);
                if (oldAppearsAnon == null || !oldAppearsAnon.equals(newAppearsAnon)) {
                    datasetItem.setAppearsAnonymous(newAppearsAnon);
                    datasetChanged = true;
                    appearsAnonChanged = true;
                }

                String oldIrbUploaded = datasetItem.getIrbUploaded();
                String newIrbUploaded = values[IU_IDX];
                if (!oldIrbUploaded.equals(newIrbUploaded)) {
                    datasetItem.setIrbUploaded(newIrbUploaded);
                    datasetChanged = true;
                    irbUploadedChanged = true;
                }

                String oldHasStudyData = datasetItem.getStudyFlag();
                String newHasStudyData = values[SF_IDX];
                if (!oldHasStudyData.equals(newHasStudyData)) {
                    datasetItem.setStudyFlag(newHasStudyData);
                    datasetChanged = true;
                }

                if (datasetChanged) {
                    datasetDao.saveOrUpdate(datasetItem);
                    dataChanged = true;
                }
                if (oldAppearsAnon == null || !oldAppearsAnon.equals(newAppearsAnon)) {
                    logChangeToAppearsAnon(datasetItem, userItem,
                            oldAppearsAnon, newAppearsAnon);
                }
                if (!oldIrbUploaded.equals(newIrbUploaded)) {
                    logChangeToIrbUploaded(datasetItem, userItem,
                            oldIrbUploaded, newIrbUploaded);
                }
                if (!oldHasStudyData.equals(newHasStudyData)) {
                    logChangeToHasStudyData(datasetItem, userItem,
                            oldHasStudyData, newHasStudyData);
                }
            } // end for loop

            // If the IRB Uploaded value was changed for any dataset,
            // then decide if we need to change the value for the project's Needs Attention flag.
            if (irbUploadedChanged) {
                updateNeedsAttentionIrbUploadedChanged(logger, projectItem, userItem);
            }

            // If the 'Appears Anonymous' value changed for any of the datasets,
            // check to see if the 'Needs Attention' flag should change for the project.
            if (appearsAnonChanged) {
                updateNeedsAttentionAppearsAnonChanged(projectItem, userItem);
            }
        }

        if (!errorFlag) {
            String msg = "No changes made.";
            if (dataChanged) {
                msg = "Changes saved for project: " + projectItem.getProjectName() + ".";
            }
            writeJSON(resp, json(
                    "flag", "success",
                    "projectId", projectItem.getId(),
                    "needsAttention", projectItem.getNeedsAttentionDisplay(),
                    "message", msg));
        } else {
            writeJSON(resp, json(
                    "flag", "error",
                    "message", "Unknown error occurred."));
        }
        return;
    }

    /**
     * Update the Project Needs Attention flag if any of the values for IRB Uploaded
     * for the datasets in that project are or are not TBD.
     * @param logger debug logger
     * @param projectItem the given project
     */
    public static void updateNeedsAttentionIrbUploadedChanged(
            Logger logger, ProjectItem projectItem, UserItem userItem) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        // Currently YES, change to No if shareability determined and no datasets are TBD
        if (projectItem.getNeedsAttention()) {
            if (projectItem.isShareableStatusDetermined()) {
                boolean changeNeedsAttention = true;
                // Get all the datasets
                Collection<DatasetItem> datasetList = datasetDao.findByProject(projectItem, true);
                for (DatasetItem datasetItem : datasetList) {
                    if (datasetItem.getIrbUploaded().equals(DatasetItem.IRB_UPLOADED_TBD)) {
                        changeNeedsAttention = false;
                        break;
                    }
                }

                String logMsg = "User " + userItem.getId() + " changed IRB Uploaded flag(s). ";
                logMsg += "Project ";
                logMsg += projectItem.getProjectName() + " (" + projectItem.getId() + "). ";

                if (changeNeedsAttention) {
                    projectItem.setNeedsAttention(false);
                    projectDao.saveOrUpdate(projectItem);

                    logMsg += "Changed Needs Attention:  No.";
                    logger.info(logMsg);
                } else {
                    logMsg += "Did not change Needs Attention: ";
                    if (projectItem.getNeedsAttention()) {
                        logMsg += "Yes.";
                    } else {
                        logMsg += "No.";
                    }
                    logger.info(logMsg);
                }
            }
        // Currently NO, change to Yes if any datasets are TBD
        } else {
            boolean changeNeedsAttention = false;
            // Get all the datasets
            Collection<DatasetItem> datasetList = datasetDao.findByProject(projectItem, true);
            for (DatasetItem datasetItem : datasetList) {
                if (datasetItem.getIrbUploaded().equals(DatasetItem.IRB_UPLOADED_TBD)) {
                    changeNeedsAttention = true;
                    break;
                }
            }
            if (changeNeedsAttention) {
                projectItem.setNeedsAttention(true);
                projectDao.saveOrUpdate(projectItem);
                logger.info("Changing Needs Attention to Yes "
                        + "because not all the datasets have IRB Uploaded as not TBD");
            } else {
                logger.debug("NOT changing Needs Attention to Yes "
                        + "because not all the datasets have IRB Uploaded as not TBD");
            }
        }
    } // end updateNeedsAttentionIrbUploadedChanged

    /**
     * If appropriate, update the Project 'Needs Attention' flag following
     * a change of the 'Appears Anonymous' value for one or more datasets.
     * @param projectItem the given project
     * @param userItem the user that made the change
     */
    public static void updateNeedsAttentionAppearsAnonChanged(ProjectItem projectItem,
                                                              UserItem userItem) {
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();

        // Currently NO, change to YES if shareability determined and any dataset
        // has an 'Appears Anonymous' value that isn't Yes or N/A.
        if (!projectItem.getNeedsAttention()) {
            if (projectItem.isShareableStatusDetermined()) {
                boolean changeNeedsAttention = false;
                // Get all the datasets
                Collection<DatasetItem> datasetList = datasetDao.findByProject(projectItem, true);
                for (DatasetItem datasetItem : datasetList) {
                    String appearsAnon = datasetItem.getAppearsAnonymous();
                    if (!appearsAnon.equals(DatasetItem.APPEARS_ANON_NA)
                        && !appearsAnon.equals(DatasetItem.APPEARS_ANON_YES)) {
                        changeNeedsAttention = true;
                        break;
                    }
                }

                if (changeNeedsAttention) {
                    projectItem.setNeedsAttention(true);
                    projectDao.saveOrUpdate(projectItem);
                }
            }
            // Currently YES, change to NO if shareability has been determined
            // and all datasets have 'Appears Anonymous' values of Yes or N/A.
        } else {
            if (projectItem.isShareableStatusDetermined()) {
                boolean changeNeedsAttention = true;
                // Get all the datasets
                Collection<DatasetItem> datasetList = datasetDao.findByProject(projectItem, true);
                for (DatasetItem datasetItem : datasetList) {
                    String appearsAnon = datasetItem.getAppearsAnonymous();
                    if (!appearsAnon.equals(DatasetItem.APPEARS_ANON_NA)
                        && !appearsAnon.equals(DatasetItem.APPEARS_ANON_YES)) {
                        changeNeedsAttention = false;
                        break;
                    }
                }

                if (changeNeedsAttention) {
                    // All datasets have Yes or N/A values for Appears Anonymous...
                    projectItem.setNeedsAttention(false);
                    projectDao.saveOrUpdate(projectItem);
                }
            }
        }
    }

}
