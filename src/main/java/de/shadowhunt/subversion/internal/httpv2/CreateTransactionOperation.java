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
package de.shadowhunt.subversion.internal.httpv2;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.TransactionImpl;
import de.shadowhunt.subversion.internal.util.URIUtils;

class CreateTransactionOperation extends AbstractOperation<TransactionImpl> {

	private static final HttpEntity ENTITY;

	private static final String HEADER_NAME = "SVN-Txn-Name";

	static {
		final ContentType contentType = ContentType.create("application/vnd.svn-skel");
		ENTITY = new StringEntity("( create-txn )", contentType);
	}

	private final Resource resource;

	CreateTransactionOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final HttpPost request = new HttpPost(uri);
		request.setEntity(ENTITY);
		return request;
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return HttpStatus.SC_CREATED == statusCode;
	}

	@Override
	protected TransactionImpl processResponse(final HttpResponse response) {
		final String transactionId = response.getFirstHeader(HEADER_NAME).getValue();
		EntityUtils.consumeQuietly(response.getEntity());
		return new TransactionImpl(transactionId);
	}
}
