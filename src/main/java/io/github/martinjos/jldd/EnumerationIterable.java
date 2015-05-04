package io.github.martinjos.jldd;

import java.util.*;

public class EnumerationIterable<T> implements Iterable<T> {

    private Enumeration<? extends T> et;

    public EnumerationIterable(Enumeration<? extends T> et) {
        this.et = et;
    }

    public Iterator<T> iterator() {
        return new EnumerationIterator<T>(et);
    }
}
