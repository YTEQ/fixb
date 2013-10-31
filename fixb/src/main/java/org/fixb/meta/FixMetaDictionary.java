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

import java.util.Collection;

/**
 * A dictionary of FIX metadata. It provides lookup methods for FIX mapping metadata by Java class or by FIX
 * message type.
 *
 * @author vladyslav.yatsenko
 */
public interface FixMetaDictionary extends FixEnumDictionary {
    /**
     * @return all FixMessageMetas registered with the FixMetaDictionary singleton
     */
    Collection<FixMessageMeta<?>> getAllMessageMetas();

    /**
     * @return a FixMessageMeta for the given class. If meta has not been previously registered with this dictionary
     *         it will be collected from the given class definition using {@link FixMetaScanner}.
     * @throws IllegalStateException if no meta instance found.
     * @see FixMetaScanner
     */
    <T> FixMessageMeta<T> getMetaForClass(Class<T> type);

    /**
     * @return a FixMessageMeta for the given FIX message type.
     * @throws IllegalStateException if no meta instance found.
     */
    <T> FixMessageMeta<T> getMetaForMessageType(String fixMessageType);
}
