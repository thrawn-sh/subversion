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
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.TransactionImpl;
import de.shadowhunt.subversion.internal.URIUtils;

class CreateTransactionOperation extends AbstractOperation<TransactionImpl> {

	private final Resource resource;

	private final UUID transactionId = UUID.randomUUID();

	CreateTransactionOperation(final URI repository, final Resource resource) {
		super(repository);
		this.resource = resource;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource, Resource.create(transactionId.toString()));
		return new DavTemplateRequest("MKACTIVITY", uri);
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return HttpStatus.SC_CREATED == statusCode;
	}

	@Override
	protected TransactionImpl processResponse(final HttpResponse response) {
		EntityUtils.consumeQuietly(response.getEntity());
		return new TransactionImpl(transactionId.toString());
	}
}
