package edu.tamu.scholars.middleware.discovery.service.component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Spliterator;

public class AddOnlyAtomicHashSet<T> extends HashSet<T> {

    static final long serialVersionUID = -5183765298735627345L;

    private AddOnlyAtomicHashSet() {

    }

    @Override
    public boolean equals(Object o) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public synchronized boolean add(T e) {
        return super.add(e);
    }

    @Override
    public Iterator<T> iterator() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int size() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean isEmpty() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean contains(Object o) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Object clone() {
        throw new RuntimeException("Operation not supported");
    }

    public Spliterator<T> spliterator() {
        throw new RuntimeException("Operation not supported");
    }

    public static AddOnlyAtomicHashSet<String> forCreatedFields() {
        return new AddOnlyAtomicHashSet<String>();
    }

}
