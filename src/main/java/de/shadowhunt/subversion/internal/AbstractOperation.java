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
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;

public abstract class AbstractOperation<T> implements Operation<T> {

	protected static final ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", "UTF-8");

	protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

	protected static void check(final HttpResponse response, final int... expectedStatusCodes) {
		final int statusCode = getStatusCode(response);

		if (isExpected(statusCode, expectedStatusCodes)) {
			return;
		}

		// FIXME throw more detailed exceptions

		EntityUtils.consumeQuietly(response.getEntity()); // in case of unexpected status code we consume everything
		throw new SubversionException("status code is: " + statusCode + ", expected was: "
				+ Arrays.toString(expectedStatusCodes));
	}

	static InputStream getContent(final HttpResponse response) {
		final HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new SubversionException("response without entity");
		}

		try {
			return entity.getContent();
		} catch (final Exception e) {
			throw new SubversionException("could not retrieve content stream", e);
		}
	}

	static int getStatusCode(final HttpResponse response) {
		final StatusLine statusLine = response.getStatusLine();
		return (statusLine == null) ? 0 : statusLine.getStatusCode();
	}

	private static boolean isExpected(final int statusCode, final int... expectedStatusCodes) {
		for (final int expectedStatusCode : expectedStatusCodes) {
			if (expectedStatusCode == statusCode) {
				return true;
			}
		}
		return false;
	}

	protected final URI repository;

	protected AbstractOperation(final URI repository) {
		this.repository = repository;
	}

	/**
	 * as the {@link Resource} can not differ between files and directories
	 * each request for an directory (without ending '/') will result
	 * in a redirect (with ending '/'), if another call to a redirected
	 * URI occurs a CircularRedirectException is thrown, as we can't
	 * determine the real target we can't prevent this from happening.
	 * Allowing circular redirects globally could lead to live locks on
	 * the other hand. Therefore we clear the redirection cache explicitly.
	 */
	final void clearRedirects(final HttpContext context) {
		context.removeAttribute(DefaultRedirectStrategy.REDIRECT_LOCATIONS);
	}

	protected abstract HttpUriRequest createRequest();

	@Override
	public T execute(final HttpClient client, final HttpContext context) {
		final HttpUriRequest request = createRequest();
		final HttpResponse response = executeRequest(request, client, context);
		return processResponse(response);
	}

	final HttpResponse executeRequest(final HttpUriRequest request, final HttpClient client, final HttpContext context) {
		clearRedirects(context);
		try {
			return client.execute(request, context);
		} catch (final Exception e) {
			throw new SubversionException("could not execute request (" + request + ')', e);
		}
	}

	protected abstract T processResponse(final HttpResponse response);
}
