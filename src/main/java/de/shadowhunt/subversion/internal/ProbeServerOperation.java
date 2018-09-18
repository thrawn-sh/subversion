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
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.ServiceLoader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.ReadOnlyRepository.ProtocolVersion;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.TransmissionException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

class ProbeServerOperation extends AbstractOperation<Repository> {

    private static ProtocolVersion determineVersion(final Header... headers) {
        for (final Header header : headers) {
            final String name = header.getName();
            if (name.startsWith("SVN")) {
                return ProtocolVersion.HTTP_V2;
            }
        }
        return ProtocolVersion.HTTP_V1;
    }

    ProbeServerOperation(final URI repository) {
        super(repository);
    }

    @Override
    protected HttpUriRequest createRequest() {
        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("options");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeEmptyElement("activity-collection-set");
            writer.writeEndElement(); // options
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final DavTemplateRequest request = new DavTemplateRequest("OPTIONS", repository);
        final String payload = body.toString();
        final StringEntity entity = new StringEntity(payload, CONTENT_TYPE_XML);
        request.setEntity(entity);
        return request;
    }

    @Override
    public Repository execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();

        final ProtocolVersion version;
        final Probe probe;
        try {
            final HttpResponse response = client.execute(request, context);
            try (final InputStream in = getContent(response)) {
                check(response);

                final Header[] headers = response.getAllHeaders();
                version = determineVersion(headers);
                probe = ProbeReader.read(in, version);
            }
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }

        for (final RepositoryLocator repositoryLocator : ServiceLoader.load(RepositoryLocator.class)) {
            if (repositoryLocator.isSupported(version)) {
                return repositoryLocator.create(repository, probe, client, context);
            }
        }

        throw new SubversionException("Could not find suitable repository for " + repository);
    }

    @Override
    public Repository handleResponse(final HttpResponse response) {
        return processResponse(response);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_OK == statusCode;
    }

    @Override
    protected Repository processResponse(final HttpResponse response) {
        // we need client and context to create the repository
        throw new UnsupportedOperationException();
    }
}
