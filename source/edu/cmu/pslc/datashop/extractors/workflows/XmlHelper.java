/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Helper class for updating the generic info.xml for a new workflow component.
 *
 * @author Cindy Tipper
 * @version $Revision: 14921 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-12 17:00:56 -0400 (Mon, 12 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class XmlHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger("XmlHelper.class");

    /** Constant. */
    private static final String INPUTS_INFO_BLOCK = "%INPUTS_INFO_BLOCK%";
    /** Constant. */
    private static final String OUTPUTS_INFO_BLOCK = "%OUTPUTS_INFO_BLOCK%";
    /** Constant. */
    private static final String OPTIONS_INFO_BLOCK = "%OPTIONS_INFO_BLOCK%";
    /** Constant. */
    private static final String INPUTS_DEFN_BLOCK = "%INPUTS_DEFN_BLOCK%";
    /** Constant. */
    private static final String OPTIONS_DEFN_BLOCK = "%OPTIONS_DEFN_BLOCK%";

    /** Constructor. */
    private XmlHelper() {
    }

    /**
     * Method to update info.xml given input, output and option definitions.
     *
     * @param fileContent contents of template info.xml file
     * @param inputs List of ComponentIO objects
     * @param outputs List of ComponentIO objects
     * @param options List of ComponentOption objects
     * @return String contents of updated info.xml file
     */
    public static String updateInfoXml(String fileContent,
                                       List<ComponentIO> inputs,
                                       List<ComponentIO> outputs,
                                       List<ComponentOption> options)
    {
        String optionsInfo = generateOptionsInfoBlock(options);
        if (optionsInfo != null) {
            fileContent = fileContent.replaceAll(OPTIONS_INFO_BLOCK, optionsInfo);
        }

        String inputInfo = generateInputInfoBlock(inputs);
        if (inputInfo != null) {
            fileContent = fileContent.replaceAll(INPUTS_INFO_BLOCK, inputInfo);
        }

        String outputInfo = generateOutputInfoBlock(outputs);
        if (outputInfo != null) {
            fileContent = fileContent.replaceAll(OUTPUTS_INFO_BLOCK, outputInfo);
        }

        return fileContent;
    }

    /** Constant. */
    private static final String NEW_LINE = "\n";
    /** Constant. */
    private static final String TAB = "\t";
    /** Constant. */
    private static final String BOLD_TAG_START = "<b>";
    /** Constant. */
    private static final String BOLD_TAG_END = "</b>";

    /** Constant. */
    private static final String OPTIONS_TAG_START = "<options>";
    /** Constant. */
    private static final String OPTIONS_TAG_END = "</options>";

    /**
     * Helper method to generate options definition block of code.
     */
    private static String generateOptionsInfoBlock(List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();
        sb.append(OPTIONS_TAG_START);
        for (ComponentOption co : options) {
            sb.append(NEW_LINE);
            sb.append(BOLD_TAG_START);
            sb.append(co.getName())
                .append(" -- type ").append(co.getType());
            sb.append(BOLD_TAG_END);
        }
        sb.append(NEW_LINE);
        sb.append(OPTIONS_TAG_END);

        sb.append(NEW_LINE);
        return sb.toString();
    }

    /** Constant. */
    private static final String INPUTS_TAG_START = "<inputs>";
    /** Constant. */
    private static final String INPUTS_TAG_END = "</inputs>";

    /**
     * Helper method to generate input info block of code.
     */
    private static String generateInputInfoBlock(List<ComponentIO> inputs) {

        StringBuffer sb = new StringBuffer();
        sb.append(INPUTS_TAG_START);
        for (ComponentIO ci : inputs) {
            sb.append(NEW_LINE);
            sb.append(BOLD_TAG_START);
            sb.append(ci.getType());
            sb.append(BOLD_TAG_END);
        }
        sb.append(NEW_LINE);
        sb.append(INPUTS_TAG_END);

        sb.append(NEW_LINE);
        return sb.toString();
    }

    /** Constant. */
    private static final String OUTPUTS_TAG_START = "<outputs>";
    /** Constant. */
    private static final String OUTPUTS_TAG_END = "</outputs>";

    /**
     * Helper method to generate output info block of code.
     */
    private static String generateOutputInfoBlock(List<ComponentIO> outputs) {

        StringBuffer sb = new StringBuffer();
        sb.append(OUTPUTS_TAG_START);
        for (ComponentIO co : outputs) {
            sb.append(NEW_LINE);
            sb.append(BOLD_TAG_START);
            sb.append(co.getType());
            sb.append(BOLD_TAG_END);
        }
        sb.append(NEW_LINE);
        sb.append(OUTPUTS_TAG_END);

        sb.append(NEW_LINE);
        return sb.toString();
    }

    /**
     * Method to update test/components/Component.xml given input and option definitions.
     *
     * @param fileContent contents of template info.xml file
     * @param inputs List of ComponentIO objects
     * @param options List of ComponentOption objects
     * @return String contents of updated xml file
     */
    public static String updateTestXml(String fileContent,
                                       List<ComponentIO> inputs,
                                       List<ComponentOption> options)
    {
        String optionsDefn = generateOptionsDefnBlock(options);
        if (optionsDefn != null) {
            fileContent = fileContent.replaceAll(OPTIONS_DEFN_BLOCK, optionsDefn);
        }

        String inputDefn = generateInputDefnBlock(inputs);
        if (inputDefn != null) {
            fileContent = fileContent.replaceAll(INPUTS_DEFN_BLOCK, inputDefn);
        }

        return fileContent;
    }

    /**
     * Helper method to generate options definition block of code.
     */
    private static String generateOptionsDefnBlock(List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();
        sb.append(OPTIONS_TAG_START);
        for (ComponentOption co : options) {
            sb.append(NEW_LINE);
            sb.append("<").append(co.getName()).append(">");
            sb.append(co.getDefault());
            sb.append("</").append(co.getName()).append(">");
        }
        sb.append(NEW_LINE);
        sb.append(OPTIONS_TAG_END);

        sb.append(NEW_LINE);
        return sb.toString();
    }

    /**
     * Helper method to generate input defn block of code.
     */
    private static String generateInputDefnBlock(List<ComponentIO> inputs) {

        StringBuffer sb = new StringBuffer();
        sb.append(INPUTS_TAG_START);
        for (ComponentIO ci : inputs) {
            sb.append(NEW_LINE);
            sb.append("<input").append(ci.getIndex()).append(">");
            sb.append(NEW_LINE);
            sb.append(dummyInputDefn);
            sb.append(NEW_LINE);
            sb.append("<files>");
            sb.append(NEW_LINE);
            sb.append("<").append(ci.getType()).append(">");
            sb.append(NEW_LINE);
            sb.append("<index>").append(ci.getIndex()).append("</index>");
            sb.append(NEW_LINE);
            sb.append("<file_path>PUT PATH TO TEST INPUT HERE.</file_path>");
            sb.append(NEW_LINE);
            sb.append("<label>text</label>");
            sb.append(NEW_LINE);
            sb.append("<file_name>PUT NAME OF TEST INPUT HERE.</file_name>");
            sb.append(NEW_LINE);
            sb.append("</").append(ci.getType()).append(">");
            sb.append(NEW_LINE);
            sb.append("</files>");
            sb.append(NEW_LINE);
            sb.append("<inputmeta />");
            sb.append(NEW_LINE);
            sb.append("</input").append(ci.getIndex()).append(">");
        }
        sb.append(NEW_LINE);
        sb.append(INPUTS_TAG_END);

        sb.append(NEW_LINE);
        return sb.toString();
    }

    /** Constant. */
    private static final String dummyInputDefn =
        "<component_id>Import-1-x995490</component_id>\n"
        + "<component_id_human>Import #1</component_id_human>\n"
        + "<component_type>import</component_type>\n"
        + "<component_name>Dummy Import Name</component_name>\n"
        + "<elapsed_seconds>0</elapsed_seconds>\n"
        + "<errors />";
}

