/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2005
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.dto;

import static edu.cmu.pslc.datashop.util.FormattingUtils.LC_DECIMAL_FORMAT;
import static edu.cmu.pslc.datashop.util.UtilConstants.HASH_INITIAL;
import static edu.cmu.pslc.datashop.util.UtilConstants.HASH_PRIME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.lang.reflect.Method;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.reflect.Modifier.isPublic;

/**
 * Default superclass for DTO objects, providing some default functionality,
 * mainly relating to introspection (for now).
 * @author jimbokun
 * @version $Revision: 5846 $
 * <BR>Last modified by: $Author: jrankin $
 * <BR>Last modified on: $Date: 2009-10-27 09:56:44 -0400 (Tue, 27 Oct 2009) $
 * <!-- $KeyWordsOff: $ -->
 */
public class DTO {
    /** The Properties annotation gives hints for marshalling a DTO into XML, JSON, etc. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Properties { String root(); String[] properties(); }

    /** Prefix for accessor methods */
    private static final String GET = "get";
    /** Prefix for boolean accessor methods */
    private static final String IS = "is";

    /**
     * Whether m is a "get" accessor method.
     * @param m a method
     * @return whether m is a "get" accessor method.
     */
    private boolean isAccessor(Method m) {
        String name = m.getName();

        return isPublic(m.getModifiers())
            && (name.startsWith(GET) || name.startsWith(IS))
            && m.getParameterTypes().length == 0
            && !"getClass".equals(m.getName());
    }

    /**
     * The property name corresponding to accessor method m.
     * (WARNING:  Only call on methods for which isAccessor is true.)
     * @param m an accessor method (as determined by isAccessor)
     * @return the property name corresponding to accessor method m.
     */
    private String propertyName(Method m) {
        String name = m.getName();
        String prefix = name.startsWith(GET) ? GET : IS;

        return toLowerCase(name.charAt(prefix.length()))
        + name.substring(prefix.length() + 1);
    }

    /**
     * Returns a string representation of this item, includes the hash code.
     * Note, it does not include sets which are not actually part of this class.
     * @return a string representation of this item
     */
    public String toString() {
         StringBuffer buffer = new StringBuffer();
         Map<String, Object> props = propertiesMap();

         buffer.append(getClass().getName());
         buffer.append("@");
         buffer.append(Integer.toHexString(hashCode()));
         buffer.append(" [");
         boolean first = true;
         for (String propName : props.keySet()) {
             buffer.append((first ? "" : " ") + propName + "::" + props.get(propName));
             first = false;
         }
         buffer.append("]");

         return buffer.toString();
    }

    /**
     * Check for a Properties annotation.
     * @return the Properties annotation for this class, if there is one
     */
    private List<String> propertyKeys() {
        Properties dtoKeys = getClass().getAnnotation(Properties.class);
        List<String> empty = emptyList();

        return dtoKeys != null ? asList(dtoKeys.properties()) : empty;
    }

    /**
     * Return a dictionary mapping each property name to its value.  This method is used
     * to convert a DTO to a JSONObject.  Since this method uses recursion, we do not want
     * to return a null map if an exception occurs - that would mean that recursive
     * processing would end.  It's better to return a map with most values.
     * @return A dictionary mapping each property name to its value.
     */
    public Map<String, Object> propertiesMap() {
        Map<String, Object> props = new HashMap<String, Object>();

        for (Method method : getClass().getDeclaredMethods()) {
            if (isAccessor(method)) {
                try {
                    Object property = method.invoke(this);
                    if (property instanceof DTO) {
                        property = ((DTO)property).propertiesMap();
                    } else if (property instanceof List) {
                        List<Object> properties = (List<Object>)property;
                        List<Object> mappedProperties = new ArrayList<Object>();
                        for (Object obj : properties) {
                            if (obj instanceof DTO) {
                                mappedProperties.add(((DTO)obj).propertiesMap());
                            } else {
                                mappedProperties.add(obj);
                            }
                        }
                        property = mappedProperties;
                    }
                    props.put(propertyName(method), property);
                } catch (Exception e) {
                    System.err.println("Error creating DTO property map: " + e);
                }
            }
        }

        // if there is a properties annotation, keep just those keys in the specified order
        List<String> orderedKeys = propertyKeys();

        if (!orderedKeys.isEmpty()) {
            Map<String, Object> orderedProps = new LinkedHashMap<String, Object>();

            for (String key : orderedKeys) { orderedProps.put(key, props.get(key)); }
            props = orderedProps;
        }

        return props;
    } // end propertiesMap()

    /**
     * Find the "set" or "add" method corresponding to property and use it to set the value.
     * @param property the property
     * @param value the value to set
     * @throws Exception if the property does not exist, or some problem with reflection
     */
    public void setPropertyValue(String property, Object value) throws Exception {
        if (property == null || property.isEmpty()) {
            throw invalidPropertyException(property);
        }

        String capitalizedProperty = toUpperCase(property.charAt(0)) + property.substring(1);
        String setterName = "set" + capitalizedProperty;
        String adderName = "add" + capitalizedProperty;

        for (Method method : getClass().getDeclaredMethods()) {
            if (isSetter(value, setterName, adderName, method)) {
                method.invoke(this, value);
                return;
            }
        }
        throw invalidPropertyException(property);
    }

    /**
     * Thrown when trying to set a property that does not exist.
     * @param property the property
     * @return an "invalid property name" exception
     */
    private IllegalArgumentException invalidPropertyException(String property) {
        return new IllegalArgumentException("Invalid property name " + property);
    }

    /**
     * Whether the method name starts with "set" or "add" and the method accepts one argument
     * of the same class as value.
     * @param value the value to set
     * @param setterName e.g. "setName"
     * @param adderName e.g. "addName"
     * @param method the method
     * @return whether this is a "setter" method
     */
    private boolean isSetter(Object value, String setterName, String adderName,
            Method method) {
        String methodName = method.getName();
        Class< ? >[] parameterTypes = method.getParameterTypes();

        return isPublic(method.getModifiers())
            && (methodName.equals(setterName) || methodName.equals(adderName))
            && parameterTypes.length == 1
            && (value == null || parameterTypes[0].equals(value.getClass()));
    }

    /**
     * Format the provided value with the LC_FORMATTER.
     * @param value the value to be formatted.
     * @return a nicely formatted value, or null if the value is null.
     */
    protected Double formatMe(Double value) {
        if (value == null) {
            return value;
        } else {
            return Double.parseDouble(LC_DECIMAL_FORMAT.format(value));
        }
    }

    /**
     * Calculate the hash code of this DTO object based on the hash codes of the fields that
     * uniquely identify this DTO. Call this method from subclass hashCode method
     * and pass the fields that uniquely identify this DTO as parameters.
     * @param os fields that uniquely identify this DTO
     * @return hash code value calculated from the hash codes of the parameters
     */
    protected int hashPrime(Object... os) {
        long hash = HASH_INITIAL;

        for (Object o : os) {
            hash = hash * HASH_PRIME + o.hashCode();
        }

        return (int)(hash % Integer.MAX_VALUE);
    }
} // end DTO.java