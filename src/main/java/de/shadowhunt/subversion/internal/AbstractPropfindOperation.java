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
import java.util.Arrays;
import java.util.Optional;

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

<<<<<<< Updated upstream
    @CheckForNull
    private static ResourceProperty.Key[] filter(final ResourceProperty.Key... requestedProperties) {
        if (requestedProperties == null) {
            return null;
=======
    private static Optional<ResourceProperty.Key[]> filter(final Optional<ResourceProperty.Key[]> propertyKeys) {
        if (!propertyKeys.isPresent()) {
            return Optional.empty();
>>>>>>> Stashed changes
        }

        final ResourceProperty.Key[] keys = propertyKeys.get();
        int w = 0;
        int r = 0;
<<<<<<< Updated upstream
        final ResourceProperty.Key[] filteredKeys = new ResourceProperty.Key[requestedProperties.length];
        while (r < requestedProperties.length) {
            if (ResourceProperty.Type.SUBVERSION_CUSTOM.equals(requestedProperties[r].getType())) {
                r++;
                continue;
            }
            filteredKeys[w++] = requestedProperties[r++];
        }
        return Arrays.copyOf(filteredKeys, w);
=======
        while (r < keys.length) {
            if (ResourceProperty.Type.SUBVERSION_CUSTOM.equals(keys[r].getType())) {
                r++;
                continue;
            }
            keys[w++] = keys[r++];
        }
        return Optional.of(Arrays.copyOf(keys, w));
>>>>>>> Stashed changes
    }

    protected final Depth depth;

    protected final Resource marker;

    protected final Optional<ResourceProperty.Key[]> propertyKeys;

    protected final Resource resource;

<<<<<<< Updated upstream
    AbstractPropfindOperation(final URI repository, final Resource resource, final Resource marker, final Depth depth, @CheckForNull final ResourceProperty.Key... requestedProperties) {
=======
    AbstractPropfindOperation(final URI repository, final Resource resource, final Resource marker, final Depth depth, final Optional<ResourceProperty.Key[]> propertyKeys) {
>>>>>>> Stashed changes
        super(repository);
        this.resource = resource;
        this.marker = marker;
        this.depth = depth;
        this.propertyKeys = filter(propertyKeys);
    }

    private boolean contains(final ResourceProperty.Key[] propertyKeys, final String namespace) {
        for (final ResourceProperty.Key requestedProperty : propertyKeys) {
            if (namespace.equals(requestedProperty.getType().getPrefix())) {
                return true;
            }
        }
        return false;
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
            if (propertyKeys.isPresent()) {
                final ResourceProperty.Key[] keys = propertyKeys.get();
                if (keys.length == 0) {
                    writer.writeEmptyElement("allprop");
                } else {
                    writer.writeStartElement("prop");
                    if (contains(keys, XmlConstants.SUBVERSION_DAV_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                    }
                    if (contains(keys, XmlConstants.SUBVERSION_SVN_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                    }
                    for (final ResourceProperty.Key requestedProperty : keys) {
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
