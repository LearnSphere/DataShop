package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.WfcRecentlyUsedDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.AbstractServlet;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.AbstractComponent;
import edu.cmu.pslc.datashop.workflows.ComponentFileItem;
import edu.cmu.pslc.datashop.workflows.ComponentFilePersistenceItem;
import edu.cmu.pslc.datashop.workflows.WfcRecentlyUsedItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;

public class ComponentHelper {

    /** Workflow Search variable. */
    public static JSONArray componentList = null;
    /** Workflow Search variable. */
    public static JSONArray authorList = null;
    /** Workflow Search variable. */
    public static JSONArray workflowTagsList = null;

    /** Used in several LearnSphereServlet processes. */
    public static String componentTypeHierarchy = null;

    /** Option types list. */
    public static JSONObject finalOptionTypes = null;

    /** Component menu identifier. */
    public static final String XML_COMPONENT_TYPE_HIERARCHY_ELEMENTS = "/component_menu";

    public static final String XML_OPTION_FILE_NODES = "//options/files";
    public static final String XML_INSTANCE_OPTION_NODE = "//options";

    public static final String XML_OPTIONS_CHILD_NODES = "//xs:complexType[@name='OptionsType']/xs:choice/*";

    public static final String XML_OPTION_FILE_LIST_DEF = "OptionFileList";
    public static final String XML_OPTION_FILE_LIST_NODES = "//xs:complexType[@name='OptionFileList']/xs:choice/xs:element";

    public static final String XML_FILE_CONTAINER_DEF = "//xs:complexType[@name='FileContainer']/xs:all/xs:element";

    public static final String XML_DEPENDENT_OPTION_NODES = "//xs:complexType[contains(@name,'option_dependency')]/xs:choice/xs:element";

    public static final String XML_SIMPLE_TYPE_ENUMERATED_PREFIX = "//xs:simpleType[@name='";
    public static final String XML_SIMPLE_TYPE_ENUMERATED_SUFFIX = "']/xs:restriction/xs:enumeration";

    public static final String XML_SIMPLE_TYPE_BOOLEAN = "xs:boolean";

    /** Dirty bits are used in workflow and workflow persistence logic. */
    /** Component Instance dirty bit. */
    public static final String DIRTY_FILE = "DIRTY_FILE";
    /** Component Instance dirty bit. */
    public static final String DIRTY_OPTION = "DIRTY_OPTION";
    /** Component Instance dirty bit. */
    public static final String DIRTY_SELECTION = "DIRTY_SELECTION";
    /** Component Instance dirty bit. */
    public static final String DIRTY_ADD_CONNECTION = "DIRTY_ADD_CONNECTION";
    /** Component Instance dirty bit. */
    public static final String DIRTY_DELETE_CONNECTION = "DIRTY_DELETE_CONNECTION";
    /** Component Instance dirty bit. */
    public static final String DIRTY_ANCESTOR = "DIRTY_ANCESTOR";


    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Debug logging. */
    private static Logger staticLogger = Logger.getLogger(WorkflowFileUtils.class.getName());

    /** The name of the component info file which describes inputs, outputs, and options in plain English. */
    private static final String COMPONENT_INFO_FILE_NAME = "info.xml";

    public static JSONObject componentInfoDivs = null;

    public JSONArray getComponentList() {
        return ComponentHelper.componentList;
    }

    /**
      * Get the component hierarchy XML.
      * @param componentTypeHierarchyFile the location of the hierarchy file
      * @return the hierarchy XM as a Javascript object
      * @throws IOException
      */
     public static String getComponentTypeHierarchy(String componentTypeHierarchyFile)
             throws IOException {

         if (componentTypeHierarchy == null) {

             WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
             List<WorkflowComponentItem> wfcItems = wfcDao.findByEnabled(true);
             List<String> enabledComponentList = new ArrayList<String>();
             if (wfcItems != null) {
                 // Store the enabled component names in a List<String> for faster
                 // instantiation in the recursive method, getOrderedComponentList
                 for (WorkflowComponentItem wfcItem : wfcItems) {
                     Hibernate.initialize(wfcItem.getComponentName());
                     // Database component_names only contain a-zA-Z0-9 and _
                     // and case does not matter.
                     if (wfcItem.getEnabled() != null && wfcItem.getEnabled()) {
                         enabledComponentList.add(wfcItem.getComponentName()
                             .toLowerCase().trim());
                     }
                 }
             }

             List<Element> componentTypeHierarchyElements = null;

             File hierarchyFile = new File(componentTypeHierarchyFile);
             if (hierarchyFile.exists() && hierarchyFile.canRead()) {
                 componentTypeHierarchyElements = WorkflowXmlUtils.getNodeList(componentTypeHierarchyFile,
                     XML_COMPONENT_TYPE_HIERARCHY_ELEMENTS);
             }


             for (int j = 0; j < componentTypeHierarchyElements.size(); j++) {

                 Element ttElement = componentTypeHierarchyElements.get(j);

                 if (ttElement.getName().equalsIgnoreCase("component_menu")) {
                     Integer counterInit = 0;
                     componentTypeHierarchy = "{ "
                         + ComponentHelper.getOrderedComponentList(ttElement, new StringBuffer(),
                                 counterInit, enabledComponentList)
                             + " } ";
                 }
                 break;
             }
         }
         return componentTypeHierarchy;
     }

