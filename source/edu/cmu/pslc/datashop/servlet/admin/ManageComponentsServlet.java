/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
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
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.servlet.AbstractServlet;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.webservices.DatashopClient;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentDTO;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceException.paramValueMissingException;

/**
 * Class that provides manage component functionalities to the admin.
 * @author
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageComponentsServlet extends AbstractServlet {

    /** Title for the components page - "Tigris Components". */
    public static final String SERVLET_LABEL = "Tigris Components";

    /** Action names to decide which operation to execute. **/
    public static final String SERVLET_NAME = "ManageComponents";

    /** JSP Page for the forwarding **/
    private static final String MANAGE_INSTANCE_JSP = "/jsp_admin/admin_wf_components.jsp";

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

        UserItem loggedInUserItem = getLoggedInUserItem(req);
        RequestDispatcher disp = getServletContext().getRequestDispatcher(MANAGE_INSTANCE_JSP);

        String requestingMethod = req.getParameter("requestingMethod");
        if (requestingMethod != null && loggedInUserItem.getAdminFlag()) {
            if (requestingMethod.equalsIgnoreCase("updateComponentField")
                    || requestingMethod.equalsIgnoreCase("refreshComponentField")) {

                String fieldId = req.getParameter("fieldId");
                String value = req.getParameter("value");
                String componentIdStr = req.getParameter("componentId");
                Long componentId = null;

                if (componentIdStr != null && componentIdStr.matches("[0-9]+")) {
                    componentId = Long.parseLong(componentIdStr);
                }

                if (fieldId != null && componentId != null && value != null) {

                    WorkflowComponentItem wfcItem = null;
                    WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
                    wfcItem = wfcDao.get(componentId);

                    if (wfcItem != null) {
                        if (requestingMethod.equalsIgnoreCase("refreshComponentField")) {
                            String existingValue = null;
                            if (fieldId.equalsIgnoreCase("enabled")) {
                                existingValue = wfcItem.getEnabled().toString();
                            } else if (fieldId.equalsIgnoreCase("remoteExecEnabled")) {
                                existingValue = wfcItem.getRemoteExecEnabled().toString();
                            } else if (fieldId.equalsIgnoreCase("author")) {
                                existingValue = wfcItem.getAuthor();
                            } else if (fieldId.equalsIgnoreCase("toolDir")) {
                                existingValue = wfcItem.getToolDir();
                            } else if (fieldId.equalsIgnoreCase("schemaPath")) {
                                existingValue = wfcItem.getSchemaPath();
                            } else if (fieldId.equalsIgnoreCase("toolPath")) {
                                existingValue = wfcItem.getToolPath();
                            } else if (fieldId.equalsIgnoreCase("citation")) {
                                existingValue = wfcItem.getCitation();
                            } else {
                                existingValue = "INVALID FIELD";
                            }

                            try {
                                writeJSON(
                                        resp,
                                        json("componentId", componentId, "fieldId", fieldId, "value", existingValue));
                            } catch (JSONException e) {
                                logger.error("Could not save workflow_component field " + fieldId
                                    + " for component (" + componentId + ")");
                            }
                            return;
                        } else if (requestingMethod.equalsIgnoreCase("updateComponentField")) {
                            if (fieldId.equalsIgnoreCase("enabled")) {
                                wfcItem.setEnabled(Boolean.parseBoolean(value));
                            } else if (fieldId.equalsIgnoreCase("remoteExecEnabled")) {
                                wfcItem.setRemoteExecEnabled(Boolean.parseBoolean(value));
                            } else if (fieldId.equalsIgnoreCase("author")) {
                                wfcItem.setAuthor(value);
                            } else if (fieldId.equalsIgnoreCase("toolDir")) {
                                wfcItem.setToolDir(value);
                            } else if (fieldId.equalsIgnoreCase("schemaPath")) {
                                wfcItem.setSchemaPath(value);
                            } else if (fieldId.equalsIgnoreCase("toolPath")) {
                                wfcItem.setToolPath(value);
                            } else if (fieldId.equalsIgnoreCase("citation")) {
                                wfcItem.setCitation(value);
                            }

                            wfcDao.saveOrUpdate(wfcItem);
                            try {
                                writeJSON(
                                        resp,
                                        json("componentId", componentId, "fieldId", fieldId, "value", value));
                            } catch (JSONException e) {
                                logger.error("Could not save workflow_component field " + fieldId
                                    + " for component (" + componentId + ")");
                            }
                            return;
                        }
                    }
                }
            }
        }
        try {
            List<WorkflowComponentDTO> wfcDtoList = new ArrayList<WorkflowComponentDTO>();
            WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
            List<WorkflowComponentItem> wfcItems = wfcDao.findAll();
            for (WorkflowComponentItem wfcItem : wfcItems) {
                WorkflowComponentDTO wfcDto = createWorkflowComponentDto(wfcItem);
                wfcDtoList.add(wfcDto);
            }
            req.setAttribute("wfcDtoList", wfcDtoList);
            logger.debug("Forwarding to JSP " + MANAGE_INSTANCE_JSP);
            disp.forward(req, resp);
            return;

        } catch (Exception exception) {
            forwardError(req, resp, logger, exception);
        } finally {
            logDebug("doPost end");
        }
    }

    private WorkflowComponentDTO createWorkflowComponentDto(WorkflowComponentItem wfcItem) {
        WorkflowComponentDTO wfcDto = new WorkflowComponentDTO();
        wfcDto.setComponentId((Long) wfcItem.getId());
        wfcDto.setAuthor(wfcItem.getAuthor());
        wfcDto.setCitation(wfcItem.getCitation());
        wfcDto.setComponentName(wfcItem.getComponentName());
        wfcDto.setComponentType(wfcItem.getComponentType());
        wfcDto.setEnabled(wfcItem.getEnabled());
        wfcDto.setInterpreterPath(wfcItem.getInterpreterPath());
        wfcDto.setRemoteExecEnabled(wfcItem.getRemoteExecEnabled());
        wfcDto.setSchemaPath(wfcItem.getSchemaPath());
        wfcDto.setToolDir(wfcItem.getToolDir());
        wfcDto.setToolPath(wfcItem.getToolPath());
        wfcDto.setVersion(wfcItem.getVersion());
        return wfcDto;
    }

    private void listComponents(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub

    }


}
