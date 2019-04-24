/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2014
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.cmu.pslc.datashop.dao.PcProblemDao;

import edu.cmu.pslc.datashop.item.PcConversionItem;
import edu.cmu.pslc.datashop.item.PcProblemItem;

/**
 * Hibernate and Spring implementation of the PcProblemDao.
 *
 * @author
 * @version $Revision: 11467 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2014-08-13 09:57:37 -0400 (Wed, 13 Aug 2014) $
 * <!-- $KeyWordsOff: $ -->
 */
public class PcProblemDaoHibernate extends AbstractDaoHibernate implements PcProblemDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a PcProblemItem by id.
     * @param id The id of PcProblem.
     * @return the matching PcProblemItem or null if none found
     */
    public PcProblemItem get(Long id) {
        return (PcProblemItem)get(PcProblemItem.class, id);
    }

    /**
     * Standard find for an PcProblemItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired PcProblemItem.
     * @return the matching PcProblemItem.
     */
    public PcProblemItem find(Long id) {
        return (PcProblemItem)find(PcProblemItem.class, id);
    }

    /**
     * Standard "find all" for PcProblem items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(PcProblemItem.class);
    }

    //
    // Non-standard methods begin.
    //

    /**
     * Find PcProblemItem given problem name and PcConversionItem.
     * @param problemName the problem name
     * @param pcConversion the PcConversionItem
     * @return matching PcProblemItem
     */
    public PcProblemItem findByNameAndConversion(String problemName,
                                                 PcConversionItem pcConversion) {
        Object[] params = {problemName, pcConversion};

        List<PcProblemItem> pcpList = getHibernateTemplate().
            find("FROM PcProblemItem p WHERE p.problemName = ? AND p.pcConversion = ?", params);

        if ((pcpList != null) && (pcpList.size() == 1)) {
            return pcpList.get(0);
        } else {
            return null;
        }
    }

    /** Constant SQL query for problems by content version. */
    private static final String PROBLEMS_BY_CONTENT_QUERY =
        "SELECT pc_problem_id AS id, problem_name AS problemName, html_file_id AS fileId"
        + " FROM pc_problem"
        + " WHERE pc_conversion_id = :pcConversionId";

    /**
     * Get a list of PcProblemItems by Problem Content Conversion.
     * @param pcConversion the PcConversionItem to match on
     * @return list of matching PcProblemItems
     */
    public List<PcProblemItem> findProblemsByConversion(PcConversionItem pcConversion) {
        return getHibernateTemplate().
            find("FROM PcProblemItem p WHERE p.pcConversion = ?", pcConversion);
    }
}
