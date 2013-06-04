package de.shadowhunt.scm.subversion.v1_6;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

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
 * {@code SubversionRepository1_6} supports subversion servers of version 1.6.X
 */
public class SubversionRepository1_6 extends AbstractSubversionRepository<SubversionRequestFactory1_6> {

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	protected SubversionRepository1_6(final URI repository, final boolean trustServerCertificat) {
		super(repository, trustServerCertificat, new SubversionRequestFactory1_6());
	}

	protected void checkout(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_VCC + "default");

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void contentUpload(final String normalizedResource, final SubversionInfo info, final UUID uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final String srcResource, final Revision srcRevision, final String targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final String normalizeSourceResource = normalizeResource(srcResource);
			final String normalizeTargetResource = normalizeResource(targetResource);

			final SubversionInfo info = info0(normalizeSourceResource, srcRevision, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			copy0(normalizeSourceResource, info.getRevision(), normalizeTargetResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void copy0(final String normalizeSourceResource, final Revision srcRevision, final String normalizeTargetResource, final UUID uuid) {
		final URI src = URI.create(repository + PREFIX_BC + srcRevision + normalizeSourceResource);
		final URI target = URI.create(repository + PREFIX_WRK + uuid + normalizeTargetResource);
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void delete(final String resource, final String message) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(normalizedResource, uuid, revision);
			delete(normalizedResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void delete(final String normalizedResource, final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + normalizedResource);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(normalizedResource, uuid, revision);
			propertiesRemove(normalizedResource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	protected InputStream download0(final String normalizedResource, final Revision revision) {
		final URI uri;
		if (Revision.HEAD.equals(revision)) {
			uri = URI.create(repository + normalizedResource);
		} else {
			uri = URI.create(repository + PREFIX_BC + revision + normalizedResource);
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
		return URI.create(repository + PREFIX_BC + revision + normalizedResource);
	}

	protected void endTransaction(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	protected List<SubversionInfo> list0(final String normalizedResource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final String uriPrefix = repository + PREFIX_BC + revision;
		return list(uriPrefix, normalizedResource, depth, withCustomProperties);
	}

	protected void merge(final SubversionInfo info, final UUID uuid) {
		final String path = repository.getPath() + PREFIX_ACT + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final String srcResource, final String targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final String normalizeSourceResource = normalizeResource(srcResource);
			final String normalizeTargetResource = normalizeResource(targetResource);

			final SubversionInfo info = info0(normalizeSourceResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			copy0(normalizeSourceResource, info.getRevision(), normalizeTargetResource, uuid);
			delete(normalizeSourceResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void prepareContentUpload(final String normalizedResource, final UUID uuid, final Revision revision) {
		final URI uri = URI.create(repository + PREFIX_VER + revision + normalizedResource);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected UUID prepareTransaction() {
		final UUID uuid = UUID.randomUUID();
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createActivityRequest(uri);
		execute(request, HttpStatus.SC_CREATED);
		return uuid;
	}

	protected void propertiesRemove(final String normalizedResource, final SubversionInfo info, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String normalizedResource, final SubversionInfo info, final UUID uuid, @Nullable final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final Revision revision, final String message) {
		final URI uri = URI.create(repository + PREFIX_WBL + uuid + "/" + revision);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final String normalizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = prepareTransaction();
		try {
			final boolean exists = exists0(normalizedResource);
			final String infoResource;
			if (exists) {
				infoResource = normalizedResource;
			} else {
				infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), normalizedResource);
			}

			final SubversionInfo info = info0(infoResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			if (exists) {
				prepareContentUpload(normalizedResource, uuid, revision);
			}
			contentUpload(normalizedResource, info, uuid, content);
			propertiesSet(normalizedResource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}
}
