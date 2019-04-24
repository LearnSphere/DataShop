/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.pslc.datashop.util.UtilConstants;
import static edu.cmu.pslc.datashop.util.CollectionUtils.set;

/**
 * Filter on a column.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11827 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-12-05 11:55:03 -0500 (Fri, 05 Dec 2014) $
 * <!-- $KeyWordsOff: $ -->
 */

public class FilterItem extends Item implements java.io.Serializable, Comparable  {

    /** The maximum length of the filter string is 65,535 characters (TEXT). */
    public static final int FILTER_STRING_MAX_LENGTH = 65535;

    public static final Map<String, String> FilterConditions;
    static {
        Map<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put("problemName", "Problem Name");
        conditionMap.put("problemDescription", "Problem Description");
        conditionMap.put("transactionTime", "Time Stamp");
        conditionMap.put("attemptAtSubgoal", "Attempt Number");
        conditionMap.put("conditionName", "Condition Name");
        conditionMap.put("type", "Condition Type");
        conditionMap.put("levelName", "Level Name");
        conditionMap.put("levelTitle", "Level Title");
        conditionMap.put("schoolName", "School Name");
        conditionMap.put("anonymousUserId", "Anon Student Id");
        conditionMap.put("transactionTypeTool", "Student Response Type");
        conditionMap.put("transactionTypeTutor", "Tutor Response Type");
        conditionMap.put("transactionSubtypeTool", "Student Response Subtype");
        conditionMap.put("transactionSubtypeTutor", "Tutor Response Subtype");
        FilterConditions = Collections.unmodifiableMap(conditionMap);
    }

    /** Database generated unique Id for this filter. */
    private Integer id;
    /** The sample this filter is associated with. */
    private SampleItem sample;
    /** Parent of this filter for complex operand building. */
    private FilterItem parent;
    /** Class this database is building a filter on. */
    private String clazz;
    /** Attribute that is being filtered on. */
    private String attribute;
    /** String of the filter. */
    private String filterString;
    /** The type of operator this filter is employing. */
    private String operator;
    /** Position of the attribute/column when using this filter as an export. */
    private Integer position;
    /** Collection of children for this filter. */
    private Set children;

    /** Default constructor. */
    public FilterItem() {
    }

    /**
     *  Constructor with id.
     *  @param filterId Database generated unique Id for this filter.
     */
    public FilterItem(Integer filterId) {
        this.id = filterId;
    }

    /**
     * Get filterId.
     * @return the Integer id as a comparable.
     */

    public Comparable getId() {
        return this.id;
    }

    /**
     * Set filterId.
     * @param filterId Database generated unique Id for this filter.
     */
    public void setId(Integer filterId) {
        this.id = filterId;
    }
    /**
     * Get sample.
     * @return edu.cmu.pslc.datashop.item.SampleItem
     */

    public SampleItem getSample() {
        return this.sample;
    }

    /**
     * Set sample.
     * @param sample The sample this filter is associated with.
     */
    public void setSample(SampleItem sample) {
        this.sample = sample;
    }

    /**
     * Get parent.
     * @return edu.cmu.pslc.datashop.item.FilterItem
     */

    public FilterItem getParent() {
        return this.parent;
    }

    /**
     * Set parent.
     * @param parent Parent of this filter for complex operand building.
     */
    public void setParent(FilterItem parent) {
        this.parent = parent;
    }
    /**
     * Get class.
     * @return java.lang.String
     */

    public String getClazz() {
        return this.clazz;
    }

    /**
     * Set class.
     * @param clazz Class this database is building a filter on.
     */
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
    /**
     * Get attribute.
     * @return java.lang.String
     */

    public String getAttribute() {
        return this.attribute;
    }

    /**
     * Set attribute.
     * @param attribute Attribute that is being filtered on.
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
    /**
     * Get filterString.
     * @return java.lang.String
     */

    public String getFilterString() {
        return this.filterString;
    }

    /**
     * True if there is no filter string.
     * @return True if there is no filter string.
     */
    public boolean isEmptyFilterString() {
        return getFilterString() == null || getFilterString().equals("");
    }

    /**
     * Escape back slashes and single quotes.
     * @return the escaped filter string.
     */
    public String escapedFilterString() {
        return getFilterString().replace("\\", "\\\\");
    }

