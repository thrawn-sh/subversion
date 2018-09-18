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
import java.util.Optional;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

public class UploadOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Void> {

    private static final long STREAM_WHOLE_CONTENT = -1L;

    private final InputStream content;

    private final Optional<LockToken> lockToken;

    private final QualifiedResource qualifiedResource;

    public UploadOperationHttpv1(final RepositoryInternal repository, final QualifiedResource qualifiedResource, final Optional<LockToken> lockToken, final InputStream content) {
        super(repository, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
        this.qualifiedResource = qualifiedResource;
        this.lockToken = lockToken;
        this.content = content;
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("PUT", uri);

        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        final HttpEntity entity = new InputStreamEntity(content, STREAM_WHOLE_CONTENT);
        request.setEntity(entity);
        return request;
    }

    @Override
    protected Void processResponse(final HttpResponse response) throws IOException {
        // nothing to do
        return null;
    }

}
