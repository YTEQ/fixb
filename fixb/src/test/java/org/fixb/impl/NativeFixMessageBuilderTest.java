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

package org.fixb.impl;

import org.fixb.FixException;
import org.fixb.meta.FixEnumMeta;
import org.fixb.meta.FixEnumRepository;
import org.fixb.test.data.SampleQuote;
import org.joda.time.*;
import org.junit.Test;

import java.math.BigDecimal;

import static org.fixb.FixConstants.BEGIN_STRING_TAG;
import static org.fixb.test.TestHelper.fix;
import static org.fixb.test.data.SampleQuote.Side;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class NativeFixMessageBuilderTest {
    private final FixEnumRepository fixEnumRepository = mock(FixEnumRepository.class);
    private final NativeFixMessageBuilder builder = new NativeFixMessageBuilder.Factory(fixEnumRepository).create();

    {
        when(fixEnumRepository.getFixEnumMeta(Side.class)).thenReturn(FixEnumMeta.forEnumClass(Side.class));
    }

    @Test
    public void testBuild() throws Exception {
        builder.setField(BEGIN_STRING_TAG, "FIX.5.0");
        builder.setField(11, 'a');
        builder.setField(12, 1);
        builder.setField(13, 11.00);
        builder.setField(14, true);
        builder.setField(15, new BigDecimal("1.23"));
        builder.setField(16, "string");
        builder.setField(17, new LocalDate("2012-02-01").toDateMidnight().toDate());
        builder.setField(18, Side.SELL);

        final String fix = builder.build();

        assertTrue("Fix message is incorrect", fix.matches(fix(
                "8=FIX.5.0",
                "9=67",
                "11=a",
                "12=1",
                "13=11.0",
                "14=Y",
                "15=1.23",
                "16=string",
                "17=20120201-00:00:00",
                "18=1",
                "10=[0-9]+")));
    }

    @Test
    public void testBuildWithJodaTime() throws Exception {
        builder.setField(BEGIN_STRING_TAG, "FIX.5.0");
        builder.setField(11, new LocalDate("2012-02-01"));
        builder.setField(12, new LocalTime("15:15:15.123"));
        builder.setField(13, new LocalDateTime("2012-02-01"));
        builder.setField(14, new DateTime("2012-02-01T01:00:00+0200"));
        builder.setField(15, new Instant("2012-02-01T01:01:01"));

        final String fix = builder.build();

        assertTrue("Fix message is incorrect", fix.matches(fix(
                "8=FIX.5.0",
                "9=91",
                "11=20120201",
                "12=15:15:15.123",
                "13=20120201-00:00:00",
                "14=20120131-23:00:00",
                "15=20120201-01:01:01",
                "10=[0-9]+")));
    }

    @Test(expected = FixException.class)
    public void testThrowsExceptionWhenBeginStringIsNotSet() throws Exception {
        builder.setField(123, "any value");
        builder.build();
    }
}
