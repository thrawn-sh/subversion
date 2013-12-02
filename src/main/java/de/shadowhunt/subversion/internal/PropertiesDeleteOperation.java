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
import java.util.Arrays;

import javax.annotation.Nullable;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;

class PropertiesDeleteOperation extends AbstractVoidOperation {

	private final Info info;

	private final ResourceProperty[] properties;

	private final Resource resource;

	PropertiesDeleteOperation(final URI repository, final Resource resource, @Nullable final Info info, final ResourceProperty[] properties) {
		super(repository);
		this.resource = resource;
		this.info = info;
		this.properties = Arrays.copyOf(properties, properties.length);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

		if ((info != null) && info.isLocked()) {
			final URI lockTarget = URIUtils.createURI(repository, info.getResource());
			request.addHeader("If", '<' + lockTarget.toASCIIString() + "> (<" + info.getLockToken() + ">)");
		}

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<propertyupdate xmlns=\"DAV:\" xmlns:C=\"http://subversion.tigris.org/xmlns/custom/\" xmlns:S=\"http://subversion.tigris.org/xmlns/svn/\" xmlns:V=\"http://subversion.tigris.org/xmlns/dav/\"><remove><prop>");
		for (final ResourceProperty property : properties) {
			sb.append('<');
			sb.append(property.getType().getPrefix());
			sb.append(property.getName());
			sb.append("/>");
		}
		sb.append("</prop></remove></propertyupdate>");
		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return HttpStatus.SC_MULTI_STATUS == statusCode;
	}

}
