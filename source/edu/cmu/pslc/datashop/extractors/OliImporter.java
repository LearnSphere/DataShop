/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.OliDiscussionDao;
import edu.cmu.pslc.datashop.dao.OliFeedbackDao;
import edu.cmu.pslc.datashop.item.OliDiscussionItem;
import edu.cmu.pslc.datashop.item.OliFeedbackItem;

/**
 * Queries the OLI log tables for feedback data and then
 * populates the analysis_db's oli_feedback table.
 * TODO Note that the stars and hidden flag are not correct.  These
 * are logged in different messages than the post message we are
 * currently retrieving.  This should be fixed in the future.
 * TODO Note that the admit code is not filled in.  This should be fixed
 * in the future.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2959 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-06-19 16:19:40 -0400 (Mon, 19 Jun 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class OliImporter {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     */
    public OliImporter() {
    }

    /**
     * Save the data in the list.
     * @param itemList a list of OliFeedbackItem objects
     */
    private void saveFeedbackData(List itemList) {

        OliFeedbackDao dao = DaoFactory.DEFAULT.getOliFeedbackDao();

        for (Iterator iter = itemList.iterator(); iter.hasNext();) {
            OliFeedbackItem item = (OliFeedbackItem)iter.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Feedback Item: " + item);
            }
            dao.saveOrUpdate(item);
        }

    }

    /**
     * Save the data in the list.
     * @param itemList a list of OliDiscussionItem objects
     */
    private void saveDiscussionData(List itemList) {

        OliDiscussionDao dao = DaoFactory.DEFAULT.getOliDiscussionDao();

        for (Iterator iter = itemList.iterator(); iter.hasNext();) {
            OliDiscussionItem item = (OliDiscussionItem)iter.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Discussion Item: " + item);
            }
            dao.saveOrUpdate(item);
        }

    }

    /**
     * Query the OLI log tables for feedback data then
     * populate the the analysis database's OLI feedback table.
     */
    public void execute() {
        // Get and store feedback data from the OLI logs
        logger.info("Feedback...");

        OliFeedbackDao feedbackDao = DaoFactory.DEFAULT.getOliFeedbackDao();
        List feedbackList = feedbackDao.getFeedbackData();
        logger.info("...found " + feedbackList.size() + " items to import");

        saveFeedbackData(feedbackList);
        logger.info("Importing feedback data done.");

        // Get and store discussion data from the OLI logs
        logger.info("Discussion...");
        OliDiscussionDao discussionDao = DaoFactory.DEFAULT.getOliDiscussionDao();

        List discussionList = discussionDao.getDiscussionData();
        logger.info("...found " + discussionList.size() + " items to import");

        saveDiscussionData(discussionList);
        logger.info("Importing discussion data done.");
    }

    /**
     * The main method for this utility.
     * USAGE: java -classpath ... OliImporter
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("OliImporter.main");
        logger.info("OliImporter starting...");
        try {
            // Create an instance of a converter
            OliImporter importer = new OliImporter();
            importer.execute();

        } catch (Throwable throwable) {
            logger.error("Unknown error in main method.", throwable);
        } finally {
            logger.info("OliImporter done.");
        }
    }
}
