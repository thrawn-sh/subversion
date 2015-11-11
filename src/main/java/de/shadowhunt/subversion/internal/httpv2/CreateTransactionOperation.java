/**
 * Copyright (C) 2013-2015 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal.httpv2;

import java.net.URI;
import java.util.UUID;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.AbstractOperation;
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

    private final Revision headRevision;

    private final UUID repositoryId;

    private final Resource resource;

    CreateTransactionOperation(final URI repository, final UUID repositoryId, final Resource resource, final Revision headRevision) {
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

    static {
        final ContentType contentType = ContentType.create("application/vnd.svn-skel");
        ENTITY = new StringEntity("( create-txn )", contentType);
    }
}
