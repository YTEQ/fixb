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

import org.fixb.test.data.TestModels;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.fixb.test.data.TestModels.Message1;

public class GenericFixAdapterTest extends AbstractFixAdapterTest {
    final GenericFixAdapter<Message1, Map<Integer, Object>> adapter =
            new GenericFixAdapter<Message1, Map<Integer, Object>>("FIX.4.4",
                    fixFieldExtractor, builderFactory, fixMetaRepository.getMetaForClass(Message1.class)
            );

    @Test
    public void testFromFixType1() {
        // Given
        final Map<Integer, Object> fixMessage = new HashMap<Integer, Object>();
        fixMessage.put(35, "M1");
        fixMessage.put(100, "VALUE");

        // When
        Message1 object = adapter.fromFix(fixMessage);

        // Then
        assertEquals("VALUE", object.component.value);
    }

    @Test
    public void testToFix() {
        // Given
        Message1 object = new Message1(new TestModels.Component("VALUE"));

        // When
        final Map<Integer, ?> fixMessage = adapter.toFix(object);

        // Then
        assertEquals(3, fixMessage.size());
        assertEquals("FIX.4.4", fixMessage.get(8));
        assertEquals("M1", fixMessage.get(35));
        assertEquals("VALUE", fixMessage.get(100));
    }
}
