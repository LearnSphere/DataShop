package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.ComponentNode;
import edu.cmu.pslc.datashop.workflows.ErrorMessageMap;
import edu.cmu.pslc.datashop.workflows.Workflow;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentAdjacencyItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentInstanceItem;
import edu.cmu.pslc.datashop.workflows.WorkflowComponentItem;
import edu.cmu.pslc.datashop.workflows.WorkflowItem;


public class ConnectionHelper {

    /** Debug logging. */
   private static Logger staticLogger = Logger.getLogger(ConnectionHelper.class.getName());
   /** Component input elements. */
   public static final String XML_INPUT_FILE_NODES = "//inputs/*";
   /** Gets the input file types. The "*" can covers both cases, xs:sequence or xs:all. */
   public static final String XML_INPUT_DEFS = "//xs:complexType[@name='InputType']/*/xs:element[contains(@name,'input')]";
   /** Gets the input file labels. The "*" can covers both cases, xs:sequence or xs:all. */
   public static final String XML_INPUT_LABELS = "//xs:complexType[@name='InputLabel']/*/xs:element[contains(@name,'input')]";
   /** Gets the output file types. The "*" covers both cases, xs:sequence or xs:all. */
   public static final String XML_OUTPUT_DEFS = "//xs:complexType[@name='OutputType']/*/xs:element[contains(@name,'output')]";

   /** Input file list. */
   public static final String XML_INPUT_FILE_LIST_NODES = "//xs:complexType[contains(@name,'InFileList')]/xs:choice/xs:element";
   /** Output file list. */
   public static final String XML_OUTPUT_FILE_LIST_NODES = "//xs:complexType[contains(@name,'OutFileList')]/xs:choice/xs:element";

   /** Global component input type (hierarchy) definition list. */
   public static JSONObject finalInputEndpoints = null;
   /** Global component output type (hierarchy) definition list. */
   public static JSONObject finalOutputEndpoints = null;

