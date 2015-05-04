package io.github.martinjos.jldd;

import java.util.*;

public class EnumerationIterator<T> implements Iterator<T> {

    private Enumeration<? extends T> et;

    public EnumerationIterator(Enumeration<? extends T> et) {
        this.et = et;
    }

    public boolean hasNext() {
        return et.hasMoreElements();
    }

    public T next() {
        return et.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
