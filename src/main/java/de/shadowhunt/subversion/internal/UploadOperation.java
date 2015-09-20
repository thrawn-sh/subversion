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

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Resource;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;

class UploadOperation extends AbstractVoidOperation {

    private static final long STREAM_WHOLE_CONTENT = -1L;

    private final InputStream content;

    private final Optional<LockToken> lockToken;

    private final Resource resource;

    UploadOperation(final URI repository, final Resource resource, final Optional<LockToken> lockToken, final InputStream content) {
        super(repository);
        this.resource = resource;
        this.lockToken = lockToken;
        this.content = content;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final HttpPut request = new HttpPut(uri);

        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        request.setEntity(new InputStreamEntity(content, STREAM_WHOLE_CONTENT));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_CREATED == statusCode) || (HttpStatus.SC_NO_CONTENT == statusCode);
    }
}
