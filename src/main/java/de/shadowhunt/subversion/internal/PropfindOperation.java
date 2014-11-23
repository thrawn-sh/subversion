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

import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;

abstract class PropfindOperation<T> extends AbstractOperation<T> {

    public static final ResourceProperty[] ALL_PROPERTIES = null;

    public static final ResourceProperty[] NO_PROPERTIES = new ResourceProperty[0];

    protected final Depth depth;

    protected final Resource marker;

    protected final ResourceProperty[] requestedProperties;

    protected final Resource resource;

    PropfindOperation(final URI repository, final Resource resource, final Resource marker, final Depth depth, final ResourceProperty[] requestedProperties) {
        super(repository);
        this.resource = resource;
        this.marker = marker;
        this.depth = depth;
        this.requestedProperties = requestedProperties;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", uri);
        request.addHeader("Depth", depth.value);

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("propfind");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            if (requestedProperties == null) {
                writer.writeEmptyElement("allprop");
            } else {
                if (requestedProperties.length == 0) {
                    writer.writeEmptyElement("prop");
                } else {
                    // FIXME
                }
            }
            writer.writeEndElement(); //propfind
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_MULTI_STATUS == statusCode || HttpStatus.SC_NOT_FOUND == statusCode;
    }

}
