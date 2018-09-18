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
    @Immutable
    public static final class Key implements Comparable<Key> {

        public static final Key EOL_SYTLE = new Key(Type.SUBVERSION_SVN, "eol-style");

        public static final Key EXECUTABLE = new Key(Type.SUBVERSION_SVN, "executable");

        public static final Key KEYWORDS = new Key(Type.SUBVERSION_SVN, "keywords");

        public static final Key MIME_TYPE = new Key(Type.SUBVERSION_SVN, "mime-type");

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
            result = (31 * result) + type.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return type + "|" + name;
        }
    }

    /**
     * {@link ResourceProperty} can have various types, depending of the context they are used.
     */
    public enum Type {
        DAV("DAV:"), //
        SUBVERSION_CUSTOM("http://subversion.tigris.org/xmlns/custom/"), //
        SUBVERSION_DAV("http://subversion.tigris.org/xmlns/dav/"), //
        SUBVERSION_SVN("http://subversion.tigris.org/xmlns/svn/"), //
        SVN("svn:");

        private final String namespace;

        Type(final String namespace) {
            this.namespace = namespace;
        }

        /**
         * Returns the namespace of the {@link Type}.
         *
         * @return the namespace of the {@link Type}
         */
        public String getNamespace() {
            return namespace;
        }
    }

    private final Key key;

    private final String value;

    public ResourceProperty(final Key key, final String value) {
        Validate.notNull(key, "key must not be null");
        Validate.notNull(value, "value must not be null");

        this.key = key;
        this.value = value;
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
        final ResourceProperty other = (ResourceProperty) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
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
     * Returns the {@link Key} of the {@link ResourceProperty}.
     *
     * @return the {@link Key} of the {@link ResourceProperty}
     */
    public Key getKey() {
        return key;
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
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((key == null) ? 0 : key.hashCode());
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ResourceProperty{ key=" + key + ", value='" + value + "'}";
    }

}
