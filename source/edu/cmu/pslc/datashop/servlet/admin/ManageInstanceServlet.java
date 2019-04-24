/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.IOException;
import java.io.StringReader;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.json.JSONException;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.webservices.DatashopClient;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;

/**
 * Class that provides manage instance functionalities to the admin.
 * @author Cindy Tipper
 * @version $Revision: 15738 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageInstanceServlet extends AbstractServlet {

    /** Serial Version UID. */
    private static final long serialVersionUID = -2L;

    /** Title for the Admin page - "Admin". */
    public static final String SERVLET_LABEL = "Admin";

    /** Action names to decide which operation to execute. **/
    public static final String SERVLET_NAME = "ManageInstance";

    /** Action names to decide which operation to execute. **/
    public static final String ACTION_VIEW = "view";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_EDIT = "edit";
    /** Action names to decide which operation to execute. **/
    public static final String ACTION_GET = "get";

    /** JSP Page for the forwarding **/
    private static final String MANAGE_INSTANCE_JSP = "/jsp_admin/admin_datashop_instance.jsp";

    /** Session key for the filter info. **/
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MM/dd/yyyy");

    /** Constant for the session attribute: message. */
    public static final String MESSAGE = "message";

    /** Constant for the session attribute: message_level. */
    public static final String MESSAGE_LEVEL = "message_level";

    /** Constant for the session attribute: success. */
    public static final String SUCCESS = "success";

    /** Constant for the session attribute: warning. */
    public static final String WARNING = "warning";

    /** Constant for the session attribute: error. */
    public static final String ERROR = "error";

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
     * Process post method.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        logDebug("doPost begin :: ", getDebugParamsString(req));

        // Set the most recent servlet name for the help page
        setRecentReport(req.getSession(true), SERVLET_NAME);

        UserItem userItem = getLoggedInUserItem(req);

        String actionName = getActionName(req);
        try {
            if (ACTION_GET.equals(actionName)) {
                // Doesn't require logged in user.
                getInfo(req, resp);
            } else if (ACTION_VIEW.equals(actionName)) {
                checkUser(userItem);
                showInstance(req, resp);
            } else if (ACTION_EDIT.equals(actionName)) {
                checkUser(userItem);
                editInstance(req, resp, userItem);
            } else {
                checkUser(userItem);
                showInstance(req, resp);
            }
        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    }

    /**
     * Gets the action name from the request. The action name is used define the specific function
     * to call in the get/post operation.
     * @param req HttpServletRequest
     * @return the request parameter, 'action'
     */
    private String getActionName(HttpServletRequest req) {
        return req.getParameter("action");
    }

    /**
     * Respond to AJAX request to get instance info.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws JSONException JSON exception
     */
    private void getInfo(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, JSONException
    {
        DataShopInstanceItem dsInstance = getInstance();

        if (dsInstance == null) {
            writeJSON(resp, json("msg", "ERROR"));
        } else {
            writeJSON(resp, json("msg", "SUCCESS",
                                 "isSlave", dsInstance.getIsSlave(),
                                 "slaveId", dsInstance.getSlaveId(),
                                 "helpEmail", dsInstance.getDatashopHelpEmail(),
                                 "datashopUrl", dsInstance.getDatashopUrl()));
        }
    }

    /**
     * Checks the preconditions: user should be logged in and must be an admin.
     * @param loggedInUser the UserItem for the logged in user
     */
    private void checkUser(UserItem loggedInUser) {
        if (loggedInUser == null) {
            logger.warn("User not logged in attempted to access ManageInstanceServlet. "
                        + "Is the AccessFilter properly set?");
            throw new IllegalStateException("You are not logged in.");
        }

        if (!loggedInUser.getAdminFlag()) {
            logger.warn("A non-admin user attempted to access ManageInstanceServlet. "
                        + "Is the AccessFilter properly set?");
            throw new IllegalStateException("You are not an Adminstrator.");
        }
    }

    /**
     * Displays the DataShopInstance.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void showInstance(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        DataShopInstanceItem dsInstance = getInstance();

        ManageInstanceDto dto = getDto(dsInstance);

        req.setAttribute("manageInstanceDto", dto);

        getServletContext().getRequestDispatcher(MANAGE_INSTANCE_JSP).forward(req, resp);
    }

    /**
     * Edit the DataShopInstance.
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param userItem the logged in user
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void editInstance(HttpServletRequest req, HttpServletResponse resp, UserItem userItem)
        throws ServletException, IOException
    {
        DataShopInstanceItem dsInstance = getInstance();

        dsInstance.setConfiguredBy(userItem.getName());
        dsInstance.setConfiguredTime(new Date());

        // If either 'url' or 'slaveId' change, update remote instance.
        boolean updateRemote = false;

        if (req.getParameter("datashopUrl") != null) {
            if (!req.getParameter("datashopUrl").equals(dsInstance.getDatashopUrl())) {
                updateRemote = true;
            }
            dsInstance.setDatashopUrl(req.getParameter("datashopUrl"));
        }

        // Input is a checkbox. If present, value is true, otherwise, false.
        if (req.getParameter("isSlave") != null) {
            dsInstance.setIsSlave(true);
        } else {
            dsInstance.setIsSlave(false);
        }
        if (req.getParameter("slaveId") != null) {
            if (!req.getParameter("slaveId").equals(dsInstance.getSlaveId())) {
                updateRemote = true;
            }
            dsInstance.setSlaveId(req.getParameter("slaveId"));
        }
        if (req.getParameter("masterUser") != null) {
            dsInstance.setMasterUser(getUser(req.getParameter("masterUser")));
        }
        if (req.getParameter("masterUrl") != null) {
            dsInstance.setMasterUrl(req.getParameter("masterUrl"));
        }
        if (req.getParameter("masterSchema") != null) {
            dsInstance.setMasterSchema(req.getParameter("masterSchema"));
        }
        if (req.getParameter("slaveApiToken") != null) {
            dsInstance.setSlaveApiToken(req.getParameter("slaveApiToken"));
        }
        if (req.getParameter("slaveSecret") != null) {
            dsInstance.setSlaveSecret(req.getParameter("slaveSecret"));
        }
        if (req.getParameter("isSendmailActive") != null) {
            dsInstance.
                setIsSendmailActive(Boolean.parseBoolean(req.getParameter("isSendmailActive")));
        }
        if (req.getParameter("datashopHelpEmail") != null) {
            dsInstance.setDatashopHelpEmail(req.getParameter("datashopHelpEmail"));
        }
        if (req.getParameter("datashopRmEmail") != null) {
            dsInstance.setDatashopRmEmail(req.getParameter("datashopRmEmail"));
        }
        if (req.getParameter("datashopBucketEmail") != null) {
            dsInstance.setDatashopBucketEmail(req.getParameter("datashopBucketEmail"));
        }
        if (req.getParameter("datashopSmtpHost") != null) {
            dsInstance.setDatashopSmtpHost(req.getParameter("datashopSmtpHost"));
        }
        if (req.getParameter("datashopSmtpPort") != null) {
            if (req.getParameter("datashopSmtpPort").equals("")) {
                dsInstance.setDatashopSmtpPort(null);
            } else {
                dsInstance.setDatashopSmtpPort(getIntegerParam(req, "datashopSmtpPort"));
            }
        }
        // Input is a checkbox. If present, value is true, otherwise, false.
        if (req.getParameter("useSslSmtp") != null) {
            dsInstance.setUseSslSmtp(true);
            if (req.getParameter("datashopSmtpUser") != null) {
                dsInstance.setDatashopSmtpUser(req.getParameter("datashopSmtpUser"));
            }
            if (req.getParameter("datashopSmtpPassword") != null) {
                dsInstance.setDatashopSmtpPassword(req.getParameter("datashopSmtpPassword"));
            }
        } else {
            dsInstance.setUseSslSmtp(false);
            dsInstance.setDatashopSmtpUser(null);
            dsInstance.setDatashopSmtpPassword(null);
        }
        if (req.getParameter("ghClientId") != null) {
            dsInstance.setGithubClientId(req.getParameter("ghClientId"));
        }
        if (req.getParameter("ghClientSecret") != null) {
            dsInstance.setGithubClientSecret(req.getParameter("ghClientSecret"));
        }
        if (req.getParameter("wfcDir") != null) {
            dsInstance.setWfcDir(req.getParameter("wfcDir"));
        }
        if (req.getParameter("wfcRemote") != null) {
            dsInstance.setWfcRemote(req.getParameter("wfcRemote"));
        }
        if (req.getParameter("wfcHeapSize") != null) {
            if (req.getParameter("wfcHeapSize").matches("[0-9]+")) {
                dsInstance.setWfcHeapSize(Long.parseLong(req.getParameter("wfcHeapSize")));
            }
        }

        // If a slave, push change out to master.
        if (updateRemote && DataShopInstance.isSlave()) {
            updateRemoteInstance(req, dsInstance.getRemoteInstanceId());
        }

        DataShopInstanceDao dao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        dao.saveOrUpdate(dsInstance);

        // Refresh
        DataShopInstance.initialize();

        ManageInstanceDto dto = getDto(dsInstance);

        req.setAttribute("manageInstanceDto", dto);

        getServletContext().getRequestDispatcher(MANAGE_INSTANCE_JSP).forward(req, resp);
    }

    /** One DataShopInstanceItem per server. */
    private static final Long DATASHOP_INSTANCE_ID = 1L;

    /**
     * Helper method to get the single DataShopInstance for this server.
     * @return DataShopInstanceItem the instance
     */
    private DataShopInstanceItem getInstance() {
        DataShopInstanceDao dao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        DataShopInstanceItem result = dao.get(DATASHOP_INSTANCE_ID);

        if (result.getMasterUser() != null) {
            result.setMasterUser(getUser((String)result.getMasterUser().getId()));
        }
        return result;
    }

    /**
     * Get a ManageInstanceDto given a DataShopInstanceItem
     * @param dsInstance the DataShopInstanceItem
     * @return ManageInstanceDto the DTO
     */
    private ManageInstanceDto getDto(DataShopInstanceItem dsInstance) {

        ManageInstanceDto dto = new ManageInstanceDto();
        dto.setConfiguredBy(dsInstance.getConfiguredBy());
        dto.setConfiguredTime(DATE_FORMAT.format(dsInstance.getConfiguredTime()));
        dto.setDatashopUrl(dsInstance.getDatashopUrl());
        // Instance properties.
        dto.setIsSlave(dsInstance.getIsSlave());
        dto.setSlaveId(dsInstance.getSlaveId());
        if (dsInstance.getMasterUser() != null) {
            UserItem masterUser = getUser((String)dsInstance.getMasterUser().getId());
            if (masterUser != null) {
                dto.setMasterUser((String)masterUser.getId());
            }
        }
        dto.setMasterUrl(dsInstance.getMasterUrl());
        dto.setMasterSchema(dsInstance.getMasterSchema());
        dto.setSlaveApiToken(dsInstance.getSlaveApiToken());
        dto.setSlaveSecret(dsInstance.getSlaveSecret());
        // Email properties.
        dto.setIsSendmailActive(dsInstance.getIsSendmailActive());
        dto.setDatashopHelpEmail(dsInstance.getDatashopHelpEmail());
        dto.setDatashopRmEmail(dsInstance.getDatashopRmEmail());
        dto.setDatashopBucketEmail(dsInstance.getDatashopBucketEmail());
        dto.setDatashopSmtpHost(dsInstance.getDatashopSmtpHost());
        if (dsInstance.getDatashopSmtpPort() != null) {
            dto.setDatashopSmtpPort(dsInstance.getDatashopSmtpPort().toString());
        } else {
            dto.setDatashopSmtpPort(null);
        }
        dto.setUseSslSmtp(dsInstance.getUseSslSmtp());
        dto.setDatashopSmtpUser(dsInstance.getDatashopSmtpUser());
        dto.setDatashopSmtpPassword(dsInstance.getDatashopSmtpPassword());
        // GitHub properties
        dto.setGithubClientId(dsInstance.getGithubClientId());
        dto.setGithubClientSecret(dsInstance.getGithubClientSecret());
        // Workflow properties
        dto.setWfcDir(dsInstance.getWfcDir());
        dto.setWfcRemote(dsInstance.getWfcRemote());
        dto.setWfcHeapSize(dsInstance.getWfcHeapSize());

        return dto;
    }

    /**
     * Helper method to get UserItem for named user.
     * @param userName the name of the user
     * @return UserItem the item
     */
    private UserItem getUser(String userName) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        return userDao.get(userName);
    }

    /**
     * Helper method to push the RemoteInstance attr changes out to the master.
     * @param req the HttpServletRequest
     * @param instanceId the remote instance id
     * @throws IOException an IO exception
     * @throws ServletException a servlet exception
     */
    private void updateRemoteInstance(HttpServletRequest req, Long instanceId)
        throws IOException, ServletException
    {
        String name = req.getParameter("slaveId");
        String url = req.getParameter("datashopUrl");

        if ((name == null) || (name.length() == 0)) {
            req.getSession().setAttribute(MESSAGE, "Error. The instance 'name' must be set.");
            req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
            return;
        }
        if ((url == null) || (url.length() == 0)) {
            req.getSession().setAttribute(MESSAGE,
                                          "Error. The instance 'DataShop URL' must be set.");
            req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
            return;
        }

        try {
            DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
            if (client != null) {
                String response = client.getInstanceSet(instanceId, name, url);
                Integer resultCode = getResultCode(response);
                if ((resultCode != null) && (resultCode == 0)) {
                    String msg = "Successfully updated this remote instance.";
                    req.getSession().setAttribute(MESSAGE, msg);
                    req.getSession().setAttribute(MESSAGE_LEVEL, SUCCESS);
                } else if ((resultCode != null) && (resultCode != 0)) {
                    String resultMessage = getResultMessage(response);
                    req.getSession().setAttribute(MESSAGE, resultMessage);
                    req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
                } else {  // resultCode = null
                    String msg = "Failed to update the remote instance on the master DataShop "
                        + "for instance '" + instanceId + "'.";
                    req.getSession().setAttribute(MESSAGE, msg);
                    req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
                }

            }
        } catch (Exception e) {
            String msg = "Failed to push remote instance changes to the master DataShop instance.";
            logger.info(msg, e);
            req.getSession().setAttribute(MESSAGE, msg);
            req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
        }
    }

    /**
     * Helper method to get the RemoteInstanceItem for the specified id.
     * @param req the HttpServletRequest
     * @param instanceId the id for the instance
     * @return RemoteInstanceItem the item
     */
    private RemoteInstanceItem getRemoteInstance(HttpServletRequest req, Long instanceId) {
        RemoteInstanceItem result = null;

        StringBuffer path = new StringBuffer();
        path.append("/instances/").append(instanceId);

        String errorMsg = "Failed to retrieve remote instance state from the master DataShop "
            + "for instance '" + instanceId + "'.";

        try {
            DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
            if (client != null) {
                String response = client.getService(path.toString(), "text/xml");
                Integer resultCode = getResultCode(response);
                if ((resultCode != null) && (resultCode == 0)) {
                    result = getRemoteInstance(response, instanceId);
                } else if (resultCode != 0) {
                    String resultMessage = getResultMessage(response);
                    req.getSession().setAttribute(MESSAGE, resultMessage);
                    req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
                    result = null;
                } else {
                    req.getSession().setAttribute(MESSAGE, errorMsg);
                    req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
                    result = null;
                }
            }
        } catch (Exception e) {
            req.getSession().setAttribute(MESSAGE, errorMsg);
            req.getSession().setAttribute(MESSAGE_LEVEL, ERROR);
            logger.info("Failed to get RemoteInstance: " + instanceId, e);
            result = null;
        }

        return result;
    }

    /**
     * Parses the xml response from a web service call and returns the result code.
     * @param responseXml the response from a web service call
     * @return Integer the result code
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private Integer getResultCode(String responseXml)
        throws IOException, JDOMException
    {
        SAXBuilder builder = new SAXBuilder();
        StringReader reader = null;
        Element rootElement = null;

        try {
            reader = new StringReader(responseXml);
            rootElement = builder.build(reader).getRootElement();
        } catch (Exception e) {
            logger.error("Failed to parse the XML response.", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (rootElement == null) {
            logger.error("Failed to parse the XML response.");
            return null;
        }

        String resultCodeString = rootElement.getAttributeValue("result_code");
        if (resultCodeString == null) {
            logger.error("No response code found in the web service response.");
            return null;
        }
        Integer resultCode = Integer.valueOf(resultCodeString);

        return resultCode;
    }

    /**
     * Parses the xml response from a web service call and returns the result message.
     * @param responseXml the response from a web service call
     * @return String the result message
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private String getResultMessage(String responseXml)
        throws IOException, JDOMException
    {
        SAXBuilder builder = new SAXBuilder();
        StringReader reader = null;
        Element rootElement = null;

        try {
            reader = new StringReader(responseXml);
            rootElement = builder.build(reader).getRootElement();
        } catch (Exception e) {
            logger.error("Failed to parse the XML response.", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (rootElement == null) {
            logger.error("Failed to parse the XML response.");
            return null;
        }


        return rootElement.getAttributeValue("result_message");
    }

    /**
     * Helper function to parse XML response and generate RemoteInstanceItem.
     * @param responseXml the response from a web service call
     * @param instanceId the remote instance id
     * @return RemoteInstanceItem the instance item
     * @throws IOException when parsing the response string fails
     * @throws JDOMException when parsing the response string fails
     */
    private RemoteInstanceItem getRemoteInstance(String responseXml, Long instanceId)
        throws IOException, JDOMException
    {
        RemoteInstanceItem result = null;

        SAXBuilder builder = new SAXBuilder();
        StringReader reader = null;
        Element rootElement;

        try {
            reader = new StringReader(responseXml);
            rootElement = builder.build(reader).getRootElement();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        Element riNode = rootElement.getChild("remote_instance");
        if (riNode != null) {
            result = new RemoteInstanceItem();
            result.setId(instanceId);
            result.setName(riNode.getChildTextTrim("name"));
            result.setDatashopUrl(riNode.getChildTextTrim("datashop_url"));
        }

        return result;
    }
}
