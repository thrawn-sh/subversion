package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.scm.subversion.SubversionProperty;
import de.shadowhunt.scm.subversion.SubversionRepository;

public class SubversionRepository1_7 extends SubversionRepository<SubversionRequestFactory1_7> {

	public SubversionRepository1_7(final HttpClient client, final URI repositoryRoot) {
		super(client, repositoryRoot, new SubversionRequestFactory1_7());

		triggerAuthentication();
	}

	public SubversionRepository1_7(final URI repositoryRoot, final String username, final String password, @Nullable final String workstation) {
		this(createClient(repositoryRoot, username, password, workstation), repositoryRoot);
	}

	@Override
	protected void createWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties) {
		final UUID uuid = UUID.randomUUID();

		createTemporyStructure(uuid);
		try {
			//			prepareCheckin();
			createMissingFolders(sanatizedResource, uuid);
			setCommitMessage(uuid, message);
			contentUpload(sanatizedResource, uuid, content);
			propertiesSet(sanatizedResource, uuid, properties);
			merge(repository.getPath() + PREFIX_TXN + uuid);
		} finally {
			deleteTemporyStructure(uuid);
		}
	}

	//	protected void prepareCheckin() {
	//		final URI uri = URI.create(repository + "/!svn/me");
	//
	//		final HttpUriRequest request = requestFactory.createPostRequest(uri, "( create-txn )");
	//		final HttpResponse response = execute(request);
	//		ensureResonse(response, HttpStatus.SC_CREATED);
	//	}

	protected void propertiesSet(final String sanatizedResource, final UUID uuid, final SubversionProperty... properties) {
		final SubversionProperty[] filtered = SubversionProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXN + uuid + sanatizedResource);

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, filtered);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final UUID uuid, final String message) {
		final URI uri = URI.create(repository + PREFIX_WRK + uuid);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		final HttpResponse response = execute(request);
		ensureResonse(response, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	public void delete(final String resource, final String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteProperties(final String resource, final String message, final SubversionProperty... properties) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void uploadWithProperties0(final String sanatizedResource, final String message, final InputStream content, final SubversionProperty... properties) {
		// TODO Auto-generated method stub

	}

}
