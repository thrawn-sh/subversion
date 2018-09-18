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
import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

public class GetTransactionIdOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, String> {

    private final String transactionId;

    public GetTransactionIdOperationHttpv1(final RepositoryInternal repository) {
        super(repository, HttpStatus.SC_CREATED);
        final UUID uuid = UUID.randomUUID();
        transactionId = uuid.toString();
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final String prefix = repository.getPrefix();
        final Resource base = Resource.create(prefix);
        final Resource specialResource = Resource.create("act" + Resource.SEPARATOR + transactionId);
        final QualifiedResource qualifiedResource = new QualifiedResource(base, specialResource);
        final URI uri = repository.getRequestUri(qualifiedResource);
        return new DavTemplateRequest("MKACTIVITY", uri);
    }

    @Override
    protected String processResponse(final HttpResponse response) throws IOException {
        return transactionId;
    }
}