    /**
     * Returns the info.xml elements as an HTML table.
     *
     * @param componentName the component name
     * @return
     */
    public static String getComponentInfoDiv(WorkflowComponentItem workflowComponentItem) {

        StringBuffer infoDiv = new StringBuffer();
        if (workflowComponentItem != null) {
            StringBuffer infoFileText = new StringBuffer();
            // Get info div
            String infoFilePath = workflowComponentItem.getToolDir() + COMPONENT_INFO_FILE_NAME;
            File infoFile = new File(infoFilePath);
            if (infoFile.exists() && infoFile.isFile() && infoFile.canRead()) {

                BufferedReader bReader = null;
                FileReader fReader = null;
                try {
                    fReader = new FileReader(infoFile);
                    bReader = new BufferedReader(fReader);
                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        infoFileText.append(line);
                    }
                } catch (IOException e) {
                    staticLogger.error(e.toString());
                } finally {
                    if (bReader != null) {
                        try {
                            bReader.close();
                        } catch (IOException e) {
                            staticLogger.error("Could not close buffered reader on file " + infoFilePath);
                        }
                    }
                }

                // Is the info file populated with XML data about the component?
                if (!infoFileText.toString().trim().isEmpty()) {
                    Element infoFileXml = null;
                    try {
                        infoFileXml = WorkflowXmlUtils.getStringAsElement(infoFileText.toString());
                    } catch (JDOMException e) {
                        staticLogger.error("Info file is not valid XML.");
                    } catch (IOException e) {
                        staticLogger.error("Info file could not be read.");
                    }

                    // Get children of the root element
                    if (infoFileXml != null && infoFileXml.getChildren() != null) {
                        if (infoFileXml.getChild("author") != null) {
                            infoDiv.insert(0, WorkflowXmlUtils.elementAsComplexString("Author",
                                    infoFileXml.getChild("author")));
                        }
                        if (infoFileXml.getChild("abstract") != null) {
                            infoDiv.append(WorkflowXmlUtils.elementAsComplexString("Abstract",
                                    infoFileXml.getChild("abstract")));
                        }
                        if (infoFileXml.getChild("details") != null) {
                            infoDiv.append(WorkflowXmlUtils.elementAsComplexString("Details",
                                    infoFileXml.getChild("details")));
                        }
                        if (infoFileXml.getChild("inputs") != null) {
                            infoDiv.append(WorkflowXmlUtils.elementAsComplexString("Inputs",
                                    infoFileXml.getChild("inputs")));
                        }
                        if (infoFileXml.getChild("outputs") != null) {
                            infoDiv.append(WorkflowXmlUtils.elementAsComplexString("Outputs",
                                    infoFileXml.getChild("outputs")));
                        }
                        if (infoFileXml.getChild("options") != null) {
                            infoDiv.append(WorkflowXmlUtils.elementAsComplexString("Options",
                                    infoFileXml.getChild("options")));
                        }
                    }
                } else {
                    staticLogger.trace("Cannot read info file: " + infoFilePath);
                }
            }
        }
        return infoDiv.toString();
    }

    public static JSONObject getComponentInfoDivs() {
        JSONObject componentInfoJson = new JSONObject();
        WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
        List<WorkflowComponentItem> workflowComponentItems = workflowComponentDao.findByEnabled(true);
        for (WorkflowComponentItem wfcItem : workflowComponentItems) {
            String infoDiv = getComponentInfoDiv(wfcItem);
            try {
                componentInfoJson.put(wfcItem.getComponentName().toLowerCase(), infoDiv);
            } catch (JSONException e) {
                staticLogger.error("Failed to retrieve info for component " + wfcItem.getComponentName());
            }
        }
        return componentInfoJson;
    }

    /**
     * Convenience method for converting a JDOM element to a string.
     * @param element the JDOM element
     * @return the string representation or null if an error occurred
  * @throws IOException
     */
    public static String getOrderedComponentList(Element element, StringBuffer sBuffer,
            Integer levelCount, List<String> enabledComponentList)
        throws IOException {

        if (element != null && element.getChildren() == null || element.getChildren().isEmpty()) {
            // Only show entries which are present and enabled in the table, workflow_component

            if (levelCount > 0) {
                sBuffer.append(", \"" + levelCount + "\" : \"" + element.getName() + "\" ");
            } else {
                sBuffer.append(" \"" + levelCount + "\" : \"" + element.getName() + "\" ");
            }

        } else {
            if (levelCount > 0) {
                sBuffer.append(", \"" + levelCount + "\" : { \"" + element.getName() + "\" : ");
            } else {
                sBuffer.append(" \"" + levelCount + "\" : { \"" + element.getName() + "\" : ");
            }

            levelCount = 0;
            sBuffer.append(" { ");
             for (Element child : (List<Element>) element.getChildren()) {
                 if ((child.getChildren() == null || child.getChildren().isEmpty())
                     && enabledComponentList != null
                         && (enabledComponentList.contains(child.getName()
                             .toLowerCase().trim().replaceAll("[^a-zA-Z0-9]", "_"))
                             || child.getName().equalsIgnoreCase("Recently_Used"))) {
                     sBuffer.append(getOrderedComponentList(child, new StringBuffer(), levelCount,
                             enabledComponentList));
                     levelCount++;
                     if (!child.getName().equalsIgnoreCase("Recently_Used")) {
                         componentList.put(child.getName());
                     }
                 } else if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                     sBuffer.append(getOrderedComponentList(child, new StringBuffer(), levelCount,
                             enabledComponentList));
                     levelCount++;
                 }
             }
            sBuffer.append(" } ");
            sBuffer.append(" } ");
        }

        return sBuffer.toString();
    }

    public static String getRecentlyUsedComponents(String userId)
             throws IOException, JSONException {

         WorkflowComponentDao wfcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
         List<WorkflowComponentItem> wfcItems = wfcDao.findByEnabled(true);
         List<String> enabledComponentList = new ArrayList<String>();
         if (wfcItems != null) {
             // Store the enabled component names in a List<String> for faster
             // instantiation in the recursive method, getOrderedComponentList
             for (WorkflowComponentItem wfcItem : wfcItems) {
                 Hibernate.initialize(wfcItem.getComponentName());
                 // Database component_names only contain a-zA-Z0-9 and _
                 // and case does not matter.
                 enabledComponentList.add(wfcItem.getComponentName()
                     .toLowerCase().trim());
             }
         }

         JSONObject recentComponentsJson = new JSONObject();
         WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
         List<String> workflowComponentTypes = workflowComponentDao.findDistinctComponentTypes(true);
         List<WfcRecentlyUsedItem> wfcRecentlyUsedItems = null;
         if (workflowComponentTypes != null && !workflowComponentTypes.isEmpty()) {

             for (String componentType : workflowComponentTypes) {

                 WfcRecentlyUsedDao wfcRecentlyUsedDao = DaoFactory.DEFAULT.getWfcRecentlyUsedDao();
                 wfcRecentlyUsedItems =
                     wfcRecentlyUsedDao.findByComponentTypeAndUserId(componentType, userId);
                 JSONArray recentComponentsJsonArray = new JSONArray();
                 if (wfcRecentlyUsedItems != null && !wfcRecentlyUsedItems.isEmpty()) {
                     for (WfcRecentlyUsedItem wfcRecentlyUsedItem : wfcRecentlyUsedItems) {
                         String componentName = wfcRecentlyUsedItem.getComponentName();
                         if (enabledComponentList.contains(componentName.toLowerCase())) {
                             recentComponentsJsonArray.put(componentName);
                         }
                     }
                     recentComponentsJson.put(componentType, recentComponentsJsonArray);
                 }
             }
         }

         return recentComponentsJson.toString();
     }

   /**
      * This method removes all components that are no longer in the workflow adjacency map.
      * @param workflowItem
      * @param digraphDoc
      * @param baseDir
      */
     public static void removeDeletedComponents(WorkflowItem workflowItem, Element digraphDoc, String baseDir) {
         baseDir = WorkflowFileUtils.getStrictDirFormat(baseDir);
         // Remove files and directories associated with deleted components
         WorkflowComponentAdjacencyDao wcaDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
         WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
         List<WorkflowComponentAdjacencyItem> wcaItems = wcaDao.findByWorkflow(workflowItem);
         Boolean workflowIsEmpty = false;
         if (workflowItem != null && workflowItem.getId() != null) {
             if (digraphDoc.getChild("components") != null) {
                 List<String> existingComponents = new ArrayList<String>();
                 if (wcaItems != null) {
                     for (WorkflowComponentAdjacencyItem wcaItem : wcaItems) {

                         Boolean componentExists = false;
                         String oldComponentId = wcaItem.getComponentId();

                         for (Element component : (List<Element>) digraphDoc.getChild("components").getChildren()) {

                             String newComponentId = component.getChildText("component_id");

                             if (component.getChild("component_id") != null) {
                                 if (oldComponentId != null && oldComponentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)
                                         && oldComponentId.equalsIgnoreCase(newComponentId)) {
                                     componentExists = true;
                                     break;
                                 }
                             }
                         }

                         // Component is no longer in the XML instance being saved
                         if (!componentExists) {
                             deleteComponent(workflowItem, oldComponentId, baseDir);
                         } else {
                             existingComponents.add(oldComponentId);
                         }
                     }
                 }

                 List<WorkflowComponentInstanceItem> componentInstances = wciDao.findByWorkflow(workflowItem);
                 // Remove components no longer in the workflow.
                 if (componentInstances != null) {
                     for (Iterator<WorkflowComponentInstanceItem> wciiIter = componentInstances.iterator(); wciiIter
                             .hasNext();) {
                         Integer wciiId = (Integer) ((WorkflowComponentInstanceItem) wciiIter.next()).getId();
                         WorkflowComponentInstanceItem wcii = wciDao.get(wciiId);

                         if (!existingComponents.contains(wcii.getComponentName())) {
                             wciDao.delete(wcii);
                         }
                     }
                 }

                 File workflowDir = new File(WorkflowFileUtils.sanitizePath(baseDir + "/")
                         + WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getWorkflowsDir(workflowItem.getId())));
                 if (workflowDir.isDirectory()) {
                     List<String> fileList = Arrays.asList(workflowDir.list());
                     for (String fileName : fileList) {
                         String filePath = workflowDir.getAbsolutePath() + "/" + fileName;
                         File file = new File(filePath);
                         // Delete component directories that were unaccounted for in the XML.
                         if (file.isDirectory() && fileName.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)
                                 && !existingComponents.contains(fileName)
                                 && /* to be paranoid in case someone changes component_id patterns*/
                                    !fileName.equalsIgnoreCase("components")) {
                             deleteComponent(workflowItem, fileName, baseDir);
                         }
                     }
                 }

             } else {
                 workflowIsEmpty = true;
             }

             if (workflowIsEmpty) {
                 if (wcaItems != null) {
                     for (WorkflowComponentAdjacencyItem wcaItem : wcaItems) {
                         deleteComponent(workflowItem, wcaItem.getComponentId(), baseDir);
                     }
                 }

                 File workflowDir = new File(WorkflowFileUtils.sanitizePath(baseDir + "/")
                         + WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getWorkflowsDir(workflowItem.getId())));
                 if (workflowDir.isDirectory()) {
                     List<String> fileList = Arrays.asList(workflowDir.list());
                     for (String fileName : fileList) {
                         String filePath = workflowDir.getAbsolutePath() + "/" + fileName;
                         File file = new File(filePath);
                         // Delete component directories that were unaccounted for in the XML.
                         if (file.isDirectory() && fileName.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)) {
                             deleteComponent(workflowItem, fileName, baseDir);
                         }
                     }
                 }
             }
         }
     }

     /**
      * Delete a component, it's XML, and any component / workflow files associated with this component.
      * @param workflowItem
      * @param oldComponentId
      * @param baseDir
      */
     public static void deleteComponent(WorkflowItem workflowItem, String oldComponentId, String baseDir) {
         deleteComponentOutput(workflowItem, oldComponentId, baseDir);
         // Delete component's directory
         String delDirectoryPath = WorkflowFileUtils.sanitizePath(baseDir + "/")
             + WorkflowFileUtils.sanitizePath(
                 WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + oldComponentId);
         // Delete component's XML instance
         String delXmlPath = WorkflowFileUtils.sanitizePath(baseDir + "/")
                 + WorkflowFileUtils.sanitizePath(
                     WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + "components/" + oldComponentId + ".xml");

         try {
             File delDirectory = new File(delDirectoryPath);
             if (delDirectory.exists() && delDirectory.isDirectory()) {
                 FileUtils.deleteDirectoryRecursively(delDirectoryPath);
             } else {
                 staticLogger.error("Component directory queued for deletion was not found: " + delDirectoryPath);
             }

             File delXml = new File(delXmlPath);
             if (delXml.exists() && delXml.isFile()) {
                 FileUtils.deleteFile(delXml);
             }
             File delXmlPersistent = new File(delXmlPath + ".bak");
             if (delXmlPersistent.exists() && delXmlPersistent.isFile()) {
                 FileUtils.deleteFile(delXmlPersistent);
             }
         } catch (IOException e) {
             staticLogger.error("Could not remove files or directory for component: " + oldComponentId);
         }
     }


     /*
      * Get input for a component.  Used in custom options interfaces
      * @param workflowDir
      * @param componentId
      * @param nodeIndex node index to find input
      * @param fileIndex file index at input node
      * @param numLinesToGet the number of lines from input file to get (-1 is entire file);
      * @return String of the input at the specified node
     */
     public static String getComponentInputString(String workflowDir, String componentId,
             int nodeIndex, int fileIndex, int numLinesToGet)
     {

         StringBuilder inputStrBuf = new StringBuilder();
         if (componentId != null && !componentId.matches(AbstractServlet.STRICT_CHARS)) { componentId = ""; }

         String componentXmlPath = WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getStrictDirFormat(workflowDir)
              + "/components/" + componentId + ".xml");

         File testFile = new File(componentXmlPath);

         List<Element> componentInputNodes = null;
         if (testFile.exists() && testFile.isFile()) {
             componentInputNodes = WorkflowXmlUtils.getNodeList(componentXmlPath,
                     ConnectionHelper.XML_INPUT_FILE_NODES);
         } else {
             staticLogger.debug("testFile does not exist getComponentInputString WorkflowHelper");
         }
         try {
             if (componentInputNodes != null) {
                 if (componentInputNodes.size() >= nodeIndex) {
                     Element inputNode = componentInputNodes.get(nodeIndex);
                     Element inFileNode = inputNode.getChild("files");
                     Integer inFileIndex = new Integer(fileIndex);

                     staticLogger.debug("Getting input String for " + componentId + ". node index: " + nodeIndex);

                     if (inFileNode != null && !inFileNode.getChildren().isEmpty()) {
                         List<Element> files = inFileNode.getChildren();
                         if (files != null) {
                             Element file = files.get(inFileIndex);
                             if (file != null) {
                                 staticLogger.debug("Reading inFileNode "  + nodeIndex + ".");

                                 //replaceExistingFilesWithIds(baseDir, (Long) workflowItem.getId(),
                                 //            componentId, componentSubdir, file);


                                 Element file_path = file.getChild("file_path");
                                 String file_path_str = file.getChildText("file_path");

                                 try {
                                     BufferedReader inputFileReader =
                                         new BufferedReader(new FileReader(file_path_str));

                                     int numLines = 0;
                                     while (inputFileReader.ready() &&
                                             ((numLines < numLinesToGet) || numLinesToGet == -1)) {
                                         inputStrBuf.append(inputFileReader.readLine() + "\n");
                                         numLines++;
                                     }

                                 } catch (IOException e) {
                                     staticLogger.error("Could not read input in getComponentInputString" + e.toString());
                                 }
                             }
                         }
                     }
                 } // end of inner for loop
             } // end of outer for loop (matching input nodes)
         } catch (Exception e) {
           staticLogger.error("failed to get component input: " + e.toString());
           return null;
         }
         return inputStrBuf.toString();
     }

    /**
      * Gets the advanced options for a component.
      * @param componentId the component id
      * @param componentTYpe the component type
      * @param componentName the component name
      * @param dirtySelection
      * @param string
      * @return the XML containing the options and their values
      */
     public static Element createComponentOptions(Long workflowId, Element existingComponentElement,
             String workflowDir, String tableTypesFile, UserItem userItem,
             String componentId, String componentName, Boolean dirtySelection, Boolean dirtyAncestor,
             Boolean isCompleted) {

         staticLogger.info("Initializing component options.");

         if (componentName != null) {
             componentName = componentName.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
         }

         // Find the workflow and component information in the database.
         WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
         staticLogger.info("Fetching component information for " + componentName);
         WorkflowComponentItem workflowComponentItem =
             workflowComponentDao.findByName(componentName);

         Element optionsElement = new Element("options");

         if (workflowComponentItem != null) {
             staticLogger.info("Preparing to read component XML file and schema.");

             String componentXmlPath = WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getStrictDirFormat(workflowDir)
                  + "/components/" + componentId + ".xml");

             File testFile = new File(componentXmlPath);

             List<Element> componentInputNodes = null;
             if (existingComponentElement != null && existingComponentElement.getChild("inputs") != null
                     && existingComponentElement.getChild("inputs").getChildren() != null) {

                 componentInputNodes = WorkflowXmlUtils.getNodeList(existingComponentElement,
                         ConnectionHelper.XML_INPUT_FILE_NODES);

             } else if (testFile.exists() && testFile.isFile()) {
                 componentInputNodes = WorkflowXmlUtils.getNodeList(componentXmlPath,
                         ConnectionHelper.XML_INPUT_FILE_NODES);
             } else {
                 return new Element("options");
             }


             List<Element> optionNodes = null;
             if (existingComponentElement != null && existingComponentElement.getChild("options") != null
                     && existingComponentElement.getChildren() != null
                     && !existingComponentElement.getChild("options").getChildren().isEmpty()) {
                 optionNodes = WorkflowXmlUtils.getNodeList(existingComponentElement, XML_INSTANCE_OPTION_NODE);

             } else if (testFile.exists() && testFile.isFile()) {
                 optionNodes = WorkflowXmlUtils.getNodeList(componentXmlPath, XML_INSTANCE_OPTION_NODE);
             }

             // Get all children of any options element
             List<Element> schemaOptionDefinitions =
                     WorkflowXmlUtils.getNodeList(workflowComponentItem.getSchemaPath(), XML_OPTIONS_CHILD_NODES);

             if (schemaOptionDefinitions != null) {
                 staticLogger.trace("Component options: " + schemaOptionDefinitions.size());
                 for (int i = 0; i < schemaOptionDefinitions.size(); i++) {

                     Element optionElem = schemaOptionDefinitions.get(i);

                     if (optionElem != null
                             && optionElem.getAttribute("type") != null
                                 && optionElem.getAttribute("name") != null) {

                         Integer inputNodeIndex = null;
                         Integer inputFileIndex = null;
                         if (optionElem.getAttribute("inputNodeIndex", WorkflowHelper.lsXmlNs) != null) {
                             String inputNodeIndexStr = optionElem.getAttributeValue("inputNodeIndex", WorkflowHelper.lsXmlNs);
                             if (inputNodeIndexStr != null ) {
                                 if (inputNodeIndexStr.matches("[0-9]+")) {
                                     inputNodeIndex = Integer.parseInt(inputNodeIndexStr);
                                 }
                             }
                         }

                         if (optionElem.getAttribute("inputFileIndex", WorkflowHelper.lsXmlNs) != null) {
                             String inputFileIndexStr = optionElem.getAttributeValue("inputFileIndex", WorkflowHelper.lsXmlNs);
                             if (inputFileIndexStr != null ) {
                                 if (inputFileIndexStr.matches("[0-9]+")) {
                                     inputFileIndex = Integer.parseInt(inputFileIndexStr);
                                 }
                             }
                         }

                         // Get the name, type, and default value from the XSD
                         String name = optionElem.getAttributeValue("name");
                         String type = optionElem.getAttributeValue("type");
                         String defaultValue = optionElem.getAttributeValue("default");
                         // Get the optionmeta values from the inputs
                         Map<String, String> optionmetaValues = new HashMap<String, String>();
                         List<String> optionmetaValuesKeys = new ArrayList<String>();
                         // Get the actual values from the component XML
                         Map<String, String> actualValues = new HashMap<String, String>();
                         List<String> actualValuesKeys = new ArrayList<String>();



                         if (optionNodes != null && optionNodes.size() > 0 && optionNodes.get(0).getChildren() != null) {
                             for (Element option : (List<Element>) optionNodes.get(0).getChildren()) {
                                 if (option.getName().equalsIgnoreCase(name)) {
                                     actualValues.put(WorkflowFileUtils.htmlDecode(option.getTextTrim()).toLowerCase(),
                                             option.getTextTrim());
                                     actualValuesKeys.add(WorkflowFileUtils.htmlDecode(option.getTextTrim()).toLowerCase());
                                 }
                             }
                         }

                         /*if (!type.matches("FileInputHeader") && !type.matches("MultiFileInputHeader")
                                 && !type.matches("FixedEffectsPanel") && !type.matches("RandomEffectsPanel")) {*/
                         if (!type.matches("FileInputHeader") && !type.matches("MultiFileInputHeader")) {
                             if (actualValuesKeys != null && !actualValuesKeys.isEmpty()) {
                                 for (String foundValue : actualValuesKeys) {
                                         Element optionElement = new Element(name);
                                         optionElement.setText(actualValues.get(foundValue));
                                         optionsElement.addContent(optionElement);
                                         break; // mck todo: tuples
                                 }
                             } else {
                                 //for FixedEffectsPanel and RandomEffectsPanel, set default to empty
                                 if (type.matches("FixedEffectsPanel") || type.matches("RandomEffectsPanel")) {
                                         defaultValue = "";
                                 }
                                 Element optionElement = new Element(name);
                                 optionElement.setText(defaultValue);
                                 optionsElement.addContent(optionElement);
                             }
                         } else {

                             //  placeholder: String workflowsDir = getWorkflowsDir(workflowId);
                             //  List<ComponentNode> expectedInputs =
                             //        getConnectedInputsInfo(workflowId, workflowsDir, tableTypesFile,
                             //            userItem, componentId, componentName, type);


                             if (componentInputNodes != null) {
                                 staticLogger.trace("Number of component input nodes: " + componentInputNodes.size());
                                 Integer inNodeIndexToMatch = 0;

                                 Map<Integer, Integer> fileIndexByNode = new HashMap<Integer, Integer>();
                                 // FileInputHeader only allows one value to be selected
                                 Boolean foundFileInputHeader = false;
                                 Boolean findFirstHeader = true;

                                 for (int j = 0; j < componentInputNodes.size(); j++) {
                                     Element inputNode = componentInputNodes.get(j);
                                     Element inFileNode = inputNode.getChild("files");

                                     if (inputNode.getName().matches("[a-zA-Z]+[0-9]+")) {
                                         String intStr = inputNode.getName().replaceAll("[a-zA-Z]+", "");
                                         inNodeIndexToMatch = Integer.parseInt(intStr);
                                     }

                                     if (inputNodeIndex == null
                                             || inputNodeIndex == inNodeIndexToMatch) {

                                         // increment the unordered file index
                                         if (fileIndexByNode.containsKey(inNodeIndexToMatch)) {
                                             Integer lastCount = fileIndexByNode.get(inNodeIndexToMatch) + 1;
                                             if (lastCount > 0 && lastCount < Integer.MAX_VALUE) {
                                                 fileIndexByNode.put(inNodeIndexToMatch, lastCount);
                                             }
                                         } else {
                                             fileIndexByNode.put(inNodeIndexToMatch, 0);
                                         }


                                         if (inputFileIndex == null
                                                 || fileIndexByNode.get(inNodeIndexToMatch) == inputFileIndex) {

                                             Element optionMetaElement = inputNode.getChild("optionmeta");

                                             if (optionMetaElement != null && optionMetaElement.getChildren() != null) {
                                                 // create two lists, one for actualValuesKeys and one for actualValues

                                                 // If an option has the same name, like <model>, then we have found a similar one
                                                 // so log it and move on.
                                                 for (Element child : (List<Element>) optionMetaElement.getChildren()) {
                                                     if (child.getName().equalsIgnoreCase(name)) {
                                                         optionmetaValues.put(WorkflowFileUtils.htmlDecode(
                                                             child.getTextTrim()).toLowerCase(), child.getText());
                                                         optionmetaValuesKeys.add(child.getTextTrim().toLowerCase());
                                                     }
                                                 }

                                             }

                                             // Ensure input has a <file> attribute
                                             if (inFileNode != null) {
                                                 List<Element> files = inFileNode.getChildren();
                                                 if (files != null) {
                                                     // if not dirty selection and not completed
                                                     if (!dirtySelection && !isCompleted) {
                                                         for (Element file : files) {
                                                             staticLogger.trace("Processing inFileNode " + inNodeIndexToMatch + ".");
                                                             Element metadata = file.getChild("metadata");
                                                             if (metadata != null && metadata.getChildren() != null) {
                                                                 for (Element child : (List<Element>) metadata.getChildren()) {
                                                                     if (child.getChild("index") != null) {


                                                                         String headerIndex = child.getChildText("index");
                                                                         String headerName = WorkflowFileUtils.htmlDecode(
                                                                                 child.getChildText("name"));
                                                                         if (headerIndex != null && headerIndex.matches("\\d+")) {
                                                                             // defaultValue can be a regular expression defined in the xsd
                                                                             if (headerName != null && headerName.matches(defaultValue)) {

                                                                                 String annotatedColString = "Input " + inNodeIndexToMatch + " ("
                                                                                     + fileIndexByNode.get(inNodeIndexToMatch).toString() + ")" + " - "
                                                                                         + headerName + " (column " + headerIndex + ")";
                                                                                 Boolean isMetaData = child.getName().equalsIgnoreCase(AbstractComponent.META_DATA_LABEL);

                                                                                 // If a labeled header exists, we will use that
                                                                                 if (isMetaData) {

                                                                                     if (type.matches("MultiFileInputHeader")
                                                                                             || type.matches("FixedEffectsPanel")
                                                                                                 || type.matches("RandomEffectsPanel") ) {

                                                                                         Element optionElement = new Element(name);
                                                                                         if (type.matches("MultiFileInputHeader")) {
                                                                                             optionElement.setText(WorkflowFileUtils.htmlEncode(annotatedColString));
                                                                                         } else if (type.matches("FixedEffectsPanel") || type.matches("RandomEffectsPanel")) {
                                                                                             optionElement.setText("");
                                                                                         }

                                                                                         optionsElement.addContent(optionElement);

                                                                                     } else if (!foundFileInputHeader) {
                                                                                       Element optionElement = new Element(name);
                                                                                       optionElement.setText(WorkflowFileUtils.htmlEncode(annotatedColString));
                                                                                       if (optionsElement.getChild(name) == null) {
                                                                                           optionsElement.addContent(optionElement);
                                                                                           foundFileInputHeader = true;
                                                                                       }
                                                                                     }

                                                                                 } // e.o.  is label
                                                                             } // e.o.  headerName matches default (regexp)
                                                                         } // e.o.  headerIndex is valid integer
                                                                     } // e.o.  if metadata has index
                                                                 } // e.o.  for each metadata child
                                                             } // e.o.  if metadata != null
                                                         } // e.o.  for each file

                                                     } else { // if dirtySelection, then we will try to use the previously
                                                         // selected values, but only if they exist in the inputs.
                                                         for (Element file : files) {
                                                             staticLogger.trace("Processing inFileNode " + inNodeIndexToMatch + ".");
                                                             Element metadata = file.getChild("metadata");
                                                             if (metadata != null && metadata.getChildren() != null) {

                                                                 for (Element child : (List<Element>) metadata.getChildren()) {
                                                                     if (child.getChild("index") != null) {


                                                                         String headerIndex = child.getChildText("index");
                                                                         String headerName = WorkflowFileUtils.htmlDecode(
                                                                                 child.getChildText("name"));
                                                                         if (headerIndex != null && headerIndex.matches("\\d+")) {
                                                                             // defaultValue can be a regular expression defined in the xsd
                                                                             if (headerName != null && headerName.matches(defaultValue)) {

                                                                                 String annotatedColString = "Input " + inNodeIndexToMatch + " ("
                                                                                     + fileIndexByNode.get(inNodeIndexToMatch).toString() + ")" + " - "
                                                                                         + headerName + " (column " + headerIndex + ")";

                                                                                 // If a labeled header exists, we will use that
                                                                                 if (actualValues.containsKey(annotatedColString.toLowerCase())) {

                                                                                     if (type.matches("MultiFileInputHeader")
                                                                                             || type.matches("FixedEffectsPanel")
                                                                                                 || type.matches("RandomEffectsPanel") ) {

                                                                                         Element optionElement = new Element(name);
                                                                                         if (type.matches("MultiFileInputHeader")) {
                                                                                             optionElement.setText(WorkflowFileUtils.htmlEncode(annotatedColString));
                                                                                         } else if (type.matches("FixedEffectsPanel") || type.matches("RandomEffectsPanel")) {
                                                                                             optionElement.setText("");
                                                                                         }

                                                                                         optionsElement.addContent(optionElement);

                                                                                     } else if (!foundFileInputHeader) {
                                                                                         Element optionElement = new Element(name);
                                                                                         optionElement.setText(WorkflowFileUtils.htmlEncode(annotatedColString));
                                                                                         if (optionsElement.getChild(name) != null) {
                                                                                             optionsElement.removeChild(name);
                                                                                         }
                                                                                         optionsElement.addContent(optionElement);
                                                                                         foundFileInputHeader = true;
                                                                                     }

                                                                                 } // e.o.  is label
                                                                             } // e.o.  headerName matches default (regexp)
                                                                         } // e.o.  headerIndex is valid integer
                                                                     } // e.o.  if metadata has index
                                                                 } // e.o.  for each metadata child
                                                             } // e.o.  if metadata != null
                                                         } // e.o.  for each file

                                                     }

                                                     // If we find no matches in the first file, then choose a default header
                                                     // which can be overridden by a match in any subsequent file.
                                                     if (!foundFileInputHeader && findFirstHeader && !type.matches("MultiFileInputHeader")
                                                             && !type.matches("FixedEffectsPanel") && !type.matches("RandomEffectsPanel")) {
                                                         // No "label" metadata was found, nor did any metadata headers match
                                                         // the previously selected values.
                                                         for (Element file : files) {

                                                             staticLogger.trace("Processing inFileNode " + inNodeIndexToMatch + ".");
                                                             Element metadata = file.getChild("metadata");


                                                             if (metadata != null && metadata.getChildren() != null) {

                                                                 for (Element child : (List<Element>) metadata.getChildren()) {
                                                                     if (child.getChild("index") != null) {


                                                                         String headerIndex = child.getChildText("index");
                                                                         String headerName = WorkflowFileUtils.htmlDecode(
                                                                                 child.getChildText("name"));
                                                                         if (headerIndex != null && headerIndex.matches("\\d+")) {
                                                                             // The variable defaultValue can be a regular expression defined in the xsd
                                                                             if (findFirstHeader && headerName != null && headerName.matches(defaultValue)) {

                                                                                 String annotatedColString = "Input " + inNodeIndexToMatch + " ("
                                                                                     + fileIndexByNode.get(inNodeIndexToMatch).toString() + ")" + " - "
                                                                                         + headerName + " (column " + headerIndex + ")";

                                                                                 Element optionElement = new Element(name);
                                                                                 optionElement.setText(WorkflowFileUtils.htmlEncode(annotatedColString));
                                                                                 if (optionsElement.getChild(name) != null) {
                                                                                     optionsElement.removeChild(name);
                                                                                 }

                                                                                 optionsElement.addContent(optionElement);

                                                                                 findFirstHeader = false;
                                                                                 break;
                                                                             } // e.o.  headerName matches default (regexp)
                                                                         } // e.o.  headerIndex is valid number
                                                                     } // e.o.  if metadata has index
                                                                 } // e.o.  each metdata child

                                                             } // e.o.  if metadata != null

                                                         } // e.o.  for each file
                                                     }
                                                 } // e.o.   if files != null
                                             }
                                         } // e.o.  inputFileIndex matches
                                     } // e.o.  inputNodeIndex matches
                                 } // e.o.  for each componentInputNodes
                             } // e.o.  if componentInputNodes != null

                         } // e.o.  option types else (MultiFileInputHeader, FileInputHeader, other custom)

                     } // e.o.  option guard (must have type/name)
                 } // e.o.  for each option in schema options
             }
         } else {
             staticLogger.error("Workflow component not found (" + componentName + ")");
         }

         return optionsElement;
     }

     /**
      * Gets the advanced options for a component.
      * @param componentId the component id
      * @param componentType the component type
      * @param componentName the component name
      * @param string
      * @return the XML containing the options and their values
      * @throws JSONException
      */
     public static JSONArray getOptionTypes(WorkflowComponentItem workflowComponentItem) throws JSONException {

         JSONArray optionTypes = new JSONArray();

         if (workflowComponentItem != null) {

             // Get all children of any options element
             List<Element> optionNodes =
                     WorkflowXmlUtils.getNodeList(workflowComponentItem.getSchemaPath(), XML_OPTIONS_CHILD_NODES);

             if (optionNodes != null) {
                 staticLogger.trace("Component options found: " + optionNodes.size());
                 for (int i = 0; i < optionNodes.size(); i++) {

                     Element node = optionNodes.get(i);


                     if (node != null
                             && node.getAttribute("type") != null
                                 && node.getAttribute("name") != null) {

                         String displayName = node.getAttributeValue("id");
                         String name = node.getAttributeValue("name");
                         String type = node.getAttributeValue("type");
                         String defaultValue = node.getAttributeValue("default");

                         JSONObject optionInfo = new JSONObject();
                         optionInfo.put("type", type);
                         optionInfo.put("name", name);
                         optionInfo.put("defaultValue", defaultValue);
                         optionInfo.put("displayName", displayName);

                         optionTypes.put(optionInfo);
                     }

                 }
             }
         }
         return optionTypes;
     }


     /**
      * Gets the advanced options for a component.
      * @param componentId the component id
      * @param componentType the component type
      * @param componentName the component name
      * @param string
      * @return the XML containing the options and their values
      */
     public String getAdvancedComponentOptions(WorkflowItem workflowItem,
             String baseDir, String tableTypesFile, UserItem userItem,
                 String componentId, String componentName, String workflowComponentsDir) {

         WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
         WorkflowComponentInstanceDao wcInstanceDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
         WorkflowComponentInstanceItem wcInstanceItem = wcInstanceDao.findByWorkflowAndId(workflowItem, componentId);

         String componentAccessLevel = WorkflowAccessHelper.getComponentAccessLevel(
                 userItem, baseDir, workflowItem, componentId);

         staticLogger.info("Getting component options.");
         WorkflowComponentItem workflowComponentItem = null;
         if (componentName != null && !componentName.equalsIgnoreCase("null")) {
             componentName = componentName.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
             workflowComponentItem = workflowComponentDao.findByName(componentName);
         }
         StringBuffer componentOptBuffer = new StringBuffer();

         if (workflowComponentItem != null && workflowItem != null
                 && componentId != null && componentId.matches(WorkflowIfaceHelper.COMPONENT_ID_PATTERN)) {

             String componentSubdir = "workflows/" + workflowItem.getId() + "/" + componentId + "/output/";

             String componentXmlPath = WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getStrictDirFormat(baseDir)
                 + "workflows/" + workflowItem.getId() + "/components/" + componentId + ".xml");

             File testFile = new File(componentXmlPath);


             List<Element> componentInputNodes = null;
             if (testFile.exists() && testFile.isFile()) {
                 componentInputNodes = WorkflowXmlUtils.getNodeList(componentXmlPath,
                         ConnectionHelper.XML_INPUT_FILE_NODES);
             }

             List<Element> optionNode = null;
             if (testFile.exists() && testFile.isFile()) {
                 optionNode = WorkflowXmlUtils.getNodeList(componentXmlPath, ComponentHelper.XML_INSTANCE_OPTION_NODE);
             }

             // Get all children of any options element
             List<Element> optionNodes =
                     WorkflowXmlUtils.getNodeListInjection(workflowComponentItem.getSchemaPath(),
                             ComponentHelper.XML_OPTIONS_CHILD_NODES);

             if (optionNodes != null) {
                 staticLogger.trace("Component options found: " + optionNodes.size());
                 for (int i = 0; i < optionNodes.size(); i++) {

                     Element node = optionNodes.get(i);

                     List<String> actualValuesKeys = new ArrayList<String>();
                     if (node != null
                             && node.getAttribute("type") != null
                                 && node.getAttribute("name") != null) {

                         Integer inputNodeIndex = null;
                         Integer inputFileIndex = null;
                         if (node.getAttribute("inputNodeIndex", WorkflowHelper.lsXmlNs) != null) {
                             String inputNodeIndexStr = node.getAttributeValue("inputNodeIndex", WorkflowHelper.lsXmlNs);
                             if (inputNodeIndexStr != null ) {
                                 if (inputNodeIndexStr.matches("[0-9]+")) {
                                     inputNodeIndex = Integer.parseInt(inputNodeIndexStr);
                                 }
                             }
                         }

                         if (node.getAttribute("inputFileIndex", WorkflowHelper.lsXmlNs) != null) {
                             String inputFileIndexStr = node.getAttributeValue("inputFileIndex", WorkflowHelper.lsXmlNs);
                             if (inputFileIndexStr != null ) {
                                 if (inputFileIndexStr.matches("[0-9]+")) {
                                     inputFileIndex = Integer.parseInt(inputFileIndexStr);
                                 }
                             }
                         }

                         String displayName = node.getAttributeValue("id");
                         String name = node.getAttributeValue("name");
                         String type = node.getAttributeValue("type");
                         String defaultValue = node.getAttributeValue("default");

                         // Allow users to set an option to be private
                         String isPrivateStr = node.getAttributeValue("privateOption", WorkflowHelper.lsXmlNs);
                         boolean isPrivate = false;
                         if (isPrivateStr != null) {
                             try {
                                 isPrivate = Boolean.parseBoolean(isPrivateStr);
                             } catch (Exception e) {
                                 // The private option defaults to false unless the user specifies it to be true
                             }
                         }

                         Boolean foundOption = false;
                         String foundValue = null;

                         String workflowsDir = WorkflowFileUtils.getWorkflowsDir(workflowItem.getId());
                         //List<ComponentNode> expectedInputs =
                         //        getConnectedInputsInfo(workflowItem.getId(), workflowsDir, tableTypesFile,
                         //            userItem, componentId, componentName, type);

                         if (optionNode != null && !optionNode.isEmpty() && optionNode.get(0).getChildren() != null) {
                             for (Element optionChild : (List<Element>) optionNode.get(0).getChildren()) {
                                 if (optionChild.getName().equalsIgnoreCase(name)) {
                                     foundOption = true;
                                     foundValue = optionChild.getTextTrim();
                                     if (!(isPrivate && componentAccessLevel.equals("view"))) {
                                         if (type.matches("FixedEffectsPanel") || type.matches("RandomEffectsPanel")) {
                                             actualValuesKeys.add(foundValue);
                                         } else {
                                             actualValuesKeys.add(foundValue.toLowerCase());
                                         }
                                     } else {
                                         actualValuesKeys.add("This option is private and you do not have access to it");
                                     }
                                     //if (!type.matches("MultiFileInputHeader")) {
                                     //    break;
                                     //}
                                 }
                             }
                         }

                         List<Element> optionChildNodes = null;

                         StringBuffer enumBuffer = new StringBuffer();
                         StringBuffer termEnumBuffer = new StringBuffer();

                         // First, build the ENUMs if there are any
                         if (type.equalsIgnoreCase(XML_SIMPLE_TYPE_BOOLEAN)) {
                             String theValue = (foundValue == null) ? defaultValue : foundValue;

                             // Boolean should also be represented as select drop-downs.
                             String selectTrue = "";
                             String selectFalse = "";
                             if (actualValuesKeys.contains("false")
                                     || (actualValuesKeys.isEmpty() && defaultValue.equalsIgnoreCase("false"))) {
                                 selectFalse = "selected='selected'";
                             } else if (actualValuesKeys.contains("true")
                                     || (actualValuesKeys.isEmpty() && defaultValue.equalsIgnoreCase("true"))) {
                                 selectTrue = "selected='selected'";
                             }

                             enumBuffer.append("<option value=\"true\" " + selectTrue + ">True</option>");
                             enumBuffer.append("<option value=\"false\" " + selectFalse + ">False</option>");

                         } else if (type.matches(".*Type")) {
                             // This is an enumerated type element.
                             // It will be used to create a select drop-down box.
                             optionChildNodes = WorkflowXmlUtils.getNodeList(workflowComponentItem.getSchemaPath(),
                                 XML_SIMPLE_TYPE_ENUMERATED_PREFIX + type + XML_SIMPLE_TYPE_ENUMERATED_SUFFIX);
                             if (optionChildNodes != null) {
                                 for (int j = 0; j < optionChildNodes.size(); j++) {

                                     Element enumNode = optionChildNodes.get(j);
                                     String isSelected = "";
                                     if (enumNode.getAttribute("value") != null) {

                                         if (foundOption) {
                                             if (actualValuesKeys.contains(enumNode.getAttributeValue("value").trim().toLowerCase())) {
                                                 isSelected = "selected='selected'";
                                             }
                                         }

                                         enumBuffer.append("<option value=\""
                                             + WorkflowFileUtils.htmlEncode(enumNode.getAttributeValue("value"))
                                              + "\" " + isSelected + ">"
                                              + WorkflowFileUtils.htmlEncode(enumNode.getAttributeValue("value"))
                                             + "</option>");

                                     }
                                 }
                             }
                         } else if (type.matches("FileInputHeader") || type.matches("MultiFileInputHeader")) {
                             String regExp = defaultValue; //.replaceAll("\\\\", "\\\\\\\\\\\\");
                             staticLogger.info("Matching regular expression: " + regExp);

                             Integer inNodeIndexToMatch = 0;
                             if (componentInputNodes != null) {

                                 Map<Integer, Integer> fileIndexByNode = new HashMap<Integer, Integer>();

                                 // Sort the order of the nodes
                                 for (int j = 0; j < componentInputNodes.size(); j++) {

                                         Element inputNode = componentInputNodes.get(j);
                                         Element inFileNode = inputNode.getChild("files");

                                         if (inputNode.getName().matches("[a-zA-Z]+[0-9]+")) {
                                             inNodeIndexToMatch = Integer.parseInt(inputNode.getName().replaceAll("[a-zA-Z]+", ""));
                                         }

                                         if (inFileNode != null && !inFileNode.getChildren().isEmpty()) {
                                             if (inputNodeIndex == null || inputNodeIndex == inNodeIndexToMatch) {
                                                 // increment the unordered file index
                                                 if (fileIndexByNode.containsKey(inNodeIndexToMatch)) {
                                                     Integer lastCount = fileIndexByNode.get(inNodeIndexToMatch) + 1;
                                                     if (lastCount > 0 && lastCount < Integer.MAX_VALUE) {
                                                         fileIndexByNode.put(inNodeIndexToMatch, lastCount);
                                                     }
                                                 } else {
                                                     fileIndexByNode.put(inNodeIndexToMatch, 0);
                                                 }

                                                 if (inputFileIndex == null || fileIndexByNode.get(inNodeIndexToMatch) == inputFileIndex) {
                                                     /*Boolean minOccursSat = true;
                                                     Boolean maxOccursSat = true;
                                                     if (expectedInputs != null && !expectedInputs.isEmpty()) {
                                                         for (ComponentNode cNode : expectedInputs) {
                                                             if (fileIndexByNode.containsKey(j)
                                                                     && fileIndexByNode.get(j) >= cNode.getMinOccurs() - 1) {
                                                                 minOccursSat = false;
                                                             }
                                                             if (fileIndexByNode.containsKey(j)
                                                                     && fileIndexByNode.get(j) >= cNode.getMaxOccurs()) {
                                                                 maxOccursSat = false;
                                                             }
                                                         }
                                                     }*/

                                                     List<Element> files = inFileNode.getChildren();
                                                     if (files != null) {
                                                         Boolean isFirstDefaultOption = true;
                                                         for (Element file : files) {
                                                             staticLogger.trace("Processing inFileNode "  + inNodeIndexToMatch + ".");

                                                             replaceExistingFilesWithIds(baseDir, (Long) workflowItem.getId(),
                                                                     componentId, componentSubdir, file);

                                                             Element metadata = file.getChild("metadata");

                                                             // Sigh. getChildren doesn't return duplicates (which we often have in
                                                             // TxExport headers) so index doesn't match headerIndex and we need to
                                                             // maintain the order so put them in a TreeMap for processing.
                                                             TreeMap<Integer, Object> childMap = new TreeMap<Integer, Object>();
                                                             if (metadata != null && metadata.getChildren() != null) {
                                                                 for (Element child : (List<Element>) metadata.getChildren()) {
                                                                     if (child.getChild("index") != null) {
                                                                         childMap.put(new Integer(Integer.parseInt(child.getChildText("index"))),
                                                                                      child);
                                                                     }
                                                                 }
                                                             }

                                                             if (childMap != null) {
                                                                 // Sort the order of the columns
                                                                 for (Iterator iter = childMap.keySet().iterator(); iter.hasNext(); ) {
                                                                     Element child = (Element)childMap.get((Integer)iter.next());
                                                                     if (child.getChild("index") != null) {
                                                                         String isSelected = "";
                                                                         //Keep: String headerId = child.getChildText("id");
                                                                         String headerIndex = child.getChildText("index");
                                                                         String headerName = WorkflowFileUtils.htmlDecode(
                                                                             child.getChildText("name"));
                                                                         if (headerIndex.matches("\\d+")) {
                                                                             if (headerName != null && headerName.matches(regExp)) {
                                                                                 String headerString = headerName;
                                                                                 String annotatedString = "Input " + inNodeIndexToMatch + " ("
                                                                                         + fileIndexByNode.get(inNodeIndexToMatch).toString() + ")" + " - "
                                                                                         + headerString
                                                                                         + " (column " + headerIndex + ")";
                                                                                 if (foundOption) {
                                                                                     if (actualValuesKeys.contains(annotatedString.trim().toLowerCase())) {

                                                                                         if (wcInstanceItem != null && wcInstanceItem.getDirtyAncestor()
                                                                                                 && child.getName().equalsIgnoreCase(AbstractComponent.META_DATA_LABEL)
                                                                                                 && !wcInstanceItem.getDirtySelection()
                                                                                                 && !wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                                                                                 && !wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                                                                             isSelected = "selected='selected'";
                                                                                         } else if (wcInstanceItem == null
                                                                                                 || (wcInstanceItem.getDirtySelection())
                                                                                                 || wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                                                                                 || wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                                                                             isSelected = "selected='selected'";
                                                                                         } else {
                                                                                             isSelected = "";
                                                                                             foundOption = false;
                                                                                         }
                                                                                     }
                                                                                 }

                                                                                 if (!foundOption && type.matches("FileInputHeader")) {
                                                                                     if (wcInstanceItem != null && !wcInstanceItem.getDirtySelection()
                                                                                             && !wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                                                                             && !wcInstanceItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)
                                                                                             && isFirstDefaultOption) {
                                                                                         isSelected = "selected='selected'";
                                                                                         isFirstDefaultOption = false;
                                                                                     } else if (wcInstanceItem == null && isFirstDefaultOption) {
                                                                                         isSelected = "selected='selected'";
                                                                                         isFirstDefaultOption = false;
                                                                                     } else {
                                                                                         isSelected = "";
                                                                                     }
                                                                                 }
                                                                                 String enumStr = "Input " + inNodeIndexToMatch + " ("
                                                                                         + fileIndexByNode.get(inNodeIndexToMatch).toString() + ")" + " - "
                                                                                      + WorkflowFileUtils.htmlEncode(headerString)
                                                                                      + " (column " + headerIndex + ")";
                                                                                 enumBuffer.append("<option value=\"" + enumStr
                                                                                                   + "\" " + isSelected + ">"
                                                                                                   + enumStr
                                                                                                   + "</option>");
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                 } // end of outer for loop (matching input nodes)
                             }

                             } else if (type.matches("FixedEffectsPanel") || type.matches("RandomEffectsPanel")) {
                                     String regExp = defaultValue; //.replaceAll("\\\\", "\\\\\\\\\\\\");

                                     Integer inNodeIndexToMatch = 0;
                                     if (componentInputNodes != null) {
                                         // Sort the order of the nodes
                                         for (int j = 0; j < componentInputNodes.size(); j++) {

                                             Element inputNode = componentInputNodes.get(j);
                                             Element inFileNode = inputNode.getChild("files");

                                             if (inputNode.getName().matches("[a-zA-Z]+[0-9]+")) {
                                                 inNodeIndexToMatch = Integer.parseInt(inputNode.getName().replaceAll("[a-zA-Z]+", ""));
                                             }

                                             if (inputNodeIndex == null || inputNodeIndex == inNodeIndexToMatch) {

                                                 List<Element> files = inFileNode.getChildren();
                                                 if (files != null) {

                                                     for (Element file : files) {
                                                         staticLogger.trace("Processing inFileNode "  + j + ".");
                                                         replaceExistingFilesWithIds(baseDir, (Long) workflowItem.getId(),
                                                                 componentId, componentSubdir, file);

                                                         Element metadata = file.getChild("metadata");
                                                         // Sigh. getChildren doesn't return duplicates (which we often have in
                                                         // TxExport headers) so index doesn't match headerIndex and we need to
                                                         // maintain the order so put them in a TreeMap for processing.
                                                         TreeMap<Integer, Object> childMap = new TreeMap<Integer, Object>();
                                                         if (metadata != null && metadata.getChildren() != null) {
                                                             for (Element child : (List<Element>) metadata.getChildren()) {
                                                                 if (child.getChild("index") != null) {
                                                                     childMap.put(new Integer(Integer.parseInt(child.getChildText("index"))),
                                                                                  child);
                                                                 }
                                                             }
                                                         }
                                                         if (childMap != null) {
                                                             // Sort the order of the columns
                                                             for (Iterator iter = childMap.keySet().iterator(); iter.hasNext(); ) {
                                                                 Element child = (Element)childMap.get((Integer)iter.next());
                                                                 if (child.getChild("index") != null) {
                                                                     //Keep: String headerId = child.getChildText("id");
                                                                     String headerIndex = child.getChildText("index");
                                                                     String headerName = WorkflowFileUtils.htmlDecode(
                                                                             child.getChildText("name"));
                                                                     if (headerIndex.matches("\\d+")) {
                                                                         if (headerName != null && headerName.matches(regExp)) {
                                                                             String headerString = headerName;
                                                                             String enumStr = WorkflowFileUtils.htmlEncode(headerString);
                                                                                        enumBuffer.append("<option value=\"" + enumStr + "\">"
                                                                                                          + enumStr
                                                                                                          + "</option>");

                                                                         }
                                                                     }
                                                                 }
                                                             }
                                                             if (actualValuesKeys != null && actualValuesKeys.size() != 0) {
                                                                     for (int foundValInd = 0; foundValInd < actualValuesKeys.size(); foundValInd++) {
                                                                             String foundVal = actualValuesKeys.get(foundValInd);
                                                                             if (foundVal != null && !foundVal.trim().equals("")) {
                                                                                     String termStr = WorkflowFileUtils.htmlEncode(foundVal);

                                                                                      //foundVal should be comma separated string

                                                                                     List<String> items = Arrays.asList(termStr.split("\\s*,\\s*"));
                                                                                     for (String item : items) {
                                                                                             termEnumBuffer.append("<option value=\"" + item + "\">"
                                                                                                  + item
                                                                                                  + "</option>");
                                                                                     }
                                                                                     /*termEnumBuffer.append("<option value=\"" + termStr + "\">"
                                                                                                     + termStr
                                                                                                     + "</option>");*/

                                                                             }
                                                                     }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }


                                         } // end of outer for loop (matching input nodes)
                                     }


                         } else if (type.matches("Detectors")) {
                             enumBuffer.append(getDetectorListHtml(workflowComponentsDir, baseDir));
                         }

                         // Do not join this if statement with the previous if/else block
                         if (type.equalsIgnoreCase(ComponentHelper.XML_OPTION_FILE_LIST_DEF)) {
                             // The element is a specific element, the FileList type.
                             List<Element> fileNodes = WorkflowXmlUtils.getNodeList(workflowComponentItem.getSchemaPath(),
                                     ComponentHelper.XML_OPTION_FILE_LIST_NODES);

                             optionChildNodes = WorkflowXmlUtils.getNodeList(tableTypesFile,
                                     ComponentHelper.XML_FILE_CONTAINER_DEF);

                             Integer optionFileIndex = 0;
                             for (int fileCount = 0; fileCount < fileNodes.size(); fileCount++) {
                                 Element fileNode = fileNodes.get(fileCount);


                                 if (optionChildNodes != null) {
                                     Integer newFileCounter = 0;
                                     Map<Integer, Element> valueMap = new HashMap<Integer, Element>();
                                     String expectedFileType = WorkflowFileUtils.htmlEncode(fileNode.getAttributeValue("ref"));
                                     String minOccursStr = fileNode.getAttributeValue("minOccurs");
                                     Integer minOccurs = 0;
                                     if (minOccursStr != null && minOccursStr.matches("\\d+")) {
                                         minOccurs = Integer.parseInt(minOccursStr);
                                     }

                                     if (expectedFileType != null) {

                                         if (optionNode != null && !optionNode.isEmpty()
                                                 && optionNode.get(0).getChildren() != null) {


                                             staticLogger.trace("Option File found in component: " + componentId);
                                             if (((Element)optionNode.get(0)).getChild("files") != null) {
                                                 Element filesNode = ((Element)optionNode.get(0)).getChild("files");

                                                 List<Element> existingFiles = filesNode.getChildren();
                                                 if (existingFiles != null && !existingFiles.isEmpty()) {
                                                     for (Element existingFile : existingFiles) {
                                                         List<String> potentialAncestors = new ArrayList<String>();
                                                         potentialAncestors.add(expectedFileType);

                                                         if (ComponentHierarchyHelper.isTypeOfAnyTable(workflowComponentItem.getSchemaPath(),
                                                                 existingFile.getName(), potentialAncestors)) {
                                                             valueMap.put(newFileCounter, existingFile); //mckz
                                                             newFileCounter++;
                                                             if (existingFile.getChild("index") != null) {
                                                                 //Keep: String existingFileIndex = existingFile.getChildText("index");
                                                                     // in this iteration, this is an empty value
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }

                                             }
                                         }


                                         for (int fileCounter = 0 ; fileCounter < minOccurs; fileCounter++) {
                                             componentOptBuffer.append("<" + "file" + ">");

                                             for (int j = 0; j < optionChildNodes.size(); j++) {
                                                 componentOptBuffer.append("<option>");
                                                 Element xsdOptionNode = optionChildNodes.get(j);

                                                 String xsdOptionName = xsdOptionNode.getAttributeValue("name");
                                                 String xsdOptionType = xsdOptionNode.getAttributeValue("type");
                                                 String optDefaultValue = xsdOptionNode.getAttributeValue("defaultValue");

                                                 Integer fileIndex = optionFileIndex;


                                                 String vPath = null;
                                                 String vName = null;
                                                 if ( (xsdOptionName.equalsIgnoreCase("file_path") || xsdOptionName.equalsIgnoreCase("file_name"))
                                                         && valueMap.containsKey(fileIndex) && valueMap.get(fileIndex) != null
                                                         && valueMap.get(fileIndex).getChildren() != null
                                                             && valueMap.get(fileIndex).getChild("file_path") != null) {
                                                     vPath = valueMap.get(fileIndex).getChildText("file_path");

                                                     WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                                                     File newFile = new File(WorkflowFileUtils.sanitizePath(vPath));
                                                     if (newFile != null) {
                                                         String parentPath = newFile.getParentFile().getAbsolutePath();
                                                         String sanitizedParentPath = WorkflowFileUtils.sanitizePath(parentPath + "/");
                                                         // Get the path relative to the DataShop baseDir (e.g., workflows/1/components/Import-x-102942/output/)
                                                         String fileDir = sanitizedParentPath.replaceAll(WorkflowFileUtils.getStrictDirFormat(baseDir), "");
                                                         List<WorkflowFileItem> fileItems =  wfFileDao.find(fileDir, newFile.getName());

                                                         if (fileItems != null && !fileItems.isEmpty()) {
                                                             vPath = fileItems.get(0).getId().toString();
                                                         }

                                                         vName = valueMap.get(fileIndex).getChildText("file_name");

                                                     }
                                                 }

                                                 if (vPath != null && xsdOptionName.equalsIgnoreCase("file_path")
                                                         && vPath.matches("\\d+")) {
                                                     optDefaultValue = vPath;
                                                 } else if (vName != null && xsdOptionName.equalsIgnoreCase("file_name")) {
                                                     optDefaultValue = WorkflowFileUtils.htmlEncode(vName);
                                                 } else if (vName != null && xsdOptionName.equalsIgnoreCase("index")
                                                         && vName.matches("\\d+")) {
                                                     optDefaultValue = fileIndex.toString();
                                                 }


                                                 componentOptBuffer.append("<type>"
                                                     + xsdOptionType + "</type>");
                                                 componentOptBuffer.append("<name>"
                                                     + xsdOptionName + "</name>");
                                                 // For the label, simply use the file type attribute defined in workflow component xsd (file "ref")
                                                 if (expectedFileType != null && xsdOptionName.equalsIgnoreCase("label")) {
                                                     componentOptBuffer.append("<defaultValue>"
                                                     + expectedFileType + "</defaultValue>");
                                                 } else if (optDefaultValue != null) {
                                                     componentOptBuffer.append("<defaultValue>"
                                                     + optDefaultValue + "</defaultValue>");
                                                 }



                                                 componentOptBuffer.append("</option>");
                                             }

                                             componentOptBuffer.append("</" + "file" + ">");
                                             optionFileIndex++;
                                         }
                                     } else {
                                         staticLogger.error("No expected file type defined in XSD.");
                                     }
                                 }

                         } else {
                             // The element is a simplyType element or a Multi/FileInputHeader
                             componentOptBuffer.append("<option>");
                             componentOptBuffer.append("<type>"
                                 + type + "</type>");
                             componentOptBuffer.append("<name>"
                                 + name + "</name>");
                             if (defaultValue != null) {
                                 componentOptBuffer.append("<defaultValue>"
                                 + WorkflowFileUtils.htmlEncode(defaultValue)
                                 + "</defaultValue>");
                             }
                             if (name != null && !name.isEmpty()) {
                                 componentOptBuffer.append("<displayName>"
                                 + WorkflowFileUtils.htmlEncode(displayName)
                                 + "</displayName>");
                             }
                             if (isPrivate) {
                                 componentOptBuffer.append("<isPrivate>true</isPrivate>");
                             }
                             if (!enumBuffer.toString().isEmpty()) {
                                 componentOptBuffer.append("<enum>"
                                     + enumBuffer.toString() + "</enum>");
                             }

                             if (!termEnumBuffer.toString().isEmpty()) {
                                     componentOptBuffer.append("<termEnum>"
                                         + termEnumBuffer.toString() + "</termEnum>");
                                 }


                             componentOptBuffer.append("</option>");

                             //currentOptionValues.put(name, actualValuesKeys);
                         }
                     }
                 }
         }


         } else {
             staticLogger.error("Workflow component not found (" + componentId + ")");
         }
         staticLogger.trace("Component options: " + componentOptBuffer.toString());

         return componentOptBuffer.toString();
     }

    /**
      * Get the endpoints without using the DAO so that components do not need to load additional classes
      * simply to initialize.
      * @param wfComponentItems the workflow component items (they can be unsaved items)
      * @param xPathPattern the XPath pattern (input or output endpoint)
      * @return the JSONObject describing the endpoints
      * @throws IOException
      * @throws JDOMException
      */
     public static JSONObject getAllOptionTypes() throws JDOMException, IOException {
         if (finalOptionTypes != null) {
             return finalOptionTypes;
         }
         JSONObject endpoints = new JSONObject();
         WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
         List<WorkflowComponentItem> wfComponentItems = workflowComponentDao.findAll();

         for (WorkflowComponentItem wfComponentItem : wfComponentItems) {

             JSONArray optionsArray;
             try {
                 optionsArray = getOptionTypes(wfComponentItem);
                 if (optionsArray != null) {
                     endpoints.put(wfComponentItem.getComponentType().toLowerCase().trim().replaceAll("[^a-zA-Z0-9]", "_")
                         + "-" + wfComponentItem.getComponentName().toLowerCase().trim().replaceAll("[^a-zA-Z0-9]", "_"),
                         optionsArray);
                 }
             } catch (JSONException e) {

             }
         }

         if (finalOptionTypes == null) {
             finalOptionTypes = endpoints;
         }
         return endpoints;
     }


     /**
      * This method removes any output files that exist for a given a component id (does not
      * remove the component XML, as this is not for deleting a component.
      * @param workflowItem the workflow
      * @param componentId the component
      * @param baseDir the datashop base dir
      */
     public static void deleteComponentOutput(WorkflowItem workflowItem, String componentId, String baseDir) {
         ComponentFileDao compFileDao = DaoFactory.DEFAULT.getComponentFileDao();
         ComponentFilePersistenceDao compFilePersistenceDao = DaoFactory.DEFAULT.getComponentFilePersistenceDao();
         String subPath = WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()) + componentId + "/output/";
         String wholePath = WorkflowFileUtils.sanitizePath(baseDir + "/" + subPath);
         File componentOutputDir = new File(wholePath);

         WorkflowFileDao wfDao = DaoFactory.DEFAULT.getWorkflowFileDao();
         List<ComponentFileItem> componentFileItems = compFileDao.findImportByComponent(workflowItem, componentId);
         List<Integer> wfFileIds = new ArrayList<Integer>();
         if (componentFileItems != null && !componentFileItems.isEmpty()) {
             for (Iterator<ComponentFileItem> cfIterator = componentFileItems.iterator(); cfIterator.hasNext() ;) {
                 ComponentFileItem componentFileItem = cfIterator.next();
                 if (componentFileItem.getFile() != null) {
                     Integer compFileId = (Integer) componentFileItem.getFile().getId();
                     WorkflowFileItem itemExists = wfDao.find(compFileId);
                     if (itemExists != null) {
                         if (!wfFileIds.contains((Integer) itemExists.getId())) {
                             wfFileIds.add((Integer) itemExists.getId());
                         }
                         compFileDao.delete(componentFileItem);
                     }
                 }

             }
         }

         List<ComponentFilePersistenceItem> componentFilePersistenceItems = compFilePersistenceDao.findImportByComponent(workflowItem, componentId);

         if (componentFilePersistenceItems != null && !componentFilePersistenceItems.isEmpty()) {
             for (Iterator<ComponentFilePersistenceItem> cfPersistenceIterator = componentFilePersistenceItems.iterator(); cfPersistenceIterator.hasNext() ;) {
                 ComponentFilePersistenceItem componentFilePersistenceItem = cfPersistenceIterator.next();
                 if (componentFilePersistenceItem.getFile() != null) {
                     Integer compFileId = (Integer) componentFilePersistenceItem.getFile().getId();
                     WorkflowFileItem itemExists = wfDao.find(compFileId);
                     if (itemExists != null) {
                         if (!wfFileIds.contains((Integer) itemExists.getId())) {
                             wfFileIds.add((Integer) itemExists.getId());
                         }
                         compFilePersistenceDao.delete(componentFilePersistenceItem);
                     }
                 }

             }
         }

         for (Integer wfFileId : wfFileIds) {
             WorkflowFileItem wfFileItem = wfDao.find(wfFileId);
             wfDao.delete(wfFileItem);
         }

         File[] files = componentOutputDir.listFiles();
         for (int idx = 0; files != null && idx < files.length; idx++) {
             File fileOrDir = files[idx];

             if (fileOrDir.isFile()) {

                     fileOrDir.delete();

             }

         }
     }

    /*
      * Get meata data for an input to a component.  Used in custom options interfaces
      * @param workflowDir
      * @param componentId
      * @param nodeIndex node index to find input
      * @param fileIndex file index at input node
      * @return String of the input at the specified node
     */
     public static String getComponentMetaDataJson(String workflowDir, String componentId,
             int nodeIndex, int fileIndex)
     {
         if (componentId != null && !componentId.matches(AbstractServlet.STRICT_CHARS)) { componentId = ""; }

         String componentXmlPath = WorkflowFileUtils.sanitizePath(WorkflowFileUtils.getStrictDirFormat(workflowDir)
              + "/components/" + componentId + ".xml");

         File testFile = new File(componentXmlPath);

         String xml = null;
         try {
           //xml = FileUtils.readFileToString(testFile);
           BufferedReader br = new BufferedReader(new FileReader(testFile));
           StringBuilder buf = new StringBuilder();
           while (br.ready()) {
             buf.append(br.readLine());
             if (br.ready()) {buf.append('\n');}
           }
           xml = buf.toString();
         } catch (IOException e) {
           staticLogger.error("Couldn't read testFile getComponentMetaDataJson. " + e.toString());
         }

         String metaJsonString = "Unable to get metadata";
         try {
           JSONObject fileJson = org.json.XML.toJSONObject(xml);

           if (fileJson != null) {
             JSONObject component = fileJson.getJSONObject("component");

             if (component != null) {
               JSONObject inputs = component.getJSONObject("inputs");

               if (inputs != null) {
                 String inputStr = "input" + nodeIndex;
                 JSONObject input = inputs.getJSONObject(inputStr);

                 if (input != null) {
                   JSONObject files = input.getJSONObject("files");

                   if (files != null) {
                     JSONArray fileKeys = files.names();
                     for (int i = 0; i < fileKeys.length(); i++) {
                       String fileKey = fileKeys.getString(i);
                       JSONObject file = files.getJSONObject(fileKey);

                       if (file != null) {
                         int index = file.getInt("index");

                         if (index == fileIndex) {
                           JSONObject metadata = file.getJSONObject("metadata");

                           if (metadata != null) {
                             metaJsonString = metadata.toString();
                           } else {
                             metaJsonString = "no metadata";
                           }
                         } else {
                           staticLogger.debug("not file idex");
                         }
                       }
                     }
                   }
                 }
               }
             }
           }

         } catch (Exception e) {
           staticLogger.error("Exception getting metadata json: " + e.toString());
         }
         return metaJsonString;
     }

    /**
      * Get a the html options of detectors for the apply_detectors component
      * this will produce the html of the dropdown of detectors from the detector datashop project
      * @param workflowComponentDir is the path to the workflow components directory
      * @return html of dropdown for detectors
      */
     private static String getDetectorListHtml(String workflowComponentsDir, String baseDir) {
       // Get the name of the dataset containing the detectors
       String propertiesFileLocation = workflowComponentsDir + File.separator +
           "DetectorTester" + File.separator + "build.properties";

       Properties applyDetectorProperties = new Properties();
       String detectorsDatasetName = null;
       String detectorsProjectName = null;
       try {
         InputStream applyDetectorPropertiesFile = new FileInputStream(propertiesFileLocation);
         applyDetectorProperties.load(applyDetectorPropertiesFile);

         detectorsDatasetName = applyDetectorProperties.getProperty("detectorsDataset");
         detectorsProjectName = applyDetectorProperties.getProperty("authorizationProject");
       } catch (IOException e) {
         staticLogger.error("Error reading apply detector build.properties: " + e.toString());
       }
       staticLogger.debug("detectorsDatasetName" + detectorsDatasetName);
       staticLogger.debug("detectorsProjectName" + detectorsProjectName);

       // Get the list of detectors from the database
       DatasetDao datasetDao = DaoFactory.DEFAULT.getDatasetDao();
       DatasetItem detectorsDataset = null;
       if (detectorsDatasetName != null) {
         List<DatasetItem> datasetsList= datasetDao.find(detectorsDatasetName);
         if (datasetsList.size() > 0) {
           detectorsDataset = datasetsList.get(0);
         }
       }

       FileDao fileDao = DaoFactory.DEFAULT.getFileDao();

       ArrayList<FileItem> detectorFileItems = new ArrayList<FileItem>();
       if (detectorsDataset != null) {
         List<Integer> fileItemIds = fileDao.find(detectorsDataset);
         if (fileItemIds != null) {
           for (Integer id : fileItemIds) {
             FileItem fi = fileDao.get(id);
             if (fi != null) {
               detectorFileItems.add(fi);
             }
           }
         }
       }


       // Put the detectors in html for a select option dropdown
       int i = 0;

       StringBuilder detectorHtml = new StringBuilder();
       for (FileItem fi : detectorFileItems) {
         String isSelected = "";
         if (i == 0) {
           isSelected = "selected='selected'";
         }
         String detectorFileName = fi.getFileName();
         String detectorFilePath = baseDir + File.separator + fi.getFilePath() +
             File.separator + detectorFileName;

         detectorHtml.append("<option value=\""+detectorFilePath+"\" title=\"" +
             detectorFileName+"\" "+isSelected+">"+detectorFileName+"</option>");
         i++;
       }

       return detectorHtml.toString();
     }

    /**
      * Sets any running components to the error state. If a running component is found, return true.
      * @param workflowItem the workflow item
      * @return true if a running component is found for this workflow item; false, otherwise.
      */
     public static Boolean setRunningComponentsToError(WorkflowItem workflowItem) {
         Boolean foundRunningComponent = false;
         WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
         if (workflowItem != null) {
             List<WorkflowComponentInstanceItem> wciItems = wciDao.findByWorkflow(workflowItem);
             if (wciItems != null) {
                 for (WorkflowComponentInstanceItem wciItem : wciItems) {
                     if (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {
                         foundRunningComponent = true;
                         staticLogger.error("Could not catch fatal error. Resetting component state for component "
                             + wciItem.getComponentName() + ".");
                         wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_ERROR);
                         wciDao.saveOrUpdate(wciItem);
                     }
                 }
             }
         }
         return foundRunningComponent;
     }

    /**
      * Replaces absolute paths in component XML with ids used by the database.
      * This prevents any attempts to circumvent authorization checks.
      * @param baseDir
      * @param workflowItem
      * @param userItem
      * @param componentId
      * @param componentSubdir
      * @param root
      * @param executionFlag
      * @return
      */
    public static Element replaceFilePathsWithIds(String baseDir, WorkflowItem workflowItem, UserItem userItem,
            String componentId, String componentSubdir, Element root, Boolean executionFlag) {
        staticLogger.trace("Replace file paths with ids.");
        ElementFilter elementFilter = new ElementFilter("files");
        List<Element> modifyFileList = new ArrayList<Element>();
        String containerDir = WorkflowFileUtils.getStrictDirFormat(baseDir) + "workflows/" + workflowItem.getId();

        for (Iterator<Element> iter = root.getDescendants(elementFilter); iter.hasNext(); ) {
            Element filesElement = iter.next();
            if (filesElement.getChildren() != null) {
                for (Element fileChild : (List<Element>) filesElement.getChildren()) {
                    modifyFileList.add(fileChild);
                }
            }
        }

        for (Element descendant : modifyFileList) {
            Element pathElement = descendant.getChild("file_path");
            Element indexElement = descendant.getChild("index");
            Element labelElement = descendant.getChild("label");
            String filePath = null;
            if (pathElement != null && !pathElement.getTextTrim().isEmpty()) {
                filePath = WorkflowFileUtils.sanitizePath(pathElement.getTextTrim());
            }
            Boolean fileIdSet = false;

            if (filePath != null && !filePath.trim().isEmpty() && !filePath.trim().matches("[0-9]+")) {
                if (filePath.indexOf(containerDir) == 0) {
                    File file = new File(filePath);

                    if (file != null && file.isFile() && file.canRead()) {

                        Integer fileId = WorkflowFileHelper.createOrGetWorkflowFile(
                            workflowItem, userItem, baseDir, file,
                            labelElement.getTextTrim(), indexElement.getTextTrim(), null, componentId, executionFlag);

                        if (fileId != null) {
                            pathElement.setText(fileId.toString());
                            staticLogger.trace("File path replaced: index: " + fileId + ", path: " + filePath);
                        } else {
                            staticLogger.error("File creation2 failed for " + filePath);
                        }

                        fileIdSet = true;
                    }

                    if (!fileIdSet && !filePath.matches("[0-9]+")) {
                        pathElement.setText("");
                        staticLogger.info("The file path was found outside of the designated workflows directory."
                                + " Setting file path to empty string.");
                    }

                }

            }

        }

        return root;
    }

    public static Element replaceFileIdsWithPaths(Element outputMapping, String baseDir) {
        WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        ElementFilter elementFilter = new ElementFilter("file_path");

        List<Element> modifyFileList = new ArrayList<Element>();

        for (Iterator iter = outputMapping.getDescendants(elementFilter); iter.hasNext(); ) {
            Element descendant = (Element) iter.next();
            modifyFileList.add(descendant);
        }

        for (Element descendant : modifyFileList) {
            String fileId = descendant.getTextTrim();

            if (fileId != null && !fileId.isEmpty() && fileId.matches("[0-9]+")) {

                WorkflowFileItem fileItem = wfFileDao.get(Integer.parseInt(fileId));

                if (fileItem != null) {
                    String filePath = WorkflowFileUtils.sanitizePath(fileItem.getFullPathName(baseDir))
                            + fileItem.getFileName();

                    File file = new File(filePath);

                    if (file != null && file.isFile() && file.canRead()) {

                        if (fileId != null) {
                            descendant.setText(filePath.replaceAll("\\\\", "/"));

                        }
                    }
                }
            }
        }

        return outputMapping;
    }

    /**
      * Replace file paths with WorkflowFileItem ids. If a record is not found, we create a new one.
      * @param baseDir the base directory, e.g. C:/datashop/files
      * @param workflowId the workflow id
      * @param componentId the component id
      * @param componentSubdir this is the file_path value, e.g. workflows/1/Import-1-x839573/output/
      * @param fileElement
      * @return
      */
     private static Element replaceExistingFilesWithIds(String baseDir, Long workflowId,
             String componentId, String componentSubdir, Element fileElement) {


         Element filePathChild = fileElement.getChild("file_path");

         // If the file path isn't a file id, then replace it with one.
         if (filePathChild != null
                 && !fileElement.getChildTextTrim("file_path").matches("\\d+")) {
             Boolean fileIdSet = false;
             String filePath = null;
             filePath = WorkflowFileUtils.sanitizePath(fileElement.getChildTextTrim("file_path"));

                 if (filePath != null && !filePath.isEmpty() && !filePath.matches("[0-9]+")) {
                     if (filePath.indexOf(baseDir) == 0) {
                         File thisFile = new File(filePath);

                         if (thisFile != null && thisFile.isFile() && thisFile.canRead()) {
                             // Title is the element's name (e.g., student-step, transaction, etc);
                             // Description is the "label", e.g. (image, tab-delim, csv, student-step, etc)
                             WorkflowFileItem dsFileItem = null;
                             WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
                             List<WorkflowFileItem> fileList =  wfFileDao.find(componentSubdir, thisFile.getName());
                             if (fileList != null && fileList.size() > 0) {
                                 dsFileItem = fileList.get(0);
                             }

                             if (dsFileItem != null) {
                                 filePathChild.setText(dsFileItem.getId().toString());
                             } else {
                                 staticLogger.error("File creation failed for " + filePath);
                             }
                             fileIdSet = true;
                         } else {
                             staticLogger.error("Cannot read file " + thisFile.getAbsolutePath());
                         }
                     }
                     if (!fileIdSet && !filePath.matches("[0-9]+")) {
                         filePathChild.setText("");
                         staticLogger.info("The file path was found outside of the designated workflows directory."
                             + " Setting file path to empty string.");
                     }
                 }
             }

         return fileElement;
     }


}
