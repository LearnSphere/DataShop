package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.AccessRequestHistoryDao;
import edu.cmu.pslc.datashop.dao.AccessRequestStatusDao;
import edu.cmu.pslc.datashop.dao.AuthorizationDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ImportQueueDao;
import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowHistoryDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagDao;
import edu.cmu.pslc.datashop.dao.WorkflowTagMapDao;
import edu.cmu.pslc.datashop.item.AccessRequestHistoryItem;
import edu.cmu.pslc.datashop.item.AccessRequestStatusItem;
import edu.cmu.pslc.datashop.item.AuthorizationItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ImportQueueItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.workflows.AbstractComponent;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowHistoryItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

public class WorkflowIfaceHelper {

       /** Debug logging. */
       private Logger logger = Logger.getLogger(getClass().getName());
       public static final String COMPONENT_ID_PATTERN = "[a-zA-Z0-9_\\-]+-1-x[0-9]{6}";

       /** Regular expression used to check email addresses. */
       public static final String EMAIL_PATTERN =
           "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9\\-]+)*@"
           + "[A-Za-z0-9\\-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    /**
     * Helper method to initialize a data access JS object for the workflow row JS object.
     * @param context the WorkflowContext
     * @param userItem the logged in user
     * @param wfRowJson the workflow row JS object (contains id, name, globalFlag, and ownerId)
     * @param isLoggedIn
     * @return the data access JS object
  * @throws JSONException
     */
     public JSONObject initializeWorkflowsJson(WorkflowContext context,
                                                         UserItem userItem,
                                                         JSONObject wfRowJson,
                                                         Boolean isLoggedIn)  {

     JSONObject row = new JSONObject();

     try {
        logger.debug("Fetching access data for workflow ("
            + wfRowJson.get("id") + ") " + wfRowJson.get("name") + ".");

        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

        ComponentFilePersistenceDao cfpDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
        WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        WorkflowHistoryDao workflowHistoryDao = DaoFactory.DEFAULT.getWorkflowHistoryDao();
        WorkflowTagDao workflowTagDao = DaoFactory.DEFAULT.getWorkflowTagDao();
        WorkflowTagMapDao workflowTagMapDao = DaoFactory.DEFAULT.getWorkflowTagMapDao();

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = (Date) cal.getTime();

        Boolean showRequestFlag = (context != null) ?
            Boolean.parseBoolean(context.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_REQUEST)) : null;
        Boolean showTemplatesFlag = (context != null) ?
            Boolean.parseBoolean(context.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_NO_AUTH)) : null;
        Boolean showSharedFlag = (context != null) ?
            Boolean.parseBoolean(context.getSearchAttribute(WorkflowContext.WF_SEARCH_ACCESS_SHARED)) : null;

        Boolean hasPapers = (context != null) ?
                Boolean.parseBoolean(context.getSearchAttribute(WorkflowContext.WF_HAS_PAPERS)) : null;

        Long thisWorkflowId = null;
        try {
            thisWorkflowId = (Long) wfRowJson.get("id");
        } catch (NumberFormatException nfe) {
            logger.error("initializeWorkflowsJson:: Invalid workflow id: " + thisWorkflowId);
        }

        Boolean globalFlag = (Boolean) wfRowJson.get("globalFlag");
        String ownerId = (String) wfRowJson.get("ownerId");
        String workflowName = (String) wfRowJson.get("name");

