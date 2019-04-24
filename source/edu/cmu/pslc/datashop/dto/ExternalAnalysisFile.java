/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Comparator;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.FileItem.SortParameter;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Simple class to hold an external analysis item, its attached file item and it's owner.
 *
 * @author Cindy Tipper
 * @version $Revision: 7564 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-28 09:53:02 -0400 (Wed, 28 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisFile implements java.io.Serializable {
    /** The external analysis item. */
    private ExternalAnalysisItem externalAnalysisItem;
    /** The file item. */
    private FileItem fileItem;
    /** The owner of the file */
    private UserItem owner;

    /** Less than value used for comparator */
    private static final Integer LESS_THAN = -1;
    /** Greater than value used for comparator */
    private static final Integer GREATER_THAN = 1;

    /**
     * Constructor.
     * @param externalAnalysisItem the external analysis item
     * @param fileItem the file item
     * @param owner the UserItem owner of the external analysis.
     */
    public ExternalAnalysisFile(ExternalAnalysisItem externalAnalysisItem,
            FileItem fileItem, UserItem owner) {
        this.externalAnalysisItem = externalAnalysisItem;
        this.fileItem = fileItem;
        this.owner = owner;
    }
    /**
     * Returns the fileItem.
     * @return the fileItem
     */
    public FileItem getFileItem() {
        return fileItem;
    }
    /**
     * Returns the externalAnalysisItem.
     * @return the externalAnalysisItem
     */
    public ExternalAnalysisItem getExternalAnalysisItem() {
        return externalAnalysisItem;
    }

    /**
     * Returns the owner as a UserItem.
     * @return UserItem of the owner.
     */
    public UserItem getOwner() {
        return owner;
    }

    /**
     * Comparator object used for sorting.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<ExternalAnalysisFile> getComparator(SortParameter... sortParameters) {
        return new ExternalAnalysisFileComparator(sortParameters);
    }

    /**
     * A class that supports comparison between two FileItem
     * objects using sort parameters specified in the ctor.
     */
    private static final class ExternalAnalysisFileComparator
        implements Comparator<ExternalAnalysisFile> {
        /** Sort parameters. */
        private SortParameter[] parameters;

        /**
         * Constructor.
         * @param params the sort parameters.
         */
        private ExternalAnalysisFileComparator(SortParameter[] params) {
            this.parameters = params;
        }

        /**
         * Comparator.
         * @param o1 the first ExternalAnalysisItem
         * @param o2 the second ExternalAnalysisItem
         * @return comparator value
         */
        public int compare(ExternalAnalysisFile o1, ExternalAnalysisFile o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_TITLE_ASC,
                        SortParameter.SORT_BY_FILE_NAME_ASC
                        };
                parameters = params;
            }

            ExternalAnalysisItem eaItem1 = o1.getExternalAnalysisItem();
            ExternalAnalysisItem eaItem2 = o2.getExternalAnalysisItem();

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                case SORT_BY_TITLE_ASC:
                    result = o1.getFileItem().getTitle().compareToIgnoreCase(
                            o2.getFileItem().getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_ASC:
                    result = o1.getFileItem().getFileName().compareToIgnoreCase(
                            o2.getFileItem().getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_KC_MODEL_ASC:
                    if (eaItem1.getSkillModelName() == null
                        && eaItem2.getSkillModelName() == null) {
                        result = 0;
                    } else if (eaItem1.getSkillModelName() == null) {
                        result = LESS_THAN;
                    } else if (eaItem2.getSkillModelName() == null) {
                        result = GREATER_THAN;
                    } else {
                        result = eaItem1.getSkillModelName().compareToIgnoreCase(
                                eaItem2.getSkillModelName());
                    }
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_STATISTICAL_MODEL_ASC:
                    if (eaItem1.getStatisticalModel() == null
                        && eaItem2.getStatisticalModel() == null) {
                        result = 0;
                    } else if (eaItem1.getStatisticalModel() == null) {
                        result = LESS_THAN;
                    } else if (eaItem2.getStatisticalModel() == null) {
                        result = GREATER_THAN;
                    } else {
                        result = eaItem1.getStatisticalModel().compareToIgnoreCase(
                                eaItem2.getStatisticalModel());
                    }
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPLOADED_BY_ASC:
                    result = o1.getOwner().compareTo(o2.getOwner());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_ASC:
                    result = o1.getFileItem().getAddedTime().compareTo(
                            o2.getFileItem().getAddedTime());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_TITLE_DESC:
                    result = o2.getFileItem().getTitle().compareToIgnoreCase(
                            o1.getFileItem().getTitle());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_DESC:
                    result = o2.getFileItem().getFileName().compareToIgnoreCase(
                            o1.getFileItem().getFileName());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_KC_MODEL_DESC:
                    if (eaItem1.getSkillModelName() == null
                        && eaItem2.getSkillModelName() == null) {
                        result = 0;
                    } else if (eaItem1.getSkillModelName() == null) {
                        result = GREATER_THAN;
                    } else if (eaItem2.getSkillModelName() == null) {
                        result = LESS_THAN;
                    } else {
                        result = eaItem2.getSkillModelName().compareToIgnoreCase(
                                eaItem1.getSkillModelName());
                    }
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_STATISTICAL_MODEL_DESC:
                    if (eaItem1.getStatisticalModel() == null
                        && eaItem2.getStatisticalModel() == null) {
                        result = 0;
                    } else if (eaItem1.getStatisticalModel() == null) {
                        result = GREATER_THAN;
                    } else if (eaItem2.getStatisticalModel() == null) {
                        result = LESS_THAN;
                    } else {
                        result = eaItem2.getStatisticalModel().compareToIgnoreCase(
                                eaItem1.getStatisticalModel());
                    }
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_UPLOADED_BY_DESC:
                    result = o2.getOwner().compareTo(o1.getOwner());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_DATE_DESC:
                    result = o2.getFileItem().getAddedTime().compareTo(
                            o1.getFileItem().getAddedTime());
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
