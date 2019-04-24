/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005-2006
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import java.util.Comparator;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.FileItem.SortParameter;
import edu.cmu.pslc.datashop.item.PaperItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Simple class to hold a paper item, its attached file item and it's owner.
 *
 * @author Alida Skogsholm
 * @version $Revision: 7541 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-23 11:52:32 -0400 (Fri, 23 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PaperFile implements java.io.Serializable {
    /** The paper item. */
    private PaperItem paperItem;
    /** The file item. */
    private FileItem fileItem;
    /** The owner of the file */
    private UserItem owner;

    /**
     * Constructor.
     * @param paperItem the paper item
     * @param fileItem the file item
     * @param owner the UserItem owner of the paper.
     */
    public PaperFile(PaperItem paperItem, FileItem fileItem, UserItem owner) {
        this.paperItem = paperItem;
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
     * Returns the paperItem.
     * @return the paperItem
     */
    public PaperItem getPaperItem() {
        return paperItem;
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
     * @param preferredPaper the preferred paper, for comparison
     * @return the comparator
     */
    public static Comparator<PaperFile> getComparator(PaperItem preferredPaper,
            SortParameter... sortParameters) {
        return new PaperFileComparator(sortParameters, preferredPaper);
    }

    /**
     * A class that supports comparison between two FileItem
     * objects using sort parameters specified in the ctor.
     */
    private static final class PaperFileComparator implements Comparator<PaperFile> {
        /** Sort parameters. */
        private SortParameter[] parameters;
        /** Preferred paper for this Dataset, if it exists. */
        private PaperItem preferredPaper;
        /** Logger */
        private Logger logger = Logger.getLogger(getClass().getName());

        /**
         * Constructor.
         * @param params the sort parameters.
         * @param preferredPaper the preferred paper for this Dataset.
         */
        private PaperFileComparator(SortParameter[] params, PaperItem preferredPaper) {
            this.parameters = params;
            this.preferredPaper = preferredPaper;
        }

        /**
         * Comparator.
         * @param o1 the first PaperItem
         * @param o2 the second PaperItem
         * @return comparator value
         */
        public int compare(PaperFile o1, PaperFile o2) {
            if (parameters == null) {
                SortParameter[] params = {
                        SortParameter.SORT_BY_PREF_CITATION_ASC,
                        SortParameter.SORT_BY_CITATION_ASC,
                        SortParameter.SORT_BY_FILE_NAME_ASC
                        };
                parameters = params;
            }

            int result = 0;

            for (SortParameter sp : parameters) {
                switch (sp) {
                case SORT_BY_PREF_CITATION_ASC:
                    result = sortByPreferredCitation(o1, o2);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CITATION_ASC:
                    result = o1.getPaperItem().getCitation().compareToIgnoreCase(
                            o2.getPaperItem().getCitation());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_ASC:
                    result = o1.getFileItem().getFileName().compareToIgnoreCase(
                            o2.getFileItem().getFileName());
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
                case SORT_BY_PREF_CITATION_DESC:
                    result = sortByPreferredCitation(o2, o1);
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_CITATION_DESC:
                    result = o2.getPaperItem().getCitation().compareToIgnoreCase(
                            o1.getPaperItem().getCitation());
                    if (result != 0) { return result; }
                    break;
                case SORT_BY_FILE_NAME_DESC:
                    result = o2.getFileItem().getFileName().compareToIgnoreCase(
                            o1.getFileItem().getFileName());
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

        /**
         * Compare two PaperFile objects according to Preferred Citation.
         * @param o1 the first PaperFile
         * @param o2 the second PaperFile
         * @return comparator value
         */
        private int sortByPreferredCitation(PaperFile o1, PaperFile o2) {
            if (preferredPaper == null) { return 0; }

            PaperItem pi1 = o1.getPaperItem();
            PaperItem pi2 = o2.getPaperItem();
            if (pi1.getId().equals(preferredPaper.getId())) { return -1; }
            if (pi2.getId().equals(preferredPaper.getId())) { return 1; }
            return 0;
        }
    }
}
