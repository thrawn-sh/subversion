package de.shadowhunt.scm.subversion.v1_6;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.AbstractSubversionRepository;
import de.shadowhunt.scm.subversion.Depth;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

public class SubversionRepository1_6 extends AbstractSubversionRepository<SubversionRequestFactory> {

	protected static final String PREFIX_ACT = "/!svn/act/";

	protected static final String PREFIX_BC = "/!svn/bc/";

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	protected static final String PREFIX_WRK = "/!svn/wrk/";

	public SubversionRepository1_6(final URI repository) {
		super(repository, new SubversionRequestFactory());
	}

	protected void contentUpload(final String sanatizedResource, final SubversionInfo info, final UUID uuid, final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, resourceUri, info, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	protected String createMissingFolders(final String sanatizedResource, final UUID uuid) {
		final String[] resourceParts = sanatizedResource.split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(repository + PREFIX_WRK + uuid + partialResource);
			final HttpUriRequest request = requestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = execute(request, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
			final int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return infoResource;
	}

	protected void createTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);

		final HttpUriRequest request = requestFactory.createActivityRequest(uri);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void delete(final String resource, final String message) {
		final String sanatizedResource = sanatizeResource(resource);
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		final int version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(sanatizedResource, uuid, version);
			delete(sanatizedResource, uuid);
			merge(info, uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	protected void delete(final String sanatizedResource, final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);
		final HttpUriRequest request = new HttpDelete(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String sanatizedResource = sanatizeResource(resource);
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		final int version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(sanatizedResource, uuid, version);
			propertiesRemove(sanatizedResource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	protected void deleteTemporyStructure(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_ACT + uuid);
		final HttpUriRequest request = new HttpDelete(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	protected InputStream download0(final String sanatizedResource, final int version) {
		final URI uri;
		if (version == HEAD_VERSION) {
			uri = URI.create(repository + sanatizedResource);
		} else {
			uri = URI.create(repository + PREFIX_BC + version + sanatizedResource);
		}

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	protected SubversionInfo info0(final String sanatizedResource, final int version, final boolean withCustomProperties) {
		final URI uri;
		if (version == HEAD_VERSION) {
			uri = URI.create(repository + sanatizedResource);
		} else {
			uri = URI.create(repository + PREFIX_BC + version + sanatizedResource);
		}

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, Depth.EMPTY);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.read(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	@Override
	public List<SubversionInfo> list(final String resource, final Depth depth, final boolean withCustomProperties) {
		final String sanatizedResource = sanatizeResource(resource);
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		final URI uri = URI.create(repository + PREFIX_BC + info.getVersion() + sanatizedResource);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.readList(in, withCustomProperties, (Depth.FILES != depth));
		} finally {
			closeQuiet(in);
		}
	}

	protected void merge(final SubversionInfo info, final UUID uuid) {
		final String path = repository.getPath() + PREFIX_ACT + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	protected void prepareCheckin(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_VCC + "default");

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void prepareContentUpload(final String sanatizedResource, final UUID uuid, final int version) {
		final URI uri = URI.create(repository + PREFIX_VER + version + sanatizedResource);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		execute(request, HttpStatus.SC_CREATED);
	}

	protected void propertiesRemove(final String sanatizedResource, final SubversionInfo info, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String sanatizedResource, final SubversionInfo info, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final int version, final String message) {
		final URI uri = URI.create(repository + PREFIX_WBL + uuid + "/" + version);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final String sanatizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			final boolean exisits = exisits0(sanatizedResource);
			final String infoResource;
			if (exisits) {
				infoResource = sanatizedResource;
			} else {
				infoResource = createMissingFolders(sanatizedResource, uuid);
			}

			final SubversionInfo info = info0(infoResource, HEAD_VERSION, false);
			final int version = info.getVersion();
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			if (exisits) {
				prepareContentUpload(sanatizedResource, uuid, version);
			}
			contentUpload(sanatizedResource, info, uuid, content);
			propertiesSet(sanatizedResource, info, uuid, properties);
			merge(info, uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}
}
