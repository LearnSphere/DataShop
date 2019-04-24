/*
* Carnegie Mellon University, Human Computer Interaction Institute
* Copyright 2005
* All Rights Reserved
*/
package edu.cmu.pslc.datashop.util;


/**
 * Constants that apply for the entire DataShop..
 *
 * @author Benjamin Billings
 * @version $Revision: 5636 $
 * <BR>Last modified by: $Author: kcunning $
 * <BR>Last modified on: $Date: 2009-08-05 11:46:48 -0400 (Wed, 05 Aug 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class UtilConstants {

    /** Private Constructor since this is a utility class.*/
    private UtilConstants() { };

    /** Prime number (17) used for the hash code seed. */
    public static final int HASH_INITIAL = 17;

    /** Prime number (7) used for when an object is not null, but the identifier we were using
     * to generate it's hash prime happens to be null. */
    public static final int NULL_HASH_PRIME = 7;

    /** Prime number (37) used for generating hash codes. */
    public static final int HASH_PRIME = 37;

    /** Flag for when the powers that be change their mind about whether
     * input is used in identifying a step or not. */
    public static final boolean USE_INPUT_IN_STEP_IDENTIFICATION = false;

    /** Magic '1000'. */
    public static final int MAGIC_1000 = 1000;

} // end class UtilConstants
