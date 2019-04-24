/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import edu.cmu.pslc.datashop.item.ProjectItem;

/**
 * Used to transfer project data as XML, JSON, etc.
 *
 * @author Shanwen Yu
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "dataset",
    properties = { "id", "name", "primaryInvestigator",
        "dataProvider", "projectTermsOfUse" })
public class ProjectInfoReport extends DTO {
    /** the project item */
    private ProjectItem projectItem;
    /** the dataset name */
    private String name;
    /** the primary investigator user id*/
    private String primaryInvestigatorId;
    /** the primary investigator name*/
    private String primaryInvestigatorName;
    /** the data provider user id*/
    private String dataProviderId;
    /** the data provider name*/
    private String dataProviderName;
    /** Project Terms of Use associated to this dataset */
    private String projectTerms;
    /** DataShop Terms of Use associated to this dataset */
    private String dataShopTerms;
    /** String value of effective date for the terms of use that is associated with this dataset */
    private String projectTermsEffectiveDate;
    /** String value of effective date for the terms of use that is associated with this dataset */
    private String dataShopTermsEffectiveDate;
    /** indicates whether the project is public or not */
    private Boolean publicFlag;
    /** the number of papers associated with the datasets of this project */
    private Long numPapers;
    /** indicates whether the project terms are accepted **/
    private Boolean termsAcceptedFlag;
    /** indicates whether the project is a 'Discourse dataset'. **/
    private Boolean isDiscourseDataset;

    /** Default constructor. */
    public ProjectInfoReport() {
    }

    /**
     * Returns the project item.
     * @return the project item.
     */
    public ProjectItem getProjectItem() {
        return this.projectItem;
    }

    /**
     * Sets the project item.
     * @param item the project item
     */
    public void setProjectItem(ProjectItem item) {
        this.projectItem = item;
    }

    /** the project ownership. @return the project ownership */
    public String getOwnership() { return projectItem.getOwnership(); }

    /** the project name. @return the project name */
    public String getName() { return name; }

    /** the project name. @param name the project name */
    public void setName(String name) { this.name = name; }

    /** the primary investigator. @return the primary investigator */
    public String getPrimaryInvestigatorId() { return primaryInvestigatorId; }

    /** the primary investigator. @param primaryInvestigatorId the user id of the PI */
    public void setPrimaryInvestigatorId(String primaryInvestigatorId) {
        this.primaryInvestigatorId = primaryInvestigatorId;
    }

    /** the primary investigator. @return the primary investigator */
    public String getPrimaryInvestigatorName() { return primaryInvestigatorName; }

    /** the primary investigator. @param primaryInvestigatorName the name of the PI */
    public void setPrimaryInvestigatorName(String primaryInvestigatorName) {
        this.primaryInvestigatorName = primaryInvestigatorName;
    }

    /** the data provider. @return the data provider */
    public String getDataProviderId() { return dataProviderId; }

    /** the data provider. @param dataProviderId the user id of the data provider */
    public void setDataProviderId(String dataProviderId) { this.dataProviderId = dataProviderId; }

    /** the data provider. @return the data provider */
    public String getDataProviderName() { return dataProviderName; }

    /** the data provider. @param dataProviderName name of the data provider */
    public void setDataProviderName(String dataProviderName) {
        this.dataProviderName = dataProviderName;
    }

    /**
     * Returns the project terms of use.
     * @return the project terms of use.
     */
    public String getProjectTerms() {
        return this.projectTerms;
    }
    /**
     * Sets the project terms of use.
     * @param projectTerms the project terms of use.
     */
    public void setProjectTerms(String projectTerms) {
        this.projectTerms = projectTerms;
    }

    /**
     * Returns the datashop terms of use.
     * @return the datashop terms of use.
     */
    public String getDataShopTerms() {
        return this.dataShopTerms;
    }
    /**
     * Sets the datashop terms of use.
     * @param dataShopTerms the datashop terms of use.
     */
    public void setDataShopTerms(String dataShopTerms) {
        this.dataShopTerms = dataShopTerms;
    }

    /**
     * Returns the effective date for project terms of use.
     * @return the effective date for project terms of use.
     */
    public String getProjectTermsEffectiveDate() {
        return this.projectTermsEffectiveDate;
    }

    /**
     * Sets the effective date for project terms of use.
     * @param projectTermsEffectiveDate the effective date for project terms of use.
     */
    public void setProjectTermsEffectiveDate(String projectTermsEffectiveDate) {
        this.projectTermsEffectiveDate = projectTermsEffectiveDate;
    }

    /**
     * Returns the effective date for DataShop terms of use.
     * @return the effective date for DataShop terms of use.
     */
    public String getDataShopTermsEffectiveDate() {
        return this.dataShopTermsEffectiveDate;
    }

    /**
     * Sets the effective date for DataShop terms of use.
     * @param dataShopTermsEffectiveDate the effective date for DataShop terms of use.
     */
    public void setDataShopTermsEffectiveDate(String dataShopTermsEffectiveDate) {
        this.dataShopTermsEffectiveDate = dataShopTermsEffectiveDate;
    }

    /**
     * Sets the flag indicating if the project is public.
     * @param publicFlag the publicFlag to set
     */
    public void setPublicFlag(Boolean publicFlag) {
        this.publicFlag = publicFlag;
    }

    /**
     * Returns the flag indicating if the project is public.
     * @return the flag indicating if the project is public
     */
    public Boolean getPublicFlag() {
        return publicFlag;
    }

    /**
     * Sets the number of papers for datasets in this project.
     * @param numPapers to set
     */
    public void setNumPapers(Long numPapers) {
        this.numPapers = numPapers;
    }

    /**
     * Returns the number of papers for datasets in this project.
     * @return the number of papers for datasets in this project
     */
    public Long getNumPapers() {
        return numPapers;
    }

    /**
     * Sets the flag indicating whether the user accepted the terms.
     * @param termsAcceptedFlag the termsAcceptedFlag to set
     */
    public void setTermsAcceptedFlag(Boolean termsAcceptedFlag) {
        this.termsAcceptedFlag = termsAcceptedFlag;
    }

    /**
     * Gets the flag indicating whether the user accepted the terms.
     * @return the termsAcceptedFlag
     */
    public Boolean getTermsAcceptedFlag() {
        return termsAcceptedFlag;
    }

    /**
     * Sets the flag indicating whether this is a discourse 'project'.
     * @param isDiscourseDataset the flag
     */
    public void setIsDiscourseDataset(Boolean isDiscourseDataset) {
        this.isDiscourseDataset = isDiscourseDataset;
    }

    /**
     * Gets the flag indicating whether this is a discourse 'project'.
     * @return Boolean the flag
     */
    public Boolean getIsDiscourseDataset() {
        return isDiscourseDataset;
    }

}
