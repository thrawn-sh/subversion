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
	 * @param srcResource the {@link Path} of the source resource (relative to the repository root)
	 * @param srcRevision {@link Revision} of the resource to copy
	 * @param targetResource the {@link Path} of the target resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void copy(Path srcResource, Revision srcRevision, Path targetResource, String message);

	/**
	 * Delete the resource from the repository
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void delete(Path resource, String message);

	/**
	 * Remove the given properties form the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link ResourceProperty} to remove
	 */
	public void deleteProperties(Path resource, String message, ResourceProperty... properties);

	/**
	 * Download the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 */
	public InputStream download(Path resource, Revision revision);

	/**
	 * Determine the HTTP download URI for the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return the HTTP download {@link URI} for the resource
	 */
	public URI downloadURI(Path resource, Revision revision);

	/**
	 * Check if the resource already exists in the latest revision of the repository
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
	 */
	public boolean exists(Path resource);

	/**
	 * Retrieve information for the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link InfoEntry} for the resource
	 */
	public InfoEntry info(Path resource, Revision revision, boolean withCustomProperties);

	/**
	 * Retrieve log information for the latest revision of the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @return {@link LogEntry} for latest revision of the resource
	 */
	public LogEntry lastLog(Path resource);

	/**
	 * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link List} of {@link InfoEntry} for the resource and its child resources (depending on depth parameter)
	 */
	public List<InfoEntry> list(Path resource, Revision revision, Depth depth, boolean withCustomProperties);

	/**
	 * Mark the current revision of the resource as locked
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 */
	public void lock(Path resource);

	/**
	 * Retrieve the log information for the revisions between startRevision and endRevision of the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @return ordered (early to latest) {@link List} of {@link LogEntry} for the revisions between startRevision and endRevision of the resource
	 */
	public List<LogEntry> log(Path resource, Revision startRevision, Revision endRevision);

	/**
	 * Recursively move a resource (latest revision)
	 * @param srcResource the {@link Path} of the source resource (relative to the repository root)
	 * @param targetResource the {@link Path} of the target resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void move(Path srcResource, Path targetResource, String message);

	/**
	 * Create a folder with all necessary parent folders
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void createFolder(Path resource, String message);

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
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link ResourceProperty} to add or override
	 */
	public void setProperties(Path resource, String message, ResourceProperty... properties);

	/**
	 * Remove the lock on the current revision of the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 */
	public void unlock(Path resource);

	/**
	 * Upload a new revision of the resource
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 */
	public void upload(Path resource, String message, InputStream content);

	/**
	 * Upload a new revision of the resource and set properties
	 * @param resource the {@link Path} of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 * @param properties {@link ResourceProperty} to add or override
	 */
	public void uploadWithProperties(Path resource, String message, InputStream content, ResourceProperty... properties);
}
