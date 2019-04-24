/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import edu.cmu.pslc.datashop.servlet.workflows.WorkflowProcessHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowXmlUtils;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ComponentHierarchyHelper;
import edu.cmu.pslc.datashop.servlet.workflows.ConnectionHelper;
import edu.cmu.pslc.datashop.servlet.workflows.MachineManager;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowFileUtils;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.servlet.workflows.WorkflowImportHelper;
import edu.cmu.pslc.datashop.util.FileUtils;

public abstract class AbstractComponent {

    public static final Long DEFAULT_INITIAL_HEAP_SIZE = 1024L;

    private static final String XML_HEADER_UTF8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String DEFAULT_PROPS_DIR = "/datashop/dataset_files/workflows";

    private Long initialHeapSize = null;

    /** Debug logging. */
    protected Logger logger = Logger.getLogger(getClass().getName());

    /** The worflow item. */
    protected WorkflowItem workflowItem;
    /** The workflow id. */
    protected Long workflowId;
    /** The component id, e.g. Analysis1. */
    protected String componentId;
    /** The human readable component id, e.g. Analysis #1. */
    protected String componentIdHumanReadable;
    /** The component type, e.g. Analysis, Import, etc. */
    protected String componentType;
    /** The component name, e.g. StudentStep, StudentStepDB, basically the component sub-type. */
    protected String componentName;

    /** The directory for which this component should be allowed write access. */
    protected String componentOutputDir;
    /** The parent directory of the componentOutputDir. */
    private String workflowDir = null;
    /** The path to the schema which defines this component. */
    private String schemaPath;
    /** The component XML used to store option and input values. */
    private String componentXmlFilePath = null;

    /** The path to the interpreter, if one exists. */
    private String interpreterPath;
    /** The path to the external tool, if one exists. */
    private String toolPath;
    /** The path to the external tool directory. */
    private String toolDir;
    /** The user id. */
    private String userId;
    /** The execution flag. False means pre-process only. */
    private Boolean preprocessFlag;

    /** The component XML element. */
    protected Element component;

    /** Private options. **/
    protected Element privateOptions;

    /** Is the component valid and initialized. */
    protected Boolean isInitialized;

    /** The child element containing the options, if any. */
    protected Element componentOptions;

    /** The output meta data XML, i.e. non-file parameters. */
    protected Element optionMetadataXml;

    /** The option file types (i.e., types of file uploads, if any). */
    protected Map<String, String> optionTypes;

    /** A map of file input indices to their corresponding property maps. */
    private Map<Integer, Map> inputDataMap;

    /** The input XML elements. */
    protected Map<Integer, List<Element>> inputXml;

    /** The input meta data XML, i.e. non-file parameters. */
    protected Element inputMetadataXml;

    /** The component input file list. */
    protected Map<Integer, List<File>> inFiles;

    /** The input file types. */
    protected Map<Integer, String> inFileTypes;

    /** The input file metadata. */
    protected Map<Integer, Element> inFileMetadata;
    /** The option file metadata. */
    protected Map<Integer, Element> optionFileMetadata;

    /** The output XML. */
    protected Map<Integer, Element> outputXml;

    /** The file output XML. */
    protected Map<Integer, Element> fileOutputXml;
    /** The component output file map. */
    protected Map<Integer, List<File>> outFiles;
    /** The output file types. */
    protected Map<String, String> outFileTypes;

    /** A list of error messages. */
    protected List<String> errorMessages;
    /** A list of other messages. */
    protected List<String> genericMessages;

    /** The decimal format for elapsed seconds. */
    private DecimalFormat elapsedTimeFormat;
    /** The time in seconds it takes to initialize and complete execution of this component. */
    private Double elapsedSeconds = new Double(0);
    /** The execution start time. */
    private Long startTime;
    /** The os.name property. */
    String osName = null;
    /** White space pattern. */
    protected String WHITE_SPACE_PATTERN = "[\\s\t\r\n]";
    /** Name of the import component */
    public static final String IMPORT_COMPONENT_NAME = "import";
    /** String used to mark component progress messages */
    public final String COMPONENT_PROGRESS_PREPEND = "%Progress::";

    Element outputMapping;
    List<String> outputInstances;


    /** Constructor. */
    protected AbstractComponent() {

        preprocessFlag = false;
        isInitialized = false;

        errorMessages = new ArrayList<String>();
        genericMessages = new ArrayList<String>();
        inputXml = new HashMap<Integer, List<Element>>();

        elapsedTimeFormat = new DecimalFormat("0");
        startTime = System.currentTimeMillis();

        inFiles = new HashMap<Integer, List<File>>();

        inFileTypes = new HashMap<Integer, String>();
        inFileMetadata = new HashMap<Integer, Element>();
        optionFileMetadata = new HashMap<Integer, Element>();
        inputDataMap = new HashMap<Integer, Map>();

        outFiles = new HashMap<Integer, List<File>>(); ///
        outFileTypes = new HashMap<String, String>();

        outputMapping = new Element("inputs");
        outputInstances = new ArrayList<String>();

        initialHeapSize = DEFAULT_INITIAL_HEAP_SIZE;

        WorkflowProcessHelper.nonceList = new ConcurrentLinkedQueue<String>();
        WorkflowProcessHelper.wfProprietaryProcessMap = Collections.synchronizedMap(new HashMap<String, Process>());
        WorkflowProcessHelper.wfProcessMap = Collections.synchronizedMap(new HashMap<String, Process>());

        // Custom log4j.properties
        Properties props = new Properties();
        try {
            // Try to find the log4j.properties file in this directory, first (for ant runComponent).
            File propsFile = new File("log4j.properties");
            // If we're on a dev machine, try using the relative path.
            if (!propsFile.exists()) {
                propsFile = new File("../../../log4j.properties");
            }
            // If none exists, use the vm/server path
            if (!propsFile.exists()) {
                propsFile = new File(DEFAULT_PROPS_DIR + "/log4j.properties");
            }

            props.load(new FileInputStream(propsFile));
        } catch (Exception e) {
            System.err.println("Could not find the log4j.properties file.");
        }

        PropertyConfigurator.configure(props);

        osName = System.getProperty("os.name").toLowerCase();

        privateOptions = new Element("privateOptions");
    }


