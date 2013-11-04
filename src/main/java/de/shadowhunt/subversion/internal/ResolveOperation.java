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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class ResolveOperation extends AbstractOperation<Resource> {

	private final RepositoryConfig config;

	private final Revision expected;

	private final boolean reportNonExistingResources;

	private final Resource resource;

	private final Revision revision;

	public ResolveOperation(final URI repository, final Resource resource, final Revision revision, final Revision expected, final RepositoryConfig config, final boolean reportNonExistingResources) {
		super(repository);
		this.resource = resource;
		this.revision = revision;
		this.expected = expected;
		this.config = config;
		this.reportNonExistingResources = reportNonExistingResources;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<get-locations xmlns=\"svn:\"><path/><peg-revision>");
		sb.append(revision);
		sb.append("</peg-revision><location-revision>");
		sb.append(expected);
		sb.append("</location-revision></get-locations>");

		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected Resource processResponse(final HttpResponse response) {
		if (reportNonExistingResources) {
			check(response, HttpStatus.SC_OK);
		} else {
			check(response, HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND);
			final int statusCode = getStatusCode(response);
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				EntityUtils.consumeQuietly(response.getEntity());
				return null;
			}
		}

		final InputStream in = getContent(response);
		try {
			final Resolve resolve = Resolve.read(in);
			return config.getVersionedResource(resolve.getResource(), expected);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
