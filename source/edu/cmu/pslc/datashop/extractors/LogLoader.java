package edu.cmu.pslc.datashop.extractors;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dao.OliLogDao;
import edu.cmu.pslc.datashop.dto.UserSession;
import edu.cmu.pslc.datashop.extractors.ffi.ImportQueue;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.OliLogItem;
import edu.cmu.pslc.datashop.oli.dao.LogActionDao;
import edu.cmu.pslc.datashop.oli.dao.LogSessionDao;
import edu.cmu.pslc.datashop.oli.dao.OliDaoFactory;
import edu.cmu.pslc.datashop.oli.item.LogActionItem;
import edu.cmu.pslc.datashop.util.VersionInformation;
import edu.cmu.pslc.datashop.xml.XmlParser;
import edu.cmu.pslc.datashop.xml.XmlParserFactory;
import edu.cmu.pslc.logging.util.DateTools;

/**
 * This utility is used to retrieve log data from the OLI log database.
 *
 * @author Alida Skogsholm
 * @version $Revision: 11403 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-07-30 15:35:40 -0400 (Wed, 30 Jul 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LogLoader extends AbstractLoader {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Number of actions to save in a single shot */
    private static final int BULK_MESSAGE_SIZE = 1000;

    /** Number of milliseconds in a second */
    private static final int ONE_THOUSAND = 1000;

    /** Import source for messages. */
    public static final String LOG_LOADER_SOURCE = "log_loader";

    /** Default constructor. */
    public LogLoader() {
    }

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
    public void setTransactionTemplate (TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /**
     * Queries the log database tables
     * and populates the analysis_db.oli_log table.
     * Queries once for valid session IDs with rows in the log session table,
     * and then queries again for those without and uses the session id itself
     * as the user id in this case where it is missing.
     */
    public void loadLogData() {
        try {
            // Get a distinct set of user/session pairs and then get the
            // associated log action data.
            logger.info("LogLoader Starting query with user id.");
            LogSessionDao sessionDao =  OliDaoFactory.HIBERNATE.getLogSessionDao();
            Collection<UserSession> itemList = sessionDao.getDistinctUserSessions();
            for (UserSession userSession : itemList) {
                try {
                    if (userSession != null
                            && userSession.getUserId() != null
                            && userSession.getSessionId() != null
                            && userSession.getUserId().length() > 0
                            && userSession.getSessionId().length() > 0) {
                        logger.debug("Executing query for session " + userSession);
                        List list = doQuery(userSession);
                        parseAndSave(list);
                    } else {
                        logger.info("Skipping user/session " + userSession);
                    }
                } catch (Throwable throwable) {
                    sendErrorEmail(logger, "Error loading log data for user session "
                            + userSession, throwable);
                }
            }

            // Get the log data does not have a session logged.
            logger.info("LogLoader Starting query without a user id.");
            List list = doQuery();
            parseAndSave(list);

        } catch (Throwable throwable) {
            // Log error and send email if this happens!
            String msg = "Unknown error in loadLogData method.";
            logger.error(msg, throwable);
            sendErrorEmail(logger, msg, throwable);
        }
    }

    /**
     * Queries the log database tables
     * and populates the analysis_db.oli_log table.
     * @param sessionData object holding both user id and session id
     * @return a list of ActionData objects
     */
    private List doQuery(UserSession sessionData) {
        List actionList = new ArrayList();

        // Make sure the given is not null.
        if (sessionData == null) { return null; }

        String userId = sessionData.getUserId();
        String sessionId = sessionData.getSessionId();

        LogActionDao actionDao = OliDaoFactory.HIBERNATE.getLogActionDao();
        Collection itemList = actionDao.getBySessionId(sessionId);

        int idx = 0;

        for (Iterator iter = itemList.iterator(); iter.hasNext();) {
            LogActionItem item = (LogActionItem)iter.next();

            if (item.getTime() != null) {
                ActionData actionData = new ActionData(userId, item);
                actionList.add(actionData);
                idx++;
            } else {
                logger.warn("Marking log action as ERROR, time stamp is null, guid: "
                        + item.getId());
                OliLogDao oliLogDao = DaoFactory.DEFAULT.getOliLogDao();
                OliLogItem oliLogItem = new OliLogItem();
                oliLogItem.setId((String)item.getId());
                oliLogItem.setImportedFlag(MessageItem.ERROR_FLAG);
                oliLogItem.setServerReceiptTime(item.getServerReceiptTime());

                if (!saveOliLogItem(logger, oliLogDao, oliLogItem)) {
                    logger.error("OliLogItem was not saved: " + oliLogItem);
                }
            }
        } // end while

        if (idx == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved " + idx
                + " items from log.log_act into datashop's oli_log for session "
                + sessionData);
            }
        } else {
            logger.info("Retrieved " + idx
            + " items from log.log_act into datashop's oli_log for session " + sessionData);
        }

        return actionList;

    } // end doQuery

    /**
     * Queries the log database tables
     * and populates the analysis_db.oli_log table.
     * @return a list of ActionData objects
     */
    private List doQuery() {
        List actionList = new ArrayList();

        LogActionDao actionDao = OliDaoFactory.HIBERNATE.getLogActionDao();

        Collection itemList = actionDao.getWithoutUser();

        int idx = 0;

        for (Iterator iter = itemList.iterator(); iter.hasNext();) {
            LogActionItem item = (LogActionItem)iter.next();

            if (item.getTime() != null) {
                //Let's stop retrieving these actions.
                //ActionData actionData = new ActionData(item.getSessionId(), item);
                //actionList.add(actionData);
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring log action, no session found, guid: "
                        + item.getId());
                }
                idx++;
            } else {
                logger.warn("Marking log action as ERROR, time stamp is null, guid: "
                        + item.getId());
                OliLogDao oliLogDao = DaoFactory.DEFAULT.getOliLogDao();
                OliLogItem oliLogItem = new OliLogItem();
                oliLogItem.setId((String)item.getId());
                oliLogItem.setImportedFlag(MessageItem.ERROR_FLAG);
                oliLogItem.setServerReceiptTime(item.getServerReceiptTime());

                if (!saveOliLogItem(logger, oliLogDao, oliLogItem)) {
                    logger.error("OliLogItem was not saved: " + oliLogItem);
                }
            }
        } // end while

        if (idx > 0) {
            logger.warn("Found " + idx
                + " items in log.log_act with missing session.");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Found " + idx
                    + " items in log.log_act with missing session.");
            }
        }
        return actionList;

    } // end doQuery

    /**
     * Parse each ActionData object in the list and save the messages to the database.
     * @param actionList a list of ActionData objects
     */
    private void parseAndSave(List actionList) {

        int actionCounter = 0;
        List bulkActionList = new ArrayList();
        Date startTime = new Date();

        for (Iterator actionIter = actionList.iterator(); actionIter.hasNext();) {
            ActionData actionData = (ActionData)actionIter.next();

            bulkActionList.add(actionData);
            actionCounter++;
            if (actionCounter > BULK_MESSAGE_SIZE) {
                bulkSave(bulkActionList);
                actionCounter = 0;
                bulkActionList.clear();
            }
        } // end for loop
        bulkSave(bulkActionList);
        if (actionList.size() > 0) {
            Date end = new Date();
            long time = end.getTime() - startTime.getTime();
            DecimalFormat df = new DecimalFormat("#0.0");
            double average = (double)actionList.size() / ((double)time / (double)ONE_THOUSAND);
            logger.info("Finished.  Averaged " + df.format(average) + " items per second");
        }
    } // end parseAndSave

    /**
     * Call the transaction to save all the action/message items in a single shot
     * @param actionList list of all actions to save in a single shot.
     */
    private void bulkSave(List actionList) {
        BulkLogLoadCallback btc = new BulkLogLoadCallback(actionList);
        Boolean successFlag = (Boolean)transactionTemplate.execute(btc);
        if (!successFlag.booleanValue()) {
            logger.error("Something bad happened in the bulk save.");
        }
    }

    /**
     * Display the usage of this utility.
     */
    protected void displayUsage() {
        System.err.println("\nUSAGE: java -classpath ..."
            + " LogLoader "
            + " [-email address]");
        System.err.println("Option descriptions:");
        System.err.println("\t-e, email           \t send email if major failure");
        System.err.println("\t-h, help           \t this help message ");
    }

    /**
     * Handle the command line arguments.
     * @param args - command line arguments passed into main
     * @return returns null if no exit is required,
     * 0 if exiting successfully (as in case of -help),
     * or any other number to exit with an error status
     */
    protected Integer handleOptions(String[] args) {
        // The value is null if no exit is required,
        // 0 if exiting successfully (as in case of -help),
        // or any other number to exit with an error status
        Integer exitLevel = null;
        if (args != null && args.length != 0) {

            java.util.ArrayList argsList = new java.util.ArrayList();
            for (int i = 0; i < args.length; i++) {
                argsList.add(args[i]);
            }

            // loop through the arguments
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-h")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-help")) {
                    displayUsage();
                    exitLevel = 0;
                } else if (args[i].equals("-e") || args[i].equals("-email")) {
                    setSendEmailFlag(true);
                    if (++i < args.length) {
                        setEmailAddress(args[i]);
                    } else {
                        System.err.println(
                            "Error: a email address must be specified with this argument");
                        displayUsage();
                        exitLevel = 1;
                    }
                } else {
                    System.err.println("Error: improper command line arguments: " + argsList);
                    displayUsage();
                    exitLevel = 1;
                } // end if then else

                // If the exitLevel was set, then break out of the loop
                if (exitLevel != null) {
                    break;
                }
            } // end for loop
        }
        return exitLevel;
    } // end handleOptions

    /**
     * Main.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("LogLoader.main");
        String version = VersionInformation.getReleaseString();
        logger.info("LogLoader starting (" + version + ")...");
        LogLoader logLoader = ExtractorFactory.DEFAULT.getLogLoader();

        boolean playMode = ImportQueue.isInPlayMode();
        // If an exitLevel exists, it will be used to exit
        // before the agg is run; otherwise exitLevel is null so continue.
        Integer exitLevel = null;
        // Handle the command line options:
        // The handleOptions method is called before entering the try-catch block
        // because it isn't affected by the ImportQueue.
        exitLevel = logLoader.handleOptions(args);
        try {
            // If exitLevel is null, then proceed with aggregation
            if (exitLevel == null) {
                // Pause the IQ
                if (playMode) {
                    logger.info("main:: PUnpausing the ImportQueue.");
                    ImportQueue.pause();
                }
                // Convert the info field data from XML to a database table.
                logLoader.loadLogData();
            }

        } catch (Throwable throwable) {
            // Log error and send email if this happens!
            logLoader.sendErrorEmail(logger, "Unknown error in main method.", throwable);
            exitLevel = 1;
        } finally {
            if (playMode) {
                logger.info("main:: Unpausing the ImportQueue.");
                ImportQueue.play();
            }
            exitOnStatus(exitLevel);
            logger.info("LogLoader done.");
        }
    }

    /**
     * Inner class to hold the data from the log_act and log_sess OLI tables in the log database.
     */
    private class ActionData {
        /** User id. */
        private final String userId;
        /** Session id. */
        private final LogActionItem logActionItem;
        /** Time in the format (yyyy-mm-dd hh:mm:ss.x). */
        private final String timeString;

        /**
         * Constructor.
         * @param userId user id
         * @param logActionItem the LogActionItem
         */
        public ActionData(String userId,
                          LogActionItem logActionItem) {
            this.userId = userId;
            this.logActionItem = logActionItem;
            this.timeString = DateTools.getTimeStringWithTimeZone(logActionItem.getTime());
        }

         /**
          * Returns a string representing this object.
          * Used for debugging.
          * @return String
          */
        public String toString() {
            return userId
                + " : " + timeString
                + " : " + logActionItem;
        }
    } // end inner class ActionData

    /**
     * Inner class to bulk update for speed.
     */
    private class BulkLogLoadCallback implements TransactionCallback {

        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** List of tool-tutor pairs. */
        private List actionList;


        /**
         * Constructor.
         * @param actionList List of ActionItems to save
         *
         */
        public BulkLogLoadCallback(List actionList) {
            this.actionList = actionList;
        }

        /** This is the string that is in the bad logs. */
        private static final String BAD_STRING =
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
          + "xsi:noNamespaceSchemaLocation='http://learnlab.web.cmu.edu/dtd/tutor_message_v4.xsd' "
          + "version_number=\"4\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        /** This is the replacement string for the bad logs. */
        private static final String GOOD_STRING =
            "version_number=\"4\">";

        /**
         * Do a bunch of messages at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {

            List messageList = null;
            MessageDao messageDao = DaoFactory.DEFAULT.getMessageDao();
            OliLogDao oliLogDao = DaoFactory.DEFAULT.getOliLogDao();

            try {
                for (int i = 0, n = actionList.size(); i < n; i++) {
                    ActionData actionData = (ActionData)actionList.get(i);
                    String infoData = actionData.logActionItem.getInfo();

                    XmlParser parser = null;
                    String importedFlag = MessageItem.SUCCESS_FLAG;

                    if (infoData != null && infoData.length() > 0 && !infoData.equals("(null)")) {
                        if (infoData.contains(BAD_STRING)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("FOUND LOG with bad XML and fixing it, guid: "
                                             + actionData.logActionItem.getId());
                            }
                            infoData = infoData.replace(BAD_STRING, GOOD_STRING);
                        }
                        parser = XmlParserFactory.getInstance().get(infoData);
                        importedFlag = null;
                        if (parser == null) {
                            logger.warn("Parser is null for GUID: "
                                    + actionData.logActionItem.getId());
                            importedFlag = MessageItem.ERROR_FLAG;
                            messageList = new ArrayList();
                        }
                    } else {
                        logger.warn("Info field is null or invalid for GUID: "
                                + actionData.logActionItem.getId());
                        importedFlag = MessageItem.ERROR_FLAG;
                        messageList = new ArrayList();
                    }

                    if (infoData != null && parser != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Parser is valid for GUID: "
                                    + actionData.logActionItem.getId());
                        }
                        parser.setUserId(actionData.userId.trim());
                        parser.setSessionId(actionData.logActionItem.getSessionId());
                        parser.setTimeString(actionData.timeString);
                        parser.setTimeZone(actionData.logActionItem.getTimeZone());

                        messageList = parser.getMessageItems();
                    }

                    // Need to keep going even if one of these fail. Be sure to log the GUID of the
                    // item that is failing so that it can be fixed.
                    try {
                        for (int j = 0, m = messageList.size(); j < m; j++) {
                            MessageItem msgItem = (MessageItem)messageList.get(j);
                            msgItem.setGuid((String)actionData.logActionItem.getId());
                            msgItem.setServerReceiptTime(
                                    actionData.logActionItem.getServerReceiptTime());
                            msgItem.setImportSource(LOG_LOADER_SOURCE);

                            if (msgItem.getMessageType().equals(
                                    MessageItem.MSG_TYPE_PLAIN_MESSAGE)) {
                                importedFlag = MessageItem.IGNORE_FLAG;
                            } else {
                                if (saveMessageItem(logger, messageDao, msgItem)) {
                                    importedFlag = MessageItem.SUCCESS_FLAG;
                                } else {
                                    importedFlag = MessageItem.ERROR_FLAG;
                                }
                            }
                        }

                        OliLogItem oliLogItem = new OliLogItem();
                        oliLogItem.setId((String)actionData.logActionItem.getId());
                        oliLogItem.setImportedFlag(importedFlag);
                        oliLogItem.setServerReceiptTime(
                                actionData.logActionItem.getServerReceiptTime());

                        if (!saveOliLogItem(logger, oliLogDao, oliLogItem)) {
                            logger.error("OliLogItem was not saved: " + oliLogItem);
                        }

                    } catch (Exception exception) {
                        logger.warn("Exception occurred saving message items for GUID : "
                                + actionData.logActionItem.getId(), exception);
                    }
                }
            } catch (Throwable exception) {
                logger.error(exception.toString(), exception);
                ts.setRollbackOnly();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    } // end inner class BulkTransactionCallback

} // end class LogLoader