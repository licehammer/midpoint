/*
 * Copyright (c) 2010-2013 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */

package com.evolveum.midpoint.web.page.admin.users.dto;

import org.apache.commons.lang.NotImplementedException;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author lazyman
 */
public class TreeStateSet<T extends Serializable> implements Set<T>, Serializable {

    private Set<T> set = new HashSet<>();
    private boolean inverse;

    public void expandAll() {
        set.clear();
        inverse = true;
    }

    public void collapseAll() {
        set.clear();
        inverse = false;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public boolean add(T t) {
        return inverse ? set.remove(t) : set.add(t);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        T t = (T) o;
        return inverse ? !set.contains(t) : set.contains(t);
    }

    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        T t = (T) o;
        return inverse ? set.add(t) : set.remove(t);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return inverse ? !set.containsAll(c) : set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return inverse ? set.removeAll(c) : set.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new NotImplementedException("Not yet implemented.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return inverse ? set.addAll((Collection<? extends T>) c) : set.removeAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public TreeStateSet clone() {
        TreeStateSet set = new TreeStateSet();
        set.inverse = this.inverse;
        set.set.addAll(this.set);

        return set;
    }
}
