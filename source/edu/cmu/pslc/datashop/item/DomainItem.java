/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A single domain object that represents table Domain.
 *
 * @author Shanwen Yu
 * @version $Revision: 6930 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2011-06-10 11:46:10 -0400 (Fri, 10 Jun 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DomainItem extends Item implements java.io.Serializable, Comparable {

    /** Database generated unique Id for this domain. */
    private Integer id;
    /** Name of this domain. */
    private String name;
    /** Collection of learnlabs associated with this domain. */
    private Set<LearnlabItem> learnlabs;
    /**
     * Get the domain id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() {
        return this.id;
    }

    /**
     * Set domain Id.
     * @param domainId Database generated unique Id for this domain.
     */
    public void setId(Integer domainId) {
        this.id = domainId;
    }

    /**
     * Returns name.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get learnlabs.
     * @return a set of learnlabs associated with this domain
     */
    public Set getLearnlabs() {
        if (this.learnlabs == null) {
            this.learnlabs = new HashSet();
        }
        return this.learnlabs;
    }

    /**
     * Public method to get learnlabs.
     * @return a list instead of a set
     */
    public List getLearnlabsExternal() {
        List sortedItems = new ArrayList(getLearnlabs());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set learnlabs.
     * @param learnlabs Collection of learnlabs associated with this Domain.
     */
    protected void setLearnlabs(Set learnlabs) {
        this.learnlabs = learnlabs;
    }

    /**
     * Adds a Learnlab to this Learnlab list of Learnlabs.
     * @param item to add.
     */
    public void addLearnlab(LearnlabItem item) {
        if (!getLearnlabs().contains(item)) {
            getLearnlabs().add(item);
            item.addDomain(this);
        }
    }

    /**
     * Removes a learnlab.
     * @param item to remove.
     */
    public void removeLearnlab(LearnlabItem item) {
        if (getLearnlabs().contains(item)) {
            getLearnlabs().remove(item);
            item.removeDomain(this);
        }
    }


    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         return super.toString("domainId", getId(), "name", getName());
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
          if (obj instanceof DomainItem) {
              DomainItem otherItem = (DomainItem)obj;
            if (!Item.objectEquals(this.getName(), otherItem.getName())) {
                return false;
            }
            return true;
          }
        return false;
      }
      /**
       * Returns the hash code for this item.
       * @return int the hash code as an int.
       */
       public int hashCode() {
           return super.objectHashCode(getName());
       }
       /**
        * Compares two objects using each attribute of this class except
        * the assigned id, if it has an assigned id.
        * <ul>
        * <li>name</li>
        * </ul>
        * @param obj the object to compare this to.
        * @return the value 0 if equal; a value less than 0 if it is less than;
        * a value greater than 0 if it is greater than
        */
       public int compareTo(Object obj) {
           DomainItem otherItem = (DomainItem)obj;
           int value = 0;

           value = objectCompareTo(this.getName(), otherItem.getName());
           if (value != 0) { return value; }

           return value;
       }

}
