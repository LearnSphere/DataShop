/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;
import edu.cmu.pslc.datashop.dao.UserDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentAdjacencyDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstanceDao;
import edu.cmu.pslc.datashop.dao.WorkflowComponentInstancePersistenceDao;
import edu.cmu.pslc.datashop.dao.WorkflowDao;
import edu.cmu.pslc.datashop.dao.WorkflowFileDao;
import edu.cmu.pslc.datashop.dao.ComponentFileDao;
import edu.cmu.pslc.datashop.dao.ComponentFilePersistenceDao;
import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentOutput;
import edu.cmu.pslc.datashop.servlet.workflows.ConnectionHelper;
import edu.cmu.pslc.datashop.servlet.workflows.MachineManager;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileUtils;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowImportHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowXmlUtils;
import edu.cmu.pslc.datashop.util.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the workflow execution thread.
 * @author mkomisin
 *
 */
public class Workflow  extends AbstractExtractor {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Base directory for DataShop files. */
    private String baseDir;

    /** Specific user directory within the workflow directory. */
    private String workflowsDir;

    String componentsDir;

    Map<Integer, String> componentFileMap;
    Integer componentFileCounter;

    /** The user associated with this workflow. */
    UserItem userItem;

    /** Result message. */
    ConcurrentLinkedQueue<String> resultsMessage;

    /** Processed components. */
    ConcurrentLinkedQueue<String> processedList;

    /** The producer/consumer threads. */
    List<Thread> producerConsumerThreads;
    /** The component threads. */
    ConcurrentLinkedQueue<Thread> componentQueueThreads;
    /** The components being processed right now. */
    ConcurrentLinkedQueue<String> componentQueue;

    // Create a hashmap to store the node connections (edges).
    // HashMap<ComponentId, Incoming Component Ids> of process.
    HashMap<String, List<String>> processInputMap;

    Boolean workflowCompleted;
    /** Error messages. */
    private ErrorMessageMap errorMessageMap;
    /** Info and Warn messages. */
    private ErrorMessageMap genericMessageMap;

    /** A mapping of input node id's to outputs of other nodes. */
    private ConcurrentHashMap<String, List<Element>> inputMappingByComponentId = null;
    private ConcurrentHashMap<String, ComponentOutput> outputMappingByComponentId = null;

    /** The workflow Id for the current workflow execution. */
    private Long workflowId;

    private Boolean workflowHasErrors = false;
    private Boolean workflowHasMajorErrors = false;
    private WorkflowFileDao wfFileDao;
    private WorkflowDao workflowDao;
    private WorkflowHelper workflowHelper;
    private WorkflowImportHelper workflowImportHelper;
    private WorkflowComponentInstanceDao wfciDao;
    private WorkflowComponentDao workflowComponentDao;
    private UserDao userDao;

    private final static long WORKFLOW_THREAD_SLEEP = 100L;

    /** DataShop instance id. */
    private static final Long DATASHOP_INSTANCE_ID = 1L;
    private static final Integer MAX_WARNING_LINES = 8;
    /**
     * Default constructor for a workflow process.
     */
    public Workflow() {
        inputMappingByComponentId = new ConcurrentHashMap<String, List<Element>>();
        outputMappingByComponentId = new ConcurrentHashMap<String, ComponentOutput>();
        errorMessageMap = new ErrorMessageMap();
        workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
        workflowImportHelper = HelperFactory.DEFAULT.getWorkflowImportHelper();
        wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
        wfciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
        userDao = DaoFactory.DEFAULT.getUserDao();
        resultsMessage = new ConcurrentLinkedQueue<String>();
        processedList = new ConcurrentLinkedQueue<String>();
        componentQueue = new ConcurrentLinkedQueue<String>();
        processInputMap = new HashMap<String, List<String>>();
        componentQueueThreads = new ConcurrentLinkedQueue<Thread>();
        producerConsumerThreads = new ArrayList<Thread>();
        workflowCompleted = false;
    }

    /**
     * Constructor for a workflow process which requires an existing workflowId.
     *
     * @param workflowId
     *            the existing workflowId
     */
    public Workflow(Long workflowId) {
        this.workflowId = workflowId;
        inputMappingByComponentId = new ConcurrentHashMap<String, List<Element>>();
        outputMappingByComponentId = new ConcurrentHashMap<String, ComponentOutput>();
        errorMessageMap = new ErrorMessageMap();
        genericMessageMap = new ErrorMessageMap();
        workflowHelper = HelperFactory.DEFAULT.getWorkflowHelper();
        workflowImportHelper = HelperFactory.DEFAULT.getWorkflowImportHelper();
        wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        workflowDao = DaoFactory.DEFAULT.getWorkflowDao();
        workflowComponentDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
        wfciDao = DaoFactory.DEFAULT.getWorkflowComponentInstanceDao();
        userDao = DaoFactory.DEFAULT.getUserDao();
        resultsMessage = new ConcurrentLinkedQueue<String>();
        processedList = new ConcurrentLinkedQueue<String>();
        componentQueue = new ConcurrentLinkedQueue<String>();
        processInputMap = new HashMap<String, List<String>>();
        componentQueueThreads = new ConcurrentLinkedQueue<Thread>();
        producerConsumerThreads = new ArrayList<Thread>();
        workflowCompleted = false;
    }

    /**
     * Initialize the workflow extractor.
     * @param the base directory
     * @param workflowsDir the workflows dir
     * @param userItem the user item
     */
    public void init(String baseDir, String workflowsDir, UserItem userItem) {
        this.baseDir = WorkflowFileUtils.getStrictDirFormat(baseDir);
        this.workflowsDir = workflowsDir;
        this.userItem = userItem;

    }

    /**
     * The run method executes the workflow extractor after it has been initialized.
     */
    public void run() {

        try {
            this.componentsDir = baseDir + "/workflows/" + workflowId + "/components/";
            FileUtils.createDirectoriesWithPermissions(Paths.get(componentsDir));

            logger.info("Processing workflow (" + workflowId + ")");
            executeWorkflow(workflowId);
            logger.info("Finished processing workflow (" + workflowId + ")");

        } catch (IOException e) {
            logger.error("Could not create required directory: " + componentsDir);
        }

    }

    public ErrorMessageMap test(WorkflowItem workflowItem, String workflowXML, Boolean isWorkflowSaved, Boolean isSaveAsNew) {
        // Java complains this isn't used, but in fact it is used to generated an error report.
        Document digraphDom = null;

        try {
            this.componentsDir = baseDir + "/workflows/" + workflowId + "/components/";
            FileUtils.createDirectoriesWithPermissions(Paths.get(componentsDir));

            logger.info("Validating workflow (" + workflowId + ")");
            validateWorkflow(workflowItem, workflowXML, isWorkflowSaved, isSaveAsNew);
            logger.info("Validated workflow (" + workflowId + ")");

        } catch (IOException e) {
            logger.error("Could not create required directory: " + componentsDir);
            errorMessageMap.add(null, e.toString());
        }

        return errorMessageMap;
    }

