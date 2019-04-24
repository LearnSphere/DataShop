/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Used to transfer KC model data as XML, JSON, etc.
 *
 * @author Jim Rankin
 * @version $Revision: 12454 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2015-07-01 16:10:52 -0400 (Wed, 01 Jul 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
// The order of the properties here determines the order of the elements returned in the XML.
@DTO.Properties(root = "kcModel",
        properties = { "id", "name", "numberOfKcs",
        "observationsWithKcs", "numberOfParameters", "logisticRegressionModelStatus",
        "logisticRegressionModelStatusDescription",
        "aic", "bic", "logLikelihood",
        "crossValidationStatus", "crossValidationStatusDescription",
        "unstratifiedCrossValidationRmse",
        "studentStratifiedCrossValidationRmse",
        //step and item are synonym here
        "stepStratifiedCrossValidationRmse",
        "unstratifiedNumberOfParameters",
        "unstratifiedNumberOfObservations" })
public class KcModelDTO extends DTO {
    /**kc model id*/
    private Integer id;
    /** The KC model name. */
    private String name;
    /** The number of KCs in this model. */
    private Integer numberOfKcs;
    /** The number of observations that were assigned KCs for this model. */
    private Integer observationsWithKcs;
    /** The current status of the LFA regression on this model. */
    private String logisticRegressionModelStatus;
    /** The current status description of the LFA regression on this model. */
    private String logisticRegressionModelStatusDescription;
    /** AIC score. */
    private Double aic;
    /** BIC score. */
    private Double bic;
    /** Log likelihood. */
    private Double logLikelihood;
    /** The number of parameters used for LFA-AFM. */
    private Integer numberOfParameters;
    /** student stratified cross validation RMSE */
    private Double studentStratifiedCrossValidationRmse;
    /** step stratified cross validation RMSE */
    private Double stepStratifiedCrossValidationRmse;
    /** unstratified cross validation RMSE */
    private Double unstratifiedCrossValidationRmse;
    /** unstratified cross validation number of observations */
    private Integer unstratifiedNumberOfObservations;
    /** unstratified cross validation number of parameters */
    private Integer unstratifiedNumberOfParameters;
    /** the current status of the cross validation on this model */
    private String crossValidationStatus;
    /** the current status description of the cross validation on this model */
    private String crossValidationStatusDescription;

    /** The KC modelid. @return the KC model id */
    public Integer getId() { return id; }

    /** The KC model id. @param id the KC model id */
    public void setId(Integer id) { this.id = id; }

    /** The KC model name. @return the KC model name */
    public String getName() { return name; }

    /** The KC model name. @param name the KC model name */
    public void setName(String name) { this.name = name; }

    /**
     * The number of KCs in this model.
     * @return the number of KCs in this model
     */
    public Integer getNumberOfKcs() { return numberOfKcs; }

    /**
     * The number of KCs in this model.
     * @param numberOfKcs the number of KCs in this model
     */
    public void setNumberOfKcs(Integer numberOfKcs) { this.numberOfKcs = numberOfKcs; }

    /**
     * The number of observations that were assigned KCs for this model.
     * @return the number of observations that were assigned KCs for this model
     */
    public Integer getObservationsWithKcs() { return observationsWithKcs; }

    /**
     * The number of observations that were assigned KCs for this model.
     * @param observationsWithKcs the number of observations that were assigned KCs for this model
     */
    public void setObservationsWithKcs(Integer observationsWithKcs) {
        this.observationsWithKcs = observationsWithKcs;
    }

    /**
     * The current status of the LFA regression on this model.
     * @return the current status of the LFA regression on this model
     */
    public String getLogisticRegressionModelStatus() { return logisticRegressionModelStatus; }

    /**
     * The current status of the LFA regression on this model.
     * @param logisticRegressionModelStatus the current status of the LFA regression on this model
     */
    public void setLogisticRegressionModelStatus(String logisticRegressionModelStatus) {
        this.logisticRegressionModelStatus = logisticRegressionModelStatus;
    }

    /**
     * The current status description of the LFA regression on this model.
     * @return the current status description of the LFA regression on this model
     */
    public String getLogisticRegressionModelStatusDescription() { return logisticRegressionModelStatusDescription; }

    /**
     * The current status description of the LFA regression on this model.
     * @param logisticRegressionModelStatusDescription the current status description of the LFA regression on this model
     */
    public void setLogisticRegressionModelStatusDescription(String logisticRegressionModelStatusDescription) {
        this.logisticRegressionModelStatusDescription = logisticRegressionModelStatusDescription;
    }

    /** AIC score. @return AIC score */
    public Double getAic() { return aic; }

    /** AIC score. @param aic AIC score */
    public void setAic(Double aic) { this.aic = aic; }

    /** BIC score. @return BIC score */
    public Double getBic() { return bic; }

    /** BIC score. @param bic BIC score */
    public void setBic(Double bic) { this.bic = bic; }

    /** Set the log likelihood value. @param logLikelihood log likelihood */
    public void setLogLikelihood(Double logLikelihood) { this.logLikelihood = logLikelihood; }

    /** Returns the log likelihood value. @return the log likelihood value */
    public Double getLogLikelihood() { return logLikelihood; }

    /** Number of parameters. @param numberOfParameters the number of parameters */
    public void setNumberOfParameters(Integer numberOfParameters) {
        this.numberOfParameters = numberOfParameters;
    }

    /** Number of parameters. @return the number of parameters */
    public Integer getNumberOfParameters() { return numberOfParameters; }

    /** Student Stratified cross validation RMSE. @param crossValidationRmse the
     * student stratified cross validation RMSE */
    public void setStudentStratifiedCrossValidationRmse(Double crossValidationRmse) {
        this.studentStratifiedCrossValidationRmse = crossValidationRmse;
    }
    /** Student stratified cross validation RMSE. @return student stratified CV RMSE */
    public Double getStudentStratifiedCrossValidationRmse() {
            return studentStratifiedCrossValidationRmse; }

    /** Step Stratified cross validation RMSE. @param crossValidationRmse the
     * step stratified cross validation RMSE */
    public void setStepStratifiedCrossValidationRmse(Double crossValidationRmse) {
        this.stepStratifiedCrossValidationRmse = crossValidationRmse;
    }
    /** Step stratified cross validation RMSE. @return step stratified cross validation RMSE */
    public Double getStepStratifiedCrossValidationRmse() {
            return stepStratifiedCrossValidationRmse; }

    /** unstratified cross validation RMSE. @param crossValidationRmse the
     * unstratified cross validation RMSE */
    public void setUnstratifiedCrossValidationRmse(Double crossValidationRmse) {
        this.unstratifiedCrossValidationRmse = crossValidationRmse;
    }
    /** Student stratified cross validation RMSE. @return unstratified cross validation RMSE */
    public Double getUnstratifiedCrossValidationRmse() {
            return unstratifiedCrossValidationRmse; }

    /**
     * The number of observations that were used in unstratified cross validation.
     * @param numberOfObservations the number of observations in unstratified CV
     */
    public void setUnstratifiedNumberOfObservations(
            Integer numberOfObservations) {
        this.unstratifiedNumberOfObservations = numberOfObservations;
    }

    /** Unstratified CV number of observations. @return number of observations */
    public Integer getUnstratifiedNumberOfObservations() {
        return unstratifiedNumberOfObservations;
    }

    /** unstratified CV number of parameters.
     * @param numberOfParameters unstratified CV number of parameters */
    public void setUnstratifiedNumberOfParameters(Integer numberOfParameters) {
        this.unstratifiedNumberOfParameters = numberOfParameters;
    }

    /** unstratified CV number of parameters.
     * @return unstratified CV number of parameters */
    public Integer getUnstratifiedNumberOfParameters() {
        return unstratifiedNumberOfParameters;
    }

    /** CV status. @param crossValidationStatus CV status*/
    public void setCrossValidationStatus(String crossValidationStatus) {
        this.crossValidationStatus = crossValidationStatus;
    }

    /** CV status. @return CV status*/
    public String getCrossValidationStatus() { return crossValidationStatus; }

    /** CV status description. @param crossValidationStatusDescription CV status description*/
    public void setCrossValidationStatusDescription(String crossValidationStatusDescription) {
        this.crossValidationStatusDescription = crossValidationStatusDescription;
    }

    /** CV status description. @return CV status description*/
    public String getCrossValidationStatusDescription() { return crossValidationStatusDescription; }

}
