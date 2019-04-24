/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

/**
 * Used to transfer sample data as XML, JSON, etc.
 *
 * @author Jim Rankin
 * @version $Revision: 5813 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-10-13 16:27:47 -0400 (Tue, 13 Oct 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "filter", properties = { "column", "operator", "filterText" })
public class FilterDTO extends DTO {
    /** the filter column */
    private String column;
    /** the filter operator */
    private String operator;
    /** the filter text */
    private String filterText;

    /** the filter column. @return the filter column */
    public String getColumn() { return column; }

    /** the filter column. @param column the filter column */
    public void setColumn(String column) { this.column = column; }

    /** the filter operator. @return the filter operator */
    public String getOperator() { return operator; }

    /** the filter operator. @param operator the filter operator */
    public void setOperator(String operator) { this.operator = operator; }

    /** the filter text. @return the filter text */
    public String getFilterText() { return filterText; }

    /** the filter text. @param filterText the filter text */
    public void setFilterText(String filterText) { this.filterText = filterText; }
}
