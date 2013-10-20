package de.shadowhunt.subversion.internal;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;
import java.net.URI;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class CommitMessageOperation extends AbstractVoidOperation {

	protected final String message;

	protected final Resource resource;

	public CommitMessageOperation(final URI repository, final Resource resource, final String message) {
		super(repository);
		this.resource = resource;
		this.message = message;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<propertyupdate xmlns=\"DAV:\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\"><set><prop><S:log>");
		body.append(StringEscapeUtils.escapeXml(message));
		body.append("</S:log></prop></set></propertyupdate>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_MULTI_STATUS);
	}

}
