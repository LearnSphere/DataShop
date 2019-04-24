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
 * @version $Revision: 15913 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2019-03-14 14:35:21 -0400 (Thu, 14 Mar 2019) $
 * <!-- $KeyWordsOff: $ -->
 */
public class WorkflowComponentInstancePersistenceItem extends Item implements java.io.Serializable, Comparable {


    /** Collection of all allowed items in the level enumeration. */
    public static final List COMPONENT_STATE_ENUM = new ArrayList();

    /** component state. */
    public static final String WF_STATE_NEW = "new";
    /** component state. */
    public static final String WF_STATE_RUNNING = "running";
    /** component state. */
    public static final String WF_STATE_RUNNING_DIRTY = "running_dirty";
    /** component state. */
    public static final String WF_STATE_ERROR = "error";
    /** component state. */
    public static final String DO_NOT_RUN = "do_not_run";
    /** component state. */
    public static final String COMPLETED = "completed";
    /** component state. */
    public static final String COMPLETED_WARN = "completed_warn";
    /**
     * Adds each message type to the enumerated list.
     */
    static {
        COMPONENT_STATE_ENUM.add(WF_STATE_NEW);
        COMPONENT_STATE_ENUM.add(WF_STATE_RUNNING);
        COMPONENT_STATE_ENUM.add(WF_STATE_RUNNING_DIRTY);
        COMPONENT_STATE_ENUM.add(WF_STATE_ERROR);
        COMPONENT_STATE_ENUM.add(DO_NOT_RUN);
        COMPONENT_STATE_ENUM.add(COMPLETED);
        COMPONENT_STATE_ENUM.add(COMPLETED_WARN);
    }

    /** Database generated unique Id for this workflow history item. */
    private Integer id;
    /** The workflow. */
    private WorkflowItem workflow;
    /** The component name. */
    private String componentName;
    /** The workflow component's dirty bit. */
    private Boolean dirtyFile;
    /** The workflow component's dirty bit. */
    private Boolean dirtyOption;
    /** The workflow component's dirty bit. */
    private Boolean dirtySelection;
    /** The workflow component's dirty bit. */
    private Boolean dirtyAddConnection;
    /** The workflow component's dirty bit. */
    private Boolean dirtyDeleteConnection;
    /** The workflow component's dirty bit. */
    private Boolean dirtyAncestor;
    /** The run state from WorkflowComponentInstanceItem. */
    private String state;
    /** The component's level in the directed graph. The level 0 is used for the start node(s), i.e. import components.
    The variable is mainly used to determine execution order. All components in the same level can be executed in any order. */
    private Integer depthLevel;
    /** The component errors. */
    private String errors;
    /** The component warnings. */
    private String warnings;

    public WorkflowComponentInstancePersistenceItem() {
        dirtyFile = false;
        dirtyOption = false;
        dirtySelection = false;
        dirtyAddConnection = false;
        dirtyDeleteConnection = false;
        dirtyAncestor = false;
        state = "ready";
        depthLevel = 0;
        errors = null;
        warnings = null;
    }

    /**
     * @return the workflow
     */
    public WorkflowItem getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(WorkflowItem workflow) {
        this.workflow = workflow;
    }

