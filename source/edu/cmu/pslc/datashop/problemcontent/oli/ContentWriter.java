/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.problemcontent.oli;

import edu.cmu.pslc.datashop.problemcontent.Constants;

import edu.cmu.pslc.datashop.problemcontent.SvnUtil;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.OUTPUT_DIRECTORY_STR;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.PC_CONVERSION_STR;
import static edu.cmu.pslc.datashop.problemcontent.PopulatePcTables.DATE_FMT_STR;
import static edu.cmu.pslc.datashop.item.PcConversionItem.OLI_CONVERTER;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Comment;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * Transforms and writes the problem content as HTML to a specified location.
 * @author mkomisin
 *
 */
public class ContentWriter {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    /** Encoding used for the XML data. */

    /** The output directory that will contain output files, e.g. C:/temp . */
    private static String outputDirectoryName = null;
    /** The course name for this course. */
    private String courseName = null;
    /** The course version for this course. */
    private String courseVersion = null;
    /** The content date  for this course. */
    private String contentDate = null;
    /** Base source directory for this tool. */
    private String baseDir;
    /** Whether to append to a file. */
    private static final boolean IS_APPEND = true;

    /** The DocType. */
    public static final String PROBLEM_CONTENT_DOCTYPE = "<!DOCTYPE html>";
    /** The max problem name length allowed (255). */
    public static final Integer MAX_PROBLEM_NAME_LENGTH = 255;
    /** The html element. */
    public static final String PROBLEM_CONTENT_HTML_ELEMENT =
        "<html xmlns:m=\"http://www.w3.org/1998/Math/MathML\" lang=\"en\">";

    /** Youtube video link prefix. */
    public static final String YOUTUBE_VIDEO_LINK =
        "http://www.youtube.com/watch?v=";

    /** The XSD cap. */
    public static final String PROBLEM_CONTENT_END = "</div></body></html>";

    /** The base directory for the generated content package,
     * e.g. C:/output/a_and_p_v_1_2-prod-2013-04-11 . */
    private String conversionOutputPath = null;

    /**
     * Constructor.
     * @param conversionOutputPath the base directory for the generated content,
     *     e.g. c:\temp\<course_name>_<course_version>\
     * @param outputDirectoryName the output directory that will contain output files, e.g. C:\temp
     * @param courseName the course name
     * @param courseVersion the course version (branch or tag)
     * @param contentDate the content date string
     * @param baseDir the base directory for this tool's files
     */
    public ContentWriter(String conversionOutputPath, String outputDirectoryName,
                         String courseName, String courseVersion, String contentDate,
                         String baseDir) {
        this.conversionOutputPath = conversionOutputPath.replaceAll("\\\\", "/");
        this.outputDirectoryName = outputDirectoryName.replaceAll("\\\\", "/");
        this.courseName = courseName;
        this.courseVersion = courseVersion;
        this.contentDate = contentDate;
        this.baseDir = baseDir;
    }


    /**
     * Writes the given string(s) in tab-delimited form to the meta data file.
     * @param metaDataFilePath the path to the meta data file for a given organization
     * @param values the string(s)
     * @return true if successful
     */
    private boolean writeFileMetaData(String metaDataFilePath, String... values) {
        File outputFile = new File(metaDataFilePath);
        FileOutputStream oStream = null;
        OutputStreamWriter out = null;
        try {
            oStream = new FileOutputStream(outputFile, IS_APPEND);
            out = new OutputStreamWriter(oStream, Constants.UTF8);

            int count = 0;
            for (String value : values) {
                if (count < values.length - 1) {
                    out.write(value + "\t");
                } else {
                    out.write(value);
                }
                count++;
            }
            out.write("\n");

            if (out != null) {
                out.close();
            }
            if (oStream != null) {
                oStream.close();
            }
        } catch (FileNotFoundException e) {
            logger.error("File " + metaDataFilePath + " not found. Skipping write.");
            return false;
        } catch (IOException e) {
            logger.error("IOException on " + metaDataFilePath + ". Skipping write.");
            return false;
        }

        return true;
    }

