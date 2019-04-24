/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.filesinfo;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.pslc.datashop.item.FileItem.SortParameter;

/**
 * Stores session information for the files info reports.
 *
 * @author Cindy Tipper
 * @version $Revision: 14841 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-02-17 11:09:46 -0500 (Sat, 17 Feb 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FilesInfoContext implements Serializable {

    /** Session Variable for storing what the current selected content is, i.e., subtab. */
    public static final String CURRENT_SUBTAB_KEY = "content_type";
    /** Session variable for storing the current sortBy parameter. */
    public static final String SORT_BY_KEY = "sort_by";

    /** Session Variable For Success/Error messages */
    private static final String STATUS_MESSAGE_KEY = "files_info_status_message";
    /** Session Variable For Success/Error messages */
    private static final String STATUS_MESSAGE_TYPE_KEY = "files_info_status_message_type";

    /**
     * HashMap to store servlet session context keyed on the subtab name. Thread safe.
     */
    private Map<String, Object> map;
    /**
     * HashMap to store Files subtab sort parameters. Thread safe.
     */
    private Map<String, Object> filesSubtabMap;
    /**
     * HashMap to store Papers subtab sort parameters. Thread safe.
     */
    private Map<String, Object> papersSubtabMap;
    /**
     * HashMap to store ExternalAnalyses subtab sort parameters. Thread safe.
     */
    private Map<String, Object> eaSubtabMap;

    /** Default Constructor. */
    public FilesInfoContext() {
        map = Collections.synchronizedMap(new HashMap <String, Object> ());
        filesSubtabMap = Collections.synchronizedMap(new HashMap<String, Object>());
        papersSubtabMap = Collections.synchronizedMap(new HashMap<String, Object>());
        eaSubtabMap = Collections.synchronizedMap(new HashMap<String, Object>());

        map.put(FilesInfoServlet.FILES_CONTENT_FILES_VALUE, filesSubtabMap);
        map.put(FilesInfoServlet.FILES_CONTENT_PAPERS_VALUE, papersSubtabMap);
        map.put(FilesInfoServlet.FILES_CONTENT_EXT_ANALYSES_VALUE, eaSubtabMap);
    }

    /**
     * Sets the current tab.
     * @param subtab the content as a String.
     */
    public void setCurrentTab(String subtab) {
        map.put(CURRENT_SUBTAB_KEY, subtab);
    }

    /**
     * Gets the current content type.
     * @return the current content type as a String.
     */
    public String getCurrentTab() {
        String result = (String)map.get(CURRENT_SUBTAB_KEY);
        if (result == null) {
            return FilesInfoServlet.FILES_CONTENT_FILES_VALUE;
        }
        return result;
    }

    /**
     * Sets the current sortBy parameters.
     * @param sortBy the parameters.
     */
    public void setSortByParameters(SortParameter[] sortBy) {
        String subtab = getCurrentTab();
        Map<String, Object> subtabMap = (Map<String, Object>)map.get(subtab);
        subtabMap.put(SORT_BY_KEY, sortBy);
    }

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public SortParameter[] getSortByParameters() {
        String subtab = getCurrentTab();
        Map<String, Object> subtabMap = (Map<String, Object>)map.get(subtab);
        return (SortParameter[])subtabMap.get(SORT_BY_KEY);
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        String subtab = getCurrentTab();
        Map<String, Object> subtabMap = (Map<String, Object>)map.get(subtab);
        Boolean ascFlag = true;
        if (subtabMap.containsKey(columnName)) {
            ascFlag = (Boolean) subtabMap.get(columnName);
            subtabMap.put(columnName, !ascFlag);
        } else {
            subtabMap.put(columnName, ascFlag);
        }
    }

    /** Get the sort order for a specific column.
     * @param columnName the column header
     * @return true for descending order and false for ascending
     */
    public Boolean getSortOrder(String columnName) {
        String subtab = getCurrentTab();
        Map<String, Object> subtabMap = (Map<String, Object>)map.get(subtab);
        Boolean result = false;
        if (subtabMap.containsKey(columnName)) {
            result = (Boolean) subtabMap.get(columnName);
        } else {
            subtabMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Returns the file message text.
     * @return the file message.
     */
    public String getFileMessage() {
        return (String)map.get(STATUS_MESSAGE_KEY);
    }

    /**
     * Sets the file message text.
     * @param fileMessage the text to set
     */
    public void setFileMessage(String fileMessage) {
        map.put(STATUS_MESSAGE_KEY, fileMessage);
    }

    /**
     * Returns the file message type.
     * @return the file message.
     */
    public String getFileMessageType() {
        return (String)map.get(STATUS_MESSAGE_TYPE_KEY);
    }

    /**
     * Sets the file message type.
     * @param fileMessageType the type to set
     */
    public void setFileMessageType(String fileMessageType) {
        map.put(STATUS_MESSAGE_TYPE_KEY, fileMessageType);
    }

}
