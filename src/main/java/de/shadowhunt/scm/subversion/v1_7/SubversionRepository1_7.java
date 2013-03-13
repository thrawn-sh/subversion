package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.AbstractSubversionRepository;
import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;

public class SubversionRepository1_7 extends AbstractSubversionRepository<SubversionRequestFactory> {

	protected static final String PREFIX_RVR = "/!svn/rvr/";

	protected static final String PREFIX_TXN = "/!svn/txn/";

	protected static final String PREFIX_TXR = "/!svn/txr/";

	public SubversionRepository1_7(final HttpClient client, final URI repositoryRoot) {
		super(client, repositoryRoot, new SubversionRequestFactory());

		triggerAuthentication();
	}

	public SubversionRepository1_7(final URI repositoryRoot, final String username, final String password, @Nullable final String workstation) {
		this(createClient(repositoryRoot, username, password, workstation), repositoryRoot);
	}

	protected void contentUpload(final String sanatizedResource, final String uuid, final InputStream content) {
		if (content == null) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	protected String createMissingFolders(final String sanatizedResource, final String uuid) {
		final String[] resourceParts = sanatizedResource.split("/");

		int existing = 1;
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
				existing++;
			}
		}

		if (existing == (resourceParts.length - 1)) {
			return sanatizedResource;
		}
		return infoResource;
	}

	@Override
	public void delete(final String resource, final String message) {
		final String sanatizedResource = sanatizeResource(resource);
		final String uuid = prepareCheckin();
		setCommitMessage(uuid, message);
		delete0(sanatizedResource, uuid);
		merge(uuid);
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
		propertiesRemove(sanatizedResource, uuid, properties);
		merge(uuid);
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

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, 0);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.read(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	@Override
	public List<SubversionInfo> list(final String resource, final int depth, final boolean withCustomProperties) {
		final String sanatizedResource = sanatizeResource(resource);
		final SubversionInfo info = info0(sanatizedResource, HEAD_VERSION, false);
		final URI uri = URI.create(repository + PREFIX_RVR + info.getVersion() + sanatizedResource);

		final HttpUriRequest request = requestFactory.createInfoRequest(uri, depth);
		final HttpResponse response = execute(request, false, HttpStatus.SC_MULTI_STATUS);

		final InputStream in = getContent(response);
		try {
			return SubversionInfo.readList(in, withCustomProperties);
		} finally {
			closeQuiet(in);
		}
	}

	protected void merge(final String uuid) {
		final String path = repository.getPath() + PREFIX_TXN + uuid;
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path);
		execute(request, HttpStatus.SC_OK);
	}

	protected String prepareCheckin() {
		final URI uri = URI.create(repository + "/!svn/me");

		final HttpUriRequest request = requestFactory.createPostRequest(uri, "( create-txn )");
		final HttpResponse response = execute(request, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesRemove(final String sanatizedResource, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String sanatizedResource, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, filtered);
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
		createMissingFolders(sanatizedResource, uuid);
		setCommitMessage(uuid, message);
		contentUpload(sanatizedResource, uuid, content);
		propertiesSet(sanatizedResource, uuid, properties);
		merge(uuid);
	}
}
