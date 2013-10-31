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

package org.fixb.adapter;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.fixb.test.data.TestModels.*;

public class CommonFixAdapterTest extends AbstractFixAdapterTest {
    final CommonFixAdapter<Map<Integer, Object>> adapter =
            new CommonFixAdapter<>("FIX.4.4", fixFieldExtractor, builderFactory, fixMetaDictionary);

    @Test
    public void testFromFixToMessage1() {
        // Given
        final Map<Integer, Object> fixMessage = new HashMap<>();
        fixMessage.put(35, "M1");
        fixMessage.put(100, "VALUE");

        // When
        Message1 object = (Message1) adapter.fromFix(fixMessage);

        // Then
        assertEquals("VALUE", object.component.value);
    }

    @Test
    public void testFromFixToMessage2() {
        // Given
        final Map<Integer, Object> fixMessage = new HashMap<>();
        fixMessage.put(35, "M2");
        fixMessage.put(100, "VALUE");

        // When
        Message2 object = (Message2) adapter.fromFix(fixMessage);

        // Then
        assertEquals("VALUE", object.component.value);
    }

    @Test
    public void testToFixFromMessage1() {
        // Given
        Message1 object = new Message1(new Component("VALUE"));

        // When
        final Map<Integer, ?> fixMessage = adapter.toFix(object);

        // Then
        assertEquals(3, fixMessage.size());
        assertEquals("FIX.4.4", fixMessage.get(8));
        assertEquals("M1", fixMessage.get(35));
        assertEquals("VALUE", fixMessage.get(100));
    }

    @Test
    public void testToFixFromMessage2() {
        // Given
        Message2 object = new Message2(new Component("VALUE"));

        // When
        final Map<Integer, ?> fixMessage = adapter.toFix(object);

        // Then
        assertEquals(3, fixMessage.size());
        assertEquals("FIX.4.4", fixMessage.get(8));
        assertEquals("M2", fixMessage.get(35));
        assertEquals("VALUE", fixMessage.get(100));
    }
}
