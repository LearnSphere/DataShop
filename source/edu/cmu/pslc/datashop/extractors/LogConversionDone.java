/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_LOG_CONVERSION_DONE;

/**
 * This class submits a 'log conversion done' action to the dataset_system_log
 * table.  This action is submitted after LFA runs during log conversion and is
 * used by the CFG to determine when it can start to cache files.
 *
 * @author Kyle Cunningham
 * @version $Revision: 5718 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-08-27 14:43:13 -0400 (Thu, 27 Aug 2009) $
 * <!-- $KeyWordsOff: $ -->
 */

public final class LogConversionDone {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Constructor.
     */
    private LogConversionDone() { };

    /**
     * Submit a 'log conversion done' action to the dataset_system_log table.
     * Since this table requires a dataset_id, use min(dataset_id) in order to
     * hack it.
     */
    private void logIt() {
        DatasetDao dao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dao.findDatasetWithMinId();
        String info = "Please ignore my dataset id.";
        SystemLogger.log(dataset, ACTION_LOG_CONVERSION_DONE, info, true);
        logger.info("Log conversion done message has been logged.");
    }

    /**
     * Main!
     * @param args command line arguments.
     */
    public static void main(String ...args) {
        LogConversionDone done = new LogConversionDone();
        done.logIt();
    }
}
