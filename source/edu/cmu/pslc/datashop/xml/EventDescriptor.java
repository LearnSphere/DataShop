/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.pslc.datashop.item.Item;

/**
 * Data object that contains all fields for the XML event_descriptor element.
 *
 * @author Hui cheng
 * @version $Revision: 3208 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2006-07-18 14:17:36 -0400 (Tue, 18 Jul 2006) $
 * <!-- $KeyWordsOff: $ -->
 */

public class EventDescriptor implements Comparable {
    /** The event_id. This is not stored in database and not used anywhere.*/
    private String eventId;
    /** Collection of selections associated with this event descriptor. */
    private Set selections;
    /** Collection of actions associated with this event descriptor. */
    private Set actions;
    /** Collection of inputs associated with this event descriptor. */
    private Set inputs;

    /** Default constructor. */
    public EventDescriptor() {
    }

    /** The constructor that sets all fields.
     *  @param eventId String.
     *  @param selections Set.
     *  @param actions Set.
     *  @param inputs Set.
     */
    public EventDescriptor(String eventId,
                            Set selections,
                            Set actions,
                            Set inputs) {
        this.eventId = eventId;
        this.selections = selections;
        this.actions = actions;
        this.inputs = inputs;
    }

    /** The getter for eventId.
     * @return The eventId.
     */
    public String getEventId () {
        return eventId;
    }

    /** The setter for eventId.
     * @param eventId String.
     */
    public void setEventId (String eventId) {
        this.eventId = eventId;
    }

    /**
     * Get selections.
     * @return java.util.Set
     */
    protected Set getSelections() {
        if (this.selections == null) {
            this.selections = new HashSet();
        }
        return this.selections;
    }

    /**
     * Public method to get Selections.
     * @return a list instead of a set
     */
    public List getSelectionsExternal() {
        List sortedList = new ArrayList(getSelections());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set selections.
     * @param selections Collection of selections associated with this event descriptor.
     */
    protected void setSelections(Set selections) {
        this.selections = selections;
    }


    /**
     * Add a SelectionItem or an attemptSelectionItem.
     * @param item to add
     */
    public void addSelection(Item item) {
        if (!getSelections().contains(item)) {
            getSelections().add(item);
        }
    }

    /**
     * Get actions.
     * @return java.util.Set
     */
    protected Set getActions() {
        if (this.actions == null) {
            this.actions = new HashSet();
        }
        return this.actions;
    }

    /**
     * Public method to get actions.
     * @return a list instead of a set
     */
    public List getActionsExternal() {
        List sortedList = new ArrayList(getActions());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set actions.
     * @param actions Collection of  actions associated with this event descriptor.
     */
    protected void setActions(Set actions) {
        this.actions = actions;
    }

    /**
     * Add a actionItem or an attemptActionItem.
     * @param item to add
     */
    public void addAction(Item item) {
        if (!getActions().contains(item)) {
            getActions().add(item);
        }
    }

    /**
     * Get inputs.
     * @return java.util.Set
     */
    protected Set getInputs() {
        if (this.inputs == null) {
            this.inputs = new HashSet();
        }
        return this.inputs;
    }

    /**
     * Public method to get inputs.
     * @return a list instead of a set
     */
    public List getInputsExternal() {
        List sortedList = new ArrayList(getInputs());
        Collections.sort(sortedList);
        return Collections.unmodifiableList(sortedList);
    }

    /**
     * Set inputs.
     * @param inputs Collection of inputs associated with this event descriptor.
     */
    protected void setInputs(Set inputs) {
        this.inputs = inputs;
    }

    /**
     * Add a InputItem or an attemptInputAction.
     * @param item to add
     */
    public void addInput(Item item) {
        if (!getInputs().contains(item)) {
            getInputs().add(item);
        }
    }

    /**
     * Compares two objects using each attribute of this class.
     * <ul>
     *   <li>id</li>
     * </ul>
     * @param obj the object to compare this to.
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
    */
    public int compareTo(Object obj) {
        EventDescriptor otherItem = (EventDescriptor)obj;
        if ((this.getEventId() != null) && (otherItem.getEventId() != null)) {
            return this.getEventId().compareTo(otherItem.getEventId());
        } else if (this.getEventId() != null) {
            return 1;
        } else if (otherItem.getEventId() != null) {
            return -1;
        }
        return 0;
    }

}