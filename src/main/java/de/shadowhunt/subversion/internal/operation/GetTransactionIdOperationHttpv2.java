/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class GetTransactionIdOperationHttpv2 extends AbstractRepositoryBaseOperation<RepositoryInternal, String> {

    private static final HttpEntity ENTITY;

    private static final String TRANSACTION_ID_HEADER = "SVN-Txn-Name";

    static {
        final ContentType contentType = ContentType.create("application/vnd.svn-skel");
        ENTITY = new StringEntity("( create-txn )", contentType);
    }

    public GetTransactionIdOperationHttpv2(final RepositoryInternal repository) {
        super(repository, HttpStatus.SC_CREATED);
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final String prefix = repository.getPrefix();
        final Resource base = Resource.create(prefix);
        final Resource suffix = Resource.create("me");
        final QualifiedResource qualifiedResource = new QualifiedResource(base, suffix);

        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("POST", uri);
        request.setEntity(ENTITY);
        return request;
    }

    @Override
    protected String processResponse(final HttpResponse response) throws IOException {
        final Header header = response.getFirstHeader(TRANSACTION_ID_HEADER);
        return header.getValue();
    }
}