        Boolean hasUnownedPrivateFiles = false;
        List<ProjectItem> unrequestedProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> accessibleProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> activeRequestProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> reRequestProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> nonShareableProjects = new ArrayList<ProjectItem>();
        WorkflowItem workflowItem = workflowDao.get(thisWorkflowId);
        // Check all of the imports for dataset associations
        List<ComponentFilePersistenceItem> cfpItems = cfpDao.findByWorkflow(workflowItem);
        if (cfpItems != null && !cfpItems.isEmpty()) {
            for (ComponentFilePersistenceItem cfpItem : cfpItems) {
                    String componentName = getComponentName(workflowItem, cfpItem);

                if (cfpItem.getDataset() != null
                        && cfpItem.getComponentId() != null &&
                        (cfpItem.getComponentId().matches("Import.*") || componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME))) {
                    DatasetItem datasetItem = datasetDao.get((Integer) cfpItem.getDataset().getId());
                    if (datasetItem.getProject() != null) {
                        ProjectItem projectItem = projectDao.get((Integer) datasetItem.getProject().getId());

                        String permLevel = authDao.getAuthLevel(
                                (String) userItem.getId(), (Integer) projectItem.getId());
                        Boolean hasProjectPermissions = permLevel != null &&
                                (permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_VIEW)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_PUBLIC)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN));
                        if (projectItem.getShareableStatus().equalsIgnoreCase(
                                 ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                                           || projectItem.getShareableStatus().equalsIgnoreCase(
                                                   ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
                                           || hasProjectPermissions) {

                            if (!unrequestedProjects.contains(projectItem) && !hasProjectPermissions) {
                                // Import uses tracked files
                                AccessRequestStatusItem arStatus = arStatusDao.findByUserAndProject(userItem, projectItem);
                                AccessRequestHistoryItem lastRequest = null;

                                if (arStatus != null) {
                                    // Get last request
                                    lastRequest = arHistoryDao.findLastRequest(arStatus);
                                    if (lastRequest != null) {
                                        Date lastRequestDate = lastRequest.getDate();
                                        Boolean isDenied = false;
                                        if (arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                                              || arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                                              || arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                                            isDenied = true;
                                        }
                                        if (lastRequestDate.after(yesterday) && !isDenied) {
                                            activeRequestProjects.add(projectItem);
                                        } else {
                                            reRequestProjects.add(projectItem);
                                        }
                                    }
                                } else {
                                    unrequestedProjects.add(projectItem);
                                }

                            } else if (!unrequestedProjects.contains(projectItem) && hasProjectPermissions) {
                                accessibleProjects.add(projectItem);
                            }
                        } else if (!projectItem.getShareableStatus().equalsIgnoreCase(
                                 ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                                           && !projectItem.getShareableStatus().equalsIgnoreCase(
                                                   ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)) {
                            nonShareableProjects.add(projectItem);
                        }
                    }
                } else if (cfpItem.getDataset() == null
                        && cfpItem.getComponentId() != null &&
                        (cfpItem.getComponentId().matches("Import.*") || componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME))) {
                    // Import uses private files
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    WorkflowFileItem itemExists = wfFileDao.find((Integer) cfpItem.getFile().getId());
                    if (itemExists != null) {
                        WorkflowFileItem wfFileItem = null;
                        wfFileItem = wfFileDao.get((Integer) itemExists.getId());
                        if (wfFileItem != null && wfFileItem.getOwner() != null) {
                            String wfFileOwnerId = (String) wfFileItem.getOwner().getId();
                            if (!wfFileOwnerId.equals(userItem.getId())) {
                                hasUnownedPrivateFiles = true;
                            }
                        }
                    }
                }
            }
        }

        row.put("hasUnownedPrivateFiles", hasUnownedPrivateFiles);

        // Add actionable name field: non-owners get view only.
        StringBuffer actionableName = new StringBuffer("<a id=\"");
        actionableName.append("workflowName_");
        actionableName.append(thisWorkflowId)
            .append("\" title=\"").append(workflowName).append("\"");
        actionableName.append("\" href=\"javascript:");

        Boolean cancelIconAdded = false;

        // Add actionable icon field
        StringBuffer icons = new StringBuffer("<span class=\"icons\">");
        if (userItem.getId().equals(UserItem.DEFAULT_USER) && globalFlag) {
            // User is not workflow owner... others can view
            actionableName.append("wfViewWorkflow");
        } else if (userItem.getAdminFlag() && !userItem.getId().equals(ownerId)) {
            // DS Admin

            icons.append("<span class=\"wfSaveAsNewIconContainer\"><a class=\"ls_wf-add\" "
                         + "title=\"Save as New Workflow\" href=\"javascript:wfSaveAsNewWorkflow('" +
                         + thisWorkflowId + "');\">Save Workflow</a></span>");
            icons.append("<a class=\"ls_wf-delete\" title=\"Delete Workflow\" "
                         + "href=\"javascript:wfDeleteWorkflow('" +
                         + thisWorkflowId + "');\">Delete Workflow</a>");
            // others can view
            actionableName.append("wfViewWorkflow");

        } else if (userItem.getId().equals(ownerId)) {
            // User is workflow owner
            if (workflowItem.getState() != null
                    && workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                icons.append("<a id=\"wf-cancel_" + thisWorkflowId + "\" class=\"ls_wf-cancel\" title=\"Cancel Workflow\" "
                    + "href=\"javascript:cancelWorkflow('" +
                    + thisWorkflowId + "');\">Cancel Workflow</a>");
                cancelIconAdded = true;
            }
            icons.append("<span class=\"wfEditIconContainer\"><a class=\"ls_wf-edit\" "
                         + "title=\"Edit Workflow\" href=\"javascript:wfEditWorkflow('" +
                         + thisWorkflowId + "');\">Edit Workflow</a></span>");
            icons.append("<span class=\"wfSaveAsNewIconContainer\"><a class=\"ls_wf-add\" "
                         + "title=\"Save as New Workflow\" href=\"javascript:wfSaveAsNewWorkflow('" +
                         + thisWorkflowId + "');\">Save Workflow</a></span>");
            icons.append("<a class=\"ls_wf-delete\" title=\"Delete Workflow\" "
                         + "href=\"javascript:wfDeleteWorkflow('" +
                         + thisWorkflowId + "');\">Delete Workflow</a>");

            row.put("pencilIcon", "<a class=\"ls_wf-pencil\" title=\"Rename Workflow\" "
                    + "href=\"javascript:wfModifyWorkflowSettings('" +
                    + thisWorkflowId + "');\">Rename Workflow</a>");
            // owner can edit
            actionableName.append("wfEditWorkflow");

        } else if (!userItem.getId().equals(ownerId) && globalFlag) {
            // User is not workflow owner

            if (isLoggedIn) {
                icons.append("<span class=\"wfSaveAsNewIconContainer\"><a class=\"ls_wf-add\" "
                             + "title=\"Save as New Workflow\" href=\"javascript:wfSaveAsNewWorkflow('" +
                     + thisWorkflowId + "');\">Save Workflow</a></span>");
            }

            // others can view
            actionableName.append("wfViewWorkflow");

        }

        if (userItem.getAdminFlag() && !cancelIconAdded && workflowItem.getState() != null
                && workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
            icons.append("<a id=\"wf-cancel_" + thisWorkflowId + "\" class=\"ls_wf-cancel\" title=\"Cancel Workflow\" "
                + "href=\"javascript:cancelWorkflow('" +
                + thisWorkflowId + "');\">Cancel Workflow</a>");
        }

        icons.append("</span>");

        row.put("actionableIcons", icons.toString());

        StringBuffer displayName = new StringBuffer();
        if (workflowName.length() > 25) {
            displayName.append(workflowName.substring(0, 25));
            displayName.append("...");
        } else {
            displayName.append(workflowName);
        }

        actionableName.append("('" + thisWorkflowId + "');\">");
        actionableName.append(displayName.toString() + "</a>");
        row.put("actionableName", actionableName.toString());

        // Moving logic from learnsphere.jsp since it's needed in multiple locations.

        WorkflowRowDto worfklowRowDto = new WorkflowRowDto();
        worfklowRowDto.setOwnerId(ownerId);
        worfklowRowDto.setIsGlobal(globalFlag);
        worfklowRowDto.setWorkflowId(thisWorkflowId);
        worfklowRowDto.setWorkflowName(workflowName);
        worfklowRowDto.setProjects(unrequestedProjects);
        worfklowRowDto.setAccessibleProjects(accessibleProjects);
        worfklowRowDto.setReRequestProjects(reRequestProjects);
        worfklowRowDto.setNonShareableProjects(nonShareableProjects);
        worfklowRowDto.setActiveRequestProjects(activeRequestProjects);
        worfklowRowDto.setHasUnownedPrivateFiles(hasUnownedPrivateFiles);
        //worfklowRowDto.setWorkflowXml(wfItem.getWorkflowXml());

        String dataAccessHtml = isLoggedIn ? getDataAccessAsHtml(worfklowRowDto) : "";
        row.put("dataAccessHtml", dataAccessHtml);

        row.put("id", (Long)workflowItem.getId());

        // Has workflow history?
        List<WorkflowHistoryItem> hasWorkflowHistory = workflowHistoryDao.find(workflowItem);
        if (hasWorkflowHistory != null && !hasWorkflowHistory.isEmpty()) {
            row.put("hasWorkflowHistory", true);
        } else {
            row.put("hasWorkflowHistory", false);
        }

        logger.trace("Returning workflows table.");
     } catch (JSONException e) {
         logger.error(e.toString());
     }
        return row;
    }

     /**
      * Helper method to create the HTML for the 'Data Access' columns
      * on the main Workflows page.
      * @param workflowRow the DTO
      */
     private String getDataAccessAsHtml(WorkflowRowDto workflowRow) {
         StringBuffer result = new StringBuffer();

         Long workflowId = workflowRow.getWorkflowId();

         Boolean isLocked = false;
         if ((workflowRow.getHasUnownedPrivateFiles() != null && workflowRow.getHasUnownedPrivateFiles())
             || (workflowRow.getNonShareableProjects() != null && !workflowRow.getNonShareableProjects().isEmpty())) {
             isLocked = true;
         }

         StringBuffer projectBuffer = new StringBuffer();
         StringBuffer projectNameBuffer = new StringBuffer();
         Integer projectCount = 0;

         // Workflow contains non-shareable files
         String requestButtonState = null;
         if (requestButtonState == null
             && workflowRow.getProjects() != null && !workflowRow.getProjects().isEmpty()) {

             requestButtonState = !isLocked ? "Request" : "Request*";

             projectCount = 0;
             projectNameBuffer.append("Access Required: ");

             for (ProjectItem project : workflowRow.getProjects()) {
                 projectBuffer.append(project.getId());
                 projectNameBuffer.append(WorkflowFileUtils.htmlEncode(project.getProjectName()));
                 if (projectCount < workflowRow.getProjects().size() - 1) {
                     projectBuffer.append(",");
                     projectNameBuffer.append(", ");
                 }
                 projectCount++;
             }

             result.append("<a href=\"#\" title=\"").append(projectNameBuffer.toString()).append("\"")
                 .append(" id=\"wfAccessRequestRow_").append(workflowId).append("\"")
                 .append(" class=\"wfAccessRequestLink wfAccess request\">")
                 .append(requestButtonState).append("</a>");
             result.append("<input type=\"hidden\" id=\"wfAccessRequestProjectList_")
                 .append(workflowId).append("\"")
                 .append("value=\"").append(projectBuffer.toString()).append("\" />");

         } else if (requestButtonState == null
                    && workflowRow.getReRequestProjects() != null
                    && !workflowRow.getReRequestProjects().isEmpty()) {

             // Workflow contains old requests (older than 24 hours)

             requestButtonState = !isLocked ? "Re-Request" : "Re-Request*";

             projectBuffer = new StringBuffer();
             projectNameBuffer = new StringBuffer();
             projectCount = 0;

             projectNameBuffer.append("Access Required: ");

             for (ProjectItem project : workflowRow.getReRequestProjects()) {
                 projectBuffer.append(project.getId());
                 projectNameBuffer.append(WorkflowFileUtils.htmlEncode(project.getProjectName()));
                 if (projectCount < workflowRow.getReRequestProjects().size() - 1) {
                     projectBuffer.append(",");
                     projectNameBuffer.append(", ");
                 }
                 projectCount++;
             }

             result.append("<a href=\"#\" title=\"").append(projectNameBuffer.toString()).append("\"")
                 .append(" id=\"wfAccessRequestRow_").append(workflowId).append("\"")
                 .append(" class=\"wfAccessRequestLink wfAccess request\">")
                 .append(requestButtonState).append("</a>");
             result.append("<input type=\"hidden\" id=\"wfAccessRequestProjectList_")
                 .append(workflowId).append("\"")
                 .append("value=\"").append(projectBuffer.toString()).append("\" />");

         } else if (requestButtonState == null
                    && workflowRow.getActiveRequestProjects() != null
                    && !workflowRow.getActiveRequestProjects().isEmpty()) {

             // Workflow contains pending access requests

             requestButtonState = !isLocked ? "Pending" : "Pending*";

             projectBuffer = new StringBuffer();
             projectNameBuffer = new StringBuffer();
             projectCount = 0;

             projectNameBuffer.append("Pending Requests: ");
             for (ProjectItem project : workflowRow.getActiveRequestProjects()) {
                 projectBuffer.append(project.getId());
                 projectNameBuffer.append(WorkflowFileUtils.htmlEncode(project.getProjectName()));
                 if (projectCount < workflowRow.getActiveRequestProjects().size() - 1) {
                     projectBuffer.append(",");
                     projectNameBuffer.append(", ");
                 }
                 projectCount++;
             }

             result.append("<a href=\"AccessRequests\" target=\"_blank\">")
                 .append("<a href=\"#\" title=\"").append(projectNameBuffer.toString()).append("\"")
                 .append(" id=\"wfAccessRequestPendingRow_").append(workflowId).append("\"")
                 .append(" class=\"wfAccessRequestPendingLink wfAccess pending\">")
                 .append(requestButtonState).append("</a></a>");
             result.append("<input type=\"hidden\" id=\"wfAccessRequestPendingProjectList_")
                 .append(workflowId).append("\"")
                 .append("value=\"").append(projectBuffer.toString()).append("\" />");

         } else if ((workflowRow.getNonShareableProjects() != null
                     && !workflowRow.getNonShareableProjects().isEmpty())
                    || (workflowRow.getHasUnownedPrivateFiles())) {
             String lockedString = null;
             String lockedTitle = null;
             if (workflowRow.getAccessibleProjects() != null && !workflowRow.getAccessibleProjects().isEmpty()) {
                 lockedString = "Locked*";
                 lockedTitle = "This workflow contains both private and accessible data.";
             } else {
                 lockedString = "Locked";
                 lockedTitle = "This workflow contains private data.";
             }
             //Restricted
             result.append("<span title=\"").append(lockedTitle).append("\"")
                 .append("class=\"wfAccess locked\">").append(lockedString).append("</span>");

         } else {
             // I already have access to the data.
             if (workflowRow.getIsGlobal()) {
                 result.append("<span id=\"").append("WorkflowGlobalTag_")
                     .append(workflowId).append("\"")
                     .append(" title=\"This workflow data is shared or public.\" class=\"wfAccess public\">SHARED</span>");
             } else {
                 result.append("<span id=\"").append("WorkflowGlobalTag_")
                     .append(workflowId).append("\"")
                     .append(" title=\"This workflow data is private.\" class=\"wfAccess private\">PRIVATE</span>");
             }
         }

         return result.toString();
     }


     /**
      * Helper method to get 'Data Access'. Similar to getDataAccessAsHtml but combined
      * request+re-request+pending into request and request*+re-request*+pending* into request*
      * @param workflowRow the DTO
      * @return: possible values: locked, locked*, request, request*, shared, public
      */
     public static String getDataAccess(WorkflowRowDto workflowRow) {
         String result = null;

         Long workflowId = workflowRow.getWorkflowId();

         Boolean isLocked = false;
         if ((workflowRow.getHasUnownedPrivateFiles() != null && workflowRow.getHasUnownedPrivateFiles())
             || (workflowRow.getNonShareableProjects() != null && !workflowRow.getNonShareableProjects().isEmpty())) {
             isLocked = true;
         }

         // Workflow contains non-shareable files
         String requestButtonState = null;
         if (requestButtonState == null
             && workflowRow.getProjects() != null && !workflowRow.getProjects().isEmpty()) {

             requestButtonState = !isLocked ? "request" : "request*";
             result = requestButtonState;

         } else if (requestButtonState == null
                    && workflowRow.getReRequestProjects() != null
                    && !workflowRow.getReRequestProjects().isEmpty()) {

             // Workflow contains old requests (older than 24 hours)

             requestButtonState = !isLocked ? "request" : "request*";

             result = requestButtonState;

         } else if (requestButtonState == null
                    && workflowRow.getActiveRequestProjects() != null
                    && !workflowRow.getActiveRequestProjects().isEmpty()) {

             // Workflow contains pending access requests

             requestButtonState = !isLocked ? "request" : "request*";

             result = requestButtonState;

         } else if ((workflowRow.getNonShareableProjects() != null
                     && !workflowRow.getNonShareableProjects().isEmpty())
                    || (workflowRow.getHasUnownedPrivateFiles())) {
             String lockedString = null;
             if (workflowRow.getAccessibleProjects() != null && !workflowRow.getAccessibleProjects().isEmpty()) {
                 lockedString = "locked*";
             } else {
                 lockedString = "locked";
             }
             //Restricted
             result = lockedString;

         } else {
             // I already have access to the data.
             if (workflowRow.getIsGlobal()) {
                 result = "shared";
             } else {
                 result= "private";
             }
         }

         return result;
     }


    /**
      * Get the component name from a ComponentFilePersistenceItem
      */
     public static String getComponentName(WorkflowItem workflowItem, ComponentFilePersistenceItem cfpItem) {
         String componentName = "";
         String componentId = cfpItem.getComponentId();
         try {
             SAXBuilder saxBuilder = new SAXBuilder();
             saxBuilder.setReuseParser(false);
             InputStream stream = new ByteArrayInputStream(workflowItem
                     .getWorkflowXml().getBytes("UTF-8"));
             Document digraphDom = saxBuilder.build(stream);
             stream.close();
             Element digraphDoc = digraphDom.getRootElement();

             List<Element> components = WorkflowXmlUtils.getNodeList(digraphDoc,
                     "/workflow/components/component");

             for (int i = 0; i < components.size(); i++) {
                 Element component = components.get(i);
                 Element componentIdEl = component.getChild("component_id");
                 Element componentNameEl = component.getChild("component_name");
                 if (componentIdEl != null && componentNameEl != null) {
                     if (componentIdEl.getText().equals(componentId)) {
                         componentName = componentNameEl.getText();
                         break;
                     }
                 }
             }

         } catch (Exception e) {

         }
         return componentName;
     }


    public static String getComponentId(HttpServletRequest req) {
         String componentIdInput = (String) req.getParameter("componentId");
         return getComponentId(componentIdInput);
     }

     public static String getComponentId(String componentIdInput) {
         String componentIdOutput = null;
         if (componentIdInput != null && componentIdInput.matches(COMPONENT_ID_PATTERN)) {
             componentIdOutput = componentIdInput;
         }
         return componentIdOutput;
     }

     public static String getComponentType(HttpServletRequest req) {
         String componentTypeInput = (String) req.getParameter("componentType");
         return getComponentType(componentTypeInput);
     }

     public static String getComponentType(String componentTypeInput) {
         String componentTypeOutput = null;
         if (componentTypeInput != null) {
             componentTypeOutput = componentTypeInput.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
         }
         return componentTypeOutput;
     }

     public static String getComponentName(HttpServletRequest req) {
         String componentNameInput = (String) req.getParameter("componentName");
         return getComponentName(componentNameInput);
     }

     public static String getComponentName(String componentNameInput) {
         String componentNameOutput = null;
         if (componentNameInput != null) {
             componentNameOutput = componentNameInput.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
         }
         return componentNameOutput;
     }

     /**
     * Gets the formatted user name and, optionally, the id.
     * @param user the user
     * @param showUserId whether or not to show the user id
     * @return the formatted user name and, optionally, the id; this function
     * can return null for null user items or when not first/last name are null
     * or empty and the user sets showUserId = false.
     */
    public static String getFormattedUserName(UserItem user, Boolean showUserId) {
        if (user == null) {
            return null;
        }
        String formattedUserName = null;
        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
            formattedUserName = user.getFirstName();
        }
        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
            formattedUserName += " " + user.getLastName();
        }
        if (showUserId) {
            String beginParen = "";
            String endParen = "";
            if (formattedUserName != null) {
                beginParen = " (";
                endParen = ")";
            } else {
                formattedUserName = new String();
            }
            formattedUserName += beginParen + user.getId() + endParen;
        }
        return formattedUserName;
    }


    public WorkflowRowDto getProjectRequestInfo(UserItem userItem, Long workflowId) {

        Boolean hasUnownedPrivateFiles = false;

        WorkflowRowDto wfRowDto = new WorkflowRowDto();
        ProjectDao projectDao = DaoFactory.DEFAULT.getProjectDao();
        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        AuthorizationDao authDao = DaoFactory.DEFAULT.getAuthorizationDao();

        ComponentFilePersistenceDao cfpDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
        WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();

        AccessRequestStatusDao arStatusDao = DaoFactory.DEFAULT.getAccessRequestStatusDao();
        AccessRequestHistoryDao arHistoryDao = DaoFactory.DEFAULT.getAccessRequestHistoryDao();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = (Date) cal.getTime();

        List<ProjectItem> unrequestedProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> accessibleProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> activeRequestProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> reRequestProjects = new ArrayList<ProjectItem>();
        List<ProjectItem> nonShareableProjects = new ArrayList<ProjectItem>();
        WorkflowItem workflowItem = workflowDao.get(workflowId);
        // Check all of the imports for dataset associations
        List<ComponentFilePersistenceItem> cfpItems = cfpDao.findByWorkflow(workflowItem);
        if (cfpItems != null && !cfpItems.isEmpty()) {
            for (ComponentFilePersistenceItem cfpItem : cfpItems) {
                    String componentName = WorkflowIfaceHelper.getComponentName(workflowItem, cfpItem);

                if (cfpItem.getDataset() != null
                        && cfpItem.getComponentId() != null &&
                        (cfpItem.getComponentId().matches("Import.*") || componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME))) {
                    DatasetItem datasetItem = datasetDao.get((Integer) cfpItem.getDataset().getId());
                    if (datasetItem.getProject() != null) {
                        ProjectItem projectItem = projectDao.get((Integer) datasetItem.getProject().getId());

                        String permLevel = authDao.getAuthLevel(
                                (String) userItem.getId(), (Integer) projectItem.getId());
                        Boolean hasProjectPermissions = permLevel != null &&
                                (permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_EDIT)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_VIEW)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_PUBLIC)
                                    || permLevel.equalsIgnoreCase(AuthorizationItem.LEVEL_ADMIN));
                        if (projectItem.getShareableStatus().equalsIgnoreCase(
                                 ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                                           || projectItem.getShareableStatus().equalsIgnoreCase(
                                                   ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
                                           || hasProjectPermissions) {

                            if (!unrequestedProjects.contains(projectItem) && !hasProjectPermissions) {
                                // Import uses tracked files
                                AccessRequestStatusItem arStatus = arStatusDao.findByUserAndProject(userItem, projectItem);
                                AccessRequestHistoryItem lastRequest = null;

                                if (arStatus != null) {
                                    // Get last request
                                    lastRequest = arHistoryDao.findLastRequest(arStatus);
                                    if (lastRequest != null) {
                                        Date lastRequestDate = lastRequest.getDate();
                                        Boolean isDenied = false;
                                        if (arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DENIED)
                                              || arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_PI_DENIED)
                                              || arStatus.getStatus()
                                                .equals(AccessRequestStatusItem.ACCESS_RESPONSE_STATUS_DP_DENIED)) {
                                            isDenied = true;
                                        }
                                        if (lastRequestDate.after(yesterday) && !isDenied) {
                                            activeRequestProjects.add(projectItem);
                                        } else {
                                            reRequestProjects.add(projectItem);
                                        }
                                    }
                                } else {
                                    unrequestedProjects.add(projectItem);
                                }

                            } else if (!unrequestedProjects.contains(projectItem) && hasProjectPermissions) {
                                accessibleProjects.add(projectItem);
                            }
                        } else if (!projectItem.getShareableStatus().equalsIgnoreCase(
                                 ProjectItem.SHAREABLE_STATUS_SHAREABLE)
                                           && !projectItem.getShareableStatus().equalsIgnoreCase(
                                                   ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)) {
                            nonShareableProjects.add(projectItem);
                        }
                    }
                } else if (cfpItem.getDataset() == null
                        && cfpItem.getComponentId() != null &&
                        (cfpItem.getComponentId().matches("Import.*") || componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME))) {
                    // Import uses private files
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    WorkflowFileItem itemExists = wfFileDao.find((Integer) cfpItem.getFile().getId());
                    if (itemExists != null) {
                        WorkflowFileItem wfFileItem = null;
                        wfFileItem = wfFileDao.get((Integer) itemExists.getId());
                        if (wfFileItem != null && wfFileItem.getOwner() != null) {
                            String wfFileOwnerId = (String) wfFileItem.getOwner().getId();
                            if (!wfFileOwnerId.equals(userItem.getId())) {
                                hasUnownedPrivateFiles = true;
                            }
                        }
                    }
                }
            }
        }

        wfRowDto.setHasUnownedPrivateFiles(hasUnownedPrivateFiles);

        if (unrequestedProjects != null && !unrequestedProjects.isEmpty()) {
            wfRowDto.setProjects(Collections.synchronizedList(unrequestedProjects));
        }

        if (accessibleProjects != null && !accessibleProjects.isEmpty()) {
            wfRowDto.setAccessibleProjects(Collections.synchronizedList(accessibleProjects));
        }

        if (activeRequestProjects != null && !activeRequestProjects.isEmpty()) {
            wfRowDto.setActiveRequestProjects(Collections.synchronizedList(activeRequestProjects));
        }

        if (reRequestProjects != null && !reRequestProjects.isEmpty()) {
            wfRowDto.setReRequestProjects(Collections.synchronizedList(reRequestProjects));
        }

        if (nonShareableProjects != null && !nonShareableProjects.isEmpty()) {
            wfRowDto.setNonShareableProjects(Collections.synchronizedList(nonShareableProjects));
        }

        return wfRowDto;
    }

 /**
     * Returns the workflowHistory for a workflow. Workflow history not yet implemented.
     * @param workflowItem the workflow item
     * @return a list of workflow history items
     */
    public List<WorkflowHistoryDto> getWorkflowHistoryRows(WorkflowItem workflowItem) {
        SampleDao sampleDao = DaoFactory.DEFAULT.getSampleDao();
        WorkflowHistoryDao workflowHistoryDao = DaoFactory.DEFAULT.getWorkflowHistoryDao();
        List<WorkflowHistoryItem> workflowHistoryItems = workflowHistoryDao.find(workflowItem);
        List<WorkflowHistoryDto> workflowHistoryDtos = new ArrayList<WorkflowHistoryDto>();
        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        ImportQueueDao iqDao = DaoFactory.DEFAULT.getImportQueueDao();
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();

        for (WorkflowHistoryItem historyItem : workflowHistoryItems) {
            DatasetItem datasetItem = dsDao.get((Integer) historyItem.getDataset().getId());
            SampleItem sampleItem = sampleDao.get((Integer) historyItem.getSample().getId());
            ImportQueueItem importQueueItem = iqDao.get((Integer) historyItem.getImportQueue().getId());

            WorkflowHistoryDto workflowHistoryDto = new WorkflowHistoryDto();
            workflowHistoryDto.setUserId(historyItem.getUserId());
            if (historyItem.getUserId() != null && userDao.find(historyItem.getUserId()) != null) {
                Pattern pattern = Pattern.compile(EMAIL_PATTERN);

                UserItem userItem = userDao.get(historyItem.getUserId());
                // Set the userLink to display the username, for starters.
                String nameAndId = getFormattedUserName(userItem, true);
                if (nameAndId == null) {
                    nameAndId = new String();
                }

                // Because getEmail could return a non-email string, we use drop-through logic
                // in the next two conditional (if) blocks, Block 1 and Block 2.
                if (userItem != null) {
                    // Block 1
                    workflowHistoryDto.setUserLink(nameAndId);
                }
                // If email matches pattern, then display the username with mailto link, instead.
                if (userItem != null && userItem.getEmail() != null) {
                    // Block 2
                    Matcher matcher = pattern.matcher(userItem.getEmail());
                    if (matcher.matches()) {
                        String userLink = "<a href=\"mailto:" + userItem.getEmail() + "\" >"
                            + getFormattedUserName(userItem, true) + "</a>";
                        workflowHistoryDto.setUserLink(userLink);
                    }
                }
            }


            workflowHistoryDto.setWorkflowId((Long) workflowItem.getId());
            workflowHistoryDto.setWorkflowName(workflowItem.getWorkflowName());
            workflowHistoryDto.setDatasetId((Integer) datasetItem.getId());
            workflowHistoryDto.setDatasetName(datasetItem.getDatasetName());
            workflowHistoryDto.setSampleId((Integer) sampleItem.getId());
            workflowHistoryDto.setSampleName(sampleItem.getSampleName());
            workflowHistoryDto.setImportQueueId((Integer) importQueueItem.getId());
            workflowHistoryDto.setImportQueueName(importQueueItem.getDatasetName());
            workflowHistoryDto.setTime(historyItem.getTime());
            workflowHistoryDto.setAction(historyItem.getAction());
            workflowHistoryDto.setInfo(historyItem.getInfo());
            workflowHistoryDto.setSampleFilters(historyItem.getSampleFilters());

            // mck Todo: workflow history
            /*if (historyItem.getAction().equals(WorkflowHistoryItem.ACTION_CREATE_DATASET_FROM_SAMPLE)) {
                ImportQueueItem iqItem = iqDao.get(historyItem.getImportQueueId());
                DatasetItem createdDataset = dsDao.get((Integer) iqItem.getDataset().getId());
                if (iqItem != null) {
                    String withKCMs = iqItem.getIncludeUserKCMs() ? "with" : "without";
                    String beginLink = "<a href=\"DatasetInfo?datasetId=" + createdDataset.getId() + "\">";
                    String endLink = "</a>";
                    info = "Created dataset '" + beginLink + createdDataset.getDatasetName() + endLink
                        + "' [" + createdDataset.getId() + "] " + "from dataset '"
                        + datasetItem.getDatasetName() + "' [" + datasetItem.getId()
                        + "] and sample '"
                        + sampleItem.getSampleName() + "' "
                        + withKCMs + " user created KC Models";
                }
            }*/

            workflowHistoryDtos.add(workflowHistoryDto);
        }


        return workflowHistoryDtos;
    }

    /**
     * Saves the sample history item.
     * @param userItem the user
     * @param workflowItem the workflow
     * @param action the sample history action
     * @param info the info field
     * @param importQueueId the optional importQueueId
     * @return
     */
    public WorkflowHistoryItem saveWorkflowHistory(UserItem userItem, WorkflowItem workflowItem,
        String action, String info, Integer importQueueId) {
        return null;
        /*//KEEP:
        WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        WorkflowHistoryDao workflowHistoryDao = DaoFactory.DEFAULT.getWorkflowHistoryDao();
        FilterDao filterDao = DaoFactory.DEFAULT.getFilterDao();
        WorkflowHistoryItem workflowHistoryItem = new WorkflowHistoryItem();
        workflowHistoryItem.setAction(action);
        workflowHistoryItem.setDataset(workflowItem.getDataset());
        workflowHistoryItem.setSample(workflowItem.getSample());
        workflowHistoryItem.setImportQueueId(importQueueId);
        workflowHistoryItem.setInfo(info);

        //walk through list adding each filter to the response string.
        StringBuffer filtersText = new StringBuffer();
        List<FilterItem> filterItems = filterDao.find(workflowItem.getSample());
        for (FilterItem filterItem : filterItems) {
            filtersText.append("\n" + filterItem.getClazz() + "\t");
            filtersText.append(filterItem.getAttribute() + "\t");
            filtersText.append(filterItem.getFilterString() + "\t");
            filtersText.append(filterItem.getOperator());
        }
        workflowHistoryItem.setFiltersText(filtersText.toString());
        // Set the owner id
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        UserItem owner = userDao.get((String) userItem.getId());
        if (owner != null) {
            workflowHistoryItem.setUserId((String) owner.getId());
        }
        workflowHistoryItem.setTime(new Date());
        workflowHistoryDao.saveOrUpdate(workflowHistoryItem);
        return workflowHistoryItem;*/
    }

    /** Calculate the Levenshtein distance between two strings.
     * @return the distance (number of replacements or shifts)
     */
    public static int levenshteinDistance(String a, String b) {

             a = a.toLowerCase();
             b = b.toLowerCase();

             int[] costs = new int[b.length() + 1];

             for (int j = 0; j < costs.length; j++) {
                 costs[j] = j;
             }

             for (int i = 1; i <= a.length(); i++)
             {
                 costs[0] = i;
                 int nw = i - 1;
                 for (int j = 1; j <= b.length(); j++) {

                     int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                         a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);

                     nw = costs[j];
                     costs[j] = cj;
                 }
             }

             return costs[b.length()];
     }



}
