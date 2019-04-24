/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml2;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.xml.AbstractToolMessageParser;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.ToolMessage;
import edu.cmu.pslc.datashop.xml.SemanticEvent;;

/**
 * Parse tool message with DTD v2 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 12083 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:55:45 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ToolMessageParser2 extends AbstractToolMessageParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param xml the XML data
     */
    public ToolMessageParser2(String xml) {
        super(xml);
    }

    /**
     * Get a ToolMesssage from the XML data.
     * @return a ToolMessage object
     */
    public ToolMessage getToolMessage() {
        ToolMessage returnVal = new ToolMessage();
        try {
            StringReader reader = new StringReader(getXml());

            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(reader);
            Element root = doc.getRootElement();

            //set ToolMessage contextId field
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
                }

            }

        } catch (JDOMException exception) {
            logger.warn("JDOMException occurred. " + exception.getMessage());
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
        }

        return returnVal;
    }

    /**Add a semantic_event to the tool message object.
     * @param semanticEventElement the semantic_event element from XML
     * @param toolMessage the tool message object to which the semantic event should be added.
     */
    private void handleSemanticEventElement(Element semanticEventElement,
                                                ToolMessage toolMessage) {
        String name = MessageCommons.replaceInvalidChars(
                semanticEventElement.getAttributeValue("name"));
        String id = MessageCommons.replaceInvalidChars(
                semanticEventElement.getAttributeValue("id"));
        String semanticEventId = MessageCommons.replaceInvalidChars(
                semanticEventElement.getAttributeValue("semantic_event_id"));
        String trigger = MessageCommons.replaceInvalidChars(
                semanticEventElement.getAttributeValue("trigger"));
        SemanticEvent se = new SemanticEvent();
        if (name != null && !name.equals("")) {
            se.setName(name);
        }
        //use semantic_event_id for transactionId if not null
        //otherwise use id.
        if (semanticEventId != null && !semanticEventId.equals("")) {
            se.setTransactionId(semanticEventId);
        } else if (id != null && !id.equals("")) {
            se.setTransactionId(id);
        }
        if (trigger != null && !trigger.equals("")) {
            se.setTrigger(trigger);
        }
        toolMessage.addSemanticEvent(se);
    }

}

