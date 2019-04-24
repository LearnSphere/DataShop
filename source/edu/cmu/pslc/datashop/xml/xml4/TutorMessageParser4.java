/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml4;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.xml.AbstractTutorMessageParser;
import edu.cmu.pslc.datashop.xml.ActionEvaluation;
import edu.cmu.pslc.datashop.xml.Interpretation;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.TutorMessage;

/**
 * Parse tutor message with DTD v4 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 15476 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-25 13:18:26 -0400 (Tue, 25 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorMessageParser4 extends AbstractTutorMessageParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param xml the XML data
     */
    public TutorMessageParser4(String xml) {
        super(xml);
    }

    /**
     * Get a ToolMesssage from the XML data.
     * @return a ToolMessage object
     */
    public TutorMessage getTutorMessage() {
        TutorMessage returnVal = new TutorMessage();
        try {
            StringReader reader = new StringReader(getXml());

            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();

            //set TutorMessage contextId field
            //based on the context_message_id attribute of root node.
            returnVal.setContextId(root.getAttributeValue("context_message_id"));

            //get each element from XML
            Iterator messageIter = root.getChildren().iterator();

            while (messageIter.hasNext()) {
                Element messageElement = (Element)messageIter.next();
                String elementName = messageElement.getName();

                //handle problem
                if (elementName.equals("problem_name")) {
                    handleProblemElement(messageElement, returnVal);
                } else if (elementName.equals("semantic_event")) {
                    //handle semantic event
                    handleSemanticEventElement(messageElement, returnVal);
                } else if (elementName.equals("event_descriptor")) {
                    handleEventDescriptorElement(messageElement, returnVal);
                } else if (elementName.equals("action_evaluation")) {
                    handleActionEvaluationElement(messageElement, returnVal);
                } else if (elementName.equals("tutor_advice")) {
                    handleTutorAdviceElement(messageElement, returnVal);
                } else if (elementName.equals("skill")) {
                    handleSkillElement(messageElement, returnVal);
                } else if (elementName.equals("interpretation")) {
                    handleInterpretationElement(messageElement, returnVal);
                } else if (elementName.equals("custom_field")) {
                    handleCustomFieldElement(messageElement, returnVal);
                }

            }

        } catch (JDOMException exception) {
            logger.warn("JDOMException occurred. " + exception.getMessage());
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
        }

        return returnVal;
    }

    /**Add a semantic_event to the tutor message object.
     * @param semanticEventElement the semantic_event element from XML
     * @param tutorMessage the tutor message object to which the semantic event should be added.
     */
    private void handleSemanticEventElement(Element semanticEventElement,
                                                TutorMessage tutorMessage) {
        String name = semanticEventElement.getAttributeValue("name");
        String id = semanticEventElement.getAttributeValue("transaction_id");
        String trigger = semanticEventElement.getAttributeValue("trigger");
        String subtype = semanticEventElement.getAttributeValue("subtype");
        SemanticEvent se = new SemanticEvent();
        if (name != null && !name.equals("")) {
            se.setName(MessageCommons.replaceInvalidChars(name));
        }
        if (id != null && !id.equals("")) {
            se.setTransactionId(MessageCommons.replaceInvalidChars(id));
        }
        if (trigger != null && !trigger.equals("")) {
            se.setTrigger(MessageCommons.replaceInvalidChars(trigger));
        }
        if (subtype != null && !subtype.equals("")) {
            se.setSubtype(MessageCommons.replaceInvalidChars(subtype));
        }
        tutorMessage.addSemanticEvent(se);
    }

    /**
     * Construct customFields for Tutor Message.
     *
     * @param cfElement the custom_field element of the context message
     * @param tm TutorMessage that will be set
     */
    private void handleCustomFieldElement(Element cfElement, TutorMessage tm) {
        String nameVal = cfElement.getChildTextTrim("name");
        String valueVal = cfElement.getChildTextTrim("value");

        // Handle possibility of null values.
        if (nameVal == null) {
            logger.warn("CustomField name is null. Ignoring.");
            return;
        }
        if (valueVal == null) {
            logger.warn("CustomField value is null. Assuming empty string.");
            valueVal = "";
        }

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
        tm.addCustomField(cfi);
    }

    /**Add a action_evaluation to the tutor message object.
     * @param actionEvaluationElement the action_evaluation element from XML
     * @param tutorMessage the tutor message object to which the action_evaluation should be added.
     */
    private void handleActionEvaluationElement(Element actionEvaluationElement,
                                                TutorMessage tutorMessage) {
        String currentHintNumber = actionEvaluationElement.getAttributeValue("current_hint_number");
        String totalHintsAvailable =
            actionEvaluationElement.getAttributeValue("total_hints_available");
        String hintId =
            actionEvaluationElement.getAttributeValue("hint_id");
        String classification =
            actionEvaluationElement.getAttributeValue("classification");
        String content = actionEvaluationElement.getTextTrim();
        ActionEvaluation ae = new ActionEvaluation();
        if (currentHintNumber != null && !currentHintNumber.equals("")) {
            ae.setCurrentHintNumber(currentHintNumber);
        }
        if (totalHintsAvailable != null && !totalHintsAvailable.equals("")) {
            ae.setTotalHintsAvailable(totalHintsAvailable);
        }
        if (hintId != null && !hintId.equals("")) {
            ae.setHintId(MessageCommons.replaceInvalidChars(hintId));
        }
        if (classification != null && !classification.equals("")) {
            ae.setClassification(MessageCommons.replaceInvalidChars(classification));
        }
        if (content != null && !content.equals("")) {
            ae.setContent(MessageCommons.replaceInvalidChars(content));
        }
        tutorMessage.addActionEvaluations(ae);
    }

    /**Add a skill to the tutor message object.
     * @param skillElement the skill element from XML
     * @param tutorMessage the tutor message object to which the skill should be added.
     */
    protected void handleSkillElement(Element skillElement,
                                            TutorMessage tutorMessage) {
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
                name = element.getTextTrim();
                if (name != null) {
                   name = MessageCommons.replaceInvalidChars(name);
                }
            } else if (elementName.equals("category")) {
                category = element.getTextTrim();
                if (category != null) {
                    category = MessageCommons.replaceInvalidChars(category);
                }
            } else if (elementName.equals("model_name")) {
                String temp = element.getTextTrim();
                if (temp != null && !temp.equals("")) {
                    models.add(MessageCommons.replaceInvalidChars(temp));
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
            smi.setGlobalFlag(Boolean.TRUE);
            smi.setModifiedTime(new Date());
            smi.setCreationTime(new Date());
            smi.setSource(SkillModelItem.SOURCE_LOGGED);
            smi.setMappingType(SkillModelItem.MAPPING_CORRECT_TRANS);
            smi.setStatus(SkillModelItem.STATUS_NOT_READY);
            smi.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);
            smi.setSkillModelName(SkillModelItem.DEFAULT_NAME);
            smi.addSkill(si);
            si.setSkillModel(smi);
            tutorMessage.addSkill(si);
            if (name != null && !name.equals("")) {
                si.setSkillName(MessageCommons.replaceInvalidChars(name));
            }
            if (category != null && !category.equals("")) {
                si.setCategory(MessageCommons.replaceInvalidChars(category));
            }
        } else {
            for (int i = 0; i < models.size(); i++) {
                SkillItem si = new SkillItem();
                SkillModelItem smi = new SkillModelItem();
                smi.setGlobalFlag(Boolean.TRUE);
                smi.setModifiedTime(new Date());
                smi.setCreationTime(new Date());
                smi.setSource(SkillModelItem.SOURCE_LOGGED);
                smi.setMappingType(SkillModelItem.MAPPING_CORRECT_TRANS);
                smi.setStatus(SkillModelItem.STATUS_NOT_READY);
                smi.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);
                smi.setSkillModelName((String)models.get(i));
                smi.addSkill(si);
                si.setSkillModel(smi);
                tutorMessage.addSkill(si);
                if (name != null && !name.equals("")) {
                    si.setSkillName(MessageCommons.replaceInvalidChars(name));
                }
                if (category != null && !category.equals("")) {
                    si.setCategory(MessageCommons.replaceInvalidChars(category));
                }
            }
        }

        //TODO Handle the probability attribute of the skill element.
    }

    /**Add an interpretation to the tutor message object.
     * @param interpretationElement the interpretation element from XML
     * @param tutorMessage the tutor message object to which the interpretation should be added.
     */
    protected void handleInterpretationElement(Element interpretationElement,
                                            TutorMessage tutorMessage) {
        String sChosen = interpretationElement.getAttributeValue("chosen");
        Boolean chosen = new Boolean(sChosen);
        Interpretation interpretation = new Interpretation();
        tutorMessage.addInterpretation(interpretation);
        interpretation.setChosen(chosen);
        ArrayList correctSteps = interpretation.getCorrectSteps();
        ArrayList incorrectSteps = interpretation.getIncorrectSteps();

        //get each element from XML
        Iterator edIter = interpretationElement.getChildren().iterator();
        while (edIter.hasNext()) {
            Element element = (Element)edIter.next();
            String elementName = element.getName();

            if (elementName.equals("correct_step_sequence")) {
                handleStepSequenceElement(element, correctSteps);
            } else if (elementName.equals("incorrect_step_sequence")) {
                handleStepSequenceElement(element, incorrectSteps);
            }
        }
    }

    /**Handle the correct_step_sequence and incorrect_step_sequence elements
     * in the interpretation elements of the tutor message.
     * @param sequenceElement the sequence element from XML.
     * @param steps the ArrayList to which the steps should be added.
     */
    protected void handleStepSequenceElement(Element sequenceElement,
                ArrayList steps) {
        //get each element from XML
        Iterator seqIter = sequenceElement.getChildren().iterator();
        while (seqIter.hasNext()) {
            Element stepElement = (Element)seqIter.next();
            CognitiveStepItem step = new CognitiveStepItem();
            steps.add(step);
            handleStepElement(stepElement, step);
        }
    }

    /**Handle the correct_step_sequence and incorrect_step_sequence elements
     * in the interpretation elements of the tutor message.
     * @param stepElement the step element from XML.
     * @param stepItem the CognitiveStepItem to which the step_info and rule should be set/added.
     */
    protected void handleStepElement(Element stepElement,
                CognitiveStepItem stepItem) {
        //get each element from XML
        Iterator stepIter = stepElement.getChildren().iterator();
        while (stepIter.hasNext()) {
            Element element = (Element)stepIter.next();
            String elementName = element.getName();
            String elementText = MessageCommons.replaceInvalidChars(element.getTextTrim());

            if (elementName.equals("step_info")) {
                stepItem.setStepInfo(elementText);
            } else if (elementName.equals("rule")) {
                SkillItem skill = new SkillItem();
                stepItem.addSkill(skill);
                skill.setSkillName(elementText);

                //create a default skill model for rules.
                SkillModelItem smi = new SkillModelItem();
                smi.setSkillModelName(SkillModelItem.DEFAULT_NAME);
                smi.setGlobalFlag(Boolean.TRUE);
                smi.addSkill(skill);
                skill.setSkillModel(smi);
            }
        }
    }

}

