/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION_NAME_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CONDITION_TYPE_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_NAME_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_VALUE;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.CUSTOM_FIELD_VALUE_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL_NAME;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DATASET_LEVEL_TITLE;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.DB;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.HQL;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.HQL_ABBREV;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.PROBLEM_DESCRIPTION_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.PROBLEM_NAME_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.SCHOOL_NAME_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.STUDENT_NAME_MAPPER;
import static edu.cmu.pslc.datashop.dao.hibernate.SampleMapper.TRANSACTION;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.item.CfTxLevelItem;
import edu.cmu.pslc.datashop.item.CustomFieldItem;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;
import edu.cmu.pslc.datashop.item.FilterItem;
import edu.cmu.pslc.datashop.item.SampleItem;
import edu.cmu.pslc.datashop.item.SkillItem;
import edu.cmu.pslc.datashop.item.StudentItem;

/**
 * The super class for the SampleDaoHibernate class so some methods can be shared
 * with the LearningCurveDaoHiberate and ErrorReportDaoHibernate classes.
 *
 * @author Alida Skogsholm
 * @version $Revision: 12840 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2015-12-18 12:12:17 -0500 (Fri, 18 Dec 2015) $
 * <!-- $KeyWordsOff: $ -->
 */
public class AbstractSampleDaoHibernate extends AbstractDaoHibernate<SampleItem> {
    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /** One hundred */
    private static final int ONE_HUNDRED = 100;

    /**
     * Default constructor.
     */
    public AbstractSampleDaoHibernate() { }

    /**
     * Adds the where clause portions for filtering on the dataset level class.
     * @param filter the FilterItem containing condition filter.
     * @return String of the query addition.
     */
    protected StringBuffer getDatasetLevelWhereClause(FilterItem filter) {
        StringBuffer query = new StringBuffer();

        if (!filter.isEmptyFilterString()) {
            List<DatasetLevelItem> levels = getMatchingLevels(filter);

            if (!levels.isEmpty()) {
                query.append("and (");
                for (DatasetLevelItem level : levels) {
                    //if it's not the first iteration add and/or
                    if (query.length() > "and (".length()) {
                        query.append(filter.isNotOperator() ? " and " : " or ");
                    }
                    query.append(" lev.id " + (filter.isNotOperator() ? "!= " : "= ")
                            + level.getId());
                }
                query.append(") ");
                logDebug("Query Addition: \"", query, "\"");
            }
        }

        return query;
    }

    /** filter class/attribute combinations to check when generating "where" clause */
    private static final SampleMapper[] MAPPERS = {
        PROBLEM_NAME_MAPPER, PROBLEM_DESCRIPTION_MAPPER, STUDENT_NAME_MAPPER,
        CONDITION_TYPE_MAPPER, CONDITION_NAME_MAPPER, SCHOOL_NAME_MAPPER,
        CUSTOM_FIELD_NAME_MAPPER, CUSTOM_FIELD_VALUE_MAPPER
    };

