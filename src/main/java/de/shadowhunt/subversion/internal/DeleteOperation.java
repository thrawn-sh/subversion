package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

public class DeleteOperation extends AbstractVoidOperation {

	protected final Resource resource;

	public DeleteOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		return new HttpDelete(uri);
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_NO_CONTENT);
	}
}
