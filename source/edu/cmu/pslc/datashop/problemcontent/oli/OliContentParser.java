/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.problemcontent.oli;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import edu.cmu.pslc.datashop.util.FileUtils;

/**
 * Provides methods specific to parsing OLI problem content.
 *
 * @author Mike Komisin
 * @version $Revision: 11067 $ <BR>
 * Last modified by: $Author: mkomisin $ <BR>
 * Last modified on: $Date: 2014-05-21 23:43:50 -0400 (Wed, 21 May 2014) $ <!-- $KeyWordsOff: $ -->
 */
public class OliContentParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The actual XML document. */
    private Document xmlDocument;
    /** The File object of the XML document. */
    private File file;
    /** An optional list of workbooks verified to be valid within the context of an organization. */
    private List validWorkbooks;
    /** The input directory of the course package. */
    private String inputDirectory;
    /** The course name associated with this course package. */
    private String courseName;
    /** The course version associated with this course package. */
    private String courseVersion;

    /**
     * Constructor.
     * @param xmlDocument the XML document
     * @param file the File object of the XML document
     * @param validWorkbooks an optional list of workbooks verified to be valid
     * within the context of an organization
     */
    public OliContentParser(Document xmlDocument, File file, List validWorkbooks) {
        this.xmlDocument = xmlDocument;
        this.file = file;
        this.validWorkbooks = validWorkbooks;
    }

    /**
     * Returns the XML data.
     * @return the XML data
     */
    public Document getXmlDocument() {
        return xmlDocument;
    }

    /**
     * Returns the File object of the XML document.
     * @return the File object of the XML document
     */
    public File getFile() {
        return file;
    }

    /**
     * Maps in-line and activity elements to the appropriate workbook.
     * @param workbookHierarchy the workbook hierarchy
     * @param problemFileMap a mapping of problems to their respective files
     * used when generating the content package
     * @param poolElementMap the mapping of problems to their respective elements used to
     * resolve question pools
     * @param organization the organization object
     * @param courseContentFile the file being parsed
     * @param resourceDir the new resource directory
     * false if the method should generate the content package
     */
    public void mapWorkbookElements(Map<String, List<String>> workbookHierarchy,

            Map<String, List<String>> problemFileMap,
            Map<String, List<Element>> poolElementMap, Organization organization,
            File courseContentFile, File resourceDir) {

        Document doc = getXmlDocument();
        Element root = doc.getRootElement();

        logger.debug("Root node: " + root.getName() + ", id = " + root.getAttributeValue("id"));

        if (root.getName().equals("workbook_page")) {
            String workbookId = root.getAttributeValue("id");

            if (workbookId != null) {
                Workbook workbook = new Workbook(root, courseContentFile);
                // Add workbook here, initially
                organization.addWorkbook(workbook);
                // Get inline activities since they may be workbooks inside of workbooks
                List<String> activityIds;
                if (workbookHierarchy.containsKey(workbookId)) {
                    activityIds = workbookHierarchy.get(workbookId);
                } else {
                    activityIds = new ArrayList<String>();
                }

                for (Element activity : workbook.getActivities()) {
                    if (!workbookId.equals(activity.getAttributeValue("idref"))) {
                        activityIds.add(activity.getAttributeValue("idref"));
                        logger.debug("Adding workbook page " + workbookId
                            + " to the workbook hierarchy for activity "
                            + activity.getAttributeValue("idref"));

                    }
                }

                for (Element inline : workbook.getInlines()) {
                    if (!workbookId.equals(inline.getAttributeValue("idref"))) {
                        activityIds.add(inline.getAttributeValue("idref"));
                        logger.debug("Adding workbook page " + workbookId
                            + " to the workbook hierarchy for inline "
                            + inline.getAttributeValue("idref"));
                    }
                }

                workbookHierarchy.put(workbookId, activityIds);
            }
        }
    }

    /**
     * Updates necessary problem and hierarchy information by pulling
     * indirectly referenced elements from the workbooks.
     * @param workbookHierarchy a mapping of inlines/activities to their parent workbooks
     * @param problemFileMap a mapping of problems to their respective files
     * used when generating the content package
     * @param poolElementMap the mapping of problems to their respective elements used to
     * resolve question pools
     * @param organization the organization object
     * @param courseContentFile the file being parsed
     * @param resourceDir the new resource directory
     * false if the method should generate the content package
     *
     */
    public void parseWorkbookDocument(Map<String, List<String>> workbookHierarchy,
            Map<String, List<String>> problemFileMap,
            Map<String, List<Element>> poolElementMap, Organization organization,
            File courseContentFile, File resourceDir) {

        Document doc = getXmlDocument();
        Element root = doc.getRootElement();
        String filePath = courseContentFile.getAbsolutePath();

        if (root.getName().equals("workbook_page")) {
            String workbookId = root.getAttributeValue("id");

            if (workbookId != null) {
                Workbook workbook = new Workbook(root, courseContentFile);
                indexWorkbookFile(problemFileMap, workbook, filePath);

                String containerId = organization.getContainerFromWorkbook(workbookId);

                // Container references workbook indirectly via an activity

                if (containerId == null || containerId.isEmpty()) {
                    logger.debug("Workbook catalogued: " + workbook.getTitle()
                        + "(" + workbook.getWorkbookId() + ")");
                    // Resolve container
                    String rootWorkbookId = getRootWorkbook(
                        organization, workbookHierarchy, workbookId);

                    if (rootWorkbookId != null) {
                        // Add the workbookId as an inline/activity of the rootWorkbook


                        if (!organization.getHierarchyFromElement(rootWorkbookId)
                                .equals(OliConverter.UNREFERENCED_CONTENT)) {

                            containerId = organization.getContainerFromWorkbook(rootWorkbookId);
                            organization.addInlineActivity(workbookId, rootWorkbookId);
                            logger.debug("Container id: " + containerId + " contains "
                                + " workbook: " + rootWorkbookId
                                + " which contains activity: " + workbookId);
                        }
                    }
                } else {
                    logger.debug("Workbook has container: " + workbook.getTitle()
                        + "(" + workbook.getWorkbookId() + ")");
                }

                if (containerId != null) {
                    logger.debug("Workbook has container: " + workbook.getTitle()
                        + "(" + workbook.getWorkbookId() + ")");
                    validWorkbooks.add(workbookId);


                    for (Element elem : workbook.getActivities()) {
                        organization.addInlineActivity(elem.getAttributeValue("idref"), workbookId);

                    }

                    for (Element elem : workbook.getInlines()) {
                        organization.addInlineActivity(elem.getAttributeValue("idref"), workbookId);

                    }

                    organization.indexActivities(workbook.getActivities(), workbook);
                    organization.indexInline(workbook.getInlines(), workbook);
                    organization.indexLearningObjectives(
                        workbook.getLearningObjectives(), workbook);
                }
            }
        }
    }

    /**
     * Updates necessary problem and hierarchy information by pulling
     * indirectly referenced elements from the workbooks.
     * @param workbookHierarchy a mapping of inlines/activities to their parent workbooks
     * @param problemFileMap a mapping of problems to their respective files
     * used when generating the content package
     * @param poolElementMap the mapping of problems to their respective elements used to
     * resolve question pools
     * @param organization the organization object
     * @param courseContentFile the file being parsed
     * @param resourceDir the new resource directory
     * false if the method should generate the content package
     *
     */
    public void parseAssessmentDocument(Map<String, List<String>> workbookHierarchy,
            Map<String, List<String>> problemFileMap,
            Map<String, List<Element>> poolElementMap, Organization organization,
            File courseContentFile, File resourceDir) {

        Document doc = getXmlDocument();
        Element root = doc.getRootElement();
        String filePath = courseContentFile.getAbsolutePath();

        if (root.getName().equals("assessment")) {
            String assessmentId = root.getAttributeValue("id");

            if (assessmentId != null) {
                String title = "";

                Element titleElement = root.getChild("title");
                if (titleElement != null) {
                    title = titleElement.getText().trim();
                }

                String containerId = organization.getContainerFromWorkbook(assessmentId);

                // Container references workbook indirectly via an activity

                if (containerId == null || containerId.isEmpty()) {
                    logger.debug("Assessment catalogued: " + title
                        + "(" + assessmentId + ")");
                    // Resolve container
                    String rootAssessmentId = getRootWorkbook(
                        organization, workbookHierarchy, assessmentId);

                    if (rootAssessmentId != null) {
                        // Add the workbookId as an inline/activity of the rootWorkbook


                        if (!organization.getHierarchyFromElement(rootAssessmentId)
                                .equals(OliConverter.UNREFERENCED_CONTENT)) {

                            containerId = organization.getContainerFromWorkbook(rootAssessmentId);
                            organization.addInlineActivity(assessmentId, rootAssessmentId);
                            logger.debug("Container id: " + containerId + " contains "
                                + " assessment: " + rootAssessmentId
                                + " which contains activity: " + assessmentId);
                        }
                    }
                }

                if (containerId != null) {
                    validWorkbooks.add(assessmentId);
                    List<Element> poolRefs = CommonXml.getElementsByTagName(root, "pool_ref");
                    indexAssessmentFile(problemFileMap, poolRefs, filePath);
                    organization.indexPools(poolRefs, assessmentId);

                }
            }
        }
    }

    /**
     * This method creates an XML document containing a list of problem elements
     * from the OLI problem content (i.e., assessments, in-line assessments, and question pools).
     * Resources are copied to the resourceDir, adjusting the file paths in the XML document
     * as needed.
     * @param workbookHierarchy a mapping of inlines/activities to their parent workbooks
     * @param problemFileMap a mapping of problems to their respective files
     * used when generating the content package
     * @param poolElementMap the mapping of problems to their respective elements used to
     * resolve question pools
     * @param organization the organization object
     * @param courseContentFile the file being parsed
     * @param resourceDir the new resource directory
     * @param resolveMappings true if the method should resolve the mappings;
     * false if the method should generate the content package
     * @return the XML document
     */
    public Document parseXMLDocument(Map<String, List<String>> workbookHierarchy,
            Map<String, List<String>> problemFileMap,
            Map<String, List<Element>> poolElementMap, Organization organization,
            File courseContentFile, File resourceDir, Boolean resolveMappings) {


        String filePath = courseContentFile.getAbsolutePath()
            .replaceAll("\\\\", "/").replaceAll("/+", "/");
        logger.debug("Course content file: " + filePath);
        Document xmlOutput = new Document(new Element("dummy_root"));
        Document doc = getXmlDocument();
        Element root = doc.getRootElement();
        String hierarchy = null;
        logger.debug("Root node: " + root.getName() + ", root id: " + root.getAttributeValue("id"));

        if (root.getName().equals("pool")
                && resolveMappings) {
            // Get pool Id.
            String problemName = root.getAttributeValue("id");
            indexPoolFile(problemFileMap, problemName, filePath);

            List<Element> pages = root.getChildren("page");
            if (pages.isEmpty()) {
                pages = new ArrayList<Element>();
                pages.add(root);
            }

            for (Element page : pages) {
                Element content = null;
                if (page.getChild("content") != null) {
                    content = page.getChild("content");
                }
                // Acquire 'question' elements that contain question types.
                List<Element> inlineQuestionElements = new ArrayList<Element>();
                inlineQuestionElements.addAll(CommonXml.getElementsByTagName(page, "question"));
                // Detach acquired elements from root.
                for (Iterator inlineIter = inlineQuestionElements.iterator();
                        inlineIter.hasNext();) {
                    Element inlineElem = (Element)inlineIter.next();
                    inlineElem.detach();
                }

                // Acquire question types not contained in 'question' elements.
                for (String qType : OliConverter.OLI_QUESTION_TYPES) {
                    inlineQuestionElements.addAll(CommonXml.getElementsByTagName(page, qType));
                }

                // Detach acquired elements from root.
                for (Iterator inlineIter = inlineQuestionElements.iterator();
                        inlineIter.hasNext();) {
                    Element inlineElem = (Element)inlineIter.next();
                    inlineElem.detach();
                }

                List<Element> poolElements = null;
                if (poolElementMap.containsKey(problemName)) {
                    poolElements = poolElementMap.get(problemName);
                } else {
                    poolElements = new ArrayList<Element>();
                }

                // Add questions found in selection pools to the poolElementMap
                Iterator elemIter = inlineQuestionElements.iterator();
                while (elemIter.hasNext()) {
                    Element element = (Element) elemIter.next();
                    // If we are resolving pool selection references, then
                    // cache the question elements found in pools.
                    logger.debug("Caching pool element "
                        + element.getAttributeValue("id"));
                    if (element.getChild("body") !=  null
                            && content != null) {
                        Element body = element.getChild("body");
                        Element cloneContent = (Element) content.clone();
                        cloneContent.setName("div");
                        cloneContent.setAttribute("class", "oli-content");
                        body.addContent(cloneContent);
                    }
                    poolElements.add(element);
                }
                // Add indirectly referenced questions to the mapping
                if (!poolElements.isEmpty()) {
                    poolElementMap.put(problemName, poolElements);
                }
            }

        } else if (root.getName().equals("assessment")
                && resolveMappings) {

            // Get pool Id.
            String problemName = root.getAttributeValue("id");
            // Create a mapping of pool questions to the poolElementMap

            // Acquire generic question elements that contain question types.
            List<Element> inlineQuestionElements = new ArrayList<Element>();
            inlineQuestionElements.addAll(CommonXml.getElementsByTagName(root, "question"));
            // Detach acquired elements from root.
            for (Iterator inlineIter = inlineQuestionElements.iterator(); inlineIter.hasNext();) {
                Element inlineElem = (Element)inlineIter.next();
                inlineElem.detach();
            }

            // Acquire elements named after question types not found in generic question elements.
            for (String qType : OliConverter.OLI_QUESTION_TYPES) {
                inlineQuestionElements.addAll(CommonXml.getElementsByTagName(root, qType));
            }

            // Detach acquired elements from root.
            for (Iterator inlineIter = inlineQuestionElements.iterator(); inlineIter.hasNext();) {
                Element inlineElem = (Element)inlineIter.next();
                inlineElem.detach();
            }

            List<Element> inlineElements = null;
            if (poolElementMap.containsKey(problemName)) {
                inlineElements = poolElementMap.get(problemName);
            } else {
                inlineElements = new ArrayList<Element>();
            }

            // Add questions found in selection pools to the poolElementMap
            Iterator elemIter = inlineQuestionElements.iterator();
            while (elemIter.hasNext()) {
                Element element = (Element) elemIter.next();
                // Add element to the inlineElements list
                inlineElements.add(element);
            }
            // Store the new list in the poolElementMap
            if (!inlineElements.isEmpty()) {
                poolElementMap.put(problemName, inlineElements);
            }

        } else if (root.getName().equals("assessment") && !resolveMappings) {

            /* Part I: Extract data from the assessment page. */

            // Get assessment Id.
            String problemName = root.getAttributeValue("id");
            // Number of parent dirs relative to the base of the course path
            Integer numParentDirs = 0;

            // Get the hierarchy for the workbook Id that references this assessment.
            hierarchy = getHierarchy(organization, problemName);

            logger.debug("Generating content for assessment id: " + problemName);
            if (hierarchy == null) {
                hierarchy = OliConverter.UNREFERENCED_CONTENT;
            } else {
                logger.debug("Found assessment in hierarchy: " + hierarchy);
            }

            // Set the number of parent directories between the problem directory
            // and base directory for the generated problem content package
            numParentDirs = hierarchy.split("/").length;

            // Get the problem description from the title of the OLI activity.
            String problemDescription = getTitle(root);

            // Create the problem element.
            Element problemElement = new Element("problem");
            problemElement.addContent(new Element("name").setText(problemName));
            problemElement.addContent(new Element("description")
                    .setText(problemDescription));

            // Get the Learning Objective for this assessment from the workbook pages
            String purpose = organization.getPurpose(problemName);
            if (purpose != null) {
                problemElement.addContent(new Element("purpose").setText(purpose));
            }
            // Create the relative path based on problem hierarchy.
            String cdString = "";

            for (int cdCount = 0; cdCount < numParentDirs; cdCount++) {
                cdString = cdString + "../";
            }

            // Acquire generic question elements that contain question types.
            List<Element> inlineQuestionElements = new ArrayList<Element>();
            inlineQuestionElements.addAll(CommonXml.getElementsByTagName(root, "question"));
            // Detach acquired elements from root.
            for (Iterator inlineIter = inlineQuestionElements.iterator(); inlineIter.hasNext();) {
                Element inlineElem = (Element)inlineIter.next();
                //////inlineElem.detach();
            }

            // Acquire elements named after question types not found in generic question elements.
            for (String qType : OliConverter.OLI_QUESTION_TYPES) {
                inlineQuestionElements.addAll(CommonXml.getElementsByTagName(root, qType));
            }

            // Detach acquired elements from root.
            for (Iterator inlineIter = inlineQuestionElements.iterator(); inlineIter.hasNext();) {
                Element inlineElem = (Element)inlineIter.next();
                //////inlineElem.detach();
            }

            /* Part II: Create a prompt to contain the extracted data. */

            // Format and add the questions to the prompt
            Element prompt = new Element("prompt");
            List<Element> questions = new ArrayList<Element>();
            // Get possible relative paths for problems included in the list
            List<String> problemFilePaths = new ArrayList<String>();

            // Add indirectly reference questions from selection pools
            List<Element> poolRefs = CommonXml.getElementsByTagName(root, "pool_ref");
            if (poolRefs.size() > 0) {
                logger.debug("Number of pool_refs in " + filePath + ": " + poolRefs.size());
            }
            // Add pool selections included in this assessment
            for (Iterator poolIter = poolRefs.iterator(); poolIter.hasNext();) {
                Element refElement = (Element) poolIter.next();
                String idref = refElement.getAttributeValue("idref");
                if (poolElementMap.containsKey(idref)) {
                    List<Element> questionList = poolElementMap.get(idref);
                    logger.debug("Found " + poolElementMap.get(idref).size()
                            + " pool elements for " + problemName);

                    inlineQuestionElements.addAll(questionList);
                    logger.debug("Pool ref(" + idref + ")");
                    if (problemFileMap.containsKey(idref)) {
                        logger.debug("Pool ref exists(idref): " + idref);
                        problemFilePaths.addAll(problemFileMap.get(idref));
                    }
                }
            }

            // Get the current directory of the assessment file
            File currentWorkingDir = new File(courseContentFile.getParent());
            // Used to resolve images when the relative paths are
            // relative to the workbooks and not the assessments
            logger.debug("Problem name: " + problemName);
            if (problemFileMap.containsKey(problemName)) {
                logger.debug("problemFileMap contains Problem Name: " + problemName);
                problemFilePaths.addAll(problemFileMap.get(problemName));
            }

            // Add questions found in selection pools to the poolElementMap
            Iterator elemIter = inlineQuestionElements.iterator();
            while (elemIter.hasNext()) {
                Element element = (Element) elemIter.next();

                // Ensure the question is in a consistent question format
                Element questionElement = standardizeQuestion(element);



                // Add the questions that could be parsed.
                if (questionElement != null) {
                    logger.debug("questionElement: " + questionElement);
                    Element content = null;
                    if (element.getParent() != null) {
                        Element parent = (Element) element.getParent();
                        content = parent.getChild("content");
                    }

                    if (content != null) {
                        logger.debug("Content element exists.");
                        Element cloneContent = (Element) content.clone();
                        cloneContent.setName("div");
                        cloneContent.setAttribute("class", "oli-content");
                        Element body = questionElement.getChild("body");
                        // If the body element exists, add content to the body
                        if (body != null) {
                            // Important: Since it is being added to the body
                            // we will wait til body is parsed to add resources
                            body.addContent(0, cloneContent);
                        } else {
                            // Otherwise, add content to the question
                            // and parse it explicitly
                            Element newBody = new Element("body");
                            newBody.addContent(0, cloneContent);
                            questionElement.addContent(0, newBody);
                        }
                    }

                    questionElement = addResources(questionElement, currentWorkingDir,
                            problemFilePaths, resourceDir, cdString, courseContentFile);

                    if (questionElement != null) {
                        List<Element> essays = CommonXml.getElementsByTagName(
                            questionElement, "essay");
                        List<Element> shortAnswers = CommonXml.getElementsByTagName(
                            questionElement, "short_answer");

                        // Eliminate the short_answer if it is accompanied by an essay element
                        // per Alida's request
                        if (essays.size() == 1 && shortAnswers.size() == 1) {
                            Element essay = essays.get(0);
                            Element shortAnswer = shortAnswers.get(0);
                            // Retain the short answer id if it exists
                            if (shortAnswer.getAttributeValue("id") != null) {
                                essay.setAttribute("id", shortAnswer.getAttributeValue("id"));
                            }
                            // Get the contents of the short_answer
                            if (shortAnswer.getChildren() != null) {
                                for (Iterator shortIter = shortAnswer.getChildren().iterator();
                                    shortIter.hasNext();) {
                                    Element shortAnswerChild = (Element) shortIter.next();
                                    shortIter.remove();
                                    essay.addContent(shortAnswerChild);
                                }

                            }
                            shortAnswer.detach();
                        }
                    }

                } else {
                    logger.debug("Null question in " + problemName);
                }

                questions.add(questionElement);
            }


            if (questions.isEmpty()) {
                logger.warn("Empty prompt found for " + problemName);
            }

            // Add all of the reformatted (for consistency) questions to the prompt
            for (Element question : questions) {
                if (question != null) {
                    question.detach();
                    logger.debug("Element type: " + question.getName());
                    logger.debug("Question id: " + question.getAttribute("id"));
                    logger.debug("Question children count: " + question.getChildren().size());

                    prompt.addContent(question);

                }
            }

            // Add the prompt to the problem element
            problemElement.addContent(prompt);

            // Add the problem to the root element
            xmlOutput.getRootElement().addContent(problemElement);


        } else if (root.getName().equals("objectives")
                && resolveMappings) {
            // Add learning objectives
            List<Element> objectives = CommonXml.getElementsByTagName(root, "objective");
            if (!objectives.isEmpty()) {
                for (Iterator iter = objectives.iterator(); iter.hasNext();) {

                    Element objective = (Element) iter.next();
                    objective.detach();
                    String objectiveId = objective.getAttributeValue("id");
                    if (objectiveId != null) {
                        // Since we don't yet know the workbook's Id,
                        // then use the objective Id as a placeholder (use "ref_" + Id).
                        // This will be resolved later during the content generation phase.
                        organization.addLearningObjective(objective, "ref_" + objectiveId);
                    }
                }
            }
        }

        return xmlOutput;
    }

    /**
     * Maintains a list of file paths associated with each of the problems
     * contained in a workbook.
     * @param problemFileMap the problem file map
     * @param workbook the workbook
     * @param filePath the file path
     */
    private void indexWorkbookFile(Map<String, List<String>> problemFileMap,
            Workbook workbook, String filePath) {
        for (Element inline : workbook.getInlines()) {
            String id = inline.getAttributeValue("idref");
            List<String> filePaths = problemFileMap.get(id);
            if (filePaths == null || filePaths.isEmpty()) {
                filePaths = new ArrayList<String>();
            }

            if (!filePaths.contains(filePath) && filePath != null) {
                logger.debug("File path of workbook: " + filePath);
                logger.debug("Inline id: " + id);
                filePaths.add(filePath);
            }
            problemFileMap.put(id, filePaths);
        }

        for (Element activity : workbook.getActivities()) {
            String id = activity.getAttributeValue("idref");
            List<String> filePaths = problemFileMap.get(id);
            if (filePaths == null || filePaths.isEmpty()) {
                filePaths = new ArrayList<String>();
            }

            if (!filePaths.contains(filePath) && filePath != null) {
                logger.debug("File path of workbook: " + filePath);
                logger.debug("Activity id: " + id);
                filePaths.add(filePath);
            }
            problemFileMap.put(id, filePaths);
        }
    }

    /**
     * Maintains a list of file paths associated with each of the problems
     * contained in an assessment.
     * @param problemFileMap the problem file map
     * @param poolRefs the pool references
     * @param filePath the file path
     */
    private void indexAssessmentFile(Map<String, List<String>> problemFileMap,
            List<Element> poolRefs, String filePath) {
        for (Element inline : poolRefs) {
            String id = inline.getAttributeValue("idref");
            List<String> filePaths = problemFileMap.get(id);
            if (filePaths == null || filePaths.isEmpty()) {
                filePaths = new ArrayList<String>();
            }

            if (!filePaths.contains(filePath) && filePath != null) {
                logger.debug("File path of workbook: " + filePath);
                logger.debug("Inline id: " + id);
                filePaths.add(filePath);
            }
            problemFileMap.put(id, filePaths);
        }
    }

    /**
     * Maintains a list of file paths associated with each of the problems
     * contained in an assessment pool.
     * @param problemFileMap the problem file map
     * @param problemName the pool id
     * @param filePath the file path
     */
    private void indexPoolFile(Map<String, List<String>> problemFileMap,
            String problemName, String filePath) {

        List<String> filePaths = problemFileMap.get(problemName);
        if (filePaths == null || filePaths.isEmpty()) {
            filePaths = new ArrayList<String>();
        }

        if (!filePaths.contains(filePath) && filePath != null) {
            logger.debug("File path of pool: " + filePath);
            logger.debug("Pool id: " + problemName);
            filePaths.add(filePath);
        }
        problemFileMap.put(problemName, filePaths);

    }

    /**
     * Recursive method which returns the root workbook Id for
     * of a workbook.
     * @param organization the organization
     * @param workbookHierarchy the workbook hierarchy map
     * @param workbookId the workbook id
     * @return the root workbook id
     */
    private String getRootWorkbook(Organization organization,
            Map<String, List<String>> workbookHierarchy, String workbookId) {

        if (organization.getContainerFromWorkbook(workbookId) != null) {
            return workbookId;
        }

        for (String wbId : workbookHierarchy.keySet()) {
            List<String> activityIds = workbookHierarchy.get(wbId);

            if (activityIds.contains(workbookId)) {
                String containerId = organization.getContainerFromWorkbook(wbId);
                if (containerId == null || containerId.isEmpty()) {
                    workbookId = getRootWorkbook(organization, workbookHierarchy, wbId);
                } else {
                    workbookId = wbId;
                }
                logger.debug("Resolved root of workbook '" + workbookId
                    + "' to root '" + wbId + "'");
                return workbookId;
            }
        }
        return null;
    }

    /**
     * Get the hierarchy for the problem name within the organization.
     * @param organization the organization
     * @param problemName the problem name
     * @return the hierarchy string
     */
    private String getHierarchy(Organization organization, String problemName) {
        String hierarchy = null;
        String workbookId = organization.getWorkbookFromElementId(problemName);

        if (workbookId == null) {
            workbookId = problemName;
        }
        // If the problem exists in a workbook referenced by the organization,
        // then get the container (seq, unit, module, or section) and the hierarchy.
        // At least one sequence must exist in the organization.
        if (workbookId != null) {
            // Get the hierarchy for the workbook which contains this problem
            String container = organization.getContainerFromWorkbook(workbookId);
            if (container != null && !container.isEmpty()) {
                // Find the hierarchy to which this element belongs.
                hierarchy = organization.getHierarchyFromElement(container);
            }
        }

        if (hierarchy == null) {
            return OliConverter.UNREFERENCED_CONTENT;
        }
        return hierarchy;
    }

    /**
     * Recursively adds resources for each element to a map, keyed on element.
     * @param resourceElement the element that contains the resource
     * @param currentWorkingDir the base directory relative to the resource file
     * @param problemFilePaths the list of files containing references to the question
     * @param resourceDir the new resource directory
     * @param cdString the file path prefix relative to the HTML file
     * @param courseContentFile the course content file being parsed
     * @return the resourceElement that contains the resource
     */
    private Element addResources(Element resourceElement, File currentWorkingDir,
            List<String> problemFilePaths, File resourceDir, String cdString,
            File courseContentFile) {
        if (resourceElement != null) {
            // Add all resources of this element's children.
            List<Element> children = resourceElement.getChildren();
            for (Element element : children) {
                element = addResources(element, currentWorkingDir,
                        problemFilePaths, resourceDir, cdString, courseContentFile);
            }
            // Create a new resource file with its own unique name.
            resourceElement = createNewResource(resourceElement, currentWorkingDir,
                    problemFilePaths, resourceDir, cdString, courseContentFile);

            return resourceElement;
        }
        // No resources were found.
        return null;
    }





    /**
     * Adds resources for each question to a question-resource map
     * and modifies the relative paths within the elements to point
     * to the new relative location in the content package based on hierarchy.
     * @param element the element that contains or references the resource
     * @param activityParentDir the base resources directory for all non-XML files
     * @param problemFilePaths the list of files containing references to the question
     * @param resourcesOutputDir the new resource directory
     * @param cdString the prefix to the file path relative to the problem content file
     * @param courseContentFile the course content file being parsed
     * @return the resourceElement that contains the resource
     */
    private Element createNewResource(Element element, File activityParentDir,
        List<String> problemFilePaths, File resourcesOutputDir,
            String cdString, File courseContentFile) {

        // Modify relative paths, if a valid path can be found
        if ((element.getAttributeValue("src") != null
                && element.getAttributeValue("src")
                    .replaceAll("\\\\", "/").replaceAll("/+", "/").matches("\\.\\./.*"))
            || (element.getAttributeValue("href") != null
                && element.getAttributeValue("href")
                    .replaceAll("\\\\", "/").replaceAll("/+", "/").matches("\\.\\./.*"))) {

            File resourceFile = null;
            // Parse the src value so we can retrieve the resource
            String src = element.getAttributeValue("src");
            if (element.getAttributeValue("href") != null) {
                src = element.getAttributeValue("href");
            }

            // Deal with improper slashes in courses, like C@CM
            src = src.replaceAll("\\\\", "/").replaceAll("/+", "/");

            // Split the path according to the OLI-specific delimiter
            String[] parsedPath = src.split("/");
            // Count the number of relative paths to backtrack since we
            // are going to move the file out of its relative path
            // and into a new path that is relative to the hierarchy directory.
            int numParents = 0;
            for (String s : parsedPath) {
                if (s.equals("..")) {
                    numParents++;
                }
            }

            // Prune the directories until we are in the correct ancestor
            String prunedDir = activityParentDir.getAbsolutePath()
                .replaceAll("\\\\", "/").replaceAll("/+", "/");
            if (prunedDir.matches(".*/")) {
                prunedDir = prunedDir.substring(0, prunedDir.lastIndexOf("/"));
            }

            // Pruning is based on the number of relative paths to backtrack
            for (int cdCount = 0; cdCount < numParents; cdCount++) {
                prunedDir = prunedDir.substring(0, prunedDir.lastIndexOf("/"));
            }

            // Build the full resourceFilePath from the assessment path
            String resourceFilePath = prunedDir + "/"
                    + src.replaceAll("\\.\\./", "");
            logger.debug("New resourceFilePath base: " + resourceFilePath);
            // Prepare the resource file for copy
            resourceFile = new File(resourceFilePath);
            // If the path is incorrect, then use the path relative to any
            // workbook pages which contain the problem
            if (!resourceFile.exists()) {
                if (problemFilePaths != null) {
                    for (String filePath : problemFilePaths) {
                        File problemFile = new File(filePath);
                        if (problemFile.exists() && problemFile.isFile()) {

                            // Prune the directories until we are in the correct ancestor
                            prunedDir = problemFile.getParent();
                            // Replace all windows-based file separators
                            prunedDir = prunedDir.replaceAll("\\\\", "/");
                            if (prunedDir.matches(".*/")) {
                                prunedDir = prunedDir.substring(0, prunedDir.lastIndexOf("/"));
                            }

                            // Pruning is based on the number of relative paths to backtrack
                            for (int cdCount = 0; cdCount < numParents; cdCount++) {
                                prunedDir = prunedDir.substring(0, prunedDir.lastIndexOf("/"));
                            }

                            // Build the full resourceFilePath from the pool path
                            resourceFilePath = prunedDir + "/"
                                    + src.replaceAll("\\.\\./", "");

                            // Prepare the resource file for copy
                            resourceFile = new File(resourceFilePath);

                            if (!resourceFile.exists()) {
                                logger.debug("Problem file: " + problemFile.getAbsolutePath());
                                logger.debug("Resource file not found in " + resourceFilePath
                                    + ". Trying next available path.");
                            } else {
                                logger.debug("Found file in " + resourceFilePath);
                                break;
                            }
                        }
                    }

                    if (!resourceFile.exists()) {
                        logger.debug("Could not find a valid file path.");
                    }
                }
            }

            // If the resource file exists and it hasn't already been
            // copied to the new resources directory, then copy it over.

            if (resourceFile.exists() && resourceFile.isFile()
                        && resourceFile.length() > 0) {

                // Get file name and extension for future reference.
                String fileName = resourceFile.getName();
                String fileExt = "";

                int extensionIndex = resourceFile.getName().length();
                if (fileName.lastIndexOf(".") > 0) {
                    extensionIndex = fileName.lastIndexOf(".");
                    fileExt = fileName.substring(extensionIndex);
                }
                // The prefix (old filename) to prepend to the md5
                // checksum and file size strings.
                String prefixName = fileName.substring(0, extensionIndex);

                // Use an md5 checksum to ensure file uniqueness.
                // This value is the md5 checksum of the actual file
                // concatenated with the file size. This guarantees uniqueness.
                // The original file name is added to the prefix for readability.
                String uniqueName = prefixName + "_" + CommonXml.md5Hash(resourceFilePath)
                        + resourceFile.length() + fileExt;

                // Use a global list to keep track of resource file uniqueness
                if (!OliConverter.resourceFileList.contains(uniqueName)) {
                    // The file has not been seen before, so add the new file.
                    OliConverter.resourceFileList.add(uniqueName);

                    String newFilePath =
                            resourcesOutputDir.getAbsolutePath().replaceAll("\\\\", "/")
                            + "/" + uniqueName;
                    File newResourceFile = new File(newFilePath);

                    try {
                        if (FileUtils.copyFile(resourceFile,
                                newResourceFile)) {
                            logger.debug("Copied resource file: "
                                    + resourceFile.getAbsolutePath()
                                    + " to \n"
                                    + newResourceFile.getAbsolutePath());
                        } else {
                            logger.error("Could not copy resource file: "
                                    + resourceFile.getAbsolutePath()
                                    + " to \n"
                                    + newResourceFile.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        logger.error("Could not attempt to copy resrouce file "
                            + resourceFile.getAbsolutePath());
                    }

                    logger.debug("Added file " + uniqueName + " to resources.");
                }

                // Replace any src attributes with the new relative path.
                String newSrc =
                    cdString  // relative to hierarchy path
                        + OliConverter.RESOURCE_DIR_NAME // to the resource path
                        + "/" + uniqueName;  // unique path within course package
                newSrc = newSrc.replaceAll("\\\\", "/").replaceAll("/+", "/");
                newSrc = newSrc.replaceAll(fileName, uniqueName);
                // Set the src attribute to the new path
                if (element.getAttributeValue("href") != null) {
                    element.setAttribute("href", newSrc);
                } else {
                    element.setAttribute("src", newSrc);
                }

                logger.debug("Updated file path " + newSrc + " in source.");

            }

        }
        return element;
    }

    /**
     * If the element is one of the 8 question types, then put the elements into a
     * consistent question format.
     * @param element the question type element
     * @return the question element
     */
    private Element standardizeQuestion(Element element) {
        logger.debug("Element: " + element.getName() + ", " + element.getAttributeValue("id"));
        for (Object att : element.getAttributes()) {
            Attribute attribute = (Attribute)att;
            logger.debug("Attributes: " + attribute.getName() + ", " + attribute.getValue());
        }
        Element questionElement = null;

        // If we find a question, then continue to the main body of the method
        boolean foundQuestion = false;
        // The element name is either a question type, or it is an actual question
        // element which contains the question type. We want either.
        for (String qType : OliConverter.OLI_QUESTION_TYPES) {
            if (qType.equals(element.getName())) {
                foundQuestion = true;
            }
        }

        if (element.getAttributeValue("id") != null
                && element.getName().equals("question")) {
            foundQuestion = true;
        }

        // Main body. Pre-format the question so we can consistently transform it to HTML.
        if (element.getAttributeValue("id") != null
                && foundQuestion) {

            logger.debug("Found question: " + element.getAttributeValue("id"));
            // Create a new question with a consistent format.
            String questionId = element.getAttributeValue("id");
            questionElement = new Element("question");
            questionElement.setAttribute("id", questionId);
            List<Element> inputRefs = new ArrayList<Element>();
            List<Element> inputs = new ArrayList<Element>();
            List<Element> qTypes = new ArrayList<Element>();
            logger.debug("Question child count: " + element.getChildren().size());

            // Get the inputs, input_refs, and question types
            String elemName = element.getName();
            Boolean innerQtypeFound = false;
            for (Iterator iter = element.getDescendants(new ElementFilter()); iter.hasNext();) {
                Element desc = (Element) iter.next();
                for (String qType : OliConverter.OLI_QUESTION_TYPES) {
                    if (qType.equals(desc.getName())) {
                        innerQtypeFound = true;
                        qTypes.add(desc);
                    }
                }
                if (desc.getName().equals("input_ref")) {
                    inputRefs.add(desc);
                } else if (desc.getName().equals("input")) {
                    inputs.add(desc);
                }
            }

            boolean replacedInputRefs = false;
            // 1. The elements in "inputs" are placed into their appropriate input_refs
            for (Element inputRef : inputRefs) {
                String inputRefId = inputRef.getAttributeValue("input");
                for (Iterator inputIter = inputs.iterator(); inputIter.hasNext();) {
                    Element input = (Element) inputIter.next();
                    String inputId = input.getAttributeValue("id");
                    // Replace input_refs with inputs if their id's match
                    // or if neither have id's
                    if ((inputId != null && inputId.equals(inputRefId))
                            || (inputId == null && inputRefId == null)) {
                        // Rename to parent element
                        inputRef.setName(element.getName());
                        inputRef.addContent(input.removeContent());
                        inputIter.remove();
                    }
                }
            }

            // 2. The elements in the questions are placed into their appropriate input_refs
            // and the inputRefs are given the question type name
            for (Element inputRef : inputRefs) {
                String inputRefId = inputRef.getAttributeValue("input");
                for (Iterator qTypeIter = qTypes.iterator(); qTypeIter.hasNext();) {
                    Element qType = (Element) qTypeIter.next();
                    String qTypeId = qType.getAttributeValue("id");
                    // Replace input_refs with qType elements if their id's match
                    // or if neither have id's
                    if ((qTypeId != null && qTypeId.equals(inputRefId))
                            || (qTypeId == null && inputRefId == null)) {
                        inputRef.setName(qType.getName());
                        // Get all attributes from the question and put into the input_ref
                        for (Attribute att : (List<Attribute>) qType.getAttributes()) {
                            inputRef.setAttribute(att.getName(), att.getValue());
                        }

                        inputRef.addContent(qType.removeContent());

                        // Override name with inner question type name, e.g. multiple_choice
                        qTypeIter.remove();
                    }
                }
            }

            // If there are any input_ref elements left, we will name them after the main element
            // 3. If no input elements or question elements were found, then the input_ref takes on
            // the name of the main question element
            for (Element inputRef : inputRefs) {
                // Override any extraneous input_refs
                if (inputRef.getName().equals("input_ref")) {
                    inputRef.setName(element.getName());
                }
            }


            // Get every element of question.
            Element cloneContent = (Element) element.clone();
            Iterator elemIter = cloneContent.getChildren().iterator();
            // Restructure question for consistency.
            while (elemIter.hasNext()) {
                Element innerElement = (Element) elemIter.next();

                if (innerElement.getName().equals("part")) {
                    elemIter.remove();
                    questionElement.addContent(innerElement);
                } else if (innerElement.getName().equals("body")) {
                    elemIter.remove();
                    questionElement.addContent(innerElement);
                } else if (innerElement.getName().equals("input")) {
                 // Rename the input element name to the question type
                    // We should not get into this any longer
                    // override any extraneous inputs
                    innerElement.setName(cloneContent.getName());
                    elemIter.remove();
                    if (inputRefs.isEmpty()) {
                        questionElement.addContent(innerElement);
                    }
                } else {
                    for (String qType : OliConverter.OLI_QUESTION_TYPES) {
                        if (qType.equals(innerElement.getName())) {
                            elemIter.remove();
                            questionElement.addContent(innerElement);
                        }

                    }
                }
            }

        }
        if (questionElement == null) {
            logger.debug("Question is null.");
        }
        // Return the formatted question element.
        return questionElement;
    }

    /**
     * Return the title from the assessment or pool element.
     * @param root the assessment or pool element
     * @return the title
     */
    protected String getTitle(Element root) {
        String title = null;
        if (root.getChildren("title") != null
                && !root.getChildren("title").isEmpty()) {
            title = root.getChild("title").getValue();
        }
        return title;
    }

    /**
     * Sets the base directory of the course package.
     * @param directory the base directory
     */
    public void setInputDir(String directory) {
        this.inputDirectory = directory;

    }

    /**
     * Sets the course name associated with this course package.
     * @param courseName the course name associated with this course package
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * Get the course name associated with this course package.
     * @return the course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Sets the course version associated with this course package.
     * @param courseVersion the course version associated with this course package
     */
    public void setCourseVersion(String courseVersion) {
        this.courseVersion = courseVersion;
    }

    /**
     * Get the course version associated with this course package.
     * @return the course version
     */
    public String getCourseVersion() {
        return courseVersion;
    }
}