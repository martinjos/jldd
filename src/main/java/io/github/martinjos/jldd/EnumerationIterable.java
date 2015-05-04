/*
 * Copyright (c) 2015 Martin Sidaway
 *
 * Permission (in the form of a perpetual, worldwide, non-exclusive, no-charge,
 * royalty-free, irrevocable copyright and patent license) is hereby granted,
 * free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation (and in any combination)
 * the rights to use, copy, modify, merge, reimplement, publish, distribute,
 * sublicense, and/or sell copies of the Software (and/or portions thereof),
 * and to permit persons to whom the Software is furnished to do so.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT OR PATENT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 */

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
