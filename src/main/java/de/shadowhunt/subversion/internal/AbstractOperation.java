/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.SubversionException;

public abstract class AbstractOperation<T> {

    protected static final ContentType CONTENT_TYPE_XML = ContentType.create("text/xml", "UTF-8");

    protected static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    protected static final class DavTemplateRequest extends HttpEntityEnclosingRequestBase {

        private final String method;

        /**
         * Create a new {@link DavTemplateRequest}
         *
         * @param method HTTP method name
         * @param uri full qualified {@link URI} this {@link DavTemplateRequest} is directed to
         */
        public DavTemplateRequest(final String method, final URI uri) {
            this.method = method;
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    static InputStream getContent(final HttpResponse response) {
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new SubversionException("Invalid server response: entity is missing");
        }

        try {
            return entity.getContent();
        } catch (final Exception e) {
            throw new SubversionException("Invalid server response: content stream is missing", e);
        }
    }

    static int getStatusCode(final HttpResponse response) {
        final StatusLine statusLine = response.getStatusLine();
        return (statusLine == null) ? 0 : statusLine.getStatusCode();
    }

    protected final URI repository;

    protected AbstractOperation(final URI repository) {
        this.repository = repository;
    }

    protected void check(final HttpResponse response, final URI requestUri) {
        final int statusCode = getStatusCode(response);

        if (isExpectedStatusCode(statusCode)) {
            return;
        }

        final String message;
        switch (statusCode) {
            case HttpStatus.SC_FORBIDDEN:
                message = "Insufficient permissions to execute operation on url:" + requestUri;
                break;
            case HttpStatus.SC_NOT_FOUND:
                message = "Requested url: " + requestUri + " could not be found";
                break;
            case HttpStatus.SC_LOCKED:
                message = "Requested url: " + requestUri + " is locked by another user";
                break;
            default:
                message = "Unexpected server response";
        }

        // in case of unexpected status code we consume everything
        EntityUtils.consumeQuietly(response.getEntity());

        throw new SubversionException(message, statusCode);
    }

    /**
     * as the Resource can not differ between files and directories
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

    /**
     * Run the {@link AbstractOperation} against the server
     *
     * @param client {@link HttpClient} to use for this {@link AbstractOperation}
     * @param context {@link HttpContext} to use for this {@link AbstractOperation}
     *
     * @return expected return value or {@code Void} if non is expected
     */
    public T execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();
        final HttpResponse response = executeRequest(request, client, context);
        check(response, request.getURI());
        return processResponse(response);
    }

    final HttpResponse executeRequest(final HttpUriRequest request, final HttpClient client, final HttpContext context) {
        clearRedirects(context);
        try {
            return client.execute(request, context);
        } catch (final Exception e) {
            throw new SubversionException("Could not execute request (" + request + ')', e);
        }
    }

    protected abstract boolean isExpectedStatusCode(final int statusCode);

    protected abstract T processResponse(final HttpResponse response);
}
