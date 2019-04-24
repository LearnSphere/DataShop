/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.exttools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.ExternalToolDao;
import edu.cmu.pslc.datashop.dao.ExternalToolFileMapDao;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.helper.UserState;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapId;
import edu.cmu.pslc.datashop.item.ExternalToolFileMapItem;
import edu.cmu.pslc.datashop.item.ExternalToolItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;

/**
 * This servlet handles all the requests from the External Tools pages.
 *
 * @author alida
 * @version $Revision: 13027 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-03-28 12:32:22 -0400 (Mon, 28 Mar 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolsServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP file to view the index of external tools. */
    private static final String JSP_NAME_TABLE = "/external_tool_table.jsp";
    /** The JSP file to view a single external tool. */
    private static final String JSP_NAME_VIEW = "/external_tool_view.jsp";

    /** Constant for the location of the external tool files. */
    private static final String EXTERNAL_TOOLS_SUB_PATH = "external_tools";

    /** Title for this page - "External Tools". */
    public static final String SERVLET_TITLE = "External Tools";

    /** Label used for setting session attribute "recent_report". */
    private static final String SERVLET_NAME = "ExternalTools";

    /** Constant string for the Tool ID parameter. */
    private static final String PARAM_TOOL_ID = "toolId";
    /** Constant string for the External Tools Action parameter. */
    private static final String PARAM_ACTION = "externalToolsAction";
    /** Constant for the name of the File Name request parameter. */
    private static final String PARAM_FILE_NAME = "fileName";

    /** User message constant. */
    private static final String ROLE_REQ_PENDING_MSG =
        "Access to add external tools is pending.";
    /** User message constant. */
    private static final String ROLE_REQ_THANK_YOU_MSG =
        "Thank you for requesting the Add External Tools role. We will review your "
        + "request and grant you access shortly.";
    /** User message constant. */
    private static final String ROLE_REQ_GRANTED_MSG =
        "You have been granted the Add External Tools role.";
    /** User message constant. */
    private static final String NAME_TOO_LONG_MSG = "Tool name must be less than 255 characters.";
    /** User message constant. */
    private static final String LANGUAGE_TOO_LONG_MSG =
            "Language must be less than 255 characters.";
    /** User message constant. */
    private static final String HOME_PAGE_TOO_LONG_MSG =
            "Home page must be less than 255 characters.";

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

            // Tell the jsp to highlight this tab
            req.getSession().setAttribute("datasets", SERVLET_TITLE);

            //----- User is logged in -----
            if (userItem != null) {
                if (ServletFileUpload.isMultipartContent(req)) {
                    uploadFile(req, resp, userItem);
                } else {
                    String actionParam = getParameter(req, PARAM_ACTION);
                    if (actionParam != null && actionParam.equals("RequestRole")) {
                        requestRole(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("AddTool")) {
                        addTool(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("DeleteTool")) {
                        deleteTool(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("DeleteToolFile")) {
                        deleteFile(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("DownloadFile")) {
                        downloadOneFile(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("DownloadAllFiles")) {
                        downloadAllFiles(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("editName")) {
                        editToolName(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("editDescription")) {
                        editToolDescription(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("editHomePage")) {
                        editToolHomePage(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("editLanguage")) {
                        editToolLanguage(req, resp, userItem);
                    } else if (actionParam != null && actionParam.equals("getToolListByOwner")) {
                        getToolListByOwner(req, resp, userItem);
                    } else if (actionParam != null &&
                               actionParam.equals("incrementDownloadsCounter")) {
                        incrementDownloadsCount(req, resp, userItem);
                    } else {
                        showToolOrTablePage(req, resp, userItem, null, null);
                    }
                }
            //----- User is NOT logged in -----
            } else {
                userItem = DaoFactory.DEFAULT.getUserDao().findOrCreateDefaultUser();
                String actionParam = getParameter(req, PARAM_ACTION);
                if (actionParam != null && actionParam.equals("DownloadFile")) {
                    downloadOneFile(req, resp, userItem);
                } else if (actionParam != null && actionParam.equals("DownloadAllFiles")) {
                    downloadAllFiles(req, resp, userItem);
                } else if (actionParam != null &&
                           actionParam.equals("incrementDownloadsCounter")) {
                    incrementDownloadsCount(req, resp, userItem);
                } else {
                    showToolOrTablePage(req, resp, userItem, null, null);
                }
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
     * @param jspName the name of the JSP to forward to
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void goToJsp(HttpServletRequest req, HttpServletResponse resp,
            String jspName) throws IOException, ServletException {
        logger.info("Going to JSP: " + jspName);
        RequestDispatcher disp;
        disp = getServletContext().getRequestDispatcher(jspName);
        disp.forward(req, resp);
    }

    /**
     * Build an ExternalToolTableDto from the HTTP request, the current user,
     * and no message.
     * @param req {@link HttpServletRequest}
     * @param userItem user currently logged in
     * @return an external tool table DTO
     */
    private ExternalToolTableDto getTableDto(
            HttpServletRequest req,
            UserItem userItem) {
        return getTableDto(req, userItem, null, null);
    }

    /**
     * Build an ExternalToolTableDto from the HTTP request, the current user,
     * and message and its level.
     * @param req {@link HttpServletRequest}
     * @param userItem user currently logged in
     * @param message the message to set
     * @param messageLevel the messageLevel to set
     * @return an external tool table DTO
     */
    private ExternalToolTableDto getTableDto(
            HttpServletRequest req,
            UserItem userItem,
            String message, String messageLevel) {
        ExternalToolsHelper toolsHelper = HelperFactory.DEFAULT.getExternalToolsHelper();
        List<ExternalToolDto> toolList =
                toolsHelper.getToolList(ExternalToolsContext.getContext(req));
        ExternalToolTableDto tableDto =
                new ExternalToolTableDto(message, messageLevel, toolList);
        boolean hasRole = toolsHelper.hasExternalToolsRole(userItem);
        if (hasRole) {
            tableDto.setRequestedExtToolsRoleFlag(hasRole);
        } else {
            boolean hasRequestedRole = UserState.hasRequestedExternalToolsRole(userItem);
            if (hasRequestedRole) {
                if (message == null) {
                    tableDto.setMessage(ROLE_REQ_PENDING_MSG);
                    tableDto.setMessageLevel(ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS);
                }
            }
            tableDto.setRequestedExtToolsRoleFlag(hasRequestedRole);
        }
        tableDto.setExtToolsRoleFlag(hasRole);
        tableDto.setAdminAuthFlag(userItem.getAdminFlag());
        return tableDto;
    }

    /**
     * Build an ExternalToolPageDto from the HTTP request, the current user,
     * the current tool and message and its level.
     * @param req {@link HttpServletRequest}
     * @param userItem user currently logged in
     * @param toolItem tool currently being viewed
     * @param message the message to set
     * @param messageLevel the messageLevel to set
     * @return an external tool table DTO
     */
    private ExternalToolPageDto getPageDto(
            HttpServletRequest req,
            UserItem userItem,
            ExternalToolItem toolItem,
            String message, String messageLevel) {

        ExternalToolsHelper toolsHelper = HelperFactory.DEFAULT.getExternalToolsHelper();
        ExternalToolDto toolDto = toolsHelper.getToolDto(toolItem);
        List<ExternalToolFileDto> fileList =
            toolsHelper.getFileList(toolItem, ExternalToolsContext.getContext(req));
        ExternalToolPageDto pageDto = new ExternalToolPageDto(
                message, messageLevel, toolDto, fileList);
        pageDto.setEditAuthFlag(userItem.getId().equals(toolDto.getContributorId()));
        pageDto.setAdminAuthFlag(userItem.getAdminFlag());

        return pageDto;
    }

    /**
     * Handle user request to view the complete list of external tools.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void showToolList(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {

        // Set the most recent servlet name for the help page.
        setRecentReport(req.getSession(true), SERVLET_NAME);

        // Check if the sort by column has been changed
        String actionParam = getParameter(req, "externalToolsAction");
        if (actionParam != null && actionParam.equals("sort")) {
            String sortByParam = getParameter(req, "sortBy");
            if (sortByParam != null) {
                setSortByColumnForTools(req, sortByParam);
            } else {
                logger.warn("showToolList: The sortBy parameter is unexpectedly null.");
            }
        }

        // Set the DTO for the table page
        req.setAttribute(ExternalToolTableDto.ATTRIB_NAME, getTableDto(req, userItem));

        UserLogger.log(userItem, UserLogger.VIEW_EXTERNAL_TOOL_TABLE, "", true);

        goToJsp(req, resp, JSP_NAME_TABLE);
    }

    /**
     * Handle user's request for the external tools role.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void requestRole(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        String message;
        String messageLevel;

        // Does the user have this role already?
        ExternalToolsHelper toolsHelper = HelperFactory.DEFAULT.getExternalToolsHelper();
        boolean hasRole = toolsHelper.hasExternalToolsRole(userItem);
        if (hasRole) {
            message = ROLE_REQ_GRANTED_MSG;
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        } else {
            boolean hasRequestedRole = UserState.hasRequestedExternalToolsRole(userItem);
            if (hasRequestedRole) {
                message = ROLE_REQ_PENDING_MSG;
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            } else {
                message = ROLE_REQ_THANK_YOU_MSG;
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;

                String reason = req.getParameter("requestReason");

                String info = "User '" + userItem.getId() + "', Reason '" + reason + "'";
                UserLogger.log(userItem, UserLogger.REQ_EXTERNAL_TOOL_ROLE, info, false);

                if (isSendmailActive()) {
                    String userEmail = userItem.getEmail();
                    String subject = "Requesting permission to add External Tools to DataShop";
                    StringBuffer msg = new StringBuffer();
                    msg.append("<br>");
                    msg.append("User ");
                    msg.append(userItem.getUserName());
                    if (userEmail != null) {
                        msg.append(", ");
                        msg.append("<a href=\"mailto:");
                        msg.append(userEmail);
                        msg.append("\">");
                        msg.append(userEmail);
                        msg.append("</a>");
                        msg.append(",");
                    }
                    msg.append(" is requesting permission to add External Tools. ");
                    if (reason != null) {
                        msg.append("The reason given is: ");
                        msg.append("<br><br>");
                        msg.append(reason);
                    }
                    msg.append("<br>");

                    sendDataShopHelpEmail(null, subject, msg.toString(), userEmail);
                }
            }
        }

        // Set the DTO for the table page
        req.setAttribute(ExternalToolTableDto.ATTRIB_NAME,
                getTableDto(req, userItem, message, messageLevel));

        goToJsp(req, resp, JSP_NAME_TABLE);
    }

    /**
     * User would like to add an external tool to DataShop.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void addTool(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        String nameParam = getParameter(req, "toolNameField");
        String descParam = getParameter(req, "toolDescField");
        String langParam = getParameter(req, "toolLangField");
        String pageParam = getParameter(req, "toolPageField");

        String message;
        String messageLevel;
        if (nameParam == null || nameParam.length() == 0) {
            message = "The tool must have a name.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
        } else {
            ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
            // Need to check if the tool is unique by contributor and name
            ExternalToolItem toolItem;
            String newToolName = stripHtml(nameParam).trim();
            toolItem = toolDao.findByNameAndContributor(newToolName, userItem);
            if (toolItem != null) {
                message = "The tool name must be unique.";
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            } else {
                toolItem = new ExternalToolItem();
                toolItem.setName(newToolName);
                if (descParam != null) { toolItem.setDescription(stripHtml(descParam).trim()); }
                if (langParam != null) { toolItem.setLanguage(stripHtml(langParam).trim()); }
                if (pageParam != null) { toolItem.setWebPage(stripHtml(pageParam).trim()); }
                toolItem.setAddedTime(new Date());
                toolItem.setUpdatedTime(toolItem.getAddedTime());
                toolItem.setContributor(userItem);
                toolDao.saveOrUpdate(toolItem);

                Integer toolId = (Integer)toolItem.getId();
                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
                UserLogger.log(userItem, UserLogger.ADD_EXTERNAL_TOOL, info, false);

                message = "The tool has been added successfully.";
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            }
        }

        // Set the DTO for the table page
        req.setAttribute(ExternalToolTableDto.ATTRIB_NAME,
                getTableDto(req, userItem, message, messageLevel));

        goToJsp(req, resp, JSP_NAME_TABLE);
    }

    /**
     * User would like to delete an external tool from DataShop.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void deleteTool(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        ExternalToolItem toolItem = getToolItem(req);

        String message;
        String messageLevel;
        if (toolItem == null) {
            message = "Invalid tool.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
        } else {
            Integer toolId = (Integer)toolItem.getId();

            ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
            ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

            // Delete Files
            List<ExternalToolFileMapItem> mapList  = mapDao.findByTool(toolItem);
            for (ExternalToolFileMapItem mapItem : mapList) {
                FileItem fileItem = mapItem.getFile();
                fileItem = fileDao.get((Integer)fileItem.getId());

                // delete the filesystem file
                fileItem.deleteFile(getBaseDir());

                // now delete from the database
                mapDao.delete(mapItem);
                fileDao.delete(fileItem);
            }

            // Remove the directory where the files were stored.
            String dirPath = getWholePath(toolId);
            File toolDir = new File(dirPath);
            toolDir.delete();

            // Delete Tool
            toolDao.delete(toolItem);

            String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
            UserLogger.log(userItem, UserLogger.DELETE_EXTERNAL_TOOL, info, false);

            message = "The tool has been deleted successfully.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
        }

        // Set the DTO for the table page
        req.setAttribute(ExternalToolTableDto.ATTRIB_NAME,
                getTableDto(req, userItem, message, messageLevel));

        goToJsp(req, resp, JSP_NAME_TABLE);
    }

    /**
     * Handle request to upload a file to a given tool.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void uploadFile(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        ExternalToolItem toolItem = getToolItem(req);

        String message = null;
        String messageLevel = null;

        if (toolItem != null && ServletFileUpload.isMultipartContent(req)) {
            List <org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
            if (items == null) {
                message = "File size limit (400MB) was exceeded.";
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            } else {
                ExternalToolsHelper helper = HelperFactory.DEFAULT.getExternalToolsHelper();
                for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {
                    if (!uploadFileItem.isFormField()) {
                        try {
                            Integer toolId = (Integer)toolItem.getId();
                            FileItem dsFileItem = createFile(uploadFileItem, userItem,
                                    getBaseDir(), getSubPath(toolId));
                            if (dsFileItem != null) {
                                helper.addFileToTool(toolItem, dsFileItem);
                                message = "The file has been added successfully.";
                                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;

                                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]"
                                    + ", File '" + dsFileItem.getDisplayFileName() + "'";
                                UserLogger.log(userItem, UserLogger.UPLOAD_EXTERNAL_TOOL_FILE,
                                        info, false);
                            } else {
                                message = "The file was not added.";
                                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
                            }
                        } catch (Exception exception) {
                            message = "Something went wrong adding the file.";
                            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
                            logger.error("uploadFile: Exception occurred.", exception);
                        }
                    }
                } // end for loop
            }
        } else {
            message = "Something went wrong.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
            logger.error("uploadFile: Request is not a multi-part.");
        }

        if (toolItem != null) {
            ExternalToolPageDto pageDto =
                    getPageDto(req, userItem, toolItem, message, messageLevel);
            // Sigh. For this case (file upload) we need to put the DTO in the session.
            req.getSession().setAttribute(ExternalToolPageDto.ATTRIB_NAME, pageDto);
            resp.sendRedirect(SERVLET_NAME + "?toolId=" + toolItem.getId());
        } else {
            showToolList(req, resp, userItem);
        }
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

        if (fieldName.equals(PARAM_FILE_NAME)) {
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
     * Handle request to delete a file from an external tool.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void deleteFile(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, ServletException {
        ExternalToolItem toolItem = getToolItem(req);
        FileItem fileItem = getFileItem(req);

        String message;
        String messageLevel;
        if (toolItem == null) {
            message = "Invalid tool.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            logger.warn("deleteOneFile: toolItem is null");
        } else if (fileItem == null) {
            message = "Invalid file.";
            messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            logger.warn("deleteOneFile: fileItem is null");
        } else {
            FileDao fileDao = DaoFactory.DEFAULT.getFileDao();
            ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();
            ExternalToolFileMapId mapId = new ExternalToolFileMapId(toolItem, fileItem);
            ExternalToolFileMapItem mapItem = mapDao.get(mapId);
            if (mapItem == null) {
                message = "Failed to delete the file.";
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            } else {
                Integer toolId = (Integer)toolItem.getId();
                Integer fileId = (Integer)fileItem.getId();

                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "], "
                    + "File '" + fileItem.getDisplayFileName() + "'"
                    + " [" + fileId + "]";
                String extraInfo = info + " :: " + fileItem.getFullFileName(getBaseDir());

                if (fileItem.deleteFile(getBaseDir())) {
                    logger.info("Deleted file: " + info);
                    UserLogger.log(userItem, UserLogger.DELETE_EXTERNAL_TOOL_FILE,
                                   info, false);
                } else {
                    logger.error("Unable to delete file: " + extraInfo);
                }

                mapDao.delete(mapItem);
                fileDao.delete(fileItem);

                message = "The file has been deleted successfully.";
                messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;

                ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                toolItem.setUpdatedTime(new Date());
                toolDao.saveOrUpdate(toolItem);
            }
        }

        viewExistingTool(req, resp, userItem, toolItem, message, messageLevel);
    }

    /**
     * Go to the External Tool Page if a tool id is specified on the request,
     * otherwise go to the External Tool Table.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @param message the message to set
     * @param messageLevel the messageLevel to set
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void showToolOrTablePage(HttpServletRequest req,
            HttpServletResponse resp, UserItem userItem,
            String message, String messageLevel) throws IOException, ServletException {
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            Integer toolId = (Integer)toolItem.getId();
            String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
            UserLogger.log(userItem, UserLogger.VIEW_EXTERNAL_TOOL, info, true);
            viewExistingTool(req, resp, userItem, toolItem, message, messageLevel);
        } else {
            showToolList(req, resp, userItem);
        }
    }

    /**
     * Handle request to view an existing external tool.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @param toolItem the external tool item to view
     * @param message the message to set
     * @param messageLevel the messageLevel to set
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void viewExistingTool(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem, ExternalToolItem toolItem,
            String message, String messageLevel) throws IOException, ServletException {

        // Check if the sort by column has been changed
        String actionParam = getParameter(req, "externalToolsAction");
        if (actionParam != null && actionParam.equals("sort")) {
            String sortByParam = getParameter(req, "sortBy");
            if (sortByParam != null) {
                setSortByColumnForFiles(req, sortByParam);
            } else {
                logger.warn("viewExistingTool: The sortBy parameter is unexpectedly null.");
            }
        }

        // Get the data for the JSP
        ExternalToolPageDto pageDto = getPageDto(req, userItem, toolItem, message, messageLevel);

        // Set the DTO for the tool page
        req.setAttribute(ExternalToolPageDto.ATTRIB_NAME, pageDto);

        goToJsp(req, resp, JSP_NAME_VIEW);
    }

    /**
     * Handle request to download a single file for a given tool.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void downloadOneFile(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws ServletException, IOException {
        ExternalToolItem toolItem = getToolItem(req);
        FileItem fileItem = getFileItem(req);

        boolean successFlag = false;

        if (toolItem == null) {
            logger.warn("downloadOneFile: toolItem is null");
            successFlag = false;
        } else if (fileItem == null) {
            logger.warn("downloadOneFile: fileItem is null");
            successFlag = false;
        } else {
            ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();
            ExternalToolFileMapId mapId = new ExternalToolFileMapId(toolItem, fileItem);
            ExternalToolFileMapItem mapItem = mapDao.get(mapId);
            if (mapItem == null) {
                logger.warn("downloadOneFile: mapItem is null");
                successFlag = false;
            } else {
                try {
                    Integer fileId = (Integer)fileItem.getId();
                    String fileName = fileItem.getFileName();
                    String actualFileName = fileItem.getUrl(getBaseDir());

                    String info = "Tool '" + toolItem.getName() + "', "
                        + "File '" + fileItem.getDisplayFileName() + "'"
                        + " [" + fileId + "]";

                    if (logger.isDebugEnabled()) {
                        logger.debug("downloadOneFile: " + info);
                    }

                    resp.setContentType("application/x-download");
                    resp.setContentLength(fileItem.getFileSize().intValue());
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

                        mapItem.incrementDownloads();
                        mapDao.saveOrUpdate(mapItem);

                        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                        toolItem.incrementDownloads();
                        toolDao.saveOrUpdate(toolItem);

                        UserLogger.log(userItem, UserLogger.DOWNLOAD_EXTERNAL_TOOL_FILE,
                                info, false);

                    } catch (FileNotFoundException exception) {
                        logger.error("downloadOneFile: FileNotFoundException occurred: "
                                + info + " :: " + actualFileName,  exception);
                    } finally {
                        // very important
                        if (inStream != null) { inStream.close(); }
                        if (outStream != null) { outStream.close(); }
                    }

                } catch (Exception exception) {
                    logger.error("downloadOneFile: Exception occurred.", exception);
                }
                successFlag = true;
            }
        }

        if (!successFlag) {
            String message = "Invalid download.";
            String messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            showToolOrTablePage(req, resp, userItem, message, messageLevel);
        }
    }

    /** Buffer size for zip file processing. */
    private static final int ZIP_BUFFER_SIZE = 18024;

    /**
     * Handle request to download all the files for a given tool.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem user currently logged in
     * @throws IOException an IO exception
     * @throws ServletException an exception creating the servlet
     */
    private void downloadAllFiles(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws ServletException, IOException {
        boolean successFlag = false;

        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem == null) {
            logger.warn("downloadAllFiles: toolItem is null");
            successFlag = false;
        } else {
            String zipFileName = toolItem.getZipFileName();

            File zipFile = createZipFile(zipFileName, toolItem);
            String zipFilePath = zipFile.getAbsolutePath();

            String info = "Tool '" + toolItem.getName() + "', "
            + "File '" + zipFileName + "'";

            resp.setContentType("application/zip; charset=UTF-8");
            resp.setContentLength((int)zipFile.length());
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + zipFileName + "\"");

            BufferedInputStream inStream = null;
            OutputStream outStream = resp.getOutputStream();
            try {
                inStream = new BufferedInputStream(new FileInputStream(zipFilePath));
                int ch;
                while ((ch = inStream.read()) != -1) {
                    outStream.write(ch);
                }
                UserLogger.log(null, userItem, UserLogger.DOWNLOAD_EXTERNAL_TOOL_FILES, info);
                successFlag = true;
            } catch (FileNotFoundException exception) {
                logger.error("downloadAllFiles: FileNotFoundException occurred: "
                        + info + " :: " + zipFilePath,  exception);
                successFlag = false;
            } finally {
                // very important
                if (inStream != null) { inStream.close();   }
                if (outStream != null) { outStream.close();   }
            }
        }

        if (!successFlag) {
            String message = "Invalid download.";
            String messageLevel = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
            showToolOrTablePage(req, resp, userItem, message, messageLevel);
        }
    }

    /**
     * Create the zip file with all the tool's attached files to return to the user.
     * @param zipFileName the name of the zip file
     * @param toolItem the external tool item to view
     * @return a File
     */
    private File createZipFile(String zipFileName, ExternalToolItem toolItem) {
        Integer toolId = (Integer)toolItem.getId();
        String zipFilePath = getWholePath(toolId) + File.separator + zipFileName;
        File zipFile = new File(zipFilePath);

        ExternalToolFileMapDao mapDao = DaoFactory.DEFAULT.getExternalToolFileMapDao();
        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
        FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

        ZipOutputStream outStream = null;
        FileInputStream inStream = null;
        ZipEntry zipEntry = null;
        byte[] buffer = new byte[ZIP_BUFFER_SIZE];

        try {
            outStream = new ZipOutputStream(new FileOutputStream(zipFile));
            outStream.setLevel(Deflater.DEFAULT_COMPRESSION);

            List<ExternalToolFileMapItem> mapItemList = mapDao.findByTool(toolItem);

            for (ExternalToolFileMapItem mapItem : mapItemList) {
                FileItem fileItem = mapItem.getFile();
                fileItem = fileDao.get((Integer)fileItem.getId());
                String fileName = fileItem.getFileName();
                String filePath = fileItem.getUrl(getBaseDir());
                File file = new File(filePath);
                zipEntry = new ZipEntry(fileName);
                inStream =  new FileInputStream(file);
                outStream.putNextEntry(zipEntry);
                int length = 0;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
                inStream.close();

                toolItem.incrementDownloads();
                toolDao.saveOrUpdate(toolItem);
                mapItem.incrementDownloads();
                mapDao.saveOrUpdate(mapItem);
            }
        } catch (FileNotFoundException exception) {
            logger.error("createZipFile: FileNotFoundException.", exception);
        } catch (IOException exception) {
            logger.error("createZipFile: IOException.", exception);
        } finally {
            // very important
            try {
                if (inStream != null) {  inStream.close(); }
                if (outStream != null) { outStream.close(); }
            } catch (IOException exception) {
                logger.error("createZipFile: IOException in finally block.", exception);
            }
        }
        return zipFile;
    }

    //----- EDIT TOOL FIELDS METHODS -----

    /** Max string length for the tool field. */
    private static final int MAX_STR_LEN = 255;

    /**
     * Save the new name for the tool and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editToolName(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message;
        String level;
        String value = "";

        // get new value for this field
        String toolValue = getParameter(req, "toolValue");
        if ((toolValue != null) && (toolValue.trim().length() > MAX_STR_LEN)) {
            message = NAME_TOO_LONG_MSG;
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;

            // write JSON response
            writeJSON(resp, json("message", message, "level", level, "value", value));
            return;
        }

        // get tool item
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            if (toolValue != null) {
                toolValue = stripHtml(toolValue).trim();
                if (toolValue.length() > 0) {
                    ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                    ExternalToolItem existingToolItem = toolDao.findByNameAndContributor(
                            toolValue, userItem);
                    if (existingToolItem != null) {
                        message = "The tool name must be unique.";
                        level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_WARN;
                    } else {
                        message = "Tool name updated";
                        level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                        value = toolValue;
                        String prevValue = toolItem.getName();
                        toolItem.setName(value);
                        toolDao.saveOrUpdate(toolItem);
                        Integer toolId = (Integer)toolItem.getId();
                        String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
                        info += " Changed name from '" + prevValue + "' to '" + value + "'";
                        UserLogger.log(userItem, UserLogger.EDIT_EXTERNAL_TOOL, info, false);
                    }
                } else {
                    message = "Tool name cannot be blank or just white space";
                    level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
                }
            } else {
                message = "Tool name not set";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        } else {
            message = "Tool not found.";
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
        }

        // write JSON response
        writeJSON(resp, json("message", message,
                             "level", level,
                             "value", value));
    }

    /**
     * Save the new language for the tool and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editToolDescription(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem)
            throws IOException, JSONException {
        String message;
        String level;
        String value = "";

        // get tool item
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            // get new value for this field
            String toolValue = getParameter(req, "toolValue");
            if (toolValue != null) {
                toolValue = stripHtml(toolValue).trim();
                message = "Description updated";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                String prevValue = toolItem.getDescription();
                value = toolValue;
                toolItem.setDescription(value);
                ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                toolDao.saveOrUpdate(toolItem);

                Integer toolId = (Integer)toolItem.getId();
                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
                info += " Changed descripton from '" + prevValue + "' to '" + value + "'";
                UserLogger.log(userItem, UserLogger.EDIT_EXTERNAL_TOOL, info, false);
            } else {
                message = "Description not set";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        } else {
            message = "Tool not found.";
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
        }

        // write JSON response
        writeJSON(resp, json("message", message,
                             "level", level,
                             "value", value));
    }

    /**
     * Save the new language for the tool and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editToolLanguage(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message;
        String level;
        String value = "";

        // get new value for this field
        String toolValue = getParameter(req, "toolValue");
        if ((toolValue != null) && (toolValue.trim().length() > MAX_STR_LEN)) {
            message = LANGUAGE_TOO_LONG_MSG;
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;

            // write JSON response
            writeJSON(resp, json("message", message, "level", level, "value", value));
            return;
        }

        // get tool item
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            if (toolValue != null) {
                toolValue = stripHtml(toolValue).trim();
                message = "Language updated";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                String prevValue = toolItem.getLanguage();
                value = toolValue;
                toolItem.setLanguage(value);
                ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                toolDao.saveOrUpdate(toolItem);

                Integer toolId = (Integer)toolItem.getId();
                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
                info += " Changed language from '" + prevValue + "' to '" + value + "'";
                UserLogger.log(userItem, UserLogger.EDIT_EXTERNAL_TOOL, info, false);
            } else {
                message = "Language not set";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        } else {
            message = "Tool not found.";
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
        }

        // write JSON response
        writeJSON(resp, json("message", message,
                             "level", level,
                             "value", value));
    }

    /**
     * Save the new home page for the tool and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void editToolHomePage(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {
        String message;
        String level;
        String value = "";

        // get new value for this field
        String toolValue = getParameter(req, "toolValue");
        if ((toolValue != null) && (toolValue.trim().length() > MAX_STR_LEN)) {
            message = HOME_PAGE_TOO_LONG_MSG;
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;

            // write JSON response
            writeJSON(resp, json("message", message, "level", level, "value", value));
            return;
        }

        // get tool item
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            if (toolValue != null) {
                toolValue = stripHtml(toolValue).trim();
                message = "Home page updated";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
                String prevValue = toolItem.getWebPage();
                value = toolValue;
                toolItem.setWebPage(value);
                ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
                toolDao.saveOrUpdate(toolItem);

                Integer toolId = (Integer)toolItem.getId();
                String info = "Tool '" + toolItem.getName() + "' [" + toolId + "]";
                info += " Changed home page from '" + prevValue + "' to '" + value + "'";
                UserLogger.log(userItem, UserLogger.EDIT_EXTERNAL_TOOL, info, false);
            } else {
                message = "Home page not set";
                level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
            }
        } else {
            message = "Tool not found.";
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;
        }

        // write JSON response
        writeJSON(resp, json("message", message,
                             "level", level,
                             "value", value));
    }

    /**
     * Get the list of tools added by this owner and return a JSON object.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void getToolListByOwner(HttpServletRequest req, HttpServletResponse resp,
            UserItem userItem) throws IOException, JSONException {

        // get tool items
        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
        List<ExternalToolItem> toolList = toolDao.findByContributor(userItem);

        Map<String, Integer> toolMap = new HashMap<String, Integer>();
        for (ExternalToolItem tool : toolList) {
            toolMap.put(tool.getName(), (Integer)tool.getId());
        }

        // As JSON object
        JSONObject toolsJson = new JSONObject(toolMap);

        // write JSON response
        writeJSON(resp, json("toolList", toolsJson));
    }

    /**
     * User has clicked on the 'Home page' link. Count this as a download.
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @param userItem the user currently logged in
     * @throws IOException an IO exception
     * @throws JSONException a JSON exception
     */
    private void incrementDownloadsCount(HttpServletRequest req, HttpServletResponse resp,
                                         UserItem userItem)
        throws IOException, JSONException
    {
        String message;
        String level;
        String value = "";

        // get tool item
        ExternalToolItem toolItem = getToolItem(req);
        if (toolItem != null) {
            toolItem.incrementDownloads();
            DaoFactory.DEFAULT.getExternalToolDao().saveOrUpdate(toolItem);

            String info = "Tool '" + toolItem.getName() + "', "
                + "External resource '" + toolItem.getWebPage() + "'";
            UserLogger.log(null, userItem, UserLogger.DOWNLOAD_EXTERNAL_TOOL_FILE, info);

            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_SUCCESS;
            value = "" + toolItem.getDownloads();

            // write JSON response
            writeJSON(resp, json("level", level,
                                 "value", value,
                                 "toolId", (Integer)toolItem.getId()));
        } else {
            message = "Tool not found.";
            level = ExternalToolTableDto.STATUS_MESSAGE_LEVEL_ERROR;

            // write JSON response
            writeJSON(resp, json("level", level, "message", message));
        }
    }

    //----- UTILITY METHODS -----

    /**
     * Get the path where the files for the given tool are to be stored on the server.
     * @param toolId the id of the tool item
     * @return the whole path for this tool
     */
    private String getWholePath(Integer toolId) {
        return getBaseDir()  + File.separator  + getSubPath(toolId);
    }

    /**
     * Get the sub path for External Tools files.
     * @param toolId
     * @param toolId the id of the tool item
     * @return the part of the path between the base directory and the file name
     */
    private String getSubPath(Integer toolId) {
        return EXTERNAL_TOOLS_SUB_PATH + File.separator + "tool_" + toolId;
    }

    /**
     * Get the External Tool Item from the database using the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the external tool item from the request parameter
     */
    private ExternalToolItem getToolItem(HttpServletRequest req) {
        ExternalToolDao toolDao = DaoFactory.DEFAULT.getExternalToolDao();
        ExternalToolItem toolItem = null;
        String toolIdParam = null;
        Integer toolId = null;
        try {
            toolIdParam = getParameter(req, PARAM_TOOL_ID);
            toolId = Integer.parseInt(toolIdParam);
            toolItem = toolDao.get(toolId);
        } catch (NumberFormatException exception) {
            // Do nothing since getToolItem is called when viewing the
            // External Tools landing page
            toolItem = null;
        }

        return toolItem;
    }

    /**
     * Get the File Item from the database using the request parameter.
     * @param req {@link HttpServletRequest}
     * @return the file item from the request parameter
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

    //----- CONTEXT METHODS -----

    /**
     * Sets the sort parameters in the tool context given a column name
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private static void setSortByColumnForTools(HttpServletRequest req, String sortByColumn) {
        ExternalToolsContext context = ExternalToolsContext.getContext(req);
        context.setToolSortByColumn(sortByColumn, true);
        ExternalToolsContext.setContext(req, context);
    }

    /**
     * Sets the sort parameters in the tool context given a column name
     * @param req {@link HttpServletRequest}
     * @param sortByColumn the column to sort the table by
     */
    private static void setSortByColumnForFiles(HttpServletRequest req, String sortByColumn) {
        ExternalToolsContext context = ExternalToolsContext.getContext(req);
        context.setFileSortByColumn(sortByColumn, true);
        ExternalToolsContext.setContext(req, context);
    }

}