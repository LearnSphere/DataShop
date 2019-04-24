/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A project is related to a list of datasets and has a list of users
 * which are connected via the authorization table.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */

public class ProjectItem extends Item implements java.io.Serializable, Comparable  {

    /** Constant for the unclassified project. */
    public static final String UNCLASSIFIED = "Unclassified";
    /** Constant for the 'Remote Datasets' project. */
    public static final String REMOTE_DATASETS = "Remote Datasets";

    /** Project role type enumerated field value - "pi". */
    public static final String ROLE_PI = "pi";
    /** Project role type enumerated field value - "dp". */
    public static final String ROLE_DP = "dp";

    /** Project ownership type enumerated field value - "pi_ne_dp". */
    public static final String PROJECT_OWNERSHIP_PI_NE_DP = "pi_ne_dp";
    /** Project ownership type enumerated field value - "pi_eq_dp". */
    public static final String PROJECT_OWNERSHIP_PI_EQ_DP = "pi_eq_dp";
    /** Project ownership type enumerated field value - "pi_only". */
    public static final String PROJECT_OWNERSHIP_PI_ONLY = "pi_only";
    /** Project ownership type enumerated field value - "dp_only". */
    public static final String PROJECT_OWNERSHIP_DP_ONLY = "dp_only";
    /** Project ownership type enumerated field value - "none". */
    public static final String PROJECT_OWNERSHIP_NONE = "none";

    /** Database generated unique Id for this project. */
    private Integer projectId;
    /** The name of the project as a string. */
    private String projectName;
    /** The data provider for this project. */
    private UserItem dataProvider;
    /** The data primary investigator for this project. */
    private UserItem primaryInvestigator;
    /** Authorizations associated with this project. */
    private Set authorizations;
    /** Datasets associated with this project. */
    private Set datasets;
    /** Collection of ProjectTermsOfUseMap associated with this project. */
    private Set projectTermsOfUseMap;
    /** Collection of Access Request statuses associated with this project. */
    private Set accessRequestStatus;
    /** The description for this project. */
    private String description;
    /** The tags for this project. */
    private String tags;
    /** External links associated with this project. */
    private Set<ExternalLinkItem> externalLinks;
    /** User that created this project. */
    private UserItem createdBy;
    /** Time this project was created. */
    private Date createdTime;
    /** User that last updated this project. */
    private UserItem updatedBy;
    /** Time this project was last updated. */
    private Date updatedTime;
    /** Data collection type. */
    private String dataCollectionType;
    /** Shareability status. */
    private String shareableStatus;
    /** Is this project subject to the DataShop IRB? */
    private String subjectToDsIrb;
    /** The research manager's notes for this project. */
    private String researchMgrNotes;
    /** IRBs associated with this project. */
    private Set<IrbItem> irbs;
    /** Date the last dataset was added. */
    private Date datasetLastAdded;
    /** Flag for needs attention. */
    private Boolean needsAttention;
    /** Flag indicating project is a DiscourseDB dataset. */
    private Boolean isDiscourseDataset = false;

    /** List of DataCollectionType values. */
    private static final List<String> DATA_COLLECTION_TYPE_ENUM = new ArrayList<String>();
    /** DataCollectionType "Not Specified" value. */
    public static final String DATA_COLLECTION_TYPE_NOT_SPECIFIED = "not_specified";
    /** DataCollectionType "Not Human Subject" value. */
    public static final String DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT = "not_human_subject";
    /** DataCollectionType "Study Data, Consent Required" value. */
    public static final String DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ =
            "study_data_consent_req";
    /** DataCollectionType "Study Data, Consent Not Required" value. */
    public static final String DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ =
            "study_data_consent_not_req";

    static {
        DATA_COLLECTION_TYPE_ENUM.add(DATA_COLLECTION_TYPE_NOT_SPECIFIED);
        DATA_COLLECTION_TYPE_ENUM.add(DATA_COLLECTION_TYPE_NOT_HUMAN_SUBJECT);
        DATA_COLLECTION_TYPE_ENUM.add(DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_REQ);
        DATA_COLLECTION_TYPE_ENUM.add(DATA_COLLECTION_TYPE_STUDY_DATA_CONSENT_NOT_REQ);
    }

