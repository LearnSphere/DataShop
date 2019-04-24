/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.Iterator;

import org.jdom.Element;

import edu.cmu.pslc.datashop.item.AttemptActionItem;
import edu.cmu.pslc.datashop.item.AttemptInputItem;
import edu.cmu.pslc.datashop.item.AttemptSelectionItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.xml.MessageCommons;

/**
 * Abstract class for the various tool message XML parsers for each version of the
 * tutor message DTD.
 *
 * @author Hui Cheng
 * @version $Revision: 12956 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-02-23 11:31:27 -0500 (Tue, 23 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractToolMessageParser implements ToolMessageParser {

    /** The actual XML data. */
    private String xml;

    /**
     * Constructor.
     * @param xml the XML data
     */
    protected AbstractToolMessageParser(String xml) {
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
     * Get a ToolMessage object from the XML.
     * @return a ToolMessage populated by XML parser.
     */
    public abstract ToolMessage getToolMessage();

    /**Set problemItem of tool message by the passed problem name.
     * @param problemElement the problem_name element from XML
     * @param toolMessage the tool message object that needs to be set with the problem
     */
    protected void handleProblemElement(Element problemElement, ToolMessage toolMessage) {
        String problemName = MessageCommons.replaceInvalidChars(problemElement.getTextTrim());
        if (problemName != null && !problemName.equals("")) {
           ProblemItem problemItem = new ProblemItem();
           problemItem.setProblemName(problemName);
           toolMessage.setProblemItem(problemItem);
        }
    }

    /**Add a event_descriptor to the tool message object.
     * @param eventDescriptorElement the event_descriptor element from XML
     * @param toolMessage the tool message object to which the event descriptor should be added.
     */
    protected void handleEventDescriptorElement(Element eventDescriptorElement,
                                            ToolMessage toolMessage) {
        EventDescriptor eventDesc = new EventDescriptor();
        toolMessage.addEventDescriptor(eventDesc);
        String eventId = MessageCommons.replaceInvalidChars(
            eventDescriptorElement.getAttributeValue("event_id"));
        if (eventId != null && !eventId.equals("")) {
            eventDesc.setEventId(eventId);
        }

        //get each element from XML
        Iterator edIter = eventDescriptorElement.getChildren().iterator();

        while (edIter.hasNext()) {
            Element element = (Element)edIter.next();
            String elementName = element.getName();

            if (elementName.equals("selection")) {
                AttemptSelectionItem si = new AttemptSelectionItem();
                String selection = MessageCommons.replaceInvalidChars(element.getTextTrim());
                si.setSelection(selection);
                String type = element.getAttributeValue("type");
                if (type != null && !type.equals("")) { si.setType(type); }
                String xmlId = element.getAttributeValue("id");
                if (xmlId != null && !xmlId.equals("")) { si.setXmlId(xmlId); }
                eventDesc.addSelection(si);

            } else if (elementName.equals("action")) {
                AttemptActionItem ai = new AttemptActionItem();
                String action = MessageCommons.replaceInvalidChars(element.getTextTrim());
                ai.setAction(action);
                String type = element.getAttributeValue("type");
                if (type != null && !type.equals("")) { ai.setType(type); }
                String xmlId = element.getAttributeValue("id");
                if (xmlId != null && !xmlId.equals("")) { ai.setXmlId(xmlId); }
                eventDesc.addAction(ai);

            } else if (elementName.equals("input")) {
                AttemptInputItem inputItem = new AttemptInputItem();
                String input = MessageCommons.replaceInvalidChars(element.getTextTrim());
                inputItem.setInput(input);
                String type = element.getAttributeValue("type");
                if (type != null && !type.equals("")) { inputItem.setType(type); }
                String xmlId = element.getAttributeValue("id");
                if (xmlId != null && !xmlId.equals("")) { inputItem.setXmlId(xmlId); }
                eventDesc.addInput(inputItem);
            }

        }

    }
}

