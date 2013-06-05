package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.AbstractSubversionRepository;
import de.shadowhunt.scm.subversion.Depth;
import de.shadowhunt.scm.subversion.Path;
import de.shadowhunt.scm.subversion.Revision;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

/**
 * {@code SubversionRepository1_7} supports subversion servers of version 1.7.X
 */
public class SubversionRepository1_7 extends AbstractSubversionRepository<SubversionRequestFactory1_7> {

	protected static final String PREFIX_ME = "/!svn/me";

	protected static final String PREFIX_RVR = "/!svn/rvr/";

	protected static final String PREFIX_TXN = "/!svn/txn/";

	protected static final String PREFIX_TXR = "/!svn/txr/";

	protected SubversionRepository1_7(final URI repositoryRoot, final boolean trustServerCertificat) {
		super(repositoryRoot, trustServerCertificat, new SubversionRequestFactory1_7());
	}

	protected void contentUpload(final Path resource, final SubversionInfo info, final String uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final Path srcResource, final Revision srcRevision, final Path targetResource, final String message) {
		final SubversionInfo info = info(srcResource, srcRevision, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		createMissingFolders(PREFIX_TXR, uuid, targetResource.getParent());
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		merge(info, uuid);
	}

	protected void copy0(final Path srcResource, final Revision srcRevision, final Path targetResource, final String uuid) {
		final URI src = URI.create(repository + PREFIX_RVR + srcRevision + srcResource.getValue());
		final URI target = URI.create(repository + PREFIX_TXR + uuid + targetResource.getValue());
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void delete(final Path resource, final String message) {
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		delete0(resource, uuid);
		final SubversionInfo info = info(resource, Revision.HEAD, false);
		merge(info, uuid);
	}

	protected void delete0(final Path resource, final String uuid) {
		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final Path resource, final String message, final SubversionProperty... properties) {
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		final SubversionInfo info = info(resource, Revision.HEAD, false);
		propertiesRemove(resource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	public InputStream download(final Path resource, final Revision revision) {
		final URI uri = downloadURI(resource, revision);

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	public URI downloadURI(final Path resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URI.create(repository + resource.getValue());
		}
		return URI.create(repository + PREFIX_RVR + revision + resource.getValue());
	}

	@Override
	public List<SubversionInfo> list(final Path resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final String uriPrefix = repository + PREFIX_RVR + revision;
		return list(uriPrefix, resource, depth, withCustomProperties);
	}

	protected void merge(final SubversionInfo info, final String uuid) {
		final Path path = Path.create(repository.getPath() + PREFIX_TXN + uuid);
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final Path srcResource, final Path targetResource, final String message) {
		final SubversionInfo info = info(srcResource, Revision.HEAD, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		delete0(srcResource, uuid);
		merge(info, uuid);
	}

	protected String prepareTransaction() {
		final URI uri = URI.create(repository + PREFIX_ME);

		final HttpUriRequest request = requestFactory.createPrepareRequest(uri);
		final HttpResponse response = execute(request, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesRemove(final Path resource, final SubversionInfo info, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final Path resource, final SubversionInfo info, final String uuid, @Nullable final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final String uuid, final String message) {
		final URI uri = URI.create(repository + PREFIX_TXN + uuid);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final Path resource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final String uuid = prepareTransaction();

		final Path infoResource;
		if (exists(resource)) {
			infoResource = resource;
		} else {
			infoResource = createMissingFolders(PREFIX_TXR, uuid, resource);
		}
		final SubversionInfo info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		contentUpload(resource, info, uuid, content);
		propertiesSet(resource, info, uuid, properties);
		merge(info, uuid);
	}
}
