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
package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class UnlockOperation extends AbstractVoidOperation {

	protected final boolean force;

	protected final String lockToken;

	protected final Resource resource;

	public UnlockOperation(final URI repository, final Resource resource, final String lockToken, final boolean force) {
		super(repository);
		this.resource = resource;
		this.lockToken = lockToken;
		this.force = force;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("UNLOCK", uri);
		request.addHeader("Lock-Token", "<" + lockToken + ">");
		if (force) {
			request.addHeader("X-SVN-Options", "lock-break");
		}
		return request;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_NO_CONTENT);
	}
}
