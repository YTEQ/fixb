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

package org.fixb;

/**
 * I serialize and deserialize arbitrary objects in FIX format to and from strings representation.
 *
 * @param <M> the type of the FIX objects
 * @author vladyslav.yatsenko
 */
public interface FixSerializer<M> {
    /**
     * Serializes the given object into FIX string.
     *
     * @param message a FIX object to serialize
     * @return a generated FIX message
     */
    String serialize(M message);

    /**
     * Deserializes the given FIX string into an object of the given type.
     *
     * @param fixMessage a FIX message to read
     * @return an object of the type M populated with the values from the given FIX message.
     */
    M deserialize(String fixMessage);
}
