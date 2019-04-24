/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dao;

import java.util.List;

import edu.cmu.pslc.datashop.dto.ProfilerOptions;

/**
 * This DAO gets the information for a performance profiler report.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 3436 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2006-11-21 14:32:20 -0500 (Tue, 21 Nov 2006) $
 * <!-- $KeyWordsOff: $ -->
 */
public interface PerformanceProfilerDao {

    /**
     * Get an ErrorRate performance profiler info.
     * @param options the ProfilerOptions.
     * @return a List with the index the key of the Y axis ID, and the value a PerformanceProfile.
     */
    List getPerformanceProfiler(ProfilerOptions options);
}
