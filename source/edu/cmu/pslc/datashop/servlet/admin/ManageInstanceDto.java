/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.admin;

/**
 * Data Transfer Object for the 'Manage Instance' UI.
 *
 * @author Cindy Tipper
 * @version $Revision: 15738 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ManageInstanceDto {

    //----- ATTRIBUTES -----

    /** User that configured the instance. */
    private String configuredBy;
    /** Time this item was configured. */
    private String configuredTime;
    /** URL */
    private String datashopUrl;

    /* Datashop Instance properties */
    /** Flag indicating if this instance is a slave. */
    private Boolean isSlave;
    /** The identifier for the instance; defaults to hostname if not set. */
    private String slaveId;
    /** The name of user for authenticating remote requests to the master. */
    private String masterUser;
    /** The schema which defines remote communications to the master. */
    private String masterSchema;
    /** The URL for the master instance. */
    private String masterUrl;
    /** The public API token for web services. */
    private String slaveApiToken;
    /** The secret key for encrypting the authentication header. */
    private String slaveSecret;

    /* Email properties */
    /** Flag indicating if emails are being sent. */
    private Boolean isSendmailActive;
    /** Email address for datashop-help on this instance. */
    private String datashopHelpEmail;
    /** Email address for datashop-rm on this instance. */
    private String datashopRmEmail;
    /** Email address for datashop-bucket on this instance. */
    private String datashopBucketEmail;
    /** SMTP host for this instance. */
    private String datashopSmtpHost;
    /** SMTP port for this instance. */
    private String datashopSmtpPort;
    /** Flag indicating if secure SMTP is being used. */
    private Boolean useSslSmtp;
    /** SMTP user for this instance. Only relevant for SSL. */
    private String datashopSmtpUser;
    /** SMTP password for this instance. Only relevant for SSL. */
    private String datashopSmtpPassword;

    /* GitHub properties. **/
    /** Client ID for GitHub OAuth app. */
    private String githubClientId;
    /** Client secret for GitHub OAuth app. */
    private String githubClientSecret;

    /* Workflow properties. **/
    /** Workflow Components directory. */
    private String wfcDir;
    /** Workflow Components remote slave instance. */
    private String wfcRemote;
    /** Workflow Components heap size. */
    private Long wfcHeapSize;

    /** Default constructor. */
    public ManageInstanceDto() { }

    /**
     * Get the userId that configured this instance.
     * @return String userId
     */
    public String getConfiguredBy() { return this.configuredBy; }

    /**
     * Set the userId that configured this instance.
     * @param configuredBy name/id of the user
     */
    public void setConfiguredBy(String configuredBy) { this.configuredBy = configuredBy; }

    /**
     * Get the time this instance was configured.
     * @return String the time, formatted
     */
    public String getConfiguredTime() { return this.configuredTime; }

    /**
     * Set the time this instance was configured.
     * @param configuredTime the formatted Date
     */
    public void setConfiguredTime(String configuredTime) { this.configuredTime = configuredTime; }

    /**
     * Get the flag indicating if this instance is a slave.
     * @return Boolean the flag
     */
    public Boolean getIsSlave() { return this.isSlave; }

    /**
     * Set the flag indicating if this instance is a slave.
     * @param isSlave the flag
     */
    public void setIsSlave(Boolean isSlave) { this.isSlave = isSlave; }

    /**
     * Get the identifier for this slave instance.
     * @return String instance identifier
     */
    public String getSlaveId() { return this.slaveId; }

    /**
     * Set the identifier for this slave instance.
     * @param slaveId the instance identifier
     */
    public void setSlaveId(String slaveId) { this.slaveId = slaveId; }

    /**
     * Get the name of the remote user for authenticating requests to the master.
     * @return String masterUser
     */
    public String getMasterUser() { return this.masterUser; }

    /**
     * Set the remote user for authenticating requests to the master.
     * @param masterUser the user ID
     */
    public void setMasterUser(String masterUser) { this.masterUser = masterUser; }

    /**
     * Get the schema used for remote communications with the master.
     * @return String a link to the schema
     */
    public String getMasterSchema() { return this.masterSchema; }

    /**
     * Set the schema used for remote communications with the master.
     * @param masterSchema link to the schema
     */
    public void setMasterSchema(String masterSchema) { this.masterSchema = masterSchema; }

    /**
     * Get the URL for the master instance.
     * @return String the URL for the master DataShop instance
     */
    public String getMasterUrl() { return this.masterUrl; }

    /**
     * Set the URL for the master instance.
     * @param masterUrl the URL
     */
    public void setMasterUrl(String masterUrl) { this.masterUrl = masterUrl; }

    /**
     * The public API token for this slave instance.
     * @return String the public API token for web services
     */
    public String getSlaveApiToken() { return this.slaveApiToken; }

    /**
     * The public API token for this slae instance.
     * @param apiToken the public API token for web services
     */
    public void setSlaveApiToken(String slaveApiToken) { this.slaveApiToken = slaveApiToken; }

    /**
     * The secret key for encrypting the authentication header.
     * @return the secret key for encrypting the authentication header
     */
    public String getSlaveSecret() { return this.slaveSecret; }

    /**
     * The secret key for encrypting the authentication header.
     * @param slaveSecret the secret key for encrypting the authentication header
     */
    public void setSlaveSecret(String slaveSecret) { this.slaveSecret = slaveSecret; }

    /**
     * Get the Datashop URL for this instance.
     * @return String the url
     */
    public String getDatashopUrl() { return this.datashopUrl; }

    /**
     * Set the Datashop URL for this instance.
     * @param url the URL
     */
    public void setDatashopUrl(String url) { this.datashopUrl = url; }

    /**
     * Get the flag indicating if emails are being sent.
     * @return Boolean the flag
     */
    public Boolean getIsSendmailActive() { return this.isSendmailActive; }

    /**
     * Set the flag indicating if emails are being sent.
     * @param isSendmailActive the flag
     */
    public void setIsSendmailActive(Boolean isSendmailActive) {
        this.isSendmailActive = isSendmailActive;
    }

    /**
     * Get the email address for datashop-help for this instance.
     * @return String datashopHelpEmail
     */
    public String getDatashopHelpEmail() { return this.datashopHelpEmail; }

    /**
     * Set the email address for datashop-help for this instance.
     * @param datashopHelpEmail the email addres
     */
    public void setDatashopHelpEmail(String datashopHelpEmail) {
        this.datashopHelpEmail = datashopHelpEmail;
    }

    /**
     * Get the email address for datashop Research Manager for this instance.
     * @return String datashopRmEmail
     */
    public String getDatashopRmEmail() { return this.datashopRmEmail; }

    /**
     * Set the email address for datashop Research Manager for this instance.
     * @param datashopRmEmail the email addres
     */
    public void setDatashopRmEmail(String datashopRmEmail) {
        this.datashopRmEmail = datashopRmEmail;
    }

    /**
     * Get the email address for datashop-bucket for this instance.
     * @return String datashopBucketEmail
     */
    public String getDatashopBucketEmail() { return this.datashopBucketEmail; }

    /**
     * Set the email address for datashop-bucket for this instance.
     * @param datashopBucketEmail the email addres
     */
    public void setDatashopBucketEmail(String datashopBucketEmail) {
        this.datashopBucketEmail = datashopBucketEmail;
    }

    /**
     * Get the SMTP host for this Datashop Instance.
     * @return String datashopSmtpHost
     */
    public String getDatashopSmtpHost() { return this.datashopSmtpHost;}

    /**
     * Set the SMTP host for this DataShop Instance.
     * @param datashopSmtpHost the host
     */
    public void setDatashopSmtpHost(String datashopSmtpHost) {
        this.datashopSmtpHost = datashopSmtpHost;
    }

    /**
     * Get the SMTP port for this Datashop Instance, as a String.
     * @return String datashopSmtpPort
     */
    public String getDatashopSmtpPort() { return this.datashopSmtpPort;}

    /**
     * Set the SMTP port for this DataShop Instance, as a String.
     * @param datashopSmtpPort the port
     */
    public void setDatashopSmtpPort(String datashopSmtpPort) {
        this.datashopSmtpPort = datashopSmtpPort;
    }

    /**
     * Get the flag indicating if SSL SMTP is in use.
     * @return Boolean useSslSmtp
     */
    public Boolean getUseSslSmtp() { return this.useSslSmtp; }

    /**
     * Set the flag indicating if SSL SMTP is in use.
     * @param useSslSmtp the flag
     */
    public void setUseSslSmtp(Boolean useSslSmtp) {
        this.useSslSmtp = useSslSmtp;
    }

    /**
     * Get the SMTP user for this Datashop Instance.
     * @return String datashopSmtpUser
     */
    public String getDatashopSmtpUser() { return this.datashopSmtpUser;}

    /**
     * Set the SMTP user for this DataShop Instance.
     * @param datashopSmtpUser the user
     */
    public void setDatashopSmtpUser(String datashopSmtpUser) {
        this.datashopSmtpUser = datashopSmtpUser;
    }

    /**
     * Get the SMTP password for this Datashop Instance.
     * @return String datashopSmtpPassword
     */
    public String getDatashopSmtpPassword() { return this.datashopSmtpPassword;}

    /**
     * Set the SMTP password for this DataShop Instance.
     * @param datashopSmtpPassword the password
     */
    public void setDatashopSmtpPassword(String datashopSmtpPassword) {
        this.datashopSmtpPassword = datashopSmtpPassword;
    }

    /**
     * Get the GitHub OAuth app client id.
     * @return String githubClientId
     */
    public String getGithubClientId() { return githubClientId; }

    /**
     * Set the GitHub OAuth app client id.
     * @param githubClientId
     */
    public void setGithubClientId(String githubClientId) { this.githubClientId = githubClientId; }

    /**
     * Get the GitHub OAuth app client secret.
     * @return String githubClientSecret
     */
    public String getGithubClientSecret() { return githubClientSecret; }

    /**
     * Set the GitHub OAuth app client secret.
     * @param githubClientSecret
     */
    public void setGithubClientSecret(String githubClientSecret) { this.githubClientSecret = githubClientSecret; }

    /**
     * Get the workflow component directory.
     * @return String wfcDir
     */
    public String getWfcDir() { return wfcDir; }

    /**
     * Set the workflow component directory.
     * @param wfcDir
     */
    public void setWfcDir(String wfcDir) { this.wfcDir = wfcDir; }


    /**
     * Get the workflow component directory.
     * @return String wfcRemote
     */
    public String getWfcRemote() { return wfcRemote; }

    /**
     * Set the workflow component directory.
     * @param wfcRemote
     */
    public void setWfcRemote(String wfcRemote) { this.wfcRemote = wfcRemote; }

    /**
     * Get the component heap size.
     * @return Long wfcHeapSize
     */
    public Long getWfcHeapSize() { return wfcHeapSize; }

    /**
     * Set the component heap size.
     * @param wfcHeapSize
     */
    public void setWfcHeapSize(Long wfcHeapSize) { this.wfcHeapSize = wfcHeapSize; }
}
