package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository
 */
@ThreadSafe
public interface SubversionRepository {

	/**
	 * Delete the resource from the repository
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 */
	public void delete(String resource, String message);

	/**
	 * Remove the given properties form the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link SubversionProperty} to remove
	 */
	public void deleteProperties(String resource, String message, SubversionProperty... properties);

	/**
	 * Download the resource (latest version)
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 */
	public InputStream download(String resource);

	/**
	 * Download the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 */
	public InputStream download(String resource, Revision revision);

	/**
	 * Determine the HTTP download URI for the resource (latest version)
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @return the HTTP download {@link URI} for the resource
	 */
	public URI downloadURI(String resource);

	/**
	 * Determine the HTTP download URI for the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return the HTTP download {@link URI} for the resource
	 */
	public URI downloadURI(String resource, Revision revision);

	/**
	 * Check if the resource already exists in the latest revision of the repository
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
	 */
	public boolean exists(String resource);

	/**
	 * Retrieve information for the resource (latest revision)
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link SubversionInfo} for the resource
	 */
	public SubversionInfo info(String resource, boolean withCustomProperties);

	/**
	 * Retrieve information for the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link SubversionInfo} for the resource
	 */
	public SubversionInfo info(String resource, Revision revision, boolean withCustomProperties);

	/**
	 * Retrieve log information for the latest revision of the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @return {@link SubversionLog} for latest revision of the resource
	 */
	public SubversionLog lastLog(String resource);

	/**
	 * Retrieve information for the resource and its child resources (depending on depth parameter)
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 * @param withCustomProperties whether to retrieve user defined properties
	 * @return {@link List} of {@link SubversionInfo} for the resource and its child resources (depending on depth parameter)
	 */
	public List<SubversionInfo> list(String resource, Depth depth, boolean withCustomProperties);

	/**
	 * Mark the current revision of the resource as locked
	 * @param resource the absolute path of the resource (relative to the repository root)
	 */
	public void lock(String resource);

	/**
	 * Retrieve the complete log information for all revision of the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @return ordered (early to latest) {@link List} of {@link SubversionLog} for all revision of the resource
	 */
	public List<SubversionLog> log(String resource);

	/**
	 * Retrieve the log information for the revisions between startRevision and endRevision of the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @return ordered (early to latest) {@link List} of {@link SubversionLog} for the revisions between startRevision and endRevision of the resource
	 */
	public List<SubversionLog> log(String resource, Revision startRevision, Revision endRevision);

	/**
	 * Authenticate with the given username, password and workstation against the server (NOTE: only the current thread will be authenticated)
	 * <p>to reset authentication call setCredentials(null, null, null)</p>
	 * @param user username to authenticate with against the repository (relevant for BAISC, DIGEST and NTLM)
	 * @param password password to authenticate with against the repository (relevant for BAISC, DIGEST and NTLM)
	 * @param workstation FQDN of the computer to authenticate from against the repository (relevant for NTLM)
	 */
	public void setCredentials(@Nullable String user, @Nullable String password, @Nullable String workstation);

	/**
	 * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param properties {@link SubversionProperty} to add or override
	 */
	public void setProperties(String resource, String message, SubversionProperty... properties);

	/**
	 * Remove the lock on the current revision of the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 */
	public void unlock(String resource);

	/**
	 * Upload a new revision of the resource
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 */
	public void upload(String resource, String message, InputStream content);

	/**
	 * Upload a new revision of the resource and set properties
	 * @param resource the absolute path of the resource (relative to the repository root)
	 * @param message the commit message for the current operation
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 * @param properties {@link SubversionProperty} to add or override
	 */
	public void uploadWithProperties(String resource, String message, InputStream content, SubversionProperty... properties);

}
