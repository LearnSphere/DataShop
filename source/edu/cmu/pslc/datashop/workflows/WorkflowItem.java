/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.workflows.WorkflowFileItem;
import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 *
 * @author Mike Komisin
 * @version $Revision: $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */

public class WorkflowItem extends Item implements java.io.Serializable, Comparable  {

    /** Database generated unique Id for this workflow. */
    private Long id;
    /** The id of the user that created this workflow. */
    private UserItem owner;
    /** The optional datasets associated with this workflow. */
    private Set<DatasetItem> datasets;

    /** Name of this workflow. */
    private String workflowName;
    /** Flag indicating whether this workflow is viewable by more than just the owner. */
    private Boolean globalFlag;
    /** Flag indicating whether this workflow is recommended. */
    private Boolean isRecommended;
    /** Description of this workflow as a string. */
    private String description;
    /** The XML which defines the workflow. */
    private String workflowXml;
    /** The last updated time. */
    private Date lastUpdated;
    /** Collection of workflow history items associated with this workflow. */
    private Set workflowHistory;
    /** Collection of workflow annotation items associated with this workflow. */
    private Set workflowAnnotation;
    /** The results of running the workflow. */
    private String results;
    /** The workflow state. */
    private String state;
    /** Files (images, imports, and exports) associated with a workflow. */
    private Set<WorkflowFileItem> files;
    /** Component queue. */
    private Set<WorkflowComponentInstanceItem> workflowComponentInstances;
    /** Persistence of component queue (actual saved). */
    private Set<WorkflowComponentInstancePersistenceItem> workflowComponentInstancePersistences;
    /** Persistent component files (non-owner cannot view non-persistent files). */
    private Set<ComponentFilePersistenceItem> componentFilePersistences;
    /** Workflow folders. */
    private Set<WorkflowFolderItem> workflowFolders;
    /** Workflow papers. */
    private Set<WorkflowPaperItem> workflowPapers;
    /** Workflow tags. */
    private Set<WorkflowTagItem> workflowTags;

    /** Collection of all allowed items in the level enumeration. */
    public static final List WF_STATE_ENUM = new ArrayList();

    /** Level enumeration field value - "edit". */
    public static final String LEVEL_EDIT = "edit";
    /** Level enumeration field value - "edit". */
    public static final String LEVEL_VIEW = "view";

    /** Workflow state. */
    public static final String WF_STATE_NEW = "new";
    /** Workflow state. */
    public static final String WF_STATE_RUNNING = "running";
    /** Workflow state. */
    public static final String WF_STATE_RUNNING_DIRTY = "running_dirty";
    /** Workflow state. */
    public static final String WF_STATE_ERROR = "error";
    /** Workflow state. */
    public static final String WF_STATE_SUCCESS = "success";
    /** Workflow state. */
    public static final String WF_STATE_PAUSED = "paused";

    /**
     * Adds each message type to the enumerated list.
     * Will fail with error if removed (mike).
     */
    static {
        WF_STATE_ENUM.add(WF_STATE_NEW);
        WF_STATE_ENUM.add(WF_STATE_RUNNING);
        WF_STATE_ENUM.add(WF_STATE_RUNNING_DIRTY);
        WF_STATE_ENUM.add(WF_STATE_ERROR);
        WF_STATE_ENUM.add(WF_STATE_SUCCESS);
        WF_STATE_ENUM.add(WF_STATE_PAUSED);
    }

    /** Default constructor. */
    public WorkflowItem() {
        this.globalFlag = Boolean.FALSE;
        state = WF_STATE_NEW;
    }

    /**
     *  Constructor with id.
     *  @param workflowId Database generated unique Id for this workflow.
     */
    public WorkflowItem(Long workflowId) {
        this.id = workflowId;
        this.globalFlag = Boolean.FALSE;
        this.isRecommended = Boolean.FALSE;
        if (state == null) {
            state = WF_STATE_NEW;
        }
    }

    /**
     * Get workflowId.
     * @return the Integer id as a Comparable
     */

    public Long getId() {
        return this.id;
    }

