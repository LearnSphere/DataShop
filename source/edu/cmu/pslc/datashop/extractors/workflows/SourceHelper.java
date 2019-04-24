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
 * Helper class for updating the generic Java source for a new workflow component.
 *
 * @author Cindy Tipper
 * @version $Revision: 15067 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-04-18 16:22:29 -0400 (Wed, 18 Apr 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SourceHelper {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger("SourceHelper.class");

    /** Constant. */
    private static final String RUN_EXTERNAL_BLOCK = "%RUN_EXTERNAL_BLOCK%";
    /** Constant. */
    private static final String OPTIONS_DEFN_BLOCK = "%OPTIONS_DEFN_BLOCK%";
    /** Constant. */
    private static final String PROCESS_OPTIONS_BLOCK = "%PROCESS_OPTIONS_BLOCK%";
    /** Constant. */
    private static final String PARSE_OPTIONS_BLOCK = "%PARSE_OPTIONS_BLOCK%";
    /** Constant. */
    private static final String INPUT_DEFN_BLOCK = "%INPUT_DEFN_BLOCK%";
    /** Constant. */
    private static final String OUTPUT_DEFN_BLOCK = "%OUTPUT_DEFN_BLOCK%";
    /** Constant. */
    private static final String OUTPUT_USAGE_BLOCK = "%OUTPUT_USAGE_BLOCK%";

    /** Constructor. */
    private SourceHelper() {
    }

    /**
     * Method to update component source given input, output and option definitions.
     *
     * @param fileContent contents of template Java file
     * @param inputs List of ComponentIO objects
     * @param outputs List of ComponentIO objects
     * @param options List of ComponentOption objects
     * @return String contents of updated Java file
     */
    public static String updateSource(String fileContent,
                                      List<ComponentIO> inputs,
                                      List<ComponentIO> outputs,
                                      List<ComponentOption> options,
                                      Boolean isExternal)
    {
        String runExternalDefn = generateRunExternalDefnBlock(outputs);
        if (runExternalDefn != null) {
            fileContent = fileContent.replaceAll(RUN_EXTERNAL_BLOCK, runExternalDefn);
        }

        String optionsDefn = generateOptionsDefnBlock(inputs,
                                                      outputs,
                                                      options);
        if (optionsDefn != null) {
            fileContent = fileContent.replaceAll(OPTIONS_DEFN_BLOCK, optionsDefn);
        }

        String processOptions = generateProcessOptionsBlock(inputs,
                                                            outputs,
                                                            options);
        if (processOptions != null) {
            fileContent = fileContent.replaceAll(PROCESS_OPTIONS_BLOCK, processOptions);
        }

        String parseOptions = generateParseOptionsBlock(inputs,
                                                        outputs,
                                                        options);
        if (parseOptions != null) {
            fileContent = fileContent.replace(PARSE_OPTIONS_BLOCK, parseOptions);
        }

        String inputDefn = generateInputDefnBlock(inputs, outputs, options);
        if (inputDefn != null) {
            fileContent = fileContent.replaceAll(INPUT_DEFN_BLOCK, inputDefn);
        }

        String outputDefn = generateOutputDefnBlock(inputs,
                                                    outputs,
                                                    options);
        if (outputDefn != null) {
            fileContent = fileContent.replaceAll(OUTPUT_DEFN_BLOCK, outputDefn);
        }

        String outputUsage = generateOutputUsageBlock(inputs,
                                                      outputs,
                                                      options);
        if (outputUsage != null) {
            fileContent = fileContent.replaceAll(OUTPUT_USAGE_BLOCK, outputUsage);
        }

        return fileContent;
    }

    /** Constant. */
    private static final String NEW_LINE = "\n";
    /** Constant. */
    private static final String TAB = "\t";

    /**
     * Helper method to generate runExternal block of code.
     */
    private static String generateRunExternalDefnBlock(List<ComponentIO> outputs) {

        StringBuffer sb = new StringBuffer();
        sb.append(TAB);
        sb.append("// Run the program...");
        sb.append(NEW_LINE);
        sb.append(TAB);
        sb.append("File outputDirectory = ").append("this.runExternal();");
        sb.append(NEW_LINE);

        return sb.toString();
    }

    /**
     * Helper method to generate options definition block of code.
     */
    private static String generateOptionsDefnBlock(List<ComponentIO> inputs,
                                                   List<ComponentIO> outputs,
                                                   List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();
        for (ComponentOption co : options) {
            sb.append(TAB);
            sb.append("/** Component option (").append(co.getName()).append("). */");
            sb.append(NEW_LINE);
            sb.append(TAB);
            sb.append("String ").append(co.getName()).append(" = null;");
            sb.append(NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Helper method to generate process options block of code.
     */
    private static String generateProcessOptionsBlock(List<ComponentIO> inputs,
                                                      List<ComponentIO> outputs,
                                                      List<ComponentOption> options) {

        if (inputs.size() == 0) { return ""; }

        ComponentIO input0 = inputs.get(0);

        StringBuffer sb = new StringBuffer();
        sb.append(TAB);
        sb.append("// Add input meta-data (headers) to output file.");
        sb.append(NEW_LINE);
        sb.append(TAB);
        sb.append("this.addMetaDataFromInput(")
            .append("\"").append(input0.getType()).append("\", 0, 0, \".*\");");
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);

        sb.append(TAB);
        sb.append("// Add additional meta-data for each output file.");
        for (ComponentIO co : outputs) {
            sb.append(NEW_LINE);
            sb.append(TAB);
            sb.append("this.addMetaData(")
                .append("\"").append(co.getType()).append("\", ")
                .append(co.getIndex()).append(", ")
                .append("META_DATA_LABEL, \"label").append(co.getIndex())
                .append("\", 0, null);");
        }
        sb.append(NEW_LINE);

        return sb.toString();
    }

    /** Constant. */
    private static final String GET_OPT_AS_STR = "this.getOptionAsString(";

    /**
     * Helper method to generate parse options block of code.
     */
    private static String generateParseOptionsBlock(List<ComponentIO> inputs,
                                                    List<ComponentIO> outputs,
                                                    List<ComponentOption> options) {

        

        StringBuffer sb = new StringBuffer();
        for (ComponentOption co : options) {
            sb.append(TAB);
            sb.append("if(").append(GET_OPT_AS_STR).append("\"")
                .append(co.getName()).append("\") != null) {");
            sb.append(NEW_LINE);
            sb.append(TAB).append(TAB);
            sb.append(co.getName()).append(" = ").append(GET_OPT_AS_STR).append("\"")
                .append(co.getName()).append("\")");
            if (co.getDefault() != null) {
                // Try to determine if user has specified regex in 'default'
                // Obviously, this is far from perfect...
                if ((co.getDefault().indexOf("\\") >= 0)
                    || (co.getDefault().indexOf("^") >= 0)
                    || (co.getDefault().indexOf("$") >= 0)) {
                    // Replace single backslash with double.
                    String tmpDefault = co.getDefault().replaceAll("\\\\", "\\\\\\\\");
                    sb.append(".replaceAll(\"").append(tmpDefault)
                        .append("\", \"$1\")");
                }
            }
            sb.append(";");
            sb.append(NEW_LINE);
            sb.append(TAB);
            sb.append("}");
            sb.append(NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Helper method to generate input defn block of code.
     */
    private static String generateInputDefnBlock(List<ComponentIO> inputs,
                                                 List<ComponentIO> outputs,
                                                 List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();
        for (ComponentIO ci : inputs) {
            sb.append(TAB);
            sb.append("File inputFile").append(ci.getIndex())
                .append(" = this.getAttachment(")
                .append(ci.getIndex())
                .append(", 0);");
            sb.append(NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Helper method to generate output defn block of code.
     */
    private static String generateOutputDefnBlock(List<ComponentIO> inputs,
                                                  List<ComponentIO> outputs,
                                                  List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();

        for (ComponentIO co : outputs) {
            sb.append(TAB);
            sb.append("File outputFile").append(co.getIndex())
                .append(" = new File(outputDirectory.getAbsolutePath() + \"/")
                .append(co.getName());
            sb.append("\");");
            sb.append(NEW_LINE);
        }

        return sb.toString();
    }

    /**
     * Helper method to generate output usage block of code.
     */
    private static String generateOutputUsageBlock(List<ComponentIO> inputs,
                                                   List<ComponentIO> outputs,
                                                   List<ComponentOption> options) {

        StringBuffer sb = new StringBuffer();
        for (ComponentIO co : outputs) {
            sb.append(TAB).append(TAB);
            sb.append("this.addOutputFile(outputFile").append(co.getIndex())
                .append(", ").append(co.getIndex())
                .append(", 0, \"").append(co.getType()).append("\");");
            sb.append(NEW_LINE);
        }

        return sb.toString();
    }
}

