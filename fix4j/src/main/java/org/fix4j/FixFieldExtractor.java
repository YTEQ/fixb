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

package org.fix4j;

import org.fix4j.meta.FixBlockMeta;

import java.util.Collection;

/**
 * I am an extractor of FIX values and groups (group corresponds to a collection) by tag from a given FIX message.
 *
 * @param <M> the type of FIX message objects
 * @author vladyslav.yatsenko
 */
public interface FixFieldExtractor<M> {

    /**
     * Reads a tag of the FIX field identified by the given tag. If optional is true then in case of not found field
     * null will be returned, otherwise should throw a FixException.
     *
     * @param fixMessage a FIX message object
     * @param type       a type to return
     * @param tag        a FIX field tag to read the tag from
     * @param optional   determines whether a not found field should generate an exception
     * @param <T>        the type of the result
     * @throws FixException
     */
    <T> T getFieldValue(M fixMessage, Class<T> type, int tag, boolean optional);

    /**
     * Reads all repeating groups from the given FIX message identified by the given tag. If optional is true then in
     * case of
     * non found groups null will be returned, otherwise should throw a FixException.
     *
     * @param fixMessage  the FIX message object
     * @param type        the collection type to return
     * @param groupTag    the FIX group tag to read from
     * @param elementType type of the resulting collection elements (has the same meaning as the type parameter in
     *                    getFieldValue)
     * @param elementTag  the group delimiter tag (or the tag of a single element)
     * @param optional    determines whether a not found group should generate an exception
     * @param <T>         type or the resulting collection elements
     */
    <T, C extends Collection<T>> C getGroups(M fixMessage,
                                             Class<C> type,
                                             int groupTag,
                                             Class<T> elementType,
                                             int elementTag,
                                             boolean optional);

    /**
     * Reads all repeating block groups from the given FIX message identified by the given tag. If optional is true
     * then in case of
     * non found groups null will be returned, otherwise should throw a FixException.
     *
     * @param fixMessage    the FIX message object
     * @param type          the collection type to return
     * @param groupTag      the FIX group tag to read from
     * @param componentMeta the FIX mapping metadata for a single repeating group
     * @param optional      determines whether a not found group should generate an exception
     * @param <T>           type or the resulting collection elements
     */
    <T, C extends Collection<T>> C getGroups(M fixMessage,
                                             Class<C> type,
                                             int groupTag,
                                             FixBlockMeta<T> componentMeta,
                                             boolean optional);
}
