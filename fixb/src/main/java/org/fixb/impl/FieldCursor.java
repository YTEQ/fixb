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

import java.util.HashMap;
import java.util.Map;

import static org.fixb.impl.FormatConstants.SOH;

/**
 * Can extract separate tag values from a FIX message string while keeping track of the read position in it. It is used
 * in the implementation of NativeFixFieldExtractor.
 *
 * @author vladyslav.yatsenko
 */
public class FieldCursor {
    private Map<Integer, String> cache = new HashMap<Integer, String>();
    private int lastPosition = -1;
    private int lastTag;

    final String fixMessage;
    String lastValue;

    /**
     * A static factory method.
     *
     * @param fixMessage a string representing FIX message
     * @return a new instance of FieldCursor based on the given FIX message.
     */
    public static FieldCursor create(String fixMessage) {
        return new FieldCursor(fixMessage);
    }

    /**
     * @return the last read FIX field value.
     */
    public String lastValue() {
        return lastValue;
    }

    /**
     * @return the last read FIX tag.
     */
    public int lastTag() {
        return lastTag;
    }

    /**
     * Reads the next FIX field in the message and updates the last read field tag and value if available.
     *
     * @return <code>true</code> if next field could be read, <code>false</code> if reached the end of message.
     */
    public boolean nextField() {
        final int start = lastPosition == -1 ? 0 : lastPosition;
        final int interim = fixMessage.indexOf('=', start);

        if (interim == -1) {
            lastTag = 0;
            lastValue = null;
            lastPosition = fixMessage.length();
            return false;
        }

        final int end = fixMessage.indexOf(SOH, interim + 1);

        lastTag = Integer.valueOf(fixMessage.substring(start, interim));
        lastValue = (end > -1) ? fixMessage.substring(interim + 1, end) : fixMessage.substring(interim + 1);
        lastPosition = (end > -1) ? end + 1 : fixMessage.length();

        return true;
    }

    /**
     * Reads the next FIX field with the given tag in the message and updates the last read field tag and value if found.
     *
     * @return <code>true</code> if the field was found, <code>false</code> if reached the end of message.
     */
    public boolean nextField(int tag) {
        final String value = cache.get(tag);
        if (value != null) {
            lastTag = tag;
            lastValue = value;
            cache.remove(tag);
            return true;
        } else {
            while (nextField()) {
                if (lastTag == tag) {
                    return true;
                } else {
                    cache.put(lastTag, lastValue);
                }
            }
        }
        return false;
    }

    /**
     * A minimal constructor.
     *
     * @param fixMessage a string representing FIX message.
     */
    private FieldCursor(String fixMessage) {
        this.fixMessage = fixMessage;
    }
}
