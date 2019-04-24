/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.sampletodataset;

import java.util.Comparator;
import edu.cmu.pslc.datashop.dto.DTO;

/**
 * Used to transfer row data for the Samples page.
 *
 * @author Mike Komisin
 * @version $Revision: 10810 $
 * <BR>Last modified by: $Author:  $
 * <BR>Last modified on: $Date: 2014-03-17 14:07:52 -0400 (Mon, 17 Mar 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class SampleRowDto extends DTO {

    /** Sample row variables. */
    /** Owner id. */
    String ownerId;
    /** Owner email string. */
    String ownerEmail;
    /** Is the sample global. */
    Integer isGlobal;
    /** Sample id. */
    Integer sampleId;
    /** Sample name. */
    String sampleName;
    /** Sample description. */
    String description;
    /** Dataset authorization level. */
    String authLevel;
    /** Number of transactions. */
    Long numTransactions;
    /** Number of students. */
    Long numStudents;
    /** Number of problems. */
    Long numProblems;
    /** Number of steps. */
    Long numSteps;
    /** Number of unique steps. */
    Long numUniqueSteps;
    /** Total student hours. */
    Double totalStudentHours;
    /** Actionable icon string (html). */
    String actionableIconString;
    /** Pencil icon string (html). */
    String pencilIconString;
    /** Sample history list. */
    Boolean hasSampleHistory;
    /** Whether or not the sample requires aggregation. */
    Boolean requiresAgg;
    /** A list of previously created datasets for this sample. */

    /** Default constructor. */
    public SampleRowDto() {

    }

    /** Get the owner id.
     * @return the ownerId
     */
    public String getOwnerId() {
        return ownerId;
    }


    /** Set the owner id.
     * @param ownerId the ownerId to set
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    /** Get the is_global sample flag.
     * @return the isGlobal
     */
    public Integer getIsGlobal() {
        return isGlobal;
    }


    /** Set the is_global sample flag.
     * @param isGlobal the isGlobal to set
     */
    public void setIsGlobal(Integer isGlobal) {
        this.isGlobal = isGlobal;
    }


    /** Get the sample id.
     * @return the sampleId
     */
    public Integer getSampleId() {
        return sampleId;
    }


    /** Set the sample id.
     * @param sampleId the sampleId to set
     */
    public void setSampleId(Integer sampleId) {
        this.sampleId = sampleId;
    }


    /** Get the sample name.
     * @return the sampleName
     */
    public String getSampleName() {
        return sampleName;
    }


    /** Set the sample name.
     * @param sampleName the sampleName to set
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    /**
     * Get the sample description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the sample description.
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Get the dataset authorization level for this sample.
     * @return the authorization level
     */
    public String getAuthLevel() {
        return authLevel;
    }


    /** Set the dataset authorization level for this sample.
     * @param authLevel the authorization level to set
     */
    public void setAuthLevel(String authLevel) {
        this.authLevel = authLevel;
    }


    /** Get the number of transactions.
     * @return the numTransactions
     */
    public Long getNumTransactions() {
        return numTransactions;
    }


    /** Set the number of transactions.
     * @param numTransactions the numTransactions to set
     */
    public void setNumTransactions(Long numTransactions) {
        this.numTransactions = numTransactions;
    }


    /** Get the number of students.
     * @return the numStudents
     */
    public Long getNumStudents() {
        return numStudents;
    }


    /** Set the number of students.
     * @param numStudents the numStudents to set
     */
    public void setNumStudents(Long numStudents) {
        this.numStudents = numStudents;
    }


    /** Get the number of problems.
     * @return the numProblems
     */
    public Long getNumProblems() {
        return numProblems;
    }


    /** Set the number of problems.
     * @param numProblems the numProblems to set
     */
    public void setNumProblems(Long numProblems) {
        this.numProblems = numProblems;
    }


    /** Get the number of steps.
     * @return the numSteps
     */
    public Long getNumSteps() {
        return numSteps;
    }


    /** Set the number of steps.
     * @param numSteps the numSteps to set
     */
    public void setNumSteps(Long numSteps) {
        this.numSteps = numSteps;
    }


    /** Get the number of unique steps.
     * @return the numUniqueSteps
     */
    public Long getNumUniqueSteps() {
        return numUniqueSteps;
    }


    /** Set the number of unique steps.
     * @param numUniqueSteps the numUniqueSteps to set
     */
    public void setNumUniqueSteps(Long numUniqueSteps) {
        this.numUniqueSteps = numUniqueSteps;
    }


    /** Get the total student hours.
     * @return the totalStudentHours
     */
    public Double getTotalStudentHours() {
        return totalStudentHours;
    }


    /** Set the total student hours.
     * @param totalStudentHours the totalStudentHours to set
     */
    public void setTotalStudentHours(Double totalStudentHours) {
        this.totalStudentHours = totalStudentHours;
    }

    /** Get requiresAgg flag.
     * @return the requiresAgg flag
     */
    public Boolean getRequiresAgg() {
        return requiresAgg;
    }


    /** Set the requiresAgg flag.
     * @param requiresAgg the requiresAgg flag to set
     */
    public void setRequiresAgg(Boolean requiresAgg) {
        this.requiresAgg = requiresAgg;
    }

    /**
     * Displays either an up or down arrow.
     * @param sortByParam the sort by parameter (column header to sort by)
     * @param column the actual column header selected
     * @param ascFlag is ascending
     * @return the path to the appropriate image
     */
    public static String showSortOrder(String sortByParam, String column, Boolean ascFlag) {
        String imgIcon = "images/trans_spacer.gif";
        if (sortByParam != null && sortByParam.equals(column)) {
            imgIcon = ascFlag
                    ? "images/grid/up.gif" : "images/grid/down.gif";
        }
        return imgIcon;
    }

    /**
     * Defines which sorting parameters to use for sorting UserRequestDTO's
     * based on an user selected column; handles ascending or descending.
     * @param sortByString name of the column to sort by
     * @param isAscending flag indication ascending or descending sort
     * @return the SortParameter array
     */
    public static SortParameter[] selectSortParameters(String sortByString, Boolean isAscending) {
        // Assign sort parameters based on the column the user elects to sort.
        if (sortByString.equals(COLUMN_SAMPLE_NAME)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_SAMPLE_NAME_DESC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_OWNER_ID)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_OWNER_ID_ASC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_OWNER_ID_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_NUM_TRANSACTIONS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_TRANSACTIONS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_TRANSACTIONS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_NUM_STUDENTS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_STUDENTS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_STUDENTS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_NUM_PROBLEMS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_PROBLEMS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_PROBLEMS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_NUM_STEPS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_STEPS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_STEPS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_NUM_UNIQUE_STEPS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_UNIQUE_STEPS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_NUM_UNIQUE_STEPS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        } else if (sortByString.equals(COLUMN_TOTAL_STUDENT_HOURS)) {
            if (isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_TOTAL_STUDENT_HOURS_ASC,
                                              SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            } else if (!isAscending) {
                SortParameter[] sortParams = { SortParameter.SORT_BY_TOTAL_STUDENT_HOURS_DESC,
                                                SortParameter.SORT_BY_SAMPLE_NAME_ASC };
                return sortParams;
            }
        }
        return null;
    }

    /** A column header for the sample rows. */
    public static final String COLUMN_SAMPLE_NAME = "Sample Name";
    /** A column header for the sample rows. */
    public static final String COLUMN_OWNER_ID = "Owner";
    /** A column header for the sample rows. */
    public static final String COLUMN_NUM_TRANSACTIONS = "Transactions";
    /** A column header for the sample rows. */
    public static final String COLUMN_NUM_STUDENTS = "Students";
    /** A column header for the sample rows. */
    public static final String COLUMN_NUM_PROBLEMS = "Problems";
    /** A column header for the sample rows. */
    public static final String COLUMN_NUM_STEPS = "Steps";
    /** A column header for the sample rows. */
    public static final String COLUMN_NUM_UNIQUE_STEPS = "Unique Steps";
    /** A column header for the sample rows. */
    public static final String COLUMN_TOTAL_STUDENT_HOURS = "Total Student Hours";


    /**
     * Comparator object used for sorting by parameters.
     * @param sortParameters the sort parameters
     * @return the comparator
     */
    public static Comparator<SampleRowDto> getComparator(SortParameter... sortParameters) {
        return new SampleComparator(sortParameters);
    }

    /** The sort parameters. */
    public enum SortParameter {
        /* Ascending sorts. */
        /** Sort by owner id ascending. */
        SORT_BY_OWNER_ID_ASC,
        /** Sort by sample name ascending. */
        SORT_BY_SAMPLE_NAME_ASC,
        /** Sort by num transactions ascending. */
        SORT_BY_NUM_TRANSACTIONS_ASC,
        /** Sort by num students ascending. */
        SORT_BY_NUM_STUDENTS_ASC,
        /** Sort by num problems ascending. */
        SORT_BY_NUM_PROBLEMS_ASC,
        /** Sort by num steps ascending. */
        SORT_BY_NUM_STEPS_ASC,
        /** Sort by num unique steps ascending. */
        SORT_BY_NUM_UNIQUE_STEPS_ASC,
        /** Sort by total student hours ascending. */
        SORT_BY_TOTAL_STUDENT_HOURS_ASC,
        /* Descending sorts. */
        /** Sort by owner id descending. */
        SORT_BY_OWNER_ID_DESC,
        /** Sort by sample name descending. */
        SORT_BY_SAMPLE_NAME_DESC,
        /** Sort by num transactions descending. */
        SORT_BY_NUM_TRANSACTIONS_DESC,
        /** Sort by num students descending. */
        SORT_BY_NUM_STUDENTS_DESC,
        /** Sort by num problems descending. */
        SORT_BY_NUM_PROBLEMS_DESC,
        /** Sort by num steps descending. */
        SORT_BY_NUM_STEPS_DESC,
        /** Sort by num unique steps descending. */
        SORT_BY_NUM_UNIQUE_STEPS_DESC,
        /** Sort by total student hours descending. */
        SORT_BY_TOTAL_STUDENT_HOURS_DESC
    }

    /**
     * A class that supports comparison between two ProjectRequestDTO's
     * using sort attributes supplied to the constructor.
     *
     */
    private static final class SampleComparator implements Comparator<SampleRowDto> {
        /** Sort parameters. */
        private SortParameter[] parameters;
        /**
         * Constructor.
         * @param parameters the sort parameters
         */
        private SampleComparator(SortParameter[] parameters) {
            this.parameters = parameters;
        }



        /**
         * Comparator.
         * @param o1 the first object being compared
         * @param o2 the second object being compared
         * @return the comparator value
         */
        public int compare(SampleRowDto o1, SampleRowDto o2) {
            if (parameters == null) {
                SortParameter[] param = {SortParameter.SORT_BY_SAMPLE_NAME_ASC};
                parameters = param;
            }

            int comparison = 0;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case SORT_BY_OWNER_ID_ASC:
                        comparison = o1.getOwnerId().compareToIgnoreCase(o2.getOwnerId());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_SAMPLE_NAME_ASC:
                        comparison = o1.getSampleName().compareToIgnoreCase(o2.getSampleName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_TRANSACTIONS_ASC:
                        comparison = o1.getNumTransactions().compareTo(o2.getNumTransactions());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_STUDENTS_ASC:
                        comparison = o1.getNumStudents().compareTo(o2.getNumStudents());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_PROBLEMS_ASC:
                        comparison = o1.getNumProblems().compareTo(o2.getNumProblems());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_STEPS_ASC:
                        comparison = o1.getNumSteps().compareTo(o2.getNumSteps());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_UNIQUE_STEPS_ASC:
                        comparison = o1.getNumUniqueSteps().compareTo(o2.getNumUniqueSteps());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOTAL_STUDENT_HOURS_ASC:
                        if (o1.getTotalStudentHours() == null && o2.getTotalStudentHours() == null) {
                            break;
                        } else if (o2.getTotalStudentHours() == null) {
                            return 1;
                        } else if (o1.getTotalStudentHours() == null) {
                            return -1;
                        }
                        comparison = o1.getTotalStudentHours().compareTo(o2.getTotalStudentHours());
                        if (comparison != 0) { return comparison; }
                        break;
                    // Descending
                    case SORT_BY_OWNER_ID_DESC:
                        comparison = o2.getOwnerId().compareToIgnoreCase(o1.getOwnerId());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_SAMPLE_NAME_DESC:
                        comparison = o2.getSampleName().compareToIgnoreCase(o1.getSampleName());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_TRANSACTIONS_DESC:
                        comparison = o2.getNumTransactions().compareTo(o1.getNumTransactions());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_STUDENTS_DESC:
                        comparison = o2.getNumStudents().compareTo(o1.getNumStudents());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_PROBLEMS_DESC:
                        comparison = o2.getNumProblems().compareTo(o1.getNumProblems());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_STEPS_DESC:
                        comparison = o2.getNumSteps().compareTo(o1.getNumSteps());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_NUM_UNIQUE_STEPS_DESC:
                        comparison = o2.getNumUniqueSteps().compareTo(o1.getNumUniqueSteps());
                        if (comparison != 0) { return comparison; }
                        break;
                    case SORT_BY_TOTAL_STUDENT_HOURS_DESC:
                        if (o1.getTotalStudentHours() == null && o2.getTotalStudentHours() == null) {
                            break;
                        } else if (o2.getTotalStudentHours() == null) {
                            return -1;
                        } else if (o1.getTotalStudentHours() == null) {
                            return 1;
                        }
                        comparison = o2.getTotalStudentHours().compareTo(o1.getTotalStudentHours());
                        if (comparison != 0) { return comparison; }
                        break;

                    default:
                        // Nothing is default
                }
            }

            return 0;
        }
    }

    /**
     * Set the owner email string.
     * @param ownerEmail
     */
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;

    }

    /**
     * Get the owner email string.
     * @return the ownerEmail
     */
    public String getOwnerEmail() {
        return ownerEmail;

    }

    /**
     * Set the pencil icon string.
     * @param pencilIconString
     */
    public void setPencilIcon(String pencilIconString) {
        this.pencilIconString = pencilIconString;

    }

    /**
     * Get the pencil icon string.
     * @return the pencilIconString
     */
    public String getPencilIcon() {
        return pencilIconString;

    }

    /**
     * Set the actionable icon string.
     * @param actionableIconString
     */
    public void setActionableIcons(String actionableIconString) {
        this.actionableIconString = actionableIconString;

    }

    /**
     * Get the actionable icon string.
     * @return the actionableIconString
     */
    public String getActionableIcons() {
        return actionableIconString;

    }

    /**
     * Returns a list of SampleHistoryDto's.
     * @return a list of SampleHistoryDto's
     */
    public Boolean hasSampleHistory() {
        return hasSampleHistory;
    }

    /**
     * Sets the Sample History.
     * @param sampleHistory a list of SampleHistoryDto's
     */
    public void hasSampleHistory(Boolean hasSampleHistory) {
        this.hasSampleHistory = hasSampleHistory;
    }

}
