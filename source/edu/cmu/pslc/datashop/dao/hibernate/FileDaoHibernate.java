/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.datalab.item.DlStudentItem;
import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.dao.hibernate.AbstractDaoHibernate.PrepareQuery;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.SampleItem;

/**
 * Hibernate and Spring implementation of the FileDao.
 *
 * @author Benjamin Billings
 * @version $Revision: 13459 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2016-08-26 12:05:41 -0400 (Fri, 26 Aug 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class FileDaoHibernate extends AbstractDaoHibernate implements FileDao {

    /**
     * Standard get for a FileItem by id.
     * @param id The id of the user.
     * @return the matching FileItem or null if none found
     */
    public FileItem get(Integer id) {
        return (FileItem)get(FileItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(FileItem.class);
    }

    /**
     * Standard find for an FileItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired FileItem.
     * @return the matching FileItem.
     */
    public FileItem find(Integer id) {
        return (FileItem)find(FileItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query to get all custom fields for a given student and problem */
    private static final String FIND_BY_PATH_AND_NAME =
          "from FileItem f"
        + " where f.filePath = ?"
        + " and f.fileName = ?";

    /**
     * Get a list of file items with the given file name and path.
     * @param path the path to the file
     * @param name the name of the file
     * @return a List of matching FileItems, though only expect one item in list
     */
    public List find(String path, String name) {
        Object [] params = new Object [2];
        params[0] = path;
        params[1] = name;

        return getHibernateTemplate().find(FIND_BY_PATH_AND_NAME, params);
    }

    /**
     * Native MySql query for retrieving a list of distinct file paths
     * for files in a given dataset.
     */
    private static final String DISTINCT_FILE_PATHS_QUERY =
        "select distinct concat(concat(file_path, '/'), actual_file_name) as filePath"
            + " from file_dataset_map"
            + " left join ds_file using (file_id)"
            + " where dataset_id = :datasetId";

    /**
     * Native MySql query for retrieving a list of distinct file paths for
     * papers in a given dataset.
     */
    private static final String DISTINCT_PAPER_PATHS_QUERY =
        "select distinct concat(concat(file_path, '/'), actual_file_name) as filePath"
            + " from paper_dataset_map as pmap"
            + " left join paper as p on p.paper_id = pmap.paper_id"
            + " left join ds_file as df on df.file_id = p.file_id"
            + " where dataset_id = :datasetId";

    /**
     * Native MySql query for retrieving a list of distinct file paths for
     * external analyses in a given dataset.
     */
    private static final String DISTINCT_EA_PATHS_QUERY =
        "select distinct concat(concat(file_path, '/'), actual_file_name) as filePath"
            + " from external_analysis_dataset_map as emap"
            + " left join external_analysis as ea on ea.external_analysis_id "
            + " = emap.external_analysis_id"
            + " left join ds_file as df on df.file_id = ea.file_id"
            + " where dataset_id = :datasetId";
    /**
     * Returns a list of distinct file paths given a dataset.
     * @param datasetItem the datasetItem
     * @return a list of distinct file paths
     */
    public List<String> getDistinctFilePaths(DatasetItem datasetItem) {
        if (datasetItem == null) {
            return null;
        }

        List<String> dbResults = new ArrayList<String>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(DISTINCT_FILE_PATHS_QUERY);
            sqlQuery.setParameter("datasetId", ((Number)datasetItem.getId()).intValue());
            dbResults = sqlQuery.list();
            sqlQuery = session.createSQLQuery(DISTINCT_PAPER_PATHS_QUERY);
            sqlQuery.setParameter("datasetId", ((Number)datasetItem.getId()).intValue());
            dbResults.addAll(sqlQuery.list());
            sqlQuery = session.createSQLQuery(DISTINCT_EA_PATHS_QUERY);
            sqlQuery.setParameter("datasetId", ((Number)datasetItem.getId()).intValue());
            dbResults.addAll(sqlQuery.list());
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dbResults;
    }

    /** HQL query to get all files for a given dataset. */
    private static final String FIND_FILES_BY_DATASET =
        "select file_id"
                + " from file_dataset_map"
                + " where dataset_id = :datasetId";

    /**
     * Get a list of file ids given the dataset item.
     * @param datasetItem the dataset item
     * @return the file ids
     */
    public List<Integer> find(DatasetItem datasetItem) {
        List<Integer> dbResults = new ArrayList<Integer>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(FIND_FILES_BY_DATASET);
            sqlQuery.setParameter("datasetId", ((Number)datasetItem.getId()).intValue());
            dbResults = sqlQuery.list();

        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dbResults;
    }


    /** HQL query to get all files for a given dataset. */
    private static final String FIND_DATASET_BY_FILE =
        "select dataset_id"
                + " from file_dataset_map"
                + " where file_id = :fileId";

    /**
     * Get a dataset id associated with given file item.
     * @param fileItem the file item
     * @return the dataset id associated with given file item
     */
    public Integer findDatasetId(FileItem fileItem) {
        Integer datasetId = null;
        Session session = null;
        try {
        	List<Integer> dbResults = new ArrayList<Integer>();
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(FIND_DATASET_BY_FILE);
            sqlQuery.setParameter("fileId", ((Number)fileItem.getId()).intValue());
            dbResults = sqlQuery.list();

            for (Integer dsId : dbResults) {
                datasetId = dsId;
                break;
            }

        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return datasetId;
    }

}
