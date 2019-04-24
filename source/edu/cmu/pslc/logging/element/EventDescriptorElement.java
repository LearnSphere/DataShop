/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.logging.element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * EventDescriptorElement.
 *
 * @author Alida Skogsholm
 * @version $Revision: 8633 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-02-01 10:09:57 -0500 (Fri, 01 Feb 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class EventDescriptorElement {

    /** Class attribute. */
    private List selectionList = new ArrayList();
    /** Class attribute. */
    private List actionList = new ArrayList();
    /** Class attribute. */
    private List inputList = new ArrayList();

    /**
     * Constructor.
     */
    public EventDescriptorElement() { }

    /**
     * Add selection.
     * @param selection selection
     */
    public void addSelection(String selection) {
        selectionList.add(new SelectionElement(selection));
    }

    /**
     * Add selection.
     * @param selection selection
     * @param type type
     */
    public void addSelection(String selection, String type) {
        selectionList.add(new SelectionElement(selection, type, null));
    }
    /**
     * Add selection.
     * @param selection selection
     * @param type type
     * @param id id
     */
    public void addSelection(String selection, String type, String id) {
        selectionList.add(new SelectionElement(selection, type, id));
    }
    /**
     * Add selection.
     * @param selectionElement selectionElement
     */
    public void addSelection(SelectionElement selectionElement) {
        selectionList.add(selectionElement);
    }

    /**
     * Add action.
     * @param action action
     */
    public void addAction(String action) {
        actionList.add(new ActionElement(action, null, null));
    }
    /**
     * Add action.
     * @param action action
     * @param type type
     * @param id id
     */
    public void addAction(String action, String type, String id) {
        actionList.add(new ActionElement(action, type, id));
    }
    /**
     * Add action.
     * @param actionElement actionElement
     */
    public void addAction(ActionElement actionElement) {
        actionList.add(actionElement);
    }

    /**
     * Add input.
     * @param input input
     */
    public void addInput(String input) {
        inputList.add(new InputElement(input, null, null));
    }
    /**
     * Add input.
     * @param input input
     * @param type type
     * @param id id
     */
    public void addInput(String input, String type, String id) {
        inputList.add(new InputElement(input, type, id));
    }
    /**
     * Add input.
     * @param inputElement inputElement
     */
    public void addInput(InputElement inputElement) {
        inputList.add(inputElement);
    }

    /**
     * Returns the xml version of the object.
     * @return xml version of the object
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("\t<event_descriptor>\n");

        for (Iterator iter = selectionList.iterator(); iter.hasNext();) {
            SelectionElement selectionElement = (SelectionElement)iter.next();
            if (selectionElement != null) { buffer.append(selectionElement.toString()); }
        }

        for (Iterator iter = actionList.iterator(); iter.hasNext();) {
            ActionElement actionElement = (ActionElement)iter.next();
            if (actionElement != null) { buffer.append(actionElement.toString()); }
        }

        for (Iterator iter = inputList.iterator(); iter.hasNext();) {
            InputElement inputElement = (InputElement)iter.next();
            if (inputElement != null) { buffer.append(inputElement.toString()); }
        }

        buffer.append("\t</event_descriptor>");
        buffer.append("\n");

        return buffer.toString();
    }
}
