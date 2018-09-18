/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.StringUtils;

/**
 * {@link Resource} defines a resource location in the repository.
 */
@Immutable
public final class Resource implements Comparable<Resource>, Serializable {

    private static final Pattern PATH_PATTERN = Pattern.compile("/");

    /**
     * Represents the base {@link Resource} in the repository.
     */
    public static final Resource ROOT = new Resource("");

    /**
     * Separator {@link String} for directories.
     */
    public static final String SEPARATOR = "/";

    /**
     * Separator {@code char} for directories.
     */
    public static final char SEPARATOR_CHAR = '/';

    private static final long serialVersionUID = 1L;

    /**
     * Create a new {@link Resource} instance for the given value.
     *
     * @param path
     *            value of the {@link Resource}
     *
     * @return the new {@link Resource} instance with the given value
     */
    public static Resource create(final String path) {
        if (StringUtils.isEmpty(path) || SEPARATOR.equals(path)) {
            return ROOT;
        }

        final StringBuilder sb = new StringBuilder();
        for (final String segment : PATH_PATTERN.split(path)) {
            if (StringUtils.isEmpty(segment) || ".".equals(segment)) {
                continue;
            }

            if ("..".equals(segment)) {
                throw new SubversionException("path is not canonical: " + path);
            }

            sb.append(SEPARATOR_CHAR);
            sb.append(segment);
        }

        final String sanatisedPath = sb.toString();
        return new Resource(sanatisedPath);
    }

    private final String value;

    private Resource(final String value) {
        this.value = value;
    }

    /**
     * Appends the specified {@link Resource} to the end of this {@link Resource}.
     *
     * @param resource
     *            the {@link Resource} that is appended to the end of this {@link Resource}
     *
     * @return a {@link Resource} that represents the combination of this {@link Resource} and the specified {@link Resource}
     */
    public Resource append(final Resource resource) {
        return new Resource(value + resource.value);
    }

    @Override
    public int compareTo(final Resource other) {
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
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    /**
     * Returns the parent {@link Resource} of the {@link Resource}, the parent of the ROOT element is the ROOT itself.
     *
     * @return the parent {@link Resource} of the {@link Resource}
     */
    public Resource getParent() {
        if (equals(ROOT)) {
            return ROOT; // parent of root is root
        }
        final int indexOf = value.lastIndexOf(SEPARATOR_CHAR);
        final String parentPath = value.substring(0, indexOf);
        return new Resource(parentPath);
    }

    /**
     * Returns a {@link String} representation of the {@link Resource}.
     *
     * @return the {@link String} representation of the {@link Resource}
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a {@link String} representation of the {@link Resource} without the leading slash.
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
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * Returns {@code true} if this {@link Resource} is a prefix of the given {@link Resource}.
     *
     * @param other
     *            {@link Resource} to check against this {@link Resource}
     *
     * @return {@code true} if this {@link Resource} is a prefix of the given {@link Resource}
     */
    public boolean isPrefix(final Resource other) {
        return other.value.startsWith(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
