/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
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

import javax.annotation.Nullable;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;

class CopyOperation extends AbstractVoidOperation {

    private final Info info;

    private final Resource source;

    private final Resource target;

    public CopyOperation(final URI repository, final Resource source, final Resource target, @Nullable final Info info) {
        super(repository);
        this.source = source;
        this.target = target;
        this.info = info;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI sourceUri = URIUtils.createURI(repository, source);
        final URI targetUri = URIUtils.createURI(repository, target);
        final DavTemplateRequest request = new DavTemplateRequest("COPY", sourceUri);
        request.addHeader("Destination", targetUri.toASCIIString());
        request.addHeader("Depth", Depth.INFINITY.value);
        request.addHeader("Override", "T");
        if ((info != null) && info.isLocked()) {
            final URI lockTarget = URIUtils.createURI(repository, info.getResource());
            request.addHeader("If", '<' + lockTarget.toASCIIString() + "> (<" + info.getLockToken() + ">)");
        }
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_CREATED == statusCode) || (HttpStatus.SC_NO_CONTENT == statusCode);
    }
}
