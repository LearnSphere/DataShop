/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import edu.cmu.pslc.datashop.dao.KCModelStepExportDao;
import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.KCModelStepExportItem;

/**
 * Hibernate and Spring implementation of the KCModelStepExportDao.
 *
 * @author Cindy Tipper
 * <!-- $KeyWordsOff: $ -->
 */
public class KCModelStepExportDaoHibernate
    extends AbstractDaoHibernate implements KCModelStepExportDao
{
    /**
     * Standard get for a KCModelStepExportItem by id.
     * @param id The id of the user.
     * @return the matching KCModelStepExportItem or null if none found
     */
    public KCModelStepExportItem get(Long id) {
        if (id == null) { return null; }
        return (KCModelStepExportItem)get(KCModelStepExportItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<KCModelStepExportItem> findAll() {
        return findAll(KCModelStepExportItem.class);
    }

    /**
     * Standard find for an KCModelStepExportItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired KCModelStepExportItem.
     * @return the matching KCModelStepExportItem.
     */
    public KCModelStepExportItem find(Long id) {
        return (KCModelStepExportItem)find(KCModelStepExportItem.class, id);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the KC model step exports for the given dataset.
     * @param datasetItem the dataset item
     * @param offset starting row
     * @param batchSize max number of rows to return
     * @return a list of KCModelStepExport items
     */
    public List<KCModelStepExportItem> find(DatasetItem datasetItem, int offset, int batchSize) {
        DetachedCriteria criteria = DetachedCriteria.forClass(KCModelStepExportItem.class);
        criteria.add(Restrictions.eq("dataset", datasetItem));
        return getHibernateTemplate().findByCriteria(criteria, offset, batchSize);
    }

    /**
     * Find the StepExportRow items for the given dataset.
     * @param datasetItem the dataset item
     * @param offset starting row
     * @param batchSize max number of rows to return
     * @return a list of step export rows
     */
    public List<StepExportRow> findStepExportRows(DatasetItem datasetItem,
                                                  int offset, int batchSize) {
        List<KCModelStepExportItem> items = find(datasetItem, offset, batchSize);

        List<StepExportRow> rows = new ArrayList<StepExportRow>();

        for (KCModelStepExportItem i : items) {
            StepExportRow rowDTO = new StepExportRow();
            rowDTO.setStepId((Long)i.getStep().getId());
            rowDTO.setStepGuid(i.getStepGuid());
            rowDTO.setProblemHierarchy(i.getProblemHierarchy());
            rowDTO.setProblemName(i.getProblemName());
            rowDTO.setStepName(i.getStepName());
            rowDTO.setMaxProblemView(i.getMaxProblemView());
            rowDTO.setAvgIncorrects(i.getAvgIncorrects());
            rowDTO.setAvgHints(i.getAvgHints());
            rowDTO.setAvgCorrects(i.getAvgCorrects());
            rowDTO.setPctIncorrectFirstAttempts(i.getPctIncorrectFirstAttempts());
            rowDTO.setPctHintFirstAttempts(i.getPctHintFirstAttempts());
            rowDTO.setPctCorrectFirstAttempts(i.getPctCorrectFirstAttempts());

            // Sigh. The duration setters convert to seconds but these
            // values have already been converted.
            rowDTO.setAvgStepDurationNoConvert(i.getAvgStepDuration());
            rowDTO.setAvgCorrectStepDurationNoConvert(i.getAvgCorrectStepDuration());
            rowDTO.setAvgErrorStepDurationNoConvert(i.getAvgErrorStepDuration());

            rowDTO.setTotalStudents(i.getTotalStudents());
            rowDTO.setTotalOpportunities(i.getTotalOpportunities());
            rows.add(rowDTO);
        }

        return rows;
    }

    /**
     * Delete all of the KC model step exports for the given dataset.
     * @param datasetItem the dataset item
     * @return number of rows deleted
     */
    public Integer clear(DatasetItem datasetItem) {
        if (datasetItem == null) {
            throw new IllegalArgumentException("Dataset cannot be null.");
        }
        int rowCount = 0;
        String query = "delete from kcm_step_export where dataset_id = ?";
        Session session = getSession();
        try {
            PreparedStatement ps = session.connection().prepareStatement(query);
            ps.setLong(1, ((Integer)datasetItem.getId()).longValue());
            rowCount = ps.executeUpdate();
            if (logger.isDebugEnabled()) {
                logger.debug("deleteByDataset (Dataset " + datasetItem.getId()
                             + "). Deleted " + rowCount + " rows.");
            }
        } catch (SQLException exception) {
            logger.error("deleteByDataset (Dataset " + datasetItem.getId()
                         + ") SQLException occurred.", exception);
        } finally {
            releaseSession(session);
        }
        return rowCount;
    }
}
