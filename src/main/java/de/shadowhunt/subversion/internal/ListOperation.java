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

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.Resource;

class ListOperation extends AbstractOperation<Set<Info>> {

    private final Depth depth;

    private final Resource marker;

    private final VersionParser parser;

    private final Resource resource;

    public ListOperation(final URI repository, final Resource resource, final Depth depth, final VersionParser parser, final Resource marker) {
        super(repository);
        this.resource = resource;
        this.depth = depth;
        this.parser = parser;
        this.marker = marker;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", uri);
        request.addHeader("Depth", depth.value);

        final StringBuilder body = new StringBuilder(XML_PREAMBLE);
        body.append("<propfind xmlns=\"DAV:\"><allprop/></propfind>");

        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_MULTI_STATUS == statusCode;
    }

    @Override
    protected Set<Info> processResponse(final HttpResponse response) {
        final InputStream in = getContent(response);
        try {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            final Set<Info> info = ((Set) InfoImpl.readAll(in, parser, repository.getPath(), marker.getValue()));
            return info;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
