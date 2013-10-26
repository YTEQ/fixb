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

/**
 * A FIX field metadata for a statically defined tag with a value that is not associated with a class field.
 *
 * @author vladyslav.yatsenko
 */
public class FixConstantFieldMeta extends FixFieldMeta {
    private final Object value;

    /**
     * @param tag the FIX tag
     * @param header identifies whether the mapped FIX field is a part of the FIX message header
     * @param value the value
     */
    public FixConstantFieldMeta(final int tag, final boolean header, final Object value) {
        super(tag, header, false);
        this.value = value;
    }

    @Override
    public Object getValue(Object o) {
        return value;
    }

    @Override
    public Class<?> getType() {
        return value.getClass();
    }

    @Override
    public boolean isGroup() {
        return false;
    }
}
