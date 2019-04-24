/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml2;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.xml.AbstractTutorMessageParser;
import edu.cmu.pslc.datashop.xml.ActionEvaluation;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.TutorMessage;

/**
 * Parse tutor message with DTD v2 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 12083 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:55:45 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorMessageParser2 extends AbstractTutorMessageParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param xml the XML data
     */
    public TutorMessageParser2(String xml) {
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
            //based on the attemp_id attribute of root node.
            returnVal.setContextId(root.getAttributeValue("attempt_id"));

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
        String id = semanticEventElement.getAttributeValue("id");
        String semanticEventId = semanticEventElement.getAttributeValue("semantic_event_id");
        String trigger = semanticEventElement.getAttributeValue("trigger");
        SemanticEvent se = new SemanticEvent();
        if (name != null && !name.equals("")) {
            se.setName(MessageCommons.replaceInvalidChars(name));
        }
        //use semantic_event_id for transactionId if not null
        //otherwise use id.
        if (semanticEventId != null && !semanticEventId.equals("")) {
            se.setTransactionId(MessageCommons.replaceInvalidChars(semanticEventId));
        } else if (id != null && !id.equals("")) {
            se.setTransactionId(MessageCommons.replaceInvalidChars(id));
        }
        if (trigger != null && !trigger.equals("")) {
            se.setTrigger(MessageCommons.replaceInvalidChars(trigger));
        }
        tutorMessage.addSemanticEvent(se);
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
        String content = actionEvaluationElement.getTextTrim();
        ActionEvaluation ae = new ActionEvaluation();
        if (currentHintNumber != null && !currentHintNumber.equals("")) {
            ae.setCurrentHintNumber(currentHintNumber);
        }
        if (totalHintsAvailable != null && !totalHintsAvailable.equals("")) {
            ae.setTotalHintsAvailable(totalHintsAvailable);
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
        SkillItem skill = new SkillItem();
        SkillModelItem smi = new SkillModelItem();
        smi.setSkillModelName(SkillModelItem.DEFAULT_NAME);
        smi.setGlobalFlag(Boolean.TRUE);
        smi.addSkill(skill);
        smi.setModifiedTime(new Date());
        smi.setCreationTime(new Date());
        smi.setSource(SkillModelItem.SOURCE_LOGGED);
        smi.setMappingType(SkillModelItem.MAPPING_CORRECT_TRANS);
        smi.setStatus(SkillModelItem.STATUS_NOT_READY);
        smi.setLfaStatus(SkillModelItem.LFA_STATUS_QUEUED);

        skill.setSkillModel(smi);
        if (content != null && !content.equals("")) {
            tutorMessage.addSkill(skill);
            //look for an empty space which divides name and category
            int index = content.indexOf(" ");
            if (index == -1) {
                skill.setSkillName(MessageCommons.replaceInvalidChars(content));
            } else {
                skill.setSkillName(MessageCommons.replaceInvalidChars(content.substring(0, index)));
                skill.setCategory(MessageCommons.replaceInvalidChars(content.substring(index + 1, content.length())));
            }
        }
        //TODO Probability: need to do something with these values
    }
}

