package de.shadowhunt.subversion.v1_7;

import java.net.URI;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.AbstractRequestFactory;

/**
 * {@link RequestFactory1_7} creates http requests suitable for subversion 1.7.X server
 */
public class RequestFactory1_7 extends AbstractRequestFactory {

	protected static final ContentType CONTENT_TYPE_SVNSKEL = ContentType.create("application/vnd.svn-skel");

	protected RequestFactory1_7() {
		// prevent global instantiation
	}

	/**
	 * Prepare the server to accept a following content upload
	 * @param uri absolute {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} preparing the server to accept the following content upload
	 */
	public HttpUriRequest createPrepareRequest(final URI uri) {
		final HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity("( create-txn )", CONTENT_TYPE_SVNSKEL));
		return request;
	}
}
