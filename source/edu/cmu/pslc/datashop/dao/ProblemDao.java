/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

/**
 * Problem Data Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 11366 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-07-23 09:59:37 -0400 (Wed, 23 Jul 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProblemDao extends AbstractDao {

    /**
     * Standard get for a ProblemItem by id.
     * @param id The id of the ProblemItem.
     * @return the matching ProblemItem or null if none found
     */
    ProblemItem get(Long id);

    /**
     * Standard find for an ProblemItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ProblemItem.
     * @return the matching ProblemItem.
     */
    ProblemItem find(Long id);

    /**
     * Standard "find all" for ProblemItems.
     * @return a List of objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Returns Problem(s) given a name.
     * @param name name of problem
     * @return Collection of ProblemItem
     */
    Collection find(String name);

    /**
     * Gets a list of all problems in the dataset.
     * @param dataset the dataset to get all problems for
     * @return a list of ProblemItems.
     */
    List find(DatasetItem dataset);

    /**
     * Gets a list of problems in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find problems in.
     * @param matchAny boolean value indicating whether to only look for problems that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching problem items sorted by name.
     */
    List<ProblemItem> findMatchingByName(String toMatch, DatasetItem dataset, boolean matchAny);

    /**
     * Gets a list of problems in the dataset that match all or a portion of the
     * description parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find problems in.
     * @param matchAny boolean value indicating whether to only look for problems that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching problem items sorted by name.
     */
    List<ProblemItem> findMatchingByDescription(String toMatch, DatasetItem dataset,
            boolean matchAny);

    /**
     * Gets a list of problems in the given dataset level that match all of the name parameter.
     * @param datasetLevel the datasetLevel item to find problems in
     * @param nameMatch the problem name to match exactly
     * @return List of all matching problem items sorted by name.
     */
    List findMatchingByLevelAndName(DatasetLevelItem datasetLevel, String nameMatch);

    /**
     * Get a map of problems by hierarchy for the specified dataset.
     * @param dataset the Dataset item
     * @param searchBy the string to filter by
     * @param problemContent filter by mapped, unmapped or both
     * @param offset the page offset
     * @param rowsPerPage the number of hierarchies displayed
     * @return map of Problem items by hiearchy
     */
    Map<String, List<ProblemItem>> getProblemsByHierarchyMap(DatasetItem dataset, String searchBy,
                                                             String problemContent,
                                                             int offset, int rowsPerPage);

    /**
     * Get the problem hierarchy for the specified problem.
     * @param problem the ProblemItem
     * @return String the hierarchy
     */
    String getHierarchy(ProblemItem problem);

    /**
     * Get the number of problems for the specified dataset.
     * @param dataset the Dataset item
     * @return Long problem count
     */
    Long getNumProblems(DatasetItem dataset);

    /**
     * Get the number of problem hierarchies for the specified dataset.
     * @param dataset the Dataset item
     * @return Integer hierarchy count
     */
    Integer getNumHierarchies(DatasetItem dataset);

    /**
     * Get the number of unmapped problems for the specified dataset.
     * @param dataset the Dataset item
     * @return Long unmapped problem count
     */
    Long getNumUnmappedProblems(DatasetItem dataset);

    /**
     * Get the list of unmapped problems for the specified dataset.
     * @param dataset the Dataset item
     * @return List of all unmapped problems
     */
    List<ProblemItem> getUnmappedProblems(DatasetItem dataset);

    /**
     * Get the list of mapped problems for the specified conversion and dataset.
     * @param pcConversion the PcConversion Item
     * @param dataset the Dataset item
     * @return List of the mapped problems
     */
    List<ProblemItem> getMappedProblemsByConversion(PcConversionItem pcConversion,
                                                    DatasetItem dataset);

    /**
     * This is a findOrCreate method that excludes problem description as a unique identifier.
     * If no problem names match, then create the new problem; Otherwise, do not.
     * This method depends on the problemItems from datasetLevelItem.getProblemsExternal().
     * @link DatasetLevelItem
     * @param problemItems the problem items to search -
     * @param newProblemItem the new problem item
     * @return an existing item
     */
    public ProblemItem findOrCreateIgnoreDescription(Collection<ProblemItem> problemItems,
            ProblemItem newProblemItem);
}
