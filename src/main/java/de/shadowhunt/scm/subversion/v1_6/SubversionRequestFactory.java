package de.shadowhunt.scm.subversion.v1_6;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.scm.subversion.AbstractSubversionRequestFactory;

class SubversionRequestFactory extends AbstractSubversionRequestFactory {

	HttpUriRequest createMergeRequest(final URI uri, final String path) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop></merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	HttpUriRequest createUploadRequest(final URI uri, final InputStream content) {
		final HttpPut request = new HttpPut(uri);
		request.setEntity(new InputStreamEntity(content, -1));
		return request;
	}
}
