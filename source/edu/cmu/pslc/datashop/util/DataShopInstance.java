/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DataShopInstanceDao;
import edu.cmu.pslc.datashop.dao.RemoteInstanceDao;
import edu.cmu.pslc.datashop.dao.UserDao;

import edu.cmu.pslc.datashop.item.DataShopInstanceItem;
import edu.cmu.pslc.datashop.item.RemoteInstanceItem;
import edu.cmu.pslc.datashop.item.UserItem;

import edu.cmu.pslc.datashop.webservices.DatashopClient;

/**
 * A utility class for master and slave DataShop instances.
 *
 * @author epennin1
 * @version $Revision: $
 * <BR>Last modified by: $Author: $
 * <BR>Last modified on: $Date: $
 * <!-- $KeyWordsOff: $ -->
 */
public final class DataShopInstance {
    /** Debug logging. */
    private static final Logger logger = Logger.getLogger(DataShopInstance.class);

    /** The DataShopInstance specific to this instance. */
    private static DataShopInstanceItem dsInstance;

    // Instance properties.

    /**
     * Get whether this is a slave DataShop instance.
     * @return whether this is a slave DataShop instance.
     */
    public static Boolean isSlave() {
        return dsInstance.getIsSlave();
    }

    /**
     * Get the user item used to authenticate requests from a slave instance.
     * @return the authentication user item
     */
    public static UserItem getMasterUser() {
        UserItem masterUser = dsInstance.getMasterUser();
        if (masterUser == null) {
            UserDao userDao = DaoFactory.DEFAULT.getUserDao();
            masterUser = userDao.get(dsInstance.getMasterUserName());
        }
        return masterUser;
    }

    /**
     * Get the message schema url used when sending responses to a slave instance.
     * @return the message schema
     */
    public static String getMasterSchema() {
        return dsInstance.getMasterSchema();
    }

    /**
     * The web services url used to send requests to a master instance.
     * @return the master url
     */
    public static String getMasterUrl() {
        return dsInstance.getMasterUrl();
    }

    /**
     * Get the api token used to send requests to a master instance.
     * @return the api token
     */
    public static String getSlaveApiToken() {
        return dsInstance.getSlaveApiToken();
    }

    /**
     * Get the secret used to send requests to a master instance.
     * @return the secret
     */
    public static String getSlaveSecret() {
        return dsInstance.getSlaveSecret();
    }

    /**
     * Get the slave unique identifier.
     * @return the id
     */
    public static String getSlaveId() {
        String slaveId = dsInstance.getSlaveId();
        if (slaveId == null) {
            // If not set in database, use hostname.
            if (slaveId == null) {
                try {
                    slaveId = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                    logger.info("Unable to determine host name.");
                    slaveId = null;
                }
            }
        }

        return slaveId;
    }

    /**
     * Get the slave id string to use in modifying name.
     * @return the id string
     */
    public static String getSlaveIdStr() {
        StringBuffer sb = new StringBuffer();

        if (getSlaveId() != null) {
            sb.append(" [");
            sb.append(getSlaveId());
            sb.append("]");
        }

        return sb.toString();
    }

    // Datashop URL.

    /**
     * Get the URL for this Datashop instance.
     * @return String the url
     */
    public static String getDatashopUrl() {
        return dsInstance.getDatashopUrl();
    }

    // Email properties.

    /**
     * Get the flag indicating if emails are being sent.
     * @return the flag
     */
    public static Boolean getIsSendmailActive() {
        return dsInstance.getIsSendmailActive();
    }

    /**
     * Get email address for DataShop Help.
     * @return String datashopHelpEmail
     */
    public static String getDatashopHelpEmail() {
        return dsInstance.getDatashopHelpEmail();
    }

    /**
     * Get encoded email address for DataShop Help.
     * @return String datashopHelpEmail
     */
    public static String getEncodedDatashopHelpEmail() {
        return shiftEncode(dsInstance.getDatashopHelpEmail());
    }

    /**
     * Get email address for DataShop Bucket.
     * @return String datashopBucketEmail
     */
    public static String getDatashopBucketEmail() {
        return dsInstance.getDatashopBucketEmail();
    }
    /**
     * Get email address for DataShop Research Manager
     * @return String datashopRmEmail
     */
    public static String getDatashopRmEmail() {
        return dsInstance.getDatashopRmEmail();
    }

    /**
     * Get SMTP host for this DataShopInstance.
     * @return String SMTP host
     */
    public static String getDatashopSmtpHost() {
        return dsInstance.getDatashopSmtpHost();
    }

    /**
     * Get SMTP port for this DataShopInstance.
     * @return Integer SMTP port
     */
    public static Integer getDatashopSmtpPort() {
        return dsInstance.getDatashopSmtpPort();
    }

    /**
     * Get useSslSmtp flag.
     * @return Boolean useSslSmtp flag
     */
    public static Boolean getUseSslSmtp() {
        return dsInstance.getUseSslSmtp();
    }

    /**
     * Get SMTP user for this DataShopInstance.
     * Only used if secure (SSL) SMTP specified.
     * @return String secure SMTP user
     */
    public static String getDatashopSmtpUser() {
        return dsInstance.getDatashopSmtpUser();
    }