    /**
     * Writes the HTML to the proper file.
     * @param probElem the problem element
     * @param hierarchyPath the hierarchy path
     * @param file the original OLI XML file
     * @param organization the organization object
     * @return the number of problems added (always 1)
     * @throws IOException the IO exception
     */
    public int writeOut(Element probElem, String hierarchyPath, File file,
            Organization organization)
            throws IOException {
        logger.debug("Converting file " + file.getAbsolutePath());
        int totalProblems = 0;


        // This code is commented out, but it is useful. It copies the original
        // xml course content file to a subdirectory along side the generated HTML.
       // File brettoFile = new File(hierarchyPath + "/" + file.getName());
       // CommonXml.createDirectory(hierarchyPath);
       // if (brettoFile.length() == 0) {
       //     FileUtils.copyFile(file, brettoFile);
       // }

        Integer lastIndex = hierarchyPath.lastIndexOf("/");

        CommonXml.createDirectory(hierarchyPath.substring(0, lastIndex));
        logger.debug("Appending HTML output to " + hierarchyPath + ".html");
        File outputFile = new File(
                hierarchyPath + ".html");
        FileOutputStream oStream = new FileOutputStream(
                outputFile, IS_APPEND);
        OutputStreamWriter out = new OutputStreamWriter(
            oStream, Constants.UTF8);

        // Create the HTML output document
        Document xmlOutput = new Document(
                new Element(
                        "dummy_root"));
        // XMLOutputter format
        // The default encoding is UTF-8.
        Format format = Format.getPrettyFormat();
        format.setExpandEmptyElements(false);
        Element rootElement = xmlOutput
                .getRootElement();

        // Omit declaration when writing because
        // the code manually handles the XML declarations.
        format.setOmitDeclaration(true);

        rootElement.addContent(probElem);

        String problemId = probElem.getChildText("name");
        if (problemId.length() > MAX_PROBLEM_NAME_LENGTH) {
            problemId = problemId.substring(0, MAX_PROBLEM_NAME_LENGTH);
        }
        // Create pc_conversion directory for the pc_conversion meta files
        String orgDir = getTagDir()
                + "/"
                + getOrgForPath(organization)
                + "/";
        String metaDataDir = outputDirectoryName + "/" + PC_CONVERSION_STR;
        CommonXml.createDirectory(metaDataDir);

        // Write (append) the header info to the metadata file
        String metaDataFilePath = metaDataDir
                + "/" + PC_CONVERSION_STR + "_" + courseName + "_"
                + courseVersion + "_" + getOrgForPath(organization) + ".txt";
        File metaDataFile = new File(metaDataFilePath);
        if (metaDataFile.length() <= 0) {
            String metaDataHeader = getHeaderContents(probElem, hierarchyPath, organization, false);
            logger.debug("Adding conversion meta data for " + organization.getId()
                + " to the " + PC_CONVERSION_STR + " directory.");

                writeFileMetaData(metaDataFilePath, metaDataHeader);
        }

        logger.debug("Appending problem content to " + metaDataFilePath);
        // Get the path to the problem content
        String problemFilePath = (hierarchyPath
                .replaceAll(getTagDir(), "")
                .replace(getOrgForPath(organization), "")).substring(2) + ".html";
        // Add the problem and path to the metadata file.
        writeFileMetaData(metaDataFilePath, "Problem", problemId, problemFilePath);


        totalProblems += 1;

        // Now, generate HTML from the pre-formatted problems and add them to
        // the appropriate html file, outputFile.
        if (outputFile.length() <= 0) {
            // Write the HTML header only if the file is new.
            out.write(PROBLEM_CONTENT_DOCTYPE);
            out.write("\n");
            out.write(PROBLEM_CONTENT_HTML_ELEMENT);
            out.write("\n");
            out.write(getHeaderContents(probElem, hierarchyPath, organization, true));
        }

        // Append the XML output to the file.
        List<Element> problemChilds = rootElement.getChildren();
        for (Iterator<Element> iterator = problemChilds.iterator();
                iterator.hasNext();) {
            Element problem = iterator.next();
            iterator.remove();
            Element htmlElement = problem2Html(organization, problem);

            if (htmlElement != null) {
                // Create a temporary file so that the XML formatting does
                // automatically close the open tags in the actual output file.
                File tempXmlFile = File.createTempFile("oli-", "-xml");
                FileOutputStream tempOutStream = new FileOutputStream(
                        tempXmlFile, IS_APPEND);
                OutputStreamWriter tempOutWriter = new OutputStreamWriter(
                        tempOutStream, Constants.UTF8);

                new XMLOutputter(format).output(htmlElement, out);

                tempOutWriter.close();
                tempOutStream.close();

                // Write the text to the actual output file.
                FileInputStream tempInputStream = new FileInputStream(tempXmlFile);
                byte[] bytes = new byte[tempInputStream.available()];
                tempInputStream.read(bytes);
                String text = new String(bytes);
                out.write(text);
            }

        }
        out.close();
        oStream.close();

        return totalProblems;
    }

