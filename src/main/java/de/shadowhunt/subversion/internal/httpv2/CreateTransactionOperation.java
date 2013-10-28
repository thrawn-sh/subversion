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
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Transaction;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class CreateTransactionOperation extends AbstractOperation<Transaction> {

	private static final HttpEntity entity;

	private static final String HEADER_NAME = "SVN-Txn-Name";

	static {
		final ContentType contentType = ContentType.create("application/vnd.svn-skel");
		entity = new StringEntity("( create-txn )", contentType);
	}

	private final UUID repositoryId;

	public CreateTransactionOperation(final URI repository, final UUID repositoryId) {
		super(repository);
		this.repositoryId = repositoryId;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, Resource.create("/!svn/me"));
		final HttpPost request = new HttpPost(uri);
		request.setEntity(entity);
		return request;
	}

	@Override
	protected Transaction processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
		final String transactionId = response.getFirstHeader(HEADER_NAME).getValue();
		EntityUtils.consumeQuietly(response.getEntity());
		return new Transaction(repositoryId, transactionId);
	}

}
