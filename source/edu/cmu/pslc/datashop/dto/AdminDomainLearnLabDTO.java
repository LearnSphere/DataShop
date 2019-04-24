/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import edu.cmu.pslc.datashop.servlet.HelperFactory;
import edu.cmu.pslc.datashop.servlet.datasetinfo.DatasetInfoEditHelper;

/**
 * Holds the data needed for the administrator tool: Set Domain/LearnLab.
 *
 * @author alida
 * @version $Revision: 7378 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-12-02 16:32:13 -0500 (Fri, 02 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AdminDomainLearnLabDTO extends DTO {

    /** The dataset id. */
    private int datasetId;
    /** The dataset name. */
    private String datasetName;
    /** The primary investigator. */
    private String primaryInvestigator;
    /** The number of student hours. */
    private String studentHours;
    /** The domain. */
    private String domain;
    /** The learnlab. */
    private String learnlab;
    /** The junk flag. */
    private Boolean junkFlag;

    /**
     * Constructor using fields.
     * @param datasetId the dataset id
     * @param datasetName the dataset name
     * @param primaryInvestigator the primary investigator
     * @param studentHours the number of student hours
     * @param domain the domain name
     * @param learnlab the learnlab name
     * @param junkFlag the junk flag
     */
    public AdminDomainLearnLabDTO(int datasetId, String datasetName,
            String primaryInvestigator,
            String studentHours,
            String domain,
            String learnlab,
            Boolean junkFlag) {
        super();
        this.datasetId = datasetId;
        this.datasetName = datasetName;
        this.primaryInvestigator = primaryInvestigator;
        this.studentHours = studentHours;
        this.domain = domain;
        this.learnlab = learnlab;
        this.junkFlag = junkFlag;
    }


    /** HTML. */
    private static final String SELECTED_HTML = " SELECTED ";

    /**
     * Returns the HTML to create the Domain/LearnLab option menu for this object.
     * @return the HTML for the option menu
     */
    public String getDomainLearnLabOptionMenu() {
        DatasetInfoEditHelper datasetInfoEditHelper =
                        HelperFactory.DEFAULT.getDatasetInfoEditHelper();
        Object[] menuOption =  datasetInfoEditHelper.getDomainLearnlabList().toArray();

        String selectName = "adl-domain-learnlab-option-menu-" + datasetId;
        String html = "<select id=\"" + selectName + "\""
            + " onchange=\"javascript:saveDomainLearnLabChange("
            + "'" + selectName + "', '" + datasetId + "')\""
            + ">";
        String domainLearnLab = "";
        if (domain != null) {
            domainLearnLab = domain;
        }
        if ((learnlab != null) && (!domain.equals("Other"))) {
           domainLearnLab = domainLearnLab + "/" + learnlab;
        }

        String selected = "";

        if (menuOption != null) {
            html += "<option> </option>";
            for (int idx = 0; idx < menuOption.length; idx++) {
                if (domainLearnLab.equals(menuOption[idx])) {
                    selected = SELECTED_HTML;
                } else {
                    selected = "";
                }
                html += "<option " + selected + ">";
                html += menuOption[idx];
                html += "</option>";
            }
        }
        html += "</select>";
        return html;
    }

    /**
     * Returns the HTML to create the junk flag option menu for this object.
     * @return the HTML for the option menu
     */
    public String getJunkFlagOptionMenu() {

        String selectName = "adl-junk-flag-option-menu-" + datasetId;
        String html = "<select id=\"" + selectName + "\""
            + " onchange=\"javascript:saveJunkFlagChange("
            + "'" + selectName + "', '" + datasetId + "')\""
            + ">";
        if (junkFlag != null && junkFlag) {
            html += "<option " + SELECTED_HTML + ">";
            html += "true";
            html += "</option>";
            html += "<option>";
            html += "false";
            html += "</option>";
        } else {
            html += "<option>";
            html += "true";
            html += "</option>";
            html += "<option " + SELECTED_HTML + ">";
            html += "false";
            html += "</option>";
        }
        html += "</select>";

        return html;
    }

    /**
     * Returns the dataset id.
     * @return the datasetId
     */
    public int getDatasetId() {
        return datasetId;
    }
    /**
     * Sets the dataset id.
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }
    /**
     * Returns the dataset name.
     * @return the datasetName
     */
    public String getDatasetName() {
        return datasetName;
    }
    /**
     * Sets the dataset name.
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    /**
     * Returns the primary investigator.
     * @return the primaryInvestigator
     */
    public String getPrimaryInvestigator() {
        return primaryInvestigator;
    }
    /**
     * Sets the primary investigator.
     * @param primaryInvestigator the primaryInvestigator to set
     */
    public void setPrimaryInvestigator(String primaryInvestigator) {
        this.primaryInvestigator = primaryInvestigator;
    }
    /**
     * Returns the number of student hours.
     * @return the studentHours
     */
    public String getStudentHours() {
        return studentHours;
    }
    /**
     * Sets the number of student hours.
     * @param studentHours the studentHours to set
     */
    public void setStudentHours(String studentHours) {
        this.studentHours = studentHours;
    }
    /**
     * Returns the domain as a string.
     * @return the domain
     */
    public String getDomainName() {
        return domain;
    }
    /**
     * Sets the domain.
     * @param domain the domain to set
     */
    public void setDomainName(String domain) {
        this.domain = domain;
    }
    /**
     * Returns the learnlab.
     * @return the learnlab
     */
    public String getLearnlabName() {
        return learnlab;
    }
    /**
     * Sets the learnlab.
     * @param learnlab the learnlab to set
     */
    public void setLearnlabName(String learnlab) {
        this.learnlab = learnlab;
    }
    /**
     * Checks the junk flag.
     * @return the junkFlag
     */
    public boolean isJunkFlag() {
        return junkFlag;
    }
    /**
     * Sets the junk flag.
     * @param junkFlag the junkFlag to set
     */
    public void setJunkFlag(boolean junkFlag) {
        this.junkFlag = junkFlag;
    }
}
