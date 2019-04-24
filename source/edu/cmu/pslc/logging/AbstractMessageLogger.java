/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

/**
 * The abstract class for the message loggers.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12489 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-05 13:05:34 -0400 (Wed, 05 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractMessageLogger implements MessageLogger {

    /** UTF-8 encoding string. */
    public static final String ENCODING_UTF8 = "UTF-8";
    /** ISO-8859-1 encoding string. */
    public static final String ENCODING_ISO_8859_1 = "ISO-8859-1";

    /** XML header for UTF-8 encoding. */
    private static final String XML_HEADER_UTF8
        = "<?xml version=\"1.0\" encoding=\"" + ENCODING_UTF8 + "\"?>\n";

    /** XML header for ISO-8859-1 encoding. */
    private static final String XML_HEADER_ISO_8859_1
        = "<?xml version=\"1.0\" encoding=\"" + ENCODING_ISO_8859_1 + "\"?>\n";

    /** The root node, tutor related message sequence schema included. */
    private static final String OPEN_ROOT_NODE_WITH_SCHEMA =
              "<tutor_related_message_sequence \n"
            + "    xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' \n"
            + "    xsi:noNamespaceSchemaLocation="
            + "'http://pslcdatashop.org/dtd/tutor_message_v4.xsd' \n"
            + "    version_number=\"4\" > \n";

    /** The root node, tutor related message sequence schema included. */
    private static final String OPEN_ROOT_NODE =
        "<tutor_related_message_sequence version_number=\"4\" > \n";

    /** Closing string for every XML document. */
    protected static final String CLOSE_ROOT_NODE = "</tutor_related_message_sequence>\n";

    /** Logging URL when in production mode. */
    public static final String PROD_URL = "https://learnlab.web.cmu.edu/log";

    /** Logging URL for QA testing. */
    public static final String QA_URL   = "https://pslc-qa.andrew.cmu.edu/log";

    /** Fixed info_type value for all messages: refers to DTD. */
    protected static final String INFO_TYPE = "tutor_message.dtd";

    /** The encoding for this logger. */
    private String encoding = ENCODING_UTF8;

    /**
     * Constructor.
     * @param encoding the encoding to be indicated at the top of the XML
     */
    protected AbstractMessageLogger(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the opening XML string including the XML version, doc type and the root node.
     * @return the opening XML string including the XML version, doc type and the root node
     */
    protected String getOpenXmlSchemaType() {
        if (encoding.equals(ENCODING_UTF8)) {
            return XML_HEADER_UTF8 + OPEN_ROOT_NODE_WITH_SCHEMA;
        }
        return XML_HEADER_ISO_8859_1 + OPEN_ROOT_NODE_WITH_SCHEMA;
    }

    /**
     * Returns the opening XML string including the XML version and the root node,
     * without the doc type.
     * @return the opening XML string including the XML version and the root node
     */
    protected String getOpenXml() {
        if (encoding.equals(ENCODING_UTF8)) {
            return XML_HEADER_UTF8 + OPEN_ROOT_NODE;
        }
        return XML_HEADER_ISO_8859_1 + OPEN_ROOT_NODE;
    }

    /**
     * Returns the closing root node string.
     * @return the closing root node string
     */
    protected String getCloseXml() {
        return CLOSE_ROOT_NODE;
    }
}
