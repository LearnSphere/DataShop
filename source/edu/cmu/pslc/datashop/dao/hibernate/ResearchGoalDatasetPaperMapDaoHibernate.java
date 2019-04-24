/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.ResearchGoalDatasetPaperMapDao;
import edu.cmu.pslc.datashop.servlet.research.ResearchGoalDto;
import edu.cmu.pslc.datashop.servlet.research.RgDatasetDto;
import edu.cmu.pslc.datashop.servlet.research.RgPaperDto;
import edu.cmu.pslc.datashop.servlet.research.RgPaperWithGoalsDto;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapId;
import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapItem;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;

/**
 * Hibernate and Spring implementation of the ResearchGoalDatasetPaperMapDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResearchGoalDatasetPaperMapDaoHibernate
        extends AbstractDaoHibernate implements ResearchGoalDatasetPaperMapDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    public ResearchGoalDatasetPaperMapItem get(ResearchGoalDatasetPaperMapId id) {
        return (ResearchGoalDatasetPaperMapItem)get(ResearchGoalDatasetPaperMapItem.class, id);
    }

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    public ResearchGoalDatasetPaperMapItem find(ResearchGoalDatasetPaperMapId id) {
        return (ResearchGoalDatasetPaperMapItem)find(ResearchGoalDatasetPaperMapItem.class, id);
    }

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    public List findAll() {
        return findAll(ResearchGoalDatasetPaperMapItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /** HQL query for the findByGoal method. */
    private static final String FIND_BY_GOAL_HQL
            = "from ResearchGoalDatasetPaperMapItem map"
            + " where researchGoal = ?";

    /**
     *  Return a list of ResearchGoalDatasetPaperMapItems.
     *  @param goalItem the given goal item
     *  @return a list of items
     */
    public List<ResearchGoalDatasetPaperMapItem> findByGoal(ResearchGoalItem goalItem) {
        Object[] params = {goalItem};
        List<ResearchGoalDatasetPaperMapItem> itemList =
                getHibernateTemplate().find(FIND_BY_GOAL_HQL, params);
        return itemList;
    }

    /** Native SQL to fill in DTO. */
    private static final String DATASET_SQL =
            "SELECT distinct dataset.dataset_id as datasetId,"
          + " dataset.dataset_name as datasetName"
          + " FROM research_goal_dataset_paper_map map"
          + " JOIN ds_dataset dataset ON map.dataset_id = dataset.dataset_id"
          + " WHERE map.research_goal_id = :goalId"
          + " ORDER BY dataset.dataset_name";

    /**
     * Get a list of research goal dataset DTOs.
     * @param goalItem the given research goal
     * @return a list of DTOs
     */
    public List<RgDatasetDto> getDatasets(ResearchGoalItem goalItem) {
        List<RgDatasetDto> dtoList = new ArrayList<RgDatasetDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(DATASET_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("datasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("datasetName", Hibernate.STRING);

            //parameters
            sqlQuery.setParameter("goalId", goalItem.getId());

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                RgDatasetDto dto = new RgDatasetDto();
                dto.setDatasetId((Integer)obj[colIdx++]);
                dto.setDatasetName((String)obj[colIdx++]);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String PAPER_SQL =
            "SELECT paper.citation as citation,"
          + " file.file_id as fileId,"
          + " file.actual_file_name as fileName,"
          + " file.file_size as fileSize"
          + " FROM research_goal_dataset_paper_map map"
          + " JOIN ds_dataset dataset ON map.dataset_id = dataset.dataset_id"
          + " JOIN paper paper ON map.paper_id = paper.paper_id"
          + " JOIN ds_file file ON paper.file_id = file.file_id"
          + " WHERE map.research_goal_id = :goalId"
          + " AND map.dataset_id = :datasetId"
          + " ORDER BY paper.paper_id desc";

    /**
     * Get a list of research goal paper DTOs.
     * @param goalItem the given research goal
     * @param datasetId the given dataset
     * @return a list of DTOs
     */
    public List<RgPaperDto> getPapers(ResearchGoalItem goalItem, Integer datasetId) {
        List<RgPaperDto> dtoList = new ArrayList<RgPaperDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(PAPER_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("citation", Hibernate.STRING);
            sqlQuery.addScalar("fileId", Hibernate.INTEGER);
            sqlQuery.addScalar("fileName", Hibernate.STRING);
            sqlQuery.addScalar("fileSize", Hibernate.LONG);

            //parameters
            sqlQuery.setParameter("goalId", goalItem.getId());
            sqlQuery.setParameter("datasetId", datasetId);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                RgPaperDto dto = new RgPaperDto();
                dto.setCitation((String)obj[colIdx++]);
                dto.setFileId((Integer)obj[colIdx++]);
                dto.setFileName((String)obj[colIdx++]);
                Long fileSize = (Long)obj[colIdx++];
                String nameWithSize = dto.getFileName()
                        + " (" + FileItem.getDisplayFileSize(fileSize) + ")";
                dto.setFileNameWithSize(nameWithSize);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String PAPERS_GIVEN_RG_SQL =
            "SELECT distinct paper.paper_id as paperId,"
          + " paper.citation as citation,"
          + " dataset.dataset_id as datasetId,"
          + " dataset.dataset_name as datasetName"
          + " FROM paper paper"
          + " LEFT JOIN research_goal_dataset_paper_map map on map.paper_id = paper.paper_id"
          + " LEFT JOIN paper_dataset_map pdmap on pdmap.paper_id = paper.paper_id"
          + " LEFT JOIN ds_dataset dataset on dataset.dataset_id = pdmap.dataset_id"
          + " WHERE research_goal_id = :goalId"
          + " ORDER BY paperId desc";

    /**
     * Get a list of papers for a given research goal.
     * @param goalItem the research goal
     * @return a list of DTOs
     */
    public List<RgPaperWithGoalsDto> getPapersGivenGoal(ResearchGoalItem goalItem) {
        List<RgPaperWithGoalsDto> dtoList = new ArrayList<RgPaperWithGoalsDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(PAPERS_GIVEN_RG_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("paperId", Hibernate.INTEGER);
            sqlQuery.addScalar("citation", Hibernate.STRING);
            sqlQuery.addScalar("datasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("datasetName", Hibernate.STRING);
            //parameters
            sqlQuery.setParameter("goalId", goalItem.getId());

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                RgPaperWithGoalsDto dto = new RgPaperWithGoalsDto();
                RgPaperDto paperDto = new RgPaperDto();
                paperDto.setPaperId((Integer)obj[colIdx++]);
                paperDto.setCitation((String)obj[colIdx++]);
                paperDto.setDatasetId((Integer)obj[colIdx++]);
                paperDto.setDatasetName((String)obj[colIdx++]);
                dto.setPaperDto(paperDto);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String PAPERS_WITH_RG_SQL =
            "SELECT distinct paper.paper_id as paperId,"
          + " paper.citation as citation,"
          + " file.file_id as fileId,"
          + " file.actual_file_name as fileName,"
          + " count(*) as goalCount,"
          + " dataset.dataset_id as datasetId,"
          + " dataset.dataset_name as datasetName"
          + " FROM paper paper"
          + " JOIN ds_file file ON paper.file_id = file.file_id"
          + " LEFT JOIN research_goal_dataset_paper_map map on map.paper_id = paper.paper_id"
          + " LEFT JOIN ds_dataset dataset on dataset.dataset_id = map.dataset_id"
          + " WHERE research_goal_id IS NOT NULL"
          + " GROUP BY paper.paper_id"
          + " ORDER BY datasetName, citation";

    /**
     * Get a list of papers with Research Goals.
     * @return a list of DTOs
     */
    public List<RgPaperWithGoalsDto> getPapersWithGoals() {
        List<RgPaperWithGoalsDto> dtoList = new ArrayList<RgPaperWithGoalsDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(PAPERS_WITH_RG_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("paperId", Hibernate.INTEGER);
            sqlQuery.addScalar("citation", Hibernate.STRING);
            sqlQuery.addScalar("fileId", Hibernate.INTEGER);
            sqlQuery.addScalar("fileName", Hibernate.STRING);
            sqlQuery.addScalar("goalCount", Hibernate.LONG);
            sqlQuery.addScalar("datasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("datasetName", Hibernate.STRING);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                RgPaperWithGoalsDto dto = new RgPaperWithGoalsDto();
                RgPaperDto paperDto = new RgPaperDto();
                paperDto.setPaperId((Integer)obj[colIdx++]);
                paperDto.setCitation((String)obj[colIdx++]);
                paperDto.setFileId((Integer)obj[colIdx++]);
                paperDto.setFileName((String)obj[colIdx++]);
                dto.setPaperDto(paperDto);
                dto.setGoalCount((Long)obj[colIdx++]);
                paperDto.setDatasetId((Integer)obj[colIdx++]);
                paperDto.setDatasetName((String)obj[colIdx++]);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String PAPERS_WITHOUT_RG_SQL =
            "SELECT distinct paper.paper_id as paperId,"
          + " paper.citation as citation,"
          + " file.file_id as fileId,"
          + " file.actual_file_name as fileName,"
          + " dataset.dataset_id as datasetId,"
          + " dataset.dataset_name as datasetName"
          + " FROM paper paper"
          + " JOIN ds_file file ON paper.file_id = file.file_id"
          + " JOIN paper_dataset_map pdmap on paper.paper_id = pdmap.paper_id"
          + " JOIN ds_dataset dataset on dataset.dataset_id = pdmap.dataset_id"
          + " LEFT JOIN research_goal_dataset_paper_map map on map.paper_id = paper.paper_id"
          + " WHERE research_goal_id IS NULL"
          + " ORDER BY datasetName, citation";

    /**
     * Get a list of papers without Research Goals.
     * @return a list of DTOs
     */
    public List<RgPaperDto> getPapersWithoutGoals() {
        List<RgPaperDto> dtoList = new ArrayList<RgPaperDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(PAPERS_WITHOUT_RG_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("paperId", Hibernate.INTEGER);
            sqlQuery.addScalar("citation", Hibernate.STRING);
            sqlQuery.addScalar("fileId", Hibernate.INTEGER);
            sqlQuery.addScalar("fileName", Hibernate.STRING);
            sqlQuery.addScalar("datasetId", Hibernate.INTEGER);
            sqlQuery.addScalar("datasetName", Hibernate.STRING);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                RgPaperDto paperDto = new RgPaperDto();
                paperDto.setPaperId((Integer)obj[colIdx++]);
                paperDto.setCitation((String)obj[colIdx++]);
                paperDto.setFileId((Integer)obj[colIdx++]);
                paperDto.setFileName((String)obj[colIdx++]);
                paperDto.setDatasetId((Integer)obj[colIdx++]);
                paperDto.setDatasetName((String)obj[colIdx++]);
                dtoList.add(paperDto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String GOALS_GIVEN_PAPER_SQL =
            "SELECT distinct goal.research_goal_id as goalId,"
          + " goal.title as title"
          + " FROM research_goal goal"
          + " LEFT JOIN research_goal_dataset_paper_map map"
              + " ON map.research_goal_id = goal.research_goal_id"
          + " WHERE map.paper_id = :paperId";

    /**
     * Get a list of research goals for the given paper.
     * @param paperId the id of the given paper
     * @return a list of DTOs
     */
    public List<ResearchGoalDto> getGoalsGivenPaper(Integer paperId) {
        List<ResearchGoalDto> dtoList = new ArrayList<ResearchGoalDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(GOALS_GIVEN_PAPER_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("goalId", Hibernate.INTEGER);
            sqlQuery.addScalar("title", Hibernate.STRING);
            //parameters
            sqlQuery.setParameter("paperId", paperId);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ResearchGoalDto dto = new ResearchGoalDto();
                dto.setId((Integer)obj[colIdx++]);
                dto.setTitle((String)obj[colIdx++]);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL to fill in DTO. */
    private static final String PAPER_OTHER_GOALS_SQL =
           "SELECT research_goal_id as goalId,"
         + " title as title, "
         + " goal_order as goalOrder"
         + " FROM research_goal"
         + " WHERE research_goal_id NOT IN"
             + " (SELECT research_goal_id"
              + " FROM research_goal_dataset_paper_map map"
              + " WHERE map.paper_id = :paperId)"
              + " ORDER by goal_order, title";


    /**
     * Get a list of other research goals not associated with the given paper.
     * @param paperId the id of the given paper
     * @return a list of DTOs
     */
    public List<ResearchGoalDto> paperGetOtherGoals(Integer paperId) {
        List<ResearchGoalDto> dtoList = new ArrayList<ResearchGoalDto>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(PAPER_OTHER_GOALS_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("goalId", Hibernate.INTEGER);
            sqlQuery.addScalar("title", Hibernate.STRING);
            sqlQuery.addScalar("goalOrder", Hibernate.INTEGER);
            //parameters
            sqlQuery.setParameter("paperId", paperId);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ResearchGoalDto dto = new ResearchGoalDto();
                dto.setId((Integer)obj[colIdx++]);
                dto.setTitle((String)obj[colIdx++]);
                dto.setOrder((Integer)obj[colIdx++]);
                dtoList.add(dto);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return dtoList;
    }

    /** Native SQL for getTypesInOrder. */
    private static final String ORDER_TYPES_BY_NUM_PAPERS_SQL
        = "SELECT researcher_type_id AS id,"
        + " label AS label,"
        + " type_order AS typeOrder,"
        + " (SELECT count(*)"
         + " FROM researcher_type_research_goal_map map"
         + " JOIN research_goal_dataset_paper_map pmap"
           + " ON pmap.research_goal_id = map.research_goal_id"
         + " WHERE map.researcher_type_id = rt.researcher_type_id) AS numPapers"
        + " FROM researcher_type rt"
        + " ORDER BY numPapers DESC, lower(label)";


    /**
     * Get the types in order of the number of papers in each (descending)
     * and by label (ascending).
     * @return a list of researcher type items in order
     */
    public List<ResearcherTypeItem> getTypesInOrder() {
        List<ResearcherTypeItem> itemList = new ArrayList<ResearcherTypeItem>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(ORDER_TYPES_BY_NUM_PAPERS_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("id", Hibernate.INTEGER);
            sqlQuery.addScalar("label", Hibernate.STRING);
            sqlQuery.addScalar("typeOrder", Hibernate.INTEGER);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ResearcherTypeItem item = new ResearcherTypeItem();
                item.setId((Integer)obj[colIdx++]);
                item.setLabel((String)obj[colIdx++]);
                item.setTypeOrder((Integer)obj[colIdx++]);
                itemList.add(item);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return itemList;
    }

    /** SQL query for the getGoalsInOrder method. */
    private static final String ORDER_GOALS_BY_NUM_PAPERS_SQL
        = "SELECT research_goal_id as id,"
        + " title,"
        + " description,"
        + " goal_order as goalOrder,"
        + "  (SELECT count(*)"
          + " FROM research_goal_dataset_paper_map map"
          + " WHERE map.research_goal_id = rg.research_goal_id) AS numPapers"
        + " FROM research_goal rg"
        + " ORDER BY numPapers DESC, lower(title)";

    /**
     * Get the goals in order of the number of papers in each (descending)
     * and by title (ascending).
     * @return a list of research goal items in order
     */
    public List<ResearchGoalItem> getGoalsInOrder() {
        List<ResearchGoalItem> itemList = new ArrayList<ResearchGoalItem>();
        Session session = null;
        try {
            session = getSession();
            SQLQuery sqlQuery = session.createSQLQuery(ORDER_GOALS_BY_NUM_PAPERS_SQL);

            //indicate which columns map to which types.
            sqlQuery.addScalar("id", Hibernate.INTEGER);
            sqlQuery.addScalar("title", Hibernate.STRING);
            sqlQuery.addScalar("description", Hibernate.STRING);
            sqlQuery.addScalar("goalOrder", Hibernate.INTEGER);

            List<Object[]> dbResults = sqlQuery.list();
            for (Object[] obj: dbResults) {
                int colIdx = 0;
                ResearchGoalItem item = new ResearchGoalItem();
                item.setId((Integer)obj[colIdx++]);
                item.setTitle((String)obj[colIdx++]);
                item.setDescription((String)obj[colIdx++]);
                item.setGoalOrder((Integer)obj[colIdx++]);
                itemList.add(item);
            }
        } finally {
            if (session != null) {
                releaseSession(session);
            }
        }
        return itemList;
    }

}
