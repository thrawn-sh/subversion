package de.shadowhunt.scm.subversion.v1_6;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.SubversionInfo;
import de.shadowhunt.scm.subversion.SubversionProperty;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class SubversionRepository1_6 extends SubversionRepository<SubversionRequestFactory> {

	protected static final String PREFIX_VCC = "/!svn/vcc/";

	protected static final String PREFIX_VER = "/!svn/ver/";

	protected static final String PREFIX_WBL = "/!svn/wbl/";

	public SubversionRepository1_6(final HttpClient client, final URI repository) {
		super(client, repository, new SubversionRequestFactory());

		triggerAuthentication();
	}

	public SubversionRepository1_6(final URI repository, final String username, final String password, @Nullable final String workstation) {
		this(createClient(repository, username, password, workstation), repository);
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

	protected void prepareCheckin(final UUID uuid) {
		final URI uri = URI.create(repository + PREFIX_VCC + "default");

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	protected void prepareContentUpload(final String sanatizedResource, final UUID uuid, final long version) {
		final URI uri = URI.create(repository + PREFIX_VER + version + sanatizedResource);

		final HttpUriRequest request = requestFactory.createCheckoutRequest(uri, repository + PREFIX_ACT + uuid);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_CREATED);
	}

	protected void propertiesRemove(final String sanatizedResource, final UUID uuid, final SubversionProperty[] properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final String sanatizedResource, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_WRK + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final long version, final String message) {
		final URI uri = URI.create(repository + PREFIX_WBL + uuid + "/" + version);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void uploadWithProperties0(final String sanatizedResource, final String message, @Nullable final InputStream content, @Nullable final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			final String infoResource = createMissingFolders(sanatizedResource, uuid);
			final boolean updateExisiting = infoResource.equals(sanatizedResource);
			final SubversionInfo info = info0(infoResource, false);
			final long version = info.getVersion();
			prepareCheckin(uuid);
			setCommitMessage(uuid, version, message);
			if (updateExisiting) {
				prepareContentUpload(sanatizedResource, uuid, version);
			}
			contentUpload(sanatizedResource, uuid, content);
			propertiesSet(sanatizedResource, uuid, properties);
			merge(repository + PREFIX_ACT + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}
}
