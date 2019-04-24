/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.customfield;

import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.TransactionItem;

/**
 * DTO for the CfTxLevel item.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldValueDto {

    //----- ATTRIBUTES -----

    /** The value of this CustomField value. */
    private String value;
    /** The logging flag of this CustomField value. */
    private Boolean loggingFlag;
    /** The CustomField this CustomField value is associated with. */
    private CustomFieldItem customField;
    /** The Transaction this CustomField value is associated with. */
    private TransactionItem transaction;

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public CustomFieldValueDto() { }

    /**
     * Constructor.
     * @param value the value of this CustomField
     * @param loggingFlag the logging flag
     */
    public CustomFieldValueDto(String value, Boolean loggingFlag) {
        this.value = value;
        this.loggingFlag = loggingFlag;
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the value.
     * @return java.lang.String
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value.
     * @param value The value for this custom field
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get the logging flag.
     * @return Boolean the flag
     */
    public Boolean getLoggingFlag() {
        return loggingFlag;
    }

    /**
     * Set the logging flag.
     * @param flag The logging flag for this custom field value
     */
    public void setLoggingFlag(Boolean flag) {
        this.loggingFlag = flag;
    }

    /**
     * Get the CustomField.
     * @return CustomFieldItem
     */
    public CustomFieldItem getCustomField() {
        return customField;
    }

    /**
     * Set the CustomField.
     * @param customField the CustomField
     */
    public void setCustomField(CustomFieldItem customField) {
        this.customField = customField;
    }

    /**
     * Get the Transaction.
     * @return TransactionItem
     */
    public TransactionItem getTransaction() {
        return transaction;
    }

    /**
     * Set the Transaction.
     * @param transaction the transaction
     */
    public void setTransaction(TransactionItem transaction) {
        this.transaction = transaction;
    }
}
