/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.irb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.IrbDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.IrbItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.ProjectServlet;
import edu.cmu.pslc.datashop.servlet.ServletDateUtil;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageHelper;
import edu.cmu.pslc.datashop.servlet.project.ProjectPageServlet;

/**
 * This servlet is for displaying a Project's IRB.
 *
 * @author Cindy Tipper
 * @version $Revision: 13027 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-28 12:32:22 -0400 (Mon, 28 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IrbServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for this servlet. */
    public static final String JSP_NAME = "/jsp_project/project_irb.jsp";
    /** The JSP name for the project_page servlet. */
    public static final String PROJECT_PAGE_JSP_NAME = "/jsp_project/project_page.jsp";

    /** Constant for the servlet. */
    public static final String SERVLET = "ProjectIRB";
    /** Constant for the servlet. */
    public static final String IRB_REVIEW_SERVLET = "IRBReview";

    /** Title for this page. */
    public static final String SERVLET_TITLE = "Project IRB";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ProjectIRB";

    /** Constant for the request attribute. */
    public static final String IRB_PROJECT_ATTR = "irbProjectDto_";
    /** Constant for the IRB id session attribute. */
    public static final String EDIT_IRB_ID_ATTR = "editIRBId_";

    /** Constant for the location of the IRB files. */
    private static final String IRB_SUB_PATH = "irbs";

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

        httpSession.setAttribute(ProjectPageServlet.PROJECT_TAB_ATTRIB, "irb");

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        try {

            String jspName = JSP_NAME;
            Integer projectId = getIntegerId((String)req.getParameter("id"));
            if (projectId != null) {
                httpSession.setAttribute(ProjectPageServlet.PROJECT_ID_ATTRIB, projectId);
            }

            // If user not logged in, go to main project page.
            UserItem userItem = getLoggedInUserItem(req);
            if ((userItem == null) || !isUserAuthorized(req)) {
                RequestDispatcher disp =
                    getServletContext().getRequestDispatcher(PROJECT_PAGE_JSP_NAME);
                disp.forward(req, resp);
                return;
            }

            if (ServletFileUpload.isMultipartContent(req)) {
                addFile(req, resp, userItem);
                return;
            }

            if (req.getParameter("editDataCollectionType") != null) {
                editDataCollectionType(req, resp, projectId, userItem);
            } else if (req.getParameter("editSubjectTo") != null) {
                editSubjectToDataShopIRB(req, resp, projectId, userItem);
            } else if (req.getParameter("editShareStatus") != null) {
                editShareabilityReviewStatus(req, resp, projectId, userItem);
            } else if (req.getParameter("editNeedsAttn") != null) {
                editNeedsAttn(req, resp, projectId, userItem);
            } else if (req.getParameter("editRMNotes") != null) {
                editResearchManagersNotes(req, resp, projectId, userItem);
            } else if (req.getParameter("submitForReview") != null) {
                submitForReview(req, resp, projectId, userItem);
            } else if (req.getParameter("addIrbWithoutProject") != null) {
                addIrbWithoutProject(req, resp, userItem);
            } else if (req.getParameter("addIRB") != null) {
                addIRB(req, resp, projectId, userItem);
            } else if (req.getParameter("removeIRB") != null) {
                removeIRB(req, resp, projectId, userItem);
            } else if (req.getParameter("downloadFile") != null) {
                downloadFile(req, resp, projectId, userItem);
            } else if (req.getParameter("deleteFile") != null) {
                deleteFile(req, resp, projectId, userItem);
            } else if (req.getParameter("getIRB") != null) {
                getIRB(req, resp);
            } else if (req.getParameter("getIRBList") != null) {
                getIRBList(req, resp, userItem);
            } else if (req.getParameter("editProtocolNumber") != null) {
                editProtocolNumber(req, resp, userItem);
            } else if (req.getParameter("editPI") != null) {
                editPI(req, resp, userItem);
            } else if (req.getParameter("editApprovalDate") != null) {
                editApprovalDate(req, resp, userItem);
            } else if (req.getParameter("editExpirationDate") != null) {
                editExpirationDate(req, resp, userItem);
            } else if (req.getParameter("editGrantingInstitution") != null) {
                editGrantingInstitution(req, resp, userItem);
            } else if (req.getParameter("editNotes") != null) {
                editNotes(req, resp, userItem);
            } else if (req.getParameter("deleteIRB") != null) {
                deleteIRB(req, resp, userItem);
            } else if (projectId != null) {
                displayProjectIrbPage(req, resp, projectId, userItem);
            } else {
                logger.info("Project id is null, going to home page.");
                jspName = ProjectServlet.SERVLET_NAME;

                logger.info("Going to JSP: " + jspName);
                RequestDispatcher disp = getServletContext().getRequestDispatcher(jspName);
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

        Integer projectId = getIntegerId((String)req.getParameter("id"));
        if (projectId == null) {
            return false;
        }

        String authLevel = projectPageHelper.getAuthLevel((String)user.getId(), projectId);
        if ((authLevel != null) && (authLevel.equals(AuthorizationItem.LEVEL_ADMIN))) {
            return true;
        }

        return false;
    }

    /**
     * Helper method for persisting changes to the Data Collection Type.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void editDataCollectionType(HttpServletRequest req, HttpServletResponse resp,
                                        Integer projectId, UserItem userItem)
        throws IOException, ServletException {

        String valueStr = (String) req.getParameter("value");
        logDebug("editDataCollectionType: new value = ", valueStr);

        if (projectId != null) {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            String origDCType = projectItem.getDataCollectionType();

            projectItem.setDataCollectionType(valueStr);
            projectItem.setUpdatedBy(userItem);
            projectItem.setUpdatedTime(new Date());
            projectDao.saveOrUpdate(projectItem);

            updateUserLog(userItem, projectItem, "Data Collection Type", origDCType, valueStr);

            String message = "Data Collection Type updated.";
            String messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

            redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
        }
    }

    /**
     * Helper method for persisting changes to the Subject To DataShop IRB attribute.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void editSubjectToDataShopIRB(HttpServletRequest req, HttpServletResponse resp,
                                          Integer projectId, UserItem userItem)
        throws IOException, ServletException {

        String valueStr = (String) req.getParameter("value");
        logDebug("editSubjectToDataShopIRB: new value = ", valueStr);

        if (projectId != null) {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            String origSubjTo = projectItem.getSubjectToDsIrb();

            projectItem.setSubjectToDsIrb(valueStr);
            projectItem.setUpdatedBy(userItem);
            projectItem.setUpdatedTime(new Date());
            projectDao.saveOrUpdate(projectItem);

            updateUserLog(userItem, projectItem,
                          "Subject To DataShop IRB", origSubjTo, valueStr);

            String message = "Subject to DataShop IRB updated.";
            String messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

            redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
        }
    }

    /**
     * Helper method for persisting changes to the Shareability Review Status attribute.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void editShareabilityReviewStatus(HttpServletRequest req, HttpServletResponse resp,
                                              Integer projectId, UserItem userItem)
        throws IOException, ServletException {

        String newSRS = (String) req.getParameter("value");
        logDebug("editShareabilityReviewStatus: new value = ", newSRS);

        if (projectId != null) {
            ProjectItem projectItem = getProjectItem(projectId);
            IrbProjectDto irbProjectDto;

            String oldSRS = projectItem.getShareableStatus();
            if (!oldSRS.equals(newSRS)) {
                IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                irbProjectDto = irbHelper.
                        updateShareabilityStatus(newSRS, userItem, projectId, getBaseUrl(req));

                updateUserLog(userItem, projectItem,
                        "Shareability Review Status", oldSRS, newSRS);
            } else {
                irbProjectDto = getIrbProjectDto(projectItem, userItem);
                irbProjectDto.setMessage("Shareability Review Status did not change.");
                irbProjectDto.setMessageLevel(IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS);
            }

            redirectToProjectIrbPage(projectId, irbProjectDto, req, resp);
        }
    }

    /**
     * Helper method for persisting changes to the Needs Attention attribute.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void editNeedsAttn(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem) throws IOException, ServletException {
        String valueStr = (String) req.getParameter("value");
        logDebug("editNeedsAttn: new value = ", valueStr);

        if (projectId != null) {
            ProjectItem projectItem = getProjectItem(projectId);
            String msg = "";

            Boolean oldFlag = projectItem.getNeedsAttention();
            Boolean newFlag = ProjectItem.getNeedsAttention(valueStr);
            if (oldFlag != newFlag) {
                projectItem.setNeedsAttention(newFlag);
                projectItem.setUpdatedBy(userItem);
                projectItem.setUpdatedTime(new Date());
                ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
                projectDao.saveOrUpdate(projectItem);

                String oldStr = "No";
                if (oldFlag) {
                    oldStr = "Yes";
                }

                updateUserLog(userItem, projectItem,
                        "Needs Attention", oldStr, valueStr);

                String logMsg = "User " + userItem.getId();
                logMsg += " changed Needs Attention for project ";
                logMsg += projectItem.getProjectName() + " (" + projectItem.getId() + ") to ";
                logMsg += "Needs Attention: " + valueStr;
                logger.info(logMsg);

                msg = "Needs Attention updated.";
            } else {
                msg = "Needs Attention is unchanged.";
            }

            IrbProjectDto irbProjectDto = getIrbProjectDto(projectItem, userItem);
            irbProjectDto.setMessage(msg);
            irbProjectDto.setMessageLevel(IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS);

            redirectToProjectIrbPage(projectId, irbProjectDto, req, resp);
        }
    }

    /**
     * Helper method for persisting changes to the Research Manager's Notes.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws JSONException a JSON Exception
     */
    private void editResearchManagersNotes(HttpServletRequest req, HttpServletResponse resp,
                                           Integer projectId, UserItem userItem)
        throws IOException, JSONException {

        String valueStr = (String) req.getParameter("value");
        logDebug("editResearchManagersNotes");

        String message;
        String messageLevel;

        if (projectId == null) {
            message = "Project id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
            ProjectItem projectItem = projectDao.get(projectId);
            String origRMNotes = projectItem.getShareableStatus();

            projectItem.setResearchManagersNotes(valueStr);
            projectItem.setUpdatedBy(userItem);
            projectItem.setUpdatedTime(new Date());
            projectDao.saveOrUpdate(projectItem);

            updateUserLog(userItem, projectItem,
                          "Research Manager's Notes", origRMNotes, valueStr);

            message = "Research Manager's Notes updated.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

            req.setAttribute(IRB_PROJECT_ATTR + projectId,
                             getIrbProjectDto(getProjectItem(projectId), userItem));
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for submitting the specified project for review.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO Exception
     * @throws ServletException an exception creating the servlet
     */
    private void submitForReview(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem)
            throws IOException, ServletException {

        ProjectItem projectItem = getProjectItem(projectId);

        String message;
        String messageLevel;

        if (projectItem == null) {
            message = "Project id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
            irbHelper.submitProjectForReview(projectItem, userItem, getBaseUrl(req));

            message = "The project has been submitted for review.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        String logInfoStr = getProjectLogInfoStr(projectItem);
        UserLogger.log(userItem, UserLogger.SUBMIT_PROJECT_FOR_REVIEW, logInfoStr, true);

        redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
    }

    /**
     * Get the specified IRB.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void getIRB(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, JSONException {

        IrbItem item = getIrbItem(req);

        // get IRB items
        IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
        IrbDto irbDto = irbHelper.getIrbDto(item);

        // Get list of relevant files.
        List<IrbFileDto> fileList = irbDto.getFileList();
        JSONArray fileListJson = new JSONArray();
        for (IrbFileDto dto : fileList) {
            JSONObject jsonFile = new JSONObject();
            jsonFile.put("fileName", dto.getFileName());
            jsonFile.put("fileId", dto.getFileId());
            jsonFile.put("fileOwner", dto.getFileOwnerString());
            jsonFile.put("addedTime", dto.getAddedTimeString());
            fileListJson.put(jsonFile);
        }

        // Get list of relevant projects.
        List<ProjectItem> projects = irbHelper.getProjectsByIRB(item);
        Map<String, Integer> projectMap = new HashMap<String, Integer>();
        for (ProjectItem p : projects) {
            projectMap.put(p.getProjectName(), (Integer)p.getId());
        }

        // Project list as a JSON object
        JSONObject projectListJson = new JSONObject(projectMap);

        String expDate = IrbItem.NOT_APPLICABLE;
        if (!irbDto.getExpirationDateNaFlag()) {
            expDate = irbDto.getExpirationDateString();
        }

        // write JSON response
        writeJSON(resp, json("irbId", (Integer)item.getId(),
                             "title", irbDto.getTitle(),
                             "protocolNumber", irbDto.getProtocolNumber(),
                             "pi", irbDto.getPiName(),
                             "addedBy", (String)irbDto.getAddedBy().getId(),
                             "approvalDate", irbDto.getApprovalDateString(),
                             "expirationDate", expDate,
                             "grantingInstitution", irbDto.getGrantingInstitutionString(),
                             "notes", irbDto.getNotes(),
                             "numFiles", irbDto.getNumFiles(),
                             "files", fileListJson,
                             "projects", projectListJson,
                             "numProjects", irbDto.getNumProjects()));
    }

    /**
     * Get the list of IRBs added by this owner and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void getIRBList(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws IOException, JSONException {

        String searchBy = getParameter(req, "searchBy");
        if (searchBy == null) {
            searchBy = "";
        } else {
            IrbContext context = IrbContext.getContext(req);
            context.setAddIRBSearchBy(searchBy);
            IrbContext.setContext(req, context);
        }

        // get IRB items
        IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
        List<IrbDto> irbList = irbHelper.getAllIRBsByUser(userItem, searchBy);

        Map<Integer, String> irbMap = new HashMap<Integer, String>();
        for (IrbDto irb : irbList) {
            irbMap.put(irb.getId(), irb.getTitle());
        }

        // As JSON object
        JSONObject irbListJson = new JSONObject(irbMap);

        // write JSON response
        writeJSON(resp, json("irbList", irbListJson));
    }

    /**
     * Helper method for adding an IRB, either a new one or choosing an existing one.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void addIRB(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem)
        throws IOException, ServletException {

        String addIRBType = getParameter(req, "addIRBType");

        IrbProjectDto irbProjectDto = null;
        if (addIRBType.equals("addNew")) {
            irbProjectDto = addNewIRB(req, projectId, userItem);
        } else {
            irbProjectDto = chooseExistingIRB(req, projectId, userItem);
        }

        redirectToProjectIrbPage(projectId, irbProjectDto, req, resp);
    }

    /**
     * Helper method for adding an IRB without a project.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void addIrbWithoutProject(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {

        addNewIRB(req, null, userItem);

        String message = "The IRB has been successfully added.";
        String messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

        redirectToIrbReviewPage(message, messageLevel, req, resp);
    }

    /**
     * Helper method for removing an IRB from the specified project.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void removeIRB(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem)
            throws IOException, ServletException {
        IrbItem item = getIrbItem(req);
        ProjectItem projectItem = getProjectItem(projectId);

        String message;
        String messageLevel;

        if (item == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (projectItem == null) {
            message = "Project id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
            irbHelper.removeIRBFromProject(item, projectItem, userItem);

            message = "The IRB has been successfully removed from the project.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        String logInfoStr = getProjectLogInfoStr(projectItem);
        logInfoStr += ": Removed IRB from project, '" + item.getTitle() + "'";
        UserLogger.log(userItem, UserLogger.MODIFY_PROJECT_IRB, logInfoStr, true);

        redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
    }

    /**
     * Helper method for editing the IRB protocol number.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editProtocolNumber(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if ((valueStr.length() == 0) || (valueStr.length() > MAX_PN_STR_LEN)) {
            message = "Protocol Number must be no more than 50 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            String origPNValue = irbItem.getProtocolNumber();

            irbItem.setProtocolNumber(valueStr);
            irbItem.setUpdatedBy(userItem);
            irbItem.setUpdatedTime(new Date());
            irbDao.saveOrUpdate(irbItem);

            updateUserLog(userItem, irbItem, "IRB Protocol Number", origPNValue, valueStr);

            message = "Protocol Number updated.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for editing the IRB PI.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editPI(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if ((valueStr.length() == 0) || (valueStr.length() > MAX_STR_LEN)) {
            message = "PI name must be no more than 255 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            String origPIValue = irbItem.getPi();

            irbItem.setPi(valueStr);
            irbItem.setUpdatedBy(userItem);
            irbItem.setUpdatedTime(new Date());
            irbDao.saveOrUpdate(irbItem);

            updateUserLog(userItem, irbItem, "IRB PI", origPIValue, valueStr);

            message = "PI Name updated.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for editing the IRB Approval Date.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editApprovalDate(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            String origValue = ServletDateUtil.getDateString(irbItem.getApprovalDate());

            Date theDate = ServletDateUtil.getDateFromString(valueStr);
            if (theDate != null) {
                irbItem.setApprovalDate(theDate);
                irbItem.setUpdatedBy(userItem);
                irbItem.setUpdatedTime(new Date());
                irbDao.saveOrUpdate(irbItem);

                valueStr = ServletDateUtil.getDateString(irbItem.getApprovalDate());
                updateUserLog(userItem, irbItem, "IRB Approval Date", origValue, valueStr);

                message = "Approval Date updated.";
                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            } else {
                message = "Invalid date format specified.";
                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for editing the IRB Expiration Date.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editExpirationDate(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");
        String naFlag = (String) req.getParameter("naFlag");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            Boolean origFlag = irbItem.getExpirationDateNa();
            String origValue = ServletDateUtil.getDateString(irbItem.getExpirationDate());

            if (naFlag.equals("true")) {
                if (origFlag) {
                    valueStr = IrbItem.NOT_APPLICABLE;
                    message = "No change made.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                } else {
                    irbItem.setExpirationDateNa(true);
                    irbItem.setExpirationDate(null);
                    irbItem.setUpdatedBy(userItem);
                    irbItem.setUpdatedTime(new Date());
                    irbDao.saveOrUpdate(irbItem);

                    valueStr = IrbItem.NOT_APPLICABLE;
                    updateUserLog(userItem, irbItem, "IRB Expiration Date", origValue, valueStr);

                    message = "Expiration Date cleared.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                }

            } else {
                Date theDate = ServletDateUtil.getDateFromString(valueStr);
                if (theDate != null) {
                    if (origValue.equals(valueStr)) {
                        message = "No change made.";
                        messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                    } else {
                        irbItem.setExpirationDateNa(false);
                        irbItem.setExpirationDate(theDate);
                        irbItem.setUpdatedBy(userItem);
                        irbItem.setUpdatedTime(new Date());
                        irbDao.saveOrUpdate(irbItem);

                        valueStr = ServletDateUtil.getDateString(irbItem.getExpirationDate());
                        updateUserLog(userItem, irbItem, "IRB Expiration Date",
                                origValue, valueStr);

                        message = "Expiration Date updated.";
                        messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                    }
                } else if (valueStr.equals("")) {
                    // Expiration date can be removed.
                    irbItem.setExpirationDateNa(true);
                    irbItem.setExpirationDate(null);
                    irbItem.setUpdatedBy(userItem);
                    irbItem.setUpdatedTime(new Date());
                    irbDao.saveOrUpdate(irbItem);

                    valueStr = ServletDateUtil.getDateString(irbItem.getExpirationDate());
                    updateUserLog(userItem, irbItem, "IRB Expiration Date", origValue, valueStr);

                    message = "Expiration Date updated.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                } else {
                    message = "Invalid date format specified.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
                }
            }
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for editing the IRB Granting Institution.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editGrantingInstitution(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if ((valueStr.length() == 0)
                || (valueStr.length() > MAX_STR_LEN)) {
            message = "Granting Institution must be no more than 255 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            String origGIValue = irbItem.getGrantingInstitution();

            irbItem.setGrantingInstitution(valueStr);
            irbItem.setUpdatedBy(userItem);
            irbItem.setUpdatedTime(new Date());
            irbDao.saveOrUpdate(irbItem);

            updateUserLog(userItem, irbItem, "IRB Granting Institution", origGIValue, valueStr);

            message = "Granting Institution updated.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for editing the IRB Notes.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON Exception
     */
    private void editNotes(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws IOException, JSONException {

        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        String valueStr = (String) req.getParameter("value");

        String message;
        String messageLevel;

        if (irbId == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            IrbItem irbItem = irbDao.get(irbId);
            String origNotesValue = irbItem.getNotes();
            if (origNotesValue == null) { origNotesValue = ""; }

            irbItem.setNotes(valueStr);
            irbItem.setUpdatedBy(userItem);
            irbItem.setUpdatedTime(new Date());
            irbDao.saveOrUpdate(irbItem);

            updateUserLog(userItem, irbItem, "Notes", origNotesValue, valueStr);

            message = "Notes updated.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // write JSON response
        writeJSON(resp, json("message", message, "level", messageLevel, "value", valueStr));
    }

    /**
     * Helper method for deleting an IRB.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void deleteIRB(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws IOException, ServletException {

        IrbItem item = getIrbItem(req);

        String message;
        String messageLevel;

        if (item == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
            irbHelper.deleteIRB(item, userItem, getBaseDir());

            message = "The IRB has been successfully deleted.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

            String logInfoStr = getIrbLogInfoStr(item);
            UserLogger.log(userItem, UserLogger.DELETE_IRB, logInfoStr, false);
        }

        // Determine where request originated from.
        Integer projectId = getIntegerId((String)req.getParameter("id"));
        if (projectId == null) {
            redirectToIrbReviewPage(message, messageLevel, req, resp);
        } else {
            redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
        }
    }

    /**
     * Helper method for deleting an IRB file.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param projectId the Project id, if appropriate
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void deleteFile(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem)
            throws IOException, ServletException {
        IrbItem item = getIrbItem(req);
        FileItem fileItem = getFileItem(req);

        String message;
        String messageLevel;

        if (item == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            String fileName = fileItem.getFileName();
            String wholePath = getBaseDir() + File.separator + fileItem.getFilePath();
            Integer fileId = (Integer)fileItem.getId();
            File theFile = new File(wholePath, fileName);

            String info = "IRB '" + item.getTitle() + "', " + "File '"
                + fileItem.getFileName() + "'" + " [" + fileId + "]";
            String extraInfo = info + " :: " + theFile.getAbsoluteFile();

            if (theFile.exists()) {
                if (theFile.delete()) {
                    IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                    irbHelper.deleteFileFromIRB(item, fileItem, userItem);

                    message = "The IRB file has been successfully deleted.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

                    String logInfoStr = getIrbLogInfoStr(item);
                    logInfoStr += ": Removed file " + fileItem.getFileName()
                        + " (" + fileItem.getId() + ")";
                    UserLogger.log(userItem, UserLogger.REMOVE_IRB_FILE, logInfoStr, false);
                } else {
                    logger.error("Unable to deleteFile: " + extraInfo);
                    message = "Unable to delete the file.";
                    messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
                }
            } else {
                logger.error("Attempting to delete a files that does not exist: " + extraInfo);
                message = "Attempt to delete a file that does not exist.";
                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        }

        // 'Edit IRB' was open in order to delete file.
        Integer irbId = getIntegerId((String)req.getParameter("irbId"));
        req.getSession().setAttribute(EDIT_IRB_ID_ATTR, irbId);

        // If file deleted via 'All IRBs' page, projectId is bogus
        if (projectId != null) {
            redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
        } else {
            redirectToIrbReviewPage(message, messageLevel, req, resp);
        }
    }

    /** Constant for the max string length for most attributes. */
    private static final int MAX_STR_LEN = 255;
    /** Constant for the max string length for Protocol Number. */
    private static final int MAX_PN_STR_LEN = 50;

    /**
     * Helper method for adding a new IRB.
     * @param req HttpServletRequest
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @return IrbProjectDto updated IRB Project DTO
     */
    private IrbProjectDto addNewIRB(HttpServletRequest req, Integer projectId, UserItem userItem) {
        String protocolNumber = getParameter(req, "protocolNumberField");
        String title = getParameter(req, "titleField");
        String piName = getParameter(req, "piNameField");
        String approvalDate = getParameter(req, "approvalDateField");
        String expirationDate = getParameter(req, "expirationDateField");
        String expirationDateNa = getParameter(req, "editDialogNaCheckbox");
        String grantingInstitution = getParameter(req, "grantingInstitutionField");

        boolean expDateValid = false;
        boolean expDateNaFlag = false;
        if (expirationDateNa != null && expirationDateNa.equals("true")) {
            expDateNaFlag = true;
        }

        if ((expirationDate != null) && (!expirationDate.equals(""))
                &&
            (ServletDateUtil.getDateFromString(expirationDate) != null)) {
            expDateValid = true;
        }

        ProjectItem projectItem = getProjectItem(projectId);

        String message;
        String messageLevel;

        if (!validateInput(title, MAX_STR_LEN)) {
            message = "IRB must have a title that is no more than 255 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!validateInput(protocolNumber, MAX_PN_STR_LEN)) {
            message = "IRB must have a protocol number that is no more than 50 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!validateInput(piName, MAX_STR_LEN)) {
            message = "IRB must have a PI that is no more than 255 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if ((approvalDate == null) || (approvalDate.equals(""))) {
            message = "IRB must have an approval date.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (ServletDateUtil.getDateFromString(approvalDate) == null) {
            message = "Invalid format for Approval Date.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!expDateNaFlag && !expDateValid) {
            message = "Invalid format for Expiration Date.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (!validateInput(grantingInstitution, MAX_STR_LEN)) {
            message = "IRB must have a granting institution that is no more than 255 characters.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        //} else if (projectItem == null) {
            //message = "Project id must be specified.";
            //messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {
            IrbItem irbItem = new IrbItem();
            irbItem.setTitle(title);
            irbItem.setProtocolNumber(protocolNumber);
            irbItem.setPi(piName);
            irbItem.setApprovalDate(ServletDateUtil.getDateFromString(approvalDate));

            irbItem.setExpirationDateNa(expDateNaFlag);
            if (!expDateNaFlag && expDateValid) {
                irbItem.setExpirationDate(ServletDateUtil.getDateFromString(expirationDate));
            }

            irbItem.setGrantingInstitution(grantingInstitution);
            irbItem.setAddedTime(new Date());
            irbItem.setAddedBy(userItem);
            irbItem.setUpdatedTime(irbItem.getAddedTime());
            irbItem.setUpdatedBy(irbItem.getAddedBy());

            IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
            irbDao.saveOrUpdate(irbItem);

            if (projectItem != null) {
                IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                irbHelper.addIRBToProject(irbItem, projectItem, userItem);

                String logInfoStr = getIrbLogInfoStr(irbItem);
                UserLogger.log(userItem, UserLogger.CREATE_IRB, logInfoStr, false);

                irbHelper.sendIrbAddedEmail(userItem, projectItem, getBaseUrl(req), irbItem);
            }
            message = "The IRB has been successfully added.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        if (projectItem != null) {
            IrbProjectDto irbProjectDto = getIrbProjectDto(projectItem, userItem);
            irbProjectDto.setMessage(message);
            irbProjectDto.setMessageLevel(messageLevel);
            return irbProjectDto;
        }
        return null;
    }

    /**
     * Helper method for choosing an existing IRB.
     * @param req HttpServletRequest
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @return IrbProjectDto updated IRB Project DTO
     */
    private IrbProjectDto chooseExistingIRB(HttpServletRequest req,
            Integer projectId, UserItem userItem) {

        IrbItem irbItem = getIrbItem(req);
        ProjectItem projectItem = getProjectItem(projectId);

        String message;
        String messageLevel;

        if (irbItem == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else if (projectItem == null) {
            message = "Project id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
        } else {

            IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
            irbHelper.addIRBToProject(irbItem, projectItem, userItem);

            message = "The IRB has been successfully added to the project.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

            String logInfoStr = getProjectLogInfoStr(projectItem);
            logInfoStr += ": Added an IRB to project, '" + irbItem.getTitle() + "'";
            UserLogger.log(userItem, UserLogger.MODIFY_PROJECT_IRB, logInfoStr, true);

            irbHelper.sendIrbAddedEmail(userItem, projectItem, getBaseUrl(req), irbItem);
        }

        IrbProjectDto irbProjectDto = getIrbProjectDto(projectItem, userItem);
        irbProjectDto.setMessage(message);
        irbProjectDto.setMessageLevel(messageLevel);

        return irbProjectDto;
    }

    /**
     * Handle request to download a file for a given IRB.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param projectId the relevant project id
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void downloadFile(HttpServletRequest req, HttpServletResponse resp,
            Integer projectId, UserItem userItem)
            throws IOException, ServletException {
        IrbItem irbItem = getIrbItem(req);
        FileItem fileItem = getFileItem(req);

        String message = null;
        String messageLevel = null;

        boolean successFlag = false;

        if (irbItem == null) {
            message = "IRB id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            successFlag = false;
        } else if (fileItem == null) {
            message = "File id must be specified.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            successFlag = false;
        } else {
            try {
                Integer fileId = (Integer)fileItem.getId();
                String fileName = fileItem.getFileName();
                String actualFileName = fileItem.getUrl(getBaseDir());

                String info = "IRB '" + irbItem.getTitle() + "', "
                    + "File '" + fileItem.getFileName() + "'"
                    + " [" + fileId + "]";

                if (logger.isDebugEnabled()) {
                    logger.debug("downloadFile: " + info);
                }

                resp.setContentType("application/x-download");
                resp.setHeader("Content-Disposition",
                               "attachment; filename=\"" + fileName + "\"");

                BufferedInputStream inStream = null;
                OutputStream outStream = resp.getOutputStream();
                try {
                    inStream = new BufferedInputStream(new FileInputStream(actualFileName));

                    int ch;
                    while ((ch = inStream.read()) != -1) {
                        outStream.write(ch);
                    }

                    UserLogger.log(userItem, UserLogger.DOWNLOAD_IRB_FILE, info, false);

                } catch (FileNotFoundException exception) {
                    logger.error("downloadFile: FileNotFoundException occurred: "
                                 + info + " :: " + actualFileName,  exception);
                } finally {
                    // very important
                    if (inStream != null) { inStream.close(); }
                    if (outStream != null) { outStream.close(); }
                }

            } catch (Exception exception) {
                logger.error("downloadFile: Exception occurred.", exception);
            }
            message = "File successfully downloaded.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            successFlag = true;
        }

        if (!successFlag) {
            if (projectId != null) {
                redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
            } else {
                redirectToIrbReviewPage(message, messageLevel, req, resp);
            }
        }
    }

    /**
     * Handle request to add a file to a IRB.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void addFile(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws IOException, ServletException {
        IrbItem irbItem = getIrbItem(req);
        String isEdit = (String)req.getParameter("isEdit");
        Integer projectId = getIntegerId((String)req.getParameter("id"));

        String message = null;
        String messageLevel = null;

        if (irbItem != null && ServletFileUpload.isMultipartContent(req)) {
            List <org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
            if (items == null) {
                message = "File size limit (400MB) was exceeded.";
                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_WARN;
            } else {
                IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
                for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {
                    if (!uploadFileItem.isFormField()) {
                        try {
                            Integer irbId = (Integer)irbItem.getId();
                            FileItem dsFileItem = createFile(uploadFileItem, userItem,
                                                             getBaseDir(), getSubPath(irbId));
                            if (dsFileItem != null) {
                                irbHelper.addFileToIRB(dsFileItem, irbItem, userItem);
                                message = "The file has been added successfully.";
                                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_SUCCESS;

                                String logInfoStr = getIrbLogInfoStr(irbItem);
                                logInfoStr += ": Added file " + dsFileItem.getFileName()
                                    + " (" + dsFileItem.getId() + ")";
                                UserLogger.log(userItem, UserLogger.ADD_IRB_FILE,
                                               logInfoStr, false);

                            } else {
                                message = "The file was not added.";
                                messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_WARN;
                            }
                        } catch (Exception exception) {
                            message = "Something went wrong adding the file.";
                            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
                            logger.error("addFile: Exception occurred.", exception);
                        }
                    }
                } // end for loop
            }
        } else {
            message = "Something went wrong.";
            messageLevel = IrbProjectDto.STATUS_MESSAGE_LEVEL_ERROR;
            logger.error("addFile: Request is not a multi-part.");
        }

        // If the 'Edit IRB' dialog was open when file was added, make note.
        if (Boolean.parseBoolean(isEdit)) {
            Integer irbId = getIntegerId((String)req.getParameter("irbId"));
            req.getSession().setAttribute(EDIT_IRB_ID_ATTR, irbId);
        }

        if (projectId != null) {
            redirectToProjectIrbPage(projectId, message, messageLevel, req, resp, userItem);
        } else {
            redirectToIrbReviewPage(message, messageLevel, req, resp);
        }
    }

    /**
     * Get the sub path for IRB files.
     * @param irbId the id of the IRB item
     * @return the part of the path between the base directory and the file name
     */
    private String getSubPath(Integer irbId) {
        return IRB_SUB_PATH + File.separator + "irb_" + irbId;
    }

    /**
     * Helper function to handle file creation for handleUpload.
     * @param uploadFileItem the FileItem to parse
     * @param owner the userItem for the owner
     * @param baseDir for all files in DataShop
     * @param subPath the path under the base directory where this tools files are kept
     * @return the DataShop file item just created
     * @throws Exception a FileUploadException or...
     */

    private FileItem createFile(org.apache.commons.fileupload.FileItem uploadFileItem,
            UserItem owner, String baseDir, String subPath) throws Exception {
        Boolean successFlag = false;
        FileItem dsFileItem = new FileItem();

        String fieldName = uploadFileItem.getFieldName();
        String fileFullName = uploadFileItem.getName();

        if (fileFullName.indexOf('\\') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('\\') + 1);
        }
        if (fileFullName.indexOf('/') >= 0) {
            fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
        }
        String fileType = uploadFileItem.getContentType();
        long sizeInBytes = uploadFileItem.getSize();

        if (fieldName.equals("fileName")) {
            dsFileItem.setFilePath(subPath);
            dsFileItem.setAddedTime(new Date());
            if (fileType == null) {
                fileType = "";
            }
            dsFileItem.setFileType(fileType);
            dsFileItem.setOwner(owner);
            dsFileItem.setFileSize(sizeInBytes);

            if (logger.isDebugEnabled()) {
                logger.debug("createFile: fileItem: "  + dsFileItem);
            }

            //Check to make sure the user has selected a file.
            if (fileFullName != null && fileFullName.length() > 0) {
                // Create the directory
                String wholePath = baseDir + File.separator + subPath;
                File newDirectory = new File(wholePath);
                if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("createFile: The directory has been created."
                            + newDirectory.getAbsolutePath());
                    }

                    //check that file with this name does not already exist.
                    //if it does start incrementing adding _1, _2, to the end
                    //of the file until a name is found that doesn't exist.
                    List<String> fileList = Arrays.asList(newDirectory.list());
                    int extensionIndex = fileFullName.lastIndexOf(".");
                    String fileExt = "";
                    String fileName = "";
                    if (extensionIndex < 0) {
                        fileName = fileFullName;
                    } else if (extensionIndex == 0) {
                        fileExt = fileFullName.substring(0, fileFullName.length());
                    } else {
                        fileExt = fileFullName.substring(extensionIndex);
                        fileName = fileFullName.substring(0, extensionIndex);
                    }
                    int i = 0;
                    String fileNameToSave = fileFullName;
                    while (fileList.contains(fileNameToSave)) {
                        i++;
                        fileNameToSave = fileName + "_" + i + fileExt;
                    }
                    dsFileItem.setFileName(fileNameToSave);

                    // Write the file to the directory
                    File newFile = new File(wholePath, fileNameToSave);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Absolute path is " + newFile.getAbsolutePath());
                    }
                    uploadFileItem.write(newFile);

                    // Create a new file item in the database
                    FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
                    fileDao.saveOrUpdate(dsFileItem);

                    successFlag = true;
                } else {
                    logger.error("createFile: Creating directory failed " + newDirectory);
                }
            } else {
                logger.error("createFile: The fileName cannot be null or empty.");
            }
        }

        if (successFlag) {
            return dsFileItem;
        } else {
            return null;
        }
    }

    /**
     * Get the Project Item from the database given the project id.
     * @param projectId the project id
     * @return the Project item
     */
    private ProjectItem getProjectItem(Integer projectId) {
        if (projectId == null) { return null; }

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        ProjectItem projectItem = projectDao.get(projectId);

        return projectItem;
    }

    /**
     * Get the IRB Item from the database using the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the IRB item
     */
    private IrbItem getIrbItem(HttpServletRequest req) {
        Integer irbId = getIntegerId((String)req.getParameter("irbId"));

        if (irbId == null) { return null; }

        IrbDao irbDao = DaoFactory.DEFAULT.getIrbDao();
        IrbItem item = irbDao.get(irbId);

        return item;
    }

    /**
     * Get the File Item from the database using the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the file item
     */
    private FileItem getFileItem(HttpServletRequest req) {
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
        FileItem fileItem = null;
        String fileIdParam = null;
        Integer fileId = null;
        try {
            fileIdParam = getParameter(req, "fileId");
            fileId = Integer.parseInt(fileIdParam);
            fileItem = fileDao.get(fileId);
        } catch (NumberFormatException exception) {
            //Do nothing
            fileItem = null;
        }
        if (fileItem == null) {
            logger.error("Invalid file id: " + fileIdParam);
        }
        return fileItem;
    }

    /**
     * Helper method to validate input.
     * @param inputStr the input String
     * @param maxLen max String length allowed
     * @return boolean indication of valid input
     */
    private boolean validateInput(String inputStr, int maxLen) {
        if ((inputStr == null)
            || (inputStr.length() == 0)
            || (inputStr.length() > maxLen)) {
            return false;
        }
        return true;
    }

    /**
     * Helper method for displaying Project IRB page.
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse
     * @param projectId the project id
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void displayProjectIrbPage(HttpServletRequest req, HttpServletResponse resp,
                                       Integer projectId, UserItem userItem)
        throws IOException, ServletException {

        if (userItem == null) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            userItem = userDao.findOrCreateDefaultUser();
        }

        ProjectItem projectItem = getProjectItem(projectId);

        String logInfoStr = getProjectLogInfoStr(projectItem);
        UserLogger.log(userItem, UserLogger.VIEW_PROJECT_IRB, logInfoStr, true);

        goToProjectIrbPage(projectId, getIrbProjectDto(projectItem, userItem), req, resp);
    }

    /**
     * Helper method for forwarding to appropriate ProjectIRB page.
     * @param projectId the project id
     * @param dto the IRB Project DTO
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void goToProjectIrbPage(Integer projectId, IrbProjectDto dto,
                                    HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        req.setAttribute(IRB_PROJECT_ATTR + projectId, dto);

        RequestDispatcher disp = getServletContext().getRequestDispatcher(JSP_NAME);
        disp.forward(req, resp);
    }

    /**
     * Helper method for getting the Project IRB DTO.
     * @param projectItem the project
     * @param userItem the user currently logged in
     * @return IrbProjectDto the DTO
     */
    private IrbProjectDto getIrbProjectDto(ProjectItem projectItem, UserItem userItem) {
        IrbHelper irbHelper = HelperFactory.DEFAULT.getIrbHelper();
        IrbProjectDto irbProjectDto = irbHelper.getIrbProjectDto(projectItem, userItem);
        return irbProjectDto;
    }

    /**
     * Constant for maximum length of description to log.
     */
    private static final int MAX_DESC_LENGTH_LOGGED = 500;

    /**
     * Helper method for generating user log info for specific IRB.
     * @param irbItem the IRB
     * @return String the log info
     */
    private String getIrbLogInfoStr(IrbItem irbItem) {
        return "IRB: '" + irbItem.getTitle() + "' (" + irbItem.getId() + ")";
    }

    /**
     * Helper method for updating user log.
     * @param userItem the user currently logged in
     * @param irbItem the IRB
     * @param attrName the attribute
     * @param oldValue the original value
     * @param newValue the new value
     */
    private void updateUserLog(UserItem userItem, IrbItem irbItem,
            String attrName, String oldValue, String newValue) {
        if (oldValue.length() > MAX_DESC_LENGTH_LOGGED) {
            oldValue = oldValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }
        if (newValue.length() > MAX_DESC_LENGTH_LOGGED) {
            newValue = newValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }

        String logInfoStr = getIrbLogInfoStr(irbItem);
        logInfoStr += ": Changed " + attrName + " from '" + oldValue + "' to '" + newValue + "'";
        UserLogger.log(userItem, UserLogger.EDIT_IRB, logInfoStr, false);
    }

    /**
     * Helper method for generating user log info for specific project.
     * @param projectItem the project
     * @return String the log info
     */
    private String getProjectLogInfoStr(ProjectItem projectItem) {
        return "Project: '" + projectItem.getProjectName() + "' (" + projectItem.getId() + ")";
    }

    /**
     * Helper method for updating user log.
     * @param userItem the user currently logged in
     * @param projectItem the project
     * @param attrName the attribute
     * @param oldValue the original value
     * @param newValue the new value
     */
    private void updateUserLog(UserItem userItem, ProjectItem projectItem,
            String attrName, String oldValue, String newValue) {
        if (oldValue.length() > MAX_DESC_LENGTH_LOGGED) {
            oldValue = oldValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }
        if (newValue.length() > MAX_DESC_LENGTH_LOGGED) {
            newValue = newValue.substring(0, MAX_DESC_LENGTH_LOGGED);
        }
        String logInfoStr = getProjectLogInfoStr(projectItem);
        logInfoStr += ": Changed " + attrName + " from '" + oldValue + "' to '" + newValue + "'";
        UserLogger.log(userItem, UserLogger.MODIFY_PROJECT_IRB, logInfoStr, false);
    }

    /**
     * Helper method for redirecting to the ProjectIRB page.
     * @param projectId the project id
     * @param dto the IRB Project DTO
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToProjectIrbPage(Integer projectId, IrbProjectDto dto,
                                          HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        // With the redirect, the DTO has to be put in the session... request is cleared.
        req.getSession().setAttribute(IRB_PROJECT_ATTR + projectId, dto);

        resp.sendRedirect(SERVLET + "?id=" + projectId);
    }

    /**
     * Helper method for redirecting to the ProjectIRB page.
     * @param projectId the project id
     * @param message the status message, if applicable
     * @param messageLevel the status message level, if applicable
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToProjectIrbPage(Integer projectId, String message, String messageLevel,
            HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
            throws IOException, ServletException {

        IrbProjectDto dto = getIrbProjectDto(getProjectItem(projectId), userItem);
        dto.setMessage(message);
        dto.setMessageLevel(messageLevel);

        redirectToProjectIrbPage(projectId, dto, req, resp);
    }

    /**
     * Helper method for redirecting to the IRBReview page.
     * @param message the status message, if applicable
     * @param messageLevel the status message level, if applicable
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void redirectToIrbReviewPage(String message, String messageLevel,
                                         HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException {

        // With the redirect, any message  has to be put in the session... request is cleared.
        req.getSession().setAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_ATTR, message);
        req.getSession().setAttribute(IrbReviewServlet.IRB_REVIEW_MESSAGE_LEVEL_ATTR, messageLevel);

        resp.sendRedirect(IRB_REVIEW_SERVLET + "?all");
    }
}
