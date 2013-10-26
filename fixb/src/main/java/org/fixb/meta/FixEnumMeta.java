package org.fixb.meta;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.fixb.FixException;
import org.fixb.annotations.FixValue;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * A metadata for a FIX enum binding.
 *
 * @author vladyslav.yatsenko
 * @see FixEnumDictionary
 */
public class FixEnumMeta<T extends Enum<T>> {
    private final Class<T> enumClass;
    private final BiMap<String, T> enumFixValues;

    public static <T extends Enum<T>> FixEnumMeta<T> forClass(Class<T> type) {
        if (!type.isEnum()) {
            throw new FixException("Expected an enum class, but got [" + type.getName() + "].");
        }
        return new FixEnumMeta<>(type);
    }

    private FixEnumMeta(Class<T> enumType) {
        try {
            final ImmutableBiMap.Builder<String, T> mapBuilder = ImmutableBiMap.builder();

            for (Field field : enumType.getFields()) {
                if (field.isEnumConstant()) {
                    FixValue fixValue = field.getAnnotation(FixValue.class);
                    if (fixValue == null) {
                        throw new FixException("Not all enum values of [" + enumType.getName() + "] have @FixValue annotation: " + field.get(null));
                    }
                    mapBuilder.put(fixValue.value(), (T) field.get(null));
                }
            }

            this.enumClass = enumType;
            this.enumFixValues = mapBuilder.build();

        } catch (IllegalAccessException e) {
            throw new FixException("Invalid FIX enum mapping", e);
        }
    }

    public Class<T> getType() {
        return enumClass;
    }

    public Enum<T> enumForFixValue(String fixValue) {
        return enumFixValues.get(fixValue);
    }

    public String fixValueForEnum(Enum<?> enumValue) {
        return enumFixValues.inverse().get(enumValue);
    }
}