    /**
     * Set filterString.
     * @param filterString String of the filter.
     */
    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }
    /**
     * Get operator.
     * @return java.lang.String
     */

    public String getOperator() {
        return this.operator;
    }

    /** The "negation" operators. */
    private static final Set<String> NOT_OPERATORS = set("!=", "NOT IN", "NOT LIKE");
    /**
     * Whether the operator is a "negation" operator.
     * @return Whether the operator is a "negation" operator.
     */
    public boolean isNotOperator() {
        return NOT_OPERATORS.contains(getOperator());
    }

    /** The "null" operators. */
    private static final Set<String> NULL_OPERATORS = set("IS NULL", "IS NOT NULL");

    /**
     * Whether the operator is a "null" operator.
     * @return Whether the operator is a "null" operator.
     */
    public boolean isNullOperator() {
        return NULL_OPERATORS.contains(getOperator());
    }

    /**
     * "Positive" equivalent of this operator, if operator is negative.
     * Just the operator, otherwise.
     * @return the "negated" version of this operator
     */
    public String negatedOperator() {
        if (getOperator().equals("!=")) {
            return "=";
        }
        if (getOperator().equalsIgnoreCase("not like")) {
            return "LIKE";
        }
        if (getOperator().equalsIgnoreCase("not in")) {
            return "IN";
        }
        return getOperator();
    }

    /**
     * We need to perform an outer join if this is a not operator, a null operator,
     * or the filter string is empty.
     * @return whether or not we need to perform an outer join
     */
    public boolean isOuterJoin() {
        return isNotOperator() || isNullOperator() || isEmptyFilterString();
    }

    /**
     * Set operator.
     * @param operator The type of operator this filter is employing.
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * Get position.
     * @return java.lang.Integer
     */
    public Integer getPosition() {
        return this.position;
    }

    /**
     * Set position.
     * @param position Position of the attribute/column when using this filter as an export.
     */
    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Get children.
     * @return java.util.Set
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
     * @param children Collection of children filters for this filter.
     */
    protected void setChildren(Set children) {
        this.children = children;
    }

    /**
     * Add a child.
     * @param item to add
     */
    public void addChild(FilterItem item) {
        if (!getChildren().contains(item)) {
            getChildren().add(item);
            item.setParent(this);
        }
    }

    /**
     * remove a child.
     * @param item to remove
     */
    public void removeChild(FilterItem item) {
        if (getChildren().contains(item)) {
            getChildren().remove(item);
            item.setParent(null);
        }
    }

    /**
     * Format is:
     *  abbr.hql op
     * for a "null" operator,
     *  abbr.hql op filterString
     * otherwise.
     * Append " or abbr.hql is null" for non-null operators.
     * @param abbrev table abbreviation
     * @param hql HQL code value
     * @return the filter clause for abbrev and hql
     */
    public String getClause(String abbrev, String hql) {
        String abbrevAndHQL = abbrev + "." + hql;
        StringBuffer clause = new StringBuffer();

        if (isNotOperator()) {
            clause.append("(");
        }
        clause.append(abbrevAndHQL + " " + getOperator());
        if (!isNullOperator()) {
            clause.append(" " + escapedFilterString());
            if (isNotOperator()) {
                clause.append(" or " + abbrevAndHQL + " is null");
            }
        }
        if (isNotOperator()) {
            clause.append(")");
        }

        return clause.toString();
    }

    /**
     * Whether this filter's attribute equals "attribute".
     * @param attribute a filter attribute value
     * @return whether "attribute" is the attribute for this filter
     */
    public boolean checkAttribute(String attribute) {
        return getAttribute().equals(attribute);
    }

    /**
     * Whether this filter's class equals "class".
     * @param clazz a filter class value
     * @return whether "class" is the class for this filter
     */
    public boolean checkClass(String clazz) {
        return getClazz().equals(clazz);
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
         buffer.append(objectToString("filterId", getId()));
         buffer.append(objectToStringFK("parentId", getParent()));
         buffer.append(objectToString("class", getClazz()));
         buffer.append(objectToString("attribute", getAttribute()));
         buffer.append(objectToString("filterString", getFilterString()));
         buffer.append(objectToString("operator", getOperator()));
         buffer.append(objectToString("position", getPosition()));
         buffer.append(objectToStringFK("sampleId", getSample()));
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
        if (obj instanceof FilterItem) {
            FilterItem otherItem = (FilterItem)obj;

            if (!objectEquals(getAttribute(), otherItem.getAttribute())) {
                return false;
            }
            if (!objectEquals(this.getClazz(), otherItem.getClazz())) {
                return false;
            }
            if (!objectEquals(getFilterString(), otherItem.getFilterString())) {
                return false;
            }
            if (!objectEquals(getOperator(), otherItem.getOperator())) {
                return false;
            }
            if (!objectEqualsFK(getParent(), otherItem.getParent())) {
                return false;
            }
            if (!objectEquals(getPosition(), otherItem.getPosition())) {
                return false;
            }
            if (!objectEqualsFK(getSample(), otherItem.getSample())) {
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
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getClazz());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAttribute());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getFilterString());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getOperator());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getPosition());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getSample());
        hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(getParent());
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
    * Compares two objects using each attribute of this class except
    * the assigned id, if it has an assigned id.
    * <ul>
    *   <li>Sample</li>
    *   <li>Parent</li>
    *   <li>Position</li>
    *   <li>Class</li>
    *   <li>Attribute</li>
    *   <li>Operator</li>
    *   <li>Filter String</li>
    * </ul>
    * @param obj the object to compare this to.
    * @return the value 0 if equal; a value less than 0 if it is less than;
    * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        FilterItem otherItem = (FilterItem)obj;
        int value = 0;

        value = objectCompareToFK(this.getSample(), otherItem.getSample());
        if (value != 0) { return value; }

        value = objectCompareToFK(this.getParent(), otherItem.getParent());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getPosition(), otherItem.getPosition());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getClazz(), otherItem.getClazz());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getAttribute(), otherItem.getAttribute());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getOperator(), otherItem.getOperator());
        if (value != 0) { return value; }

        value = objectCompareTo(this.getFilterString(), otherItem.getFilterString());
        if (value != 0) { return value; }

        return value;
    }
}