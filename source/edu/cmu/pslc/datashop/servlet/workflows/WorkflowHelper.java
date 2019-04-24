/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowDatasetMapDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.WorkflowPersistenceDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowUserLogDao;
import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstancePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentUserLogItem;
import edu.cmu.pslc.datashop.workflows.WorkflowDatasetMapItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;
import edu.cmu.pslc.datashop.workflows.WorkflowPersistenceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowUserLogItem;

/**
* Facilitate the management of workflows.
*
* @author
* @version $Revision: 11453 $
* <BR>Last modified by: $Author:  $
* <BR>Last modified on: $Date:  $
* <!-- $KeyWordsOff: $ -->
*/
public class WorkflowHelper {
    /* Maximum execution depth of a workflow, as the workflow is a digraph. */
    public static final int MAX_EXECUTIONS = 100;

   /** Debug logging. */
   private Logger logger = Logger.getLogger(getClass().getName());
   /** Debug logging. */
   private static Logger staticLogger = Logger.getLogger(WorkflowHelper.class.getName());
   /** Format for the date range method, getDateRangeString. */

   public static FastDateFormat formatDate = FastDateFormat.getInstance("MMM d, yyyy HH:mm:ss");

   public static final String BAD_FILEPATH_CHARS = "[:*?\"<>|\\s]+";

   public static final String LOG_CREATE_WORKFLOW = "Create workflow";
   public static final String LOG_DELETE_WORKFLOW = "Delete workflow";
   public static final String LOG_SAVE_WORKFLOW = "Save workflow";
   public static final String LOG_SAVE_AS_NEW_WORKFLOW = "Save as new workflow";
   public static final String LOG_RUN_WORKFLOW = "Run workflow";
   public static final String LOG_CANCEL_WORKFLOW = "Cancel workflow";

   public static final String LOG_DISPLAY_RESULTS = "Display workflow results";
   public static final String LOG_DISPLAY_COMPONENT_RESULTS = "Display component results";

   public static final String LOG_ATTACH_FILE_TO_DATASET = "Attach file to dataset";
   public static final String LOG_EDIT_WORKFLOW = "Edit workflow";
   public static final String LOG_UPLOAD_FILE = "Upload file";
   public static final String LOG_SELECT_DATASET_FILE = "Select dataset file";
   
   public static final String LOG_WEB_SERVICE_UPLOAD_FILE = "Upload file via web service";
   public static final String LOG_WEB_SERVICE_MODIFY_COMPONENT = "Modify component via web service";

   /** LearnSphere namespace. */
   public final static Namespace lsXmlNs = Namespace.getNamespace("ls", "http://learnsphere.org/ls");

   /** DataShop instance id. */
   private static final Long DATASHOP_INSTANCE_ID = 1L;
   // Most component logging options are in lsWorkflowEditor.js and WorkflowEditor.js prefixed with LOG_

   /** The component XML directory name, i.e. components. */
   public static final String COMPONENTS_XML_DIRECTORY_NAME = "components";

   public WorkflowHelper() {
       ComponentHelper.componentList = new JSONArray();
       ComponentHelper.authorList = new JSONArray();
   }


