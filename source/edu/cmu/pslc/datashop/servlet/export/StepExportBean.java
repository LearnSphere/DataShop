/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.export;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SampleMetricDao;
import edu.cmu.pslc.datashop.dto.StepInfo;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoReportHelper.StepSummaryBatcher;

import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 *  Export Bean that exports the Step List table show in the Dataset Info
 *  Report Overview. It is the list of all steps in the given dataset.
 *  It implements runnable.
 *  This bean will be spawned in a separate thread to build the export file.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10885 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-04-09 09:01:12 -0400 (Wed, 09 Apr 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class StepExportBean  extends AbstractExportBean
        implements Runnable, Serializable, ExportThread {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(StepExportBean.class.getName());

    /** Dataset to export */
    private DatasetItem dataset;

    /** Hibernate Session wrapped helper */
    private DatasetInfoReportHelper reportHelper;

    /** Hibernate Session wrapped helper */
    private SampleMetricDao sampleMetricDao;

    /** Default constructor. */
    public StepExportBean() { }

    /**
     * Sets all the attributes need to create the export file.
     * @param dataset DatasetItem to export.
     */
    public void setAttributes(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Starts this bean running.
     */
    public void run() {
        StepSummaryBatcher batcher = null;

        try {
            init();
            logDebug("Performing Step Export... ", getTemporaryFileName());
            setInitializing(true);
            setRunning(true);
            reportHelper = HelperFactory.DEFAULT.getDatasetInfoReportHelper();
            sampleMetricDao = DaoFactory.DEFAULT.getSampleMetricDao();

            Long numRows = sampleMetricDao.getTotalUniqueSteps(dataset);
            setNumTotalRows(numRows.intValue());

            logDebug("Total Rows : ", getNumTotalRows());
            setInitializing(false); //used to prevent divide by zeros.

            String hdrRow = "Step List for Dataset: " + dataset.getDatasetName() + "\n"
                + join("\t", "Row", "Problem Hierarchy", "Problem Name", "Step Name");
            dumpToFile(hdrRow, false);

            batcher = reportHelper.getStepSummaryBatcher(dataset);

            int numOfBatches = 0;

            while (!batcher.done()) {
                if (isCancelFlag()) { break; }

                List<StepInfo> stepList = batcher.getNextBatch(BATCH_SIZE, numOfBatches);

                logDebug("Retrieved ", stepList.size(), " steps, starting at number ",
                        getNumCompletedRows());
                dumpToFile("\n" + join("\n", stepList), true);
                incrementNumCompletedRows(stepList.size());
                logDebug("Completed ", getNumCompletedRows(), " rows of ", getNumTotalRows());
                numOfBatches++;
            }

            if (isCancelFlag()) {
                logDebug("Canceling export file build");
                setRunning(false);
                setInitializing(false);
                deleteTempFile();
            } else {
                logDebug("Done building export file");
                setNumCompletedRows(getNumTotalRows());
                //keep the thread alive while a call come back for the item.
                for (int i = 0; i < SLEEP_TIME && !isExported(); i++) {
                    Thread.sleep(SLEEP_TIME);
                }
            }
        } catch (Exception exception) {
            logger.error("Caught Exception: ", exception);
            setNumTotalRows(-1);
            setHasError(true);
            setRunning(false);
            setInitializing(false);
            deleteTempFile();
        } finally {
            try {
                if (batcher != null) {
                    batcher.cleanUp();
                }
            } catch (SQLException sqle) {
                logger.error("Exception trying to clean up batcher", sqle);
            }
            setRunning(false);
            logger.debug("Thread stop.");
        }
    }
}