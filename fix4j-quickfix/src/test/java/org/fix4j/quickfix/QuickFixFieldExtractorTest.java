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

package org.fix4j.quickfix;

import org.fix4j.FixException;
import org.fix4j.meta.FixBlockMeta;
import org.fix4j.meta.FixMetaScanner;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.junit.Test;
import quickfix.Message;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.fix4j.quickfix.QuickFixMessageBuilder.fixMessage;
import static org.fix4j.quickfix.test.data.TestModels.Component;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class QuickFixFieldExtractorTest {
    private final QuickFixFieldExtractor extractor = new QuickFixFieldExtractor();

    @Test
    public void testGetStringFieldValue() {
        // Given
        Message message = fixMessage().setField(10, "TEST VALUE").build();

        // When / Then
        assertEquals("TEST VALUE", extractor.getFieldValue(message, String.class, 10, false));
    }

    @Test
    public void testGetIntFieldValue() {
        // Given
        Message message = fixMessage().setField(10, 123).build();

        // When / Then
        assertEquals(123, extractor.getFieldValue(message, Integer.class, 10, false).intValue());
        assertEquals(123, extractor.getFieldValue(message, int.class, 10, false).intValue());
    }

    @Test
    public void testGetCharFieldValue() {
        // Given
        Message message = fixMessage().setField(10, 'X').build();

        // When / Then
        assertEquals('X', extractor.getFieldValue(message, Character.class, 10, false).charValue());
        assertEquals('X', extractor.getFieldValue(message, char.class, 10, false).charValue());
    }

    @Test
    public void testGetDoubleFieldValue() {
        // Given
        Message message = fixMessage().setField(10, 123.456).setField(20, 100.00).build();

        // When / Then
        assertEquals(123.456, extractor.getFieldValue(message, Double.class, 10, false), 0);
        assertEquals(100.00, extractor.getFieldValue(message, double.class, 20, false), 0);
    }

    @Test
    public void testGetBigDecimalFieldValue() {
        // Given
        Message message = fixMessage().setField(10, new BigDecimal(123.00)).build();

        // When / Then
        assertEquals(new BigDecimal(123.00), extractor.getFieldValue(message, BigDecimal.class, 10, false));
    }

    @Test
    public void testGetLocalDateFieldValue() {
        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"));
        DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getDefault()));
        // Given
        LocalDate date = LocalDate.parse("2012-10-10");
        Message message = fixMessage().setField(10, date).build();

        // When / Then
        assertEquals(date, extractor.getFieldValue(message, LocalDate.class, 10, false));
    }

    @Test
    public void testGetInstantFieldValue() {
        // Given
        Instant time = Instant.parse("2012-10-10T10:10:10");
        Message message = fixMessage().setField(10, time).build();

        // When / Then
        assertEquals(time, extractor.getFieldValue(message, Instant.class, 10, false));
    }

    @Test
    public void testGetBooleanFieldValue() {
        // Given
        Instant time = Instant.parse("2012-10-10T10:10:10");
        Message message = fixMessage().setField(10, time).build();

        // When / Then
        assertEquals(time, extractor.getFieldValue(message, Instant.class, 10, false));
    }

    @Test(expected = FixException.class)
    public void testGetFieldValueWithIncorrectType() {
        // Given
        Message message = fixMessage().setField(10, "TEXT").build();

        // When / Then
        assertEquals("TEXT", extractor.getFieldValue(message, int.class, 10, false));
    }

    @Test(expected = FixException.class)
    public void testGetFieldValueWithIncorrectTag() {
        // Given
        Message message = fixMessage().setField(10, "TEXT").build();

        // When / Then
        assertNull(extractor.getFieldValue(message, String.class, 12, true));
        assertEquals("TEXT", extractor.getFieldValue(message, String.class, 12, false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetFieldValueWithUnsupportedType() {
        // Given
        Message message = fixMessage().build();

        // When / Then
        assertEquals("777", extractor.getFieldValue(message, StringBuilder.class, 10, false));
    }

    @Test
    public void testGetGroupsWithSimpleElements() {
        // Given
        Message message = fixMessage().setGroups(10, 11, asList("A", "B", "C"), true)
                .setGroups(20, 21, asList("D", "E", "F"), false)
                .setGroups(30, 31, asList(1, 2, 3, 3), false).build();

        // When / Then
        assertEquals(asList("A", "B", "C"), extractor.getGroups(message, List.class, 10, String.class, 11, false));
        assertEquals(asList("D", "E", "F"), extractor.getGroups(message, List.class, 20, String.class, 21, false));
        assertEquals(asSet(1, 2, 3), extractor.getGroups(message, Set.class, 30, Integer.class, 31, false));
        assertEquals(asList(), extractor.getGroups(message, Collection.class, 333, Double.class, 444, true));
    }

    @Test
    public void testGetGroupsWithComplexElements() {
        // Given
        final FixBlockMeta<Component> componentMeta = FixMetaScanner.scanClass(Component.class);
        Message message = fixMessage()
                .setGroups(10, componentMeta.getFields(), asList(new Component("A"),
                        new Component("B"),
                        new Component("C")), true)
                .setGroups(20, componentMeta.getFields(), asList(new Component("D"),
                        new Component("E"),
                        new Component("F")), false).build();

        // When / Then
        assertEquals(asList(new Component("A"),
                new Component("B"),
                new Component("C")), extractor.getGroups(message, List.class, 10, componentMeta, false));
        assertEquals(asSet(new Component("D"),
                new Component("E"),
                new Component("F")), extractor.getGroups(message, Set.class, 20, componentMeta, false));
        assertEquals(asList(), extractor.getGroups(message, Collection.class, 333, componentMeta, true));
    }

    private static <T> Set<T> asSet(T... items) {
        Set<T> set = new HashSet<T>();
        set.addAll(asList(items));
        return set;
    }
}
