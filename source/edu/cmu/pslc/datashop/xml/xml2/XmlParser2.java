/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml.xml2;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.xml.AbstractXmlParser;
import edu.cmu.pslc.datashop.xml.MessageCommons;
import edu.cmu.pslc.logging.Message;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * Parse the DTD v2 XML data.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12544 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2015-08-18 16:34:49 -0400 (Tue, 18 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class XmlParser2 extends AbstractXmlParser {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * The version of the Tutor Message DTD that this parser can handle.
     */
    public static final String XML_VERSION = "2";

    /**
     * Constructor.
     * @param xml the XML data
     */
    public XmlParser2(Document xml) {
        super(xml);
    }

    /**
     * Get a list of message items from the XML data.
     * @return a list of MessageItem objects
     */
    public List getMessageItems() {
        List list = new ArrayList();

        Document doc = getXmlDocument();
        Element root = doc.getRootElement();

        XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());

        Iterator messageIter = root.getChildren().iterator();

        while (messageIter.hasNext()) {
            Element messageElement = (Element)messageIter.next();

            String messageType = getMessageType(messageElement);
            if (MessageItem.isValidMessageType(messageType)) {
                MessageItem messageItem = new MessageItem();
                messageItem.setUserId(getUserId(messageElement));
                messageItem.setSessionTag(getSessionId(messageElement));

                String timeString  = getTimeString(messageElement);
                String timeZone    = getTimeZone(messageElement);
                
                Date timeStamp     = null;
                if (timeString != null)
                        timeStamp = DateTools.getDate(timeString, timeZone);

                messageItem.setTime(timeStamp);
                messageItem.setTimeZone(timeZone);

                messageItem.setMessageType(messageType);
                messageItem.setContextMessageId(getContextMessageId(messageElement));

                String subXml = outputter.outputString(messageElement);
                messageItem.setInfo(subXml);

                messageItem.setXmlVersion(XML_VERSION);
                messageItem.setTransactionId(getTransactionId(messageElement));

                list.add(messageItem);
            } else {
                logger.info("Ignoring message type " + messageElement.getName()
                        + " for session " + getSessionId(messageElement));
            }

        } // end while loop

        return list;
    }

    /**
     * Return the transaction id of the meta element.
     * Note that this attribute was renamed the transaction_id in version 4.
     * @param element the message element
     * @return the transaction id if it exists, null otherwise
     */
    private String getTransactionId(Element element) {
        if (hasSemanticEventElement(element)) {
            String id = MessageCommons.replaceInvalidChars(
                element.getChild("semantic_event").getAttributeValue("semantic_event_id"));
            if (id == null) {
                return element.getChild("semantic_event").getAttributeValue("id");
            } else {
                return id;
            }
        }
        return null;
    }

    /**
     * Return the transaction id of the meta element.
     * @param element the message element
     * @return the transaction id if it exists, null otherwise
     */
    private String getMessageType(Element element) {
        String elementName = element.getName();

        if (elementName.equals(Message.TOOL_MSG_ELEMENT)) {
            return MessageItem.MSG_TYPE_TOOL;
        } else if (elementName.equals(Message.TUTOR_MSG_ELEMENT)) {
            return MessageItem.MSG_TYPE_TUTOR;
        } else if (elementName.equals(Message.CURRICULUM_MSG_ELEMENT)) {
            return MessageItem.MSG_TYPE_CONTEXT;
        } else if (elementName.equals(Message.MSG_ELEMENT)) {
            return MessageItem.MSG_TYPE_PLAIN_MESSAGE;
        } else {
            logger.warn("Invalid message in XML: " + elementName);
        }
        return null;
    }

    /**
     * Return the attempt id of the given message element.
     * Note that the attempt id was poorly named and is called the
     * context_message_id in version 4.
     * @param element the message element
     * @return the context message id if it exists, null otherwise
     */
    private String getContextMessageId(Element element) {
        return element.getAttributeValue("attempt_id");
    }
}
