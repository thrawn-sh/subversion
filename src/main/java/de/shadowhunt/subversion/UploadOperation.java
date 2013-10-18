package de.shadowhunt.subversion;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

import de.shadowhunt.util.URIUtils;

public class UploadOperation extends AbstractOperation<Void> {

	protected static final long STREAM_WHOLE_CONTENT = -1L;

	protected static final int PREFIX = 4; // /$svn/{baseline}/{id}/

	protected final InputStream content;

	protected final String lock;

	protected final Resource resource;

	public UploadOperation(final URI repository, final Resource resource, final String lock, final InputStream content) {
		super(repository);
		this.resource = resource;
		this.lock = lock;
		this.content = content;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final HttpPut request = new HttpPut(uri);
		if (lock != null) {
			final URI lockTarget = URIUtils.createURI(repository, resource.subResource(PREFIX));
			request.addHeader("If", "<" + lockTarget.toASCIIString() + "> (<" + lock + ">)");
		}

		request.setEntity(new InputStreamEntity(content, STREAM_WHOLE_CONTENT));
		return request;
	}

	@Override
	protected Void processResponse(final HttpResponse response) {
		// TODO HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT
		return null;
	}
}
