/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateCallback;

import edu.cmu.pslc.datashop.dao.DatasetUserLogDao;
import edu.cmu.pslc.datashop.helper.UserLogger;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetUserLogItem;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.UserItem;

/**
 * Hibernate and Spring implementation of the DatasetUserLogDao.
 *
 * @author Benjamin Billings
 * @version $Revision: 14821 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2018-02-14 05:19:08 -0500 (Wed, 14 Feb 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetUserLogDaoHibernate extends AbstractDaoHibernate
        implements DatasetUserLogDao {

    /**
     * Standard get for a DatasetUserLogItem by id.
     * @param id The id of the user.
     * @return the matching DatasetUserLogItem or null if none found
     */
    public DatasetUserLogItem get(Integer id) {
        return (DatasetUserLogItem)get(DatasetUserLogItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(DatasetUserLogItem.class);
    }

    /**
     * Standard find for an DatasetUserLogItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetUserLogItem.
     * @return the matching DatasetUserLogItem.
     */
    public DatasetUserLogItem find(Integer id) {
        return (DatasetUserLogItem)find(DatasetUserLogItem.class, id);
    }

    /** Constant value '-100'. */
    private static final int NEGATIVE_100 = -100;
    /** Constant value '10' .*/
    private static final int TEN = 10;

    /**
     * Get a list of all datasets view within the last 100 days by the user.
     * @param user the user to get datasets for.
     * @return a List of dataset items.
     */
    public List <DatasetItem> getRecentDatasetsViewed(UserItem user) {

        String query = "select dul.dataset from DatasetUserLogItem dul "
            + "where dul.user = ? and dul.action = ? and dul.time > ? "
            + " and (dul.dataset.deletedFlag is NULL OR dul.dataset.deletedFlag = false)"
            + "group by dul.dataset "
            + "order by max(dul.time) desc";

        Calendar prevDate = Calendar.getInstance();
        prevDate.setTime(new Date());
        prevDate.add(Calendar.DAY_OF_WEEK, NEGATIVE_100);
        Object[] params = new Object[] {user, UserLogger.SELECT_DATASET, prevDate.getTime()};

        List <DatasetItem> allDatasets = null;
        List <DatasetItem> tenDatasets = new ArrayList <DatasetItem>();
        allDatasets = getHibernateTemplate().find(query, params);

        int idx = 0;
        for (DatasetItem datasetItem : allDatasets) {
            tenDatasets.add(datasetItem);
            idx++;
            if (idx > TEN) {
                break;
            }
        }
        return tenDatasets;
    }

    /**
     * Checks if a given action for a given user and dataset has already been properly
     * recorded to avoid recording the same action multiple times (like when refreshing
     * or changing options on a report page).
     * @param dataset The dataset being used by the user.
     * @param user The user performing the action.
     * @param action The action being performed.
     * @return Boolean of true if the action has already been recorded, false otherwise.
     */
    public Boolean isAlreadyRecorded(DatasetItem dataset, UserItem user, String action) {
        //check that the action is one we care to even test first off.
        if (!UserLogger.VIEW_ACTIONS.contains(action)) { return false; }

        StringBuffer actionsToCheck = new StringBuffer();
        for (Iterator <String> it = UserLogger.VIEW_ACTIONS.iterator(); it.hasNext();) {
            actionsToCheck.append("'" + it.next() + "'");
            if (it.hasNext()) { actionsToCheck.append(", "); }
        }

        Calendar prevDate = Calendar.getInstance();
        prevDate.setTime(new Date());
        prevDate.add(Calendar.DAY_OF_WEEK, -1);
        Object[] params = null;
        String query = null;

        if (dataset == null) {
            //get the list of all the cared about actions without a dataset in the
            //last 24 hours.
            query = "select distinct dul from DatasetUserLogItem dul "
                + "where dul.user = ? and dul.dataset is null "
                + "and dul.action in (" + actionsToCheck + ") "
                + "and dul.time > ? "
                + "order by dul.time DESC";

            params = new Object[] {user, prevDate.getTime()};

        } else {
            //get the list of all the cared about actions for this dataset in the
            //last 24 hours.
            query = "select distinct dul from DatasetUserLogItem dul "
                + "where dul.user = ? and dul.dataset = ? "
                + "and dul.action in (" + actionsToCheck + ") "
                + "and dul.time > ? "
                + "order by dul.time DESC";

            params = new Object[] {user, dataset, prevDate.getTime()};
        }

        List <DatasetUserLogItem> logs = getHibernateTemplate().find(query, params);

        //See if the topmost log matches what we are wanting to record.
        //If it does return true because it was the last one we recorded.
        if (logs.size() > 0) {
            DatasetUserLogItem lastLog = logs.get(0);
            if (lastLog.getAction().equals(action)) { return true; }
        }

        return false;
    }

    /**
     * Get the number of times the given user has selected any dataset.
     * This is used to determine if the user is a new to DataShop or not.
     * @param userItem the user item
     * @return the number of times the given user has selected any dataset
     */
    public long getNumberDatasetSelects(UserItem userItem) {
        String query = "select count(*) from DatasetUserLogItem dul "
            + "where dul.action = ? "
            + "and dul.user = ?";
        Object[] params = new Object[] {UserLogger.SELECT_DATASET, userItem};
        Long numResults = (Long)getHibernateTemplate().find(query, params).get(0);
        return numResults.longValue();
    }

    /**
     * Checks if a given action for a given user has been logged.
     * @param user The user performing the action.
     * @param action the given action
     * @return Boolean of true if the action has been logged, false otherwise.
     */
    public boolean hasPerformedAction(UserItem user, String action) {
        // build the query
        String query = "select distinct dul from DatasetUserLogItem dul"
                     + " where dul.user = ?"
                     + " and dul.dataset is null"
                     + " and dul.action = ?"
                     + " order by dul.time DESC";

        // execute the query
        Object[] params = new Object[] {user, action};
        List <DatasetUserLogItem> logs = getHibernateTemplate().find(query, params);

        // if there are any logs then the user has performed the given action
        if (logs.size() > 0) {
            DatasetUserLogItem lastLog = logs.get(0);
            if (lastLog.getAction().equals(action)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the first access date for a given user and project.
     * @param userItem the user item
     * @param projectItem the project item
     * @return the first time the given user has selected any dataset in project
     */
    public Date getFirstAccess(UserItem userItem, ProjectItem projectItem) {
        Object[] params = new Object[] {UserLogger.SELECT_DATASET, projectItem, userItem};
        String query = "select min(dul.time) from DatasetUserLogItem as dul"
                + " left join dul.dataset as ds"
                + " where dul.action = ?"
                + " and ds.project = ?"
                + " and dul.user = ?";

        List<Date> firstAccess = (List<Date>)getHibernateTemplate().find(query, params);

        if (firstAccess != null && firstAccess.size() > 0) {
            return (Date)(firstAccess.get(0));
        } else {
            return null;
        }
    }

    /**
     * Get the first access date for a given user and project.
     * @param userItem the user item
     * @param projectItem the project item
     * @return the last time the given user has selected any dataset in project
     */
    public Date getLastAccess(UserItem userItem, ProjectItem projectItem) {
        Object[] params = new Object[] {UserLogger.SELECT_DATASET, projectItem, userItem};
        String query = "select max(dul.time) from DatasetUserLogItem as dul"
            + " left join dul.dataset as ds"
            + " where dul.action = ?"
            + " and ds.project = ?"
            + " and dul.user = ?";

        List<Date> lastAccess = (List<Date>)getHibernateTemplate().find(query, params);
        if (lastAccess != null && lastAccess.size() > 0) {
            return (Date)(lastAccess.get(0));
        } else {
            return null;
        }
    }

    /**
     * Gets an object array (UserItem, ProjectId, First access, and Last access)
     * or return null if no users accessed any datasets.
     * @return an object array (UserItem, ProjectId, First access, and Last access)
     */
    public List getAccessTimes() {
        Object[] params = new Object[] {UserLogger.SELECT_DATASET};

        String query = "select dul.user as user, ds.project.id as projectid,"
            + " min(dul.time) as first, max(dul.time) as last,"
            + " dul.dataset.datasetName as datasetName"
            + " from DatasetUserLogItem as dul"
            + " left join dul.dataset as ds"
            + " where dul.action = ?"
            + " group by dul.user, ds.project.id";

        List<Object[]> firstAccessTimes =
                (List<Object[]>)getHibernateTemplate().find(query, params);
        return firstAccessTimes;
    }

    /**
     * Gets an object array (UserItem, First access, and Last access)
     * based on the ProjectItem or return null if no users accessed the dataset.
     * @param projectItem the project item
     * @return an object array (UserItem, First access, and Last access)
     * for a given ProjectItem or null if no users accessed the dataset
     */
    public List getAccessTimesByProject(ProjectItem projectItem) {
        if (projectItem == null) {
            return null;
        }
        Object[] params = new Object[] {UserLogger.SELECT_DATASET,
                projectItem.getId()};
        String query = "select dul.user as user,"
            + " min(dul.time) as first, max(dul.time) as last"
            + " from DatasetUserLogItem as dul"
            + " left join dul.dataset as ds"
            + " where dul.action = ?"
            + " and ds.project.id = ?"
            + " group by dul.user";

        List<Object[]> firstAccessTimes =
                (List<Object[]>)getHibernateTemplate().find(query, params);
        return firstAccessTimes;
    }

    /** HQL to query for the last KCM import Starting action. */
    private static final String SELECT_LAST_KCM_IMPORTED_STARTING =
            "SELECT MAX(dul.time) FROM DatasetUserLogItem dul"
          + " WHERE dul.dataset = ? AND dul.action = ?"
          + " AND   dul.info like 'Starting%'";

    /** HQL to query for the last KCM import Finished action. */
    private static final String SELECT_LAST_KCM_IMPORTED_FINISHED =
            "SELECT MAX(dul.time) FROM DatasetUserLogItem dul"
          + " WHERE dul.dataset = ? AND dul.action = ?"
          + " AND   dul.info like 'Finished%'";

    /**
     * Check if the KCM import is already running for the given dataset.
     * @param datasetItem the dataset
     * @return true if there is no 'finished' info after the last started.
     */
    public Date areKcmsImporting(DatasetItem datasetItem) {
        Date lastStarted = (Date)findObject(
                SELECT_LAST_KCM_IMPORTED_STARTING, datasetItem, UserLogger.MODEL_IMPORT);

        Date lastFinished = (Date)findObject(
                SELECT_LAST_KCM_IMPORTED_FINISHED, datasetItem, UserLogger.MODEL_IMPORT);

        if (lastStarted != null) {
            Integer datasetId = (Integer)datasetItem.getId();
            if  (lastFinished == null) {
                logger.info("KCM Import in progress for dataset " + datasetId
                        + ". Last started is " + lastStarted);
            } else if (lastFinished.before(lastStarted)) {
                logger.info("KCM Import in progress for dataset " + datasetId
                        + ". Last started is " + lastStarted
                        + " and last finished is " + lastFinished);
            } else {
                lastStarted = null;
            }
        } else {
            lastStarted = null;
        }
        return lastStarted;
    }

    /** HQL to query for the last KCM import Starting action. */
    private static final String SELECT_LAST_LOGIN =
            "SELECT MAX(dul.time) FROM DatasetUserLogItem dul"
          + " WHERE dul.user = ? AND dul.action = 'Login'";

    /**
     * Returns the date of the given user's last login.
     * @param userItem the given user
     * @return the date of the user's last login
     */
    public Date getLastLogin(UserItem userItem) {
        return (Date)findObject(SELECT_LAST_LOGIN, userItem);
    }

    /** HQL to query to get a list of N project for a given user. */
    private static final String GET_LAST_PROJECTS =
            "SELECT project.projectName"
          + " FROM DatasetUserLogItem dul"
          + " JOIN dul.dataset as dataset"
          + " JOIN dataset.project as project"
          + " WHERE dul.user = ";
   private static final String GET_LAST_PROJECTS_GROUP_BY =
            " GROUP BY project"
          + " ORDER BY max(time) desc";

    /**
     * Get a list of N projects for a given user.
     * @param userItem given user
     * @param limit N
     * @return a list of project names
     */
    public List<String> getLastProjects(UserItem userItem, Integer limit) {
        String query = GET_LAST_PROJECTS + "'" + userItem.getId() + "'" + GET_LAST_PROJECTS_GROUP_BY;
        CallbackCreatorHelper helperCreator =
                new CallbackCreatorHelper(query, 0, limit);
        HibernateCallback callback = helperCreator.getCallback();
        List<String> results = getHibernateTemplate().executeFind(callback);
        return results;
    }

    /**
     * Gets a list of users and their last access times for a given dataset.
     * @return a list of users and their last access times for a given dataset
     */
    public List getAccessTimes(DatasetItem datasetItem) {
        Object[] params = new Object[] {UserLogger.SELECT_DATASET, datasetItem};

        String query = "select dul.user as user, dul.dataset.id as dataset,"
            + " Date(max(dul.time)) as last, dul.user.adminFlag as isDatashopAdmin"
            + " from DatasetUserLogItem as dul"
            + " where dul.action = ?"
            + " and dul.dataset = ?"
            + " group by dul.user"
            + " order by max(dul.time) desc";

        List<Object[]> lastAccessTimes =
                (List<Object[]>)getHibernateTemplate().find(query, params);
        return lastAccessTimes;
    }
}
