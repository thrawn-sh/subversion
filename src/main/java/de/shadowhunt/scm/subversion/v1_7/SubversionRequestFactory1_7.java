package de.shadowhunt.scm.subversion.v1_7;

import java.net.URI;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.scm.subversion.AbstractSubversionRequestFactory;

class SubversionRequestFactory1_7 extends AbstractSubversionRequestFactory {

	private static final ContentType CONTENT_TYPE_SVNSKEL = ContentType.create("application/vnd.svn-skel");

	HttpUriRequest createPrepareRequest(final URI uri, final String content) {
		final HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(content, CONTENT_TYPE_SVNSKEL));
		return request;
	}
}
