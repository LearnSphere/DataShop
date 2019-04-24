/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.Date;

import edu.cmu.oli.log.client.ActionLog;
import edu.cmu.oli.log.client.SessionLog;
import edu.cmu.oli.log.client.StreamLogger;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * This logger will log to a OLI-style log database directly.
 * It will log to either the PSLC production server or the PSLC QA server
 * based on the parameter passed into the create method.  It can log to any
 * available OLI-style log database given the correct URL as well.
 * <p>
 * This does not create a file, see OliDiskLogger or FileLogger to create a file.
 *
 * @author Alida Skogsholm
 * @version $Revision: 6000 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-02-26 15:18:28 -0500 (Fri, 26 Feb 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliDatabaseLogger extends AbstractMessageLogger {

    /** For logging directly to a OLI logging database. */
    private StreamLogger streamLogger = null;

    /**
     * Create the message logger.  Need to indicate whether to log to the PSLC Production
     * logging server or not.  Pass in true for production, false for QA (testing).
     * @param prodFlag indicates whether to log to the production server
     * @return the newly created OLI database logger
     */
    public static OliDatabaseLogger create(boolean prodFlag) {
        return create(prodFlag, ENCODING_UTF8);
    }

    /**
     * Create the message logger.  Need to indicate whether to log to the PSLC Production
     * logging server or not.  Pass in true for production, false for QA (testing).
     * @param prodFlag indicates whether to log to the production server
     * @param encoding the encoding to be indicated at the top of the XML
     * @return the newly created OLI database logger
     */
    public static OliDatabaseLogger create(boolean prodFlag, String encoding) {
        if (prodFlag) {
            return create(PROD_URL, encoding);
        } else {
            return create(QA_URL, encoding);
        }
    }

    /**
     * Create the message logger and log to the given URL.
     * @param url indicates the URL for the logging server
     * @param encoding the encoding to be indicated at the top of the XML
     * @return the newly created OLI database logger
     */
    public static OliDatabaseLogger create(String url, String encoding) {
        OliDatabaseLogger msgLogger = null;

        StreamLogger streamLogger = new StreamLogger();
        streamLogger.setURL(url);
        msgLogger = new OliDatabaseLogger(streamLogger, encoding);

        return msgLogger;
    }

    /**
     * Constructor.
     * @param streamLogger the OLI stream logger
     * @param encoding the encoding to be indicated at the top of the XML
     */
    public OliDatabaseLogger(StreamLogger streamLogger, String encoding) {
        super(encoding);
        this.streamLogger = streamLogger;
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

        Boolean result = streamLogger.logActionLog(actionLog);
        if (!result.booleanValue()) {
            System.out.println("OliDiskLogger: log result " + result
                      + ", last error " + streamLogger.getLastError());
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
                streamLogger.logSessionLog(slog);
            } catch (Exception exception) {
                System.out.println("OliDatabaseLogger: Exception occurred while logging session."
                        + exception.getMessage());
                exception.printStackTrace();
            }
        } // end synchronized block

    } // end logSession method

    /**
     * Does nothing.
     */
    public void close() {
        //Do nothing.
    }
}
