package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.util.URIUtils;

public class ExistsOperation extends AbstractOperation<Boolean> {

	protected final Resource resource;

	public ExistsOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		return new HttpHead(uri);
	}

	@Override
	protected Boolean processResponse(final HttpResponse response) {
		check(response, /* found */HttpStatus.SC_OK, /* not found */HttpStatus.SC_NOT_FOUND);

		final int statusCode = getStatusCode(response);
		return (statusCode == HttpStatus.SC_OK);
	}

}
