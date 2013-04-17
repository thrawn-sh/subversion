package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.AbstractSubversionRepository;
import de.shadowhunt.scm.subversion.Depth;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

public class SubversionRepository1_7 extends AbstractSubversionRepository<SubversionRequestFactory> {

	protected static final String PREFIX_RVR = "/!svn/rvr/";

	protected static final String PREFIX_TXN = "/!svn/txn/";

	protected static final String PREFIX_TXR = "/!svn/txr/";

	public SubversionRepository1_7(final URI repositoryRoot) {
		super(repositoryRoot, new SubversionRequestFactory());
	}

	protected void contentUpload(final String sanatizedResource, final SubversionInfo info, final String uuid, final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, resourceUri, info, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	protected String createMissingFolders(final String sanatizedResource, final String uuid) {
		final String[] resourceParts = sanatizedResource.split("/");

		String infoResource = "/";
		final StringBuilder partial = new StringBuilder();
		for (int i = 1; i < (resourceParts.length - 1); i++) {
			partial.append('/');
			partial.append(resourceParts[i]);

			final String partialResource = partial.toString();
			final URI uri = URI.create(repository + PREFIX_TXR + uuid + partialResource);
			final HttpUriRequest request = requestFactory.createMakeFolderRequest(uri);
			final HttpResponse response = execute(request, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
			final int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_METHOD_NOT_ALLOWED) {
				infoResource = partialResource;
			}
		}

		return infoResource;
	}

	@Override
	public void delete(final String resource, final String message) {
		final String sanatizedResource = sanatizeResource(resource);
		final String uuid = prepareCheckin();
		setCommitMessage(uuid, message);
		delete0(sanatizedResource, uuid);
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		merge(info, uuid);
	}

	protected void delete0(final String sanatizedResource, final String uuid) {
		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);
		final HttpUriRequest request = new HttpDelete(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String sanatizedResource = sanatizeResource(resource);
		final String uuid = prepareCheckin();
		setCommitMessage(uuid, message);
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		propertiesRemove(sanatizedResource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	protected InputStream download0(final String sanatizedResource, final long version) {
		final URI uri;
		if (version == HEAD_VERSION) {
			uri = URI.create(repository + sanatizedResource);
		} else {
			uri = URI.create(repository + PREFIX_RVR + version + sanatizedResource);
		}

		final HttpUriRequest request = requestFactory.createDownloadRequest(uri);
		final HttpResponse response = execute(request, false, HttpStatus.SC_OK);
		return getContent(response);
	}

	@Override
	protected SubversionInfo info0(final String sanatizedResource, final long version, final boolean withCustomProperties) {
		final URI uri;
		if (version == HEAD_VERSION) {
			uri = URI.create(repository + sanatizedResource);
		} else {
			uri = URI.create(repository + PREFIX_RVR + version + sanatizedResource);
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
		final URI uri = URI.create(repository + PREFIX_RVR + info.getVersion() + sanatizedResource);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.readList(in, withCustomProperties, (Depth.FILES != depth));
		} finally {
			closeQuiet(in);
		}
	}

	protected void merge(final SubversionInfo info, final String uuid) {
		final String path = repository.getPath() + PREFIX_TXN + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	protected String prepareCheckin() {
		final URI uri = URI.create(repository + "/!svn/me");

		final HttpUriRequest request = requestFactory.createPrepareRequest(uri, "( create-txn )");
		final HttpResponse response = execute(request, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesRemove(final String sanatizedResource, final SubversionInfo info, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String sanatizedResource, final SubversionInfo info, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);
		final URI resourceUri = URI.create(repository + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, resourceUri, info, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final String uuid, final String message) {
		final URI uri = URI.create(repository + PREFIX_TXN + uuid);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties) {
		final String uuid = prepareCheckin();
		final boolean exisits = exisits0(sanatizedResource);

		final String infoResource;
		if (exisits) {
			infoResource = sanatizedResource;
		} else {
			infoResource = createMissingFolders(sanatizedResource, uuid);
		}
		final SubversionInfo info = info0(infoResource, HEAD_VERSION, false);
		setCommitMessage(uuid, message);
		contentUpload(sanatizedResource, info, uuid, content);
		propertiesSet(sanatizedResource, info, uuid, properties);
		merge(info, uuid);
	}
}
