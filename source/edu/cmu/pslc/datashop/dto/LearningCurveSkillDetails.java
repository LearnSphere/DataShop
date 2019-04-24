/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.dto;

import edu.cmu.pslc.datashop.servlet.learningcurve.LearningCurveImage;

/**
 * DTO Object for holding detailed information for the learning curve for a skill.
 * @author hcheng
 * 
 * <!-- $KeyWordsOff: $ -->
 *
 */

public class LearningCurveSkillDetails extends DTO {
    /** The dataset name name. */
    private String datasetName;
    /** The kc model name. */
    private String kcModelName;
    /** The kc name. */
    private String kcName;
    /** learning curve category. */
    private String category;
    /** kc intercept. */
    private Double intercept;
    /** kc slope */
    private Double slope;
    /** number of unique steps. */
    private Integer numberOfUniqueStep;
    /** number of opportunity 1 step instance. */
    private Integer numberOfOpportunityOneStepInstances;
    /** number of step instance. */
    private Integer numberOfStepInstances;

    /**
     * Default Constructor.
     */
    public LearningCurveSkillDetails() {}

    /**
     * Return the dataset name.
     * @return the kcModelName
     */
    public String getDatasetName() {
        return datasetName;
    }
    /**
     * Set the datasetName.
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    /**
     * Return the KC model name.
     * @return the kcModelName
     */
    public String getKcModelName() {
        return kcModelName;
    }
    /**
     * Set the kcModelNamee.
     * @param kcModelNamee the kcModelNamee to set
     */
    public void setKcModelName(String kcModelName) {
        this.kcModelName = kcModelName;
    }
    /**
     * Return the KC name.
     * @return the kcName
     */
    public String getKcName() {
        return kcName;
    }
    /**
     * Set the kcName.
     * @param kcName the kcName to set
     */
    public void setKcName(String kcName) {
        this.kcName = kcName;
    }
    /**
     * Return the category.
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    /**
     * Set the category.
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    /**
     * Return the intercept.
     * @return the intercept
     */
    public Double getIntercept() {
        return intercept;
    }
    /**
     * Set the intercept.
     * @param intercept the intercept to set
     */
    public void setIntercept(double intercept) {
        this.intercept = intercept;
    }
    /**
     * Return the numberOfUniqueStep.
     * @return the numberOfUniqueStep
     */
    public Integer getNumberOfUniqueStep() {
        return numberOfUniqueStep;
    }
    /**
     * Set the numberOfUniqueStep.
     * @param numberOfUniqueStep the numberOfUniqueStep to set
     */
    public void setNumberOfUniqueStep(int numberOfUniqueStep) {
        this.numberOfUniqueStep = numberOfUniqueStep;
    }
    /**
     * Return the numberOfStepInstances.
     * @return the numberOfStepInstances
     */
    public Integer getNumberOfStepInstances() {
        return numberOfStepInstances;
    }
    /**
     * Set the numberOfStepInstances.
     * @param slope the numberOfStepInstances to set
     */
    public void setNumberOfStepInstances(int numberOfStepInstances) {
        this.numberOfStepInstances = numberOfStepInstances;
    }
    /**
     * Return the numberOfOpportunityOneStepInstances.
     * @return the numberOfOpportunityOneStepInstances
     */
    public Integer getNumberOfOpportunityOneStepInstances() {
        return numberOfOpportunityOneStepInstances;
    }
    /**
     * Set the numberOfOpportunityOneStepInstances.
     * @param numberOfOpportunityOneStepInstances numberOfOpportunityOneStepInstances to set
     */
    public void setNumberOfOpportunityOneStepInstances(int numberOfOpportunityOneStepInstances) {
        this.numberOfOpportunityOneStepInstances = numberOfOpportunityOneStepInstances;
    }
    /**
     * Return the slope.
     * @return the slope
     */
    public Double getSlope() {
        return slope;
    }
    /**
     * Set the slope.
     * @param slope the slope to set
     */
    public void setSlope(double slope) {
        this.slope = slope;
    }
    public String writeToString(String delimiter) {
            StringBuffer sb = new StringBuffer(getKcModelName());
            sb.append(delimiter);
            sb.append(getKcName());
            sb.append(delimiter);
            if (getCategory() != null) {
                    if (getCategory().equals(LearningCurveImage.CLASSIFIED_OTHER))
                            sb.append(LearningCurveImage.CLASSIFIED_OTHER_LABEL);
                    else
                        sb.append(getCategory());
            } else
                    sb.append("");
            sb.append(delimiter);
            if (getIntercept() != null)
                    sb.append(getIntercept());
            else
                    sb.append("");
            sb.append(delimiter);
            if (getSlope() != null)
                    sb.append(getSlope());
            else
                    sb.append("");
            sb.append(delimiter);
            if (getNumberOfUniqueStep() != null)
                    sb.append(getNumberOfUniqueStep());
            else
                    sb.append("0");
            sb.append(delimiter);
            if (getNumberOfOpportunityOneStepInstances() != null)
                    sb.append(getNumberOfOpportunityOneStepInstances());
            else
                    sb.append("0");
            sb.append(delimiter);
            if (getNumberOfStepInstances() != null)
                    sb.append(getNumberOfStepInstances());
            else
                    sb.append("0");
            return sb.toString();    
    }
    static public String writeHeader(String delimiter) {
            StringBuffer sb = new StringBuffer("KC Model");
            sb.append(delimiter);
            sb.append("KC Name");
            sb.append(delimiter);
            sb.append("Category");
            sb.append(delimiter);
            sb.append("KC Intercept");
            sb.append(delimiter);
            sb.append("KC Slope");
            sb.append(delimiter);
            sb.append("# unique steps");
            sb.append(delimiter);
            sb.append("# opportunity 1 step instances");
            sb.append(delimiter);
            sb.append("# step instances");
            return sb.toString();
    }
} // end LearningCurvePointInfoDetails.java
