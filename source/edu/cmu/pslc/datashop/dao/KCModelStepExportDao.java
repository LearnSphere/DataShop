/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2016
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dto.StepExportRow;

import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.KCModelStepExportItem;

/**
 * KCModelStepExport Data Access Object Interface.
 *
 * @author Cindy Tipper
 * <!-- $KeyWordsOff: $ -->
 */
public interface KCModelStepExportDao extends AbstractDao {

    /**
     * Standard get for a KCModelStepExportItem by id.
     * @param id The id of the KCModelStepExportItem.
     * @return the matching KCModelStepExportItem or null if none found
     */
    KCModelStepExportItem get(Long id);

    /**
     * Standard find for an KCModelStepExportItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired KCModelStepExportItem.
     * @return the matching KCModelStepExportItem.
     */
    KCModelStepExportItem find(Long id);

    /**
     * Standard "find all" for KCModelStepExportItems.
     * @return a List of objects
     */
    List<KCModelStepExportItem> findAll();

    //
    // Non-standard methods begin.
    //

    /**
     * Find all the KC model step exports for the given dataset.
     * @param datasetItem the dataset item
     * @param offset starting row
     * @param batchSize max number of rows to return
     * @return a list of KC model export items
     */
    List<KCModelStepExportItem> find(DatasetItem datasetItem, int offset, int batchSize);

    /**
     * Find the StepExportRow items for the given dataset.
     * @param datasetItem the dataset item
     * @param offset starting row
     * @param batchSize max number of rows to return
     * @return a list of StepExportRow items
     */
    List<StepExportRow> findStepExportRows(DatasetItem datasetItem, int offset, int batchSize);

    /**
     * Delete all of the KC model step exports for the given dataset.
     * @param datasetItem the dataset item
     * @return number of rows deleted
     */
    Integer clear(DatasetItem datasetItem);
}
