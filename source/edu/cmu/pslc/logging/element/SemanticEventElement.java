/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import edu.cmu.pslc.logging.util.LogFormatUtils;

public class SemanticEventElement implements EventElement {

    private String transactionId = null; //required
    private String name = null;
    private String trigger = null;
    private String subtype = null;

    public SemanticEventElement(String transactionId, String name, String trigger, String subtype) {
        this.transactionId = transactionId;
        this.name = name;
        this.trigger = trigger;
        this.subtype = subtype;
    }

    public SemanticEventElement(String transactionId, String name) {
        this.transactionId = transactionId;
        this.name = name;
    }

    public String getId() {
        return transactionId;
    }
    public void setId(String id) {
        this.transactionId = id;
    }
    public String getTransactionId() {
        return transactionId;
    }
    public String getName() {
        return name;
    }
    public String getTrigger() {
        return trigger;
    }
    public String getSubtype() {
        return subtype;
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<semantic_event");

        if (transactionId != null) {
            buffer.append(" transaction_id=\"");
            buffer.append(LogFormatUtils.escapeAttribute(transactionId));
            buffer.append("\"");
        }

        if (name != null) {
            buffer.append(" name=\"");
            buffer.append(LogFormatUtils.escapeAttribute(name));
            buffer.append("\"");
        }

        if (trigger != null) {
            buffer.append(" trigger=\"");
            buffer.append(LogFormatUtils.escapeAttribute(trigger));
            buffer.append("\"");
        }

        if (subtype != null) {
            buffer.append(" subtype=\"");
            buffer.append(LogFormatUtils.escapeAttribute(subtype));
            buffer.append("\"");
        }

        buffer.append("/>\n");

        return buffer.toString();
    }
}
