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
import java.util.Optional;

import de.shadowhunt.subversion.LockToken;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class DeleteOperation extends AbstractVoidOperation {

    private final Optional<LockToken> lockToken;

    private final QualifiedResource resource;

    DeleteOperation(final URI repository, final QualifiedResource resource, final Optional<LockToken> lockToken) {
        super(repository);
        this.resource = resource;
        this.lockToken = lockToken;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final HttpUriRequest request = new DavTemplateRequest("DELETE", uri);

        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_NO_CONTENT == statusCode;
    }
}
