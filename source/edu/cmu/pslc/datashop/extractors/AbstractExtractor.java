/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors;

import edu.cmu.pslc.datashop.util.DataShopInstance;
import edu.cmu.pslc.datashop.util.MailUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Place for common functionality across the extractors.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12591 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-08-24 17:30:28 -0400 (Mon, 24 Aug 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AbstractExtractor implements ErrorEmailSender {
    /** If sendEmailFlag is set, use it, otherwise check DataShopInstance. */
    private Boolean sendEmailFlag = null;
    /** Send to default email address. */
    private String emailAddress = null;

    /**
     * Returns the sendEmailFlag.
     * @return the sendEmailFlag
     */
    public boolean isSendEmail() {
        if (sendEmailFlag != null) { return sendEmailFlag; }

        if (DataShopInstance.getIsSendmailActive() != null) {
            return DataShopInstance.getIsSendmailActive();
        } else {
            return false;
        }
    }

    /**
     * Sets the sendEmailFlag.
     * @param sendEmailFlag The sendEmailFlag to set.
     */
    public void setSendEmailFlag(boolean sendEmailFlag) {
        this.sendEmailFlag = sendEmailFlag;
    }

    /**
     * Returns the emailAddress.
     * @return the emailAddress
     */
    public String getEmailAddress() {
        if (emailAddress != null) { return emailAddress; }

        if (DataShopInstance.getDatashopHelpEmail() != null) {
            return DataShopInstance.getDatashopHelpEmail();
        } else {
            return null;
        }
    }

    /**
     * Sets the emailAddress.
     * @param emailAddress The emailAddress to set.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Send an email.
     * @param fromAddress the email address of sender
     * @param toAddress the email address of recipient
     * @param subject the message subject
     * @param msg description of what went wrong
     */
    protected void sendEmail(String fromAddress, String toAddress,
                             String subject, String msg) {
        if (isSendEmail()) {
            // Send email with null "Reply To" and BCC address
            MailUtils.sendEmail(fromAddress, toAddress, null, null, subject, msg);
        }
    }

    /**
     * Send an email, with a BCC list.
     * @param fromAddress the email address of sender
     * @param toAddress the email address of recipient
     * @param bccList list of email addresses to BCC
     * @param subject the message subject
     * @param msg description of what went wrong
     */
    protected void sendEmail(String fromAddress, String toAddress,
                             List<String> bccList, String subject, String msg) {
        if (isSendEmail()) {
            // Send email with null "Reply To" address
            MailUtils.sendEmail(fromAddress, toAddress, null, bccList, subject, msg);
        }
    }

    /**
     * Send an email saying what went wrong.
     * @param logger the debug logger
     * @param msg description of what went wrong
     * @param throwable the error
     */
    protected void sendErrorEmail(Logger logger, String msg, Throwable throwable) {
        logger.error(msg, throwable);
        if (isSendEmail()) {
            // Prepend name of extractor and host to message.
            String extractor = this.getClass().getSimpleName();

            // An error email from extractors is to & from specified emailAddress.
            MailUtils.sendErrorEmail(extractor, emailAddress, emailAddress, msg, throwable);
        } else {
            logger.info("Not sending email, sendEmailFlag is false.");
        }
    }

    /**
     * A recursive function to return a list of all the files in a top
     * level directory, including all the subdirectories.
     * This method will skip CVS directories.
     * @param logger the debug logger
     * @param theFile a file or directory
     * @return a complete list of the files in this directory
     */
    private static List getFiles(Logger logger, File theFile) {
        List fileList = new ArrayList();

        if (theFile.isFile()) {
            logger.info("Adding file " + theFile.getName());
            fileList.add(theFile);
        } else if (theFile.isDirectory()) {
            if (theFile.getName().equals("CVS") || theFile.getName().equals(".svn")) {
                logger.info("Skipping directory " + theFile.getName());
            } else {
                logger.info("Found directory " + theFile.getName());
                File[] files = theFile.listFiles();
                for (int idx = 0; idx < files.length; idx++) {
                    if (files[idx].isFile()) {
                        logger.info("Adding file " + files[idx].getName());
                        fileList.add(files[idx]);
                    } else if (files[idx].isDirectory()) {
                        List moreFiles = getFiles(logger, files[idx]);
                        fileList.addAll(moreFiles);
                    }
                } // end for loop
            }
        } // end else

        return fileList;
    }

    /**
     * Returns an array of files given a directory.
     * @param logger the debug logger
     * @param directoryName the directory path
     * @return an array of files
     */
    protected static List getFilenameList(Logger logger, String directoryName) {

        File topLevelDirectory = new File(directoryName);

        if (!topLevelDirectory.isDirectory()) {
            logger.warn("Not a directory: " + directoryName);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Directory found: " + directoryName);
        }

        logger.info("Top level directory is " + topLevelDirectory.getName());

        return getFiles(logger, topLevelDirectory);
    }

    /**
     * This method returns if the exitLevel is null; otherwise, it
     * exists with the given integer status level
     * @param exitLevel null if no exit is desired; any other integer
     * denotes a System.exit status level
     */
    protected static void exitOnStatus(Integer exitLevel) {
        if (exitLevel == null) {
            return;
        } else {
            System.exit(exitLevel);
        }
    }
}
