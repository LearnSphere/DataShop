/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.webservices;

import java.io.Writer;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.LogUtils;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Turns DTOs into XML to be delivered through web services.
 * @author jimbokun
 * @version $Revision: 15477 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 09:36:33 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WebServiceXMLMessage {
    /** location of the message schema */
    private static URL schemaURL;
    /** factory for creating a schema instance */
    private static SchemaFactory schemaFactory;

    /** string represents QA environment */
    static final String QA_ENV = "QA";
    /** string represents production environment */
    static final String PROD_ENV = "PROD";
    /** string represents local environment*/
    static final String LOCAL_ENV = "LOCAL";
    /** string represents demo environment */
    static final String DEMO_ENV = "DEMO";
    /** string represents demo environment */
    static final String FOSSIL_ENV = "FOSSIL";

    /** string represents QA environment */
    static final String QA_SERVER_NAME = "pslc-qa";
    /** string represents production environment */
    static final String PROD_SERVER_NAME = "pslcdatashop";
    /** string represents local environment*/
    static final String LOCAL_SERVER_NAME = "localhost";
    /** string represents demo environment*/
    static final String DEMO_SERVER_NAME = "were-rabbit";
    /** string represents fossil environment*/
    static final String FOSSIL_SERVER_NAME = "fossil";

    /** QA location of the message schema. */
    private static final String QA_URL
        = "http://pslc-qa.andrew.cmu.edu/api/pslc_datashop_message.xsd";
    /** PROD location of the message schema. */
    private static final String PROD_URL
        = "http://pslcdatashop.org/api/pslc_datashop_message.xsd";
    /** LOCAL location of the message schema. */
    private static final String LOCAL_URL
        = "http://localhost:8080/pslc_datashop_message.xsd";
    /** DEMO location of the message schema. */
    private static final String DEMO_URL
        = "http://were-rabbit.pslc.cs.cmu.edu:8080/pslc_datashop_message.xsd";
    /** FOSSIL location of the message schema. */
    private static final String FOSSIL_URL
        = "http://fossil.andrew.cmu.edu/api/pslc_datashop_message.xsd";

    /** format for web service XML dates */
    private static final FastDateFormat WS_DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** the pslc_datashop_message document */
    private Document doc;
    /** transforms XML into text */
    private Transformer transformer;

    /**
     * Initialize schemaURL and schemaFactory.  Maybe set to null if URL is malformed.
     * @param environment a string which indicates whether running on production, QA or locally
     */
    public static void initializeSchema(String environment) {
        try {
            if (environment.equals(QA_ENV)) {
                schemaURL = new URL(QA_URL);
            } else if (environment.equals(PROD_ENV)) {
                schemaURL = new URL(PROD_URL);
            } else if (environment.equals(LOCAL_ENV)) {
                schemaURL = new URL(LOCAL_URL);
            } else if (environment.equals(DEMO_ENV)) {
                schemaURL = new URL(DEMO_URL);
            } else if (environment.equals(FOSSIL_ENV)) {
                schemaURL = new URL(FOSSIL_URL);
            } else if (DataShopInstance.getMasterSchema() != null) {
                schemaURL = new URL(DataShopInstance.getMasterSchema());
            }
            
            schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        } catch (MalformedURLException e) {
            schemaURL = null;
            schemaFactory = null;
        }
    }
    /**
     * Initialize the message document and transformer.
     * @param environment a string which indicates whether running on production, QA or locally
     * @throws Exception lots of exceptions thrown, need to bail out gracefully in the caller
     */
    public WebServiceXMLMessage(String environment) throws Exception {
        logDebug("environment: " + environment);
        initializeSchema(environment);
        logDebug("schemaURL: " + ((schemaURL == null) ? "null" : schemaURL.toString()));
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("pslc_datashop_message"));
        setResultCode(0);
        setResultMessage("Success.");
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(INDENT, "yes");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
    }
    
    /**
     * Initialize the message document and transformer.
     * @param environment a string which indicates whether running on production, QA or locally
     * @throws Exception lots of exceptions thrown, need to bail out gracefully in the caller
     */
    public WebServiceXMLMessage(String environment, String topElementTagName) throws Exception {
        logDebug("environment: " + environment);
        initializeSchema(environment);
        logDebug("schemaURL: " + ((schemaURL == null) ? "null" : schemaURL.toString()));
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement(topElementTagName));
        setResultCode(0);
        setResultMessage("Success.");
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(INDENT, "yes");
        transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
    }

    /**
     * Initialize and add the DTO to this message.
     * @param dto the data transfer object
     * @param environment a string which indicates whether running on production, QA or locally
     * @throws Exception lots of exceptions thrown, need to bail out gracefully in the caller
     */
    public WebServiceXMLMessage(DTO dto, String environment) throws Exception {
        this(environment);
        addDTO(dto);
    }

    /**
     * Translates a camel case Java identifier into a suitable XML tag name.
     * Example:  numberOfTransactions becomes number_of_transactions
     * @param identifier a Java identifier in camel case
     * @return a suitable XML tag for identifier
     */
    public static String decamelize(String identifier) {
        StringBuffer buf = new StringBuffer(identifier.length());

        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);

            if (isLowerCase(ch)) {
                buf.append(ch);
            } else {
                if (i > 0) {
                    buf.append('_');
                }
                buf.append(toLowerCase(ch));
            }
        }

        return buf.toString();
    }

    /**
     * Translates an XML tag name into a camel case Java identifier.
     * Example:  number_of_transactions becomes numberOfTransactions
     * @param xmlTag a suitable XML tag for identifier
     * @return String a Java identifier in camel case
     */
    public static String camelizeTag(String xmlTag) {
        StringBuffer buf = new StringBuffer(xmlTag.length());

        boolean capitalizeNext = false;
        for (int i = 0; i < xmlTag.length(); i++) {
            char ch = xmlTag.charAt(i);

            if (ch == '_') {
                capitalizeNext = true;
                continue;
            }
            if (capitalizeNext) {
                buf.append(toUpperCase(ch));
                capitalizeNext = false;
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }


    /**
     * Date formatted for web service XML.
     * @param d the date
     * @return date formatted for web service XML
     */
    public static String formatWSDate(Date d) { return WS_DATE_FMT.format(d); }


    /**
     * Decamelize the property keys in this DTO properties map into suitable XML tags.
     * @param props the properties map of a DTO
     * @return the properties map with keys translated into XML tags
     */
    private Map<String, Object> xmlProperties(final Map<String, Object> props) {
        return new LinkedHashMap<String, Object>() { {
            for (Map.Entry<String, Object> kv : props.entrySet()) {
                put(decamelize(kv.getKey()), kv.getValue());
            }
        } };
    }

    /**
     * Decamelize the simple class name of this DTO, to get the tag name for the root element
     * of this DTO's XML element.
     * @param dto the DTO
     * @return the decamelized class name
     */
    public static String xmlName(DTO dto) {
        DTO.Properties dtoProps = dto.getClass().getAnnotation(DTO.Properties.class);
        String name = dtoProps == null ? dto.getClass().getSimpleName() : dtoProps.root();

        return decamelize(name);
    }

    /**
     * Create an XML element with the given tag name and the string content as a child text
     * element.
     * @param tag the tag name
     * @param content the text content
     * @return an XML element with the given tag name and the string content as a child text
     * element
     */
    private Element createTextElement(String tag, String content) {
        Element elt = doc.createElement(tag);
        elt.appendChild(doc.createTextNode(content));
        return elt;
    }

    /**
     * Create an element for the tag name and content.
     * If content is a properties map, recursively create a DTO element, otherwise
     * create a text element with the content.
     * @param tag the tag name of the element to create
     * @param content a properties map or some scalar value
     * @return an element for the tag name and content
     */
    private Element createElement(String tag, Object content) {
        if (content instanceof Map) {
            return elementForDTO(tag, (Map<String, Object>)content);
        }
        return createTextElement(tag, format(content));
    }

    /**
     * Return formatted string representation of content for XML output.
     * @param content a scalar object
     * @return formatted string representation of content for XML output
     */
    private String format(Object content) {
        if (content instanceof Date) {
            return formatWSDate((Date)content);
        } else if (content instanceof Boolean) {
            logDebug("found boolean value ", content);
            return (Boolean)content ? "yes" : "no";
        }
        return content.toString();
    }

    /**
     * Create an XML element for the contents of this DTO properties map.
     * @param tag the tag name for the element
     * @param props a DTO's properties map
     * @return an XML element for the contents of this DTO properties map
     */
    private Element elementForDTO(String tag, Map<String, Object> props) {
        Element root = doc.createElement(tag);

        for (Map.Entry<String, Object> kv : xmlProperties(props).entrySet()) {
            String key = kv.getKey();
            Object value = kv.getValue();

            // the id property is an attribute, not a child element
            if (value != null && !"id".equals(key)) {
                if (value instanceof List) {
                    // drop the "s" at the end; what to do for irregular plurals?
                    String singularKey = key.substring(0, key.length() - 1);
                    for (Object o : (List)value) {
                        root.appendChild(createElement(singularKey, o));
                    }
                } else {
                    root.appendChild(createElement(key, value));
                }
            } else if (value != null && "id".equals(key) && !(value instanceof List)) {
                root.setAttribute("id", value.toString());
            }
        }

        return root;
    }

    /**
     * Create an XML element for the contents of this DTO properties map.
     * @param dto the DTO
     * @return an XML element for the contents of this DTO properties map
     */
    private Element elementForDTO(DTO dto) {
        Map<String, Object> props = dto.propertiesMap();
        Element elt = elementForDTO(xmlName(dto), props);
        Object id = props.get("id");

        logDebug("id is ", id);
        // the id property is an attribute, not a child element
        if (id != null) { elt.setAttribute("id", id.toString()); }

        return elt;
    }

    /**
     * Write the text for the XML node to the writer.
     * @param node an XML node
     * @param writer a writer
     * @throws Exception thrown by the transformer
     */
    private void xmlStream(Node node, Writer writer) throws Exception {
        validate();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
    }

    /**
     * Get the text for the XML node as a String.
     * @param node the node
     * @return the text for the XML node as a String
     * @throws Exception thrown by the transformer
     */
    private String xmlString(Node node) throws Exception {
        StringWriter sw = new StringWriter();
        xmlStream(node, sw);
        return sw.toString();
    }

    /** Make sure that the generated XML conforms to the schema. */
    private void validate() {
        try {
            Validator v = schemaFactory.newSchema(schemaURL).newValidator();
            v.validate(new DOMSource(doc));
        } catch (SAXException sax) {
            logInfo("Validation error: ", sax.getMessage());
        } catch (Exception e) {
            logger.error("Some other error while validating.", e);
        }
    }

    /**
     * Set the result code to be displayed in the root element of the message.
     * Defaults to 200 OK.
     * @param result the result code
     */
    public void setResultCode(int result) {
        doc.getDocumentElement().setAttribute("result_code", Integer.toString(result));
    }

    /**
     * Set the result message to be displayed in the root element of the message.
     * Defaults to "Success."
     * @param msg the result message
     */
    public void setResultMessage(String msg) {
        doc.getDocumentElement().setAttribute("result_message", msg);
    }

    /**
     * Set a custom field to be displayed in the root element of the message.
     * @param fieldName the field name
     * @param fieldValue the field value
     */
    public void setCustomField(String fieldName, String fieldValue) {
        doc.getDocumentElement().setAttribute(fieldName, fieldValue);
    }

    /**
     * Get the XML text for this message as a String.
     * @return the XML text for this message as a String
     * @throws Exception thrown by the transformer
     */
    public String xmlString() throws Exception {
        return xmlString(doc.getDocumentElement());
    }

    /**
     * Add a DTO to this message.
     * @param dto the DTO
     */
    public void addDTO(DTO dto) {
        doc.getDocumentElement().appendChild(elementForDTO(dto));
    }

    /**
     * Write the text for this message to the writer.
     * @param writer a writer
     * @throws Exception thrown by the transformer
     */
    public void writeMessage(Writer writer) throws Exception {
        xmlStream(doc.getDocumentElement(), writer);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(Object... args) { LogUtils.logDebug(logger, args); }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logInfo(Object... args) { LogUtils.logInfo(logger, args); }
}
