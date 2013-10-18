package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.util.URIUtils;

public class MergeOperation extends AbstractVoidOperation {

	protected static final int PREFIX = 4; // /$svn/{baseline}/{id}/

	protected final String lock;

	protected final Resource resource;

	public MergeOperation(final URI repository, final Resource resource, final String lock) {
		super(repository);
		this.resource = resource;
		this.lock = lock;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("MERGE", uri);
		request.addHeader("X-SVN-Options", "release-locks");

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(resource.getValue()));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
		if (lock != null) {
			body.append("<S:lock-token-list xmlns:S=\"svn:\"><S:lock><S:lock-path>");
			final Resource plain = resource.subResource(PREFIX);
			body.append(StringEscapeUtils.escapeXml(plain.getValueWithoutLeadingSeparator()));
			body.append("</S:lock-path>");
			body.append("<S:lock-token>");
			body.append(lock);
			body.append("</S:lock-token></S:lock></S:lock-token-list>");
		}
		body.append("</merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);
	}

}
