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
package de.shadowhunt.http.client.methods;

import javax.annotation.Nullable;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/** Entity enclosing HTTP request for WebDav */
public final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

	private final String method;

	/**
	 * Create a new {@link DavTemplateRequest} with default depth-level
	 *
	 * @param method HTTP method name
	 */
	public DavTemplateRequest(final String method) {
		this.method = method;
	}

	/**
	 * Create a new {@link DavTemplateRequest}
	 *
	 * @param method HTTP method name
	 * @param depth  depth-level of the request
	 */
	public DavTemplateRequest(final String method, @Nullable final Header... headers) {
		this(method);
		for (Header header : headers) {
			addHeader(header);
		}
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return method + " " + getURI() + " " + getProtocolVersion();
	}
}
