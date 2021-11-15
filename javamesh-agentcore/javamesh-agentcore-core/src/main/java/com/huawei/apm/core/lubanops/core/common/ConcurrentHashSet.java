/*
 * lianlianpay.com Copyright (c) 2019-2027 All Rights Reserved.
 *
 */

package com.huawei.apm.core.lubanops.core.common;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, java.io.Serializable {

    private static final long serialVersionUID = -8672117787651310382L;

    private static final Object PRESENT = new Object();

    private final ConcurrentMap<E, Object> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<E, Object>();
    }

    public ConcurrentHashSet(int initialCapacity) {
        map = new ConcurrentHashMap<E, Object>(initialCapacity);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    /**
     * Removes all of the elements from this set. The set will be empty after this call returns.
     */
    @Override
    public void clear() {
        map.clear();
    }

}
