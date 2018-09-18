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

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link Revision} defines the revision of a repository or a resource in that repository.
 */
@Immutable
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
     * @param revision
     *            value of the {@link Revision} must be greater or equal than {@code 1}
     *
     * @return the new {@link Revision} instance with the given value
     *
     * @throws IllegalArgumentException
     *             if revision is smaller than {@code 1}
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
        final Revision other = (Revision) obj;
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (version ^ (version >>> 32));
        return result;
    }

    @Override
    public String toString() {
        if (version == HEAD.version) {
            return "HEAD";
        }
        return Long.toString(version);
    }
}
