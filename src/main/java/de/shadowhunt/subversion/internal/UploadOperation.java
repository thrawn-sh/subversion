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

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class UploadOperation extends AbstractVoidOperation {

	protected static final int PREFIX = 4; // /$svn/{baseline}/{id}/

	protected static final long STREAM_WHOLE_CONTENT = -1L;

	protected final InputStream content;

	protected final String lock;

	protected final Resource resource;

	public UploadOperation(final URI repository, final Resource resource, final String lock, final InputStream content) {
		super(repository);
		this.resource = resource;
		this.lock = lock;
		this.content = content;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final HttpPut request = new HttpPut(uri);
		if (lock != null) {
			final URI lockTarget = URIUtils.createURI(repository, resource.subResource(PREFIX));
			request.addHeader("If", "<" + lockTarget.toASCIIString() + "> (<" + lock + ">)");
		}

		request.setEntity(new InputStreamEntity(content, STREAM_WHOLE_CONTENT));
		return request;
	}
}
