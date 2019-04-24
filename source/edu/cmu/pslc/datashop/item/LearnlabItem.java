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

import edu.cmu.pslc.datashop.util.UtilConstants;

/**
 * A single learnlab object represents Learnlab table in database.
 *
 * @author Shanwen Yu
 * @version $Revision: 6101 $
 * <BR>Last modified by: $Author: shanwen $
 * <BR>Last modified on: $Date: 2010-05-21 15:42:54 -0400 (Fri, 21 May 2010) $
 * <!-- $KeyWordsOff: $ -->
 */
public class LearnlabItem extends Item implements java.io.Serializable, Comparable {

    /** Database generated unique Id for this learnlab. */
    private Integer id;
    /** Name of this learnlab. */
    private String name;
    /** Collection of domains associated with this learnlab. */
    private Set domains;


    /**
     * Get the learnlab id as a Comparable.
     * @return The Integer id as a Comparable.
     */
    public Comparable getId() { return this.id; }

    /**
     * Set learnlab Id.
     * @param learnlabId Database generated unique Id for this learnlab.
     */
    public void setId(Integer learnlabId) { this.id = learnlabId; }

    /**
     * Returns name.
     * @return name of the learnlab.
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
     * Get domains.
     * @return java.util.Set a set of domains associated with this learnlab
     */
    public Set getDomains() {
        if (this.domains == null) {
            this.domains = new HashSet();
        }
        return this.domains;
    }

    /**
     * Public method to get domains.
     * @return a list instead of a set
     */
    public List getDomainsExternal() {
        List sortedItems = new ArrayList(getDomains());
        Collections.sort(sortedItems);
        return Collections.unmodifiableList(sortedItems);
    }

    /**
     * Set domains.
     * @param domains Collection of domains associated with this Learnlab.
     */
    protected void setDomains(Set domains) {
        this.domains = domains;
    }

    /**
     * Adds a Domain to this Learnlab list of Domains.
     * @param item to add.
     */
    public void addDomain(DomainItem item) {
        if (!getDomains().contains(item)) {
            getDomains().add(item);
            item.addLearnlab(this);
        }
    }

    /**
     * Removes a domain.
     * @param item to remove.
     */
    public void removeDomain(DomainItem item) {
        if (getDomains().contains(item)) {
            getDomains().remove(item);
            item.removeLearnlab(this);
        }
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         return super.toString("learnlabId", getId(), "name", getName());
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
          if (obj instanceof LearnlabItem) {
              LearnlabItem otherItem = (LearnlabItem)obj;

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
           long hash = UtilConstants.HASH_INITIAL;
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getName());
           return (int)(hash % Integer.MAX_VALUE);
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
           LearnlabItem otherItem = (LearnlabItem)obj;
           int value = 0;

           value = objectCompareTo(this.getName(), otherItem.getName());
           if (value != 0) { return value; }

           return value;
       }
}
