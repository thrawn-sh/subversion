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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Log;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class LogOperation extends AbstractOperation<List<Log>> {

	private final Revision end;

	private final int limit;

	private final Resource resource;

	private final Revision start;

	public LogOperation(final URI repository, final Resource resource, final Revision start, final Revision end, final int limit) {
		super(repository);
		this.resource = resource;
		this.start = start;
		this.end = end;
		this.limit = limit;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder body = new StringBuilder(XML_PREAMBLE);
		body.append("<log-report xmlns=\"svn:\"><start-revision>");
		body.append(start);
		body.append("</start-revision><end-revision>");
		body.append(end);
		body.append("</end-revision>");
		if (limit > 0) {
			body.append("<limit>");
			body.append(limit);
			body.append("</limit>");
		}
		body.append("<discover-changed-paths/><encode-binary-props/><all-revprops/><path/></log-report>");

		request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected List<Log> processResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_OK);

		final InputStream in = getContent(response);
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final List<Log> logs = ((List) LogImpl.read(in));
			return logs;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
