/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.DatasetLevelDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.DatasetLevelItem;

import static org.hibernate.Hibernate.INTEGER;
import static org.hibernate.Hibernate.STRING;

/**
 * Hibernate and Spring implementation of the DatasetLevelDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 9298 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-05-29 12:29:28 -0400 (Wed, 29 May 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DatasetLevelDaoHibernate extends AbstractDaoHibernate<DatasetLevelItem>
implements DatasetLevelDao {

    /** Logger for this class. */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Modified version of the saveOrUpdate that will properly update the left/right indexes.
     * @param level object to save
     */
    public void saveOrUpdate(DatasetLevelItem level) {
        if (logger.isDebugEnabled()) { logger.debug("saveOrUpdate: " + level); }
        if (level.getId() != null) {
            DatasetLevelItem existingLevel = get((Integer)level.getId());
            if (level.getRightIndex() == null
                    || level.getLeftIndex() == null
                    || level.getRightIndex() == 0
                    || level.getLeftIndex() == 0
                    || existingLevel == null) {

                if (level.getParent() != null) {
                    //if the parent does not equal zero add this
                    //as the right most child of this parent.
                    addAsRightMostChild(level);
                } else {
                    addRootNode(level);
                }

            } else if (!existingLevel.equals(level)) {
                getHibernateTemplate().update(level);
            }
        } else {
            if (level.getParent() != null) {
                //if the parent does not equal zero add this as the right most child of this parent.
                addAsRightMostChild(level);
            } else {
                addRootNode(level);
            }
        }
        //because we could potentially be doing multiple update and saves flush
        //the session before continuing to make sure that all updates are completed.
        getHibernateTemplate().flush();
    }

    /**
     * Adds a new leaf node as the right more child of a parent.  Will save the new child and
     * update all left and right indexes of the rest of the nested set as required.
     * @param newLevel the New Level to add.
     */
    private void addAsRightMostChild(DatasetLevelItem newLevel) {
        if (logger.isDebugEnabled()) {
            logger.debug("addAsRightMostChild: " + newLevel);
        }

        DatasetLevelItem parentLevel = get((Integer)newLevel.getParent().getId());
        //because the left/right indexes are not part of the hash code/equals function
        //we must make sure that the parentLevel in the session matches what is stored in
        //the database.  The bulk update commands will not properly update the object in the
        //session so we must first evict it from the session and get it again to make sure.
        getHibernateTemplate().evict(parentLevel);
        parentLevel = get((Integer)parentLevel.getId());
        Integer parentRightIndex = parentLevel.getRightIndex();

        //update all the items to the right in the tree by adding 2 to the left and right indexes.
        final int numValues = 2;
        Object[] values = new Object[numValues];
        values[0] = parentLevel.getDataset().getId();
        values[1] = parentRightIndex;

        if (logger.isDebugEnabled()) {
            logger.debug("Updating existing level with index greater than " + values[1]);
        }
        String updateQuery = "update DatasetLevelItem dl set dl.rightIndex = dl.rightIndex + 2"
            + " where dl.dataset.id = ? and dl.rightIndex >= ?"
            + " and dl.rightIndex IS NOT NULL and dl.leftIndex IS NOT NULL"
            + " and dl.rightIndex != 0 and dl.leftIndex != 0";
        int updated = getHibernateTemplate().bulkUpdate(updateQuery, values);
        if (logger.isDebugEnabled()) {
            logger.debug(updated + " rows had their right index updated");
        }

        updateQuery = "update DatasetLevelItem dl set dl.leftIndex = dl.leftIndex + 2"
            + " where dl.dataset.id = ? and dl.leftIndex > ?"
            + " and dl.rightIndex IS NOT NULL and dl.leftIndex IS NOT NULL"
            + " and dl.rightIndex != 0 and dl.leftIndex != 0";
        updated = getHibernateTemplate().bulkUpdate(updateQuery, values);
        if (logger.isDebugEnabled()) {
            logger.debug(updated + " rows had their left index updated");
        }

        //set the left and right index of the new leaf.
        newLevel.setLeftIndex(parentRightIndex);
        newLevel.setRightIndex(parentRightIndex + 1);

        //finally save the new dataset level.
        if (logger.isDebugEnabled()) {
            logger.debug("Saving Right Most Child: " + newLevel);
        }
        getHibernateTemplate().saveOrUpdate(newLevel);
    }

    /**
     * Adds a new dataset level as the root node.
     * Note: Can have more than one root node for single depth trees.
     * @param rootLevel the level to add as the root node.
     */
    private void addRootNode(DatasetLevelItem rootLevel) {
        logger.debug("addRootNode: " + rootLevel);

        //first check that a root node doesn't already exist.
        List existingLevels = getRootNodes(rootLevel.getDataset());
        logger.debug(existingLevels.size() + " root nodes found");
        if (existingLevels.size() > 0) {
            DatasetLevelItem rightMostRoot =
                (DatasetLevelItem)existingLevels.get(existingLevels.size() - 1);
            //because the left/right indexes are not part of the hash code/equals function
            //we must make sure that the parentLevel in the session matches what is stored in
            //the database.  The bulk update commands will not properly update the object in the
            //session so we must first evict it from the session and get it again to make sure.
            getHibernateTemplate().evict(rightMostRoot);
            rightMostRoot = get((Integer)rightMostRoot.getId());

            logger.debug("Right most root found. ID: " + rightMostRoot.getId()
                    + " lft: " + rightMostRoot.getLeftIndex()
                    + " rgt: " + rightMostRoot.getRightIndex());
            rootLevel.setLeftIndex(rightMostRoot.getRightIndex() + 1);
            rootLevel.setRightIndex(rightMostRoot.getRightIndex() + 2);
        } else {
            //set the left/right index to 1/2 respectively for the first root node.
            rootLevel.setLeftIndex(new Integer(1));
            rootLevel.setRightIndex(new Integer(2));
        }
        logger.debug("Saving Root Node: " + rootLevel);
        getHibernateTemplate().saveOrUpdate(rootLevel);
    }

    /**
     * Gets a list of all root nodes for a given dataset.
     * @param dataset the dataset to get root nodes for.
     * @return a List of DatasetLevelItems ordered by the rightIndex.
     */
    private List getRootNodes(DatasetItem dataset) {
        String query = "from DatasetLevelItem dl"
            + " where dl.parent IS NULL and dl.dataset.id = ?"
            + " and dl.rightIndex IS NOT NULL and dl.leftIndex IS NOT NULL"
            + " and dl.rightIndex != 0 and dl.leftIndex != 0"
            + " order by dl.rightIndex";
        return getHibernateTemplate().find(query, dataset.getId());
    }

    /**
     * Standard get for a DatasetLevelItem by id.
     * @param id The id of the user.
     * @return the matching DatasetLevelItem or null if none found
     */
    public DatasetLevelItem get(Integer id) {
        return (DatasetLevelItem)get(DatasetLevelItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<DatasetLevelItem> findAll() {
        return findAll(DatasetLevelItem.class);
    }

    /**
     * Standard find for an DatasetLevelItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired DatasetLevelItem.
     * @return the matching DatasetLevelItem.
     */
    public DatasetLevelItem find(Integer id) {
        return (DatasetLevelItem)find(DatasetLevelItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Returns DatasetLevel(s) given a name.
     * @param name name of DatasetLevel
     * @return Collection
     */
    public Collection<DatasetLevelItem> find(String name) {
        return getHibernateTemplate().find(
                "from DatasetLevelItem datasetLevel where datasetLevel.levelName = ?", name);
    }

    /**
     * Returns a collection of all children for this dataset level. <br />
     * @param parent the dataset level to get children of
     * @return a collection of all children that fall below the parent. <br />
     * <strong>Note:</strong> this collection will not include the parent, but will
     * be recursive so it will bottom out on the lowest most child(ren).
     */
    public Collection<DatasetLevelItem> getChildren(DatasetLevelItem parent) {
        Session session = getSession();
        parent = (DatasetLevelItem)session.get(DatasetLevelItem.class, (Integer)parent.getId());
        List results = new ArrayList();
        for (Iterator it = parent.getChildrenExternal().iterator(); it.hasNext();) {
            DatasetLevelItem child = (DatasetLevelItem)it.next();
            //re-attach the child.
            child = (DatasetLevelItem)session.get(DatasetLevelItem.class, (Integer)child.getId());
            if (child.getChildrenExternal().size() > 0) {
                results.addAll(getChildren(child));
            }
            results.add(child);
        }
        releaseSession(session);
        return results;
    }

    /**
     * Gets a list of dataset levels in the dataset that match all or a portion of the
     * title parameter.
     * @param toMatch A string to match a title too.
     * @param dataset the dataset item to find levels in.
     * @param matchAny boolean value indicating whether to only look for levels that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching levels items sorted by title.
     */
    public List<DatasetLevelItem> findMatchingByTitle(String toMatch, DatasetItem dataset,
            boolean matchAny) {
        DetachedCriteria query = DetachedCriteria.forClass(DatasetLevelItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("levelTitle", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("levelTitle", toMatch, MatchMode.START));
        }
        query.createCriteria("dataset").add(Restrictions.eq("id", dataset.getId()));
        query.addOrder(Property.forName("levelTitle").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of dataset levels in the dataset that match all or a portion of the
     * name parameter.
     * @param toMatch A string to match a name too.
     * @param dataset the dataset item to find levels in.
     * @param matchAny boolean value indicating whether to only look for levels that match
     * the string from the beginning, or to look for toMatch anywhere.
     * @return List of all matching levels items sorted by name.
     */
    public List<DatasetLevelItem> findMatchingByName(String toMatch, DatasetItem dataset,
            boolean matchAny) {
        DetachedCriteria query = DetachedCriteria.forClass(DatasetLevelItem.class);
        if (matchAny) {
            query.add(Restrictions.ilike("levelName", toMatch, MatchMode.ANYWHERE));
        } else {
            query.add(Restrictions.ilike("levelName", toMatch, MatchMode.START));
        }
        query.createCriteria("dataset").add(Restrictions.eq("id", dataset.getId()));
        query.addOrder(Property.forName("levelName").asc());
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Gets a list of dataset levels in the dataset that match all of the title and name parameters.
     * @param titleMatch the title string to match
     * @param nameMatch the name string to match
     * @param dataset the dataset item to find levels in
     * @return a list of matching dataset levels
     */
    public List<DatasetLevelItem> findMatchingByTitleAndName(String titleMatch, String nameMatch,
            DatasetItem dataset) {
        DetachedCriteria query = DetachedCriteria.forClass(DatasetLevelItem.class);
        query.add(Restrictions.ilike("levelName", nameMatch, MatchMode.START));
        query.add(Restrictions.ilike("levelTitle", titleMatch, MatchMode.START));
        query.createCriteria("dataset").add(Restrictions.eq("id", dataset.getId()));
        return getHibernateTemplate().findByCriteria(query);
    }

    /**
     * Returns the number of dataset_levels for a give dataset id.  Used in export.
     * @param id the dataset id
     * @return an Integer representing the count of dataset_levels for a given dataset.
     */
    public Long getDatasetLevelCount(Integer id) {
        String query = "select count(*) from DatasetLevelItem"
            + " where parent.id <> '(null)'"
            + " and dataset.id = ?";
        List<Long> datasetLevelList = getHibernateTemplate().find(query, id);
        // get the first count from the list and return it - it's the highest skill count
        Long datasetLevelCount = datasetLevelList.get(0);
        return datasetLevelCount == 0 ? 1L : datasetLevelCount;
    }

    /**
     * Returns an ArrayList containing all level_titles for a particular dataset.
     * @param id the dataset id
     * @return an ArrayList containing all level_titles for a given dataset_id.
     */
    public List<String> getDatasetLevelTitles(Integer id) {
        String query = "select distinct levelTitle from DatasetLevelItem"
            + " where dataset.id = ? and (problems.size != 0 or children.size !=0)"
            + " order by parent.id";
        return getHibernateTemplate().find(query, id);
    }

    /**
     * Find all dataset level Items for a given dataset level.
     * @param dataset the dataset to get all levels for
     * @return a List of matching dataset levels.
     */
    public List<DatasetLevelItem> find(DatasetItem dataset) {
        return getHibernateTemplate().find("from DatasetLevelItem where dataset.id = ?",
                dataset.getId());
    }

    /** SQL for fetching the dataset level hierarchies for a dataset. */
    private static final String DATASET_LEVELS_HIERARCHY_SQL =
        "select distinct(dl.dataset_level_id), hierarchy"
            + " from problem_hierarchy ph"
            + " left join problem using (problem_id)"
            + " left join dataset_level dl using (dataset_level_id)"
            + " where ph.dataset_id = ?"
            + " order by hierarchy asc";

    /**
     * Map from dataset level hierarchies to dataset level id's.
     * @param dataset fetch the dataset levels for this dataset
     * @return map from dataset level hierarchies to dataset level id's
     */
    public SortedMap<String, Integer> getLevelHierarchiesForDataset(final DatasetItem dataset) {
        String sql = DATASET_LEVELS_HIERARCHY_SQL;

        final List<Object[]> results = executeSQLQuery(sql, new PrepareQuery() {
            public void prepareQuery(SQLQuery query) {
                query.setInteger(0, (Integer)dataset.getId());
                addScalars(query, "dataset_level_id", INTEGER, "hierarchy", STRING);
            }
        });

        return new TreeMap<String, Integer>() { {
            for (Object[] result : results) { put((String)result[1], (Integer)result[0]); }
        } };
    }
}

