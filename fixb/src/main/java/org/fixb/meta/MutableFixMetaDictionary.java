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

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * A mutable implementation of FixMetaDictionary.
 *
 * @author vladyslav.yatsenko
 */
public final class MutableFixMetaDictionary implements FixMetaDictionary {
    private final Map<Class<?>, FixBlockMeta<?>> componentMetas = new HashMap<>();
    private final Map<String, FixMessageMeta<?>> messageMetas = new HashMap<>();
    private final Map<Class<?>, FixEnumMeta<?>> enumMetas = new HashMap<>();

    @Override
    public Collection<FixMessageMeta<?>> getAllMessageMetas() {
        return messageMetas.values();
    }

    @Override
    public <T> FixMessageMeta<T> getMetaForClass(Class<T> type) {
        final FixBlockMeta<T> meta = getOrCreateComponentMeta(type);

        if (!(meta instanceof FixMessageMeta)) {
            throw new IllegalStateException(format("No FixMessageMeta found for class %s", type));
        }

        return (FixMessageMeta<T>) meta;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> FixMessageMeta<T> getMetaForMessageType(String fixMessageType) {
        final FixMessageMeta<?> meta = messageMetas.get(fixMessageType);
        if (meta == null) {
            throw new IllegalStateException("No meta for message type [" + fixMessageType + "] found. Probably it was not added to dictionary.");
        }
        return (FixMessageMeta<T>) meta;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> FixEnumMeta<T> getFixEnumMeta(Class<T> enumType) {
        return (FixEnumMeta<T>) enumMetas.get(enumType);
    }

    @Override
    public boolean hasFixEnumMeta(Class<?> enumType) {
        return enumMetas.containsKey(enumType);
    }

    FixEnumDictionary addMeta(final FixEnumMeta<?> newMeta) {
        Preconditions.checkArgument(!hasFixEnumMeta(newMeta.getType()),
                "Meta for type [%s] has already been registered! Each type should only be registered exactly once.",
                newMeta.getType());
        enumMetas.put(newMeta.getType(), newMeta);
        return this;
    }

    FixMetaDictionary addMeta(final FixBlockMeta<?> newMeta) {
        Preconditions.checkNotNull(newMeta, "newMeta");

        Preconditions.checkArgument(!containsMeta(newMeta.getType()),
                "Meta for type [%s] has already been registered! Each type should only be registered exactly once.",
                newMeta.getType());
        componentMetas.put(newMeta.getType(), newMeta);

        if (newMeta instanceof FixMessageMeta) {
            final FixMessageMeta messageMeta = (FixMessageMeta) newMeta;
            Preconditions.checkArgument(!containsMeta(messageMeta.getMessageType()),
                    "Meta for FIX message type [%s] has already been registered! Each type should only be registered exactly once. Received meta for type [%s]",
                    messageMeta.getMessageType(), newMeta.getType());
            messageMetas.put(messageMeta.getMessageType(), messageMeta);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    <T> FixBlockMeta<T> getOrCreateComponentMeta(Class<T> type) {
        if (componentMetas.containsKey(type)) {
            return (FixBlockMeta<T>) componentMetas.get(type);
        }

        final FixBlockMeta<T> newMeta = FixMetaScanner.scanClass(type, this);
        addMeta(newMeta);
        return newMeta;
    }

    boolean containsMeta(Class<?> type) {
        return componentMetas.containsKey(type);
    }

    boolean containsMeta(String fixMessageType) {
        return messageMetas.containsKey(fixMessageType);
    }

    @SuppressWarnings("unchecked")
    <T> FixBlockMeta<T> getComponentMeta(Class<T> type) {
        final FixBlockMeta<T> meta = (FixBlockMeta<T>) componentMetas.get(type);
        if (meta == null) {
            throw new IllegalStateException("No meta for class [" + type + "] found. Probably it was not added to dictionary.");
        }
        return meta;
    }

}
