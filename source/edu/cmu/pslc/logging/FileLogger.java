/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import edu.cmu.pslc.logging.util.DateTools;

/**
 * This is a simple file logger, that will write to a file with the given name.
 * <p>
 * It does not wrap the XML within the OLI XML.
 * <p>
 * These files are readable by DataShop's FileLogger application.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6000 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-02-26 15:18:28 -0500 (Fri, 26 Feb 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FileLogger extends AbstractMessageLogger {

    /** File writer. */
    private PrintWriter outputStream;

    /**
     * Create the file logger given a file name.
     * @param fileName the file name
     * @return the newly created file logger
     */
    public static FileLogger create(String fileName) {
        return create(fileName, ENCODING_UTF8);
    }

    /**
     * Create the file logger given a file name.
     * @param fileName the file name
     * @param encoding the encoding to be indicated at the top of the XML
     * @return the newly created file logger
     */
    public static FileLogger create(String fileName, String encoding) {
        FileLogger msgLogger = null;
        try {
            PrintWriter stream = new PrintWriter(new FileOutputStream(fileName));
            msgLogger = new FileLogger(stream, encoding);
        } catch (IOException exception) {
            System.out.println("Failed to open file for writing " + fileName);
        }
        return msgLogger;
    }

    /**
     * Constructor.
     * @param outputStream the output stream
     * @param encoding the encoding to be indicated at the top of the XML
     */
    public FileLogger(PrintWriter outputStream, String encoding) {
        super(encoding);
        this.outputStream = outputStream;
        outputStream.println(getOpenXmlSchemaType());
    }

    /**
     * Logs a message to the previously given file.
     * @param message the message to log to the file
     * @return true if log succeeded, false otherwise
     */
    public boolean log(Message message) {
        Date timeStamp = DateTools.getDate(message.getTimeString(), message.getTimeZone());
        return log(message, timeStamp);
    }

    /**
     * Logs a message. This take an authentication token, but it is ignored.
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @param message the message to log
     * @return true if log succeeded, false otherwise
     */
    public boolean log(String authToken, Message message) {
        Date timeStamp = DateTools.getDate(message.getTimeString(), message.getTimeZone());
        //not sure if a print is okay here or not. log4j is not okay.
        System.out.println("FileLogger does not need an authToken, ignoring parameter.");
        return log(message, timeStamp);
    }

    /**
     * Logs a message with a new time stamp.
     * Converts the Date to a time string without a time zone.
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    public boolean log(Message message, Date timeStamp) {
        String timeString = DateTools.getTimeStringWithOutTimeZone(timeStamp);
        return log(message, timeString);
    }

    /**
     * Logs a message with a new time stamp. This take an authentication token, but it is ignored.
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    public boolean log(String authToken, Message message, Date timeStamp) {
        String timeString = DateTools.getTimeStringWithOutTimeZone(timeStamp);
        //not sure if a print is okay here or not. log4j is not okay.
        System.out.println("FileLogger does not need an authToken, ignoring parameter.");
        return log(message, timeString);
    }

    /**
     * Logs a message with a new time stamp.
     * @param message the message to log
     * @param timeString the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    public boolean log(Message message, String timeString) {
        message.setTimeString(timeString);
        outputStream.println(message.toString());
        return true;
    }
    /**
     * Closes the output stream; this is required.
     */
    public void close() {
        outputStream.println(getCloseXml());
        outputStream.close();
    }
}
