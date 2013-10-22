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

import org.fixb.meta.FixBlockMeta;
import org.fixb.meta.FixDynamicFieldMeta;
import org.fixb.meta.FixEnumMeta;
import org.fixb.meta.FixEnumRepository;
import org.fixb.test.TestHelper;
import org.joda.time.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Arrays.asList;
import static org.fixb.test.data.SampleQuote.Side;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class NativeFixFieldExtractorTest {
    private final FixEnumRepository fixEnumRepository = mock(FixEnumRepository.class);
    private final NativeFixFieldExtractor extractor = new NativeFixFieldExtractor(fixEnumRepository);

    {
        given(fixEnumRepository.hasFixEnumMeta(Side.class)).willReturn(true);
        given(fixEnumRepository.getFixEnumMeta(Side.class)).willReturn(FixEnumMeta.forEnumClass(Side.class));
    }

    @Test
    public void testGetFieldValue() throws Exception {
        final String fix = TestHelper.fix("101=blah", "102=10", "103=10.10", "104=0");

        assertEquals("blah", extractor.getFieldValue(fix, String.class, 101, false));
        assertEquals(10, extractor.getFieldValue(fix, int.class, 102, false).intValue());
        assertEquals(10, extractor.getFieldValue(fix, Integer.class, 102, false).intValue());
        assertEquals(new BigDecimal("10.10"), extractor.getFieldValue(fix, BigDecimal.class, 103, false));
        assertEquals(10.10, extractor.getFieldValue(fix, double.class, 103, false), 0);
        assertEquals(10.10, extractor.getFieldValue(fix, Double.class, 103, false), 0);
        assertEquals(10.10, extractor.getFieldValue(fix, Double.class, 103, false), 0);
        assertEquals(10.10, extractor.getFieldValue(fix, Double.class, 103, false), 0);
        assertEquals(10.10, extractor.getFieldValue(fix, Double.class, 103, false), 0);
        assertEquals(Side.BUY, extractor.getFieldValue(fix, Side.class, 104, false));
    }

    @Test
    public void testGetSimpleGroups() throws Exception {
        final String fix = TestHelper.fix("100=3", "101=one", "101=II", "101=3", "101=four");

        List<String> groups = extractor.getGroups(fix, List.class, 100, String.class, 101, false);

        assertEquals(3, groups.size());
        assertEquals("one", groups.get(0));
        assertEquals("II", groups.get(1));
        assertEquals("3", groups.get(2));
    }

    @Test
    public void testGetSimpleGroupsWithEnums() throws Exception {
        final String fix = TestHelper.fix("100=3", "101=1", "101=0");

        List<Side> groups = extractor.getGroups(fix, List.class, 100, Side.class, 101, false);

        assertEquals(2, groups.size());
        assertEquals(Side.SELL, groups.get(0));
        assertEquals(Side.BUY, groups.get(1));
    }

    @Test
    public void testGetComplexGroups() throws Exception {
        final String fix = TestHelper.fix("100=2", "101=one", "102=10", "103=1.1", "101=two", "102=20", "103=2.2");

        FixBlockMeta<Sample> componentMeta = new FixBlockMeta<>(Sample.class, asList(
                new FixDynamicFieldMeta(101, false, false, Sample.class.getDeclaredField("f1")),
                new FixDynamicFieldMeta(102, false, false, Sample.class.getDeclaredField("f2")),
                new FixDynamicFieldMeta(103, false, false, Sample.class.getDeclaredField("f3"))));

        List<Sample> groups = extractor.getGroups(fix, List.class, 100, componentMeta, false);

        assertEquals(2, groups.size());
        assertEquals(new Sample("one", 10, 1.1), groups.get(0));
        assertEquals(new Sample("two", 20, 2.2), groups.get(1));
    }

    @Test
    public void shouldSupportJodaTime() throws Exception {
        final String fix = TestHelper.fix(
                "101=20121010-10:10:10",
                "102=20121010",
                "103=20121010-10:10:10",
                "104=10:10:10");

        assertEquals(Instant.parse("2012-10-10T10:10:10"), extractor.getFieldValue(fix, Instant.class, 101, false));
        assertEquals(DateTime.parse("2012-10-10T10:10:10"), extractor.getFieldValue(fix, DateTime.class, 103, false));
        assertEquals(LocalDateTime.parse("2012-10-10T10:10:10"), extractor.getFieldValue(fix, LocalDateTime.class, 101, false));
        assertEquals(LocalDate.parse("2012-10-10"), extractor.getFieldValue(fix, LocalDate.class, 102, false));
        assertEquals(LocalTime.parse("10:10:10"), extractor.getFieldValue(fix, LocalTime.class, 104, false));
    }

    public static class Sample {
        private final String f1;
        private final int f2;
        private final double f3;

        public Sample(String f1, int f2, double f3) {
            this.f1 = f1;
            this.f2 = f2;
            this.f3 = f3;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Sample)) {
                return false;
            }
            Sample o = (Sample) obj;
            return f1.equals(o.f1) && f2 == o.f2 && f3 == o.f3;
        }
    }
}
