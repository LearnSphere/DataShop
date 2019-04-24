package edu.cmu.pslc.datashop.workflows;

public class InputHeaderOption {

    String optionName = null;
    String optionValue = null;
    Integer nodeIndex = null;
    Integer fileIndex = null;
    Integer column = null;
    public InputHeaderOption() {

    }

    public InputHeaderOption(String optionName, String optionValue, Integer nodeIndex, Integer fileIndex, Integer column) {
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.nodeIndex = nodeIndex;
        this.fileIndex = fileIndex;
        this.column = column;
    }

    /**
     * @return the optionName
     */
    public String getOptionName() {
        return optionName;
    }

    /**
     * @param optionName the optionName to set
     */
    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    /**
     * @return the optionValue
     */
    public String getOptionValue() {
        return optionValue;
    }

    /**
     * @param optionValue the optionValue to set
     */
    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    /**
     * @return the nodeIndex
     */
    public Integer getNodeIndex() {
        return (nodeIndex == null) ? 0 : nodeIndex;
    }

    /**
     * @param nodeIndex the nodeIndex to set
     */
    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    /**
     * @return the fileIndex
     */
    public Integer getFileIndex() {
        return (fileIndex == null) ? 0 : fileIndex;
    }

    /**
     * @param fileIndex the fileIndex to set
     */
    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }

    /**
     * @return the column
     */
    public Integer getColumn() {
        return (column == null) ? 0 : column;
    }

    /**
     * @param column the column to set
     */
    public void setColumn(Integer column) {
        this.column = column;
    }

}
