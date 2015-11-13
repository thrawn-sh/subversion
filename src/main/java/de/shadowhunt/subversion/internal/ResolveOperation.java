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

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.internal.AbstractBaseRepository.ResourceMapper;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class ResolveOperation extends AbstractOperation<Optional<QualifiedResource>> {

    private final ResourceMapper config;

    private final Revision expected;

    private final QualifiedResource resource;

    private final Revision revision;

    ResolveOperation(final URI repository, final QualifiedResource resource, final Revision revision, final Revision expected, final ResourceMapper config) {
        super(repository);
        this.resource = resource;
        this.revision = revision;
        this.expected = expected;
        this.config = config;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("get-locations");
            writer.writeDefaultNamespace(XmlConstants.SVN_NAMESPACE);
            writer.writeEmptyElement("path");
            writer.writeStartElement("peg-revision");
            writer.writeCharacters(revision.toString());
            writer.writeEndElement(); // peg-revision
            writer.writeStartElement("location-revision");
            writer.writeCharacters(expected.toString());
            writer.writeEndElement(); // location-revision
            writer.writeEndElement(); // get-locations
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);
        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_OK == statusCode) || (HttpStatus.SC_NOT_FOUND == statusCode);
    }

    @Override
    protected Optional<QualifiedResource> processResponse(final HttpResponse response) throws IOException {
        final int statusCode = getStatusCode(response);
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            return Optional.empty();
        }

        final Resolve resolve = Resolve.read(getContent(response));
        return Optional.of(config.getVersionedResource(new QualifiedResource(resolve.getResource()), expected));
    }
}
