/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.jdbc.UncategorizedSQLException;

import edu.cmu.pslc.datashop.dao.MessageDao;
import edu.cmu.pslc.datashop.dao.OliLogDao;
import edu.cmu.pslc.datashop.item.MessageItem;
import edu.cmu.pslc.datashop.item.OliLogItem;

/**
 * The abstract base class for the FileLoader and the LogLoader.
 *
 * @author Alida Skogsholm
 * @version $Revision: 15864 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2019-02-27 16:29:31 -0500 (Wed, 27 Feb 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AbstractLoader extends AbstractExtractor {

    /** Number of milliseconds (36 hours) for confidence that the data is complete. */
    public static final long CONFIDENCE_TIME = 129600000;

    /** Default constructor. */
    public AbstractLoader() {
    }

    /**
     * Check the necessary fields to make sure they are valid.
     * @param logger the logger to use to print warning messages for each problem
     * @param msgItem the message item to check for validity
     * @return indication of whether the message item is valid
     */
    protected boolean isValidMessageItem(Logger logger, MessageItem msgItem) {
        boolean validMessage = true;
        String warning = "";

        if (msgItem.getUserId() == null) {
            warning += "User Id cannot be null. ";
            validMessage = false;
        }
        if (msgItem.getSessionTag() == null) {
            warning += "Session Id cannot be null. ";
            validMessage = false;
        }
        if (msgItem.getContextMessageId() == null) {
            warning += "Context Message Id cannot be null. ";
            validMessage = false;
        }
        if (msgItem.getTime() == null) {
            warning += "Time cannot be null. ";
            validMessage = false;
        }
        // Checking for emojis in the message info let's us "continue on error"
        if (infoContainsEmoji(msgItem)) {
            warning += "Message info contains emoji(s). Ignoring. ";
            validMessage = false;
        }

        if (!validMessage) {
            logger.warn(warning + "Skipping this message: " + msgItem);
        }

        return validMessage;
    }

    /**
     * Constant. RegEx for emojis.
     *
     * Started with SO: https://stackoverflow.com/questions/28366172/check-if-letter-is-emoji
     * But realized the suggested regular expression was cutting off some mathematical
     * symbols (\\u2248, for instance). 
     * Using https://en.wikibooks.org/wiki/Unicode/Character_reference, opted to
     * cut-off at \\u2250 since there are a few mathematical symbols in the \\u224* range.
     *
     * Update: Extending range thru \\u22FF as the XOR symbol was being dropped.
     * Sigh: need circle \\u25E6 for OLI composite functions, e.g.,  "f o g"
     */
    private final String EMO_REGEX = "([\\u2300-\\u25e5\\u25e7-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";

    private Boolean infoContainsEmoji(MessageItem msgItem) {
        Boolean result = false;

        Matcher matcher = Pattern.compile(EMO_REGEX).matcher(msgItem.getInfo());
        if (matcher.find()) {
            result = true;
        }

        return result;
    }

    /**
     * The number of times to sleep in case of a BindException.
     * If the machine is too fast, then it could start opening connections
     * faster than it can close them, which will eventually cause the
     * UncategorizedSQLException with a nested BindException.
     */
    private static final int NUM_TRIES = 4;

    /**
     * The amount of time in milliseconds to sleep if the nested BindException occurs.
     */
    private static final int MILLISECONDS_TO_SLEEP = 30000;

    /**
     * Save a single MessageItem using the MessageDao.
     * @param logger the logger to use for warning and/or error messages
     * @param dao the DAO to use to save
     * @param item as MessageItem to be saved
     * @return whether the item was valid and saved or not
     */
    protected boolean saveMessageItem(Logger logger, MessageDao dao, MessageItem item) {
        boolean validAndSavedFlag = false;
        // If the message is not valid, the isValidMessageItem method will log any/all the problems.
        if (isValidMessageItem(logger, item)) {
            item.setImportedTime(new Date());
            for (int idx = 0; idx < NUM_TRIES; idx++) {
                try {
                    dao.saveOrUpdate(item);
                    validAndSavedFlag = true;
                    break;
                } catch (UncategorizedSQLException exception) {
                    try {
                        logger.error("Sleeping " + MILLISECONDS_TO_SLEEP + " milliseconds due to "
                                + exception.getClass().getName());
                        Thread.sleep(MILLISECONDS_TO_SLEEP);
                        logger.error("Awake again");
                    } catch (InterruptedException e) {
                        logger.debug("InterruptedException occurred while sleeping.");
                    }
                }
            } // end for loop
        }
        return validAndSavedFlag;
    }

    /**
     * Save a single OliLogItem using the MessageDao.
     * @param logger the logger to use for warning and/or error messages
     * @param dao the DAO to use to save
     * @param item as OliLogItem to be saved
     * @return whether the item was saved or not
     */
    protected boolean saveOliLogItem(Logger logger, OliLogDao dao, OliLogItem item) {
        boolean savedFlag = false;
        item.setImportedTime(new Date());
        for (int idx = 0; idx < NUM_TRIES; idx++) {
            try {
                dao.saveOrUpdate(item);
                savedFlag = true;
                break;
            } catch (UncategorizedSQLException exception) {
                try {
                    logger.error("Sleeping " + MILLISECONDS_TO_SLEEP + " milliseconds due to "
                            + exception.getClass().getName());
                    Thread.sleep(MILLISECONDS_TO_SLEEP);
                    logger.error("Awake again");
                } catch (InterruptedException e) {
                    logger.debug("InterruptedException occurred while sleeping.");
                }
            }
        } // end for loop
        return savedFlag;
    }
}