    /**
     * Parse the workflow XML file and validate against the XML schema definition.
     * @param workflowItem the workflow item
     * @param workflowXML the workflow XML
     * @param isWorkflowSaved whether or not the workflow is saved
     */
    public void validateWorkflow(WorkflowItem workflowItem, String workflowXML,
            Boolean isWorkflowSaved, Boolean isSaveAsNew) {

        // Don't execute; simply validate.
        Boolean executionFlag = false;
        StringReader reader = null;

        if (workflowItem == null || workflowXML == null) {
            errorMessageMap.add("workflow",
                "Workflow Item or Worklfow XML found to be null.");
            return;
        }

        Document doc = null;
        SAXBuilder builder = new SAXBuilder();
        // Setting reuse parser to false is a workaround
        // for a JDK 1.7u45 bug described in
        // https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {
            logger.debug("Reading workflow XML for testing.");

            reader = new StringReader(workflowXML.replaceAll("[\r\n]+", ""));
            doc = builder.build(reader);
            logger.trace("Workflow Item (" + workflowItem.getNameAndId() + ") XML is well-formed.");
            logger.trace("Workflow flags executionFlag = " + executionFlag + ", isWorkflowSaved = " + isWorkflowSaved);

            // Parse the workflow
            if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                parseWorkflowXml(doc, workflowItem, executionFlag, isWorkflowSaved, isSaveAsNew);
            }

        } catch (JDOMException e) {
            errorMessageMap.add("workflow", "Workflow Item XML is not well-formed.");
        } catch (IOException e) {
            errorMessageMap
                    .add("workflow", "Workflow Item: Reader could not be instantiated.");
        }
    }

    /**
     * Parse the workflow XML file and process its instructions.
     * @param workflowId the workflow id
     */
    public void executeWorkflow(Long workflowId) {
        if (workflowId == null) {
            return;
        }
        Boolean isSaveAsNew = false;
        // Get the workflowItem given the workflow id
        WorkflowItem workflowItem = workflowDao.get(workflowId);

        if (workflowItem == null || workflowItem.getWorkflowXml() == null) {
            errorMessageMap.add("workflow",
                "Workflow Item or Worklfow XML found to be null.");
            return;
        }

        // Execute the workflow if it is dirty.
        Boolean executionFlag = true;
        Boolean isWorkflowSaved = true;
        StringReader reader = null;
        Document doc = null;
        SAXBuilder builder = new SAXBuilder();
        // Setting reuse parser to false is a workaround
        // for a JDK 1.7u45 bug described in
        // https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {
            logger.debug("Reading workflow XML for workflow (" + workflowId
                    + ")");
            reader = new StringReader(workflowItem.getWorkflowXml().replaceAll(
                    "[\r\n]+", ""));
            doc = builder.build(reader);
            logger.trace("Workflow Item (" + workflowItem.getNameAndId() + ") XML is well-formed.");
            logger.trace("Workflow flags executionFlag = " + executionFlag + ", isWorkflowSaved = " + isWorkflowSaved);

            // Parse the workflow
            if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                parseWorkflowXml(doc, workflowItem, executionFlag, isWorkflowSaved, isSaveAsNew);
            }

        } catch (JDOMException e) {
            errorMessageMap
                    .add("workflow", "Workflow Item (" + workflowItem.getNameAndId()
                            + ") XML is not well-formed.");
        } catch (IOException e) {
            errorMessageMap.add("workflow", "Workflow Item ("
                    + workflowItem.getNameAndId()
                    + ") Reader could not be instantiated.");
        }
    }

    /** Return the map of components to errors.
     * @return the errorMessageMap
     */
    public ErrorMessageMap getErrorMessageMap() {
        return errorMessageMap;
    }

    public ErrorMessageMap getGenericMessageMap() {
        return genericMessageMap;
    }

    /**
     * Parse and process the workflow item.
     * @param doc the JDOM document
     * @param workflowItem the optional workflowItem element
     * @param executionFlag whether this is a workflow execution (true) or validity test (false)
     * @param isWorkflowSaved
     * @throws JDOMException
     */
    private void parseWorkflowXml(Document doc, WorkflowItem workflowItem,
            Boolean executionFlag, Boolean isWorkflowSaved, Boolean isSaveAsNew)  {
        if (doc == null || doc.getRootElement() == null || !doc.getRootElement().getName().equalsIgnoreCase("workflow")) {
            String err = "The document does not contain a workflow object.";
            logger.error(err);
            errorMessageMap.add("workflow", err);
            return;
        }
        // Parse XML from document
        String xmlWorkflowIdString = null;
        Element root = doc.getRootElement();

        // Set instance state to running
        if (workflowItem != null && executionFlag
                && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
            workflowItem.setState(WorkflowItem.WF_STATE_RUNNING);
            workflowDao.saveOrUpdate(workflowItem);
        }
        Long heapSize = AbstractComponent.DEFAULT_INITIAL_HEAP_SIZE;
        String remoteServer = null;
        DataShopInstanceDao dao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        DataShopInstanceItem dsInstanceItem = dao.get(DATASHOP_INSTANCE_ID);
        if (dsInstanceItem != null) {
            if (dsInstanceItem.getWfcHeapSize() != null && dsInstanceItem.getWfcHeapSize() > 0) {
                heapSize = dsInstanceItem.getWfcHeapSize();
            } else {
                heapSize = AbstractComponent.DEFAULT_INITIAL_HEAP_SIZE;
            }
            remoteServer = dsInstanceItem.getWfcRemote();
            if (remoteServer != null && !remoteServer.trim().isEmpty()) {
                // Ensure that the remote workflows server is a valid URL.
                try {
                    URL url = new URL(remoteServer);
                } catch (MalformedURLException e) {
                    logger.error("Cannot parse remote workflows server URL: " + e.toString());
                    remoteServer = null;
                }
            } else {
                // No remote workflows server set.
                remoteServer = null;
            }
        }

        // Get the root element, <workflow>
        if (root.getName().equals("workflow")) {

            xmlWorkflowIdString = root.getChildText("id").trim();
            Long xmlWorkflowId = null;
            if (xmlWorkflowIdString != null && xmlWorkflowIdString.matches("\\d+")) {
                xmlWorkflowId = Long.parseLong(xmlWorkflowIdString);
            }
            if (xmlWorkflowId != null && xmlWorkflowId.equals((Long)workflowItem.getId())) {
                logger.trace("Workflow Ids match: " + workflowId);


                // In case there are no connections between components, we use a fake element, call it ROOT
                processInputMap.put("ROOT", new ArrayList<String>());
                Element components = root.getChild("components");
                List<Element> componentNodes = null;
                // This workflow has components
                if (components != null && components.getChildren("component") != null) {
                    logger.trace("Workflow components found.");
                    componentNodes = components.getChildren("component");

                    // Load processing queue
                    if (componentNodes != null && !componentNodes.isEmpty()) {
                        // Populate the processInputMap to keep track of node connections (edges).
                        for (Element componentNode : componentNodes) {
                            String id = componentNode.getChildText("component_id");
                            String type = componentNode.getChildText("component_type");
                            logger.trace("Component found: " + id + ", " + type);

                            // Add this node's id and an empty list to the processInputMap.
                            processInputMap.put(id, new ArrayList<String>());

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

                            // Keep track of incoming connections (froms) to this node.
                            WorkflowComponentInstanceItem toInstance = wfciDao.findByWorkflowAndId(workflowItem, id);

                            logger.trace("To component instance: " + id);
                            // If this node is dirty or a predecessor is dirty, then
                            // process it
                            if ((toInstance != null
                                    && !toInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                    && !toInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN))
                                    || (fromNodes != null && !fromNodes.isEmpty())) {

                                if (fromNodes != null) {
                                    for (Iterator<Element> connectionIterator = fromNodes.iterator(); connectionIterator
                                            .hasNext();) {

                                        Element from = connectionIterator.next();
                                        String fromId = from.getText().trim();

                                        WorkflowComponentInstanceItem fromInstance = wfciDao.findIncompleteByWorkflowAndId(workflowItem,
                                                fromId);


                                        List<String> fromIds = processInputMap.get(id);
                                        if (!fromIds.contains(fromId)) {
                                            fromIds.add(fromId);
                                        }

                                        processInputMap.put(id, fromIds);
                                        logger.trace("processInputMap:: put " + id + ", " + type + ", from: "
                                                + Arrays.toString(fromIds.toArray()));

                                    }

                                }


                            } else {
                                logger.trace("For component " + id + " no 'from' connections were found.");
                            }

                        }   // end of for each component node
                    } // end of loading up of processing queue

                    // Now, process dirty components.
                    if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                        workflowCompleted = false;

                        ProducerThread myComponentQueue = new ProducerThread();
                        myComponentQueue.init(workflowItem, executionFlag, isWorkflowSaved, componentNodes, isSaveAsNew, heapSize, remoteServer);

                        ConsumerThread myResultsQueue = new ConsumerThread();
                        myResultsQueue.init(workflowItem.getId(), executionFlag, isWorkflowSaved, componentNodes, isSaveAsNew, heapSize);

                        // The producer/consumer threads for this level of the DAG
                        producerConsumerThreads.clear();

                        Thread t1 = new Thread(myComponentQueue);
                        t1.start();
                        producerConsumerThreads.add(t1);


                        Thread t2 = new Thread(myResultsQueue);
                        t2.start();
                        producerConsumerThreads.add(t2);


                        for (Thread t : producerConsumerThreads) {
                            try {
                                t.join();
                            } catch (InterruptedException e) {
                                logger.error("Exception while running component in thread: " + e.toString());
                            } catch (Exception e) {
                                logger.error("Exception while running component in thread: " + e.toString());
                            }
                        }

                    }

                }   // end of if components exist


                if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                    if (isWorkflowSaved != null && isWorkflowSaved && !workflowHasErrors && errorMessageMap.isEmpty()) {
                        if (workflowItem != null) {
                            if (executionFlag) {
                                workflowItem.setState(WorkflowItem.WF_STATE_SUCCESS);
                            }
                        }
                    } else if (isWorkflowSaved != null && isWorkflowSaved) {
                        workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                    }
                    workflowDao.saveOrUpdate(workflowItem);
                }

            } else {
                String err = "Workflow XML must contain a valid workflow Id for execution.";
                    logger.error(err);
                    errorMessageMap.add("workflow", err);
            }
        } else {
            String err = "Workflows must contain the root element: <workflow>.";
                logger.error(err);
                errorMessageMap.add("workflow", err);

        }

        if (workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
            logger.trace("Workflow process haulted. Resetting state.");
            workflowItem.setState(WorkflowItem.WF_STATE_NEW);
            workflowDao.saveOrUpdate(workflowItem);
        } else {
            logger.trace("Completed parsing of workflow.");
        }
    }

    private void parseOutput(Element outputMapping, Element componentNode, List<Element> componentNodes,
            String componentId, boolean executionFlag) {
        /*try {
            logger.trace("Component output: "
                    + ComponentXmlHandler.getElementAsString(outputMapping));
        } catch (IOException e) {
            logger.error("Exception while converting outputMapping to string.");
        }*/



        // If a component connects TO any components, remove it
        // from the processInputMap nodes to which it connects,
        // and add it's inputs to the "inputMappingByComponentId" map.
        // Otherwise, there are no outgoing connections from component and its outputs
        // will be placed in the "ROOT" element of inputMappingByComponentId before its
        // removed from the processInputMap
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
                    if (thisConnectionToIndex != null
                            && thisConnection.getChildTextTrim("index").matches("\\d+")
                            && thisConnection.getChildTextTrim("frindex").matches("\\d+")) {
                        // Get toNode index
                        toNodeIndex = Integer.parseInt(thisConnection.getChildTextTrim("index"));
                        toComponentId = thisConnection.getChildTextTrim("to");
                        frindex = Integer.parseInt(thisConnection.getChildTextTrim("frindex"));
                    }

                    for (Iterator<Element> toIterator = componentNodes
                            .iterator(); toIterator.hasNext();) {

                        Element toComponentNode = toIterator.next();
                        String toId = toComponentNode
                                .getChildText("component_id");
                        if (toId.equalsIgnoreCase(toComponentId)) {

                            Element toNodeConnectionsNode = (Element) toComponentNode.getChild("connections");
                            if (toNodeConnectionsNode != null && toNodeConnectionsNode.getChildren("connection") != null) {
                                logger.trace("Successors found.");
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
                                                && toNodeConnectionFromIndex.getTextTrim().matches("\\d+")) {
                                            // Get fromNode index
                                            fromNodeIndex = Integer.parseInt(toNodeConnectionFromIndex.getTextTrim());
                                        }
                                        if (toNodeConnectionFrom.getText()
                                                .equalsIgnoreCase(componentId) && fromNodeIndex == frindex) {

                                            // Update mappings
                                            List inputs = null;
                                            if (inputMappingByComponentId
                                                    .containsKey(toId)) {
                                                inputs = inputMappingByComponentId
                                                        .get(toId);
                                            } else {
                                                inputs = new ArrayList<Element>();
                                            }

                                            if (outputMapping != null
                                                && outputMapping.getChildren() != null) {
                                                // Order matters here since inputs should match their connection indices
                                                for (Element output : (List<Element>)
                                                        outputMapping.getChildren()) {

                                                    Element cloneChild = (Element) output.clone();

                                                    // Only add desired input files
                                                    if (cloneChild.getName().matches("[a-zA-Z]+[0-9]+")) {
                                                        Integer clonedIndex = Integer.parseInt(
                                                                cloneChild.getName().replaceAll("[a-zA-Z]+", ""));
                                                        // Does the successor's from index match this output's
                                                        // index (clonedIndex)
                                                        if (clonedIndex == fromNodeIndex) {
                                                            cloneChild.setName("input" + toNodeIndex);
                                                            inputs.add(cloneChild);
                                                        }
                                                    }

                                                }
                                            }
                                            logger.trace("inputMappingByComponentId:: fromId = " + componentId + ", toId = " + toId
                                                    + ", inputs size = " + inputs.size());

                                            inputMappingByComponentId.put(toId, inputs);

                                            // Remove this node from the
                                            // list of nodes it connects to
                                            // since it has been processed.
                                            if (processInputMap.containsKey(toId)) {
                                                List<String> newFromList = new ArrayList<String>();
                                                List<String> froms = Collections.synchronizedList(processInputMap.get(toId));

                                                for (String fromId : froms) {
                                                    if (!fromId.equalsIgnoreCase(componentId)) {
                                                        newFromList.add(fromId);
                                                    }
                                                }
                                                processInputMap.put(toId, newFromList);
                                            }

                                        }

                                    }

                                }

                            }
                        }
                    }
                    logger.trace("Execution completed.");
                } else {
                    logger.trace("There are no outgoing connections from component: " + componentId
                            + ". Processing final output for this component.");

                    // Update mappings
                    List inputs = null;
                    if (inputMappingByComponentId
                            .containsKey("ROOT")) {
                        inputs = inputMappingByComponentId
                                .get("ROOT");
                    } else {
                        inputs = new ArrayList<Element>();
                    }

                    if (outputMapping != null
                        && outputMapping.getChildren() != null) {
                        for (Element output : (List<Element>) outputMapping.getChildren()) {
                            // order doesn't matter here since we're adding output to pseudo-element, "ROOT"
                            Element cloneChild = null;
                            // Only add desired input files

                            if (output.getName().matches("[a-zA-Z]+[0-9]+")) {
                                cloneChild = (Element)output.clone();
                                Integer clonedIndex = Integer.parseInt(
                                    cloneChild.getName().replaceAll("[a-zA-Z]+", ""));
                                // Does the successor's from index match this output's
                                // index (clonedIndex)
                            }
                            if (cloneChild != null) {
                                cloneChild.setName(cloneChild.getName().replaceAll("output", "input"));
                                inputs.add(cloneChild);
                            }
                        }
                    }
                    logger.trace("inputMappingByComponentId:: fromId = " + componentId + ", toId = " + "ROOT"
                            + ", inputs size = " + inputs.size());
                    inputMappingByComponentId.put(
                            "ROOT", inputs);

                    // Remove this node from the
                    // list of nodes it connects to
                    // since it has been processed.
                    List froms = processInputMap.get("ROOT");
                    froms.remove(componentId);
                    processInputMap.put("ROOT", froms);
                }
            }
        } else {
            // Standalone imports (not doing anything special as of now)
        }

    }

    /**
     * Processes all queued workflow components for a given workflow.
     *
     * @param processInputMap
     *            the mapping of input elements to their destination components
     * @param workflowItem
     *            the workflow item
     * @param componentNodes
     *            the component nodes
     * @param executionFlag
     *            whether or not this workflow is being executed or just
     *            validated
     * @param isWorkflowSaved
     *            whether or not this workflow has been saved
     */
    public void processComponentsProducer(WorkflowItem workflowItem,
            List<Element> componentNodes, Boolean executionFlag, Boolean isWorkflowSaved, Boolean isSaveAsNew, Long heapSize, String remoteServer) {

        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        List<ComponentNode> expectedInputs = null;
        logger.info("Executing workflow " + workflowItem.getWorkflowName() + " (" + workflowItem.getId() + ")");
        // Process the components using the processInputMap which contains the
        // key, val pairs { toNode, fromNodes }
        while (!workflowCompleted) {
            if (!processInputMap.isEmpty()) {



                logger.trace("Process Input Map size: " + processInputMap.size());
                for (Iterator<Element> iterator = componentNodes.iterator(); iterator.hasNext();) {
                    Element componentNode = iterator.next();

                    // Process this component node.
                    String id = componentNode.getChildText("component_id");
                    String type = componentNode.getChildText("component_type");
                    String componentName = componentNode.getChildText("component_name");

                    // Process this component if all of its input nodes have
                    // been processed.
                    if (id != null && processInputMap.containsKey(id) && processInputMap.get(id).isEmpty()
                            && !componentQueue.contains(id)) {
                        componentQueue.add(id);
                    }
                }

                // 1
                for (Iterator<Element> iterator = componentNodes.iterator(); iterator.hasNext();) {
                    Element componentNode = iterator.next();

                    // Process this component node.
                    String id = componentNode.getChildText("component_id");
                    String type = componentNode.getChildText("component_type");
                    String componentName = componentNode.getChildText("component_name");
                    String humanId = componentNode.getChildText("component_id_human");

                    // Process this component if all of its input nodes have
                    // been processed.
                    if (processInputMap.containsKey(id) && processInputMap.get(id).isEmpty()
                            && componentQueue.contains(id)) {

                        if (processedList.contains(id)
                                && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                            //String errorMessage = "Stopping workflow due to loop-back.";
                            //errorMessageMap.add(null, errorMessage);
                            //workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                            //workflowDao.saveOrUpdate(workflowItem);
                            continue;
                        }
                        logger.trace("Processing component. Component Id: " + id + ", type:" + type + ", workflow id: "
                                + workflowId + ", workflowItem null? " + workflowItem.getId());
                        /// for processcomponent, ///
                        /// outputMapping
                        /// /////////////////////////////////////////////////
                        /// is critical! ///
                        /////////////////////////////

                        // Get the outputMapping from the input nodes.
                        Element outputMapping = null;
                        ComponentOutput componentOutput = null;

                        // Get instance from queue, if one exists
                        WorkflowComponentInstanceItem thisComponentInstance = null;
                        // This is a workflow saved in the database.
                        if (workflowItem != null && workflowItem.getId() != null) {
                            thisComponentInstance = wfciDao.findByWorkflowAndId(workflowItem, id);
                            if (componentName != null) {
                                componentName = componentName.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                            }

                            if (!type.equalsIgnoreCase("import") && !componentName.equalsIgnoreCase("import")) {
                                WorkflowComponentItem workflowComponentItem = workflowComponentDao
                                        .findByName(componentName);

                                if (workflowComponentItem != null && workflowComponentItem.getToolDir() != null) {
                                    File wfComponentDir = new File(workflowComponentItem.getToolDir());
                                    if (wfComponentDir != null && wfComponentDir.exists()) {
                                        String toolDirParent = wfComponentDir.getParent();
                                        String tableTypesFile = WorkflowFileUtils.getStrictDirFormat(toolDirParent)
                                                + "/CommonSchemas/TableTypes.xsd";

                                        expectedInputs = ConnectionHelper.getConnectedInputsInfo(
                                            workflowId, workflowsDir, tableTypesFile,
                                                userItem, id, componentName, type);
                                    }
                                }
                            } else {
                                expectedInputs = new ArrayList<ComponentNode>();
                            }



                        } else if (executionFlag) {
                            // !!!This should never, ever happen since we can only
                            // execute workflows that are in the database, but let's
                            // be sure.!!!
                            logger.error("Component instance " + id + " has null workflow.");
                            errorMessageMap.add(id, humanId, "Null workflow returned during execution.");
                            processInputMap.clear();
                            return;
                        }

                        Element connectionsNode = (Element) componentNode.getChild("connections");
                        List<Element> connectionNodes = null;
                        if (connectionsNode != null) {

                            connectionNodes = (List<Element>) connectionsNode.getChildren("connection");
                        }
                        List<Element> fromNodes = new ArrayList<Element>();

                        if (connectionNodes != null && !connectionNodes.isEmpty()) {
                            for (Element connectionNode : connectionNodes) {
                                if (connectionNode != null && connectionNode.getChildren("from") != null) {
                                    fromNodes.addAll(connectionNode.getChildren("from"));
                                }
                            }
                        }

                        // Keep track of incoming connections (froms) to this node.
                        WorkflowComponentInstanceItem toInstance = wfciDao.findByWorkflowAndId(workflowItem, id);

                        logger.trace("To component instance: " + id);
                        // If this node is dirty or a predecessor is dirty, then
                        // process it
                        if (toInstance != null || (fromNodes != null && !fromNodes.isEmpty())) {
                            Boolean hasDirtyPredecessor = false;
                            Boolean predecessorHasDirtySelection = false;
                            if (fromNodes != null) {
                                for (Iterator<Element> connectionIterator = fromNodes.iterator(); connectionIterator
                                        .hasNext();) {

                                    Element from = connectionIterator.next();
                                    String fromId = from.getText().trim();

                                    WorkflowComponentInstanceItem fromInstance = wfciDao.findByWorkflowAndId(workflowItem,
                                            fromId);

                                    List<String> fromIds = processInputMap.get(id);

                                    if (!processedList.contains(fromId)) {
                                        if (!fromIds.contains(fromId)) {
                                            fromIds.add(fromId);
                                        }
                                        processInputMap.put(id, fromIds);
                                        logger.trace("processInputMap:: put " + id + ", " + type + ", from: "
                                                + Arrays.toString(fromIds.toArray()));
                                    }

                                    if (fromInstance != null) {
                                        if (fromInstance.isDirty()) {
                                            logger.trace("Has predecessor with any changed option.");
                                            hasDirtyPredecessor = true;
                                        }

                                        if (fromInstance.getDirtySelection()
                                                && !fromInstance.getState()
                                                    .equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                                && !fromInstance.getState()
                                                    .equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                                            logger.trace("Has predecessor with a changed dependent option.");
                                            predecessorHasDirtySelection = true;
                                        }
                                    }
                                }

                            }

                            // if (isWorkflowSaved != null && isWorkflowSaved) {
                            if (!isSaveAsNew && (hasDirtyPredecessor || (toInstance != null))) {

                                if (toInstance == null || (hasDirtyPredecessor && (toInstance.getState()
                                        .equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                                            || toInstance.getState().equalsIgnoreCase(
                                                    WorkflowComponentInstanceItem.COMPLETED_WARN)))) {
                                    logger.trace("Adding component to queue: " + id + " for workflow ("
                                            + workflowItem.getId() + ")");
                                    WorkflowComponentInstanceItem wcInstanceItem = null;
                                    if (toInstance == null) {
                                        wcInstanceItem = new WorkflowComponentInstanceItem();
                                    } else {
                                        wcInstanceItem = toInstance;
                                    }
                                    wcInstanceItem.setDirtyAncestor(true);

                                    wcInstanceItem.setWorkflow(workflowItem);
                                    wcInstanceItem.setComponentName(id);
                                    wcInstanceItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                    wfciDao.saveOrUpdate(wcInstanceItem);

                                   thisComponentInstance = wfciDao.get((Integer) wcInstanceItem.getId());

                                    logger.trace("Added component to queue: " + id + " for workflow ("
                                            + workflowItem.getId() + ")");
                                } else {
                                    logger.trace("Setting state.");
                                    if (hasDirtyPredecessor) {
                                        toInstance.setDirtyAncestor(true);
                                        if (toInstance.getState()
                                            .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                                            /*if (id != null && processInputMap.containsKey(id)) {
                                                processInputMap.remove(id);
                                                if (componentQueue.contains(id)) {
                                                    componentQueue.remove(id);
                                                    processedList.add(id);
                                                }
                                            }*/
                                            processInputMap.remove(id);

                                        } else if ((!toInstance.getState()
                                                .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING))) {
                                            toInstance.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                            wfciDao.saveOrUpdate(toInstance);
                                            thisComponentInstance = wfciDao.get((Integer) toInstance.getId());
                                        }
                                    }
                                }
                            }

                        } else {
                            logger.trace("For component " + id + " no 'from' connections were found.");
                        }

                        if (thisComponentInstance != null && (thisComponentInstance.getState().equalsIgnoreCase(
                                WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY))) {

                            executionFlag = false;

                        } else if ((thisComponentInstance != null && !thisComponentInstance.getState()
                                .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN))
                                || (thisComponentInstance == null)) {

                            RunnableComponent runnableComponent = new RunnableComponent();
                            runnableComponent.init(id, thisComponentInstance, componentNode, workflowItem.getId(),
                                    executionFlag, isWorkflowSaved, expectedInputs, remoteServer, heapSize);

                            Thread componentThread = new Thread(runnableComponent);

                            componentThread.start();
                            componentQueueThreads.add(componentThread);
                        } else if (thisComponentInstance != null && thisComponentInstance.getState().equalsIgnoreCase(
                                WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                            // fall through if do_not_run is the state
                        }

                    }
                } // end of for each component

                if (!this.workflowHasMajorErrors) {
                    for (Thread t : componentQueueThreads) {
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            logger.error("Exception while running component in thread: " + e.toString());
                        } catch (Exception e) {
                            logger.error("Exception while running component in thread: " + e.toString());
                        }
                    }
                }
            } else if (processInputMap.isEmpty()) {
                workflowCompleted = true;
                if (workflowItem != null
                        && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {

                    Boolean wasCanceled = false;
                    List<WorkflowComponentInstanceItem> wciItems = wfciDao.findByWorkflow(workflowItem);

                    if (wciItems != null && !wciItems.isEmpty()) {
                        for (WorkflowComponentInstanceItem wciItem : wciItems) {
                            // Workflow has cancelled components
                            if (wciItem.getState() != null && wciItem.getState()
                                    .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY)) {
                                wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                wfciDao.saveOrUpdate(wciItem);
                                wasCanceled = true;
                            } else if (wciItem.getState() != null && wciItem.getState()
                                    .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                                // Components that are descendants of a component in error
                                wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                wfciDao.saveOrUpdate(wciItem);
                            }
                        }
                        if (wasCanceled) {
                            errorMessageMap.add("workflow", "Workflow was canceled.");
                        }
                    }
                }
            }

            try {
                Thread.sleep(WORKFLOW_THREAD_SLEEP);
            } catch (InterruptedException e) {
                break;
            }
        } // end of while loop

    }

    /**
     * Processes all queued workflow components for a given workflow.
     *
     * @param processInputMap
     *            the mapping of input elements to their destination components
     * @param workflowItem
     *            the workflow item
     * @param componentNodes
     *            the component nodes
     * @param executionFlag
     *            whether or not this workflow is being executed or just
     *            validated
     * @param isWorkflowSaved
     *            whether or not this workflow has been saved
     */
    public void processComponentsConsumer(Long workflowId,
            List<Element> componentNodes, Boolean executionFlag, Boolean isWorkflowSaved, Boolean isSaveAsNew) {
        WorkflowItem workflowItem = workflowDao.get(workflowId);
        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        List<ComponentNode> expectedInputs = null;

        /** Processed components. */

        // Process the components using the processInputMap which contains the
        // key, val pairs { toNode, fromNodes }
        while (!workflowCompleted) {

            if (!processInputMap.isEmpty() && !workflowHasMajorErrors) {

                logger.trace("Process Input Map size: " + processInputMap.size());
                for (Iterator<Element> iterator = componentNodes.iterator(); iterator.hasNext();) {
                    Element componentNode = iterator.next();

                    // Process this component node.
                    String id = componentNode.getChildText("component_id");
                    String type = componentNode.getChildText("component_type");
                    String componentName = componentNode.getChildText("component_name");

                    // Process this component if all of its input nodes have
                    // been processed.
                    if (id != null && processInputMap.containsKey(id) && processInputMap.get(id).isEmpty()
                            && !componentQueue.contains(id)) {
                        componentQueue.add(id);
                    }
                }

                // 2
                for (Iterator<Element> iterator = componentNodes.iterator(); iterator.hasNext();) {
                    Element componentNode = iterator.next();
                    Boolean thisComponentHasErrors = false;
                    Boolean hasWarnings = false;
                    // Process this component node.
                    String id = componentNode.getChildText("component_id");
                    String type = componentNode.getChildText("component_type");
                    String componentName = componentNode.getChildText("component_name");

                    // Process this component if all of its input nodes have
                    // been processed.
                    if (processInputMap.containsKey(id) && processInputMap.get(id).isEmpty() && processedList.contains(id)
                            && componentQueue.contains(id)) {

                        logger.info("Processing component. Component Id: " + id + ", type:" + type + ", workflow id: "
                                + workflowId + ", workflowItem null? " + workflowItem.getId());

                        if (outputMappingByComponentId != null && outputMappingByComponentId.containsKey(id)) {
                            ComponentOutput componentOutput = outputMappingByComponentId.get(id);
                            if (componentOutput != null) {
                                Element outputMapping = componentOutput.getNewOutput();

                                // Add results to results message
                                if (componentOutput.getPrevResults() != null) {
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (Element child : componentOutput.getPrevResults()) {
                                        try {
                                            stringBuffer.append(WorkflowXmlUtils.getElementAsString(child));
                                        } catch (IOException e) {
                                            String errMessage = "Workflow results are invalid.";
                                            errorMessageMap.add("workflow", errMessage);
                                            logger.error(errMessage);
                                        }
                                    }

                                    resultsMessage.add(stringBuffer.toString());
                                }

                                if (outputMapping != null) {

                                    // Output data was found so attempt to replace
                                    // any file paths with file item ids.
                                    outputMapping = ComponentHelper.replaceFileIdsWithPaths(outputMapping, baseDir);

                                    parseOutput(outputMapping, componentNode, componentNodes, id, executionFlag);

                                } else {

                                    WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT
                                            .getWorkflowComponentInstanceDao();
                                    WorkflowComponentInstanceItem wciItem = wciDao.findIncompleteByWorkflowAndId(workflowItem, id);
                                    if (wciItem != null && !wciItem.getState()
                                            .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                                        wciItem.setState(WorkflowItem.WF_STATE_ERROR);
                                        wciDao.saveOrUpdate(wciItem);
                                    }

                                    //processInputMap.clear();
                                    //workflowHasErrors = true;
                                    thisComponentHasErrors = true;
                                }
                            }
                        }

                        if (thisComponentHasErrors) {

                            logger.info("Workflow " + workflowId + " has errors.");
                            if (isWorkflowSaved != null) {

                                if (isWorkflowSaved
                                    && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                                    logger.trace("Workflow (saved) " + workflowItem.getId() + " has errors.");
                                    workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                                    workflowDao.saveOrUpdate(workflowItem);
                                }

                                workflowCompleted = true;

                            }
                        } else if (executionFlag) {
                            Boolean canceled = false;


                            WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT
                                    .getWorkflowComponentInstanceDao();
                            WorkflowComponentInstanceItem wciItem = wciDao.findByWorkflowAndId(workflowItem, id);
                            if (wciItem != null && wciItem.getState() != null
                                && wciItem.getState().equalsIgnoreCase(
                                    WorkflowComponentInstancePersistenceItem.WF_STATE_RUNNING_DIRTY)) {
                                canceled = true;
                            }

                            if (workflowHasMajorErrors || canceled) {

                                workflowHasMajorErrors = true;
                                processInputMap.clear();

                            } else {
                                // Completed successfully so remove from queue
                                if (wciItem != null && errorMessageMap.getErrorMessageMap(id) != null
                                        && !errorMessageMap.getErrorMessageMap(id).isEmpty()) {
//mck1a
                                } else if (wciItem != null &&
                                        (wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)
                                        || wciItem.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY))) {
                                    String compOutputDir = WorkflowFileUtils.sanitizePath(baseDir + "/workflows/"
                                        + workflowId + "/" + id + "/output");
                                    List<String> addedMessages = scanComponentLogs(compOutputDir);
                                    StringBuffer warningBuffer = new StringBuffer();
                                    if (!addedMessages.isEmpty()) {
                                        hasWarnings = true;
                                        Integer maxWarnings = addedMessages.size();
                                        if (addedMessages.size() > MAX_WARNING_LINES) {
                                            maxWarnings = MAX_WARNING_LINES;
                                        }
                                        for (Integer warningCount = 0; warningCount < maxWarnings; warningCount++) {
                                            warningBuffer.append(addedMessages.get(warningCount));
                                        }
                                        wciItem.setWarnings(warningBuffer.toString());
                                    }

                                    String nextState = WorkflowComponentInstanceItem.COMPLETED;
                                    if (hasWarnings) {
                                        nextState = WorkflowComponentInstanceItem.COMPLETED_WARN;
                                    }
                                    wciItem.setState(nextState);
                                    wciDao.saveOrUpdate(wciItem);
                                }

                            }
                        }

                        // Remove this node from the list of nodes to be
                        // processed.
                        componentQueue.remove(id);
                        processInputMap.remove(id);

                    } else {

                    }
                }

                if (processInputMap.containsKey("ROOT") && processInputMap.size() == 1) {
                    processInputMap.remove("ROOT");
                    workflowCompleted = true;
                }
            } else if (workflowHasMajorErrors) {
                workflowCompleted = true;
            }

            try {
                Thread.sleep(WORKFLOW_THREAD_SLEEP);
            } catch (InterruptedException e) {
                break;
            }
        } // end of while loop
        String errorWo = ".";
        if (workflowHasMajorErrors || workflowHasErrors) {
            errorWo = " with errors.";
        }
        logger.info("Workflow completed" + errorWo);

        // Update the components with transient states to 'ready' (WF_STATE_NEW)
        List<WorkflowComponentInstanceItem> wciItems = wfciDao.findByWorkflow(workflowItem);
        // Process the unfinished or cancelled components
        if (wciItems != null && !wciItems.isEmpty()) {
            for (WorkflowComponentInstanceItem wciItem : wciItems) {
                // Workflow has cancelled components
                if (wciItem.getState() != null && wciItem.getState()
                        .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY)) {
                    wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                    wfciDao.saveOrUpdate(wciItem);
                } else if (wciItem.getState() != null && wciItem.getState()
                        .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                    // Components that are descendants of a component in error
                    wciItem.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                    wfciDao.saveOrUpdate(wciItem);
                }
            }
        }
        // Save the instances to the persistence table
        if (isWorkflowSaved) {
            workflowHelper.saveComponentInstances(workflowItem, baseDir);
        }
    }

    /**
     * Process the workflow component and return its output mapping element where
     * the element name is "inputs" because it will be given as input to the next component.
     * @param componentNode the component node element
     * @param workflowItem the optional workflowItem
     * @param executionFlag whether this is a workflow execution (true) or validity test (false)
     * @param isWorkflowSaved
     * @param thisComponentInstance
     * @return the output mapping element where the element name is "inputs"
     *  because it will be given as input to the next component
     */
    private ComponentOutput processComponent(Element componentNode, Long workflowId,
                Boolean executionFlag, Boolean isWorkflowSaved, WorkflowComponentInstanceItem thisComponentInstance,
                    List<ComponentNode> expectedInputs, String remoteServer, MachineManager mm, Long heapSize) {
        logger.trace("processComponent method:: Starting.");

        ComponentOutput componentOutput = new ComponentOutput();;
        String osName = System.getProperty("os.name").toLowerCase();

        // The child element "component_id_human" also exists but is not needed
        String xmlWorkflowId = componentNode.getChildText("workflow_id");
        String componentId = componentNode.getChildText("component_id");
        String componentIdHuman = componentNode.getChildText("component_id_human");
        String componentType = componentNode.getChildText("component_type")
            .trim().toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "_");
        String componentName = componentNode.getChildText("component_name")
            .trim().toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "_");
        WorkflowItem workflowItem = null;
        AbstractComponent componentObject = new Component(componentNode);

        if (xmlWorkflowId.matches("\\d+") && componentId.matches("[a-zA-Z0-9_\\-]+")) {
            if (xmlWorkflowId.equalsIgnoreCase(workflowId.toString())) {
                workflowItem = workflowDao.get(workflowId);
            }
            // Find the component information in the database.
            if (componentName != null) {
                componentName = componentName.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
            }

            WorkflowComponentItem workflowComponentItem =
                workflowComponentDao.findByName(componentName);


            // Setup and execute the component.
            if (workflowComponentItem != null) {
                logger.trace("Component definition exists in database: Component type: "
                    + componentType + ", Component name: " + componentName);
                String componentXmlFilepath = WorkflowFileUtils.sanitizePath(componentsDir + componentId + ".xml");

                if (isWorkflowSaved && componentType != null && thisComponentInstance != null) {
                    cleanComponentFiles(workflowItem, thisComponentInstance.getComponentName(), componentType);
                }

                if (thisComponentInstance != null && executionFlag
                        && !thisComponentInstance.getState()
                            .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY)
                        && !thisComponentInstance.getState()
                            .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {
                    thisComponentInstance.setState(WorkflowComponentInstanceItem.WF_STATE_RUNNING);
                    wfciDao.saveOrUpdate(thisComponentInstance);


                }
                List<String> inputComponentIds = Collections.synchronizedList(new ArrayList<String>());
                List<String> inputComponentFiles = Collections.synchronizedList(new ArrayList<String>());

                Map<Integer, Integer> fileIndexByNode = new HashMap<Integer, Integer>();

                Boolean minOccursSat = false;
                Boolean maxOccursSat = true;
                // Attach any inputs to component XML
                if (inputMappingByComponentId
                        .containsKey(componentId)) {
                    Element newInputs = new Element("inputs");
                    List<Element> inputs = inputMappingByComponentId.get(componentId);
                    logger.trace("Attaching " + inputs.size() + " inputs to component " + componentId);

                    for (Element input : inputs) {
                        newInputs.addContent((Element)input.clone());
                        if (input.getName().matches("\\s*input[0-9]+\\s*")) {
                            Integer newIndex = Integer.parseInt(input.getName().replaceAll("input", "").trim());

                            if (input.getChild("component_id") != null) {
                                String cId = input.getChildText("component_id");
                                if (!inputComponentIds.contains(cId)) {
                                    Element filesElem = input.getChild("files");
                                    if (filesElem != null && filesElem.getChildren() != null) {
                                        List<Element> filesChildren = filesElem.getChildren();
                                        for (Element filesChild : filesChildren) {

                                            // increment the unordered file index
                                            if (fileIndexByNode.containsKey(newIndex)) {
                                                Integer lastCount = fileIndexByNode.get(newIndex) + 1;
                                                if (lastCount > 0 && lastCount < Integer.MAX_VALUE) {
                                                    fileIndexByNode.put(newIndex, lastCount);
                                                }
                                            } else {
                                                fileIndexByNode.put(newIndex, 0);
                                            }

                                            if (filesChild.getChild("file_name") != null) {
                                                String filePath = filesChild.getChildTextTrim("file_path");

                                                String fileName = filesChild.getChildTextTrim("file_name");
                                                if (!fileName.isEmpty()) {

                                                    String replaceChars = null;
                                                    String delimReplace = null;
                                                    if (osName.indexOf("win") >= 0) {
                                                        replaceChars = "\"";
                                                        delimReplace = "\\\"";
                                                    } else {
                                                        replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
                                                        delimReplace = "$1";
                                                    }

                                                    String safeFileName = fileName.replaceAll(replaceChars, delimReplace);
                                                    inputComponentIds.add(cId);
                                                    inputComponentFiles.add(safeFileName);

                                                    String safeFilePath = filePath.replaceAll("/+",  "/").replaceAll(replaceChars, delimReplace);
                                                    String patternStr = (baseDir + "/workflows" + "/" + workflowItem.getId() + "/(Data[a-zA-Z0-9_\\-]+)/output/.*").replaceAll("/+",  "/");
                                                    if (safeFilePath.matches(patternStr)) {
                                                        Pattern pattern = Pattern.compile(patternStr);
                                                        Matcher m = pattern.matcher(safeFilePath);
                                                        String foreignComponentId = null;
                                                        if (m.find()) {
                                                            foreignComponentId = m.group(1);
                                                            if (!inputComponentIds.contains(foreignComponentId)) {
                                                                inputComponentIds.add(foreignComponentId);
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
                    }

                    if (componentNode.getChild("inputs") != null) {
                        componentNode.removeChild("inputs");
                    }
                    componentNode.addContent(newInputs);
                }

                if (componentName.equalsIgnoreCase("import")) {
                    minOccursSat = true;
                }
                if (expectedInputs != null && !expectedInputs.isEmpty()) {
                    for (ComponentNode cNode : expectedInputs) {
                        if (fileIndexByNode.containsKey(cNode.getNodeIndex())
                                && fileIndexByNode.get(cNode.getNodeIndex()) >= cNode.getMinOccurs() - 1) {
                            minOccursSat = true;
                        } else if (fileIndexByNode.size() == 0 && cNode.getMinOccurs() == 0) {
                            // Account for the case where minOccurs="0" and there are no inputs to the component
                            minOccursSat = true;
                        }
                        if (fileIndexByNode.containsKey(cNode.getNodeIndex())
                                && fileIndexByNode.get(cNode.getNodeIndex()) > cNode.getMaxOccurs() - 1) {
                            maxOccursSat = false;
                        }
                    }
                } else if (expectedInputs.isEmpty()) {
                    // The component expects no input
                    minOccursSat = true;
                }

                // Instantiate the generic component wrapper, and setup
                // the generic component to be executed.
                componentObject.setInterpreterPath(workflowComponentItem.getInterpreterPath());
                componentObject.setToolPath(workflowComponentItem.getToolPath());
                componentObject.setSchemaPath(workflowComponentItem.getSchemaPath());
                componentObject.setToolDir(workflowComponentItem.getToolDir());
                if (userItem != null) {
                    componentObject.setUserId((String) userItem.getId());
                } else {
                    componentObject.setUserId(UserItem.DEFAULT_USER);
                }
                componentObject.setComponentXmlFilePath(componentXmlFilepath);
                componentObject.setWorkflowDir(baseDir + "/workflows/" + workflowId + "/");

                Boolean isDirty = false;

                if (thisComponentInstance != null) {
                    if (thisComponentInstance.isDirty()) {
                        isDirty = true;
                    }
                }

                if (workflowItem != null) {

                    logger.trace("Initializing component " + componentId);

                    String componentXml = null;
                    try {
                        // If the file doesn't exist in component_file for this component Id, then make a new reference.
                        componentNode = copyComponentFiles(workflowItem, componentId, componentNode);

                        componentXml = WorkflowXmlUtils.getElementAsString(
                                ComponentHelper.replaceFileIdsWithPaths((Element) componentNode, baseDir));
                    } catch (IOException e) {
                        logger.error("Could not process component XML.");
                    }

                    if (componentXml != null) {
                        componentObject.init(componentXml, thisComponentInstance);
                        componentObject.sortInputOrder();
                        try {
                            WorkflowFileUtils.writeFile(componentXmlFilepath , WorkflowXmlUtils.getElementAsString(
                                    (Element) componentObject.component.clone()));
                        } catch (IOException e) {
                            logger.error("Could not write component XML to file '" + componentXmlFilepath + "'");
                        }

                        logger.trace("Validating component " + componentId);
                        // Do not do any checks on the old import components
                        if (componentType != null && !componentType.equalsIgnoreCase("import")) {
                            if ((minOccursSat && maxOccursSat) || executionFlag) {
                            /* mck2 if (!satisfiesInputReqs && executionFlag && !componentType.equalsIgnoreCase("import")) {
                                isValid = componentObject.validate(); // errorMessageMap.add(componentId, "Component requires input");
                            } else {
                                isValid = componentObject.validate();
                            }*/
                            componentObject.validate();

                            } else if (!minOccursSat) {
                                String errMessage = "Component " + componentObject.getIdHuman() + " is missing a required input.";
                                errorMessageMap.add(componentId, componentIdHuman, errMessage);
                                logger.error(errMessage);
                            } else if (!maxOccursSat) {
                                String errMessage = "Component " + componentObject.getIdHuman() + " has too many inputs.";
                                errorMessageMap.add(componentId, componentIdHuman, errMessage);
                                logger.error(errMessage);
                            }
                        }
                    }

                    // Run the component.
                    logger.trace("Testing component " + componentId);

                    if (componentObject.test()) {
                        if (executionFlag) {
                            logger.trace("Executing component " + componentId);


                            if (thisComponentInstance != null && !componentType.equalsIgnoreCase("import")
                                    && !componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME)) {
                                ComponentHelper.deleteComponentOutput(workflowItem, componentId, baseDir);
                            }


                            // Run component
                            if (thisComponentInstance != null && thisComponentInstance.getState() != null
                                    && thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING_DIRTY)) {
                                componentOutput =
                                        loadDummyResults(componentObject, workflowItem);

                            } else {
                                componentOutput = runComponent(componentObject, workflowItem, executionFlag,
                                    inputComponentIds, inputComponentFiles, remoteServer, mm, heapSize);
                                String componentOutputDir = WorkflowFileUtils.getWorkflowsDir(workflowId) + componentId + "/output/";
                                List<String> addedMessages = scanComponentLogs(baseDir + "/" + componentOutputDir);
                                for (String addedMessage : addedMessages) {
                                    componentObject.addGenericMessage(addedMessage);
                                }
                            }

                        } else if (!executionFlag) {
                            // Do not execute. Either user previous results or run a "shallow" execution (a.k.a., pre-processing).
                            // Pre-process component:: does not run component and produces no file_path or file_name
                            // but it does produce file and component meta-data used in populating options in the GUI.
                            // It will also test and look for syntax errors during the pre-processing.

                            if (workflowItem != null && workflowItem.getResults() != null && !isDirty) {
                                // Try getting previous results if they exist and this component has not
                                // been modified since its last execution.
                                componentOutput = loadPreviousResults(componentId, workflowItem);
                            } else if (workflowItem != null && workflowItem.getResults() != null && isDirty
                                    && thisComponentInstance != null
                                    && thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_ERROR)) {
                                // If the component has been modified and its state is in error,
                                // then load its dummy results (lacking file info).
                                componentOutput =
                                        loadDummyResults(componentObject, workflowItem);
                            } else {

                            // If the results have not been loaded, run the component in pre-processing mode
                            // with the latest componentObject and workflow item properties.
                                // mckhere if dirty ancestor and not dirty selection
                                componentOutput = runComponent(componentObject, workflowItem, executionFlag,
                                    inputComponentIds, inputComponentFiles, remoteServer, null, heapSize);
                            }
                        }
                    }

                    // Update workflow item if component is dirty
                    if (isDirty) {
                        logger.trace("Pre-processing complete. Update workflow XML in database for component: " + componentId);
                                // + ComponentXmlHandler.getElementAsString(componentObject.component));

                        updateComponentXml(workflowItem, componentObject);
                    }

                }

            } else {
                String errMessage = "Component definition DOES NOT exist for component type = "
                        + componentType + ", component name = " + componentName;
                errorMessageMap.add(componentId, componentIdHuman, errMessage);
                logger.error(errMessage);
            }

            Boolean hasErrors = handleStates(workflowItem, componentId, componentObject, isWorkflowSaved, executionFlag);
            if (hasErrors && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                setWorkflowError(isWorkflowSaved, workflowItem, componentId);

                markSuccessorsDoNotRun(workflowItem, componentId);

            }
        }
        return componentOutput;

    }


    /**
     * Run (or partially run if the executionFlag is false) the component.
     * @param componentObject the AbstractComponent
     * @param workflowItem the workflow
     * @param executionFlag whether to fully execute (true) or only partially execute (false) the component
     * @return the ComponentOutput object
     */
    private ComponentOutput runComponent(AbstractComponent componentObject, WorkflowItem workflowItem, Boolean executionFlag,
            List<String> inputComponentIds, List<String> inputComponentFiles, String remoteServer, MachineManager mm, Long heapSize) {

        String componentXmlFilepath = WorkflowFileUtils.sanitizePath(componentsDir + componentObject.getComponentId() + ".xml");
        String workflowDir = (baseDir + "/workflows/" + workflowId + "/").replaceAll("\\\\", "/").replaceAll("/+", "/");
        String componentSubdir = "workflows/" + workflowId + "/" + componentObject.getComponentId() + "/output/";
        Element outputMapping = null;
        ComponentOutput componentOutput = new ComponentOutput();
        String componentName = componentObject.getComponentName().trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        String componentId = componentObject.getComponentId();
        WorkflowComponentItem workflowComponentItem =
            workflowComponentDao.findByName(componentName);
        String userId = null;
        if (userItem != null) {
            userId = (String) userItem.getId();
        } else {
            userId = UserItem.DEFAULT_USER;
        }

        Boolean preprocessFlag = !executionFlag;

        // Additional check.
        Pattern patternImport = Pattern.compile("import.*", Pattern.CASE_INSENSITIVE);
        Matcher matchImport = patternImport.matcher(componentId);
        if (matchImport.find()){
            remoteServer = null;
        } else {
            WorkflowComponentDao wcDao = DaoFactory.DEFAULT.getWorkflowComponentDao();
            WorkflowComponentItem wcItem = wcDao.findByName(componentName);
            if (!wcItem.getRemoteExecEnabled()) {
                remoteServer = null;
            }
        }

        if (remoteServer == null) {
            componentObject.run(userId, componentXmlFilepath, workflowDir,
                workflowComponentItem.getToolDir(), workflowComponentItem.getSchemaPath(),
                inputComponentIds, inputComponentFiles, preprocessFlag, remoteServer, heapSize, null);
            outputMapping =  componentObject.getOutputMapping();
            componentOutput.setNewOutput(outputMapping);
        } else {

            componentObject.run(userId, componentXmlFilepath, workflowDir,
                    workflowComponentItem.getToolDir(), workflowComponentItem.getSchemaPath(),
                    inputComponentIds, inputComponentFiles, preprocessFlag, remoteServer, heapSize, mm);
            outputMapping =  componentObject.getOutputMapping();
            componentOutput.setNewOutput(outputMapping);
        }

        if (outputMapping != null
                && outputMapping.getChildren() != null) {

            for (Element input : (List<Element>) outputMapping.getChildren()) {
                // Component program errors can be found in the <errors> element
                // of the component output (results).
                Element cloneChild = (Element) input.clone();
                String componentProgramErrors =  cloneChild.getChildText("errors");
                if (componentProgramErrors != null && !componentProgramErrors.trim().isEmpty()) {
                    errorMessageMap.add(componentObject.getComponentId(), componentObject.getIdHuman(),
                            componentProgramErrors);
                    logger.error("Component program error: '" + componentProgramErrors + "'");
                } else {
                    componentProgramErrors = null;
                }
                componentOutput.addPrevResults(ComponentHelper.replaceFilePathsWithIds(
                        baseDir, workflowItem, userItem, componentObject.getComponentId(),
                        componentSubdir, cloneChild, executionFlag));
            }



        } else if (!componentName.equalsIgnoreCase(AbstractComponent.IMPORT_COMPONENT_NAME)) {
            // The component has failed to produce XML output to stdout or any error stream to stderr
            errorMessageMap.add(componentObject.getComponentId(), componentObject.getIdHuman(),
                "Component has failed unexpectedly.");
        }

        return componentOutput;

    }

    /**
     * Replace the file paths in the componentObject with file ids and
     * and replace the previous XML (for the given component only) in the workflow item.
     * @param workflowItem the workflow item whose XML will be updated with new component XML
     * @param componentObject the component object which contains the component XML
     * @see edu.cmu.pslc.datashop.workflows.WorkflowImportHelper#saveWorkflowToDatabase(WorkflowItem)
     */
    private void updateComponentXml(WorkflowItem workflowItem, AbstractComponent componentObject) {
        try {
            String componentSubdir = "workflows/" + workflowItem.getId() + "/" + componentObject.getComponentId() + "/output/";
            Element root = WorkflowXmlUtils.getStringAsElement(workflowItem.getWorkflowXml());
            if (root != null && root.getChild("components") != null && root.getChild("components").getChildren() != null) {
                Element componentsContainer = root.getChild("components");

                List<Element> deleteElements = new ArrayList<Element>();
                for (Element componentChild : (List<Element>)componentsContainer.getChildren()) {
                    if (componentChild.getChild("component_id") != null) {

                        String text = componentChild.getChild("component_id").getTextTrim();

                        if (text != null && text.equalsIgnoreCase(componentObject.getComponentId())) {
                            deleteElements.add(componentChild);
                        }
                    }
                }
                for (Iterator it = deleteElements.iterator(); it.hasNext(); ) {
                    Element deleteElement = (Element)it.next();

                    componentsContainer.removeContent(deleteElement);

                    componentsContainer.addContent(
                            ComponentHelper.replaceFilePathsWithIds(
                            baseDir, workflowItem, userItem, componentObject.getComponentId(), componentSubdir,
                                (Element) componentObject.component.clone(), false));
                }

                root.removeChild("components");
                root.addContent(componentsContainer);

            }

            String newXml = WorkflowXmlUtils.getElementAsString(root);
            workflowItem.setWorkflowXml(newXml);

            // Update the database with this workflow XML
            logger.trace("Updating xml in database: " + newXml);
            if (!workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
                workflowHelper.saveTempWorkflowToDatabase(workflowItem, workflowDao);
            }
        } catch (IOException e) {
            logger.error("Could not save workflow '" + workflowItem.getId()  + "' to database.");
        } catch (JDOMException e) {
            logger.error("Could not convert workflow root to XML.");
        }

    }

    private Element copyComponentFiles(WorkflowItem workflowItem, String componentId, Element componentNode) {
        WorkflowFileDao wfFileDao = DaoFactory.DEFAULT.getWorkflowFileDao();
        ComponentFileDao cfDao = DaoFactory.DEFAULT.getComponentFileDao();
        ElementFilter elementFilter = new ElementFilter("file_path");

        List<Element> modifyFileList = new ArrayList<Element>();

        for (Iterator iter = componentNode.getDescendants(elementFilter); iter.hasNext(); ) {
            Element descendant = (Element) iter.next();
            modifyFileList.add(descendant);
        }

        for (Element descendant : modifyFileList) {
            String fileId = descendant.getTextTrim();

            if (fileId != null && !fileId.isEmpty() && fileId.matches("[0-9]+")) {

                WorkflowFileItem fileItem = wfFileDao.get(Integer.parseInt(fileId));

                List<ComponentFileItem> cfItems = cfDao.findByFile(fileItem);


                if (cfItems != null) {
                    Boolean foundComponent = false;
                    ComponentFileItem lastCfItem = null;
                    for (ComponentFileItem cfItem : cfItems) {
                        WorkflowItem wfItem = cfItem.getWorkflow();
                        if (wfItem.getId().equals(workflowItem.getId())) {
                            if (cfItem.getComponentId().equalsIgnoreCase(componentId)) {
                                foundComponent = true;
                            }
                            lastCfItem = cfItem;
                        }
                    }

                    if (!foundComponent && lastCfItem != null) {


                        UserItem owner = userDao.get(fileItem.getOwner().getId().toString());
                        WorkflowFileItem newFileItem = workflowHelper.saveWorkflowFileAsWorkflowFile(workflowItem.getId(), componentId,
                                fileItem.getTitle(), fileItem.getDescription(), fileItem, baseDir, owner);
                        // Important to check if null, in case the file already exists for this component
                        if (newFileItem != null) {
                            descendant.setText(newFileItem.getId().toString());
                        }
                    }
                }
            }
        }
        return componentNode;
    }

    /**
     * This method removes obsolete files if they exist given a component id.
     * @param workflowItem the workflow
     * @param componentId the component
     * @param baseDir the datashop base dir
     */
    private void cleanComponentFiles(WorkflowItem workflowItem, String componentId, String componentType) {
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

                    WorkflowFileItem wfFileItem = wfDao.get((Integer) componentFileItem.getFile().getId());
                    //String wfFileId = WorkflowHelper.sanitizePath(wfFileItem.getFullFileName(WorkflowHelper
                            //.getStrictDirFormat(getBaseDir())));
                    if (wfFileItem != null) {
                        wfFileIds.add((Integer) wfFileItem.getId());
                    }
                }

            }
        }

        List<ComponentFilePersistenceItem> componentFilePersistenceItems = compFilePersistenceDao.findImportByComponent(workflowItem, componentId);

        if (componentFilePersistenceItems != null && !componentFilePersistenceItems.isEmpty()) {
            for (Iterator<ComponentFilePersistenceItem> cfPersistenceIterator = componentFilePersistenceItems.iterator(); cfPersistenceIterator.hasNext() ;) {
                ComponentFilePersistenceItem componentFilePersistenceItem = cfPersistenceIterator.next();
                if (componentFilePersistenceItem.getFile() != null) {

                    WorkflowFileItem wfFileItem = wfDao.get((Integer) componentFilePersistenceItem.getFile().getId());
                    //String wfFileId = WorkflowHelper.sanitizePath(wfFileItem.getFullFileName(WorkflowHelper
                            //.getStrictDirFormat(getBaseDir())));
                    if (wfFileItem != null) {
                        wfFileIds.add((Integer) wfFileItem.getId());
                    }
                }

            }
        }

        File[] files = componentOutputDir.listFiles();
        for (int idx = 0; files != null && idx < files.length; idx++) {
            File fileOrDir = files[idx];
            List<WorkflowFileItem> wfFileItems = wfDao.find(subPath, fileOrDir.getName());
            if (wfFileItems != null && !wfFileItems.isEmpty()) {
                for (Iterator<WorkflowFileItem> wfFileItemIter = wfFileItems.iterator(); wfFileItemIter.hasNext(); ) {
                    WorkflowFileItem wfFileItem = wfFileItemIter.next();
                    if (fileOrDir.isFile() && !wfFileIds.contains((Integer)wfFileItem.getId())) {
                        if (!fileOrDir.getName().matches(".*\\.log")) {
                            wfDao.delete(wfFileItem);
                            fileOrDir.delete();
                        }
                    }
                }
            }
        }



    }

    private void markSuccessorsDoNotRun(WorkflowItem workflowItem, String componentId) {
        WorkflowComponentAdjacencyDao wfcAdjDao = DaoFactory.DEFAULT.getWorkflowComponentAdjacencyDao();
        List<WorkflowComponentAdjacencyItem> wfcAdjItems = wfcAdjDao.findByWorkflowAndId(workflowItem, componentId);
        if (wfcAdjItems != null) {

            WorkflowComponentInstanceDao wciDao = DaoFactory.DEFAULT
                    .getWorkflowComponentInstanceDao();
            /*WorkflowComponentInstanceItem wciItem = wciDao.findByWorkflowAndId(workflowItem, componentId);
            if (wciItem != null && wciItem.getState() // mck1
                    .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_ERROR)) {*/

                for (WorkflowComponentAdjacencyItem wfcAdjItem : wfcAdjItems) {
                    if (wfcAdjItem.getChildId() != null) {
                        WorkflowComponentInstanceItem wciChildItem =
                            wciDao.findIncompleteByWorkflowAndId(workflowItem, wfcAdjItem.getChildId());
                        if (wciChildItem != null) {
                            Hibernate.initialize(wciChildItem);
                            wciChildItem.setState(WorkflowComponentInstanceItem.DO_NOT_RUN);
                            wciDao.saveOrUpdate(wciChildItem);
                            markSuccessorsDoNotRun(workflowItem, wciChildItem.getComponentName());
                        }
                    }
                }
            /*}*/


        }
    }

    /**
     * Returns the ComponentOutput object devoid of any input or output files.
     * @param componentObject the component object
     * @param workflowItem the workflow item
     * @return the ComponentOutput object
     */
    private ComponentOutput loadDummyResults(AbstractComponent componentObject, WorkflowItem workflowItem) {

        logger.trace("No previous results found. Creating new options.");
        String componentSubdir = "workflows/" + workflowItem.getId() + "/" + componentObject.getComponentId() + "/output/";
        ComponentOutput componentOutput = new ComponentOutput();
        componentOutput.setNewOutput(new Element("inputs"));
        Element newOutput = null;
        try {
            newOutput = WorkflowXmlUtils.getStringAsElement(componentObject.getOutput());
        } catch (JDOMException e) {
            logger.error("Could not convert component output to XML for component '" + componentObject.getComponentId() + "'");
        } catch (IOException e) {
            logger.error("Could not read component item's output for component '" + componentObject.getComponentId() + "'");
        }

        if (newOutput != null) {
            if (newOutput.getName().equalsIgnoreCase("outputs")) {
                componentOutput.getNewOutput().addContent((List<Element>)newOutput.cloneContent());
            } else {
                componentOutput.getNewOutput().addContent((Element)newOutput.clone());
            }

            Element prevResults = ComponentHelper.replaceFilePathsWithIds(
                baseDir, workflowItem, userItem, componentObject.getComponentId(), componentSubdir, newOutput, false);
            componentOutput.addPrevResults(prevResults);
        }
        return componentOutput;
    }

    /**
     * Loads the component item's previous results.
     * @param componentId the Component Id
     * @param workflowItem the WorkflowItem
     * @return the ComponentOutput object
     */
    private ComponentOutput loadPreviousResults(String componentId, WorkflowItem workflowItem) {
        logger.trace("Loading previous results for component " + componentId);
        Element workflowResults = null;
        ComponentOutput componentOutput = new ComponentOutput();

        try {
            workflowResults = WorkflowXmlUtils.getStringAsElement(
                "<outputs>" + workflowItem.getResults() + "</outputs>");
        } catch (JDOMException e) {
            logger.error("Could not convert workflow results to XML.");
        } catch (IOException e) {
            logger.error("Could not read workflow results.");
        }
        if (workflowResults != null && workflowResults.getChildren() != null) {
            logger.trace("Previous results found.");
            Element outputMapping = new Element("inputs");
            for (Element output : (List<Element>)workflowResults.getChildren()) {
                if (output.getChild("component_id") != null) {
                    String resultsComponentId = output.getChildText("component_id");
                    if (resultsComponentId.equalsIgnoreCase(componentId)) {
                        componentOutput.addPrevResults(output);

                        if (output.getName().equalsIgnoreCase("outputs")) {
                            outputMapping.addContent((List<Element>) output.cloneContent());
                        } else {
                            outputMapping.addContent((Element) output.clone());
                        }



                    }
                }
            }
            componentOutput.setNewOutput(outputMapping);
        }
        return componentOutput;
    }

    /**
     * @param workflowItem
     * @param componentId
     * @param componentObject
     * @param isWorkflowSaved
     * @param executionFlag
     * @return
     */
    private Boolean handleStates(WorkflowItem workflowItem, String componentId, AbstractComponent componentObject , Boolean isWorkflowSaved, Boolean executionFlag) {

        Boolean hasErrors = false;
        Boolean hasWarnings = false;
        if (!workflowHasMajorErrors &&
            (workflowItem != null
                && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY))) {
            WorkflowComponentInstanceItem thisComponentInstance = null;
            // Handle component instances
            try{
                thisComponentInstance
                    = wfciDao.findByWorkflowAndId(workflowItem, componentId);

                // Add error messages, if any.
                if ((errorMessageMap.getErrorMessageMap("workflow") != null
                        && !errorMessageMap.getErrorMessageMap("workflow").isEmpty())
                    || (errorMessageMap.getErrorMessageMap(componentId) != null
                        && !errorMessageMap.getErrorMessageMap(componentId).isEmpty())
                    || (componentObject.getErrorMessages() != null
                        && !componentObject.getErrorMessages().isEmpty())) {


                    for (String message : componentObject.getErrorMessages()) {
                        // Component objects contain an ErrorMessageMap separate
                        // from the component's <error>
                        if (message != null && !message.trim().isEmpty()) {
                            errorMessageMap.add(componentId, componentObject.getIdHuman(), message);
                            logger.error("Component error: " + message);
                        }
                    }

                    // If the component has errors and the workflow is saved, then update the component instance item.
                    if (isWorkflowSaved != null && isWorkflowSaved) {
                        workflowItem.setState(WorkflowItem.WF_STATE_ERROR);
                        workflowDao.saveOrUpdate(workflowItem);

                        // If one doesn't exist, create the component instance.
                        if (thisComponentInstance == null) {

                            thisComponentInstance =  new WorkflowComponentInstanceItem();
                            thisComponentInstance.setDirtyOption(true);

                            thisComponentInstance.setWorkflow(workflowItem);
                            thisComponentInstance.setComponentName(componentId);
                            logger.trace("Added component to queue: " + componentId
                                    + " for workflow (" + workflowItem.getId() + ")");
                        }


                    if (thisComponentInstance != null && !thisComponentInstance.getState()
                                .equalsIgnoreCase(WorkflowComponentInstanceItem.DO_NOT_RUN)) {

                            // Set the instance state to error
                            thisComponentInstance.setState(WorkflowComponentInstanceItem.WF_STATE_ERROR);
                            if (errorMessageMap.getErrorMessageMap(componentId) != null) {
                                thisComponentInstance.setErrors(errorMessageMap.getErrorMessageMapAsJson(componentId).toString());
                            }
                            // Save or update the wfci.
                            wfciDao.saveOrUpdate(thisComponentInstance);
                            hasErrors = true;
                        }

                    } else if (thisComponentInstance != null && isWorkflowSaved != null && isWorkflowSaved) {
                        if (executionFlag) {
                            if (thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_RUNNING)) {

                                // also delete persistence item since execution flag is true
                                WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                                        .getWorkflowComponentInstancePersistenceDao();
                                WorkflowComponentInstancePersistenceItem wcipItem = wciPersistDao.findByWorkflowAndId(
                                        workflowItem, thisComponentInstance.getComponentName());
                                String nextState = WorkflowComponentInstanceItem.COMPLETED;
                                if (hasWarnings) {
                                    nextState = WorkflowComponentInstanceItem.COMPLETED_WARN;
                                }
//mck1
                                thisComponentInstance.setState(nextState);
                                wfciDao.saveOrUpdate(thisComponentInstance);

                                wcipItem.setState(nextState);
                                wciPersistDao.saveOrUpdate(wcipItem);

                            } else {
                                //thisComponentInstance.setState(WorkflowComponentInstanceItem.WF_STATE_NEW);
                                //wfciDao.saveOrUpdate(thisComponentInstance);
                            }
                        }
                    } else if (thisComponentInstance != null && isWorkflowSaved != null && !isWorkflowSaved) {

                            // Set error on instance item if workflow is unsaved and ANY error
                            // message exists (includes missing inputs error)
                            thisComponentInstance.setState(WorkflowComponentInstanceItem.WF_STATE_ERROR);
                            wfciDao.saveOrUpdate(thisComponentInstance);

                    }
                }

              //mck1b
                // Add generic messages, if any.
                if ( (genericMessageMap.getErrorMessageMap("workflow") != null
                        && !genericMessageMap.getErrorMessageMap("workflow").isEmpty())
                    || (genericMessageMap.getErrorMessageMap(componentId) != null
                        && !genericMessageMap.getErrorMessageMap(componentId).isEmpty()) ||
                    (componentObject.getGenericMessages() != null
                        && !componentObject.getGenericMessages().isEmpty())) {

                    hasWarnings = true;
                    Integer warningCount = 0;
                    StringBuffer warnings = new StringBuffer();
                    for (String message : componentObject.getGenericMessages()) {
                        // Component objects contain an GenericMessageMap separate
                        // from the component's <generic>
                        if (message != null && !message.trim().isEmpty()) {
                            genericMessageMap.add(componentId, componentObject.getIdHuman(), message);
                            if (warningCount < MAX_WARNING_LINES) {
                                warnings.append(message);
                                warningCount++;
                            }
                            logger.warn("Component warning: " + message);
                        }
                    }
                    if (thisComponentInstance != null) {
                        thisComponentInstance.setWarnings(warnings.toString());
                        wfciDao.saveOrUpdate(thisComponentInstance);
                    }
                }
            } catch(Exception e) {
                logger.error("Unexpected error:: " + e.toString() + "::" + e.getCause());
                if (isWorkflowSaved != null && isWorkflowSaved) {

                    if (thisComponentInstance == null) {
                        thisComponentInstance =  new WorkflowComponentInstanceItem();
                        thisComponentInstance.setDirtyOption(true);

                        thisComponentInstance.setWorkflow(workflowItem);
                        thisComponentInstance.setComponentName(componentId);
                        logger.trace("Added component to queue: " + componentId
                                + " for workflow (" + workflowItem.getId() + ")");
                    }

                    if (errorMessageMap.getErrorMessageMap(componentId) != null) {
                        thisComponentInstance.setErrors(errorMessageMap.getErrorMessageMapAsJson(componentId).toString());
                    }
                    thisComponentInstance.setState(WorkflowComponentInstanceItem.WF_STATE_ERROR);
                    wfciDao.saveOrUpdate(thisComponentInstance);
                }
            }
        }

        return hasErrors;
    }



    private void setWorkflowError(Boolean isWorkflowSaved, WorkflowItem workflowItem, String componentId) {
     // We have a component error so set the workflow error flag.
        workflowHasErrors = true;
        if (isWorkflowSaved != null && isWorkflowSaved
                && !workflowItem.getState().equalsIgnoreCase(WorkflowItem.WF_STATE_RUNNING_DIRTY)) {
            WorkflowComponentInstancePersistenceDao wciPersistDao = DaoFactory.DEFAULT
                .getWorkflowComponentInstancePersistenceDao();
            WorkflowComponentInstanceItem wcii = wfciDao.findByWorkflowAndId(workflowItem, componentId);

            if (wcii != null
                    && wcii.getState()
                        .equalsIgnoreCase(WorkflowComponentInstanceItem.WF_STATE_ERROR)) {

                WorkflowComponentInstancePersistenceItem wcipItem = null;
                // If there is an instance, it should persist.
                wcipItem = wciPersistDao.findByWorkflowAndId(workflowItem, componentId);

                if (wcipItem == null) {
                    wcipItem = new WorkflowComponentInstancePersistenceItem();
                    wcipItem.setComponentName(wcii.getComponentName());
                    wcipItem.setWorkflow(workflowItem);
                }
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
        }


    }

    /**
     * Gets the results message (xml).
     *
     * @return the results message
     */
    public synchronized String getWorkflowResults() {
        StringBuffer resultsString = new StringBuffer();
        if (resultsMessage != null && !resultsMessage.isEmpty()) {
            Boolean loopFlag = true;
            do {
                String queueHead = resultsMessage.poll();
                if (queueHead != null) {
                    resultsString.append(queueHead);
                } else {
                    loopFlag = false;
                }
            } while (loopFlag);
        }
        return resultsString.toString();
    }

    /**
     * Sets the results message (xml).
     *
     * @param message
     *            the results message to set
     */
    public synchronized void setWorkflowResults(String message) {
        resultsMessage.clear();
        resultsMessage.add(message);
    }


    class ProducerThread implements Runnable {
        WorkflowItem workflowItem;
        Boolean executionFlag;
        Boolean isWorkflowSaved;
        List<Element> componentNodes;
        Element outputMapping = null;
        ComponentOutput componentOutput = null;
        Boolean isSaveAsNew;
        Long heapSize = null;
        String remoteServer = null;

        public void init(WorkflowItem workflowItem, Boolean executionFlag,
                Boolean isWorkflowSaved, List<Element> componentNodes, Boolean isSaveAsNew, Long heapSize, String remoteServer) {
            this.workflowItem = workflowItem;
            this.executionFlag = executionFlag;
            this.isWorkflowSaved = isWorkflowSaved;
            this.componentNodes = componentNodes;
            this.isSaveAsNew = isSaveAsNew;
            this.heapSize = heapSize;
            this.remoteServer = remoteServer;
        }

        @Override
        public void run() {
            processComponentsProducer(workflowItem, componentNodes, executionFlag, isWorkflowSaved, isSaveAsNew, heapSize, remoteServer);
        }
    }


    class RunnableComponent implements Runnable {
        String componentId = null;
        WorkflowComponentInstanceItem thisComponentInstance;
        Long workflowId;
        Boolean executionFlag;
        Boolean isWorkflowSaved;
        Element componentNode;
        Element outputMapping = null;
        ComponentOutput componentOutput = null;
        Boolean isSaveAsNew;
        List<ComponentNode> expectedInputs = null;
        String remoteServer = null;
        MachineManager mm = null;
        Long heapSize = null;

        public void init(String componentId, WorkflowComponentInstanceItem thisComponentInstance,
                Element componentNode, Long workflowId, Boolean executionFlag,
                Boolean isWorkflowSaved, List<ComponentNode> expectedInputs, String remoteServer, Long heapSize) {
            this.componentId = componentId;
            this.thisComponentInstance = thisComponentInstance;
            this.workflowId = workflowId;
            this.executionFlag = executionFlag;
            this.isWorkflowSaved = isWorkflowSaved;
            this.componentNode = componentNode;
            this.isSaveAsNew = isSaveAsNew;
            this.expectedInputs = expectedInputs;
            this.remoteServer = remoteServer;
            this.mm = new MachineManager();
            this.heapSize = heapSize;

        }

        private ComponentOutput getComponentOutput(WorkflowComponentInstanceItem thisComponentInstance, Element componentNode,
                Long workflowId, Boolean executionFlag, Boolean isWorkflowSaved, List<ComponentNode> expectedInputs,
                String remoteServer, MachineManager mm, Long heapSize) {
            ComponentOutput componentOutput = null;

            // Execute or validate via a call to processComponent
            if (thisComponentInstance != null && !thisComponentInstance.getState()
                    .equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                        && !thisComponentInstance.getState()
                    .equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
                // Execute or pre-process component.
                componentOutput = processComponent(
                    componentNode, workflowId, executionFlag, isWorkflowSaved, thisComponentInstance,
                        expectedInputs, remoteServer, mm, heapSize);
            } else {
                // Do not execute since it is not dirty.
                componentOutput = processComponent(
                    componentNode, workflowId, false, isWorkflowSaved, thisComponentInstance,
                        expectedInputs, remoteServer, mm, heapSize);
            }
            return componentOutput;
        }

        @Override
        public void run() {

            componentOutput = getComponentOutput(thisComponentInstance, componentNode, workflowId,
                    executionFlag, isWorkflowSaved, expectedInputs, remoteServer, mm, heapSize);
            outputMappingByComponentId.put(componentId, componentOutput);

            processedList.add(componentId);
        }
    }

    class ConsumerThread implements Runnable {
        Long workflowId;
        Boolean executionFlag;
        Boolean isWorkflowSaved;
        List<Element> componentNodes;
        Element outputMapping = null;
        ComponentOutput componentOutput = null;
        Boolean isSaveAsNew;

        public void init(Long workflowId, Boolean executionFlag,
                Boolean isWorkflowSaved, List<Element> componentNodes, Boolean isSaveAsNew, Long heapSize) {
            this.workflowId = workflowId;
            this.executionFlag = executionFlag;
            this.isWorkflowSaved = isWorkflowSaved;
            this.componentNodes = componentNodes;
            this.isSaveAsNew = isSaveAsNew;
        }

        @Override
        public void run() {
            processComponentsConsumer(workflowId, componentNodes, executionFlag, isWorkflowSaved, isSaveAsNew);
        }
    }


    public List<String> scanComponentLogs(String path) {
        List<String> messageList = new ArrayList<String>();

        FileSearch finder = new FileSearch();
        List<File> files = finder.find(Paths.get(path));

        for (File file : files) {
            // Check to ensure it is a file, since the finder returns directories, too
            if (file.exists() && file.isFile()
                    && (file.getName().matches("component_output_.*\\.txt")
                    || file.getName().matches("WorkflowComponent.log")
                    || file.getName().matches(".*\\.wfl"))) {
                try {

                    if (file != null && file.exists() && file.canRead()) {
                        String mimeType = WorkflowFileUtils.getMimeType(file);
                        logger.trace("Adding output file with type: " + mimeType);
                        Integer extIndex = file.getName().lastIndexOf(".");
                        String ext = null;
                        if (extIndex >= 0 && extIndex < file.getName().length() - 1) {
                            ext = file.getName().substring(extIndex + 1);
                        }

                        if (ext != null && ext.matches("[a-zA-Z0-9_\\-]+")) {
                            //addOutputFile(file, nodeIndex, fileIndex, ext);
                        } else if (mimeType != null) {
                            String[] comboText = mimeType.split("/");
                            String label = "file";
                            if (comboText.length > 1) {
                                label = comboText[1];
                            }

                            //addOutputFile(file, nodeIndex, fileIndex, label);
                        } else {
                            String label = "file";
                            //addOutputFile(file, nodeIndex, fileIndex, label);
                        }

                        FileReader fReader;
                        BufferedReader bReader = null;

                        if (file != null && file.exists() && file.isFile()) {
                            try {

                                fReader = new FileReader(file);
                                bReader = new BufferedReader(fReader);
                                String line = null;
                                while ((line = bReader.readLine()) != null) {
                                    if (line.contains("WARN:")) {
                                        String cutLine = line.substring(line.lastIndexOf("WARN:"));
                                        messageList.add(cutLine);
                                    } else if (line.contains("WARNING:")) {
                                        String cutLine = line.substring(line.lastIndexOf("WARNING:"));
                                        messageList.add(cutLine);
                                    }
                                }


                            } catch (FileNotFoundException e) {
                                logger.error("File not found: " + file.getAbsolutePath());
                            } catch (IOException e) {
                                logger.error("IOException for file: " + file.getAbsolutePath());
                            } finally {
                                if (bReader != null) {
                                    try {
                                        bReader.close();
                                    } catch (IOException e) {
                                        logger.error("Could not close reader for file: " + file.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("\nError reading file: " + path);
                }
            }
        }


        return messageList;
    }

    /**
     * FileSearch class to return all files and directories under a given path.
     * @author mkomisin
     *
     */
    private class FileSearch
        extends SimpleFileVisitor<Path> {
        /** Debug logging. */
        protected Logger logger = Logger.getLogger(getClass().getName());

        List<File> fileList;

        /**
         * Default constructor.
         */
        FileSearch() {
            logger.trace("FileSearch is searching for files.");
            fileList = new ArrayList<File>();
        }

        /**
         * Return all files and directories under a given path.
         * @param path the path
         * @return all files and directories under a given path
         */
        public List<File> find(Path path) {

            if (path != null) {
                File file = path.toFile();

                // The finder returns files and directories.
                if (file.exists() && file.isDirectory()) {
                    File[] list = file.listFiles();
                    for (File child : list) {
                        find(Paths.get(child.getAbsolutePath()));
                    }
                } else {
                    fileList.add(file);
                }


            }
            return fileList;
        }

        @Override
        public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) {
            find(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

}
