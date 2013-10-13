/*
 * Copyright 2013 YTEQ Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fix4j.impl;

import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;

public class CollectionFactoryTest {

    @Test
    public void canCreateCollectionsOfDifferentTypes() throws InstantiationException, IllegalAccessException {
        // Sets
        assertEquals(new HashSet(), CollectionFactory.createCollection(Set.class));
        assertEquals(new HashSet(), CollectionFactory.createCollection(HashSet.class));
        assertEquals(new TreeSet(), CollectionFactory.createCollection(TreeSet.class));

        // Lists
        assertEquals(new ArrayList(), CollectionFactory.createCollection(List.class));
        assertEquals(new ArrayList(), CollectionFactory.createCollection(ArrayList.class));
        assertEquals(new LinkedList(), CollectionFactory.createCollection(LinkedList.class));
        assertEquals(new ArrayList(), CollectionFactory.createCollection(Collection.class));
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings("unchecked")
    public void cannotCreateCollectionOfNonCollectionType() throws InstantiationException, IllegalAccessException {
        final Class<?> collClass = String.class;
        CollectionFactory.createCollection((Class<Collection<?>>) collClass);
    }
}
