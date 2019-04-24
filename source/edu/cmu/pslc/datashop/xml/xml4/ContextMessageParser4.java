/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml4;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.item.ClassItem;
import edu.cmu.pslc.datashop.item.ConditionItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.InstructorItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SchoolItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.xml.AbstractContextMessageParser;
import edu.cmu.pslc.datashop.xml.ContextMessage;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.XMLConstants;

/**
 * Parse the context message with DTD v4 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 12374 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-05-20 20:57:07 -0400 (Wed, 20 May 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContextMessageParser4 extends AbstractContextMessageParser {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * Constructor.
     * @param xml the XML data
     */
    public ContextMessageParser4(String xml) {
        super(xml);
    }

    /**
     * Return the user id's anon flag in the meta element.
     * @param metaElement the meta element
     * @return the anon flag if it exists, null otherwise
     */
    protected Boolean getAnonFlag(Element metaElement) {
        Boolean retFlag = null;
        if (metaElement != null) {
            Element userElement = metaElement.getChild("user_id");
            if (userElement != null) {
                String anonString = userElement.getAttributeValue("anonFlag");
                if (anonString != null) {
                    if (anonString.equals("true") || anonString.equals("false")) {
                        retFlag = new Boolean(anonString);
                    }
                }
            }
        }
        return retFlag;
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
                MessageCommons.replaceInvalidChars(root.getAttributeValue("context_message_id")));
            returnVal.setName(
                MessageCommons.replaceInvalidChars(root.getAttributeValue("name")));


            //get each element from XML
            Iterator messageIter = root.getChildren().iterator();

            while (messageIter.hasNext()) {
                Element messageElement = (Element)messageIter.next();

                String elementName = messageElement.getName();

                //get school, class and instructor from class element
                //and set the objects in the returnVal.
                if (elementName.equals("class")) {
                     handleClassElement(messageElement, returnVal);
                } else if (elementName.equals("dataset")) {
                    //get dataset from dataset element
                    handleDatasetElement(messageElement, returnVal);
                } else if (elementName.equals("custom_field")) {
                    handleCustomFieldElement(messageElement, returnVal);
                }  else if (elementName.equals("condition")) {
                    //add new condition to problem
                    handleConditionElement(messageElement, returnVal);
                }  else if (elementName.equals("meta")) {
                    returnVal.setAnonFlag(getAnonFlag(messageElement));
                } else if (elementName.equals("skill")) {
                    handleSkillElement(messageElement, returnVal);
                }
                //TODO need to handle DFA and skill. But currently database does not store them.

            } // end while loop

            //use the default setting to make default dataset if necessary.
            if (returnVal.getDatasetItem() == null) {
                DatasetItem datasetItem = new DatasetItem();
                returnVal.setDatasetItem(datasetItem);
            }
            //use the default setting to make default dataset level
            List levels = returnVal.getDatasetItem().getDatasetLevelsExternal();
            if (levels.size() == 0) {
                DatasetLevelItem level = new DatasetLevelItem();
                level.setLevelName(getDefaultDatasetLevelName());
                level.setLevelTitle(getDefaultDatasetLevelTitle());
                returnVal.getDatasetItem().addDatasetLevel(level);
            }

        } catch (JDOMException exception) {
            System.out.println("error in parsing");
            logger.warn("JDOMException occurred. " + exception.getMessage());
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
        }

        return returnVal;
    }

    /**
     * Construct SchoolItem, ClassItem and InstructorItems for Context Message.
     *
     * @param cElement the class element of the context message
     * @param cm ContextMessage that will be set
     */
    private void handleClassElement(Element cElement, ContextMessage cm) {
        String schoolVal = cElement.getChildTextTrim("school");
        String classVal = cElement.getChildTextTrim("name");
        String periodVal = cElement.getChildTextTrim("period");
        String description = cElement.getChildTextTrim("description");
        //There may be multiple instructors in the XML. Currently, the database only
        //allow one-to-many relationship for instructor to class.
        String instructorVal = cElement.getChildTextTrim("instructor");

        //set instructor item if instructor's name exists
        InstructorItem ii = null;
        if (instructorVal != null && !instructorVal.equals("")) {
            instructorVal = MessageCommons.replaceInvalidChars(instructorVal);
            if (instructorVal.length() > XMLConstants.INSTRUCTOR_NAME_LENGTH) {
                instructorVal = instructorVal.substring(0, XMLConstants.INSTRUCTOR_NAME_LENGTH);
                logger.warn("Truncating instructor, max length is "
                        + XMLConstants.INSTRUCTOR_NAME_LENGTH
                        + " characters. New instructor is: "
                        + instructorVal);
            }
            ii = new InstructorItem();
            ii.setInstructorName(instructorVal);
            cm.setInstructorItem(ii);
        }

        //set class item
        ClassItem ci = null;
        //if classVal is empty, try period;
        if (classVal != null && classVal.equals("")) {
            classVal = periodVal;
        }
        //if classVal is still empty, try description;
        if (classVal != null && classVal.equals("")) {
            classVal = description;
        }
        //if not empty, set classItem
        if (classVal != null && !classVal.equals("")) {
            classVal = MessageCommons.replaceInvalidChars(classVal);
            ci = new ClassItem();
            if (classVal.length() > XMLConstants.CLASS_NAME_LENGTH) {
                classVal = classVal.substring(0, XMLConstants.CLASS_NAME_LENGTH);
                logger.warn("Truncating class name , max length is "
                        + XMLConstants.CLASS_NAME_LENGTH
                        + " characters. New class name is: "
                        + classVal);
            }
            ci.setClassName(classVal);
            if (description != null && !description.equals("")) {
                description = MessageCommons.replaceInvalidChars(description);
                if (description.length() > XMLConstants.DESCRIPTION_LENGTH) {
                    description = description.substring(0, XMLConstants.DESCRIPTION_LENGTH);
                    logger.warn("Truncating class description , max length is "
                        + XMLConstants.DESCRIPTION_LENGTH
                        + " characters.");
                }
                ci.setDescription(description);
            }
            if (periodVal != null && !periodVal.equals("")) {
                periodVal = MessageCommons.replaceInvalidChars(periodVal);
                if (periodVal.length() > XMLConstants.PERIOD_NAME_LENGTH) {
                    periodVal = periodVal.substring(0, XMLConstants.PERIOD_NAME_LENGTH);
                    logger.warn("Truncating period , max length is "
                        + XMLConstants.PERIOD_NAME_LENGTH
                        + " characters. New period is: "
                        + periodVal);
                }
                ci.setPeriod(periodVal);
            }
            cm.setClassItem(ci);
        }

        //set school item if school's name exists
        SchoolItem si = null;
        if (schoolVal != null && !schoolVal.equals("")) {
            schoolVal = MessageCommons.replaceInvalidChars(schoolVal);
            si = new SchoolItem();
            if (schoolVal.length() > XMLConstants.SCHOOL_NAME_LENGTH) {
                schoolVal = schoolVal.substring(0, XMLConstants.SCHOOL_NAME_LENGTH);
                logger.warn("Truncating school name, max length is "
                        + XMLConstants.SCHOOL_NAME_LENGTH
                        + " characters. New school name is: "
                        + schoolVal);
            }
            si.setSchoolName(schoolVal);
            cm.setSchoolItem(si);
        }

        //set the relationship for instructor, class, and school
        if (ii != null) {
            if (si != null) {
                ii.setSchool(si);
            }
            if (ci != null) {
                ii.addClass(ci);
            }
        }
        if (ci != null) {
            if (ii != null) {
                ci.setInstructor(ii);
            }
            if (si != null) {
                ci.setSchool(si);
            }
        }
        if (si != null) {
            if (ci != null) {
                si.addClass(ci);
            }
            if (ii != null) {
                si.addInstructor(ii);
            }
        }
    }


    /**
     * Construct customFields for Context Message.
     *
     * @param cfElement the custom_field element of the context message
     * @param cm ContextMessage that will be set
     */
    private void handleCustomFieldElement(Element cfElement, ContextMessage cm) {
        String nameVal = cfElement.getChildTextTrim("name");
        String valueVal = cfElement.getChildTextTrim("value");

        //set new customFieldItem
        CustomFieldNameValueItem cfi = new CustomFieldNameValueItem();
        if (nameVal != null) {
            nameVal = MessageCommons.replaceInvalidChars(nameVal);
        }
        cfi.setName(nameVal);

        if (valueVal != null) {
            valueVal = MessageCommons.replaceInvalidChars(valueVal);
        }
        cfi.setValue(valueVal);

        cm.addCustomField(cfi);
    }

    /**
     * Construct condition for Context Message.
     *
     * @param element the condition element of the context message
     * @param cm ContextMessage that will be set
     */
    private void handleConditionElement(Element element, ContextMessage cm) {

        ConditionItem newCondition = new ConditionItem();
        String conditionName =
            MessageCommons.replaceInvalidChars(element.getChildTextTrim("name"));
        newCondition.setConditionName(conditionName);
        if (element.getChild("desc") != null) {
            String conditionDescription =
                MessageCommons.replaceInvalidChars(element.getChildTextTrim("desc"));
            newCondition.setDescription(conditionDescription);
        }
        if (element.getChild("type") != null) {
            String conditionType = MessageCommons.replaceInvalidChars(element.getChildTextTrim("type"));
            newCondition.setType(conditionType);
        }

        cm.addCondition(newCondition);
    }

    /**
     * Construct DatasetItem for Context Message.
     *
     * @param element the class element of the context message
     * @param cm ContextMessage that will be set
     */
    private void handleDatasetElement(Element element, ContextMessage cm) throws IOException {
        //name is required in dataset
        DatasetItem dsItem = new DatasetItem();
        dsItem.setDatasetName(element.getChildTextTrim("name"));

        //set cm's datasetItem
        cm.setDatasetItem(dsItem);

        handleDatasetLevelElement(element.getChild("level"), dsItem, null);
    }

    /**
     * Parse level in level element and add to either datasetItem or datasetLevelItem.
     *
     * @param element the level element of the context message
     * @param datasetItem if not null the DatasetItem
     *      to which the datasetLevelItem should be added on.
     * @param datasetLevelItem if not null the DatasetLevelItem
     *      to which the datasetLevelItem should be added on.
     */
    private void handleDatasetLevelElement(Element element,
                                        DatasetItem datasetItem,
                                        DatasetLevelItem datasetLevelItem) {
        DatasetLevelItem newLevelItem = new DatasetLevelItem();
        String typeVal = MessageCommons.replaceInvalidChars(element.getAttributeValue("type"));
        String nameVal = MessageCommons.replaceInvalidChars(element.getChildTextTrim("name"));

        newLevelItem.setLevelName(nameVal);
        newLevelItem.setLevelTitle(typeVal);

        //if datasetItem is passed not null, add the new level to the dataset.
        //if datasetLevelItem is passed not null, set child and parent relationship.
        if (datasetItem != null) {
            datasetItem.addDatasetLevel(newLevelItem);
        } else if (datasetLevelItem != null) {
            datasetLevelItem.addChild(newLevelItem);
            newLevelItem.setParent(datasetLevelItem);
        }

        //if more level exists, call self recursively
        if (element.getChild("level") != null) {
            handleDatasetLevelElement (element.getChild("level"), null, newLevelItem);
        } else {
            if (element.getChild("problem") != null) {
                handleProblemElement (element.getChild("problem"), newLevelItem);
            } else {
                // if no problem elements were found on root level, then
                // add a default problem
                ProblemItem problemItem = new ProblemItem();
                problemItem.setProblemName(getDefaultProblemName());
                newLevelItem.addProblem(problemItem);
            }
        }
    }

    /**
     * Parse problem in problem element and add to datasetLevelItem.
     *
     * @param element the problem element of the context message
     * @param datasetLevelItem the DatasetLevelItem to which the problem should be added on.
     */
    private void handleProblemElement(Element element,
                                        DatasetLevelItem datasetLevelItem) {
        ProblemItem problemItem = new ProblemItem();
        //add this problem to the datasetLevelItem
        datasetLevelItem.addProblem(problemItem);

        //get each element in the problem element
        Iterator problemIter = element.getChildren().iterator();

        while (problemIter.hasNext()) {
            Element e = (Element)problemIter.next();
            if (e.getName().equals("name")) {
                String problemName = MessageCommons.replaceInvalidChars(e.getTextTrim());
                if (problemName.length() > XMLConstants.PROBLEM_NAME_LENGTH) {
                    problemName = problemName.substring(0, XMLConstants.PROBLEM_NAME_LENGTH);
                    logger.warn("Truncating problem name to [" + problemName + "]");
                }
                problemItem.setProblemName(problemName);
            } else if (e.getName().equals("context")) {
                //Note database column is description and XML has context.
                problemItem.setProblemDescription(MessageCommons.replaceInvalidChars(e.getTextTrim()));
            }
        }

        //set the problem properties
        String tutorFlag = MessageCommons.replaceInvalidChars(element.getAttributeValue("tutorFlag"));
        String other = MessageCommons.replaceInvalidChars(element.getAttributeValue("other"));

        if (tutorFlag != null && ProblemItem.isValidTutorFlag(tutorFlag)) {
            problemItem.setTutorFlag(tutorFlag);
            if (tutorFlag.equals(ProblemItem.TUTOR_FLAG_OTHER)
                    && other != null) {
                problemItem.setTutorOther(other);
            }
        }
    }

    /**Add a skill to the context message object.
     * @param skillElement the skill element from XML
     * @param contextMessage the context message object to which the skill should be added.
     */
    protected void handleSkillElement(Element skillElement,
                                            ContextMessage contextMessage) {
        String content = skillElement.getTextTrim();
        String name = "";
        String category = "";
        ArrayList models = new ArrayList();

        //get each element from XML
        Iterator edIter = skillElement.getChildren().iterator();

        while (edIter.hasNext()) {
            Element element = (Element)edIter.next();
            String elementName = element.getName();

            if (elementName.equals("name")) {
                name = MessageCommons.replaceInvalidChars(element.getTextTrim());
            } else if (elementName.equals("category")) {
                category = MessageCommons.replaceInvalidChars(element.getTextTrim());
            } else if (elementName.equals("model_name")) {
                String temp = MessageCommons.replaceInvalidChars(element.getTextTrim());
                if (temp != null && !temp.equals("")) {
                    models.add(temp);
                }
            }
        }

        if ((name == null || name.equals(""))
            && (content != null && !content.equals(""))) {
            int index = content.indexOf(" ");
            if (index == -1) {
                name = MessageCommons.replaceInvalidChars(content);
            } else {
                name = MessageCommons.replaceInvalidChars(content.substring(0, index));
            }
        }
        if ((category == null || category.equals(""))
                && (content != null && !content.equals(""))) {
                int index = content.indexOf(" ");
                if (index != -1) {
                    category = MessageCommons.replaceInvalidChars(content.substring(index + 1, content.length()));
                }
        }

        if (models.size() == 0) {
            SkillItem si = new SkillItem();
            //need the default skill model
            SkillModelItem smi = new SkillModelItem();
            smi.setSkillModelName(SkillModelItem.DEFAULT_NAME);
            smi.setGlobalFlag(Boolean.TRUE);
            smi.addSkill(si);
            si.setSkillModel(smi);
            contextMessage.addSkill(si);
            if (name != null && !name.equals("")) {
                si.setSkillName(name);
            }
            if (category != null && !category.equals("")) {
                si.setCategory(category);
            }
        } else {
            for (int i = 0; i < models.size(); i++) {
                SkillItem si = new SkillItem();
                SkillModelItem smi = new SkillModelItem();
                smi.setSkillModelName((String)models.get(i));
                smi.setGlobalFlag(Boolean.TRUE);
                smi.addSkill(si);
                si.setSkillModel(smi);
                contextMessage.addSkill(si);
                if (name != null && !name.equals("")) {
                    si.setSkillName(name);
                }
                if (category != null && !category.equals("")) {
                    si.setCategory(category);
                }
            }
        }

        //TODO Handle the probability attribute of the skill element.
    }
}

