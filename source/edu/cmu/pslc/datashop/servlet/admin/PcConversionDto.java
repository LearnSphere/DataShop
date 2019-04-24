/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2014
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.admin;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.PcConversionItem;

import org.apache.commons.lang.time.FastDateFormat;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * This is a POJO for the 'Problem Content Conversions' on the manage problem content page.
 *
 * @author Cindy Tipper
 * @version $Revision: 11126 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-05 16:25:50 -0400 (Thu, 05 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcConversionDto {

    //----- CONSTANTS -----

    /** Constant for the format of Content and Conversion dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("MMM dd, yyyy");

    //----- ATTRIBUTES -----

    /** The PcConversionItem describing this mapping. */
    private PcConversionItem pcConversion;
    /** Number of problems in this conversion. */
    private Long numProblems;
    /** Indication of whether or not the PcConversion can be deleted. */
    private Boolean isDeletable;


    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public PcConversionDto() { }

    /**
     * Constructor.
     * @param pcConversion the PcConversionItem describing this mapping
     * @param numProblems the number of problems in this conversion
     * @param isDeletable can this conversion be deleted; is it in use
     */
    public PcConversionDto(PcConversionItem pcConversion, Long numProblems, Boolean isDeletable) {
        setPcConversion(pcConversion);
        setNumProblems(numProblems);
        setIsDeletable(isDeletable);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the PcConversionItem.
     * @return PcConversionItem
     */
    public PcConversionItem getPcConversion() {
        return pcConversion;
    }

    /**
     * Set the PcConversionItem.
     * @param pcConversion the item
     */
    public void setPcConversion(PcConversionItem pcConversion) {
        this.pcConversion = pcConversion;
    }

    /**
     * Get the problem content conversion tool.
     * @return the conversionTool
     */
    public String getConversionTool() {
        return pcConversion.getConversionTool();
    }

    /**
     * Get the problem content version.
     * @return the contentVersion
     */
    public String getContentVersion() {
        return pcConversion.getContentVersion();
    }

    /**
     * Get the date associated with this problem content.
     * @return the contentDate
     */
    public Date getContentDate() {
        return pcConversion.getContentDate();
    }

    /**
     * Get the date associated with this problem content,
     * formatted for display.
     * @return the contentDate
     */
    public String getContentDateStr() {
        if ((pcConversion == null) || (pcConversion.getContentDate() == null)) {
            return "";
        } else {
            return DATE_FMT.format(pcConversion.getContentDate());
        }
    }

    /**
     * Get the date the conversion was generated.
     * @return the conversionDate
     */
    public Date getConversionDate() {
        return pcConversion.getConversionDate();
    }

    /**
     * Get the date the conversion was generated, formatted for display.
     * @return the conversionDate
     */
    public String getConversionDateStr() {
        if ((pcConversion == null) || (pcConversion.getConversionDate() == null)) {
            return "";
        } else {
            return DATE_FMT.format(pcConversion.getConversionDate());
        }
    }

    /**
     * Get the problem content description.
     * @return the contentDescription
     */
    public String getContentDescription() {
        return pcConversion.getContentDescription();
    }

    /**
     * Get the number of problems.
     * @return number
     */
    public Long getNumProblems() {
        return numProblems;
    }

    /**
     * Set the number of problems.
     * @param numProblems the number
     */
    public void setNumProblems(Long numProblems) {
        this.numProblems = numProblems;
    }

    /**
     * Get the flag indicating if this conversion can be deleted.
     * @return the flag
     */
    public Boolean getIsDeletable() {
        return isDeletable;
    }

    /**
     * Set the flag indicating if this conversion can be deleted.
     * @param isDeletable flag
     */
    public void setIsDeletable(Boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    /**
     * Get the tool version information, formatted for display.
     * @return String with DataShop version and tool SVN revision
     */
    public String getToolVersionStr() {
        // If either is undefined, return empty string.
        if ((pcConversion.getDatashopVersion() == null)
            || (pcConversion.getToolVersion() == null)) { return ""; }

        StringBuffer sb = new StringBuffer();
        sb.append("DataShop: ");
        sb.append(pcConversion.getDatashopVersion());
        sb.append("<br>");
        sb.append("SVN: ");
        sb.append(pcConversion.getToolVersion());

        return sb.toString();
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Content Version' column. */
    public static final String COLUMN_CONTENT_VERSION = "Content Version";
    /** Constant for the 'Conversion Tool' column. */
    public static final String COLUMN_CONVERSION_TOOL = "Conversion Tool";
    /** Constant for the 'Content Date' column. */
    public static final String COLUMN_CONTENT_DATE = "Content Date";
    /** Constant for the 'Conversion Date' column. */
    public static final String COLUMN_CONVERSION_DATE = "Conversion Date";
    /** Constant for the 'Number of Problems' column. */
    public static final String COLUMN_NUM_PROBLEMS = "Number of Problems";
    /** Constant for the 'Tool Version' column. */
    public static final String COLUMN_TOOL_VERSION = "Tool Version";

    /** Sort parameters array constant for sorting by conversion tool ascending. */
    private static final PcConversionDto.SortParameter[] ASC_CONVERSION_TOOL_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by conversion tool descending. */
    private static final PcConversionDto.SortParameter[] DESC_CONVERSION_TOOL_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_DESC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    /** Sort parameters array constant for sorting by content version ascending. */
    private static final PcConversionDto.SortParameter[] ASC_CONTENT_VERSION_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_DATE_ASC };
    /** Sort parameters array constant for sorting by content version descending. */
    private static final PcConversionDto.SortParameter[] DESC_CONTENT_VERSION_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_DESC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_DATE_ASC };

    /** Sort parameters array constant for sorting by conversion date ascending. */
    private static final PcConversionDto.SortParameter[] ASC_CONVERSION_DATE_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_DATE_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by conversion date descending. */
    private static final PcConversionDto.SortParameter[] DESC_CONVERSION_DATE_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_DATE_DESC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    /** Sort parameters array constant for sorting by numProblems ascending. */
    private static final PcConversionDto.SortParameter[] ASC_NUM_PROBLEMS_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_NUM_PROBLEMS_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by numProblems descending. */
    private static final PcConversionDto.SortParameter[] DESC_NUM_PROBLEMS_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_NUM_PROBLEMS_DESC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    /** Sort parameters array constant for sorting by toolVersion ascending. */
    private static final PcConversionDto.SortParameter[] ASC_TOOL_VERSION_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_TOOL_VERSION_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by toolVersion descending. */
    private static final PcConversionDto.SortParameter[] DESC_TOOL_VERSION_PARAMS = {
        PcConversionDto.SortParameter.SORT_BY_TOOL_VERSION_DESC,
        PcConversionDto.SortParameter.SORT_BY_CONVERSION_TOOL_ASC,
        PcConversionDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    //----- METHODS FOR SORTING -----

    /**
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(String columnName, String sortByColumn, Boolean isAscending) {

        String imgIcon = "images/trans_spacer.gif";
        if (sortByColumn != null && sortByColumn.equals(columnName)) {
            imgIcon = isAscending ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Gets the current sortBy parameters.
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return the current sortBy parameters.
     */
    public static PcConversionDto.SortParameter[] getSortByParameters(String sortByColumn,
                                                                      Boolean isAscending) {

        PcConversionDto.SortParameter[] sortParams = ASC_CONVERSION_TOOL_PARAMS;

        if (sortByColumn.equals(COLUMN_CONVERSION_TOOL)) {
            if (isAscending) {
                sortParams = ASC_CONVERSION_TOOL_PARAMS;
            } else {
                sortParams = DESC_CONVERSION_TOOL_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_CONTENT_VERSION)) {
            if (isAscending) {
                sortParams = ASC_CONTENT_VERSION_PARAMS;
            } else {
                sortParams = DESC_CONTENT_VERSION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_CONVERSION_DATE)) {
            if (isAscending) {
                sortParams = ASC_CONVERSION_DATE_PARAMS;
            } else {
                sortParams = DESC_CONVERSION_DATE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_TOOL_VERSION)) {
            if (isAscending) {
                sortParams = ASC_TOOL_VERSION_PARAMS;
            } else {
                sortParams = DESC_TOOL_VERSION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_NUM_PROBLEMS)) {
            if (isAscending) {
                sortParams = ASC_NUM_PROBLEMS_PARAMS;
            } else {
                sortParams = DESC_NUM_PROBLEMS_PARAMS;
            }
        }
        return sortParams;
    }

    //----- SORTING COMPARATOR -----

    /**
     * Comparator object used for sorting.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<PcConversionDto> getComparator(SortParameter... sortParameters) {
        return new PcConversionDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Conversion Tool Ascending. */
        SORT_BY_CONVERSION_TOOL_ASC,
        /** Conversion Tool Descending. */
        SORT_BY_CONVERSION_TOOL_DESC,
        /** Content Version Ascending. */
        SORT_BY_CONTENT_VERSION_ASC,
        /** Content Version Descending. */
        SORT_BY_CONTENT_VERSION_DESC,
        /** Content Date Ascending. */
        SORT_BY_CONTENT_DATE_ASC,
        /** Content Date Descending. */
        SORT_BY_CONTENT_DATE_DESC,
        /** Conversion Date Ascending. */
        SORT_BY_CONVERSION_DATE_ASC,
        /** Conversion Descending. */
        SORT_BY_CONVERSION_DATE_DESC,
        /** Tool Version Ascending. */
        SORT_BY_TOOL_VERSION_ASC,
        /** Tool Version Descending. */
        SORT_BY_TOOL_VERSION_DESC,
        /** Number of Problems Ascending. */
        SORT_BY_NUM_PROBLEMS_ASC,
        /** Number of Problems Descending. */
        SORT_BY_NUM_PROBLEMS_DESC
    }

    /**
     * Comparator for PcConversionDto objects.
     */
    private static final class PcConversionDtoComparator
        implements Comparator<PcConversionDto> {

        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private PcConversionDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(PcConversionDto o1, PcConversionDto o2) {
            if (parameters == null) {
                SortParameter[] params = { SortParameter.SORT_BY_CONVERSION_TOOL_ASC };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_CONVERSION_TOOL_ASC:
                    result = o1.getConversionTool().compareToIgnoreCase(o2.getConversionTool());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTENT_VERSION_ASC:
                    result = o1.getContentVersion().compareToIgnoreCase(o2.getContentVersion());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTENT_DATE_ASC:
                    result = dateComparison(o1.getContentDate(), o2.getContentDate(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONVERSION_DATE_ASC:
                    result = dateComparison(o1.getConversionDate(), o2.getConversionDate(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TOOL_VERSION_ASC:
                    result = o1.getToolVersionStr().compareTo(o2.getToolVersionStr());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NUM_PROBLEMS_ASC:
                    result = o1.getNumProblems().compareTo(o2.getNumProblems());
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_CONVERSION_TOOL_DESC:
                    result = o2.getConversionTool().compareToIgnoreCase(o1.getConversionTool());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTENT_VERSION_DESC:
                    result = o2.getContentVersion().compareToIgnoreCase(o1.getContentVersion());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTENT_DATE_DESC:
                    result = dateComparison(o1.getContentDate(), o2.getContentDate(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONVERSION_DATE_DESC:
                    result = dateComparison(o1.getConversionDate(), o2.getConversionDate(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TOOL_VERSION_DESC:
                    result = o2.getToolVersionStr().compareTo(o1.getToolVersionStr());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_NUM_PROBLEMS_DESC:
                    result = o2.getNumProblems().compareTo(o1.getNumProblems());
                    if (result != 0) { return result; }
                    break;
                default:
                    // No-op
                }
            }
            return 0;
        }
    }

}
