/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.cmu.pslc.logging.element.PropertyElement;
/**
 * The plain message is not a tutor or tool message.
 * @author Alida Skogsholm
 * @version $Revision: 9303 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-30 11:30:09 -0400 (Thu, 30 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class PlainMessage extends Message {
    /** The property list. */
    private List propertyList = new ArrayList();
    /**
     * Creates a new plain message.
     * @param contextMessage the context message
     * @return the plain message
     */
    public static PlainMessage create(ContextMessage contextMessage) {
        return new PlainMessage(contextMessage);
    }
    /**
     * Private constructor.
     * @param contextMessage the context message
     */
    private PlainMessage(ContextMessage contextMessage) {
        super(contextMessage.getContextMessageId(), contextMessage.getMetaElement());
    }
    /**
     * Add property given the name and entry.
     * @param name the name
     * @param entry the entry
     */
    public void addProperty(String name, String entry) {
        propertyList.add(new PropertyElement(name, entry));
    }
    /**
     * Add property given the property element.
     * @param propertyElement the property element
     */
    public void addProperty(PropertyElement propertyElement) {
        propertyList.add(propertyElement);
    }
    /**
     * To string method called and displays meta element.
     * @return the string to display
     */
    public String toString() {
        return toString(true);
    }
    /**
     * To string method.
     * @param logMetaFlag whether to display the meta element
     * @return the string to display
     */
    public String toString(boolean logMetaFlag) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<message context_message_id=\"" + getContextMessageId() + "\">\n");

        if (logMetaFlag) {
            buffer.append(getMetaElement());
        }

        for (Iterator iter = propertyList.iterator(); iter.hasNext();) {
            PropertyElement propertyElement = (PropertyElement)iter.next();
            if (propertyElement != null) {
                buffer.append(propertyElement);
            }
        }

        buffer.append("</message>\n");
        return buffer.toString();

    }

}
