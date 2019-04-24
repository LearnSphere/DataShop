/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.servlet.webservices.WebServiceAuthentication;
import edu.cmu.pslc.datashop.util.UtilConstants;

import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceAuthentication.generateApiToken;
import static edu.cmu.pslc.datashop.servlet.webservices.WebServiceAuthentication.generateSecret;

/**
 * Represents a single user of the system.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 14923 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2018-03-13 12:38:51 -0400 (Tue, 13 Mar 2018) $
 * <!-- $KeyWordsOff: $ -->
 */

public class UserItem extends Item implements java.io.Serializable, Comparable<UserItem>  {

    /** Debug logging. */
    private static Logger logger = Logger.getLogger(UserItem.class.getName());

    /** The default user id for global/public datasets/samples. */
    public static final String DEFAULT_USER = "%";

    /** The system user id for denying four-week-old pending access requests. */
    public static final String SYSTEM_USER = "system";

    /** OLI formated user field from the OLI authentication service. See http://www.cmu.edu/oli/ */
    private String userId;
    /** First name of the user as a string */
    private String firstName;
    /** Last name of the user as a string */
    private String lastName;
    /** email of the user as a string */
    private String email;
    /** institution of the user as a string */
    private String institution;

    /** The public API token for web services. */
    private String apiToken;
    /** The secret key for encrypting the authentication header. */
    private String secret;
    /** Flag indicating whether the user is a system administrator (true) or not (false) */
    private Boolean adminFlag;
    /** Time this item was created. */
    private Date creationTime;
    /** Alias for this user. Used in public displays if set. */
    private String userAlias;
    /** Login id for this user, if specified by Login Provider. */
    private String loginId;
    /** Login type for this user, indication of the Login Provider. */
    private String loginType;
    /** Collection of dataset usages associated with this user. */
    private Set datasetUsages;
    /** Collection of projects this user is the data provider of. */
    private Set projectsByDataProvider;
    /** Collection of projects this user is the  primary investigator of. */
    private Set projectsByPrimaryInvestigator;
    /** Collection of authorizations for this user on projects */
    private Set authorizations;
    /** Collection of skill models owned by this user. */
    private Set skillModels;
    /** Collection of samples owned by this user. */
    private Set samples;
    /** Collection of Access Request statuses associated with this project. */
    private Set accessRequestStatus;

    /** Default constructor. */
    public UserItem() {
        this.adminFlag = Boolean.FALSE;
        this.creationTime = new Date();
    }

    /**
     * Constructor with id.
     * @param userId OLI formated user field from the OLI authentication service.
     */
    public UserItem(String userId) {
        this.userId = userId;
        this.adminFlag = Boolean.FALSE;
        this.creationTime = new Date();
    }

    /**
     * Returns the id.
     * @return String
     */
    public Comparable getId() {
        return this.userId;
    }

    /**
     * Set userId.
     * @param userId OLI formated user field from the OLI authentication service.
     */
    public void setId(String userId) {
        this.userId = userId;
    }

    /**
     * Return the user's full name if data exists,
     * otherwise return the user id.
     * @return first name plus last name if not null, otherwise the user id
     */
    public String getName() {
        if (firstName != null && lastName != null
         && firstName.length() > 0 && lastName.length() > 0) {
            return firstName + " " + lastName;
        }
        return userId;
    }

    /**
     * Returns the name and id of the user.
     * @return a concatenated string of the user's first and last name and the user id
     */
    public String getUserName() {
        StringBuffer userName = new StringBuffer(getName());
        if (!userName.toString().equals(userId)) {
            userName.append(" (");
            userName.append(getId());
            userName.append(")");
        }
        return userName.toString();
    }

    /**
     * Get firstName.
     * @return firstName
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Set firstName.
     * @param firstName First name of the user as a string
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get lastName.
     * @return lastName
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Set lastName.
     * @param lastName Last name of the user as a string
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get email.
     * @return email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Set email.
     * @param email email of the user as a string
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get institution.
     * @return institution
     */
    public String getInstitution() {
        return this.institution;
    }

    /**
     * Set institution.
     * @param institution institution of the user as a string
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * The public API token for web services.
     * @return the public API token for web services
     */
    public String getApiToken() {
        return apiToken;
    }

    /**
     * The public API token for web services.
     * @param apiToken the public API token for web services
     */
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    /**
     * The secret key for encrypting the authentication header.
     * @return the secret key for encrypting the authentication header
     */
    public String getSecret() {
        return secret;
    }

    /**
     * The secret key for encrypting the authentication header.
     * @param secret the secret key for encrypting the authentication header
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * Set (or reset) the web services API token and secret key to randomly generated values.
     */
    public void updateAuthenticationCredentials() {
        setApiToken(generateApiToken());
        setSecret(generateSecret());
    }

