/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.workflows;

import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.dto.KcModelDTO;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.workflows.WorkflowTagItem;
import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * Used to transfer row data for the Workflows page.
 *
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowRowDto extends DTO implements Serializable {

    public static String[] PROJECT_REQUEST_MAP_KEYS = { "unrequestedProjects",
        "pendingRequestProjects", "reRequestProjects",
            "nonShareableProjects", "accessibleProjects" };
    /** Workflow row variables. */
    /** Owner id. */
    String ownerId;
    /** Owner email string. */
    String ownerEmail;
    /** Is the workflow global. */
    Boolean isGlobal;
    /** Workflow id. */
    Long workflowId;
    /** Workflow name. */
    String workflowName;
    /** Workflow xml. */
    String workflowXml;
    /** Workflow status. */
    String state;
    /** Workflow description. */
    String description;
    /** Last updated time. */
    Date lastUpdated;
    /** Dataset Id. */
    Integer datasetId;
    /** Dataset name. */
    String datasetName;
    /** Is Data Accessible? */
    String isDataAccessible;

    /** Data Access HTML */
    String dataAccessHtml;
    /** Actionable name string (html). */
    String actionableNameString;
    /** Actionable icon string (html). */
    String actionableIconString;
    /** Pencil icon string (html). */
    String pencilIconString;
    /** Workflow history list. */
    Boolean hasWorkflowHistory;
    /** Whether the workflow has unowned, private files used in Imports. */
    Boolean hasUnownedPrivateFiles;
    /** Workflow project id list (projects to which the user can request access). */
    private List<ProjectItem> shareableProjects;
    /** Workflow project id list for those with recent access requests (24 hours). */
    private List<ProjectItem> activeRequestProjects;
    /** Workflow project id list for those with access requests older than 24 hours. */
    private List<ProjectItem> reRequestProjects;
    /** Workflow project id list for user-accessible projects. */
    private List<ProjectItem> accessibleProjects;
    /** Workflow project id list for user-accessible projects. */
    private List<ProjectItem> nonShareableProjects;
    /** List of tags associated with this workflow. */
    private List<WorkflowTagItem> tags;
    
    /** Default constructor. */
    public WorkflowRowDto() {
        shareableProjects = new ArrayList<ProjectItem>();
        activeRequestProjects = new ArrayList<ProjectItem>();
        reRequestProjects = new ArrayList<ProjectItem>();
        accessibleProjects = new ArrayList<ProjectItem>();
        nonShareableProjects = new ArrayList<ProjectItem>();
        tags = new ArrayList<WorkflowTagItem>();
        hasUnownedPrivateFiles = false;
    }

    /** Get the owner id.
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }


    /** Set the owner id.
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    /** Get the is_global workflow flag.
     * @return the isGlobal
     */
    public Boolean getIsGlobal() {
        return isGlobal;
    }


    /** Set the is_global workflow flag.
     * @param isGlobal the isGlobal flag to set
     */
    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }


    /** Get the workflow id.
     * @return the workflowId
     */
    public Long getWorkflowId() {
        return workflowId;
    }

    /** Set the workflow id.
     * @param workflowId the workflowId to set
     */
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
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
     * Get the workflow state.
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Set the workflow state.
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Get the workflow description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the workflow description.
     * @param description the description to set
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
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }

    /**
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return the datasetName
     */
    public String getDatasetName() {
        if (datasetName == null) {
            datasetName = new String("");
        }
        return datasetName;
    }

    /**
     * @param datasetName the datasetName to set
     */
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * @return the isDataAccessible
     */
    public String getIsDataAccessible() {
        return isDataAccessible;
    }

    /**
     * @param isDataAccessible the isDataAccessible to set
     */
    public void setIsDataAccessible(String isDataAccessible) {
        this.isDataAccessible = isDataAccessible;
    }

    /** Set this workflows project IDs. */
    public void setProjects(List<ProjectItem> projects) { this.shareableProjects = projects; }

    /**Get this workflows project IDs. */
    public List<ProjectItem> getProjects() {
        shareableProjects = checkNull(shareableProjects);
        return shareableProjects;
    }

    /** Set this workflows user-accessible project IDs. */
    public void setAccessibleProjects(List<ProjectItem> accessibleProjects) { this.accessibleProjects = accessibleProjects; }

    /**Get this workflows user-accessible project IDs. */
    public List<ProjectItem> getAccessibleProjects() {
        accessibleProjects = checkNull(accessibleProjects);
        return accessibleProjects;
    }

    /** Set this workflows project IDs which have recent access requests. */
    public void setActiveRequestProjects(List<ProjectItem> activeRequestProjects) { this.activeRequestProjects = activeRequestProjects; }

    /**Get this workflows project IDs which have recent access requests. */
    public List<ProjectItem> getActiveRequestProjects() {
        activeRequestProjects = checkNull(activeRequestProjects);
        return activeRequestProjects;
    }

    /** Set this workflows project IDs which have older access requests. */
    public void setReRequestProjects(List<ProjectItem> reRequestProjects) { this.reRequestProjects = reRequestProjects; }

    /**Get this workflows project IDs which have older access requests. */
    public List<ProjectItem> getReRequestProjects() {
        reRequestProjects = checkNull(reRequestProjects);
        return reRequestProjects;
    }
    /** Set this workflow's project IDs which are user-accessible. */
    public void setNonShareableProjects(List<ProjectItem> nonShareableProjects) { this.nonShareableProjects = nonShareableProjects; }

    /** Get this workflows non-shareable project IDs. */
    public List<ProjectItem> getNonShareableProjects() {
        nonShareableProjects = checkNull(nonShareableProjects);
        return nonShareableProjects;
    }

    /** Set this workflow's list of associated tags. */
    public void setTags(List<WorkflowTagItem> tags) { this.tags = tags; }
    
    /** Get this workflow's list of associated tags. */
    public List<WorkflowTagItem> getTags() {
        this.tags = checkNull(this.tags);
        return this.tags;
    }

    /**
     * Displays either an up or down arrow.
     * @param sortByParam the sort by parameter (column header to sort by)
     * @param column the actual column header selected
     * @param ascFlag is ascending
     * @return the path to the appropriate image
     */
    public static String showSortOrder(String sortByParam, String column, Boolean ascFlag) {
        String imgIcon = "images/trans_spacer.gif";
        if (sortByParam != null && sortByParam.equals(column)) {
            imgIcon = ascFlag
                    ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Defines which sorting parameters to use for sorting WorkflowRowDto's
     * based on an user selected column; handles ascending or descending.
     * @param sortByString name of the column to sort by
     * @param isAscending flag indication ascending or descending sort
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(String sortByString, Boolean isAscending) {
        // Assign sort parameters based on the column the user elects to sort.
        if (sortByString.equals(COLUMN_WORKFLOW_NAME)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_WORKFLOW_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_WORKFLOW_NAME_DESC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_OWNER_ID)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_OWNER_ID_ASC,
                                                SortParameter.SORT_BY_WORKFLOW_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_OWNER_ID_DESC,
                                                SortParameter.SORT_BY_WORKFLOW_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_LAST_UPDATED)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_LAST_UPDATED_ASC,
                                                SortParameter.SORT_BY_WORKFLOW_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_LAST_UPDATED_DESC,
                                                SortParameter.SORT_BY_WORKFLOW_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_WORKFLOW_STATE)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_STATE_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_STATE_DESC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_DATASET_NAME)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_DATASET_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_DATASET_NAME_DESC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_IS_DATA_ACCESSIBLE)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_IS_DATA_ACCESSIBLE_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_IS_DATA_ACCESSIBLE_DESC };
                return sortParams;
            }
        }
        return null;
    }

    /** A column header for the workflow rows. */
    public static final String COLUMN_WORKFLOW_NAME = "Workflow Name";
    /** A column header for the workflow rows. */
    public static final String COLUMN_OWNER_ID = "Owner";
    /** A column header for the workflow rows. */
    public static final String COLUMN_LAST_UPDATED = "Last Updated";
    /** A column header for the workflow rows. */
    public static final String COLUMN_WORKFLOW_STATE = "State";
    /** A column header for the workflow rows. */
    public static final String COLUMN_DATASET_NAME = "Dataset";
    /** A column header for the workflow rows. */
    public static final String COLUMN_IS_DATA_ACCESSIBLE = "Is Data Accessible";


    /**
     * Comparator object used for sorting by parameters.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<WorkflowRowDto> getComparator(SortParameter... sortParameters) {
        return new WorkflowComparator(sortParameters);
    }

    /** The sort parameters. */
    public enum SortParameter {
        /* Ascending sorts. */
        /** Sort by owner id ascending. */
        SORT_BY_OWNER_ID_ASC,
        /** Sort by workflow name ascending. */
        SORT_BY_WORKFLOW_NAME_ASC,
        /** Sort by last updated ascending. */
        SORT_BY_LAST_UPDATED_ASC,
        /* Descending sorts. */
        /** Sort by owner id descending. */
        SORT_BY_OWNER_ID_DESC,
        /** Sort by workflow name descending. */
        SORT_BY_WORKFLOW_NAME_DESC,
        /** Sort by last updated descending. */
        SORT_BY_LAST_UPDATED_DESC,
        /** Sort by state ascending. */
        SORT_BY_STATE_ASC,
        /** Sort by state descending. */
        SORT_BY_STATE_DESC,
        /** Sort by dataset name ascending. */
        SORT_BY_DATASET_NAME_ASC,
        /** Sort by dataset name descending. */
        SORT_BY_DATASET_NAME_DESC,
        /** Sort by isDataAccessible ascending. */
        SORT_BY_IS_DATA_ACCESSIBLE_ASC,
        /** Sort by isDataAccessible descending. */
        SORT_BY_IS_DATA_ACCESSIBLE_DESC
    }

    /**
     * A class that supports comparison between two WorkflowRowDTOs
     * using sort attributes supplied to the constructor.
     *
     */
    private static final class WorkflowComparator implements Comparator<WorkflowRowDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;
        /**
         * Constructor.
         * @param parameters the sort parameters
         */
        private WorkflowComparator(SortParameter[] parameters) {
            this.parameters = parameters;
        }



        /**
         * Comparator.
         * @param o1 the first object being compared
         * @param o2 the second object being compared
         * @return the comparator value
         */
        public int compare(WorkflowRowDto o1, WorkflowRowDto o2) {
            if (parameters == null) {
                SortParameter[] param = {SortParameter.SORT_BY_WORKFLOW_NAME_ASC};
                parameters = param;
            }

            int comparison = 0;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case SORT_BY_OWNER_ID_ASC:
                        comparison = o1.getOwnerId().compareToIgnoreCase(o2.getOwnerId());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_WORKFLOW_NAME_ASC:
                        comparison = o1.getWorkflowName().compareToIgnoreCase(o2.getWorkflowName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_UPDATED_ASC:
                        comparison = dateComparison(o1.getLastUpdated(), o2.getLastUpdated(), true);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_STATE_ASC:
                        comparison = o1.getState().compareTo(o2.getState());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_DATASET_NAME_ASC:
                        comparison = o1.getDatasetName().compareToIgnoreCase(o2.getDatasetName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_IS_DATA_ACCESSIBLE_ASC:
                        comparison = o1.getIsDataAccessible().compareToIgnoreCase(o2.getIsDataAccessible());
                        if (comparison != 0) { return comparison; }
                        break;

                    // Descending
                    case SORT_BY_OWNER_ID_DESC:
                        comparison = o2.getOwnerId().compareToIgnoreCase(o1.getOwnerId());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_WORKFLOW_NAME_DESC:
                        comparison = o2.getWorkflowName().compareToIgnoreCase(o1.getWorkflowName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_LAST_UPDATED_DESC:
                        comparison = dateComparison(o1.getLastUpdated(), o2.getLastUpdated(), false);
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_STATE_DESC:
                        comparison = o2.getState().compareTo(o1.getState());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_DATASET_NAME_DESC:
                        comparison = o2.getDatasetName().compareToIgnoreCase(o1.getDatasetName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_IS_DATA_ACCESSIBLE_DESC:
                        comparison = o2.getIsDataAccessible().compareToIgnoreCase(o1.getIsDataAccessible());
                        if (comparison != 0) { return comparison; }
                        break;

                    default:
                        // Nothing is default
                }
            }

            return 0;
        }
    }

    /**
     * Set the owner email string.
     * @param ownerEmail
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;

    }

    /**
     * Get the owner email string.
     * @return the ownerEmail
     */
    public String getOwnerEmail() {
        return ownerEmail;

    }

    /**
     * Set the pencil icon string.
     * @param pencilIconString
     */
    public void setPencilIcon(String pencilIconString) {
        this.pencilIconString = pencilIconString;

    }

    /**
     * Get the pencil icon string.
     * @return the pencilIconString
     */
    public String getPencilIcon() {
        return pencilIconString;

    }

    /**
     * Set the data access HTML string
     * @param dataAccessHtml
     */
    public void setDataAccessHtml(String dataAccessHtml) {
        this.dataAccessHtml = dataAccessHtml;
    }

    /**
     * Get the data access HTML string
     * @return String the dataAccessHtml
     */
    public String getDataAccessHtml() {
        return dataAccessHtml;
    }

    /**
     * Set the actionable name string.
     * @param actionableNameString
     */
    public void setActionableName(String actionableNameString) {
        this.actionableNameString = actionableNameString;

    }

    /**
     * Get the actionable name string.
     * @return the actionableNameString
     */
    public String getActionableName() {
        return actionableNameString;

    }

    /**
     * Set the actionable icon string.
     * @param actionableIconString
     */
    public void setActionableIcons(String actionableIconString) {
        this.actionableIconString = actionableIconString;

    }

    /**
     * Get the actionable icon string.
     * @return the actionableIconString
     */
    public String getActionableIcons() {
        return actionableIconString;

    }

    /**
     * Returns a list of WorkflowHistoryDto's.
     * @return a list of WorkflowHistoryDto's
     */
    public Boolean hasWorkflowHistory() {
        return hasWorkflowHistory;
    }

    /**
     * Sets the Workflow History.
     * @param hasWorkflowHistory a list of WorkflowHistoryDto's
     */
    public void hasWorkflowHistory(Boolean hasWorkflowHistory) {
        this.hasWorkflowHistory = hasWorkflowHistory;
    }

    public void setHasUnownedPrivateFiles(Boolean hasUnownedPrivateFiles) {
        this.hasUnownedPrivateFiles = hasUnownedPrivateFiles;
    }

    public Boolean getHasUnownedPrivateFiles() {
        return hasUnownedPrivateFiles;
    }

}
