/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Component Creator
 * Tool to take user-specified info and create files necessary to
 * build a Tigris workflow component.
 *
 * @author Cindy Tipper
 * @version $Revision: 15570 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-10-12 11:35:56 -0400 (Fri, 12 Oct 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentCreator {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Charset. */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /** Default workflow_components directory. */
    private static final String BASE_WFC_DIR = "/datashop/workflow_components";

    /** Default Templates directory. */
    private static final String TEMPLATES_DIR = "Templates";

    /** Default package. */
    private static final String DEFAULT_PKG = "edu.cmu.learnsphere";

    /** Default version. */
    private static final String DEFAULT_VERSION = "1.0";

    /** Default build.properties. */
    private static final String DEFAULT_BUILD_PROPS = "build.properties";

    /** Sample build.properties. */
    private static final String SAMPLE_BUILD_PROPS = "build.properties.sample";

    /** Constant for the format of dates. */
    private static final String DATE_FMT_STR = "MMMM dd, yyyy";

    /** Constant for the format of dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance(DATE_FMT_STR);

    /** Constants for the build.properties options. */
    private static final HashMap<String, String> buildPropsMap = new HashMap<String, String>() {
        {
            put("r", "build.properties.R");
            put("python", "build.properties.Python");
            put("jar", "build.properties.Jar");
        }
    };

    /** Name of the base directory. Defaults to BASE_WFC_DIR. */
    private String baseDir = BASE_WFC_DIR;

    /** Name of the directory where the templates are. */
    private String templatesDir = null;

    /** Name of the component to be created. */
    private String componentName = null;

    /** Type of the component to be created. */
    private String componentType = null;

    /** Language of the component to be created. */
    private String componentLang = null;

    /** Author of the component to be created. */
    private String componentAuthor = null;

    /** Email of the author of the component to be created. */
    private String componentAuthorEmail = null;

    /** Program directory for the component to be created. */
    private String componentProgramDir = null;

    /** Program file for the component to be created. Not fully-qualified. */
    private String componentProgramFile = null;

    /** Version of the component to be created. */
    private String componentVersion = DEFAULT_VERSION;

    /** Java package for the component to be created. */
    private String componentPkg = DEFAULT_PKG;

    /** Description of the component to be created. */
    private String componentDescription = null;

    /** Component inputs. */
    private List<ComponentIO> inputs = null;

    /** Component outputs. */
    private List<ComponentIO> outputs = null;

    /** Component options. */
    private List<ComponentOption> options = null;

    /** Constructor. */
    public WorkflowComponentCreator() {
    }

    /**
     * Call this if you're using the WCC from another DataShop Program.  It will create a
     * new component based on the properties file in the directory you specify.
     * @param propsFileName properties file name and path
     * @param outputDir where to create the new component directory
     * @param wfCompDir directory of the workflow components i.e. /datashop/workflow_components/
     * @param programDir path to where to get programs for the component.  Only necessary if it's not a java component.
     * @return the File object of the newly created component dir.  Or null if something failed.
     */
    public File createNewComponent(String propsFileName, String outputDir, String wfCompDir) {
        Logger logger = Logger.getLogger("WorkflowComponentCreator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("WorkflowComponentCreator starting (" + version + ")...");

        WorkflowComponentCreator wcc = new WorkflowComponentCreator();

        File newComponentDir = null;
        try {
            parsePropsFile(propsFileName);
            this.baseDir = outputDir;
            this.templatesDir = wfCompDir + File.separator + TEMPLATES_DIR;

            // Delete the directory if it already exists
            newComponentDir = new File(baseDir + File.separator + componentName);
            if (newComponentDir.exists()) {
                deleteDir(newComponentDir);
            }

            // create component files
            run();

            newComponentDir =  new File(baseDir + File.separator + componentName);

        } catch (Throwable throwable) {
            logger.error("Unknown error in createNewComponent method.", throwable);
        } finally {
            logger.info("WorkflowComponentCreator done.");
        }

        return newComponentDir;
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("WorkflowComponentCreator.main");
        String version = VersionInformation.getReleaseString();
        logger.info("WorkflowComponentCreator starting (" + version + ")...");

        WorkflowComponentCreator wcc = new WorkflowComponentCreator();

        try {

            // parse commandline options
            wcc.handleOptions(args);

            // create component files
            wcc.run();

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("WorkflowComponentCreator done.");
        }
    }

    /**
     * Check command line arguments and System.exit if necessary.
     * @param args command line arguments passed into main
     */
    public void handleOptions(String[] args) {
        int exitCode = checkOptions(args);
        if (exitCode != CONTINUE) {
            displayUsage();
            System.exit(exitCode);
        }
    }

    /** Continue flag. */
    private static final int CONTINUE = -1;
    /** Exit okay flag. */
    private static final int EXIT_OKAY = 0;
    /** Exit error flag. */
    private static final int EXIT_ERROR = 1;

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     * @return an exit flag, see constants above
     */
    public int checkOptions(String[] args) {
        if (args.length == 0 || args == null) {
            return EXIT_ERROR;
        }

        String propsFile = null;

        String arg;
        for (int i = 0; i < args.length; i++) {
            arg = args[i].trim().toLowerCase();

            if (arg.equals("-h") || arg.equals("-help")) {
                return EXIT_OKAY;
            } else if (arg.equals("-f") || arg.equals("-file")) {
                propsFile = getArg("component properties file", ++i, args);
                if (propsFile == null) {
                    return EXIT_ERROR;
                }
            } else if (arg.equals("-d") || arg.equals("-dir")) {
                baseDir = getArg("base template directory", ++i, args);
                templatesDir = baseDir + File.separator + TEMPLATES_DIR;
                if (baseDir == null) {
                    return EXIT_ERROR;
                }
            }

        } // end for loop

        // If propsFile specified, those values "win"
        if (propsFile != null) {
            parsePropsFile(propsFile);
        } else {
            logger.error("The properties file is required.");
            return EXIT_ERROR;
        }

        // check for the required arguments
        boolean requiredArguments = true;
        if (componentName == null ) {
            requiredArguments = false;
            logger.error("Component name is required.");
        }
        if (componentType == null ) {
            requiredArguments = false;
            logger.error("Component type is required.");
        }
        if (componentLang == null ) {
            requiredArguments = false;
            logger.error("Component lang is required.");
        } else {
            // If not Java, component program files are required.
            if (!componentLang.equalsIgnoreCase("java")) {
                if ((componentProgramDir == null ) || (componentProgramFile == null)) {
                    requiredArguments = false;
                    logger.error("Component program directory and file are required.");
                }
            }
        }

        if (!requiredArguments) {
            return EXIT_ERROR;
        }

        // Set default values.
        if (componentAuthor == null) { componentAuthor = "system"; }
        if (componentAuthorEmail == null) {
            componentAuthorEmail = "datashop-help@lists.andrew.cmu.edu";
        }
        if (componentDescription == null) { componentDescription = ""; }

        logger.debug(getComponentInfo());
        return CONTINUE;

    } // end handleOptions

    /**
     * Display the usage of this utility.
     */
    private void displayUsage() {

        StringBuffer sb = new StringBuffer();
        sb.append("\nUSAGE: java -classpath ... WorkflowComponentCreator")
            .append(" -file componentFile")
            .append(" [-help]");
        sb.append("\n");
        sb.append("\n-h, -help  \t Display this help and exit");
        sb.append("\n-f, -file  \t Required. File containing properties necessary to create component");
        logger.info(sb.toString());
    }

    private String getArg(String arg, int i, String[] args) {
        if (i < args.length) {
            return args[i];
        } else {
            logger.error("The " + arg + " must be specified with this argument.");
            return null;
        }
    }

    /**
     * Helper method to parse component info file.
     * Assumption is that the file is a Properties file.
     */
    private void parsePropsFile(String fileName) {
        logger.debug("Parsing properties file: " + fileName);

        Properties p = new Properties();
        try {
            File f = new File(fileName);
            if (f.exists()) {
                p.load(new FileInputStream(f));
            } else {
                logger.error("Failed to load component properties: " + fileName);
            }
        } catch (Exception e) {
            logger.error("Failed to load component properties: " + fileName + ". " + e.toString());
        }

        componentName = p.getProperty("component.name");
        componentType = p.getProperty("component.type");
        componentLang = p.getProperty("component.lang");
        componentAuthor = p.getProperty("component.author");
        componentAuthorEmail = p.getProperty("component.author.email");
        componentProgramDir = p.getProperty("component.program.dir");
        componentProgramFile = p.getProperty("component.program.file");
        componentPkg = p.getProperty("component.pkg");
        componentVersion = p.getProperty("component.version");
        componentDescription = p.getProperty("component.description");

        parseInputProps(p);
        parseOutputProps(p);
        parseOptionProps(p);
    }

    /** Constant. */
    private static final String DEFAULT_ZERO = "0";

    /**
     * Helper method to parse Input properties.
     */
    private void parseInputProps(Properties p) {
        int numInputs =
            Integer.valueOf(p.getProperty("component.num_input_nodes", DEFAULT_ZERO));
        inputs = new ArrayList<ComponentIO>();

        if (numInputs == 0) { return; }

        for (int i = 0; i < numInputs; i++) {
            String prefix = "input." + i;

            ComponentIO input = new ComponentIO();
            input.setIndex(i);

            // Get the input type
            String propName = prefix + ".type";
            String inputType = p.getProperty(propName);
            if (inputType == null) {
                logger.warn("Missing expected input property: '" + propName
                            + "'. Will skip input: " + i + ".");
                continue;
            }
            input.setType(inputType);

            // Get the input minimum number of files
            propName = prefix + ".min_num_files";
            String minNumFiles = p.getProperty(propName);
            if (minNumFiles == null) {
                logger.warn("Missing input property: '" + propName
                            + "'. Will use defualt value \"1\" for input: " + i + ".");
                minNumFiles = "1";
            }
            input.setMinOccurs(minNumFiles);

            // Get the input maximum number of files
            propName = prefix + ".max_num_files";
            String maxNumFiles = p.getProperty(propName);
            if (maxNumFiles == null) {
                logger.warn("Missing input property: '" + propName
                            + "'. Will use defualt value \"1\" for input: " + i + ".");
                maxNumFiles = "1";
            }
            input.setMaxOccurs(maxNumFiles);

            inputs.add(input);
        }

        // Check for one more input, just in case they set the number incorrectly.
        String extraProp = "input." + numInputs + ".type";
        String extraPropVal = p.getProperty(extraProp);
        if (extraPropVal != null) {
            logger.warn("Ignoring extra input property: '"
                        + extraProp
                        + "'. Update the component.num_inputs property to include this input.");
        }
    }

    /**
     * Helper method to parse Output properties.
     */
    private void parseOutputProps(Properties p) {
        int numOutputs =
            Integer.valueOf(p.getProperty("component.num_outputs", DEFAULT_ZERO));
        outputs = new ArrayList<ComponentIO>();

        if (numOutputs == 0) { return; }

        for (int i = 0; i < numOutputs; i++) {
            String prefix = "output." + i;

            ComponentIO output = new ComponentIO();
            output.setIndex(i);

            String propName = prefix + ".type";
            String outputType = p.getProperty(propName);
            if (outputType == null) {
                logger.warn("Missing expected output property: '" + propName
                            + "'. Will skip output: " + i + ".");
                continue;
            }
            output.setType(outputType);

            propName = prefix + ".name";
            String outputName = p.getProperty(propName);
            if (outputName == null) {
                logger.warn("Missing expected output property: '" + propName
                            + "'. Will skip output: " + i + ".");
                continue;
            }
            output.setName(outputName);

            outputs.add(output);
        }

        // Check for one more output, just in case they set the number incorrectly.
        String extraProp = "output." + numOutputs + ".type";
        String extraPropVal = p.getProperty(extraProp);
        if (extraPropVal != null) {
            logger.warn("Ignoring extra output property: '"
                        + extraProp
                        + "'. Update the component.num_outputs property to include this output.");
        }
    }

    /**
     * Helper method to parse Option properties.
     */
    private void parseOptionProps(Properties p) {
        int numOptions =
            Integer.valueOf(p.getProperty("component.num_options", DEFAULT_ZERO));
        options = new ArrayList<ComponentOption>();

        if (numOptions == 0) { return; }

        for (int i = 0; i < numOptions; i++) {
            String prefix = "option." + i;

            ComponentOption co = new ComponentOption();

            String propName = prefix + ".type";
            String optionType = p.getProperty(propName);
            if (optionType == null) {
                logger.warn("Missing expected option property: '" + propName
                            + "'. Will skip option: " + i + ".");
                continue;
            }
            co.setType(optionType);

            if (optionType.endsWith("FileInputHeader")) {
                // Set the inputs for which the columns should be read/parsed
                propName = prefix + ".node_index";
                String optionNodeIndex = p.getProperty(propName);
                if (optionNodeIndex != null) {
                    co.setInputNodeIndex(optionNodeIndex);
                }

                propName = prefix + ".file_index";
                String optionFileIndex = p.getProperty(propName);
                if (optionFileIndex != null) {
                    co.setInputFileIndex(optionFileIndex);
                }
            }

            propName = prefix + ".name";
            String optionName = p.getProperty(propName);
            if (optionName == null) {
                logger.warn("Missing expected option property: '" + propName
                            + "'. Will skip option: " + i + ".");
                continue;
            }
            co.setName(optionName);

            propName = prefix + ".id";
            String optionId = p.getProperty(propName);
            if (optionId == null) {
                logger.warn("Missing expected option property: '" + propName
                            + "'. Will skip option: " + i + ".");
                continue;
            }
            co.setId(optionId);

            propName = prefix + ".default";
            String optionDefault = p.getProperty(propName);
            if (optionDefault == null) {
                logger.warn("Missing expected option property: '" + propName
                            + "'. Will skip option: " + i + ".");
                continue;
            }
            co.setDefault(optionDefault);

            options.add(co);
        }

        // Check for one more option, just in case they set the number incorrectly.
        String extraProp = "option." + numOptions + ".type";
        String extraPropVal = p.getProperty(extraProp);
        if (extraPropVal != null) {
            logger.warn("Ignoring extra option property: '"
                        + extraProp
                        + "'. Update the component.num_options property to include this option.");
        }
    }

    /**
     * Helper method to display component info.
     */
    private String getComponentInfo() {
        StringBuffer sb = new StringBuffer("[");
        sb.append("name = ").append(componentName);
        sb.append(", type = ").append(componentType);
        sb.append(", lang = ").append(componentLang);
        sb.append(", author = ").append(componentAuthor);
        sb.append(", email = ").append(componentAuthorEmail);
        sb.append(", progDir = ").append(componentProgramDir);
        sb.append(", progFile = ").append(componentProgramFile);
        sb.append(", pkg = ").append(componentPkg);
        sb.append(", version = ").append(componentVersion);
        sb.append(", description = ").append(componentDescription);
        sb.append("]");
        return sb.toString();
    }

    /** Constant for info.xml template. */
    private static final String TEMPLATE_INFO_XML = "info.xml";
    /** Constant for xsd template. */
    private static final String TEMPLATE_TEST_XML = "test/components/ComponentTemplate.xml";
    /** Constant for source template. */
    private static final String TEMPLATE_SRC = "source/TemplateMain.java";
    /** Constant for external source template. */
    private static final String TEMPLATE_EXTERNAL_SRC = "source/TemplateExternalMain.java";
    /** Constant for build.properties. */
    private static final String TEMPLATE_BUILD_PROPS = "build.properties";

    /** Template files to be copied and modified. */
    private static final String[] templates =
        new String[] {"build.xml",
                      TEMPLATE_INFO_XML,
                      "README.md",
                      "log4j.properties",
                      TEMPLATE_BUILD_PROPS,
                      TEMPLATE_TEST_XML,
                      TEMPLATE_SRC};

    /**
     * This is the where the control of the overall process to import a file or set of files is.
     */
    private void run()
        throws IOException
    {
        logger.info("Running...");

        // Make the new component dir.
        // TBD: clean-up componentName to make sure it can be used for dir name
        File baseComponentDir = new File(baseDir + File.separator + componentName);
        if (baseComponentDir.exists()) {
            // Expecting to find an empty directory.
            logger.error("Please specify a directory that does not exist for the new component: "
                         + baseComponentDir);
            return;
        } else {
            // Create the new component directories.
            createComponentDirs(baseComponentDir);
        }

        //String templatesDir = baseDir + File.separator + TEMPLATES_DIR;
        File templateDir = new File(templatesDir);
        if (!templateDir.exists()) {
            logger.error("Unable to find directory with template files: " + templatesDir);
            return;
        }
        for (String t : templates) {
            createFromTemplate(t, baseComponentDir, templateDir);
        }

        // Create XSD based on specified inputs and options.
        createXSD(baseComponentDir);
        
        return;
    }

    /**
     * Helper method to create new component directories.
     */
    private void createComponentDirs(File baseComponentDir)
        throws IOException
    {

        baseComponentDir.mkdirs();
        File schemasDir = new File(baseComponentDir + File.separator + "schemas");
        schemasDir.mkdirs();
        File testDir = new File(baseComponentDir + File.separator + "test/components");
        testDir.mkdirs();

        // Work-around issue with Windows and the use of replaceAll and File.separator...
        String componentPkgMod = StringUtils.replace(componentPkg, ".", File.separator);
        StringBuffer pkgDir = new StringBuffer(componentPkgMod);
        pkgDir.append(File.separator);
        File srcDir = new File(baseComponentDir + File.separator + "source" + File.separator + pkgDir.toString());
        srcDir.mkdirs();

        if (componentProgramDir != null) {
            File progDir = new File(baseComponentDir + File.separator + "program");
            progDir.mkdirs();

            // Copy files into component's program dir
            File userProgDir = new File(componentProgramDir);
            if (userProgDir.exists() && userProgDir.isDirectory()) {
                FileUtils.copyDirectory(userProgDir, progDir);
            }
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean deleted = deleteDir(children[i]);
                if (!deleted) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Helper method to create files from Templates.
     */
    private void createFromTemplate(String fileName, File baseComponentDir, File templateDir) {

        // build.properties depends on componentLang
        if (fileName.equals(TEMPLATE_BUILD_PROPS)) {
            fileName = buildPropsMap.get(componentLang.toLowerCase());
            if (fileName == null) {
                fileName = SAMPLE_BUILD_PROPS;
            }
        }

        // If componentLang is not java, use different source template.
        if (fileName.equals(TEMPLATE_SRC) && !componentLang.equalsIgnoreCase("java")) {
            fileName = TEMPLATE_EXTERNAL_SRC;
        }

        try {
            Path template = Paths.get(templateDir + File.separator + fileName);
            String fileContent = new String(Files.readAllBytes(template), CHARSET);
            fileContent = fileContent.replaceAll("%COMPONENT_NAME%", componentName);
            fileContent = fileContent.replaceAll("%COMPONENT_TYPE%", componentType);
            fileContent = fileContent.replaceAll("%COMPONENT_LANG%", componentLang);
            fileContent = fileContent.replaceAll("%COMPONENT_PKG%", componentPkg);
            fileContent = fileContent.replaceAll("%CREATION_DATE%", DATE_FMT.format(new Date()));
            if (componentAuthor != null) {
                fileContent = fileContent.replaceAll("%COMPONENT_AUTHOR%", componentAuthor);
            }
            if (componentAuthorEmail != null) {
                fileContent = fileContent.replaceAll("%COMPONENT_AUTHOR_EMAIL%", componentAuthorEmail);
            }
            if (componentVersion != null) {
                fileContent = fileContent.replaceAll("%COMPONENT_VERSION%", componentVersion);
            }
            if (componentDescription != null) {
                fileContent = fileContent.replaceAll("%COMPONENT_DESCRIPTION%", componentDescription);
            }
            if (componentProgramFile != null) {
                fileContent = fileContent.replaceAll("%COMPONENT_PROGRAM_FILE%", componentProgramFile);
            }

            // Work-around issue with Windows and the use of replaceAll and File.separator...
            String componentPkgMod = StringUtils.replace(componentPkg, ".", File.separator + File.separator);

            // The following is (currently) only used in build.xml
            fileContent = fileContent.replaceAll("%COMPONENT_PKG_PATH%", componentPkgMod);

            // Source filename is special...
            if (fileName.equals(TEMPLATE_SRC) || fileName.equals(TEMPLATE_EXTERNAL_SRC)) {
                
                // Extraneous... part of simple example code.
                if (options.size() > 0) {
                    ComponentOption opt0 = options.get(0);
                    fileContent = fileContent.replaceAll("%OPT_NAME%", opt0.getName());
                }

                // Update various blocks of code that depend on inputs, outputs or options.
                fileContent = SourceHelper.updateSource(fileContent, inputs, outputs, options,
                                                        fileName.equals(TEMPLATE_EXTERNAL_SRC));

                StringBuffer sb = new StringBuffer("source");
                sb.append(File.separator);
                componentPkgMod = StringUtils.replace(componentPkg, ".", File.separator);
                sb.append(componentPkgMod);
                sb.append(File.separator);
                sb.append(componentName).append("Main.java");
                fileName = sb.toString();
            } else if (fileName.equals(TEMPLATE_TEST_XML)) {
                fileContent = XmlHelper.updateTestXml(fileContent, inputs, options);
                fileName = fileName.replaceAll("Template", componentName);
            } else if (fileName.startsWith(TEMPLATE_BUILD_PROPS)) {
                // Now strip the lang from fileName
                fileName = DEFAULT_BUILD_PROPS;
            } else if (fileName.equals(TEMPLATE_INFO_XML)) {
                fileContent = XmlHelper.updateInfoXml(fileContent, inputs, outputs, options);
            }

            Path newFile = Paths.get(baseComponentDir + File.separator + fileName);
            Files.write(newFile, fileContent.getBytes(CHARSET));
        } catch (IOException ioe) {
            logger.error("Failed to create from template: " + fileName + ": " + ioe.toString());
        }
    }

    private void createXSD(File baseComponentDir) {
        String xsdContents = SchemaHelper.createXsd(inputs, outputs, options);

        StringBuffer sb = new StringBuffer("schemas");
        sb.append(File.separator);
        sb.append(componentName).append("_");
        // Sigh. If componentVersion doesn't start with a 'v', add it.
        if (!componentVersion.startsWith("v")) { sb.append("v"); }
        sb.append(componentVersion.replaceAll("\\.", "_")).append(".xsd");
        String fileName = sb.toString();

        try {
            Path newFile = Paths.get(baseComponentDir + File.separator + fileName);
            Files.write(newFile, xsdContents.getBytes(CHARSET));
        } catch (IOException ioe) {
            logger.error("Failed to create XSD: " + fileName + ": " + ioe.toString());
        }
    }
}

