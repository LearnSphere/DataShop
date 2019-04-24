package edu.cmu.pslc.datashop.workflows;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.UtilConstants;


/**
 * The workflow error translation item.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowErrorTranslationItem extends Item implements java.io.Serializable, Comparable {


    /** Database generated unique Id for this workflow history item. */
    private Integer id;

    /** The component name. */
    private String componentName;

    /** The unique error signature. */
    private String signature;
    /** The translation. */
    private String translation;
    /** The regular expression used to translate the error message. */
    private String regexp;
    /** Whether to prepend to ]the error message or replace it. */
    private Boolean replaceFlag;

    public WorkflowErrorTranslationItem() {
        componentName = null;
    }

    /**
     * Get id.
     * @return the id
     */
    public Comparable getId() {
        return id;
    }

    /**
     * Set the id.
     * @param id Database generated unique Id for this WorkflowHistoryItem.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the componentName
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return the regexp
     */
    public String getRegexp() {
        return regexp;
    }

    /**
     * @param regexp the regexp to set
     */
    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    /**
     * @return the translation
     */
    public String getTranslation() {
        return translation;
    }

    /**
     * @param translation the translation to set
     */
    public void setTranslation(String translation) {
        this.translation = translation;
    }

    /**
     * @return the replaceFlag
     */
    public Boolean getReplaceFlag() {
        return replaceFlag;
    }

    /**
     * @param replaceFlag the replaceFlag to set
     */
    public void setReplaceFlag(Boolean replaceFlag) {
        this.replaceFlag = replaceFlag;
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
         buffer.append(objectToString("id", getId()));
         buffer.append(objectToString("componentName", getComponentName()));
         buffer.append(objectToString("signature", getSignature()));
         buffer.append(objectToString("translation", getTranslation()));
         buffer.append(objectToString("regexp", getRegexp()));
         buffer.append(objectToString("replaceFlag", getReplaceFlag()));

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
        if (obj instanceof WorkflowErrorTranslationItem) {
            WorkflowErrorTranslationItem otherItem = (WorkflowErrorTranslationItem)obj;

            if (!Item.objectEquals(this.getComponentName(), otherItem.getComponentName())) {
                return false;
            }
            if (!Item.objectEquals(this.getSignature(), otherItem.getSignature())) {
                return false;
            }
            if (!Item.objectEquals(this.getTranslation(), otherItem.getTranslation())) {
                return false;
            }
            if (!Item.objectEquals(this.getReplaceFlag(), otherItem.getReplaceFlag())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentName());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSignature());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getTranslation());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRegexp());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getReplaceFlag());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>ComponentName</li>
      *   <li>Signature</li>
      *   <li>Translation</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WorkflowErrorTranslationItem otherItem = (WorkflowErrorTranslationItem)obj;

         int value = 0;

         value = objectCompareTo(this.getComponentName(), otherItem.getComponentName());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getSignature(), otherItem.getSignature());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getTranslation(), otherItem.getTranslation());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getRegexp(), otherItem.getRegexp());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getReplaceFlag(), otherItem.getReplaceFlag());
         if (value != 0) { return value; }

         return value;
     }



}
