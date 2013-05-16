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
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

class SubversionRepository1_6 extends AbstractSubversionRepository<SubversionRequestFactory1_6> {

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	SubversionRepository1_6(final URI repository, final boolean trustServerCertificat) {
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

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, resourceUri, info, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void delete(final String resource, final String message) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		final int revision = info.getRevision();

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
		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		final int revision = info.getRevision();

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
	protected InputStream download0(final String normalizedResource, final int revision) {
		final URI uri;
		if (revision == HEAD_VERSION) {
			uri = URI.create(repository + normalizedResource);
		} else {
			uri = URI.create(repository + PREFIX_BC + revision + normalizedResource);
		}

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	protected URI downloadURI0(final String normalizedResource, final int revision) {
		if (revision == HEAD_VERSION) {
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
	public List<SubversionInfo> list(final String resource, final Depth depth, final boolean withCustomProperties) {
		final String normalizedResource = normalizeResource(resource);
		final SubversionInfo info = info0(normalizedResource, HEAD_VERSION, false);
		final String uriPrefix = repository + PREFIX_BC + info.getRevision();
		return list(uriPrefix, normalizedResource, depth, withCustomProperties);
	}

	protected void merge(final SubversionInfo info, final UUID uuid) {
		final String path = repository.getPath() + PREFIX_ACT + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	protected void prepareContentUpload(final String normalizedResource, final UUID uuid, final int revision) {
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

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String normalizedResource, final SubversionInfo info, final UUID uuid, @Nullable final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + normalizedResource);
		final URI resourceUri = URI.create(repository + normalizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final int revision, final String message) {
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

			final SubversionInfo info = info0(infoResource, HEAD_VERSION, false);
			final int revision = info.getRevision();
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
