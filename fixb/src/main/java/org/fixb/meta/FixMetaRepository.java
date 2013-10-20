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
 * I am a repository of FIX metadata. I provide lookup methods to find FIX mapping metadata by Java object type or by FIX
 * message type.
 *
 * @author vladyslav.yatsenko
 */
public interface FixMetaRepository {
    /**
     * @return all FixMessageMetas registered with the FixMetaRepositoryImpl singleton
     */
    Collection<FixMessageMeta<?>> getAllMessageMetas();

    /**
     * @return a FixMessageMeta for the given class. If meta has not been previously registered with this repository
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

    /**
     * Scans the given package for classes annotated with @FixMessage and adds them to repository.
     *
     * @param packageName a name of the package containing FIX mapped classes
     */
    FixMetaRepository addPackage(String packageName);

    FixMetaRepository addMeta(FixBlockMeta<?> newMeta);

    boolean containsMeta(Class<?> type);

    boolean containsMeta(String fixMessageType);

    <T> FixBlockMeta<T> getMeta(Class<T> type);

    <T> FixBlockMeta<T> getOrCreateMeta(Class<T> type);
}