    /**
     * Set workflowId.
     * @param workflowId Database generated unique Id for this workflow.
     */
    public void setId(Long workflowId) {
        this.id = workflowId;
    }
    /**
     * Get owner.
     * @return the user item of the owner of this workflow
     */

    public UserItem getOwner() {
        return this.owner;
    }

    /**
     * Set owner.
     * @param owner The id of the user that created this workflow.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /** Get the workflow name.
     * @return the workflowName
     */
    public String getWorkflowName() {
        return workflowName;
    }

    /** Set the workflow name.
     * @param workflowName the workflowName to set
     */
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }


    /**
     * We often want to just show the name and id for an item.
     * @return the formatted name and id of this item
     */
    public String getNameAndId() { return getNameAndId(getWorkflowName()); }

    /**
     * Get globalFlag.
     * @return Boolean
     */
    public Boolean getGlobalFlag() {
        return this.globalFlag;
    }

    /**
     * Set globalFlag.
     * @param globalFlag Flag indicating whether
     * this workflow is viewable by more than just the owner.
     */
    public void setGlobalFlag(Boolean globalFlag) {
        this.globalFlag = globalFlag;
    }
    /**
     * @return the isRecommended
     */
    public Boolean getIsRecommended() {
        return isRecommended;
    }

    /**
     * @param isRecommended the isRecommended to set
     */
    public void setIsRecommended(Boolean isRecommended) {
        this.isRecommended = isRecommended;
    }

    /**
     * Get description.
     * @return the description of the workflow
     */

    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description Description of this workflow as a string.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Get the workflow XML.
     * @return the workflowXml
     */
    public String getWorkflowXml() {
        return workflowXml;
    }

    /** Set the workflow XML.
     * @param workflowXml the workflowXml to set
     */
    public void setWorkflowXml(String workflowXml) {
        this.workflowXml = workflowXml;
    }

    /** Get the last updated time.
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /** Set the last updated time.
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }


    /**
     * Get the results.
     * @return the results
     */
    public String getResults() {
        return results;
    }


    /**
     * Set the results.
     * @param results the results
     */
    public void setResults(String results) {
        this.results = results;
    }

    /**
     * Get the workflow history.
     * @return the set of workflow history items
     */
    protected Set getWorkflowHistory() {
        if (workflowHistory == null) {
            workflowHistory = new HashSet();
        }
        return workflowHistory;
    }

    /**
     * set the workflow history.
     * @param the set of workflow history items
     */
    protected Set setWorkflowHistory(Set workflowHistory) {
        if (workflowHistory == null) {
            workflowHistory = new HashSet();
        }
        return workflowHistory;
    }

    /**
     * Public method to get the workflow history.
     * @return a list instead of a set
     */
    public List<WorkflowHistoryItem> getWorkflowHistoryExternal() {
        List<WorkflowHistoryItem> sortedList = new ArrayList<WorkflowHistoryItem>(getWorkflowHistory());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Get the workflow annotations.
     * @return the set of workflow annotation items
     */
    protected Set getWorkflowAnnotation() {
        if (workflowAnnotation == null) {
            workflowAnnotation = new HashSet();
        }
        return workflowAnnotation;
    }

    /**
     * set the workflow annotations.
     * @param the set of workflow annotation items
     */
    protected Set setWorkflowAnnotation(Set workflowAnnotation) {
        if (workflowAnnotation == null) {
            workflowAnnotation = new HashSet();
        }
        return workflowAnnotation;
    }

    /**
     * Public method to get the workflow annotations.
     * @return a list instead of a set
     */
    public List<WorkflowAnnotationItem> getWorkflowAnnotationExternal() {
        List<WorkflowAnnotationItem> sortedList = new ArrayList<WorkflowAnnotationItem>(getWorkflowAnnotation());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Whether this workflow is accessible to the user.
     * @param user the user
     * @param editOnly only return workflows owned by the user, if true
     * @return whether this workflow is accessible to the user
     */
    public boolean isAccessible(UserItem user, boolean editOnly) {
        return user.equals(getOwner()) ||
            (!editOnly &&
                (user.getAdminFlag() || getGlobalFlag()));
    }

    /** Get the state.
     * @return the state
     */
    public String getState() {
        return state;
    }

    /** Set the state.
     * @param state the state to set
     */
    public void setState(String state) {
        if (isValidWorkflowState(state)) {
            this.state = state;
        } else if (state == null) {
            this.state = WF_STATE_NEW;
        } else {
            throw new LogException("State type can only be "
                + getStateTypes() + " and not : " + state);
        }
        this.state = state;
    }

    /**
     * Check for valid state.
     * @param state the workflow state
     * @return true if state is valid; false otherwise
     */
    public static boolean isValidWorkflowState(String state) {
        if (WF_STATE_ENUM.contains(state)) {
            return true;
        }
        return false;
    }

    /**
     * Return a list of the valid state types.
     * @return an unmodifiable list of the valid state types
     */
    public static List getStateTypes() {
        return Collections.unmodifiableList(WF_STATE_ENUM);
    }

    /*---------- FILES ----------*/

    /**
     * Get files.
     * @return the set of files
     */
    public Set getFiles() {
        if (this.files == null) {
            this.files = new HashSet();
        }
        return this.files;
    }

    /**
     * Public method to get Files.
     * @return a list instead of a set
     */
    public List getFilesExternal() {
        List sortedItems = new ArrayList(getFiles());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set files.
     * @param files Collection of files associated with this item.
     */
    protected void setFiles(Set files) {
        this.files = files;
    }

    /**
     * Add a file.
     * @param file file to add
     */
    public void addFile(WorkflowFileItem file) {
        if (!getFiles().contains(file)) {
            getFiles().add(file);
        }
    }

    /**
     * Remove the File Item.
     * @param item file item.
     */
    public void removeFile(WorkflowFileItem item) {
        if (getFiles().contains(item)) {
            getFiles().remove(item);
        }
    }


    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         StringBuffer buffer = new StringBuffer();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         buffer.append(objectToString("workflowId", getId()));
         buffer.append(objectToStringFK("ownerId", getOwner()));
         buffer.append(objectToString("workflowName", getWorkflowName()));
         buffer.append(objectToString("globalFlag", getGlobalFlag()));
         buffer.append(objectToString("isRecommended", getIsRecommended()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToString("workflowXml", getWorkflowXml()));
         buffer.append(objectToString("lastUpdated", getLastUpdated()));
         buffer.append(objectToString("state", getState()));
         buffer.append("]");

         return buffer.toString();
    }

     /**
      * Equals function for this class.
      * @param obj Object of any type, should be an Item for equality check
      * @return boolean true if the items are equal, false if not
      */
      public boolean equals(Object obj) {
          if (this == obj) {
              return true;
          }
          if (obj instanceof WorkflowItem) {
            WorkflowItem otherItem = (WorkflowItem)obj;

            if (!Item.objectEquals(this.getWorkflowName(), otherItem.getWorkflowName())) {
                return false;
            }
            if (!Item.objectEquals(this.getWorkflowXml(), otherItem.getWorkflowXml())) {
                return false;
            }
            if (!Item.objectEqualsFK(this.getOwner(), otherItem.getOwner())) {
                return false;
            }
            if (!Item.objectEquals(this.getGlobalFlag(), otherItem.getGlobalFlag())) {
                return false;
            }
            if (!Item.objectEquals(this.getIsRecommended(), otherItem.getIsRecommended())) {
                return false;
            }
            if (!Item.objectEquals(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!Item.objectEquals(this.getLastUpdated(), otherItem.getLastUpdated())) {
                return false;
            }
            if (!Item.objectEquals(this.getState(), otherItem.getState())) {
                return false;
            }
            return true;
        }
        return false;
    }

      /**
       * Returns the hash code for this item.
       * @return the hash code for this item
       */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflowName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWorkflowXml());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getOwner());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getGlobalFlag());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getIsRecommended());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDescription());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getLastUpdated());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getState());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     *   <li>Workflow Name</li>
     *   <li>Workflow XML</li>
     *   <li>Global Flag</li>
     *   <li>Owner</li>
     *   <li>Description</li>
     *   <li>Last Updated</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        WorkflowItem otherItem = (WorkflowItem)obj;
        int value = 0;

        value = objectCompareTo(this.getWorkflowName(), otherItem.getWorkflowName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getWorkflowXml(), otherItem.getWorkflowXml());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getGlobalFlag(), otherItem.getGlobalFlag());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getIsRecommended(), otherItem.getIsRecommended());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getOwner(), otherItem.getOwner());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getLastUpdated(), otherItem.getLastUpdated());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getState(), otherItem.getState());
        if (value != 0) { return value; }

        return value;
    }

    /**
     * Get datasets.
     * @return java.util.Set
     */
    protected Set<DatasetItem> getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet<DatasetItem>();
        }
        return this.datasets;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List<DatasetItem> getDatasetsExternal() {
        List<DatasetItem> sortedList = new ArrayList<DatasetItem>(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addDataset(DatasetItem item) {
        if (!getDatasets().contains(item)) {
            getDatasets().add(item);
        }
    }

    /**
     * Remove a dataset.
     * @param item dataset to add
     */
    public void removeDataset(DatasetItem item) {
        if (getDatasets().contains(item)) {
            getDatasets().remove(item);
        }
    }

    /**
     * Set datasets.
     * @param datasets Collection of datasets this analysis is associated with.
     */
    public void setDatasets(Set<DatasetItem> datasets) {
        this.datasets = datasets;
    }

    public Set<WorkflowComponentInstanceItem> getWorkflowComponentInstances() {
        return workflowComponentInstances;
    }

    public void setWorkflowComponentInstances(Set<WorkflowComponentInstanceItem> workflowComponentInstances) {
        this.workflowComponentInstances = workflowComponentInstances;
    }

    /**
     * Public method to get the workflow component instance.
     * @return a list instead of a set
     */
    public List<WorkflowComponentInstanceItem> getWorkflowComponentInstancesExternal() {
        List<WorkflowComponentInstanceItem> sortedList = new ArrayList<WorkflowComponentInstanceItem>(getWorkflowComponentInstances());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    public Set<WorkflowComponentInstancePersistenceItem> getWorkflowComponentInstancePersistences() {
        return workflowComponentInstancePersistences;
    }

    public void setComponentFilePersistences(Set<ComponentFilePersistenceItem> componentFilePersistences) {
        this.componentFilePersistences = componentFilePersistences;
    }


    /**
     * Get componentFilePersistences.
     * @return java.util.Set
     */
    protected Set<ComponentFilePersistenceItem> getComponentFilePersistences() {
        if (this.componentFilePersistences == null) {
            this.componentFilePersistences = new HashSet<ComponentFilePersistenceItem>();
        }
        return this.componentFilePersistences;
    }

    /**
     * Public method to get componentFilePersistences.
     * @return a list instead of a set
     */
    public List<ComponentFilePersistenceItem> getComponentFilePersistencesExternal() {
        List<ComponentFilePersistenceItem> sortedList = new ArrayList<ComponentFilePersistenceItem>(getComponentFilePersistences());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a componentFilePersistence.
     * @param item componentFilePersistence to add
     */
    public void addComponentFilePersistence(ComponentFilePersistenceItem item) {
        if (!getComponentFilePersistences().contains(item)) {
            getComponentFilePersistences().add(item);
        }
    }

    /**
     * Remove a componentFilePersistence.
     * @param item componentFilePersistence to add
     */
    public void removeComponentFilePersistence(ComponentFilePersistenceItem item) {
        if (getComponentFilePersistences().contains(item)) {
            getComponentFilePersistences().remove(item);
        }
    }


    public void setWorkflowComponentInstancePersistences(Set<WorkflowComponentInstancePersistenceItem> workflowComponentInstancePersistences) {
        this.workflowComponentInstancePersistences = workflowComponentInstancePersistences;
    }

    /**
     * Get workflowFolders.
     * @return java.util.Set
     */
    protected Set<WorkflowFolderItem> getWorkflowFolders() {
        if (this.workflowFolders == null) {
            this.workflowFolders = new HashSet<WorkflowFolderItem>();
        }
        return this.workflowFolders;
    }

    /**
     * Public method to get workflowFolders.
     * @return a list instead of a set
     */
    public List<WorkflowFolderItem> getWorkflowFoldersExternal() {
        List<WorkflowFolderItem> sortedList = new ArrayList<WorkflowFolderItem>(getWorkflowFolders());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a workflowFolder.
     * @param item workflowFolder to add
     */
    public void addWorkflowFolder(WorkflowFolderItem item) {
        if (!getWorkflowFolders().contains(item)) {
            getWorkflowFolders().add(item);
        }
    }

    /**
     * Remove a workflowFolder.
     * @param item workflowFolder to add
     */
    public void removeWorkflowFolder(WorkflowFolderItem item) {
        if (getWorkflowFolders().contains(item)) {
            getWorkflowFolders().remove(item);
        }
    }

    /**
     * Set workflowFolders.
     * @param workflowFolders Collection of workflowFolders this analysis is associated with.
     */
    public void setWorkflowFolders(Set<WorkflowFolderItem> workflowFolders) {
        this.workflowFolders = workflowFolders;
    }

    /**
     * Get workflowPapers.
     * @return java.util.Set
     */
    protected Set<WorkflowPaperItem> getWorkflowPapers() {
        if (this.workflowPapers == null) {
            this.workflowPapers = new HashSet<WorkflowPaperItem>();
        }
        return this.workflowPapers;
    }

    /**
     * Public method to get workflowPapers.
     * @return a list instead of a set
     */
    public List<WorkflowPaperItem> getWorkflowPapersExternal() {
        List<WorkflowPaperItem> sortedList = new ArrayList<WorkflowPaperItem>(getWorkflowPapers());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a workflowPaper.
     * @param item workflowPaper to add
     */
    public void addWorkflowPaper(WorkflowPaperItem item) {
        if (!getWorkflowPapers().contains(item)) {
            getWorkflowPapers().add(item);
        }
    }

    /**
     * Remove a workflowPaper.
     * @param item workflowPaper to add
     */
    public void removeWorkflowPaper(WorkflowPaperItem item) {
        if (getWorkflowPapers().contains(item)) {
            getWorkflowPapers().remove(item);
        }
    }

    /**
     * Set workflowPapers.
     * @param workflowPapers Collection of workflowPapers this analysis is associated with.
     */
    public void setWorkflowPapers(Set<WorkflowPaperItem> workflowPapers) {
        this.workflowPapers = workflowPapers;
    }

    /**
     * Get workflowTags.
     * @return java.util.Set
     */
    protected Set<WorkflowTagItem> getWorkflowTags() {
        if (this.workflowTags == null) {
            this.workflowTags = new HashSet<WorkflowTagItem>();
        }
        return this.workflowTags;
    }

    /**
     * Public method to get workflowTags.
     * @return a list instead of a set
     */
    public List<WorkflowTagItem> getWorkflowTagsExternal() {
        List<WorkflowTagItem> sortedList = new ArrayList<WorkflowTagItem>(getWorkflowTags());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a workflowTag.
     * @param item workflowTag to add
     */
    public void addWorkflowTag(WorkflowTagItem item) {
        if (!getWorkflowTags().contains(item)) {
            getWorkflowTags().add(item);
        }
    }

    /**
     * Remove a workflowTag.
     * @param item workflowTag to add
     */
    public void removeWorkflowTag(WorkflowTagItem item) {
        if (getWorkflowTags().contains(item)) {
            getWorkflowTags().remove(item);
        }
    }

    /**
     * Set workflowTags.
     * @param workflowTags Collection of workflowTags this analysis is associated with.
     */
    public void setWorkflowTags(Set<WorkflowTagItem> workflowTags) {
        this.workflowTags = workflowTags;
    }

}
