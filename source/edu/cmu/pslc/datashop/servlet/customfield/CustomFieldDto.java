/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */

package edu.cmu.pslc.datashop.servlet.customfield;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.FastDateFormat;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.UserItem;
import static edu.cmu.pslc.logging.util.DateTools.dateComparison;

/**
 * DTO for the Custom Field item.
 *
 * @author Cindy Tipper
 * @version $Revision: 11997 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2015-02-06 11:04:31 -0500 (Fri, 06 Feb 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class CustomFieldDto {

    //----- CONSTANTS -----

    /** Constant for the format of Approval and Expiration dates. */
    private static final FastDateFormat DATE_FMT = FastDateFormat.getInstance("yyyy-MM-dd");

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    //----- ATTRIBUTES -----

    /** Database generated unique ID. */
    private Integer id;
    /** The name of this Custom Field. */
    private String name;
    /** The description of this Custom Field. */
    private String description;
    /** The truncated description of this Custom Field. */
    private String descriptionTruncated;
    /** The flag indicating whether or not description is truncated. */
    private Boolean descriptionTruncatedFlag = false;
    /** The type of this Custom Field. */
    private ConcurrentMap<String, AtomicLong> type;
    /** The level of this Custom Field. */
    private String level;
    /** The dataset this Custom Field is associated with. */
    private DatasetItem dataset;
    /** User that created/added this Custom Field. */
    private UserItem owner;
    /** Date the Custom Field was created/added. */
    private Date dateCreated;
    /** User that last modified this Custom Field. */
    private UserItem updatedBy;
    /** Date the Custom Field was last modified. */
    private Date lastUpdated;
    /** Percentage of rows with CustomField values. */
    private Integer rowsWithValues;

    /** List of Custom Field Type values. */
    public static final List<String> CF_TYPE_ENUM = new ArrayList<String>();
    /** Type "number" value. */
    public static final String CF_TYPE_NUMBER = "number";
    /** Type "string" value. */
    public static final String CF_TYPE_STRING = "string";
    /** Type "date" value. */
    public static final String CF_TYPE_DATE = "date";

    static {
        CF_TYPE_ENUM.add(CF_TYPE_NUMBER);
        CF_TYPE_ENUM.add(CF_TYPE_STRING);
        CF_TYPE_ENUM.add(CF_TYPE_DATE);
    }

    /** List of Custom Field Level values. */
    private static final List<String> CF_LEVEL_ENUM = new ArrayList<String>();
    /** Level "transaction" value. */
    public static final String CF_LEVEL_TRANSACTION = "transaction";
    /** Level "student" value. */
    public static final String CF_LEVEL_STUDENT = "student";
    /** Level "problem" value. */
    public static final String CF_LEVEL_PROBLEM = "problem";
    /** Level "step" value. */
    public static final String CF_LEVEL_STEP = "step";
    /** Level "student_problem" value. */
    public static final String CF_LEVEL_STUDENT_PROBLEM = "student_problem";
    /** Level "student_problem_pv" value. */
    public static final String CF_LEVEL_STUDENT_PROBLEM_PV = "student_problem_pv";
    /** Level "student_step" value. */
    public static final String CF_LEVEL_STUDENT_STEP = "student_step";
    /** Level "student_step_pv" value. */
    public static final String CF_LEVEL_STUDENT_STEP_PV = "student_step_pv";
    /** Level "kcm" value. */
    public static final String CF_LEVEL_KCM = "kcm";
    /** Level "kc" value. */
    public static final String CF_LEVEL_KC = "kc";
    /** Level "student_kcm" value. */
    public static final String CF_LEVEL_STUDENT_KCM = "student_kcm";
    /** Level "student_kc" value. */
    public static final String CF_LEVEL_STUDENT_KC = "student_kc";
    /** Level "student_step_pv_kc" value. */
    public static final String CF_LEVEL_STUDENT_STEP_PV_KC = "student_step_pv_kc";

    static {
        CF_LEVEL_ENUM.add(CF_LEVEL_TRANSACTION);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT);
        CF_LEVEL_ENUM.add(CF_LEVEL_PROBLEM);
        CF_LEVEL_ENUM.add(CF_LEVEL_STEP);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_PROBLEM);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_PROBLEM_PV);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_STEP);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_STEP_PV);
        CF_LEVEL_ENUM.add(CF_LEVEL_KCM);
        CF_LEVEL_ENUM.add(CF_LEVEL_KC);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_KCM);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_KC);
        CF_LEVEL_ENUM.add(CF_LEVEL_STUDENT_STEP_PV_KC);
    }

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public CustomFieldDto() { }

    /**
     * Constructor.
     * @param id database generated unique id for the tool item
     * @param name the name of this custom field
     * @param description the description of this custom field
     * @param types the type of this custom field
     * @param level the level of this custom field
     */
    public CustomFieldDto(Integer id, String name, String description,
            ConcurrentMap<String, AtomicLong> types, String level) {
        this.id = id;
        this.name = name;
        setDescription(description);
        this.type = types;
        this.level = level;
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
     * Get the name.
     * @return java.lang.String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name.
     * @param name The name for this custom field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description.
     * @return java.lang.String
     */
    public String getDescription() {
        return this.description;
    }

    /** Constant for the max length of the description. */
    private static final Integer MAX_DESC_LENGTH_FOR_DISPLAY = 100;

    /**
     * Set the description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
        if ((description != null) && (description.length() > MAX_DESC_LENGTH_FOR_DISPLAY)) {
            this.descriptionTruncated = description.substring(0, MAX_DESC_LENGTH_FOR_DISPLAY);
            this.descriptionTruncated += "...";
            this.descriptionTruncatedFlag = true;
        } else {
            this.descriptionTruncated = description;
            this.descriptionTruncatedFlag = false;
        }
    }

    /**
     * Get the description to display.
     * @return the description to display
     */
    public String getDescriptionToDisplay() {
        String result = descriptionTruncatedFlag ? descriptionTruncated : description;
        return fixUrls(result);
    }

    /**
     * Get the truncated description.
     * @return the descriptionTruncated
     */
    public String getDescriptionTruncated() {
        return descriptionTruncated;
    }

    /**
     * Get the flag indicating whether the description is truncated.
     * @return the descriptionTruncatedFlag
     */
    public Boolean getDescriptionTruncatedFlag() {
        return descriptionTruncatedFlag;
    }

    /**
     * Get the type of this Custom Field.
     * @return String the type
     */
    public ConcurrentMap<String, AtomicLong> getType() {
        return type;
    }

    /**
     * Add a type of this Custom Field value.
     * @param typeString the type
     */
    public void addType(String typeString) {
        type.putIfAbsent(typeString, new AtomicLong(0));
        type.get(typeString).incrementAndGet();
    }

    private static double PERCENTAGE_MULTIPLIER = 100.0;
    private static double ZERO = 0.0;
    private static DecimalFormat decimalFormat = new DecimalFormat("#.#");
    /**
     * The toString method for the types map.
     * @return the string representation of the type map
     */
    public String getTypeString() {
        StringBuffer sBuffer = new StringBuffer();
        Double total = 0.0;

        for (String key : type.keySet()) {
            total += type.get(key).doubleValue();
        }
        for (String key : type.keySet()) {
            if (type.get(key).doubleValue() > ZERO) {
                Double percentage = type.get(key).doubleValue() / total * PERCENTAGE_MULTIPLIER;
                sBuffer.append(key + ": " + decimalFormat.format(percentage) + "% <br />");
            }
        }
        return sBuffer.toString();
    }

    /**
     * Get the level of this Custom Field.
     * @return String the level
     */
    public String getLevel() {
        return level;
    }

    /**
     * Set the level of this Custom Field.
     * @param level the level
     */
    public void setLevel(String level) {
        if (level == null) {
            this.level = null;
        } else if (CF_LEVEL_ENUM.contains(level)) {
            this.level = level;
        } else {
            throw new IllegalArgumentException("Invalid Level value: " + level);
        }
    }

    /**
     * Get the dataset.
     * @return DatasetItem
     */
    public DatasetItem getDataset() {
        return dataset;
    }

    /**
     * Set the dataset.
     * @param dataset the DatasetItem
     */
    public void setDataset(DatasetItem dataset) {
        this.dataset = dataset;
    }

    /**
     * Get the owner.
     * @return UserItem
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Get the name of the user that created this custom field.
     * @return String user name (first, last)
     */
    public String getOwnerString() {
        return owner.getName();
    }

    /**
     * Set the owner.
     * @param owner the custom field creator.
     */
    public void setOwner(UserItem owner) {
        this.owner = owner;
    }

    /**
     * Get the date this custom field was created.
     * @return java.util.Date
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * Set the date this custom field was created.
     * @param date the date
     */
    public void setDateCreated(Date date) {
        this.dateCreated = date;
    }

    /**
     * Get the created date as a string.
     * @return the dateCreated as a string
     */
    public String getDateCreatedString() {
        if (dateCreated == null) {
            return "";
        } else {
            return DATE_FMT.format(dateCreated);
        }
    }

    /**
     * Get the user that last updated this Custom Field.
     * @return UserItem user that last updated this CF
     */
    public UserItem getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Set the user that last updated this Custom Field.
     * @param updatedBy user that last updated this CF
     */
    public void setUpdatedBy(UserItem updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Get the name of the user that updated this CF.
     * @return String user name (first, last)
     */
    public String getUpdatedByString() {
        if (updatedBy == null) {
            return "";
        } else {
            return updatedBy.getName();
        }
    }

    /**
     * Get the time the Custom Field was last updated.
     * @return Date updatedTime
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set the time the Custom Field was last updated.
     * @param lastUpdated the time updated
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Get the last update time as a string.
     * @return the lastUpdated time as a string
     */
    public String getLastUpdatedString() {
        if (lastUpdated == null) {
            return "";
        } else {
            return DATE_FMT.format(lastUpdated);
        }
    }

    /**
     * Get the percentage of rows with values.
     * @return the percentage
     */
    public Integer getRowsWithValues() {
        return rowsWithValues;
    }

    /**
     * Set the percentage of rows with values.
     * @param values percentage of rows with data
     */
    public void setRowsWithValues(Integer values) {
        this.rowsWithValues = values;
    }

    //----- UTILITY METHOD -----

    /**
     * Return the given text with URLs in HTML anchor elements.
     * @param text the text to change
     * @return the new text
     */
    public String fixUrls(String text) {
        if (text == null) { return text; }

        String regex = "((http://|https://)([\\S]*))";
        String replace = "<a href=\"$1\" target=\"_blank\">$1</a>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        String s = m.replaceAll(replace);
        return s;
    }

    //----- CONSTANTS FOR SORTING -----

    /** Constant for the 'Name' column. */
    public static final String COLUMN_NAME = "Name";
    /** Constant for the 'Created By' column. */
    public static final String COLUMN_OWNER = "Created By";
    /** Constant for the 'Updated By' column. */
    public static final String COLUMN_UPDATED_BY = "Updated By";
    /** Constant for the 'Type' column. */
    public static final String COLUMN_TYPE = "Type";
    /** Constant for the 'Level' column. */
    public static final String COLUMN_LEVEL = "Level";
    /** Constant for the 'Rows with values' column. */
    public static final String COLUMN_ROWS_WITH = "Rows with values";

    /** Sort parameters array constant for sorting by name ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_NAME_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_OWNER_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_DATE_CREATED_ASC };
    /** Sort parameters array constant for sorting by name descending. */
    private static final CustomFieldDto.SortParameter[] DESC_NAME_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_NAME_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_OWNER_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_DATE_CREATED_ASC };
    /** Sort parameters array constant for sorting by Owner ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_OWNER_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_OWNER_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_DATE_CREATED_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Owner descending. */
    private static final CustomFieldDto.SortParameter[] DESC_OWNER_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_OWNER_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_DATE_CREATED_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Updated By ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_UPDATED_BY_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_UPDATED_BY_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Updated By descending. */
    private static final CustomFieldDto.SortParameter[] DESC_UPDATED_BY_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_UPDATED_BY_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_LAST_UPDATE_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Type ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_TYPE_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_TYPE_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Type descending. */
    private static final CustomFieldDto.SortParameter[] DESC_TYPE_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_TYPE_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Level ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_LEVEL_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_LEVEL_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Level descending. */
    private static final CustomFieldDto.SortParameter[] DESC_LEVEL_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_LEVEL_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Rows With ascending. */
    private static final CustomFieldDto.SortParameter[] ASC_ROWS_WITH_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_ROWS_WITH_ASC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };
    /** Sort parameters array constant for sorting by Rows With descending. */
    private static final CustomFieldDto.SortParameter[] DESC_ROWS_WITH_PARAMS = {
                         CustomFieldDto.SortParameter.SORT_BY_ROWS_WITH_DESC,
                         CustomFieldDto.SortParameter.SORT_BY_NAME_ASC };

    //----- METHODS FOR SORTING -----

    /**
     * Returns the relative path to the appropriate image.
     * @param columnName the column name to get an image for
     * @param sortByColumn the column to sort by
     * @param isAscending the directory to sort by
     * @return relative path to image for given column
     */
    public static String getSortImage(String columnName,
                                      String sortByColumn, Boolean isAscending) {

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
    public static CustomFieldDto.SortParameter[] getSortByParameters(String sortByColumn,
                                                                     Boolean isAscending) {

        CustomFieldDto.SortParameter[] sortParams = ASC_NAME_PARAMS;

        if (sortByColumn.equals(COLUMN_NAME)) {
            if (isAscending) {
                sortParams = ASC_NAME_PARAMS;
            } else {
                sortParams = DESC_NAME_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_OWNER)) {
            if (isAscending) {
                sortParams = ASC_OWNER_PARAMS;
            } else {
                sortParams = DESC_OWNER_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_UPDATED_BY)) {
            if (isAscending) {
                sortParams = ASC_UPDATED_BY_PARAMS;
            } else {
                sortParams = DESC_UPDATED_BY_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_TYPE)) {
            if (isAscending) {
                sortParams = ASC_TYPE_PARAMS;
            } else {
                sortParams = DESC_TYPE_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_LEVEL)) {
            if (isAscending) {
                sortParams = ASC_LEVEL_PARAMS;
            } else {
                sortParams = DESC_LEVEL_PARAMS;
            }
        } else if (sortByColumn.equals(COLUMN_ROWS_WITH)) {
            if (isAscending) {
                sortParams = ASC_ROWS_WITH_PARAMS;
            } else {
                sortParams = DESC_ROWS_WITH_PARAMS;
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
    public static Comparator<CustomFieldDto> getComparator(SortParameter... sortParameters) {
        return new CustomFieldDtoComparator(sortParameters);
    }

    public enum SortParameter {
        /** Name Ascending. */
        SORT_BY_NAME_ASC,
        /** Name Descending. */
        SORT_BY_NAME_DESC,
        /** Owner Ascending. */
        SORT_BY_OWNER_ASC,
        /** Owner Descending. */
        SORT_BY_OWNER_DESC,
        /** DateCreated Ascending. */
        SORT_BY_DATE_CREATED_ASC,
        /** DateCreated Descending. */
        SORT_BY_DATE_CREATED_DESC,
        /** UpdatedBy Ascending. */
        SORT_BY_UPDATED_BY_ASC,
        /** UpdatedBy Descending. */
        SORT_BY_UPDATED_BY_DESC,
        /** LastUpdate Ascending. */
        SORT_BY_LAST_UPDATE_ASC,
        /** LastUpdate Descending. */
        SORT_BY_LAST_UPDATE_DESC,
        /** Type Ascending. */
        SORT_BY_TYPE_ASC,
        /** Type Descending. */
        SORT_BY_TYPE_DESC,
        /** Level Ascending. */
        SORT_BY_LEVEL_ASC,
        /** Level Descending. */
        SORT_BY_LEVEL_DESC,
        /** RowsWithValues Ascending. */
        SORT_BY_ROWS_WITH_ASC,
        /** RowsWithValues Descending. */
        SORT_BY_ROWS_WITH_DESC
    }

    /**
     * Comparator for CustomFieldDto objects.
     */
    private static final class CustomFieldDtoComparator implements Comparator<CustomFieldDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private CustomFieldDtoComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first FileItem
         * @param o2 the second FileItem
         * @return comparator value
         */
        public int compare(CustomFieldDto o1, CustomFieldDto o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_NAME_ASC,
                        SortParameter.SORT_BY_OWNER_ASC,
                        SortParameter.SORT_BY_DATE_CREATED_ASC
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
                case SORT_BY_OWNER_ASC:
                    result = o1.getOwnerString().compareToIgnoreCase(o2.getOwnerString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_CREATED_ASC:
                    result = dateComparison(o1.getDateCreated(), o2.getDateCreated(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_BY_ASC:
                    result = o1.getUpdatedByString().compareToIgnoreCase(o2.getUpdatedByString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LAST_UPDATE_ASC:
                    result = dateComparison(o1.getLastUpdated(), o2.getLastUpdated(), true);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TYPE_ASC:
                    result = o1.getType().toString().compareTo(o2.getType().toString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LEVEL_ASC:
                    result = o1.getLevel().compareTo(o2.getLevel());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_ROWS_WITH_ASC:
                    result = o1.getRowsWithValues().compareTo(o2.getRowsWithValues());
                    if (result != 0) { return result; }
                    break;
                //--- Descending ---
                case SORT_BY_NAME_DESC:
                    result = o2.getName().compareToIgnoreCase(o1.getName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_OWNER_DESC:
                    result = o2.getOwnerString().compareToIgnoreCase(o1.getOwnerString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_CREATED_DESC:
                    result = dateComparison(o1.getDateCreated(), o2.getDateCreated(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPDATED_BY_DESC:
                    result = o2.getUpdatedByString().compareToIgnoreCase(o1.getUpdatedByString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LAST_UPDATE_DESC:
                    result = dateComparison(o1.getLastUpdated(), o2.getLastUpdated(), false);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TYPE_DESC:
                    result = o2.getType().toString().compareTo(o1.getType().toString());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_LEVEL_DESC:
                    result = o2.getLevel().compareTo(o1.getLevel());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_ROWS_WITH_DESC:
                    result = o2.getRowsWithValues().compareTo(o1.getRowsWithValues());
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