    /**
     * Recursively renames elements, and adds attributes where necessary.
     * @param elem the element
     * @param oldTag the old tag
     * @param newTag the new tag
     * @param attributeList the attribute list
     * @return the modified element
     */
    public Element updateDescendents(Element elem, String oldTag, String newTag,
            Map<String, String> attributeList) {
        Iterator iter = elem.getChildren().iterator();
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            if (child.getName().equals(oldTag)) {
                child.setName(newTag);
                if (attributeList != null) {
                    // Add attributes
                    Iterator keys = attributeList.keySet().iterator();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (attributeList.get(key) != null) {
                            child.setAttribute(key, attributeList.get(key));
                        } else {
                            // Remove attribute if the value is null
                            child.removeAttribute(key);
                        }
                    }
                }

            } else {
                child = updateDescendents(child, oldTag, newTag, attributeList);
            }
        }
        return elem;
    }

    /**
     * Covnert question types to HTML.
     * @param question the question element
     * @return the HTML oli-question
     */
    private Element renameQuestionTypes(Element question) {
        // The body element for this question
        Element bodyElem = null;
        boolean foundBody = false;
        for (Iterator iterator = question.getChildren("body").iterator();
                iterator.hasNext();) {
            bodyElem = (Element) iterator.next();
            bodyElem.setName("div");
            bodyElem.setAttribute("class", "oli-body");

            // Recursively rename input_ref elements
            HashMap<String, String> attribs = new HashMap<String, String>();
            attribs.put("class", "oli-input-ref");
            bodyElem = updateDescendents(bodyElem, "input_ref", "div", attribs);

        }

        for (Iterator iterator = question.getDescendants(new ElementFilter());
            iterator.hasNext();) {
            Element element = (Element) iterator.next();
            // If the child matches a known OLI question type,
            // then convert it to a div
            for (String questionTypeString
                    : OliConverter.OLI_QUESTION_TYPES) {
                if (questionTypeString.equals(element.getName())) {


                    // Create the question input div
                    element.setName("div");
                    element.setAttribute("class", "oli-"
                            + questionTypeString.replaceAll("_", "-"));

                    // Added choices (for fill in the blanks and multiple choice)
                    Iterator choiceIter = element.getChildren("choice").iterator();
                    while (choiceIter.hasNext()) {
                        Element choiceElem = (Element) choiceIter.next();
                        choiceElem.setName("div");
                        choiceElem.setAttribute("class", "oli-choice");
                        if (choiceElem.getAttributeValue("value") != null
                                && !choiceElem.getAttributeValue("value").equals("--")) {
                            choiceElem.addContent("  (value: "
                                + choiceElem.getAttributeValue("value") + ")");
                        }
                    }

                }
            }
        }
        // If the question did not contain a multiple choice, then add the body back
        // to the question element.
        if (bodyElem != null && !foundBody) {
            bodyElem.detach();
            question.addContent(0, bodyElem);
        }

        return question;
    }

    /**
     * Covnert part elements to HTML.
     * @param part the part element
     * @return the HTML oli-part
     */
    private Element renamePart(Element part) {
        for (Iterator iterator = part.getChildren().iterator();
            iterator.hasNext();) {
            Element element = (Element) iterator.next();

            // Convert part to div
            if (element.getName().equals("part")) {
                element.setName("div");
                element.setAttribute("class", "oli-part");
            }

            Iterator explanationIter = element.getChildren("explanation").iterator();
            while (explanationIter.hasNext()) {
                Element explanationElem = (Element) explanationIter.next();
                explanationElem.setName("div");
                explanationElem.setAttribute("class", "oli-explanation");
            }
            Iterator responseIter = element.getChildren("response").iterator();
            while (responseIter.hasNext()) {
                Element responseElem = (Element) responseIter.next();
                responseElem.setName("div");
                responseElem.setAttribute("class", "oli-response");
                Iterator feedbackIter = responseElem.getChildren("feedback").iterator();
                while (feedbackIter.hasNext()) {
                    Element feedbackElem = (Element) feedbackIter.next();
                    feedbackElem.setName("div");
                    feedbackElem.setAttribute("class", "oli-feedback");
                }
            }

            Iterator noResponseIter = element.getChildren("no_response").iterator();
            while (noResponseIter.hasNext()) {
                Element responseElem = (Element) noResponseIter.next();
                responseElem.setName("div");
                responseElem.setAttribute("class", "oli-no-response");
                Iterator feedbackIter = responseElem.getChildren("feedback").iterator();
                while (feedbackIter.hasNext()) {
                    Element feedbackElem = (Element) feedbackIter.next();
                    feedbackElem.setName("div");
                    feedbackElem.setAttribute("class", "oli-feedback");
                }
            }

            Iterator responseMultIter = element.getChildren("response_mult").iterator();
            while (responseMultIter.hasNext()) {
                Element responseElem = (Element) responseMultIter.next();
                responseElem.setName("div");
                responseElem.setAttribute("class", "oli-response");
                Iterator feedbackIter = responseElem.getChildren("feedback").iterator();
                while (feedbackIter.hasNext()) {
                    Element feedbackElem = (Element) feedbackIter.next();
                    feedbackElem.setName("div");
                    feedbackElem.setAttribute("class", "oli-feedback");
                }

                Iterator matchIter = responseElem.getChildren("match").iterator();
                while (matchIter.hasNext()) {
                    Element matchElem = (Element) matchIter.next();
                    matchElem.setName("div");
                    matchElem.setAttribute("class", "oli-match");
                    if (matchElem.getAttributeValue("match") != null) {
                        String escapedString =
                            org.apache.commons.lang.StringEscapeUtils.escapeXml(
                                matchElem.getAttributeValue("match"));
                        matchElem.setText(escapedString);
                    }
                }
            }

            Iterator hintIter = element.getChildren("hint").iterator();
            while (hintIter.hasNext()) {
                Element hintElem = (Element) hintIter.next();
                hintElem.setName("div");
                hintElem.setAttribute("class", "oli-hint");
            }

        }
        return part;
    }


    /**
     * Creates an XML element using html tags
     * to be fed to the XMLOutputter.
     * @param organization the organization
     * @param problem the problem element
     * @return the html element
     */
    private Element problem2Html(Organization organization, Element problem) {
        String problemName = problem.getChildText("name");
        String workbookId = organization
                .getWorkbookFromElementId(problemName);

        if (workbookId == null) {
            workbookId = problemName;
        }

        problem.setName("div");
        problem.setAttribute("class", "problem");


        String problemDescription = problem.getChildText("description");
        String purpose = problem.getChildText("purpose");
        problem.removeChild("name");
        problem.removeChild("description");
        problem.removeChild("purpose");

        formatProblemDiv(problem, problemName, purpose, problemDescription,
            organization, workbookId);

        Element prompt = problem.getChild("prompt");
        prompt.setName("div");
        prompt.setAttribute("class", "problem-prompt");

        Iterator questionIterator = prompt.getChildren("question").iterator();
        while (questionIterator.hasNext()) {
            Element question = (Element) questionIterator.next();

            question.setName("div");
            question.setAttribute("class", "oli-question");
            String questionId = question.getAttributeValue("id");

            question = renameQuestionTypes(question);
            question = renamePart(question);

            Element pQuestion = new Element("p");
            pQuestion.setAttribute("class", "oli-question");
            pQuestion.setText(questionId);
            question.addContent(0, pQuestion);
        }
        // Transform the resources before resolving input_ref elements
        problem = transformResources(problem);
        // Replace input_ref elements with the appropriate question element
        problem = resolveInputReferences(problem);
        // Expand unwanted unary elements like td and div by adding a jdom.Comment to them
        problem = expandRequiredElements(problem);

        return problem;
    }

    /**
     * Formats the HTML header for a problem.
     * @param problem the problem Element
     * @param problemName the problem name
     * @param problemDescription the problem description
     * @param purpose the purpose or null if no purpose exists
     * @param organization the organization being converted
     * @param workbookId the workbook Id for the problem
     */
    private void formatProblemDiv(Element problem,
            String problemName, String problemDescription, String purpose,
            Organization organization, String workbookId) {

        // Add learning objectives for this problem if there are any
        List<String> learningObjectives =
                organization.getLearningObjectivesFromWorkbook(workbookId);

        Element aElem = new Element("a");
        aElem.setAttribute("class", "back-to-the-top");
        aElem.setAttribute("href", "#top-anchor");
        aElem.setText("Back to the top");

        Element h2Elem = new Element("h2");
        h2Elem.setAttribute("id", problemName);
        h2Elem.setAttribute("class", "problem-name");
        h2Elem.setText(problemName);

        Element tableElem = new Element("table");
        tableElem.setAttribute("class", "problem-meta-data");
        Element trElem = new Element("tr");
        Element thElem = new Element("th");
        thElem.setText("Problem Description");
        Element tdElem = new Element("td");
        tdElem.setText(problemDescription);
        trElem.addContent(0, thElem);
        trElem.addContent(1, tdElem);
        tableElem.addContent(0, trElem);

        // Learning objectives
        Element trElem2 = new Element("tr");
        Element thElem2 = new Element("th");

        // Since learning object elements may have the same title,
        // then we must only add unique titles to the stringbuffer
        StringBuffer learningObjectiveFlatList = new StringBuffer("");
        List<String> uniqueTitles = new ArrayList<String>();
        for (String objectiveId : learningObjectives) {
            String title = (String) organization.getLearningObjectiveTitle(objectiveId);
            if (!uniqueTitles.contains(title)) {
                uniqueTitles.add(title);
                learningObjectiveFlatList.append(
                    title + ", ");
            }
        }

        // Learning objectives
        logger.debug("Adding learning objectives ["
                + learningObjectiveFlatList.toString()
                    + "] to problem (" + problemName + ").");

        thElem2.setText("Learning Objectives (" + learningObjectives.size() + ")");
        Element tdElem2 = new Element("td");
        if (!learningObjectiveFlatList.toString().isEmpty()) {
            tdElem2.setText(learningObjectiveFlatList.toString());
        }
        trElem2.addContent(0, thElem2);
        trElem2.addContent(1, tdElem2);
        tableElem.addContent(1, trElem2);


        // Purpose
        Element trElem3 = new Element("tr");
        Element thElem3 = new Element("th");

        thElem3.setText("Purpose");
        Element tdElem3 = new Element("td");
        tdElem3.setText(problemDescription);
        trElem3.addContent(0, thElem3);
        trElem3.addContent(1, tdElem3);
        tableElem.addContent(2, trElem3);

        problem.addContent(0, aElem);
        problem.addContent(1, h2Elem);
        problem.addContent(2, tableElem);
    }

    /**
     * Transform resources into HTML.
     * @param elem the element
     * @return the modified element
     */
    public Element transformResources(Element elem) {
        Iterator iter = elem.getChildren().iterator();
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            String childName = child.getName();

            if (child.getAttribute("src") != null) {
                String srcValue = child.getAttributeValue("src")
                    .replaceAll("\\\\", "/").replaceAll("/+", "/").trim();
                if (srcValue.matches("\\.\\./.*")) {
                    // Add resources path prefix
                    srcValue = Constants.RESOURCES_PATH + "/"
                            + srcValue;
                    child.setAttribute("src", srcValue);
                }
            }

            if (childName.equals("flash")) {
               child = transformFlash(child);
            } else if (childName.equals("image")) {
                child.setName("img");
            } else if (childName.equals("link")) {
                child.setName("a");
            } else if (childName.equals("anchor")) {
                child.setName("p");
            } else if (childName.equals("term")) {
                child.setName("div");
            } else if (childName.equals("pronunciation")
                    || childName.equals("video")) {
                // Create a element linking to audio/video file
                String src = child.getAttributeValue("src");
                if (src != null) {
                    src = src.replaceAll("\\\\", "/").replaceAll("/+", "/").trim();
                }
                if (src != null) {
                    child.setName("a");
                    child.setAttribute("href", src);
                    child.removeAttribute("src");
                    child.setText(child.getText() + ", " + src);
                }
            } else if (childName.equals("youtube")) {
                // Create a element linking to youtube video
                String src = child.getAttributeValue("src")
                    .replaceAll("\\\\", "/").replaceAll("/+", "/").trim();
                if (src != null) {
                    src = YOUTUBE_VIDEO_LINK + src;
                    child.setName("a");
                    child.setAttribute("href", src);
                    child.removeAttribute("src");
                }
            } else if (childName.equals("codeblock")) {
                child = transformCodeblock(child);
            } else if (childName.equals("div")
                    && child.getAttributeValue("class") != null
                    && child.getAttributeValue("class").equals("oli-image-hotspot")) {

                child = transformImageHotspot(child);
            } else if (childName.equals("div")
                    && child.getAttributeValue("class") != null
                    && child.getAttributeValue("class").equals("oli-fill-in-the-blank")) {
                child = transformFillInTheBlank(child);
            } else {
                transformResources(child);

            }
        }
        return elem;
    }

    /**
     * Gets the header contents for each page.
     * @param probElem the problem element
     * @param hierarchyPath the hierarchy file path
     * @param organization the organization object
     * @param formatHtml whether or not to use HTML formatting
     * @return the header contents for each page.
     */
    private String getHeaderContents(Element probElem,
            String hierarchyPath, Organization organization,
            Boolean formatHtml) {
        String problemName = probElem
                .getChildText("name");
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
        FastDateFormat fdf = FastDateFormat.getInstance(DATE_FMT_STR);
        String dateConverted = fdf.format(new Date());

        if (hierarchy == null) {
            hierarchy = OliConverter.UNREFERENCED_CONTENT;
        }

        String hierarchyDescriptionFromOrg =
            organization.getHierarchyDescription(hierarchy) != null
            ? organization.getHierarchyDescription(hierarchy) : "-";

        String workbookTitle = organization.getWorkbookTitle(workbookId) != null
                ? organization.getWorkbookTitle(workbookId) : "-";
        // Name of this file
        String problemFilePath = hierarchyPath
            .replaceAll(getTagDir(), "") + ".html";
        // The Content Version field
        String toolVersion = courseName + ", " + courseVersion + ", " + organization.getId();

        String headerContents = new String();

        if (formatHtml) {
            headerContents += "<head>"
            + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />"
            + "<title>" + problemFilePath + "</title>"
            + "<link href=\"https://pslcdatashop.web.cmu.edu/pc/pc.css\" rel=\"stylesheet\" />"
            + "<link href=\"https://pslcdatashop.web.cmu.edu/pc/pc_oli.css\" rel=\"stylesheet\" />"
            + getMathJaxScriptTags()
            + "</head>"
            + "<body id=\"top-anchor\">"

            + "<div id=\"content\">"
            + "<h1>" + problemFilePath + "</h1>"

            + "<table class=\"pc-meta-data\">"
            + "<tr><th>Conversion Tool</th><td>"
                + OLI_CONVERTER + "</td></tr>"
            + "<tr><th>Conversion Date</th><td>" + dateConverted + "</td></tr>"
            + "<tr><th>Content Version</th><td>" + toolVersion + "</td></tr>"
            + "<tr><th>Content Date</th><td>" + contentDate + "</td></tr>"
            + "<tr><th>Content Description</th><td>"
                + hierarchyDescriptionFromOrg + "</td></tr>"
            + "<tr><th>Skills</th><td>-</td></tr>"
            + "<tr><th>Hierarchy</th><td>" + hierarchy + "</td></tr>"
            + "</table>"

            + "<p class=\"problems-heading\">Problems in this problem set</p>"
            + "<ul class=\"pc-contents\">";

            headerContents += "</ul>";
        }

        // Return an HTML formatted string or a plain-text string.
        if (formatHtml) {
            return headerContents;
        } else {
            StringBuffer sBuffer = new StringBuffer("");

            sBuffer.append("Conversion Tool\t" + OLI_CONVERTER);
            sBuffer.append("\nTool Version\t" + getToolRevision());
            sBuffer.append("\nDataShop Version\t" + VersionInformation.getReleaseNumber());
            sBuffer.append("\nConversion Date\t" + dateConverted);
            sBuffer.append("\nContent Version\t" + toolVersion);
            sBuffer.append("\nContent Date\t" + contentDate);
            sBuffer.append("\nContent Description\t" + hierarchyDescriptionFromOrg);
            sBuffer.append("\nPath\t" + getCourseVersionDir(organization) + "/");
            return sBuffer.toString();
        }
    }

    /**
     * Helper function to create MathJax script tags.
     *
     * @return String with tags
     */
    private String getMathJaxScriptTags() {

        StringBuffer sb = new StringBuffer();
        sb.append("<script type=\"text/x-mathjax-config\">");
        sb.append("    MathJax.Hub.Config({");
        sb.append("    tex2jax: {");
        sb.append("    inlineMath: [['$','$'], ['\\(','\\)']],");
        sb.append("    processEscapes: true");
        sb.append("    },");
        sb.append("    styles: {");
        sb.append("    \".MathJax_Display\": {");
        sb.append("    margin:       \"0em 0em !important\"");
        sb.append("    }");
        sb.append("    }");
        sb.append("    });");
        sb.append("</script>");
        sb.append("<script type=\"text/javascript\"");
        sb.append(" src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">");
        sb.append("</script>");

        return sb.toString();
    }

    /**
     * Get the revision number for this tool.
     * @return the number
     */
    private Long getToolRevision() {
        if (baseDir == null) { return null; }

        logger.info("getToolRevision for dir: " + baseDir);

        String canonicalName = getClass().getCanonicalName();
        canonicalName = canonicalName.replaceAll("\\.", "/");
        String toolFileName = baseDir + "/" + canonicalName + ".java";
        File toolFile = new File(toolFileName);
        if (toolFile.exists()) {
            // OLI Converter is actually several files; get revision of directory.
            File parentDir = toolFile.getParentFile();
            return SvnUtil.getRevision(parentDir);
        } else {
            logger.debug("File not found: " + toolFileName);
            return null;
        }
    }

    /**
     * Add image_hotspot or image_input children. Because of the multiplicity of
     * forms, the hotspotElement can be the same element as input. This method
     * handles the case that they are the same or the case that the input
     * element is a child of hotspotElement.
     * @param hotspotElement the image_hotspot
     * @param input the element containing the hotspot elements, whether
     * that is the image_hotspot, itself, or the image_input within an image_hotspot
     */
    private void addHotspotChoices(Element hotspotElement, Element input) {

        String mapId = input.getAttributeValue("id") + "_map";

        // Map the image_input id to the hotspots it contains
        Element map = new Element("map");
        map.setAttribute("name", mapId);
        // Modify the hotspot elements
        List<Element> inputChildren = input.getChildren();
        for (Iterator iter = inputChildren.iterator();
            iter.hasNext();) {
            Element hotspot = (Element) iter.next();
            if (hotspot.getName().equals("hotspot")) {
                hotspot.setName("area");
                String title = hotspot.getAttributeValue("value");
                hotspot.removeAttribute("value");
                hotspot.setAttribute("title", title);
                // Remove the hotspot element from the image_input
                iter.remove();
                map.addContent(hotspot);
            }
        }

        // Create an image within the hotspot div
        Element img = new Element("img");
        img.setAttribute("src", input.getAttributeValue("src")
            .replaceAll("\\\\", "/").replaceAll("/+", "/").trim());
        input.removeAttribute("src");

        if (input.getAttributeValue("width") != null) {
            img.setAttribute("width", input.getAttributeValue("width"));
        }
        if (input.getAttributeValue("height") != null) {
            img.setAttribute("height", input.getAttributeValue("height"));
        }
        img.setAttribute("usemap", "#" + mapId);

        hotspotElement.addContent(img);
        logger.debug("Image hotspot image added " + img.getAttributeValue("src"));

        // Add hotspot the transformed hotspot elements
        if (!map.getChildren().isEmpty()) {
            input.addContent(map);
        }
    }

    /**
     * Transforms a image_hotspot element into a readable form.
     * @param hotspotElement the image_hotspot element
     * @return the object element
     */
    private Element transformImageHotspot(Element hotspotElement) {
        // 1st case, image_hotspot contains the hotspot elements and a src attribute

        if (hotspotElement.getAttributeValue("src") != null) {
            logger.debug("Image hotspot type 1.");
            // image_hotspot contains the src and hotspot elements
            addHotspotChoices(hotspotElement, hotspotElement);
        } else {
            // image_hotspot contains one or more image_input elements
            List<Element> hotspotChildren =
                (List<Element>)hotspotElement.getChildren("image_input");
            if (!hotspotChildren.isEmpty()) {
                logger.debug("Image hotspot type 2.");
                // each image_input contains the src attribute and one or more hotspot elements
                for (Element input : hotspotChildren) {
                    if (input.getAttributeValue("src") != null) {
                        // Each image_input has an id
                        addHotspotChoices(hotspotElement, input);
                    }
                }
            } else {
                logger.warn("No reference to an image src in " + hotspotElement.getName()
                    + " element " + hotspotElement.getAttributeValue("id"));
            }
        }

        Element p = new Element("p");
        p.setAttribute("class", "oli-image-hotspot-note");
        Element span = new Element("span");
        span.setText("Content note:");
        p.addContent(span);
        p.setText(" Hover image to see hotspots");
        hotspotElement.addContent(p);

        return hotspotElement;
    }

    /**
     * Transforms a codeblock element into a textarea to retain line breaks.
     * @param codeElement the codeblock element
     * @return the object element
     */
    private Element transformCodeblock(Element codeElement) {
        if (!codeElement.getText().trim().isEmpty()) {
            codeElement.setName("textarea");
            codeElement.setAttribute("class", "oli-code-block");
            codeElement.setAttribute("readonly", "readonly");

            if (codeElement.getAttributeValue("syntax") != null) {
                String syntax = codeElement.getAttributeValue("syntax");
                codeElement.setText(codeElement.getText());
            }
        }

        return codeElement;
    }

    /**
     * Transforms a codeblock element into a textarea to retain line breaks.
     * @param questionElement the question element
     * @return the object element
     */
    private Element transformFillInTheBlank(Element questionElement) {

        questionElement.setName("select");
        Element defaultChoice = new Element("option");
        defaultChoice.setAttribute("value", "--");
        defaultChoice.setText("--");
        questionElement.addContent(0, defaultChoice);

        for (Element choice : (List<Element>)questionElement.getChildren("div")) {
            if (choice.getAttribute("class") != null
                    && choice.getAttribute("class").getValue().equals("oli-choice")) {
                choice.setName("option");
                choice.removeAttribute("class");
            }
        }

        return questionElement;
    }

    /**
     * Transforms a flash element into an object element.
     * @param flashElement the flash element
     * @return the object element
     */
    private Element transformFlash(Element flashElement) {
        flashElement.setName("object");
        flashElement.setAttribute("type", "application/x-shockwave-flash");
        flashElement.setAttribute("data", flashElement.getAttributeValue("src")
                .replaceAll("\\\\", "/").replaceAll("/+", "/").trim());

        String flashId = flashElement.getAttributeValue("id");
        if (flashId == null) {
            Integer max = Integer.MAX_VALUE - 1;
            Integer min = 1;
            Random rand = new Random();
            int randomNumber = rand.nextInt((max - min) + 1) + min;
            flashId = "missingFlashId" + randomNumber;
        }
        flashElement.setAttribute("id", flashId);

        Element movieParam = new Element("param");
        Element wmodeParam = new Element("param");
        if (flashElement.getAttributeValue("src") != null) {
            movieParam.setAttribute("name", "movie");
            movieParam.setAttribute("value", flashElement.getAttributeValue("src")
                    .replaceAll("\\\\", "/").replaceAll("/+", "/").trim());
            wmodeParam.setAttribute("value", "transparent");


            if (flashElement.getChild("params") != null) {
                boolean foundLoggingParam = false;
                Element params = flashElement.getChild("params");
                Iterator paramsIter = params.getChildren().iterator();
                while (paramsIter.hasNext()) {
                    Element param = (Element) paramsIter.next();
                    paramsIter.remove();
                    if (param.getName().equals("param")) {
                        if (param.getAttributeValue("name") != null
                                && param.getAttributeValue("name").equalsIgnoreCase("Logging")) {
                            foundLoggingParam = true;
                            param.setText("None");
                        }
                        flashElement.addContent(param);
                    }
                }
                if (!foundLoggingParam) {
                    Element loggingParam = new Element("param");
                    loggingParam.setAttribute("name", "Logging");
                    loggingParam.setText("None");
                    flashElement.addContent(loggingParam);
                }
            }

            flashElement.addContent(movieParam);
            flashElement.addContent(wmodeParam);
            flashElement.removeAttribute("src");
        }
        return flashElement;
    }

    /**
     * Fix divs that do not have a closing tag.
     * @param elem the element
     * @return the modified element
     */
    public Element expandRequiredElements(Element elem) {
        Iterator iter = elem.getChildren().iterator();
        while (iter.hasNext()) {
            Element child = (Element) iter.next();
            if (child.getChildren().isEmpty()) {
                if ((child.getName().equals("div")
                        || child.getName().equals("td"))
                    && child.getTextTrim().isEmpty()) {
                    // HTML requires divs have closing tags so empty
                    // divs must be given an element.
                    child.addContent(new Comment("This element is intentionally empty."));
                }
            } else {
                child = expandRequiredElements(child);
            }
        }
        return elem;
    }

    /**
     * Resolve input_ref elements to their OLI question type elements.
     * @param elem the element
     * @return the modified element
     */
    public Element resolveInputReferences(Element elem) {
        List<Element> questionDivs = new ArrayList<Element>();

        List<Element> allDivs = CommonXml.getElementsByTagName(elem, "div");
        for (Element div : allDivs) {
            if (div.getAttributeValue("class") != null
                && div.getAttributeValue("class").equals("oli-question")) {
                questionDivs.add(div);
            }
        }

        for (Iterator questionIter = questionDivs.iterator(); questionIter.hasNext();) {
            Element questionDiv = (Element) questionIter.next();
            List<Element> innerDivs = CommonXml.getElementsByTagName(questionDiv, "div");

            innerDivs.addAll(CommonXml.getElementsByTagName(questionDiv, "select"));
            List<Element> inputRefDivs = new ArrayList<Element>();
            List<Element> qTypeDivs = new ArrayList<Element>();

            for (Element div : innerDivs) {
                // Get input_ref elements from inner div
                if (div.getAttributeValue("class") != null
                        && div.getAttributeValue("class").equals("oli-input-ref")) {
                    inputRefDivs.add(div);
                } else {
                    // Get qType elements from inner div
                    for (String qType : OliConverter.OLI_CONVERTED_QUESTION_TYPES) {
                        if (div.getAttributeValue("class") != null
                                && div.getAttributeValue("class").equals(qType)) {
                            if (div.getAttributeValue("input") == null) {
                                qTypeDivs.add(div);
                            } else if (div.getAttributeValue("input") != null) {
                                inputRefDivs.add(div);
                            }
                        }
                    }
                }
            }

            // Now, replace input_ref elements with qType elements
            for (Element qTypeDiv : qTypeDivs) {
                boolean inputRefFound = false;
                for (Element inputDiv : inputRefDivs) {
                    if (inputDiv.getAttributeValue("input") != null
                        && qTypeDiv.getAttributeValue("id") != null
                        && inputDiv.getAttributeValue("input")
                            .equals(qTypeDiv.getAttributeValue("id"))) {

                        if (!qTypeDiv.isAncestor(inputDiv)) {
                            inputRefFound = true;
                            inputDiv.setAttribute("class", qTypeDiv.getAttributeValue("class"));
                            inputDiv.setAttribute("id", qTypeDiv.getAttributeValue("id"));
                            inputDiv.removeAttribute("input");
                            qTypeDiv.detach();

                        } else {
                            inputDiv.detach();
                        }
                    }
                }
            }
        }

     // Remove straggler oli-question divs (empty questions)
        // which were created  so that input_ref elements
        // knows which type to become if they don't contain a valid id reference
        for (Element qTypeDiv : questionDivs) {
            List<Element> children = qTypeDiv.getChildren();

            // The oli-question div only contains a single empty <p>
            if (children.size() == 1) {
                Element child = children.get(0);
                // The oli-question div only contains a single empty <p>
                if (child.getName().equals("p") && child.getChildren().isEmpty()) {
                    qTypeDiv.detach();
                }
            }
        }

        return elem;
    }

    /**
     * Sorts the html files after all closing HTML tags have been added.
     * We sort the problems after the HTML files have finished being
     * generated to lessen memory requirements when parsing large courses.
     * In this step, we also remove problems from the table of contents
     * that have no associated problem content because the activity
     * contains no OLI questions and, thus, has no associated problem
     * in DataShop. Such a problem is usually purpose = manystudentswonder.
     * @param outputDirectory the outputDirectory for the problem content
     */
    public void sortProblemsInHtmlFiles(String outputDirectory) {
        SAXBuilder builder = new SAXBuilder();
        // Setting reusable to false is my workaround for the JDK 1.7u45 bug
        // described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {
            logger.info("\nSorting and pruning generated HTML files in " + outputDirectory);
            List<File> htmlOutputFiles =
                OliConverter.getFilenameList(logger, outputDirectory, "html");
            for (int j = 0, m = htmlOutputFiles.size(); j < m; j++) {
                // Find all files recursively in the course package
                File file = (File) htmlOutputFiles.get(j);

                String filename = file.getName();
                String filePath = file.getAbsolutePath().replaceAll("\\\\", "/");
                File parentDir = new File(file.getParent());
                logger.debug("Sorting and pruning file: , " + file.getAbsolutePath() + " ("
                        + (j + 1) + "/" + m + ")");

                if (file.isFile()) {

                        // The file could be an xml or txt file with XML inside
                        Document xmlInput = builder.build(file);
                        if (xmlInput != null) {
                            Element root = xmlInput.getRootElement();
                            logger.debug("Root node: " + root.getName());

                            // Get the problem-prompt element and its children.
                            Element bodyElement =
                                (Element) XPath.selectSingleNode(xmlInput,
                                    "/html//body//div[@id='content']");
                            Element ulElement =
                                (Element) XPath.selectSingleNode(xmlInput,
                                    "/html//body//div[@id='content']//ul");
                            Map<String, Element> problemsById = new HashMap<String, Element>();
                            List<Element> problemNodes =
                                (List<Element>) XPath.selectNodes(xmlInput,
                                "/html//body//div[@id='content']//div[@class='problem']");

                            // For each problem div
                            for (Iterator iterProblem = problemNodes.iterator();
                                    iterProblem.hasNext();) {
                                // Keep only links to problems that are not empty, like
                                // manystudentswonder types that have no inline activities
                                // associated with any problems
                                Element problemDiv = (Element) iterProblem.next();
                                Element problemNameElement = problemDiv.getChild("h2");
                                if (problemNameElement != null) {
                                    // Store the problem div in a map, keyed on problem Id
                                    String problemName = problemNameElement
                                        .getAttributeValue("id");
                                    if (problemName != null) {
                                        // This is a valid problem
                                        problemsById.put(problemName, problemDiv);
                                    }
                                }
                            } // end for each problem div

                            int problemCount = 0;
                            // Add the sorted problems  back to the div
                            if (!problemsById.isEmpty()) {
                                SortedSet<String> keys = new TreeSet<String>(
                                        problemsById.keySet());
                                for (String key : keys) {
                                    // Add anchor links (sorted)
                                    Element anchorLi = new Element("li");
                                    anchorLi.setAttribute("class", "pc-problem");
                                    Element anchorA = new Element("a");
                                    anchorA.setAttribute("href", "#" + key);
                                    anchorA.setText(key);
                                    anchorLi.addContent(anchorA);
                                    ulElement.addContent(anchorLi);
                                    // Add problem divs (sorted)
                                    Element problemDiv = problemsById.get(key);
                                    problemDiv.detach();
                                    Element hrDiv = new Element("div");
                                    hrDiv.setAttribute("style", "clear:left");
                                    hrDiv.addContent(new Element("hr"));
                                    bodyElement.addContent(hrDiv);
                                    bodyElement.addContent(problemDiv);
                                }
                            }

                            FileOutputStream oStream = new FileOutputStream(
                                    file, false);
                            OutputStreamWriter out = new OutputStreamWriter(
                                    oStream, Constants.UTF8);

                            // The default encoding is UTF-8.
                            Format format = Format.getPrettyFormat();
                            format.setExpandEmptyElements(true);

                            // Omit declaration when writing because
                            // the code manually handles the XML declarations.
                            format.setOmitDeclaration(true);
                            DocType docType = new DocType("html");
                            // Write the changes back to the file
                            new XMLOutputter(format).output(docType, out);
                            new XMLOutputter(format).output(root, out);


                        } else {
                            logger.debug("Skipping " + filePath
                                + ". Could not read HTML content in file.");
                        }

                } else {
                    logger.debug("Invalid file " + filePath);
                }

            } // end for-each file


            logger.info("\nFinished sorting problems in HTML files.");
        } catch (Exception e) {
            logger.error("Unknown exception in sorting method,  ", e);
            return;
        }



    }

    /**
     * Adds the closing tag to close the XML document.
     * @param outputDirectory the outputDirectory for the problem content
     */
    public void addHtmlClosingTags(String outputDirectory) {
        // Close the "<ds_problem_content>" tags in all the XML files that were
        // generated after all the XML has been written.
        List<File> xmlOutputFiles =
            OliConverter.getFilenameList(logger, outputDirectory, "html");
        for (int j = 0, m = xmlOutputFiles.size(); j < m; j++) {
            File outputFile = null;
            FileOutputStream oStream = null;
            OutputStreamWriter out = null;
            try {
                outputFile = (File) xmlOutputFiles.get(j);

                oStream = new FileOutputStream(
                        outputFile, IS_APPEND);
                out = new OutputStreamWriter(
                        oStream, Constants.UTF8);
                // Write the XSD cap
                out.write(PROBLEM_CONTENT_END);
                out.close();
                oStream.close();

            } catch (IOException e) {
                logger.error("An IOException occurred when adding HTML closing tags.");
            }
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
     * Returns the base course version directory.
     * @param organization the organization object used to determine the content file path
     * @return the path to the base course version directory
     */
    private String getCourseVersionDir(Organization organization) {
        if (getTagDir() != null
            && outputDirectoryName != null) {
            String courseVersionPath = OUTPUT_DIRECTORY_STR + Constants.OLI_DIR_PART
                + getTagDir().replace(outputDirectoryName, "") + "/"
                + getOrgForPath(organization);
            return courseVersionPath.replaceAll("/+", "/").trim();
        }

        return null;
    }
}
