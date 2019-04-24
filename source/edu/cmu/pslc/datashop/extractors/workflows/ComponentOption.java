/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2018
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.extractors.workflows;

/**
 * POJO for component option object.
 *
 * @author Cindy Tipper
 * @version $Revision: 15509 $
 * <BR>Last modified by: $Author: pls21 $
 * <BR>Last modified on: $Date: 2018-10-02 15:28:39 -0400 (Tue, 02 Oct 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ComponentOption {

    /** Option type. */
    private String type;

    /** Option name. */
    private String name;

    /** Option id. */
    private String id;

    /** Option default. */
    private String defaultValue;

    /** Only for [Muli]FileInputHeader. Index of input node to get columns from. */
    private String inputNodeIndex = "*";

    /** Only for [Muli]FileInputHeader. Index of file on node to get columns from. */
    private String inputFileIndex = "*";

    /** Default constructor. */
    public ComponentOption() {
    }

    public String getType() { return type; }
    public void setType(String in) { type = in; }

    public String getName() { return name; }
    public void setName(String in) { name = in; }

    public String getId() { return id; }
    public void setId(String in) { id = in; }

    public String getDefault() { return defaultValue; }
    public void setDefault(String in) { defaultValue = in; }

    public String getInputNodeIndex() { return inputNodeIndex; }
    public void setInputNodeIndex(String in) { inputNodeIndex = in; }

    public String getInputFileIndex() { return inputFileIndex; }
    public void setInputFileIndex(String in) { inputFileIndex = in; }

    public String toString() {
        StringBuffer sb = new StringBuffer("ComponentOption [");
        sb.append("type = ").append(type);
        sb.append(", name = ").append(name);
        sb.append(", id = ").append(id);
        sb.append(", default = ").append(defaultValue);
        if (type.endsWith("FileInputHeader")) {
            sb.append(", inputNodeIndex = ").append(inputNodeIndex);
            sb.append(", inputFileIndex = ").append(inputFileIndex);
        }
        sb.append("]");

        return sb.toString();
    }
}
