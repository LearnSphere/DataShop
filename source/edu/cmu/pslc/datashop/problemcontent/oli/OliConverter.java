/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.problemcontent.oli;

import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.PC_CONVERSION_STR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.problemcontent.Constants;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * This tool is used to transform OLI course packages into DataShop problem
 * content packages.
 *
 * @author Mike Komisin
 * @version $Revision: 11068 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2014-05-22 09:33:53 -0400 (Thu, 22 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliConverter extends
        edu.cmu.pslc.datashop.extractors.AbstractExtractor {

    /** Required bean structures. */

    /** The transactionTemplate is used for transaction callback. */
    private TransactionTemplate transactionTemplate;

    /** The session factory */
    private SessionFactory sessionFactory;

    /** Static constants. */

    /** OLI question types. */
    public static final String[] OLI_QUESTION_TYPES = {"multiple_choice",
            "text", "fill_in_the_blank", "numeric", "essay", "short_answer",
            "image_hotspot", "ordering" };

    /** OLI question types after conversion. */
    public static final String[] OLI_CONVERTED_QUESTION_TYPES = {"oli-multiple-choice",
            "oli-text", "oli-fill-in-the-blank", "oli-numeric", "oli-essay", "oli-short-answer",
            "oli-image-hotspot", "oli-ordering" };

    /** The base problem content directory path. */
    public static final String BASE_PROBLEM_CONTENT_PATH = "/datashop/";

    /** The location for placing unreferenced content. */
    public static final String UNREFERENCED_CONTENT = "unreferenced_content";

    /** Whether to append to a file. */
    private static final boolean IS_APPEND = true;

    /** Contains valid resource types defined in "tools/oli/oli-content-tools.conf". */
    private ArrayList<String> validResources = null;

    /**
     * Valid OLI DTD types found in the course package.
     */
    private static final String[] VALID_DTDS = {"oli_assessment",
            "oli_assessment_mathml", "oli_content_organization",
            "oli_content_package", "oli_discussion", "oli_feedback",
            "oli_inline_assessment", "oli_learning_objectives",
            "workbook_page", "workbook_page_mathml",
            "oli_workbook" };

    /** The name of this tool, used in displayUsage method. */
    private static final String TOOL_NAME = OliConverter.class
            .getSimpleName();

    /** The build properties file */
    private static final String BUILD_PROPERTIES = "build.properties";

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    /** Encoding used for the XML data. */

    /** The name of the resources directory for the generated content. */
    public static final String RESOURCE_DIR_NAME = "resources";

    /** The name displayed in the Conversion Tool field of HTML files. */
    public static final String CONVERSION_TOOL_NAME = "OLI Conversion Tool";

    /** The configuration file that contains valid OLI resources. */
    private static final String OLI_VALID_RESOURCE_FILE = "/tools/oli/oli-content-tools.conf";
    /** The configuration file that contains valid OLI resources for older courses. */
    private static final String ALTERNATIVE_OLI_VALID_RESOURCE_FILE =
        "/tools/oli/oli-package-builder.conf";

    /** Program data. */

    /** Path to the OLI course directory,
     * e.g. C:/OLIContent/a_and_p/a_and_p/tags/a_and_p_v_1_2-prod-2013-04-11 . */
    private static String inputDirectoryName = null;
    /** The output directory that will contain output files, e.g. C:/temp . */
    private static String outputDirectoryName = null;
    /** The course name for this course. */
    private String courseName = null;
    /** The course version for this course. */
    private String courseVersion = null;
    /** The content date string for this course. */
    private String contentDate = null;
    /** The base directory for the generated content package,
     * e.g. C:/output/a_and_p_v_1_2-prod-2013-04-11 . */
    private String conversionOutputPath = null;
    /** Base source directory for this tool. */
    private String baseDir;

    /** The organization files that exist for this course package. */
    private List<File> organizationFileList = null;

    /** A list of all XML files found within the course package. */
    private static List xmlFileList;

    /** A list of all valid workbook ids. */
    private List validWorkbooks;

    /** Provides the mapping of problem names (assessment and pool Ids) to the
     * parent assessment file. */
    private Map<String, List<String>> problemFileMap;

    /**
     * Provides the mapping of activities to the workbooks
     * they're in.
     */
    private Map<String, List<String>> workbookHierarchy;

    /**
     * Provides the mapping of problem names (assessment and pool Ids) to the
     * question elements. This map only used for questions in a pool.
     */
    private Map<String, List<Element>> poolElementMap;

    /** List used to ensure that all resources being copied
     * to the resources directory are unique files. The
     * key is guaranteed to be unique and is created in
     * the createNewResource method of OLIContentParser. */
    public static List<String> resourceFileList;

    /** XML structures. */

    /** Properties file key-values from build.properties. */
    private Properties properties;

    /** Constructor. */
    public OliConverter() {
        // Load properties from class path
        try {
            properties = new Properties();
            InputStream is = loadFromClasspath(BUILD_PROPERTIES);
            if (is != null) {
                properties.load(is);
            }
        } catch (Exception exception) {
            logError("Couldn't load build properties file.", exception);
        }

        // Instantiate maps and lists
        organizationFileList = new ArrayList<File>();
        problemFileMap = new HashMap<String, List<String>>();
        workbookHierarchy = new HashMap<String, List<String>>();
        poolElementMap = new HashMap<String, List<Element>>();
        resourceFileList = new ArrayList<String>();
        validResources = new ArrayList<String>();
        validWorkbooks = new ArrayList<String>();
    }

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate
     */
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Gets the sessionFactory.
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Sets the sessionFactory.
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("OliConverter.main");
        String version = VersionInformation.getReleaseString();
        logger.info("OliConverter starting (" + version + ")...");
        logger.info("System default character set: " + Charset.defaultCharset().name());
        logger.info("Reading XML files in character set: " + Constants.UTF8);
        logger.info("Writing HTML files in character set: " + Constants.UTF8);
        OliConverter converter =
            edu.cmu.pslc.datashop.extractors.ExtractorFactory.DEFAULT.getOliConverter();
        try {
            // Handle program execution parameters.
            converter.handleOptions(args);
            // Run the bootstrap method, run.
            converter.run(inputDirectoryName, outputDirectoryName);
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("OliConverter done.");
        }
    }

    /**
     * Handle command line arguments.
     * @param args command line arguments passed into main
     */
    public void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            displayUsage();
            return;
        }

        ArrayList<String> argsList = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        String argument;

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            argument = args[i].trim();

            if (argument.equals("-h") || argument.equals("-help")) {
                displayUsage();
                System.exit(0);
            } else if (argument.equals("-v") || argument.equals("-version")) {
                logDebug(VersionInformation.getReleaseString());
                System.exit(0);
            } else if (argument.equals("-courseDir")) {
                if (++i < args.length) {
                    inputDirectoryName = args[i];

                    inputDirectoryName = inputDirectoryName.replaceAll("\\\\", "/").replaceAll("/+", "/");
                    int len = inputDirectoryName.length();
                    if (!inputDirectoryName.substring(len - 1, len).equals("/")) {
                        inputDirectoryName = inputDirectoryName + "/";
                    }
                    logger.info("Input directory: " + inputDirectoryName);
                } else {
                    logger.error("A directory must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (argument.equals("-outputDir")) {
                if (++i < args.length) {
                    outputDirectoryName = args[i];

                    outputDirectoryName = outputDirectoryName
                        .replaceAll("\\\\", "/").replaceAll("/+", "/");
                    int len = outputDirectoryName.length();
                    if (!outputDirectoryName.substring(len - 1, len)
                            .equals("/")) {
                        outputDirectoryName = outputDirectoryName
                                + "/";
                    }
                    logger.info("Output directory: " + outputDirectoryName);
                } else {
                    System.out.println("An output directory must be specified with this argument");
                    logger.error("An output directory must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-courseName")) {
                if (++i < args.length) {
                    courseName = args[i];
                }
            } else if (args[i].equals("-courseVersion")) {
                if (++i < args.length) {
                    courseVersion = args[i];
                }
            } else if (args[i].equals("-contentDate")) {
                if (++i < args.length) {
                    contentDate = args[i];
                }
            } else if (args[i].equals("-baseDir")) {
                if (++i < args.length) {
                    baseDir = args[i];
                } else {
                    logger.error("Error: a directory name must be specified "
                                 + "with the -baseDir argument");
                    displayUsage();
                    System.exit(1);
                }
            } else {
                logger.info(" *** Unknown command-line option: " + args[i]);
                displayUsage();
                System.exit(1);
            }
        } // end for-each args

        // Check for the required arguments
        boolean requiredArguments = true;

        if (inputDirectoryName == null || outputDirectoryName == null
                || courseName == null || courseVersion == null || contentDate == null) {
            requiredArguments = false;
            logError("An input directory (-courseDir STRING), an output directory "
                + "(-outputDir STRING), course name (-courseName STRING), "
                + "course version (-courseVersion STRING), "
                + " and course date (-contentDate \"MMMM dd, yyyy\") "
                + " must be specified on the command-line.");
        } else {
            courseName = courseName.replaceAll(
                    CommonXml.BAD_FILEPATH_CHARS, "_");
            courseVersion = courseVersion.replaceAll(
                    CommonXml.BAD_FILEPATH_CHARS, "_");
            conversionOutputPath = outputDirectoryName + courseName + "_"
                    + courseVersion;
            conversionOutputPath = conversionOutputPath
                .replaceAll("\\\\", "/").replaceAll("/+", "/");
        }

        if (!requiredArguments) {
            displayUsage();
            System.exit(1);
        }
    }

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
        StringBuffer usageMessage = new StringBuffer();
        usageMessage.append("\nUSAGE: java -classpath ... " + TOOL_NAME
                + " [-help] [-version]" + " -outputDir output_directory\n"
                + " [-courseDir oli_course_directory]"
                + " [-courseName course_name]"
                + " [-courseVersion course_version]" + " [-debug]\n"
                + " [-baseDir baseDirectory]"
                + " Option -courseDir must be specified.\n"
                + " Option -outputDir must be specified..\n"
                + " Option -courseName must be specified..\n"
                + " Option -courseVersion must be specified..\n");

        usageMessage.append("Options:");
        usageMessage.append("\t-h, -help        \t Display this help and exit");
        usageMessage.append("\t-v, -version     \t Display the version and exit");
        usageMessage.append(
            "\t-outputDir \t Output the new problem content package XML to this directory.");
        usageMessage.append(
            "\t-courseDir \t The parent directory of the OLI course content package.");
        usageMessage.append("\t-courseName \t The OLI course name.");
        usageMessage.append("\t-courseVersion \t The OLI course version.");
        usageMessage.append("\t-debug,\t  Print debugging messages");
        logInfo(usageMessage.toString());
        System.exit(-1);
    }

    /**
     * Loads the properties file from the classpath.
     * @param fileName name of properties file
     * @return InputStream that can be loaded into a Properties object.
     * @throws Exception an exception
     */
    private InputStream loadFromClasspath(String fileName) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(fileName);
    }

    /**
     * Extract OLI content from an OLI course package.
     * @param inputDirectory the path to the OLI course package directory
     * @param outputDirectory output problem content XML to this directory
     */
    private void run(String inputDirectory, String outputDirectory) {
        File courseDir = new File(inputDirectory);
        // If the course directory exists, iterate through the problems and selections
        if (!courseDir.exists() || !courseDir.isDirectory()) {
            logger.error("Course directory " + inputDirectory
                    + " is not a directory!");
            return;
        }

        // Get the valid resource types
        validResources = getValidResourceTypes(inputDirectory);

        // Gather information from all the organization.xml files.
        List<Organization> organizations = getOrganizations(inputDirectory,
                organizationFileList);

        // Get all the xml files from the course package once.
        xmlFileList = getFilenameList(logger, inputDirectory + "/content", "xml");


        // Delete existing problem content for the course if it exists
        File tagDir = new File(getTagDir());
        if (tagDir.isDirectory()) {
            logger.info("Deleting existing files for: "
                + tagDir.getAbsolutePath());
            CommonXml.recursiveDelete(tagDir);
        }

        // For each organization, traverse the OLI content, and create
        // a DataShop problem content package based on organization hierarchies.
        for (Organization organization : organizations) {
            ContentWriter contentWriter = new ContentWriter(getTagDir(),
                                                            outputDirectoryName, courseName,
                                                            courseVersion, contentDate, baseDir);

            logger.info("Input directory: " + inputDirectory);
            logger.info("Output base directory: " + outputDirectory);

            String metaDataDir = outputDirectoryName + "/" + PC_CONVERSION_STR;
            String metaDataFilePath = metaDataDir
                    + "/" + PC_CONVERSION_STR + "_" + courseName + "_"
                    + courseVersion + "_" + getOrgForPath(organization) + ".txt";
            File metaDataFile = new File(metaDataFilePath);
            if (metaDataFile.isFile()) {
                metaDataFile.delete();
            }


            // Resolve valid workbooks
            getValidWorkbooks(organization, inputDirectory, outputDirectory);
            // Resolve all indirect references and catalog resources copied
            // to the new location.
            resolveReferences(organization, inputDirectory, outputDirectory);
            // Generate the HTML content by extracting questions and dereferencing
            // question selection pool elements. Because multiple workbooks and question pools
            // may exist for a single hierarchy, and because files are not indexed within
            // the OLI course absolutely, the file being appended to is determined
            // by the workbook's hierarchy defined in the organization. This means
            // that we do not add the </html> closing tag until all problems have been
            // extracted and written to their appropriate hierarchy.
            logger.info("Generating problem content for organization: " + organization.getId());
            generateProblemContent(organization, inputDirectory, outputDirectory);

            String pathToOrganizationOutput = getTagDir() + "/" + getOrgForPath(organization);
            logger.info("Sorting, pruning, and finishing HTML files in organization path: "
                + pathToOrganizationOutput);
            // Cap off HTML files with </html>.
            contentWriter.addHtmlClosingTags(getTagDir() + "/" + getOrgForPath(organization));
            // Since we cannot sort a content page until all data is finished being written
            // to the page, we must sort the problems after they have been written to file.
            // This prevents us from having to store the problems for all the hierarchies
            // in memory at once at the cost of slower content generation.
            contentWriter.sortProblemsInHtmlFiles(getTagDir() + "/" + getOrgForPath(organization));
        }
    }

    /**
     * Retrieves a list valid resource types from tools/oli/oli-content-tools.conf.
     * @param configFilePath the path to the configuration file, tools/oli/oli-content-tools.conf
     * @return a list valid resource types
     */
    private ArrayList<String> getValidResourceTypes(String configFilePath) {
        String contentToolsPath = OLI_VALID_RESOURCE_FILE;
        ArrayList<String> validResourceTypes = new ArrayList<String>();
        SAXBuilder builder = new SAXBuilder();
        // Setting reuse parser to false is a workaround
        // for a JDK 1.7u45 bug described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);
        File configFile = new File(configFilePath + contentToolsPath);
        if (!configFile.exists()) {
            contentToolsPath = ALTERNATIVE_OLI_VALID_RESOURCE_FILE;
            configFile = new File(configFilePath + contentToolsPath);
        }

        Document doc = getDocument(builder, configFilePath + contentToolsPath, configFile);
        Element root = doc.getRootElement();
        Element resourceTypesElement = root.getChild("resource_types");
        List<Element> resourceTypes = resourceTypesElement.getChildren("resource_type");
        for (Element resourceType : resourceTypes) {
            validResourceTypes.add(resourceType.getAttributeValue("id"));
        }

        return validResourceTypes;
    }

    /**
     * Retrieves a list of Organization objects for this course.
     * @param inputPath the path to the OLI course package directory
     * @param organizationFileList the file list
     * @return a list of Organization objects
     */
    private List<Organization> getOrganizations(String inputPath,
            List<File> organizationFileList) {
        String orgPath = inputPath + "organizations";
        logger.info("Retrieving organization.xml files from "
                + orgPath);
        // Recursively fetch all of the XML files within the organizations directory.
        // Ignore files that are not organization.xml files.
        List fileList = getFilenameList(logger, orgPath, "xml");
        List<Organization> organizationList = new ArrayList<Organization>();
        // For each XML file
        for (int j = 0, m = fileList.size(); j < m; j++) {
            File organizationFile = (File) fileList.get(j);
            String filename = organizationFile.getName();

            // Only get organization.xml files if they are in the immediate
            // subdirectory under organizations
            Boolean isValidOrganization = false;
            Integer countSubDirectories = 0;
            String orgPathStandard = orgPath
                .replaceAll("\\\\", "/").replaceAll("/+", "/");
            String filenameStandard = organizationFile.getAbsolutePath()
                .replaceAll("\\\\", "/").replaceAll("/+", "/");
            String[] splitDir = filenameStandard.replaceAll(
                orgPathStandard + "/", "").split("/");
            countSubDirectories = splitDir.length;
            if (countSubDirectories == 2) {
                isValidOrganization = true;
            }


            // Is it an organization.xml file?
            if (organizationFile.isFile()
                    && filename.equals("organization.xml")
                    && isValidOrganization) {
                logger.debug("Reading organization file "
                        + organizationFile.getAbsolutePath() + " ("
                        + (j + 1) + "/" + m + ")");
                // Store the organizations in a list for future reference
                Organization organization = new Organization(courseName + "_" + courseVersion,
                        organizationFile);
                if (organization != null) {
                    organizationList.add(organization);
                    // Copy organization files to the content package
                    logger.debug("Copying organization file " + organization.getId());
                    organization.copyToOutput(getTagDir());
                }
            }

        }
        return organizationList;
    }

    /**
     * Store selection pool references in the poolElementMap prior to XML generation.
     * Also, copy resource file referenced by the elements to the new content package.
     * Relative links within the elements are modified based on their new location
     * relative to their hierarchy.
     * @param organization the organization object
     * @param inputDirectory the directory that contains the OLI course content
     * @param outputDirectory output problem content XML to this directory
     */
    private void resolveReferences(Organization organization,
            String inputDirectory, String outputDirectory) {

        SAXBuilder builder = new SAXBuilder();
        // Setting reuse parser to false is a workaround
        // for a JDK 1.7u45 bug described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {

            // Create parent and resource directories.
            File resourceDir = CommonXml.createDirectory(getTagDir()
                    + "/" + RESOURCE_DIR_NAME);
            // Resolve all indirect references in the OLI content.
            for (int j = 0, m = xmlFileList.size(); j < m; j++) {
                File courseContentFile = (File) xmlFileList.get(j);
                String filename = courseContentFile.getName();
                String filePath = courseContentFile.getAbsolutePath().replaceAll("\\\\", "/");
                File parentDir = new File(courseContentFile.getParent());
                Boolean isValidCourseFile = false;
                for (String resourceType : validResources) {
                    if (parentDir.getAbsolutePath().contains(resourceType)) {
                        isValidCourseFile = true;
                        break;
                    }
                }

                if (courseContentFile.isFile() && isValidCourseFile) {
                    logger.debug("Resolving indirect references in file, "
                        + courseContentFile.getAbsolutePath() + " (" + (j + 1) + "/" + m + ")");
                    // Get the OLI document type element from the XML, if one exists.
                    // Older courses do not adhere to an OLI DTD.
                    String docType = getDocType(courseContentFile);

                    // If the file contains OLI problem content which adheres
                    // to an OLI DTD, then resolve all references.
                    if (docType != null) {
                        // Parse out all the
                        Document xmlInput = getDocument(builder,
                            filename, courseContentFile);

                        if (xmlInput != null) {
                            // Parse the file
                            OliContentParser parser = OliContentParserFactory
                                    .getInstance().get(xmlInput, courseContentFile, validWorkbooks);

                            if (parser != null) {
                                parser.setInputDir(inputDirectory);
                                parser.setCourseName(courseName);
                                parser.setCourseVersion(courseVersion);
                                // Since we are resolving indirect references in the
                                // problem content, resolveMappings should be set to true.
                                Boolean resolveMappings = true;

                                // Get mappings if workbook
                                parser.parseWorkbookDocument(workbookHierarchy, problemFileMap,
                                    poolElementMap, organization, courseContentFile, resourceDir);
                                // Get mappings if assessment
                                parser.parseAssessmentDocument(workbookHierarchy, problemFileMap,
                                    poolElementMap, organization, courseContentFile, resourceDir);
                                // Map problems to files and elements to their containers
                                parser.parseXMLDocument(
                                    // The workbook hierarchy
                                    workbookHierarchy,
                                    // The problemFileMap is needed to later resolve question pools
                                    problemFileMap,
                                    // The poolElementMap links problems to pools
                                    poolElementMap,
                                    // The organization
                                    organization,
                                    // The parent directory for this OLI content file
                                    courseContentFile,
                                    // The resources directory for generated content
                                    resourceDir,
                                    // Whether or not to resolve indirect references
                                    resolveMappings);


                            } else {
                                logger.warn("Error parsing XML in file "
                                        + filename);
                            }
                        } else {
                            logger.error("Could not parse " + filename
                                    + ". The XML could be malformed.");
                        }
                    }
                } else {
                    logger.debug("Invalid file " + filePath);
                }

            } // end for-each XML file

            logger.info("\nFinished mapping references.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("Unknown error.", throwable);
        }
    }

    /**
     * Store selection pool references in the poolElementMap prior to XML generation.
     * Also, copy resource file referenced by the elements to the new content package.
     * Relative links within the elements are modified based on their new location
     * relative to their hierarchy.
     * @param organization the organization object
     * @param inputDirectory the directory that contains the OLI course content
     * @param outputDirectory output problem content XML to this directory
     */
    private void getValidWorkbooks(Organization organization,
            String inputDirectory, String outputDirectory) {

        SAXBuilder builder = new SAXBuilder();
        // Setting reuse parser to false is a workaround
        // for a JDK 1.7u45 bug described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {

            // Create parent and resource directories.
            File resourceDir = CommonXml.createDirectory(getTagDir()
                    + "/" + RESOURCE_DIR_NAME);
            // Resolve all indirect references in the OLI content.
            for (int j = 0, m = xmlFileList.size(); j < m; j++) {
                File courseContentFile = (File) xmlFileList.get(j);
                String filename = courseContentFile.getName();
                String filePath = courseContentFile.getAbsolutePath().replaceAll("\\\\", "/");
                File parentDir = new File(courseContentFile.getParent());
                String parentDirPath = parentDir.getAbsolutePath().replaceAll("\\\\", "/");
                Boolean isValidCourseFile = false;
                for (String resourceType : validResources) {
                    if (parentDirPath.contains(resourceType)) {
                        isValidCourseFile = true;
                        break;
                    }
                }

                if (courseContentFile.isFile() && isValidCourseFile) {
                    // Get the OLI document type element from the XML, if one exists.
                    // Older courses do not adhere to an OLI DTD.
                    String docType = getDocType(courseContentFile);

                    // If the file contains OLI problem content which adheres
                    // to an OLI DTD, then resolve all references.
                    if (docType != null) {
                        // Parse out all the
                        Document xmlInput = getDocument(builder,
                            filename, courseContentFile);

                        if (xmlInput != null) {
                            // Parse the file
                            OliContentParser parser = OliContentParserFactory
                                    .getInstance().get(xmlInput, courseContentFile, validWorkbooks);

                            if (parser != null) {
                                parser.setInputDir(inputDirectory);
                                parser.setCourseName(courseName);
                                parser.setCourseVersion(courseVersion);
                                // Since we are resolving indirect references in the
                                // problem content, resolveMappings should be set to true.
                                Boolean resolveMappings = true;

                                // Map workbooks to their workbook containers
                                parser.mapWorkbookElements(workbookHierarchy,
                                    problemFileMap,
                                    poolElementMap, organization, courseContentFile, resourceDir);


                            } else {
                                logger.warn("Error parsing XML in file "
                                        + filename);
                            }
                        } else {
                            logger.error("Could not parse " + filename
                                    + ". The XML could be malformed.");
                        }
                    }
                } else {
                    logger.debug("Invalid file " + filePath);
                }

            } // end for-each XML file

            logger.info("\nFinished getting valid workbooks.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("Unknown error.", throwable);
        }
    }

    /**
     * Reads the document as plain text to search for a valid DTD reference.
     * This method does not guarantee that the syntax is correctly formed.
     * @param courseContentFile the XML file
     * @return the DOCTYPE element as a string, or null if it is not a valid OLI
     * problem content file
     */
    private String getDocType(File courseContentFile) {
        // UTF8 InputStream
        InputStream inputStream;
        // Whether the file is a valid OLI problem content document?
        boolean isValid = false;
        // The DTD that the file references, or null if it is not
        // a valid OLI content document
        String docTypeString = null;
        try {
            inputStream = new FileInputStream(courseContentFile);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, Constants.UTF8), Constants.IS_READER_BUFFER);
            String line = null;
            StringBuffer sBuffer = null;
            // Does this file include a document type element, and
            // if so, does it match a valid OLI DTD pattern?
            do {
                line = br.readLine();
                String stringPattern = ".*<!DOCTYPE.*";

                if (line != null && line.matches(stringPattern)) {
                    sBuffer = new StringBuffer();
                    if (line.indexOf(">") > line.indexOf("<!DOCTYPE")) {
                        sBuffer.append(
                            line.substring(line.indexOf("<!DOCTYPE"),
                                line.indexOf(">")));
                        break;
                    } else {
                        sBuffer.append(
                            line.substring(line.indexOf("<!DOCTYPE")));
                    }

                } else if (line != null && sBuffer != null) {
                    if (line.indexOf(">") > 0) {
                        sBuffer.append(
                            line.substring(0, line.indexOf(">")));
                        break;
                    } else {
                        sBuffer.append(line);
                    }
                }

            } while (line != null);

            if (sBuffer != null) {
                docTypeString = sBuffer.toString();
                // Does this match an OLI doc type?
                for (String validDTD : VALID_DTDS) {
                    if (docTypeString.contains(validDTD)) {
                        isValid = true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("File " + courseContentFile.getAbsolutePath()
                    + " not found.");
        } catch (IOException e) {
            logger.error("Could not open "
                    + courseContentFile.getAbsolutePath() + " for reading.");
        }

        if (isValid) {
            return docTypeString;
        } else {
            return null;
        }
    }


    /**
     * Parse the problem content from the OLI content files, and generate a new
     * Document in DataShop ds_problem_content format
     *
     * @param organization the organization object
     * @param inputDirectory the directory that contains the OLI course content
     * @param outputDirectory output problem content XML to this directory
     */
    private void generateProblemContent(Organization organization, String inputDirectory,
            String outputDirectory) {
        // Delete existing pc_conversion file if it exists


        // Print debugging info about the organization if debug is on
        organization.debug();

        ContentWriter contentWriter = new ContentWriter(getTagDir(),
                                                        outputDirectoryName, courseName,
                                                        courseVersion, contentDate, baseDir);

        SAXBuilder builder = new SAXBuilder();
        // Set reusable to false is a workaround for the JDK 1.7u45 bug
        // described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {
            Integer totalProblems = 0;

            File resourceDir = CommonXml.createDirectory(getTagDir()
                    + "/" + RESOURCE_DIR_NAME);

            logger.info("\nExtracting content, dereferencing selection pools, and "
                    + "standardizing question formats.");

            for (int j = 0, m = xmlFileList.size(); j < m; j++) {

                // Find all files recursively in the course package
                File courseContentFile = (File) xmlFileList.get(j);

                String filename = courseContentFile.getName();
                String filePath = courseContentFile.getAbsolutePath().replaceAll("\\\\", "/");
                File parentDir = new File(courseContentFile.getParent());
                String parentDirPath = parentDir.getAbsolutePath().replaceAll("\\\\", "/");
                Boolean isValidCourseFile = false;
                for (String resourceType : validResources) {
                    if (parentDirPath.contains(resourceType)) {
                        isValidCourseFile = true;
                        break;
                    }
                }

                if (courseContentFile.isFile() && isValidCourseFile) {
                    logger.debug("Transforming OLI content from file, "
                        + courseContentFile.getAbsolutePath() + " (" + (j + 1) + "/" + m + ")");
                    // Returns a non-null string unless the xml type is not an OLI document
                    String docType = getDocType(courseContentFile);

                    if (docType != null) {
                        logger.debug("Parsing " + courseContentFile.getAbsolutePath());
                        // The file could be an xml or txt file with XML inside
                        Document xmlInput = getDocument(builder, filename, courseContentFile);
                        if (xmlInput != null) {
                            // Create a new parser instance for this file.
                            OliContentParser parser = OliContentParserFactory
                                .getInstance().get(xmlInput, courseContentFile, validWorkbooks);

                            if (parser != null) {
                                // Set the metadata to be used by the parser.
                                parser.setInputDir(inputDirectory);
                                parser.setCourseName(courseName);
                                parser.setCourseVersion(courseVersion);

                                Boolean resolveMappings = false;
                                // Parse document for OLI problems.
                                Document doc = parser.parseXMLDocument(
                                        workbookHierarchy,
                                        problemFileMap,
                                        poolElementMap, organization,
                                        courseContentFile, resourceDir, resolveMappings);

                                if (doc == null) {
                                    continue;
                                }

                                // Iterate over the pre-formatted OLI problems.
                                List problems = doc.getRootElement()
                                        .getChildren("problem");
                                if (problems != null && !problems.isEmpty()) {

                                    for (int probCount = 0; probCount < problems
                                            .size(); probCount++) {
                                        Element probElem = (Element) problems
                                                .get(probCount);
                                        // Tie the problem back to the organization hierarchy
                                        String problemName = probElem
                                                .getChildText("name");

                                        /////if (problemName.equals("M5L2_pool1_q1")) {

                                        /////}

                                        String workbookId = organization
                                                .getWorkbookFromElementId(problemName);
                                        // Account for assessments directly referenced by organizations
                                        if (workbookId == null) {
                                            workbookId = problemName;
                                        }
                                        String containerId = organization
                                                .getContainerFromWorkbook(workbookId);

                                        String hierarchy = organization
                                                .getHierarchyFromElement(containerId);
                                        String hierarchyPath = null;

                                        if (hierarchy == null) {
                                            hierarchy = UNREFERENCED_CONTENT;
                                        }

                                        hierarchyPath = getTagDir()
                                                + "/" + getOrgForPath(organization)
                                                + "/" + hierarchy.replaceAll(
                                                    CommonXml.BAD_FILEPATH_CHARS, "_");

                                        hierarchyPath = hierarchyPath
                                            .replaceAll("\\\\", "/").replaceAll("/+", "/");
                                        // Disregard problems without any data in the prompt

                                        if (probElem.getChild("prompt")
                                                .getChildren() != null
                                                && !probElem.getChild("prompt")
                                                        .getChildren()
                                                        .isEmpty()) {
                                            probElem.detach();
                                            // Count each problem that is written to the page
                                            if (!hierarchy.equals(UNREFERENCED_CONTENT)) {
                                                totalProblems += contentWriter.writeOut(
                                                    probElem, hierarchyPath, courseContentFile,
                                                        organization);
                                            }
                                        } else {
                                            logger.debug("A selection pool was found, "
                                                + probElem.getChildText("name")
                                                + ", and the content was already extracted. ");
                                        }
                                    }
                                }
                            } else {
                                logger.error("Error parsing XML in file "
                                        + filePath);
                            }
                        } else {
                            logger.debug("Skipping "
                                    + filePath
                                    + ". Could not recover any OLI problem content from file.");
                        }
                    } else {
                        logger.debug("Skipping "
                                + filePath
                                + ". The file is not a desired OLI document type.");
                    }
                } else {
                    logger.debug("Invalid file " + filePath);
                }
            } // end for-each file

            logger.info("Found " + totalProblems + " problems.");
            logger.info("\nFinished generating HTML content.");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("Unknown error.", throwable);
        }
    }

    /**
     * Gets the directory for the generated content package,
     * e.g. C:/temp/a_and_p_v_1_2-prod-2013-04-11 where a_and_p is the course
     * name and v_1_2-prod-2013-04-11 is the branch or tag.
     * @return the base directory for the generated content package
     */
    private String getTagDir() {
        return conversionOutputPath;
    }

    /**
     * Gets the sanitized organization Id for file paths.
     * @param organization the organization
     * @return the sanitized organization Id for file paths
     */
    private String getOrgForPath(Organization organization) {
        return organization
        .getId()
            .replaceAll(
                CommonXml.BAD_FILEPATH_CHARS, "_");
    }

    /**
     * Returns an array of files given a directory and an optional extension.
     * @param logger the debug logger
     * @param directoryName the directory path
     * @param extension the file extension or null to return any types of files
     * @return an array of files
     */
    public static List getFilenameList(Logger logger, String directoryName,
            String extension) {

        File topLevelDirectory = new File(directoryName);
        if (!topLevelDirectory.isDirectory()) {
            logger.warn("Not a directory: " + directoryName);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Directory found: " + directoryName);
        }
        logger.debug("Top level directory is " + topLevelDirectory.getName());

        return getFiles(logger, topLevelDirectory, extension);
    }

    /**
     * A recursive function to return a list of all the files in a top level
     * directory, including all the subdirectories. This method will skip CVS
     * and SVN directories. An optional extension parameter will filter by
     * extension.
     * @param logger the debug logger
     * @param file a file or directory
     * @param extension the file extension or null to return any types of files
     * @return a list of files
     */
    private static List getFiles(Logger logger, File file, String extension) {
        List fileList = new ArrayList();

        if (file.isFile()
                && (file.getName().matches(".*\\." + extension) || extension == null)) {
            logger.debug("Adding file " + file.getName());
            fileList.add(file);
        } else if (file.isDirectory()) {
            if (file.getName().equals("CVS") || file.getName().equals(".svn")) {
                logger.debug("Skipping repository directory " + file.getName());
            } else {
                logger.debug("Found directory " + file.getName());
                File[] files = file.listFiles();

                for (int idx = 0; idx < files.length; idx++) {
                    if (files[idx].isFile()
                            && files[idx].getName()
                                    .matches(".*\\." + extension)) {
                        fileList.add(files[idx]);
                        logger.debug("Adding file: " + files[idx]);
                    } else if (files[idx].isDirectory()) {
                        List moreFiles = getFiles(logger, files[idx], extension);
                        fileList.addAll(moreFiles);
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * Returns problem content given a filename.
     * @param builder the SAXBuilder
     * @param fileName the name of the file
     * @param file the XML file
     * @return the XML in the given file
     */
    private Document getDocument(SAXBuilder builder,
            String fileName, File file) {
        Document document = null;
        try {
            // Fixed SVN files with sed to replace thelsamar.andrew.cmu.edu with oli.web.cmu.edu
            document = builder.build(file);
        } catch (IOException ex) {
            logger.warn("IOException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMParseException ex) {
            logger.warn("JDOMParseException occurred with " + fileName + ". ",
                    ex);
            document = null;
        } catch (JDOMException ex) {
            logger.warn("JDOMException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (Exception parseException) {
            logger.warn("Not an OLI problem content file: " + fileName);
        }
        return document;
    }

    /**
     * THIS METHOD IS CURRENTLY UNUSED, BUT IT COULD BE MODIFIED TO VERIFY THAT THERE ARE NO
     * MISSING PROBLEMS IN AN OLI COURSE PACKAGE WHEN CONVERTING, GIVEN A DATASET ID.
     *
     *
     * For each problem in the dataset, extract the OLI problem content.
     *
     * @param datasetId the dataset Id
     * @return the ProblemItem
     */
    private ProblemItem verifyProblemContentExists(Integer datasetId) {

        /*
         * STUB: Map<ProblemItem, ProblemContentMap> problemContentMaps = new
         * HashMap<ProblemItem, ProblemContentMap>();
         *
         * DatasetDao datasetDao = DaoFactory.HIBERNATE.getDatasetDao();
         * DatasetItem datasetItem = datasetDao.get(datasetId);
         *
         * if (datasetItem == null) { return null; }
         *
         * ProblemDao problemDao = DaoFactory.HIBERNATE.getProblemDao();
         * SubgoalDao subgoalDao = DaoFactory.HIBERNATE.getSubgoalDao();
         * SelectionDao selectionDao = DaoFactory.HIBERNATE.getSelectionDao();
         *
         * List<ProblemItem> problemList = problemDao.find(datasetItem); for
         * (ProblemItem problemItem : problemList) { ProblemContentMap pcMap =
         * new ProblemContentMap(problemItem); List<SubgoalItem> subgoalList =
         * subgoalDao.find(problemItem);
         *
         * List<SelectionItem> selectionList = selectionDao
         * .findOrderBySubgoal(problemItem); logger.debug("Subgoals: " +
         * subgoalList.size() + ", Selections: " + selectionList.size());
         *
         * for (SelectionItem selectionItem : selectionList) { //
         * List<ActionItem> actionList = // subgoalItem.getActionsExternal(); //
         * List<InputItem> inputList = subgoalItem.getInputsExternal();
         *
         * SubgoalItem subgoalItem = subgoalDao.get((Long) selectionItem
         * .getSubgoal().getId());
         *
         * // // File file = findContentFile(selection); // / String xmlData =
         * getXmlElement(selection, file); // / pcMap.addFile(selectionItem,
         * file); // / pcMap.addXML(selectionItem, "somexml");
         *
         * logger.debug("Dataset: " + datasetItem.getDatasetName());
         * logger.debug("Problem: " + problemItem.getProblemName());
         * logger.debug("Subgoal: " + subgoalItem.getSubgoalName());
         * logger.debug("Selection: " + selectionItem.getSelection());
         * logger.debug(""); } problemContentMaps.put(problemItem, pcMap); }
         */
        return null;
    }

    /**
     * Only log if debugging is enabled. @param args concatenate objects into
     * one string
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /**
     * Only log if info is enabled. @param args concatenate objects into one
     * string
     */
    private void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }

    /** Log warning message. @param args concatenate objects into one string */
    private void logWarn(Object... args) {
        LogUtils.logWarn(logger, args);
    }

    /** Log error message. @param args concatenate objects into one string */
    private void logError(Object... args) {
        LogUtils.logErr(logger, args);
    }

}