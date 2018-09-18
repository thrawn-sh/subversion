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
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class PropertiesUpdateOperation extends AbstractVoidOperation {

    enum Type {
        DELETE("remove"), SET("set");

        final String action;

        Type(final String action) {
            this.action = action;
        }
    }

    private final Optional<LockToken> lockToken;

    private final ResourceProperty[] properties;

    private final QualifiedResource resource;

    private final Type type;

    PropertiesUpdateOperation(final URI repository, final QualifiedResource resource, final Type type, final Optional<LockToken> lockToken, final ResourceProperty... properties) {
        super(repository);
        this.resource = resource;
        this.type = type;
        this.lockToken = lockToken;
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("propertyupdate");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.setPrefix(XmlConstants.SUBVERSION_CUSTOM_PREFIX, XmlConstants.SUBVERSION_CUSTOM_NAMESPACE);
            writer.writeNamespace(XmlConstants.SUBVERSION_CUSTOM_PREFIX, XmlConstants.SUBVERSION_CUSTOM_NAMESPACE);
            writer.setPrefix(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
            writer.writeNamespace(XmlConstants.SUBVERSION_DAV_PREFIX, XmlConstants.SUBVERSION_DAV_NAMESPACE);
            writer.setPrefix(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
            writer.writeNamespace(XmlConstants.SUBVERSION_SVN_PREFIX, XmlConstants.SUBVERSION_SVN_NAMESPACE);
            writer.writeStartElement(type.action);
            writer.writeStartElement("prop");
            for (final ResourceProperty property : properties) {
                final ResourceProperty.Type propertyType = property.getType();
                final String prefix = propertyType.getPrefix();
                final String propertyName = property.getName();
                if (type == Type.SET) {
                    final String escapedPropertyName = ResourcePropertyUtils.escapedKeyNameXml(propertyName);
                    writer.writeStartElement(prefix, escapedPropertyName);
                    final String propertyValue = property.getValue();
                    writer.writeCharacters(propertyValue);
                    writer.writeEndElement();
                } else {
                    writer.writeEmptyElement(prefix, propertyName);
                }
            }
            writer.writeEndElement(); // prop
            writer.writeEndElement(); // set || delete
            writer.writeEndElement(); // propertyupdate
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final String bodyWithMakers = body.toString();
        final String bodyWithoutMakers = ResourcePropertyUtils.filterMarker(bodyWithMakers);
        final StringEntity entity = new StringEntity(bodyWithoutMakers, CONTENT_TYPE_XML);
        request.setEntity(entity);
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_MULTI_STATUS == statusCode;
    }

}
