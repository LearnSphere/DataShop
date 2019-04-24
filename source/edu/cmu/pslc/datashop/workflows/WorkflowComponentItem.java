package edu.cmu.pslc.datashop.workflows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.pslc.datashop.item.Item;
import edu.cmu.pslc.datashop.util.LogException;
import edu.cmu.pslc.datashop.util.UtilConstants;


/**
 * The workflow component item.
 * @author Mike Komisin
 * @version $Revision:  $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date:  $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentItem extends Item implements java.io.Serializable, Comparable {


    /** Database generated unique Id for this workflow history item. */
    private Long id;
    /** The component type. */
    private String componentType;
    /** The component name. */
    private String componentName;
    /** The tool directory. */
    private String toolDir;
    /** The schema path. */
    private String schemaPath;
    /** The interpreter path. */
    private String interpreterPath;
    /** The tool path. */
    private String toolPath;
    /** The enabled flag. */
    private Boolean enabled;
    /** The remote execution enabled flag. */
    private Boolean remoteExecEnabled;
    /** The author's user id. */
    private String author;
    /** The citation, intended for a DOI link. */
    private String citation;
    /** The tool version. */
    private String version;
    /** The component info, i.e. help, meta-info, etc. */
    private String info;

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
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the componentType
     */
    public String getComponentType() {
        return componentType;
    }

    /**
     * @param componentType the componentType to set
     */
    public void setComponentType(String componentType) {
        this.componentType = componentType;
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
     * @return the schemaPath
     */
    public String getSchemaPath() {
        return schemaPath;
    }

    /**
     * @param schemaPath the schemaPath to set
     */
    public void setSchemaPath(String schemaPath) {
        if (schemaPath != null) {
            this.schemaPath = schemaPath.replaceAll("\\\\", "/");
        } else {
            this.schemaPath = null;
        }
    }


    /**
     * @return the toolDir
     */
    public String getToolDir() {
        return toolDir;
    }

    /**
     * @param toolDir the toolDir to set
     */
    public void setToolDir(String toolDir) {
        if (toolDir != null) {
            this.toolDir = toolDir.replaceAll("\\\\", "/");
        } else {
            this.toolDir = null;
        }
    }

    /**
     * @return the interpreterPath
     */
    public String getInterpreterPath() {
        return interpreterPath;
    }

    /**
     * @param interpreterPath the interpreterPath to set
     */
    public void setInterpreterPath(String interpreterPath) {
        if (interpreterPath != null) {
            this.interpreterPath = interpreterPath.replaceAll("\\\\", "/");
        } else {
            this.interpreterPath = null;
        }
    }

    /**
     * @return the toolPath
     */
    public String getToolPath() {
        return toolPath;
    }

    /**
     * @param toolPath the toolPath to set
     */
    public void setToolPath(String toolPath) {
        if (toolPath != null) {
            this.toolPath = toolPath.replaceAll("\\\\", "/");
        } else {
            this.toolPath = null;
        }
    }

    /**
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the remoteExecEnabled
     */
    public Boolean getRemoteExecEnabled() {
        return remoteExecEnabled;
    }

    /**
     * @param remoteExecEnabled the remoteExecEnabled to set
     */
    public void setRemoteExecEnabled(Boolean remoteExecEnabled) {
        this.remoteExecEnabled = remoteExecEnabled;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the citation
     */
    public String getCitation() {
        return citation;
    }

    /**
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /** Get the info.
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /** Set the info.
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
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
         buffer.append(objectToString("componentType", getComponentType()));
         buffer.append(objectToString("componentName", getComponentName()));
         buffer.append(objectToString("toolDir", getToolDir()));
         buffer.append(objectToString("schemaPath", getSchemaPath()));
         buffer.append(objectToString("interpreterPath", getInterpreterPath()));
         buffer.append(objectToString("toolPath", getToolPath()));
         buffer.append(objectToString("enabled", getEnabled()));
         buffer.append(objectToString("remoteExecEnabled", getRemoteExecEnabled()));
         buffer.append(objectToString("author", getAuthor()));
         buffer.append(objectToString("citation", getCitation()));
         buffer.append(objectToString("version", getVersion()));
         buffer.append(objectToString("info", getInfo()));

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
        if (obj instanceof WorkflowComponentItem) {
            WorkflowComponentItem otherItem = (WorkflowComponentItem)obj;


            if (!Item.objectEquals(this.getComponentType(), otherItem.getComponentType())) {
                return false;
            }
            if (!Item.objectEquals(this.getComponentName(), otherItem.getComponentName())) {
                return false;
            }
            if (!Item.objectEquals(this.getToolDir(), otherItem.getToolDir())) {
                return false;
            }
            if (!Item.objectEquals(this.getSchemaPath(), otherItem.getSchemaPath())) {
                return false;
            }
            if (!Item.objectEquals(this.getInterpreterPath(), otherItem.getInterpreterPath())) {
                return false;
            }
            if (!Item.objectEquals(this.getToolPath(), otherItem.getToolPath())) {
                return false;
            }
            if (!Item.objectEquals(this.getEnabled(), otherItem.getEnabled())) {
                return false;
            }

            if (!Item.objectEquals(this.getRemoteExecEnabled(), otherItem.getRemoteExecEnabled())) {
                return false;
            }
            if (!Item.objectEquals(this.getAuthor(), otherItem.getAuthor())) {
                return false;
            }
            if (!Item.objectEquals(this.getCitation(), otherItem.getCitation())) {
                return false;
            }
            if (!Item.objectEquals(this.getVersion(), otherItem.getVersion())) {
                return false;
            }
            if (!Item.objectEquals(this.getInfo(), otherItem.getInfo())) {
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
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentType());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentName());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getToolDir());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getSchemaPath());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInterpreterPath());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getToolPath());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getEnabled());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getRemoteExecEnabled());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getAuthor());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getCitation());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getVersion());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getInfo());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>ComponentType</li>
      *   <li>ComponentName</li>
      *   <li>ToolDir</li>
      *   <li>SchemaPath</li>
      *   <li>InterpreterPath</li>
      *   <li>ToolPath</li>
      *   <li>Enabled</li>
      *   <li>RemoteExecEnabled</li>
      *   <li>Author</li>
      *   <li>Citation</li>
      *   <li>Version</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WorkflowComponentItem otherItem = (WorkflowComponentItem)obj;

         int value = 0;

         value = objectCompareTo(this.getComponentType(), otherItem.getComponentType());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getComponentName(), otherItem.getComponentName());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getToolDir(), otherItem.getToolDir());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getSchemaPath(), otherItem.getSchemaPath());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getInterpreterPath(), otherItem.getInterpreterPath());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getToolPath(), otherItem.getToolPath());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getEnabled(), otherItem.getEnabled());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getRemoteExecEnabled(), otherItem.getRemoteExecEnabled());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getAuthor(), otherItem.getAuthor());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getCitation(), otherItem.getCitation());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getVersion(), otherItem.getVersion());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getInfo(), otherItem.getInfo());
         if (value != 0) { return value; }

         return value;
     }

}
