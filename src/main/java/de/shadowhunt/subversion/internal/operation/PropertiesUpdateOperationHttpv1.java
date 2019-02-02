/**
 * Shadowhunt Subversion - Streaming subversion library without the need for a local sandbox
 * Copyright © 2013-2019 shadowhunt (dev@shadowhunt.de)
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
import java.util.Arrays;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.ResourceProperty.Key;
import de.shadowhunt.subversion.ResourceProperty.Type;
import de.shadowhunt.subversion.internal.QualifiedResource;
import de.shadowhunt.subversion.internal.RepositoryInternal;
import de.shadowhunt.subversion.internal.ResourcePropertyUtils;
import de.shadowhunt.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

public class PropertiesUpdateOperationHttpv1 extends AbstractRepositoryBaseOperation<RepositoryInternal, Void> {

    private final Optional<LockToken> lockToken;

    private final ResourceProperty[] properties;

    private final QualifiedResource qualifiedResource;

    private final boolean setProperties;

    public PropertiesUpdateOperationHttpv1(final RepositoryInternal repository, final QualifiedResource qualifiedResource, final boolean setProperties, final Optional<LockToken> lockToken, final ResourceProperty... properties) {
        super(repository, HttpStatus.SC_MULTI_STATUS);
        this.qualifiedResource = qualifiedResource;
        this.setProperties = setProperties;
        this.lockToken = lockToken;
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    @Override
    protected HttpUriRequest createRequest() throws IOException {
        final URI uri = repository.getRequestUri(qualifiedResource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);
        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        final HttpEntity entity = createRequestBody();
        request.setEntity(entity);
        return request;
    }

    protected HttpEntity createRequestBody() throws IOException {
        try (final Writer body = new StringBuilderWriter()) {
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
                if (setProperties) {
                    writeProperties(writer, "set");
                } else {
                    writeProperties(writer, "remove");
                }
                writer.writeEndElement(); // propertyupdate
                writer.writeEndDocument();
                writer.close();
            } catch (final XMLStreamException e) {
                throw new IOException("could not create request body", e);
            }

            final String payload = body.toString();
            return new StringEntity(payload, CONTENT_TYPE_XML);
        }
    }

    @Override
    protected Void processResponse(final HttpResponse response) throws IOException {
        // nothing to do
        return null;
    }

    private void writeProperties(final XMLStreamWriter writer, final String action) throws XMLStreamException {
        writer.writeStartElement(action);
        writer.writeStartElement("prop");
        for (final ResourceProperty property : properties) {
            final Key key = property.getKey();
            final Type type = key.getType();
            final String namespace = type.getNamespace();
            final String name = key.getName();
            if (setProperties) {
                final String escapedPropertyName = ResourcePropertyUtils.escapedKeyNameXml(name);
                writer.writeStartElement(namespace, escapedPropertyName);
                final String propertyValue = property.getValue();
                writer.writeCharacters(propertyValue);
                writer.writeEndElement();
            } else {
                writer.writeEmptyElement(namespace, name);
            }
        }
        writer.writeEndElement(); // prop
        writer.writeEndElement(); // set || remove
    }

}
