package org.fixb.meta;

/**
 * A dictionary of FIX enum bindings.
 *
 * @author vladyslav.yatsenko
 * @see FixEnumMeta
 */
public interface FixEnumDictionary {
    /**
     * @param enumType the enum type
     * @param <T>      the enum type
     * @return FIX enum value bindings for the given enum type.
     */
    <T extends Enum<T>> FixEnumMeta<T> getFixEnumMeta(Class<T> enumType);

    /**
     * @param enumType the enum type
     * @return true if there is a meta object for the given enum class in this repository, false otherwise.
     */
    boolean hasFixEnumMeta(Class<?> enumType);
}