    /**
     * Look for a file named build.properties in the current directory, if
     * it exists, and load properties from it.
     */
    private void loadBuildProperties() {
        String filename = this.getToolDir() + "build.properties";
        File propsFile = new File(filename);

        if (propsFile.exists()) {
            try {
                System.getProperties().load(new FileInputStream(filename));
                logger.trace("System properties: ");

                for (Object propertyKey : System.getProperties().keySet()) {
                    logger.trace("\t" + propertyKey + " = " + System.getProperty((String) propertyKey));
                }


            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    /**
     * Get the Component Type for this component.
     * @return the Component Type
     */
    public String getComponentType() {
        return componentType;
    }


    /**
     * Get the human readable (configurable) id.
     * @return the Component Type
     */
    public String getIdHuman() {
        return componentIdHumanReadable;
    }


    /**
     * Get the Component Name for this component.
     * @return the Component Name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Get the Component Name for this component.
     * @return the Component Name
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Add an error message.
     * @param message the error message
     */
    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }


    /**
     * Get error messages.
     * @return the error messages
     */
    List<String> getErrorMessages() {
        return errorMessages;
    }


    /**
     * Add an generic message.
     * @param message the generic message
     */
    public void addGenericMessage(String message) {
        genericMessages.add(message);
    }

    /**
     * Get generic messages.
     * @return the generic messages
     */
    List<String> getGenericMessages() {
        return genericMessages;
    }


    /**
     * @return the toolDir
     */
    public String getToolDir() {
        return toolDir;
    }


    /**
     * @param toolDir the toolDir to set
     */
    public void setToolDir(String toolDir) {
        this.toolDir = toolDir;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }


    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the schemaPath
     */
    public String getSchemaPath() {
        return schemaPath;
    }


    /**
     * @param schemaPath the schemaPath to set
     */
    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }


    /**
     * @return the interpreterPath
     */
    public String getInterpreterPath() {
        return interpreterPath;
    }


    /**
     * @param interpreterPath the interpreterPath to set
     */
    public void setInterpreterPath(String interpreterPath) {
        this.interpreterPath = interpreterPath;
    }


    /**
     * @return the toolPath
     */
    public String getToolPath() {
        return toolPath;
    }


    /**
     * @param toolPath the toolPath to set
     */
    public void setToolPath(String toolPath) {

        if (osName.indexOf("win") >= 0) {
            this.toolPath = toolPath;
        } else {
            this.toolPath = toolPath;
        }
    }

    /**
     * @return the componentOutputDir
     */
    public String getComponentOutputDir() {
        return componentOutputDir;
    }


    /**
     * @return the elapsedSeconds
     */
    public Double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Returns the path to the shared applicationContext.xml file for components.
     * that need to access the server-side database.
     * @return the path to the shared applicationContext.xml file for components
     */
    public String getApplicationContextPath() {
        String toolDirParent = new File(this.getToolDir()).getParent();
        String  workflowsCommonLibsDir = WorkflowFileUtils.getStrictDirFormat(
                toolDirParent) + "/CommonLibraries/applicationContext.xml";
        logger.info("Reading username and password from application context: " + workflowsCommonLibsDir);
        return workflowsCommonLibsDir;
    }

    public void startComponent(String[] args) {

        if (args != null) {
            handleOptions(args);
        }

        logger.info("Initializing workflow component.");
        init(null, null);

        logger.info("Validating workflow component.");
        if (validate()) {

            logger.info("Testing workflow component.");
            if (this.test()) {
                logger.info("Executing workflow component.");
                // This run method uses the overriden run() method or an empty placeholder
                // If only the external program is used by the component.
                try {
                    if (preprocessFlag) {
                        System.out.println(this.getOutput());
                    } else {
                        this.runComponent();
                    }
                } catch (Exception e) {
                    logger.error("Exception in component program: " + this.getOutput());
                    logger.error(e.toString());
                } finally {
                    WorkflowProcessHelper.markProcessCompleted(this.componentId);
                }

            }
        }

    }

    public void init(String componentXml, WorkflowComponentInstanceItem thisComponentInstance) {
        try {
            loadBuildProperties();

            logger.trace("Attempting to read XML file: " + componentXmlFilePath);

            if (componentXml == null || thisComponentInstance == null || !thisComponentInstance.isDirty()) {
                componentXml = WorkflowFileUtils.readFile(componentXmlFilePath);
            }

            if (componentXml != null && !componentXml.isEmpty()) {
                this.component = WorkflowXmlUtils.getStringAsElement(componentXml);


                //logger.info("Initializing component XML: " + componentXml);
                if (this.component != null) {

                    if (schemaPath != null) {
                        this.schemaPath = schemaPath;
                        File schemaFile = new File(this.schemaPath);
                        if (schemaFile != null && schemaFile.isFile()) {
                            if (!schemaFile.canRead()) {
                                errorMessages.add("Cannot read schema file: " + schemaFile.getAbsolutePath());
                            }
                        } else {
                            errorMessages.add("Schema not found: " + this.schemaPath);
                        }
                    } else {
                        errorMessages.add("Schema path is null.");
                    }
                } else {
                    errorMessages.add("Component XML is not well-formed");
                }
            }

            File toolDirFile = new File(toolDir);
            if (!toolDirFile.exists() || !toolDirFile.isDirectory()) {
                errorMessages.add("The tool directory does not exist.");
            } else if (!toolDirFile.canExecute() || !toolDirFile.canRead()) {
                errorMessages.add("The tool directory does not have the proper permissions.");
            }

            String toolDirParent = new File(this.getToolDir()).getParent();
            String  tableTypesFile = WorkflowFileUtils.getStrictDirFormat(
                    toolDirParent) + "/CommonSchemas/TableTypes.xsd";

            isInitialized = true;
            outputXml = new HashMap<Integer, Element>();
            fileOutputXml = new HashMap<Integer, Element>();
            inputMetadataXml = new Element("inputmeta");
            optionMetadataXml = new Element("optionmeta");

            Element workflowIdElement = component.getChild("workflow_id");
            if (workflowIdElement.getTextTrim() != null && workflowIdElement.getTextTrim().matches("[0-9]+")) {
                this.workflowId = Long.parseLong(workflowIdElement.getTextTrim());


                /** Component is indexed by the component type and name. Each instance has a unique id. */

                Element componentIdElement = component.getChild("component_id");
                this.componentId = componentIdElement.getTextTrim();

                Element componentIdHumanReadableElement = component.getChild("component_id_human");
                this.componentIdHumanReadable = componentIdHumanReadableElement.getTextTrim();

                Element componentTypeElement = component.getChild("component_type");
                this.componentType = componentTypeElement.getTextTrim();

                Element componentNameElement = component.getChild("component_name");
                this.componentName = componentNameElement.getTextTrim();

                logger.trace("Initializing component inputs.");
                /** Get the input XML (one or more "input" elements) */
                Element inputs = (Element) component.getChild("inputs");
                List<Element> inputList = null;
                if (inputs != null && inputs.getChildren() != null) {
                    // Regardless of name, all children of inputs are considered to be an "input" element.
                    for (Element input : (List<Element>) inputs.getChildren()) {
                        if (input.getChildren() != null) {
                            Element clonedInput = (Element)input.clone();
                            // Get the index if one exists
                            Integer inputIndexStr = 0;
                            if (clonedInput.getName().matches("[a-zA-Z]+\\d+")) {
                                inputIndexStr = Integer.parseInt(clonedInput.getName().replaceAll("[a-zA-Z]+", ""));
                            }
                            clonedInput.setName("input" + inputIndexStr.toString());
                            if (inputXml.containsKey(inputIndexStr)) {
                                inputList = (List<Element>) inputXml.get(inputIndexStr);
                                inputList.add(input);
                            } else {
                                inputList = new ArrayList<Element>();
                                inputList.add(input);
                            }
                            inputXml.put(inputIndexStr, inputList);
                        }
                    }
                }

                this.componentOptions = (Element) component.getChild("options");
                logger.info("Initializing component options.");
                //logger.info("WFXML: " + ComponentXmlHandler.getElementAsString(component));

                if (thisComponentInstance != null && !thisComponentInstance.getDirtyFile()
                    && !this.componentName.equalsIgnoreCase(IMPORT_COMPONENT_NAME)) {

                    logger.info("Dirty bits set. Creating new options.");
                    Boolean isDirtySelection = thisComponentInstance.getDirtySelection()
                        && !thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                        && !thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN);
                    Boolean isDirtyAncestor = thisComponentInstance.getDirtyAncestor();
                    Boolean isCompleted = thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                            || thisComponentInstance.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN);
                    // Handle the component option creation such that components without explicit changes
                    // will choose the best defaults, based on the default attributes in each option's XSD.
                    Element newOptions = ComponentHelper
                            .createComponentOptions(workflowId, this.component, workflowDir, tableTypesFile, null, componentId, componentName,
                                    isDirtySelection, isDirtyAncestor, isCompleted);
                    logger.debug("createComponentOptions: " + WorkflowXmlUtils.getElementAsString(newOptions));
                    this.componentOptions = newOptions;
                    if (component.getChild("options") != null) {
                        component.removeChild("options");
                    }


                    component.addContent(this.componentOptions);

                } else {
                    logger.info("Using existing component options.");
                }


                // Initialize the output element with identifying information.
                WorkflowComponentItem dummyWfComponentItem = new WorkflowComponentItem();
                dummyWfComponentItem.setComponentName(componentName);
                dummyWfComponentItem.setComponentType(componentType);
                dummyWfComponentItem.setSchemaPath(schemaPath);
                List<WorkflowComponentItem> dummyList = new ArrayList<WorkflowComponentItem>();
                dummyList.add(dummyWfComponentItem);

                JSONObject jsonOutputEndpoints = null;
                try {

                    jsonOutputEndpoints = ConnectionHelper
                            .getEndpointsNoDao(dummyList, ConnectionHelper.XML_OUTPUT_DEFS);


                    // Get output endpoints to initialize component.

                    String endpointsKey = componentTypeElement.getTextTrim().toLowerCase()
                        + "-" + componentNameElement.getTextTrim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
                    if (jsonOutputEndpoints != null && jsonOutputEndpoints.has(endpointsKey)) {

                        JSONObject jsonTypeList = jsonOutputEndpoints.getJSONObject(endpointsKey + "_list");

                            for (Iterator<Object> iter = jsonTypeList.keys(); iter.hasNext(); ) {
                                String k = (String) iter.next();
                                if (k.matches("\\d+")) {
                                    Integer outIndex = Integer.parseInt(k);
                                    Element outputNode = new Element("output" + outIndex);
                                    Element outputIdElement = (new Element("component_id"))
                                            .addContent(componentId);
                                    outputNode.addContent(outputIdElement);

                                    Element outputHumanIdElement = (new Element("component_id_human"))
                                            .addContent(componentIdHumanReadable);
                                    outputNode.addContent(outputHumanIdElement);

                                    Element outputTypeElement = (new Element("component_type"))
                                            .addContent(componentType);
                                    outputNode.addContent(outputTypeElement);

                                    Element outputNameElement = (new Element("component_name"))
                                            .addContent(componentName);
                                    outputNode.addContent(outputNameElement);

                                    outputXml.put(outIndex, outputNode);
                                }
                            }

                    }
                } catch (JSONException e1) {
                    logger.error("Could not parse component endpoints data (JSON). " + e1.toString());
                }

                if (workflowDir != null) {
                    this.componentOutputDir = workflowDir + "/" + componentId + "/output/";
                    try {
                        WorkflowFileUtils.createDirectoriesWithPermissions(Paths.get(componentOutputDir));
                    } catch (IOException e) {
                        logger.error("Could not create required directories.");
                    }

                }

                List<Element> singleElementAsList = new ArrayList<Element>();
                singleElementAsList.add(this.componentOptions);
                addInputFiles(null, singleElementAsList, true);

                // Add files and data found (files, inputs, and options of the previous component)
                if (inputXml != null && inputXml.size() > 0) {
                    for (Integer inputIndex : this.inputXml.keySet()) {
                        // Files
                        addInputFiles(inputIndex, inputXml.get(inputIndex), false);
                        // Simple data types (from input and options)
                        addSimpleDataTypes(inputIndex, inputXml.get(inputIndex), "inputmeta");
                        addSimpleDataTypes(inputIndex, inputXml.get(inputIndex), "optionmeta");

                        inputIndex++;
                    }
                }
                /** For debugging purposed. */
                for (Integer key : inputDataMap.keySet()) {
                    logger.trace("KEY: " + key);
                    Map<String, Object> map = inputDataMap.get(key);
                    if (!map.isEmpty()) {
                        for (String propertyKey : map.keySet()) {
                            logger.trace("Prop: " + propertyKey + ", " + map.get(propertyKey));
                        }
                    }
                }

                logger.trace("Creating component: component_id = " + componentIdHumanReadable
                    + ", component_type = " + componentType
                        + ", component_name = " + componentName);


                testAttachments();


                parseInputs();
                parseOptions();

                processInputs();
                processOptions();
                logger.trace("End of preliminary component initialization.");


                logger.trace("Finished component initialization.");

            }
        } catch (Exception e) {
            this.addErrorMessage(e.toString());
        }
    }

    /**
     * Validate this component's XML.
     * @return true if valid; false, otherwise.
     */
    public Boolean validate() {
        Boolean isValid = false;
        if (componentXmlFilePath != null && toolDir != null) {
            if (this.component != null && schemaPath != null) {
                isValid = validateXml();
                logger.trace("Is valid XML? : " + isValid);
            }
        } else {
            errorMessages.add("The Component XML or tool directory were not found.");
        }
        for (String err : errorMessages) {
            logger.error(err);
        }
        return isValid;
    }


    private void addInputFiles(Integer inputIndex, List<Element> parents, Boolean isOptionFile) {

        if (inputIndex == null) {
            inputIndex = 0;
        }
        for (Element parent : parents) {
            if (parent != null && parent.getChild("files") != null && parent.getChild("files").getChildren() != null) {
                logger.trace("Found files element.");
                for (Element fileElement : (List<Element>) parent.getChild("files").getChildren()) {

                    // Each input must contain a file element and a model element to be valid.
                    if (fileElement != null
                        && fileElement.getChild("file_path") != null
                            && fileElement.getChild("file_name") != null) {
                        // fileElement.getChildTextTrim("label");
                        // fileElement.getChildTextTrim("index");

                        File existingFile = new File(fileElement.getChild("file_path").getTextTrim());

                        // File was uploaded directly through workflow GUI
                        if (existingFile != null && existingFile.isFile() && existingFile.canRead()) {
                            List<File> existingFiles = null;
                            if (inFiles.containsKey(inputIndex)) {
                                existingFiles = inFiles.get(inputIndex);
                            } else {
                                existingFiles = new ArrayList<File>();
                            }
                            existingFiles.add(existingFile);

                            inFiles.put(inputIndex, existingFiles);
                            inFileTypes.put(inputIndex,
                                    fileElement.getName());

                            Element metadata = fileElement.getChild("metadata");
                            if (metadata == null) {
                                metadata = new Element("metadata");
                            }

                            // mck todo1
                            if (isOptionFile) {
                                optionFileMetadata.put(inputIndex, metadata);
                            } else {
                                inFileMetadata.put(inputIndex, metadata);
                            }

                        }
                    }
                }
            }
        }

    }


    private void addSimpleDataTypes(int index, List<Element> inputs, String elementName) {

        for (Element input : inputs) {
            if (input != null && input.getChildren() != null) {
                if (input.getChild("component_id") != null) {
                    //input.getChild("component_id").getTextTrim();
                    Map<String, Object> componentDataMap = new HashMap<String, Object>();
                    if (input.getChild(elementName) != null
                            && input.getChild(elementName).getChildren() != null) {
                        for (Element inputElement : (List<Element>) input.getChild(elementName).getChildren()) {
                            logger.trace("Input datatype found: " + inputElement.getName());
                            // Get each non files field
                            if (!inputElement.getName().matches("files")
                                    && !inputElement.getName().matches("input[0-9]*")) {

                                componentDataMap.put(inputElement.getName(), inputElement.getTextTrim());
                            }
                        }
                        inputDataMap.put(index, componentDataMap);
                    }
                }
            }
        }
    }


    /**
     * Validate this component's xml against its schema.
     * @return true if valid XML, false otherwise
     */
    private synchronized Boolean validateXml() {
        Boolean isValidXml = false;
        if (this.component != null) {
            // Output the newly created workflow XML

            // The default encoding is UTF-8.
            Format format = Format.getPrettyFormat();
            XMLOutputter xmlo = new XMLOutputter(format);
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();


                xmlo.output(this.component, out);
                String componentXml = XML_HEADER_UTF8 + out.toString();
                SchemaFactory factory = SchemaFactory
                        .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = null;
                try {

                    schema = factory.newSchema(new File(this.schemaPath));

                    final javax.xml.validation.Validator validator = schema.newValidator();
                    validator.validate(new StreamSource(new StringReader(componentXml)));
                    isValidXml = true;
                    out.close();

                } catch (SAXException e) {
                    if (this.componentName.equalsIgnoreCase(IMPORT_COMPONENT_NAME)) { //Kinda Hacky TODO fix
                        isValidXml = true;
                    } else {
                        String xmlErrorMessage = e.toString();
                        errorMessages.add(xmlErrorMessage);
                    }
                } catch (final IOException e)  {
                    String xmlErrorMessage = e.toString();
                    logger.error(xmlErrorMessage);
                    errorMessages.add(xmlErrorMessage);
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) { }
                    }
                }

            } catch (IOException e) {
                String errorMessage = e.toString();
                if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                    this.addErrorMessage(errorMessage);
                    logger.error("validateComponentXml :: " + errorMessage);
                }
            }
        }
        return isValidXml;


    }

    public void setOutputMapping(Element outputMapping) {
        this.outputMapping = outputMapping;
    }
    public Element getOutputMapping() {
        return outputMapping;
    }

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     */
    public void handleOptions(String[] args) {
        logger.info("Command-line args: " + Arrays.toString(args));
        if (args == null) {
            return;
        }


        ArrayList<String> argsList = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        String argument;

        // loop through the arguments
        for (int i = 0; i < args.length; ) {

            argument = args[i].trim();


            if (argument.equals("-componentXmlFile")) {
                if (++i < args.length) {
                    this.componentXmlFilePath = args[i];
                    logger.trace("Setting argument: " + argument + " = " + args[i]);

                } else {
                    errorMessages.add("No file path given for option -componentXmlFile.");
                }

            } else if (argument.equals("-workflowDir")) {
                if (++i < args.length) {
                    this.workflowDir = args[i];
                    logger.trace("Setting argument: " + argument + " = " + args[i]);

                } else {
                    errorMessages.add("No file path given for option -workflowDirectory.");
                }
            } else if (argument.equals("-userId")) {
                if (++i < args.length) {
                    this.userId = args[i];
                    logger.trace("Setting argument: " + argument + " = " + args[i]);

                } else {
                    errorMessages.add("No file path given for option -userId.");
                }
            } else if (argument.equals("-schemaFile")) {
                if (++i < args.length) {
                    this.schemaPath = args[i];
                    logger.trace("Setting argument: " + argument + " = " + args[i]);
                } else {
                    errorMessages.add("No file path given for option -schemaFile.");
                }
            } else if (argument.equals("-toolDir")) {
                if (++i < args.length) {
                    this.toolDir = args[i];
                    logger.trace("Setting argument: " + argument + " = " + args[i]);

                } else {
                    errorMessages.add("No file path given for option -toolDir.");
                }
            } else if (argument.equals("-preprocess")) {
                this.preprocessFlag = true;
                i++;
            } else {
                // Discard any other options given to this program
                i++;
            }
        }



    }



    protected void processInputs() {

    }

    protected void processOptions() {

    }


    protected void parseInputs() {

    }


    protected void parseOptions() {

    }

    public static String META_DATA = "metadata";
    public static String META_DATA_HEADER = "header";
    public static String META_DATA_LABEL = "label";

    /**
     * Add meta-data, such as a column header, to the validation output of a particular node.
     * @param outputNodeIndex the file (output node) index to which we shall add the meta-data
     * @param metaDataType the meta-data type ('header' is the only type, currently)
     * @param id a meta-data instance identifier that must be compliant with HTML id's
     * @param index the integer index of the meta-data instance
     * @param name the name or value of the meta-data instance
     */
    protected void addMetaData(String fileType, Integer outputNodeIndex, String metaDataType, String id, Integer index, String name) {

        Element metaChild = new Element(metaDataType);
        Element idElement = new Element("id");
        idElement.setText(id);
        Element indexElement = new Element("index");
        indexElement.setText(index.toString());
        Element nameElement = new Element("name");
        nameElement.setText(name);
        idElement.setText(id);
        metaChild.addContent(idElement);
        metaChild.addContent(indexElement);
        metaChild.addContent(nameElement);


        logger.trace("Files element is empty.");
        Element metadata = new Element("metadata");
        metadata.addContent(metaChild);
        Element newFile = new Element(fileType);
        newFile.addContent((new Element("index")).setText(outputNodeIndex.toString()));
        newFile.addContent((new Element("label")).setText(fileType));

        Boolean headerExists = false;
        Element prevMetadataElement = null;
        Boolean outputFound = false;
        for (Integer nodeKey : fileOutputXml.keySet()) {
            Element fileOutputXmlElement = fileOutputXml.get(nodeKey);
            if (fileOutputXmlElement.getChildren() != null && !fileOutputXmlElement.getChildren().isEmpty()) {
                // Get all of the output node's files
                for (Element fileChild : (List<Element>) fileOutputXmlElement.getChildren()) {
                    if (fileChild.getChildTextTrim("index").equals(outputNodeIndex.toString())) {
                        outputFound = true;
                        if (fileChild.getChild("metadata") != null && fileChild.getChild("metadata").getChildren() != null) {
                            prevMetadataElement = fileChild.getChild("metadata");
                            for (Element child : (List<Element>)prevMetadataElement.getChildren()) { // mck todo: detect conflicting metadata headers/labels
                                if (child.getChildTextTrim("name") != null) {
                                    String optionValue = child.getChildTextTrim("name");
                                            //.replaceAll("Input_[0-9]+_Column_[0-9]+_", "");
                                    if (optionValue.equalsIgnoreCase(name)) {
                                        headerExists = true;
                                        child.setName(metaDataType);
                                        if (child.getChild("id") != null) {
                                            child.getChild("id").setText(id);
                                        } else {
                                            child.addContent(new Element("id").setText(id));
                                        }
                                    }
                                }
                            }
                            if (!headerExists) {
                                prevMetadataElement.addContent((Element)metaChild.clone());
                            }
                        } else {
                            fileChild.addContent(metadata);
                        }
                    }

                }
            }
        }

        if (!outputFound) {
            Element newFiles = new Element("files");
            newFile.addContent(metadata);
            newFiles.addContent(newFile);
            fileOutputXml.put(outputNodeIndex, newFiles);
        }
    }

    /**
     * Adds header data from a specific input node to a specific output node.
     * @param fileType the output file type, e.g. tab-delimited, csv, student-step, etc.
     * @param inputNodeIndex the input node index associated with the InFileList definitions
     * @param outputNodeIndex the output node index associated with the OutFileList definitions
     * @param name a regular expression used to be selective when choosing which metadata to add based
     * on the "<name>" element; for example, .* will match all header and labels regardless of their name value
     */
    protected void addMetaDataFromInput(String fileType, Integer inputNodeIndex, Integer outputNodeIndex, String name) {

        Element metadata = new Element("metadata");
        ///Map<Integer, Element> outputMetadata = new HashMap<Integer, Element>();

        logger.trace("Process file input for metadata.");
        for (Integer inputIndex : this.inputXml.keySet()) {
            if (inputIndex == inputNodeIndex) {
                List<Element> inputElements = inputXml.get(inputIndex);
                for (Element inputElement : inputElements) {

                    if (inputElement.getChild("files") != null && inputElement.getChild("files").getChildren() != null) {
                        for (Element filesChild : (List<Element>) inputElement.getChild("files").getChildren()) {
                            if (filesChild.getChild("metadata") != null) {
                                Element inMetaElement = filesChild.getChild("metadata");
                                if (inMetaElement != null && !inMetaElement.getChildren().isEmpty()) {
                                    for (Element child : (List<Element>) inMetaElement.getChildren()) {
                                        if (child.getChild("name") != null
                                            && child.getChild("index") != null
                                                && child.getChild("id") != null) {
                                            String colLabel = child.getChildTextTrim("name");
                                            if (colLabel != null && colLabel.matches(name)) {
                                                metadata.addContent((Element)child.clone());
                                            }
                                        }
                                    }
                                }
                                break; // we only get metadata from one of the objects for now.. more code required to handle them separately
                            }
                        }
                    }
                }




            }
        }


        Element newFile = new Element(fileType);
        newFile.addContent((new Element("index")).setText(outputNodeIndex.toString()));
        newFile.addContent((new Element("label")).setText(fileType));

        Boolean addDummyFile = true;
        Boolean outputFound = false;
        if (fileOutputXml.containsKey(outputNodeIndex)) {
            Element fileOutputXmlElement = fileOutputXml.get(outputNodeIndex);
            if (fileOutputXmlElement.getChildren() != null && !fileOutputXmlElement.getChildren().isEmpty()) {
                // Get all of the output node's files
                for (Iterator<Element> iterFileChild = fileOutputXmlElement.getChildren().iterator(); iterFileChild.hasNext(); ) {
                    Element fileChild = iterFileChild.next();
                    Integer existingFileIndex = null;
                    if (fileChild.getChild("index") != null && fileChild.getChildTextTrim("index").matches("\\d+")) {
                        existingFileIndex = Integer.parseInt(fileChild.getChildTextTrim("index"));
                    }
                    if (outputNodeIndex.equals(existingFileIndex)) {
                        outputFound = true;
                        addDummyFile = false;
                        Element newMetaElement = new Element("metadata");

                        Element inMetaElement = fileChild.getChild("metadata");
                        if (inMetaElement != null && !inMetaElement.getChildren().isEmpty()) {

                            for (Iterator<Element> iterInMetaElement = inMetaElement.getChildren().iterator(); iterInMetaElement.hasNext(); ) {
                                Element newChild2 = iterInMetaElement.next();
                                newMetaElement.addContent((Element)newChild2.clone());
                            }

                            for (Iterator<Element> iterNewChild = metadata.getChildren().iterator(); iterNewChild.hasNext(); ) {
                                Element newChild = iterNewChild.next();
                                Boolean elementExists = false;
                                if (newChild.getChild("name") != null
                                    && newChild.getChild("index") != null
                                        && newChild.getChild("id") != null) {
                                    for (Iterator<Element> iterInMetaElement = inMetaElement.getChildren().iterator(); iterInMetaElement.hasNext(); ) {
                                        Element newChild2 = iterInMetaElement.next();
                                        if (newChild2.getChild("name") != null
                                            && newChild2.getChild("index") != null
                                                && newChild2.getChild("id") != null) {
                                            if (!newChild.getChildTextTrim("name").equals(newChild2.getChildTextTrim("name"))
                                                && !newChild.getChildTextTrim("index").equals(newChild2.getChildTextTrim("index"))
                                                    && !newChild.getChildTextTrim("id").equals(newChild2.getChildTextTrim("id"))) {
                                                newMetaElement.addContent((Element)newChild.clone());
                                            }
                                        }
                                    }
                                    ///addMetadata = false;
                                }
                            }

                            if (!newMetaElement.getChildren().isEmpty()) {
                                inMetaElement.detach();
                                fileChild.removeChild("metadata");
                                fileChild.addContent(newMetaElement);

                            }

                        } else {
                            fileChild.addContent(metadata);

                        }
                    }
                }

                if (addDummyFile) {
                    Element newFiles = new Element("files");
                    newFile.addContent(metadata);
                    newFiles.addContent(newFile);
                    fileOutputXml.remove(outputNodeIndex);
                    fileOutputXml.put(outputNodeIndex, newFiles);
                }
            }
        } else {
            Element newFiles = new Element("files");
            newFile.addContent(metadata);
            newFiles.addContent(newFile);
            fileOutputXml.remove(outputNodeIndex);
            fileOutputXml.put(outputNodeIndex, newFiles);
        }




    }


    protected Boolean test() {
        return true;
    }

    protected Boolean testAttachments() {

        Boolean passing = true;
        if (inFiles != null && !inFiles.isEmpty()) {
            for (Integer inputIndex : inFiles.keySet()) {
                List<File> inFilesNode = (List<File>) inFiles.get(inputIndex);
                for (File inFile : inFilesNode) {
                    if (inFile == null
                            || !inFile.exists() || !inFile.isFile()) {
                        errorMessages.add("File " + inFile.getAbsolutePath() + " does not exist.");
                        logger.error("File " + inFile.getAbsolutePath() + " does not exist.");
                        passing = false;
                    } else if (!inFile.canRead()) {
                        errorMessages.add("File " + inFile.getAbsolutePath() + " cannot be read.");
                        logger.error("File " + inFile.getAbsolutePath() + " cannot be read.");

                        passing = false;
                    }
                }
            }
        }

        logger.trace("Attachments are valid? " + passing.toString());
        return passing;
    }

    /**
     * Run the component wrapper and return the PID.
     * @param componentXmlFile the component XML file
     * @param workflowDir the workflow directory
     * @param toolDir the tool directory
     * @param schemaFile the schema path
     * @param preprocessFlag whether or not this is a pre-processing call (not execution)
     * @return the PID of the jar
     */
    public void run(String userId, String componentXmlFile, String workflowDir,
        String toolDir, String schemaFile, List<String> inputComponentIds,
            List<String> inputComponentFiles, Boolean preprocessFlag, String remoteServer,
            Long heapSize, MachineManager mm) {

        String nonce = null;
        Element output = null;

        try {

            String replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
            String delimReplace = "$1";

            String initialHeapProperty = System.getProperty("initial.heap.mb");

            if (initialHeapProperty != null && initialHeapProperty.matches("[0-9]+")) {
                initialHeapSize = Long.parseLong(initialHeapProperty);
            }
            ArrayList<String> localJobParameters = new ArrayList<String>();
            ArrayList<String> processBuilderParameters = new ArrayList<String>();


            if (getInterpreterPath() != null && !getInterpreterPath().isEmpty()) {
                String interpreterPath = getInterpreterPath().replaceAll("\\\\ ", "<SPACE_CHAR>");
                for (String command : interpreterPath.split(" ")) {
                    localJobParameters.add(command.replaceAll("<SPACE_CHAR>", "\\\\ "));
                }
            }

            if (!getToolPath().matches(".*\\.jar")) {
                logger.error("Invalid component tool_path.");
                errorMessages.add("Invalid component tool_path.");
                return;
            }

            localJobParameters.add(getToolPath());

            // Handle OS-specific argument passing.
            String[] systemDependentArgs = new String[5];

            systemDependentArgs[0] = componentXmlFile.replaceAll(replaceChars, delimReplace);
            systemDependentArgs[1] = workflowDir.replaceAll(replaceChars, delimReplace);
            systemDependentArgs[2] = schemaFile.replaceAll(replaceChars, delimReplace);
            systemDependentArgs[3] = toolDir.replaceAll(replaceChars, delimReplace);
            systemDependentArgs[4] = userId.replaceAll(replaceChars, delimReplace);

            HashMap<String, String> processBuilderParametersMap = new HashMap<String, String>();
            if (remoteServer == null || preprocessFlag) {
                // Pass the arguments to the standalone component, then run it.
                processBuilderParameters.add("-componentXmlFile");
                processBuilderParameters.add(systemDependentArgs[0]);
                processBuilderParameters.add("-workflowDir");
                processBuilderParameters.add(systemDependentArgs[1]);
                processBuilderParameters.add("-schemaFile");
                processBuilderParameters.add(systemDependentArgs[2]);
                processBuilderParameters.add("-toolDir");
                processBuilderParameters.add(systemDependentArgs[3]);
                processBuilderParameters.add("-userId");
                processBuilderParameters.add(systemDependentArgs[4]);
                if (preprocessFlag) {
                    processBuilderParameters.add("-preprocess");
                }
            } else {
                processBuilderParametersMap.put("componentXmlFile", systemDependentArgs[0]);
                processBuilderParametersMap.put("workflowDir", systemDependentArgs[1]);
                processBuilderParametersMap.put("schemaFile", systemDependentArgs[2]);
                processBuilderParametersMap.put("toolDir", systemDependentArgs[3]);
            }

            ThreadedStreamReader inputReader = null;
            ThreadedStreamReader errorReader = null;
            List<String> inputLines = null;
            List<String> errorLines = null;

            ProcessBuilder processBuilder = new ProcessBuilder();
            Process process = null;
            File uploadFile = null;
            try {

                if (heapSize == null) {
                    heapSize = initialHeapSize;
                }

                if (!preprocessFlag) {
                    logger.info("Executing process with heap size: " + heapSize);
                    processBuilderParameters.add("-Xmx" + heapSize + "m");
                    processBuilderParametersMap.put("-Xmx", heapSize + "m");
                } else {
                    logger.info("Pre-processing: ");
                }

                if (remoteServer != null && mm != null && !preprocessFlag) {
                    inputLines = new ArrayList<String>();
                    errorLines = new ArrayList<String>();
                    // If the remote server exists (preprocessFlag will not be true), then run the component remotely.
                    File newOutputDir = new File(componentOutputDir);
                    if (newOutputDir.mkdirs()) {
                        FileUtils.makeWorldReadable(newOutputDir);
                    }
                    processBuilder.directory(newOutputDir);

                    String workflowDirParent = (new File(workflowDir)).getParent();
                    nonce = WorkflowProcessHelper.getNonce(WorkflowProcessHelper.LS_WFC_NONCE_LENGTH).toString();

                    String toolDirParent = (new File(toolDir)).getParent();

                    // WorkflowHelper.addWfMachine(componentId, mm);

                    /* Example: mm.zipAndUpload(workflowDir, 10L, "Analysis-1-x782268", "C:/datashop/dataset_files/workflows/",
                    "C:/Users/mkomisin/git/WorkflowComponents/", "123457", "C:/Users/mkomisin/git/WorkflowComponents/AnalysisAfm/dist/AnalysisAfm-1.0.jar",
                        "mkomisin", inputComponentIds, inputComponentFiles, processBuilderParametersMap); */
                    uploadFile = new File(workflowDirParent.replaceAll("\\\\", "/") + "/" + workflowId.toString() + "/" + componentId + nonce + ".zip");
                    File resultsZipFile = File.createTempFile(nonce, ".zip");

                    logger.debug("ZipAndUpload parameters: " + /*local path*/ workflowDirParent.replaceAll("\\\\", "/") + "/" + ", "
                            + workflowId + ", " + componentId + ", "
                            + /* remote path*/ workflowDirParent.replaceAll("\\\\", "/") + "/" + ", "
                            + toolDirParent.replaceAll("\\\\", "/") + "/" + ", "
                            + nonce + ", " + toolPath.replaceAll("\\\\", "/") + ", "
                            + userId + ", " + inputComponentIds + ", " + inputComponentFiles + ", "
                            + processBuilderParametersMap + ", " + remoteServer + ", " + heapSize + ", "
                            + uploadFile + ", " + resultsZipFile);

                    resultsZipFile = mm.zipAndUpload(/*local path*/ workflowDirParent.replaceAll("\\\\", "/") + "/",
                            workflowId, componentId,
                            /* remote path*/ workflowDirParent.replaceAll("\\\\", "/") + "/",
                            toolDirParent.replaceAll("\\\\", "/") + "/",
                            nonce, toolPath.replaceAll("\\\\", "/"),
                            userId, inputComponentIds, inputComponentFiles, processBuilderParametersMap, remoteServer, heapSize,
                            uploadFile, resultsZipFile);

                    if (resultsZipFile != null && resultsZipFile.exists() && resultsZipFile.isFile()) {
                        File componentFolder = WorkflowFileUtils.unzipFileToDirectory(resultsZipFile, workflowDir + "" + componentId + "/output/");
                        File zipInputFile = new File(workflowDir + "" + componentId + nonce + ".zip");
                        // Check on output folder.
                        if (componentFolder == null || !componentFolder.exists() || !componentFolder.isDirectory()) {
                            logger.error("The component output folder could not be created: " + componentFolder);
                            errorMessages.add("The component output folder could not be created: " + componentFolder + "\n");
                        }
                        // Delete sent file.
                        if (zipInputFile != null && zipInputFile.exists() && zipInputFile.isFile()) {
                            zipInputFile.delete();
                        }
                        // Delete results zip.

                        resultsZipFile.delete();



                    } else {
                        logger.error("An unknown error has occured on the remote server.");
                        errorMessages.add("An unknown error has occurred on the remote server.\n");

                    }

                    File outputStreamFile = new File(workflowDir + "" + componentId + "/output/" + "component_output_" + nonce + ".txt");
                    File errorStreamFile = new File(workflowDir + "" + componentId + "/output/" + "component_output_err_" + nonce + ".txt");

                    // Get the error and output streams that were saved into the zip file.
                    FileReader outFileReader = new FileReader(outputStreamFile);
                    BufferedReader outBufferedReader = new BufferedReader(outFileReader);

                    FileReader errFileReader = new FileReader(errorStreamFile);
                    BufferedReader errBufferedReader = new BufferedReader(errFileReader);

                    Boolean finReading = false;
                    do {
                        String line = outBufferedReader.readLine();
                        if (line != null) {
                            inputLines.add(line);
                        } else {
                            finReading = true;
                        }
                    } while (!finReading);


                    finReading = false;
                    do {
                        String line = errBufferedReader.readLine();
                        if (line != null) {
                            errorLines.add(line);
                        } else {
                            finReading = true;
                        }
                    } while (!finReading);

                    if (outBufferedReader != null) {
                        outBufferedReader.close();
                    }

                    if (errBufferedReader != null) {
                        errBufferedReader.close();
                    }

                } else {
                    // Run locally if no remote server is available.
                    File newOutputDir = new File(componentOutputDir);
                    if (newOutputDir.mkdirs()) {
                        FileUtils.makeWorldReadable(newOutputDir);
                    }
                    processBuilder.directory(newOutputDir);
                    localJobParameters.addAll(processBuilderParameters);
                    processBuilder.command(localJobParameters);


                    process = processBuilder.start();

                    // Keep track of locally running process
                    WorkflowProcessHelper.addWfProcess(componentId, process);

                    inputReader = new ThreadedStreamReader(process.getInputStream());
                    errorReader = new ThreadedStreamReader(process.getErrorStream());

                    Thread inputReaderThread = new Thread(inputReader);
                    Thread errorReaderThread = new Thread(errorReader);

                    inputReaderThread.start();
                    errorReaderThread.start();

                    inputReaderThread.join();
                    errorReaderThread.join();

                    inputLines = inputReader.getStringBuffer();
                    errorLines = errorReader.getStringBuffer();
                    if (!errorLines.isEmpty()) {
                        errorMessages.addAll(errorLines);
                    }
                }


            } catch (IOException e) {
                logger.error(e.toString());
                errorMessages.add("" + e.toString() + "\n");
            } catch (InterruptedException e) {
                logger.error(e.toString());
                errorMessages.add("" + e.toString() + "\n");
            } finally {
                if (uploadFile != null && uploadFile.exists()) {
                    uploadFile.delete();
                }
                try {
                    if (process != null) {
                        process.waitFor();
                    }
                } catch (InterruptedException e) {
                    logger.error(e.toString());
                    errorMessages.add(e.toString());
                }
            }

            if (inputLines == null || inputLines.isEmpty()) {
                logger.trace("No results were generated.");
                errorMessages.add("No results were generated.");
                output = new Element("inputs");
            } else {
                output = WorkflowXmlUtils.getStringAsElement(StringUtils.join(inputLines.toArray(), "\n"));
            }


        } catch (Exception e) {
            this.addErrorMessage(e.toString());
        } finally {
            if (WorkflowProcessHelper.hasNonce(nonce)) {
                WorkflowProcessHelper.removeNonce(nonce);
            }
        }

        setOutputMapping(output);
    }


    protected void runComponent() {
        System.out.println(this.getOutput());
        WorkflowProcessHelper.markProcessCompleted(this.componentId);
    }

    private Long runProcess(ArrayList<String> params1) {
        Long pid = null;
        ArrayList<String> processBuilderParameters = new ArrayList<String>();
        processBuilderParameters.addAll(params1);

        ThreadedStreamReader inputReader = null;
        ThreadedStreamReader errorReader = null;
        List<String> inputLines = null;
        List<String> errorLines = null;

        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process = null;

        try {

            logger.info("Executing process: " + processBuilderParameters.toString());

            processBuilder.command(processBuilderParameters);

            File newOutputDir = new File(componentOutputDir);
            if (newOutputDir.mkdirs()) {
                FileUtils.makeWorldReadable(newOutputDir);
            }
            processBuilder.directory(newOutputDir);

            process = processBuilder.start();
            WorkflowProcessHelper.addWfProprietaryProcess(componentId, process);

            inputReader = new ThreadedStreamReader(process.getInputStream());
            errorReader = new ThreadedStreamReader(process.getErrorStream());

            Thread inputReaderThread = new Thread(inputReader);
            Thread errorReaderThread = new Thread(errorReader);


            inputReaderThread.start();
            errorReaderThread.start();

            inputReaderThread.join();
            errorReaderThread.join();

            inputLines = inputReader.getStringBuffer();
            errorLines = errorReader.getStringBuffer();

            if (!errorLines.isEmpty()) {
                errorMessages.addAll(errorLines);
            }

            if (osName.indexOf("win") < 0) {
                if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                    /* get the PID on unix/linux systems */
                    try {
                        Field f = process.getClass().getDeclaredField("pid");
                        f.setAccessible(true);
                        pid = f.getLong(process);
                    } catch (Throwable e) {

                    }
                }
            }


        } catch (IOException e) {
            logger.error(e.toString());
            errorMessages.add("" + e.toString() + "\n");
        } catch (InterruptedException e) {
            logger.error(e.toString());
            errorMessages.add("" + e.toString() + "\n");
        } finally {

            try {
                if (process != null) {
                    process.waitFor();
                }
            } catch (InterruptedException e) {
                logger.error(e.toString());
                errorMessages.add(e.toString());
            }
        }

        return pid;
    }

    /**
     * Runs an external program and adds the files the component output.
     * @return the output directory as a File object
     */
    protected File runExternal() {

        String replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
        String delimReplace = "$1";

        // If there is an external component, run it by passing all options and inputs
        // to the external tool via command-line.
        String externalInterpreter = System.getProperty("component.interpreter.path");
        String externalProgram = System.getProperty("component.program.path");

        ArrayList<String> params1 = new ArrayList<String>();

        // Pass the interpreter path to the processbuilder params, if an interpreter is needed.
        if (externalInterpreter != null && !externalInterpreter.trim().isEmpty()) {
            String interpreterPath = externalInterpreter.trim().replaceAll("\\\\ ", "<SPACE_CHAR>");
            for (String command : interpreterPath.split(" ", -1)) {
                params1.add(command.replaceAll("<SPACE_CHAR>", "\\\\ "));
            }
        }

        // Pass the program path to the process builder params.
        params1.add(this.getToolDir() + externalProgram);

        // Pass the program and working directories
        params1.add("-programDir");
        params1.add(this.getToolDir());
        params1.add("-workingDir");
        params1.add(this.getComponentOutputDir());
        if (this.getUserId() != null) {
                params1.add("-userId");
                params1.add(this.getUserId().replaceAll(replaceChars, delimReplace));
        }

        List<String> componentSpecificParamNames = new ArrayList<String>();

        if (this.componentOptions != null) {
            for (Element optionElement : (List<Element>) this.componentOptions.getChildren()) {
                if (!optionElement.getText().trim().isEmpty()) {
                    if (!componentSpecificParamNames.contains(optionElement.getName())) {
                        componentSpecificParamNames.add(optionElement.getName());
                    }
                }
            }
            Collections.sort(componentSpecificParamNames);
        }

        // Pass the component's options
        if (this.componentOptions != null) {

            String pattern = "Input ([0-9]+){1} (\\(([0-9]+)\\)){0,1}.* \\(column [0-9]+\\)";
            String fileIndexPattern = "Input ([0-9]+){1} (\\(([0-9]+)\\)){1}.* \\(column [0-9]+\\)";
            for (String sortedOptionName : (List<String>) componentSpecificParamNames) {
                    for (Element optionElementInner : (List<Element>) this.componentOptions.getChildren()) {
                        if (!optionElementInner.getText().trim().isEmpty()
                                && optionElementInner.getName().equalsIgnoreCase(sortedOptionName)) {

                        // Handles cases where windows/linux finds whitespace in the command-line argument.
                        String optionValue = optionElementInner.getTextTrim();
                        String optionName = optionElementInner.getName().replaceAll(replaceChars, delimReplace);
                        if (optionValue.matches(pattern)) {
                            String nodeIndex = optionValue.replaceAll(pattern, "$1");
                            params1.add("-" + optionName + "_nodeIndex");
                            params1.add(nodeIndex);

                            if (optionValue.matches(fileIndexPattern)) {
                                String fileIndex = optionValue.replaceAll(pattern, "$3");
                                params1.add("-" + optionName + "_fileIndex");
                                params1.add(fileIndex);
                            }
                            optionValue = WorkflowFileUtils.htmlDecode(stripInputTags(optionValue));

                            params1.add("-" + optionName);
                        } else {
                            params1.add("-" + optionName);
                        }
                        params1.add(optionValue.replaceAll(replaceChars, delimReplace));

                    }
                }
            }
        }

        if (inFiles != null && !inFiles.isEmpty()) {
            for (Integer inputIndex : inFiles.keySet()) {
                List<File> inFilesNode = (List<File>) inFiles.get(inputIndex);
                Integer fileIndex = 0;
                for (File inFile : inFilesNode) {

                    params1.add("-node");
                    params1.add(inputIndex.toString());

                    params1.add("-fileIndex");
                    params1.add(fileIndex.toString());

                    // Handles cases where windows/linux finds special chars in the file path.
                    params1.add(inFile.getAbsolutePath()
                            .replaceAll(replaceChars, delimReplace));


                    fileIndex++;
                }

            }
        }

        if (privateOptions != null && privateOptions.getChildren() != null) {
            for (Element child : (List<Element>) privateOptions.getChildren()) {
                    // The option id must be alpha-numeric (with underscores allowed).
                    params1.add("-" + child.getName().replaceAll("[^a-zA-Z0-9_]+", ""));

                    if (!child.getTextTrim().isEmpty()) {
                        params1.add(child.getText().replaceAll(replaceChars, delimReplace));
                    }
            }
        }

        runProcess(params1);

        return new File(this.getComponentOutputDir());
    }


    /**
     * Returns the attachment by node and file index.
     * @param nodeIndex the node index (begin at 0)
     * @return the file attachments
     */
    protected List<File> getAttachments(int nodeIndex) {
        if (inFiles != null && nodeIndex >= 0 && nodeIndex < inFiles.size()) {
            return inFiles.get(nodeIndex);
        } else {
            logger.debug("Missing input files on input node: " + nodeIndex);
        }
        return null;
    }


    protected void traverseAttachments() {
        Map<Integer, List<File>> inFilesByNodeIndex = this.getAttachments();

        for (Integer nodeIndex : inFilesByNodeIndex.keySet()) {
            for (File file : inFilesByNodeIndex.get(nodeIndex)) {
                // do something
                logger.info(file.toString());
            }
        }

    }
    /**
     * Returns the attachment by node and file index.
     * @param nodeIndex the node index (begin at 0)
     * @param fileIndex the file index (begin at 0)
     * @return the file attachment
     */
    protected File getAttachment(int nodeIndex, int fileIndex) {
        File file = null;
        if (inFiles != null && nodeIndex >= 0 && nodeIndex < inFiles.size()) {
            if (inFiles.containsKey(nodeIndex)) {
                if (inFiles.get(nodeIndex) != null && fileIndex < inFiles.get(nodeIndex).size()
                        && inFiles.get(nodeIndex).get(fileIndex) != null) {
                    file = inFiles.get(nodeIndex).get(fileIndex);
                }
            }
        }
        return file;
    }

    /**
     * Returns the type of file.
     * @param index the input index
     * @return the type of file (see TableTypes.xsd)
     */
    protected String getAttachmentType(int index) {
        return inFileTypes.get(index);
    }

    /**
     * Returns the file attached to this component.
     * @return the file attached to this component.
     */
    protected Map<Integer, List<File>> getAttachments() {
        return inFiles;
    }

    protected File getAttachmentAndUnzip(int nodeIndex, int fileIndex)
            throws IOException {
        File file = null;
        File unzipDirectory = null;
        if (inFiles != null && nodeIndex >= 0 && nodeIndex < inFiles.size()) {
            if (inFiles.containsKey(nodeIndex)) {
                if (inFiles.get(nodeIndex) != null && fileIndex < inFiles.get(nodeIndex).size()
                        && inFiles.get(nodeIndex).get(fileIndex) != null) {

                    file = inFiles.get(nodeIndex).get(fileIndex);

                    //String tmpPath = WorkflowHelper.randomAlphanumeric(32);
                    String outputDir = this.getComponentOutputDir();
                    // Overkill to use get the mime type, in my opinion, but here's a convenient method to do so:
                    // String mimeType = WorkflowHelper.getMimeType(file);

                    if (file.getName().matches(".*\\.zip")) {
                        // Unzip
                        unzipDirectory = WorkflowFileUtils.unzipFileToDirectory(file, outputDir);
                    } else if (file.getName().matches(".*\\.tar\\.gz") || file.getName().matches(".*\\.tar\\.gzip")
                            || file.getName().matches(".*\\.t\\.gzip")) {
                        // Gunzip and untar
                        Integer endIndex = file.getName().lastIndexOf('.');
                        String newFileName = file.getName().substring(0, endIndex);
                        String tarFilePath = outputDir + "/" + newFileName;
                        WorkflowFileUtils.unGunzipFile(file.getAbsolutePath(), tarFilePath);
                        WorkflowFileUtils.uncompressTar(new File(tarFilePath), new File(outputDir));
                        unzipDirectory = new File(outputDir);
                    } else if (file.getName().matches(".*\\.tar\\.bz") || file.getName().matches(".*\\.tar\\.bz2")
                            || file.getName().matches(".*\\.t\\.bz2")) {
                        Integer endIndex = file.getName().lastIndexOf('.');
                        String newFileName = file.getName().substring(0, endIndex);
                        String tarFilePath = outputDir + "/" + newFileName;
                        WorkflowFileUtils.bunzip2(file.getAbsolutePath(), tarFilePath);
                        WorkflowFileUtils.uncompressTar(new File(tarFilePath), new File(outputDir));
                        unzipDirectory = new File(outputDir);
                    }
                }
            }
        }
        if (file == null) {
            addErrorMessage("Missing input file on input node: " + nodeIndex);
        } else if (unzipDirectory == null) {
            addErrorMessage("Unrecognized extension to uncompress input node: "
                + nodeIndex + ", file: " + file.getName());
        }
        return unzipDirectory;
    }

    /**
     * Returns the metadata of file.
     * @param index the input index
     * @return the metadata (see TableTypes.xsd)
     */
    protected Integer getLabel(int index) {
        if (index < inFileMetadata.size()) {
            Element inMeta = inFileMetadata.get(index);
            if (inMeta.getChildren() != null) {
                for (Iterator<Element> iter = inMeta.getChildren().iterator(); iter.hasNext(); ) {
                    // Types: META_DATA_HEADER, META_DATA_LABEL, other
                    Element metaDataType = iter.next();
                    if (metaDataType.getName().equalsIgnoreCase(META_DATA_LABEL)) {
                        if (metaDataType.getChildTextTrim("index") != null) {
                            logger.trace("Metadata label found: index = " + metaDataType.getChildTextTrim("index").trim());
                            return Integer.parseInt(metaDataType.getChildTextTrim("index").trim());
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the metadata of file.
     * @param index the input index
     * @return the metadata (see TableTypes.xsd)
     */
    protected Element getInfileMetadata(int index) {
        if (index < inFileMetadata.size()) {
            return inFileMetadata.get(index);
        }
        return null;
    }

    /**
     * Returns the file metadata attached to this component.
     * @return the file metadata attached to this component.
     */
    protected Map<Integer, Element> getInfileMetadata() {
        return inFileMetadata;
    }


    /**
     * Returns the metadata of file.
     * @param index the input index
     * @return the metadata (see TableTypes.xsd)
     */
    protected Element getOptionFileMetadata(int index) {
        return inFileMetadata.get(index);
    }

    /**
     * Returns the file metadata attached to this component.
     * @return the file metadata attached to this component.
     */
    protected Map<Integer, Element> getOptionFileMetadata() {
        return inFileMetadata;
    }

    /**
     * Gets the option value from the options element.
     * @param index the input index
     * @param elementName the element name of the option, e.g. model
     */
    protected String getInputAsString(int index, String elementName) {
        String stringValue = null;

        for (Integer key : inputDataMap.keySet()) {
            Map<String, Object> map = inputDataMap.get(key);
            if (map.containsKey(elementName)) {
                Object o = map.get(elementName);
                if (o != null) {
                    stringValue = o.toString().trim();
                }
                break;
            }
        }

        return stringValue;
    }


    /**
     * Adds a private component option.
     * @param elementName the element name of the option, e.g. model
     * @param value the string value
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Element addPrivateOption(String elementName, String value) {

        Element privateOption =
            (Element) privateOptions.getChild("elementName");
        if (value != null) {
            String escapedValue = WorkflowFileUtils.htmlEncode(value.replace("\n", "").replace("\r", ""));
            if (privateOption != null) {
                privateOption.setText(escapedValue);
            } else {
                Element newOption = new Element(elementName);
                newOption.setText(escapedValue);
                privateOptions.addContent(newOption);
            }
        }
        return privateOptions;
    }


    /**
     * Sets a component option.
     * @param elementName the element name of the option, e.g. model
     * @param value the string value
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Element setOption(String elementName, String value) {

        Element optionsElement =
            (Element) component.getChild("options");
        if (value != null) {
            String escapedValue = WorkflowFileUtils.htmlEncode(value.replace("\n", "").replace("\r", ""));
            if (optionsElement != null) {
                Element optionElement = optionsElement.getChild(elementName);

                if (optionElement != null) {
                    optionElement.setText(escapedValue);
                } else {
                    Element newOption = new Element(elementName);
                    newOption.setText(escapedValue);
                    optionsElement.addContent(newOption);
                }
            }
        }
        return optionsElement;
    }

    public void setInputFile(Integer nodeIndex, Integer fileIndex, File file) {
        if (inFiles != null && !inFiles.isEmpty()) {
            if (inFiles.containsKey(nodeIndex)) {
                inFiles.remove(nodeIndex);
            }
            List<File> fileList = new ArrayList<File>();
            fileList.add(file);
            inFiles.put(nodeIndex, fileList);
        }
    }

    /**
     * Returns a list of ComponentOption objects by their option name.
     * @param elementName the name of the element (option)
     * @return a list of ComponentOption objects
     */
    protected List<InputHeaderOption> getInputHeaderOption(String elementName) {
        return getInputHeaderOption(elementName, null, null);
    }


    /**
     * Returns a list of ComponentOption objects by their option name.
     * @param elementName the name of the element (option)
     * @param nodeIndex the index of the input node, beginning at 0
     * @return a list of ComponentOption objects
     */
    protected List<InputHeaderOption> getInputHeaderOption(String elementName, Integer nodeIndex) {
        return getInputHeaderOption(elementName, nodeIndex, null);
    }

    /**
     * Returns a list of ComponentOption objects by their option name.
     * @param elementName the name of the element (option)
     * @param nodeIndex the index of the input node, beginning at 0
     * @param fileIndex the index of the input file, beginning at 0
     * @return a list of ComponentOption objects
     */
    protected List<InputHeaderOption> getInputHeaderOption(String elementName, Integer nodeIndex, Integer fileIndex) {
        List<InputHeaderOption> componentOptionList = new ArrayList<InputHeaderOption>();

        if (this.componentOptions != null) {

            String replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
            String delimReplace = "$1";
            String pattern = "Input ([0-9]+){1} (\\(([0-9]+)\\)){0,1}.* \\(column ([0-9]+)\\)";
            String fileIndexPattern = "Input ([0-9]+){1} (\\(([0-9]+)\\)){1}.* \\(column [0-9]+\\)";

            for (Element optionElementInner : (List<Element>) this.componentOptions.getChildren()) {

                // Handles cases where windows/linux finds whitespace in the command-line argument.
                String optionValue = optionElementInner.getTextTrim();
                String optionName = optionElementInner.getName().replaceAll(replaceChars, delimReplace);
                if (optionName.equalsIgnoreCase(elementName)) {
                    Integer thisNodeIndex = null;
                    Integer thisFileIndex = null;
                    Integer columnIndex = null;
                    if (optionValue.matches(pattern)) {
                        String nodeIndexStr = optionValue.replaceAll(pattern, "$1");
                        if (nodeIndexStr.matches("[0-9]+")) {
                            thisNodeIndex = Integer.parseInt(nodeIndexStr);
                        }

                        String columnIndexStr = optionValue.replaceAll(pattern, "$4");
                        if (columnIndexStr.matches("[0-9]+")) {
                            columnIndex = Integer.parseInt(columnIndexStr);
                        }

                        if (optionValue.matches(fileIndexPattern)) {
                            String fileIndexStr = optionValue.replaceAll(pattern, "$3");
                            if (fileIndexStr.matches("[0-9]+")) {
                                thisFileIndex = Integer.parseInt(fileIndexStr);
                            }
                        }
                        optionValue = WorkflowFileUtils.htmlDecode(stripInputTags(optionValue));
                    }

                    if (nodeIndex != null && thisNodeIndex == nodeIndex
                            && fileIndex != null && thisFileIndex == fileIndex) {
                        InputHeaderOption componentOption = new InputHeaderOption(
                            optionName, optionValue, thisNodeIndex, thisFileIndex, columnIndex);

                        componentOptionList.add(componentOption);
                    } else if (nodeIndex == null
                            && fileIndex != null && thisFileIndex == fileIndex) {
                        InputHeaderOption componentOption = new InputHeaderOption(
                            optionName, optionValue, thisNodeIndex, thisFileIndex, columnIndex);

                        componentOptionList.add(componentOption);
                    } else if (nodeIndex != null && thisNodeIndex == nodeIndex
                            && fileIndex == null) {
                        InputHeaderOption componentOption = new InputHeaderOption(
                            optionName, optionValue, thisNodeIndex, thisFileIndex, columnIndex);

                        componentOptionList.add(componentOption);
                    }  else if (nodeIndex == null
                            && fileIndex == null) {
                        InputHeaderOption componentOption = new InputHeaderOption(
                            optionName, optionValue, thisNodeIndex, thisFileIndex, columnIndex);

                        componentOptionList.add(componentOption);
                    }
                }
            }
        }

        return componentOptionList;
    }

    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected String getOptionAsString(String elementName) {
        String value = null;

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        if (optionElement != null) {
            // HACK. Trac #769. Stripping the leading "Input (#) -"
            // and trailing "(#)" from option name.
            value = WorkflowFileUtils.htmlDecode(stripInputTags(optionElement.getTextTrim()));
        }

        return value;
    }


    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected List<String> getMultiOptionAsString(String elementName) {
        String value = null;
        List<String> multiValueList = new ArrayList<String>();
        Element optionsElement =
                (Element) component.getChild("options");
        for (Iterator<Element> optionIter = optionsElement.getChildren(elementName).iterator(); optionIter.hasNext(); ) {
            Element optionElement = optionIter.next();
            if (optionElement != null) {
                // HACK. Trac #769. Stripping the leading "Input (#) -"
                // and trailing "(#)" from option name.
                multiValueList.add(WorkflowFileUtils.htmlDecode(stripInputTags(optionElement.getTextTrim())));
            }
        }


        return multiValueList;
    }

    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    public static String getOptionAsString(String elementName, Element component) {
        String value = null;

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        if (optionElement != null) {
            // HACK. Trac #769. Stripping the leading "Input (#) -"
            // and trailing "(#)" from option name.
            value = WorkflowFileUtils.htmlDecode(stripInputTags(optionElement.getTextTrim()));
        }
        return value;
    }

    public static String stripInputTags(String value) {
        Pattern inputTagPattern = Pattern.compile("Input [0-9]+ \\([0-9]+\\).* \\(column [0-9]+\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher  = inputTagPattern.matcher(value);
        if (matcher.matches()) {
            int index1 = value.indexOf("-");
            int index2 = value.lastIndexOf("(column ");
            if ((index1 > 0) && (index2 > 0)) {
                StringBuffer sb =
                    new StringBuffer(value.substring(index1 + 1, index2));
                value = sb.toString().trim();
            }
        }

        Pattern oldInputTagPattern = Pattern.compile("Input [0-9]+ .* \\(column [0-9]+\\)",
                Pattern.CASE_INSENSITIVE);
        Matcher oldMatcher  = oldInputTagPattern.matcher(value);
        if (oldMatcher.matches()) {
            int index1 = value.indexOf("-");
            int index2 = value.lastIndexOf("(column ");
            if ((index1 > 0) && (index2 > 0)) {
                StringBuffer sb =
                    new StringBuffer(value.substring(index1 + 1, index2));
                value = sb.toString().trim();
            }
        }
        return value;
    }


    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Integer getOptionAsInteger(String elementName) {
        Integer value = null;

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        if (optionElement != null) {
            try {
                if (optionElement.getTextTrim().equalsIgnoreCase("INF")) {
                    value = Integer.MAX_VALUE;
                    this.addErrorMessage("Incorrectly casting infinite double (option = '"
                        + elementName + "') to Integer");
                } else {
                    value = Integer.parseInt(optionElement.getTextTrim());
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }

        }


        return value;
    }

    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Double getOptionAsDouble(String elementName) {
        Double value = null;

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        if (optionElement != null) {
            try {
                if (optionElement.getTextTrim().equalsIgnoreCase("INF")) {
                    value = Double.POSITIVE_INFINITY;
                } else {
                    value = Double.parseDouble(optionElement.getTextTrim());
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }

        }


        return value;
    }


    /**
     * Gets the option value from the options element.
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Boolean getOptionAsBoolean(String elementName) {
        Boolean value = null;

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        if (optionElement != null) {
            try {
                value = Boolean.parseBoolean(optionElement.getTextTrim());
            } catch (Exception e) {
                logger.error(e.toString());
            }

        }


        return value;
    }

    /**
     * Gets the option element from the options element.
     * @param index the input index
     * @param elementName the element name of the option, e.g. model
     * @throws JDOMException throws a JDOMException if a parsing error occurs
     */
    protected Element getOptionAsElement(int index, String elementName) {

        Element optionsElement =
                (Element) component.getChild("options");
        Element optionElement = optionsElement.getChild(elementName);

        return optionElement;
    }

    public void setComponentXmlFilePath(String componentXmlFilePath) {
        this.componentXmlFilePath = componentXmlFilePath;
    }


    /**
     * @return the inFileTypes
     */
    public Map<Integer, String> getInFileTypes() {
        return inFileTypes;
    }


    /**
     * @param inFileTypes the inFileTypes to set
     */
    public void setInFileTypes(Map<Integer, String> inFileTypes) {
        this.inFileTypes = inFileTypes;
    }


    /**
     * @return the outFileTypes
     */
    public Map<String, String> getOutFileTypes() {
        return outFileTypes;
    }


    /**
     * @param outFileTypes the outFileTypes to set
     */
    public void setOutFileTypes(Map<String, String> outFileTypes) {
        this.outFileTypes = outFileTypes;
    }


    public String getOutput() {
        logger.trace("Getting component output.");
        logger.trace("isInitialized = " + isInitialized);
        Element finaloutput = new Element("outputs");
        String emptyOutputs = "<outputs/>";
        String returnString = emptyOutputs;
        Boolean optionsAdded = false;
        try {
            if (!isInitialized) {
                return emptyOutputs;
            }

            // for each output node, generate an output
            for (int outIndex = 0; outIndex < this.outputXml.size(); outIndex++) {
                Element nodeOutput = this.outputXml.get(outIndex);

                // Calculate the elapsed time (seconds) of the component
                Long finalTime = System.currentTimeMillis();
                elapsedSeconds = (finalTime - startTime) / 1000.0;

                if (nodeOutput.getChild("elapsed_seconds") != null) {
                    nodeOutput.removeChild("elapsed_seconds");
                }
                Element elapsedSecondsElement = (new Element("elapsed_seconds"))
                        .addContent(elapsedTimeFormat.format(elapsedSeconds));
                nodeOutput.addContent(elapsedSecondsElement);

                if (nodeOutput.getChild("errors") != null) {
                    nodeOutput.removeChild("errors");
                }
                if (!this.getErrorMessages().isEmpty()) {
                    Element errorsElement = (new Element("errors"))
                        .addContent(StringUtils.join(this.getErrorMessages().toArray(), "\n"));
                    nodeOutput.addContent(errorsElement);

                } else {
                    Element errorsElement = new Element("errors");
                    nodeOutput.addContent(errorsElement);

                }
//mck1
                if (this.componentOptions != null) {
                    logger.trace("Getting import files.");
                    // Files must be added first so that we can use the xs:any element properly
                    for (Element element : (List<Element>) this.componentOptions.getChildren()) {
                        if (element.getName().equalsIgnoreCase("files")) {

                            if (element.getChildren() != null) {

                                for (Element fileChild : (List<Element>) element.getChildren()) {

                                    Boolean addDummyFile = true;
                                    Element delElement = null;
                                    if (fileOutputXml.size() > 0 && fileOutputXml.get(outIndex) != null && fileOutputXml.get(outIndex).getChildren() != null
                                            && !fileOutputXml.get(outIndex).getChildren().isEmpty()) {
                                        // Get all of the output node's files
                                        for (Element fileChild2 : (List<Element>) fileOutputXml.get(outIndex).getChildren()) {
                                            if (fileChild2.getChildTextTrim("index").equals(fileChild.getChildTextTrim("index"))) {
                                                delElement = fileChild2;
                                                addDummyFile = false;
                                                break;
                                            }
                                        }
                                    }


                                    if (addDummyFile) {
                                        Element newFiles = new Element("files");
                                        newFiles.addContent((Element) fileChild.clone());
                                        if (fileOutputXml.containsKey(outIndex)) {
                                            fileOutputXml.remove(outIndex);
                                        }
                                        fileOutputXml.put(outIndex, newFiles);

                                    } else if (delElement != null && delElement.getChild("metadata") != null) {
                                        fileChild.addContent((Element) delElement.getChild("metadata").clone());
                                        delElement.detach();
                                    }

                                }
                            }
                        }
                    }
                }

                logger.trace("Process file input for metadata.");
                for (Integer inputIndex : this.inputXml.keySet()) {
                    List<Element> elementList = this.inputXml.get(inputIndex);
                    for (Element element : elementList) {
                        if (element.getChild("files") != null && element.getChild("files").getChildren() != null) {
                            Element filesElement = element.getChild("files");
                            for (Element fileElement : (List<Element>)filesElement.getChildren()) {
                                if (fileElement.getChildren() != null) {
                                    if (fileElement.getChild("metadata") != null) {
                                        inFileMetadata.put(inputIndex, fileElement.getChild("metadata"));
                                        logger.trace("Input file found: " + WorkflowXmlUtils.getElementAsString(fileElement));
                                    }

                                }
                            }
                            break; // we only get metadata from one of the objects for now.. more code required to handle them separately
                        }
                    }
                }

                logger.trace("Adding file output.");
                if (nodeOutput.getChild("files") != null) {
                    nodeOutput.removeChild("files");
                }
                if (fileOutputXml.size() > 0) {

                    if (fileOutputXml.containsKey(outIndex)) {
                        Element fileOutputXmlElement = fileOutputXml.get(outIndex);
                        nodeOutput.addContent(fileOutputXmlElement);
                    }
                }

                if (optionsAdded == false && this.componentOptions != null) {
                    optionsAdded = true;
                    logger.trace("Adding option meta-data.");
                    // Non-files are added after Files so that we can use the xs:any element properly
                    if (this.componentOptions != null && this.componentOptions.getChildren() !=  null) {
                        for (Element element : (List<Element>) this.componentOptions.getChildren()) {
                            if (!element.getName().equalsIgnoreCase("files")) {
                                Element optionMeta = (Element) element.clone();
                                if (!optionMeta.getTextTrim().isEmpty()) {
                                    String optionValue = optionMeta.getTextTrim();
                                    //.replaceAll("Input_[0-9]+_Column_[0-9]+_", "");

                                    optionMeta.setText(optionValue);
                                }
                                optionMetadataXml.addContent(optionMeta);
                                logger.trace("Option meta-data found: " + WorkflowXmlUtils.getElementAsString(optionMeta));
                            }

                        }
                    }
                }

                logger.trace("Adding input meta-data.");
                for (Integer inputIndex : this.inputXml.keySet()) {
                    List<Element> elementList = this.inputXml.get(inputIndex);
                    for (Element element : elementList) {
                        if (element != null && element.getChildren() !=  null) {
                            for (Element optionElement : (List<Element>) this.componentOptions.getChildren()) {
                                if (element.getChild("inputmeta") != null) {
                                    Element inputmetaElement = element.getChild("inputmeta");
                                    // mck todo: fix me
                                    //inputMetadataXml.addContent((Element) inputmetaElement.clone());
                                    logger.trace("Input meta-data found: " + WorkflowXmlUtils.getElementAsString(element));
                                    break; // we only get metadata from one of the objects for now.. more code required to handle them separately
                                }
                            }
                        }
                    }
                }

                if (nodeOutput.getChild("inputmeta") != null) {
                    nodeOutput.removeChild("inputmeta");
                }
                nodeOutput.addContent((Element) inputMetadataXml.clone());

                if (nodeOutput.getChild("optionmeta") != null) {
                    nodeOutput.removeChild("optionmeta");
                }
                nodeOutput.addContent((Element) optionMetadataXml.clone());

                logger.trace("End of getOutput method.");
                logger.trace("Output XML: " + WorkflowXmlUtils.getElementAsString(nodeOutput));

                finaloutput.addContent((Element)(nodeOutput.clone()));
            }    // end of for each output node

            returnString = WorkflowXmlUtils.getElementAsString(finaloutput);
        } catch (IOException e) {
            this.addErrorMessage(e.toString());
            return emptyOutputs;
        }

        return returnString;
    }

    /**
     * @return the outFiles
     */
    public Map<Integer, List<File>> getOutFiles() {
        return outFiles;
    }


    /**
     * @param outFiles the outFiles to set
     */
    public void setOutFiles(Map<Integer, List<File>> outFiles) {
        this.outFiles = outFiles;
    }


    /**
     * Add a value to the output data where the optionName is the new element name,
     * e.g. <optionName>valueAsString</optionName>
     * @param index the output index
     * @param optionName
     * @param valueAsString
     */
    public void addOutputData(Integer outIndex, String optionName, String valueAsString) {
        if (optionName != null && !optionName.isEmpty() && valueAsString != null) {
            Element optionElement = new Element(optionName);
            optionElement.setText(valueAsString);
            outputXml.put(outIndex, optionElement);
        }
    }


    /**
     * Adds the output file along with the expected file type.
     * @param file the file
     * @param nodeIndex the output node index
     * @param fileIndex the file index
     * @param the file label (see TableTypes.xsd)
     * @param elementName the name of the file element, e.g. student-step
     * @param isEmptyFileAllowed whether or not an empty file is allowed to be added
     */
    public void addOutputFile(File file, Integer nodeIndex, Integer fileIndex, String label, Boolean isEmptyFileAllowed) {
        if (file != null && file.exists() && file.canRead() && (file.length() > 0 || isEmptyFileAllowed)) {
            addOutputFileNoTest(file, nodeIndex, fileIndex, label);
        } else {
            errorMessages.add("Output file " + file.getName() + " is empty.");
        }
    }

    /**
     * Adds the output file along with the expected file type.
     * @param file the file
     * @param nodeIndex the output node index
     * @param fileIndex the file index
     * @param the file label (see TableTypes.xsd)
     * @param elementName the name of the file element, e.g. student-step
     */
    public void addOutputFile(File file, Integer nodeIndex, Integer fileIndex, String label) {
        if (file != null && file.exists() && file.canRead() && file.length() > 0) {
            addOutputFileNoTest(file, nodeIndex, fileIndex, label);
        } else {
            errorMessages.add("Output file " + file.getName() + " is empty.");
        }
    }

    /**
     * Adds the output file along with the expected file type.
     * @param file the file
     * @param nodeIndex the output node index
     * @param fileIndex the file index
     * @param the file label (see TableTypes.xsd)
     * @param elementName the name of the file element, e.g. student-step
     */
    public void addOutputFileNoTest(File file, Integer nodeIndex, Integer fileIndex, String label) {
        try {
        if (file != null && file.exists() && file.canRead()
                && nodeIndex != null && fileIndex != null && label != null) {
            logger.trace("Adding output file to XML output.");

            String replaceChars = "([\\s$'\"\\#\\[\\]!<>|;{}()~])";
            String delimReplace = "$1";

            List<Element> delList = new ArrayList<Element>();
            Element outputFileElement = new Element(label);

            Element outputFilePathElement = (new Element("file_path"))
                    .addContent(file.getAbsolutePath().replaceAll(replaceChars, delimReplace));
            outputFileElement.addContent(outputFilePathElement);

            Element outputFileNameElement = (new Element("file_name"))
                    .addContent(file.getName());
            outputFileElement.addContent(outputFileNameElement);

            Element outputFileIndexElement = (new Element("index"))
                    .addContent(nodeIndex.toString());
            outputFileElement.addContent(outputFileIndexElement);

            Element outputFilelabelElement = (new Element("label"))
                    .addContent(label);
            outputFileElement.addContent(outputFilelabelElement);

            String toolDirParent = new File(this.getToolDir()).getParent();
            String tableTypesFile = WorkflowFileUtils.getStrictDirFormat(
                    toolDirParent) + "/CommonSchemas/TableTypes.xsd";
            logger.trace("Reading " + tableTypesFile);
            String delim = null;

            if (ComponentHierarchyHelper.isDescendant_static(tableTypesFile, label, "tab-delimited")) {
                delim = "\t";
            } else if (ComponentHierarchyHelper.isDescendant_static(tableTypesFile, label, "csv")) {
                delim = ",";
            } else if (ComponentHierarchyHelper.isDescendant_static(tableTypesFile, label, "text")) {
                delim = "\t";
            }

            // Add fileOutputs (if preliminary meta-data already exists,
            // then utilize it to update the component-generated meta-data.
            Boolean outputFound = false;
            logger.trace("Adding or updating files.");
            if (fileOutputXml.containsKey(nodeIndex)) {
                Element fileOutputXmlElement = fileOutputXml.get(nodeIndex);
                if (fileOutputXmlElement.getChildren() != null && !fileOutputXmlElement.getChildren().isEmpty()) {
                    logger.trace("Adding files. Existing element has children. " + fileOutputXmlElement.getChildren().size());

                    // Get all of the output node's files
                    for (Element fileChild : (List<Element>) fileOutputXmlElement.getChildren()) {

                        if (fileChild.getChild("index") != null
                                && fileChild.getChildTextTrim("index").equals(nodeIndex.toString())
                                && (fileChild.getChild("file_path") == null || fileChild.getChildTextTrim("file_path").isEmpty()
                                        || fileChild.getChildTextTrim("file_path").equalsIgnoreCase(file.getAbsolutePath().replaceAll(replaceChars, delimReplace)))) {

                            outputFound = true;
                            logger.trace("Removing file child.");
                            delList.add(fileChild);
                            Element metadata = fileChild.getChild(META_DATA);

                            if (delim != null) {
                                logger.trace("Column Delim: '" + delim + "'");
                                LinkedHashMap<String, Integer> columnHeaders = WorkflowImportHelper.getColumnHeaders(
                                    new File(file.getAbsolutePath().replaceAll(replaceChars, delimReplace)), delim);
                                if (!columnHeaders.isEmpty()) {
                                    Integer headerCount = 0;
                                    StringBuffer metadataBuffer = new StringBuffer("<metadata>");
                                    for(Map.Entry<String, Integer> entry :  columnHeaders.entrySet()) {
                                        String sanitaryKey = WorkflowFileUtils.htmlEncode(entry.getKey().trim());

                                        String role = null;
                                        String newRole = null;
                                        String value = null;
                                        ///String index = columnHeaders.get(key).toString();
                                        if (metadata.getChildren() != null && !metadata.getChildren().isEmpty()) {
                                            for (Element metaChild : (List<Element>) metadata.getChildren()) {
                                                if (metaChild.getChild("name") != null) {
                                                    role = metaChild.getName();
                                                    value = metaChild.getChildTextTrim("name");
                                                    // case-sensitive
                                                    if (value.equals(sanitaryKey)) {
                                                        newRole = role;
                                                        ///index = metaChild.getChildTextTrim("index");
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if (newRole == null) {
                                            newRole = META_DATA_HEADER;
                                        }
                                        metadataBuffer.append("<" + newRole + "><id>" + newRole + headerCount + "</id>"
                                                + "<index>" + entry.getValue() + "</index>"
                                                + "<name>" + sanitaryKey + "</name></" + newRole + ">");
                                        headerCount++;
                                    }
                                    metadataBuffer.append("</metadata>");

                                    if (headerCount > 0) {
                                        Element metadataElement = WorkflowXmlUtils.getStringAsElement(
                                                 metadataBuffer.toString());
                                        if (metadataElement != null && metadataElement.getChildren() != null) {
                                            outputFileElement.addContent((Element)metadataElement.clone());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } // end of for loop


            if (!outputFound) {
                // No preliminary meta-data already exists so simply add the component-generated meta-data.
                logger.trace("No preliminary meta-data. Column Delim: '" + delim + "'");
                if (delim != null) {
                    LinkedHashMap<String, Integer> columnHeaders = WorkflowImportHelper.getColumnHeaders(
                        new File(file.getAbsolutePath().replaceAll(replaceChars, delimReplace)), delim);
                    if (!columnHeaders.isEmpty()) {
                        Integer headerCount = 0;
                        StringBuffer metadataBuffer = new StringBuffer("<metadata>");
                        for(Map.Entry<String, Integer> entry :  columnHeaders.entrySet()) {
                            String sanitaryKey = WorkflowFileUtils.htmlEncode(entry.getKey().trim());
                            metadataBuffer.append("<header><id>header" + headerCount + "</id>"
                                + "<index>" + entry.getValue() + "</index>"
                                + "<name>" + sanitaryKey + "</name></header>");
                            headerCount++;
                        }
                        metadataBuffer.append("</metadata>");

                        if (headerCount > 0) {
                            Element metadataElement = WorkflowXmlUtils.getStringAsElement(
                                     metadataBuffer.toString());
                            if (metadataElement != null && metadataElement.getChildren() != null) {
                                outputFileElement.addContent((Element)metadataElement.clone());
                            }
                        }
                    }
                }

            }

            for (Iterator<Element> delIter = delList.iterator(); delIter.hasNext(); ) {
                Element delMe = delIter.next();
                delMe.detach();
            }


            if (fileOutputXml.containsKey(nodeIndex)) {
                Element fileOutputXmlElement = fileOutputXml.get(nodeIndex);
                fileOutputXmlElement.addContent(outputFileElement);
            } else {
                Element newFiles = new Element("files");
                newFiles.addContent(outputFileElement);
                fileOutputXml.put(nodeIndex, newFiles);
            }

        }
        } catch (Exception e) {
            this.addErrorMessage(e.toString());
        }
    }



    public String addOutputFiles(String path, Integer nodeIndex) {
        StringBuffer messages = new StringBuffer();

        FileSearch finder = new FileSearch();
        List<File> files = finder.find(Paths.get(path));

        Integer fileIndex = 0;
        for (File file : files) {
            // Check to ensure it is a file, since the finder returns directories, too
            if (file.exists() && file.isFile()
                    && !file.getName().matches("component_output_.*\\.txt")
                    && !file.getName().matches("WorkflowComponent.log")
                    && !file.getName().matches(".*\\.wfl")) {
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
                            addOutputFile(file, nodeIndex, fileIndex, ext);
                        } else if (mimeType != null) {
                            String[] comboText = mimeType.split("/");
                            String label = "file";
                            if (comboText.length > 1) {
                                label = comboText[1];
                            }

                            addOutputFile(file, nodeIndex, fileIndex, label);
                        } else {
                            String label = "file";
                            addOutputFile(file, nodeIndex, fileIndex, label);
                        }
                        fileIndex++;
                    }
                } catch (IOException e) {
                    messages.append("\nError reading mime type from file: " + path);
                }
            }
        }


        return messages.toString();
    }



    public Boolean hasMatchingColumn(File file, String columnRegExp) {
        if (getColumnIndices(file, columnRegExp).size() > 0) {
            return true;
        }
        return false;
    }

    public List<Integer> getColumnIndices(File file, String columnRegExp) {
        List<Integer> columnIndices = new ArrayList<Integer>();

        String errorMessage = null;
        FileReader fReader;
        BufferedReader bReader = null;

        if (file != null && file.exists() && file.isFile()) {
            try {

                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                String firstLine = bReader.readLine();

                Integer colCounter = 0;
                for (String columnHeader : firstLine.split("\t")) {

                    if (columnHeader.matches(columnRegExp)) {
                        columnIndices.add(colCounter);
                    }
                    colCounter++;
                }


            } catch (FileNotFoundException e) {
                errorMessage = "File not found: " + file.getAbsolutePath();
            } catch (IOException e) {
                errorMessage = "IOException for file: " + file.getAbsolutePath();
            } finally {
                if (bReader != null) {
                    try {
                        bReader.close();
                    } catch (IOException e) {
                        errorMessage = "Could not close reader for file: " + file.getAbsolutePath();
                    }
                }
            }
        }
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            logger.error(errorMessage);
        }

        return columnIndices;
    }


    public List<String> getColumnValues(File file, String columnRegExp) {

        String errorMessage = null;
        FileReader fReader;
        BufferedReader bReader = null;
        List<String> values = new ArrayList<String>();

        if (file != null && file.exists() && file.isFile()) {
            try {

                fReader = new FileReader(file);
                bReader = new BufferedReader(fReader);

                String firstLine = bReader.readLine();
                if (firstLine != null) {
                    Integer colCounter = 0;
                    Integer colIndex = 0;
                    for (String columnHeader : firstLine.split("\t")) {

                        if (columnHeader.matches(columnRegExp)) {
                            colIndex = colCounter;
                            break;
                        }
                        colCounter++;
                    }
                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        colCounter = 0;
                        for (String dataValue : line.split("\t")) {

                            if (colCounter == colIndex) {
                                values.add(dataValue);
                                break;
                            }
                            colCounter++;
                        }
                    }
                }



            } catch (FileNotFoundException e) {
                errorMessage = "File not found: " + file.getAbsolutePath();
            } catch (IOException e) {
                errorMessage = "IOException for file: " + file.getAbsolutePath();
            } finally {
                if (bReader != null) {
                    try {
                        bReader.close();
                    } catch (IOException e) {
                        errorMessage = "Could not close reader for file: " + file.getAbsolutePath();
                    }
                }

            }
        }
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            logger.error(errorMessage);
        }

        return values;
    }

    /**
     * Creates a new file with write permissions to be used by the workflow component.
     * The name contains the prefix and the suffix (extension).
     *
     * Note: this method "cleans" the prefix and suffix, substituting underscore for
     * any non-alphanumeric characters... except underscore, backslash, dash and period.
     *
     * @param prefix the prefix
     * @param suffix the suffix (extension)
     * @return the new file with write permissions
     */
    public File createFile(String prefix, String suffix) {

        File newFile = WorkflowFileUtils.createFile(componentOutputDir, prefix, suffix);
        if (newFile == null) {
            addErrorMessage("File creation failed for prefix: " + prefix + " and suffix: " + suffix);
        }
        return newFile;
    }

    /**
     * Creates a new file with write permissions to be used by the workflow component.
     *
     * Note: this method does not do any cleaning of the file name and will cause an
     * error if used with a filename containing a forward slash (/).
     * DO NOT CHANGE THIS BEHAVIOR AS SOME COMPONENTS RELY ON THE LACK OF CLEANING!
     *
     * @param fileName the name of the file to create
     * @return the new file with write permissions
     */
    public File createFile(String fileName) {

        File newFile = WorkflowFileUtils.createFile(componentOutputDir, fileName);
        if (newFile == null) {
            addErrorMessage("File creation failed for " + fileName);
        }

        return newFile;
    }

    private static final Boolean isCanceled = false;


    public String getComponentXmlFilePath() {
        return componentXmlFilePath;
    }

    public String getWorkflowComponentsDir() {
        String toolDirParent = (new File(toolDir)).getParent();
        return WorkflowFileUtils.getStrictDirFormat(toolDirParent);
    }

    /**
     * Log a special message that indicates the progress of the component.
     * This will be displayed on the component in the workflow instead of "Running".
     * @param message
     */
    public void addComponentProgressMessage(String message) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        logger.info(COMPONENT_PROGRESS_PREPEND + "@" + timeStamp + "@" + message);
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

    public void setWorkflowDir(String workflowDir) {
        this.workflowDir = workflowDir;
    }


    public void addOutputInstance(String outputInstance) {
        outputInstances.add(outputInstance);
    }

    public List<String> getOutputInstances() {
        return outputInstances;
    }

    public Boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        // do nothing, placeholder
    }

    /**
     * The inputType in the schema file is of xs:sequence.  Which means that the input's in the
     * component xml file need to be in the order that they are in the schema: input0, input1 etc.
     * This method sorts the inputs in this.component.
     */
    public void sortInputOrder() {
        ArrayList<ArrayList<Element>> binnedInputs = new ArrayList<ArrayList<Element>>();
        // Get the inputs
        if (this.component == null) {
            return;
        }
        Element inputs = this.component.getChild("inputs");

        if (inputs == null) {
            //This component has no inputs. Nothing to sort
            return;
        }

        List<Element> inputsArray = inputs.getChildren();
        if (inputsArray == null) {return;}
        logger.debug("Sorting inputs in component xml.");

        // Put the inputs into bins based off of their index
        for (Element input : inputsArray) {
            if (input != null) {
                String name = input.getName();
                if (name.length() > 5) { // Name must match inputN
                    Integer inputInd = -1;
                    try {
                        // Remove "input" from the name, parse the rest
                        inputInd = Integer.parseInt(name.substring(5, name.length()));

                        // Ensure that there are instantiated bins up to and including the input index
                        for (int j = binnedInputs.size(); j <= inputInd; j++) {
                            binnedInputs.add(j, new ArrayList<Element>());
                        }

                        binnedInputs.get(inputInd).add(input);
                    } catch (Exception e) {
                        logger.error("Could not parse input index (" + name + ") from component xml: " + e.toString());
                    }
                }
            }
        }

        // Using the bins, put the sorted elements back into the component
        inputs.removeContent();
        int ind = 0;
        for (int i = 0; i < binnedInputs.size(); i++) {
            ArrayList<Element> bin = binnedInputs.get(i);
            if (bin != null) {
                for (Element inputEl : bin) {
                    inputs.addContent(ind++, inputEl);
                }
            }
        }
    }
}
