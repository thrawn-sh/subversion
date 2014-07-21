/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * {@link Resource} defines a resource location in the repository
 */
public final class Resource implements Comparable<Resource> {

    private static final Pattern PATH_PATTERN = Pattern.compile("/");

    /**
     * Represents the base {@link Resource} in the repository
     */
    public static final Resource ROOT = new Resource("");

    /**
     * Create a new {@link Resource} instance for the given value
     *
     * @param path value of the {@link Resource}
     *
     * @return the new {@link Resource} instance with the given value
     */
    public static Resource create(final String path) {
        if (StringUtils.isEmpty(path) || "/".equals(path)) {
            return ROOT;
        }

        final StringBuilder sb = new StringBuilder();
        for (final String segment : PATH_PATTERN.split(path)) {
            if (!StringUtils.isEmpty(segment)) {
                sb.append('/');
                sb.append(segment);
            }
        }

        return new Resource(sb.toString());
    }

    private final String value;

    private Resource(final String value) {
        this.value = value;
    }

    /**
     * Appends the specified {@link Resource} to the end of this {@link Resource}
     *
     * @param resource the {@link Resource} that is appended to the end
     * of this {@link Resource}
     *
     * @return a {@link Resource} that represents the combination of this {@link Resource} and the specified {@link Resource}
     */
    public Resource append(final Resource resource) {
        return new Resource(value + resource.value);
    }

    @Override
    public int compareTo(final Resource other) {
        Validate.notNull(other, "other must not be null");
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Resource other = (Resource) obj;
        return value.equals(other.value);
    }

    /**
     * Returns the parent {@link Resource} of the {@link Resource}, the parent of the ROOT element is the ROOT itself
     *
     * @return the parent {@link Resource} of the {@link Resource}
     */
    public Resource getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }
        final int indexOf = value.lastIndexOf('/');
        return new Resource(value.substring(0, indexOf));
    }

    /**
     * Returns a {@link String} representation of the {@link Resource}
     *
     * @return the {@link String} representation of the {@link Resource}
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a {@link String} representation of the {@link Resource} without the leading slash
     *
     * @return the {@link String} representation of the {@link Resource} without the leading slash
     */
    public String getValueWithoutLeadingSeparator() {
        if (equals(ROOT)) {
            return "";
        }
        return value.substring(1);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
