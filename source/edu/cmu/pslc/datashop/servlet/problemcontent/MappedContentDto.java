/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2014
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.PcConversionItem;

import org.apache.commons.lang.time.FastDateFormat;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * This is a POJO for the 'Mapped Content' on the project content subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 11094 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-05-27 13:13:30 -0400 (Tue, 27 May 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class MappedContentDto {

    //----- CONSTANTS -----

    /** Constant for the format of Content and Conversion dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("MMM dd, yyyy");

    //----- ATTRIBUTES -----

    /** The PcConversionItem describing this mapping. */
    private PcConversionItem pcConversion;
    /** Status of Problem Content-to-Dataset mapping. */
    private String status;
    /** Number of problems mapped by this mapping. */
    private Long numProblemsMapped;


    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public MappedContentDto() { }

    /**
     * Constructor.
     * @param pcConversion the PcConversionItem describing this mapping
     */
    public MappedContentDto(PcConversionItem pcConversion) {
        setPcConversion(pcConversion);
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
     * Get the status of the mapping.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set the status of the mapping.
     * @param status the mapping status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Get the number of problems mapped.
     * @return number
     */
    public Long getNumProblemsMapped() {
        return numProblemsMapped;
    }

    /**
     * Set the number of problems mapped.
     * @param numProblemsMapped the number
     */
    public void setNumProblemsMapped(Long numProblemsMapped) {
        this.numProblemsMapped = numProblemsMapped;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Content Version' column. */
    public static final String COLUMN_CONTENT_VERSION = "Content Version";
    /** Constant for the 'Content Date' column. */
    public static final String COLUMN_CONTENT_DATE = "Content Date";
    /** Constant for the 'Conversion Date' column. */
    public static final String COLUMN_CONVERSION_DATE = "Conversion Date";
    /** Constant for the 'Status' column. */
    public static final String COLUMN_STATUS = "Status";

    /** Sort parameters array constant for sorting by content version ascending. */
    private static final MappedContentDto.SortParameter[] ASC_CONTENT_VERSION_PARAMS = {
                         MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by content version descending. */
    private static final MappedContentDto.SortParameter[] DESC_CONTENT_VERSION_PARAMS = {
                         MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_DESC };

    /** Sort parameters array constant for sorting by content date ascending. */
    private static final MappedContentDto.SortParameter[] ASC_CONTENT_DATE_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_CONTENT_DATE_ASC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by content date descending. */
    private static final MappedContentDto.SortParameter[] DESC_CONTENT_DATE_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_CONTENT_DATE_DESC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    /** Sort parameters array constant for sorting by conversion date ascending. */
    private static final MappedContentDto.SortParameter[] ASC_CONVERSION_DATE_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_CONVERSION_DATE_ASC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by conversion date descending. */
    private static final MappedContentDto.SortParameter[] DESC_CONVERSION_DATE_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_CONVERSION_DATE_DESC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

    /** Sort parameters array constant for sorting by status ascending. */
    private static final MappedContentDto.SortParameter[] ASC_STATUS_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_STATUS_ASC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };
    /** Sort parameters array constant for sorting by status descending. */
    private static final MappedContentDto.SortParameter[] DESC_STATUS_PARAMS = {
        MappedContentDto.SortParameter.SORT_BY_STATUS_DESC,
        MappedContentDto.SortParameter.SORT_BY_CONTENT_VERSION_ASC };

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
    public static MappedContentDto.SortParameter[] getSortByParameters(String sortByColumn,
                                                                       Boolean isAscending) {

        MappedContentDto.SortParameter[] sortParams = ASC_CONTENT_VERSION_PARAMS;

        if (sortByColumn.equals(COLUMN_CONTENT_VERSION)) {
            if (isAscending) {
                sortParams = ASC_CONTENT_VERSION_PARAMS;
            } else {
                sortParams = DESC_CONTENT_VERSION_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_CONTENT_DATE)) {
            if (isAscending) {
                sortParams = ASC_CONTENT_DATE_PARAMS;
            } else {
                sortParams = DESC_CONTENT_DATE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_CONVERSION_DATE)) {
            if (isAscending) {
                sortParams = ASC_CONVERSION_DATE_PARAMS;
            } else {
                sortParams = DESC_CONVERSION_DATE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_STATUS)) {
            if (isAscending) {
                sortParams = ASC_STATUS_PARAMS;
            } else {
                sortParams = DESC_STATUS_PARAMS;
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
    public static Comparator<MappedContentDto> getComparator(SortParameter... sortParameters) {
        return new MappedContentDtoComparator(sortParameters);
    }

    public enum SortParameter {
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
        /** Status Ascending. */
        SORT_BY_STATUS_ASC,
        /** Status Descending. */
        SORT_BY_STATUS_DESC
    }

    /**
     * Comparator for MappedContentDto objects.
     */
    private static final class MappedContentDtoComparator
        implements Comparator<MappedContentDto> {

        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private MappedContentDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(MappedContentDto o1, MappedContentDto o2) {
            if (parameters == null) {
                SortParameter[] params = { SortParameter.SORT_BY_CONTENT_VERSION_ASC };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
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
                case SORT_BY_STATUS_ASC:
                    result = o1.getStatus().compareToIgnoreCase(o2.getStatus());
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
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
                case SORT_BY_STATUS_DESC:
                    result = o2.getStatus().compareToIgnoreCase(o1.getStatus());
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
