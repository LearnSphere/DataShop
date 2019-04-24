/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import edu.cmu.pslc.datashop.item.MessageItem;

/**
 * A pair of message items, where the user id, session id,
 * context id and the transaction id are all the same.
 * Be sure to check for null on either item.
 *
 * @author Alida Skogsholm
 * @version $Revision: 2204 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-01-26 16:22:18 -0500 (Thu, 26 Jan 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ToolTutorMessageItemPair {
    /** The tool message item, can be null. */
    private MessageItem toolMessageItem;
    /** The tutor message item, can be null. */
    private MessageItem tutorMessageItem;

    /**
     * The constructor.
     * @param tool the tool message item
     * @param tutor the tutor message item
     */
    public ToolTutorMessageItemPair(MessageItem tool, MessageItem tutor) {
        this.toolMessageItem = tool;
        this.tutorMessageItem = tutor;
    }

    /**
     * Returns the tool message item.
     * @return the tool message item
     */
    public MessageItem getToolMessageItem() {
        return toolMessageItem;
    }

    /**
     * Returns the tutor message item.
     * @return the tutor message item
     */
    public MessageItem getTutorMessageItem() {
        return tutorMessageItem;
    }

    /**
     * Returns a string representation of both items.
     * @return a string
     */
    public String toString() {
        return toolMessageItem + ", " + tutorMessageItem;
    }
}
