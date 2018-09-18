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
import java.net.URI;
import java.util.Optional;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public class CopyOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Void> {

    private final Optional<LockToken> lockToken;

    private final QualifiedResource source;

    private final QualifiedResource target;

    public CopyOperationHttpv1(final RepositoryInternal repository, final QualifiedResource source, final QualifiedResource target, final Optional<LockToken> lockToken) {
        super(repository, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
        this.source = source;
        this.target = target;
        this.lockToken = lockToken;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI sourceUri = repository.getRequestUri(source);
        final DavTemplateRequest request = new DavTemplateRequest("COPY", sourceUri);
        final URI targetUri = repository.getRequestUri(target);
        final String asciiTarget = targetUri.toASCIIString();
        request.addHeader("Destination", asciiTarget);
        request.addHeader("Depth", Depth.INFINITY.value);
        request.addHeader("Override", "T");

        lockToken.ifPresent(x -> request.addHeader("If", "<" + targetUri + "> (<" + x + ">)"));

        return request;
    }

    @Override
    protected Void processResponse(final HttpResponse response) throws IOException {
        // nothing to do
        return null;
    }
}
