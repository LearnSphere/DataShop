/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2017
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowFolderDao;
import edu.cmu.pslc.datashop.dao.WorkflowFolderMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowPersistenceDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapId;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFolderMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;
/**
 * Helper class for workflow-related files.
 * @author Mike Komisin.
 * @version $Revision:  $ <BR>
 * Last modified by: $Author: $ <BR>
 * Last modified on: $Date: $ <!-- $KeyWordsOff: $ -->
 */
public class WorkflowFileHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowFileHelper.class.getName());
    /** Format for the date range method, getDateRangeString. */
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");

    /** HashMap that holds the dataset exports since Exports are not in the file_dataset_map in DataShop. */
    private Map <String, JSONObject> datashopCachedExportsMap;

    public WorkflowFileHelper() {
        datashopCachedExportsMap = Collections.synchronizedMap(new HashMap <String, JSONObject> ());
    }


    public static final String[] dataSensitiveElements = { "optionmeta", "inputmeta", "files", "options", "errors" };

    public String filterResultsByAuth(WorkflowHelper workflowHelper, WorkflowItem workflowItem,
            UserItem loggedInUserItem, String baseDir) {
        String modifiedResults = null;
        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        List<WorkflowComponentAdjacencyItem> wfcAdjItems = wfcAdjDao.findByWorkflow(workflowItem);
        if (workflowItem != null) {
            HashMap<String, String> accessMap = null;
            String unmodifiedResults = workflowItem.getResults();

            accessMap = WorkflowAccessHelper.getComponentAccessMap(loggedInUserItem,
                    WorkflowFileUtils.getStrictDirFormat(baseDir), workflowItem);
            if (unmodifiedResults == null) {
                unmodifiedResults = new String("");
            }

            WorkflowComponentDao wcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
            try {
                List<String> foundComponents = new ArrayList<String>();
                Element tempResults = WorkflowXmlUtils.getStringAsElement("<LS_root_8675309>"
                    + unmodifiedResults + "</LS_root_8675309>");

                List<Element> deleteElems = new ArrayList<Element>();
                if (tempResults != null && tempResults.getChildren() != null && !tempResults.getChildren().isEmpty()) {
                    for (Iterator<Element> rootIter = tempResults.getChildren().iterator(); rootIter.hasNext(); ) {
                        Element outputElem = rootIter.next();
                        String compId = null;
                        if (outputElem.getChild("component_id") != null) {
                            compId = outputElem.getChildText("component_id");
                            if (!foundComponents.contains(compId)) {
                                foundComponents.add(compId);

                                if (!accessMap.containsKey(compId)
                                    || accessMap.get(compId) == null
                                        || !(accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                        || accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                    for (String dsElement : dataSensitiveElements) {
                                        if (outputElem.getChildren(dsElement) != null
                                                && !outputElem.getChildren(dsElement).isEmpty()) {
                                            deleteElems.addAll(outputElem.getChildren(dsElement));
                                        }
                                    }
                                } else {
                                    WorkflowComponentInstancePersistenceDao wfcipDao = DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
                                    WorkflowComponentInstancePersistenceItem wfcipItem =
                                        wfcipDao.findByWorkflowAndId(workflowItem, compId);

                                    if (wfcipItem != null && wfcipItem.isDirty()
                                            && !loggedInUserItem.getId().equals(workflowItem.getOwner().getId())) {
                                        for (String dsElement : dataSensitiveElements) {
                                            if (outputElem.getChildren(dsElement) != null
                                                    && !outputElem.getChildren(dsElement).isEmpty()) {
                                                deleteElems.addAll(outputElem.getChildren(dsElement));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (String componentId : accessMap.keySet()) {
                    if (componentId != null) {
                        String componentType = null;
                        if (componentId != null && componentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)) {
                            componentType = componentId.substring(0, componentId.indexOf("-1-x"));
                        }
                        String restrictString = null;
                        if (accessMap.get(componentId) != null && (accessMap.get(componentId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                || accessMap.get(componentId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                            restrictString = new String("");
                        } else {
                            restrictString = new String("<restricted />");
                        }

                        if (!foundComponents.contains(componentId) && componentType != null) {
                            String emptyOutput = "<output0>"
                                + "<component_id>" + componentId + "</component_id>"
                                + "<component_id_human>" + componentType + "</component_id_human>"
                                + "<component_type>" + componentType + "</component_type>"
                                + "<component_name>" + componentId + "</component_name>"
                                + "<elapsed_seconds>0</elapsed_seconds>"
                                + "<errors>No workflow results.</errors>"
                                + "<files />"
                                + "<inputmeta />"
                                + "<optionmeta />"
                                + restrictString
                                + "</output0>";
                            tempResults.addContent(WorkflowXmlUtils.getStringAsElement(emptyOutput).detach());
                        }
                    }
                }


                for (Element dsElement : deleteElems) {

                    dsElement.removeContent();
                }

                if (tempResults != null && !tempResults.getChildren().isEmpty()) {
                    for (Element outNode : (List<Element>) tempResults.getChildren()) {
                        if (outNode != null && !outNode.getChildren().isEmpty()) {

                                if (outNode.getChild("component_id") != null) {
                                    String cId = outNode.getChildTextTrim("component_id");
                                    if (accessMap.get(cId) == null || !(accessMap.get(cId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                            || accessMap.get(cId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                        outNode.addContent(new Element("restricted"));
                                    }
                                }

                        }
                    }
                }

                String tempResultsString = WorkflowXmlUtils.getElementAsString(tempResults)
                    .replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "").trim();

                String modifiedResultsString = WorkflowAccessHelper.removePrivateOptionMetaData(
                    tempResultsString, loggedInUserItem, workflowItem.getOwner());
                if (modifiedResultsString != null) {
                    modifiedResults = modifiedResultsString.replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "");
                }

            } catch (JDOMException e) {
                logger.error("Could not convert results to element.");
            } catch (IOException e) {
                logger.error("Could not create new element.");
            }


        }
        return modifiedResults;
    }

    public void moveToOrCreateFolder(WorkflowFolderDao wffDao, UserItem loggedInUserItem,
            String safeFolderName, WorkflowItem newWorkflowItem) {

        WorkflowFolderMapDao wffMapDao = DaoFactory.DEFAULT.getWorkflowFolderMapDao();
        UserItem newOwner = loggedInUserItem;
        if (loggedInUserItem.getAdminFlag()
            && !newWorkflowItem.getOwner().getId().equals(loggedInUserItem.getId())) {
            newOwner = newWorkflowItem.getOwner();
        }

        List<WorkflowFolderItem> wffItems =
                wffDao.findByUserAndName(newOwner, safeFolderName);

            // Remove the workflow from its existing folder (if one exists)
            List<WorkflowFolderItem> wffList = wffDao.findByWorkflow(newWorkflowItem);
            for (WorkflowFolderItem oldWffItem : wffList) {
                WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                wffMapItem.setWorkflowExternal(newWorkflowItem);
                wffMapItem.setWorkflowFolderExternal(oldWffItem);
                wffMapDao.delete(wffMapItem);
            }

            if (wffItems != null && !wffItems.isEmpty()) {
                logger.error("A folder named \"" + safeFolderName + "\" already exists."
                    + " Using existing folder.");
                WorkflowFolderItem wffItem = wffItems.get(0);
                // mck dup 9

                WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                wffMapItem.setWorkflowExternal(newWorkflowItem);
                wffMapItem.setWorkflowFolderExternal(wffItem);
                wffMapDao.saveOrUpdate(wffMapItem);
            } else {
                WorkflowFolderItem wffItem = new WorkflowFolderItem();

                wffItem = new WorkflowFolderItem();
                wffItem.setLastUpdated(new Date());
                wffItem.setOwner(newOwner);
                wffItem.setWorkflowFolderName(safeFolderName);
                wffDao.saveOrUpdate(wffItem);

                WorkflowFolderMapItem wffMapItem = new WorkflowFolderMapItem();
                wffMapItem.setWorkflowExternal(newWorkflowItem);
                wffMapItem.setWorkflowFolderExternal(wffItem);
                wffMapDao.saveOrUpdate(wffMapItem);

                logger.debug("Created new folder: " + safeFolderName
                        + " and added workflow: " + newWorkflowItem.getWorkflowName());

            }
    }

    /**
    * Creates a persistent file and associates it with a workflow.
    * @param workflowItem the workflow item
    * @param userItem the user CAN BE NULL
    * @param outputDir the output dir
    * @param file the file
    * @param fileLabel the workflow file type (e.g., student-step, tab-delimited, file, etc.)
    * @param fileIndex the file description (index)
    * @return
    */
   public static Integer createOrGetWorkflowFile(WorkflowItem workflowItem, UserItem userItem, String baseDir,
           File file, String fileLabel, String fileIndex, DatasetItem datasetItem, String componentId, Boolean executionFlag) {
       if (file != null && file.exists() && file.isFile() && file.canRead()
               && userItem != null && workflowItem != null
               && (userItem.getId().equals(UserItem.SYSTEM_USER) || userItem.getId().equals(workflowItem.getOwner().getId()))) {

           staticLogger.trace("Create or get persistent file: " + file.getAbsolutePath());
           WorkflowFileItem dsFileItem = null;
           ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
           ComponentFilePersistenceDao compFilePersistenceDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
           WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();

           String parentPath = file.getParentFile().getAbsolutePath();
           String sanitizedParentPath = WorkflowFileUtils.sanitizePath(parentPath + "/");
           // Get the path relative to the DataShop baseDir (e.g., workflows/1/components/Import-x-102942/output/)
           String fileDir = sanitizedParentPath.replaceAll(WorkflowFileUtils.getStrictDirFormat(baseDir), "");
           String mimeType = null;
           try {
               mimeType = WorkflowFileUtils.getMimeType(file);
           } catch (IOException e) {
               staticLogger.error("Could not retrieve mime-type for file: " + file.toString());
           }
           List<WorkflowFileItem> fileList =  wfFileDao.find(fileDir, file.getName());
           if (fileList != null && fileList.size() > 0) {

               if (userItem.getId().equals(UserItem.SYSTEM_USER)) {
                   // System-created files do not get attached directly to workflows
                   dsFileItem = fileList.get(0);
                   List<ComponentFileItem> compFileItems = compFileDao.findByFile(dsFileItem);
                   if (compFileItems != null && !compFileItems.isEmpty()) {
                       dsFileItem = null;
                   }
               } else {
                   dsFileItem = fileList.get(0);
                   List<ComponentFileItem> compFileItems = compFileDao.findByFile(dsFileItem);
                   if (compFileItems != null && !compFileItems.isEmpty()) {
                       String compAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                               userItem, baseDir, workflowItem, componentId);
                       if (compAccessLevel == null ||
                           (!compAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                           && !compAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                           dsFileItem = null;
                       }
                   }
               }


           } else {

                // The only place a workflow file item can be created is here.
                dsFileItem = new WorkflowFileItem();
           }

           if (dsFileItem != null) {
               dsFileItem.setAddedTime(new Date());
               dsFileItem.setFileName(file.getName());
               dsFileItem.setFilePath(fileDir);
               dsFileItem.setFileSize(file.length());
               if (mimeType == null) {
                   mimeType = "application/octet-stream";
               }
               dsFileItem.setFileType(mimeType);
               dsFileItem.setOwner(userItem);
               dsFileItem.setTitle(fileLabel);
               dsFileItem.setDescription(fileIndex);
               wfFileDao.saveOrUpdate(dsFileItem);

               FileUtils.makeWorldReadable(file);
           }

           // The user is saving new file(s)
           if (!userItem.getId().equals(UserItem.SYSTEM_USER) && componentId != null) {

               List<ComponentFileItem> compFileItems = compFileDao.findByFile(dsFileItem);
               ComponentFileItem compFileItem = null;
               if (compFileItems.isEmpty() && dsFileItem != null) {
                   compFileItem = new ComponentFileItem(workflowItem,  componentId, datasetItem, dsFileItem);
                   compFileDao.saveOrUpdate(compFileItem);
               }

               if (executionFlag && dsFileItem != null) {
                   // Item is being executed so save created files to this persistence table
                   List<ComponentFilePersistenceItem> wipItems = compFilePersistenceDao.findByFile(dsFileItem);
                   ComponentFilePersistenceItem wipItem = null;
                   if (wipItems.isEmpty()) {
                       wipItem = new ComponentFilePersistenceItem(workflowItem,  componentId, datasetItem, dsFileItem);
                       compFileDao.saveOrUpdate(wipItem);
                   }
               }
           }
           if (dsFileItem != null) {
               return (Integer) dsFileItem.getId();
           }
       }
       return null;
   }

    /**
     * Create a new workflow item.
     *
     * @param workflowId the workflow Id
     * @param datasetItem the optional DatasetItem
     * @param owner the owner
     * @param newWorkflowName the new workflow name
     * @param workflowDescription the workflow description
     * @param isShared whether or not the workflow is public
     * @param isSaveAsNew whether or not to save this workflow as a new workflow
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @return the newly created workflow item
     * @see edu.cmu.pslc.datashop.workflows.WorkflowImportHelper#saveWorkflowToDatabase(WorkflowItem)
     */
    public WorkflowItem createWorkflowItem(WorkflowDao workflowDao, WorkflowHelper workflowHelper,
            Long workflowId, DatasetItem datasetItem, UserItem newWorkflowOwner,
                String newWorkflowName, String workflowDescription,
                    Boolean isShared, Boolean isSaveAsNew, String dsBaseDir,
                        HttpServletRequest req, HttpServletResponse resp) {

        String baseDir = WorkflowFileUtils.getStrictDirFormat(dsBaseDir);
        String escapedWorkflowName = WorkflowFileUtils.htmlEncode(newWorkflowName);
        // Ensure that the newWorkflowName is valid xml
        if (!WorkflowPropertiesHelper.isValidWorkflowName(escapedWorkflowName)) {
            return null;
        }

        Date now = new Date();
        // Try to create the new workflow, then return the workflow name and id
        WorkflowItem newWorkflowItem = new WorkflowItem();
        WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();

        newWorkflowItem.setWorkflowName(escapedWorkflowName);
        newWorkflowItem.setOwner(newWorkflowOwner);
        newWorkflowItem.setDescription(workflowDescription);

        newWorkflowItem.setGlobalFlag(isShared);
        newWorkflowItem.setIsRecommended(false);
        newWorkflowItem.setLastUpdated(now);
        newWorkflowItem.setState(WorkflowItem.WF_STATE_NEW);

        workflowDao.saveOrUpdate(newWorkflowItem);

        // If a dataset is associated with this workflow, then add an entry to the wf dataset map.
        if (datasetItem != null) {
            newWorkflowItem.addDataset(datasetItem);
            workflowDao.saveOrUpdate(newWorkflowItem);

            WorkflowDatasetMapDao wfdsMapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
            WorkflowDatasetMapItem wfdsMapItem = new WorkflowDatasetMapItem();
            wfdsMapItem.setId(new WorkflowDatasetMapId(newWorkflowItem, datasetItem));
            wfdsMapItem.setAddedBy(newWorkflowOwner);
            wfdsMapItem.setAddedTime(now);
            wfdsMapItem.setAutoDisplayFlag(true);
            wfdsMapDao.saveOrUpdate(wfdsMapItem);
        }

        WorkflowItem existingWorkflowItem = null;
        WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
        WorkflowPersistenceItem wpItem = new WorkflowPersistenceItem();

        if (workflowId != null) {
            existingWorkflowItem = workflowDao.get(workflowId);
            if (existingWorkflowItem != null) {
                String newWorkflowXml = null;
                WorkflowPersistenceItem persistentWf = wpDao.findByWorkflow(existingWorkflowItem);
                // Get the persistent workflow if it exists (not the live workflow)
                if (persistentWf != null) {
                    Hibernate.initialize(persistentWf.getWorkflowXml());
                    newWorkflowXml = persistentWf.getWorkflowXml();
                }

                // If this is a 'Save as' from an open, live WF, use that content.
                String digraphToCopy = getWorkflowXmlFromJson(req, resp); // mck3 look into this method
                if (digraphToCopy != null) {
                    newWorkflowXml = digraphToCopy;
                }

                Element resultsRoot = null;
                String existingResults = null;
                if (existingWorkflowItem.getResults() != null) {
                    existingResults = existingWorkflowItem.getResults();
                }

                String existingWorkflowXml = null;
                try {
                    existingWorkflowXml = WorkflowPropertiesHelper.updateWorkflowName(newWorkflowXml, escapedWorkflowName);
                } catch (IOException e1) {
                    logger.error("Could not update workflow name in XML.");
                }
                Element workflowRoot = null;

                if (existingWorkflowXml != null) {
                    try {
                        workflowRoot = WorkflowXmlUtils.getStringAsElement(existingWorkflowXml);
                        if (existingResults != null) {
                            resultsRoot = WorkflowXmlUtils.getStringAsElement("<outputs>" + existingResults + "</outputs>");
                        }
                    } catch (JDOMException e) {
                        logger.error("Info file is not valid XML.");
                    } catch (IOException e) {
                        logger.error("Info file could not be read.");
                    }

                    // Output results to save (data is accessible to user)
                    List<String> outputsToSave = new ArrayList<String>();
                    HashMap<String, String> componentAccessLevels = new HashMap<String, String>();
                    // Remove references to previous files and previous workflow ids
                    // from the component
                    // before saving it as a new workflow

                    HashMap<String, String> componentAccessLeves = new HashMap<String, String>();
                    ElementFilter componentElemFilter = new ElementFilter("component");
                    for (Iterator<Element> iter = workflowRoot.getDescendants(componentElemFilter); iter.hasNext();) {
                        Element componentElem = iter.next();
                        if (componentElem != null) {
                            if (componentElem.getChild("component_id") != null) {
                                String componentId = componentElem.getChildTextTrim("component_id");
                                String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                    newWorkflowOwner, baseDir, existingWorkflowItem, componentId);
                                componentAccessLevels.put(componentId, componentAccessLevel);

                            }
                        }
                    }

                    List<String> unsatisfiedComponentAccess = new ArrayList<String>();

                    ElementFilter filesElementFilter = new ElementFilter("files");
                    for (Iterator<Element> iter = workflowRoot.getDescendants(filesElementFilter); iter.hasNext();) {
                        Element filesElement = iter.next();
                        // if workflowitem.edit or view, then copy files to new workflow
                        // component_file_persistence, workflow_file, workflow_file_map, workflow_component_instance_persistence;


                        for (Element fileElement : (List<Element>) filesElement.getChildren()) {

                            // Each input must contain a file element and a model element to be valid.
                            if (fileElement != null
                                && fileElement.getChild("file_path") != null
                                    && fileElement.getChild("file_name") != null) {

                                String filePathStr = fileElement.getChild("file_path").getTextTrim();
                                if (filePathStr.matches("[0-9]+")) {
                                    String newComponentId = null;
                                    if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                            && fileElement.getParentElement().getParentElement().getParentElement() != null
                                            && fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                        newComponentId = fileElement.getParentElement().getParentElement()
                                            .getParentElement().getChildTextTrim("component_id");

                                    } else if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                            && fileElement.getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                        newComponentId = fileElement.getParentElement().getParentElement()
                                            .getChildTextTrim("component_id");

                                    }
                                    if (newComponentId != null) {
                                        Integer wfFileId = Integer.parseInt(filePathStr);
                                        WorkflowFileItem wfFileItem = wfFileDao.get(wfFileId);
                                        if (wfFileItem != null) {
                                            String componentAccessLevel = componentAccessLevels.get(newComponentId);
                                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                                outputsToSave.add(newComponentId);
                                                WorkflowFileItem newFileItem = saveWorkflowFileAsWorkflowFile(workflowHelper,
                                                        newWorkflowItem.getId(), newComponentId,
                                                        wfFileItem.getTitle(), wfFileItem.getDescription(), wfFileItem, baseDir, newWorkflowOwner);
                                                Element fpElem = fileElement.getChild("file_path");
                                                if (newFileItem != null && newFileItem.getOwner().getId().equals(newWorkflowOwner.getId())) {
                                                    fpElem.setText(newFileItem.getId().toString());
                                                }
                                            } else {
                                                fileElement.removeContent();
                                                unsatisfiedComponentAccess.add(newComponentId);
                                            }
                                        }
                                    } else {
                                        fileElement.removeContent();
                                        unsatisfiedComponentAccess.add(newComponentId);
                                    }
                                }
                            }
                        }
                    }

                    // Copy .bak files
                    File componentsDir = new File(baseDir + "/"
                        + WorkflowFileUtils.getWorkflowsDir(existingWorkflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);
                    File newComponentsDir = new File(baseDir + "/"
                            + WorkflowFileUtils.getWorkflowsDir(newWorkflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);

                    newComponentsDir.mkdirs();
                    FileUtils.makeWorldReadable(newComponentsDir);


                    // Set the workflow id in the workflow XML to the new id
                    List<Element> workflowIdElements = new ArrayList<Element>();
                    ElementFilter workflowIdElementFilter = new ElementFilter("id");
                    for (Iterator<Element> iter = workflowRoot.getDescendants(workflowIdElementFilter); iter.hasNext();) {
                        Element workflowIdElement = iter.next();
                        workflowIdElements.add(workflowIdElement);
                        break;
                    }

                    for (Element workflowIdElement : workflowIdElements) {
                        workflowIdElement.setText(newWorkflowItem.getId().toString());
                    }
                    // Save XML and optionally the Results to the newWorkflowItem
                    try {

                        if (resultsRoot != null && resultsRoot.getChildren() != null) {
                            List<Element> resChilds = resultsRoot.getChildren();
                            StringBuffer sbuf = new StringBuffer();
                            for (Element res : resChilds) {
                                if (res.getChildTextTrim("component_id") != null
                                        && outputsToSave.contains(res.getChildTextTrim("component_id"))) {
                                    sbuf.append(WorkflowXmlUtils.getElementAsString(res));
                                } else if (res.getChildTextTrim("component_id") != null) {
                                    // This must (and will) be filtered by access later in the results methods.
                                    sbuf.append(WorkflowXmlUtils.getElementAsString(res));
                                }
                            }
                            if (!sbuf.toString().isEmpty()) {
                                String modifiedResults = filterResultsByAuth(
                                        workflowHelper, existingWorkflowItem, newWorkflowOwner, baseDir);

                                Element modifiedResultsElem = WorkflowXmlUtils.getStringAsElement(
                                        "<LS_root_8675309>" + modifiedResults + "</LS_root_8675309>");

                                ElementFilter filesElementFilter2 = new ElementFilter("files");
                                if (modifiedResultsElem != null) { // optionmeta.Import : [[Text: {"fileName":"ds4_student_step_All_Data_2_2018_1026_102957.txt","importFileType":"file","importFileNameTitle":"Project: Unclassified\r\nDataset: wtf of unicode\r\nFile: ds4_student_step_All_Data_2_2018_1026_102957.txt","fileTypeSelection":"file","datasetListSelection":"372","uploadLocation":"importDataset","searchDatasetsString":"","datasetLink":"4","datasetName":"wtf of unicode","fileId":"434"}]]
                                    for (Iterator<Element> iter = modifiedResultsElem.getDescendants(filesElementFilter); iter.hasNext();) {
                                        Element filesElement = iter.next();
                                        // if workflowitem.edit or view, then copy files to new workflow
                                        // component_file_persistence, workflow_file, workflow_file_map, workflow_component_instance_persistence;


                                        for (Element fileElement : (List<Element>) filesElement.getChildren()) {

                                            // Each input must contain a file element and a model element to be valid.
                                            if (fileElement != null
                                                && fileElement.getChild("file_path") != null
                                                    && fileElement.getChild("file_name") != null) {

                                                String filePathStr = fileElement.getChild("file_path").getTextTrim();
                                                if (filePathStr.matches("[0-9]+")) {
                                                    String newComponentId = null;
                                                    if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                            && fileElement.getParentElement().getParentElement().getParentElement() != null
                                                            && fileElement.getParentElement().getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                        newComponentId = fileElement.getParentElement().getParentElement()
                                                            .getParentElement().getChildTextTrim("component_id");

                                                    } else if (fileElement.getParentElement() != null && fileElement.getParentElement().getParentElement() != null
                                                            && fileElement.getParentElement().getParentElement().getChildTextTrim("component_id") != null) {
                                                        newComponentId = fileElement.getParentElement().getParentElement()
                                                            .getChildTextTrim("component_id");

                                                    }
                                                    if (newComponentId != null) {
                                                        Integer wfFileId = Integer.parseInt(filePathStr);
                                                        WorkflowFileItem wfFileItem = wfFileDao.get(wfFileId);
                                                        if (wfFileItem != null) {
                                                            String componentAccessLevel = componentAccessLevels.get(newComponentId);
                                                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                                                if (!outputsToSave.contains(newComponentId)) {
                                                                    outputsToSave.add(newComponentId);
                                                                }
                                                                WorkflowFileItem newFileItem = saveWorkflowFileAsWorkflowFile(workflowHelper,
                                                                        newWorkflowItem.getId(), newComponentId,
                                                                        wfFileItem.getTitle(), wfFileItem.getDescription(), wfFileItem, baseDir, newWorkflowOwner);
                                                                Element fpElem = fileElement.getChild("file_path");
                                                                if (newFileItem != null && newFileItem.getOwner().getId().equals(newWorkflowOwner.getId())) {
                                                                    fpElem.setText(newFileItem.getId().toString());
                                                                }
                                                            } else {
                                                                fileElement.removeContent();
                                                                unsatisfiedComponentAccess.add(newComponentId);
                                                            }
                                                        }
                                                    } else {
                                                        fileElement.removeContent();
                                                        unsatisfiedComponentAccess.add(newComponentId);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }


                                if (modifiedResultsElem != null) {
                                    String finalString = WorkflowXmlUtils.getElementAsString(modifiedResultsElem)
                                        .replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "");
                                    newWorkflowItem.setResults(finalString);
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Workflow root could not be read.");
                    } catch (JDOMException e) {
                        logger.error("Workflow modified results could not be read.");
                    }

                    // Save the progress
                    workflowDao.saveOrUpdate(newWorkflowItem);

                    // Use the existing workflow item to determine component access since
                    // it is a duplicate and the adjacency map has yet to be realized for the new workflow item.
                    HashMap<String, String> accessMap = WorkflowAccessHelper.getComponentAccessMap(newWorkflowOwner,
                            WorkflowFileUtils.getStrictDirFormat(baseDir), existingWorkflowItem);
                    Boolean notOwnedWorkflow = !(existingWorkflowItem.getOwner().getId().equals(newWorkflowOwner.getId()));

                    File[] files = componentsDir.listFiles();

                    // Setup new component instances
                    WorkflowComponentInstancePersistenceDao wciPersistenceDao =
                        DaoFactory.DEFAULT.getWorkflowComponentInstancePersistenceDao();
                    WorkflowComponentInstanceDao wciDao =
                            DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
                    // Keep track of the Data (Import) custom json elements
                    Map<String, Element> importJsons = new HashMap<String, Element>();

                    for (Iterator<Element> iter = workflowRoot.getDescendants(componentElemFilter); iter.hasNext();) {
                        Element componentElem = iter.next();
                        if (componentElem != null) {
                            if (componentElem.getChild("component_id") != null) {
                                String componentId = componentElem.getChildTextTrim("component_id");

                                for (int idx = 0; files != null && idx < files.length; idx++) {
                                    File fileOrDir = files[idx];
                                    String backupFilePath = WorkflowFileUtils.sanitizePath(fileOrDir.getAbsolutePath());
                                    if (fileOrDir.isFile()) {
                                        String componentMatchPath = WorkflowFileUtils.sanitizePath(baseDir + "/"
                                            + WorkflowFileUtils.getWorkflowsDir(existingWorkflowItem.getId())
                                                + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME + "/"
                                                + componentId + ".xml.bak");
                                        if (backupFilePath.endsWith(".bak") && componentMatchPath.equals(backupFilePath)) {
                                            String newContent = null;
                                            try {
                                                // If it's a Data- or Import- component, or if the user doesn't have
                                                // file access to it, then use the modified element.
                                                if (componentId.matches("Data-1-x.*") || componentId.matches("Import-1-x.*")
                                                        || unsatisfiedComponentAccess.contains(componentId)) {

                                                    if (unsatisfiedComponentAccess.contains(componentId)) {
                                                        // Custom import optionmeta
                                                        ElementFilter optionsFilter = new ElementFilter("options");
                                                        for (Iterator<Element> iter2 = componentElem.getDescendants(optionsFilter); iter2.hasNext();) {
                                                            Element optionElement = iter2.next();
                                                            if (optionElement.getName().equals("options")
                                                                    && optionElement.getChild("Import") != null) {
                                                                importJsons.put(componentId, optionElement.getChild("Import")); // mckhere
                                                            }
                                                        }
                                                    }


                                                    // Finally, we can remove or modify the Data (Import) custom json
                                                    // without ConcurrentModificationException.
                                                    if (!importJsons.isEmpty()) {
                                                        for (String cId : importJsons.keySet()) {
                                                            Element importElem = importJsons.get(cId);

                                                            if (unsatisfiedComponentAccess.contains(cId)) {
                                                                importElem.detach();
                                                            } else {
                                                                // update the file_path even though the json
                                                                // only matters to the UI, as the back-end
                                                                // uses the <options><files> elements instead.
                                                                JSONObject jsonObj = null;
                                                                try {
                                                                    jsonObj = new JSONObject(importElem.getTextTrim());
                                                                } catch (JSONException e) {

                                                                }
                                                                if (jsonObj != null) {
                                                                    try {
                                                                        jsonObj.put("file_path", "");
                                                                    } catch (JSONException e) {

                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    newContent = WorkflowXmlUtils.getElementAsString(componentElem);
                                                } else {
                                                    // Otherwise, access is allowed for the component files
                                                    // so we simply replace the workflow ids.
                                                    String fileContent = WorkflowFileUtils.readFile(backupFilePath);
                                                    try {
                                                        Element filePersistentElement = WorkflowXmlUtils.getStringAsElement(fileContent);
                                                        String compId = null;
                                                        if (filePersistentElement != null
                                                            && filePersistentElement.getChild("component_id") != null) {
                                                            compId = filePersistentElement.getChildText("component_id");
                                                        }
                                                        // If access to the component's ancestor is not view/edit, then
                                                        // use the previously updated componentElem
                                                        if (!accessMap.containsKey(compId)
                                                                || accessMap.get(compId) == null
                                                                    || !(accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_EDIT)
                                                                    || accessMap.get(compId).equalsIgnoreCase(WorkflowItem.LEVEL_VIEW))) {
                                                                newContent = WorkflowXmlUtils.getElementAsString(componentElem);
                                                                unsatisfiedComponentAccess.add(compId);
                                                            } else {
                                                                // Else the user can access the component's ancenstor
                                                                // so retain the input metadata, such as header info
                                                                newContent = fileContent;
                                                            }


                                                    } catch (JDOMException e) {
                                                        logger.error("Could not filter file persistent component XML");
                                                    }


                                                }
                                            } catch (IOException e) {
                                                logger.error("Cannot parse new workflow XML.");
                                            }

                                            String componentXmlFilePath = newComponentsDir + "/" + fileOrDir.getName();
                                            File componentXmlFile = new File(componentXmlFilePath);
                                            File backupFile = new File(backupFilePath);
                                            if (backupFile != null && backupFile.exists()) {
                                                // Replace instances of the original workflow id with the new id

                                                if (newContent != null) {


                                                    String componentXml = WorkflowAccessHelper.removeDataFromPrivateOptions(
                                                        newContent, newWorkflowItem, newWorkflowOwner, baseDir, notOwnedWorkflow);

                                                    WorkflowFileUtils.writeFile(componentXmlFilePath,
                                                            componentXml.replaceAll("<workflow_id>" + workflowId + "</workflow_id>",
                                                            "<workflow_id>" + newWorkflowItem.getId() + "</workflow_id>")
                                                        .replaceAll("workflows/" + workflowId + "/",
                                                                "workflows/" + newWorkflowItem.getId() + "/")
                                                        .replaceAll("<LS_root_8675309>", "").replaceAll("</LS_root_8675309>", "")
                                                    );
                                                }
                                            }
                                        }
                                    }
                                } // end for loop


                                try {
                                    newWorkflowItem.setWorkflowXml(WorkflowAccessHelper.removeDataFromPrivateOptions(
                                            WorkflowXmlUtils.getElementAsString(workflowRoot), newWorkflowItem,
                                                newWorkflowOwner, baseDir, notOwnedWorkflow));
                                } catch (IOException e) {
                                    logger.error("Could not update workflow XML");
                                }

                                // Save the progress
                                workflowDao.saveOrUpdate(newWorkflowItem);

                                Boolean hasComponentWarning = false;
                                WorkflowComponentInstancePersistenceItem existingWcip =
                                        wciPersistenceDao.findByWorkflowAndId(existingWorkflowItem, componentId);

                                // workflow_id, component_name, dirty_file, dirty_option, state = new, depth_level = 0, errors = null
                                WorkflowComponentInstanceItem wciItem = new WorkflowComponentInstanceItem();
                                wciItem.setComponentName(componentId);
                                wciItem.setDirtyOption(true);
                                wciItem.setWorkflow(newWorkflowItem);
                                if (existingWcip != null) {
                                    wciItem.setDepthLevel(existingWcip.getDepthLevel());
                                    wciItem.setDirtyAddConnection(existingWcip.getDirtyAddConnection());
                                    wciItem.setDirtyAncestor(existingWcip.getDirtyAncestor());
                                    wciItem.setDirtyDeleteConnection(existingWcip.getDirtyDeleteConnection());
                                    wciItem.setDirtyFile(existingWcip.getDirtyFile());
                                    wciItem.setDirtyOption(existingWcip.getDirtyOption());
                                    wciItem.setDirtySelection(existingWcip.getDirtySelection());
                                    wciItem.setErrors(existingWcip.getErrors());
                                    wciItem.setWarnings(existingWcip.getWarnings());
                                    if (unsatisfiedComponentAccess.contains(existingWcip.getComponentName())) {
                                        wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                    } else {
                                        wciItem.setState(existingWcip.getState());
                                        if (existingWcip.getState().equalsIgnoreCase(
                                            WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                            hasComponentWarning = true;
                                        }
                                    }
                                } else {
                                    wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                }

                                wciDao.saveOrUpdate(wciItem);

                                // Copy Debugging info
                                copyDebugFiles(baseDir, componentId, existingWorkflowItem, newWorkflowItem, newWorkflowOwner, hasComponentWarning);

                            }
                        }
                    }
                    try {
                        newWorkflowItem.setWorkflowXml(WorkflowAccessHelper.removeDataFromPrivateOptions(
                                WorkflowXmlUtils.getElementAsString(workflowRoot), newWorkflowItem,
                                    newWorkflowOwner, baseDir, notOwnedWorkflow));
                    } catch (IOException e) {
                        logger.error("Could not update workflow XML: " + e.toString());
                    }
                    // Save the progress
                    workflowDao.saveOrUpdate(newWorkflowItem);

                    workflowHelper.saveWorkflowToDatabase(newWorkflowItem, baseDir);

                } else {
                    workflowId = null;
                }
            } else {
                workflowId = null;
            }
        }


        if (existingWorkflowItem == null) {
            String componentsXml = "";
            newWorkflowItem.setWorkflowXml("<workflow>" + "<id>" + newWorkflowItem.getId() + "</id>" + "<name>"
                    + WorkflowFileUtils.htmlEncode(newWorkflowItem.getWorkflowName()) + "</name>"
                    + "<isShared>" + isShared.toString()
                    + "</isShared>" + "<lastUpdated>" + DATE_FORMAT.format(newWorkflowItem.getLastUpdated())
                    + "</lastUpdated>" + componentsXml + "</workflow>");
        }


        newWorkflowItem.setLastUpdated(now);
        workflowDao.saveOrUpdate(newWorkflowItem);

        if (Hibernate.isPropertyInitialized(newWorkflowItem, "workflowXml")) {

            wpItem.setWorkflow(newWorkflowItem);
            String lastWorkflowXml = newWorkflowItem.getWorkflowXml();
            if (lastWorkflowXml != null) {
                wpItem.setWorkflowXml(lastWorkflowXml);
                wpItem.setLastUpdated(now);
                wpDao.saveOrUpdate(wpItem);
                // Remove the component instance persistence objects
                WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                        .getWorkflowComponentInstancePersistenceDao();
                List<WorkflowComponentInstancePersistenceItem> wciPersistList = wciPersistDao
                        .findByWorkflow(newWorkflowItem);
                if (wciPersistList != null) {
                    for (WorkflowComponentInstancePersistenceItem wciPersistenceItem : wciPersistList) {
                        wciPersistDao.delete(wciPersistenceItem);
                    }
                }
            }
        }

        workflowHelper.saveComponentInstances(newWorkflowItem, baseDir);
        workflowHelper.saveComponentXmlFiles(existingWorkflowItem, newWorkflowItem, baseDir);



        return newWorkflowItem;
    }

    /**
     * Copies debug files if owner, or creates dummy debug file if non-owner.
     * @param baseDir the workflow base dir
     * @param componentId the component id
     * @param existingWorkflowItem the existing workflow
     * @param newWorkflowItem the new workflow
     * @param newWorkflowOwner the new workflow owner
     * @param hasComponentWarning whether or not the component is in the completed_warn state
     */
    public static void copyDebugFiles(String baseDir, String componentId,
            WorkflowItem existingWorkflowItem, WorkflowItem newWorkflowItem,
                UserItem newWorkflowOwner, Boolean hasComponentWarning) {
        String oldComponentPath = WorkflowFileUtils.sanitizePath(
                baseDir + "/" + WorkflowFileUtils.getWorkflowsDir(existingWorkflowItem.getId())
                        + "/" + componentId + "/output/");
        String newComponentPath = WorkflowFileUtils.sanitizePath(
                baseDir + "/" + WorkflowFileUtils.getWorkflowsDir(newWorkflowItem.getId()) + "/"
                        + componentId + "/output/");
        File oldComponentDir = new File(oldComponentPath);

        File[] debugLogFiles = oldComponentDir.listFiles();
        for (int idx = 0; debugLogFiles != null && idx < debugLogFiles.length; idx++) {
            File fileOrDir = debugLogFiles[idx];
            if (fileOrDir.isFile() && (fileOrDir.getName().matches(".*\\.wfl")
                    || fileOrDir.getName().equals("WorkflowComponent.log"))) {
                File newFile = new File(newComponentPath + "/" + fileOrDir.getName());
                if (newFile.exists()) {
                    newFile.delete();
                }
                try {
                    if (newWorkflowOwner != null && newWorkflowOwner.getId()
                            .equals(existingWorkflowItem.getOwner().getId())) {
                        // The owner gets all the previous debug file info.
                        FileUtils.copyFile(fileOrDir, newFile);

                    } else {
                        // Non-owners must re-run the workflow to get actual debug info.
                        String dummyDebugInfo = null;
                        if (hasComponentWarning) {
                            dummyDebugInfo = "WARN: You must re-run this component to generate debugging info.";
                        } else {
                            dummyDebugInfo = "INFO: You must re-run this component to generate debugging info.";
                        }
                        FileWriter fw = null;
                        BufferedWriter out = null;
                        try {
                            fw = new FileWriter(newFile);
                            out = new BufferedWriter(fw);
                            out.write(dummyDebugInfo);
                        } catch (IOException ioe) {
                            staticLogger.error("Could not write dummy debug file: " + newFile.toString(), ioe);
                        } finally {
                            try {
                                if (out != null) { out.close(); }
                            } catch (IOException ioe) {
                                staticLogger.error("copyDebugFiles: IOException.", ioe);
                            }
                        }
                    }
                } catch (IOException e) {
                    staticLogger.error("Could not copy debug file: " + newFile.toString(), e);
                }
            }
        }
    }

    /**
     * Saves an uploaded file to the system.
     * @param workflowId the workflow id
     * @param componentId the component id
     * @param fileTitle the file title (e.g. A, file1, myFile, etc.)
     * @param fileDesc the file description (e.g. Student-step, transaction, etc)
     * @param uploadFileItem the datashop file item
     * @param baseDir the files base directory, e.g. /datashop/files
     * @param datasetItem the dataset item
     * @param userItem the user item (file uploader/owner)
     * @return the FileItem
     * @throws Exception any exception
     */
    public WorkflowFileItem saveWorkflowFileAsWorkflowFile(WorkflowHelper workflowHelper,
            Long workflowId, String componentId,
                String fileTitle, String fileDesc, WorkflowFileItem existingFileItem, String baseDir,
                    UserItem userItem) {

        Boolean successFlag = false;
        WorkflowFileItem dsFileItem = null;
        if (existingFileItem != null) {

            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            WorkflowItem workflowItem = workflowDao.get(workflowId);
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            DatasetItem datasetItem = null;

            String subPath = WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/";

            String fullPath = WorkflowFileUtils.sanitizePath(existingFileItem.getFullPathName(baseDir))
                 + existingFileItem.getFileName();
            File testFile = new File(fullPath);

            if (testFile != null && testFile.isFile() && testFile.canRead()) {

                String fileFullName = null;
                if (fullPath.indexOf('/') >= 0) {
                    fileFullName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
                }
                File newDirectory = null;

                // Check to make sure the user has selected a file.
                if (fileFullName != null && fileFullName.length() > 0) {
                    // Create the directory
                    String wholePath = WorkflowFileUtils.sanitizePath(baseDir + "/" + subPath);
                    newDirectory = new File(wholePath);
                    if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                        FileUtils.makeWorldReadable(newDirectory);
                    } else {
                        logger.error("saveWorkflowFileAsWorkflowFile: Creating directory failed " + newDirectory);
                    }
                } else {
                    logger.error("saveWorkflowFileAsWorkflowFile: The fileName cannot be null or empty.");
                }
                List<String> fileList = Arrays.asList(newDirectory.list());
                String fileNamePrefix = null;
                String newFileName = null;
                String fileExt = "";
                File tmpFile = new File(fullPath);
                Boolean moveFile = false;
                File inputFile = null;
                List<String> textExtensions = Arrays.asList(new String[] { ".txt", ".csv", ".tsv", ".log" });

                if (tmpFile.getName().matches((".*\\.zip"))) {

                    try {
                        inputFile = File.createTempFile("wf" + workflowId, ".tmp", newDirectory);
                        moveFile = true;
                        newFileName = WorkflowFileUtils.unzipFile(tmpFile, inputFile);

                    } catch (IOException e) {
                        logger.error("Could not unzip file " + tmpFile.getAbsolutePath() + " to "
                                + inputFile.getAbsolutePath());
                    }

                } else {
                    inputFile = tmpFile;
                    newFileName = tmpFile.getName();
                    fileNamePrefix = tmpFile.getName();
                }

                String mimeType = null;

                try {
                    mimeType = WorkflowFileUtils.getMimeType(inputFile);
                } catch (IOException e) {
                    logger.error("Tika could not retrieve mime type of file " + inputFile.getAbsolutePath());
                }
                // When taken as a zipEntry, some systems can mistakenly
                // identify the mime type as application/octet-stream.
                // This fixes that. (UPDATE: TAKING OUT PRIOR TO TESTING.. I THINK IT ISN'T NEEDED)
                if (tmpFile.getName().matches((".*\\.zip")) && mimeType != null
                        && mimeType.equalsIgnoreCase("application/octet-stream") && fileExt != null
                        && textExtensions.contains(fileExt)) {
                    // mimeType = "text/plain";
                }

                if (inputFile != null && newDirectory != null) {
                    // Write the file to the directory
                    File newFile = new File(newDirectory.getAbsolutePath(), newFileName);
                    Hibernate.initialize(workflowItem);
                    if (workflowItem != null && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING)) {
                        try {
                            // If we created a temporary text file
                            // while extracting from a zip, then move the file
                            ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();

                            // This method is also used for saving new files from existing workflow_file items.
                            List<ComponentFileItem> existingCfItems = compFileDao.findByFile(existingFileItem);
                            if (!existingCfItems.isEmpty()) {
                                ComponentFileItem existingCfItem = existingCfItems.get(0);
                                if (datasetItem == null && existingCfItem.getDataset() != null) {
                                    datasetItem = datasetDao.get((Integer) existingCfItem.getDataset().getId());
                                }
                            }

                            if (newFile.exists()) {
                                newFile.delete();
                            }

                            if (moveFile) {
                                Files.move(inputFile.toPath(), newFile.toPath());
                            // Otherwise, we will copy the file.
                            } else {
                                FileUtils.copyFile(inputFile, newFile);
                            }
                            Integer fileId = createOrGetWorkflowFile(
                                workflowItem, userItem, baseDir,
                                        newFile, fileTitle, "0", datasetItem, componentId, false);
                            WorkflowFileDao workflowFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                            if (fileId != null) {
                                dsFileItem = workflowFileDao.get(fileId);
                                successFlag = true;
                            }
                        } catch (IOException e) {
                            logger.error("Could not copy file " + inputFile.getAbsolutePath() + " to "
                                    + newFile.getAbsolutePath());
                        }
                    }

                }
            }
        }
        if (successFlag) {
            return dsFileItem;
        } else {
            return null;
        }
    }

    /**
     * This method deletes import items for a given workflow and component id. The method will
     * be replaced with more advanced mechanics when shared editing is implemented.
     * @param compFileDao the ComponentFileDao
     * @param workflowItem the workflow item
     * @param componentId the component ID
     */
    public void deleteComponentFileItems(ComponentFileDao compFileDao, WorkflowItem workflowItem, String baseDir) {

        List<ComponentFileItem> cfItems = compFileDao.findByWorkflow(workflowItem);
        List<String> filePaths = new ArrayList<String>();
        if (cfItems != null) {
            for (ComponentFileItem cfItem : cfItems) {
                if (cfItem.getFile() != null) {
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    WorkflowFileItem componentXmlFile = wfFileDao.get((Integer) cfItem.getFile().getId());
                    String filePath = WorkflowFileUtils.sanitizePath(componentXmlFile.getFullFileName(
                            WorkflowFileUtils.getStrictDirFormat(baseDir)));
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                }
                compFileDao.delete(cfItem);
            }

            if (!filePaths.isEmpty()) {
                for (String filePath: filePaths) {
                    File fileTest = new File(filePath);
                    if (fileTest.exists() && fileTest.canWrite() && fileTest.isFile()) {
                        fileTest.delete();
                    }
                }
            }
        }

    }

    /**
     * This method deletes import items for a given workflow and component id. The method will
     * be replaced with more advanced mechanics when shared editing is implemented.
     * @param compFileDao the ComponentFileDao
     * @param workflowItem the workflow item
     * @param componentId the component ID
     */
    public void updateFileMappings(ComponentFileDao compFileDao, WorkflowItem workflowItem,
        String componentId, Boolean deleteFiles,
            String baseDir) {

        WorkflowFileDao wfDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        List<ComponentFileItem> compFileItems = compFileDao.findImportByComponent(workflowItem, componentId);
        List<String> filePaths = new ArrayList<String>();
        if (compFileItems != null) {
            for (Iterator<ComponentFileItem> wiIterator = compFileItems.iterator(); wiIterator.hasNext() ;) {
                ComponentFileItem compFileItem = wiIterator.next();
                compFileItem = compFileDao.get((Long) compFileItem.getId());
                if (compFileItem.getFile() != null) {
                    WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                    WorkflowFileItem componentXmlFile = wfFileDao.get((Integer) compFileItem.getFile().getId());
                    String filePath = WorkflowFileUtils.sanitizePath(componentXmlFile.getFullFileName(
                            WorkflowFileUtils.getStrictDirFormat(baseDir)));
                    if (filePath != null) {
                        filePaths.add(filePath);
                    }
                }

                if (deleteFiles) { // Will also delete component_file_persistence by FK constraint.
                    WorkflowFileItem itemExists = wfDao.find((Integer) compFileItem.getFile().getId());
                    if (itemExists != null) {
                        WorkflowFileItem wfItem = wfDao.find((Integer) itemExists.getId());
                        wfDao.delete(wfItem);
                    }

                } else {
                    // If workflow_file record is not deleted, then delete cascade never triggered
                    // Delete the compononent_file
                    compFileDao.delete(compFileItem);
                }
            }


            if (deleteFiles && !filePaths.isEmpty()) {
                for (String filePath: filePaths) {
                    File fileTest = new File(filePath);
                    if (fileTest.exists() && fileTest.canWrite() && fileTest.isFile()) {
                        fileTest.delete();
                    }
                }
            }
        }
    }

    public void addExportInfoObject(String filePath, JSONObject exportInfo) {
        if (datashopCachedExportsMap.containsKey(filePath)) {
            datashopCachedExportsMap.remove(filePath);
        }
        datashopCachedExportsMap.put(filePath, exportInfo);
    }

    public JSONObject getExportInfoObject(String filePath) {
        if (datashopCachedExportsMap.containsKey(filePath)) {
            return datashopCachedExportsMap.get(filePath);
        }
        return null;
    }

    /**
     * Writes a previously saved image to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param fileIdString the FileItem id as a string
     * @param request the HttpServletRequest
     * @param response the HttpServletResponseerror
     * @param disp the RequestDispatcher
     */
    public void returnImage(WorkflowDao workflowDao, WorkflowHelper workflowHelper,
        UserItem userItem, Integer fileId, String baseDir, RequestDispatcher disp,
            HttpServletRequest request, HttpServletResponse response, ServletContext servletCtx) {
        try {

            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            WorkflowFileItem fileItem = wfFileDao.get(fileId);
            File returnImage = null;

            if (fileItem != null && fileItem.getFileType() != null) {
                File imageFile = null;
                Boolean hasAccess = false;
                ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                List<ComponentFileItem> cfItems = compFileDao.findByFile(fileItem);
                WorkflowItem workflowItem = null;
                if (cfItems != null && !cfItems.isEmpty()) {
                    for (ComponentFileItem wfmItem : cfItems) {
                        workflowItem = workflowDao.get((Long) wfmItem.getWorkflow().getId());
                        if (workflowItem != null) {
                            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                    userItem, baseDir, workflowItem, wfmItem.getComponentId());
                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                hasAccess = true;
                            }
                        }
                    }

                }

                imageFile = new File(WorkflowFileUtils.sanitizePath(fileItem.getFullFileName(
                        WorkflowFileUtils.getStrictDirFormat(baseDir))));

                Boolean isAllowedImageExtension = fileItem.getFileType().matches("image/.+");

                if (isAllowedImageExtension) {
                    if (imageFile.exists()
                            && imageFile.isFile() && hasAccess) {
                        returnImage = imageFile;
                        response.setContentType(fileItem.getFileType().replaceAll("_", "+"));
                    } else {
                        String imagePath = servletCtx.getRealPath("/images/error_small.png");
                        returnImage = new File(imagePath);
                        response.setContentType("image/png");
                    }

                    imageFile = null;
                }
                ServletOutputStream out = response.getOutputStream();
                FileInputStream fin = new FileInputStream(returnImage);

                BufferedInputStream bin = new BufferedInputStream(fin);
                BufferedOutputStream bout = new BufferedOutputStream(out);
                int ch = 0;
                ;
                while ((ch = bin.read()) != -1) {
                    bout.write(ch);
                }

                bin.close();
                bout.close();

            }
        } catch (IOException e) {
            logger.error(e.toString());
        }

        return;
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
        Integer fileId, String baseDir, RequestDispatcher disp, HttpServletRequest request,
            HttpServletResponse response) {
        ServletOutputStream out = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;

        try {

            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            Boolean hasAccess = false;
            WorkflowFileItem fileItem = wfFileDao.get(fileId);
            File returnFile = null;
            if (fileItem != null) {
                ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                List<ComponentFileItem> cfItems = compFileDao.findByFile(fileItem);
                WorkflowItem workflowItem = null;
                if (cfItems != null && !cfItems.isEmpty()) {
                    for (ComponentFileItem wfmItem : cfItems) {
                        workflowItem = workflowDao.get((Long) wfmItem.getWorkflow().getId());
                        if (workflowItem != null) {
                            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                    userItem, baseDir, workflowItem, wfmItem.getComponentId());
                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                hasAccess = true;
                            }
                        }
                    }

                }

                String newPath = WorkflowFileUtils.sanitizePath(fileItem.getFullFileName(
                        WorkflowFileUtils.getStrictDirFormat(baseDir)));

                File componentXmlFile = new File(newPath);

                if (hasAccess && componentXmlFile.exists() && componentXmlFile.isFile()) {
                    returnFile = componentXmlFile;
                } else {
                    returnFile = null;
                }
                componentXmlFile = null;

                response.setContentType(fileItem.getFileType());
                response.setContentLength((int) returnFile.length());
                // Set content disposition so that browsers will bring up the
                // Save As dialog for any mime type
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileItem.getFileName() + "\"");

                out = response.getOutputStream();
                fin = new FileInputStream(returnFile);
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

    /**
     * Writes an html file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param fileIdString the FileItem id as a string
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void returnHtmlFile(WorkflowDao workflowDao, WorkflowHelper workflowHelper,
        UserItem userItem, Integer fileId, String baseDir, RequestDispatcher disp,
            HttpServletRequest request, HttpServletResponse response) {
        ServletOutputStream out = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;

        try {

            WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
            Boolean hasAccess = false;
            WorkflowFileItem fileItem = wfFileDao.get(fileId);
            File returnFile = null;
            if (fileItem != null) {
                ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                List<ComponentFileItem> cfItems = compFileDao.findByFile(fileItem);
                WorkflowItem workflowItem = null;
                if (cfItems != null && !cfItems.isEmpty()) {
                    for (ComponentFileItem wfmItem : cfItems) {
                        workflowItem = workflowDao.get((Long) wfmItem.getWorkflow().getId());
                        if (workflowItem != null) {
                            String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                    userItem, baseDir, workflowItem, wfmItem.getComponentId());
                            if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                    || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                hasAccess = true;
                            }
                        }
                    }

                }

                String newPath = WorkflowFileUtils.sanitizePath(fileItem.getFullFileName(
                        WorkflowFileUtils.getStrictDirFormat(baseDir)));

                File componentXmlFile = new File(newPath);

                if (hasAccess && componentXmlFile.exists() && componentXmlFile.isFile()) {
                    returnFile = componentXmlFile;
                } else {
                    returnFile = null;
                }
                componentXmlFile = null;

                response.setContentType(fileItem.getFileType());
                response.setContentLength((int) returnFile.length());

                out = response.getOutputStream();
                fin = new FileInputStream(returnFile);
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

    /**
     * Writes an html file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param filePath the file path
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void returnHtmlFileFromPath(WorkflowDao workflowDao, WorkflowHelper workflowHelper,
        UserItem userItem, String path, String baseDir, RequestDispatcher disp,
            HttpServletRequest request, HttpServletResponse response) {
        ServletOutputStream out = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;

        String normalizedPath = path.replaceAll("\\\\", "/").replaceAll("/+", "/");
        Integer beginIndex = normalizedPath.lastIndexOf("/");
        if (beginIndex > 0) {
            String filePath = normalizedPath.substring(0, beginIndex + 1);
            String fileName = normalizedPath.substring(beginIndex + 1);
            Integer fileId = null;

            try {
                // Find the workflow associated with the requested file by the file path and name
                WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                Boolean hasAccess = false;
                List<WorkflowFileItem> fileItems = wfFileDao.find(filePath, fileName);
                File returnFile = null;
                if (fileItems != null && !fileItems.isEmpty()) {
                    WorkflowFileItem fileItem = fileItems.get(0);
                    fileId = (Integer) fileItem.getId();
                    ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                    List<ComponentFileItem> cfItems = compFileDao.findByFile(fileItem);
                    WorkflowItem workflowItem = null;
                    if (cfItems != null && !cfItems.isEmpty()) {
                        for (ComponentFileItem wfmItem : cfItems) {
                            workflowItem = workflowDao.get((Long) wfmItem.getWorkflow().getId());
                            if (workflowItem != null) {
                                String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                                        userItem, baseDir, workflowItem, wfmItem.getComponentId());
                                if (componentAccessLevel != null && (componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_VIEW)
                                        || componentAccessLevel.equalsIgnoreCase(WorkflowItem.LEVEL_EDIT))) {
                                    hasAccess = true;
                                }
                            }
                        }
                    }

                    // If the user is the owner of the file or the workflow exists and is global, then we can share the
                    // file.
                    String newPath = WorkflowFileUtils.sanitizePath(fileItem.getFullFileName(
                            WorkflowFileUtils.getStrictDirFormat(baseDir)));
                    logger.debug("Adding file " + fileId + " to response.");
                    File componentXmlFile = new File(newPath);

                    if (hasAccess && componentXmlFile.exists()
                            && componentXmlFile.isFile()) {
                        returnFile = componentXmlFile;
                    } else {
                        returnFile = null;
                    }
                    componentXmlFile = null;
                    if (returnFile != null) {
                        response.setContentType(fileItem.getFileType());
                        response.setContentLength((int) returnFile.length());

                        out = response.getOutputStream();
                        fin = new FileInputStream(returnFile);
                        bin = new BufferedInputStream(fin);
                        bout = new BufferedOutputStream(out);
                        int ch = 0;

                        while ((ch = bin.read()) != -1) {
                            bout.write(ch);
                        }

                        bin.close();
                        bout.close();
                    }

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
        }
        return;
    }

    /**
     * Writes an html file to the HttpServletResponse and forwards to the workflows jsp.
     *
     * @param userItem the logged in user
     * @param filePath the file path
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @param disp the RequestDispatcher
     */
    public void returnUnregisteredFile(WorkflowDao workflowDao, WorkflowHelper workflowHelper,
        UserItem loggedInUserItem, String baseDir, RequestDispatcher disp,
            HttpServletRequest request, HttpServletResponse response, String fileName) {

        String componentId = request.getParameter("componentId");
        String workflowDebugIdStr = request.getParameter("wfDebugId");
        if (componentId != null && componentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)
                && workflowDebugIdStr != null && workflowDebugIdStr.matches("[0-9]+")) {
            Long workflowDebugId = Long.parseLong(workflowDebugIdStr);
            WorkflowItem wfItem = workflowDao.get(workflowDebugId);
            if (wfItem.getOwner().getId().equals(loggedInUserItem.getId()) || loggedInUserItem.getAdminFlag()) {
                String subPath = WorkflowFileUtils.sanitizePath(
                        WorkflowFileUtils.getStrictDirFormat(baseDir)
                        + WorkflowFileUtils.getWorkflowsDir(wfItem.getId())
                            + componentId + "/output/");

                String path = subPath + fileName;

                ServletOutputStream out = null;
                FileInputStream fin = null;
                BufferedInputStream bin = null;
                BufferedOutputStream bout = null;

                try {

                    File returnFile = new File(path);

                    if (returnFile != null && returnFile.exists()) {
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + returnFile.getName() + "\"");
                        response.setContentType("text/plain");
                        response.setContentLength((int) returnFile.length());

                        out = response.getOutputStream();
                        fin = new FileInputStream(returnFile);
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
        }

        return;
    }

    /**
     * Convert the column headers to a JSONArray and return it.
     *
     * @param req the HttpServletRequest
     * @param resp the HttpServletResponse
     * @return the JSONArray of column headers
     * @throws JSONException
     */
    public JSONArray getColumnsAsJson(HttpServletRequest req, HttpServletResponse resp)
            throws JSONException {
        Map<String, Integer> columnHeaders = (Map<String, Integer>) req.getAttribute("columnHeaders");
        // Break the header into the name and index
        Integer columnHeaderDuplicateIndex = 0;
        JSONArray columnNameArray = new JSONArray();
        if (columnHeaders != null) {
            // This file has column headers

            for(Map.Entry<String, Integer> entry :  columnHeaders.entrySet()){
                JSONObject columnName = new JSONObject();

                // Model names
                columnName.put("id", "header" + columnHeaderDuplicateIndex);
                columnName.put("name", entry.getKey());
                // Max Opportunity
                columnName.put("index", entry.getValue() + "");
                columnNameArray.put(columnName);

                columnHeaderDuplicateIndex++;
            }

        }
        return columnNameArray;
    }


    public String getWorkflowXmlFromJson(HttpServletRequest req, HttpServletResponse resp) {
        String workflowXml = null;
        String jsonString = req.getParameter("digraphObject");

        if (jsonString == null || jsonString.trim().isEmpty()) { return null; }

        // Get the JSON object
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException exception) {
            logger.error("Invalid json object.");
        }
        // Convert the JSON object to XML
        Element digraphDoc = null;
        try {
            digraphDoc = WorkflowXmlUtils.convertJsonToXML(jsonObject);
        } catch (JSONException e) {
            logger.error("Error converting workflow to XML.");
        } catch (JDOMException e) {
            logger.error("Error parsing workflow.");
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding.");
        } catch (IOException e) {
            logger.error("Error opening workflow.");
        }
        // Test (validate) the XML created from the JSON object.

        try {
            workflowXml = WorkflowXmlUtils.getElementAsString(digraphDoc);
        } catch (IOException e1) {
            logger.error("Could not parse XML obtained from json object.");
        }

        if (workflowXml == null || workflowXml.isEmpty()) {
            workflowXml = null;
        }
        return workflowXml;
    }


}
