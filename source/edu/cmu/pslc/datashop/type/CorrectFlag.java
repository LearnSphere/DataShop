/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.type;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Type safe implementation of the CorrectFlag enumeration type. This
 * specific implementation saves to the database using an integer value (0, 1, 2,...)
 * which matches the ordinal integer value.  That ordinal value is used to avoid having
 * to do costly string comparison instead a faster number comparison.
 *
 * @author Benjamin K. Billings
 * @version $Revision: 4538 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2008-03-06 14:38:29 -0500 (Thu, 06 Mar 2008) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class CorrectFlag implements Serializable, Comparable {

    /** The string holding the correctFlag type */
    private final String correctFlag;

    /**
     * Private constructor prevents types that are not allowed.
     * @param correctFlag the type of correctFlag to create.
     */
    private CorrectFlag(String correctFlag) {
        this.correctFlag = correctFlag;
    }

    /** Allowed correctFlag string value */
    private static final String INCORRECT_STRING = "incorrect";
    /** Allowed correctFlag string value */
    private static final String HINT_STRING = "hint";
    /** Allowed correctFlag string value */
    private static final String CORRECT_STRING = "correct";
    /** Allowed correctFlag string value */
    private static final String UNKNOWN_STRING = "unknown";
    /** Allowed correctFlag string value */
    private static final String UNTUTORED_STRING = "untutored";

    //NOTE: Order is important when adding new ones. add new values to the bottom
    //and be sure to add to VALUES.
    /** Create an correctFlag of type "incorrect". */
    public static final CorrectFlag INCORRECT = new CorrectFlag(INCORRECT_STRING);
    /** Create an correctFlag of type "hint". */
    public static final CorrectFlag HINT = new CorrectFlag(HINT_STRING);
    /** Create an correctFlag of type "correct". */
    public static final CorrectFlag CORRECT = new CorrectFlag(CORRECT_STRING);
    /** Create an correctFlag of type "unknown". */
    public static final CorrectFlag UNKNOWN = new CorrectFlag(UNKNOWN_STRING);
    /** Create an correctFlag of type "untutored". */
    public static final CorrectFlag UNTUTORED = new CorrectFlag(UNTUTORED_STRING);

    //The following are required for serialization and useful for speed */
    /** The initial next ordinal value. */
    private static int nextOrdinal = 0;
    /** Ordinal number of the correctFlag for fast comparisons. */
    private final int ordinal = nextOrdinal++;
    /** Object array of all allowed values in order by ordinal value */
    private static final CorrectFlag[] VALUES = {INCORRECT, HINT, CORRECT, UNKNOWN, UNTUTORED};

    /**
     * Save version of the toString method.
     * @return a String representation of the class.
     */
    public String toString() {
        return String.valueOf(correctFlag);
    }

    /**
     * Returns the hash code for this item.
     * For this particular class just returns the ordinal number since
     * since there is only a limited set of values and each of them is assigned
     * a unique ordinal number.
     * @return the hash code for this item
     */
    public int hashCode() {
        return ordinal;
    }

    /**
     * Equals function for this class.
     * @param obj Object of any type, should be an Object for equality check
     * @return boolean true if the items are equal, false if not
     */
   public boolean equals(Object obj) {
       if (this == obj) {
           return true;
       }
       if (obj instanceof CorrectFlag) {
           CorrectFlag otherItem = (CorrectFlag)obj;
           if (this.ordinal != otherItem.ordinal) {
                return false;
           }
           return true;
       }
       return false;
   }

   /**
    * Compares two objects using each attribute of this class.
    * @param obj the object to compare this to.
    * @return the value 0 if equal; a value less than 0 if it is less than;
    * a value greater than 0 if it is greater than
   */
   public int compareTo(Object obj) {
       CorrectFlag otherItem = (CorrectFlag)obj;
       return this.ordinal - otherItem.ordinal;
   }

   /**
    * Override of the java.io.Serializable to force proper deserialization.
    * @return Object return the resolved object.
    * @throws ObjectStreamException ObjectStreamException
    */
   Object readResolve() throws ObjectStreamException {
       if (ordinal < 0 || ordinal > VALUES.length) {
           throw new IllegalArgumentException("readResolve corrupted, retrieval index"
                + " is out of bounds. value : " + ordinal);
       }
       return VALUES[ordinal];
   }

   /**
    * Get instance given an ordinal value/index.<br />
    * This is primarily used by hibernate when the index is retrieved
    * from the database to get the actual instance of this class.
    *
    * @param index the index of the value to retrieve.
    * @return an instance of an CorrectFlag
    */
   public static CorrectFlag getInstance(int index) {
       if (index < 0 || index > VALUES.length) {
           throw new IllegalArgumentException("retrieval index out of bounds. value : "
                   + index);
       }
       return VALUES[index];
   }

   /**
    * Get instance given a string value of the correctFlag.
    * This is the slower of the two get Instance methods.  Will return null if none found.
    *
    * @param correctFlagString String value of a CorrectFlag.
    * @return an instance of an CorrectFlag, null if none matching param.
    */
   public static CorrectFlag getInstance(String correctFlagString) {
       if (CORRECT_STRING.equals(correctFlagString)) {
           return CorrectFlag.CORRECT;
       } else if (INCORRECT_STRING.equals(correctFlagString)) {
           return CorrectFlag.INCORRECT;
       } else if (HINT_STRING.equals(correctFlagString)) {
           return CorrectFlag.HINT;
       } else if (UNKNOWN_STRING.equals(correctFlagString)) {
           return CorrectFlag.UNKNOWN;
       } else if (UNTUTORED_STRING.equals(correctFlagString)) {
           return CorrectFlag.UNTUTORED;
       } else {
           return null;
       }
   }
}
