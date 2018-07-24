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
package de.shadowhunt.subversion.internal.httpv2;

import java.net.URI;
import java.util.UUID;

import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.AbstractOperation;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.TransactionImpl;
import de.shadowhunt.subversion.internal.URIUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

class CreateTransactionOperation extends AbstractOperation<TransactionImpl> {

    private static final HttpEntity ENTITY;

    private static final String TRANSACTION_ID_HEADER = "SVN-Txn-Name";

    static {
        final ContentType contentType = ContentType.create("application/vnd.svn-skel");
        ENTITY = new StringEntity("( create-txn )", contentType);
    }

    private final Revision headRevision;

    private final UUID repositoryId;

    private final QualifiedResource resource;

    CreateTransactionOperation(final URI repository, final UUID repositoryId, final QualifiedResource resource, final Revision headRevision) {
        super(repository);
        this.repositoryId = repositoryId;
        this.resource = resource;
        this.headRevision = headRevision;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("POST", uri);
        request.setEntity(ENTITY);
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_CREATED == statusCode;
    }

    @Override
    protected TransactionImpl processResponse(final HttpResponse response) {
        final String transactionId = response.getFirstHeader(TRANSACTION_ID_HEADER).getValue();
        return new TransactionImpl(transactionId, repositoryId, headRevision);
    }
}
