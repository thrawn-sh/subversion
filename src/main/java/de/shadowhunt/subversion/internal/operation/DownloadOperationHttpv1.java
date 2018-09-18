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
package de.shadowhunt.subversion.internal.operation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import de.shadowhunt.subversion.TransmissionException;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

public class DownloadOperationHttpv1 extends AbstractRepositoryBaseOperation<ReadOnlyRepositoryInternal, InputStream> {

    private final QualifiedResource qualifiedResource;

    public DownloadOperationHttpv1(final ReadOnlyRepositoryInternal repository, final QualifiedResource qualifiedResource) {
        super(repository, HttpStatus.SC_OK);
        this.qualifiedResource = qualifiedResource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = repository.getRequestUri(qualifiedResource);
        return new DavTemplateRequest("GET", uri);
    }

    @Override
    public InputStream execute() {
        clearRedirects();
        try {
            final HttpUriRequest request = createRequest();

            final HttpClient client = repository.getClient();
            final HttpContext context = repository.getContext();
            // do not use ResponseHandler feature, as it will close the InputStream
            final HttpResponse response = client.execute(request, context);
            return handleResponse(response);
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }
    }

    @Override
    protected InputStream processResponse(final HttpResponse response) throws IOException {
        return getContent(response);
    }

}
