package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.SubversionProperty;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class SubversionRepository1_7 extends SubversionRepository<SubversionRequestFactory> {

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
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void delete(final String resource, final String message) {
		final String sanatizedResource = sanatizeResource(resource);
		final String uuid = prepareCheckin();
		setCommitMessage(uuid, message);
		deleteAAA(sanatizedResource, uuid);
		merge(repository.getPath() + PREFIX_TXN + uuid);
	}

	protected void deleteAAA(final String sanatizedResource, final String uuid) {
		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);
		delete(uri);
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String sanatizedResource = sanatizeResource(resource);
		final String uuid = prepareCheckin();
		setCommitMessage(uuid, message);
		propertiesRemove(sanatizedResource, uuid, properties);
		merge(repository.getPath() + PREFIX_TXN + uuid);
	}

	protected void propertiesRemove(final String sanatizedResource, final String uuid, final SubversionProperty[] properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	protected String prepareCheckin() {
		final URI uri = URI.create(repository + "/!svn/me");

		final HttpUriRequest request = requestFactory.createPostRequest(uri, "( create-txn )");
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesSet(final String sanatizedResource, final String uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final String uuid, final String message) {
		final URI uri = URI.create(repository + PREFIX_TXN + uuid);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties) {
		final String uuid = prepareCheckin();
		//				createMissingFolders(sanatizedResource, uuid);
		setCommitMessage(uuid, message);
		contentUpload(sanatizedResource, uuid, content);
		propertiesSet(sanatizedResource, uuid, properties);
		merge(repository.getPath() + PREFIX_TXN + uuid);
	}
}
