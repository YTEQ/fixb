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

import java.util.List;

/**
 * A FIX message metadata that describes mapping between a Java class and a FIX message. It extends FixBlockMeta by
 * adding a FIX message type that an associated class is bound to.
 *
 * @author vladyslav.yatsenko
 */
public class FixMessageMeta<T> extends FixBlockMeta<T> {
    private final String messageType;

    /**
     * The same as the alternative constructor, but defaults to non-constructor based object instantiation.
     * @param type a Java the class to map
     * @param messageType a code of the type of the FIX messages covered by this mapping meta
     * @param fields the mappings of individual class fields to FIX tags.
     */
    public FixMessageMeta(Class<T> type, String messageType, List<FixFieldMeta> fields) {
        this(type, messageType, fields, false);
    }

    /**
     * @param type a Java the class to map
     * @param messageType a code of the type of the FIX messages covered by this mapping meta
     * @param fields the mappings of individual class fields to FIX tags.
     * @param useConstructor whether to use constructor for object instantiation (it's faster, but requires all constructor parameters be mapped to FIX tags)
     */
    public FixMessageMeta(Class<T> type, String messageType, List<FixFieldMeta> fields, boolean useConstructor) {
        super(type, fields, useConstructor);
        this.messageType = messageType;
    }

    /**
     * @return the FIX message type.
     */
    public String getMessageType() {
        return messageType;
    }
}
