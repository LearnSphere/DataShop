/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.kcmodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contextual information saved to the {@link HttpSession} for the kc model
 * import and export page.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 10106 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-10-08 15:00:32 -0400 (Tue, 08 Oct 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelContext {

    /** The currently knowledge component model import bean. */
    private static final String KC_MODEL_IMPORT_BEAN =
        "edu.cmu.pslc.datashop.servlet.kcmodel.KCModelImportControllerBean";
    /** The currently knowledge component model import file. */
    private static final String KC_MODEL_AGG_AND_LFA_BEAN =
        "edu.cmu.pslc.datashop.servlet.kcmodel.KCModelAggregatorBean";
    /** The currently knowledge component model import file. */
    private static final String KC_MODEL_IMPORT_FILE = "kc_model_import_file";

    /** The current order to sort a list of KC Models. */
    private static final String KCM_SORT_BY = "kc_model_sort_by";
    /** Flag indicating whether to sort the models ascending or not. */
    private static final String KCM_SORT_BY_ASC_FLAG = "kc_model_sort_by_asc_flag";

    /** HashMap that holds the values and is thread safe. */
    private Map <String, Object> map;

    /**
     * Default Constructor.
     */
    public KCModelContext () {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
    }

    /**
     * Set the model import bean into the session.
     * @param bean the {@link KCModelImportControllerBean}
     */
    public void setImportBean(KCModelImportControllerBean bean) {
        map.put(KC_MODEL_IMPORT_BEAN, bean);
    }

    /**
     * Get the currently model import bean.
     * @return the current sub-tab.
     */
    public KCModelImportControllerBean getImportBean() {
        return (KCModelImportControllerBean)map.get(KC_MODEL_IMPORT_BEAN);
    }

    /**
     * Set the current import file.
     * @param importFile {@link File} that is being imported.
     */
    public void setImportFile(File importFile) {
        map.put(KC_MODEL_IMPORT_FILE, importFile);
    }

    /**
     * Gets the current KC Model import file.
     * @return the {@link File}
     */
    public File getImportFile() {
        return (File)map.get(KC_MODEL_IMPORT_FILE);
    }

    /**
     * Sets the current aggregator and LFA bean for KC model imports.
     * @param bean {@link KCModelAggregatorBean}
     */
    public void setAggregatorBean(KCModelAggregatorBean bean) {
        map.put(KC_MODEL_AGG_AND_LFA_BEAN, bean);
    }

    /**
     * Gets the current aggregator and LFA bean for KC model imports.
     * @return {@link KCModelAggregatorBean}
     */
    public KCModelAggregatorBean getAggregatorBean() {
        return (KCModelAggregatorBean)map.get(KC_MODEL_AGG_AND_LFA_BEAN);
    }

    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_NAME = "Model Name";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_NUM_KCS = "KCs";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_OBS = "Observations with KCs";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_AIC = "AIC";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_BIC = "BIC";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_STUDENT_STRATIFIED_CV_RMSE =
        "Cross Validation RMSE (student stratified)";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_ITEM_STRATIFIED_CV_RMSE =
        "Cross Validation RMSE (item stratified)";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_UNSTRATIFIED_CV_RMSE =
        "Cross Validation RMSE (unstratified)";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_OBS_UNSTRATIFIED =
        "Cross Validation Observations (unstratified)";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_DATE = "Date created";
    /** KC Model Sort option to sort the models. */
    public static final String SORT_BY_CREATOR = "Creator";

    /** Ordered collection of all Sorting Options.  */
    public static final List SORT_OPTIONS;
    static {
        SORT_OPTIONS = new ArrayList();
        SORT_OPTIONS.add(SORT_BY_NAME);
        SORT_OPTIONS.add(SORT_BY_NUM_KCS);
        SORT_OPTIONS.add(SORT_BY_OBS);
        SORT_OPTIONS.add(SORT_BY_AIC);
        SORT_OPTIONS.add(SORT_BY_BIC);
        SORT_OPTIONS.add(SORT_BY_STUDENT_STRATIFIED_CV_RMSE);
        SORT_OPTIONS.add(SORT_BY_ITEM_STRATIFIED_CV_RMSE);
        SORT_OPTIONS.add(SORT_BY_UNSTRATIFIED_CV_RMSE);
        SORT_OPTIONS.add(SORT_BY_OBS_UNSTRATIFIED);
        SORT_OPTIONS.add(SORT_BY_DATE);
        SORT_OPTIONS.add(SORT_BY_CREATOR);
    }

    /** Returns sort type. @return the sort as a String. */
    public String getSortBy() {
        String sortBy = (String)map.get(KCM_SORT_BY);
        if (sortBy == null) {
            sortBy = SORT_BY_AIC;
        }
        return sortBy;
    }

    /** Set sort type. @param sort the selected sort to view. */
    public void setSortBy(String sort) {
        map.put(KCM_SORT_BY, sort);
    }

    /** Returns the sort direction. @return true if sort ascending, false otherwise. */
    public Boolean getSortByAscendingFlag() {
        Boolean flag = (Boolean)map.get(KCM_SORT_BY_ASC_FLAG);
        if (flag == null) {
            flag = Boolean.TRUE;
        }
        return flag;
    }

    /** Set the sort direction. @param direction of true if the sort direction is ascending. */
    public void setSortByAscendingFlag(Boolean direction) {
        map.put(KCM_SORT_BY_ASC_FLAG, direction);
    }

}
