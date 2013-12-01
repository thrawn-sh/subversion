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

import javax.annotation.Nullable;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class DeleteOperation extends AbstractVoidOperation {

	private final Info info;

	private final Resource resource;

	public DeleteOperation(final URI repository, final Resource resource, @Nullable final Info info) {
		super(repository);
		this.resource = resource;
		this.info = info;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final HttpUriRequest request = new HttpDelete(uri);
		if ((info != null) && info.isLocked()) {
			final URI lockTarget = URIUtils.createURI(repository, info.getResource());
			request.addHeader("If", '<' + lockTarget.toASCIIString() + "> (<" + info.getLockToken() + ">)");
		}
		return request;
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return HttpStatus.SC_NO_CONTENT == statusCode;
	}
}
