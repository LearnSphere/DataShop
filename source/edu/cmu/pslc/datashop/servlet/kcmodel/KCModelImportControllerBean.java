/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.pslc.datashop.dao.DaoFactory;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.util.LogUtils;

/**
 *  ImportController Bean that implements runnable.  This bean will be spawned in
 *  a separate thread to manage the Import+Aggregator process. It does this by
 *  running the KCModelImportBean and KCModelAggregatorBean.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelImportControllerBean implements Runnable, Serializable {
    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Dataset the skill models belong to. */
    private DatasetItem dataset;
    /** User for logging purposes. */
    private UserItem user;
    /** Information required for importing as a JSON Object */
    private JSONObject importInfo;
    /** The location of the aggregator stored procedure file. */
    private String aggSpFilePath;
    /** Directory for SSSS files. */
    private String ssssDir;

    /** Flag indicating this thread is running. */
    private boolean running;

    /** Cancel indicator */
    private boolean cancelFlag;

    /** Import bean. */
    private KCModelImportBean importBean;
    /** Aggregator bean. */
    private KCModelAggregatorBean aggregatorBean;

    /**
     * Set the Import bean.
     * @param importBean the Import bean
     */
    public void setImportBean(KCModelImportBean importBean) { this.importBean = importBean; }

    /**
     * Get the Import bean.
     * @return KCModelImportBean
     */
    public KCModelImportBean getImportBean() { return this.importBean; }

    /**
     * Set the Aggregator bean.
     * @param aggBean the Import bean
     */
    public void setAggregatorBean(KCModelAggregatorBean aggBean) { this.aggregatorBean = aggBean; }

    /**
     * Get the Aggregator bean.
     * @return KCModelAggregatorBean
     */
    public KCModelAggregatorBean getAggregatorBean() { return this.aggregatorBean; }

    /** Default constructor. */
    public KCModelImportControllerBean() {
        running = false;
        cancelFlag = false;
    }

    /**
     * Sets all the attributes need to import the named file and
     * aggregate the samples of the dataset.
     * @param dataset DatasetItem to import.
     * @param user the user performing the import
     * @param importFile the File to import.
     * @param importInfo a JSONObject of the importInfo
     * @param aggSpFilePath the path to the aggregator stored procedure file.
     * @param ssssDir directory for SSSS files
     */
    protected void setAttributes(DatasetItem dataset, UserItem user, File importFile,
                                 JSONObject importInfo, String aggSpFilePath, String ssssDir) {
        this.dataset = dataset;
        this.user = user;
        this.importInfo = importInfo;
        this.aggSpFilePath = aggSpFilePath;
        this.ssssDir = ssssDir;
        importBean.setAttributes(dataset, user, importFile, importInfo);
    }

    /** Flag indicating if this build is running. @return boolean */
    public synchronized boolean isRunning() { return running; }

    /** set isRunning flag. @param running boolean of whether or not this thread is running. */
    public synchronized void setRunning(boolean running) { this.running = running; }

    /** Returns the cancelFlag. @return the cancelFlag */
    public boolean isCancelFlag() { return cancelFlag; }

    /**
     * Starts this bean running.
     */
    public void run() {
        try {
            setRunning(true);

            // A new thread was started to run this bean; run the other two in same.
            importBean.run();
            if (!isCancelFlag()) {
                List<SkillModelItem> skillModelList = generateSkillModelList(importInfo);
                aggregatorBean.setAttributes(dataset, skillModelList, user, aggSpFilePath, ssssDir);
                aggregatorBean.run();
            } else {
                String msg = "User canceled KCM import.";
                logDebug(msg);
                setRunning(false);
            }
        } catch (Exception exception) {
            logger.error("Caught Exception Trying to Import a KC Model: ", exception);
            setRunning(false);
        } finally {
            setRunning(false);
        }
    }

    /**
     * Stop running this bean.
     * Delegate the stop action to the Import and Aggregator beans.
     */
    public void stop() {
        cancelFlag = true;
        importBean.stop();
        aggregatorBean.stop();
    }

    /**
     * Given a list of skill model names, generate a list of SkillModelItems.
     * @param skillModelNames list of skill model names
     * @return skillModelList list of SkillModelItems
     * @throws JSONException exception parsing JSONObject
     */
    private List<SkillModelItem> generateSkillModelList(JSONObject json)
        throws JSONException {

        if (json == null) { return null; }

        JSONArray modelsArray = json.getJSONArray("models");

        List <SkillModelItem> result = new ArrayList <SkillModelItem>();
        for (int i = 0, n = modelsArray.length(); i < n; i++) {
            JSONObject modelJSON = modelsArray.getJSONObject(i);
            if (!modelJSON.getString("action").equals("Skip")) {
                String modelName = modelJSON.getString("name");
                SkillModelItem model =
                    DaoFactory.DEFAULT.getSkillModelDao().findByName(dataset, modelName);
                result.add(model);
            }
        }
        return result;
    }

    /**
     * Only log if debugging is enabled.
     * @param args concatenate all arguments into the string to be logged
     */
    private void logDebug(final Object... args) { LogUtils.logDebug(logger, args); }
}