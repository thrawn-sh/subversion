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

import javax.annotation.CheckForNull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.SubversionException;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class PropertiesUpdateOperation extends AbstractVoidOperation {

    static enum Type {
        SET("set"), DELETE("remove");

        final String action;

        private Type(final String action) {
            this.action = action;
        }
    }

    private final String lockToken;

    private final ResourceProperty[] properties;

    private final Resource resource;

    private final Type type;

    PropertiesUpdateOperation(final URI repository, final Resource resource, final Type type, @CheckForNull final String lockToken, final ResourceProperty... properties) {
        super(repository);
        this.resource = resource;
        this.type = type;
        this.lockToken = lockToken;
        this.properties = Arrays.copyOf(properties, properties.length);
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.createURI(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("PROPPATCH", uri);

        if (lockToken != null) {
            request.addHeader("If", "<" + uri + "> (<" + lockToken + ">)");
        }

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
                final String prefix = property.getType().getPrefix();
                if (type == Type.SET) {
                    writer.writeStartElement(prefix, ResourcePropertyUtils.escapedKeyNameXml(property.getName()));
                    writer.writeCharacters(property.getValue());
                    writer.writeEndElement();
                } else {
                    writer.writeEmptyElement(prefix, property.getName());
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
        request.setEntity(new StringEntity(bodyWithoutMakers, CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_MULTI_STATUS == statusCode;
    }

}
