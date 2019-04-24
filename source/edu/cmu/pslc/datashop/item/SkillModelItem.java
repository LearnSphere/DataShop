/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Knowledge Component (Skill) model creates
 * an association between subgoals and skills.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 13244 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-05-24 12:32:19 -0400 (Tue, 24 May 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SkillModelItem extends Item implements java.io.Serializable, Comparable  {

    /** Formatter for AIC and BIC values. */
    private static final NumberFormat AIC = new DecimalFormat("#,###,##0.00");
    /** Formatter for RMSE values. */
    private static final NumberFormat RMSE = new DecimalFormat("##0.000000");

    /** Name of the transaction skill model that's not an actual skill model. */
    public static final String ORIGINAL_NAME = "Original";

    /** Name of the default skill model derived from the transactions. */
    public static final String DEFAULT_NAME = "Default";

    /** Model Source String. */
    public static final String SOURCE_LOGGED = "logged";
    /** Model Source String. */
    public static final String SOURCE_IMPORTED = "imported";
    /** Model Source String. */
    public static final String SOURCE_USER_CREATED = "user-created";
    /** Model Source String. */
    public static final String SOURCE_AUTO_GEN = "auto-generated";

    /** Model Mapping Type. */
    public static final String MAPPING_CORRECT_TRANS = "correct-transaction-to-kc";
    /** Model Mapping Type. */
    public static final String MAPPING_STEP = "step-to-kc";
    /** Model Mapping Type. */
    public static final String MAPPING_KC = "kc-to-kc";
    /** Model Mapping Type. */
    public static final String MAPPING_PROBLEM_TO_KC = "problem-to-kc";

    /** Model status string. */
    public static final String STATUS_READY = "ready";
    /** Model status string. */
    public static final String STATUS_NOT_READY = "not ready";
    /** Model status string. */
    public static final String STATUS_ERROR = "error";

    /** Model LFA status. */
    public static final String LFA_STATUS_COMPLETE = "complete";
    /** Model LFA status. */
    public static final String LFA_STATUS_UNABLE_TO_RUN = "unable to run";
    /** Model LFA status. */
    public static final String LFA_STATUS_DO_NOT_RUN = "do not run";
    /** Model LFA status. */
    public static final String LFA_STATUS_QUEUED = "queued";

    /** Model CV status. */
    public static final String CV_STATUS_COMPLETE = "complete";
    /** Model CV status. */
    public static final String CV_STATUS_UNABLE_TO_RUN = "unable to run";
    /** Model CV status. */
    public static final String CV_STATUS_DO_NOT_RUN = "do not run";
    /** Model CV status. */
    public static final String CV_STATUS_QUEUED = "queued";
    /** Model CV status. */
    public static final String CV_STATUS_IMCOMPLETE = "run but incomplete";

    /** AFM or CV status description*/
    public static final String LFA_CV_STATUS_DESCRIPTION_NOT_ALLOWED_TO_RUN = "AFM and CV are not allowed to run";
    public static final String LFA_CV_STATUS_DESCRIPTION_OVER_SKILL_LIMIT = "Number of skills exceeds the limit";
    public static final String LFA_CV_STATUS_DESCRIPTION_OVER_MEMORY_LIMIT = "Data exceeds the memory limit";
    public static final String LFA_CV_STATUS_DESCRIPTION_OUT_MEMORY_GET_SSSVS = "Ran out of memory while preparing data for AFM and CV";
    public static final String LFA_CV_STATUS_DESCRIPTION_INVALID_SSSVS = "Invalid data";
    public static final String LFA_CV_STATUS_DESCRIPTION_ALL_0_OR_1 = "AFM and CV cannot run if transaction outcomes are either all correct or all incorrect";
    public static final String LFA_STATUS_DESCRIPTION_OUT_MEMORY_RUNNING = "Ran out of memory while running AFM";
    public static final String CV_STATUS_DESCRIPTION_OUT_MEMORY_RUNNING = "Ran out of memory while running CV";
    public static final String LFA_STATUS_DESCRIPTION_INVALID_RESULT_VALUES = "Invalid values obtained for AFM";
    public static final String CV_STATUS_DESCRIPTION_FAILED_RUN = "Failed to run CV";
    public static final String LFA_CV_STATUS_DESCRIPTION_COMPLETED = "Success";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE = "CV is incomplete";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_STUDENT = "Failed to compute student stratified CV";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_ITEM = "Failed to compute item stratified CV";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_UNSTRATIFIED = "Failed to compute unstratified CV";
    public static final String CV_STATUS_DESCRIPTION_INVALID_RESULT_VALUES = "Invalid values for all CV measurements";    
    public static final String CV_STATUS_DESCRIPTION_INVALID_MODEL_TYPE = "Not available due to model type";    
    
    /** Constant for the maximum length of LFA status display. */
    public static final int LFA_STATUS_DISPLAY_MAX_LENGTH = 30;
    /** Constant for the maximum length of CV status display. */
    public static final int CV_STATUS_DISPLAY_MAX_LENGTH = 40;

    /** Model name for a single KC model. */
    public static final String NAME_SINGLE_KC_MODEL = "Single-KC";
    /** Model name for a unique step KC model. */
    public static final String NAME_UNIQUE_STEP_MODEL = "Unique-step";

    /** Constant for the maximum length of a KC Model Name. */
    public static final int MODEL_NAME_MAX_LENGTH = 50;

    /** Database generated unique Id for this skill model. */
    private Long id;
    /** Owner/Creator of this skill model. */
    private UserItem owner;
    /** The name of this skill model as a string. */
    private String skillModelName;
    /** AIC score for this skill model. */
    private Double aic;
    /** BIC score for this skill model. */
    private Double bic;
    /** Double intercept for this skill model. */
    private Double intercept;
    /** Log likelihood for this skill model. */
    private Double logLikelihood;
    /** Flag indicating whether users other than the owner can see this skill model. */
    private Boolean globalFlag;
    /** Boolean indicating whether to run LFA on this dataset.*/
    private Boolean allowLFAFlag;
    /** The current status of this model. */
    private String status;
    /** The current status of the LFA regression on this model.*/
    private String lfaStatus;
    /** The current status description of the LFA regression on this model.*/
    private String lfaStatusDescription;
    /** The source for this model.*/
    private String source;
    /** The type of mapping this model uses.*/
    private String mappingType;
    /** The time and date this model was created.*/
    private Date creationTime;
    /** The time and date this model was last modified.*/
    private Date modifiedTime;
    /** The number of observations that were assigned KCs for this model
     * Comes from the generation of the SSSS input file for LFA. */
    private Integer numObservations;
    /** Student stratified Cross validation RMSE for this skill model. */
    private Double cvStudentStratifiedRmse;
    /** Item (ie step) stratified Cross validation RMSE for this skill model. */
    private Double cvStepStratifiedRmse;
    /** unstratified Cross validation RMSE for this skill model. */
    private Double cvUnstratifiedRmse;
    /** The number of observations that were used for unstratified cross validation.
     * Any student or skill that has only one row is eliminated. */
    private Integer unstratifiedNumObservations;
    /** The number of parameters that were used for unstratified cross validation. */
    private Integer unstratifiedNumParameters;
    /** The current status of cross validation on this model.*/
    private String cvStatus;
    /** The current status description of cross validation on this model.*/
    private String cvStatusDescription;
    /** The number of skill part of this model. This is a de-normalization for the KC Model Sort. */
    private Integer numSkills;

    /** Dataset associated with this skill model. */
    private DatasetItem dataset;

    /** Collection of skills associated with this skill model. */
    private Set skills;
    /** Collection of alpha scores for all students encompassed by the model. */
    private Set alphaScores;


    /** Default constructor. */
    public SkillModelItem() {
        this.globalFlag = Boolean.FALSE;
        this.allowLFAFlag = Boolean.TRUE;
        this.status = STATUS_NOT_READY;
        this.lfaStatus = LFA_STATUS_QUEUED;
        this.cvStatus = LFA_STATUS_QUEUED;
        this.creationTime = new Date();
        this.modifiedTime = this.creationTime;
    }

    /**
     *  Constructor with id.
     *  @param skillModelId Database generated unique Id for this skill model.
     */
    public SkillModelItem(Long skillModelId) {
        this.id = skillModelId;
    }

    /**
     * Get skillModelId.
     * @return the Long id as a comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set skillModelId.
     * @param skillModelId Database generated unique Id for this skill model.
     */
    public void setId(Long skillModelId) {
        this.id = skillModelId;
    }
    /**
     * Get owner.
     * @return a UserItem object
     */

    public UserItem getOwner() {
        return this.owner;
    }

    /**
     * Set owner.
     * @param owner Owner/Creator of this skill model.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }
    /**
     * Get skillModelName.
     * @return the skill model name
     */

    public String getSkillModelName() {
        return this.skillModelName;
    }

    /**
     * Set skillModelName.
     * @param skillModelName The name of this skill model as a string.
     */
    public void setSkillModelName(String skillModelName) {
        this.skillModelName = skillModelName;
    }
    /**
     * Get AIC.
     * @return Double
     */

    public Double getAic() {
        return this.aic;
    }

    /**
     * Set AIC.
     * @param aic AIC score for this skill model.
     */
    public void setAic(Double aic) {
        this.aic = aic;
    }

    /**
     * Get BIC.
     * @return Double
     */
    public Double getBic() {
        return this.bic;
    }

    /**
     * Set BIC.
     * @param bic BIC score for this skill model.
     */
    public void setBic(Double bic) {
        this.bic = bic;
    }

    /**
     * Get student stratified cross validation RMSE.
     * @return Double
     */

    public Double getCvStudentStratifiedRmse() {
        return this.cvStudentStratifiedRmse;
    }

    /**
     * Returns displayable string of either the student stratified RMSE if complete,
     * or an empty string otherwise.
     * @return string of student stratified CV if CV status is complete, empty string otherwise
     */
    public String getCVStatusOrCvStudentStratifiedRmseForDisplay() {
            String display;
            if (cvStatus != null && cvStatus.equals(LFA_STATUS_COMPLETE)) {
                String cvString;
                if (getCvStudentStratifiedRmse() != null) {
                    cvString = RMSE.format(getCvStudentStratifiedRmse());
                } else {
                    cvString = "N/A";
                }
                display = cvString;
            } else if (cvStatus != null && cvStatus.equals(CV_STATUS_IMCOMPLETE)) {
                    display = getCVIncompleteOrCvStudentStratifiedRmseForDisplay();
             } else {
                display = getCvStatusForDisplay();
            }
            return display;
    }

    /**
     * Set student stratified cross validation RMSE.
     * @param cvStudentStratifiedRmse student stratified Cross validation RMSE for this skill model.
     */
    public void setCvStudentStratifiedRmse(Double cvStudentStratifiedRmse) {
        this.cvStudentStratifiedRmse = cvStudentStratifiedRmse;
    }

    /**
     * Get step stratified cross validation RMSE.
     * @return Double
     */

    public Double getCvStepStratifiedRmse() {
        return this.cvStepStratifiedRmse;
    }

    /**
     * Returns displayable string of either the step stratified RMSE if complete,
     * or an empty string otherwise.
     * @return string of step stratified CV if CV status is complete, empty string otherwise
     */
    public String getEmptyStringOrCvStepStratifiedRmseForDisplay() {
        String display;
        if (cvStatus != null && cvStatus.equals(LFA_STATUS_COMPLETE)) {
            String cvString;
            if (getCvStepStratifiedRmse() != null) {
                cvString = RMSE.format(getCvStepStratifiedRmse());
            } else {
                cvString = "N/A";
            }
            display = cvString;
        } else if (cvStatus != null && cvStatus.equals(CV_STATUS_IMCOMPLETE)) {
                display = getCVIncompleteOrCvStepStratifiedRmseForDisplay();
        } else {
            display = "";
        }
        return display;
    }

    /**
     * Set step stratified cross validation RMSE.
     * @param cvStepStratifiedRmse item stratified Cross validation RMSE for this skill model.
     */
    public void setCvStepStratifiedRmse(Double cvStepStratifiedRmse) {
        this.cvStepStratifiedRmse = cvStepStratifiedRmse;
    }

    /**
     * Get unstratified cross validation RMSE.
     * @return Double
     */

    public Double getCvUnstratifiedRmse() {
        return this.cvUnstratifiedRmse;
    }

    /**
     * Returns displayable string of either the unstratified RMSE if complete,
     * or an empty string otherwise.
     * @return string of unstratified CV if CV status is complete, empty string otherwise
     */
    public String getEmptyStringOrCvUnstratifiedRmseForDisplay() {
        String display;
        if (cvStatus != null && cvStatus.equals(LFA_STATUS_COMPLETE)) {
            String cvString;
            if (getCvUnstratifiedRmse() != null) {
                cvString = RMSE.format(getCvUnstratifiedRmse());
            } else {
                cvString = "N/A";
            }
            display = cvString;
        } else if (cvStatus != null && cvStatus.equals(CV_STATUS_IMCOMPLETE)) {
                display = getCVIncompleteOrCvUnstratifiedRmseForDisplay();
        } else {
            display = "";
        }
        return display;
    }

    /**
     * Set unstratified cross validation RMSE.
     * @param cvUnstratifiedRmse unstratified Cross validation RMSE for this skill model.
     */
    public void setCvUnstratifiedRmse(Double cvUnstratifiedRmse) {
        this.cvUnstratifiedRmse = cvUnstratifiedRmse;
    }

    /**
     * Returns displayable string of either the step stratified RMSE if available,
     * or "unable to run" if cv_status is incomplete.
     * @return string of step stratified CV if available, "unable to run" if incomplete
     */
    public String getCVIncompleteOrCvStepStratifiedRmseForDisplay() {
            String display = "";
            if (getCvStepStratifiedRmse() != null) {
                    display = RMSE.format(getCvStepStratifiedRmse());
            } else {
                    display = CV_STATUS_UNABLE_TO_RUN;
            }
            return display;
    }
    
    /**
     * Returns displayable string of either the step stratified RMSE if available,
     * or cv_status_description if cv_status is incomplete.
     * @return string of step stratified CV if available, partial status description if incomplete
     */
    public String getCVIncompleteStatusOrCvStepStratifiedRmseForDisplay() {
            String display = "";
            if (getCvStepStratifiedRmse() != null) {
                    display = RMSE.format(getCvStepStratifiedRmse());
            } else {
                    display = getCvStepStatusPartialDescriptionForDisplay();
            }
            return display;
    }
    
    

    /**
     * Returns displayable string of either the unstratified RMSE if available,
     * or "unable to run" if cv_status is incomplete.
     * @return string of unstratified CV if available, "unable to run" if incomplete
     */
    public String getCVIncompleteOrCvUnstratifiedRmseForDisplay() {
            String display = "";
            if (getCvUnstratifiedRmse() != null) {
                    display = RMSE.format(getCvUnstratifiedRmse());
            } else {
                    display = CV_STATUS_UNABLE_TO_RUN;
            }
            return display;
    }
    
    /**
     * Returns displayable string of either the unstratified RMSE if available,
     * or cv_status_description if cv_status is incomplete.
     * @return string of unstratified CV if available, partial status description if incomplete
     */
    public String getCVIncompleteStatusOrCvUnstratifiedRmseForDisplay() {
            String display = "";
            if (getCvUnstratifiedRmse() != null) {
                    display = RMSE.format(getCvUnstratifiedRmse());
            } else {
                    display = this.getCvUnstratifiedStatusPartialDescriptionForDisplay();
            }
            return display;
    }

    /**
     * Returns displayable string of either the student stratified RMSE if available,
     * or "unable to run" if cv_status is incomplete.
     * @return string of student stratified CV if available, "unable to run" if incomplete
     */
    public String getCVIncompleteOrCvStudentStratifiedRmseForDisplay() {
            String display = "";
            if (getCvStudentStratifiedRmse() != null) {
                    display = RMSE.format(getCvStudentStratifiedRmse());
            } else {
                    display = CV_STATUS_UNABLE_TO_RUN;
            }
            return display;
    }
    
    /**
     * Returns displayable string of either the student stratified RMSE if available,
     * or cv_status_description if cv_status is incomplete.
     * @return string of student stratified CV if available, partial status description if incomplete
     */
    public String getCVIncompleteStatusOrCvStudentStratifiedRmseForDisplay() {
            String display = "";
            if (getCvStudentStratifiedRmse() != null) {
                    display = RMSE.format(getCvStudentStratifiedRmse());
            } else {
                    display = getCvStudentStatusPartialDescriptionForDisplay();
            }
            return display;
    }

    /**
     * Get Intercept.
     * @return Double
     */
    public Double getIntercept() {
        return this.intercept;
    }

    /**
     * Set Intercept.
     * @param intercept intercept for this skill model
     */
    public void setIntercept(Double intercept) {
        this.intercept = intercept;
    }

    /**
     * Get LogLikelihood.
     * @return Double
     */
    public Double getLogLikelihood() {
        return this.logLikelihood;
    }

    /**
     * Set LogLikelihood.
     * @param logLikelihood LogLikelihood for this skill model
     */
    public void setLogLikelihood(Double logLikelihood) {
        this.logLikelihood = logLikelihood;
    }

    /**
     * Get globalFlag.
     * @return Boolean
     */
    public Boolean getGlobalFlag() {
        return this.globalFlag;
    }

    /**
     * Set globalFlag.
     * @param globalFlag Flag indicating whether users
     * other than the owner can see this skill model.
     */
    public void setGlobalFlag(Boolean globalFlag) {
        this.globalFlag = globalFlag;
    }

    /**
     * Get dataset.
     * @return a DatasetItem object
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset DatasetItem associated with this item
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get skills.
     * @return a set of SkillItem objects
     */
    protected Set getSkills() {
        if (this.skills == null) {
            this.skills = new HashSet();
        }
        return this.skills;
    }

    /**
     * Public method to get Skills.
     * @return a list instead of a set
     */
    public List getSkillsExternal() {
        List sortedList = new ArrayList(getSkills());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set skills.
     * @param skills Collection of skills associated with this skill model.
     */
    protected void setSkills(Set skills) {
        this.skills = skills;
    }

    /**
     * Add a skill.
     * @param item the skill item to add.
     */
    public void addSkill(SkillItem item) {
        if (!this.getSkills().contains(item)) {
            getSkills().add(item);
            item.setSkillModel(this);
        }
    }
    /**
     * Get alphaScores.
     * @return a set of AlphaScoreItem objects
     */
    protected Set getAlphaScores() {
        if (this.alphaScores == null) {
            this.alphaScores = new HashSet();
        }
        return this.alphaScores;
    }

    /**
     * Public method to get Alpha Scores.
     * @return a list instead of a set
     */
    public List getAlphaScoresExternal() {
        List sortedList = new ArrayList(getAlphaScores());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set alphaScores.
     * @param alphaScores Collection of alpha scores for all students encompassed by the model.
     */
    protected void setAlphaScores(Set alphaScores) {
        this.alphaScores = alphaScores;
    }

    /**
     * Add an alpha score.
     * @param item the alpha score item to add.
     */
    public void addAlphaScore(AlphaScoreItem item) {
        if (!getAlphaScores().contains(item)) {
            getAlphaScores().add(item);
            item.setSkillModel(this);
        }
    }

    /** Returns allowLFAFlag. @return Returns the allowLFAFlag. */
    public Boolean getAllowLFAFlag() {
        return allowLFAFlag;
    }

    /** Set allowLFAFlag. @param allowLFAFlag The allowLFAFlag to set. */
    public void setAllowLFAFlag(Boolean allowLFAFlag) {
        this.allowLFAFlag = allowLFAFlag;
    }

    /** Returns status. @return Returns the status. */
    public String getStatus() {
        return status;
    }

    /** Set status. @param status The status to set. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Returns lfaStatus. @return Returns the lfaStatus. */
    public String getLfaStatus() {
        return lfaStatus;
    }
    
    /** Returns lfaStatusDescription. @return Returns the lfaStatusDescription. */
    public String getLfaStatusDescription() {
            if (lfaStatusDescription == null)
                    lfaStatusDescription = "";  
            return lfaStatusDescription;
    }

    /** Returns cvStatusDescription. @return Returns the cvStatusDescription. */
    public String getCvStatusDescription() {
            if (cvStatusDescription == null)
                    cvStatusDescription = "";
            return cvStatusDescription;
    }

    /**
     * Returns displayable string of either the AIC if complete,
     * or the LFA status otherwise.
     * @return string of AIC if LFA status is complete, LFA status otherwise
     */
    public String getLFAStatusOrAICValueForDisplay() {
        String display;
        if (lfaStatus != null && lfaStatus.equals(LFA_STATUS_COMPLETE)) {
            String aicString;
            if (getAic() != null) {
                aicString = AIC.format(getAic());
            } else {
                aicString = "N/A";
            }
            display = aicString;
        } else {
            display = getLfaStatusForDisplay();
        }
        return display;
    }

    /**
     * Returns displayable string of either the BIC if complete,
     * or an empty string otherwise.
     * @return string of BIC if LFA status is complete, empty string otherwise
     */
    public String getEmptyStringOrBICValueForDisplay() {
        String display;
        if (lfaStatus != null && lfaStatus.equals(LFA_STATUS_COMPLETE)) {
            String bicString;
            if (getBic() != null) {
                bicString = AIC.format(getBic());
            } else {
                bicString = "N/A";
            }
            display = bicString;
        } else {
            display = "";
        }
        return display;
    }

    /**
     * Returns displayable string of either the AIC if complete,
     * or an empty string otherwise.
     * @return string of AIC if LFA status is complete, empty string otherwise
     */
    public String getEmptyStringOrAICValueForDisplay() {
        String display;
        if (lfaStatus != null && lfaStatus.equals(LFA_STATUS_COMPLETE)) {
            String aicString;
            if (getAic() != null) {
                aicString = AIC.format(getAic());
            } else {
                aicString = "N/A";
            }
            display = aicString;
        } else {
            display = "";
        }
        return display;
    }

    /**
     * Returns a displayable string for LFA status
     * (either "not scheduled to run" or
     * the actual status).
     * @return string of LFA status for display.
     */
    public String getLfaStatusForDisplay() {
        String display;
        if (lfaStatus == null) {
            display = LFA_STATUS_QUEUED;
        } else if (lfaStatus.equals(LFA_STATUS_DO_NOT_RUN)) {
            display = "not scheduled to run";
        } else {
            display = lfaStatus;
        }
        return display;
    }
    /**
     * Returns a displayable string for LFA status
     * (either "not scheduled to run" or
     * the actual status).
     * @return string of LFA status for display.
     */
    public String getLfaStatusPartialDescriptionForDisplay() {
        String display = getLfaStatusForDisplay();
        if (!display.equals(LFA_STATUS_QUEUED)) {
                if (lfaStatusDescription != null && !lfaStatusDescription.equals("")) {
                        //example for display: "unable to run. invalid da...", 5 is the length of ". " plus "..."
                        display += ": " + lfaStatusDescription;
                        if (display.length() > LFA_STATUS_DISPLAY_MAX_LENGTH) 
                                display = display.substring(0, LFA_STATUS_DISPLAY_MAX_LENGTH - 3) + "...";
                }
        }
        return display;
    }
    /**
     * Returns a displayable string for LFA status
     * (either "not scheduled to run" or
     * the actual status).
     * @return string of LFA status for display.
     */
    public String getLfaStatusDescriptionForDisplay() {
        String display = getLfaStatusForDisplay();
        if (!display.equals(LFA_STATUS_QUEUED)) {
                if (lfaStatusDescription != null && !lfaStatusDescription.equals("")) {
                        display += ": " + lfaStatusDescription;
                }
        }
        return display;
    }
    
    /**
     * Returns a displayable string for LFA status
     * (either "not scheduled to run" or
     * the actual status).
     * @return string of LFA status for display.
     */
    public String getCvStatusDescriptionForDisplay() {
        String display = getCvStatusForDisplay();
        if (!display.equals(CV_STATUS_QUEUED)) {
                if (cvStatusDescription != null && !cvStatusDescription.equals("")) {
                        display += ": " + cvStatusDescription;
                }
        }
        return display;
    }
    /** Set lfaStatus. @param lfaStatus The lfaStatus to set. */
    public void setLfaStatus(String lfaStatus) {
        this.lfaStatus = lfaStatus;
    }
    
    /** Set lfaStatusDescription. @param lfaStatusDescription The lfaStatusDescription to set. */
    public void setLfaStatusDescription(String lfaStatusDescription) {
        this.lfaStatusDescription = lfaStatusDescription;
    }
    
    /** Set cvStatusDescription. @param cvStatusDescription The cvStatusDescription to set. */
    public void setCvStatusDescription(String cvStatusDescription) {
        this.cvStatusDescription = cvStatusDescription;
    }

    /** Returns source. @return Returns the source. */
    public String getSource() {
        return source;
    }

    /** Set source. @param source The source to set. */
    public void setSource(String source) {
        this.source = source;
    }

    /** Returns mappingType. @return Returns the mappingType. */
    public String getMappingType() {
        return mappingType;
    }

    /** Set mappingType. @param mappingType The mappingType to set. */
    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    /** Returns creationTime. @return Returns the creationTime. */
    public Date getCreationTime() {
        return creationTime;
    }

    /** Set creationTime. @param creationTime The creationTime to set. */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /** Returns modifiedTime. @return Returns the modifiedTime. */
    public Date getModifiedTime() {
        return modifiedTime;
    }

    /** Set modifiedTime. @param modifiedTime The modifiedTime to set. */
    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    /** Returns numObservations.  @return Returns the numObservations. */
    public Integer getNumObservations() {
        return numObservations;
    }

    /** Set numObservations.  @param numObservations the numObservations to set. */
    public void setNumObservations(Integer numObservations) {
        this.numObservations = numObservations;
    }

    /** Returns cvStatus. @return Returns the cvStatus. */
    public String getCvStatus() {
        return cvStatus;
    }

    /** Set CV status. @param cvStatus The CV status to set. */
    public void setCvStatus(String cvStatus) {
        this.cvStatus = cvStatus;
    }

    /** Returns unstratifiedNumObservations.  @return Returns the unstratifiedNumObservations. */
    public Integer getUnstratifiedNumObservations() {
        return unstratifiedNumObservations;
    }

    /** Set unstratified numObservations.
     * @param numObservations the unstratifiedNumObservations to set. */
    public void setUnstratifiedNumObservations(Integer numObservations) {
        this.unstratifiedNumObservations = numObservations;
    }

    /** Returns unstratifiedNumParameters.  @return Returns the unstratifiedNumParameters. */
    public Integer getUnstratifiedNumParameters() {
        return unstratifiedNumParameters;
    }

    /** Set unstratified numParameters.  @param numParameters the unstratified numParameters */
    public void setUnstratifiedNumParameters(Integer numParameters) {
        this.unstratifiedNumParameters = numParameters;
    }

    /**
     * Returns a displayable string for CV status
     * (either "not scheduled to run" or
     * "cross validation doesn't run for this type of model"
     * or the actual status).
     * @return string of LFA status for display.
     */
    public String getCvStatusForDisplay() {
        String display;
        if (cvStatus == null) {
            display = LFA_STATUS_QUEUED;
        } else if (cvStatus.equals(LFA_STATUS_DO_NOT_RUN)) {
            display = "not scheduled to run";
        } else {
            display = cvStatus;
        }
        return display;
    }

    public String getCvStatusPartialDescriptionForDisplay() {
            String display = getCvStatusForDisplay();
            if (!display.equals(CV_STATUS_QUEUED)) {
                    if (cvStatusDescription != null && !cvStatusDescription.equals("")) {
                            //example for display: "unable to run. invalid da...", 5 is the length of ". " plus "..."
                            display += ": " + cvStatusDescription;
                            if (display.length() > CV_STATUS_DISPLAY_MAX_LENGTH) {
                                    display = display.substring(0, CV_STATUS_DISPLAY_MAX_LENGTH - 3) + "...";
                            }
                    }
            }
            return display;
    }
    
    public String getCvStepStatusPartialDescriptionForDisplay() {
            String display = getCvStatusForDisplay();
            if (!display.equals(CV_STATUS_QUEUED)) {
                    if (cvStatusDescription != null && !cvStatusDescription.equals("")) {
                            //example for display: "unable to run. invalid da...", 5 is the length of ". " plus "..."
                            display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE + ". " + CV_STATUS_DESCRIPTION_INCOMPLETE_ITEM;
                            if (display.length() > CV_STATUS_DISPLAY_MAX_LENGTH) {
                                    display = display.substring(0, CV_STATUS_DISPLAY_MAX_LENGTH - 3) + "...";
                            }
                    }
            }
            return display;
    }
    
    public String getCvStudentStatusPartialDescriptionForDisplay() {
            String display = getCvStatusForDisplay();
            if (!display.equals(CV_STATUS_QUEUED)) {
                    if (cvStatusDescription != null && !cvStatusDescription.equals("")) {
                            //example for display: "unable to run. invalid da...", 5 is the length of ". " plus "..."
                            display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE + ". " + CV_STATUS_DESCRIPTION_INCOMPLETE_STUDENT;
                            if (display.length() > CV_STATUS_DISPLAY_MAX_LENGTH) {
                                    display = display.substring(0, CV_STATUS_DISPLAY_MAX_LENGTH - 3) + "...";
                            }
                    }
            }
            return display;
    }

    public String getCvUnstratifiedStatusPartialDescriptionForDisplay() {
            String display = getCvStatusForDisplay();
            if (!display.equals(CV_STATUS_QUEUED)) {
                    if (cvStatusDescription != null && !cvStatusDescription.equals("")) {
                            //example for display: "unable to run. invalid da...", 5 is the length of ". " plus "..."
                            display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE + ". " + CV_STATUS_DESCRIPTION_INCOMPLETE_UNSTRATIFIED;
                            if (display.length() > CV_STATUS_DISPLAY_MAX_LENGTH) {
                                    display = display.substring(0, CV_STATUS_DISPLAY_MAX_LENGTH - 3) + "...";
                            }
                    }
            }
            return display;
    }

    /**
     * Returns displayable string of cvStudentStratifiedRmse.
     * @return string of cvStudentStratifiedRmse if CV status is complete or null if otherwise
     */
    public String getCvStudentStratifiedRmseForDisplay() {
        String display = null;
        if (cvStatus != null
                        && (cvStatus.equals(LFA_STATUS_COMPLETE)
                            || cvStatus.equals(CV_STATUS_IMCOMPLETE))
                        && getCvStudentStratifiedRmse() != null) {
            NumberFormat formatter = new DecimalFormat("###.######");
            display = formatter.format(getCvStudentStratifiedRmse());
        }
        return display;
    }

    /**
     * Returns displayable string of cvStepStratifiedRmse.
     * @return string of cvStepStratifiedRmse if CV status is complete or null if otherwise
     */
    public String getCvStepStratifiedRmseForDisplay() {
        String display = null;
        if (cvStatus != null
                        && (cvStatus.equals(LFA_STATUS_COMPLETE)
                            || cvStatus.equals(CV_STATUS_IMCOMPLETE))
                        && getCvStepStratifiedRmse() != null) {
            NumberFormat formatter = new DecimalFormat("###.######");
            display = formatter.format(getCvStepStratifiedRmse());
        }
        return display;
    }

    /**
     * Returns displayable string of cvStepStratifiedRmse.
     * @return string of cvStepStratifiedRmse if CV status is complete or null if otherwise
     */
    public String getCvUnstratifiedRmseForDisplay() {
        String display = null;
        if (cvStatus != null
                        && (cvStatus.equals(LFA_STATUS_COMPLETE)
                            || cvStatus.equals(CV_STATUS_IMCOMPLETE))
                        && getCvUnstratifiedRmse() != null) {
            NumberFormat formatter = new DecimalFormat("###.######");
            display = formatter.format(getCvUnstratifiedRmse());
        }
        return display;
    }

    /**
     * Returns displayable string of StudentStratified/StepStratified/unstratified RMSE if complete,
     * or the CV status otherwise.
     * @return string of student/step stratified RMSE if CV status is complete, CV status otherwise
     */
    public String getCvStatusOrValuesForDisplay() {
        String display;
        if (cvStatus != null && cvStatus.equals(LFA_STATUS_COMPLETE)) {
            NumberFormat formatter = new DecimalFormat("###.######");
            String cvRmseString;
            if (getCvStudentStratifiedRmse() != null && getCvStepStratifiedRmse() != null
                            && getCvUnstratifiedRmse() != null) {
                cvRmseString = formatter.format(getCvStudentStratifiedRmse()) + " / "
                             + formatter.format(getCvStepStratifiedRmse()) + " / "
                             + formatter.format(getCvUnstratifiedRmse());
            } else {
                cvRmseString = "N/A";
            }
            display = cvRmseString;
        } else {
            display = getCvStatusForDisplay();
        }
        return display;
    }

    /** Returns numSkills.  @return Returns the numSkills. */
    public Integer getNumSkills() {
        return numSkills;
    }

    /** Set numSkills.  @param numSkills the numSkills to set. */
    public void setNumSkills(Integer numSkills) {
        this.numSkills = numSkills;
    }

    /**
     * Formats the skill model name and id.
     * @return The formatted skill model name and id.
     */
    public String getNameAndId() {
        return getNameAndId("'" + getSkillModelName() + "'");
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");
        buffer.append(objectToString("Id", getId()));
        buffer.append(objectToString("Name", getSkillModelName()));
        buffer.append(objectToString("AIC", getAic()));
        buffer.append(objectToString("BIC", getBic()));
        buffer.append(objectToString("Intercept", getIntercept()));
        buffer.append(objectToString("LogLikelihood", getLogLikelihood()));
        buffer.append(objectToString("GlobalFlag", getGlobalFlag()));
        buffer.append(objectToStringFK("Owner", getOwner()));
        buffer.append(objectToStringFK("Dataset", getDataset()));
        buffer.append(objectToString("allowLFAFlag", getAllowLFAFlag()));
        buffer.append(objectToString("status", getStatus()));
        buffer.append(objectToString("lfaStatus", getLfaStatus()));
        buffer.append(objectToString("lfaStatusDescription", getLfaStatusDescription()));
        buffer.append(objectToString("mappingType", getMappingType()));
        buffer.append(objectToString("creationTime", getCreationTime()));
        buffer.append(objectToString("modifiedTime", getModifiedTime()));
        buffer.append(objectToString("numObservations", getNumObservations()));
        buffer.append(objectToString("cvStudentStratifiedRmse", getCvStudentStratifiedRmse()));
        buffer.append(objectToString("cvStepStratifiedRmse", getCvStepStratifiedRmse()));
        buffer.append(objectToString("cvUnstratifiedRmse", getCvUnstratifiedRmse()));
        buffer.append(objectToString("unstratifiedNumObservations",
                getUnstratifiedNumObservations()));
        buffer.append(objectToString("unstratifiedNumParameters", getUnstratifiedNumParameters()));
        buffer.append(objectToString("cvStatus", getCvStatus()));
        buffer.append(objectToString("cvStatusDescription", getCvStatusDescription()));
        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Equals function for this class.
     * @param obj Object of any type, should be an Item for equality check
     * @return boolean true if the items are equal, false if not
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SkillModelItem) {
            SkillModelItem otherItem = (SkillModelItem)obj;

            if (!objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }

            if (!objectEquals(this.getSkillModelName(), otherItem.getSkillModelName())) {
                return false;
            }

            if (!objectEquals(this.getGlobalFlag(), otherItem.getGlobalFlag())) {
                return false;
            }
            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!objectEquals(this.getAllowLFAFlag(), otherItem.getAllowLFAFlag())) {
                return false;
            }
            if (!objectEquals(this.getStatus(), otherItem.getStatus())) {
                return false;
            }
            if (!objectEquals(this.getLfaStatus(), otherItem.getLfaStatus())) {
                return false;
            }
            if (!objectEquals(this.getLfaStatusDescription(), otherItem.getLfaStatusDescription())) {
                    return false;
                }
            if (!objectEquals(this.getSource(), otherItem.getSource())) {
                return false;
            }
            if (!objectEquals(this.getMappingType(), otherItem.getMappingType())) {
                return false;
            }
            if (!objectEquals(this.getCreationTime(), otherItem.getCreationTime())) {
                return false;
            }
            if (!objectEquals(this.getModifiedTime(), otherItem.getModifiedTime())) {
                return false;
            }
            if (!objectEquals(this.getNumObservations(), otherItem.getNumObservations())) {
                return false;
            }
            if (!objectEquals(this.getUnstratifiedNumObservations(),
                         otherItem.getUnstratifiedNumObservations())) {
                return false;
            }
            if (!objectEquals(this.getUnstratifiedNumParameters(),
                         otherItem.getUnstratifiedNumParameters())) {
                return false;
            }
            if (!objectEquals(this.getCvStatus(), otherItem.getCvStatus())) {
                return false;
            }
            if (!objectEquals(this.getCvStatusDescription(), otherItem.getCvStatusDescription())) {
                    return false;
                }
            return true;
        }
        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSkillModelName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getGlobalFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAllowLFAFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLfaStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLfaStatusDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSource());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getMappingType());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreationTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getModifiedTime());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumObservations());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUnstratifiedNumObservations());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUnstratifiedNumParameters());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCvStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCvStatusDescription());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>global_flag</li>
     * <li>name</li>
     * <li>owner</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        SkillModelItem otherItem = (SkillModelItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getGlobalFlag(), otherItem.getGlobalFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSkillModelName(), otherItem.getSkillModelName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAllowLFAFlag(), otherItem.getAllowLFAFlag());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getStatus(), otherItem.getStatus());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLfaStatus(), otherItem.getLfaStatus());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getLfaStatusDescription(), otherItem.getLfaStatusDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSource(), otherItem.getSource());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMappingType(), otherItem.getMappingType());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCreationTime(), otherItem.getCreationTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getModifiedTime(), otherItem.getModifiedTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getNumObservations(), otherItem.getNumObservations());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUnstratifiedNumObservations(),
                           otherItem.getUnstratifiedNumObservations());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUnstratifiedNumParameters(),
                           otherItem.getUnstratifiedNumParameters());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCvStatus(), otherItem.getCvStatus());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getCvStatusDescription(), otherItem.getCvStatusDescription());
        if (value != 0) { return value; }

        return 0;
    }

    /** Abstract inner class to hold the compareByName method used by all the subclasses. */
    public abstract static class AbstractKcmComparator
            implements Comparator<Comparable> {
        /** NumKcsComparator used in all comparisons. */
        protected static final ObsWithKcsComparator obsWithKcsComparator = new ObsWithKcsComparator();
        
        /** Whether this comparator is an ObsWithKcsComparator. */
        protected final boolean isObsWithKcsComparator = this instanceof ObsWithKcsComparator;
        
        /** Whether grouping by the number of observations is enabled. */
        protected boolean groupByNumObservations;
        
        /** Whether the ordering is reversed. */
        protected boolean reversed;

        /**
         * Compare the two skill models by the number of observations, then by the given field in
         * the subclass.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compare(Comparable comparable1, Comparable comparable2) {
            int retValue = 0;
            if (groupByNumObservations && !isObsWithKcsComparator) {
                retValue = obsWithKcsComparator.doCompare(comparable1, comparable2, false);
            }
            if (retValue == 0) {
                if (!reversed) {
                    retValue = doCompare(comparable1, comparable2);
                } else {
                    retValue = doCompare(comparable2, comparable1);
                }
            }
            return retValue;
        }

        /**
         * Compare the two skill models by the given field in the subclass.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        protected abstract int doCompare(Comparable comparable1, Comparable comparable2);
        
        /**
         * Compare the two skill models by the name alphabetically ascending.
         * @param item1 the first skill model item
         * @param item2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compareByName(SkillModelItem item1, SkillModelItem item2) {
            return compareByString(item1.getSkillModelName(), item2.getSkillModelName());
        }

        /**
         * Compare the two String objects, checking for null.
         * @param d1 the first String
         * @param d2 the second String
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compareByString(String d1, String d2) {
            int retValue = 0;
            if ((d1 != null) && (d2 != null)) {
                retValue = d1.compareTo(d2);
            } else if ((d1 == null) && (d2 == null)) {
                retValue = 0;
            } else if (d1 == null) {
                retValue = 1;
            } else if (d2 == null) {
                retValue = -1;
            }
            return retValue;
        }

        /**
         * Compare the two Double objects, checking for null.
         * @param d1 the first Double
         * @param d2 the second Double
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compareByDouble(Double d1, Double d2) {
            int retValue = 0;
            if ((d1 != null) && (d2 != null)) {
                retValue = d1.compareTo(d2);
            } else if ((d1 == null) && (d2 == null)) {
                retValue = 0;
            } else if (d1 == null) {
                retValue = 1;
            } else if (d2 == null) {
                retValue = -1;
            }
            return retValue;
        }

        /**
         * Compare the two Integer objects, checking for null.
         * @param i1 the first Integer
         * @param i2 the second Integer
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compareByInteger(Integer i1, Integer i2) {
            int retValue = 0;
            if ((i1 != null) && (i2 != null)) {
                retValue = i1.compareTo(i2);
            } else if ((i1 == null) && (i2 == null)) {
                retValue = 0;
            } else if (i1 == null) {
                retValue = 1;
            } else if (i2 == null) {
                retValue = -1;
            }
            return retValue;
        }
        /**
         * Compare the two Date objects, checking for null.
         * @param date1 the first date
         * @param date2 the second date
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        public int compareByDate(Date date1, Date date2) {
            int retValue = 0;
            if ((date1 != null) && (date2 != null)) {
                retValue = date1.compareTo(date2);
            } else if (date1 == null) {
                retValue = 1;
            } else if (date2 == null) {
                retValue = -1;
            }
            return retValue;
        }
        
        /**
         * Get whether the ordering is reversed.
         * @return whether the ordering is reversed
         */
        public boolean getReversed() {
            return this.reversed;
        }
        
        /**
         * Set whether the ordering is reversed.
         * @param reversed whether the ordering is reversed
         */
        public void setReversed(boolean reversed) {
            this.reversed = reversed;
        }
        
        /**
         * Get whether grouping by the number of observations is enabled.
         * @return whether grouping by the number of observations is enabled
         */
        public boolean getGroupByNumObservations() {
            return this.groupByNumObservations;
        }
        
        /**
         * Set whether group by the number of observations is enabled
         * @return whether grouping by the number of observations is enabled
         * @param groupByNumObservations
         */
        public void setGroupByNumObservations(boolean groupByNumObservations) {
            this.groupByNumObservations = groupByNumObservations;
        }
    } //end inner class AbstractKcmComparator

    /** Inner class to sort KC Models by AIC ascending where null is last. */
    public static class AicComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDouble(item1.getAic(), item2.getAic());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class AicComparator

    /**
     * Inner class to sort KC Models by BIC ascending where null is last.
     * The preferred sort.
     */
    public static class BicComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDouble(item1.getBic(), item2.getBic());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end class BicComparator

    /** Inner class to sort KC Models by CV student stratified RMSE ascending where null is last. */
    public static class CvStudentStratifiedRmseComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDouble(item1.getCvStudentStratifiedRmse(),
                            item2.getCvStudentStratifiedRmse());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class CvStudentStratifiedRmseComparator

    /** Inner class to sort KC Models by CV step stratified RMSE ascending where null is last. */
    public static class CvStepStratifiedRmseComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDouble(item1.getCvStepStratifiedRmse(),
                            item2.getCvStepStratifiedRmse());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class CvRmseComparator

    /** Inner class to sort KC Models by CV unstratified RMSE ascending where null is last. */
    public static class CvUnstratifiedRmseComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDouble(item1.getCvUnstratifiedRmse(),
                            item2.getCvUnstratifiedRmse());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class CvRmseComparator

    /** Inner class to sort KC Models by the Unstratified Number of Observations. */
    public static class CvObsUnstratifiedComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByInteger(item1.getUnstratifiedNumObservations(),
                                            item2.getUnstratifiedNumObservations());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class CvObsUnstratifiedComparator

    /** Inner class to sort KC Models by CV RMSE ascending where null is last. */
    public static class KCModelNameComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            return compareByName(item1, item2);
        }
    } // end inner class KCModelNameComparator

    /** Inner class to sort KC Models by the Number of Observations. */
    public static class NumKcsComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByInteger(item1.getNumSkills(),
                                            item2.getNumSkills());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;           
        }
    } // end inner class ObsWithKcsComparator

    /** Inner class to sort KC Models by the Number of Observations. */
    public static class ObsWithKcsComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            return doCompare(comparable1, comparable2, true);
        }
        
        /**
         * Compare the two skill models by the given field and optionally by KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @param nameComparisonEnabled whether to compare by KCM Name secondarily
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        protected int doCompare(Comparable comparable1, Comparable comparable2,
                boolean nameComparisonEnabled) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByInteger(item1.getNumObservations(),
                                            item2.getNumObservations());
            if (retValue == 0 && nameComparisonEnabled) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class ObsWithKcsComparator


    /** Inner class to sort KC Models by the Date Created. */
    public static class DateCreatedComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = compareByDate(item1.getCreationTime(),
                                         item2.getCreationTime());
            if (retValue == 0) {
                retValue = compareByName(item1, item2);
            }
            return retValue;
        }
    } // end inner class DateCreatedComparator

    /** Inner class to sort KC Models by the user id of the owner. */
    public static class CreatorComparator extends AbstractKcmComparator {
        /**
         * Compare the two skill models by the given field, then by the KCM Name.
         * @param comparable1 the first skill model item
         * @param comparable2 the second skill model item
         * @return 0 if equal; -1 if it is less than; 1 if it is greater than
         */
        @Override
        protected int doCompare(Comparable comparable1, Comparable comparable2) {
            SkillModelItem item1 = (SkillModelItem)comparable1;
            SkillModelItem item2 = (SkillModelItem)comparable2;
            int retValue = 0;

            UserItem owner1 = item1.getOwner();
            UserItem owner2 = item2.getOwner();

            if ((owner1 != null) && (owner2 != null)) {
                retValue = compareByString((String)item1.getOwner().getId(),
                        (String)item2.getOwner().getId());
                if (retValue == 0) {
                    retValue = compareByName(item1, item2);
                }
            } else if (owner1 == null) {
                retValue = 1;
            } else if (owner2 == null) {
                retValue = -1;
            }

            return retValue;
        }
    } // end inner class CreatorComparator
}