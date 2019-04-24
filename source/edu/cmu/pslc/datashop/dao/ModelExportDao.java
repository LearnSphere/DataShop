/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import org.apache.commons.collections.MultiMap;

import edu.cmu.pslc.datashop.dto.StepExportRow;
import edu.cmu.pslc.datashop.item.DatasetItem;
import edu.cmu.pslc.datashop.item.SkillModelItem;

/**
 * Skill Model Export Database Access Object Interface.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4676 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-04-15 11:41:43 -0400 (Tue, 15 Apr 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ModelExportDao {

    /**
     * Get a back a list of the step export data for a given dataset.
     * @param dataset dataset we are exporting from
     * @param limit the number of records to return
     * @param offset the offset of the first record to return
     * @return List of StepExportRows
     */
    List <StepExportRow> getStepExport(DatasetItem dataset, Integer limit, Integer offset);

    /**
     * Get a mapping of steps to skills given a list of skill models and a dataset.
     * @param dataset dataset to get a mapping for.
     * @param skillModelList list of skill models to get skill mappings for.
     * @return a MultiMap which is a key: stepId value:Collection of skill_ids mapped to that step.
     */
    MultiMap getStepSkillMapping(DatasetItem dataset, List <SkillModelItem> skillModelList);

}


