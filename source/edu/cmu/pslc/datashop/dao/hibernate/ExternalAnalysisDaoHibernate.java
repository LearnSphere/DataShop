/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2012
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.List;

import edu.cmu.pslc.datashop.dao.ExternalAnalysisDao;
import edu.cmu.pslc.datashop.item.ExternalAnalysisItem;

/**
 * Hibernate and Spring implementation of the ExternalAnalysisDao.
 *
 * @author Cindy Tipper
 * @version $Revision: 7569 $
 * <BR>Last modified by: $Author: ctipper $
 * <BR>Last modified on: $Date: 2012-03-30 12:40:09 -0400 (Fri, 30 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ExternalAnalysisDaoHibernate extends AbstractDaoHibernate<ExternalAnalysisItem>
implements ExternalAnalysisDao {

    /**
     * Standard get for a ExternalAnalysisItem by id.
     * @param id The id of the user.
     * @return the matching ExternalAnalysisItem or null if none found
     */
    public ExternalAnalysisItem get(Integer id) {
        return (ExternalAnalysisItem)get(ExternalAnalysisItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List<ExternalAnalysisItem> findAll() {
        return findAll(ExternalAnalysisItem.class);
    }

    /**
     * Standard find for an ExternalAnalysisItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired ExternalAnalysisItem.
     * @return the matching ExternalAnalysisItem.
     */
    public ExternalAnalysisItem find(Integer id) {
        return (ExternalAnalysisItem)find(ExternalAnalysisItem.class, id);
    }
}
