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

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository.
 */
@ThreadSafe
public interface Repository {

    /**
     * {@link ProtocolVersion} that represents the version of the subversion server.
     */
    public static enum ProtocolVersion {

        HTTP_V1,
        HTTP_V2
    }

    /**
     * Upload a new revision of the resource and set properties.
     *
     * @param transaction the current running {@link Transaction}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void add(Transaction transaction, Resource resource, boolean parents, InputStream content);

    /**
     * Save all modifications of the current running {@link Transaction}.
     *
     * @param transaction the current running {@link Transaction}
     * @param message the commit message for the expected operation
     *
     * @throws java.lang.NullPointerException if the transaction parameter is {@code null}
     * @throws java.lang.NullPointerException if the message parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void commit(Transaction transaction, String message);

    /**
     * Recursively copy a resource in the given revision.
     *
     * @param transaction the current running {@link Transaction}
     * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
     * @param srcRevision {@link Revision} of the resource to copy
     * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void copy(Transaction transaction, Resource srcResource, Revision srcRevision, Resource targetResource, boolean parents);

    /**
     * Create a new {@link Transaction} to make modifications within.
     *
     * @return the new {@link Transaction}
     *
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    Transaction createTransaction();

    /**
     * Create a new {@link View} to query consistent repository information.
     *
     * @return the new {@link View}
     *
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    View createView();

    /**
     * Delete the resource from the repository.
     *
     * @param transaction the current running {@link Transaction}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void delete(Transaction transaction, Resource resource);

    /**
     * Download the resource.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    InputStream download(Resource resource, Revision revision);

    /**
     * Download the resource.
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    InputStream download(View view, Resource resource, Revision revision);

    /**
     * Determine the HTTP download URI for the resource.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return the HTTP download {@link URI} for the resource
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    URI downloadURI(Resource resource, Revision revision);

    /**
     * Determine the HTTP download URI for the resource.
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return the HTTP download {@link URI} for the resource
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    URI downloadURI(View view, Resource resource, Revision revision);

    /**
     * Check if the resource already exists in the latest revision of the repository.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code
     * false}
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    boolean exists(Resource resource, Revision revision);

    /**
     * Check if the resource already exists in the latest revision of the repository.
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     *
     * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code
     * false}
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    boolean exists(View view, Resource resource, Revision revision);

    /**
     * Returns the {@link URI} to the root of this {@link Repository}.
     *
     * @return the {@link URI} to the root of this {@link Repository}
     */
    URI getBaseUri();

    /**
     * Returns the {@link ProtocolVersion} of the server running this {@link Repository}.
     *
     * @return the {@link ProtocolVersion} of the server running this {@link Repository}
     */
    ProtocolVersion getProtocolVersion();

    /**
     * Returns the {@link UUID} that identifies the {@link Repository} globally.
     *
     * @return the {@link UUID} that identifies the {@link Repository} globally
     */
    UUID getRepositoryId();

