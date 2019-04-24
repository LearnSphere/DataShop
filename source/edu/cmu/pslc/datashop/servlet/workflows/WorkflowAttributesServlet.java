/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowPaperDao;
import edu.cmu.pslc.datashop.dao.WorkflowPaperMapDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowPaperMapItem;
/**
 * This servlet handles the management and requests for workflows.
 *
 * @author Mike Komisin
 * @version $Revision: 15837 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2019-02-01 09:41:47 -0500 (Fri, 01 Feb 2019) $ <!-- $KeyWordsOff: $ -->
 */
public class WorkflowAttributesServlet extends AbstractServlet {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The JSP name for editing workflows as a standalone tool. */
    private static final String DETACHED_LIST_JSP_NAME = "/jsp_workflows/learnsphere-list.jsp";
    /** The JSP for logging in to WorkflowEditor. */
    private static final String LOGIN_JSP_NAME = "/jsp_workflows/ls_login.jsp";
    /** The paper published date format. */
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    /** The workflow helpers. */
    private WorkflowHelper workflowHelper;
    private WorkflowFileHelper workflowFileHelper;
    /** The workflow DAO. */
    private WorkflowDao workflowDao;

    /** Label used for setting session attribute "recent_report". */
    public static final String SERVLET_NAME = "WorkflowAttributes";

    /**
     * Handles the HTTP get.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        doPost(req, resp);
    }

    /**
     * Handles the HTTP POST.
     *
     * @see javax.servlet.http.HttpServlet
     * @param req HttpServletRequest.
     * @param resp HttpServletResponse.
     * @throws IOException an input output exception
     * @throws ServletException an exception creating the servlet
     */
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        setEncoding(req, resp);
        if ((req.getParameter("requestingMethod") == null)
            || (req.getParameter("requestingMethod") != null
                && !req.getParameter("requestingMethod").equalsIgnoreCase("WorkflowEditorServlet.getComponentStates"))) {

            logger.info("doPost params :: " + getDebugParamsString(req));
        }

        // Get the logged in user or null if not logged in.
        UserItem loggedInUserItem = getLoggedInUserItem(req);
        // Forward to the workflows_detached JSP (view) unless the disp is overridden by another method.

