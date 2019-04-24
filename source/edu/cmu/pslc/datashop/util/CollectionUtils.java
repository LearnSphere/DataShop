/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.pslc.datashop.util;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for processing Collections.
 *
 * @author jimbokun
 * @version $Revision: 8625 $
 * <BR>Last modified by: $Author: mkomisin $
 * <BR>Last modified on: $Date: 2013-01-31 09:03:36 -0500 (Thu, 31 Jan 2013) $
 * <!-- $KeyWordsOff: $ -->
 */
public final class CollectionUtils {
    /**
     * Private constructor for utility class.
     */
    private CollectionUtils() { }

    /**
     * A convenient way to create a set.
     * @param <E> the type of things in the set
     * @param items the things in the set
     * @return the set
     */
    public static <E> Set<E> set(final E... items) { return set(Arrays.asList(items)); }

    /**
     * A convenient way to create a set.
     * @param <E> the type of things in the set
     * @param items the things in the set
     * @return the set
     */
    public static <E> Set<E> set(List<E> items) { return new HashSet<E>(items); }

    /**
     * Little class to represent a key and a value.
     * @author jimbokun
     * @param <K> type of the key
     * @param <V> type of the value
     */
    public static class KeyValue<K, V> {
        /** the key and the value */
        private K key;
        /** the value */
        private V value;

        /**
         * Constructor.
         * @param k the key
         * @param v the value
         */
        public KeyValue(K k, V v) { this.key = k; this.value = v; };

        /** The key. @return the key */
        public K getKey() { return key; }

        /** The value. @return the value */
        public V getValue() { return value; }

        /**
         * HTML style attribute string k="v".
         * @return HTML style attribute string k="v"
         */
        public String toString() { return key + "=\"" + value + '"'; }
    }

    /**
     * Pairs up alternating keys and values into KeyValue objects.
     * @param kvs alternating keys and values
     * @param <K> type of keys
     * @param <V> type of values
     * @return alternating keys and values paired up into KeyValue objects
     */
    public static <K, V> List<KeyValue<K, V>> keyValues(final Object... kvs) {
        try {
            return new ArrayList<KeyValue<K, V>>() { {
                for (int i = 1; i < kvs.length; i += 2) {
                    add(new KeyValue<K, V>((K)kvs[i - 1], (V)kvs[i]));
                }
            } };
        } catch (ClassCastException cce) {
            throw new IllegalStateException("key is the wrong type");
        } catch (ArrayIndexOutOfBoundsException arr) {
            throw new IllegalStateException("you must have balanced key/value pairs");
        }
    }

    /**
     * Convenience method for creating a map from keys and values.
     * @param keyValues alternating keys of type K and values of type V
     * @param <K> type of keys
     * @param <V> type of values
     * @return Map constructed from the alternating keys and values
     */
    public static <K, V> Map<K, V> map(final Object... keyValues) {
        final List<KeyValue<K, V>> kvs = keyValues(keyValues);

        return new HashMap<K, V>() { {
            for (KeyValue<K, V> kv : kvs) { put(kv.getKey(), kv.getValue()); }
        } };
    }

    /**
     * The list resulting from appending all of the lists in items.
     * @param <E> the type of things in the lists
     * @param items a list of lists
     * @return The list resulting from appending all of the lists in items.
     */
    public static <E> List<E> append(final List<E>... items) {
        List<E> appended = new ArrayList<E>();

        for (List<E> item : items) { appended.addAll(item); }

        return appended;
    }

    /**
     * The result of adding item to the front of items.
     * @param <E> the type of the item and the list
     * @param item the item
     * @param items the items
     * @return the result of adding item to the front of items
     */
    public static <E> List<E> prepend(E item, List<E> items) {
        return append(Collections.singletonList(item), items);
    }

    /**
     * set if it's not null, a newly created set otherwise.
     * @param <T> the kind of things in the set
     * @param set a possibly null set
     * @return set if it's not null, a newly created set otherwise
     */
    public static <T> Set<T> checkNull(Set<T> set) {
        return set == null ? new HashSet<T>() : set;
    }

    /**
     * list if it's not null, a newly created list otherwise.
     * @param <T> the kind of things in the list
     * @param list a possibly null list
     * @return list if it's not null, a newly created list otherwise
     */
    public static <T> List<T> checkNull(List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }

    /**
     * map if it's not null, a newly created map otherwise.
     * @param <K> the type of the map keys
     * @param <V> the type of the map values
     * @param map a possibly null map
     * @return map if it's not null, a newly created map otherwise.
     */
    public static <K, V> Map<K, V> checkNull(Map<K, V> map) {
        return map == null ? new HashMap<K, V>() : map;
    }

