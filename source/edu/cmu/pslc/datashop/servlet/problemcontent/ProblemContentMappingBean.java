/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;


import java.io.Serializable;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.dao.PcProblemDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 *  Problem Content Mapping Bean that implements runnable.  This bean will be
 *  spawned in a separate thread to map dataset problems with problem content.
 *
 * @author Cindy Tipper
 * @version $Revision: 11358 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2014-07-22 07:42:00 -0400 (Tue, 22 Jul 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemContentMappingBean implements Runnable, Serializable {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(ProblemContentMappingBean.class.getName());

    /** Dataset to map. */
    private DatasetItem dataset;
    /** Problem Content to map. */
    private PcConversionItem pcConversion;
    /** Problem Content Dataset Map item. */
    private PcConversionDatasetMapItem pcConversionDatasetMap;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Flag indicating this thread is running. */
    private boolean running;

    /** Cancel indicator. */
    private boolean cancelFlag;

    /**
     * Set transactionTemplate.
     * @param transactionTemplate the TransactionTemplate
     */
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Get transactionTemplate.
     * @return TransactionTemplate
     */
    public TransactionTemplate getTransactionTemplate() {
        return this.transactionTemplate;
    }

    /** Default constructor. */
    public ProblemContentMappingBean() {
        running = false;
        cancelFlag = false;
    }

    /**
     * Sets all the attributes need to create the export file.
     * @param dataset DatasetItem to be mapped
     * @param pcConversion the Problem Content to be mapped
     * @param pcConversionDatasetMap the Problem Content Dataset Map item
     */
    protected void setAttributes(DatasetItem dataset, PcConversionItem pcConversion,
                                 PcConversionDatasetMapItem pcConversionDatasetMap) {
        this.dataset = dataset;
        this.pcConversion = pcConversion;
        this.pcConversionDatasetMap = pcConversionDatasetMap;
    }

    /** Flag indicating if this build is running. @return boolean */
    public synchronized boolean isRunning() { return running; }

    /** set isRunning flag. @param running boolean of whether or not this thread is running. */
    public synchronized void setRunning(boolean running) { this.running = running; }

    /** Returns the cancelFlag. @return the cancelFlag */
    public boolean isCancelFlag() { return cancelFlag; }

    /** Stops running this bean. */
    public void stop() { cancelFlag = true; }

    /**
     * Starts this bean running.
     */
    public void run() {
        String prefix =
            "run(" + dataset.getDatasetName() + " / " + pcConversion.getContentVersion() + "): ";

        Long numProblemsMapped = 0L;
        String status = PcConversionDatasetMapItem.STATUS_PENDING;
        try {
            setRunning(true);
            ProblemDao dao = DaoFactory.DEFAULT.getProblemDao();
            Long numProblems = dao.getNumUnmappedProblems(dataset);

            logger.info(prefix
                        + "Performing Problem Content mapping. Total problems: " + numProblems);
            BulkTransactionCallback btc = new BulkTransactionCallback();
            numProblemsMapped = (Long)transactionTemplate.execute(btc);
            if (numProblemsMapped == -1) {
                status = PcConversionDatasetMapItem.STATUS_ERROR;
                numProblemsMapped = 0L;
            } else {
                status = PcConversionDatasetMapItem.STATUS_COMPLETE;
            }
            logger.info(prefix
                        + "Problem Content mapping. Problems mapped: " + numProblemsMapped);
        } catch (Exception exception) {
            logger.error(prefix + "Caught Exception Trying to map Problem Content: ", exception);
            numProblemsMapped = 0L;
            status = PcConversionDatasetMapItem.STATUS_ERROR;
            setRunning(false);
        } finally {
            setRunning(false);
            pcConversionDatasetMap.setNumProblemsMapped(numProblemsMapped);
            pcConversionDatasetMap.setStatus(status);
            PcConversionDatasetMapDao pcConversionDatasetMapDao =
                DaoFactory.DEFAULT.getPcConversionDatasetMapDao();
            pcConversionDatasetMapDao.saveOrUpdate(pcConversionDatasetMap);
            logger.info(prefix + "ProblemContentMapping Bean thread stop");
        }
    }

    /**
     * Inner class created to manage the database transactions.
     */
    public class BulkTransactionCallback implements TransactionCallback {

        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());

        /** Default Constructor. */
        public BulkTransactionCallback() {}

        /**
         * Do a batch of problems at a time.
         * @param ts TransactionStatus
         * @return number of problem successfully mapped
         */
        public Object doInTransaction(TransactionStatus ts) {
            long numMapped = 0;

            boolean isTutorShop =
                (pcConversion.getConversionTool()).equals(PcConversionItem.TUTORSHOP_CONVERTER);

            try {
                ProblemDao problemDao = DaoFactory.DEFAULT.getProblemDao();
                List<ProblemItem> unmappedProblems = problemDao.getUnmappedProblems(dataset);

                PcProblemDao pcProblemDao = DaoFactory.DEFAULT.getPcProblemDao();
                for (ProblemItem item : unmappedProblems) {
                    boolean found = false;
                    PcProblemItem ppi =
                        pcProblemDao.findByNameAndConversion(item.getProblemName(), pcConversion);

                    if (ppi == null) { continue; }

                    if (isTutorShop) {
                        // For TutorShop, hierarchy must contain 'content version'.
                        String contentVersion = pcConversion.getContentVersion();
                        String hierarchy = problemDao.getHierarchy(item);

                        if (hierarchy.contains(contentVersion + ",")
                         || hierarchy.endsWith(contentVersion)) {
                            found = true;
                        } else {
                            continue;
                        }
                    } else {
                        found = true;
                    }

                    // Found matching PcProblem... update Problem.
                    if (found) {
                        item.setPcProblem(ppi);
                        problemDao.saveOrUpdate(item);
                        numMapped++;
                    }
                }
            } catch (Throwable exception) {
                logger.error("Failed to map problem content: ", exception);
                ts.setRollbackOnly();
                numMapped = -1;   // Used to indicate an error...
            }

            return new Long(numMapped);
        }

    } // end inner class BulkTransactionCallback
}