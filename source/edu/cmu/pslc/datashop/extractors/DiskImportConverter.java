/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.extractors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.cmu.pslc.datashop.sourcedb.dao.SourceDbDaoFactory;
import edu.cmu.pslc.datashop.sourcedb.dao.hibernate.SourceDbHibernateDaoFactory;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.datashop.util.VersionInformation;

/**
 * This tool is used to convert a log file to a SQL file.
 * The part where converting a log file to a xml file
 * is from Sandy Demi's CTAT LogConverter program. Optionally,
 * the program will load the generated SQL files into the
 * log database.
 *
 * @author Shanwen Yu
 * @version $Revision: 9480 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-06-24 09:57:53 -0400 (Mon, 24 Jun 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DiskImportConverter {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    /** Encoding used for log conversion */
    private static final String ENCODING_UTF8 = "UTF-8";
    /** The name of this tool, used in displayUsage method. */
    private static final String TOOL_NAME = DiskImportConverter.class.getSimpleName();
    /** Success prefix string. */
    private static final String SUCCESS_PREFIX = TOOL_NAME + " - ";
    /** Warning prefix string. */
    private static final String WARN_PREFIX = "WARN " + TOOL_NAME + " - ";
    /** Error prefix string. */
    private static final String ERROR_PREFIX = "ERROR " + TOOL_NAME + " - ";
    /** Whether the inner XML is URL encoded? */
    private static final boolean IS_URL_ENCODED = true;

    /** The name of the log database */
    private static final String LOG_DB_NAME = "log";
    /** MySql client path */
    private static final String MYSQL_PATH = "mysql";
    /** MySql Options (-B for batch processing) */
    private static final String MYSQL_OPTS = "-B";
    /** Flag preceding the username */
    private static final String USER_FLAG = "--user=";
    /** Flag preceding the password */
    private static final String PWD_FLAG = "--password=";
    /** Flag telling MySQL to execute the following command */
    private static final String EXECUTE_FLAG = "-e";
    /** Source file tells MySQL to run the following SQL file */
    private static final String SRC_CMD = "source ";

    /** No white spaces used for XML pretty indentation */
    private static final String XML_INDENT = "";
    /** Regular expression to match Xml 1.0 declaration */
    private static final String XML_ID_REGEXP = "<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>";

    /** String value of the directory */
    private String inputDirectoryName = null;
    /** String value of the input file name */
    private String inputFileName = null;
    /** The output directory that will contain the generated xml and SQL files */
    private String outputDirectoryName = null;
    /** String value of the autoload SQL option */
    private boolean autoloadSQLFiles = false;

    /** Name of the element */
    private String elementName = null;
    /** Type of the attribute */
    private String attributeType = null;
    /** Value of the attribute */
    private String attributeValue = null;
    /** Document */
    private Document doc = null;

    /** Properties file key-values needed for the DBMerge */
    private Properties properties;
    /** The build properties file */
    private static final String BUILD_PROPERTIES = "build.properties";

    /** Constructor.*/
    public DiskImportConverter() {
        try {
            // Load properties from class path
            properties = new Properties();
            properties.load(loadFromClasspath(BUILD_PROPERTIES));
        } catch (Exception exception) {
            logError("Couldn't load properties file.", exception);
        }
    }

    /** Main program to run.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("DiskImportConverter.main");
        String version = VersionInformation.getReleaseString();
        logger.info("DiskImportConverter starting (" + version + ")...");

        DiskImportConverter converter = new DiskImportConverter();
        try {
            // Handle command line arguments, exit if something is amiss
            converter.handleOptions(args);
            // Convert all of the log files to XML containing log transactions,
            // then convert the log transactions to SQL inserts
            converter.run();
        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("DiskImportConverter done.");
        }
    }

    /**
     * Loads a properties file found in the context of the classpath.
     * @param fileName name of properties file
     * @return InputStream that can be loaded into a Properties object.
     * @throws Exception */
    private InputStream loadFromClasspath(String fileName) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(fileName);
    }

    /**
     * Check that an XML string is well-formed and parses correctly
     * and return a compact format representation.
     * @param inputString a XML document represented by the string
     * @return the line of XML after compaction
     * @throws Exception exception
     */
    private String checkXMLValid(String inputString) throws Exception {
        SAXBuilder saxb = new SAXBuilder();
        // Setup XML outputter to use compact format, no indent, and keep XML declaration
        XMLOutputter xmlo = new XMLOutputter(Format.getCompactFormat()
                .setIndent(XML_INDENT).setOmitEncoding(false).setOmitDeclaration(
                        false).setLineSeparator("\n"));

        // Output JDOM to output stream
        ByteArrayOutputStream byteArray = null;
        InputStream inputStream = null;
        String output = null;
        try {
            byteArray = new ByteArrayOutputStream();
            inputStream = new ByteArrayInputStream(inputString.getBytes());
            doc = saxb.build(inputStream);


            // Output XML
            if (doc != null) {
                Element root = doc.getRootElement();
                if (root != null) {
                    xmlo.output(root, byteArray);
                }
            }
            output = byteArray.toString(ENCODING_UTF8);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (byteArray != null) {
                byteArray.close();
            }
        }
        return output != null ? output : "";
    }
    /**
     * Place xml transactions into their own lines and URL decode each line.
     * Also, check that each line is valid.
     * @param inFile input file
     * @param isUrlEncoded is URL encoded flag
     * @return the success flag
     * @throws IOException exception
     */
    private boolean processLogFile(File inFile, File outFile, boolean isUrlEncoded) throws IOException {
        BufferedReader source = null;
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        // Stores our generated SQL insert statements
        StringBuffer sqlStringBuffer = new StringBuffer();

        try {
            fileInputStream = new FileInputStream(inFile.getAbsoluteFile());
            inputStreamReader = new InputStreamReader(fileInputStream, ENCODING_UTF8);
            source = new BufferedReader(inputStreamReader);


            // Line from log file
            String line = new String();
            // Counter for which byte in the log file we're at (byte starts at each XML declaration)
            int characterCount = 1;

            sqlStringBuffer.append("USE log;");
            sqlStringBuffer.append("\n");
            sqlStringBuffer.append("START TRANSACTION;");
            sqlStringBuffer.append("\n");

            // Read each line in the source log
            while ((line = source.readLine()) != null) {
                String decodedLine = null;
                // We decided to split on the delimiter "<?" to catch the beginning
                // of XML declarations
                if (line.contains("<?")) {
                    String[] snippets = line.split("\\<\\?");
                    for (String snippet : snippets) {
                        if (!snippet.isEmpty()) {
                            // Parse XML to show that it's valid (include split delimiter again)
                            String snippetFix = "<?" + snippet;
                            try {
                                String encodedLine = checkXMLValid(snippetFix);

                                // Convert each encoded line of xml into a decoded line
                                if (isUrlEncoded) {
                                    decodedLine = unescape(encodedLine);
                                } else if (!isUrlEncoded) {
                                    decodedLine = encodedLine;
                                }

                                // Generate the MySql statement for inserting the XML
                                String genSql = convertToSql(decodedLine,
                                        inFile.getName(), characterCount, encodedLine);

                                // Append MySql statements to string buffer
                                if (genSql != null && !genSql.trim().isEmpty()) {
                                    sqlStringBuffer.append(genSql);
                                    sqlStringBuffer.append("\n");
                                }
                            } catch (Exception e) {
                                logError(ERROR_PREFIX, "processLogFile: " + inFile.getName() + ": "
                                    + e.getCause() + ", char " + characterCount
                                    + ": " + snippetFix);
                            } finally {
                                // Increment byte counter to the head of the next new snippet
                                characterCount += snippet.length() + "<?".length();
                            }
                        }
                    }
                } else if (!line.isEmpty()) {   // "<?" not found, so read as a single line
                    try {
                        if (isUrlEncoded) {
                            decodedLine = unescape(line);
                        } else {
                            decodedLine = line;
                        }

                        // Generate the MySql statement for inserting the XML
                        String genSql = convertToSql(decodedLine,
                                inFile.getName(), characterCount, line);

                        // Append MySql statements to string buffer
                        if (genSql != null && !genSql.trim().isEmpty()) {
                            sqlStringBuffer.append(genSql);
                            sqlStringBuffer.append("\n");
                        }

                    } catch (Exception e) {
                        logError(ERROR_PREFIX, "processLogFile: " + inFile.getName() + ": "
                                + e.getCause() + ", char " + characterCount + ": " + line);
                    }
                    // Increment byte counter to the head of the next new snippet
                    characterCount += line.length();
                }
            }
            sqlStringBuffer.append("COMMIT;");
        } finally {
            if (source != null) {
                source.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }

        // Output to sqlFile

        BufferedWriter destination = null;
        OutputStreamWriter outputStreamWriter = null;
        FileOutputStream fileOutputStream = null;
        try {

            if (outFile.exists()) {
                logError("File already exists! Skipping " + outFile.getAbsolutePath());
                return false;

            }
            fileOutputStream = new FileOutputStream(outFile);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream, ENCODING_UTF8);
            destination = new BufferedWriter(outputStreamWriter);


            destination.write(sqlStringBuffer.toString());
        } finally {
            if (destination != null) {
                destination.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
        return true;
    }


    // from w3c at http://www.w3.org/International/unescape.java
    /**
     * Unescape a string.
     * @param s the escaped string
     * @return the unescaped string
     */
    private String unescape(String s) {
        StringBuffer sbuf = new StringBuffer();
        int l = s.length();
        int ch = -1;
        int b, sumb = 0;
        for (int i = 0, more = -1; i < l; i++) {
            /* Get next byte b from URL segment s */
            ch = s.charAt(i);
            switch (ch) {
            case '%':
                ch = s.charAt(++i);
                int hb = (Character.isDigit((char) ch) ? ch - '0'
                        : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                ch = s.charAt(++i);
                int lb = (Character.isDigit((char) ch) ? ch - '0'
                        : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                b = (hb << 4) | lb;
                break;
            case '+':
                b = ' ';
                break;
            default:
                b = ch;
            }
            /* Decode byte b as UTF-8, SUMB collects incomplete chars */
            if ((b & 0xc0) == 0x80) { // 10xxxxxx (continuation byte)
                sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to SUMB
                if (--more == 0) {
                    sbuf.append((char) sumb); // Add char to SBUF
                }
            } else if ((b & 0x80) == 0x00) { // 0xxxxxxx (yields 7 bits)
                sbuf.append((char) b); // Store in SBUF
            } else if ((b & 0xe0) == 0xc0) { // 110xxxxx (yields 5 bits)
                sumb = b & 0x1f;
                more = 1; // Expect 1 more byte
            } else if ((b & 0xf0) == 0xe0) { // 1110xxxx (yields 4 bits)
                sumb = b & 0x0f;
                more = 2; // Expect 2 more bytes
            } else if ((b & 0xf8) == 0xf0) { // 11110xxx (yields 3 bits)
                sumb = b & 0x07;
                more = 3; // Expect 3 more bytes
            } else if ((b & 0xfc) == 0xf8) { // 111110xx (yields 2 bits)
                sumb = b & 0x03;
                more = 4; // Expect 4 more bytes
            } else /* if ((b & 0xfe) == 0xfc) */ { // 1111110x (yields 1 bit)
                sumb = b & 0x01;
                more = 5; // Expect 5 more bytes
            }
            /* We don't test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString();
    }

    /**
     * Find a sub element given a name.
     * @param list a list of elements
     * @param name name of the element
     * @param subElem sub element
     * @return Element sub element
     */
    private Element getSubElementByName(List<Element> list, String name, Element subElem) {
        Iterator<Element> listIterator = list.iterator();
        while (listIterator.hasNext() && (subElem == null)) {
            Element element = (Element) listIterator.next();
            logDebug("Checking " + element.getName() + " against " + name);
            if (element.getName().equals(name)) {
                logDebug("**** getSubElement found " + name);
                subElem = element;
                break;
            } else if ((subElem == null) && !(element.getChildren().isEmpty())) {
                logDebug("Checking children of " + element.getName());
                subElem = getSubElementByName(element.getChildren(), name, subElem);
            }
        }
        return subElem;
    }

    /**
     * Find a sub element based on an attribute type.
     * @param list a list of elements
     * @param type attribute type
     * @param subElem sub element
     * @return Element sub element
     */
    private Element getSubElementByAttribute(List<Element> list, String type,
            Element subElem) {
        Iterator<Element> listIterator = list.iterator();
        while (listIterator.hasNext() && (subElem == null)) {
            Element element = (Element) listIterator.next();
            logDebug("Checking " + element.getName() + " for " + type);
            if (element.getAttribute(type) != null) {
                logDebug("**** getSubElement found " + type);
                subElem = element;
                break;
            } else if ((subElem == null) && !(element.getChildren().isEmpty())) {
                logDebug(" checking children of " + element.getName());
                subElem = getSubElementByAttribute(element.getChildren(), type,
                        subElem);
            }
        }
        return subElem;
    }

    /**
     * Find whether the attribute with a given value exists.
     * @param list a list of elements
     * @param val value
     * @param found flag indicates whether the given attribute has value
     * @return boolean flag, returns true if found, otherwise false
     */
    private boolean hasAttributeValue(List<Element> list, String val, boolean found) {
        Iterator<Element> iter = list.iterator();
        while (iter.hasNext() && (!found)) {
            Element element = (Element) iter.next();
            if (hasAttributeValue(element, val)) {
                found = true;
                break;
            } else if ((!found) && !(element.getChildren().isEmpty())) {
                logDebug(" checking children of " + element.getName());
                found = hasAttributeValue(element.getChildren(), val, found);
            }
        }
        return found;
    }

    /**
     * Find whether an attribute with a given value exists.
     * @param element an Element
     * @param val value to compare
     * @return boolean flag, returns true if found, otherwise false
     */
    private boolean hasAttributeValue(Element element, String val) {
        logDebug("In hasAttributeValue, checking attributes of " + element.getName());
        boolean found = false;
        List<Attribute> aList = element.getAttributes();
        Iterator<Attribute> iter = aList.iterator();
        while (iter.hasNext()) {
            Attribute attribute = (Attribute) iter.next();
            logDebug(" checking attribute " + attribute.getName());
            if (attribute.getValue().equals(val)) {
                logDebug(" found attribute value " + attribute.getValue());
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Method to find if the filter matches the element name, attribute type or value.
     * @param logElem element
     * @return true if the filter matches, otherwise false
     */
    private boolean filterMatched(Element logElem) {
        logDebug("Checking " + logElem.getName());
        Element subElem = null;
        if (elementName != null) {
            if (!logElem.getName().equals(elementName)) {
                // search until there are no more children or element is
                // found
                List<Element> eList = logElem.getChildren();
                subElem = getSubElementByName(eList, elementName, subElem);
                if (subElem == null) {
                    logDebug("Skipping: " + logElem.getName());
                    return false;
                }
                logDebug("Found sub element: " + subElem.getName() + ", "
                        + subElem.getValue());
            }
        }

        if (attributeType != null) {
            // if an element name was given and the element was found...
            if (subElem != null) {
                // attrElem = subElem;
                if (subElem.getAttribute(attributeType) == null) {
                    logDebug("Skipped attrName " + attributeType);
                    return false;
                }
            } else if (logElem.getAttribute(attributeType) == null) {
                List<Element> eList = logElem.getChildren();
                subElem = getSubElementByAttribute(eList, attributeType,
                        subElem);
                if (subElem == null) {
                    logDebug("Skipping: " + logElem.getName());
                    return false;
                }
            }
        }

        if (attributeValue != null) {
            if (attributeType != null) {
                Element attrElem = logElem;
                if (subElem != null) {
                    attrElem = subElem;
                }
                String val = attrElem.getAttributeValue(attributeType);
                if ((val == null) || (!val.equals(attributeValue))) {
                    logDebug("Skipped val " + attributeValue);
                    return false;
                }

            } else {
                if (subElem != null) {
                    if (!hasAttributeValue(subElem, attributeValue)) {
                        logDebug("Value " + attributeValue
                                + " not found in sub element "
                                + subElem.getName());
                        return false;
                    }
                } else {
                    List<Element> eList = logElem.getChildren();
                    if (!hasAttributeValue(eList, attributeValue, false)) {
                        logDebug("Skipping: " + logElem.getName());
                        logDebug("Value " + attributeValue
                                + " not found in children of "
                                + logElem.getName());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Returns a list with the one file in it, if it exists and all that.
     * @param fileName the file name from the command line
     * @param extension the file name extension to search for
     * @return a list if the file exists, null otherwise
     */
    private List<File> getFileListFromFile(String fileName, String extension) {
        List<File> inputFileInfoList = null;

        // if windows operation system, replace slashes in path
        boolean winFlag = false;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.indexOf("win") >= 0) {
            winFlag = true;
            logDebug(SUCCESS_PREFIX,
                    "Replacing slashes path of file name, os.name: ",
                    osName);
        } else {
            logDebug(SUCCESS_PREFIX,
                    "NOT Replacing slashes path of file name, os.name: ",
                    osName);
        }

        if (fileName != null) {
            if (winFlag) {
                fileName = fileName.replaceAll("\\\\", "\\/");
            }

            File theFile =  new File(fileName);

            if (!theFile.exists()) {
                logError("File " + fileName + " does not exist.");
            } else {
                if (theFile.getName().endsWith("." + extension)) {
                    logDebug(SUCCESS_PREFIX, "File ", fileName, " found.");
                    inputFileInfoList = new ArrayList<File>();
                    inputFileInfoList.add(theFile);
                } else {
                    logError("File " + fileName
                            + " does not have a ." + extension + " extension.");
                }
            }
        } else {
            logError("File name is null");
        }
        return inputFileInfoList;
    }

    /**
     * Returns a list with all the files in the given directory in it.
     * @param dirName the directory from the command line
     * @param extension the file name extension to search for
     * @return a list if the directory exists, null otherwise
     */
    private List<File> getFileListFromDirectory(String dirName, String extension) {
        List<File> inputFileInfoList = null;

        if (dirName != null) {
            File fileObject =  new File(dirName);
            if (fileObject.exists()) {
                logDebug(SUCCESS_PREFIX, "Directory ", dirName, " found.");

                List<String> fileNameList;
                try {
                    fileNameList = getFilenameList(dirName, extension);
                    if (fileNameList.size() <= 0) {
                        logError(
                                "No files found in the directory " + dirName);
                    } else {
                        // if windows operation system, replace slashes in path
                        boolean winFlag = false;
                        String osName = System.getProperty("os.name").toLowerCase();
                        if (osName.indexOf("win") >= 0) {
                            winFlag = true;
                            logDebug(SUCCESS_PREFIX,
                                    "Replacing slashes path of file name, os.name: ",
                                    osName);
                        } else {
                            logDebug(SUCCESS_PREFIX,
                                    "NOT Replacing slashes path of file name, os.name: ",
                                    osName);
                        }

                        inputFileInfoList = new ArrayList<File>();
                        for (Iterator<String> fileIter = fileNameList.iterator();
                                fileIter.hasNext();) {
                            String fileName = new File((String)fileIter.next()).getCanonicalPath();
                            if (winFlag) {
                                fileName = fileName.replaceAll("\\\\", "\\/");
                            }
                            File theFile = new File(fileName);
                            if (!theFile.exists()) {
                                logError(
                                        "File " + fileName + " does not exist.");
                            } else {
                                logDebug("file exists: ", fileName);
                                inputFileInfoList.add(theFile);
                            }
                        }
                    }
                } catch (IOException exception) {
                    logError(ERROR_PREFIX, "IOException occurred for input directory: "
                            + dirName + ", " + exception.getCause());
                    fileNameList = new ArrayList<String>();
                }
            } else {
                logError("Directory " + dirName + " not found.");
            }
        } else {
            logError("File name is null");
        }

        return inputFileInfoList;
    }

    /**
     * Returns an list of file names given a directory.
     * @param directoryName the directory path
     * @param extension the file name extension to search for
     * @return an list of file names
     * @throws IOException possible from getFiles method
     */
    private List<String> getFilenameList(String directoryName, String extension)
            throws IOException {
        File topLevelDirectory = new File(directoryName);

        if (!topLevelDirectory.isDirectory()) {
            logWarn("Not a directory: ", directoryName);
        } else {
            logDebug("Top level directory is ", topLevelDirectory.getName());
        }

        return getFiles(topLevelDirectory, extension);
    }

    /**
     * Convert a log file to XML.
     * @param inputFileInfoList the input file list
     * @return A list of SQL files
     */
    public List<File> convertLogs(List<File> inputFileInfoList) {
        try {
            return convertLogs(inputFileInfoList, true);
        } catch (IOException e) {
            logError(ERROR_PREFIX, "convertLogs: " + e);
        } catch (JDOMException e) {
            logError(ERROR_PREFIX, "convertLogs: " + e);
        }
        return null;
    }

    /**
     * convertToXml a log file to xml file.
     * @param inputFileInfoList a list of input files to be processed.
     * @param escapedFile Specify true if the file is an OLI-style log file that
     * must be unescaped, false otherwise.
     * @return A list of SQL files
     * @throws IOException IO exception
     * @throws JDOMException JDOM exception
     */
    private List<File> convertLogs(List<File> inputFileInfoList, boolean escapedFile)
            throws IOException, JDOMException {
        ArrayList<File> sqlFileList = new ArrayList<File>();
        File inputDirFile = null;
        String subDirPath = null;

        for (File inputFile : inputFileInfoList) {
            File sqlFile = null;    // Process the log file

            if (inputFileName != null) {
                // Converting a single file and saving to the output directory
                inputDirFile = new File(inputFileName);
                subDirPath = "";
            } else {
                // Converting recursively so retain sub-directory structure
                inputDirFile = new File(inputDirectoryName);
                subDirPath = recreateDirectoryStructure(inputFile.getCanonicalPath(),
                        inputDirFile.getCanonicalPath());
            }

            File outputDir = new File(outputDirectoryName + subDirPath);
            outputDir.mkdirs();
            sqlFile = new File(outputDirectoryName + subDirPath + inputFile.getName() + ".sql");

            logInfo("Converting log file: " + inputFile.getCanonicalPath());
            logInfo("Creating sql file: " + outputDirectoryName + subDirPath
                + inputFile.getName() + ".sql");

            // Process the log file
            if (processLogFile(inputFile, sqlFile, IS_URL_ENCODED)) {
                sqlFileList.add(sqlFile);
            }
        }
        return sqlFileList;
    }

    /**
     * Returns the output directory concatenated with the proper sub-directory path.
     * @param inputFilepath the input file's absolute path
     * @param inputDirectory the input directory path (absolute or canonical)
     * @return the output directory concatenated with the proper sub-directory path
     */
    private String recreateDirectoryStructure(String inputFilepath, String inputDirectory) {
        String delimitedFilePath = inputFilepath.replaceAll("\\\\", "/");
        if (delimitedFilePath.contains("/")) {
            String fullPath = delimitedFilePath.replaceFirst(
                inputDirectory.replaceAll("\\\\", "/"), "");
            int endIndex = fullPath.lastIndexOf('/') + 1;
            endIndex = endIndex <= fullPath.length() ? endIndex : fullPath.length();
            return fullPath.substring(1, endIndex);
        } else {
            return delimitedFilePath;
        }
    }

    /**
     * A recursive function to return a list of all the *.txt file names
     * in a top level directory, including all the subdirectories.
     * This method will skip CVS directories.
     * @param theFile a file or directory
     * @param extension the file name extension to search for
     * @return a complete list of the files in this directory
     * @throws IOException if reading the file fails
     */
    private ArrayList<String> getFiles(File theFile, String extension) throws IOException {
        ArrayList<String> fileList = new ArrayList<String>();

        if (theFile.isFile()) {
            if (theFile.getName().endsWith("." + extension)) {
                logDebug(SUCCESS_PREFIX, "Adding file ", theFile.getName());
                fileList.add(theFile.getCanonicalPath());
            }
        } else if (theFile.isDirectory()) {
            if (theFile.getName().equals("CVS")) {
                logDebug("skipping directory ", theFile.getName());
            } else {
                logDebug("found directory ", theFile.getName());
                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    File fileOrDir = files[idx];
                    if (fileOrDir.isFile()) {
                        if (fileOrDir.getName().endsWith("." + extension)) {
                            logDebug(SUCCESS_PREFIX, "Adding file ", fileOrDir.getName());
                            fileList.add(fileOrDir.getCanonicalPath());
                        }
                    } else if (fileOrDir.isDirectory()) {
                        List<String> moreFiles = getFiles(fileOrDir, extension);
                        fileList.addAll(moreFiles);
                    }
                } // end for loop
            } // end else
        } // end else if isDirectory

        return fileList;
    }

    /** Prefix for SQL statement which insert data into log_sess table.*/
    public static final  String SQL_LOG_SESS_PREFIX =
            "INSERT INTO log_sess "
            + "(guid, user_sess, user_id, class_id, treatment_id, assignment_id, "
            + "date, timezone, eastern_date, server_receipt_date, info_type, info) "
            + "VALUES (";

    /** Prefix for SQL statement which insert data into log_act table.*/
    public static final  String SQL_LOG_ACT_PREFIX =
            "INSERT INTO log_act "
            + "(guid, sess_ref, source, time, timezone, action, external_object_id, container, "
            + "concept_this, concept_req, eastern_time, server_receipt_time, info_type, info) "
            + "VALUES (";

    /**
     * Convert an XML file to a SQL file with INSERT statements.
     * @param xmlLineItem xxx
     * @param currentFileName xxx
     * @param characterCount xxx
     * @param debugLine xxx
     * @return true if the conversion is successful, false otherwise
     */
    private String convertToSql(String xmlLineItem,
                            String currentFileName, int characterCount, String debugLine) {
        StringBuffer sqlStatements = new StringBuffer("");
        try {

            xmlLineItem = xmlLineItem.replaceAll(XML_ID_REGEXP, "");
            InputStream inputStream = new ByteArrayInputStream(xmlLineItem.getBytes());
            // Handles some special UTF-8 characters inside an XML file
            Reader reader = new InputStreamReader(inputStream, ENCODING_UTF8);
            InputSource inputSource = new InputSource(reader);
            inputSource.setEncoding(ENCODING_UTF8);

            // Instantiate Document Builder and parse XML line item
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document document = dBuilder.parse(inputSource);
            document.getDocumentElement().normalize();

            // Convert to SQL file

            sqlStatements.append(buildSQLFromTag(document, "log_session_start"));
            sqlStatements.append("\n");
            sqlStatements.append(buildSQLFromTag(document, "log_action"));
            sqlStatements.append("\n");

        } catch (Exception e) {
            logError(ERROR_PREFIX, "convertToSql: " + currentFileName + ": "
                    + e.getCause() + ", char " + characterCount + ": " + debugLine);
        }
        return sqlStatements.toString();
    }


    /**
     * This is the where the control of the process to convert a file or set of files is found.
     */
    public void run() {
        // Build a file list depending on whether a single file or directory was supplied
        List<File> logFileList = null;
        List<File> sqlFileList = new ArrayList<File>();
        // Create a list of file(s) based on the file or directory option
        if (inputFileName != null) {
            logFileList = getFileListFromFile(inputFileName, "log");
        } else if (inputDirectoryName != null) {
            logFileList = getFileListFromDirectory(inputDirectoryName, "log");
        } else {
            logError("File or directory must be specified.");
        }
        logInfo("Begin converting logs to SQL.");
        // Get a list of converted SQL files by invoking the primary method for conversion
        sqlFileList.addAll(convertLogs(logFileList));
        logInfo("Finished converting logs to SQL.");
        // For the autoload SQL option, execute all SQL files after conversion
        if (autoloadSQLFiles) {
            logInfo("Executing SQL in files.");
            try {
                // Execute SQL insert statements
                loadSQLFiles(sqlFileList);
            } catch (IOException e) {
                logError(ERROR_PREFIX, e);
            }
            logInfo("Finished executing SQL in files.");
        }

    }

    /**
     * Returns the string representation of an XML node
     * @param node the XML node
     * @return the string representation of the XML node
     * or null if a transformer exception occurs
     */
    private String nodeToString(org.w3c.dom.Node node) {

        String xmlString = null;
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(node);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
            System.out.println("Unknown" + "\tnodeToString: TransformerException");
            logger.error("DiskImportConverter (nodeToString method) threw TransformerException: ");
            xmlString = null;
        }

        return xmlString;
    }

    /** String constant that used as an xml prefix. */
    public static final String XML_PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    /** XML header */
    private static String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** Build a bunch of SQL insert statements based on the xml tags.
     * log_action tag needs extra work to get the content for the info field.
     * @param document parsed xml document to handle the nodes
     * @param tagName target tag name
     * @return StringBuilder a string builder object that stores all the SQL statements
     * */
    public StringBuilder buildSQLFromTag(org.w3c.dom.Document document, String tagName) {

        String sqlValue = "";
        StringBuilder sqlStatements = new StringBuilder();
        ArrayList<String> rawValues = new ArrayList<String>();
        NodeList nList = document.getElementsByTagName(tagName);

        org.w3c.dom.Node nNode = null;
        int listSize = 0;
        if (tagName.equals("log_session_start")) {
            listSize = nList.getLength();
            logDebug("Start processing <log_session_start> tags. Total " + listSize);
            //loop through log_session_start tag
            for (int i = 0; i < listSize; i++) {
                nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    rawValues.removeAll(rawValues);
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    rawValues.add(DiskImportConverter.generateGUID());
                    rawValues.add(eElement.getAttribute("session_id"));
                    rawValues.add(eElement.getAttribute("user_guid"));
                    rawValues.add(eElement.getAttribute("class_id"));
                    rawValues.add(eElement.getAttribute("treatment_id"));
                    rawValues.add(eElement.getAttribute("assignment_id"));
                    rawValues.add(eElement.getAttribute("date_time"));
                    rawValues.add(eElement.getAttribute("timezone"));
                    rawValues.add(eElement.getAttribute("eastern_date"));
                    rawValues.add("now()"); // server_receipt_date
                    rawValues.add(eElement.getAttribute("info_type"));
                    rawValues.add(eElement.getAttribute("info"));

                    //write everything in this tag to sqlStatements
                    sqlStatements.append(SQL_LOG_SESS_PREFIX);
                    sqlValue = returnAttributeValue(rawValues);
                    sqlStatements.append(sqlValue);
                } // end if
            } // end for loop
            logDebug("Finish processing <log_session_start> tags.");
        } else if (tagName.equals("log_action")) {
            // Log Action Converter
            listSize = nList.getLength();
            logDebug("Start processing <log_session_start> tags. Total " + listSize);
            // Loop through each log_action element in the XML document
            for (int i = 0; i < listSize; i++) {
                nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    rawValues.removeAll(rawValues);
                    // Get the inner XML content from the log_action element
                    NodeList oliXML = nNode.getChildNodes();
                    StringBuffer oliXMLBuffer = new StringBuffer("");
                    // Begin each log entry with the xml header
                    oliXMLBuffer.append(xmlHeader);
                    // Print out the inner XML elements in full
                    for (int childCount = 0; childCount < oliXML.getLength(); childCount++) {
                        String nodeVal = nodeToString((org.w3c.dom.Node) oliXML.item(childCount));
                        oliXMLBuffer.append(nodeVal);
                    }
                    // Get the nNode as an element and add its attributes to our SQL
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    rawValues.add(DiskImportConverter.generateGUID());  // GUID
                    rawValues.add(eElement.getAttribute("session_id")); // sess_ref
                    rawValues.add(eElement.getAttribute("source_id"));  // source
                    rawValues.add(eElement.getAttribute("date_time"));  // time
                    rawValues.add(eElement.getAttribute("timezone"));   // time zone
                    rawValues.add(eElement.getAttribute("action_id"));  // action
                    rawValues.add(eElement
                            .getAttribute("external_object_id")); // external_object_id
                    rawValues.add(eElement.getAttribute("container"));  // container
                    rawValues.add(eElement.getAttribute("concept_this"));   // concept_this
                    rawValues.add(eElement.getAttribute("concept_req"));    // concept_req
                    rawValues.add(eElement.getAttribute("eastern_time"));   // eastern_time
                    rawValues.add("now()"); // server_receipt_time
                    rawValues.add(eElement.getAttribute("info_type"));  // info_type
                    // Add the inner XML as info
                    rawValues.add(oliXMLBuffer.toString()); // info
                    //write everything in this tag to sqlStatements
                    sqlStatements.append(SQL_LOG_ACT_PREFIX);
                    sqlValue = returnAttributeValue(rawValues);
                    sqlStatements.append(sqlValue);
                } // end if
            } // end for loop
            logDebug("Finish processing <log_session_start> tags.");
        } // end if

        return sqlStatements;
    }

    /**
     * Generate a unique identifier. Returns toString() of a new instance of
     * java RMI server UID.
     *
     * @return  prefix plus the UID.toString() result
     */
    public static String generateGUID() {
        UID uid = new UID();
        return uid.toString();
    }

    /**
     * Return attribute value wrapped with single quote or null if there is no value.
     * @param rawValues a string list of raw values
     * @return a String value that concatenate the processed values
     * */
    public String returnAttributeValue(ArrayList<String> rawValues) {
        String value = "", temp = "";
        for (Iterator<String> it = rawValues.iterator(); it.hasNext();) {
            temp = it.next();
            if (temp != null && temp.equals("now()")) {
                value += temp;
            } else if ((temp != null) && (temp != "")) {
                value += "\'" + temp.replace("'", "\\\'")
                        .replaceAll("\"", "\\\"") + "\'";
            }  else {
                value += null;
            }

            if (it.hasNext()) {
                value += ", ";
            } else {
                value += ");\n";
            }
        }
        return value;
    }

    /**
     * Handle the command line arguments.
     * @param args command line arguments passed into main
     */
    public void handleOptions(String[] args) {
        if (args == null || args.length == 0) {
            displayUsage();
            return;
        }

        ArrayList<String> argsList = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            argsList.add(args[i]);
        }

        String argument;

        // loop through the arguments
        for (int i = 0; i < args.length; i++) {
            argument = args[i].trim();

            if (argument.equals("-h")
                    || argument.equals("-help")) {
                displayUsage();
                System.exit(0);
            } else if (argument.equals("-v")
                    || argument.equals("-version")) {
                logDebug(VersionInformation.getReleaseString());
                System.exit(0);
            } else if (argument.equals("-dir")
                    || argument.equals("-directory")) {
                if (++i < args.length) {
                    inputDirectoryName = args[i];
                    inputDirectoryName = inputDirectoryName.replaceAll("\\\\\\\\", "/");
                    inputDirectoryName = inputDirectoryName.replaceAll("\\\\", "/");
                    int len = inputDirectoryName.length();
                    if (!inputDirectoryName.substring(len - 1, len).equals("/")) {
                        inputDirectoryName = inputDirectoryName + "/";
                    }
                } else {
                    logger.error("A directory must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (argument.equals("-f")
                    || argument.equals("-file")) {
                if (++i < args.length) {
                    inputFileName = args[i];
                } else {
                    logger.error("A file name must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (argument.equals("-o")
                    || argument.equals("-output")) {
                if (++i < args.length) {
                    outputDirectoryName = args[i];
                    outputDirectoryName = outputDirectoryName.replaceAll("\\\\\\\\", "/");
                    outputDirectoryName = outputDirectoryName.replaceAll("\\\\", "/");
                    int len = outputDirectoryName.length();
                    if (!outputDirectoryName.substring(len - 1, len).equals("/")) {
                        outputDirectoryName = outputDirectoryName + "/";
                    }
                } else {
                    System.out.println("An output directory must be specified with this argument");
                    logger.error("An output directory must be specified with this argument");
                    displayUsage();
                    System.exit(1);
                }
            } else if (argument.equals("-execsql")) {
                autoloadSQLFiles = true;
            } else if (args[i].indexOf("-e=") != -1) {
                int x = args[i].indexOf("=") + 1;
                elementName = args[i].substring(x, args[i].length());
            } else if (args[i].indexOf("-a=") != -1) {
                int x = args[i].indexOf("=") + 1;
                attributeType = args[i].substring(x, args[i].length());
            } else if (args[i].indexOf("-v=") != -1) {
                int x = args[i].indexOf("=") + 1;
                attributeValue = args[i].substring(x, args[i].length());
            } else if (args[i].equals("-help") || (args[i].equals("-h"))) {
                displayUsage();
            } else {
                System.out.println(" *** Unknown command-line option: "
                        + args[i]);
                displayUsage();
                System.exit(1);
            }
        } // end for loop

        // Check for the required arguments
        boolean requiredArguments = true;
        if (inputFileName == null && inputDirectoryName == null) {
            requiredArguments = false;
            logError("Either a file or directory is required, specify with -f or -dir.");
        }
        // Output directory is required
        if (outputDirectoryName == null) {
            requiredArguments = false;
            logError("An output directory is requiredm, specify with -o.");
        }
        if (!requiredArguments) {
            displayUsage();
            System.exit(1);
        }
    }

    /**
     * Display the usage of this utility.
     */
    public void displayUsage() {
        StringBuffer usageMessage = new StringBuffer();
        usageMessage.append("\nUSAGE: java -classpath ... "
                + TOOL_NAME
                + " -o output_directory\n"
                + " [-help] [-version]"
                + " [-f input_file_name]"
                + " [-dir input_directory]"
                + " [-debug]\n"
                + " Option -o (the output directory) must be specified.\n"
                + " One of -file or -dir (input directory) must be specified..\n");

        usageMessage.append("Options:");
        usageMessage.append("\t-h, -help        \t Display this help and exit");
        usageMessage.append("\t-v, -version     \t Display the version and exit");
        usageMessage.append("\t-f, -file        \t The log file to import");
        usageMessage.append("\t-dir, -directory \t Import the files in the given directory");
        usageMessage.append("\t-o, -output \t Output SQL files to this directory");
        usageMessage.append("\t-debug,\t  Print debugging messages");

        logInfo(usageMessage.toString());
        System.exit(-1);
    }

    /**
     * Uses MySQL client to connect to the database and execute
     * SQL statements saved in the output directory.
     * @param sqlFileList xxx
     * @throws IOException caught by run
     */
    public void loadSQLFiles(List<File> sqlFileList) throws IOException {
        // If autoload is enabled, then execute the SQL statements

       SourceDbDaoFactory sourceDbDao = new SourceDbHibernateDaoFactory();
       HashMap<String, String> userCredentials =
               (HashMap<String, String>) sourceDbDao.getSourceDatabaseLogin();

       // Execute each SQL file in the outputDirectory with MySQL client
       for (File f : sqlFileList) {
           String userId = userCredentials.get("user");
           String passwd = userCredentials.get("password");
           // Setup the MySQL command for executing files
           String[] commands = new String[]{MYSQL_PATH,
                   LOG_DB_NAME,
                   MYSQL_OPTS,
                   USER_FLAG + userId,
                   PWD_FLAG + passwd,
                   EXECUTE_FLAG, SRC_CMD + f.getAbsolutePath()};
           // Run the MySQL client in a shell
           logInfo(MYSQL_PATH + " ",
                   LOG_DB_NAME + " ",
                   MYSQL_OPTS + " ",
                   USER_FLAG + userId + " ",
                   PWD_FLAG + "********" + " ",
                   EXECUTE_FLAG + " ", SRC_CMD + f.getAbsolutePath() + " ");
           Process child = Runtime.getRuntime()
               .exec(commands);
           // Log after exit value
           try {
               // Hold the thread until exit value returns
               if (child.waitFor() == 0) {
                   logInfo("loadSQLFiles: Success executing SQL statements from files.");
               } else {
                   logError("loadSQLFiles: Error executing SQL statements from files.");
               }
           } catch (InterruptedException e) {
               logError("loadSQLFiles: Interrupted Exception from child process (mysql).",
                       e.getMessage());
           }
       }
   }

   /** Only log if debugging is enabled. @param args concatenate objects into one string */
   private void logDebug(Object... args) {
       LogUtils.logDebug(logger, args);
   }

   /** Only log if info is enabled. @param args concatenate objects into one string */
   private void logInfo(Object... args) {
       LogUtils.logInfo(logger, args);
   }
   /** Log warning message. @param args concatenate objects into one string */
   private void logWarn(Object... args) {
       LogUtils.logWarn(logger, args);
   }
   /** Log error message. @param args concatenate objects into one string */
   private void logError(Object... args) {
       LogUtils.logErr(logger, args);
   }
}