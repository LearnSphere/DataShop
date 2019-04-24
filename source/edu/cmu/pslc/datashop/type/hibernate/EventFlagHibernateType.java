/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.type.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.usertype.UserType;

import edu.cmu.pslc.datashop.type.EventFlag;



/**
 * UserType to allow Hibernate to uses the EventFlag Enumeration type.
 *
 * @author Benjamin Billings
 * @version $Revision: 3951 $
 * <BR>Last modified by: $Author: bkb $
 * <BR>Last modified on: $Date: 2007-04-03 10:16:35 -0400 (Tue, 03 Apr 2007) $
 * <!-- $KeyWordsOff: $ -->
 */
public class EventFlagHibernateType implements UserType {

    /** Array of all available SQL Types */
    private static final int[] SQL_TYPES = {Types.VARCHAR};

    /**
     * Get the list of available SQL types for this type.
     * @return int[] of SQL types.
     */
    public int[] sqlTypes() { return SQL_TYPES; }

    /**
     * The class this type refers to.
     * @return CorrectFlag.class.
     */
    public Class returnedClass() { return EventFlag.class; }

    /**
     * Test whether the 2 object are the same since we are looking
     * at single instances of the variable.
     * @param x the first object.
     * @param y the 2nd object.
     * @return true if the objects are equal, false otherwise.
     */
    public boolean equals(Object x, Object y) { return x == y; }

    /**
     * The deepCopy method required by UserType.
     * Returns the object since this enumeration type only has static instances..
     * @param value the Object to copy.
     * @return the Object to copy.
     */
    public Object deepCopy(Object value) { return value; }


    /**
     * Serialize the object for caching.
     * @param value the object to serialize.
     * @return the serialized object as a Serializable.
     */
    public Serializable disassemble(Object value) {
        return (Serializable)value; //this object knows how to serialize itself.
    }

    /**
     * Assemble the object from it's serialized cached version.
     * @param cached the cached serialized object.
     * @param owner the owner of the object.
     * @return Object the deserialzed object.
     */
    public Object assemble(Serializable cached, Object owner) {
        return (Object)cached; //this type knows how to deserialize itself.
    }

    /**
     * For when merging replaces the correct field.  Since this object is immutable we
     * just return the original object.
     * @param original the original object.
     * @param target the target object.
     * @param owner the owner of the object.
     * @return the merged object. (The original for this instance)
     */
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    /**
     * HashCode for this object.
     * @param obj Object the object to get a hash code for.
     * @return int of the hash code.
     */
    public int hashCode(Object obj) {
        return obj.hashCode();
    }

    /**
     * Test for whether this type is mutable.
     * @return Always return boolean false as this is not a mutable type.
     */
    public boolean isMutable() { return false; }

    /**
     * Get the outcome to return.  This method is null safe.
     * @param resultSet The result set from the database.
     * @param columnNames the names of all retrieved columns.
     * @param owner I dont't have a clue what this is used for.
     * @return Object return either null or and an CorrectFlag.
     * @throws SQLException SQLException.
     */
    public Object nullSafeGet(ResultSet resultSet, String[] columnNames, Object owner)
        throws SQLException {

        int indexString = resultSet.getInt(columnNames[0]);
        EventFlag toReturn = null;
        if (!resultSet.wasNull()) {
            toReturn = EventFlag.getInstance(indexString);
        }
        return toReturn;
    }

    /**
     * Modify the prepared statement with the correct value at the correct index for this Type.
     * @param statement The prepared statement to modify.
     * @param value the value to add to the statement.
     * @param index the index at which to add the value.
     * @throws SQLException SQLException.
     */
    public void nullSafeSet(PreparedStatement statement, Object value, int index)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            //because the value in the DB is actually an ENUM of "1", "2", ...
            //have to set as a string.
            statement.setString(index, "" + value.hashCode());
        }
    }
}
