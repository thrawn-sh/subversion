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

import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

abstract class AbstractPropfindOperation<T> extends AbstractOperation<T> {

    private static boolean contains(final ResourceProperty.Key[] propertyKeys, final String namespace) {
        for (final ResourceProperty.Key requestedProperty : propertyKeys) {
            if (namespace.equals(requestedProperty.getType().getPrefix())) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    private static ResourceProperty.Key[] filter(final ResourceProperty.Key[] keys) {
        if (keys == null) {
            return null;
        }

        int w = 0;
        int r = 0;
        while (r < keys.length) {
            if (ResourceProperty.Type.SUBVERSION_CUSTOM.equals(keys[r].getType())) {
                r++;
                continue;
            }
            keys[w++] = keys[r++];
        }
        return Arrays.copyOf(keys, w);
    }

    protected final Depth depth;

    protected final Resource marker;

    protected final ResourceProperty.Key[] propertyKeys;

    protected final QualifiedResource resource;

    AbstractPropfindOperation(final URI repository, final QualifiedResource resource, final Resource marker, final Depth depth) {
        super(repository);
        this.resource = resource;
        this.marker = marker;
        this.depth = depth;
        this.propertyKeys = null;
    }

    AbstractPropfindOperation(final URI repository, final QualifiedResource resource, final Resource marker, final Depth depth,
            final ResourceProperty.Key[] propertyKeys) {
        super(repository);
        this.resource = resource;
        this.marker = marker;
        this.depth = depth;
        this.propertyKeys = filter(propertyKeys);
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", uri);
        request.addHeader("Depth", depth.value);

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("propfind");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            if (propertyKeys != null) {
                if (propertyKeys.length == 0) {
                    writer.writeEmptyElement("allprop");
                } else {
                    writer.writeStartElement("prop");
                    if (contains(propertyKeys, XmlConstants.SUBVERSION_DAV_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                    }
                    if (contains(propertyKeys, XmlConstants.SUBVERSION_SVN_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                    }
                    for (final ResourceProperty.Key requestedProperty : propertyKeys) {
                        writer.writeEmptyElement(requestedProperty.getType().getPrefix(), requestedProperty.getName());
                    }
                    writer.writeEndElement(); // prop
                }
            } else {
                writer.writeEmptyElement("prop");
            }
            writer.writeEndElement(); // propfind
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
