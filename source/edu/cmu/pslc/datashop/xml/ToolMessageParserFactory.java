/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.xml;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.xml.xml2.XmlParser2;
import edu.cmu.pslc.datashop.xml.xml4.XmlParser4;
import edu.cmu.pslc.datashop.xml.xml2.ToolMessageParser2;
import edu.cmu.pslc.datashop.xml.xml4.ToolMessageParser4;

import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * Create the correct tool message parser based on version,
 * info and message type of a MessageItem.
 * This is a singleton.
 *
 * @author Hui Cheng
 * @version $Revision: 2111 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-11 10:10:57 -0500 (Wed, 11 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ToolMessageParserFactory {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The one and only instance of this factory. */
    private static final ToolMessageParserFactory INSTANCE = new ToolMessageParserFactory();

    /**
     * Get the instance of this factory.
     * @return an instance of this class
     */
    public static ToolMessageParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Create a tool message XML parser.
     * @param messageItem the messageItem from message table
     * @return a ToolMessageParser parser
     */
    public ToolMessageParser get(MessageItem messageItem) {
        ToolMessageParser parser = null;

        String version = messageItem.getXmlVersion();
        String info = messageItem.getInfo();

        if (version.equals(XmlParser2.XML_VERSION)) {
            parser = new ToolMessageParser2(info);
        } else if (version.equals(XmlParser4.XML_VERSION)) {
            parser = new ToolMessageParser4(info);
        } else {
            logger.warn("Invalid Tool Message: XML version " + version + ";");
        }
        return parser;
    }
}
