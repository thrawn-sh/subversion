/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion.v1_7;

import de.shadowhunt.subversion.AbstractRequestFactory;
import java.net.URI;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

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
