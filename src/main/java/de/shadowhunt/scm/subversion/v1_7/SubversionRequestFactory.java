package de.shadowhunt.scm.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.scm.subversion.AbstractSubversionRequestFactory;
import de.shadowhunt.scm.subversion.SubversionInfo;

class SubversionRequestFactory extends AbstractSubversionRequestFactory {

	private static final ContentType CONTENT_TYPE_SVNSKEL = ContentType.create("application/vnd.svn-skel");

	HttpUriRequest createMergeRequest(final URI uri, final String path, final SubversionInfo info) {
		final DavTemplateRequest request = new DavTemplateRequest("MERGE");
		request.setURI(uri);

		final String path0 = StringEscapeUtils.escapeXml(info.getRelativePath());

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<merge xmlns=\"DAV:\"><source><href>");
		body.append(StringEscapeUtils.escapeXml(path));
		body.append("</href></source><no-auto-merge/><no-checkout/><prop><checked-in/><version-name/><resourcetype/><creationdate/><creator-displayname/></prop>");
		final String token = info.getLockToken();
		if (token != null) {
			request.addHeader("X-SVN-Options", "release-locks");

			body.append("<S:lock-token-list xmlns:S=\"svn:\"><S:lock><S:lock-path>");
			body.append(path0);
			body.append("</S:lock-path>");
			body.append("<S:lock-token>");
			body.append(token);
			body.append("</S:lock-token></S:lock></S:lock-token-list>");
		}
		body.append("</merge>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	HttpUriRequest createPrepareRequest(final URI uri, final String content) {
		final HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(content, CONTENT_TYPE_SVNSKEL));
		return request;
	}

	HttpUriRequest createUploadRequest(final URI uri, final URI aaa, final SubversionInfo info, final InputStream content) {
		final HttpPut request = new HttpPut(uri);

		final String token = info.getLockToken();
		if (token != null) {
			request.addHeader("If", "<" + aaa + "> (<" + token + ">)");
		}
		request.setEntity(new InputStreamEntity(content, -1));
		return request;
	}
}
