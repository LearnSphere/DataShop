/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * Utility methods for getting the current URL or displayable host name.
 * @author Alida Skogsholm
 * @version $Revision: 15657 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-10-28 20:36:33 -0400 (Sun, 28 Oct 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class ServerNameUtils {
    /** Private constructor as this is a utility class. */
    private ServerNameUtils() { }

    /** Debug logger. */
    private static Logger logger = Logger.getLogger("ServerNameUtils");

    /** Constant for the name of the QA server. */
    private static final String QA_SERVER = "everglades";
    /** Constant for the URL of the QA server. */
    private static final String QA_URL = "https://pslc-qa.andrew.cmu.edu/datashop";
    /** Constant for the name of the production server. */
    private static final String PROD_SERVER = "badlands";
    /** Constant for the name of the production server. */
    private static final String PROD_SERVER2 = "saguaro";
    /** Constant for the URL of the production server. */
    private static final String PROD_URL = "https://pslcdatashop.web.cmu.edu";

    /**
     * A platform independent method for getting the machine's hostname.
     * @return the hostname
     */
    public static String getHostName() {
        String hostname = "localhost:8080";

        try {
            // New servers are using eno1, while the VM uses eth0 for the NIC
            NetworkInterface nic = NetworkInterface.getByName("eno1");
            if (nic == null) {
                // QA still uses em1... try that.
                nic = NetworkInterface.getByName("em1");
                if (nic == null) {
                    nic = NetworkInterface.getByName("eth0");
                }
            }
            // If one of the NIC names was found, get the address and hostname
            if (nic != null) {

                Enumeration<InetAddress> iplist = nic.getInetAddresses();
                InetAddress addr = null;

                while (iplist.hasMoreElements()) {
                    InetAddress ad = iplist.nextElement();
                    byte bs[] = ad.getAddress();
                    if (bs.length == 4 && bs[0] != 127) {
                        addr = ad;
                        break;
                    }
                }

                if (addr != null) {
                    hostname = addr.getCanonicalHostName();
                }

            // Otherwise, use the development option: localhost:8080
            }
        } catch(Exception e) {
            logger.warn("No NIC found. Defaulting to localhost.");
        }

        return hostname;
    }


    /**
     * Get the local host to figure out the URL for datashop on the current host machine.
     * @return the base URL for the current host
     */
    public static String getDataShopUrl() {
        String datashopUrl;
        String hostName = DataShopInstance.getDatashopUrl();
        if (hostName.toLowerCase().contains(PROD_SERVER)
         || hostName.toLowerCase().contains(PROD_SERVER2)) {
            datashopUrl = PROD_URL;
        } else if (hostName.toLowerCase().contains(QA_SERVER)) {
            datashopUrl = QA_URL;
        } else {
            datashopUrl = hostName;
        }
        StringBuffer result = new StringBuffer(datashopUrl);

        // If necessary, prepend protocol.
        if (!datashopUrl.startsWith("http")) {
            result.insert(0, "https://");
        }

        return result.toString();
    }

    /**
     * Get the host name of the local machine so its easier/quicker to understand
     * where this email is coming from, then prepend it to the subject.
     * If its the production server, then don't show any host name.
     * @return a host name to display in email if necessary
     */
    public static String getHostNameForEmail() {
        String hostNameForEmail;
        String hostName = getHostName();
        if (hostName.toLowerCase().contains(PROD_SERVER)
         || hostName.toLowerCase().contains(PROD_SERVER2)) {
            hostNameForEmail = "";
        } else if (hostName.toLowerCase().contains(QA_SERVER)) {
            hostNameForEmail = "QA";
        } else {
            hostNameForEmail = hostName;
        }
        return hostNameForEmail;
    }

    /**
     * Get footer of the email message for use on all servers except production.
     * Include the host name, the from email address and the date.
     * @param fromEmailAddress the source email address
     * @return a string with this information, or null if production server
     */
    public static String getFooterForEmail(String fromEmailAddress) {
        String footerForEmail = null;
        String hostNameForEmail = ServerNameUtils.getHostNameForEmail();
        if (!hostNameForEmail.equals("")) {
            footerForEmail  = "<br>Host: " + hostNameForEmail;
            footerForEmail += "<br>From: " + fromEmailAddress;
            footerForEmail += "<br>Date: " + new Date();
        }
        return footerForEmail;
    }

    /**
     * Return true iff DataShop is running on the QA server.
     * @return flag indicating QA
     */
    public static boolean isQA() {
        if (getDataShopUrl().equals(QA_URL)) { return true; }
        return false;
    }
}