   /**
    * Updates the workflow XML to be in synch with the WorkflowItem attributes.
    * @param workflowItem the workflowItem
    * @return whether or not we were successful in updating the workflow XML
    */
   public Boolean saveTempWorkflowToDatabase(WorkflowItem workflowItem, WorkflowDao workflowDao) {
       synchronized(workflowItem) {
           ByteArrayOutputStream out = null;

            try {
                logger.trace("Begin saveWorkflowToDatabase");
                // Use the JDOM libs to synchronize the workflow XML to the workflow
                // Item
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setReuseParser(false);
                InputStream stream = new ByteArrayInputStream(workflowItem
                        .getWorkflowXml().getBytes("UTF-8"));
                Document digraphDom = saxBuilder.build(stream);
                stream.close();
                Element digraphDoc = digraphDom.getRootElement();

                // Get the XML attributes.
                Element idElement = digraphDoc.getChild("id");
                Element nameElement = digraphDoc.getChild("name");
                Element lastUpdatedElement = digraphDoc.getChild("lastUpdated");
                Element isSharedElement = digraphDoc.getChild("isShared");

                // Update attributes with user-given values.
                idElement.setText(workflowItem.getId().toString());
                nameElement.setText(workflowItem.getWorkflowName());
                String lastUpdated = formatDate.format(workflowItem
                        .getLastUpdated());
                if (lastUpdatedElement != null) {
                    lastUpdatedElement.setText(lastUpdated);
                }
                if (isSharedElement != null) {
                    isSharedElement.setText(workflowItem.getGlobalFlag().toString());
                }
                // Write the modified XML back to the database.
                String newWorkflowXml = null;
                try {
                    newWorkflowXml = WorkflowXmlUtils.getElementAsString(digraphDoc);
                    workflowItem.setWorkflowXml(newWorkflowXml);
                } catch (IOException e) {
                    logger.error("Could not convert workflow XML to string.");
                }

                workflowDao.saveOrUpdate(workflowItem);

                ///saveComponentInstances(workflowItem, getBaseDir());

                ///WorkflowHelper.saveComponentXmlFiles(workflowItem, getBaseDir());

                logger.trace("End saveWorkflowToDatabase");
            } catch (IOException e) {
                logger.error("IOException when saving XML to workflow item.");
                return false;
            } catch (JDOMException e) {
                logger.error("JDOMException when saving XML to workflow item.");
                return false;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) { }
                }
            }

       }
        logger.info("Successfully updated the workflow XML for WorkflowItem: " + workflowItem.getNameAndId());
        return true;
    }


