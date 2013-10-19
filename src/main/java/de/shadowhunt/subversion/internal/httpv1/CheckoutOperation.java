package de.shadowhunt.subversion.internal.httpv1;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.AbstractVoidOperation;
import java.net.URI;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class CheckoutOperation extends AbstractVoidOperation {

	protected final Resource resource, transaction;

	public CheckoutOperation(final URI repository, final Resource resource, final Resource transaction) {
		super(repository);
		this.resource = resource;
		this.transaction = transaction;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>");

		final URI transactionURI = createURI(repository, transaction);
		body.append(StringEscapeUtils.escapeXml(transactionURI.toASCIIString()));
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
	}
}
