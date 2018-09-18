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

import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public class MkdirOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Boolean> {

    private final QualifiedResource qualifiedResource;

    public MkdirOperationHttpv1(final RepositoryInternal repository, final QualifiedResource qualifiedResource) {
        // created: HttpStatus.SC_CREATED
        // existed: HttpStatus.SC_METHOD_NOT_ALLOWED
        super(repository, HttpStatus.SC_CREATED, HttpStatus.SC_METHOD_NOT_ALLOWED);
        this.qualifiedResource = qualifiedResource;
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        return new DavTemplateRequest("MKCOL", uri);
    }

    @Override
    protected Boolean processResponse(final HttpResponse response) throws IOException {
        final int status = getStatusCode(response);
        return (status == HttpStatus.SC_CREATED);
    }

}
