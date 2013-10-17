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
 * Created with IntelliJ IDEA.
 * User: vyatsenko
 * Date: 13/10/2013
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public interface FixMetaRepository {
    Collection<FixMessageMeta<?>> getAllMessageMetas();

    <T> FixMessageMeta<T> getMetaForClass(Class<T> type);

    @SuppressWarnings("unchecked")
    <T> FixMessageMeta<T> getMetaForMessageType(String fixMessageType);

    FixMetaRepository addPackage(String packageName);

    FixBlockMeta<?> addMeta(FixBlockMeta<?> newMeta);

    boolean containsMeta(Class<?> type);

    boolean containsMeta(String fixMessageType);

    FixBlockMeta<?> getMeta(Class<?> type);

    FixMessageMeta<?> getMeta(String fixMessageType);

    FixBlockMeta<?> getOrCreateMeta(Class<?> type);
}
