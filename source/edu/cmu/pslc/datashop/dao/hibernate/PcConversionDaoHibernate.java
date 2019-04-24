/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SQLQuery;

import edu.cmu.pslc.datashop.dao.PcConversionDao;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.PcConversionItem;

import edu.cmu.pslc.datashop.servlet.problemcontent.MappedContentDto;

/**
 * Hibernate and Spring implementation of the PcConversionDao.
 *
 * @author
 * @version $Revision: 11834 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2014-12-05 14:42:05 -0500 (Fri, 05 Dec 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcConversionDaoHibernate extends AbstractDaoHibernate implements PcConversionDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a PcConversionItem by id.
     * @param id The id of PcConversion.
     * @return the matching PcConversionItem or null if none found
     */
    public PcConversionItem get(Long id) {
        return (PcConversionItem)get(PcConversionItem.class, id);
    }

    /**
     * Standard find for an PcConversionItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired PcConversionItem.
     * @return the matching PcConversionItem.
     */
    public PcConversionItem find(Long id) {
        return (PcConversionItem)find(PcConversionItem.class, id);
    }

    /**
     * Standard "find all" for PcConversion items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(PcConversionItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** Constant SQL query for get by tool, date and version. */
    private static final String TOOL_DATE_AND_VERSION_QUERY =
        "SELECT pc_conversion_id AS id FROM pc_conversion"
        + " WHERE conversion_tool = :conversionTool"
        + " AND content_version = :contentVersion"
        + " AND content_date = :contentDate";

    /**
     * Get PcConversionItem matching specified conversion tool, conversion date
     * and content version. Null returned if match not found.
     * @param conversionTool the conversion tool
     * @param contentDate the content date
     * @param contentVersion the content version
     * @return the PCConversionItem
     */
    public PcConversionItem getByToolDateAndVersion(String conversionTool, Date contentDate,
            String contentVersion) {
        PcConversionItem result = null;

        Session session = null;
        try {
            session = getSession();

            SQLQuery query = session.createSQLQuery(TOOL_DATE_AND_VERSION_QUERY);

            query.addScalar("id", Hibernate.LONG);
            query.setParameter("conversionTool", conversionTool);
            query.setParameter("contentVersion", contentVersion);
            query.setParameter("contentDate", contentDate);

            List<Long> dbResults = query.list();

            if (dbResults != null && dbResults.size() > 0) {
                result = get(dbResults.get(0));
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /** Constant SQL query for content versions by conversion tool. */
    private static final String CONTENT_VERSION_QUERY =
        "SELECT pc_conversion_id AS id, conversion_tool AS tool,"
        + " tool_version AS toolVersion, datashop_version AS datashopVersion,"
        + " conversion_date AS conversionDate, content_version AS contentVersion,"
        + " content_date AS contentDate, content_description AS description, path AS path"
        + " FROM pc_conversion"
        + " WHERE conversion_tool = :conversionTool"
        + " AND pc_conversion_id NOT IN"
        + " (SELECT pc_conversion_id FROM pc_conversion_dataset_map WHERE dataset_id = :datasetId)";


    /** Constant SQL for filtering by content version. */
    private static final String SEARCH_BY_STRING = " AND LOWER(content_version) LIKE :searchBy";

    /** Constant SQL for ordering content versions. */
    private static final String CONTENT_VERSION_ORDER_BY = " ORDER BY contentVersion";

    /**
     * Get a list of content versions by conversion tool, filtered. Do not
     * include conversions which are already in the pc_conversion_dataset_map
     * for the specified dataset.
     * @param conversionTool the name of the conversion tool
     * @param dataset the specified dataset
     * @param searchBy the string to filter by
     * @return list of PcConversionItems
     */
    public List<PcConversionItem> getContentVersionsByTool(String conversionTool,
                                                           DatasetItem dataset,
                                                           String searchBy) {

        List<PcConversionItem> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(CONTENT_VERSION_QUERY);

            if (StringUtils.isNotBlank(searchBy)) {
                sb.append(SEARCH_BY_STRING);
            }

            // Append ORDER BY clause
            sb.append(CONTENT_VERSION_ORDER_BY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query = addQueryScalars(query);
            query.setParameter("conversionTool", conversionTool);
            query.setParameter("datasetId", (Integer)dataset.getId());

            if (StringUtils.isNotBlank(searchBy)) {
                query.setParameter("searchBy", "%" + searchBy.toLowerCase() + "%");
            }

            List<Object[]> dbResults = query.list();

            result = new ArrayList<PcConversionItem>();

            for (Object[] o : dbResults) {
                PcConversionItem item = createPcConversionItem(o);
                result.add(item);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /** Constant SQL query for mapped content by dataset. */
    private static final String MAPPED_CONTENT_QUERY =
        "SELECT pc.pc_conversion_id AS id, pc.conversion_tool AS tool,"
        + " pc.tool_version AS toolVersion, pc.datashop_version AS datashopVersion,"
        + " pc.conversion_date AS conversionDate, pc.content_version AS contentVersion,"
        + " pc.content_date AS contentDate, pc.content_description AS description,"
        + " pc.path AS path,"
        + " map.status AS status, map.num_problems_mapped AS numProblems"
        + " FROM pc_conversion_dataset_map AS map"
        + " JOIN pc_conversion pc USING (pc_conversion_id)"
        + " WHERE map.dataset_id = :datasetId";

    /**
     * Get a list of mapped content versions by dataset.
     * @param dataset the dataset to match
     * @return list of MappedContentDto objects
     */
    public List<MappedContentDto> getMappedContent(DatasetItem dataset) {

        List<MappedContentDto> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(MAPPED_CONTENT_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());
            query = addQueryScalars(query);
            query.addScalar("status", Hibernate.STRING);
            query.addScalar("numProblems", Hibernate.LONG);
            query.setParameter("datasetId", (Integer)dataset.getId());

            List<Object[]> dbResults = query.list();

            result = new ArrayList<MappedContentDto>();

            int STATUS_INDEX = 9;
            int NUM_PROBLEMS_INDEX = 10;

            for (Object[] objArray : dbResults) {
                PcConversionItem item = createPcConversionItem(objArray);

                MappedContentDto dto = new MappedContentDto(item);
                dto.setStatus(StringUtils.capitalize((String)objArray[STATUS_INDEX]));
                dto.setNumProblemsMapped((Long)objArray[NUM_PROBLEMS_INDEX]);

                result.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /** Constant SQL query for mapped conversions by dataset. */
    private static final String MAPPED_BY_DATASET_QUERY =
        "SELECT pc.pc_conversion_id AS id, pc.conversion_tool AS tool,"
        + " pc.tool_version AS toolVersion, pc.datashop_version AS datashopVersion,"
        + " pc.conversion_date AS conversionDate, pc.content_version AS contentVersion,"
        + " pc.content_date AS contentDate, pc.content_description AS description,"
        + " pc.path AS path"
        + " FROM pc_conversion_dataset_map AS map"
        + " JOIN pc_conversion pc USING (pc_conversion_id)"
        + " WHERE map.dataset_id = :datasetId"
        + " AND map.status = 'complete'"
        + " AND map.num_problems_mapped > 0";

    /**
     * Get a list of mapped conversions by dataset. Only the conversions
     * with a status of 'complete' and num_problems_mapped > 0 are included.
     * @param dataset the dataset to match
     * @return list of PcConversionItem objects
     */
    public List<PcConversionItem> getMappedByDataset(DatasetItem dataset) {

        List<PcConversionItem> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(MAPPED_BY_DATASET_QUERY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query = addQueryScalars(query);
            query.setParameter("datasetId", (Integer)dataset.getId());

            List<Object[]> dbResults = query.list();

            result = new ArrayList<PcConversionItem>();

            for (Object[] objArray : dbResults) {
                PcConversionItem item = createPcConversionItem(objArray);
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
    private static final String CONTENT_VERSION_FILTERED_QUERY =
        "SELECT DISTINCT pc_conversion_id AS id, conversion_tool AS tool,"
        + " tool_version AS toolVersion, datashop_version AS datashopVersion,"
        + " conversion_date AS conversionDate, content_version AS contentVersion,"
        + " content_date AS contentDate, content_description AS description, path AS path"
        + " FROM pc_conversion";

    /** Constant SQL for searching by conversion tool." */
    private static final String TOOL_SEARCH_BY_STRING = " conversion_tool = :conversionTool";

    /** Constant SQL for filtering by content version. */
    private static final String CV_SEARCH_BY_STRING = " LOWER(content_version) LIKE :cvSearchBy";

    /** Constant SQL for filtering by dataset name. */
    private static final String DS_SEARCH_BY_STRING =
        " JOIN pc_conversion_dataset_map USING (pc_conversion_id)"
        + " JOIN ds_dataset USING (dataset_id)"
        + " WHERE"
        + " LOWER(dataset_name) LIKE :dsSearchBy";

    /** Constant SQL for filtering by mapped content. */
    private static final String PC_MAPPED_STRING =
        " pc_conversion_id IN"
        + " (SELECT pc_conversion_id FROM pc_conversion_dataset_map)";

    /** Constant SQL for filtering by unmapped content. */
    private static final String PC_UNMAPPED_STRING =
        " pc_conversion_id NOT IN"
        + " (SELECT pc_conversion_id FROM pc_conversion_dataset_map)";

    /**
     * Get a list of content versions, narrowed by one or more of the following:
     * conversion tool, content version, dataset name and whether or not
     * the content is mapped.
     * @param conversionTool the name of the conversion tool
     * @param contentVersionSearchBy the string to filter by
     * @param datasetSearchBy the string to filter by
     * @param mapped if content is mapped to one or more datasets
     * @return list of PcConversionItems
     */
    public List<PcConversionItem> getContentVersionsFiltered(String conversionTool,
                                                             String contentVersionSearchBy,
                                                             String datasetSearchBy,
                                                             Boolean mapped) {

        List<PcConversionItem> result = null;

        Session session = null;
        try {
            session = getSession();
            StringBuffer sb = new StringBuffer(CONTENT_VERSION_FILTERED_QUERY);

            // Add datasetSearchBy first as it requires a JOIN...
            if (StringUtils.isNotBlank(datasetSearchBy)) {
                sb.append(DS_SEARCH_BY_STRING);
            }

            if (StringUtils.isNotBlank(conversionTool)) {
                int whereIndex = sb.indexOf("WHERE");
                if (whereIndex < 0) { sb.append(" WHERE"); }
                if (whereIndex > 0) { sb.append(" AND"); }
                sb.append(TOOL_SEARCH_BY_STRING);
            }

            if (StringUtils.isNotBlank(contentVersionSearchBy)) {
                int whereIndex = sb.indexOf("WHERE");
                if (whereIndex < 0) { sb.append(" WHERE"); }
                if (whereIndex > 0) { sb.append(" AND"); }
                sb.append(CV_SEARCH_BY_STRING);
            }

            if (mapped != null) {
                int whereIndex = sb.indexOf("WHERE");
                if (whereIndex < 0) { sb.append(" WHERE"); }
                if (whereIndex > 0) { sb.append(" AND"); }
                if (mapped) {
                    sb.append(PC_MAPPED_STRING);
                } else {
                    sb.append(PC_UNMAPPED_STRING);
                }
            }

            // Append ORDER BY clause
            sb.append(CONTENT_VERSION_ORDER_BY);

            SQLQuery query = session.createSQLQuery(sb.toString());

            query = addQueryScalars(query);

            if (StringUtils.isNotBlank(datasetSearchBy)) {
                query.setParameter("dsSearchBy", "%" + datasetSearchBy.toLowerCase() + "%");
            }
            if (StringUtils.isNotBlank(conversionTool)) {
                query.setParameter("conversionTool", conversionTool);
            }
            if (StringUtils.isNotBlank(contentVersionSearchBy)) {
                query.setParameter("cvSearchBy", "%" + contentVersionSearchBy.toLowerCase() + "%");
            }

            List<Object[]> dbResults = query.list();

            result = new ArrayList<PcConversionItem>();

            for (Object[] o : dbResults) {
                PcConversionItem item = createPcConversionItem(o);
                result.add(item);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }

        return result;
    }

    /**
     * Helper method to add common scalars to SQL query.
     * @param query the SQLQuery
     * @return the updated query
     */
    private SQLQuery addQueryScalars(SQLQuery query) {
        query.addScalar("id", Hibernate.LONG);
        query.addScalar("tool", Hibernate.STRING);
        query.addScalar("toolVersion", Hibernate.STRING);
        query.addScalar("datashopVersion", Hibernate.STRING);
        query.addScalar("conversionDate", Hibernate.TIMESTAMP);
        query.addScalar("contentVersion", Hibernate.STRING);
        query.addScalar("contentDate", Hibernate.TIMESTAMP);
        query.addScalar("description", Hibernate.STRING);
        query.addScalar("path", Hibernate.STRING);

        return query;
    }

    /**
     * Create the PcConversionItem
     * @param objArray the object array containing the row data
     * @return the PcConversionItem
     */
    private PcConversionItem createPcConversionItem(Object[] objArray) {

        PcConversionItem item = new PcConversionItem();

        int index = 0;
        item.setId((Long)objArray[index++]);
        item.setConversionTool((String)objArray[index++]);
        item.setToolVersion((String)objArray[index++]);
        item.setDatashopVersion((String)objArray[index++]);
        item.setConversionDate((Date)objArray[index++]);
        item.setContentVersion((String)objArray[index++]);
        item.setContentDate((Date)objArray[index++]);
        item.setContentDescription((String)objArray[index++]);
        item.setPath((String)objArray[index++]);

        return item;
    }
}
