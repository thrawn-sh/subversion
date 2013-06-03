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

	protected void contentUpload(final String normalizedResource, final SubversionInfo info, final String uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final String srcResource, final Revision srcRevision, final String targetResource, final String message) {
		final String normalizeSourceResource = normalizeResource(srcResource);
		final String normalizeTargetResource = normalizeResource(targetResource);

		final SubversionInfo info = info0(normalizeSourceResource, srcRevision, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		copy0(normalizeSourceResource, info.getRevision(), normalizeTargetResource, uuid);
		merge(info, uuid);
	}

	protected void copy0(final String normalizeSourceResource, final Revision srcRevision, final String normalizeTargetResource, final String uuid) {
		final URI src = URI.create(repository + PREFIX_RVR + srcRevision + normalizeSourceResource);
		final URI target = URI.create(repository + PREFIX_TXR + uuid + normalizeTargetResource);
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void delete(final String resource, final String message) {
		final String normalizedResource = normalizeResource(resource);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		delete0(normalizedResource, uuid);
		final SubversionInfo info = info0(normalizedResource, Revision.HEAD, false);
		merge(info, uuid);
	}

	protected void delete0(final String normalizedResource, final String uuid) {
		final URI uri = URI.create(repository + PREFIX_TXR + uuid + normalizedResource);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String normalizedResource = normalizeResource(resource);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		final SubversionInfo info = info0(normalizedResource, Revision.HEAD, false);
		propertiesRemove(normalizedResource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	protected InputStream download0(final String normalizedResource, final Revision revision) {
		final URI uri;
		if (Revision.HEAD.equals(revision)) {
			uri = URI.create(repository + normalizedResource);
		} else {
			uri = URI.create(repository + PREFIX_RVR + revision + normalizedResource);
		}

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	protected URI downloadURI0(final String normalizedResource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URI.create(repository + normalizedResource);
		}
		return URI.create(repository + PREFIX_RVR + revision + normalizedResource);
	}

	@Override
	public List<SubversionInfo> list(final String resource, final Depth depth, final boolean withCustomProperties) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, Revision.HEAD, false);
		final String uriPrefix = repository + PREFIX_RVR + info.getRevision();
		return list(uriPrefix, normalizedResource, depth, withCustomProperties);
	}

	protected void merge(final SubversionInfo info, final String uuid) {
		final String path = repository.getPath() + PREFIX_TXN + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final String srcResource, final String targetResource, final String message) {
		final String normalizeSourceResource = normalizeResource(srcResource);
		final String normalizeTargetResource = normalizeResource(targetResource);

		final SubversionInfo info = info0(normalizeSourceResource, Revision.HEAD, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		copy0(normalizeSourceResource, info.getRevision(), normalizeTargetResource, uuid);
		delete0(normalizeSourceResource, uuid);
		merge(info, uuid);
	}

	protected String prepareTransaction() {
		final URI uri = URI.create(repository + PREFIX_ME);

		final HttpUriRequest request = requestFactory.createPrepareRequest(uri);
		final HttpResponse response = execute(request, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesRemove(final String normalizedResource, final SubversionInfo info, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String normalizedResource, final SubversionInfo info, final String uuid, @Nullable final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

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
	protected void uploadWithProperties0(final String normalizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final String uuid = prepareTransaction();
		final boolean exists = exists0(normalizedResource);

		final String infoResource;
		if (exists) {
			infoResource = normalizedResource;
		} else {
			infoResource = createMissingFolders(PREFIX_TXR, uuid, normalizedResource);
		}
		final SubversionInfo info = info0(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		contentUpload(normalizedResource, info, uuid, content);
		propertiesSet(normalizedResource, info, uuid, properties);
		merge(info, uuid);
	}
}
