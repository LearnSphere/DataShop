/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.problemcontent.tutorshop;

import edu.cmu.pslc.datashop.extractors.AbstractExtractor;
import edu.cmu.pslc.datashop.extractors.ExtractorFactory;

import edu.cmu.pslc.datashop.item.PcConversionItem;

import edu.cmu.pslc.datashop.problemcontent.PopulatePcTables;
import edu.cmu.pslc.datashop.problemcontent.SvnUtil;
import static edu.cmu.pslc.datashop.problemcontent.Constants.RESOURCES_PATH;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.DATE_FMT_STR;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.OUTPUT_DIRECTORY_STR;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.PC_CONVERSION_STR;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.SEPARATOR;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.StringUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Reads a TutorShop problem_set.xml file and generates DataShop-formatted HTML.
 *
 * @author Cindy Tipper
 * @version $Revision: 13773 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-01-24 13:55:46 -0500 (Tue, 24 Jan 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorShopConverter extends AbstractExtractor {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Name of directory where Problem Content exists. */
    private String inputDir = null;

    /** Name of output directory. */
    private String outputDir = null;

    /** Name of conversion output directory. */
    private String conversionDir = null;

    /** Common portion of pathname. Used to construct resources dir path. */
    private String commonPath = null;

    /** ProblemSet-specific Selection value keyword for prompt. */
    private List<String> selectionValueKeywords = null;

    /** SVN user name. Used to determine problem content date. */
    private String svnUser;

    /** SVN password. Used to determine problem content date. */
    private String svnPassword;

    /** Base source directory for this tool. */
    private String baseDir;

    /** Constant for top-level directory structure. */
    private static final String PARENT_DIR = "TutorShop";

    /** Constant for the name of the conversion tool. */
    private static final String CONVERSION_TOOL_NAME = PcConversionItem.TUTORSHOP_CONVERTER;

    /** Constant for the name of the problem set definition file. */
    private static final String PROBLEM_SET_DEFN_FILE = "problem_set.xml";

    /** Definition of single newline for formatting output. */
    private static final String SINGLE_NEW_LINE = "\r\n";
    /** Definition of double newlines for formatting output. */
    private static final String DOUBLE_NEW_LINES = "\r\n\r\n";

    /** Constant for the name of CSS file. */
    private static final String CSS_FILE_URL = "https://pslcdatashop.web.cmu.edu/pc/pc.css";

    /** Constant for the format of dates. */
    public static final FastDateFormat DATE_FMT = FastDateFormat.getInstance(DATE_FMT_STR);

    /**
     * The constructor.
     */
    public TutorShopConverter() { }

    /**
     * Returns JDOM Document given a file.
     * @param theFile as File
     * @return the XML in the given file
     */
    private Document readInputFile(File theFile) {
        Document document = null;
        String fileName = theFile.getName();
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            document = saxBuilder.build(theFile);
        } catch (IOException ex) {
            logger.warn("IOException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMException ex) {
            logger.warn("JDOMException occurred with " + fileName + ". ", ex);
            document = null;
        }
        return document;
    }

    /**
     * The main method of this converter.
     */
    public void convert() {
        try {
            File inputFile = new File(inputDir + "/" + PROBLEM_SET_DEFN_FILE);

            String fileName = inputFile.getName();
            logger.info("Reading file " + inputFile.getCanonicalPath());
            if (inputFile.exists()) {
                Document xmlDocument = readInputFile(inputFile);

                // A list of resources (files) to be referenced.
                List<File> resourceList = new ArrayList<File>();

                if (xmlDocument != null) {
                    Element root = xmlDocument.getRootElement();

                    if (root.getName().equals("ProblemSet")) {

                        // Read content date for problem_set.xml from SVN.
                        Date contentDate = SvnUtil.getContentDate(svnUser, svnPassword, inputFile);

                        // Initialize output directory now that we have content date.
                        initializeOutputDirs(contentDate);

                        String psName = root.getAttributeValue("name");

                        // Now that we have the problemSet name, add it to the path.
                        commonPath = commonPath + "/" + psName;

                        // Write conversion meta-data to a file.
                        File theConversionFile = writeConversionDetails(root, contentDate);

                        StringBuffer htmlOutput = new StringBuffer();
                        htmlOutput.append("<!DOCTYPE html>" + SINGLE_NEW_LINE);
                        htmlOutput.append("<html lang=\"en\">" + SINGLE_NEW_LINE);
                        htmlOutput.append("<head>" + SINGLE_NEW_LINE);
                        htmlOutput.append("<title>");
                        htmlOutput.append(psName);
                        htmlOutput.append("</title>" + SINGLE_NEW_LINE);
                        htmlOutput.append("<link href=\"" + CSS_FILE_URL + "\" ");
                        htmlOutput.append(" rel=\"stylesheet\">" + SINGLE_NEW_LINE);
                        htmlOutput.append("</head>" + SINGLE_NEW_LINE);

                        htmlOutput.append("<body id=\"top-anchor\">");
                        htmlOutput.append(DOUBLE_NEW_LINES);
                        htmlOutput.append("<div id=\"content\">");
                        htmlOutput.append(SINGLE_NEW_LINE);
                        htmlOutput.append("<h1>");
                        htmlOutput.append("TutorShop Problem Set ");
                        htmlOutput.append(psName);
                        htmlOutput.append("</h1>" + DOUBLE_NEW_LINES);

                        addMetaData(htmlOutput, root, contentDate);

                        addProblemList(htmlOutput, root);
                        addProblems(htmlOutput, root, resourceList, theConversionFile);

                        htmlOutput.append("</body>" + SINGLE_NEW_LINE);
                        htmlOutput.append("</html>" + SINGLE_NEW_LINE);

                        // Write output file.
                        writeOutputFile(psName, htmlOutput, resourceList);

                    } else {
                        logger.warn("Unable to find expected ProblemSet root element. "
                                    + "Invalid TutorShop XML file: " + fileName);
                    }
                } else {
                    logger.warn("Invalid XML in file: " + fileName);
                }
            } else {
                logger.warn("Input file not found: " + fileName);
            }

            logger.info("\nDone.");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            logger.error("Unknown error.", throwable);
        }
    }

    /**
     * Append anchor for the top of the page.
     * @param htmlOutput StringBuffer with HTML output
     * @param root the root element in problem_set.xml
     * @param contentDate the content date, not found in xml file
     */
    private void addMetaData(StringBuffer htmlOutput, Element root, Date contentDate) {
        String psName = root.getAttributeValue("name");
        String psDescription = root.getAttributeValue("description");

        htmlOutput.append("<table class=\"pc-meta-data\">" + SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONVERSION_TOOL_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append(CONVERSION_TOOL_NAME);
        htmlOutput.append("</td></tr>");
        htmlOutput.append(SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONVERSION_DATE_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append(DATE_FMT.format(new Date()));
        htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONTENT_VERSION_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append(psName);
        htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONTENT_DATE_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append((contentDate == null) ? " " : DATE_FMT.format(contentDate));
        htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONTENT_DESCRIPTION_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append(psDescription);
        htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);
        htmlOutput.append("<tr><th>");
        htmlOutput.append(PopulatePcTables.CONTENT_SKILLS_LABEL);
        htmlOutput.append("</th><td>");
        htmlOutput.append(getSkills(root));
        htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);
        htmlOutput.append("</table>" + DOUBLE_NEW_LINES);
    }

    /**
     * Get list of skills for this Problem Set.
     * @param root the root element in problem_set.xml
     * @return String list of skills
     */
    private String getSkills(Element root) {

        StringBuffer result = new StringBuffer();

        Element skillsEle = root.getChild("Skills");
        if (skillsEle == null) { return ""; }

        List<Element> skills = skillsEle.getChildren("Skill");
        for (Element s : skills) {
            if (result.length() > 0) { result.append(", "); }
            String skillName = s.getAttributeValue("name");
            result.append(skillName);
        }

        return result.toString();
    }

    /**
     * Append list of problems.
     * @param htmlOutput StringBuffer with HTML output
     * @param root the root element in problem_set.xml
     */
    private void addProblemList(StringBuffer htmlOutput, Element root) {

        htmlOutput.append("<p class=\"problems-heading\">Problems in this problem set</p>");
        htmlOutput.append(SINGLE_NEW_LINE);
        htmlOutput.append("<ul class=\"pc-contents\">" + SINGLE_NEW_LINE);

        Element problemsEle = root.getChild("Problems");
        List<Element> problems = problemsEle.getChildren("Problem");
        for (Element p : problems) {
            String problemName = p.getAttributeValue("name");
            htmlOutput.append("<li class=\"pc-problem\"><a href=\"#");
            htmlOutput.append(problemName);
            htmlOutput.append("\">");
            htmlOutput.append(problemName);
            htmlOutput.append("</a></li>" + SINGLE_NEW_LINE);
        }

        htmlOutput.append("</ul>" + DOUBLE_NEW_LINES);
    }

    /**
     * Append problems.
     * @param htmlOutput StringBuffer with HTML output
     * @param root the root element in problem_set.xml
     * @param resourceList list of resource files
     * @param theConversionFile the pc_conversion file
     * @throws Exception if we fail to parse BRD file or write conversion details
     */
    private void addProblems(StringBuffer htmlOutput, Element root, List<File> resourceList,
                             File theConversionFile)
        throws Exception {

        String psName = root.getAttributeValue("name");

        File screenshotDir = new File(inputDir + "/" + "screenshots");

        Element problemsEle = root.getChild("Problems");
        List<Element> problems = problemsEle.getChildren("Problem");
        for (Element p : problems) {
            String problemName = p.getAttributeValue("name");

            htmlOutput.append("<div style=\"clear:left\"><hr></div>" + SINGLE_NEW_LINE);
            htmlOutput.append("<div class=\"problem\">" + SINGLE_NEW_LINE);
            htmlOutput.append("<a class=\"back-to-the-top\"");
            htmlOutput.append(" href=\"#top-anchor\">Back to the top</a>" + SINGLE_NEW_LINE);
            htmlOutput.append("<h2 class=\"problem-name\" id=\"");
            htmlOutput.append(problemName);
            htmlOutput.append("\">");
            htmlOutput.append(problemName);
            htmlOutput.append("</h2>" + DOUBLE_NEW_LINES);

            htmlOutput.append("<table class=\"problem-meta-data\">");
            htmlOutput.append(SINGLE_NEW_LINE);

            String description = p.getAttributeValue("description");
            htmlOutput.append("<tr><th>Problem Description</th><td>");
            htmlOutput.append(description);
            htmlOutput.append("</td></tr>" + SINGLE_NEW_LINE);

            // Skills
            addSkillInfo(htmlOutput, p);

            // Close-out meta-data table.
            htmlOutput.append("</table>" + SINGLE_NEW_LINE);

            // Prompt
            String brdFileName = p.getAttributeValue("problem_file");
            String prompt = readPromptFromBRD(brdFileName);
            if (prompt != null) {
                htmlOutput.append("<div class=\"problem-prompt\">");
                htmlOutput.append(prompt);
                htmlOutput.append("</div>" + SINGLE_NEW_LINE);
            }

            // Screenshot
            if (screenshotDir.exists()) {
                if ((brdFileName == null) || (brdFileName.length() == 0))  { brdFileName = problemName; }
                File screenshotFile = getScreenshot(screenshotDir, brdFileName);
                if (screenshotFile != null) {
                    resourceList.add(screenshotFile);
                    htmlOutput.append("<figure>" + SINGLE_NEW_LINE);
                    htmlOutput.append("<img class=\"problem-screenshot\" ");
                    htmlOutput.append("src=\"" + RESOURCES_PATH+ "/resources/");
                    htmlOutput.append(screenshotFile.getName());
                    htmlOutput.append("\">" + SINGLE_NEW_LINE);
                    htmlOutput.append("<figcaption>Screenshot of problem \"");
                    htmlOutput.append(problemName);
                    htmlOutput.append("\" on TutorShop, taken on ");
                    htmlOutput.append(DATE_FMT.format(screenshotFile.lastModified()));
                    htmlOutput.append("</figcaption>" + SINGLE_NEW_LINE);
                    htmlOutput.append("</figure>" + SINGLE_NEW_LINE);
                }
            }

            htmlOutput.append("</div>" + SINGLE_NEW_LINE);

            addToConversionDetails(problemName, psName, theConversionFile);
        }

        if (!screenshotDir.exists()) {
            logger.warn("screenshots directory doesn't exist in: " + inputDir);
        }

        htmlOutput.append(SINGLE_NEW_LINE);
    }

    /**
     * Append skill info for the specified problem.
     * @param htmlOutput StringBuffer with HTML output
     * @param problem a problem element in problem_set.xml
     */
    private void addSkillInfo(StringBuffer htmlOutput, Element problem) {

        Element skillsEle = problem.getChild("Skills");

        if (skillsEle != null) {
            htmlOutput.append("<tr><th>Skills (# occurrences)</th>" + SINGLE_NEW_LINE);
            htmlOutput.append("<td><ul class=\"problem-skills\">" + SINGLE_NEW_LINE);
            List<Element> skills = skillsEle.getChildren("Skill");
            for (Element s : skills) {
                htmlOutput.append("<li>");
                htmlOutput.append(s.getAttributeValue("name"));
                htmlOutput.append(" (");
                htmlOutput.append(s.getAttributeValue("occurrences"));
                htmlOutput.append(")");
                htmlOutput.append("</li>" + SINGLE_NEW_LINE);
            }
            htmlOutput.append("</ul></td></tr>" + SINGLE_NEW_LINE);
        }
    }

    /**
     * Get screenshot file.
     * @param screenshotDir directory holding screenshots
     * @param brdFileName name of BRD for problem
     * @return if found, the screenshot file, null otherwise
     * @throws IOException if we fail to parse BRD file
     */
    private File getScreenshot(File screenshotDir, String brdFileName)
        throws IOException {

        File screenshotFile = null;

        // Use BRD naming scheme for screenshots...
        String fileName = brdFileName.substring(0, brdFileName.lastIndexOf('.'));
        screenshotFile = new File(screenshotDir.getPath() + "/" + fileName + ".png");
        if (!screenshotFile.exists()) {
            screenshotFile = null;
            logger.warn("Screenshot file doesn't exist for problem name: " + fileName);
        }

        return screenshotFile;
    }

    /**
     * Find the named BRD file and parse contents for the problem prompt.
     * @param brdFileName name of BRD for problem
     * @return String the prompt, null if BRD not found
     * @throws IOException if we fail to parse BRD file
     */
    private String readPromptFromBRD(String brdFileName)
        throws IOException {

        StringBuffer result = new StringBuffer();

        File brdDir = new File(inputDir + "/" + "FinalBRDs");
        if (brdDir.exists()) {
            File brdFile = new File(brdDir.getPath() + "/" + brdFileName);
            if (brdFile.exists()) {
                Document xmlDocument = readInputFile(brdFile);
                if (xmlDocument != null) {
                    Element root = xmlDocument.getRootElement();

                    if (root.getName().equals("stateGraph")) {
                        Element startNodeMsgsEle = root.getChild("startNodeMessages");
                        List<Element> messages = startNodeMsgsEle.getChildren("message");
                        for (Element m : messages) {
                            Element properties = m.getChild("properties");
                            if (properties == null) { continue; }
                            Element selection = properties.getChild("Selection");
                            if (selection == null) { continue; }
                            Element value = selection.getChild("value");
                            if (value == null) { continue; }
                            String theValue = value.getText();
                            if (selectionValueKeywords.contains(theValue)) {
                                Element input = properties.getChild("Input");
                                if (input == null) { continue; }
                                value = input.getChild("value");
                                if (value == null) { continue; }
                                if (result.length() > 0) {
                                    result.append(" ");
                                }
                                result.append(value.getText());
                            }
                        }
                    } else {
                        logger.warn("Unable to find expected stateGraph root element. "
                                    + "Invalid TutorShop XML file: " + brdFileName);
                    }
                } else {
                    logger.warn("Invalid XML in file " + brdFileName);
                }
            } else {
                logger.warn("BRD file: " + brdFile.getCanonicalPath() + " NOT found.");
            }
        } else {
            logger.warn("FinalBRDs directory doesn't exist in: " + inputDir);
        }

        if (result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     * Write the output file given the HTML
     * @param psName name of the problem set, writing one per file
     * @param htmlOutput the StringBuffer with the HTML contents
     * @param resourceList list of resource files
     * @throws Exception failure to create output dir or write file
     */
    private void writeOutputFile(String psName, StringBuffer htmlOutput, List<File> resourceList)
        throws Exception {

        // Output directory based on problem set name.
        outputDir = outputDir + "/" + psName;
        File theOutputDir = new File(outputDir);
        if (!theOutputDir.exists() && !theOutputDir.mkdirs()) {
            logger.error("Unable to find/create output directory: " + outputDir);
            throw new Exception("Unable to find/create output directory: " + outputDir);
        }

        String outputFileName = outputDir + "/" + psName + ".html";
        logger.info("Writing file " + outputFileName);

        File outputFile = new File(outputFileName);

        FileWriter fw = null;
        BufferedWriter out = null;
        try {
            fw = new FileWriter(outputFile);
            out = new BufferedWriter(fw);
            out.write(htmlOutput.toString());
        } catch (IOException exception) {
            logger.error("writeOutputFile: IOException.", exception);
            throw exception;
        } finally {
            try {
                if (out != null) { out.close(); }
            } catch (IOException exception) {
                logger.error("writeOutputFile: IOException in finally block.", exception);
            }
        }

        // Write resources dir to outputDir.
        String resourcesDirName = outputDir + "/resources";
        File resourcesDir = new File(resourcesDirName);

        if (resourcesDir.exists() || resourcesDir.mkdirs()) {
            for (File f : resourceList) {
                String fileName = f.getName();
                FileUtils.copyFile(f, new File(resourcesDirName + "/" + fileName));
            }
        }
    }

    /**
     * Write problem conversion meta-data to a file.
     * @param root the root element in problem_set.xml
     * @param contentDate the content date, not found in xml file
     * @return File the file created
     * @throws Exception failure to create output dir or write file
     */
    private File writeConversionDetails(Element root, Date contentDate)
        throws Exception {

        String psName = root.getAttributeValue("name");
        String psDescription = root.getAttributeValue("description");

        Date conversionDate = new Date();

        // Generate comma-separated string describing problem conversion.
        StringBuffer metaData = new StringBuffer();
        metaData.append(PopulatePcTables.CONVERSION_TOOL_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(CONVERSION_TOOL_NAME);
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.TOOL_VERSION_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(getToolVersion());
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.DATASHOP_VERSION_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(VersionInformation.getReleaseNumber());
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.CONVERSION_DATE_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(DATE_FMT.format(conversionDate));
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.CONTENT_VERSION_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(psName);   // content_version is ProblemSet name
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.CONTENT_DATE_LABEL);
        metaData.append(SEPARATOR);
        metaData.append((contentDate == null) ? " " : DATE_FMT.format(contentDate));
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.CONTENT_DESCRIPTION_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(psDescription);
        metaData.append(SINGLE_NEW_LINE);
        metaData.append(PopulatePcTables.CONTENT_PATH_LABEL);
        metaData.append(SEPARATOR);
        metaData.append(commonPath);
        metaData.append(SINGLE_NEW_LINE);

        String dateStr = FastDateFormat.getInstance("yyyy_MMdd_HHmmss").format(conversionDate);

        // Conversion File: 'pc_conversion_<problem set name>_<date>.txt'
        String conversionFileName =
            conversionDir + "/" + PC_CONVERSION_STR + "_" + psName + "_" + dateStr + ".txt";
        File conversionFile = new File(conversionFileName);

        FileWriter fw = null;
        BufferedWriter out = null;
        try {
            fw = new FileWriter(conversionFile);
            out = new BufferedWriter(fw);
            out.write(metaData.toString());
        } catch (IOException exception) {
            logger.error("writeConversionDetails: IOException.", exception);
            throw exception;
        } finally {
            try {
                if (out != null) { out.close(); }
            } catch (IOException exception) {
                logger.error("writeConversionDetails: IOException in finally block.", exception);
            }
        }

        return conversionFile;
    }

    /**
     * Get the revision number for this tool.
     * @return the number
     */
    private Long getToolVersion() {
        String canonicalName = getClass().getCanonicalName();
        canonicalName = canonicalName.replaceAll("\\.", "/");
        String toolFileName = baseDir + "/" + canonicalName + ".java";
        File toolFile = new File(toolFileName);
        if (toolFile.exists()) {
            return SvnUtil.getRevision(toolFile);
        } else {
            logger.debug("*** File not found");
            return null;
        }
    }

    /**
     * Add problem to conversion file.
     * @param problemName the name of converted problem
     * @param psName name of the problem set
     * @param theConversionFile the pc_conversion file
     * @throws Exception failure to create output dir or write file
     */
    private void addToConversionDetails(String problemName, String psName, File theConversionFile)
        throws Exception {

        String problemSetFileName = psName + ".html";

        StringBuffer sb = new StringBuffer();
        sb.append(PopulatePcTables.CONTENT_PROBLEM_LABEL);
        sb.append(SEPARATOR);
        sb.append(problemName);
        sb.append(SEPARATOR);
        sb.append(problemSetFileName);
        sb.append(SINGLE_NEW_LINE);

        FileWriter fw = null;
        BufferedWriter out = null;
        try {
            fw = new FileWriter(theConversionFile, true);
            out = new BufferedWriter(fw);
            out.write(sb.toString());
        } catch (IOException exception) {
            logger.error("addToConversionDetails: IOException.", exception);
            throw exception;
        } finally {
            try {
                if (out != null) { out.close(); }
            } catch (IOException exception) {
                logger.error("addToConversionDetails: IOException in finally block.", exception);
            }
        }
    }

    /**
     * Parse command line arguments.
     * @param args Command line arguments
     * @throws IOException failure to create output directory
     */
    private void handleArgs(String[] args)
        throws IOException {

        if (args == null || args.length == 0) {
            displayUsage();
            System.exit(0);
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {
                displayUsage();
                System.exit(0);
            } else if (args[i].equals("-i")) {
                if (++i < args.length) {
                    inputDir = args[i];
                } else {
                    logger.error("Error: a directory name must be specified with the -i argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-s")) {
                if (++i < args.length) {
                    processSelectionKeywords(args[i]);
                } else {
                    logger.error("Error: a selection keyword must be specified "
                                 + "with the -s argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-o")) {
                if (++i < args.length) {
                    outputDir = args[i];
                } else {
                    logger.error("Error: a directory name must be specified with the -o argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-u")) {
                if (++i < args.length) {
                    svnUser = args[i];
                } else {
                    logger.error("Error: a username must be specified with the -u argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-p")) {
                if (++i < args.length) {
                    svnPassword = args[i];
                } else {
                    logger.error("Error: a password must be specified with the -p argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (args[i].equals("-b")) {
                if (++i < args.length) {
                    baseDir = args[i];
                } else {
                    logger.error("Error: a directory name must be specified with the -b argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if ((args[i].equals("-e")) || (args[i].equals("-email"))) {
                setSendEmailFlag(true);
                if (++i < args.length) {
                    setEmailAddress(args[i]);
                } else {
                    logger.error("Error: a email address must be specified with the -e argument");
                    displayUsage();
                    System.exit(1);
                }
            }
        }

        if ((inputDir == null) || (selectionValueKeywords == null)) {
            displayUsage();
            System.exit(0);
        }
    }

    /**
     * Display usage for this converter.
     */
    private void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
                           + " TutorShopConverter -i <inputDir> -s <selectionValueKeyword>");
        System.err.println("where <selectionValueKeyword> can be one or more keywords, "
                           + " separated by a comma.");
        System.err.println("Optional args:");
        System.err.println("\t-h\t usage info... this message");
        System.err.println("\t-u\t SVN user name");
        System.err.println("\t-p\t SVN password");
        System.err.println("\t-e\t email: user to notify in case of failure");
        System.err.println("\t-o\t output directory");
    }

    /**
     * Helper method for initializing output directory structure.
     * Results are written to
     * <outputDir>/OUTPUT_DIRECTORY_STR/PARENT_DIR/<inputDir>_<date>/<problemSetName>
     * where <inputDir> is the directory specifying the tutor,
     * i.e., "UBC_PreTest3" or "TutorShop".
     * @param contentDate the date of the problem content
     *
     * @throws IOException failure to create output directory
     */
    private void initializeOutputDirs(Date contentDate)
        throws IOException {

        // If unable to determine contentDate from SVN, use today.
        if (contentDate == null) { contentDate = new Date(); }

        if (outputDir == null) {
            outputDir = OUTPUT_DIRECTORY_STR + "/" + PARENT_DIR;
            logger.warn("Output directory not specified. Result written to current directory.");
        } else {
            outputDir = outputDir + "/" + OUTPUT_DIRECTORY_STR + "/" + PARENT_DIR;
        }
        conversionDir = outputDir + "/" + PC_CONVERSION_STR;

        // Append tutor and date info to output directory.
        int lastIndex = inputDir.lastIndexOf("/");
        if (lastIndex == -1) { lastIndex = inputDir.lastIndexOf("\\"); }
        String tutorName = inputDir.substring(lastIndex + 1);
        if (StringUtils.isNumeric(tutorName)) { tutorName = "TutorShop"; }
        String dateStr = FastDateFormat.getInstance("yyyy_MMdd").format(contentDate);
        outputDir = outputDir + "/" + tutorName + "_" + dateStr;

        // Make note of common portion of pathname.
        commonPath = OUTPUT_DIRECTORY_STR + "/" + PARENT_DIR + "/" + tutorName + "_" + dateStr;

        File theOutputDir = new File(outputDir);
        if (!theOutputDir.exists() && !theOutputDir.mkdirs()) {
            logger.error("Unable to find/create output directory: " + outputDir);
            System.exit(1);
        }
        File theConversionDir = new File(conversionDir);
        if (!theConversionDir.exists() && !theConversionDir.mkdirs()) {
            logger.error("Unable to find/create conversion directory: " + conversionDir);
            System.exit(1);
        }
    }

    /**
     * Parse the input string for the selection keywords.
     * @param keywords the list of keywords, as a String
     */
    private void processSelectionKeywords(String keywords) {
        selectionValueKeywords = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(keywords, ",");
        if (!st.hasMoreTokens()) {
            selectionValueKeywords.add(keywords);
            return;
        }

        while (st.hasMoreTokens()) {
            selectionValueKeywords.add(st.nextToken());
        }

        return;
    }

    /**
     * Run the Info Field Converter.
     * @param args command line arguments
     */
    public static void main(String[] args) {

        Logger logger = Logger.getLogger("TutorShopConverter.main");

        String version = VersionInformation.getReleaseString();
        logger.info("TutorShopConverter starting (" + version + ")...");
        try {
            TutorShopConverter tsc = ExtractorFactory.DEFAULT.getTutorShopConverter();
            tsc.handleArgs(args);
            tsc.convert();
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("TutorShopConverter done.");
        }
    }

} // end class
