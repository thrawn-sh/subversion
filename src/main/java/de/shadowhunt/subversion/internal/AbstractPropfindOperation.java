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

    AbstractPropfindOperation(final URI repository, final QualifiedResource resource, final Resource marker, final Depth depth, final ResourceProperty.Key[] propertyKeys) {
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
