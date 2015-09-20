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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.annotation.CheckForNull;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.TransmissionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

class DownloadOperation extends AbstractOperation<InputStream> {

    private final Resource resource;

    DownloadOperation(final URI repository, final Resource resource) {
        super(repository);
        this.resource = resource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        return new HttpGet(uri);
    }

    @Override
    @CheckForNull
    public InputStream execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();

        try {
            final HttpResponse response = client.execute(request, context);
            if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            final InputStream content = getContent(response);
            check(response);
            return content;
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }
    }

    @Override
    public InputStream handleResponse(final HttpResponse response) {
        return processResponse(response);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_OK == statusCode) || (HttpStatus.SC_NOT_FOUND == statusCode);
    }

    @Override
    protected InputStream processResponse(final HttpResponse response) {
        // we return the content stream
        throw new UnsupportedOperationException();
    }
}
