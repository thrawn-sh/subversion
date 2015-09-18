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

import java.util.Comparator;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link ResourceProperty} represents a resource property.
 */
@Immutable
public final class ResourceProperty {

    /**
     * {@link Key} is the internal name a {@link ResourceProperty} is stored.
     */
    public static final class Key implements Comparable<Key> {

        private final String name;

        private final Type type;

        public Key(final Type type, final String name) {
            Validate.notNull(type, "type must not be null");
            Validate.notNull(name, "name must not be null");

            this.type = type;
            this.name = name;
        }

        @Override
        public int compareTo(final Key other) {
            final int result = type.compareTo(other.type);
            if (result != 0) {
                return result;
            }
            return name.compareTo(other.name);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key)) {
                return false;
            }

            final Key key = (Key) o;

            if (!name.equals(key.name)) {
                return false;
            }
            if (type != key.type) {
                return false;
            }

            return true;
        }

        /**
         * Returns the name of the {@link Key}.
         *
         * @return the name of the {@link Key}
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the {@link Type} of the {@link Key}.
         *
         * @return the {@link Type} of the {@link Key}
         */
        public Type getType() {
            return type;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Key{type=" + type + ", name='" + name + "'}";
        }
    }

    public static final Key AUTHOR = new Key(Type.DAV, "creator-displayname");

    public static final Key CREATION_DATE = new Key(Type.DAV, "creationdate");

    public static final Key LAST_MODIFIED_DATE = new Key(Type.DAV, "getlastmodified");

    public static final Key LOCK_STATUS = new Key(Type.DAV, "lockdiscovery");

    public static final Key MD5_HASH = new Key(Type.SUBVERSION_DAV, "md5-checksum");

    public static final Key REPOSITORY_ID = new Key(Type.SUBVERSION_DAV, "repository-uuid");

    public static final Key RESOURCE = new Key(Type.SUBVERSION_DAV, "baseline-relative-path");

    public static final Key RESOURCE_TYPE = new Key(Type.DAV, "resourcetype");

    public static final Key VERSION = new Key(Type.DAV, "version-name");

    /**
     * {@link ResourceProperty} can have various types, depending of the context they are used.
     */
    public enum Type {
        DAV("DAV:"),
        SVN("svn:"),
        SUBVERSION_CUSTOM("http://subversion.tigris.org/xmlns/custom/"),
        SUBVERSION_DAV("http://subversion.tigris.org/xmlns/dav/"),
        SUBVERSION_SVN("http://subversion.tigris.org/xmlns/svn/");

        private final String prefix;

        Type(final String prefix) {
            this.prefix = prefix;
        }

        /**
         * Returns the prefix of the {@link Type}.
         *
         * @return the prefix of the {@link Type}
         */
        public String getPrefix() {
            return prefix;
        }
    }

    private final Key key;

    /**
     * {@link Comparator} that compares {@link ResourceProperty} by their type and name.
     */
    public static final Comparator<ResourceProperty> TYPE_NAME_COMPARATOR = (rp1, rp2) -> {
        Validate.notNull(rp1, "rp1 must not be null");
        Validate.notNull(rp2, "rp2 must not be null");

        return rp1.getKey().compareTo(rp2.getKey());
    };

    private final String value;

    /**
     * Create a new {@link ResourceProperty} with the given {@link Type}, name and value.
     *
     * @param type {@link Type} of the {@link ResourceProperty}
     * @param name name of the {@link ResourceProperty}
     * @param value value of the {@link ResourceProperty}
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public ResourceProperty(final Type type, final String name, final String value) {
        Validate.notNull(value, "value must not be null");

        this.key = new Key(type, name);
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceProperty)) {
            return false;
        }

        final ResourceProperty that = (ResourceProperty) o;

        if (!key.equals(that.key)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the {@link de.shadowhunt.subversion.ResourceProperty.Key} of the {@link
     * de.shadowhunt.subversion.ResourceProperty}.
     *
     * @return the {@link de.shadowhunt.subversion.ResourceProperty.Key} of the {@link
     * de.shadowhunt.subversion.ResourceProperty}
     */
    public Key getKey() {
        return key;
    }

    /**
     * Returns the name of the {@link de.shadowhunt.subversion.ResourceProperty.Key}.
     *
     * @return the name of the {@link de.shadowhunt.subversion.ResourceProperty.Key}
     */
    public String getName() {
        return key.getName();
    }

    /**
     * Returns the {@link Type} of the {@link de.shadowhunt.subversion.ResourceProperty.Key}.
     *
     * @return the {@link Type} of the {@link de.shadowhunt.subversion.ResourceProperty.Key}
     */
    public Type getType() {
        return key.getType();
    }

    /**
     * Returns the value of the {@link ResourceProperty}.
     *
     * @return the value of the {@link ResourceProperty}
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResourceProperty{ key=" + key + ", value='" + value + "'}";
    }
}
