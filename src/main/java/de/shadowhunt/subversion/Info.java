/**
 * Copyright (C) 2013-2016 shadowhunt (dev@shadowhunt.de)
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
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.Validate;

/**
 * {@link Info} holds all status information for a single {@link Revision} of a {@link Resource}.
 */
public interface Info {

    /**
     * {@link java.util.Comparator} orders {@link de.shadowhunt.subversion.Info}s by their relative {@link Resource}.
     */
    Comparator<Info> RESOURCE_COMPARATOR = (i1, i2) -> {
        Validate.notNull(i1, "i1 must not be null");
        Validate.notNull(i2, "i2 must not be null");

        return i1.getResource().compareTo(i2.getResource());
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
