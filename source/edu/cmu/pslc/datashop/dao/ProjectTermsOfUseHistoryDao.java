/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2011
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.Collection;
import edu.cmu.pslc.datashop.item.ProjectItem;
import edu.cmu.pslc.datashop.item.ProjectTermsOfUseHistoryItem;
import edu.cmu.pslc.datashop.item.TermsOfUseVersionItem;


/**
 * Student Data Access Object Interface.
 *
 * @author Mike Komisin
 * @version $Revision: 7388 $
 * <BR>Last modified by: $Author: mkomi $
 * <BR>Last modified on: $Date: 2011-12-06 13:08:40 -0500 (Tue, 06 Dec 2011) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface ProjectTermsOfUseHistoryDao extends AbstractDao {

    /**
     * Standard get for a ProjectTermsOfUseHistoryItem by id.
     * @param id The id of the ProjectTermsOfUseHistoryItem.
     * @return the matching ProjectTermsOfUseHistoryItem or null if none found
     */
    ProjectTermsOfUseHistoryItem get(Integer id);

    /**
     * Standard find for an ProjectTermsOfUseHistoryItem by id.
     * Only guarantees the id of the item will be filled in.
     * @param id the id of the desired ProjectTermsOfUseHistoryItem.
     * @return the matching ProjectTermsOfUseHistoryItem.
     */
    ProjectTermsOfUseHistoryItem find(Integer id);

    /**
     * Standard "find all" for ProjectTermsOfUseHistoryItems.
     * @return a List of objects
     */
    Collection findAll();

    /**
     * Returns a set of project terms of use given a specific project.
     * @param project The project item
     * @return The set of terms of use items associated with a given project
     */
    Collection findByProject(ProjectItem project);

    /**
     * Returns a set of project terms of use history items given a specific project and version.
     * @param project The project item
     * @param versionItem The Terms of Use Version item
     * @return set of project terms of use history items associated with a given project and version
     */
    Collection findByProjectAndVersion(ProjectItem project, TermsOfUseVersionItem versionItem);

}
