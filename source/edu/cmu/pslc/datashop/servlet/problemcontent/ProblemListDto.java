/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2014
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * This is a POJO for the project list subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 11110 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-02 14:05:39 -0400 (Mon, 02 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemListDto {

    //----- ATTRIBUTES -----

    /** Message. */
    private String message;
    /** Message level. */
    private String messageLevel;
    /** List of hierarchies. */
    private List<String> hierarchyList;
    /** Map of problems to hierarchy. */
    private Map<String, List<ProblemItem>> problemMap;
    /** Number of problem hierarchies that match filter. */
    private Integer numProblemHierarchies;
    /** Total number of problem hierarchies. */
    private Integer numProblemHierarchiesTotal;
    /** Total number of problems. */
    private Long numProblemsTotal;
    /** Number of pages in 'problem list'. */
    private Integer numPages;
    /** Flag indicating if download is enabled. */
    private Boolean downloadEnabled;

    /** Constant for the success message level : SUCCESS. */
    public static final String STATUS_MESSAGE_LEVEL_SUCCESS = "SUCCESS";
    /** Constant for the success message level : WARN. */
    public static final String STATUS_MESSAGE_LEVEL_WARN = "WARN";
    /** Constant for the success message level : ERROR. */
    public static final String STATUS_MESSAGE_LEVEL_ERROR = "ERROR";

    /** Constant for the mapped problem content. */
    public static final String PROBLEM_CONTENT_MAPPED = "only problems with problem content";
    /** Constant for the un-mapped problem content. */
    public static final String PROBLEM_CONTENT_UNMAPPED = "only problems without problem content";
    /** Constant for both mapped and un-mapped problem content. */
    public static final String PROBLEM_CONTENT_BOTH = "all problems";

    //----- CONSTRUCTOR -----

    /**
     * Default constructor.
     */
    public ProblemListDto() { }

    /**
     * Constructor.
     * @param hierarchyList the list of problem hierarchies
     * @param problemMap map of hierarchies to problems
     */
    public ProblemListDto(List<String> hierarchyList, Map<String, List<ProblemItem>> problemMap) {
        setHierarchyList(hierarchyList);
        setProblemMap(problemMap);
    }

    //----- GETTERS and SETTERS -----

    /**
     * Get the list of problem hierarchies.
     * @return the list
     */
    public List<String> getHierarchyList() {
        return hierarchyList;
    }

    /**
     * Set the list of problem hierarchies.
     * @param hierarchyList the list of problem hierarchies
     */
    public void setHierarchyList(List<String> hierarchyList) {
        this.hierarchyList = hierarchyList;
    }

    /**
     * Get the map of hierarchies to problems.
     * @return the map
     */
    public Map<String, List<ProblemItem>> getProblemMap() {
        return problemMap;
    }

    /**
     * Set the map of hiearchies to problems.
     * @param problemMap the map
     */
    public void setProblemMap(Map<String, List<ProblemItem>> problemMap) {
        this.problemMap = problemMap;
    }
        
    /**
     * Get the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the messageLevel.
     * @return the messageLevel
     */
    public String getMessageLevel() {
        return messageLevel;
    }

    /**
     * Set the messageLevel.
     * @param messageLevel the messageLevel to set
     */
    public void setMessageLevel(String messageLevel) {
        this.messageLevel = messageLevel;
    }

    /**
     * Get the number of problem hierarchies to be displayed.
     * This is the number matching the filter criteria or the
     * total if filtering is not being used.
     * @return Integer number of hierarchies
     */
    public Integer getNumProblemHierarchies() {
        return numProblemHierarchies;
    }

    /**
     * Set the number of problem hierarchies to be displayed.
     * This is the number matching the filter criteria or the
     * total if filtering is not being used.
     * @param numHierarchies the number of hierarchies
     */
    public void setNumProblemHierarchies(Integer numHierarchies) {
        this.numProblemHierarchies = numHierarchies;
    }

    /**
     * Get the total number of problem hierarchies.
     * @return Integer number of hierarchies
     */
    public Integer getNumProblemHierarchiesTotal() {
        return numProblemHierarchiesTotal;
    }

    /**
     * Set the total number of problem hierarchies.
     * @param numHierarchiesTotal the number of hierarchies
     */
    public void setNumProblemHierarchiesTotal(Integer numHierarchies) {
        this.numProblemHierarchiesTotal = numHierarchies;
    }

    /**
     * Get the total number of problems.
     * @return Integer num problems
     */
    public Long getNumProblemsTotal() {
        return numProblemsTotal;
    }

    /**
     * Set the total number of problems.
     * @param numProblems the number of problems
     */
    public void setNumProblemsTotal(Long numProblems) {
        this.numProblemsTotal = numProblems;
    }

    /**
     * Get the number of 'problem list' pages.
     * @return Integer num pages
     */
    public Integer getNumPages() {
        return numPages;
    }

    /**
     * Set the number of 'problem list' pages.
     * @param numPages the num pages
     */
    public void setNumPages(Integer numPages) {
        this.numPages = numPages;
    }

    /**
     * Get flag indicating if the 'Download' button should be
     * enabled. Only true when Problem Content is present.
     * @return Boolean flag
     */
    public Boolean getDownloadEnabled() {
        return downloadEnabled;
    }

    /**
     * Set flag indicating if the 'Download' button should be
     * enabled. Only true when Problem Content is present.
     * @param downloadEnabled flag
     */
    public void setDownloadEnabled(Boolean downloadEnabled) {
        this.downloadEnabled = downloadEnabled;
    }
}
