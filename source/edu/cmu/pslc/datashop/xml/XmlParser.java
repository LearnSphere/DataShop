/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.List;

/**
 * Interface for the various XML parsers for each version of the
 * tutor message DTD.
 *
 * @author Alida Skogsholm
 * @version $Revision: 9054 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2013-04-02 14:22:55 -0400 (Tue, 02 Apr 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface XmlParser {
    /**
     * Get a list of message items from the XML data.
     * @return a list of MessageItem objects
     */
    List getMessageItems();

    /**
     * Sets the user id.
     * @param userId the user id
     */
    void setUserId(String userId);
    /**
     * Sets the session id.
     * @param sessionId the session id
     */
    void setSessionId(String sessionId);

    /**
     * Sets the time string.
     * @param timeString the time as a string
     */
    void setTimeString(String timeString);

    /**
     * Sets the time zone.
     * @param timeZone the time zone
     */
    void setTimeZone(String timeZone);

    /**
     * Sets the flag indicating whether or not users must be anonymized by DataShop.
     * @param anonymizeUserId the flag
     */
    void setAnonymizeUserId(Boolean anonymizeUserId);
}
