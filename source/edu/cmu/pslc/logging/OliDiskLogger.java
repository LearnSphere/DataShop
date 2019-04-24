/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import edu.cmu.oli.log.client.ActionLog;
import edu.cmu.oli.log.client.DiskLogger;
import edu.cmu.oli.log.client.SessionLog;
import edu.cmu.pslc.logging.util.DateTools;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * This logger will log to a file which can then be imported to a OLI-style log database.
 * <p>
 * The file created will be XML within XML.  See the power point document for the DTD
 * for details.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7737 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-05-30 14:17:21 -0400 (Wed, 30 May 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliDiskLogger extends AbstractMessageLogger {

    /** For formatting dates for filenames.  Format: "yyyyMMddHHmmssSSS". */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyyMMddHHmmssSSS");

    /**
     * Directory for disk logging.
     * Default value is ".", the current working directory at runtime.
     */
    private static final String DISK_DIRECTORY = ".";

    /** For logging to a disk. */
    private DiskLogger diskLogger = null;

    /**
     * Create the message logger.
     * @param fileName the file name
     * @return the newly created OLI disk logger
     */
    public static OliDiskLogger create(String fileName) {
        return create(fileName, ENCODING_UTF8);
    }

    /**
     * Create the message logger.
     * @param fileName the file name
     * @param encoding the encoding to be indicated at the top of the XML
     * @return the newly created OLI disk logger
     */
    public static OliDiskLogger create(String fileName, String encoding) {
        OliDiskLogger msgLogger = null;

        DiskLogger diskLogger = new DiskLogger();
        if (fileName == null || fileName.length() == 0) {
            fileName = DISK_DIRECTORY + File.separator + getDefaultFilename();
        }
        diskLogger.setOutfile(fileName);

        msgLogger = new OliDiskLogger(diskLogger, encoding);

        return msgLogger;
    }

    /**
     * Constructor.
     * @param diskLogger the OLI disk logger
     * @param encoding the encoding to be indicated at the top of the XML
     */
    public OliDiskLogger(DiskLogger diskLogger, String encoding) {
        super(encoding);
        this.diskLogger = diskLogger;
    }

    /**
     * Logs a message to the previously given file.
     * @param message the message to log to the file
     * @return true if log succeeded, false otherwise
     */
    public boolean log(Message message) {
        Date timeStamp = DateTools.getDate(message.getTimeString(), message.getTimeZone());
        return log(null, message, timeStamp);
    }

    /**
     * Logs a message with an authentication token.
     * @param message the message to log
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @return true if log succeeded, false otherwise
     */
    public boolean log(String authToken, Message message) {
        Date timeStamp = DateTools.getDate(message.getTimeString(), message.getTimeZone());
        return log(authToken, message, timeStamp);
    }

    /**
     * Logs a message with a new time stamp.
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @return true if log succeeded, false otherwise
     */
    public boolean log(Message message, Date timeStamp) {
        return log(null, message, timeStamp);
    }

    /**
     * Logs a message with a new time stamp, and an authentication token.
     * @param message the message to log
     * @param timeStamp the new time stamp to use
     * @param authToken the authentication token needed for authenticated logging (OLI servers)
     * @return true if log succeeded, false otherwise
     */
    public boolean log(String authToken, Message message, Date timeStamp) {
        String info = getOpenXml() + message.toString(false) + getCloseXml();

        ActionLog actionLog = new ActionLog();
        if (authToken != null && authToken.length() > 0) {
            actionLog.setAuthToken(authToken);
        }
        actionLog.setUserGuid(message.getUserId());
        actionLog.setSessionId(message.getSessionId());
        actionLog.setTimeStamp(timeStamp);
        actionLog.setTimezone(message.getTimeZone());
        actionLog.setSourceId(message.getSource());
        actionLog.setInfoType(INFO_TYPE);
        actionLog.setInfo(info);

        Boolean result = diskLogger.logActionLog(actionLog);
        if (!result.booleanValue()) {
            System.out.println("OliDiskLogger: log result " + result
                      + ", last error " + diskLogger.getLastError());
        }
        return true;
    }

    /**
     * Log a SessionLog record.
     * @param  userName the user name
     * @param  sessionId the session id
     * @see SessionLog
     */
    public void logSession(String userName, String sessionId) {
        synchronized (this) {   // recheck w/in synch block after unlocked check
            SessionLog slog = new SessionLog();
            slog.setUserGuid(userName);
            slog.setSessionId(sessionId);
            try {
                diskLogger.logSessionLog(slog);
            } catch (Exception exception) {
                System.out.println("OliDiskLogger: Exception occurred while logging session."
                        + exception.getMessage());
                exception.printStackTrace();
            }
        } // end synchronized block

    } // end logSession method

    /**
     * Close the file stream.
     */
    public void close() {
        try {
            FileOutputStream fos = diskLogger.getOutfile();
            fos.close();
        } catch (IOException exception) {
            System.out.println("OliDiskLogger: Exception occurred while closing stream."
                    + exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * Return the default filename.
     * @return String of form <i>userID</i>_<i>yyyyMMddhhmmssSSS.log</i>
     */
    public static String getDefaultFilename() {
        return DATE_FMT.format(new Date()) + ".log";
    }
}
