/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml2;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.xml.AbstractContextMessageParser;
import edu.cmu.pslc.datashop.xml.ContextMessage;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.XMLConstants;

/**
 * Parse context message with DTD v2 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 12374 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-20 20:57:07 -0400 (Wed, 20 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContextMessageParser2 extends AbstractContextMessageParser {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param xml the XML data
     */
    public ContextMessageParser2(String xml) {
        super(xml);
    }

    /**
     * Get a ContextMesssage from the XML data.
     * @return a ContextMessage object
     */
    public ContextMessage getContextMessage() {
        ContextMessage returnVal = new ContextMessage();
        try {
            StringReader reader = new StringReader(getXml());
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();

            //set contextMessage contextId and name fields
            //based on the context_id and name attributes of root node.
            returnVal.setContextId(
                MessageCommons.replaceInvalidChars(root.getAttributeValue("attempt_id")));
            returnVal.setName(
                MessageCommons.replaceInvalidChars(root.getAttributeValue("name")));

            //get each element from XML
            String school = MessageCommons.replaceInvalidChars(root.getChildTextTrim("school_name"));
            if (school == null) {
                school = "";
            }
            String course = MessageCommons.replaceInvalidChars(root.getChildTextTrim("course_name"));
            if (course == null) {
                course = "";
            }
            String unit = MessageCommons.replaceInvalidChars(root.getChildTextTrim("unit_name"));
            if (unit == null) {
                unit = "";
            }
            String section = MessageCommons.replaceInvalidChars(root.getChildTextTrim("section_name"));
            if (section == null) {
                section = "";
            }
            ProblemItem problemItem = getProblemItem(root);
            //set problem with default problem name if necessary
            if (problemItem.getProblemName() == null
                    || problemItem.getProblemName().equals("")) {
                problemItem.setProblemName(getDefaultProblemName());
            }

            //handle school
            if (!school.equals("")) {
                SchoolItem schoolItem = new SchoolItem();
                schoolItem.setSchoolName(school);
                returnVal.setSchoolItem(schoolItem);
            }

            //handle dataset
            if (course.equals("")) {
                course = getDefaultDatasetName();
            }
            if (!course.equals("")) {
                DatasetItem datasetItem = new DatasetItem();
                datasetItem.setDatasetName(course);
                returnVal.setDatasetItem(datasetItem);
                //handle dataset level and problem.
                if (!unit.equals("") && !section.equals("")) {
                    DatasetLevelItem unitLevel = new DatasetLevelItem();
                    unitLevel.setLevelName(unit);
                    unitLevel.setLevelTitle(DatasetLevelItem.UNIT_TITLE);
                    datasetItem.addDatasetLevel(unitLevel);
                    DatasetLevelItem sectionLevel = new DatasetLevelItem();
                    sectionLevel.setLevelName(section);
                    sectionLevel.setLevelTitle(DatasetLevelItem.SECTION_TITLE);
                    unitLevel.addChild(sectionLevel);
                    sectionLevel.setParent(unitLevel);
                    //process problem
                    if (problemItem != null) {
                        sectionLevel.addProblem(problemItem);
                    }
                } else if (!unit.equals("") && section.equals("")) {
                    DatasetLevelItem unitLevel = new DatasetLevelItem();
                    unitLevel.setLevelName(unit);
                    unitLevel.setLevelTitle(DatasetLevelItem.UNIT_TITLE);
                    datasetItem.addDatasetLevel(unitLevel);
                    //process problem
                    if (problemItem != null) {
                        unitLevel.addProblem(problemItem);
                    }
                } else if (unit.equals("") && !section.equals("")) {
                    DatasetLevelItem sectionLevel = new DatasetLevelItem();
                    sectionLevel.setLevelName(section);
                    sectionLevel.setLevelTitle(DatasetLevelItem.SECTION_TITLE);
                    datasetItem.addDatasetLevel(sectionLevel);
                    //process problem
                    if (problemItem != null) {
                        sectionLevel.addProblem(problemItem);
                    }
                } else if (unit.equals("") && section.equals("") && problemItem != null) {
                    if (!getDefaultDatasetLevelName().equals("")) {
                        DatasetLevelItem dslLevel = new DatasetLevelItem();
                        dslLevel.setLevelName(getDefaultDatasetLevelName());
                        dslLevel.setLevelTitle(getDefaultDatasetLevelTitle());
                        datasetItem.addDatasetLevel(dslLevel);
                        dslLevel.addProblem(problemItem);
                    }
                } else if (unit.equals("") && section.equals("") && problemItem == null) {
                    if (!getDefaultDatasetLevelName().equals("")
                            && !getDefaultProblemName().equals("")) {
                        DatasetLevelItem dslLevel = new DatasetLevelItem();
                        dslLevel.setLevelName(getDefaultDatasetLevelName());
                        dslLevel.setLevelTitle(getDefaultDatasetLevelTitle());
                        datasetItem.addDatasetLevel(dslLevel);
                        // get the default problem
                        problemItem = new ProblemItem();
                        problemItem.setProblemName(getDefaultProblemName());
                        dslLevel.addProblem(problemItem);
                    }
                }
            }

        } catch (JDOMException exception) {
            logger.warn("JDOMException occurred. " + exception.getMessage());
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
        }

        return returnVal;
    }

    /**
     * Get the problem element if it exists.  If not get the problem_name element.
     * @param root the root element
     * @return a problem item, or null if neither element is found
     */
    private ProblemItem getProblemItem(Element root) {
        Element problemElement = root.getChild("problem");
        ProblemItem problemItem = new ProblemItem();
        if (problemElement != null) {
            String problemName = MessageCommons.replaceInvalidChars(problemElement.getChildTextTrim("name"));
            if (problemName.length() > XMLConstants.PROBLEM_NAME_LENGTH) {
                problemName = problemName.substring(0, XMLConstants.PROBLEM_NAME_LENGTH);
                logger.warn("Truncating problem name to [" + problemName + "]");
            }
            problemItem.setProblemName(problemName);

            String problemDesc = problemElement.getChildTextTrim("description");
            if (problemDesc != null && !problemDesc.equals("")) {
                problemItem.setProblemDescription(problemDesc);
            }
        } else {
            String problemName = root.getChildTextTrim("problem_name");
            problemItem.setProblemName(problemName);
        }
        return problemItem;
    }

}
