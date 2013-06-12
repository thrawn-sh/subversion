package de.shadowhunt.subversion.v1_6;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.subversion.AbstractRepository;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.Path;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;

/**
 * {@link Repository1_6} supports subversion servers of version 1.6.X
 */
public class Repository1_6 extends AbstractRepository<RequestFactory1_6> {

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	protected Repository1_6(final URI repository, final boolean trustServerCertificat) {
		super(repository, trustServerCertificat, new RequestFactory1_6());
	}

	protected void checkout(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_VCC + "default");
		final URI href = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, href);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void contentUpload(final Path resource, final InfoEntry info, final UUID uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final Path srcResource, final Revision srcRevision, final Path targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final InfoEntry info = info(srcResource, srcRevision, false);
			final Revision realSourceRevision = info.getRevision();
			setCommitMessage(uuid, realSourceRevision, message);
			createMissingFolders(PREFIX_WRK, uuid.toString(), targetResource.getParent());
			copy0(srcResource, realSourceRevision, targetResource, uuid);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	protected void copy0(final Path srcResource, final Revision srcRevision, final Path targetResource, final UUID uuid) {
		final URI src = URI.create(repository + PREFIX_BC + srcRevision + srcResource.getValue());
		final URI target = URI.create(repository + PREFIX_WRK + uuid + targetResource.getValue());
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void createFolder(final Path resource, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final UUID uuid = prepareTransaction();
		try {
			final Path infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), resource);
			final InfoEntry info = info(infoResource, Revision.HEAD, false);
			checkout(uuid);
			setCommitMessage(uuid, info.getRevision(), message);
			merge(info, uuid);
		} finally {
			endTransaction(uuid);
		}
	}

	@Override
	public void delete(final Path resource, final String message) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
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
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource.getValue());
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final Path resource, final String message, final ResourceProperty... properties) {
		final InfoEntry info = info(resource, Revision.HEAD, false);
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
	public URI downloadURI(final Path resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URI.create(repository + resource.getValue());
		}
		return URI.create(repository + PREFIX_BC + revision + resource.getValue());
	}

	protected void endTransaction(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public List<InfoEntry> list(final Path resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreateRevision(resource, revision);
		final String uriPrefix = repository + PREFIX_BC + concreateRevision;
		return list(uriPrefix, resource, depth, withCustomProperties);
	}

	protected void merge(final InfoEntry info, final UUID uuid) {
		final Path path = Path.create(repository.getPath() + PREFIX_ACT + uuid);
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final Path srcResource, final Path targetResource, final String message) {
		final UUID uuid = prepareTransaction();
		try {
			final InfoEntry info = info(srcResource, Revision.HEAD, false);
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
		final URI uri = URI.create(repository + PREFIX_VER + revision + resource.getValue());
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

	protected void propertiesRemove(final Path resource, final InfoEntry info, final UUID uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final Path resource, final InfoEntry info, final UUID uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + resource.getValue());
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
	protected void upload0(final Path resource, final String message, @Nullable final InputStream content, final ResourceProperty... properties) {
		final UUID uuid = prepareTransaction();
		try {
			final boolean exists = exists(resource, Revision.HEAD);
			final Path infoResource;
			if (exists) {
				infoResource = resource;
			} else {
				infoResource = createMissingFolders(PREFIX_WRK, uuid.toString(), resource.getParent());
			}

			final InfoEntry info = info(infoResource, Revision.HEAD, false);
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
