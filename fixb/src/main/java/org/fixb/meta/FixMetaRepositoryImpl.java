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
import org.fixb.annotations.FixEnum;
import org.fixb.annotations.FixMessage;
import org.reflections.Reflections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;

/**
 * I am a repository of FIX metadata. I provide lookup methods to find FIX mapping metadata by Java object type or by FIX
 * message type.
 *
 * @author vladyslav.yatsenko
 */
public final class FixMetaRepositoryImpl implements FixMetaRepository {
    private final ConcurrentMap<Class<?>, FixBlockMeta<?>> componentMetas = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FixMessageMeta<?>> messageMetas = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, FixEnumMeta<?>> enumMetas = new ConcurrentHashMap<>();

    public FixMetaRepositoryImpl() {
    }

    public FixMetaRepositoryImpl(final String packageName) {
        addPackage(packageName);
    }

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
        return (FixMessageMeta<T>) getMessageMeta(fixMessageType);
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

    @Override
    public FixEnumRepository addPackage(String packageName) {
        try {
            for (Class<?> type : getClasses(packageName)) {
                if (type.isEnum()) {
                    FixEnumMeta<? extends Enum> enumMeta = FixEnumMeta.forEnumClass((Class<? extends Enum>) type);
                    addMeta(enumMeta);
                } else {
                    getOrCreateComponentMeta(type);
                }
            }
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Error registering classes in package " + packageName + ": " + e.getMessage(), e);
        }
    }

    public FixEnumRepository addMeta(final FixEnumMeta<?> newMeta) {
        FixEnumMeta<?> prevMeta = enumMetas.putIfAbsent(newMeta.getType(), newMeta);
        Preconditions.checkArgument(prevMeta == null,
                "Meta for type [%s] has already been registered! Each type should only be registered exactly once.",
                newMeta.getType());
        return this;
    }

    @Override
    public FixEnumRepository addMeta(final FixBlockMeta<?> newMeta) {
        Preconditions.checkNotNull(newMeta, "newMeta");

        final FixBlockMeta<?> prevMeta = componentMetas.putIfAbsent(newMeta.getType(), newMeta);
        Preconditions.checkArgument(prevMeta == null,
                "Meta for type [%s] has already been registered! Each type should only be registered exactly once.",
                newMeta.getType());

        if (newMeta instanceof FixMessageMeta) {
            final FixMessageMeta messageMeta = (FixMessageMeta) newMeta;
            FixMessageMeta<?> prevMsgMeta = messageMetas.putIfAbsent(messageMeta.getMessageType(), messageMeta);
            Preconditions.checkArgument(prevMsgMeta == null,
                    "Meta for FIX message type [%s] has already been registered! Each type should only be registered exactly once. Received meta for type [%s]",
                    messageMeta.getMessageType(), newMeta.getType());

        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> FixBlockMeta<T> getOrCreateComponentMeta(Class<T> type) {
        if (componentMetas.containsKey(type)) {
            return (FixBlockMeta<T>) componentMetas.get(type);
        }

        final FixBlockMeta<T> newMeta = FixMetaScanner.scanClass(type, this);
        addMeta(newMeta);
        return newMeta;
    }

    @Override
    public boolean containsMeta(Class<?> type) {
        return componentMetas.containsKey(type);
    }

    @Override
    public boolean containsMeta(String fixMessageType) {
        return messageMetas.containsKey(fixMessageType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> FixBlockMeta<T> getComponentMeta(Class<T> type) {
        final FixBlockMeta<T> meta = (FixBlockMeta<T>) componentMetas.get(type);
        if (meta == null) {
            throw new IllegalStateException("No meta for class [" + type + "] found. Probably it was not added to repository.");
        }
        return meta;
    }

    public FixMessageMeta<?> getMessageMeta(String fixMessageType) {
        final FixMessageMeta<?> meta = messageMetas.get(fixMessageType);
        if (meta == null) {
            throw new IllegalStateException("No meta for message type [" + fixMessageType + "] found. Probably it was not added to repository.");
        }
        return meta;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName the base package
     * @return All found classes with FIX bindings.
     */
    private static Set<Class<?>> getClasses(final String packageName) {
        final Reflections reflections = new Reflections(packageName);
        final Set<Class<?>> allClasses = new HashSet<>();
        allClasses.addAll(reflections.getTypesAnnotatedWith(FixMessage.class));
        allClasses.addAll(reflections.getTypesAnnotatedWith(FixEnum.class));

        // Process classes in constant order
        final TreeSet<Class<?>> sortedClasses = new TreeSet<>(new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        sortedClasses.addAll(allClasses);

        return sortedClasses;
    }
}
