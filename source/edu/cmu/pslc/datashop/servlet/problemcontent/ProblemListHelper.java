/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.servlet.problemcontent;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.DatasetDao;
import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.ProblemItem;

import edu.cmu.pslc.datashop.servlet.HelperFactory;

/**
 * Helper to get data for the Dataset Info -> Problem List subtab.
 *
 * @author Cindy Tipper
 * @version $Revision: 11110 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-06-02 14:05:39 -0400 (Mon, 02 Jun 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemListHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** Default constructor. */
    public ProblemListHelper() { }

    /**
     * Helper method to generate DTO necessary to display 'Problem List'
     * for a specific dataset.
     * @param datasetId the dataset id
     * @param context the ProblemListContext
     * @return ProblemListDto
     */
    public ProblemListDto getProblemListDto(Integer datasetId, ProblemListContext context) {
        ProblemListDto result = new ProblemListDto();

        DatasetDao dsDao = DaoFactory.DEFAULT.getDatasetDao();
        DatasetItem dataset = dsDao.get(datasetId);

        String searchBy = context.getSearchBy();

        // Determine if query is for mapped, unmapped or both.
        String pcOption = context.getProblemContent();

        // Set number matching filter, regardless of paging.
        ProblemDao pDao = DaoFactory.DEFAULT.getProblemDao();
        Map<String, List<ProblemItem>> problemMap =
            pDao.getProblemsByHierarchyMap(dataset, searchBy, pcOption, 0, 0);
        int filteredHierarchyCount = problemMap.size();

        int rowsPerPage = context.getRowsPerPage();

        // If the 'rowsPerPage' value is larger than the total number of
        // matching rows, set the 'currentPage' to 1.
        if (rowsPerPage > filteredHierarchyCount) {
            context.setCurrentPage(ProblemListContext.DEFAULT_CURRENT_PAGE);
        }
        int currentPage = context.getCurrentPage();
        int offset = (currentPage - 1) * rowsPerPage;

        problemMap = pDao.getProblemsByHierarchyMap(dataset, searchBy,
                                                    pcOption, offset, rowsPerPage);
        result.setProblemMap(problemMap);

        List<String> hierarchyList = new ArrayList<String>(problemMap.size());
        hierarchyList.addAll(problemMap.keySet());
        result.setHierarchyList(hierarchyList);

        result.setNumProblemHierarchies(filteredHierarchyCount);

        // Set totals, without filtering.
        result.setNumProblemHierarchiesTotal(pDao.getNumHierarchies(dataset));
        result.setNumProblemsTotal(pDao.getNumProblems(dataset));

        result.setNumPages(calcNumPages(filteredHierarchyCount, rowsPerPage));

        // Determine if Problem Content is present at all.
        ProblemContentHelper pcHelper = HelperFactory.DEFAULT.getProblemContentHelper();
        result.setDownloadEnabled(pcHelper.isProblemContentAvailable(dataset));
        
        return result;
    }

    /**
     * Helper method to calculate total number of pages.
     * @param numRecords number of total records
     * @param entriesPerPage the number of items per page
     * @return int the offset
     */
    private int calcNumPages(int numRecords, int entriesPerPage) {
        return (int)Math.ceil((float)numRecords / (float)entriesPerPage);
    }
}
