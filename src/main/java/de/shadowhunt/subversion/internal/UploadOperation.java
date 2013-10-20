package de.shadowhunt.subversion.internal;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.io.InputStream;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

public class UploadOperation extends AbstractVoidOperation {

	protected static final int PREFIX = 4; // /$svn/{baseline}/{id}/

	protected static final long STREAM_WHOLE_CONTENT = -1L;

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
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}
}
