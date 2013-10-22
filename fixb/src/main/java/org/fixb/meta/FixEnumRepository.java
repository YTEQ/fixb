package org.fixb.meta;

/**
 * Created with IntelliJ IDEA.
 * User: vyatsenko
 * Date: 22/10/2013
 * Time: 02:11
 * To change this template use File | Settings | File Templates.
 */
public interface FixEnumRepository {
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
