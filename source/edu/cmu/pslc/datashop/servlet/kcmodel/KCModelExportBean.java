/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetSystemLogDao;
import edu.cmu.pslc.datashop.dto.ExportCache;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.extractors.KCModelStepExportTask;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.export.AbstractExportBean;
import edu.cmu.pslc.datashop.servlet.export.ExportThread;

/**
 *  Export Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to build the export file.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15746 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-10 16:19:23 -0500 (Mon, 10 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelExportBean extends AbstractExportBean
                implements Runnable, Serializable, ExportThread {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(KCModelExportBean.class.getName());

    /** Dataset to export */
    private DatasetItem dataset;
    /** User exporting */
    private UserItem user;
    /** List of Skill Models to export */
    private List <SkillModelItem> skillModelList;
    /** MultiMap of the step to skill mapping */
    private MultiMap skillMapping;

    /** Cache to hold information particular to the export transaction*/
    private ExportCache cache;

    /** Number of batches processed */
    private int batchNumber = 0;

    /** Hibernate Session wrapped helper */
    private KCModelHelper modelHelper;

    /**
     * Sets exportHelper. @param exportHelper the KCModelHelper to be set to. */
    public void setModelHelper (KCModelHelper exportHelper) { this.modelHelper = exportHelper; }

    /** Gets exportHelper. @return KCModelHelper */
    public KCModelHelper getModelHelper() { return this.modelHelper; }

    /** magic 1000! */
    private static final int ONE_THOUSAND = 1000;

    /** Default constructor. */
    public KCModelExportBean() { }

    /**
     * Sets all the attributes needed to create the export file.
     * @param dataset DatasetItem to export.
     * @param skillModelList List of all skill models to export.
     * @param user the UserItem of the person doing the export for logging purposes.
     */
    public void setAttributes(DatasetItem dataset,
                              List <SkillModelItem> skillModelList,
                              UserItem user) {
        this.skillModelList = skillModelList;
        this.dataset = dataset;
        this.user = user;
    }

    /**
     * Returns the skillModelList.
     * @return the skillModelList
     */
    public List<SkillModelItem> getSkillModelList() {
        return skillModelList;
    }

    /**
     * Process a single back of steps
     * @param steps the steps to process.
     */
    private void processBatch(List steps) {
        String prefix = "processBatch(" + (String)user.getId() + "): ";

        StringBuffer textBuffer = new StringBuffer(BUFFER_SIZE);
        cache.init(); //reinitialize the cache before each batch to prevent memory buildup.
        Date start = null;
        if (logger.isDebugEnabled()) { start = new Date(); }

        for (int i = 0, n = steps.size(); i < n && !isCancelFlag(); i++) {
            StepExportRow row = (StepExportRow)steps.get(i);
            if (row != null) {
                textBuffer.append("\n");
                textBuffer.append(modelHelper.processStepRow(
                        row, skillMapping, cache, skillModelList));
            } else {
                logger.warn(prefix + "Null step row.");
            }
            setNumCompletedRows(getNumCompletedRows() + 1);
        }
        dumpToFile(textBuffer, true);
        batchNumber++;
        if (logger.isDebugEnabled()) {
            Long time = (new Date()).getTime() - start.getTime();
            logDebug(prefix, "Completed ", getNumCompletedRows(),
                    " transactions total, recent batch of ", BATCH_SIZE, " at rate of ",
                    (int)(BATCH_SIZE / (float)(time / ONE_THOUSAND)), " per second");
        }
    }

    /**
     * Starts this bean running.
     */
    public void run() {
        String prefix = "run(" + (String)user.getId() + "): ";
        try {
            setInitializing(true);
            setRunning(true);
            cache = new ExportCache();
            init();

            logger.info(prefix + "Performing Skill Model Export... " + getTemporaryFileName());

            log("Started export for");
            int numTotalRows = modelHelper.getStepExportSize(dataset).intValue();
            setNumTotalRows(numTotalRows);
            if (numTotalRows == 0) {
                logger.info(prefix + "Stopping thread as there are zero steps for this model");
                stop();
            } else {
                logDebug(prefix, "Total Steps : ", numTotalRows);
            }
            skillMapping = modelHelper.getStepSkillMap(dataset, skillModelList);

            setInitializing(false); //used to prevent divide by zeros.
            dumpToFile(modelHelper.getStepMappingHeaders(skillModelList), false);

            // If cache doesn't exist, populate it first and then use it.
            DatasetSystemLogDao dslDao = DaoFactory.DEFAULT.getDatasetSystemLogDao();
            if (dslDao.requiresCachedKcmStepExportGeneration(dataset)) {
                KCModelStepExportTask kcmTask = null;
                try {
                    kcmTask = new KCModelStepExportTask(dataset);

                    if (!kcmTask.isExportStarted()) {

                        // delete the old cached version
                        kcmTask.deleteKCModelStepExportRows(dataset);

                        kcmTask.writeKCModelStepExport();

                        kcmTask.logExportCompleted(true);
                    }
                } catch (Throwable throwable) {
                    kcmTask.logExportCompleted(false);
                    logDebug("Error occurred processing KC model export for"
                             + " dataset: " + dataset.getDatasetName(), throwable);
                } finally {
                    if (kcmTask != null) { kcmTask.cleanup(); }
                }
            }

            int offset = 0;
            List <StepExportRow> batch = new ArrayList <StepExportRow>();
            do {
                batch = modelHelper.getStepBatch(dataset, offset, BATCH_SIZE);
                processBatch(batch);
                offset += BATCH_SIZE;
            } while (batch.size() == BATCH_SIZE && !isCancelFlag());

            dumpToFile(new StringBuffer("\n\n"), true);

            if (isCancelFlag()) {
                logDebug(prefix, "Canceling export file build");
                setRunning(false);
                setInitializing(false);
                deleteTempFile();
                log("Canceled export for");
            } else {
                logDebug(prefix, "Done building export file");
                setNumCompletedRows(getNumTotalRows());
                log("Finished export for");

                //keep the thread alive while a call come back for the item.
                for (int i = 0; i < ONE_THOUSAND && !isExported(); i++) {
                        Thread.sleep(ONE_THOUSAND);
                }
            }

        } catch (Exception exception) {
            logger.error(prefix + "Caught Exception: ", exception);
            setNumTotalRows(-1);
            setHasError(true);
            setRunning(false);
            setInitializing(false);
            deleteTempFile();
            log("Failed to export");
        } finally {
            setRunning(false);
            logger.info(prefix + "Export Bean thread stop");
        }
    }

    /**
     * Write a message to the user log
     * @param message Message to start the user log with.
     */
    private void log(String message) {
        StringBuffer logString = new StringBuffer(message + " KCM(s)");
        for (Iterator <SkillModelItem> it = skillModelList.iterator(); it.hasNext();) {
            SkillModelItem model = it.next();
            logString.append("'" + model.getSkillModelName() + "' (" + model.getId() + ")");
            logString.append((it.hasNext()) ? ", " : "");
        }
        UserLogger.log(dataset, user, UserLogger.MODEL_EXPORT, logString.toString());
    }

}