/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml4;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.item.CustomFieldNameValueItem;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.xml.AbstractToolMessageParser;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.datashop.xml.SemanticEvent;
import edu.cmu.pslc.datashop.xml.ToolMessage;
import edu.cmu.pslc.datashop.xml.UiEvent;

/**
 * Parse tool message with DTD v4 XML data.
 *
 * @author Hui Cheng
 * @version $Revision: 15476 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-09-25 13:18:26 -0400 (Tue, 25 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ToolMessageParser4 extends AbstractToolMessageParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     * @param xml the XML data
     */
    public ToolMessageParser4(String xml) {
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
            returnVal.setContextId(root.getAttributeValue("context_message_id"));

            //get each element from XML
            Iterator messageIter = root.getChildren().iterator();

            while (messageIter.hasNext()) {
                Element messageElement = (Element)messageIter.next();
                String elementName = messageElement.getName();

                if (elementName.equals("problem_name")) {
                    // handle problem
                    handleProblemElement(messageElement, returnVal);
                } else if (elementName.equals("semantic_event")) {
                    //handle semantic event
                    handleSemanticEventElement(messageElement, returnVal);
                } else if (elementName.equals("ui_event")) {
                    //handle ui event
                    handleUiEventElement(messageElement, returnVal);
                } else if (elementName.equals("event_descriptor")) {
                    //handle event description
                    handleEventDescriptorElement(messageElement, returnVal);
                } else if (elementName.equals("custom_field")) {
                    //handle custom field
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

    /**Add a semantic_event to the tool message object.
     * @param semanticEventElement the semantic_event element from XML
     * @param toolMessage the tool message object to which the semantic event should be added.
     */
    private void handleSemanticEventElement(Element semanticEventElement,
                                                ToolMessage toolMessage) {
        String name = MessageCommons.replaceInvalidChars(semanticEventElement.getAttributeValue("name"));
        String id = MessageCommons.replaceInvalidChars(semanticEventElement.getAttributeValue("transaction_id"));
        String trigger = MessageCommons.replaceInvalidChars(semanticEventElement.getAttributeValue("trigger"));
        String subtype = MessageCommons.replaceInvalidChars(semanticEventElement.getAttributeValue("subtype"));
        SemanticEvent se = new SemanticEvent();
        if (name != null && !name.equals("")) {
            se.setName(name);
        }
        if (id != null && !id.equals("")) {
            se.setTransactionId(id);
        }
        if (trigger != null && !trigger.equals("")) {
            se.setTrigger(trigger);
        }
        if (subtype != null && !subtype.equals("")) {
            se.setSubtype(subtype);
        }
        toolMessage.addSemanticEvent(se);
    }

    /**Add a ui_event to the tool message object.
     * @param uiEventElement the ui_event element from XML
     * @param toolMessage the tool message object to which the UI event should be added.
     */
    private void handleUiEventElement(Element uiEventElement,
                                      ToolMessage toolMessage) {
        String id = MessageCommons.replaceInvalidChars(uiEventElement.getAttributeValue("id"));
        id = (id == null) ? null : id.trim();
        String name = MessageCommons.replaceInvalidChars(uiEventElement.getAttributeValue("name"));
        name = (name == null) ? null : name.trim();
        String contents = MessageCommons.replaceInvalidChars(uiEventElement.getTextTrim());

        UiEvent uiEvent = new UiEvent();
        if (id != null && !id.equals("")) {
            uiEvent.setId(id);
        }
        if (name != null && !name.equals("")) {
            uiEvent.setName(name);
        }
        if (contents != null && !contents.equals("")) {
            uiEvent.setName(contents);
        }
        toolMessage.addUiEvent(uiEvent);
    }

    /**
     * Construct customFields for Context Message.
     *
     * @param cfElement the custom_field element of the context message
     * @param tm ToolMessage that will be set
     */
    private void handleCustomFieldElement(Element cfElement, ToolMessage tm) {
        String nameVal = MessageCommons.replaceInvalidChars(cfElement.getChildTextTrim("name"));
        String valueVal = MessageCommons.replaceInvalidChars(cfElement.getChildTextTrim("value"));

        // Handle possibility of null values.
        if (nameVal == null) {
            logger.warn("CustomField name is null. Ignoring.");
            return;
        }
        if (valueVal == null) {
            logger.warn("CustomField value is null. Assuming empty string.");
            valueVal = "";
        }

        //set new customFieldNameValueItem
        CustomFieldNameValueItem cfi = new CustomFieldNameValueItem();
        cfi.setName(nameVal);
        cfi.setValue(valueVal);

        tm.addCustomField(cfi);
    }
}