   /** Constructor. */
   public ConnectionHelper() {

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
public static List<ComponentNode> getConnectedInputsInfo(Long workflowId,
        String workflowDir, String tableTypesFile, UserItem userItem,
        String componentId, String componentName, String componentType) {

    List<ComponentNode> inputsConnected = new ArrayList<ComponentNode>();
    JSONObject jsonOutputEndpoints = null;
    try {
        WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
        staticLogger.info("Fetching component information for " + componentName);
        WorkflowComponentItem workflowComponentItem =
            workflowComponentDao.findByName(componentName);

        List<WorkflowComponentItem> dummyList = new ArrayList<WorkflowComponentItem>();
        dummyList.add(workflowComponentItem);
        jsonOutputEndpoints = ConnectionHelper
                .getEndpointsNoDao(dummyList, ConnectionHelper.XML_INPUT_DEFS);

        String endpointsKey = componentType.toLowerCase()
            + "-" + componentName;
        if (jsonOutputEndpoints != null && jsonOutputEndpoints.has(endpointsKey)) {

            JSONObject jsonTypeList = jsonOutputEndpoints.getJSONObject(endpointsKey + "_list");
            JSONObject jsonRequiredList = jsonOutputEndpoints.getJSONObject(endpointsKey + "_required");
            JSONObject jsonMaxInputsList = jsonOutputEndpoints.getJSONObject(endpointsKey + "_maxinputs");

                for (Iterator<Object> iter = jsonTypeList.keys(); iter.hasNext(); ) {
                    String k = (String) iter.next();
                    if (k.matches("\\d+")) {
                      try {
                            String minOccursString = "0";
                            if (jsonRequiredList.has(k)) {
                                minOccursString = jsonRequiredList.getString(k);
                            }
                            String maxOccursString = "1";
                            if (jsonMaxInputsList.has(k)) {
                                maxOccursString = jsonMaxInputsList.getString(k);
                            }

                            Integer inputIndex = Integer.parseInt(k);
                            ComponentNode inputNode = new ComponentNode(inputIndex);
                            if ((minOccursString != null && minOccursString.trim().matches("[0-9]+"))) {
                                Integer minOccurs = Integer.parseInt(minOccursString.trim());
                                if (minOccurs > 0) {
                                    inputNode.setMinOccurs(minOccurs);
                                }
                            }
                            if ((maxOccursString != null
                                && (maxOccursString.trim().matches("[0-9]+"))
                                || maxOccursString.trim().equalsIgnoreCase("unbounded"))) {
                                inputNode.setMaxOccursFromString(maxOccursString);
                            }
                            inputsConnected.add(inputNode);
                      } catch (JSONException ex) {
                          staticLogger.debug("Could not parse component endpoints data (JSON) within loop. " + ex.toString());
                      }
                    }
                }
        }
    } catch (JSONException e) {
        staticLogger.error("Could not parse component endpoints data (JSON). " + e.toString());
    } catch (JDOMException e) {
        staticLogger.error("Could not parse component endpoints data (JSON). " + e.toString());
    } catch (IOException e) {
        staticLogger.error("Could not parse component endpoints data (JSON). " + e.toString());
    }

    return inputsConnected;
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
public static JSONObject getEndpointsNoDao(List<WorkflowComponentItem> wfComponentItems, String xPathPattern) throws JDOMException, IOException {
    /*mck3if (finalInputEndpoints != null && xPathPattern != null && xPathPattern.equals(XML_INPUT_DEFS)) {
        return finalInputEndpoints;
    } else if (finalOutputEndpoints != null && xPathPattern != null && xPathPattern.equals(XML_OUTPUT_DEFS)) {
        return finalOutputEndpoints;
    }*/
    JSONObject jsonEndpointObject = new JSONObject();
    for (WorkflowComponentItem wfComponentItem : wfComponentItems) {
        JSONObject jsonEndpointIndex = new JSONObject();
        JSONObject jsonEndpointRequired = new JSONObject();
        JSONObject jsonEndpointMaxInputs = new JSONObject();
        List<Element> endpoints =
                WorkflowXmlUtils.getNodeList(wfComponentItem.getSchemaPath(), xPathPattern);

        if (endpoints != null) {
            for (int i = 0; i < endpoints.size(); i++) {

                try {
                // Add new JSON header array object
                staticLogger.trace("Getting file list info.");
                List<Element> inputNodes = null;
                List<Element> fileNodes = null;
                List<Element> fileLabels = null;
                if (xPathPattern != null && xPathPattern.equals(XML_INPUT_DEFS)) {
                    // Can house consist of one or more file nodes
                    inputNodes = WorkflowXmlUtils.getNodeList(wfComponentItem.getSchemaPath(), XML_INPUT_DEFS);
                    // A single file (some input nodes have only 1 file, some have lists of files)
                    fileNodes = WorkflowXmlUtils.getNodeList(wfComponentItem.getSchemaPath(), XML_INPUT_FILE_LIST_NODES);
                    fileLabels = WorkflowXmlUtils.getNodeList(wfComponentItem.getSchemaPath(), XML_INPUT_LABELS);
                } else if (xPathPattern != null && xPathPattern.equals(XML_OUTPUT_DEFS)) {
                    fileNodes = WorkflowXmlUtils.getNodeList(wfComponentItem.getSchemaPath(),
                            XML_OUTPUT_FILE_LIST_NODES);
                }

                // The non-list will contain the component -> # endpoints
                jsonEndpointObject.put( wfComponentItem.getComponentType().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "-"
                        + wfComponentItem.getComponentName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_"),
                        fileNodes.size());

                for (Element fileNodeElement : fileNodes) {

                    Element fileListElement = null;
                    String indexSuffix = null;
                    Integer index = 0;
                    if (fileNodeElement.getParentElement() != null
                        && fileNodeElement.getParentElement().getParentElement() != null) {
                        fileListElement = fileNodeElement.getParentElement().getParentElement();
                        String fileListName = fileListElement.getAttributeValue("name");
                        if (fileListName.matches("(?i).*FileList[0-9]+")) {
                            indexSuffix = fileListElement.getAttributeValue("name").replaceAll("(?i).*FileList", "");
                        }

                    }
                    if (indexSuffix != null && indexSuffix.matches("\\d+")) {
                        index = Integer.parseInt(indexSuffix);
                    }

                 // The *_list will contain index -> ref
                    if (xPathPattern != null && xPathPattern.equals(XML_INPUT_DEFS)) {
                        if (inputNodes != null && inputNodes.size() > 0) {
                            String minOccursString = null;
                            String maxOccursString = null;
                            for (Element inputNode : inputNodes) {

                                if (inputNode.getAttributeValue("name") != null
                                    && inputNode.getAttributeValue("name").replaceAll("input", "")
                                        .trim().equalsIgnoreCase(index.toString())) {
                                    minOccursString = inputNode.getAttributeValue("minOccurs");
                                    maxOccursString = inputNode.getAttributeValue("maxOccurs");
                                    break;
                                }
                            }
                            if (minOccursString != null && minOccursString.trim().matches("[0-9]+")) {
                                jsonEndpointRequired.put(index.toString(), minOccursString);
                            }
                            if (maxOccursString != null
                                    && (maxOccursString.trim().equalsIgnoreCase("unbounded")
                                        || maxOccursString.trim().matches("[0-9]+"))) {
                                jsonEndpointMaxInputs.put(index.toString(), maxOccursString);
                            }
                        }
                    }

                    // The *_list will contain index -> ref
                    if (xPathPattern != null && xPathPattern.equals(XML_INPUT_DEFS)) {
                        if (fileLabels != null && fileLabels.size() > 0) {
                            String newLabel = null;
                            for (Element fileLabel : fileLabels) {

                                if (fileLabel.getAttributeValue("name") != null
                                    && fileLabel.getAttributeValue("name").replaceAll("input", "").equalsIgnoreCase(indexSuffix)) {
                                    newLabel = fileLabel.getAttributeValue("default");
                                    break;
                                }
                            }
                            jsonEndpointIndex.put(index.toString(), newLabel);
                        } else {
                            jsonEndpointIndex.put(index.toString(), fileNodeElement.getAttributeValue("ref"));
                        }
                    } else if (xPathPattern != null && xPathPattern.equals(XML_OUTPUT_DEFS)) {
                        jsonEndpointIndex.put(index.toString(), fileNodeElement.getAttributeValue("ref"));
                    }

                }

                jsonEndpointObject.put(wfComponentItem.getComponentType().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "-"
                    + wfComponentItem.getComponentName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "_list",
                    jsonEndpointIndex);
                jsonEndpointObject.put(wfComponentItem.getComponentType().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "-"
                        + wfComponentItem.getComponentName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "_required",
                        jsonEndpointRequired);
                jsonEndpointObject.put(wfComponentItem.getComponentType().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "-"
                        + wfComponentItem.getComponentName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_") + "_maxinputs",
                        jsonEndpointMaxInputs);

                } catch (JSONException exception) {
                    staticLogger.error("Error getting component endpoints.");
                    return null;
                }
            }
        }
    }
    Boolean outputPatternTest = xPathPattern != null && xPathPattern.equals(XML_OUTPUT_DEFS);
    if (finalInputEndpoints == null && xPathPattern != null && xPathPattern.equals(XML_INPUT_DEFS)) {
        finalInputEndpoints = jsonEndpointObject;
    } else if (finalOutputEndpoints == null && outputPatternTest) {
        finalOutputEndpoints = jsonEndpointObject;
    }
    return jsonEndpointObject;
}

/**
 * Gets the XSD-defined endpoints for all components.
 * @param xPathPattern the XPath pattern to the input or output elements
 * @return the JSON string containing the component type, component name, and a list of inputs
 * @throws IOException
 * @throws JDOMException
 */
public String getEndpoints(String xPathPattern) throws JDOMException, IOException {
    staticLogger.trace("Getting component endpoint definitions: " + xPathPattern);

    // Find the component information in the database.
    WorkflowComponentDao workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();

    List<WorkflowComponentItem> workflowComponentItems = workflowComponentDao.findAll();

    JSONObject jsonEndpointObject = getEndpointsNoDao(workflowComponentItems, xPathPattern);
    staticLogger.trace("Fetched component endpoint definitions: " + jsonEndpointObject.toString());
    return jsonEndpointObject.toString().toLowerCase();
}

    /**
     * Processes all queued workflow components for a given workflow.
     * @param processInputMap the mapping of input elements to their destination components
     * @param workflowItem the workflow item
     * @param componentNodes the component nodes
     * @param executionFlag whether or not this workflow is being executed or just validated
     * @param isWorkflowSaved whether or not this workflow has been saved
     */
    private static Boolean createAdjacencyList(HashMap<String, List<String>> pInputMap,
            WorkflowItem workflowItem, List<Element> componentNodes) {

        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        //List<Integer> expectedInputs = null;
        /** Processed components. */
        List<String> tempProcessedList = new ArrayList<String>();
        Integer safetyCounter = 0;
        Integer dagDepthLevel = 0;
        // Process the components using the pInputMap which contains the key, val pairs { toNode, fromNodes }
        List<WorkflowComponentAdjacencyItem> wfcAdjItems = wfcAdjDao.findByWorkflow(workflowItem);
        if (wfcAdjItems != null && !wfcAdjItems.isEmpty()) {
            for (Iterator it1 = wfcAdjItems.iterator(); it1.hasNext(); ) {
                WorkflowComponentAdjacencyItem wfcAdjItem = (WorkflowComponentAdjacencyItem) it1.next();
                String oldComponentId = wfcAdjItem.getComponentId();
                wfcAdjDao.delete(wfcAdjItem); // mck2 on call to open options to restricted
            }
        }
        while (!pInputMap.isEmpty()) {

            if (safetyCounter++ > WorkflowHelper.MAX_EXECUTIONS) {
                return false;
            }

            List<String> componentQueue = new ArrayList<String>();
            staticLogger.trace("Process Input Map size: " + pInputMap.size());
            for (Iterator<Element> iterator = componentNodes.iterator(); iterator
                    .hasNext();) {
                Element componentNode = iterator.next();

                // Process this component node.
                String id = componentNode.getChildText("component_id");
                String type = componentNode.getChildText("component_type");
                String componentName = componentNode.getChildText("component_name")
                        .trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");

                // Process this component if all of its input nodes have
                // been processed.
                if (pInputMap.containsKey(id)
                        && pInputMap.get(id).isEmpty()) {
                    componentQueue.add(id);
                }
            }
            for (Iterator<Element> iterator = componentNodes.iterator(); iterator
                    .hasNext();) {
                Element componentNode = iterator.next();

                // Process this component node.
                String id = componentNode.getChildText("component_id");
                String type = componentNode.getChildText("component_type");
                String componentName = componentNode.getChildText("component_name");

                // Process this component if all of its input nodes have
                // been processed.
                if (pInputMap.containsKey(id)
                        && pInputMap.get(id).isEmpty()
                        && componentQueue.contains(id)) {


                    // Get the outputMapping from the input nodes.
                    Element outputMapping = null;
                    ComponentOutput componentOutput = null;

                    // This is a workflow saved in the database.

                        if (componentName != null) {
                            componentName = componentName;
                        }

                        if (!type.equalsIgnoreCase("import")) {
                            WorkflowComponentDao workflowComponentDao
                                = DaoFactory.DEFAULT.getWorkflowComponentDao();
                            WorkflowComponentItem workflowComponentItem =
                                workflowComponentDao.findByName(componentName);

                            if (workflowComponentItem != null
                                    && workflowComponentItem.getToolDir() != null) {
                                File wfComponentDir = new File(workflowComponentItem.getToolDir());
                                if (wfComponentDir != null && wfComponentDir.exists()) {
                                    String toolDirParent = wfComponentDir.getParent();
                                    String  tableTypesFile = WorkflowFileUtils.getStrictDirFormat(
                                        toolDirParent) + "/CommonSchemas/TableTypes.xsd";

                                   // expectedInputs = getConnectedInputsInfo(workflowId,
                                   //     workflowsDir, tableTypesFile, userItem, id, componentName, type);
                                }
                            }
                        } else {
                            //expectedInputs = new ArrayList<Integer>();
                        }


                    tempProcessedList.add(id);


                        Element thisConnectionsNode = (Element) componentNode.getChild("connections");
                        if (thisConnectionsNode != null && thisConnectionsNode.getChildren("connection") != null
                                && !thisConnectionsNode.getChildren("connection").isEmpty()) {

                            List<Element> thisConnections = thisConnectionsNode.getChildren("connection");
                            for (Iterator<Element> thisConnectionIterator = thisConnections
                                    .iterator(); thisConnectionIterator
                                    .hasNext();) {
                                Element thisConnection = thisConnectionIterator
                                        .next();

                                Element thisConnectionTo = thisConnection
                                        .getChild("to");
                                Element thisConnectionToIndex = thisConnection
                                        .getChild("index");
                                if (thisConnectionTo != null && thisConnectionToIndex != null) {
                                    Integer toNodeIndex = 0;
                                    Integer frindex = 0;
                                    String toComponentId = null;
                                    if (thisConnection != null) {
                                        if (thisConnection.getChild("frindex") != null
                                                && thisConnection.getChildTextTrim("frindex").matches("\\d+")) {
                                            frindex = Integer.parseInt(thisConnection.getChildTextTrim("frindex"));
                                        }
                                        if (thisConnection.getChild("index") != null
                                                && thisConnection.getChildTextTrim("index").matches("\\d+")) {
                                            // Get toNode index
                                            toNodeIndex = Integer.parseInt(thisConnection.getChildTextTrim("index"));
                                        }
                                        if (thisConnection.getChild("to") != null
                                                && thisConnection.getChildTextTrim("to").matches("[a-zA-Z0-9_\\-]+")) {
                                            // Get toNode index
                                            toComponentId = thisConnection.getChildTextTrim("to");
                                        }
                                    }

                                    // After processing a component, remove it
                                    // from the pInputMap nodes to which it connects,
                                    // and add it's inputs to the "inputMappingByComponentId" map.
                                    for (Iterator<Element> toIterator = componentNodes
                                            .iterator(); toIterator.hasNext();) {

                                        Element toComponentNode = toIterator.next();
                                        String toId = toComponentNode
                                                .getChildText("component_id");
                                        if (toId.equalsIgnoreCase(toComponentId)) {

                                            Element toNodeConnectionsNode = (Element) toComponentNode.getChild("connections");
                                            if (toNodeConnectionsNode != null && toNodeConnectionsNode.getChildren("connection") != null) {
                                                staticLogger.trace("Successors found.");
                                                List<Element> toNodeConnections = toNodeConnectionsNode.getChildren("connection");
                                                for (Iterator<Element> toNodeConnectionIterator = toNodeConnections
                                                        .iterator(); toNodeConnectionIterator
                                                        .hasNext();) {
                                                    Element toNodeConnection = toNodeConnectionIterator
                                                            .next();

                                                    Element toNodeConnectionFrom = toNodeConnection
                                                            .getChild("from");
                                                    Element toNodeConnectionFromIndex = toNodeConnection
                                                            .getChild("index");
                                                    Element toNodeConnectionToIndex = toNodeConnection
                                                            .getChild("tindex");

                                                    if (toNodeConnectionFrom != null) {
                                                        Integer fromNodeIndex = 0;
                                                        if (toNodeConnectionFromIndex != null
                                                                && toNodeConnection.getChildTextTrim("index").matches("\\d+")) {
                                                            // Get fromNode index
                                                            fromNodeIndex = Integer.parseInt(toNodeConnection.getChildTextTrim("index"));
                                                        }
                                                        if (toNodeConnectionFrom.getText()
                                                                .equalsIgnoreCase(id) && fromNodeIndex == frindex) {


                                                            WorkflowComponentAdjacencyItem wfcAdjItem =
                                                                wfcAdjDao.findUnique(workflowItem, id, frindex, toId, toNodeIndex);
                                                            if (wfcAdjItem == null) {
                                                                wfcAdjItem = new WorkflowComponentAdjacencyItem();
                                                                wfcAdjItem.setComponentId(id);
                                                            }

                                                            wfcAdjItem.setWorkflow(workflowItem);
                                                            wfcAdjItem.setComponentIndex(frindex);
                                                            wfcAdjItem.setChildId(toId);
                                                            wfcAdjItem.setChildIndex(toNodeIndex);
                                                            wfcAdjItem.setDepthLevel(dagDepthLevel);
                                                            wfcAdjDao.saveOrUpdate(wfcAdjItem);


                                                            // Remove this node from the
                                                            // list of nodes it connects to
                                                            // since it has been processed.
                                                            if (pInputMap.containsKey(toId)) {
                                                                List froms = pInputMap.get(toId);
                                                                froms.remove(id);
                                                                pInputMap.put(toId, froms);
                                                            }

                                                        }

                                                    }

                                                }

                                            }
                                        }
                                    }
                                    staticLogger.trace("Execution completed.");
                                } else {
                                    staticLogger.trace("There are no outgoing connections from component: " + id
                                            + ". Processing final output for this component.");

                                    // Remove this node from the
                                    // list of nodes it connects to
                                    // since it has been processed.
                                    List froms = pInputMap.get("ROOT");
                                    froms.remove(id);
                                    pInputMap
                                            .put("ROOT", froms);

                                    WorkflowComponentAdjacencyItem wfcAdjItem =
                                        wfcAdjDao.findUnique(workflowItem, id, null, null, null);
                                    if (wfcAdjItem == null) {
                                        wfcAdjItem = new WorkflowComponentAdjacencyItem();
                                        wfcAdjItem.setComponentId(id);
                                    }

                                    wfcAdjItem.setWorkflow(workflowItem);
                                    wfcAdjItem.setDepthLevel(dagDepthLevel);
                                    wfcAdjDao.saveOrUpdate(wfcAdjItem);
                                }
                            }
                        } else {
                            // Standalone imports
                            WorkflowComponentAdjacencyItem wfcAdjItem =
                                    wfcAdjDao.findUnique(workflowItem, id, null, null, null);
                                if (wfcAdjItem == null) {
                                    wfcAdjItem = new WorkflowComponentAdjacencyItem();
                                    wfcAdjItem.setComponentId(id);
                                }

                                wfcAdjItem.setWorkflow(workflowItem);
                                wfcAdjItem.setDepthLevel(dagDepthLevel);
                                wfcAdjDao.saveOrUpdate(wfcAdjItem);
                        }

                    // Remove this node from the list of nodes to be
                    // processed.
                    componentQueue.remove(id);
                    pInputMap.remove(id);
                }

            }


            if (pInputMap.containsKey("ROOT") && pInputMap.size() == 1) {
                pInputMap.remove("ROOT");
            }
            dagDepthLevel++;
        }   // end of while loop
        return true;
    }

    /**
     *
     * @param wfciDao
     * @param workflowItem
     * @param workflowXML
     * @param isWorkflowSaved
     * @return true if successful
     */
    public static Boolean createAdjList(WorkflowComponentInstanceDao wfciDao, WorkflowItem workflowItem, String workflowXML, String componentsDir) {
        Document digraphDom = null;

        ErrorMessageMap errorMessageMap = new ErrorMessageMap();
        try {

            FileUtils.createDirectoriesWithPermissions(Paths.get(componentsDir));

            staticLogger.info("Creating adjacency list for workflow (" + workflowItem.getId() + ")");

            //Boolean executionFlag = false;
            StringReader reader = null;

            if (workflowItem == null || workflowXML == null) {
                errorMessageMap.add("workflow", "Workflow Item or Worklfow XML found to be null.");
                return false;
            }

            Document doc = null;
            SAXBuilder builder = new SAXBuilder();
            // Setting reuse parser to false is a workaround
            // for a JDK 1.7u45 bug described in
            // https://community.oracle.com/thread/2594170
            builder.setReuseParser(false);

            try {
                staticLogger.debug("Reading workflow XML for testing.");

                reader = new StringReader(workflowXML.replaceAll("[\r\n]+", ""));
                doc = builder.build(reader);
                staticLogger.trace("Workflow Item (" + workflowItem.getNameAndId() + ") XML is well-formed.");

                readXmlAndUpdateAdjList(wfciDao, doc, workflowItem);


            } catch (JDOMException e) {
                errorMessageMap.add("workflow", "Workflow Item XML is not well-formed.");
            } catch (IOException e) {
                errorMessageMap.add("workflow", "Workflow Item: Reader could not be instantiated.");
            }
            staticLogger.info("Created adjacency list for workflow (" + workflowItem.getId().toString() + ")");

        } catch (IOException e) {
            staticLogger.error("Could not create required directory: " + componentsDir);
            errorMessageMap.add(null, e.toString());
        }
        return true;
    }

    /**
     * Read the component XML and update the adjacency list.
     * @param doc the JDOM document
     * @param workflowItem the optional workflowItem element
     * @param executionFlag whether this is a workflow execution (true) or validity test (false)
     * @param isWorkflowSaved
     * @return true if successful
     * @throws JDOMException
     */
    private static Boolean readXmlAndUpdateAdjList(WorkflowComponentInstanceDao wfciDao, Document doc, WorkflowItem workflowItem)  {

        ErrorMessageMap errorMessageMap = new ErrorMessageMap();
        HashMap<String, List<String>> processInputMap = new HashMap<String, List<String>>();

        if (doc == null || doc.getRootElement() == null || !doc.getRootElement().getName().equalsIgnoreCase("workflow")) {
            return false;
        }
        // Parse XML from document
        String xmlWorkflowIdString = null;
        Element root = doc.getRootElement();

        // Get the root element, <workflow>
        if (root.getName().equals("workflow")) {

            xmlWorkflowIdString = root.getChildText("id").trim();
            Long xmlWorkflowId = null;
            if (xmlWorkflowIdString != null && xmlWorkflowIdString.matches("\\d+")) {
                xmlWorkflowId = Long.parseLong(xmlWorkflowIdString);
            }
            if (xmlWorkflowId != null && xmlWorkflowId.equals((Long)workflowItem.getId())) {
                staticLogger.trace("Workflow Ids match: " + workflowItem.getId().toString());

                // In case there are no connections between components, we use a fake element, call it ROOT
                processInputMap.put("ROOT", new ArrayList<String>());
                Element components = root.getChild("components");
                List<Element> componentNodes = null;
                // This workflow has components
                if (components != null && components.getChildren("component") != null) {
                    staticLogger.trace("Workflow components found.");
                    componentNodes = components.getChildren("component");
                    if (componentNodes != null && !componentNodes.isEmpty()) {
                        // Populate the processInputMap to keep track of node connections (edges).
                        for (Element componentNode : componentNodes) {
                            String id = componentNode.getChildText("component_id");
                            String type = componentNode.getChildText("component_type");
                            staticLogger.trace("Component found: " + id + ", " + type);

                            Element connectionsNode = (Element) componentNode.getChild("connections");
                            List<Element> connectionNodes = null;
                            if (connectionsNode != null) {

                                connectionNodes = (List<Element>) connectionsNode
                                    .getChildren("connection");
                            }
                            List<Element> fromNodes = new ArrayList<Element>();

                            if (connectionNodes != null && !connectionNodes.isEmpty()) {
                                for (Element connectionNode : connectionNodes) {
                                    if (connectionNode != null && connectionNode.getChildren("from") != null) {
                                        fromNodes.addAll(connectionNode
                                                .getChildren("from"));
                                    }
                                }
                            }

                            // Add this node's id and an empty list to the processInputMap.
                            processInputMap.put(id, new ArrayList<String>());

                            // Keep track of incoming connections (froms) to this node.
                            WorkflowComponentInstanceItem toInstance
                                = wfciDao.findIncompleteByWorkflowAndId(workflowItem, id);
                            staticLogger.trace("To component instance: " + id);
                            // If this node is dirty or a predecessor is dirty, then process it
                            if (toInstance != null
                                    || (fromNodes != null && !fromNodes.isEmpty())) {

                                if (fromNodes != null) {
                                    for (Iterator<Element> connectionIterator = fromNodes
                                            .iterator(); connectionIterator.hasNext();) {

                                        Element from = connectionIterator.next();
                                        String fromId = from.getText().trim();

                                        List<String> fromIds = processInputMap.get(id);
                                        if (!fromIds.contains(fromId)) {
                                            fromIds.add(fromId);
                                        }
                                        processInputMap.put(id, fromIds);
                                        staticLogger.trace("processInputMap:: put " + id
                                                + ", " + type + ", from: "
                                                + Arrays.toString(fromIds.toArray()));

                                    }

                                }

                            } else {
                                staticLogger.trace("For component " + id + " no 'from' connections were found.");
                            }
                        }   // end of for each component node
                    }
                    Boolean adjMatSuccess = createAdjacencyList(processInputMap, workflowItem, componentNodes);
                    if (!adjMatSuccess) {
                        String err = "Component adjacency matrix failed to update.";
                        staticLogger.error(err);
                        errorMessageMap.add("workflow", err);
                    }

                }   // end of if components exist

            } else {
                String err = "Workflow XML must contain a valid workflow Id for execution.";
                staticLogger.error(err);
                errorMessageMap.add("workflow", err);
            }
        } else {
            String err = "Workflows must contain the root element: <workflow>.";
                staticLogger.error(err);
                errorMessageMap.add("workflow", err);

        }

        staticLogger.trace("Completed parsing of workflow.");
        return true;
    }

    /**
      * Update the workflow_component_adjacency table.
      * @param baseDir the base dir
      * @param workflowItem the workflow item
      * @param loggedInUserItem the logged-in user item
      */
     public static synchronized List<WorkflowComponentAdjacencyItem> updateAdjacencyList(
             String baseDir, WorkflowItem workflowItem, UserItem loggedInUserItem) {
         WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();

         Workflow adjBackwardsCompat = new Workflow();
         adjBackwardsCompat.init(WorkflowFileUtils.getStrictDirFormat(baseDir),
                 WorkflowFileUtils.getWorkflowsDir(workflowItem.getId()), loggedInUserItem);
         WorkflowComponentInstanceDao wfciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
         String componentsDir = baseDir + "/workflows/" + workflowItem.getId().toString() + "/components/";
         ConnectionHelper.createAdjList(wfciDao, workflowItem, workflowItem.getWorkflowXml(), componentsDir);
         List<WorkflowComponentAdjacencyItem> wfcAdjItems = wfcAdjDao.findByWorkflow(workflowItem);
         return wfcAdjItems;
     }

}
