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
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Version;

public class ProbeServerOperation extends AbstractOperation<RepositoryConfig> {

	protected static Version determineVersion(final HttpResponse response) {
		for (final Header header : response.getAllHeaders()) {
			if (header.getName().startsWith("SVN")) {
				return Version.HTTPv2;
			}
		}
		return Version.HTTPv1;
	}

	public ProbeServerOperation(final URI repository) {
		super(repository);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final DavTemplateRequest request = new DavTemplateRequest("OPTIONS", repository);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<options xmlns=\"DAV:\"><activity-collection-set/></options>");
		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));

		return request;
	}

	@Override
	protected RepositoryConfig processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);

		final Version version = determineVersion(response);
		final Resource prefix;
		final InputStream in = getContent(response);
		try {
			prefix = Prefix.read(in, version);
		} finally {
			IOUtils.closeQuietly(in);
		}

		if (Version.HTTPv2 == version) {
			return new de.shadowhunt.subversion.internal.httpv2.RepositoryConfig(prefix);
		}
		// oldest protocol is fallback, in case we determine the wrong
		// protocol, server could still provide backwards compatibility
		return new de.shadowhunt.subversion.internal.httpv1.RepositoryConfig(prefix);
	}
}
