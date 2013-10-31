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

package org.fixb.meta;

import org.fixb.annotations.FixBlock;
import org.fixb.annotations.FixField;
import org.fixb.annotations.FixGroup;
import org.fixb.annotations.FixMessage;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;
import static org.fixb.meta.MutableFixMetaDictionaryTest.Sample.Part;

public class MutableFixMetaDictionaryTest {

    private MutableFixMetaDictionary fixMetaDictionary = new MutableFixMetaDictionary();

    @Test
    public void testGetMetaForUnregisteredClassRegistersMessageType() {
        // When
        final FixMessageMeta<Sample> meta = fixMetaDictionary.getMetaForClass(Sample.class);

        // Then
        assertSame(meta, fixMetaDictionary.getMetaForMessageType("TEST"));
    }

    @Test
    public void testGetMetaForRegisteredClassReturnsPreloadedMeta() {
        // Given
        final FixMessageMeta<Sample> meta = fixMetaDictionary.getMetaForClass(Sample.class);

        // When / Then
        assertSame(meta, fixMetaDictionary.getMetaForClass(Sample.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMetaForFixBlockClassThrowsException() {
        // Given
        fixMetaDictionary.getMetaForClass(Sample.class);
        assertTrue(fixMetaDictionary.containsMeta(Part.class));

        // When
        fixMetaDictionary.getMetaForClass(Part.class);

        // Then
        // expect IllegalStateException
    }

    @Test(expected = IllegalStateException.class)
    public void testGetMetaForUnregisteredMessageType() {
        // Given
        assertFalse(fixMetaDictionary.containsMeta("TEST"));

        // When
        fixMetaDictionary.getMetaForMessageType("TEST");

        // Then
        // expect IllegalStateException
    }

    @Test
    public void testGetMetaForRegisteredMessageType() {
        // Given
        fixMetaDictionary.getMetaForClass(Sample.class);

        // When
        final FixMessageMeta<Sample> meta = fixMetaDictionary.getMetaForMessageType("TEST");

        // Then
        assertEquals(Sample.class, meta.getType());
        assertEquals("TEST", meta.getMessageType());
    }

    @FixMessage(type = "TEST")
    public static class Sample {
        @FixGroup(tag = 11)
        final List<Part> parts;

        public Sample(@FixField(tag = 11) List<Part> parts) {
            this.parts = parts;
        }

        @FixBlock
        public static class Part {
            @FixField(tag = 123)
            final int value;

            public Part(@FixField(tag = 123) int value) {
                this.value = value;
            }
        }
    }
}
