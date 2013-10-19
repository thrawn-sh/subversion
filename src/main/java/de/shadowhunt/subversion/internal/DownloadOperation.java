package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;
import java.io.InputStream;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

public class DownloadOperation extends AbstractOperation<InputStream> {

	protected final Resource resource;

	public DownloadOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = createURI(repository, resource);
		return new HttpGet(uri);
	}

	@Override
	protected InputStream processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);
		return getContent(response);
	}

}
