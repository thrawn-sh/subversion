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

import de.shadowhunt.subversion.LockToken;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class UnlockOperation extends AbstractVoidOperation {

    private final boolean force;

    private final LockToken lockToken;

    private final QualifiedResource resource;

    UnlockOperation(final URI repository, final QualifiedResource resource, final LockToken lockToken, final boolean force) {
        super(repository);
        this.resource = resource;
        this.lockToken = lockToken;
        this.force = force;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("UNLOCK", uri);
        request.addHeader("Lock-Token", '<' + lockToken.toString() + '>');
        if (force) {
            request.addHeader("X-SVN-Options", "lock-break");
        }
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_NO_CONTENT == statusCode;
    }
}
