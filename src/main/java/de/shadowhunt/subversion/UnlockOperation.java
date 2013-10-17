package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class UnlockOperation extends AbstractOperation<Void> {

	protected final Resource resource;

	protected final String lockToken;

	protected final boolean force;

	public UnlockOperation(final URI repository, final Resource resource, final String lockToken, final boolean force) {
		super(repository);
		this.resource = resource;
		this.lockToken = lockToken;
		this.force = force;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK", uri);
		request.addHeader("Lock-Token", "<" + lockToken + ">");
		if (force) {
			request.addHeader("X-SVN-Options", "lock-break");
		}
		return request;
	}

	@Override
	protected Void processResponse(final HttpResponse response) {
		// TODO SC_NO_CONTENT
		return null;
	}
}