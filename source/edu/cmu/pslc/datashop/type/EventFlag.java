/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.type;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * Type safe implementation of the EventFlag enumeration type. This
 * specific implementation saves to the database using an integer value (0, 1, 2,...)
 * which matches the ordinal integer value.  That ordinal value is used to avoid having
 * to do costly string comparison instead a faster number comparison.
 *
 * Ordinal Value
 * 0: start
 * 1: resume
 * 2: skip
 * 3: done
 * 4: quit
 * 5: reset
 * 6: unknown
 * 7: reading
 * 8: timeout
 * 9: switch to
 * 10: switch from
 * 11: graduated
 * 12: promoted
 *
 * @author Benjamin K. Billings
 * @version $Revision: 7504 $
 * <BR>Last modified by: $Author: alida $
 * <BR>Last modified on: $Date: 2012-03-12 15:19:07 -0400 (Mon, 12 Mar 2012) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class EventFlag implements Serializable, Comparable {

    /** Debug logging. */
    private static final Logger LOGGER = Logger.getLogger(EventFlag.class.getName());

    /** The string holding the eventFlag type */
    private final String eventFlag;

    /** Collection of all valid strings to flag as a "start problem" */
    private static final Collection <String> ALLOWED_START_STRINGS = new HashSet <String>();
    static {
        ALLOWED_START_STRINGS.add("START_PROBLEM");
        ALLOWED_START_STRINGS.add("AGG_START_PROBLEM");
        ALLOWED_START_STRINGS.add("FFI_START_PROBLEM");
        ALLOWED_START_STRINGS.add("LOAD_PROBLEM");
        ALLOWED_START_STRINGS.add("PROBLEM_START");
        ALLOWED_START_STRINGS.add("START");
        ALLOWED_START_STRINGS.add("QUESTION");
        ALLOWED_START_STRINGS.add("LOAD_TUTOR");
        ALLOWED_START_STRINGS.add("LOAD_AUDIO");
        ALLOWED_START_STRINGS.add("LOAD_VIDEO");
        ALLOWED_START_STRINGS.add("LOAD_MEDIA");
        ALLOWED_START_STRINGS.add("START_TUTOR");
        ALLOWED_START_STRINGS.add("START_AUDIO");
        ALLOWED_START_STRINGS.add("START_VIDEO");
        ALLOWED_START_STRINGS.add("START_MEDIA");
    }

    /** Collection of all valid strings to flag as a "skip problem" */
    private static final Collection <String> ALLOWED_SKIP_STRINGS = new HashSet <String>();
    static {
        ALLOWED_SKIP_STRINGS.add("SKIP_PROBLEM");
        ALLOWED_SKIP_STRINGS.add("SKIP_TUTOR");
        ALLOWED_SKIP_STRINGS.add("SKIP");
    }

    /** Collection of all valid strings to flag as a "resume problem" */
    private static final Collection <String> ALLOWED_RESUME_STRINGS = new HashSet <String>();
    static {
        ALLOWED_RESUME_STRINGS.add("RESUME");
        ALLOWED_RESUME_STRINGS.add("CONTINUE");
        ALLOWED_RESUME_STRINGS.add("RESUME_PROBLEM");
        ALLOWED_RESUME_STRINGS.add("RESUME_TUTOR");
        ALLOWED_RESUME_STRINGS.add("CONTINUE_PROBLEM");
    }

    /** Collection of all valid strings to flag as a "done problem" */
    private static final Collection <String> ALLOWED_DONE_STRINGS = new HashSet <String>();
    static {
        ALLOWED_DONE_STRINGS.add("DONE_PROBLEM");
        ALLOWED_DONE_STRINGS.add("DONE");
        ALLOWED_DONE_STRINGS.add("FINISHED");
    }

    /** Collection of all valid strings to flag as a "quit problem" */
    private static final Collection <String> ALLOWED_QUIT_STRINGS = new HashSet <String>();
    static {
        ALLOWED_QUIT_STRINGS.add("QUIT_PROBLEM");
        ALLOWED_QUIT_STRINGS.add("QUIT");
    }

    /** Collection of all valid strings to flag as a "reset problem" */
    private static final Collection <String> ALLOWED_RESET_STRINGS = new HashSet <String>();
    static {
        ALLOWED_RESET_STRINGS.add("RESET_PROBLEM");
        ALLOWED_RESET_STRINGS.add("RESET");
    }

    /** Collection of all valid strings to flag as a "reading problem" */
    private static final Collection <String> ALLOWED_READING_STRINGS = new HashSet <String>();
    static {
        ALLOWED_READING_STRINGS.add("READING");
    }

    /** Collection of all valid strings to flag as an "unknown" */
    private static final Collection <String> ALLOWED_UNKNOWN_STRINGS = new HashSet <String>();
    static {
        ALLOWED_UNKNOWN_STRINGS.add("UNKNOWN");
        ALLOWED_UNKNOWN_STRINGS.add("VLAB_PROBLEM");
    }

    /** Collection of all valid strings to flag as a "timeout" */
    private static final Collection <String> ALLOWED_TIMEOUT_STRINGS = new HashSet <String>();
    static {
        ALLOWED_TIMEOUT_STRINGS.add("TIMEOUT");
    }

    /** Collection of all valid strings to flag as an "switch to" */
    private static final Collection <String> ALLOWED_SWITCH_TO_STRINGS = new HashSet <String>();
    static {
        ALLOWED_SWITCH_TO_STRINGS.add("SWITCH TO");
        ALLOWED_SWITCH_TO_STRINGS.add("SWITCH_TO");
        ALLOWED_SWITCH_TO_STRINGS.add("SWITCHTO");
    }

    /** Collection of all valid strings to flag as an "switch from" */
    private static final Collection <String> ALLOWED_SWITCH_FROM_STRINGS = new HashSet <String>();
    static {
        ALLOWED_SWITCH_FROM_STRINGS.add("SWITCH FROM");
        ALLOWED_SWITCH_FROM_STRINGS.add("SWITCH_FROM");
        ALLOWED_SWITCH_FROM_STRINGS.add("SWITCHFROM");
    }

    /** Collection of all valid strings to flag as a "graduated" */
    private static final Collection <String> ALLOWED_GRADUATED_STRINGS = new HashSet <String>();
    static {
        ALLOWED_GRADUATED_STRINGS.add("GRADUATED");
    }

    /** Collection of all valid strings to flag as a "promoted" */
    private static final Collection <String> ALLOWED_PROMOTED_STRINGS = new HashSet <String>();
    static {
        ALLOWED_PROMOTED_STRINGS.add("PROMOTED");
    }

    /**
     * Private constructor prevents types that are not allowed.
     * @param correctFlag the type of correctFlag to create.
     */
    private EventFlag(String correctFlag) {
        this.eventFlag = correctFlag;
    }

    /** Start event string value */
    private static final String START_EVENT_STRING = "start";
    /** Resume event string value */
    private static final String RESUME_EVENT_STRING = "resume";
    /** Skip event string value */
    private static final String SKIP_EVENT_STRING = "skip";
    /** Done event string value */
    private static final String DONE_EVENT_STRING = "done";
    /** Quit event string value */
    private static final String QUIT_EVENT_STRING = "quit";
    /** Reset event string value */
    private static final String RESET_EVENT_STRING = "reset";
    /** Unknown event string value */
    private static final String UNKNOWN_EVENT_STRING = "unknown";
    /** Reading event string value */
    private static final String READING_EVENT_STRING = "reading";
    /** Timeout event string value. */
    private static final String TIMEOUT_EVENT_STRING = "timeout";
    /** Switch To event string value. */
    private static final String SWITCH_TO_EVENT_STRING = "switch to";
    /** Switch From event string value. */
    private static final String SWITCH_FROM_EVENT_STRING = "switch from";
    /** Graduated event string value. */
    private static final String GRADUATED_EVENT_STRING = "graduated";
    /** Promoted event string value. */
    private static final String PROMOTED_EVENT_STRING = "promoted";


    //NOTE: Order is important when adding new ones. add new values to the bottom
    //with a specific ordinal number and be sure to add to VALUES.
    /** Create an eventFlag of type "start". */
    public static final EventFlag START = new EventFlag(START_EVENT_STRING);
    /** Create an eventFlag of type "resume". */
    public static final EventFlag RESUME = new EventFlag(RESUME_EVENT_STRING);
    /** Create an eventFlag of type "skip". */
    public static final EventFlag SKIP = new EventFlag(SKIP_EVENT_STRING);
    /** Create an eventFlag of type "done". */
    public static final EventFlag DONE = new EventFlag(DONE_EVENT_STRING);
    /** Create an eventFlag of type "quit". */
    public static final EventFlag QUIT = new EventFlag(QUIT_EVENT_STRING);
    /** Create an eventFlag of type "reset". */
    public static final EventFlag RESET = new EventFlag(RESET_EVENT_STRING);
    /** Create an eventFlag of type "unknown". */
    public static final EventFlag UNKNOWN = new EventFlag(UNKNOWN_EVENT_STRING);
    /** Create an eventFlag of type "reading". */
    public static final EventFlag READING = new EventFlag(READING_EVENT_STRING);
    /** Create an eventFlag of type "timeout". */
    public static final EventFlag TIMEOUT = new EventFlag(TIMEOUT_EVENT_STRING);
    /** Create an eventFlag of type "switch to". */
    public static final EventFlag SWITCH_TO = new EventFlag(SWITCH_TO_EVENT_STRING);
    /** Create an eventFlag of type "switch from". */
    public static final EventFlag SWITCH_FROM = new EventFlag(SWITCH_FROM_EVENT_STRING);
    /** Create an eventFlag of type "graduated". */
    public static final EventFlag GRADUATED = new EventFlag(GRADUATED_EVENT_STRING);
    /** Create an eventFlag of type "promoted". */
    public static final EventFlag PROMOTED = new EventFlag(PROMOTED_EVENT_STRING);

    //The following are required for serialization and useful for speed */
    /** The initial next ordinal value. */
    private static int nextOrdinal = 0;
    /** Ordinal number of the correctFlag for fast comparisons. */
    private final int ordinal = nextOrdinal++;
    /** Object array of all allowed values in order by ordinal value */
    private static final EventFlag[] VALUES =
        {START, RESUME, SKIP, DONE, QUIT, RESET, UNKNOWN, READING,
        TIMEOUT, SWITCH_TO, SWITCH_FROM, GRADUATED, PROMOTED};

    /**
     * Save version of the toString method.
     * @return a String representation of the class.
     */
    public String toString() {
        return String.valueOf(eventFlag);
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
       if (obj instanceof EventFlag) {
           EventFlag otherItem = (EventFlag)obj;
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
       EventFlag otherItem = (EventFlag)obj;
       return this.ordinal - otherItem.ordinal;
   }

   /**
    * Override of the Serializable to force proper de-serialization.
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
   public static EventFlag getInstance(int index) {
       if (index < 0 || index > VALUES.length) {
           throw new IllegalArgumentException("retrieval index out of bounds. value : "
                   + index);
       }
       return VALUES[index];
   }

   /** The string that the aggregator appends to the PROBLEM START event flag
    *  if it generates the Problem View. */
   private static final String PV_GEN_SUFFIX = "_PV_GEN";
   /** The length of the PV_GEN_SUFFIX. */
   private static final int PV_GEN_SUFFIX_LEN = PV_GEN_SUFFIX.length();

   /**
    * Get instance given a string value of the EventFlag.
    * This is the slower of the two get Instance methods.
    * Will return EventFlag.UNKNOWN if none found.
    *
    * @param eventFlagString String value of a EventFlag.
    * @return an instance of an CorrectFlag, returns UNKNOWN if none found.
    */
   public static EventFlag getInstance(String eventFlagString) {
       if (eventFlagString == null) {
           throw new NullPointerException("Event Flag cannot be null");
       }
       if (eventFlagString.endsWith(PV_GEN_SUFFIX)) {
           int evsLen = eventFlagString.length();
           eventFlagString = eventFlagString.substring(0, evsLen - PV_GEN_SUFFIX_LEN);
       }

       eventFlagString = eventFlagString.toUpperCase();
       if (ALLOWED_START_STRINGS.contains(eventFlagString)) {
           return EventFlag.START;
       } else if (ALLOWED_DONE_STRINGS.contains(eventFlagString)) {
           return EventFlag.DONE;
       } else if (ALLOWED_QUIT_STRINGS.contains(eventFlagString)) {
           return EventFlag.QUIT;
       } else if (ALLOWED_SKIP_STRINGS.contains(eventFlagString)) {
           return EventFlag.SKIP;
       } else if (ALLOWED_RESUME_STRINGS.contains(eventFlagString)) {
           return EventFlag.RESUME;
       } else if (ALLOWED_RESET_STRINGS.contains(eventFlagString)) {
           return EventFlag.RESET;
       } else if (ALLOWED_UNKNOWN_STRINGS.contains(eventFlagString)) {
           return EventFlag.UNKNOWN;
       } else if (ALLOWED_READING_STRINGS.contains(eventFlagString)) {
           return EventFlag.READING;
       } else if (ALLOWED_TIMEOUT_STRINGS.contains(eventFlagString)) {
           return EventFlag.TIMEOUT;
       } else if (ALLOWED_SWITCH_TO_STRINGS.contains(eventFlagString)) {
           return EventFlag.SWITCH_TO;
       } else if (ALLOWED_SWITCH_FROM_STRINGS.contains(eventFlagString)) {
           return EventFlag.SWITCH_FROM;
       } else if (ALLOWED_GRADUATED_STRINGS.contains(eventFlagString)) {
           return EventFlag.GRADUATED;
       } else if (ALLOWED_PROMOTED_STRINGS.contains(eventFlagString)) {
           return EventFlag.PROMOTED;
       } else {
           LOGGER.warn("Event Flag '" + eventFlagString
                   + "' unknown.  Returning EventFlag.UNKNOWN.");
           return EventFlag.UNKNOWN;
       }
   } // end getInstance()
} // end EventFlag.java
