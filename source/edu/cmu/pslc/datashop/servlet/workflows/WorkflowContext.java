package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * This context is used to keep track of sorting on the Workflows page.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2 $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowContext implements Serializable {

    /** The current order to sort a list of Global Workflows. */
    private static final String WORKFLOW_SORT_BY = "workflow_sortby";
    /** The current order to sort a list of My Workflows. */
    private static final String WORKFLOW_MY_SORT_BY = "workflow_my_sortby";
    /** Content type. */
    private String contentType;
    /** Default sort by string. */
    public static final String DEFAULT_SORT_BY = WorkflowRowDto.COLUMN_WORKFLOW_NAME;

    /** HashMap that holds the 'Global Workflows' sorting context and is thread safe. */
    private Map <String, Object> globalMap;
    /** HashMap that holds the 'My Workflows' sorting context and is thread safe. */
    private Map <String, Object> myMap;
    /**
     *  The projectAndDatasetInfoMap. These are projects and datasets for
     *  which the user has view access.
     */
    private Map <String, JSONObject> viewableProjectAndDatasetInfoMap;
    /**
     *  Projects and datasets user can attach files to. These are projects
     *  and datasets for which the user has edit access.
     *  Because this does not include existing files, it is not component-specific.
     */
    private JSONObject editableProjectAndDatasetInfo = null;

    /** Public projects. */
    private List<ProjectItem> publicProjects = null;
    /** My projects. */
    private List<ProjectItem> myProjects = null;
    /** My samples by dataset. */
    private Map<DatasetItem, List<SampleItem>> datasetSampleMap;
    private Map<ProjectItem, Integer> viewableDatasetCountMap;
    private Map<ProjectItem, Integer> editableDatasetCountMap;
    private Map<Integer, List<Integer>> datasetToFileIdMap;
    /** Time at which viewable info was cached. Used to check for data changes. */
    // Not necessary for 'editable' as file info isn't cached.
    private static Date timeViewableInfoCached = null;
    /** Current dataset... the dataset by which we most recently accessed this WF. */
    private DatasetItem currentDataset = null;
    /** The search attributes by (key,value) pair. */
    private Map<String, String> searchAttributes;

    public static String WF_SEARCH_BY = "wf_search_by";
    public static String WF_SEARCH_AUTHOR = "wf_search_author";
    public static String WF_SEARCH_COMPONENT = "wf_search_component";
    public static String WF_SEARCH_DATE_LOWER = "wf_search_date_lower";
    public static String WF_SEARCH_DATE_UPPER = "wf_search_date_upper";
    public static String WF_SEARCH_TAGS = "workflowTagsDivSearch";

    public static String WF_SEARCH_PANEL = "wf_search_panel";

    public static String WF_SEARCH_ACCESS_REQUEST = "wf_search_access_request";
    public static String WF_SEARCH_ACCESS_NO_AUTH = "wf_search_access_no_auth";
    public static String WF_SEARCH_ACCESS_SHARED = "wf_search_access_shared";

    public static String WF_HAS_PAPERS = "wf_has_papers";

    public static String WF_SEARCH_DATASET = "wf_search_dataset";

    public static String[] SEARCH_ATTRIBUTE_IDS = {
            WF_HAS_PAPERS,
            WF_SEARCH_BY,
            WF_SEARCH_AUTHOR,
            WF_SEARCH_COMPONENT,
            WF_SEARCH_DATE_LOWER,
            WF_SEARCH_DATE_UPPER,

            WF_SEARCH_PANEL,

            WF_SEARCH_ACCESS_REQUEST,
            WF_SEARCH_ACCESS_NO_AUTH,
            WF_SEARCH_ACCESS_SHARED };

    public static String WF_MY_FOLDERS = "my-folders";

    public static Integer DEFAULT_PAGE = 1;
    public static Integer DEFAULT_PAGE_LIMIT = 10;
    private Map<String, Integer> page;
    private Integer pageLimit;

    /**
     * Default Constructor.
     */
    public WorkflowContext() {
        globalMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        myMap = Collections.synchronizedMap(new HashMap <String, Object> ());
        viewableProjectAndDatasetInfoMap = Collections.synchronizedMap(new HashMap <String, JSONObject> ());
        datasetSampleMap = Collections.synchronizedMap(new HashMap <DatasetItem, List<SampleItem>> ());
        globalMap.put(WORKFLOW_SORT_BY, DEFAULT_SORT_BY);
        myMap.put(WORKFLOW_MY_SORT_BY, DEFAULT_SORT_BY);
        viewableDatasetCountMap = Collections.synchronizedMap(new HashMap<ProjectItem, Integer>());
        editableDatasetCountMap = Collections.synchronizedMap(new HashMap<ProjectItem, Integer>());
        datasetToFileIdMap = Collections.synchronizedMap(new HashMap <Integer, List<Integer>> ());

        searchAttributes = Collections.synchronizedMap(new HashMap <String, String> ());
        page = new HashMap<String, Integer>();
        pageLimit = DEFAULT_PAGE_LIMIT;

    }

    /** Returns sort type. @return the sort type as a String. */
    public String getSortBy() {
        String sortBy = null;
        if (globalMap.containsKey(WORKFLOW_SORT_BY)) {
            sortBy = (String)globalMap.get(WORKFLOW_SORT_BY);
        }

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = DEFAULT_SORT_BY;
            globalMap.put(DEFAULT_SORT_BY, true);

        }
        return sortBy;
    }

    /** Set sort type.@param sortBy the selected sort type as a String. */
    public void setSortBy(String sortBy) {
        globalMap.put(WORKFLOW_SORT_BY, sortBy);
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleSortOrder(String columnName) {
        Boolean ascFlag = true;

        if (globalMap.containsKey(columnName)) {
            ascFlag = (Boolean) globalMap.get(columnName);
            globalMap.put(columnName, !ascFlag);
        } else {
            globalMap.put(columnName, ascFlag);
        }
    }

    /** Get the sort order for a specific column.
     * @param columnName the column header
     * @return true for ascending order and false for descending
     */
    public Boolean getSortOrder(String columnName) {
        Boolean ascFlag = true;

        if (globalMap.containsKey(columnName)) {
            ascFlag = (Boolean) globalMap.get(columnName);
        } else {
            globalMap.put(columnName, ascFlag);
        }
        return ascFlag;
    }

    /**
     * Sets the flag describing if the sort order is ascending.
     * @param columnName the columnName
     * @param sortOrder the flag describing if the sort order is ascending
     */
    public void setSortOrder(String columnName, Boolean sortOrder) {

        globalMap.put(columnName, sortOrder);
    }

    /** Returns sort type. @return the sort type as a String. */
    public String getMySortBy() {
        String sortBy = null;
        if (myMap.containsKey(WORKFLOW_MY_SORT_BY)) {
            sortBy = (String)myMap.get(WORKFLOW_MY_SORT_BY);
        }

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = DEFAULT_SORT_BY;
            myMap.put(DEFAULT_SORT_BY, true);

        }
        return sortBy;
    }

    /** Set sort type.@param sortBy the selected sort type as a String. */
    public void setMySortBy(String sortBy) {
        myMap.put(WORKFLOW_MY_SORT_BY, sortBy);
    }

    /**
     * Toggles the sort order for a specific column.
     * @param columnName the column header
     */
    public void toggleMySortOrder(String columnName) {
        Boolean ascFlag = true;

        if (myMap.containsKey(columnName)) {
            ascFlag = (Boolean) myMap.get(columnName);
            myMap.put(columnName, !ascFlag);
        } else {
            myMap.put(columnName, ascFlag);
        }
    }

    /** Get the sort order for a specific column.
     * @param columnName the column header
     * @return true for ascending order and false for descending
     */
    public Boolean getMySortOrder(String columnName) {
        Boolean ascFlag = true;

        if (myMap.containsKey(columnName)) {
            ascFlag = (Boolean) myMap.get(columnName);
        } else {
            myMap.put(columnName, ascFlag);
        }
        return ascFlag;
    }

    /**
     * Sets the flag describing if the sort order is ascending.
     * @param columnName the columnName
     * @param sortOrder the flag describing if the sort order is ascending
     */
    public void setMySortOrder(String columnName, Boolean sortOrder) {

        myMap.put(columnName, sortOrder);
    }

    public void setContentType(String subTab) {
        this.contentType = subTab;
    }

    public String getContentType() {
        return contentType;
    }

    public void setViewableProjectsAndDatasets(String componentName, JSONObject projectsAndDatasetsJsonInfo) {
        if (viewableProjectAndDatasetInfoMap.containsKey(componentName)) {
            viewableProjectAndDatasetInfoMap.remove(componentName);
        }
        viewableProjectAndDatasetInfoMap.put(componentName, projectsAndDatasetsJsonInfo);
        setTimeViewableInfoCached();
    }

    public JSONObject getViewableProjectsAndDatasets(String componentName) {
        if (viewableProjectAndDatasetInfoMap.containsKey(componentName)) {
            return viewableProjectAndDatasetInfoMap.get(componentName);
        }
        return null;
    }

    public void removeViewableProjectsAndDatasets(String componentName) {
        if (viewableProjectAndDatasetInfoMap.containsKey(componentName)) {
            viewableProjectAndDatasetInfoMap.remove(componentName);
        }
    }

    public void setEditableProjectsAndDatasets(JSONObject projectsAndDatasetsJsonInfo) {
        editableProjectAndDatasetInfo = projectsAndDatasetsJsonInfo;
    }

    public JSONObject getEditableProjectsAndDatasets() {
        return editableProjectAndDatasetInfo;
    }

    public void removeEditableProjectsAndDatasets() {
        editableProjectAndDatasetInfo = null;
    }

    public List<ProjectItem> getPublicProjects() {
        return publicProjects;
    }

    public List<ProjectItem> getMyProjects() {
        return myProjects;
    }

    public void setMyProjects(List<ProjectItem> myProjects) {
        this.myProjects = Collections.synchronizedList(myProjects);
    }

    public void setPublicProjects(List<ProjectItem> publicProjects) {
        this.publicProjects = Collections.synchronizedList(publicProjects);
    }

    public List<SampleItem> getSamples(DatasetItem datasetItem) {
        if (datasetSampleMap.containsKey(datasetItem)) {
            return datasetSampleMap.get(datasetItem);
        }
        return null;
    }

    public void setSamples(DatasetItem datasetItem, List<SampleItem> sampleItems) {
        if (datasetSampleMap.containsKey(datasetItem)) {
            datasetSampleMap.remove(datasetItem);
        }
        datasetSampleMap.put(datasetItem, Collections.synchronizedList(sampleItems));
    }

    /**
     * Get the viewableDatasetCount for the specified project
     * @param project the ProjectItem
     * @return the dataset count
     */
    public Integer getViewableDatasetCount(ProjectItem project) {
        if (viewableDatasetCountMap.containsKey(project)) {
            return viewableDatasetCountMap.get(project);
        }
        return 0;
    }

    /**
     * Set the viewableDatasetCount for the specified project
     * @param project the ProjectItem
     * @param size the number of datasets
     */
    public void setViewableDatasetCount(ProjectItem project, Integer size) {
        // Don't let a user set it to null.
        if (size == null) { size = new Integer(0); }
        if (viewableDatasetCountMap.containsKey(project)) {
            viewableDatasetCountMap.remove(project);
        }
        viewableDatasetCountMap.put(project, size);
    }

    /**
     * Get the editableDatasetCount for the specified project
     * @param project the ProjectItem
     * @return the dataset count
     */
    public Integer getEditableDatasetCount(ProjectItem project) {
        if (editableDatasetCountMap.containsKey(project)) {
            return editableDatasetCountMap.get(project);
        }
        return 0;
    }

    /**
     * Set the editableDatasetCount for the specified project
     * @param project the ProjectItem
     * @param size the number of datasets
     */
    public void setEditableDatasetCount(ProjectItem project, Integer size) {
        // Don't let a user set it to null.
        if (size == null) { size = new Integer(0); }
        if (editableDatasetCountMap.containsKey(project)) {
            editableDatasetCountMap.remove(project);
        }
        editableDatasetCountMap.put(project, size);
    }
    /**
     * Get the datasetToFileIdMap for the specified dataset
     * @param datasetId the dataset id
     * @return the File id list
     */
    public List<Integer> getDatasetToFileIdMap(Integer datasetId) {
        if (datasetToFileIdMap.containsKey(datasetId)) {
            return datasetToFileIdMap.get(datasetId);
        }
        return null;
    }

    /**
     * Set the datasetToFileIdMap for the specified dataset
     * @param datasetId the dataset id
     * @param fileIdList the File id list
     */
    public void setDatasetToFileIdMap(Integer datasetId, List<Integer> fileIdList) {
        // Don't let a user set it to null.
        if (fileIdList == null) {
            fileIdList = new ArrayList<Integer>();
        }
        if (datasetToFileIdMap.containsKey(datasetId)) {
            datasetToFileIdMap.remove(datasetId);
        }
        datasetToFileIdMap.put(datasetId, fileIdList);
    }
    /**
     * Get the time viewable info was cached.
     * @return the time
     */
    public Date getTimeViewableInfoCached() {
        return timeViewableInfoCached;
    }

    /**
     * Set the time viewable info was cached.
     */
    public void setTimeViewableInfoCached() {
        this.timeViewableInfoCached = new Date();
    }

    /**
     * Get the current dataset item. This is the dataset by which this workflow
     * was most recently accessed. Null if not via dataset.
     */
    public DatasetItem getCurrentDataset() { return currentDataset; }

    /**
     * Set the current dataset item. This is the dataset by which this workflow
     * was most recently accessed. Null if not via dataset.
     */
    public void setCurrentDataset(DatasetItem dataset) {
        this.currentDataset = dataset;
    }

    /**
     * Get the search attribute map.
     */
    public Map<String, String> getSearchAttributes() { return this.searchAttributes; }

    /**
     * Get a specific search attribute.
     */
    public String getSearchAttribute(String key) {
        if (this.searchAttributes.containsKey(key)) {
            return this.searchAttributes.get(key);
        }
        return new String();
    }

    /**
     * Set the current dataset item. This is the dataset by which this workflow
     * was most recently accessed. Null if not via dataset.
     */
    public void removeSearchAttribute(String key) {
        this.searchAttributes.remove(key);
    }

    /**
     * Set the current dataset item. This is the dataset by which this workflow
     * was most recently accessed. Null if not via dataset.
     */
    public void setSearchAttribute(String key, String val) {
        this.searchAttributes.put(key, val);
    }

    public Integer getPage(String panelId) {
        if (page.containsKey(panelId)) {
            return page.get(panelId);
        }
        return new Integer(DEFAULT_PAGE);
    }

    public void setPage(String panelId, Integer pageNumber) {
        page.put(panelId, pageNumber);
    }

    public Integer getPageLimit() {
        if (pageLimit != null) {
            return pageLimit;
        }
        return DEFAULT_PAGE_LIMIT;
    }

    public void setPageLimit(Integer pageLimit) {
        this.pageLimit = pageLimit;
    }

}