/**
    * Updates the workflow XML to be in synch with the WorkflowItem attributes.
    * @param workflowItem the workflowItem
    * @return whether or not we were successful in updating the workflow XML
    */
   public Boolean saveWorkflowToDatabase(WorkflowItem workflowItem, String baseDir) {
       WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
       ByteArrayOutputStream out = null;

        try {
            staticLogger.trace("Begin saveWorkflowToDatabase");
            // Use the JDOM libs to synchronize the workflow XML to the workflow
            // Item
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setReuseParser(false);
            InputStream stream = new ByteArrayInputStream(workflowItem
                    .getWorkflowXml().getBytes("UTF-8"));
            Document digraphDom = saxBuilder.build(stream);
            stream.close();
            Element digraphDoc = digraphDom.getRootElement();

            // Get the XML attributes.
            Element idElement = digraphDoc.getChild("id");
            Element nameElement = digraphDoc.getChild("name");
            Element lastUpdatedElement = digraphDoc.getChild("lastUpdated");
            Element isSharedElement = digraphDoc.getChild("isShared");

            // Update attributes with user-given values.
            idElement.setText(workflowItem.getId().toString());
            nameElement.setText(workflowItem.getWorkflowName());
            String lastUpdated = formatDate.format(workflowItem
                    .getLastUpdated());
            if (lastUpdatedElement != null) {
                lastUpdatedElement.setText(lastUpdated);
            }
            if (isSharedElement != null) {
                isSharedElement.setText(workflowItem.getGlobalFlag().toString());
            }

            // Set workflow_id that exists inside component elements
            List<Element> workflowIdElements = new ArrayList<Element>();
            ElementFilter workflowIdElementFilter = new ElementFilter("workflow_id");
            for (Iterator<Element> iter = digraphDoc.getDescendants(workflowIdElementFilter); iter.hasNext();) {
                Element workflowIdElement = iter.next();
                workflowIdElements.add(workflowIdElement);
            }
            for (Element workflowIdElement : workflowIdElements) {
                workflowIdElement.setText(workflowItem.getId().toString());
            }

            // Write the modified XML back to the database.
            String newWorkflowXml = null;

            try {
                newWorkflowXml = WorkflowPropertiesHelper.updateWorkflowName(
                        WorkflowXmlUtils.getElementAsString(digraphDoc),
                        workflowItem.getWorkflowName());
            } catch (IOException e) {
                logger.error("Could not convert workflow XML to string.");
            }

            workflowItem.setWorkflowXml(newWorkflowXml);
            workflowDao.saveOrUpdate(workflowItem);

            // Save the previous workflow XML to the workflow persistence object
            WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
            WorkflowPersistenceItem wpItem = wpDao.findByWorkflow(workflowItem);
            if (wpItem == null) {
                // If a persistence item was not found, create one.
                wpItem = new WorkflowPersistenceItem();
                wpItem.setWorkflow(workflowItem);
            }
            wpItem.setWorkflowXml(workflowItem.getWorkflowXml());
            wpItem.setLastUpdated(workflowItem.getLastUpdated());
            wpDao.saveOrUpdate(wpItem);

            staticLogger.trace("End saveWorkflowToDatabase");
        } catch (IOException e) {
            staticLogger.error("IOException when saving XML to workflow item.");
            return false;
        } catch (JDOMException e) {
            staticLogger.error("JDOMException when saving XML to workflow item.");
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) { }
            }
        }


        staticLogger.info("Successfully updated the workflow XML for WorkflowItem: " + workflowItem.getNameAndId());
        return true;
    }

   /**
    * Restore the last saved workflow item and its component instances.
    * @param workflowItem the workflow item
    * @return the workflow xml
    */
   public String restoreSavedWorkflow(WorkflowItem workflowItem) {
       String workflowXml = null;

       if (workflowItem != null) {
           WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
           WorkflowPersistenceItem wpItem = wpDao.findByWorkflow(workflowItem);
           if (wpItem != null) {
               WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
               Hibernate.initialize(wpItem);
               workflowXml = wpItem.getWorkflowXml();
               workflowItem.setWorkflowXml(workflowXml);
               workflowItem.setLastUpdated(wpItem.getLastUpdated());
               workflowDao.saveOrUpdate(workflowItem);
           }

           WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
           // Remove the component instance objects
           List<WorkflowComponentInstanceItem> wciList = wciDao.findByWorkflow(workflowItem);
           if (wciList != null) {
               for (WorkflowComponentInstanceItem wciItem : wciList) {
                   Hibernate.initialize(wciItem);
                   wciDao.delete(wciItem);
               }
           }

           WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                   .getWorkflowComponentInstancePersistenceDao();
           List<WorkflowComponentInstancePersistenceItem> wcipItems = wciPersistDao.findByWorkflow(workflowItem);

           // Create the new component instance persistence objects
           if (wcipItems != null) {
               for (Iterator<WorkflowComponentInstancePersistenceItem> wciPersistIter = wcipItems.iterator(); wciPersistIter
                       .hasNext();) {
                   Integer wcipId = (Integer) ((WorkflowComponentInstancePersistenceItem) wciPersistIter.next())
                           .getId();
                   WorkflowComponentInstancePersistenceItem wcipItem = wciPersistDao.get(wcipId);
                   WorkflowComponentInstanceItem wciItem = new WorkflowComponentInstanceItem();
                   wciItem.setComponentName(wcipItem.getComponentName());
                   wciItem.setWorkflow(workflowItem);
                   wciItem.setDepthLevel(wcipItem.getDepthLevel());
                   wciItem.setDirtyAddConnection(wcipItem.getDirtyAddConnection());
                   wciItem.setDirtyAncestor(wcipItem.getDirtyAncestor());
                   wciItem.setDirtyDeleteConnection(wcipItem.getDirtyDeleteConnection());
                   wciItem.setDirtyFile(wcipItem.getDirtyFile());
                   wciItem.setDirtyOption(wcipItem.getDirtyOption());
                   wciItem.setDirtySelection(wcipItem.getDirtySelection());
                   wciItem.setState(wcipItem.getState());
                   wciItem.setErrors(wcipItem.getErrors());
                   wciItem.setWarnings(wcipItem.getWarnings());

                   wciDao.saveOrUpdate(wciItem);
               }
           }

       }
       return workflowXml;
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
    public WorkflowFileItem saveWorkflowFileAsWorkflowFile(Long workflowId, String componentId,
            String fileTitle, String fileDesc, WorkflowFileItem existingFileItem, String datashopBaseDir,
                UserItem userItem) {

        Boolean successFlag = false;
        WorkflowFileItem dsFileItem = null;
        if (existingFileItem != null) {

            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            WorkflowItem workflowItem = workflowDao.get(workflowId);
            DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
            DatasetItem datasetItem = null;

            String subPath = WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/";

            String fullPath = WorkflowFileUtils.sanitizePath(existingFileItem.getFullPathName(datashopBaseDir))
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
                    String wholePath = WorkflowFileUtils.sanitizePath(datashopBaseDir + "/" + subPath);
                    newDirectory = new File(wholePath);
                    if (newDirectory.isDirectory() || newDirectory.mkdirs()) {
                        FileUtils.makeWorldReadable(newDirectory);
                    } else {
                        logger.error("saveWorkflowFileAsWorkflowFile: Creating directory failed " + newDirectory);
                    }
                } else {
                    logger.error("saveWorkflowFileAsWorkflowFile: The fileName cannot be null or empty.");
                }

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
                            Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                                workflowItem, userItem, datashopBaseDir,
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



    public void saveComponentXmlFiles(WorkflowItem existingWorkflowItem,
            WorkflowItem workflowItem, String baseDir) {

        baseDir = WorkflowFileUtils.getStrictDirFormat(baseDir);
        File componentsDir = new File(baseDir + "/"
            + WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + WorkflowHelper.COMPONENTS_XML_DIRECTORY_NAME);
        File[] files = componentsDir.listFiles();
        for (int idx = 0; files != null && idx < files.length; idx++) {
            File fileOrDir = files[idx];
            String nonpersistentXmlFilePath = WorkflowFileUtils.sanitizePath(fileOrDir.getAbsolutePath());
            if (fileOrDir.isFile()) {
                if (nonpersistentXmlFilePath.endsWith(".xml")) {
                    String componentXmlFilePath = nonpersistentXmlFilePath + ".bak";
                    File persistentFile = new File(componentXmlFilePath);
                    File nonpersistentXmlFile = new File(nonpersistentXmlFilePath);
                    if (nonpersistentXmlFile != null && nonpersistentXmlFile.exists()) {
                        try {
                            if (existingWorkflowItem != null
                                && existingWorkflowItem.getOwner() != null
                                    && workflowItem.getOwner() != null
                                        && !existingWorkflowItem.getOwner().getId().equals(
                                            workflowItem.getOwner().getId())) {
                                FileReader fReader = new FileReader(nonpersistentXmlFilePath);
                                BufferedReader bReader = new BufferedReader(fReader);
                                StringBuffer unmodifiedResults = new StringBuffer();
                                String line = null;
                                while ((line = bReader.readLine()) != null) {
                                    unmodifiedResults.append(line);
                                }
                                String modifiedResults = WorkflowAccessHelper.pruneWorkflowXml(workflowItem, workflowItem.getOwner(), baseDir);
                            } else {
                                // We own both workflows or we're creating a new one
                                FileUtils.copyFile(nonpersistentXmlFile, persistentFile);
                            }
                        } catch (IOException ioe) {
                            staticLogger.error("Could not create persistent component XML file.");
                        }
                    }
                }
            }
        } // end for loop
    }

    /**
     * Save the current workflow to persistent storage.
     * @param workflowItem the workflow item
     * @param dataFilesDirectory the data files directory (usually, /datashop/dataset_files)
     */
    public void saveComponentInstances(WorkflowItem workflowItem, String dataFilesDirectory) {
        if (workflowItem != null) {
            WorkflowDao workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
            // First, save the workflow item to the workflow_persistence table
            WorkflowPersistenceDao wpDao = DaoFactory.DEFAULT.getWorkflowPersistenceDao();
            WorkflowPersistenceItem wpItem = wpDao.findByWorkflow(workflowItem);
            Date now = new Date();
            workflowItem.setLastUpdated(now);
            workflowDao.saveOrUpdate(workflowItem);

            if (wpItem == null) {
                // If a persistence item was not found, create one.
                wpItem = new WorkflowPersistenceItem();
            } else {
                Hibernate.initialize(wpItem);
            }
            wpItem.setWorkflow(workflowItem);
            String tempWorkflowXml = workflowItem.getWorkflowXml();
            if (tempWorkflowXml != null) {
                wpItem.setWorkflowXml(tempWorkflowXml);
                wpItem.setLastUpdated(now);
                wpDao.saveOrUpdate(wpItem);

                // Next, save the component instances.
                WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
                List<WorkflowComponentInstanceItem> componentInstances = wciDao.findByWorkflow(workflowItem);
                WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                        .getWorkflowComponentInstancePersistenceDao();
                List<WorkflowComponentInstancePersistenceItem> wcipItems = wciPersistDao
                        .findByWorkflow(workflowItem);
                // Create the new component instance persistence objects
                if (componentInstances != null) {

                    if (wcipItems != null) {
                        for (WorkflowComponentInstancePersistenceItem wcipItem : wcipItems) {
                            wciPersistDao.delete(wcipItem);
                        }
                    }


                    for (Iterator<WorkflowComponentInstanceItem> wciiIter = componentInstances.iterator(); wciiIter
                            .hasNext();) {
                        Integer wciiId = (Integer) ((WorkflowComponentInstanceItem) wciiIter.next()).getId();
                        WorkflowComponentInstanceItem wcii = wciDao.get(wciiId);

                        WorkflowComponentInstancePersistenceItem wcipItem = new WorkflowComponentInstancePersistenceItem();
                        wcipItem.setComponentName(wcii.getComponentName());
                        wcipItem.setWorkflow(workflowItem);
                        wcipItem.setDepthLevel(wcii.getDepthLevel());
                        wcipItem.setDirtyAddConnection(wcii.getDirtyAddConnection());
                        wcipItem.setDirtyAncestor(wcii.getDirtyAncestor());
                        wcipItem.setDirtyDeleteConnection(wcii.getDirtyDeleteConnection());
                        wcipItem.setDirtyFile(wcii.getDirtyFile());
                        wcipItem.setDirtyOption(wcii.getDirtyOption());
                        wcipItem.setDirtySelection(wcii.getDirtySelection());
                        wcipItem.setState(wcii.getState());
                        wcipItem.setErrors(wcii.getErrors());
                        wcipItem.setWarnings(wcii.getWarnings());
                        wciPersistDao.saveOrUpdate(wcipItem);

                    }
                } else {
                    if (wcipItems != null) {
                        for (WorkflowComponentInstancePersistenceItem wcipItem : wcipItems) {
                            wciPersistDao.delete(wcipItem);
                        }
                    }
                }

                // Persist workflow imports.
                ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
                ComponentFilePersistenceDao compFilePersistenceDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
                // Remove component file persistence items before "persisting" the latest component file items

                compFilePersistenceDao.deleteByWorkflow(workflowItem);

                List<ComponentFileItem> cfFileItems = compFileDao.findByWorkflow(workflowItem);
                if (cfFileItems != null && !cfFileItems.isEmpty()) {
                    for (ComponentFileItem cfItem : cfFileItems) {
                        Hibernate.initialize(cfItem);
                        // Get CFP items for this component
                        ComponentFilePersistenceItem newCfpi = new ComponentFilePersistenceItem(
                            cfItem.getWorkflow(), cfItem.getComponentId(), cfItem.getDataset(), cfItem.getFile());
                                compFilePersistenceDao.saveOrUpdate(newCfpi);
                    }
                }
            }
        }
    }

    public void logWorkflowUserAction(UserItem userItem,
        WorkflowItem workflowItem, DatasetItem datasetItem,
            String action, String info, WorkflowItem newWorkflowItem) {
        if (action != null && !action.isEmpty()) {
            String userId = userItem != null ? (String) userItem.getId() : null;
            Long workflowId = workflowItem != null ? (Long) workflowItem.getId() : null;
            Integer datasetId = datasetItem != null ? (Integer) datasetItem.getId() : null;
            Long newWorkflowId = newWorkflowItem != null ? (Long) newWorkflowItem.getId() : null;

            WorkflowUserLogItem wfUserLogItem = new WorkflowUserLogItem(userId,
                    workflowId, datasetId,
                    action, info, newWorkflowId);
            WorkflowUserLogDao wfUserLogDao = DaoFactory.DEFAULT.getWorkflowUserLogDao();
            wfUserLogDao.saveOrUpdate(wfUserLogItem);
        }
    }

    public void logWorkflowComponentUserAction(UserItem userItem,
        WorkflowItem workflowItem, DatasetItem datasetItem,
        String componentId, String componentName, String componentType, String componentIdHumanReadable,
        Integer outputNode, WorkflowFileItem workflowFile, FileItem dsFile,
            String action, String info) {
        if (action != null && !action.isEmpty()) {
            String userId = userItem != null ? (String) userItem.getId() : null;
            Long workflowId = workflowItem != null ? (Long) workflowItem.getId() : null;
            Integer datasetId = datasetItem != null ? (Integer) datasetItem.getId() : null;
            Integer workflowFileId = workflowFile != null ? (Integer) workflowFile.getId() : null;
            Integer dsFileId = dsFile != null ? (Integer) dsFile.getId() : null;

            WorkflowComponentUserLogItem wfUserLogItem = new WorkflowComponentUserLogItem(
                userId, workflowId, datasetId,
                    componentId, componentName, componentType, componentIdHumanReadable,
                        outputNode, workflowFileId, dsFileId,
                            action, info);
            WorkflowUserLogDao wfUserLogDao = DaoFactory.DEFAULT.getWorkflowUserLogDao();
            wfUserLogDao.saveOrUpdate(wfUserLogItem);
        }
    }


    /**
     * Get the JSON error message map.
     * @param workflowItem the workflow item
     * @return the JSON error message map
     * @throws JSONException JSON Exception
     */
    public JSONObject getLastErrorMessage(WorkflowItem workflowItem) throws JSONException {
        JSONObject errorMessageJson = null;

        if (workflowItem != null) {
            WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                    .getWorkflowComponentInstancePersistenceDao();
            List<WorkflowComponentInstancePersistenceItem> wcipItems = wciPersistDao.findByWorkflow(workflowItem);

            // Create the new component instance persistence objects
            if (wcipItems != null) {
                for (Iterator<WorkflowComponentInstancePersistenceItem> wciPersistIter = wcipItems.iterator(); wciPersistIter
                        .hasNext();) {
                    Integer wcipId = (Integer) ((WorkflowComponentInstancePersistenceItem) wciPersistIter.next())
                            .getId();
                    WorkflowComponentInstancePersistenceItem wcipItem = wciPersistDao.get(wcipId);
                    if (wcipItem.getErrors() != null) {
                        errorMessageJson = new JSONObject(wcipItem.getErrors());
                        break;
                    }

                }
            }
        }
        return errorMessageJson;
    }

    public static <K extends Comparable, V extends Comparable> LinkedHashMap<K, V> getSortedMapByValues(final Map<K, V> map){

         LinkedHashMap<K, V> mapSortedByValues = new LinkedHashMap<K, V>();

         //get all the entries from the original map and put it in a List
         List<Map.Entry<K, V>> list = new ArrayList<Entry<K, V>>(map.entrySet());

         //sort the entries based on the value by custom Comparator
         Collections.sort(list, new Comparator<Map.Entry<K, V>>(){

             public int compare(Entry<K, V> entry1, Entry<K, V> entry2) {
                 return entry1.getValue().compareTo( entry2.getValue() );
             }

         });

         //put all sorted entries in LinkedHashMap
         for( Map.Entry<K, V> entry : list  )
             mapSortedByValues.put(entry.getKey(), entry.getValue());

         //return Map sorted by values
         return mapSortedByValues;
     }

    public static String getWorkflowDatasets(WorkflowItem workflowItem) {

        WorkflowDatasetMapDao mapDao = DaoFactory.DEFAULT.getWorkflowDatasetMapDao();
        List<WorkflowDatasetMapItem> mapItems = mapDao.findByWorkflow(workflowItem);

        if (mapItems.size() == 0) { return "<datasets />"; }

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();

        StringBuffer sb = new StringBuffer("<datasets>");

        for (WorkflowDatasetMapItem item : mapItems) {
            DatasetItem ds = dsDao.get((Integer)item.getDataset().getId());
            sb.append("<dataset>");
            sb.append("<id>").append((Integer)ds.getId()).append("</id>");
            sb.append("<name>").append(WorkflowFileUtils.htmlEncode(ds.getDatasetName())).append("</name>");
            sb.append("</dataset>");
        }
        sb.append("</datasets>");

        return sb.toString();
    }

    public static String getWorkflowComponentsDir() {
        String workflowComponentsDir = null;

        DataShopInstanceDao dsInstanceDao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        DataShopInstanceItem dsInstanceItem = dsInstanceDao.get(DATASHOP_INSTANCE_ID);
        if (dsInstanceItem != null && dsInstanceItem.getWfcDir() != null
                && !dsInstanceItem.getWfcDir().isEmpty()) {
            File testWfcDir = new File(dsInstanceItem.getWfcDir());
            if (testWfcDir.exists() && testWfcDir.isDirectory() && testWfcDir.canWrite()) {
                workflowComponentsDir = dsInstanceItem.getWfcDir();
            }
        }

        return workflowComponentsDir;
    }


}


