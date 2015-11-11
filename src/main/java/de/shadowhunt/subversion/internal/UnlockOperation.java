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
package de.shadowhunt.subversion.internal;

import java.net.URI;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class UnlockOperation extends AbstractVoidOperation {

    private final boolean force;

    private final LockToken lockToken;

    private final Resource resource;

    UnlockOperation(final URI repository, final Resource resource, final LockToken lockToken, final boolean force) {
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
