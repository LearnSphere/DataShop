package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This context is used to keep track of sorting on the Samples page.
 *
 * @author Mike Komisin
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleToDatasetContext implements Serializable {

    /** The current order to sort a list of Project Access Requests. */
    private static final String SAMPLE_TO_DATASET_SORT_BY = "s2d_sortby";
    /** Default sort by string. */
    public static final String DEFAULT_SORT_BY = SampleRowDto.COLUMN_SAMPLE_NAME;

    /** HashMap that holds the context and is thread safe. */
    private Map <String, Object> mainMap;

    /**
     * Default Constructor.
     */
    public SampleToDatasetContext() {
        mainMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        mainMap.put(SAMPLE_TO_DATASET_SORT_BY, DEFAULT_SORT_BY);
    }

    /** Returns sort type. @return the sort type as a String. */
    public String getSortBy() {
        String sortBy = null;
        if (mainMap.containsKey(SAMPLE_TO_DATASET_SORT_BY)) {
            sortBy = (String)mainMap.get(SAMPLE_TO_DATASET_SORT_BY);
        }

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = DEFAULT_SORT_BY;
            mainMap.put(DEFAULT_SORT_BY, true);

        }
        return sortBy;
    }

    /** Set sort type.@param sortBy the selected sort type as a String. */
    public void setSortBy(String sortBy) {
        mainMap.put(SAMPLE_TO_DATASET_SORT_BY, sortBy);
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        Boolean ascFlag = true;

        if (mainMap.containsKey(columnName)) {
            ascFlag = (Boolean) mainMap.get(columnName);
            mainMap.put(columnName, !ascFlag);
        } else {
            mainMap.put(columnName, ascFlag);
        }
    }

    /** Get the sort order for a specific column.
     * @param columnName the column header
     * @return true for ascending order and false for descending
     */
    public Boolean getSortOrder(String columnName) {
        Boolean ascFlag = true;

        if (mainMap.containsKey(columnName)) {
            ascFlag = (Boolean) mainMap.get(columnName);
        } else {
            mainMap.put(columnName, false);
        }
        return ascFlag;
    }

    /**
     * Sets the flag describing if the sort order is ascending.
     * @param columnName the columnName
     * @param sortOrder the flag describing if the sort order is ascending
     */
    public void setSortOrder(String columnName, Boolean sortOrder) {

        mainMap.put(columnName, sortOrder);
    }

}
