/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.item;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.util.CollectionUtils.KeyValue;

import static edu.cmu.pslc.datashop.util.UtilConstants.HASH_INITIAL;
import static edu.cmu.pslc.datashop.util.UtilConstants.HASH_PRIME;
import static edu.cmu.pslc.datashop.util.CollectionUtils.keyValues;
import static edu.cmu.pslc.datashop.util.StringUtils.join;

/**
 * Abstract class for all items to ensure that the standard methods
 * such as equals, hashCode and compareTo are correct.
 *
 * @author Alida Skogsholm
 * @version $Revision: 10344 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-11-18 14:26:41 -0500 (Mon, 18 Nov 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public abstract class Item {

    /** Debug logging. */
    //private Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Returns the id.
     * @return the id
     */
    public abstract Comparable getId();

    /**
     * Builds the display string with the id taking null into account.
     * @param displayName how the name of the object should be displayed.
     * @param item the item to display
     * @return a StringBuffer that is the display.
     */
    protected StringBuffer objectToStringFK(String displayName, Item item) {
        StringBuffer buffer = new StringBuffer();
        if (item != null) {
            if (item.getId() != null) {
                return buffer.append(displayName).append("='").append(item.getId()).append("' ");
            } else {
                return buffer.append(displayName).append("='null id' ");
            }
        } else {
            return buffer.append(displayName).append("='null' ");
        }
    }

    /**
     * Builds the display string taking into account whether or not
     * this object is an item. If it is an item it will check to make
     * sure the object is not null and then displays its id.
     * @param displayName how the name of the object should be displayed.
     * @param obj the object to display
     * @return a StringBuffer that is the display.
     */
    protected StringBuffer objectToString(String displayName, Object obj) {
        StringBuffer buffer = new StringBuffer();
        return buffer.append(displayName).append("='").append(obj).append("' ");
    }

    /**
     * Determines whether another FK object is equal to another FK object,
     * taking null into account.
     * @param one the first object to compare to
     * @param two the other object to compare to
     * @return true if the items are equal, false otherwise
     */
   public static boolean objectEqualsFK(Object one, Object two) {
        if ((one == null) && (two == null)) {
            return true;
        } else if (one == null) {
            return false;
        } else if (two == null) {
            return false;
        } else {
            Item oneItem = (Item)one;
            Item twoItem = (Item)two;

            return objectEquals(oneItem.getId(), twoItem.getId());
        }
    }

    /**
     * Performs an equals() check on 2 strings ignoring case.
     * @param one - the first string for .equals()
     * @param two - the second string for .equals()
     * @return true if the strings are equal, false if not
     */
    protected boolean stringEqualsIgnoreCase(String one, String two) {
        if (one != null) {
            if (!one.equalsIgnoreCase(two)) {
                return false;
            }
        } else if (two != null) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether another object is equal to another,
     * taking null into account.
     * @param one the first object to compare to
     * @param two the other object to compare to
     * @return true if the items are equal, false otherwise
     */
    public static boolean objectEquals(Object one, Object two) {
        if (one != null) {
            if (!one.equals(two)) {
                return false;
            }
        } else if (two != null) {
            return false;
        }
        return true;
    }

    /**
     * Returns the hash code for a foreign key item. null safe.
     * <br><br>
     * This function works by returning 0 if the object is null.
     * For the case where the ID has been assigned, the returned value is the hash code
     * of the key object
     *
     * @param item the object to get the hash code for
     * @return the hash code for this item
     */
    public int objectHashCodeFK(Item item) {
        int hash = 0;
        if (item != null && item.getId() != null) {
            hash = item.getId().hashCode();
        }
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Returns the hash code for this item. null safe.
     * <br><br>
     * If the object is null it will return a 0.
     * For all other cases the function will return the
     * the hash code of the object.
     *
     * @param object the object to get the hash code for
     * @return the hash code for this item
     */
    public int objectHashCode(Object object) {
        int hash = 0;
        if (object != null) {
            hash = object.hashCode();
        }
        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Used to compare the IDs of two items.
     * @param one the first item to compare to
     * @param two the other item to compare to
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    protected int objectCompareToFK(Item one, Item two) {
        //first check that both items are not null
        if ((one != null) && (two != null)) {
            //check that the id fields are not null
            if ((one.getId() != null) && (two.getId() != null)) {
                return one.getId().compareTo(two.getId());
            } else if (one.getId() != null) {
                return 1;
            } else if (two.getId() != null) {
                return -1;
            }
        } else if (one != null) {
            return 1;
        } else if (two != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Used to compare two strings, taking null into account.
     * @param one the first string to compare to
     * @param two the other string to compare to
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    protected int stringCompareToIgnoreCase(String one, String two) {
        if ((one != null) && (two != null)) {
            return one.compareToIgnoreCase(two);
        } else if (one != null) {
            return 1;
        } else if (two != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Used to compare one attribute of a class, taking null into account.
     * @param one the first object to compare to
     * @param two the other object to compare to
     * @param <T> the type
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    protected <T> int objectCompareTo(Comparable<T> one, Comparable<T> two) {
        if ((one != null) && (two != null)) {
            return one.compareTo((T)two);
        } else if (one != null) {
            return 1;
        } else if (two != null) {
            return -1;
        }
        return 0;
    }

    /**
     * Used to compare two booleans for java 1.4.
     * @param one the first boolean to compare to
     * @param two the other boolean to compare to
     * @return the value 0 if equal; a value less than 0 if it is less than;
     * a value greater than 0 if it is greater than
     */
    protected int objectCompareToBool(Boolean one, Boolean two) {
        if ((one != null) && (two != null)) {
            if (!one.equals(two)) {
                if (two.booleanValue()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        } else if (one != null) {
            return 1;
        } else if (two != null) {
            return -1;
        }
        return 0;
    }

    /**
     * We often want to just show the name and id for an item.
     * @param name pass in the name because don't know if it is getDatasetName(),
     * getSampleName(), etc.
     * @return the formatted name and id of this item
     */
    public String getNameAndId(String name) {
        return name + " (" + getId() + ")";
    }

    /** The length of the TINYTEXT data type. */
    public static final int TINY_TEXT_LENGTH = 255;

    /**
     * Truncate the value if necessary.
     * @param logger for debug logging
     * @param column the name of the column
     * @param value the string to check length and truncate if necessary
     * @return the original string or a truncated version of it
     */
    protected String truncateTinyText(Logger logger, String column, String value) {
        if (value == null) { return value; }
        if (value.length() > TINY_TEXT_LENGTH) {
            logger.warn("Truncating " + column + " to " + TINY_TEXT_LENGTH + " characters "
                    + " for value[" + value + "]");
            value = value.substring(0, TINY_TEXT_LENGTH);
        }
        return value;
    }

    /**
     * Truncate the value using the user supplied max length.
     * @param logger for debug logging
     * @param column the name of the column
     * @param value the string to check length and truncate if necessary
     * @param length the allowed maximum length of the string
     * @return the original string or a truncated version of it
     */
    protected String truncateValue(Logger logger, String column, String value, Integer length) {
        if (value == null) { return value; }
        if (value.length() > length) {
            logger.debug("Truncating " + column + " to " + length + " characters "
                    + " for value[" + value + "]");
            value = value.substring(0, length);
        }
        return value;
    }

    /**
     * Calculate the hash code of this item based on the hash codes of the fields that
     * uniquely identify it. Call this method from subclass hashCode method
     * and pass the fields that uniquely identify this item as parameters.
     * @param os fields that uniquely identify this item
     * @return hash code value calculated from the hash codes of the parameters
     */
    protected int hashPrime(Object... os) {
        long hash = HASH_INITIAL;

        for (Object o : os) {
            int ohash;
            if (o instanceof Item) {
                ohash = objectHashCodeFK((Item)o);
            } else {
                ohash = objectHashCode(o);
            }
            hash = hash * HASH_PRIME + ohash;
        }

        return (int)(hash % Integer.MAX_VALUE);
    }

    /**
     * Default toString implementation.
     * @param labelsAndValues labels alternating with the corresponding objects we want to
     * display as part of the string representation
     * @return the toString representation of this object
     */
    public String toString(Object... labelsAndValues) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(" [");

        final List<KeyValue<String, Object>> kvs = keyValues(labelsAndValues);

        buffer.append(join(" ", new ArrayList<StringBuffer>() { {
            for (KeyValue<String, Object> kv : kvs) {
                add(objectToString(kv.getKey(), kv.getValue()));
            }
        } }));
        buffer.append("]");

        return buffer.toString();
    }
}
