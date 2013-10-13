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

package org.fix4j.adapter;

import org.fix4j.FixFieldExtractor;
import org.fix4j.FixMessageBuilder;
import org.fix4j.meta.FixMetaRepository;
import org.fix4j.meta.FixMetaRepositoryImpl;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;

public class AbstractFixAdapterTest {
    final FixMetaRepository fixMetaRepository = new FixMetaRepositoryImpl();
    final FixFieldExtractor<Map<Integer, Object>> fixFieldExtractor = mock(FixFieldExtractor.class);
    final FixMessageBuilder<Map<Integer, Object>> fixMessageBuilder = mock(FixMessageBuilder.class);
    final FixMessageBuilder.Factory<Map<Integer, Object>, FixMessageBuilder<Map<Integer, Object>>>
            builderFactory = mock(FixMessageBuilder.Factory.class);
    private final Map<Integer, Object> fixMessage = new HashMap<Integer, Object>();

    @Before
    public void setup() {
        fixMetaRepository.addPackage("org.fix4j.test");

        given(builderFactory.create()).willReturn(fixMessageBuilder);

        given(fixMessageBuilder.build()).willReturn(fixMessage);

        given(fixMessageBuilder.setField(anyInt(), anyString(), anyBoolean()))
                .will(new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                        final Object[] arguments = invocationOnMock.getArguments();
                        fixMessage.put((Integer) arguments[0], arguments[1]);
                        return fixMessageBuilder;
                    }
                });

        given(fixMessageBuilder.setField(anyInt(), any(), anyBoolean()))
                .will(new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                        final Object[] arguments = invocationOnMock.getArguments();
                        fixMessage.put((Integer) arguments[0], arguments[1]);
                        return fixMessageBuilder;
                    }
                });

        given(fixFieldExtractor.getFieldValue(anyMap(), any(Class.class), anyInt(), anyBoolean()))
                .will(new Answer<Object>() {
                    public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                        final Object[] arguments = invocationOnMock.getArguments();
                        final Map<Integer, Object> map = (Map<Integer, Object>) arguments[0];
                        return map.get(arguments[2]);
                    }
                });
    }
}