    /**
     * Find out which class each filter is filtering on and apply the correct filter.
     * @param filters filter item list
     * @param query the query string buffer
     * @return the query string modified
     */
    protected StringBuffer getCommonWhereClause(List<FilterItem> filters, StringBuffer query) {
        for (int i = 0, n = filters.size(); i < n; i++) {
            FilterItem filter = filters.get(i);

            if (filter.checkClass(DATASET_LEVEL.get(DB))) {
                query.append(getDatasetLevelWhereClause(filter));
            } else if (filter.isEmptyFilterString() && !filter.isNullOperator()) {
                logDebug("No filter added from filter item: " + filter);
            } else if (filter.isNotOperator() && filter.checkAttribute(CUSTOM_FIELD_NAME.get(DB))) {
                // Custom Field 'not' filters require special handling.
                query.append(getCustomFieldNotWhereClause(filter));
            } else {
                String abbrev = null, hql = null;

                if (filter.checkClass(TRANSACTION.get(DB))) {
                    hql = filter.getAttribute();
                    abbrev = TRANSACTION.get(HQL_ABBREV);
                } else if (filter.checkAttribute(CUSTOM_FIELD_VALUE.get(DB))) {
                    hql = CUSTOM_FIELD_VALUE.get(HQL);
                    abbrev = CUSTOM_FIELD_VALUE.get(HQL_ABBREV);
                } else {
                    for (SampleMapper mapper : MAPPERS) {
                        if (filter.checkClass(mapper.getClassDB())
                                && filter.checkAttribute(mapper.getAttributeDB())) {
                            hql = mapper.getHQL();
                            abbrev = mapper.getHQLAbbrev();
                            if (filter.checkClass(CONDITION.get(DB))) {
                                abbrev += i;
                            }
                            break;
                        }
                    }
                }
                if (abbrev == null && hql == null) {
                    logDebug("No mapper found for filter item: " + filter);
                } else {
                    String clause = filter.getClause(abbrev, hql);

                    logDebug("Adding " + hql + " filter to sample query.");
                    logDebug("Query Addition: \"", clause, "\"");
                    query.append("and " + clause + " ");
                }
            }
        }

        return query;
    }

    /**
     * Adds the where clause portion for filtering on != Custom Field names.
     * @param filter the FilterItem containing condition filter.
     * @return String of the query addition.
     */
    protected StringBuffer getCustomFieldNotWhereClause(FilterItem filter) {
        DatasetItem datasetItem = filter.getSample().getDataset();

        String filterString = stripQuotes(filter.getFilterString());

        StringBuffer query = new StringBuffer();
        query.append("and trans.id not in (");
        query.append("select distinct lev2.transaction.id from ");
        query.append(CfTxLevelItem.class.getName()).append(" lev2");
        query.append(" join lev2.transaction tt2");
        query.append(" where tt2.dataset.id = ").append(datasetItem.getId());
        query.append(" and lev2.customField.id in (");
        query.append("select cf2.id from ").append(CUSTOM_FIELD.get(HQL)).append(" cf2");
        query.append(" where cf2.dataset.id = ").append(datasetItem.getId()).append(" and");

        // Handle the 'NOT IN' case.
        if (!filter.getOperator().equals("NOT IN")) {
            query.append(" cf2.customFieldName like '%");
            query.append(filterString).append("%'");
        } else {
            query.append(" (");

            // First, remove parens added by Javascript.
            filterString = filterString.substring(1, filterString.length() - 1);

            int count = 0;
            String[] notIn = filterString.split(", ");
            for (String s : notIn) {
                if (count > 0) { query.append(" or "); }
                query.append("cf2.customFieldName like '%")
                    .append(stripQuotes(s)).append("%'");
                count++;
            }

            query.append(")");            
        }

        query.append("))");

        return query;
    }

    /**
     * Helper method to remove the single or double quotes from a filter
     * string that were added by the Javascript. Not necessary in the
     * Custom Field 'not' filters.
     * @param s input String
     * @return String
     */
    private String stripQuotes(String s) {
        String result = s;

        if (s.startsWith("'") && s.endsWith("'")) {
            result = s.substring(1, s.length() - 1);
        }
        if (s.startsWith("\"") && s.endsWith("\"")) {
            result = s.substring(1, s.length() - 1);
        }

        return result;
    }

