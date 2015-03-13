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

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.util.ServiceLoader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Repository.ProtocolVersion;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.SubversionException;
import de.shadowhunt.subversion.TransmissionException;

import org.apache.commons.io.IOUtils;
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
            if (header.getName().startsWith("SVN")) {
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
        final DavTemplateRequest request = new DavTemplateRequest("OPTIONS", repository);

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("options");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeEmptyElement("activity-collection-set");
            writer.writeEndElement(); //options
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body: " + e.getMessage(), e);
        }
        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));

        return request;
    }

    @Override
    public Repository execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();

        final ProtocolVersion version;
        final Resource prefix;
        InputStream in = null;
        try {
            final HttpResponse response = client.execute(request, context);
            in = getContent(response);
            check(response);

            version = determineVersion(response.getAllHeaders());
            prefix = Prefix.read(in, version);
        } catch (final IOException e) {
            throw new TransmissionException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        for (final RepositoryLocator repositoryLocator : ServiceLoader.load(RepositoryLocator.class)) {
            if (repositoryLocator.isSupported(version)) {
                return repositoryLocator.create(repository, prefix, client, context);
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
