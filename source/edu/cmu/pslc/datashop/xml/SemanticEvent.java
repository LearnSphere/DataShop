/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Data object that contains all fields for the XML semantic_event element.
 *
 * @author Hui cheng
 * @version $Revision: 2992 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-06-21 12:00:13 -0400 (Wed, 21 Jun 2006) $
 * <!-- $KeyWordsOff: $ -->
 */

public class SemanticEvent {
    /** The transaction_id, previously id. This is not stored in database
     * but is used to pair tool and tutor messages.*/
    private String transactionId;
    /** The name. */
    private String name;
    /** The trigger. */
    private String trigger;
    /** The subtype. */
    private String subtype;

    /** Default constructor. */
    public SemanticEvent() {
    }

    /** The constructor that sets all fields.
     *  @param transactionId String.
     *  @param name String.
     *  @param trigger String.
     *  @param subtype String.
     */
    public SemanticEvent(String transactionId,
                            String name,
                            String trigger,
                            String subtype) {
        this.transactionId = transactionId;
        this.name = name;
        this.trigger = trigger;
        this.subtype = subtype;
    }

    /** The getter for transactionId.
     * @return The transactionId.
     */
    public String getTransactionId () {
        return transactionId;
    }

    /** The setter for transactionId.
     * @param transactionId String.
     */
    public void setTransactionId (String transactionId) {
        this.transactionId = transactionId;
    }

    /** The getter for name.
     * @return name String.
     */
    public String getName () {
        return name;
    }

    /** The setter for name.
     * @param name String.
     */
    public void setName (String name) {
        this.name = name;
    }

    /** The getter for trigger.
     * @return trigger String.
     */
    public String getTrigger () {
        return trigger;
    }

    /** The setter for trigger.
     * @param trigger String.
     */
    public void setTrigger (String trigger) {
        this.trigger = trigger;
    }

    /** The getter for subtype.
     * @return subtype String.
     */
    public String getSubtype () {
        return subtype;
    }

    /** The setter for subtype.
     * @param subtype String.
     */
    public void setSubtype (String subtype) {
        this.subtype = subtype;
    }

    /**
     * Returns a string representation of this item, in this case just
     * the transaction id.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append(" [");
        buffer.append("TransactionId: " + transactionId);
        buffer.append("]");

        return buffer.toString();
    }
}