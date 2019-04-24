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
 * Meta-data for remote Knowledge Component (Skill) model.
 * Very similar to a DTO in that it represents data from remote DB.
 *
 * @author Cindy Tipper
 * @version $Revision: 12706 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-20 09:09:25 -0400 (Tue, 20 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class RemoteSkillModelItem extends Item implements java.io.Serializable, Comparable  {

    /** Formatter for AIC and BIC values. */
    private static final NumberFormat AIC = new DecimalFormat("#,###,##0.00");
    /** Formatter for RMSE values. */
    private static final NumberFormat RMSE = new DecimalFormat("##0.000000");

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
    public static final String CV_STATUS_INCOMPLETE = "run but incomplete";

    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE = "CV is incomplete";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_STUDENT = "Failed to compute student stratified CV";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_ITEM = "Failed to compute item stratified CV";
    public static final String CV_STATUS_DESCRIPTION_INCOMPLETE_UNSTRATIFIED = "Failed to compute unstratified CV";

    /** Constant for the maximum length of LFA status display. */
    public static final int LFA_STATUS_DISPLAY_MAX_LENGTH = 30;
    /** Constant for the maximum length of CV status display. */
    public static final int CV_STATUS_DISPLAY_MAX_LENGTH = 40;

    /** Database generated unique Id for this skill model. */
    private Long id;
    /** The name of this skill model as a string. */
    private String skillModelName;
    /** AIC score for this skill model. */
    private Double aic;
    /** BIC score for this skill model. */
    private Double bic;
    /** Log likelihood for this skill model. */
    private Double logLikelihood;
    /** The current status of the LFA regression on this model.*/
    private String lfaStatus;
    /** The current status description of the LFA regression on this model.*/
    private String lfaStatusDescription;
    /** The number of observations that were assigned KCs for this model. */
    private Integer numObservations;
    /** Student stratified Cross validation RMSE for this skill model. */
    private Double cvStudentStratifiedRmse;
    /** Item (ie step) stratified Cross validation RMSE for this skill model. */
    private Double cvStepStratifiedRmse;
    /** unstratified Cross validation RMSE for this skill model. */
    private Double cvUnstratifiedRmse;
    /** The number of observations that were used for unstratified cross validation. */
    private Integer unstratifiedNumObservations;
    /** The number of parameters that were used for unstratified cross validation. */
    private Integer unstratifiedNumParameters;
    /** The current status of cross validation on this model.*/
    private String cvStatus;
    /** The current status description of cross validation on this model.*/
    private String cvStatusDescription;
    /** The number of skill part of this model. */
    private Integer numSkills;

    /** The RemoteDatasetInfo associated with this skill model. */
    private RemoteDatasetInfoItem remoteDatasetInfo;

    /** Default constructor. */
    public RemoteSkillModelItem() { }

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
            } else if (cvStatus != null && cvStatus.equals(CV_STATUS_INCOMPLETE)) {
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
        } else if (cvStatus != null && cvStatus.equals(CV_STATUS_INCOMPLETE)) {
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
        } else if (cvStatus != null && cvStatus.equals(CV_STATUS_INCOMPLETE)) {
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
                display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE
                    + ". " + CV_STATUS_DESCRIPTION_INCOMPLETE_ITEM;
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
                display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE
                    + ". " + CV_STATUS_DESCRIPTION_INCOMPLETE_STUDENT;
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
                display += ": " + CV_STATUS_DESCRIPTION_INCOMPLETE + ". "
                    + CV_STATUS_DESCRIPTION_INCOMPLETE_UNSTRATIFIED;
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
                || cvStatus.equals(CV_STATUS_INCOMPLETE))
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
                || cvStatus.equals(CV_STATUS_INCOMPLETE))
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
                || cvStatus.equals(CV_STATUS_INCOMPLETE))
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
     * Get the remote dataset info item.
     * @return a RemoteDatasetInfoItem object
     */
    public RemoteDatasetInfoItem getRemoteDatasetInfo() {
        return this.remoteDatasetInfo;
    }

    /**
     * Set remote dataset info.
     * @param remoteDatasetInfo RemoteDatasetInfoItem associated with this item
     */
    public void setRemoteDatasetInfo(RemoteDatasetInfoItem remoteDatasetInfo) {
        this.remoteDatasetInfo = remoteDatasetInfo;
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
        buffer.append(objectToString("LogLikelihood", getLogLikelihood()));
        buffer.append(objectToString("lfaStatus", getLfaStatus()));
        buffer.append(objectToString("lfaStatusDescription", getLfaStatusDescription()));
        buffer.append(objectToString("numObservations", getNumObservations()));
        buffer.append(objectToString("cvStudentStratifiedRmse", getCvStudentStratifiedRmse()));
        buffer.append(objectToString("cvStepStratifiedRmse", getCvStepStratifiedRmse()));
        buffer.append(objectToString("cvUnstratifiedRmse", getCvUnstratifiedRmse()));
        buffer.append(objectToString("unstratifiedNumObservations",
                                     getUnstratifiedNumObservations()));
        buffer.append(objectToString("unstratifiedNumParameters", getUnstratifiedNumParameters()));
        buffer.append(objectToString("cvStatus", getCvStatus()));
        buffer.append(objectToString("cvStatusDescription", getCvStatusDescription()));
        buffer.append(objectToString("numSkills", getNumSkills()));
        buffer.append(objectToStringFK("RemoteDatasetInfo", getRemoteDatasetInfo()));
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
        if (obj instanceof RemoteSkillModelItem) {
            RemoteSkillModelItem otherItem = (RemoteSkillModelItem)obj;

            if (!objectEquals(this.getSkillModelName(), otherItem.getSkillModelName())) {
                return false;
            }
            if (!objectEquals(this.getLfaStatus(), otherItem.getLfaStatus())) {
                return false;
            }
            if (!objectEquals(this.getLfaStatusDescription(),
                              otherItem.getLfaStatusDescription())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(this.getSkillModelName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLfaStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLfaStatusDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getNumObservations());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUnstratifiedNumObservations());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getUnstratifiedNumParameters());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCvStatus());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCvStatusDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getRemoteDatasetInfo());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        RemoteSkillModelItem otherItem = (RemoteSkillModelItem)obj;

        int value = 0;

        value = objectCompareTo(this.getSkillModelName(), otherItem.getSkillModelName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLfaStatus(), otherItem.getLfaStatus());
        if (value != 0) { return value; }
        
        value = objectCompareTo(this.getLfaStatusDescription(),
                                otherItem.getLfaStatusDescription());
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

        value = objectCompareToFK(this.getRemoteDatasetInfo(), otherItem.getRemoteDatasetInfo());
        if (value != 0) { return value; }

        return 0;
    }
}