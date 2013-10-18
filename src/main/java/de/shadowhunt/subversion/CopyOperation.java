package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class CopyOperation extends AbstractVoidOperation {

	protected final Resource source, target;

	public CopyOperation(final URI repository, final Resource source, final Resource target) {
		super(repository);
		this.source = source;
		this.target = target;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI sourceUri = URIUtils.createURI(repository, source);
		final URI targetUri = URIUtils.createURI(repository, target);
		final DavTemplateRequest request = new DavTemplateRequest("COPY", sourceUri);
		request.addHeader("Destination", targetUri.toASCIIString());
		request.addHeader("Depth", Depth.INFINITY.value);
		request.addHeader("Override", "T");
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
	}
}
