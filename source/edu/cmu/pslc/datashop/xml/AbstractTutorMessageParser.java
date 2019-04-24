/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Element;

import edu.cmu.pslc.datashop.item.ActionItem;
import edu.cmu.pslc.datashop.item.InputItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.item.SelectionItem;
import edu.cmu.pslc.datashop.xml.MessageCommons;

/**
 * Abstract class for the various tutor message XML parsers for each version of the
 * tutor message DTD.
 *
 * @author Hui Cheng
 * @version $Revision: 12083 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:55:45 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractTutorMessageParser implements TutorMessageParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The actual XML data. */
    private String xml;

    /**
     * Constructor.
     * @param xml the XML data
     */
    protected AbstractTutorMessageParser(String xml) {
        this.xml = xml;
    }

    /**
     * Returns the XML data.
     * @return the XML data
     */
    protected String getXml() {
        return this.xml;
    }

    /**
     * Get a TutorMessage object from the XML.
     * @return a TutorMessage populated by XML parser.
     */
    public abstract TutorMessage getTutorMessage();

    /**Set problemItem of tutor message.
     * @param problemElement the problem_name element from XML
     * @param tutorMessage the tutor message object that needs to be set with the problem
     */
    protected void handleProblemElement(Element problemElement, TutorMessage tutorMessage) {
        String problemName = MessageCommons.replaceInvalidChars(problemElement.getTextTrim());
        if (problemName != null && !problemName.equals("")) {
           ProblemItem problemItem = new ProblemItem();
           problemItem.setProblemName(problemName);
           tutorMessage.setProblemItem(problemItem);
        }
    }

    /**Add a tutor advice of tutor message.
     * @param tutorAdviceElement the tutor_advice element from XML
     * @param tutorMessage the tutor message object that needs to be set with tutor advice.
     */
    protected void handleTutorAdviceElement(Element tutorAdviceElement, TutorMessage tutorMessage) {
        String advice = tutorAdviceElement.getTextTrim();
        if (advice != null && !advice.equals("")) {
           tutorMessage.addTutorAdvices(MessageCommons.replaceInvalidChars(advice));
        }
    }

    /**Add a event_descriptor to the tutor message object.
     * @param eventDescriptorElement the event_descriptor element from XML
     * @param tutorMessage the tutor message object to which the event descriptor should be added.
     */
    protected void handleEventDescriptorElement(Element eventDescriptorElement,
                                            TutorMessage tutorMessage) {
        if (tutorMessage.getEventDescriptors().size() > 0) {
            logger.warn("Multiple event descriptors found.  "
                    + "Ignoring subsequent ones for context id: " + tutorMessage.getContextId());
            return;
        }

        EventDescriptor eventDesc = new EventDescriptor();
        tutorMessage.addEventDescriptor(eventDesc);
        String eventId = eventDescriptorElement.getAttributeValue("event_id");
        if (eventId != null && !eventId.equals("")) {
            eventDesc.setEventId(eventId);
        }

        //get each element from XML
        Iterator edIter = eventDescriptorElement.getChildren().iterator();

        while (edIter.hasNext()) {
            Element element = (Element)edIter.next();
            String elementName = element.getName();

            if (elementName.equals("selection")) {
                SelectionItem si = new SelectionItem();
                String selection = MessageCommons.replaceInvalidChars(element.getTextTrim());
                si.setSelection(selection);
                String type = element.getAttributeValue("type");
                if (type != null && !type.equals("")) { si.setType(type); }
                String xmlId = element.getAttributeValue("id");
                if (xmlId != null && !xmlId.equals("")) { si.setXmlId(xmlId); }
                eventDesc.addSelection(si);
            } else if (elementName.equals("action")) {
                ActionItem ai = new ActionItem();
                String action = MessageCommons.replaceInvalidChars(element.getTextTrim());
                ai.setAction(action);
                String type = element.getAttributeValue("type");
                if (type != null && !type.equals("")) { ai.setType(type); }
                String xmlId = element.getAttributeValue("id");
                if (xmlId != null && !xmlId.equals("")) { ai.setXmlId(xmlId); }
                eventDesc.addAction(ai);
            } else if (elementName.equals("input")) {
                InputItem ii = new InputItem();
                String input = MessageCommons.replaceInvalidChars(element.getTextTrim());
                ii.setInput(input);
                eventDesc.addInput(ii);
            }

        }

    }

}

