package edu.cmu.pslc.datashop.workflows;

public class ComponentNode {

    private Integer nodeIndex;
    private Integer minOccurs;
    private Integer maxOccurs;

    public ComponentNode() {

    }

    public ComponentNode(Integer nodeIndex) {
        this.minOccurs = 0;
        this.maxOccurs = 1;
        this.nodeIndex = nodeIndex;
    }

    public ComponentNode(Integer nodeIndex, Integer minOccurs, Integer maxOccurs) {
        this.nodeIndex = nodeIndex;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }

    /**
     * @return the nodeIndex
     */
    public Integer getNodeIndex() {
        if (nodeIndex == null) {
            nodeIndex = 0;
        }
        return nodeIndex;
    }

    /**
     * @param nodeIndex the nodeIndex to set
     */
    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    /**
     * @return the minOccurs
     */
    public Integer getMinOccurs() {
        if (minOccurs == null) {
            minOccurs = 0;
        }
        return minOccurs;
    }

    /**
     * @param minOccurs the minOccurs to set
     */
    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * @return the maxOccurs
     */
    public Integer getMaxOccurs() {
        if (maxOccurs == null) {
            maxOccurs = 0;
        }
        return maxOccurs;
    }

    /**
     * @param maxOccurs the maxOccurs to set
     */
    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * @param maxOccursStr the maxOccurs string to set
     */
    public void setMaxOccursFromString(String maxOccursStr) {
        if (maxOccursStr != null) {
            if (maxOccursStr.trim().equalsIgnoreCase("unbounded")) {
                maxOccurs = Integer.MAX_VALUE;
            } else if (maxOccursStr.trim().matches("[0-9]+")) {
                maxOccurs = Integer.parseInt(maxOccursStr.trim());
            } else {
                maxOccurs = 1;
            }
        }
    }
}