    /**
     * Builds the end part of the query which contains the conditional information
     * on the requested attributes.
     * @param filters the list of filter items to parse.
     * @param skills list of skill IDs to filter on.
     * @param students list of student IDs to filter on.
     * @return string of query.
     */
    protected StringBuffer getWhereClause(List<FilterItem> filters, List<SkillItem> skills,
            List<StudentItem> students) {
        StringBuffer query = new StringBuffer();
        query = getCommonWhereClause(filters, query);

        //add the skill portion of the where clause.
        if (skills != null && skills.size() > 0) {
            query.append(" and ( skill.id in ( ");

            int counter = 0;

            for (Iterator<SkillItem> it = skills.iterator(); it.hasNext();) {
                SkillItem skill = it.next();

                query.append(" " + skill.getId());
                counter++;
                //this checks for more than 100 selected skills and will break the skills
                //at 100 because more than about 500ish skills was causing a stack overflow
                //due too too many objects in the IN when it built the string
                if (counter > ONE_HUNDRED && it.hasNext()) {
                    query.append(" ) OR skill.id IN ( ");
                    counter = 0;
                } else {
                    query.append(it.hasNext() ? "," : " )  ");
                }
            }
            query.append(" ) ");
        } else if (skills != null) {
            logger.debug("getWhereClause: skillList is empty.");
            query.append(" and ( skill.id in ( -1 ) OR skill.id IS NULL  ) ");
        }
        //add the student portion of the where clause.
        if (students != null && students.size() > 0) {
            query.append(" and ( stud.id in ( ");

            int counter = 0;

            for (Iterator<StudentItem> it = students.iterator(); it.hasNext();) {
                StudentItem student = it.next();
                query.append(" " + student.getId());

                counter++;
                //this checks for more than 100 selected students and will break the students
                //at 100 because more than about 500ish students was causing a stack overflow
                //due too too many objects in the IN when it built the string
                if (counter > ONE_HUNDRED && it.hasNext()) {
                    query.append(" ) OR stud.id IN ( ");
                    counter = 0;
                } else {
                    //if there are more append a comma separator.
                    query.append(it.hasNext() ? "," : " ) ");
                }
            }
            query.append(" ) ");
        } else if (students != null) {
            logDebug("getWhereClause: studentList was empty.");
            query.append(" and ( stud.id in ( -1 ) ) ");
        }
        logDebug("getWhereClause: Completed where clause :: ", query);

        return query;
    }

    /**
     * Get the list of all dataset levels that apply to the given filter
     * and dataset.  This will recursively look through the children of any
     * filters matching and return a list of all matching lists and their children.
     * @param filter for a datasetLevel.
     * @return list of all items and their children for the filter item.
     */
    protected List getMatchingLevels(FilterItem filter) {
        DatasetItem datasetItem = null;

        if (filter.getId() != null) {
            Session session = getSession();
            //re-attach the filter to the session.
            filter = (FilterItem)session.get(FilterItem.class, (Integer)filter.getId());
            //get the dataset item.
            datasetItem = filter.getSample().getDataset();
            releaseSession(session);
        } else {
            datasetItem = filter.getSample().getDataset();
        }

        StringBuffer query = new StringBuffer();

        query.append("from " + DatasetLevelItem.class.getName() + " lev");
        query.append(" where lev.dataset.id = " + datasetItem.getId());

        if (filter.getAttribute().equals(DATASET_LEVEL_NAME.get(DB))
                && !filter.isEmptyFilterString()) {
            query.append(" and lev." + DATASET_LEVEL_NAME.get(HQL) + " "
                    + filter.negatedOperator() + " " + filter.escapedFilterString() + " ");
        } else if (filter.getAttribute().equals(DATASET_LEVEL_TITLE.get(DB))
                && !filter.isEmptyFilterString()) {
            query.append(" and lev." + DATASET_LEVEL_TITLE.get(HQL) + " "
                    + filter.negatedOperator() + " " + filter.escapedFilterString() + " ");
        } else {
            logDebug("No filter added from filter item: ", filter);
        }
        logDebug("Get Matching Levels query: ", query);

        List<DatasetLevelItem> results = getHibernateTemplate().find(query.toString());
        List finalResults = new ArrayList();

        for (DatasetLevelItem result : results) {
            finalResults.addAll(DaoFactory.DEFAULT.getDatasetLevelDao().getChildren(result));
            finalResults.add(result);
        }

        return finalResults;
    }
}
