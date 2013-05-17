package de.shadowhunt.scm.subversion.v1_6;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.scm.subversion.AbstractSubversionRequestFactory;

/**
 * {@code SubversionRequestFactory1_6} creates http requests suitable for subversion 1.6.X server
 */
public class SubversionRequestFactory1_6 extends AbstractSubversionRequestFactory {

	protected SubversionRequestFactory1_6() {
		// prevent global instantiation
	}

	/**
	 * @param uri absolute {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} representing the request
	 */
	public HttpUriRequest createMergeRequest(final URI uri, final String path) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop></merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

}
