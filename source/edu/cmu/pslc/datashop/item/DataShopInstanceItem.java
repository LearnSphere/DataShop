/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a DataShop Instance.
 *
 * @author Cindy Tipper
 * @version $Revision: 15738 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-12-04 16:35:34 -0500 (Tue, 04 Dec 2018) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DataShopInstanceItem extends Item
    implements java.io.Serializable, Comparable<DataShopInstanceItem>
{
    /** Debug logging. */
    private static Logger logger = Logger.getLogger(DataShopInstanceItem.class.getName());

    /** Primary key. */
    private Long datashopInstanceId;
    /** User that configured the instance. */
    private String configuredBy;
    /** Time this item was configured. */
    private Date configuredTime;
    /* URL. DataShop URL given to master to locate slave... Shibboleth too. */
    private String datashopUrl;

    /* Slave Instance Properties */
    /** Flag indicating if this instance is a slave. */
    private Boolean isSlave;
    /** The identifier for the instance; defaults to hostname if not set. */
    private String slaveId;
    /** The user for authenticating remote requests to the master. */
    private UserItem masterUser;
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
    private Integer datashopSmtpPort;
    /** Use SSL SMTP for this instance. */
    private Boolean useSslSmtp;
    /** SMTP user for this instance. */
    private String datashopSmtpUser;
    /** SMTP password for this instance. */
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

    /**
     * Reference to the remote instance. The ID only makes sense to
     * the master instance so the item isn't needed, just the ID.
     * Value is assigned by the master on slave instances, the value is NULL
     * on the master DataShop instance.
     */
    private Long remoteInstanceId;

    /** The name of the master user. Not part of persistent state. */
    private String masterUserName;

    /* Default values */
    /** Default is_slave value. */
    private static final Boolean DEFAULT_IS_SLAVE = Boolean.FALSE;
    /** Default master_user value. */
    private static final String DEFAULT_MASTER_USER_NAME = "webservice_request";
    /** Default master_schema value. */
    private static final String DEFAULT_MASTER_SCHEMA = "";
    /** Default master_url value. */
    private static final String DEFAULT_MASTER_URL = "";
    /** Default slave_api_token value. */
    private static final String DEFAULT_SLAVE_API_TOKEN = "";
    /** Default slave_secret value. */
    private static final String DEFAULT_SLAVE_SECRET = "";
    /** Default Datashop URL. */
    private static final String DEFAULT_DATASHOP_URL = "localhost:8080";
    /** Default is_sendmail_active value. */
    private static final Boolean DEFAULT_IS_SENDMAIL_ACTIVE = Boolean.FALSE;

    /** Default constructor. */
    public DataShopInstanceItem() {
        this.setConfiguredTime(new Date());
        this.setDatashopUrl(DEFAULT_DATASHOP_URL);
        this.setIsSlave(DEFAULT_IS_SLAVE);
        this.setMasterUserName(DEFAULT_MASTER_USER_NAME);
        this.setMasterSchema(DEFAULT_MASTER_SCHEMA);
        this.setMasterUrl(DEFAULT_MASTER_URL);
        this.setSlaveApiToken(DEFAULT_SLAVE_API_TOKEN);
        this.setSlaveSecret(DEFAULT_SLAVE_SECRET);
        this.setIsSendmailActive(DEFAULT_IS_SENDMAIL_ACTIVE);
    }

    /**
     * Returns the id.
     * @return String the id, the database primary key
     */
    public Comparable getId() { return this.datashopInstanceId; }

    /**
     * Set userId.
     * @param datashopInstanceId the id, the primary key
     */
    public void setId(Long datashopInstanceId) { this.datashopInstanceId = datashopInstanceId; }

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
     * @return Date the time
     */
    public Date getConfiguredTime() { return this.configuredTime; }

    /**
     * Set the time this instance was configured.
     * @param configuredTime the Date
     */
    public void setConfiguredTime(Date configuredTime) { this.configuredTime = configuredTime; }

    /**
     * Get the Datashop URL.
     * @return String the url
     */
    public String getDatashopUrl() { return this.datashopUrl; }

    /**
     * Set the Datashop URL.
     * @param url the URL
     */
    public void setDatashopUrl(String url) { this.datashopUrl = url; }

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
     * Get the remote user for authenticating requests to the master.
     * @return String masterUser
     */
    public UserItem getMasterUser() { return this.masterUser; }

    /**
     * Set the remote user for authenticating requests to the master.
     * @param masterUser the user ID
     */
    public void setMasterUser(UserItem masterUser) { this.masterUser = masterUser; }


    /**
     * Get the name of the remote user for authenticating requests to the master.
     * @return String masterUserName
     */
    public String getMasterUserName() { return this.masterUserName; }

    /**
     * Set the name of the remote user for authenticating requests to the master.
     * @param masterUserName the user ID
     */
    public void setMasterUserName(String masterUserName) { this.masterUserName = masterUserName; }

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
    public String getDatashopSmtpHost() { return this.datashopSmtpHost; }

    /**
     * Set the SMTP host for this DataShop Instance.
     * @param datashopSmtpHost the host
     */
    public void setDatashopSmtpHost(String datashopSmtpHost) {
        this.datashopSmtpHost = datashopSmtpHost;
    }

    /**
     * Get the SMTP port for this Datashop Instance.
     * @return Integer datashopSmtpPort
     */
    public Integer getDatashopSmtpPort() { return this.datashopSmtpPort; }

    /**
     * Set the SMTP port for this DataShop Instance.
     * @param datashopSmtpPort the port
     */
    public void setDatashopSmtpPort(Integer datashopSmtpPort) {
        this.datashopSmtpPort = datashopSmtpPort;
    }

    /**
     * Get flag indicating if SSL SMTP is to be used.
     * @return Boolean use SSL SMTP
     */
    public Boolean getUseSslSmtp() { return this.useSslSmtp; }

    /**
     * Set flag indicating if SSL SMTP is to be used.
     * @param useSslSmtp the flag
     */
    public void setUseSslSmtp(Boolean useSslSmtp) { this.useSslSmtp = useSslSmtp; }

    /**
     * Get the SMTP user for this Datashop Instance.
     * @return String datashopSmtpUser
     */
    public String getDatashopSmtpUser() { return this.datashopSmtpUser; }

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
    public String getDatashopSmtpPassword() { return this.datashopSmtpPassword; }

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
     * Get the remote workflow slave instance.
     * @return String wfcRemote
     */
    public String getWfcRemote() { return wfcRemote; }

    /**
     * Set the workflow component directory.
     * @param wfcRemote
     */
    public void setWfcRemote(String wfcRemote) { this.wfcRemote = wfcRemote; }

    /**
     * Get the remote component heap size.
     * @return Long wfcHeapSize
     */
    public Long getWfcHeapSize() { return wfcHeapSize; }

    /**
     * Set the component heap size.
     * @param wfcHeapSize
     */
    public void setWfcHeapSize(Long wfcHeapSize) { this.wfcHeapSize = wfcHeapSize; }

    /**
     * Get the RemoteInstance id.
     * @return Long remoteInstanceId
     */
    public Long getRemoteInstanceId() { return this.remoteInstanceId; }

    /**
     * Set the RemoteInstance id.
     * @param remoteInstanceId the id
     */
    public void setRemoteInstanceId(Long remoteInstanceId) {
        this.remoteInstanceId = remoteInstanceId;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        return toString("DatashopInstanceId", getId(),
                        "ConfiguredBy", getConfiguredBy(), "ConfiguredTime", getConfiguredTime(),
                        "DatashopURL", getDatashopUrl(),
                        "isSlave", getIsSlave(), "MasterUser", getMasterUser(),
                        "MasterSchema", getMasterSchema(), "MasterUrl", getMasterUrl(),
                        "SlaveId", getSlaveId(),
                        "SlaveApiToken", getSlaveApiToken(), "SlaveSecret", getSlaveSecret(),
                        "isSendmailActive", getIsSendmailActive(),
                        "DatashopHelpEmail", getDatashopHelpEmail(),
                        "DatashopRmEmail", getDatashopRmEmail(),
                        "DatashopBucketEmail", getDatashopBucketEmail(),
                        "DatashopSmtpHost", getDatashopSmtpHost(),
                        "DatashopSmtpPort", getDatashopSmtpPort(),
                        "UseSslSmtp", getUseSslSmtp(),
                        "DatashopSmtpUser", getDatashopSmtpUser(),
                        "DatashopSmtpPassword", getDatashopSmtpPassword(),
                        "WfcDir", getWfcDir(),
                        "WfcRemote", getWfcRemote(),
                        "WfcHeapSize", getWfcHeapSize(),
                        "RemoteInstanceId", getRemoteInstanceId());
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof DataShopInstanceItem) {
            DataShopInstanceItem otherItem = (DataShopInstanceItem)obj;

            if (!objectEquals(this.getConfiguredBy(), otherItem.getConfiguredBy())) {
                return false;
            }
            if (!objectEquals(this.getConfiguredTime(), otherItem.getConfiguredTime())) {
                return false;
            }
            if (!objectEquals(this.getDatashopUrl(), otherItem.getDatashopUrl())) {
                return false;
            }
            if (!objectEquals(this.getIsSlave(), otherItem.getIsSlave())) {
                return false;
            }
            if (!objectEqualsFK(this.getMasterUser(), otherItem.getMasterUser())) {
                return false;
            }
            if (!objectEquals(this.getMasterSchema(), otherItem.getMasterSchema())) {
                return false;
            }
            if (!objectEquals(this.getMasterUrl(), otherItem.getMasterUrl())) {
                return false;
            }
            if (!objectEquals(this.getSlaveId(), otherItem.getSlaveId())) {
                return false;
            }
            if (!objectEquals(this.getSlaveApiToken(), otherItem.getSlaveApiToken())) {
                return false;
            }
            if (!objectEquals(this.getSlaveSecret(), otherItem.getSlaveSecret())) {
                return false;
            }
            if (!objectEquals(this.getIsSendmailActive(), otherItem.getIsSendmailActive())) {
                return false;
            }
            if (!objectEquals(this.getDatashopHelpEmail(), otherItem.getDatashopHelpEmail())) {
                return false;
            }
            if (!objectEquals(this.getDatashopRmEmail(), otherItem.getDatashopRmEmail())) {
                return false;
            }
            if (!objectEquals(this.getDatashopBucketEmail(), otherItem.getDatashopBucketEmail())) {
                return false;
            }
            if (!objectEquals(this.getDatashopSmtpHost(), otherItem.getDatashopSmtpHost())) {
                return false;
            }
            if (!objectEquals(this.getDatashopSmtpPort(), otherItem.getDatashopSmtpPort())) {
                return false;
            }
            if (!objectEquals(this.getUseSslSmtp(), otherItem.getUseSslSmtp())) {
                return false;
            }
            if (!objectEquals(this.getDatashopSmtpUser(), otherItem.getDatashopSmtpUser())) {
                return false;
            }
            if (!objectEquals(this.getDatashopSmtpPassword(), otherItem.getDatashopSmtpPassword())) {
                return false;
            }
            if (!objectEquals(this.getWfcDir(), otherItem.getWfcDir())) {
                return false;
            }
            if (!objectEquals(this.getWfcRemote(), otherItem.getWfcRemote())) {
                return false;
            }
            if (!objectEquals(this.getWfcHeapSize(), otherItem.getWfcHeapSize())) {
                return false;
            }
            if (!objectEquals(this.getRemoteInstanceId(), otherItem.getRemoteInstanceId())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(configuredBy);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(configuredTime);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopUrl);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(isSlave);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(masterUser);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(masterSchema);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(masterUrl);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(slaveId);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(slaveApiToken);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(slaveSecret);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(isSendmailActive);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopHelpEmail);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopRmEmail);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopBucketEmail);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopSmtpHost);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopSmtpPort);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(useSslSmtp);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopSmtpUser);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(datashopSmtpPassword);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(wfcDir);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(wfcRemote);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(wfcHeapSize);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(remoteInstanceId);
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(DataShopInstanceItem obj) {
        DataShopInstanceItem otherItem = (DataShopInstanceItem)obj;

        int value = 0;

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConfiguredBy(), otherItem.getConfiguredBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getConfiguredTime(), otherItem.getConfiguredTime());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopUrl(), otherItem.getDatashopUrl());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getIsSlave(), otherItem.getIsSlave());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getMasterUser(), otherItem.getMasterUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMasterSchema(), otherItem.getMasterSchema());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getMasterUrl(), otherItem.getMasterUrl());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSlaveId(), otherItem.getSlaveId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSlaveApiToken(), otherItem.getSlaveApiToken());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getSlaveSecret(), otherItem.getSlaveSecret());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getIsSendmailActive(), otherItem.getIsSendmailActive());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopHelpEmail(), otherItem.getDatashopHelpEmail());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopRmEmail(), otherItem.getDatashopRmEmail());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopBucketEmail(), otherItem.getDatashopBucketEmail());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopSmtpHost(), otherItem.getDatashopSmtpHost());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopSmtpPort(), otherItem.getDatashopSmtpPort());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getUseSslSmtp(), otherItem.getUseSslSmtp());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopSmtpUser(), otherItem.getDatashopSmtpUser());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDatashopSmtpPassword(), otherItem.getDatashopSmtpPassword());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWfcDir(), otherItem.getWfcDir());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWfcRemote(), otherItem.getWfcRemote());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWfcHeapSize(), otherItem.getWfcHeapSize());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getRemoteInstanceId(), otherItem.getRemoteInstanceId());
        if (value != 0) { return value; }

        return value;
    }

}
