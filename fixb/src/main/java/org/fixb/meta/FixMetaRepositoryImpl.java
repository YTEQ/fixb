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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
    private final ConcurrentMap<Class<?>, FixBlockMeta<?>> allMetas = new ConcurrentHashMap<Class<?>, FixBlockMeta<?>>();
    private final ConcurrentMap<String, FixMessageMeta<?>> messageMetas = new ConcurrentHashMap<String, FixMessageMeta<?>>();

    public FixMetaRepositoryImpl() {
    }

    public FixMetaRepositoryImpl(final String packageName) {
        addPackage(packageName);
    }

    /**
     * @return all FixMessageMetas registered with the FixMetaRepositoryImpl singleton
     */
    @Override
    public Collection<FixMessageMeta<?>> getAllMessageMetas() {
        return messageMetas.values();
    }

    /**
     * @return a FixMessageMeta for the given class. If meta has not been previously registered with this repository
     *         it will be collected from the given class definition using {@link FixMetaScanner}.
     * @throws IllegalStateException if no meta instance found.
     * @see FixMetaScanner
     */
    @Override
    public <T> FixMessageMeta<T> getMetaForClass(Class<T> type) {
        final FixBlockMeta<?> meta = getOrCreateMeta(type);

        if (!(meta instanceof FixMessageMeta)) {
            throw new IllegalStateException(format("No FixMessageMeta found for class %s", type));
        }

        return (FixMessageMeta<T>) meta;
    }

    /**
     * @return a FixMessageMeta for the given FIX message type.
     * @throws IllegalStateException if no meta instance found.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> FixMessageMeta<T> getMetaForMessageType(String fixMessageType) {
        return (FixMessageMeta<T>) getMeta(fixMessageType);
    }

    /**
     * Scans the given package for classes annotated with @FixMessage and adds them to repository.
     *
     * @param packageName a name of the package containing FIX mapped classes
     */
    @Override
    public FixMetaRepository addPackage(String packageName) {
        try {
            for (Class<?> type : getClasses(packageName)) {
                getOrCreateMeta(type);
            }
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("Error registering classes in package " + packageName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public FixBlockMeta<?> addMeta(final FixBlockMeta<?> newMeta) {
        Preconditions.checkNotNull(newMeta, "newMeta");

        final FixBlockMeta<?> prevMeta = allMetas.putIfAbsent(newMeta.getType(), newMeta);
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

        return newMeta;
    }

    @Override
    public FixBlockMeta<?> getOrCreateMeta(Class<?> type) {
        if (allMetas.containsKey(type)) {
            return allMetas.get(type);
        }

        return addMeta(FixMetaScanner.scanClass(type, this));
    }

    @Override
    public boolean containsMeta(Class<?> type) {
        return allMetas.containsKey(type);
    }

    @Override
    public boolean containsMeta(String fixMessageType) {
        return messageMetas.containsKey(fixMessageType);
    }

    @Override
    public FixBlockMeta<?> getMeta(Class<?> type) {
        final FixBlockMeta<?> meta = allMetas.get(type);
        if (meta == null) {
            throw new IllegalStateException("No meta for class ["
                    + type
                    + "] found. Probably it was not added to repository.");
        }
        return meta;
    }

    @Override
    public FixMessageMeta<?> getMeta(String fixMessageType) {
        final FixMessageMeta<?> meta = messageMetas.get(fixMessageType);
        if (meta == null) {
            throw new IllegalStateException("No meta for message type ["
                    + fixMessageType
                    + "] found. Probably it was not added to repository.");
        }
        return meta;
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     */
    private static Set<Class<?>> getClasses(final String packageName) {
        final Reflections reflections = new Reflections(packageName);
        final Set<Class<?>> allClasses = ImmutableSet.copyOf(reflections.getTypesAnnotatedWith(FixMessage.class));

        /*
        * Sort the set by type name because otherwise while running this, the ordering of how
        * the classes are loaded into the FixMetaRepositoryImpl will affect when a malformed FixMessage
        * will be processed and therefore at what point in the code the error will be observed;
        * suggesting this observation to be a race-condition, but is actually caused by the order
        * Reflections returns found classes.
        */
        List<Class<?>> toSort = Lists.newArrayList(allClasses);
        Collections.sort(toSort, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                Preconditions.checkNotNull(o1, "o1");
                Preconditions.checkNotNull(o2, "o2");
                return o1.getName().compareTo(o2.getName());
            }
        });

        final Set<Class<?>> sortedClasses = ImmutableSet.copyOf(toSort);
        return sortedClasses;
    }
}
