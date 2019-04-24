/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * Represents a hierarchal layer in the dataset which groups problems together.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 6372 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2010-10-15 09:41:13 -0400 (Fri, 15 Oct 2010) $
 * <!-- $KeyWordsOff: $ -->
 */

public class DatasetLevelItem extends Item implements java.io.Serializable, Comparable  {

    /** Unit title constant for backward compatibility. */
    public static final String UNIT_TITLE = "Unit";

    /** Section title constant for backward compatibility. */
    public static final String SECTION_TITLE = "Section";

    /** Database generated unique Id for this dataset level. */
    private Integer id;
    /** Dataset that this level is describing. */
    private DatasetItem dataset;
    /** The title of this level. */
    private String levelTitle;
    /** The name of this level as a string. */
    private String levelName;
    /** The description.  CL Munger will fill this in with the human readable description. */
    private String description;
    /** The dataset level id of the parent level in the hierarchy. */
    private DatasetLevelItem parent;
    /** Collection of problems associated with this level. */
    private Set problems;
    /** Collection of children levels for this level. */
    private Set children;

    /** The left index of the nested set. */
    private Integer leftIndex;
    /** The right index of the nested set. */
    private Integer rightIndex;

    /** Default constructor. */
    public DatasetLevelItem() {
    }

    /**
     *  Constructor with id.
     *  @param datasetLevelId Database generated unique Id for this dataset level.
     */
    public DatasetLevelItem(Integer datasetLevelId) {
        this.id = datasetLevelId;
    }

    /**
     * Get datasetLevelId (Integer).
     * @return the id as a comparable
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Get datasetLevelId.
     * @return the id as an integer
     * @deprecated
     */
    public Integer getDatasetLevelId() {
        return this.id;
    }

    /**
     * Set datasetLevelId.
     * @param datasetLevelId Database generated unique Id for this dataset level.
     */
    public void setId(Integer datasetLevelId) {
        this.id = datasetLevelId;
    }
    /**
     * Get dataset.
     * @return the dataset item that owns this level
     */
    public DatasetItem getDataset() {
        return this.dataset;
    }

    /**
     * Set dataset.
     * @param dataset Dataset that this level is describing.
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get levelName.
     * @return the levelName
     */
    public String getLevelName() {
        return this.levelName;
    }

    /**
     * Set levelName.
     * @param levelName The name of this level as a string.
     */
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    /**
     * Get description.
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set description.
     * @param description The description of this level
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get levelTitle.
     * @return the levelTitle
     */
    public String getLevelTitle() {
        return this.levelTitle;
    }

    /**
     * Set levelTitle.
     * @param levelTitle The title of this level.
     */
    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    /**
     * Get parent.
     * @return a DatasetLevelItem
     */
    public DatasetLevelItem getParent() {
        return this.parent;
    }

    /**
     * Set parent.
     * @param parent The dataset level id of the parent level in the hierarchy.
     */
    public void setParent(DatasetLevelItem parent) {
        this.parent = parent;
    }

    /**
     * Get problems.
     * @return a set of problem items
     */
    protected Set getProblems() {
        if (this.problems == null) {
            this.problems = new HashSet();
        }
        return this.problems;
    }

