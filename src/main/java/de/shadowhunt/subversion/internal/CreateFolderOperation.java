/**
 * Copyright (C) 2013-2017 shadowhunt (dev@shadowhunt.de)
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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class CreateFolderOperation extends AbstractOperation<Boolean> {

    private final QualifiedResource resource;

    CreateFolderOperation(final URI repository, final QualifiedResource resource) {
        super(repository);
        this.resource = resource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        return new DavTemplateRequest("MKCOL", uri);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        // created: HttpStatus.SC_CREATED
        // existed: HttpStatus.SC_METHOD_NOT_ALLOWED
        return (HttpStatus.SC_CREATED == statusCode) || (HttpStatus.SC_METHOD_NOT_ALLOWED == statusCode);
    }

    @Override
    protected Boolean processResponse(final HttpResponse response) {
        final int status = getStatusCode(response);
        return (status == HttpStatus.SC_CREATED);
    }

}
