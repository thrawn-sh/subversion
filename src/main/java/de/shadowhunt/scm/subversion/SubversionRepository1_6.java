package de.shadowhunt.scm.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

class SubversionRepository1_6 extends SubversionRepository {

	String repositoryRoot = null;

	SubversionRepository1_6(final HttpClient client, final URI repository) {
		super(client, repository);

		triggerAuthentication();
	}

	SubversionRepository1_6(final URI repository, final String username, final String password, @Nullable final String workstation) {
		this(createClient(repository, username, password, workstation), repository);
	}

	@Override
	void createWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			final String infoResource = createMissingFolders(sanatizedResource, uuid);
			final SubversionInfo info = info0(infoResource, false);
			final long version = info.getVersion();
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			contentUpload(sanatizedResource, uuid, content);
			propertiesSet(sanatizedResource, uuid, properties);
			merge(repository + PREFIX_ACT + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	@Override
	public void delete(final String resource, final String message) {
		final String sanatizedResource = sanatizeResource(resource);
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info0(sanatizedResource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(sanatizedResource, uuid, version);
			delete(sanatizedResource, uuid);
			merge(repository + PREFIX_ACT + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		final String sanatizedResource = sanatizeResource(resource);
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info0(sanatizedResource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(sanatizedResource, uuid, version);
			propertiesRemove(sanatizedResource, uuid, properties);
			merge(repository + PREFIX_ACT + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	void prepareCheckin(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_VCC + "default");

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT
				+ uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	void prepareContentUpload(final String sanatizedResource, final UUID uuid, final long version) {
		final URI uri = URI.create(repository + PREFIX_VER + version + sanatizedResource);

		final HttpUriRequest request = SubversionRequestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT
				+ uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	void propertiesRemove(final String sanatizedResource, final UUID uuid, final SubversionProperty[] properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);

		final HttpUriRequest request = SubversionRequestFactory.createRemovePropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	void propertiesSet(final String sanatizedResource, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);

		final HttpUriRequest request = SubversionRequestFactory.createSetPropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	void setCommitMessage(final UUID uuid, final long version, final String message) {
		final URI uri = URI.create(repository + PREFIX_WBL + uuid + "/" + version);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = SubversionRequestFactory.createCommitMessageRequest(uri, trimmedMessage);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	void uploadWithProperties0(final String sanatizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();
		final SubversionInfo info = info0(sanatizedResource, false);
		final long version = info.getVersion();

		createTemporyStructure(uuid);
		try {
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			prepareContentUpload(sanatizedResource, uuid, version);
			propertiesSet(sanatizedResource, uuid, properties);

			if (content != null) {
				contentUpload(sanatizedResource, uuid, content);
			}
			merge(repository + PREFIX_ACT + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}
}
