/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.SkillDao;
import edu.cmu.pslc.datashop.dao.SkillModelDao;
import edu.cmu.pslc.datashop.dao.SubgoalDao;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.helper.DatasetCreator;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.SubgoalItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;

/**
 *  Import Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to import a sepecified file and report progress.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13099 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-04-14 12:44:17 -0400 (Thu, 14 Apr 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelImportBean implements Runnable, Serializable {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(KCModelImportBean.class.getName());

    /** Dataset to import to */
    private DatasetItem dataset;
    /** File to import */
    private File importFile;
    /** Information required for importing as a JSON Object */
    private JSONObject importInfo;
    /** User doing the importing */
    private UserItem user;

    /** Hibernate Session wrapped helper */
    private KCModelHelper modelHelper;
    /** Skill Model Dao */
    private SkillModelDao skillModelDao;
    /** The transactionTemplate is used for transaction callback.*/
    private TransactionTemplate transactionTemplate;

    /** Any potential error messages are put here. */
    private JSONObject errorMessage;
    /** Flag indicating this thread is running. */
    private boolean running;
    /** Total number of transactions to export */
    private int numTotalRows;
    /** Total number of rows skipped due to an error. */
    private int numRowsNotImported = 0;

    /** Cancel indicator */
    private boolean cancelFlag;

    /** Model action, corresponds to javascript actions of same name. */
    public static final String IMPORT_ACTION_RENAME =  "Import Renamed";
    /** Model action, corresponds to javascript actions of same name. */
    public static final String IMPORT_ACTION_SKIP = "Skip";
    /** Model action, corresponds to javascript actions of same name. */
    public static final String IMPORT_ACTION_OVERWRITE = "Overwrite existing";
    /** Model action, corresponds to javascript actions of same name. */
    public static final String IMPORT_ACTION_NEW = "Import New";

    /** # of processed rows */
    private int numCompletedRows;

    /** Constant for one hundred used in percentage. */
    private static final int ONE_HUNDRED = 100;

    /**
     * Sets exportHelper. @param exportHelper the KCModelHelper to be set to. */
    public void setModelHelper(KCModelHelper exportHelper) { this.modelHelper = exportHelper; }

    /** Gets exportHelper. @return KCModelHelper */
    public KCModelHelper getModelHelper() { return this.modelHelper; }

    /** Returns modelDao. @return Returns the modelDao. */
    public SkillModelDao getSkillModelDao() { return skillModelDao; }

    /** Set modelDao. @param skillModelDao The skillModelDao to set. */
    public void setSkillModelDao(SkillModelDao skillModelDao) {
        this.skillModelDao = skillModelDao;
    }

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
        return this.transactionTemplate;
    }

    /** Default constructor. */
    public KCModelImportBean() {
        running = false;
        numTotalRows = 0;
        numRowsNotImported = 0;
        cancelFlag = false;
    }

    /**
     * Sets all the attributes need to create the export file.
     * @param dataset DatasetItem to export.
     * @param user the user performing the import
     * @param importFile the File to import.
     * @param importInfo a JSONObject of the importInfo
     */
    protected void setAttributes(DatasetItem dataset, UserItem user,
            File importFile, JSONObject importInfo) {
        this.dataset = dataset;
        this.user = user;
        this.errorMessage = null;
        this.importFile = importFile;
        this.importInfo = importInfo;
    }

    /** Flag indicating if this build is running. @return boolean */
    public synchronized boolean isRunning() { return running; }

    /** set isRunning flag. @param running boolean of whether or not this thread is running. */
    public synchronized void setRunning(boolean running) { this.running = running; }

    /** Returns the numTotalRows. @return the numTotalRows */
    public int getNumTotalRows() { return numTotalRows; }

    /** Sets the numTotalRows. @param numTotalRows The numTotalRows to set. */
    public void setNumTotalRows(int numTotalRows) { this.numTotalRows = numTotalRows; }

    /** Returns numRowsNotImported. @return Returns the numRowsNotImported. */
    public int getNumRowsNotImported() { return numRowsNotImported; }

    /** Set numRowsNotImported. @param numRowsNotImported The numRowsNotImported to set. */
    public void setNumRowsNotImported(int numRowsNotImported) {
        this.numRowsNotImported = numRowsNotImported;
    }

    /** Returns the cancelFlag. @return the cancelFlag */
    public boolean isCancelFlag() { return cancelFlag; }

    /** Returns the numCompletedRows. @return the numCompletedRows */
    public int getNumCompletedRows() { return numCompletedRows; }

    /** Sets the numCompletedRows. @param numCompletedRows The numCompletedRows to set. */
    public void setNumCompletedRows(int numCompletedRows) {
        this.numCompletedRows = numCompletedRows;
    }

    /** Stops running this bean. */
    public void stop() { cancelFlag = true; }

    /**
     * Get the percentage done on the build.
     * @return the percentage complete
     */
    public synchronized int getPercent() {
        if (numTotalRows > 0) {
            int percent =  (numCompletedRows * ONE_HUNDRED) / numTotalRows;
            logDebug("getPercent :: current percent = ", percent);
            return percent;
        } else if (numTotalRows == 0) {
            logDebug("getPercent :: current percent = 0 due to numTotalRows = 0");
            return 0;
        } else {
            logDebug("getPercent :: Zero or less total transactions");
            return -1;
        }
    }

    /** Returns the error . @return the errorMessage as a String, null if none. */
    public JSONObject getErrorMessage() { return errorMessage; }

    /**
     * Starts this bean running.
     */
    public void run() {
        String prefix = "run(" + importFile.getName() + "): ";
        try {
            setRunning(true);
            int numLines = FileUtils.countLines(importFile);
            setNumTotalRows(numLines);

            logger.info(prefix
                    + "Performing Skill Model Import. Total lines: " + getNumTotalRows());
            BulkTransactionCallback btc = new BulkTransactionCallback();
            JSONObject transactionResults = (JSONObject)transactionTemplate.execute(btc);

            if (isCancelFlag()) {
                String msg = "User canceled KCM import.";
                logger.info(prefix + msg);
                UserLogger.log(dataset, user, UserLogger.MODEL_IMPORT, msg, false);
                setRunning(false);
            } else if (transactionResults.getBoolean("success")) {
                logger.info(prefix + "Done importing models");
                setNumCompletedRows(getNumTotalRows());
                updateMasterInstance(dataset);
            } else {
                logger.error(prefix
                        + "Transaction template returned false trying to import a KC Model");
                this.errorMessage = transactionResults;
                setNumTotalRows(-1);
                setRunning(false);
            }
        } catch (Exception exception) {
            logger.error(prefix + "Caught Exception Trying to Import a KC Model: ", exception);
            setNumTotalRows(-1);
            setRunning(false);
        } finally {
            setRunning(false);
            logger.info(prefix + "Import Bean thread stop");
            importFile.delete();
        }
    }

    /**
     * Inner class created to manage the database transactions.
     * @author kcunning
     *
     */
    public class BulkTransactionCallback implements TransactionCallback {

        /** Debug logging. */
        private Logger logger = Logger.getLogger(getClass().getName());


        /** Cache of skill objects to prevent multiple lookups. */
        private HashMap <String, SkillItem> skillCache;

        /** JSONObject that will be the return message. */
        private JSONObject toReturn;

        /** Default Constructor. */
        public BulkTransactionCallback() {
            skillCache = new HashMap <String, SkillItem>();
            toReturn = new JSONObject();
        }

        /**
         * Do a batch of line items at a time.  Hoping to improve speed.
         * @param ts TransactionStatus.
         * @return true if successful, false otherwise
         */
        public Object doInTransaction(TransactionStatus ts) {
            try {
                HashMap <Integer, SkillModelItem> modelPositions =
                    new HashMap <Integer, SkillModelItem>();
                JSONArray modelsArray = importInfo.getJSONArray("models");

                for (int i = 0, n = modelsArray.length(); i < n; i++) {
                    if (isCancelFlag()) { break; }

                    JSONObject model = modelsArray.getJSONObject(i);
                    String name = model.getString("name");
                    String action = model.getString("action");

                    SkillModelItem importModel = new SkillModelItem();
                    importModel.setOwner(user);
                    importModel.setSkillModelName(name);
                    importModel.setAllowLFAFlag(true);
                    importModel.setGlobalFlag(true);
                    importModel.setSource(SkillModelItem.SOURCE_IMPORTED);
                    importModel.setMappingType(SkillModelItem.MAPPING_STEP);
                    importModel.setDataset(dataset);

                    if (IMPORT_ACTION_SKIP.equals(action)) {
                        continue;
                    } else if (IMPORT_ACTION_OVERWRITE.equals(action)) {
                        SkillModelItem existingModel = skillModelDao.find(model.getLong("id"));
                        if (existingModel.getOwner() == null
                                || !existingModel.getOwner().equals(user)) {
                            logger.warn("Unauthorized attempt to overwrite an existing model. "
                                    + " user " + user.getId() + " sent command to overwrite model "
                                    + existingModel.getId() + " for which they are not the owner."
                                    + " Aborting import.");
                            ts.setRollbackOnly();
                            toReturn.put("success", false);
                            toReturn.put("message",
                                    "Not authorized to overwrite model '" + name + "'.");
                            return toReturn;
                        }

                        importModel.setCreationTime(new Date());
                        importModel.setModifiedTime(new Date());

                        skillModelDao.delete(existingModel);
                        skillModelDao.saveOrUpdate(importModel);
                    } else if (IMPORT_ACTION_NEW.equals(action)
                            || IMPORT_ACTION_RENAME.equals(action)) {
                        //double check that the model name is not already in use.
                        SkillModelItem existingModel = skillModelDao.findByName(dataset, name);
                        if (existingModel != null) {
                            logger.warn("Model with name '" + name + "' already exists for dataset "
                                    + dataset.getId()
                                    + " not sure how it happened but not gonna insert"
                                    + " the new one with the same name. Aborting import.");
                            ts.setRollbackOnly();

                            toReturn.put("success", false);
                            toReturn.put("message", "Model with name '" + name + "' already exists "
                                    + "for this dataset, please try again with a different name.");
                            return toReturn;
                        }

                        skillModelDao.saveOrUpdate(importModel);
                    } else {
                        logger.error("No matching action found trying to import model '"
                                + name + "'");
                        continue;
                    }

                    for (int k = 0, m = model.getJSONArray("positions").length(); k < m; k++) {
                        modelPositions.put(
                                model.getJSONArray("positions").getInt(k), importModel);
                    }
                }

                Set <SkillModelItem> modelSet = new HashSet <SkillModelItem>();
                modelSet.addAll(modelPositions.values());
                log("Starting Import of ", modelSet);

                parseInputFile(modelPositions);

                if (isCancelFlag()) {
                    toReturn.put("success", false);
                    toReturn.put("message", "CANCELED");
                    ts.setRollbackOnly();
                } else {
                    log("Finished import of ", modelSet);
                    toReturn.put("success", true);
                    toReturn.put("totalRows", numCompletedRows);
                    toReturn.put("numRowsNotImported", numRowsNotImported);
                }

            } catch (Throwable exception) {
                logger.error("EXCEPTION!: ", exception);
                ts.setRollbackOnly();
                log("Error importing skill models, aborted.", null);
                try {
                    toReturn.put("success", false);
                    toReturn.put("message", "An unexpected error occurred trying to import. "
                            + "Please try again and contact the DataShop team "
                            + "if the problem persists.");
                    toReturn.put("totalRows", numCompletedRows);
                    toReturn.put("numRowsNotImported", numRowsNotImported);
                } catch (JSONException jsonException) {
                    logger.error("Oh man, not only did the transaction fail, building the error"
                            + " message JSON Object failed as well. "
                            + "Time to give up, go home and take a nap.", exception);
                    return null;
                }
                return toReturn;
            }
            return toReturn;
        }

        /**
         * Parse the input file based on the found model positions.
         * @param modelPositions HashMap that has the SkillModelItems, and the positions
         * of the columns in the input file.
         * @throws Exception any exception during the import process.
         */
        private void parseInputFile(HashMap <Integer, SkillModelItem> modelPositions)
            throws Exception {
            logDebug("Starting parse of import file.");
            SkillDao skillDao = DaoFactory.DEFAULT.getSkillDao();
            SubgoalDao subgoalDao = DaoFactory.DEFAULT.getSubgoalDao();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(importFile), "UTF8"));
            try {
                String line = in.readLine();

                while (line != null) {
                    if (isCancelFlag()) { break; }
                    numCompletedRows++;
                    String [] splitLine = line.split("\t");
                    String stepTag = null;
                    if (splitLine.length > 0) {
                        stepTag = splitLine[0].trim();
                    }

                    //skip blank lines and the headers
                    if (stepTag == null || stepTag.equals("") || stepTag.equals("Step ID")) {
                        line = in.readLine();
                        continue;
                    }

                    SubgoalItem subgoal = subgoalDao.find(dataset, stepTag);
                    if (subgoal == null) {
                        logger.warn("No subgoal found for step GUID: " + stepTag);
                        line = in.readLine();
                        numRowsNotImported++;
                        continue;
                    }

                    int lineLength = splitLine.length;
                    for (Integer position : modelPositions.keySet()) {
                        if (lineLength <= position) {
                            logDebug("no value found at position ", position, " of line # ",
                                    numCompletedRows);
                            continue;
                        }

                        String skillName = splitLine[position].trim();
                        if (skillName == null || skillName.equals("")) { continue; }

                        SkillModelItem model = modelPositions.get(position);
                        String skillAndModel = skillName + " :: " + model.getSkillModelName();
                        SkillItem skillItem = skillCache.get(skillAndModel);
                        if (skillItem == null) {
                            skillItem = new SkillItem();
                            skillItem.setSkillName(skillName);
                            skillItem.setSkillModel(model);
                            skillDao.saveOrUpdate(skillItem);
                            skillCache.put(skillAndModel, skillItem);
                        }
                        skillDao.populateTransactionSkillMap(subgoal, skillItem, skillAndModel);
                    }

                    line = in.readLine();
                }
            } finally {
                if (in != null) { in.close(); }
            }
        }
    } // end inner class BulkTransactionCallback

    /**
     * Helper method to push dataset change out to master DataShop instance.
     * @param dataset the DatasetItem
     */
    private void updateMasterInstance(DatasetItem dataset) {

        // If a slave, update master DataShop instance with dataset info.
        if (DataShopInstance.isSlave()) {
            try {
                Integer datasetId = (Integer)dataset.getId();
                DatasetDTO datasetDto =
                    HelperFactory.DEFAULT.getWebServiceHelper().datasetDTOForId(datasetId);
                DatasetCreator.INSTANCE.setDataset(datasetDto);
            } catch (Exception e) {
                // Failed to push Dataset info to master. Ignore?
                logDebug("Failed to push dataset info to master for dataset '"
                         + dataset.getDatasetName() + "': " + e);
            }
        }
    }

    /**
     * Write a message to the user log.
     * @param message Message to start the user log with.
     * @param skillModelList list of skill models (can be null)
     */
    private void log(String message, Collection <SkillModelItem> skillModelList) {
        StringBuffer logString;
        if (skillModelList != null) {
            logString = new StringBuffer(message + " model(s) ");
            for (Iterator <SkillModelItem> it = skillModelList.iterator(); it.hasNext();) {
                SkillModelItem model = it.next();
                logString.append("'" + model.getSkillModelName() + "' (" + model.getId() + ")");
                logString.append((it.hasNext()) ? ", " : "");
            }
        } else {
            logString = new StringBuffer(message);
        }
        UserLogger.log(dataset, user, UserLogger.MODEL_IMPORT, logString.toString(), false);
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    public void logDebug(final Object... args) { LogUtils.logDebug(logger, args); }
}