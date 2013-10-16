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

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository
 */
@ThreadSafe
public interface Repository {

	/**
	 * Recursively copy a resource in the given revision
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param srcRevision {@link Revision} of the resource to copy
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void copy(Resource srcResource, Revision srcRevision, Resource targetResource, String message);

	/**
	 * Create a folder with all necessary parent folders
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void createFolder(Resource resource, String message);

	/**
	 * Delete the resource from the repository
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void delete(Resource resource, String message);

	/**
	 * Remove the given properties form the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link ResourceProperty} to remove
	 */
	public void deleteProperties(Resource resource, String message, ResourceProperty... properties);

	/**
	 * Download the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 */
	public InputStream download(Resource resource, Revision revision);

	/**
	 * Determine the HTTP download URI for the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return the HTTP download {@link URI} for the resource
	 */
	public URI downloadURI(Resource resource, Revision revision);

	/**
	 * Check if the resource already exists in the latest revision of the repository
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
	 */
	public boolean exists(Resource resource, Revision revision);

	/**
	 * Retrieve information for the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link InfoEntry} for the resource
	 */
	public InfoEntry info(Resource resource, Revision revision, boolean withCustomProperties);

	/**
	 * Retrieve log information for the latest revision of the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @return {@link LogEntry} for latest revision of the resource
	 */
	public LogEntry lastLog(Resource resource);

	/**
	 * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link List} of {@link InfoEntry} for the resource and its child resources (depending on depth parameter)
	 */
	public List<InfoEntry> list(Resource resource, Revision revision, Depth depth, boolean withCustomProperties);

	/**
	 * Mark the current revision of the resource as locked
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
	 */
	public void lock(Resource resource, boolean steal);

	/**
	 * Retrieve the log information for the revisions between startRevision and endRevision of the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @return ordered (early to latest) {@link List} of {@link LogEntry} for the revisions between startRevision and endRevision of the resource
	 */
	public List<LogEntry> log(Resource resource, Revision startRevision, Revision endRevision, int limit);

	/**
	 * Recursively move a resource (latest revision)
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void move(Resource srcResource, Resource targetResource, String message);

	/**
	 * Authenticate with the given username, password and workstation against the server (NOTE: only the current thread will be authenticated)
	 * <p>to reset authentication call setCredentials(null, null, null)</p>
	 * @param user username to authenticate with against the repository my include the domain ("DOMAIN\\username") (relevant for BAISC, DIGEST and NTLM)
	 * @param password password to authenticate with against the repository (relevant for BAISC, DIGEST and NTLM)
	 * @param workstation FQDN of the computer to authenticate from against the repository (relevant for NTLM)
	 */
	public void setCredentials(@Nullable String user, @Nullable String password, @Nullable String workstation);

	/**
	 * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link ResourceProperty} to add or override
	 */
	public void setProperties(Resource resource, String message, ResourceProperty... properties);

	/**
	 * Remove the lock on the current revision of the resource
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
	 */
	public void unlock(Resource resource, boolean force);

	/**
	 * Upload a new revision of the resource and set properties
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 * @param properties {@link ResourceProperty} to add or override
	 */
	public void upload(Resource resource, String message, InputStream content, ResourceProperty... properties);
}
