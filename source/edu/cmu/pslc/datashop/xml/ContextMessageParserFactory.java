/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.xml;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.xml.xml2.XmlParser2;
import edu.cmu.pslc.datashop.xml.xml4.XmlParser4;
import edu.cmu.pslc.datashop.xml.xml2.ContextMessageParser2;
import edu.cmu.pslc.datashop.xml.xml4.ContextMessageParser4;

import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * Create the correct context message parser based on version,
 * info and message type of a MessageItem.
 * This is a singleton.
 *
 * @author Hui Cheng
 * @version $Revision: 2101 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-10 16:57:53 -0500 (Tue, 10 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ContextMessageParserFactory {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The one and only instance of this factory. */
    private static final ContextMessageParserFactory INSTANCE = new ContextMessageParserFactory();

    /**
     * Get the instance of this factory.
     * @return an instance of this class
     */
    public static ContextMessageParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Create a context message XML parser.
     * @param messageItem the messageItem from message table
     * @return a ContextMessageParser parser
     */
    public ContextMessageParser get(MessageItem messageItem) {
        ContextMessageParser parser = null;

        String version = messageItem.getXmlVersion();
        String info = messageItem.getInfo();

        if (version.equals(XmlParser2.XML_VERSION)) {
            parser = new ContextMessageParser2(info);
        } else if (version.equals(XmlParser4.XML_VERSION)) {
            parser = new ContextMessageParser4(info);
        } else {
            logger.warn("Invalid Context Message: XML version " + version + "; ");
        }
        return parser;
    }
}
