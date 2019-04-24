/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.datasetinfo;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores session information for the dataset info reports.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4816 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-05-08 15:18:22 -0400 (Thu, 08 May 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetInfoContext implements Serializable {

    /** Session Variable For Success/Error messages */
    private static final String FILE_MESSAGE_KEY = "file_servlet_message";
    /** Session Variable For Success/Error messages */
    private static final String FILE_MESSAGE_TYPE_KEY = "file_servlet_message_type";
    /** Session Variable for storing what the current selected content is. */
    public static final String CURRENT_CONTENT_KEY = "content_type";

    /**
     * Servlet Session context stored in the map, this allows us to synchronize
     * the list to handle multiple thread requests.
     */
    private Map map;

    /** Default Constructor. */
    public DatasetInfoContext() {
        map = Collections.synchronizedMap(new HashMap <Integer, Map> ());
    }

    /**
     * Returns the file message text.
     * @return the file message.
     */
    public String getFileMessage() {
        return (String)map.get(FILE_MESSAGE_KEY);
    }

    /**
     * Sets the file message text.
     * @param fileMessage the text to set
     */
    public void setFileMessage(String fileMessage) {
        map.put(FILE_MESSAGE_KEY, fileMessage);
    }

    /**
     * Returns the file message type.
     * @return the file message.
     */
    public String getFileMessageType() {
        return (String)map.get(FILE_MESSAGE_TYPE_KEY);
    }

    /**
     * Sets the file message type.
     * @param fileMessageType the type to set
     */
    public void setFileMessageType(String fileMessageType) {
        map.put(FILE_MESSAGE_TYPE_KEY, fileMessageType);
    }

    /**
     * Sets the current content.
     * @param contentType the content as a String.
     */
    public void setContentType(String contentType) {
        map.put(CURRENT_CONTENT_KEY, contentType);
    }

    /**
     * Gets the current content type.
     * @return the current content type as a String.
     */
    public String getContentType() {
        return (String)map.get(CURRENT_CONTENT_KEY);
    }


}
