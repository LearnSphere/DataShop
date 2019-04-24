/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.importqueue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cmu.pslc.datashop.dto.importqueue.ImportQueueDto;

/**
 * Stores session information for the ImportQueue page.
 *
 * @author Cindy Tipper
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ImportQueueContext implements Serializable {

    //----- SINGLETON BY USER'S HTTP SESSION -----

    /** Session Key for the ImportQueueContext */
    private static final String IMPORT_QUEUE_SESS_KEY = "iq_context";

    /**
     * Get the ImportQueueContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @return the ImportQueueContext stored in the session
     */
    public static ImportQueueContext getContext(HttpServletRequest req) {
        ImportQueueContext context =
            (ImportQueueContext)req.getSession().getAttribute(IMPORT_QUEUE_SESS_KEY);
        if (context == null) {
            context = new ImportQueueContext();
            setContext(req, context);
        }
        return context;
    }

    /**
     * Set the ImportQueueContext stored in the session.
     * @param req {@link HttpServletRequest}
     * @param context the ImportQueueContext
     */
    public static void setContext(HttpServletRequest req, ImportQueueContext context) {
        req.getSession().setAttribute(IMPORT_QUEUE_SESS_KEY, context);
    }

    //----- SESSION VARIABLES -----

    /** Session variable for storing the current column to sort by. */
    private static final String KEY_SORT_BY_COLUMN = "sort_by_column";
    /** Status map key. */
    private static final String IQ_STATUS = "iq_status";

    /**
     *  HashMap to store context values for the ImportQueue 'Recently Loaded'
     *  table. Thread safe.
     */
    private Map<String, Object> loadedMap;

    /**
     *  HashMap to store context values for the ImportQueue 'Recently Created... No Data'
     *  table. Thread safe.
     */
    private Map<String, Object> noDataMap;

    /** HashMap to store status values for Undo.  Thread safe. */
    private Map <String, Object> statusMap;

    //----- CONSTRUCTOR -----

    /** Default Constructor. */
    public ImportQueueContext() {
        // Sorting information
        loadedMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        noDataMap = Collections.synchronizedMap(new HashMap <String, Object> ());

        setLoadedSortByColumn(ImportQueueDto.COLUMN_LAST_UPDATE, true);
        setNoDataSortByColumn(ImportQueueDto.COLUMN_LAST_UPDATE, true);

        // Initially, they use a DESC sort.
        toggleLoadedSortOrder(ImportQueueDto.COLUMN_LAST_UPDATE);
        toggleNoDataSortOrder(ImportQueueDto.COLUMN_LAST_UPDATE);

        // Status information for Undo
        statusMap = Collections.synchronizedMap(new HashMap <String, Object> ());
    }

    //----- ImportQueue Status -----

    /**
     * Gets an IqStatus object for the given import queue id.
     * @param importQueueId the given import queue id
     * @return and IqStatus item if found, null otherwise
     */
    public IqStatus getIqStatus(Integer importQueueId) {
        Map<Integer, IqStatus> iqMap = (Map<Integer, IqStatus>)statusMap.get(IQ_STATUS);
        if (iqMap == null) {
            iqMap = new HashMap <Integer, IqStatus> ();
            return null;
        }
        return iqMap.get(importQueueId);
    }

    /**
     * Puts an iqStatus into the map.
     * @param importQueueId the import queue id of the item
     * @param iqStatus the info to store
     */
    public void setIqStatus(Integer importQueueId, IqStatus iqStatus) {
        Map<Integer, IqStatus> iqMap = (Map<Integer, IqStatus>)statusMap.get(IQ_STATUS);
        if (iqMap == null) {
            iqMap = new HashMap <Integer, IqStatus> ();
        }
        iqMap.put(importQueueId, iqStatus);
        statusMap.put(IQ_STATUS, iqMap);
    }

    //----- ImportQueue 'Recently Loaded' MAP GETTERs and SETTERs -----

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getLoadedSortByColumn() {
        return (String)loadedMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setLoadedSortByColumn(String columnName, Boolean toggleFlag) {
        loadedMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleLoadedSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isLoadedAscending(String columnName) {
        Boolean result = true;
        if (loadedMap.containsKey(columnName)) {
            result = (Boolean) loadedMap.get(columnName);
        } else {
            loadedMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleLoadedSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (loadedMap.containsKey(columnName)) {
            ascFlag = (Boolean) loadedMap.get(columnName);
            loadedMap.put(columnName, !ascFlag);
        } else {
            loadedMap.put(columnName, ascFlag);
        }
    }

    //----- ImportQueue 'Recently Created... No Data' MAP GETTERs and SETTERs -----

    /**
     * Gets the current sortBy parameters.
     * @return the current sortBy parameters.
     */
    public String getNoDataSortByColumn() {
        return (String)noDataMap.get(KEY_SORT_BY_COLUMN);
    }

    /**
     * Sets up the sorting parameters according to user-selected column header.
     * @param columnName the column header to sort by.
     * @param toggleFlag flag indicating whether or not sort order toggled.
     */
    public void setNoDataSortByColumn(String columnName, Boolean toggleFlag) {
        noDataMap.put(KEY_SORT_BY_COLUMN, columnName);
        if (toggleFlag) {
            toggleNoDataSortOrder(columnName);
        }
    }

    /**
     * Get whether the specified column isAscending.
     * @param columnName the column header
     * @return true for ascending, false for descending
     */
    public Boolean isNoDataAscending(String columnName) {
        Boolean result = true;
        if (noDataMap.containsKey(columnName)) {
            result = (Boolean) noDataMap.get(columnName);
        } else {
            noDataMap.put(columnName, result);
        }
        return result;
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleNoDataSortOrder(String columnName) {
        Boolean ascFlag = true;
        if (noDataMap.containsKey(columnName)) {
            ascFlag = (Boolean) noDataMap.get(columnName);
            noDataMap.put(columnName, !ascFlag);
        } else {
            noDataMap.put(columnName, ascFlag);
        }
    }
}
