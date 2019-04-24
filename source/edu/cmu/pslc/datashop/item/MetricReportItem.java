/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2010
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.item;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.util.UtilConstants;

import static java.util.Arrays.asList;

/**
 * A metric report object represents Metric_Report table in database.
 *
 * @author Shanwen Yu
 * @version $Revision: 12671 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-10-08 09:36:42 -0400 (Thu, 08 Oct 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MetricReportItem extends Item implements java.io.Serializable, Comparable {

    /** Database generated unique Id for this report. */
    private Integer id;
    /** When this report is generated. */
    private Date time;
    /** Reference to the remote instance for this metric_report. */
    private RemoteInstanceItem remoteInstance;

    // The following are needed by the MetricsReportGenerator and MetricsSet WebService.
    /** Expected headers in the post data. */
    public static final List<String> HEADERS = asList("Category", "Files", "Papers", "Datasets",
                                                      "Actions", "Students", "Hours");
    /** Static final for Domain. */
    public static final String DOMAIN = "Domain";
    /** Static final for LearnLab. */
    public static final String LEARNLAB = "LearnLab";
    /** Static final for category separator. */
    public static final String SEP = ":";

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
     * Returns time.
     * @return time When the report is generated.
     */
    public Date getTime() {
        return time;
    }

    /**
     * Set time.
     * @param time When the report is generated.
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Get the RemoteInstance reference.
     * @return RemoteInstanceItem remoteInstance
     */
    public RemoteInstanceItem getRemoteInstance() { return this.remoteInstance; }

    /**
     * Set the RemoteInstance reference.
     * @param remoteInstance the reference
     */
    public void setRemoteInstance(RemoteInstanceItem remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
     public String toString() {
         return super.toString("metricReportId", getId(), "time", getTime(),
                               "RemoteInstance", getRemoteInstance());
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
          if (obj instanceof MetricReportItem) {
              MetricReportItem otherItem = (MetricReportItem)obj;

            if (!Item.objectEquals(this.getTime(), otherItem.getTime())) {
                return false;
            }
            if (!objectEqualsFK(this.getRemoteInstance(), otherItem.getRemoteInstance())) {
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
           hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTime());
           hash = hash * UtilConstants.HASH_PRIME + objectHashCodeFK(remoteInstance);
           return (int)(hash % Integer.MAX_VALUE);
       }

       /**
        * Compares two objects using each attribute of this class except
        * the assigned id, if it has an assigned id.
        * <ul>
        * <li>time</li>
        * </ul>
        * @param obj the object to compare this to.
        * @return the value 0 if equal; a value less than 0 if it is less than;
        * a value greater than 0 if it is greater than
        */
       public int compareTo(Object obj) {
           MetricReportItem otherItem = (MetricReportItem)obj;
           int value = 0;

           value = objectCompareTo(this.getTime(), otherItem.getTime());
           if (value != 0) { return value; }

           value = objectCompareToFK(this.getRemoteInstance(), otherItem.getRemoteInstance());
           if (value != 0) { return value; }

           return value;
       }
}
