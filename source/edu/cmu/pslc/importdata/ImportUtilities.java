/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2006
 * All Rights Reserved
 */
package edu.cmu.pslc.importdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.Format;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Utility class used by Import tools.  Contains
 * methods used for file i/o and system i/o
 *
 * @author Kyle A Cunningham
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ImportUtilities {

    /** Formatter to interpret time field with millisecond granularity. */
    private static FastDateFormat formatter =
            FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS ");

    /**
     * Private Constructor
     */
    private ImportUtilities() { };

    /**
     * Prepare the dataset input file for reading & format verification.
     * @throws FileNotFoundException
     * @param inputFileName String containing name of input file to be opened
     * @return initialized BufferedReader for provided inputFileName
     * @throws FileNotFoundException thrown if file is not found
     * @throws UnsupportedEncodingException the encoding is not supported.
     */
    public static BufferedReader handleInputFile(String inputFileName)
        throws FileNotFoundException, UnsupportedEncodingException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFileName), "UTF8"));
        return in;
    } // end handleInput

    /**
     * Prepare the output file for writing.
     * @throws IOException
     * @throws FileNotFoundException
     * @param fileName String containing name for output file.
     * @param overWrite Boolean indicating if file should be overwritten or appended to.
     * @return initialized PrintWriter for provided output fileName
     * @throws IOException an IOException from creating a Printer, Buffered or File writer
     */
    public static Writer setupOutputFile(String fileName, boolean overWrite)
        throws IOException {
        Writer out = new OutputStreamWriter(new FileOutputStream(fileName, overWrite), "UTF8");
        return out;
    } // end setupOutputFile

    /**
     * Write DEBUG messages to System.out and PrinterWriter out simultaneously.
     * @param out PrintWriter instance
     * @param text text to write
     */
    public static void writeDebug(Writer out, String text) {
        String timeStamp = formatter.format(new Date());
        String msg = timeStamp + " DEBUG: " + text + "\n";
        System.out.print(msg);
        try {
            if (out != null) {
                out.write(msg);
            } else {
                System.err.println("ImportUtilities.writeDebug(): Cannot write debug to log file: "
                        + msg);
            }
        } catch (IOException e) {
            System.err.println("IOException occurred. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write ERROR messages to System.err and PrinterWriter out simultaneously.
     * @param out PrintWriter instance
     * @param text String containing text to write.
     */
    public static void writeError(Writer out, String text) {
        String timeStamp = formatter.format(new Date());
        String msg = timeStamp + " ERROR: " + text + "\n";
        System.err.print(msg);
        try {
            if (out != null) {
                out.write(msg);
            } else {
                System.err.println("ImportUtilities.writeError(): Cannot write error to log file: "
                        + msg);
            }
        } catch (IOException e) {
            System.err.println("IOException occurred. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write INFO messages to System.out and PrinterWriter out simultaneously.
     * @param out PrintWriter instance
     * @param text text to write
     */
    public static void writeInfo(Writer out, String text) {
        String timeStamp = formatter.format(new Date());
        String msg = timeStamp + " INFO: " + text + "\n";
        System.out.print(msg);
        try {
            if (out != null) {
                out.write(msg);
            } else {
                System.err.println("ImportUtilities.writeInfo(): Cannot write info to log file: "
                        + msg);
            }
        } catch (IOException e) {
            System.err.println("IOException occurred. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Write WARN messages to System.out and PrinterWriter out simultaneously.
     * @param out PrintWriter instance
     * @param text text to write
     */
    public static void writeWarn(Writer out, String text) {
        String timeStamp = formatter.format(new Date());
        String msg = timeStamp + " WARN: " + text + "\n";
        System.out.print(msg);
        try {
            if (out != null) {
                out.write(msg);
            } else {
                System.err.println("ImportUtilities.writeWarn(): Cannot write warn to log file: "
                        + msg);
            }
        } catch (IOException e) {
            System.err.println("IOException occurred. " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all the files in a directory.
     * @param directory String directory
     * @return the file array of all files in the directory
     */
    public static File[] getFilesInDirectory(String directory) {
        File[] files = null;
        File dir = new File(directory);
        if (dir != null) {
            files = dir.listFiles();
        } else {
            System.err.println("ImportUtilities.getFilesInDirectory(String directory)"
                    + ": Directory is not valid: "
                    + dir);
        }
        return files;
    }
} // end ImportUtilities class
