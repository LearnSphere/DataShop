/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapId;
import edu.cmu.pslc.datashop.item.ResearchGoalDatasetPaperMapItem;
import edu.cmu.pslc.datashop.item.ResearchGoalItem;
import edu.cmu.pslc.datashop.item.ResearcherTypeItem;
import edu.cmu.pslc.datashop.servlet.research.ResearchGoalDto;
import edu.cmu.pslc.datashop.servlet.research.RgDatasetDto;
import edu.cmu.pslc.datashop.servlet.research.RgPaperDto;
import edu.cmu.pslc.datashop.servlet.research.RgPaperWithGoalsDto;

/**
 * ResearchGoal Dataset Paper Map Data Access Object Interface.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10435 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2013-12-19 10:07:17 -0500 (Thu, 19 Dec 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ResearchGoalDatasetPaperMapDao extends AbstractDao {

    /**
     * Standard get by id.
     * @param id The id of the item.
     * @return the matching item or null if none found
     */
    ResearchGoalDatasetPaperMapItem get(ResearchGoalDatasetPaperMapId id);

    /**
     * Standard find by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired item.
     * @return the matching item.
     */
    ResearchGoalDatasetPaperMapItem find(ResearchGoalDatasetPaperMapId id);

    /**
     * Standard "find all".
     * @return a List of item objects
     */
    List findAll();

    //
    // Non-standard methods begin.
    //

    /**
     *  Return a list of ResearchGoalDatasetPaperMapItems.
     *  @param goalItem the given goal item
     *  @return a list of items
     */
    List<ResearchGoalDatasetPaperMapItem> findByGoal(ResearchGoalItem goalItem);

    /**
     * Get a list of research goal dataset DTOs.
     * @param goalItem the given research goal
     * @return a list of DTOs
     */
    List<RgDatasetDto> getDatasets(ResearchGoalItem goalItem);

    /**
     * Get a list of research goal paper DTOs.
     * @param goalItem the given research goal
     * @param datasetId the given dataset
     * @return a list of DTOs
     */
    List<RgPaperDto> getPapers(ResearchGoalItem goalItem, Integer datasetId);

    /**
     * Get a list of papers  with Research Goals.
     * @return a list of DTOs
     */
    List<RgPaperWithGoalsDto> getPapersWithGoals();

    /**
     * Get a list of papers without Research Goals.
     * @return a list of DTOs
     */
    List<RgPaperDto> getPapersWithoutGoals();

    /**
     * Get a list of papers for a given research goal.
     * @param goalItem the research goal
     * @return a list of DTOs
     */
    List<RgPaperWithGoalsDto> getPapersGivenGoal(ResearchGoalItem goalItem);

    /**
     * Get a list of research goals for the given paper.
     * @param paperId the id of the given paper
     * @return a list of DTOs
     */
    List<ResearchGoalDto> getGoalsGivenPaper(Integer paperId);

    /**
     * Get a list of other research goals not associated with the given paper.
     * @param paperId the id of the given paper
     * @return a list of DTOs
     */
    List<ResearchGoalDto> paperGetOtherGoals(Integer paperId);

    /**
     * Get the types in order of the number of papers in each (descending)
     * and by label (ascending).
     * @return a list of researcher type items in order
     */
    List<ResearcherTypeItem> getTypesInOrder();

    /**
     * Get the goals in order of the number of papers in each (descending)
     * and by title (ascending).
     * @return a list of research goal items in order
     */
    List<ResearchGoalItem> getGoalsInOrder();
}
