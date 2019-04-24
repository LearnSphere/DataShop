/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.xml;

/**
 * Data object that contains all fields for the XML semantic_event element.
 *
 * @author Hui cheng
 * @version $Revision: 4899 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-06-02 14:16:28 -0400 (Mon, 02 Jun 2008) $
 * <!-- $KeyWordsOff: $ -->
 */

public class UiEvent {
    /** The id. */
    private String id;
    /** The name. */
    private String name;
    /** The contents. */
    private String contents;

    /** Default constructor. */
    public UiEvent() {
    }

    /** The constructor that sets all fields.
     *  @param id String.
     *  @param name String.
     *  @param contents String.
     */
    public UiEvent(String id, String name, String contents) {
        this.id = id;
        this.name = name;
        this.contents = contents;
    }

    /** The getter for id.
     * @return id String.
     */
    public String getId () {
        return id;
    }

    /** The setter for id.
     * @param id String.
     */
    public void setId (String id) {
        this.id = id;
    }

    /** The getter for name.
     * @return name String.
     */
    public String getName () {
        return name;
    }

    /** The setter for name.
     * @param name String.
     */
    public void setName (String name) {
        this.name = name;
    }

    /** The getter for contents.
     * @return contents String.
     */
    public String getContents () {
        return contents;
    }

    /** The setter for contents.
     * @param contents String.
     */
    public void setContents (String contents) {
        this.contents = contents;
    }

    /**
     * Returns a string representation of this item, in this case just
     * the transaction id.
     * @return a string representation of this item
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append(" [");
        buffer.append("Name: " + name);
        buffer.append("Contents: " + contents);
        buffer.append("]");

        return buffer.toString();
    }
}