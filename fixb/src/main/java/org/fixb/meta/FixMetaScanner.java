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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.fixb.FixException;
import org.fixb.annotations.*;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.fixb.FixConstants.MSG_TYPE_TAG;
import static org.fixb.meta.FixFieldMeta.fixFieldMeta;
import static org.fixb.meta.FixFieldMeta.fixGroupMeta;

/**
 * Scans Java classes annotated with FixB annotations and builds FIX bindings metadata dictionary.
 * The MutableFixMetaDictionary is used to optimise the scanning process allowing each class to be scanned only once
 * i.e. some classes appear as components of other classes.
 *
 * @author vladyslav.yatsenko
 */
public class FixMetaScanner {

    /**
     * Scans the given packages for classes annotated with @FixMessage and @FixEnum and adds them to the resulting repository.
     *
     * @param packageNames a name of the package containing FIX mapped classes
     */
    public static FixMetaDictionary scanClassesIn(String... packageNames) {
        final MutableFixMetaDictionary repository = new MutableFixMetaDictionary();
        for (String packageName : packageNames) {
            try {
                for (Class<?> type : getClasses(packageName)) {
                    if (type.isEnum()) {
                        repository.addMeta(FixEnumMeta.forClass((Class<? extends Enum>) type));
                    } else {
                        repository.addMeta(scanClass(type, repository));
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Error registering classes in package " + packageName + ": " + e.getMessage(), e);
            }
        }
        return repository;
    }

    /**
     * Scans the given class for FIX mapping annotations.
     *
     * @param model the domain class to scan
     * @param <T>   the domain type to scan
     * @return a FixBlockMeta for the given type.
     * @throws FixException if the given class is not properly annotated.
     */
    public static <T> FixBlockMeta<T> scanClass(Class<T> model) {
        return scanClassAndAddToRepository(model, new MutableFixMetaDictionary());
    }

    static <T> FixBlockMeta<T> scanClassAndAddToRepository(Class<T> model, MutableFixMetaDictionary repository) {
        Preconditions.checkNotNull(model, "model");
        Preconditions.checkNotNull(model, "repository");
        if (repository.containsMeta(model)) {
            return repository.getComponentMeta(model);
        }

        final FixBlockMeta<T> result = scanClass(model, repository);
        repository.addMeta(result);
        return result;
    }

    static <T> FixBlockMeta<T> scanClass(Class<T> model, MutableFixMetaDictionary repository) {
        if (repository.containsMeta(model)) {
            return repository.getComponentMeta(model);
        }

        final FixBlockMeta<T> result;

        if (model.getConstructors().length == 0) {
            throw new FixException("Class [" + model.getName() + "] does not provide a public constructor.");
        }

        @SuppressWarnings("unchecked")
        Optional<Constructor<T>> constructor = Optional.of((Constructor<T>) model.getConstructors()[0]);

        final int c = numberOfFixParameters(constructor.get());

        if (c == 0) {
            constructor = Optional.absent();
        } else if (c != constructor.get().getParameterTypes().length) {
            throw new FixException("Some constructor parameters don't have FIX mapping in class [" + model.getName() + "].");
        }

        if (model.isAnnotationPresent(FixMessage.class)) {
            final FixMessage messageAnnotation = model.getAnnotation(FixMessage.class);
            ImmutableList.Builder<FixFieldMeta> allFieldsBuilder = ImmutableList.builder();
            allFieldsBuilder.addAll(processConstantFields(messageAnnotation)); // add constant fields
            allFieldsBuilder.addAll(processFields(model, constructor, repository)); // add all other fields
            result = new FixMessageMeta<>(model, messageAnnotation.type(), allFieldsBuilder.build(), constructor.isPresent());
        } else if (model.isAnnotationPresent(FixBlock.class)) {
            final List<FixFieldMeta> fixFields = processFields(model, constructor, repository);
            result = new FixBlockMeta<>(model, fixFields, constructor.isPresent());
        } else {
            throw new FixException("Neither @FixBlock nor @FixMessage annotation present on class [" + model.getName() + "].");
        }

        return result;
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

    private static int numberOfFixParameters(Constructor<?> constructor) {
        int fixAnnotationCount = 0;
        for (Annotation[] annotations : constructor.getParameterAnnotations()) {
            if (hasFixParamAnnotation(annotations)) {
                fixAnnotationCount++;
            }
        }
        return fixAnnotationCount;
    }

    private static boolean hasFixParamAnnotation(Annotation[] annotations) {
        if (annotations.length == 0) {
            return false;
        }
        final Set<Class<? extends Annotation>> fixAnnotationType = new HashSet<>(asList(FixBlock.class, FixField.class, FixGroup.class));
        for (Annotation annotation : annotations) {
            if (fixAnnotationType.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    private static List<FixFieldMeta> processConstantFields(FixMessage messageAnnotation) {
        final List<FixFieldMeta> headerFields = new ArrayList<>();
        headerFields.add(fixFieldMeta(MSG_TYPE_TAG, messageAnnotation.type(), true));
        for (FixMessage.Field f : messageAnnotation.header()) {
            headerFields.add(fixFieldMeta(f.tag(), f.value(), true));
        }
        for (FixMessage.Field f : messageAnnotation.body()) {
            headerFields.add(fixFieldMeta(f.tag(), f.value(), false));
        }
        return headerFields;
    }

    private static <T> List<FixFieldMeta> processFields(final Class<T> model,
                                                        final Optional<Constructor<T>> constructor,
                                                        final MutableFixMetaDictionary repository) {
        final ImmutableMap<Integer, FixFieldMeta> fixFields = scanFields(model, repository);

        if (constructor.isPresent()) {
            final FixFieldMeta[] orderedFixFields = new FixFieldMeta[fixFields.size()];
            orderFixFields(constructor.get(), fixFields, orderedFixFields, repository, 0);
            return asList(orderedFixFields);
        } else {
            return new ArrayList<>(fixFields.values());
        }
    }

    private static int orderFixFields(Constructor<?> constructor,
                                      ImmutableMap<Integer, FixFieldMeta> fixFields,
                                      FixFieldMeta[] ordered,
                                      MutableFixMetaDictionary repository,
                                      int offset) {
        final Annotation[][] annotations = constructor.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            if (hasFixAnnotation(annotations[i], FixBlock.class)) {
                final Class<?>[] paramTypes = constructor.getParameterTypes();
                for (FixFieldMeta fixFieldMeta : repository.getComponentMeta(paramTypes[i]).getFields()) {
                    ordered[offset++] = fixFields.get(fixFieldMeta.getTag());
                }
            } else {
                final Optional<Integer> tag = getFixTagFromAnnotations(annotations[i]);
                if (!tag.isPresent()) {
                    throw new FixException("Some constructor parameters don't have FIX mapping in class ["
                            + constructor.getDeclaringClass().getName() + "].");
                }

                final FixFieldMeta fixFieldMeta = fixFields.get(tag.get());
                if (fixFieldMeta == null) {
                    throw new FixException("No field with tag [" + tag.get() + "] found, however constructor parameters exist in class ["
                            + constructor.getDeclaringClass().getName() + "].");
                }

                ordered[offset++] = fixFieldMeta;
            }
        }

        return offset;
    }

    private static boolean hasFixAnnotation(Annotation[] annotations, Class<? extends Annotation> annotationType) {
        for (Annotation x : annotations) {
            if (x.annotationType() == annotationType) {
                return true;
            }
        }
        return false;
    }

    private static Optional<Integer> getFixTagFromAnnotations(Annotation... annotations) {
        Integer tag = null;
        for (Annotation x : annotations) {
            Class<? extends Annotation> annType = x.annotationType();
            if (annType == FixField.class) {
                tag = ((FixField) x).tag();
                break;
            } else if (annType == FixGroup.class) {
                tag = ((FixGroup) x).tag();
                break;
            }
        }

        return Optional.fromNullable(tag);
    }

    private static <T> ImmutableMap<Integer, FixFieldMeta> scanFields(final Class<T> model,
                                                                      final MutableFixMetaDictionary repository,
                                                                      final Field... parentPath) {
        Map<Integer, FixFieldMeta> fixFields = Maps.newLinkedHashMap();

        for (Field f : model.getDeclaredFields()) {
            final Class<?> type = f.getType();
            final Field[] path = newPath(parentPath, f);

            for (Annotation annotation : f.getDeclaredAnnotations()) {
                if (FixField.class == annotation.annotationType()) {
                    FixField fixField = (FixField) annotation;
                    int tag = fixField.tag();
                    if (fixFields.containsKey(tag)) {
                        throw new FixException("There are more than one fields mapped with FIX tag [" + tag + "].");
                    }
                    fixFields.put(tag, fixFieldMeta(tag, fixField.header(), fixField.optional(), path));

                } else if (FixBlock.class == annotation.annotationType()) {
                    for (FixFieldMeta fixFieldMeta : scanClassAndAddToRepository(type, repository).getFields()) {
                        Field[] fieldPath = ((FixDynamicFieldMeta) fixFieldMeta).getPath();
                        Field[] newFieldPath = Arrays.copyOf(path, path.length + fieldPath.length);
                        for (int i = path.length; i < newFieldPath.length; i++) {
                            newFieldPath[i] = fieldPath[i - path.length];
                        }
                        fixFields.put(fixFieldMeta.getTag(), new FixDynamicFieldMeta(
                                fixFieldMeta.getTag(),
                                fixFieldMeta.isHeader(),
                                fixFieldMeta.isOptional(),
                                newFieldPath));
                    }
                } else if (FixGroup.class == annotation.annotationType()) {
                    if (Collection.class.isAssignableFrom(type)) {
                        FixGroup fixGroup = (FixGroup) annotation;
                        if (!fixFields.containsKey(fixGroup.tag())) {
                            Class<?> componentType = getComponentType(fixGroup, f.getGenericType());
                            FixFieldMeta fieldMeta = isSimpleType(componentType) ?
                                    fixGroupMeta(fixGroup.tag(),
                                            fixGroup.header(),
                                            fixGroup.optional(),
                                            fixGroup.componentTag(),
                                            componentType,
                                            path) :
                                    fixGroupMeta(fixGroup.tag(),
                                            fixGroup.header(),
                                            fixGroup.optional(),
                                            repository.getOrCreateComponentMeta(componentType),
                                            path);
                            fixFields.put(fixGroup.tag(), fieldMeta);
                        }
                    } else {
                        throw new FixException("Only Collection can represent a FIX group: ["
                                + f.getName() + "] in class [" + model.getName() + "].");
                    }
                }
            }
        }

        final Class<? super T> superclass = model.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            fixFields.putAll(scanFields(superclass, repository));
        }

        return ImmutableMap.copyOf(fixFields);
    }

    private static Class<?> getComponentType(FixGroup annotation, Type genericType) {
        Class<?> explicitComponent = annotation.component();
        return explicitComponent == Void.class ? getElementType(genericType) : explicitComponent;
    }

    private static Class<?> getElementType(Type collectionType) {
        if (collectionType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) collectionType;
            Type[] typeArguments = pType.getActualTypeArguments();
            if (typeArguments.length == 1 && typeArguments[0] instanceof Class) {
                return (Class<?>) typeArguments[0];
            }
        }
        return Object.class;
    }

    private static Field[] newPath(Field[] parentPath, Field f) {
        Field[] path = Arrays.copyOf(parentPath, parentPath.length + 1);
        path[path.length - 1] = f;
        return path;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || SIMPLE_CLASSES.contains(type);
    }

    @SuppressWarnings("unchecked")
    private static final List<? extends Class<?>> SIMPLE_CLASSES = asList(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            String.class,
            BigDecimal.class,
            Instant.class,
            LocalDate.class,
            Enum.class);
}
