/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.xml;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.xml.xml2.XmlParser2;
import edu.cmu.pslc.datashop.xml.xml4.XmlParser4;
import edu.cmu.pslc.datashop.xml.xml2.TutorMessageParser2;
import edu.cmu.pslc.datashop.xml.xml4.TutorMessageParser4;

import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * Create the correct tutor message parser based on version,
 * info and message type of a MessageItem.
 * This is a singleton.
 *
 * @author Hui Cheng
 * @version $Revision: 2152 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2006-01-20 13:56:50 -0500 (Fri, 20 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class TutorMessageParserFactory {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The one and only instance of this factory. */
    private static final TutorMessageParserFactory INSTANCE = new TutorMessageParserFactory();

    /**
     * Get the instance of this factory.
     * @return an instance of this class
     */
    public static TutorMessageParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Create a tool message XML parser.
     * @param messageItem the messageItem from message table
     * @return a TutorMessageParser parser
     */
    public TutorMessageParser get(MessageItem messageItem) {
        TutorMessageParser parser = null;

        String version = messageItem.getXmlVersion();
        String info = messageItem.getInfo();

        if (version.equals(XmlParser2.XML_VERSION)) {
            parser = new TutorMessageParser2(info);
        } else if (version.equals(XmlParser4.XML_VERSION)) {
            parser = new TutorMessageParser4(info);
        } else {
            logger.warn("Invalid Tutor Message: XML version " + version + ";");
        }
        return parser;
    }
}
