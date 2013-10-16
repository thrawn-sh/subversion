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
package de.shadowhunt.subversion.v1_6;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.AbstractRequestFactory;

/**
 * {@link RequestFactory1_6} creates http requests suitable for subversion 1.6.X server
 */
public class RequestFactory1_6 extends AbstractRequestFactory {

	protected RequestFactory1_6() {
		// prevent global instantiation
	}

	/**
	 * Create a new temporary directory for a transaction
	 * @param uri {@link URI} to perform the request against
	 * @return {@link HttpUriRequest} creating the new temporary directory for the transaction
	 */
	public HttpUriRequest createActivityRequest(final URI uri) {
		return new DavTemplateRequest("MKACTIVITY", uri);
	}

	/**
	 * Perform a server side checkout of a resource
	 * @param uri {@link URI} to perform the request against
	 * @param href absolute resource-path relative to the repository root
	 * @return {@link HttpUriRequest} performing a server side checkout of the resource
	 */
	public HttpUriRequest createCheckoutRequest(final URI uri, final URI href) {
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>");
		body.append(StringEscapeUtils.escapeXml(href.toString()));
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}
}
