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
package de.shadowhunt.subversion.internal.operation;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.ReadOnlyRepositoryInternal;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public abstract class AbstractPropfindOperationHttpv1<E> extends AbstractRepositoryBaseOperation<ReadOnlyRepositoryInternal, E> {

    private static boolean contains(final List<Key> keys, final String namespaceUri) {
        for (final Key key : keys) {
            final Type type = key.getType();
            final String namespace = type.getNamespace();
            if (namespaceUri.equals(namespace)) {
                return true;
            }
        }
        return false;
    }

    private static List<Key> filter(final Key... keys) {
        final List<Key> result = new ArrayList<>(keys.length);
        for (final Key key : keys) {
            final Type type = key.getType();
            if (ResourceProperty.Type.SUBVERSION_CUSTOM.equals(type)) {
                continue;
            }
            result.add(key);
        }
        return result;
    }

    protected final Depth depth;

    protected final Key[] propertyKeys;

    protected final QualifiedResource qualifiedResource;

    protected AbstractPropfindOperationHttpv1(final ReadOnlyRepositoryInternal repository, final int[] expectedStatusCodes, final QualifiedResource qualifiedResource, final Depth depth, final Key... propertyKeys) {
        super(repository, expectedStatusCodes);
        this.qualifiedResource = qualifiedResource;
        this.depth = depth;
        this.propertyKeys = Arrays.copyOf(propertyKeys, propertyKeys.length);
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPFIND", uri);
        request.addHeader("Depth", depth.value);

        final HttpEntity entity = createRequestBody();
        request.setEntity(entity);
        return request;
    }

    protected HttpEntity createRequestBody() throws IOException {
        try (final Writer body = new StringBuilderWriter()) {
            try {
                final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
                writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
                writer.writeStartElement("propfind");
                writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
                final List<Key> filteredKeys = filter(propertyKeys);
                if (filteredKeys.isEmpty()) {
                    writer.writeEmptyElement("allprop");
                } else {
                    writer.writeStartElement("prop");
                    if (contains(filteredKeys, XmlConstants.SUBVERSION_DAV_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
                    }
                    if (contains(filteredKeys, XmlConstants.SUBVERSION_SVN_NAMESPACE)) {
                        writer.writeNamespace(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                        writer.setPrefix(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
                    }
                    for (final Key key : filteredKeys) {
                        final Type type = key.getType();
                        final String namespace = type.getNamespace();
                        final String name = key.getName();
                        writer.writeEmptyElement(namespace, name);
                    }
                    writer.writeEndElement(); // prop
                }
                writer.writeEndElement(); // propfind
                writer.writeEndDocument();
                writer.close();
            } catch (final XMLStreamException e) {
                throw new IOException("could not create request body", e);
            }

            final String payload = body.toString();
            return new StringEntity(payload, CONTENT_TYPE_XML);
        }
    }
}
