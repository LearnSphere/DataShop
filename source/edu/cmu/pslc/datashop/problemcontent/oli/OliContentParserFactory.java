/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.problemcontent.oli;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Create the correct parser by checking the version number in the XML.
 * This is a singleton.
 *
 * @author Mike Komisin
 * @version $Revision: 11005 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-12 11:07:08 -0400 (Mon, 12 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliContentParserFactory {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The one and only instance of this factory. */
    private static final OliContentParserFactory INSTANCE = new OliContentParserFactory();

    /**
     * Get the instance of this factory.
     * @return an instance of this class
     */
    public static OliContentParserFactory getInstance() {
        return INSTANCE;
    }

    /** Constant. */
    private static final int MAX_XML_DEBUG_OUTPUT = 300;

    /**
     * Create a XML parser from a string.
     * @param xmlString the XML string
     * @param file the File object of the XML document
     * @param validWorkbooks an optional list of workbooks verified to be valid
     * within the context of an organization
     * @return a XML parser
     */
    public OliContentParser get(String xmlString, File file, List validWorkbooks) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document xmlDocument = builder.build(new StringReader(xmlString));
            return get(xmlDocument, file, validWorkbooks);
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
     * @param file the File object of the XML document
     * @param validWorkbooks an optional list of workbooks verified to be valid
     * within the context of an organization
     * @return a XML parser
     */
    public OliContentParser get(Document xmlDocument, File file, List validWorkbooks) {
        return new OliContentParser(xmlDocument, file, validWorkbooks);
    }


}
