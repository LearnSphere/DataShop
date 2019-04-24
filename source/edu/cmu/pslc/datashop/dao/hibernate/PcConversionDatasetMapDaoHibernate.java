/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dao.DaoFactory;
import edu.cmu.pslc.datashop.dao.PcConversionDatasetMapDao;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapId;
import edu.cmu.pslc.datashop.item.PcConversionDatasetMapItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.ProjectItem;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

/**
 * Hibernate and Spring implementation of the PcConversionDatasetMapDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcConversionDatasetMapDaoHibernate
    extends AbstractDaoHibernate implements PcConversionDatasetMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public PcConversionDatasetMapItem get(PcConversionDatasetMapId id) {
        return (PcConversionDatasetMapItem)get(PcConversionDatasetMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public PcConversionDatasetMapItem find(PcConversionDatasetMapId id) {
        return (PcConversionDatasetMapItem)find(PcConversionDatasetMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List<PcConversionDatasetMapItem> findAll() {
        return findAll(PcConversionDatasetMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByPcConversion method. */
    private static final String FIND_BY_PCCONVERSION_HQL
            = "from PcConversionDatasetMapItem map"
            + " where map.pcConversion = ?";

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param pcConversionItem the given pcConversion item
     *  @return a list of items
     */
    public List<PcConversionDatasetMapItem> findByPcConversion(PcConversionItem pcConversionItem) {
        Object[] params = {pcConversionItem};
        List<PcConversionDatasetMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_PCCONVERSION_HQL, params);
        return itemList;
    }

    /** HQL query for the findByDataset method. */
    private static final String FIND_BY_DATASET_HQL =
        "from PcConversionDatasetMapItem map" + " where dataset = ?";

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param datasetItem the given DATASET item
     *  @return a list of items
     */
    public List<PcConversionDatasetMapItem> findByDataset(DatasetItem datasetItem) {
        Object[] params = {datasetItem};
        List<PcConversionDatasetMapItem> itemList =
            getHibernateTemplate().find(FIND_BY_DATASET_HQL, params);
        return itemList;
    }

    /** HQL query for the findByDataset method. */
    private static final String FIND_MAPPED_BY_DATASET_HQL =
        "from PcConversionDatasetMapItem map" + " where dataset = ?"
        + " and map.numProblemsMapped > 0";

    /**
     *  Returns whether or not the dataset has problem content mapping.
     *  @param datasetItem the given DATASET item
     *  @return whether or not the dataset has problem content mapping
     */
    public boolean isDatasetMapped(DatasetItem datasetItem) {
        Object[] params = {datasetItem};
        boolean isMapped = false;
        List<PcConversionDatasetMapItem> itemList =
            getHibernateTemplate().find(FIND_MAPPED_BY_DATASET_HQL, params);
        if (itemList != null && !itemList.isEmpty()) {
            isMapped = true;
        }
        return isMapped;
    }

    /** HQL query for the findByDataset method. */
    private static final String FIND_MAPPED_BY_CONVERSION_HQL =
        "select dataset from PcConversionDatasetMapItem map where pc_conversion_id = ?";

    /**
     *  Returns a list of datasets mapped to the given PcConversionItem.
     *  @param pcConversionItem the given PcConversionItem
     *  @return a list of datasets
     */
    public List<DatasetItem> findDatasets(PcConversionItem pcConversionItem) {
        Object[] params = {pcConversionItem};
        // Return any datasets that have mappings to this PcConversionItem
        List<DatasetItem> itemList =
            getHibernateTemplate().find(FIND_MAPPED_BY_CONVERSION_HQL, params);
        return itemList;
    }

    /** Constant SQL query for content versions, filtered. */
    private static final String CONVERSION_BY_PROJECT_QUERY =
        "SELECT dataset_id AS datasetId, pc_conversion_id AS pcConversionId,"
        + " map.status AS status, num_problems_mapped AS numProblemsMapped,"
        + " mapped_time AS mappedTime, mapped_by AS mappedBy"
        + " FROM pc_conversion_dataset_map map"
        + " JOIN ds_dataset ds USING (dataset_id)"
        + " WHERE ds.project_id = :projectId";

    /**
     *  Return a list of PcConversionDatasetMapItems.
     *  @param projectItem the given PROJECT item
     *  @return a list of items
     */
    public List<PcConversionDatasetMapItem> findByProject(ProjectItem projectItem) {

        List<PcConversionDatasetMapItem> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(CONVERSION_BY_PROJECT_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query.addScalar("datasetId", Hibernate.INTEGER);
            query.addScalar("pcConversionId", Hibernate.LONG);
            query.addScalar("status", Hibernate.STRING);
            query.addScalar("numProblemsMapped", Hibernate.INTEGER);
            query.addScalar("mappedTime", Hibernate.TIMESTAMP);
            query.addScalar("mappedBy", Hibernate.STRING);

            query.setParameter("projectId", (Integer)projectItem.getId());

            List<Object[]> dbResults = query.list();

            result = new ArrayList<PcConversionDatasetMapItem>();

            for (Object[] o : dbResults) {
                PcConversionDatasetMapItem item = new PcConversionDatasetMapItem();

                int index = 0;
                Integer datasetId = (Integer)o[index++];
                Long pcConversionId = (Long)o[index++];
                PcConversionDatasetMapId id =
                    new PcConversionDatasetMapId(pcConversionId, datasetId);
                item.setId(id);
                item.setStatus((String)o[index++]);
                item.setNumProblemsMapped(new Long((Integer)o[index++]));
                item.setMappedTime((Date)o[index++]);
                item.setMappedBy(DaoFactory.DEFAULT.getUserDao().get((String)o[index++]));

                result.add(item);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /** Constant SQL query for content versions, filtered. */
    private static final String DATASET_COUNT_QUERY =
        "SELECT DISTINCT(dataset_id) AS datasetId"
        + " FROM pc_conversion_dataset_map"
        + " JOIN ds_dataset ds USING (dataset_id)"
        + " WHERE ds.project_id = :projectId GROUP BY dataset_id";

    /**
     *  Determine number of datasets that have Problem Content, in specified project.
     *  @param projectItem the given Project item
     *  @return the count
     */
    public Integer getNumDatasetsWithProblemContent(ProjectItem projectItem) {

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(DATASET_COUNT_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query.addScalar("datasetId", Hibernate.INTEGER);
            query.setParameter("projectId", (Integer)projectItem.getId());

            List<Object[]> dbResults = query.list();

            return dbResults.size();

        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
    }
}
