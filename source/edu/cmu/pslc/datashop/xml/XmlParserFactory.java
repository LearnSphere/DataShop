/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.cmu.pslc.datashop.xml.xml2.XmlParser2;
import edu.cmu.pslc.datashop.xml.xml4.XmlParser4;
import edu.cmu.pslc.logging.Message;

/**
 * Create the correct parser by checking the version number in the XML.
 * This is a singleton.
 *
 * @author Alida Skogsholm
 * @version $Revision: 3318 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-09-08 11:41:26 -0400 (Fri, 08 Sep 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class XmlParserFactory {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The one and only instance of this factory. */
    private static final XmlParserFactory INSTANCE = new XmlParserFactory();

    /**
     * Get the instance of this factory.
     * @return an instance of this class
     */
    public static XmlParserFactory getInstance() {
        return INSTANCE;
    }

    /** Constant. */
    private static final int MAX_XML_DEBUG_OUTPUT = 300;

    /**
     * Create a XML parser from a string.
     * @param xmlString the XML string
     * @return a XML parser
     */
    public XmlParser get(String xmlString) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document xmlDocument = builder.build(new StringReader(xmlString));
            return get(xmlDocument);
        } catch (JDOMException exception) {
            logger.warn("JDOMException occurred. " + exception.getMessage());
        } catch (IOException exception) {
            logger.warn("IOException occurred. " + exception.getMessage());
        }
        return null;
    }

    /**
     * Create a XML parser from an XML document.
     * @param xmlDocument the actual XML document
     * @return a XML parser
     */
    public XmlParser get(Document xmlDocument) {
        AbstractXmlParser parser = null;

            Element root = xmlDocument.getRootElement();

            String version;

            if (root.getName().equals(Message.MSG_SEQUENCE_ELEMENT)) {
                version = root.getAttributeValue(Message.VERSION_NUMBER_ATTR);

                if (version == null) {
                    logger.warn("XML version NOT found. Assuming version 2.");
                }

                if ((version == null) || (version.equals(XmlParser2.XML_VERSION))) {
                    parser = new XmlParser2(xmlDocument);
                } else if (version.equals(XmlParser4.XML_VERSION)) {
                    parser = new XmlParser4(xmlDocument);
                } else {
                    logger.warn("Invalid XML version found: " + version);
                }

            } else {
                XMLOutputter outputter =
                    new XMLOutputter(Format.getPrettyFormat().setIndent(" ").setOmitEncoding(false).
                                     setOmitDeclaration(false).setLineSeparator("\n"));

                String xmlString = outputter.outputString(xmlDocument);

                int outputSize = MAX_XML_DEBUG_OUTPUT;
                if (xmlString.length() < outputSize) {
                    outputSize = xmlString.length();
                }
                logger.warn("XML data did not contain the proper root element."
                        + " Received " + xmlString.substring(0, outputSize) + "...");
            }
        return parser;
    }
}
