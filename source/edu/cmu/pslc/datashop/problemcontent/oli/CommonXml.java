/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.problemcontent.oli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.JDOMParseException;
import org.jdom.input.SAXBuilder;

/**
 * Utility class for common XML-related functions for documents, elements, and files.
 * @author mkomisin
 *
 */
public final class CommonXml {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger("CommonXml");
    /** Bad filename characters. */
    public static final String BAD_FILEPATH_CHARS = "[:*?\"<>|\\s]+";

    /**
     * Private empty constructor.
     */
    private CommonXml() {

    }

    /**
     * Returns problem content given a filename.
     * @param file the XML File
     * @return the XML Document
     */
    public static Document getDocument(File file) {
        Document document = null;
        String fileName = file.getName();
        /** The SaxBuilder used to create the problem content XML document. */
        SAXBuilder builder = new SAXBuilder();
        // Workaround for JDK 1.7u45 bug described in https://community.oracle.com/thread/2594170
        builder.setReuseParser(false);

        try {
            // Attempt to build XML document using SAXBuilder
            document = builder.build(file);
        } catch (IOException ex) {
            logger.warn("IOException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMParseException ex) {
            logger.warn("JDOMParseException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (JDOMException ex) {
            logger.warn("JDOMException occurred with " + fileName + ". ", ex);
            document = null;
        } catch (Exception parseException) {
            logger.warn("Not a valid XML file: " + fileName);
        }
        return document;
    }

    /**
     * Returns all elements matching the specified tag name.
     * @param name the tag name or null to get all elements
     * @param elem the parent element
     * @return a list of elements matching the specified tag name
     */
    public static List<Element> getElementsByTagName(Element elem, String name) {
        ArrayList<Element> ArrayList = null;

        if (name == null) {
            ArrayList = new ArrayList<Element>(elem.getChildren());
        } else {
            ArrayList = new ArrayList<Element>(elem.getChildren(name));
            Namespace ns = Namespace.getNamespace("http://oli.web.cmu.edu/activity/workbook/");
            ArrayList<Element> ArrayListNS = new ArrayList(elem.getChildren(name, ns));
            ArrayList.addAll(ArrayListNS);
        }

        for (Iterator<Element> it = elem.getChildren().iterator(); it.hasNext();) {
            ArrayList.addAll(getElementsByTagName((Element) it.next(), name));

        }
        return ArrayList;
    }

    /**
     * Create a directory; all non-existent ancestor directories are
     * automatically created.
     * @param directory the path of the directory to create
     * @return the File if successful, null otherwise
     */
    public static File createDirectory(String directory) {
        File dir = new File(directory);
        boolean statusFlag = (new File(directory)).mkdirs();
        return dir;
    }

    /** Buffer size for building the md5 hash. */
    private static final int MD5_INPUT_STREAM_BUFFER_SIZE = 1024;
    /**
     * Returns the 32 byte MD5 checksum of a file given its file path.
     * @param filePath the file path of the file
     * @return the 32 byte MD5 hash string
     * @throws RuntimeException
     */
    public static String md5Hash(String filePath) {
        MessageDigest mDigest;
        StringBuffer sb = new StringBuffer();

        try {
            mDigest = MessageDigest.getInstance("MD5");
            int nread = 0;

            FileInputStream fis = new FileInputStream(filePath);
            byte[] data = new byte[MD5_INPUT_STREAM_BUFFER_SIZE];

            while ((nread = fis.read(data)) != -1) {
                mDigest.update(data, 0, nread);
            }
            byte[] mdBytes = mDigest.digest();

            for (int i = 0; i < mdBytes.length; i++) {
                sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }

            logger.debug("Digest(in hex format):: " + sb.toString());

        } catch (NoSuchAlgorithmException e) {
            logger.error("MD5 checksum of " + filePath + " could not be read.");
        } catch (FileNotFoundException e) {
            logger.error("File " + filePath + " not found.");
        } catch (IOException e) {
            logger.error("File " + filePath + " could not be read.");
        }
        return sb.toString();
    }

    /**
     * Recursively deletes a directory since Java will only delete empty directories.
     * @param file the file or directory to delete
     */
    public static void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }

}
