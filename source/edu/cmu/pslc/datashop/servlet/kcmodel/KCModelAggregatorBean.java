/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;

import static edu.cmu.pslc.datashop.helper.SystemLogger.ACTION_AGGREGATE_KC_MODEL;
import static edu.cmu.pslc.datashop.item.SkillModelItem.STATUS_READY;
import static edu.cmu.pslc.datashop.util.StringUtils.join;
import static java.lang.String.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SampleDao;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.StepRollupDao;
import edu.cmu.pslc.datashop.helper.SystemLogger;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.learningfactor.AbstractLearningFactor;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 *  Aggregator Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to keep alive the aggregation process.  Spring injects
 *  the transactionTemplate on creation, and wraps the bean in a hibernate interceptor.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 15763 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-16 15:57:08 -0500 (Sun, 16 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelAggregatorBean extends AbstractLearningFactor
            implements Runnable, Serializable {

    /** Debug logging. */
    private static Logger logger =
        Logger.getLogger("edu.cmu.pslc.datashop.servlet.KCModelAggregatorBean");

    /** Static flag indicating whether to output the SSSS when running AFM. */
    private static final boolean OUTPUT_SSSS_FLAG = true;

    /** Flag indicating this thread is running. */
    private volatile boolean running;
    /** Flag indicating the data for this thread are initializing. */
    private volatile boolean initializing;
    /** Flag indicating the bean should cancel. */
    private volatile boolean isCanceled;

    /** Status type. */
    public static final String STATUS_FINISHED = "FINISHED";
    /** Status type. */
    public static final String STATUS_QUEUED = "QUEUED";
    /** Status type. */
    public static final String STATUS_RUNNING = "RUNNING";
    /** Status type. */
    public static final String STATUS_ERROR = "ERROR";
    /** Status type. */
    public static final String STATUS_CANCELED = "CANCELED";

    /** Total number of students to aggregate. */
    private int total;
    /** # of students processed. */
    private int completed;

    /** current status of this thread. */
    private String lfaStatus;
    /** current status of the aggregation. */
    private String aggStatus;
    /** current status of the cross validation. */
    private String cvStatus;

    /** The location of the aggregator stored procedure file. */
    private String aggSpFilePath;

    /** Time at which the KC model aggregation started. */
    private long startTime;

    /** Used to customize the set of procedures for sample creation. */
    private static final String TO_INSERT_BASE = "KCM_";

    /** Skill models to aggregate. */
    private List <SkillModelItem> skillModelList;
    /** Dataset the skill models belong to. */
    private DatasetItem dataset;
    /** User for logging purposes. */
    private UserItem user;
    /** Directory for SSSS files. */
    private String ssssDir;

    /** Sample dao for getting items by sample. */
    private SampleDao sampleDao;
    /** SkillModel dao for manipulating the skill models. */
    private SkillModelDao skillModelDao;
    /** StepRollup dao for executing the aggregation. */
    private StepRollupDao stepRollupDao;

    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /**
     * Sets transactionTemplate.
     * @param transactionTemplate the TransactionTemplate to be set to.*/
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * Gets transactionTemplate.
     * @return TransactionTemplate*/
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    /**
     * Stop this thread, delete the imported skill models.
     */
    public void stop() { isCanceled = true; }

    /** magic 100! */
    private static final int MAGIC_ONE_HUNDRED = 100;

    /** Default constructor. */
    public KCModelAggregatorBean() {
        running = false;
        isCanceled = false;
        total = 0;
        completed = 0;
        lfaStatus = STATUS_QUEUED;
        aggStatus = STATUS_QUEUED;
        cvStatus = STATUS_QUEUED;
    }

    /**
     * Sets all the attributes need to aggregate.
     * @param dataset the dataset we are aggregating for.
     * @param skillModelList the list of skill models to aggregate.
     * @param user the current user that imported a KCM for logging purposes.
     * @param aggSpFilePath the path to the aggregator stored procedure file.
     * @param ssssDir directory for SSSS files
     */
    protected void setAttributes(DatasetItem dataset, List <SkillModelItem> skillModelList,
            UserItem user, String aggSpFilePath, String ssssDir) {
        this.dataset = dataset;
        this.skillModelList = skillModelList;
        this.user = user;
        this.aggSpFilePath = aggSpFilePath;
        this.ssssDir = ssssDir;
    }

    /**
     * Get the percentage complete of the KC model import.
     * @return the percentage (0-100) complete
     */
    public synchronized int getPercent() {
        String prefix = "getPercent(" + (String)user.getId() + "): ";
        if (isInitializing()) {
            logDebug(prefix, "getPercent :: still initializing, returning 0");
            return 0;
        }

        if (total == completed && total > 0) {
            return MAGIC_ONE_HUNDRED;
        } else if (total > 0) {
            int percent =  (completed * MAGIC_ONE_HUNDRED) / total;
            logDebug(prefix, "Current percent = ", percent);
            return percent;
        } else if (lfaStatus.equals(STATUS_RUNNING)) {
            logDebug(prefix, "AFM in progress, no percent available yet.");
            return 0;
        } else if (cvStatus.equals(STATUS_RUNNING)) {
            logDebug(prefix, "CV in progress, no percent available yet.");
            return 0;
        } else {
            logger.info(prefix + "There was an error in the thread.");
            return -1;
        }
    }

    /** Get the current status of the LFA. @return the lfaStatus as a String */
    public synchronized String getLFAStatus() { return lfaStatus; }

    /**
     * Get the current status of the aggregation.
     * @return the aggStatus as a string
     */
    public synchronized String getAggStatus() { return aggStatus; }

    /**
     * Get the current status of the cross validation.
     * @return the cvStatus as a String
     */
    public synchronized String getCvStatus() { return cvStatus; }

    /**
     * Flag indicating if this build has completed.
     * @return boolean
     */
    public synchronized boolean isCompleted() { return completed == total; }

    /**
     * Flag indicating if this build is initializing.
     * @return boolean
     */
    public synchronized boolean isInitializing() { return initializing; }

    /**
     * set the flag indicating if this build is initializing.
     * @param init whether or not this is initializing.
     */
    public synchronized void setIntitializing(boolean init) { initializing = init; }

    /**
     * Flag indicating if this build is running.
     * @return boolean
     */
    public synchronized boolean isRunning() { return running; }

    /**
     * set the flag indicating if this build is running.
     * @param running whether or not this is running.
     */
    public synchronized void setRunning(boolean running) { this.running = running; }

    /**
     * Ensure that only one thread is aggregating at a time.
     */
    private static final Object syncObject = new Object();

    /**
     * Default numCvRuns is 20 but not necessary in day-to-day use. For KC
     * model imports via UI (or webservices) a single run is sufficient.
     */
    private static final Integer NUM_CV_RUNS = 1;

    /**
     * Starts this bean running.
     * Note that LFA now runs after aggregation (DS792).
     */
    public void run() {
        List<SampleItem> sampleList = null;
        String prefix = "run(" + (String)user.getId() + "): ";

        // Trac 588. Do this work as a single atomic unit.
        synchronized (syncObject) {
            try {
                logger.info(prefix + "Performing Data Aggregation on Skill Models");
                aggStatus = STATUS_RUNNING;
                sampleDao = DaoFactory.DEFAULT.getSampleDao();
                skillModelDao = DaoFactory.DEFAULT.getSkillModelDao();
                stepRollupDao = DaoFactory.DEFAULT.getStepRollupDao();
                SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();

                setIntitializing(true);
                setRunning(true);

                if (skillModelList == null || skillModelList.isEmpty()) {
                    setRunning(false);
                    return;
                }

                this.startTime = System.currentTimeMillis();

                //get the list of all samples in the dataset.
                sampleList = sampleDao.find(dataset);
                total = sampleList.size() * skillModelList.size();
                
                if (total < 1) {
                    logger.warn(prefix
                                + "No samples with any students/transactions, stopping process.");
                    setIntitializing(false);
                    aggStatus = STATUS_ERROR;
                    total = -1;
                    setRunning(false);
                    return;
                } else {
                    logDebug(prefix,
                             "Aggregating ", sampleList.size(), " samples for the addition of ",
                             skillModelList.size(), " skill models.");
                }
                
                setIntitializing(false); //used to prevent divide by zeros.
                int numRowsCreated = 0;
                boolean loadSuccess = false;
                
                for (SampleItem sample : sampleList) {
                    if (isCanceled) { break; }
                    String toInsert = TO_INSERT_BASE + sample.getId();
                    loadSuccess =
                        stepRollupDao.loadAggregatorStoredProcedure(aggSpFilePath, toInsert);
                    if (loadSuccess) {
                        numRowsCreated = stepRollupDao.callAggregatorStoredProcedure(sample,
                            toInsert, createSkillModelSQLClause());
                        stepRollupDao.dropAll(sample, TO_INSERT_BASE);
                        logDebug(prefix, "Finished aggregation of sample ", sample.getSampleName(),
                                 " for the new KC Models.");
                    }
                    if (numRowsCreated < 0) {
                        logger.error(prefix + "There was an error processing the sample, "
                                     + sample.getSampleName() + " while aggregating for new"
                                     + " KC Model(s)");
                        total = -1;
                        break;
                    } else {
                        completed += skillModelList.size();
                        logDebug(prefix, "Created ", numRowsCreated, " stepRollups for sample ",
                                 sample.getSampleName(), "(", sample.getId(), ")");
                    }
                } // end for (sample)

                String logString = skillModelsLogMessage("Aggregated new skill model(s) ");
                for (SkillModelItem model : skillModelList) {
                    if (isCanceled) { break; }
                    model.setStatus(STATUS_READY);
                    int numSkills = skillDao.getNumSkills(model);
                    model.setNumSkills(numSkills);
                    skillModelDao.saveOrUpdate(model);
                }

                if (isCanceled) {
                    //TODO should be able to back all the way out? recover overwritten model?
                    logString = "Canceled import and deleted skill model(s) ";
                    
                    UserLogger.log(dataset, user, UserLogger.MODEL_IMPORT, logString, false);
                    aggStatus = STATUS_CANCELED;
                    lfaStatus = STATUS_CANCELED;
                    cvStatus = STATUS_CANCELED;
                } else {
                    aggStatus = STATUS_FINISHED;
                }

                long elapsedTime = System.currentTimeMillis() - startTime;
                SystemLogger.log(dataset, null, null, ACTION_AGGREGATE_KC_MODEL, logString,
                                 true, numRowsCreated, elapsedTime);

                // This needs to come after the aggregation. (DS792)
                logger.info(prefix + "Running LFA/CV on new/updated Skill Models");

                // Use smaller number of runs when importing via UI or webservices.
                setNumCVRun(NUM_CV_RUNS);

                lfaStatus = STATUS_RUNNING;
                cvStatus = STATUS_RUNNING;
                for (SkillModelItem skillModel : skillModelList) {
                    if (isCanceled) { break; }
                    runCalculators(dataset, skillModel,
                                   true, false, true, false, OUTPUT_SSSS_FLAG, ssssDir);
                }
                lfaStatus = STATUS_FINISHED;
                cvStatus = STATUS_FINISHED;
                
            } catch (Throwable throwable) {
                aggStatus = STATUS_ERROR;
                logger.error(prefix + "Oh boy, a throwable occurred while aggregating. "
                             + "Exiting and attempt to flag the KC models as errors", throwable);
                
                for (SkillModelItem model : skillModelList) {
                    model.setStatus(SkillModelItem.STATUS_ERROR);
                    skillModelDao.saveOrUpdate(model);
                }
                logSkillModels("ERROR aggregating new skill model(s) ", ACTION_AGGREGATE_KC_MODEL);
            } finally {
                setRunning(false);
                logger.info(prefix + "Model Aggregator Bean thread stop");
            }
        }
    }

    /**
     * Log a message along with the list of skill models for the action.
     * (See skillModelsLogMessage.)
     * @param message the message
     * @param action the action
     */
    private void logSkillModels(String message, String action) {
        UserLogger.log(dataset, user, action,
                skillModelsLogMessage(message), false);
    }

    /**
     * Build a log message by appending skill model names and IDs to the message parameter.
     * @param message the message to be logged
     * @return the list of skill model names and IDs appended to the message
     */
    private String skillModelsLogMessage(String message) {
        StringBuffer logString = new StringBuffer(message);
        logString.append(join(", ", new ArrayList<String>() { {
            for (SkillModelItem model : skillModelList) { add(model.getNameAndId()); }
        } }));
        return logString.toString();
    }

    /**
     * Generate the "in" clause needed to aggregate the new skill models.  This is used by the
     * aggregator stored procedure.
     * @return a partial SQL statement to pass to the stored procedure.
     */
    private String createSkillModelSQLClause() {
        final ArrayList<String> skillModelIds = new ArrayList<String>() { {
            for (SkillModelItem skm : skillModelList) { add(skm.getId().toString()); }
        } };
        return format(" sk.skill_model_id IN (%s)", join(", ", skillModelIds));
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }
}