    /**
     * Public method to get problems.
     * @return a list instead of a set
     */
    public List getProblemsExternal() {
        List sortedList = new ArrayList(getProblems());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set problems.
     * @param problems Collection of problems associated with this level.
     */
    protected void setProblems(Set problems) {
        this.problems = problems;
    }

    /**
     * Add a problem.
     * @param item to add
     */
    public void addProblem(ProblemItem item) {
        if (!getProblems().contains(item)) {
            getProblems().add(item);
            item.setDatasetLevel(this);
        }
    }

    /**
     * Get children.
     * @return a set of the children dataset levels
     */
    protected Set getChildren() {
        if (this.children == null) {
            this.children = new HashSet();
        }
        return this.children;
    }

    /**
     * Public method to get children.
     * @return a list instead of a set
     */
    public List getChildrenExternal() {
        List sortedList = new ArrayList(getChildren());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set children.
     * @param children Collection of children levels for this level.
     */
    protected void setChildren(Set children) {
        this.children = children;
    }

    /**
     * Add a child.
     * @param item to add
     */
    public void addChild(DatasetLevelItem item) {
        if (!getChildren().contains(item)) {
            getChildren().add(item);
            item.setParent(this);
        }
    }

    /**
     * remove a child.
     * @param item to remove
     */
    public void removeChild(DatasetLevelItem item) {
        if (getChildren().contains(item)) {
            getChildren().remove(item);
            item.setParent(null);
        }
    }

    /**
     * Returns leftIndex.
     * @return Returns the leftIndex.
     */
    public Integer getLeftIndex() {
        return leftIndex;
    }

    /**
     * Set leftIndex.
     * @param leftIndex The leftIndex to set.
     */
    public void setLeftIndex(Integer leftIndex) {
        this.leftIndex = leftIndex;
    }

    /**
     * Returns rightIndex.
     * @return Returns the rightIndex.
     */
    public Integer getRightIndex() {
        return rightIndex;
    }

    /**
     * Set rightIndex.
     * @param rightIndex The rightIndex to set.
     */
    public void setRightIndex(Integer rightIndex) {
        this.rightIndex = rightIndex;
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
         buffer.append(objectToString("datasetLevelId", getId()));
         buffer.append(objectToStringFK("datasetId", getDataset()));
         buffer.append(objectToString("levelTitle", getLevelTitle()));
         buffer.append(objectToString("levelName", getLevelName()));
         buffer.append(objectToString("description", getDescription()));
         buffer.append(objectToStringFK("parentId", getParent()));
         buffer.append(objectToString("leftIndex", getLeftIndex()));
         buffer.append(objectToString("rightIndex", getRightIndex()));
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
        if (obj instanceof DatasetLevelItem) {
            DatasetLevelItem otherItem = (DatasetLevelItem)obj;

            if (!objectEqualsFK(this.getDataset(), otherItem.getDataset())) {
                return false;
            }
            if (!this.stringEqualsIgnoreCase(this.getLevelName(), otherItem.getLevelName())) {
                return false;
            }
            if (!stringEqualsIgnoreCase(this.getLevelTitle(), otherItem.getLevelTitle())) {
                return false;
            }
            if (!stringEqualsIgnoreCase(this.getDescription(), otherItem.getDescription())) {
                return false;
            }
            if (!objectEqualsFK(this.getParent(), otherItem.getParent())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getDataset());

        String name = this.getLevelName();
        if (name != null) { name = name.toUpperCase(); }
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(name);

        String title = this.getLevelTitle();
        if (title != null) { title = title.toUpperCase(); }
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(title);

        String desc = this.getDescription();
        if (desc != null) { desc = desc.toUpperCase(); }
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(desc);

        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(this.getParent());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Compares two objects using each attribute of this class except
     * the assigned id, if it has an assigned id.
     * <ul>
     * <li>dataset</li>
     * <li>title</li>
     * <li>name</li>
     * <li>description</li>
     * <li>parent</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    public int compareTo(Object obj) {
        DatasetLevelItem otherItem = (DatasetLevelItem)obj;

        int value = 0;

        value = objectCompareToFK(this.getDataset(), otherItem.getDataset());
        if (value != 0) { return value; }

        value = stringCompareToIgnoreCase(this.getLevelTitle(), otherItem.getLevelTitle());
        if (value != 0) { return value; }

        value = stringCompareToIgnoreCase(this.getLevelName(), otherItem.getLevelName());
        if (value != 0) { return value; }

        value = stringCompareToIgnoreCase(this.getDescription(), otherItem.getDescription());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getParent(), otherItem.getParent());
        if (value != 0) { return value; }

        return value;
    }
}