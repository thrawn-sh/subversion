/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.Validate;

/**
 * {@link Info} holds all status information for a single {@link Revision} of a {@link Resource}.
 */
@Immutable
public interface Info {

    /**
     * {@link Comparator} orders {@link Info} s by their relative {@link Resource}.
     */
    Comparator<Info> RESOURCE_COMPARATOR = (i1, i2) -> {
        Validate.notNull(i1, "i1 must not be null");
        Validate.notNull(i2, "i2 must not be null");

        final Resource resource1 = i1.getResource();
        final Resource resource2 = i2.getResource();
        return resource1.compareTo(resource2);
    };

    /**
     * Returns the {@link Date} when the resource was created.
     *
     * @return the {@link Date} when the resource was created
     */
    Date getCreationDate();

    /**
     * Returns the {@link Date} when the resource was last modified.
     *
     * @return the {@link Date} when the resource was last modified
     */
    Date getLastModifiedDate();

    /**
     * Returns a name of the lock owner.
     *
     * @return the name of the lock owner if the resource is locked
     */
    Optional<String> getLockOwner();

    /**
     * Returns a lock-token.
     *
     * @return the lock-token if the resource is locked
     */
    Optional<LockToken> getLockToken();

    /**
     * Returns a MD5 checksum of the resource.
     *
     * @return the MD5 checksum of the resource if the resource is a file
     */
    Optional<String> getMd5();

    /**
     * Returns an array of the custom {@link ResourceProperty}.
     *
     * @return the array of the custom {@link ResourceProperty} or an empty array if there a non
     */
    ResourceProperty[] getProperties();

    /**
     * Returns a globally unique identifier of the repository.
     *
     * @return the globally unique identifier of the repository
     */
    UUID getRepositoryId();

    /**
     * Returns a {@link Resource} of the resource (relative to the root of the repository).
     *
     * @return the {@link Resource} of the resource (relative to the root of the repository)
     */
    Resource getResource();

    /**
     * Returns a {@link Revision} of the resource.
     *
     * @return the {@link Revision} of the resource
     */
    Revision getRevision();

    /**
     * Determines if the resource is a directory.
     *
     * @return {@code true} if the resource is a directory otherwise {@code false}
     */
    boolean isDirectory();

    /**
     * Determines if the resource is a file.
     *
     * @return {@code true} if the resource is a file otherwise {@code false}
     */
    boolean isFile();

    /**
     * Determines if the resource is locked.
     *
     * @return {@code true} if the resource is locked otherwise {@code false}
     */
    boolean isLocked();
}
