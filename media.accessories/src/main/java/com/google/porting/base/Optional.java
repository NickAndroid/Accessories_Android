/*
 * Copyright (C) 2011 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.porting.base;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import static com.google.porting.base.Preconditions.checkNotNull;

/**
 * @author Kurt Alfred Kluever
 * @author Kevin Bourrillion
 * @since 10.0
 */
public abstract class Optional<T> implements Serializable {
    private static final long serialVersionUID = 0;

    Optional() {
    }

    /**
     * Returns an {@code Optional} instance with no contained reference.
     * {@code Optional.empty}.
     */
    public static <T> Optional<T> absent() {
        return Absent.withType();
    }

    /**
     * Returns an {@code Optional} instance containing the given non-null reference. To have {@code
     * null} treated as {@link #absent}, use {@link #fromNullable} instead.
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> Optional<T> of(T reference) {
        return new Present<T>(checkNotNull(reference));
    }

    /**
     * If {@code nullableReference} is non-null, returns an {@code Optional} instance containing that
     * reference; otherwise returns {@link Optional#absent}.
     * {@code Optional.ofNullable}.
     */
    public static <T> Optional<T> fromNullable(@Nullable T nullableReference) {
        return (nullableReference == null)
                ? Optional.<T>absent()
                : new Present<T>(nullableReference);
    }

    /**
     * Returns the value of each present instance from the supplied {@code optionals}, in order,
     * skipping over occurrences of {@link Optional#absent}. Iterators are unmodifiable and are
     * evaluated lazily.
     * {@code Optional} class; use
     * {@code optionals.stream().filter(Optional::isPresent).map(Optional::get)} instead.
     *
     * @since 11.0 (generics widened in 13.0)
     */
    public static <T> Iterable<T> presentInstances(
            final Iterable<? extends Optional<? extends T>> optionals) {
        checkNotNull(optionals);
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new AbstractIterator<T>() {
                    private final Iterator<? extends Optional<? extends T>> iterator =
                            checkNotNull(optionals.iterator());

                    @Override
                    protected T computeNext() {
                        while (iterator.hasNext()) {
                            Optional<? extends T> optional = iterator.next();
                            if (optional.isPresent()) {
                                return optional.get();
                            }
                        }
                        return endOfData();
                    }
                };
            }
        };
    }

    /**
     * Returns {@code true} if this holder contains a (non-null) instance.
     */
    public abstract boolean isPresent();

    /**
     * Returns the contained instance, which must be present. If the instance might be
     * absent, use {@link #or(Object)} or {@link #orNull} instead.
     *
     * @throws IllegalStateException if the instance is absent ({@link #isPresent} returns
     *                               {@code false}); depending on this <i>specific</i> exception type (over the more general
     *                               {@link RuntimeException}) is discouraged
     */
    public abstract T get();

    /**
     * Returns the contained instance if it is present; {@code defaultValue} otherwise. If
     * no default value should be required because the instance is known to be present, use
     * {@link #get()} instead. For a default value of {@code null}, use {@link #orNull}.
     */
    public abstract T or(T defaultValue);

    /**
     * Returns this {@code Optional} if it has a value present; {@code secondChoice}
     * otherwise.
     * {@code Optional} class; write {@code thisOptional.isPresent() ? thisOptional : secondChoice}
     * instead.
     */
    public abstract Optional<T> or(Optional<? extends T> secondChoice);

    /**
     * Returns the contained instance if it is present; {@code supplier.get()} otherwise.
     *
     * @throws NullPointerException if this optional's value is absent and the supplier returns
     *                              {@code null}
     */
    public abstract T or(Supplier<? extends T> supplier);

    /**
     * Returns the contained instance if it is present; {@code null} otherwise. If the
     * instance is known to be present, use {@link #get()} instead.
     */
    @Nullable
    public abstract T orNull();

    /**
     * Returns an immutable singleton {@link Set} whose only element is the contained instance
     * if it is present; an empty immutable {@link Set} otherwise.
     *
     * @since 11.0
     */
    public abstract Set<T> asSet();

    /**
     * If the instance is present, it is transformed with the given {@link Function}; otherwise,
     * {@link Optional#absent} is returned.
     *
     * @throws NullPointerException if the function returns {@code null}
     * @since 12.0
     */
    public abstract <V> Optional<V> transform(Function<? super T, V> function);

    /**
     * Returns {@code true} if {@code object} is an {@code Optional} instance, and either
     * the contained references are {@linkplain Object#equals equal} to each other or both
     * are absent. Note that {@code Optional} instances of differing parameterized types can
     * be equal.
     */
    @Override
    public abstract boolean equals(@Nullable Object object);

    /**
     * Returns a hash code for this instance.
     * hash code unspecified, unlike the Java 8 equivalent.
     */
    @Override
    public abstract int hashCode();

    /**
     * Returns a string representation for this instance.
     * representation unspecified, unlike the Java 8 equivalent.
     */
    @Override
    public abstract String toString();
}
