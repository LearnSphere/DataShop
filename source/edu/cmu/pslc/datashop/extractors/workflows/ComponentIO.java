/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.workflows;

/**
 * POJO for component input/output object.
 *
 * @author Cindy Tipper
 * @version $Revision: 15509 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-10-02 15:28:39 -0400 (Tue, 02 Oct 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ComponentIO {

    /** Order of IO component. */
    private int index;

    /** Type of component. */
    private String type;

    /** Minimum number of input files to this node. */
    private String min_occurs = "1";

    /** Maximum number of input files to this node. */
    private String max_occurs = "1";

    /**
     * Name of component.
     * Used only to allow user to specify the
     * desired name of the output file.
     */
    private String name;

    /** Default constructor. */
    public ComponentIO() {
    }

    public int getIndex() { return index; }
    public void setIndex(int in) { index = in; }

    public String getType() { return type; }
    public void setType(String in) { type = in; }

    public String getMinOccurs() { return min_occurs; }
    public void setMinOccurs(String in) { min_occurs = in; }

    public String getMaxOccurs() { return max_occurs; }
    public void setMaxOccurs(String in) { max_occurs = in; }

    public String getName() { return name; }
    public void setName(String in) { name = in; }

    public String toString() {
        StringBuffer sb = new StringBuffer("ComponentIO [");
        sb.append("index = ").append(index);
        sb.append(", type = ").append(type);
        sb.append(", name = ").append(name);
        sb.append("]");

        return sb.toString();
    }
}
