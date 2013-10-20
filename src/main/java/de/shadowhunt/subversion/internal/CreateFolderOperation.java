package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public class CreateFolderOperation extends AbstractOperation<Boolean> {

	protected final Resource resource;

	public CreateFolderOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		return new DavTemplateRequest("MKCOL", uri);
	}

	@Override
	protected Boolean processResponse(final HttpResponse response) {
		check(response, /* created */HttpStatus.SC_CREATED, /* existed */HttpStatus.SC_METHOD_NOT_ALLOWED);
		final int status = getStatusCode(response);
		return (status == HttpStatus.SC_CREATED);
	}

}