    /**
     * Translate an Enumeration into an equivalent Iterable.  This is useful for the
     * abbreviated for loop syntax.
     * @param <E> the kind of thing we want to iterate over
     * @param e the enumeration
     * @return an Iterable over the items in the enumeration
     */
    public static <E> Iterable<E> iter(final Enumeration<E> e) {
        final Iterator<E> iter = new Iterator<E>() {
            public boolean hasNext() { return e.hasMoreElements(); }

            public E next() { return e.nextElement(); }

            public void remove() { throw new UnsupportedOperationException(); }
        };
        return new Iterable<E>() { public Iterator<E> iterator() { return iter; } };
    }

    /**
     * Iterate over partitions of the items.  For example, if the partition size is 10,
     * first a list containing items 0 - 9 is returned, then 10 - 19, etc.
     * @param <T> the kind of thing in the list
     * @param partitionSize size of each partition
     * @param items the items to partition
     * @return an Iterable of the partitions that can be used in a for loop
     */
    public static <T> Iterable<List<T>> partition(final int partitionSize, final List<T> items) {
        final Iterator<List<T>> iter = new Iterator<List<T>>() {
            private final int total = items.size();
            private final int lastBatchEnd = total + partitionSize;
            private int batchEnd = partitionSize;

            public boolean hasNext() { return batchEnd < lastBatchEnd; }

            public List<T> next() {
                List<T> batch = items.subList(batchEnd - partitionSize, min(batchEnd, total));
                batchEnd += partitionSize;
                return batch;
            }

            public void remove() { throw new UnsupportedOperationException(); }
        };
        return new Iterable<List<T>>() { public Iterator<List<T>> iterator() { return iter; } };
    }

    /**
     * Iterate over partitions of the items.  For example, if the partition size is 10,
     * first a list containing items 0 - 9 is returned, then 10 - 19, etc.
     * @param <T> the kind of thing in the list
     * @param partitionSize size of each partition
     * @param items the items to partition
     * @return an Iterable of the partitions that can be used in a for loop
     */
    public static <T> Iterable<List<T>> partition(
            final int partitionSize, final Iterator<T> items) {
        final Iterator<List<T>> iter = new Iterator<List<T>>() {
            public boolean hasNext() { return items.hasNext(); }

            public List<T> next() {
                return new ArrayList<T>() { {
                    for (int i = 0; i < partitionSize && hasNext(); i++) {
                        add(items.next());
                    }
                } };
            }

            public void remove() { throw new UnsupportedOperationException(); }
        };
        return new Iterable<List<T>>() { public Iterator<List<T>> iterator() { return iter; } };
    }

    /**
     * Select the items from list with the specified index values.
     * So selecting (1, 3, 4) from [a b c d e] returns [b d e].
     * @param <E> the kind of elements in the list
     * @param indices the index values
     * @param list the list from which to select
     * @return the items from list with the specified index values
     */
    public static <E> List<E> select(final List<Integer> indices, final List<E> list) {
        return new ArrayList<E>() { {
            for (int index : indices) {
                if (index < list.size()) { add(list.get(index)); }
            }
        } };
    }

    /**
     * Used by DefaultMap and subclasses.
     * @author jimbokun
     * @return the interface
     */
    public interface Factory<V> { V create(); }

    /**
     * Returns a default object if no value for a key.
     * @author jimbokun
     * @param <K> key type
     * @param <V> value type
     */
    public static class DefaultMap<K, V> extends HashMap<K, V> {
        /** the factory that generates default values */
        private Factory<V> factory;

        /**
         * Constructor.
         * @param factory the factory that generates default values
         */
        public DefaultMap(Factory<V> factory) { this.factory = factory; }

        /**
         * If there is no value for key, create a default object with the factory and
         * return that.
         * @param key the key
         * @return the value for key, or a default object if it's not there
         */
        public V getDefault(K key) {
            if (!containsKey(key)) { put(key, factory.create()); }
            return get(key);
        }
    }

    /**
     * Default value is a list, for one to many mappings.
     * @author jimbokun
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static class ManyMap<K, V> extends DefaultMap<K, List<V>> {
        /** Constructor. */
        public ManyMap() {
            super(new Factory<List<V>>() {
                public List<V> create() { return new ArrayList<V>(); }
            });
        }
    }

    /**
     * Return defaultValue if no value for key.
     * @author jimbokun
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static class DefaultValueMap<K, V> extends DefaultMap<K, V> {
        /**
         *  Return defaultValue if no value for key.
         *  @param defaultValue return this if no value for key
         */
        public DefaultValueMap(final V defaultValue) {
            super(new Factory<V>() { public V create() { return defaultValue; } });
        }
    }
}
