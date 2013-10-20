package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class LockOperation extends AbstractVoidOperation {

	protected final Resource resource;

	protected final boolean steal;

	public LockOperation(final URI repository, final Resource resource, final boolean steal) {
		super(repository);
		this.resource = resource;
		this.steal = steal;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("LOCK", uri);
		if (steal) {
			request.addHeader("X-SVN-Options", "lock-steal");
		}

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<lockinfo xmlns=\"DAV:\"><lockscope><exclusive/></lockscope><locktype><write/></locktype></lockinfo>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);
	}

}