    /**
     * Retrieve information for the resource.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     * @param keys request only the specified {@link ResourceProperty.Key}s
     *
     * @return {@link Info} for the resource (if keys where specified, only reduced {@link Info} will be returned)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    Info info(Resource resource, Revision revision, ResourceProperty.Key... keys);

    /**
     * Retrieve information for the resource.
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     * @param keys request only the specified {@link ResourceProperty.Key}s
     *
     * @return {@link Info} for the resource (if keys where specified, only reduced {@link Info} will be returned)
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    Info info(View view, Resource resource, Revision revision, ResourceProperty.Key... keys);

    /**
     * Retrieve information for the resource in the given revision and its child resources (depending on depth
     * parameter).
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     * @param depth whether to retrieve only for the given resource, its children or only part of its children depending
     * on the value of {@link Depth}
     * @param keys request only the specified {@link ResourceProperty.Key}s
     *
     * @return {@link Set} of {@link Info} for the resource and its child resources (depending on depth parameter), if
     * keys where specified, only reduced {@link Info} will contained in the {@link Set}
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    Set<Info> list(Resource resource, Revision revision, Depth depth, ResourceProperty.Key... keys);

    /**
     * Retrieve information for the resource in the given revision and its child resources (depending on depth
     * parameter).
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param revision the {@link Revision} of the resource to retrieve
     * @param depth whether to retrieve only for the given resource, its children or only part of its children depending
     * on the value of {@link Depth}
     * @param keys request only the specified {@link ResourceProperty.Key}s
     *
     * @return {@link Set} of {@link Info} for the resource and its child resources (depending on depth parameter), if
     * keys where specified, only reduced {@link Info} will contained in the {@link Set}
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    Set<Info> list(View view, Resource resource, Revision revision, Depth depth, ResourceProperty.Key... keys);

    /**
     * Mark the expected revision of the resource as locked.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the
     * operation will fail
     *
     * @throws java.lang.NullPointerException if the resource parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void lock(Resource resource, boolean steal);

    /**
     * Retrieve the log information for the revisions between startRevision and endRevision of the resource.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param startRevision the first {@link Revision} of the resource to retrieve (including)
     * @param endRevision the last {@link Revision} of the resource to retrieve (including)
     * @param limit maximal number of {@link Log} entries, if the value is lower or equal to {@code 0} all entries will be returned
     * @þaram stopOnCopy do not cross copies while traversing history
     *
     * @return ordered (early to latest) {@link List} of {@link Log} for the revisions between startRevision and
     * endRevision of the resource
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    List<Log> log(Resource resource, Revision startRevision, Revision endRevision, int limit, boolean stopOnCopy);

    /**
     * Retrieve the log information for the revisions between startRevision and endRevision of the resource.
     *
     * @param view the current valid {@link View}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param startRevision the first {@link Revision} of the resource to retrieve (including)
     * @param endRevision the last {@link Revision} of the resource to retrieve (including)
     * @param limit maximal number of {@link Log} entries, if the value is lower or equal to {@code 0} all entries will be returned
     * @þaram stopOnCopy do not cross copies while traversing history
     *
     * @return ordered (early to latest) {@link List} of {@link Log} for the revisions between startRevision and
     * endRevision of the resource
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    List<Log> log(View view, Resource resource, Revision startRevision, Revision endRevision, int limit, boolean stopOnCopy);

    /**
     * Create a folder with all necessary parents folders.
     *
     * @param transaction the current running {@link Transaction}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void mkdir(Transaction transaction, Resource resource, boolean parents);

    /**
     * Recursively move a resource (latest revision).
     *
     * @param transaction the current running {@link Transaction}
     * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
     * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
     * @param parents whether to create missing parents folders or not
     *
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void move(Transaction transaction, Resource srcResource, Resource targetResource, boolean parents);

    /**
     * Remove the given properties form the resource.
     *
     * @param transaction the current running {@link Transaction}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param properties {@link ResourceProperty} to remove
     *
     * @throws java.lang.IllegalArgumentException if properties contain {@code null} elements
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void propertiesDelete(Transaction transaction, Resource resource, ResourceProperty... properties);

    /**
     * Set the given properties for the resource (new properties will be added, existing properties will be
     * overridden).
     *
     * @param transaction the current running {@link Transaction}
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param properties {@link ResourceProperty} to add or override
     *
     * @throws java.lang.IllegalArgumentException if properties contain {@code null} elements
     * @throws java.lang.NullPointerException if any parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void propertiesSet(Transaction transaction, Resource resource, ResourceProperty... properties);

    /**
     * Abort the current running {@link Transaction} and revert all modifications.
     *
     * @param transaction the current running {@link Transaction}
     *
     * @throws java.lang.NullPointerException if the transaction parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void rollback(Transaction transaction);

    /**
     * Abort the current running {@link Transaction} and revert all modifications if the transaction is not committed.
     *
     * @param transaction the current running {@link Transaction}
     *
     * @throws java.lang.NullPointerException if the transaction parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void rollbackIfNotCommitted(Transaction transaction);

    /**
     * Remove the lock on the expected revision of the resource.
     *
     * @param resource the {@link Resource} of the resource (relative to the repository root)
     * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code
     * true}
     *
     * @throws java.lang.NullPointerException if the resource parameter is {@code null}
     * @throws de.shadowhunt.subversion.SubversionException if an error occurs while operating on the repository
     * @throws de.shadowhunt.subversion.TransmissionException if an error occurs in the underlining communication with
     * the server
     */
    void unlock(Resource resource, boolean force);
}
