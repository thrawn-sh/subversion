package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class CheckoutOperationV1 extends AbstractOperation<Void> {

	protected final Resource resource, transaction;

	public CheckoutOperationV1(final URI repository, final Resource resource, final Resource transaction) {
		super(repository);
		this.resource = resource;
		this.transaction = transaction;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>");

		final URI transactionURI = URIUtils.createURI(repository, transaction);
		body.append(StringEscapeUtils.escapeXml(transactionURI.toASCIIString()));
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected Void processResponse(final HttpResponse response) {
		// TODO SC_CREATED
		return null;
	}
}
