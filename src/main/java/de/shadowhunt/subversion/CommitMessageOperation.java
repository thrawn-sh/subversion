package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class CommitMessageOperation extends AbstractOperation<Void> {

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
	protected Void processResponse(final HttpResponse response) {
		// TODO HttpStatus.SC_MULTI_STATUS
		return null;
	}

}
