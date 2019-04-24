/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.ProblemDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProblemItem;
import edu.cmu.pslc.datashop.servlet.problemcontent.ProblemListDto;

/**
 * Hibernate and Spring implementation of the ProblemDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 13711 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-12-03 16:57:33 -0500 (Sat, 03 Dec 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProblemDaoHibernate extends AbstractDaoHibernate implements ProblemDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a ProblemItem by id.
     * @param id The id of the user.
     * @return the matching ProblemItem or null if none found
     */
    public ProblemItem get(Long id) {
        return (ProblemItem)get(ProblemItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(ProblemItem.class);
    }

    /**
     * Standard find for an ProblemItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ProblemItem.
     * @return the matching ProblemItem.
     */
    public ProblemItem find(Long id) {
        return (ProblemItem)find(ProblemItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Gets a list of all problems in the dataset.
     * @param dataset the dataset to get all problems for
     * @return  a list of projectItems
     */
    public List find(DatasetItem dataset) {
        StringBuffer query = new StringBuffer();
        query.append("select problem from " + ProblemItem.class.getName() + " problem");
        query.append(" join problem.datasetLevel level");
        query.append(" join level.dataset dat");
        query.append(" where dat.id = " + dataset.getId());
        if (logger.isDebugEnabled()) {
            logger.debug("findAll(DatasetItem) called with query: " + query);
        }
        return getHibernateTemplate().find(query.toString());
    }

    /**
     * Returns Problem(s) given a name.
     * @param name name of problem
     * @return Collection of ProblemItem
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from ProblemItem problem where problem.problemName = ?", name);
    }

    /**
     * Gets a list of problems in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find problems in.
     * @param matchAny boolean value indicating whether to only look for problems that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching problem items sorted by name.
     */
    public List findMatchingByName(String toMatch, DatasetItem dataset, boolean matchAny) {

        DetachedCriteria query = DetachedCriteria.forClass(ProblemItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("problemName", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("problemName", toMatch, MatchMode.START));
        }
        query.createCriteria("datasetLevel")
             .createCriteria("dataset")
             .add(Restrictions.eq("id", dataset.getId()));
        query.addOrder(Property.forName("problemName").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of problems in the dataset that match all or a portion of the
     * description parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find problems in.
     * @param matchAny boolean value indicating whether to only look for problems that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching problem items sorted by description.
     */
    public List findMatchingByDescription(String toMatch, DatasetItem dataset, boolean matchAny) {
        DetachedCriteria query = DetachedCriteria.forClass(ProblemItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("problemDescription", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("problemDescription", toMatch, MatchMode.START));
        }
        query.createCriteria("datasetLevel")
             .createCriteria("dataset")
             .add(Restrictions.eq("id", dataset.getId()));
        query.addOrder(Property.forName("problemDescription").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of problems in the given dataset level that match all of the name parameter.
     * @param datasetLevel the datasetLevel item to find problems in
     * @param nameMatch the problem name to match exactly
     * @return List of all matching problem items sorted by name.
     */
    public List findMatchingByLevelAndName(DatasetLevelItem datasetLevel, String nameMatch) {
        DetachedCriteria query = DetachedCriteria.forClass(ProblemItem.class);
        query.add(Restrictions.ilike("problemName", nameMatch, MatchMode.START));
        query.createCriteria("datasetLevel").add(Restrictions.eq("id", datasetLevel.getId()));
        return getHibernateTemplate().findByCriteria(query);
    }

    /** Constant for separator in problem id list. */
    private static final String SEPARATOR = ",";

    /** Constant SQL query for problem hierarchies and problem lists per hierarchy. */
    private static final String PROBLEM_HIERARCHY_MAP_QUERY =
        "SELECT DISTINCT(hierarchy) as theHierarchy,"
        + " GROUP_CONCAT(problem_id SEPARATOR '" + SEPARATOR + "') as problemIdList"
        + " FROM problem_hierarchy ph JOIN problem p USING (problem_id)"
        + " WHERE dataset_id = :datasetId";

    /** Constant SQL for filtering by problem name and hierarchy. */
    private static final String SEARCH_BY_STRING = " AND (LOWER(hierarchy) LIKE :searchBy"
        + " OR LOWER(problem_name) LIKE :searchBy)";

    /** Constant SQL for filtering by mapped problem content. */
    private static final String MAPPED_PC_STRING = " AND p.pc_problem_id IS NOT NULL";

    /** Constant SQL for filtering by unmapped problem content. */
    private static final String UNMAPPED_PC_STRING = " AND p.pc_problem_id IS NULL";

    /** Constant SQL. */
    private static final String GROUP_BY_STRING = " GROUP BY hierarchy";

    /** Constant SQL. */
    private static final String ORDER_BY_STRING = " ORDER BY hierarchy";

    /**
     * Get a map of problems by hierarchy for the specified dataset.
     * @param dataset the Dataset item
     * @param searchBy the string to filter by
     * @param problemContent filter by mapped, unmapped or both
     * @param offset the page offset
     * @param rowsPerPage the number of hierarchies displayed
     * @return map of Problem items by hierarchy
     */
    public Map<String, List<ProblemItem>> getProblemsByHierarchyMap(final DatasetItem dataset,
                                                                    final String searchBy,
                                                                    String problemContent,
                                                                    final int offset,
                                                                    final int rowsPerPage) {
        HashMap<String, List<ProblemItem>> result = null;

        StringBuffer sb = new StringBuffer(PROBLEM_HIERARCHY_MAP_QUERY);

        if (StringUtils.isNotBlank(searchBy)) {
            sb.append(SEARCH_BY_STRING);
        }

        if (StringUtils.isNotBlank(problemContent)) {
            if (problemContent.equals(ProblemListDto.PROBLEM_CONTENT_MAPPED)) {
                sb.append(MAPPED_PC_STRING);
            } else if (problemContent.equals(ProblemListDto.PROBLEM_CONTENT_UNMAPPED)) {
                sb.append(UNMAPPED_PC_STRING);
            }
        }

        sb.append(GROUP_BY_STRING);
        sb.append(ORDER_BY_STRING);

        final String queryStr = sb.toString();

        List<Object[]> dbResults = executeSQLQueryMaxConcat(queryStr, new PrepareQuery() {
                @Override
                public void prepareQuery(final SQLQuery query) {
                    query.addScalar("theHierarchy", Hibernate.STRING);
                    query.addScalar("problemIdList", Hibernate.STRING);
                    query.setParameter("datasetId", (Integer)dataset.getId());
                    if (StringUtils.isNotBlank(searchBy)) {
                        query.setParameter("searchBy", "%" + searchBy.toLowerCase() + "%");
                    }

                    if (offset > 0) { query.setFirstResult(offset); }
                    if (rowsPerPage > 0) { query.setMaxResults(rowsPerPage); }
                }
            });

        // HashMap that maintains insertion ordering...
        result = new LinkedHashMap<String, List<ProblemItem>>();

        for (Object[] o : dbResults) {
            int index = 0;
            String hierarchy = (String)o[index++];
            String problemIdList = (String)o[index++];
            
            String[] problemIds = problemIdList.split(SEPARATOR);
            for (String s : problemIds) {
                Long problemId = new Long(s);
                ProblemItem problem = get(problemId);
                List<ProblemItem> curList = result.get(hierarchy);
                if (curList == null) {
                    curList = new ArrayList<ProblemItem>();
                    result.put(hierarchy, curList);
                }
                curList.add(problem);
            }
        }

        // For each hierarchy, sort the problems.
        for (String hierarchy : result.keySet()) {
            List<ProblemItem> curList = result.get(hierarchy);
            Collections.sort(curList);
            result.put(hierarchy, curList);
        }

        return result;
    }

    /** Constant SQL query for retrieving problem hierarchy. */
    private static final String PROBLEM_HIERARCHY_QUERY =
        "SELECT ph.hierarchy AS theHierarchy "
        + "FROM problem_hierarchy ph WHERE problem_id = :problemId";

    /**
     * Get the problem hierarchy for the specified problem.
     * @param problem the ProblemItem
     * @return String the hierarchy
     */
    public String getHierarchy(ProblemItem problem) {

        String hierarchy = null;

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(PROBLEM_HIERARCHY_QUERY);
            query.addScalar("theHierarchy", Hibernate.STRING);
            query.setParameter("problemId", (Long)problem.getId());

            List<String> result = query.list();
            if (result != null && result.size() > 0) {
                hierarchy = result.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return hierarchy;
    }

    /** Constant SQL query for retrieving number of problems in a dataset. */
    private static final String PROBLEM_COUNT_QUERY =
        "SELECT COUNT(*) FROM ProblemItem p JOIN p.datasetLevel dl WHERE dl.dataset = ?";

    /**
     * Get the number of problems for the specified dataset.
     * @param dataset the Dataset item
     * @return Long problem count
     */
    public Long getNumProblems(DatasetItem dataset) {

        Object[] params = {dataset};
        Long theCount =
            (Long) getHibernateTemplate().find(PROBLEM_COUNT_QUERY, params).get(0);
        if (theCount == null) {
            theCount = Long.valueOf(0);
        }
        return theCount;
    }

    /** Constant SQL query for retrieving number of problem hierarchies in a dataset. */
    private static final String HIERARCHY_COUNT_QUERY =
        "SELECT COUNT(DISTINCT(hierarchy)) AS theCount"
        + " FROM problem_hierarchy ph WHERE dataset_id = :datasetId";

    /**
     * Get the number of problem hierarchies for the specified dataset.
     * @param dataset the Dataset item
     * @return Integer hierarchy count
     */
    public Integer getNumHierarchies(DatasetItem dataset) {

        Integer theCount = 0;

        Session session = null;
        try {
            session = getSession();
            SQLQuery query = session.createSQLQuery(HIERARCHY_COUNT_QUERY);
            query.addScalar("theCount", Hibernate.INTEGER);
            query.setParameter("datasetId", (Integer)dataset.getId());

            List<Integer> result = query.list();
            if (result != null && result.size() > 0) {
                theCount = result.get(0);
            }
        } finally {
            releaseSession(session);
        }

        return theCount;
    }

    /** Constant SQL query for retrieving number of unmapped problems in a dataset. */
    private static final String UNMAPPED_PROBLEM_COUNT_QUERY =
        "SELECT COUNT(*) FROM ProblemItem p JOIN p.datasetLevel dl"
        + " WHERE dl.dataset = ? AND p.pcProblem IS NULL";

    /**
     * Get the number of unmapped problems for the specified dataset.
     * @param dataset the Dataset item
     * @return Long unmapped problem count
     */
    public Long getNumUnmappedProblems(DatasetItem dataset) {

        Object[] params = {dataset};
        Long theCount =
            (Long) getHibernateTemplate().find(UNMAPPED_PROBLEM_COUNT_QUERY, params).get(0);
        if (theCount == null) {
            theCount = Long.valueOf(0);
        }
        return theCount;
    }

    /**
     * Get the list of unmapped problems for the specified dataset.
     * @param dataset the Dataset item
     * @return List of all unmapped problems
     */
    public List<ProblemItem> getUnmappedProblems(DatasetItem dataset) {
        DetachedCriteria query = DetachedCriteria.forClass(ProblemItem.class);
        query.createCriteria("datasetLevel")
             .createCriteria("dataset")
             .add(Restrictions.eq("id", dataset.getId()));
        query.add(Restrictions.isNull("pcProblem"));
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Get the list of mapped problems for the specified conversion and dataset.
     * @param pcConversion the PcConversion Item
     * @param dataset the Dataset item
     * @return List of the mapped problems
     */
    public List<ProblemItem> getMappedProblemsByConversion(PcConversionItem pcConversion,
                                                           DatasetItem dataset) {
        DetachedCriteria query = DetachedCriteria.forClass(ProblemItem.class);
        query.createCriteria("datasetLevel")
            .createCriteria("dataset")
            .add(Restrictions.eq("id", dataset.getId()));
        query.createCriteria("pcProblem")
            .add(Restrictions.eq("pcConversion", pcConversion));
        return getHibernateTemplate().findByCriteria(query);
    }

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
            ProblemItem newProblemItem)  {
        boolean found = false;
        // Compare the list of problems passed in from the ContextMessageTransactionCallback
        // with the new problem item
        for (ProblemItem problemItem : problemItems) {
            // Same problem name
            if (problemItem.getProblemName().equals(newProblemItem.getProblemName())) {
                // Determine if the description needs to be updated or not.
                found = true;

                // The problem already exists for this dataset level
                // so update the problem
                newProblemItem = problemItem;
                // We found the problem so break out of the for loop
                break;
            }
        }

        if (!found) {
            newProblemItem = (ProblemItem) findOrCreate(problemItems, newProblemItem);
        }

        return newProblemItem;
    }
}