        if (loggedInUserItem != null) {

            // Helper
            workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
            workflowFileHelper = HelperFactory.DEFAULT.getWorkflowFileHelper();
            // DAOs
            workflowDao = DaoFactory.DEFAULT.getWorkflowDao();

            // Workflow and workflow context
            WorkflowContext workflowContext = null;

            // Authorization conditions
            Boolean isAdmin = false;
            Boolean isOwner = false;
            Boolean isShared = false;

            // Handle requests for Workflow role
            if (req.getParameter("requestRole") != null) {
                logger.info("**** where did this come from?");
                return;
            }

            if (loggedInUserItem.getAdminFlag()) {
                isAdmin = true;
            }

            /**
             *  Workflow Context.
             */
            workflowContext = (WorkflowContext) req.getSession().getAttribute("workflowContext");

            if (workflowContext == null) {
                // In this case, neither the dataset context nor session contains a
                // workflow context so we create a new workflow context.
                workflowContext = new WorkflowContext();
                req.getSession().setAttribute("workflowContext", (WorkflowContext) workflowContext);
                req.setAttribute("workflowContext", workflowContext);
            }

            /**
             *  Workflow item initialization and get workflow permissions.
             */

            // Get the requesting method.
             String requestingMethod = req.getParameter("requestingMethod");

                /**
                 * Handle file uploads and file requests.
                 */
                // Handling multi-part form data (it will not have doPost params, but
                // only the owner and LearnSphere admins can upload files to the workflow (see the uploadOptionsFile
                // JavaScript function in WorkflowEditor.js).
                // If multi-part form data was submitted, then save it and set the necessary request attributes for the
                // fileUpload requestMethod.
                if (ServletFileUpload.isMultipartContent(req)) {
                    // The upload file method not only saves the file, but also extracts
                    // the necessary form field values and puts them back in the
                    // request--
                    // i.e., componentId, workflowId, requestingMethod, filePath, and fileName
                    List<org.apache.commons.fileupload.FileItem> items = getDatasetUploadItems(req);
                    if (items == null) {
                        String errorMsg = isAdmin ? "File size exceeds 1GB allowance."
                            : "File size exceeds 400MB allowance.";
                        writeJsonError(resp, errorMsg);
                        return;
                    }
                    // We may bring back a map of columnHeaders to their indices on the next method
                    handlePaperUploadRequest(workflowFileHelper, workflowHelper,
                        req, resp, items,
                            WorkflowFileUtils.getStrictDirFormat(getBaseDir()),
                                WorkflowHelper.getWorkflowComponentsDir(),
                                    loggedInUserItem);

                    return;
                    /* End multi-part form handling. */
                } else {
                    String workflowIdString = (String) req.getParameter("workflowId");
                    WorkflowItem workflowItem = null;
                    if (workflowIdString != null && workflowIdString.matches("\\d+")) {
                        Long workflowId = Long.parseLong(workflowIdString);
                        workflowItem = workflowDao.get(workflowId);
                    }
                    // Not multi-part content (not a paper upload)
                     if (requestingMethod != null
                            && requestingMethod.equals("WorkflowAttributesServlet.populatePaperLinkDialog")) {
                         // Get the list of papers.
                         if (workflowItem != null) {
                             doPostRequestPapers(req, resp, workflowContext, workflowItem, loggedInUserItem, null);
                             return;
                         }

                     } else if (requestingMethod != null
                             && requestingMethod.equals("WorkflowAttributesServlet.linkPaper")) {
                         doPostLinkPaper(req, resp, workflowItem, loggedInUserItem);
                         return;
                     } else if (requestingMethod != null
                                && requestingMethod.equals("WorkflowAttributesServlet.unlinkPaper")) {
                         doPostUnLinkPaper(req, resp, workflowItem, loggedInUserItem);
                         return;
                     } else if (requestingMethod != null
                               && requestingMethod.equals("WorkflowAttributesServlet.linkDataset")) {
                        doPostLinkWorkflow(req, resp, workflowItem, loggedInUserItem);
                        return;
                    } else if (requestingMethod != null
                               && requestingMethod.equals("WorkflowAttributesServlet.unlinkDataset")) {
                        doPostUnLinkWorkflow(req, resp, workflowItem, loggedInUserItem);
                        return;
                    } else if (requestingMethod != null && requestingMethod.equalsIgnoreCase("WorkflowAttributesServlet.urlSubmit")) {

                        req.setAttribute("workflowId", req.getParameter("workflowId"));
                        req.setAttribute("title", req.getParameter("title"));
                        req.setAttribute("authorNames", req.getParameter("authorNames"));
                        req.setAttribute("publication", req.getParameter("publication"));
                        req.setAttribute("citation", req.getParameter("citation"));
                        req.setAttribute("paperAbstract", req.getParameter("paperAbstract"));
                        req.setAttribute("publishDate", req.getParameter("publishDate"));
                        req.setAttribute("url", req.getParameter("url"));
                        req.setAttribute("paperId", req.getParameter("paperId"));

                        handleUrlSubmitRequest(workflowFileHelper, workflowHelper,
                                req, resp,
                                WorkflowFileUtils.getStrictDirFormat(getBaseDir()),
                                WorkflowHelper.getWorkflowComponentsDir(), loggedInUserItem);

                        return;
                    } else if (requestingMethod != null && requestingMethod.equalsIgnoreCase("WorkflowAttributesServlet.deletePaper")
                            && req.getParameter("paperId") != null && req.getParameter("paperId").matches("\\d+")) {
                        Integer paperId = Integer.parseInt(req.getParameter("paperId"));
                        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);

                        deletePaper(workflowDao, workflowHelper, loggedInUserItem, paperId, getBaseDir(), disp, req, resp);
                        return;
                    } else if (requestingMethod != null && requestingMethod.equalsIgnoreCase("WorkflowAttributesServlet.editPaper")
                            && req.getParameter("paperId") != null && req.getParameter("paperId").matches("\\d+")) {
                        Integer paperId = Integer.parseInt(req.getParameter("paperId"));
                        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);

                        editPaperAttributes(workflowDao, workflowHelper, loggedInUserItem, paperId, getBaseDir(), disp, req, resp);
                        return;
                    } else if (req.getParameter("paperId") != null && req.getParameter("paperId").matches("\\d+")) {
                        Integer paperId = Integer.parseInt(req.getParameter("paperId"));
                        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);

