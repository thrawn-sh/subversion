/**
 * Copyright © 2013-2017 shadowhunt (dev@shadowhunt.de)
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
package de.shadowhunt.subversion;

import java.io.Serializable;

import org.apache.commons.lang3.Validate;

/**
 * {@link Revision} defines the revision of a repository or a resource in that repository.
 */
public final class Revision implements Comparable<Revision>, Serializable {

    /**
     * Represents the {@link Revision} of an empty repository.
     */
    public static final Revision EMPTY = new Revision(0L);

    /**
     * Represents the newest {@link Revision} in the repository.
     */
    public static final Revision HEAD = new Revision(Long.MAX_VALUE);

    /**
     * Represents the first {@link Revision} in the repository.
     */
    public static final Revision INITIAL = new Revision(1L);

    private static final long serialVersionUID = 1L;

    /**
     * Create a new {@link Revision} instance for the given value.
     *
     * @param revision value of the {@link Revision} must be greater or equal than {@code 1}
     *
     * @return the new {@link Revision} instance with the given value
     *
     * @throws IllegalArgumentException if revision is smaller than {@code 1}
     */
    public static Revision create(final int revision) {
        Validate.isTrue((revision >= 0), "Value must be greater or equal than 0, was {0}", revision);

        switch (revision) {
            case 0:
                return EMPTY;
            case 1:
                return INITIAL;
            default:
                return new Revision(revision);
        }
    }

    private final long version;

    private Revision(final long revision) {
        version = revision;
    }

    @Override
    public int compareTo(final Revision other) {
        return Long.compare(version, other.version);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Revision)) {
            return false;
        }

        final Revision revision = (Revision) o;

        if (version != revision.version) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (version ^ (version >>> 32));
    }

    @Override
    public String toString() {
        if (version == HEAD.version) {
            return "HEAD";
        }
        return Long.toString(version);
    }
}
