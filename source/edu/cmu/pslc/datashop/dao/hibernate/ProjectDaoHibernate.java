/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.ProjectDao;
import edu.cmu.pslc.datashop.dto.importqueue.ExistingProjectDto;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.servlet.irb.IrbReviewFilter;

/**
 * Data access object to retrieve the data from the Project database table
 * via Hibernate.
 *
 * @author Alida Skogsholm
 * @version $Revision: 14293 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2017-09-28 14:12:25 -0400 (Thu, 28 Sep 2017) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ProjectDaoHibernate extends AbstractDaoHibernate
    implements ProjectDao {

    /**
     * Standard get for a project item by id.
     * @param id the id of the desired project item
     * @return the matching ProjectItem or null if none found
     */
    public ProjectItem get(Integer id) {
        return (ProjectItem)get(ProjectItem.class, id);
    }
    /**
     * Standard find for a project item by id.
     * @param id id of the object to find
     * @return ProjectItem
     */
    public ProjectItem find(Integer id) {
        return (ProjectItem)find(ProjectItem.class, id);
    }

    /**
     * Standard "find all" for project items.
     * @return a List of objects which are ProjectItems
     */
    public List findAll() {
        return getHibernateTemplate().find("from " + ProjectItem.class.getName());
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Nonstandard "find all" for project items sorted by projectName.
     * @return a List of objects which are ProjectItems sorted by projectName
     */
    public List findAllSortByName() {
        return getHibernateTemplate().find("from " + ProjectItem.class.getName()
                + " order by Lower(projectName)");
    }
    /**
     * Returns ProjectItem given a name.
     * @param name name of the ProjectItem
     * @return a collection of ProjectItems
     */
    public Collection find(String name) {
        return getHibernateTemplate().find(
                "from ProjectItem project where project.projectName = ?", name);
    }

    /**
     * Count the number of papers for the datasets of this project.
     *
     * @param projectItem the dataset
     * @return the number of papers attached to this project
     */
    public Long countPapers(ProjectItem projectItem) {
        String query = "select count(*) from DatasetItem dat"
            + " join dat.project project"
            + " join dat.papers papers"
            + " where project.id = ?"
            + " and (dat.deletedFlag is NULL OR dat.deletedFlag = false)";

        if (logger.isTraceEnabled()) {
            logger.trace("Getting number of papers in project with query :: " + query);
        }

        Long numResults = (Long)getHibernateTemplate().find(query, projectItem.getId()).get(0);
        if (numResults == null) {
             numResults = Long.valueOf(0);
        }
        return numResults;
    }

    /**
     * Find projects given a data provider.
     *
     * @param userItem the data provider
     * @return a collection of ProjectItems
     */
    public List<ProjectItem> findByDataProvider(UserItem userItem) {
        return getHibernateTemplate().find(
                "from ProjectItem project where project.dataProvider = ?", userItem);
    }

    /**
     * Find all data providers.
     *
     * @return a collection of UserItems
     */
    public List<UserItem> findAllDataProviders() {
        return getHibernateTemplate().find(
                "select distinct project.dataProvider from ProjectItem project "
                + "order by project.dataProvider");
    }

    /**
     * Find projects given a data provider or primary investigator.
     * @param ownerItem the data provider or PI
     * @return a collection of ProjectItems
     */
    public List<ProjectItem> findByOwner(UserItem ownerItem) {
        Object[] params = {ownerItem, ownerItem};
        return getHibernateTemplate().find(
                "from ProjectItem project "
                + " where project.primaryInvestigator = ?"
                + " or project.dataProvider = ?", params);
    }

    /**
     * Nonstandard "find all" for project items, filtered by IRB attributes.
     * @param filter IrbReviewFilter specifying how to filter list of projects
     * @return a List of objects which are ProjectItems sorted by projectName
     */
    public List findAllFiltered(IrbReviewFilter filter) {
        DetachedCriteria query = DetachedCriteria.forClass(ProjectItem.class);
        Conjunction conjunction = Restrictions.conjunction();
        if (filter.getShareability() != null) {
            conjunction.add(Restrictions.eq("shareableStatus", filter.getShareability()));
        }
        if (filter.getDataCollectionType() != null) {
            conjunction.add(Restrictions.eq("dataCollectionType", filter.getDataCollectionType()));
        }
        if (filter.getSubjectTo() != null) {
            conjunction.add(Restrictions.eq("subjectToDsIrb", filter.getSubjectTo()));
        }
        if (filter.getNeedsAttn() != null) {
            conjunction.add(Restrictions.eq("needsAttention", filter.getNeedsAttn()));
        }
        if (filter.getPcDate() != null) {
            if (filter.getPcBefore().equals("on")) {
                Date maxDate = addOneDay(filter.getPcDateStr());
                conjunction.add(Restrictions.ge("createdTime", filter.getPcDate()));
                conjunction.add(Restrictions.lt("createdTime", maxDate));
            } else if (filter.getPcBefore().equals("after")) {
                conjunction.add(Restrictions.ge("createdTime", filter.getPcDate()));
            } else { //before (default)
                conjunction.add(Restrictions.le("createdTime", filter.getPcDate()));
            }
        }
        if (filter.getDlaDate() != null) {
            if (filter.getDlaBefore().equals("on")) {
                Date maxDate = addOneDay(filter.getDlaDateStr());
                conjunction.add(Restrictions.ge("datasetLastAdded", filter.getDlaDate()));
                conjunction.add(Restrictions.lt("datasetLastAdded", maxDate));
            } else if (filter.getDlaBefore().equals("after")) {
                conjunction.add(Restrictions.ge("datasetLastAdded", filter.getDlaDate()));
            } else { //before (default)
                conjunction.add(Restrictions.le("datasetLastAdded", filter.getDlaDate()));
            }
        }
        query.add(conjunction);
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Quick little date utility to add one day to the given date string.
     * @param dateStr a date string of the format, year-month-day
     * @return a new Date, with one day added
     */
    private Date addOneDay(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date newDate = null;
        try {
            Date minDate = sdf.parse(dateStr);
            Calendar c = Calendar.getInstance();
            c.setTime(minDate);
            c.add(Calendar.DATE, 1); // add one day
            newDate = c.getTime();
        } catch (ParseException exception) {
            logger.error("addOneDay: Failed to parse date.", exception);
        }
        return newDate;
    }

     /** Native SQL. */
    private static final String EXISTING_SQL =
        "SELECT project.project_id as projectId,"
            + " project.project_name as projectName,"
            + " project.shareable_status as srs,"
            + " project.primary_investigator as pi,"
            + " pi_user.first_name as piFirstName,"
            + " pi_user.last_name as piLastName,"
            + " project.data_provider as dp,"
            + " dp_user.first_name as dpFirstName,"
            + " dp_user.last_name as dpLastName,"
            + " project.is_discourse_dataset as discourseFlag,"
            + " IF ( (SELECT count(*) FROM authorization a2"
                  + " WHERE a2.project_id = project.project_id"
                  + " AND a2.user_id = '%')"
                  + " > 0, true, false ) as publicFlag,"
            + " IF ( (SELECT count(*) FROM authorization a2"
                  + " WHERE a2.project_id = project.project_id"
                  + " AND a2.level IN ('edit', 'view'))"
                  + " > 0, true, false ) as someFlag"
            + " FROM project project"
            + " LEFT JOIN user pi_user ON pi_user.user_id = project.primary_investigator"
            + " LEFT JOIN user dp_user ON dp_user.user_id = project.data_provider";
     /** Native SQL. */
    private static final String PROJECT_ADMIN =
              " JOIN authorization auth USING (project_id)"
            + " WHERE auth.user_id = :userId AND auth.level = 'admin'";
     /** Native SQL. */
    private static final String EXISTING_ORDER_BY =
              " ORDER BY lower(projectName)";

    /**
     * Return a list of ExistingProjectDto objects for the given user.
     * @param userItem the user item for the current user
     * @return list of ExistingProjectDto objects
     */
    public List<ExistingProjectDto> getExistingProjects(UserItem userItem) {
        List<ExistingProjectDto> dtoList = new ArrayList<ExistingProjectDto>();
        Map<String, Object> params = new HashMap<String, Object>();
        String queryString = EXISTING_SQL;
        if (!userItem.getAdminFlag()) {
            queryString += PROJECT_ADMIN;
            params.put("userId", (String)userItem.getId());
        }
        queryString += EXISTING_ORDER_BY;

        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(queryString);

            //indicate which columns map to which types.
            sqlQuery.addScalar("projectId", Hibernate.INTEGER);
            sqlQuery.addScalar("projectName", Hibernate.STRING);
            sqlQuery.addScalar("srs", Hibernate.STRING);
            sqlQuery.addScalar("pi", Hibernate.STRING);
            sqlQuery.addScalar("piFirstName", Hibernate.STRING);
            sqlQuery.addScalar("piLastName", Hibernate.STRING);
            sqlQuery.addScalar("dp", Hibernate.STRING);
            sqlQuery.addScalar("dpFirstName", Hibernate.STRING);
            sqlQuery.addScalar("dpLastName", Hibernate.STRING);
            sqlQuery.addScalar("discourseFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("publicFlag", Hibernate.BOOLEAN);
            sqlQuery.addScalar("someFlag", Hibernate.BOOLEAN);

            //set the parameters
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    sqlQuery.setParameter(param.getKey(), param.getValue());
                }
            }

            if (logger.isTraceEnabled()) {
                logger.trace("getExistingProjects sqlQuery [" + sqlQuery + "]");
            }

            List<Object[]> dbResults = sqlQuery.list();

            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ExistingProjectDto dto = new ExistingProjectDto();
                dto.setProjectId((Integer)obj[colIdx++]);
                String projectName = (String)obj[colIdx++];
                // Don't include Remote projects in list...
                if (projectName.equals(ProjectItem.REMOTE_DATASETS)) {
                    continue;
                }
                dto.setProjectName(projectName);
                dto.setShareabilityStatus((String)obj[colIdx++]);
                String piUsername = (String)obj[colIdx++];
                String piFirstName = (String)obj[colIdx++];
                String piLastName = (String)obj[colIdx++];
                String dpUsername = (String)obj[colIdx++];
                String dpFirstName = (String)obj[colIdx++];
                String dpLastName = (String)obj[colIdx++];
                Boolean discourseFlag = (Boolean)obj[colIdx++];
                Boolean publicFlag = (Boolean)obj[colIdx++];
                Boolean someFlag = (Boolean)obj[colIdx++];

                // Don't include DiscourseDB projects in list...
                if ((discourseFlag != null) && discourseFlag) {
                    continue;
                }

                if (piFirstName != null && piFirstName.length() > 0
                 && piLastName != null && piLastName.length() > 0) {
                    dto.setPi(piFirstName + " " + piLastName);
                } else {
                    dto.setPi(piUsername);
                }

                if (dpFirstName != null && dpFirstName.length() > 0
                 && dpLastName != null && dpLastName.length() > 0) {
                    dto.setDp(dpFirstName + " " + dpLastName);
                } else {
                    dto.setDp(dpUsername);
                }

                String permissions = ExistingProjectDto.PERM_PRIVATE;
                if (publicFlag) {
                    permissions = ExistingProjectDto.PERM_PUBLIC;
                } else if (someFlag) {
                    permissions = ExistingProjectDto.PERM_SOME;
                }
                dto.setPermissions(permissions);

                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /**
     * Return the special 'Remote Datasets' project that holds datasets
     * created for SLAVE instances.
     *
     * @return ProjectItem
     */
    public ProjectItem findRemoteDatasetsProject() {
        List<ProjectItem> projectList = 
            getHibernateTemplate().find("from ProjectItem project where project.projectName = ?",
                                        ProjectItem.REMOTE_DATASETS);

        // If a null or empty list, return null.
        if ((projectList == null) || (projectList.size() == 0)) { return null; }

        // There should be only one of these...
        return projectList.get(0);
    }

    /**
     * Return a list of the DiscourseDB projects.
     * 
     * @return list of ProjectItem objects
     */
    public List<ProjectItem> findDiscourseDbProjects() {
        List<ProjectItem> projectList = 
            getHibernateTemplate().find("from ProjectItem project where project.isDiscourseDataset = ?", true);

        // If a null or empty list, return null.
        if ((projectList == null) || (projectList.size() == 0)) { return null; }

        // There should be only one of these...
        return projectList;
    }
}
