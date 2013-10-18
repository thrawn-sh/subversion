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
package de.shadowhunt.subversion;

import java.net.URI;

import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Basic class for all SubversionRequestFactories
 */
public abstract class AbstractRequestFactory {

	/**
	 * Perform authentication, with a cheap http request without any payload, the http connection will be authenticated afterwards
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} performing the authentication
	 */
	public HttpUriRequest createAuthRequest(final URI uri) {
		final HttpOptions request = new HttpOptions(uri);
		request.addHeader("Keep-Alive", "");
		return request;
	}
}