    /**
     * Verify that the encrypted text was generated from the plain test
     * using this users secret key.
     * @param plain the original text
     * @param encrypted the encrypted text
     * @return whether the encrypted text was generated from the plain test
     * using this users secret key
     */
    public boolean authenticate(String plain, String encrypted) {
        if (getSecret() == null) {
            throw new IllegalStateException("Tried to authenticate a user with no secret key.");
        }
        return new WebServiceAuthentication(getSecret()).authenticate(plain, encrypted);
    }

    /**
     * Get adminFlag.
     * @return Boolean
     */
    public Boolean getAdminFlag() {
        return this.adminFlag;
    }

    /**
     * Set adminFlag.
     * @param adminFlag Flag indicating whether the user is a system administrator (true) or
     * not (false)
     */
    public void setAdminFlag(Boolean adminFlag) {
        this.adminFlag = adminFlag;
    }

    /**
     * Get creation time.
     * @return the time this item was created
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Set the creation time.
     * @param creationTime the time this item was created
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get userAlias.
     * @return userAlias
     */
    public String getUserAlias() {
        return this.userAlias;
    }

    /**
     * Set userAlias.
     * @param userAlias userAlias of the user as a string
     */
    public void setUserAlias(String userAlias) {
        this.userAlias = userAlias;
    }

    /**
     * Get loginId.
     * @return loginId
     */
    public String getLoginId() {
        return this.loginId;
    }

    /**
     * Set loginId.
     * @param loginId loginId of the user as a string
     */
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    /**
     * Get loginType.
     * @return loginType
     */
    public String getLoginType() {
        return this.loginType;
    }

