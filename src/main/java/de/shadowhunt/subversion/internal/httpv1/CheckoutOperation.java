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
package de.shadowhunt.subversion.internal.httpv1;

import java.net.URI;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.AbstractVoidOperation;
import de.shadowhunt.subversion.internal.util.URIUtils;

class CheckoutOperation extends AbstractVoidOperation {

	private final Resource resource;

	private final Resource transaction;

	CheckoutOperation(final URI repository, final Resource resource, final Resource transaction) {
		super(repository);
		this.resource = resource;
		this.transaction = transaction;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<checkout xmlns=\"DAV:\"><activity-set><href>");

		final URI transactionURI = URIUtils.createURI(repository, transaction);
		body.append(StringEscapeUtils.escapeXml(transactionURI.toASCIIString()));
		body.append("</href></activity-set><apply-to-version/></checkout>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return HttpStatus.SC_CREATED == statusCode;
	}
}
