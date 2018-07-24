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
