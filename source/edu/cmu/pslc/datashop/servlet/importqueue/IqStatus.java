/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.Serializable;
import java.util.Date;

import edu.cmu.pslc.datashop.item.ImportQueueItem;

/**
 * Hold the Import Queue Item status fields, which are editable on the Edit Status Dialog,
 * to enable Undo throughout a session.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class IqStatus implements Serializable {
    /** Class attribute. */
    private String status;
    /** Class attribute. */
    private Integer queueOrder;
    /** Class attribute. */
    private Date estImportDate;
    /** Class attribute. */
    private Integer issues;
    /** Class attribute. */
    private Integer errors;

    /**
     * Constructor.
     * @param item the import queue item
     */
    public IqStatus(ImportQueueItem item) {
        status = item.getStatus();
        queueOrder = item.getQueueOrder();
        estImportDate = item.getEstImportDate();
        issues = item.getNumIssues();
        errors = item.getNumErrors();
    }

    /**
     * Gets status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets queueOrder.
     * @return the queueOrder
     */
    public Integer getQueueOrder() {
        return queueOrder;
    }

    /**
     * Sets the queueOrder.
     * @param queueOrder the queueOrder to set
     */
    public void setQueueOrder(Integer queueOrder) {
        this.queueOrder = queueOrder;
    }

    /**
     * Gets estImportDate.
     * @return the estImportDate
     */
    public Date getEstImportDate() {
        return estImportDate;
    }

    /**
     * Sets the estImportDate.
     * @param estImportDate the estImportDate to set
     */
    public void setEstImportDate(Date estImportDate) {
        this.estImportDate = estImportDate;
    }

    /**
     * Gets issues.
     * @return the issues
     */
    public Integer getIssues() {
        return issues;
    }

    /**
     * Sets the issues.
     * @param issues the issues to set
     */
    public void setIssues(Integer issues) {
        this.issues = issues;
    }

    /**
     * Gets errors.
     * @return the errors
     */
    public Integer getErrors() {
        return errors;
    }

    /**
     * Sets the errors.
     * @param errors the errors to set
     */
    public void setErrors(Integer errors) {
        this.errors = errors;
    }
}