    /**
     * Get SMTP password for this DataShopInstance.
     * Only used if secure (SSL) SMTP specified.
     * @return String secure SMTP password
     */
    public static String getDatashopSmtpPassword() {
        return dsInstance.getDatashopSmtpPassword();
    }

    // GitHub properties.

    /**
     * Get the GitHub client ID.
     * @return String githubClientId
     */
    public static String getGithubClientId() {
        return dsInstance.getGithubClientId();
    }

    /**
     * Get the GitHub client secret.
     * @return String githubClientSecret
     */
    public static String getGithubClientSecret() {
        return dsInstance.getGithubClientSecret();
    }

    // Workflow properties.

    /**
     * Get the workflow component dir.
     * @return String wfcDir
     */
    public static String getWfcDir() {
        return dsInstance.getWfcDir();
    }

    /**
     * Get the remote workflow slave instance.
     * @return String wfcDir
     */
    public static String getWfcRemote() {
        return dsInstance.getWfcRemote();
    }

    /**
     * Get the remote workflow slave instance.
     * @return String wfcDir
     */
    public static Long getWfcHeapSize() {
        return dsInstance.getWfcHeapSize();
    }

    /**
     * Get remoteInstanceId for this DataShopInstance.
     * @return Long the remoteInstance id
     */
    public static Long getRemoteInstanceId() {
        return dsInstance.getRemoteInstanceId();
    }

    /** One DataShopInstanceItem per server. */
    private static final Long DATASHOP_INSTANCE_ID = 1L;

    /**
     * Load the instance properties from the database.
     */
    public static void initialize() {
        DataShopInstanceDao dao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        dsInstance = dao.get(DATASHOP_INSTANCE_ID);

        // If the instance has not been created for the slave, use defaults.
        if (dsInstance == null) {
            dsInstance = new DataShopInstanceItem();
            dao.saveOrUpdate(dsInstance);
        }

        if (dsInstance.getMasterUser() != null) {
            dsInstance.setMasterUser(getMasterUser((String)dsInstance.getMasterUser().getId()));
        } else {
            dsInstance.setMasterUser(getMasterUser(dsInstance.getMasterUserName()));
        }

        if ((dsInstance.getRemoteInstanceId() == null) && (dsInstance.getIsSlave())) {
            // Create RemoteInstance
            dsInstance.setRemoteInstanceId(createRemoteInstance());
            dao.saveOrUpdate(dsInstance);
        }

        logger.debug("initialize: " + dsInstance);
    }

    /**
     * See if the instance is dirty and if so, refresh it.
     */
    public static void refreshIfDirty() {
        DataShopInstanceDao dao = DaoFactory.DEFAULT.getDataShopInstanceDao();
        DataShopInstanceItem dsi = dao.get(DATASHOP_INSTANCE_ID);

        if ((dsi == null) || (dsInstance.getConfiguredTime().before(dsi.getConfiguredTime()))) {
            initialize();
        }

        return;
    }

    /**
     * Get the user item used to authenticate requests from a slave instance.
     * @param masterUserName the name of the master user
     * @return the authentication user item
     */
    private static UserItem getMasterUser(String masterUserName) {
        UserDao userDao = DaoFactory.DEFAULT.getUserDao();
        return userDao.get(masterUserName);
    }

    /**
     * Helper method to create a RemoteInstance on the master.
     * @return the item
     */
    private static Long createRemoteInstance() {

        Long remoteInstanceId = null;

        DatashopClient client = DatashopClient.getDatashopClientForRemoteRequests();
        if (client != null) {
            try {
                String response = client.getInstanceAdd(getSlaveId(), getDatashopUrl());
                remoteInstanceId = parseXmlResponse(response);
            } catch (Exception e) {
                // Failed to create remote instance.
                logger.debug("Failed to create remote instance: ", e);
            }
        }

        return remoteInstanceId;
    }

    /**
     * Helper method to parse XML response from the InstanceAdd WebService call.
     * @param response the String
     * @return Long the instance id
     */
    private static Long parseXmlResponse(String response) {

        Long instanceId = null;

        SAXBuilder builder = new SAXBuilder();
        StringReader reader = null;
        Element rootElement = null;

        try {
            reader = new StringReader(response);
            rootElement = builder.build(reader).getRootElement();
        } catch (Exception e) {
            logger.debug("Failed to parse XML response.", e);
            return null;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (rootElement != null) {
            String instanceIdStr = rootElement.getAttributeValue("instance_id");
            if ((instanceIdStr != null) && (instanceIdStr.matches("\\d+"))) {
                instanceId = Long.valueOf(instanceIdStr);
            }
        }

        return instanceId;
    }

    // Offset for Caesar shift. Same as that used in Javascript: 13.
    private static final int OFFSET = 13;

    /**
     * Helper method to do a Caesar shift encoding of input. Used for email address.
     * @param toEncode
     * @return String encoded (shifted) input
     */
    private static String shiftEncode(String toEncode) {
        StringBuffer result = new StringBuffer();
        for (char c : toEncode.toCharArray()) {
            if (Character.isLetter(c)) {
                if (Character.isUpperCase(c)) {
                    result.append((char) ('A' + (c - 'A' + OFFSET) % 26));
                } else {
                    result.append((char) ('a' + (c - 'a' + OFFSET) % 26));
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
