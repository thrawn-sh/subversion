/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository
 */
@ThreadSafe
public interface Repository {

	/**
	 * {@link ProtocolVersion} that represents the version of the subversion server
	 */
	public static enum ProtocolVersion {

		HTTPv1,
		HTTPv2
	}

	/**
	 * Upload a new revision of the resource and set properties
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param parents whether to create missing parents folders or not
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void add(Transaction transaction, Resource resource, boolean parents, InputStream content) throws SubversionException;

	/**
	 * Save all modifications of the current running {@link Transaction}
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param message the commit message for the expected operation
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void commit(Transaction transaction, String message) throws SubversionException;

	/**
	 * Recursively copy a resource in the given revision
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param srcRevision {@link Revision} of the resource to copy
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 * @param parents whether to create missing parents folders or not
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void copy(Transaction transaction, Resource srcResource, Revision srcRevision, Resource targetResource, boolean parents) throws SubversionException;

	/**
	 * Create a new {@link Transaction} to make modifications within
	 *
	 * @return the new {@link Transaction}
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	Transaction createTransaction() throws SubversionException;

	/**
	 * Delete the resource from the repository
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void delete(Transaction transaction, Resource resource) throws SubversionException;

	/**
	 * Download the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	InputStream download(Resource resource, Revision revision) throws SubversionException;

	/**
	 * Determine the HTTP download URI for the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return the HTTP download {@link URI} for the resource
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	URI downloadURI(Resource resource, Revision revision) throws SubversionException;

	/**
	 * Check if the resource already exists in the latest revision of the repository
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	boolean exists(Resource resource, Revision revision) throws SubversionException;

	/**
	 * Returns the {@link URI} to the root of this {@link Repository}
	 *
	 * @return the {@link URI} to the root of this {@link Repository}
	 */
	URI getBaseUri();

	/**
	 * Returns the {@link ProtocolVersion} of the server running this {@link Repository}
	 *
	 * @return the {@link ProtocolVersion} of the server running this {@link Repository}
	 */
	ProtocolVersion getProtocolVersion();

	/**
	 * Returns the {@link UUID} that identifies the {@link Repository} globally
	 *
	 * @return the {@link UUID} that identifies the {@link Repository} globally
	 */
	UUID getRepositoryId();

	/**
	 * Retrieve information for the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return {@link Info} for the resource
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	Info info(Resource resource, Revision revision) throws SubversionException;

	/**
	 * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 *
	 * @return {@link Set} of {@link Info} for the resource and its child resources (depending on depth parameter)
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	Set<Info> list(Resource resource, Revision revision, Depth depth) throws SubversionException;

	/**
	 * Mark the expected revision of the resource as locked
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void lock(Resource resource, boolean steal) throws SubversionException;

	/**
	 * Retrieve the log information for the revisions between startRevision and endRevision of the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @param limit maximal number of {@link Log} entries, if the value is lower or equal to {@code 0} all entries will be returned
	 *
	 * @return ordered (early to latest) {@link List} of {@link Log} for the revisions between startRevision and endRevision of the resource
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	List<Log> log(Resource resource, Revision startRevision, Revision endRevision, int limit) throws SubversionException;

	/**
	 * Create a folder with all necessary parents folders
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param parents whether to create missing parents folders or not
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void mkdir(Transaction transaction, Resource resource, boolean parents) throws SubversionException;

	/**
	 * Recursively move a resource (latest revision)
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 * @param parents whether to create missing parents folders or not
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void move(Transaction transaction, Resource srcResource, Resource targetResource, boolean parents) throws SubversionException;

	/**
	 * Remove the given properties form the resource
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param properties {@link ResourceProperty} to remove
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void propertiesDelete(Transaction transaction, Resource resource, ResourceProperty... properties) throws SubversionException;

	/**
	 * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param properties {@link ResourceProperty} to add or override
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void propertiesSet(Transaction transaction, Resource resource, ResourceProperty... properties) throws SubversionException;

	/**
	 * Abort the current running {@link Transaction} and revert all modifications
	 *
	 * @param transaction the current running {@link Transaction}
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void rollback(Transaction transaction) throws SubversionException;

	/**
	 * Remove the lock on the expected revision of the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
	 * @throws SubversionException if an error occurs while operating on the repository
	 */
	void unlock(Resource resource, boolean force) throws SubversionException;
}
