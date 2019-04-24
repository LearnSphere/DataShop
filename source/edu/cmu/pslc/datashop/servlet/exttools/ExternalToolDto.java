package edu.cmu.pslc.datashop.servlet.exttools;

import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.UserItem;

/**
 * This is a POJO for the table of tools. It is very similar to an ExternalToolItem
 * but with differences to display the data.
 *
 * @author alida
 * @version $Revision: 10721 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-03-05 08:26:26 -0500 (Wed, 05 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalToolDto {

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** Contributor user id . */
    private String contributorId;
    /** Contributor full name. */
    private String contributor;

    /** Full Name. */
    private String name;

    /** Full Description. */
    private String description;

    /** Language. */
    private String language;
    /** WebPage. */
    private String webPage;
    /** Downloads as an integer. */
    private Integer downloads;
    /** Downloads as a string. */
    private String downloadsString;
    /** Added Time. */
    private Date addedTime;
    /** Added Time String. */
    private String addedTimeString;
    /** Updated Time. */
    private Date updatedTime;
    /** Updated Time String. */
    private String updatedTimeString;

    //----- CONSTANTS -----
    /** Fast date format object. */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");


    //----- CONSTRUCTOR -----

    /**
     * Constructor.
     * @param id database generated unique id for the tool item
     * @param name the name of the tool
     * @param description the description of the tool
     * @param language the language the tool is written in
     * @param webPage the webPage for more information
     * @param downloads the number of times this tool has been downloaded
     * @param addedTime the last time this tool was added
     * @param updatedTime the last time this tool was updated
     */
    public ExternalToolDto(Integer id,
            String name, String description,
            String language, String webPage,
            Integer downloads,
            Date addedTime, Date updatedTime) {
        this.id = id;
        setName(name);
        setDescription(description);
        setLanguage(language);
        setWebPage(webPage);
        setDownloads(downloads);
        setAddedTime(addedTime);
        setUpdatedTime(updatedTime);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }
    /**
     * Set the id.
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
    /**
     * Get the contributor.
     * @return the contributor
     */
    public String getContributor() {
        return contributor;
    }
    /**
     * Set the contributor with a filled-in user item object.
     * @param userItem the contributor as a user item
     */
    public void setContributor(UserItem userItem) {
        String firstName =
            (userItem.getFirstName() != null && userItem.getFirstName().length() > 0)
                ? userItem.getFirstName()
                : "-";

        String lastName =
            (userItem.getLastName() != null && userItem.getLastName().length() > 0)
                ? Character.toUpperCase(userItem.getLastName().charAt(0))
                        + userItem.getLastName().substring(1)
                : "-";

        this.contributor = (firstName.equals("-") && lastName.equals("-"))
                ? (String)userItem.getId() : firstName + " " + lastName;

        this.contributorId = (String)userItem.getId();
    }
    /**
     * Get the contributorId.
     * @return the contributorId
     */
    public String getContributorId() {
        return contributorId;
    }
    /**
     * Get the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param name the name to set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     * @param desc the description to set
     */
    private void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Get the language.
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    /**
     * Set the language.
     * @param language the language to set
     */
    private void setLanguage(String language) {
        this.language = language;
    }
    /**
     * Get the web page.
     * @return the web page
     */
    public String getWebPage() {
        return webPage;
    }
    /**
     * Set the web page.
     * @param webPage the web page to set
     */
    private void setWebPage(String webPage) {
        this.webPage = webPage;
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
        StringBuffer sb = new StringBuffer(downloads.toString());
        sb.append(" download");
        if (downloads != 1) {
            sb.append("s");
        }
        this.downloadsString = sb.toString();
    }
    /**
     * Get the downloads as a string.
     * @return the downloads as a string
     */
    public String getDownloadsString() {
        return downloadsString;
    }
    /**
     * Get the added time.
     * @return the addedTime
     */
    public Date getAddedTime() {
        return addedTime;
    }
    /**
     * Set the added time.
     * @param addedTime the addedTime to set
     */
    private void setAddedTime(Date addedTime) {
        this.addedTime = addedTime;
        this.addedTimeString = DATE_FORMAT.format(addedTime);
    }
    /**
     * Get the added time as a string.
     * @return the addedTime as a string
     */
    public String getAddedTimeString() {
        return addedTimeString;
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
        this.updatedTimeString = DATE_FORMAT.format(updatedTime);
    }
    /**
     * Get the updated time as a string.
     * @return the updatedTim as a string
     */
    public String getUpdatedTimeString() {
        return updatedTimeString;
    }

    //----- UTILITY METHODs -----

    /**
     * Return the given text with URLs in HTML anchor elements.
     * @param text the text to change
     * @return the new text
     */
    public String fixUrls(String text) {
        String regex = "((http://|https://)([\\S]*))";
        String replace = "<a href=\"$1\" target=\"_blank\">$1</a>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        String s = m.replaceAll(replace);
        return s;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the Name column. */
    public static final String COLUMN_NAME = "Name";
    /** Constant for the Language column. */
    public static final String COLUMN_LANGUAGE = "Language";
    /** Constant for the Contributor column. */
    public static final String COLUMN_CONTRIBUTOR = "Contributor";
    /** Constant for the Downloads column. */
    public static final String COLUMN_DOWNLOADS = "Downloads";
    /** Constant for the Updated column. */
    public static final String COLUMN_UPDATED = "Updated";


    /** Sort parameters array constant for sorting by name ascending. */
    private static final ExternalToolDto.SortParameter[] ASC_NAME_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by name descending. */
    private static final ExternalToolDto.SortParameter[] DESC_NAME_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_NAME_DESC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by language ascending. */
    private static final ExternalToolDto.SortParameter[] ASC_LANGUAGE_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_LANGUAGE_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by language descending. */
    private static final ExternalToolDto.SortParameter[] DESC_LANGUAGE_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_LANGUAGE_DESC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by contributor ascending. */
    private static final ExternalToolDto.SortParameter[] ASC_CONTRIBUTOR_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by contributor descending. */
    private static final ExternalToolDto.SortParameter[] DESC_CONTRIBUTOR_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_DESC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by downloads ascending. */
    private static final ExternalToolDto.SortParameter[] ASC_DOWNLOADS_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_DOWNLOADS_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by downloads descending. */
    private static final ExternalToolDto.SortParameter[] DESC_DOWNLOADS_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_DOWNLOADS_DESC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by updated ascending. */
    private static final ExternalToolDto.SortParameter[] ASC_UPDATED_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_UPDATED_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };
    /** Sort parameters array constant for sorting by updated descending. */
    private static final ExternalToolDto.SortParameter[] DESC_UPDATED_PARAMS = {
                         ExternalToolDto.SortParameter.SORT_BY_UPDATED_DESC,
                         ExternalToolDto.SortParameter.SORT_BY_NAME_ASC,
                         ExternalToolDto.SortParameter.SORT_BY_CONTRIBUTOR_ASC };

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
    public static ExternalToolDto.SortParameter[] getSortByParameters(
            String sortByColumn, Boolean isAscending) {

        ExternalToolDto.SortParameter[] sortParams = ASC_NAME_PARAMS;

        if (sortByColumn.equals(COLUMN_NAME)) {
            if (isAscending) {
                sortParams = ASC_NAME_PARAMS;
            } else {
                sortParams = DESC_NAME_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_LANGUAGE)) {
            if (isAscending) {
                sortParams = ASC_LANGUAGE_PARAMS;
            } else {
                sortParams = DESC_LANGUAGE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_CONTRIBUTOR)) {
            if (isAscending) {
                sortParams = ASC_CONTRIBUTOR_PARAMS;
            } else {
                sortParams = DESC_CONTRIBUTOR_PARAMS;
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
    public static Comparator<ExternalToolDto> getComparator(SortParameter... sortParameters) {
        return new ToolDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Tool Name Ascending. */
        SORT_BY_NAME_ASC,
        /** Tool Name Descending. */
        SORT_BY_NAME_DESC,
        /** Language Ascending. */
        SORT_BY_LANGUAGE_ASC,
        /** Language Descending. */
        SORT_BY_LANGUAGE_DESC,
        /** Contributor Ascending. */
        SORT_BY_CONTRIBUTOR_ASC,
        /** Contributor Descending. */
        SORT_BY_CONTRIBUTOR_DESC,
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
    private static final class ToolDtoComparator implements Comparator<ExternalToolDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private ToolDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(ExternalToolDto o1, ExternalToolDto o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_UPDATED_DESC,
                        SortParameter.SORT_BY_NAME_ASC,
                        SortParameter.SORT_BY_CONTRIBUTOR_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                //--- Ascending ---
                case SORT_BY_NAME_ASC:
                    result = o1.getName().compareToIgnoreCase(o2.getName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LANGUAGE_ASC:
                    result = o1.getLanguage().compareToIgnoreCase(o2.getLanguage());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTRIBUTOR_ASC:
                    result = o1.getContributor().compareTo(o2.getContributor());
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
                    result = o2.getName().compareToIgnoreCase(o1.getName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LANGUAGE_DESC:
                    result = o2.getLanguage().compareToIgnoreCase(o1.getLanguage());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CONTRIBUTOR_DESC:
                    result = o2.getContributor().compareTo(o1.getContributor());
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
    } // end inner static class ToolDtoComparator
}
