package edu.cmu.pslc.datashop.servlet.exttools;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * This is a POJO for the table of files for a given tool.
 * It is a combination of External Tool File Map Item and File Item
 * with changes necessary for the UI.
 *
 * @author alida
 * @version $Revision: 10720 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-04 21:47:10 -0500 (Tue, 04 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolFileDto {

    //----- ATTRIBUTES -----

    /** File ID. */
    private Integer fileId;

    /** File Name. */
    private String fileName;
    /** File Size as a long. */
    private Long fileSize;
    /** File Size as a string. */
    private String fileSizeString;
    /** Downloads as an integer. */
    private Integer downloads;
    /** Downloads as a string. */
    private String downloadsString;
    /** Updated Time. */
    private Date updatedTime;
    /** Updated Time String. */
    private String updatedTimeString;

    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param fileId database generated unique id for the file item
     * @param fileName file name
     * @param fileSize size of the file
     * @param fileSizeString the file size string
     * @param downloads the number of times this tool has been downloaded
     * @param updatedTime the last time this tool was updated
     */
    public ExternalToolFileDto(Integer fileId,
            String fileName,
            Long fileSize,
            String fileSizeString,
            Integer downloads,
            Date updatedTime) {
        this.fileId = fileId;
        setFileName(fileName);
        setFileSize(fileSize);
        setFileSizeString(fileSizeString);
        setDownloads(downloads);
        setUpdatedTime(updatedTime);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the file id.
     * @return the file id
     */
    public Integer getFileId() {
        return fileId;
    }
    /**
     * Set the file id.
     * @param fileId the file id to set
     */
    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    /**
     * Get the file name.
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set the file name.
     * @param fileName the file name to set
     */
    private void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * Get the file size.
     * @return the file size
     */
    public Long getFileSize() {
        return fileSize;
    }
    /**
     * Set the file size.
     * @param fileSize the file size to set
     */
    private void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    /**
     * Get the file size as a string.
     * @return the file size as a string
     */
    public String getFileSizeString() {
        return fileSizeString;
    }
    /**
     * Set the fileSizeString.
     * @param fileSizeString the fileSizeString to set
     */
    public void setFileSizeString(String fileSizeString) {
        this.fileSizeString = fileSizeString;
    }
    /**
     * Get the downloads.
     * @return the downloads
     */
    public Integer getDownloads() {
        return downloads;
    }
    /**
     * Set the downloads.
     * @param downloads the downloads to set
     */
    private void setDownloads(Integer downloads) {
        this.downloads = downloads;
        this.downloadsString = downloads.toString() + " downloads";
    }
    /**
     * Get the downloads as a string.
     * @return the downloads as a string
     */
    public String getDownloadsString() {
        return downloadsString;
    }
    /**
     * Get the updated time.
     * @return the updatedTime
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }
    /**
     * Set the updated time.
     * @param updatedTime the updatedTime to set
     */
    private void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
        FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd");
        this.updatedTimeString = dateFormat.format(updatedTime);
    }
    /**
     * Get the updated time as a string.
     * @return the updatedTim as a string
     */
    public String getUpdatedTimeString() {
        return updatedTimeString;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the Name column. */
    public static final String COLUMN_NAME = "Name";
    /** Constant for the Language column. */
    public static final String COLUMN_SIZE = "Size";
    /** Constant for the Downloads column. */
    public static final String COLUMN_DOWNLOADS = "Downloads";
    /** Constant for the Updated column. */
    public static final String COLUMN_UPDATED = "Updated";

    /** Sort parameters array constant for sorting by name ascending. */
    private static final ExternalToolFileDto.SortParameter[] ASC_NAME_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_DOWNLOADS_ASC };
    /** Sort parameters array constant for sorting by name descending. */
    private static final ExternalToolFileDto.SortParameter[] DESC_NAME_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_DESC,
                         ExternalToolFileDto.SortParameter.SORT_BY_DOWNLOADS_ASC };
    /** Sort parameters array constant for sorting by language ascending. */
    private static final ExternalToolFileDto.SortParameter[] ASC_SIZE_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_SIZE_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_ASC };
    /** Sort parameters array constant for sorting by language descending. */
    private static final ExternalToolFileDto.SortParameter[] DESC_SIZE_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_SIZE_DESC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_ASC };
    /** Sort parameters array constant for sorting by downloads ascending. */
    private static final ExternalToolFileDto.SortParameter[] ASC_DOWNLOADS_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_DOWNLOADS_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_ASC };
    /** Sort parameters array constant for sorting by downloads descending. */
    private static final ExternalToolFileDto.SortParameter[] DESC_DOWNLOADS_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_DOWNLOADS_DESC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_ASC };
    /** Sort parameters array constant for sorting by updated ascending. */
    private static final ExternalToolFileDto.SortParameter[] ASC_UPDATED_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_ASC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by updated descending. */
    private static final ExternalToolFileDto.SortParameter[] DESC_UPDATED_PARAMS = {
                         ExternalToolFileDto.SortParameter.SORT_BY_UPDATED_DESC,
                         ExternalToolFileDto.SortParameter.SORT_BY_NAME_ASC };

    //----- METHODS FOR SORTING -----

    /**
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(
            String columnName, String sortByColumn, Boolean isAscending) {

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
    public static ExternalToolFileDto.SortParameter[] getSortByParameters(
            String sortByColumn, Boolean isAscending) {

        ExternalToolFileDto.SortParameter[] sortParams = ASC_NAME_PARAMS;

        if (sortByColumn.equals(COLUMN_NAME)) {
            if (isAscending) {
                sortParams = ASC_NAME_PARAMS;
            } else {
                sortParams = DESC_NAME_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_SIZE)) {
            if (isAscending) {
                sortParams = ASC_SIZE_PARAMS;
            } else {
                sortParams = DESC_SIZE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_DOWNLOADS)) {
            if (isAscending) {
                sortParams = ASC_DOWNLOADS_PARAMS;
            } else {
                sortParams = DESC_DOWNLOADS_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_UPDATED)) {
            if (isAscending) {
                sortParams = ASC_UPDATED_PARAMS;
            } else {
                sortParams = DESC_UPDATED_PARAMS;
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
    public static Comparator<ExternalToolFileDto> getComparator(SortParameter... sortParameters) {
        return new ToolFileDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** File Name Ascending. */
        SORT_BY_NAME_ASC,
        /** File Name Descending. */
        SORT_BY_NAME_DESC,
        /** Size Ascending. */
        SORT_BY_SIZE_ASC,
        /** Size Descending. */
        SORT_BY_SIZE_DESC,
        /** Downloads Ascending. */
        SORT_BY_DOWNLOADS_ASC,
        /** Downloads Descending. */
        SORT_BY_DOWNLOADS_DESC,
        /** Updated Time Ascending. */
        SORT_BY_UPDATED_ASC,
        /** Updated Time Descending. */
        SORT_BY_UPDATED_DESC
    }

    /**
     * Comparator for ToolDto objects.
     */
    private static final class ToolFileDtoComparator implements Comparator<ExternalToolFileDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private ToolFileDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(ExternalToolFileDto o1, ExternalToolFileDto o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_NAME_ASC,
                        SortParameter.SORT_BY_UPDATED_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_NAME_ASC:
                    result = o1.getFileName().compareToIgnoreCase(o2.getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_SIZE_ASC:
                    result = o1.getFileSize().compareTo(o2.getFileSize());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DOWNLOADS_ASC:
                    result = o1.getDownloads().compareTo(o2.getDownloads());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_ASC:
                    result = dateComparison(o1.getUpdatedTime(), o2.getUpdatedTime(), true);
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_NAME_DESC:
                    result = o2.getFileName().compareToIgnoreCase(o1.getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_SIZE_DESC:
                    result = o2.getFileSize().compareTo(o1.getFileSize());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DOWNLOADS_DESC:
                    result = o2.getDownloads().compareTo(o1.getDownloads());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_DESC:
                    result = dateComparison(o1.getUpdatedTime(), o2.getUpdatedTime(), false);
                    if (result != 0) { return result; }
                    break;
                default:
                    // No-op
                } // end switch
            } // end for loop
            return 0;
        } // end method compare
    } // end inner static class ToolFileDtoComparator
}
