/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2008
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

/**
 * Place to put math functions copied from LFA code.
 * This is used by StepRollupDaoHibernate and LFA Values report.
 *
 * @author Alida Skogsholm
 * @version $Revision: 4572 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-03-20 14:15:43 -0400 (Thu, 20 Mar 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class LfaMath {

    /** Private constructor as this is a utility class. */
    private LfaMath() { }

    /**
     * Copied from LFA's MyMath utility class.
     * Uses the Math package for the exponent method.
     * @param x the value to inverse
     * @return the inverse thing of the given
     */
    public static double inverseLogit(double x) {
        double numerator = Math.exp(x);
        double denominator = 1 + Math.exp(x);
        if (denominator == Double.POSITIVE_INFINITY
                || denominator == Double.NEGATIVE_INFINITY) {
            return 1d;
        } else {
            return (numerator / denominator);
        }
    }

}
