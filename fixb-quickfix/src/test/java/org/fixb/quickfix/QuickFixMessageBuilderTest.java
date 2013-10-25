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

package org.fixb.quickfix;

import org.fixb.FixMessageBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.junit.Test;
import quickfix.FieldNotFound;
import quickfix.Message;

import java.math.BigDecimal;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNull;

public class QuickFixMessageBuilderTest {
    private final FixMessageBuilder<Message> builder = new QuickFixMessageBuilder();

    @Test
    public void testBuild() throws FieldNotFound {
        // When
        final DateTime time = DateTime.now();

        final Message message = builder.setField(11, 'A', true)
                .setField(12, 'Z', false)
                .setField(13, 1234, true)
                .setField(14, 4321, false)
                .setField(15, true, true)
                .setField(16, false, false)
                .setField(17, 123.123, true)
                .setField(18, 321.321, false)
                .setField(19, "TEST1", true)
                .setField(20, "TEST2", false)
                .setField(21, new BigDecimal(555.55), true)
                .setField(22, new BigDecimal(777.77), false)
                .setField(23, new LocalDate(2010, 10, 10), true)
                .setField(24, new LocalDate(2012, 12, 12), false)
                .setField(25, time, true)
                .setField(26, time.plus(1000), false)
                .setField(27, Thread.State.NEW, true)
                .setField(28, Thread.State.TERMINATED, false)
                .setField(29, (Object) null, false)
                .build();

        // Then
        assertEquals('A', message.getHeader().getChar(11));
        assertEquals('Z', message.getChar(12));
        assertEquals(1234, message.getHeader().getInt(13));
        assertEquals(4321, message.getInt(14));
        assertEquals(true, message.getHeader().getBoolean(15));
        assertEquals(false, message.getBoolean(16));
        assertEquals(123.123, message.getHeader().getDouble(17));
        assertEquals(321.321, message.getDouble(18));
        assertEquals("TEST1", message.getHeader().getString(19));
        assertEquals("TEST2", message.getString(20));
        assertEquals(new BigDecimal(555.55), message.getHeader().getDecimal(21));
        assertEquals(new BigDecimal(777.77), message.getDecimal(22));
        assertEquals(toUtcDate(new LocalDate(2010, 10, 10)), message.getHeader().getUtcDateOnly(23));
        assertEquals(toUtcDate(new LocalDate(2012, 12, 12)), message.getUtcDateOnly(24));
        assertEquals(time.toDate().toString(), message.getHeader().getUtcTimeStamp(25).toString());
        assertEquals(time.plus(1000).toDate().toString(), message.getUtcTimeStamp(26).toString());
        assertEquals(Thread.State.NEW.ordinal() + 1, message.getHeader().getInt(27));
        assertEquals(Thread.State.TERMINATED.ordinal() + 1, message.getInt(28));
        try {
            assertNull(message.getString(29));
            fail("Expected FieldNotFound exception.");
        } catch (FieldNotFound e) {
            // expected exception
        }
    }

    private static Date toUtcDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();
    }
}
