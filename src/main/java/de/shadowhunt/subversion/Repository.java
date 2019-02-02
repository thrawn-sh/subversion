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

import java.io.InputStream;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository.
 */
@ThreadSafe
public interface Repository extends ReadOnlyRepository {

    /**
     * Upload a new revision of the resource and set properties.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param parents
     *            whether to create missing parents folders or not
     * @param content
     *            {@link InputStream} from which the content will be read (will be closed after transfer)
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void add(Transaction transaction, Resource resource, boolean parents, InputStream content);

    /**
     * Save all modifications of the current running {@link Transaction}.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param message
     *            the commit message for the expected operation
     * @param releaseLocks
     *            remove all locks from {@link Resource}s in this {@link Transaction}
     *
     * @throws java.lang.NullPointerException
     *             if the transaction parameter is {@code null}
     * @throws java.lang.NullPointerException
     *             if the message parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void commit(Transaction transaction, String message, boolean releaseLocks);

    /**
     * Recursively copy a resource in the given revision.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param sourceResource
     *            the {@link Resource} of the source resource (relative to the repository root)
     * @param sourceRevision
     *            {@link Revision} of the resource to copy
     * @param targetResource
     *            the {@link Resource} of the target resource (relative to the repository root)
     * @param parents
     *            whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void copy(Transaction transaction, Resource sourceResource, Revision sourceRevision, Resource targetResource, boolean parents);

    /**
     * Create a new {@link Transaction} to make modifications within.
     *
     * @return the new {@link Transaction}
     *
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    Transaction createTransaction();

    /**
     * Delete the resource from the repository.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void delete(Transaction transaction, Resource resource);

    /**
     * Mark the expected revision of the resource as locked.
     *
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param steal
     *            if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
     *
     * @throws java.lang.NullPointerException
     *             if the resource parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void lock(Resource resource, boolean steal);

    /**
     * Create a folder with all necessary parents folders.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param parents
     *            whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void mkdir(Transaction transaction, Resource resource, boolean parents);

    /**
     * Recursively move a resource (latest revision).
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param sourceResource
     *            the {@link Resource} of the source resource (relative to the repository root)
     * @param targetResource
     *            the {@link Resource} of the target resource (relative to the repository root)
     * @param parents
     *            whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void move(Transaction transaction, Resource sourceResource, Resource targetResource, boolean parents);

    /**
     * Remove the given properties form the resource.
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param properties
     *            {@link ResourceProperty} to remove
     *
     * @throws java.lang.IllegalArgumentException
     *             if properties contain {@code null} elements
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void propertiesDelete(Transaction transaction, Resource resource, ResourceProperty... properties);

    /**
     * Set the given properties for the resource (new properties will be added, existing properties will be overridden).
     *
     * @param transaction
     *            the current running {@link Transaction}
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param properties
     *            {@link ResourceProperty} to add or override
     *
     * @throws java.lang.IllegalArgumentException
     *             if properties contain {@code null} elements
     * @throws java.lang.NullPointerException
     *             if any parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void propertiesSet(Transaction transaction, Resource resource, ResourceProperty... properties);

    /**
     * Abort the current running {@link Transaction} and revert all modifications.
     *
     * @param transaction
     *            the current running {@link Transaction}
     *
     * @throws java.lang.NullPointerException
     *             if the transaction parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void rollback(Transaction transaction);

    /**
     * Abort the current running {@link Transaction} and revert all modifications if the transaction is not committed.
     *
     * @param transaction
     *            the current running {@link Transaction}
     *
     * @throws java.lang.NullPointerException
     *             if the transaction parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void rollbackIfNotCommitted(Transaction transaction);

    /**
     * Remove the lock on the expected revision of the resource.
     *
     * @param resource
     *            the {@link Resource} of the resource (relative to the repository root)
     * @param force
     *            the user that created the lock must match the user who wants to delete it, unless force is {@code
     * true}
     *
     * @throws java.lang.NullPointerException
     *             if the resource parameter is {@code null}
     * @throws SubversionException
     *             if an error occurs while operating on the repository
     * @throws TransmissionException
     *             if an error occurs in the underlining communication with the server
     */
    void unlock(Resource resource, boolean force);
}