                        returnFile(workflowDao, workflowHelper, loggedInUserItem, paperId, getBaseDir(), disp, req, resp);
                        return;
                    } else if (requestingMethod != null && requestingMethod.equalsIgnoreCase("WorkflowAttributesServlet.getWorkflowPaperCount")) {

                        WorkflowPaperMapDao wfpmDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();
                        Integer paperCount = 0;

                        if (workflowItem != null) {
                            List<WorkflowPaperMapItem> paperMappings = wfpmDao.findByWorkflow(workflowItem);
                            if (paperMappings != null && !paperMappings.isEmpty()) {
                                paperCount = paperMappings.size();
                            }
                        }

                        try {
                            writeJSON(resp, json("paperCount", paperCount));
                        } catch (JSONException e) {
                            writeJsonError(resp, "Cannot retrieve paper count.");
                        }
                        return;
                    }
                }

        } else { // User not logged in

            // Let the editor know the user is logged out.
            if (req.getParameter("requestingMethod") != null) {
                try {
                    writeJSON(resp, json("success", true, "loggedOut", true));
                    return;
                } catch (IOException ioe) {
                    writeJsonError(resp, "Status update in error.");
                } catch (JSONException je) {
                    writeJsonError(resp, "Status update in error.");
                }
            }


        }

        RequestDispatcher disp = getServletContext().getRequestDispatcher(DETACHED_LIST_JSP_NAME);
        // The user is not logged in
        logger.debug("Forwarding to JSP " + LOGIN_JSP_NAME);
        disp = getServletContext().getRequestDispatcher(LOGIN_JSP_NAME);
        disp.forward(req, resp);
        return;
    }

    /**
     * Helper method to get the list of projects and datasets that user has 'edit' access to.
     * These are datasets that the user can attach a workflow to.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowContext WorkflowContext used to determine if info is already cached
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostRequestPapers(HttpServletRequest req, HttpServletResponse resp,
                                                  WorkflowContext workflowContext, WorkflowItem workflowItem,
                                                  UserItem loggedInUserItem, Integer paperId) {

        try {
            JSONArray myPaperArray = new JSONArray();

            JSONObject jsonInfo = null;

            jsonInfo = getWorkflowPapers(req, workflowContext, loggedInUserItem, workflowItem, paperId);

            if (jsonInfo != null) {
                myPaperArray = jsonInfo.getJSONArray("myPaperArray");
            }

            writeJSON(
                      resp,
                      json("myPaperArray", myPaperArray));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error retrieving papers.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error retrieving papers.");
        }
    }

    private JSONObject getWorkflowPapers(HttpServletRequest req, WorkflowContext workflowContext,
            UserItem loggedInUserItem, WorkflowItem workflowItem, Integer paperId)
                throws JSONException {

   WorkflowPaperDao wfPaperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
   WorkflowPaperMapDao wfpMapDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();
   JSONObject jsonInfo = null;
   JSONArray paperJsonArray = new JSONArray();

   List<WorkflowPaperItem> papers = null;

   Boolean isOwner = false;
   Long thisWorkflowId =  workflowItem.getId();
   if (workflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
       isOwner = true;
   }

   if (paperId != null) {
       // Get a single paper (for edit paper attributes)
       WorkflowPaperItem wfpItem = wfPaperDao.get(paperId);
       if (wfpItem != null) {
           papers = new ArrayList<WorkflowPaperItem>();
           papers.add(wfpItem);
       }
   } else if (!isOwner) {
       // Not the owner so only get papers for THIS workflow item.
       papers = wfPaperDao.findByWorkflow(workflowItem);
   } else {
       // The owner gets a list of all papers they own.
       papers = wfPaperDao.findByOwner(loggedInUserItem);
   }

   if (papers != null) {
       for (WorkflowPaperItem paper : papers) {
           Hibernate.initialize(paper);

//mck3 todo: tooltip with the following dataset info over samples/datasets
           JSONObject paperObject = new JSONObject();
           paperObject.put("paperId", paper.getId());
           paperObject.put("title", WorkflowFileUtils.htmlEncode(paper.getTitle()));
           paperObject.put("authorNames", WorkflowFileUtils.htmlEncode(paper.getAuthorNames()));
           paperObject.put("publicationr", paper.getPublication());
           paperObject.put("citation", paper.getCitation());
           paperObject.put("publishDate", paper.getPublishDate());
           paperObject.put("paperAbstract", paper.getPaperAbstract());
           paperObject.put("owner", paper.getOwner().getId());
           paperObject.put("url", paper.getUrl());
           paperObject.put("addedTime", paper.getAddedTime());

           if (paper.getFilePath() != null && !paper.getFilePath().isEmpty()) {
               File fileItem = new File(paper.getFilePath());
               if (fileItem.exists() && fileItem.isFile()) {
                   paperObject.put("fileName", fileItem.getName());
               }
           }

           if (isOwner) {
               Boolean alreadyAttached = false;
               List<WorkflowPaperMapItem> paperMap = wfpMapDao.findByWorkflowPaper(paper);
               for (WorkflowPaperMapItem wpmItem : paperMap) {
                   if (wpmItem.getWorkflow().getId().equals(thisWorkflowId)) {
                       alreadyAttached = true;
                   }
               }
               if (alreadyAttached) {
                   paperObject.put("workflowId", thisWorkflowId);
               } else {
                   paperObject.put("workflowId", "");
               }
           } else {
               paperObject.put("workflowId", workflowItem.getId());
           }
           paperJsonArray.put(paperObject);

       }
   }


   jsonInfo = null;
   if (paperJsonArray.length() > 0) {
       jsonInfo = json("myPaperArray", paperJsonArray);
   }

   return jsonInfo;

}

    /**
     * Helper method to link a workflow to paper.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostLinkPaper(HttpServletRequest req, HttpServletResponse resp,
                                    WorkflowItem workflowItem, UserItem loggedInUserItem) {

        Long workflowId = workflowItem.getId();
        workflowItem = workflowDao.get(workflowId);

        List<Integer> paperIds = new ArrayList<Integer>();
        String paperIdStr = req.getParameter("paperIds");
        if (paperIdStr != null) {
            if ((paperIdStr.indexOf(",") < 0) &&
                (paperIdStr.matches("\\d+"))) {
                paperIds.add(Integer.parseInt(paperIdStr));
            } else {
                String[] ids = paperIdStr.split(",");
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i].matches("\\d+")) {
                        paperIds.add(Integer.parseInt(ids[i]));
                    }
                }
            }
        }

        WorkflowPaperDao paperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        WorkflowPaperMapDao mapDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();

        for (Integer paperId : paperIds) {
            WorkflowPaperItem paperItem = paperDao.get(paperId);

            WorkflowPaperMapItem mapItem = new WorkflowPaperMapItem();
            WorkflowPaperMapId mapId = new WorkflowPaperMapId(workflowItem, paperItem);
            mapItem.setId(mapId);
            mapDao.saveOrUpdate(mapItem);
        }

        try {
            writeJSON(resp, json("success", "true"));
            return;

        } catch (JSONException exception) {
            writeJsonError(resp, "Error linking workflow to paper.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error linking workflow to paper.");
            return;
        }
    }

    /**
     * Helper method to remove a workflow-paper link.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostUnLinkPaper(HttpServletRequest req, HttpServletResponse resp,
                                      WorkflowItem workflowItem, UserItem loggedInUserItem) {

        Long workflowId = workflowItem.getId();
        workflowItem = workflowDao.get(workflowId);

        Integer paperId = null;
        if (req.getParameter("paperId") != null
            && req.getParameter("paperId").matches("\\d+")) {
            paperId = Integer.parseInt(req.getParameter("paperId"));
        }

        WorkflowPaperDao paperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        WorkflowPaperItem paperItem = paperDao.get(paperId);

        WorkflowPaperMapDao mapDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();
        WorkflowPaperMapId mapId = new WorkflowPaperMapId(workflowItem, paperItem);
        WorkflowPaperMapItem mapItem = mapDao.find(mapId);
        mapDao.delete(mapItem);

        try {
            writeJSON(resp, json("success", "true"));
            return;
        } catch (JSONException exception) {
            writeJsonError(resp, "Error linking workflow to paper.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error linking workflow to paper.");
            return;
        }
    }

    /**
     * Helper method to link a workflow to dataset.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostLinkWorkflow(HttpServletRequest req, HttpServletResponse resp,
                                    WorkflowItem workflowItem, UserItem loggedInUserItem) {

        Long workflowId = workflowItem.getId();
        workflowItem = workflowDao.get(workflowId);

        List<Integer> datasetIds = new ArrayList<Integer>();
        String datasetIdStr = req.getParameter("datasetIds");
        if (datasetIdStr != null) {
            if ((datasetIdStr.indexOf(",") < 0) &&
                (datasetIdStr.matches("\\d+"))) {
                datasetIds.add(Integer.parseInt(datasetIdStr));
            } else {
                String[] ids = datasetIdStr.split(",");
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i].matches("\\d+")) {
                        datasetIds.add(Integer.parseInt(ids[i]));
                    }
                }
            }
        }

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        WorkflowDatasetMapDao mapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();

        for (Integer datasetId : datasetIds) {
            DatasetItem datasetItem = datasetDao.get(datasetId);

            WorkflowDatasetMapItem mapItem = new WorkflowDatasetMapItem();
            WorkflowDatasetMapId mapId = new WorkflowDatasetMapId(workflowItem, datasetItem);
            mapItem.setId(mapId);
            mapItem.setAddedBy(loggedInUserItem);
            mapItem.setAddedTime(new Date());
            mapItem.setAutoDisplayFlag(true);
            mapDao.saveOrUpdate(mapItem);
        }

        String wfDatasetsXml = WorkflowHelper.getWorkflowDatasets(workflowItem);

        try {
            writeJSON(resp, org.json.XML.toJSONObject(wfDatasetsXml));
        } catch (JSONException exception) {
            writeJsonError(resp, "Error linking workflow to dataset.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error linking workflow to dataset.");
            return;
        }
    }

    /**
     * Helper method to remove a workflow-dataset link.
     * The assumption is that this is will not be called with a null workflow or user item.
     *
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param workflowItem WorkflowItem the current workflow
     * @param loggedInUserItem UserItem the current user
     */
    private void doPostUnLinkWorkflow(HttpServletRequest req, HttpServletResponse resp,
                                      WorkflowItem workflowItem, UserItem loggedInUserItem) {

        Long workflowId = workflowItem.getId();
        workflowItem = workflowDao.get(workflowId);

        Integer datasetId = null;
        if (req.getParameter("datasetId") != null
            && req.getParameter("datasetId").matches("\\d+")) {
            datasetId = Integer.parseInt(req.getParameter("datasetId"));
        }

        DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem datasetItem = datasetDao.get(datasetId);

        WorkflowDatasetMapDao mapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
        WorkflowDatasetMapId mapId = new WorkflowDatasetMapId(workflowItem, datasetItem);
        WorkflowDatasetMapItem mapItem = mapDao.find(mapId);
        mapDao.delete(mapItem);

        String wfDatasetsXml = WorkflowHelper.getWorkflowDatasets(workflowItem);

        try {
            writeJSON(resp, org.json.XML.toJSONObject(wfDatasetsXml));
        } catch (JSONException exception) {
            writeJsonError(resp, "Error linking workflow to dataset.");
            return;
        } catch (IOException e) {
            writeJsonError(resp, "Error linking workflow to dataset.");
            return;
        }
    }

    /**
     * Handle the request to submit a paper's URL to associate with a workflow.
     * Only DS admins and the workflow owner can add a file to a workflow.
     *
     * @param req the servlet request
     * @param resp the servlet response
     * @param items the multi-part form data
     * @param baseDir the directory where the file can be found
     * @param userItem the user item
     * @return whether or not the user is allowed to upload a file to the workflow
     */
    public void handleUrlSubmitRequest(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper,
        HttpServletRequest req, HttpServletResponse resp,
            String baseDir, String componentsDir,
                UserItem userItem) {
        WorkflowPaperItem wpItem = null;
        WorkflowItem workflowItem = null;
        WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        // Long
        Long workflowId = null;

        Boolean isOwner = false;
        Boolean isAdmin = false;

        if (req.getAttribute("url") != null) {


            if (req.getAttribute("workflowId") != null) {

                String workflowIdString = (String) req.getAttribute("workflowId");
                if (workflowIdString.matches("\\d+")) {
                    workflowId = Long.parseLong(workflowIdString);
                    workflowItem = workflowDao.get(workflowId);
                }
            }


            if (workflowItem != null && workflowItem.getOwner() != null
                    && workflowItem.getOwner().getId().equals(userItem.getId())) {
                isOwner = true;
            } else if (workflowItem != null && userItem.getAdminFlag()) {
                isAdmin = true;
            }

            // The workflow is not null and the user has permission to upload a file to the workflow specified in the
            // multi-part form data
            if ((isAdmin || isOwner)) {
                try {

                    // Process files separately
                    String urlString = (String) req.getAttribute("url");
                    Boolean isValidUrl = true;
                    try {
                        URL urlTest = new URL(urlString);
                    } catch (MalformedURLException e) {
                        logger.error("Cannot parse remote workflows server URL: " + e.toString());
                        isValidUrl = false;
                    }
                    if (isValidUrl) {
                        Integer paperId = null;
                        if (req.getAttribute("paperId") != null && ((String)req.getAttribute("paperId")).matches("\\d+")) {
                            paperId = Integer.parseInt((String) req.getAttribute("paperId"));
                        }

                        wpItem = saveWorkflowPaper(req, paperId, workflowFileHelper, workflowHelper, workflowId,
                            urlString, null,
                                baseDir, userItem);

                        workflowHelper.logWorkflowUserAction(userItem, workflowItem, null,
                                "Paper upload", "Uploaded new file item, workflow_paper_id = " + wpItem.getId(), null);

                        req.setAttribute("requestingMethod", "WorkflowEditorServlet.paperUpload");
                        req.setAttribute("workflowId", workflowId.toString());

                        req.setAttribute("paperId", wpItem.getId());
                        req.setAttribute("title", wpItem.getTitle());
                        req.setAttribute("authorNames", wpItem.getAuthorNames());
                        req.setAttribute("publication", wpItem.getPublication());
                        req.setAttribute("paperAbstract", wpItem.getPaperAbstract());
                        req.setAttribute("publishDate", wpItem.getPublishDate());
                        req.setAttribute("url", wpItem.getUrl());
                        req.setAttribute("citation", wpItem.getCitation());
                    }

                } catch (Exception exception) {
                    String errorMessage = "Could not save paper.";
                    logger.error(errorMessage);
                    logger.error(exception.toString());
                }
            } else {
                // The workflow is not owned by the user and the user is not an admin.
            }
        }

        if (wpItem != null && workflowItem != null && (isOwner || isAdmin)) {
            paperUploadResponse(req, resp, userItem, workflowItem);
        }
    }

    /**
     * Handle the request to upload a file and check the user's access to upload a file to the workflow.
     * Only DS admins and the workflow owner can add a file to a workflow.
     *
     * @param req the servlet request
     * @param resp the servlet response
     * @param items the multi-part form data
     * @param baseDir the directory where the file can be found
     * @param userItem the user item
     * @return whether or not the user is allowed to upload a file to the workflow
     */
    public void handlePaperUploadRequest(WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper, HttpServletRequest req, HttpServletResponse resp,
            List<org.apache.commons.fileupload.FileItem> items, String baseDir, String componentsDir,
                UserItem userItem) {
        WorkflowPaperItem wpItem = null;
        Integer paperId = null;
        WorkflowItem workflowItem = null;
        WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        // Long
        Long workflowId = null;
        Boolean isOwner = false;
        Boolean isAdmin = false;
        WorkflowPaperDao wpDao = DaoFactory.DEFAULT.getWorkflowPaperDao();

        if (items == null) {
            logger.error("File was not received.");

        } else {
            logger.debug("Handling multi-part form data.");
            for (org.apache.commons.fileupload.FileItem uploadFileItem : items) {

                if (uploadFileItem.isFormField()) {
                    logger.trace("Upload Form field: " + uploadFileItem.getFieldName() + ", "
                            + uploadFileItem.getString());
                    // Put text-based form fields into the request
                    req.setAttribute(uploadFileItem.getFieldName(), uploadFileItem.getString());

                    if (uploadFileItem.getFieldName().equalsIgnoreCase("workflowId")) {

                        String workflowIdString = uploadFileItem.getString();
                        if (workflowIdString.matches("\\d+")) {
                            workflowId = Long.parseLong(workflowIdString);
                            workflowItem = workflowDao.get(workflowId);
                        }
                    }

                    if (uploadFileItem.getFieldName().equalsIgnoreCase("paperId")) {

                        String paperIdString = uploadFileItem.getString();
                        if (paperIdString.matches("\\d+")) {
                            Integer tmpPaperId = Integer.parseInt(paperIdString);
                            wpItem = wpDao.get(tmpPaperId);
                            if (wpItem != null && wpItem.getOwner().getId().equals(userItem.getId())) {
                                paperId = (Integer) wpItem.getId();
                            } else {
                                wpItem = null;
                                paperId = null;
                            }
                        }
                    }

                }
            } // end for loop

            if (workflowItem != null && workflowItem.getOwner() != null
                    && workflowItem.getOwner().getId().equals(userItem.getId())) {
                isOwner = true;
            } else if (workflowItem != null && userItem.getAdminFlag()) {
                isAdmin = true;
            }

            // The workflow is not null and the user has permission to upload a file to the workflow specified in the
            // multi-part form data
            if ((isAdmin || isOwner)) {
                org.apache.commons.fileupload.FileItem uploadFileItem = null;
                try {
                    for (org.apache.commons.fileupload.FileItem formItem : items) {

                        if (!formItem.isFormField()) {

                            if (!formItem.isFormField()) {
                                // Process files separately
                                Boolean isValidFile = WorkflowFileUtils.checkFileIsSupported(uploadFileItem);
                                if (!isValidFile) {
                                    logger.error("Uploaded file is not valid.");
                                    continue;
                                } else {
                                    uploadFileItem = formItem;
                                }
                            }

                        }

                    } // end for loop
                } catch (Exception exception) {
                    String errorMessage = "Could not save paper.";
                    logger.error(errorMessage);
                    logger.error(exception.toString());
                }

                wpItem = saveWorkflowPaper(req, paperId, workflowFileHelper, workflowHelper, workflowId,
                    null, uploadFileItem,
                        baseDir, userItem);

                workflowHelper.logWorkflowUserAction(userItem, workflowItem, null,
                        "Paper upload", "Uploaded new file item, workflow_paper_id = " + wpItem.getId(), null);

                req.setAttribute("requestingMethod", "WorkflowEditorServlet.paperUpload");
                req.setAttribute("workflowId", workflowId.toString());

                req.setAttribute("paperId", wpItem.getId());
                req.setAttribute("title", wpItem.getTitle());
                req.setAttribute("authorNames", wpItem.getAuthorNames());
                req.setAttribute("publication", wpItem.getPublication());
                req.setAttribute("paperAbstract", wpItem.getPaperAbstract());
                req.setAttribute("publishDate", wpItem.getPublishDate());
                req.setAttribute("url", wpItem.getUrl());
                req.setAttribute("citation", wpItem.getCitation());

                if (wpItem.getFilePath() != null && !wpItem.getFilePath().isEmpty()) {
                    File fileItem = new File(wpItem.getFilePath());
                    if (fileItem.exists() && fileItem.isFile()) {
                        req.setAttribute("fileName", fileItem.getName());
                    }
                }

            } else {
                // The workflow is not owned by the user and the user is not an admin.
            }
        }

        if (wpItem != null && workflowItem != null && (isOwner || isAdmin)) {
            paperUploadResponse(req, resp, userItem, workflowItem);
        }
    }

    /**
     * Handle the file upload once it has finished. This method does not check file permissions. Do not call it outside
     * a secure block of code.
     *
     * @param req the HTTP Servlet Request
     * @param resp the HTTP Servlet Response
     * @param loggedInUserItem the logged in user item
     * @param workflowItem
     */
    private void paperUploadResponse(HttpServletRequest req, HttpServletResponse resp, UserItem loggedInUserItem, WorkflowItem workflowItem) {

        if (workflowItem != null && loggedInUserItem != null) {

            String workflowName = workflowItem.getWorkflowName();
            try {

                if (req.getAttribute("title") != null && !((String) req.getAttribute("title")).isEmpty()) {

                    String message = "Paper uploaded successfully.";
                    String paperId = ((Integer) req.getAttribute("paperId")).toString();

                    writeJSON(
                            resp,
                            json("success", "true",
                                    "workflowId", workflowItem.getId(), "workflowName", workflowName,
                                    "paperId", paperId,
                                    "title", req.getAttribute("title"),
                                    "authorNames", req.getAttribute("authorNames"),
                                    "publication", req.getAttribute("publication"),
                                    "paperAbstract", req.getAttribute("paperAbstract"),
                                    "publishDate", req.getAttribute("publish_date"),
                                    "fileName", req.getAttribute("fileName"),
                                    "url", req.getAttribute("url"),
                                    "citation", req.getAttribute("citation"),
                                    "message", message));
                    return;
                } else {
                    String message = "Paper could not be uploaded.";
                    writeJSON(
                            resp,
                            json("success", "false", "workflowId", workflowItem.getId(), "workflowName", workflowName,
                                    "message", message));
                    return;
                }

            } catch (JSONException exception) {
                writeJsonError(resp, "Error importing file meta-data.");
                return;
            } catch (IOException e) {
                writeJsonError(resp, "Error importing file meta-data.");
                return;
            }
        }
    }

    private WorkflowPaperItem saveWorkflowPaper(HttpServletRequest req, Integer paperId,
        WorkflowFileHelper workflowFileHelper, WorkflowHelper workflowHelper, Long workflowId,
            String url, org.apache.commons.fileupload.FileItem uploadFileItem,
                String baseDir, UserItem userItem) {

        WorkflowPaperItem wpItem = null;


        WorkflowItem workflowItem = workflowDao.get(workflowId);
        if (workflowItem != null) {
            if (uploadFileItem != null) {
                // Actual paper upload
                String fieldName = uploadFileItem.getFieldName();
                String fileFullName = WorkflowFileUtils.sanitizePath(uploadFileItem.getName());

                if (fileFullName.indexOf('/') >= 0) {
                    fileFullName = fileFullName.substring(fileFullName.lastIndexOf('/') + 1);
                }

                if (fieldName.equals("file")) {

                    // Check to make sure the user has selected a file.
                    if (fileFullName != null && fileFullName.length() > 0) {
                        // Papers directory
                        String wholePath = WorkflowFileUtils.sanitizePath(baseDir + "/" + ((String)userItem.getId()));
                        File newDirectory = new File(wholePath);
                        if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                            FileUtils.makeWorldReadable(newDirectory);
                            if (logger.isDebugEnabled()) {
                                logger.trace("saveWorkflowPaper: The directory has been created." + newDirectory.getAbsolutePath());
                            }

                            // Save the file to the file system.
                            String newFileName = fileFullName;
                            File newFile = null;
                            File fileParent = new File(wholePath);
                            if (fileParent.exists() && fileParent.isDirectory()) {
                                List<String> fileList = Arrays.asList(fileParent.list());
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

                                // Ensure unique file name
                                while (fileList.contains(newFileName)) {
                                    i++;
                                    newFileName = fileName + "_" + i + fileExt;
                                }

                                newFile = new File(wholePath, newFileName);
                                if (!newFile.exists()) {
                                    try {
                                        uploadFileItem.write(newFile);
                                    } catch (Exception e) {
                                        logger.error("saveWorkflowPaper: Writing paper failed " + newFile.toString());
                                    }
                                }
                            }

                            wpItem = savePaperItem(paperId, userItem, workflowItem, req, null, newFile);

                        } else {
                            logger.error("saveWorkflowPaper: Creating directory failed " + newDirectory);
                        }
                    } else {
                        logger.error("saveWorkflowPaper: The fileName cannot be null or empty.");
                    }
                }
            } else if (url != null) {
                // URL submission
                wpItem = savePaperItem(paperId, userItem, workflowItem, req, url, null);
            } else if (paperId != null) {
                // File-based paper but without new file
                wpItem = savePaperItem(paperId, userItem, workflowItem, req, null, null);
            }
        }

        if (wpItem == null) {
            logger.error("saveWorkflowPaper: Failed to add paper.");
        }

        return wpItem;
    }

    private WorkflowPaperItem savePaperItem(Integer paperId, UserItem userItem, WorkflowItem workflowItem,
            HttpServletRequest req, String url, File newFile) {

        WorkflowPaperDao wpDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        WorkflowPaperMapDao wpmDao = DaoFactory.DEFAULT.getWorkflowPaperMapDao();
        WorkflowPaperItem wpItem = null;
        if (newFile != null || url != null || paperId != null) {

            String title = (String) req.getAttribute("title");
            String authorNames = (String) req.getAttribute("authorNames");
            String publication = (String) req.getAttribute("publication");
            String paperAbstract = (String) req.getAttribute("paperAbstract");
            String publishDate = (String) req.getAttribute("publishDate");
            String citation = (String) req.getAttribute("citation");

            // Create or get the paper item.
            if (paperId != null) {
                wpItem = wpDao.get(paperId);
                if (!wpItem.getOwner().getId().equals(userItem.getId())) {
                    // Something wrong- the owner is not the same as the user.
                    return null;
                }
            } else {
                wpItem = new WorkflowPaperItem();
            }

            wpItem.setOwner(userItem);
            wpItem.setAddedTime(new Date());

            // Title is required
            if (title != null && !title.trim().isEmpty()) {
                wpItem.setTitle(title);
            } else if (newFile != null && newFile.exists()) {
                title = newFile.getName();
            } else if (url != null) {
                title = url;
            }

            // Url will be null here if uploading a file
            if (url != null && !url.trim().isEmpty()) {
                wpItem.setUrl(url);
            }

            if (newFile != null && newFile.exists()) {
                wpItem.setFilePath(
                    newFile.getAbsolutePath().replaceAll("\\\\", "/"));
            }

            if (title != null && !title.trim().isEmpty()) {
                wpItem.setTitle(title);
            }
            if (authorNames != null && !authorNames.trim().isEmpty()) {
                wpItem.setAuthorNames(authorNames);
            }
            if (publication != null && !publication.trim().isEmpty()) {
                wpItem.setPublication(publication);
            }
            if (citation != null && !citation.trim().isEmpty()) {
                wpItem.setCitation(citation);
            }
            if (paperAbstract != null && !paperAbstract.trim().isEmpty()) {
                wpItem.setPaperAbstract(paperAbstract);
            }
            if (publishDate != null && !publishDate.trim().isEmpty()) {
                wpItem.setPublishDate(getDate(publishDate));
            }

            wpDao.saveOrUpdate(wpItem);


            WorkflowPaperMapItem wpmItem = new WorkflowPaperMapItem();
            wpmItem.setWorkflowExternal(workflowItem);
            wpmItem.setWorkflowPaperExternal(wpItem);
            wpmDao.saveOrUpdate(wpmItem);

        }
        return wpItem;
    }

    /**
     * Writes an import file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param fileIdString the FileItem id as a string
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void deletePaper(WorkflowDao workflowDao, WorkflowHelper workflowHelper, UserItem userItem,
        Integer paperId, String baseDir, RequestDispatcher disp, HttpServletRequest request,
            HttpServletResponse response) {

        WorkflowPaperDao wfPaperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        WorkflowPaperItem paperItem = wfPaperDao.get(paperId);
        String title = null;
        if (paperItem != null && paperItem.getOwner().getId().equals(userItem.getId())) {
            title = paperItem.getTitle();
            String paperPath = paperItem.getFilePath();
            if (paperPath != null) {
                File paperFile = new File(paperPath);
                if (paperFile.exists()) {
                    paperFile.delete();
                }
            }

            wfPaperDao.delete(paperItem);

            try {
                if (title != null) {
                    writeJSON(
                            response,
                            json("success", "true",
                                    "paperId", paperId,
                                    "title", title));
                } else {
                    writeJsonError(response, "Paper not found (" + paperId + ")");
                }
            } catch (JSONException e) {
                writeJsonError(response, "Could not delete paper (" + paperId + "): " + title);
            } catch (IOException e) {
                writeJsonError(response, "Could not delete paper (" + paperId + "): " + title);
            }
        }
    }


    /**
     * Writes an import file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param fileIdString the FileItem id as a string
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void editPaperAttributes(WorkflowDao workflowDao, WorkflowHelper workflowHelper, UserItem userItem,
        Integer paperId, String baseDir, RequestDispatcher disp, HttpServletRequest request,
            HttpServletResponse response) {


        WorkflowPaperDao wfPaperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
        WorkflowPaperItem paperItem = wfPaperDao.get(paperId);

        String fileName = null;
        if (paperItem != null && paperItem.getOwner().getId().equals(userItem.getId())) {

            if (paperItem.getFilePath() != null && !paperItem.getFilePath().isEmpty()) {
                File fileItem = new File(paperItem.getFilePath());
                if (fileItem.exists() && fileItem.isFile()) {
                    fileName = fileItem.getName();
                }
            }
            try {
                writeJSON(
                        response,
                        json("success", "true",
                                "paperId", paperId,
                                "title", paperItem.getTitle(),
                                "authorNames", paperItem.getAuthorNames(),
                                "publication", paperItem.getPublication(),
                                "paperAbstract", paperItem.getPaperAbstract(),
                                "publishDate", paperItem.getPublishDate(),
                                "fileName", fileName,
                                "url", paperItem.getUrl(),
                                "citation", paperItem.getCitation()));

            } catch (JSONException e) {
                writeJsonError(response, "Could not fetch/update paper (" + paperId + "). ");
            } catch (IOException e) {
                writeJsonError(response, "Could not fetch/update paper (" + paperId + "). ");
            }

        } else {
            writeJsonError(response, "Paper could not be fetched/updated (" + paperId + ").");
        }
    }

    /**
     * Writes an import file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param fileIdString the FileItem id as a string
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void returnFile(WorkflowDao workflowDao, WorkflowHelper workflowHelper, UserItem userItem,
        Integer paperId, String baseDir, RequestDispatcher disp, HttpServletRequest request,
            HttpServletResponse response) {
        ServletOutputStream out = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;

        try {

            WorkflowPaperDao wfPaperDao = DaoFactory.DEFAULT.getWorkflowPaperDao();
            WorkflowPaperItem paperItem = wfPaperDao.get(paperId);

            if (paperItem != null) {

                String paperPath = paperItem.getFilePath();

                File paperFile = new File(paperPath);

                String mimeType = WorkflowFileUtils.getMimeType(paperFile);
                response.setContentType(mimeType);
                response.setContentLength((int) paperFile.length());
                // Set content disposition so that browsers will bring up the
                // Save As dialog for any mime type
                response.setHeader("Content-Disposition", "attachment; filename=\"" + paperFile.getName() + "\"");

                out = response.getOutputStream();
                fin = new FileInputStream(paperFile);
                bin = new BufferedInputStream(fin);
                bout = new BufferedOutputStream(out);
                int ch = 0;

                while ((ch = bin.read()) != -1) {
                    bout.write(ch);
                }

                bin.close();
                bout.close();


            }
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            if (bin != null) {
                try {
                    bin.close();
                } catch (IOException e) {
                }
            }
            if (bout != null) {
                try {
                    bout.close();
                } catch (IOException e) {
                }
            }
        }
        return;
    }

    private Date getDate(String publishDate) {

        Date newDate = null;
        try {
            newDate = sdf.parse(publishDate);
        } catch (ParseException exception) {
            logger.error("getDate: Failed to parse date.", exception);
        }
        return newDate;
    }

    /**
     * Writes a json error message to the response.
     * @param resp the HttpServletResponse
     * @param errorMessage the error message
     */
    private void writeJsonError(HttpServletResponse resp, String errorMessage) {
        try {
            logger.error("Error relayed to user: " + errorMessage);
            writeJSON(resp, json("error_flag", "true", "message", errorMessage));
            return;
        } catch (JSONException e) {
            logger.error("LearnSphereServlet caused a JSON Exception.");
        } catch (IOException e) {
            logger.error("LearnSphereServlet caused an IO Exception.");
        }
    }

} // end of WorkflowEditorServlet class

