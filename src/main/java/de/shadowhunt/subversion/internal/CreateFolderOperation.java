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

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class CreateFolderOperation extends AbstractOperation<Boolean> {

    private final QualifiedResource resource;

    CreateFolderOperation(final URI repository, final QualifiedResource resource) {
        super(repository);
        this.resource = resource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        return new DavTemplateRequest("MKCOL", uri);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        // created: HttpStatus.SC_CREATED
        // existed: HttpStatus.SC_METHOD_NOT_ALLOWED
        return (HttpStatus.SC_CREATED == statusCode) || (HttpStatus.SC_METHOD_NOT_ALLOWED == statusCode);
    }

    @Override
    protected Boolean processResponse(final HttpResponse response) {
        final int status = getStatusCode(response);
        return (status == HttpStatus.SC_CREATED);
    }

}
