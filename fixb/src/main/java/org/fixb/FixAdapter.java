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
 * An interface for FIX model adapter between FIX-mapped classes and FIX message representations, including ones
 * supported by third-party libraries.
 *
 * @param <T> the type of FIX-mapped domain objects (a FixAdapter instance per T)
 * @param <M> the type of FIX message objects (specific for the underlying FIX protocol implementation,
 *            e.g. quickfix.Message for QuickFix/J)
 * @author vladyslav.yatsenko
 */
public interface FixAdapter<T, M> {
    /**
     * Converts the given library specific FIX message object into a domain object.
     *
     * @param fixMessage the FIX message object specific to the underlying FIX library
     * @return a domain object represented by the given FIX message object.
     */
    T fromFix(M fixMessage);

    /**
     * Converts the given domain object into a FIX message object specific.
     *
     * @param data a domain object (a FIX-enabled POJO)
     * @return a FIX message specific to the underlying FIX protocol implementation.
     */
    M toFix(T data);
}
