/*
 * Copyright (C) 2022 Tony Guo. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jxtras.android.build.tools.arpt.options;

import java.util.Iterator;

public class ArgumentIterator implements Iterator<String> {

    /** The underlying argument iterator */
    private final Iterator<String> iterator;

    /** Extra state used to implement peek and current */
    private String current;
    private String buffered;

    public ArgumentIterator(Iterable<String> iterable) {
        iterator = iterable.iterator();
    }

    @Override
    public boolean hasNext() {
        return buffered != null || iterator.hasNext();
    }

    @Override
    public String next() {
        fillBuffer();
        current = buffered;
        buffered = null;
        return current;
    }

    /**
     * @return the last element returned by next() (or {@code null} if next has
     * never been invoked on this iterator).
     */
    public String current() {
        return current;
    }

    /**
     * Can't remove current element, since we may have buffered it.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Returns the next element without advancing the iterator
     */
    public String peek() {
        fillBuffer();
        return buffered;
    }

    private void fillBuffer() {
        if (buffered == null && iterator.hasNext()) {
            buffered = iterator.next();
        }
    }
}