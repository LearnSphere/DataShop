/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Abstract class for the various XML parsers for each version of the
 * tutor message DTD.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12083 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-03-13 10:55:45 -0400 (Fri, 13 Mar 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class AbstractXmlParser implements XmlParser {

    /** The actual XML document. */
    private Document xmlDocument;

    /** The user id, optional. */
    private String userId;

    /** The session id, optional. */
    private String sessionId;

    /** The time string, optional. */
    private String timeString;

    /** The time zone, optional. */
    private String timeZone;

    /** The 'anonymizeUserId' flag, optional. */
    protected Boolean anonymizeUserId = null;

    /**
     * Constructor.
     * @param xmlDoc the XML data
     */
    protected AbstractXmlParser(Document xmlDoc) {
        this.xmlDocument = xmlDoc;
    }

    /**
     * Returns the XML data.
     * @return the XML data
     */
    protected Document getXmlDocument() {
        return this.xmlDocument;
    }

    /**
     * Sets the user id.
     * @param userId the user id
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the session id.
     * @param sessionId the session id
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Sets the time string.
     * @param timeString the time as a string
     */
    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    /**
     * Sets the time zone.
     * @param timeZone the time zone
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Sets the flag indicating whether or not users must be anonymized by DataShop.
     * @param anonymizeUserId the flag
     */
    public void setAnonymizeUserId(Boolean anonymizeUserId) {
        this.anonymizeUserId = anonymizeUserId;
    }

    /**
     * Check if a meta element exists.
     * @param element the message element
     * @return true if a meta element is a child of the given element
     */
    protected boolean hasMetaElement(Element element) {
        if (element.getChild("meta") != null) {
            return true;
        }
        return false;
    }

    /**
     * Return the user id in the meta element.
     * @param element the message element
     * @return the user id if it exists, null otherwise
     */
    protected String getUserId(Element element) {
        String retId = userId;
        if (hasMetaElement(element)) {
            Element metaElement = element.getChild("meta");
            String newId = MessageCommons.replaceInvalidChars(metaElement.getChildTextTrim("user_id"));
            if (newId != null) {
               retId = newId;
            }
        }
        return retId;
    }

    /**
     * Return the session id in the meta element.
     * @param element the message element
     * @return the session id if it exists, null otherwise
     */
    protected String getSessionId(Element element) {
        String retId = sessionId;
        if (hasMetaElement(element)) {
            Element metaElement = element.getChild("meta");
            String newId = MessageCommons.replaceInvalidChars(
                metaElement.getChildTextTrim("session_id"));
            if (newId != null) {
                retId = newId;
            }
        }
        return retId;
    }

    /**
     * Return the time string in the meta element.
     * @param element the message element
     * @return the time string if it exists, null otherwise
     */
    protected String getTimeString(Element element) {
        String retValue = timeString;
        if (hasMetaElement(element)) {
            Element metaElement = element.getChild("meta");
            String newValue = MessageCommons.replaceInvalidChars(
                 metaElement.getChildTextTrim("time"));
            if (newValue != null) {
                retValue = newValue;
            }
        }
        return retValue;
    }

    /**
     * Return the time zone in the meta element.
     * @param element the message element
     * @return the time zone if it exists, null otherwise
     */
    protected String getTimeZone(Element element) {
        String retValue = timeZone;
        if (hasMetaElement(element)) {
            Element metaElement = element.getChild("meta");
            String newValue = MessageCommons.replaceInvalidChars(
                metaElement.getChildTextTrim("time_zone"));
            if (newValue != null) {
                retValue = newValue;
            }
        }
        return retValue;
    }

    /**
     * Check if a meta element exists.
     * @param element the message element
     * @return true if a meta element is a child of the given element
     */
    protected boolean hasSemanticEventElement(Element element) {
        if (element.getChild("semantic_event") != null) {
            return true;
        }
        return false;
    }

    /**
     * Get a list of message items from the XML data.
     * @return a list of MessageItem objects
     */
    public abstract List getMessageItems();

}
