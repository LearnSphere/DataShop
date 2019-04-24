/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao.hibernate;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.InterpretationDao;
import edu.cmu.pslc.datashop.item.CognitiveStepItem;
import edu.cmu.pslc.datashop.item.InterpretationItem;

/**
 * Hibernate and Spring implementation of the InterpretationDao.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4521 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2008-02-29 21:18:58 -0500 (Fri, 29 Feb 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public class InterpretationDaoHibernate extends AbstractDaoHibernate implements InterpretationDao {

    /** Logger for this class */
    private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Standard get for a InterpretationItem by id.
     * @param id The id of the user.
     * @return the matching InterpretationItem or null if none found
     */
    public InterpretationItem get(Long id) {
        return (InterpretationItem)get(InterpretationItem.class, id);
    }

    /**
     * Standard "find all" for user items.
     * @return a List of objects
     */
    public List findAll() {
        return findAll(InterpretationItem.class);
    }

    /**
     * Standard find for an InterpretationItem by id.
     * Only the id of the item will be filled in.
     * @param id the id of the desired InterpretationItem.
     * @return the matching InterpretationItem.
     */
    public InterpretationItem find(Long id) {
        return (InterpretationItem)find(InterpretationItem.class, id);
    }

    /**
     * Return one interpretation item (if it is found) for the
     * pair of step sequences.
     * TODO add the position field to the query
     * @param correctStepSeq the list of steps in the correct sequence
     * @param incorrectStepSeq the list of steps in the incorrect sequence
     * @return the existing interpretation or null if not found
     */
    public InterpretationItem find(List correctStepSeq, List incorrectStepSeq) {
        InterpretationItem interpretationItem = null;

        if (correctStepSeq.size() == 0 || incorrectStepSeq.size() == 0) {
            return null;
        }

        int numSteps = correctStepSeq.size() + incorrectStepSeq.size();

        StringBuffer query = new StringBuffer();

        query.append("select interpretation");
        query.append(" from InterpretationItem interpretation");
        query.append(" join interpretation.cogStepSequences sequence");

        // correct step sequence part of query
        query.append(" where (sequence.cognitiveStep in (");
        for (Iterator iter = correctStepSeq.iterator(); iter.hasNext();) {
            CognitiveStepItem cogStep = (CognitiveStepItem)iter.next();
            query.append(cogStep.getId() + ",");
        }
        query.deleteCharAt(query.length() - 1);
        query.append(")");
        query.append(" and sequence.correctFlag = " + true + ")");

        // incorrect step sequence part of query
        query.append(" or (sequence.cognitiveStep in (");
        for (Iterator iter = incorrectStepSeq.iterator(); iter.hasNext();) {
            CognitiveStepItem cogStep = (CognitiveStepItem)iter.next();
            query.append(cogStep.getId() + ",");
        }
        query.deleteCharAt(query.length() - 1);
        query.append(")");
        query.append(" and sequence.correctFlag = " + false + ")");

        //TODO there's something still not right about this query, need something like this:
        query.append(" and interpretation.cogStepSequences.size = " + numSteps);

        query.append(" group by interpretation.id");
        query.append(" having count(*) = " + numSteps);

        if (logger.isDebugEnabled()) {
            logger.debug("find(correctSeq, incorrectSeq): query: " + query.toString());
        }

        List interpList = getHibernateTemplate().find(query.toString());

        if (interpList.size() >= 1) {
            interpretationItem = (InterpretationItem)interpList.get(0);
            if (interpList.size() > 1) {
                logger.warn(interpList.size() + " interpretations found for given step sequences"
                        + " which are " + interpList);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Interpretation found for correct sequence: "
                    + correctStepSeq + " and incorrect sequence: " + incorrectStepSeq
                    + ": " + interpretationItem);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No interpretation found for correct sequence: "
                    + correctStepSeq + " and incorrect sequence: " + incorrectStepSeq);
            }
        }

        return interpretationItem;
    }

    /**
     * Return one interpretation item (if it is found) for
     * one step sequence.
     * TODO add the position field to the query
     * @param stepSeq the list of steps in the sequence
     * @param correctFlag indicates whether sequence is correct or not
     * @return the existing interpretation or null if not found
     */
    public InterpretationItem find(List stepSeq, Boolean correctFlag) {
        InterpretationItem interpretationItem = null;

        int numSteps = stepSeq.size();
        if (numSteps == 0) {
            return null;
        }

        StringBuffer query = new StringBuffer();

        query.append("select interpretation");
        query.append(" from InterpretationItem interpretation");
        query.append(" join interpretation.cogStepSequences sequence");

        // correct step sequence part of query
        query.append(" where (sequence.cognitiveStep in (");
        for (Iterator iter = stepSeq.iterator(); iter.hasNext();) {
            CognitiveStepItem cogStep = (CognitiveStepItem)iter.next();

            query.append(cogStep.getId());
            if (iter.hasNext()) { query.append(", "); }
        }

        query.append(")");
        query.append(" and sequence.correctFlag = " + correctFlag.booleanValue() + ")");

        query.append(" and interpretation.cogStepSequences.size = " + numSteps);

        query.append(" group by interpretation.id");
        query.append(" having count(*) = " + numSteps);

        if (logger.isDebugEnabled()) {
            logger.debug("find(cogSteps, correctFlag): query: " + query.toString());
        }

        List interpList = getHibernateTemplate().find(query.toString());

        if (interpList.size() >= 1) {
            interpretationItem = (InterpretationItem)interpList.get(0);
            if (interpList.size() > 1) {
                logger.warn(interpList.size() + " interpretations found for step sequence.");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Interpretation found for sequence: "
                    + stepSeq + " with correct flag: " + correctFlag + ": " + interpretationItem);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No interpretation found for sequence: "
                    + stepSeq + " with correct flag: " + correctFlag);
            }
        }

        return interpretationItem;
    }
}
