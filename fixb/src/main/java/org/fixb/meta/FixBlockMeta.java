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
import org.fixb.FixException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static java.util.Collections.unmodifiableList;


/**
 * I represent FIX block metadata (a set of FIX fields and repeating groups).
 *
 * @param <T>
 * @author vladyslav.yatsenko
 */
public class FixBlockMeta<T> {
    private final Class<T> type;
    private final Optional<Constructor<T>> constructor;
    private final List<FixFieldMeta> fields;

    /**
     * The same as FixBlockMeta, but with useConstructor default to false.
     *
     * @param type   the domain object type this FixBlockMeta is for
     * @param fields the fields metadata
     */
    public FixBlockMeta(Class<T> type, List<FixFieldMeta> fields) {
        this(type, fields, false);
    }

    /**
     * @param type           the domain object type this FixBlockMeta is for
     * @param fields         the fields metadata
     * @param useConstructor identifies whether to use constructor for instance initialisations
     */
    @SuppressWarnings("unchecked")
    public FixBlockMeta(Class<T> type, List<FixFieldMeta> fields, boolean useConstructor) {
        this.type = type;
        this.constructor = useConstructor ? Optional.of((Constructor<T>) type.getConstructors()[0]) : Optional.<Constructor<T>>absent();
        this.fields = unmodifiableList(fields);
    }

    /**
     * @return the related domain object type.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return the fields metadata.
     */
    public List<FixFieldMeta> getFields() {
        return fields;
    }

    /**
     * @param values constructor parameter values
     * @return a domain object created using given parameter values.
     */
    public T createModel(Map<FixFieldMeta, Object> values) {
        try {
            return (constructor.isPresent()) ?
                    createModel(constructor.get(), values, 0) :
                    createModel(type, values, 0);
        } catch (Exception e) {
            throw new FixException("Unable to create object from FIX parameters: " + values.values(), e);
        }
    }

    private <T> T createModel(Constructor<T> constr, Map<FixFieldMeta, Object> values, int level) throws Exception {
        final List<Object> params = new ArrayList<Object>(constr.getParameterTypes().length);

        final FixFieldMeta[] keys = new FixFieldMeta[values.keySet().size()];
        values.keySet().toArray(keys);

        for (int i = 0; i < keys.length; i++) {
            FixFieldMeta meta = keys[i];
            Object value = values.get(meta);
            if (meta instanceof FixDynamicFieldMeta) {
                FixDynamicFieldMeta df = (FixDynamicFieldMeta) meta;
                if (df.getPath().length == 1 + level) {
                    params.add(value);
                } else {
                    Field rootField = df.getPath()[level];
                    Map<FixFieldMeta, Object> componentValues = new LinkedHashMap<FixFieldMeta, Object>();
                    while (df != null && df.getPath()[level] == rootField) {
                        componentValues.put(df, value);
                        if (i < keys.length) {
                            i++;
                            if (keys.length > i) {
                                df = (FixDynamicFieldMeta) keys[i];
                                value = values.get(df);
                            } else {
                                df = null;
                            }
                        } else {
                            df = null;
                        }

                    }
                    params.add(createModel(rootField.getType().getConstructors()[0], componentValues, level + 1));

                    if (df != null) {
                        i--;
                    }
                }
            }
        }

        return constr.newInstance(params.toArray());
    }

    private <T> T createModel(Class<T> clazz, Map<FixFieldMeta, Object> values, int level) throws Exception {
        Map<Field, Object> params = new HashMap<Field, Object>(clazz.getDeclaredFields().length);

        FixFieldMeta[] keys = new FixFieldMeta[values.keySet().size()];
        values.keySet().toArray(keys);

        for (int i = 0; i < keys.length; i++) {
            FixFieldMeta meta = keys[i];
            Object value = values.get(meta);
            if (meta instanceof FixDynamicFieldMeta) {
                FixDynamicFieldMeta df = (FixDynamicFieldMeta) meta;
                if (df.getPath().length == 1 + level) {
                    params.put(df.getPath()[level], value);
                } else {
                    Field rootField = df.getPath()[level];
                    Map<FixFieldMeta, Object> componentValues = new LinkedHashMap<FixFieldMeta, Object>();
                    while (df != null && df.getPath()[level] == rootField) {
                        componentValues.put(df, value);
                        if (i < keys.length) {
                            i++;
                            if (keys.length > i) {
                                df = (FixDynamicFieldMeta) keys[i];
                                value = values.get(df);
                            } else {
                                df = null;
                            }
                        } else {
                            df = null;
                        }

                    }
                    params.put(rootField, createModel(rootField.getType().getConstructors()[0], componentValues, level + 1));

                    if (df != null) {
                        i--;
                    }
                }
            }
        }

        return instantiate(clazz, params);
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(Class<T> cls, Map<Field, ?> args) throws Exception {
        // Create instance of the given class
        final Constructor<T> constr = (Constructor<T>) cls.getDeclaredConstructors()[0];
        final List<Object> params = new ArrayList<Object>();
        if (constr.getParameterTypes().length > 0) {
            for (Class<?> pType : constr.getParameterTypes()) {
                params.add(defaultValue(pType));
            }
        }
        final T instance = constr.newInstance(params.toArray());
        // Set separate fields
        for (Map.Entry<Field, ?> pair : args.entrySet()) {
            Field field = pair.getKey();
            field.setAccessible(true);
            field.set(instance, pair.getValue());
        }

        return instance;
    }

    private static Object defaultValue(Class<?> clazz) {
        if (clazz == int.class) return 0;
        else if (clazz == boolean.class) return false;
        else if (clazz == char.class) return (char) 0;
        else if (clazz == byte.class) return (byte) 0;
        else if (clazz == short.class) return (short) 0;
        else if (clazz == long.class) return (long) 0;
        else if (clazz == float.class) return 0f;
        else if (clazz == double.class) return 0d;
        else return null;
    }
}
