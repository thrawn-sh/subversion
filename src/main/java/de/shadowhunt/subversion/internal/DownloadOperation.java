/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.shadowhunt.subversion.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import de.shadowhunt.subversion.TransmissionException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

class DownloadOperation extends AbstractOperation<Optional<InputStream>> {

    private final QualifiedResource resource;

    DownloadOperation(final URI repository, final QualifiedResource resource) {
        super(repository);
        this.resource = resource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        return new DavTemplateRequest("GET", uri);
    }

    @Override
    public Optional<InputStream> execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();

        try {
            final HttpResponse response = client.execute(request, context);
            if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            }
            final InputStream content = getContent(response);
            check(response);
            return Optional.of(content);
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }
    }

    @Override
    public Optional<InputStream> handleResponse(final HttpResponse response) {
        return processResponse(response);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_OK == statusCode) || (HttpStatus.SC_NOT_FOUND == statusCode);
    }

    @Override
    protected Optional<InputStream> processResponse(final HttpResponse response) {
        // we return the content stream
        throw new UnsupportedOperationException();
    }
}