    /**
     * Set loginType.
     * @param loginType loginType of the user as a string
     */
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * Get datasetUsages.
     * @return dataset usages
     */
    protected Set getDatasetUsages() {
        if (this.datasetUsages == null) {
            this.datasetUsages = new HashSet();
        }
        return this.datasetUsages;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getDatasetUsagesExternal() {
        List sortedList = new ArrayList(getDatasetUsages());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset usage.
     * @param item dataset to add
     */
    public void addDatasetUsage(DatasetUsageItem item) {
        getDatasetUsages().add(item);
        item.setUser(this);
    }

    /**
     * Set datasetUsages.
     * @param items Collection of dataset usages associated with this user.
     */
    public void setDatasetUsages(Set items) {
        this.datasetUsages = items;
    }

    /**
     * Get datasets.
     * @return a set of dataset items
     */
    protected Set getProjectsByDataProvider() {
        if (this.projectsByDataProvider == null) {
            this.projectsByDataProvider = new HashSet();
        }
        return this.projectsByDataProvider;
    }

    /**
     * Public method to get datasets.
     * @return a list instead of a set
     */
    public List getProjectsByDataProviderExternal() {
        List sortedList = new ArrayList(getProjectsByDataProvider());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addProjectByDataProvider(ProjectItem item) {
        getProjectsByDataProvider().add(item);
        item.setDataProvider(this);
    }

    /**
     * Remove a dataset.
     * @param item dataset to remove
     */
    public void removeProjectByDataProvider(ProjectItem item) {
        getProjectsByDataProvider().remove(item);
        item.setDataProvider(null);
    }

    /**
     * Set datasets.
     * @param items Collection of datasets this user is the data provider of.
     */
    public void setProjectsByDataProvider(Set items) {
        this.projectsByDataProvider = items;
    }

    /**
     * Get projects.
     * @return a set of project items
     */
    protected Set getProjectsByPrimaryInvestigator() {
        if (this.projectsByPrimaryInvestigator == null) {
            this.projectsByPrimaryInvestigator = new HashSet();
        }
        return this.projectsByPrimaryInvestigator;
    }

    /**
     * Public method to get projects.
     * @return a list instead of a set
     */
    public List getProjectsByPrimaryInvestigatorExternal() {
        List sortedList = new ArrayList(getProjectsByPrimaryInvestigator());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a dataset.
     * @param item dataset to add
     */
    public void addProjectByPrimaryInvestigator(ProjectItem item) {
        getProjectsByPrimaryInvestigator().add(item);
        item.setPrimaryInvestigator(this);
    }

    /**
     * Remove a dataset.
     * @param item dataset to remove
     */
    public void removeProjectByPrimaryInvestigator(ProjectItem item) {
        getProjectsByPrimaryInvestigator().remove(item);
        item.setPrimaryInvestigator(null);
    }

    /**
     * Set datasets.
     * @param items Collection of datasets this user is the primary investigator of.
     */
    public void setProjectsByPrimaryInvestigator(Set items) {
        this.projectsByPrimaryInvestigator = items;
    }

    /**
     * Get authorizations.
     * @return a set of authorization items
     */
    protected Set getAuthorizations() {
        if (this.authorizations == null) {
            this.authorizations = new HashSet();
        }
        return this.authorizations;
    }

    /**
     * Get a list of authorization items.
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
        item.setUser(this);
    }

    // No removeAuthorization needed.  Too funky.

    /**
     * Set authorizations.
     * @param items Collection of authorizations for this user on projects
     */
    public void setAuthorizations(Set items) {
        this.authorizations = items;
    }

    /**
     * Get skillModels.
     * @return a set of skill model items
     */
    protected Set getSkillModels() {
        if (this.skillModels == null) {
            this.skillModels = new HashSet();
        }
        return this.skillModels;
    }

    /**
     * Public method to get skillModels.
     * @return a list instead of a set
     */
    public List getSkillModelsExternal() {
        List sortedList = new ArrayList(getSkillModels());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a skill model.
     * @param item skill model to add
     */
    public void addSkillModel(SkillModelItem item) {
        getSkillModels().add(item);
        item.setOwner(this);
    }

    /**
     * Remove a skill model.
     * @param item skill model to remove
     */
    public void removeSkillModel(SkillModelItem item) {
        getSkillModels().remove(item);
        item.setOwner(null);
    }

    /**
     * Set skillModels.
     * @param items Collection of skill models owned by this user.
     */
    public void setSkillModels(Set items) {
        this.skillModels = items;
    }

    /**
     * Get samples.
     * @return a set of sample items
     */
    protected Set getSamples() {
        if (this.samples == null) {
            this.samples = new HashSet();
        }
        return this.samples;
    }

    /**
     * Public method to get samples.
     * @return a list instead of a set
     */
    public List getSamplesExternal() {
        List sortedList = new ArrayList(getSamples());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Add a sample.
     * @param item sample to add
     */
    public void addSample(SampleItem item) {
        getSamples().add(item);
        item.setOwner(this);
    }

    /**
     * Remove a sample.
     * @param item sample to remove
     */
    public void removeSample(SampleItem item) {
        getSamples().remove(item);
        item.setOwner(null);
    }

    /**
     * Set samples.
     * @param items Collection of samples owned by this user.
     */
    public void setSamples(Set items) {
        this.samples = items;
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
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
        return toString("UserId", getId(), "FirstName", getFirstName(), "LastName", getLastName(),
                        "Email", getEmail(), "ApiToken", getApiToken(), "AdminFlag", getAdminFlag(),
                        "CreationTime", getCreationTime(), "UserAlias", getUserAlias(),
                        "LoginId", getLoginId(), "LoginType", getLoginType());
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

        if (obj instanceof UserItem) {
            UserItem otherItem = (UserItem)obj;

            if (!objectEquals(this.getId(), otherItem.getId())) {
                return false;
            }
            if (!objectEquals(this.getFirstName(), otherItem.getFirstName())) {
                return false;
            }
            if (!objectEquals(this.getLastName(), otherItem.getLastName())) {
                return false;
            }
            if (!objectEquals(this.getEmail(), otherItem.getEmail())) {
                return false;
            }
            if (!objectEquals(this.getAdminFlag(), otherItem.getAdminFlag())) {
                return false;
            }
            return true;
        }

        if (logger.isDebugEnabled()) {
            //We shouldn't get this debug message, but if we do, here is how we can see
            //where the call came from.
            StringWriter sw = new StringWriter();
            new Throwable("").printStackTrace(new PrintWriter(sw));
            String stackTrace = sw.toString();
            logger.debug("Object is not an instance of UserItem" + stackTrace);
        }

        return false;
    }

    /**
     * Returns the hash code for this item.
     * @return the hash code for this item
     */
    public int hashCode() {
        long hash = UtilConstants.HASH_INITIAL;
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(userId);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(firstName);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(lastName);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(email);
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(adminFlag);
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>user id</li>
     * <li>last name</li>
     * <li>first name</li>
     * <li>email</li>
     * <li>adminFlag</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(UserItem obj) {
        UserItem otherItem = (UserItem)obj;

        int value = 0;

        value = objectCompareTo(this.getLastName(), otherItem.getLastName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFirstName(), otherItem.getFirstName());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getId(), otherItem.getId());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getEmail(), otherItem.getEmail());
        if (value != 0) { return value; }

        value = objectCompareToBool(this.getAdminFlag(), otherItem.getAdminFlag());
        if (value != 0) { return value; }

        return value;
    }

}