    /**
     * @return the dirtyAddConnection
     */
    public Boolean getDirtyAddConnection() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = dirtyAddConnection;
        }
        return dirty;
    }

    /**
     * @param dirtyAddConnection the dirtyAddConnection to set
     */
    public void setDirtyAddConnection(Boolean dirtyAddConnection) {
        this.dirtyAddConnection = dirtyAddConnection;
    }

    /**
     * @return the dirtyDeleteConnection
     */
    public Boolean getDirtyDeleteConnection() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = dirtyDeleteConnection;
        }
        return dirty;
    }

    /**
     * @param dirtyDeleteConnection the dirtyDeleteConnection to set
     */
    public void setDirtyDeleteConnection(Boolean dirtyDeleteConnection) {
        this.dirtyDeleteConnection = dirtyDeleteConnection;
    }

    /**
     * @return the dirtyFile
     */
    public Boolean getDirtyFile() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = dirtyFile;
        }
        return dirty;
    }

    /**
     * @param dirtyFile the dirtyFile to set
     */
    public void setDirtyFile(Boolean dirtyFile) {
        this.dirtyFile = dirtyFile;
    }

    /**
     * @return the dirtyOption
     */
    public Boolean getDirtyOption() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = dirtyOption;
        }
        return dirty;
    }

    /**
     * @param dirtyOption the dirtyOption to set
     */
    public void setDirtyOption(Boolean dirtyOption) {
        this.dirtyOption = dirtyOption;
    }


    /**
     * @return the dirtySelection
     */
    public Boolean getDirtySelection() {
        return dirtySelection;
    }

    /**
     * @param dirtySelection the dirtySelection to set
     */
    public void setDirtySelection(Boolean dirtySelection) {
        this.dirtySelection = dirtySelection;
    }

    /**
     * @return the dirtyAncestor
     */
    public Boolean getDirtyAncestor() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = dirtyAncestor;
        }
        return dirty;
    }

    /**
     * @param dirtyAncestor the dirtyAncestor to set
     */
    public void setDirtyAncestor(Boolean dirtyAncestor) {
        this.dirtyAncestor = dirtyAncestor;
    }

    /**
     * @return the depthLevel
     */
    public Integer getDepthLevel() {
        return depthLevel;
    }

    /**
     * @param depthLevel the depthLevel to set
     */
    public void setDepthLevel(Integer depthLevel) {
        this.depthLevel = depthLevel;
    }

    /**
     * @return the errors
     */
    public String getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(String errors) {
        this.errors = errors;
    }

    /**
     * @return the warnings
     */
    public String getWarnings() {
        return warnings;
    }

    /**
     * @param warnings the warnings to set
     */
    public void setWarnings(String warnings) {
        this.warnings = warnings;
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
    /** Get the state.
     * @return the state
     */
    public String getState() {
        return state;
    }

    /** Set the state.
     * @param state the state to set
     */
    public void setState(String state) {
        if (isValidComponentState(state)) {
            this.state = state;
        } else if (state == null) {
            state = WF_STATE_NEW;
        } else {
            throw new LogException("State type can only be "
                + getStateTypes() + " and not : " + state);
        }
        this.state = state;
    }

    /**
     * Check for valid state.
     * @param state the component state
     * @return true if state is valid; false otherwise
     */
    public static boolean isValidComponentState(String state) {
        if (COMPONENT_STATE_ENUM.contains(state)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the bitwise-and of all dirty flags.
     * @return the bitwise-and of all dirty flags
     */
    public Boolean isDirty() {
        Boolean dirty = false;
        if (!checkCompleted()) {
            dirty = this.getDirtyAddConnection() || this.getDirtyAncestor() || this.getDirtyDeleteConnection()
                    || this.getDirtyFile() || this.getDirtyOption() || this.getDirtySelection();
        }

        return dirty;
    }

    /**
     * Return a list of the valid state types.
     * @return an unmodifiable list of the valid state types
     */
    public static List getStateTypes() {
        return Collections.unmodifiableList(COMPONENT_STATE_ENUM);
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
         buffer.append(objectToString("workflow", getWorkflow()));
         buffer.append(objectToString("componentName", getComponentName()));
         buffer.append(objectToString("dirtyFile", getDirtyFile()));
         buffer.append(objectToString("dirtyOption", getDirtyOption()));
         buffer.append(objectToString("dirtySelection", getDirtySelection()));
         buffer.append(objectToString("dirtyAddConnection", getDirtyAddConnection()));
         buffer.append(objectToString("dirtyDeleteConnection", getDirtyDeleteConnection()));
         buffer.append(objectToString("dirtyAncestor", getDirtyAncestor()));
         buffer.append(objectToString("state", getState()));
         buffer.append(objectToString("depthLevel", getDepthLevel()));
         buffer.append(objectToString("errors", getErrors()));
         buffer.append(objectToString("warnings", getWarnings()));

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
        if (obj instanceof WorkflowComponentInstancePersistenceItem) {
            WorkflowComponentInstancePersistenceItem otherItem = (WorkflowComponentInstancePersistenceItem)obj;

            if (!Item.objectEquals(this.getWorkflow(), otherItem.getWorkflow())) {
                return false;
            }

            if (!Item.objectEquals(this.getComponentName(), otherItem.getComponentName())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtyFile(), otherItem.getDirtyFile())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtyOption(), otherItem.getDirtyOption())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtySelection(), otherItem.getDirtySelection())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtyAddConnection(), otherItem.getDirtyAddConnection())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtyDeleteConnection(), otherItem.getDirtyDeleteConnection())) {
                return false;
            }
            if (!Item.objectEquals(this.getDirtyAncestor(), otherItem.getDirtyAncestor())) {
                return false;
            }
            if (!Item.objectEquals(this.getState(), otherItem.getState())) {
                return false;
            }
            if (!Item.objectEquals(this.getDepthLevel(), otherItem.getDepthLevel())) {
                return false;
            }
            if (!Item.objectEquals(this.getErrors(), otherItem.getErrors())) {
                return false;
            }
            if (!Item.objectEquals(this.getWarnings(), otherItem.getWarnings())) {
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
         hash = hash * UtilConstants.HASH_PRIME + this.objectHashCode(getWorkflow());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getComponentName());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtyFile());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtyOption());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtySelection());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtyAddConnection());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtyDeleteConnection());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDirtyAncestor());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getState());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getDepthLevel());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getErrors());
         hash = hash * UtilConstants.HASH_PRIME + objectHashCode(getWarnings());
         return (int)(hash % Integer.MAX_VALUE);
     }

     /**
      * Compares two objects using each attribute of this class except
      * the assigned id, if it has an assigned id.
      * <ul>
      *   <li>Workflow</li>
      *   <li>ComponentName</li>
      *   <li>DirtyFile</li>
      *   <li>DirtyOption</li>
      *   <li>DirtySelection</li>
      *   <li>DirtyAddConnection</li>
      *   <li>DirtyDeleteConnection</li>
      *   <li>DirtyAncestor</li>
      *   <li>State</li>
      *   <li>DepthLevel</li>
      *   <li>Errors</li>
      *   <li>Warnings</li>
      * </ul>
      * @param obj the object to compare this to.
      * @return the value 0 if equal; a value less than 0 if it is less than;
      * a value greater than 0 if it is greater than
     */
     public int compareTo(Object obj) {
         WorkflowComponentInstancePersistenceItem otherItem = (WorkflowComponentInstancePersistenceItem)obj;

         int value = 0;

         value = objectCompareTo(this.getWorkflow(), otherItem.getWorkflow());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getComponentName(), otherItem.getComponentName());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtyFile(), otherItem.getDirtyFile());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtyOption(), otherItem.getDirtyOption());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtySelection(), otherItem.getDirtySelection());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtyAddConnection(), otherItem.getDirtyAddConnection());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtyDeleteConnection(), otherItem.getDirtyDeleteConnection());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDirtyAncestor(), otherItem.getDirtyAncestor());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getState(), otherItem.getState());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getDepthLevel(), otherItem.getDepthLevel());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getErrors(), otherItem.getErrors());
         if (value != 0) { return value; }

         value = objectCompareTo(this.getWarnings(), otherItem.getWarnings());
         if (value != 0) { return value; }

         return value;
     }


     private Boolean checkCompleted() {
         if (this.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED)
                 || this.getState().equalsIgnoreCase(WorkflowComponentInstanceItem.COMPLETED_WARN)) {
             return true;
         }
         return false;
     }


}
