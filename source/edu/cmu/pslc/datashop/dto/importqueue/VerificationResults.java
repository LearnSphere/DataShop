/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto.importqueue;

import java.util.ArrayList;

/**
 * Contains all the verification results from a single FFI run.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class VerificationResults {
    /** Class attribute. */
    private String status;
    /** Class attribute. */
    private Integer totalErrors = null;
    /** Class attribute. */
    private Integer totalIssues = null;
    /** Class attribute. */
    private String successMessage = null;
    /** Class attribute. */
    private Messages generalMessages = new Messages();
    /** Class attribute. */
    private ArrayList<Messages> fileMessages =
            new ArrayList<Messages>();

    /**
     * Constructor.
     * @param status the status of the FFI run, passed, issues, errors
     */
    public VerificationResults(String status) {
        this.status = status;
    }

    /**
     * Gets status.
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets total errors.
     * @return total errors
     */
    public Integer getTotalErrors() {
        return totalErrors;
    }

    /**
     * Sets total errors.
     * @param total total errors
     */
    public void setTotalErrors(Integer total) {
        this.totalErrors = total;
    }

    /**
     * Gets total issues.
     * @return total issues
     */
    public Integer getTotalIssues() {
        return totalIssues;
    }

    /**
     * Sets total issues.
     * @param total total issues
     */
    public void setTotalIssues(Integer total) {
        this.totalIssues = total;
    }

    /**
     * Gets successMessage.
     * @return the successMessage
     */
    public String getSuccessMessage() {
        return successMessage;
    }

    /**
     * Sets the successMessage.
     * @param successMessage the successMessage to set
     */
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    /**
     * Gets the General Verification Messages.
     * @return general messages
     */
    public Messages getGeneralMessages() {
        return generalMessages;
    }

    /**
     * Sets the General Verification Messages.
     * @param msgs general messages
     */
    public void setGeneralMessages(Messages msgs) {
        this.generalMessages = msgs;
    }

    /**
     * Gets the list of file-specific verification messages.
     * @return list of file-specific messages
     */
    public ArrayList<Messages> getFileMessages() {
        return fileMessages;
    }

    /**
     * Adds file-specific verification messages.
     * @param msgs file-specific messages
     */
    public void addFileMessages(Messages msgs) {
        fileMessages.add(msgs);
    }

    // ----- GET RESULTS AS HTML -----

    /**
     * Return html of the results.
     * @return html of the results
     */
    public String generateHtml() {
        VerificationResults.Messages genMsgs = getGeneralMessages();
        ArrayList<VerificationResults.Messages> fileMsgsArrayList = getFileMessages();

        StringBuffer buffer = new StringBuffer();
        if (totalErrors != null && totalErrors > 0) {
            buffer.append("<p class=\"errorHeading\">" + totalErrors + " errors</p>");
            buffer.append("<div><div>");
            for (String msg : genMsgs.getErrors()) {
                buffer.append("<div class=\"uploadVerifyResults\">" + msg + "</div>");
            }
            buffer.append("</div></div>");
        }

        for (VerificationResults.Messages vm : fileMsgsArrayList) {
            ArrayList<String> errors = vm.getErrors();
            if (errors.size() > 0) {
                if (vm.getFileName() != null) {
                    buffer.append("<p>In file"  + vm.getFileName() + "</p>");
                }
                buffer.append("<div>");
                for (String msg : errors) {
                    buffer.append("<div class=\"uploadVerifyResults\">" + msg + "</div>");
                }
                buffer.append("</div>");
            }
        }

        if (totalIssues != null && totalIssues > 0) {
            buffer.append("<p class=\"issuesHeading\">" + totalIssues + " potential issues</p>");
            buffer.append("<div>");
            buffer.append("<div>");
            for (String msg : genMsgs.getIssues()) {
                buffer.append("<div class=\"uploadVerifyResults\">" + msg + "</div>");
            }
            buffer.append("</div>");
            buffer.append("</div>");
        }

        for (VerificationResults.Messages vm : fileMsgsArrayList) {
            ArrayList<String> issues = vm.getIssues();
            if (issues.size() > 0) {
                if (vm.getFileName() != null) {
                    buffer.append("<p>In file " + vm.getFileName() + "</p>");
                }
                buffer.append("<div>");
                for (String msg : issues) {
                    buffer.append("<div class=\"uploadVerifyResults\">" + msg + "</div>");
                }
                buffer.append("</div>");
            }
        }
        return buffer.toString();
    }

    // ----- INNER CLASSES -----

    /**
     * Used by the VerificationResults class to hold general messages,
     * in which case the fileName is null/ignored,
     * or multiple file messages.
     */
    public class Messages {
        /** Class attribute. */
        private String fileName = null;
        /** Class attribute. */
        private ArrayList<String> errors = new ArrayList<String>();
        /** Class attribute. */
        private ArrayList<String> issues = new ArrayList<String>();

        /** Constructor. */
        public Messages() { }

        /**
         * Constructor.
         * @param fileName the file with the messages
         */
        public Messages(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Gets fileName.
         * @return fileName
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Sets fileName.
         * @param fileName fileName
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Adds an error message to the errors list.
         * @param msg the error message
         */
        public void addError(String msg) {
            errors.add(msg);
        }

        /**
         * Adds an issue message to the issues list.
         * @param msg the issue message
         */
        public void addIssue(String msg) {
            issues.add(msg);
        }

        /**
         * Gets the errors lists.
         * @return a list of error messages as strings
         */
        public ArrayList<String> getErrors() {
            return this.errors;
        }

        /**
         * Gets the issues lists.
         * @return a list of issue messages as strings
         */
        public ArrayList<String> getIssues() {
            return this.issues;
        }
    }
}