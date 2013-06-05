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
import de.shadowhunt.scm.subversion.Path;
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
		final URI href = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, href);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void contentUpload(final Path resource, final SubversionInfo info, final UUID uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource);
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final Path srcResource, final Revision srcRevision, final Path targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final SubversionInfo info = info(srcResource, srcRevision, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			createMissingFolders(PREFIX_WRK, uuid.toString(), targetResource.getParent());
			copy0(srcResource, info.getRevision(), targetResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void copy0(final Path srcResource, final Revision srcRevision, final Path targetResource, final UUID uuid) {
		final URI src = URI.create(repository + PREFIX_BC + srcRevision + srcResource);
		final URI target = URI.create(repository + PREFIX_WRK + uuid + targetResource);
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void delete(final Path resource, final String message) {
		final SubversionInfo info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(resource, uuid, revision);
			delete(resource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void delete(final Path resource, final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final Path resource, final String message, final SubversionProperty... properties) {
		final SubversionInfo info = info(resource, Revision.HEAD, false);
		final Revision revision = info.getRevision();

		final UUID uuid = prepareTransaction();
		try {
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			prepareContentUpload(resource, uuid, revision);
			propertiesRemove(resource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public InputStream download(final Path resource, final Revision revision) {
		final URI uri;
		if (Revision.HEAD.equals(revision)) {
			uri = URI.create(repository + resource.getValue());
		} else {
			uri = URI.create(repository + PREFIX_BC + revision + resource);
		}

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	public URI downloadURI(final Path resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URI.create(repository + resource.getValue());
		}
		return URI.create(repository + PREFIX_BC + revision + resource);
	}

	protected void endTransaction(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public List<SubversionInfo> list(final Path resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final String uriPrefix = repository + PREFIX_BC + revision;
		return list(uriPrefix, resource, depth, withCustomProperties);
	}

	protected void merge(final SubversionInfo info, final UUID uuid) {
		final Path path = Path.create(repository.getPath() + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final Path srcResource, final Path targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final SubversionInfo info = info(srcResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			copy0(srcResource, info.getRevision(), targetResource, uuid);
			delete(srcResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void prepareContentUpload(final Path resource, final UUID uuid, final Revision revision) {
		final URI uri = URI.create(repository + PREFIX_VER + revision + resource);
		final URI href = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, href);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected UUID prepareTransaction() {
		final UUID uuid = UUID.randomUUID();
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createActivityRequest(uri);
		execute(request, HttpStatus.SC_CREATED);
		return uuid;
	}

	protected void propertiesRemove(final Path resource, final SubversionInfo info, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource);
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final Path resource, final SubversionInfo info, final UUID uuid, @Nullable final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource);
		final URI resourceUri = URI.create(repository + resource.getValue());

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
	protected void uploadWithProperties0(final Path resource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = prepareTransaction();
		try {
			final boolean exists = exists0(resource);
			final Path infoResource;
			if (exists) {
				infoResource = resource;
			} else {
				infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), resource);
			}

			final SubversionInfo info = info(infoResource, Revision.HEAD, false);
			final Revision revision = info.getRevision();
			checkout(uuid);
			setCommitMessage(uuid, revision, message);
			if (exists) {
				prepareContentUpload(resource, uuid, revision);
			}
			contentUpload(resource, info, uuid, content);
			propertiesSet(resource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}
}
