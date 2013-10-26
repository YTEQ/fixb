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

package org.fixb.test.perf;

import org.fixb.FixSerializer;
import org.fixb.adapter.CommonFixAdapter;
import org.fixb.impl.NativeFixSerializer;
import org.fixb.meta.FixMetaDictionary;
import org.fixb.meta.FixMetaScanner;
import org.fixb.quickfix.QuickFixFieldExtractor;
import org.fixb.quickfix.QuickFixMessageBuilder;
import org.fixb.quickfix.QuickFixSerializer;
import org.fixb.test.perf.TestModels.SampleQuote;
import org.junit.Test;
import quickfix.Message;

import java.util.Random;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static org.fixb.test.perf.TestModels.SampleQuote.Side.BUY;
import static org.fixb.test.perf.TestModels.SampleQuote.Side.SELL;

/**
 * A performance test that runs different implementations of FixSerializer with preheating cycles and prints out the
 * captured timings.
 */
public class NativeSerializerPerfTest {

    public static final String FIX_5_0 = "FIX.5.0";
    private final FixMetaDictionary fixMetaRepository = FixMetaScanner.scanClassesIn("org.fixb.test.perf");
    private final FixSerializer<Object> nativeSerializer = new NativeFixSerializer<>(FIX_5_0, fixMetaRepository);
    private final FixSerializer<Message> quickFixSerializer = new QuickFixSerializer(FIX_5_0, fixMetaRepository);
    private final CommonFixAdapter<Message> quickFixAdapter = new CommonFixAdapter<>(FIX_5_0,
            new QuickFixFieldExtractor(), new QuickFixMessageBuilder.Factory(), fixMetaRepository);

    @Test
    public void run() {
        int messageCount = 1000;
        final SampleQuote[] objectData = generateData(messageCount);
        final Message[] quickFixData = new Message[messageCount];
        final String[] fixData = new String[messageCount];

        measure("Iterations", 10, new Runnable() {
            @Override
            public void run() {
                int laps = 10;
                measure("Serialize with FixB/Native", laps, new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (SampleQuote sampleQuote : objectData) {
                            fixData[i++] = nativeSerializer.serialize(sampleQuote);
                        }
                    }
                });
                measure("Generate QuickFixJ data", laps, new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (SampleQuote sampleQuote : objectData) {
                            quickFixData[i++] = quickFixAdapter.toFix(sampleQuote);
                        }
                    }
                });
                measure("Serialize with FixB/QuickFixJ", laps, new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (SampleQuote sampleQuote : objectData) {
                            fixData[i++] = quickFixSerializer.serialize(quickFixAdapter.toFix(sampleQuote));
                        }
                    }
                });
                measure("Serialize with QuickFixJ Only", laps, new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        for (Message sampleQuote : quickFixData) {
                            fixData[i++] = quickFixSerializer.serialize(sampleQuote);
                        }
                    }
                });
                measure("Deserialize with FixB/Native", laps, new Runnable() {
                    @Override
                    public void run() {
                        for (String sampleQuote : fixData) {
                            nativeSerializer.deserialize(sampleQuote);
                        }
                    }
                });
                measure("Deserialize with FixB/QuickFixJ", laps, new Runnable() {
                    @Override
                    public void run() {
                        for (String sampleQuote : fixData) {
                            quickFixAdapter.fromFix(quickFixSerializer.deserialize(sampleQuote));
                        }
                    }
                });
                measure("Deserialize with QuickFixJ Only", laps, new Runnable() {
                    @Override
                    public void run() {
                        for (String sampleQuote : fixData) {
                            quickFixSerializer.deserialize(sampleQuote);
                        }
                    }
                });
                System.out.println("----------");
            }
        });

    }

    private SampleQuote[] generateData(int n) {
        final SampleQuote[] data = new SampleQuote[n];
        for (int i = 0; i < n; i++) {
            data[i] = message(i);
        }
        return data;
    }

    private SampleQuote message(int i) {
        Random rnd = new Random();
        return new SampleQuote(
                UUID.randomUUID().toString(),
                rnd.nextBoolean() ? BUY : SELL,
                UUID.randomUUID().toString().substring(0, 3),
                asList(i, 5 * i, 10 * i, 50 * i, 100 * i, 1234 * i),
                asList(new TestModels.Params(i + "param1", i + "-param2"), new TestModels.Params(i + "param3", i + "param4")),
                new TestModels.Params(i + "single param1", i + "single param2"));
    }

    public static void measure(String name, int laps, Runnable task) {
        long start = currentTimeMillis();
        for (int i = 0; i < laps; i++) {
            task.run();
        }
        long ms = currentTimeMillis() - start;
        out.println(laps + " laps of " + name + " took \t" + ms + " ms (average " + (ms / laps) + " ms)");
    }
}