    /** List of ShareableStatus values. */
    public static final List<String> SHAREABLE_STATUS_ENUM = new ArrayList<String>();
    /** ShareableStatus "Not Submitted" value. */
    public static final String SHAREABLE_STATUS_NOT_SUBMITTED = "not_submitted";
    /** ShareableStatus "Waiting for researcher" value. */
    public static final String SHAREABLE_STATUS_WAITING_FOR_RESEARCHER = "waiting_for_researcher";
    /** ShareableStatus "Submitted for review". */
    public static final String SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW = "submitted_for_review";
    /** ShareableStatus "Shareable". */
    public static final String SHAREABLE_STATUS_SHAREABLE = "shareable";
    /** ShareableStatus "Shareable, but cannot be public". */
    public static final String SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC = "shareable_not_public";
    /** ShareableStatus "Not Shareable". */
    public static final String SHAREABLE_STATUS_NOT_SHAREABLE = "not_shareable";

    static {
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_NOT_SUBMITTED);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_WAITING_FOR_RESEARCHER);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SHAREABLE);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC);
        SHAREABLE_STATUS_ENUM.add(SHAREABLE_STATUS_NOT_SHAREABLE);
    }

    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_NOT_SUBMITTED = "Not submitted";
    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_WAITING = "Waiting for researcher";
    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_SUBMITTED = "PI submitted for review";
    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_SHAREABLE = "Shareable";
    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_SHAREABLE_NOT_PUBLIC =
            "Shareable, but cannot be public";
    /** String constant for shareability review status. */
    public static final String SHAREABLE_STATUS_STR_NOT_SHAREABLE = "Not shareable";

    /** List of SubjectToDsIrb values. */
    private static final List<String> SUBJECT_TO_DS_IRB_ENUM = new ArrayList<String>();
    /** SubjectToDsIrb "Not Specified" value. */
    public static final String SUBJECT_TO_DS_IRB_NOT_SPECIFIED = "not_specified";
    /** SubjectToDsIrb "Yes" value. */
    public static final String SUBJECT_TO_DS_IRB_YES = "yes";
    /** SubjectToDsIrb "No" value. */
    public static final String SUBJECT_TO_DS_IRB_NO = "no";

    static {
        SUBJECT_TO_DS_IRB_ENUM.add(SUBJECT_TO_DS_IRB_NOT_SPECIFIED);
        SUBJECT_TO_DS_IRB_ENUM.add(SUBJECT_TO_DS_IRB_YES);
        SUBJECT_TO_DS_IRB_ENUM.add(SUBJECT_TO_DS_IRB_NO);
    }

    /** NeedsAttention "Yes". */
    public static final String NEEDS_ATTN_YES = "Yes";
    /** NeedsAttention "No". */
    public static final String NEEDS_ATTN_NO = "No";

    /** Default constructor. */
    public ProjectItem() {
        setDataCollectionType(DATA_COLLECTION_TYPE_NOT_SPECIFIED);
        setShareableStatus(SHAREABLE_STATUS_NOT_SUBMITTED);
        setSubjectToDsIrb(SUBJECT_TO_DS_IRB_YES);
        setNeedsAttention(false);
        setCreatedTime(new Date());
    }

    /**
     *  Constructor with id.
     *  @param projectId Database generated unique Id for this project.
     */
    public ProjectItem(Integer projectId) {
        this.projectId = projectId;
        setDataCollectionType(DATA_COLLECTION_TYPE_NOT_SPECIFIED);
        setShareableStatus(SHAREABLE_STATUS_NOT_SUBMITTED);
        setSubjectToDsIrb(SUBJECT_TO_DS_IRB_YES);
        setNeedsAttention(false);
        setCreatedTime(new Date());
    }

    /**
     * Returns the id.
     * @return Integer
     */
    public Comparable getId() {
        return this.projectId;
    }

    /**
     * Set projectId.
     * @param projectId Database generated unique Id for this project.
     */
    public void setId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     * Get projectName.
     * @return the project name
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Set projectName.
     * @param projectName The name of the project as a string.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Get dataProvider.
     * @return User
     */
    public UserItem getDataProvider() {
        Hibernate.initialize(dataProvider);
        return dataProvider;
    }

    /**
     * Set dataProvider.
     * @param dataProvider The data provider for this project.
     */
    public void setDataProvider(UserItem dataProvider) {
        this.dataProvider = dataProvider;
    }

    /**
     * Get primaryInvestigator.
     * @return primaryInvestigator
     */
    public UserItem getPrimaryInvestigator() {
        Hibernate.initialize(primaryInvestigator);
        return primaryInvestigator;
    }

    /**
     * Set primaryInvestigator.
     * @param primaryInvestigator The primary investigator for this project.
     */
    public void setPrimaryInvestigator(UserItem primaryInvestigator) {
        this.primaryInvestigator = primaryInvestigator;
    }

    /**
     * Get authorizations.
     * @return the set of authorizations
     */
    protected Set getAuthorizations() {
        if (this.authorizations == null) {
            this.authorizations = new HashSet();
        }
        return this.authorizations;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List<AuthorizationItem> getAuthorizationsExternal() {
        List sortedList = new ArrayList(getAuthorizations());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a authorization.
     * @param item authorization to add
     */
    public void addAuthorization(AuthorizationItem item) {
        getAuthorizations().add(item);
        item.setProject(this);
    }

    // No removeAuthorization needed.  Too funky.

    /**
     * Set authorizations.
     * @param authorizations Authorizations associated with this project.
     */
    public void setAuthorizations(Set authorizations) {
        this.authorizations = authorizations;
    }

    /**
     * Get datasets.
     * @return the set of datasets associated with this project
     */
    protected Set getDatasets() {
        if (this.datasets == null) {
            this.datasets = new HashSet();
        }
        return this.datasets;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List<DatasetItem> getDatasetsExternal() {
        List sortedList = new ArrayList(getDatasets());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addDataset(DatasetItem item) {
        getDatasets().add(item);
        item.setProject(this);
    }

    /**
     * Remove a dataset.
     * @param item dataset to remove
     */
    public void removeDataset(DatasetItem item) {
        getDatasets().remove(item);
        item.setProject(null);
    }

    /**
     * Set datasets.
     * @param datasets Datasets associated with this project.
     */
    protected void setDatasets(Set datasets) {
        this.datasets = datasets;
    }

    /**
     * Get external links.
     * @return the set of external links associated with this project
     */
    protected Set<ExternalLinkItem> getExternalLinks() {
        if (this.externalLinks == null) {
            this.externalLinks = new HashSet<ExternalLinkItem>();
        }
        return this.externalLinks;
    }

    /**
     * Public method to get external links.
     * @return a list instead of a set
     */
    public List<ExternalLinkItem> getExternalLinksExternal() {
        List<ExternalLinkItem> sortedList = new ArrayList<ExternalLinkItem>(getExternalLinks());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add an external link.
     * @param item external link to add
     */
    public void addExternalLink(ExternalLinkItem item) {
        getExternalLinks().add(item);
        item.setProject(this);
    }

    /**
     * Remove an external link.
     * @param item external link to remove
     */
    public void removeExternalLink(ExternalLinkItem item) {
        getExternalLinks().remove(item);
        item.setProject(null);
    }

    /**
     * Set external links.
     * @param links External links associated with this project.
     */
    protected void setExternalLinks(Set<ExternalLinkItem> links) {
        this.externalLinks = links;
    }

    /**
     * Get projectTermsOfUseMap.
     * @return a set of projectTermsOfUseMap
     */
    protected Set getProjectTermsOfUseMap() {
        if (this.projectTermsOfUseMap == null) {
            this.projectTermsOfUseMap = new HashSet();
        }
        return this.projectTermsOfUseMap;
    }

    /**
     * Public method to get terms of use.
     * @return a list of terms of use
     */
    public List<ProjectTermsOfUseMapItem> getProjectTermsOfUseMapExternal() {
        List sortedList = new ArrayList(getProjectTermsOfUseMap());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a project terms of use mapping.
     * @param item project terms of use mapping to add
     */
    public void addProjectTermsOfUseMap(ProjectTermsOfUseMapItem item) {
        if (!getProjectTermsOfUseMap().contains(item)) {
            getProjectTermsOfUseMap().add(item);
        }
    }

    /**
     * Remove a terms of use.
     * @param item terms of use to remove
     */
    public void removeProjectTermsOfUseMap(ProjectTermsOfUseMapItem item) {
        if (getProjectTermsOfUseMap().contains(item)) {
            getProjectTermsOfUseMap().remove(item);
        }
    }

    /**
     * Set projectTermsOfUseMap.
     * @param projectTermsOfUseMap set of ProjectTermsOfUseMap items associated with this project.
     */
    public void setProjectTermsOfUseMap(Set projectTermsOfUseMap) {
        this.projectTermsOfUseMap = projectTermsOfUseMap;
    }

    /**
     * Get accessRequestStatus.
     * @return a set of accessRequestStatus
     */
    protected Set getAccessRequestStatus() {
        if (this.accessRequestStatus == null) {
            this.accessRequestStatus = new HashSet();
        }
        return this.accessRequestStatus;
    }

    /**
     * Set accessRequestStatus.
     * @param accessRequestStatus set of accessRequestStatus items associated with this project.
     */
    public void setAccessRequestStatus(Set accessRequestStatus) {
        this.accessRequestStatus = accessRequestStatus;
    }

    /**
     * Public method to get AccessRequestStatus items.
     * @return a list of AccessRequestStatus items
     */
    public List<AccessRequestStatusItem> getAccessRequestStatusExternal() {
        List sortedList = new ArrayList(getAccessRequestStatus());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add an AccessRequestStatus item.
     * @param item AccessRequestStatus item to add
     */
    public void addAccessRequestStatus(AccessRequestStatusItem item) {
        if (!getAccessRequestStatus().contains(item)) {
            getAccessRequestStatus().add(item);
        }
    }

    /**
     * Remove an AccessRequestStatus item.
     * @param item AccessRequestStatus item to remove
     */
    public void removeAccessRequestStatus(AccessRequestStatusItem item) {
        if (getAccessRequestStatus().contains(item)) {
            getAccessRequestStatus().remove(item);
        }
    }

    /**
     * Returns a string describing the state of this project's ownership.
     * @return a string describing the state of this project's ownership
     */
    public String getOwnership() {
        String ownership = null;

        if (getPrimaryInvestigator() != null
                && getDataProvider() != null
                && getPrimaryInvestigator().equals(getDataProvider())) {
            ownership = PROJECT_OWNERSHIP_PI_EQ_DP;

        } else if (getPrimaryInvestigator() != null
                && getDataProvider() != null
                && !getPrimaryInvestigator().equals(getDataProvider())) {
            ownership = PROJECT_OWNERSHIP_PI_NE_DP;

        } else if (getPrimaryInvestigator() != null) {
            ownership = PROJECT_OWNERSHIP_PI_ONLY;

        } else if (getDataProvider() != null) {
            ownership = PROJECT_OWNERSHIP_DP_ONLY;

        } else {
            ownership = PROJECT_OWNERSHIP_NONE;
        }
        return ownership;
    }

    /**
     * Returns a string describing the role of the user for this project.
     * @param user the user
     * @return a string describing the role of the user for this project
     */
    public String getRole(UserItem user) {
        String role = null;
        if (getPrimaryInvestigator() != null && getPrimaryInvestigator().equals(user)) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI;
        } else if (getDataProvider() != null && getDataProvider().equals(user)) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP;
        } else if (user.getAdminFlag()) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN;
        } else {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR;
        }
        return role;
    }

    /**
     * A version of the above method that also takes a flag indicating whether
     * or not the user is a project admin.
     * @param user the user
     * @param isPA flag indicating if user is a project admin
     * @return a string describing the role of the user for this project
     */
    public String getRole(UserItem user, boolean isPA) {
        String role = null;
        if (getPrimaryInvestigator() != null && getPrimaryInvestigator().equals(user)) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI;
        } else if (getDataProvider() != null && getDataProvider().equals(user)) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_DP;
        } else if (user.getAdminFlag()) {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_ADMIN;
        } else if (isPA) {
            // Project Admin acts on behalf of PI, if not PI, DP or DS-Admin.
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_PI;
        } else {
            role = AccessRequestHistoryItem.ACCESS_REQUEST_ROLE_REQUESTOR;
        }
        return role;
    }

    /**
     * Get the project description.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the project description.
     * @param description string describing this project
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the tags for this project, a comma-delimited string.
     * @return tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Set the tags for this project.
     * @param tags comma-delimited string of tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Gets createdBy.
     * @return the createdBy
     */
    public UserItem getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets the createdBy.
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(UserItem createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets createdTime.
     * @return the createdTime
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * Sets the createdTime.
     * @param createdTime the createdTime to set
     */
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * Get the user that last updated this project.
     * @return UserItem updatedBy
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user that last updated this project.
     * @param userItem the user
     */
    public void setUpdatedBy(UserItem userItem) {
        this.updatedBy = userItem;
    }

    /**
     * Get the date this project was last updated.
     * @return Date updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * Set the date this project was last updated.
     * @param updatedTime the Date
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * Get the DataCollectionType.
     * @return String dataCollectionType
     */
    public String getDataCollectionType() {
        return dataCollectionType;
    }

    /**
     * Set the Data Collection Type.
     * @param dcType String
     */
    public void setDataCollectionType(String dcType) {
        if (DATA_COLLECTION_TYPE_ENUM.contains(dcType)) {
            this.dataCollectionType = dcType;
        } else {
            throw new IllegalArgumentException("Invalid DataCollectionType value: " + dcType);
        }
    }

    /**
     * Get the Shareability Status.
     * @return String shareableStatus
     */
    public String getShareableStatus() {
        return shareableStatus;
    }

    /**
     * Check if the shareability status is a determined state,
     * i.e. shareable, shareable but not public or not shareable,
     * if so return true.
     * @return true if status is determined, false otherwise
     */
    public boolean isShareableStatusDetermined() {
        if (shareableStatus.equals(SHAREABLE_STATUS_SHAREABLE)
         || shareableStatus.equals(SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)
         || shareableStatus.equals(SHAREABLE_STATUS_NOT_SHAREABLE)) {
            return true;
        }
        return false;
    }

    /**
     * Get the Shareability Status as a string for display.
     * @return for display
     */
    public static String getShareabilityStatusString(String shareabilityStatus) {
        if (shareabilityStatus == null) { return ""; }

        if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_NOT_SUBMITTED)) {
            return SHAREABLE_STATUS_STR_NOT_SUBMITTED;
        } else if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_WAITING_FOR_RESEARCHER)) {
            return SHAREABLE_STATUS_STR_WAITING;
        } else if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_SUBMITTED_FOR_REVIEW)) {
            return SHAREABLE_STATUS_STR_SUBMITTED;
        } else if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_SHAREABLE)) {
            return SHAREABLE_STATUS_STR_SHAREABLE;
        } else if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_SHAREABLE_NOT_PUBLIC)) {
            return SHAREABLE_STATUS_STR_SHAREABLE_NOT_PUBLIC;
        } else if (shareabilityStatus.equalsIgnoreCase(ProjectItem.SHAREABLE_STATUS_NOT_SHAREABLE)) {
            return SHAREABLE_STATUS_STR_NOT_SHAREABLE;
        } else {  // default
            return SHAREABLE_STATUS_STR_NOT_SUBMITTED;
        }
    }

    /**
     * Set the Shareability Status.
     * @param shareableStatus String
     */
    public void setShareableStatus(String shareableStatus) {
        if (SHAREABLE_STATUS_ENUM.contains(shareableStatus)) {
            this.shareableStatus = shareableStatus;
        } else {
            throw new IllegalArgumentException("Invalid ShareableStatus value: " + shareableStatus);
        }
    }

    /**
     * Get the Subject to DataShop IRB indicator.
     * @return String subjectToDsIrb
     */
    public String getSubjectToDsIrb() {
        return subjectToDsIrb;
    }

    /**
     * Set the Subject to DataShop IRB indicator.
     * @param subjectToDsIrb String
     */
    public void setSubjectToDsIrb(String subjectToDsIrb) {
        if (SUBJECT_TO_DS_IRB_ENUM.contains(subjectToDsIrb)) {
            this.subjectToDsIrb = subjectToDsIrb;
        } else {
            throw new IllegalArgumentException("Invalid SubjectToDsIrb value: " + subjectToDsIrb);
        }
    }

    /**
     * Get the Research Manager's Notes.
     * @return String researchMgrNotes
     */
    public String getResearchManagersNotes() {
        return researchMgrNotes;
    }

    /**
     * Set the Research Manager's Notes.
     * @param notes String
     */
    public void setResearchManagersNotes(String notes) {
        this.researchMgrNotes = notes;
    }

    /**
     * Get IRBs.
     * @return the set of IRBs associated with this project
     */
    protected Set<IrbItem> getIrbs() {
        if (this.irbs == null) {
            this.irbs = new HashSet<IrbItem>();
        }
        return this.irbs;
    }

    /**
     * Public method to get IRBs.
     * @return a list instead of a set
     */
    public List<IrbItem> getIrbsExternal() {
        List<IrbItem> sortedList = new ArrayList<IrbItem>(getIrbs());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add an IRB.
     * @param item IRB to add
     */
    public void addIrb(IrbItem item) {
        getIrbs().add(item);
        item.addProject(this);
    }

    /**
     * Remove an IRB.
     * @param item IRB to remove
     */
    public void removeIrb(IrbItem item) {
        getIrbs().remove(item);
        item.removeProject(this);
    }

    /**
     * Set IRBs.
     * @param links IRBs associated with this project.
     */
    protected void setIrbs(Set<IrbItem> links) {
        this.irbs = links;
    }

    /**
     * Gets the datasetLastAdded.
     * @return the datasetLastAdded
     */
    public Date getDatasetLastAdded() {
        return datasetLastAdded;
    }

    /**
     * Sets the datasetLastAdded.
     * @param datasetLastAdded the datasetLastAdded to set
     */
    public void setDatasetLastAdded(Date datasetLastAdded) {
        this.datasetLastAdded = datasetLastAdded;
    }

    /**
     * Sets the datasetLastAdded to now.
     */
    public void setDatasetLastAddedToNow() {
        this.datasetLastAdded = new Date();
    }

   /**
     * Gets the needsAttention.
     * @return the needsAttention
     */
    public Boolean getNeedsAttention() {
        return needsAttention;
    }

    /**
     * Gets the needsAttention boolean as a Yes/No string for display.
     * @return the needsAttention as a Yes/No string for display.
     */
    public String getNeedsAttentionDisplay() {
        if (needsAttention.booleanValue()) {
            return NEEDS_ATTN_YES;
        }
        return NEEDS_ATTN_NO;
    }

    /**
     * Gets the boolean from the string.
     * @param needsAttnStr string version of the needs attention flag
     * @return true if 'Yes', false otherwise
     */
    public static Boolean getNeedsAttention(String needsAttnStr) {
        if (needsAttnStr.equals(NEEDS_ATTN_YES)) {
            return true;
        }
        return false;
    }

    /**
     * Sets the needsAttention.
     * @param needsAttention the needsAttention to set
     */
    public void setNeedsAttention(Boolean needsAttention) {
        this.needsAttention = needsAttention;
    }

   /**
     * Gets the isDiscourseDataset flag.
     * @return the isDiscourseDataset
     */
    public Boolean getIsDiscourseDataset() {
        return isDiscourseDataset;
    }

    /**
     * Sets the isDiscourseDataset flag.
     * @param isDiscourseDataset value of flag
     */
    public void setIsDiscourseDataset(Boolean isDiscourseDataset) {
        this.isDiscourseDataset = isDiscourseDataset;
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
         buffer.append(objectToString("Id", getId()));
         buffer.append(objectToString("ProjectName", getProjectName()));
         buffer.append(objectToStringFK("DataProvider", getDataProvider()));
         buffer.append(objectToStringFK("PrimaryInvestigator", getPrimaryInvestigator()));
         buffer.append(objectToStringFK("CreatedBy", getCreatedBy()));
         buffer.append(objectToString("CreatedTime", getCreatedTime()));
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Determines whether another object is equal to this one.
     * @param obj the object to test equality with this one
     * @return true if the items are equal, false otherwise
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProjectItem) {
            ProjectItem otherItem = (ProjectItem)obj;

            if (!objectEquals(this.getProjectName(), otherItem.getProjectName())) {
                return false;
            }

            if (!objectEqualsFK(this.getDataProvider(), otherItem.getDataProvider())) {
                return false;
            }

            if (!objectEqualsFK(this.getPrimaryInvestigator(),
                    otherItem.getPrimaryInvestigator())) {
                return false;
            }

            if (!objectEqualsFK(this.getCreatedBy(),
                    otherItem.getCreatedBy())) {
                return false;
            }

            if (!objectEquals(this.getCreatedTime(),
                    otherItem.getCreatedTime())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getProjectName());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getDataProvider());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getPrimaryInvestigator());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getCreatedBy());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCreatedTime());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>name</li>
     * <li>data provider</li>
     * <li>primary investigator</li>
     * </ul>
     *
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        ProjectItem otherItem = (ProjectItem)obj;
        int value = 0;

        value = objectCompareTo(this.getProjectName(), otherItem.getProjectName());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getDataProvider(), otherItem.getDataProvider());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getPrimaryInvestigator(),
                otherItem.getPrimaryInvestigator());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getCreatedBy(), otherItem.getCreatedBy());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getCreatedTime(), otherItem.getCreatedTime());
        if (value != 0) { return value; }

        return value;
    }
}