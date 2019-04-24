/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.workflows;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.List;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 * Helper class for generating the XSD for a new workflow component.
 *
 * @author Cindy Tipper
 * @version $Revision: 15537 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-10-08 15:49:39 -0400 (Mon, 08 Oct 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SchemaHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger("SchemaHelper.class");

    /** Constant. */
    private static final String SCHEMA_TAG_START =
	"<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\""
        + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
        + " xmlns:ls=\"http://learnsphere.org/ls\" >";

    /** Constant. */
    private static final String SCHEMA_TAG_END = "</xs:schema>";

    /** Constant. */
    private static final String INCLUDE_COMMONS =
        "<xs:include schemaLocation=\"../../CommonSchemas/WorkflowsCommon.xsd\" />";

    /** Constant. */
    private static final String NAMED_COMPLEX_TAG_START = "<xs:complexType name=";
    /** Constant. */
    private static final String COMPLEX_TAG_START = "<xs:complexType>";
    /** Constant. */
    private static final String COMPLEX_TAG_END = "</xs:complexType>";
    /** Constant. */
    private static final String CONTENT_TAG_START = "<xs:complexContent>";
    /** Constant. */
    private static final String CONTENT_TAG_END = "</xs:complexContent>";
    /** Constant. */
    private static final String SEQUENCE_TAG_START = "<xs:sequence>";
    /** Constant. */
    private static final String SEQUENCE_TAG_END = "</xs:sequence>";
    /** Constant. Always? */
    private static final String EXT_TAG_START = "<xs:extension base=\"InputContainer\">";
    /** Constant. */
    private static final String EXT_TAG_END = "</xs:extension>";
    /** Constant. */
    private static final String ANY_TAG_START = "<xs:any ";
    /** Constant. */
    private static final String ALL_TAG_START = "<xs:all>";
    /** Constant. */
    private static final String ALL_TAG_END = "</xs:all>";
    /** Constant. */
    private static final String CHOICE_TAG_START = "<xs:choice>";
    /** Constant. */
    private static final String CHOICE_TAG_END = "</xs:choice>";
    /** Constant. */
    private static final String ELE_TAG_START = "<xs:element ";
    /** Constant. */
    private static final String ELE_TAG_END = "</xs:element>";
    /** Constant. */
    private static final String TAG_END = " />";
    /** Constant. */
    private static final String SIMPLE_TAG_START = "<xs:simpleType ";
    /** Constant. */
    private static final String SIMPLE_TAG_END = "</xs:simpleType>";
    /** Constant. */
    private static final String FINAL_RESTRICTION = " final=\"restriction\">";
    /** Constant. */
    private static final String RESTRICTION_TAG_START = "<xs:restriction base=\"xs:string\">";
    /** Constant. */
    private static final String RESTRICTION_TAG_END = "</xs:restriction>";
    /** Constant. */
    private static final String ENUM_TAG_START = "<xs:enumeration ";
    /** Constant. */
    private static final String MIN_OCCURS_0 = "minOccurs=\"0\"";
    /** Constant. */
    private static final String MIN_OCCURS_1 = "minOccurs=\"1\"";
    /** Constant. */
    private static final String MAX_OCCURS_1 = "maxOccurs=\"1\"";
    /** Constant. */
    private static final String MAX_OCCURS_NONE = "maxOccurs=\"unbounded\"";
    /** Constant. */
    private static final String CHOICE_TAG_UNBOUNDED_START
        = "<xs:choice " + MIN_OCCURS_0 + " " + MAX_OCCURS_NONE + ">";
    /** Constant */
    private static final String PREFIX_IN = "In";
    /** Constant */
    private static final String PREFIX_OUT = "Out";

    /** Constructor. */
    private SchemaHelper() {
    }

    /**
     * Method to create component schema given input, output and option definitions.
     *
     * @param inputs List of ComponentIO objects
     * @param outputs List of ComponentIO objects
     * @param options List of ComponentOption objects
     * @return String contents of XSD
     */
    public static String createXsd(List<ComponentIO> inputs,
                                   List<ComponentIO> outputs,
                                   List<ComponentOption> options)
    {
        StringBuffer sb = new StringBuffer(SCHEMA_TAG_START);
        sb.append(INCLUDE_COMMONS);

        int count = 0;
        for (ComponentIO input : inputs) {
            sb.append(generateDefinition(PREFIX_IN, count));
            sb.append(generateFileList(PREFIX_IN, input));
            count++;
        }
        sb.append(generateType(PREFIX_IN, inputs));
        sb.append(generateInputLabel(inputs));

        count = 0;
        for (ComponentIO output : outputs) {
            sb.append(generateDefinition(PREFIX_OUT, count));
            sb.append(generateFileList(PREFIX_OUT, output));
            count++;
        }
        sb.append(generateType(PREFIX_OUT, outputs));

        sb.append(generateOptions(options));

        sb.append(generateComponent());

        sb.append(SCHEMA_TAG_END);
        return prettyPrint(sb.toString());
    }

    private static String generateDefinition(String prefix, int index) {
        StringBuffer sb = new StringBuffer(NAMED_COMPLEX_TAG_START);
        sb.append("\"").append(prefix).append("putDefinition").append(index).append("\">");
        sb.append(CONTENT_TAG_START);
        sb.append(EXT_TAG_START);
        sb.append(SEQUENCE_TAG_START);
        sb.append(ELE_TAG_START)
            .append("name=\"files\" type=\"").append(prefix).append("FileList").append(index).append("\"");
        sb.append(TAG_END);
        sb.append(ANY_TAG_START);
        // First input is required.
        if ((prefix.equals(PREFIX_IN)) && (index == 0)) {
            sb.append(MIN_OCCURS_1);
        } else {
            sb.append(MIN_OCCURS_0);
        }
        sb.append(" processContents=\"skip\" ").append(MAX_OCCURS_NONE);
        sb.append(TAG_END);
        sb.append(SEQUENCE_TAG_END);
        sb.append(EXT_TAG_END);
        sb.append(CONTENT_TAG_END);
        sb.append(COMPLEX_TAG_END);
        return sb.toString();
    }

    private static String generateType(String prefix, List<ComponentIO> ioList) {
        int count = ioList.size();
        StringBuffer sb = new StringBuffer(NAMED_COMPLEX_TAG_START);
        sb.append("\"").append(prefix).append("putType\">");
        sb.append(SEQUENCE_TAG_START);
        for (int i = 0; i < count; i++) {
            sb.append(ELE_TAG_START)
                .append("name=\"").append(prefix.toLowerCase()).append("put").append(i)
                .append("\" type=\"").append(prefix).append("putDefinition").append(i).append("\" ");
            ComponentIO compIo = ioList.get(i);
            if (compIo != null) {
                sb.append(prefix.equals(PREFIX_IN) ? " minOccurs=\"" + compIo.getMinOccurs() : "")
                    .append(prefix.equals(PREFIX_IN) ? "\" maxOccurs=\"" + compIo.getMaxOccurs() + "\" " : "");
            }
            sb.append(TAG_END);
        }
        sb.append(SEQUENCE_TAG_END);
        sb.append(COMPLEX_TAG_END);
        return sb.toString();
    }

    private static String generateInputLabel(List<ComponentIO> inputs) {
        StringBuffer sb = new StringBuffer(NAMED_COMPLEX_TAG_START);
        sb.append("\"InputLabel\">");
        sb.append(ALL_TAG_START);
        int i = 0;
        for (ComponentIO in : inputs) {
            sb.append(ELE_TAG_START)
                .append("name=\"input").append(i).append("\" ")
                .append("type=\"xs:string\" ")
                .append("default=\"").append(in.getType()).append("\" ");
            sb.append(MIN_OCCURS_0);
            sb.append(TAG_END);
            i++;
        }
        sb.append(ALL_TAG_END);
        sb.append(COMPLEX_TAG_END);
        return sb.toString();
    }

    private static String generateFileList(String prefix, ComponentIO cio) {
        StringBuffer sb = new StringBuffer(NAMED_COMPLEX_TAG_START);
        sb.append("\"")
            .append(prefix).append("FileList").append(cio.getIndex())
            .append("\">");
        sb.append(CHOICE_TAG_START);
        sb.append(ELE_TAG_START).append("ref=\"");
        // Has to be 'file' for Inputs.
        if (prefix.equals(PREFIX_IN)) {
            sb.append("file");
        } else {
            sb.append(cio.getType());
        }
        sb.append("\" ");
        if (prefix.equals(PREFIX_IN)) { sb.append(MIN_OCCURS_0); }
        sb.append(TAG_END);
        sb.append(CHOICE_TAG_END);
        sb.append(COMPLEX_TAG_END);
        return sb.toString();
    }

    /** Constant. */
    private static final String ENUM = "Enum";

    private static String generateOptions(List<ComponentOption> options) {
        StringBuffer sb = new StringBuffer();

        // If any of the options is of type Enum, declare types first.
        for (ComponentOption co : options) {
            if (co.getType().startsWith(ENUM)) {
                sb.append(generateEnumType(co));
            }
        }

        sb.append(NAMED_COMPLEX_TAG_START);
        sb.append("\"OptionsType\">");
        sb.append(CHOICE_TAG_UNBOUNDED_START);
        
        for (ComponentOption co : options) {
            String optType = co.getType();
            if (optType.startsWith(ENUM)) {
                optType = co.getName() + "Type";
            }
            sb.append(ELE_TAG_START)
                .append("name=\"").append(co.getName())
                .append("\" id=\"").append(co.getId())
                .append("\" type=\"").append(optType)
                .append("\" default=\"").append(co.getDefault())
                .append(optType.endsWith("FileInputHeader") ? "\" ls:inputNodeIndex=\"" : "")
                .append(optType.endsWith("FileInputHeader") ? co.getInputNodeIndex() : "")
                .append(optType.endsWith("FileInputHeader") ? "\" ls:inputFileIndex=\"" : "")
                .append(optType.endsWith("FileInputHeader") ?  co.getInputFileIndex() : "")
                .append("\"");
            sb.append(TAG_END);
        }
        sb.append(CHOICE_TAG_END);
        sb.append(COMPLEX_TAG_END);
        return sb.toString();
    }

    /* Parse enum definition. Of the form: Enum(Foo, Bar, Blah) */
    private static String generateEnumType(ComponentOption co) {
        StringBuffer sb = new StringBuffer();
        sb.append(SIMPLE_TAG_START)
            .append("name=\"")
            .append(co.getName()).append("Type")
            .append("\"")
            .append(FINAL_RESTRICTION);
        sb.append(RESTRICTION_TAG_START);
        String tmpStr = co.getType();
        int index1 = tmpStr.indexOf("(") + 1;
        int index2 = tmpStr.indexOf(")");
        String enumStr = tmpStr.substring(index1, index2);
        String[] values = enumStr.split(",");

        for (String s : values) {
            sb.append(ENUM_TAG_START)
                .append("value=\"")
                .append(s.trim())
                .append("\"")
                .append(TAG_END);
        }

        sb.append(RESTRICTION_TAG_END);
        sb.append(SIMPLE_TAG_END);
        return sb.toString();
    }

    private static String generateComponent() {
        StringBuffer sb = new StringBuffer(ELE_TAG_START);
        sb.append("name=\"component\">");
        sb.append(COMPLEX_TAG_START);
        sb.append(ALL_TAG_START);
        sb.append(ELE_TAG_START)
            .append("name=\"workflow_id\" type=\"xs:integer\"").append(TAG_END);
        sb.append(ELE_TAG_START)
            .append("name=\"component_id\" type=\"xs:string\"").append(TAG_END);
        sb.append(ELE_TAG_START)
            .append("name=\"component_id_human\" type=\"xs:string\"").append(TAG_END);
        sb.append(ELE_TAG_START)
            .append("name=\"component_name\" type=\"xs:string\"").append(TAG_END);
        sb.append(ELE_TAG_START)
            .append("name=\"component_type\" type=\"xs:string\"").append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"left\" type=\"xs:double\"").append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"top\" type=\"xs:double\"").append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"connections\" ")
            .append(MIN_OCCURS_0).append(" ").append(MAX_OCCURS_1)
            .append(" type=\"ConnectionType\"").append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"inputs\" type=\"InputType\" ")
            .append(MIN_OCCURS_0).append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"inputLabels\" type=\"InputLabel\" ")
            .append(MIN_OCCURS_0).append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"outputs\" type=\"OutputType\" ")
            .append(MIN_OCCURS_0).append(TAG_END);
        sb.append(ELE_TAG_START).append("name=\"options\" type=\"OptionsType\" ")
            .append(MIN_OCCURS_0).append(TAG_END);
        sb.append(ALL_TAG_END);
        sb.append(COMPLEX_TAG_END);
        sb.append(ELE_TAG_END);
        return sb.toString();
    }

   /* This method resulted in  error during the transform step 
    public static String prettyPrint(String inputXml) {

        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(inputXml)));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(document);
            StringWriter strWriter = new StringWriter();
            StreamResult result = new StreamResult(strWriter);
            
            transformer.transform(source, result);
            
            return strWriter.getBuffer().toString();
        } catch (Exception e) {
            // Failure to format returns original xml.
            logger.error("Failed to format inputXml: " + e.toString());
        }

        return inputXml;
    }*/
    @SuppressWarnings( "deprecation" )
    public static String prettyPrint(String inputXml) {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(inputXml)));

            org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat(document);
            format.setIndenting(true);
            format.setIndent(2);
            format.setLineWidth(Integer.MAX_VALUE);
            StringWriter outxml = new StringWriter();
            org.apache.xml.serialize.XMLSerializer serializer = new org.apache.xml.serialize.XMLSerializer(outxml, format);
            serializer.serialize(document);

            return outxml.toString();
        } catch (Exception e) {
            // Failure to format returns original xml.
            logger.error("Failed to format inputXml: " + e.toString());
        }
        // Was unable to transform the inputXml, so return the original format
        return inputXml;
    }
}

